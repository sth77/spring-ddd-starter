package com.example.app.infra.logging;

import com.example.app.common.logging.LogPrefix;
import com.example.app.common.model.Command;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
//@Aspect
@Component
public class EventLogger {

    @EventListener
    void on(DomainEvent event) {
        log.info(LogPrefix.EVENT.withText("! " + event));
    }

    @EventListener
    void on(Command command) {
        log.info(LogPrefix.COMMAND.withText("! " + command));
    }

    /*
    It would be desirable to log the event _before_ any listener has
    been invoked, to see the log before other logs induced by event
    processing. Unfortunately, the join point is not picked up by
    Spring, possibly because the event multicaster is not being proxied.

    @Before("execution(* multicastEvent(event, ..)) && args(event, ..))")
    public void log(ApplicationEvent event) {
        if (event instanceof DomainEvent domainEvent) {
            log.info(LogPrefix.EVENT.withText("" + domainEvent));
        } else if (event instanceof Command command) {
            log.info(LogPrefix.COMMAND.withText("" + command));
        }
    }
     */

}
