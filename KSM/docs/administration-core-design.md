# Administration Core Design

Etat de conception au `2026-03-09`.

## Etat d'implementation

La phase 1 et une large partie de la phase 2 sont implementees dans `iwm-administration-core`:
- catalogue des permissions expose
- creation, lecture, edition et suppression de roles custom
- clonage de roles
- remplacement des permissions d'un role avec garde-fous
- assignation et revocation de roles utilisateur
- options generales d'administration exposees
- options plateforme admin persistees dans `administration.administrative_platform_options`
- audit admin persiste en PostgreSQL via `Liquibase`
- RBAC multi-scope implemente:
- `Role.scopeType`
- `UserRoleAssignment.scopeType/scopeId`
- compatibilite conservee avec le champ legacy `scope`
- templates et provisionnement des roles par defaut:
- `GENERAL_ADMIN`
- `ORGANIZATION_ADMIN`
- `AGENCY_ADMIN`
- `SALES_MANAGER`
- `INVENTORY_MANAGER`
- `ACCOUNTANT`
- `TREASURY_OFFICER`
- `RESOURCE_MANAGER`
- gouvernance admin implemente:
- `BusinessActor` avec `PENDING_REVIEW | APPROVED | REJECTED | SUSPENDED | BLOCKED`
- `Organization` avec `PENDING_APPROVAL | APPROVED | REJECTED | SUSPENDED | CLOSED`
- `Agency` avec `ACTIVE | SUSPENDED | CLOSED`

Reste pour les phases suivantes:
- workflows admin plus fins par scope
- consommation plus large des options plateforme dans les flux metier amont secondaires

Options plateforme deja actives dans les workflows:
- `requireBusinessActorApproval` pilote l'onboarding `BusinessActor`
- `requireOrganizationApproval` pilote la creation `Organization`
- `allowRoleCloning` pilote `POST /api/administration/roles/{roleId}/clone`
- `allowAgencyScopedCustomRoles` pilote la creation de roles custom `AGENCY`
- `allowOrganizationAdminsToGovernAgencies` pilote la gouvernance d'agence par admin organisation
- `allowOrganizationSelfServiceCreation` pilote `POST /api/organizations`
- `allowAgencySelfServiceCreation` pilote `POST /api/organizations/{organizationId}/agencies` et `POST /api/warehouses`
- `allowBusinessActorSelfReactivation` pilote `POST /api/actors/me/reactivate`

## Objet

Definir un vrai `administration-core` pour gerer l'administration generale du systeme, sans surcharger `iwm-roles-core` ni melanger bootstrap technique, IAM et administration metier.

## Pourquoi un module dedie

Le besoin "creer des roles et choisir les permissions d'un role" depasse `roles-core`.

`roles-core` doit rester le noyau RBAC:
- `Role`
- `Permission`
- `RolePermission`
- `UserRoleAssignment`

`administration-core` doit porter l'orchestration fonctionnelle:
- catalogue des permissions
- creation et edition de roles
- composition permissions <-> role
- affectation et revocation de roles
- administration globale du tenant et de l'organisation
- audit des actions administratives
- garde-fous sur les roles et permissions sensibles

## Position dans l'architecture

Nouveau module cible:
- `iwm-administration-core`

Dependances autorisees:
- `iwm-kernel-core`
- `iwm-common-core`
- `iwm-roles-core`
- `iwm-auth-core`
- `iwm-organization-core`
- `iwm-settings-core`

Dependances interdites:
- pas de logique `sales`, `inventory`, `accounting`, `treasury` embarquee
- pas de couplage direct aux adapters techniques d'autres modules

## Frontiere fonctionnelle

### Ce que possede `administration-core`

- administration RBAC metier
- administration du catalogue de permissions exposable aux admins
- administration des roles systeme/tenant/organization
- administration des assignations utilisateur
- administration de certains parametres generaux a haut privilege
- audit fonctionnel des operations admin

### Ce qu'il ne possede pas

- authentification pure
- hashage de mot de passe
- emission des access tokens JWT
- calcul metier produit/stock/compta
- securite d'exploitation `management` technique

## Scopes d'administration

Quatre niveaux doivent etre explicites:

- `SYSTEM`
  - administration globale de la plateforme
  - reservee a `system:admin`

- `TENANT`
  - administration transversale du tenant
  - reservee a `tenant:admin`

- `ORGANIZATION`
  - administration metier d'une organisation
  - reservee a `organization:admin`

- `AGENCY`
  - administration locale d'une agence
  - reservee a `agency:admin`

Un role doit porter un scope strict.  
Une permission doit declarer son scope maximal.

## Agregats recommandes

### 1. `AdminRoleProfile`

Vue d'administration d'un role metier.

Champs:
- `roleId`
- `tenantId`
- `organizationId` nullable
- `scope`
- `code`
- `name`
- `description`
- `isSystem`
- `isEditable`
- `isDefault`
- `permissionCodes`

Note:
- l'agregat source peut rester `Role` dans `roles-core`
- `administration-core` orchestre la composition et les garde-fous

### 2. `PermissionCatalogEntry`

Reference administrable de permission.

Champs:
- `code`
- `name`
- `description`
- `module`
- `scope`
- `isSystem`
- `isAssignable`
- `isDeprecated`

### 3. `AdminAuditEntry`

Journal fonctionnel des actions admin.

Champs:
- `id`
- `tenantId`
- `organizationId` nullable
- `actorUserId`
- `action`
- `targetType`
- `targetId`
- `payloadSummary`
- `occurredAt`

## Invariants metier

