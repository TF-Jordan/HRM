# Scaling Strategy

## Objectif

Scaler sans casser la coherence du monolithe modulaire.

## Strategie retenue

### 1. Horizontal scaling applicatif

- scaler `iwm-app` horizontalement
- garder `Nginx` en frontal
- utiliser `Kafka` et l outbox pour absorber l asynchrone
- eviter de fragmenter en microservices tant que le monolithe modulaire tient

### 2. Base de donnees

- PostgreSQL reste le premier point de vigilance
- scaler d abord verticalement puis par optimisation
- ne monter les pools R2DBC qu apres validation de `max_connections`, IOPS, WAL et checkpoints

### 3. Cache et recherche

- `Redis` pour quotas et cache permissions
- `Elasticsearch` pour projections de recherche
- ne jamais deplacer la source de verite hors PostgreSQL

### 4. Capacite minimale recommandee

- local: `app=2`
- preprod critique: `app=3`
- prod initiale: `app=4`
- rolling restart obligatoire

### 5. Ce qui doit preceder toute montee de palier

1. reexecuter `load`, `endurance`, `spike`, `saturation`
2. verifier les drills `postgres`, `kafka`, `restart under load`, `outbox replay`
3. ajuster `capacity-planning.md`

## Critere d extraction microservice

Un module ne devient candidat a extraction que si:
- son profil de charge est structurellement different
- il doit scaler independamment
- son couplage evenementiel est deja stabilise
- l exploitation gagne reellement en simplicite
