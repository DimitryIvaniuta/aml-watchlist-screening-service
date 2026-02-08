package com.github.dimitryivaniuta.gateway.aml.screening;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.gateway.aml.config.ScreeningProperties;
import com.github.dimitryivaniuta.gateway.aml.domain.ScreeningDecision;
import com.github.dimitryivaniuta.gateway.aml.domain.ScreeningResult;
import com.github.dimitryivaniuta.gateway.aml.domain.Transaction;
import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistEntry;
import com.github.dimitryivaniuta.gateway.aml.events.ScreeningResultCreatedEvent;
import com.github.dimitryivaniuta.gateway.aml.outbox.OutboxService;
import com.github.dimitryivaniuta.gateway.aml.repo.ScreeningResultRepository;
import com.github.dimitryivaniuta.gateway.aml.repo.TransactionRepository;
import com.github.dimitryivaniuta.gateway.aml.repo.WatchlistBatchRepository;
import com.github.dimitryivaniuta.gateway.aml.repo.WatchlistEntryRepository;
import com.github.dimitryivaniuta.gateway.aml.watchlist.WatchlistCacheService;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Async screening service with explainable decisions.
 */
@Slf4j
@Service
public class ScreeningService {

  private static final String ENGINE = "engine-v1";

  private final TransactionRepository txRepo;
  private final ScreeningResultRepository resultRepo;
  private final WatchlistEntryRepository entryRepo;
  private final WatchlistBatchRepository batchRepo;
  private final WatchlistCacheService cache;
  private final ScreeningEngine engine;
  private final ScreeningProperties props;
  private final OutboxService outbox;
  private final ObjectMapper mapper;

  public ScreeningService(
      TransactionRepository txRepo,
      ScreeningResultRepository resultRepo,
      WatchlistEntryRepository entryRepo,
      WatchlistBatchRepository batchRepo,
      WatchlistCacheService cache,
      ScreeningEngine engine,
      ScreeningProperties props,
      OutboxService outbox,
      ObjectMapper mapper) {
    this.txRepo = txRepo;
    this.resultRepo = resultRepo;
    this.entryRepo = entryRepo;
    this.batchRepo = batchRepo;
    this.cache = cache;
    this.engine = engine;
    this.props = props;
    this.outbox = outbox;
    this.mapper = mapper;
  }

  /**
   * Submit transaction for async screening.
   */
  @Transactional
  public UUID submit(Transaction tx) {
    txRepo.save(tx);
    screenAsync(tx.getId());
    return tx.getId();
  }

  /**
   * Get screening result if available.
   */
  @Transactional(readOnly = true)
  public Optional<ScreeningResult> getByTransaction(UUID txId) {
    return resultRepo.findByTransactionId(txId);
  }

  @Async("screeningExecutor")
  @Transactional
  public void screenAsync(UUID txId) {
    Transaction tx = txRepo.findById(txId).orElseThrow();

    // Candidate selection via redis index
    Set<UUID> candidateIds = cache.candidatesByName(tx.getCustomerFullName());
    List<WatchlistEntry> candidates = candidateIds.isEmpty() ? List.of() : entryRepo.findAllById(candidateIds);

    MatchResult best = null;
    for (WatchlistEntry e : candidates) {
      MatchResult r = engine.score(tx, e);
      if (best == null || r.score() > best.score()) best = r;
    }

    double score01 = best == null ? 0.0 : best.score();
    ScreeningDecision decision = decide(score01, props.thresholds().block(), props.thresholds().review());
    int score = (int) Math.round(score01 * 100);

    ScreeningResult sr = new ScreeningResult();
    sr.setId(UUID.randomUUID());
    sr.setTransaction(tx);
    sr.setDecision(decision);
    sr.setScore(score);
    sr.setEngineVersion(ENGINE);
    sr.setCorrelationId(MDC.get("correlationId"));

    // attach latest watchlist batch id for audit (if exists)
    batchRepo.findAll().stream().max(Comparator.comparingLong(b -> b.getId())).ifPresent(sr::setWatchlistBatch);

    // reasons JSON
    Map<String, Object> reasons = new LinkedHashMap<>();
    reasons.put("decision", decision.name());
    reasons.put("score", score);
    reasons.put("matchedEntryId", best != null ? best.entry().getId() : null);
    reasons.put("reasons", best != null ? best.reasons() : List.of(new MatchReason("NO_CANDIDATES", "No candidates found", 0)));
    sr.setReasons(writeJson(reasons));

    resultRepo.save(sr);

    // publish event via outbox
    ScreeningResultCreatedEvent ev = new ScreeningResultCreatedEvent(
        sr.getId(),
        tx.getId(),
        sr.getDecision(),
        sr.getScore(),
        sr.getWatchlistBatch() != null ? sr.getWatchlistBatch().getId() : null,
        sr.getEngineVersion(),
        sr.getCorrelationId(),
        Instant.now()
    );
    outbox.saveEvent("Transaction", tx.getId().toString(), "ScreeningResultCreated", writeJson(ev), sr.getCorrelationId());

    log.info("Screened tx={} decision={} score={}", txId, decision, score);
  }

  private static ScreeningDecision decide(double score01, double block, double review) {
    if (score01 >= block) return ScreeningDecision.BLOCK;
    if (score01 >= review) return ScreeningDecision.REVIEW;
    return ScreeningDecision.CLEAR;
  }

  private String writeJson(Object o) {
    try {
      return mapper.writeValueAsString(o);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
