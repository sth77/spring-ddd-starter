---
to: src/main/java/com/example/app/<%= feature %>/web/<%= Name %>CollectionLinks.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= CommonPackage %>.model.AggregateCommands;
import <%= FeaturePackage %>.<%= AggregateType %>;
import <%= FeaturePackage %>.<%= CommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
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
    private final AggregateCommands<<%= AggregateType %>, <%= CommandType %>> aggregateCommands =
            new AggregateCommands<>(<%= AggregateType %>.class, <%= CommandType %>.class);

    @Override
    public CollectionModel<EntityModel<<%= AggregateType %>>> process(CollectionModel<EntityModel<<%= AggregateType %>>> model) {
        final var rel = aggregateCommands.getRel(<%= CreateCommandType %>.class);
        return model.add(entityLinks.linkFor(<%= AggregateType %>.class).slash(rel).withRel(rel));
    }
}
