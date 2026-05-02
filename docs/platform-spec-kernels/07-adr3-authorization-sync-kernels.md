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

Status: Superseded
Kind: contract

Specification statement:

Events need authority context that records the grant under which the action was performed. Phase 1 identified the authority context as actor reference, assignment reference, and optionally process reference, rather than a simple role field. ADR-003 course correction supersedes this envelope-field candidate in favor of deriving authority from assignment/process timelines during sync.

Source basis:

- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / S09 `Act 3`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### Coordinated Campaign Summary`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` / `### EQ-1: Authority Context in the Event Envelope`
- `docs/exploration/archive/12-adr3-course-correction.md` / `The Envelope Question -- Let's Settle It`
- `docs/exploration/archive/12-adr3-course-correction.md` / `Stress Test Impact and Classification`

Closure basis:

Superseded by ADR-003 course-correction lineage, pending final ADR-003 verification.

Scope:

Applies to standing assignment authority, campaign authority, process-scoped authority, attribution, and post-sync validation.

Non-goals:

Does not decide exact envelope field names, multiplicity of assignment references, or code-level event vocabulary.

Forbidden interpretations:

- Do not preserve only actor identity while losing the assignment/process authority that authorized the action.
- Do not infer that every event must reference a process.
- Do not treat Phase 1 authority-context-in-envelope as still preferred after the course-correction source chooses authority-as-projection.

Open edges:

Scope composition and authority context shape remain high-severity Phase 1 hot spots.

Platform specification note:

Use as superseded lineage explaining why authority reconstruction was considered.

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

## Kernel: ADR-003 Phase 2 Stress-Test Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/11-adr3-phase2-stress-test.md` is ADR-003 Phase 2 adversarial stress-test evidence. It tests Phase 1 mechanisms against security, scale, bandwidth, staleness, projection, envelope, shared-device, and auditor scenarios. Its findings can narrow, reject, or promote Phase 1 candidates into constraint candidates, but final ADR-003 closure remains outside this source.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / supersession notice
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 1. Mechanisms Tested`
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## Summary: What ADR-003 Must Decide as Constraints`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-003 Phase 2.

Non-goals:

Does not decide final ADR-003 constraints, final event envelope fields, final sync protocol, or final authorization model.

Forbidden interpretations:

- Do not treat Phase 2 `RESOLVED` labels as ADR-settled decisions.
- Do not discard Phase 2 `BREAKS` findings unless later ADR-003 sources explicitly correct or reject them.

Open edges:

ADR-003 course correction and ADR-003 must confirm, adapt, or reject Phase 2 constraint candidates.

Platform specification note:

Use this source as adversarial evidence and closure pressure for ADR-003.

## Kernel: Assignment Model Structural Extension Set

Status: Conditional
Kind: conditional-validity

Specification statement:

The Phase 1 assignment model survives as a foundation only if extended beyond a single role-plus-geographic-scope containment check. Stress testing requires actor-as-subject visibility, assignment data lifecycle policy, scope-transition sync atomicity, explicit multiple-assignment tagging, and alias-respects-original-scope handling.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding A1
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 9. Assumptions Surfaced` P1
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 11. Hot Spot Resolutions` HS-2, HS-3, HS-6, HS-7, HS-12

Closure basis:

Conditional Phase 2 stress-test survivor. Later ADR-003 sources must decide whether each extension becomes a constraint, strategy, deferral, or rejection.

Scope:

Applies to assignment-based authorization, actor-as-subject events, local data lifecycle, scope transitions, campaign overlays, and scope-crossing identity merges.

Non-goals:

Does not decide exact assignment event shape, role vocabulary, or scope-type implementation.

Forbidden interpretations:

- Do not describe ADR-003 authorization as only one simple geographic containment check after this stress-test point.
- Do not treat actor-as-subject visibility or alias-cross-scope behavior as cosmetic edge cases.

Open edges:

Course correction must classify which additions are irreversible constraints and which are evolvable strategies.

Platform specification note:

Use as the central Phase 2 correction to the Phase 1 authorization model.

## Kernel: Assignment Creation Scope-Containment Invariant Candidate

Status: Conditional
Kind: invariant

Specification statement:

Creating an assignment must be validated server-side so that the new assignment scope is contained within the creating actor's authorized scope. Without this invariant, a coordinator can grant out-of-scope access and cause the sync engine to deliver unauthorized data.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding A3
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / invariant report I2
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 1 candidate summary

Closure basis:

Conditional Phase 2 constraint candidate. Final ADR-003 must confirm or replace it.

Scope:

Applies to `AssignmentCreated` commands/events, coordinator authority, campaign assignment creation, and emergency assignment creation.

Non-goals:

Does not define the complete assignment aggregate or all coordinator permissions.

Forbidden interpretations:

- Do not rely on UI convention or deployment policy to prevent out-of-scope assignment creation.
- Do not accept a valid-looking assignment event without validating creator authority.

Open edges:

Transactional behavior for coordinator assignment creation remains an open operational path.

Platform specification note:

Likely platform invariant candidate for assignment-write validation.

## Kernel: Role And Capability Enforcement Candidate

Status: Conditional
Kind: configuration-boundary

Specification statement:

Role-action compatibility must be validated server-side for every incoming event, independent of device-side checks. Role hierarchy or role-permission tables are a hidden dependency on configuration, and auditor/read-only cases introduce a possible capability dimension beyond role name.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Findings A2, A4, A5
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Combo delta
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 9. Assumptions Surfaced` P6

Closure basis:

Conditional Phase 2 finding with ADR-004 dependency. ADR-003 must decide the structural boundary; ADR-004 may own configurable permission content.

Scope:

Applies to role hierarchy, role incompatibility, capability-restricted actions, read-only auditor access, and rooted-device bypass detection.

Non-goals:

Does not decide concrete role names, permission table syntax, or domain-specific action rules.

Forbidden interpretations:

- Do not trust device-local role checks as authoritative.
- Do not let simultaneous incompatible role grants silently accumulate privileges.
- Do not hide the ADR-004 dependency for role permission definitions.

Open edges:

Role-incompatibility invariants, capability fields, and deployer-configured permission tables need later closure.

Platform specification note:

Use as a cross-ADR boundary marker between authorization structure and configurable permission content.

## Kernel: Sync Scope Expansion And Resumption Strategy

Status: Conditional
Kind: algorithm

Specification statement:

Server-side sync-scope computation is feasible at the tested scale, but first sync and large scope expansion break on 2G without mitigation. The stress test requires priority sync, resumable partial delivery, and either per-subject watermarks or an equivalent delivered-through marker.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Findings B1, B3
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 9. Assumptions Surfaced` P2
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 2 candidate summary

