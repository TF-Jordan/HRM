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
cp .env.example .env.local
# Générer un secret de session (32+ octets) :
echo "SESSION_SECRET=$(openssl rand -hex 32)" >> .env.local

# 3) Démarrer en dev (port 3000, HMR)
npm run dev

# … ou build + start production :
npm run build
npm start
```

**Vérification** : ouvrir <http://localhost:3000/fr/login>. Comptes de démo
seedés par `V068__hrm_demo_seed.yaml` (mot de passe commun : `Demo@2024!`) :

| Email                          | Rôle               |
| ------------------------------ | ------------------ |
| `super.admin@hrcore.demo`      | SuperAdmin tenant  |
| `admin.rh@hrcore.demo`         | Admin RH           |
| `manager@hrcore.demo`          | Manager            |
| `employee@hrcore.demo`         | Employé            |
| `recruiter@hrcore.demo`        | Recruteur          |
| `accountant@hrcore.demo`       | Comptable / DAF    |
| `drh@hrcore.demo`              | DRH                |
| `doctor@hrcore.demo`           | Médecin du travail |

### 2.4 Scripts NPM exposés

| Commande               | Effet                                    |
| ---------------------- | ---------------------------------------- |
| `npm run dev`          | Next.js dev server (HMR, port 3000)      |
| `npm run build`        | Build production                         |
| `npm start`            | Serveur production (après `build`)       |
| `npm run typecheck`    | `tsc --noEmit` strict                    |
| `npm run lint`         | ESLint                                   |
| `npm run openapi:generate` | Régénère les types KSM depuis `iwm-openapi.json` |

### 2.5 Scripts Maven utiles

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
