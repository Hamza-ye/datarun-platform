# Phase 1: ADR-002 Identity + Conflict Recovery

## Context Capsule

- Recovery mode: archaeological reconstruction only; no redesign, no alternatives, no reopening.
- Anchor file: `002-phase0-decision-register.md`.
- ADR target: ADR-002 S1–S14.
- Source files read:
  - `09-adr2-phase3-classification-results.md`
  - `05-adr2-event-storm-identity.md`
  - `07-adr2-phase2-stress-test-results.md`
- Reading order used: `09 → 05 → 07`.
- Settled outputs:
  - Identity taxonomy and lifecycle semantics for `subject`, `actor`, `process`, `assignment`.
  - Conflict detection and resolution vocabulary.
  - Causal ordering semantics using `device_id`, `device_sequence`, `sync_watermark`.
  - Accept-and-flag semantics and detect-before-act boundary.
  - Alias table boundary: raw-reference detection before projection-time resolution.
  - Merge/split semantics: alias merge, no unmerge, corrective split, split freezes history.
- Rejected / excluded:
  - `SubjectsUnmerged` as a symmetric reverse of merge.
  - Physical re-reference of historical events after merge.
  - Device-time-based structural ordering.
  - Untyped UUID references.
  - Account-bound `device_id`.
  - Offline split.
- Deferred / open evolution:
  - Who may resolve each conflict type → ADR-003.
  - Downstream cascade after flag resolution → ADR-005.
  - Domain-specific conflict rules → ADR-004 / ADR-005.
  - Flag grouping, backlog management, auto-resolution, and reviewer ergonomics → evolvable strategy.
  - Projection rebuild optimization after merge → implementation strategy.
- Terms locked in this pass:
  - `typed identity reference`
  - `subject`
  - `actor`
  - `process`
  - `assignment`
  - `device_id`
  - `device_sequence`
  - `sync_watermark`
  - `device_time` advisory
  - `hardware-bound device identity`
  - `SubjectsMerged`
  - `SubjectSplit`
  - `alias mapping`
  - `retired_id`
  - `surviving_id`
  - `successor`
  - `active`
  - `archived`
  - `lineage DAG`
  - `ConflictDetected`
  - `ConflictResolved`
  - `designated resolver`
  - `single-writer resolution`
  - `detect-before-act`
  - `accept-and-flag`
  - `raw-reference detection`

---

## 1. ADR Checkpoint

This pass is bounded by ADR-002 S1–S14 from the Phase 0 register.

| ADR-002 ID | Recovery use in this pass                                                                                                                    |
| ---------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| S1         | Recover causal ordering semantics: every event carries `device_id`, `device_sequence`, `sync_watermark`; causality is evaluated per subject. |
| S2         | Recover the four typed identity categories and the typed reference protocol.                                                                 |
| S3         | Recover why `device_time` is advisory only.                                                                                                  |
| S4         | Recover durability requirements for `device_sequence` and `sync_watermark`.                                                                  |
| S5         | Recover why `device_id` is hardware-bound.                                                                                                   |
| S6         | Recover merge-as-alias semantics.                                                                                                            |
| S7         | Recover why `SubjectsUnmerged` was rejected.                                                                                                 |
| S8         | Recover split semantics: source archived, historical events frozen, new events to successors.                                                |
| S9         | Recover lineage DAG invariants.                                                                                                              |
| S10        | Recover why merge and split are online-only server-validated operations.                                                                     |
| S11        | Recover single-writer conflict resolution.                                                                                                   |
| S12        | Recover detect-before-act.                                                                                                                   |
| S13        | Recover raw-reference detection before alias projection.                                                                                     |
| S14        | Recover accept-and-flag.                                                                                                                     |

Everything below maps to one or more of these entries. Anything not mappable is either excluded or listed as open evolution.

---

## 2. Identity Taxonomy

ADR-002 settles that all identity references carry `{type, id}` and that the four identity categories are `subject`, `actor`, `process`, and `assignment`.

