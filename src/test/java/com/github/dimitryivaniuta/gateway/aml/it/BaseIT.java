package com.github.dimitryivaniuta.gateway.aml.it;

import com.github.dimitryivaniuta.gateway.aml.testsupport.Containers;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class BaseIT {

  @BeforeAll
  static void init() {
    // triggers container startup
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", Containers.POSTGRES::getJdbcUrl);
    r.add("spring.datasource.username", Containers.POSTGRES::getUsername);
    r.add("spring.datasource.password", Containers.POSTGRES::getPassword);

    r.add("spring.data.redis.host", Containers.REDIS::getHost);
    r.add("spring.data.redis.port", () -> Containers.REDIS.getMappedPort(6379));

    // Embedded Kafka is used via @EmbeddedKafka in tests that need it.
  }
}
