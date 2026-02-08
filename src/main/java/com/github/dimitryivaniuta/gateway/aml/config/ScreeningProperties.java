package com.github.dimitryivaniuta.gateway.aml.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Screening thresholds.
 */
@ConfigurationProperties(prefix = "aml.screening")
public record ScreeningProperties(Thresholds thresholds) {
  public record Thresholds(double block, double review) {}
}
