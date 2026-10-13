package com.example.app.common.model;

import org.jmolecules.ddd.types.Identifier;

/**
 * An aggregate looked up by its identifier does not exist. Mapped to HTTP 404 by the infrastructure layer (see
 * DomainExceptionHandler).
 */
public class AggregateNotFoundException extends DomainException {

    private static final long serialVersionUID = -2405863429447396514L;

    public AggregateNotFoundException(Class<? extends AbstractAggregate<?, ?>> type, Identifier id) {
        super("Aggregate %s with ID %s not found".formatted(type.getSimpleName(), id.toString()));
    }
}
