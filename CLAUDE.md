# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository layout

```
HRM/
├── frontend/     Next.js 16 BFF + React 19 UI (all active work)
└── KSM/          Spring Boot 4 backend (modular monolith, read-only reference)
```

All frontend work lives under `frontend/`. Always `cd frontend` before running pnpm commands.

---

## Commands (run from `frontend/`)

```bash
pnpm dev              # dev server on http://localhost:3005 (Turbopack)
pnpm build            # production build
pnpm typecheck        # tsc --noEmit (strict, noUncheckedIndexedAccess)
pnpm lint             # ESLint flat config
pnpm format           # Prettier
pnpm test             # Vitest unit tests
pnpm test -- --reporter=verbose <pattern>   # single test
pnpm test:e2e         # Playwright (requires full KSM stack running)
pnpm ksm:types        # regenerate src/lib/types/ksm-openapi.d.ts from KSM OpenAPI spec
```

Pre-commit gate: `pnpm lint && pnpm typecheck && pnpm test`

E2e tests require KSM + Postgres + Redis + Elasticsearch running.
Pass `PLAYWRIGHT_NO_WEBSERVER=1` when the dev server is already up.

---

## Architecture: pure BFF (no business logic)

The frontend stores nothing. Every data operation proxies to KSM at `/api/v1/hrm/`.

### Full request flow

```
Browser
  → Next.js Route Handler  (/api/hrm/*)
      getKsmContext()       reads session cookie → in-memory store → Session{user, context, accessToken}
      callKsm()             injects X-Client-Id, X-Api-Key, X-Tenant-Id, X-Organization-Id, Bearer
      → KSM REST API        returns ApiResponse<T> { success, data, message, errorCode, timestamp }
      withKsmHandler()      unwraps data / maps errors → consistent JSON
  → TanStack Query cache
  → Client component
```

### Key server-side files

| File | Role |
|---|---|
| `src/server/ksm/client.ts` | `callKsm<T>()` — single KSM fetch entry-point. Injects all headers, handles timeout, unwraps envelope. **Never call KSM from client components.** |
| `src/server/ksm/context.ts` | `getKsmContext()` — reads session, throws if no org context |
| `src/server/ksm/handler.ts` | `withKsmHandler()` — wraps route handlers; maps HttpError / ZodError / unknown → consistent JSON |
| `src/server/ksm/modules/*.ts` | Typed wrappers per domain resource (employees, leaves, payroll, …) |
| `src/server/session.ts` | `getSession()` / `setSession()` / `requireSession()` — HS256-signed cookie storing only opaque `sid` |
| `src/server/session-store.ts` | In-memory `Map<sid, Session>` (process-local; replace with Redis for multi-instance) |
| `src/server/profile.ts` | `getProfileSummary()` — server-side: decodes JWT permissions, infers role, finds Employee record by `actorId` |
| `src/proxy.ts` | Next.js middleware: auth guard (→ /login) + next-intl locale routing |

### Session & permissions design

- Cookie carries only an opaque `sid` (~340 B) signed HS256 — well under the 4 KiB Chromium limit.
- The actual `Session` (user, context, KSM `accessToken`) lives in the in-memory store keyed by `sid`.
- **Permissions are not stored** — they are decoded from the KSM JWT on each `GET /api/auth/me` call.
- `usePermissions()` (client hook) calls `/api/auth/me` → decodes `payload.permissions[]` from the JWT.
- Role codes (`PLATFORM_ADMIN`, `HRM_ADMIN`, `DRH`, `RESP_PAIE`, `COMPTABLE`, `RECRUTEUR`, `MANAGER`, `MEDECIN`, `EMPLOYE`) are **inferred** from the permission set by `inferRoleCode()` in `src/lib/roles.ts`.

### Multi-tenant auth flow

1. `POST /api/auth/discover-contexts {principal, password}` → `selectionToken` + list of tenant×org contexts
2. If exactly 1 tenant and 1 org → auto-select; call `select-context` and create session immediately.
3. Otherwise → return `kind:"select-context"` so the client routes to `/select-context`.
4. `POST /api/auth/select-context {selectionToken, contextId, organizationId}` → KSM JWT (RS256) with full context.

---

## Module structure — adding a new resource

For each domain resource, 5 layers must all exist:

1. `src/server/ksm/modules/<resource>.ts` — typed `callKsm` wrappers (server-only)
2. `src/app/api/hrm/<resource>/route.ts` — Next.js route handler using `withKsmHandler` + `getKsmContext`
3. `src/hooks/modules/use<Resource>.ts` — TanStack Query hooks using `bffFetch` + `queryKeys`
4. `src/lib/api-client.ts → queryKeys.hrm` — centralised query key definitions
5. `src/lib/types/hrm/<resource>.ts` + `src/lib/validation/hrm/<resource>.schema.ts`

### Page pattern

Each page is split:
- `page.tsx` — RSC, minimal, calls `getProfileSummary()` or other server data, passes props
- `<name>-client.tsx` — `"use client"`, TanStack Query hooks, all interactivity

### KSM API conventions

- All endpoints: `GET /api/v1/hrm/<resource>` — list endpoints require `organizationId` as **query param** (not just in headers)
- All responses: `{ success: boolean, data: T, message: string, errorCode: string | null, timestamp: string }`
- Permission format: `hrm:<resource>:<action>` (e.g. `hrm:leave:approve`)
- KSM is Spring WebFlux reactive — no pagination on listings (returns full arrays)

