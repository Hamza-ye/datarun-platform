# User Roles And Operational Contexts

Status: Session 3 product-alignment artifact

This document describes the people the product must serve, the conditions they work under, and the expectations their product experience creates. It is not an authorization model, not an actor schema, and not a deployment-specific role catalog.

The purpose is to keep later journeys, information architecture, vocabulary, and first-slice planning grounded in real operating contexts without turning product personas into architecture primitives.

## Source Basis

Primary inputs:

- `../../README.md`
- `../../constraints.md`
- `../../access-control-scenario.md`
- `../../scenarios/README.md`
- `01-phase-1-scenario-boundary-map.md`
- `02-product-experience-principles.md`

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

The roles below are product operating contexts. They do not grant authority by themselves.

Do not infer:

- a product role is an event-envelope field
- a product role is an architecture actor type
- a product role is a direct permission source
- a user group grants sync scope or action authority
- an identity-provider claim grants platform authority
- a tenant or deployment field grants visibility

Effective authority remains assignment-derived through roles, scopes, activity/context, time, and platform-fixed containment semantics. Authentication proves that a principal may act as an actor; it does not define what the actor may see or do.

If a later journey appears to require a role-specific storage field, permanent permission shortcut, special sync exception, or deployer-authored authority rule, treat that as product pressure on the baseline. Route it through gap review or change control instead of embedding it in product vocabulary.

## Role Model Vocabulary

Use these terms narrowly in this product-alignment track:

| Term | Product Meaning | Not Allowed To Mean |
|---|---|---|
| Operating context | The conditions, responsibilities, devices, connectivity, and decision pressure a person works under | Architecture boundary or security role |
| Product role | A recurring user lens used for product design | Event actor type, permission source, or deployer role catalog |
| Deployer role | A configured label used by a deployment to express responsibility or authority within bounded platform mechanisms | Platform primitive or hard-coded product role |
| Actor | A person or system identity that authors/performs an action in the baseline | Product persona or user group |
| Assignment | A time-bound responsibility/authority binding consumed by authorization and sync | Product role, user group, or team membership by itself |
| Team/group | A possible admin or organizational convenience | Direct authority source unless later formal change control allows it |

## Role Summary

| Product Role | Primary Work | Typical Device | Connectivity | Product Priority |
|---|---|---|---|---|
| Field-level worker | Capture, lookup, assigned work, visits, handoffs, follow-up | Low-end Android phone | Frequently offline | Fast, resilient, scoped work with clear local status |
| Supervisor / team lead | Review, assess, coordinate teams, resolve operational bottlenecks | Phone, tablet, sometimes laptop | Intermittent | Queues, team progress, review decisions, freshness awareness |
| Coordinator / administrator | Configure activities, assign responsibility, monitor progress, manage operational setup | Laptop/desktop | Generally reliable | Setup, oversight, controlled change, validation, bounded configuration |
| Auditor / external reviewer | Verify work, inspect records, review compliance or process evidence | Laptop/tablet/phone | Usually planned access | Targeted visibility, traceability, read-mostly review, exception access |

These are broad product roles. A deployment may use different names, combine roles, or split them further. The product should support that without changing core concepts.

## Field-Level Worker

Operating context:

- Does primary operational work.
- Often works alone or in small teams.
- Often has low or intermittent connectivity.
- Usually uses a phone, often low-end Android.
- Needs the product in the field, not only at a desk.
- May have basic to moderate digital literacy and local-language needs.

Typical work:

- capture structured information
- look up scoped subjects, locations, units, resources, or situations
- complete assigned work
- perform visits or field actions
- acknowledge handoffs or record discrepancies
- follow up on active situations
- sync when connectivity appears

Experience expectations:

- capture starts quickly and works offline
- assigned work is easy to find
- the product explains what must be done now, what is optional, and what can wait
- local work clearly shows whether it is saved locally, pending sync, or synced
- stale information is shown only when it matters to the action
- correction and exception flows are understandable without exposing architecture machinery

