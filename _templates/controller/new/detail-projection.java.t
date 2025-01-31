---
to: src/main/java/com/example/app/domain/<%= name %>/web/<%= Name + "Detail" %>.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= FeaturePackage %>.<%= AggregateType %>;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Detail projection of a {@link <%= AggregateType %>}. Can be used by clients of the API to get all data of the
 * aggregate suitable for rendering a detail view.
 */
@Projection(name = "detail", types = {<%= AggregateType %>.class})
public interface <%= AggregateType %>Detail {

    String getName();

    // TODO: declare other fields to show in the detail projection.
    // You can load related entities or parts of it using a Spring expression (SPEL) like this:
    // @Value("#{@people.resolveRequired(target.owner).getName()}")
    // String getOwner();

}
