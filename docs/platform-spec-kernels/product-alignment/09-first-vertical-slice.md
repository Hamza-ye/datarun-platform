# First Vertical Slice

Status: Session 9 product-alignment artifact

This document selects the first product-backed vertical slice for atomization and implementation progression. It is a slice-selection gate, not a delivery plan, not a backlog, and not an architecture decision.

The goal is to choose the smallest slice that proves product behavior, scenario pressure, and baseline boundary ownership compose under offline conditions without quietly closing unresolved gaps.

## Source Basis

Primary inputs:

- `01-phase-1-scenario-boundary-map.md`
- `02-product-experience-principles.md`
- `03-user-roles-and-operational-contexts.md`
- `04-core-operational-journeys.md`
- `05-information-architecture.md`
- `06-product-vocabulary-alignment.md`
- `07-interaction-state-model.md`
- `08-ux-gap-routing.md`

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

The first slice may prove composition. It may not close open gaps by assumption.

Do not infer:

- the selected slice is the full product scope
- the selected slice defines final UI layout
- a slice state is stored canonical state
- a slice queue is canonical work-item storage
- review implies full Pattern Registry inventory
- offline sync states define sync delivery mechanics
- simple assignment implies full permission-table closure
- target context implies full identity/lineage closure
- freshness-aware oversight implies full reporting/aggregation closure

If the first slice needs a blocked gap to work, either narrow the slice or explicitly make that gap the object of the next architecture/product decision. Do not hide it inside implementation.

## Slice Selection Criteria

A professional first slice for this platform should satisfy these criteria:

| Criterion | Reason |
|---|---|
| Proves composition | The platform risk is whether capture, assignment, offline, sync, review, evidence, and oversight work together |
| Keeps simple work simple | Scenario `00` remains the simplicity baseline |
| Exercises offline as normal | Offline assumptions have repeatedly broken easy designs; the slice must include offline pressure |
| Uses assignment-derived access only | Avoids closing permission-table, temporary authority, auditor, shared-device, or subject-scope gaps |
| Preserves immutable history | Proves current interpretation and evidence/history separation |
| Includes one controlled exception | Proves routing without generalizing flag/conflict semantics |
| Avoids broad identity resolution | Target context may exist, but merge/split/alias/duplicate behavior should not be required |
| Avoids generalized setup | Configuration exists, but setup-builder UX is not the first proof |
| Avoids broad reporting | Minimal freshness-aware oversight only |
| Avoids scenario silos | Slice should cross reusable surfaces rather than implement one scenario as a standalone feature |

## Candidate Slices

### Candidate A: Simple Offline Capture Only

Flow:

- field user opens capture
- records structured information offline
- sees saved locally / pending sync / synced
- history shows recorded fact

Strengths:

- strong simplicity baseline
- tests append-only capture and local/sync states
- avoids most open gaps

Weaknesses:

- does not prove assignment-derived work visibility
- does not prove review or decision trail
- does not prove returned correction
- does not prove cross-role product behavior
- may create false confidence because offline capture alone is too narrow

Decision:

Reject as first vertical slice. Keep as an inner path inside the selected slice.

### Candidate B: Configured Assigned Capture With Offline Submission And Supervisor Review

Flow:

- one configured activity and information shape exist
- one field actor has one simple assignment in one access context
- field actor sees assigned work
- field actor captures structured information while offline
- product shows saved locally / pending sync / synced
- submitted work becomes centrally visible after sync
- supervisor sees review work within the simple assignment/supervision context
- supervisor approves or returns with a reason
- returned work routes back to field actor
- field actor corrects/resubmits while preserving history
- evidence shows original record, correction, and review decision
- minimal oversight shows freshness-aware counts for this narrow loop

Strengths:

- composes capture, assignment, offline, sync, review, evidence, and oversight
- uses product vocabulary and IA surfaces from Sessions 5 and 6
- includes one controlled exception: returned work
- exercises local/central visibility without defining sync protocol
- proves history/current interpretation separation
- stays inside assignment-derived access without temporary authority, auditors, or shared devices

Weaknesses:

- needs a minimal workflow/review pattern, so Pattern Registry detail must be kept deliberately narrow
- risks implying a work-item primitive unless `Work` remains a surface/read-model concept
- risks implying reporting closure if oversight is overbuilt
- does not prove identity merge/split, transfer, multi-step approval, or broad setup

Decision:

Select as first vertical slice, with strict scope controls.

### Candidate C: Subject-Linked Capture And Target Profile

Flow:

- field user selects a subject/target
- records structured information
- target profile shows history
- possible inactive/duplicate target state appears

Strengths:

- tests target context and subject-linked history
- important for many Phase 1 scenarios

Weaknesses:

- quickly touches identity, subject-based scope, duplicate handling, pending match, and alias-cycle gaps
- offline target creation can create conflict pressure too early
- does not prove review or cross-role loop

