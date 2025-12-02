# Guide Pédagogique 5 : Évolution du Stack Technique

> **Objectif** : Comprendre les choix technologiques et leur évolution dans le projet BFB

---

## 📦 Stack Technique Final

```
┌─────────────────────────────────────────────┐
│           SPRING BOOT 3.5.7                 │
│              (Java 17)                      │
└─────────────────────────────────────────────┘
         │
         ├── Spring Web (REST API)
         ├── Spring Data JPA (Persistence)
         ├── H2 Database (Runtime)
         ├── Flyway (Migrations)
         ├── MapStruct 1.5.5 (Mapping)
         ├── Bean Validation (Annotations)
         ├── Springdoc OpenAPI 2.7.0 (Documentation)
         └── Spring Boot Test + Mockito (Tests)
```

---

## 🗺️ MapStruct : Adieu Mapping Manuel !

### Le Problème : Boilerplate de Mapping

#### Avant MapStruct (Mapping Manuel)

```java
// interfaces/rest/contract/dto/ContractMapper.java (version manuelle)
public class ContractMapper {
    
    public static ContractDto toDto(Contract contract) {
        if (contract == null) {
            return null;
        }
        
        ContractDto dto = new ContractDto();
        dto.setId(contract.getId());
        dto.setClientId(contract.getClientId());
        dto.setVehicleId(contract.getVehicleId());
        dto.setStartDate(contract.getPeriod().startDate());
        dto.setEndDate(contract.getPeriod().endDate());
        dto.setStatus(contract.getStatus().name());
        dto.setCreatedAt(contract.getCreatedAt());
        dto.setUpdatedAt(contract.getUpdatedAt());
        
        return dto;
    }
    
    public static Contract toDomain(CreateContractRequest request) {
        if (request == null) {
            return null;
        }
        
        Period period = new Period(request.startDate(), request.endDate());
        
        return new Contract(
            request.clientId(),
            request.vehicleId(),
            period,
            ContractStatus.PENDING
        );
    }
    
    public static List<ContractDto> toDtoList(List<Contract> contracts) {
        if (contracts == null) {
            return null;
        }
        
        return contracts.stream()
            .map(ContractMapper::toDto)
            .collect(Collectors.toList());
    }
}
```

