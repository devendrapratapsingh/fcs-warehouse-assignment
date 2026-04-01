# GitHub Copilot Instructions

## Project Context
This is a **Quarkus-based Java monolith** — a Warehouse Colocation Management System.
Always follow these principles when generating or suggesting code for this project.

---

## ✅ SOLID Principles — Always Apply

### S — Single Responsibility
- Each class must do **ONE thing only**.
- Validators, business rules, persistence, and REST handling must be in **separate classes**.
- ❌ Never put validation logic inside a UseCase or Service method directly.
- ✅ Always extract each validation rule into its own `*Validator` class.

### O — Open/Closed
- Classes should be **open for extension, closed for modification**.
- New business rules = new class, NOT modifying existing ones.
- Use interfaces like `WarehouseValidator` so new rules can be added without touching existing code.

### L — Liskov Substitution
- All implementations of an interface must be substitutable.
- `LocationGateway` implements `LocationResolver` — never bypass the interface.

### I — Interface Segregation
- Keep interfaces small and focused.
- Separate ports: `CreateWarehouseOperation`, `ArchiveWarehouseOperation`, `ReplaceWarehouseOperation`.
- ❌ Never create one big "WarehouseService" interface with all methods.

### D — Dependency Inversion
- Always depend on **abstractions (interfaces), not concretions**.
- ❌ Never inject `WarehouseRepository` directly into a UseCase — use `WarehouseStore` port instead.
- ❌ Never inject `LocationGateway` directly — use `LocationResolver` port.

---

## ✅ Validation Pattern — Always Use

For any new business rule validation, create a dedicated validator class:

```java
// ✅ Correct pattern
@ApplicationScoped
public class MyNewRuleValidator implements WarehouseValidator {
  @Override
  public void validate(Warehouse warehouse) {
    // ONE rule only
    if (/* rule violated */) {
      throw new BadRequestException("Clear message here.");
    }
  }
}
```

Then inject and call it in the UseCase:
```java
myNewRuleValidator.validate(warehouse); // clean, readable, testable
```

❌ Never do this:
```java
// Bad — inline validation in UseCase/Service
if (warehouse.capacity > location.maxCapacity) {
  throw new BadRequestException("...");
}
```

---

## ✅ Architecture Layers — Always Respect

```
REST Layer       →  adapters/restapi/      (HTTP in/out only)
Use Cases        →  domain/usecases/       (orchestration only)
Validators       →  domain/validators/     (ONE rule per class)
Ports/Interfaces →  domain/ports/          (abstractions)
DB Layer         →  adapters/database/     (persistence only)
```

- ❌ Never put business logic in `*ResourceImpl` (REST layer).
- ❌ Never put HTTP concerns (`Response`, `@Path`) in UseCases.
- ✅ UseCases call validators and ports only — nothing else.

---

## ✅ Transaction Rules

- `@Transactional` belongs only in REST resource methods or Service layer.
- UseCases must NOT be `@Transactional` — transaction is managed by the caller.
- For post-commit side effects (e.g. legacy system sync), always use:
  ```java
  @Observes(during = TransactionPhase.AFTER_SUCCESS)
  ```
  Never call external systems directly inside a `@Transactional` method.

---

## ✅ Testing Rules

- Every validator must have its own unit test.
- Use **Mockito** (`@ExtendWith(MockitoExtension.class)`) for unit tests — no Quarkus context needed.
- Each test method tests **ONE scenario** only.
- Test naming convention: `shouldDoX_WhenY()`.

---

## ✅ General Code Rules

- No `UnsupportedOperationException` left in code — always implement.
- No `null` returns — throw `NotFoundException` for missing entities.
- Error messages must be clear and include the offending value.
- Always use constructor injection (not field injection) in domain classes.
- CDI beans (`@ApplicationScoped`, etc.) required on all injectable classes — especially Gateway implementations.
