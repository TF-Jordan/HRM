# Plan — Onboarding SuperAdmin & provisioning des comptes HRM

> **Objectif** : le propriétaire (OWNER) d'une organisation qui a souscrit au service
> **HRM** (ou **PAYROLL**) utilise **ses identifiants globaux de plateforme** pour se
> connecter au frontend HRM **en tant que SuperAdmin de son organisation**, et y crée
> les autres comptes (DRH, gestionnaire de paie, contrôleur, médecin, employés…).
> **Aucun compte SuperAdmin séparé n'est créé** : le compte global *est* le compte HRM.
>
> Décision validée : le **bootstrap** (provision des rôles + auto-assignation du rôle
> SuperAdmin) se fait **côté BFF, au premier accès HRM** (option A). Une migration
> ultérieure vers un hook backend à la souscription reste possible (Phase 5).
>
> Périmètre modifiable : `RT-comops-hrm-core`, `RT-comops-payroll-core`, `frontend/`.
> Tous les autres cores (auth, kernel, organization, administration, roles, actor…)
> sont **read-only**.

---

## 1. Modèle cible (rappel du workflow officiel)

Source : `RAPPORT-organisation-workflows-api-1.pdf` (kernel-core, 16 juin 2026).

Hors HRM (fait par le OWNER via les API plateforme — domaine du collègue) :

1. `POST /api/auth/sign-up` (avec `tenantId`) → 1 compte + 1 business actor.
2. Devenir **OWNER** → porte `organizations:write` **et** `tenant:admin`.
3. `POST /api/actors/onboarding` + `POST /api/organizations` → org `PENDING_APPROVAL`.
4. `POST /api/organizations/{org}/approve` (nécessite `tenant:admin`) → `APPROVED`.
5. `POST /api/organizations/{org}/services` avec `serviceCode = HRM` (et/ou `PAYROLL`).

Dans HRM (notre périmètre) :

6. Le OWNER se connecte au **frontend HRM** avec **les mêmes identifiants**.
   Comme il porte `tenant:admin`, il est routé vers l'espace **admin** (SuperAdmin).
7. Au **premier accès**, le BFF provisionne les rôles HRM par défaut et auto-assigne
   le rôle `SUPER_ADMIN` (scope ORGANIZATION) au OWNER.
8. Le SuperAdmin crée DRH / paie / etc. (mot de passe temporaire affiché à copier).
9. Le DRH se connecte et crée les employés (chacun reçoit ses identifiants par mail).

---

## 2. Ce qui est DÉJÀ en place ✅

| Brique | Fichier / preuve |
|---|---|
| Auth unifiée (mêmes identifiants partout) | `frontend/src/app/api/auth/login/route.ts` → `discover-contexts`/`select-context` sur auth-core |
| OWNER autorisé à créer des comptes | `kernel-core …/BusinessAccessPolicy.java` `canManageIdentity` = `system:admin`/`iam:admin`/`tenant:admin` ; gate de `POST /api/auth/register` |
| Templates de rôles HRM complets | `administration-core …/AdministrationApplicationService.java` : `SUPER_ADMIN`, `HR_ADMIN`, `HR_DIRECTOR` (DRH), `HR_MANAGER`, `PAYROLL_MANAGER`, `EMPLOYEE`, `RECRUITER`, `OCCUPATIONAL_DOCTOR`, `HR_CONTROLLER` |
| `SUPER_ADMIN` (scope TENANT) inclut `tenant:admin` + `administration:*` + tout `hrm:*` | idem, def. `template("SUPER_ADMIN", …)` |
| Provision des rôles par défaut | `POST /api/administration/roles/defaults` ; wrapper `adminApi.provisionDefaultRoles` |
| Création d'user orchestrée | `frontend/src/server/orchestration/create-user.ts` (`createUserOrchestrated`) : actor→register→assignRole, **renvoie `temporaryPassword`** |
| UI création/listing d'users | `frontend/src/components/admin/user-create-form.tsx`, `users-list.tsx` ; route `frontend/src/app/api/admin/users/route.ts` (POST gardé par `tenant:admin`) |
| Création d'employé orchestrée + mail | `frontend/src/server/orchestration/create-employee.ts` : actor→employee→register→rôle `EMPLOYEE`→`welcomeMail` |
| Aiguillage rôle→espace | `frontend/src/lib/roles.ts` : `ROLE_CODE_TO_SLUG` (`SUPER_ADMIN`→`admin`…) |
| **Filet : `tenant:admin` → espace `admin`** | `frontend/src/lib/roles.ts` `inferRoleSlugFromPermissions` (déjà présent) |
| Cloisonnement par service | 403 sur `/api/v1/hrm/*` si org non souscrite (testé `tools/payroll-onboarding-mock/verify-payonly-cycle.mjs`) |

