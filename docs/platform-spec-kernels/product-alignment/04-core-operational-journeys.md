# Core Operational Journeys

Status: Session 4 product-alignment artifact

This document describes the recurring operational journeys the product must support across Phase 1 scenarios. It is not a wireframe, not an information architecture, not a workflow schema, and not an implementation plan.

The purpose is to make user-facing movement through the platform explicit before selected-slice atomization: how work appears, how users act on it, how review and oversight happen, how offline work remains understandable, and where unresolved pressure must route to gaps instead of becoming hidden architecture debt.

## Source Basis

Primary inputs:

- `../../README.md`
- `../../constraints.md`
- `../../access-control-scenario.md`
- `../../scenarios/README.md`
- `01-phase-1-scenario-boundary-map.md`
- `02-product-experience-principles.md`
- `03-user-roles-and-operational-contexts.md`

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

Journeys are product-facing movement patterns. They do not define canonical storage, event types, projection schemas, permission tables, sync protocol, or workflow pattern inventory.

Do not infer:

- a journey step is a stored workflow state
- a queue item is a canonical work-item record
- a dashboard value is canonical operational truth
- a product status label is an event-envelope value
- a journey actor is an architecture actor type
- a journey route grants authority
- a journey exception closes general flag/conflict semantics
- a journey needs a scenario-specific subsystem because one scenario uses it prominently

If a journey seems to require new authority semantics, special sync behavior, field-level sensitivity, broad identity ownership, deployer-authored logic, or new envelope fields, route that pressure through the gap register or change control.

## Journey Design Rule

Each journey should preserve three product promises:

1. Users can understand why they are seeing something and what they can do.
2. Users can distinguish current interpretation from recorded history.
3. Users can tell when offline, stale, scoped, or pending conditions matter.

Each journey should preserve three architecture constraints:

1. Events remain the source of truth; current views are derived.
2. Authority and sync scope remain assignment-derived under the accepted baseline.
3. Product composition must not collapse identity, workflow, reporting, configuration, conflict, and sync into one broad mechanism.

## Journey Set

Session 4 stabilizes these core journeys:

| Journey | Primary Role Lens | Primary Product Boundaries |
|---|---|---|
| J1. Find And Start Work | Field-level worker; supervisor | Responsibility; Queues; Offline/sync |
| J2. Capture Or Update Operational Information | Field-level worker | Capture; Subject continuity; Offline/sync |
| J3. Work With A Subject Or Operational Target | Field-level worker; supervisor; coordinator | Subject continuity; Capture; History |
| J4. Review, Return, Approve, Or Escalate | Supervisor; auditor | Review; Queues; Authority; History |
| J5. Configure And Publish Operational Setup | Coordinator / administrator | Configuration; Assignment; Offline/sync |
| J6. Monitor Progress, Gaps, And Freshness | Supervisor; coordinator | Oversight; Reporting; Queues; Freshness |
| J7. Transfer, Receive, Or Dispute Custody | Field-level worker; supervisor; coordinator | Transfer; Discrepancy; Oversight |
| J8. Follow Up A Long-Running Situation | Field-level worker; supervisor | Follow-up; Queues; Cross-reference |
| J9. Audit Or Inspect Operational Evidence | Auditor; coordinator; supervisor | Visibility; History; Reporting |
| J10. Sync, Reconcile, And Recover Trust | All roles, especially field and supervisor | Offline/sync; Flags; Local lifecycle |

These are not separate products. They are reusable movement patterns that later information architecture should compose into one coherent operational system.

## J1. Find And Start Work

Primary roles:

- Field-level worker
- Supervisor / team lead

User goal:

Find what needs attention now, understand why it is visible, and start the allowed action with minimal decision overhead.

Typical entry signals:

- assigned work
- due or overdue recurring work
- planned visit
- returned work
- triggered follow-up
- transfer awaiting acknowledgement
- review item waiting for judgment
- campaign or period work within scope

Normal path:

1. User opens a work surface such as personal work, team work, review work, or campaign work.
2. Product groups items by operational meaning: due, overdue, returned, waiting, triggered, assigned, pending sync, or blocked.
3. User sees why each item exists in plain operational terms.
4. Product shows whether the user can act, only view, wait for sync, or escalate.
5. User opens an item and starts the relevant journey: capture, review, transfer, follow-up, or inspection.

Experience requirements:

- Work must not appear as a raw projection or rule output.
- The product must answer "why is this here?" before users need to investigate.
- Read-only, blocked, stale, and offline-limited states must be understandable without exposing policy internals.
- Simple assigned work should require fewer decisions than oversight or review work.

