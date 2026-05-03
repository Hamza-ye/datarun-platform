# System Boundary Map

Status: Draft boundary routing map from ADR-001 through ADR-005 baseline

This document maps the current platform baseline into engineering boundaries. It is not a new source of platform behavior and does not replace `04-architecture-baseline-v0.md` or `05-decision-gap-register.md`.

Its purpose is to prevent later platform-spec artifacts from becoming detached prose. Every future spec section, gap closure, implementation design, or ADR-006+ assessment should route through one primary boundary.

## Source Basis

Primary baseline inputs:

- `../10-adr1-5-rest-state-closure-register.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`

Accepted validation overlays:

- `08-baseline-acceptance-check.md`
- `09-identity-boundary-control.md`

Lineage context only:

- `../../viability-assessment.md`
- `../00-extraction-state.md` Iteration 9

The viability assessment identified early primitive groupings and architecture pressures, but Iteration 9 preserved them as narrowing context, not final authority. The boundaries below are therefore derived from ADR-001 through ADR-005 closure, with viability used only to check that the original pressure areas still have a place to land. The acceptance check and identity boundary control overlay constrain this map before it is used for later-source assessment or platform-spec atomization.

## Boundary Rules

- Every settled mechanism belongs to one primary boundary.
- A mechanism may cross boundaries only through named contracts, references, projections, configuration packages, or events.
- Every unresolved gap has one primary owning boundary, even if other boundaries are affected.
- ADR-006-R through ADR-009 claims are not absorbed here. Later assessment must classify them against these boundaries.
- Boundary names are engineering routing surfaces, not implementation modules.

## Boundary: Event Log / Storage

Owns:

- append-only event log source of truth
- immutable operational facts
- write-path source-of-truth discipline
- projection rebuild from events
- derived read-model permission only where the event log remains gap-free

Does not own:

- identity merge/split semantics
- assignment-derived authorization
- deployer configuration language
- workflow state-machine semantics
- reporting product semantics

Inputs:

- typed events accepted by the event store
- client-generated event identifiers
- event envelope fields required by the baseline

Outputs:

- immutable event stream
- event subsets available for projection rebuild
- materialized/read models as derived artifacts

Crosses boundary through:

- event envelope contract
- projection rebuild contract
- sync event delivery contract

Settled mechanisms:

- immutable event log is canonical source of truth
- state changes enter through the event store
- projections and views are recomputed from events
- snapshots and materialized views are derived, not primary

Open / deferred:

- projection optimization and caching
- event schema/versioning tooling
- projection merge strategy across schema versions
- central retention/archive policy where it affects immutable history

Forbidden coupling:

- mutable canonical records plus separate audit log
- snapshot-primary or action-log-primary source of truth
- direct canonical projection patching
- deletion, redaction, or mutation of canonical events without formal baseline reconsideration

## Boundary: Event Envelope / Schema

Owns:

- stable event envelope obligations
- event identity/type/payload/timestamp expression
- ADR-002 causal/device/typed-reference requirements
- ADR-004 `shape_ref`, optional `activity_ref`, structural type vocabulary, and system actor convention
- device-time advisory semantics

Does not own:

- field-level deployer sensitivity
- stored immutable authority context
- workflow state storage
- dynamic deployer-created envelope references

Inputs:

- event payloads
- actor, subject, record, shape, activity, device, and causal references
- device clock timestamp for display/audit

Outputs:

- stable event envelope contract
- schema/versioning obligations for later specification

Crosses boundary through:

- identity/reference contract
- configuration-owned shape/activity references
- sync ordering/concurrency metadata

Settled mechanisms:

- every event envelope expresses identity, type, payload, and timestamp
- `device_time` is advisory for display and audit only
- `device_sequence` handles intra-device ordering
- `sync_watermark` supports cross-device concurrency detection
- device identity namespace is hardware/app-installation-bound rather than actor-bound
- ADR-005 adds no envelope field and no structural event type

Open / deferred:

- exact event schema/versioning tooling
- projection compatibility across schema versions
- formal envelope serialization details

Forbidden coupling:

- structural ordering by `device_time`
- stored immutable `authority_context`
- `status_changed`, `current_state`, or `pattern_ref` as structural envelope additions
- deployer policy fields becoming envelope fields

## Boundary: Identity / Lineage

Owns:

- subject identity continuity for referents with subject-lineage semantics
- subject alias/projection identity evolution
- corrective split behavior
- lineage acyclicity
- raw-reference preservation for conflict and authorization checks
- subject-lineage facts needed by conflict and audit consumers

Does not own:

