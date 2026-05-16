# Organization Service Subscriptions

## Objet

Chaque organisation porte maintenant une liste explicite de services plateforme actives.

Cette liste sert a trois choses:
- exposer au backend consommateur quels modules l'organisation peut utiliser
- enrichir `POST /api/auth/login` et `GET /api/users/me`
- bloquer les endpoints metier quand l'organisation n'est pas abonnée au service requis
- porter un quota de trafic propre a l'organisation pour chaque service abonable
- formaliser les dependances inter-services et les packs metier explicites

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
- `BILLING`
- `ACCOUNTING`
- `BANKING`
- `TREASURY`
- `CASHIER`
- `RESOURCE`
- `HRM`
- `BLOCKCHAIN`

Packs explicites:
- `COMMERCIAL_PACK` = `COMMERCIAL + PRODUCT + SALES + BILLING`
- `FINANCE_PACK` = `ACCOUNTING + BANKING + TREASURY + CASHIER`
- `OPERATIONS_PACK` = `PRODUCT + INVENTORY + RESOURCE + HRM + BLOCKCHAIN`

Dependances requises:
- `INVENTORY` requiert `PRODUCT`
- `SALES` requiert `COMMERCIAL` et `PRODUCT`
- `BILLING` requiert `COMMERCIAL`
- `TREASURY` requiert `ACCOUNTING` et `BANKING`
- `CASHIER` requiert `ACCOUNTING`

Dependances recommandees:
- `PRODUCT` recommande `COMMERCIAL`
- `INVENTORY` recommande `RESOURCE`
- `BILLING` recommande `ACCOUNTING`, `CASHIER`, `TREASURY`
- `ACCOUNTING` recommande `COMMERCIAL`
- `BANKING` recommande `ACCOUNTING`
- `TREASURY` recommande `BILLING`
- `CASHIER` recommande `BILLING`
- `RESOURCE` recommande `INVENTORY`
- `BLOCKCHAIN` recommande `ACCOUNTING`, `BILLING`

## Matrice de modularite

La matrice ci-dessous resume la realite d'exploitation de chaque service.

Niveaux d autonomie:
- `socle` = service structurel ou obligatoire
- `elevee` = module largement exploitable seul
- `moyenne` = module isolable, mais rarement deploye seul sans perte de valeur
- `faible` = module surtout pertinent dans un cluster metier plus large

| Service | Dependances requises | Dependances recommandees | Pack | Autonomie | Notes |
| --- | --- | --- | --- | --- | --- |
| `ORGANIZATION` | aucune | aucune | aucun | `socle` | socle organisationnel obligatoire; porte encore la structure de base |
| `SETTINGS` | aucune | aucune | aucun | `socle` | options globales et sequences documentaires |
| `COMMERCIAL` | aucune | aucune | `COMMERCIAL_PACK` | `elevee` | base tiers/commerciale souvent utile aux autres modules |
| `PRODUCT` | aucune | `COMMERCIAL` | `COMMERCIAL_PACK`, `OPERATIONS_PACK` | `elevee` | catalogue produit exploitable seul |
| `INVENTORY` | `PRODUCT` | `RESOURCE` | `OPERATIONS_PACK` | `moyenne` | stock et entrepots; gagne en valeur avec ressources et produit |
| `SALES` | `COMMERCIAL`, `PRODUCT` | aucune | `COMMERCIAL_PACK` | `moyenne` | commandes et execution commerciale; depend du referentiel tiers + catalogue |
| `BILLING` | `COMMERCIAL` | `ACCOUNTING`, `CASHIER`, `TREASURY` | `COMMERCIAL_PACK` | `moyenne` | facturation exploitable seule, mais plus complete avec le pack finance |
| `ACCOUNTING` | aucune | `COMMERCIAL` | `FINANCE_PACK` | `elevee` | noyau comptable central |
| `BANKING` | aucune | `ACCOUNTING` | `FINANCE_PACK` | `moyenne` | banques et releves; plus coherent avec comptabilite |
| `TREASURY` | `ACCOUNTING`, `BANKING` | `BILLING` | `FINANCE_PACK` | `moyenne` | tresorerie et settlements; module finance compose |
| `CASHIER` | `ACCOUNTING` | `BILLING` | `FINANCE_PACK` | `moyenne` | caisse et mouvements; s integre naturellement a compta + facturation |
| `RESOURCE` | aucune | `INVENTORY` | `OPERATIONS_PACK` | `elevee` | ressources, affectations, reservations |
| `HRM` | aucune | aucune | `OPERATIONS_PACK` | `elevee` | RH autonome cote abonnement; `/api/employees` lui est maintenant rattache |
| `BLOCKCHAIN` | aucune | `ACCOUNTING`, `BILLING` | `OPERATIONS_PACK` | `elevee` | module additif d ancrage, preuve et audit transverse |

