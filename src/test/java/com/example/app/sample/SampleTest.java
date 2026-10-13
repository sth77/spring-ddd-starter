package com.example.app.sample;

import static com.example.app.AggregateEvents.clearEvents;
import static com.example.app.AggregateEvents.getEvents;
import static com.example.app.sample.Sample.SampleState.DRAFT;
import static com.example.app.sample.Sample.SampleState.PUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.app.common.model.DomainException;
import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand;
import com.example.app.referencedata.City;
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
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

public class SampleTest {

    private static final List<Class<? extends SampleItemCommand>> ITEM_COMMANDS = List.of(
            RenameSampleItem.class, CheckSampleItem.class, RemoveSampleItem.class);

    @Test
    void create_validDataGiven_created() {
        // arrange
        val name = I18nText.en("Sample 1");

        // act
        val sample = Sample.create(
                CreateSample.builder().name(name).city(city()).owner(person()).build());

        // assert
        assertThat(sample.getName()).isEqualTo(name);
        assertThat(sample.getState()).isEqualTo(DRAFT);
        assertThat(getEvents(sample)).containsExactly(SampleCreated.of(sample.getId()));
    }

    @Test
    void update_validDataGiven_updated() {
        // arrange
        val sample = sample();
        val updatedName = I18nText.en("Sample with updated name");
        val updatedDescription = "Sample with updated description";

        // act
        sample.update(UpdateSample.builder()
                .name(updatedName)
                .description(updatedDescription)
                .city(city())
                .build());

        // assert
        assertThat(sample.getName()).isEqualTo(updatedName);
        assertThat(sample.getState()).isEqualTo(DRAFT);
        assertThat(getEvents(sample)).containsExactly(SampleUpdated.builder()
                .sampleId(sample.getId())
                .name(updatedName)
                .description(updatedDescription)
                .build());
    }

    @Test
    void update_onlyCityChanged_cityUpdatedAndEventRegistered() {
        // arrange
        val sample = sample();
        val otherCity = City.ofPostalCodeAndName(
                8000, I18nText.builder().en("Zurich").de("Zuerich").build());

        // act: name and description stay as they are, only the city changes
        sample.update(UpdateSample.builder()
                .name(sample.getName())
                .description(sample.getDescription())
                .city(otherCity)
                .build());

        // assert
        assertThat(sample.getCity()).isEqualTo(com.example.app.sample.City.of(otherCity));
        assertThat(getEvents(sample)).containsExactly(SampleUpdated.builder()
                .sampleId(sample.getId())
                .name(sample.getName())
                .description(sample.getDescription())
                .build());
    }

    @Test
    void publish_inDraftState_published() {
        // arrange
        val sample = sample();

        // act
        sample.publish(PublishSample.create());

        // assert
        assertThat(sample.getState()).isEqualTo(PUBLISHED);
        assertThat(getEvents(sample)).containsExactly(SamplePublished.of(sample.getId()));
    }

