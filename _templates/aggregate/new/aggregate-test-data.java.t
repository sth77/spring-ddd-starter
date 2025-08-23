---
to: src/test/java/com/example/app/<%= h.changeCase.lower(feature) %>/<%= Name %>TestData.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeaturePackage %>;

import <%= FeaturePackage %>.<%= CommandType %>.<%= CreateCommandType %>;
import lombok.experimental.UtilityClass;
import lombok.val;

import static com.example.app.AggregateEvents.clearEvents;

@UtilityClass
public class <%= AggregateType %>TestData {

    public static <%= AggregateType %> <%= aggregateName %>() {
        return <%= aggregateName %>("<%= AggregateType %> X");
    }

    public static <%= AggregateType %> <%= aggregateName %>(String name) {
        val result = <%= AggregateType %>.create(<%= CreateCommandType %>.builder()
                .name(name)
                .build());
        clearEvents(result);
        return result;
    }

}
