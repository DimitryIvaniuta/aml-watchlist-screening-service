package com.github.dimitryivaniuta.gateway.aml.events;

import com.github.dimitryivaniuta.gateway.aml.domain.ScreeningDecision;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a screening result is created.
 */
public record ScreeningResultCreatedEvent(
    UUID screeningId,
    UUID transactionId,
    ScreeningDecision decision,
    int score,
    Long watchlistBatchId,
    String engineVersion,
    String correlationId,
    Instant createdAt
) {}
