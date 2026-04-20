---
to: src/main/java/com/example/app/<%= feature %>/web/<%= Name %>EntityLinks.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= CommonPackage %>.model.AggregateCommands;
import <%= FeaturePackage %>.<%= AggregateType %>;
import <%= FeaturePackage %>.<%= CommandType %>;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class <%= AggregateType %>EntityLinks implements RepresentationModelProcessor<EntityModel<<%= AggregateType %>>> {

    private final EntityLinks entityLinks;
    private final AggregateCommands<<%= AggregateType %>, <%= CommandType %>> aggregateCommands =
            new AggregateCommands<>(<%= AggregateType %>.class, <%= CommandType %>.class);

    @Override
    public EntityModel<<%= AggregateType %>> process(EntityModel<<%= AggregateType %>> model) {
        final var <%= aggregateName %> = model.getContent();
        if (<%= aggregateName %> != null) {
            aggregateCommands.getCommands().forEach(
                    command -> addCommandLink(model, <%= aggregateName %>, command));
            model.addIf(!model.hasLink(IanaLinkRelations.SELF),
                    () -> entityLinks.linkForItemResource(<%= AggregateType %>.class, <%= aggregateName %>.getId()).withSelfRel());
        }
        return model;
    }

    private void addCommandLink(
            EntityModel<<%= AggregateType %>> model,
            <%= AggregateType %> <%= aggregateName %>,
            Class<? extends <%= CommandType %>> commandType) {
        final var rel = aggregateCommands.getRel(commandType);
        model.addIf(<%= aggregateName %>.can(commandType), () -> entityLinks
                .linkForItemResource(<%= AggregateType %>.class, <%= aggregateName %>.getId()).slash(rel)
                .withRel(rel));
    }
}
