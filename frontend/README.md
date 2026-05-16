# HR Core — Frontend (Next.js BFF for KSM HRM)

Frontend Next.js 16 et BFF intégré pour le module HRM de la plateforme KSM
(plateforme SaaS multi-tenant Spring Boot). Le BFF ne stocke aucune donnée :
il proxifie les appels vers KSM en injectant les en-têtes serveur
(`X-Client-Id`, `X-Api-Key`) et le contexte tenant
(`X-Tenant-Id`, `X-Organization-Id`, `X-Agency-Id`).

## Stack

- **Next.js 16** App Router (Turbopack par défaut) + React 19
- **TypeScript strict** (avec `noUncheckedIndexedAccess`)
- **TailwindCSS v4** avec tokens du design system HR Core (orange chaud)
- **shadcn/ui** primitives sur Radix
- **next-intl** (FR par défaut + EN secondaire)
- **TanStack Query v5** pour le state serveur
- **React Hook Form + Zod 4** pour les formulaires
- **Zustand** pour le client state
- **jose** + cookies HttpOnly natifs Next.js pour les sessions
- **pino** logger serveur
- **Vitest** (unit) + **Playwright** (e2e) + **@axe-core/playwright** (a11y)

## Prérequis

- Node.js >= 22
- pnpm >= 10
- Un KSM joignable (par défaut `http://localhost:8080`) avec un
  `ClientApplication` enregistré (id + apiKey).

## Démarrage

```bash
pnpm install
cp .env.example .env.local      # ajuste KSM_BASE_URL / KSM_CLIENT_ID / KSM_API_KEY / SESSION_SECRET
pnpm dev                        # http://localhost:3005
```

Le port par défaut est **3005** (le 3000 entre en conflit avec
Grafana dans la stack KSM locale).

## Variables d'environnement

Définies via `src/env.ts` (validation Zod). Tout ce qui n'est pas préfixé
`NEXT_PUBLIC_*` reste côté serveur uniquement.

| Variable | Côté | Rôle |
|---|---|---|
| `KSM_BASE_URL` | server | URL absolue de l'instance KSM |
| `KSM_CLIENT_ID` | server | Identifiant `ClientApplication` enregistré |
| `KSM_API_KEY` | server | Secret partagé avec KSM |
| `KSM_REQUEST_TIMEOUT_MS` | server | Timeout d'appel (15s par défaut) |
| `SESSION_SECRET` | server | Clé HS256 de signature des cookies de session |
| `SESSION_COOKIE_NAME` | server | Nom du cookie (préfixe `__Host-` recommandé) |
| `SESSION_TTL_SECONDS` | server | Durée de vie de session |
| `LOG_LEVEL` | server | Pino log level (`info`, `debug`, ...) |
| `NEXT_PUBLIC_DEFAULT_LOCALE` | client | `fr` par défaut, `en` secondaire |

## Structure

