package com.github.dimitryivaniuta.gateway.aml.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="aml.kafka")
public record KafkaTopicProperties(Topic topic) {
  public record Topic(String screeningResults, String screeningResultsDlq) {}
}
