package com.example.app.booking;

import com.example.app.booking.AirplaneBooking.AirplaneBookingId;
import lombok.Builder;
import org.jmolecules.event.types.DomainEvent;

public sealed interface AirplaneBookingEvent extends DomainEvent {

    AirplaneBookingId airplaneBookingId();

    record AirplaneBookingCreated(
        AirplaneBookingId airplaneBookingId) implements AirplaneBookingEvent {
        static AirplaneBookingCreated of(AirplaneBookingId airplaneBookingId) {
            return new AirplaneBookingCreated(airplaneBookingId);
        }
    }

    @Builder
    record AirplaneBookingUpdated(
        AirplaneBookingId airplaneBookingId,
        String name) implements AirplaneBookingEvent {
    }

    @Builder
    record AirplaneBookingPublished(
        AirplaneBookingId airplaneBookingId,
        String name) implements AirplaneBookingEvent {
    }

}
