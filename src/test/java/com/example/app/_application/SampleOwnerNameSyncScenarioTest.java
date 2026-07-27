package com.example.app._application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.app.common.model.I18nText;
import com.example.app.person.People;
import com.example.app.person.Person;
import com.example.app.person.PersonCommand.CreatePerson;
import com.example.app.person.PersonEvent.PersonUpdated;
import com.example.app.sample.Sample;
import com.example.app.sample.SampleCommand.CreateSample;
import com.example.app.sample.SampleEvent.SampleOwnerNameChanged;
import com.example.app.sample.Samples;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

/**
 * Verifies the cross-module choreography with Spring Modulith's {@link Scenario} API: when a person is
 * renamed, the sample module keeps its denormalized copy of the owner's name in sync — reacting to the
 * {@link PersonUpdated} event instead of reading across the module boundary.
 *
 * <p>
 * {@code @ApplicationModuleTest} bootstraps the application module together with its dependencies, so the
 * asynchronous {@code @ApplicationModuleListener} and the event publication registry are exercised end to end.
 */
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
class SampleOwnerNameSyncScenarioTest {

    @Autowired
    People people;

    @Autowired
    Samples samples;

    @Test
    void personRenamed_denormalizedOwnerNameOnSampleIsSynced(Scenario scenario) {
        // arrange: a person owning a sample that carries a denormalized copy of the owner's name
        val owner = people.save(
                Person.create(CreatePerson.builder().name("Ada Lovelace").build()));
        val sample = samples.save(Sample.create(CreateSample.builder()
                .name(I18nText.en("Analytical Engine"))
                .description("A sample owned by Ada")
                .owner(owner)
                .build()));

        // act & assert: renaming the person propagates to the sample's denormalized owner name
        scenario.publish(new PersonUpdated(owner.getId(), "Ada King"))
                .andWaitForEventOfType(SampleOwnerNameChanged.class)
                .toArriveAndVerify(event -> {
                    assertThat(event.sampleId()).isEqualTo(sample.getId());
                    assertThat(event.ownerName()).isEqualTo("Ada King");
                });
    }
}
