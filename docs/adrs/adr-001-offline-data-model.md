# ADR-001: Offline Data Model

> Status: **Decided**
> Date: 2026-04-10
> Exploration: [Reading Guide](../exploration/guide-adr-001.md) · Raw: [02](../exploration/archive/02-adr1-offline-data-model.md), [03](../exploration/archive/03-adr1-forward-projection.md), [04](../exploration/archive/04-decision-audit.md)

---

## Context

The platform's storage primitive — how records are created, stored on-device, and synced — is the root of the architectural dependency tree. Every subsequent decision (identity, authorization, configuration, workflow) is constrained by it. It is the least reversible choice: changing the storage model after deployment requires data migration across thousands of devices at unpredictable connectivity.

The exploration evaluated three options (immutable snapshots, immutable events, unified action log) against all Phase 1 scenarios, five system commitments, and four downstream ADRs. The forward projection revealed that events hold the safest irreversibility position — they can retreat toward an action-log model cheaply (add materialized views) but the reverse path is risky (dual-write gaps compromise log completeness).

---

## Decision

The platform uses **immutable events** as its foundational storage primitive. Five sub-decisions follow:

### S1: Record Mutability

**All writes are append-only.** Once written, a record is never modified or deleted. Corrections, reviews, status changes, and amendments produce new records that reference earlier ones.

Forced by: V3 (Trustworthy records), P3 (Records are append-only), S00 edge case (traceable corrections).

### S2: Write Granularity

**The atomic unit of a write is a typed, immutable event.** Each event records what happened — a capture, a correction, a review, a transfer — with only the action-specific payload. Current state is a projection computed from the event stream for a given subject.

Projections (materialized views of current state) are maintained for read performance. **Projections are always rebuildable from the event stream.** If a projection diverges from the events, the events win — the projection is discarded and recomputed. What subset of events a device holds — and therefore what it can rebuild locally — is determined by ADR-3 (sync scope). The event log is the single source of truth.

**Write-path discipline**: Every state change in the system enters through the event store. Projections and views are derived — maintained eagerly or lazily, but never written to independently. If a projection is corrupt or stale, the fix is rebuild from events, never direct patch. This discipline is what makes the event log the single source of truth in practice, not just in theory. It is also what keeps the escape hatch (adding application-maintained views if projection complexity proves excessive) viable: the escape hatch works only if the event log is gap-free from day one.

### S3: Identity Generation

**All identifiers (for events, subjects, and records) are client-generated UUIDs.** Devices mint identifiers independently without requiring a network roundtrip. Duplicate real-world subjects receiving separate UUIDs is a domain-layer concern resolved in ADR-2, not a storage concern.

Forced by: V1 (Works without connectivity), P1 (Offline is the default).

### S4: Sync Unit

**The sync unit is the immutable event.** Sync transfers events that the receiving party has not yet seen, filtered to the receiver's assigned scope (determined in ADR-3). Sync is:

- **Idempotent** — receiving the same event twice is a no-op
- **Append-only** — the server never instructs a device to delete or modify an event
- **Order-independent** — events carry their own timestamps and references; arrival order does not determine logical order

### S5: Event Envelope Guarantees

Every event carries a common envelope. ADR-1 commits to **what the envelope must express**, not the specific field schema — because downstream ADRs (identity, authorization, configuration) will shape the exact representation.

**Decided here — these are properties of the storage primitive:**

- **Identity**: every event has a globally unique, client-generated UUID
- **Type**: every event declares what kind of action it represents
- **Payload**: every event carries an action-specific data payload
- **Timestamp**: every event records when the action was actually performed (device time, not sync time)

**Required but shaped by downstream ADRs:**

- **Subject association**: events relate to subjects, but how subject identity works (single ID, composite key, multi-subject events) is ADR-2
- **Event references**: events can reference prior events (corrections, reviews), but what reference structures exist is shaped by ADR-2 (conflict) and ADR-5 (workflow)
- **Schema versioning**: every event is tagged with the data shape version active when it was created, but the versioning scheme is shaped by ADR-4 (configuration)
- **Authorship**: every event records who performed the action, but the user identity model is not yet decided
- **Authority context**: every event records the authority under which it was performed (role, scope, or equivalent), but the authorization model is ADR-3 — the envelope must carry whatever ADR-3 requires, without pre-deciding its shape

