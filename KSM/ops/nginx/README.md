# Nginx Gateway

Le gateway Nginx est le frontal public de `iwm-backend`.

Responsabilites:
- terminaison TLS
- routage HTTP -> backend applicatif
- rate limiting par IP
- rate limiting par tenant
- suppression des headers identitaires entrants (`X-User-Id`, `X-Actor-Id`)
- non exposition publique de `/actuator/**`
- exposition publique minimale de `GET /healthz`

Certificats:
- `certs/local`: certificat auto-signe pour usage local uniquement
- `certs/preprod`: cert/key reels preprod, hors Git
- `certs/prod`: cert/key reels prod, hors Git

Fichiers attendus:
- `certs/<env>/server.crt`
- `certs/<env>/server.key`
