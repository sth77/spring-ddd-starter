package com.example.app.sample;

import com.example.app.common.model.I18nText;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.Nullable;

@ValueObject
public record City(int postalCode, I18nText name) {
    public static City ofPostalCodeAndName(int postalCode, I18nText name) {
        return new City(postalCode, name);
    }

    public static @Nullable City of(com.example.app.referencedata.@Nullable City city) {
        return city == null ? null : new City(city.getPostalCode(), city.getName());
    }
}
