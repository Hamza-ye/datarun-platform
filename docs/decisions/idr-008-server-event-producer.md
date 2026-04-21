---
id: idr-008
title: Server as event producer (DD-2)
status: active
date: 2026-04-18
phase: 1a
type: decision
touches: [server/identity, server/integrity]
commit: 3d41bcd
tags: [identity, server, dd]
---

# Server as event producer (DD-2)

## Context

Merge, split, conflict detection, and resolution events are server-generated. The envelope requires `device_id` and `device_seq`. The server needs a stable event-producer identity.

## Decision

- Persistent `device_id` UUID: env var `SERVER_DEVICE_ID` primary, DB-stored fallback (`server_identity` single-row table, auto-generated on first boot)
- Monotonic `device_seq`: PostgreSQL SEQUENCE (`nextval('server_device_seq')`). Gaps from rollback are harmless — [2-S1] requires monotonic, not gapless
- `actor_ref`: `{type: "actor", id: "system"}` for auto-generated events, coordinator UUID for human-triggered
- `ServerIdentity` bean initialized at startup before push processing

## Consequences

- Mobile receives server events via normal pull — no special handling
- Concurrent `nextval()` is safe (unique, monotonic across sessions)
- Env-var-with-DB-fallback suits Docker deployment

## Traces

- Constraint: [2-S1], [2-S4], [2-S5]
- Files: ServerIdentity, server_identity table, server_device_seq sequence
