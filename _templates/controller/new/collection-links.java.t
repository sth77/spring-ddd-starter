---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/web/<%= Name %>CollectionLinks.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= CommonPackage %>.web.SecuredAggregateCommands;
import <%= FeaturePackage %>.<%= AggregateType %>;

import <%= FeaturePackage %>.<%= CommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static <%= CommonPackage %>.web.CommandLinks.afford;

/**
 * Offers the creation of a {@link <%= AggregateType %>} on the collection resource: a link with the HAL-FORMS template of
 * {@link <%= CreateCommandType %>}, provided the current user's role permits it.
 */
@Component
@RequiredArgsConstructor
public class <%= AggregateType %>CollectionLinks implements RepresentationModelProcessor<CollectionModel<EntityModel<<%= AggregateType %>>>> {

    private final EntityLinks entityLinks;
    private final SecuredAggregateCommands<<%= AggregateType %>, <%= CommandType %>> aggregateCommands = new SecuredAggregateCommands<>(<%= AggregateType %>.class, <%= CommandType %>.class, <%= ControllerType %>.class);

    @Nonnull
    @Override
    public CollectionModel<EntityModel<<%= AggregateType %>>> process(CollectionModel<EntityModel<<%= AggregateType %>>> model) {
        return model.addIf(
                aggregateCommands.isAllowedForCurrentUser(<%= CreateCommandType %>.class),
                () -> afford(
                        entityLinks.linkFor(<%= AggregateType %>.class).withRel(aggregateCommands.getRel(<%= CreateCommandType %>.class)),
                        <%= CreateCommandType %>.class));
    }

}
