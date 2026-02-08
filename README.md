# AML Watchlist Screening Service (async + explainable + DLQ ops)

Production-grade reference implementation:
- Watchlist ingestion (API + optional Kafka)
- Async transaction screening
- Explainable, auditable decisions (reasons stored as JSON)
- Kafka publish via Outbox (retry/backoff + DLQ)
- DLQ storage in Postgres + 4-eyes approval + replay-by-id + replay job
- DLQ Ops API returns DTOs + paging + multi-sort + searchQ
- Kafka headers: `x-event-id` (outboxId) + `x-correlation-id`
- Consumer-side dedup example using Redis TTL
- Postgres + Flyway, Kafka (KRaft), Redis, tests, Postman

## Run local infra
```bash
docker compose up -d
```

## Run the app
```bash
./gradlew bootRun
# or: gradle bootRun
```

## Core endpoints
- `POST /api/watchlists/import`
- `POST /api/transactions` (async)
- `GET /api/screenings/{transactionId}`
- `GET /api/audit/screenings/export?from=...&to=...` (CSV)

## DLQ ops UI endpoints (DTOs)
- List/search (paged + multi-sort):
  - `GET /api/dlq/messages?page=0&size=20&sort=receivedAt,desc&sort=nextAttemptAt,asc`
- Filters:
  - `status`, `approval`, `eventType`, `aggregateId`, `outboxId`
- Search box:
  - `searchQ` matches across `aggregateId`, `outboxId` (UUID), `messageKey`, `eventType`
- Details:
  - `GET /api/dlq/messages/{id}` (includes payload JSON)
- 4-eyes flow:
  1. `POST /api/dlq/messages/{id}/request` header `X-Operator`
  2. `POST /api/dlq/messages/{id}/approve` header `X-Operator` (must differ)
  3. `POST /api/dlq/messages/{id}/replay`

## Kafka topics
- `screening-results` (primary)
- `screening-results-dlq` (DLQ)
- `watchlist-updates` (optional ingestion)

## Postman
Import from `/postman`.



## Swagger
- UI: http://localhost:8080/swagger-ui
- Spec: http://localhost:8080/v3/api-docs
