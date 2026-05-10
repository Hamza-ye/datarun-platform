# Event Envelope And Schema

Status: Draft
Owning boundary: Event Envelope / Schema
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/09-identity-boundary-control.md`
- `../../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`
- `../../pre-operations/04-accepted-pre-atomization-decisions.md`

Depends on:

- `01-spec-governance.md`
- `02-glossary-and-core-definitions.md`
- `03-event-log-storage.md`
- `90-open-decisions.md`
- `91-rejected-paths.md`

Consumed by:

- `05-references-and-identity-lineage.md`
- `06-configuration-and-parametrization.md`
- `07-assignment-authority-and-sync.md`
- `09-projections-workflow-and-patterns.md`
- `10-conflict-flag-and-resolution.md`
- implementation designs for event validation, persistence, projection, sync, and schema tooling

## Purpose

This atom defines the stable platform-owned event envelope contract every accepted event carries before storage. It keeps envelope processing, payload shape, references, activity context, authority, workflow state, and product vocabulary on separate axes.

## Scope

This atom owns:

- stable envelope obligations for event identity, `type`, `payload`, and timestamp expression
- platform-owned structural `type` vocabulary and processing meaning
- required `shape_ref` contract for payload schema and version
- optional `activity_ref` contract for configured activity context
- `actor_ref` authorship and system actor convention
- subject, device, causal, and typed-reference obligations where required by the accepted envelope/reference contract or relevant shape
- device-time advisory semantics, intra-device sequence semantics, and cross-device concurrency metadata semantics
- envelope-side constraints that prevent deployer policy, workflow state, authority snapshots, tenant/deployment identity, or product labels from becoming envelope fields

## Non-Scope

This atom does not own:

- new envelope fields, new structural reference categories, or new `type` values
- formal envelope serialization details or implementation schema tooling
- final shape inventory or platform-bundled shape inventory
- referent registration, referent attributes, catalogs, or referent lifecycle behavior
- identity merge/split lifecycle behavior
- assignment-derived authorization, authority reconstruction, or sync delivery mechanics
- workflow state, pattern inventory, pattern schema, or product queue/status behavior
- flag lifecycle or conflict-resolution behavior
- configuration authoring format, packaging UX, or deploy-time validator UX
- retention, archival, reporting freshness, or local data lifecycle behavior

## Definitions

| Term | Meaning In This Atom | Must Not Mean |
|---|---|---|
| Event envelope | Stable platform-owned contract carried by every event so the platform can store, sync, route, attribute, and interpret immutable facts | Deployer-authored schema surface, product-specific wrapper, or mutable record body |
| Event identity | Envelope identity obligation for the immutable event fact, including baseline client-generated identity discipline | Tenant, deployment, account, group, or product work-item identity |
| Envelope `type` | Platform-owned processing-pipeline discriminator | Domain fact taxonomy, lifecycle state, role label, product surface, authorship, online/offline class, tenant, deployment, or authority marker |
| Payload | Shape-conforming fact body carried by an event | Envelope contract, authority snapshot, workflow state store, or place to redefine envelope fields |
| Timestamp | Event time data required by the envelope | Sole ordering authority for projections, conflict detection, or protocol correctness |
| `shape_ref` | Required payload schema and version reference, formatted as `{shape_name}/v{version}` | Workflow state, authority marker, product surface, online/offline class, role label, tenant identity, or deployment identity |
| Shape | Typed, versioned payload schema identified by `shape_ref` | Envelope `type`, workflow pattern, actor class, access rule, or accepted final platform shape inventory |
| `activity_ref` | Optional reference to the deployer-configured activity instance in which the event was produced, or null when unknown | Pattern reference, tenant/deployment reference, work-item identity, assignment authority, or immutable authority context |
| `actor_ref` | Reference identifying who or what authored the event | Permission grant, role class, product persona, device identity, or complete authentication identity |
| System actor | Platform-owned author convention using `system:{source_type}/{source_id}` | Hidden human actor, tenant identity, or deployer-authored authority source |
| `subject_ref` | Reference identifying what an event is about where the accepted envelope/reference contract or relevant shape requires a subject reference | Ownership of every lifecycle, authority, matching, workflow, or reporting behavior for that referent |
| Typed reference | Reference whose category and contract are meaningful to the platform boundary consuming it when the accepted contract or relevant shape requires that reference | Lifecycle ownership claim over the referent |
| Causal reference | Link to a source or cause where required by the accepted envelope/reference contract or the relevant shape | Universal source-event envelope field, workflow state, or pattern reference |
| `device_id` | Device or app-installation namespace for device semantics | Actor identity or authorization source |
| `device_time` | Device clock time recorded for display and audit | Structural ordering source |
| `device_sequence` | Intra-device ordering signal | Cross-device total order or actor identity |
| `sync_watermark` | Cross-device concurrency detection signal | Sync protocol, workflow state, event truth, or access entitlement |

## Invariants

- Every event handed to Event Log / Storage must carry the accepted envelope obligations for identity, `type`, `payload`, timestamp, required shape reference, authorship reference, any subject, causal, device, or typed-reference values required by the accepted envelope/reference contract or relevant shape, and device/concurrency metadata required by the baseline.
- The envelope is platform-owned. Adding fields, changing field meanings, or adding structural reference categories requires formal change control.
- The structural `type` vocabulary is fixed to the accepted six values: `capture`, `review`, `alert`, `task_created`, `task_completed`, and `assignment_changed`.
- Envelope `type` is only a processing-pipeline discriminator; it must not encode domain fact names, lifecycle states, workflow states, product surfaces, role labels, activity labels, sync/display states, tenant/deployment identity, or authority.
- Every event carries `shape_ref`; payload fact vocabulary changes through shape addition or shape versioning, not through new envelope type values.
- `shape_ref` values use `{shape_name}/v{version}`, where shape names match `[a-z][a-z0-9_]*` and versions are integer versions.
- Payload must conform to the shape identified by `shape_ref`; payload fields do not modify the envelope contract.
- `activity_ref` is optional and may be null when activity context is unknown. When present, it preserves configured activity-instance context but does not carry authority.
- `actor_ref` records authorship. Human and system actors can author events, and system actors use the `system:{source_type}/{source_id}` convention.
- `device_id` is device or app-installation identity, not actor identity.
- `subject_ref` and other typed references, where required, are reference contracts. They do not decide referent lifecycle ownership.
- `device_time` is advisory for display and audit only. Projection ordering, conflict detection, and protocol correctness must not depend on device clock ordering.
- `device_sequence` supplies intra-device ordering, and `sync_watermark` supports cross-device concurrency detection. This atom does not define sync transport, pagination, priority, bandwidth handling, or delivery mechanics.
- The envelope must not store immutable `authority_context`, `tenant_id`, `deployment_id`, `user_id`, `group_id`, `current_state`, or `pattern_ref`.
- `status_changed` is not a structural event type.

## Contracts

### Inputs

- event identity data following baseline client-generated identity discipline
- accepted structural `type` value
- `payload` body to validate against the shape named by `shape_ref`
- `shape_ref` supplied under the shape/configuration contract
- optional `activity_ref` supplied under the activity/configuration contract
- `actor_ref` authorship reference
- subject, causal, device, and typed-reference values where required by the accepted envelope/reference contract or relevant shape
- timestamp data, including device clock timestamp for display and audit
- `device_id`, `device_sequence`, and `sync_watermark` metadata required by baseline ordering and concurrency semantics

### Outputs

- structurally valid event envelope for Event Log / Storage persistence
- envelope validation result before an event is treated as an accepted operational fact
- stable parsing obligations for downstream storage, projection, assignment/sync, identity, flag, reporting, and local lifecycle consumers
- schema/versioning obligations to be detailed by later tooling work without changing this contract

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Log / Storage | structurally valid event envelope | Storage persists immutable events but does not redefine envelope fields. |
| Identity / Lineage | subject or typed references where required, raw reference values, and event identity discipline | Identity owns subject continuity only for referents with subject-lineage semantics; envelope owns only reference contracts. |
| Assignment / Authority / Sync | `actor_ref`, `activity_ref`, subject or typed references where required, device metadata, and sync concurrency metadata | Authority is reconstructed through assignment/sync behavior, not stored in the envelope. |
| Configuration | `shape_ref`, `activity_ref`, and platform-owned `type` vocabulary | Configuration may define shapes and activity instances inside bounded mechanisms; it cannot add envelope fields or type values. |
| Projection / Workflow State | `type`, `shape_ref`, payload, references, and timestamps as event inputs | Workflow state remains projection-derived and is not stored as an envelope field. |
| Flag / Resolution | `type`, payload, causal/source links where required by shape, and system actor convention | This atom does not define general flag lifecycle or conflict-resolution behavior. |
| Reporting / Aggregation | event envelope and shape references as source material | Reports and aggregates consume derived/read models; they are not envelope truth. |
| Local Data Lifecycle | event identity, references, and scoped event subsets | Local retain/remove behavior must not mutate central event history or envelope semantics. |

## Allowed Extension Points

- New payload shapes and shape versions may be added through the shape/configuration path while preserving the `shape_ref` contract.
- Activity instance values may vary by deployment through configuration while preserving the optional `activity_ref` contract.
- Platform-bundled shapes may be accepted later only as explicit exceptions for platform-owned facts; they remain shapes identified by `shape_ref`.
- Serialization format, schema tooling, and validator implementation may vary only if they preserve the accepted envelope meanings and are routed through the open schema/tooling gap.
- New envelope fields, new structural reference categories, changed field meanings, or new `type` values are not local extension points; they require formal change control.

## Forbidden Couplings

- Do not add envelope fields without formal change control.
- Do not change envelope field meaning without formal change control.
- Do not encode domain facts, identity/integrity facts, workflow states, product surfaces, role labels, activity labels, sync/display states, or escalation levels as envelope `type` values.
- Do not use `shape_ref` as workflow state, authority marker, product surface, online/offline class, role label, tenant identity, or deployment identity.
- Do not use `activity_ref` as immutable `authority_context`, pattern reference, deployment/tenant reference, assignment authority, or product queue identity.
- Do not infer permission, role class, product persona, tenant, deployment, or sync entitlement from `actor_ref`.
- Do not treat `device_id` as actor identity.
- Do not treat reference fields as referent lifecycle ownership.
- Do not add `tenant_id`, `deployment_id`, `user_id`, or `group_id` to the event envelope without formal change control.
- Do not store `current_state`, `pattern_ref`, or `status_changed` in the envelope to represent workflow state.
- Do not make deployer policy fields become envelope fields.
- Do not make configuration authoring define envelope fields, type values, access-control logic, state-machine mechanisms, or arbitrary platform code.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Formal envelope serialization details | Event Envelope / Schema plus implementation tooling | Implementation needs wire format, database representation, or interoperability schema. |
| Event schema/versioning tooling | Event Envelope / Schema plus Event Log / Storage and Configuration | Validation, migration, or mixed-version event handling must be implemented. |
| Projection compatibility across schema versions | Projection / Workflow State plus Event Envelope / Schema | Projections must consume multiple shape or envelope schema versions. |
| Platform-bundled shape inventory | Owning behavior atoms plus Event Envelope / Schema and Configuration | A platform-owned fact needs a bundled shape contract. |
| Final reference serialization and active emission sites | Event Envelope / Schema plus Identity / Lineage, Assignment / Authority / Sync, Configuration, Projection / Workflow State, and owning behavior atoms | A later atom or implementation needs canonical field names, placement, cardinality, or required emission sites for accepted subject, causal, device, process, assignment, or other typed-reference values. |
| Referent registration, attributes, and catalogs | Event Envelope / Schema for reference contracts; Identity / Lineage, Configuration, Projection / Workflow State, and Assignment / Authority / Sync for lifecycle/configuration ownership | A spec needs subject registration events, referent attribute mutation/projection, deployer-defined catalogs, or platform-bundled registration shapes. |
| Sync delivery mechanics | Assignment / Authority / Sync plus implementation tooling | Transport, pagination, priority, bandwidth handling, or operational sync delivery must be specified. |
| Configuration authoring and deploy-time validation UX | Configuration plus implementation tooling | Admin/configuration authoring or deployment validation surfaces are selected for implementation. |
| Structured import/export contracts | Event Envelope / Schema plus Reporting / Aggregation | First deployment requires import/export or audit exchange. |

## Rejected Paths

- Adding envelope fields as implementation convenience.
- Treating the six structural `type` values as deployer-extensible domain event taxonomy.
- Adding `status_changed` as a structural type.
- Storing `current_state` as canonical event state.
- Adding `pattern_ref` as an event-envelope structural reference.
- Storing immutable `authority_context`.
- Adding `tenant_id`, `deployment_id`, `user_id`, or `group_id` as event-envelope authority fields.
- Using `device_time` for structural ordering.
- Treating `actor_ref`, account identity, group membership, IdP claims, tenant, or deployment as direct authority sources.
- Treating `shape_ref`, `activity_ref`, references, patterns, projections, assignments, and product labels as interchangeable.
- Treating queues, work items, dashboards, review lists, or status labels as envelope or storage primitives.
- Requiring central pre-registration of every referenceable entity for structurally valid offline capture.
- Encoding referent registration or lifecycle as envelope `type`.

## Implementation Implications

- Event creation code should validate the envelope contract before handing an event to storage.
- Event processors should branch on `type` for platform processing and on `shape_ref` for payload schema interpretation.
- Authorization, workflow, conflict, reporting, and local lifecycle code must consume envelope values without treating them as stored authority, stored workflow state, or referent lifecycle ownership.
- Implementations may choose concrete serializers and validators later, but those choices cannot change field meanings or add fields outside change control.
- Product and deployer vocabulary should map through shapes, activities, assignments, patterns, projections, and UI translation rather than through envelope extensions.

## Review Checklist

- [x] Source basis is accepted and cited.
- [x] Owner and boundary are singular.
- [x] Scope and non-scope are explicit.
- [x] Contracts identify inputs, outputs, and boundary crossings.
- [x] Open gaps are not closed accidentally.
- [x] Forbidden couplings include the likely drift risks.
- [x] No envelope field, type value, authority shortcut, or canonical projection state was added without change control.
- [x] Product labels, role labels, and UI surfaces remain outside platform-core semantics.

Drafting Agent note:

- This is a narrow envelope-contract draft. It does not accept serialization format, schema tooling, referent lifecycle, workflow state, sync mechanics, or configuration authoring UX.

Architecture Steward reconciliation, 2026-05-11:

- Challenge Reviewer P1 accepted. Reference wording now qualifies subject, causal, device, and typed references as required only where the accepted envelope/reference contract or relevant shape requires them; final serialization and active emission sites remain an explicit open gap.
- Challenge Reviewer P2 accepted. Source basis now cites the decision gap register and the identity, conflict/offline, operational-constraint, and authorization-visibility controls used by this atom.
- Glossary check: no glossary behavior was added here. SPEC-004 consumes cross-boundary definitions while keeping reference emission, referent lifecycle, authority, workflow, and serialization behavior in their owning atoms or open gaps.
