#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

compose_base() {
  docker compose -f "${ROOT_DIR}/docker-compose.infrastructure.yml" -f "${ROOT_DIR}/docker-compose.application.yml" "$@"
}

compose_with_loadtest() {
  docker compose -f "${ROOT_DIR}/docker-compose.infrastructure.yml" \
    -f "${ROOT_DIR}/docker-compose.application.yml" \
    -f "${ROOT_DIR}/docker-compose.loadtest.yml" "$@"
}

wait_for_http() {
  local url="$1"
  local timeout_seconds="${2:-180}"
  local header_name="${3:-}"
  local header_value="${4:-}"
  local deadline=$((SECONDS + timeout_seconds))

  while (( SECONDS < deadline )); do
    if [[ -n "${header_name}" ]]; then
      if curl -fsS -H "${header_name}: ${header_value}" "${url}" >/dev/null 2>&1; then
        return 0
      fi
    else
      if curl -fsS "${url}" >/dev/null 2>&1; then
        return 0
      fi
    fi
    sleep 2
  done

  echo "Timed out waiting for ${url}" >&2
  return 1
}