The recovered architectural rule is:

> Identity is not one aggregate. The platform uses one shared typed-reference protocol across four identity categories with different lifecycles.

### 2.1 Common protocol for all identity categories

All four categories share these properties:

- They are referenced by events through a typed UUID reference: `{type, id}`.
- The `type` discriminator is mandatory for semantic correctness; an untyped UUID is ambiguous.
- The UUID identity is immutable after creation.
- References survive sync boundaries.
- Cross-type references use the same reference shape; the platform does not invent per-category pointer formats.

This is the boundary recovered from ADR-002 S2 and Q1 in the classification file.

### 2.2 Subject identity

**Definition:** the real-world thing an event is about: person, facility, household, equipment, organizational unit, or similar operational entity.

**Lifecycle:**

| Stage | Semantics | Settled boundary |
|---|---|---|
| Created | A subject starts as a client-generated UUID created by `SubjectRegistered` / equivalent registration event. | Client-generated UUID is valid even offline. |
| Referenced | Domain events point to the subject via `{type: subject, id}`. | Historical references are never rewritten. |
| Mutated | Attributes may change without changing identity. | Name/type/location changes are state changes, not new identity. |
| Ambiguous duplicate | Two subject IDs may represent the same real-world thing. | Matching may flag candidates; identity remains separate until resolved. |
| Merged | `SubjectsMerged` maps `retired_id → surviving_id`. | Merge is alias-in-projection; no physical re-reference. |
| Retired by merge | Retired subject ID no longer appears as active selectable identity. | Historical events stay under retired ID and project through alias. |
| Split | `SubjectSplit` creates successors and archives the source. | Historical events freeze under source; new events go to successors. |
| Archived by split | Source becomes terminal. | Archived subjects cannot become active, be merged into, or be split again. |
| Stale/cross-lifecycle referenced | Offline events may arrive against retired, archived, deactivated, or otherwise stale subject state. | Event is accepted and flagged; not rejected. |

**Boundary:** subject identity owns lineage and alias semantics. It does not own authorization, sync scope, role permission, workflow state, or configuration-specific matching rules.

### 2.3 Actor identity

**Definition:** the person or system actor that authors, approves, transfers, reviews, or otherwise performs work.

**Lifecycle:**

| Stage | Semantics | Settled boundary |
|---|---|---|
| Provisioned | Actor identity is created by the organization/platform, not self-registered like a field subject. | Actor is distinct from subject even when a person can also be the subject of an assessment. |
| Referenced | Events reference actor ID independently of assignment and device. | `actor_id` and `device_id` are different axes. |
| Role changes | Roles can change over time. | Role change does not change actor identity. |
| Assignment changes | Actor may gain/lose assignments. | Assignment lifecycle is separate from actor lifecycle. |
| Device changes | Actor may use different hardware. | Device identity remains hardware-bound; actor identity does not determine `device_id`. |
| Persistent | Actor identity survives transfers and role changes. | No actor merge/split semantics are settled in ADR-002. |

**Boundary:** actor identity answers “who authored/performed this?” It does not answer “was this actor authorized?” That is recovered in ADR-003.

### 2.4 Process identity

**Definition:** a bounded operational process instance such as a shipment, case, campaign instance, or handoff chain.

**Lifecycle:**

| Stage | Semantics | Settled boundary |
|---|---|---|
| Initiated | Process identity is created when the operational process starts. | Process ID can be client-generated at initiation. |
| Referenced | Events across handoffs or stages reference the same process identity. | Process gives continuity across multiple subjects/actors/events. |
| Progresses | Process accumulates events over its duration. | State progression details are not ADR-002; they are workflow territory. |
| Completed/closed | Process may end when the operational chain completes. | Post-completion events are cross-lifecycle/stale-context candidates if they occur offline. |
| Transient | Process identity exists for process duration, unlike actor/subject persistence. | Process lifecycle is not a substitute for subject identity. |

