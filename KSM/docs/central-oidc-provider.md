# Provider OIDC Central

Ce document decrit le provider OIDC central expose par le kernel pour les integrations inter-services et multi-plateformes.

## Endpoints supportes

- `GET /.well-known/openid-configuration`
- `GET /.well-known/oauth-authorization-server`
- `GET /.well-known/jwks.json`
- `POST /oauth2/token`
- `GET /oauth2/userinfo`
- `POST /oauth2/userinfo`
- `POST /oauth2/introspect`

## Ce que le provider fait reellement

- exposition du metadata OIDC/OAuth 2.0 du provider
- echange d un jeton SSO central contre un access token JWT RS256 scope service
- support de l authentification client `client_secret_basic` et `client_secret_post`
- `userinfo` pour jeton SSO partage et pour access token echange
- introspection RFC 7662 des access tokens et jetons SSO centraux
- publication de la cle publique via JWKS

## Flux principal

1. L utilisateur se connecte sur le kernel via `/api/auth/login`.
2. Le kernel retourne `sharedSession.token`.
3. Le client choisit un `contextId` dans `/oauth2/userinfo`.
4. Le client appelle `/oauth2/token` avec `grant_type=urn:ietf:params:oauth:grant-type:token-exchange`.
5. Le kernel retourne un JWT RS256 consommable par les autres services.

## Exemple de token exchange

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Authorization: Basic $(printf '%s' '<client-id>:<client-secret>' | base64 -w0)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "grant_type=urn:ietf:params:oauth:grant-type:token-exchange" \
  --data-urlencode "subject_token_type=urn:ietf:params:oauth:token-type:jwt" \
  --data-urlencode "subject_token=<shared-session-token>" \
  --data-urlencode "context_id=<context-id>" \
  --data-urlencode "organization_id=<organization-id>" \
  --data-urlencode "service_code=ORGANIZATION"
```

Reponse type:

```json
{
  "access_token": "<jwt-rs256>",
  "token_type": "Bearer",
  "expires_in": 900,
  "scope": "organizations:write",
  "issued_token_type": "urn:ietf:params:oauth:token-type:access_token"
}
```

## Exemple d introspection

```bash
curl -X POST http://localhost:8080/oauth2/introspect \
  -H "Authorization: Basic $(printf '%s' '<client-id>:<client-secret>' | base64 -w0)" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "token=<jwt-rs256>" \
  --data-urlencode "token_type_hint=access_token"
```

Reponse type:

```json
{
  "active": true,
  "client_id": "test-client",
  "token_type": "Bearer",
  "sub": "user-id",
  "scope": "organizations:write",
  "iss": "http://localhost:8080",
  "tid": "tenant-id",
  "oid": "organization-id",
  "svc": "ORGANIZATION"
}
```

## Limites assumees

- ce provider central ne publie pas encore de flux interactif `/oauth2/authorize`
- il est centre sur le SSO partage et le token exchange backend-to-backend
- il ne gere pas de `refresh_token`
