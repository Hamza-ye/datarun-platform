# Phase 0: Core Loop

> S00 end-to-end — the P7 benchmark. "A field worker captures data offline, syncs, and the data appears on the server."

**Exercises**: ADR-1 (partially). SY-1, SY-2, SY-3, SY-4.

**Primitives built**: Event Store (device + server), Projection Engine (minimal: per-subject state derivation), Shape Registry (minimal: load shapes from config, no authoring UI).

**Contracts exercised**: C1 (event integrity), C2 (stream → projection), C11 (shape versions available).

---

## 1. What Phase 0 Proves

Phase 0 is the P7 benchmark: "the simplest scenario stays simple." If S00 (basic structured capture) doesn't work cleanly as the first thing we build, the architecture has a problem.

It validates:
- Events can be created offline with a complete 11-field envelope
- Events sync to the server via idempotent, order-independent exchange
- Projections derive current state from the event stream
- A shape defines what gets captured
- The entire loop works on a low-end Android device with SQLite

**What Phase 0 does NOT include**: No conflict detection, no identity resolution, no authorization, no triggers, no patterns, no multi-actor, no configuration authoring. One user, one device, one hardcoded shape, happy path only. This is intentional — Phase 0 tests the storage + sync + projection core in isolation.

---

## 2. Sub-Phases

Phase 0 builds bottom-up in three sub-phases instead of building all three codebases simultaneously.

### Phase 0a: Server Core

**Focus**: Event Store + Sync endpoints + minimal read API. One codebase, one database.

**Deliverables**:
1. `contracts/envelope.schema.json` — the 11-field event envelope
2. `contracts/sync-protocol.md` — push/pull contract
3. `server/` — Spring Boot application:
   - PostgreSQL event table
   - `POST /api/sync/push` — accept events, deduplicate, assign watermarks
   - `POST /api/sync/pull` — return events since watermark, paginated
   - `GET /api/subjects` — list subjects with latest event summary (projection)
   - `GET /api/subjects/{id}/events` — event timeline for one subject
4. `docker-compose.yml` — PostgreSQL + server, one command start
5. CI pipeline — build + test on every push

**Quality gates**:
- [x] Push 10 events via curl → all persisted with watermarks assigned
- [x] Push same 10 events again → zero duplicates, same response
- [x] Pull with watermark 0 → all events returned, ordered
- [x] Pull with watermark N → only events after N returned
- [x] Push with malformed envelope → 400 error, nothing persisted
- [x] Integration tests cover all 5 checks above, running against real PostgreSQL
- [x] `docker compose up` starts cold in < 60 seconds
- [x] JSON Schema validation: every event accepted by push endpoint validates against `envelope.schema.json`

**What's NOT in 0a**: No Flutter. No Angular. No shape rendering. No form logic.

---

### Phase 0b: Mobile Core

**Focus**: Flutter app — offline capture, local storage, shape-driven form, sync client.

**Deliverables**:
1. `mobile/` — Flutter application:
   - SQLite event table
   - Config loader: reads `basic_capture/v1` shape from bundled JSON
   - S3 (Form screen): renders fields from shape definition, produces a complete 11-field event
   - S1 (Work List): shows subjects with latest capture timestamp
   - S2 (Subject Detail): shows event timeline for one subject
   - Sync service: push unpushed events, pull new events, update watermarks
   - U1 (Sync Panel): manual sync trigger with status
2. One bundled shape definition: `basic_capture/v1` (~5 fields: name, date, category, notes, numeric value)

**Quality gates**:
- [x] Airplane mode: create 3 events for 2 subjects → events in SQLite with `pushed=0` → subjects appear in Work List
- [x] Sync: connect to Phase 0a server → push 3 events → server reports `accepted: 3` → device marks `pushed=1`
- [x] Pull: server has events from another source → device pulls → new subjects appear in Work List
- [x] Idempotency: trigger sync twice → no duplicates on device or server
- [x] Form rendering: all 5 field types render correctly from shape JSON (not hardcoded widgets)
- [x] Back navigation: S1 → S2 → S3 → back → S2 → back → S1 works correctly
- [x] Unsaved form: back from S3 with data → confirmation dialog appears
- [x] Widget tests for Form Engine (FieldResolver → WidgetMapper → EventAssembler pipeline)
- [x] Builds cleanly: `flutter build apk` produces installable APK

**What's NOT in 0b**: No admin UI. No multi-user. No conflict detection. Happy path only.

---

### Phase 0c: Integration & Admin Baseline

**Focus**: End-to-end S00 flow works. Minimal admin visibility. All acceptance criteria passing.

**Deliverables**:
1. `admin/` — minimal Angular app OR server-rendered pages (decide at start of 0c):
   - Subject list view (reads from server projection API)
   - Subject event timeline view
2. End-to-end test script: captures on device → syncs → visible in admin
3. All 8 acceptance criteria verified and documented

