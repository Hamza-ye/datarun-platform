# Phase 1: Identity & Integrity

> Multi-subject, multi-device reality. "Two devices capture data about the same subject; conflicts are detected and surfaced for resolution."

**Exercises**: ADR-2 fully.

**Primitives built**: Identity Resolver (alias resolution, merge/split), Conflict Detector (accept-and-flag, 3 flag types), Projection Engine (extended: alias resolution, flagged-event exclusion).

**Contracts exercised**: C3 (ES accepts all events), C4 (PE → CD: projected state), C7 (IR → PE: alias resolution), C8 (CD → PE: flag events).

---

## 1. What Phase 1 Proves

Phase 1 proves the platform can handle reality — concurrent work from multiple devices, conflicting observations, and identity confusion — without losing data or silently hiding problems.

It validates:
- Events are never rejected for state staleness (accept-and-flag)
- Concurrent modifications to the same subject are detected and flagged
- Flagged events remain in the timeline but are excluded from state derivation
- Subject identity can be corrected after the fact (merge/split) without rewriting history
- The alias table resolves identity chains in a single hop (transitive closure)
- Flag resolution re-derives state to include the previously-excluded event

**What Phase 1 does NOT include**: No authorization scoping (Phase 2). No configuration authoring (Phase 3). No triggers or workflows (Phase 4). All users see all data. Single hardcoded shape. `identity_conflict` detection is manual only — auto-detection of duplicate subjects is a later concern.

---

## 2. Design Decisions

5 implementation design decisions must be resolved before building. 3 benefit from skilled agent review. See analysis below each decision.

### DD-1: Concurrency Detection Mechanism

**Problem**: When Device A pushes an event for Subject X, how does the server determine that Device A didn't know about Device B's concurrent events for the same subject? The envelope carries causal metadata (`device_id`, `device_seq`, `sync_watermark`) but none directly encode "what this device had seen when it created the event."

**Options**:

| | Approach | Pro | Con |
|---|---------|-----|-----|
| A | Server tracks per-device last-pull watermark (`device_sync_state` table). During push: check if Subject X has events with `sync_watermark > device's last pull` from other devices → concurrent | No protocol change. Server-internal state. | Edge case: device creates events, fails push, pulls again (updates watermark), then retries push of old events — server overestimates device's knowledge horizon for those events |
| B | Device reports knowledge horizon in push request: `{events: [...], device_id: "...", last_pull_watermark: N}`. Protocol metadata, not envelope change (F1 safe) | Precise per-push-batch. Stateless server. | Protocol change to push request shape. Device could misreport. |
| C | Parent event reference in payload — each event includes "last known event ID for this subject" | Precise per-event causal chain | Pollutes domain payload with sync metadata. Couples capture logic to sync state. |

**Recommended**: B — cleanest causality signal. Precise per-push. The device already tracks its watermark in SharedPreferences. Push request body is `contracts/sync-protocol.md`, not the envelope.

**Detection algorithm** (per-event, not per-batch):
1. For each pushed event, extract `subject_ref.id`
2. Compute effective knowledge horizon: `W_effective = min(event.sync_watermark, request.last_pull_watermark)`
3. Query: does Subject X have events from *other devices* with server-assigned `sync_watermark > W_effective`?
4. If yes → the pushing device didn't see those events → `concurrent_state_change` flag on the pushed event

The per-event `sync_watermark` (from the envelope, [2-S1]) captures the device's knowledge at event creation time. The batch `last_pull_watermark` is a ceiling (handles buggy devices overstating envelope watermarks). Using `min()` is strictly more conservative than either value alone.

**Why per-event, not per-batch**: A device may create events, pull (advancing its watermark), then push. The batch watermark reflects current knowledge, but older events in the batch were created at a lower horizon. Using only the batch watermark creates a false-negative window for events created before the pull. (Agent review: Software Architect identified this as a structural false negative in normal operation, not just failure scenarios.)

**False positive risk**: If Subject X was updated between W_effective and push time, but the update was trivial or non-conflicting, the flag fires anyway. This is conservative (better to over-flag than miss real conflicts). Resolution is fast for false positives.

**Asymmetric detection**: The first pusher's events may escape detection (the concurrent events from other devices don't exist on the server yet). The second pusher's events are flagged with context pointing to the first pusher's events. This is acceptable — the reviewer sees both sides from the flag. A sweep job catches any remaining asymmetry.

**Agent review outcome**: Software Architect validated. DD-1 is safe with the `min()` per-event fix. No protocol change needed — uses existing envelope data.

---

### DD-2: Server as Event Producer

**Problem**: Merge, split, conflict detection, and resolution events are server-generated. The envelope requires `device_id` and `device_seq`. The server needs a stable event-producer identity.

