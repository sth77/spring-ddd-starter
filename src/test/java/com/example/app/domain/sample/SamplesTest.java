package com.example.app.domain.sample;

import com.example.app.domain.person.Person;
import com.example.app.domain.person.PersonCommand;
import com.example.app.domain.sample.Sample.SampleState;
import com.example.app.domain.sample.SampleCommand.CreateSample;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SamplesTest {

    @Autowired
    Samples samples;

    @Test
    void save_validSampleGiven_savedToDb() {
        // arrange
        val sample = sample();
        val initialCount = samples.count();

        // act
        samples.save(sample);

        // assert
        assertThat(samples.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void findById_exists_returned() {
        // arrange
        val sample = sample();
        samples.save(sample);

        // act
        val result = samples.findById(sample.getId());

        // assert
        assertThat(result).isPresent();
        assertThat(result).get().satisfies(actual -> {
            assertThat(actual.getName()).isEqualTo(sample.getName());
            assertThat(actual.getState()).isEqualTo(SampleState.DRAFT);
        });
    }

    private static Sample sample() {
        return Sample.create(CreateSample.builder()
                .name("Sample 1")
                .owner(person())
                .build());
    }

    private static Person person() {
        return Person.create(PersonCommand.CreatePerson.builder()
                .name("N.N.")
                .build());
    }
}
