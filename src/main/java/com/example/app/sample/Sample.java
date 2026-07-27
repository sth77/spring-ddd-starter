package com.example.app.sample;

import com.example.app.common.model.DomainException;
import com.example.app.common.model.AbstractAggregate;
import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import com.example.app.person.Person.PersonId;
import com.example.app.sample.Sample.SampleId;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.SampleCommand.UpdateOwnerName;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.SampleEvent.SampleCreated;
import com.example.app.sample.SampleEvent.SampleOwnerNameChanged;
import com.example.app.sample.SampleEvent.SamplePublished;
import com.example.app.sample.SampleEvent.SampleUpdated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Sample extends AbstractAggregate<Sample, SampleId> implements AggregateRoot<Sample, SampleId> {

    private final SampleId id;
    private final Association<Person, PersonId> owner;

    private I18nText name;
    private String description;
    private @Nullable City city;
    private SampleState state;
    // denormalized copy of the owner's name, kept in sync via a PersonUpdated listener
    private String ownerName;

    public static Sample create(CreateSample data) {
        val result = new Sample(
                SampleId.random(),
                Association.forAggregate(data.owner()),
                data.name(),
                data.description(),
                City.of(data.city()),
                SampleState.DRAFT,
                data.owner().getName());
        result.registerEvent(SampleCreated.of(result.getId()));
        return result;
    }

    public void update(UpdateSample data) {
        assertCan(UpdateSample.class);
        if (!(Objects.equals(name, data.name())
                && Objects.equals(description, data.description()))) {
            name = data.name();
            description = data.description();
            city = City.of(data.city());
            registerEvent(SampleUpdated.builder()
                    .sampleId(id)
                    .name(name)
                    .description(description)
                    .build());
        }
    }

    /**
     * Keeps the denormalized owner name in sync with the owning {@link Person}. Invoked by an
     * application listener reacting to the owner's name change.
     */
    public void updateOwnerName(UpdateOwnerName data) {
        assertCan(UpdateOwnerName.class);
        if (!Objects.equals(ownerName, data.ownerName())) {
            ownerName = data.ownerName();
            registerEvent(SampleOwnerNameChanged.builder()
                    .sampleId(id)
                    .ownerName(ownerName)
                    .build());
        }
    }

    public void publish(PublishSample data) {
        assertCan(PublishSample.class);
        state = SampleState.PUBLISHED;
        registerEvent(SamplePublished.of(id));
    }

    private void assertCan(Class<? extends SampleCommand> command) {
        if (!can(command)) {
            throw new DomainException("Operation %s not allowed for sample in state %s"
                    .formatted(command.getSimpleName(), state));
        }
    }

    public boolean can(Class<? extends SampleCommand> operation) {
        if (operation.equals(CreateSample.class)) {
            return false;
        }
        if (operation.equals(UpdateOwnerName.class)) {
            return true; // technical sync, allowed in every state
        }
        return state != SampleState.PUBLISHED;
    }

    public record SampleId(UUID uuidValue) implements Identifier {

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