**Conséquence** : un OWNER `tenant:admin` qui se connecte au frontend HRM est **déjà**
routé vers l'espace `admin` et **peut déjà** créer des users via l'UI existante — même
sans rôle `SUPER_ADMIN` explicite.

---

## 3. Trous à combler ⚠️

| # | Trou | Détail | Sévérité |
|---|---|---|---|
| ~~**G1**~~ | ~~MFA admin non gérée côté front~~ | **LEVÉ en Phase 0** : MFA opt-in (`mfaEnabled=false` par défaut), jamais déclenchée par le flow `discover/select-context`. Pas de gate admin forcé dans ce build. | ~~Bloquant~~ → **Non bloquant** |
| **B2** | **Provisioning de login réservé à `tenant:admin`** | `POST /api/auth/register` gardé par `canManageIdentity`. Aucun rôle ORGANIZATION (DRH, HR_ADMIN…) ne peut créer de compte de login → seul le SuperAdmin le peut. Bloque « le DRH onboard les employés ». | **Haute (décision requise)** |
| **B1** | **Sémantique du rôle DRH** | Le DRH-métier (crée employés/contrats) = template `HR_ADMIN`, pas `HR_DIRECTOR` (lecture/stratégie). | Moyenne |
| **G2** | **Bootstrap SuperAdmin absent** | Rien n'appelle `roles/defaults` ni n'assigne `SUPER_ADMIN` au OWNER à l'arrivée sur HRM. Sans ça : les rôles HRM (DRH, paie…) n'existent pas encore dans le tenant, donc rien à assigner aux comptes créés. | Haute |
| **G3** | **Rôle `SUPER_ADMIN` non porté par le OWNER** | Le OWNER atterrit dans `admin` par *inférence de permission*, pas par code de rôle. Acceptable, mais fragile (dépend de la présence de `tenant:admin` dans la session résolue). | Moyenne |
| **G4** | **Unicité « un seul par rôle »** | « 1 DRH, 1 gestionnaire paie… par organisation » n'est enforced nulle part. | Moyenne |
| **G5** | **Robustesse préconditions** | Org non approuvée / non souscrite → le front doit afficher un message clair, pas un crash/403 brut. | Basse |

---

## 4. Plan d'exécution

### Phase 0 — Vérifications de levée de risque ✅ FAIT (2026-06-20)

Réalisée par analyse statique des chemins d'autorisation (gates en dur dans le code,
non configurables — un test runtime ne changerait pas le verdict).

- [x] **G1 / MFA → NON BLOQUANT.** La MFA est **opt-in** : `UserAccount.create(...)`
      met `mfaEnabled = false` par défaut (`UserAccount.java:82`). La MFA n'est déclenchée
      que par `POST /api/auth/login` **et seulement si** `mfaEnabled = true`
      (`AuthController.java:98`). Le frontend passe par `discover-contexts` /
      `select-context`, qui **n'ont aucune logique MFA**. La chaîne `MFA_REQUIRED_FOR_ADMIN`
      du PDF **n'existe pas** dans ce build → pas de gate admin forcé. ⇒ Le Owner-admin se
      connecte avec email + mot de passe, sans MFA.

- [x] **G3 / aiguillage Owner → espace admin → OK.** `inferRoleSlugFromPermissions`
      (`frontend/src/lib/roles.ts:106`) route `tenant:admin` → `admin`. Un Owner portant
      `tenant:admin` atterrit donc dans l'espace admin même sans code de rôle `SUPER_ADMIN`.
      `hasPermission` (`BusinessAccessPolicy.java:113`) reconnaît `tenant:admin` via le
      suffixe `#TENANT`.

- [x] **« OWNER » n'est PAS un rôle `roles_core`** — c'est un attribut du
      `BusinessActorProfile` (actor-core, champ `role:"OWNER"` du `POST /api/actors/onboarding`).
      Les permissions `organizations:write` + `tenant:admin` viennent d'une **assignation de
      rôle** (templates `SUPER_ADMIN` / `GENERAL_ADMIN` qui les portent). Donc « devenir
      OWNER » = se voir assigner un rôle tenant-scope avec `tenant:admin`.

