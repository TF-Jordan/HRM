# Recherche approfondie : Moteurs de calcul de paie

Document préparatoire à l'extraction du module `RT-comops-payroll-core` depuis `RT-comops-hrm-core`.

---

## 1. COMPOSANTES D'UN MOTEUR DE PAIE COMPLET

### 1.1 Calcul du salaire brut (Gross Pay Computation)

Un moteur de paie mature assemble le brut à partir de **multiples sources de rémunération** :

| Composante | Description | État actuel HRM |
|---|---|---|
| **Salaire de base** | Montant contractuel mensuel | ✅ `contract.salaireBase()` |
| **Avantages en nature** | Logement, véhicule, nourriture, domesticité | ✅ `contract.avantagesNature()` (montant unique) |
| **Heures supplémentaires** | Majoration 20/40/50/100% selon palier et jour | ❌ Absent |
| **Primes fixes** | Ancienneté, technicité, responsabilité, risque, salissure | ❌ Absent |
| **Primes variables** | Performance, objectifs, commission, intéressement | ❌ Absent |
| **Indemnités** | Transport, représentation, panier, déplacement | ❌ Absent |
| **Gratifications** | 13ème mois, prime de fin d'année | ❌ Absent |
| **Rappels de salaire** | Rétroactifs suite à promotion, erreur, accord collectif | ❌ Absent |
| **Absences impactant la paie** | Congé sans solde, absence injustifiée, maladie (indemnisation partielle) | ❌ Absent |
| **Avantages sociaux valorisés** | Stock options, participation, épargne salariale | ❌ Absent |

**Constat** : le brut actuel est `salaireBase + avantagesNature` — un calcul simpliste. Les plateformes comme Sage Paie, ADP, Workday et SAP SuccessFactors gèrent des dizaines d'éléments de paie configurables (earnings types) avec des règles d'éligibilité, de prorata temporis, et de cumuls.

### 1.2 Cotisations sociales — Cameroun (état actuel)

