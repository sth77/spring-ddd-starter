package com.example.app.domain.sample;

import com.example.app.domain.person.People;
import com.example.app.domain.person.Person;
import com.example.app.domain.person.PersonCommand.CreatePerson;
import com.example.app.domain.sample.Sample.SampleState;
import com.example.app.domain.sample.SampleCommand.CreateSample;
import lombok.val;
import org.jmolecules.ddd.types.Association;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class SamplesTest {

    @Autowired
    Samples samples;

    @Autowired
    People people;

    @Test
    void save_validSampleGiven_addedToDb() {
        // arrange
        val person = person();
        val sample = sample(person);
        val initialCount = samples.count();

        // act
        samples.save(sample);

        // assert
        assertThat(samples.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void findById_exists_returned() {
        // arrange
        val person = person();
        val sample = sample(person);
        samples.save(sample);

        // act
        val result = samples.findById(sample.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result).get().satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo(sample.getName());
            assertThat(actual.getDescription()).isEqualTo(sample.getDescription());
            assertThat(actual.getOwner()).isEqualTo(Association.forAggregate(person));
            assertThat(actual.getState()).isEqualTo(SampleState.DRAFT);
        });
    }

    private static Sample sample(Person person) {
        return Sample.create(CreateSample.builder()
                .name("Sample 1")
                .description("some description")
                .owner(person)
                .build());
    }

    private Person person() {
        return people.save(Person.create(CreatePerson.builder()
                .name("Glen Miller")
                .build()));
    }

}
