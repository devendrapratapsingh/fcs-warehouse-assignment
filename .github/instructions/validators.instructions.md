---
applyTo: "**/validators/**/*.java"
---

# Validator Pattern Instructions

## Rules
- Every validator class checks **ONE business rule only**.
- Class name must describe the rule: e.g. `MaxWarehousesPerStoreValidator`.
- Always annotate with `@ApplicationScoped` for CDI injection.
- Always throw `BadRequestException` with a clear message including the offending value.

## Interface
- Warehouse-level validators implement `WarehouseValidator`.
- Fulfillment-level validators use their own focused `validate(...)` signature.
- Never make a validator do two things — split into two classes.

## Template
```java
@ApplicationScoped
public class MyRuleValidator {

  private static final int MAX = X; // limit defined here, not in service

  @Inject SomeDependency dependency;

  public void validate(/* specific params */) {
    if (/* rule violated */) {
      throw new BadRequestException(
          "Descriptive message with offending value: " + value);
    }
  }
}
```

## Anti-patterns to avoid
- ❌ Validators returning `boolean` — always throw, never return false.
- ❌ Multiple rules in one `validate()` method.
- ❌ Magic numbers — always use named constants.
- ❌ Catching and re-throwing generic `Exception`.
