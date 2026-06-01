# Client Applications

## Objet

Le kernel est appele par des backends consommateurs, pas par des frontends.

L'authentification serveur-vers-serveur repose donc sur une vraie notion de `ClientApplication` au lieu d'une cle globale partagee.

## Modele

Une `ClientApplication` represente un backend consommateur connu du kernel.

Champs principaux:
- `id`
- `clientId`
- `name`
- `description`
- `secretHash`
- `status`
- `systemManaged`
- `allowedServiceCodes`
- `createdAt`
- `updatedAt`
- `lastAuthenticatedAt`
- `secretRotatedAt`

Statuts:
- `ACTIVE`
- `REVOKED`

## Services autorises

Chaque `ClientApplication` porte maintenant sa propre liste `allowedServiceCodes`.

Catalogue actuel:
- `ORGANIZATION`
- `SETTINGS`
- `COMMERCIAL`
- `PRODUCT`
- `INVENTORY`
- `SALES`
- `ACCOUNTING`
- `TREASURY`
- `RESOURCE`

Regles:
- si `allowedServices` est omis a la creation, le kernel autorise tout le catalogue par defaut
- les codes sont normalises via `PlatformServiceCode`
- un backend ne peut appeler qu un service present dans sa liste
- un refus retourne `403 CLIENT_APPLICATION_SERVICE_NOT_ALLOWED`

Cette couche protege l architecture cote plateformes consommatrices.
Elle s ajoute ensuite au quota backend `tenant + client + service`, au filtre d abonnement de l organisation, au quota `organization + service`, puis au RBAC utilisateur.

## Contrat HTTP

Un appel backend -> kernel transporte:
- `X-Client-Id`
- `X-Api-Key`
- `X-Tenant-Id`
- `Authorization: Bearer <jwt>` si un contexte utilisateur est porte
- `X-Organization-Id` si l'endpoint metier est scope organisation

Lecture:
- `X-Client-Id` identifie l'application appelante
- `X-Api-Key` est le secret de cette application
- `Authorization: Bearer ...` porte l'identite utilisateur
- `X-Tenant-Id` fixe le contexte tenant quand le JWT ne suffit pas ou quand le backend veut l'expliciter
- `X-Organization-Id` fixe le scope de securite organisationnel pour les services metier concernes

## Login

Le login utilisateur fait aussi partie du contrat inter-backends.

Donc `POST /api/auth/login` exige deja:
- `X-Client-Id`
- `X-Api-Key`
- `X-Tenant-Id`
- body `principal/password`

La reponse de login retourne aussi `organizations[].services` pour que le backend consommateur sache quels modules exposer a l'utilisateur pour chaque organisation.

## Bootstrap

Pour un demarrage a froid, le kernel peut provisionner un client systeme gere par configuration:
- `IWM_BOOTSTRAP_CLIENT_ENABLED`
- `IWM_BOOTSTRAP_CLIENT_ID`
- `IWM_BOOTSTRAP_CLIENT_NAME`
- `IWM_BOOTSTRAP_CLIENT_DESCRIPTION`
- `IWM_BOOTSTRAP_CLIENT_SECRET`

Ce client bootstrap:
- est persiste comme une vraie `ClientApplication`
- est marque `systemManaged=true`
- peut etre rehydrate/rotate au redemarrage si la configuration change
- peut etre borne des le bootstrap a un sous-ensemble de services via `IWM_BOOTSTRAP_CLIENT_ALLOWED_SERVICES`

## Administration

Endpoints:
- `GET /api/client-applications`
- `POST /api/client-applications`
- `PATCH /api/client-applications/{clientApplicationId}`
- `POST /api/client-applications/{clientApplicationId}/rotate-secret`
- `POST /api/client-applications/{clientApplicationId}/revoke`

Ces endpoints exigent un utilisateur authentifie porteur de:
- `system:admin`
- ou `iam:admin`

## Ordre des controles

Pour une requete metier protegee, le kernel controle maintenant:
1. `ClientApplication` valide (`X-Client-Id` + `X-Api-Key`)
2. `ClientApplication` autorisee sur le service porte par la route
3. quota backend `tenant + client + service`
4. organisation abonnee au service si le module est abonable et scope organisation
5. quota `organization + service` si le module est abonable et scope organisation
6. permission utilisateur resolue depuis le JWT et le contexte courant

Exemple:
- un `sales-platform-backend` peut etre borne a `SALES`, `COMMERCIAL`, `PRODUCT`
- meme si l utilisateur a une permission `treasury:*`, il ne pourra pas appeler `/api/treasury/**` via ce backend
- inversement, un backend `admin-platform-backend` peut etre borne aujourd hui a `ORGANIZATION` et `SETTINGS`, puis completer avec les services metier strictement necessaires

## Bonnes pratiques

- ne jamais partager un meme secret entre plusieurs backends
- donner un `clientId` stable par plateforme consommatrice
- utiliser la rotation reguliere via l'endpoint dedie
- reserver le client bootstrap aux phases de provisioning et aux environnements controles
- preferer ensuite des client applications metier explicites:
  - `sales-platform-backend`
  - `admin-platform-backend`
  - `erp-backend`
- borner chaque backend au minimum de services necessaires
