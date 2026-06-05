# PROMPT — Claude Design : Frontend complet HRM + Payroll

## TON RÔLE

Tu conçois le **frontend complet** d'une plateforme SaaS multi-tenant qui couvre **la gestion RH (`hrm-core`) et la paie (`payroll-core`)**. Le backend Spring Boot existe déjà ; je te le donne en contexte. **Ton design doit épouser exactement ce backend** — c'est non négociable. Si une donnée n'est pas exposée par un endpoint, **elle ne peut pas apparaître** sur un écran. Si un champ n'existe pas sur le modèle backend, **il ne peut pas apparaître** dans un formulaire. Tu n'inventes rien.

Cette règle évite que l'équipe backend doive retoucher quoi que ce soit pendant que le frontend est codé : ils continuent leur roadmap, le frontend se branche, ça marche.

**Cible** : entreprises camerounaises (CEMAC/OHADA), locale **français principal** (EN secondaire), devise **XAF**.

---

## RÈGLE D'OR (à relire à chaque écran)

> Avant de placer un widget, une carte, une colonne de tableau ou un champ de formulaire : **trace-le à un endpoint REST réel et à un champ existant**. Si tu ne trouves pas, **supprime-le**. Pas de "ce serait bien d'afficher…" — soit la donnée vient du backend tel qu'il est, soit elle n'apparaît pas.

Tu peux te référer aux endpoints listés plus bas. Pour les types de retour exacts, lis les controllers Java (`RT-comops-hrm-core/src/main/java/yowyob/comops/api/hrm/adapter/in/web/*Controller.java` et `RT-comops-payroll-core/src/main/java/yowyob/comops/api/payroll/adapter/in/web/*Controller.java`) et les domain models (`domain/model/*.java`) qui te sont donnés en contexte.

---

# 1. DESIGN SYSTEM (verbatim — copie ces tokens, n'en invente pas)

C'est le design system existant. Tu **n'en sors pas**. Pas de nouvelle teinte, pas de nouvelle famille de typo, pas de nouvelle ombre. Tu peux **composer** avec ce qui est là, c'est tout.

## 1.1 Couleurs

### Brand orange (couleur signature, identité)
```
--color-orange-50:  #fff1e6
--color-orange-100: #ffdcc2
--color-orange-200: #ffc299
--color-orange-300: #ffa366
--color-orange-400: #fb8533
--color-orange-500: #f26b0f   ← couleur principale (CTA, accent)
--color-orange-600: #dc560a   ← hover
--color-orange-700: #b0420a
--color-orange-800: #7a2d08
```

