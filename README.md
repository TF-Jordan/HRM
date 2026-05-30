# HR Core — Plateforme HRM SaaS multi-tenant

> Module RH (paie, congés, missions, recrutement, formation, médical,
> déclarations sociales) de la plateforme **KSM (RT-Comops Backend)**, taillé
> pour le contexte camerounais (CNPS, DIPE, IRPP, FCFA, fr/en).
> Backend Spring Boot 4 modulaire réactif, frontend Next.js 16 BFF.

```
┌────────────────────────┐    HTTPS    ┌──────────────────────────┐
│  Frontend Next.js 16   │  ─────────▶ │   BFF Route Handlers     │
│  React 19 · Tailwind 4 │             │   (Next.js server)       │
└────────────────────────┘             └────────────┬─────────────┘
                                                    │ X-Client-Id
                                                    │ X-Api-Key
                                                    │ Bearer JWT
                                                    ▼
                              ┌────────────────────────────────────┐
                              │  KSM Spring Boot 4 (port 8080)     │
                              │  ┌───────────┬───────────────────┐ │
                              │  │ hrm-core  │ auth · roles      │ │
                              │  │ employees │ kernel · actor    │ │
                              │  │ leaves    │ file · settings   │ │
                              │  │ missions  │ org · admin       │ │
                              │  │ …         │ accounting · …    │ │
                              │  └───────────┴───────────────────┘ │
                              └─────┬────────────┬──────────┬──────┘
                                    ▼            ▼          ▼
                              PostgreSQL 18   Redis 8    Kafka 8
                              (port 5432)   (port 6379) (port 9092)
```

---

## 1. Prérequis

| Outil          | Version | Vérification           |
| -------------- | ------- | ---------------------- |
| **JDK**        | 21      | `java -version`        |
| **Maven**      | 3.9+    | `mvn -v`               |
| **Node.js**    | 20+     | `node -v`              |
| **npm**        | 10+     | `npm -v`               |
| **PostgreSQL** | 16–18   | `psql --version`       |
| **Redis**      | 7–8     | `redis-server --version` |

> Kafka et Elasticsearch sont **optionnels** en dev : l'outbox tombe en mode
> `inMemory` et la santé Elasticsearch est désactivable via env.

---

## 2. Mise en route express (sans Docker)

### 2.1 Infrastructure locale

```bash
# Postgres — créer la base iwm avec l'utilisateur iwm/iwm
sudo -u postgres psql <<'SQL'
CREATE USER iwm WITH PASSWORD 'iwm';
CREATE DATABASE iwm OWNER iwm;
GRANT ALL PRIVILEGES ON DATABASE iwm TO iwm;
SQL

# Redis — démarrer en arrière-plan (port 6379)
redis-server --daemonize yes
```

### 2.2 Backend KSM (JVM)

```bash
cd KSM

# Build complet (compile les 21 modules, lance les tests désactivés)
mvn -DskipTests clean install

# Lancer le bootstrap (port 8080)
# Profile r2dbc actif par défaut — Liquibase exécute V001…V075 au démarrage.
mvn -pl RT-comops-bootstrap -am spring-boot:run

# … ou en jar packagé :
java -jar RT-comops-bootstrap/target/RT-comops-bootstrap-*.jar
```

Variables d'environnement utiles (toutes ont une valeur par défaut adaptée au
poste de dev) :

```bash
export IWM_R2DBC_URL=r2dbc:postgresql://localhost:5432/iwm
export IWM_R2DBC_USERNAME=iwm
export IWM_R2DBC_PASSWORD=iwm
export IWM_LIQUIBASE_URL=jdbc:postgresql://localhost:5432/iwm
export IWM_REDIS_HOST=localhost
export IWM_REDIS_PORT=6379
export IWM_JWT_AUTO_GENERATE_KEY_PAIR=true    # dev only
export IWM_ELASTICSEARCH_HEALTH_ENABLED=false # si pas d'ES local
export IWM_OUTBOX_RELAY_ENABLED=false         # si pas de Kafka local
```

**Vérification** : `curl -s http://localhost:8080/actuator/health | jq` doit
renvoyer `"status": "UP"`.

### 2.3 Frontend BFF Next.js

```bash
cd frontend

# 1) Installer les dépendances
npm install

# 2) Configurer l'environnement
# Option A — utiliser le .env racine déjà configuré (cf. § 2.5)
ln -s ../.env .env.local

# Option B — un .env.local indépendant pour le front
cp .env.example .env.local
# Générer un secret de session (32+ octets) :
sed -i "s|^SESSION_SECRET=.*|SESSION_SECRET=$(openssl rand -hex 32)|" .env.local

# 3) Démarrer en dev (port 3000, HMR)
npm run dev

# … ou build + start production :
npm run build
npm start
```

