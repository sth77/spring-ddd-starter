package com.example.app._infrastructure.web;

import com.example.app.booking.AirplaneBooking;
import com.example.app.booking.web.AirplaneBookingLinks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AirplaneBookingApiConfiguration {

    /**
     * Ensures that projections are equipped with the same links as the aggregate.
     */
    @Bean
    ProjectionLinks<AirplaneBooking> airplaneBookingProjectionLinks(AirplaneBookingLinks delegate) {
        return new ProjectionLinks<>(delegate, AirplaneBooking.class);
    }

}
