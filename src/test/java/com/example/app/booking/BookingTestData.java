package com.example.app.booking;


import com.example.app.airplane.Airplane;
import com.example.app.booking.AirplaneBookingCommand.CreateAirplaneBooking;

import lombok.experimental.UtilityClass;
import lombok.val;

import static com.example.app.AggregateEvents.clearEvents;

@UtilityClass
public class BookingTestData {


    public static AirplaneBooking airplaneBooking() {
        return airplaneBooking("AirplaneBooking X");
    }

    public static AirplaneBooking airplaneBooking(String name) {
        val result = AirplaneBooking.create(CreateAirplaneBooking.builder()
                .name(name)
                .build(),
                Airplane.Id.random());
        clearEvents(result);
        return result;
    }


}
