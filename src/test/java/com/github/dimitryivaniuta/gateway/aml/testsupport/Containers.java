package com.github.dimitryivaniuta.gateway.aml.testsupport;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/** Shared Testcontainers. */
public final class Containers {
  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("aml")
      .withUsername("aml")
      .withPassword("aml");

  public static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

  static {
    POSTGRES.start();
    REDIS.start();
  }

  private Containers() {}
}
