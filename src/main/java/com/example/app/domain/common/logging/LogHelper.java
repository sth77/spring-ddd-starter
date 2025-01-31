package com.example.app.domain.common.logging;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.slf4j.event.Level;

import java.util.Optional;

@UtilityClass
public class LogHelper {

    public static final Level DEFAULT_LEVEL = Level.INFO;

    public static Level getLevel(Object object) {
        val annotatedLevel = Optional.ofNullable(object.getClass().getAnnotation(LogLevel.class));
        return annotatedLevel
                .map(LogLevel::value)
                .orElse(DEFAULT_LEVEL);
    }

}