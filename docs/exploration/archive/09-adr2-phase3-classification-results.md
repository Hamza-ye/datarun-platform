> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-002**. For a navigated reading guide, see [guide-adr-002.md](../guide-adr-002.md).
# Phase 3: Classification & Boundary Judgment — ADR-2

> **Phase**: 3 — Classification & Boundary Judgment
> **Method**: Irreversibility boundary test (ADR-1 framework) applied to every Phase 2 finding
> **Author**: Product Manager
> **Date**: 2026-04-10
> **Input documents consumed**:
> 1. `docs/constraints.md`
> 2. `docs/adrs/adr-001-offline-data-model.md`
> 3. `docs/exploration/05-adr2-event-storm-identity.md`
> 4. `docs/exploration/07-adr2-phase2-stress-test-results.md`
> 5. Scenarios: S01, S06, S19, S03, S07
> **Output consumer**: ADR-2 draft

---

## The Boundary Test Applied

ADR-1 established:

> **Constraint (ADR)**: If changing it requires data migration across deployed devices, it is an irreversible constraint. Commit now.
> **Strategy**: If changing it requires only code updates (projection logic, read models, UI), it is an evolvable strategy. Defer with a clear interface.

Every item below was subjected to this test. Where I disagree with the stress test agent's severity or framing, I say so explicitly.

---

## Classification Summary Table

| Item | Bucket | One-line rationale |
|------|--------|-------------------|
| A1 | 2 | Flag creation location is a projection/policy concern; the event schema is the same regardless of who emits ConflictDetected |
| A2 | 1 | Resolution concurrency affects whether the ConflictResolved event structure needs a single-writer designation — this is event-level semantics |
| A3 | 2 | Batch resolution and flag grouping are read-model and projection concerns; root-cause metadata in flags is a payload strategy, not envelope schema |
| A4 | 2 | Flag annotation after identity change is a projection/policy behavior, not stored event structure |
| A5 | 3 | Downstream work cancellation is a workflow concern (ADR-5), not an identity/conflict structure concern |
| A6 | 2 | Backlog management (auto-resolution, escalation) is operational policy, changeable with code |
| B1 | 2 | Projection rebuild strategy is a code-level optimization; does not affect stored events |
| B2 | 1 | Whether conflict detection runs on raw subject_id before alias resolution determines what the ConflictDetected event records — this is event semantics |
| B3 | 2 | Eager vs. lazy transitive closure is a projection-layer implementation choice |
| B4 | 1 | Acyclicity enforcement rules constrain which SubjectsMerged and SubjectSplit events can be written — this is aggregate validation affecting the event store |
| B5 | 2 | Batch merge projection cost is an optimization concern; does not affect event structure |
| B6 | 1 | Replacing SubjectsUnmerged with corrective split changes the event type vocabulary — this is a schema-level commitment |
| C1 | 4 | Concurrent state changes requiring human resolution is by-design; operational volume is a deployment-scaling concern |
| C2 | 4 | Batch conflict detection is computationally feasible; downstream flag volume is covered by A3/A6 |
| C3 | 1 | Whether device_time is advisory or structural determines what the event envelope promises — this is an envelope guarantee |
| C4 | 4 | Cross-device ordering limitation is inherent to device-sequence model; accepted as a design trade-off |
| C5 | 4 | Mechanism correctly detects S19 Act 2 conflict; no action required |
| Alpha | 2 | Flag grouping in the read model is a projection concern |
| Beta | 2 | Post-split event handling via attribution queue is a read-model/workflow concern |
| Gamma | 3 | Pending match bijective constraint is a workflow/process concern (ADR-5) |
| M1 | 1 | Meta-conflict prevention requires a structural rule about ConflictResolved events — solved by the A2 constraint |
| M2 | 2 | Flag re-evaluation is a policy/projection behavior |
| M3 | 3 | Downstream cancellation cascade is an ADR-5 workflow concern |
| M4 | 2 | Post-split attribution workflow is a read-model/projection concern; the attribution event is a standard event with a payload |
| M5 | 4 | Eliminated if B6 (corrective split over unmerge) is adopted |
| M6 | 3 | Pending match timeout/escalation is a workflow concern (ADR-5) |
| M7 | 2 | Bulk operation flag grouping is a read-model concern; covered by A3 |
| M8 | 1 | Device sequence namespace identity affects event envelope interpretation — must be tied to hardware, not account |
| Assumption A1 | 2 | Flag creation location is a strategy decision (same as finding A1) |
| Assumption A2 | 1 | Device sequence persistence is required for envelope guarantee integrity — must be committed |
| Assumption A3 | 1 | Sync watermark persistence is required for staleness detection — must be committed |
| Assumption A4 | 2 | Alias table maintenance on device is a projection concern |
| Assumption A5 | 2 | Projection rebuild strategy is a code-level concern |
| Assumption A6 | 1 | Acyclicity enforcement rules — same as B4 |
| Assumption A7 | 1 | Single-writer resolution — same as A2 |
| Assumption A8 | 1 | device_time advisory status — same as C3 |
| Assumption A9 | 1 | Device identity tied to hardware — same as M8 |
| Assumption A10 | 3 | Pending match generality is a workflow/configuration concern (ADR-4/ADR-5) |
| Assumption A11 | 1 | Split successor ID generation location affects who can emit SubjectSplit — this constrains the event structure |
| Assumption A12 | 1 | Sync processing order (detect-before-act) determines whether ConflictDetected events can reference downstream work — this is a protocol-level commitment |
| Q1 | 1 | Identity granularity (shared protocol vs. separate aggregates) affects event envelope schema |
| Q2 | 1 | Merge semantics (alias table, not physical re-reference) is an immutability consequence — already decided by ADR-1, but must be stated explicitly |
| Q3 | 1 | Split semantics (historical events frozen under source) affects what SubjectSplit commits to the event store |
| Q4 | 2 | Cross-type reference mechanism (UUID pointer) is uniform by ADR-1 S3 — no new decision needed |
| Q5 | 3 | Conflict definition boundary (platform vs. configurable) is ADR-4's decision |
| Q6 | 2 | Conflict resolution location (device vs. central) is a deployment strategy, not event structure |
| Q7 | 2 | Cascading resolution handling (iterative vs. compound) is a workflow/projection concern |
| Q8 | 1 | Causal ordering mechanism choice affects event envelope fields |
| Q9 | 1 | Ordering scope (per-subject) determines what the envelope must carry |
| Q10 | 1 | Clock trust level — same as C3 (device_time advisory) |
| Q11 | 2 | Post-merge sync behavior (device receives merge event and resolves locally) is a sync implementation strategy |
| Q12 | 1 | Orphaned events accepted-and-flagged — this is a protocol commitment that events are never rejected |
| Q13 | 3 | Pending match generality is ADR-4/ADR-5's decision |

