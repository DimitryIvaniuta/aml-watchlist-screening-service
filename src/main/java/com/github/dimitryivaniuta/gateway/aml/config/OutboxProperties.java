package com.github.dimitryivaniuta.gateway.aml.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Outbox retry + DLQ properties.
 */
@ConfigurationProperties(prefix = "aml.outbox")
public record OutboxProperties(
    int maxAttempts,
    long initialBackoffMillis,
    long maxBackoffMillis,
    double jitterRatio,
    String dlqTopic
) {}
