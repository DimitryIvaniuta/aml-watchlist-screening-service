package com.github.dimitryivaniuta.gateway.aml.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI metadata. */
@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info()
        .title("AML Watchlist Screening Service")
        .version("1.1.0")
        .description("Async explainable screening with outbox, DLQ ops, RBAC, and RFC7807 errors."));
  }
}