---

## Bucket 1: ADR-2 Constraints (Detail)

### A2 / M1 / Assumption A7: Conflict resolution is single-writer per conflict

A `ConflictDetected` event designates exactly one resolver. Only that resolver's `ConflictResolved` event is canonical. A second resolution from another actor is itself flagged as unauthorized.

- **Irreversibility reason**: If ConflictResolved events are written without a designated resolver field, and we later decide single-writer is needed, every existing ConflictResolved event in the store is ambiguous — the system cannot determine post-hoc which was the canonical resolution. We cannot retroactively add designation semantics to events already synced across thousands of devices.
- **Addresses findings**: A2 (meta-conflict breaks the model), M1 (no termination condition), Assumption A7, Invariant #6 (Conflict Resolver — Critical).
- **Proposed constraint language**: Every ConflictDetected event MUST designate a single resolver identity. Only a ConflictResolved event authored by the designated resolver is canonical; resolution events from other actors are accepted but flagged as unauthorized.

### B2: Conflict detection operates on raw event references before alias resolution

The Conflict Detector evaluates incoming events using their original `subject_id` as written. Alias resolution to surviving IDs happens afterward, in the projection layer only.

- **Irreversibility reason**: If ConflictDetected events are written referencing the alias-resolved (surviving) ID rather than the raw (retired) ID, the flag loses the information that the event was created against a retired identity. That provenance is baked into the stored flag event. Changing the detection order later means existing flags have a different semantic meaning than new flags, creating an inconsistent audit trail.
- **Addresses findings**: B2 (alias resolution ordering), A4 (flag context invalidation).
- **Proposed constraint language**: Conflict detection evaluates events using their original subject references as written. Alias resolution to surviving identities is a projection-layer concern that runs after conflict detection, never before.

### B4 / Assumption A6: Lineage graph acyclicity is enforced by aggregate validation rules

The Subject Identity Resolver enforces: (1) the surviving_id of a merge MUST be in 'active' state; (2) both merge operands must be 'active'; (3) the source of a split transitions to 'archived', which is a terminal state — archived subjects cannot become active, be merged into, or be split again.

- **Irreversibility reason**: If SubjectsMerged or SubjectSplit events are written without these validation gates, cyclic lineage is constructable (B4 counter-example: merge a successor back into an archived source). Once cyclic events exist in the store, no amount of projection logic can make the lineage graph acyclic — the contradiction is in the stored events themselves.
- **Addresses findings**: B4 (acyclicity enforcement), Invariant #4 (Subject Identity Resolver — High).
- **Proposed constraint language**: A SubjectsMerged event MUST have both surviving_id and retired_id in 'active' lifecycle state at write time. A SubjectSplit event transitions the source to 'archived', a terminal state: archived subjects cannot be targets of merge or split operations. These rules guarantee the lineage graph is a directed acyclic graph by construction.

### B6: No SubjectsUnmerged event type — wrong merges are corrected by corrective split

A merge that was incorrect is corrected by splitting the surviving subject, creating a new successor for the entity that should not have been merged. The `SubjectsUnmerged` event type does not exist.

- **Irreversibility reason**: If `SubjectsUnmerged` events are allowed into the store, every one creates an unsolvable attribution problem — post-merge events reference the surviving_id immutably and cannot be automatically re-attributed (B6 finding). The event type itself is structurally unsound given immutability. If we commit to supporting unmerge now and later realize it's unworkable, every `SubjectsUnmerged` event in the store is a permanent inconsistency that projections must work around forever. Better to never allow the event type.
- **Addresses findings**: B6 (unmerge structural unsoundness), Invariant #2 (Projection Engine — High), M5 (eliminated).
- **Proposed constraint language**: The platform does NOT define a SubjectsUnmerged event type. An incorrect merge is corrected by a SubjectSplit on the surviving subject, producing a new successor for the wrongly-merged entity. Post-merge events default to the surviving subject; manual re-attribution to the new successor is optional, not required.

### C3 / Assumption A8 / Q10: device_time is advisory, not structural

`device_time` in the event envelope is for display and audit. It is never used for ordering or conflict detection. Intra-device ordering uses `device_sequence`. Inter-device concurrency detection uses `sync_watermark`.

