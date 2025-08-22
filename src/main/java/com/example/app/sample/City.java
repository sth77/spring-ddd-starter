package com.example.app.sample;

import com.example.app.common.model.I18nText;
import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record City(int postalCode, I18nText name) {
    public static City ofPostalCodeAndName(int postalCode, I18nText name) {
        return new City(postalCode, name);
    }

    public static City of(com.example.app.referencedata.City city) {
        return new City(city.getPostalCode(), city.getName());
    }
}
