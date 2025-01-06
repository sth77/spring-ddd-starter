package com.example.app.domain.person;

import com.example.app.domain.person.Person.PersonId;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.jmolecules.event.types.DomainEvent;

public sealed interface PersonEvent extends DomainEvent {

    PersonId personId();

    @Builder
    record PersonCreated(
        @NotNull PersonId personId) implements PersonEvent {

        static PersonCreated of(PersonId personId) {
            return new PersonCreated(personId);
        }
    }

    @Builder
    record PersonNameUpdated(
        @NotNull PersonId personId,
        @NotNull String name) implements PersonEvent {
    }

}
