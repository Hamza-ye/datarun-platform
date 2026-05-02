# ADR-002 Identity And Conflict Kernel Staging

Status: Iteration 18 staging

This temporary staging file holds ADR-002 identity and conflict lineage kernels. It is not a final atomic document.

## Staged Kernels

## Kernel: ADR-002 Event Storm Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/05-adr2-event-storm-identity.md` is ADR-002 Phase 1 event discovery. It discovers domain events, commands, aggregates, identity touchpoints, conflict types, open questions, and hot spots across selected scenarios. It is raw exploration and does not make final ADR-002 decisions.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / supersession notice
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / opening phase, method, purpose, and output consumers
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `## Output Handoff to Phase 2`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-002 Phase 1 event storming.

Non-goals:

Does not decide ADR-002 identity model, conflict definition, resolution strategy, causal ordering mechanism, or aggregate boundaries.

Forbidden interpretations:

- Do not treat discovered events or commands as final platform event vocabulary.
- Do not treat proposed aggregates as accepted platform primitives.
- Do not treat hot-spot handling examples as closed policy.

Open edges:

Phase 2 stress testing, Phase 3 classification, and ADR-002 must close, revise, or reject these findings.

Platform specification note:

Use this source for identity/conflict lineage and pressure discovery, not final platform contracts.

## Kernel: Identity As Load-Bearing Event Reference

Status: Candidate
Kind: invariant

Specification statement:

Every event is about some real-world or operational thing, and identity is the mechanism by which events find that subject or process. Incorrect identity corrupts projections, assignment, conflict detection, and historical views.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `## Platform Vision Context`

Closure basis:

Candidate ADR-002 framing. Later ADR-002 sources must determine how identity is represented.

Scope:

Applies to event references to subjects, actors, assignments, processes, and other identity-bearing things.

Non-goals:

Does not decide identity granularity, reference schema, merge/split semantics, or external identifier handling.

Forbidden interpretations:

- Do not treat identity as a display attribute.
- Do not treat UUID assignment alone as sufficient identity semantics.

Open edges:

ADR-002 must close the identity model and reference semantics.

Platform specification note:

Use as rationale for treating identity as a core platform primitive area.

## Kernel: Identity Type Taxonomy Candidate

Status: Candidate
Kind: primitive

Specification statement:

The event storm discovers at least four identity types: subject identity for persistent real-world things, actor identity for people, process identity for transient operational processes, and assignment identity for temporal responsibility bindings. These types share UUID-based, event-referenceable, sync-survivable behavior, but differ in lifecycle and relationships.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `### Identity Taxonomy Discovered`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S01, S03, S07 scenario event storms

Closure basis:

Candidate taxonomy from Phase 1 event storm. ADR-002 must decide whether these are separate aggregates or one shared identity protocol.

Scope:

Applies to subject, actor, process, and assignment identity lineage.

Non-goals:

Does not decide whether supply items, org units, shipments, cases, or campaigns are represented as subjects, processes, types, or another construct.

Forbidden interpretations:

- Do not collapse all identity types into subject identity without later source support.
- Do not assume all identity types have the same lifecycle.

Open edges:

Identity granularity is explicitly an ADR-002 open question.

Platform specification note:

Use as candidate taxonomy to reconcile against later ADR-002 classification and decision sources.

## Kernel: Subject Identity Lifecycle Candidate

Status: Candidate
Kind: interaction-rule

Specification statement:

Subject identity can be created, referenced, have descriptive attributes updated, be deactivated, be merged, be split, or become ambiguous. Historical events remain linked to the identity that was current when they were recorded, while projections interpret lifecycle events to produce current views.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `## S01 — Entity-Linked Capture`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `## S06 — Registry Lifecycle`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `### Identity Map — S06`

Closure basis:

Candidate lifecycle model from event storm. Later ADR-002 sources must close merge/split/deactivation semantics.

Scope:

Applies to persistent subjects such as people, places, facilities, equipment, households, and similar real-world operational entities.

Non-goals:

Does not decide exact event names, lifecycle state machine, or whether historical events can ever be re-attributed.

Forbidden interpretations:

- Do not delete or rewrite historical subject events during lifecycle changes.
- Do not equate attribute mutation with identity replacement.

Open edges:

Merge reversal, split attribution, deactivation under offline work, and post-merge sync behavior remain open.

Platform specification note:

Use as candidate lifecycle lineage for subject identity.

## Kernel: Identity Merge And Split Lineage Candidate

Status: Candidate
Kind: invariant

Specification statement:

Merge and split operations should preserve lineage rather than rewrite historical events. Merge maps retired IDs to a surviving ID for projection; split records source-to-successor lineage. The event storm proposes that merge lineage is transitive and split/merge lineage should be acyclic.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S01 `Act 4: Duplicate Detected and Merged`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S06 `Act 3: Subject Split`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S01 and S06 aggregate tables