**Problèmes** :
- 📝 50+ lignes de code répétitif par mapper
- 🐛 Erreurs manuelles (oubli d'un champ)
- 🔧 Maintenance difficile (ajouter un champ = modifier 3 endroits)
- ⚡ Performances : OK mais verbose

### Solution : MapStruct (Commit `10e7caa`, 30 Nov)

#### Configuration Maven

```xml
<!-- pom.xml -->
<properties>
    <java.version>17</java.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
    <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</properties>

<dependencies>
    <!-- MapStruct dependency -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <!-- MapStruct annotation processor -->
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### Après MapStruct (Interface Déclarative)

```java
// interfaces/rest/contract/mapper/ContractMapper.java (version MapStruct)
@Mapper(componentModel = "spring")
public interface ContractMapper {
    
    // Mapping Entity → DTO
    @Mapping(source = "period.startDate", target = "startDate")
    @Mapping(source = "period.endDate", target = "endDate")
    @Mapping(source = "status", target = "status")
    ContractDto toDto(Contract contract);
    
    // Mapping Request → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Contract toDomain(CreateContractRequest request);
    
    // Mapping automatique List<Entity> → List<DTO>
    List<ContractDto> toDtoList(List<Contract> contracts);
    
    // Mapping automatique Page<Entity> → Page<DTO>
    default Page<ContractDto> toDtoPage(Page<Contract> contractPage) {
        return contractPage.map(this::toDto);
    }
}
```

**Ce qui se passe** :
1. ✨ MapStruct **génère l'implémentation** à la compilation
2. 🚀 Code généré = **performances optimales** (pas de réflexion)
3. ✅ **Type-safe** : erreurs détectées à la compilation
4. 📦 Spring l'injecte comme un bean normal

#### Code Généré (par MapStruct)

```java
// target/generated-sources/annotations/.../ContractMapperImpl.java
@Component
public class ContractMapperImpl implements ContractMapper {
    
    @Override
    public ContractDto toDto(Contract contract) {
        if (contract == null) {
            return null;
        }
        
        ContractDto contractDto = new ContractDto();
        
        contractDto.setStartDate(contractPeriodStartDate(contract));
        contractDto.setEndDate(contractPeriodEndDate(contract));
        contractDto.setId(contract.getId());
        contractDto.setClientId(contract.getClientId());
        contractDto.setVehicleId(contract.getVehicleId());
        // ... reste du mapping
        
        return contractDto;
    }
    
    // Méthodes helper générées
    private LocalDate contractPeriodStartDate(Contract contract) {
        if (contract == null) {
            return null;
        }
        Period period = contract.getPeriod();
        if (period == null) {
            return null;
        }
        return period.startDate();
    }
    
    // ... autres méthodes
}
```

### Comparaison Avant/Après

| Aspect | Manuel | MapStruct |
|--------|--------|-----------|
| **Lignes de code** | 50+ | 10 |
| **Maintenance** | Manuelle | Auto |
| **Type safety** | Runtime | Compile-time |
| **Performance** | Bonne | Optimale |
| **Erreurs** | Faciles | Détectées à la compilation |
| **Boilerplate** | 100% | 0% |

### Cas Complexe : Mapping avec Logique Métier

```java
@Mapper(componentModel = "spring")
public interface ContractMapper {
    
    // Mapping avec expression Java
    @Mapping(target = "daysRemaining", 
             expression = "java(calculateDaysRemaining(contract))")
    ContractDto toDto(Contract contract);
    
    // Méthode Java personnalisée
    default int calculateDaysRemaining(Contract contract) {
        if (contract.getStatus() == ContractStatus.COMPLETED) {
            return 0;
        }
        
        LocalDate now = LocalDate.now();
        LocalDate endDate = contract.getPeriod().endDate();
        
        if (now.isAfter(endDate)) {
            return 0; // Déjà terminé
        }
        
        return (int) ChronoUnit.DAYS.between(now, endDate);
    }
}
```

---

## 🚀 Flyway : Migrations de Base de Données

### Le Problème : Évolution du Schéma

#### Sans Flyway (Dangereux !)

```java
// application.yml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # ⚠️ Hibernate modifie le schéma automatiquement
```

**Problèmes** :
- 🎲 Non déterministe (Hibernate devine les changements)
- 🐛 Perte de données possible
- 🔍 Pas d'historique des changements
- 🚫 Impossible de revenir en arrière
- 🔥 En production = CATASTROPHE

**Exemple catastrophe** :
```java
// V1 : Colonne "email" NOT NULL
@Column(nullable = false)
private String email;

// V2 : On rend "email" nullable
@Column(nullable = true)
private String email;

// Hibernate avec ddl-auto=update :
// ❌ Ne modifie PAS la contrainte NOT NULL !
// → Incohérence schéma/code
```

### Solution : Flyway (Commit `9f99ced`, 30 Nov)

#### Configuration

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    
  jpa:
    hibernate:
      ddl-auto: validate  # ← Hibernate ne modifie RIEN, juste valide
```

#### Structure des Migrations

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__initial_schema.sql
        ├── V2__add_audit_columns.sql
        └── V3__add_indexes.sql
```

#### Migration V1 : Schéma Initial

```sql
-- V1__initial_schema.sql
CREATE TABLE IF NOT EXISTS clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    registration_number VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

-- Indexes pour performance
CREATE INDEX idx_contracts_vehicle_dates 
    ON contracts(vehicle_id, start_date, end_date);

CREATE INDEX idx_contracts_status 
    ON contracts(status);

-- Données de test
INSERT INTO clients (name, email, phone) VALUES
    ('John Doe', 'john.doe@example.com', '+33612345678'),
    ('Jane Smith', 'jane.smith@example.com', '+33698765432');

INSERT INTO vehicles (brand, model, registration_number, status) VALUES
    ('Renault', 'Clio', 'AA-123-BB', 'AVAILABLE'),
    ('Peugeot', '208', 'CC-456-DD', 'AVAILABLE'),
    ('Citroën', 'C3', 'EE-789-FF', 'MAINTENANCE');
```

#### Migration V2 : Colonnes d'Audit

```sql
-- V2__add_audit_columns.sql
-- Ajouter colonnes d'audit si elles n'existent pas déjà

ALTER TABLE clients 
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

ALTER TABLE vehicles 
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);

