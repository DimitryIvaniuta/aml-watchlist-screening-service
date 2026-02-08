package com.github.dimitryivaniuta.gateway.aml.api;

import com.github.dimitryivaniuta.gateway.aml.repo.ScreeningResultRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auditor export endpoint (CSV).
 */
@RestController
@RequestMapping("/api/audit/screenings")
public class AuditExportController {

  private final ScreeningResultRepository repo;

  public AuditExportController(ScreeningResultRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/export")
  public ResponseEntity<byte[]> export(@RequestParam("from") String from, @RequestParam("to") String to) {
    Instant f = parse(from);
    Instant t = parse(to);

    var rows = repo.findAllByCreatedAtBetween(f, t);
    StringBuilder csv = new StringBuilder();
    csv.append("screeningId,transactionId,decision,score,engineVersion,watchlistBatchId,correlationId,createdAt\n");
    for (var r : rows) {
      csv.append(r.getId()).append(',')
          .append(r.getTransaction().getId()).append(',')
          .append(r.getDecision()).append(',')
          .append(r.getScore()).append(',')
          .append(r.getEngineVersion()).append(',')
          .append(r.getWatchlistBatch() != null ? r.getWatchlistBatch().getId() : "").append(',')
          .append(r.getCorrelationId() != null ? r.getCorrelationId() : "").append(',')
          .append(r.getCreatedAt()).append('\n');
    }

    byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
    return ResponseEntity.ok()
        .header("Content-Type", "text/csv; charset=utf-8")
        .header("Content-Disposition", "attachment; filename=screenings.csv")
        .body(bytes);
  }

  private static Instant parse(String s) {
    try {
      return Instant.parse(s);
    } catch (DateTimeParseException ex) {
      throw new IllegalArgumentException("Invalid ISO-8601 instant: " + s);
    }
  }
}
