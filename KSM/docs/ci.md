# CI

## Objectif
- separer les checks rapides des validations d infrastructure
- isoler clairement les echecs `code`, `PostgreSQL/Liquibase` et `Kafka/outbox`
- garder une pipeline deterministe et exploitable

## Workflow
- fichier: `.github/workflows/ci.yml`
- jobs:
  - `build`
  - `unit-memory`
  - `legacy-parity`
  - `architecture-guards`
  - `contracts-postgres`
  - `kafka-integration`
  - `search-smoke`
  - `observability-stack-smoke`

## Jobs

### `build`
- compile l ensemble du multi-module
- commande:
```bash
mvn -q -DskipTests compile
```

### `unit-memory`
- execute la suite standard sans dependance externe
- profil dominant: `test-memory`
- commande:
```bash
mvn -q test
```

### `contracts-postgres`
- demarre `postgres:16-alpine`
- applique les migrations via `Liquibase`
- execute les suites de contrat module par module
- commande:
```bash
mvn -q -pl RT-comops-bootstrap -am \
  -Diwm.tests.r2dbc.enabled=true \
  -Dtest=IdentityAccessContractTests,OrganizationCatalogContractTests,SalesInventoryContractTests,AccountingTreasuryContractTests,ResourceContractTests \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

### `kafka-integration`
- execute le test d integration outbox -> Kafka -> consumers
- pas de broker externe requis dans la CI GitHub Actions actuelle
- le test demarre un broker embarque
- commande:
```bash
mvn -q -Dtest=KafkaOutboxIntegrationTests \
  -Diwm.tests.kafka.enabled=true \
  -pl RT-comops-bootstrap -am \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

### `architecture-guards`
- execute les tests de garde-fous d architecture
- verifie que les adapters Elasticsearch restent sur `ReactiveElasticsearchOperations`
- verifie que Redis et Kafka ne sont pas contournes par `WebClient` ou `RestTemplate`

### `legacy-parity`
- execute la reference de non-regression `KSM_V0.1`
- couvre:
  - un flux legacy en `memory`
  - un flux legacy contre `PostgreSQL + Liquibase`
- script central:
```bash
./scripts/run-legacy-parity.sh all
```
- en CI, le job est decoupe en:
  - `./scripts/run-legacy-parity.sh memory`
  - `./scripts/run-legacy-parity.sh r2dbc`

### `search-smoke`
- demarre `PostgreSQL` et `Elasticsearch`
- execute les smoke tests de recherche reactive

### `observability-stack-smoke`
- construit l image de l application
- demarre `PostgreSQL + Redis + Kafka + Elasticsearch + Prometheus + Grafana + app`
- valide:
  - `/actuator/health`
  - `/actuator/health/operations`
  - `/actuator/prometheus`
  - l endpoint `up` de Prometheus
  - `/api/health` de Grafana
- commande centrale:
```bash
docker compose -f docker-compose.infrastructure.yml -f docker-compose.application.yml up -d --build
./scripts/smoke-observability.sh
```

## Regles d evolution
- toute nouvelle suite de contrat R2DBC doit etre ajoutee a `contracts-postgres`
- tout nouveau test Kafka bout en bout doit rester dans `kafka-integration`
- tout changement `Prometheus/Grafana/management` doit rester couvert par `observability-stack-smoke`
- les tests `test-memory` doivent rester rapides; ils ne doivent pas devenir un substitut a `contracts-postgres`
- les tests qui exigent des sockets locales ou un broker embarque ne doivent pas etre melanges aux jobs purement unitaires