---

## RBAC / Sidebar

- Sidebar items have `requiredAnyPerm?: string[]` — filtered client-side via `hasAnyPermission(perms, item.requiredAnyPerm)`.
- `usePermissions()` is the client hook; `getProfileSummary()` is its RSC equivalent.
- Dashboard renders role-specific panels via `renderForRole(roleCode, employeeId)` in `dashboard-client.tsx`.

### Role summary

| RoleCode | Key permissions | Main pages |
|---|---|---|
| `PLATFORM_ADMIN` | `iam:admin` | Everything |
| `HRM_ADMIN` | All `hrm:*` | Employees, payroll, declarations |
| `DRH` | `hrm:training:manage`, `hrm:kpi:read` | Trainings, budgets, reviews, analytics |
| `RESP_PAIE` | `hrm:payroll:run` | Payroll runs, declarations (CNPS, DIPE, IRPP/CAC) |
| `COMPTABLE` | `hrm:loan:approve` | Payroll validation, expenses/loans approval |
| `MANAGER` | `hrm:leave:approve` | Pending leaves, timesheets, mission orders, reviews |
| `RECRUTEUR` | `hrm:recruitment:manage` | Job offers, application pipeline |
| `MEDECIN` | `hrm:medical:create` | Medical visits, aptitude certificates |
| `EMPLOYE` | `hrm:leave:create`, `hrm:expense:create` | Self-service (my leaves, loans, expenses, profile) |

---

## Domain state machines (KSM enforces; BFF just proxies transitions)

| Entity | States |
|---|---|
| Employee | `ACTIVE` ↔ `ON_LEAVE`, `ACTIVE` ↔ `SUSPENDED`, `ACTIVE`/`SUSPENDED` → `TERMINATED` |
| Contract | `ACTIVE` → `EXPIRED`/`TERMINATED`/`RENEWED` (one ACTIVE per employee) |
| LeaveRequest | `PENDING` → `APPROVED`/`REJECTED`/`CANCELLED` |
| LoanAdvance | `PENDING` → `IN_REPAYMENT` → `FULLY_REPAID` |
| PayrollRun | `CALCULATED` → `VALIDATED` → `PAID` |
| Expense | `DRAFT` → `SUBMITTED` → `APPROVED`/`REJECTED` → `REIMBURSED` |
| MissionOrder | `DRAFT` → `APPROVED` → `IN_PROGRESS` → `COMPLETED`/`CANCELLED` |
| Application | `NEW` → `SHORTLISTED` → `INTERVIEWING` → `OFFERED` → `HIRED` (or `REJECTED` at any stage) |
| Review | `DRAFT` → `SUBMITTED` → `ACKNOWLEDGED` → `FINALIZED` |
| SocialDeclaration | `DRAFT` → `GENERATED` → `SUBMITTED` → `ACKNOWLEDGED` |

`StatusBadge` in `src/components/ui-tokens/StatusBadge.tsx` centralises the status → color tone mapping for all of these.

---

## i18n

- Locales: `fr` (default), `en`. Config in `src/i18n/routing.ts`.
- All namespaces loaded in `src/i18n/request.ts`. **When adding a namespace: add JSON files for both `fr/` and `en/` AND register it in `request.ts`.**
- Raw translation keys rendered (e.g. `domain.title`) always mean the namespace is missing from `request.ts`.
- Server components: `getTranslations('namespace')`. Client components: `useTranslations('namespace')`.
- Currency: XAF (FCFA) via `useFormat().money()`. Dates via `useFormat().date()`. Timezone: `Africa/Douala`.

---

## KSM security model (must-know for BFF calls)

KSM applies 5 security layers in order:
1. `X-Client-Id` + `X-Api-Key` → identifies the frontend as an authorised ClientApplication
2. Service filter → client must be allowed for the HRM service
3. Redis quota per `(tenantId, clientId, serviceCode)`
4. Organisation service subscription check (must be subscribed to HRM)
5. User permission check (`hrm:<resource>:<action>`)

**`X-Client-Id` and `X-Api-Key` must never appear in client-side code or browser responses.**

---

## Environment variables

Validated at startup by Zod in `src/env.ts`. Server-only (not exposed to browser):

| Variable | Purpose | Default |
|---|---|---|
| `KSM_BASE_URL` | KSM absolute URL | `http://localhost:8080` |
| `KSM_CLIENT_ID` / `KSM_API_KEY` | Injected on every KSM call | `dev-platform-backend` / `dev-api-key` |
| `SESSION_SECRET` | HS256 signing key (min 32 bytes) | dev placeholder |
| `SESSION_COOKIE_NAME` | Cookie name | `hrm_session` |
| `SESSION_TTL_SECONDS` | Session lifetime | `3600` |

Copy `frontend/.env.example` → `frontend/.env.local` before first run.

---

## Dev seed (first run)

Credentials: `admin@hrcore.local` / `Admin@HRCore2025!`
Tenant: `00000001-0000-0000-0000-000000000001`
Organisation: `00000001-0000-0000-0000-000000000002`
The admin user has no Employee record by default → `employeeId` is null in `getProfileSummary()`.

Full stack startup and troubleshooting: see `frontend/README.md`.
