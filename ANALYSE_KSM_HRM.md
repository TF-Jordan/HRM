# Analyse complète — Plateforme KSM & module HRM

> Document d'analyse préalable à la construction du frontend HRM en Next.js.
> Rédigé après lecture exhaustive du code source `RT-comops-hrm-core`, du cahier
> de conception `hrm_conception.pdf`, des diagrammes PlantUML `conception/diagrams/*`
> et des esquisses de design `fichiers_design_du_module_hrm/`.

---

## 1. Architecture de la plateforme KSM (vision macro)

### 1.1 Nature de KSM

**KSM (RT-Comops Backend)** est un **monolithe modulaire** Spring Boot organisé en
"cores" hexagonaux indépendants, partageant une seule JVM mais avec des frontières
strictes (interfaces, packages, événements). Le projet est conçu comme une
**plateforme SaaS multi-tenant** (un schéma PostgreSQL par tenant) avec une
hiérarchie à 4 niveaux :

```
System → Tenant → Organisation → Agence
```

### 1.2 Cartographie des modules (cores)

| Core | Responsabilité |
|---|---|
| `RT-comops-kernel-core` | Multi-tenancy, audit, outbox, `ClientApplication`, quotas, sécurité transverse |
| `RT-comops-common-core` | `BaseEntity`, `PartyRef`, `ApiResponse`, `PagedResponse`, adresses, contacts |
| `RT-comops-actor-core` | Identité humaine canonique `BusinessActor` |
| `RT-comops-organization-core` | Organisations, agences, abonnements de services |
| `RT-comops-tp-core` | Tiers externes (clients, fournisseurs, prospects, agents commerciaux) |
| `RT-comops-auth-core` | Comptes techniques, authentification, JWT RS256, `users/me`, onboarding |
| `RT-comops-roles-core` | RBAC multi-scope (SYSTEM / TENANT / ORGANIZATION / AGENCY) |
| `RT-comops-administration-core` | Catalogue de permissions, gestion des rôles, audit admin, gouvernance |
| `RT-comops-file-core` | Métadonnées de fichiers et stockage binaire (MinIO/S3) |
| `RT-comops-product-core` | Catalogue, variantes, prix |
| `RT-comops-inventory-core` | Stock, transformations |
| `RT-comops-resource-core` | Ressources matérielles internes |
| `RT-comops-settings-core` | Settings hiérarchiques organisation / agence / tenant, séquences |
| `RT-comops-sales-core` | Commandes commerciales |
| `RT-comops-accounting-core` | Comptabilité, facturation |
| `RT-comops-treasury-core` | Banques, relevés, chèques, rapprochements |
| `RT-comops-blockchain-core` | Blockchain / signatures |
| `RT-comops-cashier-core` | Caisses |
| `RT-comops-billing-core` | Facturation client |
| **`RT-comops-hrm-core`** | **Module RH — sujet de ce frontend** |
| `RT-comops-bootstrap` | Seul module exécutable (assemblage Spring Boot) |

### 1.3 Modèle de sécurité KSM (5 couches sur `/api/**`)

D'après `KSM/ARCHITECTURE.md`, l'ordre de contrôle pour chaque requête API est :

1. **Authentification `ClientApplication` + JWT éventuel**
   - Headers `X-Client-Id` + `X-Api-Key` → identifie un backend consommateur autorisé
   - Header `Authorization: Bearer <JWT>` → identifie l'utilisateur final (JWT RS256
     signé par `auth-core`, vérifiable via `/.well-known/jwks.json`)
2. **Filtre `ClientApplication → service`** sur les préfixes de routes connus.
   Le service code HRM est appliqué sur `/api/employees` (et par extension sur
   tout le préfixe `/api/v1/hrm/`).
3. **Quota backend Redis** sur `(tenantId, clientId, serviceCode, bucket)` →
   expose les en-têtes `X-IWM-Quota-*`.
4. **Filtre `Organization → service`** : si l'organisation n'est pas abonnée au
   service HRM, retour `ORGANIZATION_SERVICE_NOT_SUBSCRIBED`.
5. **Quota métier Redis** sur `(tenantId, organizationId, serviceCode, bucket)`.
6. **Permissions utilisateur** au bon scope (RBAC `hrm:<resource>:<action>`).

Important : **les endpoints HRM scopés organisation exigent `X-Organization-Id`** ;
son absence retourne `ORGANIZATION_CONTEXT_REQUIRED`.

### 1.4 Propagation du contexte tenant — extrait du `TenantWebFilter`

```
Headers entrants (préférentiels) :
  X-Tenant-Id        UUID
  X-Organization-Id  UUID
  X-Agency-Id        UUID (optionnel)
  Authorization      Bearer <JWT>     (claims: tenantId, organizationId, agencyId, userId, actorId)
```

Si l'en-tête n'est pas présent, les claims du JWT prennent le relais. Le filtre
construit ensuite un `TenantContext` dans le `ReactiveSecurityContext`.

### 1.5 Authentification utilisateur — endpoints exposés

| Endpoint | Usage |
|---|---|
| `POST /api/auth/identify` | Identifie l'utilisateur (avant choix de tenant) |
| `POST /api/auth/discover-contexts` | Liste les tenants/orgs où l'utilisateur peut se connecter |
| `POST /api/auth/login` | Login principal → renvoie JWT (ou défi MFA) |
| `POST /api/auth/login/mfa/confirm` | Confirme un code MFA |
| `POST /api/auth/select-context` | Sélectionne un contexte (tenant + org) après login multi-tenant |
| `GET /api/users/me` | Profil de l'utilisateur courant |
| `PUT /api/users/me/plan` | Mise à jour du plan |
| `PUT /api/users/me/onboarding` | Statut d'onboarding |
| `PUT /api/users/me/identity-onboarding` | Onboarding identité |

Le JWT contient déjà `tenantId`, `organizationId`, `agencyId`, `userId`, `actorId`,
mais **les controllers HRM exigent explicitement `organizationId` en query
parameter** pour les listings — c'est une convention du projet.

### 1.6 Évènements outbox + Kafka publiés par HRM

| Direction | Événement | Cible |
|---|---|---|
| Publie | `PAYROLL_VALIDATED` | accounting-core (écritures comptables) |
| Publie | `PAYMENT_ORDER_CREATED` (1 par employé) | treasury-core |
| Publie | `EMPLOYEE_CREATED`, `CONTRACT_CREATED` | notifications, intégrations |
| Publie | `LEAVE_APPROVED`, `LEAVE_BALANCE_UPDATED`, `LEAVE_SUBMITTED` | notifications |
| Publie | `PAYROLL_CALCULATED`, `LOAN_APPROVED` | consommateurs internes |
| Consomme | `PAYMENT_COMPLETED` / `PAYMENT_FAILED` | ← treasury-core |
| Consomme | `ACTOR_UPDATED` | ← actor-core |

Topic canonique : `iwm.events.business`. Dead-letter : `iwm.events.dead-letter`.

### 1.7 Conventions transverses