Closure basis:

Conditional Phase 2 strategy candidate. Later ADR-003 sources must decide whether any part is promoted to protocol constraint.

Scope:

Applies to new-worker first sync, large reassignment, historical backfill, low-bandwidth operation, and interrupted sync.

Non-goals:

Does not define packet format, transport protocol, compression, or UI progress behavior.

Forbidden interpretations:

- Do not assume global `sync_watermark` alone is enough to resume large first-sync payloads on unreliable 2G.
- Do not treat bandwidth failure as only a performance concern if it prevents operational readiness.

Open edges:

Priority order and resume markers need protocol-level classification.

Platform specification note:

Use as sync-protocol pressure for initial scope delivery and historical backfill.

## Kernel: Scope Contraction Purge Constraint Candidate

Status: Conditional
Kind: interaction-rule

Specification statement:

For sensitive personal data, scope contraction requires active device data removal rather than retain-and-hide or indefinite retention. The stress-test survivor is crash-safe purge with selective retain: keep the actor's own events where needed for work-history continuity, and purge other events for subjects no longer in scope.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Findings B2, B4
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / invariant report I3
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / hot spot HS-3

Closure basis:

Conditional Phase 2 stress-test conclusion. Final ADR-003 must decide if purge is a hard constraint for all data or conditional on sensitivity.

Scope:

Applies to reassignment, campaign end, time-limited access, auditor expiry, local storage lifecycle, and data minimization.

Non-goals:

Does not define exact purge journal format, encryption policy, or retention law policy.

Forbidden interpretations:

- Do not treat retain-but-hide as acceptable for sensitive health data.
- Do not treat absence of future sync delivery as equivalent to local data removal.

Open edges:

Crash-safe purge mechanics, own-event retention boundaries, and sensitivity-conditioned policy remain to be closed.

Platform specification note:

Use as a likely sync/local-data lifecycle constraint candidate.

## Kernel: Retain-Hide And Indefinite Retain Rejection For Sensitive Data

Status: Conditional
Kind: rejected-alternative

Specification statement:

For sensitive personal data on field devices, retaining no-longer-in-scope data while hiding it in the UI, or retaining it indefinitely, is rejected by the Phase 2 stress test. Both alternatives preserve sensitive data on physically vulnerable devices after authorization has narrowed and fail the data-minimization pressure that scope contraction is meant to enforce.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding B4
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / hot spot HS-3

Closure basis:

Conditional rejected-alternative lineage for sensitive-data deployments. Final ADR-003 must decide whether the rejection applies globally or only to sensitive data classes.

Scope:

Applies to sensitive health or personal data retained on field devices after reassignment, role downgrade, campaign end, or other scope narrowing.

Non-goals:

Does not reject local retention of the actor's own events where selective retain is later chosen.

Forbidden interpretations:

- Do not treat UI hiding as a platform-level access control once data remains locally stored.
- Do not treat indefinite local retention as compatible with data minimization for sensitive field data.

Open edges:

Whether lower-sensitivity operational data may use a different local-retention policy remains to be closed.

Platform specification note:

Use as rejected-alternative guardrail for scope-contraction policy.

## Kernel: Authorization Flag Detect-Before-Act Candidate

Status: Conditional
Kind: algorithm

Specification statement:

Detect-before-act processing must extend to authorization flags. Role-stale or capability-violating events that could trigger downstream policies must be intercepted before policy execution, otherwise invalid actions can propagate before review.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding C2
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / hot spot HS-5
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 1 candidate summary

Closure basis:

Conditional Phase 2 constraint candidate extending ADR-002 detect-before-act semantics. Final ADR-003 and later flag semantics sources must confirm the exact scope.

Scope:

Applies to `RoleStaleFlag`, `ScopeStaleFlag`, capability-restricted actions, and downstream policy execution.

Non-goals:

Does not decide final flag names, severity vocabulary, or UI flow.

Forbidden interpretations:

- Do not let authorization flags behave as post-hoc queue items when downstream irreversible policy actions may fire.
- Do not assume ADR-002 identity-conflict interception automatically covers authorization flags without ADR-003 closure.

Open edges:

Flag taxonomy and blocking semantics require later ADR-003 and flag ADR reconciliation.

Platform specification note:

Use as the main cross-ADR assumption to carry from ADR-002 conflict processing into ADR-003 authorization.

## Kernel: Authorization Flag Coordination Candidate

Status: Conditional
Kind: algorithm

Specification statement:

Multiple flags on a single event cannot be resolved independently when they affect different validity dimensions. Triple staleness requires either bundling all flags for one coordinating resolver with sufficient scope, or a defined resolution order where clinical/capability validity can make attribution flags moot.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding C3
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 9. Assumptions Surfaced` P7

Closure basis:

Conditional Phase 2 finding. Later ADR-003 and flag semantics sources must decide resolution coordination.

Scope:

Applies to events with combined role, scope, and identity staleness; resolver designation; batch resolution; and projection consequences.

Non-goals:

Does not define the final resolver selection algorithm or all precedence rules.

Forbidden interpretations:

- Do not allow independent contradictory resolutions for flags attached to the same event.
- Do not assume identity, scope, and role flags are symmetric.

Open edges:

Single coordinating resolver versus ordered resolution remains open.

Platform specification note:

Use as flag-resolution coordination lineage.

## Kernel: Conflict Resolution Online-Only Candidate

Status: Conditional
Kind: invariant

Specification statement:

Conflict resolution should be online-only so resolver authority can be verified at resolution time and stale resolver decisions do not generate recursive meta-flags. This mirrors the ADR-002 online-only precedent for merge/split operations.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding C4
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / hot spot HS-13
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 1 candidate summary

Closure basis:

Conditional Phase 2 constraint candidate. ADR-003 must confirm, adapt, or defer.

Scope:

Applies to resolver authority, `ConflictResolved`-style events, reassigned resolvers, and administrative conflict handling.

Non-goals:

Does not decide all operations that must be online-only.

Forbidden interpretations:

- Do not allow offline conflict resolution if resolver authority cannot be validated before committing the resolution.

Open edges:

Final conflict-resolution event shape and resolver reassignment protocol remain to be closed.

Platform specification note:

Use as a likely authorization-side invariant for resolution operations.

## Kernel: Sensitive Subject Authorization Exception Open

Status: Open
Kind: open-question

Specification statement:

Accept-and-flag breaks for deployments where unauthorized collection or transmission of sensitive-subject data is itself a regulatory violation. Phase 2 proposes sensitive-subject classification and modified sync or authorization behavior for high-sensitivity data, but this creates tension with ADR-002 stale-event acceptance.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding C5
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 9. Assumptions Surfaced` P3
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 1 candidate summary

