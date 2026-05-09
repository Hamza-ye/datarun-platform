# Information Architecture

Status: Session 5 product-alignment artifact

This document defines the product's information architecture direction before selected-slice atomization. It describes how users should find work, records, review, oversight, setup, evidence, exceptions, and sync state as one coherent product.

It is not a final sitemap, wireframe set, route table, permission model, data model, or implementation plan.

## Source Basis

Primary inputs:

- `../../README.md`
- `../../constraints.md`
- `../../access-control-scenario.md`
- `../../scenarios/README.md`
- `01-phase-1-scenario-boundary-map.md`
- `02-product-experience-principles.md`
- `03-user-roles-and-operational-contexts.md`
- `04-core-operational-journeys.md`

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

Information architecture organizes product surfaces. It does not define architecture ownership, canonical storage, projection schemas, permission rules, or sync behavior.

Do not infer:

- a navigation area is a bounded context
- a product surface is a service boundary
- a list or queue is a stored work-item table
- a profile page owns subject-lineage lifecycle
- a review surface owns workflow state
- an oversight surface owns reporting truth
- an exception surface closes general flag/conflict semantics
- a setup surface allows deployer-authored platform logic
- a visible surface grants authority
- a hidden surface revokes authority

Surface visibility, item visibility, and action availability remain consequences of accepted authority/sync behavior. This artifact only says how the product should organize what a user can appropriately see and do.

## IA Direction

The product should use one role-scoped operational shell with reusable surface families.

The product should not become:

- a separate capture app
- a separate review app
- a separate campaign app
- a separate transfer app
- a separate registry app
- a separate reporting app
- a separate audit app
- a separate configuration tool disconnected from operations

Different roles may see different default surfaces, counts, labels, and available actions, but they should still feel they are using the same operational system.

## IA Layers

### 1. Operational Home

Purpose:

- orient the user
- show the most important work for the user's current operating context
- surface sync/freshness warnings that affect action
- provide entry into the main surface families

Expected behavior:

- Field users land on actionable work and quick capture.
- Supervisors land on team work, review queues, and exceptions.
- Coordinators land on operational progress, setup changes, and broad exceptions.
- Auditors land on scoped evidence, inspection queues, or review contexts where access allows.

Guardrails:

- Home is not a global dashboard.
- Home is not canonical truth.
- Home must not imply live field completeness when sync is stale.
- Home must not expose inaccessible work merely to explain that it exists.

### 2. Surface Families

Surface families are stable product areas. A concrete implementation may name or arrange them differently by device and role, but the same conceptual families should remain recognizable.

| Surface Family | User Question | Primary Journeys |
|---|---|---|
| Work | What needs my attention, and what can I do now? | J1, J2, J4, J7, J8, J10 |
| Operational Targets | What subject, place, unit, resource, or situation is this about? | J2, J3, J8, J9 |
| Review And Decisions | What needs judgment, return, approval, escalation, or verification? | J1, J4, J8, J9 |
| Oversight | What is progressing, missing, late, stale, or blocked? | J1, J6, J7, J8, J10 |
| Exceptions | What needs attention because something is disputed, stale, invalid, ambiguous, or unresolved? | J1, J4, J7, J8, J10 |
| Setup | What operational patterns, activities, shapes, assignments, and policy values are configured? | J5, J6 |
| Evidence | What happened, why, under whose responsibility, and with what history? | J3, J4, J6, J9 |
| Sync And Local Status | What is saved locally, pending, synced, stale, or unable to sync? | J1, J2, J6, J10 |

These are product organization concepts. They are not platform modules.

### 3. Utility Layer

Some concerns should appear across surfaces instead of becoming standalone product areas for every user:

- search within scoped data
- freshness indicators
- offline/pending sync indicators
- local drafts and failed sync notices
- read-only or blocked-action explanations
- history/timeline access
- item-level exception indicators
- deployer-specific labels for configured activities and roles

The utility layer should reduce repeated explanation. It should not hide important state changes.

## Surface Family Details

### Work

