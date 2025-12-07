# 🎨 Design Patterns - Projet BFB Architecture

> **Système de Gestion de Locations Automobiles**  
> Stack: Spring Boot 3.5.7 • Java 17 • Architecture 3-Tiers

---

## 📋 Table des Matières

1. [Vue d'Ensemble](#vue-densemble)
2. [Patterns Architecturaux](#patterns-architecturaux)
3. [Patterns Comportementaux](#patterns-comportementaux)
4. [Patterns Structurels](#patterns-structurels)
5. [Patterns de Validation](#patterns-de-validation)
6. [Patterns de Persistance](#patterns-de-persistance)
7. [Patterns d'Injection de Dépendances](#patterns-dinjection-de-dépendances)
8. [Anti-Patterns Évités](#anti-patterns-évités)
9. [Questions/Réponses de Soutenance](#questionsréponses-de-soutenance)

---

## 🎯 Vue d'Ensemble

Le projet BFB implémente **8 design patterns majeurs** pour garantir:
- ✅ **Maintenabilité** : Code modulaire et découplé
- ✅ **Testabilité** : Tests unitaires sans contexte Spring
- ✅ **Extensibilité** : Ajout facile de nouvelles fonctionnalités
- ✅ **Robustesse** : Validation stricte des règles métier

---

## 🏗️ 1. Patterns Architecturaux

### 1.1 Architecture 3-Tiers (Layered Architecture)

**Structure du projet:**

```
┌─────────────────────────────────────┐
│   COUCHE PRÉSENTATION               │  ← Controllers, DTOs, Validation API
│   (interfaces/rest/)                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   COUCHE MÉTIER                     │  ← Services, Logique métier, Validations
│   (business/)                       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   COUCHE DONNÉES                    │  ← Repositories, JPA Entities
│   (infrastructure/persistence/)     │
└─────────────────────────────────────┘
```

**Implémentation:**

```java
// COUCHE PRÉSENTATION
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController extends BaseRestController<Client, ClientDto> {
    private final ClientService clientService;
    
    @PostMapping
    public ResponseEntity<ClientDto> create(@Valid @RequestBody CreateClientRequest request) {
        Client client = clientService.create(
            request.firstName(), 
            request.lastName(), 
            request.address(), 
            request.licenseNumber(), 
            request.birthDate()
        );
        return created(clientMapper.toDto(client));
    }
}

// COUCHE MÉTIER
@Service
@Transactional
public class ClientService {
    private final ClientRepository clientRepository;
    
    public Client create(String firstName, String lastName, String address, 
                        String licenseNumber, LocalDate birthDate) {
        // Validation des règles métier
        if (clientRepository.existsByLicenseNumber(licenseNumber)) {
            throw new DuplicateLicenseException(...);
        }
        return clientRepository.save(new Client(...));
    }
}

// COUCHE DONNÉES
@Component
public class ClientRepositoryImpl implements ClientRepository {
    private final ClientJpaRepository jpaRepository;
    
    @Override
    public Client save(Client client) {
        ClientEntity entity = toEntity(client);
        ClientEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
}
```

**Avantages:**
- ✅ Séparation claire des responsabilités (SRP)
- ✅ Logique métier isolée du framework Spring
- ✅ Tests unitaires sans contexte applicatif
- ✅ Changement de persistance/API sans impact métier

---

### 1.2 Dependency Injection (Spring IoC)

**Injection par constructeur (Best Practice):**

```java
@Service
@Transactional
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractValidationChain validationChain;

    // Injection par constructeur (immutable)
    public ContractService(
            ContractRepository contractRepository,
            ContractValidationChain validationChain) {
        this.contractRepository = contractRepository;
        this.validationChain = validationChain;
    }
}
```

**Avantages:**
- ✅ Immutabilité des dépendances
- ✅ Facilite les tests (injection de mocks)
- ✅ Détection des dépendances circulaires au démarrage

---

## 🔄 2. Patterns Comportementaux

### 2.1 Chain of Responsibility (Validation Chain)

**Contexte:** Valider la création d'un contrat selon plusieurs règles métier indépendantes.

**Implémentation:**

```java
// Interface commune pour tous les validateurs
public interface ContractValidator {
    void validate(ContractCreationContext context);
}

// Chaîne de validation
@Component
public class ContractValidationChain {
    private final List<ContractValidator> validators;

    public ContractValidationChain(
            DateValidator dateValidator,
            ClientExistenceValidator clientExistenceValidator,
            VehicleAvailabilityValidator vehicleAvailabilityValidator,
            OverlapValidator overlapValidator) {
        this.validators = List.of(
            dateValidator,                      // 1️⃣ Dates valides ?
            clientExistenceValidator,           // 2️⃣ Client existe ?
            vehicleAvailabilityValidator,       // 3️⃣ Véhicule disponible ?
            overlapValidator                    // 4️⃣ Pas de chevauchement ?
        );
    }

    public void validateAll(ContractCreationContext context) {
        validators.forEach(validator -> validator.validate(context));
    }
}
```

**Exemple de validateur:**

```java
@Component
public class OverlapValidator implements ContractValidator {
    private final ContractRepository contractRepository;

    @Override
    public void validate(ContractCreationContext context) {
        List<Contract> overlapping = contractRepository.findOverlappingContracts(
            context.vehicleId(),
            context.startDate(),
            context.endDate()
        );
        
        if (!overlapping.isEmpty()) {
            throw new OverlapException(
                String.format("Vehicle %s already booked from %s to %s",
                    context.vehicleId(), 
                    overlapping.get(0).getStartDate(),
                    overlapping.get(0).getEndDate())
            );
        }
    }
}
```

**Utilisation dans le service:**

```java
@Service
public class ContractService {
    private final ContractValidationChain validationChain;

    public Contract create(UUID clientId, UUID vehicleId, 
                          LocalDate startDate, LocalDate endDate) {
        ContractCreationContext context = new ContractCreationContext(
            clientId, vehicleId, startDate, endDate
        );
        
        // La chaîne exécute tous les validateurs
        validationChain.validateAll(context);
        
        return createAndSaveContract(clientId, vehicleId, startDate, endDate);
    }
}
```

**Avantages:**
- ✅ Ajout de nouvelles validations sans modifier le service
- ✅ Chaque validateur a une responsabilité unique (SRP)
- ✅ Ordre d'exécution contrôlé
- ✅ Tests unitaires indépendants par validateur

---

### 2.2 State Pattern (Contract Status Management)

**Contexte:** Gérer les transitions d'état d'un contrat avec des règles strictes.

**Diagramme d'états:**

```
PENDING ──┬──> IN_PROGRESS ──┬──> COMPLETED
          │                  └──> LATE ──> COMPLETED
          └──> CANCELLED
```

**Implémentation:**

```java
public enum ContractStatus {
    
    PENDING {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.of(IN_PROGRESS, CANCELLED);
        }
    },
    
    IN_PROGRESS {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.of(LATE, COMPLETED);
        }
    },
    
    LATE {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.of(COMPLETED);
        }
    },
    
    COMPLETED {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.noneOf(ContractStatus.class);
        }
    },
    
    CANCELLED {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.noneOf(ContractStatus.class);
        }
    };

    // Méthode abstraite implémentée par chaque état
    public abstract Set<ContractStatus> getAllowedTransitions();

    // Validation des transitions
    public ContractStatus transitionTo(ContractStatus target) {
        if (!getAllowedTransitions().contains(target)) {
            throw new TransitionNotAllowedException(
                String.format("Cannot transition from %s to %s. Allowed: %s",
                    this, target, getAllowedTransitions())
            );
        }
        return target;
    }

    public boolean canTransitionTo(ContractStatus target) {
        return getAllowedTransitions().contains(target);
    }
}
```

**Utilisation dans le modèle Contract:**

```java
public class Contract {
    private ContractStatus status;
    
    public void start() {
        this.status = this.status.transitionTo(ContractStatus.IN_PROGRESS);
    }
    
    public void terminate() {
        this.status = this.status.transitionTo(ContractStatus.COMPLETED);
    }
    
    public void cancel() {
        this.status = this.status.transitionTo(ContractStatus.CANCELLED);
    }
    
    public void markLate() {
        this.status = this.status.transitionTo(ContractStatus.LATE);
    }
}
```

**Exemple de rejet de transition invalide:**

```java
Contract contract = new Contract(...);
contract.setStatus(ContractStatus.COMPLETED);

contract.cancel(); // ❌ Exception: Cannot transition from COMPLETED to CANCELLED
```

**Avantages:**
- ✅ Impossible de faire une transition invalide
- ✅ Logique de transition encapsulée dans l'état
- ✅ Ajout de nouveaux états facilité
- ✅ Tests exhaustifs des transitions

---

### 2.3 Strategy Pattern (Validation Strategies)

**Contexte:** Valider des DTOs avec des stratégies de validation personnalisées.

**Implémentation:**

```java
// Annotation personnalisée pour validation de plage de dates
@Constraint(validatedBy = DateRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "Start date must be before end date";
    String startDate();
    String endDate();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Stratégie de validation
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    private String startDateField;
    private String endDateField;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDate();
        this.endDateField = constraintAnnotation.endDate();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            LocalDate startDate = getFieldValue(value, startDateField);
            LocalDate endDate = getFieldValue(value, endDateField);
            
            return startDate == null || endDate == null || startDate.isBefore(endDate);
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Utilisation sur un DTO:**

```java
@ValidDateRange(startDate = "startDate", endDate = "endDate")
public record CreateContractRequest(
    @NotNull UUID clientId,
    @NotNull UUID vehicleId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
```

---

## 🏛️ 3. Patterns Structurels

### 3.1 Repository Pattern (Abstraction de Persistance)

**Contexte:** Isoler la logique métier de la couche de persistance JPA.

**Architecture:**

```
business/contract/service/
    └── ContractRepository (interface)    ← Contrat du domaine
              ↑
              │ implémente
              │
infrastructure/persistence/
    └── ContractRepositoryImpl            ← Adaptation JPA
              ↓
        ContractJpaRepository (Spring Data)
```

**Implémentation:**

```java
// Interface du domaine (business/)
public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(UUID id);
    List<Client> findAll();
    Page<Client> findAll(Pageable pageable);
    boolean existsByLicenseNumber(String licenseNumber);
    boolean existsByFirstNameAndLastNameAndBirthDate(
        String firstName, String lastName, LocalDate birthDate
    );
}

// Implémentation JPA (infrastructure/)
@Component
public class ClientRepositoryImpl implements ClientRepository {
    private final ClientJpaRepository jpaRepository;

    @Override
    public Client save(Client client) {
        ClientEntity entity = toEntity(client);          // Domain → Entity
        ClientEntity saved = jpaRepository.save(entity);
        return toDomain(saved);                          // Entity → Domain
    }

    @Override
    public boolean existsByLicenseNumber(String licenseNumber) {
        return jpaRepository.existsByLicenseNumber(licenseNumber);
    }

    // Conversion Domain ↔ Entity
    private ClientEntity toEntity(Client client) {
        ClientEntity entity = new ClientEntity();
        entity.setId(client.getId());
        entity.setFirstName(client.getFirstName());
        entity.setLastName(client.getLastName());
        // ...
        return entity;
    }

    private Client toDomain(ClientEntity entity) {
        return new Client(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getAddress(),
            entity.getLicenseNumber(),
            entity.getBirthDate()
        );
    }
}

// Spring Data JPA Repository
@Repository
public interface ClientJpaRepository extends JpaRepository<ClientEntity, UUID> {
    boolean existsByLicenseNumber(String licenseNumber);
    boolean existsByFirstNameAndLastNameAndBirthDate(
        String firstName, String lastName, LocalDate birthDate
    );
}
```

**Avantages:**
- ✅ Domaine isolé de JPA (pas d'annotations JPA sur les modèles)
- ✅ Changement de base de données transparent
- ✅ Tests du service avec mock du repository
- ✅ Couche d'abstraction claire

---

### 3.2 Adapter Pattern (Domain ↔ Entity ↔ DTO)

**Contexte:** Convertir entre 3 représentations d'une même entité.

**Flux de données:**

```
REST Request → CreateClientRequest (DTO)
                      ↓
                ClientMapper
                      ↓
               Client (Domain)
                      ↓
          ClientRepositoryImpl.toEntity()
                      ↓
            ClientEntity (JPA)
                      ↓
               H2 Database
```

**Implémentation avec MapStruct:**

```java
// Mapper automatique (génération à la compilation)
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {
    ClientDto toDto(Client client);
}

// Usage dans le controller
@RestController
public class ClientController {
    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @PostMapping
    public ResponseEntity<ClientDto> create(@Valid @RequestBody CreateClientRequest request) {
        // DTO → Domain
        Client client = clientService.create(
            request.firstName(), 
            request.lastName(), 
            request.address(), 
            request.licenseNumber(), 
            request.birthDate()
        );
        
        // Domain → DTO
        return created(clientMapper.toDto(client));
    }
}
```

**Avantages:**
- ✅ API découplée du modèle interne
- ✅ Contrôle de l'exposition des données
- ✅ Validation au niveau DTO (Bean Validation)
- ✅ Évolution indépendante API/Domain

---

### 3.3 Facade Pattern (BaseRestController)

**Contexte:** Simplifier les réponses HTTP communes dans les controllers.

**Implémentation:**

```java
public abstract class BaseRestController<T, D> {

    protected ResponseEntity<D> created(D dto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(dto);
    }

    protected ResponseEntity<D> ok(D dto) {
        return ResponseEntity.ok(dto);
    }

    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    protected ResponseEntity<Page<D>> okPage(Page<D> page) {
        return ResponseEntity.ok(page);
    }
}
```

**Usage:**

```java
@RestController
public class ClientController extends BaseRestController<Client, ClientDto> {
    
    @PostMapping
    public ResponseEntity<ClientDto> create(@Valid @RequestBody CreateClientRequest request) {
        Client client = clientService.create(...);
        return created(clientMapper.toDto(client));  // ← Méthode héritée
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getById(@PathVariable UUID id) {
        Client client = clientService.findById(id);
        return ok(clientMapper.toDto(client));       // ← Méthode héritée
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return noContent();                          // ← Méthode héritée
    }
}
```

**Avantages:**
- ✅ Code DRY (Don't Repeat Yourself)
- ✅ Standards HTTP cohérents
- ✅ Réduction du boilerplate

---

### 3.4 Value Object Pattern

**Contexte:** Représenter un concept métier immutable avec logique encapsulée.

**Implémentation:**

```java
public record Period(LocalDate startDate, LocalDate endDate) {
    
    // Validation à la construction (record compact constructor)
    public Period {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException(
                String.format("Start date (%s) must be before end date (%s)", 
                    startDate, endDate)
            );
        }
    }
    
    // Logique métier encapsulée
    public boolean overlapsWith(Period other) {
        Objects.requireNonNull(other, "Cannot check overlap with null period");
        return !this.endDate.isBefore(other.startDate) 
            && !other.endDate.isBefore(this.startDate);
    }
    
    public boolean hasEndedBefore(LocalDate date) {
        return endDate.isBefore(date);
    }
    
    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    public long durationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    public static Period of(LocalDate startDate, LocalDate endDate) {
        return new Period(startDate, endDate);
    }
}
```

**Utilisation:**

```java
Period period1 = Period.of(LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 15));
Period period2 = Period.of(LocalDate.of(2025, 12, 12), LocalDate.of(2025, 12, 20));

if (period1.overlapsWith(period2)) {
    throw new OverlapException("Periods overlap!");
}

long duration = period1.durationInDays(); // 5 jours
```

**Avantages:**
- ✅ Immutabilité (thread-safe)
- ✅ Validation automatique
- ✅ Logique métier proche des données
- ✅ Réutilisable dans tout le domaine

---

## ✔️ 4. Patterns de Validation

### 4.1 Bean Validation (JSR 380)

**Validation au niveau DTO:**

```java
public record CreateClientRequest(
    @NotNull(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,
    
    @NotNull(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName,
    
    @NotNull(message = "Address is required")
    String address,
    
    @NotNull(message = "License number is required")
    @Pattern(regexp = "^[A-Z0-9]{8,12}$", message = "Invalid license number format")
    String licenseNumber,
    
    @NotNull(message = "Birth date is required")
    @AdultAge // Validation personnalisée
    LocalDate birthDate
) {}
```

**Validation personnalisée:**

```java
@Constraint(validatedBy = AdultAgeValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdultAge {
    String message() default "Client must be at least 18 years old";
    int minAge() default 18;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class AdultAgeValidator implements ConstraintValidator<AdultAge, LocalDate> {
    private int minAge;

    @Override
    public void initialize(AdultAge constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) return true;
        
        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();
        
        return age >= minAge;
    }
}
```

---

### 4.2 Custom Validation Chain

**Validation métier dans les services:**

```java
@Service
public class ClientService {
    
    public Client create(String firstName, String lastName, String address,
                        String licenseNumber, LocalDate birthDate) {
        // Validation unicité identité
        if (clientRepository.existsByFirstNameAndLastNameAndBirthDate(
                firstName, lastName, birthDate)) {
            throw new DuplicateClientException(
                String.format("A client with name '%s %s' and birth date '%s' already exists",
                    firstName, lastName, birthDate)
            );
        }
        
        // Validation unicité permis
        if (clientRepository.existsByLicenseNumber(licenseNumber)) {
            throw new DuplicateLicenseException(
                String.format("License number '%s' is already registered", licenseNumber)
            );
        }
        
        Client client = new Client(null, firstName, lastName, address, licenseNumber, birthDate);
        return clientRepository.save(client);
    }
}
```

---

## 💾 5. Patterns de Persistance

### 5.1 Unit of Work (Spring @Transactional)

**Gestion transactionnelle déclarative:**

```java
@Service
@Transactional  // ← Toutes les méthodes sont transactionnelles
public class ContractService {
    
    public Contract create(UUID clientId, UUID vehicleId, 
                          LocalDate startDate, LocalDate endDate) {
        // Validation
        validationChain.validateAll(...);
        
        // Création et sauvegarde (dans la même transaction)
        Contract contract = new Contract(...);
        return contractRepository.save(contract);
    }
    
    @Transactional(readOnly = true)  // ← Optimisation lecture seule
    public Contract findById(UUID id) {
        return contractRepository.findById(id)
            .orElseThrow(() -> new ContractNotFoundException(...));
    }
}
```

**Avantages:**
- ✅ Rollback automatique en cas d'exception
- ✅ Cohérence des données garantie
- ✅ Gestion de session Hibernate transparente

---

### 5.2 Active Record vs Domain Model

**Ce projet utilise Domain Model (Anemic Domain) avec services riches:**

```java
// Modèle du domaine (simple POJO)
public class Client {
    private UUID id;
    private String firstName;
    private String lastName;
    // ... getters/setters
}

// Service riche (logique métier)
@Service
public class ClientService {
    public Client create(...) {
        // Validation
        // Logique métier
        // Persistance
    }
}
```

**Alternative Active Record (non utilisée):**

```java
// Le modèle contiendrait la logique métier
public class Client {
    public void save() { ... }
    public static Client findById(UUID id) { ... }
}
```

**Justification Domain Model:**
- ✅ Séparation logique métier / persistance
- ✅ Tests unitaires plus simples (pas de DB)
- ✅ Compatible avec architecture 3-tiers

---

## 🔌 6. Patterns d'Injection de Dépendances

### 6.1 Constructor Injection (Best Practice)

**Toujours utilisé dans ce projet:**

```java
@Service
public class ContractService {
    // Dépendances déclarées final (immutables)
    private final ContractRepository contractRepository;
    private final ContractValidationChain validationChain;

    // Injection par constructeur
    public ContractService(
            ContractRepository contractRepository,
            ContractValidationChain validationChain) {
        this.contractRepository = contractRepository;
        this.validationChain = validationChain;
    }
}
```

**Avantages vs @Autowired sur champs:**
- ✅ Immutabilité (thread-safe)
- ✅ Tests plus faciles (injection manuelle possible)
- ✅ Détection dépendances circulaires au démarrage
- ✅ Obligation de fournir les dépendances

---

### 6.2 Interface Segregation

**Repositories exposent uniquement les méthodes nécessaires:**

```java
// Interface minimale
public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(UUID id);
    boolean existsByLicenseNumber(String licenseNumber);
    // Seulement ce qui est utilisé
}

// L'implémentation peut avoir plus de méthodes
@Component
public class ClientRepositoryImpl implements ClientRepository {
    private final ClientJpaRepository jpaRepository;
    
    // Méthodes publiques
    @Override
    public Client save(Client client) { ... }
    
    // Méthodes privées internes
    private ClientEntity toEntity(Client client) { ... }
    private Client toDomain(ClientEntity entity) { ... }
}
```

---

## ❌ 7. Anti-Patterns Évités

### 7.1 God Object / God Service

**❌ Anti-pattern:**

```java
// Service monolithique faisant tout
@Service
public class RentalManagementService {
    public Client createClient(...) { ... }
    public Vehicle createVehicle(...) { ... }
    public Contract createContract(...) { ... }
    public Payment processPayment(...) { ... }
    // 50+ méthodes...
}
```

**✅ Solution appliquée:**

```java
// Services spécialisés avec responsabilité unique
@Service
public class ClientService { ... }

@Service
public class VehicleService { ... }

@Service
public class ContractService { ... }
```

---

### 7.2 Anemic Domain Model (partiellement applicable)

**Contexte:** Ce projet a des modèles "anémiques" mais c'est un choix architectural.

**❌ Anti-pattern classique:**

```java
// Modèle sans logique (simple conteneur de données)
public class Contract {
    private UUID id;
    private ContractStatus status;
    // Juste getters/setters
}

// Toute la logique dans le service
@Service
public class ContractService {
    public void startContract(UUID id) {
        Contract contract = find(id);
        if (contract.getStatus() != PENDING) throw ...;
        contract.setStatus(IN_PROGRESS);
    }
}
```

**✅ Solution partielle appliquée:**

```java
// Modèle avec logique de transition d'état
public class Contract {
    private ContractStatus status;
    
    // Logique métier encapsulée
    public void start() {
        this.status = this.status.transitionTo(ContractStatus.IN_PROGRESS);
    }
    
    public void cancel() {
        this.status = this.status.transitionTo(ContractStatus.CANCELLED);
    }
}

// Service orchestrateur (plus léger)
@Service
public class ContractService {
    public Contract start(UUID contractId) {
        Contract contract = findByIdOrThrow(contractId);
        contract.start(); // ← Délégation au modèle
        return contractRepository.save(contract);
    }
}
```

**Justification:**
- ✅ Équilibre pragmatique pour application 3-tiers
- ✅ Logique d'état dans le modèle
- ✅ Logique de coordination dans le service

---

### 7.3 Primitive Obsession

**❌ Anti-pattern:**

```java
// Utilisation primitive pour concept métier
public Contract create(LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) throw ...;
    if (startDate.isBefore(LocalDate.now())) throw ...;
    // Logique de dates éparpillée partout
}
```

**✅ Solution appliquée (Value Object):**

```java
// Encapsulation dans Period
public record Period(LocalDate startDate, LocalDate endDate) {
    public Period {
        if (!startDate.isBefore(endDate)) throw ...;
    }
    
    public boolean overlapsWith(Period other) { ... }
    public long durationInDays() { ... }
}

// Usage simplifié
public Contract create(Period period) {
    // Validation automatique via le Value Object
}
```

---

### 7.4 Magic Strings / Magic Numbers

**❌ Anti-pattern:**

```java
if (status.equals("PENDING")) { ... }
if (age < 18) { ... }
```

**✅ Solution appliquée:**

```java
// Énumération typée
public enum ContractStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED, LATE
}

// Constantes
@AdultAge(minAge = 18)
LocalDate birthDate;
```

---

## 📊 8. Récapitulatif des Patterns

| Pattern | Type | Localisation | Complexité | Impact |
|---------|------|--------------|------------|--------|
| **3-Tiers Architecture** | Architectural | Tout le projet | ⭐⭐⭐ | 🎯 Structuration globale |
| **Chain of Responsibility** | Comportemental | `business/contract/validation/` | ⭐⭐⭐ | ✅ Validation extensible |
| **State Pattern** | Comportemental | `ContractStatus` enum | ⭐⭐ | 🔒 Transitions sécurisées |
| **Repository Pattern** | Structurel | `infrastructure/persistence/` | ⭐⭐ | 🗄️ Abstraction persistance |
| **Value Object** | Structurel | `Period`, `Rules` | ⭐ | 📦 Encapsulation concepts |
| **Adapter/Mapper** | Structurel | `interfaces/rest/mapper/` | ⭐⭐ | 🔄 Conversion DTO↔Domain |
| **Facade** | Structurel | `BaseRestController` | ⭐ | 🎭 Simplification API |
| **Strategy** | Comportemental | Bean Validation | ⭐⭐ | ✔️ Validation flexible |
| **Dependency Injection** | Structurel | Spring IoC | ⭐ | 🔌 Couplage faible |

---

## 🎓 Bonnes Pratiques Appliquées

### Principes SOLID

✅ **S**ingle Responsibility
- Un service = un domaine métier (ClientService, VehicleService, ContractService)
- Un validateur = une règle métier

✅ **O**pen/Closed
- Ajout de validateurs sans modifier `ContractValidationChain`
- Extension de `BaseRestController` sans modification

✅ **L**iskov Substitution
- Toutes les implémentations de `ContractValidator` sont interchangeables

✅ **I**nterface Segregation
- Repositories avec méthodes minimales
- Interfaces métier découplées de JPA

✅ **D**ependency Inversion
- Services dépendent des interfaces `Repository`
- Pas de dépendance directe à JPA dans le métier

---

## 📚 Ressources Complémentaires

### Documentation Projet

- [`SOUTENANCE.md`](SOUTENANCE.md) - Présentation complète du projet
- [`demo/README.md`](demo/README.md) - Guide développeur
- [`docs/GUIDE_1_TDD_DDD.md`](docs/GUIDE_1_TDD_DDD.md) - Méthodologie TDD/DDD
- [`docs/GUIDE_2_ARCHITECTURE_EVOLUTION.md`](docs/GUIDE_2_ARCHITECTURE_EVOLUTION.md) - Évolution architecturale

### Références Externes

- **Design Patterns (Gang of Four)** - Gamma, Helm, Johnson, Vlissides
- **Domain-Driven Design** - Eric Evans
- **Implementing Domain-Driven Design** - Vaughn Vernon
- **Clean Architecture** - Robert C. Martin
- **Spring Framework Documentation** - https://spring.io/projects/spring-framework

---

## 🏆 Conclusion

Le projet BFB démontre une **maîtrise approfondie des design patterns** en contexte Spring Boot, avec :

- ✅ **Architecture claire** : 3-tiers bien séparées
- ✅ **Validation robuste** : Chain of Responsibility + Bean Validation
- ✅ **État sécurisé** : State Pattern pour transitions contrôlées
- ✅ **Persistance abstraite** : Repository Pattern découplé de JPA
- ✅ **Code maintenable** : SOLID + patterns structurels
- ✅ **Tests complets** : 24/24 tests passent (TDD strict)

**Ces patterns ne sont pas appliqués "pour le pattern", mais pour résoudre des problèmes concrets de maintenabilité, testabilité et extensibilité.**

---

## 💬 9. Questions/Réponses de Soutenance

### 📐 Architecture & Patterns Architecturaux

#### Q1: Pourquoi avoir choisi une architecture 3-tiers plutôt qu'une architecture hexagonale ?

**Réponse:**

Nous avons démarré avec une architecture hexagonale (Ports & Adapters) mais avons migré vers 3-tiers pour plusieurs raisons pragmatiques :

✅ **Contexte applicatif:**
- Application monolithique Spring Boot (1 seul déploiement)
- Pas de multiples canaux d'entrée (pas de CLI, MQ, gRPC)
- Pas de contraintes de DDD strict avec bounded contexts externes

✅ **Avantages obtenus:**
- Code plus simple et direct
- Moins de couches d'abstraction (pas de ports/adapters superflus)
- Communication directe entre services métier (`ContractService` → `VehicleService`)
- Maintenabilité améliorée pour l'équipe

✅ **Principes préservés:**
- Logique métier toujours isolée dans `business/`
- Aucune dépendance framework dans le domaine
- Testabilité maintenue (mocks des repositories)

**Quand utiliser Hexagonal ?**
- Multiples interfaces (REST + CLI + MQ)
- Changements fréquents de technologie
- Bounded contexts DDD stricts

---

#### Q2: Comment garantissez-vous l'isolation de la couche métier du framework Spring ?

**Réponse:**

**1. Modèles du domaine purs (POJOs):**
```java
// ✅ Aucune annotation Spring/JPA
public class Contract {
    private UUID id;
    private ContractStatus status;
    // Pas de @Entity, @Service, @Autowired
}
```

**2. Interfaces de repositories dans `business/`:**
```java
// Interface métier (pas Spring Data)
public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(UUID id);
}

// Implémentation JPA isolée dans infrastructure/
@Component
public class ContractRepositoryImpl implements ContractRepository { ... }
```

**3. Tests unitaires sans contexte Spring:**
```java
@Test
void shouldValidateOverlap() {
    // Pas de @SpringBootTest
    ContractRepository mockRepo = mock(ContractRepository.class);
    OverlapValidator validator = new OverlapValidator(mockRepo);
    // Test pur sans DB ni Spring
}
```

**Avantages:**
- Migration vers Quarkus/Micronaut possible
- Tests ultra-rapides (pas de contexte Spring)
- Logique métier réutilisable

---

### 🔄 Chain of Responsibility

#### Q3: Pourquoi utiliser le pattern Chain of Responsibility pour les validations plutôt qu'une simple méthode avec des if/else ?

**Réponse:**

**❌ Approche naïve (if/else):**
```java
@Service
public class ContractService {
    public Contract create(...) {
        // Tous les ifs dans une seule méthode
        if (startDate.isAfter(endDate)) throw ...;
        if (!clientExists(clientId)) throw ...;
        if (!vehicleAvailable(vehicleId)) throw ...;
        if (hasOverlap(vehicleId, dates)) throw ...;
        // Logique difficile à étendre et tester
    }
}
```

**✅ Avec Chain of Responsibility:**
```java
@Component
public class ContractValidationChain {
    private final List<ContractValidator> validators;
    
    public ContractValidationChain(
        DateValidator dateValidator,
        ClientExistenceValidator clientValidator,
        VehicleAvailabilityValidator vehicleValidator,
        OverlapValidator overlapValidator
    ) {
        this.validators = List.of(
            dateValidator,      // Ordre important
            clientValidator,
            vehicleValidator,
            overlapValidator
        );
    }
}
```

**Avantages:**

1. **Single Responsibility Principle:**
   - Chaque validateur = 1 règle métier
   - `DateValidator` : validation des dates
   - `OverlapValidator` : chevauchements

2. **Open/Closed Principle:**
   ```java
   // Ajout d'une nouvelle validation SANS modifier le service
   public class PaymentMethodValidator implements ContractValidator {
       @Override
       public void validate(ContractCreationContext context) {
           // Nouvelle règle métier
       }
   }
   
   // Injection automatique via Spring
   public ContractValidationChain(..., PaymentMethodValidator paymentValidator) {
       this.validators = List.of(..., paymentValidator);
   }
   ```

3. **Testabilité:**
   ```java
   // Test unitaire isolé
   @Test
   void shouldRejectOverlappingContracts() {
       OverlapValidator validator = new OverlapValidator(mockRepo);
       // Test uniquement la règle de chevauchement
   }
   ```

4. **Ordre d'exécution contrôlé:**
   - Vérifier dates AVANT d'interroger la BD
   - Vérifier existence client AVANT disponibilité véhicule

---

#### Q4: Pourquoi ne pas utiliser le pattern Decorator au lieu de Chain of Responsibility ?

**Réponse:**

**Chain of Responsibility vs Decorator:**

| Critère | Chain of Responsibility | Decorator |
|---------|------------------------|-----------|
| **But** | Traiter séquentiellement des validations | Enrichir un objet avec des comportements |
| **Arrêt** | S'arrête à la première erreur | Tous les decorators s'exécutent |
| **Usage** | Validation, logging, filtrage | Ajout de fonctionnalités (cache, log, retry) |

**Notre cas (validation):**
- Chaque validateur peut lancer une exception et stopper la chaîne
- Pas besoin d'enrichir un objet
- Ordre strict : dates → client → véhicule → chevauchement

**Decorator serait adapté pour:**
```java
// Ajouter des comportements à un service
Service service = new BasicService();
service = new CachedService(service);
service = new LoggedService(service);
service = new RetryService(service);
```

---

### 🎯 State Pattern

#### Q5: Pourquoi utiliser le State Pattern pour gérer les statuts de contrat ?

**Réponse:**

**❌ Sans State Pattern (logique dispersée):**
```java
public class ContractService {
    public void startContract(UUID id) {
        Contract contract = findById(id);
        
        // Validation manuelle des transitions
        if (contract.getStatus() == ContractStatus.COMPLETED) {
            throw new Exception("Cannot start completed contract");
        }
        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new Exception("Cannot start cancelled contract");
        }
        
        contract.setStatus(ContractStatus.IN_PROGRESS);
        // Risque d'oublier des transitions interdites
    }
}
```

**✅ Avec State Pattern:**
```java
public enum ContractStatus {
    PENDING {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.of(IN_PROGRESS, CANCELLED);
        }
    },
    // Chaque état définit ses transitions autorisées
}

// Usage sécurisé
public void start() {
    this.status = this.status.transitionTo(IN_PROGRESS);
    // Exception automatique si transition invalide
}
```

**Avantages:**

1. **Sécurité:** Impossible de faire une transition invalide
2. **Centralisation:** Toute la logique d'état dans l'enum
3. **Documentation:** Les transitions sont explicites
4. **Tests exhaustifs:**
   ```java
   @Test
   void shouldRejectTransitionFromCompletedToCancelled() {
       Contract contract = new Contract(..., COMPLETED);
       assertThrows(TransitionNotAllowedException.class, 
           () -> contract.cancel());
   }
   ```

---

#### Q6: Pourquoi un enum et pas des classes séparées pour chaque état ?

**Réponse:**

**Pattern State classique (GoF):**
```java
interface ContractState {
    ContractState start();
    ContractState cancel();
}

class PendingState implements ContractState { ... }
class InProgressState implements ContractState { ... }
// 5 classes séparées
```

**Notre choix (enum):**
```java
public enum ContractStatus {
    PENDING, IN_PROGRESS, LATE, COMPLETED, CANCELLED
}
```

**Justification:**

✅ **Simplicité:**
- Pas de hiérarchie de classes complexe
- Toutes les transitions visibles en un coup d'œil
- Moins de fichiers à maintenir

✅ **États simples:**
- Pas de logique métier complexe par état
- Juste des transitions autorisées
- Pas besoin de polymorphisme avancé

✅ **Type-safe:**
- Enum natif Java (exhaustivité des switch)
- Impossible d'instancier un état invalide

**Quand utiliser des classes ?**
- Chaque état a une logique métier complexe
- Comportements très différents par état
- Besoin de sous-états

---

### 🏛️ Repository Pattern

#### Q7: Pourquoi créer une interface Repository dans business/ alors que Spring Data JPA existe déjà ?

**Réponse:**

**Architecture sans Repository Pattern:**
```java
// Service dépend directement de JPA
@Service
public class ClientService {
    private final ClientJpaRepository jpaRepository; // Couplage JPA
    
    public Client create(...) {
        ClientEntity entity = new ClientEntity(); // Dépendance @Entity
        ClientEntity saved = jpaRepository.save(entity);
        return convertToClient(saved);
    }
}
```

**Problèmes:**
- ❌ Logique métier couplée à JPA
- ❌ Impossible de tester sans base de données
- ❌ Migration vers autre ORM difficile

**Avec Repository Pattern:**
```java
// Interface métier (business/)
public interface ClientRepository {
    Client save(Client client);  // ← Modèle domaine, pas Entity
    Optional<Client> findById(UUID id);
}

// Service dépend de l'interface
@Service
public class ClientService {
    private final ClientRepository clientRepository; // ← Abstraction
    
    public Client create(...) {
        Client client = new Client(...); // ← POJO pur
        return clientRepository.save(client);
    }
}

// Implémentation JPA (infrastructure/)
@Component
public class ClientRepositoryImpl implements ClientRepository {
    private final ClientJpaRepository jpaRepository;
    
    @Override
    public Client save(Client client) {
        ClientEntity entity = toEntity(client);
        ClientEntity saved = jpaRepository.save(entity);
        return toDomain(saved); // Conversion ici
    }
}
```

**Avantages:**

1. **Testabilité:**
   ```java
   @Test
   void shouldCreateClient() {
       ClientRepository mockRepo = mock(ClientRepository.class);
       ClientService service = new ClientService(mockRepo);
       // Test sans DB, sans Spring
   }
   ```

2. **Dependency Inversion (SOLID):**
   - Service dépend d'une abstraction
   - Pas de dépendance à l'implémentation JPA

3. **Flexibilité:**
   - Changement MongoDB → PostgreSQL transparent
   - Ajout d'un cache sans modifier le service

4. **Domain-Driven Design:**
   - Repository parle le langage métier
   - `findByLicenseNumber()` vs `findByLicenseNumberEquals()`

---

#### Q8: Pourquoi convertir entre Domain Model, Entity et DTO ? N'est-ce pas du code dupliqué ?

**Réponse:**

**3 représentations différentes pour 3 objectifs différents:**

```
1. DTO (interfaces/rest/dto/)
   ↓ Validation API, exposition contrôlée
   
2. Domain Model (business/model/)
   ↓ Logique métier pure
   
3. Entity (infrastructure/persistence/)
   ↓ Mapping base de données
```

**Exemple concret:**

```java
// 1. DTO - Exposition API
public record ClientDto(
    UUID id,
    String fullName,        // ← Concaténation prénom + nom
    String license,
    int age                 // ← Calculé à partir de birthDate
) {}

// 2. Domain Model - Métier
public class Client {
    private String firstName;    // ← Séparés
    private String lastName;
    private LocalDate birthDate; // ← Date brute
    private String licenseNumber;
    
    // Logique métier
    public boolean isAdult() {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }
}

// 3. Entity - Persistance
@Entity
@Table(name = "clients")
public class ClientEntity {
    @Id
    private UUID id;
    
    @Column(name = "first_name", length = 50)
    private String firstName;
    
    @Column(name = "license_number", unique = true)
    private String licenseNumber;
    
    // Annotations JPA/Hibernate
}
```

**Avantages:**

1. **Évolution indépendante:**
   - Changer l'API sans toucher la BD
   - Refactorer le domaine sans casser l'API
   - Migration BD sans impact métier

2. **Sécurité:**
   ```java
   // DTO expose seulement ce qui doit être public
   public record ClientDto(
       UUID id,
       String fullName  // Pas de password, pas de données sensibles
   ) {}
   ```

3. **Validation par couche:**
   - DTO : `@NotNull`, `@Size`, `@Pattern`
   - Domain : Règles métier (unicité permis)
   - Entity : Contraintes BD (`@Column`, `@UniqueConstraint`)

**Coût:**
- ⚠️ Code de mapping (réduit avec MapStruct)
- ✅ Flexibilité et découplage en retour

---

### 📦 Value Object

#### Q9: Quelle est la différence entre une classe normale et un Value Object ?

**Réponse:**

**Classe normale (Entity):**
```java
public class Client {
    private UUID id; // ← Identité
    private String firstName;
    
    // Deux clients avec même nom mais ID différent ≠ égaux
    @Override
    public boolean equals(Object o) {
        return this.id.equals(((Client) o).id);
    }
}
```

**Value Object:**
```java
public record Period(LocalDate startDate, LocalDate endDate) {
    // Pas d'ID, égalité basée sur les valeurs
    // Period(2025-12-01, 2025-12-10) == Period(2025-12-01, 2025-12-10)
    
    public boolean overlapsWith(Period other) {
        return !this.endDate.isBefore(other.startDate) 
            && !other.endDate.isBefore(this.startDate);
    }
}
```

**Caractéristiques Value Object:**

1. **Pas d'identité:** Égalité par valeur
2. **Immutable:** Pas de setters
3. **Logique métier encapsulée:**
   ```java
   // ❌ Logique éparpillée
   if (contract.getEndDate().isBefore(otherContract.getStartDate())) { ... }
   
   // ✅ Logique dans le Value Object
   if (contract.getPeriod().overlapsWith(otherContract.getPeriod())) { ... }
   ```

4. **Validation à la construction:**
   ```java
   public Period {
       if (!startDate.isBefore(endDate)) {
           throw new IllegalArgumentException("Invalid period");
       }
   }
   
   // Impossible d'avoir un Period invalide
   ```

**Exemples courants:**
- `Money` : amount + currency
- `Address` : street + city + zipCode
- `Email` : avec validation format
- `Period` : startDate + endDate

---

### ✅ Validation

#### Q10: Pourquoi deux niveaux de validation (Bean Validation + Validation métier) ?

**Réponse:**

**Deux types de règles différentes:**

**1. Bean Validation (JSR 380) - Couche API:**
```java
public record CreateClientRequest(
    @NotNull(message = "First name required")
    @Size(min = 2, max = 50)
    String firstName,
    
    @Pattern(regexp = "^[A-Z0-9]{8,12}$")
    String licenseNumber
) {}
```

**Rôle:**
- ✅ Validation **syntaxique** (format, longueur, pattern)
- ✅ Vérification **avant** d'appeler le service
- ✅ Évite les appels inutiles avec données invalides

**2. Validation métier - Couche Service:**
```java
@Service
public class ClientService {
    public Client create(...) {
        // Validation métier (nécessite la BD)
        if (clientRepository.existsByLicenseNumber(licenseNumber)) {
            throw new DuplicateLicenseException(...);
        }
    }
}
```

**Rôle:**
- ✅ Validation **sémantique** (unicité, cohérence métier)
- ✅ Nécessite accès aux données existantes
- ✅ Règles métier complexes

**Séparation nécessaire:**

| Bean Validation | Validation Métier |
|----------------|-------------------|
| Format email valide | Email déjà utilisé |
| Date non nulle | Date dans le futur |
| Longueur 2-50 caractères | Client mineur (<18 ans) |
| Pattern regex | Permis suspendu |

**Pourquoi ne pas tout mettre dans le service ?**
- ❌ Appels service inutiles avec données mal formatées
- ❌ Pas de feedback immédiat (avant sérialisation)
- ❌ Couplage validation/logique métier

---

### 🔌 Injection de Dépendances

#### Q11: Pourquoi l'injection par constructeur plutôt que @Autowired sur les champs ?

**Réponse:**

**❌ Injection par champ:**
```java
@Service
public class ContractService {
    @Autowired
    private ContractRepository contractRepository;
    
    @Autowired
    private ValidationChain validationChain;
    
    // Pas de constructeur visible
}
```

**Problèmes:**
1. **Pas immutable:** Champs modifiables après construction
2. **Tests difficiles:** 
   ```java
   ContractService service = new ContractService();
   // Comment injecter les mocks ? Reflection !
   ```
3. **Dépendances cachées:** Constructeur par défaut ne montre rien
4. **Dépendances circulaires silencieuses**

**✅ Injection par constructeur:**
```java
@Service
public class ContractService {
    private final ContractRepository contractRepository;
    private final ValidationChain validationChain;

    public ContractService(
            ContractRepository contractRepository,
            ValidationChain validationChain) {
        this.contractRepository = contractRepository;
        this.validationChain = validationChain;
    }
}
```

**Avantages:**

1. **Immutabilité (thread-safe):**
   ```java
   private final ContractRepository repo; // ← final = immutable
   ```

2. **Tests simples:**
   ```java
   @Test
   void shouldCreateContract() {
       ContractRepository mockRepo = mock(ContractRepository.class);
       ValidationChain mockChain = mock(ValidationChain.class);
       
       ContractService service = new ContractService(mockRepo, mockChain);
       // Pas besoin de Spring pour les tests
   }
   ```

3. **Dépendances explicites:**
   - Constructeur montre toutes les dépendances
   - Code autodocumenté

4. **Détection erreurs au démarrage:**
   ```java
   // Dépendance circulaire détectée immédiatement
   // A → B → C → A
   // Exception au démarrage de Spring
   ```

**Best Practice Spring officielle:** Constructor Injection

---

### 🚫 Anti-Patterns

#### Q12: Pourquoi votre domaine est "anémique" alors que c'est souvent considéré comme un anti-pattern ?

**Réponse:**

**Anemic Domain Model - Anti-pattern DDD:**

Un modèle "anémique" a des objets sans logique, juste des getters/setters:

```java
// ❌ Modèle complètement anémique
public class Contract {
    private UUID id;
    private ContractStatus status;
    
    // Juste getters/setters, pas de logique
    public UUID getId() { return id; }
    public void setStatus(ContractStatus status) { this.status = status; }
}

// ❌ Toute la logique dans le service
@Service
public class ContractService {
    public void startContract(UUID id) {
        Contract contract = find(id);
        if (contract.getStatus() == PENDING) {
            contract.setStatus(IN_PROGRESS);
        }
    }
}
```

**Notre approche - Anemic partiel mais assumé:**

```java
// ✅ Modèle avec logique d'état
public class Contract {
    private ContractStatus status;
    
    // Logique métier critique encapsulée
    public void start() {
        this.status = this.status.transitionTo(IN_PROGRESS);
    }
    
    public void cancel() {
        this.status = this.status.transitionTo(CANCELLED);
    }
}

// ✅ Service orchestrateur (plus léger)
@Service
public class ContractService {
    public Contract start(UUID id) {
        Contract contract = findById(id);
        contract.start(); // ← Délégation au modèle
        return contractRepository.save(contract);
    }
}
```

**Justification:**

1. **Architecture 3-tiers pragmatique:**
   - Services coordonnent les opérations
   - Modèles gèrent leur état interne
   - Équilibre entre DDD pur et pragmatisme

2. **Logique métier critique dans le modèle:**
   - Transitions d'état → `Contract.start()`
   - Validation période → `Period.overlapsWith()`
   - Calculs métier → `Period.durationInDays()`

3. **Logique de coordination dans les services:**
   - Validation multi-entités (client + véhicule + contrat)
   - Orchestration transactions
   - Appels inter-services

**Quand utiliser Rich Domain Model ?**
- DDD strict avec bounded contexts
- Logique métier très complexe par entité
- Event Sourcing / CQRS

**Notre contexte:**
- Application CRUD avec règles métier modérées
- TDD avec tests unitaires rapides
- Équilibre maintenabilité/complexité

---

#### Q13: Vous n'utilisez pas CQRS, pourquoi ?

**Réponse:**

**CQRS (Command Query Responsibility Segregation):**

Séparer les modèles de lecture et d'écriture:

```java
// Modèle Command (écriture)
public class CreateContractCommand {
    private UUID clientId;
    private UUID vehicleId;
    // ...
}

// Modèle Query (lecture)
public class ContractView {
    private UUID id;
    private String clientName;  // Dénormalisé
    private String vehiclePlate;
    // Optimisé pour la lecture
}

// Deux bases de données distinctes
```

**Pourquoi nous ne l'utilisons pas:**

❌ **Complexité non justifiée:**
- Application à charge modérée
- Pas de différence lecture/écriture extrême
- Pas besoin de scalabilité séparée

❌ **Overhead développement:**
- Double modélisation (command + query)
- Synchronisation entre les modèles
- Équipe de 4 développeurs

✅ **Notre contexte:**
- Ratio lecture/écriture équilibré
- Requêtes simples (pas d'agrégations complexes)
- Base unique PostgreSQL suffit

**Quand utiliser CQRS ?**
- 1000+ lectures pour 1 écriture
- Requêtes analytiques complexes
- Besoin de scalabilité indépendante
- Event Sourcing

**Alternative choisie:**
- Repository Pattern simple
- Pagination avec Spring Data
- Cache niveau service si besoin

---

### 🎯 Questions Transversales

#### Q14: Comment ces patterns facilitent-ils les tests ?

**Réponse:**

**1. Tests unitaires sans Spring (rapides):**

```java
// Test d'un validateur isolé
@Test
void shouldRejectOverlappingContracts() {
    // Arrange
    ContractRepository mockRepo = mock(ContractRepository.class);
    when(mockRepo.findOverlappingContracts(...))
        .thenReturn(List.of(existingContract));
    
    OverlapValidator validator = new OverlapValidator(mockRepo);
    
    // Act & Assert
    assertThrows(OverlapException.class, 
        () -> validator.validate(context));
}
// ✅ Pas de @SpringBootTest, pas de BD, exécution <10ms
```

**2. Mock des interfaces (Dependency Inversion):**

```java
@Test
void shouldCreateContract() {
    // Mock des dépendances
    ContractRepository mockRepo = mock(ContractRepository.class);
    ValidationChain mockChain = mock(ValidationChain.class);
    
    // Service avec dépendances mockées
    ContractService service = new ContractService(mockRepo, mockChain);
    
    // Test du comportement
    Contract result = service.create(clientId, vehicleId, start, end);
    
    verify(mockChain).validateAll(any());
    verify(mockRepo).save(any());
}
```

**3. Tests de composants indépendants (SRP):**

Chaque pattern = composant testable isolément:

- ✅ Test `DateValidator` sans les autres validateurs
- ✅ Test `ContractStatus` transitions sans service
- ✅ Test `Period.overlapsWith()` sans base de données

**4. Tests d'intégration ciblés:**

```java
@SpringBootTest
@AutoConfigureTestDatabase
class ContractControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateContractEndToEnd() {
        // Test complet avec base H2
        mockMvc.perform(post("/api/contracts")
            .content(jsonRequest))
            .andExpect(status().isCreated());
    }
}
```

**Pyramide de tests respectée:**
```
        /\
       /  \  E2E (Integration)
      /────\
     /      \ Tests Services (Mocks)
    /────────\
   /          \ Tests Unitaires (Rapides)
  /────────────\
```

**Résultat:** 24/24 tests passent en <5 secondes

---

#### Q15: Si vous deviez ajouter une nouvelle règle métier demain, comment feriez-vous ?

**Réponse:**

**Scénario:** *"Un client ne peut pas louer plus de 3 véhicules simultanément"*

**Étapes (TDD):**

**1. RED - Écrire le test:**
```java
@Test
void shouldRejectContractWhenClientHas3ActiveRentals() {
    // Arrange
    UUID clientId = UUID.randomUUID();
    when(contractRepository.countActiveContractsByClient(clientId))
        .thenReturn(3);
    
    MaxRentalsValidator validator = new MaxRentalsValidator(contractRepository);
    ContractCreationContext context = new ContractCreationContext(
        clientId, vehicleId, startDate, endDate
    );
    
    // Act & Assert
    assertThrows(MaxRentalsExceededException.class,
        () -> validator.validate(context));
}
```

**2. GREEN - Implémenter le validateur:**
```java
@Component
public class MaxRentalsValidator implements ContractValidator {
    private static final int MAX_ACTIVE_RENTALS = 3;
    private final ContractRepository contractRepository;

    @Override
    public void validate(ContractCreationContext context) {
        long activeRentals = contractRepository
            .countActiveContractsByClient(context.clientId());
        
        if (activeRentals >= MAX_ACTIVE_RENTALS) {
            throw new MaxRentalsExceededException(
                String.format("Client %s already has %d active rentals. Maximum is %d.",
                    context.clientId(), activeRentals, MAX_ACTIVE_RENTALS)
            );
        }
    }
}
```

**3. REFACTOR - Ajouter à la chaîne:**
```java
@Component
public class ContractValidationChain {
    private final List<ContractValidator> validators;

    public ContractValidationChain(
            DateValidator dateValidator,
            ClientExistenceValidator clientValidator,
            VehicleAvailabilityValidator vehicleValidator,
            OverlapValidator overlapValidator,
            MaxRentalsValidator maxRentalsValidator) { // ← Nouvelle dépendance
        this.validators = List.of(
            dateValidator,
            clientValidator,
            vehicleValidator,
            overlapValidator,
            maxRentalsValidator // ← Ajout dans la chaîne
        );
    }
}
```

**4. Ajouter la méthode repository:**
```java
public interface ContractRepository {
    // ... méthodes existantes
    
    long countActiveContractsByClient(UUID clientId); // ← Nouvelle signature
}

@Component
public class ContractRepositoryImpl implements ContractRepository {
    @Override
    public long countActiveContractsByClient(UUID clientId) {
        return jpaRepository.countByClientIdAndStatusIn(
            clientId, 
            List.of(ContractStatus.PENDING, ContractStatus.IN_PROGRESS)
        );
    }
}
```

**Modifications nécessaires:**
- ✅ 1 nouvelle classe (`MaxRentalsValidator`)
- ✅ 1 test unitaire
- ✅ 1 ligne dans `ContractValidationChain`
- ✅ 1 méthode dans `ContractRepository`

**Pas de modification:**
- ✅ `ContractService` inchangé
- ✅ `ContractController` inchangé
- ✅ Autres validateurs inchangés

**Avantages Chain of Responsibility:**
- Open/Closed Principle respecté
- Ajout sans casser l'existant
- Tests isolés

---

#### Q16: Quels sont les compromis (trade-offs) de votre architecture ?

**Réponse:**

**Avantages:**

✅ **Maintenabilité:**
- Code organisé et prévisible
- Chaque couche a un rôle clair
- Facile pour nouveaux développeurs

✅ **Testabilité:**
- 24/24 tests passent
- Tests unitaires rapides (<5s)
- Couverture >80%

✅ **Extensibilité:**
- Ajout validateurs sans modification service
- Nouveaux endpoints sans toucher métier

**Inconvénients assumés:**

⚠️ **Verbosité:**
- 3 représentations (DTO/Domain/Entity)
- Code de mapping (réduit avec MapStruct)
- Plus de fichiers qu'un monolithe "simple"

⚠️ **Over-engineering potentiel:**
- Patterns parfois "lourds" pour CRUD simple
- Chain of Responsibility pour 4 validations
- Justifié par l'apprentissage et l'évolutivité

⚠️ **Performance:**
- Conversions DTO↔Domain↔Entity (coût négligeable)
- Pas de cache (volontairement simplifié)
- Acceptable pour notre charge

**Quand cette architecture est justifiée:**
- ✅ Application évolutive (nouvelles règles métier)
- ✅ Équipe >3 développeurs
- ✅ Tests automatisés obligatoires
- ✅ Maintenabilité long terme

**Quand elle est excessive:**
- ❌ Prototype jetable
- ❌ Application ultra-simple (5 endpoints CRUD)
- ❌ Équipe 1 personne court terme

**Notre conclusion:**
Balance pragmatique entre patterns académiques et réalité projet étudiant.

---

<div align="center">

**Projet BFB Architecture**  
*Équipe: Saad, Vulzyun, Mohamedlam, Xaymaa*

</div>