Closure basis:

Open Phase 2 design-constraint collision. Later ADR sources must decide whether this is accepted limitation, configuration option, or structural constraint.

Scope:

Applies to high-sensitivity subjects, health privacy, data minimization, sync filtering, and exceptions to uniform accept-and-flag behavior.

Non-goals:

Does not decide legal compliance policy or jurisdiction-specific sensitive categories.

Forbidden interpretations:

- Do not treat a review flag as sufficient protection when the unauthorized data is already permanently stored.
- Do not silently weaken ADR-002's stale-event acceptance without explicit later closure.

Open edges:

Need reconciliation between append-only/no-data-loss guarantees and sensitive-data access restrictions.

Platform specification note:

Use as a high-risk open issue for ADR-003/flag/configuration reconciliation.

## Kernel: Projection Freshness Metadata Candidate

Status: Conditional
Kind: contract

Specification statement:

Projection and summary consumers need freshness metadata: per-worker last-known-sync time for supervisor summaries, data-as-of metadata for assessments, and server projection consistency watermarks for coordinator views during batch rebuilds.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Findings D2, D3, D4
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / invariant report I5

Closure basis:

Conditional Phase 2 finding carried from the ground-truth eventual-consistency requirement. Later ADR-003 sources must decide exact metadata contracts.

Scope:

Applies to supervisor dashboards, assessments made from stale projections, coordinator server queries, and batch merge projection consistency.

Non-goals:

Does not define UI copy, dashboard layout, or projection storage implementation.

Forbidden interpretations:

- Do not present absent activity as no activity when the worker has not synced.
- Do not hide partial projection rebuild state from coordinator-facing reads.

Open edges:

Exact metadata fields and projection consistency guarantees remain to be closed.

Platform specification note:

Use as projection/sync visibility contract lineage.

## Kernel: Incremental Projection Update Strategy

Status: Conditional
Kind: algorithm

Specification statement:

Supervisor projection performance holds if incremental updates are the normal operational path and full rebuild from raw events is reserved for recovery. Hybrid supervisor sync remains viable: raw events for drill-down and offline review, plus server summaries for aggregate dashboards.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding D1
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / hot spots HS-4 and HS-11
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 2 candidate summary

Closure basis:

Conditional Phase 2 strategy finding. ADR-003 must decide projection topology, while implementation can evolve incremental mechanics.

Scope:

Applies to supervisor devices, raw-event local projections, server-computed summaries, and projection rebuild recovery.

Non-goals:

Does not decide projection implementation language, indexing strategy, or exact rebuild SLA.

Forbidden interpretations:

- Do not make full rebuild the routine path for supervisor views.
- Do not drop raw supervisor events if offline drill-down/review remains required.

Open edges:

Final tiered sync topology still requires ADR-003 closure.

Platform specification note:

Use as Phase 2 validation of tiered projection strategy.

## Kernel: Authority Context Bounded Reference Candidate

Status: Superseded
Kind: rejected-alternative

Specification statement:

To avoid an unbounded variable-length envelope field, Phase 2 proposes bounded authority context references: `primary_assignment_ref` for the most-specific active assignment and optional `secondary_assignment_ref` for the standing assignment when the primary assignment is a campaign overlay. ADR-003 course correction supersedes this proposal because Option (c) stores no authority-context assignment references in the event envelope.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding E2
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / hot spot HS-7
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 1 candidate summary
- `docs/exploration/archive/12-adr3-course-correction.md` / `Findings That Strengthen Our Option (c) Choice`

Closure basis:

Superseded by ADR-003 course-correction lineage, pending final ADR-003 verification.

Scope:

Applies to event authority context, campaign overlays, standing assignments, and immutable envelope schema.

Non-goals:

Does not decide final field names beyond source terminology or all possible future assignment types.

Forbidden interpretations:

- Do not use an unbounded list of assignment references without later ADR support.
- Do not lose campaign-vs-standing attribution when both assignments cover an event.
- Do not keep primary/secondary assignment references as the current preferred ADR-003 envelope solution after Option (c).

Open edges:

Authority reconstruction correctness and assignment sync ordering remain unresolved until ADR-003 verification.

Platform specification note:

Use as rejected/superseded envelope-option lineage.

## Kernel: Authority Context Assertion Semantics Candidate

Status: Conditional
Kind: invariant

Specification statement:

Authority context is a device assertion about the authorization believed to be in effect at capture time, not a server-verified fact. The server validates the assertion on sync against assignment activity and the event's knowledge state.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding E3

Closure basis:

Conditional Phase 2 interpretation. Later ADR-003 must confirm this semantic contract.

Scope:

Applies to authority audit, stale assignment validation, `sync_watermark`, and authorization flags.

Non-goals:

Does not decide exact validation algorithm.

Forbidden interpretations:

- Do not treat `authority_context` as proof that authority was active.
- Do not erase the distinction between device belief and server validation outcome.

Open edges:

How server knowledge-state validation is encoded remains to be closed.

Platform specification note:

Use as audit semantics for authority context.

## Kernel: Platform Actor For System Events Candidate

