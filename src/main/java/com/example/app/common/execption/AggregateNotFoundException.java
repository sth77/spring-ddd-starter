package com.example.app.common.execption;

import com.example.app.common.AbstractAggregate;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AggregateNotFoundException extends RuntimeException {
    public AggregateNotFoundException(Class<? extends AbstractAggregate<?, ?>> type, Identifier id) {
        super("Aggregate %s with id %s not found".formatted(type.getSimpleName(), id.toString()));
    }
}
