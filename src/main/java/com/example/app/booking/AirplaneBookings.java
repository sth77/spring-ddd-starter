package com.example.app.booking;

import com.example.app.booking.AirplaneBooking.AirplaneBookingId;
import com.example.app.common.model.AggregateRepository;

public interface AirplaneBookings extends AggregateRepository<AirplaneBooking, AirplaneBookingId> {

    @Override
    default Class<AirplaneBooking> getAggregateType() {
        return AirplaneBooking.class;
    }

}