Status: Superseded
Kind: rejected-alternative

Specification statement:

System-generated events need a reserved platform actor identity if `authority_context` is mandatory on every event. ADR-003 course correction supersedes this need by choosing authority-as-projection with no authority-context envelope field; system events can be identified by event type under this lineage.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Finding E5
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / invariant report I2
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 1 candidate summary
- `docs/exploration/archive/12-adr3-course-correction.md` / `Findings That Strengthen Our Option (c) Choice`

Closure basis:

Superseded by ADR-003 course-correction lineage, pending final ADR-003 verification.

Scope:

Applies to conflict detection, generated flags, merge/split processing where system events are emitted, and all event readers.

Non-goals:

Does not decide whether human-initiated administrative events can ever omit assignment references.

Forbidden interpretations:

- Do not treat absent authority context on system events as unrestricted authority.
- Do not require event readers to infer a missing actor from event type alone.
- Do not keep platform-actor identity as a required ADR-003 primitive if no authority-context envelope field is committed.

Open edges:

Final ADR-003 must verify that system-event identification by event type is sufficient.

Platform specification note:

Use as superseded lineage tied to the rejected mandatory authority-context envelope path.

## Kernel: Shared Device Per-Actor Session Candidate

Status: Conditional
Kind: invariant

Specification statement:

Shared devices break if local storage and sync knowledge state are only device-scoped. Phase 2 requires actor-partitioned local storage for shared-device scenarios and either per-actor sync sessions/watermarks or an explicit limitation that shared devices make causal staleness analysis unreliable.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Combo beta
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 9. Assumptions Surfaced` P5
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Bucket 1 and Bucket 2 candidate summaries

Closure basis:

Conditional Phase 2 finding. ADR-003 must decide whether shared devices are supported structurally or explicitly limited.

Scope:

Applies to hardware-bound `device_id`, actor-scoped sync payloads, local storage partitioning, `sync_watermark`, and device sequence behavior.

Non-goals:

Does not reinterpret ADR-002 `device_id` as actor-bound.

Forbidden interpretations:

- Do not share one actor's sync watermark as another actor's knowledge state without explicit closure.
- Do not store shared-device data in a way that relies only on query filtering for actor isolation.

Open edges:

Per-actor watermark/session model versus unsupported/limited shared devices remains to be closed.

Platform specification note:

Use as the key Phase 2 correction to shared-device handling.

## Kernel: Auditor Access Structural Additions Open

Status: Open
Kind: open-question

Specification statement:

Auditor access is not a simple geographic assignment special case. Stress testing requires at least auditor role, read-only or capability-restricted access, query-based scope for cross-hierarchy review, and device-side data expiry for time-limited grants.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / Combo delta
- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 9. Assumptions Surfaced` P6 and P8

Closure basis:

Open Phase 2 structural gap. Later sources must decide initial scope, deferral, or constraint promotion.

Scope:

Applies to external auditors, temporary cross-boundary access, query-scoped access, read-only capabilities, audit findings, and data expiry.

Non-goals:

Does not decide concrete auditor workflows or compliance policy.

Forbidden interpretations:

- Do not assume geographic hierarchy plus role name fully covers auditor access.
- Do not defer auditor access silently if envelope or assignment fields would later be locked.

Open edges:

Initial-platform inclusion versus explicit deferral remains unresolved.

Platform specification note:

Use as auditor access closure tracker before finalizing assignment/envelope fields.

## Kernel: Missing Operational Path Set

Status: Open
Kind: open-question

Specification statement:

Phase 2 surfaces operational paths not addressed by Phase 1: active case reassignment, cross-CHW referral, coordinator connectivity loss during assignment creation, accumulation of expired campaign assignments, and assessments of deactivated actors.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## 10. Missing Operational Paths`

Closure basis:

Open Phase 2 gap set.

Scope:

Applies to case responsibility, referral-driven temporary scope, assignment transactionality, expired assignment filtering, actor lifecycle, and flag taxonomy.

Non-goals:

Does not decide which gaps belong to ADR-003 versus ADR-004/ADR-005 or later phases.

Forbidden interpretations:

- Do not let the Phase 2 constraint set ignore these paths merely because they were missing from Phase 1.

Open edges:

Each path needs ownership classification in later ADR-003 or cross-ADR reconciliation.

Platform specification note:

Use as a checklist to prevent hidden architecture gaps.

## Kernel: ADR-003 Phase 2 Candidate Classification Set

Status: Conditional
Kind: open-question

Specification statement:

Phase 2 classifies several findings as Bucket 1 constraint candidates and Bucket 2 strategy candidates. Constraint candidates include bounded authority references, assignment scope-containment, authorization detect-before-act, alias-respects-original-scope, online-only conflict resolution, platform actor identity, sensitive-subject classification, and actor-partitioned shared-device storage. Strategy candidates include selective retain purge, priority sync, watermark-based auto-resolution, per-actor sync sessions, incremental projections, and batch flag resolution. Course correction later reconciles this classification by superseding the authority-context envelope path, promoting four constraints, and reclassifying several overcalled constraints as strategies or non-ADR-003 structural concerns.

Source basis:

- `docs/exploration/archive/11-adr3-phase2-stress-test.md` / `## Summary: What ADR-003 Must Decide as Constraints`
- `docs/exploration/archive/12-adr3-course-correction.md` / `Stress Test Impact and Classification`

Closure basis:

Conditional classification after course-correction reconciliation. Final verification remains owned by ADR-003.

Scope:

Applies to ADR-003 constraint/strategy classification and cross-ADR deferral handling.

Non-goals:

Does not settle any candidate by itself.

Forbidden interpretations:

- Do not promote Bucket 1 candidates to settled platform constraints until an owning later source commits them.
- Do not drop Bucket 2 strategies if later sources rely on them for feasibility.

Open edges:

ADR-003 must verify which course-correction classifications are committed, deferred, or contradicted.

Platform specification note:

Use as the Phase 2 handoff register for ADR-003 closure.