**Approach**:
- Server gets a persistent `device_id` UUID: env var `SERVER_DEVICE_ID` primary, DB-stored fallback (auto-generated on first boot, stored in `server_identity` single-row table)
- Server maintains a monotonic `device_seq` counter: PostgreSQL SEQUENCE (`nextval('server_device_seq')`). Gaps from rollback are expected and harmless — S1 requires monotonically increasing, not gapless.
- Server-generated events receive `sync_watermark` immediately on insert (they're already on the server)
- `actor_ref` for system-generated events: `{type: "actor", id: "system"}` for auto-detection events, `{type: "actor", id: <coordinator_uuid>}` for human-triggered merge/split/resolution
- `ServerIdentity` bean must be initialized at startup (constructor injection) before any push processing

**Mobile impact**: Device receives server-generated events via normal pull. `device_id = server_uuid` is just another device ID — no special handling needed. The event is processed like any other.

**Agent review outcome**: Backend Architect validated. SEQUENCE behavior under rollback confirmed safe. Concurrent nextval() returns unique monotonic values across sessions. Env-var-with-DB-fallback recommended for Docker deployment.

---

### DD-3: Alias Table Storage

**Approach** (recommended): Materialized table with eager transitive closure — contract C7 demands single-hop lookup.

**Server schema**:
```sql
CREATE TABLE subject_aliases (
    retired_id   UUID PRIMARY KEY,
    surviving_id UUID NOT NULL,
    merged_at    TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_aliases_surviving ON subject_aliases (surviving_id);
```

**Merge procedure** (full, within a single transaction):
```sql
BEGIN;

-- Step 0: Acquire locks — prevents concurrent merge race condition.
-- Eager-insert lifecycle rows (active is the default, changes no semantics)
-- then lock in consistent order to prevent deadlocks.
INSERT INTO subject_lifecycle (subject_id, state)
  VALUES ($retired_id, 'active') ON CONFLICT (subject_id) DO NOTHING;
INSERT INTO subject_lifecycle (subject_id, state)
  VALUES ($surviving_id, 'active') ON CONFLICT (subject_id) DO NOTHING;

SELECT state FROM subject_lifecycle
  WHERE subject_id IN ($retired_id, $surviving_id)
  ORDER BY subject_id FOR UPDATE;
-- Application checks: both must be 'active'. If not → ROLLBACK.

-- Step 1: Cascade existing aliases pointing to retired → surviving
UPDATE subject_aliases SET surviving_id = $surviving_id
  WHERE surviving_id = $retired_id;

-- Step 2: Record new alias (idempotent for replay safety)
INSERT INTO subject_aliases (retired_id, surviving_id, merged_at)
  VALUES ($retired_id, $surviving_id, NOW())
  ON CONFLICT (retired_id) DO NOTHING;

-- Step 3: Archive retired subject
UPDATE subject_lifecycle SET state = 'archived', archived_at = NOW()
  WHERE subject_id = $retired_id;

-- Step 4: Insert SubjectsMerged event
INSERT INTO events (...) VALUES (...);

COMMIT;
-- Step 5 (application): refresh alias cache
```

**Why row-level locking** (Agent review: Database Optimizer identified): Under READ COMMITTED, two concurrent merges involving a shared subject can produce a broken alias table — T1 inserts A→B while T2 cascades B→C, but T2 doesn't see T1's uncommitted insert. Result: A→B and B→C exist but no A→C. Single-hop violated. `SELECT ... FOR UPDATE` on lifecycle rows serializes merges that share operands.

**Alias cache**: Full alias table loaded into `ConcurrentHashMap<UUID, UUID>` at startup. Refreshed after each merge event. At hundreds-to-thousands of entries (<100KB), this is the optimal hot-path strategy — zero DB round-trips per event resolution.

**Device schema**: Same structure in SQLite. Updated when `subjects_merged` events are received during pull.

**Terminology note**: The alias table uses `retired_id` (ADR-002 merge terminology). Both merged-retired and split-archived subjects enter `archived` state in `subject_lifecycle`. The alias table's `retired_id` is merge-specific; the lifecycle table normalizes all non-active subjects to `archived`.

**Agent review outcome**: Database Optimizer validated with 3 required fixes applied above (row-level locking, idempotent INSERT, S9 precondition check). Schema and indexes confirmed correct. In-memory cache recommended for hot path.

---

### DD-4: Conflict Detection Intercept Point

**Approach** (recommended): Synchronous, same request, **separate transaction**.

**Pipeline**:
```
Push request arrives
  → Transaction 1: Validate schema → Persist events → Commit
  → Transaction 2: CD evaluates new events → Persist flag events → Commit
  → Return response: {accepted, duplicates, flags_raised}
```

If Transaction 1 succeeds but Transaction 2 fails: events are persisted (C3 satisfied), flags are missing (acceptable — can be detected later by sweep job). CD failure never blocks event persistence.

**Implementation**: Use `TransactionTemplate` (programmatic transaction management) — makes the two-transaction boundary explicit in code, avoids `@Transactional` proxy pitfalls. Single Tx2 for all flags in the batch (not per-flag) — flag failures are systemic (DB down), not per-flag.

**Deterministic flag IDs**: Derive the flag event UUID from `(source_event_id + shape_ref + flag_category)` — enables idempotent sweep re-evaluation with `ON CONFLICT DO NOTHING`. (`shape_ref` was added to the derivation input in Phase 3e / DD-3 so future integrity shapes cannot collide; pre-3e the derivation used only `source_event_id + flag_category`.)

**Sweep job**: Stateless re-evaluation every 5 minutes. No tracking tables. Re-runs CD on subjects with multi-device events in a trailing window. Deterministic IDs make duplicate flags impossible. Catches both Tx2 failures and the rare asymmetric-flagging race (two concurrent pushes for the same subject where one push's Tx2 runs before the other's Tx1 commits).

**Agent review outcome**: Backend Architect validated. Failure modes confirmed correct. TransactionTemplate recommended over @Transactional. Sweep approach validated as minimum-viable with zero additional schema.

---

### DD-5: Identity Conflict Detection Scope

**Decision**: `identity_conflict` is **manual-only in Phase 1**. Coordinators flag potential duplicates via admin UI. Auto-detection (entity resolution heuristics) is a later concern when shapes and domain semantics are richer.

The CD auto-detects only:
- `concurrent_state_change` — algorithmic (DD-1 mechanism)
- `stale_reference` — event references a retired subject ID (alias table lookup)

The pipeline handles all 3 flag types identically. Only the detection trigger differs.

---

## 3. Reversibility Triage (retroactive)

Classified after Phase 1 completion. See [execution-plan.md §6.1](../execution-plan.md#61-reversibility-triage-required-step) for the framework.

| Decision | Bucket | Why | Implemented as | Evolve-later trigger |
|----------|--------|-----|----------------|---------------------|
| DD-1: Concurrency detection (knowledge horizon) | **Lock** | Protocol extension (`last_pull_watermark` in push request). Detection algorithm determines which events get flagged — flags are persisted events. | `min(event.sync_watermark, request.last_pull_watermark)` per-event. IDR-007. | — |
| DD-2: Server event producer identity | **Lock** | Server `device_id` persisted in every server-generated event forever. Sequence-based `device_seq`. | Env var + DB fallback. PostgreSQL SEQUENCE. IDR-008. | — |
| DD-3: Alias table (materialized, eager transitive closure) | **Lock** | Schema (`subject_aliases` table). Mobile receives aliases via sync. Single-hop contract C7. | Materialized table, in-memory cache. IDR-009. | — |
| DD-4: CD intercept point (two-transaction) | **Lean** | Code-only. Interface is C3 (events always persisted) + C8 (flags created). Internals can change freely. | Same request, separate transaction. `TransactionTemplate`. IDR-010. | CD latency >100ms per push batch |
| DD-5: Identity conflict scope (manual-only) | **Lean** | Auto-detection deferred. Same CD pipeline handles all flag types. Adding auto-detection = new detection code, no schema/protocol change. | Manual creation via admin UI. IDR-011. | >50 subjects AND deployer requests auto-detection |
| Flag exclusion from PE | **Lean** | Code-only. Interface is C8. | Query-time exclusion, not physical separation. | Flag volume >10% of events |
| Sweep job (stateless re-evaluation) | **Lean** | Code-only. Deterministic flag IDs make it idempotent. | 5-min interval, trailing window. | Asymmetric flagging rate >1% |
| Admin flag badges | **Leaf** | Thymeleaf UI. No contract. | Badge count on subject list. | — |

**Skill callouts**: DD-1 required **Software Architect** review — identified a structural false-negative in the original per-batch design (events created before a mid-batch pull). Fixed with per-event `min()` approach.

---

## 4. Sub-Phases

### Phase 1a: Conflict Detector (server-side)

**Focus**: Accept-and-flag pipeline, `concurrent_state_change` detection, flag creation, PE flag exclusion. Server only.

**Prerequisites**: DD-1, DD-2, DD-4 resolved.

**Deliverables**:
1. `contracts/envelope.schema.json` updated — type enum extended with 4 new types
2. `contracts/sync-protocol.md` updated — push request includes `last_pull_watermark`
3. Server `conflict/` module:
   - `ConflictDetector` — evaluates incoming events against projected state
   - `concurrent_state_change` detection using knowledge-horizon comparison
   - Creates `conflict_detected` events with source ref, flag category, designated resolver
4. Server `sync/` module updated:
   - Push endpoint: persist events → run CD → persist flag events (two transactions)
   - Pull endpoint: update `device_sync_state` on each pull
   - Response extended: `{accepted, duplicates, flags_raised}`
5. Server Projection Engine extended:
   - Flag exclusion: flagged events visible in timeline, excluded from state derivation
   - `GET /api/subjects` reflects flag-excluded state
   - `GET /api/subjects/{id}/events` shows all events including flagged (with flag indicator)
6. Database migration V2: `device_sync_state` table, server device_seq sequence
7. Admin view updated: flag badge on flagged subjects

**Quality gates**:
- [ ] Push 2 events from different `device_id`s for same subject, with knowledge horizon before each other → `concurrent_state_change` flag raised
- [ ] Push events with knowledge horizon AFTER the other device's events → no flag raised (not concurrent)
- [ ] Flagged event appears in subject timeline but NOT in projected state (subject list shows state without flagged event)
- [ ] Push → CD failure (simulated) → events still persisted (C3), no flag, no crash
- [ ] Server-generated `conflict_detected` event has valid 11-field envelope (validates against schema)
- [ ] Push same events twice → idempotent (no duplicate flags)
- [ ] Contract test C3: event with stale data → persisted, not rejected
- [ ] Contract test C8: anomaly → `conflict_detected` event with source ref + flag category + resolver

---

### Phase 1b: Identity Resolver

**Focus**: Alias table, merge/split events, projection re-derivation with aliases, `stale_reference` detection. Server only.

**Prerequisites**: DD-3 resolved. Phase 1a complete.

**Deliverables**:
1. Server `identity/` module:
   - Merge endpoint: `POST /api/identity/merge` (online-only, server-validated)
     - Preconditions: both subjects active, not archived (checked against `subject_lifecycle`)
     - Creates `subjects_merged` event
     - Updates `subject_lifecycle`: sets `retired_id` state to `archived`
     - Updates `subject_aliases` table with transitive closure
   - Split endpoint: `POST /api/identity/split` (online-only, server-validated)
     - Preconditions: source subject active (not already archived)
     - Creates `subject_split` event
     - Archives source, creates successor
     - Historical events remain attributed to archived source
2. Server Projection Engine extended:
   - Alias resolution: queries against retired IDs resolve to surviving IDs
   - `GET /api/subjects` shows merged subjects as one unified entry
   - `GET /api/subjects/{id}/events` includes events from all alias chains
3. Conflict Detector extended:
   - `stale_reference` detection: event references retired subject ID → flag raised
   - Detection uses raw references (2-S13) — checks alias table for the `subject_ref.id` in incoming events
4. Database migration V3: `subject_aliases` table, archive state tracking
5. Admin view: merge/split actions, alias chain visualization

**Quality gates**:
- [ ] Merge Subject A and B → alias created → `GET /api/subjects` shows one unified subject with events from both
- [ ] Merge A→B, then B→C → alias table: A→C (transitive), B→C → single-hop lookup confirmed
- [ ] Split subject (undo wrong merge) → source archived → successor created → historical events stay with archived source
- [ ] Archived subject cannot be merge target (precondition rejected)
- [ ] Archived subject cannot be split again (precondition rejected)
- [ ] Event pushed for retired subject ID → `stale_reference` flag raised
- [ ] Contract test C7: merge creates alias → transitive closure → PE re-attributes events to canonical ID
- [ ] Contract test C4: PE provides alias-resolved state to CD for anomaly evaluation

---

### Phase 1c: Mobile + Admin + Integration

**Focus**: Mobile PE extended for aliases + flags. Admin flag resolution UI. End-to-end multi-device scenario. Projection equivalence.

**Prerequisites**: Phase 1a and 1b complete.

**Deliverables**:
1. Mobile Projection Engine extended:
   - Alias table: built from `subjects_merged` events received via sync
   - SQLite `subject_aliases` table, updated during pull
   - Flag exclusion: `conflict_detected` events identify flagged source events, excluded from state
   - Work List (S1): shows merged subjects as one entry, flag badge for flagged subjects
   - Subject Detail (S2): shows all events including flagged (with visual indicator)
2. Mobile SQLite migration v2: `subject_aliases` table
3. Admin flag resolution UI:
   - Flag list: unresolved flags grouped by subject + category (IG-3)
   - Resolution interface: reviewer sees flagged event + context → creates `conflict_resolved` event
   - Resolution outcomes: accept (include in state), reject (permanent exclusion), reclassify (change subject attribution)
4. Conflict resolution flow:
   - `POST /api/conflicts/{flag_id}/resolve` — creates `conflict_resolved` event
   - Projection re-derived: previously-excluded event now included (or permanently excluded based on resolution)
5. End-to-end test: two devices capture for same subject → sync → flag raised → admin resolves → devices pull resolution → state unified
6. Projection equivalence test: same event set (with aliases + flags) → server PE and device PE produce identical output

**Quality gates**:
- [ ] Mobile receives `subjects_merged` event via sync → local alias table updated → Work List shows merged subject
- [ ] Mobile receives `conflict_detected` event via sync → flagged event has visual badge → excluded from subject state in Work List
- [ ] Admin resolves flag → `conflict_resolved` event created → flagged event now included in state → projection re-derived
- [ ] End-to-end: Device A captures offline → Device B captures offline → both sync → flag raised → admin resolves → both devices pull → unified state
- [ ] Projection equivalence: given identical ordered event set with merges + flags, server PE (Java) and device PE (Dart) produce byte-identical JSON output
- [ ] All 8 acceptance criteria (§5) pass
- [ ] Performance: projection rebuild with alias resolution + flag exclusion < 200ms/subject on reference device

---

## 5. Scope by Module

**contracts/**

- `envelope.schema.json` — **(historical)** Phase 1 extended the `type` enum with `subjects_merged`, `subject_split`, `conflict_detected`, `conflict_resolved`. [ADR-002 Addendum](../../adrs/adr-002-addendum-type-vocabulary.md) (2026-04-21) reclassifies these as shape names; Phase 3e removed them from the type enum. The canonical mapping is documented in the Addendum.
- `sync-protocol.md` — push request body extended: `{events: [...], device_id: "...", last_pull_watermark: N}`
- `flag-catalog.md` — 3 flag categories documented: `concurrent_state_change`, `stale_reference`, `identity_conflict`

**server/identity/**

- Identity Resolver: merge/split event handling, precondition validation (both active, not archived), alias table management
- Alias table: materialized `subject_aliases` table with eager transitive closure
- Merge endpoint: `POST /api/identity/merge` — online-only, server-validated
- Split endpoint: `POST /api/identity/split` — online-only, server-validated
- Server event production: fixed server `device_id`, monotonic `device_seq` (DB-backed)

**server/integrity/** (new)

- Conflict Detector: evaluates incoming events against projected state
- Detection algorithms:
  - `concurrent_state_change`: knowledge-horizon comparison (push's `last_pull_watermark` vs. subject's latest events from other devices)
  - `stale_reference`: incoming `subject_ref.id` found in alias table as retired ID
  - `identity_conflict`: manual creation via admin endpoint (no auto-detection in Phase 1)
- Flag creation: `conflict_detected` events with source_event_id, flag_category, designated_resolver
- Designated resolver assignment: for Phase 1, hardcoded coordinator actor (Phase 2 adds assignment-based resolution)

**server/core/** (extended)

- Event Store: unchanged (new event types are just events with different `type` values)
- Projection Engine extended:
  - Alias resolution: retirement lookup before subject grouping
  - Flag exclusion: identify flagged source events from `conflict_detected` events, exclude from state derivation
  - Source-chain traversal: on-demand (IG-5), not precomputed

**server/sync/** (extended)

- Push: accept `last_pull_watermark` in request body. After persisting events, run CD (separate transaction). Return `{accepted, duplicates, flags_raised}`.
- Pull: update `device_sync_state` table on each pull request. Return merge/split/flag events like normal events.

**server/admin/** (extended)

- Flag list view: unresolved flags, grouped by subject + category
- Flag resolution endpoint: `POST /api/conflicts/{flag_id}/resolve`
- Merge/split admin actions (triggering the identity endpoints)
- Subject view: shows alias chains, flag badges

**mobile/data/** (extended)

- Projection Engine: alias resolution from `subject_aliases` SQLite table. Flag exclusion from `conflict_detected` events in local store.
- Event Store: SQLite migration v2 — `subject_aliases` table
- Sync Service: sends `last_pull_watermark` in push request. Processes `subjects_merged` events during pull to update local alias table. Processes `conflict_detected` events during pull to mark flagged events.

**mobile/presentation/** (extended)

- Work List (S1): merged subjects shown as one entry. Flag badge for subjects with unresolved flags.
- Subject Detail (S2): flagged events shown with visual indicator. State section excludes flagged events.

---

## 6. Technical Specifications

### Event Type Vocabulary Extension

Phase 1 appends 4 types to the platform vocabulary. F2 governs ad-hoc type creation by implementers; these are architecture-mandated types from ADR-2.

| Type | Processing behavior | Producer | Online-only? |
|------|-------------------|----------|:------------:|
| `subjects_merged` | Alias creation in projection | Server (Identity Resolver) | Yes |
| `subject_split` | Archive source, create successor | Server (Identity Resolver) | Yes |
| `conflict_detected` | Flag source event, exclude from state | Server (Conflict Detector) | Yes |
| `conflict_resolved` | Un-flag source event, re-derive state | Server (admin action) | Yes |

All 4 are server-generated, online-only. They use the standard 11-field envelope. `shape_ref` for system events: `system/identity/v1` (merge/split) and `system/integrity/v1` (flags).

### Envelope for Phase 1 System Events

System events use the same 11-field envelope. Phase 1 specific values:

| Field | `subjects_merged` | `subject_split` | `conflict_detected` | `conflict_resolved` |
|-------|-------------------|-----------------|---------------------|---------------------|
| `id` | Server-generated UUID | Server-generated UUID | Server-generated UUID | Server-generated UUID |
| `type` | `subjects_merged` | `subject_split` | `conflict_detected` | `conflict_resolved` |
| `shape_ref` | `system/identity/v1` | `system/identity/v1` | `system/integrity/v1` | `system/integrity/v1` |
| `activity_ref` | `null` | `null` | `null` | `null` |
| `subject_ref` | `{type: "subject", id: <surviving_id>}` | `{type: "subject", id: <source_id>}` | `{type: "subject", id: <flagged_event_subject>}` | `{type: "subject", id: <flagged_event_subject>}` |
| `actor_ref` | `{type: "actor", id: <coordinator>}` | `{type: "actor", id: <coordinator>}` | `{type: "actor", id: "system"}` | `{type: "actor", id: <resolver>}` |
| `device_id` | Server `device_id` | Server `device_id` | Server `device_id` | Server `device_id` |
| `device_seq` | Server's next seq | Server's next seq | Server's next seq | Server's next seq |
| `sync_watermark` | Assigned on insert | Assigned on insert | Assigned on insert | Assigned on insert |
| `timestamp` | Server clock | Server clock | Server clock | Server clock |
| `payload` | See below | See below | See below | See below |

### Payload Schemas

**`subjects_merged` payload**:
```json
{
  "surviving_id": "uuid",
  "retired_id": "uuid",
  "reason": "string (optional)"
}
```

**`subject_split` payload**:
```json
{
  "source_id": "uuid",
  "successor_id": "uuid",
  "reason": "string (optional)"
}
```

**`conflict_detected` payload**:
```json
{
  "source_event_id": "uuid",
  "flag_category": "concurrent_state_change | stale_reference | identity_conflict",
  "resolvability": "manual_only | auto_eligible",
  "designated_resolver": {"type": "actor", "id": "uuid"},
  "reason": "string"
}
```

**`conflict_resolved` payload**:
```json
{
  "flag_event_id": "uuid",
  "source_event_id": "uuid",
  "resolution": "accepted | rejected | reclassified",
  "reclassified_subject_id": "uuid (only if resolution=reclassified)",
  "reason": "string"
}
```

### Sync Protocol Extension

**Push** — `POST /api/sync/push`

```
Request:  { events: [Event, ...], device_id: "uuid", last_pull_watermark: N }
Response: { accepted: N, duplicates: N, flags_raised: N }
```

`device_id` and `last_pull_watermark` are request metadata for concurrency detection. Not part of the event envelope. The server uses `last_pull_watermark` to determine the device's knowledge horizon.

Backward-compatible: if `last_pull_watermark` is omitted, defaults to 0 (conservative — treats all existing events as potentially unseen, may over-flag).

**Pull** — `POST /api/sync/pull` (unchanged)

Server updates `device_sync_state` on each pull (internal bookkeeping). Merge/split/flag events are returned like normal events, ordered by `sync_watermark`.

### Database Schema Additions

**Migration V2** (Phase 1a):
```sql
-- Server identity for event production
CREATE SEQUENCE server_device_seq START 1;

-- Server identity — persistent device_id for server-generated events
CREATE TABLE server_identity (
    device_id UUID PRIMARY KEY
);

-- Device sync state — operational bookkeeping (audit, monitoring).
-- Concurrency detection uses min(event.sync_watermark, request.last_pull_watermark),
-- NOT this server-tracked state.
CREATE TABLE device_sync_state (
    device_id            UUID PRIMARY KEY,
    last_pull_watermark  BIGINT NOT NULL DEFAULT 0,
    last_pull_at         TIMESTAMPTZ
);
```

**Migration V3** (Phase 1b):
```sql
-- Alias table for identity resolution
CREATE TABLE subject_aliases (
    retired_id    UUID PRIMARY KEY,
    surviving_id  UUID NOT NULL,
    merged_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_no_self_alias CHECK (retired_id != surviving_id)
);
CREATE INDEX idx_aliases_surviving ON subject_aliases (surviving_id);

-- Subject lifecycle state (for merge/split preconditions)
-- Population: starts empty. Subjects with no row are treated as 'active' (default).
-- Merge/split operations insert or update rows. The table grows as identity operations occur.
CREATE TABLE subject_lifecycle (
    subject_id  UUID PRIMARY KEY,
    state       VARCHAR(20) NOT NULL DEFAULT 'active',  -- active | archived
    archived_at TIMESTAMPTZ,
    successor_id UUID  -- only set on split (points to new subject)
);
```

### Mobile SQLite Schema Addition

**Version 2** (Phase 1c):
```sql
CREATE TABLE subject_aliases (
    retired_id   TEXT PRIMARY KEY,
    surviving_id TEXT NOT NULL
);
```

No `subject_lifecycle` table needed on device — lifecycle is inferred from events in local store.

---

## 7. IG Decisions Scheduled

| IG# | Item | Approach direction | Sub-phase |
|-----|------|-------------------|-----------|
| IG-3 | Batch resolution and flag grouping | Individual `conflict_resolved` events in store. Admin UI groups by subject + flag category for reviewer convenience. | 1c |
| IG-5 | Source-chain traversal depth | On-demand (lazy) computation. No precomputed downstream graph. DAG max path 2 for trigger chains (Phase 4); organic chains measured before limiting. | 1b |

---

## 8. Acceptance Criteria

1. **Concurrent capture flagged**: Device A and Device B both create events for Subject X concurrently (neither saw the other's events) → both accepted → `concurrent_state_change` flag raised for the later-arriving event.
2. **Flag excludes from state**: Flagged event is visible in subject timeline but excluded from projected state (subject list shows state without the flagged event's contribution).
3. **Flag resolved, state re-derived**: Resolver creates `conflict_resolved` event → flagged event now included in projected state → projection reflects the unified data.
4. **Subject merge**: `subjects_merged` event → alias created → projection shows unified subject with events from both original IDs.
5. **Subject split**: `subject_split` event → source archived → successor created → historical events remain attributed to archived source → new events go to successor.
6. **Transitive closure**: Merge A→B, then B→C → alias table: A→C, B→C → single-hop lookup for both.
7. **Stale reference detection**: Event pushed with `subject_ref.id` that is a retired alias → `stale_reference` flag raised.
8. **Projection equivalence**: Server PE (Java) and device PE (Dart), given the same ordered event set including merges and flags, produce byte-identical JSON projection output.

---

## 9. Milestones

Implementation decisions and discoveries are in [`docs/decisions/`](../../decisions/) (idr-007 through idr-012). Design decision specs (DD-1 through DD-5) remain in §2 above.

---

### Pre-implementation

**2026-04-18** — Design decisions DD-1 through DD-5 settled. Agent reviews (Software Architect, Backend Architect, Database Optimizer) completed with fixes applied. See IDRs: idr-007 through idr-011.

---

### Phase 1a: Conflict Detector

**[2026-04-18] MILESTONE: Phase 1a complete — all 8 quality gates pass**

19/19 integration tests green (8 Phase 0 preserved + 3 subject + 8 Phase 1a quality gates).

**Deliverables shipped:**
- `contracts/envelope.schema.json` — type enum extended with 4 new types
- `contracts/sync-protocol.md` — push request extended (`device_id`, `last_pull_watermark`), response extended (`flags_raised`)
- Migration V2: `server_identity` table, `server_device_seq` SEQUENCE, `device_sync_state` table
- `server/identity/ServerIdentity` — env var primary, DB fallback, SEQUENCE-backed `device_seq`
- `server/integrity/ConflictDetector` — per-event `W_effective = min(event.sync_watermark, last_pull_watermark)`, deterministic UUID flag IDs
- `server/integrity/ConflictSweepJob` — `@Scheduled` stateless 5-min sweep, trailing-window re-evaluation
- `server/sync/SyncController` — two-Tx pipeline (`TransactionTemplate`: Tx1 persist → Tx2 CD + flags)
- `server/subject/SubjectProjection` — CTE-based flag exclusion (flagged events excluded from state, visible in timeline)
- `server/event/EventRepository` — `ON CONFLICT (id) DO NOTHING`, `getSyncWatermark()`, `hasNewerEventsFromOtherDevices()`

**Quality gates verified:**
- [x] QG1: Concurrent events from different devices → `concurrent_state_change` flag raised
- [x] QG2: Knowledge horizon after other events → no flag
- [x] QG3: Flagged event in timeline, excluded from projected state
- [x] QG4: Events always persisted regardless of CD outcome (C3)
- [x] QG5: Server-generated `conflict_detected` event has valid 11-field envelope
- [x] QG6: Duplicate push → no duplicate flags (deterministic IDs)
- [x] QG7: Contract C3 — stale event persisted, not rejected
- [x] QG8: Contract C8 — `conflict_detected` has source_event_id + flag_category + designated_resolver

**Bugs fixed during build:**
1. PostgreSQL transaction abort on `DuplicateKeyException` inside `TransactionTemplate` — switched `INSERT` to `ON CONFLICT (id) DO NOTHING` for transaction-safe deduplication.
2. Ungrouped column in CTE correlated subquery (`SubjectProjection`) — split into separate CTE + join.

---

### Phase 1b: Identity Resolver

**[2026-04-18] MILESTONE: Phase 1b complete — all 8 quality gates pass**

27/27 integration tests green (19 Phase 0+1a preserved + 8 Phase 1b quality gates).

**Deliverables shipped:**
- Migration V3: `subject_aliases` table (retired_id UUID PK, surviving_id UUID NOT NULL, merged_at TIMESTAMPTZ, CHECK retired_id != surviving_id), `idx_aliases_surviving` index, `subject_lifecycle` table (subject_id UUID PK, state VARCHAR(20) DEFAULT 'active', archived_at TIMESTAMPTZ, successor_id UUID)
- `server/identity/AliasCache` — `ConcurrentHashMap<UUID, UUID>` loaded at `@PostConstruct`, refreshed after each merge via `refresh()`. Single-hop resolve + isRetired check.
- `server/identity/IdentityService` — full DD-3 merge procedure: eager-insert lifecycle rows → SELECT FOR UPDATE (ordered by subject_id to prevent deadlocks) → check both active → cascade existing aliases (transitive closure) → insert new alias (ON CONFLICT DO NOTHING) → archive retired → insert `subjects_merged` event → commit → refresh alias cache. Split procedure: precondition check → archive source with successor_id → insert `subject_split` event.
- `server/identity/IdentityController` — `POST /api/identity/merge` and `POST /api/identity/split` REST endpoints with precondition validation.
- `server/integrity/ConflictDetector` extended — `stale_reference` detection: checks `AliasCache.isRetired()` for incoming event's `subject_ref.id` before concurrent_state_change check. Produces `conflict_detected` events with `flag_category=stale_reference`. `buildFlagEvent()` parameterized by flag category and reason.
- `server/subject/SubjectProjection` extended — alias resolution via LEFT JOIN `subject_aliases` in CTE. `COALESCE(sa.surviving_id, e.subject_ref->>'id')` as `canonical_subject_id`. Merged subjects appear as one unified entry.
- `server/subject/SubjectController` extended — `GET /api/subjects/{id}/events` resolves through alias cache, includes events from all retired IDs in the alias chain, sorted by sync_watermark.

**Quality gates verified:**
- [x] QG1: Merge A and B → alias created → `GET /api/subjects` shows one unified subject with events from both (event_count=3)
- [x] QG2: Merge A→B, then B→C → alias table: A→C (transitive), B→C → single-hop lookup confirmed
- [x] QG3: Split subject → source archived → successor created → historical events stay with archived source
- [x] QG4: Archived subject cannot be merge target (precondition rejected with 400)
- [x] QG5: Archived subject cannot be split again (precondition rejected with 400)
- [x] QG6: Event pushed for retired subject ID → `stale_reference` flag raised
- [x] QG7: Contract C7 — merge creates alias → transitive closure → PE re-attributes events to canonical ID
- [x] QG8: Contract C4 — PE provides alias-resolved state to CD for anomaly evaluation

---

### Phase 1c: Mobile + Admin + Integration

**[2026-04-18] MILESTONE: Phase 1c server-side conflict resolution — complete**

36/36 integration tests green (27 Phase 0+1a+1b preserved + 9 Phase 1c conflict resolution tests).

**Deliverables shipped (server-side resolution slice):**
- `server/integrity/ConflictResolutionService` — Resolves conflict flags with three outcomes: `accepted` (event re-included in state), `rejected` (event permanently excluded), `reclassified` (event re-attributed to different subject). Manual `identity_conflict` flag creation (DD-5). Precondition checks: flag must exist, must be `conflict_detected` type, must not already be resolved. Lists unresolved flags.
- `server/integrity/ConflictController` — `POST /api/conflicts/{flag_id}/resolve` (resolution endpoint), `GET /api/conflicts` (list unresolved flags), `POST /api/conflicts/identity` (manual identity_conflict creation).
- `server/event/EventRepository` — added `findById(UUID)` method.
- `server/subject/SubjectProjection` — CTE refined: `flagged_event_ids` now only un-flags events resolved with `accepted` or `reclassified` (not `rejected`). This ensures rejected events stay permanently excluded from state derivation.

**Quality gates verified:**
- [x] QG3 (server slice): Admin resolves flag → `conflict_resolved` event created → flagged event re-included in state → projection re-derived
- [x] Rejected resolution → event stays permanently excluded from projection
- [x] Reclassified resolution → `conflict_resolved` payload contains `reclassified_subject_id`
- [x] `conflict_resolved` event has valid 11-field envelope with correct shape_ref, actor_ref, subject_ref
- [x] Double-resolve → 400 (idempotency guard)
- [x] Non-existent flag → 400
- [x] List flags shows unresolved, hides resolved
- [x] Manual identity_conflict creation → flag created → event excluded from projection
- [x] Duplicate manual identity_conflict → 400 (deterministic ID)

**Remaining Phase 1c deliverables** (not yet started):
- Mobile PE extensions for aliases + flags
- Admin flag resolution UI (Thymeleaf)
- End-to-end multi-device scenario test
- Projection equivalence test (server PE vs mobile PE)

**[2026-04-18] MILESTONE: Admin flag resolution UI — complete**

42/42 integration tests green (36 prior + 6 new admin flag UI tests).

**Deliverables shipped (admin UI slice):**
- `server/admin/AdminController` — Extended with flag-related endpoints: `GET /admin/flags` (flag list), `GET /admin/flags/{flagId}` (flag detail + source event), `POST /admin/flags/{flagId}/resolve` (form submission → redirect). Subject list now includes `flagCount` and `flaggedSubjects` model attributes. Subject detail includes `flaggedEventIds`.
- `server/integrity/ConflictResolutionService` — Added `getFlagDetail(UUID)` returning `FlagDetail` record (flag metadata + source event details including payload, device_id, timestamp).
- `templates/flag-list.html` — Unresolved flags table with category color coding (concurrent=yellow, stale=blue, identity=red), "All clear" when empty, flash message areas.
- `templates/flag-detail.html` — Flag details + source event details (type, ID, device, timestamp, payload), resolution form with radio group (accepted/rejected/reclassified), reclassify subject field (JS toggle), reason textarea.
- `templates/subject-list.html` — Updated: nav bar (Subjects | Flags with count badge), flag badge (!) on flagged subjects.
- `templates/subject-detail.html` — Updated: FLAGGED indicator on events present in flaggedEventIds set.

**Quality gates verified:**
- [x] Subject list shows flag count badge, flagged subjects marked
- [x] Subject detail highlights flagged events
- [x] Flag list shows unresolved flags with category color coding
- [x] Flag detail shows flag metadata + full source event details
- [x] Resolution form POST → redirect → flag list (accepted resolution)
- [x] Non-existent flag → redirect to flag list

**Remaining Phase 1c deliverables** (not yet started):
- Mobile PE extensions for aliases + flags
- End-to-end multi-device scenario test
- Projection equivalence test (server PE vs mobile PE)

**[2026-04-18] MILESTONE: Mobile PE extensions (aliases + flags) — complete**

61 total tests green (42 server + 19 mobile: 10 prior + 9 new projection engine tests).

**Deliverables shipped (mobile PE extensions):**
- `mobile/data/event_store.dart` — DB migration v2: `subject_aliases` table (retired_id PK, surviving_id, merged_at). `_onCreate` builds both tables for fresh installs. `_onUpgrade` adds alias table for v1→v2 upgrades. New methods: `upsertAlias()` (with eager transitive closure — updates existing aliases pointing to retired ID), `getAllAliases()`, `close()`. Constructor accepts optional `dbPath` for test injection.
- `mobile/data/sync_service.dart` — Now takes `DeviceIdentity`. Push sends `last_pull_watermark` + `device_id` (DD-1 compliance). Pull processes `subjects_merged` events to update local alias table via `upsertAlias()`.
- `mobile/data/projection_engine.dart` — Full rewrite for Phase 1:
  - `getSubjectList()`: loads aliases, builds flagged event set from `conflict_detected` events, un-flags events resolved with `accepted`/`reclassified`, resolves subject IDs through alias table, filters system event types from grouping, excludes flagged events from state derivation (captureCount, name), counts unresolved flags per subject.
  - `getSubjectDetail()`: fetches events for surviving ID + all retired aliases, deduplicates, sorts DESC.
  - `getFlaggedEventIds()`: returns set of source_event_ids for unresolved flags (used by UI for indicators).
  - `_isSystemEventType()`: filters `conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`.
- `mobile/domain/subject_summary.dart` — Added `flagCount` field (default 0, backward-compatible).
- `mobile/presentation/screens/work_list_screen.dart` — Flag badge (red pill) on subjects with `flagCount > 0`.
- `mobile/presentation/screens/subject_detail_screen.dart` — Loads `flaggedEventIds`, passes `isFlagged` to `_EventTile`. Flagged events show red icon tint + "FLAGGED" badge.

**Quality gates verified (mobile):**
- [x] Alias resolution: merged subjects shown as one entry in subject list (test)
- [x] Transitive closure: A→B→C resolves to C in single hop (test)
- [x] Flag exclusion: flagged events excluded from state derivation (captureCount, name) (test)
- [x] Flag resolution accepted: event re-included in state (test)
- [x] Flag resolution rejected: event stays excluded (test)
- [x] getFlaggedEventIds: returns only unresolved flags (test)
- [x] System events excluded from subject grouping (test)
- [x] Subject detail includes events from retired aliases (test)
- [x] All 19 mobile tests green, all 42 server tests green

**Note**: sqflite in-memory path sharing trap — see [idr-012](../../decisions/idr-012-sqflite-memory-path.md).

**Remaining Phase 1c deliverables** (not yet started):
- End-to-end multi-device scenario test
- Projection equivalence test (server PE vs mobile PE)

**[2026-04-18] MILESTONE: Batch 3 — E7 projection equivalence + E2E multi-device — complete**

64 total tests green (44 server + 20 mobile).

**Deliverables shipped:**
- `contracts/fixtures/projection-equivalence.json` — Shared fixture exercising: basic capture, flag exclusion, flag resolution (accepted → re-included), alias merge, system event filtering. 7 events + 1 alias → 2 expected subjects in canonical format `{subject_id, event_count, flag_count, latest_timestamp}`.
- `server/subject/SubjectSummary` — Added `flag_count` field (was missing from server PE output).
- `server/subject/SubjectProjection` — CTE extended with `unresolved_flags` sub-CTE to compute `flag_count` per subject. Both PEs now produce equivalent canonical shape.
- `server/projection/ProjectionEquivalenceTest` — Loads shared fixture, inserts events + aliases into DB, runs server PE, compares to expected output field-by-field (including timestamp instant comparison).
- `mobile/test/projection_equivalence_test.dart` — Loads same shared fixture from contracts/, inserts events + aliases into sqflite, runs Dart PE, compares to expected output.
- `server/e2e/MultiDeviceE2ETest` — Full lifecycle test: Device A pushes 2 captures → Device B pushes 1 capture (concurrent, `last_pull_watermark=0`) → conflict auto-detected → PE shows 1 flag, 2 events → admin resolves as accepted → PE shows 0 flags, 3 events → pull returns all 5 events (3 domain + 2 system).

**Quality gates verified:**
- [x] E7: Server PE and Dart PE produce identical canonical output from same ordered event set
- [x] E2E: Push → auto-detect conflict → resolve → projection re-derives correctly
- [x] E2E: Pull after resolution returns all events including system events
- [x] `flag_count` now exposed in server PE (was missing before this batch)

**Phase 1c is COMPLETE. All deliverables shipped.**