Purpose:

The work surface helps users find what needs attention and start the right journey.

Includes:

- personal assigned work
- due and overdue recurring work
- planned visits
- returned work
- triggered follow-up
- transfer receipt tasks
- review work where review is part of the user's context
- pending local work where action is needed

Role emphasis:

| Role | Work Surface Emphasis |
|---|---|
| Field-level worker | personal work, quick capture, assigned visits, transfers, follow-up, local pending status |
| Supervisor / team lead | team work, review queues, returns, escalations, unresolved exceptions |
| Coordinator / administrator | operational periods, campaign work, assignment coverage, setup-driven work |
| Auditor / external reviewer | scoped inspection or evidence work where policy allows |

IA rules:

- Work should be grouped by operational meaning, not by architecture mechanism.
- Work should explain why it is present.
- Work should show whether it is actionable, read-only, blocked, stale, pending sync, or waiting.
- Work should route into capture, review, transfer, follow-up, evidence, or exception detail without forcing users to choose a subsystem.

Guardrails:

- Do not imply that work items are canonical stored records.
- Do not make the work surface a source of authority.
- Do not create scenario-specific work areas when a shared queue pattern is enough.
- Do not expose raw trigger, projection, sync, or workflow internals.

### Operational Targets

Purpose:

The operational-target surface helps users find and understand the subject, place, unit, resource, or situation that work is about.

Includes:

- scoped lookup and search
- target profile or summary
- related records and work history
- inactive/deactivated visibility where relevant
- duplicate or ambiguity warnings where relevant
- links to capture, follow-up, review, transfer, or evidence

Role emphasis:

| Role | Operational-Target Emphasis |
|---|---|
| Field-level worker | fast lookup, safe selection, create-if-allowed, scoped history |
| Supervisor / team lead | review context, team history, related work, discrepancy context |
| Coordinator / administrator | registry stewardship where policy allows, setup impact, grouped-location context |
| Auditor / external reviewer | scoped evidence and traceability |

IA rules:

- Target surfaces should make ordinary lookup simple.
- Ambiguity, duplicate, inactive, merged, or split conditions should appear only where they affect the user's decision.
- Related work should be visible as context without forcing all related activities into one lifecycle.
- The surface should support deployer labels without hard-coding domain-specific nouns.

Guardrails:

- Do not make this surface imply broad Identity / Lineage ownership.
- Do not make global target search bypass access/sync scope.
- Do not close subject-based scope, auditor access, alias-cycle behavior, or duplicate-resolution policy.
- Do not make target profiles canonical storage.

### Review And Decisions

Purpose:

The review surface helps users judge work, return it, approve it, reject it, question it, escalate it, or verify it where allowed.

Includes:

- review queues
- returned work
- approval-step work
- assessment review
- decision history
- review-related exceptions
- handoff or discrepancy review where relevant

Role emphasis:

| Role | Review Emphasis |
|---|---|
| Field-level worker | returned work and required correction |
| Supervisor / team lead | review queues, aging, decision trail, escalation |
| Coordinator / administrator | bottlenecks, policy-controlled escalation, operational review setup |
| Auditor / external reviewer | inspection of decisions and supporting evidence |

IA rules:

- Review should be reachable from Work, Oversight, Evidence, and relevant target/work detail.
- Returned work should clearly explain what changed and what action is required.
- Multi-step approval should show current position and next responsible context without exposing full internal workflow machinery to every user.
- Decision history should be accessible where review affects accountability.

Guardrails:

- Do not make review surfaces workflow authoring tools.
- Do not make review status canonical stored state.
- Do not close general flag semantics through review labels.
- Do not let supervisor visibility become action authority.

### Oversight

Purpose:

The oversight surface helps supervisors and coordinators understand progress, gaps, bottlenecks, stale information, and exceptions across a scoped operational area.

Includes:

- progress by activity, area, period, or campaign
- missing and overdue work
- review backlog
- transfer chain status
- follow-up backlog
- freshness and completeness cues
- exception summaries
- drill-down into work, target, review, transfer, or evidence detail

