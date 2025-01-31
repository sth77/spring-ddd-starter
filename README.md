# Spring DDD Starter

Seed project to easily get started implementing applications using patterns of tactical domain driven design in Java.
It provides a suitable set of [Spring](https://spring.io/) libraries complemented by scaffolding through [hygen](https://github.com/jondot/hygen) 
templates allowing to quickly get started with aggregates, repositories, domain events etc. in a consistent and minimal 
fashion. The project lays a foundation to build the application in a modular fashion based on [Spring Modulith](https://spring.io/projects/spring-modulith). 
Layering along the simplified onion architecture cleanly separates domain, application, and infrastructure concerns, 
helping to reduce complexity as applications grow large. Both DDD and architectural concepts are made 
explicit and enforced through [jMolecules](https://github.com/xmolecules/jmolecules).

## Getting started

1) Once you checked out the repository from Git, import it into your IDE
2) Adapt artifact group and name, description, developer info in the `pom.xml` to your liking
3) Right-click on the `pom.xml` and import the project as Maven project to your IDE
4) Refactor the package `com.example.app` to a suitable name
5) Replace the path `com/example/app` in all files to match the package you defined in the previous step
6) Rename the value of the variable "RootPackage" under _templates/variables.ejs to your chosen package name

### Register byte-buddy in IntelliJ

jMolecules equips aggregates with required JPA annotations by executing the `byte-buddy` Maven plugin. To make sure 
that these annotations are also available when editing and running code from your IDE, you have to register the plugin 
to your `build` and `rebuild` commands. For IntelliJ, this is done as follows:

1) After opening the project in IntelliJ, right click on pom.xml and select 'Add as Maven Project'.
2) Open the Maven tool window (found on the right) and find the list of plugins included in the project. Expand 
   `byte-buddy`, right click on transform-extended, and select 'Execute After Build' and 'Execute After Rebuild'.

### Install hygen for scaffolding

The project contains templates to scaffold elements of tactical domain-driven design such as aggregates, repositories, 
or domain events in a consistent fashion. To make use of this tool, install NodeJS and then run the following command 
to install `hygen`.

`npm i -g hygen`

In the folder `_templates`, replace the package `com/example/app` in all `*.java.t` files with your own package name.

## Scaffolding your DDD artifacts

The scaffolding promotes a naming strategy where the name of a repository is simply the plural form of the aggregate, 
e.g. `Orders` for aggregate `Order`. This underlines the aim to employ a purely domain oriented language in the domain 
ring of the application.

