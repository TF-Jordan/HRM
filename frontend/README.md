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

| Outil | Version min | Note |
|---|---|---|
| Docker + Docker Compose | 24+ | Pour l'infra KSM (Postgres, Kafka, Redis, Elasticsearch) |
| Java JDK | 21 | Compilation et exécution de KSM (Spring Boot 4) |
| Maven | 3.9+ | Build KSM |
| Node.js | 22+ | Runtime du frontend Next.js 16 |
| pnpm | 10+ | `corepack enable && corepack prepare pnpm@10.33.0 --activate` |

## Démarrage rapide (frontend seul, KSM déjà lancé)

```bash
pnpm install
cp .env.example .env.local      # ajuste KSM_BASE_URL / KSM_CLIENT_ID / KSM_API_KEY / SESSION_SECRET
pnpm dev                        # http://localhost:3005
```

Le port par défaut est **3005** (le 3000 entre en conflit avec
Grafana dans la stack KSM locale).

## Lancement complet en local (KSM + Frontend)

Procédure validée pour tester l'intégralité de la chaîne (KSM backend
+ infra Docker + frontend BFF Next.js) sur un poste de développement.
Le frontend Next.js embarque le BFF — pas de service séparé à lancer.

### 1. Démarrer l'infrastructure (Postgres, Kafka, Redis, Elasticsearch)

```bash
cd /chemin/vers/HRM/KSM
docker compose -f docker-compose.infrastructure.yml up -d
```

Attendre que Postgres et Elasticsearch passent en `healthy` (~60–90 s
pour ES au premier démarrage) :

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
# iwm-postgres        healthy
# iwm-kafka           healthy
# iwm-redis           healthy
# iwm-elasticsearch   healthy
curl -s -o /dev/null -w "ES: %{http_code}\n" http://localhost:9200/_cluster/health
# ES: 200
```

### 2. Construire le JAR bootstrap de KSM (première fois uniquement)

```bash
cd /chemin/vers/HRM/KSM
mvn -pl RT-comops-bootstrap -am -Dmaven.test.skip=true package
# génère RT-comops-bootstrap/target/RT-comops-bootstrap-0.1.0-SNAPSHOT.jar
```

Le build prend 3–5 min la première fois (téléchargement des deps
Maven), puis quelques secondes en incrémental.

### 3. Lancer KSM

```bash
SPRING_PROFILES_ACTIVE=r2dbc \
IWM_BOOTSTRAP_CLIENT_ENABLED=true \
IWM_BOOTSTRAP_CLIENT_ID=dev-platform-backend \
IWM_BOOTSTRAP_CLIENT_SECRET=dev-api-key \
IWM_BOOTSTRAP_CLIENT_ALLOWED_SERVICES=ORGANIZATION,SETTINGS,COMMERCIAL,PRODUCT,INVENTORY,SALES,ACCOUNTING,TREASURY,RESOURCE,HRM,BILLING,BANKING,CASHIER,BLOCKCHAIN \
IWM_JWT_AUTO_GENERATE_KEY_PAIR=true \
IWM_JWT_KEY_ID=iwm-key-local \
IWM_JWT_ISSUER=iwm-backend-local \
IWM_MANAGEMENT_API_KEY=dev-management-key \
MANAGEMENT_SERVER_PORT=8081 \
SERVER_PORT=8080 \
IWM_R2DBC_URL=r2dbc:postgresql://localhost:5432/iwm \
IWM_R2DBC_USERNAME=iwm IWM_R2DBC_PASSWORD=iwm IWM_R2DBC_POOL_ENABLED=true \
IWM_LIQUIBASE_URL=jdbc:postgresql://localhost:5432/iwm \
IWM_LIQUIBASE_USERNAME=iwm IWM_LIQUIBASE_PASSWORD=iwm \
IWM_KAFKA_BOOTSTRAP_SERVERS=localhost:9092 \
IWM_REDIS_PERMISSION_CACHE_ENABLED=true \
IWM_REDIS_HOST=localhost IWM_REDIS_PORT=6379 \
IWM_ELASTICSEARCH_SEARCH_ENABLED=true \
IWM_ELASTICSEARCH_URIS=http://localhost:9200 \
IWM_TENANT_REQUEST_QUOTA_ENABLED=false \
java -jar RT-comops-bootstrap/target/RT-comops-bootstrap-0.1.0-SNAPSHOT.jar
```

KSM démarre en 10–15 s. Vérifier :

```bash
curl -s -o /dev/null -w "Management: %{http_code}\n" http://localhost:8081/actuator/health
# Management: 200
curl -s -o /dev/null -w "API: %{http_code}\n" http://localhost:8080/api/v1/hrm/employees
# API: 401  (attendu sans Authorization)
```

Au premier démarrage, Liquibase joue la migration `067-dev-seed` qui crée :

- Tenant `00000001-0000-0000-0000-000000000001`
- Organisation HRCORE `00000001-0000-0000-0000-000000000002`
- Utilisateur admin `admin@hrcore.local` / `Admin@HRCore2025!`
- 52 permissions `hrm:*:*` rattachées à l'admin
- Séquence matricule `HRC-00001` → `HRC-99999`

### 4. Créer l'Employee admin (première fois ou après reset DB)

L'utilisateur admin est créé en Phase Liquibase, mais il n'a pas
d'enregistrement `Employee` (nécessaire pour les pages self-service).
À créer une fois via l'API :

```bash
TOKEN=$(curl -s -X POST \
  -H "X-Client-Id: dev-platform-backend" -H "X-Api-Key: dev-api-key" \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: 00000001-0000-0000-0000-000000000001" \
  -d '{"principal":"admin@hrcore.local","password":"Admin@HRCore2025!"}' \
  http://localhost:8080/api/auth/login \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

curl -s -X POST http://localhost:8080/api/v1/hrm/employees \
  -H "X-Client-Id: dev-platform-backend" -H "X-Api-Key: dev-api-key" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: 00000001-0000-0000-0000-000000000001" \
  -H "X-Organization-Id: 00000001-0000-0000-0000-000000000002" \
  -H "Content-Type: application/json" \
  -d '{
    "actorId":"00000001-0000-0000-0000-000000000003",
    "categorie":10,
    "dateEmbauche":"2024-01-01",
    "modePaiement":"BANK_TRANSFER",
    "compteBancaire":"00099999999",
    "departmentCode":"DIRECTION",
    "numCnps":"ADM-2024-001"
  }'
