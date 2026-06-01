# Infrastructure locale

## Image Kafka retenue
- reference locale: `confluentinc/cp-kafka:8.2.0`
- mode: KRaft single-node pour local/dev
- raison:
  - ZooKeeper est retire de la stack locale
  - le kernel consomme Kafka uniquement via `bootstrap.servers`
  - la configuration reste compatible avec Spring Kafka et les outbox workers

## Pourquoi pas `bitnamilegacy/kafka:3.7.0`
- ce n est pas necessaire ici
- le projet est deja cable autour d un bootstrap Kafka standard, pas autour d une distribution specifique Bitnami
- garder une seule distribution reduit les ecarts de config, de healthchecks et de troubleshooting

## Stack locale retenue
- PostgreSQL: `postgres:18.3-alpine`
- Redis: `redis:8.6.2-alpine`
- Kafka: `confluentinc/cp-kafka:8.2.0`
- Elasticsearch: `docker.elastic.co/elasticsearch/elasticsearch:9.3.3`
- Redis exporter: `oliver006/redis_exporter:v1.82.0`
- Elasticsearch exporter: `quay.io/prometheuscommunity/elasticsearch-exporter:v1.10.0`
- Prometheus: `prom/prometheus:v3.11.2`
- Grafana: `grafana/grafana:12.4.2`
- Nginx gateway: `nginx:1.29.8-alpine`

Note: Elasticsearch est aligne sur la ligne 9.x avec Spring Boot 4.x et le client Java Elasticsearch 9.x.
Note: PostgreSQL 18 monte le volume local sur `/var/lib/postgresql`, pas sur `/var/lib/postgresql/data`, afin de respecter le layout Docker officiel compatible upgrades.

## Demarrage des services externes
```bash
./scripts/start-full-stack.sh --infra-only --pull
```

Ce mode lance uniquement:
- PostgreSQL sur `localhost:5432`
- Redis sur `localhost:6379`
- Kafka sur `localhost:9092`
- Elasticsearch sur `http://localhost:9200`
- Prometheus sur `http://localhost:9090`
- Grafana sur `http://localhost:3000`

## Attacher les services externes au kernel local
```bash
cp .env.local.example .env.local
./scripts/start-full-stack.sh --kernel-local
```

Equivalent manuel:
```bash
cp .env.local.example .env.local
set -a && source .env.local && set +a
docker compose -f docker-compose.infrastructure.yml up -d
mvn -q -f pom.xml -pl RT-comops-bootstrap -am -DskipTests install
mvn -q -f RT-comops-bootstrap/pom.xml spring-boot:run
```

Variables d'attachement kernel:
- `IWM_R2DBC_URL=r2dbc:postgresql://localhost:5432/iwm`
- `IWM_LIQUIBASE_URL=jdbc:postgresql://localhost:5432/iwm`
- `IWM_KAFKA_BOOTSTRAP_SERVERS=localhost:9092`
- `IWM_REDIS_HOST=localhost`
- `IWM_REDIS_PORT=6379`
- `IWM_ELASTICSEARCH_URIS=http://localhost:9200`
- `SPRING_PROFILES_ACTIVE=r2dbc`

## Demarrage complet containerise
```bash
./scripts/start-full-stack.sh --app --pull
```

## Scripts utilitaires
```bash
./scripts/start-full-stack.sh
./scripts/start-full-stack.sh --infra-only
./scripts/start-full-stack.sh --kernel-local
./scripts/start-dev-full-infra.sh --infra-only
./scripts/start-dev-full-infra.sh
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
docker compose -f docker-compose.infrastructure.yml -f docker-compose.application.yml -f docker-compose.management.yml down -v
```

Pour supprimer puis relancer en une commande:
```bash
./scripts/start-full-stack.sh --infra-only --reset-volumes
```

Pour supprimer puis relancer toute la stack containerisee:
```bash
./scripts/start-full-stack.sh --app --reset-volumes --pull
```

Attention: `--reset-volumes` supprime les donnees locales Docker. Il est utile en dev apres les upgrades majeurs suivants:
- PostgreSQL 16 -> 18: l'ancien volume PG16 ne demarre pas directement avec l'image PG18.
- Elasticsearch 8.15 -> 9.3: un noeud 8.15 ne peut pas etre upgrade directement en 9.3 sans passer par 8.19.
- Kafka ZooKeeper/ancien KRaft -> Kafka 8.2 KRaft: l'ancien `cluster.id` conserve dans le volume peut differer du `CLUSTER_ID` local.

Pour conserver les donnees locales, ne pas utiliser `--reset-volumes`:
- PostgreSQL: faire un dump PG16, supprimer le volume, restaurer dans PG18.
- Elasticsearch: reindexer/restaurer via un chemin compatible, au minimum 8.15 -> 8.19 -> 9.3.
- Kafka: recreer les topics et rejouer les evenements depuis la source de verite applicative si necessaire.

Le script de demarrage utilise `--remove-orphans` pour nettoyer les anciens conteneurs retires du compose, par exemple `iwm-zookeeper`.

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
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
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
