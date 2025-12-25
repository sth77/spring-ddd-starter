package com.example.app.sample;

import com.example.app.common.model.I18nText;
import com.example.app.person.People;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand;
import com.example.app.referencedata.Cities;
import com.example.app.referencedata.City;
import com.example.app.sample.Sample.SampleState;
import com.example.app.sample.SampleCommand.CreateSample;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SamplesTest {

    @Autowired
    Samples samples;

    @Autowired
    People people;

    @Autowired
    Cities cities;

    @Test
    void save_validSampleGiven_savedToDb() {
        // arrange
        val person = people.save(person());
        val city = cities.save(city());
        val sample = sample(person, city);
        val initialCount = samples.count();

        // act
        samples.save(sample);

        // assert
        assertThat(samples.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void findById_exists_returned() {
        // arrange
        val person = people.save(person());
        val city = cities.save(city());
        val sample = sample(person, city);
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

    private static Sample sample(Person person, City city) {
        return Sample.create(CreateSample.builder()
                .name(I18nText.en("Sample 1"))
                .description("Sample description")
                .city(city)
                .owner(person)
                .build());
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
