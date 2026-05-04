package com.example.app.airplane;

public sealed interface AirplaneCommand {

    record Register(InventoryCode inventoryCode) implements AirplaneCommand {
    }

    record CompleteMaintenance(DocumentId maintenanceReport) implements AirplaneCommand {}

    record TakeOutOfService() implements AirplaneCommand {
    }

    record Decommission() implements AirplaneCommand {
    }
}
