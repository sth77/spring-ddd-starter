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
./mvnw verify -Phygen-it        # regenerate a feature from the templates and test it (needs a clean src/ tree);
                                # runs the generated unit tests too, via a nested `mvn test`, so it takes ~2x
                                # as long and wipes target/classes + target/test-classes when done
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
- **Enums**: Persisted by name for the whole model by `_infrastructure/persistence/StringEnumTypeContributor` —
  a new enum needs no declaration at all, and never `@Enumerated` (ArchUnit-enforced)
- **Physical names**: never `@Table`/`@Column`/`@JoinColumn`/`@AttributeOverride` in the domain ring — column names
  come from `_infrastructure/persistence/ValueObjectAwareImplicitNamingStrategy` plus Spring Boot's default
  physical strategy (`CamelCaseToUnderscoresNamingStrategy`), which snake-cases them (ArchUnit-enforced). The only
  `@JoinColumn` allowed is the unnamed one byte-buddy itself writes on a child entity collection (`Sample.items` →
  join column `items_id` in `sample_item`)
- **Forms**: every command link is wrapped with `common/web/CommandLinks.afford(link, commandType)`, which adds a
  HAL-FORMS template keyed by the link's rel (see README "Forms with HAL-FORMS")
- **Child entities**: operations take `(<Child>Id, command)`, guarded by the root's `can(command, childId)`; links
  come from `<feature>/web/<Child>Links` via `common/web/ChildEntityLinks` (see README "Command links on child
  entities")

## Framework behavioral notes (correct common knowledge-cutoff errors)

- Uses native `@Retryable` from `org.springframework.resilience.annotation`
- Jackson 3: Package changed from `com.fasterxml.jackson` to `tools.jackson`
- Test annotations moved: `@DataJpaTest` in `org.springframework.boot.data.jpa.test.autoconfigure`
- Hibernate 7 has **no** global "enums as string" setting (`hibernate.type.prefer_native_enum_types` only picks a
  *native* enum SQL type once the style is already `STRING`), hence the `TypeContributor`
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
  In IntelliJ it can also mean the module got split: IntelliJ splits a Maven module in two as soon as the
  `default-compile` and `default-testCompile` executions of `maven-compiler-plugin` differ in any way, and the
  after-build byte-buddy task then no longer covers the test side. Keep the compiler plugin's Error Prone /
  NullAway setup in the single plugin-level `<configuration>` (Error Prone is scoped to main via
  `-XepExcludedPaths`) - never move it into a per-execution block.
- Hibernate `Schema-validation` / `wrong column type` / `missing table` at startup → the mapped entity and the
  Flyway schema disagree (`ddl-auto: validate`). Check the `<type>Value` component-naming convention for value
  objects and add a **new** migration (never edit an applied one).
- `-Phygen-it` aborts immediately with "Uncommitted changes detected in src/" → commit or stash first; the profile
  `git clean`s `src/` after running.
- A command link is missing from a HAL response → both conditions must hold: the aggregate allows the operation in
  its state (`can(...)`) **and** the current user has the controller method's `@Secured` role.
- A command link is missing on a child entity (e.g. `sample.items[i]._links`) → check, in order: the root's
  `can(command, childId)` allows it (aggregate state **and** the child's own state); the current user has the
  controller method's `@Secured` role; the command is a record of the nested `<Child>Command` interface and handled
  by the operations controller (`<Child>Links` builds its `SecuredAggregateCommands` from that interface); and the
  child is not rendered through a projection (`?projection=...`), where entities are serialized as plain beans
  without the processors.
- `_templates` missing from a response → check, in order: the client sent `Accept: application/prs.hal-forms+json`
  (plain HAL never renders templates, and a resource without any command link falls back to `application/hal+json`
  even when HAL-FORMS was asked for); the command link exists at all (templates follow the links, see the two
  signatures above); and the link was built through `common/web/CommandLinks.afford(link, commandType)` — a link
  added without it renders in `_links` but has no template. A template whose property is `readOnly` or whose text has
  `min`/`max` instead of `maxLength` was built with Spring HATEOAS's plain `withInput(...)`, not `afford`.
- "required a bean of type 'com.example.app.common.web.ChildEntityLinks' that could not be found" in an
  `@ApplicationModuleTest` → `common` lost its `@Modulith(sharedModules = "common")` declaration on
  `SampleApplication`; module tests only bootstrap the dependencies they inject beans from.
- ArchUnit rules "pass" but never appear as executed tests → the test class is missing `@AnalyzeClasses`.

## Scaffolding (hygen)

```bash
hygen feature new <name>
hygen aggregate new <Name> --feature=<feature>
hygen controller new <Name> --feature=<feature>
hygen referencedata new <Name>
```

The `sample` module must stay structurally identical to the scaffolding output (plus clearly-marked enrichments).
Change a pattern in the templates first, then bring `sample` in line; `mvn verify -Phygen-it` enforces it.

The templates also emit **tests** (`<Name>Test`, `<Plural>Test`, `<Feature>TestData`), and `-Phygen-it` compiles and
runs them. A template change that alters a command's fields must therefore keep the generated tests passing —
`src/test/resources/hygen-it/wire-country-into-human.sh` patches the generated test data for the mandatory `country`
field it adds, and every one of its `patch` calls fails loudly if its anchor has moved.

See INSTRUCTIONS.md for detailed implementation guidelines.