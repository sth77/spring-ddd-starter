---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= Name %>Event.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import lombok.Builder;
import lombok.NonNull;
import org.jmolecules.event.types.DomainEvent;

public sealed interface <%= EventType %> extends DomainEvent {

    <%= IdType %> <%= idName %>();

    record <%= CreatedEventType %>(
        <%= IdType %> <%= idName %>) implements <%= EventType %> {
        static <%= CreatedEventType %> of(<%= IdType %> <%= idName %>) {
            return new <%= CreatedEventType %>(<%= idName %>);
        }
    }

    @Builder
    record <%= UpdatedEventType %>(
        @NonNull <%= IdType %> <%= idName %>,
        @NonNull String name) implements <%= EventType %> {
    }

    @Builder
    record <%= PublishedEventType %>(
        @NonNull <%= IdType %> <%= idName %>,
        @NonNull String name) implements <%= EventType %> {
    }

}