ALTER TABLE contracts 
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(100),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(100);
```

#### Migration V3 : Optimisations

```sql
-- V3__add_indexes.sql
-- Indexes pour queries fréquentes

-- Index pour recherche par email
CREATE INDEX IF NOT EXISTS idx_clients_email 
    ON clients(email);

-- Index composite pour disponibilité véhicule
CREATE INDEX IF NOT EXISTS idx_vehicles_status_brand 
    ON vehicles(status, brand);

-- Index pour contrats en retard
CREATE INDEX IF NOT EXISTS idx_contracts_overdue 
    ON contracts(status, end_date)
    WHERE status = 'IN_PROGRESS';
```

### Table Flyway (Générée Automatiquement)

```sql
-- flyway_schema_history (table de tracking)
SELECT * FROM flyway_schema_history;

+----------------+----------+--------------+---------------------+---------------------+
| installed_rank | version  | description  | installed_on        | success             |
+----------------+----------+--------------+---------------------+---------------------+
|              1 | 1        | initial schema| 2025-11-30 10:00:00| true                |
|              2 | 2        | audit columns | 2025-11-30 14:30:00| true                |
|              3 | 3        | indexes      | 2025-12-01 09:15:00| true                |
+----------------+----------+--------------+---------------------+---------------------+
```

### Workflow Flyway

```
1. Au démarrage de l'application :
   ├── Flyway vérifie flyway_schema_history
   ├── Compare avec migrations dans db/migration/
   ├── Exécute UNIQUEMENT les nouvelles migrations
   └── Met à jour flyway_schema_history

2. Si migration échoue :
   ├── Rollback automatique (transaction)
   ├── Application refuse de démarrer
   └── Erreur claire dans les logs

3. En production :
   ├── Même processus
   ├── Migrations versionnées = traçabilité
   └── Pas de surprise
```

### Conventions de Nommage

```
V{VERSION}__{DESCRIPTION}.sql

V  : Version (obligatoire)
1  : Numéro de version (incrémental)
__ : Deux underscores (séparateur)
description : Description snake_case

Exemples :
✓ V1__initial_schema.sql
✓ V2__add_audit_columns.sql
✓ V3.1__fix_indexes.sql         (version mineure)
✓ V10__migrate_to_postgresql.sql

✗ v1_initial.sql                 (minuscule)
✗ V1_initial_schema.sql          (un seul underscore)
✗ 1__initial_schema.sql          (pas de V)
```

---

## 📋 Bean Validation : Validation Déclarative

### Avant : Validation Manuelle

```java
@PostMapping
public ResponseEntity<ContractDto> create(@RequestBody CreateContractRequest request) {
    // Validations manuelles
    if (request.clientId() == null) {
        throw new ValidationException("Client ID is required");
    }
    if (request.vehicleId() == null) {
        throw new ValidationException("Vehicle ID is required");
    }
    if (request.startDate() == null) {
        throw new ValidationException("Start date is required");
    }
    if (request.endDate() == null) {
        throw new ValidationException("End date is required");
    }
    if (request.startDate().isAfter(request.endDate())) {
        throw new ValidationException("Start date must be before end date");
    }
    
    // Enfin... la logique métier
    Contract contract = contractService.create(request);
    return ResponseEntity.ok(contractMapper.toDto(contract));
}
```

### Après : Bean Validation (Commit `ef4e2a0`, 30 Nov)

```java
// CreateContractRequest.java (DTO)
public record CreateContractRequest(
    
    @NotNull(message = "Client ID is required")
    @Positive(message = "Client ID must be positive")
    Long clientId,
    
    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be positive")
    Long vehicleId,
    
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    LocalDate startDate,
    
    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    LocalDate endDate
) {
    // Validation custom dans le constructeur compact
    public CreateContractRequest {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }
}

