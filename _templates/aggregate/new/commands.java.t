---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= Name %>Command.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= CommonPackage %>.model.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * The commands of the {@link <%= AggregateType %>} aggregate, one record per operation. A command is pure payload: the aggregate it
 * applies to is addressed by the URI. The operations controller exposes each command under a rel derived from the
 * record name without the aggregate name ({@code <%= UpdateCommandType %>} becomes {@code update}), unless {@code @Relation} overrides it.
 * The Bean Validation constraints of the components are validated on the request and describe the fields of the
 * operation's HAL-FORMS template.
 */
public sealed interface <%= CommandType %> extends Command {

    @Builder
    record <%= CreateCommandType %>(
        @NotBlank String name
        // TODO add the fields required to create the aggregate
        ) implements <%= CommandType %> { }

    @Builder
    record <%= UpdateCommandType %>(
        @NotBlank String name) implements <%= CommandType %> { }

    record <%= PublishCommandType %>() implements <%= CommandType %> {
        public static <%= PublishCommandType %> create() {
            return new <%= PublishCommandType %>();
        }
    }

}
