# Datarun Platform — Codebase Map

> **Scope**: this file is a codebase map only. Strategy, phase tracking, invariants, and
> forward plans live in [`docs/charter.md`](docs/charter.md) and [`docs/ships/`](docs/ships/).
> Do not record status/test counts/architectural rules here — they drift.
>
> **Before drafting an IDR or starting new work**, read [`docs/flagged-positions.md`](docs/flagged-positions.md).
> Rule R-4: any FP whose `Blocks:` field names the upcoming work must be resolved or explicitly re-deferred.

---

## Current Ship

**Ship-1 — Offline Structured Capture Under Assigned Scope** — implementation COMPLETE,
retro pending. Spec: [`docs/ships/ship-1.md`](docs/ships/ship-1.md). Scenarios
delivered: [S00](docs/scenarios/00-basic-structured-capture.md),
[S01](docs/scenarios/01-entity-linked-capture.md),
[S03](docs/scenarios/03-user-based-assignment.md) under the [S19](docs/scenarios/19-offline-first.md)
offline constraint.

Acceptance: `WalkthroughAcceptanceTest` (3/3 pass) drives the real server as two
simulated devices through W-0 (happy path), W-1 (duplicate household → `identity_conflict`),
W-2 (out-of-scope capture → `scope_violation`). No mobile app yet — scripted CI gate
is satisfied; an emulator build is the next Ship task.

---

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Server | Java / Spring Boot | Java 17 (Temurin via sdkman), Spring Boot 3.2.5 |
| Server DB | PostgreSQL | 16.x (JSONB for event payloads) |
| Admin UI | Thymeleaf | server-rendered |
| Contracts | JSON Schema Draft 2020-12 | networknt json-schema-validator 1.4.0 |
| Migrations | Flyway Community | 9.22.3 |

Mobile (Flutter) is rebuilt per-ship; no active mobile code in-tree at Ship-1 close.

---

## Build & Test

```bash
# Start test database (host networking — works with VPN)
docker compose -f docker-compose.test.yml up -d

# Run server tests
cd server && ./mvnw test      # WalkthroughAcceptanceTest, 3 tests

# Stop test database
docker compose -f docker-compose.test.yml down
```

### Dev environment notes

- **VPN + Docker**: use `network_mode: host` — Windscribe breaks port forwarding.
- **Test DB port**: `localhost:15432` (local).
- **Java**: `sdk use java 17.0.14-tem`.
- If Flyway complains about checksum drift after schema edits, wipe the test
  volume: `docker compose -f docker-compose.test.yml down -v`.

---

## Repository Layout

```
contracts/                              # Language-neutral shared definitions
  envelope.schema.json                  # 11-field envelope, closed 6-type vocabulary
  sync-protocol.md                      # Ship-1 push/pull/config contract
  flag-catalog.md                       # Flag categories
  fixtures/                             # Shared test fixtures (not used in Ship-1)
  shapes/
    assignment_created.schema.json      # Platform-bundled (ADR-007 §S2)
    assignment_ended.schema.json
    conflict_detected.schema.json
    conflict_resolved.schema.json
    subject_split.schema.json
    subjects_merged.schema.json
    household_observation.schema.json   # Ship-1 deployer shape — NEW

server/                                 # Spring Boot app (fresh Ship-1 build; package
  pom.xml                               # dev.datarun.ship1)
  src/main/java/dev/datarun/ship1/
    DatarunApplication.java
    event/                              # Envelope + store
    sync/                               # Push/pull + bearer auth
    scope/                              # Event-replay scope reconstruction
    integrity/                          # Conflict detection (scope_violation, identity_conflict)
    config/                             # /api/sync/config + village registry
    admin/                              # Thymeleaf admin UI + dev bootstrap
  src/main/resources/
    db/migration/V1__ship1_schema.sql   # Sole migration (clean-slate)
    schemas/envelope.schema.json        # Copy of contracts/envelope.schema.json
    schemas/shapes/*.schema.json        # Copies of contracts/shapes/*
    templates/admin/events.html
    templates/admin/flags.html
  src/test/java/dev/datarun/ship1/
    acceptance/WalkthroughAcceptanceTest.java    # W-0, W-1, W-2

docs/                                   # Design + ship docs (see charter.md)
  charter.md                            # Current strategy + status — authoritative
  ships/ship-1.md                       # Current Ship spec
  ships/ship-1-retro.md                 # Ship-1 retro (written at close)
  adrs/                                 # 9 ADRs (all DECIDED)
  flagged-positions.md                  # Deferred verification register

scripts/check-convergence.sh            # Convergence-phase tool (dormant)
docker-compose.yml, docker-compose.test.yml
```

---

## Server — Package Map

All production code lives under `dev.datarun.ship1`.

### `event/` — Envelope validation + append-only store
| File | Purpose |
|------|---------|
| `Event.java` | 11-field envelope record (`id`, `type`, `shapeRef`, `activityRef`, `subjectType`, `subjectId`, `actorId`, `deviceId`, `deviceSeq`, `syncWatermark`, `timestamp`, `payload`). |
| `EnvelopeValidator.java` | Validates inbound envelopes against the bundled `envelope.schema.json`. |
| `ShapePayloadValidator.java` | Validates `payload` against the schema referenced by `shape_ref`. Unknown `shape_ref` → reject (Ship-1 strict choice). |
| `EventRepository.java` | Sole writer of `events`. `insert()` uses `ON CONFLICT (id) DO NOTHING` and returns `INSERTED`/`DUPLICATE`. Read helpers: `findById`, `findSince`, `findByShapeRefPrefix`, `findBySubjectId`, `findAll`. |
| `EventMapper.java` | JsonNode ↔ `Event` record mapping for the wire envelope. |

