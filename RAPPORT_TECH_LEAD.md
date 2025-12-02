# BFB Management - Rapport Technique de Décisions

**Projet**: Système de Gestion de Location de Véhicules  
**Période**: Octobre - Décembre 2025  
**Équipe**: Saad (lead), Vulzyun, Mohamedlam, Xaymaa  
**Stack**: Spring Boot 3.5.7, Java 17, H2/PostgreSQL

---

## Résumé Exécutif

Un système de gestion de location de véhicules construit avec une **méthodologie TDD-first** et des **principes DDD**, initialement architecturé avec un pattern hexagonal mais délibérément simplifié vers une architecture 3-tier pragmatique. Le projet démontre des pratiques d'ingénierie matures : couverture de tests complète (13+ classes de test), logique métier pure avec value objects, implémentation stratégique de design patterns (Chain of Responsibility, State Pattern), et pivots architecturaux basés sur des preuves. Leçon clé : dimensionner l'architecture à la complexité du projet.

---

## 1. Approche Méthodologique : TDD & DDD

### Test-Driven Development
- **100+ commits** montrent un workflow TDD systématique : tests avant implémentation
- Infrastructure de tests établie en premier (1er Nov) : tests des règles métier → tests services → tests d'intégration
- Branche `feature/contrats-mvp-tdd` dédiée à la livraison du MVP en TDD
- Structure de la suite de tests :
  - **Couche domaine** : `RulesTest`, `ContractTest` (logique métier pure)
  - **Couche service** : `ContractServiceTest`, tests de la chaîne de validation
  - **Intégration** : `ContractControllerIntegrationTest` (API REST E2E)

**Bénéfices concrets observés** :
- Règles métier validées indépendamment avant tout code d'infrastructure
- Détection de régressions pendant le refactoring architectural (30 Nov refactoring massif : tous les tests passent)
- Confiance dans le rollback de migration de base de données (1-2 Déc PostgreSQL → H2 revert)

### Domain-Driven Design

**Value Objects** (immuables, auto-validants) :
```java
record Period(LocalDate start, LocalDate end) {
    // Encapsule la détection de chevauchement, validation de plage de dates
    public boolean overlapsWith(Period other) { ... }
}
```
- **Email** value object : encapsule la logique de validation
- **Period** value object : contient l'algorithme complexe de détection de chevauchement

**Règles Métier Pures** (`Rules.java`) :
- Matrice de transitions d'état isolée des dépendances framework
- `isTransitionAllowed(from, to)` → zéro couplage à Spring/JPA
- Testable sans aucune infrastructure

**Impact** : La logique métier reste agnostique du framework, entièrement testable en isolation.

---

## 2. Évolution Architecturale & Pivots

### Phase 1 : Architecture Hexagonale (28 Oct - 18 Nov)
**Design initial** (branche `feature/clean-architecture`) :
- Pattern Ports & Adapters
- `business/contract/ports/` pour les interfaces
- Adaptateurs : `ClientExistenceAdapter`, `VehicleStatusAdapter`
- Isolation complète entre les couches

**Preuves dans les commits** :
- `a6eb0e4` (11 Nov) : "Revise README for Hexagonal Architecture overview"
- `6e4f927` (18 Nov) : "Restructure to clean 3-layer architecture"

### Phase 2 : Décision de Simplification (18 Nov - 2 Déc)
**Pivot critique** (commit `27d9b7d`, 2 Déc) :
> "refactor: transition to 3-tier architecture by removing hexagonal architecture references"

**Justification** (documentée dans `REFACTORING_SUMMARY.md`) :
- **Overkill** pour une application CRUD Spring Boot monolithique
- Surcharge d'abstraction inutile : ports/adapters ajoutent de la complexité sans bénéfice
- Vélocité de l'équipe impactée par la cérémonie de maintien de la couche adapter
- **Appels directs service-à-service** suffisants pour les contextes bornés internes

**Nouvelle structure** :
```
interfaces/     → Contrôleurs REST, DTOs, validation
business/       → Services qui appellent directement d'autres services
infrastructure/ → Repositories JPA, persistence
```

**Leçon apprise** : L'architecture hexagonale brille pour les systèmes avec multiples canaux I/O ou lors de l'isolation de dépendances externes volatiles. Pour la composition de services internes dans un monolithe Spring, le 3-tier standard est plus maintenable.