## Lecture strategique

Packs les plus naturels:
- `COMMERCIAL_PACK` pour prospection, tiers, catalogue, ventes et facturation
- `FINANCE_PACK` pour comptabilite, banque, tresorerie et caisse
- `OPERATIONS_PACK` pour produit, stock, ressource, RH et extension blockchain

Clusters metier a connaitre:
- `BILLING + ACCOUNTING + TREASURY + CASHIER` forment le cluster finance le plus dense
- `SALES` est rarement pertinent sans `COMMERCIAL` et `PRODUCT`
- `INVENTORY` est fonctionnel sans `RESOURCE`, mais l exploitation est plus riche avec lui
- `BLOCKCHAIN` reste optionnel et transverse; il n impose aucun autre module

## Independance metier profonde

Le projet ne se limite plus au blocage par abonnement.
Les couplages metier critiques ont aussi ete refactores en contrats explicites:

- `billing -> cashier` passe maintenant par le contrat `CashierBillingBridgeUseCase`
- `inventory -> resource` passe par `ResourceOccupancyGateway` et `AssetPortfolioInsightGateway`
- `inventory -> file` passe par `DocumentHubInsightGateway`
- `inventory -> settings` passe par `OperationalPolicyGateway`

Principe de cablage:
- chaque core declare ses besoins comme ports locaux
- `bootstrap` fournit les adaptateurs entre cores
- un module ne depend plus du package `application.service` d un autre module pour son execution metier normale

Consequence:
- le retrait d un module ne casse plus un autre core par appel de service implicite
- l absence d un module se gere par abonnement, dependances formelles ou degradation fonctionnelle explicite
- les integrations transverses restent visibles et auditables dans `bootstrap`

Perimetre restant:
- les agregations d administration transverse restent centralisees dans `administration-core`
- elles passent maintenant par un gateway d orchestration admin explicite, cable dans `bootstrap`
- elles sont considerees comme orchestration de plateforme, pas comme dependance metier de base entre modules

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
- `GET /api/organizations/services/packs`
- `GET /api/organizations/commercial-subscriptions/catalog`
- `GET /api/organizations/{organizationId}/services`
- `POST /api/organizations/{organizationId}/commercial-subscriptions`
- `POST /api/organizations/{organizationId}/services`
- `PATCH /api/organizations/{organizationId}/services/{serviceCode}/quota`
- `DELETE /api/organizations/{organizationId}/services/{serviceCode}`

Le retour expose:
- `subscribedServices`
- `effectiveServices`
- `serviceQuotas`
- `dependencyIssues`

`effectiveServices` = services obligatoires + services souscrits.

Les services obligatoires restent toujours visibles dans `effectiveServices`, mais ils ne passent pas par le filtre d abonnement organisationnel runtime.

## Pricing et packaging

Le catalogue commercial expose maintenant trois niveaux:
- `Plan` = offre principale appliquee a une organisation
- `Pack` = groupement fonctionnel de services plateforme
- `Add-on` = option commerciale compatible avec certains plans

Plans disponibles:
- `STARTER` = `COMMERCIAL`
- `COMMERCE` = `COMMERCIAL_PACK`
- `FINANCE` = `FINANCE_PACK`
- `OPERATIONS` = `OPERATIONS_PACK`
- `ENTERPRISE` = `COMMERCIAL_PACK + FINANCE_PACK + OPERATIONS_PACK`

Add-ons disponibles:
- `BLOCKCHAIN_AUDIT_ADDON` = `BLOCKCHAIN`
- `HRM_PAYROLL_ADDON` = `HRM`
- `INVENTORY_OPERATIONS_ADDON` = `PRODUCT + INVENTORY + RESOURCE`
- `POINT_OF_SALE_ADDON` = `ACCOUNTING + CASHIER`
- `TREASURY_SETTLEMENTS_ADDON` = `ACCOUNTING + BANKING + TREASURY`

