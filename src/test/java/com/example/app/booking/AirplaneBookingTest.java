package com.example.app.booking;

import com.example.app.airplane.Airplane;
import com.example.app.common.model.DomainException;
import com.example.app.booking.AirplaneBookingCommand.CreateAirplaneBooking;
import com.example.app.booking.AirplaneBookingCommand.UpdateAirplaneBooking;
import com.example.app.booking.AirplaneBookingCommand.PublishAirplaneBooking;
import com.example.app.booking.AirplaneBookingEvent.AirplaneBookingCancelled;
import com.example.app.booking.AirplaneBookingEvent.AirplaneBookingCreated;
import com.example.app.booking.AirplaneBookingEvent.AirplaneBookingPublished;
import com.example.app.booking.AirplaneBookingEvent.AirplaneBookingUpdated;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.example.app.AggregateEvents.clearEvents;
import static com.example.app.AggregateEvents.getEvents;
import static com.example.app.booking.AirplaneBooking.AirplaneBookingState.CANCELLED;
import static com.example.app.booking.AirplaneBooking.AirplaneBookingState.DRAFT;
import static com.example.app.booking.AirplaneBooking.AirplaneBookingState.PUBLISHED;
import static com.example.app.booking.BookingTestData.airplaneBooking;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AirplaneBookingTest {

    @Test
    void create_validDataGiven_created() {
        // arrange
        val name = "AirplaneBooking 1";
        val airplaneId = Airplane.Id.random();

        // act
        val airplaneBooking = AirplaneBooking.create(CreateAirplaneBooking.builder()
                .name(name)
                .build(),
                airplaneId);

        // assert
        assertThat(airplaneBooking.getName()).isEqualTo(name);
        assertThat(airplaneBooking.getState()).isEqualTo(DRAFT);
        assertThat(airplaneBooking.getAirplaneId()).isEqualTo(airplaneId);
        assertThat(getEvents(airplaneBooking)).containsExactly(
                AirplaneBookingCreated.of(airplaneBooking.getId()));
    }

    @Test
    void update_validDataGiven_updated() {
        // arrange
        val airplaneBooking = airplaneBooking();
        val updatedName = "AirplaneBooking with updated name";

        // act
        airplaneBooking.update(UpdateAirplaneBooking.builder()
                .name(updatedName)
                .build());

        // assert
        assertThat(airplaneBooking.getName()).isEqualTo(updatedName);
        assertThat(airplaneBooking.getState()).isEqualTo(DRAFT);
        assertThat(getEvents(airplaneBooking)).containsExactly(AirplaneBookingUpdated.builder()
                .airplaneBookingId(airplaneBooking.getId())
                .name(updatedName)
                .build());
    }

    @Test
    void publish_inDraftState_published() {
        // arrange
        val airplaneBooking = airplaneBooking();

        // act
        airplaneBooking.publish();

        // assert
        assertThat(airplaneBooking.getState()).isEqualTo(PUBLISHED);
        assertThat(getEvents(airplaneBooking)).containsExactly(AirplaneBookingPublished.builder()
                .airplaneBookingId(airplaneBooking.getId())
                .name(airplaneBooking.getName())
                .build());
    }

    @Test
    void publish_alreadyPublished_exceptionThrown() {
        // arrange
        val airplaneBooking = airplaneBooking();
        airplaneBooking.publish();
        clearEvents(airplaneBooking);

        // act & assert
        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(airplaneBooking::publish);
    }

    @Test
    void cancel_inDraftState_cancelled() {
        // arrange
        val airplaneBooking = airplaneBooking();

        // act
        airplaneBooking.cancel();

        // assert
        assertThat(airplaneBooking.getState()).isEqualTo(CANCELLED);
        assertThat(getEvents(airplaneBooking)).containsExactly(
                new AirplaneBookingCancelled(airplaneBooking.getId()));
    }

    @Test
    void cancel_alreadyCancelled_exceptionThrown() {
        // arrange
        val airplaneBooking = airplaneBooking();
        airplaneBooking.cancel();
        clearEvents(airplaneBooking);

        // act & assert
        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(airplaneBooking::cancel);
    }

    @Test
    void assignAirplane_newAirplaneAssigned() {
        // arrange
        val airplaneBooking = airplaneBooking();
        val newAirplaneId = Airplane.Id.random();

        // act
        airplaneBooking.assignAirplane(newAirplaneId);

        // assert
        assertThat(airplaneBooking.getAirplaneId()).isEqualTo(newAirplaneId);
    }

    @Test
    void canPublish_inDraftState_trueReturned() {
        // arrange
        val airplaneBooking = airplaneBooking();

        // act & assert
        assertThat(airplaneBooking.can(PublishAirplaneBooking.class)).isTrue();
    }


    @Test
    void canUpdate_inPublishedState_falseReturned() {
        // arrange
        val airplaneBooking = airplaneBooking();
        airplaneBooking.publish();

        // act & assert
        assertThat(airplaneBooking.can(UpdateAirplaneBooking.class)).isFalse();
    }

    @Test
    void canUpdate_inCancelledState_falseReturned() {
        // arrange
        val airplaneBooking = airplaneBooking();
        airplaneBooking.cancel();

        // act & assert
        assertThat(airplaneBooking.can(UpdateAirplaneBooking.class)).isFalse();
    }

}
