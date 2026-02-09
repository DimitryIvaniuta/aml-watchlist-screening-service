package com.github.dimitryivaniuta.gateway.aml.dlq;

import com.github.dimitryivaniuta.gateway.aml.repo.DlqMessageRepository;
import com.github.dimitryivaniuta.gateway.aml.repo.DlqReplayAuditRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ops workflow service:
 * - request replay (ROLE_OPS_REQUESTER)
 * - approve replay (ROLE_OPS_APPROVER) with 4-eyes enforcement
 */
@Service
public class DlqOpsService {

  private final DlqMessageRepository repo;
  private final DlqReplayAuditRepository audit;

  public DlqOpsService(DlqMessageRepository repo, DlqReplayAuditRepository audit) {
    this.repo = repo;
    this.audit = audit;
  }

  @PreAuthorize("hasAnyRole('OPS_REQUESTER','OPS_APPROVER')")
  @Transactional(readOnly = true)
  public DlqMessage getById(long id) {
    return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("DLQ message not found: " + id));
  }

  @PreAuthorize("hasAnyRole('OPS_REQUESTER','OPS_APPROVER')")
  @Transactional(readOnly = true)
  public Page<DlqMessage> search(DlqMessageStatus status, DlqApprovalStatus approvalStatus, String eventType,
                                String aggregateId, UUID outboxId, String searchQ, Pageable pageable) {
    UUID qOutboxId = tryParseUuid(searchQ);
    String q = blankToNull(searchQ);
    return repo.findByFiltersAndSearchQ(status, approvalStatus, blankToNull(eventType), blankToNull(aggregateId), outboxId, q, qOutboxId, pageable);
  }

  @PreAuthorize("hasRole('OPS_REQUESTER')")
  @Transactional
  public DlqMessage requestReplay(long id, String operator) {
    DlqMessage m = getById(id);
    requirePending(m);
    if (operator == null || operator.isBlank()) throw new IllegalArgumentException("Operator is required.");

    m.setApprovalStatus(DlqApprovalStatus.REQUESTED);
    m.setRequestedBy(operator);
    m.setRequestedAt(Instant.now());
    appendAudit(m, "REQUEST_REPLAY", "SUCCESS", null);
    return m;
  }

  @PreAuthorize("hasRole('OPS_APPROVER')")
  @Transactional
  public DlqMessage approveReplay(long id, String operator) {
    DlqMessage m = getById(id);
    requirePending(m);
    if (operator == null || operator.isBlank()) throw new IllegalArgumentException("Operator is required.");

    if (m.getApprovalStatus() != DlqApprovalStatus.REQUESTED) throw new IllegalStateException("Replay must be requested before approval.");
    if (m.getRequestedBy() != null && m.getRequestedBy().equalsIgnoreCase(operator)) throw new IllegalStateException("4-eyes: approver must differ from requester.");

    m.setApprovalStatus(DlqApprovalStatus.APPROVED);
    m.setApprovedBy(operator);
    m.setApprovedAt(Instant.now());
    appendAudit(m, "APPROVE_REPLAY", "SUCCESS", null);
    return m;
  }

  private void appendAudit(DlqMessage m, String action, String status, String err) {
    DlqReplayAudit a = new DlqReplayAudit();
    a.setMessage(m);
    a.setAction(action);
    a.setStatus(status);
    a.setError(err);
    a.setCorrelationId(m.getCorrelationId());
    audit.save(a);
  }

  private static void requirePending(DlqMessage m) {
    if (m.getStatus() != DlqMessageStatus.PENDING) throw new IllegalStateException("Only PENDING messages allowed.");
  }

  private static String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
  private static UUID tryParseUuid(String s) { try { return s == null ? null : UUID.fromString(s.trim()); } catch (Exception e) { return null; } }
}
