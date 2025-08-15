package com.example.app.infrastructure.demo;

import com.example.app.domain.common.logging.LogPrefix;
import com.example.app.domain.person.People;
import com.example.app.domain.person.Person;
import com.example.app.domain.person.PersonCommand;
import com.example.app.domain.person.PersonCommand.CreatePerson;
import com.example.app.domain.sample.Sample;
import com.example.app.domain.sample.SampleCommand.CreateSample;
import com.example.app.domain.sample.SampleCommand.PublishSample;
import com.example.app.domain.sample.SampleCommand.UpdateSample;
import com.example.app.domain.sample.Samples;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataInitializer {

    private final People people;
    private final Samples samples;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    void initData() throws InterruptedException {
        Thread.sleep(2);

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
                        .name("Sample " + i)
                        .description("Description of sample " + i)
                        .owner(personData.get(i % personData.size()))
                        .build()))
                .map(samples::save)
                .toList();

        val sample2 = sampleData.get(1);
        samples.save(sample2.update(UpdateSample.builder()
                .name(sample2.getName())
                .description("Updated description of sample 2")
                .build()));

        val sample3 = sampleData.get(2);
        samples.save(sample3.publish(PublishSample.create()));

    }

}
