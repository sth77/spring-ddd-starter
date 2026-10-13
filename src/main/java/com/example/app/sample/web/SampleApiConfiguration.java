package com.example.app.sample.web;

import com.example.app.common.web.ProjectionLinks;
import com.example.app.sample.Sample;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The REST API configuration of the {@link Sample} aggregate.
 */
@Configuration
public class SampleApiConfiguration {

    /**
     * Ensures that projections are equipped with the same links as the aggregate.
     */
    @Bean
    ProjectionLinks<Sample> sampleProjectionLinks(SampleLinks delegate) {
        return new ProjectionLinks<>(delegate, Sample.class);
    }
}
