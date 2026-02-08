CREATE INDEX IF NOT EXISTS ix_dlq_messages_aggregate_id ON dlq_messages(aggregate_id);
CREATE INDEX IF NOT EXISTS ix_dlq_messages_outbox_id ON dlq_messages(outbox_id);
CREATE INDEX IF NOT EXISTS ix_dlq_messages_event_type ON dlq_messages(event_type);
CREATE INDEX IF NOT EXISTS ix_dlq_messages_message_key ON dlq_messages(message_key);
