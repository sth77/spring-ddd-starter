package com.example.app._infrastructure.web;

import com.example.app.sample.Sample;
import com.example.app.sample.web.SampleEntityLinks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleApiConfiguration {

    @Bean
    ProjectionLinks<Sample> sampleProjectionLinks(SampleEntityLinks delegate) {
        return new ProjectionLinks<>(delegate, Sample.class);
    }

}
