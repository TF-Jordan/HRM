# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository overview

This is an HRM (Human Resources Management) platform composed of two independent sub-projects:

- `KSM/` — Spring Boot modular monolith backend (RT-comops), the authoritative API server
- `frontend/` — Next.js 16 BFF + React frontend that consumes KSM

Design references are in `DESIGN/` and the full KSM/HRM analysis is in `ANALYSE_KSM_HRM.md`.

---

## Frontend (`frontend/`)

### Stack

- **Next.js 16.2.6** with App Router — **breaking changes vs. prior versions**; consult `node_modules/next/dist/docs/` before writing Next.js-specific code
- TypeScript, Tailwind CSS v4, `next-intl` (fr/en)
- React Query for server data, Zustand for client state
- `react-hook-form` + `zod` for forms, `@react-pdf/renderer` for PDF export
- Session: `iron-session` (HTTP-only cookie), iron-session stores the KSM JWT

### Commands (run from `frontend/`)

```bash
npm run dev          # local dev server
npm run build        # production build
npm run typecheck    # tsc --noEmit
npm run lint         # eslint
npm run format       # prettier --write src/**
npm run openapi:generate  # regenerate src/lib/types/ksm-openapi.ts from KSM/iwm-openapi.json
```

### Architecture

**BFF pattern.** The browser never calls KSM directly. All API traffic goes through Next.js Route Handlers in `src/app/api/`, which proxy to KSM with server-only credentials.

Key files:

| File | Role |
|---|---|
| `src/server/ksm/client.ts` | Central KSM HTTP client (server-only). Injects `X-Client-Id`, `X-Api-Key`, `Authorization: Bearer`, `X-Tenant-Id`, `X-Organization-Id`. |
| `src/server/ksm/modules/` | One file per KSM domain (employees, leaves, expenses, …). Each exports typed wrappers around `callKsm`. |
| `src/server/handlers.ts` | `authenticatedRoute()` / `requirePermissionRoute()` wrappers for Route Handlers. |
| `src/server/api-response.ts` | `ok()`, `created()`, `fail()`, `handleRoute()` — every BFF route uses these to emit `{ ok: true, data }` / `{ ok: false, ... }`. |
| `src/lib/api-client.ts` | Client-side `apiFetch<T>()` — calls BFF routes, unwraps `BffSuccess`/`BffError`. |
| `src/lib/types/auth.ts` | `AppSession`, `SessionUser`, `WorkspaceContext` types. |
| `src/env.ts` | Zod-validated env (server + client split). Fails loudly at boot if vars are missing. |

**Route structure:**

```
src/app/
  [locale]/
    (app)/          # Protected pages — guarded by AppLayout (redirects to /login if no session)
    (auth)/         # Login, change-password, select-context
  api/
    auth/           # login, logout, me, select-context, change-password
    hrm/            # all HRM domain BFF routes
    admin/          # roles, users, organization, audit
    files/          # upload + download proxy
```

**i18n.** All user-facing strings live in `src/i18n/messages/{fr,en}/<domain>.json`. Use `next-intl`'s `useTranslations` / `getTranslations` — never hardcode UI strings. Navigation helpers that preserve locale are in `src/i18n/navigation.ts`.

**RBAC on the frontend:**
- `useCan("hrm:expense:read")` — returns true if the session holds the permission (any scope)
- `requirePermissionRoute(["hrm:expense:approve"], handler)` — server-side guard on Route Handlers
- KSM enforces permissions authoritatively; the frontend checks are UI-only

### Environment variables

Copy `.env.example` to `.env` at the repo root. Key vars:

| Var | Purpose |
|---|---|
| `KSM_BASE_URL` | KSM server URL (e.g. `http://localhost:8080`) |
| `KSM_CLIENT_ID` / `KSM_API_KEY` | Server-only credentials injected by `callKsm` |
| `SESSION_SECRET` | iron-session encryption key, minimum 32 chars |
| `NEXT_PUBLIC_DEFAULT_LOCALE` | `fr` or `en` |

---

## Backend (`KSM/`)

### Stack

- Spring Boot, **modular monolith**, **hexagonal architecture per module**
- `RT-comops-bootstrap` is the **only executable module** — all others are libraries
- PostgreSQL (R2DBC reactive) + Liquibase migrations
- Kafka for outbox relay, Redis for permission cache/quotas, Elasticsearch for search projections (all optional in dev)
- JWT RS256; set `IWM_JWT_AUTO_GENERATE_KEY_PAIR=true` in dev

