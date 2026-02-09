package com.github.dimitryivaniuta.gateway.aml.dlq;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DLQ replay job configuration.
 */
@ConfigurationProperties(prefix = "aml.dlq.replay")
public record DlqReplayProperties(
    boolean enabled,
    int batchSize,
    int maxAttempts,
    long initialBackoffMillis,
    long maxBackoffMillis,
    double jitterRatio,
    long fixedDelayMillis
) {}
