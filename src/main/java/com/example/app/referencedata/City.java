package com.example.app.referencedata;

import com.example.app.common.model.I18nText;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jmolecules.ddd.types.Identifier;

import java.util.UUID;

@Entity
@Getter
@Slf4j
@ToString
@AllArgsConstructor
public class City {

    @Identity
    private final CityId id;

    private final int postalCode;

    private final I18nText name;

    public static City ofPostalCodeAndName(int postalCode, @NonNull I18nText name) {
        return log(new City(CityId.random(), postalCode, name));
    }

    public record CityId(UUID id) implements Identifier {
        static CityId random() {
            return new CityId(UUID.randomUUID());
        }
    }

    private static City log(City city) {
        log.info("City: " + city);
        return city;
    }

}