Closure basis:

Candidate invariant from event storm. ADR-002 must close aliasing, split attribution, and reversal semantics.

Scope:

Applies to subject identity merge/split lineage and may generalize to process identities only if later sources carry it forward.

Non-goals:

Does not decide whether a surviving ID is selected or a new canonical ID is created; does not decide where alias mapping lives or how it syncs.

Forbidden interpretations:

- Do not physically re-reference historical events unless a later ADR explicitly permits it.
- Do not assume a merge is irreversible without later source support.

Open edges:

Alias table location, sync behavior, unmerge, and manual re-attribution remain open.

Platform specification note:

Use as lineage for identity-resolver and alias/projection semantics.

## Kernel: Conflict Taxonomy Candidate

Status: Candidate
Kind: primitive

Specification statement:

The event storm identifies conflict categories: concurrent additive work, concurrent state change, duplicate identity, stale reference, content mismatch, revoked authority, and cross-lifecycle event conflicts. Not all concurrent events are conflicts; conflict depends on identity, causality, lifecycle state, and business rules.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S19 event storm
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `### Conflict Type Taxonomy`

Closure basis:

Candidate conflict taxonomy from event storm.

Scope:

Applies to ADR-002 conflict definition and later owning ADR areas where conflict types intersect authorization, configuration, and workflow.

Non-goals:

Does not decide which conflict categories are platform-fixed, configurable, automatically resolvable, centrally resolved, or user-visible.

Forbidden interpretations:

- Do not treat every pair of concurrent same-subject events as a conflict.
- Do not silently resolve genuine state or identity conflicts.

Open edges:

ADR-002 must decide conflict definition boundary, resolution authority, and causal ordering mechanism.

Platform specification note:

Use as candidate taxonomy for conflict handling and flag semantics lineage.

## Kernel: Accept-And-Flag Stale Identity Work Candidate

Status: Candidate
Kind: interaction-rule

Specification statement:

Events created offline against stale identity or assignment state are factually valid because they happened, but may be invalid relative to current lifecycle or authority state. The event storm repeatedly suggests accepting such events and surfacing flags for review instead of rejecting or rewriting them.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S06 `Act 2: Subject Deactivation`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S19 `Act 4: Stale State Sync`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S03 `Act 3: Assignment Transfer`

Closure basis:

Candidate interaction rule from event storm. Later ADR-002 and ADR-003 sources must decide acceptance, flag lifecycle, and authority handling.

Scope:

Applies to events against deactivated, merged, split, reclassified, or assignment-revoked state during offline work.

Non-goals:

Does not decide severity, blocking behavior, rejection policy, reviewer role, or flag semantics.

Forbidden interpretations:

- Do not reject stale offline work by default from this event storm alone.
- Do not treat accepted stale work as clean or unproblematic.

Open edges:

ADR-002 must close identity conflict classification; ADR-003 must close stale authorization/access handling.

Platform specification note:

Use as lineage for offline stale-state reconciliation.

## Kernel: Causal Ordering Mechanism Open

Status: Open
Kind: open-question

Specification statement:

ADR-002 must choose a causal ordering mechanism sufficient to distinguish before, after, and concurrent events where needed. Device time alone is insufficient. Candidate mechanisms include hybrid logical clocks, vector clocks, device sequence plus sync watermark, and explicit predecessor references for causal dependencies.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `### Causal Ordering Discovery`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `### Questions for ADR-2` / `Causal Ordering`

Closure basis:

Explicitly open central ADR-002 mechanism decision.

Scope:

Applies to conflict detection, stale-state detection, correction/review/supersession references, and event ordering within at least subject streams.

Non-goals:

Does not decide HLC, vector clocks, Lamport timestamps, device sequences, or ordering scope.

Forbidden interpretations:

- Do not rely on wall-clock device time alone for causality.
- Do not assume global cross-subject ordering is required without later source support.

Open edges:

ADR-002 must decide mechanism choice, ordering scope, and clock-trust assumptions.

Platform specification note:

Use as the main unresolved mechanism item entering ADR-002 stress testing.

## Kernel: Assignment Identity Axis Candidate

Status: Candidate
Kind: primitive

Specification statement:

Assignment identity binds actor identity to subject or scope identity with temporal bounds and authority context. Events may need to reference subject_id, actor_id, and assignment_id to preserve who acted, under what responsibility, and in what capacity.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `## S03 — User-Based Assignment`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `### Identity Map — S03`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S03 aggregate table

Closure basis:

Candidate identity axis from event storm. It intersects ADR-003 authorization and sync and must not be fully closed by ADR-002 alone without later support.

Scope:

Applies to actor identity, assignment identity, responsibility transfers, revoked assignments, and capacity-specific attribution.