Decision:

Defer as first slice. Allow only optional stable pre-existing target context inside Candidate B if needed, with no merge/split/duplicate/offline target creation.

### Candidate D: Setup / Configuration First

Flow:

- coordinator creates activity
- defines information shape
- assigns users
- publishes setup

Strengths:

- addresses a known weak area: setup and onboarding
- clarifies deployment/configuration UX

Weaknesses:

- pulls in configuration authoring, Pattern Registry schema, validator UX, package rollout, migration tooling, and permission details
- risks turning setup into programming before product operations are proven
- does not prove field/review/offline loop by itself

Decision:

Reject as first vertical slice. Candidate B assumes one preconfigured activity and shape; setup UX remains later.

### Candidate E: Transfer / Custody First

Flow:

- sender records transfer
- receiver acknowledges or disputes
- chain view shows in-transit/outstanding state

Strengths:

- valuable compositional pattern
- tests discrepancy and custody history

Weaknesses:

- pulls in transfer-specific workflow patterns, cross-level visibility, discrepancy handling, linked supply/field context, and possibly domain conflict automation
- less foundational than capture/assignment/review/offline

Decision:

Defer. Consider as a later slice after the selected slice proves the core loop.

### Candidate F: Oversight / Reporting First

Flow:

- supervisor/coordinator sees progress, missing, late, stale, and exception views

Strengths:

- important for operational value
- surfaces freshness and stale field-state pressure

Weaknesses:

- reporting/aggregation is open
- dashboards can become false canonical truth
- without field/review/evidence loop, oversight has no proven operational source

Decision:

Reject as first slice. Include only minimal freshness-aware oversight in Candidate B.

## Selected Slice

Selected slice:

```text
Configured assigned capture with offline submission and supervisor review
```

Short form:

```text
Assigned offline capture -> sync visibility -> supervisor review -> returned correction -> evidence/history -> minimal freshness-aware oversight
```

This slice is selected because it proves the smallest meaningful operational loop where product behavior and accepted architecture boundaries have to cooperate:

- field work starts from assignment-derived visibility
- offline capture is normal
- sync changes central visibility
- supervisor review creates auditable judgment
- returned work routes back without rewriting history
- correction preserves original record and current interpretation
- oversight reflects the loop without pretending to be live or canonical

## Scope

Included:

- one authenticated actor context for field user
- one authenticated actor context for supervisor
- one configured activity
- one information shape
- one simple assignment-derived work context
- one field capture flow
- local states: saved locally, pending sync, synced, sync problem where narrow
- supervisor review queue for submitted work
- decisions: approve and return with reason
- returned correction and resubmission
- evidence/history for original record, correction, and decision
- minimal freshness-aware oversight for the selected loop

Excluded:

- account schema
- IdP integration
- tenant/deployment authority
- user groups as authority
- shared devices
- temporary authority and revocation grace
- subject-based scope
- auditor access
- merge/split/alias-cycle behavior
- duplicate-resolution workflow
- offline target creation
- multi-step approval
- transfer/custody
- long-running case/follow-up beyond returned correction
- generalized Pattern Registry inventory/schema
- setup builder / configuration authoring UX
- reporting/aggregation beyond minimal narrow oversight
- general flag/conflict semantics
- domain conflict automation
- import/export
- retention/archive
- sensitive local lifecycle beyond baseline-safe non-sensitive assumptions

## Optional Target Context

The slice may include one stable pre-existing operational target only if needed to keep the product behavior realistic.

Allowed:

- select from a small scoped list
- show target label/context on capture, review, and evidence
- preserve record-to-target context

Not allowed in this slice:

- create target offline
- merge/split targets
- resolve duplicates
- alias-cycle behavior
- subject-based scope
- global target search
- auditor target access
- target lifecycle ownership claims

If target context creates identity pressure, remove it from the first slice rather than silently closing identity gaps.

## Offline Pressure Walkthrough

This slice must be tested through offline pressure, not just a connected happy path.

### Offline Field Capture

Expected behavior:

- field user sees assigned work from last-known scoped data
- field user captures using the local information shape
- product shows `Saved Locally`
- product shows `Pending Sync`
- product does not imply supervisor can see the work yet

Boundary ownership:

- Event Log / Storage owns immutable local event history
- Event Envelope / Schema owns structural validity
- Configuration owns the information shape reference
- Assignment / Authority / Sync owns scoped local visibility
- Local Data Lifecycle remains deferred except for baseline-safe local storage assumptions

Open gap avoided:

- sync delivery mechanics are not specified
- scope contraction behavior is not tested
- temporary authority/revocation is not included
- shared devices are not included

### Sync Visibility

Expected behavior:

- once synced, product shows work as centrally visible
- supervisor review queue may now show submitted work
- field user can distinguish synced from locally saved

