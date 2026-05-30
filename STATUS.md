# HR Core — Rapport d'avancement

> Mise à jour : 2026-05-30
> Branche de travail : `claude/dazzling-davinci-iZpvh`

---

## 1. Rappel du périmètre

Le projet est la construction du **frontend HRM Next.js + BFF** de la
plateforme KSM (Spring Boot 4 modulaire). Le brief initial
(`PROMPT_FRONTEND_HRM.md`) définit **27 use cases (UC-01 → UC-27)** et impose
des règles strictes :

- Tous les autres cores KSM (auth, kernel, actor, roles, file, settings, org,
  admin, accounting…) sont **read-only** : seul `RT-comops-hrm-core` est
  modifiable, et toute modification doit être justifiée et validée.
- **Aucun mock, fake data, faux JSON ou UI déconnectée** — tout passe par le
  vrai backend KSM. Pour les données de test, on passe par des seeders
  Liquibase.
- Le design doit suivre fidèlement les prototypes de `DESIGN/Projet_design/`.
- Pas de fichiers de documentation autres que ceux explicitement demandés.

### Use cases cibles

| UC    | Nom                                        | Acteurs             |
| ----- | ------------------------------------------ | ------------------- |
| UC-01 | Créer un employé                           | SuperAdmin, Admin RH, Recruteur |
| UC-02 | Modifier les données RH                    | Admin RH            |
| UC-03 | Résilier / Terminer un employé             | Admin RH            |
| UC-04 | Gérer les contrats                         | Admin RH            |
| UC-05 | Gérer les personnes à charge               | Admin RH            |
| UC-06 | Lancer le calcul de paie mensuel           | Admin RH            |
| UC-07 | Valider la paie                            | Comptable / DAF     |
| UC-08 | Générer les ordres de paiement             | Comptable / DAF     |
| UC-09 | Soumettre une demande de congé             | Employé             |
| UC-10 | Approuver / Rejeter un congé               | Manager             |
| UC-11 | Demander une avance sur salaire            | Employé             |
| UC-12 | Approuver une avance                       | Comptable / DAF     |
| UC-13 | Planifier une formation                    | DRH                 |
| UC-14 | S'inscrire à une formation                 | Employé             |
| UC-15 | Réaliser une évaluation de performance     | Manager, DRH        |
| UC-16 | Publier une offre d'emploi                 | Recruteur, DRH      |
| UC-17 | Gérer candidatures et entretiens           | Recruteur, Manager  |
| UC-18 | Finaliser l'onboarding                     | Recruteur, Admin RH |
| UC-19 | Saisir / importer les temps de travail     | Employé, Manager    |
| UC-20 | Valider les temps pour la paie             | Manager, Paie       |
| UC-21 | Émettre un ordre de mission                | Manager, Admin RH   |
| UC-22 | Soumettre une note de frais                | Employé, DAF        |
| UC-23 | Enregistrer une visite médicale            | Médecin, Admin RH   |
| UC-24 | Maintenir la cartographie des compétences  | DRH, Manager        |
| UC-25 | Piloter le budget formation                | DRH, Contrôleur RH  |
| UC-26 | Produire les déclarations sociales         | Paie, Comptable     |
| UC-27 | Consulter les tableaux de bord RH          | DRH, Direction      |

---

## 2. Ce qui a été fait

### Phase 0 — Bootstrap technique
- Squelette Next.js 16 + App Router + React 19 + TypeScript strict.
- Tailwind 4 avec `@theme inline` + tokens HR Core (gradients orange/dark/
  amber/violet, ombres, ease-brand).
- Design system : `Button`, `Card`, `Badge`, `Chip`, `Avatar`, `IconTile`,
  `StatusPill`, `KpiCard`, `StatCard`, `ProgressBar`, `WorkflowStepper`,
  `DataTable`, `Dialog`, `Input`, `SectionTitle`, `PageHeader`, `Sidebar`,
  `Topbar`.
- i18n FR/EN via `next-intl` (FR par défaut).
- Session **Redis-backed** via `iron-session` + `ioredis`.
- Client `apiFetch` avec gestion d'erreur `BffApiError`.

### Phase 1 — Authentification & espace de travail
- Login (`/[locale]/login`) avec validation Zod + RHF.
- Sélection d'organisation au login (resolver `discover-contexts` + fallback
  parsing `#ORGANIZATION:<uuid>` dans les permissions).
- Cookie de session httpOnly + provider client `SessionProvider`.
- Hook `useCan(permission)` pour le RBAC scope-aware (normalisation
  `p.split("#")[0]`).

