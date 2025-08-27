---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.inflection.pluralize(Name) %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import <%= CommonPackage %>.model.AggregateRepository;

public interface <%= RepositoryType %> extends AggregateRepository<<%= AggregateType %>, <%= IdType %>> {

    @Override
    default Class<<%= AggregateType %>> getAggregateType() {
        return <%= AggregateType %>.class;
    }

}
