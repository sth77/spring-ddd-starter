---
to: src/main/java/com/example/app/domain/<%= h.changeCase.lower(feature) %>/<%= Name %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= RootPackage %>.domain.common.model.AbstractAggregate;
import <%= RootPackage %>.domain.common.model.DomainException;
import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= UpdateCommandType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= CreatedEventType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= UpdatedEventType %>;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class <%= AggregateType %> extends AbstractAggregate<<%= Name %>, <%= IdType %>> implements AggregateRoot<<%= AggregateType %>, <%= IdType %>>{

    private final <%= IdType %> id;
    private String name;
    private <%= StateType %> state = <%= StateType %>.DRAFT;

    public static <%= AggregateType %> create(<%= CreateCommandType %> data) {
        val result = new <%= AggregateType %>(<%= IdType %>.random());
        result.name = data.name();
        result.registerEvent(new <%= CreatedEventType %>(result.id));
        return result;
    }

    public <%= AggregateType %> update(<%= UpdateCommandType %> data) {
        assertCan(Operation.UPDATE);
        if (!Objects.equals(this.name, data.name())) {
            this.name = data.name();
            registerEvent(new <%= UpdatedEventType %>(id, name));
        }
        return this;
    }

    public <%= AggregateType %> publish() {
        assertCan(Operation.PUBLISH);
        state = <%= StateType %>.PUBLISHED;
        registerEvent(new <%= UpdatedEventType %>(id, name));
        return this;
    }

    private void assertCan(Operation operation) {
        if (!can(operation)) {
            throw new DomainException("Cannot do operation %s on aggregate in state %s"
                    .formatted(operation.key, state));
        }
    }

    public boolean can(Operation operation) {
        return state != <%= StateType %>.PUBLISHED;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Operation {
        UPDATE("update"),
        PUBLISH("publish");

        public final String key;
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
