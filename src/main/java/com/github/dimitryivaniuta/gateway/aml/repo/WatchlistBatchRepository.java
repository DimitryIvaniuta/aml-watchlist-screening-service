package com.github.dimitryivaniuta.gateway.aml.repo;

import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistBatch;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for watchlist batches. */
public interface WatchlistBatchRepository extends JpaRepository<WatchlistBatch, Long> {}