- actor provisioning or authentication
- actor assignment or access scope
- device identity as actor identity
- assignment validity, role, scope, or authority
- process state machines for shipments, campaigns, cases, reviews, or transfer chains
- pending-match workflows
- conflict resolution lifecycle
- general flag semantics
- deployer-configured domain matching policy
- workflow state progression
- deployer shape ownership
- reporting projections as source of identity truth

Inputs:

- subject references written into events
- merge/split/corrective split operations
- raw historical references
- typed reference values from the event envelope

Outputs:

- subject identity projection
- alias lineage
- original and resolved subject-reference facts
- lineage state such as merged, split, archived, or stale-reference status
- conflict inputs for duplicate or stale identity cases, without owning domain-specific matching policy

Crosses boundary through:

- event original-reference contract
- typed reference contract
- authorization original-subject check
- conflict detection before downstream action
- read-only lineage query/projection contract

Settled mechanisms:

- events, subjects, and records use client-generated UUIDs for offline creation
- historical event references are not rewritten after identity evolution
- lineage remains acyclic
- merge/split operations are online-only where the baseline requires validation
- raw references are preserved before alias projection for conflict and authorization checks

Open / deferred:

- secondary input to shared-device actor scope where device identity and actor identity interact operationally
- alias-cycle enforcement, read-side behavior, and resolution semantics
- user-facing identity resolution UX
- duplicate-detection policy beyond platform-fixed subject-lineage facts

Forbidden coupling:

- server-allocated identifiers for offline event/subject/record creation
- rewriting historical event references to express identity evolution
- using post-merge alias projection as the authorization target for historical events
- treating actor, assignment, or process references as subject-lineage ownership
- making shipment, campaign, case, review, or transfer matching a core subject-lineage feature
- making identity own general flag or conflict-resolution lifecycle

## Boundary: Assignment / Authority / Sync

Owns:

- assignment-derived access
- sync scope as access scope
- immutable event sync
- authority reconstruction as projection
- original-subject authorization checks
- scope-change local data strategy baseline

Does not own:

- domain workflow states
- deployer-authored access-control programs
- general reporting entitlement policy beyond access constraints
- field-level sensitivity

Inputs:

- actor references
- assignment timeline
- original subject/process references
- event creation context
- sync knowledge state

Outputs:

- access-scoped event delivery
- authorization projection
- stale/invalid authorization flags or surfaced issues
- local data retain/remove candidates under scope changes

Crosses boundary through:

- immutable event sync contract
- assignment reference contract
- flag/conflict surfacing contract
- local lifecycle policy handoff

Settled mechanisms:

- sync unit is the immutable event
- sync is idempotent, append-only, order-independent, and scope-filtered
- access is assignment-derived
- sync scope is access scope
- authority is reconstructed rather than stored in event envelopes
- authorization checks use the original subject reference written into the event
- scope expansion is additive
- scope contraction uses selective retain as ADR-003's initial strategy

Open / deferred:

- subject-based scope
- auditor access
- shared-device actor scope
- assessment visibility
- grace-period policy
- permission table details
- cross-level distribution visibility
- sync pagination, priority, bandwidth handling, transport details, and operational delivery mechanics

Forbidden coupling:

- stored immutable `authority_context`
- deployer-authored arbitrary access-control logic
- treating post-merge identity projection as the authorization target for historical events
- retaining sensitive local data by hide-only behavior after scope contraction

## Boundary: Configuration

Owns:

- platform/deployer responsibility boundary
- structural event type vocabulary ownership
- deployer-configured shapes and activities
- roles, schedules, thresholds, severities, sensitivity parameters, and bounded policy choices
- expression limits, trigger limits, atomic config packages, trigger DAG depth, and complexity budgets

Does not own:

- platform structural type vocabulary
- arbitrary access-control programs
- field-level sensitivity
- general-purpose workflow/rules programming
- event-log source-of-truth semantics

Inputs:

- deployer configuration packages
- platform-owned vocabulary and mechanism definitions
- bounded expression and trigger definitions

Outputs:

- shape/activity definitions
- deployer policy values over platform-owned mechanisms
- validation constraints for authoring/deployment

Crosses boundary through:

- `shape_ref` and optional `activity_ref`
- atomic configuration package contract
- bounded expression evaluation contract
- server-only trigger contract where required

Settled mechanisms:

- platform owns structural event types and processing semantics
- deployers configure bounded surfaces
- ADR-004 fixes six structural event types
- per-deployment flag severity overrides are policy values over platform-owned flags
- domain uniqueness constraints are evaluated optimistically on device and authoritatively on server
- scope composition uses platform-fixed scope types
- shape/activity-level sensitivity classification is configurable policy

