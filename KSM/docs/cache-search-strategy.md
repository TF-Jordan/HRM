# Redis And Elasticsearch Strategy

## Etat actuel
- `Kafka`: integre et utilise en runtime pour le relay externe de l outbox
- `Redis`: integre pour le cache de permissions, activable par feature flag
- `Elasticsearch`: integre pour les projections de recherche, activable par feature flag

Ce choix est volontaire.

- la source de verite transactionnelle reste PostgreSQL
- `Kafka` traite l integration evenementielle
- `Redis` et `Elasticsearch` restent optionnels tant que l infra n est pas fournie
- leur activation ne doit pas modifier les invariants transactionnels

## Redis

### Usages autorises
- cache de lecture court pour des vues couteuses et tres consultees
- rate limiting transverse
- blacklist de tokens / sessions courtes si le volume l exige
- coordination volatile tres localisee si une contention reelle apparait

### Modules ou Redis peut avoir une vraie valeur
- `iwm-auth-core`
  - revocation courte duree
  - throttle de tentatives
- `iwm-roles-core`
  - cache des permissions resolues pour `ReactivePermissionResolver`
- `iwm-product-core`
  - cache de catalogues et suggestions tres lues
- `iwm-tp-core`
  - cache de recherche rapide sur des fiches souvent consultees
- `iwm-settings-core`
  - cache de settings de lecture uniquement

### Usages interdits
- source de verite de stock
- source de verite comptable
- generation definitive des sequences documentaires
- persistence de l outbox
- etat de facture, de rapprochement ou de reglement

### Position architecturale
- `Redis` doit rester un adapter `out`
- toute invalidation doit etre pilotee par les evenements d outbox
- en cas de perte totale de Redis, le systeme doit continuer en mode degrade

## Elasticsearch

### Usages autorises
- recherche full-text
- recherche multi-criteres et facettes
- projection de lecture optimisee
- recherche globale transversale

### Modules ou Elasticsearch a une vraie valeur
- `iwm-product-core`
  - recherche catalogue
  - filtres par famille, statut, tags, fabricant
- `iwm-tp-core`
  - recherche tiers par nom, code, role, contact
- `iwm-resource-core`
  - recherche par code, serie, categorie, adresse MAC, IP, site
- `iwm-organization-core`
  - recherche agence / point d interet / domaine

### Ce qui est deja cable
- projection `PRODUCT_CREATED` vers index produit
- projection `THIRD_PARTY_CREATED` vers index tiers
- projection `ORGANIZATION_CREATED` vers index organisation
- projection `MATERIAL_RESOURCE_REGISTERED` et evenements de cycle de vie vers index ressource
- endpoints de recherche:
  - `GET /api/products/search`
  - `GET /api/third-parties/search`
  - `GET /api/resources/search`

### Usages interdits
- ecriture transactionnelle primaire
- arbitrage des invariants metier
- calcul des soldes comptables
- verification de disponibilite de stock en temps reel

### Position architecturale
- `Elasticsearch` doit etre alimente par des projections issues de l outbox
- les indexes doivent etre reconstruisables depuis PostgreSQL + outbox
- une indisponibilite Elasticsearch ne doit pas bloquer les transactions metier

## Priorite recommandee

### 1. Kafka
- deja en place
- reste la priorite pour l integration et la projection asynchrone

### 2. Elasticsearch
- prochaine brique justifiable
- apporte une vraie valeur immediate sur la recherche catalogue, tiers et ressources

### 3. Redis
- a introduire seulement sur mesure
- apres mesures de latence, de charge ou de contention

## Recommandation nette
- ne pas ajouter `Redis` maintenant sans preuve de besoin
- introduire `Elasticsearch` d abord si le besoin de recherche devient prioritaire
- conserver PostgreSQL comme source de verite et `Kafka` comme colonne vertebrale d integration