# {"success":true,"data":{"id":"...","matricule":"HRC-00001",...}}
```

(Optionnel) Créditer un solde de congés pour tester les demandes :

```bash
EMPID=<id-renvoyé-au-dessus>
docker exec iwm-postgres psql -U iwm -d iwm -c \
  "UPDATE hrm_leave_balance SET acquis=18 WHERE employee_id='$EMPID' AND type='ANNUAL';"
```

### 5. Lancer le frontend Next.js (BFF intégré)

Dans un autre terminal :

```bash
cd /chemin/vers/HRM/frontend
pnpm install                  # première fois uniquement
cp .env.example .env.local    # valeurs par défaut OK pour KSM local
pnpm dev
# ▲ Next.js 16.2.6 (Turbopack)
# - Local: http://localhost:3005
```

Vérifier la santé de la chaîne :

```bash
curl -s http://localhost:3005/api/health
# {"ok":true,"ksm":{"ok":true,"status":401},...}
```

`ksm.status: 401` est attendu — le frontend tape l'endpoint KSM sans
session pour confirmer que le serveur répond.

### 6. Se connecter dans le navigateur

Ouvrir <http://localhost:3005>.

- Email : `admin@hrcore.local`
- Mot de passe : `Admin@HRCore2025!`

Le dashboard s'affiche avec les KPI en orange HR Core. Naviguer via
la sidebar : Employés, Congés, Paie, Recrutement, Formations,
Compétences, Analytics, Suivi médical, Déclarations. Tester ⌘K
(palette de commandes), le toggle dark mode et l'export PDF/CSV
sur la page Employés.

### Arrêt propre

```bash
# Frontend : Ctrl+C dans le terminal pnpm dev
# KSM      : Ctrl+C dans le terminal java -jar
# Infra Docker (préserve les volumes) :
docker compose -f /chemin/vers/HRM/KSM/docker-compose.infrastructure.yml stop
# Reset complet (efface la DB) :
docker compose -f /chemin/vers/HRM/KSM/docker-compose.infrastructure.yml down -v
```

### Troubleshooting

| Symptôme | Cause probable | Solution |
|---|---|---|
| KSM `BadGrammar: relation "kernel.outbox_event" does not exist` | Liquibase pas terminé | Vérifier `docker logs iwm-postgres`, relancer KSM (Liquibase reprend) |
| `/api/auth/login` 401 | Mauvais credentials OU admin pas seedé | Vérifier `SELECT * FROM auth_user;` dans postgres |
| `/api/hrm/me/employee` 404 `NO_EMPLOYEE` | Étape 4 non faite | Recréer l'Employee admin (cf. §4) |
| Cookie `hrm_session` rejeté par Chromium | Cookie > 4 KiB | Vérifier que la session ne stocke pas le JWT KSM (déjà géré : opaque sessionId) |
| Frontend affiche `domain.title` brut | Namespace i18n oublié dans `src/i18n/request.ts` | Ajouter l'import + redémarrer `pnpm dev` |
| `ERR_PNPM_NO_IMPORTER_MANIFEST_FOUND` | pnpm lancé hors du dossier `frontend/` | `cd frontend && pnpm <cmd>` |
| Docker rate limit sur `pull` | Anonymes limités à 100/6h sur docker.io | Attendre 6 h ou `docker login` |
| ES `health: starting` longtemps | Index initial lent | Patienter, vérifier `curl localhost:9200/_cluster/health` |
| Liquibase changeset `067-dev-seed` cassé après reset partiel | Checksum invalide | `docker exec iwm-postgres psql -U iwm -d iwm -c "DELETE FROM databasechangelog WHERE id='067-dev-seed';"` puis relancer KSM |

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

1. Suivre `Lancement complet en local (KSM + Frontend)` ci-dessus
   pour avoir KSM + infra + frontend up.
2. Modifier le code dans `src/`.
3. Vérifier `http://localhost:3005/api/health` → `ksm.ok: true`.
4. Avant chaque commit :
   ```bash
   pnpm lint && pnpm typecheck && pnpm test
   ```