- **Irreversibility reason**: If any projection or detection logic is built treating `device_time` as structural (ordering events by it, detecting conflicts with it), and device clocks are unreliable (battery resets, time zone errors, deliberate manipulation), the resulting events are permanently misordered in the store. You cannot fix the stored `device_time` — events are immutable. The only defense is committing now that `device_time` is advisory, so no structural logic ever depends on it.
- **Addresses findings**: C3 (clock reset breaks ordering), Assumption A8, Q10 (clock trust), Invariant #10 (Causal Ordering — device_time dependency).
- **Proposed constraint language**: The event envelope field device_time is advisory: used for display and audit trail purposes only. Intra-device event ordering uses device_sequence (monotonic counter). Cross-device concurrency detection uses sync_watermark. No projection, conflict detection, or protocol logic may depend on device_time for correctness.

### M8 / Assumption A9: device_id is tied to hardware identity, not user account

Each physical device has a unique `device_id`. When a device is replaced, the new device gets a new `device_id`. The `device_sequence` namespace is scoped to `device_id` and never reused.

- **Irreversibility reason**: `device_sequence` is meaningful only within a `device_id` namespace. If `device_id` is tied to a user account, replacing the hardware resets the sequence counter under the same namespace, creating sequence number collisions in the event store. Events already stored with `{device_id: D1, device_sequence: 42}` would become ambiguous — there would be two events with the same (device_id, device_sequence) pair. This cannot be fixed after the fact.
- **Addresses findings**: M8 (device sequence reset), Assumption A9.
- **Proposed constraint language**: device_id identifies a physical device, not a user account. Each device_id has an independent, monotonically increasing device_sequence. The tuple (device_id, device_sequence) is globally unique and never reused. Device replacement produces a new device_id.

### Assumption A2: device_sequence MUST be persisted to durable storage

The device sequence counter survives reboots, app restarts, and crashes.

- **Irreversibility reason**: If the sequence counter is lost (e.g., stored in RAM only), post-restart events reuse sequence numbers. The (device_id, device_sequence) uniqueness guarantee — which is an event envelope property — is violated. Existing events with those sequence numbers are already in the store; the collision is permanent and undetectable at the server.
- **Addresses findings**: Assumption A2, C3 (what survives clock reset).
- **Proposed constraint language**: device_sequence MUST be persisted to durable storage on the device. It MUST survive reboots, app restarts, and crashes. The sequence is incremented before the event is written, ensuring no two events from the same device share a sequence number even under crash conditions.

### Assumption A3: sync_watermark MUST be persisted to durable storage

The sync watermark survives reboots, app restarts, and crashes.

- **Irreversibility reason**: If the watermark is lost, post-restart events carry no staleness context. The server cannot detect that these events were created against stale state. Every such event enters the store without the metadata needed for conflict detection. This is an envelope guarantee — once events are stored without a watermark, they're permanently undetectable for staleness.
- **Addresses findings**: Assumption A3, C3 (what survives clock reset).
- **Proposed constraint language**: sync_watermark MUST be persisted to durable storage on the device. It MUST survive reboots, app restarts, and crashes. Every event carries the sync_watermark that was current at the time of its creation.

### Assumption A11: SubjectSplit is an online-only operation

Split successor IDs are generated server-side. The `SubjectSplit` command can only be executed by a connected coordinator.

- **Irreversibility reason**: If two coordinators can split the same subject offline, they produce conflicting SubjectSplit events with different successor IDs for the same source. Both events are immutable. The event store now contains two contradictory assertions about the source's successors — a permanent structural inconsistency in the lineage graph. Unlike merge (which the alias table and conflict model handle), split collision has no recovery path that doesn't require one set of successors to be retroactively invalidated.
- **Addresses findings**: Assumption A11.
- **Proposed constraint language**: SubjectSplit is an online-only operation. The server validates that the source subject has not already been split or archived before writing the SubjectSplit event. Successor subject_ids are generated during this server-validated transaction. Offline SubjectSplit commands are not supported.

### Assumption A12: Sync processing runs conflict detection before policies fire

During sync, incoming events are evaluated by the Conflict Detector before any policies (assignment creation, review triggering, allocation) fire on those events.

- **Irreversibility reason**: If policies fire first, downstream events (assignments, reviews, allocations) are written to the event store referencing the incoming event. If that event is subsequently flagged, the downstream events are permanently in the store, referencing a flagged event. You cannot un-write them. The cascade means flag resolution now requires resolving not just the original event but every downstream event it triggered — an exponentially harder problem that is baked into the stored event graph.
- **Addresses findings**: A5 (downstream work on flagged events), Assumption A12, Invariant #5 (Conflict Detector — detect-before-act).
- **Proposed constraint language**: During sync processing, the Conflict Detector evaluates all incoming events BEFORE any reactive policies execute on those events. Events that are flagged by the Conflict Detector do not trigger policy execution until the flag is resolved.

### Q1: Four identity types, one envelope protocol

Subject, Actor, Process, and Assignment are distinct identity types with different lifecycles. All share a common identity envelope: client-generated UUID, referenceable by events, survive sync boundaries. The event envelope uses a uniform reference mechanism (typed UUID: `{type, id}`) regardless of identity type.

- **Irreversibility reason**: The event envelope must carry identity references. If we use untyped UUIDs now and need typed references later, every existing event's references are ambiguous — was `abc-123` a subject, an actor, a shipment? Adding the type discriminator after the fact requires re-interpreting every existing event's references, which is de facto data migration.
- **Addresses findings**: Q1 (identity granularity), cross-scenario identity taxonomy (4 types).
- **Proposed constraint language**: Identity references in event envelopes are typed: each reference carries both a type discriminator and a UUID. The four identity types are Subject, Actor, Process, and Assignment. All follow the same envelope protocol: client-generated UUID, immutable after creation, referenceable across events.