Product boundaries touched:

- Capture And History
- Subject Continuity And Registry Work
- Responsibility, Authority, And Visibility
- Queues, Rhythm, And Due Work
- Transfer, Custody, And Discrepancy
- Long-Running Situations And Follow-Up
- Offline, Sync, And Reconciliation

Principle checks:

- PX3 Simple Work Stays Simple
- PX4 Offline Work Feels Normal
- PX5 Pending Work Must Explain Why It Exists
- PX6 Current State Is Useful, History Remains Visible
- PX7 Authority Is Contextual
- PX8 Exceptions Are Visible, Routed, And Proportionate

Architecture debt guardrails:

- Do not make offline field work require global knowledge.
- Do not make device identity actor identity.
- Do not require field users to understand subject-lineage mechanics to capture ordinary work.
- Do not expose every flag/conflict pathway in ordinary capture.
- Do not make group membership or product role grant access.

Open questions to route later:

- shared-device actor scope if field devices are shared
- local data lifecycle after scope contraction
- exact offline revocation/grace behavior for temporary assignments
- low-end device performance and sync delivery mechanics

## Supervisor / Team Lead

Operating context:

- Oversees field-level work.
- Works partly in the field and partly from a hub, office, or meeting point.
- May use a phone, tablet, or laptop.
- Connectivity is better than field workers but still intermittent.
- Needs to see across multiple people, places, periods, or work queues.

Typical work:

- review submitted work
- return, approve, question, or escalate work
- inspect team progress and bottlenecks
- plan or verify visits
- monitor overdue or missing work
- resolve or route operational exceptions
- coordinate reassignment or handoff where policy allows

Experience expectations:

- review queues show aging and why each item is waiting
- team views show progress without pretending to be live
- decisions are easy to make and leave a clear trail
- exceptions are routed to the right action surface
- freshness is visible when it affects judgment
- supervisor authority is contextual and should be visible as allowed actions, read-only states, or escalation paths

Product boundaries touched:

- Review, Judgment, And Approval
- Queues, Rhythm, And Due Work
- Oversight, Progress, And Reporting
- Responsibility, Authority, And Visibility
- Offline, Sync, And Reconciliation
- Transfer, Custody, And Discrepancy

Principle checks:

- PX1 One Coherent Operational System
- PX5 Pending Work Must Explain Why It Exists
- PX6 Current State Is Useful, History Remains Visible
- PX7 Authority Is Contextual
- PX8 Exceptions Are Visible, Routed, And Proportionate
- PX9 Oversight Is Freshness-Aware

Architecture debt guardrails:

- Do not turn supervisor visibility into uncontrolled action authority.
- Do not make reports or dashboards canonical operational truth.
- Do not bypass assignment/sync-scope constraints for oversight convenience.
- Do not silently close general flag semantics through review UX.
- Do not make multi-step approval a special-case screen if it should compose from review/judgment patterns.

Open questions to route later:

- cross-level distribution visibility
- assessment visibility
- reporting freshness metadata
- permission table details
- temporary authority if supervisors can cover, delegate, or revoke while devices are offline

## Coordinator / Administrator

Operating context:

- Manages operational setup and large-scope oversight.
- Usually works from an office, regional hub, or central location.
- Uses laptop/desktop most often, with more reliable connectivity.
- Needs to change setup carefully because changes affect field devices later on sync.
- Thinks in terms of activities, assignments, oversight rules, reporting needs, and operational periods.

Typical work:

- define or update what information is collected
- configure activities and their expected rhythms
- assign people, roles, areas, or responsibilities
- monitor high-level progress and exceptions
- manage setup, onboarding, and role transitions
- publish configuration changes
- coordinate campaign windows or grouped-location efforts
- manage registry stewardship where policy allows

Experience expectations:

- setup feels like assembling known operational patterns, not programming
- configuration changes are previewed, validated, and published deliberately
- impact of changes is understandable, especially for offline users and in-progress work
- assignment and visibility changes are auditable
- the product clearly separates what can be configured from what requires platform evolution
- large-scope dashboards are useful but freshness-aware

Product boundaries touched:

- Configuration / Setup Experience
- Responsibility, Authority, And Visibility
- Oversight, Progress, And Reporting
- Queues, Rhythm, And Due Work
- Subject Continuity And Registry Work
- Offline, Sync, And Reconciliation

Principle checks:

- PX1 One Coherent Operational System
- PX2 Domain-Agnostic Language With Deployer-Specific Labels
- PX7 Authority Is Contextual
- PX9 Oversight Is Freshness-Aware
- PX10 Configuration Feels Like Setup, Not Programming
- PX11 Product Vocabulary Must Map Cleanly To Baseline Boundaries
- PX12 Product Slices Must Prove Composition Before Breadth

Architecture debt guardrails:

- Do not turn configuration into arbitrary platform logic.
- Do not let deployers author structural event types, access-control programs, or workflow state machines.
- Do not add envelope fields to satisfy setup or admin convenience.
- Do not let user groups or identity-provider claims directly grant access.
- Do not treat deployment/tenant context as an event-envelope concern under the current pre-op guardrail.

Open questions to route later:

- configuration authoring and deployment UX
- exact Pattern Registry inventory and schema
- setup, onboarding, and role transition details
- permission table details
- sensitive-subject policy beyond shape/activity sensitivity
- deployment packaging and self-host operations if needed

## Auditor / External Reviewer

Operating context:

- Needs targeted or periodic access to verify work and process integrity.
- May cut across normal hierarchy for a specific purpose.
- Usually acts in planned review windows, audits, or investigations.
- Often needs traceability more than operational action authority.

Typical work:

- inspect records, decisions, corrections, and handoffs
- verify that work happened under appropriate responsibility and authority
- review evidence across teams, locations, activities, or time windows
- inspect exceptions, discrepancies, or resolution history
- export or reference audit evidence where policy allows

Experience expectations:

- access is explicit, scoped, and explainable
- audit views show history and current interpretation together
- reviewer can see why an item was visible to them
- audit access does not silently grant operational action authority
- sensitive information is handled according to deployment policy and platform lifecycle constraints

Product boundaries touched:

- Responsibility, Authority, And Visibility
- Oversight, Progress, And Reporting
- Capture And History
- Transfer, Custody, And Discrepancy
- Subject Continuity And Registry Work
- Offline, Sync, And Reconciliation

Principle checks:

- PX2 Domain-Agnostic Language With Deployer-Specific Labels
- PX6 Current State Is Useful, History Remains Visible
- PX7 Authority Is Contextual
- PX8 Exceptions Are Visible, Routed, And Proportionate
- PX9 Oversight Is Freshness-Aware
- PX11 Product Vocabulary Must Map Cleanly To Baseline Boundaries

Architecture debt guardrails:

- Do not close auditor access semantics accidentally.
- Do not bypass assignment-derived access or sync-scope constraints.
- Do not create broad cross-scope access as a UI shortcut.
- Do not treat audit export as canonical operational state.
- Do not define field-level sensitivity unless the baseline is formally reopened.

Open questions to route later:

- subject-based scope and auditor access
- assessment visibility
- structured import/export compatibility
- reporting and aggregation
- retention and archival policy
- sensitive local data lifecycle where audit access intersects device storage

## Cross-Role Relationships

### Field Worker To Supervisor

Product relationship:

- field worker creates or completes operational work
- supervisor reviews, verifies, coordinates, or follows up
- supervisor may see team-level progress and exception queues

UX risk:

- returned/rejected work can feel punitive or confusing if the product does not explain what changed and what action is required
- offline delay can make supervisor queues look incomplete or stale

Guardrail:

- review decisions are auditable work, not hidden mutable status
- supervisor visibility must not become automatic action authority

### Supervisor To Coordinator / Administrator

Product relationship:

- supervisor sees operational bottlenecks
- coordinator changes setup, assignments, or campaign plans where policy allows
- coordinator monitors larger scope and configuration impact

UX risk:

- setup changes may appear immediate centrally but reach field devices later
- progress dashboards may appear authoritative even when field sync is stale

Guardrail:

- configuration changes propagate on sync
- oversight is projection-derived and freshness-aware
- setup/admin convenience must not bypass event/configuration mechanisms

### Coordinator / Administrator To Field Worker

Product relationship:

- coordinator defines activities, shapes, assignments, and expected rhythms
- field worker experiences those definitions as work to do, forms to complete, and scope to sync

UX risk:

- admin abstractions can leak into field experience
- configuration complexity can make simple work feel complicated

Guardrail:

- simple work stays simple
- deployer labels are configuration instances, not platform mechanisms
- work started under older configuration remains understandable

### Auditor / External Reviewer To All Roles

Product relationship:

- auditor inspects records and operational history across a scoped purpose
- audit may cross normal reporting hierarchy

UX risk:

- audit visibility can become an uncontrolled global read path
- audit access can be confused with authority to act

Guardrail:

- auditor access remains an explicit gap unless expressible through existing assignment/scope mechanisms
- audit surfaces must preserve traceability and access constraints

## Cross-Role Product Requirements

These requirements apply across all roles:

- The product must explain why work is visible, actionable, read-only, blocked, stale, or missing.
- The product must distinguish local completion from central visibility.
- The product must preserve history behind current views.
- The product must show freshness where stale information affects decisions.
- The product must keep exceptions proportionate and routed.
- The product must support deployer labels without losing domain-agnostic structure.
- The product must not expose internal architecture concepts as required user knowledge.

## Role-To-Boundary Matrix

| Product Role | Primary Product Boundaries | High-Risk Gaps |
|---|---|---|
| Field-level worker | Capture; Subject lookup; Assigned work; Offline/sync; Handoff/follow-up | shared devices, local lifecycle, sync delivery mechanics, temporary authority/offline revocation |
| Supervisor / team lead | Review; Team progress; Exceptions; Freshness-aware oversight | cross-level visibility, assessment visibility, reporting freshness, permission details |
| Coordinator / administrator | Setup/configuration; Assignment; Campaign/period planning; Broad oversight | configuration UX, Pattern Registry inventory/schema, onboarding/role transition, permission details |
| Auditor / external reviewer | Audit views; Traceability; Scoped cross-cutting visibility; Export/reference | auditor access, subject-based scope, retention/archive, structured export, sensitive lifecycle |

## Role-To-Principle Matrix

| Product Role | Most Important Principles |
|---|---|
| Field-level worker | PX3, PX4, PX5, PX6, PX7, PX8 |
| Supervisor / team lead | PX1, PX5, PX6, PX7, PX8, PX9 |
| Coordinator / administrator | PX1, PX2, PX7, PX9, PX10, PX11, PX12 |
| Auditor / external reviewer | PX2, PX6, PX7, PX8, PX9, PX11 |

## Explicit Non-Decisions

This artifact does not decide:

- account schema
- user group model
- external identity-provider integration
- shared-device multi-actor sessions
- concrete deployer role names
- permission table details
- subject-based scope semantics
- auditor access semantics
- tenant or deployment identity model
- exact admin/configuration screens
- reporting aggregation model

These remain governed by the professional-baseline gap register, pre-operations decisions, and later product-alignment artifacts.

## Session 3 Output

Later product artifacts should use these role contexts as lenses:

- `04-core-operational-journeys.md` should describe journeys by operating context, not by architecture boundary.
- `05-information-architecture.md` should organize surfaces so each role can find work without creating separate products.
- `06-product-vocabulary-alignment.md` should distinguish product roles, deployer roles, actors, assignments, groups, and scopes.
- `08-ux-gap-routing.md` should route every role-related uncertainty to an existing gap or proposed clarification.
