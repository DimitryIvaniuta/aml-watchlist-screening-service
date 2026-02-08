CREATE TABLE IF NOT EXISTS watchlist_batches (
  id BIGSERIAL PRIMARY KEY,
  source VARCHAR(128) NOT NULL,
  imported_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS watchlist_entries (
  id UUID PRIMARY KEY,
  batch_id BIGINT NOT NULL REFERENCES watchlist_batches(id),
  full_name VARCHAR(300) NOT NULL,
  first_name VARCHAR(120),
  last_name VARCHAR(120),
  full_name_norm VARCHAR(320) NOT NULL,
  last_name_norm VARCHAR(140),
  dob DATE,
  nationality VARCHAR(80),
  country VARCHAR(80),
  risk_level VARCHAR(16) NOT NULL,
  aliases JSONB,
  address_line1 VARCHAR(200),
  city VARCHAR(120),
  postal_code VARCHAR(40),
  address_country VARCHAR(80),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ix_watchlist_last_name_norm ON watchlist_entries(last_name_norm);

CREATE TABLE IF NOT EXISTS transactions (
  id UUID PRIMARY KEY,
  customer_full_name VARCHAR(300) NOT NULL,
  customer_full_name_norm VARCHAR(320) NOT NULL,
  customer_dob DATE,
  customer_nationality VARCHAR(80),
  customer_country VARCHAR(80),
  customer_id_number_token VARCHAR(128),
  amount NUMERIC(19,2) NOT NULL,
  currency VARCHAR(8) NOT NULL,
  address_line1 VARCHAR(200),
  city VARCHAR(120),
  postal_code VARCHAR(40),
  address_country VARCHAR(80),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS screening_results (
  id UUID PRIMARY KEY,
  transaction_id UUID NOT NULL UNIQUE REFERENCES transactions(id),
  decision VARCHAR(16) NOT NULL,
  score INT NOT NULL,
  reasons JSONB NOT NULL,
  engine_version VARCHAR(32) NOT NULL,
  watchlist_batch_id BIGINT REFERENCES watchlist_batches(id),
  correlation_id VARCHAR(128),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS outbox_events (
  id UUID PRIMARY KEY,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id VARCHAR(128) NOT NULL,
  event_type VARCHAR(128) NOT NULL,
  payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  published_at TIMESTAMPTZ,
  attempts INT NOT NULL,
  next_attempt_at TIMESTAMPTZ,
  status VARCHAR(16) NOT NULL,
  last_error VARCHAR(512),
  correlation_id VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS ix_outbox_due ON outbox_events(status, next_attempt_at);
