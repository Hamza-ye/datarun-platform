# ADR-003 Authorization And Sync Kernel Staging

Status: Iteration 23 staging

This temporary staging file holds ADR-003 authorization and selective-sync lineage kernels. It is not a final atomic document.

## Staged Kernels

## Kernel: ADR-003 Phase 1 Policy Scenario Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` is ADR-003 Phase 1 scenario-driven policy analysis. It explores authorization and sync-scope pressures across selected scenarios, verifies upstream ADR assumptions, identifies hot spots and emergent questions, and prepares inputs for later stress testing. It does not make final ADR-003 decisions.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / supersession notice
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / opening phase, method, and input scenarios
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `## 8. Hot Spot Inventory`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `## 9. Emergent Questions`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-003 Phase 1.

Non-goals:

Does not decide final authorization model, sync topology, projection location, scope contraction behavior, authority envelope fields, or auditor access.

Forbidden interpretations:

- Do not treat Phase 1 findings as ADR-003 closure.
- Do not let ADR-003 Phase 1 rewrite ADR-001 or ADR-002 commitments.
- Do not promote hot spots into settled platform behavior.

Open edges:

ADR-003 Phase 2 stress testing, later ADR-003 exploration, and ADR-003 must close, adapt, or reject these findings.

Platform specification note:

Use this source for authorization/sync lineage and hot-spot tracking.

## Kernel: ADR-003 Upstream Assumption Compatibility

Status: Settled
Kind: interaction-rule

Specification statement:

ADR-003 Phase 1 verifies its upstream assumptions against ADR-001, ADR-002, and operational constraints. It treats immutable events, event-log source of truth, typed identities, device-sequence/sync-watermark ordering, accept-and-flag state staleness, single-writer conflict resolution, advisory device time, conflict detection before policy execution, and raw-reference conflict detection as committed inputs.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `## 1. Upstream Assumptions`

Closure basis:

Settled as ADR-003 extraction context.

Scope:

Applies to ADR-003 exploration dependency handling.

Non-goals:

Does not re-decide ADR-001 or ADR-002.

Forbidden interpretations:

- Do not reopen ADR-002's stale-event acceptance while extracting ADR-003.
- Do not use ADR-003 to make device time structural.
- Do not reject stale authorization events when ADR-002 requires validly structured stale-state events to be accepted and flagged.

Open edges:

ADR-003 may add authorization-specific flags and sync behavior only within these upstream constraints.

Platform specification note:

Use to keep ADR-003 aligned with earlier ADR closure.

## Kernel: Authorization Staleness Accept-And-Flag Candidate

Status: Candidate
Kind: interaction-rule

Specification statement:

Authorization staleness is structurally handled like identity staleness: work created under stale role, scope, assignment, temporal authority, or authority context is accepted as an immutable event and surfaced with authorization-specific flags on sync rather than rejected.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `Critical constraint interaction for this analysis`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S03 `Act 4`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S09 `Act 5`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S19 `Act 5`

Closure basis:

Candidate ADR-003 Phase 1 finding. Later sources must classify and decide flag severity, policy firing, and resolver handling.

Scope:

Applies to stale assignment scope, stale role, expired campaign authority, and stale cross-scope identity conditions.

Non-goals:

Does not decide flag event names, severity, blocking behavior, auto-resolution, or downstream policy effects.

Forbidden interpretations:

- Do not reject validly structured offline work solely because authority changed while disconnected.
- Do not treat acceptance as authorization approval.

Open edges:

Stale-scope flag severity, expired-authority grace, and resolver authority remain to be stress-tested and decided.

Platform specification note:

Use as lineage for authorization-specific accept-and-flag behavior.

## Kernel: Assignment-Based Authorization Candidate

Status: Candidate
Kind: primitive

Specification statement:

ADR-003 Phase 1 identifies Assignment as the atomic unit of authorization: an assignment binds actor, role, scope, duration, and optionally process. Access reduces to checking whether the actor has an active assignment whose scope contains the target entity and whose role permits the intended action.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### EQ-3: The Assignment as the Atomic Unit of Authorization`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Designated Responsibility Summary`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S20/S21 simplicity verdict

Closure basis:

Candidate ADR-003 Phase 1 finding.

Scope:

Applies to actor authority, role, scope, temporal bounds, standing work, campaign work, and conflict resolver designation.

Non-goals:

Does not decide final scope representation, role vocabulary, auditor access, or authorization policy language.

Forbidden interpretations:

