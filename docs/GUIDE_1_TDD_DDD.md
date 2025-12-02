# Guide Pédagogique 1 : TDD & Domain-Driven Design

> **Objectif** : Comprendre comment et pourquoi nous avons utilisé TDD et DDD dans le projet BFB

---

## 🎯 Qu'est-ce que le TDD (Test-Driven Development) ?

### Le Principe de Base

**TDD = "Red → Green → Refactor"**

```
1. RED    : Écrire un test qui échoue (la fonctionnalité n'existe pas encore)
2. GREEN  : Écrire le code minimum pour faire passer le test
3. REFACTOR : Améliorer le code sans casser les tests
```

### Pourquoi TDD dans notre projet ?

#### ❌ Sans TDD (approche classique)
```java
// D'abord on code...
public Contract createContract(CreateContractRequest request) {
    // 200 lignes de code complexe
    // Validations mélangées avec la logique métier
    // Difficile à tester après coup
}

// Puis on essaie de tester... mais c'est compliqué !
// On ne sait pas par où commencer
```

#### ✅ Avec TDD (notre approche)
```java
// ÉTAPE 1 : On écrit d'abord le test (qui va échouer)
@Test
void shouldRejectContractWhenStartDateAfterEndDate() {
    // Given
    LocalDate start = LocalDate.of(2025, 12, 10);
    LocalDate end = LocalDate.of(2025, 12, 5);
    
    // When & Then
    assertThrows(ValidationException.class, 
        () -> contractService.create(clientId, vehicleId, start, end)
    );
}

// ÉTAPE 2 : On code juste ce qu'il faut pour passer le test
public Contract createContract(...) {
    if (startDate.isAfter(endDate)) {
        throw new ValidationException("Start date must be before end date");
    }
    // ... reste du code
}

// ÉTAPE 3 : On refactore (améliore) sans casser le test
```

---

## 📊 Notre Implémentation TDD : Les Preuves

### Structure de Tests (Bottom-Up)

```
1. Tests du Domaine Pur (Nov 1)
   ├── RulesTest.java
   │   ├── testTransitionFromPendingToInProgress() ✓
   │   ├── testIllegalTransitionFromCompletedToPending() ✓
   │   └── testTransitionMatrix() ✓
   │
   └── ContractTest.java
       ├── testOverlapDetection() ✓
       └── testStatusTransitions() ✓

2. Tests de Services (Nov 1)
   └── ContractServiceTest.java
       ├── testCreateContractWithValidData() ✓
       ├── testRejectOverlappingContracts() ✓
       └── testClientNotFound() ✓

3. Tests d'Intégration (Nov 1)
   └── ContractControllerIntegrationTest.java
       ├── testCreateContractViaAPI() ✓
       ├── testGetContractsPagination() ✓
       └── testDeleteContract() ✓
```

### Bénéfices Concrets Observés

#### 1. Confiance dans le Refactoring (30 Nov)
```bash
# On a fait un ÉNORME refactoring architectural
# Résultat : TOUS les tests sont passés !

[INFO] Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
```

**Traduction** : On a pu changer toute l'architecture (hexagonal → 3-tier) en étant sûrs de ne rien casser, parce que les tests nous protégeaient.

#### 2. Détection de Régression (2 Déc)
```bash
# Lors du changement de base de données PostgreSQL → H2
# Les tests ont immédiatement signalé les problèmes de compatibilité

[ERROR] VehicleAvailabilityValidatorTest: Expected H2 syntax, got PostgreSQL
```

**Traduction** : Pas de surprise en production, les bugs sont détectés immédiatement.

#### 3. Documentation Vivante
```java
// Ce test DOCUMENTE le comportement attendu
@Test
void shouldCalculateCorrectPriceForWeekendRental() {
    // Given: Un week-end (2 jours)
    Period weekend = new Period(
        LocalDate.of(2025, 12, 6),  // Samedi
        LocalDate.of(2025, 12, 8)   // Lundi
    );
    
    // When: On calcule le prix
    double price = priceCalculator.calculate(weekend, vehicleRate);
    
    // Then: Tarif week-end appliqué (1.5x le tarif normal)
    assertEquals(300.0, price); // 100€/jour * 2 jours * 1.5
}
```

