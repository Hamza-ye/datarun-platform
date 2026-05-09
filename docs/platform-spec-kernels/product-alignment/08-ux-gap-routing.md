# UX Gap Routing

Status: Session 8 product-alignment artifact

This document routes product/UX pressure from Sessions 1 through 7 to the professional-baseline gap register, product clarifications, deferrals, or change-control triggers.

It is not a new gap register, not a backlog, not an implementation plan, and not an architecture decision. Its purpose is to prevent product alignment from quietly closing architecture gaps or creating hidden implementation assumptions before first-slice planning.

## Source Basis

Primary inputs:

- `01-phase-1-scenario-boundary-map.md`
- `02-product-experience-principles.md`
- `03-user-roles-and-operational-contexts.md`
- `04-core-operational-journeys.md`
- `05-information-architecture.md`
- `06-product-vocabulary-alignment.md`
- `07-interaction-state-model.md`

Architecture guardrails:

- `../professional-baseline/02-change-control.md`
- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../pre-operations/04-accepted-pre-atomization-decisions.md`

## Control Rule

UX gap routing may classify product pressure. It may not close baseline gaps.

Do not infer:

- a routed product pressure is accepted architecture behavior
- a product clarification changes the baseline
- a first-slice candidate may depend on a P1 unresolved gap as if it were closed
- an IA surface or visible state creates a platform primitive
- product language can bypass `../professional-baseline/02-change-control.md`

If the product needs behavior that changes assignment-derived access, sync scope, event-envelope closure, projection-derived state, Pattern Registry ownership, field-level sensitivity rejection, or deployer-authored logic rejection, that behavior requires formal change control.

## Routing Classes

| Route | Meaning | Allowed Next Step |
|---|---|---|
| Existing gap | Already present in `05-decision-gap-register.md` | Reference the gap and preserve its closure path |
| Product clarification | Product wording/UX rule can clarify without changing architecture | Add to product alignment or later product spec |
| Platform-spec detail | Boundary is closed; spec detail is still needed | Atomize only under accepted baseline constraints |
| Implementation/tooling design | UX/tooling/engineering mechanics needed | Design/prototype later without changing baseline |
| Operational policy | Deployment/product policy needed | Define policy; escalate only if it requires architecture change |
| Defer/no first-slice dependency | Keep visible but do not block first slice | Avoid depending on it in `09-first-vertical-slice.md` |
| Change-control trigger | Product pressure would alter baseline | Use professional-baseline change-control process |

## Routing Summary

| Pressure Area | Primary Route | First-Slice Implication |
|---|---|---|
| Permission details and action availability | Existing gap | Avoid broad permission matrix dependency |
| Temporary authority/offline revocation | Existing gap | Defer temporary grants unless explicitly scoped as policy/spec detail |
| Shared-device actor scope | Existing gap | Do not assume shared-device mode in first slice |
| Subject-based scope and auditor access | Existing gap | Do not make auditor access or subject-scope core to first slice |
| Alias-cycle and duplicate resolution | Existing gap + product clarification | Allow possible-duplicate UI only as non-closing pressure |
| Pattern Registry inventory/schema | Existing gap | First slice may use a minimal named pattern only if spec detail is explicit |
| General flag/conflict semantics | Existing gap | Use accepted workflow flag behavior or narrow exception language only |
| Domain conflict automation | Existing gap | Do not automate non-workflow conflict resolution in first slice |
| Reporting/aggregation/freshness | Existing gap + product clarification | Use freshness-aware simple oversight, not broad reporting |
| Sync delivery mechanics | Existing gap | Show local/pending/synced states without defining protocol |
| Local data lifecycle/sensitive lifecycle | Existing gap | Avoid sensitive lifecycle dependency in first slice |
| Configuration authoring/deployment UX | Existing gap | Keep setup minimal and bounded |
| Event schema/versioning tooling | Existing gap | Use stable shape/version language without tooling assumptions |
| Import/export and retention/archive | Existing gap | Do not include export/archive in first slice unless policy demands |
| Deployment/tenant/account/IdP | Existing gap/pre-op guardrail | Keep outside event envelope and authority shortcuts |

## Detailed Routing

### Authorization, Visibility, And Action Availability

Product pressure:

- users need to know why work is visible, actionable, read-only, blocked, stale, or missing
- supervisors need broader visibility without uncontrolled action authority
- auditors may need scoped evidence access
- offline devices may act under last-known authority

Existing gaps:

- Subject-Based Scope And Auditor Access
- Shared Device Actor Scope
- Temporary Authority And Offline Revocation Reconciliation
- permission table details in Architecture Baseline v0 open/deferred items

Product clarification:

- UI may show `Actionable`, `Read-Only`, `Not Available Here`, `Needs Online Action`, and `Stale Authority Warning` as product translation-layer states.
- These states explain affordances; they do not grant or revoke authority.
- Product language should explain context in operational terms instead of exposing raw policy machinery.

First-slice implication:

- First slice should use simple assignment-derived action availability.
- Do not include auditor access, subject-based scope, shared devices, or temporary grants unless deliberately chosen as the slice risk.

Change-control trigger:

- new envelope fields for authority
- stored immutable `authority_context`
- group, IdP claim, tenant, deployment, or device as direct authority source
- sync scope diverging from access scope

### Offline, Sync, And Local Status

Product pressure:

- field work needs to continue offline
- users need `Saved Locally`, `Pending Sync`, `Synced`, `Sync Problem`, and freshness cues
- supervisors need to know that central visibility may lag field reality
- scope contraction and sensitive local lifecycle affect trust

Existing gaps:

- Sync Delivery Mechanics
- Projection Performance And Caching
- local data lifecycle in Architecture Baseline v0 open/deferred items
- sensitive local lifecycle under Temporary Authority And Offline Revocation Reconciliation and Retention/Archival pressure
- Shared Device Actor Scope

Product clarification:

- Sync/local states are operational-surface explanations.
- They may distinguish local completion from central visibility.
- They should not define transport, pagination, priority, bandwidth handling, or retention/deletion rules.

First-slice implication:

- First slice should include visible local/pending/synced/freshness states.
- First slice should not require finalized bandwidth strategy, scope-contraction deletion policy, shared-device partitioning, or sensitive lifecycle handling.

Change-control trigger:

- accepting malformed events as normal conflict items
- bypassing scope filtering for sync convenience
- changing append-only/idempotent/order-independent sync invariants
- replacing selective-retain scope contraction without formal decision

### Work, Queues, And Pending Attention

Product pressure:

- users need a coherent `Work` surface
- work can be due, overdue, waiting, returned, triggered, escalated, blocked, missing, or pending local sync
- product needs a queue language without creating canonical work-item storage

Existing gaps:

- Exact Pattern Registry Inventory
- Formal Pattern Schema Format
- Reporting And Aggregation
- Bounded Context Expression Details if state-aware form behavior is included
- permission table details

Product clarification:

- `Work` is a surface/read-model concept.
- `Work item` may describe a visible queue item, but `WorkItem` is not accepted as a platform primitive.
- Queue states should explain why they are visible and where to go next.

First-slice implication:

- First slice may include a simple `Work` surface with assigned/due/returned/pending sync items.
- Avoid depending on a generalized work-item engine or all trigger/escalation patterns.

Change-control trigger:

- stored canonical work-item model that competes with event/projection baseline
- event-envelope fields added for work state
- arbitrary deployer-authored trigger/rule logic

### Review, Decisions, And Approval

Product pressure:

- users need review queues, return reasons, approval progress, decision trails, and aging
- multi-step approval should be understandable without exposing workflow machinery
- invalid/stale decisions may need surfacing

Existing gaps:

- Exact Pattern Registry Inventory
- Formal Pattern Schema Format
- Source-Chain Traversal Limits
- Temporary Authority And Offline Revocation Reconciliation
- general flag semantics beyond closed ADR-005 workflow behavior

Product clarification:

- `Review`, `Decision`, `Returned`, `Waiting Review`, and `Step Waiting` are operational-surface labels.
- Decision history remains auditable product language over immutable event history.
- Review status is not canonical stored state.

First-slice implication:

- First slice should include one simple review/return decision path if review is included.
- Avoid multi-step approval and offline reviewer temporary authority unless intentionally selected.

Change-control trigger:

- review UX becomes workflow authoring
- review labels close general flag semantics
- reviewer visibility becomes uncontrolled action authority

### Exceptions, Flags, Conflicts, And Resolution

Product pressure:

- users need proportionate exception routing
- possible duplicates, stale authority, invalid transitions, transfer disputes, sync problems, and returned work all need attention
- product needs `Needs Attention` without merging every issue into one subsystem

Existing gaps:

- Domain Conflict Automation Outside Workflow
- Alias-Cycle Enforcement And Resolution Semantics
- Source-Chain Traversal Limits
- Auto-Resolution Authoring And Monitoring
- general flag semantics in Architecture Baseline v0 open/deferred items
- malformed-event boundaries are controlled by the accepted baseline

Product clarification:

- Use narrow product labels where possible: `Returned`, `Disputed`, `Possible Duplicate`, `Invalid Transition`, `Sync Problem`, `Stale Authority Issue`.
- `Exception` is a routed UX grouping, not one architecture subsystem.
- `Flag` remains restricted to accepted behavior or explicit architecture/spec language.

First-slice implication:

- First slice should include at most one narrow exception path, preferably returned review work or sync problem visibility.
- Avoid general conflict resolution automation.

Change-control trigger:

- making malformed events acceptable as conflicts
- last-write-wins or invisible automatic merge
- general domain conflict automation beyond ADR-005 workflow behavior
- treating all flagged records as inert outside accepted behavior

### Operational Targets, Subjects, And Related Context

Product pressure:

- users need lookup, target detail, related history, inactive/deactivated cues, possible duplicates, and created-locally states
- product needs `Operational Target` without collapsing all referents into Identity / Lineage
- cross-flow linking needs to add context without leaking inaccessible data

Existing gaps:

- Subject-Based Scope And Auditor Access
- Alias-Cycle Enforcement And Resolution Semantics
- duplicate-resolution UX under alias/subject pressure
- cross-flow link visibility and access behavior in product-alignment matrices
- process identity and pending-match UX remains unresolved pressure

Product clarification:

- `Operational Target` is a user orientation term, not identity ownership.
- `Subject` is narrow and applies only where continuity semantics exist.
- `Possible Duplicate`, `Ambiguous`, `Created Locally`, and `Pending Match` are product states that cannot close identity or access gaps.

First-slice implication:

- First slice may include scoped target lookup and record-to-target context.
- Avoid merge/split, alias-cycle handling, subject-based scope, auditor target access, and pending-match behavior unless chosen deliberately.

Change-control trigger:

- broad Identity / Lineage ownership of every referent
- global target search outside access/sync scope
- subject-based scope semantics accepted without formal route
- rewriting historical references after identity changes

### Transfer, Custody, And Discrepancy

Product pressure:

- users need sent, awaiting receipt, received, partial, disputed, outstanding, and chain-delayed interpretations
- multi-hop transfer needs to preserve traceability
- field work may need linked supply context

Existing gaps:

- Domain Conflict Automation Outside Workflow
- Reporting And Aggregation
- Exact Pattern Registry Inventory
- cross-level distribution visibility in Architecture Baseline v0 open/deferred items
- linked-work access behavior remains product pressure

Product clarification:

- Transfer states are derived operational interpretations.
- Disputes preserve sender and receiver history; they do not rewrite custody history.
- Transfer does not become subject-lineage ownership or a separate supply subsystem by default.

First-slice implication:

- Defer transfer/custody unless the first slice specifically targets distribution.
- If included, use a single-hop transfer with acknowledgement/discrepancy only.

Change-control trigger:

- automatic non-workflow discrepancy resolution
- transfer state as canonical stored state
- cross-level visibility outside assignment/sync constraints

### Oversight, Reporting, And Freshness

Product pressure:

- supervisors/coordinators need progress, missing/late work, bottlenecks, exceptions, and freshness
- dashboards need to show useful current interpretation without pretending to be live or canonical
- missing work can mean not done, not synced, stale, or unknown

Existing gaps:

- Reporting And Aggregation
- Projection Performance And Caching
- reporting freshness metadata in product-alignment matrices
- access-constrained aggregate views
- cross-level distribution visibility

Product clarification:

- `Oversight` is a freshness-aware view over derived data.
- `Missing`, `Incomplete`, `Unknown Completeness`, and `May Be Stale` should be distinct when decisions depend on it.
- Dashboards and reports are not canonical truth.

First-slice implication:

- First slice should include minimal freshness-aware oversight if supervisor review is included.
- Avoid broad reporting, cross-level aggregation, export, or management dashboards.

Change-control trigger:

- aggregate views bypass access/sync scope
- reporting projections become canonical state
- freshness is hidden where it affects operational decisions

### Setup, Configuration, And Rollout

Product pressure:

- coordinators need setup that feels like assembling operational patterns
- setup changes need validation, publish, rollout, impact warning, and older-setup-in-use states
- deployer labels cannot become platform primitives

Existing gaps:

- Configuration Authoring And Deployment UX
- Setup Experience And Onboarding
- Exact Pattern Registry Inventory
- Formal Pattern Schema Format
- Event Schema And Versioning Tooling
- deployment packaging UX, deploy-time validator UX, migration tooling

Product clarification:

- Setup is a bounded product experience over platform-owned mechanisms.
- Setup states are operational-surface labels, not core event history claims by themselves.
- `Requires Platform Evolution` should be visible when product need exceeds configuration.

First-slice implication:

- First slice should keep setup minimal: one activity, one information shape, simple assignment, simple review if needed.
- Avoid generalized setup builders, arbitrary workflow authoring, or full deployment packaging UX.

Change-control trigger:

- deployer-authored arbitrary access-control logic
- deployer-authored structural event types
- deployer-authored workflow state machines
- field-level sensitivity
- envelope fields added for setup/deployment convenience

### Evidence, Audit, Export, Retention

Product pressure:

- users need current interpretation plus history
- auditors may need scoped evidence
- exports and retention/archive appear in scenarios/constraints but are not first-order UX for basic field work

Existing gaps:

- Subject-Based Scope And Auditor Access
- Structured Import Export Compatibility
- Retention And Archival
- sensitive local data lifecycle
- field-level sensitivity remains rejected unless formally reopened

Product clarification:

- `Evidence` organizes traceability; it is not a second source of truth.
- Export is policy-bound and not canonical operational state.
- Audit access remains explicit gap pressure unless expressible through accepted assignment/scope mechanisms.

First-slice implication:

- First slice should include basic history/evidence for submitted/reviewed work.
- Avoid audit exports, retention/archive policy, and broad auditor access.

Change-control trigger:

- audit area becomes a privileged back door
- export becomes canonical truth
- retention policy requires mutation/deletion/redaction of canonical events
- field-level sensitivity is introduced

### Deployment, Tenant, Account, And IdP

Product pressure:

- product may need account/session/admin experience later
- deployments may be cloud or self-hosted
- earlier product pressure sometimes used tenant-like assumptions

Existing guardrails/gaps:

- `../pre-operations/04-accepted-pre-atomization-decisions.md`
- deployment packaging UX
- account/schema/IdP integration as future product need
- permission table details

Product clarification:

- Deployment/tenant/account/IdP language stays outside event-envelope and direct authority semantics.
- Authentication proves a principal may act as an actor; it does not define what the actor may see or do.

First-slice implication:

- First slice may assume an authenticated actor context is provided.
- Do not atomize account schema, IdP integration, tenant model, or group-managed authorization as part of product surface unless explicitly selected.

Change-control trigger:

- `tenant_id`, `deployment_id`, `user_id`, or group claims added to the event envelope
- tenant/deployment/group/IdP as direct visibility or action authority

## First-Slice Blockers

These gaps should block first-slice planning if the first slice depends on them:

| Gap | Blocks If First Slice Requires |
|---|---|
| Subject-Based Scope And Auditor Access | auditor surfaces, subject-scope visibility, cross-scope evidence |
| Shared Device Actor Scope | multiple actor sessions on one device |
| Temporary Authority And Offline Revocation Reconciliation | temporary grants, emergency cover, offline revocation/grace behavior |
| Exact Pattern Registry Inventory / Schema | generalized workflow pattern authoring or multiple pattern families |
| Domain Conflict Automation Outside Workflow | automated resolution of non-workflow disputes |
| Reporting And Aggregation | management reporting, broad aggregate dashboards, workflow-aware reporting |
| Sync Delivery Mechanics | transport/pagination/bandwidth guarantees beyond visible local/sync states |
| Configuration Authoring And Deployment UX | generalized setup builder, package publishing, validator UX |
| Retention And Archival | deletion/redaction/archive behavior |
| Structured Import Export Compatibility | external exchange/export obligations |

## Safe First-Slice Assumptions

These are acceptable assumptions for `09-first-vertical-slice.md` if kept narrow:

- one authenticated actor context exists
- one simple assignment-derived access context exists
- one configured activity exists
- one information shape exists
- field user can capture while offline
- local work can show saved locally / pending sync / synced
- submitted work can appear in a supervisor review queue
- reviewer can approve or return with a reason
- returned work can be corrected and resubmitted
- basic history/evidence can show original record, correction, and decision trail
- oversight can show minimal freshness-aware counts for the narrow slice
- one narrow exception can be shown as `Returned`, `Sync Problem`, or `Needs Attention` without closing general flag semantics

These assumptions should remain product-surface and platform-spec-detail inputs, not new architecture baseline claims.

## Proposed Clarifications To Carry Forward

These do not need immediate gap-register edits, but should be preserved in product alignment and checked during closeout:

- Product-visible state is product translation/read-model language, not platform-core state.
- `Work` is a surface concept; avoid accepting `WorkItem` as platform primitive during product alignment.
- `Operational Target` is a user orientation term; use `Subject` only for referents with subject-continuity semantics.
- `Exception` is a UX routing group; use narrower labels wherever possible.
- `Evidence` is traceability over event history and projections, not a second source of truth.
- Setup should expose `Requires Platform Evolution` when need exceeds bounded configuration.
- Freshness and local/central visibility should be visible where they affect decisions.
- Product specs and implementation tickets should not use IA surface names as service/storage boundaries.

## Change-Control Watchlist

During first-slice and atomization planning, stop and route through change control if any artifact proposes:

- new event-envelope fields
- stored immutable authority context
- mutable canonical records or current state
- direct authority from group, IdP claim, tenant, deployment, or device identity
- sync scope different from access scope
- global subject/work search outside access scope
- deployer-authored access-control logic
- deployer-authored structural event types
- deployer-authored workflow state machines
- field-level sensitivity
- automatic non-workflow conflict resolution
- last-write-wins or invisible merge for judgment conflicts
- dashboard/reporting state as canonical truth
- audit/export as bypass around assignment-derived access

## Session 8 Output

Later product artifacts should use this routing as follows:

- `09-first-vertical-slice.md` should choose a slice that avoids blockers unless the slice explicitly exists to resolve one.
- `10-atomization-readiness-from-product.md` should verify that every atomization candidate is either safe under this routing or has an explicit gap/change-control path.
- `11-alignment-closeout.md` should confirm that product alignment did not quietly close professional-baseline gaps.
