package com.github.dimitryivaniuta.gateway.aml.consumer;

import com.github.dimitryivaniuta.gateway.aml.events.ScreeningResultCreatedEvent;
import com.github.dimitryivaniuta.gateway.aml.util.KafkaHeadersUtil;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Example consumer that demonstrates consumer-side dedup using {@code x-event-id}.
 *
 * <p>In real architectures, this would live in another service.
 */
@Slf4j
@Component
public class ScreeningResultConsumer {

  private final StringRedisTemplate redis;
  private final Duration ttl;

  public ScreeningResultConsumer(StringRedisTemplate redis, @Value("${aml.consumer.dedup.ttlSeconds:86400}") long ttlSeconds) {
    this.redis = redis;
    this.ttl = Duration.ofSeconds(ttlSeconds);
  }

  @KafkaListener(topics = "${aml.consumer.topic:screening-results}", groupId = "aml-screening-results-consumer")
  public void onEvent(ConsumerRecord<String, ScreeningResultCreatedEvent> record) {
    String eventId = header(record, KafkaHeadersUtil.HEADER_EVENT_ID);
    if (eventId == null || eventId.isBlank()) {
      eventId = "nohdr:" + record.topic() + ":" + record.partition() + ":" + record.offset();
    }

    String key = "dedup:screening-results:" + eventId;
    Boolean first = redis.opsForValue().setIfAbsent(key, "1", ttl);
    if (Boolean.FALSE.equals(first)) {
      log.info("Duplicate ignored: eventId={} txId={}", eventId, record.value() != null ? record.value().transactionId() : null);
      return;
    }

    log.info("Consumed event: eventId={} txId={} decision={}",
        eventId,
        record.value() != null ? record.value().transactionId() : null,
        record.value() != null ? record.value().decision() : null);
  }

  private static String header(ConsumerRecord<?, ?> record, String key) {
    var h = record.headers().lastHeader(key);
    if (h == null) return null;
    return new String(h.value(), StandardCharsets.UTF_8);
  }
}
