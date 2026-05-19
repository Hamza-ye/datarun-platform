# Architecture Baseline v0

Status: Accepted baseline from ADR-001 through ADR-005 closure

This document is the current engineering-facing architecture baseline. It is generated from `../10-adr1-5-rest-state-closure-register.md`, not directly from ADR prose. It is not a final platform specification and does not absorb ADR-006-R through ADR-009.

## Baseline Scope

ADR-001 through ADR-005 are the current accepted baseline. Later claims may clarify, challenge, or propose changes only through `02-change-control.md`.

Primary source map:

- `../10-adr1-5-rest-state-closure-register.md`

Evidence archive:

- `../04-architecture-lineage-kernels.md`
- `../06-adr2-identity-conflict-kernels.md`
- `../07-adr3-authorization-sync-kernels.md`
- `../08-adr4-configuration-boundary-kernels.md`
- `../09-adr5-state-progression-kernels.md`

## Storage Model

The platform stores operational facts as typed immutable events in an append-only event log. The event log is the canonical source of truth.

All state changes enter through the event store. Projections and views are derived from events and are repaired by recomputing from events, not by directly patching projection state as canonical truth.

Current state is projection-derived. Projection rebuild scope is limited by the event subset available to the device or server doing the rebuild.

## Event Envelope

Every event envelope must express:

- identity
- type
- payload
- timestamp

The baseline also includes later ADR-001 through ADR-005 envelope closure:

- ADR-002 adds causal/device/typed-identity reference requirements.
- ADR-003 keeps authority context projection-derived and adds no `authority_context` field in the current baseline.
- ADR-004 settles `shape_ref`, optional `activity_ref`, structural type vocabulary, and system actor convention.
- ADR-005 adds no envelope fields and no structural event type.

Device time is recorded for display and audit. Projection logic, conflict detection, and protocol correctness do not depend on device clock time for ordering. Intra-device ordering uses `device_sequence`; cross-device concurrency detection uses `sync_watermark`; the device identity namespace is hardware/app-installation-bound rather than actor-bound.

The event envelope must not be revised by post-baseline material without explicit change control.

## Identity And References

Events, subjects, and records use client-generated UUIDs for offline creation.

Subject identity evolves through alias/projection and corrective split behavior. Historical event references are not rewritten to express identity evolution.

Lineage must remain acyclic. Merge/split operations are online-only where the baseline requires server validation. Raw references remain available for conflict and authorization checks.

## Conflict And Stale-Event Handling

The baseline uses detect-before-act discipline: conflict and authorization checks run before downstream policy or workflow effects.

Validly structured work that is stale or state-anomalous under current projections is accepted as immutable factual history and surfaced with flags rather than rejected, except where the baseline explicitly defines an online-only resolution operation. Structural validation still applies to malformed envelopes, unsupported event types, and payloads that do not match their declared shape.

Last-write-wins and invisible automatic merge are rejected for operational conflicts requiring judgment.

## Authorization And Sync

The sync unit is the immutable event. Sync is:

- idempotent
- append-only
- order-independent
- filtered by assigned access scope

Access is assignment-derived. Sync scope is access scope.

Authority is projection-derived rather than stored as immutable `authority_context`. Authorization is reconstructed on sync from actor, subject/process references, assignment timeline, event creation context, and sync knowledge state.

Authorization for an event is checked against the original subject reference written into that event, not against post-merge alias projections.

Operational role labels are configuration and product-surface vocabulary, not platform-owned actor classes. Labels such as field worker, supervisor, coordinator, reviewer, auditor, or regional lead may appear in scenarios and UX artifacts, but the baseline expresses authority through actors, assignments, roles, scopes, activities, projections, and sync scope.

Scope expansion is additive. For scope contraction, the ADR-003 initial strategy is selective retain: an actor's own events remain on device, while other actors' events about out-of-scope subjects are candidates for device-side removal.

Sensitive deployments require stronger local lifecycle handling than retain-and-hide.

## Configuration Boundary

The platform owns structural event type vocabulary and processing semantics. Deployers configure within bounded surfaces: shapes, activities, roles, schedules, thresholds, severities, sensitivity parameters, and policy choices.

The baseline fixes six structural event types and rejects `status_changed` as a seventh type.

Deployer policy configuration is closed only as policy values over platform-owned vocabularies and mechanisms. Closed examples include per-deployment flag severity overrides, domain uniqueness constraints evaluated optimistically on device and authoritatively on server, composition of platform-fixed scope types, and shape/activity-level sensitivity classification.

