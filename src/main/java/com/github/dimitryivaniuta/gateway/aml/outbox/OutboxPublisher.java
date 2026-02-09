package com.github.dimitryivaniuta.gateway.aml.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.gateway.aml.config.KafkaTopicProperties;
import com.github.dimitryivaniuta.gateway.aml.config.OutboxProperties;
import com.github.dimitryivaniuta.gateway.aml.domain.OutboxEvent;
import com.github.dimitryivaniuta.gateway.aml.domain.OutboxStatus;
import com.github.dimitryivaniuta.gateway.aml.events.OutboxDlqEvent;
import com.github.dimitryivaniuta.gateway.aml.events.ScreeningResultCreatedEvent;
import com.github.dimitryivaniuta.gateway.aml.repo.OutboxEventRepository;
import com.github.dimitryivaniuta.gateway.aml.util.KafkaHeadersUtil;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox publisher:
 * - exponential backoff + jitter
 * - DLQ topic for publish failures
 * - partitioning strategy: key = aggregateId (transactionId) to preserve ordering per transaction
 * - explicit headers: x-event-id = outboxId
 *
 * Note: For multi-instance deployments, consider a DB-claim query (UPDATE ... RETURNING) to guarantee single sender.
 */
@Slf4j
@Component
public class OutboxPublisher {

  private final OutboxEventRepository repo;
  private final KafkaTemplate<String, Object> kafka;
  private final ObjectMapper mapper;
  private final OutboxProperties props;
  private final KafkaTopicProperties topics;

  public OutboxPublisher(OutboxEventRepository repo, KafkaTemplate<String, Object> kafka, ObjectMapper mapper,
                         OutboxProperties props, KafkaTopicProperties topics) {
    this.repo = repo;
    this.kafka = kafka;
    this.mapper = mapper;
    this.props = props;
    this.topics = topics;
  }

  @Scheduled(fixedDelayString = "PT2S")
  @Transactional
  public void publishDue() {
    List<OutboxEvent> due = repo.findDue(OutboxStatus.PENDING, Instant.now());

    for (OutboxEvent e : due) {
      // In-flight guard: move next attempt into the future to reduce duplicate sends in multi-instance setups.
      e.setNextAttemptAt(Instant.now().plusSeconds(60));
      repo.save(e);

      try {
        Object event = materialize(e);

        kafka.send(KafkaHeadersUtil.withEventHeaders(
            topics.topic().screeningResults(),
            e.getAggregateId(),
            event,
            e.getId(),
            e.getCorrelationId()
        ));

        e.setStatus(OutboxStatus.PUBLISHED);
        e.setPublishedAt(Instant.now());
        e.setLastError(null);
        log.info("Outbox published: id={} type={} key={}", e.getId(), e.getEventType(), e.getAggregateId());
      } catch (Exception ex) {
        e.setAttempts(e.getAttempts() + 1);
        e.setLastError(ex.getMessage());

        if (e.getAttempts() >= props.maxAttempts()) {
          OutboxDlqEvent dlq = new OutboxDlqEvent(e.getId(), e.getEventType(), e.getAggregateType(), e.getAggregateId(), e.getPayload(), ex.getMessage());
          kafka.send(KafkaHeadersUtil.withEventHeaders(props.dlqTopic(), e.getAggregateId(), dlq, e.getId(), e.getCorrelationId()));
          e.setStatus(OutboxStatus.DEAD);
          e.setNextAttemptAt(Instant.now().plusSeconds(365L * 24 * 3600));
          log.error("Outbox DEAD -> DLQ: id={} attempts={} err={}", e.getId(), e.getAttempts(), ex.getMessage());
        } else {
          e.setNextAttemptAt(nextAttempt(e.getAttempts(), props.initialBackoffMillis(), props.maxBackoffMillis(), props.jitterRatio()));
          log.warn("Outbox publish failed: id={} attempt={} err={}", e.getId(), e.getAttempts(), ex.getMessage());
        }
      }
    }
  }

  private Object materialize(OutboxEvent e) throws Exception {
    if ("ScreeningResultCreated".equals(e.getEventType())) {
      return mapper.readValue(e.getPayload(), ScreeningResultCreatedEvent.class);
    }
    return mapper.readTree(e.getPayload());
  }

  private static Instant nextAttempt(int attempts, long initialMs, long maxMs, double jitter) {
    long base = Math.min(maxMs, (long) (initialMs * Math.pow(2, Math.max(0, attempts - 1))));
    long spread = (long) (base * jitter);
    long delta = ThreadLocalRandom.current().nextLong(-spread, spread + 1);
    return Instant.now().plusMillis(Math.max(50, base + delta));
  }
}
