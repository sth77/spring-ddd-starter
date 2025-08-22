package com.example.app._application;

import com.example.app.common.logging.LogPrefix;
import com.example.app.common.model.I18nText;
import com.example.app.person.People;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand.CreatePerson;
import com.example.app.referencedata.Cities;
import com.example.app.referencedata.City;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleCommand.PublishSample;
import com.example.app.sample.SampleCommand.UpdateSample;
import com.example.app.sample.Samples;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataInitializer {

    private static final int POSTAL_CODE1 = 3000;
    private static final int POSTAL_CODE2 = 8000;

    private final People people;
    private final Samples samples;
    private final Cities cities;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    void initData() throws InterruptedException {
        Thread.sleep(2);
        initCities();

        log.info(LogPrefix.APPLICATION.withText("Initializing sample data"));
        /*
         * TODO:
         * 1) declare dependencies to your repositories as final fields
         * 2) create some test data useful for developing the application
         */
        val personData = IntStream.range(1, 3)
                .mapToObj(i -> Person.create(CreatePerson.builder()
                        .name("Person " + i)
                        .build()))
                .map(people::save)
                .toList();
        val sampleData = IntStream.range(1, 4)
                .mapToObj(i -> Sample.create(CreateSample.builder()
                        .name(I18nText.en("Sample " + i))
                        .description("Description of sample " + i)
                        .owner(personData.get(i % personData.size()))
                        .city(cities.findByPostalCode(POSTAL_CODE1).orElseThrow())
                        .build()))
                .map(samples::save)
                .toList();

        val sample2 = sampleData.get(1);
        samples.save(sample2.update(UpdateSample.builder()
                .name(sample2.getName())
                .description("Updated description of sample 2")
                .city(cities.findByPostalCode(POSTAL_CODE2).orElseThrow())
                .build()));

        val sample3 = sampleData.get(2);
        samples.save(sample3.publish(PublishSample.create()));

    }

    private void initCities() {
        Map.of(
                        POSTAL_CODE1, I18nText.builder().en("Bern").de("Bern").build(),
                        POSTAL_CODE2, I18nText.builder().en("Zurich").de("Zürich").build())
                .forEach((postalCode, name) -> cities.save(City.ofPostalCodeAndName(postalCode, name)));
    }

}
