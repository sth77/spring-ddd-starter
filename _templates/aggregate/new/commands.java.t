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

public sealed interface <%= CommandType %> extends Command {

    @Builder
    record <%= CreateCommandType %>(
        @NotBlank String name
        // TODO add the fields required to create the aggregate
        ) implements <%= CommandType %> { }

    @Builder
    record <%= UpdateCommandType %>(
        @NotBlank String name) implements <%= CommandType %> { }

    record <%= PublishCommandType %>() implements <%= CommandType %> { }

}
