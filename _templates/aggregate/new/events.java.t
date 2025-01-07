---
to: src/main/java/com/example/app/domain/<%= h.changeCase.lower(feature) %>/<%= Name %>Event.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.jmolecules.event.types.DomainEvent;

public sealed interface <%= EventType %> extends DomainEvent {

    <%= IdType %> <%= idName %>();

    @Builder
    record <%= CreatedEventType %>(
        @NotNull <%= IdType %> <%= idName %>) implements <%= EventType %> {

        static <%= CreatedEventType %> of(<%= IdType %> <%= idName %>) {
            return new <%= CreatedEventType %>(<%= idName %>);
        }
    }

    @Builder
    record <%= NameUpdatedEventType %>(
        @NotNull <%= IdType %> <%= idName %>,
        @NotNull String name) implements <%= EventType %> {
    }

}