#### 🔴 BLOCKER découvert — B2 : seul `tenant:admin` peut provisionner des comptes

`POST /api/auth/register` est gardé par `canManageIdentity` =
`{system:admin, iam:admin, tenant:admin}` (`BusinessAccessPolicy.java:11-20`). **Aucun rôle
scope ORGANIZATION ne peut créer de compte de login** :

- `HR_DIRECTOR` (DRH) : permissions HRM stratégiques uniquement, **sans** `tenant:admin`
  — et **sans même `hrm:employee:create`** (`HR_DIRECTOR_PERMISSIONS`, ligne 166). Il ne
  peut donc ni créer d'employé via le BFF (`requirePermissionRoute("hrm:employee:create")`),
  ni provisionner un login.
- `HR_ADMIN` / `HR_MANAGER` : portent tout `hrm:*` (donc `hrm:employee:create`) mais
  **pas** `tenant:admin` → peuvent créer la **fiche** employé, mais l'étape `registerUser`
  de `create-employee.ts` renverra **403** sur `/api/auth/register`.

**Conséquence** : dans l'état actuel, **seul le SuperAdmin (`tenant:admin`) peut créer des
comptes de connexion** (DRH, paie, employés). Le scénario « le DRH onboard les employés
qui reçoivent leurs identifiants par mail » **n'est pas réalisable tel quel**.

#### 🟠 B1 : mismatch de sémantique du rôle « DRH »

Le « DRH » de la vision métier (crée employés/contrats, gère temps & avances) correspond au
template **`HR_ADMIN`** (périmètre HRM complet), **pas** à `HR_DIRECTOR` (lecture/stratégie :
formation, revues, recrutement, budgets, KPI). À clarifier lors de l'assignation.

**Décisions induites (voir §5) :**
1. Soit **le SuperAdmin provisionne tous les logins** (DRH + employés) — réalisable
   immédiatement, 0 dépendance.
2. Soit on demande au collègue d'**ouvrir `/api/auth/register` à une permission scope
   ORGANIZATION** (ex. `administration:assignments:write` ou un nouveau `iam:provision`)
   pour que le DRH/HR_ADMIN onboarde les employés en autonomie — touche kernel/auth
   (read-only) → hors notre périmètre.

### Phase 1 — Bootstrap SuperAdmin au premier accès (BFF) ✅ FAIT (2026-06-20)

Implémenté :
- `frontend/src/server/orchestration/bootstrap-hrm-superadmin.ts` — `ensureHrmSuperAdmin(session)`,
  idempotent, best-effort (ne casse jamais le login), garde `session.hrmBootstrapped`.
- Branché avant `writeSession` dans `frontend/src/app/api/auth/login/route.ts` (auto-select)
  **et** `frontend/src/app/api/auth/select-context/route.ts` (sélection manuelle) — les
  deux seuls points où une session finale est produite et où l'écriture de cookie est permise.
- Ajout du flag `hrmBootstrapped?: boolean` à `AppSession` (`frontend/src/lib/types/auth.ts`).
- Validé : `npm run typecheck` ✅, `eslint` sur les fichiers touchés ✅ (0 erreur),
  `npm run build` ✅ (avec Node 22 via nvm — Node 18 du système est trop ancien).

