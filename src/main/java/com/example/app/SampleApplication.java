package com.example.app;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.modulith.Modulith;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;

// common is the shared kernel every feature builds on: module tests (@ApplicationModuleTest) bootstrap its beans
// - such as the request-scoped ChildEntityLinks - no matter which module they test
@Modulith(sharedModules = "common")
@EnableAsync
@EnableResilientMethods
@EnableHypermediaSupport(type = HAL)
@ConfigurationPropertiesScan
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}