| Cotisation | Part salariale | Part patronale | Plafond | Implémenté |
|---|---|---|---|---|
| **CNPS - Pension Vieillesse (PV)** | 4,2% | 4,2% | 750 000 FCFA/mois | ✅ |
| **CNPS - Allocations Familiales (AF)** | — | 7% | Pas de plafond | ✅ |
| **CNPS - Accidents du Travail (AT)** | — | 1,75% (variable par secteur : 1% à 5%) | Pas de plafond | ✅ (taux fixe 1,75%) |
| **CFC (Crédit Foncier)** | 1% | 1,5% | — | ✅ salarié / ❌ employeur |
| **RAV (Redevance Audio-Visuelle)** | Forfait par tranche | — | — | ✅ |
| **TDL (Taxe de Développement Local)** | Forfait annuel/12 | — | — | ✅ |
| **FNE (Fonds National de l'Emploi)** | — | 1% | — | ❌ |
| **Taxe patronale** | — | 2,5% du brut | — | ❌ |
| **CNPS - Assurance Maladie** | À venir (loi votée) | À venir | — | ❌ |

### 1.3 Cotisations sociales — Ce que font les plateformes multi-pays

**France** (~60 lignes de cotisations) :
- CSG déductible (6,80%) / non-déductible (2,40%) + CRDS (0,50%)
- URSSAF : maladie, vieillesse plafonnée/déplafonnée, allocations familiales
- AGIRC-ARRCO : retraite complémentaire T1 (3,15% salarié / 4,72% employeur) et T2 (8,64% / 12,95%)
- Prévoyance, mutuelle obligatoire
- CEG, CET
- Taxe d'apprentissage, formation professionnelle
- AGS (0,25%), FNAL, versement mobilité
- Forfait social sur certains éléments

Plafond mensuel SS 2026 (PMSS) : 4 005 EUR / Plafond annuel (PASS) : 48 060 EUR.

**USA** :
- Federal: FICA = Social Security 6,2% (plafonné $176 100 en 2026) + Medicare 1,45% + 0,9% Additional Medicare au-delà de $200k
- FUTA (fédéral chômage), SUTA (état par état)
- State income tax : 50 systèmes différents (7 états sans impôt sur le revenu)
- Local taxes (NYC, Philadelphia, etc.)
- 401k, HSA, FSA pré-impôt

**Afrique CEMAC/OHADA** :
| Pays | Sécurité sociale employé | Sécurité sociale employeur | Particularité |
|---|---|---|---|
| **Cameroun** | CNPS PV 4,2% + CFC 1% | ~16,95% (PV+AF+AT+CFC+FNE+taxe) | AT variable par secteur |
| **Gabon** | CNSS 2,5% + CNAMGS 2% | ~20,1% | Assurance maladie obligatoire (CNAMGS) |
| **Congo** | Variable | Taxe unique sur salaires 7,5% | Simplifié en 2024 |
| **Tchad** | CNPS 3,5% | CNPS 16,5% | |
| **RCA** | Variable | Variable | |
| **Guinée Éq.** | Variable | Variable | |
| **Sénégal (UEMOA)** | IPRES 5,6% | IPRES 8,4% + CSS 1-5% + AF 7% | Zone XOF |

### 1.4 Impôt sur le revenu

| Aspect | Implémentation actuelle | Moteur complet |
|---|---|---|
| **Barème progressif** | 4 tranches (10/15/25/35%) ✅ | Configurable N tranches par pays |
| **Abattement forfaitaire** | 30% plafonné 400k ✅ | Configurable par type |
| **Charges déductibles** | ❌ | Enfants à charge, conjoint, assurances |
| **Quotient familial** | ❌ | Parts fiscales (célibataire=1, marié=2, +0,5/enfant) |
| **Situation familiale** | ❌ | Célibataire/Marié/Divorcé/Veuf |
| **Prélèvement à la source** | N/A Cameroun | France: PAS mensuel, taux personnalisé DGFiP |
| **Régularisation annuelle** | ❌ | Cumul progressif, ajustement décembre |
| **Exonérations spéciales** | Seuil 62k ✅ | Zones franches, jeunes entreprises, handicap |
| **Retenue non-résidents** | ❌ | Expatriés, conventions fiscales |

**Point critique Cameroun** : l'IRPP applique un **quotient familial** (nombre de parts) qui réduit l'impôt. Le calcul actuel ne prend PAS en compte la situation familiale — les employés mariés avec enfants sont surtaxés.

Barème IRPP Cameroun (annuel) :
| Tranche (XAF annuel) | Taux IRPP |
|---|---|
| 0 — 2 000 000 | 10% |
| 2 000 001 — 3 000 000 | 15% |
| 3 000 001 — 5 000 000 | 25% |
| > 5 000 000 | 35% |

**Méthode de calcul complète** :
1. Partir du salaire brut
2. Soustraire la cotisation CNPS salarié (4,2%)
3. Appliquer l'abattement professionnel forfaitaire de 30%
4. Appliquer la déduction forfaitaire de 500 000 XAF (annuel)
5. Diviser par le quotient familial (nombre de parts)
6. Appliquer le barème progressif sur le résultat
7. Multiplier par le nombre de parts
8. Ajouter le CAC (10% de l'IRPP)
9. Seuil d'exonération : 62 000 FCFA/mois brut (en dessous, pas d'IRPP)

### 1.5 Éléments variables de paie (Variable Pay Elements)

Les plateformes matures gèrent un concept de **"variable de paie"** — tout élément qui varie d'un mois à l'autre :

| Variable | Description | Impact |
|---|---|---|
| **Heures travaillées** | Entrée temps réel ou import timesheet | Prorata si temps partiel |
| **Heures supplémentaires** | Par palier de majoration (Code du travail CM: +25% h41-48, +40% h49-60, +50% nuit, +75% dimanche/fériés) | Ajouté au brut |
| **Absences** | Maladie, congé sans solde, absence injustifiée | Retenue sur brut |
| **Congé payé pris** | Maintien de salaire | Neutre (comptabilisé) |
| **Congé maladie** | Indemnité CNPS + complément employeur | Calcul spécifique |
| **Primes exceptionnelles** | Naissance, mariage, décès, ancienneté | Ajouté au brut |
| **Astreintes** | Indemnité forfaitaire | Ajouté au brut |
| **Avantages mensuels** | Tickets restaurant, transport | Partiellement exonéré |
| **Saisies sur salaire** | Pension alimentaire, saisie-arrêt | Prélevé sur net |
| **Acomptes** | Versement anticipé en cours de mois | Prélevé sur net |
| **Prêts employeur** | Échéancier de remboursement | ✅ Implémenté |

---

## 2. CYCLE DE VIE D'UN RUN DE PAIE

### 2.1 Cycle actuel vs cycle professionnel

**Cycle actuel** : `CALCULATED → VALIDATED → PAID`

**Cycle complet des plateformes professionnelles** :

```
DRAFT                    → Préparation, saisie des variables
  ↓
VARIABLES_LOCKED         → Les managers ne peuvent plus modifier les variables
  ↓
CALCULATED               → Calcul brut→net pour tous les employés
  ↓
REVIEW                   → Le responsable paie vérifie, compare M-1
  ↓  (anomalies ?)
RECALCULATED             → Correction et re-calcul partiel ou total
  ↓
VALIDATED                → Approbation hiérarchique (DRH/DAF)
  ↓
APPROVED                 → Double validation (séparation des tâches)
  ↓
PAYMENT_INITIATED        → Fichiers de virement générés (SEPA/CEMAC)
  ↓
PAID                     → Confirmation bancaire reçue
  ↓
CLOSED                   → Période clôturée, comptabilisée, archivée
```

### 2.2 Rétroactifs et régularisations

**Déclencheurs** :
- Rappel de salaire : promotion effective le 1er janvier, traitée en mars → recalcul des 2 mois précédents
- Régularisation de cotisations : changement de plafond CNPS en cours d'année
- Régularisation IRPP : cumul progressif avec trop-perçu ou sous-perçu
- Correction d'erreur : entrée erronée le mois précédent → ligne de régularisation
- STC (Solde de Tout Compte) : calcul spécial au départ (indemnité de licenciement, compensatrice de congé, de préavis, gratification prorata)

**Comportement moteur (modèle Oracle/Workday)** :
1. Détection de l'événement rétroactif
2. Recalcul des périodes affectées avec les paramètres mis à jour
3. Comparaison résultats nouveaux vs résultats originaux
4. Création d'entrées d'ajustement pour les différences
5. Les différences alimentent le prochain run régulier
6. Recalcul fiscal sur le delta

### 2.3 Prorata temporis

Un employé embauché le 15 du mois ou partant le 10 doit recevoir un salaire au prorata :
- **Méthode jours ouvrés** : salaire × (jours travaillés / jours ouvrés du mois)
- **Méthode calendaire** : salaire × (jours calendaires / jours du mois)
- **Méthode 30ème** : salaire / 30 × jours de présence

### 2.4 Paie complémentaire

Un run additionnel dans le même mois pour traiter :
- Prime exceptionnelle
- Gratification
- 13ème mois
- Rappel isolé

---

## 3. DÉCLARATIONS SOCIALES ET FISCALES

### 3.1 Cameroun

| Déclaration | Fréquence | Destinataire | Format | État actuel |
|---|---|---|---|---|
| **État 301 (CNPS)** | Mensuelle | CNPS | CSV/papier | ❌ |
| **Bordereau de versement CNPS** | Mensuelle | CNPS | PDF | ❌ |
| **Déclaration IRPP employeur** | Mensuelle (15 du M+1) | DGI | Formulaire | ❌ |
| **DIPE (Déclaration Individuelle)** | Annuelle | CNPS | Format CNPS | Modèle existe, génération ❌ |
| **DSF (Déclaration Statistique et Fiscale)** | Annuelle | INS/DGI | Formulaire | ❌ |
| **Bilan social** | Annuelle | Inspection du travail | Libre | ❌ |
| **Attestation de travail** | À la demande | Employé | PDF | ❌ |
| **Certificat de travail** | Au départ | Employé | PDF | ❌ |
| **Solde de tout compte** | Au départ | Employé | PDF | ❌ |

**Délais et pénalités** : déclarations mensuelles au 15 du mois suivant (DGI et CNPS). Pénalité de retard : 10% + 1,5% d'intérêt mensuel. DIPE annuelle au 20 janvier.

### 3.2 France (référence pour l'architecture)

- **DSN (Déclaration Sociale Nominative)** : déclaration mensuelle obligatoire qui remplace toutes les anciennes déclarations (DADS, DUCS, etc.)
- Transmise via net-entreprises.fr
- Contient : données individuelles employé, rémunérations brutes, cotisations sociales, impôt prélevé à la source
- Délais : 5 du M+1 (> 50 salariés), 15 du M+1 (autres)
- Pénalités : jusqu'à 60,07 EUR par salarié par mois de retard
- Le moteur doit **pouvoir générer des déclarations au format attendu par chaque pays**

### 3.3 Traitement de fin d'année

- Vérifier que tous les runs de paie sont complets et comptabilisés
- Rapprocher salaires bruts totaux, impôts retenus, nets payés
- Traiter les ajustements et corrections finaux
- Générer et distribuer les attestations fiscales aux employés
- Déposer les déclarations électroniques auprès des administrations
- Reporter ou payer les soldes de congés non pris
- Mettre à jour les barèmes fiscaux et taux de cotisation pour l'année suivante
- Archiver les dossiers de paie selon les exigences de conservation

---

## 4. BULLETIN DE PAIE

### 4.1 Contenu légal (Code du travail camerounais, Art. 68)

| Section | Contenu | Implémenté |
|---|---|---|
| **En-tête employeur** | Raison sociale, RCCM, n° contribuable, n° CNPS employeur | ❌ |
| **En-tête employé** | Nom, matricule, catégorie, échelon, n° CNPS, situation familiale | ❌ |
| **Période** | Mois/année | ✅ |
| **Gains** | Salaire de base, primes détaillées, heures sup, avantages | ✅ Partiel |
| **Cotisations salariales** | CNPS, CFC — avec base, taux, montant | ✅ |
| **Impôts** | IRPP, CAC, RAV, TDL — avec calcul | ✅ |
| **Retenues** | Avances, prêts, saisies | ✅ Partiel |
| **Cotisations patronales** | CNPS AF, AT, PV — pour information | ❌ |
| **Cumuls annuels** | Brut cumulé, net cumulé, CNPS cumulé, IRPP cumulé depuis janvier | ❌ |
| **Net à payer** | En chiffres et en lettres | ✅ / ❌ (lettres) |
| **Mode de paiement** | Virement, chèque, espèces | ✅ |
| **Congés** | Solde de congés, congés pris dans le mois | ❌ |
| **Ancienneté** | Date d'embauche, ancienneté | ❌ |

### 4.2 Bulletin simplifié France (référence pour le format)

Sections obligatoires depuis 2026 :
1. Identification employeur et employé
2. Salaire brut avec ventilation
3. Cotisations sociales groupées par catégorie : Santé, Accidents du travail, Retraite, Famille, Chômage
4. **Net social** (obligatoire depuis juillet 2023)
5. CSG/CRDS (déductible et non-déductible)
6. Net avant impôt
7. Prélèvement à la source (montant et taux)
8. Net à payer
9. Section coût employeur
10. Cumuls annuels
11. Solde de congés

---

## 5. PAIEMENTS ET INTÉGRATIONS

### 5.1 Génération de fichiers de paiement

| Format | Usage | Implémenté |
|---|---|---|
| **Fichier CEMAC** | Virements bancaires zone CEMAC | ❌ |
| **SEPA XML (pain.001)** | Virements bancaires Europe | ❌ |
| **ACH/NACHA** | Virements USA | ❌ |
| **BACS** | Virements UK | ❌ |
| **Mobile Money API** | MTN MoMo, Orange Money — API de disbursement (PawaPay, Flutterwave) | ❌ |
| **Fichier CSV bancaire** | Format banque spécifique (Ecobank, BICEC, Afriland) | ❌ |

Le système actuel publie un `PAYMENT_ORDER_CREATED` event — bonne base mais le fichier de virement réel n'est pas généré.

**Paiement mobile en Afrique** : 70% du marché mondial du mobile money ($1 trillion) est en Afrique. MTN Mobile Money, Orange Money, M-Pesa, Airtel Money. Fournisseurs de disbursement en masse : PawaPay (34+ pays), Flutterwave, Onafriq. Règlement le jour même ou J+1.

### 5.2 Comptabilisation OHADA (SYSCOHADA)

Les écritures comptables de paie suivent le plan comptable OHADA :

```
Débit  6611  Salaires bruts
Débit  6641  CNPS part patronale (PV)
Débit  6642  CNPS part patronale (AF)
Débit  6643  CNPS part patronale (AT)
Débit  6644  FNE
Débit  6645  CFC part patronale
Crédit 4211  Personnel — rémunérations dues (= net à payer)
Crédit 4311  CNPS — cotisations salariales
Crédit 4312  CNPS — cotisations patronales
Crédit 4421  État — IRPP retenu
Crédit 4422  État — CAC
Crédit 4423  État — CFC salarié
Crédit 4424  État — RAV
Crédit 4425  État — TDL
Crédit 4213  Personnel — avances et acomptes
```

### 5.3 Intégrations clés

| Système | Direction | Données |
|---|---|---|
| **RH (hrm-core)** | Payroll ← HRM | Employés, contrats, prêts, absences, timesheets |
| **Comptabilité (accounting-core)** | Payroll → Accounting | Écritures comptables, provisions |
| **Banque** | Payroll → Banque | Fichiers de virement |
| **Mobile Money** | Payroll → API | Ordres de disbursement |
| **Administration fiscale (DGI)** | Payroll → DGI | Déclarations IRPP |
| **Sécurité sociale (CNPS)** | Payroll → CNPS | États 301, DIPE |
| **Self-service employé** | Payroll → Frontend | Bulletins, cumuls |

---

## 6. ARCHITECTURE DES MOTEURS ENTERPRISE

### 6.1 Modèle SAP : Schema/PCR (Configuration-Driven)

SAP SuccessFactors utilise :
- **Payroll Schemas** : pipelines de traitement séquentiels composés de Functions, Operations et PCRs
- **Personnel Calculation Rules (PCRs)** : blocs de règles configurables qui traitent des "wage types" via des opérations (multiply, compare, accumulate, table lookup)
- **Localisation pays** : schemas pré-livrés par pays (DE00, FR00, US00, etc.) avec des centaines de PCRs pré-construits
- **Wage types** : codes internes représentant chaque composante de paie. Types "modèle" livrés par pays ; types spécifiques client dans des plages configurables

### 6.2 Modèle Workday : Continuous Calculation

- Recalcul en temps réel à mesure que les données changent
- Pas de "batch" — la paie est toujours à jour
- Règles d'earning et deduction configurables
- Multi-tenant cloud avec scalabilité élastique
- Global Payroll Platform (GPP) pour flux bidirectionnel avec fournisseurs pays tiers

### 6.3 Modèle PayrollEngine (Open-Source)

Architecture de référence open-source (MIT, v1.0 avril 2026) :
- **Regulation-driven** : règles métier définies en couches de régulation configurables, pas en dur
- **Couches empilables** : Régulation pays → Régulation industrie → Règles entreprise → Personnalisations client. Chaque couche peut surcharger ou étendre la précédente
- **Multi-tenant** avec partage de régulations entre entreprises
- **API-first** : backend ASP.NET Core REST API
- **Scripts de paie** compilés via Roslyn (C#)
- **Wage types et collectors** comme modèle de données central
- Supporte prévisions et aperçu instantané de run sans effets de bord
- Framework de test intégré pour validation de cas et tests de régression

### 6.4 Principes architecturaux clés

1. **Configuration plutôt que code** : taux, tranches, plafonds stockés comme données de configuration versionnées, pas compilés. Le versioning des règles trace quelles règles s'appliquaient à quel run
2. **Plugins/couches pays** : les règles fiscales et sociales de chaque pays encapsulées comme modules de régulation indépendants et empilables
3. **Modèle de données temporel** : toutes les données employé (salaire, poste, statut fiscal) stockées avec dates d'effet et périodes de validité. Les changements rétroactifs retraitent les périodes affectées
4. **Résultats de paie immuables** : une fois un run finalisé, les résultats sont immuables. Les corrections créent de nouvelles entrées d'ajustement, ne modifient jamais les enregistrements historiques
5. **Audit trail by design** : chaque étape de calcul, application de règle et changement de données loggé avec timestamp, utilisateur et valeurs avant/après

---

## 7. FONCTIONNALITÉS AVANCÉES DES PLATEFORMES MODERNES

| Fonctionnalité | Description | Plateformes |
|---|---|---|
| **Simulation de paie** | "What-if" : simuler augmentation, recrutement | Workday, SAP, Sage |
| **Paie partielle** | Entrée/sortie en cours de mois (prorata temporis) | Toutes |
| **Multi-contrats** | Un employé avec 2 contrats (temps partiel + CDD) | ADP, SAP |
| **Paie complémentaire** | Run additionnel pour prime exceptionnelle | ADP, Workday |
| **Comparaison M/M-1** | Écarts automatiques avec alertes si > seuil | Sage, Workday |
| **Verrouillage période** | Empêcher modifications après clôture | Toutes |
| **Audit trail** | Qui a modifié quoi et quand | Toutes |
| **Multi-devises** | Expatriés payés en devise différente | Deel, Remote, SAP |
| **Paie multi-établissements** | Consolidation multi-agences | ✅ `agencyId` existe |
| **Archivage légal** | Conservation bulletins 5 ans minimum | Sage, ADP |
| **Export comptable** | Fichier d'écritures comptable (FEC) | Sage, Cegid |
| **Gestion des acomptes** | Versement anticipé, régularisation | ADP, Sage |
| **Saisies sur salaire** | Quotité saisissable, barème légal, priorité des créanciers | Sage, ADP |
| **On-demand pay** | Accès anticipé au salaire acquis | Paylocity, DailyPay |
| **IA payroll** | Explication paie, détection anomalies, suggestions corrections | SAP (2026) |

### 7.1 Spécificités Afrique / CEMAC

Les plateformes spécialisées Afrique (Sage Paie Afrique, NOVAPAIE, Kamtar HR, IPT Africa) gèrent :

- **Convention collective** : chaque secteur (bâtiment, commerce, banque, industrie) a ses propres grilles salariales, classifications, primes
- **SMIG/SMAG** : vérification salaire minimum (41 875 FCFA au Cameroun)
- **Heures supplémentaires** : majoration Code du travail camerounais (+25% h41-48, +40% h49-60, +50% nuit 22h-6h, +75% dimanche/fériés). Semaine légale : 40h (non-agricole), 48h (agricole). Maximum heures sup : 20h/semaine
- **Prime d'ancienneté** : obligatoire au Cameroun (5% après 2 ans, +2% par année supplémentaire, plafonnée)
- **Indemnité de logement** : exonérée jusqu'à un plafond
- **Indemnité de transport** : exonérée (barème forfaitaire)
- **Gratification/13ème mois** : souvent conventionnelle
- **Congé maternité** : 14 semaines à 100%, pris en charge CNPS
- **Accident du travail** : taux variable selon branche d'activité (1% à 5%)

### 7.2 Heures supplémentaires par pays

| Pays | Semaine légale | Palier 1 | Palier 2 | Nuit | Dimanche/Férié |
|---|---|---|---|---|---|
| **Cameroun** | 40h | +25% (h41-48) | +40% (h49-60) | +50% | +75% |
| **France** | 35h | +25% (h36-43) | +50% (h44+) | +25-50% (convention) | +100% (dimanche) |
| **USA (fédéral)** | 40h | +50% (h41+) | N/A | Pas d'obligation fédérale | Pas d'obligation fédérale |
| **USA (Californie)** | 40h/8h jour | +50% (h9-12/jour) | +100% (h13+/jour) | N/A | +50% (7ème jour consécutif) |

**SMIG Cameroun** : 43 969 FCFA/mois (fonctionnaires), 45 000 FCFA (agriculture), 60 000 FCFA (privé non-agricole).

### 7.3 Saisies sur salaire

Ordre de priorité légal :
1. Pension alimentaire (aucun plafond, prélevée en premier)
2. Saisie-arrêt du Trésor (impôts)
3. Saisies des autres créanciers

Quotité saisissable (barème progressif camerounais) : fraction du salaire net saisissable selon tranches, protégeant un minimum vital.

### 7.4 Expatriés et Shadow Payroll

- **Home-based** : employé payé depuis le pays d'origine, ajustements coût de la vie
- **Host-based** : employé payé selon les conditions locales du pays d'accueil
- **Balance sheet** : garantie "no better, no worse off". Calculs d'impôt hypothétique, différentiels logement/biens
- **Tax equalization** : l'employeur absorbe la différence de charge fiscale
- **Shadow payroll** : paie parallèle dans le pays d'accueil calculant (sans décaisser) les obligations fiscales et sociales locales

---

## 8. GAPS CRITIQUES DE L'IMPLÉMENTATION ACTUELLE

### Gap 1 : Calcul figé en dur (CRITIQUE)

Tous les taux, tranches, plafonds sont des `static final` dans `PayrollCalculationEngine.java`. Impossible de :
- Changer un taux sans redéployer
- Supporter un autre pays
- Appliquer un changement de législation avec une date d'effet
- Gérer des taux AT différents par secteur d'activité

→ **Solution** : modèle `PayRule` / `TaxRule` configurable par tenant, pays et date d'effet, stocké en base.

### Gap 2 : Pas de variables de paie

Le brut est `salaireBase + avantagesNature`. Pas de primes, heures sup, absences, rappels.

→ **Solution** : concept de `PayElement` / `PayVariable` avec un catalogue de rubriques.

### Gap 3 : Pas de quotient familial pour l'IRPP

L'IRPP camerounais utilise un quotient familial (parts selon situation familiale + enfants à charge) qui peut réduire l'impôt de 30-50%.

→ **Solution** : intégrer `Employee.situationFamiliale` + `Dependant` dans le calcul IRPP.

### Gap 4 : Pas de rétroactifs ni de STC

Aucun mécanisme pour recalculer des mois antérieurs ou produire un solde de tout compte.

### Gap 5 : Pas de cumuls annuels

Le bulletin ne montre pas les cumuls depuis janvier. Nécessaire pour : régularisation IRPP fin d'année, DIPE annuelle, contrôle employé.

### Gap 6 : Pas de génération de fichiers

Ni fichiers de virement bancaire, ni DIPE, ni bordereaux CNPS.

### Gap 7 : Pas de prorata temporis

Un employé embauché le 15 reçoit un salaire complet.

### Gap 8 : Pas de saisies sur salaire

Les saisies (pension alimentaire, saisie-arrêt) ont une priorité légale et une quotité maximale.

### Gap 9 : Pas de primes obligatoires

La prime d'ancienneté est obligatoire au Cameroun et non calculée.

### Gap 10 : Pas de cotisations patronales complètes

FNE (1%), taxe patronale (2,5%), CFC patronale (1,5%) manquent.

---

## 9. RECOMMANDATION D'ARCHITECTURE POUR `RT-comops-payroll-core`

### 9.1 Concept central : Pay Element configurable (Rubrique de paie)

```java
record PayElement(
    UUID id,
    UUID tenantId,
    String code,              // "CNPS_PV_EE", "IRPP", "PRIME_ANCIENNETE"
    String label,             // "CNPS Part Salariale"
    PayElementCategory category,    // EARNING, DEDUCTION, EMPLOYER_CHARGE, INFORMATIONAL
    CalculationMethod method,       // RATE, BRACKET, FLAT, CUSTOM
    String baseReference,           // "GROSS", "BASE_SALARY", "CNPS_CEILING", "IRPP"
    BigDecimal rate,                // 0.042 pour CNPS
    BigDecimal ceiling,             // 750000 pour CNPS PV
    BigDecimal floor,               // 62000 seuil exonération
    String bracketTableCode,        // référence vers table de tranches
    boolean taxable,                // soumis à l'IRPP ?
    boolean socialContributable,    // soumis CNPS ?
    String countryCode,             // "CM", "GA", "FR"
    int displayOrder,
    LocalDate effectiveFrom,        // date d'effet
    LocalDate effectiveTo           // date de fin (null = en vigueur)
)
```

### 9.2 Structure proposée du module

```
RT-comops-payroll-core/
  domain/
    model/
      PayrollRun                      (cycle de paie : DRAFT→CALCULATED→VALIDATED→PAID→CLOSED)
      PayrollEntry                    (entrée par employé)
      PayslipLine                     (ligne de bulletin)
      PayElement                      (rubrique de paie configurable)
      PayElementCategory              (EARNING, DEDUCTION, EMPLOYER_CHARGE, INFORMATIONAL)
      CalculationMethod               (RATE, BRACKET, FLAT, CUSTOM)
      TaxBracketTable                 (table de tranches d'imposition)
      TaxBracket                      (tranche : min, max, taux)
      PayVariable                     (variable de paie mensuelle par employé)
      RetroactiveAdjustment           (régularisation rétroactive)
      PaymentBatch                    (lot de paiement bancaire/mobile)
      PaymentInstruction              (instruction de paiement individuelle)
      AnnualAccumulator               (cumuls annuels par employé)
      GarnishmentOrder                (saisie sur salaire)
      FinalSettlement                 (solde de tout compte)
      PayrollRunStatus                (DRAFT, VARIABLES_LOCKED, CALCULATED, REVIEW, VALIDATED, APPROVED, PAYMENT_INITIATED, PAID, CLOSED)
      PaymentStatus                   (PENDING, PROCESSING, COMPLETED, FAILED)
      
  application/
    service/
      PayrollCalculationPipeline      (orchestrateur de calcul)
      GrossPayCalculator              (assemblage du brut depuis rubriques)
      StatutoryDeductionCalculator    (cotisations + impôts via règles configurables)
      VoluntaryDeductionProcessor     (prêts, saisies, acomptes)
      RetroactiveEngine               (recalcul rétroactif)
      PayslipGenerator                (génération bulletin légal complet)
      PaymentFileGenerator            (fichiers virement CEMAC/SEPA/Mobile Money)
      DeclarationGenerator            (DIPE, État 301, bordereaux CNPS)
      AccountingEntryGenerator        (écritures OHADA automatiques)
      PayrollSimulationService        (simulation what-if)
      FinalSettlementService          (STC au départ)
      AnnualAccumulatorService        (cumuls annuels)
      
    port/
      in/
        RunPayrollUseCase
        SimulatePayrollUseCase
        ValidatePayrollUseCase
        GeneratePayslipUseCase
        GeneratePaymentFileUseCase
        GenerateDeclarationUseCase
        ManagePayElementUseCase
        ManageTaxBracketUseCase
        ManagePayVariableUseCase
        CalculateFinalSettlementUseCase
        
      out/
        PayrollRunRepository
        PayrollEntryRepository
        PayslipLineRepository
        PayElementRepository
        TaxBracketTableRepository
        PayVariableRepository
        AnnualAccumulatorRepository
        GarnishmentOrderRepository
        FinalSettlementRepository
        PaymentBatchRepository
        EmployeePayrollPort           (lecture employé+contrat+dépendants depuis hrm-core)
        LeaveBalancePort              (lecture solde congés depuis hrm-core)
        TimesheetPort                 (lecture heures depuis hrm-core)
        LoanPort                      (lecture prêts depuis hrm-core)
        AccountingPort                (push écritures vers accounting-core)
        PaymentGatewayPort            (interface banques/mobile money)
        
  adapter/
    in/web/
      PayrollController               (endpoints admin)
      PayslipController               (endpoints self-service employé)
      PayElementController            (configuration rubriques)
      TaxRuleController               (configuration barèmes)
      DeclarationController           (déclarations sociales)
      
    out/persistence/
      (repositories R2DBC pour toutes les entités)
```

### 9.3 Pipeline de calcul

```
1. COLLECT INPUTS
   ├── Employee master data (contrat, catégorie, ancienneté, situation familiale)
   ├── Pay variables du mois (primes, heures sup, absences)
   ├── Timesheets (heures travaillées, heures sup)
   ├── Leave data (congés sans solde, maladie)
   └── Active pay elements (rubriques applicables)

2. CALCULATE GROSS
   ├── Salaire de base (proraté si entrée/sortie en cours de mois)
   ├── + Avantages en nature
   ├── + Primes (ancienneté, transport, logement, etc.)
   ├── + Heures supplémentaires (par palier de majoration)
   ├── + Variables du mois
   ├── − Retenues absence
   └── = BRUT

3. APPLY STATUTORY DEDUCTIONS (via pay elements configurables)
   ├── CNPS PV salarié (taux × min(brut, plafond))
   ├── CFC salarié (taux × brut)
   ├── IRPP (barème progressif avec quotient familial)
   ├── CAC (% IRPP)
   ├── RAV (barème forfaitaire)
   ├── TDL (barème forfaitaire)
   └── = Total cotisations + impôts salarié

4. APPLY VOLUNTARY DEDUCTIONS
   ├── Prêts employeur (mensualités)
   ├── Acomptes versés
   ├── Saisies sur salaire (par priorité, dans la quotité)
   └── Autres retenues volontaires

5. CALCULATE NET
   └── NET = BRUT − Cotisations − Impôts − Retenues volontaires

6. CALCULATE EMPLOYER CHARGES (informatif)
   ├── CNPS PV patronal
   ├── CNPS AF
   ├── CNPS AT (taux secteur)
   ├── FNE
   ├── CFC patronal
   ├── Taxe patronale
   └── = Coût total employeur

7. GENERATE OUTPUTS
   ├── PayrollEntry (résumé chiffré)
   ├── PayslipLines (détail du bulletin)
   ├── AnnualAccumulator update (cumuls)
   ├── Accounting entries (écritures OHADA)
   └── Payment instructions
```

### 9.4 Priorités d'implémentation

**Priorité 1 — MVP (indispensable)**
1. Rubriques de paie configurables (PayElement) — remplacer le calcul hardcodé
2. Quotient familial pour l'IRPP — correction d'un bug fiscal
3. Heures supplémentaires — raccordement avec les timesheets existants
4. Primes (ancienneté obligatoire, transport, logement)
5. Prorata temporis — entrée/sortie en cours de mois
6. Cumuls annuels — indispensable pour DIPE et bulletin légal
7. Bulletin de paie complet — conformité Code du travail Art. 68
8. Cotisations patronales complètes (FNE, taxe patronale, CFC patronale)

**Priorité 2 — Important**
9. Rétroactifs — rappel de salaire, correction erreur
10. STC (Solde de Tout Compte) — calcul au départ
11. Saisies sur salaire — quotité saisissable, priorité
12. Fichiers de virement (format bancaire camerounais + Mobile Money)
13. Génération DIPE / bordereaux CNPS
14. Écritures comptables OHADA automatiques
15. Simulation / What-if
16. Cycle de vie étendu (DRAFT → ... → CLOSED)

**Priorité 3 — Multi-pays / Scale**
17. Tax rule engine multi-pays — règles par pays, changement sans redéploiement
18. Multi-devises pour expatriés
19. Conventions collectives — grilles salariales par secteur
20. Paie complémentaire (run additionnel dans le mois)
21. Comparaison M/M-1 avec détection d'anomalies
22. Archivage légal avec signature électronique
23. Shadow payroll pour expatriés
24. On-demand pay (accès anticipé au salaire)
