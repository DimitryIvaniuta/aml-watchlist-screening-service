package com.github.dimitryivaniuta.gateway.aml.api.dto;

import com.github.dimitryivaniuta.gateway.aml.dlq.DlqApprovalStatus;
import com.github.dimitryivaniuta.gateway.aml.dlq.DlqMessageStatus;
import java.time.Instant;
import java.util.UUID;

/** DLQ message DTO for ops UI. */
public record DlqMessageDto(
    Long id,
    String topic,
    int partition,
    long offset,
    String messageKey,
    String eventType,
    UUID outboxId,
    String aggregateType,
    String aggregateId,
    String error,
    Instant receivedAt,
    DlqMessageStatus status,
    int replayAttempts,
    Instant nextAttemptAt,
    String lastError,
    Instant replayedAt,
    DlqApprovalStatus approvalStatus,
    String requestedBy,
    Instant requestedAt,
    String approvedBy,
    Instant approvedAt,
    String correlationId
) {}
