---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= Name %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= CommonPackage %>.model.AbstractAggregate;
import <%= CommonPackage %>.model.DomainException;
import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= UpdateCommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= PublishCommandType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= PublishedEventType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= CreatedEventType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= UpdatedEventType %>;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import java.util.Objects;
import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class <%= AggregateType %> extends AbstractAggregate<<%= Name %>, <%= IdType %>> implements AggregateRoot<<%= AggregateType %>, <%= IdType %>> {

    private final <%= IdType %> id;
    private String name;
    private <%= StateType %> state;

    public static <%= AggregateType %> create(<%= CreateCommandType %> data) {
        val result = new <%= AggregateType %>(
            <%= IdType %>.random(),
            data.name(),
            <%= StateType %>.DRAFT);
        result.registerEvent(new <%= CreatedEventType %>(result.id));
        return result;
    }

    public void update(<%= UpdateCommandType %> data) {
        assertCan(data.getClass());
        if (!Objects.equals(this.name, data.name())) {
            this.name = data.name();
            registerEvent(new <%= UpdatedEventType %>(id, name));
        }
    }

    public void publish() {
        assertCan(<%= PublishCommandType %>.class);
        state = <%= StateType %>.PUBLISHED;
        registerEvent(new <%= PublishedEventType %>(id, name));
    }

    private void assertCan(Class<? extends <%= CommandType %>> command) {
        if (!can(command)) {
            throw new DomainException("Command %s not allowed for <%= aggregateName %> in state %s"
                    .formatted(command.getSimpleName(), state));
        }
    }

    public boolean can(Class<? extends <%= CommandType %>> operation) {
        if (operation.equals(<%= CreateCommandType %>.class)) {
            return false;
        }
        return state != <%= StateType %>.PUBLISHED;
    }

    public record <%= IdType %>(@JsonValue UUID id) implements Identifier {

        public static <%= IdType %> random() {
            return <%= IdType %>.of(UUID.randomUUID());
        }

        public static <%= IdType %> of(UUID id) {
            return new <%= IdType %>(id);
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    public enum <%= StateType %> {
        DRAFT,
        PUBLISHED
    }

}
