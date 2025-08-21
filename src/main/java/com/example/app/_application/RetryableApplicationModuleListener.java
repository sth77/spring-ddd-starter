package com.example.app._application;

import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.SQLException;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Async
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {
        SQLException.class,
        IOException.class
})
@TransactionalEventListener
@Retryable(
        maxAttempts = RetryableApplicationModuleListener.ATTEMPTS_NUMBER,
        retryFor = {
                SQLException.class,
                IOException.class,
                RuntimeException.class,
                Error.class
        }
)
public @interface RetryableApplicationModuleListener {
    int ATTEMPTS_NUMBER = 5;

    /**
     * Override this value if another threshold is needed.
     *
     * @return attempts
     */
    int attempts() default ATTEMPTS_NUMBER;
}