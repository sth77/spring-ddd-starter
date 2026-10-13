---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/web/<%= Name %>Links.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= CommonPackage %>.web.SecuredAggregateCommands;
import <%= FeaturePackage %>.<%= AggregateType %>;

import <%= FeaturePackage %>.<%= CommandType %>;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static <%= CommonPackage %>.web.CommandLinks.afford;
import static org.springframework.hateoas.IanaLinkRelations.SELF;

/**
 * Renders the operations of a {@link <%= AggregateType %>} as HAL links: one link per command that the aggregate allows in its current
 * state ({@code can}) and the current user's role permits ({@code @Secured} on the operations controller), each
 * carrying the HAL-FORMS template of its command.
 */
@Component
@RequiredArgsConstructor
public class <%= AggregateType %>Links implements RepresentationModelProcessor<EntityModel<<%= AggregateType %>>> {

   private final EntityLinks entityLinks;
   private final SecuredAggregateCommands<<%= AggregateType %>, <%= CommandType %>> aggregateCommands = new SecuredAggregateCommands<>(<%= AggregateType %>.class, <%= CommandType %>.class, <%= ControllerType %>.class);

   @Override
   public EntityModel<<%= AggregateType %>> process(EntityModel<<%= AggregateType %>> model) {
      if (model.getContent() instanceof <%= AggregateType %> <%= aggregateName %>) {
         aggregateCommands.getAllowedCommands().forEach(
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
      model.addIf(<%= aggregateName %>.can(commandType), () -> afford(entityLinks
              .linkForItemResource(<%= AggregateType %>.class, <%= aggregateName %>.getId()).slash(rel)
              .withRel(rel), commandType));
   }

}
