# Platform Specification Outline

Status: Draft outline from accepted professional baseline

This document defines the top-level structure for the normative platform specification. It is not the platform specification itself, not an implementation design, and not a new governance layer.

The outline exists to route future platform-spec sections through accepted architecture responsibility areas before detailed spec text is accepted.

## Source Basis

Primary inputs:

- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`

Assessed inputs and guardrails:

- `10-adr006r-flag-semantics-assessment.md`
- `11-adr007-envelope-type-assessment.md`
- `12-adr008-reference-fields-assessment.md`
- `13-adr009-duality-rule-assessment.md`
- `14-pattern-inventory-walkthrough-assessment.md`
- `15-conflict-flag-offline-boundary-control.md`
- `16-operational-constraints-boundary-control.md`
- `17-authorization-visibility-boundary-control.md`
- `18-envelope-shape-parametrization-boundary-control.md`
- `19-envelope-shape-parametrization-definitions.md`

Not source authority for this outline:

- ADR prose directly, except through the accepted baseline and classified assessments.
- Existing `../platform-spec/atoms/` files. They are draft section input only.
- Product-alignment material, until separately assessed into this baseline path.
- Prior process labels or implementation-era contract IDs.

## Outline Rules

- Every normative behavior section must cite its accepted baseline source or a named gap in `05-decision-gap-register.md`.
- Every normative behavior section must name one primary architecture responsibility owner from `07-system-boundary-map.md`.
- Governance, source-control, open-decision, and rejected-path sections are non-behavioral control sections and must cite their governing professional-baseline source instead.
- Detailed platform-spec sections must not silently close architecture decision gaps.
- Open decisions belong in a dedicated open-decisions section and must cite `05`.
- Rejected alternatives belong in a dedicated rejected-paths section.
- Mechanism and instance must be split when both appear in the same topic.
- Envelope `type`, `shape_ref`, reference fields, projections, patterns, assignments, configuration, and product labels must remain separate axes.

## Top-Level Platform Specification Structure

### 00. Specification Governance

Primary owner: professional-baseline governance control

Purpose:

- define normative keywords, source hierarchy, acceptance process, and change-control relationship
- state that the platform specification is generated from the accepted baseline and gap register
- state how open decisions and rejected alternatives are cited

Must include:

- source hierarchy from evidence to baseline to platform specification
- section acceptance rules
- open-decision citation rule
- change-control rule for claims that change baseline semantics

Must not include:

- ADR narrative
- implementation planning
- product alignment as authority before assessment

Blocking gaps:

- none

### 01. Core Definitions And Boundary Vocabulary

Primary owner: professional-baseline governance control

Purpose:

- provide citable terms used by later normative sections
- preserve the axis split from `19-envelope-shape-parametrization-definitions.md`

Must include:

- event envelope
- envelope `type`
- `shape_ref`
- payload
- `actor_ref`
- `subject_ref`
- `activity_ref`
- shape
- platform-bundled shape
- activity
- pattern
- projection
- deployer parameterization
- operation classes: offline-capable, online/coordination-required, offline-with-constraints, configuration/control-plane

Must not include:

- final bundled shape inventory
- fixed product personas or actor subclasses
- platform behavior not owned by later sections

Blocking gaps:

- none

### 02. Event Log And Storage Model

Primary owner: Event Log / Storage

Purpose:

- specify canonical event-log source-of-truth behavior
- define append-only write discipline and projection-derived state rule

Must include:

- immutable event log as canonical truth
- all state changes enter through event store
- projections and views are derived and rebuildable
- no mutable canonical records or snapshot-primary truth
- acceptance boundary between structurally valid events and structural rejection

Must not include:

- identity evolution rules beyond raw-reference preservation
- authorization policy
- workflow state machines
- retention deletion or redaction policy unless formally closed

Blocking or constraining gaps:

- `Projection performance and caching` constrains implementation design, not baseline storage semantics.
- `Low-end device scale and offline performance` constrains implementation planning.
- `Retention and archival` blocks any normative deletion, redaction, or archive behavior that changes canonical history.

### 03. Event Envelope, Schema, And References

Primary owner: Event Envelope / Schema

Purpose:

- specify the stable event envelope contract and schema/version obligations
- preserve the `type` / `shape_ref` / reference-field separation

Must include:

- required envelope fields from the accepted baseline
- six platform-owned structural event `type` values
- `shape_ref` as payload fact schema/version
- optional `activity_ref` as activity context
- `actor_ref` as authorship
- `subject_ref` and typed reference contract
- device time as display/audit data only
- `device_sequence` and `sync_watermark` ordering/concurrency roles
- forbidden encodings as new envelope types

Must not include:

- new envelope fields
- new structural event types
- fixed role subclasses
- `authority_context`
- `pattern_ref` as an event-envelope structural reference
- domain facts, workflow states, sync states, or product surfaces as envelope types

Blocking or constraining gaps:

- `Envelope type, shape ref, references, and parametrization boundary` is P1 guardrail material for this section.
- `Event schema and versioning tooling` blocks final versioning obligations that affect compatibility and migration.
- `Structured import/export compatibility` constrains compatibility language if external exchange is included.
- `Process reference and process lifecycle semantics` blocks active process-reference lifecycle behavior if included.

### 04. Identity And Lineage

Primary owner: Identity / Lineage

Purpose:

- specify client-generated subject identity, alias/projection evolution, corrective split behavior, acyclicity, and raw-reference preservation

Must include:

- client-generated UUIDs for offline subject/record creation
- historical event references are not rewritten
- raw references remain available for conflict and authorization checks
- subject-lineage acyclicity as accepted baseline
- merge/split online-only where server validation is required

Must not include:

- actor provisioning or role assignment
- access scope ownership
- process lifecycle unless separately closed
- general flag lifecycle
- alias-cycle accept-and-flag behavior as settled

Blocking or constraining gaps:

- `Alias-cycle enforcement and resolution semantics` blocks any cycle-closing event behavior, read-side cyclic-graph behavior, or `cycle_violation` normalization.
- `Shared device actor scope` constrains actor/device/session language where identity examples touch device identity.
- `Process reference and process lifecycle semantics` blocks process lifecycle ownership if first identity sections include process references beyond contract language.

### 05. Assignment, Authority, And Sync

Primary owner: Assignment / Authority / Sync

Purpose:

- specify assignment-derived access, projection-derived authority, sync scope, original-reference authorization, and offline authority boundaries

Must include:

- sync unit is immutable event
- sync is idempotent, append-only, order-independent, and access-scope-filtered
- access is assignment-derived
- sync scope is access scope
- authority is projection-derived, not stored in events
- authorization checks use original subject reference
- scope expansion is additive
- scope contraction starts from selective retain under the accepted baseline
- local enforcement uses last-known scoped state where offline operation is allowed

Must not include:

- stored immutable `authority_context`
- group or identity-provider claims as direct authority
- arbitrary deployer access-control logic
- field-level sensitivity
- fixed platform actor subclasses
- new scope types without decision closure

Blocking or constraining gaps:

- `Subject-based scope and auditor access` blocks subject/auditor/cross-level scope semantics if included.
- `Shared device actor scope` blocks shared-device session/accountability support if included.
- `Temporary authority and offline revocation reconciliation` blocks temporary grants, grace periods, late revocation handling, and stale local authority surfacing if included.
- `Authorization visibility and role-action detail surfaces` blocks detailed permission table and visibility semantics if included.
- `Operational actor vocabulary and operation-class routing` is P1 before first-slice specification that mentions review, approval, oversight, setup, or resolution labels.
- `Sync delivery mechanics` constrains transport, pagination, priority, and bandwidth behavior.

### 06. Configuration And Parameterization

Primary owner: Configuration

Purpose:

- specify bounded deployer configuration over platform-owned mechanisms
- split platform-fixed mechanisms from deployer-configured instances

Must include:

- shapes, activities, roles, schedules, thresholds, severities, sensitivity parameters, and policy values as bounded configuration surfaces
- configuration gradient and expression limits
- atomic configuration packages
- server-only triggers where required
- trigger DAG depth constraints and complexity budgets where normative
- platform/deployer responsibility split
- deployer identifier naming rules where required for shapes or activities

Must not include:

- deployer-authored platform logic
- deployer-owned structural event type vocabulary
- arbitrary access-control programs
- field-level sensitivity
- state-machine mechanism authoring by deployers

Blocking or constraining gaps:

- `Configuration authoring and deployment UX` constrains concrete authoring, validation, packaging UX, and migration tooling.
- `Bounded context expression details` blocks final expression timing/value semantics if included.
- `Domain-agnostic proof gap` constrains broad claims that configuration can support all domains.
- `Sensitive data policy and local lifecycle` constrains sensitivity-policy language.

### 07. Projection, Workflow, And Pattern Registry

Primary owner: Projection / Workflow State

Purpose:

- specify projection-derived state, Pattern Registry mechanism, workflow transition evaluation, and workflow-specific ADR-005 behavior

Must include:

- workflow state is derived, not canonical event state
- Pattern Registry is platform-owned workflow primitive
- invalid workflow transitions are accepted and flagged, not rejected
- unresolved flagged events remain visible in timeline
- unresolved ADR-005 workflow flags are excluded from workflow state-machine evaluation until accepted resolution re-derives state
- pattern mechanism versus activity binding split
- no-pattern activity behavior if accepted into the section

Must not include:

- exact Pattern Registry inventory or schema without closing the P1 gaps
- `current_state` as canonical event state
- `status_changed` as structural event type
- pattern participants as platform actor subclasses
- product queues as canonical storage

Blocking or constraining gaps:

- `Exact Pattern Registry inventory` blocks final initial pattern list.
- `Formal Pattern Registry schema format` blocks final pattern definition contract.
- `Source-chain traversal limits` blocks normative source-chain traversal behavior if workflow flag lineage is specified in detail.
- `Bounded context expression details` blocks final form-context evaluation semantics.
- `Process reference and process lifecycle semantics` blocks process-scoped workflow behavior if included.
- `Reporting and aggregation` constrains any reporting-facing projection guarantees.

### 08. Flags, Conflict Surfacing, And Resolution

Primary owner: Flag / Resolution

Purpose:

- specify closed ADR-005 workflow flag behavior and route general flag semantics through explicit gaps
- define how accept-and-flag, detect-before-act, and structural rejection are distinguished

Must include:

- accept-and-flag applies to validly structured state anomalies, not malformed envelopes or invalid payloads
- structural validation remains separate from state anomaly handling
- ADR-005 workflow `transition_violation` behavior
- source-only workflow flag lineage
- platform-defined resolvability classification for ADR-005 workflow cases
- L3b auto-resolution for eligible workflow cases
- `system:auto_resolution/{policy_id}` actor attribution
- detect-before-act effect ordering for downstream workflow/policy effects

Must not include:

- unified general flag catalog as accepted baseline
- `cycle_violation` as accepted category
- server-created flags as permanent invariant
- request-time temporal anchor as general detector rule
- non-workflow auto-resolution behavior
- identity, authorization, workflow, or reporting source facts as flag-owned facts

Blocking or constraining gaps:

- `General flag semantics` is P1 if the platform spec needs non-workflow flags.
- `Domain conflict automation outside workflow` blocks general domain-conflict resolution automation.
- `Alias-cycle enforcement and resolution semantics` blocks cycle-related flag behavior.
- `Source-chain traversal limits` blocks detailed traversal requirements.
- `Auto-resolution authoring and monitoring` constrains tooling and audit surfaces beyond accepted ADR-005 behavior.

### 09. Local Data Lifecycle And Operational Constraints

Primary owner: Local Data Lifecycle

Purpose:

- specify device-side retain/remove/lifecycle obligations constrained by sync scope, scope changes, sensitivity, and operational conditions

Must include:

- scope expansion is additive
- scope contraction starts from selective retain under ADR-003 initial strategy
- sensitive deployments require stronger local lifecycle handling than retain-and-hide
- local lifecycle must not mutate central canonical event history
- offline field work and low-end-device constraints as constraints, not architecture changes

Must not include:

- deletion, redaction, or mutation of canonical events unless formally decided
- treating retain-and-hide as sufficient for sensitive local data
- changing access or sync semantics
- named regulatory-framework implementation as core behavior

Blocking or constraining gaps:

- `Sensitive data policy and local lifecycle` blocks concrete sensitive purge, encryption/key handling, retention window, and device-loss behavior if included.
- `Retention and archival` blocks canonical retention/archive rules.
- `Low-end device scale and offline performance` constrains local storage pressure and reference-device obligations.
- `Sync delivery mechanics` constrains local lifecycle interactions with delivery priority and pagination.

### 10. Reporting, Aggregation, And Freshness

Primary owner: Projection / Workflow State

Affected owner: Assignment / Authority / Sync

Purpose:

- specify reporting and aggregation only as derived, access-constrained read models with freshness semantics

Must include, if included in the platform spec:

- reports are derived from events/projections
- reporting respects assignment-derived access and sync scope
- freshness/staleness must be visible where operationally material
- reports do not become conflict, identity, or authority truth

Must not include:

- reporting as canonical state
- reporting as authority shortcut
- cross-level or auditor visibility without gap closure
- real-time oversight guarantee unless separately closed

Blocking or constraining gaps:

- `Reporting and aggregation` blocks final reporting capability boundaries.
- `Subject-based scope and auditor access` blocks auditor or cross-level reporting visibility.
- `Authorization visibility and role-action detail surfaces` constrains visibility rules.
- `Structured import/export compatibility` constrains export/reporting exchange language.

### 11. Trigger And Reactivity

Primary owner: Configuration

Affected owners: Event Log / Storage; Projection / Workflow State; Flag / Resolution

Purpose:

- specify trigger declaration limits and downstream effect ordering only where needed by the platform specification

Must include, if included:

- triggers are constrained by bounded configuration
- trigger writes enter through the event store
- downstream effects respect detect-before-act
- server-only triggers are allowed where global knowledge or authoritative validation is required

Must not include:

- trigger behavior as standalone architecture boundary
- arbitrary deployer code
- trigger expression access to forbidden projection/global data
- irreversible downstream effects before required checks

Blocking or constraining gaps:

- `Configuration authoring and deployment UX` constrains trigger declaration UX and validation.
- `Bounded context expression details` constrains expression input semantics.
- `General flag semantics` constrains flag-trigger interactions outside closed workflow cases.

### 12. Import, Export, And External Compatibility

Primary owner: Event Envelope / Schema

Affected owners: Event Log / Storage; Configuration; Reporting / Aggregation; Assignment / Authority / Sync

Purpose:

- preserve structured interoperability compatibility without making external schemas canonical

Must include, if included:

- internal event/envelope/schema model remains canonical
- exports and imports are derived or mapped artifacts
- access/scope constraints apply to exported views
- real-time integration is not implied by baseline constraints

Must not include:

- external system schema as canonical platform record
- mutation of historical events for export convenience
- Phase 1 real-time integration requirement
- bypass of assignment/sync-scope constraints

Blocking or constraining gaps:

- `Structured import/export compatibility` blocks final exchange contracts.
- `Reporting and aggregation` constrains export of derived reports.
- `Event schema and versioning tooling` constrains version compatibility.

### 90. Open Decisions And Gap Register Citations

Primary owner: professional-baseline governance control

Purpose:

- provide the platform-spec-facing open-decision list
- cite `05-decision-gap-register.md` as the only canonical gap source

Must include:

- gap identifier or heading
- affected platform-spec section
- handling: blocker, allowed deferral, implementation/tooling follow-up, operational policy, or product validation
- current priority from `05`

Must not include:

- duplicated gap ownership matrix that can drift from `05`
- new closure decisions
- implementation backlog items unless they are explicitly non-normative references

Blocking gaps:

- none, but must stay synchronized with `05`

### 91. Rejected Alternatives

Primary owner: professional-baseline governance control

Purpose:

- preserve rejected paths as implementation-facing forbidden patterns

Must include:

- rejected paths from `04` and `08`
- forbidden encodings from `18` and `19`
- unauthorized later-source absorption patterns from `10` through `13`

Must not include:

- new rejections without source or change-control record
- ADR narrative beyond the minimum source reference

Blocking gaps:

- none

### 92. Source Basis And Change-Control Log

Primary owner: professional-baseline governance control

Purpose:

- record which baseline artifacts each accepted platform-spec section used
- record later changes that affect the platform specification

Must include:

- accepted source basis per section
- change-control entries for any baseline-affecting update
- links to formal decisions that close gaps

Must not include:

- detailed engineering design
- speculative future decisions
- product-alignment source absorption before assessment

Blocking gaps:

- none

## First Section Blocker Map

This map identifies which `05` gaps block or constrain the first platform-spec sections. It is a drafting aid only; `05-decision-gap-register.md` remains canonical.

| Platform-Spec Section | Can Draft Stable Baseline Now? | Blocking Or Constraining Gaps |
|---|---|---|
| 00. Specification Governance | Yes | None. |
| 01. Core Definitions And Boundary Vocabulary | Yes | None, if it stays definitional and uses `19`. |
| 02. Event Log And Storage Model | Yes | Retention/archive and performance gaps constrain later implementation/policy text. |
| 03. Event Envelope, Schema, And References | Partial | Versioning obligations, process-reference emission, structured import/export compatibility, and final reference serialization details remain constrained. |
| 04. Identity And Lineage | Partial | Alias-cycle semantics block cycle behavior; process lifecycle and shared-device actor scope are deferrable unless in scope. |
| 05. Assignment, Authority, And Sync | Partial | Subject/auditor access, shared-device actor scope, temporary authority/offline revocation, and role-action visibility block those surfaces if included. Core assignment-derived sync can be drafted. |
| 06. Configuration And Parameterization | Yes | Authoring/deployment UX, bounded context details, and domain-agnostic proof constrain detail level. |
| 07. Projection, Workflow, And Pattern Registry | Partial | Exact Pattern Registry inventory and formal schema are P1 blockers before final workflow pattern spec. Source-chain traversal and bounded context details constrain workflow flag/form behavior. |
| 08. Flags, Conflict Surfacing, And Resolution | Partial | ADR-005 workflow flag behavior can be drafted. General flag semantics, domain conflict automation, alias-cycle flags, and traversal limits block broader flag spec. |
| 09. Local Data Lifecycle And Operational Constraints | Partial | Sensitive data policy, retention/archive, low-end device scale, and sync delivery mechanics block concrete lifecycle requirements. |
| 10. Reporting, Aggregation, And Freshness | Partial | Reporting/aggregation, subject/auditor access, authorization visibility, and structured export constraints block final reporting spec. |
| 11. Trigger And Reactivity | Partial | Trigger details route through configuration and detect-before-act; bounded context and general flag semantics constrain behavior. |
| 12. Import, Export, And External Compatibility | Partial | Structured import/export compatibility and schema/versioning obligations block final contracts. |
| 90. Open Decisions | Yes | Must cite `05`, not duplicate ownership. |
| 91. Rejected Alternatives | Yes | None. |
| 92. Source Basis And Change-Control Log | Yes | None. |

## Recommended Drafting Order

Draft in this order to keep the first pass professionally bounded:

1. `00. Specification Governance`
2. `01. Core Definitions And Boundary Vocabulary`
3. `91. Rejected Alternatives`
4. `90. Open Decisions And Gap Register Citations`
5. `02. Event Log And Storage Model`
6. `03. Event Envelope, Schema, And References`
7. `06. Configuration And Parameterization`
8. `05. Assignment, Authority, And Sync`
9. `04. Identity And Lineage`
10. `07. Projection, Workflow, And Pattern Registry`
11. `08. Flags, Conflict Surfacing, And Resolution`
12. `09` through `12` only where a selected platform-spec slice needs them

This order establishes governance and vocabulary first, then stable event/configuration foundations, then authority and identity, then workflow and flags where the P1 gaps must be handled explicitly.

## Immediate Decisions Before Detailed Sections

Before accepting detailed platform-spec sections, decide or explicitly defer these first:

- `Exact Pattern Registry inventory`
- `Formal Pattern Registry schema format`
- `General flag semantics`, if the first platform spec needs more than ADR-005 workflow flags
- `Alias-cycle enforcement and resolution semantics`, if identity-lineage or flag sections discuss cycle-closing events
- `Subject-based scope and auditor access`, if authorization/reporting includes those visibility paths
- `Temporary authority and offline revocation reconciliation`, if authorization/sync includes campaigns, emergency cover, temporary grants, or late revocation behavior
- `Authorization visibility and role-action detail surfaces`, if the first section includes detailed permission tables or assessment/cross-level visibility

Items outside that list can usually remain as explicit section hold-backs until implementation planning or deployment policy requires them.

## Existing Draft Section Inputs

Existing `../platform-spec/atoms/` files should be reviewed only after this outline is accepted. Review should classify each file as one of:

- compatible section input
- needs rewrite to match outline section ownership
- contains open-gap closure claims that must route through `05`
- contains rejected or unauthorized baseline drift
- implementation/tooling material, not normative platform specification

They should not be treated as accepted governance or as the source of this outline.
