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
- Next.js 15 (App Router) + React 19 + TypeScript strict
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
  des dépendances figées.
- Tu n'utilises **JAMAIS** de mocks, fake data, faux JSON, fake API ou UI
  statique déconnectée. Tout doit aller au vrai backend KSM, qui est
  fonctionnel. Pour tester avec des données, propose des seeders SQL/Liquibase
  côté backend, jamais en frontend.
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

9 rôles distincts, chacun avec ses permissions :

| Rôle | Permissions principales | Pages clés |
|---|---|---|
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
| UC-01 | Créer un employé (lié à un Actor existant) | Admin RH, Recruteur |
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
│   │   └── select-context/page.tsx
│   ├── (app)/                        # Routes protégées (layout avec sidebar)
│   │   ├── layout.tsx                # Shell + auth guard + permissions
│   │   ├── dashboard/page.tsx        # Dashboard par rôle (server-rendered)
│   │   ├── employees/
│   │   │   ├── page.tsx              # Liste
│   │   │   ├── new/page.tsx          # Création
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
│   │   └── settings/
│   ├── api/                          # Route Handlers (BFF endpoints)
│   │   ├── auth/
│   │   │   ├── login/route.ts
│   │   │   ├── logout/route.ts
│   │   │   ├── select-context/route.ts
│   │   │   └── me/route.ts
│   │   ├── hrm/
│   │   │   └── [...path]/route.ts    # Proxy générique optionnel
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
│   │       └── ... (15 modules)
│   ├── session.ts                    # Sessions httpOnly + CSRF
│   ├── permissions.ts                # Vérifie hrm:*:* perms
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
│   └── format.ts                     # XAF formatter, dates, etc.
├── components/
│   ├── ui/                           # shadcn/ui generated
│   ├── shell/
│   │   ├── Sidebar.tsx               # Filtre par perms
│   │   ├── Topbar.tsx                # Search + notif + user chip
│   │   ├── PageHeader.tsx            # avec breadcrumb + badge UC
│   │   ├── WorkspaceSwitcher.tsx     # Sélecteur d'organisation
│   │   └── ProtectedRoute.tsx
│   ├── kpi/                          # KpiCard, KpiGrid, Sparkline
│   ├── tables/                       # DataTable, FilterBar
│   ├── forms/                        # FormField, FormDialog
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
│   └── modules/
│       ├── useEmployees.ts           # CRUD + listings
│       ├── useLeaves.ts
│       └── ...
├── stores/
│   ├── workspace.ts                  # Org/agence courante (Zustand)
│   ├── ui.ts                         # Sidebar collapsed, theme
│   └── notifications.ts
├── middleware.ts                     # Auth guard, redirects
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

# Public
NEXT_PUBLIC_APP_NAME="HR Core"
NEXT_PUBLIC_DEFAULT_CURRENCY=XAF
NEXT_PUBLIC_DEFAULT_LOCALE=fr            # FR par défaut
NEXT_PUBLIC_SUPPORTED_LOCALES=fr,en      # FR + EN secondaire

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
  utilisateur (nom + rôle + avatar initiales).
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

> Liste exhaustive de **~80 pages**, organisée par sous-domaine. Statut `existant`
> = déjà esquissé dans les designs ; `nouveau` = manquant à inventer.
> Chaque page mentionne le rôle dominant, l'UC couvert, et les endpoints.

### 5.1 Authentification & onboarding (priorité bootstrap)

| Page | Statut | UC | Rôles | Endpoints BFF → KSM |
|---|---|---|---|---|
| `/login` | nouveau | — | Public | `POST /api/auth/identify`, `POST /api/auth/login` |
| `/mfa` | nouveau | — | Public | `POST /api/auth/login/mfa/confirm` |
| `/select-context` | nouveau | — | Authentifié multi-org | `POST /api/auth/select-context` |
| `/forgot-password` | nouveau | — | Public | (à clarifier avec backend si endpoint) |
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

