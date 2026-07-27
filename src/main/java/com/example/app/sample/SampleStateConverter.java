package com.example.app.sample;

import com.example.app.common.persistence.EnumConverter;
import com.example.app.sample.Sample.SampleState;
import jakarta.persistence.Converter;

/**
 * Persists {@link SampleState} by name (matching the {@code VARCHAR(20)} schema column) rather than the fragile
 * ordinal default, without annotating the aggregate itself. Applied automatically to every {@link SampleState}
 * attribute, keeping the domain model free of persistence annotations.
 */
@Converter(autoApply = true)
class SampleStateConverter extends EnumConverter<SampleState> {

    SampleStateConverter() {
        super(SampleState.class);
    }
}
