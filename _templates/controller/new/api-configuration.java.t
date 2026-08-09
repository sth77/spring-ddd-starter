---
to: src/main/java/com/example/app/<%= h.changeCase.lower(feature) %>/web/<%= Name %>ApiConfiguration.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package <%= FeatureWebPackage %>;

import <%= CommonPackage %>.web.ProjectionLinks;
import <%= FeaturePackage %>.<%= AggregateType %>;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class <%= Name %>ApiConfiguration {

    /**
     * Ensures that projections are equipped with the same links as the aggregate.
     */
    @Bean
    ProjectionLinks<<%= AggregateType %>> <%= aggregateName %>ProjectionLinks(<%= AggregateType %>Links delegate) {
        return new ProjectionLinks<>(delegate, <%= AggregateType %>.class);
    }

}
