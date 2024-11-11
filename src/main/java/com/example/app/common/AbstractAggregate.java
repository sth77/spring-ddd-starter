package com.example.app.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.AbstractAggregateRoot;

@Slf4j
@MappedSuperclass
public abstract class AbstractAggregate<T extends AbstractAggregate<T, ID>, ID extends Identifier> extends AbstractAggregateRoot<T> {

    /**
     * The version field is used by Spring Data JDBC for optimistic locking
     * and to determine whether an entity is new or not.
     */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Version
    private int version;

}
