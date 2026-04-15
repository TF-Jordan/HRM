# Capacity Planning

## Objectif

Donner un point de depart explicite pour dimensionner la cible Docker sans improvisation.

## Principes

- PostgreSQL reste la source de verite
- Redis et Elasticsearch restent des accelerateurs, pas des stores primaires
- Kafka absorbe les pics et decouple le relay/projections
- le dimensionnement se fait par paliers mesures, pas par intuition

## Point de depart recommande

## Mesures reelles obtenues

Campagne executee sur Docker avec :
- `app=2`
- `nginx` en frontal
- `PostgreSQL + Kafka + Redis + Elasticsearch + Prometheus + Grafana`

Mesures consolidees :
- `load.js`
  - `checks`: `100%`
  - `http_req_failed`: `0.00%`
  - `avg`: `32.12ms`
  - `p95`: `75.45ms`
  - `max`: `257.72ms`
  - `vus max`: `50`
- `endurance.js` sur `3m` avec `20 VUs`
  - `checks`: `100%`
  - `http_req_failed`: `0.00%`
  - `avg`: `44.66ms`
  - `p95`: `91.55ms`
  - `max`: `208.70ms`
- drill `PostgreSQL failure`
  - `checks`: `100%`
  - `http_req_failed`: `0.00%`
  - `avg`: `166.57ms`
  - `p95`: `134.72ms`
  - `max`: `19.91s`
  - lecture:
    - la coupure DB est absorbee sans perte HTTP
    - la latence maximale explose pendant la pause, ce qui est normal
- drill `Kafka failure`
  - `checks`: `100%`
  - `http_req_failed`: `0.00%`
  - `avg`: `72.26ms`
  - `p95`: `148.10ms`
  - `max`: `295.66ms`
  - lecture:
    - le trafic HTTP reste disponible
    - la coupure Kafka n'affecte pas la disponibilite immediate de l'API
- drill `restart under load`
  - `checks`: `94.04%`
  - `http_req_failed`: `5.95%`
  - `avg`: `82.30ms`
  - `p95`: `154.32ms`
  - `max`: `1.67s`
  - lecture:
    - avec `2` replicas, un restart simultane cree une indisponibilite transitoire
    - ce point doit etre traite operationnellement par restart progressif
- `spike.js`
  - protection active par throttling
  - `p95`: `34.72ms`
  - `http_req_failed`: `72.53%`
  - lecture:
    - le systeme ne s'effondre pas en latence
    - les limites `Nginx + quota tenant` deviennent le plafond effectif
- `saturation.js`
  - protection active par throttling
  - `p95`: `35.75ms`
  - `http_req_failed`: `84.22%`
  - lecture:
    - meme conclusion que `spike`
    - sous la configuration actuelle, les quotas protegent l'infra avant collapse
- drill `outbox replay massif`
  - resultat: `pending=0`, `published=12`
  - lecture:
    - le backlog injecte a bien ete resorbe apres redemarrage

### Application

- replicas `app`
  - local/validation: `2`
  - preprod: `2` a `3`
  - prod initial: `4`
- recommandation issue des mesures:
  - pour un objectif de continuite stricte, ne pas exploiter la prod a `2` replicas avec restart simultane
  - cible minimale conseillee:
    - `3` replicas pour preprod critique
    - `4` replicas pour prod initiale
  - imposer un restart progressif, pas un restart global du service
- `JAVA_OPTS`
  - garder `UseContainerSupport`
  - fixer `MaxRAMPercentage` selon la limite memoire reelle

### R2DBC / PostgreSQL

- `IWM_R2DBC_POOL_INITIAL_SIZE`
  - local: `10`
  - preprod: `15`
  - prod initial: `20`
- `IWM_R2DBC_POOL_MAX_SIZE`
  - local: `50`
  - preprod: `80`
  - prod initial: `120`