Role emphasis:

| Role | Oversight Emphasis |
|---|---|
| Field-level worker | minimal personal progress or local completion where useful |
| Supervisor / team lead | team progress, review backlog, field freshness, exceptions |
| Coordinator / administrator | broad progress, grouped-location work, setup impact, operational bottlenecks |
| Auditor / external reviewer | scoped process evidence and exception trends where access allows |

IA rules:

- Oversight should make missing work visible as a meaningful state.
- Oversight should distinguish not done, done locally but not centrally visible, stale, blocked, and waiting where the platform knows enough to do so.
- Oversight should preserve drill-down to current interpretation and history.
- Oversight should support deployer labels without changing the platform's base structure.

Guardrails:

- Do not make oversight or reporting state canonical truth.
- Do not bypass access/sync scope for aggregate convenience.
- Do not hide freshness when it affects decisions.
- Do not close reporting aggregation, workflow-aware reporting, or cross-level visibility gaps.

### Exceptions

Purpose:

The exception surface helps users see and route issues that need attention without making exceptions dominate ordinary work.

Includes:

- returned work
- transfer discrepancies
- stale-authority or stale-configuration warnings where relevant
- duplicate or ambiguous targets
- invalid workflow transitions covered by accepted baseline behavior
- unresolved review issues
- sync problems
- flagged items where behavior is already accepted or explicitly routed as a gap

Role emphasis:

| Role | Exception Emphasis |
|---|---|
| Field-level worker | issues blocking or returning their own work, sync problems, required corrections |
| Supervisor / team lead | team exceptions, review issues, discrepancies, stale decisions |
| Coordinator / administrator | broad exception patterns, setup impact, unresolved operational bottlenecks |
| Auditor / external reviewer | scoped exception history and resolution evidence |

IA rules:

- Exceptions should appear inline at the point of work and in a consolidated view for responsible roles.
- Exceptions should show severity and required action only where that behavior is defined.
- Exceptions should route to the relevant work, target, review, transfer, setup, sync, or evidence surface.
- Ordinary work should remain possible when exceptions are not relevant to the action.

Guardrails:

- Do not close general flag/conflict semantics.
- Do not treat malformed events as normal conflict items.
- Do not create a broad conflict subsystem from product grouping alone.
- Do not hide unresolved flagged history where decisions depend on it.

### Setup

Purpose:

The setup surface helps coordinators and administrators configure operational work through bounded platform mechanisms.

Includes:

- activity setup
- information-shape setup
- rhythms, schedules, windows, thresholds, severities, and policy values
- assignment and responsibility setup
- review/approval pattern selection where available
- transfer or follow-up pattern setup where available
- publish, validation, rollout, and impact review

Role emphasis:

| Role | Setup Emphasis |
|---|---|
| Field-level worker | sees resulting work and labels, not setup machinery |
| Supervisor / team lead | may inspect or request setup changes where policy allows |
| Coordinator / administrator | primary setup, validation, publication, and rollout |
| Auditor / external reviewer | may inspect setup history where access allows |

IA rules:

- Setup should feel like assembling known operational patterns.
- Setup should separate configurable choices from platform evolution.
- Setup changes should show rollout and offline/freshness implications.
- Setup should keep deployer labels connected to domain-agnostic platform concepts.

Guardrails:

- Do not turn setup into programming.
- Do not allow deployer-authored access-control logic, structural event types, or workflow state machines.
- Do not add event-envelope fields for setup convenience.
- Do not make groups, IdP claims, tenant, or deployment fields direct authority sources.
- Do not close Pattern Registry inventory or schema gaps by naming setup sections.

### Evidence

Purpose:

The evidence surface helps users with appropriate scoped access inspect what happened, why it happened, and what history supports current interpretation.

Includes:

- record history
- decision history
- correction history
- transfer and receipt history
- review history
- exception and resolution history where defined
- freshness and sync context where relevant
- export or reference actions where policy allows

Role emphasis:

