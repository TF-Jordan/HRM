# Deployment Runtime

## Objectif

Fournir un chemin clair entre local, preprod et prod sans reintroduire de configuration inline insecure.

## Principes

- pas de secrets en clair dans Git
- secrets montes via fichiers et lus par l'entree container
- profils `preprod` et `prod` explicites
- validation des variables runtime avant de livrer
- les parametres de capacite restent explicites par variables d environnement
- le scaling applicatif se fait par replicas Docker, pas en exposant directement l application

## Fichiers utiles

- [Dockerfile](/home/blhack/Projets/Kernel-core/iwm-backend/Dockerfile)
- [docker-compose.application.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.application.yml)
- [docker-compose.infrastructure.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.infrastructure.yml)
- [docker-compose.preprod.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.preprod.yml)
- [docker-compose.prod.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.prod.yml)
- [scripts/start-full-stack.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/start-full-stack.sh)
- [scripts/start-dev-full-infra.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/start-dev-full-infra.sh)
- [scripts/docker-entrypoint.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/docker-entrypoint.sh)
- [scripts/validate-runtime-env.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/validate-runtime-env.sh)
- [ops/secrets/README.md](/home/blhack/Projets/Kernel-core/iwm-backend/ops/secrets/README.md)

## Validation preprod/prod

1. Copier `.env.preprod.example` ou `.env.prod.example`
2. Renseigner les variables non secretes
3. Creer les fichiers dans `ops/secrets/preprod/` ou `ops/secrets/prod/`
4. Exporter les variables de l'env file
5. Executer `scripts/validate-runtime-env.sh`
6. Demarrer la stack cible

## Commandes

Local, services externes seulement:

```bash
cd /home/blhack/Projets/Kernel-core/iwm-backend
cp .env.local.example .env.local
./scripts/start-full-stack.sh --infra-only --pull
```

Local, services externes attaches au kernel lance par Maven:

```bash
cd /home/blhack/Projets/Kernel-core/iwm-backend
cp .env.local.example .env.local
./scripts/start-full-stack.sh --kernel-local
```

Local, stack complete containerisee:

```bash
cd /home/blhack/Projets/Kernel-core/iwm-backend
cp .env.local.example .env.local
./scripts/start-full-stack.sh --app --pull
```

Swagger local:
- UI: `http://localhost:8080/swagger-ui.html`
- JSON OpenAPI: `http://localhost:8080/v3/api-docs`
- `/` redirige vers Swagger UI via le gateway Nginx.

Si la stack locale refuse de demarrer apres les upgrades majeurs, choisir une des deux options:
- conserver les donnees: migrer explicitement les donnees avant de changer de volume.
- environnement dev jetable: `./scripts/start-full-stack.sh --app --reset-volumes --pull`

Cas connus:
- PostgreSQL 16 -> 18: dump PostgreSQL 16, supprimer le volume, restaurer dans PostgreSQL 18.
- Elasticsearch 8.15 -> 9.3: upgrade intermediaire 8.19 requis avant 9.3 si les donnees doivent etre conservees.
- Kafka ancien cluster -> Kafka 8.2 KRaft: l'ancien `cluster.id` peut bloquer le demarrage; en dev, supprimer le volume est le chemin le plus simple.
- ZooKeeper: le conteneur `iwm-zookeeper` est un orphelin depuis le passage Kafka KRaft; le script demarre avec `--remove-orphans`.

Preprod:

```bash
cp .env.preprod.example .env.preprod
set -a && source .env.preprod && set +a
./scripts/validate-runtime-env.sh
./scripts/start-preprod-stack.sh
```

Prod-like:

```bash
cp .env.prod.example .env.prod
set -a && source .env.prod && set +a
./scripts/validate-runtime-env.sh
./scripts/start-prod-stack.sh
```

## Variables de capacite a piloter

- `IWM_R2DBC_POOL_*`
- `IWM_OUTBOX_KAFKA_CONCURRENCY`
- `IWM_OUTBOX_RELAY_BATCH_SIZE`
- `IWM_OUTBOX_RELAY_DELIVERY_CONCURRENCY`
- `IWM_OUTBOX_REPLAY_ON_STARTUP_MAX_EVENTS`
- `IWM_OUTBOX_REPLAY_ON_STARTUP_CONSUMER_CONCURRENCY`
- `IWM_TENANT_REQUEST_QUOTA_*`
- `IWM_GATEWAY_TENANT_RATE_LIMIT`
- `IWM_GATEWAY_TENANT_CONNECTION_LIMIT`

## Versions runtime locales

- Java runtime image: `eclipse-temurin:21.0.10_7-jre-noble`
- Maven build image: `maven:3.9.14-eclipse-temurin-21`
- PostgreSQL: `postgres:18.3-alpine`
- Redis: `redis:8.6.2-alpine`
- Kafka: `confluentinc/cp-kafka:8.2.0` en KRaft
- Spring Boot: `4.0.5`
- Elasticsearch: `docker.elastic.co/elasticsearch/elasticsearch:9.3.3`
- Nginx: `nginx:1.29.8-alpine`
- Prometheus: `prom/prometheus:v3.11.2`
- Grafana: `grafana/grafana:12.4.2`

PostgreSQL 18 local:
- le volume Docker doit etre monte sur `/var/lib/postgresql`
- ne pas monter le volume sur `/var/lib/postgresql/data`, layout refuse par l'image officielle PostgreSQL 18
