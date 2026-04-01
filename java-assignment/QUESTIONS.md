# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes — I would converge on a single repository pattern using Panache, and I would do it gradually.

Currently the codebase mixes two styles:
  • Active Record (Store, Product) — the entity itself holds persistence methods like persist(), delete(), findById().
  • Repository pattern (WarehouseRepository, FulfillmentRepository) — a dedicated class implements PanacheRepository<T>
    and the entity is a plain JPA class (DbWarehouse).

The inconsistency creates a higher cognitive load for anyone maintaining the codebase: you have to remember
which style applies to which entity, and it affects how you write tests (Active Record entities are harder
to mock in pure unit tests without starting Quarkus).

My preference for a maintained codebase is the Repository pattern because:
  1. Testability — repositories can be mocked with Mockito; Active Record requires @QuarkusTest + a real DB.
  2. Single Responsibility — the entity is only a data holder; persistence logic lives in one dedicated class.
  3. Extensibility — adding query methods, caching, or switching ORM providers touches one class, not the entity.

I would refactor Product and Store to use dedicated PanacheRepository classes (ProductRepository, StoreRepository)
in a subsequent sprint, keeping the Active Record variants as thin adapters until the migration is complete.
This is a low-risk, incremental change that pays compound interest in testability and maintainability.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Code-first (Product & Store):
  Pros:
    • Fastest path from idea to working endpoint — no context switching between YAML and Java.
    • The contract always reflects what is actually deployed (no drift).
    • Simpler toolchain — no code-generation step in the build.
  Cons:
    • API consumers (frontend, other services) cannot agree on the contract upfront.
    • Harder to enforce a consistent style across teams (naming, error shapes, versioning).
    • Generating client SDKs requires post-hoc spec extraction, which can miss details.

Spec-first / OpenAPI-driven (Warehouse):
  Pros:
    • The contract is the single source of truth: consumers can generate typed clients before
      the server is built, enabling parallel development.
    • Forces explicit, reviewed API design decisions (field names, status codes, error bodies)
      before any code is written — much cheaper to change at spec stage.
    • Consistency enforced by the generator — all endpoints follow the same shape.
  Cons:
    • Adds build complexity (generator plugin, generated sources in version control or .gitignore).
    • Generated interfaces can be verbose or opinionated; customising them requires extra glue.
    • Round-tripping spec ↔ implementation can cause merge conflicts when both evolve.

My choice: Spec-first for any API with external consumers or multiple teams.
For an internal monolith where the team owns every caller, code-first with a Quarkus SmallRye
OpenAPI annotation scan is a pragmatic middle ground — you write code, and the spec is generated
automatically, giving you most of the benefits of spec-first with none of the YAML ceremony.
In this codebase I would adopt that hybrid approach: annotate Product and Store resources with
@Tag, @Operation, and @APIResponse, let SmallRye generate the spec, and use that spec as the
contract for Warehouse going forward.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Priority order (highest ROI first):

1. Unit tests for domain logic and validators (no DB, pure Mockito).
   These are cheap to write, run in milliseconds, and catch the majority of business-rule bugs.
   Every use case (Create, Replace, Archive) and every validator has its own test class covering
   the happy path, each distinct failure branch, and boundary conditions.

2. Integration tests for the REST layer (@QuarkusTest + Dev Services).
   These verify the full HTTP contract — status codes, JSON shapes, correct DB state — without
   needing a separately deployed environment. They are slower (~30 s for the whole suite) but
   give very high confidence that wiring, transactions, and error mappers work end-to-end.

3. BDD / Cucumber scenarios for cross-cutting business rules.
   Written in Gherkin, these are readable by non-engineers and directly map to the business
   requirements in CODE_ASSIGNMENT.md. They live at the same @QuarkusTest level as integration
   tests but express intent rather than implementation.

4. Contract tests (consumer-driven, e.g. Pact) — deferred until there are real external consumers.

Coverage strategy to keep tests effective over time:
  • JaCoCo enforces a minimum threshold (currently 95%+ instructions). A failed coverage gate
    blocks the build, preventing regressions from slipping in unnoticed.
  • Test isolation: each @QuarkusTest class uses the same Dev Services container but restores
    seed data after mutations (as done in WarehouseRepositoryTest), so tests don't depend on
    execution order when run in isolation.
  • BDD feature files serve as living documentation — a new business rule gets a Gherkin
    scenario first, then the step definitions and implementation, following BDD's red-green loop.
  • The CI pipeline runs the full suite on every PR, with test results published to GitHub Checks,
    so failures are visible immediately rather than discovered later.
```