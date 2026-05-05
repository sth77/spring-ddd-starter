package com.example.app.booking.web;

import com.example.app.booking.AirplaneBooking;
import com.example.app.booking.AirplaneBooking.AirplaneBookingId;
import com.example.app.booking.AirplaneBookings;
import com.example.app.booking.AirplaneBookingCommand.CreateAirplaneBooking;
import com.example.app.booking.AirplaneBookingCommand.UpdateAirplaneBooking;
import com.example.app.booking.app.AirplaneBookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.function.Consumer;

/**
 * Extends the REST controller provided by Spring Data REST with aggregate specific operations.
 */
@Transactional
@RequiredArgsConstructor
@RepositoryRestController
@ExposesResourceFor(AirplaneBooking.class)
@SecurityRequirement(name = "basicAuth")
public class AirplaneBookingCommandController {

	private final AirplaneBookings airplaneBookings;
    private final AirplaneBookingService airplaneBookingService;

    @Secured("ROLE_USER")
    @PostMapping("/airplaneBookings")
    public ResponseEntity<EntityModel<AirplaneBooking>> create(@Valid @RequestBody CreateAirplaneBooking data) {
        val result = airplaneBookingService.create(data);
        return ResponseEntity.ok(EntityModel.of(result));
    }

    @Secured("ROLE_USER")
	@PostMapping(path = "/airplaneBookings/{airplaneBookingId}/update")
	public ResponseEntity<EntityModel<AirplaneBooking>> update(@PathVariable AirplaneBookingId airplaneBookingId, @Valid @RequestBody UpdateAirplaneBooking data) {
		return doWithAirplaneBooking(airplaneBookingId, it -> it.update(data));
	}

    @Secured("ROLE_ADMIN")
	@PostMapping(path = "/airplaneBookings/{airplaneBookingId}/publish")
	public ResponseEntity<EntityModel<AirplaneBooking>> publish(@PathVariable AirplaneBookingId airplaneBookingId) {
		return doWithAirplaneBooking(airplaneBookingId, AirplaneBooking::publish);
	}

	private ResponseEntity<EntityModel<AirplaneBooking>> doWithAirplaneBooking(AirplaneBookingId airplaneBookingId, Consumer<AirplaneBooking> action) {
		return airplaneBookings.doWith(airplaneBookingId, action)
				.map(EntityModel::of)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

}
