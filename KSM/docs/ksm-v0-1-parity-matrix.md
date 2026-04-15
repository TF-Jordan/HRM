# KSM_V0.1 Parity Matrix

Etat de reference au `2026-03-09`.

Statuts :
- `covered` : flux legacy disponible dans `iwm-backend`
- `partial` : flux principal disponible, mais pas a parite exacte
- `missing` : pas encore disponible

## Identity

| Legacy capability | Legacy entrypoint | Current state | Notes |
| --- | --- | --- | --- |
| Register user | `/auth/register` | `covered` | `POST /api/auth/register` |
| Login user | `/auth/login` | `covered` | `POST /api/auth/login` retourne maintenant un jeton de session signe, avec `tenantId`, `userId` et `actorId` |
| Actor creation | `/actors` | `covered` | `POST /api/actors` |
| Business actor onboarding | `/actors/onboarding` | `covered` | `POST /api/actors/onboarding` |
| Current business actor profile | `/actors/me` | `covered` | `GET /api/actors/me` |
| Update business actor profile | `PUT /actors/me` | `covered` | `PUT /api/actors/me` |

## Organization

| Legacy capability | Legacy entrypoint | Current state | Notes |
| --- | --- | --- | --- |
| Create organization | `/organizations` | `covered` | `POST /api/organizations` |
| List/read organization | `/organizations` | `covered` | `GET /api/organizations`, `GET /api/organizations/{id}` |
| Agencies / branches | `/agencies` | `covered` | `POST/GET /api/organizations/{organizationId}/agencies` |
| Warehouses dedicated API | `/warehouses` | `covered` | `GET/POST/PATCH/DELETE /api/warehouses` |
| Opening hours | `/opening-hours` | `covered` | `POST /api/organizations/opening-hours` + list endpoint |
| Points of interest | `/points-of-interest` | `covered` | `POST /api/organizations/points-of-interest` + list endpoint |
| Employees list/invite/remove | `/employees` | `covered` | `GET /api/employees`, `POST /api/employees/invite`, `DELETE /api/employees/{id}` |
| Employee invite with custom permission ids | `/employees/invite` | `covered` | permissions ad hoc supportees via creation d’un role dedie puis affectation au collaborateur invite |
| Employee roles catalog | `/employees/roles` | `covered` | `GET /api/employees/roles` |

## Third parties / commercial actors

| Legacy capability | Legacy entrypoint | Current state | Notes |
| --- | --- | --- | --- |
| Client CRUD | `/clients` | `covered` | `GET/POST/PATCH/DELETE /api/clients` exposes sur `tp-core` |
| Supplier CRUD | `/suppliers` | `covered` | `GET/POST/PATCH/DELETE /api/suppliers` exposes sur `tp-core` |
| Prospect CRUD and lifecycle | `/prospects` | `covered` | `GET/POST/PATCH/DELETE /api/prospects`, activation, desactivation, conversion |
| Sales agent CRUD | `/sales-agents` | `covered` | `GET/POST/PATCH/DELETE /api/sales-agents` |
| Third-party bank accounts | `/{type}/{id}/bank-accounts` | `covered` | sous-ressource dediee avec ajout, suppression et compte principal |
| Third-party accounting account | varies | `covered` | lookup et mise a jour dedies |
| Prospect support | varies | `covered` | gere dans `tp-core` avec conversion `prospect -> customer` |
| Third-party statistics | varies | `covered` | statistiques dediees clients, fournisseurs, prospects, agents commerciaux |
| Third-party search | no strong equivalent | `covered` | plus riche avec Elasticsearch |

## Sales / accounting / treasury

| Legacy capability | Legacy entrypoint | Current state | Notes |
| --- | --- | --- | --- |
| Orders | `/orders` | `covered` | `POST/GET/PATCH/DELETE /api/sales/orders`, `POST /api/sales/orders/{orderId}/confirm`, `POST /api/sales/orders/{orderId}/cancel` |
| Invoices | `/invoices` | `covered` | `POST/GET/PATCH/DELETE /api/accounting/invoices`, `POST /api/accounting/invoices/{invoiceId}/validate`, `POST /api/accounting/invoices/from-orders/{orderId}` |
| Invoice posting | domain flow | `covered` | workflow `POSTED` present |
| Bank accounts | `/banking` | `covered` | `GET/POST /api/banking/accounts`, `GET /api/banking/accounts/{bankAccountId}`, `POST /api/banking/transactions`, `GET /api/banking/accounts/{bankAccountId}/transactions` |
| Bank statements | `/banking/statements` | `covered` | `POST /api/banking/statements/import`, `GET /api/banking/statements`, `GET /api/banking/statements/{statementId}` |
| Checks | `/checks` | `covered` | `POST/GET /api/banking/checks`, `POST /api/banking/checks/{checkPaymentId}/deposit` |
| Reconciliation | `/reconciliations` | `covered` | `POST /api/banking/reconciliation/auto/{accountId}`, `POST /api/banking/reconciliation/manual`, lecture/close aussi couvertes en API canonique `treasury` |

