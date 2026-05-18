# Platform Specification Outline

Status: Draft outline from accepted professional baseline

This document defines the top-level structure for the normative platform specification. It is not the platform specification itself, not an implementation design, and not another process layer.

The outline exists to route future platform-spec sections through accepted architecture responsibility areas before detailed spec text is accepted.

## Source Basis

Primary inputs:

- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`

Assessed inputs already routed through `05`, `07`, and this outline:

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
- Existing `../platform-spec/sections/` files. They are draft section input only.
- Product-alignment material, until separately assessed into this baseline path.
- Prior process labels or implementation-era contract IDs.

## Outline Rules

- Every normative behavior section must cite its accepted baseline source or a named gap in `05-decision-gap-register.md`.
- Every normative behavior section must name one primary architecture responsibility owner from `07-system-boundary-map.md`.
- Source-authority, open-decision, and rejected-path sections are non-behavioral control sections and must cite their professional-baseline source instead.
- Detailed platform-spec sections must not silently close architecture decision gaps.
- Open decisions belong in a dedicated open-decisions section and must cite `05`.
- Rejected alternatives belong in a dedicated rejected-paths section.
- Mechanism and instance must be split when both appear in the same topic.
- Envelope `type`, `shape_ref`, reference fields, projections, patterns, assignments, configuration, and product labels must remain separate axes.

## Top-Level Platform Specification Structure

### 00. Specification Source Authority

Primary owner: professional-baseline source authority

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

Primary owner: professional-baseline source authority

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

- `Envelope type, shape ref, references, and parametrization boundary` is P1 constraint material for this section.
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

Primary owner: professional-baseline source authority

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

Primary owner: professional-baseline source authority

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

Primary owner: professional-baseline source authority

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
| 00. Specification Source Authority | Yes | None. |
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

## Architecture Steward Review Of First Sections

This review checks sections `00`, `01`, `02`, `03`, `90`, and `91` against `05-decision-gap-register.md`.

Decision language:

- `Can draft`: the section can be written as platform-spec text now.
- `Implementation-ready`: engineering can depend on the section without inventing missing contracts.
- `Blocker`: a `05` gap must be closed or explicitly held back before the section can claim the affected behavior.
- `Constraint`: the section can proceed, but must state the limit and cite the owning `05` gap.

| Section | Can Draft? | Implementation-Ready? | Architecture Steward Decision |
|---|---|---|---|
| `00. Specification Source Authority` | Yes | Yes for source hierarchy and status rules | No `05` gap blocks this section. It must establish that `05` remains the canonical open-gap register and that candidate section files are not normative until accepted under the platform-spec outline. |
| `01. Core Definitions And Boundary Vocabulary` | Yes | Yes for vocabulary only | No `05` gap blocks definitional text. The section must not close final bundled shape inventory, Pattern Registry inventory, general flag catalog, role taxonomy, process lifecycle, or reference serialization. It should use `19` and cite `05` where a term names an open surface. |
| `02. Event Log And Storage Model` | Yes | Yes for append-only storage invariants; no for retention, archival, performance, or local lifecycle mechanics | No architecture gap blocks event-log source-of-truth, append-only acceptance, or projection-derived state. `Projection performance and caching`, `Low-end device scale and offline performance`, and `Retention and archival` are constraints. The section must not define deletion, redaction, archive policy, compaction, cache strategy, or low-end-device budgets as settled behavior. |
| `03. Event Envelope, Schema, And References` | Yes | Partial | The stable envelope axes can be drafted now: required baseline fields, six `type` values, `shape_ref`, `actor_ref`, optional `activity_ref`, device-time advisory semantics, `device_sequence`, and `sync_watermark`. Implementation-ready append still needs enough schema/reference detail to validate an event. Therefore `Event schema and versioning tooling` and final reference serialization/emission details are blockers for implementation-ready append semantics, unless explicitly held back from the first slice. `Process reference and process lifecycle semantics` is a blocker only if process lifecycle or active process-reference emission is included. `Structured import/export compatibility` is a constraint only if external exchange is included. |
| `90. Open Decisions And Gap Register Citations` | Yes | Yes, if synchronized with `05` | No `05` gap blocks this section. It must not duplicate ownership or invent closure. Its job is to cite `05`, list affected platform-spec sections, and state whether each gap is a blocker, constraint, deferral, policy item, implementation/tooling item, or product-validation item. |
| `91. Rejected Alternatives` | Yes | Yes for forbidden paths | No `05` gap blocks this section. It must preserve rejected paths from `04`, `08`, and assessed inputs without adding new rejections. If a rejected path appears necessary for implementation, the response is change control, not local exception text. |

First-section blocker result:

- `00`, `01`, `02`, `90`, and `91` can be drafted now without closing additional `05` gaps.
- `03` can be drafted now as the envelope boundary section, but cannot be treated as sufficient for implementation-ready append until schema/versioning and reference serialization/emission obligations are settled or explicitly scoped out of the first append slice.
- The first implementation-facing slice must not begin from `02` alone. Append-to-log needs at least stable text from `00`, `01`, `02`, `03`, `90`, and `91`.

Immediate gap decisions for the first sections:

| Gap From `05` | Decision For Sections `00`, `01`, `02`, `03`, `90`, `91` |
|---|---|
| `Envelope type, shape ref, references, and parametrization boundary` | Constraint for `01`, `03`, and `91`; not a blocker if the sections preserve the axis split and do not add envelope fields, type values, actor subclasses, or product classes. |
| `Event schema and versioning tooling` | Blocker for implementation-ready `03`; constraint for conceptual `03`. The first append slice needs at least normative event schema/version obligations, even if tooling and migration design remain later. |
| Final reference serialization and active emission details under the envelope/reference gap | Blocker for implementation-ready `03` if append validation requires canonical reference field names, placement, cardinality, or required emission sites. Can be held back only if the first slice limits references narrowly and states that limit. |
| `Process reference and process lifecycle semantics` | Not a blocker for first sections if process lifecycle and active process-reference emission are out of scope. Blocker if `03` or `01` tries to define process lifecycle behavior. |
| `Structured import/export compatibility` | Not a blocker for first sections unless `03` or `91` defines external exchange. Keep as explicit hold-back. |
| `Projection performance and caching` | Constraint for `02`; not a blocker. Do not specify cache/rebuild strategy as architecture. |
| `Low-end device scale and offline performance` | Constraint for `02`; not a blocker. Do not use performance pressure to weaken event-log source of truth. |
| `Retention and archival` | Constraint for `02` and `91`; blocker only if the section defines deletion, redaction, archival, or canonical-history mutation. |
| `Operational actor vocabulary and operation-class routing` | Constraint for `01` and `91`; not a blocker if role labels remain examples and operation classes remain behavioral. |
| `Exact Pattern Registry inventory` and `Formal Pattern Registry schema format` | Not blockers for first sections. `01` may define `pattern` only as a term; it must not define inventory or schema. |
| `General flag semantics`, `Domain conflict automation outside workflow`, `Alias-cycle enforcement and resolution semantics`, and `Source-chain traversal limits` | Not blockers for first sections unless `91` lists rejected/held-back paths. They remain blockers for later flag, workflow, and identity behavior sections. |
| `Subject-based scope and auditor access`, `Shared device actor scope`, `Temporary authority and offline revocation reconciliation`, and `Authorization visibility and role-action detail surfaces` | Not blockers for first sections unless `03` over-defines actor/session/reference behavior. They remain blockers for affected authority/sync/reporting sections. |

## Recommended Drafting Order

Draft in this order to keep the first pass professionally bounded:

1. `00. Specification Source Authority`
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

This order establishes source authority and vocabulary first, then stable event/configuration foundations, then authority and identity, then workflow and flags where the P1 gaps must be handled explicitly.

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

## Minimum Append-To-Log Gate

The accepted architecture baseline is not enough by itself to implement even the first append-to-log slice. Before engineering design treats event append as buildable, the first platform-spec sections must settle or explicitly hold back the following:

| Needed For First Append | Platform-Spec Section | Required Standing |
|---|---|---|
| Source authority and status rules | `00. Specification Source Authority` | Stable enough that implementation knows which documents are normative. |
| Core vocabulary | `01. Core Definitions And Boundary Vocabulary` | Stable enough that `event`, `envelope`, `payload`, `shape_ref`, references, actor, device, projection, and configuration mean one thing. |
| Append-only storage invariant | `02. Event Log And Storage Model` | Normative statement that accepted events are immutable operational facts and projections are derived. |
| Event acceptance boundary | `02` and `03. Event Envelope, Schema, And References` | Clear split between structural rejection and accepted state anomaly. |
| Minimal envelope contract | `03` | Required fields, six `type` values, `shape_ref`, `actor_ref`, `activity_ref`, subject/reference obligations where in scope, device metadata, and timestamp semantics. |
| Shape/version obligation | `03` and `06. Configuration And Parameterization` | Enough shape naming/versioning and validation language to decide whether a payload matches its declared shape. |
| Authorship and device distinction | `03` and `05. Assignment, Authority, And Sync` | `actor_ref` authorship and device identity cannot be collapsed; shared-device behavior can be explicitly held back if out of first slice. |
| Offline ordering/concurrency metadata | `03` and `05` | `device_sequence`, `sync_watermark`, and advisory `device_time` semantics must be clear enough for append and later sync. |
| Open-decision citation | `90. Open Decisions And Gap Register Citations` | Any missing reference serialization, schema tooling, sync delivery, or authorization detail must be visible as a `05` gap, not hidden in implementation. |
| Rejected paths | `91. Rejected Alternatives` | Implementation must know not to add mutable canonical records, snapshot-primary truth, new envelope fields, stored authority snapshots, or structural ordering by device time. |

If any first-slice implementation needs final reference serialization, process-reference emission, shared-device sessions, temporary authority, schema migration, or sync transport behavior, that need must be routed to the owning gap before code depends on it.

## Existing Candidate Section Inputs

Existing `../platform-spec/sections/` files should be reviewed only after this outline is accepted. Review should classify each file as one of:

- compatible section input
- needs rewrite to match outline section ownership
- contains open-gap closure claims that must route through `05`
- contains rejected or unauthorized baseline drift
- implementation/tooling material, not normative platform specification

They should not be treated as accepted platform specification or as the source of this outline.