Open / deferred:

- configuration authoring format
- deployment packaging UX
- deploy-time validator UX
- migration tooling for breaking changes
- sensitive-subject policy beyond shape/activity-level sensitivity classification

Forbidden coupling:

- configuration becoming a general-purpose programming language
- deployer-authored arbitrary access-control logic
- field-level sensitivity
- deployer-owned structural event type semantics
- deployer policy adding event envelope fields

## Boundary: Projection / Workflow State

Owns:

- projection-derived workflow state
- state-machine evaluation from event sequences and pattern definitions
- Pattern Registry as platform-owned workflow primitive
- invalid-transition handling under ADR-005
- unresolved flagged-event effect on workflow state-machine evaluation
- process identity and pending-match routing where owned by workflow/process patterns

Does not own:

- event-log canonical truth
- subject lineage lifecycle
- general flag semantics outside ADR-005 workflow interactions
- deployer structural event vocabulary
- reporting aggregation semantics

Inputs:

- immutable event sequences
- pattern definitions
- process references attached to events
- unresolved-reference facts from identity/lineage projections
- source flags and resolution state
- bounded `context.*` form-expression values

Outputs:

- derived workflow state
- process-pattern projections
- pending-match workflow state where later specification includes it
- timeline visibility behavior for flagged events
- workflow-specific transition violation flags
- projection state after accepted resolution

Crosses boundary through:

- Pattern Registry contract
- source-only flag lineage contract
- bounded context-expression contract
- projection rebuild contract

Settled mechanisms:

- workflow state is derived, not stored as canonical event state
- invalid workflow transitions are accepted and flagged rather than rejected
- unresolved flagged events remain visible in timeline
- unresolved flagged events are excluded from workflow state-machine evaluation until accepted resolution re-derives state
- `context.*` is form-only and limited to seven platform-fixed pre-resolved values named by ADR-005

Open / deferred:

- exact Pattern Registry inventory
- pattern skeletons
- formal pattern schema format
- process-identity and pending-match specification where required by workflow/process patterns
- workflow-aware reporting and aggregation
- projection performance/caching details

Forbidden coupling:

- `status_changed` as a structural event type
- `current_state` as canonical event state
- `pattern_ref` as an event-envelope structural reference
- process lifecycle or pending match stored as subject-lineage identity state
- trigger expressions gaining `context.*`
- dynamic joins, arbitrary projections, aggregates, functions, or live updates through `context.*`

## Boundary: Flag / Resolution

Owns:

- ADR-005 workflow-specific flag lineage
- source-only flagging
- source-chain traversal
- workflow flag resolvability classification
- L3b auto-resolution boundary for eligible workflow cases
- auto-resolution actor attribution
- surfaced conflict/resolution lifecycle routing where later closed or explicitly assessed

Does not own:

- identity conflict detection itself
- authorization checks themselves
- subject lineage facts
- general ADR-006+ flag semantics
- all future flag category creation
- reporting UX for every flag class

Inputs:

- source events or source-chain references
- detector results from identity, authorization, or workflow boundaries
- workflow transition validation result
- resolution events
- auto-resolution policy identity

Outputs:

- root/source flags
- conflict/resolution lifecycle projections for surfaced issues
- projected contamination/derived flag effects
- accepted resolution effects
- `system:auto_resolution/{policy_id}` attribution for eligible auto-resolution events

Crosses boundary through:

- detect-before-act contract from conflict/authorization boundaries
- raw-reference and lineage-fact contract from identity boundaries
- projection workflow-state exclusion contract
- resolution event contract

Settled mechanisms:

- ADR-005 closes workflow `transition_violation`
- source-only flagging stores root/source flag and projects downstream effects
- source-chain traversal is required for ADR-005 workflow cases
- eligible L3b workflow cases may auto-resolve
- auto-resolution actor reference uses `system:auto_resolution/{policy_id}`

Open / deferred:

- general flag semantics
- `cycle_violation` flag-category acceptance if alias-cycle semantics are formally closed
- source-chain traversal depth limits
- auto-resolution authoring UX
- auto-resolution monitoring/reporting surface
- domain conflict automation outside ADR-005 workflow cases

Forbidden coupling:

- storing every downstream projected flag as a canonical source flag
- merging ADR-005 workflow flag behavior into general flag semantics without later assessment
- making identity, authorization, or workflow projection own the flag lifecycle for surfaced issues
- rejecting invalid workflow transitions instead of accepting and flagging
- treating ADR-006+ as automatic authority over ADR-001 through ADR-005 flag-adjacent decisions

