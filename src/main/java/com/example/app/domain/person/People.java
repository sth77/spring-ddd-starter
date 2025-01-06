package com.example.app.domain.person;

import java.util.Optional;
import java.util.function.Consumer;

import com.example.app.common.execption.AggregateNotFoundException;
import org.jmolecules.ddd.types.Repository;
import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.util.Streamable;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.domain.person.Person.PersonId;

public interface People extends Repository<Person, PersonId>, AssociationResolver<Person, PersonId> {
    
    Person save(Person person);

    Optional<Person> findById(PersonId id);

    default Person getRequired(PersonId id) {
        return findById(id)
                .orElseThrow(() -> new AggregateNotFoundException(Person.class, id));
    }

    Streamable<Person> findAll();
    
    int count();

    /**
     * Applies the operation to the Person and saves it.
     * @param id the ID of the Person
     * @param operation the operation to apply to the person
     * @return the person or empty if not found
     */
    @Transactional
    default Optional<Person> doWith(PersonId id, Consumer<Person> operation) {
        return findById(id)
                .map(it -> {
                    operation.accept(it);
                    return it;
                })
                .map(this::save);
    }

}