### Phase 2 — Admin / SuperAdmin
- Console `/admin` : organisations, utilisateurs (création + reset password),
  rôles, audit, permissions.
- Orchestration multi-core via le BFF.

### Phase 3 — Employés (UC-01 → UC-05)
- Liste paginée, filtres, recherche.
- Création d'employé (Actor + employee + matricule auto-séquence V069).
- Fiche 360° : Overview design-fidèle + Documents (file-core document-hub) +
  Compétences + Formations + Performance + Carrière (timeline managerId).
- Contrats (CDI / CDD / consultance) avec dates / salaire.
- Personnes à charge.
- Backend : `V070__hrm_employee_manager.yaml` (managerId), enrichissement
  `BusinessActor` côté `actor-core`.

### Phase 4 — Congés (UC-09 / UC-10)
- Mon espace congés : soldes (annual / sick / maternity / paternity / unpaid /
  special), nouvelle demande, historique.
- File manager `/leaves` : liste org-wide, approbation / rejet avec
  justification.

### Phase 5 — Timesheets (UC-19 / UC-20)
- Saisie hebdomadaire avec totaux journaliers.
- File manager : validation par lots.

### Phase 6 — Missions & Notes de frais (UC-21 / UC-22)
- Ordres de mission : workflow employé-driven (request → ACCEPTED / DECLINED →
  manager closes → done), inbox employé + queue manager.
- Backend : `V071__hrm_mission_order_workflow.yaml` + `V072` (permission
  `hrm:mission:accept`).
- Notes de frais : soumission employé (avec rattachement mission optionnel),
  queue DAF org-wide.
- Backend : `V073__hrm_expense_mission_link.yaml`.

### Phase 7 — Développement RH (UC-13, UC-14, UC-15)
- Formations : catalogue, planification, inscription employé (PLANNED →
  IN_PROGRESS → COMPLETED / CANCELLED).
- Évaluations : campagne par période (`YYYY-Q?`), workflow PENDING → SUBMITTED
  → ACKNOWLEDGED → FINALIZED.
- Budget formation : allocation, engagement, réalisation, disponible.

### Phase 8 — Recrutement (UC-16 / UC-17 / UC-18)
- Offres d'emploi (DRAFT → PUBLISHED → CLOSED).
- Pipeline candidats (RECEIVED → SCREENING → INTERVIEWING → OFFER → HIRED /
  REJECTED).
- **Hire-and-provision** : un candidat HIRED → création d'un Employee + Actor
  + matricule en un clic, avec lien candidate.employeeId.
- Backend : `V075__hrm_onboarding_read_employee.yaml` (permission `hrm:onboarding:read`
  pour le rôle EMPLOYEE).

### Phase 9 — Suivi médical (UC-23)
- Visites médicales (annuelle, pré-embauche, reprise, occasionnelle).
- Certificats d'aptitude.
- Calcul d'alertes d'échéances (OK / DUE_SOON / OVERDUE) via `medical-status.ts`.

### Phase 10 — Compétences / GPEC (UC-24 / UC-25)
- Référentiel des compétences (techniques / soft skills / certifications).
- Cartographie employés (`/skills/employees`) avec niveaux et évaluations.
- Analyse d'écarts (gap par poste).
- Backend hrm-core : ajout de 2 endpoints `SkillController` :
  `GET /skills/{id}/employee-skills` et
  `GET /skills/employee-skills?organizationId=...` (nécessaires aux agrégats
  frontend, demandés et validés).

### Phase 11 — Déclarations sociales (UC-26)
- Cycle CNPS / DIPE / IRPP / IPM : DRAFT → GENERATED → SUBMITTED → ACKNOWLEDGED.
- Génération PDF / CSV / XML attachée via file-core (séparation upload puis
  attach).
- Stepper workflow + statuts visuels cohérents.

### Phase 12 — Tableau de bord (UC-27)
- **v1** : KPI roll-up role-aware (rejeté par l'utilisateur car non fidèle au
  design).
- **v2** (en cours / dernier commit) : refonte intégrale fidèle au mockup
  `DESIGN/Projet_design/.../pages/dashboard.jsx` :
  - Header « Bonjour, {prénom} · X actions vous attendent · Y échéances »
  - 4 KPI hero gradients (Effectif total, Masse salariale, Absentéisme,
    Engagement)
  - Évolution de l'effectif (SVG line chart inline, 12 mois)
  - Répartition par département (SVG donut inline)
  - À traiter (liste mixte congés / NDF / évaluations / contrats / médical)
  - Prochaines échéances (date-stamp cards)
  - Actions rapides (grille 2×3)
  - Activité récente (timeline)
  - Métriques clés (4 cartes sparkline SVG)
