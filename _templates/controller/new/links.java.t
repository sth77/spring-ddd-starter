---
to: src/main/java/com/example/app/domain/<%= name %>/web/<%= Name %>Links.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>;

import <%= InfrastructureWebPackage %>.ProjectionLinks;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class <%= AggregateType %>Links implements RepresentationModelProcessor<EntityModel<<%= AggregateType %>>> {

    private final EntityLinks entityLinks;

    @NonNull
    @Override
    public EntityModel<<%= AggregateType %>> process(EntityModel<<%= AggregateType %>> model) {
        if (model.getContent() instanceof <%= AggregateType %> <%= aggregateName %>) {
            addOperationLink(model, <%= aggregateName %>, <%= AggregateType %>.Operation.UPDATE_NAME);
        }
        return model;
    }

    private void addOperationLink(EntityModel<<%= AggregateType %>> model, <%= AggregateType %> <%= aggregateName %>, <%= AggregateType %>.Operation operation) {
        model.addIf(!model.hasLink(operation.rel) && <%= aggregateName %>.can(<%= AggregateType %>.Operation.UPDATE_NAME), () -> entityLinks
                .linkFor(<%= AggregateType %>.class).slash(operation.rel)
                .withRel(operation.rel));
    }

}
