package com.github.dimitryivaniuta.gateway.aml.watchlist;

import com.github.dimitryivaniuta.gateway.aml.domain.RiskLevel;
import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistBatch;
import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistEntry;
import com.github.dimitryivaniuta.gateway.aml.repo.WatchlistBatchRepository;
import com.github.dimitryivaniuta.gateway.aml.repo.WatchlistEntryRepository;
import com.github.dimitryivaniuta.gateway.aml.util.Normalize;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ingests watchlist updates.
 */
@Slf4j
@Service
public class WatchlistService {

  private final WatchlistBatchRepository batches;
  private final WatchlistEntryRepository entries;
  private final WatchlistCacheService cache;

  public WatchlistService(WatchlistBatchRepository batches, WatchlistEntryRepository entries, WatchlistCacheService cache) {
    this.batches = batches;
    this.entries = entries;
    this.cache = cache;
  }

  /**
   * Imports a batch of entries (daily ingestion).
   */
  @Transactional
  public WatchlistBatch importBatch(String source, List<WatchlistImportItem> items) {
    WatchlistBatch b = new WatchlistBatch();
    b.setSource(source);
    b.setImportedAt(Instant.now());
    b = batches.save(b);

    List<WatchlistEntry> saved = new ArrayList<>();
    for (WatchlistImportItem i : items) {
      WatchlistEntry e = new WatchlistEntry();
      e.setId(UUID.randomUUID());
      e.setBatch(b);
      e.setFullName(i.fullName());
      e.setFirstName(i.firstName());
      e.setLastName(i.lastName());
      e.setFullNameNorm(Normalize.norm(i.fullName()));
      e.setLastNameNorm(Normalize.norm(i.lastName()));
      e.setDob(i.dob());
      e.setNationality(i.nationality());
      e.setCountry(i.country());
      e.setRiskLevel(i.riskLevel() == null ? RiskLevel.MEDIUM : i.riskLevel());
      e.setAliases(i.aliasesJson());
      e.setAddressLine1(i.addressLine1());
      e.setCity(i.city());
      e.setPostalCode(i.postalCode());
      e.setAddressCountry(i.addressCountry());
      saved.add(entries.save(e));
    }

    cache.indexEntries(saved);

    log.info("Imported watchlist batch: id={} source={} entries={}", b.getId(), source, saved.size());
    return b;
  }

  /**
   * Import item DTO.
   */
  public record WatchlistImportItem(
      String fullName,
      String firstName,
      String lastName,
      LocalDate dob,
      String nationality,
      String country,
      RiskLevel riskLevel,
      String aliasesJson,
      String addressLine1,
      String city,
      String postalCode,
      String addressCountry
  ) {}
}
