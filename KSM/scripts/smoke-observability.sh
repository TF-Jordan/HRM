#!/usr/bin/env bash
set -euo pipefail

APP_URL="${APP_URL:-http://localhost:8080}"
MANAGEMENT_URL="${MANAGEMENT_URL:-http://localhost:8081}"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9090}"
GRAFANA_URL="${GRAFANA_URL:-http://localhost:3000}"
MANAGEMENT_API_KEY="${MANAGEMENT_API_KEY:-${IWM_MANAGEMENT_API_KEY:-}}"
MANAGEMENT_API_KEY="${MANAGEMENT_API_KEY:?MANAGEMENT_API_KEY or IWM_MANAGEMENT_API_KEY is required}"

wait_for_ok() {
  local label="$1"
  local url="$2"
  shift 2

  for _ in {1..60}; do
    if curl -fsS "$@" "${url}" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done

  printf 'Timed out waiting for %s at %s\n' "${label}" "${url}" >&2
  return 1
}

wait_for_match() {
  local label="$1"
  local url="$2"
  local pattern="$3"
  local response
  shift 3

  for _ in {1..60}; do
    if response="$(curl -fsS "$@" "${url}" 2>/dev/null)" && grep -Eq "${pattern}" <<<"${response}"; then
      return 0
    fi
    sleep 2
  done

  printf 'Timed out waiting for %s at %s\n' "${label}" "${url}" >&2
  return 1
}

wait_for_ok "management health" "${MANAGEMENT_URL}/actuator/health"
wait_for_ok "operations health" "${MANAGEMENT_URL}/actuator/health/operations" -H "X-Management-Api-Key: ${MANAGEMENT_API_KEY}"
wait_for_ok "application prometheus metrics" "${MANAGEMENT_URL}/actuator/prometheus" -H "X-Management-Api-Key: ${MANAGEMENT_API_KEY}"
wait_for_match "prometheus query API" "${PROMETHEUS_URL}/api/v1/query?query=up" '"status"[[:space:]]*:[[:space:]]*"success"'
wait_for_match "grafana health API" "${GRAFANA_URL}/api/health" '"database"[[:space:]]*:[[:space:]]*"ok"'

printf 'Observability smoke passed.\n'
