# JWT Key Rotation

Le kernel signe maintenant les bearer tokens utilisateur en `RS256`.

## Configuration

- `iwm.security.jwt.private-key-path`
- `iwm.security.jwt.key-id`
- `iwm.security.jwt.issuer`
- `iwm.security.jwt.access-token-ttl`
- `iwm.security.jwt.auto-generate-key-pair`

Le endpoint public de verification est:

- `GET /.well-known/jwks.json`

## Production

Utiliser une cle privee RSA PKCS#8.

Exemple:

```bash
openssl genrsa 2048 | openssl pkcs8 -topk8 -nocrypt -out jwt-private-key.pem
```

Puis configurer:

```yaml
iwm:
  security:
    jwt:
      private-key-path: /run/secrets/jwt-private-key.pem
      key-id: iwm-key-1
      issuer: iwm-backend
      access-token-ttl: 15m
```

## Rotation

1. generer une nouvelle cle PKCS#8
2. deployer la nouvelle cle privee
3. incrementer `iwm.security.jwt.key-id`
4. redeployer
5. laisser expirer naturellement les anciens access tokens

## Developpement

Pour le dev local uniquement:

```yaml
iwm:
  security:
    jwt:
      auto-generate-key-pair: true
```

Tous les tokens deviennent invalides au redemarrage.
