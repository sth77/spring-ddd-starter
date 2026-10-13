package com.example.app.common.model;

import org.jmolecules.ddd.types.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * A reference data item looked up by its identifier does not exist. Mapped to HTTP 404 by the infrastructure layer
 * (see DomainExceptionHandler).
 */
public class ReferenceDataNotFoundException extends DomainException {

    private static final long serialVersionUID = 4329316580318407207L;

    public ReferenceDataNotFoundException(@Nullable Class<?> type, Identifier id) {
        super("Reference data %s with ID %s not found"
                .formatted(type == null ? "n/a" : type.getSimpleName(), id.toString()));
    }
}
