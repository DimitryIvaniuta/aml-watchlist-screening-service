-- Ensure 4-eyes approval workflow columns exist (safe for already-migrated DBs).
ALTER TABLE dlq_messages
  ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
  ADD COLUMN IF NOT EXISTS requested_by VARCHAR(128),
  ADD COLUMN IF NOT EXISTS requested_at TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS approved_by VARCHAR(128),
  ADD COLUMN IF NOT EXISTS approved_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS ix_dlq_messages_status_approval_next_attempt
  ON dlq_messages(status, approval_status, next_attempt_at);
