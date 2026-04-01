---
applyTo: "**/adapters/**/*.java,**/domain/usecases/**/*.java,**/domain/ports/**/*.java"
---

# Architecture Layer Instructions

## Layer Map
```
adapters/restapi/    → REST only       (@Path, @GET, @POST, Response)
domain/usecases/     → Orchestration   (calls validators + ports)
domain/validators/   → ONE rule each   (BadRequestException)
domain/ports/        → Interfaces only (no implementation)
adapters/database/   → Persistence     (Panache, @Entity, queries)
```

## Rules per Layer

### REST Layer (`adapters/restapi/`)
- ✅ Handle HTTP in/out only.
- ✅ Map domain objects to/from API beans.
- ✅ Own `@Transactional` here.
- ❌ No business logic, no validation rules.

### UseCases (`domain/usecases/`)
- ✅ Inject and call validators.
- ✅ Inject ports (interfaces), not repositories directly.
- ✅ Orchestrate the flow — call validators, then port.
- ❌ No `@Transactional` — managed by REST layer.
- ❌ No `jakarta.ws.rs.*` imports.
- ❌ No DB queries.

### Ports (`domain/ports/`)
- ✅ Interfaces only — no `@ApplicationScoped`, no implementation.
- ✅ One interface per operation: `CreateWarehouseOperation`, etc.
- ❌ No fat interfaces with multiple unrelated methods.

### Database (`adapters/database/`)
- ✅ Extend `PanacheRepository<T>` or `PanacheEntity`.
- ✅ All queries live here.
- ❌ No business rules or validation logic.
- ❌ No HTTP concerns.

## Dependency Direction (always inward):
```
REST → UseCase → Port ← Repository
                 ↑
             Validator
```
Outer layers depend on inner layers — never the reverse.
