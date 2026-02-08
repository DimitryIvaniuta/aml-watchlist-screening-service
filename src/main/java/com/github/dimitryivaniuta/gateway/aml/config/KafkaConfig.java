package com.github.dimitryivaniuta.gateway.aml.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Kafka topics and producer.
 */
@Configuration
public class KafkaConfig {

  public static final String TOPIC_SCREENING_RESULTS = "screening-results";
  public static final String TOPIC_SCREENING_RESULTS_DLQ = "screening-results-dlq";
  public static final String TOPIC_WATCHLIST_UPDATES = "watchlist-updates";

  @Bean
  public NewTopic screeningResultsTopic() {
    return TopicBuilder.name(TOPIC_SCREENING_RESULTS).partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic screeningResultsDlqTopic() {
    return TopicBuilder.name(TOPIC_SCREENING_RESULTS_DLQ).partitions(3).replicas(1).build();
  }

  @Bean
  public NewTopic watchlistUpdatesTopic() {
    return TopicBuilder.name(TOPIC_WATCHLIST_UPDATES).partitions(1).replicas(1).build();
  }

  @Bean
  public ProducerFactory<String, Object> producerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrap) {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> pf) {
    return new KafkaTemplate<>(pf);
  }
}