// Controller (propre !)
@PostMapping
public ResponseEntity<ContractDto> create(@Valid @RequestBody CreateContractRequest request) {
    //                                   ^^^^
    //                      Spring valide automatiquement !
    
    Contract contract = contractService.create(request);
    return ResponseEntity.ok(contractMapper.toDto(contract));
}
```

### Annotations Standard

```java
public record ClientDto(
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be valid")
    String phone,
    
    @Min(value = 18, message = "Must be at least 18 years old")
    @Max(value = 120, message = "Age must be realistic")
    Integer age
) {}
```

### Validation Custom

```java
// Annotation custom
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdultAgeValidator.class)
public @interface AdultAge {
    String message() default "Must be 18 years or older";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Validateur
public class AdultAgeValidator implements ConstraintValidator<AdultAge, LocalDate> {
    
    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true; // @NotNull gère la nullité
        }
        
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }
}

// Utilisation
public record CreateClientRequest(
    String name,
    String email,
    
    @NotNull
    @AdultAge  // ← Validation custom
    LocalDate birthDate
) {}
```

---

## 📚 Springdoc OpenAPI : Documentation Auto-Générée

### Configuration (Commit `dea5be3`, 4 Nov)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

```yaml
# application.yml
springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operations-sorter: method
    tags-sorter: alpha
```

### Accès Swagger UI

```
http://localhost:8080/swagger-ui.html

→ Interface interactive pour tester l'API !
```

### Annotations OpenAPI

```java
@RestController
@RequestMapping("/api/v1/contracts")
@Tag(name = "Contracts", description = "Contract management API")
public class ContractController extends BaseRestController<Contract, ContractDto> {
    
    @PostMapping
    @Operation(
        summary = "Create a new contract",
        description = "Creates a new rental contract with validation"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Contract created successfully",
            content = @Content(schema = @Schema(implementation = ContractDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Client or vehicle not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Contract overlaps with existing contract"
        )
    })
    public ResponseEntity<ContractDto> create(
        @Valid @RequestBody 
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contract creation request",
            required = true
        )
        CreateContractRequest request
    ) {
        Contract contract = contractService.create(request);
        return created(contractMapper.toDto(contract));
    }
}
```

---

## ⏰ Scheduled Jobs : Automatisation

### Le Besoin

**Problème** : Contrats en retard non détectés automatiquement

```
Client loue véhicule du 1er au 10 décembre
→ 11 décembre : Contrat toujours "IN_PROGRESS"
→ Devrait passer à "LATE" automatiquement !
```

### Solution : @Scheduled (Commit `88a52a8`, 30 Nov)

```java
// ContractScheduledJobs.java
@Component
@EnableScheduling
public class ContractScheduledJobs {
    
    private final ContractService contractService;
    
    /**
     * Détecte les contrats en retard chaque jour à minuit.
     * Cron : seconde minute heure jour mois jour-semaine
     */
    @Scheduled(cron = "${contract.late-detection.cron:0 0 0 * * *}")
    public void detectLateContracts() {
        log.info("Starting late contract detection job");
        
        LocalDate today = LocalDate.now();
        List<Contract> overdueContracts = contractService.findOverdueContracts(today);
        
        int marked = 0;
        for (Contract contract : overdueContracts) {
            try {
                contract.markAsLate();
                contractService.update(contract);
                marked++;
            } catch (Exception e) {
                log.error("Failed to mark contract {} as late", contract.getId(), e);
            }
        }
        
        log.info("Late contract detection completed: {} contracts marked as LATE", marked);
    }
    