**Boundary:** process identity gives correlation across a running operation. ADR-002 only settles that it is a typed identity category; workflow behavior is later ADR-005 territory.

### 2.5 Assignment identity

**Definition:** a bounded responsibility relation, for example “Actor A is responsible for Zone 3 during interval T.”

**Lifecycle:**

| Stage | Semantics | Settled boundary |
|---|---|---|
| Created by authority | An assignment is created by an actor/system with assignment authority. | Creation authority is ADR-003; identity category is ADR-002. |
| Binds actor to scope | Assignment connects one actor to one operational scope. | Exact scope types/containment are ADR-003/ADR-004. |
| Temporally bounded | Assignment has effective time bounds and can expire/revoke. | Temporal access semantics are ADR-003. |
| Referenced | Events may reference assignment identity where later ADRs require it. | ADR-003 later rejects authority context in envelope; do not infer envelope assignment refs here. |
| Revoked/ended | Assignment can cease being active. | Events created under stale assignment become authorization flags in ADR-003, not ADR-002 structural conflicts. |

**Boundary:** assignment identity exists as a referenceable identity type. ADR-002 does not settle the authorization model. It only preserves the identity category needed for later access/sync work.

---

## 3. Primitive Boundaries Recovered from ADR-002

| Primitive / component | Settled responsibility | Explicit non-responsibility |
|---|---|---|
| Event Store | Stores immutable events. Maintains append-only source of truth. | Does not rewrite event references after merge/split. |
| Projection Engine | Builds current read models from events; applies alias mapping after conflict detection. | Does not create historical truth; projections are rebuildable and derived. |
| Subject Registry | Maintains subject identity attributes and lifecycle state. | Does not resolve duplicate identity by itself without resolver semantics. |
| Subject Identity Resolver | Emits and validates `SubjectsMerged` and `SubjectSplit`; enforces lineage DAG. | Does not physically move old events between subjects. |
| Conflict Detector | Evaluates incoming events for concurrency, stale references, duplicate identity, and cross-lifecycle anomalies. | Does not silently choose winners. Does not execute downstream policy before detection. |
| Conflict Resolver | Records resolution through `ConflictResolved`; canonical resolution comes from designated resolver only. | Does not allow competing canonical resolutions. |
| Assignment Registry | Binds actor to scope temporally as an identity category discovered in ADR-002. | Authorization enforcement details are ADR-003. |

---

## 4. Conflict Type Taxonomy

ADR-002 settles the structural conflict mechanism, not every possible domain conflict rule. The clean boundary is:

> Structural conflicts are platform-detected because they threaten identity, causality, lifecycle, or event validity. Domain conflicts are deployer/configuration concerns handled later.

### 4.1 Settled ADR-002 structural conflict types

| Conflict type | What constitutes it | Detection basis | Default handling |
|---|---|---|---|
| Concurrent state change | Two causally concurrent events attempt incompatible state changes for the same subject. | Same subject; causality comparison using `device_sequence` + `sync_watermark`; no causal ordering between the writes. | Accept both; create/attach conflict flag; designated resolver decides. |
| Duplicate identity | Two or more subject IDs plausibly represent the same real-world thing. | Matching rules and human review over subject identity attributes. | Keep identities independent until resolved; merge only through `SubjectsMerged`. |
| Stale reference | Event was created against subject state older than the actor/device knew, such as a retired/merged identity or outdated classification. | Incoming event’s raw subject reference + `sync_watermark` compared to server state changes since that watermark. | Accept event; flag staleness; projection may resolve alias after detection. |
| Cross-lifecycle | Event references a subject/process whose lifecycle state makes the event context stale, e.g. deactivated, split/archived, completed, or otherwise no longer valid for new work. | Lifecycle state check during sync, using raw references before projection resolution. | Accept event; flag for review or annotation. |

### 4.2 Explicit non-conflict