```
src/
├── env.ts                        Validation Zod des env vars
├── proxy.ts                      Next 16 middleware (auth guard + i18n)
├── i18n/
│   ├── config.ts                 locales = ['fr', 'en'], defaultLocale = 'fr'
│   ├── routing.ts                next-intl routing (as-needed prefix)
│   ├── navigation.ts             Link / useRouter / redirect localisés
│   ├── request.ts                Chargement des namespaces par locale
│   └── messages/{fr,en}/         Namespaces : common, auth, validation,
│                                  errors, statuses, navigation, dashboard
├── app/
│   ├── layout.tsx                Root layout (fonts, html)
│   ├── [locale]/
│   │   ├── layout.tsx            NextIntlClientProvider + QueryProvider + Toaster
│   │   ├── page.tsx              redirect -> /dashboard
│   │   ├── (auth)/
│   │   │   ├── layout.tsx        Ambient backdrop + LocaleSwitcher
│   │   │   └── login/            Page de login (RHF + Zod)
│   │   ├── (app)/
│   │   │   ├── layout.tsx        Sidebar + Topbar (auth-guarded)
│   │   │   └── dashboard/        Tableau de bord (placeholder Phase 0)
│   │   ├── error.tsx
│   │   └── not-found.tsx
│   └── api/
│       ├── auth/{login,logout,me}/route.ts
│       └── health/route.ts       Sonde frontend + KSM
├── components/
│   ├── ui/                       Boutons, inputs, cartes, dropdown, avatar, ...
│   ├── shell/                    Sidebar, Topbar, PageHeader, LocaleSwitcher,
│   │                             UserChip
│   ├── ui-tokens/                KpiCard, StatusBadge, WorkflowTimeline, DataTable
│   └── providers/                QueryProvider
├── server/
│   ├── ksm/
│   │   ├── client.ts             Wrapper fetch -> KSM (injection headers + envelope)
│   │   ├── errors.ts             Mapping ApiResponse -> HttpError
│   │   └── modules/auth.ts       Typed wrappers KSM auth
│   └── session.ts                jose + Next cookies (signature HS256)
└── lib/
    ├── utils.ts                  cn(), initials(), ...
    ├── types/                    api.ts, auth.ts, ksm-openapi.d.ts (généré)
    └── validation/               schemas Zod
```

## Commandes principales

| Commande | Effet |
|---|---|
| `pnpm dev` | Serveur de développement sur `http://0.0.0.0:3005` |
| `pnpm build` | Build production (Turbopack) |
| `pnpm start` | Démarrage du build de production |
| `pnpm typecheck` | `tsc --noEmit` strict |
| `pnpm lint` | ESLint flat config |
| `pnpm test` | Vitest (jsdom, parité i18n, schemas Zod, utils) |
| `pnpm test:e2e` | Playwright (smoke + golden paths) |
| `pnpm ksm:types` | Régénère `src/lib/types/ksm-openapi.d.ts` depuis `../KSM/iwm-openapi.json` |
| `pnpm format` | Prettier |

## Boucle de développement

1. KSM accessible (cf. `KSM/README.md` ou
   `docker compose -f KSM/docker-compose.application.yml up`).
2. `.env.local` configuré avec un `ClientApplication` valide.
3. `pnpm dev` puis vérifier `http://localhost:3005/api/health` qui doit
   indiquer `ksm.ok: true`.
4. Tester `/login` (FR) et `/en/login` (EN).
5. `pnpm typecheck && pnpm lint && pnpm test` doivent rester verts avant
   chaque commit.

## Sécurité (rappel BFF)

- `X-Client-Id` et `X-Api-Key` ne quittent **jamais** le serveur.
- Les sessions sont signées HS256 et stockées dans un cookie
  `HttpOnly` + `SameSite=Lax`.
- En production, mettre `SESSION_SECRET` >= 48 octets aléatoires
  (`openssl rand -base64 48`).
- Aucun mock côté frontend : tous les appels passent par le vrai KSM.

## Statut Phase 0

Phase 0 — bootstrap & infrastructure — est complète :

- Next.js 16 + Turbopack + React 19 + TS strict
- Tokens HR Core portés en Tailwind v4
- shadcn-style primitives (Button, Input, Label, Card, Badge, Dropdown, Avatar, Skeleton)
- next-intl FR/EN avec 7 namespaces et tests de parité
- Pino + Zod env validation
- BFF KSM client + session jose + proxy auth-guarded (Next 16 `proxy.ts`)
- Composants shell (Sidebar, Topbar, PageHeader, LocaleSwitcher, UserChip)
- Tokens métier (KpiCard, StatusBadge, WorkflowTimeline, DataTable)
- Page `/login` (RHF + Zod)
- Page `/dashboard` (placeholder)
- Route Handlers `/api/auth/{login,logout,me}` + `/api/health`
- Vitest + Playwright configurés, tests unit et smoke
- Types KSM générés depuis OpenAPI

Phase 1 (Auth complète + workspace + sélection contexte) reprendra ce socle.