    /**
     * Nettoie les contrats complétés depuis plus de 1 an (archivage).
     * Exécuté chaque dimanche à 2h du matin.
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void archiveOldContracts() {
        log.info("Starting old contracts archiving job");
        
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        int archived = contractService.archiveCompletedContractsBefore(oneYearAgo);
        
        log.info("Archiving completed: {} contracts archived", archived);
    }
}
```

### Configuration Cron

```yaml
# application.yml
contract:
  late-detection:
    cron: "0 0 0 * * *"  # Chaque jour à minuit
    enabled: true
  archiving:
    cron: "0 0 2 * * SUN"  # Chaque dimanche à 2h
    enabled: true
```

### Format Cron Expliqué

```
 ┌─────────── seconde (0-59)
 │ ┌───────── minute (0-59)
 │ │ ┌─────── heure (0-23)
 │ │ │ ┌───── jour du mois (1-31)
 │ │ │ │ ┌─── mois (1-12 ou JAN-DEC)
 │ │ │ │ │ ┌─ jour de la semaine (0-7 ou SUN-SAT)
 │ │ │ │ │ │
 * * * * * *

Exemples :
0 0 0 * * *       → Minuit chaque jour
0 0 12 * * *      → Midi chaque jour
0 30 9 * * MON-FRI → 9h30 du lundi au vendredi
0 0 */6 * * *     → Toutes les 6 heures
0 0 0 1 * *       → 1er de chaque mois à minuit
```

---

## ❓ Questions Probables du Tech Lead

### Q1 : "Pourquoi MapStruct et pas un mapper manuel ?"
**Réponse** :

**Métriques** :

| Aspect | Manuel | MapStruct |
|--------|--------|-----------|
| Temps d'écriture | 30 min/mapper | 5 min/mapper |
| Maintenance | Manuelle | Auto-sync |
| Bugs | 2-3 par mapper | 0 (compile-time) |
| Performance | Bonne | Optimale |
| Lisibilité | Verbose | Déclarative |

**ROI** : 3 mappers = 75 min économisées + 0 bugs

### Q2 : "Flyway vs Liquibase ?"
**Réponse** :

**Flyway** :
- ✅ Simple (SQL natif)
- ✅ Facile à apprendre
- ✅ Suffisant pour 90% des projets

**Liquibase** :
- ✅ Format XML/YAML/JSON
- ✅ Rollback automatique
- ❌ Plus complexe
- ❌ Courbe d'apprentissage

**Notre choix** : Flyway (simplicité, SQL natif)

### Q3 : "Scheduled jobs, pourquoi pas Quartz ?"
**Réponse** :

**@Scheduled Spring** :
- ✅ Built-in (0 dépendance)
- ✅ Suffisant pour jobs simples
- ✅ Configuration YAML
- ❌ Pas de persistence des jobs
- ❌ Pas de clustering

**Quartz** :
- ✅ Persistence en BDD
- ✅ Clustering
- ✅ Jobs complexes
- ❌ Dépendance externe
- ❌ Configuration complexe

**Notre cas** : @Scheduled suffit (jobs simples, 1 serveur)

### Q4 : "Bean Validation, c'est pas suffisant pour tout ?"
**Réponse** :

**Non !** Bean Validation = Validation des DONNÉES

**Validations dans BFB** :

1. **Données (Bean Validation)** ✓
   - Format email
   - Champs obligatoires
   - Longueurs min/max

2. **Métier (Custom Validators)** ✓
   - Client existe ?
   - Véhicule disponible ?
   - Pas de chevauchement ?

3. **Business Rules (Domain)** ✓
   - Transitions d'états
   - Calculs de prix
   - Règles de gestion

**Principe** : Validation en couches

### Q5 : "H2 avec Flyway, ça marche en production ?"
**Réponse** :

**H2** : NON en production (données en mémoire perdues)

**Flyway** : OUI en production avec PostgreSQL !

**Stratégie** :
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:bfbdb
  flyway:
    locations: classpath:db/migration/h2

# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://...
  flyway:
    locations: classpath:db/migration/postgresql
```

**Migrations** : Écrire SQL compatible ou versions séparées.
