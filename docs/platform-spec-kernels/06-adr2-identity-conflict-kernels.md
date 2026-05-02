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
