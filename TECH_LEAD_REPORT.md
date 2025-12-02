# BFB Management - Technical Decision Report

**Project**: Vehicle Rental Management System  
**Period**: October - December 2025  
**Team**: Saad (lead), Vulzyun, Mohamedlam, Xaymaa  
**Stack**: Spring Boot 3.5.7, Java 17, H2/PostgreSQL

---

## Executive Summary

A vehicle rental management system built with **TDD-first methodology** and **DDD principles**, initially architected with hexagonal pattern but deliberately simplified to a pragmatic 3-tier architecture. The project demonstrates mature engineering practices: comprehensive test coverage (13+ test classes), pure domain logic with value objects, strategic design pattern implementation (Chain of Responsibility, State Pattern), and evidence-based architectural pivots. Key lesson: right-sizing architecture to project complexity.

---

## 1. Methodological Approach: TDD & DDD

### Test-Driven Development
- **100+ commits** show systematic TDD workflow: tests precede implementation
- Test infrastructure established first (Nov 1): domain rules tests → service tests → integration tests
- Branch `feature/contrats-mvp-tdd` dedicated to TDD MVP delivery
- Test suite structure:
  - **Domain layer**: `RulesTest`, `ContractTest` (pure business logic)
  - **Service layer**: `ContractServiceTest`, validation chain tests
  - **Integration**: `ContractControllerIntegrationTest` (E2E REST API)

**Concrete benefits observed**:
- Business rules validated independently before any infrastructure code
- Regression detection during architectural refactoring (Nov 30 mass refactor: all tests passed)
- Confidence in database migration rollback (Dec 1-2 PostgreSQL → H2 revert)

### Domain-Driven Design

**Value Objects** (immutable, self-validating):
```java
record Period(LocalDate start, LocalDate end) {
    // Encapsulates overlap detection, date range validation
    public boolean overlapsWith(Period other) { ... }
}
```
- **Email** value object: encapsulates validation logic
- **Period** value object: contains complex overlap detection algorithm

**Pure Business Rules** (`Rules.java`):
- State transition matrix isolated from framework dependencies
- `isTransitionAllowed(from, to)` → zero coupling to Spring/JPA
- Testable without any infrastructure

**Impact**: Business logic remains framework-agnostic, fully testable in isolation.

---

## 2. Architectural Evolution & Pivots

### Phase 1: Hexagonal Architecture (Oct 28 - Nov 18)
**Initial design** (`feature/clean-architecture` branch):
- Ports & Adapters pattern
- `business/contract/ports/` for interfaces
- Adapters: `ClientExistenceAdapter`, `VehicleStatusAdapter`
- Complete isolation between layers

**Commits evidence**:
- `a6eb0e4` (Nov 11): "Revise README for Hexagonal Architecture overview"
- `6e4f927` (Nov 18): "Restructure to clean 3-layer architecture"

### Phase 2: Simplification Decision (Nov 18 - Dec 2)
**Critical pivot** (commit `27d9b7d`, Dec 2):
> "refactor: transition to 3-tier architecture by removing hexagonal architecture references"

**Rationale** (documented in `REFACTORING_SUMMARY.md`):
- **Overkill** for a monolithic Spring Boot CRUD application
- Unnecessary abstraction overhead: ports/adapters added complexity without benefit
- Team velocity impacted by ceremony of maintaining adapter layer
- **Direct service-to-service calls** sufficient for internal bounded contexts

**New structure**:
```
interfaces/     → REST controllers, DTOs, validation
business/       → Services directly call other services
infrastructure/ → JPA repositories, persistence
```

**Lesson learned**: Hexagonal architecture shines for systems with multiple I/O channels or when isolating from volatile external dependencies. For internal service composition in a Spring monolith, standard 3-tier is more maintainable.

---

## 3. Database Journey: H2 → PostgreSQL → H2

### Timeline
- **Nov 1**: Start with H2 (in-memory, rapid development)
- **Nov 23** (`5209cdc`): "connexion à la bdd postgres" → migrate to PostgreSQL
- **Nov 23** (`d8adcc0`): "retour a h2 pour le dev" → rollback same day
- **Dec 1-2** (`c5481a7`, `dbd876a`): Second PostgreSQL attempt
- **Dec 2** (`f37af88`): **Revert** → permanent return to H2

### Why the Rollback?
Commits reveal:
- Docker Compose setup complexity for team onboarding
- Local environment inconsistencies (different OS: Windows/Mac/Linux)
- PostgreSQL connection issues blocking development workflow
- No actual PostgreSQL-specific features required (no advanced indexing, partitioning, etc.)

**Technical decision**: H2 provides:
- Zero configuration overhead
- Consistent behavior across team environments
- Adequate for prototype/MVP phase
- Easy integration testing (embedded mode)

**Lesson learned**: Premature optimization. PostgreSQL planned for "production-ready" feel, but MVP phase doesn't justify operational complexity. Defer production database selection until deployment requirements are clear.

---

## 4. Design Patterns Implementation

### Chain of Responsibility (Validation)
**Problem**: Contract creation requires 4+ distinct validations  
**Implementation** (Nov 30, commit `7740ec1`):

```java
interface ContractValidator {
    void validate(ContractCreationContext context);
}

// Individual validators:
- DateValidator           → date coherence
- ClientExistenceValidator → client exists
- VehicleAvailabilityValidator → vehicle free
- OverlapValidator        → no scheduling conflicts
```

