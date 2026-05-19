# Architecture Responsibility Map

Status: Accepted responsibility routing aid from ADR-001 through ADR-005 baseline

This file is kept at its original path for traceability. Its role has been narrowed from a broad system boundary map into an architecture responsibility map.

This document is not a source of platform behavior, not a platform specification, not a second decision register, and not an implementation plan. It routes accepted baseline mechanisms, open gaps, and future controlled specification claims to the responsibility area that should own the language.

Authoritative sources remain:

- `04-architecture-baseline-v0.md` for accepted baseline decisions
- `05-decision-gap-register.md` for open gaps, ownership, priority, closure path, and hold-back triggers
- `02-change-control.md` for later claims that change or close the baseline

## Source Basis

Primary baseline inputs:

- `../10-adr1-5-rest-state-closure-register.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`

Accepted validation inputs:

- `08-baseline-acceptance-check.md`
- `09-identity-boundary-control.md`

Lineage context only:

- `../../viability-assessment.md`
- `../00-extraction-state.md` Iteration 9

The viability assessment identified early primitive groupings and architecture pressures, but Iteration 9 preserved them as narrowing context, not final authority. The responsibilities below are derived from ADR-001 through ADR-005 closure, with viability used only to check that original pressure areas still have a place to land.

## Documentation Role

Use the documents at these altitudes:

| Layer | Owns | Does Not Own |
|---|---|---|
| ADRs and extraction evidence | Why decisions exist and what evidence produced them | Current implementation-facing specification |
| `04-architecture-baseline-v0.md` | Current accepted architecture baseline | Full platform behavior or implementation design |
| `05-decision-gap-register.md` | Open decisions, ownership, closure path, priority, and hold-backs | Boundary prose or repeated gap matrices |
| This map | Responsibility routing for accepted mechanisms and future spec sections | Gap state, normative spec detail, or delivery breakdown |
| Platform specification | Normative implementation-facing behavior under accepted boundaries | Reopening architecture decisions silently |
| Engineering design and tickets | How to build, test, migrate, and operate | Changing baseline semantics without change control |

## Responsibility Rules

- Every settled mechanism has one primary responsibility owner.
- A mechanism may cross responsibilities only through named contracts, references, projections, configuration packages, sync delivery, or events.
- Every unresolved gap has one primary owner in `05-decision-gap-register.md`, even when other areas are affected.
- Later-source claims from ADR-006-R through ADR-009 are assessed input, not automatic authority.
- Responsibility names are routing surfaces for specification and review. They are not deployment services, code modules, product personas, or UI areas.
- Operational actor labels such as field worker, supervisor, coordinator, auditor, reviewer, and regional lead route through assignment, configuration, projection, sync, and product-surface behavior. They do not create fixed platform actor classes.

## Boundary Classification

| Area From The Earlier Map | Classification | Treatment |
|---|---|---|
| Event Log / Storage | True architecture responsibility boundary | Keep. Owns canonical event-log source-of-truth discipline, append-only write path, and projection rebuild constraints. |
| Event Envelope / Schema | Platform-spec contract surface | Keep as a contract owner, not as a separate system boundary. It owns envelope stability and serialization obligations under the baseline. |
| Identity / Lineage | True architecture responsibility boundary | Keep. Owns subject-lineage continuity, alias/split semantics, acyclicity, and raw-reference preservation. |
| Assignment / Authority / Sync | True architecture responsibility boundary | Keep. Owns assignment-derived access, sync scope as access scope, authority reconstruction, and original-subject authorization. |
| Configuration | True architecture responsibility boundary | Keep. Owns the platform/deployer responsibility split and bounded configuration surfaces. |
| Projection / Workflow State | True architecture responsibility boundary plus platform-spec section | Keep. Owns projection-derived workflow state and ADR-005 workflow behavior; platform specification must detail Pattern Registry contracts. |
| Flag / Resolution | Limited true architecture responsibility boundary | Keep only for closed ADR-005 workflow flag behavior and registered open flag gaps. General flag semantics remain open in `05`. |
| Trigger / Reactivity | Implementation/design concern with possible platform-spec section | Do not keep as a baseline architecture boundary. Route through Configuration, Event Log / Storage, and Projection / Workflow State if included later. |
| Reporting / Aggregation | Platform-spec and product capability area | Do not keep as a baseline architecture boundary. Route through Projection / Workflow State and Assignment / Authority / Sync, with policy detail in `05`. |
| Local Data Lifecycle | Policy/implementation responsibility area constrained by architecture | Keep as a gap owner for retention, sensitive data, and local lifecycle work, but do not let it redefine canonical event history or access semantics. |

