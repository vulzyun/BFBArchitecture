# ADR-001: Architecture Hexagonale (Ports & Adapters)

## Statut
**Accepté** - 2025-11-01

## Contexte
Le projet BFBManagement nécessite une architecture modulaire, testable et évolutive pour la gestion des contrats de location de véhicules. Nous devons isoler la logique métier des détails techniques (base de données, API REST, services externes).

## Décision
Nous adoptons l'**architecture hexagonale** (Ports & Adapters) pour structurer l'application.

### Structure des couches

#### 1. Domain Layer (`architecture.contrats.domain`)
- **Contrat** : Entité JPA + méthodes métier (start, cancel, markLate...)
- **EtatContrat** : Énumération des états possibles
- **Rules** : Classe utilitaire pour valider les transitions d'états
- **ContratRepository** : Port (interface) pour la persistance

**Principe** : Le domaine ne dépend d'aucune technologie. Il définit ses propres interfaces (ports).

#### 2. Business Layer (`business.contrats`)
- **ContratService** : Orchestration des règles métier
- **Ports** : 
  - `VehicleStatusPort` : Interface pour vérifier l'état d'un véhicule
  - `ClientExistencePort` : Interface pour vérifier l'existence d'un client
- **Exceptions** : Exceptions métier spécifiques

**Principe** : Le service métier dépend des ports, pas des implémentations.

#### 3. Adapters Layer (`architecture.contrats.adapters` et `presentation`)
- **ContratRepositoryJpa** : Implémentation JPA de `ContratRepository`
- **VehicleStatusAdapter** : Stub/implémentation de `VehicleStatusPort`
- **ClientExistenceAdapter** : Stub/implémentation de `ClientExistencePort`

**Principe** : Les adapters implémentent les ports définis par le domaine.

#### 4. Presentation Layer (`presentation.contrats`)
- **ContratController** : API REST publique
- **VehicleEventsController** : API REST interne (événements)
- **DTOs** : CreateContratDto, ContratDto
- **Mapper** : Conversion Contrat ↔ DTO

**Principe** : La présentation dépend du service métier, pas directement du domaine.

## Alternatives considérées

### 1. Architecture en couches traditionnelle (Layered)
**Rejeté** : Couplage fort entre les couches, difficile à tester unitairement sans Spring.

### 2. CQRS complet avec Event Sourcing
**Rejeté** : Trop complexe pour les besoins actuels. Pas de besoin d'audit événementiel complet.

### 3. Domain-Driven Design (DDD) complet avec Aggregates
**Partiellement appliqué** : 
- ✅ Ubiquitous Language : EtatContrat, Rules, transitions
- ✅ Entities : Contrat
- ❌ Aggregates complexes : pas nécessaire ici
- ❌ Value Objects : LocalDate suffit pour les dates

## Conséquences

### Positives ✅
1. **Testabilité** : Le domaine et le service sont testables sans contexte Spring
2. **Flexibilité** : Facile de changer de base de données ou d'API externe (remplacer un adapter)
3. **Clarté** : Séparation nette entre logique métier et infrastructure
4. **Evolutivité** : Ajout de nouvelles fonctionnalités sans impacter le cœur métier

### Négatives ❌
1. **Complexité initiale** : Plus de fichiers et d'interfaces qu'une architecture simple
2. **Overhead** : Pour un petit projet, peut sembler sur-architecturé
3. **Courbe d'apprentissage** : Les développeurs doivent comprendre les principes SOLID et la DIP

### Neutres ⚖️
1. **Spring Dependency Injection** : Utilisé pour câbler les adapters aux ports
2. **JPA dans le domaine** : Compromis acceptable (@Entity sur Contrat) pour éviter un mapping supplémentaire

## Implémentation

### Exemple de flux
```
HTTP Request → ContratController 
             → ContratService (Business)
             → ContratRepository (Port)
             → ContratRepositoryJpa (Adapter/JPA)
```

### Injection de dépendances
```java
@Service
public class ContratService {
    // Dépend des ports (interfaces)
    private final ContratRepository repository;
    private final VehicleStatusPort vehiclePort;
    private final ClientExistencePort clientPort;
    
    // Spring injecte les adapters concrets
    public ContratService(
        ContratRepository repository,
        VehicleStatusPort vehiclePort,
        ClientExistencePort clientPort
    ) { ... }
}
```

## Validation
- ✅ Tests unitaires sans Spring : `ContratServiceTest`
- ✅ Tests d'intégration avec Spring : `ContratControllerIT`
- ✅ Indépendance technologique : possibilité de remplacer H2 par PostgreSQL sans toucher au service

## Références
- [Hexagonal Architecture (Alistair Cockburn)](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture (Robert C. Martin)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Dependency Inversion Principle](https://en.wikipedia.org/wiki/Dependency_inversion_principle)
