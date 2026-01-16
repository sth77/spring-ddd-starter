package com.example.app.common.model;

import lombok.Builder;
import org.jmolecules.ddd.annotation.ValueObject;

@Builder
@ValueObject
public record I18nText(String en, String de) {

    public static I18nText en(String en) {
        return new I18nText(en, "DE_" + en);
    }
}
