# PROMPT DESIGN — Frontend du module Payroll (RT-comops-payroll-core)

## CONTEXTE

Je construis le **frontend du nouveau module Payroll** d'une plateforme HRM SaaS multi-tenant déployée pour des entreprises camerounaises (zone CEMAC/OHADA). Ce module remplace toute la logique de paie qui était auparavant dans le module HRM générique. Il devient un domaine fonctionnel à part entière, avec son propre namespace de pages, ses propres écrans, sa propre identité visuelle.

Tu dois me livrer un **design futuriste, premium, dense en information mais lisible**, dans l'esprit des produits comme **Linear, Vercel, Stripe Dashboard, Ramp, Pleo, Deel, Mercury, Notion**. Pas de skeuomorphisme, pas de gradients criards, pas de "neon glow" excessif. Un design **éditorial, technique, sobre, premium, avec une typographie soignée et des micro-interactions raffinées**. Mode sombre par défaut, mais le mode clair doit être tout aussi soigné.

La plateforme est en **français (locale principale)** avec une version anglaise. Tous les libellés, écrans, tableaux, formulaires sont rédigés en français pour le design principal.

---

## STACK TECHNIQUE (à respecter dans le design)

- **Next.js 16** App Router, React Server Components, Turbopack
- **TypeScript** strict
- **Tailwind CSS v4** (variables CSS, design tokens)
- **shadcn/ui** comme base de composants (Radix UI sous le capot)
- **Lucide React** pour les icônes (pas d'emojis dans l'UI)
- **Recharts** pour les graphes
- **React PDF** pour les bulletins exportables
- **React Hook Form + Zod** pour les formulaires
- **TanStack Query** pour la data, **Zustand** pour le state client
- **next-intl** pour i18n

Tous les écrans doivent être pensés pour être :
- **Réactifs** : skeletons pendant le chargement, états optimistes pour les mutations
- **Accessibles** : navigation clavier complète, ARIA, contraste WCAG AA
- **Responsifs** : desktop d'abord (vrai usage métier), tablette tolérée, mobile pour le self-service employé uniquement
- **Multi-tenant** : header avec sélecteur d'organisation et d'agence en haut de chaque page

---

## IDENTITÉ VISUELLE À PRODUIRE

