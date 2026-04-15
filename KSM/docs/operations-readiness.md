# Operations Readiness

## Surface d'exploitation
- `GET /actuator/health`
- `GET /actuator/health/operations`
- `GET /actuator/metrics`
- `GET /actuator/metrics/iwm.outbox.pending`
- `GET /actuator/prometheus`
- `GET /api/observability/runtime`
- `GET /api/observability/outbox/summary?tenantId=...`
- `GET /api/observability/projections/summary?tenantId=...`

## Protection management
- `health` et `info` restent publics
- tout autre endpoint `/actuator/**` exige `X-Management-Api-Key`
- port management separable via:
  - `MANAGEMENT_SERVER_PORT`
- cle dediee:
  - `IWM_MANAGEMENT_API_KEY`

## Health indicators metier
- `outboxRuntime`
  - critique si `deadLetterCount >= iwm.observability.outbox.dead-letter-critical-threshold`
  - degrade si `pendingCount >= iwm.observability.outbox.pending-warning-threshold`
  - critique si l'age du plus vieux pending depasse `iwm.observability.outbox.max-pending-age`
- `projectionRuntime`
  - critique si des evenements sont publies mais aucune projection n'existe
  - critique si le silence des projections depasse `iwm.observability.projections.max-silence`

## Metriques custom
- `iwm.outbox.pending`
- `iwm.outbox.published`
- `iwm.outbox.dead_letter`
- `iwm.outbox.oldest_pending_age.seconds`
- `iwm.outbox.relay.events.published`
- `iwm.outbox.relay.events.failed`
- `iwm.outbox.relay.batch.duration`
- `iwm.kafka.outbox.delivery.success`
- `iwm.kafka.outbox.delivery.failure`
- `iwm.projections.total`
- `iwm.projections.latest_age.seconds`
- `iwm.quotas.tenant_requests.allowed`
- `iwm.quotas.tenant_requests.rejected`
- `iwm.quotas.tenant_requests.fail_open`
- `iwm.quotas.organization_service_requests.allowed`
- `iwm.quotas.organization_service_requests.rejected`
- `iwm.quotas.organization_service_requests.fail_open`

Les quotas backend sont maintenant comptes par:
- `tenantId`
- `clientId`
- `serviceCode`
- bucket temporel

Le service est derive de la route quand elle est mappee, sinon le fallback `iwm.quotas.tenant-requests.core-service-code` est utilise.

Les quotas metier d'organisation sont maintenant comptes par:
- `tenantId`
- `organizationId`
- `serviceCode`
- bucket temporel propre au service souscrit

## Alertes recommandees
- `iwm.outbox.dead_letter > 0` pendant 5 minutes
- `iwm.outbox.oldest_pending_age.seconds > 900`
- `iwm.outbox.relay.events.failed > 0`
- `iwm.projections.latest_age.seconds > 1800` alors que `iwm.outbox.published > 0`
- `kafka.consumer.records.lag.max` non nul durablement
- `increase(iwm_quotas_tenant_requests_rejected_total[5m]) > 0` de facon inattendue
- `increase(iwm_quotas_organization_service_requests_rejected_total[5m]) > 0` de facon inattendue
- health `operations != UP`
- hausse anormale de `429` concentree sur un `clientId` ou un `serviceCode`
- hausse anormale de `429` concentree sur une `organizationId`

## Configuration recommande
- exposer `metrics` et `prometheus` uniquement sur reseau interne ou via ingress restreint
- utiliser un port management separe en pre-prod/prod quand l'infra le permet
- ne pas laisser un `bootstrap client` partage entre plusieurs backends applicatifs
- ne pas partager `IWM_MANAGEMENT_API_KEY` avec un secret de `ClientApplication`
- ne jamais laisser `IWM_BOOTSTRAP_CLIENT_SECRET` vide ou partage hors canal secret
- ne pas laisser `IWM_BOOTSTRAP_CLIENT_ALLOWED_SERVICES` vide en pre-prod/prod si le backend consommateur n a pas vocation a appeler tout le kernel
- ne pas laisser `IWM_ORGANIZATION_SERVICE_DEFAULT_REQUEST_QUOTA_LIMIT` a une valeur uniforme sans revue metier si plusieurs offres commerciales existent
- garder `Liquibase` actif en pre-prod et prod pour les migrations versionnees
- en local, `Prometheus` et `Grafana` sont provisionnes via `docker-compose.infrastructure.yml`
- en local, la validation bout en bout peut etre faite via `docker-compose.infrastructure.yml` + `docker-compose.application.yml`
- en local, aligner `IWM_MANAGEMENT_API_KEY=dev-management-key` avec `ops/prometheus/prometheus.yml`
- en local full compose, le scrape applicatif utilise `ops/prometheus/prometheus.compose.yml`
- la protection du trafic repose sur deux couches:
  - rate limiting Nginx par IP et par tenant
  - quota backend par `tenant + client application + service` via Redis
  - quota metier par `organization + service` via Redis

## Commandes utiles
```bash
curl -H "X-Management-Api-Key: $IWM_MANAGEMENT_API_KEY" http://localhost:8081/actuator/metrics/iwm.outbox.pending
curl -H "X-Client-Id: $IWM_BOOTSTRAP_CLIENT_ID" -H "X-Api-Key: $IWM_BOOTSTRAP_CLIENT_SECRET" \
  -H "X-Tenant-Id: <tenant>" -H "Authorization: Bearer <access-token>" \
  http://localhost:8080/api/observability/runtime
curl -i -H "X-Client-Id: <client-id>" -H "X-Api-Key: <client-secret>" \
  -H "X-Tenant-Id: <tenant-id>" -H "Authorization: Bearer <access-token>" \
  -H "X-Organization-Id: <organization-id>" \
  http://localhost:8080/api/sales/orders/<order-id>
curl -H "X-Management-Api-Key: $IWM_MANAGEMENT_API_KEY" http://localhost:8081/actuator/health/operations
curl -H "X-Management-Api-Key: $IWM_MANAGEMENT_API_KEY" http://localhost:8081/actuator/prometheus
curl http://localhost:8080/.well-known/jwks.json
```

## Lecture rapide incident
1. verifier `/actuator/health/operations`
2. verifier `iwm.outbox.dead_letter`
3. verifier `iwm.outbox.oldest_pending_age.seconds`
4. verifier `iwm.projections.latest_age.seconds`
5. verifier le topic Kafka principal et le dead-letter
6. verifier l'etat Redis si le cache de permissions est active
7. verifier l'etat Elasticsearch si la recherche est active
8. verifier les headers `X-IWM-Quota-*` sur une requete representative
9. verifier les headers `X-IWM-Organization-Quota-*` sur une requete representative
10. verifier les dashboards Grafana du dossier `ops/grafana/dashboards`

## Dashboards
- `IWM Outbox Runtime`
- `IWM Kafka Integration`
- `IWM Search and Cache`
