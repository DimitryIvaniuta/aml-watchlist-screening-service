package com.github.dimitryivaniuta.gateway.aml.api;

import com.github.dimitryivaniuta.gateway.aml.api.dto.ScreeningResultDto;
import com.github.dimitryivaniuta.gateway.aml.domain.ScreeningResult;
import com.github.dimitryivaniuta.gateway.aml.screening.ScreeningService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Read API for screening results.
 */
@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

  private final ScreeningService screening;

  public ScreeningController(ScreeningService screening) {
    this.screening = screening;
  }

  @GetMapping("/{txId}")
  public ResponseEntity<?> get(@PathVariable("txId") UUID txId) {
    return screening.getByTransaction(txId)
        .<ResponseEntity<?>>map(sr -> ResponseEntity.ok(toDto(sr)))
        .orElseGet(() -> ResponseEntity.status(404).body(java.util.Map.of("status", "PENDING_OR_NOT_FOUND")));
  }

  private static ScreeningResultDto toDto(ScreeningResult sr) {
    return new ScreeningResultDto(
        sr.getId(),
        sr.getTransaction().getId(),
        sr.getDecision(),
        sr.getScore(),
        sr.getReasons(),
        sr.getEngineVersion(),
        sr.getWatchlistBatch() != null ? sr.getWatchlistBatch().getId() : null,
        sr.getCorrelationId(),
        sr.getCreatedAt()
    );
  }
}
