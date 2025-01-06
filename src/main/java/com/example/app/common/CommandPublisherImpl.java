package com.example.app.common;

import com.example.app.common.logging.LogPrefix;
import com.example.app.common.model.Command;
import org.jmolecules.ddd.annotation.Service;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
class CommandPublisherImpl implements CommandPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishCommand(Command command) {
        val caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        LoggerFactory.getLogger(caller).info(LogPrefix.COMMAND.withText("" + command));
        applicationEventPublisher.publishEvent(command);
    }

}