package com.example.app.booking;

import com.example.app.common.model.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public sealed interface AirplaneBookingCommand extends Command {

    @Builder
    record CreateAirplaneBooking(
        @NotBlank String name
        // TODO add the fields required to create the aggregate
        ) implements AirplaneBookingCommand { }

    @Builder
    record UpdateAirplaneBooking(
        @NotBlank String name) implements AirplaneBookingCommand { }

    record PublishAirplaneBooking() implements AirplaneBookingCommand { }

}
