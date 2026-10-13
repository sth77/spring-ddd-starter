This file contains instructions how to implement different parts of the architecture, suitable both for human readers and for AI coding agents.

# Domain layer

## Value objects

Value objects should be implemented using Java records. Every package is `@NullMarked` (JSpecify) and NullAway fails the build on a potential null dereference, so fields are non-null by default and only nullable ones are annotated with `@Nullable`. Do not add defensive null checks; do validate business invariants in the compact constructor, failing with a `DomainException`.

Value objects with no fields should implement a static factory `create`. With exactly one field, `of` is right for a technical wrapper (`SampleId.of(uuid)`, `Birthdate.of(date)`), but where the domain has a word for the value, use it: `Weight.ofKg(2.5)`, `TakeOutOfService.forReason("...")`.

Value objects with more than one field should be annotated with Lombok's @Builder annotation, so that arguments are named at the call site. Add the `toBuilder` option where a modified copy is actually needed.

## Entities

ID types of entities and aggregate roots should be implemented as value objects with the name of the entity and suffix "Id". The record should contain a single field `uuidValue`, static factories `of(UUID uuidValue)` (see above) and `random()` which returns an ID instance initialized to a random UUID value, and a `toString()` returning the bare value, which is what Spring Data REST builds resource URIs from.

Entities should implement jMolecules Entity interface, taking the Aggregate they belong to as first template parameter, the ID type as second template parameter, e.g. `Child implements Entity<Parent, ChildId>`.

Entities should be annotated with Lombok's @Getter at class level. They should have a field `id` with the type of the entity's ID (see above). The entity may contain one or more fields holding business data. Fields that must not change once initialized (such as `id`) should be declared as `final`. Fields should _not_ be annotated with JPA annotations.

## Aggregate Roots

Aggregate roots are by nature entities, so all of the above rules apply, except declaration of implementing jMolecules Entity interface.

Aggregates should be instantiated through the hygen command `hygen aggregate new <aggregate name> --feature <feature-name>`, which also creates the related repository.

References to other aggregates should be modeled through jMolecules `Association` class.

Aggregate Roots must not implement setters, but expose methods implementing required business operations, such as `create` (static), `update` or `publish`. Business operations should not take individual arguments as inputs, but rather the dedicated commands, which hold the required data to execute the operation on the aggregate root. Operations must first invoke `assertCan(<CommandType>.class)` to ensure that only valid operations are invoked (it reads better than passing the command instance, at the price of repeating the command type). They may register a domain event through the inherited `registerEvent` method to notify interested parties about the data change.

## Commands and events

Commands and events are value objects, so the above state instructions apply. In addition, new commands and domain events should be declared in the `<AggregateName>Command` and in the `<AggregateName>Event` interface respectively.

## Domain services

Domain services should be implemented as classes in the toplevel directory of the feature the service most belongs to. They should _not_ listen to events nor create database transactions.

## Application services

Application services closely related to a specific domain should be in a subpackage `app` of the feature package the service relates to. App services should declare Transactions and may listen to domain events. Domain events from other modules should be consumed using `@RetryableApplicationModuleListener`, which processes the event in a separate thread and transaction and retries the failures a concurrent writer causes. Handlers must be idempotent: the event publication registry redelivers on restart.

# Infrastructure layer

## REST Controller

A REST controller serving an aggregate to the frontend should be created through the hygen command `hygen controller new <aggregate name> --feature <feature-name>`. Fields that are used in list representations should be added to the summary projection interface (in subpackage `web` in the feature package), fields used for a full presentation of the aggregate in the detail projection interface. Parts of referenced aggregates may be included in the projection through Spring @Value annotations and SPEL expressions. 

## External APIs

Each API implemented by the application should reside in a subpackage of the `_infrastructure` package. Integration with the domain layer can be realized through an interface declared in a domain package (the port), which is implemented in the API package under `_infrastructure/<api-name>` (the adapter), by listening to commands or events published by the domain layer (for outgoing messages), or by directly invoking the respective domain operation or application service (for incoming messages).

## Database migrations

