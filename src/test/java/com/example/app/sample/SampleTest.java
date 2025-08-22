package com.example.app.sample;

import com.example.app.common.model.DomainException;
import com.example.app.common.model.I18nText;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand;
import com.example.app.referencedata.City;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.SampleEvent.SampleCreated;
import com.example.app.sample.SampleEvent.SamplePublished;
import com.example.app.sample.SampleEvent.SampleUpdated;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.example.app.AggregateEvents.clearEvents;
import static com.example.app.AggregateEvents.getEvents;
import static com.example.app.sample.Sample.SampleState.DRAFT;
import static com.example.app.sample.Sample.SampleState.PUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SampleTest {

    @Test
    void create_validDataGiven_created() {
        // arrange
        val name = I18nText.en("Sample 1");

        // act
        val sample = Sample.create(CreateSample.builder()
                .name(name)
                .city(city())
                .owner(person())
                .build());

        // assert
        assertThat(sample.getName()).isEqualTo(name);
        assertThat(sample.getState()).isEqualTo(DRAFT);
        assertThat(getEvents(sample)).containsExactly(new SampleCreated(sample.getId()));
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
        assertThat(getEvents(sample)).containsExactly(new SampleUpdated(
                sample.getId(),
                updatedName,
                updatedDescription));
    }

    @Test
    void publish_inDraftState_published() {
        // arrange
        val sample = sample();

        // act
        sample.publish(new PublishSample());

        // assert
        assertThat(sample.getState()).isEqualTo(PUBLISHED);
        assertThat(getEvents(sample)).containsExactly(new SamplePublished(sample.getId()));
    }

    @Test
    void publish_alreadyPublished_exceptionThrown() {
        // arrange
        val sample = sample();
        sample.publish(new PublishSample());
        clearEvents(sample);

        // act & assert
        assertThatExceptionOfType(DomainException.class)
                .isThrownBy(() -> sample.publish(new PublishSample()));
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
        val sample = sample().publish(new PublishSample());

        // act & assert
        assertThat(sample.can(UpdateSample.class)).isFalse();
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
        return Person.create(PersonCommand.CreatePerson.builder()
                .name("N.N.")
                .build());
    }

    private static City city() {
        return City.ofPostalCodeAndName(3000, I18nText.builder().en("Bern").de("Bern").build());
    }

}
