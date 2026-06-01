# K6 Load Tests

## Scripts

- `smoke.js`: verification courte du runtime et du management
- `load.js`: montee en charge simple sur `actors` et `prometheus`
- `endurance.js`: charge stable longue duree
- `spike.js`: pic brutal de trafic
- `saturation.js`: montee graduelle vers saturation
- `../scripts/run-k6-smoke.sh`: orchestration smoke locale
- `../scripts/run-k6-load.sh`: orchestration charge Docker avec stack complete
- `../scripts/run-k6-endurance.sh`: orchestration endurance Docker
- `../scripts/run-k6-spike.sh`: orchestration spike Docker
- `../scripts/run-k6-saturation.sh`: orchestration saturation Docker

## Variables attendues

- `IWM_BASE_URL`
- `IWM_MANAGEMENT_URL`
- `IWM_BOOTSTRAP_CLIENT_ID`
- `IWM_BOOTSTRAP_CLIENT_SECRET`
- `IWM_MANAGEMENT_API_KEY`

## Exemple local

```bash
docker run --rm --network host \
  -e IWM_BASE_URL=http://localhost:8080 \
  -e IWM_MANAGEMENT_URL=http://localhost:8081 \
  -e IWM_BOOTSTRAP_CLIENT_ID=dev-platform-backend \
  -e IWM_BOOTSTRAP_CLIENT_SECRET=dev-api-key \
  -e IWM_MANAGEMENT_API_KEY=dev-management-key \
  -v "$PWD/ops/k6:/scripts:ro" \
  grafana/k6:0.49.0 run /scripts/smoke.js
```

## Charge Docker recommandee

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

## Reference actuelle

Derniere execution validee :
- `checks`: `100%`
- `http_req_failed`: `0.00%`
- `avg`: `32.12ms`
- `p95`: `75.45ms`
- `3380` requetes HTTP
- `3050` iterations
- `50` VUs max
