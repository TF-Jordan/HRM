# Socle Freeze

Etat gele au `2026-03-09`.

## Objet

Ce document fige le socle technique et architectural a partir duquel les enrichissements metier peuvent continuer sans remettre en cause les fondations.

## Socle gele

- style architectural:
  - monolithe modulaire
  - hexagonal par module
- runtime nominal:
  - `Spring Boot`
  - `WebFlux`
  - `PostgreSQL`
  - `R2DBC`
  - `Liquibase`
- integration:
  - `Kafka` pour le relay externe d'outbox
  - outbox persistante en base
  - consumers internes persistants
- support optionnel:
  - `Redis` pour le cache de permissions
  - `Elasticsearch` pour la recherche
- observabilite:
  - `Actuator`
  - `Prometheus`
  - `Grafana`

## Regles gelees

- pas de retour a une architecture monolithique en couches techniques
- pas de logique metier cross-module directe hors ports et contrats explicites
- pas de source de verite metier en dehors de PostgreSQL
- pas d'usage de Redis comme base fonctionnelle
- pas d'usage d'Elasticsearch comme source primaire
- pas de contournement `WebClient` direct pour Elasticsearch
- pas de fallback runtime `memory` hors tests explicites

## Modules geles

- `iwm-kernel-core`
- `iwm-common-core`
- `iwm-actor-core`
- `iwm-organization-core`
- `iwm-tp-core`
- `iwm-auth-core`
- `iwm-roles-core`
- `iwm-product-core`
- `iwm-inventory-core`
- `iwm-resource-core`
- `iwm-settings-core`
- `iwm-sales-core`
- `iwm-accounting-core`
- `iwm-treasury-core`
- `iwm-bootstrap`

## Capacites gelees

- parite fonctionnelle principale avec `KSM_V0.1`
- securite transverse par `ClientApplication` + access token JWT RS256
- management separe par cle dediee
- CI multi-etapes
- contrats PostgreSQL
- smoke Kafka
- smoke Elasticsearch
- smoke observabilite

## References de gel

- matrice de parite legacy:
  - [docs/ksm-v0-1-parity-matrix.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/ksm-v0-1-parity-matrix.md)
- gel de parite legacy:
  - [docs/legacy-parity-freeze.md](/home/blhack/Projets/Kernel-core/iwm-backend/docs/legacy-parity-freeze.md)

## Ce qui peut encore evoluer sans casser le socle

- enrichissement des cas d'usage metier
- policies metier plus fines
- dashboards et alertes
- packaging de deploiement
- optimisation de performance
- consumers et integrations aval supplementaires

## Ce qui ne doit plus etre reouvert sans decision explicite

- choix `Kafka` comme broker du projet
- choix `Liquibase` pour les migrations
- choix `WebFlux` comme pile applicative
- choix `PostgreSQL` comme source de verite
- choix `Redis` et `Elasticsearch` en support optionnel et non central