## Boundary: Trigger / Reactivity

Owns:

- event-triggered action boundary where later specification closes it
- sync-visible eventual trigger timing
- server-only trigger constraints where required
- bounded trigger expression and side-effect limits

Does not own:

- arbitrary rules-engine behavior
- real-time guarantees under offline operation
- canonical workflow state storage
- deployer-authored platform code

Inputs:

- synced events
- configuration-defined trigger declarations
- server knowledge state where required

Outputs:

- bounded generated actions where allowed
- system-authored events with platform-defined actor convention where closed
- visible eventual-consistency timing metadata where later spec requires it

Crosses boundary through:

- configuration trigger package contract
- system actor convention
- event-store write contract

Settled mechanisms:

- configuration must remain bounded by ADR-004 expression and trigger ceilings
- server-only triggers are required where the baseline demands server knowledge
- system actor references use platform-owned conventions

Open / deferred:

- exact event-triggered action capability
- trigger execution semantics beyond ADR-004 constraints
- notification/task-creation boundary if later platform spec includes it
- relationship to workflow patterns and reporting

Forbidden coupling:

- unbounded rules engine
- trigger expressions gaining arbitrary projection access
- trigger side effects bypassing event-store write discipline
- treating offline capture as real-time server-visible trigger input

## Boundary: Reporting / Aggregation

Owns:

- reporting and aggregation capability once specified
- workflow-aware reporting surface once specified
- freshness/consistency presentation where reporting depends on projections
- access-constrained aggregate views

Does not own:

- canonical operational truth
- authorization rules
- workflow state-machine semantics
- data retention law/policy

Inputs:

- derived projections
- access-scoped event subsets or aggregates
- workflow state outputs
- flag/resolution state outputs

Outputs:

- reporting projections
- aggregate views
- decision-maker visibility surfaces

Crosses boundary through:

- projection contract
- access/sync scope contract
- freshness metadata contract where later specification closes it

Settled mechanisms:

- reporting must preserve event-log source of truth
- reporting must preserve projection derivation
- reporting must respect access-scope constraints

Open / deferred:

- reporting aggregation
- workflow-aware reporting and aggregation
- decision-maker reporting requirements
- aggregation freshness/consistency surface

Forbidden coupling:

- reporting projections becoming canonical operational state
- aggregate access bypassing assignment/sync-scope constraints
- reporting requirements redefining workflow state semantics silently

## Boundary: Local Data Lifecycle

Owns:

- local retain/remove strategy under scope changes
- local archive/summarization policy where later closed
- device storage pressure response under baseline constraints
- sensitive local data lifecycle requirements where later closed

Does not own:

- central immutable event history
- access assignment semantics
- event schema identity
- reporting product semantics

Inputs:

- scope expansion/contraction events or assignments
- local event subsets
- sensitivity classification
- retention/archive policy once defined

Outputs:

- retained local event subsets
- device-side removal candidates
- local archive/summarized artifacts where allowed
- lifecycle policy obligations for implementation

Crosses boundary through:

- authorization/sync scope contract
- sensitivity policy from configuration boundary
- storage immutable-history constraint

Settled mechanisms:

- scope expansion is additive
- scope contraction uses selective retain as the ADR-003 initial strategy
- own events are retained
- other actors' events about out-of-scope subjects are candidates for device-side removal
- sensitive deployments require stronger lifecycle handling than retain-and-hide

Open / deferred:

- retention and archival policy
- local archive/summarization mechanics
- low-end device storage/performance strategy
- concrete sensitive-data purge/lifecycle rules

Forbidden coupling:

- local lifecycle mutating central canonical events
- retain-and-hide as sufficient sensitive-data handling
- local purge behavior breaking auditability without formal decision

## Gap Ownership Routing

Each known gap has one primary owning boundary. Secondary boundaries may be affected, but they do not own closure unless change control reassigns ownership.

