package com.github.dimitryivaniuta.gateway.aml.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async executor for screening.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean
  public Executor screeningExecutor() {
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setCorePoolSize(8);
    ex.setMaxPoolSize(32);
    ex.setQueueCapacity(1000);
    ex.setThreadNamePrefix("screening-");
    ex.initialize();
    return ex;
  }
}
