package com.github.dimitryivaniuta.gateway.aml.dlq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.gateway.aml.config.KafkaTopicProperties;
import com.github.dimitryivaniuta.gateway.aml.events.ScreeningResultCreatedEvent;
import com.github.dimitryivaniuta.gateway.aml.repo.DlqMessageRepository;
import com.github.dimitryivaniuta.gateway.aml.repo.DlqReplayAuditRepository;
import com.github.dimitryivaniuta.gateway.aml.util.KafkaHeadersUtil;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** DLQ replay job and replay-by-id endpoint (idempotent publish + audit trail). */
@Service
public class DlqReplayService {

  private final DlqMessageRepository messages;
  private final DlqReplayAuditRepository audit;
  private final KafkaTemplate<String, Object> kafka;
  private final ObjectMapper mapper;
  private final DlqReplayProperties props;
  private final KafkaTopicProperties topics;

  public DlqReplayService(DlqMessageRepository messages, DlqReplayAuditRepository audit, KafkaTemplate<String, Object> kafka,
                          ObjectMapper mapper, DlqReplayProperties props, KafkaTopicProperties topics) {
    this.messages = messages;
    this.audit = audit;
    this.kafka = kafka;
    this.mapper = mapper;
    this.props = props;
    this.topics = topics;
  }

  @Scheduled(fixedDelayString = "${aml.dlq.replay.fixedDelayMillis:5000}")
  @Transactional
  public void replayJob() {
    if (!props.enabled()) return;
    replayDue(props.batchSize());
  }

  @Transactional
  public ReplayResult replayDue(int limit) {
    List<DlqMessage> due = messages.findDue(DlqMessageStatus.PENDING, Instant.now());
    int attempted = 0, succeeded = 0, failed = 0, dead = 0;

    for (DlqMessage m : due) {
      if (attempted >= limit) break;
      if (m.getApprovalStatus() != DlqApprovalStatus.APPROVED) continue;

      attempted++;
      try {
        publish(m, "REPLAY");
        succeeded++;
      } catch (Exception ex) {
        failed++;
        handleFailure(m, "REPLAY", ex);
        if (m.getStatus() == DlqMessageStatus.DEAD) dead++;
      }
    }
    return new ReplayResult(attempted, succeeded, failed, dead);
  }

  /** Replay by ID requires approver role and approval flag. */
  @PreAuthorize("hasRole('OPS_APPROVER')")
  @Transactional
  public ReplayResult replayById(long id) {
    DlqMessage m = messages.findById(id).orElseThrow(() -> new IllegalArgumentException("DLQ message not found: " + id));
    if (m.getStatus() != DlqMessageStatus.PENDING) return new ReplayResult(0, 0, 0, 0);
    if (m.getApprovalStatus() != DlqApprovalStatus.APPROVED) throw new IllegalStateException("DLQ message not approved for replay: id=" + id);

    try {
      publish(m, "REPLAY_BY_ID");
      return new ReplayResult(1, 1, 0, 0);
    } catch (Exception ex) {
      handleFailure(m, "REPLAY_BY_ID", ex);
      return new ReplayResult(1, 0, 1, m.getStatus() == DlqMessageStatus.DEAD ? 1 : 0);
    }
  }

  private void publish(DlqMessage m, String action) throws Exception {
    Object ev = mapper.readValue(m.getPayload(), ScreeningResultCreatedEvent.class);

    // Partitioning strategy: key = aggregateId (transactionId) to keep ordering.
    String key = (m.getAggregateId() != null && !m.getAggregateId().isBlank()) ? m.getAggregateId() : m.getMessageKey();

    kafka.send(KafkaHeadersUtil.withEventHeaders(topics.topic().screeningResults(), key, ev, m.getOutboxId(), m.getCorrelationId()));

    m.setStatus(DlqMessageStatus.REPLAYED);
    m.setReplayedAt(Instant.now());
    m.setLastError(null);
    m.setReplayAttempts(m.getReplayAttempts() + 1);

    DlqReplayAudit a = new DlqReplayAudit();
    a.setMessage(m);
    a.setAction(action);
    a.setStatus("SUCCESS");
    a.setPublishedTopic(topics.topic().screeningResults());
    a.setPublishedKey(key);
    a.setCorrelationId(m.getCorrelationId());
    audit.save(a);
  }

  private void handleFailure(DlqMessage m, String action, Exception ex) {
    m.setReplayAttempts(m.getReplayAttempts() + 1);
    m.setLastError(ex.getMessage());

    if (m.getReplayAttempts() >= props.maxAttempts()) {
      m.setStatus(DlqMessageStatus.DEAD);
      m.setNextAttemptAt(Instant.now().plusSeconds(365L * 24 * 3600));
      audit(action, m, "DEAD", ex.getMessage());
    } else {
      m.setNextAttemptAt(nextAttempt(m.getReplayAttempts()));
      audit(action, m, "FAILED", ex.getMessage());
    }
  }

  private void audit(String action, DlqMessage m, String status, String err) {
    DlqReplayAudit a = new DlqReplayAudit();
    a.setMessage(m);
    a.setAction(action);
    a.setStatus(status);
    a.setError(err);
    a.setCorrelationId(m.getCorrelationId());
    audit.save(a);
  }

  private Instant nextAttempt(int attempts) {
    long base = Math.min(props.maxBackoffMillis(), (long) (props.initialBackoffMillis() * Math.pow(2, Math.max(0, attempts - 1))));
    long spread = (long) (base * props.jitterRatio());
    long delta = ThreadLocalRandom.current().nextLong(-spread, spread + 1);
    return Instant.now().plusMillis(Math.max(50, base + delta));
  }

  public record ReplayResult(int attempted, int succeeded, int failed, int dead) {}
}
