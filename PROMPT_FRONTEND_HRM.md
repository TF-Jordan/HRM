# PROMPT — Construction du frontend HRM (Next.js) de la plateforme KSM

> Ce document est un brief complet, autonome et exploitable directement, destiné
> à une IA spécialisée dans le développement frontend Next.js / TypeScript.
> Il succède à un travail d'analyse documenté dans `ANALYSE_KSM_HRM.md`
> (à la racine du dépôt). Tu peux et tu **dois** consulter ce document
> ainsi que les fichiers cités, avant toute implémentation.
>
> **IMPORTANT** : ce prompt te demande **d'abord de produire un plan détaillé**
> que l'utilisateur va critiquer, puis seulement après accord, d'implémenter
> phase par phase. **Ne commence à coder qu'après validation du plan.**

---

## 0. Identité de la mission

### Qui tu es
Tu es une IA experte en développement frontend, spécialisée :
- Next.js 16 (App Router) + React 19 + TypeScript strict
- TailwindCSS + shadcn/ui (Radix UI primitives)
- TanStack Query v5, React Hook Form + Zod, Zustand
- Architecture BFF (Backend For Frontend), sécurité web (cookies httpOnly,
  CSRF, RBAC, multi-tenant), accessibilité (a11y), i18n, dark mode.

### Ce que tu vas construire
Le **frontend du module HRM** (gestion des ressources humaines) de la plateforme
SaaS multi-tenant **KSM (RT-Comops Backend)**, ainsi que son **pseudo-backend
(BFF)** intégré en Next.js Route Handlers / Server Components.

### Ce que tu ne dois PAS faire
- Tu ne touches **AUCUN module backend KSM autre que `RT-comops-hrm-core`**.
  Tous les autres cores (`auth-core`, `kernel-core`, `actor-core`,
  `roles-core`, etc.) sont **read-only** pour toi. Considère-les comme
  des dépendances figées si une modification est necessaire, fais en la demande en justifiant pourquoi tu dit ce que tu veux faire, pourquoi tu ne peux pas le faire avec la version actuelle et en quoi la modification le permettra apres ca uniquement on pourra valider la modification .
- Tu n'utilises **JAMAIS** de mocks, fake data, faux JSON, fake API ou UI
  statique déconnectée. Tout doit aller au vrai backend KSM, qui est
  fonctionnel. Pour tester avec des données, propose des seeders SQL/Liquibase
  côté backend, jamais en frontend (mest des seeders pour generer autaumatiquement des données en BD au demarrage).
- Tu ne crées pas de fichiers de documentation autres que ceux du frontend
  (ex: pas de `decisions-log.md`). Le commit message et le code parlent.

---

## 1. Contexte du projet KSM

### 1.1 Architecture macro

KSM est un **monolithe modulaire Spring Boot** (Java, WebFlux réactif) composé
de ~20 cores hexagonaux indépendants. Chaque core est une application métier
autonome :

- `kernel-core` : multi-tenancy, sécurité transverse, outbox, quotas
- `auth-core` : authentification utilisateurs (JWT RS256)
- `roles-core` : RBAC multi-scope (SYSTEM / TENANT / ORGANIZATION / AGENCY)
- `actor-core` : identités humaines, contrat canonique `BusinessActor`
- `organization-core` : organisations, agences, abonnements aux services
- `settings-core` : paramètres hiérarchiques (Agence → Org → Tenant), séquences
- `file-core` : stockage de fichiers (MinIO/S3)
- **`hrm-core`** : **module RH — c'est notre cible**
- (autres : sales, accounting, treasury, product, inventory, tp, etc.)

**Principe fondamental** : KSM est un noyau backend, il ne doit jamais
communiquer directement avec un frontend. Chaque module fournit son propre
frontend qui passe par un **BFF (Backend For Frontend)** chargé d'injecter le
contexte tenant et les secrets serveur.

### 1.2 Modèle multi-tenant

Hiérarchie à 4 niveaux : `System → Tenant → Organisation → Agence`.

Headers acceptés par KSM (filtre `TenantWebFilter` côté `kernel-core`) :

| Header | Source | Usage |
|---|---|---|
| `Authorization: Bearer <JWT>` | auth-core, signé RS256 | identifie l'utilisateur, contient `tenantId`, `organizationId`, `agencyId`, `userId`, `actorId` |
| `X-Tenant-Id` | UUID | override l'identifiant tenant du JWT |
| `X-Organization-Id` | UUID | **REQUIS** pour les endpoints scopés org (sinon `ORGANIZATION_CONTEXT_REQUIRED`) |
| `X-Agency-Id` | UUID | filtre optionnel par agence |
| `X-Client-Id` | string | identifie le backend consommateur (ex: notre BFF) |
| `X-Api-Key` | secret | secret partagé avec ClientApplication enregistrée |

> **Conséquence pour le BFF** : `X-Client-Id` + `X-Api-Key` sont des **secrets
> serveur** qui ne doivent jamais transiter par le navigateur. Le BFF les
> injecte côté serveur uniquement.

### 1.3 Sécurité KSM (5 couches sur `/api/**`)

D'après `KSM/ARCHITECTURE.md` :

1. Authentification `ClientApplication` (X-Client-Id + X-Api-Key) + JWT éventuel
2. Filtre `ClientApplication → service` sur préfixes connus (HRM = `/api/v1/hrm/*`)
3. Quota backend Redis `(tenantId, clientId, serviceCode, bucket)` →
   en-têtes `X-IWM-Quota-*`
4. Filtre `Organization → service` : l'organisation doit avoir un abonnement
   `HRM` actif (sinon `ORGANIZATION_SERVICE_NOT_SUBSCRIBED`)
5. Quota métier Redis `(tenantId, organizationId, serviceCode, bucket)` →
   `X-IWM-Organization-Quota-*`
6. Permissions utilisateur RBAC au format `hrm:<resource>:<action>`

### 1.4 Format de réponse standard KSM

Toutes les réponses sont enveloppées dans :

```ts
type ApiResponse<T> = {
  success: boolean;
  data: T | null;
  message: string;
  errorCode: string | null;
  timestamp: string;  // ISO Instant
}
```

Codes HTTP utilisés :
- `200 OK` (lecture, mise à jour réussie)
- `201 CREATED` (création)
- `400 BAD_REQUEST` (validation, données invalides)
- `401 UNAUTHORIZED` (JWT invalide/absent)
- `403 FORBIDDEN` (permission refusée)
- `404 NOT_FOUND` (entité absente)
- `409 CONFLICT` (doublon, état invalide pour transition)
- `422 UNPROCESSABLE_ENTITY` (règle métier violée — ex : solde de congés insuffisant)

---

## 2. Module HRM — Périmètre fonctionnel exhaustif

> Source : `RT-comops-hrm-core/`, `hrm_conception.pdf`, `conception/diagrams/*`.
> **Cahier de conception** : `hrm_conception.pdf` à la racine, intégralement
> traduit en texte dans `ANALYSE_KSM_HRM.md`.

### 2.1 Stack backend HRM

- Spring WebFlux réactif (Project Reactor)
- R2DBC PostgreSQL (un schéma par tenant)
- Apache Kafka (outbox, topic `iwm.events.business`)
- JWT validé par `kernel-core`
- RBAC vérifié par annotations `@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:...:...')")`
- Tous les controllers : `@Profile("!test-memory")`
- Préfixe REST : `/api/v1/hrm/`

### 2.2 Rôles métier (acteurs)

10 rôles distincts, chacun avec ses permissions :

| Rôle | Permissions principales | Pages clés |
|---|---|---|
| **SuperAdmin** | Toutes les permissions de l'organisation + `org:*`, `roles:*`, `settings:*`, `actor:*` | Gestion organisation, attribution rôles, paramètres, onboarding plateforme |
| **Admin RH** | `hrm:employee:*`, `hrm:contract:*`, `hrm:dependent:*`, `hrm:payroll:run`, `hrm:medical:*`, `hrm:training:*` | Gestion personnel, lancement paie, conformité, dashboard org |
| **DRH** | `hrm:training:*`, `hrm:budget:*`, `hrm:review:*`, `hrm:kpi:*`, `hrm:declaration:read` | Pilotage formations, budget, évaluations, KPIs |
| **Manager** | `hrm:leave:approve`, `hrm:timesheet:validate`, `hrm:mission:manage`, `hrm:review:manage`, `hrm:expense:manage` | Validation congés/timesheets/frais, missions, évaluations |
| **Employé** | `hrm:leave:create`, `hrm:expense:create`, `hrm:loan:create`, `hrm:timesheet:create`, lecture de son propre dossier | Self-service : congés, paie, frais, formations, profil |
| **Comptable / DAF** | `hrm:payroll:validate`, `hrm:loan:approve`, `hrm:expense:manage` | Validation paie, approbation avances, remboursement frais |
| **Responsable Paie** | `hrm:payroll:*` (read), `hrm:declaration:manage`, `hrm:timesheet:*` | Bulletins, déclarations sociales, variables de temps |
| **Recruteur** | `hrm:recruitment:*`, `hrm:onboarding:*`, `hrm:employee:create` | Offres, candidatures, entretiens, onboarding |
| **Médecin du travail** | `hrm:medical:*` | Visites médicales, certificats, suivi |
| **Contrôleur de gestion RH** | `hrm:kpi:read`, `hrm:payroll:read`, analytics tous modules en lecture | Analytics, reporting, dashboards |

Un même utilisateur peut cumuler plusieurs rôles. Le RBAC est **scopé** (`SYSTEM`,
`TENANT`, `ORGANIZATION`, `AGENCY`).

### 2.3 Use cases (27 — UC-01 à UC-27)

| ID | Nom | Acteurs |
|---|---|---|
| UC-01 | Créer un employé (lié à un Actor existant) | SuperAdmin, Admin RH, Recruteur |
| UC-02 | Modifier les données RH d'un employé | Admin RH |
| UC-03 | Résilier / Terminer un employé | Admin RH |
| UC-04 | Gérer les contrats de travail | Admin RH |
| UC-05 | Gérer les personnes à charge | Admin RH |
| UC-06 | Lancer le calcul de paie mensuel | Admin RH |
| UC-07 | Valider la paie | Comptable / DAF |
| UC-08 | Générer les ordres de paiement | Comptable / DAF (auto via validation) |
| UC-09 | Soumettre une demande de congé | Employé |
| UC-10 | Approuver / Rejeter un congé | Manager |
| UC-11 | Demander une avance sur salaire | Employé |
| UC-12 | Approuver une avance | Comptable / DAF |
| UC-13 | Planifier une formation | DRH |
| UC-14 | S'inscrire à une formation | Employé |
| UC-15 | Réaliser une évaluation de performance | Manager, DRH |
| UC-16 | Publier une offre d'emploi | Recruteur, DRH |
| UC-17 | Gérer les candidatures et entretiens | Recruteur, Manager |
| UC-18 | Finaliser l'onboarding | Recruteur, Admin RH |
| UC-19 | Saisir / importer les temps de travail | Employé, Manager, Responsable Paie |
| UC-20 | Valider les temps pour la paie | Manager, Responsable Paie |
| UC-21 | Émettre un ordre de mission | Manager, Admin RH |
| UC-22 | Soumettre une note de frais | Employé, Comptable / DAF |
| UC-23 | Enregistrer une visite médicale | Médecin du travail, Admin RH |
| UC-24 | Maintenir la cartographie des compétences | DRH, Manager |
| UC-25 | Piloter le budget formation | DRH, Contrôleur de gestion RH |
| UC-26 | Produire les déclarations sociales et fiscales | Responsable Paie, Comptable |
| UC-27 | Consulter les tableaux de bord RH | DRH, Contrôleur de gestion, Direction |

### 2.4 API REST exhaustive (résumé)

Détail intégral dans `ANALYSE_KSM_HRM.md § 2.3`. Synthèse :

- 15 controllers REST
- ~110 endpoints
- Pas de pagination (tous retournent `List<T>` complets)
- Pas d'endpoint `DELETE` ni `PATCH`
- Authentification multi-couches (cf. § 1.3)

### 2.5 Modèle de domaine (machines à états)

**Employee** : `ACTIVE ⇄ ON_LEAVE`, `ACTIVE ⇄ SUSPENDED`, `* → TERMINATED`
**Contract** : `ACTIVE`, `EXPIRED`, `TERMINATED`, `RENEWED` (un seul ACTIVE par employé)
**LeaveRequest** : `PENDING → APPROVED | REJECTED | CANCELLED`, `APPROVED → CANCELLED` (avant date début)
**LoanAdvance** : `PENDING → IN_REPAYMENT → FULLY_REPAID`, `PENDING → REJECTED`
**PayrollRun** : `CALCULATED → VALIDATED → PAID`
**ExpenseReport** : `DRAFT → SUBMITTED → APPROVED → REIMBURSED`, `SUBMITTED → REJECTED`
**MissionOrder** : `DRAFT → APPROVED → IN_PROGRESS → COMPLETED`, `* → CANCELLED`
**Application** : `NEW → SHORTLISTED → INTERVIEWING → OFFERED → HIRED`, `* → REJECTED`
**Interview** : `PENDING → PASS | FAIL` (résultat)
**OnboardingTask** : `PENDING → IN_PROGRESS → COMPLETED`
**JobOffer** : `DRAFT → PUBLISHED → CLOSED`
**Training** : `PLANNED → IN_PROGRESS → COMPLETED`, `* → CANCELLED`
**TrainingEnrollment** : `ENROLLED → COMPLETED | CANCELLED`
**PerformanceReview** : `DRAFT → SUBMITTED → ACKNOWLEDGED → FINALIZED`
**Timesheet** : `DRAFT → SUBMITTED → VALIDATED`
**SocialDeclaration** : `DRAFT → GENERATED → SUBMITTED → ACKNOWLEDGED`

> **Important** : ces transitions sont **strictes** côté serveur (lèvent
> `IllegalStateException`). Le frontend doit refléter visuellement l'état
> courant et **désactiver / masquer les boutons d'actions non valides**.

### 2.6 Événements outbox (à savoir pour notifications/UX temps réel)

Émis par HRM (consommables si l'on implémente plus tard des notifications SSE
depuis le BFF) :

