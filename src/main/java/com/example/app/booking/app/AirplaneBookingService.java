package com.example.app.booking.app;

import com.example.app._application.RetryableApplicationModuleListener;
import com.example.app.airplane.Airplane;
import com.example.app.airplane.AirplaneEvent.AirplaneAvailabilityChanged;
import com.example.app.airplane.Airplanes;
import com.example.app.booking.AirplaneBooking;
import com.example.app.booking.AirplaneBooking.AirplaneBookingState;
import com.example.app.booking.AirplaneBookingCommand.CreateAirplaneBooking;
import com.example.app.booking.AirplaneBookings;
import com.example.app.common.model.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AirplaneBookingService {

    private final AirplaneBookings airplaneBookings;
    private final Airplanes airplanes;

    public AirplaneBooking create(CreateAirplaneBooking command) {
        var available = findAvailableAirplane()
            .orElseThrow(() -> new DomainException("No available airplane for booking"));
        return airplaneBookings.save(AirplaneBooking.create(command, available.getId()));
    }

    @RetryableApplicationModuleListener
    void on(AirplaneAvailabilityChanged event) {
        if (event.status() != Airplane.Status.OUT_OF_SERVICE) {
            return;
        }
        airplaneBookings.findActiveByAirplaneId(event.airplaneId()).ifPresent(booking -> {
            var replacement = findAvailableAirplane();
            if (replacement.isPresent()) {
                booking.assignAirplane(replacement.get().getId());
            } else {
                booking.cancel();
            }
            airplaneBookings.save(booking);
        });
    }

    private Optional<Airplane> findAvailableAirplane() {
        var assignedAirplaneIds = airplaneBookings.findAll().stream()
            .filter(b -> b.getState() != AirplaneBookingState.CANCELLED)
            .map(AirplaneBooking::getAirplaneId)
            .collect(Collectors.toSet());

        return airplanes.findAll().stream()
            .filter(a -> a.getStatus() == Airplane.Status.READY)
            .filter(a -> !assignedAirplaneIds.contains(a.getId()))
            .findFirst();
    }

}
