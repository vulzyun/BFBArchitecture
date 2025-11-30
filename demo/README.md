# BFB Management - MVP Contrats

## 📋 Vue d'ensemble

MVP implémenté en **TDD (Test-Driven Development)** pour la gestion des contrats de location de véhicules avec architecture hexagonale (Ports & Adapters).

### 🎯 Objectifs
- Gestion complète du cycle de vie des contrats
- Règles métier strictes (chevauchements, transitions d'état, disponibilité)
- Architecture découplée prête pour l'intégration avec les domaines Véhicules et Clients

---

## 🏗️ Architecture

### Couches

```
com.BFBManagement/
├── architecture.contrats.domain/    # Domain layer (Entities, Repository, Règles pures)
│   ├── Contrat.java               # Entity JPA
│   ├── ContratRepository.java      # Spring Data JPA
│   ├── EtatContrat.java           # Enum états
│   └── Rules.java                 # Règles métier pures (stateless)
│
├── business.contrats/              # Business layer (Services, Ports, Adapters)
│   ├── ContratService.java        # Service métier principal
│   ├── ports/                     # Interfaces de découplage
│   │   ├── VehicleStatusPort.java
│   │   └── ClientExistencePort.java
│   ├── adapters/                  # Implémentations stub (temporaires)
│   │   ├── InMemoryVehicleStatusAdapter.java
│   │   └── InMemoryClientAdapter.java
│   └── exceptions/                # Exceptions métier
│       ├── ValidationException.java
│       ├── OverlapException.java
│       ├── VehicleUnavailableException.java
│       ├── ClientUnknownException.java
│       ├── TransitionNotAllowedException.java
│       └── ContratNotFoundException.java
│
└── presentation.contrats/          # Presentation layer (REST API, DTOs)
    ├── ContratController.java      # REST Controller
    ├── CreateContratDto.java       # DTO création
    ├── ContratDto.java            # DTO réponse
    ├── ContratMapper.java         # Entity <-> DTO
    └── GlobalExceptionHandler.java # Gestion erreurs HTTP
```

### États et Transitions

```
EN_ATTENTE  →  EN_COURS  →  TERMINE
    ↓            ↓
  ANNULE     EN_RETARD  →  TERMINE
```

**Transitions autorisées :**
- `EN_ATTENTE` → `EN_COURS`, `ANNULE`
- `EN_COURS` → `TERMINE`, `EN_RETARD`
- `EN_RETARD` → `TERMINE`
- `TERMINE`, `ANNULE` : états terminaux (aucune sortie)

---

## 🚀 Démarrage rapide

### Prérequis
- Java 17+
- Maven 3.8+ (ou utiliser le wrapper `mvnw`)

### Lancer l'application

```powershell
# Windows
cd demo
.\mvnw.cmd spring-boot:run

# Linux/Mac
cd demo
./mvnw spring-boot:run
```

L'application démarre sur **http://localhost:8080**

### Accès aux interfaces

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **H2 Console** : http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:bfbdb`
  - Username: `sa`
  - Password: _(vide)_

---

## 🧪 Tests

### Lancer tous les tests

```powershell
.\mvnw.cmd test
```

### Couverture des tests

✅ **24 tests au total** :
- **8 tests** : `RulesTest` - Règles métier pures (noOverlap, transitionAllowed)
- **15 tests** : `ContratServiceTest` - Service métier avec mocks
- **1 test** : Intégration Spring Boot

---

## 📡 API REST

### Endpoints principaux

#### 📝 Créer un contrat
```http
POST /api/contrats
Content-Type: application/json

{
  "clientId": "550e8400-e29b-41d4-a716-446655440000",
  "vehiculeId": "650e8400-e29b-41d4-a716-446655440001",
  "dateDebut": "2025-12-01",
  "dateFin": "2025-12-10"
}
```

#### 🔍 Récupérer un contrat
```http
GET /api/contrats/{id}
```

#### 🔎 Rechercher des contrats
```http
GET /api/contrats?clientId={uuid}&vehiculeId={uuid}&etat=EN_COURS
```

#### ▶️ Démarrer / ✅ Terminer / ❌ Annuler
```http
PATCH /api/contrats/{id}/start
PATCH /api/contrats/{id}/terminate
PATCH /api/contrats/{id}/cancel
```

#### ⏰ Job : marquer les retards
```http
POST /api/contrats/jobs/mark-late
```

---

## ⏰ Scheduled Jobs

### Automatic Late Contract Detection

The application includes an **automatic scheduler** that marks contracts as LATE when they exceed their end date.

**Schedule:** Every day at 2:00 AM (configurable)

**Configuration** (application.yml):
```yaml
bfb:
  scheduling:
    mark-late-job:
      enabled: true                    # Enable/disable the job
      cron: "0 0 2 * * ?"              # Cron expression (default: 2:00 AM daily)
```

**Cron Expression Examples:**
- `"0 0 2 * * ?"` - Every day at 2:00 AM
- `"0 */30 * * * ?"` - Every 30 minutes
- `"0 0 */6 * * ?"` - Every 6 hours
- `"0 0 0 * * ?"` - Every day at midnight

**To disable the scheduler:**
```yaml
bfb:
  scheduling:
    mark-late-job:
      enabled: false
```

**Manual trigger** is still available via REST API:
```http
POST /api/contracts/jobs/mark-late
```

---

## 🔒 Règles métier

### 1. Validation des dates
- `dateDebut` **strictement** < `dateFin`

### 2. Chevauchement (Overlap)
Deux contrats "occupants" (`EN_ATTENTE`, `EN_COURS`, `EN_RETARD`) sur le même véhicule ne peuvent pas se chevaucher.

### 3. Disponibilité véhicule
Véhicule `EN_PANNE` → refus (409)

### 4. Existence client
Client inexistant → refus (409)

### 5. Transitions d'état
Toute transition invalide → 422 Unprocessable Entity

---

## 🚨 Codes HTTP

| Code | Signification | Cas d'usage |
|------|---------------|-------------|
| **201** | Created | Contrat créé |
| **400** | Bad Request | Données invalides |
| **404** | Not Found | Contrat introuvable |
| **409** | Conflict | Chevauchement, véhicule indisponible |
| **422** | Unprocessable Entity | Transition interdite |

---

## 🎓 Approche TDD suivie

### Cycle Red-Green-Refactor

1. **RED** : Écrire les tests **AVANT** l'implémentation
2. **GREEN** : Implémenter le minimum pour faire passer les tests
3. **REFACTOR** : Nettoyer le code

### Ordre d'implémentation

1. ✅ Tests règles pures (`Rules`)
2. ✅ Tests service métier (avec mocks)
3. ✅ Implémentation domaine + service
4. ✅ Controller + DTOs
5. ✅ Configuration Spring
6. ✅ **24/24 tests passent** ✨

---

## 🛠️ Stack technique

- Java 17
- Spring Boot 3.5.7
- Spring Data JPA
- Flyway (Database Migrations)
- H2 Database (dev)
- Springdoc OpenAPI 2.3.0
- JUnit 5 + Mockito + AssertJ

---

## 🗃️ Database Migrations

The application uses **Flyway** for version-controlled database schema management, ensuring consistent and reproducible database states across all environments.

### Migration Files

Located in `src/main/resources/db/migration/`:
- `V1__Initial_schema.sql` - Creates tables (clients, vehicles, contracts) with indexes
- `V2__Sample_data.sql` - Inserts sample data for development (optional in production)

### How It Works

1. **Automatic execution**: Migrations run on application startup
2. **Version tracking**: Flyway maintains a `flyway_schema_history` table
3. **Immutable migrations**: Once applied, migrations cannot be modified
4. **Validation**: Hibernate validates schema matches entities (`ddl-auto: validate`)

### Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Validates schema instead of auto-creating
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### Production Best Practices

✅ **Never modify applied migrations** - Create new versions instead  
✅ **Test migrations on production copy** before deploying  
✅ **Keep migrations small** - One logical change per migration  
✅ **Version control migrations** - They are code  
✅ **Review migration history**: Check `flyway_schema_history` table

### Useful Commands

```bash
# Show migration status
./mvnw flyway:info

# Validate migrations
./mvnw flyway:validate

# Clean database (⚠️ dangerous - drops all objects)
./mvnw flyway:clean
```

For more details, see `src/main/resources/db/migration/README.md`

---

**🎉 Projet généré en approche TDD complète avec 100% de tests passants!**