**Benefits**:
- Each validator = Single Responsibility
- Add/remove validators without modifying service logic
- Independent unit testing per validator
- Clear error messages from specific validator

**Alternative considered**: Validation in service method → rejected (700+ LOC method, untestable)

### State Pattern (Contract Status)
**Problem**: Complex status transitions (PENDING → IN_PROGRESS → LATE → COMPLETED)  
**Implementation** (Nov 30, commit `eed8de1`):

```java
class Rules {
    Map<ContractStatus, Set<ContractStatus>> ALLOWED_TRANSITIONS;
    boolean isTransitionAllowed(from, to);
}
```

**Benefits**:
- Prevents illegal transitions at compile-time
- Business rules as data (Map), not scattered if/else
- Easy to visualize state machine
- `TransitionNotAllowedException` with clear messaging

### Repository Pattern
**Standard Spring Data JPA** with custom queries:
- `@Query` for overlap detection (Nov 30, `73d7a53`: "optimized query for overdue contracts")
- `existsById()` optimization (commit `b463e89`) → avoid loading full entity for existence checks

---

## 5. Technical Stack Evolution

| Component | Initial | Current | Reason for Change |
|-----------|---------|---------|-------------------|
| **Database** | H2 | H2 (after PostgreSQL detour) | Simplicity wins for MVP |
| **Mapping** | Manual toDto() | MapStruct 1.5.5 | Reduce boilerplate, type-safe |
| **Migrations** | None | Flyway | Schema versioning, audit trail |
| **Validation** | Scattered | Bean Validation + Chain | Separation of concerns |
| **API Docs** | None | Springdoc OpenAPI 2.7.0 | Auto-generated Swagger UI |

### Key Additions

**Flyway** (Nov 30, commit `9f99ced`):
- `V1__initial_schema.sql` → versioned migrations
- `V2__audit_columns.sql` → audit timestamps
- Rationale: Track schema evolution, reproducible environments

**MapStruct** (Nov 30, commit `10e7caa`):
- Replaces 200+ lines of manual mapping code
- Compile-time generation → zero runtime overhead
- `ContractMapper`, `VehicleMapper`, `ClientMapper` interfaces

**Scheduled Jobs** (Nov 30, commit `88a52a8`):
- `@Scheduled` for automatic late contract detection
- Configurable cron expressions via `application.yml`
- Demonstrates automation of business processes

**Base Controller Abstraction** (Nov 30, commit `329d1d9`):
```java
abstract class BaseRestController<T, D> {
    protected ResponseEntity<D> created(D dto);
    protected ResponseEntity<D> ok(D dto);
    protected ResponseEntity<PageResponse<D>> okPage(Page<D> page);
}
```
- DRY principle: eliminate 100+ lines of repetitive `ResponseEntity.status(...).body(...)`

---

## 6. Key Takeaways for Tech Leads

### ✅ Do's

1. **TDD is Non-Negotiable for Domain Logic**
   - Business rules tested in isolation = confidence in refactoring
   - Integration tests caught architectural mistakes during 3-tier migration

2. **Architecture Should Match Complexity**
   - Hexagonal architecture became overhead, not enabler
   - Team velocity improved post-simplification
   - **Ask**: "What problem does this abstraction solve TODAY?"

3. **Value Objects are Worth the Investment**
   - `Period.overlapsWith()` encapsulates complex logic
   - Immutability prevents state bugs
   - Self-validating constructors → fail-fast at boundaries

4. **Design Patterns with Purpose**
   - Chain of Responsibility cleaned 700 LOC service
   - State Pattern prevents illegal transitions (contract lifecycle integrity)
   - **Not used**: Factory (no complex object creation), Observer (no event system yet)

5. **Defer Infrastructure Decisions**
   - H2 vs PostgreSQL debate resolved by "what's blocking us NOW?"
   - Production database choice = deployment concern, not MVP concern

### ❌ Don'ts

1. **Premature Production Optimization**
   - PostgreSQL migration wasted 2 days, added zero value
   - Team blocked by Docker setup issues

2. **Architecture Astronauts**
   - Hexagonal ports/adapters pattern → over-engineered for team size and project scope
   - "Might need to swap databases" → YAGNI principle violated

3. **Skip Migration Tooling**
   - Flyway added late → early schema changes not versioned
   - **Recommendation**: Add on day 1, even for H2

### Metrics

- **Test Coverage**: 13+ test classes, ~40+ test methods
- **Refactoring Events**: 3 major (TDD setup, 3-tier pivot, validation chain)
- **Database Migrations**: 2 failed, 1 lesson learned
- **Design Patterns**: 3 deliberately applied (Chain, State, Repository)
- **Team Velocity**: Improved post-architectural simplification (Nov 30 mass refactor = 15 commits in 1 day)

---

## Conclusion

This project exemplifies **pragmatic software engineering**: starting with solid principles (TDD, DDD), making evidence-based architectural pivots (hexagonal → 3-tier), and prioritizing delivery over perfection (H2 over PostgreSQL). The team demonstrated maturity by **reversing decisions** when complexity didn't justify benefits. For tech leads: encourage this mindset—architecture serves the team, not vice versa.

**Final State**: Production-ready MVP with clean 3-tier architecture, comprehensive test suite, pure domain logic, and zero unnecessary abstractions. Ready to scale when requirements demand it.
