package com.example.app.referencedata;

import com.example.app.common.model.I18nText;
import java.util.UUID;
import lombok.Value;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jmolecules.ddd.types.Identifier;

/**
 * Reference (master) data: an entity with a lifecycle of its own, served read-only over REST for selection. An
 * aggregate refers to it by URI in a command and embeds a copy of the fields it needs instead of associating with it
 * (see the README, "Master data handling").
 */
@Value
@Entity
public class City {

    @Identity
    private final CityId id;

    private final int postalCode;

    private final I18nText name;

    public static City ofPostalCodeAndName(int postalCode, I18nText name) {
        return new City(CityId.random(), postalCode, name);
    }

    public record CityId(UUID uuidValue) implements Identifier {

        public static CityId random() {
            return CityId.of(UUID.randomUUID());
        }

        public static CityId of(UUID uuidValue) {
            return new CityId(uuidValue);
        }

        @Override
        public String toString() {
            return uuidValue.toString();
        }
    }
}
