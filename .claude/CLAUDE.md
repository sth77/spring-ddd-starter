# Project Context for AI Assistants

This is a Spring Boot DDD starter project implementing tactical domain-driven design patterns.

## Technology Stack

- Java 25
- Spring Boot 4.0.1
- Spring Framework 7.0
- Spring Modulith 2.0.1
- jMolecules 2025.0.2
- springdoc-openapi 3.0.0
- Jackson 3.0
- Hibernate 7.0
- H2 Database (dev), Flyway migrations

## Build

```bash
mvn clean verify
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

## Spring Boot 4.0 Notes

- Uses native `@Retryable` from `org.springframework.resilience.annotation`
- Jackson 3: Package changed from `com.fasterxml.jackson` to `tools.jackson`
- Test annotations moved: `@DataJpaTest` in `org.springframework.boot.data.jpa.test.autoconfigure`
- Starter renames: `spring-boot-starter-webmvc`, `spring-boot-starter-security-oauth2-client`

## Architecture Rules

- Onion: infrastructure -> application -> domain
- Modules verified by Spring Modulith
- DDD rules verified by jMolecules ArchUnit tests

## Scaffolding (hygen)

```bash
hygen feature new <name>
hygen aggregate new <Name> --feature=<feature>
hygen controller new <Name> --feature=<feature>
```

See INSTRUCTIONS.md for detailed implementation guidelines.