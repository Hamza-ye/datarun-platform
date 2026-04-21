---
id: idr-014
title: Geographic hierarchy — materialized path in static locations table (DD-2)
status: active
date: 2026-04-19
phase: 2a
type: decision
touches: [server/authorization, mobile/data]
superseded-by: ~
evolves: ~
commit: ~
tags: [authorization, geography, database, dd]
---

# Geographic hierarchy — materialized path in static locations table (DD-2)

## Context

The `geographic` scope type (ADR-4 S7) requires a location hierarchy for containment tests: "is the subject's location a descendant of this actor's assigned area?" The hierarchy structure and query strategy must be decided before scope-filtered sync can work.

## Decision

**Static reference data** in a `locations` table with **materialized path** for containment queries.

```sql
CREATE TABLE locations (
    id        UUID PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    parent_id UUID REFERENCES locations(id),
    level     VARCHAR(50) NOT NULL,
    path      TEXT NOT NULL  -- e.g. '/region1/district3/site7'
);
CREATE INDEX idx_locations_path ON locations (path text_pattern_ops);
```

**Containment test**: `subject_location.path LIKE :actor_scope_path || '%'`

This is a btree prefix match — O(log N), index-backed with `text_pattern_ops`.

### Why materialized path over recursive CTE

- SQLite on mobile doesn't reliably support recursive CTEs across all target versions
- Prefix match (`LIKE 'path%'`) is simpler, faster, and works identically on both PostgreSQL and SQLite
- Path is maintained on write (admin API for location CRUD), not computed on read
- Hierarchy depth is 3-4 levels — path strings are short

### Why static reference data over event-sourced

Location hierarchies change rarely — restructuring a health district is an administrative event, not a daily operation. Event-sourcing locations would be permanent (events are immutable) for data that occasionally needs correction. A mutable reference table with admin API is simpler and sufficient.

### Subject-to-location mapping

```sql
CREATE TABLE subject_locations (
    subject_id  UUID PRIMARY KEY,
    location_id UUID NOT NULL REFERENCES locations(id),
    path        TEXT NOT NULL  -- denormalized from locations.path
);
CREATE INDEX idx_subject_locations_path ON subject_locations (path text_pattern_ops);
```

The `path` column is denormalized from `locations.path` at insert/update time for direct prefix matching without a JOIN.

## Alternatives Rejected

- **Recursive CTE at query time** — incompatible with SQLite on mobile. Slower than prefix match for repeated queries. No caching benefit.
- **Event-sourced locations** — over-engineers a rarely-changing data set. Events are permanent; location corrections would require compensating events. Not worth the complexity.
- **Config Packager artifact** — Config Packager is Phase 3. Creates a blocking dependency. Deferred mechanisms must not block earlier phases.

## Consequences

- Location CRUD is admin-only (seeded via migration + managed via admin API)
- Mobile receives locations as reference data during sync (not as events)
- Path must be recomputed if a location is reparented (cascade update on `locations` + `subject_locations`)
- Scale: hundreds to low thousands of locations, 3-4 levels. Well within btree prefix match performance.

## Traces

- ADR: adr-003 (S1 containment test), adr-004 (S7 scope types)
- Constraint: IG-1 (implementation-grade choice)
- Files: server migration V4, mobile SQLite migration V3
