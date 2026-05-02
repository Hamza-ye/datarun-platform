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
