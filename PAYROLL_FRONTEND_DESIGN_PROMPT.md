# PROMPT DESIGN — Module Paie (workspace dédié + insertions HRM)

## CONTEXTE GLOBAL

Je construis le frontend d'un **module Paie** qui s'intègre dans une plateforme HRM SaaS multi-tenant déjà en production. Backend : deux modules Spring Boot séparés (`hrm_core` et `payroll_core`), le module Paie consomme uniquement les données employé via un port d'intégration — il est totalement autonome.

**Cible** : entreprises camerounaises, zone CEMAC/OHADA. Locale principale **français**, locale secondaire anglais. Devise XAF.

**Architecture frontend retenue** : **un seul Next.js**, mais le module Paie se manifeste de **deux manières distinctes** :

1. **Un workspace dédié `/payroll/*`** — produit dans le produit, identité visuelle propre, sidebar dédiée — réservé au **Responsable Paie** (et lecture seule pour l'Admin RH)
2. **Des insertions ciblées dans les sidebars HRM existantes** des autres rôles (Employé, Comptable, DRH, Contrôleur) — qui consultent ou agissent ponctuellement sur la paie depuis leur HRM normal

L'objectif : que la Paie ressemble à un **vrai outil métier autonome** quand le Responsable Paie y entre, tout en restant **invisible** pour les rôles qui n'en ont pas besoin, et **intégrée naturellement** pour les rôles qui consomment ses données.

---

## STACK TECHNIQUE (à respecter strictement)

- **Next.js 16** App Router, React Server Components, Turbopack — *attention : breaking changes vs versions antérieures*
- **TypeScript** strict
- **Tailwind CSS v4** (variables CSS, design tokens)
- **shadcn/ui** comme base (Radix UI sous le capot)
- **Lucide React** pour les icônes (pas d'emojis dans l'UI)
- **Recharts** pour les graphes
- **React PDF** pour les bulletins
- **React Hook Form + Zod** pour les formulaires
- **TanStack Query** pour la data, **Zustand** pour l'état client
- **next-intl** pour i18n

Les écrans doivent être :
- **Réactifs** : skeletons précis pendant le loading, états optimistes pour les mutations
- **Accessibles** : navigation clavier complète, ARIA, contraste WCAG AA
- **Responsifs desktop-first** ; mobile soigné uniquement pour les pages employé (self-service) et le portail Comptable
- **Multi-tenant** : sticky context switcher (organisation + agence) en haut de chaque page

---

## RÔLES ET PARTITIONNEMENT URL

La plateforme est déjà partitionnée par rôle. Chaque rôle a son namespace HRM existant. **Tu ne dois rien casser de ces sidebars HRM** — tu y **ajoutes** des entrées Paie où c'est pertinent.

| Rôle | Namespace HRM existant | Périmètre Paie |
|---|---|---|
| **Responsable Paie** | `/payroll-manager/*` | Entrée "Aller dans Paie →" qui ouvre le workspace dédié |
| **Admin RH** | `/hr-admin/*` | Carte dashboard "Cycle en cours" + lecture seule workspace Paie |
| **Comptable / DAF** | `/accountant/*` | Section "Paie" dans sa sidebar : validation, écritures, ordres |
| **DRH** | `/drh/*` | Widget analytique "Masse salariale" |
| **Employé** | `/employee/*` | Section "Mes bulletins" dans sa sidebar |
| **Manager d'équipe** | `/manager/*` | Rien — les variables se valident dans Timesheets |
| **Médecin du travail** | `/doctor/*` | Rien |
| **Recruteur** | `/recruiter/*` | Rien |
| **Contrôleur RH** | `/controller/*` | Carte dashboard "KPI paie" en lecture seule |

---

## PARTIE 1 — LE WORKSPACE PAIE DÉDIÉ (`/payroll/*`) — 80% DU BRIEF

C'est ici que tu peux exprimer une **identité visuelle dédiée**. Le Responsable Paie y passe ses journées. Le workspace ressemble à un produit autonome (style Linear, Stripe Dashboard, Ramp, Mercury) tout en restant cohérent avec le design system global de la plateforme.

### 1.1 Identité visuelle du workspace Paie

- **Couleur signature** : un indigo profond ou bleu-violet électrique (style Linear `#5E6AD2`) qui se distingue du primaire HRM mais reste de la même famille
- **Sidebar** : sombre par défaut même en mode clair (charbon `zinc-900`), texte clair, accent indigo sur l'item actif
- **Header workspace** : bandeau sticky avec à gauche un bouton **"← Retour HRM"** discret mais visible, au centre le sélecteur de période + organisation/agence, à droite le statut du cycle en cours + notifications
- **Typographie** : Inter Display pour les titres, Geist Mono pour tous les montants, codes de rubrique, matricules, numéros CNPS
- **Densité** : option "compact" par défaut dans les tableaux (le Responsable Paie veut voir beaucoup d'infos d'un coup)
- **Tabular numbers** activés systématiquement pour les colonnes de montants

### 1.2 Transition HRM → Workspace Paie

Designe explicitement :
- **Côté HRM** : l'item de sidebar "Aller dans Paie →" du Responsable Paie (avec petite flèche, séparateur visuel)
- **Animation de transition** : fondu rapide vers le workspace, le header change d'identité visuelle, la sidebar HRM est remplacée par la sidebar Paie
- **Côté Paie** : le bouton "← Retour HRM" en haut à gauche qui ramène vers le dashboard HRM du Responsable Paie
- **Bandeau "Vous êtes dans le module Paie"** discret la première fois qu'on entre, dismissible

### 1.3 Sidebar du workspace Paie

Sections :
- **Pilotage** : Dashboard
- **Cycles** : Cycles de paie, Saisie variables, Régularisations
- **Configuration** : Rubriques, Barèmes fiscaux, Tables de référence
- **Sortie** : Bulletins, Ordres de paiement, Déclarations
- **Spécial** : Soldes de tout compte, Saisies sur salaire, Simulateur
- **Analyse** : Analytique, Audit

### 1.4 Les ~18 pages du workspace Paie

Pour chaque page, designe : état happy path, loading (skeleton précis), empty state, erreur, et les drawers/modales ouverts depuis la page.

#### P1. `/payroll/dashboard` — Tableau de bord paie
- Hero : période en cours (ex. "Octobre 2026"), stepper horizontal des 9 statuts (`DRAFT → VARIABLES_LOCKED → CALCULATED → REVIEW → VALIDATED → APPROVED → PAYMENT_INITIATED → PAID → CLOSED`), bouton primaire **"Lancer la paie"** si pas encore lancé
- 4 cartes KPI : Masse salariale brute (delta M-1), Net total à verser (sparkline 6 mois), Cotisations CNPS à reverser (compte à rebours échéance 15), Effectif payé / effectif total
- Bloc "Anomalies & Alertes" actionnable (employés sans contrat, écarts > 20%, rubriques manquantes)
- Bloc "Échéances déclaratives" : tuiles DIPE/CNPS/IRPP avec compte à rebours
- Graphe principal : masse salariale 12 mois décomposée brut/charges/net
- Activité récente : journal des actions

#### P2. `/payroll/runs` — Liste des cycles
- Toggle vue liste / calendrier annuel
- Filtres : période, statut multi-select, agence, type (régulier/complémentaire/13ème mois/prime exceptionnelle)
- Tableau dense : Période, Agence, Type, Statut (badge), Effectif, Brut, Net, Cotisations, Validation, Actions
- Hover ligne : actions rapides (Voir, Dupliquer, Exporter)

#### P3. `/payroll/runs/new` — Wizard de lancement (5 étapes)
Stepper en haut, contenu dynamique :
1. **Configuration** : période, agence, type, date paiement, mode (calcul/simulation)
2. **Variables** : tableau éditable des employés (heures sup, primes, absences, acomptes) + import Excel/CSV + import Timesheets
3. **Aperçu** : récapitulatif + top 10 écarts vs M-1 + drill-down
4. **Confirmation** : checklist visuelle + bouton danger "Lancer le calcul"
5. **Calcul en cours** : progression employé par employé, log streaming des étapes

#### P4. `/payroll/runs/[id]` — Détail cycle
Layout 3 colonnes :
- **Gauche sticky** : carte d'identité (période, statut avec stepper vertical, totaux, validateurs), boutons d'action contextuels selon le statut
- **Centre** : 4 onglets (Vue d'ensemble avec graphes, Bulletins liste filtrable, Anomalies, Audit timeline)
- **Droite** : drawer qui s'ouvre sur la fiche bulletin sélectionnée

#### P5. `/payroll/runs/[id]/entries/[entryId]` — Détail bulletin
- Header sticky : identité employé (photo, matricule, catégorie, agence)
- Section "Bulletin" : `PayslipLine`s groupées par catégorie (Gains, Retenues, Charges patronales info)
- Section "Cumuls annuels" : brut/net/IRPP/CNPS cumulés depuis janvier
- Section "Paiement" : mode, compte, statut, référence
- Section "Congés" : solde début/fin de mois
- Aperçu PDF fidèle côté droit, scrollable
- Boutons : PDF, Email, Corriger (ouvre formulaire d'ajustement), Recalculer

#### P6. `/payroll/variables` — Saisie des variables mensuelles
- Sélecteur période + boutons "Importer Excel/CSV" et "Importer Timesheets"
- Tableau éditable inline : Matricule, Nom, Catégorie, Heures sup (jour/nuit), Primes, Absences sans solde, Acomptes
- Sauvegarde auto sur blur
- Indicateur de progression "187 / 248 saisis"
- Bouton "Verrouiller la saisie" en bas

#### P7. `/payroll/pay-elements` — Catalogue des rubriques
- Filtres : Catégorie (Gain/Retenue/Charge patronale/Info), Méthode (RATE/BRACKET/FLAT/LOOKUP), Pays, Actif/Inactif
- Tableau : Code (mono), Libellé, Catégorie (badge), Méthode, Taux/ref barème, Plafond, Base, Imposable, Cotisable, Date d'effet, Statut
- Click → drawer d'édition

#### P8. `/payroll/pay-elements/[code]` — Édition rubrique (drawer)
Onglets :
- **Configuration** : Code, Libellé FR/EN, Catégorie, Pays, Date d'effet
- **Calcul** : Méthode, Base de référence, Taux, Plafond, Plancher
- **Soumissions** : Imposable, Cotisable, Inclus dans le net imposable
- **Versions** : timeline historique
- **Test** : simulateur (entrer un brut, voir le calcul résultant)

#### P9. `/payroll/tax-brackets` — Barèmes fiscaux
- Liste des tables (IRPP_CM_2026, IRPP_CM_2025, …)
- Édition tranches Min/Max/Taux
- Courbe progressive visualisée
- Bouton "Cloner + nouvelle date d'effet"

#### P10. `/payroll/lookup-tables` — Tables RAV/TDL
Édition barèmes forfaitaires Min/Max/Montant.

#### P11. `/payroll/declarations` — Déclarations sociales et fiscales
- Onglets : CNPS / DIPE / IRPP / Bordereau
- Liste des déclarations (Période, Statut, Génération, Envoi, Fichier)
- Workflow visuel `DRAFT → GENERATED → SUBMITTED → ACKNOWLEDGED`
- Bouton "Générer pour la période en cours"
- Aperçu du fichier généré, download au format officiel

#### P12. `/payroll/payments` — Ordres de paiement
- Liste des batches (Période, Mode, Nombre, Montant, Statut)
- Détail batch : instructions individuelles, bouton "Télécharger fichier bancaire (XML CEMAC)", bouton "Pousser vers MTN MoMo / Orange Money", statuts mis à jour via callbacks

#### P13. `/payroll/retroactive` — Régularisations rétroactives
- Tableau : Employé, Période d'origine, Période de paiement, Motif, Montant, Statut
- Wizard "Nouvelle régularisation" : sélection employé, période à recalculer, motif
- Vue comparative Avant/Après par mois

#### P14. `/payroll/final-settlements` — Soldes de tout compte
- Liste employés en départ
- Assistant STC : salaire prorata, indemnité congés non pris, préavis, indemnité licenciement (selon ancienneté), gratifications prorata, soldes prêts à apurer, net STC
- Bouton "Générer document légal" (PDF signé)

#### P15. `/payroll/garnishments` — Saisies sur salaire
- Liste : Employé, Bénéficiaire, Type (pension alimentaire / saisie Trésor / créancier), Total, Restant dû, Mensualité, Priorité
- Création nouvelle saisie + calcul quotité saisissable automatique

#### P16. `/payroll/simulator` — Simulateur
4 modes :
- **Brut → Net** : entrée brut → détail cotisations/impôts → net
- **Net → Brut** : calcul inverse depuis un net cible
- **What-if global** : augmentation X% sur catégorie Y → impact masse salariale
- **Embauche** : coût employeur total d'un nouveau recrutement

#### P17. `/payroll/analytics` — Analytique paie
- Graphes Recharts interactifs : masse salariale par département/agence/catégorie sur 12 mois, coût employeur vs net, répartition cotisations, effectif vs masse salariale
- Filtres : période, agence, département, catégorie
- Export CSV/PDF

#### P18. `/payroll/audit` — Journal d'audit
- Timeline filtrable de toutes les actions critiques (validation, modification rubrique, recalcul, génération déclaration)
- Qui / Quand / Quoi / Avant / Après

### 1.5 Composants signature du workspace Paie

Designe en détail :
1. **PayrollRunStatusStepper** (horizontal + vertical, interactif)
2. **PayslipPreview** (aperçu fidèle du PDF, utilisé en P5, P14, P16)
3. **PayElementRow** (libellé + base + taux + montant + tooltip formule)
4. **AmountInput** (auto-format espaces milliers, devise, indicateur delta)
5. **AnomalyCard** (sévérité info/warning/error, employés concernés, résolution inline)
6. **PeriodPicker** (sélecteur mensuel avec nav rapide année)
7. **CalculationTraceViewer** (déroulé étape par étape : brut → cotisations → impôts → net avec formules)
8. **PayrollKpiCard** (statistique avec mini-graphe et delta M-1)
9. **DeclarationStatusBadge** (badge avec workflow visuel intégré)
10. **PaymentChannelChip** (Virement bancaire / MTN MoMo / Orange Money / Espèces avec icône)

---

## PARTIE 2 — INSERTIONS PAIE DANS LES SIDEBARS HRM EXISTANTES (15% DU BRIEF)

**Règle absolue** : tu ne touches pas aux sidebars HRM existantes. Tu ajoutes uniquement les items listés ci-dessous, exactement à la place indiquée, **dans le design system HRM existant** (mêmes tokens, mêmes composants, mêmes patterns que le reste de HRM).

### 2.1 Sidebar EMPLOYÉ (`/employee/*`)

Ajout dans la section **Personnel** :
- "Mes bulletins de paie" → `/employee/payslips`

#### Écrans à designer (4) — mobile-first, design system HRM
- **`/employee/payslips`** : liste vue timeline ou grille de cartes, filtre année, carte "Récap annuel" (brut/net cumulé) en haut
- **`/employee/payslips/[entryId]`** : aperçu PDF stylisé du bulletin + section "Comprendre mon bulletin" avec tooltips explicatifs ligne par ligne + bouton Download + lien vers simulateur
- **`/employee/payslips/annual`** : récap annuel, graphe d'évolution, tableau récapitulatif pour aide déclaration fiscale, bouton "Demander attestation fiscale"
- **`/employee/payslips/documents`** : attestations de travail, attestations fiscales, STC, demandes en cours

### 2.2 Sidebar COMPTABLE (`/accountant/*`)

Ajout d'une section **"Paie"** dans la sidebar (sous Notes de frais existant) :
- "Validation paie" → `/accountant/payroll-validation`
- "Écritures paie" → `/accountant/payroll-journal`
- "Ordres de paiement" → `/accountant/payroll-payments`

#### Écrans à designer (5) — design system HRM
- **`/accountant/payroll-validation`** : file d'attente des cycles en attente, tableau avec montants, bouton "Valider" qui ouvre la fiche de validation
- **`/accountant/payroll-validation/[runId]`** : écran de validation détaillé avec rapprochement comptable, comparaison vs prévisionnel, boutons "Valider" / "Renvoyer pour correction"
- **`/accountant/payroll-journal`** : tableau des écritures comptables OHADA générées par cycle, regroupées par compte
- **`/accountant/payroll-journal/[entryId]`** : détail écriture avec débit/crédit, compte SYSCOHADA, libellé
- **`/accountant/payroll-payments`** : suivi des ordres de paiement, rapprochement bancaire, statuts individuels

### 2.3 Sidebar DRH (`/drh/*`)

Ajout dans la section **Pilotage** existante :
- "Masse salariale" → `/drh/payroll-analytics` (lecture seule)

#### Écran à designer (1) — design system HRM
- **`/drh/payroll-analytics`** : dashboard analytique haut niveau, masse salariale annuelle, évolution effectif, ratios, drilldown par BU/département/catégorie, comparaisons N-1

### 2.4 Sidebar ADMIN RH (`/hr-admin/*`)

Pas d'ajout de section, mais ajout d'un item en bas de la section **Personnel** existante :
- "Bulletins de mes employés" → `/hr-admin/payroll-readonly` (lecture seule, recherche par employé/période, redirige vers le workspace Paie en mode lecture)

### 2.5 Sidebar CONTRÔLEUR RH (`/controller/*`)

Ajout dans la section **Pilotage** existante :
- "Indicateurs paie" → `/controller/payroll-kpi` (lecture seule)

### 2.6 Sidebars sans ajout

Aucun changement pour : **Manager**, **Médecin du travail**, **Recruteur**, **Admin système**.

---

## PARTIE 3 — DASHBOARDS HRM PAR RÔLE : WIDGETS PAIE (5% DU BRIEF)

Sur le dashboard HRM de chaque rôle, designe les widgets Paie qui s'insèrent **parmi les autres widgets existants du dashboard**. Pas de page séparée — un widget ou une carte par rôle.

| Rôle | Widget Paie à designer sur son dashboard HRM |
|---|---|
| **Employé** | Carte "Dernier bulletin" : période + net du mois + statut paiement + bouton "Voir le bulletin" |
| **Admin RH** | Carte "Cycle de paie en cours" : période, statut, effectif, brut prévisionnel, lien vers workspace Paie |
| **Comptable** | Carte "À valider" (nombre de cycles + montant total) + carte "Échéances CNPS/IRPP" (compte à rebours) |
| **DRH** | Carte "Masse salariale du mois" avec sparkline 6 mois + delta vs N-1 |
| **Contrôleur** | Carte "KPI paie" : effectif payé, brut moyen, top 3 alertes |
| **Responsable Paie** | Bandeau hero "Cycle en cours" avec statut + bouton primaire "Entrer dans le workspace Paie →" |
| **Manager / Médecin / Recruteur** | Rien |

---

## PARTIE 4 — DESIGN SYSTEM PARTAGÉ HRM + PAIE

**Un seul design system** pour toute la plateforme. Le workspace Paie utilise les mêmes tokens, composants, patterns que HRM — juste avec :
- Une **couleur d'accent secondaire** (indigo Paie) qui marque visuellement qu'on est dans la Paie
- Une **densité de tableau** par défaut "compact" dans le workspace Paie
- Une **sidebar sombre** par défaut dans le workspace Paie (même en mode clair)

À produire **en premier** avant les écrans :

### 4.1 Tokens
- **Couleurs** : modes clair + sombre, primaire HRM (à conserver), secondaire Paie (indigo), sémantiques (success/warning/danger/info), couleurs domaines paie (gains=vert, retenues=ambre, charges patronales=violet, info=graphite)
- **Typographie** : échelle display → caption, monospace pour montants/codes/matricules, tabular numbers activés
- **Espacement** : 4/8/12/16/24/32/48/64/96
- **Border-radius** : sm 6 / md 10 / lg 14 / xl 20 / pill
- **Élévations** : subtle / small / medium / large

### 4.2 Composants atomiques
Button, Input, AmountInput, Badge, Avatar, Tooltip, Popover, Switch, Checkbox, Radio, Select, DatePicker, PeriodPicker

### 4.3 Composants moléculaires
Card, KpiCard, Drawer, Modal, Table (sortable + filterable + sticky header + sélection multiple + pagination + infinite scroll), Tabs, Stepper (horizontal + vertical), EmptyState, Toast, CommandPalette (cmd+K)

---

## PARTIE 5 — ÉTATS À DESIGNER POUR CHAQUE PAGE CRITIQUE

Pour les pages **P1, P3, P4, P5, P6, P7, P11, P12** du workspace Paie et **/employee/payslips, /accountant/payroll-validation/[runId], /drh/payroll-analytics**, designe explicitement :

1. **Loading** : skeletons précis qui reflètent la structure finale
2. **Happy path** : avec données réalistes
3. **Empty** : illustration légère + CTA
4. **Error** : message clair + retry
5. **Permission insuffisante** : explication + CTA "Contacter votre administrateur"
6. **Action en cours** : boutons disabled, banner de progression, mise à jour optimiste
7. **Succès post-action** : toast + confirmation visuelle

---

## PARTIE 6 — STORYBOARDS BOUT-EN-BOUT (6 SCÉNARIOS)

Mets en scène ces parcours sous forme de planches successives :

### Storyboard 1 : "Le 28 du mois — Run mensuel"
Le Responsable Paie ouvre HRM, voit le bandeau "Cycle d'octobre non lancé" sur son dashboard HRM, clique "Entrer dans le workspace Paie". Transition vers le workspace. Il va sur "Saisie variables", importe les timesheets, contrôle les heures sup, lance le calcul via le wizard. Pendant le calcul, il voit la progression. Une fois terminé, il consulte les anomalies, en résout une, transmet à la validation. Notification poussée au Comptable.

### Storyboard 2 : "Le Comptable valide"
Le Comptable se connecte, voit sur **son dashboard HRM** la carte "À valider — 1 cycle 245M XAF". Clique. Atterrit sur `/accountant/payroll-validation`, voit la liste. Ouvre la fiche du cycle, vérifie les totaux, le rapprochement, signe la validation. Le système génère automatiquement les ordres de paiement.

### Storyboard 3 : "L'employé consulte son bulletin"
L'employé reçoit une notification mobile "Votre bulletin d'octobre est disponible". Ouvre l'app, atterrit sur son dashboard HRM, voit la carte "Dernier bulletin". Clique. Atterrit sur `/employee/payslips/[entryId]`. Lit l'aperçu PDF, télécharge, lit les explications via "Comprendre mon bulletin". Va sur le simulateur personnel et joue avec son brut.

### Storyboard 4 : "Régularisation rétroactive"
Un employé a obtenu une promotion effective au 1er septembre, traitée en novembre. Le Responsable Paie va dans le workspace Paie, ouvre "Régularisations", crée une nouvelle régularisation. L'écran calcule le delta, affiche le comparatif Avant/Après pour septembre et octobre. Le delta sera intégré au prochain run de novembre.

### Storyboard 5 : "Configurer une nouvelle rubrique"
Le Responsable Paie va dans "Rubriques" dans le workspace Paie. Clique "Nouvelle rubrique". Drawer s'ouvre. Remplit : code `PRIME_RISQUE`, méthode RATE, 5% du salaire de base, catégorie X, date d'effet 1er du mois suivant. Onglet "Test" : entre un brut, voit le calcul résultant. Active. Au prochain run, la rubrique apparaît automatiquement sur les bulletins éligibles.

### Storyboard 6 : "DRH consulte la masse salariale"
La DRH se connecte. Sur **son dashboard HRM**, elle voit la carte "Masse salariale du mois" avec delta vs N-1. Clique sur la carte → atterrit sur `/drh/payroll-analytics`. Voit les graphes 12 mois, drilldown par BU. Tout en lecture seule, aucun bouton d'action. Reste dans son namespace HRM.

---

## PARTIE 7 — CONTRAINTES BUSINESS À RESPECTER

1. **Multi-tenant strict** : chaque écran porte explicitement le contexte tenant + organisation + agence dans le header
2. **Audit-friendly** : chaque action critique demande confirmation et trace "qui, quand, pourquoi"
3. **Permissions UI** : les boutons d'action ne sont visibles que pour les rôles autorisés (gating frontend, KSM enforce authoritativement)
4. **Immutabilité** : un bulletin validé ne se modifie plus — seul un ajustement crée une nouvelle ligne
5. **Conformité légale Cameroun** : le bulletin de paie doit respecter le Code du travail Art. 68 — en-têtes employeur/employé complets, mentions obligatoires, montant en lettres
6. **Devise XAF par défaut**, EUR/USD possibles pour expatriés
7. **Format date français** : JJ/MM/AAAA
8. **Format montant** : espace comme séparateur des milliers, code devise après : `1 250 000 XAF`
9. **Aucune fuite cross-tenant** : changement de contexte = invalidation cache + refetch complet

---

## PARTIE 8 — DÉTAILS UI/UX NON NÉGOCIABLES

- **Sidebar Paie** dédiée au workspace, **distincte** de la sidebar HRM
- **Sticky header context switcher** (organisation + agence) en haut de chaque page du workspace Paie
- **Breadcrumbs** systématiques en haut de chaque page de détail (avec retour HRM en première position quand pertinent)
- **Command Palette (cmd+K)** dans le workspace Paie : recherche employés, bulletins, cycles, rubriques
- **Centre de notifications** en haut à droite : cycle terminé, anomalie détectée, échéance déclarative, validation en attente
- **Dark mode** soigné, switch dans le menu utilisateur
- **Densité tableau** : option compact/normal/confortable, compact par défaut dans Paie
- **Export** : tous les tableaux ont CSV / Excel / PDF
- **Filtres avancés** : sauvegarde en presets nommés
- **Bulk actions** : sélection multiple sur les tableaux principaux
- **Comparaisons M-1** : toutes les valeurs chiffrées affichent un delta % vs M-1
- **Tooltips explicatifs** sur tous les termes techniques (CNPS, IRPP, CAC, RAV, TDL, CFC, FNE)
- **Glossaire intégré** accessible depuis le menu utilisateur

---

## PARTIE 9 — RÉFÉRENCES VISUELLES À ÉMULER

Pour le **workspace Paie** :
- **Linear** (densité, typographie, micro-interactions)
- **Stripe Dashboard** (tableaux, formulaires, navigation)
- **Ramp / Mercury** (KPI cards, finance UX premium)
- **Deel** (paie internationale moderne)
- **Notion** (drawers, command palette)

Pour les **insertions HRM** :
- Reste **strictement dans la continuité du HRM existant**
- Sobriété maximale, aucune fioriture, le contenu prime

---

## PARTIE 10 — TON ET PERSONNALITÉ

Le **workspace Paie** doit transmettre : **rigueur, confiance, expertise métier, modernité**. Le Responsable Paie doit se sentir aux commandes d'un outil professionnel sérieux et premium. Aucun ton "fun startup", aucun emoji, aucune fioriture — c'est un outil métier exigeant.

Les **insertions HRM** doivent rester invisibles dans le sens où elles s'intègrent naturellement, sans rupture de cohérence avec le reste de la plateforme HRM.

L'**Employé** doit se sentir respecté et bien informé : son bulletin est compréhensible, ses droits sont clairs, ses documents légaux accessibles facilement.

---

## LIVRABLE ATTENDU

1. **Design system complet** : pages de présentation des tokens et composants partagés HRM/Paie
2. **Workspace Paie complet** : ~18 pages avec états (loading/happy/empty/error/permission/action en cours/succès)
3. **Insertions HRM** : tous les écrans listés dans la partie 2, dans le design system HRM existant
4. **Widgets dashboards HRM** : un par rôle (partie 3)
5. **6 storyboards** bout-en-bout
6. **Annexe** : guide d'utilisation du design system, principes d'interaction, motion principles, matrice rôle × écrans accessibles

---

**Démarre par le design system partagé, puis le dashboard P1 du workspace Paie, puis enchaîne le workspace Paie dans l'ordre des pages, puis les insertions HRM rôle par rôle.**
