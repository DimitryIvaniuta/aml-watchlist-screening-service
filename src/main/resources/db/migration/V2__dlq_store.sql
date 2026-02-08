CREATE TABLE IF NOT EXISTS dlq_messages (
  id BIGSERIAL PRIMARY KEY,
  topic VARCHAR(200) NOT NULL,
  partition INT NOT NULL,
  offset BIGINT NOT NULL,
  message_key VARCHAR(500),
  event_type VARCHAR(200) NOT NULL,
  outbox_id UUID,
  aggregate_type VARCHAR(200),
  aggregate_id VARCHAR(200),
  payload JSONB NOT NULL,
  error TEXT,
  received_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
  replay_attempts INT NOT NULL DEFAULT 0,
  next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_error TEXT,
  replayed_at TIMESTAMPTZ,
  correlation_id VARCHAR(128),
  approval_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
  requested_by VARCHAR(128),
  requested_at TIMESTAMPTZ,
  approved_by VARCHAR(128),
  approved_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_dlq_messages_topic_partition_offset
  ON dlq_messages(topic, partition, offset);

CREATE INDEX IF NOT EXISTS ix_dlq_messages_status_approval_next_attempt
  ON dlq_messages(status, approval_status, next_attempt_at);

CREATE TABLE IF NOT EXISTS dlq_replay_audit (
  id BIGSERIAL PRIMARY KEY,
  dlq_message_id BIGINT NOT NULL REFERENCES dlq_messages(id),
  action VARCHAR(40) NOT NULL,
  status VARCHAR(30) NOT NULL,
  published_topic VARCHAR(200),
  published_key VARCHAR(500),
  error TEXT,
  correlation_id VARCHAR(128),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_dlq_replay_audit_msg
  ON dlq_replay_audit(dlq_message_id, created_at);