Non-goals:

Does not decide authorization model, assignment scope language, sync distribution of assignment data, or device versus actor identity authority.

Forbidden interpretations:

- Do not treat actor identity alone as sufficient authority context.
- Do not rewrite historical events when assignments transfer.

Open edges:

ADR-002 may close identity/reference semantics; ADR-003 must close enforcement and access behavior.

Platform specification note:

Use as lineage connecting identity with contextual authority and assignment.

## Kernel: Process Identity And Pending Match Candidate

Status: Candidate
Kind: primitive

Specification statement:

Some operational processes, such as shipments, have transient identities that persist across handoff events and can split into child process identities. Offline actors may create events that cannot yet reference the correct process identity, requiring a pending-match pattern until the event can be linked.

Source basis:

- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `## S07 — Resource Distribution`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `### Identity Through Handoffs (S07)`
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / S07 aggregate table
- `docs/exploration/archive/05-adr2-event-storm-identity.md` / `### Questions for ADR-2`

Closure basis:

Candidate identity pattern from event storm.

Scope:

Applies to shipment-like process identities, handoffs, sub-shipments, and events with unknown references at capture time.

Non-goals:

Does not decide whether pending match is shipment-specific or a general platform pattern.

Forbidden interpretations:

- Do not require sync before recording offline receipt-like facts solely because a process identity is missing.
- Do not silently attach unlinked events to guessed identities without traceability.

Open edges:

ADR-002 must decide whether pending match is a general identity/reference pattern.

Platform specification note:

Use as lineage for ambiguous references and process-scoped identity.

## Kernel: ADR-002 Phase 2 Stress-Test Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/07-adr2-phase2-stress-test-results.md` is ADR-002 Phase 2 adversarial workflow stress-test evidence. It tests proposed identity/conflict mechanisms, records breaks and weakenings, and hands required modifications to Phase 3. It does not itself make the final ADR-002 decision.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / supersession notice
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / opening phase, method, and output consumer
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `## 8. Verdict`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-002 Phase 2 stress testing.

Non-goals:

Does not accept ADR-002, decide final event types, or close all identity/conflict mechanisms.

Forbidden interpretations:

- Do not treat "holds with modifications" as final platform closure.
- Do not ignore modifications attached to a surviving mechanism.
- Do not promote tested event names into final vocabulary without later ADR support.

Open edges:

Phase 3 synthesis and ADR-002 must decide which modifications become final.

Platform specification note:

Use this source to separate survivor mechanisms from rejected or incomplete mechanism variants.

## Kernel: Accept-And-Flag Stress-Test Survivor

Status: Conditional
Kind: interaction-rule

Specification statement:

Accept-and-flag survives stress testing only with modifications: immutable events are preserved, anomalies are surfaced as separate events, and resolutions are events, but the model requires single-writer conflict resolution, structured root-cause metadata, batch resolution, conflict detection before policy reactions, and configurable auto-resolution for low-severity flags.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `## 1. Mechanism A Findings (Accept-and-Flag)`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Mechanism A: Accept-and-Flag`

Closure basis:

Conditional stress-test survivor. Final ADR-002 must decide the concrete mechanism.

Scope:

Applies to stale references, duplicate identity, revoked authority, lifecycle conflicts, and other anomalies surfaced during sync or review.

Non-goals:

Does not decide flag event schema, flag severity model, UI, reviewer roles, or auto-resolution policy language.

Forbidden interpretations:

- Do not accept unbounded flag queues as viable.
- Do not allow multiple independent final resolutions for one conflict.
- Do not let policies fire on incoming events before conflict detection if those events may later be invalidated.

Open edges:

Phase 3 and ADR-002 must close flag ownership, deduplication, resolution authority, batching, and backlog rules.

Platform specification note:

Use as candidate conflict-surfacing contract only with the required modifications attached.

## Kernel: Single-Writer Conflict Resolution Requirement

Status: Conditional
Kind: invariant

Specification statement:

Conflict resolution must have a termination rule. The stress test identifies multiple authorized offline reviewers resolving the same conflict differently as a structural break unless each conflict has a single designated resolver, a resolution lock, or an escalation path with a terminating top authority.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### A2: Reviewer Offline — Conflicting Resolutions`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Mechanism A: Accept-and-Flag`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Overall Verdict`

Closure basis:

Conditional stress-test requirement. The exact termination strategy remains to be decided by ADR-002.

Scope:

Applies to conflict-resolution events and reviewer authority.

Non-goals:

Does not decide resolver assignment, lock semantics, hierarchy model, or escalation workflow.

Forbidden interpretations:

- Do not permit unbounded meta-conflict recursion.
- Do not assume "authorized actor" is sufficient if multiple authorized actors can emit incompatible resolutions.