| Gap | Primary Boundary | Secondary Boundaries | Closure Path |
|---|---|---|---|
| Domain conflict automation outside workflow | Flag / Resolution | Projection / Workflow State; Configuration | Formal architecture decision or later-source assessment |
| Subject-based scope and auditor access | Assignment / Authority / Sync | Reporting / Aggregation; Local Data Lifecycle | Formal architecture decision or operational policy |
| Shared device actor scope | Assignment / Authority / Sync | Identity / Lineage; Event Envelope / Schema | Formal architecture decision or implementation/tooling design |
| Alias-cycle enforcement and resolution semantics | Identity / Lineage | Flag / Resolution; Projection / Workflow State; Event Log / Storage | Formal architecture decision before identity/flag atomization if in scope |
| Assessment visibility | Assignment / Authority / Sync | Reporting / Aggregation | Formal architecture decision or operational policy |
| Sensitive-subject policy beyond shape/activity sensitivity | Configuration | Assignment / Authority / Sync; Local Data Lifecycle | Operational policy; formal decision if access or lifecycle semantics change |
| Grace-period policy | Assignment / Authority / Sync | Flag / Resolution; Projection / Workflow State | Formal architecture decision or operational policy |
| Permission table details | Assignment / Authority / Sync | Configuration | Platform-spec detailing or implementation/tooling design |
| Cross-level distribution visibility | Assignment / Authority / Sync | Reporting / Aggregation | Formal architecture decision or operational policy |
| Exact Pattern Registry inventory | Projection / Workflow State | Configuration; Flag / Resolution | Platform-spec detailing |
| Formal Pattern Registry schema format | Projection / Workflow State | Configuration | Platform-spec detailing plus implementation/tooling design |
| Source-chain traversal limits | Flag / Resolution | Projection / Workflow State | Platform-spec detailing plus implementation/tooling design |
| Bounded context expression details | Projection / Workflow State | Configuration | Platform-spec detailing plus implementation/tooling design |
| Projection performance and caching | Event Log / Storage | Projection / Workflow State | Implementation/tooling design |
| Event schema and versioning tooling | Event Envelope / Schema | Event Log / Storage; Configuration | Implementation/tooling design plus platform-spec detailing |
| Structured import/export compatibility | Event Envelope / Schema | Event Log / Storage; Configuration; Reporting / Aggregation | Platform-spec detailing plus implementation/tooling design; formal decision only if canonical semantics change |
| Configuration authoring and deployment UX | Configuration | Trigger / Reactivity | Implementation/tooling design |
| Auto-resolution authoring and monitoring | Flag / Resolution | Reporting / Aggregation | Implementation/tooling design plus platform-spec detailing if audit surfaces are needed |
| Sync delivery mechanics | Assignment / Authority / Sync | Local Data Lifecycle | Implementation/tooling design |
| Retention and archival | Local Data Lifecycle | Event Log / Storage; Reporting / Aggregation | Operational policy, formal decision if canonical history changes |
| Setup experience and onboarding | Configuration | Assignment / Authority / Sync | Operational policy plus implementation/tooling design |
| Onboarding and role transition details | Assignment / Authority / Sync | Configuration; Local Data Lifecycle | Operational policy plus implementation/tooling design |
| Reporting and aggregation | Reporting / Aggregation | Assignment / Authority / Sync; Projection / Workflow State | Platform-spec detailing, implementation/tooling design, operational policy |
| Domain-agnostic proof gap | Configuration | Projection / Workflow State; Reporting / Aggregation | Product validation; formal decision only if new primitives are required |
| Low-end device scale risk | Local Data Lifecycle | Event Log / Storage; Projection / Workflow State | Implementation/tooling design; formal decision if lifecycle changes canonical constraints |
| ADR-006-R through ADR-009 assessment | Flag / Resolution | All touched boundaries | Later-source assessment |
| General flag semantics | Flag / Resolution | Identity / Lineage; Assignment / Authority / Sync; Projection / Workflow State; Reporting / Aggregation | Later-source assessment; formal decision for baseline changes |

## Post-ADR Assessment Routing

ADR-006-R through ADR-009 must be routed through this map after baseline acceptance and gap triage. A later claim must be classified as one of:

- consistent elaboration of a settled boundary
- valid closure candidate for a named gap owned by that boundary
- deferred implementation/specification detail
- new unauthorized claim outside an owned gap
- conflict with a closed boundary
- valid dispute requiring formal reopen

Assessment must not silently move ownership between boundaries. If a later claim crosses boundaries, the assessment must name:

- primary affected boundary
- secondary affected boundaries
- whether the claim changes a settled mechanism
- whether the claim closes a named gap
- whether formal change control is required

## Closure Checklist

This map is ready to govern platform-spec skeleton work when:

- every ADR-001 through ADR-005 settled mechanism has an owning boundary
- every known gap has one primary owning boundary
- every boundary lists forbidden coupling
- flag interactions remain separated from identity conflict detection, authorization checking, workflow projection, and general ADR-006+ flag semantics
- configuration cannot become deployer-authored platform logic
- reporting, triggers, and local lifecycle cannot redefine event-log source-of-truth or authority semantics
- ADR-006-R through ADR-009 claims are routed through classification rather than absorbed as authority
