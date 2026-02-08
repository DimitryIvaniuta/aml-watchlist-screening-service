package com.github.dimitryivaniuta.gateway.aml.events;

import java.util.UUID;

/**
 * DLQ event for outbox publish failures.
 */
public record OutboxDlqEvent(
    UUID outboxId,
    String eventType,
    String aggregateType,
    String aggregateId,
    String payload,
    String error
) {}
