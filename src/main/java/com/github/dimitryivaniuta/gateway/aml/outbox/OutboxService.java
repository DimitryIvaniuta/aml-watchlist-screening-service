package com.github.dimitryivaniuta.gateway.aml.outbox;

import com.github.dimitryivaniuta.gateway.aml.domain.OutboxEvent;
import com.github.dimitryivaniuta.gateway.aml.domain.OutboxStatus;
import com.github.dimitryivaniuta.gateway.aml.repo.OutboxEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates outbox events (persisted in DB).
 */
@Service
public class OutboxService {

  private final OutboxEventRepository repo;

  public OutboxService(OutboxEventRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public UUID saveEvent(String aggregateType, String aggregateId, String eventType, String payloadJson, String correlationId) {
    OutboxEvent e = new OutboxEvent();
    e.setId(UUID.randomUUID());
    e.setAggregateType(aggregateType);
    e.setAggregateId(aggregateId);
    e.setEventType(eventType);
    e.setPayload(payloadJson);
    e.setCreatedAt(Instant.now());
    e.setAttempts(0);
    e.setNextAttemptAt(Instant.now());
    e.setStatus(OutboxStatus.PENDING);
    e.setCorrelationId(correlationId);
    repo.save(e);
    return e.getId();
  }
}
