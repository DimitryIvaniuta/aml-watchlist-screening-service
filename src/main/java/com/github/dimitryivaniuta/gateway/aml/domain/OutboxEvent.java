package com.github.dimitryivaniuta.gateway.aml.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbox event record for publish-after-commit semantics.
 */
@Entity
@Table(name = "outbox_events")
@Getter @Setter @NoArgsConstructor
public class OutboxEvent {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "aggregate_type", nullable = false, length = 64)
  private String aggregateType;

  @Column(name = "aggregate_id", nullable = false, length = 128)
  private String aggregateId;

  @Column(name = "event_type", nullable = false, length = 128)
  private String eventType;

  @Column(columnDefinition = "jsonb", nullable = false)
  private String payload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(nullable = false)
  private int attempts;

  @Column(name = "next_attempt_at")
  private Instant nextAttemptAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private OutboxStatus status;

  @Column(name = "last_error", length = 512)
  private String lastError;

  @Column(name = "correlation_id", length = 128)
  private String correlationId;
}
