package com.example.app.booking.web;

import com.example.app.common.model.AggregateCommands;
import com.example.app.booking.AirplaneBooking;

import com.example.app.booking.AirplaneBookingCommand;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.IanaLinkRelations.SELF;

@Component
@RequiredArgsConstructor
public class AirplaneBookingLinks implements RepresentationModelProcessor<EntityModel<AirplaneBooking>> {

   private final EntityLinks entityLinks;
   private final AggregateCommands<AirplaneBooking, AirplaneBookingCommand> aggregateCommands = new AggregateCommands<>(AirplaneBooking.class, AirplaneBookingCommand.class);

   @Override
   public EntityModel<AirplaneBooking> process(EntityModel<AirplaneBooking> model) {
      if (model.getContent() instanceof AirplaneBooking airplaneBooking) {
         aggregateCommands.getCommands().forEach(
                 command -> addCommandLink(model, airplaneBooking, command));
         model.addIf(!model.hasLink(SELF),
                 () -> entityLinks.linkForItemResource(AirplaneBooking.class, airplaneBooking.getId()).withSelfRel());
      }
      return model;
   }

   private void addCommandLink(
           EntityModel<AirplaneBooking> model,
           AirplaneBooking airplaneBooking,
           Class<? extends AirplaneBookingCommand> commandType) {
      val rel = aggregateCommands.getRel(commandType);
      model.addIf(airplaneBooking.can(commandType), () -> entityLinks
              .linkForItemResource(AirplaneBooking.class, airplaneBooking.getId()).slash(rel)
              .withRel(rel));
   }

}
