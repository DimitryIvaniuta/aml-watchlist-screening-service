package com.github.dimitryivaniuta.gateway.aml.repo;

import com.github.dimitryivaniuta.gateway.aml.domain.ScreeningResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for screening results. */
public interface ScreeningResultRepository extends JpaRepository<ScreeningResult, UUID> {

  Optional<ScreeningResult> findByTransactionId(UUID transactionId);

  List<ScreeningResult> findAllByCreatedAtBetween(Instant from, Instant to);
}
