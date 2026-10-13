package com.example.app.sample;

import com.example.app.common.model.I18nText;
import com.example.app.sample.Sample.SampleId;
import com.example.app.sample.SampleItem.SampleItemId;
import lombok.Builder;
import org.jmolecules.event.types.DomainEvent;

/**
 * The domain events of the {@link Sample} aggregate. They are registered by its operations and published by Spring once
 * the aggregate is saved; another module reacts to them with a {@code @RetryableApplicationModuleListener} instead of
 * being called by this one.
 */
public sealed interface SampleEvent extends DomainEvent {

    SampleId sampleId();

    record SampleCreated(SampleId sampleId) implements SampleEvent {

        public static SampleCreated of(SampleId sampleId) {
            return new SampleCreated(sampleId);
        }
    }

    @Builder
    record SampleUpdated(SampleId sampleId, I18nText name, String description) implements SampleEvent {
    }

    record SamplePublished(SampleId sampleId) implements SampleEvent {

        public static SamplePublished of(SampleId sampleId) {
            return new SamplePublished(sampleId);
        }
    }

    @Builder
    record SampleOwnerNameChanged(SampleId sampleId, String ownerName) implements SampleEvent {
    }

    // sample enrichment: events of the SampleItem child entity

    @Builder
    record SampleItemAdded(SampleId sampleId, SampleItemId itemId) implements SampleEvent {
    }

    @Builder
    record SampleItemChanged(SampleId sampleId, SampleItemId itemId) implements SampleEvent {
    }

    @Builder
    record SampleItemRemoved(SampleId sampleId, SampleItemId itemId) implements SampleEvent {
    }
}