- Do not reduce authorization to role alone.
- Do not turn this into a generic ABAC policy engine without later source support.

Open edges:

Auditor access, subject-based scope, device sharing, and scope composition require stress testing.

Platform specification note:

Use as candidate authorization primitive lineage.

## Kernel: Authority Context Envelope Candidate

Status: Candidate
Kind: contract

Specification statement:

Events need authority context that records the grant under which the action was performed. Phase 1 identifies the authority context as actor reference, assignment reference, and optionally process reference, rather than a simple role field.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S09 `Act 3`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Coordinated Campaign Summary`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### EQ-1: Authority Context in the Event Envelope`

Closure basis:

Candidate ADR-003 Phase 1 finding. Final envelope fields must be decided by ADR-003 and later envelope ADRs if applicable.

Scope:

Applies to standing assignment authority, campaign authority, process-scoped authority, attribution, and post-sync validation.

Non-goals:

Does not decide exact envelope field names, multiplicity of assignment references, or code-level event vocabulary.

Forbidden interpretations:

- Do not preserve only actor identity while losing the assignment/process authority that authorized the action.
- Do not infer that every event must reference a process.

Open edges:

Scope composition and authority context shape remain high-severity Phase 1 hot spots.

Platform specification note:

Use as candidate event-envelope addition for authorization context.

## Kernel: Sync Scope As Offline Authorization Candidate

Status: Candidate
Kind: interaction-rule

Specification statement:

For offline operation, sync scope functions as the primary authorization mechanism: the server decides what data reaches the device, and the device can work with the local data and assignment events it holds. Device-local authorization is mostly scope containment and role checks against synced assignment state.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S19 `Act 1`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### EQ-2: Sync Scope as the Primary Offline Authorization Mechanism`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Q2: Sync Scope vs. Access Scope`

Closure basis:

Candidate ADR-003 Phase 1 finding.

Scope:

Applies to field-worker offline operation, server sync filtering, and local device behavior.

Non-goals:

Does not decide data removal on scope narrowing, supervisor raw-vs-summary sync, coordinator query behavior, or device sharing.

Forbidden interpretations:

- Do not require an online policy check for primary field work.
- Do not sync less than the field worker needs to work offline.
- Do not sync extra sensitive data merely for convenience without later decision.

Open edges:

Scope contraction, supervisor sync shape, and projection location remain high-severity hot spots.

Platform specification note:

Use as candidate offline authorization/sync contract.

## Kernel: Subject-Scoped Sync Candidate

Status: Candidate
Kind: interaction-rule

Specification statement:

Field-worker continuity requires subject-scoped sync rather than actor-scoped sync: when a subject is in an actor's scope, the device needs the subject's event history regardless of which actor authored those events.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S20/S21 `Act 2`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S03 `Act 6`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Designated Responsibility Summary`

Closure basis:

Candidate ADR-003 Phase 1 finding.

Scope:

Applies to field-worker subject history, reassignment, and continuity of care/work.

Non-goals:

Does not decide how much history must be included, retention, or supervisor/coordinator summary sync.

Forbidden interpretations:

- Do not limit a new assignee's device to only events authored by that actor if the subject history is needed.

Open edges:

Subject history completeness on reassignment remains a medium hot spot.

Platform specification note:

Use as lineage for sync-scope decisions.

## Kernel: Scope Composition Candidate

Status: Open
Kind: open-question

Specification statement:

ADR-003 Phase 1 identifies scope composition as unresolved: standing assignments and campaign assignments may combine as union, tagged union, or independent checks. Tagged union preserves which assignment authorized an event and supports campaign reporting and cleanup.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S09 `Act 2`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Q1: Scope Representation`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / hot spot HS-7

Closure basis:

Open high-severity Phase 1 hot spot.

Scope:

Applies to multiple active assignments, campaigns, additive temporal scope, effective scope, and authority attribution.

Non-goals:

Does not decide final scope algebra.

Forbidden interpretations:

- Do not collapse campaign authority into standing scope without preserving attribution if later sources require it.

Open edges:

Phase 2 stress testing and ADR-003 must resolve scope composition.

Platform specification note:

Use as a key open question for authorization specification.

## Kernel: Temporal Authority Server-Side Enforcement Candidate

Status: Candidate
Kind: interaction-rule

Specification statement:

Time-bound access grants are represented as assignment events with temporal bounds, often tied to a process such as a campaign. Because `device_time` is advisory, devices can only warn about possible expiration; authoritative temporal enforcement happens server-side on sync, and expired-authority events are accepted and flagged.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S09 `Act 4`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Q4: Temporal Access`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / hot spot HS-8

Closure basis:

Candidate ADR-003 Phase 1 finding.

Scope:

Applies to campaign assignments, time-bound scopes, temporal expiration, and disconnected capture.

Non-goals:

Does not decide grace periods, auto-resolution, or exact flag schema.

Forbidden interpretations:

- Do not hard-block offline work using untrusted device time.
- Do not make device time structural to temporal access.

Open edges:

Temporal access enforcement is a high-severity item for ADR-003 closure; grace period is likely policy/configuration.

Platform specification note:

Use as lineage for temporal authority rules.

## Kernel: Tiered Projection And Sync Topology Candidate

Status: Candidate
Kind: conditional-validity

Specification statement:

ADR-003 Phase 1 suggests tiered projection and sync topology: field workers use device-local projections from raw in-scope events; supervisors may need a hybrid of raw events for offline drill-down and server-computed summaries for aggregate views; coordinators with reliable connectivity use server-computed projections and online queries.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S21 `Act 6`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S14 `Visibility Analysis Across Levels`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Q3: Projection Location`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / hot spots HS-4 and HS-11

Closure basis:

Candidate Phase 1 topology finding. Later stress tests and ADR-003 must decide.

Scope:

Applies to projection location, raw event sync, summary sync, dashboards, drill-down, and tier-specific connectivity.

Non-goals:

Does not decide exact sync protocol, summary shape, or projection delivery guarantees.

Forbidden interpretations:

- Do not force one projection location for all tiers without later evidence.
- Do not turn supervisor summary sync into final topology from Phase 1 alone.

Open edges:

Projection computation location and upward aggregation remain high-severity hot spots.

Platform specification note:

Use as candidate sync topology lineage.

## Kernel: Scope Contraction Data Handling Open

Status: Open
Kind: open-question

Specification statement:

ADR-003 must decide what happens to data already on a device when scope narrows: purge immediately, retain but hide, or retain indefinitely. Scope contraction affects security, privacy, device storage, usability, and continuity of historical work.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S20/S21 `Act 5`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### EQ-4: Asymmetry Between Scope Expansion and Scope Contraction`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / hot spot HS-3

Closure basis:

Open high-severity Phase 1 hot spot.

Scope:

Applies to reassignment, campaign end, role downgrade, data minimization, and local storage lifecycle.

Non-goals:

Does not decide retention, purge protocol, or UI hiding behavior.

Forbidden interpretations:

- Do not assume scope expansion and contraction are symmetric.
- Do not leave no-longer-in-scope data behavior implicit.

Open edges:

Phase 2 and ADR-003 must close or explicitly defer scope contraction.

Platform specification note:

Use as a major open item for sync and local data lifecycle.

## Kernel: Hierarchical Scope Model Candidate

Status: Candidate
Kind: primitive

Specification statement:

