---
to: src/main/java/com/example/app/infrastructure/web/<%= Name %>ApiConfiguration.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package com.example.app.infrastructure.web;

import <%= FeaturePackage %>.<%= AggregateType %>;
import <%= FeatureWebPackage %>.<%= AggregateType %>Detail;
import <%= FeatureWebPackage %>.<%= AggregateType %>Links;
import <%= FeatureWebPackage %>.<%= AggregateType %>Summary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class <%= Name %>ApiConfiguration {

    @Bean
    ProjectionLinks<<%= AggregateType %>> <%= aggregateName %>SummaryLinks(<%= AggregateType %>Links delegate) {
        return new ProjectionLinks<>(delegate, <%= AggregateType %>.class);
    }

}
