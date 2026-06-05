# Datarun Platform

Domain-agnostic operations platform for field-based organizations. Offline-first, configurable, append-only event store with sync.

## Current Standing

Datarun has completed its initial architecture sequence and Phase 4 workflow/policy implementation. The current accepted baseline includes offline sync, rebuildable projections, deployer-authored configuration, platform-owned workflow patterns, conflict detection/resolution routing, production OIDC/JWT authority through explicit principal binding, and server-side assignment-admin command capability.

Current status and routing move faster than this README. Use:

- [AGENTS.md](AGENTS.md) for the default agent/implementer bootstrap.
- [docs/status.md](docs/status.md) for current routing and accepted implementation standing.
- [docs/agent-working-surface/README.md](docs/agent-working-surface/README.md) for post-Phase-4 steward/agent working-surface routing.
- [docs/implementation/module-interfaces.md](docs/implementation/module-interfaces.md) for implemented module boundaries.
- [docs/milestone-review/phase-4-review/architecture-rationale-and-routing-companion.md](docs/milestone-review/phase-4-review/architecture-rationale-and-routing-companion.md) for non-authoritative rationale, change routing, and test-intent context.

## Architecture

- **Storage**: Immutable append-only events. No updates, no deletes.
- **Sync**: Watermark-based push/pull. Idempotent, order-independent.
- **Identity**: Client-generated UUIDs. Works fully offline.
- **Authority**: Actor authority is resolved from authenticated principals and event-derived assignments; IdP groups/claims are not direct platform authority.
- **Projections**: Current state derived from events. Always rebuildable.
- **Configuration**: Deployer-authored setup defines shapes, activities, role actions, expression rules, severity overrides, and pattern bindings inside platform-owned mechanisms.

## Quick Start

```bash
# Start PostgreSQL
docker compose up -d db

# Run server
cd server
./mvnw spring-boot:run
```

Server starts at `http://localhost:8080`.

## Main Runtime Surfaces

- Sync push/pull and subject-history backfill.
- Subject summaries and event timelines.
- Config package delivery.
- Assignment create/end with authenticated-actor authority and command-capability checks.
- Conflict resolution APIs with designated-resolver enforcement.
- Production auth principal binding through deployment-managed provisioning.

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
- Flutter mobile client
- PostgreSQL 16, Flyway migrations
- JSON Schema (Draft 2020-12) for envelope, config, pattern, and payload contract validation

## Project Structure

```
contracts/              # Source-controlled process-boundary contracts
docs/                   # Architecture, decisions, status, implementation notes
server/                 # Spring Boot backend
mobile/                 # Flutter client
docker-compose.yml      # Development PostgreSQL + server
docker-compose.test.yml # Test PostgreSQL on port 15432
```

## License

Private - all rights reserved.
