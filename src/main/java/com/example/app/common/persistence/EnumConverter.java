package com.example.app.common.persistence;

import jakarta.persistence.AttributeConverter;
import org.jspecify.annotations.Nullable;

/**
 * Base class for JPA converters that persist an enum by name. Hibernate lacks a global "enums as string"
 * switch, so every enum used in the domain model gets one small {@code @Converter(autoApply = true)} subclass:
 *
 * <pre>{@code
 * @Converter(autoApply = true)
 * class SampleStateConverter extends EnumConverter<SampleState> {
 *     SampleStateConverter() {
 *         super(SampleState.class);
 *     }
 * }
 * }</pre>
 *
 * <p>
 * This replaces {@code @Enumerated} on the domain model, which either breaks on constant reordering
 * (ordinal mapping) or scatters persistence annotations over the aggregates; an ArchUnit rule in
 * {@code ArchitectureTests} forbids it.
 *
 * @param <E>
 *            the enum type persisted by the converter
 */
public abstract class EnumConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

    private final Class<E> enumType;

    protected EnumConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable E value) {
        return value == null ? null : value.name();
    }

    @Override
    public @Nullable E convertToEntityAttribute(@Nullable String value) {
        return value == null ? null : Enum.valueOf(enumType, value);
    }
}
