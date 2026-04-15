# Docker Industrialization

## Objectif

Rendre le projet exploitable proprement avec Docker comme cible finale.

## Socle existant

- `Dockerfile`
- `docker-compose.infrastructure.yml`
- `docker-compose.application.yml`
- overlays `preprod` et `prod`
- `docker-entrypoint.sh`
- validation runtime via `validate-runtime-env.sh`

## Processus recommande

### 1. Build image

```bash
./scripts/build-release-image.sh 0.1.0-rc1
```

### 2. Verifier le rendu compose

```bash
./scripts/verify-docker-release.sh preprod
./scripts/verify-docker-release.sh prod
```

### 3. Lancer la stack cible

- preprod:
  - `./scripts/start-preprod-stack.sh`
- prod:
  - `./scripts/start-prod-stack.sh`

## Regles de qualite Docker

- pas de secret en dur dans les compose
- support `*_FILE` pour les secrets montes
- l application n est pas exposee directement au host
- `Nginx` est le point d entree public
- management separe et protege

## Ce qui reste a industrialiser plus tard

- registry d images et tags immuables
- scan de vulnerabilites image
- signatures d images
- pipeline de promotion preprod -> prod
