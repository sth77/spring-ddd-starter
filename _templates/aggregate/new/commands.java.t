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

    record <%= CreateCommandType %>(
        String name
        // TODO add the fields required to create the aggregate
        ) implements <%= CommandType %> { }

    record <%= UpdateCommandType %>(
        String name) implements <%= CommandType %> { }

    record <%= PublishCommandType %>() implements <%= CommandType %> { }

}