Architecture guardrails:

- Do not create a canonical work-item store assumption.
- Do not make queue visibility a direct authority grant.
- Do not bypass assignment-derived access for convenience.
- Do not expose trigger internals as product explanation unless bounded and user-meaningful.

Open pressure to route:

- permission table details
- temporary authority and offline revocation reconciliation
- trigger explanation boundaries
- reporting freshness metadata

## J2. Capture Or Update Operational Information

Primary role:

- Field-level worker

Supporting roles:

- Supervisor / team lead
- Coordinator / administrator

User goal:

Record structured information quickly, with confidence that it is saved, understandable later, and connected to the right operational context.

Typical entry signals:

- user starts ad hoc capture within scope
- user starts assigned work
- user opens a planned visit
- user records receipt, discrepancy, or follow-up
- user corrects or amends previous work

Normal path:

1. User selects or opens the operational activity.
2. Product presents the expected information shape and any required subject/context.
3. User records information, using local validation where possible.
4. Product saves locally even when offline.
5. User sees local status: draft, saved locally, pending sync, synced, returned, or needs attention.
6. History remains available where correction, review, or audit matters.

Experience requirements:

- Basic capture must start quickly and stay focused.
- Shape/version complexity should appear only when it affects the user's action.
- Correction must feel like adding or amending history, not secretly overwriting it.
- Offline save must feel normal, with clear difference between local completion and central visibility.

Architecture guardrails:

- Do not imply mutable record overwrite.
- Do not expose event-envelope mechanics to ordinary users.
- Do not make old shape usage appear invalid just because central setup changed.
- Do not treat device identity as actor identity.

Open pressure to route:

- event schema and versioning tooling
- projection compatibility across shape versions
- local data lifecycle after scope contraction
- low-end device performance and sync delivery mechanics

## J3. Work With A Subject Or Operational Target

Primary roles:

- Field-level worker
- Supervisor / team lead
- Coordinator / administrator

User goal:

Find, identify, create, or understand a subject or operational target without needing to understand lineage mechanics.

Typical entry signals:

- capture requires a known subject, place, unit, resource, or situation
- user searches before recording work
- user discovers a new target while offline
- supervisor reviews history for a target
- coordinator manages registry stewardship where policy allows

Normal path:

1. User searches or scans within scoped data.
2. Product shows likely matches and enough context to choose safely.
3. If no match exists and policy allows, user creates a new local or central target.
4. Product connects captured work to the selected or created target.
5. Where relevant, product shows current profile, inactive/deactivated status, related history, and possible duplicate warnings.
6. Ambiguity routes to a resolution surface instead of blocking ordinary valid work unnecessarily.

Experience requirements:

- Users should experience a stable "thing profile" or target view.
- Duplicate, inactive, merged, split, or ambiguous targets should be progressively disclosed.
- History must remain meaningful even if the current target interpretation changes.
- Cross-flow links should add context without forcing all related work into one process.

Architecture guardrails:

- Do not treat every referenced thing as Identity / Lineage ownership.
- Do not collapse subject continuity, actor authority, campaign lifecycle, shipment/custody lifecycle, case progression, and reporting views into one identity subsystem.
- Do not close alias-cycle behavior or duplicate-resolution policy accidentally.
- Do not create global subject visibility outside access/sync scope.

Open pressure to route:

- alias-cycle enforcement and resolution semantics
- duplicate-detection and resolution UX
- subject-based scope
- cross-flow link visibility and access behavior
- process identity and pending-match UX

## J4. Review, Return, Approve, Or Escalate

Primary role:

- Supervisor / team lead

Supporting roles:

- Field-level worker
- Auditor / external reviewer
- Coordinator / administrator

User goal:

Judge submitted or waiting work, leave a clear decision trail, and return or escalate work when needed.

Typical entry signals:

- submitted work waiting review
- returned work reopened by a field worker
- approval step waiting for a decision
- planned assessment requiring supervisor judgment
- exception or discrepancy requiring review

Normal path:

1. Reviewer opens a review queue.
2. Product shows age, source, required judgment, freshness, and reason waiting.
3. Reviewer inspects the current interpretation and supporting history.
4. Reviewer decides: approve, return, reject, question, escalate, or defer where allowed.
5. Product records the decision as auditable work and routes the resulting next action.
6. Field worker or next reviewer sees what changed and what action is required.

Experience requirements:

- Review should feel like operational judgment, not raw state transition editing.
- Returned work must explain what needs correction.
- Multi-step approval should show where work sits without making every user understand the whole workflow model.
- Stale or offline-created decisions should surface clearly if they become problematic on sync.

Architecture guardrails:

- Do not turn review UX into a general workflow-authoring system.
- Do not make review status canonical stored state.
- Do not silently close general flag semantics through review labels.
- Do not let supervisor visibility become uncontrolled action authority.

Open pressure to route:

- exact Pattern Registry inventory and schema
- source-chain traversal limits
- general flag semantics beyond closed workflow behavior
- temporary authority if reviewers act offline
- permission table details

## J5. Configure And Publish Operational Setup

Primary role:

- Coordinator / administrator

Supporting roles:

- Supervisor / team lead
- Field-level worker

User goal:

Set up operational work through bounded platform mechanisms, preview impact, and publish changes deliberately.

Typical entry signals:

- new activity, reporting period, campaign, visit plan, or transfer chain
- information shape changes
- assignment or responsibility changes
- role transition or onboarding
- configured rhythm, threshold, severity, or policy value change

Normal path:

1. Coordinator selects an operational pattern or configured activity type.
2. Product asks for bounded setup values: information shape, rhythm, assignment, scope, review needs, transfer steps, or relevant policy choices.
3. Product validates setup before publishing.
4. Product previews expected operational impact, including affected users, active work, offline devices, and reporting surfaces where possible.
5. Coordinator publishes the configuration change.
6. Product shows rollout/freshness implications instead of implying instant global effect.

Experience requirements:

- Setup should feel like assembling known operational patterns, not programming.
- The product must show what is configurable versus what requires platform evolution.
- Publishing should be deliberate when it affects active or offline work.
- Field users should receive simple work, not leaked admin abstractions.

Architecture guardrails:

- Do not allow deployer-authored arbitrary access-control logic.
- Do not allow deployers to author structural event types or workflow state machines.
- Do not add envelope fields for deployment, tenant, admin, or setup convenience.
- Do not make groups or IdP claims direct authority sources.
- Do not turn Pattern Registry details into unresolved user-facing complexity.

Open pressure to route:

- exact Pattern Registry inventory
- formal Pattern Registry schema format
- configuration authoring and deployment UX
- setup experience and onboarding
- deployment packaging and self-host operations if needed
- permission table details

## J6. Monitor Progress, Gaps, And Freshness

Primary roles:

- Supervisor / team lead
- Coordinator / administrator

Supporting roles:

- Auditor / external reviewer

User goal:

Understand progress, missing work, bottlenecks, exceptions, and stale information across a scoped operational area.

Typical entry signals:

- daily supervision
- campaign monitoring
- reporting deadline
- review backlog
- transfer chain delay
- exception or discrepancy growth
- management or external reporting need

Normal path:

1. User opens a team, area, activity, campaign, review, or reporting surface.
2. Product shows progress, missing/late items, bottlenecks, and exceptions within scope.
3. Product shows freshness and completeness cues where stale field state affects interpretation.
4. User drills into relevant work, history, or exception detail.
5. User acts where authorized or routes the issue to the appropriate journey.

Experience requirements:

- Oversight must be useful without pretending to be live.
- Missing work must appear as a meaningful state, not merely absence.
- Aggregate views must preserve access to detail and history where decisions depend on them.
- Users should be able to separate "not done" from "done locally but not yet visible centrally" where the platform knows enough to say so.

Architecture guardrails:

- Do not make dashboard state canonical operational truth.
- Do not bypass access/sync scope for aggregate convenience.
- Do not imply reporting projections can ignore event-log derivation.
- Do not hide freshness when it affects decisions.

Open pressure to route:

- reporting and aggregation
- workflow-aware reporting
- reporting freshness metadata
- access-constrained aggregate views
- cross-level distribution visibility
- projection performance and caching

## J7. Transfer, Receive, Or Dispute Custody

Primary roles:

- Field-level worker
- Supervisor / team lead
- Coordinator / administrator

User goal:

Record movement between responsible parties, confirm receipt, and make discrepancies visible without rewriting transfer history.

Typical entry signals:

- resources or materials sent to another person, location, or level
- user receives expected items
- user receives partial, damaged, unexpected, or missing items
- coordinator monitors a multi-hop chain
- field execution depends on supply availability

Normal path:

1. Sender records what is being transferred and to whom.
2. Receiver sees expected receipt work when in scope.
3. Receiver confirms full receipt or records discrepancy.
4. Product shows in-transit, received, partial, disputed, or outstanding interpretation.
5. Supervisor or coordinator sees unresolved discrepancies and chain bottlenecks.
6. Related field work can reference supply context without merging into the same process.

