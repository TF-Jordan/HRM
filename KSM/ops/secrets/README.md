# Secrets Layout

Ce dossier ne doit pas contenir de secrets reels dans Git.

## Arborescence attendue

- `ops/secrets/preprod/`
- `ops/secrets/prod/`

Fichiers attendus:

- `iwm_bootstrap_client_secret.txt`
- `iwm_management_api_key.txt`
- `jwt-private-key.pem`
- `iwm_r2dbc_password.txt`
- `iwm_liquibase_password.txt`
- `iwm_redis_password.txt`

Les overlays `docker-compose.preprod.yml` et `docker-compose.prod.yml` montent ces fichiers comme `Docker secrets`, puis l'entree container les transforme en variables runtime.

Ne pas committer les repertoires `preprod/` et `prod/` avec des valeurs reelles.