**Vérification** : ouvrir <http://localhost:3000/fr/login>.

#### Comptes de démo seedés (V068 + V076)

Tous partagent le même mot de passe **`Demo@2024!`** (BCrypt strength 10).
Au premier login, l'application demande une organisation de travail
(`MUFID Union` est la seule seedée pour la démo).

| Email                          | Rôle             | Périmètre des permissions                     |
| ------------------------------ | ---------------- | --------------------------------------------- |
| `super.admin@hrcore.demo`      | SuperAdmin       | TENANT — tout                                 |
| `hr.admin@hrcore.demo`         | Admin RH         | ORG — RH complet + création de comptes / rôles |
| `drh@hrcore.demo`              | DRH              | ORG — pilotage, formation, évaluations         |
| `manager@hrcore.demo`          | Manager          | ORG — équipe, approbations congés / missions  |
| `recruiter@hrcore.demo`        | Recruteur        | ORG — recrutement + onboarding                |
| `accountant@hrcore.demo`       | Comptable / DAF  | ORG — notes de frais, validation paie         |
| `payroll@hrcore.demo`          | Payroll Manager  | ORG — calcul paie + déclarations sociales     |
| `doctor@hrcore.demo`           | Médecin du travail | ORG — visites, certificats                  |
| `employee@hrcore.demo`         | Employé          | ORG — self-service (congés, missions, paie)   |

En plus de ces comptes, V076 crée **11 fiches employés réelles** (5 liées
aux comptes ci-dessus + 6 « orphelines » sans login) avec leurs contrats
CDI/CDD actifs, prêtes pour tester la liste des employés, le moteur de paie
et le tableau de bord.

### 2.4 Workflow : créer un nouvel employé et lui permettre de se connecter

L'écran **`/employees/new`** (accessible à SuperAdmin et Admin RH) crée
l'employé via une orchestration multi-cores en 5 étapes :

1. `POST /api/actors` — identité humaine dans actor-core.
2. `POST /api/v1/hrm/employees` — fiche employé + contrat initial dans hrm-core,
   matricule auto-séquentiel `EMP-…` généré par V069.
3. `POST /api/auth/register` — compte utilisateur dans auth-core avec un
   **mot de passe temporaire** généré par le BFF (`forcePasswordChange = true`).
4. `POST /api/administration/users/{id}/roles` — rôle `EMPLOYEE` assigné sur
   l'organisation courante.
5. **Envoi de l'email de bienvenue** par le BFF (provider défini par
   `EMAIL_PROVIDER`, sender défini par `EMAIL_FROM`, cf. § 2.5).

Le nouvel employé reçoit alors un email avec :
- son **email de connexion**,
- son **mot de passe temporaire**,
- le **lien vers `/fr/login`**.

Au premier login il sera invité à changer son mot de passe (écran
`/[locale]/change-password`).

> Les étapes 3 → 5 sont *best-effort* : si l'orchestration échoue partiellement
> (par exemple SMTP indisponible) la fiche employé reste créée et un
> avertissement remonte dans le toast UI. L'Admin RH peut alors relancer la
> création du compte ou renvoyer l'email depuis `/admin/users`.

### 2.5 Variables d'environnement & email sender

Un fichier **`.env.example`** est fourni à la racine du dépôt. Copiez-le en
`.env`, ajustez-le, et il sera lu à la fois par KSM et par le BFF :

```bash
cp .env.example .env
sed -i "s|^SESSION_SECRET=.*|SESSION_SECRET=$(openssl rand -hex 32)|" .env

# Lancer KSM avec ces variables :
set -a; source .env; set +a
mvn -pl RT-comops-bootstrap -am spring-boot:run
```

Le `.env` contient des blocs commentés pour :