Experience requirements:

- Single-hop transfer should be simple.
- Multi-hop chains need traceability without exposing internal workflow machinery.
- Disputes should be visible and resolvable while preserving what each party recorded.
- Product language should avoid domain-specific logistics assumptions in the core model.

Architecture guardrails:

- Do not treat transfer lifecycle as subject-lineage ownership.
- Do not make discrepancy resolution rewrite custody history.
- Do not close domain conflict automation beyond accepted workflow behavior.
- Do not make linked campaign or field work collapse into the transfer process.

Open pressure to route:

- cross-level distribution visibility
- domain conflict automation outside workflow
- reporting and aggregation over transfers
- exact workflow pattern inventory
- access behavior for linked supply and field work

## J8. Follow Up A Long-Running Situation

Primary roles:

- Field-level worker
- Supervisor / team lead

Supporting roles:

- Coordinator / administrator
- Auditor / external reviewer

User goal:

Keep an ongoing situation understandable across time, people, related activities, and changing conditions until it is resolved or no longer active.

Typical entry signals:

- active situation needs another interaction
- condition changes and creates follow-up
- supervisor returns or escalates work
- related activity provides new context
- situation is overdue, waiting, resolved, or reopened

Normal path:

1. User opens the active situation or follow-up item.
2. Product shows current interpretation, next expected action, owner/responsibility where relevant, and important history.
3. User records follow-up, changes status where allowed, links related context, or routes for review/escalation.
4. Product updates the current interpretation and preserves the timeline.
5. Product keeps future work visible until the situation is resolved or explicitly no longer active.

Experience requirements:

- Users need one coherent view of the situation without requiring domain-specific case terminology.
- Current state and history must both be visible.
- Triggered follow-up must explain the condition or decision that caused it.
- Related work should be visible as context without forcing all work into one lifecycle.

Architecture guardrails:

- Do not create a broad "case engine" that owns every long-running process.
- Do not close bounded trigger semantics accidentally.
- Do not make cross-flow links override access scope.
- Do not turn current situation status into canonical stored state.

Open pressure to route:

- bounded trigger semantics
- exact workflow patterns for long-running state
- cross-flow link visibility and access behavior
- reporting over active/resolved work
- general flag semantics

## J9. Audit Or Inspect Operational Evidence

Primary roles:

- Auditor / external reviewer
- Coordinator / administrator
- Supervisor / team lead

User goal:

Inspect scoped operational evidence, understand what happened and why, and verify process integrity without receiving uncontrolled operational authority.

Typical entry signals:

- planned audit window
- compliance review
- process integrity question
- dispute or discrepancy investigation
- management request for evidence
- export or reference need where policy allows

Normal path:

1. Reviewer opens an audit, inspection, or evidence surface within scope.
2. Product shows records, decisions, corrections, handoffs, freshness, and relevant history.
3. Reviewer sees why the evidence is visible to them.
4. Reviewer follows links to related operational context where authorized.
5. Reviewer records findings or references evidence where policy allows.

Experience requirements:

- Audit access must be explicit, scoped, and explainable.
- Audit views should present current interpretation and history together.
- Export/reference behavior must not make external files canonical truth.
- Sensitive information handling should be visible as policy/product behavior without inventing field-level sensitivity.

Architecture guardrails:

- Do not close auditor access semantics accidentally.
- Do not create broad cross-scope read paths as a UI shortcut.
- Do not bypass assignment-derived access and sync scope.
- Do not define field-level sensitivity without formally reopening the baseline.

Open pressure to route:

- subject-based scope and auditor access
- structured import/export compatibility
- retention and archival
- reporting and aggregation
- sensitive local data lifecycle

## J10. Sync, Reconcile, And Recover Trust

Primary roles:

- Field-level worker
- Supervisor / team lead
- Coordinator / administrator

Supporting roles:

- Auditor / external reviewer

User goal:

Continue work during disconnection, understand what has synced, and recover trust when stale or conflicting conditions appear.

Typical entry signals:

- user works offline
- connectivity returns
- local work waits to upload
- central assignments or configuration changed while device was offline
- duplicate, conflict, stale authority, or discrepancy appears after sync
- supervisor or coordinator sees stale or incomplete field state

Normal path:

1. Product lets scoped work continue offline where allowed.
2. User sees local status for saved, pending, synced, failed, stale, or needs-attention items.
3. On reconnect, product syncs according to accepted scope rules and later-defined delivery mechanics.
4. Product summarizes results in operational terms.
5. Valid but anomalous work is surfaced to the right action surface.
6. Users can understand what is complete locally, what is visible centrally, and what needs resolution.

Experience requirements:

- Offline must feel normal for field work.
- The product must not promise real-time visibility.
- Sync errors and reconciliation issues should be specific enough to act on, not generic failure messages.
- Supervisors and coordinators must see freshness cues before making decisions from stale views.

Architecture guardrails:

- Do not weaken immutable event sync or append-only behavior.
- Do not bypass scope filtering for sync convenience.
- Do not turn stale local authority into stored authority snapshots.
- Do not make malformed events acceptable as normal conflict items.
- Do not define local data deletion/retention beyond the accepted baseline without gap closure.

Open pressure to route:

- sync delivery mechanics
- local data lifecycle
- temporary authority and offline revocation reconciliation
- general flag semantics
- projection performance and caching
- shared-device actor scope

## Cross-Journey UX Requirements

The product must provide these behaviors across the journey set:

- explain why an item is visible, actionable, read-only, blocked, stale, overdue, returned, or missing
- distinguish local completion from central visibility
- preserve history behind current interpretations
- show freshness when stale information affects decisions
- route exceptions to proportionate action surfaces
- keep simple work simple while revealing complexity only at the point of need
- support deployer-specific labels without losing domain-agnostic platform structure
- avoid making internal architecture concepts required user knowledge

## Journey-To-Gap Matrix

| Journey | High-Risk Existing Gaps |
|---|---|
| J1. Find And Start Work | permission table details; temporary authority/offline revocation; trigger explanation boundaries; reporting freshness |
| J2. Capture Or Update Operational Information | event schema/versioning tooling; projection compatibility; local data lifecycle; sync delivery mechanics |
| J3. Work With A Subject Or Operational Target | alias-cycle behavior; duplicate resolution UX; subject-based scope; cross-flow link access; pending-match UX |
| J4. Review, Return, Approve, Or Escalate | Pattern Registry inventory/schema; source-chain traversal limits; general flag semantics; temporary reviewer authority |
| J5. Configure And Publish Operational Setup | Pattern Registry inventory/schema; configuration authoring/deployment UX; setup/onboarding; permission details |
| J6. Monitor Progress, Gaps, And Freshness | reporting/aggregation; workflow-aware reporting; freshness metadata; access-constrained aggregation; cross-level visibility |
| J7. Transfer, Receive, Or Dispute Custody | cross-level visibility; domain conflict automation; transfer reporting; workflow pattern inventory; linked-work access |
| J8. Follow Up A Long-Running Situation | bounded trigger semantics; long-running workflow patterns; cross-flow link access; active/resolved reporting; general flag semantics |
| J9. Audit Or Inspect Operational Evidence | subject-based scope/auditor access; structured import/export; retention/archive; sensitive local lifecycle |
| J10. Sync, Reconcile, And Recover Trust | sync delivery mechanics; local data lifecycle; temporary authority/offline revocation; general flag semantics; shared devices |

## Product Debt Filter

A journey detail is not stable enough for atomization if it:

- creates a new platform object only because one scenario needs a convenient screen label
- assumes a gap is closed without naming it
- requires a user role to carry authority outside assignment-derived access
- requires a group, IdP claim, tenant, deployment, or device to grant direct visibility or action authority
- makes projection or reporting state sound canonical
- hides freshness or offline uncertainty where a user decision depends on it
- makes field users understand architecture mechanisms before they can do basic work
- turns configuration into programming
- hard-codes domain vocabulary that should remain deployer-configured

Such pressure should route to `08-ux-gap-routing.md`, a focused clarification to `../professional-baseline/05-decision-gap-register.md`, or formal change control if it changes the accepted baseline.

## Session 4 Output

Later product artifacts should use these journeys as follows:

- `05-information-architecture.md` should organize work surfaces around these journeys without creating separate products for each scenario.
- `06-product-vocabulary-alignment.md` should map journey terms such as work, queue, item, subject, target, review, transfer, follow-up, exception, status, and progress to baseline-safe meanings.
- `07-interaction-state-model.md` should define the user-visible states that recur across these journeys.
- `08-ux-gap-routing.md` should route each open journey pressure to an existing gap or proposed clarification.
- `09-first-vertical-slice.md` should choose a slice that proves journey composition, especially capture, assignment, review, oversight freshness, and offline sync.
