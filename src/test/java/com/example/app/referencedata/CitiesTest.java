package com.example.app.referencedata;

import com.example.app.common.model.I18nText;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CitiesTest {

    @Autowired
    Cities cities;

    @Test
    void save_validDataGiven_savedToDb() {
        val initialCount = cities.count();

        // act
        val city = cities.save(city());

        // assert
        assertThat(cities.count()).isEqualTo(initialCount + 1);
    }

    @Test
    void findById_exists_returned() {
        // arrange
        val city = cities.save(city());

        // act
        val result = cities.findById(city.getId());

        // assert
        assertThat(result).hasValue(city);
    }

    @Test
    void findByPostalCode_exists_returned() {
        // arrange
        val city = cities.save(city());

        // act
        val result = cities.findByPostalCode(city.getPostalCode());

        // assert
        assertThat(result).hasValue(city);
    }

    @Test
    void findByNameEnStartingWith_exists_returned() {
        // arrange
        val city = cities.save(city());

        // act
        val result = cities.findByNameEnStartingWith("Be");

        // assert
        assertThat(result).containsExactly(city);
    }

    private static City city() {
        return City.ofPostalCodeAndName(3000, I18nText.builder().en("Bern").de("Bern").build());
    }
}