    @Test
    void publish_alreadyPublished_exceptionThrown() {
        // arrange
        val sample = sample();
        sample.publish(PublishSample.create());
        clearEvents(sample);

        // act & assert
        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> sample.publish(PublishSample.create()));
    }

    @Test
    void updateOwnerName_nameChanged_updatedAndEventRegistered() {
        // arrange
        val sample = sample();
        sample.publish(PublishSample.create());
        clearEvents(sample);

        // act: allowed even on a published sample — it is a technical sync, not a business operation
        sample.updateOwnerName(UpdateOwnerName.of("New Owner"));

        // assert
        assertThat(sample.getOwnerName()).isEqualTo("New Owner");
        assertThat(getEvents(sample)).containsExactly(SampleOwnerNameChanged.builder()
                .sampleId(sample.getId())
                .ownerName("New Owner")
                .build());
    }

    @Test
    void updateOwnerName_nameUnchanged_noEventRegistered() {
        // arrange
        val sample = sample();

        // act
        sample.updateOwnerName(UpdateOwnerName.of(sample.getOwnerName()));

        // assert
        assertThat(getEvents(sample)).isEmpty();
    }

    @Test
    void canPublish_inDraftState_trueReturned() {
        // arrange
        val sample = sample();

        // act & assert
        assertThat(sample.can(PublishSample.class)).isTrue();
    }

    @Test
    void canUpdate_inPublishedState_falseReturned() {
        // arrange
        val sample = sample();
        sample.publish(PublishSample.create());

        // act & assert
        assertThat(sample.can(UpdateSample.class)).isFalse();
    }

    @Test
    void addItem_inDraftState_addedUncheckedAndEventRegistered() {
        // arrange
        val sample = sample();

        // act
        sample.addItem(AddSampleItem.of("Item A"));

        // assert
        assertThat(sample.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getLabel()).isEqualTo("Item A");
            assertThat(item.isChecked()).isFalse();
        });
        assertThat(getEvents(sample)).containsExactly(SampleItemAdded.builder()
                .sampleId(sample.getId())
                .itemId(sample.getItems().getFirst().getId())
                .build());
    }

    @Test
    void addItem_inPublishedState_exceptionThrown() {
        // arrange
        val sample = sample();
        sample.publish(PublishSample.create());

        // act & assert
        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> sample.addItem(AddSampleItem.of("Item A")));
    }

    @Test
    void canItemCommand_uncheckedItem_allItemCommandsAllowed() {
        // arrange
        val sample = sample();
        val itemId = addItem(sample, "Item A");

        // act & assert
        assertThat(ITEM_COMMANDS).allSatisfy(command -> assertThat(sample.can(command, itemId)).isTrue());
    }

    @Test
    void checkItem_uncheckedItem_checkedAndNothingAllowedOnItWhileSiblingStillAllowsAll() {
        // arrange
        val sample = sample();
        val itemA = addItem(sample, "Item A");
        val itemB = addItem(sample, "Item B");
        clearEvents(sample);

        // act
        sample.checkItem(itemA, CheckSampleItem.create());

        // assert
        assertThat(item(sample, itemA).isChecked()).isTrue();
        assertThat(ITEM_COMMANDS).allSatisfy(command -> {
            assertThat(sample.can(command, itemA)).isFalse();
            assertThat(sample.can(command, itemB)).isTrue();
        });
        assertThat(getEvents(sample)).containsExactly(SampleItemChanged.builder()
                .sampleId(sample.getId())
                .itemId(itemA)
                .build());
    }

    @Test
    void renameItem_uncheckedItem_renamedAndEventRegistered() {
        // arrange
        val sample = sample();
        val itemId = addItem(sample, "Item A");
        clearEvents(sample);

        // act
        sample.renameItem(itemId, RenameSampleItem.of("Item A2"));

        // assert
        assertThat(item(sample, itemId).getLabel()).isEqualTo("Item A2");
        assertThat(getEvents(sample)).containsExactly(SampleItemChanged.builder()
                .sampleId(sample.getId())
                .itemId(itemId)
                .build());
    }

    @Test
    void renameItem_checkedItem_exceptionThrown() {
        // arrange
        val sample = sample();
        val itemId = addItem(sample, "Item A");
        sample.checkItem(itemId, CheckSampleItem.create());

        // act & assert
        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> sample.renameItem(itemId, RenameSampleItem.of("Item A2")));
    }

    @Test
    void removeItem_uncheckedItem_removedAndEventRegistered() {
        // arrange
        val sample = sample();
        val itemA = addItem(sample, "Item A");
        val itemB = addItem(sample, "Item B");
        clearEvents(sample);

        // act
        sample.removeItem(itemA, RemoveSampleItem.create());

        // assert
        assertThat(sample.getItems()).extracting(SampleItem::getId).containsExactly(itemB);
        assertThat(getEvents(sample)).containsExactly(SampleItemRemoved.builder()
                .sampleId(sample.getId())
                .itemId(itemA)
                .build());
    }

    @Test
    void canItemCommand_inPublishedState_nothingAllowed() {
        // arrange
        val sample = sample();
        val itemId = addItem(sample, "Item A");
        sample.publish(PublishSample.create());

        // act & assert
        assertThat(ITEM_COMMANDS).allSatisfy(command -> assertThat(sample.can(command, itemId)).isFalse());
    }

    @Test
    void canItemCommandWithoutItem_falseReturned() {
        // arrange: the type-level guard cannot answer for an item, whatever the state of the items
        val sample = sample();
        addItem(sample, "Item A");

        // act & assert
        assertThat(ITEM_COMMANDS).allSatisfy(command -> assertThat(sample.can(command)).isFalse());
    }

    @Test
    void checkItem_unknownItemId_exceptionThrown() {
        // arrange
        val sample = sample();
        addItem(sample, "Item A");

        // act & assert
        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> sample.checkItem(SampleItemId.random(), CheckSampleItem.create()));
    }

    @Test
    void getItems_modifiedFromOutside_rejected() {
        // arrange
        val sample = sample();
        addItem(sample, "Item A");

        // act & assert: the items change through the aggregate's operations only
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> sample.getItems().clear());
    }

    private static SampleItemId addItem(Sample sample, String label) {
        sample.addItem(AddSampleItem.of(label));
        return sample.getItems().getLast().getId();
    }

    private static SampleItem item(Sample sample, SampleItemId itemId) {
        return sample.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow();
    }

    private static Sample sample() {
        val result = Sample.create(CreateSample.builder()
                .name(I18nText.en("Sample X"))
                .owner(person())
                .city(city())
                .build());
        clearEvents(result);
        return result;
    }

    private static Person person() {
        return Person.create(PersonCommand.CreatePerson.builder().name("N.N.").build());
    }

    private static City city() {
        return City.ofPostalCodeAndName(
                3000, I18nText.builder().en("Bern").de("Bern").build());
    }
}
