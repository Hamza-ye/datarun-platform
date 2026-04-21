---
id: idr-010
title: Conflict detection — same request, separate transactions (DD-4)
status: active
date: 2026-04-18
phase: 1a
type: decision
touches: [server/sync, server/integrity]
commit: 3d41bcd
tags: [conflict, pipeline, dd]
---

# Conflict detection — same request, separate transactions (DD-4)

## Context

Where in the push pipeline does conflict detection (CD) run? CD failure must never block event persistence (C3).

## Decision

Synchronous, same request, **two separate transactions**:
- Tx1: validate → persist events → commit
- Tx2: CD evaluate → persist flag events → commit
- Return: `{accepted, duplicates, flags_raised}`

Uses `TransactionTemplate` (programmatic) — makes two-Tx boundary explicit, avoids `@Transactional` proxy pitfalls. Single Tx2 for all flags in batch.

Deterministic flag IDs: derived from `(source_event_id + flag_category)` — enables idempotent sweep with `ON CONFLICT DO NOTHING`.

Sweep job: stateless 5-min re-evaluation, trailing-window, no tracking tables. Catches Tx2 failures and asymmetric-flagging race.

## Alternatives Rejected

- **Single transaction** — CD failure rolls back event persistence, violating C3
- **Async CD** — acceptable but unnecessary complexity for Phase 1

## Consequences

- If Tx1 succeeds but Tx2 fails: events safe (C3 ✓), flags missing (swept later)
- Deterministic IDs make duplicate flags impossible

## Traces

- Constraint: C3 (accept all events), [2-S12]
- Files: SyncController, ConflictDetector, ConflictSweepJob
