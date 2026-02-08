package com.github.dimitryivaniuta.gateway.aml.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduled jobs (outbox publishing, DLQ replay).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {}
