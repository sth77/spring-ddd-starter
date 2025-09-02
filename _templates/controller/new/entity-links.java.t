---
to: src/main/java/com/example/app/<%= feature %>/web/<%= Name %>Links.java
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

import static org.springframework.hateoas.IanaLinkRelations.SELF;

@Component
@RequiredArgsConstructor
public class <%= AggregateType %>Links implements RepresentationModelProcessor<EntityModel<<%= AggregateType %>>> {

   private final EntityLinks entityLinks;
   private final AggregateCommands<<%= AggregateType %>, <%= CommandType %>> aggregateCommands = new AggregateCommands<>(<%= AggregateType %>.class, <%= CommandType %>.class);

   @Override
   public EntityModel<<%= AggregateType %>> process(EntityModel<<%= AggregateType %>> model) {
      if (model.getContent() instanceof <%= AggregateType %> <%= aggregateName %>) {
         aggregateCommands.getCommands().forEach(
                 command -> addCommandLink(model, <%= aggregateName %>, command));
         model.addIf(!model.hasLink(SELF),
                 () -> entityLinks.linkForItemResource(<%= AggregateType %>.class, <%= aggregateName %>.getId()).withSelfRel());
      }
      return model;
   }

   private void addCommandLink(
           EntityModel<<%= AggregateType %>> model,
           <%= AggregateType %> <%= aggregateName %>,
           Class<? extends <%= CommandType %>> commandType) {
      val rel = aggregateCommands.getRel(commandType);
      model.addIf(<%= aggregateName %>.can(commandType), () -> entityLinks
              .linkForItemResource(<%= AggregateType %>.class, <%= aggregateName %>.getId()).slash(rel)
              .withRel(rel));
   }

}
