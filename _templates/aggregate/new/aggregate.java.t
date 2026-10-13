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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;

import java.util.Objects;
import java.util.UUID;

/**
 * The <%= AggregateType %> aggregate root, the consistency boundary of its feature. Its state changes only through the methods taking
 * one of the {@link <%= CommandType %>} records: each first checks {@link #can(Class)}, then changes the state and registers a
 * {@link <%= EventType %>}, which Spring publishes once the aggregate is saved through {@link <%= RepositoryType %>}. Add an operation with
 * {@code hygen operation new <Name> --aggregate=<%= AggregateType %> --feature=<feature>}.
 *
 * <p>
 * The class carries no JPA annotations: the jMolecules byte-buddy plugin adds the mapping at build time, and the
 * column names follow from the field names (see the README, "Persistence").
 */
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
        result.registerEvent(<%= CreatedEventType %>.of(result.id));
        return result;
    }

    public void update(<%= UpdateCommandType %> data) {
        assertCan(<%= UpdateCommandType %>.class);
        if (!Objects.equals(this.name, data.name())) {
            this.name = data.name();
            registerEvent(new <%= UpdatedEventType %>(id, name));
        }
    }

    public void publish(<%= PublishCommandType %> data) {
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

    /**
     * Whether the command is allowed in the aggregate's current state. Besides guarding the operations, it decides
     * whether the operation's HAL link is rendered (see {@code <%= AggregateType %>Links}).
     */
    public boolean can(Class<? extends <%= CommandType %>> operation) {
        if (operation.equals(<%= CreateCommandType %>.class)) {
            return false;
        }
        return state != <%= StateType %>.PUBLISHED;
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

    public enum <%= StateType %> {
        DRAFT,
        PUBLISHED
    }

}