### Q2: Merge is alias-in-projection, never physical re-reference

When subjects merge, no existing event is modified. The projection layer maintains a `retired_id → surviving_id` alias. Queries for either ID return the unified projection.

- **Irreversibility reason**: This is a direct consequence of ADR-1 S1 (all writes are append-only) and S2 (events are immutable). Stating it explicitly in ADR-2 prevents any implementation from attempting physical re-reference, which would violate the foundational storage model. If any code path ever re-writes a `subject_id` in a stored event, the audit trail is destroyed — and the damage is permanent.
- **Addresses findings**: Q2 (merge semantics), S01 Act 4 (merge hot spot), B2-B6 (alias table findings).
- **Proposed constraint language**: SubjectsMerged creates an alias mapping (retired_id → surviving_id). No existing event is modified. The projection layer resolves retired_id references to surviving_id for read purposes. The alias mapping is itself an event and syncs to devices like any other event.

### Q3: Split freezes historical events under the source ID

When a subject splits, all historical events remain attributed to the source_id. The source becomes an archived record with lineage pointers to successors. New events go to successor IDs.

- **Irreversibility reason**: The alternative — re-attributing historical events to successors — requires modifying stored events' `subject_id` fields, violating immutability (ADR-1 S1). Committing to the freeze model now means projections and workflows are built around it. If we defer this and some implementations re-attribute while others freeze, the event store has inconsistent semantics.
- **Addresses findings**: Q3 (split semantics), S06 Act 3 (split hot spot), Scenario Beta (post-split events).
- **Proposed constraint language**: A SubjectSplit event archives the source subject and creates successor subjects. All historical events remain under the source_id. New events reference successor_ids. Post-split events that reference the archived source are accepted and flagged for manual attribution to a successor.

### Q8 / Q9: Causal ordering uses device_sequence + sync_watermark, scoped per subject

The causal ordering mechanism is: `device_sequence` for intra-device total order, `sync_watermark` for cross-device concurrency detection. Ordering is scoped per subject — two events on different subjects are never compared for causality.

- **Irreversibility reason**: The causal ordering mechanism determines what fields the event envelope MUST carry. Choosing device_sequence + sync_watermark means every event carries these two fields. Choosing HLC or vector clocks would require different (or additional) fields. Once events are stored with a particular ordering metadata schema, switching to a different mechanism requires either (a) dual-reading old and new formats forever, or (b) data migration. This is the definition of an irreversible envelope commitment.
- **Addresses findings**: Q8 (mechanism choice), Q9 (ordering scope), Phase 1 causal ordering discovery, C1-C5 (mechanism C findings — all hold).
- **Proposed constraint language**: Every event carries device_sequence (monotonic per device_id) and sync_watermark (last-known server state version at event creation time). Causal ordering is evaluated per-subject: two events on the same subject are concurrent if neither's sync_watermark reflects knowledge of the other. Cross-subject causal ordering is not tracked.

### Q12: Events against stale state are always accepted, never rejected

Events recorded against deactivated, merged, split, or otherwise changed subjects are accepted into the event store. They are immutable. The anomaly is surfaced via a separate ConflictDetected event. The platform never rejects a validly-structured event based on state staleness.

- **Irreversibility reason**: This is a core protocol commitment that every client, server, and sync implementation depends on. If we defer this and some implementations reject stale events, those events are permanently lost — the field worker's data is gone. Committing to accept-always means the event store is the complete record of everything that happened, regardless of state validity. Reversing this later (to reject-sometimes) would change the sync protocol and break the trust model.
- **Addresses findings**: Q12 (orphaned events), A1-A6 (accept-and-flag mechanism), P3 (append-only), P5 (conflict surfaced not resolved).
- **Proposed constraint language**: The platform NEVER rejects a validly-structured event based on subject state staleness. Events against deactivated, merged, split, reclassified, or otherwise changed subjects are accepted and stored. State anomalies are surfaced as separate ConflictDetected events. The event store is the complete record of all field work performed, regardless of state validity at the time of sync.

---

## Bucket 2: ADR-2 Strategies (Detail)

### A1: Flag creation is server-side initially; device-side flagging deferred

ConflictDetected events are created by the server's Conflict Detector during sync processing. Devices do not create flags in v1.

- **Reversibility reason**: The ConflictDetected event structure is the same regardless of who emits it. Adding device-side flag creation later requires only deploying updated client code that can emit the same event type. No existing events change. The deduplication concern (device flag + server flag for same anomaly) is a projection/read-model concern solvable with a deterministic hash in the event payload.
- **Fallback if wrong**: If field workers need immediate feedback about anomalies, add device-side flag creation with a deduplication hash. The flag event structure supports this without modification — add a `source: "device" | "server"` payload field and collapse duplicates in the projection.
- **Interface required**: The ConflictDetected event payload must include enough structured context that a future device-side implementation can produce equivalent flags. Specifically: the event must carry the anomaly type, the triggering condition, and the subject reference — not opaque server-internal metadata.

### A3 / M7: Flag events carry structured root-cause metadata enabling batch resolution

Flag events include structured metadata about which specific state change triggered the flag (e.g., `{trigger_type: "reclassification", trigger_event_id: "xxx", subject_id: "yyy"}`). The read model groups flags by root cause and supports batch resolution.

