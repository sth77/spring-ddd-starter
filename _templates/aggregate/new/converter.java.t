---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= Name %>StateConverter.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= CommonPackage %>.persistence.EnumConverter;
import <%= FeaturePackage %>.<%= AggregateType %>.<%= StateType %>;
import jakarta.persistence.Converter;

/**
 * Persists {@link <%= StateType %>} by name (matching the {@code VARCHAR(20)} schema column) rather than the
 * fragile ordinal default, without annotating the aggregate itself. Applied automatically to every
 * {@link <%= StateType %>} attribute, keeping the domain model free of persistence annotations.
 */
@Converter(autoApply = true)
class <%= StateType %>Converter extends EnumConverter<<%= StateType %>> {

    <%= StateType %>Converter() {
        super(<%= StateType %>.class);
    }
}
