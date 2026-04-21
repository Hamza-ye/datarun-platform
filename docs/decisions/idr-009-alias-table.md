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

Subject merge needs an alias table for identity resolution. Contract C7 demands single-hop lookup (retired_id → surviving_id in one query, no chain-chasing).

## Decision

Materialized `subject_aliases` table with eager transitive closure updated atomically within the merge transaction. In-memory `ConcurrentHashMap` cache (<100KB at scale) refreshed after each merge — zero DB round-trips on the hot path.

Merge procedure (within single transaction): eager-insert lifecycle rows → `SELECT FOR UPDATE` (ordered by subject_id to prevent deadlocks) → check both active → cascade existing aliases → insert new alias (`ON CONFLICT DO NOTHING`) → archive retired → insert `subjects_merged` event → commit → refresh cache.

Three fixes from Database Optimizer agent review:
1. **Concurrent merge race**: `SELECT ... FOR UPDATE` on lifecycle rows (without this, READ COMMITTED allows broken alias table)
2. **Non-idempotent INSERT**: `ON CONFLICT (retired_id) DO NOTHING` for replay safety
3. **Missing S9 check**: validate lifecycle state after lock, rollback if not active

## Alternatives Rejected

- **Lazy chain-chasing** — O(chain_length) per read, violates C7 (exploration doc 07 §B3)

## Consequences

- `CHECK (retired_id != surviving_id)` prevents self-alias
- Merges that share operands are serialized via row-level locks
- Acyclicity guaranteed by archived-is-terminal rule ([2-S9])

## Traces

- ADR: adr-002
- Constraint: C7, [2-S9]
- Exploration: doc 07 §B3–B5
- Files: subject_aliases table, AliasCache, IdentityService
