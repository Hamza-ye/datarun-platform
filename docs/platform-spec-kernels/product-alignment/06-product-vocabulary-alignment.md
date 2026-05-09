# Product Vocabulary Alignment

Status: Session 6 product-alignment artifact

This document aligns product vocabulary with the accepted architecture baseline before atomization resumes. It is a vocabulary control artifact, not a glossary for marketing copy and not an architecture rewrite.

The purpose is to let the product use natural, domain-agnostic language without letting product words quietly become canonical storage, service boundaries, permission rules, workflow schemas, or identity ownership claims.

## Source Basis

Primary inputs:

- `../../README.md`
- `../../constraints.md`
- `../../access-control-scenario.md`
- `../../behavioral_patterns.md`
- `../../principles.md`
- `../../scenarios/README.md`
- `01-phase-1-scenario-boundary-map.md`
- `02-product-experience-principles.md`
- `03-user-roles-and-operational-contexts.md`
- `04-core-operational-journeys.md`
- `05-information-architecture.md`

Architecture guardrails:

- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../pre-operations/04-accepted-pre-atomization-decisions.md`

## Control Rule

Product vocabulary may make the platform understandable to users. It must not define platform mechanics.

Do not infer from a product term:

- an event-envelope field
- a stored canonical record
- a projection schema
- a service boundary
- a workflow state machine
- a Pattern Registry entry
- a permission source
- a sync-scope rule
- an identity subsystem owner
- a deployer-authored platform mechanism
- a closed decision for an open gap

When a product term needs a stronger meaning than this artifact allows, route the pressure to `08-ux-gap-routing.md`, the professional-baseline gap register, or change control.

## Vocabulary Layering Rule

Product vocabulary belongs to the operational surface and product translation layer. It does not replace the platform core's language.

Use this layer split:

| Layer | Speaks In | Responsibility |
|---|---|---|
| Platform core | events, envelope, assignments, sync scope, projections, configuration, Pattern Registry, flags, lineage | Preserve accepted architecture semantics and invariants |
| Product translation layer | product projections, read models, action affordances, authority results, freshness, routing, explanations | Translate core facts and decisions into user-facing surfaces |
| Operational surface | work, records, targets, review, setup, evidence, exceptions, sync status | Help users understand and act without learning platform internals |

The operational surface may present work, records, targets, review, setup, evidence, exceptions, freshness, and sync status. The core still answers through accepted platform mechanisms: immutable events, projection-derived state, assignment-derived authority, scoped sync, bounded configuration, and accepted flag behavior.

This means:

- `Work` may be a surface and read-model concept, but not a core storage primitive.
- `Record` may be user-facing history language, but events remain canonical storage.
- `Operational Target` may help users orient, but it does not create broad identity ownership.
- `Review`, `Transfer`, and `Follow-Up` may be operational flows, but their state remains derived from accepted event and pattern behavior.
- `Evidence` may organize traceability, but it is not a second source of truth.
- `Sync Status` may explain local/central visibility, but it does not define sync protocol.

If later atomization needs a product term to become a core primitive, that is not vocabulary cleanup. It is architecture pressure and must route through gap review or change control.

## Term Classes

Use these classes consistently:

| Class | Meaning | Rule |
|---|---|---|
| Product-wide term | Safe user-facing concept across scenarios | Must map to baseline behavior or explicit gap routing |
| Narrow product term | Safe only with explicit constraints | Must include forbidden interpretations |
| Deployer label | Name a deployment may customize | Must not become a platform primitive |
| Architecture-only term | Engineering/baseline mechanism | Avoid in ordinary UX and product IA |
| Gap-routed term | Useful but touches an open decision | May be discussed, but not atomized as closed behavior |
| Rejected term | Too misleading or architecture-debt-prone | Do not use as product or platform vocabulary |

## Vocabulary Direction

The product should use plain operational language:

- Work
- Record
- Operational Target
- Subject
- Activity
- Information Shape
- Assignment
- Review
- Decision
- Transfer
- Follow-Up
- Oversight
- Exception
- Evidence
- Sync Status
- Freshness

The product should avoid making users reason in these architecture terms:

- Event
- Event envelope
- Projection
- Sync scope
- Authority context
- Pattern Registry
- Workflow state machine
- Structural event type
- Identity lineage
- Flag source-chain
- Bounded context expression

Architecture terms may appear in engineering specs, diagnostics, developer tooling, or advanced admin documentation only when needed and explicitly mapped.

## Core Product Terms

### Work

Class: Product-wide term

Baseline-safe meaning:

Work is anything that currently needs attention or may be acted on by a user: capture, review, transfer receipt, follow-up, correction, setup, inspection, or exception handling.

Allowed use:

- Work surface
- My Work
- Team Work
- review work
- returned work
- overdue work
- pending work

Forbidden interpretations:

- Work is not a canonical stored `work_item` table.
- Work is not a service boundary.
- Work visibility does not grant action authority.
- Work queues do not bypass assignment-derived access or sync scope.
- Work status is not canonical event state.

Baseline mapping:

- Projection-derived current views
- Assignment / Authority / Sync
- Projection / Workflow State
- Event Log / Storage for underlying history

Gap routing:

- permission table details
- temporary authority and offline revocation reconciliation
- reporting freshness
- trigger explanation boundaries

### Work Item

Class: Narrow product term

Baseline-safe meaning:

A work item is an item shown in a product queue or detail view because the platform can currently present something as pending, waiting, due, returned, assigned, triggered, blocked, or needing review.

Allowed use:

- lower-case descriptive use in UX copy where "item" helps readability
- item detail in product journeys
- queue item in implementation-neutral prose

Forbidden interpretations:

- Do not use `WorkItem` as a platform primitive without a later platform-spec decision.
- Do not assume explicit and derived work share one stored table.
- Do not say every state transition is stored as a work-item state.
- Do not make work-item state canonical truth.

Preferred product wording:

Use `Work` for the surface and `item` only when referring to a visible row/card/detail object.

Gap routing:

- exact Pattern Registry inventory and schema
- workflow-aware reporting
- source-chain traversal limits
- trigger and escalation behavior

### Record

Class: Narrow product term

Baseline-safe meaning:

A record is the user-facing presentation of an operational fact or action that has been captured into the platform's append-only history.

Allowed use:

- record history
- recorded work
- correction record
- review decision record
- transfer receipt record
- evidence record

Forbidden interpretations:

- Record is not a mutable database row.
- Record is not a replacement name for the event envelope.
- Record does not add envelope fields such as `record_id`, `scope_ref`, `tenant_id`, or `authority_context`.
- Record is not a separate canonical storage model beside events.
- Record does not imply device time is structural ordering.

Baseline mapping:

- Immutable typed events in the append-only event log
- Current views derived from events
- Device time for display/audit only

Preferred distinction:

- Product UX may say `record`.
- Architecture/spec language should say `event` when referring to canonical storage.

Gap routing:

- event schema and versioning tooling
- structured import/export compatibility
- retention and archival

### Event

Class: Architecture-only term

Baseline-safe meaning:

An event is the canonical stored operational fact in the accepted architecture baseline.

Allowed use:

- platform specification
- engineering documentation
- audit/evidence explanation where exactness is required
- developer or diagnostic tooling

Forbidden interpretations:

- Do not make ordinary field users navigate through "events".
- Do not rename events to records in architecture specs without explicit mapping.
- Do not make event type vocabulary deployer-authored.
- Do not add event-envelope fields through product vocabulary.

Product mapping:

Use `record`, `history`, `decision`, `receipt`, or `correction` as appropriate in user-facing surfaces.

### Operational Target

Class: Product-wide term

Baseline-safe meaning:

An operational target is the thing the work is about from the user's perspective: a subject, place, unit, resource, situation, grouped location, transfer context, or related operational context.

Allowed use:

- operational-target lookup
- target detail
- target context
- target history
- target selection

Forbidden interpretations:

- Operational Target is not a single identity subsystem.
- Operational Target does not mean every referent belongs to Identity / Lineage.
- Operational Target does not grant global search or visibility.
- Operational Target does not own campaign, shipment, case, process, or reporting lifecycle.

Baseline mapping:

- Typed references
- Subject continuity only where referent has subject-lineage semantics
- Assignment-derived access and sync scope
- Projection-derived current views

Gap routing:

- subject-based scope
- auditor access
- cross-flow link visibility
- process identity and pending-match UX

### Subject

Class: Narrow product term

Baseline-safe meaning:

A subject is a persistent referent whose continuity matters across records and time, and whose identity may require lookup, ambiguity handling, deactivation, merge, split, or duplicate resolution.

Allowed use:

- subject lookup
- subject history
- subject profile
- inactive subject
- possible duplicate subject

Forbidden interpretations:

- Subject is not every real-world noun in the product.
- Subject is not actor identity.
- Subject is not assignment scope by itself.
- Subject is not campaign, case, shipment, transfer, or reporting lifecycle ownership.
- Subject profile is not canonical storage.

Baseline mapping:

- Identity / Lineage only for subject-continuity semantics
- Historical references are not rewritten
- Alias/projection and corrective split behavior
- Online-only merge/split where required by baseline

Gap routing:

- alias-cycle enforcement and resolution semantics
- duplicate-resolution UX
- subject-based scope and auditor access

### Activity

Class: Product-wide term

Baseline-safe meaning:

An activity is a deployer-configured operational kind of work that gives users a meaningful context for capture, review, assignment, rhythm, transfer, follow-up, oversight, or setup.

Allowed use:

- activity setup
- activity label
- activity progress
- activity-specific work
- activity history

Forbidden interpretations:

- Activity is not a structural event type.
- Activity is not a deployer-authored platform mechanism.
- Activity does not authorize arbitrary workflow, trigger, or access logic.
- Activity does not add required envelope fields beyond accepted `activity_ref` behavior.
- Activity does not make scenario-specific apps.

Baseline mapping:

- Optional `activity_ref`
- Configuration boundary
- Shapes, schedules, thresholds, severities, roles, and policy values within bounded platform mechanisms
- Pattern Registry where workflow behavior is platform-owned

Gap routing:

- configuration authoring and deployment UX
- Pattern Registry inventory and schema
- setup/onboarding
- permission table details

### Information Shape

Class: Product-wide term

Baseline-safe meaning:

An information shape describes what structured information is expected for a configured activity or capture context.

Allowed use:

- information shape
- shape version
- old shape / new shape
- shape change
- shape-based validation

Forbidden interpretations:

- Shape is not a deployer-authored structural event type.
- Shape is not arbitrary code.
- Shape changes do not invalidate older valid records.
- Shape does not define field-level sensitivity under the current baseline.

Baseline mapping:

- `shape_ref`
- Configuration boundary
- Event schema/versioning tooling gap
- Shape/activity-level sensitivity classification only

Preferred wording:

Use `Information Shape` in product-alignment artifacts. `Template` may be a deployer-facing label or UI synonym only if it maps cleanly to shape behavior.

Gap routing:

- event schema/versioning tooling
- projection compatibility across schema versions
- configuration authoring UX
- sensitive-subject policy beyond shape/activity sensitivity

### Template

Class: Deployer label / narrow product synonym

Baseline-safe meaning:

Template may be used as a familiar UI label for an information shape if a deployment audience expects that word.

Allowed use:

- deployer-facing setup label
- localized/customized label for information shape

Forbidden interpretations:

- Template is not the baseline term for event-envelope behavior.
- Template must not drift away from `shape_ref` mapping.
- Template does not imply arbitrary form scripting.
- Template does not close schema/versioning tooling details.

Preferred product rule:

Use `Information Shape` as the alignment term; allow `Template` only as a mapped label.

### Assignment

Class: Product-wide term

Baseline-safe meaning:

An assignment is a time-bounded responsibility/authority binding that helps determine what work a user sees, can act on, and syncs to their device under the accepted baseline.

Allowed use:

- assigned work
- assignment history
- assignment change
- reassignment
- temporary assignment
- assignment coverage

Forbidden interpretations:

- Assignment is not the same as product role.
- Assignment is not the same as group membership.
- Assignment does not store immutable `authority_context`.
- Assignment does not bypass original-reference authorization.
- Assignment does not make all hierarchy visibility closed.

Baseline mapping:

- Assignment-derived access
- Sync scope as access scope
- Projection-derived authority
- Time and activity/context constraints

Gap routing:

- temporary authority and offline revocation reconciliation
- permission table details
- shared-device actor scope
- cross-level distribution visibility

### Role

Class: Narrow product term

Baseline-safe meaning:

Role can describe deployer-configured responsibility labels or product operating contexts, but it does not grant authority by itself.

Allowed use:

- deployer role label
- product role lens
- reviewer role label
- coordinator role label

Forbidden interpretations:

- Product role is not an architecture actor type.
- Role is not a direct permission source without assignment/context.
- Role is not a user group.
- Role is not an IdP claim.
- Role is not an event-envelope field.

Baseline mapping:

- Roles can participate in configured policy values under bounded configuration
- Effective authority remains assignment-derived through roles, scopes, activity/context, time, and platform-fixed containment semantics

Gap routing:

- permission table details
- onboarding and role transition
- group-managed administration if later needed

### Actor

Class: Architecture/product-explanation term

Baseline-safe meaning:

An actor is the person or system identity attributable for an action in the platform history.

Allowed use:

- engineering specs
- audit/evidence explanation
- actor attribution
- system actor where baseline uses that convention

Forbidden interpretations:

- Actor is not a product persona.
- Actor is not a device.
- Actor is not a user group.
- Actor identity does not define what the actor may see or do without assignment/context.

Product mapping:

Ordinary UX should usually say `person`, `user`, `worker`, `reviewer`, or a deployer role label. Evidence/audit surfaces may say actor when attribution precision matters.

Gap routing:

- shared-device actor scope
- external identity-provider integration
- account schema

### Team / Group

Class: Deployer/admin label with restricted meaning

Baseline-safe meaning:

A team or group is an organizational convenience for display, administration, coordination, or assignment management where later design allows it.

Allowed use:

- team view
- team progress
- group of locations
- administrative grouping
- grouped-location context

Forbidden interpretations:

- A user group does not directly grant sync scope or action authority.
- Group membership is not assignment.
- Group is not an IdP authority source.
- Grouped-location work is not a tenant/deployment envelope field.

Baseline mapping:

- Assignment / Authority / Sync
- Configuration boundary where grouping is configured
- Reporting/aggregation where grouped views are derived

Gap routing:

- permission table details
- group-managed administration if product need appears
- cross-level distribution visibility

### Scope

Class: Architecture/configuration term with restricted product use

Baseline-safe meaning:

Scope is the boundary used by the accepted baseline for access and sync. Product UX should usually explain scope as "your assigned area", "your team", "this activity", "this period", or "this responsibility" rather than requiring users to reason about scope as a technical object.

Allowed use:

- engineering specs
- setup/admin surfaces where exactness is required
- audit/evidence explanation when visibility needs to be explained
- product-alignment gap routing

Forbidden interpretations:

- Scope is not a product role.
- Scope is not tenant/deployment identity.
- Scope is not a stored `scope_ref` envelope addition.
- Scope does not close subject-based scope or auditor access.
- Scope is not global visibility.

Baseline mapping:

- Sync scope equals access scope
- Assignment-derived access
- Platform-fixed containment semantics

Product mapping:

Prefer contextual labels such as area, activity, responsibility, team, review context, audit window, or assigned work where they are accurate.

Gap routing:

- subject-based scope and auditor access
- temporary authority/offline revocation
- cross-level visibility
- permission table details

### Review

Class: Product-wide term

Baseline-safe meaning:

Review is a user-facing judgment process where submitted, waiting, disputed, returned, or approval-step work is inspected and decided on where allowed.

Allowed use:

- review queue
- review decision
- returned from review
- review history
- review bottleneck

Forbidden interpretations:

- Review is not a general workflow authoring tool.
- Review status is not canonical stored state.
- Review does not close general flag semantics.
- Review visibility does not create action authority.

Baseline mapping:

- Projection-derived workflow state
- Pattern Registry where applicable
- Immutable decision history as events
- Assignment-derived authority

Gap routing:

- exact Pattern Registry inventory/schema
- source-chain traversal limits
- temporary reviewer authority
- general flag semantics beyond closed workflow behavior

### Decision

Class: Product-wide term

Baseline-safe meaning:

A decision is a user-facing action of judgment: approve, return, reject, question, escalate, accept, resolve, defer, or similar configured choices.

Allowed use:

- decision trail
- decision history
- review decision
- resolution decision
- decision reason

Forbidden interpretations:

- Decision is not a mutable status update.
- Decision labels are not structural event types.
- Decision does not imply all choices are available offline.
- Decision does not close resolver authority.

Baseline mapping:

- Immutable events
- Projection-derived current interpretation
- Assignment-derived authority
- Closed ADR-005 workflow flag interactions where applicable

Gap routing:

- permission table details
- temporary authority/offline revocation
- general conflict resolution automation

### Transfer

Class: Product-wide term

Baseline-safe meaning:

Transfer is the user-facing movement of responsibility, custody, resources, materials, or handoff context between parties, with acknowledgement or discrepancy handling where configured.

Allowed use:

- transfer
- receipt
- acknowledgement
- discrepancy
- in transit
- chain of responsibility

Forbidden interpretations:

- Transfer lifecycle is not subject-lineage ownership.
- Transfer does not rewrite custody history.
- Transfer is not a separate supply subsystem by default.
- Transfer discrepancy does not close general domain conflict automation.

Baseline mapping:

- Projection / Workflow State
- Event Log / Storage
- Assignment / Authority / Sync
- Flag / Resolution where applicable

Gap routing:

- cross-level distribution visibility
- domain conflict automation outside workflow
- reporting and aggregation over transfers
- Pattern Registry inventory

### Follow-Up

Class: Product-wide term

Baseline-safe meaning:

Follow-up is work that remains active across time because a situation, condition, review decision, trigger, or related activity requires later attention.

Allowed use:

- follow-up work
- active follow-up
- resolved follow-up
- follow-up history
- follow-up queue

Forbidden interpretations:

- Follow-up is not a broad case engine.
- Follow-up does not own all long-running state.
- Follow-up status is not canonical stored state.
- Follow-up triggers do not get unbounded projection access.

Baseline mapping:

- Projection-derived workflow/current state
- Trigger / Reactivity within accepted constraints
- Assignment / Authority / Sync
- Cross-reference/link behavior where access allows

Gap routing:

- bounded trigger semantics
- long-running workflow patterns
- cross-flow link visibility
- active/resolved reporting

### Oversight

Class: Product-wide term

Baseline-safe meaning:

Oversight is a freshness-aware product view over progress, missing work, bottlenecks, stale information, exceptions, and operational trends within access constraints.

Allowed use:

- oversight surface
- team oversight
- coordinator oversight
- progress view
- bottleneck view

Forbidden interpretations:

- Oversight is not canonical reporting truth.
- Oversight does not bypass access/sync scope.
- Oversight does not grant action authority.
- Oversight does not imply live field completeness.

Baseline mapping:

- Reporting / Aggregation gap
- Projection-derived current views
- Assignment / Authority / Sync
- Freshness and sync status

Gap routing:

- reporting and aggregation
- workflow-aware reporting
- freshness metadata
- access-constrained aggregate views
- cross-level visibility

### Exception

Class: Narrow product term

Baseline-safe meaning:

An exception is a user-facing condition that needs attention because something is returned, stale, disputed, ambiguous, invalid under accepted workflow behavior, blocked, failed to sync, or otherwise unresolved.

Allowed use:

- exception list
- exception detail
- exception history
- exception routing
- exception summary

Forbidden interpretations:

- Exception is not one architecture subsystem.
- Exception does not close general flag/conflict semantics.
- Exception does not mean malformed events are accepted.
- Exception does not combine review, transfer discrepancy, sync failure, duplicate subject, and workflow violation into one hidden mechanism.

Baseline mapping:

- Closed ADR-005 flag interactions where applicable
- Open general flag/conflict semantics
- Detect-before-act and accept-and-flag discipline
- Sync/local status where applicable

Gap routing:

- general flag semantics
- domain conflict automation outside workflow
- malformed-event handling boundaries
- stale-authority surfacing

### Flag

Class: Architecture/product-explanation term with restricted UX use

Baseline-safe meaning:

A flag is a baseline-controlled marker for accepted flag behavior or a product-explanation term where an item needs attention under explicitly defined behavior.

Allowed use:

- engineering specs for accepted flag behavior
- audit/evidence explanation
- advanced exception detail where exactness is useful

Forbidden interpretations:

- Flag is not a general product bucket for every issue unless semantics are defined.
- Flag does not close general domain conflict automation.
- Flag does not make malformed events valid.
- Flag does not imply all flagged records are inert unless covered by accepted behavior.

Product mapping:

Use `Exception`, `Needs Attention`, `Discrepancy`, `Possible Duplicate`, `Returned`, `Sync Problem`, or `Blocked` in ordinary UX when more precise and safer.

Gap routing:

- general flag semantics
- domain conflict automation outside workflow
- source-chain traversal limits

### Conflict

Class: Gap-routed term

Baseline-safe meaning:

Conflict may describe operational disagreement or incompatible facts that require judgment, but product usage must be precise about what kind of issue it is.

Allowed use:

- conflict requiring judgment
- sync conflict where defined
- transfer dispute if "dispute" is not clearer
- duplicate/identity conflict where defined

Forbidden interpretations:

- Conflict is not last-write-wins.
- Conflict is not invisible automatic merge.
- Conflict is not a catch-all for validation failure.
- Conflict does not close general conflict automation.

Preferred product wording:

Use narrower terms where possible: discrepancy, possible duplicate, stale assignment, invalid transition, returned work, blocked sync, or needs review.

Gap routing:

- domain conflict automation outside workflow
- general flag semantics
- alias-cycle behavior
- duplicate-resolution UX

### Evidence

Class: Product-wide term

Baseline-safe meaning:

Evidence is a user-facing way to inspect recorded history, decisions, corrections, handoffs, freshness, and traceability where access allows.

Allowed use:

- evidence surface
- evidence history
- audit evidence
- supporting evidence
- export evidence where policy allows

Forbidden interpretations:

- Evidence is not a second source of truth.
- Evidence export is not canonical operational state.
- Evidence navigation does not create auditor access.
- Evidence does not define retention, archival, or field-level sensitivity.

Baseline mapping:

- Event Log / Storage
- Projection-derived current interpretation
- Assignment / Authority / Sync
- Structured import/export gap

Gap routing:

- auditor access
- structured import/export compatibility
- retention and archival
- sensitive local data lifecycle

### Sync Status

Class: Product-wide term

Baseline-safe meaning:

Sync status tells users whether local work and views are saved, pending, synced, stale, failed, blocked, or need reconciliation.

Allowed use:

- offline
- saved locally
- pending sync
- synced
- last synced
- stale
- sync problem
- retry needed

Forbidden interpretations:

- Sync status does not expose protocol mechanics.
- Sync status does not promise real-time central visibility.
- Sync status does not define sync delivery mechanics.
- Sync status does not define local data deletion, retention, or sensitive lifecycle.

Baseline mapping:

- Immutable event sync
- Sync scope as access scope
- Local last-known enforcement
- Sync delivery mechanics gap

Gap routing:

- sync delivery mechanics
- local data lifecycle
- temporary authority/offline revocation
- shared-device actor scope

### Freshness

Class: Product-wide term

Baseline-safe meaning:

Freshness tells users how current a view, item, queue, dashboard, assignment, setup change, or evidence set is relative to known sync and projection state.

Allowed use:

- freshness cue
- last synced
- may be stale
- updated from known data
- field state may be delayed

Forbidden interpretations:

- Freshness is not live truth.
- Freshness does not make dashboards canonical.
- Freshness does not solve sync uncertainty.
- Freshness does not use device time for structural ordering.

Baseline mapping:

- Projection-derived views
- Device time as display/audit only
- Sync status
- Reporting/aggregation gap

Gap routing:

- reporting freshness metadata
- projection performance/caching
- sync delivery mechanics

### Setup

Class: Product-wide term

Baseline-safe meaning:

Setup is the product experience for configuring bounded platform mechanisms: activities, information shapes, schedules, thresholds, severities, assignments, review needs, transfer/follow-up patterns, and policy values.

Allowed use:

- setup surface
- activity setup
- assignment setup
- setup validation
- publish setup change
- rollout impact

Forbidden interpretations:

- Setup is not programming.
- Setup does not allow arbitrary access-control logic.
- Setup does not author structural event types.
- Setup does not author workflow state machines.
- Setup does not add envelope fields.

Baseline mapping:

- Configuration boundary
- Atomic configuration packages
- Pattern Registry as platform-owned primitive
- Deployment packaging and validator UX gaps

Gap routing:

- configuration authoring/deployment UX
- setup/onboarding
- Pattern Registry inventory/schema
- permission table details

### Link

Class: Narrow product term

Baseline-safe meaning:

A link is a visible relationship between otherwise independent work, records, targets, transfers, reviews, or evidence, where access allows.

Allowed use:

- related work
- linked context
- source record
- supporting evidence
- connected activity

Forbidden interpretations:

- Link does not couple separate workflows into one lifecycle.
- Link does not bypass access/sync scope.
- Link does not make hidden target details visible.
- Link is not broad identity ownership.

Preferred product wording:

Use `Related` or `Connected` in ordinary UX unless exact link semantics matter.

Gap routing:

- cross-flow link visibility and access behavior
- source-chain traversal limits
- subject-based scope and auditor access

### Device

Class: Architecture/product-explanation term

Baseline-safe meaning:

A device is the hardware/app-installation-bound offline execution environment that can hold local scoped data and sequence local events.

Allowed use:

- device setup
- device sync
- device storage warning
- audit/diagnostic context

Forbidden interpretations:

- Device is not actor identity.
- Device does not grant authority.
- Device time is not structural ordering.
- Device state does not define central truth.

Product mapping:

Ordinary UX should usually say `this phone`, `this device`, or `offline data` only where the distinction matters.

Gap routing:

- shared-device actor scope
- local data lifecycle
- sync delivery mechanics

### Sync Session

Class: Architecture/tooling term

Baseline-safe meaning:

A sync session is an implementation/tooling concept for a sync exchange. Users usually need the result, not the session object.

Allowed use:

- engineering specs
- diagnostics
- admin observability
- support tooling

Forbidden interpretations:

- Sync session is not a user-facing workflow by default.
- Sync session does not define sync delivery mechanics.
- Sync session does not change access scope.

Product mapping:

Use `Sync Status`, `Last Synced`, `Pending Sync`, `Sync Problem`, or `Retry` for normal UX.

Gap routing:

- sync pagination, priority, bandwidth, transport, and operational delivery mechanics
- operational health/observability

### Tenant / Deployment

Class: Architecture/operations term with product restrictions

Baseline-safe meaning:

Deployment and tenant may describe operational packaging, hosting, isolation, or administration outside the event envelope.

Allowed use:

- operations planning
- deployment packaging
- self-host/cloud documentation
- admin/support context where needed

Forbidden interpretations:

- Tenant is not an event-envelope field under current guardrails.
- Deployment is not an event-envelope field under current guardrails.
- Tenant/deployment does not grant visibility or action authority.
- Tenant/deployment does not replace assignment-derived access.

Baseline mapping:

- Pre-operations deployment/tenancy guardrail
- Authorization / Sync remains assignment-derived

Gap routing:

- deployment packaging UX
- self-host operations if needed
- account/schema/IdP integration if product need appears

## Product Surface Vocabulary

Use these as current product-alignment surface names:

| Surface Term | Class | Baseline-Safe Meaning | Forbidden Interpretation |
|---|---|---|---|
| Work | Product-wide | Attention/action surface for visible work | Canonical work-item storage |
| Operational Targets | Product-wide | Things work is about | One broad identity owner |
| Review And Decisions | Product-wide | Judgment surfaces | Workflow authoring or canonical state |
| Oversight | Product-wide | Freshness-aware progress/gap views | Canonical reporting truth |
| Exceptions | Narrow product | Routed issues needing attention | Closed general conflict subsystem |
| Setup | Product-wide | Bounded configuration experience | Programming or deployer-authored mechanisms |
| Evidence | Product-wide | Traceable history/current interpretation | Second source of truth or audit back door |
| Sync And Local Status | Product-wide | Local/sync/freshness status | Sync protocol or real-time guarantee |

These are surface names, not platform modules.

## Terms To Avoid In Ordinary UX

Avoid these terms for ordinary field, supervisor, and coordinator workflows unless a specialized surface requires exactness:

| Avoid | Prefer | Reason |
|---|---|---|
| Event | Record, history, action, decision | Event is canonical architecture storage |
| Projection | Current view, current status, summary | Projection is implementation/baseline language |
| Sync scope | Assigned area/context, visible work, access | Scope has exact authorization/sync meaning |
| Authority context | What you can do here, allowed action, read-only | Stored `authority_context` is rejected |
| Pattern Registry | Setup pattern, review pattern, configured behavior | Exact inventory/schema remains open |
| Workflow state machine | Review flow, transfer flow, follow-up state | Avoid exposing machinery |
| Identity lineage | Subject history, possible duplicate, merged/split history | Lineage has narrow baseline ownership |
| Flag source-chain | Related issue history, source issue | Source-chain details are platform-spec detail |
| Tenant | Deployment, organization, workspace if needed | Tenant must not become envelope/access shortcut |
| User group grants | Assignment, responsibility, team view | Groups do not directly grant authority |

## Candidate Terms Rejected Or Deferred

| Term | Disposition | Reason |
|---|---|---|
| `WorkItem` as platform primitive | Deferred/rejected for product alignment | Risks canonical storage and workflow schema assumptions |
| `Record envelope` | Rejected | Confuses product record with event envelope |
| `Scope` as ordinary UX noun | Restricted | Too close to authorization/sync mechanics |
| `Template` as baseline term | Restricted | Must map to information shape / `shape_ref` |
| `Flag` as general UX bucket | Restricted | General flag semantics remain open |
| `Conflict` as broad subsystem | Rejected | Conflict behavior is split across accepted behavior and open gaps |
| `Activity defines who may do what` | Restricted | Must remain bounded policy values, not arbitrary access logic |
| `Tenant boundary` for product access | Rejected | Deployment/tenant does not grant visibility/action authority |
| `Current state` as stored product object | Rejected | Current state is projection-derived |
| `Dashboard truth` | Rejected | Oversight/reporting must be freshness-aware and derived |

## Vocabulary-To-Gap Matrix

| Vocabulary Area | Existing Gap Pressure |
|---|---|
| Work / Work Item | Pattern Registry inventory/schema; workflow-aware reporting; permission details |
| Record / Event | event schema/versioning tooling; import/export; retention/archive |
| Subject / Operational Target | alias-cycle behavior; subject-based scope; auditor access; duplicate resolution |
| Activity / Setup | configuration authoring/deployment UX; Pattern Registry inventory/schema; setup/onboarding |
| Assignment / Role / Scope | temporary authority/offline revocation; permission details; shared devices; cross-level visibility |
| Review / Decision | source-chain traversal limits; temporary reviewer authority; general flag semantics |
| Transfer / Discrepancy | cross-level distribution visibility; domain conflict automation; transfer reporting |
| Follow-Up / Triggered Work | bounded trigger semantics; long-running workflow patterns; active/resolved reporting |
| Oversight / Freshness | reporting/aggregation; freshness metadata; projection performance/caching |
| Exception / Flag / Conflict | general flag semantics; domain conflict automation; malformed-event boundaries |
| Evidence / Audit | auditor access; structured import/export; retention/archive; sensitive local lifecycle |
| Sync Status / Device | sync delivery mechanics; local data lifecycle; shared-device actor scope |
| Tenant / Deployment | deployment packaging; self-host operations; account/IdP integration |

## Vocabulary Rules For Later Artifacts

Apply these rules in `07` through `11` and later platform-spec atomization:

- Every product term that appears in an interaction state must map to this artifact or be added through explicit review.
- Do not introduce CamelCase product primitives unless they are already baseline terms or deliberately accepted product terms.
- Use product-wide terms for user surfaces and architecture-only terms for engineering surfaces.
- If a UI label needs deployer customization, classify it as a deployer label and map it to the product-wide concept.
- If a term touches authority, sync, identity, reporting, conflict, or setup programming, include a gap route.
- If a term sounds natural but changes the baseline, reject it until change control accepts it.

## Session 6 Output

Later product artifacts should use this vocabulary as follows:

- `07-interaction-state-model.md` should define user-visible states using accepted product terms and avoid turning state labels into canonical stored state.
- `08-ux-gap-routing.md` should use the vocabulary-to-gap matrix to route unresolved terms and pressure points.
- `09-first-vertical-slice.md` should choose wording from accepted product-wide and narrow product terms only.
- `10-atomization-readiness-from-product.md` should verify that atomization candidates do not depend on rejected, restricted, or gap-routed terms as if they were closed.
- `11-alignment-closeout.md` should confirm this artifact is the stable vocabulary input surface.
