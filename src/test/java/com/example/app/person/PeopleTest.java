package com.example.app.person;

import static com.example.app.person.PersonTestData.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.example.app.common.model.AggregateNotFoundException;
import com.example.app.person.Person.PersonId;
import com.example.app.person.PersonCommand.UpdatePersonName;
import jakarta.persistence.EntityManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PeopleTest {

    @Autowired
    People people;

    @Autowired
    EntityManager entityManager;

    @Test
    void save_validPersonGiven_savedToDb() {
        // arrange
        val person = person();
        val initialCount = people.count();

        // act
        people.save(person);

        // assert
        assertThat(people.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void findById_exists_returned() {
        // arrange
        val person = person();
        people.save(person);
        entityManager.flush();
        entityManager.clear();

        // act
        val result = people.findById(person.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result).get().satisfies(actual -> {
            assertThat(actual).isEqualTo(person);
            assertThat(actual).isNotSameAs(person);
            assertThat(actual.getName()).isEqualTo(person.getName());
        });
    }

    @Test
    void doWith_exists_operationAppliedAndSaved() {
        // arrange
        val person = people.save(person("Person 1"));
        entityManager.flush();
        entityManager.clear();

        // act
        val result = people.doWith(person.getId(), it -> it.updateName(UpdatePersonName.builder()
                .name("Person 1'")
                .build()));
        entityManager.flush();
        entityManager.clear();

        // assert
        assertThat(result).get().extracting(Person::getName).isEqualTo("Person 1'");
        assertThat(people.getRequired(person.getId()).getName()).isEqualTo("Person 1'");
    }

    @Test
    void getRequired_unknownId_exceptionThrown() {
        assertThatExceptionOfType(AggregateNotFoundException.class)
                .isThrownBy(() -> people.getRequired(PersonId.random()));
    }
}
