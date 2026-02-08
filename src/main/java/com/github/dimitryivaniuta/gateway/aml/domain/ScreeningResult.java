package com.github.dimitryivaniuta.gateway.aml.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Screening result for a transaction.
 *
 * <p>Reasons are stored as JSONB for explainability and auditability.
 */
@Entity
@Table(name = "screening_results", uniqueConstraints = @UniqueConstraint(name = "uk_screening_tx", columnNames = "transaction_id"))
@Getter @Setter @NoArgsConstructor
public class ScreeningResult {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "transaction_id", nullable = false)
  private Transaction transaction;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ScreeningDecision decision;

  @Column(nullable = false)
  private int score;

  @Column(columnDefinition = "jsonb", nullable = false)
  private String reasons;

  @Column(name = "engine_version", nullable = false, length = 32)
  private String engineVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "watchlist_batch_id")
  private WatchlistBatch watchlistBatch;

  @Column(name = "correlation_id", length = 128)
  private String correlationId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}
