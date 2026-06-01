# Platform Governance Design

Etat de conception et d'implementation au `2026-03-09`.

## Objet

Definir ce que peuvent faire:
- l'admin general
- l'admin d'organisation
- l'admin d'agence

et formaliser la gouvernance des:
- `BusinessActor`
- `Organization`
- `Agency`

## Niveaux d'administration

### 1. Admin general

Portee:
- `TENANT`
- eventuellement `SYSTEM` pour les permissions protegees

Peut:
- lire et administrer le catalogue des permissions
- creer, editer, cloner et supprimer des roles
- assigner et revoquer des roles
- provisionner les templates de roles par defaut
- lire l'audit admin
- gerer les options plateforme admin
- approuver, rejeter, suspendre, bloquer, reactiver un `BusinessActor`
- approuver, rejeter, suspendre, fermer, reouvrir une `Organization`
- activer, suspendre, fermer une `Agency`

Permissions typiques:
- `administration:read`
- `administration:write`
- `administration:roles:read`
- `administration:roles:write`
- `administration:roles:clone`
- `administration:permissions:read`
- `administration:assignments:write`
- `administration:settings:read`
- `administration:settings:write`
- `administration:audit:read`
- `administration:govern:business-actors`
- `administration:govern:organizations`
- `administration:govern:agencies`

### 2. Admin d'organisation

Portee:
- `ORGANIZATION`

Peut:
- administrer les roles et affectations de son organisation
- lire l'audit admin dans son scope
- gerer certains parametres admin de son scope
- gouverner les agences de son organisation si l'option plateforme `allowOrganizationAdminsToGovernAgencies` est active

Ne peut pas:
- approuver ou bloquer un `BusinessActor`
- approuver ou rejeter une `Organization`
- agir hors de son `organizationId`

### 3. Admin d'agence

Portee:
- `AGENCY`

Peut:
- gerer des affectations et roles d'agence
- administrer l'operationnel local si le role le permet

Ne peut pas:
- gouverner un `BusinessActor`
- gouverner une `Organization`
- fermer ou suspendre arbitrairement d'autres agences

## Statuts de gouvernance

### BusinessActor

Statuts:
- `PENDING_REVIEW`
- `APPROVED`
- `REJECTED`
- `SUSPENDED`
- `BLOCKED`

Actions:
- `APPROVE`
- `REJECT`
- `SUSPEND`
- `BLOCK`
- `REACTIVATE`

### Organization

Statuts:
- `PENDING_APPROVAL`
- `APPROVED`
- `REJECTED`
- `SUSPENDED`
- `CLOSED`

Actions:
- `APPROVE`
- `REJECT`
- `SUSPEND`
- `CLOSE`
- `REOPEN`

### Agency

Statuts:
- `ACTIVE`
- `SUSPENDED`
- `CLOSED`

Actions:
- `ACTIVATE`
- `SUSPEND`
- `CLOSE`

## Options plateforme admin persistees

Agrégat:
- `AdministrativePlatformOptions`

Champs:
- `requireBusinessActorApproval`
- `requireOrganizationApproval`
- `allowOrganizationSelfServiceCreation`
- `allowAgencySelfServiceCreation`
- `allowRoleCloning`
- `allowAgencyScopedCustomRoles`
- `allowOrganizationAdminsToGovernAgencies`
- `allowBusinessActorSelfReactivation`

Defaults runtime:
- `requireBusinessActorApproval=true`
- `requireOrganizationApproval=true`
- `allowOrganizationSelfServiceCreation=true`
- `allowAgencySelfServiceCreation=true`
- `allowRoleCloning=true`
- `allowAgencyScopedCustomRoles=true`
- `allowOrganizationAdminsToGovernAgencies=true`
- `allowBusinessActorSelfReactivation=false`

Table:
- `administration.administrative_platform_options`

## Endpoints exposes

- `GET /api/administration/settings/platform-options`
- `PUT /api/administration/settings/platform-options`
- `POST /api/administration/roles/{roleId}/clone`
- `GET /api/administration/governance/business-actors`
- `POST /api/administration/governance/business-actors/{businessActorId}`
- `GET /api/administration/governance/organizations`
- `POST /api/administration/governance/organizations/{organizationId}`
- `GET /api/administration/governance/agencies`
- `POST /api/administration/governance/agencies/{agencyId}`

## Regles

- un role ne peut etre assigne qu'au scope exact de sa definition
- un role d'agence custom peut etre bloque par `allowAgencyScopedCustomRoles=false`
- le clonage de role peut etre bloque par `allowRoleCloning=false`
- `POST /api/organizations` est un flux de self-service et est bloque si `allowOrganizationSelfServiceCreation=false`
- `POST /api/organizations/{organizationId}/agencies` et `POST /api/warehouses` sont des flux de self-service et sont bloques si `allowAgencySelfServiceCreation=false`
- `POST /api/actors/me/reactivate` est bloque si `allowBusinessActorSelfReactivation=false`
- la self-reactivation d'un `BusinessActor` ne contourne jamais un statut `BLOCKED`
- toute mutation admin importante produit:
  - un audit admin
  - un evenement metier
