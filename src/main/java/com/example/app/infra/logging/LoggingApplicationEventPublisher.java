package com.example.app.infra.logging;

import com.example.app.common.logging.LogHelper;
import com.example.app.common.logging.LogPrefix;
import com.example.app.common.model.Command;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class LoggingApplicationEventPublisher implements ApplicationEventPublisher {

    private final ApplicationContext delegate;

    @Override
    public void publishEvent(@Nonnull Object event) {
        if (event instanceof DomainEvent || event instanceof Command) {
            val prefix = event instanceof DomainEvent
                    ? LogPrefix.EVENT
                    : LogPrefix.COMMAND;
            log.atLevel(LogHelper.getLevel(event))
                    .log(prefix.withText("" + event));
        } else {
            log.info(LogPrefix.APPLICATION.withText("" + event));
        }
        delegate.publishEvent(event);
    }

    @EventListener
    void on(ApplicationEvent event) {
        log.info(LogPrefix.APPLICATION.withText("! " + event.getClass().getSimpleName()));
    }

}
