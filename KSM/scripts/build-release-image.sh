#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE_NAME="${IWM_RELEASE_IMAGE_NAME:-iwm-backend-app}"
IMAGE_TAG="${1:-}"

if [[ -z "${IMAGE_TAG}" ]]; then
  echo "usage: $0 <image-tag>" >&2
  echo "example: $0 0.1.0-rc1" >&2
  exit 1
fi

echo "Building Docker release image ${IMAGE_NAME}:${IMAGE_TAG}"

docker build \
  --file "${ROOT_DIR}/Dockerfile" \
  --tag "${IMAGE_NAME}:${IMAGE_TAG}" \
  --tag "${IMAGE_NAME}:latest" \
  "${ROOT_DIR}"

echo "Built images:"
docker image inspect "${IMAGE_NAME}:${IMAGE_TAG}" --format '{{.RepoTags}}'
