---
to: src/main/java/com/example/app/referencedata/<%= Name %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= ReferenceDataPackage %>;

import <%= CommonPackage %>.model.I18nText;
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
public class <%= ReferenceDataType %> {

    @Identity
    private final <%= IdType %> id;

    private final I18nText name;

    public static <%= ReferenceDataType %> of(@NonNull I18nText name) {
        return new <%= ReferenceDataType %>(<%= IdType %>.random(), name);
    }

    public record <%= IdType %>(UUID id) implements Identifier {
        static <%= IdType %> random() {
            return new <%= IdType %>(UUID.randomUUID());
        }
    }

}