### 5.3 Personnel — Employés

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/employees` | esquissé | UC-01..05 | `GET /employees` |
| `/employees/new` | esquissé | UC-01 | `POST /employees` |
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

> Note : pas d'endpoint backend `GET /contracts` global. Le BFF doit agréger
> en listant les employés puis les contrats. À mentionner pour ajustement
> backend si besoin (proposer `GET /api/v1/hrm/contracts` côté backend).

### 5.5 Personnel — Compétences

| Page | Statut | UC | Endpoints |
|---|---|---|---|
| `/skills` (référentiel) | nouveau | UC-24 | `GET /skills` |
| `/skills/new` | nouveau | UC-24 | `POST /skills` |
| `/skills/{id}` (détail + qui possède) | nouveau | UC-24 | ❌ endpoint manquant `GET /skills/{id}/employees` (à demander) |

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
| `/settings/users` (admin tenant) | nouveau | (cross-core auth si exposé) |
| `/settings/profile` | nouveau | `GET /api/users/me` |

> Pour les paramètres de **paie** (taux CNPS, IRPP, etc.), les routes
> backend sont dans `settings-core` (non HRM). Tu peux les afficher en
> **read-only** dans `/settings/payroll-rules` mais n'implémente l'édition
> que si une perm `settings:write` est exposée. **Demande à l'utilisateur**
> avant de toucher ce sujet.

### 5.19 Mes pages d'employé (self-service complet)

Regroupement des pages vues par un employé "lambda" :

| Page | Statut |
|---|---|
| `/dashboard` (vue employé) | esquissé |
| `/me/profile` | partiel |
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
- Charte tokens : KpiCard, StatusBadge, WorkflowTimeline, DataTable
- Tests setup (Vitest + Playwright)
- README dédié frontend (instructions, .env.example)

#### Phase 1 — Authentification + workspace
- Page `/login` (form Zod)
- Page `/mfa` (form code)
- Page `/select-context` (sélection org)
- Route Handlers : `POST /api/auth/login`, `mfa/confirm`, `select-context`,
  `logout`, `GET /api/auth/me`
- Stores Zustand : `workspace`, `session`
- Hook `useSession`, `useCan`
- Pages erreur (`/403`, `/404`, error boundary)
- **Critère de sortie** : un utilisateur peut se logger sur KSM, choisir une org,
  voir son user + permissions sur `/dashboard` (placeholder).

#### Phase 2 — Dashboard générique + premier rôle (Admin RH)
- Implémentation complète du rôle **Admin RH** (le plus large) :
  - Dashboard Admin RH avec KPIs réels (effectif, masse salariale, alertes)
  - Module Employés (liste, fiche, nouveau, edit, terminate/suspend/reactivate)
  - Module Contrats (par employé + vue globale)
  - Module Dépendants
  - Module Conformité (alertes CDD expirants, visites médicales)
- BFF modules `employees.ts`, `contracts.ts`, `dependents.ts`
- Hooks TanStack Query + invalidations
- Tests unitaires Zod + tests e2e Playwright des golden paths
- **Critère de sortie** : l'Admin RH peut créer un employé bout en bout avec
  données réelles en base, voir sa fiche 360°, gérer ses contrats et dépendants.

#### Phase 3 — Rôle Employé (self-service)
- Dashboard Employé
- Mon profil
- Mes congés (liste, nouvelle demande, annulation)
- Mes avances (liste, nouvelle demande)
- Mes notes de frais (liste, nouvelle, ajout lignes, soumission)
- Mes timesheets (saisie hebdomadaire, soumission)
- Mes bulletins de paie (consultation + téléchargement PDF)
- Mes formations (catalogue, inscription)
- Mon suivi médical
- Mes évaluations
- **Critère de sortie** : un employé peut faire l'intégralité de son
  self-service de bout en bout en données réelles.

#### Phase 4 — Rôle Manager
- Dashboard Manager (équipe + tâches en attente)
- Validation des congés (file PENDING)
- Validation des timesheets (équipe)
- Création / approbation ordres de mission
- Création / suivi des évaluations
- Approbation des notes de frais
- Planning des congés équipe (vue calendrier)
- **Critère de sortie** : un manager peut valider/rejeter tout ce qui lui est
  soumis et a une vue claire de son équipe.

#### Phase 5 — Rôle Comptable / DAF
- Dashboard Comptable
- Validation des cycles de paie (liste + détail + validation)
- Approbation des avances sur salaire (file + détail)
- Validation/remboursement des notes de frais
- Suivi des ordres de paiement
- **Critère de sortie** : un comptable peut valider un cycle de paie qui
  déclenche les événements `PAYROLL_VALIDATED` et `PAYMENT_ORDER_CREATED`.

#### Phase 6 — Rôle Responsable Paie
- Dashboard Resp. Paie
- Variables de temps (consolidation)
- Génération des bulletins
- Déclarations sociales (création, génération, soumission)
- **Critère de sortie** : produit les déclarations CNPS/DIPE/IRPP_CAC d'une période.

#### Phase 7 — Rôle Recruteur
- Dashboard Recruteur (pipeline)
- Offres d'emploi (liste, publication, clôture)
- Pipeline candidatures (kanban : NEW → … → HIRED)
- Fiche candidat (CV, lettre, entretiens)
- Planification d'entretiens
- Onboarding tasks (checklist par employé)
- Conversion candidat → employé
- **Critère de sortie** : workflow recrutement complet bout en bout.

#### Phase 8 — Rôle DRH (formations & développement)
- Dashboard DRH
- Catalogue de formations (liste, planification)
- Inscriptions
- Budget formation (création, engagement, réalisation)
- Évaluations globales
- KPIs RH (snapshots, comparaison)
- Compétences (référentiel + mapping)

#### Phase 9 — Rôle Médecin du travail
- Dashboard Médecin
- Visites médicales (liste, nouvelle, détail, historique par employé)
- Certificats d'aptitude
- Alertes échéances

#### Phase 10 — Rôle Contrôleur de gestion RH
- Dashboard analytics
- Reporting avancé (tendances, exports)
- KPI snapshots comparés

#### Phase 11 — Cross-rôle, polish, accessibility, dark mode
- Notifications (SSE depuis BFF)
- Recherche globale (⌘K)
- Export PDF des bulletins (côté BFF avec `@react-pdf/renderer` ou `puppeteer`)
- Export Excel (CSV) des listings
- Mobile responsive complet
- Dark mode
- a11y audit (Playwright axe)
- i18n (FR par défaut, structure prête pour EN)

#### Phase 12 — Tests, monitoring, déploiement
- Couverture e2e Playwright (golden paths par rôle)
- Tests intégration BFF (Vitest)
- Documentation utilisateur (1 page par rôle, en optionnel)
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

7. **Format de retour `LeaveBalance`** : exposer `soldeRestant` (déjà fait
   dans la response, OK).

8. **`PayrollRun.status = DRAFT`** : présent dans le PDF mais absent du code
   Java. Soit l'ajouter, soit aligner le PDF. **Demander à l'utilisateur**.

9. **`Application` création anonyme** : actuellement la sécurité au niveau
   classe exige `hasUserContext`. Si l'on veut un endpoint public pour qu'un
   candidat crée sa propre candidature, il faudra exposer un endpoint
   spécifique sans cette annotation.

10. **Webhook de paiement** : `handlePaymentCallback` n'est pas exposé en REST.
    Pour le suivi des paiements, soit créer un endpoint webhook (à protéger),
    soit consommer Kafka.

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

L'utilisateur tranchera entre :
- Modifier `hrm-core` (autorisé)
- Faire côté BFF (acceptable mais moins performant)
- Différer

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

L'application doit être **bilingue dès la Phase 1** :

- **Langue par défaut** : `fr` (français) — cible Cameroun + Afrique francophone
- **Langue secondaire** : `en` (anglais) — disponible immédiatement, sélecteur
  de langue dans le topbar / menu utilisateur
- Stockage de la préférence : cookie côté serveur + persistance dans le profil
  utilisateur si le backend `auth-core` expose un champ `locale`
- Détection initiale : `Accept-Language` HTTP en fallback, sinon `fr`
- **Routing** : URL non préfixée pour la langue par défaut (`/employees`) et
  préfixée pour l'autre (`/en/employees`) — convention `next-intl`

**Stack recommandée** : `next-intl` (compatible App Router, Server Components,
type-safe via JSON ou TS).

**Organisation des messages** :

```
src/i18n/
├── config.ts             # locales: ['fr', 'en']; defaultLocale: 'fr'
├── request.ts            # getRequestConfig() pour next-intl
└── messages/
    ├── fr/
    │   ├── common.json   # boutons, labels génériques, statuts
    │   ├── auth.json
    │   ├── employees.json
    │   ├── payroll.json
    │   └── ... (1 namespace par module HRM)
    └── en/
        └── ... (mêmes namespaces, mêmes clés)