**Quality gates**:
- [x] All 8 acceptance criteria (§5) pass
- [x] End-to-end demo: offline capture → sync → server → admin view (recorded or scripted)
- [x] Contract validation: events created by mobile validate against `envelope.schema.json`
- [x] Events created by mobile, when pulled back, are byte-identical in payload
- [x] Performance baseline recorded: capture time, sync time, projection time on reference device or emulator

---

## 3. Scope by Module

**contracts/**

- `envelope.schema.json` — 11-field event envelope JSON Schema
- `sync-protocol.md` — two endpoints: push (device → server) and pull (server → device), watermark-based
- One sample shape definition (S00: basic structured capture — ~5 fields)

**server/**

- Event Store: PostgreSQL schema for events table. Insert-only. JSONB payload column. Index on `subject_ref` and `device_id + device_seq`.
- Projection Engine: per-subject state query (latest event per subject, grouped). Full replay — no materialized views yet.
- Sync endpoints:
  - `POST /api/sync/push` — accepts array of events, deduplicates by `id`, returns count persisted
  - `POST /api/sync/pull` — accepts `since_watermark`, returns events after that watermark (paginated)
- Shape loader: reads shape definitions from classpath (no authoring, no DB storage yet)
- Minimal admin view: list subjects, view events per subject (read-only, server-rendered or API for Angular)

**mobile/**

- Event Store: SQLite schema mirroring server structure. Insert-only. Same envelope.
- Projection Engine: per-subject list query. Full replay from local events.
- Capture screen: form rendered from shape definition (5 fields). Produces one `capture` event with full envelope.
- Subject list screen: shows subjects with latest capture state from projection.
- Sync service:
  - Push: send unsent events (events with `device_seq` > last confirmed push)
  - Pull: request events since last watermark, insert into local store
  - Manual trigger (button) — no background sync yet
- Config loader: reads one hardcoded shape definition from bundled assets

**admin/** (minimal)

- Event list view: server-rendered or minimal Angular page showing all synced events
- Subject list view: per-subject latest state

---

## 4. Technical Specifications

### Envelope for Phase 0

All 11 fields are present from the first event. Phase 0 uses fixed values for fields that become dynamic later:

| Field | Phase 0 value |
|-------|--------------|
| `id` | Client-generated UUID |
| `type` | `capture` (only type used in Phase 0) |
| `shape_ref` | Hardcoded: `basic_capture/v1` |
| `activity_ref` | `null` (optional field, no activities in Phase 0) |
| `subject_ref` | `{type: "subject", id: <client-generated UUID>}` |
| `actor_ref` | `{type: "actor", id: <hardcoded single-user UUID>}` |
| `device_id` | Generated on first app launch, persisted |
| `device_seq` | Monotonically increasing integer per device |
| `sync_watermark` | Server-assigned on receipt, used for pull pagination |
| `timestamp` | Device clock (advisory only) |
| `payload` | Shape-conforming JSON object |

### SQLite schema (device)

```sql
CREATE TABLE events (
    id              TEXT PRIMARY KEY,
    type            TEXT NOT NULL,
    shape_ref       TEXT NOT NULL,
    activity_ref    TEXT,
    subject_ref     TEXT NOT NULL,   -- JSON: {type, id}
    actor_ref       TEXT NOT NULL,   -- JSON: {type, id}
    device_id       TEXT NOT NULL,
    device_seq      INTEGER NOT NULL,
    sync_watermark  INTEGER,         -- NULL until synced, server-assigned
    timestamp       TEXT NOT NULL,   -- ISO 8601
    payload         TEXT NOT NULL,   -- JSON
    pushed          INTEGER NOT NULL DEFAULT 0,  -- 0 = not yet pushed
    UNIQUE(device_id, device_seq)
);
```

`pushed` is a device-local bookkeeping column — not part of the envelope. It tracks which events have been successfully pushed to the server.

### PostgreSQL schema (server)

```sql
CREATE TABLE events (
    id              UUID PRIMARY KEY,
    type            VARCHAR(50) NOT NULL,
    shape_ref       VARCHAR(255) NOT NULL,
    activity_ref    VARCHAR(255),
    subject_ref     JSONB NOT NULL,
    actor_ref       JSONB NOT NULL,
    device_id       UUID NOT NULL,
    device_seq      INTEGER NOT NULL,
    sync_watermark  BIGSERIAL,       -- Server-assigned on insert
    timestamp       TIMESTAMPTZ NOT NULL,
    payload         JSONB NOT NULL,
    received_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(device_id, device_seq)
);

CREATE INDEX idx_events_subject ON events ((subject_ref->>'id'));
CREATE INDEX idx_events_watermark ON events (sync_watermark);
```

`sync_watermark` as `BIGSERIAL` gives each event a monotonically increasing server-assigned sequence number. `received_at` is server-side bookkeeping for ops — not part of the envelope.

### Sync protocol

**Push** — `POST /api/sync/push`

```
Request:  { events: [Event, ...] }
Response: { accepted: N, duplicates: N }
```

Server deduplicates by `id`. For each new event, assigns `sync_watermark` on insert. Returns counts. Idempotent — sending the same events twice produces the same result.

**Pull** — `POST /api/sync/pull`

```
Request:  { since_watermark: W, limit: 100 }
Response: { events: [Event, ...], latest_watermark: W' }
```

Returns events with `sync_watermark > W`, ordered by `sync_watermark`, paginated. Device stores `latest_watermark` and uses it for next pull. On first sync, `since_watermark = 0`.

---

## 5. Acceptance Criteria

1. **Offline capture**: Device creates events with full 11-field envelope while airplane mode is on. Events are stored in SQLite.
2. **Sync push**: When connectivity is available, device pushes unpushed events to server. Server persists and assigns watermarks.
3. **Sync pull**: Device pulls events it hasn't seen (for Phase 0: its own events echoed back with watermarks, establishing the pattern for multi-device in Phase 1).
4. **Idempotent sync**: Pushing the same events twice does not create duplicates. Pulling the same watermark range returns the same results.
5. **Projection on device**: Subject list screen shows latest state derived from local events.
6. **Projection on server**: Admin view shows subjects and their events, derived from server event store.
7. **Shape-driven form**: Capture form fields are defined by the shape, not hardcoded in Flutter widget code.
8. **Envelope completeness**: Every stored event — device and server — has all 11 fields populated (activity_ref excluded as null is valid).

---

## 6. Reversibility Triage (retroactive)

Classified after Phase 0 completion. See [execution-plan.md §6.1](../execution-plan.md#61-reversibility-triage-required-step) for the framework.

| Decision | Bucket | Why | Implemented as | Evolve-later trigger |
|----------|--------|-----|----------------|---------------------|
| 11-field event envelope | **Lock** | Persisted forever on both sides. F1 forbids changes. Every event ever created carries this structure. | ADR-1 S1, S5. 11 fields final. | — (irreversible by design) |
| PostgreSQL events schema | **Lock** | Server data store. Any change = Flyway migration + downstream query changes. | BIGSERIAL watermark, JSONB payload, UNIQUE(device_id, device_seq). | — |
| SQLite events schema | **Lock** | Device data store. Any change = `onUpgrade` migration on every device. | Mirrors server structure. `pushed` bookkeeping column (device-local). | — |
| Sync protocol (push/pull) | **Lock** | Cross-boundary contract. Both codebases depend on request/response shape. | Watermark-based pagination. Push deduplicates by `id`. | — |
| PE: full replay from events | **Lean** | Code-only. Interface is C2 (stream → projection). No persisted projection state. | Full replay, no materialized views. | Projection rebuild >200ms/subject (escape hatch) |
| Shape loading mechanism | **Lean** | Bundled JSON, replaced by design in Phase 3. Interface is C11 (shapes available to PE). | Classpath resource (server), bundled asset (mobile). | Phase 3 replaces this |
| Form engine pipeline | **Lean** | Code-only. Interface is "shape → rendered form → complete event." | FieldResolver → WidgetMapper → EventAssembler. | Shape complexity requires >6 field types |
| Manual sync trigger | **Lean** | Code-only. No protocol impact. | Button in sync panel. SharedPreferences for watermark. | Deployment needs background sync |
| Admin UI technology | **Leaf** | No downstream contracts. Internal visibility only. | Thymeleaf server-rendered (IDR-006). | — |

**Skill callouts**: None — Phase 0 Locks were all settled by ADR-1 before implementation began.

---

## 7. Milestones

Implementation decisions and discoveries are in [`docs/decisions/`](../../decisions/) (idr-001 through idr-006).

---

### Phase 0a: Server Core

**2026-04-16** — Phase 0a complete. All 8 quality gates pass. 11 integration tests green (8 sync + 3 subject). BUILD SUCCESS.

- Deliverables: All 5 produced (contracts, server app, docker-compose, CI pipeline)
- Commits: `40bca48` (Phase 0a) + `2d30d91` (README)
- Metrics: Docker compose cold start ~4s. Test suite ~15s against real PostgreSQL.
- IDRs: idr-001 through idr-005

---

### Phase 0b: Mobile Core

_(Milestone pending — sub-phase completed as part of integrated Phase 0.)_

---

### Phase 0c: Integration & Admin Baseline

**2026-04-17** — Phase 0c complete. All 5 quality gates pass. All 8 acceptance criteria (§5) verified.

- End-to-end demo confirmed on real Android device (SM-G715U1, Android 13)
- Contract validation: all events validate against `envelope.schema.json` (Draft 2020-12)
- Payload byte-identity: round-tripped payloads are JSON-identical
- IDR: idr-006

**Performance baseline** (5 events, 3 subjects, local network):

| Operation | Time |
|-----------|------|
| Sync push (5 dupes) | 57ms |
| Sync pull (all events) | 13ms |
| Subject list (API) | 19ms |
| Subject detail (API) | 9ms |
| Admin list (Thymeleaf) | 721ms (cold, includes template compilation) |
| Admin detail (Thymeleaf) | 42ms |

**Phase 0 status**: All three sub-phases (0a, 0b, 0c) complete. The P7 benchmark is proven — "a field worker captures data offline, syncs, and the data appears on the server."
