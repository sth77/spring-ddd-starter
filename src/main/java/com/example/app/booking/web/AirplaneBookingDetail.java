package com.example.app.booking.web;

import com.example.app.booking.AirplaneBooking;
import org.springframework.data.rest.core.config.Projection;

/**
 * Detail projection of a {@link AirplaneBooking}. Can be used by clients of the API to get all data of the
 * aggregate suitable for rendering a detail view.
 */
@Projection(name = "detail", types = {AirplaneBooking.class})
public interface AirplaneBookingDetail {

    String getName();

    // TODO: declare other fields to show in the detail projection.
    // You can load related entities or parts of it using a Spring expression (SPEL) like this:
    // @Value("#{@people.resolveRequired(target.owner).getName()}")
    // String getOwner();

}