## Retained Responsibility Areas

| Responsibility Area | Owns | Does Not Own | Primary Spec Routing |
|---|---|---|---|
| Event Log / Storage | Immutable event log source of truth, append-only write discipline, derived read-model rule, projection rebuild source | Identity evolution, authorization policy, workflow semantics, reporting product semantics | Event model, storage, rebuild, versioning obligations |
| Event Envelope / Schema | Stable envelope contract, structural type expression, typed references, `shape_ref`, optional `activity_ref`, device-time advisory semantics | Workflow state storage, authority snapshots, deployer-owned envelope fields, domain facts as structural types | Event envelope, schema/versioning, reference serialization |
| Identity / Lineage | Subject identity continuity, alias/projection evolution, corrective split behavior, acyclicity, raw-reference preservation | Actor provisioning, role assignment, access scope, process lifecycle, general flag lifecycle | Subject identity, lineage projection, identity conflict inputs |
| Assignment / Authority / Sync | Assignment-derived access, sync scope, immutable event sync, projection-derived authority, original-subject authorization, scope-change baseline | Deployer-authored access programs, field-level sensitivity, product personas, workflow state machines | Authorization, sync, offline authority, role-action tables |
| Configuration | Platform/deployer responsibility boundary, bounded shapes/activities/roles/schedules/thresholds/severities/sensitivity parameters/policy choices | Structural event type ownership by deployers, arbitrary access-control logic, general-purpose rules programming | Configuration package, validation, bounded expressions, deployer policy surfaces |
| Projection / Workflow State | Derived workflow state, Pattern Registry as workflow primitive, ADR-005 invalid-transition handling, flagged-event effect on workflow state | Canonical operational truth, subject-lineage lifecycle, general flag semantics, reporting as source of truth | Pattern Registry, workflow projection, bounded `context.*`, process/workflow behavior |
| Flag / Resolution | ADR-005 workflow flag lineage, source-only flagging, source-chain traversal, workflow resolvability classification, L3b auto-resolution attribution | All future flag categories, identity conflict detection itself, authorization checks themselves, general ADR-006+ flag semantics | Workflow flags now; general flag semantics only after `05` closure |
| Local Data Lifecycle | Local retain/remove behavior under scope changes, device storage pressure response, sensitive local lifecycle obligations once closed | Central canonical event mutation, assignment semantics, envelope identity, reporting product semantics | Local retention/removal, sensitive data lifecycle, offline storage constraints |

## Demoted Or Routed Elsewhere

| Earlier Area | Current Routing | Reason |
|---|---|---|
| Trigger / Reactivity | Configuration for trigger declarations, Event Log / Storage for generated event writes, Projection / Workflow State for state inputs, engineering design for execution mechanics | ADR-001 through ADR-005 constrain triggers but do not close a standalone trigger architecture boundary. |
| Reporting / Aggregation | Projection / Workflow State for derived state, Assignment / Authority / Sync for access constraints, product/policy work for reporting needs | Reporting is constrained by the baseline but not a canonical source-of-truth boundary. |
| Product personas and operational roles | Configuration and product-surface vocabulary, with authority routed through Assignment / Authority / Sync | Operational labels are useful examples, not platform actor subclasses. |
| Pattern inventory and pattern schema format | Platform specification under Projection / Workflow State, with gaps owned in `05` | ADR-005 closes the Pattern Registry primitive, not the complete inventory or schema. |
| Process reference emission and process lifecycle | Event Envelope / Schema for reference contract, Projection / Workflow State for process/workflow lifecycle if needed, `05` for closure | ADR-002 names the typed reference category, but active emission sites and lifecycle ownership remain open. |
| Sensitive data purge, encryption, and local lifecycle mechanics | Local Data Lifecycle plus operational policy and implementation design | ADR-003/ADR-004 constrain sensitivity and scope contraction, but policy and mechanics remain open. |