- **Reversibility reason**: Root-cause metadata is in the flag event's payload, not in the envelope. Payload structure can evolve per ADR-1 S5 (schema versioning). Batch resolution is a read-model feature — it writes individual ConflictResolved events but does so programmatically. All of this is changeable with code updates.
- **Fallback if wrong**: If the root-cause metadata structure proves inadequate, add new metadata fields in the next payload version. Old flags retain their original metadata. Projection logic handles both versions.
- **Interface required**: ConflictDetected event payload must include a `root_cause` object with at minimum: `{trigger_type, trigger_event_id}`. Batch resolution produces individual ConflictResolved events (one per flag) but is driven by read-model grouping logic that can evolve.

### A4 / M2: Flags are annotated when their referenced subject undergoes identity changes

When a subject is merged, split, or deactivated after a flag was created, the projection annotates the flag with the current identity state. The flag event itself is unchanged; the annotation is in the read model.

- **Reversibility reason**: Annotation is purely a projection/read-model behavior. The stored flag events are untouched. The annotation logic can be rewritten, extended, or removed without affecting stored data.
- **Fallback if wrong**: If annotation proves insufficient and flags need to be formally re-evaluated (producing new events), a "flag re-evaluation" policy can be added that emits new ConflictDetected events referencing the original flag. This is additive — no existing events change.
- **Interface required**: The flag read model must join flag events with the current identity graph, displaying both the original subject reference and the current resolved identity.

### A6: Configurable auto-resolution policies for low-severity flags

Deployments can define auto-resolution rules (e.g., "stale-reference flags for classification changes older than 30 days are auto-accepted"). These produce standard ConflictResolved events but are system-authored, not human-authored.

- **Reversibility reason**: Auto-resolution policies are configuration-driven logic that writes standard events. No structural change. Policies can be added, modified, or removed with configuration changes.
- **Fallback if wrong**: Disable auto-resolution. All flags return to manual review. Flags that were auto-resolved remain resolved (their ConflictResolved events are immutable), but future flags require manual handling.
- **Interface required**: ConflictResolved event payload must accommodate `resolved_by: {type: "policy", policy_id: "..."}` in addition to `resolved_by: {type: "human", actor_id: "..."}`. This is a payload convention, not an envelope change.

### B1 / B5: Projection rebuild after merge is incremental with server-computed fallback

Devices rebuild merged projections incrementally (append retired events, re-sort, recompute affected state). If a sync yields more merges than a configurable threshold, the device requests server-computed projections.

- **Reversibility reason**: Projection rebuild strategy is entirely a code-level concern. The stored events and merge events are identical regardless of whether rebuilds are incremental, full, or server-delegated. The strategy can be changed with a client code update.
- **Fallback if wrong**: If incremental rebuild proves unreliable, switch to full rebuild with a longer time budget. If full rebuild is too slow, increase server-computed projection use. All transitions are code-only.
- **Interface required**: The sync protocol must support an optional "server-computed projection" response alongside merge events. This is a sync protocol extension, not an event structure change — and it's deferrable to ADR-3 (sync topology).

### B3: Eager transitive closure for alias chains

When `SubjectsMerged{surviving: C, retired: B}` is processed, all existing alias entries where `target = B` are updated to `target = C`. Lookup is always O(1).

- **Reversibility reason**: The alias table is a projection — a derived data structure, not a stored event. Whether closure is eager or lazy is an implementation decision. The underlying merge events are identical. Switching from lazy to eager (or vice versa) requires rebuilding the alias table from merge events, which is a standard projection rebuild.
- **Fallback if wrong**: If eager closure proves too expensive at merge time for servers with 50,000+ merged subjects, switch to lazy chasing with a bounded depth limit (e.g., max 10 hops) and a background job that eagerly closes chains during off-peak hours.
- **Interface required**: The alias table abstraction must expose a single `resolve(id) → canonical_id` interface. Whether it's eager or lazy is hidden behind this interface.

### M4 / Scenario Beta: Post-split events enter a dedicated attribution workflow

Events referencing an archived (split) subject are accepted, stored under the archived stream, and flagged with type "post-split-reference." The read model shows them in an attribution queue with successor subjects' attributes for comparison. A reviewer attributes each event to a successor.

- **Reversibility reason**: The attribution action produces a standard event (e.g., `EventAttributedToSuccessor{original_event_id, successor_subject_id, attributed_by}`). The flag type, queue presentation, and attribution workflow are all projection/read-model/policy concerns. The stored events — both the original post-split event and the attribution event — follow standard event structures.
- **Fallback if wrong**: If manual attribution proves too expensive at scale, add heuristic auto-attribution (e.g., GPS proximity to successor locations). This is a policy change, not a structural one.
- **Interface required**: The ConflictDetected event must support a `conflict_type: "post_split_reference"` value. The attribution event must reference both the original event and the target successor. Both are payload conventions.

### Q4: Cross-type references use the same typed UUID mechanism

All identity references — subject to subject, event to subject, event to actor, shipment to org unit — use the same typed UUID reference format: `{type, id}`.

- **Reversibility reason**: This is already implied by ADR-1 S3 (all identifiers are client-generated UUIDs) and the Q1 constraint (typed references). Confirming it here as a uniform strategy means no special-case reference mechanism. If a specific identity type later needs additional reference metadata (e.g., a version or timestamp), the reference format can be extended — the type discriminator makes this safe.
- **Fallback if wrong**: Extend the reference format with additional fields for specific types. The type discriminator ensures backward compatibility.
- **Interface required**: Reference format: `{type: "subject" | "actor" | "process" | "assignment", id: UUID}`. Extensible (future fields can be added per-type).

