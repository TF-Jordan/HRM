# Domain Enrichment Roadmap

## Objectif

Faire evoluer le produit au dela de `KSM_V0.1` sans rebrasser le socle.

## Axes

### Administration generale

- options generales admin pleinement persistantes
- politiques plus fines par scope exact
- audit admin encore plus exploitable

### Gouvernance plateforme

- workflows d approbation enrichis pour `BusinessActor`, `Organization`, `Agency`
- regles de suspension, blocage, reactivation plus riches
- tableaux de bord d etat admin via APIs

### Metier transverse

- regles plus riches `sales -> accounting -> treasury`
- politiques stock plus fines par agence
- enrichissement `resource-core` sur maintenance, garanties, usage et cout

### Recherche et operations

- projections Elasticsearch plus riches
- quotas differencies par tenant et par type d operation
- analytique operationnelle sans casser le modele metier

## Regle

Chaque enrichissement doit:
- rester dans le bon module
- passer par ports et evenements
- conserver la lisibilite hexagonale
