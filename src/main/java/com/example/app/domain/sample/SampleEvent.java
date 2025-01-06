package com.example.app.domain.sample;

import com.example.app.domain.sample.Sample.SampleId;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.jmolecules.event.types.DomainEvent;

public sealed interface SampleEvent extends DomainEvent {

    SampleId sampleId();

    @Builder
    record SampleCreated(
        @NotNull SampleId sampleId) implements SampleEvent {

        static SampleCreated of(SampleId sampleId) {
            return new SampleCreated(sampleId);
        }
    }

    @Builder
    record SampleUpdated(
        @NotNull
        SampleId sampleId,
        @NotNull
        String name,
        @NotNull
        String description) implements SampleEvent {
    }

    @Builder
    record SamplePublished(
            @NotNull
            SampleId sampleId) implements SampleEvent {
    }

}
