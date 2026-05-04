package com.example.app.booking;

import com.example.app.booking.AirplaneBooking.AirplaneBookingState;
import jakarta.persistence.EntityManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static com.example.app.booking.BookingTestData.airplaneBooking;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AirplaneBookingsTest {

    @Autowired
    AirplaneBookings airplaneBookings;

    @Autowired
    EntityManager entityManager;

    @Test
    void save_validAirplaneBookingGiven_savedToDb() {
        // arrange
        val airplaneBooking = airplaneBooking();
        val initialCount = airplaneBookings.count();

        // act
        airplaneBookings.save(airplaneBooking);

        // assert
        assertThat(airplaneBookings.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void findById_exists_returned() {
        // arrange
        val airplaneBooking = airplaneBooking();
        airplaneBookings.save(airplaneBooking);
        entityManager.flush();
        entityManager.clear();

        // act
        val result = airplaneBookings.findById(airplaneBooking.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result).get().satisfies(actual -> {
            assertThat(actual).isEqualTo(airplaneBooking);
            assertThat(actual).isNotSameAs(airplaneBooking);
            assertThat(actual.getName()).isEqualTo(airplaneBooking.getName());
            assertThat(actual.getState()).isEqualTo(AirplaneBookingState.DRAFT);
        });
    }

}