`EMPLOYEE_CREATED`, `EMPLOYEE_UPDATED`, `CONTRACT_CREATED`, `LEAVE_SUBMITTED`,
`LEAVE_APPROVED`, `LEAVE_BALANCE_UPDATED`, `LOAN_APPROVED`, `PAYROLL_CALCULATED`,
`PAYROLL_VALIDATED`, `PAYMENT_ORDER_CREATED`.

> Pour la v1, **ne pas câbler Kafka**. Si tu veux des notifications, le BFF peut
> proposer un endpoint SSE qui poll régulièrement KSM ou attend une intégration
> Kafka future. Reste pragmatique : pour la v1, le rafraîchissement TanStack
> Query suffit.

### 2.7 Règles métier critiques

- **Calcul de paie (Cameroun)** : CNPS 4,2% (plafond 750k), IRPP barème
  progressif (10/15/25/35%, exo < 62k, abattement 30%/400k), CAC 10% IRPP,
  CFC 1% brut, RAV barème forfaitaire, TDL barème annuel/12.
- **Acquisition congés** : 1,5 j/mois + bonus ancienneté `floor(années/5)×2/an`
  + bonus enfants `<6 ans` (mères) `enfants × 2/an` plafonné 10 j.
- **Unicité paie** : un seul `PayrollRun` par `(periode, organizationId, agencyId)`.
- **Unicité employé** : un seul `Employee` par `actorId` dans un tenant.
- **Plafond prêt** : cumul des prêts actifs ne dépasse pas un plafond
  paramétrable (settings-core).

---

## 3. Architecture technique cible du frontend + BFF

### 3.1 Stack technique IMPOSÉE

| Couche | Choix | Justification |
|---|---|---|
| Framework | **Next.js 15** (App Router) | SSR + RSC + Route Handlers natifs, BFF intégré |
| Langage | **TypeScript strict** | Sécurité de typage, DX, partage de types client/serveur |
| Style | **TailwindCSS v4** + design tokens HR Core | Cohérence avec les designs |
| UI primitives | **shadcn/ui** (Radix) | Accessibles, customisables, légères |
| Server state | **TanStack Query v5** | Cache, invalidation, mutations |
| Forms | **React Hook Form** + **Zod** | Validation partagée, UX fluide |
| Client state | **Zustand** | Léger, simple, persistance localStorage si besoin |
| Charts | **Recharts** (ou Tremor si pertinent) | Bonne intégration React |
| Icons | **lucide-react** | Conforme aux designs |
| Date | **date-fns** | Léger, fonctionnel |
| i18n | **next-intl** (FR défaut + EN secondaire) | App Router compatible, ICU MessageFormat, type-safe |
| HTTP | **fetch natif** + wrapper typé | Pas de dépendance, supporte Edge Runtime |
| Sessions | **iron-session** ou cookies signés natifs Next.js | Cookies httpOnly sécurisés |
| Tests | **Vitest** + **Testing Library** + **Playwright** | Unit/intégration/e2e |
| Lint | **ESLint** strict + **Prettier** | Qualité code |
| Logger | **pino** | Performant côté serveur |

> Tu peux suggérer des alternatives si tu juges pertinent (ex: Hono pour le BFF,
> Tremor pour les charts, etc.), mais **propose-les dans ton plan, n'implémente
> rien sans l'avis de l'utilisateur**.

### 3.2 Architecture du BFF dans Next.js

Le BFF est **intégré au projet Next.js**. Pas de serveur séparé. Les Server
Components et Route Handlers appellent directement le module `src/server/`.

```
src/
├── app/
│   ├── (auth)/                       # Routes publiques
│   │   ├── login/page.tsx
│   │   ├── mfa/page.tsx
│   │   ├── change-password/page.tsx
│   │   └── select-context/page.tsx
│   ├── (app)/                        # Routes protégées (layout avec sidebar)
│   │   ├── layout.tsx                # Shell + auth guard + permissions
│   │   ├── dashboard/page.tsx        # Dashboard par rôle (server-rendered)
│   │   ├── employees/
│   │   │   ├── page.tsx              # Liste
│   │   │   ├── new/page.tsx          # Création
│   │   │   ├── import/page.tsx       # Import CSV en masse
│   │   │   └── [employeeId]/
│   │   │       ├── page.tsx          # Fiche 360°
│   │   │       ├── edit/page.tsx
│   │   │       ├── contracts/page.tsx
│   │   │       ├── dependents/page.tsx
│   │   │       ├── leave-balances/page.tsx
│   │   │       └── skills/page.tsx
│   │   ├── contracts/
│   │   ├── recruitment/
│   │   ├── leaves/
│   │   ├── timesheets/
│   │   ├── mission-orders/
│   │   ├── payroll/
│   │   ├── loans/
│   │   ├── expenses/
│   │   ├── reviews/
│   │   ├── trainings/
│   │   ├── training-budgets/
│   │   ├── medical/
│   │   ├── declarations/
│   │   ├── skills/
│   │   ├── analytics/
│   │   ├── notifications/page.tsx
│   │   └── settings/
│   │   └── admin/                    # Pages SuperAdmin
│   │       ├── organization/
│   │       │   ├── page.tsx
│   │       │   └── legal/page.tsx
│   │       ├── users/
│   │       │   ├── page.tsx
│   │       │   ├── new/page.tsx
│   │       │   └── [actorId]/roles/page.tsx
│   │       ├── services/page.tsx
│   │       ├── settings/
│   │       │   ├── payroll/page.tsx
│   │       │   ├── sequences/page.tsx
│   │       │   └── holidays/page.tsx
│   │       ├── audit/page.tsx
│   │       └── onboarding/page.tsx   # Stepper première configuration
│   ├── api/                          # Route Handlers (BFF endpoints)
│   │   ├── auth/
│   │   │   ├── login/route.ts
│   │   │   ├── logout/route.ts
│   │   │   ├── change-password/route.ts
│   │   │   ├── select-context/route.ts
│   │   │   └── me/route.ts
│   │   ├── hrm/
│   │   │   └── [...path]/route.ts    # Proxy générique optionnel
│   │   ├── files/
│   │   │   └── upload/route.ts
│   │   ├── documents/
│   │   │   ├── payslip/[entryId]/route.ts
│   │   │   ├── contract/[contractId]/route.ts
│   │   │   ├── mission-order/[orderId]/route.ts
│   │   │   └── declaration/[declarationId]/route.ts
│   │   ├── notifications/route.ts
│   │   └── (endpoints typés par ressource — préféré)
│   ├── error.tsx
│   ├── not-found.tsx
│   ├── globals.css                   # Tokens TailwindCSS + theme HR Core
│   └── layout.tsx
├── server/                           # 🔒 Server-only (import 'server-only')
│   ├── ksm/
│   │   ├── client.ts                 # Wrapper fetch typé, injection headers
│   │   ├── errors.ts                 # Mapping ApiResponse → HttpError
│   │   ├── pagination.ts             # Slice côté serveur
│   │   ├── jwt.ts                    # Decode JWT (jose)
│   │   └── modules/
│   │       ├── auth.ts
│   │       ├── employees.ts          # 1 module par ressource HRM
│   │       ├── leaves.ts
│   │       ├── payroll.ts
│   │       ├── organization.ts       # Pour SuperAdmin (organization-core)
│   │       ├── actors.ts             # Pour création d'acteurs (actor-core)
│   │       ├── roles.ts              # Pour attribution de rôles (roles-core)
│   │       ├── settings.ts           # Pour paramètres (settings-core)
│   │       ├── files.ts              # Pour uploads (file-core)
│   │       └── ... (15+ modules)
│   ├── documents/
│   │   ├── templates/
│   │   │   ├── payslip.tsx           # Bulletin de paie PDF
│   │   │   ├── employment-contract.tsx
│   │   │   ├── mission-order.tsx
│   │   │   ├── work-certificate.tsx
│   │   │   └── social-declaration.tsx
│   │   ├── fonts/
│   │   │   ├── Inter-Regular.ttf
│   │   │   └── Inter-Bold.ttf
│   │   ├── generator.ts
│   │   └── organization-header.tsx   # Composant en-tête entreprise réutilisable
│   ├── email/
│   │   ├── sender.ts                 # Service d'envoi (Resend / Nodemailer)
│   │   └── templates/
│   │       ├── welcome.tsx           # Mail de bienvenue nouvel employé
│   │       └── password-reset.tsx
│   ├── session.ts                    # Sessions httpOnly + CSRF
│   ├── permissions.ts                # Vérifie hrm:*:* perms + data scoping
│   └── tenant-context.ts             # Lit cookie workspace, fournit X-Organization-Id
├── lib/                              # Code partagé client + serveur
│   ├── types/
│   │   ├── api.ts                    # ApiResponse<T>, HttpError, PageResult
│   │   ├── auth.ts                   # Session, User, Permission
│   │   └── hrm/
│   │       ├── employee.ts
│   │       ├── payroll.ts
│   │       └── ... (1 fichier par sous-domaine)
│   ├── validation/
│   │   └── hrm/
│   │       ├── employee.schema.ts    # Schemas Zod
│   │       ├── leave.schema.ts
│   │       └── ...
│   ├── api-client.ts                 # fetch côté navigateur → /api/*
│   ├── query-keys.ts                 # Clés TanStack Query
│   ├── permissions.ts                # useCan(), hasPermission() (client)
│   ├── error-codes.ts               # Mapping errorCode → i18n + action
│   ├── hrm-permissions.ts           # Référentiel statique des permissions HRM
│   ├── holidays-cm.ts               # Jours fériés Cameroun
│   └── format.ts                     # XAF formatter, dates, nombres
├── components/
│   ├── ui/                           # shadcn/ui generated
│   ├── shell/
│   │   ├── Sidebar.tsx               # Filtre par perms
│   │   ├── Topbar.tsx                # Search + notif + user chip + locale switcher
│   │   ├── PageHeader.tsx            # avec breadcrumb + badge UC
│   │   ├── WorkspaceSwitcher.tsx     # Sélecteur d'organisation
│   │   ├── LocaleSwitcher.tsx        # FR / EN
│   │   └── ProtectedRoute.tsx
│   ├── kpi/                          # KpiCard, KpiGrid, Sparkline
│   ├── tables/                       # DataTable, FilterBar
│   ├── forms/                        # FormField, FormDialog, FileUpload
│   ├── workflows/                    # WorkflowTimeline, StatusBadge
│   ├── employees/                    # EmployeeCard, EmployeePicker
│   ├── payroll/                      # PayslipViewer, PayrollSummary
│   ├── leaves/                       # LeavePlanner, LeaveBalanceBar
│   ├── recruitment/                  # KanbanBoard pipeline
│   └── ... (1 dossier par sous-domaine)
├── hooks/
│   ├── useSession.ts
│   ├── useWorkspace.ts
│   ├── useCan.ts                     # Permissions
│   ├── usePagination.ts
│   ├── useDebounce.ts
│   ├── useApiError.ts               # Gestion erreurs métier
│   ├── useSessionTimeout.ts         # Détection inactivité
│   └── modules/
│       ├── useEmployees.ts           # CRUD + listings
│       ├── useLeaves.ts
│       └── ...
├── stores/
│   ├── workspace.ts                  # Org/agence courante (Zustand)
│   ├── ui.ts                         # Sidebar collapsed, theme
│   └── notifications.ts
├── middleware.ts                     # Auth guard, redirects, locale
├── instrumentation.ts                # Pino logger init
└── env.ts                            # Validation des env vars (Zod)
```

### 3.3 Variables d'environnement

```env
# KSM Backend
KSM_BASE_URL=http://localhost:8080
KSM_CLIENT_ID=hrm-frontend                  # Identifie le BFF
KSM_API_KEY=<secret-rotated-via-ksm>        # Server-only

# Session
SESSION_COOKIE_NAME=hrm_session
SESSION_SECRET=<32-bytes-min>               # iron-session
SESSION_TTL_SECONDS=3600

# Email (pour les mails de bienvenue)
EMAIL_PROVIDER=resend                        # resend | smtp
RESEND_API_KEY=<secret>                      # si resend
SMTP_HOST=smtp.example.com                   # si smtp
SMTP_PORT=587
SMTP_USER=<user>
SMTP_PASS=<secret>
EMAIL_FROM=noreply@hrcore.example.com

# Public
NEXT_PUBLIC_APP_NAME="HR Core"
NEXT_PUBLIC_APP_URL=http://localhost:3000
NEXT_PUBLIC_DEFAULT_CURRENCY=XAF
NEXT_PUBLIC_DEFAULT_LOCALE=fr
NEXT_PUBLIC_SUPPORTED_LOCALES=fr,en

# Logs
LOG_LEVEL=info
NODE_ENV=development
```

**Aucune** variable `NEXT_PUBLIC_*` ne doit contenir de secret. Le client navigateur ne doit jamais voir `KSM_API_KEY` ou le JWT.

### 3.4 Flux d'authentification (à implémenter)

```
1. GET /login                                  → Page de login (formulaire email + password)
2. POST /api/auth/login                        → Route Handler Next.js
   ↳ Côté BFF :
     POST {KSM}/api/auth/login                 (Body: {principal, password})
       avec Headers: X-Client-Id, X-Api-Key
   ↳ Réponse KSM :
     - Si MFA: { mfaToken, channel } → renvoie 202 vers le client, redirige vers /mfa
     - Sinon: { sessionToken (JWT), user, contexts[] }
   ↳ Le BFF :
     - Vérifie si forcePasswordChange = true → redirige vers /change-password
     - Stocke le JWT dans un cookie httpOnly signé (iron-session)
     - Pose un cookie csrfToken
     - Renvoie au client { user, redirectTo }
3. Si plusieurs contexts → /select-context
   POST /api/auth/select-context { contextId, organizationId }
   ↳ Le BFF appelle KSM /api/auth/select-context
   ↳ Met à jour la session avec le contexte choisi
4. Toutes les requêtes suivantes utilisent le cookie session
5. POST /api/auth/logout → vide la session
```

### 3.5 Headers injectés par le BFF lors d'un appel à KSM

