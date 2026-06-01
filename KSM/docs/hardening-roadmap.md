# Hardening Roadmap

## Objectif

Fermer les risques techniques encore ouverts sans remettre en cause le socle modulaire hexagonal.

## Priorites

### 1. Securite applicative

- supprimer tout bootstrap permissif restant en environnement non controle
- forcer bearer token signe pour les operations utilisateur sensibles
- garder `X-Management-Api-Key` et les endpoints management hors exposition publique
- durcir encore les audits admin et les actions de gouvernance

### 2. Frontieres hexagonales

- garder les modules sur `domain / application / adapter`
- interdire les dependances `adapter -> domain` inverses par garde-fous de tests
- interdire Spring dans le domaine
- interdire les imports `adapter` depuis `application`

### 3. Runtime et resilience

- rolling restart obligatoire a partir de `3` replicas
- quotas par tenant gardes actifs
- verification systematique de l outbox, de Kafka, de Redis et des projections
- drills de panne executes regulierement

### 4. Exploitation

- runbooks tenus a jour
- seuils Prometheus relies aux mesures reelles
- revue periodique des configurations `preprod` et `prod`

## Definition of done

- architecture guards verts en CI
- parity legacy verte
- contrats PostgreSQL verts
- smoke observabilite vert
- drills critiques documentes avec dernier resultat connu