- regle de depart:
  - ne pas cumuler plus de connexions applicatives que PostgreSQL ne peut soutenir
  - ajuster `max_connections` PostgreSQL avant de monter les replicas applicatives
- lecture issue des mesures:
  - `2` replicas avec pool max `50` tiennent correctement la charge de validation
  - avant de depasser `4` replicas applicatives, requalifier `max_connections`, WAL, checkpointing et IOPS

### Kafka

- `IWM_OUTBOX_KAFKA_CONCURRENCY`
  - local: `2`
  - preprod: `3`
  - prod initial: `6`
- partitions du topic principal `iwm.events.business`
  - preprod: `6`
  - prod initial: `12`
- objectif:
  - avoir au moins autant de partitions utiles que de consumers paralleles
- lecture issue des mesures:
  - la panne Kafka n'a pas degrade la dispo HTTP
  - il faut donc dimensionner Kafka pour le rattrapage d'outbox, pas pour la simple latence HTTP

### Outbox

- `IWM_OUTBOX_RELAY_BATCH_SIZE`
  - local: `100`
  - preprod: `250`
  - prod initial: `500`
- `IWM_OUTBOX_RELAY_DELIVERY_CONCURRENCY`
  - local: `4`
  - preprod: `6`
  - prod initial: `8`
- `IWM_OUTBOX_REPLAY_ON_STARTUP_MAX_EVENTS`
  - preprod: `5000`
  - prod initial: `10000`
- lecture issue des mesures:
  - le replay injecte pendant le drill s'est correctement vide
  - la limite de replay au demarrage peut rester conservative tant que les temps de boot restent acceptables

### Redis

- usage actuel:
  - cache des permissions
  - quotas de requetes par tenant si actives
- sizing initial:
  - commencer simple, puis monitorer:
    - hit rate
    - memoire
    - evictions

### Elasticsearch

- usage actuel:
  - projections de recherche
- sizing initial:
  - single-node en local/preprod
  - eviter de le traiter comme source de verite
  - surveiller:
    - heap
    - temps de refresh
    - taille des index

## Rate limiting et quotas

- gateway Nginx
  - limite par IP
  - limite par tenant
- backend
  - quota plateforme par `tenant + client application + service`
  - quota metier par `organization + service`
- point de depart:
  - gateway tenant rate: `120r/s` local, `180r/s` preprod, `300r/s` prod initial
  - backend quota tenant: `1200/min` local, `1800/min` preprod, `2400/min` prod initial
  - backend quota organisation service par defaut: `10000/min`
- lecture issue des mesures:
  - avec les limites actuelles, `spike` et `saturation` plafonnent par throttling avant effondrement de latence
  - c'est un bon comportement de protection
  - ne pas augmenter ces limites sans besoin business explicite et sans requalification complete

## Recommandations immediates

1. Garder `app=2` seulement pour validation locale et tests de base.
2. Passer a `app=3` minimum pour les environnements ou l'on veut valider les redemarrages sans erreur transitoire visible.
3. Conserver les quotas actuels tant que le besoin metier n'exige pas de burst plus fort.
4. Ajuster les quotas `organization + service` selon les offres et le fair-use attendus.
5. Si le besoin impose plus de debit:
  - augmenter d'abord les limites `tenant/IP`
  - puis revoir les quotas `tenant + client + service`
  - puis revoir les quotas `organization + service`
  - puis relancer `spike` et `saturation`
  - ensuite seulement revoir pool DB, replicas et partitions Kafka.

## Comment ajuster

1. monter les replicas applicatives
2. observer R2DBC pool, latence DB et backlog outbox
3. augmenter partitions Kafka si les consumers plafonnent
4. ajuster Redis/Elasticsearch seulement si les metriques le justifient

## Ce que cette base ne prouve pas

- elle ne prouve pas une tenue a des millions d'utilisateurs simultanes
- elle donne un point de depart coherent pour qualifier la charge et decider objectivement des paliers suivants
