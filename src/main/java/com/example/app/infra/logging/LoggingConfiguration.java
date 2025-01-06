package com.example.app.infra.logging;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class LoggingConfiguration {

    @Bean
    @Primary
    ApplicationEventPublisher applicationEventPublisher(ApplicationContext applicationContext) {
        log.info("Instantiating logging application event publisher");
        return new LoggingApplicationEventPublisher(applicationContext);
    }

}
