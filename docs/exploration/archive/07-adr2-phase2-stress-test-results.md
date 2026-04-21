> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-002**. For a navigated reading guide, see [guide-adr-002.md](../guide-adr-002.md).
# Phase 2: Workflow Stress-Test Results — ADR-2 Identity & Conflict

> **Phase**: 2 — Workflow Stress-Test
> 
> **Method**: Systematic adversarial path analysis per mechanism, then combined
> 
> **Author**: Workflow Architect
> 
> **Date**: 2026-04-10
> 
> **Input documents consumed**:
> 1. [`docs/constraints.md`](../constraints.md)
> 2. [`docs/adrs/adr-001-offline-data-model.md`](../adrs/adr-001-offline-data-model.md)
> 3. [`docs/exploration/05-adr2-event-storm-identity.md`](05-adr2-event-storm-identity.md)
> 4. Scenarios: S01, S06, S19, S03, S07
>    
> **Output consumer**: Phase 3 (ADR-2 Decision Synthesis)

---

## 1. Mechanism A Findings (Accept-and-Flag)

**Mechanism under test**: When an event arrives that was recorded against stale state (deactivated subject, revoked assignment, changed schema, reclassified entity), the platform accepts the event (immutability) and flags the anomaly as a separate event. A human with authority reviews and resolves.

---

### A1: Flag Created Offline, Syncs Late

**Stress question**: What happens when the flag itself is created offline and syncs late?

**Finding**: There is confusion about *where flags are created*. The event storm implies flags are server-side artifacts — the Conflict Detector runs on sync, and the `ConflictDetected` event is written by the server. If this is the case, flags are never created offline. However, the design is ambiguous about whether device-side validation can also flag anomalies (e.g., a device detects that a subject was deactivated from a recently-synced event and flags its own pending observations). If the device can create flags:

- The flag event carries a stale sync watermark.
- On sync, the server receives both the offending event and its flag.
- The server's own Conflict Detector may independently create a *second* flag for the same event.
- Result: **duplicate flags for the same anomaly**, one device-originated and one server-originated.
- There is no deduplication mechanism specified. The reviewer sees two flags for the same problem.

If flags are server-only: this stress case does not apply, but then the device has no local mechanism to warn the field worker that their work may be problematic. The worker submits confidently, unaware of the anomaly until a reviewer acts — which could be days.

**Severity**: **Weakens.** The mechanism works if flags are strictly server-side, but the operational feedback loop to field workers is dangerously slow. If flags can be device-side, there is no deduplication contract.

**Recommendation**: ADR-2 must specify: *who* can emit `ConflictDetected` events? If server-only, acknowledge the latency cost. If both, define a deduplication invariant (e.g., flag events carry a deterministic hash of the anomaly they describe, and duplicate hashes are collapsed in the projection).

---

### A2: Reviewer Offline — Conflicting Resolutions

**Stress question**: What happens when the reviewer is also offline and their resolution conflicts with another reviewer's resolution?

**Finding**: Walk-through:

1. Server flags anomaly F1 for subject X.
2. Reviewer A (a supervisor in the field) syncs, receives F1, goes offline.
3. Reviewer B (a coordinator at headquarters) sees F1 online and resolves it: `ConflictResolved{conflict_id: F1, resolution_type: "keep_event", resolved_by: B}`.
4. Reviewer A, still offline, resolves F1 differently: `ConflictResolved{conflict_id: F1, resolution_type: "discard_event", resolved_by: A}`.
5. Reviewer A syncs.

The system now has **two ConflictResolved events for the same conflict**. Both are immutable. The Conflict Resolver aggregate states: "Every resolution is an event. Only authorized actors can resolve." It does **not** state: "Only one resolution per conflict."

This is a **meta-conflict** — a conflict about conflict resolution. The mechanisms proposed do not handle this recursion. The projection for F1 must choose which resolution is canonical, but no rule is specified:
- Is it "first synced wins"? That penalizes offline reviewers.
- Is it "highest authority wins"? Authority hierarchy is not specified in the conflict resolution model.
- Is it "both are accepted, and a NEW flag is generated for the meta-conflict"? Then you have unbounded recursion.

This is strictly worse than the original conflict, because the original conflict was between field events from workers who are expected to be sometimes wrong. This is a conflict between *authoritative resolutions* from people who are expected to be right.

**Severity**: **Breaks.** The mechanism as stated has no termination condition for conflicting resolutions. This is a structural gap, not an edge case — any deployment with multiple offline reviewers will hit this.

**Recommendation**: The resolution model must enforce one of:
- **Single-writer resolution**: Only one designated resolver per conflict (assigned when the flag is created). A second resolution from another actor is itself flagged as unauthorized.
- **Resolution locking**: A conflict can only be resolved once. The first `ConflictResolved` event that arrives (by sync order) is canonical. Subsequent attempts are rejected at the Conflict Resolver aggregate — but this requires the aggregate to be stateful about "already resolved," which devices cannot know offline.
- **Resolution escalation**: Conflicting resolutions automatically escalate to a higher authority, with a defined top-level that has no offline capability (ensuring termination). This requires an authority hierarchy that the model does not yet define.

---

### A3: Flag Backlog Accumulation (50 Flags for Same Subject)

**Stress question**: What happens when 50 flagged events accumulate for the same subject — individual review or batch? What if resolving one invalidates another?

**Finding**: Walk-through:

1. Field worker offline for 2 weeks, generates 50 observations against subject X.
2. During those 2 weeks, subject X was reclassified (Type A → Type B).
3. On sync, all 50 events are accepted. All 50 are flagged as "stale reference — recorded under old classification."
4. Reviewer opens the queue: 50 individual flags for the same subject, same root cause.

**Problem 1: Review ergonomics.** The event storm specifies a `Conflict Resolution Queue` read model but does not specify whether flags can be grouped. If the reviewer must resolve 50 flags individually (clicking "accept" 50 times), the mechanism does not scale. A field deployment with 10,000 workers, each generating 20 events during a 3-day connectivity outage following a bulk reclassification, produces **200,000 flags**. No reviewer queue survives this volume.

**Problem 2: Resolution interdependence.** Suppose the reviewer resolves flag #1 by saying "the old classification is acceptable for this observation." Does that resolution logically apply to flags #2–#50, which have the identical root cause? The model does not specify batch resolution. If the reviewer must explicitly resolve each one, the system is operationally unusable. If the system auto-resolves #2–#50 when #1 is resolved — what rule determines that they share a root cause? "Same subject + same conflict type" is too broad (a subject may have stale-reference flags for *different* stale conditions). "Same subject + same conflict type + same triggering change" is more precise but requires the flag event to carry structured metadata about *which* state change made it stale.

**Problem 3: Inter-flag invalidation.** Consider: flags #1–#48 are "stale reference: old classification." Flag #49 is "stale reference: old classification + observation references deactivated sub-subject after a split." Flag #50 is a duplicate-identity flag (the subject was merged during the 2 weeks). Resolving #50 (merge) changes the subject context. Flags #1–#49 now point at a subject whose identity has changed since the flags were created. The flag references are themselves stale. The reviewer is resolving flags against a subject that no longer exists in its flagged form. There is no mechanism to re-evaluate flags when the underlying subject changes.

**Severity**: **Weakens.** The core accept-and-flag principle holds — events are preserved, anomalies are surfaced. But the operational model for *draining* the flag backlog is unspecified and likely unworkable at deployment scale without batch resolution capability and flag interdependence rules.

**Recommendation**:
- Flag events must carry structured root-cause metadata (which specific state change triggered the flag), enabling batch grouping.
- The read model must support batch resolution: "resolve all flags for subject X with root cause Y as [accepted/rejected]."
- When a subject's identity changes (merge, split, deactivation), existing unresolved flags for that subject must be re-evaluated or at minimum annotated ("subject context changed since flag creation").

---

### A4: Subject Merged or Split Between Flag Creation and Flag Review

**Stress question**: What happens when the subject the flag references has been merged or split between flag creation and flag review?

**Finding**:

**Merge case**: Flag F1 references subject X. Before the reviewer acts, X is merged into Y (X is retired, Y survives). The reviewer opens the flag queue and sees F1 referencing X. Does the alias table resolve X→Y in the flag's projection, so the reviewer sees it as a flag on Y? Or does the flag still show X (which is now retired)?

