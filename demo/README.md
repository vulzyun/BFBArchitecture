# BFB Management - Gestion de Contrats de Location# BFB Management - MVP Contrats



## 📋 Vue d'ensemble## 📋 Vue d'ensemble



Application Spring Boot implémentée en **TDD (Test-Driven Development)** pour la gestion complète des contrats de location de véhicules avec architecture hexagonale (Ports & Adapters).MVP implémenté en **TDD (Test-Driven Development)** pour la gestion des contrats de location de véhicules avec architecture hexagonale (Ports & Adapters).



### 🎯 Fonctionnalités### 🎯 Objectifs

- Gestion complète du cycle de vie des contrats

- ✅ Gestion complète du cycle de vie des contrats- Règles métier strictes (chevauchements, transitions d'état, disponibilité)

- ✅ Règles métier strictes (chevauchements, transitions d'état, disponibilité)- Architecture découplée prête pour l'intégration avec les domaines Véhicules et Clients

- ✅ Annulation automatique des contrats en attente lors de panne véhicule

- ✅ Job de marquage en retard et annulation des contrats bloqués---

- ✅ Pagination et tri des résultats

- ✅ Validation avancée des DTOs## 🏗️ Architecture

- ✅ Métriques métier avec Micrometer

- ✅ Monitoring avec Spring Boot Actuator### Couches

- ✅ Architecture découplée prête pour l'intégration avec les domaines Véhicules et Clients

```

---com.BFBManagement/

├── architecture.contrats.domain/    # Domain layer (Entities, Repository, Règles pures)

## 🏗️ Architecture│   ├── Contrat.java               # Entity JPA

│   ├── ContratRepository.java      # Spring Data JPA

### Documentation architecture│   ├── EtatContrat.java           # Enum états

│   └── Rules.java                 # Règles métier pures (stateless)

- 📄 [Diagramme de classes](docs/architecture/01_classes_contrats.md)│

- 📄 [Séquence: Job Mark Late](docs/architecture/02_sequence_markLate.md)├── business.contrats/              # Business layer (Services, Ports, Adapters)

- 📄 [Séquence: Vehicle Down](docs/architecture/03_sequence_vehicleDown.md)│   ├── ContratService.java        # Service métier principal

- 📄 [ADR-001: Ports & Adapters](docs/adr/ADR-001-PortsAndAdapters.md)│   ├── ports/                     # Interfaces de découplage

- 📄 [ADR-002: Retard et Annulation](docs/adr/ADR-002-RetardEtAnnulationAutomatique.md)│   │   ├── VehicleStatusPort.java

│   │   └── ClientExistencePort.java

### États et Transitions│   ├── adapters/                  # Implémentations stub (temporaires)

│   │   ├── InMemoryVehicleStatusAdapter.java

```│   │   └── InMemoryClientAdapter.java

EN_ATTENTE  →  EN_COURS  →  TERMINE│   └── exceptions/                # Exceptions métier

    ↓            ↓│       ├── ValidationException.java

  ANNULE     EN_RETARD  →  TERMINE│       ├── OverlapException.java

```│       ├── VehicleUnavailableException.java

│       ├── ClientUnknownException.java

**Transitions autorisées :**│       ├── TransitionNotAllowedException.java

- `EN_ATTENTE` → `EN_COURS`, `ANNULE`│       └── ContratNotFoundException.java

- `EN_COURS` → `TERMINE`, `EN_RETARD`│

- `EN_RETARD` → `TERMINE`└── presentation.contrats/          # Presentation layer (REST API, DTOs)

- `TERMINE`, `ANNULE` : états terminaux (aucune sortie)    ├── ContratController.java      # REST Controller

    ├── CreateContratDto.java       # DTO création

---    ├── ContratDto.java            # DTO réponse

    ├── ContratMapper.java         # Entity <-> DTO

## 🚀 Démarrage rapide    └── GlobalExceptionHandler.java # Gestion erreurs HTTP

```

### Prérequis

- Java 17+### États et Transitions

- Maven 3.8+ (ou utiliser le wrapper `mvnw`)

```

### Lancer l'applicationEN_ATTENTE  →  EN_COURS  →  TERMINE

    ↓            ↓

```powershell  ANNULE     EN_RETARD  →  TERMINE

# Windows```

.\mvnw.cmd spring-boot:run

**Transitions autorisées :**

# Linux/Mac- `EN_ATTENTE` → `EN_COURS`, `ANNULE`

./mvnw spring-boot:run- `EN_COURS` → `TERMINE`, `EN_RETARD`

```- `EN_RETARD` → `TERMINE`

- `TERMINE`, `ANNULE` : états terminaux (aucune sortie)

L'application démarre sur **http://localhost:8080**

---

### Accès aux interfaces

## 🚀 Démarrage rapide

- **Swagger UI** : http://localhost:8080/swagger-ui.html

- **H2 Console** : http://localhost:8080/h2-console (JDBC: `jdbc:h2:mem:bfbdb`, user: `sa`, password: vide)### Prérequis

- **Actuator Health** : http://localhost:8080/actuator/health- Java 17+

- **Métriques** : http://localhost:8080/actuator/metrics- Maven 3.8+ (ou utiliser le wrapper `mvnw`)

- **Prometheus** : http://localhost:8080/actuator/prometheus

### Lancer l'application

---

```powershell

## 🧪 Tests# Windows

cd demo

```powershell.\mvnw.cmd spring-boot:run

.\mvnw.cmd test

```# Linux/Mac

cd demo

✅ **Tests complets** : Tests unitaires, d'intégration, événements, jobs, pagination, validation./mvnw spring-boot:run

```

---

L'application démarre sur **http://localhost:8080**

## 📡 API REST

### Accès aux interfaces

### Endpoints API Publique

- **Swagger UI** : http://localhost:8080/swagger-ui.html

#### 📝 Créer un contrat- **H2 Console** : http://localhost:8080/h2-console

```bash  - JDBC URL: `jdbc:h2:mem:bfbdb`

curl -X POST http://localhost:8080/api/contrats \  - Username: `sa`

  -H "Content-Type: application/json" \  - Password: _(vide)_

  -d '{

    "clientId": "550e8400-e29b-41d4-a716-446655440000",---

    "vehiculeId": "650e8400-e29b-41d4-a716-446655440001",

    "dateDebut": "2025-12-01",## 🧪 Tests

    "dateFin": "2025-12-10"

  }'### Lancer tous les tests

```

```powershell

#### 🔎 Rechercher avec pagination.\mvnw.cmd test

```bash```

GET /api/contrats?page=0&size=10&sort=dateDebut,asc

GET /api/contrats?vehiculeId={uuid}&page=1&size=20&sort=dateFin,desc### Couverture des tests

```

✅ **24 tests au total** :

#### ▶️ Transitions d'état- **8 tests** : `RulesTest` - Règles métier pures (noOverlap, transitionAllowed)

```bash- **15 tests** : `ContratServiceTest` - Service métier avec mocks

PATCH /api/contrats/{id}/start- **1 test** : Intégration Spring Boot

PATCH /api/contrats/{id}/terminate

PATCH /api/contrats/{id}/cancel---

```

## 📡 API REST

#### ⏰ Job : marquer les retards

```bash### Endpoints principaux

POST /api/contrats/jobs/mark-late

```#### 📝 Créer un contrat

```http

### Endpoints API InternePOST /api/contrats

Content-Type: application/json

#### 🚨 Événement : Véhicule en panne

```bash{

POST /internal/events/vehicules/marked-down  "clientId": "550e8400-e29b-41d4-a716-446655440000",

{"vehiculeId": "650e8400-e29b-41d4-a716-446655440001"}  "vehiculeId": "650e8400-e29b-41d4-a716-446655440001",

```  "dateDebut": "2025-12-01",

  "dateFin": "2025-12-10"

---}

