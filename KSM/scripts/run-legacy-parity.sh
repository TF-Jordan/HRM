#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODE="${1:-memory}"

run_memory() {
  mvn -q -f "${ROOT_DIR}/pom.xml" -pl RT-comops-bootstrap -am \
    -Dtest=ApiIntegrationTests#authBusinessActorEmployeesAndWarehousesSupportLegacyParityFlow+legacyParityGeneralOptionsInventorySessionsAndBankingRoutesAreAvailable \
    -Dsurefire.failIfNoSpecifiedTests=false \
    test
}

run_r2dbc() {
  mvn -q -f "${ROOT_DIR}/pom.xml" -pl RT-comops-bootstrap -am \
    -Diwm.tests.r2dbc.enabled=true \
    -Dtest=R2dbcApiIntegrationTests#legacyParityIdentityOrganizationFlowWorksAgainstRealPostgresql+legacyParitySettingsInventoryAndBankingRoutesWorkAgainstRealPostgresql \
    -Dsurefire.failIfNoSpecifiedTests=false \
    test
}

case "${MODE}" in
  memory)
    run_memory
    ;;
  r2dbc)
    run_r2dbc
    ;;
  all)
    run_memory
    run_r2dbc
    ;;
  *)
    echo "Usage: $0 [memory|r2dbc|all]" >&2
    exit 2
    ;;
esac
