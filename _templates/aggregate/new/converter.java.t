---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= Name %>StateConverter.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= StateType %>;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jspecify.annotations.Nullable;

/**
 * Persists {@link <%= StateType %>} by name (matching the {@code VARCHAR(20)} schema column) rather than the
 * fragile ordinal default, without annotating the aggregate itself. Hibernate lacks a global "enums as string"
 * switch, so the canonical approach is one {@code autoApply} converter per enum: it is applied automatically to
 * every {@link <%= StateType %>} attribute, keeping the domain model free of persistence annotations.
 */
@Converter(autoApply = true)
class <%= StateType %>Converter implements AttributeConverter<<%= StateType %>, String> {

    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable <%= StateType %> state) {
        return state == null ? null : state.name();
    }

    @Override
    public @Nullable <%= StateType %> convertToEntityAttribute(@Nullable String value) {
        return value == null ? null : <%= StateType %>.valueOf(value);
    }
}
