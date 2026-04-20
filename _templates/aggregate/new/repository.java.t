---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= h.inflection.pluralize(Name) %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import <%= CommonPackage %>.model.AggregateRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "<%= collectionRel %>", path = "<%= collectionRel %>")
public interface <%= RepositoryType %> extends AggregateRepository<<%= AggregateType %>, <%= IdType %>> {

    @Override
    default Class<<%= AggregateType %>> getAggregateType() {
        return <%= AggregateType %>.class;
    }

}
