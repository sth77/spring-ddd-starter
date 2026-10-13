package com.example.app._infrastructure.web;

import com.example.app._application.ApplicationProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
public class WebConfiguration implements RepositoryRestConfigurer {

    public static final String BASE_PATH = "/api";

    private static final HttpMethod[] WRITE_METHODS = {
            HttpMethod.POST, HttpMethod.PATCH, HttpMethod.PUT, HttpMethod.DELETE
    };

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        // Nothing is written through the generic CRUD API: an aggregate changes through the operations of its
        // root, which the operations controller exposes and secures per role; reference data is master data,
        // offered to clients for selection only.
        config.getExposureConfiguration()
                .withCollectionExposure((metadata, httpMethods) -> httpMethods.disable(WRITE_METHODS))
                .withItemExposure((metadata, httpMethods) -> httpMethods.disable(WRITE_METHODS));
        config.setBasePath(BASE_PATH);
    }

    /**
     * Enforces the write lockdown above before Spring Data REST binds a request body, see
     * {@link GenericCrudWriteInterceptor}. Spring Data REST's handler mappings pick up mapped interceptor beans.
     */
    @Bean
    public MappedInterceptor genericCrudWriteInterceptor() {
        return new MappedInterceptor(new String[]{BASE_PATH + "/**"}, new GenericCrudWriteInterceptor());
    }

    @Bean
    public OpenAPI customOpenAPI(ApplicationProperties applicationProperties) {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(
                                "basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")))
                .info(new Info()
                        .title(applicationProperties.getName() + " API")
                        .version(applicationProperties.getVersion()));
    }
}
