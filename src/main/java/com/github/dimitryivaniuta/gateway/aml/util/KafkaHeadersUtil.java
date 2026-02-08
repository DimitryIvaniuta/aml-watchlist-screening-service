package com.github.dimitryivaniuta.gateway.aml.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;

/**
 * Kafka producer header utilities.
 */
public final class KafkaHeadersUtil {
  public static final String HEADER_EVENT_ID = "x-event-id";
  public static final String HEADER_CORRELATION_ID = "x-correlation-id";

  private KafkaHeadersUtil() {}

  public static ProducerRecord<String, Object> withEventHeaders(
      String topic, String key, Object value, UUID eventId, String correlationId) {
    ProducerRecord<String, Object> r = new ProducerRecord<>(topic, key, value);
    if (eventId != null) {
      r.headers().add(new RecordHeader(HEADER_EVENT_ID, eventId.toString().getBytes(StandardCharsets.UTF_8)));
    }
    if (correlationId != null && !correlationId.isBlank()) {
      r.headers().add(new RecordHeader(HEADER_CORRELATION_ID, correlationId.getBytes(StandardCharsets.UTF_8)));
    }
    return r;
  }
}
