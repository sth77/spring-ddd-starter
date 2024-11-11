package com.example.app.infra.restapi;

import com.example.app.application.ApplicationProperties;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.jmolecules.ddd.types.AggregateRoot;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RestApiConfiguration implements RepositoryRestConfigurer {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .annotationIntrospector(new LombokJacksonAnnotationIntrospector())
                .modules(
                        new Jackson2HalModule(),
                        new JavaTimeModule(),
                        new Jdk8Module());
    }

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // for aggregates, force modifying operations to go through the aggregate operation controller instead of using
        // the Spring Data REST CRUD API.
        config.getExposureConfiguration().withCollectionExposure((metadata, httpMethods)
                -> AggregateRoot.class.isAssignableFrom(metadata.getDomainType())
                ? httpMethods.disable(HttpMethod.POST, HttpMethod.PATCH, HttpMethod.PUT)
                : httpMethods);
        config.getExposureConfiguration().withItemExposure((metadata, httpMethods)
                -> AggregateRoot.class.isAssignableFrom(metadata.getDomainType())
                ? httpMethods.disable(HttpMethod.POST, HttpMethod.PATCH, HttpMethod.PUT)
                : httpMethods);
    }

    @Bean
    public OpenAPI customOpenAPI(ApplicationProperties applicationProperties) {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .info(new Info()
                        .title(applicationProperties.getName() + " API")
                        .version(applicationProperties.getVersion()));
    }
}
