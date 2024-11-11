package com.example.app.common;

import org.jmolecules.event.types.DomainEvent;

public interface DomainEventPublisher {
    
    void publishEvent(DomainEvent event);

}
