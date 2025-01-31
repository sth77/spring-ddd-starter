---
to: src/main/java/com/example/app/domain/<%= name %>/web/<%= Name + "Summary" %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Summary projection of a {@link <%= AggregateType %>}. Can be used by clients of the API to get a compact
 * representation of the aggregate suitable for rendering it in a list view.
 */
@Projection(name = "summary", types = {<%= AggregateType %>.class})
public interface <%= AggregateType %>Summary {

    String getName();

    // TODO: declare other fields to show in the summary.
    // You can load related entities or parts of it using a Spring expression (SPEL) like this:
    // @Value("#{@people.resolveRequired(target.owner).getName()}")
    // String getOwner();

}
