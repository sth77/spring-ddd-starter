---
to: src/main/java/com/example/app/<%= h.changeCase.lower(name) %>/<%= h.inflection.pluralize(Name) %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import java.util.Optional;
import java.util.function.Consumer;

import org.jmolecules.ddd.types.Repository;
import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.util.Streamable;
import org.springframework.transaction.annotation.Transactional;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;

public interface <%= RepositoryType %> extends Repository<<%= AggregateType %>, <%= IdType %>>, AssociationResolver<<%= AggregateType %>, <%= IdType %>> {
    
    <%= AggregateType %> save(<%= AggregateType %> <%= aggregateName %>);

    Optional<<%= AggregateType %>> findById(<%= IdType %> id);

    Streamable<<%= AggregateType %>> findAll();
    
    int count();

    /**
     * Applies the operation to the <%= AggregateType %> and saves it.
     * @param id the ID of the <%= AggregateType %>
     * @param operation the operation to apply to the <%= aggregateName %>
     * @return the <%= aggregateName %> or empty if not found
     */
    @Transactional
    default Optional<<%= AggregateType %>> doWith(<%= IdType %> id, Consumer<<%= AggregateType %>> operation) {
        return findById(id)
                .map(it -> {
                    operation.accept(it);
                    return it;
                })
                .map(this::save);
    }

}