- If the alias table resolves: the reviewer sees the flag in the context of Y, which includes events from both X and Y. The original flag context is lost — the reviewer cannot see "this event was flagged because X was deactivated, but X no longer exists as a separate subject." The merge obscured the flag's original rationale.
- If the alias table does NOT resolve flags: the reviewer sees a flag on a retired subject. They can see the flag in its original context, but they cannot resolve it meaningfully because the subject's canonical identity has changed. Any resolution event they emit references X, but X is retired. Does the resolution propagate to Y?

**Split case**: Flag F1 references subject X. Before the reviewer acts, X is split into X1 and X2. The flag now points at an archived subject. The reviewer must determine: does the flagged event belong to X1 or X2? But the flag itself doesn't give them that information — it just says "this event on X is stale." The reviewer needs domain context to make the attribution decision. The flag resolution now requires *both* conflict resolution authority *and* identity attribution authority. These may not be the same person.

**Severity**: **Weakens.** The mechanism does not account for identity state changes between flag creation and flag review. The flag stores a point-in-time subject reference, but review happens against a potentially different identity graph.

**Recommendation**: Flag events must be annotated when the referenced subject undergoes identity changes. The annotation should preserve the original context ("this flag was created when X was active and independent") while linking to the current identity state ("X is now retired, surviving ID is Y"). Resolution of flags on retired/split subjects must be explicitly modeled — it's a different workflow from resolving flags on stable subjects.

---

### A5: Flagged Event Triggered Downstream Work Before Flag Was Noticed

**Stress question**: What happens when a flagged event triggered downstream work before the flag was noticed?

**Finding**: Walk-through:

1. Worker A (offline) records observation O1 on subject X.
2. Worker A syncs. O1 is accepted normally (no conflict detected *yet* — the deactivation event hasn't been processed or arrived yet from another sync path, or conflict detection has a processing lag).
3. A policy fires: "When new observation arrives on subject X, create a review assignment for Supervisor B."
4. The system creates `ReviewAssigned{assignment_id: RA1, observation: O1, assigned_to: B}`.
5. Now the Conflict Detector runs (or new events arrive revealing the deactivation). Flag F1 is created: "O1 was recorded against deactivated subject X."
6. Supervisor B has already received the review assignment RA1 and may have started or completed the review.

The downstream work (RA1) is built on a flagged event. If the reviewer resolves F1 as "reject O1" — what happens to RA1? It references O1, which has been effectively invalidated. But RA1 is also an immutable event. The system has no mechanism to cascade flag resolution to downstream events and work items.

This is worse for shipments. If a flagged observation triggered a supply allocation decision, that allocation may have already been dispatched. The cascade could cross process boundaries.

**Severity**: **Weakens.** Accept-and-flag creates a temporal window between event acceptance and flag creation. Any policies that fire on accepted events during this window create work based on potentially flagged events. No rollback mechanism exists for downstream consequences.

**Recommendation**: Two possible mitigations (for Phase 3 to choose):
- **Quarantine window**: Events from devices with stale watermarks are held for conflict evaluation before policies fire. This delays downstream work but prevents cascading.
- **Downstream flag propagation**: When a flag resolution invalidates an event, all events that were *caused by* that event (identified by explicit predecessor references in the event envelope) are themselves flagged. This requires causal reference tracking, which the event envelope supports in principle but is not yet specified.

---

### A6: Unbounded Flag Backlog Growth

**Stress question**: Can the flag backlog grow unboundedly? What operational pressure exists to drain it?

**Finding**: There is **no mechanism** in the model that limits flag backlog growth or creates pressure to resolve flags. Consider:

- 10,000 field workers, each syncing 20 events/day.
- 5% of events are against stale state (conservative estimate after any configuration change).
- That is 1,000 new flags/day.
- If a reviewer can resolve 10 flags/minute (generous), that's 600/hour, and draining 1,000 flags requires ~1.7 hours of dedicated reviewer time per day.
- After a major reconfiguration (bulk reclassification, schema change), the stale rate could spike to 50%, producing 100,000 flags in a single day. No realistic reviewer capacity can drain this.

The model has no:
- **Auto-resolution rules** (e.g., "stale-reference flags for classification changes older than 30 days are auto-accepted").
- **Flag expiration/TTL**.
- **Backpressure** (e.g., "if the flag queue exceeds N, escalate to platform admin").
- **SLA on flag resolution** (e.g., "unresolved flags older than 7 days trigger alerts").

An unbounded, unmonitored flag backlog is operationally equivalent to no flags at all — reviewers will learn to ignore it, which defeats the purpose of accept-and-flag.

**Severity**: **Weakens.** The mechanism is structurally sound (events are preserved, anomalies are surfaced) but operationally fragile. Without backlog management, it fails at deployment scale.

**Recommendation**: ADR-2 must specify:
- Batch resolution rules (see A3).
- Auto-resolution policies for low-severity flag types (configurable per deployment).
- Escalation thresholds for backlog size.
- Visibility of backlog age and depth in the coordinator's read model.

---

## 2. Mechanism B Findings (Alias Table in Projection)

**Mechanism under test**: When subjects merge, events are never re-referenced. The projection layer maintains a `retired_id → surviving_id` alias mapping. Queries for either ID return the merged subject's projected state. Devices receive the merge event on sync and update their local alias table. `SubjectsUnmerged` reverses the alias. On split, historical events are frozen under the source ID; the source becomes an archived record with lineage pointers to successors.

---

### B1: Full Merge Lifecycle on a Device Offline During Merge

**Stress question**: Walk through the full merge lifecycle on a device offline during the merge.

**Finding**: Walk-through:

**Before sync (device state)**:
- Subject Registry contains X (UUID: xxx) with attributes {name: "Clinic Alpha", type: "A"}.
- Local event stream for X: [SubjectRegistered, Obs1, Obs2, Obs3] (4 events).
- Subject Registry contains Y (UUID: yyy) with attributes {name: "Clinic Beta", type: "B"}.
- Local event stream for Y: [SubjectRegistered, Obs4] (2 events).
- Device has no knowledge of the merge.

**What arrives on sync**:
- `SubjectsMerged{surviving_id: yyy, retired_id: xxx, merged_by, merged_at, rationale}`
- Possibly additional events that occurred on both streams after the merge.

**What the device must do**:
1. **Update alias table**: Add entry `xxx → yyy`.
2. **Rebuild projection for yyy**: The projection for yyy must now include ALL events from both xxx and yyy streams. That's at minimum 6 events (4 from xxx + 2 from yyy), ordered by device_time.
3. **Invalidate projection for xxx**: xxx no longer has an independent projection. Queries for xxx redirect to yyy.
4. **Update Subject Registry**: Mark xxx as retired. Remove xxx from the active subject list. If the UI shows a subject picker, xxx must not appear as selectable.
5. **Reindex local search/lookup**: If the device has a name-based lookup, "Clinic Alpha" must now resolve to yyy, not xxx.

**Worst-case cost on a low-end phone**:
- Constraints.md specifies: low-end Android, 2G/3G connectivity.
- If yyy has accumulated 80 server-side events and xxx has 40, the device receives the merge event plus up to 120 events it didn't have.
- The local projection rebuild for yyy must process 120+ events.
- If the device has 200 subjects that have been involved in merges (not uncommon in a large deployment): 200 × average_stream_length events to re-project.
- A conservative estimate: 200 subjects × 60 events average = 12,000 events to re-project.
- On a low-end Android phone with limited RAM and CPU, this could take **minutes** of processing time during sync.
- During this time, the device is in an inconsistent state — some projections are rebuilt, others are not.

**Problem**: The event storm does not specify whether projection rebuild after merge is **incremental** (add the retired stream's events to the surviving projection) or **full** (discard and recompute from scratch). Incremental is sufficient if the projection function is purely additive. If the projection function depends on event ordering (e.g., "most recent observation takes precedence"), the merged stream must be re-sorted and re-projected in full.

**Severity**: **Weakens.** The mechanism works logically, but the computational cost on low-end devices is unquantified and potentially prohibitive. The sync window on intermittent 2G may not be long enough to both receive the events AND rebuild projections before connectivity drops.

**Recommendation**: Specify that projection rebuilds after merge MUST be incremental (surviving projection appends retired events, re-sorts, and recomputes only the affected state). If the projection model cannot support incremental merge, this is a fundamental design constraint that must be surfaced now. Additionally, quantify the time budget: "projection rebuild for a merge must complete in < X seconds on the reference low-end device."

---

### B2: Events Created Under retired_id After Merge, Before Sync

**Stress question**: Device A creates events under `retired_id` after the merge but before syncing. Does the alias table resolve them automatically, or does each one generate a flag?

**Finding**: Walk-through:

1. Server merges X into Y at time T1. X is retired.
2. Device A, offline since T0 (before T1), creates:
   - Obs5 at T2: `{event_id: eee, subject_id: X, payload: {temp: 36.8}}`
   - Obs6 at T3: `{event_id: fff, subject_id: X, payload: {temp: 37.1}}`
3. Device A syncs at T4.

**Two mechanisms collide here**:
- **Alias table** (Mechanism B): X→Y. The events reference X, which is retired. The alias should resolve them to Y's stream.
- **Accept-and-flag** (Mechanism A): These events were created against a retired identity. They should be flagged as "stale reference."

**Question: Does the event's `subject_id` get resolved via alias before or after conflict detection?**

- **If alias resolves first**: Events are treated as if they reference Y. They enter Y's stream. Conflict detection evaluates them against Y's state. If Y's state is compatible (same subject, just a different name), no flag is generated. The events are silently absorbed. This is clean but loses the signal that the worker *thought* they were working on X, not Y. If the merge was wrong, these events were attributed to the wrong subject with no audit trail.
- **If conflict detection runs first**: Events reference X, which is retired. Each event generates a "stale reference: retired identity" flag. After flagging, the alias table resolves X→Y for projection purposes. The reviewer sees: "these events reference retired X, but X is now Y. Are they valid observations of Y?" This preserves the audit trail but generates N flags (one per event) for what is likely a routine post-merge cleanup.

The model does not specify the ordering of these mechanisms.

**Additional hazard**: The events' payloads may contain attributes that are specific to X's pre-merge state. For example, if X was "Clinic Alpha, Type A" and Y is "Clinic Beta, Type B," and the observation payload contains `{facility_type: "A"}` (captured from the device's local state), the observation now sits under Y's stream but asserts a facility type that contradicts Y's attributes. The projection for Y now contains internally inconsistent data — some events say Type A, some say Type B. This is not a conflict the Conflict Detector is specified to catch (it looks for concurrent state changes, not payload-attribute mismatches).

**Severity**: **Weakens.** The mechanism works at the identity level (alias resolves correctly) but breaks at the data integrity level (payload attributes may contradict the surviving subject's state). The ordering of alias resolution vs. conflict detection is unspecified and produces different outcomes.

**Recommendation**: ADR-2 must specify:
1. **Ordering**: Conflict detection runs FIRST on the raw event (with its original `subject_id`). Alias resolution happens AFTER, for projection purposes only. This preserves the audit trail.
2. **Payload-attribute conflict**: When an event is alias-resolved into a surviving subject's stream, and the event's payload contains attributes that contradict the surviving subject's current attributes, a specific flag type ("post-merge attribute mismatch") is generated. This is distinct from stale-reference flags.

---

### B3: Transitive Merges (A→B→C)

**Stress question**: A→B, then B→C. Device has events under A. On sync, does it receive two merge events? Does the alias table resolve A→C directly, or must it chase A→B→C?

**Finding**: Walk-through:

1. Time T1: A merges into B. `SubjectsMerged{surviving: B, retired: A}`.
2. Time T2: B merges into C. `SubjectsMerged{surviving: C, retired: B}`.
3. Device offline since T0 (before both merges). Has events under A.
4. Device syncs.

**What the device receives**: Two merge events, in order: (A→B), then (B→C).

**Alias table after processing**:
- After first merge: `A → B`.
- After second merge: `B → C`.
- Two-hop resolution: A → B → C.

**The event storm states**: "The mapping is transitional [sic — presumably 'transitive']: if A→B and B→C, then A→C." (From the Subject Identity Resolver aggregate invariant.)

**But the alias table as described stores pairwise mappings**, not pre-computed transitive closures. Two implementation paths:

- **Eager transitive closure**: When B→C is processed, scan the alias table for anything pointing at B, and update it to point at C. So `A → B` becomes `A → C`. The table now has: `{A → C, B → C}`. Lookup is always single-hop.
  - **Cost**: Every merge requires a scan of the entire alias table for entries pointing at the retired_id. For a table with M entries, that's O(M) per merge. On a device with 200 merged subjects, this is trivial. On a server with 50,000 merged subjects, this could be expensive.

- **Lazy chain-chasing**: The alias table stores raw pairwise mappings. On lookup, chase: A → B → C. Return C.
  - **Cost**: Lookup is O(chain_length). In pathological cases (a subject merged 10 times through intermediaries), chain length = 10. This is functionally unbounded.

**Worst case**: In a large deployment with ongoing data cleanup, transitive merge chains of length 5–10 are plausible (e.g., data cleaning campaign identifies five records that are all the same facility, merges them pairwise). Chain-chasing on every projection read adds latency linear in chain length.

**Severity**: **Weakens.** The mechanism works but the transitive closure strategy is unspecified. Lazy chain-chasing degrades read performance. Eager closure requires write-time maintenance that adds cost to every merge.

**Recommendation**: Specify eager transitive closure as the canonical implementation. When `SubjectsMerged{surviving: C, retired: B}` is processed, all existing alias entries where `target = B` are updated to `target = C`. This bounds lookup to O(1) at the cost of O(M) at merge time, which is acceptable because merges are rare relative to reads.

---

### B4: Split-Then-Merge — Acyclic Lineage Proof

**Stress question**: Subject X splits into X1 and X2. Later, X2 is merged into Y. Can the lineage graph become cyclic?

**Finding**: Walk-through:

1. `SubjectSplit{source: X, successors: [X1, X2]}` — X is archived, X1 and X2 are created.
   - Lineage: `X →(split)→ [X1, X2]`

2. `SubjectsMerged{surviving: Y, retired: X2}` — X2 is retired, Y survives.
   - Alias: `X2 → Y`
   - Lineage: `X2 →(merged into)→ Y`

**Full lineage graph**:
```
X ──split──▶ X1  (independent)
X ──split──▶ X2 ──merged──▶ Y
```

**Is this cyclic?** No. All edges are directed forward in time (split and merge are temporally ordered). For a cycle to exist, Y would need a path back to X. That would require Y to be split or merged into something that eventually leads to X. Since X is archived (frozen), no future event can target X as a surviving_id. The "archived" state is a sink — nothing flows back into it.

**Formal argument**: The lineage graph is a DAG (Directed Acyclic Graph) if and only if:
- The `source` of a split is always archived (cannot be a target of future merge or split).
- The `retired_id` of a merge is always retired (cannot be a target of future merge or split — but CAN it be a source of a future split? The model doesn't say).

**Counter-attempt to create a cycle**:
1. X splits into X1, X2. (X archived)
2. X2 merges into Y. (X2 retired)
3. Y splits into Y1, Y2. (Y archived)
4. Y1 merges into X1. (Y1 retired)
5. Lineage: X → X2 → Y → Y1 → X1 ... and X → X1 (from step 1).
6. X1 is now both a successor of X (from split) AND the surviving target of Y1 (from merge). This is not a cycle — it's a convergence. The graph is still a DAG.

**Actual cycle attempt**:
Could X1 be merged into X (the original archived subject)? The Subject Registry aggregate says subjects can be "active, deactivated, or split." X is in "split/archived" state. The merge command would need to target X as surviving_id. If the aggregate enforces "surviving_id must be active," then this is blocked. **If the aggregate does NOT enforce this**, then: X1 merges into X. X is un-archived. Lineage: X → X1 → X. **This is a cycle.**

**Severity**: **Holds — with condition.** The lineage graph is acyclic IF AND ONLY IF the Subject Identity Resolver enforces: "the surviving_id of a merge must be in 'active' state" and "the source of a split becomes permanently archived (cannot be reactivated, cannot be the target of a merge)." The event storm states lineage must be acyclic but does not specify the invariants that *enforce* acyclicity.

**Recommendation**: Add explicit invariants to the Subject Identity Resolver:
- Merge: `surviving_id.status MUST be 'active'`. `retired_id.status MUST be 'active'` (you cannot merge an already-retired subject).
- Split: `source_id` transitions to `archived` (terminal state — cannot become active, retired, or anything else).
- These two rules together guarantee acyclicity by construction.

---

### B5: Projection Rebuild Cost After Merge (200 Merged Subjects on Device)

**Stress question**: Surviving subject has 80 events, retired has 40. With 200 merged subjects, what is the total projection rebuild cost?

**Finding**:

**Single merge**: 80 + 40 = 120 events. If projection is a linear scan with aggregation, cost is O(120) — trivial.

**200 merged subjects on device**: This does not mean 200 merges happen at once. It means the device has 200 subjects in its alias table over the lifetime of the deployment. But on any given sync, the device may receive 0–10 merge events (depending on how active data cleanup is).

**Worst-case sync**: A data cleanup campaign merges 200 subjects in one batch (coordinator identifies 200 duplicates and merges them). Device receives 200 `SubjectsMerged` events in one sync. Each requires a projection rebuild.

- 200 rebuilds × 120 events average = 24,000 events processed.
- If each event takes 1ms to process on a low-end Android phone: 24 seconds.
- If each event takes 5ms (complex projection with nested references): 120 seconds.
- Constraints.md says sync should complete in "minutes, not hours." 120 seconds is borderline — and this is JUST the projection rebuild, not the event transfer itself.

**Additional concern**: Projection rebuild requires all events for both streams to be in local storage. If the device doesn't have all events for the surviving subject (because it wasn't in the device's sync scope), the local projection rebuild is incomplete. The device either:
- Requests the missing events (additional network transfer during sync — may fail on 2G).
- Builds a partial projection and marks it as incomplete (degraded but functional).
- Defers the projection rebuild until all events are available (the merged subject is temporarily unprojectable).

The model does not specify which approach to take.

**Severity**: **Weakens.** The mechanism works for small merge volumes but does not scale to batch merge operations on low-end devices with constrained connectivity. The dependency between projection rebuild and event availability is unspecified.

**Recommendation**: Specify:
1. Projection rebuild after merge is deferred until the device has all events for both streams. Until then, the subject is projected with a "partial — merge pending" marker.
2. The server can pre-compute and send a "merged projection" as a read model alongside the merge event, allowing the device to skip local rebuild entirely. This is a sync optimization that the model should explicitly allow.
3. Set a bound: "if a single sync yields more than N merge events, the device should request server-computed projections instead of rebuilding locally."

---

### B6: Unmerge — Events Recorded After Merge But Before Unmerge

**Stress question**: Walk through `SubjectsUnmerged`. Events recorded against `surviving_id` after the merge but before the unmerge — which subject do they belong to?

**Finding**: This is, as stated in the prompt, the hardest question.

Walk-through:

1. T1: `SubjectsMerged{surviving: Y, retired: X}`. X's events are aliased into Y's projection.
2. T2: Worker records Obs7 against Y. `{subject_id: Y, payload: {about: "the facility that used to be X"}}`.
3. T3: Worker records Obs8 against Y. `{subject_id: Y, payload: {about: "the original Y facility"}}`.
4. T4: `SubjectsUnmerged{subject_a: Y, subject_b: X, unmerged_by, rationale: "merge was incorrect — these are different facilities"}`.

**After unmerge**:
- X is restored as an independent subject. Its pre-merge events are returned to its own stream.
- Y continues as an independent subject. Its pre-merge events remain.
- **The problem**: Obs7 and Obs8 were recorded against Y during the merge window. They carry `subject_id: Y`. They are immutable. They cannot be rewritten to `subject_id: X`.

But Obs7 is actually about the facility that was X. It was attributed to Y only because X was merged into Y at the time. After unmerge, Obs7 should belong to X — but it *says* Y and it's immutable.

Obs8 is actually about Y. It correctly says Y. No problem.

**The system cannot automatically determine** which post-merge events on Y were "really about X" and which were "really about Y." This requires human judgment — the same kind of manual re-attribution that the event storm flagged as "extremely expensive at scale" for splits (S06, Act 3 hot spot).

**Possible states after unmerge**:
- **Option 1: All post-merge events stay on Y.** X gets back only its pre-merge events. Simple, automatable, but wrong — Obs7 is about the wrong subject.
- **Option 2: All post-merge events are flagged for manual re-attribution.** Correct, but if there are 50 events in the merge window, a human must review each one. At scale, this is the same unbounded-review problem as A3.
- **Option 3: Unmerge is banned.** A wrong merge is corrected by splitting Y into Y and X' (a new subject cloned from X), with future events going to the right one. Historical post-merge events on Y are annotated but not moved. This avoids the re-attribution problem but means X's identity is gone forever (replaced by X').

**Severity**: **Breaks.** The `SubjectsUnmerged` event is structurally problematic. Immutability of events means post-merge events cannot be re-attributed. The only correct solution requires human review of every post-merge event, which is operationally infeasible at scale. The mechanism as stated does not define what happens to post-merge events, making `SubjectsUnmerged` an incomplete specification.

**Recommendation**: ADR-2 should strongly consider **not supporting unmerge** as a symmetric reverse of merge. Instead:
- A wrong merge is corrected by a **corrective split**: the surviving subject is split, creating a new successor for the entity that should not have been merged. Historical events during the merge window are flagged for optional re-attribution but default to staying on the surviving subject.
- The `SubjectsUnmerged` event type should be removed or redefined as "corrective split with lineage annotation."

---

## 3. Mechanism C Findings (Device-Sequence + Sync-Watermark)

**Mechanism under test**: Causal ordering uses (1) a device sequence — monotonically increasing counter per device, giving total ordering within a device, and (2) a sync watermark — each event carries the last-known server state version the device had seen, enabling staleness detection.

---

### C1: Two Events, Same Watermark, Conflicting State Changes — Who Wins?

**Stress question**: Two events on different devices with the same sync watermark — can the system determine which one "should win"?

**Finding**: No. And this is by design — but the implications are not fully specified.

Device A and Device B both last synced at server version V100. Both create state-changing events on subject X. Both events carry `sync_watermark: V100`. The system can determine:
- They are concurrent (neither causally depends on the other — same watermark means neither saw the other's events).
- They are both "equally stale" relative to the server.

The system **cannot** determine:
- Which one "should win." The device sequence is per-device, not cross-device. A's sequence 47 and B's sequence 23 are incomparable.
- Temporal priority. `device_time` is unreliable (clock drift, time zones, battery resets). Even if A's timestamp is 10:00 and B's is 10:30, the system cannot trust this ordering.

**The mechanism correctly identifies concurrency** — that's its job. But it punts "who wins" to human judgment (via the Conflict Resolver). For additive conflicts (two observations), this is fine — both are kept. For state conflicts (deactivation vs. observation, two different status changes), a human must always decide. There is no automated resolution path.

**Is this acceptable?** For the stated design philosophy (P5: "Conflict is surfaced, not silently resolved"), yes. But it means **every concurrent state change requires human review**. In a deployment with 10,000 workers and hundreds of subjects, the volume of concurrent state changes could be significant, especially after a connectivity outage that forces mass sync.

**Severity**: **Holds.** The mechanism correctly identifies concurrency. It does not claim to resolve it — that's by design. But the operational cost of human resolution for every concurrent state change must be acknowledged.

**Recommendation**: No change to the mechanism. But ADR-2 should specify clearly: "concurrent state changes are ALWAYS human-resolved. The platform does not implement automatic conflict resolution for state changes. Volume management requires batch resolution tools (see A3 findings)."

---

### C2: Two Weeks Offline, 200 Events, Batch Conflict Detection

**Stress question**: Device offline for 2 weeks, 200 events, evaluated against 2 weeks of server changes.

**Finding**: Walk-through:

1. Device last synced at server version V100.
2. Over 2 weeks, the server processes events up to V5000 (4,900 new events from other devices).
3. The device creates 200 events, all with `sync_watermark: V100`.
4. On sync, the server receives 200 events.

**Conflict detection process**:
For each of the 200 incoming events, the server must:
1. Identify the subject_id.
2. Load the subject's event stream from V100 to V5000 (the events the device didn't know about).
3. Determine if any of those events conflict with the incoming event.

**Computational cost**:
- If the 200 events span 50 different subjects: average 4 events per subject to process.
- For each subject, the server loads events between V100 and V5000 that reference that subject. If each subject has an average of 10 events in that window: 50 × 10 = 500 comparisons.
- For each comparison, determine conflict type (concurrent state change, stale reference, etc.).
- This is O(N × M) where N = incoming events and M = per-subject events in the staleness window. For 200 × 10 = 2,000 comparisons: trivial.

**But the worst case is worse**: A single subject could have 4,900 events in the staleness window (all other devices were working on the same subject). 200 incoming events × 4,900 comparisons = 980,000 comparisons. This is still computationally feasible on a server but no longer trivial.

**The real problem is not computation but output volume**: If 50 of the 200 events are flagged (25% rate on a 2-week stale watermark is conservative), the reviewer gets 50 new flags from a single sync. Multiply by the number of devices that were offline simultaneously (after a regional connectivity outage, could be hundreds), and the flag volume is the same scaling problem identified in A3/A6.

**Severity**: **Holds.** Batch conflict detection is computationally feasible. The mechanism itself works. The downstream problem (flag volume) is an accept-and-flag problem, not a watermark problem.

**Recommendation**: No change to the mechanism. The server should process conflict detection in batch (all 200 events in one pass) rather than event-by-event, to amortize the subject-stream loading cost.

---

### C3: Device Clock Reset — What Breaks?

**Stress question**: Phone battery dies, clock resets to a default date. Device sequence is still valid. What breaks?

**Finding**:

**What survives**:
- `device_id`: Unchanged. Identifies the device.
- `device_sequence`: Persisted in local storage, survives reboot. The monotonic counter continues correctly.
- `sync_watermark`: Persisted in local storage, survives reboot. Still accurately reflects the last-known server version.

**What breaks**:
- `device_time` in the event envelope: Now wrong. Could be January 1, 2000, or any default.
- The event storm says `device_time` is recorded as "when the action was actually performed."

**How is `device_time` used?**

1. **Display**: "This observation was recorded at [time]." Wrong time is confusing for reviewers but not structurally damaging.
2. **Projection ordering**: The event storm says "events are ordered by device_time" in the Subject Stream aggregate. If `device_time` is used for ordering events within a projection, a clock-reset event with a year-2000 timestamp would sort BEFORE all other events in the stream. The projection could show this very recent observation as the oldest event. This corrupts the projection's temporal view.
3. **Conflict detection**: The sync watermark, not `device_time`, is used for staleness detection. So conflict detection is unaffected.
4. **Audit trail**: Constraints.md requires "who did what, when." A wrong `device_time` makes the audit record unreliable for the "when" dimension.

**Is `device_time` cosmetic or structural?** ADR-1 says: "every event records when the action was actually performed (device time, not sync time)." This is an envelope guarantee. But the event storm uses `device_time` for projection ordering ("events are ordered by device_time"). So `device_time` is structural for projections, not just cosmetic.

**Severity**: **Weakens.** The ordering mechanism (`device_sequence + sync_watermark`) survives clock reset. But `device_time`, which is used for projection ordering and display, is corrupted. The model does not distinguish between `device_time` (unreliable, for display/audit) and `device_sequence` (reliable, for ordering). Projections should be ordered by `device_sequence` within a device, not by `device_time`.

**Recommendation**: ADR-2 must specify:
- `device_time` is advisory — used for display and audit, not for ordering or conflict detection.
- Projection ordering within a single device's events uses `device_sequence`, not `device_time`.
- Cross-device ordering within a projection uses `sync_watermark` to group events by knowledge epoch, with `device_time` as a secondary hint (best-effort, not trusted).
- If a `device_time` is implausible (before the device's first-ever sync, or more than 48 hours in the future), the event is accepted but annotated with a "clock anomaly" flag.

---

### C4: Two Devices, Same Actor (Phone Sharing)

**Stress question**: Worker A uses Device 1 in the morning, Device 2 in the afternoon. Events carry the same actor_id but different device_ids.

**Finding**:

Events from Device 1: `{actor_id: A, device_id: D1, device_seq: 1, 2, 3, ...}`
Events from Device 2: `{actor_id: A, device_id: D2, device_seq: 1, 2, 3, ...}`

**Are these correctly identified as sequential work by the same person?**

The `actor_id` is the same, so the system can attribute all events to Worker A. The `device_id` differs, so the event streams are separate. The device sequences are independent — D1 seq 3 and D2 seq 1 have no ordering relationship.

**What the system sees**: Two concurrent event streams, both authored by Worker A, from different devices. The system **cannot distinguish** this from:
- Worker A and Worker B sharing a device (different actor_ids, same device_id — easy to distinguish via actor_id).
- Worker A genuinely using two devices simultaneously (e.g., tablet for one task, phone for another).
- Worker A's account credentials being used by someone else on a second device (credential sharing — the system cannot detect this from the data model alone).

**Is this a problem?** Only if:
- Business rules require "one device per worker" (phone sharing violates this).
- Ordering across Device 1 and Device 2 matters (the system cannot establish it from device sequences alone — it would need `sync_watermark` if both devices synced between morning and afternoon use, or `device_time` as a hint).

**The sync watermarks may help**: If Worker A syncs Device 1 at noon, then starts using Device 2, Device 2's events will carry a sync watermark ≥ Device 1's last sync. This establishes a causal order: Device 2's events causally follow Device 1's last sync (and therefore all of Device 1's events that were synced). But if Device 1 was never synced between morning and afternoon (Worker A just switched phones), the watermarks provide no ordering.

**Severity**: **Holds.** The mechanism correctly tracks that the same person authored events on two devices. Cross-device ordering is a best-effort reconstruction from watermarks and device_time. This is an inherent limitation of a device-sequence-based ordering model, not a bug.

**Recommendation**: No change to the mechanism. Document the limitation: "events from the same actor across different devices are not totally ordered unless the actor syncs between device changes. This is accepted as a consequence of the offline-first model."

---

### C5: Detecting the S19 Act 2 Conflict (Deactivation at 11:00, Observation at 11:30)

**Stress question**: Can this mechanism detect the specific conflict between deactivation on Device A and observation on Device B?

**Finding**: Walk-through:

1. Device A deactivates subject X at 11:00AM. Event: `SubjectDeactivated{subject_id: X, device_id: DA, device_seq: 47, sync_watermark: V100, device_time: 11:00}`.
2. Device B records observation on subject X at 11:30AM. Event: `ObservationRecorded{subject_id: X, device_id: DB, device_seq: 23, sync_watermark: V100, device_time: 11:30}`.

Both devices have `sync_watermark: V100` — they both last synced at the same server version. Neither knew about the other's action.

**Can the system detect this is a conflict?**

The Conflict Detector receives both events on sync. It can determine:
- Both reference subject X.
- Both have the same sync watermark (concurrent — neither causally depends on the other).
- One is a state change (deactivation). The other is an observation on the now-deactivated subject.

**Yes, the mechanism detects this.** The detection rule is: "if an event arrives with a sync_watermark that is before the subject's deactivation, and the event's type is incompatible with deactivation (e.g., an observation on an inactive subject), flag it."

The `device_time` (11:00 vs. 11:30) is irrelevant for detection — the sync watermarks are what matter. Even if both events had the same `device_time`, the system would still detect the conflict because both are concurrent (same watermark) and one is a state change that invalidates the other.

**What the mechanism CANNOT determine**: Which one "should win" — should the deactivation stand (and the observation is flagged), or should the observation stand (and the deactivation is questioned)? This requires human judgment.

**Severity**: **Holds.** The mechanism correctly detects this conflict using sync watermarks to identify concurrency.

---

## 4. Combination Test Results

### Scenario Alpha: Assignment Transfer During Offline Merge

**Setup**:
1. Supervisor transfers Worker A from Zone 1 to Zone 2 (online). Events: `ResponsibilityTransferred{from_actor: A, old_scope: Zone1, new_scope: Zone2}`.
2. Data manager merges Subject X (in Zone 1) with Subject Y (in Zone 2). Events: `SubjectsMerged{surviving: Y, retired: X}`.
3. Worker A is offline through both changes.
4. Worker A records observation against Subject X under their Zone 1 assignment. Event: `ObservationRecorded{subject_id: X, recorded_by: A, under_assignment: old_assignment_id}`.
5. Worker A syncs.

**Walk-through of every mechanism that fires**:

**Step 1 — Alias table (Mechanism B)**: The event references `subject_id: X`. X is retired. The alias table resolves `X → Y`. For projection purposes, the observation enters Y's stream. The event itself is unchanged (immutability) — it still says `subject_id: X`.

**Step 2 — Conflict detection for stale watermark (Mechanism C)**: Worker A's sync watermark is from before both the transfer and the merge. The Conflict Detector identifies: this event was created with stale state knowledge. At minimum, two stale conditions exist:
- The subject referenced (X) has been retired (merge).
- The assignment referenced (old_assignment_id, Zone 1) has been revoked (transfer).

**Step 3 — Accept-and-flag (Mechanism A)**: The event is accepted (immutability). Two flags are generated:
- Flag 1: `ConflictDetected{type: "stale_reference", detail: "subject X retired, merged into Y"}`.
- Flag 2: `ConflictDetected{type: "revoked_authority", detail: "assignment to Zone 1 revoked, now assigned to Zone 2"}`.

**What does the final state look like?**

- The observation event is in the system, immutable, referencing retired X under revoked assignment.
- The projection for Y includes this observation (via alias).
- Two flags are in the reviewer queue.

**Can the reviewer untangle this?**

The reviewer sees:
- An observation about Subject Y (nee X) by Worker A.
- Flag 1: "Worker A referenced subject X, which is now merged into Y."
- Flag 2: "Worker A's assignment to Zone 1 has been revoked; they're now in Zone 2."

**The irony**: After the merge, Subject Y is in Zone 2. Worker A is now assigned to Zone 2. The observation is actually about a subject that is now in Worker A's new zone. If the reviewer understands the full context, they may accept the observation as valid — the worker was observing a subject that *would have been* in their scope under the new assignment, even though at the time they were recording under the old assignment against the old identity. This is a case where two "errors" cancel out, but only a human who understands the full picture can see that.

**What if the reviewer doesn't understand the full picture?** They see two flags and might reject the observation, losing valid data. Or they might accept one flag and reject the other, creating an inconsistent state.

**What breaks**: Nothing structurally breaks. All three mechanisms fire correctly. But the reviewer's cognitive load is high, and the flag presentation does not help them see the "cancellation" pattern. The flag read model does not link related flags or show their combined context.

**Recommendation**: The flag read model should group flags by root event and show them together. When multiple flags reference the same event, the reviewer should see them as a single compound anomaly, not as independent items.

---

### Scenario Beta: Split + Concurrent Offline Observations

**Setup**:
1. Subject X is split into X1 and X2 (online). `SubjectSplit{source: X, successors: [X1, X2]}`.
2. Worker A (offline) records 3 observations against X: Obs1, Obs2, Obs3.
3. Worker B (online) records 2 observations against X1: Obs4, Obs5.
4. Worker A syncs.

**Where do Worker A's 3 observations go?**

The split model says: "Historical events under source_id are NOT re-attributed. They remain as-is." X is now archived. But Worker A's observations are *new* events referencing X — they are not historical events frozen from before the split.

**The alias table does not help here**: X was NOT merged into another subject. X was split. The alias table stores `retired_id → surviving_id` for merges. For splits, there is no alias — X has *two* successors (X1 and X2), so there is no single target to alias to.

**What happens**:
1. Worker A's events reference X.
2. X is in "archived" state (post-split).
3. The Conflict Detector flags all 3 events: "stale reference — subject X has been split into X1 and X2."
4. The events are accepted and stored (immutability).
5. They remain referencing X in perpetuity.

**For projection purposes**: Where do they appear? Three options:
- **Option a**: In X's archived stream. X's projection shows its pre-split history plus these 3 post-split observations. This is technically correct (the events reference X) but misleading — X is archived.
- **Option b**: Nowhere — they are "orphaned" until a reviewer attributes them. They exist in the event store but are not projected into any active subject's view. This loses visibility.
- **Option c**: Flagged as pending attribution, visible in a special "unattributed observations" queue.

**The reviewer must manually attribute each observation to X1 or X2.** But as the prompt notes, the observations might contain information indicating they're about X2's physical location. The reviewer needs domain context to make this determination. If the observation payload says "GPS: [coordinates near X2]", a human can make the attribution. If it says "temperature: 37.2", there's no way to determine attribution without physically visiting the location.

**What if attribution produces ambiguity?** Obs1 might be about X1, Obs2 about X2, and Obs3 ambiguous. Attribution is per-event, not per-batch. This is the "extremely expensive at scale" problem flagged in the event storm (S06, Act 3).

**What breaks**: The alias table is not designed for splits (it's one-to-one, not one-to-many). Post-split events referencing the archived source have no automatic resolution path. Every one requires human attribution.

**Recommendation**: ADR-2 must specify a different handling pattern for post-split events:
- Events referencing an archived (split) subject are accepted and stored under the archived subject's stream.
- They are flagged with a specific type: "post-split reference — requires attribution."
- The read model shows them in a special queue, grouped by source subject, with the successor subjects' attributes displayed for comparison.
- The reviewer can attribute each event to a successor or leave it on the archived subject.
- Events left on the archived subject remain part of the historical record but are not projected into any active subject's view.

---

### Scenario Gamma: Shipment Receipt Offline with Identity Collision

**Setup**:
1. Warehouse ships S1 (online). `ShipmentInitiated{shipment_id: S1, items: [{vaccine_A, 500}], to: district_D}`.
2. Receiver at district_D is offline.
3. Receiver creates a "pending receipt" with no shipment_id: `ReceiptRecorded{receipt_id: R1, items: [{vaccine_A, 480}], received_at: T1}`.
4. Warehouse ships S2 to the same receiver: `ShipmentInitiated{shipment_id: S2, items: [{vaccine_A, 300}], to: district_D}`.
5. Receiver syncs. Now has S1 and S2 to match against R1.

**How does matching work?**

The Pending Match aggregate (identified as a candidate in the event storm) must resolve `R1 → S1 or S2`. Information available:
- R1: 480 units of vaccine_A, received at T1.
- S1: 500 units of vaccine_A, dispatched (presumably before T1).
- S2: 300 units of vaccine_A, dispatched (presumably after R1 was created, since receiver didn't know about S2).

**Automatic matching attempt**:
- R1 (480 units) is closest to S1 (500 units) by quantity. Likely match: R1 → S1.
- But what if S1 was 490 and S2 was 470? The quantities are ambiguous. Automatic matching confidence is low.
- What if both shipments contained exactly 500 units? Quantity provides no signal.

**Temporal signal**:
- If S1 was dispatched at T0 and S2 at T2 (after receiver went offline), and T0 < T1 < T2, then R1 was created between S1's dispatch and S2's dispatch. S1 is the more likely match (it arrived before R1 was created). But `device_time` (T1) is unreliable (C3 finding).

**What if the receipt attributes are genuinely ambiguous?**
- The Pending Match aggregate flags R1 as "ambiguous match — multiple candidate shipments."
- A human must resolve it.
- Until resolution, the receipt is "pending" — not attributed to any shipment.
- Both S1 and S2 show "pending receipt" in their projections.
- If the resolver picks wrong (R1 → S2 instead of S1), S1 has no receipt, and the subsequent investigation reveals the mismatch. This is correctable but expensive.

**What breaks**: The pending match pattern works for single-candidate matches but has no automated resolution for multi-candidate matches. This is not specific to shipments — any event that needs to be linked to an identity the device didn't have presents the same problem. The general pattern is: "an event with an unresolved identity reference enters a pending state and requires either automatic matching (if confidence is high) or human resolution (if ambiguous)."

**Additional hazard**: While R1 is pending, the receiver might create *another* receipt R2 (for S2, which they now know about after sync). If R1 and R2 are both pending, and the system tries to match both simultaneously, it needs to enforce: "each shipment matches at most one receipt." This is a constraint across the matching problem, not just per-receipt. The Pending Match aggregate does not specify this constraint.

**Severity**: **Weakens.** The mechanism works for the simple case (one receipt, one shipment). It breaks down for the multi-candidate case and does not specify the constraint that matching must be bijective (one receipt per shipment, one shipment per receipt at each level).

**Recommendation**:
- The Pending Match aggregate must enforce bijective matching: "each shipment matches at most one receipt at each handoff level."
- Multi-candidate matches are flagged for human resolution.
- The read model shows candidate matches ranked by confidence (quantity similarity, temporal proximity), but never auto-resolves below a configurable confidence threshold.
- The pending match pattern should be specified as a **general mechanism** (not shipment-specific) for any event that references an identity the device doesn't yet know about.

---

## 5. Invariant Survival Report

| # | Aggregate | Stated Invariant | Can I Break It? | How | Severity |
|---|-----------|-----------------|-----------------|-----|----------|
| 1 | **Event Store** | Events immutable, IDs unique, sole write path | **No** | Tried: creating events that retroactively modify the store via merge/unmerge. The immutability holds because merge/split/unmerge are all new events, not modifications. UUID collision is statistically negligible. Write-path discipline is an architectural commitment, not a runtime invariant — it can only be broken by implementation bugs, not by workflow paths. | N/A |
| 2 | **Projection Engine** | Current state = f(events), rebuildable | **Yes** | After an unmerge (B6), events recorded during the merge window reference the surviving_id but "belong to" the retired subject. The projection cannot correctly attribute them without human input. The projection function is no longer purely deterministic — it depends on resolution decisions that may not yet exist. Until all post-merge events are manually re-attributed, the projection for both subjects is wrong. | **High** |
| 3 | **Subject Registry** | Subjects have UUID identity, lifecycle states, required attributes at creation | **Partially** | Tried: creating a subject with missing required attributes offline. Assuming the device enforces attribute validation, this holds. But: if the Shape Registry changes while the device is offline (new required field added), events created under the old shape are valid per the old rules but would fail validation under the new rules. The invariant holds per-shape-version but its meaning changes across versions. Not a break — the invariant is scoped correctly — but a source of confusion. | **Low** |
| 4 | **Subject Identity Resolver** | Merge: one surviving_id. Lineage transitive, acyclic. | **Conditionally** | Acyclicity holds IF the aggregate enforces: surviving_id must be active, archived subjects cannot be targets of merge (see B4). Without these explicit enforcement rules, a cycle is constructable: archive a subject via split, then merge a successor back into it. The stated invariant ("acyclic") is correct in intent but missing the enforcement rules that guarantee it. | **High** |
| 5 | **Conflict Detector** | Concurrent events on same subject always evaluated. Detection deterministic. Conflicts never silently resolved. | **No** | Tried: crafting a scenario where concurrent events escape detection. The sync watermark mechanism ensures that any event with a watermark older than the subject's latest state change is evaluated. The determinism holds because detection rules are purely mechanical (watermark comparison, lifecycle state check). The "never silently resolved" invariant holds because resolutions are always separate events. I could not break this. | N/A |
| 6 | **Conflict Resolver** | Every resolution is an event. References the conflict. Only authorized actors resolve. | **Yes** | Two authorized actors can independently resolve the same conflict offline (A2). Both resolutions are events. Both reference the conflict. Both actors are authorized. The aggregate does not enforce "at most one resolution per conflict." This produces a meta-conflict with no termination condition. | **Critical** |
| 7 | **Assignment Registry** | Binds actor to scope with temporal bounds. Enforced on-device against local state. | **Partially** | Enforcement on-device against *local* state means: if the assignment was revoked after the device's last sync, the device cannot enforce the revocation. Events under revoked assignments are accepted and flagged (Mechanism A). The invariant holds from the device's perspective (it enforces against what it knows) but not from the system's perspective (the assignment is revoked). This is accepted by design (accept-and-flag), so the invariant is accurate but must be read as "enforced against local state, which may be stale." | **Low** |
| 8 | **Actor Identity** | One canonical ID per person. Multiple simultaneous assignments. | **No** | Tried: crafting scenarios where actor identity diverges. Phone sharing (C4) means different devices carry the same actor_id, but identity is per-person, not per-device. Credential theft could create events under a stolen actor_id, but that's a security concern outside the identity model's scope. The invariant holds within the model's boundaries. | N/A |
| 9 | **Shipment** | Process-scoped identity persisting across handoffs. Sequential state progression. | **Partially** | The sequential state progression (initiated → dispatched → received) can be violated if a receiver offline creates a receipt before the dispatch event has been recorded. Result: `ShipmentReceived` event exists but `ShipmentDispatched` doesn't (yet). The invariant "sequential" is violated in the sync timeline (receipt arrives before dispatch). It holds in the *logical* timeline (dispatch happened before receipt in the real world). The projection must use causal ordering to reconstruct the correct sequence. If it uses arrival order, it's wrong. | **Medium** |
| 10 | **Causal Ordering** | Distinguishes before/after/concurrent for events on same subject. | **Partially** | The device-sequence + sync-watermark mechanism cannot distinguish ordering for events from *different devices* that share the same watermark — they are all "concurrent." This is by design; the mechanism defines the limits of what can be known. But it means: for a subject with 10 concurrent events from 10 devices, the system identifies 45 pairs as "concurrent" but cannot determine any ordering among them. The invariant holds (it correctly identifies concurrency) but provides less information than vector clocks would. Within a single device, ordering is fully determined. | **Low** |
| 11 | **Shape Registry** | Shape versions ordered. Old shapes never deleted. Events carry version. | **No** | Tried: creating a scenario where old shapes become unavailable. Since shapes are never deleted (append-only, matching the event store model), and events carry their shape version, historical events always have their shape available. Projection logic must handle multi-version events, but the invariant itself holds. | N/A |
| 12 | **Pending Match** (candidate) | Unlinked event remains pending until matched. Matching automatic or manual. | **Yes** | The invariant does not specify bijective matching (Scenario Gamma). One receipt could match multiple shipments, or multiple receipts could match one shipment, with no constraint preventing this. The "pending until matched" part holds, but "matched" is under-constrained. | **Medium** |

---

## 6. Missing Workflow Paths

The following workflow paths were discovered during stress-testing that are not covered by the event storm's event catalog or conflict taxonomy:

### M1: Meta-Conflict Resolution
**Path**: ConflictDetected → ConflictResolved (by Reviewer A) + ConflictResolved (by Reviewer B, offline, different resolution) → ?
**Gap**: No event type for "conflicting resolutions." No workflow for resolving the meta-conflict. No termination condition.

### M2: Flag Re-evaluation After Identity Change
**Path**: ConflictDetected (flag on subject X) → SubjectsMerged (X into Y) → Flag still references X → Reviewer opens flag → Context has changed
**Gap**: No event or policy for re-evaluating / annotating flags when their referenced subject undergoes identity changes.

### M3: Downstream Work Cancellation
**Path**: Event accepted → Policy triggers downstream work (assignment, review, allocation) → ConflictDetected on the original event → ConflictResolved as "reject" → Downstream work is now based on rejected event
**Gap**: No cascade mechanism from conflict resolution to downstream events. No event type for "downstream work invalidated due to upstream flag resolution."

### M4: Post-Split Event Attribution
**Path**: SubjectSplit → Offline device creates events referencing archived source → Sync → Events cannot be auto-attributed to successors
**Gap**: No event type for "manual attribution of post-split events." The `ConflictDetected` type exists but the resolution for this specific case (attributing to a successor) requires a different action than standard conflict resolution — it requires creating a new event that links the original event to a successor subject.

### M5: Unmerge Event Attribution
**Path**: SubjectsMerged → Events created on surviving_id during merge window → SubjectsUnmerged → Events on surviving_id that were actually about the retired subject
**Gap**: No mechanism for retroactive attribution. The event catalog has `SubjectsUnmerged` but no corresponding "re-attribute event to unmerged subject" event type.

### M6: Pending Match Timeout
**Path**: Offline receipt created → Sync → No matching shipment found → Receipt remains pending indefinitely
**Gap**: No timeout, escalation, or fallback workflow for pending matches that are never resolved. An unmatched receipt could persist in the pending queue forever. There is also no workflow for "create shipment retroactively to match a receipt" (the reverse direction — the field reality drives the record creation, rather than the planned shipment).

### M7: Bulk Operation Conflict Amplification
**Path**: Coordinator performs bulk reclassification (200 subjects) → Offline devices sync → 200 × N events flagged
**Gap**: The conflict taxonomy identifies "stale reference" but has no specific handling for conflicts caused by bulk operations. Bulk operations should generate a single "bulk change" context that allows flag grouping, but no such mechanism is specified.

### M8: Device Sequence Reset
**Path**: Device is factory-reset or replaced. New device is provisioned for the same field worker. Device sequence restarts from 0. Events from the old device (sequence 1–500) and new device (sequence 1–N) overlap.
**Gap**: The model does not specify what happens when a device sequence namespace is reused. If `device_id` changes with the new device, there is no problem (new namespace). If `device_id` is tied to the user account rather than the hardware, sequence numbers collide. The boundary between device identity and user identity is not defined.

---

## 7. Assumptions Register

| # | Assumption | Where Verified | Risk if Wrong |
|---|-----------|---------------|---------------|
| A1 | Flags (ConflictDetected events) are created server-side only, not device-side | Not verified — event storm is ambiguous about the Conflict Detector's execution location | If device-side flags are possible, duplicate flag deduplication is needed (A1 finding) |
| A2 | Device sequence numbers are persisted to durable storage and survive device reboots and app restarts | Not verified — no spec for device storage implementation | If device_seq is in-memory only, reboot resets the counter, breaking intra-device ordering |
| A3 | Sync watermark is persisted to durable storage on the device | Not verified — same as A2 | If watermark is lost, all post-reboot events have no staleness context. The server cannot detect their actual staleness. |
| A4 | The alias table is maintained on each device locally after processing merge events | Not verified — the event storm says "devices receive the merge event on sync and update their local alias table" but does not specify persistence or crash recovery | If the alias table is lost, device queries return incorrect subjects (retired IDs are not resolved). Requires reprocessing all merge events to rebuild. |
| A5 | Projection rebuild after merge is incremental (append retired events to existing projection), not full (discard and recompute) | Not verified — projection rebuild strategy is not specified | If full rebuild is required, merge cost on low-end devices may be prohibitive (B1/B5 findings) |
| A6 | The Subject Identity Resolver enforces that surviving_id must be in 'active' state and archived subjects cannot be merge targets | Not verified — the aggregate's validation rules for merge targets are not specified | If not enforced, cyclic lineage graphs are constructable (B4 finding) |
| A7 | Conflict resolution is single-writer (only one resolver per conflict) | Not verified — the model does not specify concurrency control on conflict resolution | If multiple reviewers can resolve the same conflict independently, meta-conflicts arise (A2 finding) |
| A8 | `device_time` is used only for display/advisory purposes, not for structural ordering or conflict detection | Not verified — the event storm says "events are ordered by device_time" in Subject Stream, suggesting structural use | If device_time is structural, clock resets corrupt ordering (C3 finding) |
| A9 | Device identity (device_id) is tied to hardware, not to user account, and changes when a device is replaced | Not verified — the model does not define the relationship between device_id and hardware vs. account | If device_id is per-account, device replacement does not change device_id, and sequence numbers from old and new hardware collide (M8) |
| A10 | The pending match pattern applies only to shipments, not to other event types | Not verified — the event storm identifies it as a candidate aggregate "that may be a general pattern beyond shipments" but does not commit | If it's a general pattern, every aggregate that can receive events with unresolved identity references needs a pending match mechanism. This significantly expands the design scope. |
| A11 | Split successor IDs are server-generated, not client-generated | Not verified — the SplitSubject command is described as a coordinator action (presumably online), but the event storm uses "Creates two new subject_ids" without specifying where | If successors can be created offline, two coordinators could split the same subject into different successors simultaneously, creating identity collision at the split level |
| A12 | The Conflict Detector runs synchronously during sync processing, before any policies fire on the received events | Not verified — the ordering of sync processing steps is not specified | If policies fire before conflict detection, downstream work is created on potentially flagged events (A5 finding) |

---

## 8. Verdict

### Mechanism A: Accept-and-Flag
**Verdict: HOLDS WITH MODIFICATIONS**

The core principle is sound: immutable events are preserved, anomalies are surfaced as separate events, resolution is itself an event. This aligns with P3 (append-only) and P5 (conflict is surfaced, not silently resolved).

**Required modifications**:
1. **Conflict resolution must be single-writer** — one designated resolver per conflict, preventing meta-conflicts (A2).
2. **Flag generation must carry structured root-cause metadata** — enabling batch resolution and grouping (A3).
3. **Batch resolution must be supported** — "resolve all flags with root cause X as [accepted]" (A3, A6).
4. **Sync processing must run conflict detection before policies fire** — preventing downstream work on flagged events (A5).
5. **Auto-resolution policies for low-severity flags** must be configurable per deployment to prevent unbounded backlog growth (A6).

### Mechanism B: Alias Table in Projection
**Verdict: HOLDS WITH MODIFICATIONS**

The alias table correctly preserves immutability while enabling identity evolution in projections. The split model (freeze historical events under source) is a defensible simplification.

**Required modifications**:
1. **Eager transitive closure** for alias chains — A→C, not A→B→C (B3).
2. **Acyclicity enforcement rules** must be explicit in the Subject Identity Resolver aggregate (B4).
3. **Unmerge (`SubjectsUnmerged`) should be replaced** with a corrective-split model to avoid the unsolvable post-merge event attribution problem (B6).
4. **Post-split events** referencing archived subjects need a dedicated attribution workflow (Scenario Beta, M4).
5. **Projection rebuild cost** on low-end devices must be bounded, with fallback to server-computed projections (B1, B5).

### Mechanism C: Device-Sequence + Sync-Watermark
**Verdict: HOLDS**

This is the most robust of the three mechanisms. It correctly identifies concurrency within its stated scope, is tolerant of clock drift, and is lightweight enough for low-end devices. Its principal limitation — inability to order events across devices with identical watermarks — is inherent to any mechanism simpler than vector clocks, and is acceptable given P5 (surface conflicts for human resolution).

**Required clarification**:
1. **`device_time` must be designated as advisory**, not structural. Ordering must use `device_sequence` (intra-device) and `sync_watermark` (inter-device) (C3).
2. **Device sequence and sync watermark persistence** must be guaranteed across reboots (A2, A3).

### Overall Verdict

**Can ADR-2 proceed with these three mechanisms as foundation? YES — with the modifications listed above.**

The three mechanisms form a coherent system: sync watermarks detect staleness, the alias table handles identity evolution, and accept-and-flag preserves data integrity while surfacing anomalies. None of the mechanisms is fundamentally broken. But each has operational gaps that will cause real problems at deployment scale (tens of thousands of workers, intermittent connectivity, millions of events) if left unaddressed.

**The most critical gaps to close before proceeding**:

1. **Single-writer conflict resolution** (A2) — without this, the system has no termination condition for disagreements between reviewers. This is a structural flaw, not an operational inconvenience.

2. **Replace unmerge with corrective split** (B6) — `SubjectsUnmerged` is structurally unsound given immutable events. Corrective split is the only clean alternative.

3. **Sync processing order: detect before act** (A5, A12) — if policies fire before conflict detection, downstream work cascades make flag resolution exponentially harder.

4. **Batch flag resolution** (A3, A6) — without this, the accept-and-flag mechanism collapses under real-world volumes. This is not a nice-to-have; it is a deployment prerequisite.

5. **Acyclicity enforcement in lineage graph** (B4) — the invariant is stated but not enforced. Add the two rules (surviving_id must be active, archived is terminal) to close the gap.

These five modifications are **concrete constraints** that Phase 3 should adopt as requirements for the ADR-2 decision.
