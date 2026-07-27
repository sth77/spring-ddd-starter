package com.example.app._application;

import com.example.app.person.Person;
import com.example.app.person.Person.PersonId;
import com.example.app.person.PersonEvent.PersonUpdated;
import com.example.app.sample.SampleCommand.UpdateOwnerName;
import com.example.app.sample.Samples;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.types.Association;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Keeps each {@link com.example.app.sample.Sample}'s denormalized owner name in sync whenever the
 * owning person is renamed. Reacting to the {@link PersonUpdated} event rather than reading across
 * the module boundary keeps the two modules loosely coupled.
 */
@Component
@RequiredArgsConstructor
class SampleOwnerNameSynchronizer {

    private final Samples samples;

    @ApplicationModuleListener
    void on(PersonUpdated event) {
        Association<Person, PersonId> owner = Association.forId(event.personId());
        samples.findByOwner(owner)
                .forEach(sample -> samples.save(sample.updateOwnerName(UpdateOwnerName.of(event.name()))));
    }
}
