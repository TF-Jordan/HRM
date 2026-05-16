#!/usr/bin/env bash
set -euo pipefail

required_vars=(
  SPRING_PROFILES_ACTIVE
  IWM_BOOTSTRAP_CLIENT_ID
  IWM_BOOTSTRAP_CLIENT_SECRET
  IWM_MANAGEMENT_API_KEY
  IWM_R2DBC_URL
  IWM_R2DBC_USERNAME
  IWM_R2DBC_PASSWORD
  IWM_LIQUIBASE_URL
  IWM_LIQUIBASE_USERNAME
  IWM_LIQUIBASE_PASSWORD
  IWM_KAFKA_BOOTSTRAP_SERVERS
  IWM_JWT_KEY_ID
  IWM_JWT_ISSUER
)

invalid=0

for name in "${required_vars[@]}"; do
  value="${!name:-}"
  if [[ -z "${value}" ]]; then
    echo "Missing required environment variable: ${name}" >&2
    invalid=1
    continue
  fi
  if [[ "${value}" == "change-me" || "${value}" == "dev-api-key" || "${value}" == "dev-management-key" ]]; then
    echo "Insecure value detected for ${name}: ${value}" >&2
    invalid=1
  fi
done

if [[ "${SPRING_PROFILES_ACTIVE:-}" == "prod" || "${SPRING_PROFILES_ACTIVE:-}" == "preprod" ]]; then
  if [[ -z "${IWM_JWT_PRIVATE_KEY_PATH:-}" ]]; then
    echo "Missing required environment variable in ${SPRING_PROFILES_ACTIVE}: IWM_JWT_PRIVATE_KEY_PATH" >&2
    invalid=1
  elif [[ ! -f "${IWM_JWT_PRIVATE_KEY_PATH}" ]]; then
    echo "JWT private key file does not exist: ${IWM_JWT_PRIVATE_KEY_PATH}" >&2
    invalid=1
  fi
  if [[ "${IWM_OUTBOX_REPLAY_ON_STARTUP_ENABLED:-true}" != "true" ]]; then
    echo "Unexpected setting: IWM_OUTBOX_REPLAY_ON_STARTUP_ENABLED should stay true in ${SPRING_PROFILES_ACTIVE}." >&2
    invalid=1
  fi
  if [[ "${IWM_OUTBOX_ALLOW_MANUAL_RELAY_ONLY:-false}" == "true" ]]; then
    echo "Unexpected setting: manual relay only is not allowed in ${SPRING_PROFILES_ACTIVE}." >&2
    invalid=1
  fi
fi

if [[ "${invalid}" -ne 0 ]]; then
  exit 1
fi

echo "Runtime environment looks valid."
