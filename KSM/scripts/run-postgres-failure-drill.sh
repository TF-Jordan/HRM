#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib-stack.sh"

APP_REPLICAS="${IWM_APP_REPLICAS:-2}"
PAUSE_SECONDS="${IWM_POSTGRES_FAILURE_PAUSE_SECONDS:-20}"

compose_base up -d --build --scale app="${APP_REPLICAS}"
wait_for_http "http://localhost:8080/healthz" 240

compose_with_loadtest --profile loadtest run --rm k6 run /scripts/load.js &
LOAD_PID=$!

sleep 15
docker pause iwm-postgres
sleep "${PAUSE_SECONDS}"
docker unpause iwm-postgres

wait "${LOAD_PID}"
