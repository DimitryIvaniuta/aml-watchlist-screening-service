package com.github.dimitryivaniuta.gateway.aml.api.dto;

import com.github.dimitryivaniuta.gateway.aml.domain.ScreeningDecision;
import java.time.Instant;
import java.util.UUID;

/** Screening result DTO for API responses. */
public record ScreeningResultDto(
    UUID screeningId,
    UUID transactionId,
    ScreeningDecision decision,
    int score,
    String reasonsJson,
    String engineVersion,
    Long watchlistBatchId,
    String correlationId,
    Instant createdAt
) {}