## Kernel: ADR-003 Course-Correction Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/12-adr3-course-correction.md` is ADR-003 Phase 3 irreversibility filtering and stress-test reconciliation. It narrows ADR-003's claimed irreversible surface, chooses authority-as-projection over authority-context-in-envelope, and reconciles Phase 2 stress-test findings. It remains superseded exploration and must be verified against ADR-003 before final platform closure.

Source basis:

- `docs/exploration/archive/12-adr3-course-correction.md` / supersession notice
- `docs/exploration/archive/12-adr3-course-correction.md` / opening contribution and two-stage description
- `docs/exploration/archive/12-adr3-course-correction.md` / `Stress Test Impact and Classification`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all ADR-003 course-correction lineage.

Non-goals:

Does not itself settle final ADR-003 platform constraints.

Forbidden interpretations:

- Do not treat this source as final ADR-003 text.
- Do not let its reclassification erase Phase 2 risks unless it explicitly explains the replacement classification.

Open edges:

Final verification is owned by `docs/adrs/adr-003-authorization-sync.md`.

Platform specification note:

Use as the reconciliation checkpoint before ADR-003 extraction.

## Kernel: Authority-As-Projection Candidate

Status: Conditional
Kind: contract

Specification statement:

ADR-003 course correction chooses Option (c): events do not store `authority_context` in the immutable envelope. Authority is reconstructed on sync from the actor, subject/process references, assignment timeline, event creation context, and sync knowledge state. Authority is therefore a projection rather than a stored device assertion.

Source basis:

- `docs/exploration/archive/12-adr3-course-correction.md` / `The Envelope Question -- Let's Settle It`
- `docs/exploration/archive/12-adr3-course-correction.md` / `Stress Test Impact and Classification`
- `docs/exploration/archive/12-adr3-course-correction.md` / `Evolvability Analysis` S3

Closure basis:

Conditional ADR-003 course-correction decision. Final ADR-003 must verify whether it is committed.

Scope:

Applies to event envelope design, assignment timeline reconstruction, authority validation on sync, campaign/process attribution, and audit interpretation.

Non-goals:

Does not decide storage/indexing of assignment timelines or all performance optimizations.

Forbidden interpretations:

- Do not require `assignment_ref`, assignment-ref lists, or platform actor authority context in every event under this lineage.
- Do not treat derived authority as unknowable merely because it is not stored in the event envelope.

Open edges:

Assignment events must be reliably available before or with work events for reconstruction. This sync-ordering requirement remains to be verified by ADR-003.

Platform specification note:

Use as the current ADR-003 envelope candidate pending ADR verification.

## Kernel: Assignment Sync Ordering Requirement Candidate

Status: Conditional
Kind: invariant

Specification statement:

Authority-as-projection is valid only if assignment events needed to reconstruct authority are synced before, or atomically with, the work events they authorize. If a work event reaches the server before the relevant assignment history, authority cannot be reconstructed at that moment.

Source basis:

- `docs/exploration/archive/12-adr3-course-correction.md` / Option (c) verdict and important note
- `docs/exploration/archive/12-adr3-course-correction.md` / `Evolvability Analysis` S3

Closure basis:

Conditional requirement implied by the chosen Option (c). Final ADR-003 must confirm whether this becomes a sync invariant or strategy.

Scope:

Applies to assignment event delivery, sync ordering, partial sync, first sync, campaign assignment sync, and authority reconstruction.

Non-goals:

Does not define transport-level bundling or retry protocol.

Forbidden interpretations:

- Do not choose authority-as-projection while leaving assignment/work sync ordering unspecified.

Open edges:

Need final classification of assignment-before-work delivery as constraint, protocol strategy, or accepted risk.

Platform specification note:

Use as the key viability condition for authority-as-projection.

## Kernel: ADR-003 Course-Correction Constraint Promotion Set

Status: Conditional
Kind: invariant

Specification statement:

The course-correction source promotes four Phase 2 stress-test findings as genuine ADR-003 constraint-level updates: server-side scope containment on assignment creation, alias-respects-original-scope authorization evaluation, online-only conflict resolution, and detect-before-act coverage for authorization flags.

Source basis:

- `docs/exploration/archive/12-adr3-course-correction.md` / `Findings That Require ADR-003 Updates (Constraint-level)`
- `docs/exploration/archive/12-adr3-course-correction.md` / `Evolvability Analysis` S4, S5, S6, S7

Closure basis:

Conditional course-correction promotion pending ADR-003 verification.

Scope:

Applies to assignment writes, scope-crossing identity merge projection, conflict-resolution transactions, and sync-time flag processing.

Non-goals:

Does not settle per-flag severity configuration or all future scope types.

Forbidden interpretations:

- Do not bury these four findings as mere implementation concerns after the course-correction source promotes them.
- Do not treat all Phase 2 Bucket 1 candidates as equally promoted.

Open edges:

ADR-003 must verify whether these are committed as S4-S7 or otherwise named commitments.

Platform specification note:

Use as pre-ADR checklist for ADR-003 extraction.

## Kernel: ADR-003 Strategy Reclassification Set

Status: Conditional
Kind: configuration-boundary

Specification statement:

The course-correction source reclassifies several Phase 2 findings as strategies rather than irreversible constraints: priority sync, data removal policy, supervisor freshness metadata, watermark-based auto-resolution, and shared-device per-actor sessions.

Source basis:

- `docs/exploration/archive/12-adr3-course-correction.md` / `Findings Correctly Classified as Strategies`

Closure basis:

Conditional course-correction classification pending ADR-003 verification.

Scope:

Applies to sync optimization, local data lifecycle policy, summary metadata, flag auto-resolution logic, and shared-device sync sessions.

Non-goals:

Does not say these items are unimportant or optional for every deployment.

Forbidden interpretations:

- Do not promote these items to immutable envelope constraints from Phase 2 alone.
- Do not discard them as irrelevant; they remain required strategy pressure where the platform supports the corresponding deployment conditions.

Open edges:

Final ADR-003 must confirm which are explicit strategies, accepted risks, or deferred items.

Platform specification note:

Use to prevent over-hardening evolvable ADR-003 strategy items.

## Kernel: ADR-003 Stress-Test Overcall Correction Set

Status: Conditional
Kind: forbidden-interpretation

Specification statement:

