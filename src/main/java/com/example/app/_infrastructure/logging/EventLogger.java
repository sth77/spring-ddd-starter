package com.example.app._infrastructure.logging;

import com.example.app.common.logging.LogPrefix;
import com.example.app.common.model.Command;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.event.types.DomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.example.app.common.logging.LogHelper.getLevel;

@Slf4j
@Component
public class EventLogger {

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    void on(DomainEvent event) {
        log.atLevel(getLevel(event)).log(LogPrefix.EVENT.withText("" + event));
    }

    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    void on(Command command) {
        log.atLevel(getLevel(command)).log(LogPrefix.COMMAND.withText("" + command));
    }

}