ADR-003 Phase 1 finds that the primary scope axis appears to be a geographic or organizational hierarchy: assignments bind actors to hierarchy nodes, and higher-level assignments cover the lower-level subjects, actors, or locations contained under those nodes. Subject-based scope may still be required for specialized cases and remains undecided.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S14 `Distribution Summary`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Q1: Scope Representation`

Closure basis:

Candidate ADR-003 Phase 1 finding.

Scope:

Applies to geographic areas, organizational hierarchy levels, worker areas, supervisor regions, district/warehouse visibility, and possible subject-list exceptions.

Non-goals:

Does not decide the final scope type system, hierarchy representation, or whether subject-based scope is first-class.

Forbidden interpretations:

- Do not treat hierarchy-only scope as settled from Phase 1.
- Do not discard subject-based scope pressure merely because it did not dominate the Phase 1 scenarios.

Open edges:

Subject-based scope, cross-level visibility, and cross-scope identity operations remain to be stress-tested.

Platform specification note:

Use as candidate lineage for scope primitive definition.

## Kernel: Scope Transition Atomicity Open

Status: Open
Kind: open-question

Specification statement:

When an actor's assignment scope changes, sync must avoid leaving the device in an inconsistent partial transition. The assignment change, data for the new scope, and data withdrawal or hiding for the old scope may need atomic delivery, an idempotent retry protocol, or an explicit multi-step transition.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S03 `Act 5`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / hot spot HS-6

Closure basis:

Open medium-severity Phase 1 hot spot.

Scope:

Applies to reassignment, scope expansion, scope narrowing, interrupted sync, and local assignment/data consistency.

Non-goals:

Does not decide the sync transaction protocol, retry semantics, or local transition UI behavior.

Forbidden interpretations:

- Do not deliver assignment changes and scope payloads as unrelated facts if that can create an unusable or unexplained local state.
- Do not assume connectivity is stable during scope transition.

Open edges:

Phase 2 and ADR-003 must decide whether this is a protocol invariant, a projection concern, or an operational retry strategy.

Platform specification note:

Use as sync-protocol closure tracking for authorization scope changes.

## Kernel: Shared Device Actor Scope Open

Status: Open
Kind: open-question

Specification statement:

Multiple actors may share one hardware device, while ADR-002 keeps `device_id` hardware-bound. ADR-003 Phase 1 therefore leaves unresolved how actor-scoped sync, local data segregation, purge/re-sync, and authorization context work when one physical device is used by more than one actor.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### EQ-5: Device Sharing and Actor Scope`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Q2: Sync Scope vs. Access Scope`

Closure basis:

Open Phase 1 emergent question.

Scope:

Applies to shared Android devices, actor login/session state, actor-scoped sync payloads, local storage segregation, and hardware-bound device sequencing.

Non-goals:

Does not decide whether shared devices are supported in initial scope.

Forbidden interpretations:

- Do not reinterpret ADR-002 `device_id` as actor-bound.
- Do not make sync scope device-scoped if the source pressure is actor authorization.

Open edges:

Later ADR-003 sources must decide whether shared devices require separate local stores, scoped purge/re-sync, encrypted partitions, or explicit deferral.

Platform specification note:

Use as a boundary check between ADR-002 device identity and ADR-003 actor authorization.

## Kernel: Resolver Designation Candidate

Status: Candidate
Kind: interaction-rule

Specification statement:

Conflict resolver designation appears assignment-derived: the resolver is the nearest actor in the hierarchy whose scope encompasses all parties to the conflict and whose role is sufficient. Pending conflicts must be re-routed if the designated resolver's role or assignment changes.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Q5: Conflict Resolver Designation`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / hot spot HS-13

Closure basis:

Candidate ADR-003 Phase 1 finding.

Scope:

Applies to ADR-002 single-writer conflict resolution eligibility and authorization.

Non-goals:

Does not decide exact resolver hierarchy, reassignment event shape, or orphaned-conflict workflow.

Forbidden interpretations:

- Do not treat resolver designation as independent from authorization scope.
- Do not assume a stale resolver's resolution remains canonical without validation.

Open edges:

Orphaned conflicts and resolver reassignment require stress testing and ADR-003 closure.

Platform specification note:

Use as lineage for conflict-resolution authorization.

## Kernel: ADR-003 Phase 1 Hot Spot Set

Status: Open
Kind: open-question

Specification statement:

Phase 1 identifies high-severity ADR-003 hot spots that must be resolved or explicitly deferred: data removal on scope narrowing, supervisor projection location, scope composition model, temporal access enforcement, and upward visibility aggregation. Medium hot spots include subject history completeness on reassignment, stale-scope flag severity, scope transition atomicity, cross-level visibility boundaries, scope-crossing merge, and orphaned conflicts.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `## 8. Hot Spot Inventory`

Closure basis:

Open Phase 1 hot-spot register.

Scope:

Applies to ADR-003 stress-test planning and closure tracking.

Non-goals:

Does not decide any hot spot.

Forbidden interpretations:

- Do not proceed to final ADR-003 closure without accounting for high-severity hot spots.

Open edges:

Phase 2 stress testing must test these items.

Platform specification note:

Use as ADR-003 open-risk checklist.

## Kernel: Auditor Access Exception Open

Status: Open
Kind: open-question

Specification statement:

Auditor access is a cross-cutting scope exception that does not fit cleanly into geographic hierarchy or campaign assignment. It may require query-scoped, temporary, cross-boundary access and was not stressed by ADR-003 Phase 1 scenarios.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### EQ-6: Auditor Access as a Scope Exception`

Closure basis:

Open Phase 1 emergent question.

Scope:

Applies to auditor and external reviewer access patterns.

Non-goals:

Does not decide auditor access mechanism.

Forbidden interpretations:

- Do not assume geographic assignment covers auditor access.
- Do not omit auditor access from later stress testing if the platform must support it.

Open edges:

Later ADR-003 sources must decide whether auditor access is in initial scope or explicitly deferred.

Platform specification note:

Use as cross-boundary access exception tracking.
