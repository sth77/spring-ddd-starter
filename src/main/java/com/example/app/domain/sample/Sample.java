package com.example.app.domain.sample;

import com.example.app.common.model.AbstractAggregate;
import com.example.app.common.execption.DomainException;
import com.example.app.domain.person.Person;
import com.example.app.domain.person.Person.PersonId;
import com.example.app.domain.sample.Sample.SampleId;
import com.example.app.domain.sample.SampleCommand.CreateSample;
import com.example.app.domain.sample.SampleCommand.PublishSample;
import com.example.app.domain.sample.SampleCommand.UpdateSample;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;

import java.util.Objects;
import java.util.UUID;

@Getter
@DomainRing
@RequiredArgsConstructor
public class Sample extends AbstractAggregate<Sample, SampleId> implements AggregateRoot<Sample, SampleId> {

    private final SampleId id;
    private final Association<Person, PersonId> owner;
    private String name;
    private String description;
    private SampleState state = SampleState.DRAFT;

    public static Sample create(CreateSample data) {
        val result = new Sample(
                SampleId.random(),
                Association.forAggregate(data.owner()));
        result.name = data.name();
        result.description = data.description();
        result.registerEvent(SampleEvent.SampleCreated.of(result.getId()));
        return result;
    }

    public Sample update(UpdateSample data) {
        assertCan(Operation.UPDATE);
        if (!(Objects.equals(name, data.name())
                && Objects.equals(description, data.description()))) {
            name = data.name();
            description = data.description();
            registerEvent(SampleEvent.SampleUpdated.builder()
                    .sampleId(id)
                    .name(name)
                    .description(description)
                    .build());
        }
        return this;
    }

    public Sample publish(PublishSample data) {
        assertCan(Operation.PUBLISH);
        state = SampleState.PUBLISHED;
        registerEvent(SampleEvent.SamplePublished.builder()
                .sampleId(id)
                .build());
        return this;
    }

    private void assertCan(Operation operation) {
        if (!can(operation)) {
            throw new DomainException("Operation %s not allowed for sample in state %s"
                    .formatted(operation.rel, state));
        }
    }

    public boolean can(Operation operation) {
        return switch (operation) {
            case CREATE -> false;
            case UPDATE, PUBLISH -> state != SampleState.PUBLISHED;
        };
    }

    @Getter
    @RequiredArgsConstructor
    public enum Operation {
        CREATE("create"),
        UPDATE("update"),
        PUBLISH("publish");

        public final String rel;
    }

    public record SampleId(@Column(name = "id") UUID uuidValue) implements Identifier {

        public static SampleId random() {
            return SampleId.of(UUID.randomUUID());
        }

        public static SampleId of(UUID uuidValue) {
            return new SampleId(uuidValue);
        }

        @Override
        public String toString() {
            return uuidValue.toString();
        }
    }

    public enum SampleState {
        DRAFT,
        PUBLISHED
    }

}
