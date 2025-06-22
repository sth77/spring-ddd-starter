---
to: src/main/java/com/example/app/domain/<%= feature %>/web/<%= Name %>CollectionLinks.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= CommonPackage %>.model.AggregateCommands;
import <%= FeaturePackage %>.<%= AggregateType %>;

import <%= FeaturePackage %>.<%= CommandType %>;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class <%= AggregateType %>CollectionLinks implements RepresentationModelProcessor<EntityModel<<%= AggregateType %>>> {

   private final EntityLinks entityLinks;
   private final AggregateCommands<<%= AggregateType %>, <%= CommandType %>> aggregateCommands = new AggregateCommands<>(<%= AggregateType %>.class, <%= CommandType %>.class);

   @Override
   public EntityModel<<%= AggregateType %>> process(EntityModel<<%= AggregateType %>> model) {
      if (model.getContent() instanceof <%= AggregateType %> <%= aggregateName %>) {
         aggregateCommands.getCommands().forEach(
                 command -> addCommandLink(model, <%= aggregateName %>, command));
      }
      return model;
   }

   private void addCommandLink(
           EntityModel<<%= AggregateType %>> model,
           <%= AggregateType %> <%= aggregateName %>,
           Class<? extends <%= CommandType %>> commandType) {
      val rel = aggregateCommands.getRel(commandType);
      model.addIf(<%= aggregateName %>.can(commandType), () -> entityLinks
              .linkFor(<%= AggregateType %>.class).slash(rel)
              .withRel(rel));
   }

}
