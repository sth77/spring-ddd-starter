---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.inflection.pluralize(Name) %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import <%= CommonPackage %>.model.AggregateRepository;

/**
 * The repository of the {@link <%= AggregateType %>} aggregates, named after their plural. Spring Data REST exports it read-only; an
 * aggregate changes through {@link #doWith}, which applies an operation and saves the aggregate in one transaction.
 */
public interface <%= RepositoryType %> extends AggregateRepository<<%= AggregateType %>, <%= IdType %>> {

    @Override
    default Class<<%= AggregateType %>> getAggregateType() {
        return <%= AggregateType %>.class;
    }

}