## Cross-Boundary Contracts

| Contract | Primary Owner | Crosses Into | Baseline Constraint |
|---|---|---|---|
| Event envelope contract | Event Envelope / Schema | Event Log / Storage, Identity / Lineage, Assignment / Authority / Sync, Projection / Workflow State | No new envelope fields or structural type values without change control. |
| Original-reference contract | Identity / Lineage | Assignment / Authority / Sync, Flag / Resolution | Authorization checks use original subject reference, not post-merge alias projection. |
| Sync delivery contract | Assignment / Authority / Sync | Event Log / Storage, Local Data Lifecycle, Projection / Workflow State | Sync unit is immutable event; sync is idempotent, append-only, order-independent, and scope-filtered. |
| Configuration package contract | Configuration | Event Envelope / Schema, Projection / Workflow State, Assignment / Authority / Sync, Flag / Resolution | Configuration remains bounded and cannot become deployer-authored platform logic. |
| Projection rebuild contract | Event Log / Storage | Projection / Workflow State, Reporting / Aggregation | Projections are derived and rebuildable from available event subsets. |
| Source-only flag lineage contract | Flag / Resolution | Projection / Workflow State, Identity / Lineage, Assignment / Authority / Sync | ADR-005 workflow flags store root/source flags and project downstream effects. |
| Local lifecycle handoff | Assignment / Authority / Sync | Local Data Lifecycle, Event Log / Storage | Scope expansion is additive; scope contraction starts from selective retain; sensitive handling cannot be hide-only. |

## Open-Gap Ownership

Open-gap ownership is controlled only by `05-decision-gap-register.md`.

This map may explain why a responsibility area is a plausible owner, but it must not carry a second gap matrix. If a gap owner, affected area, closure path, priority, or hold-back trigger changes, update `05-decision-gap-register.md` first.

## Post-ADR Assessment Routing

ADR-006-R through ADR-009 have been assessed through `10` through `13`. Any future later claim must be classified as one of:

- consistent elaboration of a settled baseline rule
- valid closure candidate for a named gap in `05-decision-gap-register.md`
- deferred implementation or specification detail
- new unauthorized claim outside an owned gap
- conflict with a closed baseline rule
- valid dispute requiring formal reopen

Assessment must not silently move ownership between responsibility areas. If a later claim crosses areas, the assessment must name:

- primary responsibility area
- affected responsibility areas
- whether the claim changes a settled mechanism
- whether the claim closes a named gap
- whether formal change control is required

## Disposition

This document is retained, shrunk, and reframed.

- Retained because architecture responsibility routing is still useful for baseline and gap assessment.
- Shrunk because `04` owns accepted baseline decisions and `05` owns gap state.
- Reframed because several earlier "system boundaries" were actually platform-spec sections, implementation/design concerns, or policy surfaces.
- Not retired because the platform still needs one place that says which responsibility area reviews future specification claims.
- Not renamed on disk yet to avoid reference churn. A later documentation hygiene pass can rename the file to `07-architecture-responsibility-map.md` after updating inbound links.

## Closure Checklist

This map is ready to guide responsibility routing when:

- every accepted ADR-001 through ADR-005 mechanism has one responsibility owner
- every known open gap is owned in `05-decision-gap-register.md`
- future specification claims route through the responsibility tables above
- flag behavior remains separated from identity conflict detection, authorization checking, workflow projection, and general flag semantics
- configuration cannot become deployer-authored platform logic
- reporting, triggers, and local lifecycle cannot redefine event-log source-of-truth, authority semantics, or canonical history
- ADR-006-R through ADR-009 claims are routed through classification rather than absorbed as authority
