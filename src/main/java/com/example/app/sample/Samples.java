package com.example.app.sample;

import com.example.app.common.model.AggregateRepository;
import com.example.app.person.Person;
import com.example.app.person.Person.PersonId;
import com.example.app.sample.Sample.SampleId;
import org.jmolecules.ddd.types.Association;
import org.springframework.data.util.Streamable;

/**
 * The repository of the {@link Sample} aggregates, named after their plural. Spring Data REST exports it read-only; an
 * aggregate changes through {@link #doWith}, which applies an operation and saves the aggregate in one transaction.
 */
public interface Samples extends AggregateRepository<Sample, SampleId> {

    @Override
    default Class<Sample> getAggregateType() {
        return Sample.class;
    }

    Streamable<Sample> findByOwner(Association<Person, PersonId> owner);
}
