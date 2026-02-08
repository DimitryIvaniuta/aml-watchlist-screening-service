package com.github.dimitryivaniuta.gateway.aml.config;

import com.github.dimitryivaniuta.gateway.aml.dlq.DlqReplayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Registers @ConfigurationProperties.
 */
@Configuration
@EnableConfigurationProperties({
    com.github.dimitryivaniuta.gateway.aml.security.SecurityUsersProperties.class,OutboxProperties.class, ScreeningProperties.class, DlqReplayProperties.class, KafkaTopicProperties.class})
public class PropertiesConfig {}