### Palette
- **Primaire** : un bleu-violet électrique premium type Linear (#5E6AD2) ou un indigo profond (#4F46E5)
- **Surfaces** : blancs cassés en mode clair, charbons en mode sombre (zinc-950 / zinc-900 / zinc-800)
- **Couleurs sémantiques** : success (emerald), warning (amber), danger (rose), info (sky)
- **Couleurs domaines** :
  - Gains/Earnings : vert sapin
  - Retenues/Deductions : ambré-doré
  - Cotisations patronales : violet-indigo
  - Information : gris graphite
- **Texte** : hiérarchie claire entre titre, sous-titre, label, métadonnée

### Typographie
- **Headings** : Inter Display ou Geist Sans, tracking serré pour les gros titres
- **Body** : Inter ou Geist Sans
- **Monospaced** : Geist Mono ou JetBrains Mono pour les montants, codes de rubrique, numéros CNPS, matricules
- **Tabular numbers** activés pour toutes les colonnes de montants

### Composants signature à designer en premier
1. **Carte KPI** (statistique de tableau de bord avec mini-graphe et delta)
2. **Tableau de données** (sortable, filterable, sticky header, sélection multiple, pagination, infinite scroll)
3. **Drawer/Sheet latéral** (pour fiches détail et formulaires sans changement de page)
4. **Modale de confirmation critique** (pour valider la paie, signer un STC)
5. **Stepper horizontal** (pour le workflow de cycle de paie)
6. **Badge de statut** (couleurs sémantiques avec point coloré)
7. **Empty state** (illustration légère + CTA)
8. **Toast de notification** (avec progression, action de défaire)
9. **Composant "Pay element row"** (ligne de bulletin avec base, taux, montant)
10. **Composant "Payslip preview"** (aperçu du bulletin PDF stylisé)

---

## ARCHITECTURE PAR RÔLE (ce que tu dois designer)

La plateforme est **partitionnée par rôle** : chaque rôle a son propre namespace URL `/{role-slug}/...` et sa propre sidebar.

Le module Payroll concerne **5 rôles distincts**, chacun avec une expérience différente. Tu dois designer **toutes les pages de chaque rôle**.

| Rôle | Slug URL | Sidebar Section | Périmètre Payroll |
|---|---|---|---|
| **Responsable Paie** | `/payroll-manager/...` | Toutes les sections | Cœur métier : configurer, lancer, vérifier, déclarer |
| **Admin RH** | `/hr-admin/payroll/...` | Section Rémunération | Lance la paie, gère les rubriques de base |
| **Comptable / DAF** | `/accountant/payroll/...` | Section Paie & Trésorerie | Valide, comptabilise, génère ordres de paiement |
| **DRH** | `/drh/payroll/...` | Section Rémunération | Vue stratégique, KPI, analytics |
| **Employé** | `/employee/payslips/...` | Section Personnel | Self-service : consultation bulletins, cumuls, simulations |

---

## SCÉNARIOS UTILISATEURS DÉTAILLÉS PAR PAGE

Pour chaque page ci-dessous, tu dois designer :
- L'écran principal (état "happy path" avec données)
- Les états dérivés (loading, empty, error)
- Les variants pertinents (mobile pour les pages employé, hover et focus pour les éléments interactifs)
- Les modales et drawers ouverts depuis la page

---

### A) RESPONSABLE PAIE (`payroll-manager`)

C'est le **persona principal** du module. Designe avec le plus de soin. ~18 pages.

#### A1. `/payroll-manager/dashboard` — Tableau de bord paie

**Objectif** : vue à 360° du dernier cycle de paie + alertes.

**Contenu** :
- **Bandeau hero** : période en cours (ex. "Octobre 2026"), statut global du cycle (un grand stepper horizontal : DRAFT → VARIABLES_LOCKED → CALCULATED → REVIEW → VALIDATED → APPROVED → PAID), date du prochain run automatique, bouton **"Lancer la paie"** primaire si pas encore lancé
- **4 cartes KPI** :
  1. Masse salariale brute du mois (avec variation vs M-1)
  2. Net total à verser (avec mini-graphe sparkline sur 6 mois)
  3. Cotisations CNPS à reverser (avec compteur de jours avant échéance du 15)
  4. Nombre d'employés dans le run (vs effectif total)
- **Bloc Anomalies & Alertes** (liste actionnable) :
  - Employés sans contrat actif détectés (3)
  - Écart de plus de 20% vs M-1 sur 5 employés (lien vers comparateur)
  - Plafond CNPS atteint ce mois pour 12 employés
  - Rubrique "Prime ancienneté" non calculée pour 2 employés (date d'embauche manquante)
- **Bloc "Échéances déclaratives"** : tuiles cliquables vers DIPE, CNPS, IRPP avec compte à rebours
- **Graphe principal** : évolution masse salariale sur 12 mois avec décomposition brut / charges patronales / net
- **Activité récente** : journal des dernières actions (calcul, validation, modification rubrique, anomalie résolue)

**Micro-interactions** :
- Hover sur le stepper révèle la timeline détaillée (qui a fait quoi, quand)
- Click sur une carte KPI ouvre un drawer avec décomposition par agence/département

---

#### A2. `/payroll-manager/runs` — Liste des cycles de paie

**Objectif** : historique de tous les runs avec filtres puissants.

**Contenu** :
- **Header** : titre "Cycles de paie", bouton "Nouveau cycle" primaire, bouton secondaire "Importer variables"
- **Barre de filtres** : période (date range), statut (multi-select), agence, type (régulier / complémentaire / 13ème mois)
- **Toggle vue** : liste / calendrier / cartes
- **Vue liste (tableau dense)** :
  - Colonnes : Période, Agence, Type, Statut (badge), Effectif, Brut total, Net total, Cotisations, Date validation, Validé par, Actions
  - Ligne cliquable pour ouvrir le détail
  - Hover révèle des actions rapides : "Voir", "Dupliquer", "Exporter PDF récapitulatif"
- **Vue calendrier** : grille année, chaque mois est une cellule colorée selon le statut du run

---

#### A3. `/payroll-manager/runs/new` — Lancement d'un nouveau cycle

**Objectif** : assistant en étapes pour lancer un cycle de paie.

**Contenu** : un grand **wizard à 5 étapes** avec stepper en haut :

1. **Configuration**
   - Période (sélecteur mois-année)
   - Agence (multi-select, "Toutes" par défaut)
   - Type de run (Régulier / Complémentaire / 13ème mois / Prime exceptionnelle)
   - Date de paiement prévue
   - Mode de calcul (Calcul réel / Simulation seulement)

2. **Variables de paie**
   - Tableau des employés avec colonnes éditables : Heures sup, Primes ponctuelles, Absences (j), Acomptes versés
   - Bouton "Importer depuis Excel" + drag & drop CSV
   - Bouton "Importer depuis Timesheets" (pré-rempli automatique)
   - Validation en temps réel avec icônes d'erreur en bout de ligne

3. **Aperçu / Simulation**
   - Récapitulatif chiffré : nombre d'employés, brut prévisionnel, net prévisionnel, total cotisations
   - Bouton "Calculer en aperçu" qui lance une simulation
   - Tableau des 10 plus gros écarts vs M-1 avec drill-down

4. **Confirmation**
   - Récapitulatif final
   - Checklist visuelle : "J'ai vérifié les variables", "J'ai contrôlé les anomalies", "Je confirme le lancement"
   - Bouton "Lancer le calcul" en danger (action irréversible)

5. **Calcul en cours**
   - Animation de progression employé par employé (compteur "Calcul en cours : 156 / 248 employés")
   - Log streaming des étapes : "Chargement contrats", "Application rubriques", "Vérification cohérence"
   - À la fin, redirection vers la page détail du run

---

#### A4. `/payroll-manager/runs/[id]` — Détail d'un cycle de paie

**Objectif** : vue complète d'un run avec tous les outils pour vérifier et finaliser.

**Layout 3 colonnes** :

**Colonne gauche (sticky)** : carte d'identité du run
- Période, statut (avec stepper vertical)
- Totaux principaux (Brut, Net, Cotisations, Charges patronales)
- Effectif
- Validé par / Approuvé par / Payé par
- Boutons d'action selon le statut :
  - DRAFT : "Verrouiller variables", "Lancer calcul"
  - CALCULATED : "Mettre en revue", "Recalculer"
  - REVIEW : "Valider"
  - VALIDATED : "Approuver"
  - APPROVED : "Générer ordres de paiement"
  - PAID : "Clôturer le cycle"

**Colonne centrale** : 4 onglets
1. **Vue d'ensemble** : graphes, KPI, comparaison M-1, top 10 salaires, répartition par catégorie
2. **Bulletins** : tableau filtrable de toutes les `PayrollEntry` (Matricule, Nom, Brut, Net, Statut paiement)
3. **Anomalies** : liste des contrôles, avec résolution inline
4. **Audit** : timeline complète qui a fait quoi

**Colonne droite (drawer)** : ouvre une fiche bulletin quand on clique sur une entrée

---

#### A5. `/payroll-manager/runs/[id]/entries/[entryId]` — Détail d'un bulletin

**Objectif** : voir et corriger un bulletin individuel.

**Contenu** :
- **Header sticky** : Nom, matricule, photo, catégorie, agence, période, statut
- **Section identité** : 2 colonnes (employeur / employé)
- **Section "Bulletin de paie"** :
  - Liste de toutes les `PayslipLine` groupées par catégorie : Gains, Retenues salariales, Charges patronales (info)
  - Chaque ligne : libellé, base, taux, montant
  - Ligne récapitulative : Brut, Net imposable, IRPP, Net à payer
- **Section "Cumuls annuels"** : brut cumulé, net cumulé, IRPP cumulé, CNPS cumulé
- **Section "Paiement"** : mode, compte, statut, référence
- **Section "Congés"** : solde au début/fin de mois
- **Boutons d'action** : Télécharger PDF, Envoyer par email, Corriger (ouvre formulaire d'ajustement), Recalculer ce bulletin
- **Aperçu PDF du bulletin** côté droit, scrollable, fidèle au PDF final

---

#### A6. `/payroll-manager/pay-elements` — Catalogue des rubriques de paie

**Objectif** : gérer toutes les rubriques configurables (CNPS_PV_EE, IRPP, PRIME_ANCIENNETE, etc.).

**Contenu** :
- **Header** : "Rubriques de paie", bouton "Nouvelle rubrique"
- **Filtres** : Catégorie (Gain/Retenue/Charge patronale/Info), Méthode de calcul, Pays, Actif/Inactif
- **Tableau** :
  - Code (monospace), Libellé, Catégorie (badge coloré), Méthode (RATE/BRACKET/FLAT/LOOKUP), Taux ou ref barème, Plafond, Base de calcul, Imposable, Cotisable, Date d'effet, Statut
- Click sur une ligne → drawer latéral d'édition

---

#### A7. `/payroll-manager/pay-elements/[code]` — Édition d'une rubrique

**Drawer ou page** : formulaire d'édition d'une rubrique.

**Contenu** :
- Onglet **Configuration** : champs Code, Libellé FR/EN, Catégorie, Pays, Date d'effet début/fin
- Onglet **Calcul** : Méthode (RATE/BRACKET/FLAT/LOOKUP), Base de référence (BRUT/SALAIRE_BASE/PLAFOND_CNPS/IRPP), Taux, Plafond, Plancher
- Onglet **Soumissions** : checkboxes "Soumis à l'IRPP", "Cotisable CNPS", "Inclus dans le net imposable"
- Onglet **Versions** : timeline de toutes les versions historiques de cette rubrique
- Onglet **Test** : simulateur — on entre un brut, on voit le calcul résultant
- Footer : "Désactiver à date", "Créer une nouvelle version", "Sauvegarder"

---

#### A8. `/payroll-manager/tax-brackets` — Barèmes fiscaux

**Objectif** : gérer les tables de tranches d'imposition (IRPP, etc.).

**Contenu** :
- Liste des tables : IRPP_CM_2026, IRPP_CM_2025, etc.
- Édition tableau dense des tranches : Min, Max, Taux
- Visualisation graphique de la courbe progressive
- Bouton "Créer un nouveau barème (clone + date d'effet)"

---

#### A9. `/payroll-manager/lookup-tables` — Tables de référence (RAV, TDL)

Édition des barèmes forfaitaires par tranche : Min brut, Max brut, Montant forfaitaire.

---

#### A10. `/payroll-manager/variables` — Saisie des variables mensuelles

**Objectif** : interface principale pour saisir les variables de paie du mois.

**Contenu** :
- **Header** : sélecteur de période, bouton "Importer Excel/CSV", bouton "Importer Timesheets"
- **Tableau éditable inline** :
  - Filtres : Agence, Département, Statut (saisi/non saisi)
  - Colonnes : Matricule, Nom, Catégorie, Heures sup (j et nuit), Primes, Absences (j sans solde), Acomptes
  - Édition en place avec sauvegarde auto sur blur
  - Indicateur de progression "187 / 248 employés saisis"
- **Bouton "Verrouiller la saisie"** quand prêt

---

#### A11. `/payroll-manager/declarations` — Déclarations sociales et fiscales

**Objectif** : générer et suivre les déclarations CNPS, DIPE, IRPP.

**Contenu** :
- **Onglets par type** : CNPS / DIPE / IRPP / Bordereau
- **Pour chaque type** :
  - Liste des déclarations passées (Période, Statut, Date génération, Date envoi, Fichier)
  - Bouton "Générer pour le mois en cours"
  - Workflow visuel : DRAFT → GENERATED → SUBMITTED → ACKNOWLEDGED
  - Aperçu du fichier généré
  - Download du fichier au format officiel

---

#### A12. `/payroll-manager/declarations/[id]` — Détail d'une déclaration

- Récapitulatif chiffré
- Aperçu du fichier
- Workflow d'envoi (manuel ou via API si disponible)
- Historique d'événements

---

#### A13. `/payroll-manager/payments` — Ordres de paiement

**Objectif** : gérer les lots de paiement après validation de la paie.

**Contenu** :
- **Liste des batches** : Période, Mode (Virement bancaire / Mobile Money), Nombre d'instructions, Montant total, Statut
- **Détail d'un batch** :
  - Liste des instructions (employé, montant, compte, statut)
  - Bouton "Télécharger fichier bancaire (XML CEMAC)"
  - Bouton "Pousser vers MTN MoMo / Orange Money"
  - Statuts individuels mis à jour via callbacks

---

#### A14. `/payroll-manager/retroactive` — Régularisations rétroactives

**Objectif** : gérer les rappels de salaire et corrections rétroactives.

**Contenu** :
- **Tableau** : Employé, Période d'origine, Période de paiement, Motif, Montant, Statut
- **Bouton "Nouvelle régularisation"** : sélection employé, période à recalculer, motif (changement salaire / correction erreur / accord collectif)
- Visualisation comparative "Avant / Après" de la période recalculée

---

#### A15. `/payroll-manager/final-settlements` — Soldes de tout compte

**Objectif** : calculer le STC d'un employé qui quitte.

**Contenu** :
- Liste des employés en départ (date sortie < 30 jours ou marqué pour STC)
- **Détail STC** : assistant qui calcule
  - Salaire prorata mois de sortie
  - Indemnité compensatrice de congés non pris
  - Indemnité de préavis
  - Indemnité de licenciement (selon ancienneté)
  - Gratifications prorata
  - Soldes de prêts à apurer
  - Net STC à verser
- Bouton "Générer document légal" (PDF signé)

---

#### A16. `/payroll-manager/simulator` — Simulateur de paie

**Objectif** : tester un scénario sans impact sur les données réelles.

**Contenu** :
- **Mode "Brut → Net"** : on entre un brut, on voit le détail des cotisations et impôts, le net résultant
- **Mode "Net → Brut"** : on entre un net cible, on remonte au brut nécessaire (calcul inverse)
- **Mode "What-if global"** : on simule une augmentation de X% sur Y catégorie, on voit l'impact sur la masse salariale
- **Mode "Embauche"** : on simule le coût employeur d'un nouveau recrutement

Tous les modes affichent un détail de calcul ligne par ligne, comme un bulletin.

---

#### A17. `/payroll-manager/garnishments` — Saisies sur salaire

**Objectif** : gérer les saisies sur salaire (pension alimentaire, saisie-arrêt).

**Contenu** :
- Liste : Employé, Bénéficiaire, Type (Pension alimentaire / Saisie Trésor / Créancier), Montant total, Restant dû, Mensualité, Priorité
- Création d'une nouvelle saisie avec calcul automatique de la quotité saisissable
- Détail avec historique des prélèvements

---

#### A18. `/payroll-manager/analytics` — Analytique paie

**Objectif** : analyser la masse salariale en profondeur.

**Contenu** :
- Graphes interactifs (Recharts) :
  - Masse salariale par département / agence / catégorie sur 12 mois
  - Coût employeur total vs net versé
  - Répartition des cotisations
  - Évolution de l'effectif vs masse salariale
- Filtres puissants : période, agence, département, catégorie
- Export CSV / PDF

---

### B) ADMIN RH (`hr-admin/payroll`)

Vue allégée, focus opérationnel. ~6 pages.

- `/hr-admin/payroll` — Liste des cycles (mêmes filtres que A2, mais sans config rubriques)
- `/hr-admin/payroll/new` — Lancement du cycle (wizard simplifié)
- `/hr-admin/payroll/[id]` — Détail cycle
- `/hr-admin/payroll/[id]/entries/[entryId]` — Détail bulletin
- `/hr-admin/payroll/variables` — Saisie variables (lecture seule sur la config, édition sur les variables)
- `/hr-admin/payroll/payslips` — Recherche d'un bulletin par employé/période

---

### C) COMPTABLE (`accountant/payroll`)

Focus validation et comptabilisation. ~7 pages.

- `/accountant/payroll/validation` — File d'attente des cycles en attente de validation
- `/accountant/payroll/[id]/validate` — Écran de validation avec rapprochement comptable
- `/accountant/payroll/journal` — Journal des écritures comptables générées
- `/accountant/payroll/journal/[entryId]` — Détail écriture OHADA
- `/accountant/payroll/payments` — Suivi des ordres de paiement (mêmes données que A13 mais focus paiement)
- `/accountant/payroll/payments/reconciliation` — Rapprochement bancaire
- `/accountant/payroll/reports` — États comptables (état des salaires, état des cotisations)

---

### D) DRH (`drh/payroll`)

Vue stratégique, KPI, gouvernance. ~5 pages.

- `/drh/payroll` — Dashboard analytique haut niveau (masse salariale annuelle, évolution effectif, ratios)
- `/drh/payroll/budget` — Suivi du budget paie vs réel
- `/drh/payroll/analytics` — Drilldown par BU/département/catégorie
- `/drh/payroll/benchmarks` — Comparaison interne (médiane, p75, p25 par catégorie)
- `/drh/payroll/approvals` — Approbations finales avant paiement

---

### E) EMPLOYÉ (`employee/payslips`)

Self-service mobile-first. ~5 pages.

#### E1. `/employee/payslips` — Mes bulletins

- Vue **liste / timeline** alternative
- Chaque bulletin : période, brut, net, statut paiement, bouton télécharger
- Filtre par année
- Card "Récap annuel" en haut (brut cumulé, net cumulé)

#### E2. `/employee/payslips/[entryId]` — Détail d'un bulletin

- Aperçu PDF stylisé du bulletin
- Bouton Download
- Section "Comprendre mon bulletin" avec tooltips explicatifs sur chaque ligne
- Lien vers le simulateur personnel

#### E3. `/employee/payslips/annual` — Mon cumul annuel

- Vue annuelle de tous les bulletins
- Graphe d'évolution du salaire
- Tableau récapitulatif pour aide à la déclaration fiscale
- Bouton "Demander mon attestation fiscale annuelle"

#### E4. `/employee/payslips/simulator` — Mon simulateur de salaire

- Simulateur personnel : "Si j'ai X heures sup ce mois, mon net sera Y"
- Mode "augmentation" : "Si mon brut passait à X, mon net serait Y"
- Mode "départ" : estimation du STC

#### E5. `/employee/payslips/documents` — Mes documents légaux

- Attestations de travail
- Attestations fiscales annuelles
- STC (si départ)
- Bouton "Demander un nouveau document"

---

## ÉCRANS TRANSVERSES À DESIGNER (multi-rôles)

### F1. Composant **PayrollRunStatusStepper**
Stepper horizontal et vertical des 9 statuts du cycle de paie avec timeline interactive.

### F2. Composant **PayslipPreview**
Aperçu fidèle du bulletin de paie PDF, utilisé dans plusieurs contextes (détail bulletin, simulateur, STC).

### F3. Composant **PayElementRow**
Ligne d'une rubrique sur un bulletin : libellé, base, taux, montant, avec hover qui montre le détail de la formule.

### F4. Composant **AmountInput**
Input numérique pour montants avec :
- Auto-formatage (espace milliers, devise XAF/EUR)
- Conversion brut ↔ net en temps réel optionnelle
- Indicateur de variation vs valeur de référence

### F5. Composant **AnomalyCard**
Carte d'anomalie détectée avec sévérité (info/warning/error), description, employés concernés, bouton de résolution.

### F6. Composant **PeriodPicker**
Sélecteur de période mensuelle avec navigation rapide année.

### F7. Composant **OrganizationAgencyContextSwitcher**
Sticky header avec sélecteur organisation + agence, breadcrumb du contexte courant.

### F8. Composant **CalculationTraceViewer**
Visualisation du "trace de calcul" pour un bulletin : étapes successives (brut → cotisations → impôts → net) avec déroulé des formules.

---

## ÉTATS À DESIGNER POUR CHAQUE PAGE

Pour les pages critiques (A1, A4, A5, A6, A10, E1, E2), designe explicitement :
1. **État initial / loading** (skeletons précis)
2. **État avec données** (happy path)
3. **État empty** (avec illustration et CTA)
4. **État erreur** (avec retry)
5. **État permission insuffisante** (avec explication et CTA "Contacter votre admin")
6. **État action en cours** (boutons disabled, spinners, banner de progression)
7. **État succès post-action** (toast + mise à jour optimiste)

---

## SCÉNARIOS D'USAGE BOUT-EN-BOUT À METTRE EN SCÈNE

Conçois des **storyboards visuels** pour ces parcours :

### Scénario 1 : "Le 28 du mois — Run mensuel"
Le Responsable Paie ouvre la plateforme. Le dashboard affiche que le run d'octobre n'est pas encore lancé. Il clique sur "Saisir les variables", contrôle les heures sup importées des timesheets, lance le calcul, vérifie les anomalies (un employé sans contrat actif → ouvre l'écran de résolution), valide une nouvelle fois, soumet à la comptable. La comptable reçoit une notification, valide, génère les ordres de paiement. Le run passe à PAID le 30.

### Scénario 2 : "Régularisation rétroactive"
Un employé a obtenu une promotion effective au 1er septembre, mais elle n'est saisie qu'en novembre. Le Responsable Paie crée une régularisation rétroactive : sélectionne l'employé, indique la période à recalculer, le nouveau salaire. Le système calcule le delta, affiche un comparatif "Avant/Après" pour chaque mois, et le delta sera intégré au prochain run de novembre.

### Scénario 3 : "Solde de tout compte"
Un employé démissionne avec un préavis de 1 mois. L'Admin RH marque l'employé "En départ". Le Responsable Paie va sur la liste STC, ouvre la fiche de cet employé. L'assistant calcule : salaire prorata du mois, congés non pris, préavis, indemnité de licenciement, soldes de prêts. Le PDF du STC est généré et envoyé pour signature.

### Scénario 4 : "Employé qui consulte son bulletin"
Un employé reçoit une notification "Votre bulletin d'octobre est disponible". Il ouvre l'app sur son mobile, clique sur le bulletin, voit l'aperçu, télécharge le PDF, va sur "Comprendre mon bulletin" et lit les explications de chaque ligne. Puis il va sur "Simulateur" et joue avec son brut pour voir l'impact d'une augmentation.

### Scénario 5 : "Configurer une nouvelle rubrique"
Le Responsable Paie veut ajouter une "Prime de risque" pour une catégorie spécifique. Il va sur le catalogue des rubriques, clique "Nouvelle rubrique", remplit le formulaire (code PRIME_RISQUE, méthode RATE, taux 5% du salaire de base, applicable à la catégorie X), choisit la date d'effet au 1er du mois suivant, et active. Au prochain run, la rubrique est automatiquement appliquée.

### Scénario 6 : "Génération de la DIPE annuelle"
En janvier, le Responsable Paie doit générer la DIPE annuelle de l'année passée. Il va sur `/declarations`, sélectionne "DIPE annuelle 2026", clique "Générer". Le système agrège tous les bulletins, génère le fichier au format CNPS, propose une vérification (totaux par employé), et permet le téléchargement et la soumission.

---

## CONTRAINTES BUSINESS À RESPECTER

1. **Multi-tenant strict** : chaque écran porte explicitement le contexte tenant + organisation + agence en cours
2. **Audit-friendly** : chaque action critique (validation, modification rubrique) doit demander confirmation et tracer "qui, quand, pourquoi"
3. **Permissions UI** : les boutons d'action critiques ne sont visibles que pour les rôles autorisés
4. **Immutabilité** : un bulletin validé ne se modifie plus — seul un ajustement crée une nouvelle ligne
5. **Conformité légale** : le bulletin doit respecter le Code du travail camerounais (Art. 68) — en-têtes employeur/employé complets, mentions obligatoires, montant en lettres
6. **Devise XAF par défaut** mais le système doit pouvoir afficher en EUR ou USD pour les expatriés
7. **Format de date** français (JJ/MM/AAAA) sauf en mode anglais
8. **Format de montant** avec espace comme séparateur des milliers : `1 250 000 XAF`

---

## DÉTAILS UI/UX À NE PAS OUBLIER

- **Sidebar** : la section "Paie" doit être designée pour chacun des 5 rôles avec leurs items spécifiques
- **Breadcrumbs** systématiques en haut de chaque page de détail
- **Recherche globale** (cmd+K) qui indexe les employés, les bulletins, les runs, les rubriques
- **Notifications** : centre de notif en haut à droite avec : run terminé, anomalie détectée, échéance déclarative, validation en attente
- **Dark mode** soigné, switch dans la sidebar
- **Densité** : option "compact" / "normal" / "confortable" pour les tableaux
- **Export** : tous les tableaux ont un bouton d'export CSV / Excel / PDF
- **Filtres avancés** : pouvoir sauvegarder des filtres en presets nommés
- **Bulk actions** : sélection multiple sur tous les tableaux principaux (entries, employés, rubriques)
- **Comparaison M-1** : presque toutes les valeurs chiffrées affichent un delta % vs M-1
- **Tooltips explicatifs** sur tous les termes techniques (CNPS, IRPP, CAC, etc.)
- **Glossaire intégré** accessible depuis le menu utilisateur

---

## DESIGN SYSTEM À PRODUIRE

Avant les écrans, produis le **design system de base** :
- Tokens couleur (modes clair et sombre)
- Échelle typographique (display, h1-h6, body, small, label, caption, code)
- Échelle d'espacement (4, 8, 12, 16, 24, 32, 48, 64, 96)
- Échelle de border-radius (sm 6px, md 10px, lg 14px, xl 20px, pill)
- Élévations (subtle, small, medium, large)
- Composants atomiques (button, input, badge, avatar, tooltip, popover)
- Composants moléculaires (card, drawer, modal, table, tabs, stepper)

---

## LIVRABLE ATTENDU

1. **Design system complet** : pages de présentation des tokens et composants
2. **Tous les écrans listés ci-dessus** (au moins ~40 écrans uniques)
3. **3-5 variations clé** par écran critique (loading/empty/error/desktop/mobile)
4. **Storyboards des 6 scénarios** bout-en-bout
5. **Annexe** : guide d'utilisation du design system, principes d'interaction, motion principles

---

## RÉFÉRENCES VISUELLES À ÉMULER

- **Linear** (typographie, densité, micro-interactions)
- **Vercel Dashboard** (sobriété, cards, dark mode)
- **Stripe Dashboard** (tableaux, formulaires, navigation)
- **Ramp / Mercury** (KPI cards, finance UX premium)
- **Deel** (interface paie internationale moderne)
- **Notion** (drawers, hovers, command palette)
- **Pleo** (mobile self-service employé)

---

## TON ET PERSONNALITÉ

Le produit doit transmettre : **rigueur, confiance, modernité, expertise**. Le Responsable Paie doit se sentir aux commandes d'un outil professionnel sérieux. L'Employé doit se sentir respecté et bien informé. La comptabilité doit avoir une vue exhaustive et auditable. Aucune fioriture, aucun emoji, aucun ton "fun startup" — c'est de la paie, c'est sérieux, mais c'est moderne et clair.

Fais en sorte que chaque écran donne envie de l'utiliser tous les mois.

---

**Démarre par le design system, puis le dashboard A1 du Responsable Paie, puis enchaîne dans l'ordre listé.**
