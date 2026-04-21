---
id: idr-015
title: Scope-filtered sync — denormalized location_path on events (DD-3)
status: active
date: 2026-04-19
phase: 2a
type: decision
touches: [server/sync, server/authorization]
superseded-by: ~
evolves: ~
commit: ~
tags: [sync, authorization, performance, database, dd]
---

# Scope-filtered sync — denormalized location_path on events (DD-3)

## Context

Every pull request must filter events by actor scope. The server must: identify actor → compute active assignments → build scope predicate → filter events by watermark order. For supervisors with broad geographic scope, this could touch thousands of events. The query strategy has permanent performance implications.

## Decision

**Denormalized `location_path` column on the events table**, resolved at write time from the event's `subject_ref` via `subject_locations`. Combined with a small CTE for actor-assignment reconstruction.

### Why this works

ADR-3 S4 (alias-respects-original-scope) means the scope evaluation for an event is permanently fixed to the original `subject_ref` at write time. The denormalized `location_path` is **never stale for existing events**. This eliminates the fundamental weakness of denormalization (stale data) while preserving its strength (fastest reads).

### Schema

```sql
ALTER TABLE events ADD COLUMN location_path TEXT;

-- Primary pull index: watermark-ordered with covering columns
CREATE INDEX idx_events_scoped_pull
    ON events (sync_watermark)
    INCLUDE (location_path, type, activity_ref)
    WHERE location_path IS NOT NULL;

-- Assignment event lookup by target actor
CREATE INDEX idx_events_assignment_actor
    ON events ((payload->'target_actor'->>'id'))
    WHERE type = 'assignment_changed';
```

`location_path` is infrastructure metadata with the same status as `sync_watermark` — server-managed, not in the event envelope, invisible to the API contract.

### Write path

At event persist time, resolve `location_path` from `subject_locations` (PK lookup, sub-millisecond):

- `subject_ref_type = 'subject'` → look up `subject_locations.path` by `subject_ref_id` → set `location_path`
- All other `subject_ref_type` values → `location_path = NULL`

Events with `NULL location_path` (assignments, system events without subject locations) are handled by separate query categories.

### Pull query structure

Three categories OR'd together:

1. **Subject events matching scope**: `location_path LIKE :scope_path || '%'` AND activity/subject_list filters. PostgreSQL scans the watermark index forward, evaluating the LIKE prefix per row. With the covering index, this avoids heap fetches during filtering.

2. **Own assignment events** (sync rule E9): `type = 'assignment_changed' AND payload->'target_actor'->>'id' = :actor_id`. Always included regardless of geographic scope — the device PE needs them to know its own scope.

3. **System events in scope**: `shape_ref LIKE 'conflict_detected/%' OR shape_ref LIKE 'conflict_resolved/%' OR shape_ref LIKE 'subjects_merged/%' OR shape_ref LIKE 'subject_split/%'` with geographic filter on `location_path`. (Phase 3e migration: pre-3e this filter keyed on `type`, which was architecturally wrong — those four strings are shape names, not envelope types. See [ADR-002 Addendum](../adrs/adr-002-addendum-type-vocabulary.md). If EXPLAIN ANALYZE shows regression vs. the pre-3e `type IN (...)` path, add a prefix or partial index on `shape_ref`.)

The actor's active assignments are reconstructed via a small CTE (typically 1-10 assignment events per actor). Assignment identity uses `subject_ref_id` on the event (per IDR-013: no `assignment_id` in payload).

### Key corrections from agent output

- Use `subject_ref_id` (UUID column) for subject matching, not JSONB path `subject_ref->>'id'`
- Use `subject_ref_id` for assignment identity (per IDR-013), not `payload->>'assignment_id'`
- `text_pattern_ops` on path indexes for btree prefix matching with `LIKE`
- No phased CTE→denormalization migration — start with `location_path` from Phase 2a

### Performance at target scale

| Scenario | Est. query time |
|----------|----------------|
| Field worker, incremental sync | <5ms |
| Supervisor, 1000 subjects, page of 100 | 5-15ms |
| Coordinator, 5000 subjects, page of 100 | 3-10ms |
| Actor with no assignments | <1ms |

### Known behaviors

- Events written before a subject has a location get `location_path = NULL` and are invisible to geographic scopes. A backfill query can update them when the subject gets a location — this is a controlled admin operation.
- Subject-list-only scopes (no geographic) match events regardless of `location_path` value, including NULL.

## Alternatives Rejected

- **CTE-only (Option A)** — recomputes full scope chain on every pull. Viable for small scopes but wasteful at coordinator scale (repeated work at the wrong time). Acceptable as initial implementation approach but not the target.
- **Materialized scope cache (Option B)** — stale cache = security breach. A stale `actor_scope_cache` could serve events the actor shouldn't see, violating ADR-3 S2. Cache invalidation triggers (assignment change, subject location change, new subject) make this fragile for a security-critical path.

## Consequences

- Write path has one additional PK lookup per event (sub-millisecond, negligible vs. persist + CD)
- Pull query is watermark-ordered with per-row scope filter — PostgreSQL stops at page_size matches, never scans full table
- No cache invalidation logic — always-fresh by construction
- Migration path if performance limits hit: covering composite index → pre-computed active assignments table → partitioning by watermark range

## Traces

- ADR: adr-003 (S2 sync scope, S4 alias-respects-original-scope), adr-001 (S4 watermark-ordered sync)
- Constraint: IG-4 (sync query), execution-plan §5 (performance targets)
- Files: server migration V4, EventRepository, SyncService