---

## 🏗️ Domain-Driven Design (DDD)

### Qu'est-ce que DDD ?

**DDD** = Modéliser le code comme le métier parle

#### Le Vocabulaire Métier (Ubiquitous Language)

```java
// ❌ Mauvais : Vocabulaire technique
class Record {
    int id;
    String person;
    String car;
    int status; // 0=pending, 1=active, 2=late ???
}

// ✅ Bon : Vocabulaire métier (DDD)
class Contract {
    ContractId id;
    Client client;
    Vehicle vehicle;
    ContractStatus status; // PENDING, IN_PROGRESS, LATE, COMPLETED
}
```

---

## 🎁 Value Objects : Les Objets de Valeur

### Qu'est-ce qu'un Value Object ?

**Un objet qui représente une valeur, pas une entité**

#### Caractéristiques :
- **Immuable** : Une fois créé, il ne change jamais
- **Auto-validant** : Se valide à la construction
- **Égalité par valeur** : Deux objets avec les mêmes valeurs sont égaux

### Exemple 1 : Value Object `Period`

#### ❌ Sans Value Object (approche primitive)

```java
public class Contract {
    private LocalDate startDate;
    private LocalDate endDate;
    
    public boolean overlapsWith(Contract other) {
        // Logique complexe répétée partout où on en a besoin
        if (this.endDate.isBefore(other.startDate)) return false;
        if (other.endDate.isBefore(this.startDate)) return false;
        return true;
    }
    
    public void setStartDate(LocalDate date) {
        this.startDate = date; // ⚠️ Pas de validation !
    }
}
```

**Problèmes** :
- Validation manquante ou éparpillée
- Logique de chevauchement dupliquée
- On peut mettre une date de fin avant la date de début (bug !)

#### ✅ Avec Value Object `Period`

```java
// Period.java - VALUE OBJECT
public record Period(LocalDate startDate, LocalDate endDate) {
    
    // 1. AUTO-VALIDATION dans le constructeur
    public Period {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException(
                "Start date must be before end date"
            );
        }
    }
    
    // 2. LOGIQUE MÉTIER encapsulée
    public boolean overlapsWith(Period other) {
        return !this.endDate.isBefore(other.startDate) 
            && !other.endDate.isBefore(this.startDate);
    }
    
    // 3. MÉTHODES MÉTIER explicites
    public boolean hasEndedBefore(LocalDate date) {
        return endDate.isBefore(date);
    }
    
    public long durationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}

// Utilisation dans Contract
public class Contract {
    private Period rentalPeriod; // ✓ Déjà validé, immuable, avec comportement
    
    public boolean overlapsWith(Contract other) {
        return this.rentalPeriod.overlapsWith(other.rentalPeriod);
    }
}
```

**Avantages** :
- ✅ **Fail-fast** : Impossible de créer une période invalide
- ✅ **Logique centralisée** : L'algorithme d'overlap est en UN SEUL endroit
- ✅ **Immuable** : Pas de `setPeriod()`, donc pas de bugs de modification accidentelle
- ✅ **Réutilisable** : `Period` peut être utilisé partout (locations, réservations, promotions...)

### Exemple 2 : Value Object `Email`

```java
// Email.java - VALUE OBJECT
public record Email(String value) {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }
}

// Utilisation
public class Client {
    private Email email; // ✓ Toujours valide, pas de String anarchique
}

// Dans le code
Email email = new Email("invalid"); // ❌ Exception immédiate !
Email email = new Email("john@example.com"); // ✓ OK
```

---

## 🔐 Pure Business Rules (Règles Métier Pures)

### Principe : Zéro Dépendance Technique

#### ❌ Mauvais : Logique métier couplée à Spring

```java
@Service // ⚠️ Annotation Spring dans la logique métier
public class ContractRules {
    
    @Autowired // ⚠️ Injection de dépendance technique
    private ApplicationContext context;
    
    public boolean canTransition(ContractStatus from, ContractStatus to) {
        // Logique métier mélangée avec du technique
        logger.info("Checking transition..."); // ⚠️ Dépend du framework
        return true;
    }
}
```

#### ✅ Bon : Règles métier pures (notre implémentation)

