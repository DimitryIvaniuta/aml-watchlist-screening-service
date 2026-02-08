CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS gin_dlq_messages_aggregate_id_trgm
  ON dlq_messages USING gin (aggregate_id gin_trgm_ops);

CREATE INDEX IF NOT EXISTS gin_dlq_messages_message_key_trgm
  ON dlq_messages USING gin (message_key gin_trgm_ops);

CREATE INDEX IF NOT EXISTS gin_dlq_messages_event_type_trgm
  ON dlq_messages USING gin (event_type gin_trgm_ops);