### `sync/` — Push/pull + bearer auth
| File | Purpose |
|------|---------|
| `SyncController.java` | `POST /api/sync/push` (batch validate → insert → detect → flag counts); `POST /api/sync/pull` (scope-filter events for the caller, paginated). |
| `ActorTokenRepository.java` | `actor_tokens` CRUD. `issueToken(actorId)` creates a 64-hex token. `resolveToken(token) → Optional<UUID>`. |
| `ActorAuthInterceptor.java` | `Authorization: Bearer <token>` → request attribute `ship1.actor_id`. 401 on missing/unknown. |
| `WebConfig.java` | Registers the interceptor on `/api/sync/**`. |

### `scope/` — Event-replay scope reconstruction (no cache)
| File | Purpose |
|------|---------|
| `ScopeResolver.java` | Replays `type=assignment_changed` events (`assignment_created/v1` / `assignment_ended/v1`) to derive the actor's active geographic scopes at a given instant. `activeGeographicScopes(actorId, at) → Set<UUID>`; `activeAssignments(actorId, at) → List<Assignment>`. |

### `integrity/` — Conflict detection
| File | Purpose |
|------|---------|
| `ConflictDetector.java` | Per-capture detection, post-persist. Emits `conflict_detected/v1` flag events with `type=alert` and `actor_ref.id = "system:conflict_detector/<category>"`. Server device_id is reserved `00000000-0000-0000-0000-000000000001`; `device_seq` from PostgreSQL sequence `server_device_seq`. Ship-1 categories: `scope_violation` (payload village_ref outside actor's active scope at event time) and `identity_conflict` (same normalized `household_name` + same village, different `subject_id`). |

### `config/` — Config delivery
| File | Purpose |
|------|---------|
| `ConfigController.java` | `GET /api/sync/config` — returns `{version, activities, shapes, villages}` scope-filtered to the caller's assigned villages. |
| `VillageRepository.java` | `villages` CRUD: `findById`, `findByIds`, `findAll`, `insert`. |

### `admin/` — Admin UI + dev helpers
| File | Purpose |
|------|---------|
| `AdminController.java` | Thymeleaf: `/admin/events` (full timeline), `/admin/flags` (unresolved flags = all `conflict_detected/v1` events, since Ship-1 has no resolution). |
| `DevBootstrapController.java` | `POST /dev/bootstrap`: seeds Mirpur district + 2 villages, 2 CHV actors with fresh tokens, 2 `assignment_created/v1` events binding CHV-A→village-1 and CHV-B→village-2. Returns the tokens in the response body. Dev-only. |

### Database migration
| Version | File | Tables |
|---------|------|--------|
| V1 | `V1__ship1_schema.sql` | `events` (PK `id`, UNIQUE `sync_watermark BIGSERIAL`, UNIQUE `(device_id, device_seq)`, envelope `type` CHECK constraint matches ADR-007 §S1 closure); `actor_tokens`; `villages`; `CREATE SEQUENCE server_device_seq START 1`. |

---

## Server — Test Classes

| Class | Tests | What it covers |
|-------|:-----:|---|
| `acceptance.WalkthroughAcceptanceTest` | 3 | W-0 happy path, W-1 duplicate household across devices, W-2 out-of-scope capture. Drives the real HTTP surface via `TestRestTemplate`. Asserts flags are shape-discriminated (`conflict_detected/v1` + `type=alert`) never type-discriminated. |

Run: `cd server && ./mvnw test`.

---

## Forbidden Patterns (at Ship-1 close)

| # | Forbidden | Why |
| --- | --- | --- |
| F1 | Add or modify envelope fields | Envelope is 11 fields, frozen at ADR-001 §S3 + ADR-008. |
| F2 | Add a 7th envelope `type` value | Vocabulary closed at 6 by ADR-007 §S1. A new "type" is a new *shape*, not a new type. |
| F-A2 | Branch integrity logic on envelope `type` | Integrity events carry `type=alert` + distinguishing `shape_ref`. Any `if (type == "conflict_detected")`-style code is wrong by construction. Use `shape_ref`. |
| F-A3 | Emit system flags without `system:{component}/{id}` actor_ref | ADR-008 §S2. |
| F-A4 | Encode authorship in envelope `type` | `type` answers "what pipeline"; `actor_ref` answers "who authored". |
| F3 | Write to `events` outside `EventRepository` | Write-path discipline. |
| F4 | Modify or delete persisted events | Append-only is the foundational invariant (ADR-001 §S1). |
| F6 | Reject events for state staleness | Accept-and-flag (ADR-006 §S1) — events are never rejected for state. |
| F11 | Schema changes without migration scripts | Flyway migration per change. |
| F15 | Silent deferral | If you observe a position correct today but drift-prone, add an FP to [`docs/flagged-positions.md`](docs/flagged-positions.md) before close (Rule R-1). |

---

## Journal Triggers

Record in the active Ship retro (`docs/ships/ship-N-retro.md`) when:

1. An implementation-grade choice is made that isn't pre-decided in the spec.
2. Something is tried and abandoned after >1 hour.
3. A quality gate passes or fails.
4. An environment/tooling workaround is adopted.
5. A ledger row or ADR position comes under real load — capture the observation.

---

## Codebase Map Maintenance

This file is the primary context source for new sessions. **At each Ship close**,
update the Server Package Map + Test Classes to match the tree before tagging.
Do not move strategy/status/invariants content in here — that belongs to
[`docs/charter.md`](docs/charter.md) and [`docs/flagged-positions.md`](docs/flagged-positions.md).
