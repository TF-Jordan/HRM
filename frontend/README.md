# HR Core — Frontend HRM (KSM)

Frontend Next.js 16 + BFF intégré pour le module HRM de la plateforme SaaS multi-tenant KSM.

> Voir `../PROMPT_FRONTEND_HRM.md` pour le brief complet et `../ANALYSE_KSM_HRM.md`
> pour l'analyse du backend.

## Stack

- **Next.js 16** (App Router, Server Components, Route Handlers BFF, Turbopack)
- **React 19** + **TypeScript strict**
- **TailwindCSS v4** (@theme inline, design tokens HR Core)
- **next-intl 4** (FR par défaut + EN)
- **TanStack Query 5** · **React Hook Form** · **Zod 4** · **Zustand 5**
- **iron-session** (sessions httpOnly chiffrées)
- **Resend / Nodemailer** (mails côté BFF)
- **@react-pdf/renderer** (bulletins de paie)
- **Vitest** + **Playwright** (tests)
- **pino** (logs serveur)

## Démarrage

```bash
# 1. Copier les variables d'environnement et remplir les secrets KSM
cp .env.example .env.local

# 2. Lancer le backend KSM en parallèle (depuis ../KSM/)
#    docker compose -f docker-compose.application.yml up

# 3. Démarrer le dev server
npm run dev
```

Le site écoute sur http://localhost:3000 (FR par défaut, EN via `/en`).

## Scripts

| Commande | Effet |
|---|---|
| `npm run dev` | Dev server (Turbopack) |
| `npm run build` | Build production |
| `npm run start` | Lance le build |
| `npm run lint` | ESLint |
| `npm run typecheck` | `tsc --noEmit` |
| `npm run format` | Prettier |
| `npm run openapi:generate` | Régénère `src/lib/types/ksm-openapi.ts` depuis `../KSM/iwm-openapi.json` |

## Variables d'environnement

Toutes documentées dans `.env.example`. Les secrets (`KSM_API_KEY`, `SESSION_SECRET`,
`RESEND_API_KEY`, etc.) sont **serveur uniquement** — jamais préfixés `NEXT_PUBLIC_`.

## Architecture

```
src/
├── app/[locale]/         # App Router localisé (FR/EN)
│   ├── (app)/            # Routes protégées (shell sidebar + topbar)
│   └── layout.tsx        # NextIntlClientProvider + QueryProvider
├── components/
│   ├── ui/               # Design system (Button, Card, KpiCard, …)
│   ├── shell/            # Sidebar, Topbar, PageHeader, LocaleSwitcher
│   └── providers/        # QueryProvider, etc.
├── server/               # 🔒 server-only — BFF
│   ├── ksm/              # client.ts (fetch wrapper), errors.ts, jwt.ts, modules/
│   ├── session.ts        # iron-session
│   └── logger.ts         # pino
├── lib/
│   ├── types/            # api.ts, auth.ts, ksm-openapi.ts (généré)
│   ├── validation/       # Helpers Zod (date-helpers, etc.)
│   ├── error-codes.ts    # Mapping KSM errorCode → i18n + action UI
│   ├── format.ts         # XAF entier, dates FR/EN, périodes
│   └── utils.ts          # cn, initials, …
├── i18n/                 # Config + messages JSON (fr/en)
├── env.ts                # Validation Zod des env vars
└── proxy.ts              # Middleware Next.js 16 (next-intl routing)
```

## Phases d'implémentation

Voir `PROMPT_FRONTEND_HRM.md` § 4. Branche `claude/dazzling-davinci-iZpvh`.

- **Phase 0** ✅ Bootstrap & Design System
- **Phase 1** Auth + Session + Workspace
- **Phase 2** SuperAdmin (organisation, utilisateurs, rôles, services)
- **Phase 3** Employés (UC-01 à UC-05)
- … (voir le brief)

## Page de démonstration

`http://localhost:3000/showcase` (ou `/en/showcase`) — preview de tous les composants
du design system HR Core.