```

## 📊 Métriques métier

#### 🔍 Récupérer un contrat

| Métrique | Description |```http

|----------|-------------|GET /api/contrats/{id}

| `contracts.canceled.byVehicleDown` | Contrats annulés suite à panne véhicule |```

| `contracts.canceled.byLateBlock` | Contrats annulés car bloqués par retard |

#### 🔎 Rechercher des contrats

```bash```http

curl http://localhost:8080/actuator/metrics/contracts.canceled.byVehicleDownGET /api/contrats?clientId={uuid}&vehiculeId={uuid}&etat=EN_COURS

curl http://localhost:8080/actuator/prometheus | grep contracts_canceled```

```

#### ▶️ Démarrer / ✅ Terminer / ❌ Annuler

---```http

PATCH /api/contrats/{id}/start

## 🔒 Règles métierPATCH /api/contrats/{id}/terminate

PATCH /api/contrats/{id}/cancel

1. **Validation dates** : `dateDebut` < `dateFin````

2. **Chevauchement** : Contrats occupants (`EN_ATTENTE`, `EN_COURS`, `EN_RETARD`) sur même véhicule interdits

3. **Disponibilité véhicule** : Véhicule `EN_PANNE` → refus (409)#### ⏰ Job : marquer les retards

4. **Existence client** : Client inexistant → refus (409)```http

