---
to: src/main/java/com/example/app/_infrastructure/web/<%= Name %>ApiConfiguration.java
---
<%
   include(`${templates}/variables.ejs`)
-%>
package com.example.app._infrastructure.web;

import <%= FeaturePackage %>.<%= AggregateType %>;
import <%= FeatureWebPackage %>.<%= AggregateType %>EntityLinks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class <%= Name %>ApiConfiguration {

    @Bean
    ProjectionLinks<<%= AggregateType %>> <%= aggregateName %>ProjectionLinks(<%= AggregateType %>EntityLinks delegate) {
        return new ProjectionLinks<>(delegate, <%= AggregateType %>.class);
    }

}