The course-correction source explicitly says Phase 2 overcalled several findings as structural constraints: sensitive-subject classification is strategy/configuration with a structural capability boundary, actor-partitioned local storage is implementation, actor-as-subject visibility is sync filter logic rather than a new assignment primitive, and auditor access is deferred/evolvable through roles, capabilities, assignment configuration, and sync behavior.

Source basis:

- `docs/exploration/archive/12-adr3-course-correction.md` / `Findings That the Stress Test Overcalls`

Closure basis:

Conditional course-correction classification pending ADR-003 verification.

Scope:

Applies to sensitive-subject sync behavior, shared-device local storage, actor-as-subject assessment delivery, and auditor access.

Non-goals:

Does not resolve the operational need for these capabilities or their final ADR ownership.

Forbidden interpretations:

- Do not treat Phase 2's Bucket 1 label as final for these items.
- Do not silently remove the risks; reclassify them with explicit ownership.

Open edges:

ADR-003 and later ADR-004/flag sources must verify ownership and closure.

Platform specification note:

Use as a correction layer over Phase 2 classification.

## Kernel: ADR-003 Course-Correction Residual Risk

Status: Open
Kind: open-question

Specification statement:

The course-correction source addresses the largest Phase 2 classification conflict by choosing no authority-context envelope field, but it leaves residual verification needs: whether assignment sync ordering is strong enough for authority reconstruction, whether reclassified strategy items are explicitly carried as strategies or deferred, and whether auditor/sensitive/shared-device risks are intentionally out of ADR-003's constraint surface.

Source basis:

- `docs/exploration/archive/12-adr3-course-correction.md` / Option (c) important note
- `docs/exploration/archive/12-adr3-course-correction.md` / `Findings Correctly Classified as Strategies`
- `docs/exploration/archive/12-adr3-course-correction.md` / `Findings That the Stress Test Overcalls`
- `docs/exploration/archive/12-adr3-course-correction.md` / `Evolvability Analysis`

Closure basis:

Open pre-ADR verification risk.

Scope:

Applies to ADR-003 extraction and later cross-ADR ownership checks.

Non-goals:

Does not reopen ADR-002 or decide ADR-004/ADR-005.

Forbidden interpretations:

- Do not equate "not irreversible" with "not platform-relevant."
- Do not accept Option (c) without checking final ADR-003 for assignment-sync ordering and reconstruction semantics.

Open edges:

Final ADR-003 extraction must confirm closure, deferral, or contradiction for each residual risk.

Platform specification note:

Use as the ADR-003 final-extraction checklist.

## Kernel: ADR-003 Decision Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/adrs/adr-003-authorization-sync.md` is the decided ADR-003 source for authorization and selective sync. It commits assignment-based access control, sync scope as offline authorization, authority-as-projection, alias-respects-original-scope, scope containment for assignment creation, online-only conflict resolution, detect-before-act coverage for authorization flags, and initial evolvable strategies for projection, staleness handling, and scope-change data handling.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / status and `## Decision`
- `docs/adrs/adr-003-authorization-sync.md` / `## Traceability`

Closure basis:

ADR-settled extraction boundary.

Scope:

Applies to ADR-003 kernels and their closure status.

Non-goals:

Does not decide ADR-004 configuration boundary or ADR-005 workflow behavior.

Forbidden interpretations:

- Do not use ADR-003 exploration files to override the decided ADR where the ADR commits a different classification.
- Do not treat ADR-003 strategies as immutable event-envelope constraints unless the ADR marks them structural.

Open edges:

ADR-004 and ADR-005 deferrals remain open until their owning sources are processed.

Platform specification note:

Use ADR-003 as the closure source for authorization and selective-sync kernels.

## Kernel: ADR-003 Assignment Access Contract

Status: Settled
Kind: contract

Specification statement:

Every access rule reduces to checking whether the actor has an active assignment whose scope contains the target entity and whose role permits the intended action. The assignment is a structured typed grant; role qualification is secondary to scope containment, and role-action permissions are deployment configuration owned by ADR-004.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S1
- `docs/adrs/adr-003-authorization-sync.md` / `What This Does NOT Decide`
- `docs/adrs/adr-003-authorization-sync.md` / `Consequences`

Closure basis:

ADR-settled.

Scope:

Applies to authorization checks for actor operations against subjects, processes, assignments, and configured action types.

Non-goals:

Does not define concrete roles, role hierarchies, role-action permission tables, or all scope types.

Forbidden interpretations:

- Do not model ADR-003 authorization as generic RBAC without scope.
- Do not model ADR-003 authorization as generic open-ended ABAC.
- Do not hard-code deployment role permissions as ADR-003 structural platform rules.

Open edges:

ADR-004 must close how assignments, scope definitions, and role-action tables are configured and synced.

Platform specification note:

Use as the platform authorization contract.

## Kernel: ADR-003 Sync Scope Access Invariant

Status: Settled
Kind: invariant

Specification statement:

Sync scope equals access scope. A device receives exactly the data its actor is authorized to act on, computed server-side from the actor's active assignments at sync time. Device-side access control is UI behavior, not authoritative policy enforcement.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S2
- `docs/adrs/adr-003-authorization-sync.md` / `Consequences` / Sync protocol

Closure basis:

ADR-settled.

Scope:

Applies to offline field devices, selective sync, server sync filtering, scope expansion, and scope contraction.

Non-goals:

Does not decide pagination, priority ordering, compression, low-bandwidth optimization, or local data purge mechanics.

Forbidden interpretations:

- Do not deliver data outside the actor's authorized scope as a convenience cache.
- Do not require a full offline policy engine on the device for core scope enforcement.
- Do not treat UI hiding as a substitute for server-side sync filtering.

Open edges:

Implementation strategies for bandwidth, shared devices, and data removal remain evolvable or deferred as ADR-003 states.

Platform specification note:

Use as the selective-sync authorization invariant.

## Kernel: ADR-003 Authority-As-Projection Contract

Status: Settled
Kind: contract

Specification statement:

