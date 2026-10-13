package com.example.app.sample;

import com.example.app.common.model.AbstractAggregate;
import com.example.app.common.model.DomainException;
import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import com.example.app.person.Person.PersonId;
import com.example.app.sample.Sample.SampleId;
import com.example.app.sample.SampleCommand.AddSampleItem;
import com.example.app.sample.SampleCommand.CheckSampleItem;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.SampleCommand.RemoveSampleItem;
import com.example.app.sample.SampleCommand.RenameSampleItem;
import com.example.app.sample.SampleCommand.SampleItemCommand;
import com.example.app.sample.SampleCommand.UpdateOwnerName;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.SampleEvent.SampleCreated;
import com.example.app.sample.SampleEvent.SampleItemAdded;
import com.example.app.sample.SampleEvent.SampleItemChanged;
import com.example.app.sample.SampleEvent.SampleItemRemoved;
import com.example.app.sample.SampleEvent.SampleOwnerNameChanged;
import com.example.app.sample.SampleEvent.SamplePublished;
import com.example.app.sample.SampleEvent.SampleUpdated;
import com.example.app.sample.SampleItem.SampleItemId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Association;
import org.jmolecules.ddd.types.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * The Sample aggregate root, the consistency boundary of its feature. Its state changes only through the methods taking
 * one of the {@link SampleCommand} records: each first checks {@link #can(Class)}, then changes the state and registers
 * a
 * {@link SampleEvent}, which Spring publishes once the aggregate is saved through {@link Samples}. Add an operation
 * with
 * {@code hygen operation new <Name> --aggregate=Sample --feature=<feature>}.
 *
 * <p>
 * The class carries no JPA annotations: the jMolecules byte-buddy plugin adds the mapping at build time, and the
 * column names follow from the field names (see the README, "Persistence").
 */
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
    // sample enrichment: a child entity collection, see the item operations below
    @Getter(AccessLevel.NONE)
    private final List<SampleItem> items;

    public static Sample create(CreateSample data) {
        val result = new Sample(
                SampleId.random(),
                Association.forAggregate(data.owner()),
                data.name(),
                data.description(),
                City.of(data.city()),
                SampleState.DRAFT,
                data.owner().getName(),
                new ArrayList<>());
        result.registerEvent(SampleCreated.of(result.getId()));
        return result;
    }

    public void update(UpdateSample data) {
        assertCan(UpdateSample.class);
        val updatedCity = City.of(data.city());
        // every updatable field takes part in the comparison - a field left out here would be silently
        // dropped whenever it is the only one that changed
        if (!(Objects.equals(name, data.name())
                && Objects.equals(description, data.description())
                && Objects.equals(city, updatedCity))) {
            name = data.name();
            description = data.description();
            city = updatedCity;
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

    // sample enrichment: operations on the SampleItem child entity

    public List<SampleItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addItem(AddSampleItem data) {
        assertCan(AddSampleItem.class);
        val item = SampleItem.create(data);
        items.add(item);
        registerEvent(SampleItemAdded.builder()
                .sampleId(id)
                .itemId(item.getId())
                .build());
    }

    public void renameItem(SampleItemId itemId, RenameSampleItem data) {
        assertCan(RenameSampleItem.class, itemId);
        requiredItem(itemId).rename(data.label());
        registerEvent(SampleItemChanged.builder()
                .sampleId(id)
                .itemId(itemId)
                .build());
    }

    public void checkItem(SampleItemId itemId, CheckSampleItem data) {
        assertCan(CheckSampleItem.class, itemId);
        requiredItem(itemId).check();
        registerEvent(SampleItemChanged.builder()
                .sampleId(id)
                .itemId(itemId)
                .build());
    }

    public void removeItem(SampleItemId itemId, RemoveSampleItem data) {
        assertCan(RemoveSampleItem.class, itemId);
        items.remove(requiredItem(itemId));
        registerEvent(SampleItemRemoved.builder()
                .sampleId(id)
                .itemId(itemId)
                .build());
    }

    private Optional<SampleItem> findItem(SampleItemId itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    }

    private SampleItem requiredItem(SampleItemId itemId) {
        // assertCan has already rejected an unknown id
        return findItem(itemId).orElseThrow();
    }

    private void assertCan(Class<? extends SampleItemCommand> command, SampleItemId itemId) {
        if (!can(command, itemId)) {
            throw new DomainException("Operation %s not allowed for item %s of sample in state %s"
                    .formatted(command.getSimpleName(), itemId, state));
        }
    }

    /**
     * Instance-level guard for the commands on a single item: the aggregate's rule (no change once published)
     * combined with the item's own. An id the sample does not contain allows nothing.
     */
    public boolean can(Class<? extends SampleItemCommand> operation, SampleItemId itemId) {
        return state != SampleState.PUBLISHED
                && findItem(itemId).map(item -> item.can(operation)).orElse(false);
    }

    private void assertCan(Class<? extends SampleCommand> command) {
        if (!can(command)) {
            throw new DomainException("Operation %s not allowed for sample in state %s"
                    .formatted(command.getSimpleName(), state));
        }
    }

    /**
     * Whether the command is allowed in the aggregate's current state. Besides guarding the operations, it decides
     * whether the operation's HAL link is rendered (see {@code SampleLinks}).
     */
    public boolean can(Class<? extends SampleCommand> operation) {
        if (operation.equals(CreateSample.class)) {
            return false;
        }
        if (SampleItemCommand.class.isAssignableFrom(operation)) {
            return false; // item commands need the item: see can(Class, SampleItemId)
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
        DRAFT, PUBLISHED
    }
}