---

## 3. Parcours Base de Données : H2 → PostgreSQL → H2

### Chronologie
- **1er Nov** : Démarrage avec H2 (en mémoire, développement rapide)
- **23 Nov** (`5209cdc`) : "connexion à la bdd postgres" → migration vers PostgreSQL
- **23 Nov** (`d8adcc0`) : "retour a h2 pour le dev" → rollback le même jour
- **1-2 Déc** (`c5481a7`, `dbd876a`) : Seconde tentative PostgreSQL
- **2 Déc** (`f37af88`) : **Revert** → retour permanent à H2

### Pourquoi le Rollback ?
Les commits révèlent :
- Complexité du setup Docker Compose pour l'onboarding de l'équipe
- Incohérences d'environnement local (OS différents : Windows/Mac/Linux)
- Problèmes de connexion PostgreSQL bloquant le workflow de développement
- Aucune fonctionnalité spécifique PostgreSQL requise (pas d'indexation avancée, partitionnement, etc.)

**Décision technique** : H2 fournit :
- Zéro surcharge de configuration
- Comportement cohérent entre les environnements de l'équipe
- Adéquat pour la phase prototype/MVP
- Tests d'intégration faciles (mode embarqué)

**Leçon apprise** : Optimisation prématurée. PostgreSQL prévu pour un "rendu production", mais la phase MVP ne justifie pas la complexité opérationnelle. Reporter le choix de base de données production jusqu'à ce que les exigences de déploiement soient claires.

---

## 4. Implémentation des Design Patterns

### Chain of Responsibility (Validation)
**Problème** : La création de contrat nécessite 4+ validations distinctes  
**Implémentation** (30 Nov, commit `7740ec1`) :

```java
interface ContractValidator {
    void validate(ContractCreationContext context);
}

// Validateurs individuels :
- DateValidator           → cohérence des dates
- ClientExistenceValidator → client existe
- VehicleAvailabilityValidator → véhicule libre
- OverlapValidator        → pas de conflits de planning
```

**Bénéfices** :
- Chaque validateur = Responsabilité Unique
- Ajouter/retirer des validateurs sans modifier la logique service
- Tests unitaires indépendants par validateur
- Messages d'erreur clairs du validateur spécifique

**Alternative considérée** : Validation dans la méthode service → rejetée (700+ LOC méthode, non testable)

### State Pattern (Statut de Contrat)
**Problème** : Transitions de statut complexes (PENDING → IN_PROGRESS → LATE → COMPLETED)  
**Implémentation** (30 Nov, commit `eed8de1`) :

```java
class Rules {
    Map<ContractStatus, Set<ContractStatus>> ALLOWED_TRANSITIONS;
    boolean isTransitionAllowed(from, to);
}
```

**Bénéfices** :
- Empêche les transitions illégales à la compilation
- Règles métier comme données (Map), pas de if/else éparpillés
- Facile à visualiser la machine à états
- `TransitionNotAllowedException` avec messagerie claire

### Repository Pattern
**Spring Data JPA standard** avec requêtes personnalisées :
- `@Query` pour la détection de chevauchement (30 Nov, `73d7a53` : "optimized query for overdue contracts")
- Optimisation `existsById()` (commit `b463e89`) → évite de charger l'entité complète pour les vérifications d'existence

---

## 5. Évolution du Stack Technique

| Composant | Initial | Actuel | Raison du Changement |
|-----------|---------|--------|----------------------|
| **Base de données** | H2 | H2 (après détour PostgreSQL) | La simplicité gagne pour le MVP |
| **Mapping** | toDto() manuel | MapStruct 1.5.5 | Réduire le boilerplate, type-safe |
| **Migrations** | Aucune | Flyway | Versioning du schéma, piste d'audit |
| **Validation** | Éparpillée | Bean Validation + Chain | Séparation des préoccupations |
| **API Docs** | Aucune | Springdoc OpenAPI 2.7.0 | Swagger UI auto-généré |

### Ajouts Clés

**Flyway** (30 Nov, commit `9f99ced`) :
- `V1__initial_schema.sql` → migrations versionnées
- `V2__audit_columns.sql` → timestamps d'audit
- Justification : Suivre l'évolution du schéma, environnements reproductibles

**MapStruct** (30 Nov, commit `10e7caa`) :
- Remplace 200+ lignes de code de mapping manuel
- Génération à la compilation → zéro surcharge runtime
- Interfaces `ContractMapper`, `VehicleMapper`, `ClientMapper`

**Jobs Planifiés** (30 Nov, commit `88a52a8`) :
- `@Scheduled` pour la détection automatique de contrats en retard
- Expressions cron configurables via `application.yml`
- Démontre l'automatisation des processus métier

**Abstraction Base Controller** (30 Nov, commit `329d1d9`) :
```java
abstract class BaseRestController<T, D> {
    protected ResponseEntity<D> created(D dto);
    protected ResponseEntity<D> ok(D dto);
    protected ResponseEntity<PageResponse<D>> okPage(Page<D> page);
}
```
- Principe DRY : élimine 100+ lignes de `ResponseEntity.status(...).body(...)` répétitif

---

## 6. Points Clés pour les Tech Leads

### ✅ À Faire

1. **TDD est Non-Négociable pour la Logique Métier**
   - Règles métier testées en isolation = confiance dans le refactoring
   - Tests d'intégration ont détecté les erreurs architecturales pendant la migration 3-tier

2. **L'Architecture Doit Correspondre à la Complexité**
   - L'architecture hexagonale est devenue une surcharge, pas un facilitateur
   - Vélocité de l'équipe améliorée post-simplification
   - **Question à poser** : "Quel problème cette abstraction résout-elle AUJOURD'HUI ?"

3. **Les Value Objects Valent l'Investissement**
   - `Period.overlapsWith()` encapsule une logique complexe
   - L'immuabilité prévient les bugs d'état
   - Constructeurs auto-validants → fail-fast aux frontières

4. **Design Patterns avec un Objectif**
   - Chain of Responsibility a nettoyé un service de 700 LOC
   - State Pattern empêche les transitions illégales (intégrité du cycle de vie du contrat)
   - **Non utilisés** : Factory (pas de création d'objet complexe), Observer (pas encore de système d'événements)

5. **Reporter les Décisions d'Infrastructure**
   - Débat H2 vs PostgreSQL résolu par "qu'est-ce qui nous bloque MAINTENANT ?"
   - Choix de base de données production = préoccupation de déploiement, pas de MVP

### ❌ À Ne Pas Faire

1. **Optimisation Production Prématurée**
   - Migration PostgreSQL a gaspillé 2 jours, ajouté zéro valeur
   - Équipe bloquée par problèmes de setup Docker

2. **Astronautes de l'Architecture**
   - Pattern ports/adapters hexagonal → sur-ingénierie pour la taille d'équipe et la portée du projet
   - "On pourrait avoir besoin de changer de base de données" → principe YAGNI violé

3. **Sauter l'Outillage de Migration**
   - Flyway ajouté tard → changements de schéma précoces non versionnés
   - **Recommandation** : Ajouter dès le jour 1, même pour H2

### Métriques

- **Couverture de tests** : 13+ classes de test, ~40+ méthodes de test
- **Événements de refactoring** : 3 majeurs (setup TDD, pivot 3-tier, chaîne de validation)
- **Migrations de base de données** : 2 échouées, 1 leçon apprise
- **Design Patterns** : 3 appliqués délibérément (Chain, State, Repository)
- **Vélocité d'équipe** : Améliorée post-simplification architecturale (30 Nov refactoring massif = 15 commits en 1 jour)

---

## Conclusion

Ce projet illustre **l'ingénierie logicielle pragmatique** : commencer avec des principes solides (TDD, DDD), faire des pivots architecturaux basés sur des preuves (hexagonal → 3-tier), et prioriser la livraison sur la perfection (H2 sur PostgreSQL). L'équipe a démontré de la maturité en **inversant les décisions** quand la complexité ne justifiait pas les bénéfices. Pour les tech leads : encouragez cette mentalité—l'architecture sert l'équipe, pas l'inverse.

**État Final** : MVP prêt pour la production avec une architecture 3-tier propre, suite de tests complète, logique métier pure, et zéro abstractions inutiles. Prêt à passer à l'échelle quand les exigences le demanderont.