| Role | Evidence Emphasis |
|---|---|
| Field-level worker | history needed to correct or understand their work |
| Supervisor / team lead | decision trail, review support, discrepancy support |
| Coordinator / administrator | operational traceability and setup impact |
| Auditor / external reviewer | scoped inspection, process evidence, and reference/export |

IA rules:

- Evidence should be reachable from work, targets, review, oversight, exceptions, setup, and audit contexts.
- Evidence should show current interpretation and history together where decisions depend on both.
- Export or reference behavior should be visibly policy-bound.
- Evidence should explain why it is visible where that matters for audit trust.

Guardrails:

- Do not make exports canonical truth.
- Do not create broad auditor access from evidence navigation.
- Do not bypass assignment-derived access or sync scope.
- Do not define field-level sensitivity or retention/archival behavior without gap closure.

### Sync And Local Status

Purpose:

Sync and local status help users understand what is saved locally, pending, synced, stale, failed, blocked, or waiting for reconciliation.

Includes:

- offline indicator
- last synced or freshness cues
- pending upload count
- local drafts
- sync result summary
- failed or blocked sync items
- stale data warnings where decisions depend on freshness
- local data lifecycle messaging where defined

Role emphasis:

| Role | Sync / Local Status Emphasis |
|---|---|
| Field-level worker | confidence that local work is saved and what still needs sync |
| Supervisor / team lead | freshness of team state and delayed field visibility |
| Coordinator / administrator | rollout/freshness impact of setup or assignment changes |
| Auditor / external reviewer | freshness and evidence completeness where relevant |

IA rules:

- Sync status should be visible across surfaces, not hidden in settings.
- The product should distinguish local completion from central visibility.
- Sync problems should route to actionable detail where available.
- Freshness should be shown when it changes the meaning of progress, review, oversight, or audit.

Guardrails:

- Do not promise real-time visibility.
- Do not expose protocol mechanics as required user knowledge.
- Do not define sync delivery mechanics beyond accepted scope rules.
- Do not define local deletion, retention, or sensitive lifecycle behavior beyond the baseline and open gaps.

## Role Entry Models

The same surface families should be arranged differently by operating context.

### Field-Level Worker

Primary entry:

- Work
- quick capture
- operational-target lookup
- sync/local status

Secondary entry:

- returned work
- transfer receipt/discrepancy
- follow-up
- limited history/evidence for their work

Avoid:

- broad dashboards
- raw setup concepts
- global exception lists
- architecture mechanism labels

### Supervisor / Team Lead

Primary entry:

- team work
- review and decisions
- oversight
- exceptions
- freshness cues

Secondary entry:

- target context
- transfer chains
- follow-up backlog
- evidence for decisions

Avoid:

- making oversight imply action authority
- hiding stale field state
- turning review into workflow editing
- separate scenario-specific review tools

### Coordinator / Administrator

Primary entry:

- oversight
- setup
- operational periods or campaigns
- assignment and responsibility setup
- broad exception patterns

Secondary entry:

- evidence
- review bottlenecks
- transfer chain progress
- target stewardship where policy allows

Avoid:

- configuration as programming
- admin abstractions leaking into field work
- setup shortcuts that bypass events, configuration packages, or authority rules
- treating deployment/tenant context as event-envelope structure

### Auditor / External Reviewer

Primary entry:

- evidence
- scoped inspection
- exception history
- process traceability

Secondary entry:

- oversight summaries where access allows
- target/work/review history
- export/reference actions where policy allows

Avoid:

- broad cross-scope search by default
- treating audit access as operational action authority
- making export files canonical truth
- closing subject-based scope or auditor access semantics through navigation design

## Cross-Surface Routing Rules

The product should support these routing patterns:

- Work can open capture, review, transfer, follow-up, target detail, evidence, exception detail, or sync detail.
- Target detail can open related work, history, capture, follow-up, review context, transfer context, exception detail, or evidence.
- Review can open source work, target context, decision history, returned-work action, exception detail, or evidence.
- Oversight can drill into work, targets, review queues, transfer chains, follow-up queues, exceptions, freshness detail, or evidence.
- Exceptions can route to the surface that can resolve, explain, or inspect the issue.
- Setup can preview affected work, targets, assignments, reports, and offline/freshness implications without pretending changes are instantly global.
- Evidence can link back to work, targets, review, transfer, setup, or oversight context where access allows.
- Sync/local status can explain affected work and route to items needing action.

Routing must preserve access/sync constraints. A link may explain that an action is unavailable or context is read-only, but it must not create visibility or authority outside the baseline.

## Navigation Anti-Patterns

Avoid these IA shapes:

- one top-level area per scenario
- separate products for capture, review, reporting, transfer, registry, audit, and setup
- architecture-bound navigation such as Events, Projections, Sync Scopes, Flags, Pattern Registry, or Identity Lineage for ordinary users
- a global dashboard that hides freshness and access limits
- a global search that implies all subjects or work are visible
- a setup area that looks like a low-code platform for arbitrary logic
- an exception center that absorbs all conflict, flag, review, discrepancy, and sync behavior as one subsystem
- an audit area that becomes a privileged back door around assignment-derived access
- a mobile IA that forces field users through coordinator/admin concepts before capture

## IA-To-Journey Matrix

| Surface Family | Supports Journeys |
|---|---|
| Work | J1, J2, J4, J7, J8, J10 |
| Operational Targets | J2, J3, J7, J8, J9 |
| Review And Decisions | J1, J4, J8, J9 |
| Oversight | J1, J6, J7, J8, J10 |
| Exceptions | J1, J4, J7, J8, J10 |
| Setup | J5, J6 |
| Evidence | J3, J4, J6, J7, J8, J9 |
| Sync And Local Status | J1, J2, J5, J6, J10 |

## IA-To-Gap Matrix

| IA Area | High-Risk Existing Gaps |
|---|---|
| Work | permission table details; temporary authority/offline revocation; trigger explanation boundaries |
| Operational Targets | subject-based scope; auditor access; alias-cycle behavior; duplicate resolution; cross-flow link access |
| Review And Decisions | Pattern Registry inventory/schema; source-chain traversal limits; general flag semantics; temporary reviewer authority |
| Oversight | reporting/aggregation; workflow-aware reporting; freshness metadata; access-constrained aggregation; cross-level visibility |
| Exceptions | general flag semantics; domain conflict automation; local stale-authority surfacing; malformed-event handling boundaries |
| Setup | configuration authoring/deployment UX; setup/onboarding; Pattern Registry inventory/schema; permission details |
| Evidence | auditor access; structured import/export; retention/archive; sensitive local data lifecycle |
| Sync And Local Status | sync delivery mechanics; local data lifecycle; shared-device actor scope; temporary authority/offline revocation |

## Atomization Implications

Session 5 does not authorize broad product-spec atomization yet. It does clarify what later atomization must preserve:

- product specs should describe reusable surfaces, not scenario-specific apps
- work discovery, capture, review, oversight, setup, evidence, exception, and sync language must be vocabulary-aligned before implementation specs harden
- first-slice planning should prove movement across surfaces, not just isolated capture
- implementation tickets should not use IA labels as storage or service boundaries
- unresolved IA pressure should route to `08-ux-gap-routing.md` before platform-spec atoms depend on it

## Session 5 Output

Later product artifacts should use this IA as follows:

- `06-product-vocabulary-alignment.md` should stabilize the names and forbidden interpretations for Work, Operational Targets, Review, Oversight, Exceptions, Setup, Evidence, and Sync/Local Status.
- `07-interaction-state-model.md` should define user-visible states shared across these surface families.
- `08-ux-gap-routing.md` should route IA pressure around search, audit, reporting, exceptions, setup, and sync to the existing gap register or proposed clarifications.
- `09-first-vertical-slice.md` should choose a slice that moves through Work, Capture, Target Context, Review, Oversight, Evidence, and Sync/Local Status without creating separate scenario products.