ADR-003 adds no new event-envelope fields. Authority context is derived from the assignment event timeline rather than stored in events. Assignment events are expected to be on the server before the work events they authorize, because assignments are created by online actors, delivered to workers by sync, and work events sync back to a server that already has the authorizing assignments.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S3
- `docs/adrs/adr-003-authorization-sync.md` / `Consequences` / Event envelope
- `docs/adrs/adr-003-authorization-sync.md` / `Risks accepted`

Closure basis:

ADR-settled.

Scope:

Applies to event envelopes, authority reconstruction, assignment timelines, audit reconstruction, and future envelope extensibility.

Non-goals:

Does not forbid a future ADR from adding `authority_context` to new events through ADR-001 envelope extensibility.

Forbidden interpretations:

- Do not add ADR-003 `assignment_ref`, assignment-ref lists, process refs, or platform actor authority fields to the event envelope.
- Do not treat missing `authority_context` as an absence of auditable authority.
- Do not treat authority reconstruction performance risk as a reason to reclassify ADR-003 S3 without a revisit trigger.

Open edges:

Authority reconstruction performance is an accepted risk with revisit trigger. ADR-004 still owns configurable role/scope definitions.

Platform specification note:

Use as the final ADR-003 answer to the authority-context envelope question.

## Kernel: ADR-003 Alias Original Scope Invariant

Status: Settled
Kind: invariant

Specification statement:

Authorization for an event is evaluated against the original `subject_ref` written into the immutable event, not against the post-merge surviving subject's scope. Identity aliasing is projection behavior and does not create authorization grants.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S4
- `docs/adrs/adr-003-authorization-sync.md` / `Consequences` / Authorization after merges
- `docs/adrs/adr-003-authorization-sync.md` / `Traceability`

Closure basis:

ADR-settled.

Scope:

Applies to scope-crossing subject merges, alias projection, stale references, and authorization evaluation for historical events.

Non-goals:

Does not redefine ADR-002 alias mechanics or subject merge validation.

Forbidden interpretations:

- Do not evaluate historical event authority against the surviving subject's current scope after a merge.
- Do not treat an identity merge as authorization expansion.

Open edges:

Flag metadata and UI treatment for scope-crossing merged events may still be refined by implementation or later flag semantics sources.

Platform specification note:

Use as the authorization rule that preserves ADR-002 identity aliasing without scope escalation.

## Kernel: ADR-003 Assignment Creation Scope-Containment Contract

Status: Settled
Kind: invariant

Specification statement:

An `AssignmentCreated` command must be validated server-side so the new assignment's scope is contained within the creating actor's own assignment scope. The invariant may be relaxed for specific configured role types such as super-coordinators, but default behavior is containment.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S5
- `docs/adrs/adr-003-authorization-sync.md` / `Traceability`

Closure basis:

ADR-settled.

Scope:

Applies to assignment creation, coordinator authority, campaign assignment creation, and server precondition validation.

Non-goals:

Does not define concrete super-coordinator roles or all delegation policies.

Forbidden interpretations:

- Do not rely on UI or app-layer convention for assignment scope containment.
- Do not accept assignment creation that bypasses server-side creator-scope validation.

Open edges:

ADR-004 owns configuration of role types and scope definitions.

Platform specification note:

Use as the assignment-write authorization invariant.

## Kernel: ADR-003 Conflict Resolution Online-Only Invariant

Status: Settled
Kind: invariant

Specification statement:

`ConflictResolved` events can only be created through a server-validated transaction. This extends the ADR-002 online-only precedent for merge/split operations to conflict resolution so resolver authority is verified before the resolution event is committed.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S6
- `docs/adrs/adr-003-authorization-sync.md` / `Consequences` / ADR-5

Closure basis:

ADR-settled.

Scope:

Applies to conflict resolution, authorization of resolvers, meta-flag prevention, and workflow consequences of conflict resolution.

Non-goals:

Does not decide whether specific low-severity flag types may later be relaxed by deployment strategy.

Forbidden interpretations:

- Do not allow offline conflict resolution where resolver authority cannot be validated before commit.
- Do not allow stale resolver decisions to create recursive authority flag chains.

Open edges:

ADR-005 owns workflow behavior when conflict resolution invalidates downstream state or interacts with approval chains.

Platform specification note:

Use as the conflict-resolution authorization invariant.

## Kernel: ADR-003 Authorization Detect-Before-Act Contract

Status: Settled
Kind: algorithm

Specification statement:

ADR-002 detect-before-act extends to all flag types, including authorization flags. Blocking flags prevent downstream policy execution until resolved. Per-flag severity is configurable; ADR-003 initial configuration treats `ScopeStaleFlag` and `TemporalAuthorityExpiredFlag` as informational and `RoleStaleFlag` as blocking for capability-restricted actions.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S7
- `docs/adrs/adr-003-authorization-sync.md` / S9
- `docs/adrs/adr-003-authorization-sync.md` / `Consequences` / ADR-4 and ADR-5

Closure basis:

ADR-settled for mechanism scope; per-flag severity remains configurable.

Scope:

Applies to sync processing, authorization flags, downstream policy execution, and workflow interaction with blocking flags.

Non-goals:

Does not decide all future flag types, final severity taxonomy, or flag UI.

Forbidden interpretations:

- Do not let blocking authorization flags trigger irreversible downstream policy actions before resolution.
- Do not hard-code all flag severities as platform constants.

Open edges:

ADR-004 owns per-flag-type severity configuration. ADR-005 owns whether blocking flags suspend in-progress workflow steps or only prevent new ones.

Platform specification note:

Use as the authorization extension of ADR-002 conflict processing.

## Kernel: ADR-003 Tiered Projection Strategy

Status: Settled
Kind: interaction-rule

Specification statement:

ADR-003 initial projection strategy is tiered: field workers use device-local projections from raw events; supervisors use hybrid sync with raw events for review visits plus server-computed summaries; coordinators use server-computed projections online. Devices sync directly with the server rather than relaying through supervisors.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S8
- `docs/adrs/adr-003-authorization-sync.md` / `Risks accepted`

Closure basis:

ADR-settled as initial evolvable strategy, not immutable envelope constraint.

Scope:

Applies to projection location, direct sync topology, supervisor review, and coordinator dashboards.

Non-goals:

Does not make supervisor hybrid projection permanent if revisit triggers fire.

Forbidden interpretations:

- Do not require one projection location for all tiers.
- Do not infer supervisor relay sync topology.

Open edges:

Supervisor hybrid projection is an accepted risk with revisit trigger.

Platform specification note:

Use as initial projection/sync topology strategy.

## Kernel: ADR-003 Authorization Staleness Strategy

Status: Settled
Kind: interaction-rule

Specification statement:

Authorization staleness uses ADR-002 accept-and-flag. ADR-003 introduces `ScopeStaleFlag`, `RoleStaleFlag`, and `TemporalAuthorityExpiredFlag`; assigns multiple flags on one event to one broadest-scope resolver; and uses watermark-based auto-resolution for `ScopeStaleFlag` where the event watermark predates the triggering assignment-end watermark.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S9
- `docs/adrs/adr-003-authorization-sync.md` / S7

Closure basis:

ADR-settled as initial evolvable strategy, constrained by detect-before-act.

Scope:

Applies to stale scope, stale role, expired temporal authority, resolver assignment, and batch/auto-resolution.

Non-goals:

Does not finalize all flag semantics or severity configuration.

Forbidden interpretations:

- Do not reject validly structured offline events solely due to authorization staleness.
- Do not resolve multiple flags on one event independently when the ADR assigns them to one broadest-scope resolver.

Open edges:

Later flag semantics ADRs and ADR-004 may refine severity, configuration, and flag taxonomy.

Platform specification note:

Use as initial authorization-staleness handling.

## Kernel: ADR-003 Scope Change Data Handling Strategy

Status: Settled
Kind: interaction-rule

Specification statement:

Scope expansion is additive. For scope contraction, ADR-003 initial strategy is selective retain: own events are retained; others' events about out-of-scope subjects are candidates for device-side removal. Non-sensitive data may retain all; sensitive personal data recommends crash-safe journaled selective purge. Retain-but-hide is not recommended for sensitive data.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S10
- `docs/adrs/adr-003-authorization-sync.md` / `Risks accepted`

Closure basis:

ADR-settled as initial evolvable strategy and policy boundary.

Scope:

Applies to reassignment, campaign end, scope narrowing, local data lifecycle, and device-side storage policy.

Non-goals:

Does not define the exact purge protocol, crash journal format, or deployment sensitivity classes.

Forbidden interpretations:

- Do not represent device-side purge as a server sync instruction required by ADR-003.
- Do not treat retain-but-hide as recommended for sensitive data.

Open edges:

ADR-004 owns sensitivity classification. Selective-retain storage accumulation is an accepted risk with revisit trigger.

Platform specification note:

Use as scope-change local data policy lineage.

## Kernel: ADR-003 Explicit Deferral Contract

Status: Settled
Kind: open-question

Specification statement:

ADR-003 explicitly defers subject-based scope, auditor access, device sharing, sync pagination/priority/bandwidth, assessment visibility to assessed worker, sensitive-subject classification, grace-period policy, role-action permission tables, and cross-level distribution visibility to ADR-004, strategy, implementation, or deployment policy as named in the ADR. ADR-005 owns workflow interactions with assignment changes, conflict resolution invalidation, blocking flags, and approval chains.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / `What This Does NOT Decide`
- `docs/adrs/adr-003-authorization-sync.md` / `Consequences`
- `docs/adrs/adr-003-authorization-sync.md` / `Next Decision`

Closure basis:

ADR-settled deferral map.

Scope:

Applies to ADR-003 cross-ADR boundaries and residual Phase 2/course-correction risks.

Non-goals:

Does not decide the deferred items.

Forbidden interpretations:

- Do not promote deferred configuration or implementation items into ADR-003 structural constraints.
- Do not drop deferred concerns from later extraction passes.

Open edges:

ADR-004 and ADR-005 must close or carry the relevant deferrals.

Platform specification note:

Use as the handoff contract from ADR-003 to ADR-004/ADR-005.

## Kernel: ADR-003 Accepted Risk Contract

Status: Settled
Kind: conditional-validity

Specification statement:

ADR-003 accepts specific risks with revisit triggers: authority reconstruction may be slow, selective-retain may accumulate out-of-scope own-event data, blocking flags may delay valid work, supervisor hybrid projection is least validated, shared-device causal ordering is known to be corrupt without per-actor sessions, and first-sync on 2G may be prohibitive without priority sync.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / `Risks accepted`

Closure basis:

ADR-settled accepted-risk contract.

Scope:

Applies to ADR-003 operational validity and future revisit conditions.

Non-goals:

Does not resolve the risks immediately.

Forbidden interpretations:

- Do not treat accepted risks as forgotten issues.
- Do not treat shared-device causal ordering as reliable until per-actor sessions or another mitigation is implemented.

Open edges:

Revisit triggers and later ADR ownership determine when these risks require redesign or strategy change.

Platform specification note:

Use as ADR-003 conditional validity and monitoring context.

## Kernel: ADR-003 Reconciliation Result

Status: Settled
Kind: invariant

Specification statement:

ADR-003 confirms the course-correction reconciliation: authority context is not stored in the event envelope; authority-as-projection is committed; Phase 2 bounded-reference and platform-actor envelope candidates are rejected/superseded; the four course-correction constraint promotions are carried into S4-S7; and Phase 2 overcalled items are either strategies, ADR-004/configuration items, implementation items, accepted risks, or explicit deferrals.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md` / S3 through S10
- `docs/adrs/adr-003-authorization-sync.md` / `What This Does NOT Decide`
- `docs/adrs/adr-003-authorization-sync.md` / `Traceability`

Closure basis:

ADR-settled reconciliation result.

Scope:

Applies to resolving ADR-003 Phase 1, Phase 2, and course-correction lineage.

Non-goals:

Does not close ADR-004 or ADR-005 deferrals.

Forbidden interpretations:

- Do not continue treating Phase 2 authority-context envelope candidates as live options for ADR-003.
- Do not ignore Phase 2 risks that ADR-003 explicitly accepts or defers.

Open edges:

ADR-004 is the next decision source for configuration boundary, including role-action tables, scope types, sensitivity, and flag severity.

Platform specification note:

Use as the final ADR-003 lineage reconciliation before moving to ADR-004.
