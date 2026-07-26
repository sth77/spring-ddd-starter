package com.example.app.sample;

import com.example.app.sample.Sample.SampleState;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

/**
 * Persists {@link SampleState} by name (matching the {@code VARCHAR(20)} schema column) rather than the fragile
 * ordinal default, without annotating the aggregate itself. Hibernate lacks a global "enums as string" switch,
 * so the canonical approach is one {@code autoApply} converter per enum: it is applied automatically to every
 * {@link SampleState} attribute, keeping the domain model free of persistence annotations.
 */
@Converter(autoApply = true)
class SampleStateConverter implements AttributeConverter<SampleState, String> {

    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable SampleState state) {
        return state == null ? null : state.name();
    }

    @Override
    public @Nullable SampleState convertToEntityAttribute(@Nullable String value) {
        return value == null ? null : SampleState.valueOf(value);
    }
}