Open edges:

ADR-002 must choose the resolution termination model.

Platform specification note:

Use as a hard candidate invariant for conflict resolution.

## Kernel: Structured Flag Root Cause And Batch Resolution Requirement

Status: Conditional
Kind: interaction-rule

Specification statement:

Flag events must carry structured root-cause metadata so related flags can be grouped and resolved in batches. Large stale-state bursts, bulk operations, and repeated flags for one subject are not operationally viable as one-by-one review queues.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### A3: Flag Backlog Accumulation`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### A6: Unbounded Flag Backlog Growth`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### M7: Bulk Operation Conflict Amplification`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Mechanism A: Accept-and-Flag`

Closure basis:

Conditional stress-test requirement. Final grouping and policy semantics remain for ADR-002 or later owning sources.

Scope:

Applies to conflict/flag events, reviewer queues, backlog management, and bulk operation aftermath.

Non-goals:

Does not decide root-cause schema, severity taxonomy, auto-resolution thresholds, or UI.

Forbidden interpretations:

- Do not model every flag as an unrelated review item.
- Do not allow unresolved flag backlog to grow without visibility, grouping, or escalation.

Open edges:

ADR-002 must close batchability requirements; configuration ownership of auto-resolution policies may intersect ADR-004.

Platform specification note:

Use as operational scalability lineage for conflict handling.

## Kernel: Detect Before Act Sync Processing Requirement

Status: Conditional
Kind: algorithm

Specification statement:

During sync, conflict detection must run before policies or downstream reactions fire on received events. Otherwise downstream work can be created from events that are later flagged or rejected, causing cascading invalidation.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### A5: Flagged Event Triggered Downstream Work Before Flag Was Noticed`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### A12` in `## 7. Assumptions Register`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Overall Verdict`

Closure basis:

Conditional stress-test requirement. Final sync pipeline details remain to be confirmed by ADR-002 and ADR-003.

Scope:

Applies to sync ingestion ordering, conflict detection, and policy-triggered downstream work.

Non-goals:

Does not decide quarantine windows, downstream flag propagation, or policy engine implementation.

Forbidden interpretations:

- Do not trigger review assignments, allocations, or workflow reactions before conflict eligibility is evaluated.
- Do not rely on rollback of immutable downstream events as the normal correction path.

Open edges:

ADR-002 must close conflict-detection ordering; ADR-003/ADR-005 may own sync topology and downstream workflow consequences.

Platform specification note:

Use as lineage for sync ingestion algorithm constraints.

## Kernel: Alias Table Stress-Test Survivor

Status: Conditional
Kind: interaction-rule

Specification statement:

Alias-table projection for merges survives stress testing only with modifications: transitive alias closure should be eager, lineage acyclicity must be enforced, unmerge should be replaced or redefined as corrective split, post-split events require attribution workflow, and projection rebuild cost must be bounded with possible server-computed projection fallback.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `## 2. Mechanism B Findings (Alias Table in Projection)`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Mechanism B: Alias Table in Projection`

Closure basis:

Conditional stress-test survivor. Final ADR-002 must decide the concrete identity-evolution mechanism.

Scope:

Applies to subject merge, split, retired ID resolution, projection rebuild, and local alias state.

Non-goals:

Does not decide alias storage schema, sync protocol, projection implementation, or complete lifecycle event vocabulary.

Forbidden interpretations:

- Do not treat unmerge as a safe symmetric inverse of merge.
- Do not allow alias lookup chains to grow without bound.
- Do not assume split can be resolved by one-to-one aliasing.

Open edges:

Phase 3 and ADR-002 must close alias semantics, corrective split, post-split attribution, and performance bounds.

Platform specification note:

Use as candidate identity-evolution contract only with the required modifications attached.

## Kernel: Corrective Split Over Unmerge Requirement

Status: Conditional
Kind: rejected-alternative

Specification statement:

The stress test finds `SubjectsUnmerged` structurally unsound as a symmetric reverse of merge because events recorded during the merge window cannot be automatically re-attributed under immutability. A wrong merge should instead be corrected by a corrective split or equivalent lineage annotation, with optional human re-attribution of affected events.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### B6: Unmerge — Events Recorded After Merge But Before Unmerge`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### M5: Unmerge Event Attribution`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Mechanism B: Alias Table in Projection`

Closure basis:

Conditional stress-test rejection of symmetric unmerge. Final rejection waits for ADR-002.

Scope:

Applies to mistaken merges and post-merge event attribution.

Non-goals:

Does not decide the exact corrective split event shape or attribution workflow.

Forbidden interpretations:

- Do not assume immutable historical events can be rewritten to undo a merge.
- Do not require automatic attribution of merge-window events without evidence.

Open edges:

ADR-002 must decide the correction model for wrong merges.

