# Datarun Platform

Domain-agnostic operations platform for field-based organizations. Offline-first, configurable, append-only event store with sync.

## Status

**Phase 0a — Server Core** ✅

| Component | Status |
|-----------|--------|
| Event envelope contract (11 fields) | Done |
| Sync protocol (push/pull) | Done |
| Spring Boot server (4 endpoints) | Done |
| PostgreSQL event store | Done |
| Integration tests (11 tests, 8 quality gates) | Done |
| CI pipeline (GitHub Actions) | Done |

## Architecture

- **Storage**: Immutable append-only events. No updates, no deletes.
- **Sync**: Watermark-based push/pull. Idempotent, order-independent.
- **Identity**: Client-generated UUIDs. Works fully offline.
- **Projections**: Current state derived from events. Always rebuildable.

## Quick Start

```bash
# Start PostgreSQL
docker compose up -d db

# Run server
cd server
./mvnw spring-boot:run
```

Server starts at `http://localhost:8080`.

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/sync/push` | Push events from device |
| POST | `/api/sync/pull` | Pull events since watermark |
| GET | `/api/subjects` | List subjects with summaries |
| GET | `/api/subjects/{id}/events` | Event timeline for a subject |

## Testing

```bash
# Start test database
docker compose -f docker-compose.test.yml up -d

# Run tests
cd server
./mvnw test
```

## Tech Stack

- Java 17, Spring Boot 3.2.5, Maven
- PostgreSQL 16, Flyway migrations
- JSON Schema (Draft 2020-12) for envelope validation

## Project Structure

```
contracts/          # Source-of-truth schemas and protocol docs
  envelope.schema.json
  sync-protocol.md
server/             # Spring Boot application
  src/main/
    java/           # Event store, sync, subject projection
    resources/
      db/migration/ # Flyway SQL migrations
docker-compose.yml      # Dev: PostgreSQL + server
docker-compose.test.yml # Test: PostgreSQL on port 15432
```

## License

Private — all rights reserved.