- un role `isSystem=true` ne peut pas etre supprime par un admin metier
- un role `isEditable=false` ne peut pas etre modifie hors `system:admin`
- une permission `isAssignable=false` ne peut pas etre ajoutee a un role custom
- un role `ORGANIZATION` ne peut pas embarquer de permission `SYSTEM`
- un role par defaut ne peut pas etre supprime s'il est requis par le bootstrap du tenant
- toute mutation d'un role ou d'une assignation doit produire un audit
- la suppression d'un role exige l'absence d'assignations actives

## Cas d'usage minimums

### Catalogue

1. `ListPermissionCatalog`
2. `GetPermissionCatalogEntry`
3. `ListRolesByScope`
4. `GetRoleDetails`

### Gestion des roles

5. `CreateCustomRole`
6. `UpdateCustomRole`
7. `DeleteCustomRole`
8. `CloneRole`
9. `MarkRoleAsDefault`
10. `UnmarkRoleAsDefault`

### Composition permissions <-> role

11. `AttachPermissionsToRole`
12. `DetachPermissionsFromRole`
13. `ReplaceRolePermissions`
14. `ValidateRoleAgainstScopeRules`

### Affectations

15. `AssignRoleToUser`
16. `RevokeRoleFromUser`
17. `ListUserAssignments`
18. `ListOrganizationAdminAssignments`

### Administration generale

19. `ListAdministrativeSettings`
20. `UpdateAdministrativeSettings`
21. `BootstrapOrganizationDefaultRoles`
22. `ListAdminAuditTrail`

## Permissions cibles a introduire

Permissions d'administration generales:
- `administration:read`
- `administration:write`
- `administration:roles:read`
- `administration:roles:write`
- `administration:permissions:read`
- `administration:assignments:write`
- `administration:settings:write`
- `administration:audit:read`

Permissions systeme reservees:
- `system:admin`
- `iam:admin`
- `tenant:admin`
- `organization:admin`

## Endpoints recommandes

Base:
- `/api/administration`

### Permissions

- `GET /api/administration/permissions`
- `GET /api/administration/permissions/{code}`

### Roles

- `GET /api/administration/roles`
- `GET /api/administration/roles/{roleId}`
- `POST /api/administration/roles`
- `PATCH /api/administration/roles/{roleId}`
- `DELETE /api/administration/roles/{roleId}`
- `POST /api/administration/roles/{roleId}/clone`
- `PUT /api/administration/roles/{roleId}/permissions`

### Assignations

- `GET /api/administration/users/{userId}/roles`
- `POST /api/administration/users/{userId}/roles`
- `DELETE /api/administration/users/{userId}/roles/{assignmentId}`

### Parametres et audit

- `GET /api/administration/settings`
- `PUT /api/administration/settings`
- `GET /api/administration/audit`
- `GET /api/administration/settings/platform-options`
- `PUT /api/administration/settings/platform-options`
- `GET /api/administration/governance/business-actors`
- `POST /api/administration/governance/business-actors/{businessActorId}`
- `GET /api/administration/governance/organizations`
- `POST /api/administration/governance/organizations/{organizationId}`
- `GET /api/administration/governance/agencies`
- `POST /api/administration/governance/agencies/{agencyId}`

## Contrats inter-modules

### Avec `roles-core`

Ports sortants attendus:
- `CreateRolePort`
- `UpdateRolePort`
- `DeleteRolePort`
- `FindRolePort`
- `ListRolesPort`
- `AssignRolePort`
- `RevokeRolePort`
- `ListAssignmentsPort`

Point important:
- `roles-core` reste proprietaire du modele RBAC
- `administration-core` n'ecrit pas directement dans les tables `roles`

### Avec `auth-core`

Ports sortants attendus:
- `FindUserAccountPort`
- `ListUserAccountsPort`

Usage:
- resoudre les utilisateurs administrables
- eviter d'assigner un role a un utilisateur inexistant

### Avec `organization-core`

Ports sortants attendus:
- `FindOrganizationPort`
- `FindOrganizationMembershipPort`

Usage:
- verifier le scope d'administration
- restreindre les assignations a l'organisation

### Avec `settings-core`

Ports sortants attendus:
- `ReadAdministrativeSettingsPort`
- `WriteAdministrativeSettingsPort`

Usage:
- exposer des reglages generaux a haut privilege

## Evenements metier recommandes

- `ADMIN_ROLE_CREATED`
- `ADMIN_ROLE_UPDATED`
- `ADMIN_ROLE_DELETED`
- `ROLE_PERMISSIONS_REPLACED`
- `USER_ROLE_ASSIGNED`
- `USER_ROLE_REVOKED`
- `ADMIN_SETTINGS_UPDATED`
- `ADMIN_AUDIT_RECORDED`

## Strategie de mise en oeuvre

### Phase 1

- cataloguer les permissions existantes
- exposer lecture roles/permissions
- creer role custom
- remplacer les permissions d'un role
- assigner/revoquer un role
- audit minimal

### Phase 2

- clone role
- roles par defaut de l'organisation
- filtres par scope
- ecrans/API d'administration plus riches

### Phase 3

- parametres generaux admin
- audit et observabilite admin avances
- workflows d'approbation pour permissions sensibles

## Ce qu'il faut corriger dans l'etat actuel

Etat actuel:
- `roles-core` sait creer un role et assigner un role
- mais il n'existe pas encore de vrai catalogue des permissions
- il n'existe pas encore de composition administrable permission <-> role
- il n'existe pas encore d'audit admin dedie
- il n'existe pas encore de surface d'administration generale

Conclusion:
- `administration-core` est la bonne prochaine brique
- il faut la concevoir comme un module d'orchestration metier
- il ne faut pas transformer `roles-core` en gros module "admin fourre-tout"
