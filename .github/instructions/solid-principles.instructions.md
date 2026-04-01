---
applyTo: "**/*.java"
---

# SOLID Principles — Quarkus Warehouse Colocation System

All Java code in this project must follow SOLID principles. Below are the rules
with concrete examples taken directly from this codebase.

---

## S — Single Responsibility Principle

> Each class has **one reason to change**.

### Rule
- Validators, use cases, repositories, and REST resources are **separate classes**.
- A class whose name contains "And", "Manager", or "Everything" is a red flag.

### ✅ This project's correct pattern

```java
// ONE class → ONE rule
@ApplicationScoped
public class MaxWarehousesPerLocationValidator implements WarehouseValidator {
  private static final int MAX = 3;

  @Override
  public void validate(Warehouse warehouse) {
    long count = warehouseStore.findActiveByLocation(warehouse.locationId).size();
    if (count >= MAX) {
      throw new BadRequestException(
          "Location already has " + MAX + " active warehouses.");
    }
  }
}
```

### ❌ What to avoid

```java
// BAD — UseCase doing validation, persistence AND business logic
public class CreateWarehouseUseCase {
  public void create(Warehouse w) {
    // validation mixed in
    if (repo.findByCode(w.code).isPresent()) throw new BadRequestException("...");
    if (repo.countByLocation(w.locationId) >= 3) throw new BadRequestException("...");
    // persistence mixed in
    repo.persist(w);
  }
}
```

---

## O — Open/Closed Principle

> Open for **extension**, closed for **modification**.

### Rule
- Adding a new business rule = **new class**, never editing an existing validator.
- Use the `WarehouseValidator` interface so new rules plug in without touching existing code.

### ✅ This project's correct pattern

```java
// Step 1 — existing validators stay untouched
// Step 2 — add a new rule by creating a new class
@ApplicationScoped
public class MinCapacityValidator implements WarehouseValidator {
  private static final int MIN_CAPACITY = 10;

  @Override
  public void validate(Warehouse warehouse) {
    if (warehouse.capacity < MIN_CAPACITY) {
      throw new BadRequestException("Capacity must be at least " + MIN_CAPACITY + ".");
    }
  }
}

// Step 3 — inject it in the UseCase alongside existing validators
@Inject MinCapacityValidator minCapacityValidator;
```

### ❌ What to avoid

```java
// BAD — editing an existing validator to add a second rule
public class LocationCapacityValidator implements WarehouseValidator {
  public void validate(Warehouse w) {
    if (w.capacity > location.maxCapacity) throw ...;
    // ← adding a second unrelated rule here violates OCP AND SRP
    if (w.capacity < 10) throw ...;
  }
}
```

---

## L — Liskov Substitution Principle

> Implementations must be **fully substitutable** for their interface.

### Rule
- Every `WarehouseValidator` implementation must honour the contract: throw
  `BadRequestException` on failure, return normally on success.
- `LocationGateway` implements `LocationResolver` — never bypass the interface
  by injecting the concrete class into a use case.

### ✅ This project's correct pattern

```java
// Port (inner layer) — the contract
public interface LocationResolver {
  Location resolveByIdentifier(String identifier);
}

// Adapter (outer layer) — the implementation
@ApplicationScoped
public class LocationGateway implements LocationResolver {
  @Override
  public Location resolveByIdentifier(String identifier) {
    return locations.stream()
        .filter(l -> l.identifier.equalsIgnoreCase(identifier))
        .findFirst()
        .orElseThrow(NotFoundException::new);
  }
}

// UseCase depends on the interface, not the concrete class
@Inject LocationResolver locationResolver; // ✅
```

### ❌ What to avoid

```java
@Inject LocationGateway locationGateway; // ❌ depends on concretion
```

---

## I — Interface Segregation Principle

> Clients should not be forced to depend on methods they don't use.

### Rule
- Keep operation interfaces **small and focused**: one interface per use-case operation.
- Never create a single `WarehouseService` interface with all methods.

### ✅ This project's correct pattern

```java
// Separate, focused operation interfaces
public interface CreateWarehouseOperation {
  void create(Warehouse warehouse);
}

public interface ArchiveWarehouseOperation {
  void archive(String businessUnitCode);
}

public interface ReplaceWarehouseOperation {
  void replace(String businessUnitCode, Warehouse replacement);
}
```

REST resource injects only what it needs:

```java
@Inject CreateWarehouseOperation createOp;  // only what this endpoint needs
@Inject ArchiveWarehouseOperation archiveOp;
```

### ❌ What to avoid

```java
// BAD — fat interface forces every consumer to know about all operations
public interface WarehouseService {
  void create(Warehouse w);
  void archive(String code);
  void replace(String code, Warehouse w);
  List<Warehouse> findAll();
  // ... grows forever
}
```

---

## D — Dependency Inversion Principle

> Depend on **abstractions**, not concretions.  
> High-level modules must not depend on low-level modules — both depend on interfaces.

### Rule
- UseCases inject **ports** (interfaces in `domain/ports/`), not repositories.
- Validators inject ports, not `PanacheRepository` classes.
- `@ApplicationScoped` concrete classes live in `adapters/` — inner layers never import them.

### ✅ This project's correct pattern

```java
// domain/ports/WarehouseStore.java — abstraction
public interface WarehouseStore {
  Optional<Warehouse> findByBusinessUnitCode(String code);
  List<Warehouse> findActiveByLocation(Long locationId);
  void persist(Warehouse warehouse);
}

// domain/usecases/CreateWarehouseUseCase.java — depends on abstraction
@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {
  @Inject WarehouseStore warehouseStore;        // ✅ interface
  @Inject LocationResolver locationResolver;    // ✅ interface
  @Inject UniqueBusinessUnitCodeValidator uniqueCodeValidator;
  // ...
}
```

### ❌ What to avoid

```java
// BAD — UseCase imports the concrete repository
@Inject WarehouseRepository warehouseRepository;  // ❌ concretion
@Inject LocationGateway locationGateway;           // ❌ concretion
```

---

## Dependency Direction (always inward)

```
REST (adapters/restapi/)
  └─► UseCase (domain/usecases/)
        └─► Port interface (domain/ports/)
                ▲
        Repository (adapters/database/)  ← implements the port
        Validator  (domain/validators/)  ← called by UseCase
```

- Outer layers depend on inner layers — **never the reverse**.
- `adapters/` may import `domain/` — `domain/` must **never** import `adapters/`.

---

## Quick Checklist Before Committing

| Check | Question |
|-------|----------|
| **S** | Can you describe this class in one sentence without "and"? |
| **O** | Can you add a new business rule without editing existing files? |
| **L** | Does every validator throw (not return false) on failure? |
| **I** | Does each interface have ≤ 3 methods? |
| **D** | Does the UseCase inject only interfaces, not `*Repository` or `*Gateway`? |
