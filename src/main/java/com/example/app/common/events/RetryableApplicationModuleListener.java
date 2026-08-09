package com.example.app.common.events;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.resilience.annotation.Retryable;

/**
 * An {@link ApplicationModuleListener} that retries the failures a concurrent writer causes: two transactions
 * touching the same aggregate, where the loser can simply try again on the state the winner left behind.
 *
 * <p>
 * Everything else is left to the event publication registry, which keeps the failed publication for the next
 * application start. Retrying an exception that will fail again only delays that.
 *
 * <p>
 * Requires {@code @EnableResilientMethods} on a configuration class; the starter declares it on the application
 * class. Each attribute overrides one default, e.g. {@code @RetryableApplicationModuleListener(maxRetries = 10)}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApplicationModuleListener
@Retryable
public @interface RetryableApplicationModuleListener {

    /** Exception types to retry. Defaults to the two a concurrent writer provokes. */
    @AliasFor(annotation = Retryable.class, attribute = "includes")
    Class<? extends Throwable>[] includes() default {
            OptimisticLockingFailureException.class, DataIntegrityViolationException.class
    };

    /** Exception types never to retry, even when covered by {@link #includes()}. */
    @AliasFor(annotation = Retryable.class, attribute = "excludes")
    Class<? extends Throwable>[] excludes() default {};

    /** Attempts after the initial one. */
    @AliasFor(annotation = Retryable.class, attribute = "maxRetries")
    long maxRetries() default 4;

    /** Delay before the first retry, in milliseconds. */
    @AliasFor(annotation = Retryable.class, attribute = "delay")
    long delay() default 100;

    /** Factor the delay grows by after every attempt, so that contention eases off rather than repeats. */
    @AliasFor(annotation = Retryable.class, attribute = "multiplier")
    double multiplier() default 2.0;

    /** Upper bound for the growing delay, in milliseconds. */
    @AliasFor(annotation = Retryable.class, attribute = "maxDelay")
    long maxDelay() default 2000;

    /** Random spread added to each delay, in milliseconds, so competing handlers do not retry in lockstep. */
    @AliasFor(annotation = Retryable.class, attribute = "jitter")
    long jitter() default 50;

    /** Identifies the publication in the event publication registry; defaults to the method signature. */
    @AliasFor(annotation = ApplicationModuleListener.class, attribute = "id")
    String id() default "";

    /** SpEL expression deciding whether the event is handled at all. */
    @AliasFor(annotation = ApplicationModuleListener.class, attribute = "condition")
    String condition() default "";
}