5. Pour les e2e (nécessite la stack complète) :
   ```bash
   PLAYWRIGHT_NO_WEBSERVER=1 pnpm test:e2e
   ```

## Sécurité (rappel BFF)

- `X-Client-Id` et `X-Api-Key` ne quittent **jamais** le serveur.
- Les sessions sont signées HS256 et stockées dans un cookie
  `HttpOnly` + `SameSite=Lax`.
- En production, mettre `SESSION_SECRET` >= 48 octets aléatoires
  (`openssl rand -base64 48`).
- Aucun mock côté frontend : tous les appels passent par le vrai KSM.

## Déploiement (pré-prod / prod)

### Build Docker (Next.js standalone)

```bash
docker build -t hrm-frontend:latest .
docker run --rm -p 3000:3000 \
  -e KSM_BASE_URL=https://ksm.example.com \
  -e KSM_CLIENT_ID=<client-id> \
  -e KSM_API_KEY=<api-key> \
  -e SESSION_SECRET="$(openssl rand -base64 48)" \
  hrm-frontend:latest
```

L'image (~180 MiB) embarque uniquement `.next/standalone` + `.next/static`
+ `public`. Le serveur écoute sur `0.0.0.0:3000`.

### Variables d'environnement obligatoires en prod

| Variable | Description |
|---|---|
| `KSM_BASE_URL` | URL HTTPS de KSM (ex: `https://ksm.example.com`) |
| `KSM_CLIENT_ID` | `ClientApplication.clientId` enregistré côté KSM |
| `KSM_API_KEY` | `ClientApplication.apiKey` (secret) |
| `SESSION_SECRET` | >= 48 octets aléatoires pour signer le cookie session (HS256) |
| `NODE_ENV` | `production` |

### Checklist pré-prod

- [ ] Cookie session : préfixe `__Host-` activable dès que TLS terminé (HTTPS direct, pas de proxy en clair)
- [ ] `SESSION_SECRET` rotation policy documentée (la rotation invalide tous les sessions actives)
- [ ] CSP headers à ajouter via `next.config.ts` selon politique
- [ ] Reverse proxy : transmettre `X-Forwarded-For` + `X-Forwarded-Proto`
- [ ] Vérifier `/api/health` → `{ ksm: { ok: true } }`
- [ ] Healthcheck Docker : `HEALTHCHECK CMD wget -q -O /dev/null http://localhost:3000/api/health || exit 1`
- [ ] Backup régulier de la base KSM (Postgres) — le frontend est stateless
- [ ] Sessions store en mémoire : sticky sessions OU partager via Redis (TODO si scale-out)

### CI

`.github/workflows/frontend-ci.yml` exécute à chaque push/PR sur `frontend/**` :
- `pnpm install --frozen-lockfile`
- `pnpm lint` + `pnpm typecheck` + `pnpm test` (unit Vitest)
- `docker build` validation (sans push)

Les tests e2e Playwright ne sont **pas** dans la CI car ils requièrent
KSM + Postgres + Redis + ES. Les exécuter localement ou dans un environnement
de pre-merge avec stack complète.

## Statut

Phases 0 → 12 livrées sur la branche de travail.

| Phase | Périmètre | UC |
|---|---|---|
| 0 | Bootstrap Next 16 + Tailwind v4 + tokens + shadcn + i18n + BFF + login | — |
| 1 | Auth multi-tenant KSM, sessions opaques (cookie 340 B) | — |
| 2 | Admin RH — `/employees`, fiche 360°, contrats, dépendants, soldes | UC-01..05 |
| 3 | Self-service — `/leaves/my`, `/loans/my`, `/expenses/my`, profil | UC-09, 11, 22 |
| 4 | Manager — congés pending, mission orders, expenses approve, reviews | UC-10, 15, 20, 21, 22 |
| 5 | Comptable — payroll runs, payslips, loan approval | UC-06, 07, 08, 12 |
| 6 | Responsable Paie — déclarations CNPS/DIPE/IRPP-CAC/FNE/CFC | UC-26 |
| 7 | Recruteur — job offers + kanban candidatures | UC-16, 17, 18 |
| 8 | DRH — trainings, training-budgets, skills, analytics KPI | UC-13, 14, 24, 25, 27 |
| 9 | Médecin du travail — visits & certificates | UC-23 |
| 10 | Contrôleur RH — comparaison KPI + exports CSV | UC-27 |
| 11 | Polish — exports PDF, dark mode, mobile, ⌘K, axe a11y | — |
| 12 | Deploy — Dockerfile standalone, CI GitHub Actions, guide pré-prod | — |

**Tests** : 86/86 e2e Playwright + 10/10 unit Vitest + axe a11y sur 2 pages.
**Coverage** : ≥1 golden path par rôle, ≥1 e2e backend par module via `page.request.post`.
**Lint** : 0 erreurs.
