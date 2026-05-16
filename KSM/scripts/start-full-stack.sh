#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KERNEL_ROOT_POM="${ROOT_DIR}/pom.xml"
BOOTSTRAP_POM="${ROOT_DIR}/RT-comops-bootstrap/pom.xml"

MODE="app"
RESET_VOLUMES=false
PULL_IMAGES=false

usage() {
  cat <<'USAGE'
Usage: ./scripts/start-full-stack.sh [--app|--infra-only|--kernel-local] [--pull] [--reset-volumes]

Modes:
  --app           Start external infrastructure, backend container, gateway and management stack. Default.
  --infra-only    Start only external services used by a locally launched kernel.
  --kernel-local  Start external services, build the reactor, then run RT-comops-bootstrap locally.

Options:
  --pull          Pull pinned Docker images before starting.
  --reset-volumes Stop the stack and delete Docker volumes first. Required after incompatible DB major upgrades.
USAGE
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

load_dotenv() {
  local env_file="$1"
  local line key value

  while IFS= read -r line || [[ -n "${line}" ]]; do
    line="${line#"${line%%[![:space:]]*}"}"
    line="${line%"${line##*[![:space:]]}"}"

    if [[ -z "${line}" || "${line}" == \#* ]]; then
      continue
    fi

    if [[ "${line}" != *=* ]]; then
      echo "Ignoring invalid dotenv line in ${env_file}: ${line}" >&2
      continue
    fi

    key="${line%%=*}"
    value="${line#*=}"
    key="${key%"${key##*[![:space:]]}"}"
    value="${value#"${value%%[![:space:]]*}"}"
    value="${value%"${value##*[![:space:]]}"}"

    if [[ ! "${key}" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]]; then
      echo "Ignoring invalid dotenv key in ${env_file}: ${key}" >&2
      continue
    fi

    if [[ "${value}" == \"*\" && "${value}" == *\" ]]; then
      value="${value:1:${#value}-2}"
    elif [[ "${value}" == \'*\' && "${value}" == *\' ]]; then
      value="${value:1:${#value}-2}"
    fi

    export "${key}=${value}"
  done < "${env_file}"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --app)
      MODE="app"
      ;;
    --infra-only)
      MODE="infra-only"
      ;;
    --kernel-local)
      MODE="kernel-local"
      ;;
    --pull)
      PULL_IMAGES=true
      ;;
    --reset-volumes)
      RESET_VOLUMES=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
  shift
done

require_cmd docker

if [[ "${MODE}" == "kernel-local" ]]; then
  require_cmd mvn
  require_cmd java
fi

if [[ ! -f "${KERNEL_ROOT_POM}" ]]; then
  echo "Kernel root pom not found: ${KERNEL_ROOT_POM}" >&2
  exit 1
fi

if [[ ! -f "${BOOTSTRAP_POM}" ]]; then
  echo "Bootstrap pom not found: ${BOOTSTRAP_POM}" >&2
  exit 1
fi

cd "${ROOT_DIR}"

if [[ -f ".env.local" ]]; then
  load_dotenv ".env.local"
fi

export IWM_MANAGEMENT_API_KEY="${IWM_MANAGEMENT_API_KEY:-dev-management-key}"
export IWM_BOOTSTRAP_CLIENT_ENABLED="${IWM_BOOTSTRAP_CLIENT_ENABLED:-true}"
export IWM_BOOTSTRAP_CLIENT_ID="${IWM_BOOTSTRAP_CLIENT_ID:-dev-platform-backend}"
export IWM_BOOTSTRAP_CLIENT_NAME="${IWM_BOOTSTRAP_CLIENT_NAME:-Local Platform Backend}"
export IWM_BOOTSTRAP_CLIENT_SECRET="${IWM_BOOTSTRAP_CLIENT_SECRET:-dev-api-key}"
export IWM_BOOTSTRAP_CLIENT_ALLOWED_SERVICES="${IWM_BOOTSTRAP_CLIENT_ALLOWED_SERVICES:-ORGANIZATION,SETTINGS,COMMERCIAL,PRODUCT,INVENTORY,SALES,ACCOUNTING,TREASURY,RESOURCE,HRM,BILLING,BANKING,CASHIER,BLOCKCHAIN}"
export IWM_JWT_AUTO_GENERATE_KEY_PAIR="${IWM_JWT_AUTO_GENERATE_KEY_PAIR:-true}"
export IWM_JWT_KEY_ID="${IWM_JWT_KEY_ID:-iwm-key-local}"
export IWM_JWT_ISSUER="${IWM_JWT_ISSUER:-iwm-backend-local}"
export IWM_R2DBC_URL="${IWM_R2DBC_URL:-r2dbc:postgresql://localhost:5432/iwm}"
export IWM_R2DBC_USERNAME="${IWM_R2DBC_USERNAME:-iwm}"
export IWM_R2DBC_PASSWORD="${IWM_R2DBC_PASSWORD:-iwm}"
export IWM_LIQUIBASE_URL="${IWM_LIQUIBASE_URL:-jdbc:postgresql://localhost:5432/iwm}"
export IWM_LIQUIBASE_USERNAME="${IWM_LIQUIBASE_USERNAME:-iwm}"
export IWM_LIQUIBASE_PASSWORD="${IWM_LIQUIBASE_PASSWORD:-iwm}"
export IWM_KAFKA_BOOTSTRAP_SERVERS="${IWM_KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"
export IWM_REDIS_PERMISSION_CACHE_ENABLED="${IWM_REDIS_PERMISSION_CACHE_ENABLED:-true}"
export IWM_REDIS_HOST="${IWM_REDIS_HOST:-localhost}"
export IWM_REDIS_PORT="${IWM_REDIS_PORT:-6379}"
export IWM_ELASTICSEARCH_SEARCH_ENABLED="${IWM_ELASTICSEARCH_SEARCH_ENABLED:-true}"
export IWM_ELASTICSEARCH_URIS="${IWM_ELASTICSEARCH_URIS:-http://localhost:9200}"
export IWM_TENANT_REQUEST_QUOTA_ENABLED="${IWM_TENANT_REQUEST_QUOTA_ENABLED:-true}"
export IWM_ORGANIZATION_SERVICE_REQUEST_QUOTA_ENABLED="${IWM_ORGANIZATION_SERVICE_REQUEST_QUOTA_ENABLED:-true}"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-r2dbc}"

compose_infra=(docker compose -f docker-compose.infrastructure.yml)
compose_full=(docker compose -f docker-compose.infrastructure.yml -f docker-compose.application.yml -f docker-compose.management.yml)

if [[ "${RESET_VOLUMES}" == "true" ]]; then
  echo "Stopping stack and deleting local Docker volumes."
  "${compose_full[@]}" down -v --remove-orphans
fi

if [[ "${PULL_IMAGES}" == "true" ]]; then
  echo "Pulling pinned Docker images."
  if [[ "${MODE}" == "app" ]]; then
    "${compose_full[@]}" pull --ignore-buildable
  else
    "${compose_infra[@]}" pull
  fi
fi

case "${MODE}" in
  app)
    echo "Starting full containerized stack."
    "${compose_full[@]}" up -d --build --remove-orphans
    echo "Full stack started."
    echo "Public API: http://localhost:${IWM_GATEWAY_PUBLIC_HTTP_PORT:-8080}"
    echo "Management: http://localhost:8081/actuator/health"
    ;;
  infra-only)
    echo "Starting external services only."
    "${compose_infra[@]}" up -d --remove-orphans
    echo "External services started and ready for a local kernel."
    echo "Postgres:      ${IWM_R2DBC_URL}"
    echo "Kafka:         ${IWM_KAFKA_BOOTSTRAP_SERVERS}"
    echo "Redis:         ${IWM_REDIS_HOST}:${IWM_REDIS_PORT}"
    echo "Elasticsearch: ${IWM_ELASTICSEARCH_URIS}"
    ;;
  kernel-local)
    echo "Starting external services for local kernel."
    "${compose_infra[@]}" up -d --remove-orphans
    echo "Building reactor dependencies for bootstrap."
    mvn -q -f "${KERNEL_ROOT_POM}" -pl RT-comops-bootstrap -am -DskipTests install
    echo "Starting kernel in foreground."
    echo "Use Ctrl+C to stop the kernel. Docker infrastructure remains up."
    exec mvn -q -f "${BOOTSTRAP_POM}" spring-boot:run
    ;;
esac
