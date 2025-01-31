package com.example.app.domain.person;

import com.example.app.domain.common.model.AbstractAggregate;
import com.example.app.domain.person.Person.PersonId;
import com.example.app.domain.person.PersonCommand.CreatePerson;
import com.example.app.domain.person.PersonCommand.UpdatePersonName;
import com.example.app.domain.person.PersonEvent.PersonCreated;
import com.example.app.domain.person.PersonEvent.PersonNameUpdated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Person extends AbstractAggregate<Person, PersonId> implements AggregateRoot<Person, PersonId>{

    private final PersonId id;
    private String name;

    public static Person create(CreatePerson data) {
        val result = new Person(PersonId.random());
        result.name = data.name();
        result.registerEvent(PersonCreated.of(result.getId()));
        return result;
    }

    public Person updateName(UpdatePersonName data) {
        if (!Objects.equals(this.name, data.name())) {
            this.name = data.name();
            registerEvent(PersonNameUpdated.builder()
                      .personId(id)
                      .name(name)
                      .build());
        }
        return this;
    }

    public boolean can(Operation operation) {
        return true;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Operation {
        CREATE("create"),
        UPDATE_NAME("updateName");

        public final String rel;
    }

    public record PersonId(UUID id) implements Identifier {

        public static PersonId random() {
            return PersonId.of(UUID.randomUUID());
        }

        public static PersonId of(UUID uuidValue) {
            return new PersonId(uuidValue);
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

}
