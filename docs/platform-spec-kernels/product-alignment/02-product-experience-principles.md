# Product Experience Principles

Status: Session 2 product-alignment artifact

This document states product-facing principles for the platform experience before broad atomization resumes. These principles translate the domain ambition, constraints, behavioral patterns, and Session 1 boundary map into testable UX/product expectations.

They are not architecture decisions. They do not rename baseline mechanisms or close gaps. They guide later role, journey, information architecture, vocabulary, interaction-state, and first-slice work.

## Source Basis

Primary inputs:

- `../../README.md`
- `../../constraints.md`
- `../../access-control-scenario.md`
- `../../behavioral_patterns.md`
- `../../principles.md`
- `../../viability-assessment.md`
- `../../scenarios/README.md`
- `01-phase-1-scenario-boundary-map.md`

Architecture guardrails:

- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../pre-operations/04-accepted-pre-atomization-decisions.md`

## Principle Status

These principles are product-alignment guidance. A future artifact may refine wording, but any principle that requires an architecture baseline change must route through the professional-baseline change-control process.

Each principle includes:

- product rule: what the product should feel like or guarantee to users
- scenario pressure: why the rule exists
- architecture guardrail: what it must not violate
- later-session tests: where the principle must be checked next

## PX1. One Coherent Operational System

Product rule:

The platform should feel like one system for recording, responsibility, review, handoff, follow-up, oversight, and configuration. Users should not feel that each scenario is a separate tool with separate concepts.

Scenario pressure:

Simple capture, periodic reporting, review, case follow-up, transfer chains, and coordinated campaigns all share recurring patterns. Scenario 22 proves that multiple patterns can appear together in one operational effort.

Architecture guardrail:

Coherence must come from product composition over accepted boundaries, not from collapsing boundaries into one broad subsystem. Identity, authorization, workflow, flag/resolution, reporting, and configuration remain separately routed.

Later-session tests:

- User roles must describe common work surfaces across tiers.
- Journeys must reuse product patterns instead of inventing bespoke flows for every scenario.
- Information architecture must avoid one-off scenario silos.
- Vocabulary alignment must separate product language from architecture mechanism names.

## PX2. Domain-Agnostic Language With Deployer-Specific Labels

Product rule:

Core product language must be domain-agnostic. Deployers may label configured activities, forms, roles, locations, units, and operational content in domain-specific terms, but the platform's base experience should not assume a health, logistics, agriculture, humanitarian, or government domain.

Scenario pressure:

The domain ambition is a shared operational substrate. Phase 1 scenarios are intentionally domain-pure, while composite examples are validation context rather than core vocabulary authority.

Architecture guardrail:

Do not turn domain labels into platform primitives, envelope fields, structural event types, or hard-coded lifecycle owners. ADR-009's mechanism/instance split remains active: platform mechanisms and deployer-configured instances must be separated.

Later-session tests:

- Vocabulary alignment must classify each term as product-wide, deployer-configured, domain example, or architecture-only.
- Information architecture should support deployer labels without changing platform navigation logic.
- First vertical slice should avoid domain-specific wording in core platform behavior.

## PX3. Simple Work Stays Simple

Product rule:

The simplest capture and lookup experiences should stay lightweight. Users doing basic structured work should not need to understand workflows, flags, identity lineage, pattern composition, sync internals, or projection behavior.

Scenario pressure:

Scenario 00 is the simplicity baseline. Complexity appears as scenarios add subject linkage, responsibility, review, transfer, coordination, or offline reconciliation, but those layers should not burden basic capture.

Architecture guardrail:

Do not hide required audit or validity facts, but disclose them at the point of need. The existence of append-only events, projections, flags, and workflow machinery must not make every activity feel advanced.

Later-session tests:

- Capture journey must have a minimal path.
- Information architecture must not require users to pick technical object types before working.
- Interaction states must define which complexity is visible immediately and which appears only on exception, review, history, or admin surfaces.

## PX4. Offline Work Feels Normal, Not Exceptional

Product rule:

Field users should be able to capture, look up scoped data, make allowed decisions, and continue assigned work while disconnected. The product should show offline state clearly, but offline should not feel like a failure mode for primary field work.

Scenario pressure:

Connectivity constraints and scenario 19 make disconnection the default operating reality. Offline work can overlap with central changes, stale assignments, stale configuration, and other users' work.

Architecture guardrail:

Offline product behavior must preserve immutable event sync, assignment-derived access, local last-known enforcement, and later reconciliation. It must not promise real-time central visibility or global conflict certainty.

Later-session tests:

- Field-worker role expectations must assume intermittent or absent connectivity.
- Journeys must distinguish "done locally" from "visible centrally".
- Interaction states must include offline, pending sync, stale, synced, and sync-problem states.
- Oversight journeys must show freshness without implying live field state.

## PX5. Pending Work Must Explain Why It Exists

Product rule:

When the product shows work as due, overdue, waiting, triggered, assigned, escalated, returned, or blocked, users should be able to understand why it is there in operational terms.

Scenario pressure:

Recurring reports, planned visits, campaign coverage, review queues, trigger responses, and escalations all create work that appears because of time, assignment, observed conditions, state, or another person's decision.

Architecture guardrail:

Explanations must not expose arbitrary rule-engine internals or imply that trigger expressions have unbounded projection access. Scenario 12 remains bounded by the configuration and trigger guardrails.

Later-session tests:

- Queue journeys must include "why this is here" behavior.
- Information architecture must distinguish personal work, team work, review work, exceptions, and oversight without fragmenting the system.
- Interaction states must distinguish waiting, overdue, blocked, triggered, returned, and escalated states.

## PX6. Current State Is Useful, History Remains Visible

Product rule:

Users need clear current interpretations: what is active, resolved, in review, overdue, in transit, complete, disputed, or waiting. But they must also be able to see how the platform reached that interpretation.

Scenario pressure:

Records, corrections, review decisions, transfers, cases, approval chains, and campaign progress all require current views and trustworthy history.

Architecture guardrail:

Current state is projection-derived. Product surfaces may present current status, but must not imply that projection state is canonical truth or that history can be overwritten.

Later-session tests:

- Journey details must include history/timeline access where decisions matter.
- Reporting and dashboard surfaces must preserve freshness and derivation cues.
- Vocabulary alignment must avoid making UI state labels sound like stored canonical event state.

## PX7. Authority Is Contextual, But The UI Should Not Expose Policy Machinery

Product rule:

Users should understand what they can see and do in context: this work is mine, this is read-only, this needs review authority, this belongs to another scope, this requires online/admin action. They should not need to reason through raw policy structures.

Scenario pressure:

Access control is cross-cutting. Authority varies by role, scope, activity, time, review step, hierarchy, temporary grant, and offline freshness.

Architecture guardrail:

The product must preserve assignment-derived access, sync scope as access scope, projection-derived authority, actor/device separation, and no direct authority from accounts, groups, identity-provider claims, or tenant fields.

Later-session tests:

- User-role artifact must separate product roles from architecture actors.
- Journeys must show read-only, not-allowed, reassigned, temporarily granted, and stale-authority cases only where needed.
- UX gap routing must classify subject-based scope, auditor access, shared devices, permission-table details, and temporary authority/offline revocation reconciliation.

## PX8. Exceptions Are Visible, Routed, And Proportionate

Product rule:

Conflicts, flags, duplicate subjects, transfer discrepancies, stale decisions, invalid transitions, and unresolved review issues should be visible to the right people and routed for action, but they should not dominate ordinary work.

Scenario pressure:

Offline work, corrections, duplicate subjects, review returns, transfer disputes, and stale authority all create anomalies that need attention without erasing what happened.

Architecture guardrail:

Accept-and-flag applies only to validly structured state, authority, workflow, identity-lineage, or configured-domain anomalies. Malformed events remain structurally invalid. General flag semantics remain open beyond closed ADR-005 workflow behavior.

Later-session tests:

- Interaction-state model must separate normal, warning, blocked, flagged, disputed, and resolution states.
- Journeys must identify who sees exceptions and who can resolve them.
- UX gap routing must avoid silently closing general flag semantics or domain conflict automation.

## PX9. Oversight Is Freshness-Aware

Product rule:

Supervisor, coordinator, reporting, and campaign views should make progress, gaps, bottlenecks, and exceptions visible while making the age and completeness of the underlying information understandable.

Scenario pressure:

Coordinators need progress across areas, supervisors need team state, distribution chains need current location and outstanding handoffs, and field sync may lag reality by hours or days.

Architecture guardrail:

Oversight and reporting must respect access/sync scope, preserve projection derivation, and not bypass assignment-derived authority. Aggregate views must not become canonical operational state.

Later-session tests:

- Role artifact must distinguish field, supervisor, coordinator/admin, and auditor needs.
- Information architecture must provide oversight surfaces without making them a separate product.
- UX gap routing must carry reporting/aggregation, freshness metadata, and cross-level visibility as explicit gaps.

## PX10. Configuration Feels Like Setup, Not Programming

Product rule:

Coordinators and administrators should experience setup as assembling known operational patterns, defining information shapes, assigning responsibility, and choosing bounded policy values. The product should make the boundary between setup and platform evolution visible.

Scenario pressure:

The domain ambition is "set up, not built." The viability assessment identifies setup/configuration as the weakest-tested promise and the configuration boundary as a major risk.

Architecture guardrail:

Configuration must remain bounded. Deployers configure shapes, activities, roles, schedules, thresholds, severities, sensitivity parameters, and policy choices over platform-owned mechanisms. They do not author arbitrary access-control logic, structural event vocabulary, general-purpose workflow code, or field-level sensitivity.

Later-session tests:

- User-role artifact must define coordinator/admin expectations separately from field and supervisor work.
- Journeys must include a setup/configuration journey before atomization readiness.
- Information architecture must keep admin/configuration surfaces connected to operational concepts without exposing raw internals.
- UX gap routing must preserve configuration authoring/deployment UX and Pattern Registry inventory/schema gaps.

## PX11. Product Vocabulary Must Map Cleanly To Baseline Boundaries

Product rule:

Product terms should be natural for users and stable across scenarios, but every product term that touches platform behavior must map to an accepted boundary, an explicit gap, or a deployer-configured label.

Scenario pressure:

The product needs a coherent language across records, subjects, assignments, reviews, transfers, cases, campaigns, triggers, reports, and exceptions.

Architecture guardrail:

Vocabulary must not collapse reference fields into referent lifecycles, mechanism into instance, or product UI state into canonical storage.

Later-session tests:

- Vocabulary alignment must explicitly map high-risk terms: record, subject, target, unit, work item, activity, scope, assignment, review, transfer, case/situation, flag, conflict, alert, report.
- It must state forbidden interpretations for each high-risk term.
- Candidate vocabulary must become active only through explicit mapping to accepted boundaries, product translation rules, or gap routing.

## PX12. Product Slices Must Prove Composition Before Breadth

Product rule:

Implementation progression should start with a thin but real operational slice that composes multiple product boundaries, instead of atomizing every internal platform surface first.

Scenario pressure:

The Phase 1 scenarios compose from recurring behavioral patterns. Scenario 22 shows that real operations combine capture, subjects, assignment, progress, transfer, offline work, and reconciliation.

Architecture guardrail:

The first slice must preserve accepted baseline constraints and explicitly hold back unresolved gaps. It must not use a product flow to silently decide architecture gaps.

Later-session tests:

- Core journeys should identify slice candidates.
- UX gap routing should mark which gaps block or do not block the first slice.
- First vertical slice should name its accepted scope, excluded concerns, and atomization dependencies.

## Principle-To-Session Trace

| Principle | Roles | Journeys | IA | Vocabulary | State Model | UX Gaps | First Slice |
|---|---|---|---|---|---|---|---|
| PX1 One coherent system | required | required | required | required | required | required | required |
| PX2 Domain-agnostic language | required | required | required | required | optional | required | required |
| PX3 Simple work stays simple | field role | capture journey | required | required | required | required | required |
| PX4 Offline feels normal | field/supervisor | required | required | optional | required | required | required |
| PX5 Pending work explains why | field/supervisor | required | required | required | required | required | required |
| PX6 Current state and history | all roles | required | required | required | required | required | required |
| PX7 Contextual authority | all roles | required | required | required | required | required | required |
| PX8 Proportionate exceptions | supervisor/admin | required | required | required | required | required | required |
| PX9 Freshness-aware oversight | supervisor/coordinator | required | required | optional | required | required | required |
| PX10 Setup, not programming | coordinator/admin | setup journey | admin IA | required | optional | required | maybe |
| PX11 Vocabulary maps to baseline | required | required | required | required | required | required | required |
| PX12 Composition before breadth | optional | required | required | optional | optional | required | required |

## Session 2 Output

These principles should govern the next artifact, `03-user-roles-and-operational-contexts.md`.

The role artifact should not start by listing architecture actors. It should start with operating contexts: field worker, supervisor/team lead, coordinator/administrator, and auditor/external reviewer. It should then map role expectations to the principles above and to the Session 1 product boundaries.