### Q6: Conflict resolution happens centrally (coordinator queue) with delegation capability

Conflicts are resolved via a coordinator-accessible queue. Coordinators may delegate specific conflict types to supervisors. Conflict resolution does not happen on field worker devices.

- **Reversibility reason**: Resolution location is a UI and authorization concern. The ConflictResolved event is identical regardless of where the resolution was initiated. Moving resolution to devices requires only deploying resolution UI to field devices and extending delegation rules.
- **Fallback if wrong**: If coordinator capacity is insufficient, extend resolution UI to supervisor devices. The event structure supports this — the `resolved_by` field already captures the actor, not the location.
- **Interface required**: The ConflictResolved event structure is location-agnostic. The authorization model (ADR-3) determines who can resolve what.

### Q7: Cascading resolution is iterative, not compound

Resolving conflict A may surface conflict B. These are handled as separate items in the resolution queue. There is no "compound resolution" event type.

- **Reversibility reason**: Whether resolution is iterative or compound is a workflow/UX concern. The event structure is the same — individual ConflictResolved events. A future "compound resolution" feature would produce the same individual events behind a UX convenience layer.
- **Fallback if wrong**: If iterative resolution proves too slow for chains of related conflicts, add a "batch resolve related" action in the read model (similar to A3 batch resolution). This is a projection/UX change.
- **Interface required**: ConflictResolved events carry a `related_conflicts` field (optional) that links to other conflicts surfaced by this resolution. This is a payload convention for read-model grouping.

### Q11: Post-merge sync — devices receive the merge event and resolve locally

Devices receive SubjectsMerged events via standard sync. The device processes the event locally: updates its alias table, rebuilds affected projections, updates the subject registry view. The server does not rewrite the device's local state.

- **Reversibility reason**: This is a sync implementation strategy. If local resolution proves too costly on devices, the server can send pre-computed projections alongside merge events (B1/B5 strategy). The underlying merge events are identical.
- **Fallback if wrong**: Server-push merged projections. Device skips local rebuild and accepts the server-computed state. This requires ADR-3 (sync topology) to support projection delivery, but does not change ADR-2's event or identity model.
- **Interface required**: The sync protocol delivers merge events as standard events. Device processing is local by default. Server-computed projection delivery is an optional optimization deferred to ADR-3.

### Alpha (Combination): Flag read model groups compound anomalies by root event

When multiple flags reference the same event (e.g., stale subject reference + revoked assignment), the read model displays them as a single compound anomaly, showing all flags together with their combined context.

- **Reversibility reason**: Purely a read-model/UX concern. The stored flag events are independent. Grouping logic can be changed, extended, or removed without affecting stored data.
- **Fallback if wrong**: If compound grouping proves confusing, revert to independent flag display. No data changes.
- **Interface required**: Flag events must carry the `triggering_event_id` in their payload (already specified in A3 strategy). Grouping uses this field.

---

## Bucket 3: Deferred to Other ADRs (Detail)

### A5 / M3: Downstream work invalidation cascade — Deferred to ADR-5

- **Owning ADR**: ADR-5 (Workflow / State)
- **What ADR-2 must NOT decide**: ADR-2 must not define how downstream events (assignments, reviews, allocations) are invalidated when their triggering event is flagged. ADR-2 commits to detect-before-act ordering (Assumption A12 constraint), which prevents the worst version of this problem. The remaining concern — what happens when a flag is resolved as "reject" and downstream work already exists — is a workflow state machine concern.
- **Explicit "Not Decided Here" entry needed?**: Yes. ADR-2 should state: "Cascade behavior when a ConflictResolved event invalidates an upstream event is deferred to ADR-5. ADR-2's detect-before-act constraint reduces but does not eliminate this concern."

### Gamma (Combination) / M6: Pending match timeout and bijective constraint — Deferred to ADR-5

- **Owning ADR**: ADR-5 (Workflow / State), with some aspects in ADR-4 (Configuration)
- **What ADR-2 must NOT decide**: ADR-2 must not define matching algorithms, confidence thresholds, timeout policies, or bijective constraints for pending matches. These are workflow concerns specific to the process types (shipments, etc.) that use the pending match pattern. ADR-2 only needs to ensure that the identity model supports unresolved references — which it does, because events are accepted even with unresolved identity links (Q12 constraint).
- **Explicit "Not Decided Here" entry needed?**: Yes. ADR-2 should state: "The pending match pattern (events with unresolvable identity references at creation time) is acknowledged. ADR-2's accept-always protocol ensures such events are stored. The matching, resolution, and timeout workflows are deferred to ADR-5."

### Q5: Conflict definition boundary — Deferred to ADR-4

- **Owning ADR**: ADR-4 (Configuration)
- **What ADR-2 must NOT decide**: ADR-2 must not define which specific conditions constitute conflicts beyond the structural ones (concurrent state changes, stale references, duplicate identity). Whether "two observations of the same subject on the same day" is a conflict is a domain-specific business rule that deployers configure. ADR-2 provides the detection mechanism; ADR-4 defines the configuration boundary for conflict rules.
- **Explicit "Not Decided Here" entry needed?**: Yes. ADR-2 should state: "ADR-2 defines the structural conflict types the platform detects by default (concurrent state change, stale reference, duplicate identity, cross-lifecycle). Domain-specific conflict rules (business rule violations) are configurable per activity, deferred to ADR-4."