Regles de compatibilite:
- un add-on inconnu est refuse
- un add-on non compatible avec le plan choisi est refuse
- le service set final doit satisfaire toutes les dependances requises
- les dependances recommandees restent informatives et remontent dans `dependencyIssues`
- appliquer un plan remplace l abonnement commercial courant: les services hors plan/add-ons sont retires

Quotas commerciaux:
- chaque plan porte ses quotas par service
- chaque add-on peut surcharger ou ajouter ses quotas par service
- les quotas sont appliques sur les lignes `organization_service_subscription`
- les services sans quota commercial explicite utilisent les defaults `IWM_ORGANIZATION_SERVICE_DEFAULT_REQUEST_QUOTA_*`

Endpoints commerciaux:
- `GET /api/organizations/commercial-subscriptions/catalog`
- `POST /api/organizations/{organizationId}/commercial-subscriptions`

Exemple de requete:

```json
{
  "planCode": "COMMERCE",
  "addOnCodes": ["POINT_OF_SALE_ADDON", "BLOCKCHAIN_AUDIT_ADDON"]
}
```

Effet:
- souscrit les services du plan
- ajoute les services des add-ons compatibles
- met a jour les quotas de chaque service
- retire les anciens services qui ne sont plus dans le package commercial cible
- retourne les entitlements effectifs de l organisation

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
- `/api/warehouses`
- `/api/inventory`
- `/api/inventories`
- `/api/sales`
- `/api/bons-achat`
- `/api/bon-commande`
- `/api/bons-livraison`
- `/api/v1/facturation`
- `/api/facture-fournisseurs`
- `/api/factures-proforma`
- `/api/paiement`
- `/api/tableau-de-bord`
- `/api/accounting`
- `/api/accounting-service`
- `/api/comptable`
- `/api/v1/accounting`
- `/api/taxes`
- `/api/journals`
- `/api/banks`
- `/api/transaction-types`
- `/api/statement-lines`
- `/api/audit-logs`
- `/api/bank-accounts`
- `/api/bank-statements`
- `/api/checks`
- `/api/reconciliation`
- `/api/treasury`
- `/api/banking`
- `/api/admin/accounts`
- `/api/cashier/accounts`
- `/api/accounts/transfer`
- `/api/accounts/withdraw`
- `/api/accounts/transfer-p2p`
- `/api/cashier/fund-requests`
- `/api/cashier/bills`
- `/api/bills`
- `/api/cash-registers`
- `/api/cashiers`
- `/api/cashier/sessions`
- `/api/dashboard/stats`
- `/api/dashboard/stat`
- `/api/admin/documents`
- `/api/config/denominations`
- `/api/cashier/movements`
- `/api/movements`
- `/api/transactions`
- `/api/notifications`
- `/api/audit`
- `/api/admin/reconciliations`
- `/api/cashier/reconciliations`
- `/api/reconciliations`
- `/api/reports/transactions`
- `/api/reports/register`
- `/api/reports/session`
- `/api/reports/audit`
- `/api/sessions`
- `/api/notify-unauthorized`
- `/api/resources`
- `/api/employees`
- `/api/v1/hrm`
- `/api/v1/blockchain`

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
- les services abonnables ne sont plus provisionnes par defaut
- la plateforme doit souscrire explicitement les modules souhaites
- un mode de compatibilite reste possible via `IWM_ORGANIZATION_SERVICE_PROVISION_ON_CREATE=true`

Les quotas par defaut restent utilises au moment de chaque souscription explicite:
- `IWM_ORGANIZATION_SERVICE_DEFAULT_REQUEST_QUOTA_LIMIT`
- `IWM_ORGANIZATION_SERVICE_DEFAULT_REQUEST_QUOTA_WINDOW`

Les souscriptions incoherentes sont maintenant refusees:
- abonnement refuse si les dependances requises manquent
- desabonnement refuse si un autre module souscrit depend du service vise
- les incoherences residuelles eventuelles remontent dans `dependencyIssues`

## Integration backend -> kernel

Pour un endpoint metier scope organisation, le backend consommateur doit envoyer:
- `X-Client-Id`
- `X-Api-Key`
- `X-Tenant-Id`
- `Authorization: Bearer <jwt>`
- `X-Organization-Id`

Le body peut aussi contenir `organizationId`, mais ce n'est pas ce champ qui porte le scope de securite.
Le scope de securite est le header `X-Organization-Id`.
