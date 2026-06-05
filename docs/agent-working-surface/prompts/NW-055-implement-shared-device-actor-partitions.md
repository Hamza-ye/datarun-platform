# NW-055 Agent Prompt: Implement Shared-Device Actor Partitions

You are working in `/home/hamza/datarun-platform`.

## Goal

Implement the IDR-030 shared-device session lifecycle boundary without changing
assignment-derived scope authority.

Exit target:

```text
Mobile can switch between shared-device actor sessions using per-actor local
partitions: prior-actor data and tokens do not leak, unpushed events keep their
original actor, and normal sync/config state cannot skip or corrupt data across
actors.
```

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
4. `docs/decisions/idr-030-shared-device-session-lifecycle.md`
5. `docs/implementation/module-interfaces.md` sections `Authenticated Actor Resolver`, `Scope Resolver`, `Event Store`, and sync-related notes.
6. `contracts/sync-protocol.md`
7. `mobile/lib/data/device_identity.dart`
8. `mobile/lib/data/sync_service.dart`
9. `mobile/lib/data/event_store.dart`
10. Existing focused mobile tests for event assembly, sync, projection, config, and retention.
11. Inspect server sync code only if mobile implementation shows that `device_sync_state` is device-only and must become actor-scoped to satisfy IDR-030.

## Authority And Guardrails

- IDR-030 is the implementation authority for shared-device actor partitioning.
- IDR-027 remains the production-auth actor-binding authority.
- CDL-030 and CDL-031 keep access/sync assignment-derived.
- CDL-032 keeps authority out of the envelope.
- CDL-037 keeps local retention separate from canonical server event history.
- CDL-055 keeps scope mechanisms fixed to current platform axes.

## Required Behavior

- Exactly one active actor session at a time.
- Switching away from actor A stops A capture, advisory evaluation, push, pull,
  subject-history, and config promotion.
- A's unpushed events are pushed with A's credential when possible or sealed in
  A's partition when not possible.
- Switching to actor B uses `/api/auth/me` or a previously established B session
  whose actor id came from `/api/auth/me`.
- Local actor-scoped mutable state is per actor: events, pending push,
  projections, watermarks, subject-history cursors, active/pending config state,
  and token/session material.
- Immutable config package blobs may be shared only when they contain no
  actor-scoped data and actor-local active/pending state remains separate.

## Forbidden Work

- No new scope mechanisms.
- No IdP group, role, claim, or JWT `actor_id` authority.
- No mobile authoritative rejection of structurally valid state/policy anomalies.
- No normal live-sync watermark rewrites or broad history backfill through pull.
- No broad audit/history read APIs.
- No emergency override writes.
- No envelope fields, envelope `type` values, or assignment payload fields.
- No BAR-106 retention/expiry/decommission/recovery feature beyond what is
  required to seal actor partitions safely.

## Expected Tests

- Actor switch refresh/resume uses server-resolved actor context.
- B cannot see A local events, projections, summaries, pending events,
  subject-history cursors, config promotion state, or advisory state.
- A unpushed events remain in A's sealed partition and are absent from B's push
  batch.
- Server rejects an A-authored event pushed with B's credential.
- Normal pull watermarks and `last_pull_watermark` are actor-isolated.
- Active request headers use only the current actor's credential after switch.
- Interrupted switch recovers to A, B, or no active actor, never B with A data.
- Shared immutable config blobs do not share active/pending config state.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
feat(mobile): partition shared device actor sessions
```

## Stop And Report

Stop if implementation requires a new scope mechanism, IdP claim authority,
normal sync watermark rewrites, broad audit/history APIs, emergency override
writes, mobile authoritative rejection, or BAR-106 retention/security behavior
beyond safe partition sealing.
