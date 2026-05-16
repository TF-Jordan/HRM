# Grafana Dashboards

Dashboards versionnes pour l'exploitation locale et pre-prod.

## Fichiers
- `dashboards/iwm-outbox-runtime-dashboard.json`
- `dashboards/iwm-kafka-integration-dashboard.json`
- `dashboards/iwm-search-cache-dashboard.json`

## Source attendue
- datasource Prometheus nommee `Prometheus`

## Import
1. ouvrir Grafana
2. importer les JSON du dossier `dashboards/`
3. verifier que la datasource `Prometheus` cible bien `/actuator/prometheus`

## Remarque
- les panneaux Redis/Elasticsearch supposent que ces exporters existent dans l'environnement cible
- les panneaux outbox et Kafka reposent sur les metriques exposees directement par l'application
