package com.example.app.common.logging;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.slf4j.event.Level;

@UtilityClass
public class LogHelper {

    public static final Level DEFAULT_LEVEL = Level.INFO;

    public static Level getLevel(Object object) {
        val annotatedLevel = Optional.ofNullable(object.getClass().getAnnotation(LogLevel.class));
        return annotatedLevel.map(LogLevel::value).orElse(DEFAULT_LEVEL);
    }
}
