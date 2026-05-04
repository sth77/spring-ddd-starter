package com.example.app.airplane;

import com.example.app.airplane.AirplaneCommand.CompleteMaintenance;
import com.example.app.common.model.DomainException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.example.app.airplane.Airplane.Status.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(of = "id")
public class Airplane
        extends AbstractAggregateRoot<Airplane>
        implements AggregateRoot<Airplane, Airplane.Id> {
    private Id id;
    private Status status;
    private InventoryCode inventoryCode;
    private List<MaintenanceReport> maintenanceReports;

    public static Airplane register(InventoryCode inventoryCode) {
        Airplane airplane = new Airplane(
                Id.random(),
                OUT_OF_SERVICE,
                inventoryCode,
                new ArrayList<>());
        airplane.registerEvent(new AirplaneEvent.AirplaneRegistered(
                airplane.getId(),
                airplane.getInventoryCode()));
        return airplane;
    }



    void completeMaintenance(CompleteMaintenance data) {
        can(CompleteMaintenance.class);
        DocumentId maintenanceReport = data.maintenanceReport();
        if (maintenanceReports.stream()
                .anyMatch(report -> report.getDocumentId().equals(maintenanceReport))) {
            throw new DomainException("Maintenance report with document ID " + maintenanceReport.stringValue() + " already exists for this airplane.");
        }
        if (maintenanceReports.stream().anyMatch(report -> report.getConfirmedAt() == null)) {
            throw new DomainException("There is already an unconfirmed maintenance report for this airplane. Please confirm it before adding a new one.");
        }
        maintenanceReports.add(MaintenanceReport.create(maintenanceReport));
        registerEvent(new AirplaneEvent.AirplaneMaintenanceCompleted(
                    this.getId(),
                    maintenanceReport));
    }

    List<MaintenanceReport> getMaintenanceReports() {
        return Collections.unmodifiableList(maintenanceReports);
    }

    void confirmMaintenance() {
        MaintenanceReport lastReport = maintenanceReports.stream()
                .filter(report -> report.getConfirmedAt() == null)
                .findFirst()
                .orElseThrow(() -> new DomainException("There is no unconfirmed maintenance report for this airplane."));
        lastReport.confirm();
    }

    void takeOutOfService() {
        if (status == DECOMMISSIONED) {
            throw new DomainException("Airplane is already decommissioned and cannot be taken out of service.");
        }
        this.status = OUT_OF_SERVICE;
        registerEvent(new AirplaneEvent.AirplaneAvailabilityChanged(
                this.getId(),
                this.getStatus()));
    }

    public record Id(UUID id) implements Identifier {
        static Id random() {
            return new Id(UUID.randomUUID());
        }
    }

    public enum Status {
        OUT_OF_SERVICE,
        READY,
        DECOMMISSIONED
    }

    private void assertCan(Class<? extends AirplaneCommand> commandClass) {
        if (!can(commandClass)) {
            throw new DomainException("Command " + commandClass.getSimpleName() + " cannot be executed in current state: " + status);
        }
    }

    public boolean can(Class<? extends AirplaneCommand> commandClass) {
        if (status == DECOMMISSIONED) { return false; }
        if (commandClass.equals(CompleteMaintenance.class)) {
            return status == OUT_OF_SERVICE;
        }
        if (commandClass.equals(AirplaneCommand.TakeOutOfService.class)) {
            return status == READY;
        }
        return true;
    }
}


