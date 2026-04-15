# Infrastructure locale

## Image Kafka retenue
- reference locale: `confluentinc/cp-zookeeper:7.6.1` + `confluentinc/cp-kafka:7.6.1`
- raison:
  - deja disponible chez toi
  - alignement simple avec Spring Kafka
  - moins de variation d exploitation qu un mix `Confluent` / `Bitnami`

## Pourquoi pas `bitnamilegacy/kafka:3.7.0`
- ce n est pas necessaire ici
- le projet est deja cable autour d un bootstrap Kafka standard, pas autour d une distribution specifique Bitnami
- garder une seule distribution reduit les ecarts de config, de healthchecks et de troubleshooting

## Stack locale retenue
- PostgreSQL: `postgres:16-alpine`
- Redis: `redis:7-alpine`
- Zookeeper: `confluentinc/cp-zookeeper:7.6.1`
- Kafka: `confluentinc/cp-kafka:7.6.1`
- Elasticsearch: `docker.elastic.co/elasticsearch/elasticsearch:8.15.3`
- Redis exporter: `oliver006/redis_exporter:v1.67.0`
- Elasticsearch exporter: `quay.io/prometheuscommunity/elasticsearch-exporter:v1.8.0`
- Prometheus: `prom/prometheus:v2.54.1`
- Grafana: `grafana/grafana:11.2.0`

## Demarrage
```bash
docker compose -f docker-compose.infrastructure.yml up -d
```

## Demarrage complet avec l application
```bash
docker compose -f docker-compose.infrastructure.yml -f docker-compose.application.yml up -d --build
```

## Scripts utilitaires
```bash
./scripts/start-full-stack.sh
./scripts/stop-full-stack.sh
./scripts/run-k6-load.sh
./scripts/run-k6-endurance.sh
./scripts/run-k6-spike.sh
./scripts/run-k6-saturation.sh
./scripts/run-outbox-replay-drill.sh
./scripts/run-postgres-failure-drill.sh
./scripts/run-kafka-failure-drill.sh
./scripts/run-restart-under-load.sh
```

## Arret
```bash
docker compose -f docker-compose.infrastructure.yml down
```

## Arret complet avec l application
```bash
docker compose -f docker-compose.infrastructure.yml -f docker-compose.application.yml down
```

## Arret avec suppression des volumes
```bash
docker compose -f docker-compose.infrastructure.yml down -v
```

## Variables d environnement runtime
```bash
export SPRING_PROFILES_ACTIVE=r2dbc
export IWM_MANAGEMENT_API_KEY=dev-management-key
export IWM_BOOTSTRAP_CLIENT_ENABLED=true
export IWM_BOOTSTRAP_CLIENT_ID=dev-platform-backend
export IWM_BOOTSTRAP_CLIENT_NAME="Local Platform Backend"
export IWM_BOOTSTRAP_CLIENT_SECRET=dev-api-key
export IWM_R2DBC_URL=r2dbc:postgresql://localhost:5432/iwm
export IWM_R2DBC_USERNAME=iwm
export IWM_R2DBC_PASSWORD=iwm
export IWM_LIQUIBASE_URL=jdbc:postgresql://localhost:5432/iwm
export IWM_LIQUIBASE_USERNAME=iwm
export IWM_LIQUIBASE_PASSWORD=iwm
export IWM_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export IWM_REDIS_PERMISSION_CACHE_ENABLED=true
export IWM_REDIS_HOST=localhost
export IWM_REDIS_PORT=6379
export IWM_ELASTICSEARCH_SEARCH_ENABLED=true
export IWM_ELASTICSEARCH_URIS=http://localhost:9200
export MANAGEMENT_SERVER_PORT=8080
```

Une base d exemple est disponible dans [.env.local.example](/home/blhack/Projets/Kernel-core/iwm-backend/.env.local.example).

## Bootstrap admin dev
- tenant local seedé: `11111111-1111-1111-1111-111111111111`
- username: `platform-admin`
- email: `platform-admin@example.com`
- password: `PlatformAdmin!123`

Login:
```bash
curl -i \
  -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Client-Id: dev-platform-backend" \
  -H "X-Api-Key: dev-api-key" \
  -H "X-Tenant-Id: 11111111-1111-1111-1111-111111111111" \
  -d '{
    "principal": "platform-admin",
    "password": "PlatformAdmin!123"
  }'
```

La reponse retourne `data.accessToken` et `data.tenantId`.
Ce compte sert a debloquer le bootstrap IAM et la creation des
`ClientApplication` dediees en environnement local complet.

## Endpoints exposes par l application
- recherche produit: `GET /api/products/search`
- recherche tiers: `GET /api/third-parties/search`
- recherche ressource: `GET /api/resources/search`
- prometheus scrape: `GET /actuator/prometheus`
- health operations: `GET /actuator/health/operations`

## Monitoring local
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`
- public API gateway: `http://localhost:8080`
- public TLS gateway: `https://localhost:8443`
- management local only: `http://localhost:8081`
- management: `http://localhost:8081`
- credentials Grafana locales:
  - user: `admin`
  - password: `admin`
- dashboards provisionnes:
  - `IWM / IWM Outbox Runtime`
  - `IWM / IWM Kafka Integration`
  - `IWM / IWM Search and Cache`

## Remarques d exploitation
- PostgreSQL reste la source de verite
- Liquibase reste la seule voie de migration
- Redis est un cache de permissions, pas une source de verite
- Elasticsearch sert de projection de recherche, pas de persistence primaire
- Kafka transporte les evenements externes issus de l outbox
- Prometheus scrape l application via `host.docker.internal:8080/actuator/prometheus`
- la cle locale attendue par Prometheus est `dev-management-key`; l'application doit utiliser la meme valeur sur `IWM_MANAGEMENT_API_KEY`
- en mode `docker-compose.application.yml`, le gateway Nginx expose uniquement l API publique
- le gateway applique maintenant:
  - un rate limiting par IP
  - un rate limiting par tenant
- le backend peut appliquer en plus:
  - un quota plateforme par `tenant + client + service` via Redis
  - un quota metier par `organization + service` via Redis
- en mode `docker-compose.application.yml`, Prometheus scrape directement `iwm-app:8081`
- le script de verification locale est [scripts/smoke-observability.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/smoke-observability.sh)
