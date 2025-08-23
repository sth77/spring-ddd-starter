---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= Name %>Command.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= CommonPackage %>.model.Command;
import lombok.Builder;
import lombok.NonNull;

public sealed interface <%= CommandType %> extends Command {

    @Builder
    record <%= CreateCommandType %>(
        @NonNull String name
        // TODO add the fields required to create the aggregate
        ) implements <%= CommandType %> { }

    @Builder
    record <%= UpdateCommandType %>(
        @NonNull String name) implements <%= CommandType %> { }

    record <%= PublishCommandType %>() implements <%= CommandType %> { }

}
