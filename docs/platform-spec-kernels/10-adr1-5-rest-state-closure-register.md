# ADR-001 To ADR-005 Rest-State Closure Register

Status: Iteration 40 validated closure overlay

This file is a compact closure overlay for ADR-001 through ADR-005. It does not replace the larger staging files and does not delete lineage. The staging files remain the evidence trail; this register records the current rest-state baseline that later platform-spec section drafting and ADR-006+ assessment must preserve.

## Use Rules

- This file consolidates ADR-001 through ADR-005 only.
- Source authority remains the approved ADR/exploration sources named in the staging kernels.
- "Covered by" means the listed staging kernels are represented by the compact closure item; it does not remove or rewrite them.
- If a later source conflicts with a closed item here, classify the later claim as conflict/dispute/contextual rather than silently changing this baseline.
- ADR-006-R through ADR-009 are not closure sources for this register. They must be assessed separately against this baseline rather than silently changing it.

## Settled Platform Kernels

### Immutable Event Log Source Of Truth

The platform stores operational facts as typed immutable events in an append-only event log. The event log is the source of truth; state changes enter through the event store; projections and views are derived and repaired by recomputation from events, not by direct canonical patching.

Source basis:

- `docs/adrs/adr-001-offline-data-model.md`

Covered by:

- `ADR-001 Immutable Event Store Primitive`
- `ADR-001 Append-Only Write Invariant`
- `ADR-001 Write-Path Source-Of-Truth Discipline`
- `ADR-001 Projection Rebuild Scope Boundary`
- `ADR-001 Reconciliation Result`

### Event Envelope Baseline And Stability

Every event envelope must express identity, type, payload, and timestamp. Later ADRs extend/shape the envelope within their owned boundaries: ADR-002 adds causal/device/typed-identity reference requirements; ADR-003 rejects stored authority context as an envelope field; ADR-004 settles `shape_ref`, optional `activity_ref`, structural type vocabulary, and system actor convention; ADR-005 adds no envelope fields and no structural event type.

Source basis:

- `docs/adrs/adr-001-offline-data-model.md`
- `docs/adrs/adr-002-identity-conflict.md`
- `docs/adrs/adr-003-authorization-sync.md`
- `docs/adrs/adr-004-configuration-boundary.md`
- `docs/adrs/adr-005-state-progression.md`

Covered by:

- `ADR-001 Minimum Event Envelope Expression`
- `ADR-002 Envelope Contract`
- `ADR-003 Authority-As-Projection Contract`
- `ADR-004 Envelope Configuration Contract`
- `ADR-004 Structural Event Type Contract`
- `ADR-005 No Structural Change Contract`

### Client-Generated Identity And Lineage Preservation

Events, subjects, and records use client-generated UUIDs for offline creation. Subject identity evolves by alias/projection and corrective split behavior rather than rewriting historical event references. Lineage is acyclic, merge/split operations are online-only where required, and raw references remain available for conflict and authorization checks.

Source basis:

- `docs/adrs/adr-001-offline-data-model.md`
- `docs/adrs/adr-002-identity-conflict.md`

Covered by:

- `ADR-001 Client-Generated Identifier Contract`
- `ADR-002 Identity Evolution Contract`
- `ADR-002 Lineage Validation Contract`
- `ADR-002 Conflict Contract`

### Event Ordering And Device-Time Semantics

Device time is recorded for display and audit, but projection logic, conflict detection, and protocol correctness do not depend on device clock time for ordering. Intra-device ordering uses `device_sequence`; cross-device concurrency detection uses `sync_watermark`; the device identity namespace is hardware/app-installation-bound rather than actor-bound.

Source basis:

- `docs/adrs/adr-002-identity-conflict.md`

Covered by:

- `ADR-002 Envelope Contract`
- `ADR-002 Device Time Advisory Invariant`

### Immutable Event Sync And Access-Scoped Delivery

