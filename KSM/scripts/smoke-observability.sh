#!/usr/bin/env bash
set -euo pipefail

APP_URL="${APP_URL:-http://localhost:8080}"
MANAGEMENT_URL="${MANAGEMENT_URL:-http://localhost:8081}"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9090}"
GRAFANA_URL="${GRAFANA_URL:-http://localhost:3000}"
MANAGEMENT_API_KEY="${MANAGEMENT_API_KEY:-${IWM_MANAGEMENT_API_KEY:-}}"
MANAGEMENT_API_KEY="${MANAGEMENT_API_KEY:?MANAGEMENT_API_KEY or IWM_MANAGEMENT_API_KEY is required}"

curl -fsS "${MANAGEMENT_URL}/actuator/health" >/dev/null
curl -fsS -H "X-Management-Api-Key: ${MANAGEMENT_API_KEY}" "${MANAGEMENT_URL}/actuator/health/operations" >/dev/null
curl -fsS -H "X-Management-Api-Key: ${MANAGEMENT_API_KEY}" "${MANAGEMENT_URL}/actuator/prometheus" >/dev/null
curl -fsS "${PROMETHEUS_URL}/api/v1/query?query=up" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"success"'
curl -fsS "${GRAFANA_URL}/api/health" | grep -Eq '"database"[[:space:]]*:[[:space:]]*"ok"'

printf 'Observability smoke passed.\n'
