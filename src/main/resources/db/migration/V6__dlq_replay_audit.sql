-- Ensure replay audit table exists (idempotent).
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