```ts
async function callKsm(path: string, init?: RequestInit) {
  const session = await getSession();
  const workspace = await getWorkspace(session);
  return fetch(`${process.env.KSM_BASE_URL}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      'X-Client-Id': process.env.KSM_CLIENT_ID!,
      'X-Api-Key': process.env.KSM_API_KEY!,
      'X-Tenant-Id': session.tenantId,
      'X-Organization-Id': workspace.organizationId,
      ...(workspace.agencyId ? { 'X-Agency-Id': workspace.agencyId } : {}),
      Authorization: `Bearer ${session.accessToken}`,
      'X-Request-Id': crypto.randomUUID(),
      ...init?.headers,
    },
    cache: 'no-store',
  });
}
```

### 3.6 Cache et invalidation

- **Server Components** : `cache()` React pour dédupliquer les appels dans
  un même render.
- **TanStack Query** : `queryKey = ['hrm', resource, ...filters]`.
- **Invalidation** : après une mutation, invalider toutes les clés
  `['hrm', resource, ...]` impactées.
- **Server actions ou fetch** : `revalidateTag` ou `revalidatePath` après mutation.

### 3.7 Sécurité

- Cookies `Secure`, `HttpOnly`, `SameSite=Lax`, signés
- CSRF token sur toutes les mutations (header `X-CSRF-Token`)
- Validation Zod **côté serveur ET client**
- Pas de `dangerouslySetInnerHTML` sans `DOMPurify`
- CSP headers via `next.config.js`
- Rate limiting sur `/api/auth/login` (`@upstash/ratelimit` ou middleware maison)
- Logs : pas de PII dans les logs (masquer email, CNPS, comptes bancaires)
- File upload : passe par `file-core` (`POST {KSM}/api/files`), valider taille/type **côté serveur** avant relai

---

## 4. Design system HR Core (à respecter, avec liberté d'amélioration)

> **Les designs présents dans `fichiers_design_du_module_hrm/`** sont la
> direction visuelle souhaitée. Ils ne sont **pas obligatoires à reproduire à
> l'identique** : tu peux et tu **dois** améliorer l'UX/UI si tu vois mieux,
> tant que tu restes cohérent avec la charte chaud/orange premium et le ton
> "SaaS sérieux multi-tenant africain".

### 4.1 Palette (tokens Tailwind)

Source : `fichiers_design_du_module_hrm/Propostion de theme/styles.css`. À porter
en TailwindCSS v4 (`@theme inline { --color-orange-500: #F26B0F; ... }`) :

```
Brand orange:
  50  #FFF1E6   100 #FFDCC2   200 #FFC299   300 #FFA366
  400 #FB8533   500 #F26B0F   600 #DC560A   700 #B0420A   800 #7A2D08
Backgrounds:
  bg     #F8F5EF
  bg-2   #F1ECE0
  bg-soft #F2EBDB
Dark surfaces (sidebar/hero):
  dark   #110D08    dark-2 #1C1610    dark-3 #2B2218
Lines:
  line   #E8DEC6    line-soft #F0E8D4   line-strong #D5C8AA
Ink (textes):
  ink   #0F0B05   ink-2 #2E281E   ink-3 #6B6253   ink-4 #9A9283   ink-5 #C4BCAE
Statuts:
  green-500 #10B981   blue-500 #3B82F6   red-500 #EF4444
  amber-500 #F59E0B   violet-500 #8B5CF6  teal-500 #14B8A6
Radii: xs 8 / sm 12 / md 16 / lg 20 / xl 28 / 2xl 36
```

Gradients signatures :
- `--grad-orange: linear-gradient(135deg, #FB8533 0%, #F26B0F 50%, #DC560A 100%)`
- Background ambiant : voir `styles.css` (radial-gradients chauds)
- Grain texture optionnelle (subtile)

### 4.2 Typo

- **Inter** (corps, 400/500/600/700)
- **Inter Tight** (titres `h-display 34/800`, `h1 28/700`, `h2 20/700`, `h3 16/600`)
- **JetBrains Mono** (badges UC, refs techniques `tabular-nums`)

### 4.3 Composants signatures (à recréer)

- **Sidebar** : fond crème, sections labellées (`PILOTAGE`, `PERSONNEL`, etc.),
  nav-items avec icône + label + badge optionnel, item actif en gradient orange.
- **Topbar** : recherche universelle ⌘K, info, notifications (avec dot), chip
  utilisateur (nom + rôle + avatar initiales), sélecteur de langue (FR/EN).
- **PageHeader** : breadcrumb (avec badge UC bleu/orange en mono), titre
  display, sous-titre, actions à droite.
- **KPI Cards** : 4 cards de stats hautes en gradient (orange, dark, amber,
  violet) avec icône + label small caps + valeur grosse + delta.
- **Workflow timeline** : stepper visuel pour les workflows (PayrollRun,
  ExpenseReport, Application).
- **DataTable** : colonnes triables, pagination locale (BFF la gère), filters
  chip, recherche.
- **Status badges** : code couleur cohérent (active/approved → vert, pending →
  amber, rejected/terminated → rouge, draft → gris).
- **WorkspaceSwitcher** : dropdown dans le topbar pour changer d'organisation.

### 4.4 Sidebar par rôle (filtrage par permissions)

Sections candidates :
```
PILOTAGE       Tableau de bord | Analytics RH
PERSONNEL      Employés | Contrats | Compétences | Recrutement
ACTIVITÉ       Temps & Présences | Congés | Ordres de mission
RÉMUNÉRATION   Paie | Avances & Prêts | Notes de frais
DÉVELOPPEMENT  Évaluations | Formations | Budget formation
CONFORMITÉ     Suivi médical | Déclarations
SYSTÈME        Paramètres
ADMINISTRATION Entreprise | Utilisateurs | Rôles | Services | Audit (SuperAdmin uniquement)
```

Chaque rôle voit un sous-ensemble. Exemple pour un **Employé** :
```
Tableau de bord
Mes congés
Avances & Prêts
Formations
Bulletins de paie
Notes de frais
Temps & Présences
Suivi médical
Mon profil
```

### 4.5 Liberté UX/UI

Tu peux et tu dois améliorer :
- **Fiche employé 360°** (absente des designs, indispensable Admin RH).
- **Planning de congés équipe** en vue calendaire (manager).
- **Workflow visuel** (timeline) sur tous les écrans de détail des entités à
  cycle de vie.
- **Sélecteur d'organisation** explicite dans le header.
- **Dark mode** optionnel.
- **Mobile responsive** : la sidebar collapse, layout fluide.
- **a11y** : focus visible, ARIA labels, navigation clavier, contraste AA min.
- **Sélecteur de langue** (FR / EN) visible dans le topbar ou le menu profil.

---

## 5. Inventaire complet des pages à construire

> Liste exhaustive de **~85 pages**, organisée par sous-domaine. Statut `existant`
> = déjà esquissé dans les designs ; `nouveau` = manquant à inventer.
> Chaque page mentionne le rôle dominant, l'UC couvert, et les endpoints.

### 5.1 Authentification & onboarding (priorité bootstrap)

| Page | Statut | UC | Rôles | Endpoints BFF → KSM |
|---|---|---|---|---|
| `/login` | nouveau | — | Public | `POST /api/auth/identify`, `POST /api/auth/login` |
| `/mfa` | nouveau | — | Public | `POST /api/auth/login/mfa/confirm` |
| `/change-password` | nouveau | — | Authentifié (première connexion) | `PUT /api/auth/change-password` |
| `/select-context` | nouveau | — | Authentifié multi-org | `POST /api/auth/select-context` |
| `/account/profile` | partiel | — | Tous | `GET /api/users/me`, `PUT /api/users/me/onboarding` |
| `/account/security` | nouveau | — | Tous | (sessions, MFA toggle si dispo) |

### 5.2 Tableaux de bord (par rôle)

| Page | Statut | UC | Rôles |
|---|---|---|---|
| `/dashboard` (rendu différent selon rôle) | esquissé par rôle | UC-27 | Tous |
| `/analytics` (KPIs détaillés + drilldown) | partiel | UC-27 | DRH, Contrôleur |
| `/analytics/snapshots` | nouveau | UC-27 | DRH, Contrôleur |
| `/analytics/snapshots/{id}` | nouveau | UC-27 | DRH, Contrôleur |
| `/analytics/snapshots/new` | nouveau | UC-27 | Admin RH, DRH |
| `/notifications` | nouveau | — | Tous |

### 5.3 Personnel — Employés

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/employees` | esquissé | UC-01..05 | `GET /employees` |
| `/employees/new` | esquissé | UC-01 | `POST /employees` (orchestré multi-core) |
| `/employees/import` | nouveau | UC-01 | Import CSV en masse |
| `/employees/{id}` (fiche 360°) | partiel | UC-01..05 | `GET /employees/{id}` + agrégats |
| `/employees/{id}/edit` | nouveau | UC-02 | `PUT /employees/{id}` |
| `/employees/{id}/terminate` (modal) | nouveau | UC-03 | `PUT /employees/{id}/terminate` |
| `/employees/{id}/suspend` (modal) | nouveau | UC-03 | `PUT /employees/{id}/suspend` |
| `/employees/{id}/reactivate` (modal) | nouveau | UC-03 | `PUT /employees/{id}/reactivate` |
| `/employees/{id}/contracts` | esquissé | UC-04 | `GET /employees/{id}/contracts` |
| `/employees/{id}/contracts/new` | esquissé | UC-04 | `POST /employees/{id}/contracts` |
| `/employees/{id}/dependents` | esquissé | UC-05 | `GET /employees/{id}/dependents` |
| `/employees/{id}/dependents/new` | esquissé | UC-05 | `POST /employees/{id}/dependents` |
| `/employees/{id}/leave-balances` | nouveau | UC-09/10 | `GET /employees/{id}/leave-balances?annee=` |
| `/employees/{id}/skills` | nouveau | UC-24 | `GET /skills/employees/{id}/skills` |

### 5.4 Personnel — Contrats

| Page | Statut | Endpoints |
|---|---|---|
| `/contracts` (vue globale) | esquissé | (agrégat) liste tous les employés + leurs contrats |
| `/contracts/expiring` | nouveau | (filtre côté BFF) |

### 5.5 Personnel — Compétences

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/skills` (référentiel) | nouveau | UC-24 | `GET /skills` |
| `/skills/new` | nouveau | UC-24 | `POST /skills` |
| `/skills/{id}` (détail + qui possède) | nouveau | UC-24 | `GET /skills/{id}/employees` (à demander) |

### 5.6 Recrutement

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/recruitment` (dashboard pipeline) | esquissé | UC-16..18 | `GET /job-offers?organizationId=` + agrégats |
| `/recruitment/offers` | esquissé | UC-16 | `GET /job-offers` |
| `/recruitment/offers/new` | esquissé | UC-16 | `POST /job-offers` |
| `/recruitment/offers/{id}` | partiel | UC-16 | `GET /job-offers/{id}` + actions publish/close |
| `/recruitment/applications` (kanban) | esquissé | UC-17 | `GET /job-offers/{id}/applications` (par job) |
| `/recruitment/applications/{id}` | nouveau | UC-17 | `GET /applications/{id}` |
| `/recruitment/applications/{id}/interviews` | nouveau | UC-17 | `POST /interviews`, `GET /applications/{id}/interviews` |
| `/recruitment/onboarding/{employeeId}` | esquissé | UC-18 | `GET /onboarding-tasks/employee/{id}` |
| `/recruitment/onboarding-tasks` (agrégée) | nouveau | UC-18 | (agrégat côté BFF) |

### 5.7 Activité — Congés

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/leaves` | esquissé | UC-09/10 | `GET /leaves/pending` (admin/manager) |
| `/leaves/my` | esquissé | UC-09 | `GET /leaves/employee/{myEmployeeId}` |
| `/leaves/new` | esquissé | UC-09 | `POST /leaves` |
| `/leaves/{id}` | nouveau | UC-10 | `GET /leaves/{id}` + approve/reject/cancel |
| `/leaves/pending` | esquissé | UC-10 | `GET /leaves/pending?organizationId=&agencyId=` |
| `/leaves/planning` (calendaire) | nouveau | UC-10 | agrégat côté BFF |
| `/leaves/balances` | nouveau | UC-09 | agrégat |

### 5.8 Activité — Timesheets

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/timesheets/my` | esquissé | UC-19 | `GET /timesheets/employee/{id}?periode=` |
| `/timesheets/new` | partiel | UC-19 | `POST /timesheets` |
| `/timesheets` (admin) | partiel | UC-20 | `GET /timesheets?organizationId=&periode=` |
| `/timesheets/manager` (validation) | esquissé | UC-20 | (filtrage côté BFF par équipe) |
| `/timesheets/{id}` | nouveau | UC-19/20 | `GET /timesheets/{id}` + submit/validate |

### 5.9 Activité — Ordres de mission

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/mission-orders` | esquissé | UC-21 | `GET /mission-orders?employeeId=` (par employé) |
| `/mission-orders/new` | esquissé | UC-21 | `POST /mission-orders` |
| `/mission-orders/{id}` | nouveau | UC-21 | `GET /mission-orders/{id}` + actions approve/start/complete/cancel |

### 5.10 Rémunération — Paie

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/payroll` (dashboard cycles) | esquissé | UC-06/07/08 | `GET /payroll/runs?organizationId=` |
| `/payroll/runs/new` (formulaire de lancement) | esquissé | UC-06 | `POST /payroll/run` |
| `/payroll/runs/{id}` | esquissé | UC-06/07 | `GET /payroll/runs/{id}` |
| `/payroll/runs/{id}/entries` | esquissé | UC-06/07 | `GET /payroll/runs/{id}/entries` |
| `/payroll/runs/{id}/validate` (modal/page de validation) | esquissé | UC-07 | `PUT /payroll/runs/{id}/validate` |
| `/payroll/entries/{id}` (détail + payslip lines) | partiel | UC-08 | `GET /payroll/entries/{id}/payslip` |
| `/payroll/entries/{id}/payslip` (impression PDF) | partiel | UC-08 | BFF génère le PDF |
| `/payroll/my` (employé : mes bulletins) | esquissé | — | filtrage côté BFF par employee.actorId == userActorId |
| `/payroll/payment-orders` (suivi paiements) | nouveau | UC-08 | (filtrage côté BFF) |

### 5.11 Rémunération — Avances & Prêts

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/loans` (admin) | partiel | UC-11/12 | (agrégat) |
| `/loans/my` | partiel | UC-11 | `GET /loan-advances/employee/{id}` |
| `/loans/new` | esquissé | UC-11 | `POST /loan-advances` |
| `/loans/{id}` | nouveau | UC-12 | `GET /loan-advances/{id}` + approve/reject |
| `/loans/approve` (file d'attente) | nouveau | UC-12 | (filtrage côté BFF) |

### 5.12 Rémunération — Notes de frais

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/expenses` | esquissé | UC-22 | `GET /expenses?employeeId=` |
| `/expenses/my` | esquissé | UC-22 | filtré côté BFF |
| `/expenses/new` | esquissé | UC-22 | `POST /expenses` |
| `/expenses/{id}` (détail + lignes) | partiel | UC-22 | `GET /expenses/{id}` + `GET /expenses/{id}/lines` + submit |
| `/expenses/{id}/lines/new` | nouveau | UC-22 | `POST /expenses/{id}/lines` |
| `/expenses/approve` (file comptable) | esquissé | UC-22 | (filtrage côté BFF) |

### 5.13 Développement — Évaluations

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/reviews` (manager view) | partiel | UC-15 | `GET /reviews?organizationId=&periode=` |
| `/reviews/new` | esquissé | UC-15 | `POST /reviews` |
| `/reviews/{id}` (workflow + objectifs) | esquissé | UC-15 | `GET /reviews/{id}` + `GET /reviews/{id}/objectives` |
| `/reviews/{id}/objectives/new` | nouveau | UC-15 | `POST /reviews/{id}/objectives` |
| `/reviews/{id}/submit` | partiel | UC-15 | `PUT /reviews/{id}/submit` |
| `/reviews/employee/{id}` (historique perso) | nouveau | UC-15 | `GET /reviews/employee/{id}` |

### 5.14 Développement — Formations

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/trainings` (catalogue) | esquissé | UC-13/14 | `GET /trainings?organizationId=` |
| `/trainings/new` | nouveau | UC-13 | `POST /trainings` |
| `/trainings/{id}` (détail + inscriptions) | nouveau | UC-13/14 | `GET /trainings/{id}` + `GET /trainings/{id}/enrollments` |
| `/trainings/{id}/enroll` (admin enroll bulk) | nouveau | UC-14 | `POST /trainings/{id}/enrollments` |
| `/trainings/my` (employé) | nouveau | UC-14 | `GET /trainings/enrollments/employee/{id}` |

### 5.15 Développement — Budget formation

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/training-budgets` | esquissé | UC-25 | `GET /training-budgets?organizationId=&annee=` |
| `/training-budgets/new` | nouveau | UC-25 | `POST /training-budgets` |
| `/training-budgets/{id}` | partiel | UC-25 | `GET /training-budgets/{id}` + engage/realiser |

### 5.16 Conformité — Suivi médical

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/medical` (dashboard) | esquissé | UC-23 | (agrégat) |
| `/medical/visits` | esquissé | UC-23 | (filtrage côté BFF) |
| `/medical/visits/new` | nouveau | UC-23 | `POST /medical/visits` |
| `/medical/visits/{id}` | nouveau | UC-23 | `GET /medical/visits/{id}` |
| `/medical/employees/{id}/visits` | nouveau | UC-23 | `GET /medical/employees/{id}/visits` |
| `/medical/certificates` | esquissé | UC-23 | (filtrage côté BFF) |
| `/medical/certificates/new` | nouveau | UC-23 | `POST /medical/certificates` |
| `/medical/certificates/{id}` | nouveau | UC-23 | `GET /medical/certificates/{id}` |

### 5.17 Conformité — Déclarations sociales

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/declarations` | esquissé | UC-26 | `GET /declarations?orgId=` |
| `/declarations/new` | nouveau | UC-26 | `POST /declarations` |
| `/declarations/{id}` (workflow) | nouveau | UC-26 | `GET /declarations/{id}` + generate/submit/acknowledge |

### 5.18 Système — Paramètres

| Page | Statut | Endpoints |
|---|---|---|
| `/settings` (index) | nouveau | — |
| `/settings/organization` (workspace courant) | nouveau | (lecture session) |
| `/settings/profile` | nouveau | `GET /api/users/me` |

### 5.19 Administration — SuperAdmin

| Page | Statut | Description |
|---|---|---|
| `/admin/onboarding` | nouveau | Stepper première configuration (6 étapes) |
| `/admin/organization` | nouveau | Infos générales de l'organisation |
| `/admin/organization/legal` | nouveau | Infos légales + logo |
| `/admin/organization/agencies` | nouveau | Gestion des agences |
| `/admin/users` | nouveau | Liste des utilisateurs / acteurs |
| `/admin/users/new` | nouveau | Création utilisateur + attribution rôle |
| `/admin/users/{actorId}/roles` | nouveau | Gestion rôles d'un utilisateur |
| `/admin/services` | nouveau | Abonnements aux services (HRM activé ?) |
| `/admin/settings/payroll` | nouveau | Paramètres de paie (taux CNPS, IRPP, etc.) |
| `/admin/settings/sequences` | nouveau | Séquences matricules |
| `/admin/settings/holidays` | nouveau | Jours fériés du Cameroun |
| `/admin/audit` | nouveau | Logs d'audit (lecture seule) |

### 5.20 Mes pages d'employé (self-service complet)

| Page | Statut |
|---|---|
| `/dashboard` (vue employé) | esquissé |
| `/account/profile` | partiel |
| `/leaves/my` + `/leaves/new` | esquissé |
| `/loans/my` + `/loans/new` | esquissé |
| `/expenses/my` + `/expenses/new` | esquissé |
| `/timesheets/my` + saisie | esquissé |
| `/payroll/my` | esquissé |
| `/trainings/my` + inscription | partiel |
| `/medical/my` (mes visites/certificats) | nouveau |
| `/reviews/my` (mes évaluations) | nouveau |

---

## 6. Plan d'implémentation — Phase par phase (DOIT être proposé d'abord)

> **Tu DOIS soumettre ce plan à validation AVANT de coder.** Voici la structure
> attendue, à affiner par toi. Présente-le sous forme de phases numérotées
> avec dépendances, durées estimées (en heures-IA) et livrables clairs.

### Squelette à proposer (à raffiner)

#### Phase 0 — Bootstrap & infrastructure (avant tout)
- Initialisation projet Next.js 15 + TypeScript strict + ESLint/Prettier
- TailwindCSS v4 + design tokens HR Core
- shadcn/ui init + composants de base (Button, Input, Dialog, Tabs, Sheet)
- **i18n `next-intl` configuré dès cette phase : locales `fr` (défaut) + `en`,
  middleware de routing, namespaces JSON, hook `useTranslations`, sélecteur
  de langue dans le topbar, mappers pour les enums backend (statuses, types)**
- Pino logger
- Config env vars + validation Zod
- Couche `server/ksm/client.ts` (wrapper fetch)
- `server/session.ts` (iron-session)
- Middleware Next.js (auth guard + locale)
- Layout root + composants shell (Sidebar, Topbar, PageHeader, WorkspaceSwitcher, LocaleSwitcher)
- Charte tokens : KpiCard, StatusBadge, WorkflowTimeline, DataTable, FileUpload
- Système de gestion d'erreurs (error-codes.ts + useApiError)
- Utilitaires de formatage (format.ts : XAF entier, dates, nombres)
- Tests setup (Vitest + Playwright)
- README dédié frontend (instructions, .env.example)

#### Phase 1 — Authentification + workspace + SuperAdmin onboarding
- Page `/login` (form Zod, identifiant = email uniquement)
- Page `/mfa` (form code)
- Page `/change-password` (première connexion)
- Page `/select-context` (sélection org)
- Route Handlers : `POST /api/auth/login`, `mfa/confirm`, `change-password`,
  `select-context`, `logout`, `GET /api/auth/me`
- Stores Zustand : `workspace`, `session`
- Hook `useSession`, `useCan`, `useSessionTimeout`
- Pages erreur (`/403`, `/404`, error boundary)
- Pages SuperAdmin : `/admin/onboarding` (stepper 6 étapes),
  `/admin/organization/legal`, `/admin/users/new`, `/admin/users/{id}/roles`,
  `/admin/services`, `/admin/settings/payroll`
- **Critère de sortie** : un SuperAdmin peut se logger, configurer son organisation,
  créer le premier Admin RH avec son rôle, et l'Admin RH peut se connecter
  avec le mot de passe temporaire reçu par email et le changer.

#### Phase 2 — UC-01..05 : Personnel complet (tous rôles concernés)
- Module Employés complet :
  - SuperAdmin + Admin RH : création (stepper orchestré multi-core), édition
  - Admin RH : fiche 360°, terminate/suspend/reactivate, import CSV
  - Employé : mon profil (lecture + édition limitée)
- Module Contrats (Admin RH + Employé lecture)
- Module Dépendants (Admin RH + Employé lecture)
- Module Compétences (DRH + Manager)
- Upload de fichiers (photos, pièces d'identité)
- BFF modules `employees.ts`, `contracts.ts`, `dependents.ts`, `files.ts`
- Hooks TanStack Query + invalidations
- Tests unitaires Zod + tests e2e Playwright
- **Critère de sortie** : workflow complet de création d'employé bout en bout
  (SuperAdmin crée → employé reçoit mail → se connecte → change mot de passe →
  voit son profil). Admin RH peut gérer le personnel complet.

#### Phase 3 — UC-06..08 : Cycle de paie complet (tous rôles)
- Admin RH : lancement du calcul (`/payroll/runs/new`)
- Responsable Paie : consultation bulletins, variables de temps
- Comptable/DAF : validation paie, ordres de paiement
- Employé : consultation de ses bulletins, téléchargement PDF
- Contrôleur RH : lecture analytics paie
- Génération PDF des bulletins de paie (template camerounais)
- **Critère de sortie** : Admin RH lance → Comptable valide → Employé télécharge
  son bulletin PDF avec les infos légales de l'entreprise.

#### Phase 4 — UC-09..10 : Congés (tous rôles)
- Employé : demande, annulation, consultation solde
- Manager : approbation/rejet, planning calendaire équipe
- Admin RH : vue globale, soldes, alertes
- **Critère de sortie** : Employé soumet → Manager approuve/rejette → solde
  mis à jour → planning équipe visible.

#### Phase 5 — UC-11..12 : Avances & Prêts (tous rôles)
- Employé : demande d'avance
- Comptable/DAF : file d'approbation, détail
- Admin RH : vue globale
- **Critère de sortie** : workflow PENDING → IN_REPAYMENT bout en bout.

#### Phase 6 — UC-19..21 : Temps, présences & missions (tous rôles)
- Employé : saisie timesheet, soumission
- Manager : validation timesheets, création/approbation ordres de mission
- Responsable Paie : consolidation des temps
- **Critère de sortie** : workflows timesheet et mission complets.

#### Phase 7 — UC-22 : Notes de frais (tous rôles)
- Employé : soumission note + ajout lignes + justificatifs (upload)
- Comptable/DAF : approbation, remboursement
- **Critère de sortie** : workflow DRAFT → REIMBURSED bout en bout.

#### Phase 8 — UC-13..15 : Développement RH (tous rôles)
- DRH : formations (planification, catalogue), budget, évaluations
- Employé : inscription formation, acknowledge évaluation
- Manager : évaluations, objectifs
- **Critère de sortie** : workflows formation et évaluation complets.

#### Phase 9 — UC-16..18 : Recrutement & Onboarding (tous rôles)
- Recruteur : offres, pipeline candidatures (kanban), entretiens
- Manager : participation entretiens
- Admin RH : onboarding tasks, conversion candidat → employé
- Upload CV et lettres de motivation
- **Critère de sortie** : workflow recrutement NEW → HIRED → Employee créé.

#### Phase 10 — UC-23 : Suivi médical (tous rôles)
- Médecin du travail : visites, certificats
- Admin RH : alertes échéances
- Employé : consultation de son dossier médical
- **Critère de sortie** : enregistrement visite + certificat + alertes.

#### Phase 11 — UC-24..25 : Compétences + Budget formation
- DRH + Manager : référentiel compétences, mapping employé↔skill
- Contrôleur : budget formation engagement vs réalisation

#### Phase 12 — UC-26 : Déclarations sociales
- Responsable Paie : création, génération, soumission
- Comptable : validation
- Génération PDF/CSV des déclarations CNPS/DIPE

#### Phase 13 — UC-27 : Tableaux de bord & Analytics (finition)
- Dashboards enrichis par rôle avec données réelles (cf. §26)
- KPI snapshots, comparaisons

#### Phase 14 — Cross-rôle, polish, accessibility, dark mode
- Notifications in-app (cf. §25)
- Recherche globale (⌘K)
- Export Excel (CSV) des listings
- Mobile responsive complet
- Dark mode
- a11y audit (Playwright axe)
- Gestion session timeout (cf. §22)

#### Phase 15 — Tests, monitoring, déploiement
- Couverture e2e Playwright (golden paths par rôle)
- Tests intégration BFF (Vitest)
- Dockerfile multi-stage
- Pipeline CI (lint + typecheck + tests)
- Pre-prod deployment guide

### Règles transversales pour chaque phase

1. **Avant de coder une page** : vérifier que l'endpoint backend existe et
   répond. Si un endpoint manque ou nécessite un ajustement (cf. § 7), **STOP,
   demander à l'utilisateur**.
2. **Tester en données réelles** : utiliser un environnement KSM démarré
   (`docker-compose.application.yml`) + seeders. Aucun mock.
3. **Chaque phase doit être livrable** : code mergeable, tests verts, démo
   fonctionnelle.
4. **Permissions** : à chaque page, vérifier la perm requise, masquer/désactiver
   les actions hors scope.
5. **Multi-org** : changer d'organisation dans le `WorkspaceSwitcher` doit
   recharger les données du nouveau workspace.

---

## 7. Ajustements éventuels du backend `hrm-core` à demander

> Tu peux modifier `RT-comops-hrm-core` UNIQUEMENT si l'utilisateur le valide.
> Avant de modifier, **demande explicitement** en listant les changements
> proposés et leur justification.

### Liste prioritaire (à confirmer avec l'utilisateur avant Phase 2)

1. **Pagination + tri + recherche** sur les endpoints listing (volumétrie).
   Proposer un format type `?page=0&size=20&sort=field,asc&q=...`.

2. **Endpoint d'agrégation pour les dashboards** :
   `GET /api/v1/hrm/dashboards/summary?organizationId=&role=` qui renvoie les
   4-5 KPIs majeurs (effectif actif, masse salariale, congés en attente,
   alertes conformité, etc.) en un seul appel.

3. **Endpoints d'alertes** :
   `GET /api/v1/hrm/alerts?organizationId=` agrégant : CDD expirants
   (< 30 jours), visites médicales expirées, budget formation > 90%,
   timesheets non validés, etc.

4. **Endpoints d'export** :
   - `GET /api/v1/hrm/payroll/entries/{id}/payslip.pdf` (bulletin PDF)
   - À défaut, le BFF peut générer (avec `@react-pdf/renderer`).

5. **Endpoints "endpoint missing" déjà détectés** :
   - `GET /api/v1/hrm/loan-advances/employee/{employeeId}/active`
   - `GET /api/v1/hrm/skills/{skillId}/employees`
   - `GET /api/v1/hrm/contracts` (vue globale)
   - `GET /api/v1/hrm/contracts/expiring?organizationId=&days=30`

6. **Cohérence query params** : `orgId` (RhKpi, SocialDeclaration) vs
   `organizationId` (autres). Standardiser sur `organizationId`.

7. **`PayrollRun.status = DRAFT`** : présent dans le PDF mais absent du code
   Java. Soit l'ajouter, soit aligner le PDF. **Demander à l'utilisateur**.

8. **Champ `managerId`** sur Employee : nécessaire pour filtrer les données
   par périmètre Manager (cf. §24).

### Comment demander

Lorsque tu détectes un besoin de modification backend pendant l'implémentation :

```markdown
## Demande de modification backend HRM (Phase X)

**Contexte** : implémentation de la page Y, j'ai besoin de Z.

**Besoin précis** : [description]

**Proposition** : [endpoint, méthode, body, réponse]

**Alternative côté BFF** : [si possible, avec ses limites]

**Validation utilisateur ?**
```

---

## 8. Conventions de code

### 8.1 Naming

- Fichiers : `kebab-case.tsx` / `.ts`
- Composants : `PascalCase`
- Hooks : `useCamelCase`
- Functions / variables : `camelCase`
- Types / interfaces : `PascalCase`
- Constantes : `UPPER_SNAKE_CASE`
- Server-only files : préfixe `import 'server-only';`

### 8.2 Imports

Ordre :
1. External (react, next, …)
2. Aliases internes (`@/components`, `@/lib`, `@/server`)
3. Relatifs (`./`, `../`)

### 8.3 Types HRM

Centralisés dans `src/lib/types/hrm/`. Préférer des types `enum`-like en
union de strings :

```ts
export type EmployeeStatus = 'ACTIVE' | 'ON_LEAVE' | 'SUSPENDED' | 'TERMINATED';
export type ContractType = 'CDD' | 'CDI' | 'STAGE' | 'INTERIM';
// …
```

Si un endpoint OpenAPI est disponible (`KSM/iwm-openapi.json` à la racine de KSM),
on peut générer les types avec `openapi-typescript`. **À proposer à l'utilisateur**.

### 8.4 Internationalisation (i18n) — FR par défaut + EN secondaire

L'application doit être **bilingue dès la Phase 0** :

- **Langue par défaut** : `fr` (français) — cible Cameroun + Afrique francophone
- **Langue secondaire** : `en` (anglais) — disponible immédiatement
- **Stack** : `next-intl` (App Router, Server Components, type-safe)

**Organisation des messages** :

```
src/i18n/
├── config.ts
├── request.ts
└── messages/
    ├── fr/
    │   ├── common.json
    │   ├── auth.json
    │   ├── employees.json
    │   ├── payroll.json
    │   ├── errors.json
    │   └── ...
    └── en/
        └── ...
```

**Règles** :
- Clés en `camelCase.dot.notation`
- Pluralisation native ICU MessageFormat
- Aucune chaîne en dur dans le code
- Enums backend mappés vers clés i18n (`statuses.employee.ACTIVE`)

### 8.5 Tests

- **Vitest** : schemas Zod, fonctions utilitaires, hooks
- **Playwright** : golden paths e2e par rôle
- **No mocks** : environnement KSM réel avec seeders

### 8.6 Commits

- Convention `conventional commits` : `feat(employees): add 360 profile page`
- Branche dédiée : `claude/setup-ksm-multitenant-WwlUJ`
- Push après chaque phase validée

---

## 9. Ce que tu dois LIVRER dans ton premier message (BEFORE coding)

### Livrable attendu : un **plan détaillé** structuré comme suit

1. **Confirmation que tu as lu** : `ANALYSE_KSM_HRM.md`, `hrm_conception.pdf`,
   les controllers `RT-comops-hrm-core/.../adapter/in/web/`, les designs sous
   `fichiers_design_du_module_hrm/`. Liste 3-5 points qui te paraissent les
   plus importants.

2. **Validation/discussion du plan d'architecture** :
   - Confirmer ou contester la stack (Next.js 15 / TailwindCSS v4 / etc.).
   - Confirmer le BFF intégré (Route Handlers) vs alternative.
   - Confirmer les conventions de nommage et la structure de dossiers.

3. **Liste des questions ouvertes** que tu poses avant de coder :
   - Choix de la session (iron-session vs jose vs autre)
   - Choix de la génération des types depuis OpenAPI (`KSM/iwm-openapi.json`)
   - Tests e2e : KSM dans Docker en CI ?
   - Si des seeders backend sont disponibles ou à créer
   - Librairie de génération PDF (`@react-pdf/renderer` vs `pdf-lib` vs autre)
   - Service d'email (Resend vs Nodemailer vs autre)

4. **Plan détaillé phase par phase** (raffinement de § 6) :
   - Pour chaque phase : objectif, livrables, durée estimée, dépendances
   - Ordre proposé (peut différer de la liste § 6)

5. **Liste des modifications backend HRM proposées** (cf. § 7) :
   - Priorisées (P0/P1/P2)
   - Avec impact et alternative BFF

6. **Critères de validation** pour chaque phase :
   - Pages livrées
   - Endpoints intégrés
   - Tests passants
   - Démo possible

**Aucun code n'est attendu dans ce premier message.** Attends la validation
de l'utilisateur avant de démarrer Phase 0.

---

## 10. Ce que tu dois faire en boucle après validation du plan

À chaque phase :
1. **Annoncer** la phase qui commence et son périmètre exact.
2. **Coder** les pages/modules de la phase.
3. **Tester en données réelles** sur KSM (pas de mock).
4. **Demander à l'utilisateur** si une ambiguïté ou un besoin de modification
   backend apparaît.
5. **Commiter** des changements clairs (`feat(phase-2): admin RH module`).
6. **Pousser** sur la branche `claude/setup-ksm-multitenant-WwlUJ`.
7. **Annoncer** la fin de la phase avec la liste des pages livrées,
   des endpoints utilisés et de ce qui reste à faire.
8. **Attendre** la validation explicite avant la phase suivante.

---

## 11. Ressources & références

### Dans le dépôt
- `ANALYSE_KSM_HRM.md` (analyse complète, à lire intégralement)
- `KSM/ARCHITECTURE.md` (architecture KSM)
- `KSM/README.md` (instructions runtime)
- `KSM/iwm-openapi.json` (OpenAPI complet, peut servir à générer des types)
- `KSM/RT-comops-hrm-core/` (le module backend à intégrer)
- `hrm_conception.pdf` + version texte `(/tmp/hrm_conception.txt)` si dispo
- `conception/diagrams/*.puml` (architecture, MLD, cas d'usage, séquences)
- `fichiers_design_du_module_hrm/Propostion de theme/` (design system, JSX, CSS)
- `fichiers_design_du_module_hrm/design par role/` (designs par rôle)

### Pour tester KSM en local
- `KSM/docker-compose.application.yml` (stack complète)
- `KSM/docker-compose.infrastructure.yml` (infrastructure seule)
- `KSM/scripts/start-full-stack.sh` (script de bootstrap)

---

## 12. Résumé exécutif (TL;DR)

- Tu construis un **frontend Next.js 15 + BFF intégré** pour le module HRM de la
  plateforme SaaS multi-tenant **KSM**.
- Tu utilises **Next.js App Router**, **Server Components** pour les pages,
  **Route Handlers** pour le BFF, **TanStack Query** pour le state serveur,
  **shadcn/ui + TailwindCSS v4** avec le design system **HR Core**
  (orange chaud sur fond crème).
- Tu implémentes **10 rôles** (SuperAdmin, Admin RH, DRH, Manager, Employé,
  Comptable/DAF, Responsable Paie, Recruteur, Médecin du travail, Contrôleur RH)
  couvrant **27 use cases** (UC-01 à UC-27) répartis sur **~85 pages**.
- Tu **développes par cas d'utilisation** : chaque UC est implémenté pour
  **tous ses rôles** simultanément avant de passer au suivant.
- Tu **respectes le RBAC** `hrm:*:*`, le **multi-tenant** (X-Organization-Id),
  et la **sécurité serveur** (X-Client-Id + X-Api-Key jamais exposés au navigateur).
- Tu **n'utilises aucun mock**. Tout passe par le vrai KSM.
- Tu **ne touches AUCUN autre module KSM** que `hrm-core` (et uniquement avec
  validation de l'utilisateur).
- Tu **produis d'abord un plan détaillé**, puis implémentes **phase par phase,
  UC par UC**.
- Tu **commites et pousses** sur la branche `claude/setup-ksm-multitenant-WwlUJ`.

---

## 13. Règle de gestion des modifications backend

### Avant de toucher un fichier backend, pose cette question :
"Modification backend demandée : [endpoint/champ manquant].
Voulez-vous que je (A) modifie hrm-core, (B) gère côté BFF,
ou (C) diffère ?"
→ N'agis qu'après la réponse.

### Workflow session mixte backend + frontend :
1. Identifie l'écart entre ce que le backend retourne et ce que le design attend
2. Documente l'écart dans API_CONTRACT.md section "Ajustements nécessaires"
3. Demande validation
4. Si validé : modifie UNIQUEMENT le controller/service ciblé dans hrm-core
5. Mets à jour API_CONTRACT.md avec le nouveau contrat
6. Reviens en frontend avec le contrat à jour

---

## 14. Règle de développement par cas d'utilisation (UC-driven development)

### Principe fondamental
Chaque use case (UC-01 à UC-27) doit être implémenté dans sa **globalité
transversale** : toutes les pages de tous les rôles qui y participent sont
codées ensemble, dans la même phase, avant de passer au use case suivant.

**Tu ne codes JAMAIS une page en isolation de son contexte métier complet.**

### Exemple concret — UC-06/07/08 : Cycle de paie

Quand tu implémentes le module Paie, tu livres simultanément :

| Rôle | Pages à livrer | Actions disponibles |
|---|---|---|
| **Admin RH** | `/payroll/runs/new`, `/payroll` (liste) | Lancer le calcul, voir tous les cycles |
| **Responsable Paie** | `/payroll/runs/{id}`, `/payroll/runs/{id}/entries` | Consulter bulletins, variables de temps |
| **Comptable / DAF** | `/payroll/runs/{id}/validate`, `/payroll/payment-orders` | Valider, rejeter, voir ordres de paiement |
| **Employé** | `/payroll/my`, `/payroll/entries/{id}/payslip` | Consulter ses bulletins, télécharger PDF |
| **Contrôleur RH** | `/payroll` (lecture seule, analytics) | Analyser masse salariale |

→ **Toutes ces pages sont livrées ensemble** à la fin de la phase "Paie".
→ Le workflow complet est testable bout en bout :
   Admin RH lance → Responsable Paie vérifie → Comptable valide → Employé
   consulte son bulletin.

### Règle de priorisation des rôles par UC

Pour chaque use case, identifie :
1. **Le rôle initiateur** (qui déclenche l'action)
2. **Les rôles approbateurs** (qui valident / rejettent)
3. **Les rôles lecteurs** (qui consultent le résultat)
4. **Le rôle propriétaire des données** (l'employé concerné)

Tous ces rôles doivent avoir leur page fonctionnelle avant de clôturer le UC.

### Référence — matrice UC × Rôles

| UC | Initiateur | Approbateur(s) | Lecteurs | Propriétaire |
|---|---|---|---|---|
| UC-01 Créer employé | SuperAdmin / Admin RH | — | DRH | Employé |
| UC-06 Lancer la paie | Admin RH | — | Resp. Paie, Contrôleur | — |
| UC-07 Valider la paie | — | Comptable/DAF | Resp. Paie, Admin RH | — |
| UC-08 Ordres de paiement | — | Comptable/DAF (auto) | Contrôleur, DAF | Employé (reçoit) |
| UC-09 Demande de congé | Employé | — | Admin RH | Employé |
| UC-10 Approbation congé | — | Manager | Admin RH, Employé | Employé |
| UC-11 Demande avance | Employé | — | Comptable | Employé |
| UC-12 Approbation avance | — | Comptable/DAF | Admin RH | Employé |
| UC-13 Planifier formation | DRH | — | Manager, Employé | — |
| UC-14 Inscription formation | Employé | DRH (enrollment) | DRH | Employé |
| UC-15 Évaluation | Manager / DRH | Employé (acknowledge) | DRH, Contrôleur | Employé |
| UC-16 Offre d'emploi | Recruteur / DRH | — | Public | — |
| UC-17 Gestion candidatures | Recruteur | Manager (interview) | DRH | Candidat |
| UC-18 Onboarding | Recruteur / Admin RH | — | DRH, Manager | Employé |
| UC-19 Saisie temps | Employé | — | Manager, Resp. Paie | Employé |
| UC-20 Validation temps | — | Manager / Resp. Paie | Admin RH | Employé |
| UC-21 Ordre de mission | Manager / Admin RH | Manager | Employé, Contrôleur | Employé |
| UC-22 Note de frais | Employé | Comptable/DAF | Manager | Employé |
| UC-23 Visite médicale | Médecin du travail | — | Admin RH | Employé |
| UC-24 Compétences | DRH / Manager | — | Admin RH | Employé |
| UC-25 Budget formation | DRH | Contrôleur | DRH, DAF | — |
| UC-26 Déclarations sociales | Resp. Paie | Comptable | DAF, Admin RH | — |
| UC-27 Tableaux de bord | — | — | Tous (vue filtrée/rôle) | — |

### Critère de validation d'un UC

Un UC est considéré **terminé** si et seulement si :
- ✅ Toutes les pages de tous les rôles concernés sont implémentées
- ✅ Le workflow complet est testable bout en bout
- ✅ Les transitions d'état sont visuellement reflétées
- ✅ Les boutons d'actions hors état valide sont masqués / désactivés
- ✅ Les permissions RBAC sont vérifiées (`useCan('hrm:...:...')`)
- ✅ Les données affichées proviennent du vrai KSM (pas de mock)
- ✅ Les deux langues (FR / EN) fonctionnent sur toutes les pages du UC

### Vérification systématique avant de clôturer un UC

```
□ Qui initie ? → sa page de création/soumission est-elle livrée ?
□ Qui approuve ? → sa page de validation/rejet avec motif est-elle livrée ?
□ Qui consulte le résultat ? → sa page de lecture (liste + détail) est-elle livrée ?
□ L'employé concerné peut-il voir l'état de son dossier ? → page self-service livrée ?
□ Le workflow timeline reflète-t-il l'état courant de l'entité ?
```

---

#les pages du frontend doivent etre fideles au design foruni, tu vas lancer le projet du dossier DESIGN a la racine et la tu veras l'aspect du frontend a reproduire fidelement et de facon focntionnelle
#Tu prend module par module et dans l'ondre des dependances croissantes , tu commences par les modules qui ne dependent d'aucun autre, puis les modules qui dependent directement des modules precedents et ainsi de suite il serait par exemeple stupide de faire le module de paie ou de gestion des congés alors que le module de gestion des employées n'est pas encore fait ca n'a pas de sens
#Tu y vas module par module , par exemple la gestion de la paie est un module a part si tu dois le faire ce module, tu gere toutes les pages concernés et ceux pour tous les roles avant de passer a un autre module et a la fin de chaque module, ce dernier doit etre testable directement 

#pour les design des formulaire fichier ("forms.html") tu vas voir que a droite de chaque champs de formulaire il y un nom marqué il faut les ignorer je voulais juste faire un corespondance entre les colonnes en BD   et les champs du formaulire
## 15. Rôle SuperAdmin — Gestionnaire de la plateforme

### 15.1 Nature du rôle

Le **SuperAdmin** est le rôle de niveau `ORGANIZATION` le plus élevé côté
client. C'est la première personne à se connecter lors de l'onboarding d'une
nouvelle organisation sur KSM.

> Le SuperAdmin **n'est pas** un rôle SYSTEM (réservé à l'équipe KSM).
> C'est le **responsable administrateur de l'organisation cliente** —
> typiquement le DG, le DAF ou le DSI.

### 15.2 Responsabilités exclusives

| Responsabilité | Core KSM ciblé |
|---|---|
| Renseigner les informations légales de l'entreprise | `organization-core` |
| Configurer les abonnements aux services | `organization-core` |
| Créer les comptes des premiers responsables (Admin RH, etc.) | `actor-core` + `auth-core` + `roles-core` |
| Attribuer / révoquer les rôles | `roles-core` |
| Gérer les agences | `organization-core` |
| Configurer les paramètres de paie | `settings-core` |
| Configurer les jours fériés | `settings-core` |
| Consulter les logs d'audit | `kernel-core` |

### 15.3 Informations légales de l'entreprise

Ces données sont saisies **une fois** par le SuperAdmin et réutilisées dans
**tous les documents générés** :

```ts
type OrganizationLegalInfo = {
  raisonSociale: string;          // "MUFID UNION S.A."
  formeJuridique: string;         // "SA", "SARL", "SAS", etc.
  capitalSocial: number;          // En FCFA
  rccm: string;                   // Registre Commerce
  niu: string;                    // Numéro Identifiant Unique (fiscal)
  numCnps: string;                // Numéro employeur CNPS
  adresseSiege: string;
  ville: string;
  pays: string;
  telephone: string;
  email: string;
  siteWeb?: string;
  logo?: string;                  // URL (file-core)
  banque?: string;
  rib?: string;
  directeurGeneral?: string;      // Pour signature documents
  responsableRH?: string;
}
```

Utilisation dans les documents :
| Document | Champs utilisés |
|---|---|
| **Bulletin de paie** | Logo, raisonSociale, adresse, NIU, numCnps, nomDG |
| **Contrat de travail** | Tous les champs légaux |
| **Ordre de paiement** | raisonSociale, banque, RIB, NIU |
| **Déclaration CNPS** | numCnps, raisonSociale, adresse, NIU |
| **Ordre de mission** | Logo, raisonSociale, adresse, signature DG |

> **Règle BFF** : avant de générer tout document, le BFF appelle
> `GET {KSM}/api/organizations/{organizationId}` pour récupérer ces infos.

### 15.4 Flux d'onboarding SuperAdmin (première connexion)

```
1. Login → /select-context → redirigé vers /admin/onboarding
2. Étape 1 : Infos légales de l'organisation
3. Étape 2 : Upload du logo
4. Étape 3 : Créer les premiers responsables (Admin RH, DRH, etc.)
5. Étape 4 : Attribuer leurs rôles
6. Étape 5 : Configurer les paramètres de paie
7. Étape 6 : Vérifier que le service HRM est activé
8. → Redirection vers le dashboard principal
```

### 15.5 Contraintes techniques

- Les cores `organization-core`, `actor-core`, `roles-core`, `settings-core`
  sont appelés **en lecture et écriture** via des modules BFF dédiés
  (`src/server/ksm/modules/organization.ts`, etc.)
- **Ne pas modifier le code source** de ces cores (read-only)
- Le logo est uploadé via `file-core`
- Pour la génération de documents, le BFF appelle systématiquement :
  `const org = await organizationModule.getLegalInfo(organizationId);`

---

## 16. Workflow de création d'employé — Flux complet orchestré

### 16.1 Principe fondamental

La création d'un employé est une **opération orchestrée multi-core**.
**L'utilisateur remplit UN SEUL formulaire.** Le BFF dispatche les appels.

### 16.2 Flux technique complet

```
Étape 1 — Créer l'identité humaine (actor-core)
  POST {KSM}/api/actors
  Body: { firstName, lastName, email, phone, dateOfBirth, gender, ... }
  → Retourne: { actorId }

Étape 2 — Créer le compte utilisateur (auth-core)
  POST {KSM}/api/users
  Body: { actorId, email, temporaryPassword: <généré BFF>, forcePasswordChange: true }
  → Retourne: { userId }

Étape 3 — Créer l'employé HRM (hrm-core)
  POST {KSM}/api/v1/hrm/employees
  Body: { actorId, organizationId, agencyId, categorie, echelon, ... }
  → Retourne: { employeeId, matricule }

Étape 4 — Attribuer un rôle (roles-core) — si nécessaire
  POST {KSM}/api/roles/assignments
  → Rôle EMPLOYEE par défaut, rôles supérieurs par SuperAdmin uniquement

Étape 5 — Envoyer le mail de bienvenue
  Contient : nom organisation, matricule (informatif), email (identifiant de connexion),
  mot de passe temporaire, lien de connexion
```

### 16.3 Identifiant de connexion

**L'identifiant de connexion est l'email — et uniquement l'email.**
Le matricule est un identifiant RH interne utilisé dans les documents
officiels mais jamais pour l'authentification.

### 16.4 Modifications backend auto-implémentables

Claude Code peut implémenter dans `hrm-core` SANS demander validation :

| Modification | Core | Justification |
|---|---|---|
| Champ `forcePasswordChange` sur la réponse user | `auth-core` (si accessible) | Première connexion |
| Endpoint `PUT /api/users/me/password` | `auth-core` (si accessible) | Changement de mot de passe |
| Publication événement `EMPLOYEE_ONBOARDED` | `hrm-core` | Futur hook notification |

> Si `auth-core` est read-only, le BFF gère via le mécanisme d'onboarding existant.

### 16.5 Formulaire unique de création (UI)

Stepper multi-étapes :

```
Étape 1/4 — Identité
  Prénom*, Nom*, Email*, Téléphone, Date de naissance, Genre,
  Nationalité, Adresse, Photo (upload optionnel)

Étape 2/4 — Informations RH
  Organisation (pré-rempli), Agence, Département, Catégorie,
  Échelon, Date d'embauche*, Type de contrat, Durée (si CDD)

Étape 3/4 — Rémunération
  Salaire de base*, Mode de paiement, Banque + compte ou
  Opérateur + numéro Mobile Money, Numéro CNPS (optionnel)

Étape 4/4 — Rôle & Accès (visible UNIQUEMENT pour le SuperAdmin)
  Rôle à attribuer (checkboxes cumulables)

Récapitulatif → Bouton "Créer l'employé"
```

### 16.6 Différences SuperAdmin vs Admin RH

| Aspect | SuperAdmin | Admin RH |
|---|---|---|
| Peut créer | Tous (y compris responsables) | Employés ordinaires |
| Étape 4 (rôles) | Visible, tout rôle attribuable | Masquée (rôle EMPLOYEE par défaut) |

### 16.7 Première connexion de l'employé

```
1. Email de bienvenue :
   "Bienvenue chez [Raison Sociale].
    Votre matricule : EMP-2024-0042
    Connectez-vous avec votre email : jean.dupont@example.com
    Mot de passe temporaire : Xk9#mP2$
    → [Bouton : Se connecter]"

2. /login → email + mot de passe temporaire
   → BFF détecte forcePasswordChange = true
   → Redirection vers /change-password

3. /change-password :
   → Nouveau mot de passe (min 8 chars, 1 maj, 1 chiffre, 1 spécial)
   → Confirmation → Succès → /dashboard (vue Employé)

4. Connexions suivantes : login normal
```

### 16.8 Gestion du rollback en cas d'échec

```
Étapes 1-3 : critiques (l'employé doit exister)
Étapes 4-5 : "best effort" (l'employé est fonctionnel même en échec)

Si étape 5 échoue → warning "Employé créé. Mail non envoyé.
  Mot de passe temporaire : [affiché une fois]. Communiquez-le manuellement."
```

---

## 17. Gestion des fichiers et uploads

### 17.1 Architecture

Tous les fichiers binaires transitent par `file-core` (MinIO/S3).
Flux : `Browser → BFF (Route Handler) → file-core API → MinIO/S3`

### 17.2 Flux d'upload

```
1. L'utilisateur sélectionne un fichier
2. POST /api/files/upload (multipart/form-data)
3. Le BFF :
   a. Valide côté serveur (taille, MIME type)
   b. Relaie vers file-core
   c. Retourne { fileId, url, filename, mimeType, size }
4. Le client stocke le fileId dans le formulaire principal
```

### 17.3 Types de fichiers par catégorie

| Catégorie | Entité | MIME autorisés | Taille max |
|---|---|---|---|
| `PHOTO_PROFIL` | Employee, Actor | image/jpeg, image/png, image/webp | 5 MB |
| `LOGO_ENTREPRISE` | Organization | image/jpeg, image/png, image/svg+xml | 2 MB |
| `CV` | Application | application/pdf | 10 MB |
| `LETTRE_MOTIVATION` | Application | application/pdf | 5 MB |
| `CONTRAT_SIGNE` | Contract | application/pdf, image/jpeg, image/png | 10 MB |
| `JUSTIFICATIF_FRAIS` | ExpenseLine | application/pdf, image/jpeg, image/png | 5 MB |
| `CERTIFICAT_MEDICAL` | MedicalCertificate | application/pdf, image/jpeg, image/png | 5 MB |
| `PIECE_IDENTITE` | Employee | application/pdf, image/jpeg, image/png | 5 MB |
| `DOCUMENT_MISSION` | MissionOrder | application/pdf | 10 MB |

### 17.4 Composant réutilisable `FileUpload`

```
Props : category, entityType, entityId?, maxSize, accept, onUploadComplete,
        onError, multiple?
Fonctionnalités : drag & drop, preview image, barre de progression,
                  validation client-side, retry 1x
```

---

## 18. Gestion des erreurs métier — Mapping errorCode → UI

### 18.1 Principe

Le backend retourne des `errorCode` spécifiques. Le frontend mappe chaque
code vers un message traduit (FR/EN) et une action corrective.

### 18.2 Catalogue des errorCode

Créer `src/lib/error-codes.ts` avec les mappings :

| errorCode | Message FR | Action UI |
|---|---|---|
| `UNAUTHORIZED` | "Votre session a expiré. Veuillez vous reconnecter." | Redirect /login |
| `FORBIDDEN` | "Vous n'avez pas la permission d'effectuer cette action." | Toast |
| `ORGANIZATION_CONTEXT_REQUIRED` | "Veuillez sélectionner une organisation." | Redirect /select-context |
| `ORGANIZATION_SERVICE_NOT_SUBSCRIBED` | "Le module RH n'est pas activé pour cette organisation." | Toast |
| `DUPLICATE_EMPLOYEE` | "Un employé avec cette identité existe déjà." | Highlight champ actorId |
| `EMPLOYEE_NOT_FOUND` | "Employé introuvable." | Redirect /employees |
| `INSUFFICIENT_LEAVE_BALANCE` | "Solde de congés insuffisant." | Highlight champ leaveType |
| `PAYROLL_ALREADY_EXISTS` | "Un cycle de paie existe déjà pour cette période." | Toast warning |
| `ILLEGAL_STATE_TRANSITION` | "Cette action n'est plus disponible." | Toast + refresh page |
| `LOAN_CEILING_EXCEEDED` | "Le plafond de prêt est dépassé." | Toast |
| `ACTIVE_CONTRACT_EXISTS` | "Cet employé a déjà un contrat actif." | Toast warning |
| `VALIDATION_ERROR` | "Certains champs contiennent des erreurs." | Highlight champs |

### 18.3 Hook useApiError()

```ts
const { handleError } = useApiError();
// Dans mutation TanStack Query : onError: (error) => handleError(error)
// → affiche le bon toast traduit, redirige si nécessaire, surligne les champs
```

### 18.4 Erreurs inconnues

Si le `errorCode` n'est pas dans le catalogue :
- Afficher "Une erreur inattendue est survenue. Réessayez ou contactez le support."
- Logger l'errorCode inconnu côté BFF (pino)

---

## 19. Génération de documents (PDF)

### 19.1 Documents à générer

| Document | Déclencheur | Format |
|---|---|---|
| **Bulletin de paie** | Employé consulte / télécharge | PDF |
| **Contrat de travail** | Admin RH génère | PDF |
| **Ordre de mission** | Manager génère | PDF |
| **Attestation de travail** | Admin RH sur demande | PDF |
| **Déclaration CNPS / DIPE** | Responsable Paie génère | PDF ou CSV |

### 19.2 Architecture

Génération **côté BFF** (Node.js) avec `@react-pdf/renderer` :

```
GET /api/documents/payslip/{entryId}
→ BFF : récupère PayrollEntry + Employee + Organization legal info
→ Injecte dans le template PDF
→ Retourne stream PDF
```

### 19.3 Bulletin de paie camerounais — Structure réglementaire

```
┌─────────────────────────────────────────────────┐
│  [LOGO]  RAISON SOCIALE                         │
│  Forme juridique — Capital social               │
│  Adresse siège                                  │
│  NIU : xxxxxxx — N° CNPS Employeur : xxxxxxx   │
│  RCCM : xxxxxxx                                 │
├─────────────────────────────────────────────────┤
│  BULLETIN DE PAIE — Période : Janvier 2024      │
├───────────────────┬─────────────────────────────┤
│  EMPLOYÉ          │  CLASSIFICATION              │
│  Nom : DUPONT J.  │  Catégorie : 7              │
│  Matricule : E042 │  Échelon : B                │
│  N° CNPS : xxxxx  │  Département : Informatique │
│  Date embauche    │  Ancienneté : 3 ans         │
├───────────────────┴─────────────────────────────┤
│  GAINS                        Montant (FCFA)    │
│  Salaire de base              350 000           │
│  Prime d'ancienneté            17 500           │
│  Heures supplémentaires        25 000           │
│  Prime de transport             20 000          │
│  SALAIRE BRUT                 412 500           │
├─────────────────────────────────────────────────┤
│  RETENUES              Salarial    Patronal     │
│  CNPS PV (4.2%/8.4%)    17 325      34 650     │
│  CFC (1%/1.5%)           4 125       6 188     │
│  IRPP                   32 150          —       │
│  CAC (10% IRPP)          3 215          —       │
│  RAV                     2 500          —       │
│  TDL                       833          —       │
│  Avances sur salaire    50 000          —       │
│  TOTAL RETENUES        110 148                  │
├─────────────────────────────────────────────────┤
│  NET À PAYER                  302 352 FCFA      │
├─────────────────────────────────────────────────┤
│  Mode de paiement : Virement bancaire           │
│  Banque : BICEC — Compte : xxxxxxxxxx           │
├─────────────────────────────────────────────────┤
│  Le Directeur Général                           │
│  [Nom DG]                                       │
│  Date d'émission : 31/01/2024                   │
└─────────────────────────────────────────────────┘
```

### 19.4 Templates PDF

```
src/server/documents/
├── templates/
│   ├── payslip.tsx
│   ├── employment-contract.tsx
│   ├── mission-order.tsx
│   ├── work-certificate.tsx
│   └── social-declaration.tsx
├── fonts/
├── generator.ts
└── organization-header.tsx    (en-tête entreprise réutilisable)
```

### 19.5 Route Handlers

```
GET /api/documents/payslip/{entryId}
GET /api/documents/contract/{contractId}
GET /api/documents/mission-order/{orderId}
GET /api/documents/work-certificate/{empId}
GET /api/documents/declaration/{declarationId}
```

---

## 20. Cycle de vie complet de l'employé — Résiliation et désactivation

### 20.1 Flux de résiliation

```
1. Admin RH clique "Résilier" → Modal :
   - Date de fin effective*
   - Motif* (licenciement, démission, fin CDD, retraite, décès, abandon, accord mutuel)
   - Commentaire optionnel
   - Checkbox confirmation

2. Le BFF orchestre :
   a. PUT /employees/{id}/terminate
   b. Annuler les congés APPROVED futurs
   c. Désactiver le compte utilisateur (auth-core)
   d. Révoquer tous les rôles (roles-core)

3. Post-résiliation :
   - Status badge rouge "Résilié"
   - Toutes actions désactivées sauf "Consulter"
   - Données restent en lecture (archivage)
   - Disparaît des listings par défaut (filtre status != TERMINATED)
   - Bulletins restent téléchargeables
```

### 20.2 Suspension vs Résiliation

| | Suspension | Résiliation |
|---|---|---|
| Status | SUSPENDED | TERMINATED |
| Connexion | Bloquée temporairement | Bloquée définitivement |
| Rôles | Conservés mais inactifs | Révoqués |
| Réversible | Oui (reactivate) | Non |
| UI | Badge orange + bouton "Réactiver" | Badge rouge, aucune action |

---

## 21. Import en masse (CSV/Excel)

### 21.1 Flux d'import

```
1. /employees/import → Bouton "Télécharger le modèle CSV"
2. L'utilisateur remplit le CSV et le reupload
3. Le BFF parse + valide chaque ligne avec le schema Zod
4. Rapport de pré-validation :
   { totalLines: 200, valid: 187, errors: [{ line, field, error }] }
5. L'utilisateur confirme → création séquentielle (pas en parallèle)
6. Barre de progression en temps réel
7. Rapport final : { created: 185, failed: 2, failures: [...] }
8. Chaque employé créé reçoit son mail de bienvenue
```

### 21.2 Limitations v1

- CSV uniquement, maximum 500 lignes
- Création uniquement (pas de mise à jour en masse)
- Pas d'import de contrats ou dépendants

---

## 22. Gestion de session et expiration du JWT

### 22.1 Stratégie

```
Si auth-core fournit un refresh token :
  → Le BFF stocke accessToken + refreshToken en cookies httpOnly
  → À chaque appel, si accessToken expiré : refresh automatique
  → Si refreshToken expiré : redirect /login

Sinon :
  → Le BFF stocke uniquement l'accessToken
  → Si expiré : retourne 401 au client
  → Le client affiche modal "Session expirée"
  → SAUVEGARDE le state du formulaire en cours dans sessionStorage
  → Après reconnexion : restauration automatique du formulaire
```

### 22.2 Inactivité

- Avertissement à 25 min : "Votre session expire dans 5 minutes"
- Déconnexion automatique à 30 min si pas de clic
- Hook `useSessionTimeout()`

### 22.3 Intercepteur global

```
Chaque fetch vers /api/* :
  401 → flux d'expiration
  403 → toast "Permission refusée"
  5xx → toast "Erreur serveur, réessayez"
  Timeout réseau → toast "Connexion perdue"
```

---

## 23. Filtres et recherche sur les listings

### 23.1 Principe

Le backend retourne des listes complètes. Le BFF fait la **pagination, le tri
et le filtrage en mémoire** côté serveur.

### 23.2 Format standard

```
GET /api/hrm/employees?page=1&size=20&sort=nom,asc&search=dupont&status=ACTIVE
```

### 23.3 Filtres spécifiques par entité

| Entité | Filtres |
|---|---|
| **Employees** | status, agencyId, departmentCode, categorie, echelon, dateEmbauche (range), search (nom/prénom/matricule/email) |
| **Contracts** | status, type, expiresWithin (days), employeeId |
| **LeaveRequests** | status, type, employeeId, dateRange, agencyId |
| **PayrollRuns** | status, periode (YYYY-MM), agencyId |
| **LoanAdvances** | status, employeeId |
| **ExpenseReports** | status, employeeId, dateRange |
| **Timesheets** | status, periode, employeeId |
| **MissionOrders** | status, employeeId, dateRange |
| **Trainings** | status, organizationId |
| **Reviews** | status, periode, employeeId, reviewerId |
| **JobOffers** | status, organizationId |
| **Applications** | status, jobOfferId |
| **MedicalVisits** | employeeId, dateRange, type |
| **Declarations** | status, type, periode |

### 23.4 Composant DataTable générique

```
Props : columns, data, totalCount, filters, searchPlaceholder,
        onParamsChange, isLoading, emptyState
Fonctionnalités : chips de filtres, barre de recherche, colonnes triables,
                  pagination, sélection lignes, export CSV
```

### 23.5 Recherche globale (⌘K)

Recherche à travers toutes les entités, résultats groupés par catégorie,
chaque résultat cliquable → redirige vers la page de détail.

---

## 24. Périmètre de données par rôle (data scoping)

### 24.1 Problème

Le RBAC dit **si** tu peux accéder. Mais un Manager ne doit voir que
**les employés de son équipe**.

### 24.2 Règles de filtrage

| Rôle | Périmètre |
|---|---|
| **SuperAdmin** | Toute l'organisation + toutes les agences |
| **Admin RH** | Toute l'organisation (ou agence si scopé AGENCY) |
| **DRH** | Toute l'organisation |
| **Manager** | Uniquement les employés de son département / équipe |
| **Employé** | Uniquement ses propres données |
| **Comptable / DAF** | Toute l'organisation (données financières) |
| **Responsable Paie** | Toute l'organisation (données paie) |
| **Recruteur** | Toutes les offres et candidatures |
| **Médecin du travail** | Tous les dossiers médicaux |
| **Contrôleur RH** | Toute l'organisation (lecture seule) |

### 24.3 Champ managerId

Pour le filtrage Manager, l'entité Employee a besoin d'un champ `managerId`.
Modification backend autorisée si ce champ n'existe pas.

---

## 25. Notifications in-app

### 25.1 v1 — Polling

Le BFF construit les notifications en agrégeant les données (congés PENDING
pour managers, PayrollRun CALCULATED pour comptables, etc.).
Le client poll `/api/notifications` toutes les 60 secondes.

### 25.2 Événements par rôle

| Événement | Notifié | Message |
|---|---|---|
| Congé soumis | Manager | "{nom} a soumis une demande de congé" |
| Congé approuvé/rejeté | Employé | "Votre demande a été {statut}" |
| Paie calculée | Comptable | "Cycle de paie {periode} prêt pour validation" |
| Paie validée | Employé | "Votre bulletin de {periode} est disponible" |
| Avance soumise | Comptable | "{nom} a demandé une avance de {montant} FCFA" |
| Note de frais soumise | Comptable | "{nom} a soumis une note de frais" |
| Nouveau candidat | Recruteur | "Nouvelle candidature pour : {titre}" |
| Contrat expire bientôt | Admin RH | "CDD de {nom} expire dans {jours} jours" |
| Visite médicale à planifier | Admin RH, Médecin | "Visite de {nom} à planifier" |
| Timesheet à valider | Manager | "{count} timesheets en attente" |
| Employé créé | Admin RH | "Nouvel employé : {nom} — {matricule}" |

---

## 26. Tableaux de bord — KPIs précis par rôle

### 26.1 Dashboard Admin RH
| KPI | Calcul |
|---|---|
| Effectif actif | count employees ACTIVE |
| CDD expirant < 30j | count contrats CDD expirant sous 30 jours |
| Congés en attente | count leaves PENDING |
| Masse salariale du mois | sum netAPayer du dernier PayrollRun |

### 26.2 Dashboard Employé
| KPI | Calcul |
|---|---|
| Solde congés annuels | soldeRestant type=ANNUAL |
| Prochain congé | premier APPROVED futur |
| Dernier bulletin | dernier PayrollEntry PAID |
| Prêts en cours | sum soldeRestant prêts actifs |

### 26.3 Dashboard Manager
| KPI | Calcul |
|---|---|
| Taille équipe | count employés managerId = moi |
| Congés à valider | count leaves PENDING (mon équipe) |
| Timesheets à valider | count timesheets SUBMITTED (mon équipe) |
| Évaluations en cours | count reviews (mon équipe) |

### 26.4 Dashboard Comptable / DAF
| KPI | Calcul |
|---|---|
| Paies à valider | count PayrollRun CALCULATED |
| Avances à approuver | count loan-advances PENDING |
| Notes de frais à traiter | count expenses SUBMITTED |
| Total ordres de paiement du mois | agrégat BFF |

### 26.5 Dashboard DRH
| KPI | Calcul |
|---|---|
| Effectif total | count ACTIVE |
| Turnover 12 mois | TERMINATED 12 mois / effectif |
| Budget formation consommé | training-budgets année courante |
| Évaluations finalisées | count FINALIZED / total |

### 26.6 Dashboard Recruteur
| KPI | Calcul |
|---|---|
| Offres ouvertes | count job-offers PUBLISHED |
| Candidatures en cours | count applications (excl. HIRED/REJECTED) |
| Entretiens à venir | count interviews PENDING futurs |
| Onboarding en cours | count onboarding-tasks IN_PROGRESS |

### 26.7 Dashboard Médecin du travail
| KPI | Calcul |
|---|---|
| Visites ce mois | count visits mois courant |
| Certificats émis | count certificates mois courant |
| Visites en retard | count employés sans visite > 12 mois |

### 26.8 Dashboard Responsable Paie
| KPI | Calcul |
|---|---|
| Dernier cycle de paie | PayrollRun le plus récent |
| Déclarations en attente | count declarations DRAFT ou GENERATED |
| Timesheets non validés | count timesheets SUBMITTED |
| Masse salariale du mois | sum brut dernier PayrollRun |

### 26.9 Dashboard Contrôleur de gestion RH
| KPI | Calcul |
|---|---|
| Masse salariale mensuelle | agrégat PayrollRun |
| Ratio charges patronales | charges / masse brute |
| Coût moyen par employé | masse salariale / effectif |
| Évolution effectif (sparkline) | historique 12 mois |

---

## 27. Conventions monétaires et formatage

### 27.1 Devise

Devise par défaut : **Franc CFA (XAF)** zone CEMAC.

### 27.2 Règles d'affichage

```
- Le FCFA n'a PAS de centimes. Tous les montants sont des ENTIERS.
- Affichage : 350 000 FCFA (espace séparateur milliers)
- JAMAIS : 350 000,00 FCFA ← INTERDIT
```

### 27.3 Fonction utilitaire

```ts
export function formatMoney(amount: number, locale: string = 'fr'): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: 'XAF',
    maximumFractionDigits: 0,
    minimumFractionDigits: 0,
  }).format(amount);
}
```

### 27.4 Validation Zod

```ts
const moneySchema = z.number()
  .int('Les montants doivent être des nombres entiers')
  .nonnegative('Le montant ne peut pas être négatif');
```

### 27.5 Formatage des dates

```
FR : 15/01/2024 | 15 janvier 2024 | 15/01/2024 à 14h30
EN : 01/15/2024 | January 15, 2024
Périodes de paie : Janvier 2024 / January 2024
```

### 27.6 Nombres

```
Pourcentages : 4,2% (FR) / 4.2% (EN)
Jours de congé : 1,5 jour — la seule valeur décimale
Compteurs : 42 employés (pas de décimale)
```

---

## 28. Jours fériés et calendrier camerounais

### 28.1 Jours fériés légaux

| Date | Jour férié |
|---|---|
| 1er janvier | Jour de l'An |
| 11 février | Fête de la Jeunesse |
| 1er mai | Fête du Travail |
| 20 mai | Fête Nationale |
| 15 août | Assomption |
| 1er octobre | Jour de la Réunification |
| 25 décembre | Noël |
| Variable | Vendredi Saint |
| Variable | Lundi de Pâques |
| Variable | Ascension |
| Variable | Aïd el-Fitr (fin du Ramadan) |
| Variable | Aïd el-Kébir (Tabaski) |
| Variable | Mouloud (naissance du Prophète) |

> Les 3 fêtes musulmanes sont à dates variables (calendrier lunaire),
> fixées annuellement par décret.

### 28.2 Implémentation

Recommandé : configuration via `settings-core` par le SuperAdmin
(`/admin/settings/holidays`). Le calendrier de congés et les timesheets
les marquent visuellement.

### 28.3 Impact sur les calculs

- **Congés** : jours fériés NON décomptés du solde
- **Timesheets** : jours fériés pré-remplis "non travaillés"
- **Planning** : affichés distinctement (couleur différente des week-ends)

---

## 29. Hors scope v1 (à mentionner mais ne pas implémenter)

Les fonctionnalités suivantes sont identifiées mais **explicitement hors scope**
pour la v1. Ne pas les implémenter sauf demande explicite de l'utilisateur :

- Mode hors-ligne / PWA (cache offline)
- Audit trail côté UI (visualisation "qui a modifié quoi")
- Export données personnelles (RGPD)
- Webhooks / intégrations tierces (logiciels comptables, banques)
- Multi-devise (tout est en XAF pour la v1)
- WebSocket / SSE pour notifications temps réel (polling suffit en v1)
- Application mobile native
- Rapports personnalisables (drag & drop de KPIs)

---

## 30. Cohérence chronologique des dates — Validation transversale obligatoire

### 30.1 Règle fondamentale

**Dans TOUT formulaire contenant deux dates ou plus, la chronologie doit être
respectée et validée en temps réel.** Une date de fin ne peut JAMAIS être
antérieure à sa date de début. Une date d'effet ne peut JAMAIS être antérieure
à la date de création de l'entité parente. Cette règle s'applique à la fois
côté client (UX immédiate) et côté serveur (schema Zod dans le BFF).

### 30.2 Inventaire complet des paires de dates à valider

| Formulaire | Date début / référence | Date fin / dépendante | Règle |
|---|---|---|---|
| **Contrat de travail** | `dateDebut` | `dateFin` (si CDD) | `dateFin > dateDebut` |
| **Contrat CDD** | `dateDebut` | `dateFin` | `dateFin >= dateDebut + 1 jour` ET `dateFin <= dateDebut + 24 mois` (durée max CDD Cameroun) |
| **Demande de congé** | `dateDebut` | `dateFin` | `dateFin >= dateDebut` ET `dateDebut >= aujourd'hui` (pas de congé rétroactif) |
| **Ordre de mission** | `dateDepart` | `dateRetour` | `dateRetour >= dateDepart` |
| **Ordre de mission** | `dateDepart` | — | `dateDepart >= aujourd'hui` (sauf régularisation si admin) |
| **Formation** | `dateDebut` | `dateFin` | `dateFin >= dateDebut` |
| **Formation** | `dateLimiteInscription` | `dateDebut` | `dateLimiteInscription <= dateDebut` |
| **Évaluation** | `periodeDebut` | `periodeFin` | `periodeFin > periodeDebut` |
| **Note de frais (ligne)** | `dateFrais` | — | `dateFrais <= aujourd'hui` (pas de frais futurs) ET `dateFrais >= date de la mission ou du mois courant` |
| **Timesheet** | `periode` (mois) | — | `periode <= mois courant` (pas de saisie dans le futur) |
| **Visite médicale** | `dateVisite` | `prochainRdv` | `prochainRdv > dateVisite` |
| **Certificat médical** | `dateDebut` | `dateFin` | `dateFin >= dateDebut` |
| **Offre d'emploi** | `datePublication` | `dateCloture` | `dateCloture > datePublication` |
| **Entretien** | `dateEntretien` | — | `dateEntretien >= aujourd'hui` |
| **Cycle de paie** | `periode` (YYYY-MM) | — | `periode <= mois courant` (pas de paie future) |
| **Déclaration sociale** | `periodeDebut` | `periodeFin` | `periodeFin >= periodeDebut` |
| **Budget formation** | `annee` | — | `annee >= année courante` (pas de budget rétroactif sauf régularisation) |
| **Employé** | `dateEmbauche` | — | `dateEmbauche <= aujourd'hui + 30 jours` (embauche future max 30j) |
| **Employé** | `dateNaissance` | `dateEmbauche` | `dateEmbauche > dateNaissance + 16 ans` (âge minimum légal Cameroun) |
| **Résiliation employé** | `dateEmbauche` (existante) | `dateTermination` | `dateTermination >= dateEmbauche` |
| **Prêt / avance** | `dateOctroi` | `datePremiereEcheance` | `datePremiereEcheance > dateOctroi` |
| **Suspension employé** | — | `dateFinSuspension` (si définie) | `dateFinSuspension > aujourd'hui` |

### 30.3 Comportement UX attendu

```
1. VALIDATION EN TEMPS RÉEL (pas seulement au submit) :
   - Dès que l'utilisateur saisit ou modifie une date, valider immédiatement
     la cohérence avec les dates liées dans le même formulaire.
   - Afficher l'erreur SOUS LE CHAMP fautif en rouge, pas dans un toast.

2. CONTRAINTE DU DATE PICKER :
   - Quand une date de début est saisie, le date picker de la date de fin
     doit DÉSACTIVER (griser) toutes les dates antérieures à dateDebut.
   - Quand une date de fin est saisie en premier, le date picker de la date
     de début doit désactiver toutes les dates postérieures à dateFin.
   - Utiliser la prop `minDate` / `maxDate` du composant DatePicker.

3. RÉACTION AU CHANGEMENT :
   - Si l'utilisateur change la date de début APRÈS avoir saisi la date de fin,
     et que la date de fin devient invalide :
     → Vider automatiquement la date de fin
     → Afficher un message : "La date de fin a été réinitialisée car elle
       précédait la nouvelle date de début."

4. CALCULS DÉPENDANTS :
   - Quand les deux dates sont valides, calculer et afficher en temps réel :
     → Congés : "X jours ouvrables (hors week-ends et jours fériés)"
     → Contrats CDD : "Durée : X mois et Y jours"
     → Missions : "Durée : X jours"
     → Formations : "Durée : X jours"
```

### 30.4 Schema Zod — Helper réutilisable

Créer un helper dans `src/lib/validation/date-helpers.ts` :

```ts
import { z } from 'zod';
import { isBefore, isAfter, isEqual, differenceInYears, addDays } from 'date-fns';

/**
 * Crée un schema Zod pour une paire de dates avec chronologie obligatoire.
 * À utiliser dans les .refine() des schemas de formulaire.
 */
export function dateRangeRefinement(
  startField: string,
  endField: string,
  options?: {
    allowEqual?: boolean;         // défaut true (début == fin accepté)
    startMinDate?: Date;          // date minimum pour le début
    endMaxDate?: Date;            // date maximum pour la fin
    maxDurationDays?: number;     // durée max entre les deux dates
    errorMessageKey?: string;     // clé i18n pour le message d'erreur
  }
) {
  const { allowEqual = true, maxDurationDays, errorMessageKey } = options ?? {};

  return (data: Record<string, unknown>, ctx: z.RefinementCtx) => {
    const start = data[startField] as Date | undefined;
    const end = data[endField] as Date | undefined;

    if (!start || !end) return; // les champs requis gèrent le "vide"

    const isInvalid = allowEqual
      ? isBefore(end, start)
      : !isAfter(end, start);

    if (isInvalid) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: errorMessageKey ?? 'validation.dateRange.endBeforeStart',
        path: [endField],
      });
    }

    if (maxDurationDays) {
      const maxEnd = addDays(start, maxDurationDays);
      if (isAfter(end, maxEnd)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: 'validation.dateRange.durationExceeded',
          path: [endField],
        });
      }
    }
  };
}

/**
 * Vérifie l'âge minimum (ex : employé doit avoir ≥ 16 ans à l'embauche)
 */
export function minAgeRefinement(
  birthField: string,
  referenceField: string,
  minYears: number
) {
  return (data: Record<string, unknown>, ctx: z.RefinementCtx) => {
    const birth = data[birthField] as Date | undefined;
    const ref = data[referenceField] as Date | undefined;
    if (!birth || !ref) return;

    if (differenceInYears(ref, birth) < minYears) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: 'validation.minAge',
        path: [referenceField],
      });
    }
  };
}
```

### 30.5 Exemples d'utilisation dans les schemas

```ts
// Schema de création de congé
export const leaveRequestSchema = z.object({
  dateDebut: z.date().min(new Date(), { message: 'validation.leave.notInPast' }),
  dateFin: z.date(),
  type: z.enum(['ANNUAL', 'SICK', 'MATERNITY', ...]),
  motif: z.string().min(1),
}).superRefine((data, ctx) => {
  dateRangeRefinement('dateDebut', 'dateFin', {
    allowEqual: true,  // congé d'1 jour = début == fin
  })(data, ctx);
});

// Schema de création de contrat CDD
export const contractCddSchema = z.object({
  dateDebut: z.date(),
  dateFin: z.date(),
  type: z.literal('CDD'),
}).superRefine((data, ctx) => {
  dateRangeRefinement('dateDebut', 'dateFin', {
    allowEqual: false,
    maxDurationDays: 730,  // 24 mois max pour un CDD au Cameroun
    errorMessageKey: 'validation.contract.cddMaxDuration',
  })(data, ctx);
});

// Schema de création d'employé
export const employeeCreateSchema = z.object({
  dateNaissance: z.date(),
  dateEmbauche: z.date(),
  // ...
}).superRefine((data, ctx) => {
  minAgeRefinement('dateNaissance', 'dateEmbauche', 16)(data, ctx);
});
```

### 30.6 Messages i18n

```json
// src/i18n/messages/fr/validation.json
{
  "dateRange": {
    "endBeforeStart": "La date de fin ne peut pas être antérieure à la date de début.",
    "durationExceeded": "La durée dépasse la limite autorisée.",
    "startAfterEnd": "La date de début ne peut pas être postérieure à la date de fin.",
    "dateResetNotice": "La date de fin a été réinitialisée car elle précédait la nouvelle date de début."
  },
  "minAge": "L'âge minimum requis n'est pas atteint à la date indiquée.",
  "leave": {
    "notInPast": "La date de début du congé ne peut pas être dans le passé."
  },
  "contract": {
    "cddMaxDuration": "Un CDD ne peut pas dépasser 24 mois (législation camerounaise)."
  },
  "mission": {
    "departureInPast": "La date de départ ne peut pas être dans le passé."
  },
  "expense": {
    "futureDate": "Une dépense ne peut pas être datée dans le futur."
  },
  "timesheet": {
    "futurePeriod": "Impossible de saisir un timesheet pour un mois futur."
  },
  "medical": {
    "nextBeforeCurrent": "La date du prochain rendez-vous doit être postérieure à la visite actuelle."
  }
}
```

### 30.7 Composant DateRangePicker réutilisable

Créer un composant `DateRangePicker` dans `src/components/ui/date-range-picker.tsx`
qui encapsule la logique de contrainte :

```
Props :
  - startDate: Date | null
  - endDate: Date | null
  - onStartChange: (date: Date) => void
  - onEndChange: (date: Date | null) => void   // null = reset automatique
  - minStartDate?: Date                         // ex : aujourd'hui
  - maxEndDate?: Date                           // ex : +24 mois
  - allowEqual?: boolean                        // début == fin autorisé ?
  - showDuration?: boolean                      // afficher "X jours" calculé
  - durationUnit?: 'days' | 'months'            // unité du calcul affiché
  - excludeDates?: Date[]                       // jours fériés, week-ends
  - labels: { start: string, end: string }      // labels i18n

Comportement interne :
  - Le picker "fin" a minDate = startDate (ou startDate + 1 si !allowEqual)
  - Le picker "début" a maxDate = endDate (si endDate est déjà saisie)
  - Changement de début qui invalide fin → reset fin + message info
  - Si showDuration → affiche un badge "X jours ouvrables" ou "X mois Y jours"
    sous les pickers, en excluant les dates de excludeDates
```

### 30.8 Checklist pour chaque formulaire (à vérifier systématiquement)

Avant de livrer un formulaire contenant des dates, Claude Code vérifie :

```
□ Toutes les paires de dates ont une validation Zod .superRefine()
□ Le DatePicker contraint dynamiquement minDate/maxDate selon l'autre champ
□ Le changement d'une date invalide l'autre si nécessaire (reset + message)
□ Les messages d'erreur sont traduits FR + EN
□ Les calculs dépendants (durée, jours ouvrables) s'affichent en temps réel
□ La validation existe AUSSI côté BFF (pas seulement côté client)
□ Les dates impossibles sont testées (fin < début, passé, futur interdit)
```


les pages du frontend doivent etre fideles au design foruni, tu vas lancer le projet du dossier DESIGN a la racine et la tu veras l'aspect du frontend a reproduire fidelement et de facon focntionnelle
Tu prend module par module et dans l'ondre des dependances croissantes , tu commences par les modules qui ne dependent d'aucun autre, puis les modules qui dependent directement des modules precedents et ainsi de suite il serait par exemeple stupide de faire le module de paie ou de gestion des congés alors que le module de gestion des employées n'est pas encore fait ca n'a pas de sens
Tu y vas module par module , par exemple la gestion de la paie est un module a part si tu dois le faire ce module, tu gere toutes les pages concernés et ceux pour tous les roles avant de passer a un autre module et a la fin de chaque module, ce dernier doit etre testable directement 
---

**Bonne construction. Pose tes questions, propose ton plan, et attends le go.**
