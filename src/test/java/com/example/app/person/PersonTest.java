package com.example.app.person;

import static com.example.app.AggregateEvents.getEvents;
import static com.example.app.person.PersonTestData.person;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.app.person.PersonCommand.CreatePerson;
import com.example.app.person.PersonCommand.UpdatePersonName;
import com.example.app.person.PersonEvent.PersonCreated;
import com.example.app.person.PersonEvent.PersonUpdated;
import lombok.val;
import org.junit.jupiter.api.Test;

class PersonTest {

    @Test
    void create_validDataGiven_created() {
        // arrange
        val name = "Person 1";

        // act
        val person = Person.create(CreatePerson.builder()
                .name(name)
                .build());

        // assert
        assertThat(person.getName()).isEqualTo(name);
        assertThat(getEvents(person)).containsExactly(PersonCreated.of(person.getId()));
    }

    @Test
    void updateName_newNameGiven_updatedAndEventRegistered() {
        // arrange
        val person = person("Person 1");

        // act
        person.updateName(UpdatePersonName.builder()
                .name("Person 1'")
                .build());

        // assert
        assertThat(person.getName()).isEqualTo("Person 1'");
        assertThat(getEvents(person)).containsExactly(PersonUpdated.builder()
                .personId(person.getId())
                .name("Person 1'")
                .build());
    }

    /**
     * The event drives the denormalized owner name of every sample the person owns, so a rename to the same name
     * must not trigger that synchronization.
     */
    @Test
    void updateName_sameNameGiven_noEventRegistered() {
        // arrange
        val person = person("Person 1");

        // act
        person.updateName(UpdatePersonName.builder()
                .name("Person 1")
                .build());

        // assert
        assertThat(person.getName()).isEqualTo("Person 1");
        assertThat(getEvents(person)).isEmpty();
    }
}