- Tables HRM préfixées `hrm_`
- Endpoints REST préfixés `/api/v1/hrm/`
- Permissions au format `hrm:<resource>:<action>` (ex : `hrm:payroll:validate`)
- Tous les identifiants : `UUID` générés côté applicatif
- Tous les montants : `NUMERIC(15,2)` (FCFA)
- Timestamps : `TIMESTAMPTZ`
- Verrouillage optimiste via `version: Long`
- Réponse standardisée : `ApiResponse<T> { boolean success; T data; String message; String errorCode; Instant timestamp; }`

---

## 2. Analyse profonde du module `hrm-core`

### 2.1 Architecture interne (hexagonale)

```
RT-comops-hrm-core/
└── src/main/java/yowyob/comops/api/hrm/
    ├── domain/
    │   ├── model/         (50 fichiers : agrégats, value objects, enums)
    │   ├── *Exception     (EmployeeNotFoundException, DuplicateEmployeeException,
    │   │                   ActorNotFoundException, InsufficientLeaveBalanceException)
    ├── application/
    │   ├── port/in/       (UseCases + Commands)
    │   ├── port/out/      (Repositories + Ports externes)
    │   └── service/       (Implémentations : EmployeeService, PayrollService,
    │                       PayrollCalculationEngine, LeaveAccrualService, …)
    ├── adapter/
    │   ├── in/web/        (15 controllers REST WebFlux)
    │   └── out/persistence/  (Adapters R2DBC PostgreSQL)
    └── config/
        └── HrmCoreConfiguration
```

### 2.2 Acteurs (rôles métier)

Issu de `hrm_conception.pdf` § 3.2 et confirmé par les dossiers de design :

