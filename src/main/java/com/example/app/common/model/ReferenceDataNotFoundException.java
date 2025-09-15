package com.example.app.common.model;

import jakarta.annotation.Nullable;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReferenceDataNotFoundException extends RuntimeException {
    public ReferenceDataNotFoundException(@Nullable Class<?> type, Identifier id) {
        super("Reference data %s with ID %s not found".formatted(
                type == null ? "n/a" : type.getSimpleName(),
                id.toString()));
    }
}
