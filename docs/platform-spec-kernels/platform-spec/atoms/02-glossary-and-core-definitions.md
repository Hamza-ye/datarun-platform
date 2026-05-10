# Glossary And Core Definitions

Status: Draft
Owning boundary: Cross-boundary definitions
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../../pre-operations/04-accepted-pre-atomization-decisions.md`
- `../../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`

Depends on:

- `01-spec-governance.md`
- `90-open-decisions.md`
- `91-rejected-paths.md`

Consumed by:

- `03-event-log-storage.md`
- `04-event-envelope-schema.md`
- `05-references-and-identity-lineage.md`
- `06-configuration-and-parametrization.md`
- `07-assignment-authority-and-sync.md`
- all later atoms that need stable platform vocabulary

## Purpose

This atom defines shared vocabulary for platform specification atoms so later specs can use stable terms without re-deciding ownership, envelope shape, authority, projection, workflow, or configuration semantics.

## Scope

This atom owns:

- cross-boundary definitions used by platform-spec atoms
- distinctions between canonical truth, derived state, references, configuration, product vocabulary, and operational labels
- glossary-level extension and forbidden-meaning rules
- definition routing for downstream atoms

## Non-Scope

This atom does not own:

- event-envelope field list or serialization details
- final shape inventory or platform-bundled shape inventory
- formal Pattern Registry schema or inventory
- identity merge/split behavior
- assignment, authorization, sync, or local lifecycle behavior
- conflict, flag, or resolution lifecycle behavior
- reporting freshness model
- product labels, UI wording, or implementation module names
- closure of any open decision listed in `90-open-decisions.md`

## Definitions

| Term | Meaning In This Atom | Must Not Mean |
|---|---|---|
| Platform baseline | The accepted ADR-001 through ADR-005 closure represented by the professional baseline | Later ADR material, product alignment prose, implementation design, or draft atom text |
| Spec atom | A small implementation-facing specification unit with one owner, one boundary, contracts, forbidden couplings, and open gaps | Product feature brief, implementation module, or free-form architecture essay |
| Canonical truth | The append-only event log's accepted immutable events as the source of operational facts | Projection state, mutable record state, snapshot state, queue state, or dashboard state |
| Event | An immutable operational fact accepted through the event-store write path | Mutable record, projection row, task card, product notification, or workflow state |
| Operational fact | A valid accepted event preserved as historical fact | Proof that every downstream effect is authorized, conflict-free, workflow-valid, or clean |
| Event log | The append-only canonical store of accepted immutable events | Audit sidecar for mutable records, action-log substitute, projection cache, or queue store |
| Append-only | Accepted events are added as new facts, and later interpretation changes through later events and projections | No validation, no correction path, no retention policy, or no lifecycle policy |
| Event envelope | The stable platform-owned contract every event carries so the platform can store, sync, route, attribute, and interpret immutable facts | Deployer-authored schema surface or product-specific record wrapper |
| Envelope `type` | Platform-owned processing-pipeline discriminator | Domain fact taxonomy, lifecycle state, role label, product surface, authorship, tenant, deployment, or authority marker |
| Payload | The shape-conforming fact body carried by an event | Envelope contract, authority snapshot, or canonical workflow state |
| Timestamp | Event time data required by the envelope | Sole ordering authority for projections, conflict detection, or protocol correctness |
| `device_time` | Device clock time recorded for display and audit | Structural ordering source |
| `device_sequence` | Intra-device ordering signal | Cross-device total order or actor identity |
| `sync_watermark` | Cross-device concurrency detection signal | Global workflow state, event truth, or access entitlement |
| `shape_ref` | Payload schema and version reference, formatted as `{shape_name}/v{version}` | Workflow state, authority marker, product surface, online/offline class, role label, tenant identity, or deployment identity |
| Shape | A typed, versioned payload schema identified by `shape_ref`; shapes are deployer-defined by default | Envelope `type`, workflow pattern, actor class, access rule, or platform-owned domain schema catalog |
| Platform-bundled shape | Narrow exception for an explicitly accepted platform-owned fact shape | New envelope `type`, general platform-supplied business schema catalog, or accepted final bundled-shape inventory |
| `activity_ref` | Optional reference to the deployer-configured activity instance in which an event was produced, or null when unknown | Pattern reference, tenant/deployment reference, work-item identity, assignment authority, or immutable authority context |
| Activity | A deployer-configured operational instance binding shapes, pattern selections, role/action mappings, scope parameters, schedules, thresholds, or policy values | Platform-owned actor class, envelope type, or arbitrary deployer code |
| `actor_ref` | Reference identifying who or what authored the event | Permission grant, role class, product persona, or complete authentication identity |
| System actor | Platform-owned author convention such as `system:{source_type}/{source_id}` | Hidden human actor, tenant identity, or deployer-authored authority source |
| `subject_ref` | Reference identifying what an event is about | Ownership of all lifecycle, authority, matching, workflow, or reporting behavior for that referent |
| Record reference | Reference to a record-like entity where a shape or boundary requires record identity | Mutable canonical record truth |
| Typed reference | A reference whose category and contract are meaningful to the platform boundary consuming it | Arbitrary foreign key with implicit lifecycle ownership |
| Raw reference | The original reference value written into an event | A reference rewritten after identity evolution |
| Resolved reference | Projection-derived interpretation of a raw reference after identity or lineage processing | Replacement of historical event references |
| Causal reference | Link from an event to a source or cause where required by the event shape or boundary | Universal workflow state, pattern reference, or canonical projection state |
| Device identity | Hardware or app-installation namespace used for device ordering and sync semantics | Actor identity |
| Projection | Derived state or read model computed from events plus relevant configuration and pattern definitions | Canonical truth or directly patchable source of state |
| Read model | Materialized view, cache, summary, report, queue, status view, or query shape derived from events | Source of operational truth |
| Projection rebuild | Recomputing derived state from the available event subset and relevant configuration | Rewriting history or repairing canonical state by patching projections |
| Event subset | Events available to a server or device under sync, access, and local lifecycle constraints | Complete global history on every device |
| Workflow state | Projection-derived state computed from immutable event sequences plus pattern definitions | Stored canonical event state or `current_state` envelope field |
| Pattern | Platform-fixed workflow skeleton that deployers select and parameterize | Deployer-authored state-machine code or product queue |
| Pattern Registry | Platform-owned registry of workflow pattern mechanisms | Final pattern inventory or formal schema unless a later atom accepts it |
| Participant capacity | Role-like capacity inside a pattern, such as capturer or reviewer | Platform actor subclass |
| Configuration | Bounded deployer selection of values inside platform-owned mechanisms | General-purpose programming language or authority to add platform primitives |
| Deployer parameterization | Selecting names, bindings, scopes, thresholds, severities, schedules, pattern bindings, sensitivity levels, or policy values inside bounded mechanisms | Deployer-authored envelope fields, type values, access-control programs, scope logic, state-machine mechanisms, or arbitrary detector logic |
| Role label | Product or deployer vocabulary mapped through configuration, assignments, capacities, or UI | Platform actor subclass |
| Product surface | UI, queue, dashboard, review list, work item, status label, or product-facing workflow view | Canonical storage primitive or architecture boundary |
| Assignment | Platform-recognized relation used to derive access/sync scope through assignment timeline, scope, and role/configuration context | External IdP claim, group membership, product role label, or immutable event authority snapshot |
| Access scope | Assignment-derived visibility or authority scope | Independent entitlement model unrelated to sync scope |
| Sync scope | The access scope used to filter immutable event delivery | Separate sync-only permission model |
| Authority projection | Reconstructed authority from actor, original subject/process references, assignment timeline, event creation context, and sync knowledge state | Stored immutable `authority_context` |
| Original subject authorization | Authorization check against the original subject reference written into the event | Authorization shortcut through post-merge alias projection |
| Conflict | A surfaced inconsistency or anomaly requiring detection and, where applicable, judgment before downstream effects | Automatic merge, last-write-wins, or proof that the source event must be rejected |
| Flag | Surfaced marker of invalid, stale, conflicting, or otherwise constrained interpretation | Canonical mutation of the source event or complete general flag lifecycle |
| Resolution | Later accepted event or process that changes interpretation/projection of prior facts | Rewriting, deleting, or mutating historical events |
| Source event | Event that caused or directly anchors a flag, review, task, or derived effect | Downstream projected copy stored as a second canonical fact |
| Source chain | Traversal through source links where a closed workflow case requires it | Open-ended universal traversal rule or accepted depth policy |
| Detect-before-act | Checks for conflict and authorization run before downstream policy or workflow effects | Global rejection of stale work at write time |
| Stale or invalid work | Validly structured work accepted as factual history but surfaced with flags where baseline rules require | Clean workflow progress or invisible automatic correction |
| Operation class | Architecture lens for offline-capable, online/coordination-required, offline-with-constraints, or configuration/control-plane operations | Product persona or role hierarchy |
| Local data lifecycle | Device-side retain, remove, purge, archive, or summarize behavior under access, sensitivity, storage, and lifecycle constraints | Mutation of central canonical event history |
| Retain-and-hide | A local lifecycle strategy that keeps data locally but hides it from ordinary use | Sufficient handling for sensitive deployments |
| Change control | Formal process required to alter accepted baseline semantics or reopen rejected paths | Informal wording change or implementation convenience |
| Hold-back | Known unresolved area atomization must avoid deciding accidentally | Permission to implement or ignore the issue |
| Rejected path | Design direction the accepted baseline or control overlay says not to use | Naming preference or impossible future change |

## Invariants

- Glossary terms clarify accepted baseline vocabulary; they do not add platform behavior.
- Event-log truth, envelope processing, payload shape, references, projection state, assignment-derived authority, and deployer configuration remain separate axes.
- Product labels, role labels, queues, dashboards, work items, and UI statuses do not become platform classes or canonical storage primitives by being named here.
- Draft glossary wording must not close open decisions from `90-open-decisions.md`.
- Rejected meanings in this glossary remain review triggers for downstream atoms and implementation designs.
- Any definition that would add an envelope field, type value, authority source, canonical projection state, or deployer-authored platform logic requires change control.

## Contracts

### Inputs

- accepted baseline vocabulary from ADR-001 through ADR-005 closure
- envelope, shape, activity, pattern, and parameterization definitions from the atomization definition file
- control rules for keeping type, shape, activity, references, configuration, projections, and product vocabulary separate
- open-decision and rejected-path registers as dependency checks

### Outputs

- stable glossary terms for downstream platform-spec atoms
- forbidden meanings for common drift-prone terms
- routing table for definition consumers
- change-control triggers for definition drift

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Log / Storage | canonical truth, event log, operational fact, projection definitions | Storage owns behavior; this atom owns shared words only. |
| Event Envelope / Schema | envelope, type, payload, timestamp, shape/activity/actor/subject reference terms | Envelope atom must define field contract details; this glossary only fixes meaning boundaries. |
| Identity / Lineage | raw reference, resolved reference, subject reference, original-reference terms | Identity atom owns lineage behavior and merge/split semantics. |
| Assignment / Authority / Sync | assignment, access scope, sync scope, authority projection, original subject authorization terms | Assignment atom owns access and sync behavior. |
| Configuration | shape, activity, deployer parameterization, role label, bounded configuration terms | Configuration atom owns package, validation, and policy surfaces. |
| Projection / Workflow State | projection, workflow state, pattern, Pattern Registry, participant capacity terms | Workflow atom owns pattern mechanics and projection behavior. |
| Flag / Resolution | conflict, flag, resolution, source event, source chain terms | Flag atom owns lifecycle only where accepted or later specified. |
| Reporting / Aggregation | read model, product surface, freshness-related derived state terms | Reporting atom owns freshness and aggregate behavior. |
| Local Data Lifecycle | event subset, retain-and-hide, local lifecycle terms | Local lifecycle atom owns device-side retain/remove rules. |

## Definition Routing Matrix

| Question | Route |
|---|---|
| Which platform pipeline processes this event? | Envelope `type` |
| What fact schema and version does the payload carry? | `shape_ref` |
| What is the event about? | `subject_ref` or another typed reference owned by the relevant boundary |
| Who or what authored the event? | `actor_ref` |
| Which configured activity produced the event? | `activity_ref` |
| Which workflow state is visible now? | projection plus pattern definition |
| Who may see or act on the event? | assignment-derived access and sync scope |
| Which role label appears in a deployment or UI? | deployer/product vocabulary mapped through configuration, assignments, and capacities |
| Is something canonical? | event log, unless a formal baseline change says otherwise |
| Is something a product queue, status, work item, or dashboard item? | projection/product surface, not canonical storage |

## Allowed Extension Points

- Later atoms may narrow a term for their own boundary if they preserve this glossary's forbidden meanings.
- New glossary terms may be added when a downstream atom exposes repeated cross-boundary vocabulary.
- Platform-bundled shapes may be defined later only as explicit exceptions for platform-owned facts; Pattern Registry entries may be defined later by their owning atoms without changing the meaning of `shape_ref` or pattern.
- Product and deployer labels may vary by deployment if they map back to platform-owned mechanisms.

## Forbidden Couplings

- Do not use `type` as a domain-event taxonomy.
- Do not use `shape_ref` as workflow state, authority, product surface, role, tenant, deployment, or online/offline marker.
- Do not treat platform-bundled shapes as a general platform-owned domain schema catalog.
- Do not use `activity_ref` as authority snapshot, pattern identity, tenant/deployment identity, or work-item identity.
- Do not infer authority from `actor_ref`, role labels, groups, accounts, IdP claims, tenant context, or deployment context.
- Do not treat projections, reports, queues, dashboards, work items, or status labels as canonical truth.
- Do not treat review as one fixed subsystem: distinguish `type=review`, review payloads, review patterns, review queues, and reviewer labels.
- Do not treat deployer parameterization as permission to author platform code, access-control logic, envelope fields, type values, or state-machine mechanisms.
- Do not use local lifecycle behavior to mutate central canonical event history.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Final event-envelope field specification and serialization details | `04-event-envelope-schema.md` | Envelope atom drafting begins. |
| Rules for any platform-owned fact shapes and final platform-bundled shape inventory | Event Envelope / Schema plus owning behavior atoms | A later atom needs to specify a bundled platform fact shape. |
| Exact Pattern Registry inventory and formal schema | `09-projections-workflow-and-patterns.md` plus Configuration | Workflow atom needs normative pattern skeletons or serialized schema. |
| General flag semantics outside closed workflow cases | `10-conflict-flag-and-resolution.md` and `90-open-decisions.md` | A later atom needs non-workflow flag creation, blocking, lifecycle, or auto-resolution semantics. |
| Subject-based scope, auditor access, shared-device actor scope, and temporary authority policy | `07-assignment-authority-and-sync.md` and `90-open-decisions.md` | Assignment/sync atom or first implementation slice requires those cases. |
| Reporting freshness vocabulary | `12-reporting-aggregation-and-freshness.md` | Reporting atom or implementation needs normative freshness semantics. |
| Sensitive local lifecycle vocabulary beyond retain-and-hide rejection | `08-local-data-lifecycle.md` | Sensitive deployment or scope contraction behavior must be implemented. |

## Rejected Paths

- Defining product personas as platform actor subclasses.
- Defining queues, dashboards, work items, or review lists as canonical storage.
- Treating `type`, `shape_ref`, references, activity context, pattern, projection, and product vocabulary as interchangeable.
- Adding glossary definitions that close open decisions without routing through change control.
- Borrowing SPEC-003 storage wording as source authority for terms instead of using the accepted baseline and definition overlays.

## Implementation Implications

- Implementation designs should cite these terms when naming event, projection, configuration, authority, and product-surface concepts.
- Schema, API, database, and UI names may be more specific than this glossary, but must not invert the ownership or forbidden meanings here.
- Downstream specs should define boundary-specific behavior by consuming these terms, not by redefining them.

## Review Checklist

- [x] Source basis is named and cited.
- [x] Owner and boundary are singular.
- [x] Scope and non-scope are explicit.
- [x] Contracts identify inputs, outputs, and boundary crossings.
- [x] Open gaps are not closed accidentally.
- [x] Forbidden couplings include likely definition drift risks.
- [x] No envelope field, type value, authority shortcut, or canonical projection state was added without change control.
- [x] Product labels, role labels, and UI surfaces remain outside platform-core semantics.

Architecture Steward review, 2026-05-10:

- Pass for draft status. This atom defines shared terms only and does not introduce behavior, close open gaps, or draft envelope field contracts.
- SPEC-004 remains undrafted; envelope field contracts and schema details are intentionally left to `04-event-envelope-schema.md`.
