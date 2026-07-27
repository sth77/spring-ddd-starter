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
        static <%= IdType %> random() {
            return new <%= IdType %>(UUID.randomUUID());
        }
    }

}
