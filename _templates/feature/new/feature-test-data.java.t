---
to: src/test/java/com/example/app/<%= name %>/<%= Name %>TestData.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import lombok.experimental.UtilityClass;
import lombok.val;

import static com.example.app.AggregateEvents.clearEvents;

@UtilityClass
public class <%= TestDataType %> {

}
