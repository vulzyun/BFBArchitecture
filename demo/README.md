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
- H2 Database (dev)
- Springdoc OpenAPI 2.3.0
- JUnit 5 + Mockito + AssertJ

---

**🎉 Projet généré en approche TDD complète avec 100% de tests passants!**
