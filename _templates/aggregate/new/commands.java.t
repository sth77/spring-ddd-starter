---
to: src/main/java/com/example/app/<%= h.changeCase.lower(name) %>/<%= Name %>Command.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= RootPackage %>.common.model.Command;
import lombok.Builder;

public sealed interface <%= CommandType %> extends Command {

    @Builder
    record <%= CreateCommandType %>(
        String name
        // TODO add the fields required to create the aggregate
        ) implements <%= CommandType %> { }

    @Builder
    record <%= UpdateNameCommandType %>(
        String name) implements <%= CommandType %> { }

}
