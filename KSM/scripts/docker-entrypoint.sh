#!/usr/bin/env bash
set -euo pipefail

read_secret_file() {
  local env_name="$1"
  local file_env_name="${env_name}_FILE"
  local current_value="${!env_name:-}"
  local file_value="${!file_env_name:-}"

  if [[ -n "${current_value}" && -n "${file_value}" ]]; then
    echo "Both ${env_name} and ${file_env_name} are set. Use only one." >&2
    exit 1
  fi

  if [[ -n "${file_value}" ]]; then
    if [[ ! -f "${file_value}" ]]; then
      echo "Secret file ${file_value} referenced by ${file_env_name} does not exist." >&2
      exit 1
    fi
    export "${env_name}=$(tr -d '\r' < "${file_value}")"
  fi
}

read_secret_file "IWM_BOOTSTRAP_CLIENT_SECRET"
read_secret_file "IWM_MANAGEMENT_API_KEY"
read_secret_file "IWM_R2DBC_PASSWORD"
read_secret_file "IWM_LIQUIBASE_PASSWORD"
read_secret_file "IWM_REDIS_PASSWORD"

exec java ${JAVA_OPTS:-} -jar /app/iwm-backend.jar
