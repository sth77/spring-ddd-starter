---
to: src/main/java/com/example/app/domain/<%= feature %>/web/<%= Name %>Links.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.springframework.hateoas.IanaLinkRelations.SELF;

@Component
@RequiredArgsConstructor
public class <%= AggregateType %>Links implements RepresentationModelProcessor<EntityModel<<%= AggregateType %>>> {

    public static final String REL_CREATE = "create";

    private final EntityLinks entityLinks;

    @NonNull
    @Override
    public EntityModel<<%= AggregateType %>> process(EntityModel<<%= AggregateType %>> model) {
        if (model.getContent() instanceof <%= AggregateType %> <%= aggregateName %>) {
            model.addIf(!model.hasLink(SELF),
                    () -> entityLinks.linkToItemResource(<%= AggregateType %>.class, <%= aggregateName %>.getId()).withSelfRel());
            Arrays.stream(<%= AggregateType %>.Operation.values())
                    .forEach(operation -> addOperationLink(model, <%= aggregateName %>, operation));
        }
        return model;
    }

    private void addOperationLink(EntityModel<<%= AggregateType %>> model, <%= AggregateType %> <%= aggregateName %>, <%= AggregateType %>.Operation operation) {
        model.addIf(!model.hasLink(operation.key) && <%= aggregateName %>.can(<%= AggregateType %>.Operation.UPDATE_NAME), () -> entityLinks
                .linkForItemResource(<%= AggregateType %>.class, <%= aggregateName %>.getId()).slash(operation.key)
                .withRel(operation.key));
    }

}
