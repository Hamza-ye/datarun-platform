# ADR-002: Identity Model and Conflict Resolution

> Status: **Decided**
> Date: 2026-04-10
> Exploration: [Reading Guide](../exploration/guide-adr-002.md) · Raw: [05](../exploration/archive/05-adr2-event-storm-identity.md) (Phase 1), [07](../exploration/archive/07-adr2-phase2-stress-test-results.md) (Phase 2), [09](../exploration/archive/09-adr2-phase3-classification-results.md) (Phase 3)
> **Addendum (2026-04-21)**: [Envelope Type Mapping for Identity and Integrity Events](adr-002-addendum-type-vocabulary.md) — binding. Read before writing or modifying any code that touches `conflict_detected`, `conflict_resolved`, `subjects_merged`, or `subject_split`.

---

## Context

ADR-001 established immutable events as the storage primitive: all writes are append-only, identifiers are client-generated UUIDs, events are the sync unit, and projections are derived from events. ADR-001 explicitly deferred four concerns to ADR-002:

1. How duplicate real-world subjects (same thing, two UUIDs) are detected and merged
2. What causal ordering mechanism distinguishes concurrent from sequential events
3. What constitutes a conflict versus independent additive work
4. What resolution options the platform offers

Identity is the primary axis for projections, assignments, and conflict detection. Corruption in the identity model cascades to every downstream system. This makes ADR-002 the second most irreversible decision after ADR-001: identity references are baked into every stored event, and the causal ordering metadata in the event envelope cannot be changed without data migration across deployed devices.

---

## Decision

The platform uses **four typed identity categories**, **alias-based identity evolution**, and **device-sequence + sync-watermark causal ordering** as its identity and conflict model. Fourteen sub-decisions follow, classified by permanence: structural constraints (S1–S9, S13–S14) lock what cannot change without data migration; strategy-protecting constraints (S10–S12) guard structural invariants through server-side logic.

### S1: Event Envelope — Causal Ordering Fields

**Every event carries `device_id` (hardware-bound UUID), `device_sequence` (monotonically increasing integer per device), and `sync_watermark` (last-known server state version at the time the event was created).**

- `device_sequence` provides total ordering within a single device's event stream.
- `sync_watermark` provides cross-device concurrency detection: two events on different devices with the same watermark are concurrent — neither causally depends on the other.
- Causal ordering is evaluated per-subject: two events on different subjects are never compared for causality.

### S2: Event Envelope — Typed Identity References

**All identity references in event envelopes carry a type discriminator and a UUID: `{type, id}`, where type is one of `subject`, `actor`, `process`, or `assignment`.**

The event storm identified four distinct identity categories across scenarios:

- **Subject**: persistent, represents a real-world entity (person, facility, location). Mergeable, splittable, has a full lifecycle.
- **Actor**: persistent, represents a person who performs actions. Multiple simultaneous assignments. Provisioned, not discovered.
- **Process**: transient, scoped to a specific workflow instance (e.g., a shipment, a review cycle). Created and completed within a bounded timeframe.
- **Assignment**: temporal, binds an actor to a scope with start/end bounds. The authority context under which work is performed.

All four share a common identity protocol: client-generated UUID, immutable after creation, referenceable across events, syncable. The differences are lifecycle-specific, not structural.

### S3: device_time Is Advisory

**`device_time` in the event envelope is for display and audit only. No projection logic, conflict detection, or protocol correctness may depend on `device_time` for ordering or structural decisions.**

