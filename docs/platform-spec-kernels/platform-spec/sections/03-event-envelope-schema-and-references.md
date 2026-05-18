# 03 Event Envelope, Schema, And References

Status: Draft
Implementation readiness: Partial only
Owning boundary: Event Envelope / Schema
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/11-adr007-envelope-type-assessment.md`
- `../../professional-baseline/12-adr008-reference-fields-assessment.md`
- `../../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`
- `../../professional-baseline/20-platform-spec-outline.md`

Depends on:

- `00-specification-source-authority.md`
- `01-core-definitions-and-boundary-vocabulary.md`
- `02-event-log-and-storage-model.md`
- `90-open-decisions-and-gap-register-citations.md`
- `91-rejected-alternatives.md`

Consumed by:

- identity/reference, configuration, authority/sync, projection/workflow, flag/resolution, reporting, and local lifecycle sections
- append-to-log engineering design only after blockers in this section are closed or explicitly held back

## Purpose

This section defines the stable event-envelope boundary and the conceptual schema/reference obligations needed before implementation can validate accepted events. It intentionally keeps final serialization, tooling, and some active reference-emission details open where `05` says they remain unresolved.

## Scope

This section owns:

- stable envelope axes: event identity, `type`, `shape_ref`, payload, references, authorship, activity context, device metadata, and timestamp semantics
- fixed six-value `type` vocabulary
- `shape_ref` as payload schema/version reference
- `actor_ref` authorship and device identity separation
- optional `activity_ref` as activity context
- structural validation boundary versus accepted state anomaly
- explicit implementation-readiness blockers for schema/versioning and final reference serialization/emission

## Non-Scope

This section does not own:

- new envelope fields, type values, or structural reference categories
- final wire/database serialization
- final reference field names, placement, cardinality, or required emission sites beyond accepted baseline coverage
- process lifecycle or active process-reference emission
- final platform-bundled shape inventory
- schema/versioning tooling, migration design, or validator implementation
- assignment-derived authority, sync delivery mechanics, workflow state, flag lifecycle, reporting freshness, or local data lifecycle behavior

## Definitions

| Term | Meaning In This Section | Must Not Mean |
|---|---|---|
| Structural validation | Envelope, payload, schema, reference-contract, and platform-vocabulary checks before event acceptance | State, authority, workflow, identity-lineage, configured-domain, or reporting correctness |
| Envelope `type` | One of the accepted processing values: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed` | Domain event taxonomy, lifecycle state, role, product surface, activity, tenant, deployment, or authority |
| `shape_ref` | Required reference to payload schema and version | Workflow state, authority marker, role label, product surface, tenant, or deployment identity |
| `actor_ref` | Authorship reference for human or system actor | Device identity, role class, account identity, or permission grant |
| `activity_ref` | Optional configured activity context | Pattern reference, authority snapshot, work-item identity, tenant, or deployment |
| Reference contract | Envelope or shape-level way to point to a referent owned by another boundary | Lifecycle ownership of the referent |
| `device_time` | Device clock value for display/audit | Structural ordering source |
| `device_sequence` | Intra-device ordering signal | Cross-device total order |
| `sync_watermark` | Cross-device concurrency detection signal | Sync transport protocol or access entitlement |

## Invariants

- The envelope is platform-owned. Adding fields, changing field meaning, adding type values, or adding structural reference categories requires formal change control.
- `type` remains fixed to the six accepted processing values.
- `type` is processing behavior; payload fact vocabulary changes through `shape_ref` and payload schemas.
- Every event carries `shape_ref`; payload must match the declared shape before the event is structurally accepted.
- `shape_ref` values use the accepted `{shape_name}/v{version}` convention unless a later accepted section changes it through change control.
- `actor_ref` records authorship. `device_id` is device/app-installation identity and must not be treated as actor identity.
- `activity_ref` is optional configured context and must not become authority, pattern identity, tenant/deployment identity, or work-item identity.
- `device_time` is advisory for display and audit only. Projection ordering, conflict detection, and protocol correctness must not depend on device-clock ordering.
- Structural validation rejects malformed envelopes, invalid platform vocabulary, impossible schema references, malformed required reference contracts, and payload schema failures.
- Accept-and-flag applies only after structural validity. State, authority, workflow, identity-lineage, or configured-domain anomalies must not be hidden as envelope errors unless formally accepted.