Boundary ownership:

- Assignment / Authority / Sync owns immutable, idempotent, scope-filtered sync
- Projection / Workflow State owns review queue derivation
- Product translation layer owns visible `Synced` and `Waiting Review` labels

Open gap avoided:

- no transport/pagination/bandwidth guarantees
- no broad projection performance claims
- no real-time visibility promise

### Supervisor Review

Expected behavior:

- supervisor sees submitted work within simple assignment/supervision context
- supervisor can approve or return with a reason
- review decision is visible as evidence/history
- returned work routes back to field user

Boundary ownership:

- Assignment / Authority / Sync owns action availability
- Projection / Workflow State owns review-state interpretation under a minimal pattern
- Event Log / Storage owns decision history
- Product translation layer owns review surface labels

Open gap avoided:

- no multi-step approval
- no offline reviewer temporary authority
- no broad permission matrix
- no general Pattern Registry inventory

### Returned Correction

Expected behavior:

- field user sees returned work and reason
- field user corrects/resubmits
- product preserves original record, return decision, correction, and new current interpretation

Boundary ownership:

- Event Log / Storage owns append-only correction history
- Projection / Workflow State owns current interpretation
- Evidence surface organizes history without becoming source of truth

Open gap avoided:

- no general conflict automation
- no source-chain traversal beyond narrow review/correction evidence
- no mutation of original record

### Minimal Oversight

Expected behavior:

- supervisor/coordinator can see narrow counts such as assigned, submitted, returned, approved
- product shows freshness cues
- product does not present oversight as live or canonical

Boundary ownership:

- Reporting / Aggregation remains a gap
- Projection-derived views support narrow operational visibility
- Product translation layer owns freshness explanation

Open gap avoided:

- no broad reporting engine
- no cross-level aggregation
- no management dashboard
- no export

## Boundary Ownership Check

| Slice Concern | Owning Baseline Boundary | Agreement Status |
|---|---|---|
| local capture as immutable history | Event Log / Storage | Agrees |
| structural validity and shape reference | Event Envelope / Schema; Configuration | Agrees if no new envelope fields are added |
| assigned work visibility | Assignment / Authority / Sync | Agrees if simple assignment-derived scope only |
| offline local work | Event Log / Storage; Assignment / Authority / Sync | Agrees if no global knowledge is required |
| sync visibility | Assignment / Authority / Sync | Agrees if no protocol mechanics are specified |
| review queue/current interpretation | Projection / Workflow State | Agrees if minimal pattern detail is explicit and non-general |
| approve/return decision history | Event Log / Storage; Projection / Workflow State | Agrees |
| returned correction | Event Log / Storage; Projection / Workflow State | Agrees |
| evidence/history | Event Log / Storage; product translation layer | Agrees if evidence is not source of truth |
| minimal oversight | Projection-derived views; Reporting / Aggregation gap | Partially agrees; keep narrow and freshness-aware |
| exception handling | Flag / Resolution; Projection / Workflow State | Partially agrees; use returned work only, not general flag semantics |
| target context | Identity / Lineage only if subject-continuity semantics exist | Conditional; avoid identity-resolution behavior |

## Candidate Slice Verdict

The selected slice agrees with baseline boundary ownership only under these constraints:

- no new event-envelope fields
- no stored `authority_context`
- no canonical `WorkItem`
- no generalized Pattern Registry claim
- no general flag/conflict semantics
- no subject identity resolution
- no broad reporting/aggregation
- no sync delivery mechanics
- no setup builder
- no auditor/subject-based scope

If any of these constraints are violated during atomization, the slice is no longer safe and must route back to `08-ux-gap-routing.md` or change control.

## First Atomization Direction

The first atomization should be product-backed and boundary-aware:

1. Define the selected slice as a thin product spec.
2. Split platform atoms only where the slice needs them.
3. Keep each atom tied to one accepted boundary.
4. Route gaps explicitly instead of embedding assumptions.
5. Preserve product vocabulary layering from `06-product-vocabulary-alignment.md`.

Likely atomization surfaces after this slice:

- selected-slice product behavior
- event/history obligations for capture, decision, correction
- minimal assignment-derived work visibility
- local/sync interaction states
- minimal review pattern obligations
- evidence/history projection for the slice
- minimal freshness-aware oversight for the slice

Do not atomize broad platform areas yet:

- full authorization model
- full sync engine
- full Pattern Registry
- full reporting/aggregation
- full setup/configuration authoring
- identity merge/split/duplicate resolution
- transfer/custody
- audit/export/retention

## Session 9 Output

Later product artifacts should use this selected slice as follows:

- `10-atomization-readiness-from-product.md` should verify that this slice is sufficient to restart atomization narrowly and identify which atoms are safe now.
- `11-alignment-closeout.md` should record this slice as the product-backed input surface and remove temporary scratch references.
