# RT-comops-hrm-core — Module de Gestion des Ressources Humaines

Module HRM (Human Resources Management) du monolithe modulaire ComOps. Il couvre l'ensemble du cycle de vie RH : dossier employé, paie camerounaise, congés, formations, recrutement, médecine du travail, notes de frais, déclarations sociales et tableaux de bord KPI.

## Table des matières

- [Architecture](#architecture)
- [Sous-domaines](#sous-domaines)
- [Modèle de domaine](#modèle-de-domaine)
- [API REST](#api-rest)
- [Base de données](#base-de-données)
- [Intégration cross-module](#intégration-cross-module)
- [Sécurité et permissions](#sécurité-et-permissions)
- [Structure du code](#structure-du-code)

---

## Architecture

Le module suit l'**architecture hexagonale** (Ports & Adapters) du monolithe :

```
hrm/
├── domain/model/          # Entités, value objects, enums (aucune dépendance framework)
├── domain/                # Exceptions métier
├── application/
│   ├── port/in/           # Use cases (interfaces) + commandes (records)
│   └── port/out/          # Ports sortants (repositories + ports cross-module)
│   └── service/           # Implémentations des use cases (@Service)
├── adapter/
│   ├── in/web/            # Contrôleurs REST (@RestController)
│   └── out/persistence/   # Entités R2DBC, Spring Data repos, adaptateurs
└── config/                # Configuration Spring du module
```

**Stack technique** : Java 21, Spring Boot 3.x, WebFlux (réactif), R2DBC / PostgreSQL, Liquibase, Apache Kafka (outbox transactionnel).

---

## Sous-domaines

Le module est organisé en **11 sous-domaines fonctionnels** :

| # | Sous-domaine | Description |
|---|---|---|
| 1 | **Dossier Employé** | Employé, contrats, personnes à charge, soldes de congés, cycle de vie (actif → suspendu → terminé) |
| 2 | **Congés** | Demandes de congé, approbation hiérarchique, annulation, soldes par type, cumul automatique |
| 3 | **Prêts & Avances** | Demandes de prêt/avance sur salaire, workflow d'approbation/rejet |
| 4 | **Feuilles de temps** | Saisie des heures, soumission, validation managériale |
| 5 | **Paie** | Campagnes de paie, calcul du bulletin (moteur fiscal camerounais IRPP/CNPS/CAC), validation |
| 6 | **Formation & Évaluations** | Catalogue de formations, inscriptions, évaluations de performance, objectifs |
| 7 | **Recrutement & Onboarding** | Offres d'emploi, candidatures, entretiens, tâches d'intégration |
| 8 | **Frais & Missions** | Ordres de mission, notes de frais avec lignes détaillées, remboursement |
| 9 | **Médecine du travail** | Visites médicales, certificats d'aptitude (apte / apte avec restrictions / inapte) |
| 10 | **Compétences & GPEC** | Référentiel de compétences, évaluation par employé, budgets de formation |
| 11 | **Déclarations sociales & KPI** | Déclarations réglementaires (CNPS, DIPE, IRPP/CAC), snapshots KPI RH |

---

## Modèle de domaine

### Entités principales

Toutes les entités étendent `BaseEntity` (id, tenantId, createdAt, updatedAt) et suivent le pattern *private constructor + factory methods* (`create()` / `rehydrate()`).

| Entité | Champs clés | Machine à états |
|---|---|---|
| `Employee` | actorId, organizationId, agencyId, matricule, poste, departement, salaire | `ACTIVE` → `SUSPENDED` → `TERMINATED` (+ réactivation) |
| `Contract` | employeeId, type (CDI/CDD/STAGE/INTERIM), dateDebut, dateFin, salaire | `ACTIVE` → `TERMINATED` |
| `Dependent` | employeeId, nom, prenom, dateNaissance, lienParente | — |
| `LeaveBalance` | employeeId, type (ANNUAL/SICK/MATERNITY/...), annee, acquired, taken | — |
| `LeaveRequest` | employeeId, type, dateDebut, dateFin, joursOuvrables | `PENDING` → `APPROVED` / `REJECTED` / `CANCELLED` |
| `LoanAdvance` | employeeId, montant, motif, mensualite | `PENDING` → `APPROVED` / `REJECTED` |
| `Timesheet` | employeeId, periodeDebut, periodeFin, heuresNormales/supplementaires | `DRAFT` → `SUBMITTED` → `VALIDATED` |
| `PayrollRun` | organizationId, agencyId, mois, annee | `DRAFT` → `VALIDATED` |
| `PayrollEntry` | payrollRunId, employeeId, salaireBrut/Net, totalRetenues/Cotisations | — |
| `PayslipLine` | payrollEntryId, libelle, type (GAIN/DEDUCTION/EMPLOYER_CHARGE), montant | Value object (`Persistable<UUID>`) |
| `Training` | titre, formateur, dateDebut, dateFin, capaciteMax | `PLANNED` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED` |
| `TrainingEnrollment` | trainingId, employeeId | `ENROLLED` → `COMPLETED` / `CANCELLED` |
| `PerformanceReview` | employeeId, reviewerId, periode, scoreGlobal | `DRAFT` → `SUBMITTED` → `ACKNOWLEDGED` → `FINALIZED` |
| `ReviewObjective` | reviewId, description, poids, score, commentaire | Value object (`Persistable<UUID>`) |
| `JobOffer` | organizationId, titre, departement, description | `DRAFT` → `PUBLISHED` → `CLOSED` |
| `Application` | jobOfferId, candidatNom/Email/Telephone, lettreMotivation | `RECEIVED` → `SHORTLISTED` → `INTERVIEW` → `OFFERED` → `HIRED` / `REJECTED` |
| `Interview` | applicationId, interviewerName, type, scheduledAt, result | `SCHEDULED` → `COMPLETED` |
| `OnboardingTask` | employeeId, titre, description, assignedTo | `PENDING` → `IN_PROGRESS` → `COMPLETED` |
| `MissionOrder` | employeeId, destination, objet, dateDepart/Retour | `DRAFT` → `APPROVED` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED` |
| `ExpenseReport` | employeeId, missionOrderId | `DRAFT` → `SUBMITTED` → `APPROVED` → `REIMBURSED` / `REJECTED` |
| `ExpenseLine` | expenseReportId, description, montant, categorie, date, justificatifId | Value object (`Persistable<UUID>`) |
| `MedicalVisit` | employeeId, dateVisite, medecinNom, typeVisite, observations | — |
| `MedicalCertificate` | employeeId, visitId, aptitudeResult, restrictions, validUntil | — |
| `Skill` | name, categorie, description | — |
| `EmployeeSkill` | employeeId, skillId, niveauActuel, niveauAttendu, dateEvaluation | — |
| `TrainingBudget` | organizationId, agencyId, annee, montantAlloue/Engage/Realise | Mutations : `engage()`, `realiser()` |
| `SocialDeclaration` | organizationId, type (CNPS/DIPE/IRPP_CAC), periode, format | `DRAFT` → `GENERATED` → `SUBMITTED` → `ACKNOWLEDGED` |
| `RhKpiSnapshot` | organizationId, periode, effectifTotal/Actif, tauxTurnover/Absenteisme, masseSalariale | — |

### Enums

| Enum | Valeurs |
|---|---|
| `EmployeeStatus` | ACTIVE, SUSPENDED, TERMINATED |
| `ContractType` | CDI, CDD, STAGE, INTERIM |
| `ContractStatus` | ACTIVE, TERMINATED |
| `LeaveType` | ANNUAL, SICK, MATERNITY, PATERNITY, UNPAID, SPECIAL |
| `LeaveStatus` | PENDING, APPROVED, REJECTED, CANCELLED |
| `LoanAdvanceStatus` | PENDING, APPROVED, REJECTED |
| `TimesheetStatus` | DRAFT, SUBMITTED, VALIDATED |
| `PayrollRunStatus` | DRAFT, VALIDATED |
| `PayslipLineType` | GAIN, DEDUCTION, EMPLOYER_CHARGE |
| `PaymentChannel` | BANK_TRANSFER, MOBILE_MONEY, CASH |
| `PaymentStatus` | PENDING, PAID |
| `MobileOperator` | MTN, ORANGE |
| `TrainingStatus` | PLANNED, IN_PROGRESS, COMPLETED, CANCELLED |
| `TrainingEnrollmentStatus` | ENROLLED, COMPLETED, CANCELLED |
| `ReviewStatus` | DRAFT, SUBMITTED, ACKNOWLEDGED, FINALIZED |
| `JobOfferStatus` | DRAFT, PUBLISHED, CLOSED |
| `ApplicationStatus` | RECEIVED, SHORTLISTED, INTERVIEW, OFFERED, HIRED, REJECTED |
| `InterviewType` | PHONE, VIDEO, IN_PERSON, TECHNICAL |
| `InterviewResult` | PASS, FAIL, PENDING |
| `OnboardingTaskStatus` | PENDING, IN_PROGRESS, COMPLETED |
| `MissionOrderStatus` | DRAFT, APPROVED, IN_PROGRESS, COMPLETED, CANCELLED |
| `ExpenseReportStatus` | DRAFT, SUBMITTED, APPROVED, REJECTED, REIMBURSED |
| `AptitudeResult` | APTE, APTE_AVEC_RESTRICTIONS, INAPTE_TEMPORAIRE |
| `DeclarationType` | CNPS, DIPE, IRPP_CAC |
| `DeclarationStatus` | DRAFT, GENERATED, SUBMITTED, ACKNOWLEDGED |

### Exceptions métier

| Exception | Usage |
|---|---|
| `ActorNotFoundException` | L'acteur (business actor) référencé n'existe pas |
| `DuplicateEmployeeException` | Un employé existe déjà pour cet acteur |
| `EmployeeNotFoundException` | L'employé demandé n'existe pas |
| `InsufficientLeaveBalanceException` | Le solde de congé est insuffisant pour la demande |

---

## API REST

Toutes les routes sont préfixées par `/api/v1/hrm`. Chaque endpoint est protégé par `@PreAuthorize` avec les permissions RBAC correspondantes.

### Employés — `/api/v1/hrm/employees`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:employee:create` | Créer un employé |
| GET | `/{employeeId}` | `hrm:employee:read` | Consulter un employé |
| GET | `/` | `hrm:employee:read` | Lister les employés |
| PUT | `/{employeeId}` | `hrm:employee:update` | Modifier un employé |
| PUT | `/{employeeId}/terminate` | `hrm:employee:manage` | Résilier un employé |
| PUT | `/{employeeId}/suspend` | `hrm:employee:manage` | Suspendre un employé |
| PUT | `/{employeeId}/reactivate` | `hrm:employee:manage` | Réactiver un employé |
| POST | `/{employeeId}/contracts` | `hrm:employee:manage` | Ajouter un contrat |
| GET | `/{employeeId}/contracts` | `hrm:employee:read` | Lister les contrats |
| POST | `/{employeeId}/dependents` | `hrm:employee:manage` | Ajouter une personne à charge |
| GET | `/{employeeId}/dependents` | `hrm:employee:read` | Lister les personnes à charge |
| GET | `/{employeeId}/leave-balances` | `hrm:employee:read` | Consulter les soldes de congé |

### Congés — `/api/v1/hrm/leaves`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:leave:create` | Soumettre une demande de congé |
| PUT | `/{id}/approve` | `hrm:leave:approve` | Approuver une demande |
| PUT | `/{id}/reject` | `hrm:leave:approve` | Rejeter une demande |
| PUT | `/{id}/cancel` | `hrm:leave:create` | Annuler une demande |
| GET | `/{id}` | `hrm:leave:read` | Consulter une demande |
| GET | `/employee/{employeeId}` | `hrm:leave:read` | Demandes par employé |
| GET | `/pending` | `hrm:leave:approve` | Demandes en attente |

### Prêts & Avances — `/api/v1/hrm/loan-advances`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:loan:create` | Demander un prêt/avance |
| PUT | `/{id}/approve` | `hrm:loan:approve` | Approuver |
| PUT | `/{id}/reject` | `hrm:loan:approve` | Rejeter |
| GET | `/{id}` | `hrm:loan:read` | Consulter un prêt |
| GET | `/employee/{employeeId}` | `hrm:loan:read` | Prêts par employé |

### Feuilles de temps — `/api/v1/hrm/timesheets`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:timesheet:create` | Créer une feuille de temps |
| PUT | `/{id}/submit` | `hrm:timesheet:create` | Soumettre |
| PUT | `/{id}/validate` | `hrm:timesheet:validate` | Valider |
| GET | `/{id}` | `hrm:timesheet:read` | Consulter |
| GET | `/employee/{employeeId}` | `hrm:timesheet:read` | Par employé |
| GET | `/` | `hrm:timesheet:read` | Lister (filtres mois/annee) |

### Paie — `/api/v1/hrm/payroll`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/run` | `hrm:payroll:run` | Lancer une campagne de paie |
| PUT | `/runs/{id}/validate` | `hrm:payroll:validate` | Valider une campagne |
| GET | `/runs/{id}` | `hrm:payroll:read` | Consulter une campagne |
| GET | `/runs` | `hrm:payroll:read` | Lister les campagnes |
| GET | `/runs/{id}/entries` | `hrm:payroll:read` | Bulletins d'une campagne |
| GET | `/entries/{id}/payslip` | `hrm:payroll:read` | Lignes d'un bulletin de paie |

### Formations — `/api/v1/hrm/trainings`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:training:create` | Planifier une formation |
| PUT | `/{id}/start` | `hrm:training:manage` | Démarrer |
| PUT | `/{id}/complete` | `hrm:training:manage` | Terminer |
| PUT | `/{id}/cancel` | `hrm:training:manage` | Annuler |
| GET | `/{id}` | `hrm:training:read` | Consulter |
| GET | `/` | `hrm:training:read` | Lister |
| POST | `/{id}/enrollments` | `hrm:training:manage` | Inscrire un employé |
| PUT | `/enrollments/{id}/complete` | `hrm:training:manage` | Compléter une inscription |
| PUT | `/enrollments/{id}/cancel` | `hrm:training:manage` | Annuler une inscription |
| GET | `/{id}/enrollments` | `hrm:training:read` | Inscriptions d'une formation |
| GET | `/enrollments/employee/{employeeId}` | `hrm:training:read` | Inscriptions par employé |

### Évaluations — `/api/v1/hrm/reviews`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:review:create` | Créer une évaluation |
| PUT | `/{id}/submit` | `hrm:review:manage` | Soumettre |
| PUT | `/{id}/acknowledge` | `hrm:review:manage` | Acquitter |
| PUT | `/{id}/finalize` | `hrm:review:manage` | Finaliser |
| GET | `/{id}` | `hrm:review:read` | Consulter |
| GET | `/employee/{employeeId}` | `hrm:review:read` | Par employé |
| GET | `/` | `hrm:review:read` | Lister |
| POST | `/{id}/objectives` | `hrm:review:manage` | Ajouter un objectif |
| PUT | `/objectives/{id}/evaluate` | `hrm:review:manage` | Evaluer un objectif |
| GET | `/{id}/objectives` | `hrm:review:read` | Objectifs d'une évaluation |

### Recrutement & Onboarding — `/api/v1/hrm`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/job-offers` | `hrm:recruitment:create` | Créer une offre d'emploi |
| PUT | `/job-offers/{id}/publish` | `hrm:recruitment:manage` | Publier |
| PUT | `/job-offers/{id}/close` | `hrm:recruitment:manage` | Clôturer |
| GET | `/job-offers/{id}` | `hrm:recruitment:read` | Consulter |
| GET | `/job-offers` | `hrm:recruitment:read` | Lister |
| POST | `/applications` | `hrm:recruitment:manage` | Créer une candidature |
| PUT | `/applications/{id}/shortlist` | `hrm:recruitment:manage` | Présélectionner |
| PUT | `/applications/{id}/interview` | `hrm:recruitment:manage` | Passer en entretien |
| PUT | `/applications/{id}/offer` | `hrm:recruitment:manage` | Faire une offre |
| PUT | `/applications/{id}/reject` | `hrm:recruitment:manage` | Rejeter |
| PUT | `/applications/{id}/hire` | `hrm:recruitment:manage` | Embaucher |
| GET | `/applications/{id}` | `hrm:recruitment:read` | Consulter une candidature |
| GET | `/job-offers/{id}/applications` | `hrm:recruitment:read` | Candidatures par offre |
| POST | `/interviews` | `hrm:recruitment:manage` | Planifier un entretien |
| PUT | `/interviews/{id}/complete` | `hrm:recruitment:manage` | Compléter un entretien |
| GET | `/applications/{id}/interviews` | `hrm:recruitment:read` | Entretiens par candidature |
| POST | `/onboarding-tasks` | `hrm:recruitment:manage` | Créer une tâche d'intégration |
| PUT | `/onboarding-tasks/{id}/start` | `hrm:recruitment:manage` | Démarrer |
| PUT | `/onboarding-tasks/{id}/complete` | `hrm:recruitment:manage` | Compléter |
| GET | `/onboarding-tasks/employee/{employeeId}` | `hrm:recruitment:read` | Tâches par employé |

### Frais & Missions — `/api/v1/hrm/expenses` et `/api/v1/hrm/mission-orders`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/mission-orders` | `hrm:mission:create` | Créer un ordre de mission |
| PUT | `/mission-orders/{id}/approve` | `hrm:mission:manage` | Approuver |
| PUT | `/mission-orders/{id}/start` | `hrm:mission:manage` | Démarrer |
| PUT | `/mission-orders/{id}/complete` | `hrm:mission:manage` | Terminer |
| PUT | `/mission-orders/{id}/cancel` | `hrm:mission:manage` | Annuler |
| GET | `/mission-orders/{id}` | `hrm:mission:read` | Consulter |
| GET | `/mission-orders` | `hrm:mission:read` | Lister par employé |
| POST | `/expenses` | `hrm:expense:create` | Créer une note de frais |
| POST | `/expenses/{id}/lines` | `hrm:expense:create` | Ajouter une ligne de frais |
| PUT | `/expenses/{id}/submit` | `hrm:expense:create` | Soumettre |
| PUT | `/expenses/{id}/approve` | `hrm:expense:approve` | Approuver |
| PUT | `/expenses/{id}/reject` | `hrm:expense:approve` | Rejeter |
| PUT | `/expenses/{id}/reimburse` | `hrm:expense:approve` | Rembourser |
| GET | `/expenses/{id}` | `hrm:expense:read` | Consulter |
| GET | `/expenses` | `hrm:expense:read` | Lister par employé |
| GET | `/expenses/{id}/lines` | `hrm:expense:read` | Lignes d'une note |

### Médecine du travail — `/api/v1/hrm/medical`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/visits` | `hrm:medical:create` | Enregistrer une visite médicale |
| GET | `/visits/{id}` | `hrm:medical:read` | Consulter |
| GET | `/employees/{employeeId}/visits` | `hrm:medical:read` | Visites par employé |
| POST | `/certificates` | `hrm:medical:create` | Créer un certificat |
| GET | `/certificates/{id}` | `hrm:medical:read` | Consulter |
| GET | `/employees/{employeeId}/certificates` | `hrm:medical:read` | Certificats par employé |

### Compétences — `/api/v1/hrm/skills`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:skill:create` | Créer une compétence |
| GET | `/{id}` | `hrm:skill:read` | Consulter |
| GET | `/` | `hrm:skill:read` | Lister |
| POST | `/employee-skills` | `hrm:skill:create` | Évaluer une compétence pour un employé |
| GET | `/employees/{employeeId}/skills` | `hrm:skill:read` | Compétences par employé |

### Budgets de formation — `/api/v1/hrm/training-budgets`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:budget:create` | Créer un budget |
| GET | `/{id}` | `hrm:budget:read` | Consulter |
| GET | `/` | `hrm:budget:read` | Lister (filtres orgId + annee) |
| PUT | `/{id}/engage` | `hrm:budget:manage` | Engager un montant |
| PUT | `/{id}/realiser` | `hrm:budget:manage` | Réaliser un montant |

### Déclarations sociales — `/api/v1/hrm/declarations`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:declaration:create` | Créer une déclaration |
| PUT | `/{id}/generate` | `hrm:declaration:manage` | Générer le fichier |
| PUT | `/{id}/submit` | `hrm:declaration:manage` | Déposer |
| PUT | `/{id}/acknowledge` | `hrm:declaration:manage` | Accuser réception |
| GET | `/{id}` | `hrm:declaration:read` | Consulter |
| GET | `/` | `hrm:declaration:read` | Lister par organisation |

### KPI RH — `/api/v1/hrm/kpi`

| Méthode | Chemin | Permission | Description |
|---|---|---|---|
| POST | `/` | `hrm:kpi:create` | Créer un snapshot KPI |
| GET | `/{id}` | `hrm:kpi:read` | Consulter |
| GET | `/` | `hrm:kpi:read` | Lister par organisation |

---

## Base de données

28 tables PostgreSQL, créées via 11 migrations Liquibase (V51 à V61). Toutes les tables sont préfixées `hrm_` et suivent les conventions du monolithe (snake_case, `tenant_id` sur chaque table, index composites `(tenant_id, ...)`).

| Migration | Tables |
|---|---|
| `V51` — Employee | `hrm_employee`, `hrm_contract`, `hrm_dependent`, `hrm_leave_balance` |
| `V52` — Leave | `hrm_leave_request` |
| `V53` — Loan | `hrm_loan_advance` |
| `V54` — Timesheet | `hrm_timesheet` |
| `V55` — Payroll | `hrm_payroll_run`, `hrm_payroll_entry`, `hrm_payslip_line` |
| `V56` — Training & Reviews | `hrm_training`, `hrm_training_enrollment`, `hrm_performance_review`, `hrm_review_objective` |
| `V57` — Recruitment | `hrm_job_offer`, `hrm_application`, `hrm_interview`, `hrm_onboarding_task` |
| `V58` — Expenses & Missions | `hrm_mission_order`, `hrm_expense_report`, `hrm_expense_line` |
| `V59` — Medical & HSE | `hrm_medical_visit`, `hrm_medical_certificate` |
| `V60` — Skills & GPEC | `hrm_skill`, `hrm_employee_skill`, `hrm_training_budget` |
| `V61` — Declarations & KPI | `hrm_social_declaration`, `hrm_rh_kpi_snapshot` |

Les fichiers SQL se trouvent dans `RT-comops-bootstrap/src/main/resources/db/r2dbc/` et les descripteurs YAML Liquibase dans `RT-comops-bootstrap/src/main/resources/db/changelog/releases/` (051 à 061).

---

## Intégration cross-module

Le module HRM ne dépend directement que de `RT-comops-common-core` et `RT-comops-kernel-core`. Les dépendances vers les autres modules passent par des **ports sortants** implémentés dans le module bootstrap :

| Port HRM | Implémentation Bootstrap | Module source | Rôle |
|---|---|---|---|
| `ActorPort` | `ActorCoreHrmActorPort` | actor-core | Résolution d'un acteur (nom, identité) par UUID |
| `FilePort` | `FileCoreHrmFilePort` | file-core | Stockage et vérification de documents |
| `SettingsPort` | `SettingsCoreHrmSettingsPort` | settings-core | Génération automatique des matricules employé |

Les adaptateurs sont dans `RT-comops-bootstrap/.../integration/hrm/`.

Le module est enregistré comme service plateforme (`PlatformServiceCode.HRM`) avec les alias `HRM`, `RH`, `HUMAN_RESOURCES` et le routage `/api/v1/hrm` dans `PlatformServiceRouteResolver`.

### Événements métier

Le module publie des événements via `BusinessEventPublisher` (outbox transactionnel Kafka) lors des actions clés : création d'employé, changement de statut, validation de paie.

---

## Sécurité et permissions

Tous les contrôleurs exigent un contexte utilisateur authentifié via `@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")` au niveau de la classe. Chaque endpoint a une permission granulaire (ex : `hrm:employee:create`, `hrm:payroll:run`).

Les permissions sont organisées par sous-domaine :

| Préfixe | Sous-domaine |
|---|---|
| `hrm:employee:*` | Dossier employé |
| `hrm:leave:*` | Congés |
| `hrm:loan:*` | Prêts et avances |
| `hrm:timesheet:*` | Feuilles de temps |
| `hrm:payroll:*` | Paie |
| `hrm:training:*` | Formations |
| `hrm:review:*` | Évaluations de performance |
| `hrm:recruitment:*` | Recrutement et onboarding |
| `hrm:mission:*` | Ordres de mission |
| `hrm:expense:*` | Notes de frais |
| `hrm:medical:*` | Médecine du travail |
| `hrm:skill:*` | Compétences |
| `hrm:budget:*` | Budgets de formation |
| `hrm:declaration:*` | Déclarations sociales |
| `hrm:kpi:*` | KPI RH |

Le multi-tenant est garanti par `ReactiveRequestContextHolder` : chaque service extrait le `tenantId` du contexte réactif et le propage à tous les repositories.

---

## Structure du code

```
RT-comops-hrm-core/src/main/java/yowyob/comops/api/hrm/
│
├── config/
│   └── HrmCoreConfiguration.java
│
├── domain/
│   ├── ActorNotFoundException.java
│   ├── DuplicateEmployeeException.java
│   ├── EmployeeNotFoundException.java
│   ├── InsufficientLeaveBalanceException.java
│   └── model/
│       ├── Employee.java, EmployeeStatus.java
│       ├── Contract.java, ContractType.java, ContractStatus.java
│       ├── Dependent.java
│       ├── LeaveBalance.java, LeaveRequest.java, LeaveType.java, LeaveStatus.java
│       ├── LoanAdvance.java, LoanAdvanceStatus.java
│       ├── Timesheet.java, TimesheetStatus.java
│       ├── PayrollRun.java, PayrollRunStatus.java
│       ├── PayrollEntry.java, PaymentChannel.java, PaymentStatus.java, MobileOperator.java
│       ├── PayslipLine.java, PayslipLineType.java
│       ├── Training.java, TrainingStatus.java
│       ├── TrainingEnrollment.java, TrainingEnrollmentStatus.java
│       ├── PerformanceReview.java, ReviewStatus.java
│       ├── ReviewObjective.java
│       ├── JobOffer.java, JobOfferStatus.java
│       ├── Application.java, ApplicationStatus.java
│       ├── Interview.java, InterviewType.java, InterviewResult.java
│       ├── OnboardingTask.java, OnboardingTaskStatus.java
│       ├── MissionOrder.java, MissionOrderStatus.java
│       ├── ExpenseReport.java, ExpenseReportStatus.java, ExpenseLine.java
│       ├── MedicalVisit.java, MedicalCertificate.java, AptitudeResult.java
│       ├── Skill.java, EmployeeSkill.java
│       ├── TrainingBudget.java
│       ├── SocialDeclaration.java, DeclarationType.java, DeclarationStatus.java
│       └── RhKpiSnapshot.java
│
├── application/
│   ├── port/in/       # 16 use cases + 22 commandes
│   ├── port/out/      # 22 repositories + 3 ports cross-module
│   └── service/       # 15 services (dont PayrollCalculationEngine et LeaveAccrualService)
│
└── adapter/
    ├── in/web/        # 15 contrôleurs REST
    └── out/persistence/  # 28 entités + 28 Spring Data repos + 28 R2DBC adapters
```

**Statistiques** : ~170 fichiers Java, 28 tables, 15 contrôleurs, ~120 endpoints REST.
