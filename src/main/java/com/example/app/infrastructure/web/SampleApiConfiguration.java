package com.example.app.infrastructure.web;

import com.example.app.domain.sample.Sample;
import com.example.app.domain.sample.web.SampleLinks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleApiConfiguration {

    @Bean
    ProjectionLinks<Sample> sampleProjectionLinks(SampleLinks delegate) {
        return new ProjectionLinks<>(delegate, Sample.class);
    }

}
