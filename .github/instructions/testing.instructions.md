---
applyTo: "**/*Test.java,**/Test*.java"
---

# Testing Instructions

## Framework
- Use **JUnit 5** + **Mockito** (`@ExtendWith(MockitoExtension.class)`) for all unit tests.
- Use `@QuarkusTest` only for integration/endpoint tests that require a running Quarkus context.
- Never use `@QuarkusTest` for testing plain UseCases or Validators — Mockito is enough.

## Structure
- One test class per production class.
- One test method per scenario.
- Naming: `shouldDoX_WhenY()` — e.g. `shouldThrowBadRequest_WhenCapacityExceedsMax()`.

## Mocking
- Mock all dependencies with `@Mock`.
- Use `@InjectMocks` for the class under test.
- Never create real DB connections or Quarkus beans in unit tests.

## Assertions
- Always assert the exception **message** content, not just the type.
- Use `assertDoesNotThrow()` for happy-path tests.
- Use `assertThrows()` for all failure scenarios.

## Coverage
- Every `*Validator` class must have its own `*ValidatorTest` class.
- Test both: ✅ rule passes AND ❌ rule violated.
- Test boundary values (e.g. exactly at limit, one over limit).
