package com.example.app.person;

import com.example.app.common.model.AbstractAggregate;
import com.example.app.person.Person.PersonId;
import com.example.app.person.PersonCommand.CreatePerson;
import com.example.app.person.PersonCommand.UpdatePersonName;
import com.example.app.person.PersonEvent.PersonCreated;
import com.example.app.person.PersonEvent.PersonUpdated;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

@Getter
@RequiredArgsConstructor
public class Person extends AbstractAggregate<Person, PersonId> implements AggregateRoot<Person, PersonId> {

    private final PersonId id;
    private String name = "";

    public static Person create(CreatePerson data) {
        val result = new Person(PersonId.random());
        result.name = data.name();
        result.registerEvent(PersonCreated.of(result.getId()));
        return result;
    }

    /**
     * Renames the person. The {@link PersonUpdated} event is registered only on an actual change, since it drives the
     * denormalized owner name of every sample the person owns (see {@code SampleOwnerNameSynchronizer}).
     */
    public void updateName(UpdatePersonName data) {
        if (!Objects.equals(this.name, data.name())) {
            this.name = data.name();
            registerEvent(PersonUpdated.builder()
                    .personId(id)
                    .name(name)
                    .build());
        }
    }

    public record PersonId(UUID uuidValue) implements Identifier {

        public static PersonId random() {
            return PersonId.of(UUID.randomUUID());
        }

        public static PersonId of(UUID uuidValue) {
            return new PersonId(uuidValue);
        }

        @Override
        public String toString() {
            return uuidValue.toString();
        }
    }
}
