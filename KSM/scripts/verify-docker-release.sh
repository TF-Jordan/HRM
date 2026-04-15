#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENVIRONMENT="${1:-preprod}"

case "${ENVIRONMENT}" in
  preprod)
    COMPOSE_FILES=(
      -f "${ROOT_DIR}/docker-compose.infrastructure.yml"
      -f "${ROOT_DIR}/docker-compose.application.yml"
      -f "${ROOT_DIR}/docker-compose.preprod.yml"
    )
    ;;
  prod)
    COMPOSE_FILES=(
      -f "${ROOT_DIR}/docker-compose.infrastructure.yml"
      -f "${ROOT_DIR}/docker-compose.application.yml"
      -f "${ROOT_DIR}/docker-compose.prod.yml"
    )
    ;;
  *)
    echo "unsupported environment: ${ENVIRONMENT}" >&2
    echo "supported values: preprod | prod" >&2
    exit 1
    ;;
esac

echo "Validating runtime environment for ${ENVIRONMENT}"
"${ROOT_DIR}/scripts/validate-runtime-env.sh"

echo "Rendering docker compose config for ${ENVIRONMENT}"
docker compose "${COMPOSE_FILES[@]}" config >/dev/null

echo "Docker release verification passed for ${ENVIRONMENT}"
