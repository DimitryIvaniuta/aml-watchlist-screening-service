package com.github.dimitryivaniuta.gateway.aml.repo;

import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistEntry;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for watchlist entries. */
public interface WatchlistEntryRepository extends JpaRepository<WatchlistEntry, UUID> {

  @Query("select e from WatchlistEntry e where e.lastNameNorm like concat(:prefix, '%')")
  List<WatchlistEntry> findByLastNameNormPrefix(@Param("prefix") String prefix);
}
