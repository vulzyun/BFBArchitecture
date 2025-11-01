# Diagramme de Classes - Module Contrats

Ce diagramme présente l'architecture hexagonale du module de gestion des contrats.

```mermaid
classDiagram
    %% Domain Layer
    class Contrat {
        -UUID id
        -UUID clientId
        -UUID vehiculeId
        -LocalDate dateDebut
        -LocalDate dateFin
        -EtatContrat etat
        +start()
        +terminate()
        +cancel()
        +markLate()
        +isOccupant() boolean
    }
    
    class EtatContrat {
        <<enumeration>>
        EN_ATTENTE
        EN_COURS
        EN_RETARD
        TERMINE
        ANNULE
    }
    
    class Rules {
        <<static>>
        +transitionAllowed(from, to) boolean
    }
    
    %% Ports (Interfaces)
    class ContratRepository {
        <<interface>>
        +save(Contrat) Contrat
        +findById(UUID) Optional~Contrat~
        +findByEtat(EtatContrat) List~Contrat~
        +findByVehiculeIdAndEtat(UUID, EtatContrat) List~Contrat~
        +findOverlappingContrats(UUID, LocalDate, LocalDate) List~Contrat~
        +findByCriteria(UUID, UUID, EtatContrat, Pageable) Page~Contrat~
    }
    
    class VehicleStatusPort {
        <<interface>>
        +getStatus(UUID) EtatVehicule
    }
    
    class ClientExistencePort {
        <<interface>>
        +existsById(UUID) boolean
    }
    
    %% Business Layer
    class ContratService {
        -ContratRepository repository
        -VehicleStatusPort vehiclePort
        -ClientExistencePort clientPort
        -Counter canceledByVehicleDown
        -Counter canceledByLateBlock
        +create(UUID, UUID, LocalDate, LocalDate) Contrat
        +start(UUID) Contrat
        +terminate(UUID) Contrat
        +cancel(UUID) Contrat
        +markLateIfOverdue() int
        +cancelPendingContractsForVehicle(UUID) int
        +markLateAndCancelBlocked() int
        +findById(UUID) Contrat
        +findByCriteria(..., Pageable) Page~Contrat~
    }
    
    %% Adapters Layer
    class ContratRepositoryJpa {
        <<JPA Repository>>
    }
    
    class VehicleStatusAdapter {
        <<stub>>
        +getStatus(UUID) EtatVehicule
    }
    
    class ClientExistenceAdapter {
        <<stub>>
        +existsById(UUID) boolean
    }
    
    %% Presentation Layer
    class ContratController {
        -ContratService service
        -ContratMapper mapper
        +create(CreateContratDto) ResponseEntity
        +getById(UUID) ResponseEntity
        +search(..., Pageable) ResponseEntity
        +start(UUID) ResponseEntity
        +terminate(UUID) ResponseEntity
        +cancel(UUID) ResponseEntity
    }
    
    class VehicleEventsController {
        -ContratService service
        +handleVehicleMarkedDown(VehicleMarkedDownRequest) ResponseEntity
    }
    
    class CreateContratDto {
        +UUID clientId
        +UUID vehiculeId
        +LocalDate dateDebut
        +LocalDate dateFin
    }
    
    class ContratDto {
        +UUID id
        +UUID clientId
        +UUID vehiculeId
        +LocalDate dateDebut
        +LocalDate dateFin
        +EtatContrat etat
    }
    
    class ContratMapper {
        +toDto(Contrat) ContratDto
        +toDomain(CreateContratDto) Contrat
    }
    
    %% Relations
    Contrat --> EtatContrat
    Contrat --> Rules
    ContratService --> Contrat
    ContratService --> ContratRepository
    ContratService --> VehicleStatusPort
    ContratService --> ClientExistencePort
    ContratRepositoryJpa ..|> ContratRepository
    VehicleStatusAdapter ..|> VehicleStatusPort
    ClientExistenceAdapter ..|> ClientExistencePort
    ContratController --> ContratService
    ContratController --> ContratMapper
    VehicleEventsController --> ContratService
    ContratMapper --> Contrat
    ContratMapper --> ContratDto
    ContratMapper --> CreateContratDto
```

## Légende

- **Domain Layer** : Contrat, EtatContrat, Rules (logique métier pure)
- **Ports** : Interfaces définissant les besoins du domaine
- **Business Layer** : ContratService (orchestration et règles métier)
- **Adapters** : Implémentations concrètes des ports (JPA, stubs)
- **Presentation** : Controllers REST et DTOs

## Principes appliqués

1. **Hexagonal Architecture** : Le domaine ne dépend que de ses ports
2. **Dependency Inversion** : Les adapters implémentent les interfaces du domaine
3. **Single Responsibility** : Chaque classe a une responsabilité unique
4. **Clean Architecture** : Séparation claire des couches avec dépendances unidirectionnelles
