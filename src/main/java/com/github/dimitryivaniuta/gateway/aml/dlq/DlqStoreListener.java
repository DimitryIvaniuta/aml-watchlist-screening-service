package com.github.dimitryivaniuta.gateway.aml.dlq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.gateway.aml.config.KafkaConfig;
import com.github.dimitryivaniuta.gateway.aml.events.OutboxDlqEvent;
import com.github.dimitryivaniuta.gateway.aml.repo.DlqMessageRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Stores DLQ records in Postgres (idempotent by topic+partition+offset).
 */
@Slf4j
@Component
public class DlqStoreListener {

  private final DlqMessageRepository repo;
  private final ObjectMapper mapper;

  public DlqStoreListener(DlqMessageRepository repo, ObjectMapper mapper) {
    this.repo = repo;
    this.mapper = mapper;
  }

  @KafkaListener(topics = KafkaConfig.TOPIC_SCREENING_RESULTS_DLQ, groupId = "aml-dlq-store")
  public void onDlq(ConsumerRecord<String, OutboxDlqEvent> record) {
    DlqMessage m = new DlqMessage();
    m.setTopic(record.topic());
    m.setPartition(record.partition());
    m.setOffset(record.offset());
    m.setMessageKey(record.key());
    m.setEventType(record.value() != null ? record.value().eventType() : "unknown");
    m.setAggregateType(record.value() != null ? record.value().aggregateType() : null);
    m.setAggregateId(record.value() != null ? record.value().aggregateId() : record.key());
    m.setOutboxId(record.value() != null ? record.value().outboxId() : null);
    m.setError(record.value() != null ? record.value().error() : null);
    m.setNextAttemptAt(Instant.now());
    m.setCorrelationId(null);

    try {
      // store payload from the original outbox (json string)
      String payloadJson = record.value() != null ? record.value().payload() : "{}";
      m.setPayload(payloadJson);
      repo.save(m);
      log.warn("DLQ stored: topic={} partition={} offset={} outboxId={}", record.topic(), record.partition(), record.offset(), m.getOutboxId());
    } catch (DataIntegrityViolationException dup) {
      log.info("DLQ duplicate ignored (topic/partition/offset): {}:{}:{}", record.topic(), record.partition(), record.offset());
    } catch (Exception e) {
      log.error("DLQ store failed: {}", e.getMessage(), e);
    }
  }
}
