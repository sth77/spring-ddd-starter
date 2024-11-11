package com.example.app.common;

import org.jmolecules.ddd.annotation.Service;
import org.jmolecules.event.types.DomainEvent;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
class DomainEventPublisherImpl implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishEvent(DomainEvent event) {
        val caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        LoggerFactory.getLogger(caller).info("<e> {}", event);
        applicationEventPublisher.publishEvent(event);
    }

}