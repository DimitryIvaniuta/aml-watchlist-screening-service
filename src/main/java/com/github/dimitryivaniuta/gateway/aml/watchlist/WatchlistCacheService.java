package com.github.dimitryivaniuta.gateway.aml.watchlist;

import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistEntry;
import com.github.dimitryivaniuta.gateway.aml.repo.WatchlistEntryRepository;
import com.github.dimitryivaniuta.gateway.aml.util.Normalize;
import java.time.Duration;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Redis-backed index to speed candidate selection.
 *
 * <p>Stores sets: wl:ln:{prefix} -> entryId
 */
@Slf4j
@Service
public class WatchlistCacheService {

  private static final Duration TTL = Duration.ofHours(36);
  private final StringRedisTemplate redis;
  private final WatchlistEntryRepository entries;

  public WatchlistCacheService(StringRedisTemplate redis, WatchlistEntryRepository entries) {
    this.redis = redis;
    this.entries = entries;
  }

  /**
   * Rebuilds cache index for a list of entries.
   */
  @Transactional(readOnly = true)
  public void indexEntries(List<WatchlistEntry> batchEntries) {
    for (WatchlistEntry e : batchEntries) {
      String ln = e.getLastNameNorm();
      if (ln == null || ln.isBlank()) continue;
      for (String p : prefixes(ln, 3)) {
        String key = "wl:ln:" + p;
        redis.opsForSet().add(key, e.getId().toString());
        redis.expire(key, TTL);
      }
    }
    log.info("Watchlist cache indexed: entries={}", batchEntries.size());
  }

  /**
   * Returns candidate ids for a transaction name (by last-name prefix), with DB fallback.
   */
  public Set<UUID> candidatesByName(String fullName) {
    String norm = Normalize.norm(fullName);
    String last = lastToken(norm);
    if (last == null || last.isBlank()) return Set.of();

    Set<UUID> ids = new LinkedHashSet<>();
    for (String p : prefixes(last, 3)) {
      Set<String> s = redis.opsForSet().members("wl:ln:" + p);
      if (s != null) {
        for (String id : s) {
          try { ids.add(UUID.fromString(id)); } catch (Exception ignored) {}
        }
      }
    }

    // If cache empty, fallback to DB prefix query.
    if (ids.isEmpty()) {
      List<WatchlistEntry> e = entries.findByLastNameNormPrefix(last.substring(0, Math.min(3, last.length())));
      for (WatchlistEntry x : e) ids.add(x.getId());
    }
    return ids;
  }

  private static List<String> prefixes(String s, int max) {
    List<String> out = new ArrayList<>();
    for (int i = 1; i <= Math.min(max, s.length()); i++) out.add(s.substring(0, i));
    return out;
  }

  private static String lastToken(String s) {
    if (s == null) return null;
    String[] p = s.trim().split("\\s+");
    return p.length == 0 ? null : p[p.length - 1];
  }
}
