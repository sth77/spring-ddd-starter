---
to: src/main/java/com/example/app/referencedata/<%= Name %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= ReferenceDataPackage %>;

import <%= CommonPackage %>.model.I18nText;
import lombok.Value;
import org.jmolecules.ddd.annotation.Entity;
import org.jmolecules.ddd.annotation.Identity;
import org.jmolecules.ddd.types.Identifier;

import java.util.UUID;

/**
 * Reference (master) data: an entity with a lifecycle of its own, served read-only over REST for selection. An
 * aggregate refers to it by URI in a command and embeds a copy of the fields it needs instead of associating with it
 * (see the README, "Master data handling").
 */
@Value
@Entity
public class <%= ReferenceDataType %> {

    @Identity
    private final <%= IdType %> id;

    private final I18nText name;

    public static <%= ReferenceDataType %> of(I18nText name) {
        return new <%= ReferenceDataType %>(<%= IdType %>.random(), name);
    }

    public record <%= IdType %>(UUID uuidValue) implements Identifier {

        public static <%= IdType %> random() {
            return <%= IdType %>.of(UUID.randomUUID());
        }

        public static <%= IdType %> of(UUID uuidValue) {
            return new <%= IdType %>(uuidValue);
        }

        @Override
        public String toString() {
            return uuidValue.toString();
        }
    }

}
