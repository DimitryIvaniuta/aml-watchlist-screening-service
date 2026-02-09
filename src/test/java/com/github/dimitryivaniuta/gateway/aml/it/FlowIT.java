package com.github.dimitryivaniuta.gateway.aml.it;

import com.github.dimitryivaniuta.gateway.aml.AmlWatchlistScreeningApplication;
import com.github.dimitryivaniuta.gateway.aml.api.dto.TransactionCreateRequest;
import com.github.dimitryivaniuta.gateway.aml.api.dto.WatchlistImportRequest;
import com.github.dimitryivaniuta.gateway.aml.domain.RiskLevel;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AmlWatchlistScreeningApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"screening-results", "screening-results-dlq"})
class FlowIT extends BaseIT {

  @Autowired
  TestRestTemplate rest;

  @Test
  void shouldIngestAndScreenAsync() throws Exception {
    var wl = new WatchlistImportRequest("unit-test", List.of(
        new WatchlistImportRequest.Item("Ivan Petrov", "Ivan", "Petrov", LocalDate.of(1990,1,1),
            "BY", "BY", RiskLevel.HIGH, List.of("I. Petrov"), "Main 1", "Gdansk", "80-000", "PL")
    ));
    rest.postForEntity("/api/watchlists/import", wl, Map.class);

    var tx = new TransactionCreateRequest("Ivan Petrov", LocalDate.of(1990,1,1), "BY", "BY", "AB123456",
        new BigDecimal("100.00"), "EUR", "Main 1", "Gdansk", "80-000", "PL");
    ResponseEntity<Map> resp = rest.postForEntity("/api/transactions", tx, Map.class);
    assertEquals(202, resp.getStatusCode().value());
    String txId = (String) resp.getBody().get("transactionId");
    assertNotNull(txId);

    // poll for screening
    for (int i = 0; i < 30; i++) {
      ResponseEntity<Map> r = rest.getForEntity("/api/screenings/" + txId, Map.class);
      if (r.getStatusCode().value() == 200) {
        assertNotNull(r.getBody().get("decision"));
        return;
      }
      Thread.sleep(200);
    }
    fail("Screening result not produced in time");
  }
}