Platform specification note:

Use as rejected-alternative lineage for unmerge.

## Kernel: Device Sequence Sync Watermark Survivor

Status: Conditional
Kind: algorithm

Specification statement:

Device sequence plus sync watermark survives stress testing as the ADR-002 causal-ordering foundation: device sequence gives per-device total order, sync watermark detects staleness and concurrency, and the model intentionally does not decide winners for concurrent state changes. Concurrent state changes require human resolution.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `## 3. Mechanism C Findings (Device-Sequence + Sync-Watermark)`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Mechanism C: Device-Sequence + Sync-Watermark`

Closure basis:

Conditional stress-test survivor. Final ADR-002 must confirm the mechanism.

Scope:

Applies to causal ordering, staleness detection, conflict detection, and same-device event ordering.

Non-goals:

Does not decide global ordering, vector clocks, HLC rejection, or human-resolution workflow details.

Forbidden interpretations:

- Do not use this mechanism to pick a winner between concurrent state changes.
- Do not assume events from the same actor across unsynced different devices are totally ordered.

Open edges:

ADR-002 must close mechanism choice, ordering scope, and persistence requirements.

Platform specification note:

Use as candidate causal-ordering algorithm lineage.

## Kernel: Device Time Advisory Requirement

Status: Conditional
Kind: invariant

Specification statement:

Device time must be advisory for display and audit, not structural for ordering or conflict detection. Ordering should use device sequence within a device and sync watermark across knowledge epochs; implausible device times should be accepted but annotated with a clock-anomaly flag.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### C3: Device Clock Reset — What Breaks?`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Mechanism C: Device-Sequence + Sync-Watermark`

Closure basis:

Conditional stress-test clarification. Final ADR-002 must confirm envelope semantics.

Scope:

Applies to event ordering, conflict detection, projections, display, and audit timestamps.

Non-goals:

Does not decide clock-anomaly thresholds, time synchronization, or full event envelope schema.

Forbidden interpretations:

- Do not sort projections structurally by untrusted device time.
- Do not use device time to determine causal ordering.

Open edges:

ADR-002 must confirm timestamp and ordering semantics.

Platform specification note:

Use as candidate event-envelope invariant.

## Kernel: Pending Match Bijective Constraint

Status: Conditional
Kind: invariant

Specification statement:

Pending-match resolution for events with unresolved identity references must enforce matching constraints appropriate to the process. In shipment-like handoffs, matching must be bijective at the handoff level: each shipment matches at most one receipt and each receipt matches at most one shipment. Multi-candidate matches require human resolution.

Source basis:

- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Scenario Gamma: Shipment Receipt Offline with Identity Collision`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### Pending Match` in `## 5. Invariant Survival Report`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` / `### M6: Pending Match Timeout`

Closure basis:

Conditional stress-test requirement for pending match. Final generality and ownership remain open.

Scope:

Applies to pending identity/reference matching, especially shipment receipt-like workflows.

Non-goals:

Does not decide matching confidence algorithms, timeout policy, retroactive process creation, or whether pending match is a general platform primitive.

Forbidden interpretations:

- Do not auto-resolve ambiguous matches below a defined confidence threshold.
- Do not allow one pending event to be silently matched to multiple process identities.

Open edges:

ADR-002 must decide whether pending match is general identity infrastructure or domain-specific workflow behavior; ADR-005 may own workflow-specific matching.

Platform specification note:

Use as lineage for unresolved-reference handling.

## Kernel: ADR-002 Phase 3 Classification Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/09-adr2-phase3-classification-results.md` classifies ADR-002 Phase 2 findings by irreversibility boundary: ADR-002 constraints, ADR-002 strategies, deferrals to other ADRs, and accepted risks. It produces the ADR-002 decision skeleton, but final platform closure still requires ADR-002 extraction.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / supersession notice
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / opening phase, method, and output consumer
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `## The Boundary Test Applied`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `## ADR-2 Decision Skeleton`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to ADR-002 classification, cross-ADR assumption handling, and decision-skeleton lineage.

Non-goals:

Does not replace ADR-002 and does not close ADR-004 or ADR-005 deferrals.

Forbidden interpretations:

- Do not treat Bucket 2 strategies as irreversible constraints.
- Do not let Bucket 3 deferrals become hidden ADR-002 decisions.
- Do not ignore Bucket 1 constraints when extracting ADR-002.

Open edges:

ADR-002 must be extracted to verify final accepted decisions and any wording changes.

Platform specification note:

Use as the main ADR-002 pre-ADR classification map.

## Kernel: ADR-002 Irreversibility Classification Rule

Status: Settled
Kind: interaction-rule

Specification statement:

