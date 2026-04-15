# Checklist Pre-Prod -> Prod

## 1. Configuration
- `IWM_BOOTSTRAP_CLIENT_ENABLED=true`
- `IWM_BOOTSTRAP_CLIENT_ID` defini
- `IWM_BOOTSTRAP_CLIENT_SECRET` defini via secret manager
- `IWM_OUTBOX_ALLOW_MANUAL_RELAY_ONLY=false`
- `IWM_KAFKA_BOOTSTRAP_SERVERS` pointe vers le cluster cible
- `IWM_ELASTICSEARCH_SEARCH_ENABLED` coherent avec la presence d'Elasticsearch
- `IWM_REDIS_PERMISSION_CACHE_ENABLED` coherent avec la presence de Redis

## 2. Securite
- endpoints management restreints par reseau, ingress ou API gateway
- `IWM_MANAGEMENT_API_KEY` defini via secret manager
- `MANAGEMENT_SERVER_PORT` separe du port applicatif si possible
- aucune valeur par defaut insecure en variables d'environnement
- comptes `system:admin` et `system:observe` provisionnes et testes
- rotation des secrets documentee

## 3. Base de donnees
- sauvegarde validee avant migration
- `Liquibase` applique sans drift
- jeux de contrats reussis sur l'environnement cible ou un clone
- index PostgreSQL verifies sur les tables `kernel.outbox_event` et `integration.domain_event_projection`

## 4. Kafka
- topic principal cree ou auto-creation explicitement acceptee
- dead-letter topic cree
- retention et replication verifiees
- consumer group `iwm-outbox-consumers` visible
- lag et dead letters monitorés

## 5. Redis
- TTL du cache permissions valide
- eviction policy compatible
- indisponibilite Redis sans perte metier confirmee

## 6. Elasticsearch
- index search crees
- mappings verifies
- recherche smoke validee sur organizations, products, third-parties et resources

## 7. Observabilite
- `/actuator/health/operations` visible depuis la supervision
- `/actuator/prometheus` scrape par Prometheus
- alertes outbox/projections configurees
- logs d'application centralises
- dashboards Grafana importes depuis `ops/grafana/dashboards`
- datasource Prometheus connectee
- scrape `iwm-management`, `redis-exporter` et `elasticsearch-exporter` valides
- smoke `observability-stack` rejouable a l identique depuis la CI

## 8. Validation finale
- `mvn -q test`
- contrats PostgreSQL passes
- smoke Elasticsearch passe
- integration Kafka passee
- observability stack smoke passe
- compose complet `infrastructure + application` demarrable sans correction manuelle
- scenario de bootstrap client application controle valide

## 9. Go/No-Go
- `GO` si migrations, contrats, smoke search, Kafka et health operations sont verts
- `NO-GO` si dead letters presentes, relay outbox degrade, projection stale, ou secrets insecurises