### Commands (run from `KSM/`)

```bash
# Standard unit tests (in-memory profile)
mvn -q test

# Full contract tests against PostgreSQL (requires local PG)
mvn -q -pl RT-comops-bootstrap -am \
  -Diwm.tests.r2dbc.enabled=true \
  -Dtest=IdentityAccessContractTests,OrganizationCatalogContractTests,SalesInventoryContractTests,AccountingTreasuryContractTests,ResourceContractTests \
  -Dsurefire.failIfNoSpecifiedTests=false test

# Kafka integration tests
mvn -q -Dtest=KafkaOutboxIntegrationTests \
  -Diwm.tests.kafka.enabled=true \
  -pl RT-comops-bootstrap -am \
  -Dsurefire.failIfNoSpecifiedTests=false test

# Hexagonal architecture guard tests
mvn -q -pl RT-comops-bootstrap -am \
  -Dtest=SearchAdapterArchitectureTests,IntegrationAdapterArchitectureTests,HexagonalModuleArchitectureTests \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

### Module map

| Module | Responsibility |
|---|---|
| `RT-comops-kernel-core` | Multi-tenancy, outbox, `ClientApplication` auth, quotas, audit |
| `RT-comops-auth-core` | Accounts, JWT RS256, `users/me`, onboarding |
| `RT-comops-roles-core` | RBAC multi-scope (SYSTEM / TENANT / ORGANIZATION / AGENCY) |
| `RT-comops-administration-core` | Permission catalogue, role management, admin audit |
| `RT-comops-actor-core` | Canonical `BusinessActor` identity |
| `RT-comops-organization-core` | Organisations, agencies, service subscriptions |
| `RT-comops-hrm-core` | **Employees, leaves, expenses, recruitment, timesheets, training, reviews, medical, missions, declarations, skills, KPI dashboard** |
| `RT-comops-payroll-core` | **Payroll engine: payslip/STC calculation, validation, payment orders, signed PDF documents (see `RT-comops-payroll-core/DOCUMENT_SIGNING.md`)** — split out of hrm-core |
| `RT-comops-file-core` | File upload/download with MIME validation |
| `RT-comops-bootstrap` | Assembly, Liquibase migrations, integration tests |

Other modules exist (`accounting`, `actor`, `billing`, `blockchain`, `cashier`, `common`, `inventory`, `product`, `resource`, `sales`, `settings`, `tp`, `treasury`-core) but are out of scope for HRM work.

> **Modifiable-module constraint:** for HRM frontend work, only `RT-comops-hrm-core` and `RT-comops-payroll-core` are editable. **All other cores (auth, kernel, actor, roles, file, settings, organization, administration, accounting, …) are read-only** — any change to them must be justified and validated. No mocks/fake data: every screen consumes real KSM; seed test data via Liquibase seeders.

### HRM module permissions pattern

Permission codes follow `hrm:<resource>:<action>`, e.g.:
- `hrm:employee:read`, `hrm:employee:create`
- `hrm:expense:approve`, `hrm:leave:approve`
- `hrm:payroll:validate`

KSM appends a scope suffix at runtime: `hrm:expense:read#ORGANIZATION:<uuid>`. The frontend strips the suffix with `.split("#")[0]`.

### Key request headers

Every request from the BFF to KSM carries:

| Header | Value |
|---|---|
| `X-Client-Id` | `KSM_CLIENT_ID` env var |
| `X-Api-Key` | `KSM_API_KEY` env var |
| `Authorization` | `Bearer <jwt>` from session |
| `X-Tenant-Id` | From session workspace |
| `X-Organization-Id` | From session workspace (required for HRM routes) |

### OpenAPI

KSM exposes `GET /v3/api-docs`. The JSON snapshot used to generate frontend types is at `KSM/iwm-openapi.json`. After updating it, run `npm run openapi:generate` from `frontend/`.

---

## Local infrastructure

The `KSM/` directory contains a `docker-compose.infrastructure.yml` (referenced in docs but living in the original KSM repo). A local PostgreSQL 16, Redis, Kafka (KRaft, no ZooKeeper), and optionally Elasticsearch are needed for full integration tests.

Default dev coordinates from `.env.example`:
- PostgreSQL: `localhost:5432/iwm` (user: `iwm`, password: `iwm`)
- Redis: `localhost:6379`
- Kafka: `localhost:9092`
- KSM: `http://localhost:8080`
- Frontend: `http://localhost:3000`
