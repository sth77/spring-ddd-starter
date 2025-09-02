This file contains instructions how to implement different parts of the architecture, suitable both for human readers and for AI coding agents.

# Domain layer

## Value objects

Value objects should be implemented using Java records. Nullable fields should be annotated with an @Nullable annotation, required fields with Lombok's @NonNull (to have runtime null-checks generated).

Value objects with no fields should implement a static factory "create", value objects with exactly one field a static factory with name "of", taking the single value as input.

Value objects with more than one field should be annotated with Lombok's @Builder annotation, including the toBuilder option set to true.

## Entities

ID types of entities and aggregate roots should be implemented as value objects with the name of the entity and suffix "Id". The record should contain a single field `uuidValue`, as well as static factories `of(UUID uuidValue)` (see above) and `random()` which returns an ID instance initialized to a random UUID value.

Entities should implement jMolecules Entity interface, taking the Aggregate they belong to as first template parameter, the ID type as second template parameter, e.g. `Child implements Entity<Parent, ChildId>`.

Entities should be annotated with Lombok's @Getter at class level. They should have a field `id` with the type of the entity's ID (see above). The entity may contain one or more fields holding business data. Fields that must not change once initialized (such as `id`) should be declared as `final`. Fields should _not_ be annotated with JPA annotations.

## Aggregate Roots

Aggregate roots are by nature entities, so all of the above rules apply, except declaration of implementing jMolecules Entity interface.

Aggregates should be instantiated through the hygen command `hygen aggregate new <aggregate name> --feature <feature-name>`, which also creates the related repository.

References to other aggregates should be modeled through jMolecules `Association` class.

Aggregate Roots must not implement setters, but expose methods implementing required business operations, such as `create` (static), `update` or `publish`. Business operations should not take individual arguments as inputs, but rather the dedicated commands, which hold the required data to execute the operation on the aggregate root. Operations must first invoke `assertCan(<CommandType>.class)` to ensure that only valid operations are invoked. They may register a domain event through the inherited `registerEvent` method to notify interested parties about the data change.

## Commands and events

Commands and events are value objects, so the above state instructions apply. In addition, new commands and domain events should be declared in the `<AggregateName>Command` and in the `<AggregateName>Events` interface respectively.

## Domain services

Domain services should be implemented as classes in the toplevel directory of the feature the service most belongs to. They should _not_ listen to events nor create database transactions.

## Application services

Application services closely related to a specific domain should be in a subpackage `app` of the feature package the service relates to. App services should declare Transactions and may listen to domain events. Domain events from other modules should be consumed using jMolecules @ApplicationModuleListener annotation, processing the event in a separate thread and using a database transaction.

# Infrastructure layer

## REST Controller

A REST controller serving an aggregate to the frontend should be created through the hygen command `hygen controller new <aggregate name> --feature <feature-name>`. Fields that are used in list representations should be added to the summary projection interface (in subpackage `web` in the feature package), fields used for a full presentation of the aggregate in the detail projection interface. Parts of referenced aggregates may be included in the projection through Spring @Value annotations and SPEL expressions. 

## External APIs

Each API implemented by the application should reside in a subpackage of the `_infrastructure` package. Integration with the domain layer can be realized through an interface declared in a domain package (the port), which is implemented in the API package under `_infrastructure/<api-name>` (the adapter), by listening to commands or events published by the domain layer (for outgoing messages), or by directly invoking the respective domain operation or application service (for incoming messages).

# Adding business operations

To add a business operation to an aggregate, the following steps are necessary:
* Declare a command with fields representing the data required to execute the operation (if any). 
* Extend the ´can´ method in the aggregate to define rules when the new command can be executed or not, depending on the state of the aggregate.
* Declare a new method on the aggregate which takes the command as input parameter named 'data', checks whether the command can be executed (`assertCan(<Command Type>.class)`) and updates the fields of the aggregate accordingly.
* If needed, register one or more domain events on the aggregate after having updated the fields, which are published by Spring on saving the aggregate to the DB.
* If the aggregate has a REST controller, add a method to the controller with the name of the command, taking the command as input and invoking with it the ´doWith<AggregateType> method´.
* the new methods in aggregate and controller should be covered by a new unit test