## Inventory / stock

| Legacy capability | Legacy entrypoint | Current state | Notes |
| --- | --- | --- | --- |
| Stock movements | stock endpoints | `covered` | `POST /api/inventory/movements`, `GET /api/inventory/movements`, `GET /api/inventory/movements/{movementId}`, `POST /api/inventory/movements/{movementId}/validate`, `GET /api/inventory/movements/balance` |
| Product transformations | transformation endpoints | `covered` | `GET/POST /api/inventory/transformations`, `POST /api/inventory/transformations/{transformationId}/validate` |
| Warehouse transfers | transfer endpoints | `covered` | `GET/POST /api/inventory/transfers`, `POST /api/inventory/transfers/{transferId}/complete` |
| Stock dispatch from sales confirmation | implicit legacy flow | `covered` | plus strict dans `iwm-backend` |

## Additional legacy surface verified after code audit

| Legacy capability | Legacy entrypoint | Current state | Notes |
| --- | --- | --- | --- |
| Organization current ownership view | `/organizations/my` | `covered` | `GET /api/organizations/my` |
| Update organization | `PATCH /organizations/{id}` | `covered` | `PATCH /api/organizations/{organizationId}` |
| Transfer organization ownership | `POST /organizations/{id}/transfer/{newOwnerId}` | `covered` | `POST /api/organizations/{organizationId}/transfer/{newOwnerId}` |
| General options | `/generalOptions` | `covered` | `GET/PUT /api/generalOptions` et alias `/api/general-options` |
| User self-service profile | `/users/me` | `covered` | `GET /api/users/me`, `PUT /api/users/me/plan`, `PUT /api/users/me/onboarding` |
| System audit read API | `/system-audit` | `covered` | `GET /api/system-audits/me`, `GET /api/system-audits/organization` |
| File upload / download | `/files` | `covered` | `POST /api/files`, `GET /api/files/{fileId}` |
| Opening hours regular/exceptions/status | `/agencies/{agencyId}/schedule` | `covered` | `GET /api/agencies/{agencyId}/schedule`, `POST /api/agencies/{agencyId}/schedule/exceptions`, `DELETE /api/agencies/{agencyId}/schedule/exceptions/{exceptionId}`, `GET /api/agencies/{agencyId}/schedule/status` |
| Point of interest link/unlink | `/pois/link` | `covered` | `POST /api/pois/link`, `DELETE /api/pois/link` |
| Product update/delete | `PATCH/DELETE /products/{id}` | `covered` | `PATCH /api/products/{productId}`, `DELETE /api/products/{productId}` |
| Inventory sessions | `/inventories` | `covered` | `GET/POST /api/inventories`, `POST /api/inventories/{inventoryId}/validate` |

## Extra capabilities beyond KSM_V0.1

- Kafka outbox relay
- Liquibase versioned migrations
- Redis permission cache
- Elasticsearch search projections
- Prometheus / Grafana observability
- architecture guards in CI
- modular hexagonal structure

## Residual differences vs KSM_V0.1

1. La parite fonctionnelle utile avec `KSM_V0.1` est maintenant couverte, y compris les alias legacy exposes pour la compatibilite API.
2. Le modele `third-parties` reste unifie en interne, meme si des routes dediees `clients` et `suppliers` sont exposees pour la parite API.
3. Les permissions employee ad hoc sont implementees proprement via creation d’un role dedie, pas par stockage brut d’identifiants de permissions sur la relation d’invitation.
4. Le mecanisme de session est volontairement signe et stateless, plus propre que le comportement legacy.
5. La validation de cette parite a ete rejouee sur tests standards et sur PostgreSQL reel avec Liquibase.
