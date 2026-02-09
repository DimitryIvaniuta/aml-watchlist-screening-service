package com.github.dimitryivaniuta.gateway.aml.dlq;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Audit record for DLQ replay actions.
 */
@Entity
@Table(name = "dlq_replay_audit")
@Getter @Setter @NoArgsConstructor
public class DlqReplayAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "dlq_message_id", nullable = false)
  private DlqMessage message;

  @Column(nullable = false, length = 40)
  private String action;

  @Column(nullable = false, length = 30)
  private String status;

  @Column(name = "published_topic", length = 200)
  private String publishedTopic;

  @Column(name = "published_key", length = 500)
  private String publishedKey;

  @Column(columnDefinition = "text")
  private String error;

  @Column(name = "correlation_id", length = 128)
  private String correlationId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}
