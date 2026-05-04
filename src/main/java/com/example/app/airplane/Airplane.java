package com.example.app.airplane;

import com.example.app.common.model.DomainException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.example.app.airplane.Airplane.Status.DECOMMISSIONED;
import static com.example.app.airplane.Airplane.Status.OUT_OF_SERVICE;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(of = "id")
public class Airplane {
    private Id id;
    private Status status;
    private InventoryCode inventoryCode;
    private List<MaintenanceReport> maintenanceReports;

    public static Airplane register(InventoryCode inventoryCode) {
        return new Airplane(
                Id.random(),
                OUT_OF_SERVICE,
                inventoryCode,
                new ArrayList<>());
    }

    void completeMaintenance(DocumentId maintenanceReport) {
        if (status != OUT_OF_SERVICE) {
            throw new DomainException("Airplane must be out of service to complete maintenance.");
        }
        if (maintenanceReports.stream()
                .anyMatch(report -> report.getDocumentId().equals(maintenanceReport))) {
            throw new DomainException("Maintenance report with document ID " + maintenanceReport.stringValue() + " already exists for this airplane.");
        }
        if (maintenanceReports.stream().anyMatch(report -> report.getConfirmedAt() == null)) {
            throw new DomainException("There is already an unconfirmed maintenance report for this airplane. Please confirm it before adding a new one.");
        }
        maintenanceReports.add(MaintenanceReport.create(maintenanceReport));
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
    }

    public record Id(UUID id) {
        static Id random() {
            return new Id(UUID.randomUUID());
        }
    }

    public record InventoryCode(String stringValue) {
        static InventoryCode of(String stringValue) {
            return new InventoryCode(stringValue);
        }
    }

    public enum Status {
        OUT_OF_SERVICE,
        READY,
        DECOMMISSIONED
    }
}