The schema is managed by Flyway. Never edit a migration that has already been applied (`V0001__initial_schema.sql`
and anything shipped). Every schema change is a **new** versioned file; the `hygen` scaffolding creates one automatically for new tables,
named `V<timestamp>__create_<table>.sql`. Hand-written migrations continue the `V000n__<description>.sql` sequence. Because `ddl-auto` is `validate`, a migration that does
not match the mapped entity fails the build at startup.

# Code conventions

Format long method chains with a line break before each stream operation and before each builder operation, e.g.

```java
val people = IntStream.range(1, 3)
        .mapToObj(i -> Person.create(CreatePerson.builder()
                .name("Person " + i)
                .build()))
        .map(people::save)
        .toList();
```

The formatter (Spotless with the Eclipse profile in `eclipse-formatter.xml`) enforces indentation and line length
but never joins wrapped lines, so these intentional breaks are preserved. Run `./mvnw spotless:apply` before
committing.

# Adding business operations

To add a business operation to an aggregate generated from the templates, start with `hygen operation new <Name> --aggregate=<AggregateName> --feature=<feature>` (see the README, "Scaffolding your DDD artifacts"): it injects the command, the event, the guarded aggregate method, the controller endpoint and two unit tests, following the steps below. Then add the command's data, the state change and the rules in `can`, and the integration test.

To add a business operation by hand, the following steps are necessary:
* Declare a command with fields representing the data required to execute the operation (if any). 
* Extend the ´can´ method in the aggregate to define rules when the new command can be executed or not, depending on the state of the aggregate.
* Declare a new method (returning `void`) on the aggregate which takes the command as input parameter named 'data', checks whether the command can be executed (`assertCan(<CommandType>.class)`) and updates the fields of the aggregate accordingly.
* If needed, register one or more domain events on the aggregate after having updated the fields, which are published by Spring on saving the aggregate to the DB.
* If the aggregate has a REST controller, add a method to the controller with the name of the command, taking the command as input (`@Valid @RequestBody`) and invoking with it the `doWith<AggregateType>` method.
* Put the constraints of the operation's input on the command's record components (`@NotNull`, `@NotBlank`, `@Size`, ...): they are validated on the request and also describe the fields of the operation's HAL-FORMS template, which every command link carries through `CommandLinks.afford`.
* The new aggregate method should be covered by a unit test. If the aggregate has a controller, also add an integration test that follows the operation's HAL link from the response (`_links.<rel>.href`) and POSTs to that exact URL — this catches link/route mismatches that reconstructing the URL by hand would hide.

## Operations on child entities

An operation on a single child entity of an aggregate (see `SampleItem` in the sample module) follows the same steps, with these differences:
* Declare the commands on a child under a nested sealed interface `<Child>Command extends <AggregateName>Command` (e.g. `SampleItemCommand`). Adding a child stays a command of the root.
* The child is addressed by the URI, not by the command: the aggregate method takes the child's id followed by the command, e.g. `checkItem(SampleItemId itemId, CheckSampleItem data)`, and first invokes `assertCan(<CommandType>.class, itemId)`. Children are changed through their aggregate root only, so their own methods are package-private.
* Answer "is this allowed for this child" on the root, with `can(Class<? extends <Child>Command>, <Child>Id)`: combine the aggregate's state with the child's own (package-private) `can`, and allow nothing for an unknown id. The type-level `can(Class)` returns `false` for child commands.
* The controller method maps to `/<aggregates>/{<aggregate>Id}/<children>/{<child>Id}/<rel>`, takes the child id as `@PathVariable` and answers with the whole aggregate.
* The links of the children are produced by a `<Child>Links` processor in the feature's `web` package (see `SampleItemLinks`), which hands them to `ChildEntityLinks`. Wrap each child link with `CommandLinks.afford(link, commandType)`, like the generated links, so it carries its HAL-FORMS template. Do not change the generated `<AggregateName>Links`.
* The integration test follows the child's link (`$.items[?(@.label == 'A')]._links.<rel>.href`), selecting the child by a business field, never by position: an entity collection has no defined order.