| Acteur | Responsabilités principales |
|---|---|
| **Admin RH** | Gère employés, contrats, dépendants, alertes conformité, lance la paie |
| **DRH** | Pilote formations, budget formation, évaluations, KPIs, conformité |
| **Manager (chef d'équipe)** | Approuve/rejette congés, valide timesheets, réalise évaluations, crée ordres de mission |
| **Employé** | Soumet congés, consulte ses bulletins, demande avances, saisit ses temps, soumet notes de frais, s'inscrit aux formations |
| **Comptable / DAF** | Valide la paie, approuve avances, approuve notes de frais, déclenche remboursements |
| **Responsable Paie** | Consolide variables de temps, produit déclarations sociales (CNPS, DIPE, IRPP/CAC), gère bulletins |
| **Recruteur** | Crée offres, gère candidatures, planifie entretiens, finalise onboarding |
| **Médecin du travail** | Enregistre visites médicales, certificats, suit aptitudes |
| **Contrôleur de gestion RH** | Analyse KPIs, dashboards et reportings RH |

Chaque rôle correspond à un ensemble de permissions `hrm:*:*` distinctes.

### 2.3 Cartographie API REST exhaustive

**Préfixe racine** : `/api/v1/hrm`. **Stack** : Spring WebFlux réactif.
**Toutes les réponses** : `Mono<ResponseEntity<ApiResponse<T>>>`.
**Aucune pagination** sur les listings (Flux → List complet).
**Permission au niveau classe** : `@businessAccessPolicy.hasUserContext(authentication)`.
**Permission au niveau endpoint** : `@businessAccessPolicy.hasPermission(authentication, 'hrm:<resource>:<action>')`.

#### Employés (`EmployeeController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/employees` | `hrm:employee:create` |
| GET | `/api/v1/hrm/employees/{employeeId}` | `hrm:employee:read` |
| GET | `/api/v1/hrm/employees?organizationId=...&agencyId=...` | `hrm:employee:read` |
| PUT | `/api/v1/hrm/employees/{employeeId}` | `hrm:employee:update` |
| PUT | `/api/v1/hrm/employees/{employeeId}/terminate` | `hrm:employee:terminate` |
| PUT | `/api/v1/hrm/employees/{employeeId}/suspend` | `hrm:employee:suspend` |
| PUT | `/api/v1/hrm/employees/{employeeId}/reactivate` | `hrm:employee:reactivate` |
| POST | `/api/v1/hrm/employees/{employeeId}/contracts` | `hrm:contract:create` |
| GET | `/api/v1/hrm/employees/{employeeId}/contracts` | `hrm:contract:read` |
| POST | `/api/v1/hrm/employees/{employeeId}/dependents` | `hrm:dependent:create` |
| GET | `/api/v1/hrm/employees/{employeeId}/dependents` | `hrm:dependent:read` |
| GET | `/api/v1/hrm/employees/{employeeId}/leave-balances?annee=` | `hrm:leave:read` |

#### Congés (`LeaveController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/leaves` | `hrm:leave:create` |
| PUT | `/api/v1/hrm/leaves/{leaveRequestId}/approve` | `hrm:leave:approve` |
| PUT | `/api/v1/hrm/leaves/{leaveRequestId}/reject` | `hrm:leave:approve` |
| PUT | `/api/v1/hrm/leaves/{leaveRequestId}/cancel` | `hrm:leave:create` |
| GET | `/api/v1/hrm/leaves/{leaveRequestId}` | `hrm:leave:read` |
| GET | `/api/v1/hrm/leaves/employee/{employeeId}` | `hrm:leave:read` |
| GET | `/api/v1/hrm/leaves/pending?organizationId=&agencyId=` | `hrm:leave:approve` |

#### Avances / Prêts (`LoanAdvanceController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/loan-advances` | `hrm:loan:create` |
| PUT | `/api/v1/hrm/loan-advances/{id}/approve` | `hrm:loan:approve` |
| PUT | `/api/v1/hrm/loan-advances/{id}/reject` | `hrm:loan:approve` |
| GET | `/api/v1/hrm/loan-advances/{id}` | `hrm:loan:read` |
| GET | `/api/v1/hrm/loan-advances/employee/{employeeId}` | `hrm:loan:read` |
| ❌ MANQUE | `GET /api/v1/hrm/loan-advances/employee/{employeeId}/active` | use case existe |

#### Paie (`PayrollController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/payroll/run` | `hrm:payroll:run` |
| PUT | `/api/v1/hrm/payroll/runs/{id}/validate` | `hrm:payroll:validate` |
| GET | `/api/v1/hrm/payroll/runs/{id}` | `hrm:payroll:read` |
| GET | `/api/v1/hrm/payroll/runs?organizationId=` | `hrm:payroll:read` |
| GET | `/api/v1/hrm/payroll/runs/{id}/entries` | `hrm:payroll:read` |
| GET | `/api/v1/hrm/payroll/entries/{id}/payslip` | `hrm:payroll:read` |

#### Notes de frais (`ExpenseController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/expenses` | `hrm:expense:create` |
| POST | `/api/v1/hrm/expenses/{id}/lines` | `hrm:expense:create` |
| PUT | `/api/v1/hrm/expenses/{id}/submit` | `hrm:expense:create` |
| PUT | `/api/v1/hrm/expenses/{id}/approve` | `hrm:expense:manage` |
| PUT | `/api/v1/hrm/expenses/{id}/reject` | `hrm:expense:manage` |
| PUT | `/api/v1/hrm/expenses/{id}/reimburse` | `hrm:expense:manage` |
| GET | `/api/v1/hrm/expenses/{id}` | `hrm:expense:read` |
| GET | `/api/v1/hrm/expenses?employeeId=` | `hrm:expense:read` |
| GET | `/api/v1/hrm/expenses/{id}/lines` | `hrm:expense:read` |

#### Suivi médical (`MedicalController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/medical/visits` | `hrm:medical:create` |
| GET | `/api/v1/hrm/medical/visits/{id}` | `hrm:medical:read` |
| GET | `/api/v1/hrm/medical/employees/{employeeId}/visits` | `hrm:medical:read` |
| POST | `/api/v1/hrm/medical/certificates` | `hrm:medical:create` |
| GET | `/api/v1/hrm/medical/certificates/{id}` | `hrm:medical:read` |
| GET | `/api/v1/hrm/medical/employees/{employeeId}/certificates` | `hrm:medical:read` |

#### Ordres de mission (`MissionOrderController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/mission-orders` | `hrm:mission:create` |
| PUT | `/api/v1/hrm/mission-orders/{id}/approve` | `hrm:mission:manage` |
| PUT | `/api/v1/hrm/mission-orders/{id}/start` | `hrm:mission:manage` |
| PUT | `/api/v1/hrm/mission-orders/{id}/complete` | `hrm:mission:manage` |
| PUT | `/api/v1/hrm/mission-orders/{id}/cancel` | `hrm:mission:manage` |
| GET | `/api/v1/hrm/mission-orders/{id}` | `hrm:mission:read` |
| GET | `/api/v1/hrm/mission-orders?employeeId=` | `hrm:mission:read` |

#### Recrutement (`RecruitmentController`)

> ⚠ Préfixe atypique : `/api/v1/hrm` (et non `/api/v1/hrm/recruitment`).

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/job-offers` | `hrm:recruitment:create` |
| PUT | `/api/v1/hrm/job-offers/{id}/publish` | `hrm:recruitment:manage` |
| PUT | `/api/v1/hrm/job-offers/{id}/close` | `hrm:recruitment:manage` |
| GET | `/api/v1/hrm/job-offers/{id}` | `hrm:recruitment:read` |
| GET | `/api/v1/hrm/job-offers?organizationId=` | `hrm:recruitment:read` |
| POST | `/api/v1/hrm/applications` | `hrm:recruitment:create` |
| PUT | `/api/v1/hrm/applications/{id}/shortlist` | `hrm:recruitment:manage` |
| PUT | `/api/v1/hrm/applications/{id}/interview` | `hrm:recruitment:manage` |
| PUT | `/api/v1/hrm/applications/{id}/offer` | `hrm:recruitment:manage` |
| PUT | `/api/v1/hrm/applications/{id}/reject` | `hrm:recruitment:manage` |
| PUT | `/api/v1/hrm/applications/{id}/hire` | `hrm:recruitment:manage` |
| GET | `/api/v1/hrm/applications/{id}` | `hrm:recruitment:read` |
| GET | `/api/v1/hrm/job-offers/{jobOfferId}/applications` | `hrm:recruitment:read` |
| POST | `/api/v1/hrm/interviews` | `hrm:recruitment:manage` |
| PUT | `/api/v1/hrm/interviews/{id}/complete` | `hrm:recruitment:manage` |
| GET | `/api/v1/hrm/applications/{applicationId}/interviews` | `hrm:recruitment:read` |
| POST | `/api/v1/hrm/onboarding-tasks` | `hrm:onboarding:create` |
| PUT | `/api/v1/hrm/onboarding-tasks/{id}/start` | `hrm:onboarding:manage` |
| PUT | `/api/v1/hrm/onboarding-tasks/{id}/complete` | `hrm:onboarding:manage` |
| GET | `/api/v1/hrm/onboarding-tasks/employee/{employeeId}` | `hrm:onboarding:read` |

#### Évaluations de performance (`ReviewController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/reviews` | `hrm:review:create` |
| PUT | `/api/v1/hrm/reviews/{id}/submit` | `hrm:review:manage` |
| PUT | `/api/v1/hrm/reviews/{id}/acknowledge` | `hrm:review:manage` |
| PUT | `/api/v1/hrm/reviews/{id}/finalize` | `hrm:review:manage` |
| GET | `/api/v1/hrm/reviews/{id}` | `hrm:review:read` |
| GET | `/api/v1/hrm/reviews/employee/{employeeId}` | `hrm:review:read` |
| GET | `/api/v1/hrm/reviews?organizationId=&periode=` | `hrm:review:read` |
| POST | `/api/v1/hrm/reviews/{id}/objectives` | `hrm:review:manage` |
| PUT | `/api/v1/hrm/reviews/objectives/{objectiveId}/evaluate` | `hrm:review:manage` |
| GET | `/api/v1/hrm/reviews/{id}/objectives` | `hrm:review:read` |

#### Compétences (`SkillController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/skills` | `hrm:skill:create` |
| GET | `/api/v1/hrm/skills/{id}` | `hrm:skill:read` |
| GET | `/api/v1/hrm/skills` | `hrm:skill:read` |
| POST | `/api/v1/hrm/skills/employee-skills` | `hrm:skill:create` |
| GET | `/api/v1/hrm/skills/employees/{employeeId}/skills` | `hrm:skill:read` |
| ❌ MANQUE | `GET /api/v1/hrm/skills/{skillId}/employees` | use case existe |

#### Timesheets (`TimesheetController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/timesheets` | `hrm:timesheet:create` |
| PUT | `/api/v1/hrm/timesheets/{id}/submit` | `hrm:timesheet:create` |
| PUT | `/api/v1/hrm/timesheets/{id}/validate` | `hrm:timesheet:validate` |
| GET | `/api/v1/hrm/timesheets/{id}` | `hrm:timesheet:read` |
| GET | `/api/v1/hrm/timesheets/employee/{employeeId}?periode=` | `hrm:timesheet:read` |
| GET | `/api/v1/hrm/timesheets?organizationId=&periode=` | `hrm:timesheet:read` |

#### Formations (`TrainingController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/trainings` | `hrm:training:create` |
| PUT | `/api/v1/hrm/trainings/{id}/start` | `hrm:training:manage` |
| PUT | `/api/v1/hrm/trainings/{id}/complete` | `hrm:training:manage` |
| PUT | `/api/v1/hrm/trainings/{id}/cancel` | `hrm:training:manage` |
| GET | `/api/v1/hrm/trainings/{id}` | `hrm:training:read` |
| GET | `/api/v1/hrm/trainings?organizationId=` | `hrm:training:read` |
| POST | `/api/v1/hrm/trainings/{id}/enrollments` | `hrm:training:manage` |
| PUT | `/api/v1/hrm/trainings/enrollments/{id}/complete` | `hrm:training:manage` |
| PUT | `/api/v1/hrm/trainings/enrollments/{id}/cancel` | `hrm:training:manage` |
| GET | `/api/v1/hrm/trainings/{id}/enrollments` | `hrm:training:read` |
| GET | `/api/v1/hrm/trainings/enrollments/employee/{employeeId}` | `hrm:training:read` |

#### Budget formation (`TrainingBudgetController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/training-budgets` | `hrm:budget:create` |
| GET | `/api/v1/hrm/training-budgets/{id}` | `hrm:budget:read` |
| GET | `/api/v1/hrm/training-budgets?organizationId=&annee=` | `hrm:budget:read` |
| PUT | `/api/v1/hrm/training-budgets/{id}/engage` | `hrm:budget:manage` |
| PUT | `/api/v1/hrm/training-budgets/{id}/realiser` | `hrm:budget:manage` |

#### Déclarations sociales (`SocialDeclarationController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/declarations` | `hrm:declaration:create` |
| PUT | `/api/v1/hrm/declarations/{id}/generate` | `hrm:declaration:manage` |
| PUT | `/api/v1/hrm/declarations/{id}/submit` | `hrm:declaration:manage` |
| PUT | `/api/v1/hrm/declarations/{id}/acknowledge` | `hrm:declaration:manage` |
| GET | `/api/v1/hrm/declarations/{id}` | `hrm:declaration:read` |
| GET | `/api/v1/hrm/declarations?orgId=` | `hrm:declaration:read` |

#### KPIs RH (`RhKpiController`)

| Méthode | Endpoint | Permission |
|---|---|---|
| POST | `/api/v1/hrm/kpi` | `hrm:kpi:create` |
| GET | `/api/v1/hrm/kpi/{id}` | `hrm:kpi:read` |
| GET | `/api/v1/hrm/kpi?orgId=` | `hrm:kpi:read` |

### 2.4 Modèle de domaine — Agrégats et machines à états

#### Employee (cycle de vie complet)

```
[*] --hire()--> ACTIVE
ACTIVE --goOnLeave()--> ON_LEAVE
ON_LEAVE --returnFromLeave()--> ACTIVE
ACTIVE --suspend(reason)--> SUSPENDED
SUSPENDED --reactivate()--> ACTIVE
ACTIVE --terminate(date,reason)--> TERMINATED
SUSPENDED --terminate(date,reason)--> TERMINATED
```

Champs : `organizationId, agencyId, actorId, matricule (UNIQUE par tenant), numCnps,
categorie, echelon, dateEmbauche, departmentCode, modePaiement, compteBancaire,
numMobileMoney, operateurMm, actorDisplayName`.

#### Contract

États : `ACTIVE, EXPIRED, TERMINATED, RENEWED`.
Types : `CDD, CDI, STAGE, INTERIM`.
Règle : **un seul contrat ACTIVE par employé à la fois**.

#### LeaveRequest

```
[*] --submit()--> PENDING
PENDING --approve(managerId)--> APPROVED
PENDING --reject(managerId, commentaire)--> REJECTED
PENDING --cancel()--> CANCELLED
APPROVED --cancel()--> CANCELLED  (avant date de début)
```

Types : `ANNUAL, SICK, MATERNITY, PATERNITY, UNPAID, SPECIAL`.

#### LoanAdvance

```
[*] --request()--> PENDING
PENDING --approve()--> IN_REPAYMENT  (note: pas d'état APPROVED intermédiaire dans le code, contrairement au PDF)
PENDING --reject(motif)--> REJECTED
IN_REPAYMENT --deduire()--> IN_REPAYMENT  (tant que soldeRestant > 0)
IN_REPAYMENT --deduire()--> FULLY_REPAID  (soldeRestant = 0)
```

#### PayrollRun

```
[*] --create()--> CALCULATED  (note: dans le code, état initial CALCULATED, pas DRAFT comme dans le PDF)
CALCULATED --validate(validatorId)--> VALIDATED
VALIDATED --markPaid()--> PAID
```

> **Incohérence détectée** : le PDF mentionne un état `DRAFT` initial avant
> `CALCULATED`, mais l'implémentation actuelle ne le matérialise pas. La transition
> `CALCULATED → DRAFT` (rejet par le comptable) du PDF n'est pas implémentée non plus.

#### Application (recrutement)

```
[*] --create()--> NEW
NEW --shortlist()--> SHORTLISTED
SHORTLISTED --interview()--> INTERVIEWING
INTERVIEWING --offer()--> OFFERED
OFFERED --hire()--> HIRED
* --reject()--> REJECTED  (sauf si HIRED ou déjà REJECTED)
```

#### Workflows complets

| Workflow | Étapes | Acteurs |
|---|---|---|
| **Onboarding** | offre → application(NEW→…→HIRED) → onboardingTask → Employee.hire() → Contract(ACTIVE) → LeaveBalance init | Recruteur, Admin RH |
| **Cycle de paie** | calcul (CALCULATED) → validation (VALIDATED, événements vers accounting/treasury) → paid (sur callback) | Admin RH, Comptable |
| **Congé** | submit (PENDING) → approve/reject → débit balance | Employé, Manager |
| **Note de frais** | DRAFT → SUBMITTED → APPROVED/REJECTED → REIMBURSED | Employé, Comptable |
| **Mission** | DRAFT → APPROVED → IN_PROGRESS → COMPLETED | Manager, Employé |
| **Évaluation** | DRAFT → SUBMITTED → ACKNOWLEDGED → FINALIZED | Manager/DRH, Employé |
| **Formation** | PLANNED → IN_PROGRESS → COMPLETED + Enrollment(ENROLLED→COMPLETED) | DRH, Employé |
| **Avance** | PENDING → IN_REPAYMENT (déduite sur paie) → FULLY_REPAID | Employé, Comptable |
| **Visite médicale** | enregistrement + certificat + prochaine échéance | Médecin du travail |
| **Déclaration sociale** | DRAFT → GENERATED → SUBMITTED → ACKNOWLEDGED | Responsable Paie |

### 2.5 Règles métier critiques (législation camerounaise)

- **CNPS PV** : part salariale = `min(brut, 750 000) × 4,2%`
- **IRPP** : exonération si brut < 62 000 ; abattement 30% jusqu'à 1 333 333,
  puis abattement plafonné à 400 000. Barème progressif : 10/15/25/35% sur 4 tranches.
- **CAC** : 10% de l'IRPP
- **RAV** : barème forfaitaire mensuel par tranche de brut
- **CFC** : 1% du brut (part salariale)
- **TDL** : barème forfaitaire annuel proratisé
- **Acquisition de congés** : 1,5 j/mois (18 j/an) ; majoration ancienneté
  `floor(ancienneté/5) × 2 j/an` ; majoration enfants < 6 ans (mères salariées)
  `nb_enfants × 2 j/an` ; régime jeunes < 18 ans : 2,5 j/mois.
- **Plafond prêt** : cumul des soldes restants des prêts actifs ne doit pas
  dépasser un plafond paramétrable (settings-core).
- **Unicité paie** : un seul `PayrollRun` par `(période, organizationId, agencyId)`.
- **Unicité employé** : un seul `Employee` par `actorId` dans un tenant.

### 2.6 Dépendances inter-cores

Ports OUT du module HRM (synchrones) :

| Port | Core cible | Usage |
|---|---|---|
| `ActorPort` | `actor-core` | Résolution `PartyRef` (manager, employé, valideur) |
| `OrganizationPort` | `organization-core` | Structure départements |
| `SettingsPort` | `settings-core` | Paramètres paie + séquences (matricule) |
| `FilePort` | `file-core` | Stockage documents (contrats, certificats, justificatifs) |

Dépendances implicites :

- `common-core` → `BaseEntity`, `PartyRef`, `ApiResponse`
- `kernel-core` → outbox, audit, tenant routing
- `auth-core` → JWT (validé en amont)
- `roles-core` → vérification permissions transparente

---

## 3. Analyse UX/UI des designs existants

### 3.1 Direction graphique (HR Core)

D'après `fichiers_design_du_module_hrm/Propostion de theme/styles.css` :

**Palette principale** :

- Orange brand : `#FB8533` (400) → `#F26B0F` (500, primaire) → `#DC560A` (600)
  → `#B0420A` (700)
- Fond chaud : `#F8F5EF` (bg) / `#F1ECE0` (bg-2) / texture grain subtile
- Surfaces sombres : `#110D08` (dark) / `#1C1610` (dark-2) — réservées aux sidebars,
  hero/landing
- Statuts : vert `#10B981`, bleu `#3B82F6`, rouge `#EF4444`, ambre `#F59E0B`,
  violet `#8B5CF6`, teal `#14B8A6`

**Typo** : Inter (corps) + Inter Tight (titres) + JetBrains Mono (références techniques).

**Composants signature** : KPI cards en gradient orange/noir/violet, sidebar
avec sections (Pilotage / Personnel / Activité / Rémunération / Développement /
Conformité / Système), barre de recherche universelle (`⌘K`), Topbar avec
notifications + chip utilisateur, breadcrumb avec **badge UC** (ex : `UC-09`).

**Sentiment** : moderne, premium, africain (palette chaude), orienté SaaS sérieux.

### 3.2 Navigation par sections

La sidebar de référence (cf. `Propostion de theme/lib/shell.jsx`) propose :

```
PILOTAGE       — Tableau de bord, Analytics RH
PERSONNEL      — Employés (badge effectif), Contrats, Compétences, Recrutement (badge)
ACTIVITÉ       — Temps & Présences, Congés (badge), Ordres de mission
RÉMUNÉRATION   — Paie, Avances & Prêts, Notes de frais (badge)
DÉVELOPPEMENT  — Évaluations, Formations, Budget formation
CONFORMITÉ     — Suivi médical, Déclarations
SYSTÈME        — Paramètres
```

Chaque rôle voit une sidebar **filtrée** par permissions. Exemples observés :
- **Employé** : Tableau de bord, Mes congés, Avances & Prêts, Formations,
  Bulletins de paie, Notes de frais, Temps & Présences, Suivi médical, Mon profil
- **Médecin du travail** : Tableau de bord, Visites médicales, Certificats &
  aptitudes, Suivi médical
- **Comptable** : Validation paie, Bulletins, Ordres de paiement, Notes de frais,
  Remboursements, Écritures, Rapports

### 3.3 Pages déjà esquissées dans les designs

#### Côté Admin RH (10 pages)
- Tableau de bord, Employés (liste, fiche, nouveau), Contrats (liste, nouveau, détail),
  Personnes à charge (liste, ajout), Paie (cycles), Lancement de paie,
  Avances (liste), Déclarations (liste)

#### Côté DRH (10 pages)
- Tableau de bord, Employés, Paie, Formations (catalogue), Budget formation,
  Évaluations, Tableaux de bord/KPI, Time-tracking, Trainings, Reporting

#### Côté Comptable / DAF (10 pages)
- Tableau de bord, Validation paie (liste + détail), Validation note de frais,
  Approbation note de frais (liste + détail), Confirmation paie, Reporting,
  Remboursements

#### Côté Manager (6 pages)
- Validation congés, Validation timesheets, Ordres de mission,
  Variables de temps, Évaluations

#### Côté Employé (9 pages)
- Tableau de bord, Nouvelle demande de congé, Avance demande, Bulletins de paie,
  Note de frais soumission, Temps & présences saisie, Inscription formation,
  Suivi médical, Mon profil

#### Côté Recruteur (6 pages)
- Tableau de bord, Offres (liste + publication), Pipeline candidatures,
  Onboarding finalization

#### Côté Médecin du travail (4 pages)
- Tableau de bord, Visites médicales, Certificats & aptitudes, Suivi médical

#### Côté Responsable Paie (5 pages)
- Tableau de bord, Bulletins, Déclarations sociales, Variables de temps

#### Côté Contrôleur RH (4 pages)
- Tableau de bord, Analytics, Reporting, Time-tracking

#### Landing publique (1 page)
- Welcome / homepage marketing avec "14 modules, 1 plateforme"

---

## 4. Analyse critique — Pages et fonctionnalités manquantes

### 4.1 Pages manquantes par rapport au backend exposé

> Légende : ✅ esquissé, ⚠ partiel (pas tous les états/écrans), ❌ non couvert.

#### Authentification & onboarding plateforme

| Page | Statut | Justification |
|---|---|---|
| `/auth/login` | ❌ | `POST /api/auth/login` non couvert |
| `/auth/identify` | ❌ | Pré-login |
| `/auth/mfa/confirm` | ❌ | MFA |
| `/auth/select-context` | ❌ | Choix tenant + organisation après login multi-tenant |
| `/auth/forgot-password` | ❌ | Côté UX |
| `/auth/onboarding` | ❌ | `PUT /api/users/me/onboarding` non couvert |
| `/account/profile` | ⚠ | "Mon profil" esquissé sans formulaire d'édition |
| `/account/security` (MFA, sessions) | ❌ | UX standard |

#### Pilotage

| Page | Statut |
|---|---|
| `/dashboard` (Admin RH, DRH, Comptable, etc., par rôle) | ✅ par rôle |
| `/analytics` (analyses libres, drilldown) | ⚠ vue partielle |
| `/kpi/snapshots` (liste, création, comparaison) | ❌ |
| `/kpi/{id}` (détail snapshot) | ❌ |

#### Personnel (Employés)

| Page | Statut |
|---|---|
| `/employees` (liste filtrable) | ✅ |
| `/employees/new` (création) | ✅ |
| `/employees/{id}` (fiche 360°) | ⚠ pas vu de fiche complète |
| `/employees/{id}/edit` (édition RH) | ❌ |
| `/employees/{id}/terminate` (résiliation) | ❌ |
| `/employees/{id}/suspend` / `/reactivate` | ❌ |
| `/employees/{id}/contracts` (liste + nouveau) | ✅ partiel |
| `/employees/{id}/dependents` (liste + ajout) | ✅ |
| `/employees/{id}/leave-balances` (consultation solde) | ❌ explicite |
| `/employees/{id}/skills` (compétences employé) | ❌ |
| `/employees/{id}/history` (audit / changements) | ❌ |
| `/employees/{id}/documents` (contrats, certificats…) | ❌ |
| `/contracts` (liste globale tous employés) | ✅ |
| `/contracts/{id}` (détail) | ⚠ |
| `/contracts/{id}/renew` | ❌ |
| `/contracts/{id}/terminate` | ❌ |
| `/contracts/expiring` (alerte CDD < 30j) | ❌ |

#### Compétences (Skills)

| Page | Statut |
|---|---|
| `/skills` (référentiel) | ❌ |
| `/skills/new` | ❌ |
| `/skills/{id}` (détail + qui possède la compétence) | ❌ |
| `/skills/employee-skills` (mapping employé↔skill) | ❌ |

#### Recrutement

| Page | Statut |
|---|---|
| `/recruitment` (dashboard pipeline) | ✅ |
| `/recruitment/offers` (liste) | ✅ |
| `/recruitment/offers/new` (publication) | ✅ |
| `/recruitment/offers/{id}` (détail, publish/close) | ⚠ |
| `/recruitment/applications` (toutes candidatures, kanban) | ✅ |
| `/recruitment/applications/{id}` (fiche candidat) | ❌ |
| `/recruitment/applications/{id}/interview` (planification, complétion) | ❌ |
| `/recruitment/interviews` (planning global) | ❌ |
| `/recruitment/onboarding/{employeeId}` (checklist) | ✅ |
| `/recruitment/onboarding-tasks` (vue agrégée) | ❌ |

#### Activité

| Page | Statut |
|---|---|
| `/timesheets` (vue globale org) | ⚠ |
| `/timesheets/my` (employé) | ✅ saisie |
| `/timesheets/manager` (validation) | ✅ |
| `/timesheets/{id}` (détail) | ❌ |
| `/timesheets/new` | ❌ (intégré dans saisie) |
| `/leaves` (gestion globale) | ✅ |
| `/leaves/my` (employé) | ⚠ |
| `/leaves/new` (employé) | ✅ |
| `/leaves/pending` (manager) | ✅ |
| `/leaves/{id}` (détail + actions) | ❌ |
| `/leaves/planning` (planning équipe) | ❌ critical |
| `/leaves/balances` (consultation soldes globaux) | ❌ |
| `/mission-orders` (liste) | ✅ |
| `/mission-orders/new` | ✅ |
| `/mission-orders/{id}` (détail + transitions) | ❌ |

#### Rémunération

| Page | Statut |
|---|---|
| `/payroll` (dashboard cycles) | ✅ |
| `/payroll/runs/new` (lancement) | ✅ |
| `/payroll/runs/{id}` (détail + bulletins) | ✅ partiel |
| `/payroll/runs/{id}/validate` (validation comptable) | ✅ |
| `/payroll/runs/{id}/entries` (bulletins individuels) | ✅ |
| `/payroll/entries/{id}` (détail + payslip lines) | ⚠ |
| `/payroll/entries/{id}/payslip` (bulletin PDF / vue) | ⚠ |
| `/payroll/my` (employé : bulletins personnels) | ✅ |
| `/payroll/payment-orders` (suivi paiements) | ❌ |
| `/loans` (liste) | ⚠ |
| `/loans/new` (employé) | ✅ |
| `/loans/{id}` (détail) | ❌ |
| `/loans/approve` (comptable - file d'attente) | ❌ |
| `/expenses` (liste) | ✅ |
| `/expenses/new` (employé) | ✅ |
| `/expenses/{id}` (détail + lignes) | ⚠ |
| `/expenses/{id}/lines` (gestion lignes) | ❌ |
| `/expenses/approve` (comptable) | ✅ |

#### Développement

| Page | Statut |
|---|---|
| `/reviews` (gestion globale) | ⚠ |
| `/reviews/new` | ✅ |
| `/reviews/{id}` (détail + objectifs + workflow états) | ✅ |
| `/reviews/{id}/submit` (soumission) | ⚠ |
| `/reviews/employee/{employeeId}` (historique perso) | ❌ |
| `/trainings` (catalogue) | ✅ |
| `/trainings/new` (planification) | ❌ |
| `/trainings/{id}` (détail + inscriptions) | ❌ |
| `/trainings/{id}/enrollments` (gestion) | ❌ |
| `/trainings/my` (inscriptions employé) | ❌ |
| `/training-budgets` (liste par année) | ❌ |
| `/training-budgets/new` | ❌ |
| `/training-budgets/{id}` (engagement, réalisation) | ⚠ |

#### Conformité

| Page | Statut |
|---|---|
| `/medical` (dashboard médecin) | ✅ |
| `/medical/visits` (liste) | ✅ |
| `/medical/visits/new` | ❌ |
| `/medical/visits/{id}` (détail) | ❌ |
| `/medical/certificates` (liste) | ✅ |
| `/medical/certificates/new` | ❌ |
| `/medical/certificates/{id}` | ❌ |
| `/medical/employees/{id}/visits` (historique d'un employé) | ❌ |
| `/declarations` (liste sociales) | ✅ |
| `/declarations/new` | ❌ |
| `/declarations/{id}` (détail + workflow) | ❌ |
| `/declarations/{id}/generate` | ❌ |

#### Système

| Page | Statut |
|---|---|
| `/settings` | ❌ |
| `/settings/payroll-rules` (CNPS/IRPP/CAC/RAV/CFC/TDL) | ❌ |
| `/settings/leave-rules` | ❌ |
| `/settings/sequences` (matricules) | ❌ |
| `/settings/organization` (sélection contexte) | ❌ |
| `/settings/users` (admin du tenant) | ❌ |
| `/settings/roles` (visualisation) | ❌ |

**Total : ~80 pages frontend, dont environ 35-40 esquissées et 40-45 totalement
absentes.**

### 4.2 Fonctionnalités backend exposées mais non couvertes par les designs

- **Suspension / réactivation employé** : endpoints existent, aucun design.
- **Renouvellement de contrat** : non explicitement esquissé.
- **Compétences (Skill / EmployeeSkill)** : référentiel global jamais montré.
- **Onboarding tasks** : esquisse partielle pour finalisation, mais pas de
  vue agrégée multi-employé.
- **Mission orders — transitions complètes** : approve/start/complete/cancel.
- **Declarations sociales — génération de fichier (CSV/XML)** : pas de UX.
- **KPI snapshots** : création de snapshot non couverte.
- **Training budgets — engagement vs réalisation** : peu visible.

### 4.3 Incohérences API vs PDF / vs designs

1. **PayrollRun — état `DRAFT`** : présent dans le PDF (§ 5.2 et § 7.1), absent
   du modèle Java (l'agrégat démarre directement en `CALCULATED`). Le design
   du dashboard paie affiche d'ailleurs `calculated/validated/paid` sans `draft`.
   → Cohérence à clarifier. Option : ne pas exposer `DRAFT` côté UI.

2. **Query parameter `orgId` vs `organizationId`** : `RhKpiController` et
   `SocialDeclarationController` utilisent `orgId`, tous les autres
   `organizationId`. → Le BFF doit gérer les deux noms.

3. **Reject avec ou sans motif** : `LeaveController.reject` exige un commentaire,
   `ExpenseController.reject` n'en exige pas. → Côté UX, toujours proposer un
   commentaire optionnel, même si l'API ne le requiert pas.

4. **Aucune pagination** : tous les endpoints listing retournent `List<T>`. → Le
   BFF doit faire la pagination côté serveur (slicing manuel) avant de retourner
   au frontend, et avertir si la volumétrie devient critique.

5. **Aucun endpoint DELETE / PATCH** : les "suppressions" sont des transitions
   d'état (cancel, terminate). À refléter en UX (jamais de bouton "Supprimer"
   pour les agrégats — toujours "Annuler / Résilier / Clôturer").

6. **Aucune recherche avancée** : pas d'endpoint `GET /api/v1/hrm/employees/search`.
   La recherche textuelle doit se faire côté BFF (filtrage en mémoire) ou
   directement Elasticsearch — non documenté pour HRM. → Le BFF peut implémenter
   un mode de recherche locale avec pagination/tri.

### 4.4 Endpoints HRM manquants — ajustements éventuels du backend `hrm-core`

À mentionner à la seconde IA pour qu'elle pose la question avant de coder :

1. `GET /api/v1/hrm/loan-advances/employee/{employeeId}/active`
   → use case `listActiveByEmployee` existe, endpoint non exposé.
2. `GET /api/v1/hrm/skills/{skillId}/employees`
   → use case existe, endpoint non exposé.
3. **Endpoint d'agrégation pour le dashboard** : pas d'endpoint dédié `GET /api/v1/hrm/dashboard/summary?organizationId=&role=` qui retournerait les 4-5 KPIs visibles sur la page d'accueil de chaque rôle. À défaut, le BFF agrégera les multiples appels.
4. **Endpoint d'export PDF/Excel** : pas d'endpoint backend pour bulletins PDF
   ou exports CSV. À implémenter côté BFF (génération côté Node.js).
5. **Endpoint "alertes"** : pas d'endpoint qui renvoie `[CDD expirant, visite
   médicale expirée, budget formation > 90%, ...]`. À agréger côté BFF ou à
   créer côté backend.
6. **Pagination + tri + recherche** : à demander côté backend si la volumétrie
   le justifie. À défaut, BFF.

### 4.5 Améliorations UX/UI suggérées par rapport aux designs

- **Page profil employé "360°"** : actuellement absente. Indispensable pour
  l'Admin RH afin de consulter en un coup d'œil tout sur un employé (RH,
  contrats, congés, formations, paie, médical, évaluations, compétences).
- **Planning de congés équipe** : aucun design. Vue calendaire indispensable
  pour les managers.
- **Workflow visuel** (timeline) sur les pages de détail
  (PayrollRun, ExpenseReport, LeaveRequest, Application) montrant clairement
  l'étape courante et qui doit agir.
- **Notifications temps réel** : SSE / WebSocket via le BFF pour les
  événements `LEAVE_SUBMITTED`, `PAYROLL_VALIDATED`, etc.
- **Sélecteur d'organisation** : actuellement implicite ; à expliciter dans
  le header pour les utilisateurs multi-organisations.
- **Mode sombre** : la palette le permet, optionnel.
- **Accessibilité (a11y)** : focus visible, contraste AA, navigation clavier
  complète, ARIA labels.

---

## 5. Architecture cible — Pseudo-backend (BFF)

### 5.1 Pourquoi un BFF ?

Le `hrm-core` est exposé via Nginx mais demande :
- des en-têtes serveur-à-serveur (`X-Client-Id`, `X-Api-Key`) qu'on **ne peut
  pas exposer au navigateur** (secret),
- un JWT utilisateur (`Authorization: Bearer ...`) qu'il vaut mieux stocker en
  **cookie httpOnly** côté Next.js plutôt qu'en localStorage,
- l'injection contextuelle de `X-Organization-Id` et `X-Agency-Id` selon le
  workspace courant (sélecteur).

Le rôle du BFF :

1. Recevoir les requêtes du frontend (HTTPS, même origine, cookies httpOnly).
2. Lire le JWT depuis le cookie et injecter `Authorization: Bearer ...` vers KSM.
3. Injecter les secrets serveur (`X-Client-Id` + `X-Api-Key`) — jamais exposés
   au navigateur.
4. Injecter le contexte tenant (`X-Tenant-Id`, `X-Organization-Id`,
   `X-Agency-Id`) selon le workspace.
5. Gérer le login → set-cookie httpOnly.
6. Faire la pagination/tri/recherche en mémoire (à défaut d'API paginée).
7. Agréger les multiples appels pour les dashboards (Server Components Next.js).
8. Gérer le cache des données peu volatiles (settings, référentiel compétences,
   permissions utilisateur) via `revalidate` ou Redis local.
9. Encapsuler `ApiResponse<T>` pour propager les erreurs proprement vers le
   client (mapping HTTP).
10. Centraliser la sécurité (CSRF, rate limiting BFF, logs).

### 5.2 Comparaison des approches

| Approche | Avantages | Inconvénients | Recommandation |
|---|---|---|---|
| **Next.js API Routes / Route Handlers** | Même repo, déploiement unique, Server Components peuvent appeler directement le BFF interne, SSR/RSC simple | Cou­plage front/back, scaling commun | ✅ **Recommandé** |
| BFF Node.js séparé (Express/Fastify) | Découplé, peut être scalé indépendamment | Surcoût ops, deux déploiements, double base de connaissances | À considérer si l'équipe est mature |
| NestJS séparé | Structure forte, opinionated | Lourd pour un BFF stateless | Non |
| Kong / Nginx gateway dédié | Performance, mature | Pas d'agrégation applicative possible | Complément, pas remplaçant |

### 5.3 Choix retenu — Next.js 15 (App Router) + Route Handlers

> ✅ **Next.js (App Router) avec Route Handlers** = BFF natif intégré.

Justification :

- L'utilisateur a explicitement énoncé sa préférence pour Next.js si possible.
- L'App Router supporte les Server Components, idéaux pour les pages SSR/SSG qui
  appellent directement les fonctions BFF (sans surcouche HTTP interne).
- Les Route Handlers (`app/api/**/route.ts`) servent les appels client-side
  (TanStack Query / mutations) avec la même base de code.
- Single deployment, single TypeScript codebase, sécurité unifiée (middleware
  Next.js, cookies httpOnly).
- Possibilité d'extraire plus tard en service Node séparé si besoin (la couche
  BFF est isolée en `src/server/`).

### 5.4 Architecture détaillée du BFF Next.js

```
src/
├── app/
│   ├── (auth)/               (route group publique)
│   │   ├── login/page.tsx
│   │   └── select-context/page.tsx
│   ├── (app)/                (route group authentifié)
│   │   ├── layout.tsx        (sidebar + topbar + Auth guard)
│   │   ├── dashboard/page.tsx
│   │   ├── employees/...
│   │   ├── payroll/...
│   │   └── ...
│   ├── api/
│   │   ├── auth/
│   │   │   ├── login/route.ts          (POST → KSM /api/auth/login + set-cookie)
│   │   │   ├── logout/route.ts         (POST → clear-cookie)
│   │   │   ├── select-context/route.ts (POST → KSM /api/auth/select-context)
│   │   │   └── me/route.ts             (GET → KSM /api/users/me)
│   │   └── hrm/
│   │       ├── employees/route.ts
│   │       ├── employees/[id]/route.ts
│   │       ├── employees/[id]/contracts/route.ts
│   │       ├── ... (proxy par ressource)
│   └── layout.tsx
├── server/                   (CŒUR BFF — Server-only)
│   ├── ksm-client.ts         (Fetch wrapper, headers tenant, retries, telemetry)
│   ├── auth.ts               (sessions, cookies httpOnly, JWT decode)
│   ├── tenant-context.ts     (résolution org courante)
│   ├── permissions.ts        (cache des permissions de l'utilisateur)
│   ├── pagination.ts         (slice côté BFF)
│   ├── error-mapper.ts       (ApiResponse → réponse Next.js)
│   └── modules/
│       ├── employees.ts      (méthodes typées : list, get, create, …)
│       ├── payroll.ts
│       ├── leaves.ts
│       └── ... (1 fichier par module HRM)
├── lib/                      (client-safe utils)
│   ├── api-client.ts         (fetcher côté navigateur — appelle /api/hrm/...)
│   ├── query-keys.ts         (clés TanStack)
│   ├── types/                (types partagés générés depuis OpenAPI)
│   └── validation/           (schemas Zod)
├── components/
│   ├── ui/                   (shadcn/ui)
│   ├── shell/                (Sidebar, Topbar, PageHeader, KpiCard…)
│   ├── employees/
│   ├── payroll/
│   └── ...
├── hooks/                    (React hooks : useEmployees, usePayroll, …)
├── stores/                   (Zustand : tenant/org workspace, UI state)
└── middleware.ts             (vérif session, redirection /login si absente)
```

### 5.5 Flux d'une requête

1. **Browser** → `GET /employees` (page React)
2. **Next.js Server Component** : lit la session via cookies (`next/headers`),
   appelle `server/modules/employees.ts → list(orgId)`.
3. **Server function** : injecte `X-Client-Id`, `X-Api-Key`, `X-Tenant-Id`,
   `X-Organization-Id`, `Authorization: Bearer <jwt>` → `GET ${KSM_BASE}/api/v1/hrm/employees?organizationId=...`.
4. **KSM** : 5 couches de sécurité (cf. § 1.3), renvoie `ApiResponse<List<EmployeeResponse>>`.
5. **BFF** : déballe `data`, applique pagination/tri/recherche éventuels.
6. **Server Component** : rend la page HTML avec données initiales.
7. **Client-side hydration** : TanStack Query rebrousse les caches.

Pour les mutations (POST/PUT) :

1. Composant client → `mutation.mutate(payload)` (TanStack Query)
2. → `fetch('/api/hrm/employees', { method:'POST', body, credentials:'include' })`
3. → Route Handler Next.js → `server/modules/employees.create(payload)`
4. → KSM
5. ← réponse → invalidation des caches concernés

### 5.6 Sécurité du BFF

- **Cookies httpOnly + Secure + SameSite=Lax** pour stocker le JWT (jamais
  exposé au JS).
- **CSRF** : token CSRF par session (cookie `__Host-csrf` + header
  `X-CSRF-Token` requis pour mutations).
- **Rate limiting** : `@upstash/ratelimit` ou middleware maison sur `/api/**`.
- **Secrets** : `.env.local` non commit ; en prod, secret manager.
- **CSP, X-Frame-Options, etc.** : configurés via `next.config.js`.
- **Logs** : pino + correlation ID `X-Request-Id`.

---

## 6. Architecture cible — Frontend Next.js

### 6.1 Stack technique recommandée

| Couche | Outil |
|---|---|
| Framework | **Next.js 15 (App Router)** + React 19 |
| Langage | TypeScript strict |
| Style | TailwindCSS v4 + design tokens du thème HR Core |
| UI primitives | shadcn/ui (Radix UI) |
| Charts | Recharts ou Tremor |
| Form | React Hook Form + Zod (resolvers) |
| Validation | Zod (partagé client/serveur) |
| Server state | TanStack Query v5 |
| Client state | Zustand (workspace, UI persistant) |
| Tests | Vitest + Testing Library + Playwright (e2e) |
| Date | date-fns ou dayjs |
| Icônes | lucide-react |
| Logger | pino |
| Auth | iron-session ou jose (JWT decode) |

### 6.2 Conventions de code

- File names en kebab-case
- Composants en PascalCase
- Hooks en camelCase préfixés `use`
- Routes organisées par groupes : `(auth)`, `(app)`, `(public)`
- Server-only files dans `src/server/` (vérifié via `import 'server-only'`)
- Types HRM dans `src/lib/types/hrm.ts`
- Pas de `any` (eslint rule)
- Pas de mocks frontend (interdit par cahier des charges)

### 6.3 Multi-tenant côté frontend

- Au login, `POST /api/auth/login` → choix de contexte si plusieurs orgs.
- Sélecteur d'organisation persistant dans Zustand + cookie ré-envoyé au BFF.
- Le BFF injecte automatiquement `X-Organization-Id`. Le frontend ne le voit
  jamais.
- Le `dashboard` change de contenu selon l'organisation courante.

### 6.4 Permissions côté UI

- Le BFF expose `/api/auth/me` qui retourne aussi la liste des permissions
  utilisateur (résolues par `roles-core`).
- Un store Zustand `usePermissions()` ; un hook `useCan('hrm:payroll:validate')`
  pour cacher / désactiver les actions UI.
- Le rendu pour les pages restreintes : redirection 403 si pas la perm.

### 6.5 Tests

- Pas de mocks. Pour les tests, **deux options à proposer au backend** :
  - **Option A** (préférée) : seeders Liquibase HRM dédiés à un environnement
    `dev`/`demo` avec données réalistes (10 employés, 2 PayrollRun, etc.).
  - **Option B** : tests e2e Playwright sur un environnement complet KSM
    démarré via `docker-compose.application.yml` + script de seed.

---

## 7. Synthèse exécutive

- KSM est une plateforme SaaS multi-tenant moderne, multi-services, où **chaque
  module métier (dont HRM) doit avoir son propre frontend** et un BFF dédié.
- Le module HRM expose **environ 110 endpoints REST** structurés autour de 15
  controllers, couvrant 27 use cases (UC-01 à UC-27).
- Le **BFF doit être un Next.js (App Router)** : Server Components + Route
  Handlers pour les mutations, sessions par cookies httpOnly, secrets backend
  jamais exposés.
- **80 pages frontend environ** sont nécessaires pour couvrir 100% des use
  cases × tous les rôles. Les designs fournis en couvrent ~40 (50%).
- Les rôles à implémenter : Admin RH, DRH, Manager, Employé, Comptable/DAF,
  Responsable Paie, Recruteur, Médecin du travail, Contrôleur de gestion RH
  (9 rôles).
- Plusieurs ajustements éventuels du `hrm-core` à demander : endpoints
  d'agrégation pour dashboards, endpoints de recherche/pagination, génération
  PDF (bulletins) côté BFF.
- Suivi strict : **aucun module KSM hors `hrm-core` ne doit être modifié**
  (read-only). Le BFF ne touche pas KSM, il l'appelle.

---

*Fin du document d'analyse.*
