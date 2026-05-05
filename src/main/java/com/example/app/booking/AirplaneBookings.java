package com.example.app.booking;

import com.example.app.airplane.Airplane;
import com.example.app.booking.AirplaneBooking.AirplaneBookingId;
import com.example.app.booking.AirplaneBooking.AirplaneBookingState;
import com.example.app.common.model.AggregateRepository;

import java.util.Optional;

public interface AirplaneBookings extends AggregateRepository<AirplaneBooking, AirplaneBookingId> {

    @Override
    default Class<AirplaneBooking> getAggregateType() {
        return AirplaneBooking.class;
    }

    default Optional<AirplaneBooking> findActiveByAirplaneId(Airplane.Id airplaneId) {
        return findAll().stream()
            .filter(b -> airplaneId.equals(b.getAirplaneId()) && b.getState() != AirplaneBookingState.CANCELLED)
            .findFirst();
    }

}
