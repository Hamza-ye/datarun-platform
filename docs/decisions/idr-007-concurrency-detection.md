---
id: idr-007
title: Concurrency detection via watermark (DD-1)
status: active
date: 2026-04-18
phase: 1a
type: decision
touches: [server/sync, server/integrity, contracts]
commit: 3d41bcd
tags: [sync, conflict, dd]
---

# Concurrency detection via watermark (DD-1)

## Context

When Device A pushes events for Subject X, the server must detect if Device B has concurrent unacknowledged events. The envelope carries causal metadata but doesn't directly encode "what this device had seen at creation time."

## Decision

Device reports `last_pull_watermark` in push request body — protocol metadata, not envelope change (F1 safe). Detection is **per-event**: `W_effective = min(event.sync_watermark, request.last_pull_watermark)`, checking if Subject X has events from other devices with watermark > W_effective.

The per-event `min()` was a refinement from agent review (Software Architect): batch-level watermark alone creates false negatives in normal operation when a device creates events, pulls, then pushes — the batch watermark is higher than older events' creation-time horizon.

## Alternatives Rejected

- **Server-tracked per-device watermark** — edge case with failed push + pull + retry overestimates device knowledge
- **Parent event reference in payload** — couples capture logic to sync metadata, pollutes domain payload

## Consequences

- Push request shape extended (`contracts/sync-protocol.md`), not the envelope
- Asymmetric detection: first pusher may escape detection; sweep job catches gaps
- Conservative: over-flags rather than misses real conflicts
- `device_time` is NOT used for detection ([2-S3])

## Traces

- ADR: adr-002
- Constraint: [2-S3], [2-S14] (accept-and-flag)
- Exploration: doc 07 §C1–C5
