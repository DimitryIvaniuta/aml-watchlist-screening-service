package com.github.dimitryivaniuta.gateway.aml.dlq;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stored copy of a Kafka DLQ record for replay/triage.
 *
 * <p>Idempotency: unique(topic, partition, offset).
 */
@Entity
@Table(
    name = "dlq_messages",
    uniqueConstraints = @UniqueConstraint(name = "ux_dlq_messages_topic_partition_offset", columnNames = {"topic", "partition", "offset"})
)
@Getter @Setter @NoArgsConstructor
public class DlqMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String topic;

  @Column(nullable = false)
  private int partition;

  @Column(nullable = false)
  private long offset;

  @Column(name = "message_key", length = 500)
  private String messageKey;

  @Column(name = "event_type", nullable = false, length = 200)
  private String eventType;

  @Column(name = "outbox_id")
  private UUID outboxId;

  @Column(name = "aggregate_type", length = 200)
  private String aggregateType;

  @Column(name = "aggregate_id", length = 200)
  private String aggregateId;

  /** JSON payload of the original event (stored as JSONB). */
  @Column(nullable = false, columnDefinition = "jsonb")
  private String payload;

  @Column(columnDefinition = "text")
  private String error;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt = Instant.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DlqMessageStatus status = DlqMessageStatus.PENDING;

  @Column(name = "replay_attempts", nullable = false)
  private int replayAttempts = 0;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt = Instant.now();

  @Column(name = "last_error", columnDefinition = "text")
  private String lastError;

  @Column(name = "replayed_at")
  private Instant replayedAt;

  @Column(name = "correlation_id", length = 128)
  private String correlationId;

  @Enumerated(EnumType.STRING)
  @Column(name = "approval_status", nullable = false, length = 20)
  private DlqApprovalStatus approvalStatus = DlqApprovalStatus.NONE;

  @Column(name = "requested_by", length = 128)
  private String requestedBy;

  @Column(name = "requested_at")
  private Instant requestedAt;

  @Column(name = "approved_by", length = 128)
  private String approvedBy;

  @Column(name = "approved_at")
  private Instant approvedAt;
}
