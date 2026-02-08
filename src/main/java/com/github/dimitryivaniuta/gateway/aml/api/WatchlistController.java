package com.github.dimitryivaniuta.gateway.aml.api;

import com.github.dimitryivaniuta.gateway.aml.api.dto.WatchlistImportRequest;
import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistBatch;
import com.github.dimitryivaniuta.gateway.aml.watchlist.WatchlistService;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Watchlist ingestion API.
 */
@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {

  private final WatchlistService watchlists;

  public WatchlistController(WatchlistService watchlists) {
    this.watchlists = watchlists;
  }

  @PostMapping("/import")
  public ResponseEntity<?> importBatch(@Valid @RequestBody WatchlistImportRequest req) {
    WatchlistBatch b = watchlists.importBatch(
        req.source(),
        req.items() == null ? java.util.List.of() :
            req.items().stream().map(i -> new WatchlistService.WatchlistImportItem(
                i.fullName(), i.firstName(), i.lastName(), i.dob(), i.nationality(), i.country(), i.riskLevel(),
                i.aliases() != null ? toJson(i.aliases()) : "[]",
                i.addressLine1(), i.city(), i.postalCode(), i.addressCountry()
            )).collect(Collectors.toList())
    );
    return ResponseEntity.ok(java.util.Map.of("batchId", b.getId(), "importedAt", b.getImportedAt()));
  }

  private static String toJson(Object o) {
    try {
      return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(o);
    } catch (Exception e) {
      return "[]";
    }
  }
}
