package com.example.app.booking.web;

import com.example.app.booking.AirplaneBooking;
import org.springframework.data.rest.core.config.Projection;

/**
 * Summary projection of a {@link AirplaneBooking}. Can be used by clients of the API to get a compact
 * representation of the aggregate suitable for rendering it in a list view.
 */
@Projection(name = "summary", types = {AirplaneBooking.class})
public interface AirplaneBookingSummary {

    String getName();

    // TODO: declare other fields to show in the summary.
    // You can load related entities or parts of it using a Spring expression (SPEL) like this:
    // @Value("#{@people.resolveRequired(target.owner).getName()}")
    // String getOwner();

}