| Case | Why it is not an ADR-002 conflict |
|---|---|
| Concurrent additive events | Two observations on the same subject with no contradiction are accretive. Both are valid and no resolver is needed. |
| Same actor using multiple devices | Actor identity is the same, but device streams are separate; ordering across devices is best-effort unless watermarks establish causality. |

### 4.3 Discovered but not locked as ADR-002 structural vocabulary

| Discovered type | Recovery classification | Reason |
|---|---|---|
| Content mismatch in transfer/receipt | Expansion of an explicitly open front. | Process/domain comparison; later configuration/workflow territory unless elevated by a structural event type. |
| Revoked authority | Deferred to ADR-003. | Authorization staleness, not identity/conflict structure. |
| Domain uniqueness violation | Deferred/open evolution to ADR-004/ADR-005. | Configured business rule; may reuse `ConflictDetected` infrastructure. |
| Transition violation | Deferred/open evolution to ADR-005. | Workflow state/pattern issue, not ADR-002. |
| Flag severity | Deferred to ADR-004/ADR-003 interaction. | Deployment policy over flag types. |

---

## 5. Causal Ordering Recovery

### 5.1 Problem discovered

Device clocks cannot be trusted for structural ordering. Offline-first operation creates events on low-end devices under clock drift, battery reset, timezone errors, and disconnected work. The event storm identified these distinct ordering needs:

| Need | Meaning | Settled mechanism |
|---|---|---|
| Same-device order | Event A happened before Event B on the same physical device. | `device_sequence`. |
| Staleness detection | Event was created without knowledge of server changes after the device last synced. | `sync_watermark`. |
| Cross-device concurrency | Two devices wrote without knowledge of each other. | Compare sync watermarks and subject streams; if incomparable, surface conflict. |
| Explicit dependency | Event intentionally corrects/reviews/references a prior event. | Event reference in payload/envelope as appropriate. |
| Human-facing time | Approximate time user performed the action. | `device_time`, advisory only. |

### 5.2 Chosen mechanism

Every event carries:

```txt
(device_id, device_sequence, sync_watermark, device_time)
```

Only the first three are structural for causal semantics:

- `device_id` scopes the device sequence namespace.
- `device_sequence` is a durable monotonic counter per physical device.
- `sync_watermark` records the last-known server state version at event creation time.
- `device_time` is retained for display/audit only and cannot determine correctness.

### 5.3 Why alternatives did not become settled architecture

| Alternative | Why rejected / not locked |
|---|---|
| Device-time ordering | Device clocks can reset or drift; using device time would permanently misorder immutable events. |
| Vector clocks | Grow with number of devices; too heavy for the low-end/offline deployment model. |
| Hybrid Logical Clocks | More envelope complexity than needed for the project’s accepted conflict-surfacing posture. |
| Cross-device total order | Not derivable without stronger coordination. ADR-002 accepts that concurrent state changes require human resolution. |

### 5.4 Durability and namespace requirements

- `device_sequence` must persist across reboot, app restart, crash, and battery loss.
- `sync_watermark` must persist across reboot, app restart, crash, and battery loss.
- `(device_id, device_sequence)` is globally unique.
- `device_id` is hardware-bound, not account-bound.
- New hardware gets a new `device_id`; it must not reuse the previous device’s sequence namespace.

### 5.5 Limitation accepted as design trade-off

If two devices share the same sync watermark and write conflicting state changes for the same subject, the platform can detect concurrency but cannot decide the winner. That is acceptable because ADR-002’s policy is to surface conflicts, not silently resolve them.

---

## 6. Accept-and-Flag Recovery

### 6.1 Settled rule

Events are never rejected for state staleness.

When an event arrives under stale state, retired identity, stale subject lifecycle, or concurrent state mutation:

1. The event is accepted into the append-only event store.
2. The Conflict Detector evaluates it before downstream policies execute.
3. If anomalous, the event is flagged, typically via `ConflictDetected` or an equivalent stored conflict record.
4. The flagged event does not trigger downstream policies until the flag is resolved.
5. The designated resolver emits `ConflictResolved` if/when resolution occurs.

