# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would. The codebase currently mixes two styles:
- Store and Product use Active Record (the entity calls persist(), delete() directly)
- Warehouse uses a separate Repository class (WarehouseRepository implements PanacheRepository)

It works, but it's inconsistent and that inconsistency has a real cost when you're
maintaining it — you have to remember which style applies to which entity. The bigger
practical issue is testability: Active Record entities need a running Quarkus container
to test persistence behaviour, whereas a repository can be mocked with Mockito in a
plain unit test. That difference shows up in test run time and in how easy it is to
test edge cases in isolation.

I'd move Store and Product to the repository pattern, probably as part of a larger
refactor sprint rather than as a one-off change. Not urgent, but worth doing before the
codebase grows further and the inconsistency becomes harder to untangle.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Both approaches have real trade-offs and I don't think either is universally better.

Code-first (Product & Store) is faster to get started and the spec always reflects what's
actually deployed — there's no risk of the YAML drifting from the implementation. The
downside is that consumers can't agree on the contract until the code exists, which makes
parallel development harder.

Spec-first (Warehouse via OpenAPI YAML) forces you to think about the API design before
you write any code, which is genuinely valuable — it's much cheaper to rename a field in
YAML than to change it after clients are already using it. The cost is build complexity
and the occasional frustration when the generated interfaces are more verbose than what
you'd have written by hand.

My preference in practice depends on the context. For an API with external consumers or
multiple teams working in parallel, spec-first is worth the overhead. For an internal
service where the same team owns the whole stack, I'd lean toward code-first with
SmallRye OpenAPI annotations — you write the code, the spec gets generated automatically,
and you get most of the benefits without maintaining a YAML file separately.

For this specific codebase, I'd probably add proper @Operation and @APIResponse annotations
to Product and Store, let SmallRye generate the spec, and use that as the contract
going forward. That gets you to a consistent approach without a big upfront rewrite.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Given time constraints, I'd focus on unit tests for domain logic first — the validators and
use cases in this project are a good example. They're fast to write, run in milliseconds,
and cover the business rules that are most likely to break. Each validator here gets its
own test class that checks the happy path and each distinct failure condition. That's not
exhaustive, but it's the right level of coverage for the effort.

Integration tests (@QuarkusTest with Dev Services) are second priority. They're slower
but they catch things unit tests miss — wiring issues, transaction boundaries, HTTP error
shapes. For this project, WarehouseEndpointIT covers the full HTTP contract end-to-end.
I wouldn't skip these even under time pressure because they give confidence that the
pieces actually fit together.

BDD scenarios I'd use selectively — they're good for cross-cutting rules that are easier
to express in Gherkin than in JUnit (e.g., "a warehouse cannot exceed location capacity"),
and they serve as living documentation for non-technical reviewers. I wouldn't write
Cucumber steps for every unit test though; that just creates duplication.

To keep coverage meaningful over time: JaCoCo with a threshold enforced in CI means
regressions get caught before merge rather than discovered during review. I'd also make
sure tests fail for the right reasons — a test that only verifies HTTP 200 without
checking the response body doesn't really tell you much.
```