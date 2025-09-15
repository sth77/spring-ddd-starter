package com.example.app._infrastructure.persistence;

import com.example.app.referencedata.Cities;
import com.example.app.referencedata.City;
import com.example.app.referencedata.City.CityId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Converter(autoApply = true)
@RequiredArgsConstructor
public class CityConverter implements AttributeConverter<City, UUID> {

    private final Cities cities;

    @Override
    public UUID convertToDatabaseColumn(City city) {
        return city.getId().uuidValue();
    }

    @Override
    public City convertToEntityAttribute(UUID uuid) {
        return cities.getRequired(new CityId(uuid));
    }
}
