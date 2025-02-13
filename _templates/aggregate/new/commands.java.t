---
to: src/main/java/com/example/app/domain/<%= h.changeCase.lower(feature) %>/<%= Name %>Command.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= RootPackage %>.domain.common.model.Command;
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
