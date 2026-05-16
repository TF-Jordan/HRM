# Post KSM_V0.1 Roadmap

Etat de travail au `2026-03-09`.

## Objet

Le projet a atteint la parite fonctionnelle utile avec `KSM_V0.1`.  
La suite ne doit plus consister a recopier le legacy, mais a etendre proprement le systeme sur le socle gele.

## Axes prioritaires

### 1. Metier au-dela du legacy

- workflows d'approbation et de validation plus fins
- politiques de prix et remises avancees
- tresorerie plus riche:
  - rapprochement semi-automatique
  - import de formats bancaires reels
  - audit complet des reglements
- ressources:
  - maintenance planifiee
  - exploitation avancee des historiques geo/reseau

### 2. Durcissement produit

- secrets industrialises
- politiques d'acces par cas d'usage
- alerting et runbooks d'exploitation
- charge et resilience

### 3. Experience equipe

- garder les tests de parite lisibles et rapides
- ajouter des tests de contrat plus fins par module
- eviter toute reintroduction de logique cross-module implicite

## Regles pour ouvrir de nouvelles evolutions

- ne pas casser la matrice de parite legacy
- ne pas contourner `Liquibase`, `Kafka`, `Redis`, `Elasticsearch` par bricolage local
- toute nouvelle integration externe doit passer par un port et un adapter explicites
- toute nouvelle API doit etre documentee dans `README` et les docs d'exploitation si elle affecte le runtime

## Backlog recommande

1. policies metier fines par role, organisation et agence
2. import/export bancaires reels
3. enrichissement des recherches Elasticsearch
4. projections et dashboards fonctionnels supplementaires
5. scenarios k6 plus riches sur `orders / invoices / treasury / search`
6. enrichissement `third-parties` au-dela du legacy:
   - scoring prospects
   - segmentation commerciale
   - workflows de relance
   - unification analytics tiers / ventes / recouvrement

## Premier palier deja ouvert

- qualification commerciale des tiers:
  - `segment`
  - `qualificationScore`
- base saine pour aller vers:
  - scoring automatique
  - priorisation commerciale
  - relances par segment
