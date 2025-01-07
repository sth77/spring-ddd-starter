package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;

@Modulith
@EnableAsync
@EnableHypermediaSupport(type = HAL)
@ConfigurationPropertiesScan
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

}