### Assumption A10 / Q13: Pending match pattern generality — Deferred to ADR-4 / ADR-5

- **Owning ADR**: ADR-4 (Configuration) for whether it's a general pattern; ADR-5 (Workflow) for the matching/resolution mechanics
- **What ADR-2 must NOT decide**: Whether the pending match pattern is shipment-specific or a general mechanism for any event with an unresolved identity reference. ADR-2's identity model supports both — events with unresolved references are accepted (Q12), and the matching logic is projection-layer work.
- **Explicit "Not Decided Here" entry needed?**: No. ADR-2's accept-always constraint implicitly supports pending matches without needing to define them.

---

## Bucket 4: Accepted Risks (Detail)

### C1: Every concurrent state change requires human resolution

- **Why acceptable now**: This is by design — P5 ("Conflict is surfaced, not silently resolved"). Automatic conflict resolution for state changes would require the platform to make domain-specific semantic judgments that it cannot make generically. The stress test confirmed the mechanism holds.
- **Trigger to revisit**: If, in live deployment, concurrent state change volume exceeds reviewer capacity by more than 3x (measured as: flags generated per day / flags resolved per day > 3 for more than 2 consecutive weeks), introduce priority-based auto-resolution for specific, well-defined state change patterns. This is enabled by the auto-resolution strategy in A6.

### C2: Batch conflict detection is computationally feasible at projected scale

- **Why acceptable now**: The stress test showed O(N × M) comparisons for N incoming events and M per-subject events in the staleness window. Worst case (980,000 comparisons) is computationally trivial on a server. The real bottleneck is downstream flag volume, which is addressed by A3/A6 strategies.
- **Trigger to revisit**: If server-side sync processing time exceeds 30 seconds for a single device's batch of events, investigate conflict detection optimization (indexing by subject, caching subject streams).

### C4: Cross-device ordering for the same actor is best-effort

Events from the same actor across different devices are not totally ordered unless the actor syncs between device changes. This is an inherent limitation of device-sequence ordering.