```

**Règles d'écriture des messages** :

- Clés en `camelCase.dot.notation` (`employees.actions.create`,
  `payroll.status.calculated`)
- Pluralisation native ICU MessageFormat (`{count, plural, one {# employé} other {# employés}}`)
- Interpolations typées : `{name}`, `{count}`, `{date, date, long}`
- Aucune chaîne en dur dans le code (UI/serveur) sauf logs techniques
- Les enums backend (`ACTIVE`, `PENDING`, `IN_REPAYMENT`, …) sont des **codes
  techniques** non traduits ; on mappe vers une clé i18n
  (`statuses.employee.ACTIVE`) pour l'affichage

**Devise & formats numériques** :

- Devise XAF (Franc CFA) :
  `new Intl.NumberFormat(locale, { style: 'currency', currency: 'XAF', maximumFractionDigits: 0 })`
- Heures, pourcentages : `Intl.NumberFormat(locale, …)`
- Dates : `date-fns` avec `fr` / `enUS` selon locale active
- Hook utilitaire `useFormat()` qui expose `formatMoney`, `formatDate`,
  `formatNumber` calés sur la locale courante

**Tests i18n** :

- Vérifier qu'aucune page ne contient de string hardcodée (lint custom ou
  audit Playwright)
- Vérifier la parité des clés entre `fr/*.json` et `en/*.json` (script CI)
- Tester un golden path en `en` au minimum (Playwright)

### 8.5 Tests

- **Vitest** : pour les schemas Zod, pour les fonctions utilitaires, pour les
  hooks (avec `@testing-library/react-hooks`).
- **Playwright** : pour les golden paths e2e par rôle.
- **No mocks** : utiliser un environnement de test KSM avec seeders.

### 8.6 Commits

- Convention `conventional commits` : `feat(employees): add 360 profile page`
- Branche dédiée : `claude/setup-ksm-multitenant-WwlUJ` (déjà en place)
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
   - Choix du tooling tests (`vitest` vs `jest`)
   - Tests e2e : KSM dans Docker en CI ?
   - Si l'utilisateur veut un design léger ou complet sur Phase 1
   - Si des seeders backend sont disponibles ou à créer
   - Pour l'i18n : préférence du namespace/découpage (par module vs par feature) ?

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
- `KSM/docker-compose.application.yml` (stack complète : Postgres, Kafka, Redis, Elasticsearch, MinIO, KSM, Nginx)
- `KSM/docker-compose.infrastructure.yml` (infrastructure seule)
- `KSM/scripts/start-full-stack.sh` (script de bootstrap)

### Bonnes pratiques Next.js 15
- App Router : <https://nextjs.org/docs/app>
- Server Components vs Client Components : <https://nextjs.org/docs/app/building-your-application/rendering>
- Route Handlers : <https://nextjs.org/docs/app/building-your-application/routing/route-handlers>

### Bonnes pratiques TanStack Query
- <https://tanstack.com/query/latest/docs/framework/react/overview>

### Bonnes pratiques shadcn/ui
- <https://ui.shadcn.com/>

---

## 12. Résumé exécutif (TL;DR)

- Tu construis un **frontend Next.js 15 + BFF intégré** pour le module HRM de la
  plateforme SaaS multi-tenant **KSM**.
- Tu utilises **Next.js App Router**, **Server Components** pour les pages,
  **Route Handlers** pour le BFF, **TanStack Query** pour le state serveur,
  **shadcn/ui + TailwindCSS v4** avec le design system **HR Core**
  (orange chaud sur fond crème).
- Tu implémentes **9 rôles** (Admin RH, DRH, Manager, Employé, Comptable/DAF,
  Responsable Paie, Recruteur, Médecin du travail, Contrôleur RH) couvrant
  **27 use cases** (UC-01 à UC-27) répartis sur **~80 pages**.
- Tu **respectes le RBAC** `hrm:*:*`, le **multi-tenant** (X-Organization-Id),
  et la **sécurité serveur** (X-Client-Id + X-Api-Key jamais exposés au navigateur).
- Tu **n'utilises aucun mock**. Tout passe par le vrai KSM. Pour tester, demande
  des seeders backend.
- Tu **ne touches AUCUN autre module KSM** que `hrm-core` (et uniquement avec
  validation de l'utilisateur).
- Tu **produis d'abord un plan détaillé** que l'utilisateur valide, puis tu
  implémentes **phase par phase, rôle par rôle, use case par use case**.
- Tu **demandes** l'utilisateur dès qu'une décision sort de ton périmètre
  (modification backend, choix architectural majeur, etc.).
- Tu **commites et pousses** sur la branche `claude/setup-ksm-multitenant-WwlUJ`
  après chaque livrable validé.

**Bonne construction. Pose tes questions, propose ton plan, et attends le go.**
