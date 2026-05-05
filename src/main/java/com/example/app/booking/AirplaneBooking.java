package com.example.app.booking;

import com.example.app.airplane.Airplane;
import com.example.app.common.model.AbstractAggregate;
import com.example.app.common.model.DomainException;
import com.example.app.booking.AirplaneBooking.AirplaneBookingId;
import com.example.app.booking.AirplaneBookingCommand.CreateAirplaneBooking;
import com.example.app.booking.AirplaneBookingCommand.UpdateAirplaneBooking;
import com.example.app.booking.AirplaneBookingCommand.PublishAirplaneBooking;
import com.example.app.booking.AirplaneBookingEvent.AirplaneBookingPublished;
import com.example.app.booking.AirplaneBookingEvent.AirplaneBookingCreated;
import com.example.app.booking.AirplaneBookingEvent.AirplaneBookingUpdated;
import com.example.app.booking.AirplaneBookingEvent.AirplaneBookingCancelled;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.modulith.events.ApplicationModuleListener;

import java.util.Objects;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AirplaneBooking extends AbstractAggregate<AirplaneBooking, AirplaneBookingId> implements AggregateRoot<AirplaneBooking, AirplaneBookingId> {

    private final AirplaneBookingId id;
    private String name;
    private AirplaneBookingState state;
    private Airplane.Id airplaneId;

    public static AirplaneBooking create(CreateAirplaneBooking data, Airplane.Id airplaneId) {
        val result = new AirplaneBooking(
            AirplaneBookingId.random(),
            data.name(),
            AirplaneBookingState.DRAFT,
            airplaneId);
        result.registerEvent(new AirplaneBookingCreated(result.id));
        return result;
    }
    @ApplicationModuleListener
    public void update(UpdateAirplaneBooking data) {
        assertCan(data.getClass());
        if (!Objects.equals(this.name, data.name())) {
            this.name = data.name();
            registerEvent(new AirplaneBookingUpdated(id, name));
        }
    }

    public void publish() {
        assertCan(PublishAirplaneBooking.class);
        state = AirplaneBookingState.PUBLISHED;
        registerEvent(new AirplaneBookingPublished(id, name));
    }

    public void assignAirplane(Airplane.Id newAirplaneId) {
        if (state == AirplaneBookingState.CANCELLED) {
            throw new DomainException("Cannot assign airplane to cancelled booking");
        }
        this.airplaneId = newAirplaneId;
    }

    public void cancel() {
        if (state == AirplaneBookingState.CANCELLED) {
            throw new DomainException("AirplaneBooking is already cancelled");
        }
        state = AirplaneBookingState.CANCELLED;
        registerEvent(new AirplaneBookingCancelled(id));
    }

    private void assertCan(Class<? extends AirplaneBookingCommand> command) {
        if (!can(command)) {
            throw new DomainException("Command %s not allowed for airplaneBooking in state %s"
                    .formatted(command.getSimpleName(), state));
        }
    }

    public boolean can(Class<? extends AirplaneBookingCommand> operation) {
        if (operation.equals(CreateAirplaneBooking.class)) {
            return false;
        }
        return state != AirplaneBookingState.PUBLISHED && state != AirplaneBookingState.CANCELLED;
    }

    public record AirplaneBookingId(@JsonValue UUID uuidValue) implements Identifier {

        public static AirplaneBookingId random() {
            return AirplaneBookingId.of(UUID.randomUUID());
        }

        public static AirplaneBookingId of(UUID uuidValue) {
            return new AirplaneBookingId(uuidValue);
        }

        @Override
        public String toString() {
            return uuidValue.toString();
        }
    }

    public enum AirplaneBookingState {
        DRAFT,
        PUBLISHED,
        CANCELLED
    }

}
