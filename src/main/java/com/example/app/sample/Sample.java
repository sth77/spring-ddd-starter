package com.example.app.sample;

import com.example.app.common.model.DomainException;
import com.example.app.common.model.AbstractAggregate;
import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import com.example.app.person.Person.PersonId;
import com.example.app.sample.Sample.SampleId;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.SampleEvent.SampleCreated;
import com.example.app.sample.SampleEvent.SamplePublished;
import com.example.app.sample.SampleEvent.SampleUpdated;
import jakarta.persistence.Column;
import lombok.AccessLevel;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sample extends AbstractAggregate<Sample, SampleId> implements AggregateRoot<Sample, SampleId> {

    private final SampleId id;
    private final Association<Person, PersonId> owner;

    private I18nText name;
    private String description;
    private City city;
    private SampleState state;

    public static Sample create(CreateSample data) {
        val result = new Sample(
                SampleId.random(),
                Association.forAggregate(data.owner()),
                data.name(),
                data.description(),
                City.of(data.city()),
                SampleState.DRAFT);
        result.registerEvent(new SampleCreated(result.getId()));
        return result;
    }

    public Sample update(UpdateSample data) {
        assertCan(data);
        if (!(Objects.equals(name, data.name())
                && Objects.equals(description, data.description()))) {
            name = data.name();
            description = data.description();
            city = City.of(data.city());
            registerEvent(new SampleUpdated(id, name, description));
        }
        return this;
    }

    public Sample publish(PublishSample data) {
        assertCan(data);
        state = SampleState.PUBLISHED;
        registerEvent(new SamplePublished(id));
        return this;
    }

    private void assertCan(SampleCommand command) {
        if (!can(command.getClass())) {
            throw new DomainException("Operation %s not allowed for sample in state %s"
                    .formatted(command.getClass().getSimpleName(), state));
        }
    }

    public boolean can(Class<? extends SampleCommand> operation) {
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
