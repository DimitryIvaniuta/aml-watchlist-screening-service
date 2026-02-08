package com.github.dimitryivaniuta.gateway.aml.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A batch of watchlist entries imported from a source.
 */
@Entity
@Table(name = "watchlist_batches")
@Getter @Setter @NoArgsConstructor
public class WatchlistBatch {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 128)
  private String source;

  @Column(name = "imported_at", nullable = false)
  private Instant importedAt = Instant.now();
}
