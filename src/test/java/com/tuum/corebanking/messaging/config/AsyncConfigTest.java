package com.tuum.corebanking.messaging.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

    private AsyncConfig config;

    @BeforeEach
    void setUp() {
        config = new AsyncConfig();
    }

    @Test
    void eventPublisherExecutorShouldReturnThreadPoolTaskExecutor() {
        Executor executor = config.eventPublisherExecutor();

        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
    }

    @Test
    void eventPublisherExecutorShouldBeConfiguredWithExpectedPoolSettings() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.eventPublisherExecutor();

        assertThat(executor.getCorePoolSize()).isEqualTo(10);
        assertThat(executor.getMaxPoolSize()).isEqualTo(50);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("event-publisher-");
    }

    @Test
    void eventPublisherExecutorShouldBeInitializedAndReadyToAcceptTasks() throws Exception {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) config.eventPublisherExecutor();

        assertThat(executor.getThreadPoolExecutor()).isNotNull();

        Thread.sleep(50);
        java.util.concurrent.atomic.AtomicBoolean ran = new java.util.concurrent.atomic.AtomicBoolean(false);
        executor.execute(() -> ran.set(true));

        Thread.sleep(50);
        assertThat(ran.get()).isTrue();
    }
}