The scaffolding implies a package design where top-level packages form modules of the applications, containing directly 
the domain logic. Controllers, on the other hand, which belong to the infrastructure ring of the onion architecture,
are placed in a subpackage `web` close to the domain classes. The approach has been described in this [blog post](https://medium.com/elca-it/feature-based-modular-code-organization-in-java-e4b611d6c103).

To create a new feature module, type this command:

> $ hygen feature new todo

Next, create an aggregate root with a repository and some first commands and events by typing:

> $ hygen aggregate new Todo --feature=todo

If you want to make your aggregate available through the REST API, type:

> $ hygen controller new Todo --feature=todo

## Baked-in concepts

This project follows an opinionated approach to building DDD-style applications in Java. It is highly inspired by 
Oliver Drotbohm's [Spring Restbucks](https://github.com/odrotbohm/spring-restbucks) sample application. The approach
implemented here more strictly separates different rings of the onion architecture, placing for example every controller 
in a package in the infrastructure layer, avoiding in the domain package dependencies to Jackson and other concepts 
related to the REST API, and explicitly introducing a package for application and infrastructure rings of the onion.

### Package structure

The generator adheres to the following package structure, which is largely governed by the name of 
features and of aggregates:
<pre>
&lt;root-package&gt;/
  domain/                              Domain ring of the applicataion
    common/                              Root package for common functionality
      exception/                           Basic exception types
      logging/                             Helpers to work with log prefixes
      model/                               Basic types that can be used everywhere
    &lt;feature1&gt;/                          Root package of a featute
      Sample.java                          An aggregate, here with name "Sample"
      SampleCommand.java                   Commands to change the aggregate
      SampleEvent.java                     Domain events produced by the aggregate
      Samples.java                         Repository to work with the aggregate
      web/                                 Root package of the feature's REST API
        SampleOperationsController.java      Controller exposing operations of the aggregate
        SampleSummary.java                   Projection with reduced data suitable to render lists
        SampleDetail.java                    Projection with detailed data to render detail views
        SampleLinks.java                     Factory for HAL links of the aggregate
    &lt;feature2&gt;/                        Root package of another feature
      ...
  application/                         Application ring of the application
  infrastructure/                      Infrastructure ring of the application
    &lt;api1&gt;/                              Implementation of an API to another system
    &lt;api2&gt;/                              Implementation of another API to another system
    ...
    demo/                                Demo data creation suitable for development
    logging/                             Logging configuration 
    persistence/                         Persistence configuration
    security/                            Security configuration
    web/                                 REST API of the application
      SampleApiConfiguration.java          Configuration for link generation for projections
      ...
</pre>

### Persistence

Aggregates are directly persisted to a relational database through JPA / Hibernate. To avoid jeopardizing the domain 
model with persistence logic, Aggregates rely on jMolecules `byte-buddy` plugin to generated required annotations.
The initial schema is created through Flyway.

### REST API with Links in HAL format

The REST API is largely provided out of the box by Spring Data REST. Its converters allow to reference aggregates through their URI. If exposed through `@RepositoryRestResource`, finder methods of a repository are published under the search resource of an aggregate collection.

Through configuration, http methods to create, update or patch an aggregate are disabled in favor of using well-defined operations for creating and changing aggregates, forcing them to go through the business logic implemented in the domain layer. 

#### Summary and detail projections

It is a common scenario that an application displays a list of aggregates, with a detail view of a single 
aggregate if the user clicks on an item in the list. Both use cases, list view and detail view, have a different needs 
in terms of data required to display the respective view. The list view typically only needs the name of the aggregate 
and some additional fields such as the status of each aggregate. The detail view, on the other hand, typically requires 
all fields. To display an editor which allows to edit an aggregate instance, even more data such as values for 
dropdowns would be desirable.

To make the interaction of the GUI with the backend efficient, Spring allows defining _projections_ of an aggregate to 
return different representations depending on the use case.The hygen _controller_ generator produces two projection 
interfaces, `<AggregateName>Summary` and `<AggregateName>Detail`. It is then up to the developer to equip each with the 
required getters. Please note that projections are a concept of Spring Data and therefore have an impact how data is
fetched from the database. See the documentation [here](https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html).

A projection can be fetched by appending a `projection` query parameter in the call to the aggregate resource, e.g.
```bash
GET /api/samples?project=summary
```

Spring allows adding arbitrary data to a projection through the use of Spring's `@Value` annotation. In the following example,
the `detail` projection resolves the association to the owner with the actual `Person` aggregate through access to the 
`people` repository. I.e. the `@` prefix allows to access any bean in the Spring context, making projections really 
powerful.

```java
@Projection(name = "detail", types = {Sample.class})
public interface SampleDetail {

    String getName();

    String getDescription();

    @Value("#{@people.resolveRequired(target.owner)}")
    Person getOwner();
}
```
In sum, projections act as highly customizable data transfer objects, seamlessly integrated with the application's REST API.

#### HAL link generation

Spring MVC and Spring Data REST are based on the hypertext application language [HAL](https://www.ietf.org/archive/id/draft-kelly-json-hal-11.html). 
It basically allows to attach hyperlinks to the representation of a REST resource. If you do a GET request to the API 
root, Spring returns an object with the field "_links", pointing at the aggregates served by the application:

```bash
$ curl user:1234@localhost:8080/api
Enter host password for user 'user':
```

```json
{
  "_links" : {
    "persons" : {
      "href" : "http://localhost:8080/api/people"
    },
    "samples" : {
      "href" : "http://localhost:8080/api/samples"
    },
    "profile" : {
      "href" : "http://localhost:8080/api/profile"
    }
  }
}
```

When accessing a single aggregate, Spring shows the fields of the aggregate, plus two default links:
```json
{
  "name" : "Sample 1",
  "description" : "Description of sample 1",
  "state" : "DRAFT",
  "_links" : {
    "self" : {
      "href" : "http://localhost:8080/api/samples/c30cb331-5aee-4e00-99d0-17b9141ee5c1"
    },
    "sample" : {
      "href" : "http://localhost:8080/api/samples/c30cb331-5aee-4e00-99d0-17b9141ee5c1"
    }
  }
}
```

We can now use the same mechanism to communicate to the client of the API which business operations (i.e. commands) are
currently available depending on the aggregate state and the user role. The convention is to produce a HAL link for each
available operation and let it point at the path served by the controller to execute the command. The logic
to produce the links is placed in a class `<AggregateName>Links` in the web API package of the feature

For the sample aggregate, this looks like this:
```java
public class SampleLinks implements RepresentationModelProcessor<EntityModel<Sample>> {

    private final EntityLinks entityLinks;

    @Override
    public EntityModel<Sample> process(EntityModel<Sample> model) {
        if (model.getContent() instanceof Sample sample) {
            addOperationLink(model, sample, Sample.Operation.UPDATE);
            addOperationLink(model, sample, Sample.Operation.PUBLISH);
        }
        return model;
    }

    private void addOperationLink(EntityModel<Sample> model, Sample sample, Sample.Operation operation) {
        model.addIf(sample.can(Sample.Operation.UPDATE), () -> entityLinks
                .linkFor(Sample.class).slash(operation.rel)
                .withRel(operation.rel));
    }
}
```
Note how Spring's entity links comes in handy to resolve the path annotated on the `SampleOperationsController`. A link
is only produced if the Sample's `can` method allows for this operation. The check could be extended here by checking 
the user's role, allowing the publish operation for example only for a user with role `ADMIN`.

Projections of the aggregate (see above) do not automatically pick up the links generated for the aggregate. The 
`ProjectionLinks` helper class can be used to declare a link generator for the projection which delegates to the link 
generator for the aggregate:

```java
@Configuration
public class SampleApiConfiguration {

   @Bean
   ProjectionLinks<Sample> sampleSummaryLinks(SampleLinks delegate) {
      return new ProjectionLinks<>(delegate, Sample.class);
   }

}
```
With this, projections have now exactly the same links as the raw aggregate.

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