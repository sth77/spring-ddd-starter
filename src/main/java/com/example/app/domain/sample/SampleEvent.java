package com.example.app.domain.sample;

import com.example.app.domain.sample.Sample.SampleId;
import org.jmolecules.event.types.DomainEvent;

public sealed interface SampleEvent extends DomainEvent {

    SampleId sampleId();

    record SampleCreated(
            SampleId sampleId) implements SampleEvent {
    }

    record SampleUpdated(
            SampleId sampleId,
            String name,
            String description) implements SampleEvent {
    }

    record SamplePublished(
            SampleId sampleId) implements SampleEvent {
    }

}
