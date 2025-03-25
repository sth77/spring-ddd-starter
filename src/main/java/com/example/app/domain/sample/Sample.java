package com.example.app.domain.sample;

import com.example.app.domain.common.model.AbstractAggregate;
import com.example.app.domain.common.execption.DomainException;
import com.example.app.domain.person.Person;
import com.example.app.domain.person.Person.PersonId;
import com.example.app.domain.sample.Sample.SampleId;
import com.example.app.domain.sample.SampleCommand.CreateSample;
import com.example.app.domain.sample.SampleCommand.PublishSample;
import com.example.app.domain.sample.SampleCommand.UpdateSample;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.jmolecules.architecture.onion.simplified.DomainRing;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;

import java.util.Objects;
import java.util.UUID;

@Getter
@DomainRing
@AllArgsConstructor
public class Sample extends AbstractAggregate<Sample, SampleId> implements AggregateRoot<Sample, SampleId> {

    private final SampleId id;
    private final Association<Person, PersonId> owner;
    private String name;
    private String description;
    private SampleState state;

    public static Sample create(CreateSample data) {
        val result = new Sample(
                SampleId.random(),
                Association.forAggregate(data.owner()),
                data.name(),
                data.description(),
                SampleState.DRAFT);
        result.registerEvent(SampleEvent.SampleCreated.of(result.getId()));
        return result;
    }

    public Sample update(UpdateSample data) {
        assertCan(data);
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
        assertCan(data);
        state = SampleState.PUBLISHED;
        registerEvent(SampleEvent.SamplePublished.builder()
                .sampleId(id)
                .build());
        return this;
    }

    private void assertCan(SampleCommand command) {
        if (!can(command.getClass())) {
            throw new DomainException("Operation %s not allowed for sample in state %s"
                    .formatted(command.getClass().getSimpleName(), state));
        }
    }

    public <T extends SampleCommand> boolean can(Class<T> operation) {
        if (operation.equals(CreateSample.class)) {
            return false;
        }
        return state != SampleState.PUBLISHED;
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