- Intra-device event ordering uses `device_sequence`.
- Cross-device concurrency detection uses `sync_watermark`.
- `device_time` is recorded for human-readable audit trails and display purposes. If a `device_time` is implausible (e.g., before the device's first sync or far in the future), the event is accepted but the implausibility is visible in the projection.

### S4: Device Sequence and Sync Watermark Persistence

**`device_sequence` and `sync_watermark` MUST be persisted to durable storage on the device. They MUST survive reboots, app restarts, and crashes.**

- `device_sequence` is incremented *before* the event is written, ensuring no two events from the same device share a sequence number even under crash conditions.
- The tuple `(device_id, device_sequence)` is globally unique and never reused.
- If persistence is lost (catastrophic storage failure), the device must re-provision with a new `device_id` before creating any new events.

### S5: Device Identity Is Hardware-Bound

**`device_id` identifies a physical device (or a specific app installation on a device), not a user account. When a device is replaced, the new device gets a new `device_id`. The `device_sequence` namespace is scoped to `device_id`.**

A single actor (person) may use multiple devices. Events from different devices correctly carry the same `actor_ref` but different `device_id` values. Cross-device ordering for the same actor is best-effort, determined by sync watermarks when available.

### S6: Merge Is Alias-in-Projection, Never Re-Reference

**A `SubjectsMerged` event creates an alias mapping: `retired_id → surviving_id`. No existing event is modified.** The projection layer resolves retired-ID references to the surviving ID for read purposes. Queries for either ID return the merged subject's projected state.

- The alias mapping is itself an event and syncs to devices like any other event.
- Devices update their local alias tables upon processing the merge event.
- The alias table uses eager transitive closure: if A→B and B→C, the table is updated so A→C directly. Lookup is always single-hop.

### S7: No SubjectsUnmerged — Wrong Merges Use Corrective Split

**The platform does NOT define a `SubjectsUnmerged` event type.** An incorrect merge is corrected by splitting the surviving subject (via `SubjectSplit`), creating a new successor for the entity that was wrongly merged.

- Post-merge events that were recorded against the surviving ID remain attributed to the surviving subject by default.
- Manual re-attribution of specific post-merge events to the new successor is optional, not required.
- This eliminates the unsolvable retroactive attribution problem that `SubjectsUnmerged` would create under immutable events.

### S8: Split Freezes History; Source Is Permanently Archived

**A `SubjectSplit` event archives the source subject (terminal lifecycle state). All historical events remain attributed to the source_id. New events go to successor IDs.**

- Post-split events that reference the archived source are accepted (S14) and flagged for manual attribution to a successor.
- The source becomes an archived record with lineage pointers to its successors.
- Re-attribution of historical events to successors is not supported — this would violate immutability.

### S9: Lineage Graph Acyclicity By Construction

**The Subject Identity Resolver enforces two rules that guarantee the identity lineage graph is a directed acyclic graph (DAG):**

1. **Merge operands must be active**: A `SubjectsMerged` event MUST have both `surviving_id` and `retired_id` in 'active' lifecycle state at write time.
2. **Archived is terminal**: A `SubjectSplit` event transitions the source to 'archived'. Archived subjects cannot be targets of merge or split operations. They cannot be reactivated.

Without these rules, cyclic lineage is constructable (Phase 2 finding B4 demonstrated: split X into X1/X2, then merge X1 back into X — creating a cycle). The rules prevent this by construction: X is archived after split, and archived subjects cannot be merge targets.

### S10: SubjectsMerged and SubjectSplit Are Online-Only Operations

**`SubjectsMerged` and `SubjectSplit` commands execute only through a server-validated transaction. Offline execution is not supported.** The server verifies lifecycle state preconditions (S9) before writing the event.

- This prevents offline collision: two coordinators simultaneously performing conflicting merges or splits that would produce contradictory lineage events. In an immutable store, conflicting lineage events have no recovery path.
- Field workers and supervisors do not perform merges or splits. These are coordinator-level operations performed at regional hubs or offices with reliable connectivity (constraints.md: "Generally reliable broadband" for coordination level).
- Duplicate subject *registration* (two workers registering the same real-world entity) remains an offline operation — it is detected and merged post-sync, not prevented pre-submission.

### S11: Single-Writer Conflict Resolution

**Every `ConflictDetected` event designates exactly one resolver identity. Only a `ConflictResolved` event authored by the designated resolver is canonical.** Resolution events from other actors are accepted (immutability) but flagged as unauthorized.

- The designated resolver is determined by the conflict type and the subject's current assignment context (e.g., the assigned supervisor for that subject's scope).
- If the designated resolver is unavailable, re-assignment of the resolver is a separate administrative event, not an implicit fallback.
- This prevents meta-conflicts: without single-writer designation, two offline reviewers can independently resolve the same conflict with contradictory outcomes, producing a recursive conflict with no termination condition.

### S12: Conflict Detection Before Policy Execution

**During sync processing, the Conflict Detector evaluates all incoming events BEFORE any reactive policies execute on those events. Events flagged by the Conflict Detector do not trigger policy execution until the flag is resolved.**

