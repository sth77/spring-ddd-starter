package com.example.app._infrastructure.persistence;

import com.example.app.booking.AirplaneBooking.AirplaneBookingState;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AirplaneBookingStateConverter implements AttributeConverter<AirplaneBookingState, String> {

    @Override
    public String convertToDatabaseColumn(AirplaneBookingState attribute) {
        return attribute.name();
    }

    @Override
    public AirplaneBookingState convertToEntityAttribute(String dbData) {
        return AirplaneBookingState.valueOf(dbData);
    }
}