The sync unit is the immutable event. Sync is idempotent, append-only, order-independent, and filtered by assigned scope. ADR-003 closes access as assignment-derived and sync scope as access scope, with projection strategy and scope-change handling treated as ADR-settled evolvable strategies rather than envelope constraints.

Source basis:

- `docs/adrs/adr-001-offline-data-model.md`
- `docs/adrs/adr-003-authorization-sync.md`

Covered by:

- `ADR-001 Immutable Event Sync Contract`
- `ADR-003 Assignment Access Contract`
- `ADR-003 Sync Scope Access Invariant`
- `ADR-003 Tiered Projection Strategy`
- `ADR-003 Scope Change Data Handling Strategy`

### Authority As Projection

Events do not store immutable `authority_context`. Authority is reconstructed on sync from actor, subject/process references, assignment timeline, event creation context, and sync knowledge state. Authorization is checked against the original subject reference written into the event, not against post-merge alias projections.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md`

Covered by:

- `ADR-003 Authority-As-Projection Contract`
- `ADR-003 Alias Original Scope Invariant`
- `ADR-003 Assignment Creation Scope-Containment Contract`

### Detect-Before-Act And Accept-And-Flag Discipline

Conflict and authorization checks run before downstream policy or workflow action. Validly structured stale or invalid work is accepted as an immutable event and surfaced with flags rather than rejected, except where a later closed kernel explicitly defines online-only resolution operations.

Source basis:

- `docs/adrs/adr-002-identity-conflict.md`
- `docs/adrs/adr-003-authorization-sync.md`
- `docs/adrs/adr-005-state-progression.md`

Covered by:

- `ADR-002 Conflict Contract`
- `ADR-002 Stale Event Acceptance Invariant`
- `ADR-003 Conflict Resolution Online-Only Invariant`
- `ADR-003 Authorization Detect-Before-Act Contract`
- `ADR-003 Authorization Staleness Strategy`
- `ADR-005 Transition Violation Flag Contract`

### Platform-Fixed Structural Types And Deployer-Configured Shapes

The platform owns the structural event type vocabulary and processing semantics. Deployers configure shapes, activities, roles, schedules, thresholds, severities, sensitivity parameters, and policy choices inside bounded configuration surfaces. ADR-004 fixes six structural event types; ADR-005 rejects `status_changed` as a seventh type.

Source basis:

- `docs/adrs/adr-004-configuration-boundary.md`
- `docs/adrs/adr-005-state-progression.md`

Covered by:

- `ADR-004 Structural Event Type Contract`
- `ADR-004 Configuration Gradient Contract`
- `ADR-004 Deployer Policy Configuration Contract`
- `ADR-005 No Structural Change Contract`

### Configuration Evaluation Limits

Configuration is bounded by the ADR-004 gradient, expression limits, server-only triggers where required, atomic config packages, trigger DAG depth constraints, complexity budgets, deployer identifier naming rules, and platform/deployer responsibility boundaries. Deployer-authored arbitrary access-control logic and field-level sensitivity are rejected.

Source basis:

- `docs/adrs/adr-004-configuration-boundary.md`

Covered by:

- `ADR-004 Strategy-Protecting Constraint Contract`
- `ADR-004 Shape Evolution And Logic Strategy Contract`
- `ADR-004 Trigger And Complexity Budget Contract`
- `ADR-004 Deployer Policy Configuration Contract`

### Deployer Policy Configuration Boundaries

Deployers may configure policy values over platform-owned vocabularies and mechanisms, but they do not own the vocabularies or mechanisms themselves. This includes per-deployment flag severity overrides, domain uniqueness constraints evaluated optimistically on device and authoritatively on server, composition of platform-fixed scope types, and shape/activity-level sensitivity classification. These policy surfaces must not add envelope fields or become deployer-authored platform logic.

Source basis:

- `docs/adrs/adr-004-configuration-boundary.md`

Covered by:

- `ADR-004 Deployer Policy Configuration Contract`
- `ADR-004 Configuration Gradient Contract`
- `ADR-004 Strategy-Protecting Constraint Contract`

### Scope-Change Local Data Strategy

Scope expansion is additive. For scope contraction, ADR-003's initial strategy is selective retain: an actor's own events are retained, while other actors' events about out-of-scope subjects are candidates for device-side removal. Sensitive deployments require stronger local lifecycle handling than retain-and-hide.

Source basis:

- `docs/adrs/adr-003-authorization-sync.md`

Covered by:

- `ADR-003 Scope Change Data Handling Strategy`
- `ADR-003 Accepted Risk Contract`

### Projection-Derived Workflow State

Workflow state is derived from immutable event sequences plus pattern definitions. State is not stored as canonical event state, invalid transitions are accepted and flagged rather than rejected, and unresolved flagged events remain visible in timeline while excluded from workflow state-machine evaluation until accepted resolution re-derives state.

Source basis:

- `docs/adrs/adr-005-state-progression.md`

Covered by:

- `ADR-005 Projection-Derived State Machine Contract`
- `ADR-005 Flagged Event State Derivation Contract`
- `ADR-005 Pattern Registry Contract`
- `ADR-005 Pattern Composition Contract`

### Workflow Flag Lineage And Auto-Resolution Boundary

ADR-005 closes workflow-specific flag behavior as source-only flagging with source-chain traversal, platform-defined resolvability classification, L3b auto-resolution for eligible cases, and `system:auto_resolution/{policy_id}` actor attribution. This is not a general ADR-006 flag-semantics closure.

Source basis:

- `docs/adrs/adr-005-state-progression.md`

Covered by:

- `ADR-005 Transition Violation Flag Contract`
- `ADR-005 Flag Resolvability Classification Contract`
- `ADR-005 Source-Only Flagging Contract`
- `ADR-005 Auto-Resolution L3b Contract`
- `ADR-005 Auto-Resolution Actor Contract`

### Bounded Context Expressions

ADR-005 closes `context.*` only for form expressions and only for the seven platform-fixed pre-resolved values it names. Trigger expressions do not gain `context.*`, and the decision does not authorize dynamic joins, arbitrary projections, aggregates, functions, or live updates.

Source basis:

- `docs/adrs/adr-005-state-progression.md`

Covered by:

- `ADR-005 Context Expression Scope Contract`

## Open Questions

Open items here remain open only where not already closed by ADR-001 through ADR-005.

- Retention, archival, setup experience, onboarding/role transition, reporting aggregation, and domain-agnostic proof gaps remain open from ground-truth/viability extraction unless later platform-spec work closes them.
- Subject-based scope, auditor access, shared device actor scope, assessment visibility, sensitive-subject classification, grace-period policy, permission table details, and cross-level distribution visibility remain open or policy/implementation-owned after ADR-003/ADR-004 closure.
- Domain conflict resolution automation remains open after ADR-004/ADR-005 except where ADR-005 closes workflow `transition_violation`, resolvability class, and L3b auto-resolution.
- Workflow-aware reporting and aggregation remains open after ADR-005.
- Consolidated platform specification document remains open after ADR-005.

Source/covered-by anchors:

- `ADR-003 Explicit Deferral Contract`
- `ADR-004 Explicit Deferral Contract`
- `ADR-005 Explicit Non-Decisions`
- `Setup Experience Blind Spot`
- `Retention And Archival Blind Spot`
- `Onboarding And Role Transition Blind Spot`
- `Reporting Aggregation Blind Spot`
- `Domain-Agnosticism Proof Gap`

## Rejected Alternatives

- Mutable canonical records plus separate audit log are rejected as the source-of-truth model.
- Snapshot-primary and action-log-primary storage are rejected as foundational storage primitives; snapshots/materialized views remain valid only as derived read models, summaries, exports, or repair artifacts.
- Server-allocated identifiers for offline event/subject/record creation are rejected.
- Last-write-wins and invisible automatic merge are rejected for operational conflicts requiring judgment.
- Stored immutable `authority_context` in the event envelope is rejected by ADR-003.
- Structural ordering by `device_time` is rejected; device time is advisory for display and audit only.
- Deployer-authored arbitrary access-control logic and field-level sensitivity are rejected by ADR-004.
- Retain-but-hide is not recommended for sensitive data under ADR-003 scope-contraction strategy.
- `status_changed`, `current_state`, and `pattern_ref` are rejected as ADR-005 structural additions.
- Workflow invalid-transition rejection is rejected; invalid transitions are accepted and flagged under the ADR-005 workflow contract.

Source/covered-by anchors:

- `ADR-001 Rejected Storage Alternatives`
- `ADR-002 Conflict Contract`
- `ADR-002 Device Time Advisory Invariant`
- `ADR-003 Authority-As-Projection Contract`
- `ADR-003 Scope Change Data Handling Strategy`
- `ADR-004 Strategy-Protecting Constraint Contract`
- `ADR-004 Deployer Policy Configuration Contract`
- `ADR-005 No Structural Change Contract`
- `ADR-005 Transition Violation Flag Contract`

## Deferred Implementation / Specification Details

These are not unresolved architecture disputes unless a later valid source turns them into one.

- Projection optimization, projection caching, rebuild implementation, low-end device performance strategy, and materialized-view physical design.
- Event schema/versioning tooling and projection merge strategy across schema versions.
- Configuration authoring format, deployment packaging UX, deploy-time validator UX, and migration tooling for breaking changes.
- Exact Pattern Registry inventory, pattern skeletons, and formal pattern schema format.
- Source-chain traversal depth limits.
- Auto-resolution authoring UX and reporting/monitoring surface.
- Sync pagination, priority, bandwidth handling, transport details, and operational delivery mechanics.
- Accepted-risk revisit triggers from ADR-001 through ADR-005 remain monitoring inputs for platform specification and implementation work.
- Platform-spec section drafting and final document structure.

Source/covered-by anchors:

- `ADR-001 Accepted Risk Set`
- `ADR-002 Accepted Risk Contract`
- `ADR-003 Accepted Risk Contract`
- `ADR-003 Explicit Deferral Contract`
- `ADR-004 Explicit Deferral Contract`
- `ADR-005 Explicit Non-Decisions`
- `ADR-005 Accepted Risk Set`

## Dispute / Assessment Lane For ADR-006+

ADR-006-R through ADR-009 are not used as closure sources in this file. Their claims must be assessed separately against the ADR-001 through ADR-005 closure baseline.

Later assessment must classify each ADR-006-R through ADR-009 claim as one of:

- consistent elaboration of a settled ADR-001 through ADR-005 kernel
- valid candidate closure for an item explicitly left open above
- deferred implementation/specification detail
- new unauthorized claim
- conflict with an already closed kernel
- valid dispute against a closed kernel, requiring explicit reconsideration rather than silent supersession

Flag-specific assessment must keep these boundaries separate:

- flag category creation
- conflict detection timing
- source-only cascade/projection lineage
- unresolved-flag state derivation
- flag resolution and auto-resolution
- general ADR-006+ flag semantics

ADR-006-R may supersede ADR-006 only inside the ADR-006 revision lineage. Neither ADR-006-R nor ADR-007 through ADR-009 supersedes ADR-001 through ADR-005.

## Validation Checklist

- ADR-001 through ADR-005 decision and reconciliation kernels are represented above.
- Explicit ADR-003, ADR-004, and ADR-005 deferrals are represented as open or implementation/specification-deferred.
- ADR-006-R through ADR-009 are not used as closure sources.
- No final atomic platform-spec files are created by this register.
- Original staging files remain intact as the evidence trail.

Decision/reconciliation anchor coverage:

- `ADR-001 Decision Boundary`
- `ADR-001 Reconciliation Result`
- `ADR-002 Decision Boundary`
- `ADR-002 Reconciliation Result`
- `ADR-003 Decision Boundary`
- `ADR-003 Reconciliation Result`
- `ADR-004 Decision Boundary`
- `ADR-004 Reconciliation Result`
- `ADR-005 Decision Boundary`
- `ADR-005 Reconciliation Result`