### 6.2 Why this was forced

The underlying constraints make rejection unsafe:

- Offline work may be operationally real even if based on stale local state.
- The event store is append-only; deleting or rejecting after the fact would lose traceability.
- State can change while devices are offline; rejecting all stale events would discard useful field data.
- Last-write-wins and silent overwrite are outside the accepted architecture.

The mechanism therefore preserves the event and surfaces the anomaly.

### 6.3 Under-load findings

The stress test found that accept-and-flag is structurally sound but operationally fragile without read-model and workflow support.

| Pressure | Settled architectural impact |
|---|---|
| Duplicate/offline flag creation | Flag creation location is strategy, not ADR-002 schema. Do not lock server-only or device-created flags in ADR-002 recovery. |
| Multiple reviewers resolving same conflict | Structural: every conflict has one designated resolver; other resolutions are unauthorized/flagged. |
| 50+ flags from one stale root cause | Operational strategy: grouping, root-cause metadata, batch resolution are evolvable read-model/workflow concerns. |
| Flag backlog growth | Operational strategy: escalation, auto-resolution, backlog dashboards are not ADR-002 structural vocabulary. |
| Flagged event triggers downstream work | Structural: detect-before-act prevents policies from firing on flagged events during sync. Remaining cascade after resolution is ADR-005. |

### 6.4 Single-writer resolution

Each `ConflictDetected` designates exactly one resolver identity. Only a `ConflictResolved` authored by that resolver is canonical. A resolution from any other actor is accepted as an event but is flagged as unauthorized / non-canonical.

This avoids recursive meta-conflicts: without a designated resolver, two immutable resolution events could contradict each other with no termination condition.

### 6.5 Detect-before-act

During sync, conflict detection runs before policy execution. A flagged event does not trigger assignment creation, review creation, allocation, task generation, trigger execution, or other downstream policy actions until resolved.

This is strategy-protecting because it prevents stored downstream cascades from being created on top of unresolved upstream events.

---

## 7. Alias Table and Raw-Reference Boundary

### 7.1 Settled rule

Merge does not rewrite events.

`SubjectsMerged` creates an alias mapping:

```txt
retired_id → surviving_id
```

The projection layer applies this mapping to read models. Historical event payloads and references remain as originally written.

### 7.2 Detection before alias resolution

Conflict detection evaluates the original raw `subject_id` as written by the device.

Only after conflict detection does alias resolution map retired IDs to surviving IDs for projection.

Pipeline:

```txt
incoming event
  → evaluate raw subject_ref / raw subject_id
  → detect stale reference, duplicate identity, cross-lifecycle, concurrency
  → attach/emit flag if needed
  → projection applies alias mapping
  → read model shows current surviving subject context
```

### 7.3 Why this boundary exists

If alias resolution happened before conflict detection, an event written against a retired identity would appear as if it had been written directly against the surviving identity. That would erase the provenance that the actor was operating with stale identity knowledge.

ADR-002 therefore preserves two truths:

- Stored truth: the event referenced the raw subject ID it was written with.
- Projected truth: current views resolve that ID through the alias table.

### 7.4 Alias table scope

Settled:

- Alias mapping is projection-level.
- Historical events are immutable.
- Transitive closure is eager: if `A → B` and `B → C`, projections resolve `A → C`.
- `retired_id` is not selectable for new normal work once the device knows it is retired.

Evolvable:

- Whether a device rebuilds projections locally or receives server-computed projections.
- Incremental vs full projection rebuild after merge.
- Performance thresholds for batch merges.

---

## 8. Merge / Split Recovery

### 8.1 Merge semantics

`SubjectsMerged` asserts that two subject IDs represent the same real-world subject.

Required semantics:

- Both operands must be active at write time.
- `surviving_id` remains active.
- `retired_id` becomes retired.
- All historical events retain their original `subject_id`.
- Projection for the surviving subject includes events from both streams.
- No event is physically re-referenced.

### 8.2 Why physical re-reference is rejected

Physical re-reference violates append-only immutability. Rewriting historical events from `retired_id` to `surviving_id` would destroy provenance and require data migration across deployed devices.

The alias table preserves historical truth while allowing current-state projection to unify the streams.

### 8.3 Why `SubjectsUnmerged` is rejected

`SubjectsUnmerged` is not a settled event type.

Rejected path:

```txt
T1: X merged into Y
T2: new event written against Y during merge window
T3: unmerge attempts to separate X and Y again
```

Problem:

- The T2 event immutably says `subject_id = Y`.
- It may have been about the old X or the original Y.
- The system cannot infer which without manual review.
- A symmetric unmerge would require re-attributing every post-merge event, which is operationally unbounded and structurally incompatible with immutable event references.

Final rule:

> Wrong merges are corrected by `SubjectSplit`, not `SubjectsUnmerged`.

### 8.4 Corrective split semantics

A wrong merge is corrected by splitting the surviving subject and creating a new successor for the entity that should not have been merged.

Default rule:

- Historical events before split remain where they were written.
- Post-merge events default to the surviving subject.
- Manual re-attribution may be offered as workflow/projection support, but it is not required for structural correctness.

### 8.5 Split freezes history

`SubjectSplit` means one source subject becomes two or more successor subjects.

Settled semantics:

- Source subject transitions to `archived`.
- `archived` is terminal.
- Historical events stay with `source_id`.
- New events go to successor IDs.
- Projection for source shows lineage to successors.
- Successors are new active subject identities.

### 8.6 Acyclic lineage DAG

The lineage graph must be acyclic by construction.

Required invariants:

- `SubjectsMerged`: both `surviving_id` and `retired_id` must be active at write time.
- `SubjectSplit`: source transitions to archived terminal state.
- Archived subjects cannot become active.
- Archived subjects cannot be merge targets.
- Archived subjects cannot be split again.

These rules prevent cycles such as merging a successor back into its archived source.

### 8.7 Online-only operations

Merge and split are online-only server-validated operations.

Reason:

- Offline merge can create contradictory alias mappings.
- Offline split can create conflicting successor sets for the same source.
- Once such events are written, the lineage graph can contain permanent contradictions.

Server validation is part of the structural boundary, not a UI convenience.

---

## 9. Rejected / Excluded Paths

| Path | Status | Reason |
|---|---|---|
| `SubjectsUnmerged` | `[REJECTED]` | Post-merge events cannot be automatically re-attributed without violating immutability or requiring unbounded manual review. |
| Physical event re-reference after merge | `[REJECTED]` | Rewrites event history and destroys provenance. |
| Device-time ordering | `[REJECTED]` | Device clocks are unreliable; structural ordering must not depend on them. |
| Untyped UUID references | `[REJECTED]` | Existing references would become ambiguous across subject/actor/process/assignment. |
| Account-bound `device_id` | `[REJECTED]` | Device replacement would reuse sequence namespace and create `(device_id, device_sequence)` collisions. |
| Offline split | `[REJECTED]` | Conflicting successor sets become permanent lineage contradictions. |
| First-resolution-wins for conflict resolution | `[REJECTED]` | Penalizes offline reviewers and does not encode canonical authority. |
| Recursive meta-conflict resolution | `[REJECTED]` | Conflicts about conflict resolutions have no natural termination point. |

---

## 10. Deferred / Open Evolution

