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
- [docker-compose.preprod.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.preprod.yml)
- [docker-compose.prod.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.prod.yml)
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
