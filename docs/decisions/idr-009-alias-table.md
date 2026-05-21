---
id: idr-009
title: Alias table with eager transitive closure (DD-3)
status: active
date: 2026-04-18
phase: 1b
type: decision
touches: [server/identity, mobile/data, database]
commit: 3d41bcd
tags: [identity, database, dd]
---

# Alias table with eager transitive closure (DD-3)

## Context

Subject merge needs an alias table for identity resolution. ADR-002 S6 demands single-hop lookup (`retired_id → surviving_id`) and projection-layer alias resolution without rewriting historical events.

## Decision

Materialized `subject_aliases` projection table with eager transitive closure updated atomically within the merge transaction. The table is derived from `subjects_merged/v1` events; it is not an independent source of truth. It can be rebuilt by replaying merge events in `sync_watermark` order.

In-memory `ConcurrentHashMap` cache (<100KB at scale) is loaded from the projection table and refreshed after each merge — zero DB round-trips on the hot read path.

Merge procedure (within single transaction): acquire transaction-scoped advisory locks for both subject IDs → project lifecycle from identity events and require both operands active → build `subjects_merged/v1` event → update the alias projection with eager transitive closure → insert event with `type = capture` and `shape_ref = subjects_merged/v1` → commit → refresh cache. (`subjects_merged` is a shape name, not an envelope type — see [ADR-007](../adrs/adr-007-envelope-type-closure.md).)

Three fixes from Database Optimizer agent review:
1. **Concurrent merge race**: transaction-scoped advisory locks on the involved subject IDs serialize irreversible lineage writes.
2. **Alias projection drift**: alias rows are rebuildable from `subjects_merged/v1` and updated with eager transitive closure.
3. **Missing S9 check**: lifecycle state is projected from events after locking; rollback if either operand is not active.

## Alternatives Rejected

- **Lazy chain-chasing** — O(chain_length) per read, violates C7 (exploration doc 07 §B3)

## Consequences

- `CHECK (retired_id != surviving_id)` prevents self-alias
- Merges that share operands are serialized by subject-scoped advisory locks
- Acyclicity guaranteed by event-derived lifecycle and archived-is-terminal rule ([2-S9])
- If `subject_aliases` diverges from `subjects_merged/v1`, the event stream wins and the projection is rebuilt

## Traces

- ADR: adr-002
- Constraint: C7, [2-S9]
- Exploration: doc 07 §B3–B5
- Files: subject_aliases table, AliasCache, IdentityService
