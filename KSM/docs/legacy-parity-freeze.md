# Legacy Parity Freeze

Etat gele au `2026-03-09`.

## Objet

Ce document fige la parite `KSM_V0.1` comme reference de non-regression.  
La source de verite fonctionnelle est la matrice :

- [docs/ksm-v0-1-parity-matrix.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/ksm-v0-1-parity-matrix.md)

## Decision

- toute capacite marquee `covered` dans la matrice est consideree comme contractuelle
- aucun retour arriere ne doit etre introduit sur cette surface sans decision explicite
- les alias legacy exposes pour compatibilite API font partie de cette reference

## Surface gelee

- identity:
  - `register`
  - `login`
  - `business actor onboarding`
  - `actors/me`
- organization:
  - organizations
  - agencies
  - warehouses
  - employees
  - horaires
  - points d'interet
- commercial:
  - clients
  - suppliers
  - prospects
  - sales agents
  - comptes bancaires tiers
  - conversion prospect -> client
  - statistiques tiers
- sales/accounting/treasury:
  - orders
  - invoices
  - bank accounts
  - bank statements
  - checks
  - reconciliation
- inventory:
  - stock movements
  - transformations
  - warehouse transfers
  - inventory sessions
- settings:
  - general options
- user self-service:
  - `users/me`
  - `users/me/plan`
  - `users/me/onboarding`
- platform support:
  - audit systeme consultable
  - upload/download de fichiers

## Garde-fous de non-regression

- tests memoire:
  - `ApiIntegrationTests#authBusinessActorEmployeesAndWarehousesSupportLegacyParityFlow`
  - `ApiIntegrationTests#legacyParityGeneralOptionsInventorySessionsAndBankingRoutesAreAvailable`
- tests PostgreSQL reel:
  - `R2dbcApiIntegrationTests#legacyParityIdentityOrganizationFlowWorksAgainstRealPostgresql`
  - `R2dbcApiIntegrationTests#legacyParitySettingsInventoryAndBankingRoutesWorkAgainstRealPostgresql`
- script d'execution:
  - [scripts/run-legacy-parity.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/run-legacy-parity.sh)
- job CI dedie:
  - `legacy-parity`

## Regles d'evolution

- toute evolution au-dela de `KSM_V0.1` doit conserver cette surface
- toute suppression d'alias legacy doit passer par une decision explicite et une migration API
- les tests de parite doivent evoluer quand la matrice evolue

## Ce qui n'est pas fige

- la forme interne des agregats
- la structure des modules
- l'optimisation des performances
- les integrations supplementaires
- les endpoints nouveaux au-dela du legacy
