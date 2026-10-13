package com.example.app.person;

import com.example.app.common.model.AggregateRepository;
import com.example.app.person.Person.PersonId;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "people", path = "people")
public interface People extends AggregateRepository<Person, PersonId> {

    @Override
    default Class<Person> getAggregateType() {
        return Person.class;
    }
}
