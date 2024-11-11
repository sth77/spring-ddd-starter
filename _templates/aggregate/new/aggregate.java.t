---
to: src/main/java/com/example/app/<%= h.changeCase.lower(name) %>/<%= Name %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= RootPackage %>.common.AbstractAggregate;
import <%= FeaturePackage %>.<%= AggregateType %>.<%= IdType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
import <%= FeaturePackage %>.<%= CommandType %>.<%= UpdateNameCommandType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= CreatedEventType %>;
import <%= FeaturePackage %>.<%= EventType %>.<%= NameUpdatedEventType %>;
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
    private <%= StateType %> state = <%= StateType %>.DRAFT;
    private String name;

    public static <%= AggregateType %> create(<%= CreateCommandType %> data) {
        val result = new <%= AggregateType %>(<%= IdType %>.random());
        result.name = data.name();
        result.registerEvent(<%= CreatedEventType %>.of(result.getId()));
        return result;
    }

    public <%= AggregateType %> updateName(<%= UpdateNameCommandType %> data) {
        if (!Objects.equals(this.name, data.name())) {
            this.name = data.name();
            registerEvent(<%= NameUpdatedEventType %>.of(id));
        }
        return this;
    }

    public boolean can(Operation operation) {
        return true;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Operation {
        CREATE("create"),
        UPDATE_NAME("updateName");

        public final String rel;
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
        DRAFT
    }

}
