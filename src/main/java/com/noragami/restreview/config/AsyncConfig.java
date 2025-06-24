package com.noragami.restreview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {
    @Bean
    public ExecutorService emailExecutorService() {
        return Executors.newFixedThreadPool(5); // or use ThreadPoolTaskExecutor for more control
    }
}
