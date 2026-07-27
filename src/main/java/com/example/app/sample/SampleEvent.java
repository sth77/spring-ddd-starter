package com.example.app.sample;

import com.example.app.common.model.I18nText;
import com.example.app.sample.Sample.SampleId;
import lombok.Builder;
import org.jmolecules.event.types.DomainEvent;

public sealed interface SampleEvent extends DomainEvent {

    SampleId sampleId();

    record SampleCreated(
            SampleId sampleId) implements SampleEvent {

        public static SampleCreated of(SampleId sampleId) {
            return new SampleCreated(sampleId);
        }
    }

    @Builder
    record SampleUpdated(
            SampleId sampleId,
            I18nText name,
            String description) implements SampleEvent {
    }

    record SamplePublished(
            SampleId sampleId) implements SampleEvent {

        public static SamplePublished of(SampleId sampleId) {
            return new SamplePublished(sampleId);
        }
    }

    @Builder
    record SampleOwnerNameChanged(
            SampleId sampleId,
            String ownerName) implements SampleEvent {
    }

}