| Item | Classification | Why it remains outside Phase 1 lock |
|---|---|---|
| Resolver authority policy | Expansion of explicitly open front → ADR-003. | ADR-002 says each conflict has one resolver; ADR-003 decides who can be that resolver. |
| Domain-specific conflict definitions | Expansion of explicitly open front → ADR-004/ADR-005. | ADR-002 locks structural conflict mechanism, not deployer-defined business rules. |
| Conflict auto-resolution | Expansion of explicitly open front → ADR-005. | Requires workflow/state/resolution policy. |
| Flag severity | Expansion of explicitly open front → ADR-004. | Blocking vs informational behavior is deployment policy over flag types. |
| Flag grouping and batch resolution | Platform evolution that does not violate accepted decisions. | Read-model/workflow optimization; stored events remain compatible. |
| Root-cause metadata on flags | Platform evolution that does not violate accepted decisions. | Payload/read-model strategy, not envelope schema. |
| Projection rebuild optimization after merge | Platform evolution that does not violate accepted decisions. | Implementation/sync strategy. |
| Downstream cascade after rejecting a flagged event | Expansion of explicitly open front → ADR-005. | ADR-002 prevents policy-fire-before-detect; later workflow decides existing downstream consequences. |
| Sync scope for merge/split events | Expansion of explicitly open front → ADR-003. | Which devices receive which identity events is selective sync. |

---

## 11. Terms Locked by ADR-002

| Term | Definition | ADR anchor |
|---|---|---|
| `typed identity reference` | A reference carrying both identity category and UUID: `{type, id}`. | S2 |
| `subject` | Real-world thing an event is about. | S2 |
| `actor` | Person/system identity that authors or performs work. | S2 |
| `process` | Operational process instance identity. | S2 |
| `assignment` | Temporal responsibility identity binding actor to scope. | S2 |
| `device_id` | Hardware-bound physical-device identifier. | S1, S5 |
| `device_sequence` | Durable monotonic per-device event counter. | S1, S4 |
| `sync_watermark` | Durable last-known server state marker stamped on new events. | S1, S4 |
| `device_time` | Advisory timestamp for display/audit, not structural ordering. | S3 |
| `SubjectsMerged` | Event asserting `retired_id → surviving_id`. | S6 |
| `alias mapping` | Projection-layer mapping from retired subject IDs to surviving IDs. | S6 |
| `retired_id` | Subject ID subsumed by merge. | S6 |
| `surviving_id` | Subject ID that remains active after merge. | S6 |
| `SubjectSplit` | Event that archives a source subject and creates successors. | S8 |
| `archived` | Terminal lifecycle state for split source. | S8, S9 |
| `successor` | New active subject identity created by split. | S8 |
| `lineage DAG` | Directed acyclic graph of merge/split lineage. | S9 |
| `ConflictDetected` | Stored conflict/flag record identifying anomaly and designated resolver. | S11 |
| `ConflictResolved` | Stored resolution event for a conflict. | S11 |
| `designated resolver` | The single actor identity whose resolution is canonical for a conflict. | S11 |
| `single-writer resolution` | Rule that exactly one resolver can canonically resolve each conflict. | S11 |
| `detect-before-act` | Sync processing order: conflict detection before downstream policy execution. | S12 |
| `raw-reference detection` | Conflict detection uses the original subject reference as written, before alias projection. | S13 |
| `accept-and-flag` | Stale/conflicting events are stored and flagged, never rejected for staleness. | S14 |

---

## 12. Output Summary

Phase 1 recovers ADR-002 as a compact architecture layer:

1. Identity is a typed-reference system with four categories.
2. Subject identity has explicit merge/split lineage semantics.
3. Merge is projection aliasing, never event rewriting.
4. Split freezes history and archives the source.
5. Causal ordering uses durable device sequence plus sync watermark, not device time.
6. Device identity is hardware-bound to protect sequence namespace uniqueness.
7. Structural conflicts are surfaced, not silently resolved.
8. Stale events are accepted and flagged, not rejected.
9. Conflict resolution is single-writer per conflict.
10. Conflict detection runs before downstream policy execution.
11. Raw references are evaluated before alias projection.
12. Operational scale concerns around flag queues and projection rebuilds remain evolvable strategies, not ADR-002 structural vocabulary.
