# Project Context for AI Assistants

This is a Spring Boot DDD starter project implementing tactical domain-driven design patterns.

## Technology Stack

Java, Spring Boot / Spring Framework, Spring Modulith, jMolecules, springdoc-openapi, Jackson 3, Hibernate,
H2 (dev), Flyway. **The `pom.xml` properties are the single source of truth for versions** — do not hard-code
version numbers here; they only go stale.

## Build

```bash
./mvnw test -Dtest=SampleTest   # fast inner loop: domain-only unit test, seconds
./mvnw verify                   # full build incl. ArchUnit + NullAway + IT, ~15s
./mvnw verify -Phygen-it        # regenerate a feature from the templates and test it (needs a clean src/ tree)
./mvnw spotless:apply           # auto-format sources to satisfy the spotless:check gate
```

## Project Structure

- `com.example.app` - Root package
  - `<feature>/` - Feature modules (domain layer)
  - `<feature>/app/` - Feature specific event handlers and application services
  - `<feature>/web/` - REST controllers (infrastructure)
  - `_application/` - Global application services
  - `_infrastructure/` - Infrastructure components, API implementations
  - `common/` - Shared domain types

## Key Patterns

- **Aggregates**: Use jMolecules `@AggregateRoot`, no JPA annotations (byte-buddy adds them)
- **Commands**: Sealed interfaces in `<Aggregate>Command.java`
- **Events**: Sealed interfaces in `<Aggregate>Event.java`
- **Repositories**: Named as plural of aggregate (e.g., `Samples` for `Sample`)
- **Controllers**: In `web/` subpackage, expose operations via commands
- **Enums**: Persist via `AttributeConverter` extending `common.persistence.EnumConverter` — never `@Enumerated` (ArchUnit-enforced)

## Framework behavioral notes (correct common knowledge-cutoff errors)

- Uses native `@Retryable` from `org.springframework.resilience.annotation`
- Jackson 3: Package changed from `com.fasterxml.jackson` to `tools.jackson`
- Test annotations moved: `@DataJpaTest` in `org.springframework.boot.data.jpa.test.autoconfigure`
- Starter renames: `spring-boot-starter-webmvc`, `spring-boot-starter-security-oauth2-client`

## Architecture Rules

- Onion: infrastructure -> application -> domain
- Modules verified by Spring Modulith
- DDD + onion rules verified by jMolecules ArchUnit rules in `ArchitectureTests` (runs under `mvn verify`)
- jMolecules `jmolecules-apt` additionally enforces the DDD rules at compile time
- Schema drift is caught at startup by Hibernate `ddl-auto: validate` against the Flyway schema

## Known failure signatures

- `Not a managed type: class ...Sample` → the jMolecules byte-buddy transform did not run. Build through the
  full lifecycle (`./mvnw compile`), or register the byte-buddy `transform-extended` goal in the IDE (see README).
- Hibernate `Schema-validation` / `wrong column type` / `missing table` at startup → the mapped entity and the
  Flyway schema disagree (`ddl-auto: validate`). Check the `<type>Value` component-naming convention for value
  objects and add a **new** migration (never edit an applied one).
- `-Phygen-it` aborts immediately with "Uncommitted changes detected in src/" → commit or stash first; the profile
  `git clean`s `src/` after running.
- A command link is missing from a HAL response → both conditions must hold: the aggregate allows the operation in
  its state (`can(...)`) **and** the current user has the controller method's `@Secured` role.
- ArchUnit rules "pass" but never appear as executed tests → the test class is missing `@AnalyzeClasses`.

## Scaffolding (hygen)

```bash
hygen feature new <name>
hygen aggregate new <Name> --feature=<feature>
hygen controller new <Name> --feature=<feature>
```

The `sample` module must stay structurally identical to the scaffolding output (plus clearly-marked enrichments).
Change a pattern in the templates first, then bring `sample` in line; `mvn verify -Phygen-it` enforces it.

See INSTRUCTIONS.md for detailed implementation guidelines.