| Bloc                | Variables clés                                   |
| ------------------- | ------------------------------------------------ |
| Postgres            | `IWM_R2DBC_URL`, `IWM_R2DBC_USERNAME`, `IWM_R2DBC_PASSWORD`, `IWM_LIQUIBASE_URL` |
| Redis               | `IWM_REDIS_HOST`, `IWM_REDIS_PORT`               |
| Elasticsearch/Kafka (option.) | `IWM_ELASTICSEARCH_URIS`, `IWM_KAFKA_BOOTSTRAP_SERVERS`, `IWM_OUTBOX_RELAY_ENABLED` |
| JWT                 | `IWM_JWT_AUTO_GENERATE_KEY_PAIR`, `IWM_JWT_KEY_ID` |
| BFF ↔ KSM           | `KSM_BASE_URL`, `KSM_CLIENT_ID`, `KSM_API_KEY`   |
| Session             | `SESSION_SECRET`, `SESSION_TTL_SECONDS`, `SESSION_COOKIE_NAME` |
| **Email**           | `EMAIL_PROVIDER`, `EMAIL_FROM`, `EMAIL_REPLY_TO`, `SMTP_*`, `RESEND_API_KEY` |
| Public Next.js      | `NEXT_PUBLIC_APP_URL`, `NEXT_PUBLIC_DEFAULT_LOCALE`, `NEXT_PUBLIC_DEFAULT_CURRENCY` |

#### Adresse expéditrice des emails (CRITIQUE pour tester l'envoi)

> **C'est `EMAIL_FROM` qui définit le « De: » visible par vos employés.**
> Renseignez-la dans `.env` (ou `frontend/.env.local`) avec le format
> `"Nom affiché <inbox@domaine>"`.

Choix du provider via `EMAIL_PROVIDER` :

- **`none`** (par défaut) — l'envoi est *simulé* : le mailer écrit le contenu
  dans la console (`[MAILER · NONE]`) et dans les logs Pino. Idéal pour tester
  l'UI sans déranger une boîte mail. Vous voyez le mot de passe temporaire
  directement dans la console.
- **`smtp`** — vrai envoi via `nodemailer`. Renseigner :
  ```ini
  EMAIL_PROVIDER=smtp
  SMTP_HOST=smtp.gmail.com         # ou Mailtrap, AWS SES, SendGrid…
  SMTP_PORT=587
  SMTP_SECURE=false                # true pour le port 465 (SSL)
  SMTP_USER=votre-compte@gmail.com
  SMTP_PASS=votre-mot-de-passe-application
  EMAIL_FROM="HR Core <votre-compte@gmail.com>"
  ```
  > Pour Gmail : créez un *mot de passe d'application* (2FA requis).
  > Pour Mailtrap (test sandbox) : `SMTP_HOST=sandbox.smtp.mailtrap.io`,
  > `SMTP_PORT=2525`, et utilisez les credentials affichés dans Mailtrap.
- **`resend`** — vrai envoi via [Resend.com](https://resend.com) (free tier
  généreux). Renseigner :
  ```ini
  EMAIL_PROVIDER=resend
  RESEND_API_KEY=re_xxxxxxxxxxxxx
  EMAIL_FROM="HR Core <noreply@votre-domaine-verifie.com>"
  ```
  > **Important** : le domaine de `EMAIL_FROM` doit être *vérifié* dans
  > Resend (DNS DKIM + SPF), sinon le mail est rejeté.

`EMAIL_REPLY_TO` (optionnel) définit le `Reply-To:` si vous voulez que les
réponses arrivent ailleurs que sur l'adresse expéditrice (ex. support RH).

### 2.6 Scripts NPM exposés

| Commande               | Effet                                    |
| ---------------------- | ---------------------------------------- |
| `npm run dev`          | Next.js dev server (HMR, port 3000)      |
| `npm run build`        | Build production                         |
| `npm start`            | Serveur production (après `build`)       |
| `npm run typecheck`    | `tsc --noEmit` strict                    |
| `npm run lint`         | ESLint                                   |
| `npm run openapi:generate` | Régénère les types KSM depuis `iwm-openapi.json` |

### 2.7 Scripts Maven utiles

| Commande                                        | Effet                          |
| ----------------------------------------------- | ------------------------------ |
| `mvn -DskipTests clean install`                 | Build complet 21 modules       |
| `mvn -pl RT-comops-bootstrap -am spring-boot:run` | Lancement avec hot-reload Spring |
| `mvn -pl RT-comops-hrm-core test`               | Tests d'un seul core           |
| `mvn -pl RT-comops-bootstrap -am verify`        | Build + tests + intégration    |

---

## 3. Arborescence

