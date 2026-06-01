# Resilience Drills

## Objectif

Versionner des exercices simples pour valider le comportement du runtime sous incident.

## Scripts

- [run-outbox-replay-drill.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/run-outbox-replay-drill.sh)
- [run-postgres-failure-drill.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/run-postgres-failure-drill.sh)
- [run-kafka-failure-drill.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/run-kafka-failure-drill.sh)
- [run-restart-under-load.sh](/home/blhack/Projets/Kernel-core/iwm-backend/scripts/run-restart-under-load.sh)

## Drills

### Outbox replay massif

But:
- injecter un backlog `PENDING`
- redemarrer l'application
- verifier que le backlog se draine correctement

Commande:

```bash
IWM_OUTBOX_REPLAY_EVENT_COUNT=5000 ./scripts/run-outbox-replay-drill.sh
```

### Panne PostgreSQL

But:
- lancer une charge simple
- pauser PostgreSQL
- verifier le comportement applicatif et la reprise

Commande:

```bash
IWM_POSTGRES_FAILURE_PAUSE_SECONDS=20 ./scripts/run-postgres-failure-drill.sh
```

### Panne Kafka

But:
- lancer une charge
- pauser Kafka
- verifier backlog outbox, dead-letter et reprise

Commande:

```bash
IWM_KAFKA_FAILURE_PAUSE_SECONDS=20 ./scripts/run-kafka-failure-drill.sh
```

### Redemarrage sous charge

But:
- lancer une charge
- redemarrer `app`
- verifier la reprise via Nginx et les health checks

Commande:

```bash
./scripts/run-restart-under-load.sh
```

## Lecture attendue

- pas de perte de donnees primaires PostgreSQL
- backlog outbox attendu et visible pendant la panne Kafka
- health `operations` degrade puis revient a `UP`
- reprise explicable via les dashboards et les metriques

## Resultats observes

- `PostgreSQL failure`
  - `checks`: `100%`
  - `http_req_failed`: `0.00%`
  - `p95`: `134.72ms`
  - `max`: `19.91s`
  - conclusion:
    - la plateforme garde la disponibilite HTTP
    - la coupure DB provoque une forte latence transitoire, mais pas de perte

- `Kafka failure`
  - `checks`: `100%`
  - `http_req_failed`: `0.00%`
  - `p95`: `148.10ms`
  - conclusion:
    - l'API reste disponible
    - le chemin asynchrone absorbe la coupure comme prevu

- `Outbox replay massif`
  - resultat: `pending=0`, `published=12`
  - conclusion:
    - le replay au redemarrage draine correctement le backlog injecte

- `Restart under load`
  - `checks`: `94.04%`
  - `http_req_failed`: `5.95%`
  - `p95`: `154.32ms`
  - conclusion:
    - avec `2` replicas et un restart non progressif, il existe une fenetre d'erreurs transitoires
    - pour un objectif de disponibilite plus strict, il faut un restart progressif et/ou plus de replicas