ADR-002 classifies a finding as an irreversible constraint when changing it would require data migration or reinterpretation across deployed devices and stored events. Findings that only require code, projection, read-model, UI, policy, or configuration changes are strategies or deferrals.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `## The Boundary Test Applied`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `## Classification Summary Table`

Closure basis:

Settled as ADR-002 classification method.

Scope:

Applies to interpreting Bucket 1, Bucket 2, Bucket 3, and Bucket 4 in Phase 3.

Non-goals:

Does not decide the final ADR text.

Forbidden interpretations:

- Do not promote read-model strategies into event-envelope constraints.
- Do not defer envelope semantics that would make old events ambiguous.

Open edges:

ADR-002 extraction must confirm which Bucket 1 items were committed.

Platform specification note:

Use to preserve why each ADR-002 kernel is structural or evolvable.

## Kernel: ADR-002 Event Envelope Constraint Set

Status: Conditional
Kind: contract

Specification statement:

The ADR-002 decision skeleton requires each event to carry hardware-bound `device_id`, monotonically increasing per-device `device_sequence`, `sync_watermark` from the last-known server state at creation time, and typed identity references of the form `{type, id}` for subject, actor, process, and assignment references.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S1: Event Envelope — Causal Ordering Fields`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S2: Event Envelope — Typed Identity References`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S4: Device Sequence and Sync Watermark Persistence`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S5: Device Identity Is Hardware-Bound`

Closure basis:

Conditional Phase 3 classification closure. Final ADR-002 must confirm.

Scope:

Applies to ADR-002 additions to the ADR-001 event envelope.

Non-goals:

Does not decide full event payload schema, device provisioning, actor authentication, or future identity types.

Forbidden interpretations:

- Do not use untyped UUID references where identity type ambiguity would affect interpretation.
- Do not tie `device_id` to user accounts.
- Do not reuse `(device_id, device_sequence)`.

Open edges:

ADR-002 must verify final envelope contract and exact terminology.

Platform specification note:

Likely platform event-envelope contract pending ADR-002 verification.

## Kernel: Device Time Advisory Constraint

Status: Conditional
Kind: invariant

Specification statement:

`device_time` is advisory for display and audit only. Projection logic, conflict detection, and protocol correctness must not depend on `device_time`; intra-device ordering uses `device_sequence`, and cross-device concurrency uses `sync_watermark`.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### C3 / Assumption A8 / Q10: device_time is advisory, not structural`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S3: device_time Is Advisory`

Closure basis:

Conditional Phase 3 classification closure. Final ADR-002 must confirm.

Scope:

Applies to event ordering, conflict detection, projections, and timestamp interpretation.

Non-goals:

Does not decide clock-anomaly flag thresholds or clock synchronization.

Forbidden interpretations:

- Do not structurally order events by device clock timestamps.
- Do not use device time to decide conflict winners.

Open edges:

ADR-002 must verify final timestamp semantics.

Platform specification note:

Likely event-envelope invariant pending ADR-002 verification.

## Kernel: Merge Alias Projection Constraint

Status: Conditional
Kind: interaction-rule

Specification statement:

`SubjectsMerged` creates an alias mapping from retired identity to surviving identity. No existing event is modified or physically re-referenced. Projections resolve retired references for reads, and the alias mapping itself is represented as an event that syncs normally.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### Q2: Merge is alias-in-projection, never physical re-reference`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S6: Merge Is Alias-in-Projection, Never Re-Reference`

Closure basis:

Conditional Phase 3 classification closure. Final ADR-002 must confirm.

Scope:

Applies to subject merge semantics.

Non-goals:

Does not decide alias-table implementation strategy, eager/lazy closure, or sync optimization.

Forbidden interpretations:

- Do not rewrite stored events to replace retired IDs.
- Do not hide the original identity reference from audit/conflict detection.

Open edges:

ADR-002 must verify final merge semantics.

Platform specification note:

Likely identity-evolution contract pending ADR-002 verification.

## Kernel: Corrective Split Constraint

Status: Conditional
Kind: rejected-alternative

Specification statement:

The ADR-002 classification rejects `SubjectsUnmerged` as an event type. Incorrect merges are corrected by splitting the surviving subject and creating a successor for the wrongly merged entity; post-merge events default to the surviving subject, with optional manual re-attribution.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### B6: No SubjectsUnmerged event type — wrong merges are corrected by corrective split`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S7: No SubjectsUnmerged — Wrong Merges Use Corrective Split`

Closure basis:

Conditional Phase 3 classification closure. Final ADR-002 must confirm.

Scope:

Applies to wrong-merge correction and unmerge rejection.

Non-goals:

Does not decide attribution workflow details or corrective split payload shape.

Forbidden interpretations:

- Do not introduce symmetric unmerge without reopening ADR-002.
- Do not require automatic re-attribution of merge-window events.

Open edges:

ADR-002 must verify rejection of `SubjectsUnmerged`.

Platform specification note:

Likely rejected-alternative and correction contract pending ADR-002 verification.

## Kernel: Split Frozen-History Acyclicity Constraint

Status: Conditional
Kind: invariant

Specification statement:

`SubjectSplit` archives the source subject as a terminal state. Historical events remain under the source ID; new events go to successors; post-split events referencing the archived source are accepted and flagged. `SubjectsMerged` requires active operands, and archived subjects cannot be merge targets, split again, or reactivated, guaranteeing a DAG lineage graph.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### Q3: Split freezes historical events under the source ID`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### B4 / Assumption A6: Lineage graph acyclicity is enforced by aggregate validation rules`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S8: Split Freezes History; Source Is Permanently Archived`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S9: Lineage Graph Acyclicity By Construction`

Closure basis:

Conditional Phase 3 classification closure. Final ADR-002 must confirm.

Scope:

Applies to subject split, merge operands, archived lifecycle state, and lineage graph validity.

Non-goals:

Does not decide UI for archived subjects or manual attribution workflow.

Forbidden interpretations:

- Do not reassign historical source events to successors by mutation.
- Do not allow archived subjects to become active again.
- Do not permit merge/split events that create lineage cycles.

Open edges:

ADR-002 must verify final split and lineage rules.

Platform specification note:

Likely lineage invariant pending ADR-002 verification.

## Kernel: SubjectSplit Online-Only Constraint

Status: Conditional
Kind: interaction-rule

Specification statement:

`SubjectSplit` is online-only and server-validated. Offline split commands are not supported; the server verifies the source has not already been split or archived before writing `SubjectSplit`, and successor IDs are generated during the server-validated transaction.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### Assumption A11: SubjectSplit is an online-only operation`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S10: SubjectSplit Is Online-Only`

Closure basis:

Conditional Phase 3 classification closure. Final ADR-002 must confirm.

Scope:

Applies to subject split command execution.

Non-goals:

Does not decide online-only status for other identity actions.

Forbidden interpretations:

- Do not allow disconnected coordinators to split the same source independently.
- Do not let clients mint competing successor sets for one source.

Open edges:

ADR-002 must verify final split execution rule.

Platform specification note:

Likely command-validity rule pending ADR-002 verification.

## Kernel: Conflict Resolution And Detection Constraints

Status: Conditional
Kind: contract

Specification statement:

ADR-002 classification requires single-writer conflict resolution, conflict detection before policy execution during sync, and conflict detection using raw event references before alias resolution. Every `ConflictDetected` event designates one resolver; only that resolver's `ConflictResolved` event is canonical. Flagged events do not trigger policies until resolved.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### A2 / M1 / Assumption A7: Conflict resolution is single-writer per conflict`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### Assumption A12: Sync processing runs conflict detection before policies fire`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### B2: Conflict detection operates on raw event references before alias resolution`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S11: Single-Writer Conflict Resolution`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S12: Conflict Detection Before Policy Execution`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S13: Conflict Detection Uses Raw References`

Closure basis:

Conditional Phase 3 classification closure. Final ADR-002 must confirm.

Scope:

Applies to conflict detection, conflict resolution, sync processing order, and alias/conflict ordering.

Non-goals:

Does not decide conflict queue UI, delegation rules, flag event full schema, or resolution location strategy.

Forbidden interpretations:

- Do not allow competing canonical conflict resolutions.
- Do not run downstream policies before conflict detection on incoming sync events.
- Do not alias-resolve away retired-reference provenance before conflict detection.

Open edges:

ADR-002 must verify final conflict contract.

Platform specification note:

Likely conflict contract pending ADR-002 verification.

## Kernel: Accept Stale Events Constraint

Status: Conditional
Kind: invariant

Specification statement:

The platform never rejects a validly structured event because it was recorded against stale subject state. Events against deactivated, merged, split, reclassified, or otherwise changed subjects are accepted and stored; state anomalies are surfaced as separate `ConflictDetected` events.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### Q12: Events against stale state are always accepted, never rejected`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `### S14: Events Are Never Rejected for State Staleness`

Closure basis:

Conditional Phase 3 classification closure. Final ADR-002 must confirm.

Scope:

Applies to sync acceptance of validly structured events under stale state.

Non-goals:

Does not decide invalid payload handling, malformed event rejection, flag severity, or resolution outcome.

Forbidden interpretations:

- Do not drop field work merely because the subject state changed while offline.
- Do not conflate event acceptance with semantic approval.

Open edges:

ADR-002 must verify final stale-event acceptance rule.

Platform specification note:

Likely offline/immutability invariant pending ADR-002 verification.

## Kernel: ADR-002 Strategy Classification Set

Status: Conditional
Kind: conditional-validity

Specification statement:

