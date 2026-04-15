# Organization Service Subscriptions

## Objet

Chaque organisation porte maintenant une liste explicite de services plateforme actives.

Cette liste sert a trois choses:
- exposer au backend consommateur quels modules l'organisation peut utiliser
- enrichir `POST /api/auth/login` et `GET /api/users/me`
- bloquer les endpoints metier quand l'organisation n'est pas abonnée au service requis
- porter un quota de trafic propre a l'organisation pour chaque service abonable

Elle n est pas la premiere couche de controle.
Le kernel applique d abord les restrictions de `ClientApplication`, puis les quotas backend `tenant + client + service`, puis les abonnements et quotas de l organisation, puis les permissions utilisateur.

## Catalogue

Services obligatoires:
- `ORGANIZATION`
- `SETTINGS`

Services gerables par abonnement:
- `COMMERCIAL`
- `PRODUCT`
- `INVENTORY`
- `SALES`
- `ACCOUNTING`
- `TREASURY`
- `RESOURCE`

## Stockage

Le stockage canonique se fait dans:
- `organization.organization_service_subscription`

Contrainte:
- unicite `(tenant_id, organization_id, service_code)`

Chaque ligne d'abonnement porte aussi maintenant:
- `requestQuotaLimit`
- `requestQuotaWindowSeconds`

## API organization-core

- `GET /api/organizations/services/catalog`
- `GET /api/organizations/{organizationId}/services`
- `POST /api/organizations/{organizationId}/services`
- `PATCH /api/organizations/{organizationId}/services/{serviceCode}/quota`
- `DELETE /api/organizations/{organizationId}/services/{serviceCode}`

Le retour expose:
- `subscribedServices`
- `effectiveServices`
- `serviceQuotas`

`effectiveServices` = services obligatoires + services souscrits.

Les services obligatoires restent toujours visibles dans `effectiveServices`, mais ils ne passent pas par le filtre d abonnement organisationnel runtime.

## Exposition auth-core

`POST /api/auth/login` et `GET /api/users/me` retournent maintenant:

```json
{
  "data": {
    "organizations": [
      {
        "organizationId": "uuid",
        "organizationCode": "ORG-001",
        "displayName": "Display Name",
        "legalName": "Legal Name",
        "services": ["COMMERCIAL", "PRODUCT", "INVENTORY"]
      }
    ]
  }
}
```

Cela permet a un backend consommateur:
- de savoir quelles organisations l'utilisateur peut operer
- de savoir quels modules il doit afficher ou masquer pour chaque organisation

## Enforcement runtime

Le kernel applique maintenant deux filtres de service distincts:

1. filtre `ClientApplication -> service`
2. filtre `Organization -> service`

Le filtre organisationnel ne s applique qu aux prefixes metier abonables:

- `/api/clients`
- `/api/customers`
- `/api/suppliers`
- `/api/prospects`
- `/api/sales-agents`
- `/api/third-parties`
- `/api/products`
- `/api/inventory`
- `/api/inventories`
- `/api/sales`
- `/api/accounting`
- `/api/treasury`
- `/api/banking`
- `/api/resources`

Regles:
- si la `ClientApplication` n est pas autorisee sur le service requis, le kernel retourne:
  - `403`
  - `errorCode = CLIENT_APPLICATION_SERVICE_NOT_ALLOWED`
- si le quota backend `tenant + client + service` est depasse, le kernel retourne:
  - `429`
  - `errorCode = TENANT_REQUEST_QUOTA_EXCEEDED`
- `X-Organization-Id` devient obligatoire sur ces endpoints
- si l'organisation n'a pas le service requis, le kernel retourne:
  - `403`
  - `errorCode = ORGANIZATION_SERVICE_NOT_SUBSCRIBED`
- si le quota de l'organisation sur ce service est depasse, le kernel retourne:
  - `429`
  - `errorCode = ORGANIZATION_SERVICE_QUOTA_EXCEEDED`

Les routes suivantes sont bornees uniquement par la `ClientApplication`:
- `ORGANIZATION`
- `SETTINGS`

## Quotas organisationnels

Chaque abonnement explicite porte son propre quota:
- `requestQuotaLimit`
- `requestQuotaWindowSeconds`

Le filtre runtime calcule une cle Redis distincte:

```text
iwm:quotas:organization-service-requests:<tenantId>:<organizationId>:<serviceCode>:<bucket>
```

Headers exposes:
- `X-IWM-Organization-Quota-Limit`
- `X-IWM-Organization-Quota-Remaining`
- `X-IWM-Organization-Quota-Window-Seconds`
- `X-IWM-Organization-Quota-Scope=organization-service`
- `X-IWM-Organization-Quota-Organization-Id`
- `X-IWM-Organization-Quota-Service`

Si Redis est indisponible:
- `fail-open = true` -> la requete passe quand meme
- `fail-open = false` -> `503 ORGANIZATION_SERVICE_QUOTA_UNAVAILABLE`

## Provisioning par defaut

A la creation d'une organisation:
- les services obligatoires restent implicites
- les services abonnables sont provisionnes par defaut pour conserver la compatibilite des flux existants
- chaque abonnement provisionne prend les valeurs par defaut:
  - `IWM_ORGANIZATION_SERVICE_DEFAULT_REQUEST_QUOTA_LIMIT`
  - `IWM_ORGANIZATION_SERVICE_DEFAULT_REQUEST_QUOTA_WINDOW`

La restriction se fait ensuite par desabonnement explicite si la plateforme veut fermer certains modules.

## Integration backend -> kernel

Pour un endpoint metier scope organisation, le backend consommateur doit envoyer:
- `X-Client-Id`
- `X-Api-Key`
- `X-Tenant-Id`
- `Authorization: Bearer <jwt>`
- `X-Organization-Id`

Le body peut aussi contenir `organizationId`, mais ce n'est pas ce champ qui porte le scope de securite.
Le scope de securite est le header `X-Organization-Id`.
