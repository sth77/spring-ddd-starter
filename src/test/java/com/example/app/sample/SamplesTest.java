package com.example.app.sample;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.app.common.model.I18nText;
import com.example.app.person.People;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand;
import com.example.app.referencedata.Cities;
import com.example.app.referencedata.City;
import com.example.app.sample.Sample.SampleState;
import com.example.app.sample.SampleCommand.CreateSample;
import jakarta.persistence.EntityManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class SamplesTest {

    @Autowired
    Samples samples;

    @Autowired
    People people;

    @Autowired
    Cities cities;

    @Autowired
    EntityManager entityManager;

    @Test
    void save_validSampleGiven_stateStoredAsEnumName() {
        // arrange: no converter and no @Enumerated anywhere - StringEnumTypeContributor maps every enum by name
        val sample = sample(people.save(person()), cities.save(city()));
        samples.save(sample);
        entityManager.flush();

        // act
        val stored = entityManager
                .createNativeQuery("SELECT state FROM sample WHERE id = ?1")
                .setParameter(1, sample.getId().uuidValue())
                .getSingleResult();

        // assert: the name, not the ordinal - reordering the constants must not change stored data
        assertThat(stored).isEqualTo("DRAFT");
    }

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
            assertThat(actual.getCity()).isEqualTo(com.example.app.sample.City.of(city));
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
        return Person.create(PersonCommand.CreatePerson.builder().name("N.N.").build());
    }

    private static City city() {
        return City.ofPostalCodeAndName(
                3000, I18nText.builder().en("Bern").de("Bern").build());
    }
}
