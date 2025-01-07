# Spring DDD Starter

Seed project to easily get started implementing applications using patterns of tactical domain driven design.
Besides providing a set of libraries, scaffolding through hygen templates allows to quickly get started with aggregates, repositories, domain events etc. in a consistent and minimal fashion.

## Getting started

1) Once you checked out the repository from Git, import it into your IDE
2) Adapt artifact group and name, description, developer info in the `pom.xml` to your liking
3) Right-click on the `pom.xml` and import the project as Maven project to your IDE
4) Refactor the package `com.example.app` to a suitable name
5) Rename the value of the variable "RootPackage" under _templates/variables.ejs to your chosen package name

### Register byte-buddy in IntelliJ

jMolecules equips aggregates with required JPA annotations by executing the `byte-buddy` Maven plugin. To make sure that these annotations are also available when editing and running code from your IDE, you have to register the plugin to your `build` and `rebuild` commands. For IntelliJ, this is done as follows:

1) After opening the project in IntelliJ, right click on pom.xml and select 'Add as Maven Project'.
2) Open the Maven tool window (found on the right) and find the list of plugins included in the project. Expand `byte-buddy`, right click on transform-extended, and select 'Execute After Build' and 'Execute After Rebuild'.

### Install hygen for scaffolding

The project contains templates to scaffold elements of tactical domain-driven design such as aggregates, repositories, or domain events in a consistent fashion. To make use of this tool, install NodeJS and then run the following command to install `hygen`.

`npm i -g hygen`

In the folder `_templates`, replace the package `com/example/app` in all `*.java.t` files with your own package name.

## Scaffolding your DDD artifacts

The scaffolding promotes a naming strategy, where the name of a repository is simply the plural form of the aggregate, e.g. `Orders` for aggregate `Order`. This underlines the aim to employ a purely domain oriented language in the domain ring of the application.

The scaffolding implies a package design where toplevel packages form modules of the applications, containing directly the domain logic. Controller, on the other hand, which belong to the infrastructure layer, are placed in a subpackage `web` close to the domain logic. The approach has been described in this [blog post](https://medium.com/elca-it/feature-based-modular-code-organization-in-java-e4b611d6c103).

To create a new feature module, type this command:

> $ hygen feature new todo

Next, create an aggregate root with a repository and some first commands and events by typing:

> $ hygen aggregate new Todo --feature=todo

If you want to make your aggregate available through the REST API, type:

> $ hygen controller new Todo --feature=todo

## Baked-in concepts

This project follows a highly opinionated approach to building DDD-style applications in Java. 
It is highly inspired by Oliver Drotbohm's [Spring Restbucks](https://github.com/odrotbohm/spring-restbucks) sample 
application. The approach implemented here more strictly separates different layers of the onion architecture, 
placing for example every controller in a package in the infrastructure layer, avoiding in the domain package 
dependencies to Jackson and other concepts related to the REST API, and explicitly introducing a package for application
and infrastructure rings of the onion.

### Package structure

The generator adheres to the following package structure, which is largely governed by the name of 
features and of aggregates:
<pre>
&lt;root-package&gt;/
  common/                                Root package for common functionality
    exception/                           Basic exception types
    logging/                             Helpers to work with log prefixes
    model/                               Basic types that can be used everywhere
  domain/                                Domain ring of the applicataion
    &lt;feature1&gt;/                    Root package of a featute
      Sample.java                        An aggregate, here with name "Sample"
      SampleCommand.java                 Commands to change the aggregate
      SampleEvent.java                   Domain events produced by the aggregate
      Samples.java                       Repository to work with the aggregate
    &lt;feature2&gt;/                    Root package of another feature
      ...
  application/                           Application ring of the application
  infrastructure/                        Infrastructure ring of the application
    &lt;api1&gt;/                        Implementation of an API to another system
    &lt;api2&gt;/                        Implementation of another API to another system
    demo/                                Demo data creation suitable for development
    logging/                             Logging configuration 
    persistence/                         Persistence configuration
    security/                            Security configuration
    web/                                 REST API of the application
      &lt;feature1&gt;api                Root package of the feature's REST API
        SampleOperationsController.java  Controller exposing operations of the aggregate
        SampleSummary.java               Projection with reduced data suitable to render lists
        SampleDetail.java                Projection with detailed data to render detail views
        SampleLinks.java                 Factory for HAL links of the aggregate
      ...
</pre>

### Persistence

Aggregates are directly persisted to a relational database through JPA / Hibernate. To avoid jeopardizing the domain model with persistence logic, Aggregates rely on jMolecules `byte-buddy` plugin to generated required annotations.
The initial schema is created through Flyway.

### REST API with Links in HAL format

The REST API is largely provided out of the box by Spring Data REST. Its converters allow to reference aggregates through their URI. If exposed through `@RepositoryRestResource`, finder methods of a repository are published under the search resource of an aggregate collection.

Through configuration, http methods to create, update or patch an aggregate are disabled in favor of using well-defined operations for creating and changing aggregates, forcing them to go through the business logic implemented in the domain layer. 

### Architecture verification

The setup comes with several architecture verifications which ensure that new code does not violate the architecture.

#### Module Architecture

The module architecture is verified through Spring Modulith. It ensures that modules do not form cycles and that only allowed dependencies are present.

#### Onion architecture

The Onion architecture is verified through jMolecules, using the simplified onion architecture as a basis, having the three rings
* domain
* application
* infrastructure

Dependencies are only allowed in this direction: infrastructure -> application -> domain

#### DDD architecture

jMolecules also ensures that the elements of tactical domain-driven design are used correctly. There is both a Unit test and the jMolecules annotation processor (jmolecules-atp) which can detect breaches already at compile time.

## Open Issues

* Generate test classes 
* Generate initial DB schema for new aggregates
* Add documentation to generated artifacts
* Allow adding operations with related commands and events one by one
* Log domain events _before_ they are published

## References

* https://odrotbohm.github.io/2021/04/Spring-RESTBucks-in-2021/
* https://github.com/odrotbohm/spring-restbucks

## License

MIT