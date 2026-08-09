package com.example.app.common.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class RetryableApplicationModuleListenerTest {

    @Autowired
    FailingListener listener;

    @Autowired
    ApplicationEventPublisher events;

    @Autowired
    TransactionTemplate transactions;

    /**
     * The retry has to happen inside the asynchronous execution of the listener - applied around it, it would
     * see the handler return immediately and never retry.
     */
    @Test
    void transientlyFailingHandler_isRetriedUntilItSucceeds() {
        listener.failTimes(2);

        transactions.executeWithoutResult(__ -> events.publishEvent(new SomethingHappened("event")));

        await().atMost(Duration.ofSeconds(5)).until(() -> listener.handled() == 1);
        assertThat(listener.attempts()).isEqualTo(3);
    }

    @Test
    void defaults_retryOnlyWhatAConcurrentWriterCauses() throws Exception {
        var method = FailingListener.class.getDeclaredMethod("on", SomethingHappened.class);

        var retryable = AnnotatedElementUtils.findMergedAnnotation(method, Retryable.class);

        assertThat(retryable).isNotNull();
        assertThat(retryable.includes()).containsExactly(
                OptimisticLockingFailureException.class, DataIntegrityViolationException.class);
        assertThat(retryable.maxRetries()).isEqualTo(4);
        assertThat(retryable.multiplier()).isEqualTo(2.0);
    }

    record SomethingHappened(String payload) {
    }

    /** Counters are read through methods: the bean is proxied for the retry, so its fields are not the target's. */
    static class FailingListener {

        private final AtomicInteger attempts = new AtomicInteger();
        private final AtomicInteger handled = new AtomicInteger();
        private final AtomicInteger failuresLeft = new AtomicInteger();

        @RetryableApplicationModuleListener(delay = 10, maxDelay = 20, jitter = 0)
        void on(SomethingHappened event) {
            attempts.incrementAndGet();
            if (failuresLeft.getAndDecrement() > 0) {
                throw new OptimisticLockingFailureException("concurrent write");
            }
            handled.incrementAndGet();
        }

        void failTimes(int times) {
            failuresLeft.set(times);
        }

        int attempts() {
            return attempts.get();
        }

        int handled() {
            return handled.get();
        }
    }

    @TestConfiguration
    static class Config {

        @Bean
        FailingListener failingListener() {
            return new FailingListener();
        }
    }
}
