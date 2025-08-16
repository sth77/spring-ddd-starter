package com.example.app.sample;

import java.util.Optional;
import java.util.function.Consumer;

import com.example.app.common.model.AggregateNotFoundException;
import org.jmolecules.ddd.types.Repository;
import org.jmolecules.ddd.integration.AssociationResolver;
import org.springframework.data.util.Streamable;
import org.springframework.transaction.annotation.Transactional;

import com.example.app.sample.Sample.SampleId;

public interface Samples extends Repository<Sample, SampleId>, AssociationResolver<Sample, SampleId> {
    
    Sample save(Sample sample);

    Optional<Sample> findById(SampleId id);

    default Sample getRequired(SampleId id) {
        return findById(id)
                .orElseThrow(() -> new AggregateNotFoundException(Sample.class, id));
    }

    Streamable<Sample> findAll();
    
    int count();

    /**
     * Applies the operation to the Sample and saves it.
     * @param id the ID of the Sample
     * @param operation the operation to apply to the Sample
     * @return the Sample or empty if not found
     */
    @Transactional
    default Optional<Sample> doWith(SampleId id, Consumer<Sample> operation) {
        return findById(id)
                .map(it -> {
                    operation.accept(it);
                    return it;
                })
                .map(this::save);
    }

}
