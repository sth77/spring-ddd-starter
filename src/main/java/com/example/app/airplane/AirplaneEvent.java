package com.example.app.airplane;

import org.jmolecules.event.types.DomainEvent;

public sealed interface AirplaneEvent
        extends DomainEvent {

    record AirplaneRegistered(
            Airplane.Id airplaneId,
            InventoryCode inventoryCode) implements AirplaneEvent {
    }
    record AirplaneMaintenanceCompleted(
            Airplane.Id airplaneId,
            DocumentId maintenanceReport
            ) implements AirplaneEvent {
    }
    record AirplaneAvailabilityChanged(
            Airplane.Id airplaneId,
            Airplane.Status status) implements AirplaneEvent {
    }
}
