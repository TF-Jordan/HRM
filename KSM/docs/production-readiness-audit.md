# Production Readiness Audit

Etat de reference au `2026-03-09`.

## Verdict

- `ready for continued professional development`: `yes`
- `ready for pre-production hardening`: `yes`
- `ready for unrestricted production rollout`: `not yet`

Le socle est coherent, modulaire, teste et exploitable. Les points restants ne sont plus des trous d'architecture majeurs, mais des exigences de durcissement et d'industrialisation de niveau production.

## Ce qui est pret

- architecture modulaire hexagonale stabilisee
- PostgreSQL reactif comme runtime nominal
- migrations versionnees avec `Liquibase`
- outbox persistante + relay Kafka + dead-letter
- consumers internes et projections persistantes
- Redis branche proprement pour le cache de permissions
- Elasticsearch branche proprement pour la recherche
- observabilite d'exploitation:
  - `Actuator`
  - `Prometheus`
  - `Grafana`
  - dashboards versionnes
- CI decoupee:
  - build
  - tests standard
  - guards d'architecture
  - contrats PostgreSQL
  - Kafka
  - smoke Elasticsearch
  - smoke observabilite
- parite fonctionnelle principale atteinte avec `KSM_V0.1`

## Gaps avant vrai niveau production

### 1. Gestion des secrets et des identites runtime

Statut: `required`

Le projet sait echouer proprement si les cles sont absentes ou insecure. En revanche, la gestion des secrets n'est pas encore industrialisee.

Reste a faire:
- brancher un secret manager ou un mecanisme de distribution securise des secrets
- separer les secrets par environnement
- definir la rotation des cles:
  - `IWM_BOOTSTRAP_CLIENT_SECRET`
  - `IWM_MANAGEMENT_API_KEY`
  - `IWM_JWT_PRIVATE_KEY_PATH`
  - `IWM_JWT_KEY_ID`

### 2. Securite applicative plus fine

Statut: `required`

Le controle d'acces est deja serieux, mais il reste a durcir les cas les plus sensibles en conditions reelles.

Reste a faire:
- reviser tous les cas d'usage `auth/roles/bootstrap` par politique d'exploitation
- definir la strategie de bootstrap admin hors exploitation normale
- completer les tests de securite de non-regression sur les endpoints d'administration

### 3. Resilience et exploitation Kafka

Statut: `required`

Kafka est present et fonctionne, mais l'exploitation production demande encore une couche d'encadrement.

Reste a faire:
- documenter la retention et les politiques de topic
- definir la surveillance et la purge de la dead-letter
- definir le plan de relecture/rejeu d'evenements
- ajouter des alertes d'exploitation sur:
  - backlog outbox
  - age du backlog
  - volume dead-letter
  - silence des projections

### 4. Performance et charge

Statut: `required`

Le projet est valide fonctionnellement et techniquement, mais pas encore qualifie sous charge.

Reste a faire:
- tests de charge HTTP
- tests de charge Kafka
- verification de tenue Redis/Elasticsearch sous concurrence
- tuning pool/timeout/backpressure sur les environnements cibles

### 5. Packaging et deploiement

Statut: `required`

Le projet est lancable localement et en CI, mais le mode de deploiement cible n'est pas encore verrouille.

Reste a faire:
- standardiser le packaging image et les variables d'environnement de prod
- definir le deploiement cible:
  - Docker Compose
  - Kubernetes
  - autre
- formaliser les probes, ressources et limites

## Risques residuels connus

- `Redis` et `Elasticsearch` sont optionnels au runtime, donc il faut bien verrouiller les feature flags par environnement
- l'API de login est stateless et signee, ce qui est propre, mais il faut decider si un mecanisme de revocation est necessaire a terme
- l'observabilite est bonne en local et CI, mais l'exposition des endpoints management doit rester strictement controlee par reseau et ingress

## Decision recommandee

- `develop`: continuer normalement sur ce socle
- `preprod`: autorise apres industrialisation des secrets et du deploiement
- `prod`: autoriser seulement apres validation des 5 chantiers ci-dessus
