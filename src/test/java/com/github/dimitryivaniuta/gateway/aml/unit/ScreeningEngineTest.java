package com.github.dimitryivaniuta.gateway.aml.unit;

import com.github.dimitryivaniuta.gateway.aml.domain.Transaction;
import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistBatch;
import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistEntry;
import com.github.dimitryivaniuta.gateway.aml.domain.RiskLevel;
import com.github.dimitryivaniuta.gateway.aml.screening.ScreeningEngine;
import com.github.dimitryivaniuta.gateway.aml.util.Normalize;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreeningEngineTest {

  @Test
  void shouldScoreHigherForSimilarName() {
    ScreeningEngine eng = new ScreeningEngine();

    Transaction tx = new Transaction();
    tx.setId(UUID.randomUUID());
    tx.setCustomerFullName("Ivan Petrov");
    tx.setCustomerFullNameNorm(Normalize.norm(tx.getCustomerFullName()));
    tx.setCustomerDob(LocalDate.of(1990, 1, 1));
    tx.setAmount(BigDecimal.TEN);
    tx.setCurrency("EUR");

    WatchlistEntry e = new WatchlistEntry();
    e.setId(UUID.randomUUID());
    e.setBatch(new WatchlistBatch());
    e.setFullName("Ivan Petrov");
    e.setFullNameNorm(Normalize.norm("Ivan Petrov"));
    e.setLastNameNorm(Normalize.norm("Petrov"));
    e.setDob(LocalDate.of(1990, 1, 1));
    e.setRiskLevel(RiskLevel.HIGH);

    var r = eng.score(tx, e);
    assertTrue(r.score() > 0.8, "Expected high match score");
    assertFalse(r.reasons().isEmpty());
  }
}
