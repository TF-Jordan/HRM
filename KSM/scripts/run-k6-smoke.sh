#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "${ROOT_DIR}"

docker compose \
  -f docker-compose.infrastructure.yml \
  -f docker-compose.application.yml \
  -f docker-compose.loadtest.yml \
  --profile loadtest \
  run --rm k6