**Usage de l'orange** :
- Boutons primaires (CTA principal d'une page : « Lancer la paie », « Soumettre », « Approuver »)
- Liens actifs dans la sidebar (item sélectionné)
- Highlights de KPI critiques (l'effectif, le net du mois, la masse salariale)
- Sparklines / mini-graphes principaux
- Le bandeau "hero" du dashboard de chaque rôle peut avoir un dégradé orange (`--grad-orange`)

**N'utilise PAS l'orange** pour :
- Les fonds de page (utilise `--color-bg`)
- Les textes courants (utilise `--color-ink`)
- Plusieurs actions concurrentes sur la même page (1 CTA primaire à la fois)

### Neutres chauds (fond, surfaces)
```
--color-bg:      #f8f5ef   ← fond global de la page
--color-bg-2:    #f1ece0   ← variante (pour zonings alternés)
--color-bg-elev: #ffffff   ← cards, surfaces élevées, drawers, modales
--color-bg-soft: #f2ebdb   ← hover lignes de tableau, headers sticky
--color-bg-dim:  #efe7d2   ← fond de sections déprimées (empty state)
```

### Surfaces sombres (rares)
```
--color-dark:      #110d08   ← sidebar du workspace Paie en mode "outil pro"
--color-dark-2:    #1c1610
--color-dark-3:    #2b2218
--color-dark-line: #3a2f22
```

> Utilisation suggérée des sombres : **bandeaux de bulletin de paie**, **cartes "net à payer"** sur le bulletin (très lisible), **footer signature électronique** des documents. Pas la sidebar HRM standard, qui reste claire.

### Lignes / séparateurs
```
--color-line:        #e8dec6   ← séparateurs principaux
--color-line-soft:   #f0e8d4   ← séparateurs très discrets (dans cards)
--color-line-strong: #d5c8aa   ← cadres / borders d'inputs
```

### Encre (textes)
```
--color-ink:   #0f0b05   ← texte principal (titres, libellés forts)
--color-ink-2: #2e281e   ← texte courant
--color-ink-3: #6b6253   ← texte secondaire (sous-titres, métadonnées)
--color-ink-4: #9a9283   ← texte tertiaire (placeholders, hints)
--color-ink-5: #c4bcae   ← texte décoratif (timestamps)
```

### Couleurs sémantiques (statuts)
```
SUCCESS  (vert)   : 50 #e8f8f0 · 500 #10b981 · 600 #059669
INFO     (bleu)   : 50 #e8f0fe · 500 #3b82f6 · 600 #2563eb
DANGER   (rouge)  : 50 #feecec · 500 #ef4444 · 600 #dc2626
WARNING  (ambre)  : 50 #fff6e0 · 500 #f59e0b · 600 #d97706
VIOLET            : 50 #f1ecfe · 500 #8b5cf6 · 600 #7c3aed
TEAL              : 50 #dcfce7 · 500 #14b8a6 · 600 #0d9488
```

**Mapping statuts métier → couleurs** (à respecter strictement) :
| Statut | Couleur |
|---|---|
| `PENDING`, `DRAFT`, `SUBMITTED` (en attente) | ambre (warning) |
| `APPROVED`, `VALIDATED`, `ACTIVE`, `PAID`, `COMPLETED` | vert (success) |
| `REJECTED`, `CANCELLED`, `FAILED`, `TERMINATED` | rouge (danger) |
| `IN_PROGRESS`, `PROCESSING`, `IN_REPAYMENT`, `INTERVIEWING` | bleu (info) |
| Statuts neutres / informatifs | violet ou teal |

### Dégradés autorisés (uniquement ceux-ci)
```
--grad-orange:      135deg → #fb8533 → #f26b0f → #dc560a   (hero, CTA hover)
--grad-orange-soft: 135deg → #ffc299 → #f26b0f             (cards KPI)
--grad-amber:       135deg → #fcd34d → #f59e0b
--grad-green:       135deg → #34d399 → #059669
--grad-blue:        135deg → #60a5fa → #2563eb
--grad-violet:      135deg → #a78bfa → #7c3aed
--grad-teal:        135deg → #2dd4bf → #0d9488
--grad-dark:        135deg → #2b2218 → #0f0b05             (bandeau bulletin)
```

## 1.2 Typographie

```
--font-sans:    "Inter"                            ← UI courante (corps, labels)
--font-display: "Inter Tight"                       ← titres, hero, gros chiffres
--font-mono:    "JetBrains Mono"                    ← montants, codes, matricules
```

**Échelle typographique** (utilise les utilitaires existants) :
- `text-display` : 34px / 800 / -0.03em — titres de pages clés (dashboard hero)
- `text-h1` : 28px / 700 / -0.03em — titres de page secondaire
- `text-h2` : 20px / 700 / -0.022em — titres de section
- `text-h3` : 16px / 600 / -0.012em — sous-titres, headers de card
- corps : 14px / 400, line-height 1.5 (base body)
- petit : 12px (badges, métadonnées, footers)
- micro : 11px (timestamps, légendes de graphes)

**Règles strictes** :
- Tous les montants en `font-mono-tabular` (chiffres tabulaires) pour s'aligner verticalement
- Tous les matricules, codes CNPS, codes de rubrique, références (`SAL-2026-10-...`) en `font-mono`
- Tracking serré (-0.025em) sur les titres, jamais sur le corps
- Pas plus de 2 graisses par bloc

## 1.3 Espacement, rayons, ombres

**Rayons** :
```
xs  8px    sm  12px    md  16px    lg  20px    xl  28px    2xl 36px
```
- Inputs : `--radius-sm` (12px)
- Cards : `--radius-md` (16px) à `--radius-lg` (20px)
- Modales, drawers : `--radius-lg` (20px)
- Boutons : `--radius-sm` (12px)
- Avatars : `--radius-full` (cercle)

**Ombres** (utilitaires `shadow-{xs,sm,md,lg,xl}-brand` et `shadow-orange-brand`) :
- Cards repos : `shadow-sm-brand`
- Cards hover / élevées : `shadow-md-brand`
- Drawers / modales : `shadow-xl-brand`
- CTA primaire (orange) : `shadow-orange-brand`
- Bandeau dark (bulletin) : `shadow-dark-brand`

**Espacement** : utilise les multiples de 4 (4, 8, 12, 16, 20, 24, 32, 40, 48, 64). Padding card : 20–24px. Padding section : 32–48px.

## 1.4 Ambiance

Le fond de page a :
- Une grille de points discrets (grain à 22px d'écart, opacity 0.6)
- 3 spots radiaux d'orange/jaune très diffus (10% d'opacité max)

→ donne une chaleur visuelle sans bruit. **Ne pas surcharger** avec d'autres effets.

## 1.5 Composants atomiques à utiliser

- **Button** : variants `primary` (orange plein), `secondary` (fond clair, border `line-strong`), `ghost` (texte seul), `danger` (rouge plein). Tailles `sm` (32px), `md` (40px), `lg` (48px).
- **Input** : border `line-strong`, fond `bg-elev`, focus orange-500 + ring orange-100. Label au-dessus, helper text en-dessous (ink-3, 12px).
- **Badge / Chip** : `bg-{semantic}-50` + `text-{semantic}-600`, padding 4×10px, border-radius 999px, un point coloré 6px à gauche.
- **Card** : `bg-bg-elev`, `border 1px solid line-soft`, `radius-md`, `shadow-sm`, padding 20–24px.
- **Table** : header sticky `bg-bg-soft`, cellules padding 12×16, hover ligne `bg-bg-soft`, séparateurs `line-soft`.
- **Avatar** : 40px par défaut, fond `orange-100` + initiales `orange-700`. Photo si présente, sinon initiales.
- **Tooltip** : `bg-dark` + `text-bg-elev`, 12px padding, radius 8px.
- **Toast** : top-right, `bg-bg-elev`, border 1px `line-strong`, icône sémantique colorée à gauche, durée 4s.

---

# 2. ARCHITECTURE FRONTEND (à respecter)

## 2.1 Stack

- **Next.js 16** App Router (RSC), Turbopack — **breaking changes vs versions antérieures, ne pas s'appuyer sur ta mémoire de Next.js 13/14**
- TypeScript strict
- Tailwind CSS v4 (tokens définis ci-dessus dans `globals.css`)
- shadcn/ui (Radix sous le capot)
- Lucide React (icônes — pas d'emoji dans l'UI)
- Recharts (graphes)
- React Hook Form + Zod (formulaires)
- TanStack Query (data) + Zustand (UI state)
- next-intl (i18n FR/EN)
- iron-session pour la session (JWT KSM stocké server-side)

## 2.2 Pattern BFF

Le navigateur **ne parle jamais à KSM directement**. Tout passe par des Route Handlers Next.js `/api/...` qui :
1. lisent la session iron-session
2. injectent les headers `X-Tenant-Id`, `X-Organization-Id`, `Authorization: Bearer`
3. proxifient vers KSM

**Tu ne conçois pas ces routes**, mais tu sais que **toute donnée affichée vient d'un appel** `fetch('/api/...')` côté client (avec TanStack Query) ou `fetch` server-side (RSC).

## 2.3 Routing par rôle

Le frontend est **partitionné par rôle** : chaque rôle a son namespace URL et sa propre sidebar/dashboard. La résolution du rôle se fait depuis `session.user.roles[0]` (mappé via `ROLE_CODE_TO_SLUG`) ou par inférence depuis les permissions.

| Code KSM | Slug URL | Persona |
|---|---|---|
| `SUPER_ADMIN`, `ORGANIZATION_ADMIN` | `/admin` | Admin tenant |
| `HR_ADMIN`, `HR_MANAGER` | `/hr-admin` | Admin RH (cœur RH opérationnel) |
| `HR_DIRECTOR` | `/drh` | DRH (vue stratégique) |
| `PAYROLL_MANAGER` | `/payroll-manager` | Responsable Paie (workspace dédié) |
| `MANAGER` | `/manager` | Manager d'équipe |
| `EMPLOYEE` | `/employee` | Employé (self-service) |
| `RECRUITER` | `/recruiter` | Recruteur |
| `OCCUPATIONAL_DOCTOR` | `/doctor` | Médecin du travail |
| `HR_CONTROLLER` | `/controller` | Contrôleur RH |
| `ACCOUNTANT` | `/accountant` | Comptable |

**Règle** : chaque rôle voit **uniquement** sa sidebar et ses pages. Les permissions backend (`hrm:*:*`) sont enforcées par KSM, mais le frontend gate aussi visuellement via `useCan("hrm:expense:approve")`.

## 2.4 Couleur d'accent par persona (subtile)

Tous les rôles partagent le même design system. Pour donner une **identité légère** à chaque workspace sans casser la cohérence, chaque persona reçoit une **couleur d'accent secondaire** appliquée uniquement sur :
- Le bord gauche actif de la sidebar
- L'icône de la page courante dans le breadcrumb
- L'ombre du logo en haut de la sidebar

| Persona | Couleur d'accent |
|---|---|
| Employé | orange-500 (l'identité de base) |
| Manager | teal-500 |
| Admin RH | violet-500 |
| DRH | orange-500 + dégradé dark sur le hero |
| Responsable Paie | dark (#1c1610) — workspace plus "outil pro" |
| Comptable | blue-600 |
| Recruteur | amber-500 |
| Médecin | teal-600 |
| Contrôleur | violet-600 |
| Admin | dark |

> L'orange reste partout la couleur des **CTA principaux**, peu importe le rôle. L'accent persona ne s'applique qu'aux 3 éléments listés.

---

# 3. ENDPOINTS BACKEND DISPONIBLES (référence)

**Tu n'inventes aucune route, aucun champ, aucun statut hors de cette liste.** Pour les payloads exacts, lis les controllers Java fournis en contexte.

## 3.1 HRM (préfixe `/api/v1/hrm/`)

### Employés (`/employees`)
- `POST /employees` — créer (`hrm:employee:create`)
- `GET /employees/{id}` — fiche
- `GET /employees/{id}/profile` — fiche 360° (Employee + ActorInfo + manager)
- `GET /employees/{id}/timeline` — timeline d'événements
- `GET /employees?organizationId=&agencyId=` — liste
- `PUT /employees/{id}` — mise à jour
- `PUT /employees/{id}/terminate` — terminer (date + raison)
- `PUT /employees/{id}/suspend` — suspendre
- `PUT /employees/{id}/reactivate` — réactiver
- Sub-ressources : `/contracts`, `/dependents`, `/personal-info`, `/emergency-contacts`

**Champs Employee exposés** (à reproduire fidèlement dans formulaires) :
matricule, numCnps, categorie (int), echelon, dateEmbauche, dateSortie, motifSortie, status (ACTIVE/SUSPENDED/ON_LEAVE/TERMINATED), departmentCode, modePaiement (BANK_TRANSFER/MTN_MOBILE_MONEY/ORANGE_MONEY/CASH), compteBancaire, numMobileMoney, operateurMm (MTN/ORANGE), actorDisplayName, managerId.

**EmployeePersonalInfo** : lieuNaissance, situationMatrimoniale, typePiece (CNI/PASSPORT/DRIVING_LICENSE/RESIDENCE_PERMIT), numeroPiece, dateEmissionPiece, niuFiscal, permisConduire, languesParlees, emailPersonnel, telephoneDomicile, whatsapp, adressePostale, adresseDomicile, ville, region, codePostal.

**Dependent** : nom, prenom, dateNaissance, lienParente, certificatFileId.

**EmergencyContact** : nom, prenom, relation, telephone, email, priorite (int).

### Contrats (sous `/employees/{id}/contracts`)
- POST create, GET list, GET one, PUT terminate, POST renew
- Champs : type (CDD/CDI/STAGE/INTERIM), **position**, dateDebut, dateFin, salaireBase, avantagesNature, periodeEssai (int mois), status (ACTIVE/EXPIRED/TERMINATED/RENEWED), motifFin, documentFileId.

> **Ce qui n'existe PAS encore côté backend (donc à ne pas afficher)** :
> - Avenants/modifications de salaire avec historique
> - Suspension de contrat
> - Mutation/promotion (changement catégorie/échelon tracé)
> - Conversion CDD→CDI
> - Signature électronique du contrat

### Congés (`/leaves`)
- POST submit, PUT approve, PUT reject, PUT cancel
- GET mine, GET pending (manager), GET by employee, GET balances
- LeaveType : ANNUAL, SICK, MATERNITY, PATERNITY, UNPAID, SPECIAL
- LeaveStatus : PENDING, APPROVED, REJECTED, CANCELLED
- LeaveBalance : { acquis, pris, soldeRestant, type, annee }
- **Accrual** auto (1.5j/mois + bonus ancienneté + bonus enfants <6 ans)

> **N'existe PAS** : demi-journées, jours fériés (calendrier), carry-over, cap solde, délégation d'approbation, congés exceptionnels typés.

### Timesheets (`/timesheets`)
- POST create, PUT submit, PUT validate
- Champs : periode (YYYY-MM), heuresNormales, heuresSupplementaires, heuresNuit, heuresWeekend, absencesNonJustifiees
- Statuts : DRAFT, SUBMITTED, VALIDATED

> **N'existe PAS** : saisie par jour/semaine, par projet, par centre de coût, clock in/out, approbation hiérarchique, banque d'heures.

### Ordres de mission (`/mission-orders`)
- Cycle 7 états : DRAFT → PENDING_ACCEPTANCE → APPROVED → IN_PROGRESS → COMPLETED, + DECLINED, CANCELLED
- Champs : destination, objet, dateDebut, dateFin, montantAvance, centreCout, parentOrderId (avenant)
- Actions : create, send, acceptByEmployee, declineByEmployee(reason), start, complete, cancel, amend

> **N'existe PAS** : transport/hébergement/repas/per diem détaillés, rapport de mission obligatoire, multi-approbateurs.

### Notes de frais (`/expenses`)
- POST create report, POST add line, PUT submit, PUT approve, PUT reject, PUT reimburse
- ExpenseReport : periode, totalMontant, motif, missionOrderId (optionnel), status (DRAFT/SUBMITTED/APPROVED/REJECTED/REIMBURSED)
- ExpenseLine : description, montant, categorie (String **libre**, pas d'enum), justificatifFileId

> **N'existe PAS** : limites par catégorie, multi-devises, audit trail, OCR.

### Avances/Prêts (`/loan-advances`)
- POST request, PUT approve, PUT reject
- Champs : montant, soldeRestant, mensualite, dateDebut, nbEcheances, status (PENDING/IN_REPAYMENT/FULLY_REPAID/REJECTED), motif, approvedBy
- GET mine, POST mine (self-service), GET by employee

> **N'existe PAS** : taux d'intérêt, types de prêts, garant, échéancier détaillé, anticipation, plafond métier appliqué.

### Compétences (`/skills`)
- POST skill, GET skill list (catalogue)
- POST employee-skill, GET by employee, GET by skill, GET all by org
- Skill : name, categorie (String libre), description
- EmployeeSkill : niveauActuel (int), niveauAttendu (int), dateEvaluation

> **N'existe PAS** : hiérarchie de catégories, certifications avec expiration, auto-eval vs manager, matching candidat/poste.

### Formations (`/trainings`, `/training-budgets`)
- POST plan training, PUT start, PUT complete, PUT cancel
- POST enroll, PUT complete enrollment, PUT cancel enrollment
- Training : intitule, organisme, dateDebut, dateFin, cout, nbPlaces, lieu, status (PLANNED/IN_PROGRESS/COMPLETED/CANCELLED)
- TrainingEnrollment : noteEvaluation, attestationFileId, status (ENROLLED/COMPLETED/CANCELLED)
- TrainingBudget : annee, montantAlloue, montantEngage, montantConsomme

> **N'existe PAS** : validation manager, compétences acquises mappées, multi-sessions, LMS, formations obligatoires.

### Évaluations (`/reviews`)
- POST create review, POST add objective, PUT evaluate objective, PUT submit, PUT acknowledge, PUT finalize
- PerformanceReview : periode, evaluateurPartyId, evaluateurDisplayName, noteGlobale, commentaires, planAction, status (DRAFT/SUBMITTED/ACKNOWLEDGED/FINALIZED)
- ReviewObjective : description, poids, noteAtteinte, commentaire

> **N'existe PAS** : auto-évaluation employé, 360°, calibration, OKR/SMART structuré, multi-niveaux N+2, lien avec augmentation/promotion.

### Médical (`/medical`)
- POST visit, GET visit, GET visits by employee, GET visits by org
- POST certificate, GET certificate, GET certificates by employee/org
- MedicalVisit : date, medecin (String), restrictions, prochaineEcheance, certificatFileId, resultatAptitude (APTE/APTE_AVEC_RESTRICTIONS/INAPTE_TEMPORAIRE)
- MedicalCertificate : typeCertificat (String libre), dateEmission, dateExpiration, statut, fichierId

> **N'existe PAS** : type de visite typé (EMBAUCHE/PERIODIQUE/REPRISE), accidents du travail, maladies professionnelles, vaccinations, habilitations sécurité, statistiques HSE.

### Recrutement (`/job-offers`, `/applications`, `/interviews`, `/onboarding-tasks`)
- JobOffer : poste, departement, localisation, competencesRequises, dateLimite, packageSalarial, status (DRAFT/PUBLISHED/CLOSED)
- Application : candidatNom/prenom/email/telephone, cvFileId, lettreMotivationFileId, status (NEW/SHORTLISTED/INTERVIEWING/OFFERED/HIRED/REJECTED)
- Interview : type (RH/TECHNIQUE/FINAL), dateHeure, lieu, interviewerPartyId, notes, resultat (PENDING/PASS/FAIL)
- OnboardingTask : titre, description, assignedToPartyId, echeance, status (PENDING/IN_PROGRESS/COMPLETED)
- Action critique : `POST /applications/{id}/convert-to-employee` (création Employee + Contract en 1 appel)

> **N'existe PAS** : scoring, panel multi-interviewers, sources de candidature, talent pool, templates onboarding, offboarding.

### Déclarations sociales (`/declarations`)
- POST create, PUT generate, PUT submit, PUT acknowledge
- DeclarationType : CNPS, DIPE, IRPP_CAC
- DeclarationStatus : DRAFT → GENERATED → SUBMITTED → ACKNOWLEDGED
- Champs : periode, format (libre), fichierId, generatedAt, submittedAt

> **N'existe PAS** : état REJECTED, format officiel CNPS validé, transmission électronique.

### KPI RH (`/kpi`)
- POST snapshot, GET snapshots
- RhKpiSnapshot : periode, effectifTotal, effectifActif, tauxTurnover, tauxAbsenteisme, masseSalariale, couvertureCompetences

> **N'existe PAS** : drill-down par agence/département, calcul auto, variation N/N-1, projections, alertes.

## 3.2 Payroll (préfixe `/api/v1/payroll/`)

### Cycles de paie (`/runs`)
- POST run, PUT validate, PUT approve, PUT initiate-payment, PUT close
- GET run, GET runs by org, GET entries by run, GET payslip lines by entry
- POST payment callback
- PayrollRunStatus (9 états) : DRAFT, VARIABLES_LOCKED, CALCULATED, REVIEW, VALIDATED, APPROVED, PAYMENT_INITIATED, PAID, CLOSED
- Totaux : totalGross, totalEmployeeDeductions, totalIncomeTax, totalNet, totalEmployerCharges, nbEmployes
- PayrollEntry : salaireBase, brut, totalDeductions, incomeTax, employerCharges, net, paymentStatus, paymentChannel, accountRef
- PayslipLine : payElementCode, libelle, type (EARNING/DEDUCTION/EMPLOYER_INFO), base, taux, montant, ordreAffichage

### Self-service employé (`/my-entries`)
- GET my-entries — liste mes bulletins
- GET my-entries/{entryId}/payslip — lignes de mon bulletin

### Rubriques de paie (`/pay-elements`)
- POST create, DELETE deactivate, GET list, GET one
- PayElement : code, label, category (EARNING/DEDUCTION/EMPLOYER_CHARGE/INFORMATIONAL), method (RATE/BRACKET/FLAT/LOOKUP_TABLE), baseReference, rate, ceiling, floor, exemptionThreshold, flatAmount, bracketTableCode, lookupTableCode, taxable, socialContributable, countryCode, displayOrder, active, effectiveFrom, effectiveTo

### Variables mensuelles (`/variables`)
- POST capture, GET by employee+period, GET by org+period
- Champs : overtimeHoursDay/Night/SundayHoliday, bonuses, unpaidAbsenceDays, advances, workedDaysOverride, locked

### Solde de tout compte (`/final-settlements`)
- POST calculate, PUT pay, GET one, GET list (par employé ou org)
- Champs : departureDate, reason (TerminationReason : RESIGNATION/DISMISSAL/DISMISSAL_GROSS_MISCONDUCT/END_OF_CONTRACT/RETIREMENT/MUTUAL_AGREEMENT/DEATH), seniorityYears, proratedSalary, leaveCompensation, noticeIndemnity, severanceIndemnity, gratification, grossSettlement, loanDeducted, netSettlement, status (CALCULATED/PAID)

### Saisies sur salaire (`/garnishments`)
- POST create, DELETE cancel, GET one, GET list
- Champs : type (ALIMONY/TAX_LEVY/CREDITOR), beneficiary, reference, totalAmount, remainingBalance, monthlyAmount, status (ACTIVE/SUSPENDED/COMPLETED/CANCELLED)

### Rétroactifs (`/retroactive`)
- POST calculate, PUT apply, DELETE cancel, GET one, GET list
- Champs : originPeriod, targetPeriod, reason, oldGross, newGross, deltaGross, oldNet, newNet, deltaNet, status (PENDING/APPLIED/CANCELLED)

### Déclarations paie (`/declarations`)
- GET declaration (JSON), GET declaration/csv (téléchargement CSV)
- Params : type (CNPS/DIPE/IRPP_CAC) + runId

### Documents signés (`/documents`)
- POST payslip, POST final-settlement, POST work-certificate
- GET one, GET verify, GET list par employé
- Champs : type (PAYSLIP/FINAL_SETTLEMENT/WORK_CERTIFICATE), subjectId, periode, fileId, fileName, algorithm, contentHashHex, verificationCode, keyId, signedAt
- Le PDF se télécharge via `/api/files/{fileId}` (BFF proxy)

---

# 4. ARCHITECTURE DES ÉCRANS PAR RÔLE

Pour chaque persona, je te liste **les pages à concevoir**, **ce qu'elles affichent**, et **les scénarios de navigation**. Tu produis le design fidèlement.

**Layout commun à tous les rôles** :
- **Topbar** (60px) : breadcrumb, sélecteur d'organisation/agence (dropdown), recherche cmd+K, notifications (cloche), avatar utilisateur avec menu (profil, paramètres, déconnexion).
- **Sidebar gauche** (240px) : logo en haut, sections de navigation (nom de section en `text-h3` + items en 14px), profil utilisateur compact en bas. Items actifs : fond `bg-orange-50` + texte `orange-700` + bord gauche 3px `orange-500` (ou couleur d'accent persona).
- **Main** : fond `bg`, scrollable, padding 24–32px.
- **Drawers à droite** (480–560px) pour les fiches détail rapides, sans navigation de page.

## 4.1 EMPLOYÉ (`/employee/*`)

**Sidebar** (8 entrées) :
- Tableau de bord
- Mon profil
- Mes congés
- Mes bulletins
- Mes notes de frais
- Mes timesheets
- Mes formations
- Mes prêts

### Pages

**`/employee/dashboard`** — Tableau de bord personnel
- Hero (40% hauteur, fond `bg-grad-orange-soft` discret) avec :
  - Avatar + nom (depuis `actorDisplayName`)
  - Matricule en `font-mono`
  - Date d'embauche → calcul ancienneté ("3 ans 2 mois")
  - Statut badge (ACTIVE → vert)
- 4 cards KPI compactes :
  - **Solde congés annuels** → `soldeRestant` depuis `GET /leaves/balances`
  - **Dernier bulletin net** → premier item de `GET /payroll/my-entries`
  - **Notes de frais en cours** → count `SUBMITTED` depuis `GET /expenses/mine`
  - **Prêt en cours** → `soldeRestant` du prêt `IN_REPAYMENT` depuis `GET /loan-advances/mine`
- Section "À faire" : timesheet du mois courant si non `SUBMITTED`, congés `PENDING`
- Section "Activité récente" : 5 derniers événements (bulletin payé, congé approuvé, etc.) — utilise `/employees/{id}/timeline` si possible

**`/employee/profile`** — Mon profil
- Onglets : **Identité** (champs Employee + EmployeePersonalInfo), **Mes proches** (Dependents + EmergencyContacts), **Contrat** (Contract actif en lecture seule), **Documents** (liste générée par `/payroll/documents?employeeId={me}`)
- Identité : seuls les champs `EmployeePersonalInfo` sont éditables (le reste est admin RH)
- Bouton "Demander une attestation" → modal qui appelle `POST /payroll/documents/work-certificate?employeeId={me}`

**`/employee/leaves`** — Mes congés
- En haut : 3 cards "Solde par type" (ANNUAL/SICK/SPECIAL — autres ne sont pas accru)
- Tableau : période, type, jours, statut (badge), motif, action (annuler si `PENDING`)
- CTA primaire orange "Nouvelle demande" → drawer formulaire (dateDebut, dateFin, type [select], motif [textarea], justificatifFileId [upload optionnel])
- Filtres : statut, type, année

**`/employee/payslips`** — Mes bulletins
- Vue alternative : tableau OU grille de cartes (toggle)
- Carte "Cumul annuel" sticky en haut : brut cumulé, net cumulé, IRPP cumulé (calcul côté frontend à partir des items)
- Liste : période (YYYY-MM), statut run, net (orange, mono-tabular), bouton "Voir / Télécharger"
- Détail bulletin = drawer ou page séparée `/employee/payslips/{entryId}` avec :
  - Bandeau dark en haut : nom, période, NET en gros (`text-display`, font-mono)
  - Tableau des `PayslipLine` ordonnées par `ordreAffichage`, séparées en sections Gains / Retenues / Charges patronales (info)
  - Bouton "Télécharger PDF" → `POST /payroll/documents/payslip?entryId=...` puis téléchargement du `fileId`
  - Badge "Signé électroniquement" si document existe, avec code de vérification

**`/employee/expenses`** — Mes notes de frais
- Liste des ExpenseReport (badge statut, total, période, motif)
- CTA "Nouvelle note de frais" → wizard 2 étapes : (1) infos report (periode, motif, missionOrderId optionnel) → (2) ajout des lignes (description, montant XAF, categorie texte libre, upload justificatif)
- Détail report : ses lignes, statut, total, possibilité de soumettre si `DRAFT`
- ⚠️ `categorie` est une string libre côté backend → input texte (avec suggestions client side : Transport, Hébergement, Repas, Fournitures, Autre) mais pas un select strict

**`/employee/timesheets`** — Mes timesheets
- Liste mensuelle (1 ligne par mois, dernier 12 mois)
- Détail / saisie : formulaire mois courant avec champs `heuresNormales`, `heuresSupplementaires`, `heuresNuit`, `heuresWeekend`, `absencesNonJustifiees`
- CTA "Soumettre" si `DRAFT`
- ⚠️ **Ne pas afficher** : saisie par jour, par projet, code de coût (n'existe pas)

**`/employee/trainings`** — Mes formations
- Onglet "Catalogue" : liste des `Training` `PLANNED` avec possibilité de s'inscrire → `POST /trainings/{id}/enrollments`
- Onglet "Mes inscriptions" : ses `TrainingEnrollment` avec statut, note (si COMPLETED), attestation à télécharger

**`/employee/loans`** — Mes prêts
- Carte "Prêt actuel" si actif : montant, soldeRestant, mensualite, échéances restantes (calcul : `montant / mensualite`)
- Historique : tous mes prêts avec statut
- CTA "Demander une avance/prêt" → form (montant, motif, nbEcheances)

## 4.2 MANAGER (`/manager/*`)

**Sidebar** :
- Tableau de bord
- Mon équipe
- Validations en attente (badge avec count)
- Congés équipe
- Timesheets équipe
- Notes de frais équipe
- Ordres de mission
- Évaluations

### Pages

**`/manager/dashboard`** — Tableau de bord manager
- 4 KPI : Effectif équipe, Congés à approuver, Timesheets à valider, Notes de frais à approuver
- Liste "Mon équipe" (avatars, nom, statut, dernier événement) — `GET /employees?managerId=me`
- "À approuver" (top 5 par âge) avec actions inline (Approuver / Rejeter avec raison)
- Calendrier compact des absences équipe (mois courant) → couleur par employé

**`/manager/team`** — Mon équipe
- Tableau ou grille d'avatars de mon équipe
- Click ligne → drawer fiche employé (vue limitée : pas le salaire, juste poste, contact, contrat type, manager)

**`/manager/approvals`** — Validations en attente
- Onglets : Congés, Timesheets, Notes de frais, Missions
- Chaque onglet = liste des items `PENDING/SUBMITTED` avec actions
- Actions de bulk autorisées si même type

**`/manager/leaves/team`** — Congés équipe
- Tableau : employé, type, période, jours, statut
- Filtre période
- Vue calendrier alternative (semaine/mois) → bloc coloré par employé

**`/manager/timesheets/team`** — Timesheets équipe
- Tableau : employé, mois, heures normales, heures sup total, statut
- Action : valider individuellement ou bulk

**`/manager/expenses/team`** — Notes de frais équipe
- Liste reports équipe avec total, motif, statut
- Détail : voir lignes, justificatifs, approuver/rejeter

**`/manager/mission-orders`** — Ordres de mission
- Liste avec statut (les 7 états)
- CTA "Créer un ordre de mission" → formulaire (employee, destination, dates, objet, avance, centre coût)
- Détail : timeline des transitions + actions selon état

**`/manager/reviews`** — Évaluations
- Liste des PerformanceReview qu'il a créés/à compléter
- CTA "Lancer une campagne" si autorisé → form (employee, période)
- Détail review : ses objectifs (table éditable), note globale, commentaires, plan d'action

## 4.3 ADMIN RH (`/hr-admin/*`)

**Sidebar** :
- Tableau de bord
- Employés
- Contrats
- Compétences
- Médical
- Personnes à charge
- Formations
- Prêts
- Déclarations sociales
- Documents

### Pages clés

**`/hr-admin/dashboard`**
- 6 KPI (effectif actif, embauches du mois, départs, contrats expirant dans 30j, visites médicales expirant dans 30j, masse salariale)
- "Alertes" : contrats avec `isExpiringSoon(30)` (calcul frontend via dateFin), certificats médicaux à renouveler
- Graphique : évolution effectif sur 12 mois (snapshots KPI)

**`/hr-admin/employees`**
- Tableau riche : photo, matricule, nom, poste (Contract.position), département, statut, ancienneté
- Filtres : statut, agence, département, type contrat
- CTA "Nouvel employé" → wizard multi-étapes : (1) identité, (2) contrat actif (avec position obligatoire), (3) infos paie (modePaiement + compteBancaire ou numMobileMoney+operateurMm)
- Click ligne → page fiche `/hr-admin/employees/{id}` avec onglets : Identité, Contrat(s), Personnes à charge, Compétences, Évaluations, Médical, Documents, Timeline, Actions (suspendre, terminer, réactiver)
- Bouton "Terminer" sur fiche : modal demandant `terminationDate` ET `reason` (date stockée, raison stockée)

**`/hr-admin/contracts`**
- Vue transverse de TOUS les contrats avec filtres
- CTA "Renouveler" sur les contrats CDD en `ACTIVE` ou `EXPIRED`
- ⚠️ **Pas de bouton "Modifier salaire"** (avenant n'existe pas backend)
- ⚠️ **Pas de "Convertir CDD→CDI"** (n'existe pas backend)

**`/hr-admin/skills`** — Référentiel
- 2 onglets : "Catalogue" (Skill) et "Compétences employés" (EmployeeSkill)
- Catalogue : grille de cards (nom, categorie en chip, description). CTA "Ajouter compétence" (form simple : name, categorie texte libre, description)
- Compétences employés : matrice (lignes = employés, colonnes = skills), cellule = `niveauActuel/niveauAttendu` avec barre visuelle
- Click cellule → drawer pour saisir/mettre à jour

**`/hr-admin/medical`**
- 2 onglets : Visites, Certificats
- Visites : liste avec employé, date, médecin, résultat aptitude, prochaine échéance (badge `warning` si <30j)
- Certificats : liste avec type, dateEmission, dateExpiration, statut, PDF
- CTA : "Programmer une visite", "Émettre un certificat"

**`/hr-admin/trainings`**
- Liste formations + bouton "Planifier" (intitule, organisme, dates, lieu, cout, nbPlaces)
- Détail : inscrits (TrainingEnrollment), actions start/complete/cancel
- Onglet "Budget" → TrainingBudget (annee, alloué, engagé, consommé) avec barre de progression

**`/hr-admin/loans`**
- Liste demandes `PENDING` avec actions Approuver / Rejeter
- Onglet "En cours" : prêts `IN_REPAYMENT` avec soldeRestant et progression visuelle
- Onglet "Historique" : tous statuts

**`/hr-admin/declarations`**
- Onglets par type : CNPS, DIPE, IRPP_CAC
- Liste : période, statut (workflow visuel DRAFT→GENERATED→SUBMITTED→ACKNOWLEDGED), génération, soumission, fichier
- CTA "Nouvelle déclaration" → form (type, periode, format)
- Actions selon état : Générer, Soumettre, Acquitter

**`/hr-admin/documents`** — Documents signés produits
- Liste générée par `/payroll/documents` (tous types)
- Filtres : type, employé, période
- Verify badge sur chaque ligne (appel `/verify`) + PDF download

## 4.4 DRH (`/drh/*`)

Vue **stratégique** : pas d'opérationnel.

**Sidebar** :
- Tableau de bord
- Analytics
- KPI
- Masse salariale
- Évaluations (vue agrégée)
- Formations (budget)
- Effectif

### Pages

**`/drh/dashboard`**
- 6 KPI orientés stratégie : effectif total, masse salariale, taux turnover, taux absentéisme, couverture compétences, budget formation consommé
- Graphes : évolution effectif 12 mois, répartition contrats (donut CDD/CDI/STAGE/INTERIM), répartition par catégorie

**`/drh/kpi`** — KPI RH
- Tableau des snapshots historiques avec lignes pour chaque période
- Graphes Recharts comparatifs N vs N-1 (calcul frontend depuis snapshots)
- Filtre période
- CTA "Créer snapshot" (form avec tous les champs RhKpiSnapshot)

**`/drh/payroll`** — Vue masse salariale (lecture seule)
- Liste des PayrollRun toutes périodes, totaux
- Click run → détail (entries, payslip lines) en lecture seule

**`/drh/reviews`** — Vue agrégée évaluations
- Tableau : période, employé, evaluateur, noteGlobale, statut
- Filtre : période, statut

**`/drh/budget`** — Budget formations
- Liste TrainingBudget par année
- Barre de progression alloué / engagé / consommé

## 4.5 RESPONSABLE PAIE (`/payroll-manager/*`)

**Workspace dédié** — c'est son outil métier principal. Sidebar plus sombre que les autres (fond `--color-dark-2`), texte clair, accent orange.

**Sidebar** :
- **Pilotage** : Dashboard
- **Cycles** : Cycles de paie, Variables, Régularisations rétroactives
- **Configuration** : Rubriques de paie, Barèmes fiscaux, Tables de référence (si exposé)
- **Sortie** : Bulletins, Documents, Déclarations
- **Spécial** : Soldes de tout compte, Saisies sur salaire
- **Analyse** : Audit (via timeline des events)

### Pages

**`/payroll-manager/dashboard`**
- Hero : période courante + stepper 9 états du cycle en cours (si existe pour cette période)
- CTA primaire orange "Lancer le cycle" (visible uniquement si aucun cycle `REGULAR` n'existe pour la période)
- 4 KPI : Masse salariale (total brut), Net total à verser, Cotisations CNPS du mois (à reverser, deadline 15), Effectif payé
- Liste "Échéances déclaratives" : CNPS/DIPE/IRPP du mois précédent avec statut DRAFT/GENERATED/SUBMITTED/ACKNOWLEDGED et compte à rebours du 15

**`/payroll-manager/runs`** — Cycles
- Liste paginée : période, agence, type, statut (badge), effectif, brut, net, validation, actions
- Filtres : période, statut multi-select, agence
- CTA "Nouveau cycle" → wizard 5 étapes :
  1. Configuration (période YYYY-MM, agence optionnelle, runType select REGULAR/COMPLEMENTARY/THIRTEENTH_MONTH/EXCEPTIONAL_BONUS)
  2. Variables : tableau employés avec leurs PayVariable (overtimeHoursDay/Night/SundayHoliday, bonuses, unpaidAbsenceDays, advances, workedDaysOverride). Bouton "Verrouiller" pour basculer en VARIABLES_LOCKED
  3. Aperçu : appel POST /runs qui calcule → affiche totaux
  4. Confirmation
  5. Résultat (CALCULATED)

**`/payroll-manager/runs/{runId}`** — Détail cycle
- Header : période, statut (badge + stepper 9 états vertical sticky à gauche)
- 3 colonnes :
  - Gauche sticky : carte d'identité du run + boutons d'action contextuels (Mettre en revue / Valider / Approuver / Initier paiement / Clôturer selon état)
  - Centre : onglets
    - **Vue d'ensemble** : 5 KPI (brut, retenues, IT, net, charges patronales), graphique répartition
    - **Bulletins** : tableau des PayrollEntry (matricule, nom, brut, retenues, net, statut paiement, paymentChannel, accountRef)
    - **Audit** : timeline des transitions (via events si exposé, sinon timestamps des champs validatedAt/approvedAt/paidAt/closedAt)
  - Droite : drawer fiche bulletin (apparaît au click)

**`/payroll-manager/runs/{runId}/entries/{entryId}`** — Fiche bulletin (drawer ou page)
- Bandeau dark : identité employé + NET (text-display)
- Liste des PayslipLine groupées (Gains / Retenues / Employer info)
- Section "Paiement" : status, channel, accountRef
- Boutons : Générer PDF signé → `POST /documents/payslip?entryId=...`

**`/payroll-manager/variables`** — Saisie variables
- Sélecteur période
- Tableau éditable inline des employés avec colonnes : OT jour, OT nuit, OT dimanche/férié, primes, jours absence, acomptes, jours travaillés (override). Sauvegarde auto par ligne via `POST /variables`
- Indicateur progression saisis/total

**`/payroll-manager/pay-elements`** — Catalogue rubriques
- Tableau riche : code (mono), label, category badge, method badge, baseReference, rate (% si applicable), ceiling, floor, exemption, taxable/cotisable (chips), countryCode, displayOrder, active (toggle), effectiveFrom/To
- CTA "Nouvelle rubrique" → drawer formulaire avec tous les champs PayElement (en tenant compte du method choisi : si RATE → rate visible, si BRACKET → bracketTableCode select, si LOOKUP_TABLE → lookupTableCode select, si FLAT → flatAmount)
- Click ligne → drawer édition (en pratique : désactivation via DELETE → re-création — backend n'a pas d'UPDATE)

**`/payroll-manager/retroactive`** — Régularisations rétroactives
- Liste avec employé, originPeriod, targetPeriod, deltaGross, deltaNet, statut
- CTA "Nouvelle régularisation" → form (employeeId search, originPeriod, newBaseSalary, targetPeriod, reason)
- Détail : table Avant / Après (oldGross/newGross/deltaGross, oldNet/newNet/deltaNet)
- Actions : Appliquer (PUT apply), Annuler (DELETE)

**`/payroll-manager/final-settlements`** — STC
- Liste employés en départ : nom, departureDate, reason badge, netSettlement, statut
- CTA "Calculer un STC" → form (employee search, departureDate, reason select de TerminationReason, unusedLeaveDays optionnel, noticeMonths, accruedGratification)
- Détail STC : décomposition complète (proratedSalary, leaveCompensation, noticeIndemnity, severanceIndemnity, gratification, grossSettlement, loanDeducted, netSettlement)
- Boutons : "Générer document signé" → `POST /documents/final-settlement?settlementId=...`, puis "Marquer payé"

**`/payroll-manager/garnishments`** — Saisies sur salaire
- Liste : employé, type (ALIMONY/TAX_LEVY/CREDITOR), beneficiary, reference, totalAmount, remainingBalance, monthlyAmount, status
- CTA "Nouvelle saisie" → form
- Actions : Annuler

**`/payroll-manager/declarations`** — Déclarations
- Tabs CNPS / DIPE / IRPP_CAC
- Tableau : période, statut workflow, dates
- Bouton "Générer" → appelle `GET /payroll/declarations?type=...&runId=...` (JSON visible) puis "Télécharger CSV" → `/csv` download

**`/payroll-manager/documents`** — Documents émis
- Liste de tous PayrollDocument : type, employé, période, fileName, verificationCode, signedAt
- Filtres : type, période, employé
- Bouton "Vérifier" → modal qui appelle `/verify` et montre Valid/Invalid

## 4.6 RECRUTEUR (`/recruiter/*`)

**Sidebar** :
- Tableau de bord
- Offres
- Candidatures
- Entretiens
- Onboarding

### Pages

**`/recruiter/dashboard`**
- 4 KPI : Offres publiées, Candidatures NEW, Entretiens à venir, Embauches du mois
- Funnel pipeline (NEW → SHORTLISTED → INTERVIEWING → OFFERED → HIRED) avec compteurs
- Liste "Entretiens prochains 7 jours"

**`/recruiter/job-offers`**
- Tableau : poste, departement, localisation, dateLimite, statut, candidatures count
- CTA "Nouvelle offre" → form (poste, departement, localisation, competencesRequises textarea, dateLimite, packageSalarial)
- Détail : champs + liste candidatures associées

**`/recruiter/applications`**
- Kanban : colonnes NEW / SHORTLISTED / INTERVIEWING / OFFERED / HIRED / REJECTED (drag & drop entre colonnes = appel API correspondant)
- Carte candidature : avatar initiales, nom, email, télé, JobOffer poste
- Click → drawer détail avec lien CV/lettre + boutons d'action contextuels
- Bouton final dans OFFERED : "Convertir en employé" → wizard qui appelle `POST /applications/{id}/convert-to-employee`

**`/recruiter/interviews`**
- Liste avec date, candidat, type, interviewer, résultat
- CTA "Programmer un entretien" → form
- Détail : saisie notes + résultat (PASS/FAIL)
- ⚠️ Un seul interviewer par entretien (backend)

**`/recruiter/onboarding`**
- Vue par employé récemment embauché
- Liste OnboardingTask avec titre, assignedTo, échéance, statut
- CTA "Nouvelle tâche"
- Actions sur tâche : start, complete

## 4.7 MÉDECIN DU TRAVAIL (`/doctor/*`)

**Sidebar** :
- Tableau de bord
- Visites
- Certificats
- Suivi médical employé

### Pages

**`/doctor/dashboard`**
- 3 KPI : Visites du mois, Certificats expirant dans 30j, Employés inaptes/restrictions
- Liste "Visites prochaines" (basé sur prochaineEcheance des visites antérieures)

**`/doctor/visits`**
- Liste avec employé, date, medecin, résultatAptitude (badge couleur : APTE vert, APTE_AVEC_RESTRICTIONS ambre, INAPTE_TEMPORAIRE rouge), restrictions, prochaineEcheance
- CTA "Nouvelle visite" → form (employeeId search, date, medecin texte, resultatAptitude select, restrictions textarea, prochaineEcheance date, certificatFileId upload)

**`/doctor/certificates`**
- Liste : employé, typeCertificat (texte libre), dateEmission, dateExpiration, statut, PDF
- CTA "Nouveau certificat" → form
- Badge `warning` si dateExpiration < 30j

**`/doctor/employees/{id}`** — Suivi médical d'un employé
- Historique visites + certificats de cet employé

> **N'affiche PAS** : accidents du travail, vaccinations, habilitations sécurité (n'existent pas backend).

## 4.8 COMPTABLE (`/accountant/*`)

**Sidebar** :
- Tableau de bord
- Validation paie
- Écritures paie (si disponibles)
- Ordres de paiement
- Notes de frais à approuver
- Remboursements

### Pages

**`/accountant/dashboard`**
- 4 KPI : Cycles à valider, Notes de frais à approuver, Total à verser, Total remboursements en attente

**`/accountant/payroll-validation`**
- Liste des PayrollRun en statut CALCULATED ou REVIEW
- Click → vue de validation (totaux + bulletins) avec bouton "Valider" → PUT /runs/{id}/validate

**`/accountant/payroll-payments`**
- Vue de tous les runs APPROVED / PAYMENT_INITIATED / PAID
- Détail run : tableau des PayrollEntry avec leur paymentStatus (PENDING/PROCESSING/COMPLETED/FAILED) et paymentChannel
- Action : Initier paiement (PUT /initiate-payment)

**`/accountant/expenses`**
- Liste de toutes les ExpenseReport SUBMITTED + APPROVED + REIMBURSED
- Actions : approuver (note de motif optionnelle), rejeter (raison), marquer remboursé

## 4.9 CONTRÔLEUR RH (`/controller/*`)

**Sidebar** :
- Tableau de bord
- KPI
- Compétences (cartographie)
- Effectif
- Reporting

### Pages

**`/controller/dashboard`**
- KPI : effectif actif, turnover, absentéisme, masse salariale, couverture compétences

**`/controller/kpi`** — Création de snapshots
- Comme DRH mais avec CTA "Créer snapshot" actif (`hrm:kpi:create`)

**`/controller/skills-mapping`** — Cartographie
- Matrice compétences x employés (lecture seule) avec heatmap couleur

## 4.10 ADMIN (`/admin/*`)

**Sidebar** :
- Organisation
- Utilisateurs (tenant)
- Rôles
- Audit

Vue d'admin tenant — délègue à ce qui existe côté `auth-core`/`roles-core`. **Le frontend de l'admin n'est pas le sujet de ce design** (les rôles et users sont des modules transverses) ; tu peux te concentrer sur les 9 autres personas.

---

# 5. SCÉNARIOS DE NAVIGATION CLÉS (storyboards à mettre en scène)

Conçois des **planches successives** pour ces 8 parcours :

### Scénario 1 : Cycle de paie mensuel (Responsable Paie)
1. Dashboard → voit "Cycle d'octobre non lancé" → click "Lancer le cycle"
2. Wizard step 1 : choisit période 2026-10, runType REGULAR
3. Wizard step 2 : visualise le tableau des variables, importe ou saisit OT/primes, clique "Verrouiller"
4. Wizard step 3 : lance le calcul (loading précis avec compteur), aperçu des totaux
5. Détail run en statut CALCULATED → onglet Anomalies (s'il y en a) → onglet Bulletins
6. Click "Mettre en revue" → puis "Valider" → notification vers Comptable
7. Comptable se connecte → dashboard montre "1 cycle à valider" → click → validation finale → "Approuver"
8. Bouton "Initier paiement" → status PAYMENT_INITIATED, events PAYMENT_ORDER_CREATED publiés
9. Plus tard : un callback met le run en PAID → "Clôturer"

### Scénario 2 : Self-service employé consulte son bulletin
1. Employé se connecte → dashboard
2. Voit "Dernier bulletin : Octobre 2026 — 332 066 XAF"
3. Click → page bulletin avec bandeau dark
4. Click "Télécharger PDF" → génération + download
5. Voit le code de vérification (A1B2-C3D4-E5F6) en bas

### Scénario 3 : Demande de congé
1. Employé → "Mes congés" → CTA "Nouvelle demande"
2. Drawer : sélectionne type ANNUAL, dates, motif
3. Soumet → PENDING
4. Manager voit dans son "À approuver" → approuve
5. Notification employé, statut APPROVED, solde mis à jour

### Scénario 4 : Recrutement de bout en bout
1. Recruteur publie une offre (DRAFT → PUBLISHED)
2. Reçoit candidatures (NEW)
3. Drag vers SHORTLISTED
4. Programme entretien → INTERVIEWING
5. Saisit résultat PASS → bouton "Faire une offre" → OFFERED
6. Click "Convertir en employé" → wizard pré-rempli avec données candidat → création Employee + Contract + status HIRED de la candidature

### Scénario 5 : STC d'un employé qui démissionne
1. Admin RH : page employé → bouton "Terminer" → modal date + raison RESIGNATION → statut TERMINATED + dateSortie persistée
2. Responsable Paie : "Soldes de tout compte" → "Calculer un STC" → form avec employee search, departureDate (pré-rempli depuis dateSortie), reason RESIGNATION, unusedLeaveDays (auto-pré-rempli depuis LeaveBalance via backend)
3. Décompte affiché
4. Click "Générer document signé" → PDF avec sceau
5. Click "Marquer payé"

### Scénario 6 : Régularisation rétroactive
1. Responsable Paie : "Régularisations" → "Nouvelle"
2. Form : employee search, originPeriod 2026-09 (mois passé), newBaseSalary 400 000 (corrigé), targetPeriod 2026-11, reason "Promotion rétroactive"
3. Backend recalcule → écran montre delta gross 100 000, delta net 82 066 (Avant/Après)
4. Click "Appliquer" → status APPLIED, delta sera ajouté au prochain run

### Scénario 7 : Configuration d'une nouvelle rubrique
1. Responsable Paie : "Rubriques" → "Nouvelle rubrique"
2. Drawer : code PRIME_RISQUE, label, category EARNING, method RATE, baseReference BASE_SALARY, rate 0.05, countryCode CM, displayOrder 6, effectiveFrom 2026-12-01
3. Active → apparaît dans la liste avec badge active vert
4. Au prochain run, la rubrique est appliquée à tous les bulletins

### Scénario 8 : DRH consulte la masse salariale
1. DRH dashboard : KPI "Masse salariale du mois" + sparkline 6 mois
2. Click → page Analytics
3. Drill-down : courbe masse salariale 12 mois (calculée à partir des PayrollRun.totalGross)
4. Lecture seule : il ne peut pas valider ni modifier

---

# 6. ÉTATS À DESIGNER POUR CHAQUE PAGE CRITIQUE

Pour les pages principales (dashboards des 9 rôles, listes/tableaux principaux, wizards, fiches détail), designe **explicitement** :

1. **Loading** : skeletons précis reproduisant la structure finale (titres, cartes, lignes de tableau)
2. **Happy path** : données réalistes (employés camerounais, montants XAF, dates 2026)
3. **Empty** : illustration légère (icône Lucide grande + texte explicatif) + CTA pour créer la première donnée
4. **Erreur** : message clair + bouton "Réessayer"
5. **Permission insuffisante** : explication + bouton "Contacter votre admin"
6. **Action en cours** : boutons disabled + spinner + banner de progression si long
7. **Succès post-action** : toast + mise à jour optimiste

---

# 7. CONTRAINTES MÉTIER À RESPECTER

1. **Multi-tenant strict** : chaque écran affiche le contexte (tenant + organization + agency) en haut. Switch contexte = invalidation cache + refetch complet.
2. **Permissions UI** : les boutons d'actions critiques (créer employé, lancer paie, valider, approuver) ne sont visibles QUE pour les rôles autorisés. KSM enforce de toute façon, mais le gating UI évite la frustration.
3. **Devise XAF** par défaut, format "1 250 000 XAF" (espace milliers, code après).
4. **Format date français** : JJ/MM/AAAA pour l'affichage, ISO YYYY-MM-DD pour les inputs.
5. **Format période** : YYYY-MM partout (le backend l'utilise ainsi).
6. **i18n** : tous les libellés via `next-intl` → JSON `fr/...` et `en/...`. Pas de hardcoded.
7. **Immutabilité** : un bulletin validé n'est pas modifiable. Affiche-le en mode lecture seule avec icône cadenas.
8. **Statuts visuels** : utilise le mapping couleur du §1.1.
9. **Affichage des sceaux de documents** : verification code en font-mono très visible sur le PDF, code dans une chip orange dans la liste des documents.
10. **Pas d'inventions** : tu n'ajoutes pas un onglet "Avenants", "Accidents du travail", "Vaccinations", "Saisie quotidienne timesheet", "Compétences acquises post-formation", "OKR", "360°", "Forced ranking", "Plan de développement", "Carte bancaire entreprise", "Multi-devises", "OCR de reçus", "Calendrier jours fériés", "Calibration", "Pool de talents". Ces fonctionnalités **n'existent pas** côté backend.

---

# 8. DÉTAILS UI/UX NON NÉGOCIABLES

- **Sidebar** filtrée par rôle, profondeur 2 max (sections → items).
- **Command Palette (Cmd+K)** dans tous les workspaces : recherche employés, bulletins, pay-elements (selon rôle).
- **Notifications** centre en haut à droite : congés à approuver, échéances déclaratives, cycle terminé.
- **Dark mode** : pas demandé pour le moment, **fais clair only** (l'identité chaleur orange ne fonctionne pas en dark).
- **Densité tableau** : option compact/normal (compact par défaut dans le workspace Paie).
- **Export** : CSV / Excel / PDF sur tous les tableaux principaux.
- **Bulk actions** : sélection multiple sur les listes principales (approbations groupées si même type).
- **Comparaisons N-1** : indiquées par delta % à côté des KPI (calcul frontend si données dispo).
- **Tooltips** sur tous les termes techniques (CNPS, IRPP, CAC, RAV, TDL, CFC, FNE).

---

# 9. LIVRABLE ATTENDU

1. **Design system** : pages présentation des tokens (couleurs, typo, espacement, ombres) et des composants atomiques (Button, Input, Badge, Card, Table, Drawer, Modal, Stepper, Toast, EmptyState).
2. **Sidebars de chaque rôle** (9) avec leurs items.
3. **Dashboard de chaque rôle** (9) avec ses KPI + sections.
4. **Pages principales** par rôle (estimé total : ~80 écrans uniques).
5. **5-7 variations** par écran critique (loading/happy/empty/erreur/permission).
6. **8 storyboards** des scénarios du §5.
7. **Tableau "écran ↔ endpoint(s) appelé(s)"** comme annexe pour validation que rien n'est inventé.

---

# 10. CHECKLIST DE VALIDATION AVANT LIVRAISON

Avant de me livrer ton design, vérifie pour chaque écran :

- [ ] Chaque champ de formulaire correspond à un champ du domain model backend
- [ ] Chaque colonne de tableau correspond à un champ retourné par un endpoint
- [ ] Chaque bouton d'action correspond à un endpoint réel
- [ ] Chaque statut visible correspond à une valeur d'enum réelle
- [ ] Les couleurs sont uniquement celles du §1.1
- [ ] La typo est uniquement Inter / Inter Tight / JetBrains Mono
- [ ] Les rayons sont uniquement xs/sm/md/lg/xl/2xl du §1.3
- [ ] Aucune fonctionnalité de la liste "N'existe PAS" du §3 n'est dessinée
- [ ] Chaque écran est gateable par une permission backend explicite

Si **une seule** case n'est pas cochée, retravaille avant de livrer.

---

**Commence par le design system (§1) en grand format, puis le dashboard du Responsable Paie (workspace le plus distinctif), puis l'employé (le plus utilisé), puis enchaîne les autres rôles. Pour chaque écran, place une légende "Endpoint(s) : ..." sous le titre, pour prouver la traçabilité.**
