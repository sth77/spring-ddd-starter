package com.example.app.common.model;

import lombok.Builder;
import lombok.Value;
import org.jmolecules.ddd.annotation.ValueObject;

@Value
@Builder
@ValueObject
public class I18nText {

    String en;

    String de;

    public static I18nText en(String en) {
        return I18nText.builder()
                .en(en)
                .de("DE_" + en)
                .build();
    }
}
