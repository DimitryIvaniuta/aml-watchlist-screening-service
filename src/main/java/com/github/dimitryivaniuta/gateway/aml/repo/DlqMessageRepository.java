package com.github.dimitryivaniuta.gateway.aml.repo;

import com.github.dimitryivaniuta.gateway.aml.dlq.DlqApprovalStatus;
import com.github.dimitryivaniuta.gateway.aml.dlq.DlqMessage;
import com.github.dimitryivaniuta.gateway.aml.dlq.DlqMessageStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for DLQ stored messages. */
public interface DlqMessageRepository extends JpaRepository<DlqMessage, Long> {

  @Query("""
      select m from DlqMessage m
      where m.status = :status
        and m.nextAttemptAt <= :now
      order by m.nextAttemptAt asc, m.id asc
      """)
  List<DlqMessage> findDue(@Param("status") DlqMessageStatus status, @Param("now") Instant now);

  @Query("""
      select m from DlqMessage m
      where (:status is null or m.status = :status)
        and (:approval is null or m.approvalStatus = :approval)
        and (:eventType is null or m.eventType = :eventType)
        and (:aggregateId is null or m.aggregateId = :aggregateId)
        and (:outboxId is null or m.outboxId = :outboxId)
        and (
          :q is null
          or lower(m.aggregateId) like concat('%', lower(:q), '%')
          or lower(m.messageKey) like concat('%', lower(:q), '%')
          or lower(m.eventType) like concat('%', lower(:q), '%')
          or (:qOutboxId is not null and m.outboxId = :qOutboxId)
        )
      """)
  Page<DlqMessage> findByFiltersAndSearchQ(
      @Param("status") DlqMessageStatus status,
      @Param("approval") DlqApprovalStatus approval,
      @Param("eventType") String eventType,
      @Param("aggregateId") String aggregateId,
      @Param("outboxId") UUID outboxId,
      @Param("q") String q,
      @Param("qOutboxId") UUID qOutboxId,
      Pageable pageable
  );
}