```java
// Rules.java - AUCUNE dépendance technique !
public class Rules {
    
    // Règle métier = DONNÉES pures
    private static final Map<ContractStatus, Set<ContractStatus>> ALLOWED_TRANSITIONS = Map.of(
        ContractStatus.PENDING,     Set.of(ContractStatus.IN_PROGRESS, ContractStatus.CANCELLED),
        ContractStatus.IN_PROGRESS, Set.of(ContractStatus.COMPLETED, ContractStatus.LATE),
        ContractStatus.LATE,        Set.of(ContractStatus.COMPLETED)
    );
    
    // Méthode pure : input → output, pas d'effets de bord
    public static boolean isTransitionAllowed(ContractStatus from, ContractStatus to) {
        Set<ContractStatus> allowedTargets = ALLOWED_TRANSITIONS.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }
    
    // Constructeur privé : classe utilitaire
    private Rules() {}
}
```

**Pourquoi c'est génial ?**

1. **Testable en isolation** (pas besoin de Spring)
```java
@Test
void testTransitionRules() {
    // Pas de @SpringBootTest, pas de contexte, juste la logique !
    assertTrue(Rules.isTransitionAllowed(PENDING, IN_PROGRESS));
    assertFalse(Rules.isTransitionAllowed(COMPLETED, PENDING));
}
```

2. **Réutilisable partout** (backend, frontend, mobile, batch...)
```java
// Même logique utilisable dans un batch job, une app mobile, etc.
if (Rules.isTransitionAllowed(currentStatus, newStatus)) {
    contract.updateStatus(newStatus);
}
```

3. **Facile à visualiser** (documentation graphique)
```
PENDING ──→ IN_PROGRESS ──→ COMPLETED
   │                ↓
   └──→ CANCELLED   LATE ──→ COMPLETED
```

---

## 📚 Résumé pour Révision Rapide

### TDD en 3 Points
1. **Red** : Test d'abord (il échoue)
2. **Green** : Code minimum pour passer
3. **Refactor** : Améliorer sans casser

**Bénéfice dans BFB** : 42 tests passent toujours, même après refactoring massif

### DDD en 3 Points
1. **Value Objects** : `Period`, `Email` → immuables, auto-validants
2. **Pure Business Rules** : `Rules.java` → zéro dépendance technique
3. **Ubiquitous Language** : `Contract`, `ContractStatus` → vocabulaire métier

**Bénéfice dans BFB** : Logique métier testable sans Spring, réutilisable partout

---

## ❓ Questions Probables du Tech Lead

### Q1 : "Pourquoi TDD ? Ça prend pas plus de temps ?"
**Réponse** :
- Court terme : Oui, 20-30% plus lent
- Moyen/Long terme : **2x plus rapide** car moins de bugs, refactoring sans peur
- **Preuve dans BFB** : Refactoring architectural (30 Nov) terminé en 1 jour au lieu de 1 semaine estimée

### Q2 : "C'est quoi la différence entre Entity et Value Object ?"
**Réponse** :
- **Entity** : A une identité (ID), mutable, suit un cycle de vie
  - Exemple : `Contract` (id=123, peut changer de statut)
- **Value Object** : Pas d'identité, immuable, égalité par valeur
  - Exemple : `Period` (01/12 → 10/12), on s'en fout de "quel" Period, juste ses valeurs

### Q3 : "Pourquoi les règles métier dans une classe séparée ?"
**Réponse** :
- **Testabilité** : Pas besoin de Spring, base de données, etc.
- **Réutilisabilité** : Même logique dans API, batch, mobile
- **Maintenabilité** : Changement de règle = 1 seul fichier à modifier
- **Preuve dans BFB** : `Rules.java` testé avec `RulesTest.java` (0 dépendances)

### Q4 : "Record en Java, c'est quoi ?"
**Réponse** :
```java
// Avant (Java 8-15)
public class Period {
    private final LocalDate start;
    private final LocalDate end;
    
    public Period(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }
    
    public LocalDate getStart() { return start; }
    public LocalDate getEnd() { return end; }
    
    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... }
}

// Avec record (Java 17+)
public record Period(LocalDate start, LocalDate end) {
    // Tout le reste est auto-généré !
}
```

**Parfait pour les Value Objects** car immuable par défaut.
