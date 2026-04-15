# Performance And Load

## Objectif

Fournir une base de charge reproductible et versionnee avant une vraie campagne de performance.

## Outillage

- `k6` pour les scenarios HTTP
- compose dedie pour lancer un conteneur de charge sur le reseau local de la stack

## Fichiers

- [ops/k6/smoke.js](/home/blhack/Projets/Kernel-core/iwm-backend/ops/k6/smoke.js)
- [ops/k6/load.js](/home/blhack/Projets/Kernel-core/iwm-backend/ops/k6/load.js)
- [ops/k6/README.md](/home/blhack/Projets/Kernel-core/iwm-backend/ops/k6/README.md)
- [docker-compose.loadtest.yml](/home/blhack/Projets/Kernel-core/iwm-backend/docker-compose.loadtest.yml)
- [scripts/run-k6-smoke.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/run-k6-smoke.sh)
- [scripts/run-k6-load.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/run-k6-load.sh)

## Scenarios

- `smoke.js`
  - verifie le health management
  - verifie `health/operations`
  - envoie une creation d'acteur

- `load.js`
  - exerce `prometheus`
  - exerce la creation d'acteur
  - pose des seuils simples sur erreurs et latence

- `endurance.js`
  - charge stable sur une duree longue
  - verifie la tenue du runtime dans le temps

- `spike.js`
  - envoie une brusque montee de trafic
  - verifie que le gateway et les quotas protegent correctement

- `saturation.js`
  - pousse progressivement vers les limites
  - sert a identifier les paliers de rupture

## Lancer un smoke local

```bash
./scripts/run-k6-smoke.sh
```

## Lancer une montee en charge Docker

```bash
IWM_APP_REPLICAS=2 \
IWM_K6_STAGE1_DURATION=15s \
IWM_K6_STAGE1_TARGET=15 \
IWM_K6_STAGE2_DURATION=30s \
IWM_K6_STAGE2_TARGET=30 \
IWM_K6_STAGE3_DURATION=15s \
IWM_K6_STAGE3_TARGET=0 \
./scripts/run-k6-load.sh
```

## Resultat de reference actuel

Qualification reelle executee sur Docker avec :
- `app=2`
- `gateway nginx` en frontal
- `PostgreSQL + Kafka + Redis + Elasticsearch + Prometheus + Grafana`
- scenario `load.js`

Mesures obtenues :
- `checks`: `100%`
- `http_req_failed`: `0.00%`
- `http_req_duration avg`: `32.12ms`
- `http_req_duration p95`: `75.45ms`
- `http_req_duration max`: `257.72ms`
- `http_reqs`: `3380`
- `iterations`: `3050`
- `vus max`: `50`

Campagne complementaire executee :
- `endurance.js` sur `3m` avec `20 VUs`
  - `checks`: `100%`
  - `http_req_failed`: `0.00%`
  - `avg`: `44.66ms`
  - `p95`: `91.55ms`
  - `max`: `208.70ms`
- `spike.js`
  - `p95`: `34.72ms`
  - `http_req_failed`: `72.53%`
  - interpretation:
    - le backend ne s'effondre pas en latence
    - les protections `Nginx + quota tenant` rejettent massivement la surpression
- `saturation.js`
  - `p95`: `35.75ms`
  - `http_req_failed`: `84.22%`
  - interpretation:
    - sous la configuration actuelle, le plafond est fixe par throttling
    - il faut ajuster les limites avant de conclure sur la capacite brute

Lecture correcte de ce resultat :
- c'est une premiere qualification de scaling Docker validee
- ce n'est pas encore une preuve de tenue pour des millions d'utilisateurs ou de plateformes simultanes
- il faut encore une campagne de saturation, d'endurance et de dimensionnement
- la campagne de saturation/endurance existe maintenant
- la prochaine lecture utile n'est plus "est-ce que ca tient", mais "ou veut-on placer le plafond de protection"

Depuis l introduction des `ClientApplications` bornees par service, la lecture des campagnes doit aussi tenir compte de:
- `X-Client-Id`
- service derive de la route
- quota backend par `(tenantId, clientId, serviceCode)`
- quota metier par `(tenantId, organizationId, serviceCode)`

Les prochaines campagnes doivent donc varier:
- plusieurs `clientId`
- plusieurs families de routes (`COMMERCIAL`, `SALES`, `TREASURY`, etc.)
- plusieurs organisations dans un meme tenant
- plusieurs profils de quotas organisationnels par service

## Limites actuelles

- ce n'est pas encore une campagne de performance complete
- il manque encore:
  - jeux de donnees volumineux
  - scenarios business riches
  - mesures de saturation PostgreSQL/Kafka/Redis/Elasticsearch
  - endurance multi-heures
  - drills de panne couples a la charge
