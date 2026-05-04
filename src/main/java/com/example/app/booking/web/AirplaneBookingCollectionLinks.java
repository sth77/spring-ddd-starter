package com.example.app.booking.web;

import com.example.app.common.model.AggregateCommands;
import com.example.app.booking.AirplaneBooking;

import com.example.app.booking.AirplaneBookingCommand;
import com.example.app.booking.AirplaneBookingCommand.CreateAirplaneBooking;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AirplaneBookingCollectionLinks implements RepresentationModelProcessor<CollectionModel<EntityModel<AirplaneBooking>>> {

    private final EntityLinks entityLinks;
    private final AggregateCommands<AirplaneBooking, AirplaneBookingCommand> aggregateCommands = new AggregateCommands<>(AirplaneBooking.class, AirplaneBookingCommand.class);

    @Nonnull
    @Override
    public CollectionModel<EntityModel<AirplaneBooking>> process(CollectionModel<EntityModel<AirplaneBooking>> model) {
        return model.add(entityLinks.linkFor(AirplaneBooking.class).withRel(aggregateCommands.getRel(CreateAirplaneBooking.class)));
    }

}
