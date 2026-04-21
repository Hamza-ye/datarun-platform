---
id: idr-011
title: Identity conflict detection — manual only in Phase 1 (DD-5)
status: active
date: 2026-04-18
phase: 1
type: decision
touches: [server/integrity]
commit: 0e22275
tags: [conflict, scope, dd]
---

# Identity conflict detection — manual only in Phase 1 (DD-5)

## Context

Should `identity_conflict` (duplicate subject detection) be automated in Phase 1?

## Decision

Manual-only. Coordinators flag potential duplicates via admin UI. Auto-detection deferred — requires shape definitions (ADR-4, Phase 3) and domain semantics to define "similar enough."

CD auto-detects only `concurrent_state_change` (algorithmic) and `stale_reference` (alias table lookup). `identity_conflict` is manual.

## Consequences

- Pipeline handles all 3 flag types identically — no special code paths
- Additive: auto-detection later produces the same event type through the same pipeline
- No structural change required when auto-detection is added

## Traces

- ADR: adr-002 (defers to adr-004)