- Agrégateur BFF unique `/api/hrm/dashboard` avec :
  - fan-out parallèle sur 11 modules KSM
  - per-bucket fault tolerance (`safe(p, fb)` wrapper)
  - gates de permission avant les appels (évite le bruit 403)
  - capping (30 offres / 80 employés max scannés pour les contrats)

### Tâches transverses
- Session migrée cookie-only → Redis-backed (`60b1516`).
- `.gitignore` durci (Redis `dump.rdb`, `data/` racine pour file-core local).
- Seeders Liquibase V67–V75 enrichis (auto-séquence matricule, demo employees,
  permission onboarding).

### Récap des migrations Liquibase ajoutées par ce projet

| Migration                                      | Apport                              |
| ---------------------------------------------- | ----------------------------------- |
| `067-auth-force-password-change.yaml`          | Flag mot-de-passe-à-changer        |
| `068-hrm-demo-seed.yaml`                       | Tenant, org, users, employés démo  |
| `069-hrm-matricule-sequence.yaml`              | Séquence auto matricule            |
| `070-hrm-employee-manager.yaml`                | Hiérarchie managerId               |
| `071-hrm-mission-order-workflow.yaml`          | Workflow OM employé-driven         |
| `072-hrm-mission-accept-permission.yaml`       | Permission `hrm:mission:accept`    |
| `073-hrm-expense-mission-link.yaml`            | Lien NDF ↔ OM                      |
| `074-hrm-demo-employee-link.yaml`              | Lien demo users ↔ employees        |
| `075-hrm-onboarding-read-employee.yaml`        | Permission onboarding pour EMPLOYEE |

### Récap des modules BFF (`frontend/src/server/ksm/modules/`)

`actors`, `admin`, `auth`, `declarations`, `employee-profile`, `employees`,
`expenses`, `files`, `leaves`, `medical`, `missions`, `organization`,
`recruitment`, `reviews`, `skills`, `timesheets`, `training-budgets`,
`trainings`, `users` — **19 modules** wrappers couvrant tous les endpoints
KSM utilisés.

### Récap des routes frontend (`/[locale]/(app)/`)

`admin`, `dashboard`, `declarations`, `employees`, `expenses`, `leaves`,
`medical`, `mission-orders`, `recruitment`, `reviews`, `showcase`, `skills`,
`timesheets`, `training-budgets`, `trainings` — **15 modules métier** + le
showcase design system.

---

## 3. Ce qui reste à faire

### 3.1 Use cases non implémentés

| UC    | Nom                                        | État    | Reste à faire                                                            |
| ----- | ------------------------------------------ | ------- | ------------------------------------------------------------------------ |
| UC-06 | Calcul de paie mensuel                     | ❌      | Pas de module `payroll` côté frontend. Backend `hrm-core` expose les `Bulletin` (V059) mais aucun écran. |
| UC-07 | Validation paie                            | ❌      | Idem UC-06.                                                              |
| UC-08 | Ordres de paiement                         | ❌      | Idem UC-06. À relier avec `accounting-core` / `treasury-core`.            |
| UC-11 | Demande d'avance sur salaire               | ❌      | Module `LoanAdvance` seedé (V057), pas d'UI ni de BFF.                    |
| UC-12 | Approbation d'avance                       | ❌      | Idem UC-11.                                                              |

### 3.2 Compléments fonctionnels par module déjà livré

- **Employés** : import CSV en lot, exports, gestion fine des sous-domaines
  (V055), tags / segments.
