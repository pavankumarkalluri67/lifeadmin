package com.LifeAdmin.ai.lifeadmin.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * AsyncConfig: configures async task execution.
 * Requirement 26
 */
@Component
@EnableAsync
public class AsyncConfig {

    @Bean(name = "processingTaskExecutor")
    public Executor processingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("processing-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ai-");
        executor.initialize();
        return executor;
    }
}