5. **Transitions d'état** : Transitions invalides → 422POST /api/contrats/jobs/mark-late

6. **Annulation automatique** :```

   - **Suite à panne** : Annule les `EN_ATTENTE` uniquement

   - **Suite à retard** : Annule les `EN_ATTENTE` bloqués (`dateDebut <= aujourd'hui`)---



---## 🔒 Règles métier



## 🎬 Scénarios de démonstration### 1. Validation des dates

- `dateDebut` **strictement** < `dateFin`

### Scénario 1 : Panne véhicule

```bash### 2. Chevauchement (Overlap)

# 1. Créer contrats EN_ATTENTEDeux contrats "occupants" (`EN_ATTENTE`, `EN_COURS`, `EN_RETARD`) sur le même véhicule ne peuvent pas se chevaucher.

# 2. Simuler panne

curl -X POST http://localhost:8080/internal/events/vehicules/marked-down \### 3. Disponibilité véhicule

  -H "Content-Type: application/json" \Véhicule `EN_PANNE` → refus (409)

  -d '{"vehiculeId": "..."}'

### 4. Existence client

# 3. Vérifier annulationsClient inexistant → refus (409)

curl "http://localhost:8080/api/contrats?vehiculeId=...&etat=ANNULE"

```### 5. Transitions d'état

Toute transition invalide → 422 Unprocessable Entity

### Scénario 2 : Job de retard

```bash---

# 1. Créer contrat avec dateFin passée, le mettre EN_COURS

# 2. Exécuter job## 🚨 Codes HTTP

curl -X POST http://localhost:8080/api/contrats/jobs/mark-late

| Code | Signification | Cas d'usage |

# 3. Vérifier EN_RETARD|------|---------------|-------------|

curl "http://localhost:8080/api/contrats?etat=EN_RETARD"| **201** | Created | Contrat créé |

```| **400** | Bad Request | Données invalides |

| **404** | Not Found | Contrat introuvable |

---| **409** | Conflict | Chevauchement, véhicule indisponible |

| **422** | Unprocessable Entity | Transition interdite |

## 🛠️ Stack technique

---

- Java 17

- Spring Boot 3.5.7## 🎓 Approche TDD suivie

- Spring Data JPA + Pagination

- Spring Boot Actuator### Cycle Red-Green-Refactor

- Micrometer + Prometheus

- Bean Validation (Jakarta)1. **RED** : Écrire les tests **AVANT** l'implémentation

- H2 Database2. **GREEN** : Implémenter le minimum pour faire passer les tests

- Springdoc OpenAPI 2.3.03. **REFACTOR** : Nettoyer le code

- JUnit 5 + Mockito

### Ordre d'implémentation

---

1. ✅ Tests règles pures (`Rules`)

## 🔄 CI/CD2. ✅ Tests service métier (avec mocks)

3. ✅ Implémentation domaine + service

Pipeline GitHub Actions (`.github/workflows/ci.yml`) :4. ✅ Controller + DTOs

- Compilation5. ✅ Configuration Spring

- Tests (unitaires + intégration)6. ✅ **24/24 tests passent** ✨

- Upload rapports Surefire

---

---

## 🛠️ Stack technique

**🎉 Projet développé en TDD avec architecture hexagonale complète!**

- Java 17
- Spring Boot 3.5.7
- Spring Data JPA
- H2 Database (dev)
- Springdoc OpenAPI 2.3.0
- JUnit 5 + Mockito + AssertJ

---

**🎉 Projet généré en approche TDD complète avec 100% de tests passants!**
