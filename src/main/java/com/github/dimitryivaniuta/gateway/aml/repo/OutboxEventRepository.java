package com.github.dimitryivaniuta.gateway.aml.repo;

import com.github.dimitryivaniuta.gateway.aml.domain.OutboxEvent;
import com.github.dimitryivaniuta.gateway.aml.domain.OutboxStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for outbox events. */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

  @Query("""
      select e from OutboxEvent e
      where e.status = :status
        and (e.nextAttemptAt is null or e.nextAttemptAt <= :now)
      order by e.createdAt asc
      """)
  List<OutboxEvent> findDue(@Param("status") OutboxStatus status, @Param("now") Instant now);
}
