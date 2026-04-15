#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib-stack.sh"

APP_REPLICAS="${IWM_APP_REPLICAS:-1}"
EVENT_COUNT="${IWM_OUTBOX_REPLAY_EVENT_COUNT:-5000}"
TIMEOUT_SECONDS="${IWM_OUTBOX_REPLAY_TIMEOUT_SECONDS:-300}"

cd "${ROOT_DIR}"

env IWM_OUTBOX_RELAY_ENABLED=false IWM_OUTBOX_REPLAY_ON_STARTUP_ENABLED=false \
  docker compose -f "${ROOT_DIR}/docker-compose.infrastructure.yml" -f "${ROOT_DIR}/docker-compose.application.yml" \
  up -d --build --scale app="${APP_REPLICAS}"

wait_for_http "http://localhost:8080/healthz" 240

docker exec iwm-postgres psql -U iwm -d iwm -v event_count="${EVENT_COUNT}" <<'SQL'
CREATE EXTENSION IF NOT EXISTS pgcrypto;
WITH seeded AS (
  SELECT
    gen_random_uuid() AS id,
    '00000000-0000-0000-0000-000000000001'::uuid AS tenant_id,
    NOW() AS created_at,
    NOW() AS updated_at,
    NULL::uuid AS organization_id,
    'LOAD_TEST_REPLAY'::text AS event_type,
    'LOAD_TEST'::text AS aggregate_type,
    gen_random_uuid() AS aggregate_id,
    NOW() AS occurred_at,
    '{"source":"replay-drill"}'::text AS payload,
    'PENDING'::text AS status,
    0 AS attempt_count,
    NULL::timestamptz AS last_attempt_at,
    NOW() AS next_attempt_at,
    NULL::text AS last_error,
    NULL::timestamptz AS dead_lettered_at,
    NULL::timestamptz AS published_at
  FROM generate_series(1, :'event_count')
)
INSERT INTO kernel.outbox_event (
  id, tenant_id, created_at, updated_at, organization_id, event_type, aggregate_type, aggregate_id,
  occurred_at, payload, status, attempt_count, last_attempt_at, next_attempt_at, last_error,
  dead_lettered_at, published_at
)
SELECT * FROM seeded;
SQL

docker compose -f "${ROOT_DIR}/docker-compose.infrastructure.yml" -f "${ROOT_DIR}/docker-compose.application.yml" stop app

env IWM_OUTBOX_RELAY_ENABLED=true IWM_OUTBOX_REPLAY_ON_STARTUP_ENABLED=true \
  docker compose -f "${ROOT_DIR}/docker-compose.infrastructure.yml" -f "${ROOT_DIR}/docker-compose.application.yml" \
  up -d --build --scale app="${APP_REPLICAS}" app

wait_for_http "http://localhost:8080/healthz" 240

deadline=$((SECONDS + TIMEOUT_SECONDS))
while (( SECONDS < deadline )); do
  pending_count="$(docker exec iwm-postgres psql -U iwm -d iwm -Atc "select count(*) from kernel.outbox_event where status = 'PENDING';")"
  if [[ "${pending_count}" == "0" ]]; then
    published_count="$(docker exec iwm-postgres psql -U iwm -d iwm -Atc "select count(*) from kernel.outbox_event where status = 'PUBLISHED';")"
    echo "Outbox replay drill complete: pending=0 published=${published_count}"
    exit 0
  fi
  sleep 5
done

echo "Outbox replay drill timed out before pending backlog drained." >&2
exit 1
