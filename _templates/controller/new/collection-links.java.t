---
to: src/main/java/com/example/app/<%= feature %>/web/<%= Name %>CollectionLinks.java
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
                () -> entityLinks.linkFor(<%= AggregateType %>.class).withRel(aggregateCommands.getRel(<%= CreateCommandType %>.class)));
    }

}
