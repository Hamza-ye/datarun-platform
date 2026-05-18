# Interaction State Model

Status: Session 7 product-alignment artifact

This document defines user-visible interaction states for the operational surface before selected-slice platform-spec section drafting. It describes how the product should label, explain, and route visible states across work, records, operational targets, review, oversight, exceptions, setup, evidence, and sync/local status.

It is not a workflow schema, not a stored state model, not a projection schema, not a permission table, and not a sync protocol.

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
- `05-information-architecture.md`
- `06-product-vocabulary-alignment.md`

Architecture guardrails:

- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../pre-operations/04-accepted-pre-specification-decisions.md`

## Control Rule

Interaction states are user-visible interpretations produced by the product translation layer. They do not define platform-core storage.

Do not infer:

- a visible state is a stored event state
- a visible state is an event-envelope value
- a visible state is a workflow state machine node
- a visible state is a Pattern Registry entry
- a visible state is a permission rule
- a visible state grants or revokes authority
- a visible state defines sync protocol
- a visible state closes general flag/conflict semantics
- a visible state creates a canonical work-item, dashboard, record, or target model

Visible state labels must map to accepted product vocabulary from `06-product-vocabulary-alignment.md`. If a state needs a stronger platform meaning, route the pressure to `08-ux-gap-routing.md`, the gap register, or change control.

## State Layering Rule

Use this split:

| Layer | State Meaning |
|---|---|
| Platform core | Immutable events, assignment-derived authority, scoped sync, projection-derived workflow/current views, accepted flag behavior |
| Product translation layer | Read models, action affordances, freshness, explanations, routing, warning/blocked/read-only decisions |
| Operational surface | User-visible labels such as due, waiting, returned, stale, pending sync, read-only, needs attention |

The operational surface may say that work is `due`, a review is `waiting`, a record is `pending sync`, or a target is `possible duplicate`. The platform core still speaks through accepted mechanisms. Product state labels must never become a hidden replacement for those mechanisms.

## State Model Principles

Every visible state should answer five questions where relevant:

1. What does this mean in operational language?
2. Why is the user seeing it?
3. What can the user do next?
4. Is the information fresh, stale, local, pending, or centrally visible?
5. Where can the user inspect history or evidence?

Every visible state should avoid four failures:

1. Implying live truth when the view may be stale.
2. Hiding history behind a current label.
3. Presenting authority as a role, group, tenant, device, or surface shortcut.
4. Turning an open architecture gap into a settled product behavior.

## State Families

Session 7 stabilizes these state families:

| Family | Purpose | Primary Surfaces |
|---|---|---|
| Work Attention States | Explain why work needs attention | Work, Oversight, Review And Decisions, Exceptions |
| Action Availability States | Explain what the user can do | Work, Review And Decisions, Setup, Evidence, Sync And Local Status |
| Local And Sync States | Explain local/central visibility | Work, Evidence, Sync And Local Status, Oversight |
| Freshness States | Explain age/completeness of visible information | Oversight, Review And Decisions, Evidence, Setup, Sync And Local Status |
| Review And Decision States | Explain review/judgment position | Review And Decisions, Work, Evidence, Oversight |
| Exception And Resolution States | Explain issues requiring attention | Exceptions, Work, Review And Decisions, Sync And Local Status, Evidence |
| Operational Target States | Explain target usability and ambiguity | Operational Targets, Work, Evidence |
| Transfer And Custody States | Explain handoff/custody interpretation | Work, Oversight, Exceptions, Evidence |
| Setup And Rollout States | Explain configuration readiness and propagation | Setup, Work, Oversight, Sync And Local Status |
| Evidence And History States | Explain traceability and current interpretation | Evidence, Review And Decisions, Operational Targets, Oversight |

## Work Attention States

Purpose:

Show why work appears and what kind of attention it needs.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Due | Expected now or within the current period | Start or complete work | Do not imply a stored work-item primitive |
| Overdue | Expected time has passed | Complete, escalate, or inspect reason | Aging is product/read-model interpretation |
| Waiting | Work cannot progress until another action happens | Inspect reason or wait | Waiting is not canonical state by itself |
| Returned | Someone sent the work back for correction or more information | Review explanation and correct | Review decision remains auditable history |
| Triggered | Something observed or configured made this need attention | Inspect reason and act where allowed | Do not expose or expand trigger internals |
| Escalated | Work has exceeded threshold or needs higher attention | Act, route, or inspect | Escalation policy details may be configured/gap-routed |
| Blocked | User cannot proceed until a condition is resolved | Inspect blocker or route issue | Blocked does not define permission or workflow schema |
| Missing | Expected work has not been received or observed | Assign, follow up, or inspect freshness | Missing may be stale/incomplete, not absolute truth |
| Pending Local Work | Work exists locally but is not centrally visible | Sync or inspect local status | Must distinguish local completion from central visibility |

Required explanations:

- why this work is visible
- whether it is actionable or read-only
- whether freshness affects interpretation
- where history/evidence can be inspected

Gap routing:

- trigger explanation boundaries
- reporting freshness metadata
- Pattern Registry inventory/schema
- workflow-aware reporting

## Action Availability States

Purpose:

Show whether the user can act, view, wait, escalate, or needs a different context.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Actionable | The user can perform the shown action in this context | Act | Does not reveal raw permission machinery |
| Read-Only | The user may view but not act | Inspect or follow route | Visibility does not imply action authority |
| Not Available Here | Action exists but not in this context | Explain required context or route | Do not expose inaccessible work merely to explain it |
| Needs Online Action | Action cannot be completed offline | Connect or defer | Use only where baseline/implementation requires online handling |
| Temporarily Available | User appears to have time-bound access/action | Act with freshness warning where needed | Temporary authority policy remains gap-routed |
| Stale Authority Warning | Local authority may differ from central state | Act cautiously, sync, or inspect | Do not store `authority_context` or close revocation policy |
| Requires Review | User action must be judged by another context | Submit or inspect review path | Review path is not a permission shortcut |
| Requires Setup Change | User cannot solve through ordinary work | Route to setup/admin context | Setup cannot become arbitrary platform logic |

Required explanations:

- what can be done here
- why an action is unavailable
- whether sync/freshness may change availability
- where to route if the user cannot act

Gap routing:

- permission table details
- temporary authority/offline revocation reconciliation
- shared-device actor scope
- setup/onboarding and role transition

## Local And Sync States

Purpose:

Help users understand what is saved locally, what is centrally visible, and what needs sync or reconciliation.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Offline | Device is not connected | Continue allowed local work | Offline is normal, not failure |
| Saved Locally | Work is stored on this device | Continue or sync later | Do not imply central visibility |
| Pending Sync | Local work is waiting to upload | Sync when possible | Does not define transport mechanics |
| Syncing | Exchange is currently in progress | Wait or continue where safe | Does not define protocol |
| Synced | Known local work has been sent/received according to current sync result | Continue or inspect | Do not imply global real-time truth |
| Sync Problem | Something failed or needs attention | Retry, inspect, or route | Error should be actionable where possible |
| Needs Reconciliation | Valid work created under stale/conflicting conditions needs attention | Inspect exception | Do not make malformed events acceptable |
| Local Data May Be Stale | Device has not received recent central changes | Sync or act with caution | Freshness must be visible when decision-relevant |
| Removed From Device Scope | Data/action may no longer be available locally after scope change | Inspect explanation where allowed | Local lifecycle remains gap-constrained |

Required explanations:

- what is saved locally
- what is centrally visible
- when the last meaningful sync happened
- what failed or needs reconciliation
- whether the user can keep working

Gap routing:

- sync delivery mechanics
- local data lifecycle
- temporary authority/offline revocation reconciliation
- shared-device actor scope
- sensitive local data lifecycle

## Freshness States

Purpose:

Make current views useful without pretending they are live or canonical.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Fresh | View is based on recent known data | Continue | Fresh does not mean globally complete |
| May Be Stale | View may not include recent offline/remote work | Inspect age or sync | Do not hide stale state where decision-relevant |
| Stale | View is old enough to affect decisions | Sync, wait, or act cautiously | Do not imply live field state |
| Incomplete | Product knows the view lacks some expected data | Inspect missing/pending detail | Incomplete is not always non-compliance |
| Unknown Completeness | Product cannot know whether more data exists | Explain uncertainty | Avoid false certainty |
| Locally Complete | User/device completed known local work | Sync or inspect | Not centrally visible yet |
| Centrally Visible | Shared system has received the work | Continue, review, or report | Still projection-derived, not dashboard truth |

Required explanations:

- data age or last known sync where relevant
- whether missing work may be caused by offline delay
- whether current interpretation is safe for decision-making
- where to inspect detail/history

Gap routing:

- reporting freshness metadata
- reporting and aggregation
- projection performance and caching
- access-constrained aggregate views

## Review And Decision States

Purpose:

Explain review position and decisions without exposing raw workflow machinery.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Waiting Review | Work is ready for judgment | Reviewer acts where allowed | Not a stored canonical state by itself |
| Under Review | Judgment is in progress or assigned | Wait or inspect | Do not invent locking semantics |
| Approved / Accepted | Reviewer accepted the work | Continue next journey | Decision is auditable history |
| Returned | Reviewer requested correction or more information | Correct and resubmit | Return reason must be visible |
| Rejected | Reviewer rejected the work where configured | Inspect reason or appeal/escalate if allowed | Rejection behavior depends on pattern/config |
| Questioned | Reviewer raised an issue without final decision | Respond or inspect | Do not close general flag semantics |
| Escalated | Review moved to higher/different context | Inspect route | Authority remains contextual |
| Deferred | Reviewer delayed decision where allowed | Wait or inspect reason | Do not imply indefinite hidden state |
| Step Waiting | Multi-step review is waiting at a specific visible step | Act, wait, or inspect | Avoid exposing full workflow internals to all users |

Required explanations:

- who/what context is expected to act where visible
- age of waiting review where relevant
- decision reason where configured/required
- history of prior decisions
- what the current user can do next

Gap routing:

- Pattern Registry inventory/schema
- source-chain traversal limits
- temporary reviewer authority
- general flag semantics
- permission table details

## Exception And Resolution States

Purpose:

Surface issues that need attention without collapsing all exception types into one subsystem.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Needs Attention | Something requires user review/action | Open detail | Must explain specific reason |
| Disputed | Parties recorded disagreement or discrepancy | Resolve or route | Does not close domain conflict automation |
| Possible Duplicate | The platform suspects two targets/records may overlap | Inspect or route | Duplicate policy remains controlled/gap-routed |
| Invalid Transition | Workflow action conflicts with accepted workflow behavior | Review/resolve where allowed | Covered only where baseline behavior exists |
| Stale Authority Issue | Work/action happened under possibly outdated authority | Inspect/route | Revocation/grace policy remains open |
| Stale Configuration Issue | Work used older setup after change | Inspect/route | Older valid work remains understandable |
| Sync Problem | Local/central exchange failed or needs user action | Retry/inspect | Does not define transport behavior |
| Blocked By Exception | Downstream action waits on resolution | Resolve or route | Do not overgeneralize flag inertness |
| Resolved | Issue was handled and history remains visible | Continue/inspect | Resolution does not erase original history |

Required explanations:

- exact issue type where known
- why it matters
- who can act where visible
- whether ordinary work can continue
- resolution history or route

Gap routing:

- general flag semantics
- domain conflict automation outside workflow
- alias-cycle behavior
- malformed-event handling boundaries
- stale-authority surfacing

## Operational Target States

Purpose:

Help users select, inspect, and trust operational targets without exposing broad identity-lineage machinery.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Active | Target is available for ordinary work | Select or inspect | Does not imply global visibility |
| Inactive | Target should not be used for new ordinary work by default | Inspect history or choose another | History remains intact |
| Deactivated | Target was intentionally retired/closed where configured | Inspect history | Does not break historical references |
| Possible Duplicate | Target may overlap another target | Inspect or route | Duplicate resolution remains controlled |
| Ambiguous | Product cannot confidently distinguish targets | Choose carefully or route | Avoid blocking valid work unnecessarily |
| Merged / Split History | Target continuity changed where baseline allows | Inspect history | Historical references are not rewritten |
| Created Locally | Target was created offline/on device | Sync or continue where allowed | Central visibility may be pending |
| Pending Match | Target/context may need later matching | Continue or route | Process identity/pending-match remains gap-routed |

Required explanations:

- whether target can be used for new work
- what history remains visible
- whether ambiguity affects the current action
- whether central visibility is pending

Gap routing:

- alias-cycle enforcement and resolution semantics
- duplicate-resolution UX
- subject-based scope
- auditor access
- process identity and pending-match UX

## Transfer And Custody States

Purpose:

Make handoffs and custody understandable without rewriting history.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Prepared / Sent | Sender recorded a transfer | Await receipt | Does not imply receiver acknowledged |
| In Transit | Transfer has not yet been acknowledged/disputed | Monitor or follow up | Derived view, not canonical truth |
| Awaiting Receipt | Receiver is expected to acknowledge | Acknowledge or dispute | Visibility/action remains scoped |
| Received | Receiver acknowledged full receipt | Continue linked work | Receipt remains auditable |
| Partially Received | Receiver acknowledged less than expected | Record/resolve discrepancy | Does not rewrite sender record |
| Disputed | Receiver/sender recorded disagreement | Resolve or route | Domain conflict automation remains open |
| Outstanding | Expected acknowledgement or item remains unresolved | Follow up/escalate | Freshness may affect interpretation |
| Chain Delayed | Multi-hop chain is aging or stalled | Inspect bottleneck | Reporting/aggregation remains gap-routed |

Required explanations:

- what was recorded by each party
- what is still outstanding
- whether freshness affects chain view
- where discrepancy/history can be inspected

Gap routing:

- cross-level distribution visibility
- domain conflict automation outside workflow
- transfer reporting and aggregation
- exact workflow pattern inventory
- linked-work access behavior

## Setup And Rollout States

Purpose:

Explain configuration readiness and propagation without making setup feel like programming.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Draft Setup | Setup changes are not published | Validate or edit | Draft setup is not core event history claim by itself |
| Needs Validation | Setup has issues before publish | Fix or inspect | Validator UX remains implementation/tooling detail |
| Ready To Publish | Setup can be deliberately published | Publish where allowed | Does not bypass configuration package rules |
| Published | Setup change is available centrally | Monitor rollout | Does not imply all devices have received it |
| Rolling Out | Some users/devices may not have received setup | Monitor freshness | Sync delivery remains gap-routed |
| Older Setup In Use | Work may continue under older setup version | Inspect or allow completion | Older valid work remains valid/readable |
| Setup Impact Warning | Change affects active/offline work | Review impact | Does not authorize arbitrary migration behavior |
| Requires Platform Evolution | Need exceeds bounded setup | Route to product/platform decision | Setup is not programming |

Required explanations:

- whether setup is draft, validated, published, or rolling out
- what users/work may be affected
- whether offline users may still see old setup
- what cannot be configured without platform change

Gap routing:

- configuration authoring/deployment UX
- deploy-time validator UX
- migration tooling for breaking changes
- Pattern Registry inventory/schema
- deployment packaging UX

## Evidence And History States

Purpose:

Help users understand current interpretation and history together.

| Visible State | User Meaning | Typical Next Step | Guardrail |
|---|---|---|---|
| Current Interpretation | Product's current derived view | Inspect history if needed | Not canonical storage |
| Original Record Visible | The underlying recorded fact/action can be inspected | Inspect evidence | Product record maps to event history |
| Corrected / Amended | Later record changed interpretation | Compare history | Original history remains visible |
| Decision Recorded | Judgment was recorded | Inspect decision/reason | Decision is auditable work |
| Resolution Recorded | Exception/dispute was handled | Inspect original and resolution | Resolution does not erase issue |
| Export Available | Evidence can be exported/referenced where policy allows | Export/reference | Export is not canonical truth |
| Access-Limited Evidence | Some evidence cannot be shown in this context | Explain limits where safe | Do not leak inaccessible data |
| Retention/Archive Notice | Evidence availability is affected by policy | Inspect policy/context | Retention/archive remains gap-routed |

Required explanations:

- what current interpretation is based on
- where original history can be inspected
- what changed and who acted where visible
- whether export/access/retention constraints apply

Gap routing:

- structured import/export compatibility
- retention and archival
- auditor access
- sensitive local data lifecycle
- field-level sensitivity rejection

## Cross-State Composition Rules

States may combine. The product should prefer clear combinations over hiding important conditions.

Examples:

| Combined State | Meaning |
|---|---|
| Due + Offline + Saved Locally | User can do work offline; central visibility is pending |
| Returned + Pending Sync | User corrected locally, but reviewer cannot see correction yet |
| Approved + May Be Stale | Decision is visible, but underlying field state may have changed |
| Missing + Unknown Completeness | Product expected work but cannot know whether offline work exists |
| Awaiting Receipt + Chain Delayed | Transfer needs acknowledgement and is aging |
| Read-Only + Needs Attention | User can inspect issue but cannot resolve it here |
| Older Setup In Use + Pending Sync | Work was created under older local setup and not yet centrally visible |
| Possible Duplicate + Created Locally | Locally created target may need central matching |

Rules:

- Do not hide sync/freshness state behind a workflow label.
- Do not hide authority/action state behind an exception label.
- Do not show dashboard/oversight states without freshness when freshness affects decisions.
- Do not collapse exception type into generic "conflict" when a narrower term is available.
- Do not show architecture terms to ordinary users when product terms can explain the state.

## State-To-Surface Matrix

| State Family | Primary Surfaces |
|---|---|
| Work Attention States | Work, Oversight, Review And Decisions, Exceptions |
| Action Availability States | Work, Review And Decisions, Setup, Evidence, Sync And Local Status |
| Local And Sync States | Work, Sync And Local Status, Evidence, Oversight |
| Freshness States | Oversight, Review And Decisions, Evidence, Setup, Sync And Local Status |
| Review And Decision States | Review And Decisions, Work, Evidence, Oversight |
| Exception And Resolution States | Exceptions, Work, Review And Decisions, Sync And Local Status, Evidence |
| Operational Target States | Operational Targets, Work, Evidence |
| Transfer And Custody States | Work, Oversight, Exceptions, Evidence |
| Setup And Rollout States | Setup, Work, Oversight, Sync And Local Status |
| Evidence And History States | Evidence, Review And Decisions, Operational Targets, Oversight |

## State-To-Gap Matrix

| State Area | High-Risk Existing Gaps |
|---|---|
| Work attention | Pattern Registry inventory/schema; trigger explanation; workflow-aware reporting |
| Action availability | permission table details; temporary authority/offline revocation; shared-device actor scope |
| Local/sync | sync delivery mechanics; local data lifecycle; sensitive local lifecycle |
| Freshness | reporting freshness metadata; reporting/aggregation; projection performance/caching |
| Review/decision | Pattern Registry inventory/schema; source-chain traversal limits; temporary reviewer authority |
| Exception/resolution | general flag semantics; domain conflict automation; malformed-event boundaries |
| Operational targets | alias-cycle behavior; duplicate resolution; subject-based scope; auditor access |
| Transfer/custody | cross-level distribution visibility; domain conflict automation; transfer reporting |
| Setup/rollout | configuration authoring UX; deploy-time validator UX; migration tooling; Pattern Registry schema |
| Evidence/history | structured import/export; retention/archive; auditor access; sensitive local data lifecycle |

## Product Debt Filter

An interaction state is not ready for platform-spec section drafting if it:

- implies a stored canonical state that the baseline does not define
- requires new event-envelope fields
- requires user roles, groups, IdP claims, tenant/deployment, or device identity to grant authority
- hides stale/offline uncertainty where decisions depend on it
- closes general flag/conflict semantics by naming a UI state
- collapses subject lineage, process identity, transfer lifecycle, and reporting identity into one target state model
- treats setup states as deployer-authored platform logic
- treats export/evidence state as canonical truth
- uses architecture-only vocabulary for ordinary user-facing states

Such pressure should route to `08-ux-gap-routing.md`, the professional-baseline gap register, or change control.

## Session 7 Output

Later product artifacts should use this state model as follows:

- `08-ux-gap-routing.md` should route state families and high-risk states to existing gaps or proposed clarifications.
- `09-first-vertical-slice.md` should choose a slice that exercises work attention, action availability, local/sync, freshness, review, evidence, and at least one exception state.
- `10-platform-spec-readiness-from-product.md` should verify that platform-spec section drafting candidates do not depend on visible states as hidden storage/workflow/permission decisions.
- `11-alignment-closeout.md` should confirm this state model remains an operational-surface contract and not a platform-core state schema.