- This prevents downstream cascade: if policies fire before detection, they create downstream work (assignments, reviews, allocations) referencing potentially-flagged events. Those downstream events are immutable once written, making flag resolution exponentially harder.
- The detection-before-act guarantee applies to server-side sync processing. On-device, events are written without conflict detection (the device cannot detect conflicts it doesn't know about). Detection happens on sync.

### S13: Conflict Detection Uses Raw References

**The Conflict Detector evaluates events using their original `subject_id` as written by the device. Alias resolution to surviving identities occurs only in the projection layer, after detection.**

- A flag event records the original subject reference from the incoming event, preserving the provenance that the worker referenced a specific (potentially retired) identity.
- This ensures the audit trail captures what the worker thought they were doing, not what the system later resolved it to.
- Alias resolution happens after detection, in the projection layer, for read purposes only.

### S14: Events Are Never Rejected for State Staleness

**The platform NEVER rejects a validly-structured event based on subject state staleness.** Events recorded against deactivated, merged, split, reclassified, or otherwise changed subjects are accepted and stored. State anomalies are surfaced as separate `ConflictDetected` events.

- The event store is the complete record of all field work performed, regardless of the worker's state knowledge at the time.
- This is the foundation of the accept-and-flag mechanism: every event that was physically recorded in the field is preserved. The platform's job is to surface anomalies, never to discard data.
- Structural event validation (well-formed envelope, valid event type, payload matches declared shape version) still applies. S14 addresses only state-based validation (is the subject active? is the assignment valid? is the schema current?).

---

## What This Does NOT Decide

| Concern | Belongs to | Why deferred |
|---------|-----------|--------------|
| Which specific conditions constitute domain-level conflicts (beyond structural ones) | ADR-4 | Whether "two observations on the same day" is a conflict is a deployment-configured business rule, not a platform constant. ADR-2 provides the detection mechanism; ADR-4 defines the configuration boundary for conflict rules. |
| Cascade behavior when conflict resolution invalidates downstream work | ADR-5 | S12 prevents downstream work on *flagged* events. What happens when a flag resolution retroactively invalidates work that was created on an initially-clean event is a workflow state machine concern. |
| Pending match pattern — matching, timeout, bijective constraints | ADR-5 | Events with unresolvable identity references at creation time are accepted (S14). The matching, resolution, and timeout workflows are deferred to ADR-5. |
| Pending match generality — whether it's shipment-specific or a general mechanism | ADR-4 / ADR-5 | ADR-2's accept-always protocol (S14) supports pending matches implicitly. Whether this becomes a first-class configurable pattern is a configuration boundary question. |
| Who can resolve which conflict types (authorization for conflict resolution) | ADR-3 | S11 requires a designated resolver per conflict. Who is eligible to be designated is an authorization model concern. |
| What data syncs to which device (sync scope for merge/split events) | ADR-3 | Merge and split events sync to devices like any other event. Which devices receive them is a sync scope decision. |
| Projection rebuild strategy (incremental vs. full, server-computed fallback) | Strategy | The projection rebuild approach after merges is a code-level optimization. ADR-2 constrains only that projections are derived from events (ADR-001 S2) and that the alias table drives merge resolution (S6). |
| Flag creation location (server-only vs. device-side) | Strategy | ConflictDetected events are initially server-side only. Device-side flag creation can be added later without changing event structure. |
| Auto-resolution policies for low-severity flags | Strategy | Deployments can define configurable auto-resolution rules. These produce standard ConflictResolved events and are additive — they can be introduced, modified, or removed without structural changes. |
| Batch resolution and flag grouping in the review queue | Strategy | Flag events carry structured root-cause metadata enabling batch grouping. Batch resolution writes individual ConflictResolved events but is driven by read-model grouping logic that can evolve. |

---

## Consequences

### What is now constrained

- **ADR-3 (Auth & Sync)**: Sync transfers immutable events including merge, split, and conflict events. Alias tables are maintained on-device from merge events. The authorization model must determine who can be designated as a conflict resolver (S11) and who can perform merge/split operations (S10). Sync scope determines which devices receive which identity-evolution events.

- **ADR-4 (Configuration)**: Configuration defines domain-specific conflict rules — the structural conflict types (concurrent state change, stale reference, duplicate identity, cross-lifecycle) are platform-level and always detected. Business rule violations are configurable per activity. The pending match pattern's generality is ADR-4's decision.

- **ADR-5 (Workflow)**: Workflow state machines operate within the accept-and-flag model: events are always accepted, anomalies always surfaced, resolution always explicit. Downstream cascade handling (what happens when upstream events are flagged after policies have already fired) is ADR-5's concern. The detect-before-act guarantee (S12) reduces but does not eliminate cascade scenarios.

- **Event envelope**: The envelope now carries (in addition to ADR-001 fields): `device_id`, `device_sequence`, `sync_watermark`, and typed identity references (`{type, id}`). `device_time` is present but advisory. These fields are in every event, forever — they cannot be removed or reinterpreted.

### Risks accepted

- **Concurrent state changes always require human resolution.** The platform does not implement automatic conflict resolution for state changes on the same subject. This is by design (P5). If flag volume exceeds reviewer capacity, auto-resolution policies (a strategy, not a constraint) can be introduced per deployment. Revisit trigger: flags generated per day / flags resolved per day > 3 for more than 2 consecutive weeks.

- **Cross-device ordering for the same actor is best-effort.** Events from the same person across different devices are not totally ordered unless the person syncs between device changes. This is inherent to device-sequence ordering and accepted as a design trade-off. Revisit trigger: >10% of total conflict flags traced to same-actor cross-device ordering ambiguity in a live deployment.

- **Batch merge volume on low-end devices.** A data cleanup campaign that merges hundreds of subjects in one batch creates a large projection rebuild workload on sync. The strategy (incremental rebuild with server-computed fallback) mitigates this, but the worst case (200 merges × 120 events each = 24,000 events to re-project) may exceed the sync time budget on 2G connections. Revisit trigger: sync time after a batch merge exceeds 3 minutes on the reference low-end device.

- **Post-split manual attribution does not scale.** When a subject splits and offline workers have created events against the archived source, manual attribution to successors is required. At scale, this is expensive. The frozen-history constraint (S8) accepts this cost as the trade-off for not violating immutability. Revisit trigger: attribution queue depth exceeds the operating team's capacity for more than 1 week.

### Principles confirmed

- **P1 (Offline is the default)**: Events are created locally with causal ordering metadata (device_sequence, sync_watermark). No identity resolution, conflict detection, or merge/split operation requires a network roundtrip for field workers. The complex identity operations (merge, split) are restricted to coordinators who operate with reliable connectivity.

- **P3 (Records are append-only)**: Merge, split, and conflict resolution all produce new events. No existing event is modified, re-referenced, or deleted. The alias table is a projection — derived data, not stored records.

- **P5 (Conflict is surfaced, not silently resolved)**: Every anomaly produces a `ConflictDetected` event with a designated resolver. Concurrent state changes are always surfaced. Resolution is always explicit and itself an event. The system never silently discards, overrides, or auto-resolves structural conflicts.

- **P7 (Simplest scenario stays simple)**: S00 (basic capture) is one event with a payload plus three new envelope fields (device_id, device_sequence, sync_watermark). One aggregate validates the write. One sync step. No alias resolution, no flags, no conflict handling for the happy path. The identity and conflict model adds no complexity to the simple case.

---

## Traceability

| Sub-decision | Classification | Key forcing inputs |
|---|---|---|
| S1 (causal ordering fields) | Structural constraint | Phase 1 S01/S06/S19, Phase 2 C1/C5/M8 |
| S2 (typed identity refs) | Structural constraint | Phase 1 identity taxonomy |
| S3 (device_time advisory) | Structural constraint | Phase 2 C3, constraints.md |
| S4 (sequence/watermark persistence) | Structural constraint | Phase 2 A2/A3 |
| S5 (device_id hardware-bound) | Structural constraint | Phase 2 M8 |
| S6 (merge = alias-in-projection) | Structural constraint | ADR-001 S1, Phase 2 B1–B6 |
| S7 (no unmerge) | Structural constraint | Phase 2 B6 (BREAKS) |
| S8 (split freezes history) | Structural constraint | ADR-001 S1, Phase 2 B4, Phase 1 S06 |
| S9 (lineage acyclicity) | Structural constraint | Phase 2 B4 (cycle demo), invariant #4 |
| S10 (merge/split online-only) | Strategy-protecting | Phase 2 A11, S9 preconditions |
| S11 (single-writer resolution) | Strategy-protecting | Phase 2 A2 (CRITICAL), M1 |
| S12 (detect-before-act) | Strategy-protecting | Phase 2 A5, A12 |
| S13 (raw references in detection) | Structural constraint | Phase 2 B2 |
| S14 (never reject for staleness) | Structural constraint | P3, P5, V1 |

---

## Next Decision

**ADR-3: Authorization and Sync.**

ADR-2 established that identity references are typed, causal ordering uses device-sequence + sync-watermark, conflicts are always surfaced with single-writer resolution, and merge/split are online-only operations. ADR-3 must now decide:

1. What data flows to which device (sync scope) — including how merge/split/conflict events are distributed
2. What authorization model governs who can perform which operations (field work, review, merge, split, conflict resolution)
3. What sync topology is used (one-tier, two-tier, summary sync)
4. Where projections live (device, server, or both) and how they are maintained

Inputs: ADR-001 (storage model), ADR-002 (this decision), constraints.md (connectivity tiers, user tiers, responsiveness expectations), S03 "what makes this hard" (assignment enforcement offline), S07 "what makes this hard" (multi-level distribution handoffs).