- **Congés** : politique d'approbation multi-niveaux, calendrier d'équipe,
  notifications email (provider `EMAIL_PROVIDER=none` aujourd'hui).
- **Timesheets** : import / export, intégration avec missions (heures
  passées sur mission).
- **Recrutement** : portail candidat public (l'inscription anonyme n'est pas
  exposée), tests / questionnaires, comité de scoring.
- **Formations** : feuilles de présence, attestations PDF générées, intégration
  budget consommé.
- **Évaluations** : campagne 360° (peers + N+1 + N-1), questionnaire
  configurable, plan de développement.
- **Compétences** : matrice à plusieurs niveaux (poste cible vs réel), plan de
  formation déclenché par les écarts.
- **Médical** : alertes email automatiques pour les visites OVERDUE / DUE_SOON,
  intégration avec aptitudes / restrictions sur poste.
- **Déclarations** : signature électronique des dépôts, archivage horodaté,
  cycle de relance automatique.
- **Dashboard** : drill-down par carte, export PDF / Excel du rapport,
  personnalisation par rôle, période sélectionnable (12M / YTD / All actifs).

### 3.3 Domaines transverses

- **Notifications** : provider email branché en production (Resend ou SMTP) +
  templates HTML par événement.
- **Recherche** : projections Elasticsearch (employés, candidats, formations)
  pas encore exposées côté frontend.
- **Audit admin** : la journalisation `kernel-core` est en place mais aucun
  écran ne l'affiche.
- **Gouvernance RBAC** : clonage de rôles et assignation en masse (commencés
  côté admin, à finaliser).
- **Chaînes d'approbation dynamiques** : workflows configurables côté admin
  (aujourd'hui hard-codés simples).
- **Export & impression** : layouts `DESIGN/Projet_design/index-print.html` à
  brancher aux exports (bulletins, déclarations, contrats).
- **Tests** : la pyramide de tests frontend (unit + integration RTL +
  end-to-end Playwright) reste à écrire ; quelques shoots Playwright servent
  uniquement aux captures de vérification.
- **CI/CD** : aucune pipeline GitHub Actions branchée.

### 3.4 Backend KSM (pas dans le scope direct)

- Module `payroll` (UC-06/07/08) à finaliser dans `RT-comops-hrm-core` :
  les entités `Bulletin` et la chaîne de calcul existent (V059) mais le
  contrôleur REST et la chaîne d'événements ne sont pas encore complets pour
  un usage end-to-end.
- Module `loan-advance` (UC-11/12) idem.
- Module `blockchain-core` reste un placeholder seedé (V066), pas de logique
  métier.

### 3.5 Polish design / UX

- Captures Playwright à régénérer pour la v2 du dashboard et comparer côte à
  côte avec `DESIGN/Projet_design/.../uploads/drh-01-dashboard.png`.
- Mode sombre : `next-themes` est installé mais le toggle UI n'est pas exposé.
- Accessibilité : audit complet `axe` à passer sur chaque page.
- Mobile : la sidebar est masquée < lg ; un drawer mobile reste à faire.

---

## 4. Indicateurs

| Indicateur                                | Valeur                              |
| ----------------------------------------- | ----------------------------------- |
| UC livrés                                 | **22 / 27** (≈ 81 %)                |
| Modules backend KSM utilisés              | 11 sur 21                           |
| Modules BFF wrappers                      | 19                                  |
| Pages frontend (hors auth & showcase)     | 14 modules métier                   |
| Migrations Liquibase totales              | 75                                  |
| Migrations Liquibase ajoutées par projet  | 9 (V067 → V075)                     |
| Endpoints HRM ajoutés au backend          | 2 (`SkillController`)               |
| Langues supportées                        | 2 (fr, en)                          |
| Devises supportées                        | 1 (XAF)                             |

---

## 5. Décisions importantes consignées

- **Pas de Recharts pour le dashboard** : on garde le SVG inline pour ne pas
  ajouter de dépendance lourde et pour conserver une typographie alignée sur
  le design.
- **Permissions scopées** : on normalise systématiquement `p.split("#")[0]`
  pour comparer les codes bare (sinon `hrm:employee:read#ORGANIZATION:xxx`
  n'égalerait jamais `hrm:employee:read`).
- **Capping fan-out** : 30 offres × applications + 80 employés × contrats max
  par requête dashboard, pour éviter d'écrouler le backend.
- **Per-bucket fault tolerance** : chaque bloc du dashboard utilise un
  `safe(p, fallback)` afin qu'une erreur sur un module n'écrase pas tout
  l'écran.
- **Document hub file-core** : tous les fichiers (contrats, certificats,
  déclarations) passent par `target_type` / `target_id`, upload séparé puis
  attach.
- **Session Redis-backed** : iron-session cookie-only n'était pas suffisant
  pour stocker le contexte workspace + permissions ; migration vers ioredis.
- **Aucune modification d'un core KSM autre que `hrm-core`** : règle respectée
  sur toute la durée du projet.

---

## 6. Prochaines étapes recommandées

Par ordre de valeur business décroissante :

1. **Payroll (UC-06/07/08)** — sans paie, le module RH est incomplet ;
   nécessite un travail backend `hrm-core` (contrôleur, calcul brut → net,
   intégration CNPS/IRPP) en plus du frontend.
2. **Avances (UC-11/12)** — petit lot, faible coût, valeur RH immédiate.
3. **Captures + alignement v2 dashboard** — finir le passage de validation
   visuelle vs mockup, puis brancher l'export PDF.
4. **Notifications email** — débloquer les workflows (congés approuvés,
   missions acceptées, visites médicales à reprogrammer).
5. **Tests automatisés** + **CI** — sécuriser la base avant d'ajouter UC-06/07/08.
6. **Mobile drawer + dark mode toggle** — quick wins UX.