Phase 3 classifies several ADR-002 findings as evolvable strategies rather than irreversible constraints: server-side flags initially; root-cause metadata and batch grouping as payload/read-model strategy; flag annotation after identity changes; configurable auto-resolution; projection rebuild strategy and server-computed projection fallback; eager alias closure behind a resolve interface; post-split attribution workflow; uniform typed UUID references; central conflict queue with delegation; iterative cascading resolution; post-merge local sync resolution; and compound flag grouping by root event.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `## Bucket 2: ADR-2 Strategies (Detail)`

Closure basis:

Conditional strategy classification. Final ADR-002 may carry some as notes or constraints, but Phase 3 marks them evolvable.

Scope:

Applies to ADR-002 strategy and implementation-boundary lineage.

Non-goals:

Does not decide ADR-3 sync topology, ADR-4 configuration boundary, or ADR-5 workflow details.

Forbidden interpretations:

- Do not mistake these strategies for event-envelope constraints unless ADR-002 later commits them as such.
- Do not omit their interfaces where Phase 3 says an interface is required.

Open edges:

ADR-002 extraction must verify which strategies are carried forward and how they are worded.

Platform specification note:

Use to keep final platform spec from over-constraining evolvable implementation choices.

## Kernel: ADR-002 Cross-ADR Deferral Set

Status: Open
Kind: open-question

Specification statement:

Phase 3 explicitly defers downstream work invalidation cascade to ADR-005, pending-match timeout and bijective matching to ADR-005 with some ADR-4 aspects, domain-specific conflict definition boundary to ADR-004, and pending-match generality to ADR-4/ADR-5. ADR-002 should support unresolved references and structural conflict detection without deciding those downstream policies.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `## Bucket 3: Deferred to Other ADRs (Detail)`

Closure basis:

Open cross-ADR deferral from Phase 3.

Scope:

Applies to ADR-004 and ADR-005 deferred assumptions discovered during ADR-002 exploration.

Non-goals:

Does not decide workflow cascade behavior, matching algorithms, timeouts, confidence thresholds, or configurable business conflict rules.

Forbidden interpretations:

- Do not let ADR-002 decide workflow cancellation cascades.
- Do not let ADR-002 decide domain-specific conflict rule configuration.
- Do not bury pending-match generality without revisiting it in ADR-4/5 extraction.

Open edges:

ADR-004 and ADR-005 sources must promote, adapt, reject, or leave these assumptions open.

Platform specification note:

Use as cross-ADR assumption register for later reconciliation.

## Kernel: ADR-002 Accepted Risk Set

Status: Conditional
Kind: conditional-validity

Specification statement:

Phase 3 accepts several ADR-002 risks with revisit triggers: concurrent state changes require human resolution; batch conflict detection is computationally feasible, with sync-time trigger for optimization; same-actor cross-device ordering is best-effort, with a trigger to consider actor-sequence fields if it causes excessive false flags; S19 deactivation/observation conflict detection is confirmed; unmerge attribution risk is eliminated if corrective split remains adopted.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `## Bucket 4: Accepted Risks (Detail)`

Closure basis:

Conditional accepted-risk classification. Final ADR-002 must confirm.

Scope:

Applies to operational risk and revisit criteria for ADR-002 mechanisms.

Non-goals:

Does not decide monitoring implementation or future ADR amendment process.

Forbidden interpretations:

- Do not treat accepted risks as absent risks.
- Do not add actor-sequence fields now unless the revisit trigger is met or ADR-002 changes.

Open edges:

ADR-002 extraction must verify whether accepted risks and triggers are carried forward.

Platform specification note:

Use to preserve conditional validity and future-change triggers.

## Kernel: ADR-002 Simplicity Validation

Status: Conditional
Kind: conditional-validity

Specification statement:

Phase 3 validates that the Bucket 1 ADR-002 constraints do not materially complicate the simple S00 path: a worker records one observation, the event gains three lightweight envelope fields plus typed references, one aggregate validates the write, and sync remains a single server round-trip with conflict check, store, and projection.

Source basis:

- `docs/exploration/archive/09-adr2-phase3-classification-results.md` / `## Simplicity Validation`

Closure basis:

Conditional Phase 3 validation. Final ADR-002 must confirm accepted constraints.

Scope:

Applies to simplicity pressure for basic capture under ADR-002 constraints.

Non-goals:

Does not prove complex identity/conflict cases are simple.

Forbidden interpretations:

- Do not use ADR-002 complexity in edge cases to claim the simple capture path is overburdened.
- Do not use S00 simplicity to ignore identity/conflict edge-case constraints.

Open edges:

ADR-002 extraction must verify final constraints before this validation becomes settled.

Platform specification note:

Use as evidence that irreversible ADR-002 metadata does not violate simplicity baseline.
