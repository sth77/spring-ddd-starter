package com.example.app.sample.web;

import com.example.app.common.web.ProjectionLinks;
import com.example.app.sample.Sample;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleApiConfiguration {

    @Bean
    ProjectionLinks<Sample> sampleProjectionLinks(SampleLinks delegate) {
        return new ProjectionLinks<>(delegate, Sample.class);
    }
}