Reste à faire dans une itération UI (Phase 2) : surfacer `needsReconnect` à l'utilisateur
(bandeau « reconnectez-vous pour finaliser l'accès HRM ») quand `SUPER_ADMIN` vient d'être
auto-assigné. Aujourd'hui c'est seulement journalisé (`logAuthEvent("hrm_bootstrap", …)`).

Spécification de référence (telle qu'implémentée) :

```
ensureHrmSuperAdmin(session):
  préconditions: session.user a tenant:admin (ou OWNER) ET workspace.organizationId résolu
  1. lister les rôles du tenant (GET /api/administration/roles)
  2. si les templates HRM manquent → POST /api/administration/roles/defaults  (idempotent)
  3. si le user n'a aucun rôle HRM dans l'org →
       POST /api/administration/users/{userId}/roles
       { roleId: <SUPER_ADMIN.id>, scopeType: "ORGANIZATION",
         scope: "ORGANIZATION:<org>", scopeId: <org> }
  4. retourner { bootstrapped: bool, mustReconnect: bool }
```

Points d'attention :
- **Idempotence** : tout est ré-exécutable sans effet de bord (defaults = upsert ;
  ne ré-assigne pas si déjà présent).
- **Re-login** : le PDF rappelle que le JWT ne reflète les nouveaux rôles **qu'après
  un nouveau login**. Après bootstrap → inviter à se reconnecter (toast + redirection
  vers `/login`, ou ré-émission de session si on a un mécanisme de refresh).
- **Déclencheur** : à brancher dans le garde de l'espace admin (layout `(app)/admin`)
  ou juste après `select-context` quand `roleSlug === "admin"` et qu'aucun rôle HRM
  n'est présent. Préférer un appel explicite côté serveur, pas dans un composant client.
- **Garde** : ne bootstrapper que si l'org est souscrite à HRM (sinon laisser le 403
  natif faire son office — cf. G5).

**Fichiers touchés** : nouvelle orchestration ; éventuel hook dans
`frontend/src/app/[locale]/(app)/admin/layout.tsx` (ou équivalent) ; réutilise
`adminApi.provisionDefaultRoles`, `adminApi.listRoles`, `adminApi.assignRole`.

**Critère d'acceptation** : un OWNER tout neuf, après souscription HRM, se connecte →
est routé vers `/admin` → les rôles HRM existent dans son tenant → il porte `SUPER_ADMIN`
→ il peut ouvrir la page de création d'utilisateurs.

### Phase 2 — Espace SuperAdmin (UI de création des comptes) ✅ FAIT (2026-06-20)

- [x] Page de création déjà complète : sélection du rôle, **affichage one-shot du mot de
      passe temporaire** + bouton copier (`user-create-form.tsx`) — vérifié, rien à ajouter.
- [x] **G4 — unicité (single-holder)** : la règle existait pour l'écran *role-assignments*
      mais **pas** pour la création. Extrait dans un module partagé
      `frontend/src/server/admin/exclusive-roles.ts` (`EXCLUSIVE_FUNCTION_CODES`,
      `holdersByRole`), désormais source unique pour les deux chemins :
        - `role-assignments/route.ts` refactoré pour l'importer (plus de duplication) ;
        - `users/route.ts` POST **rejette en 409 `ROLE_ALREADY_ASSIGNED`** si un rôle
          fonction demandé est déjà détenu (re-vérification autoritative côté serveur,
          avant toute création) ;
        - le formulaire **désactive** les rôles fonction déjà pris et affiche
          « Déjà attribué à {holder} ».
- [x] i18n fr/en : clés `admin.users.create.roleTaken` / `roleTakenError`.
- [x] Validé : `typecheck` ✅, `eslint` ✅ (0 erreur), `build` ✅.

**Critère d'acceptation atteint** : une 2ᵉ tentative de créer un compte avec un rôle
fonction déjà occupé est refusée (UI désactivée + 409 serveur avec message clair).

### Phase 2b — Bandeau de reconnexion ✅ FAIT (2026-06-20)

- [x] Le bootstrap persiste `session.hrmNeedsReconnect = true` quand il auto-assigne
      `SUPER_ADMIN` (token courant antérieur à l'assignation).
- [x] Propagé : `AppSession` → `/api/auth/me` → `ClientSession` → `(app)/layout.tsx`.
- [x] Nouveau composant non bloquant `frontend/src/components/auth/hrm-reconnect-banner.tsx`
      (monté dans le shell, sous le Topbar) : invite à se reconnecter, bouton qui logout +
      redirige vers `/login`. Après re-login l'utilisateur porte les perms HRM → le bootstrap
      court-circuite et le bandeau disparaît.
- [x] i18n fr/en : `auth.reconnect.*`.
- [x] Validé : `typecheck` ✅, `eslint` ✅ (0 problème), `build` ✅.

### Phase 2c — Résolution B2 (provisioning de login) ✅ FAIT (2026-06-20)

**Découverte bloquante** : l'approche « rôle custom = HRM_ALL + `tenant:admin` » est
**impossible pour un tenant admin**. `tenant:admin` est une **permission protégée**
(`PermissionCatalogService` : `{system:admin, iam:admin, tenant:admin, …}`) ; **créer** un
rôle qui la contient exige `canUseProtectedPermissions` = `system:admin`/`iam:admin`
(`AdministrationController:472`), que le SuperAdmin/OWNER n'a pas. (En revanche, **assigner**
un rôle existant porteur de `tenant:admin`, ex. `SUPER_ADMIN`, ne demande que `tenant:admin`
— d'où le bootstrap Phase 1.)

**Décision retenue** : **le SuperAdmin provisionne les logins** ; le DRH (`HR_ADMIN`, sans
`tenant:admin`) gère fiches/contrats/temps mais ne crée pas de comptes. C'est le découpage
que la plateforme impose de toute façon. 0 dépendance, moindre privilège.

Implémenté :
- `create-employee.ts` : helper `canProvisionLogins(session)` (= `tenant:admin`/`system:admin`/
  `iam:admin`). Si un DRH demande un login sans en avoir le droit → l'étape `register` est
  **sautée proprement** (`loginSkipped = "forbidden"`) au lieu de 403 en plein flux ; la
  fiche employé est bien créée.
- `employee-create-form.tsx` : branche d'affichage dédiée (note info « Accès créé par
  l'administrateur ») quand `loginSkipped === "forbidden"`.
- i18n fr/en : `employees.create.credentials.loginByAdmin*`.
- Retiré le wrapper `createRole` (admin.ts) ajouté pour l'approche custom abandonnée.
- Validé : `typecheck` ✅, `eslint` ✅ (0 erreur), `build` ✅.

**Suivi possible (hors périmètre, collègue)** : si on veut un jour un DRH **autonome** sur
l'onboarding employé, faire ajouter par la plateforme soit un template
`HR_DIRECTOR_DELEGATED` (HRM_ALL + `tenant:admin`) que le SuperAdmin assignerait, soit
l'ouverture de `/api/auth/register` à une permission scope ORGANIZATION.

### Phase 3 — DRH crée les employés

- [ ] Re-vérifier `create-employee.ts` : rôle `EMPLOYEE` bien assigné, mail de bienvenue
      bien envoyé (mode SMTP vs preview selon l'env — cf. PDF §3.4).
- [ ] S'assurer que le DRH (rôle `HR_DIRECTOR`/`HR_ADMIN`) a les permissions pour
      l'orchestration (actor + register + assignRole `EMPLOYEE`). NB : `register` exige
      `canManageIdentity` = `tenant:admin`. **Vérifier** si le DRH (scope ORGANIZATION)
      le possède ; sinon, soit la création de compte employé reste au SuperAdmin, soit
      on étend le gate (hors périmètre → collègue).

> ⚠️ **Risque à confirmer en Phase 0/3** : `POST /api/auth/register` est gardé par
> `tenant:admin` (scope tenant). Un DRH scope ORGANIZATION pourrait ne **pas** pouvoir
> créer les comptes de login des employés. Si confirmé, deux options : (a) le SuperAdmin
> crée les comptes, le DRH ne fait que les fiches RH ; (b) demander au collègue d'ouvrir
> `register` à un scope organisation. À trancher selon le résultat de la vérification.

### Phase 4 — Robustesse du cloisonnement (G5)

- [ ] Intercepter les 403 « org non souscrite / non approuvée » sur les routes HRM et
      afficher un écran d'explication (« souscrivez au service HRM », lien vers le portail
      organisation) plutôt qu'une erreur brute.

### Phase 5 — Coordination collègue (hors périmètre, optionnel/ultérieur)

- [ ] Proposer que le bootstrap (Phase 1) soit déplacé dans un **hook à la souscription**
      HRM côté `organization-core`/`administration-core` (provision des rôles + assignation
      `SUPER_ADMIN` au OWNER au moment du `POST /services`). Plus propre, supprime la
      dépendance au « premier login ».
- [ ] Statuer sur la **MFA admin** (G1) : flow de challenge à exposer/gérer, ou exemption.

---

## 4bis. Validation E2E en live (2026-06-20, KSM:8080 + front:3000)

> Note : le frontend n'était PAS démarré (rien sur :3000) — relancé avec Node 22.

Validé bout-en-bout contre le vrai stack :
- ✅ **Plateforme (PDF)** : owner créé (actor + compte + rôle SUPER_ADMIN) → business actor →
  organisation `PENDING_APPROVAL` → `approve` → `APPROVED` → souscription **HRM** OK
  (`effectiveServices: [ORGANIZATION, SETTINGS, HRM]`).
- ✅ **Login HRM sans MFA** (confirme Phase 0). `hrm_session` posé par le BFF.
- ✅ **Bootstrap** : court-circuité correctement pour un owner déjà privilégié (perms HRM
  présentes) → pas de double-écriture, `hrmNeedsReconnect=false`.
- ✅ **Création DRH** via BFF (`createUserOrchestrated`) : compte + mot de passe temporaire renvoyé.
- ✅ **Unicité G4** : 2ᵉ DRH (même rôle fonction) → **409 `ROLE_ALREADY_ASSIGNED`** nommant le titulaire.

- ✅ **Création employé (happy path)** : super.admin (`tenant:admin`) → employé **EMP000010**
  avec **login provisionné** (rôle EMPLOYEE assigné). 201.
- ✅ **Création employé — B2 `loginSkipped`** : un `HR_MANAGER` (porte `hrm:employee:create`
  mais **aucune** perm d'identité) → employé **EMP000012** créé, **`login:null`**,
  **`loginSkipped:"forbidden"`**, 0 warning. La branche B2 fonctionne exactement comme prévu
  (fiche créée, provisioning de login sauté proprement — pas de 403 en plein flux).

**Fix BFF apporté (2026-06-20)** : `create-employee.ts` provisionne désormais la séquence
`HRM_MATRICULE` de l'org si absente (`ensureMatriculeSequence`, via nouveau module
`server/ksm/modules/settings.ts`, endpoint `POST /api/settings/document-sequences`). Confirmé
en live : l'erreur passe de `DocumentSequenceNotFoundException` à génération effective du
matricule. **Suffisant pour la production** (un tenant par owner ⇒ tenant vide ⇒ pas de
collision).

**Problème backend résolu (option b retenue)** : `idx_hrm_employee_tenant_matricule` était
UNIQUE `(tenant_id, matricule)` (tenant-wide) alors que les séquences sont par org → collision
`EMP000001` en tenant multi-org. Migration **V103**
(`V103__hrm_employee_matricule_per_org_unique.sql` + release 103 + master) : drop de l'ancien
index, création de `idx_hrm_employee_tenant_org_matricule` UNIQUE `(tenant_id, organization_id,
matricule)`. Index strictement plus permissif → aucune violation des lignes existantes.
Appliquée aussi à chaud sur la base de dev pour test immédiat (idempotente avec la migration).

**E2E final (org neuve, après fix) — TOUT VERT :**
- Employé créé par l'owner (`tenant:admin`) → **EMP000003 / EMP000004**, login provisionné. ✅
- Employé créé par un `HR_MANAGER` (sans identité) → **EMP000005**, `loginSkipped:"forbidden"`. ✅
- Matricule s'incrémente correctement par org, plus aucune collision tenant-wide.

**Constats backend (hors périmètre onboarding, pour info) :**
1. `POST /api/v1/hrm/employees` sur une **org fraîche** → 500
   `DocumentSequenceNotFoundException: HRM_MATRICULE`. La séquence de matricule n'est seedée
   que pour l'org démo a0002 (`V69`). Toute nouvelle org a besoin qu'on lui provisionne une
   séquence `HRM_MATRICULE` (settings-core) avant de pouvoir créer des employés.
2. La colonne `hrm_employee.compte_bancaire` est `varchar(30)` : un IBAN/RIB de 31 caractères
   fait planter l'INSERT (`value too long`). Le front devrait valider la longueur ≤ 30 (ou
   hrm-core élargir la colonne).
3. Le compte démo `hr.admin@hrcore.demo` porte **`iam:admin`** (sur-privilégié) — il PEUT donc
   provisionner des logins ; c'est pourquoi il ne déclenche pas `loginSkipped` (comportement
   correct). Pour un vrai DRH non-identité, utiliser un rôle type `HR_MANAGER`.

## 5. Risques & dépendances

- **G1 (MFA)** est la seule inconnue potentiellement bloquante. À lever en Phase 0.
- **Register scope tenant** (Phase 3) peut limiter ce que le DRH peut faire seul.
- Tout le reste est dans notre périmètre et s'appuie sur des briques existantes.

## 6. Démo / non-régression

- Les comptes de démo actuels (`super.admin@hrcore.demo`, etc., seedés en V068/V076/V100)
  restent valides : ce sont des comptes pré-câblés équivalents au résultat du bootstrap.
  Le nouveau flux concerne les **vrais** tenants créés via le workflow PDF.
- Le tenant standalone `PAYONLY S.A.` (PAYROLL only, sans HRM) reste cloisonné — vérifier
  que le bootstrap ne s'y déclenche pas (pas de souscription HRM → pas de SuperAdmin HRM).
