#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ "${1:-}" == "--infra-only" ]]; then
  exec "${SCRIPT_DIR}/start-full-stack.sh" --infra-only "${@:2}"
fi

exec "${SCRIPT_DIR}/start-full-stack.sh" --kernel-local "$@"