## Contracts

### Inputs

- event identity data following accepted client-generated identity discipline
- accepted `type`
- `shape_ref` and payload
- `actor_ref`
- optional `activity_ref`
- subject, causal, process, assignment, or other typed references only where accepted baseline language, shape contract, or later accepted section requires them
- timestamp data including advisory device time
- `device_id`, `device_sequence`, and `sync_watermark`

### Outputs

- structurally valid event envelope for Event Log / Storage
- structural validation result
- parsing and validation obligations for downstream sections
- explicit blocker list where implementation validation requires missing schema/reference detail

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Log / Storage | structurally valid event envelope | Storage persists; this section validates structural contract. |
| Identity / Lineage | raw subject/reference values where required | Identity owns subject continuity and lineage behavior. |
| Assignment / Authority / Sync | `actor_ref`, activity/reference context, device metadata | Authority is projection-derived, not stored in the envelope. |
| Configuration | `shape_ref`, activity instances, bounded shape/version rules | Configuration may add shapes and activities but not envelope fields or type values. |
| Projection / Workflow State | type, shape, payload, references, timestamps | Workflow state remains projection-derived. |
| Flag / Resolution | structurally valid event contract | Flag lifecycle and detector ownership remain outside this section. |

## Allowed Extension Points

- New shapes and shape versions may be added through Configuration while preserving `shape_ref`.
- Activity instance values may vary by deployment while preserving `activity_ref` meaning.
- Platform-bundled shapes may be accepted later only by owning behavior sections and remain `shape_ref` values.
- Serialization and validator implementations may vary only after preserving this contract and closing or deferring the schema/tooling gap.

## Forbidden Couplings

- Do not add envelope fields for implementation convenience.
- Do not encode domain facts, lifecycle states, workflow states, role labels, product surfaces, activity labels, sync states, or identity/integrity facts as `type`.
- Do not store `authority_context`, `current_state`, `pattern_ref`, `tenant_id`, `deployment_id`, `user_id`, or `group_id` in the envelope.
- Do not use `device_time` for structural ordering.
- Do not use `actor_ref`, account, group, identity-provider claim, tenant, or deployment as direct authority.
- Do not require central pre-registration of every referenceable entity for structurally valid offline capture.
- Do not treat references as referent lifecycle ownership.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Event schema and versioning tooling | `05`; Event Envelope / Schema plus Event Log / Storage and Configuration | Required before this section is implementation-ready for general append validation. |
| Final reference serialization and active emission sites | `05`; Event Envelope / Schema plus owning behavior sections | Required before append validation depends on canonical reference field names, placement, cardinality, or emission sites. |
| Process reference and process lifecycle semantics | `05`; Projection / Workflow State plus Event Envelope / Schema | Required if process lifecycle or active process-reference emission is included. |
| Structured import/export compatibility | `05`; Event Envelope / Schema plus Reporting / Aggregation | Required if external exchange or audit export is included. |
| Platform-bundled shape inventory | `05`; owning behavior sections plus Event Envelope / Schema and Configuration | Required before any platform-owned fact shape is normative. |
| Configuration versioning and stale-configuration reconciliation | `05`; Configuration plus Event Envelope / Schema | Required before append validation handles offline work created under older configuration. |

## Rejected Paths

- Adding envelope fields, type values, or structural reference categories without change control.
- Adding `status_changed`, `current_state`, or `pattern_ref` to the envelope.
- Treating `type` as deployer-extensible domain taxonomy.
- Treating `shape_ref`, `activity_ref`, references, assignments, patterns, projections, and product labels as interchangeable.
- Treating device identity as actor identity.
- Treating malformed payloads as accept-and-flag cases.

## Implementation Implications

- This draft is not sufficient by itself for implementation-ready append.
- A first append slice needs at least enough accepted schema/version and reference-serialization detail to decide whether an event is structurally valid.
- If the first slice excludes process references, structured import/export, shared-device sessions, temporary authority, or sync transport, those exclusions must be explicit in `90`.
- Code must not invent missing reference or schema contracts locally.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] `05` blockers and constraints are explicit.
- [ ] No new envelope fields, type values, authority sources, actor subclasses, or canonical projection state are introduced.
- [ ] Structural rejection remains separate from accepted state anomaly.
- [ ] Implementation-readiness blockers are visible.