**Envelope extensibility**: the envelope is designed to be extended by future ADRs (e.g. causal ordering metadata from ADR-2, authorization context from ADR-3). Existing events remain valid when new envelope fields are introduced.

---

## What This Does NOT Decide

| Concern | Belongs to | Why deferred |
|---------|-----------|--------------|
| Causal ordering mechanism (HLC, vector clocks, etc.) | ADR-2 | Tightly coupled with conflict detection semantics |
| What constitutes a "conflict" between concurrent events | ADR-2 | Requires identity model to define "same subject" |
| What data flows to which device (sync scope) | ADR-3 | Requires authorization model |
| Event type vocabulary (which types the platform defines) | ADR-4 | Configuration paradigm determines what is fixed platform vocabulary vs. configurable |
| Projection rules (how events compose into current state) | ADR-4 / ADR-5 | Depends on configuration boundary and workflow model |
| Where projections live (device, server, or both) | ADR-3 | Sync and device strategy |
| Sync topology (one-tier, two-tier, summary sync) | ADR-3 | Requires authorization model |
| Whether event types are platform-fixed or deployment-configurable | ADR-4 | Configuration boundary question |
| Activity context / correlation metadata in events | ADR-4 | Requires the activity model |

---

## Consequences

### What is now constrained

- **ADR-2 (Identity & Conflict)**: Subject identity is UUID-based. Conflict detection operates on concurrent events within a subject's stream. Identity merge is stream-linking (a `subjects_merged` event), not content-rewriting.
- **ADR-3 (Auth & Sync)**: Sync transfers immutable events filtered by scope. The event model enables differentiated sync strategies (e.g., full event streams vs. pre-computed projections). ADR-3 decides the sync topology.
- **ADR-4 (Configuration)**: Configuration defines data shapes per event type and selects which event types an activity uses. The boundary between platform-defined event types and deployment-configurable types is ADR-4's decision.
- **ADR-5 (Workflow)**: The event model makes state-as-projection possible: state transitions can be events, and state machines can emerge from projection rules. The specific state progression model — including whether data and workflow events are separate types — is ADR-5's decision.

### Risks accepted

- **Projection complexity on low-end Android.** The projection layer must handle events from multiple schema versions, out-of-order arrival, and be fast enough for every screen load. If this proves too complex, the escape hatch is adding application-maintained views (moving toward the action-log model) without losing the event store. This escape hatch is viable only because the write-path discipline (S2) guarantees the event log is gap-free.
- **Developer paradigm shift.** Event-sourcing is less familiar than mutable-state programming. The team must reason about "what happened" rather than "what is the current state." Mitigation: projections provide a familiar current-state read path; only the write path is event-native.
- **Event schema versioning.** Immutable events cannot be migrated. The projection logic must understand events from every schema version ever produced. Over years, this accumulates payload diversity. Mitigation: the envelope guarantees are stable; only the action-specific payload varies by type and version.

### Principles confirmed

- **P1 (Offline is the default)**: Events are created locally, synced when connectivity returns. No operation requires a network roundtrip.
- **P3 (Records are append-only)**: Structural — events are immutable by definition.
- **P5 (Conflict is surfaced)**: Concurrent events on the same subject are structurally visible in the event stream. Resolution produces its own event, preserving the conflict and the decision.
- **P7 (Simplest scenario stays simple)**: S00 (basic capture) is one event with a data payload. No projections needed for standalone captures. The model does not impose unnecessary complexity on the simple case.

---

## Next Decision

**ADR-2: Identity Model and Conflict Resolution.**

ADR-1 established that subjects are identified by client-generated UUIDs and that conflicts are concurrent events on the same subject's stream. ADR-2 must now decide:

1. How duplicate real-world subjects (same thing, two UUIDs) are detected and merged
2. What causal ordering mechanism is used to distinguish concurrent from sequential events
3. What constitutes a conflict vs. independent additive work
4. What resolution options the platform offers (keep-both, pick-one, supersede, require-review)

Inputs: ADR-001 (this decision), S01 "what makes this hard" (duplicate identities, ambiguous identity), S06 "what makes this hard" (deactivated subjects still referenced), S19 "what makes this hard" (offline conflicts, ordering), P5 (Conflict is surfaced, not silently resolved).
