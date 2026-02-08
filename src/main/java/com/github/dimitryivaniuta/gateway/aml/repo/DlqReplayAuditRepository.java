package com.github.dimitryivaniuta.gateway.aml.repo;

import com.github.dimitryivaniuta.gateway.aml.dlq.DlqReplayAudit;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for DLQ replay audit. */
public interface DlqReplayAuditRepository extends JpaRepository<DlqReplayAudit, Long> {}
