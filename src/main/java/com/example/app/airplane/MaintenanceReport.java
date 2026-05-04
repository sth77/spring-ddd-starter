package com.example.app.airplane;

import com.example.app.common.model.DomainException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class MaintenanceReport implements Entity<Airplane, MaintenanceReport.Id> {
    private Id id;
    private DocumentId documentId;
    @Nullable
    private LocalDateTime confirmedAt;

    public static MaintenanceReport create(DocumentId documentId) {
        return new MaintenanceReport(
                Id.random(),
                documentId,
                null);
    }

    void confirm() {
        if (this.confirmedAt != null) {
            throw new DomainException("Maintenance report is already confirmed.");
        }
        this.confirmedAt = LocalDateTime.now();
    }

    public record Id (UUID id) {
        static MaintenanceReport.Id random() {
            return new Id(UUID.randomUUID());
        }
    }
}