- **Why acceptable now**: Phone sharing and device switching are real but infrequent. The sync watermark provides partial ordering when the actor syncs between devices. For the cases where no sync occurs, the events are correctly flagged as concurrent — which is technically accurate (neither device knew about the other's events).
- **Trigger to revisit**: If deployments report that same-actor cross-device ordering is a significant source of incorrect conflict flags (>10% of total flags traced to this cause), evaluate adding an optional actor-sequence counter that spans devices. This would require a new envelope field — escalating to a Bucket 1 decision at that point.

### C5: S19 Act 2 conflict is correctly detected — no action required

- **Why acceptable now**: The mechanism works exactly as designed. The sync watermark comparison correctly identifies the deactivation + observation as concurrent events requiring resolution.
- **Trigger to revisit**: N/A. This is a confirmed capability, not a risk.

### M5: Unmerge event attribution problem — eliminated

- **Why acceptable now**: B6 (corrective split replaces unmerge) eliminates this problem entirely. The SubjectsUnmerged event type does not exist. Post-merge corrections use SubjectSplit, which has a defined attribution workflow (M4 strategy).
- **Trigger to revisit**: Only if B6 is reversed — which would require re-introducing SubjectsUnmerged, escalating to a major ADR amendment.

---

## Simplicity Validation

**Test**: Walk through S00 (a single worker records an observation about a known subject on one device that syncs normally) with ALL Bucket 1 constraints applied.

### S00 Walk-through With All Constraints

**Step 1: Worker opens the app, selects a subject, records an observation.**

The device writes one event:

```
{
  event_id: UUID (client-generated),
  type: "ObservationRecorded",
  subject_ref: {type: "subject", id: UUID},      ← Q1 constraint: typed reference
  actor_ref: {type: "actor", id: UUID},           ← Q1 constraint: typed reference
  payload: {temperature: 37.2, ...},
  device_time: "2026-04-10T10:00:00",             ← C3 constraint: advisory only
  device_id: UUID,                                ← M8 constraint: hardware-bound
  device_sequence: 48,                            ← C3/Q8 constraint: monotonic counter
  sync_watermark: V100,                           ← Q8 constraint: last-known server version
  shape_version: "v1"                             ← ADR-1 S5
}
```

**Step 2: Worker syncs.**

The device sends the event to the server.

**Step 3: Server processes the event.**

Per A12 constraint, the Conflict Detector runs first:
- Subject is active? Yes.
- Sync watermark V100 — any state changes on this subject since V100? No.
- Result: No conflict. No flag generated.

Policies fire: None triggered (simple observation, no downstream work).

Event is stored. Projection for the subject is updated (append one event).

**Metadata count in event envelope:**

| Field | Required by | New to ADR-2? |
|-------|------------|---------------|
| event_id | ADR-1 S5 | No |
| type | ADR-1 S5 | No |
| subject_ref (typed) | ADR-2 Q1 | **Yes — adds type discriminator** |
| actor_ref (typed) | ADR-2 Q1 | **Yes — adds type discriminator** |
| payload | ADR-1 S5 | No |
| device_time | ADR-1 S5 | No |
| device_id | ADR-2 M8 | **Yes — explicit hardware binding** |
| device_sequence | ADR-2 Q8 | **Yes** |
| sync_watermark | ADR-2 Q8 | **Yes** |
| shape_version | ADR-1 S5 | No |

**New envelope fields added by ADR-2**: 3 (device_id, device_sequence, sync_watermark) plus type discriminators on identity references.

**Aggregates that validate the write**: 1 — Subject Stream (validates subject_ref exists locally and payload matches shape). The Conflict Detector runs on sync, not on write. No identity resolution, no alias lookup, no merge handling.

**Sync steps**: 1 — send event to server. Server conflict-checks, stores, projects. Standard round-trip.

### Simplicity Assessment

S00 remains simple. The worker writes one event. The event has 3 new fields compared to ADR-1, all lightweight (two integers and a UUID). One aggregate validates the write. Sync is one step. No alias resolution, no flag generation, no conflict handling.

The type discriminator on identity references adds a small constant overhead (a string field per reference) but prevents ambiguity that would be far more expensive to resolve later.

**Verdict: S00 has not gotten materially more complex.** The Bucket 1 constraints add necessary metadata to the envelope without adding steps, aggregates, or conditional logic to the simple case. P7 holds.

---

## ADR-2 Decision Skeleton

Using ONLY the Bucket 1 constraints:

### S1: Event Envelope — Causal Ordering Fields

**Every event carries `device_id` (hardware-bound), `device_sequence` (monotonic per device), and `sync_watermark` (last-known server state version at creation time).**

Forced by: Q8 (mechanism choice), Q9 (ordering scope per-subject), C3/C5 (device_time unreliable, watermark detects S19 conflict), M8 (device sequence namespace must not collide).

### S2: Event Envelope — Typed Identity References

**All identity references in event envelopes carry a type discriminator and a UUID: `{type: "subject" | "actor" | "process" | "assignment", id: UUID}`.**

Forced by: Q1 (four identity types discovered across S01, S03, S06, S07, S19), cross-type reference need (shipments reference org_units, actors, and supply items — all different types).

### S3: device_time Is Advisory

**`device_time` is for display and audit only. No projection logic, conflict detection, or protocol correctness may depend on `device_time`. Intra-device ordering uses `device_sequence`. Cross-device concurrency uses `sync_watermark`.**

Forced by: C3 (clock reset corrupts device_time), Assumption A8, Q10 (clock trust).

### S4: Device Sequence and Sync Watermark Persistence

**`device_sequence` and `sync_watermark` MUST be persisted to durable storage on the device, surviving reboots, app restarts, and crashes. The tuple `(device_id, device_sequence)` is globally unique and never reused.**

Forced by: Assumption A2, Assumption A3 (loss of either breaks envelope guarantees permanently).

### S5: Device Identity Is Hardware-Bound

**`device_id` identifies a physical device, not a user account. Device replacement produces a new `device_id`. The `device_sequence` namespace is scoped to `device_id`.**

Forced by: M8 (sequence collision on device replacement), Assumption A9.

### S6: Merge Is Alias-in-Projection, Never Re-Reference

**`SubjectsMerged` creates an alias mapping (`retired_id → surviving_id`). No existing event is modified. Projections resolve retired references for read purposes. The alias is an event and syncs normally.**

Forced by: ADR-1 S1 (immutability), ADR-1 S2 (events are source of truth), Q2 (merge semantics), B2-B6 (alias table findings).

### S7: No SubjectsUnmerged — Wrong Merges Use Corrective Split

**The platform does NOT define a `SubjectsUnmerged` event type. Incorrect merges are corrected by splitting the surviving subject, creating a new successor for the wrongly-merged entity.**

Forced by: B6 (unmerge is structurally unsound given immutability), Invariant #2 (projection engine cannot deterministically rebuild after unmerge).

### S8: Split Freezes History; Source Is Permanently Archived

**A `SubjectSplit` archives the source subject (terminal state). Historical events remain under the source_id. New events go to successors. Post-split events referencing the archived source are accepted and flagged.**

Forced by: Q3 (split semantics), ADR-1 S1 (immutability prevents re-attribution), B4 (archived is terminal — required for acyclicity).

### S9: Lineage Graph Acyclicity By Construction

**`SubjectsMerged` requires both operands in 'active' state. `SubjectSplit` transitions the source to 'archived' (terminal — cannot be merged into, split again, or reactivated). These rules guarantee the lineage graph is a DAG.**

Forced by: B4 (cycle is constructable without these rules), Invariant #4 (lineage must be acyclic).

### S10: SubjectSplit Is Online-Only

**`SubjectSplit` commands execute only through a server-validated transaction. Offline split is not supported. The server verifies the source has not already been archived before writing the event.**

Forced by: Assumption A11 (offline split collision produces unrecoverable lineage contradictions).

### S11: Single-Writer Conflict Resolution

**Every `ConflictDetected` event designates exactly one resolver identity. Only that resolver's `ConflictResolved` event is canonical.**

Forced by: A2 (meta-conflict has no termination condition), M1 (missing workflow path), Invariant #6 (Critical).

### S12: Conflict Detection Before Policy Execution

**During sync processing, the Conflict Detector evaluates all incoming events BEFORE reactive policies execute. Flagged events do not trigger policies until the flag is resolved.**

Forced by: A5 (downstream work on flagged events), Assumption A12, cascade prevention.

### S13: Conflict Detection Uses Raw References

**The Conflict Detector evaluates events using their original `subject_id` as written. Alias resolution to surviving identities occurs only in the projection layer, after detection.**

Forced by: B2 (ordering of alias vs. detection), audit trail preservation (flag must record the provenance that the event referenced a retired identity).

### S14: Events Are Never Rejected for State Staleness

**The platform never rejects a validly-structured event based on subject state staleness. All events are accepted and stored. State anomalies produce separate `ConflictDetected` events.**

Forced by: Q12 (orphaned events), P3 (append-only), P5 (conflict surfaced), V1 (offline work is always preserved).
