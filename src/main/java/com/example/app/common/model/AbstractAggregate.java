package com.example.app.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.AbstractAggregateRoot;

@Slf4j
@MappedSuperclass
@JsonIgnoreProperties("new")
public abstract class AbstractAggregate<T extends AbstractAggregate<T, ID>, ID extends Identifier>
        extends
            AbstractAggregateRoot<T>
        implements
            AggregateRoot<T, ID> {

    /**
     * Carries optimistic locking for every aggregate, and tells Spring Data whether an instance is new.
     * Together with {@code @MappedSuperclass} and {@code registerEvent(..)} of the superclass, this is the one
     * place where persistence and event publication reach into the model - the aggregates themselves stay free
     * of both.
     */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Version
    private int version;
}