```
HRM/
├── KSM/                              ← Backend Spring Boot (Java 21)
│   ├── RT-comops-kernel-core/        ← Multi-tenancy, outbox, audit, quotas
│   ├── RT-comops-common-core/        ← Types partagés, catalogue services
│   ├── RT-comops-actor-core/         ← Identités humaines (BusinessActor)
│   ├── RT-comops-organization-core/  ← Tenants, organisations, agences
│   ├── RT-comops-auth-core/          ← JWT RS256, login, self-service
│   ├── RT-comops-roles-core/         ← RBAC multi-scope
│   ├── RT-comops-administration-core/← Permissions, gouvernance, audit admin
│   ├── RT-comops-settings-core/      ← Réglages hiérarchiques, séquences
│   ├── RT-comops-file-core/          ← Document hub (target_type / target_id)
│   ├── RT-comops-hrm-core/           ← ★ Domaine RH (cible du projet)
│   ├── RT-comops-product-core/       ← Catalogue produits (hors HRM)
│   ├── RT-comops-inventory-core/     ← Stocks (hors HRM)
│   ├── RT-comops-resource-core/      ← Ressources matérielles (hors HRM)
│   ├── RT-comops-tp-core/            ← Tiers (hors HRM)
│   ├── RT-comops-sales-core/         ← Ventes (hors HRM)
│   ├── RT-comops-accounting-core/    ← Comptabilité (hors HRM)
│   ├── RT-comops-treasury-core/      ← Trésorerie (hors HRM)
│   ├── RT-comops-cashier-core/       ← Caisses (hors HRM)
│   ├── RT-comops-billing-core/       ← Facturation services (hors HRM)
│   ├── RT-comops-blockchain-core/    ← Placeholder
│   └── RT-comops-bootstrap/          ← Exécutable agrégé + Liquibase
│
├── frontend/                         ← Frontend Next.js 16 + BFF
│   ├── src/app/[locale]/(auth)/      ← Login, mot de passe
│   ├── src/app/[locale]/(app)/       ← Modules métier (dashboard, employés…)
│   ├── src/app/api/hrm/              ← BFF Route Handlers
│   ├── src/server/ksm/modules/       ← Wrappers KSM (19 modules)
│   ├── src/components/               ← UI primitives + features
│   └── src/i18n/messages/{fr,en}/    ← Traductions
│
├── DESIGN/                           ← Mockups HTML + screenshots
│   └── Projet_design/                ← Prototypes statiques de référence
│
├── conception/                       ← Diagrammes legacy
├── ANALYSE_KSM_HRM.md                ← Analyse fonctionnelle initiale
├── PROMPT_FRONTEND_HRM.md            ← Brief de construction du frontend
└── STATUS.md                         ← Rapport d'avancement (fait / reste à faire)
```

---

## 4. Architecture en bref

### Sécurité multi-tenant (5 couches sur `/api/**`)

1. **ClientApplication** : `X-Client-Id` + `X-Api-Key` (secrets serveur, injectés
   par le BFF, jamais exposés au navigateur).
2. **ClientApplication → service** : filtre sur préfixes (`/api/v1/hrm/*` →
   service `HRM`).
3. **Quota backend Redis** par `(tenantId, clientId, serviceCode, bucket)`.
4. **Organization → service** : abonnement actif HRM requis.
5. **Quota métier Redis** par `(tenantId, organizationId, serviceCode, bucket)`.
6. **RBAC utilisateur** : permissions `hrm:<resource>:<action>` (optionnellement
   scopées `#ORGANIZATION:<uuid>`).

### Modèle de données

- PostgreSQL 18, schéma `public` pour le métier, schéma `kernel` pour l'audit /
  outbox / projections.
- Migrations Liquibase **V001 → V075** (75 changelogs), exécutées
  automatiquement par `RT-comops-bootstrap` au démarrage.
- Outbox pattern (BusinessEvent) → relayage Kafka (ou inMemory en dev).

### BFF (Next.js Route Handlers)

- Les routes côté navigateur appellent uniquement le BFF (`/api/hrm/*`).
- Le BFF stocke la session côté serveur via `iron-session` + Redis.
- Aucun secret KSM ne transite par le client.

---

## 5. Tests rapides

```bash
# Backend
cd KSM && mvn -DskipTests clean install
curl -s http://localhost:8080/actuator/health | jq

# Frontend
cd frontend
npm run typecheck
npm run build
```

---

## 6. Documents projet

- **`STATUS.md`** — État d'avancement détaillé (phases livrées, restant à faire).
- **`ANALYSE_KSM_HRM.md`** — Analyse exhaustive du backend hrm-core.
- **`PROMPT_FRONTEND_HRM.md`** — Brief de construction (UC, design system, BFF).
- **`KSM/ARCHITECTURE.md`** — Architecture détaillée du backend.
- **`KSM/iwm-openapi.json`** — Source de vérité des contrats API.
- **`DESIGN/Projet_design/`** — Prototypes statiques de référence.

---

## 7. Licence & contact

Projet interne. Aucune license publique pour le moment.
