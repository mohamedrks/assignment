# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
The codebase uses three different database access patterns:

1. Product - uses PanacheRepository (Repository pattern): clean separation, testable, recommended.
2. Store - uses PanacheEntity (Active Record pattern): simpler but couples domain model to persistence.
3. Warehouse - uses a dedicated DbWarehouse JPA entity mapped to a domain model (Warehouse): the cleanest
   approach as it fully separates persistence concerns from the domain.

If maintaining this codebase, I would standardise everything towards the Warehouse approach (option 3),
because:
- The domain model is completely free from JPA annotations, making it easy to test without a database.
- The mapping between DbWarehouse and Warehouse is explicit and easy to reason about.
- It follows the Hexagonal Architecture already established by the ports/adapters structure.

I would refactor Product and Store to follow the same pattern:
- Introduce DbProduct and DbStore JPA entities.
- Keep Product and Store as pure domain models.
- Use dedicated Repository classes that implement a port interface.

This makes the codebase consistent, easier to test, and properly aligned with the hexagonal architecture
already in place for Warehouse.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
Code-first (Product, Store):
Pros:
- Faster to start - no upfront spec writing required.
- Less tooling setup.
- Changes to the API are immediately reflected in the code.

Cons:
- API contract is implicit, buried in implementation details.
- Harder for consumers to understand the API without running it.
- No single source of truth for the contract; documentation can drift.
- Harder to enforce consistency across teams.

Spec-first / OpenAPI-first (Warehouse):
Pros:
- API contract is explicit and serves as a single source of truth.
- Frontend/consumers can work in parallel using the spec (mock servers).
- Generated interfaces enforce that the implementation matches the contract.
- Better for team collaboration and public-facing APIs.
- Easier to version and evolve the API deliberately.

Cons:
- More upfront effort to write the YAML spec.
- Code generation adds build complexity and can produce verbose boilerplate.
- Changes require updating the spec first, which can feel slower during rapid iteration.

My choice: Spec-first (OpenAPI) for any API that is consumed by external teams or services.
For internal, single-team APIs in early development, code-first is acceptable as long as the
team commits to generating the spec from code (e.g. using SmallRye OpenAPI annotations) before
the API stabilises. The Warehouse approach in this project is the right long-term direction.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Priority order (highest ROI first):

1. Unit tests for Use Cases (highest priority)
   The use cases contain all the business rules and validations - this is where bugs are most costly.
   They are pure Java with no framework dependency, so they run fast and are easy to write using mocks
   (Mockito). These tests give the most confidence per unit of effort.
   Example: CreateWarehouseUseCaseTest, ReplaceWarehouseUseCaseTest, ArchiveWarehouseUseCaseTest.


2. Integration tests for REST endpoints (medium priority)
   Use @QuarkusTest + REST Assured to test the full HTTP stack including request/response serialisation,
   status codes, and validation. Focus on the happy path and the most critical error cases.
   Quarkus Dev Services spins up a real database automatically, so these tests are reliable without
   manual setup.
   Example: WarehouseEndpointIT, ProductEndpointTest.

3. Repository/database layer tests (lower priority)
   If the repository logic is complex, add @QuarkusTest tests that verify persistence behaviour.
   For simple CRUD like in this project, the integration tests above already cover this indirectly.

Strategy to keep coverage effective over time:
- Follow the testing pyramid: many unit tests, fewer integration tests.
- Run all tests in CI on every pull request.
- Require new business rules to come with a corresponding use case unit test.

4. Load test would be another last choice to test the API responsiveness. Because this will give you nearly the real behaviour of the system in the production.
```