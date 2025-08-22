package com.example.app.common.model;

import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReferenceDataRepository<T, ID extends Identifier> extends CrudRepository<T, ID> {

    Class<T> getItemType();

    default T getRequired(ID id) {
        return findById(id).orElseThrow(() -> new ReferenceDataNotFoundException(getItemType(), id));
    }

}
