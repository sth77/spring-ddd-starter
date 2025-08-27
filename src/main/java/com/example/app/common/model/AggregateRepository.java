package com.example.app.common.model;

import org.jmolecules.ddd.integration.AssociationResolver;
import org.jmolecules.ddd.types.Identifier;
import org.jmolecules.ddd.types.Repository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@NoRepositoryBean
public interface AggregateRepository<T extends AbstractAggregate<T, ID>, ID extends Identifier> extends Repository<T, ID>, AssociationResolver<T, ID> {

    Class<T> getAggregateType();

    T save(T aggregate);

    Optional<T> findById(ID id);

    default T getRequired(ID id) {
        return findById(id)
                .orElseThrow(() -> new AggregateNotFoundException(getAggregateType(), id));
    }

    List<T> findAll();

    int count();

    /**
     * Applies the operation to the aggregate and saves it.
     *
     * @param id        the ID of the aggregate
     * @param operation the operation to apply to the aggregate
     * @return the aggregate or empty if not found
     */
    @Transactional
    default Optional<T> doWith(ID id, Consumer<T> operation) {
        return findById(id)
                .map(it -> {
                    operation.accept(it);
                    return save(it);
                });
    }

}

