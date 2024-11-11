package com.example.app.infra.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataCreator {

    @EventListener(ApplicationReadyEvent.class)
    void initData() {
        /*
         * TODO:
         * 1) declare dependencies to your repositories as final fields
         * 2) create some test data useful for developing the application
         */
    }

}