These policy surfaces must not add envelope fields or become deployer-authored platform logic.

Configuration is bounded by:

- configuration gradient
- expression limits
- server-only triggers where required
- atomic configuration packages
- trigger DAG depth constraints
- complexity budgets
- deployer identifier naming rules
- platform/deployer responsibility boundaries

Deployer-authored arbitrary access-control logic and field-level sensitivity are rejected.

## Projection And Workflow

Workflow state is derived from immutable event sequences plus pattern definitions. Workflow state is not stored as canonical event state.

Invalid workflow transitions are accepted and flagged rather than rejected.

Unresolved flagged events remain visible in timeline but are excluded from workflow state-machine evaluation until accepted resolution re-derives state.

The Pattern Registry is a platform-owned workflow primitive. Exact pattern inventory, pattern skeletons, and formal pattern schema format remain outside the current baseline.

## Closed Flag Interactions

Only ADR-001 through ADR-005 flag interactions are part of this baseline.

Closed baseline items:

- workflow `transition_violation` flag behavior
- source-only flagging with source-chain traversal
- platform-defined flag resolvability classification for ADR-005 workflow cases
- L3b auto-resolution for eligible workflow cases
- `system:auto_resolution/{policy_id}` actor attribution
- unresolved flagged-event exclusion from workflow state-machine derivation until resolution

This is not general flag semantics. ADR-006-R through ADR-009 remain assessment material rather than automatic baseline authority.

## Bounded Context Expressions

`context.*` is closed only for form expressions and only for the seven platform-fixed pre-resolved values named by ADR-005.

Trigger expressions do not gain `context.*`.

The baseline does not authorize:

- dynamic joins
- arbitrary projection access
- aggregates
- functions
- live updates

## Rejected Paths

The following paths are rejected under the current baseline:

- mutable canonical records plus separate audit log
- snapshot-primary source-of-truth storage
- action-log-primary source-of-truth storage
- server-allocated identifiers for offline event/subject/record creation
- last-write-wins for operational conflicts requiring judgment
- invisible automatic merge where judgment is required
- structural ordering by `device_time`
- an `authority_context` field in the current envelope without formal change control
- deployer-authored arbitrary access-control logic
- field-level sensitivity
- retain-and-hide as sufficient handling for sensitive local data after scope contraction
- `status_changed` as ADR-005 workflow structural type
- `current_state` as canonical event state
- `pattern_ref` as an event-envelope structural reference
- rejecting invalid workflow transitions instead of accepting and flagging

## Open And Deferred Items

Open or deferred items are not architecture baseline decisions unless later change control makes them so. `05-decision-gap-register.md` is the canonical register for classification, ownership, closure path, and priority.

Architecture or policy gaps still visible from the baseline:

- subject-based scope, auditor access, assessment visibility, and cross-level visibility exceptions
- shared-device actor scope
- sensitive-subject policy beyond shape/activity-level sensitivity classification
- temporary authority, grace-period policy, and offline revocation reconciliation
- domain conflict resolution automation outside closed ADR-005 workflow cases
- general flag semantics outside closed ADR-005 workflow interactions

Platform-spec detail gaps:

- exact Pattern Registry inventory, pattern skeletons, and formal pattern schema format
- source-chain traversal depth limits
- bounded `context.*` evaluation details beyond the closed seven-value form scope
- workflow-aware reporting and aggregation
- event schema/versioning obligations and projection compatibility across schema versions

Implementation, tooling, product, or operations gaps:

- retention and archival policy
- setup experience, onboarding, and role transition process
- reporting aggregation and decision-maker reporting requirements
- domain-agnostic proof or product validation gaps
- projection optimization, caching, rebuild strategy, and low-end device performance
- configuration authoring format, deployment packaging UX, deploy-time validator UX, and migration tooling
- auto-resolution authoring UX and monitoring/reporting surface
- sync pagination, priority, bandwidth handling, transport details, and operational delivery mechanics

## Post-Baseline Assessment Rule

ADR-006-R through ADR-009 must be assessed through the gap register and change-control rules. They do not supersede this baseline automatically.

Allowed classifications for later claims:

- consistent elaboration
- open-gap closure candidate
- deferred implementation/spec detail
- new unauthorized claim
- conflict with closed baseline
- valid dispute requiring formal reopen
