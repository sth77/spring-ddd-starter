package com.example.app;

import com.example.app.application.ApplicationProperties;
import com.example.app.infra.security.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.modulith.Modulith;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;

@Modulith
@EnableHypermediaSupport(type = HAL)
@EnableConfigurationProperties({ApplicationProperties.class, SecurityProperties.class})
public class SampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}

}
