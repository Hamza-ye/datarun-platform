# Configuration And Parameterization

Status: Draft
Owning boundary: Configuration
Primary owner: Architecture Steward

Source basis:

- `../../professional-baseline/04-architecture-baseline-v0.md`
- `../../professional-baseline/05-decision-gap-register.md`
- `../../professional-baseline/07-system-boundary-map.md`
- `../../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../../professional-baseline/18-envelope-shape-parametrization-boundary-control.md`
- `../../professional-baseline/19-envelope-shape-parametrization-definitions.md`

Depends on:

- `00-specification-source-authority.md`
- `01-core-definitions-and-boundary-vocabulary.md`
- `03-event-envelope-schema-and-references.md`
- `05-references-and-identity-lineage.md`
- `90-open-decisions-and-gap-register-citations.md`
- `91-rejected-alternatives.md`

Consumed by:

- `07-assignment-authority-and-sync.md`
- `09-projections-workflow-and-patterns.md`
- `10-conflict-flag-and-resolution.md`
- `11-trigger-reactivity.md`
- implementation designs for configuration packaging, validation, shape registries, activity setup, policy-value management, and deployer administration

## Purpose

This section defines the narrow Configuration boundary for bounded deployer parameterization. It lets deployments vary shapes, activities, labels, bindings, schedules, thresholds, severities, sensitivity classifications, and policy values inside platform-owned mechanisms without letting configuration redefine the event envelope, authority model, workflow mechanisms, reference lineage, or platform code.

## Scope

This section owns:

- deployer-defined shapes and shape versions as payload schemas identified by `shape_ref`
- deployer-configured activity instances referenced by optional `activity_ref`
- bounded activity bindings to shapes, pattern selections, role/action mappings, scopes, schedules, thresholds, deadlines, severities, sensitivity classifications, and policy values
- configuration package constraints needed to preserve atomic deployment and bounded validation
- deployer identifier naming constraints where they protect `shape_ref`, activity identifiers, and configuration package integrity
- configuration-side validation that prevents deployer values from becoming envelope fields, event type values, authority sources, state-machine mechanisms, arbitrary detector logic, or platform code
- Configuration-side handoffs to Assignment / Authority / Sync, Projection / Workflow State, Flag / Resolution, Trigger / Reactivity, Reporting / Aggregation, and Local Data Lifecycle

## Non-Scope

This section does not own:

- event-envelope fields, structural reference categories, structural `type` values, or envelope serialization
- platform-owned structural event processing semantics
- referent registration, referent attributes, subject lifecycle, subject lineage, or descriptive subject profiles
- assignment lifecycle, access scope semantics, sync scope semantics, authority reconstruction, permission-table closure, or authorization policy
- state-machine mechanisms, workflow state, Pattern Registry inventory, or formal Pattern Registry schema
- conflict detection algorithms, flag lifecycle, resolution-event mapping, source-chain traversal policy, or auto-resolution monitoring
- arbitrary deployer-authored platform logic, access-control programs, scope containment logic, state-machine code, or detector code
- platform-bundled shape inventory or a general platform-owned domain schema catalog
- configuration authoring format, deployment packaging UX, deploy-time validator UX, migration tooling, or stale-configuration reconciliation details
- reporting freshness semantics, product queues, dashboards, work items, or local data lifecycle behavior

## Definitions

| Term | Meaning In This Section | Must Not Mean |
|---|---|---|
| Configuration | Bounded deployer selection of values inside platform-owned mechanisms | General-purpose programming language, platform extension runtime, or architecture authority |
| Deployer parameterization | Selection of names, bindings, scopes, thresholds, severities, schedules, sensitivity classifications, pattern bindings, role/action mappings, or policy values inside accepted limits | Permission to author envelope fields, event type values, access-control programs, state-machine mechanisms, detector algorithms, or platform code |
| Configuration package | Atomic deployer configuration unit that groups bounded definitions and policy values for deployment validation and distribution | Final versioning model, migration UX, stale-work reconciliation policy, or mutable event truth |
| Shape definition | Deployer-defined typed payload schema and version identified by `shape_ref` unless a later section accepts a platform-bundled shape | Envelope `type`, workflow state, authority marker, platform-owned domain catalog, or referent lifecycle owner |
| Activity definition | Deployer-configured operational instance that may bind shapes, pattern selections, role/action mappings, scope parameters, schedules, deadlines, thresholds, severities, sensitivity classifications, and policy values | Tenant/deployment identity, work-item identity, assignment authority, pattern identity, or immutable authority context |
| Role label | Deployer or product vocabulary mapped through configuration and later consumed by assignment, pattern capacity, or product surfaces | Platform actor subclass, direct permission grant, envelope type, or authority source |
| Role/action mapping | Bounded configuration declaration that maps deployer role labels or capacities to platform-owned action categories where a later authority section consumes them | Final permission table, direct access-control program, or complete authority policy |
| Policy value | Deployer-selected value inside a platform-owned mechanism, such as severity, threshold, deadline, sensitivity classification, or uniqueness constraint | New platform mechanism, arbitrary rule program, or event-envelope field |
| Sensitivity classification | Shape-level or activity-level policy value consumed by authority, local lifecycle, reporting, or operational policy where later sections define effects | Field-level sensitivity mechanism, regulatory framework implementation, or automatic access bypass |
| Platform-bundled shape | Narrow platform-supplied payload schema for an explicitly platform-owned fact, if later accepted by the owning section | General platform-owned domain schema catalog or new structural event type |

## Invariants

- Configuration is bounded deployer parameterization over platform-owned mechanisms.
- The platform owns structural event type vocabulary and processing semantics. Configuration must not add, remove, rename, or reinterpret the six accepted structural `type` values from `SPEC-004`.
- Every event payload schema is addressed through `shape_ref` using the accepted `SPEC-004` meaning and format. Configuration must not use `shape_ref` as workflow state, authority marker, product surface, role label, tenant identity, deployment identity, or online/offline marker.
- `activity_ref` remains optional event context for a configured activity instance. Configuration must not use `activity_ref` as immutable `authority_context`, pattern reference, tenant/deployment reference, assignment authority, or product queue identity.
- Shapes are deployer-defined by default. Platform-bundled shapes remain narrow exceptions owned by the relevant behavior section and are not accepted as a final inventory here.
- Activity definitions may bind platform-owned mechanisms and deployer values, but they do not create new platform mechanisms.
- Role labels and role/action mappings are configuration inputs to later assignment, authority, pattern, and product-surface behavior. They are not platform actor subclasses or direct authority grants.
- Sensitivity classification is configurable only at shape or activity level under the accepted baseline. Field-level sensitivity remains rejected unless formally reopened.
- Configuration packages must be validated before they are treated as deployable configuration, but final authoring format, deployment UX, validator UX, package versioning, and stale-configuration reconciliation remain open.
- Structurally valid offline work created under older configuration must not be retroactively invalidated solely because newer configuration has synced. Exact stale-configuration reconciliation remains open.
- Configuration does not own referent lifecycle. Referent registration, attributes, catalogs, and final reference serialization remain routed through the open gaps and owning boundaries.
- Configuration cannot close authority, sync, workflow, flag, reporting, or local lifecycle gaps by providing values those later sections have not accepted.

## Contracts

### Inputs

- accepted envelope meanings for structural `type`, `shape_ref`, optional `activity_ref`, payload, references, and system actor convention from Event Envelope / Schema
- accepted glossary definitions for configuration, deployer parameterization, shape, activity, pattern, role label, and policy value
- accepted Identity / Lineage constraints on raw references, subject-lineage ownership, and referent lifecycle non-ownership
- platform-owned mechanism definitions from later accepted sections, including assignment/scope mechanisms, pattern mechanisms, flag categories, trigger capabilities, reporting constraints, and local lifecycle effects where applicable
- deployer-supplied shape definitions, activity definitions, labels, bindings, schedules, thresholds, severities, sensitivity classifications, and policy values
- open-decision and rejected-path registers for validation against unresolved gaps and forbidden encodings

### Outputs

- validated deployer shape definitions and shape-version references usable as `shape_ref`
- validated activity definitions usable as optional `activity_ref` values
- bounded role/action mapping declarations for later authority and workflow consumers
- bounded schedule, threshold, deadline, severity, sensitivity-classification, and policy-value declarations
- configuration package validation results that distinguish deployable bounded configuration from rejected configuration drift
- handoff artifacts for downstream sections that consume configuration without treating draft configuration behavior as accepted authority

### Boundary Crossings

| Crosses To | Through | Notes |
|---|---|---|
| Event Envelope / Schema | `shape_ref`, optional `activity_ref`, and payload schema validation | Configuration supplies shape and activity definitions but cannot add envelope fields, type values, or reference categories. |
| Identity / Lineage | shape/reference needs and possible future catalog policy values | Configuration does not own subject lineage, referent registration, referent attributes, or raw/resolved reference behavior. |
| Assignment / Authority / Sync | role labels, role/action mappings, scope parameters, activity context, schedules, sensitivity classifications, and policy values | Authority remains assignment-derived and projection-derived. Configuration does not provide direct authority grants or access-control code. |
| Projection / Workflow State | pattern selections, shape roles, activity bindings, and policy values | Pattern mechanisms, workflow state, Pattern Registry inventory, and formal Pattern Registry schema remain owned by Projection / Workflow State. |
| Flag / Resolution | severity values, threshold values, uniqueness policy values, and detector parameters where later accepted | Configuration may supply bounded values; it does not own arbitrary detector logic, flag lifecycle, or resolution semantics. |
| Trigger / Reactivity | bounded schedules, thresholds, trigger declarations, and server-only constraints where later accepted | Trigger capabilities and side effects remain owned by Trigger / Reactivity and must preserve event-store write discipline. |
| Reporting / Aggregation | shape/activity labels, sensitivity classifications, and policy values consumed by reporting projections | Reports remain derived and access-constrained; configuration does not define freshness semantics. |
| Local Data Lifecycle | sensitivity classifications and package changes consumed by local lifecycle behavior | Local retain/remove/purge behavior remains owned by Local Data Lifecycle and must not mutate central event history. |

## Allowed Extension Points

- Deployers may add shape definitions and shape versions within accepted naming, validation, and package constraints.
- Deployers may configure activity instances and bind them to shapes, pattern selections, role/action mappings, scope parameters, schedules, thresholds, deadlines, severities, sensitivity classifications, and policy values inside accepted limits.
- Deployers may choose product-facing labels if they map back to platform-owned mechanisms and do not become platform actor classes, envelope types, or canonical storage primitives.
- Later sections may define additional platform-owned mechanisms that configuration can parameterize, provided they preserve the platform/deployer responsibility boundary.
- Implementation tooling may choose authoring, packaging, validation, and migration UX later, provided it preserves atomic configuration packages, bounded validation, and older-configuration coexistence constraints.

## Forbidden Couplings

- Do not let deployers author envelope fields, structural `type` values, structural reference categories, platform code, access-control programs, scope containment logic, state-machine mechanisms, or arbitrary detector logic.
- Do not treat configuration as a general-purpose programming language or platform extension runtime.
- Do not treat deployer-defined shapes, attributes, or catalogs as a platform-owned domain schema catalog.
- Do not use platform-bundled shapes as a back door for new envelope `type` values or broad platform domain models.
- Do not use `shape_ref` as workflow state, authority marker, product surface, role label, tenant identity, deployment identity, or online/offline marker.
- Do not use `activity_ref` as immutable `authority_context`, pattern identity, tenant/deployment reference, assignment authority, or work-item identity.
- Do not infer authority from role labels, accounts, groups, identity-provider claims, tenant context, deployment context, or configured labels.
- Do not make field-level sensitivity a platform mechanism.
- Do not retroactively force in-progress offline work created under older configuration to obey newly synced configuration by implication.
- Do not make configuration own assignment lifecycle, sync delivery, workflow state, Pattern Registry inventory, flag lifecycle, reporting freshness, local data lifecycle, referent lifecycle, or final reference serialization.

## Open Gaps

| Gap | Owner / Route | Reopen Trigger |
|---|---|---|
| Configuration versioning and stale-configuration reconciliation | Configuration plus Event Envelope / Schema and Assignment / Authority / Sync | Configuration packages, shape/version coexistence, offline work under older configuration, or migration behavior must be specified. |
| Configuration authoring and deploy-time validation UX | Configuration plus implementation tooling | Admin/configuration authoring, packaging, deploy-time validator UX, or breaking-change migration tooling is selected for implementation. |
| Event schema/versioning tooling | Event Envelope / Schema plus Event Log / Storage and Configuration | Validation, schema registry, migration, or mixed-version event handling must be implemented. |
| Platform-bundled shape inventory | Owning behavior sections plus Event Envelope / Schema and Configuration | A platform-owned fact needs a normative bundled shape or payload schema. |
| Formal Pattern Registry schema and exact inventory | Projection / Workflow State plus Configuration | Workflow section or implementation needs serialized pattern definitions, validation tooling, or normative pattern skeletons. |
| Permission/activity authority details | Assignment / Authority / Sync plus Configuration | Concrete permission matrices, role/action bindings, activity/context authority semantics, or policy values must become normative. |
| Referent registration, attributes, and catalogs | Event Envelope / Schema, Identity / Lineage, Configuration, Projection / Workflow State, and Assignment / Authority / Sync | A spec needs subject registration events, descriptive attribute mutation/projection, deployer-defined catalogs, or lifecycle ownership for non-subject referents. |
| Final reference serialization and active emission sites | Event Envelope / Schema plus Identity / Lineage, Assignment / Authority / Sync, Configuration, Projection / Workflow State, and owning behavior sections | A later section or implementation needs canonical field names, placement, cardinality, or required emission sites for subject, causal, process, assignment, or other typed-reference values. |

## Rejected Paths

- Treating deployer configuration as arbitrary platform code.
- Letting deployers author envelope fields, event type values, scope containment logic, state-machine mechanisms, access-control programs, or arbitrary detector logic.
- Treating deployer-defined attributes, catalogs, or shapes as a platform-owned domain schema catalog.
- Retroactively forcing in-progress offline work created under older configuration to follow newly synced configuration rules.
- Treating `shape_ref` as workflow state, authority marker, product surface, online/offline class, role label, tenant identity, or deployment identity.
- Treating `activity_ref` as immutable authority context, pattern identity, tenant/deployment reference, or work-item identity.
- Making role labels into platform actor subclasses.
- Making field-level sensitivity a platform mechanism.
- Using configuration to close authority, sync, workflow, flag, reporting, local lifecycle, referent registration, or reference serialization gaps by implication.

## Implementation Implications

- Configuration tooling should validate identifiers, shape references, activity references, package consistency, and forbidden encodings before a package is deployable.
- Event creation code should consume deployed shape and activity definitions without allowing deployers to change the envelope contract.
- Authority, workflow, flag, trigger, reporting, and local lifecycle implementations should consume configuration as bounded input, not as executable platform behavior.
- Implementations should plan for shape/version coexistence and older-configuration offline work, while leaving exact stale-configuration reconciliation to the named gap.
- Tests should include negative cases proving deployer configuration cannot add envelope fields, event types, authority shortcuts, field-level sensitivity, arbitrary detector logic, or platform code.

## Review Checklist

- [ ] Source basis is accepted and cited.
- [ ] Owner and boundary are singular.
- [ ] Scope and non-scope are explicit.
- [ ] Contracts identify inputs, outputs, and boundary crossings.
- [ ] Open gaps are not closed accidentally.
- [ ] Forbidden couplings include the likely drift risks.
- [ ] No envelope field, type value, authority shortcut, or canonical projection state was added without change control.
- [ ] Product labels, role labels, and UI surfaces remain outside platform-core semantics.

Drafting Agent note, 2026-05-12:

- This draft intentionally carries forward unresolved configuration versioning, stale-configuration reconciliation, authoring and deploy-time validation UX, event schema/versioning tooling, platform-bundled shape inventory, Pattern Registry schema/inventory, permission/activity authority details, referent catalog gaps, and final reference serialization/emission-site gaps.

Challenge Review, 2026-05-12:

- Verdict: Pass With Notes. No rejected-path reintroduction or change-control trigger found.
- Draft checklist remains unchecked by design; checklist completion is deferred to the future acceptance workflow.
- Forbidden-path terms appear only as constraints, rejected paths, or non-scope constraints.

Integration Review, 2026-05-12:

- Verdict: Carry Explicit Gap. `SPEC-006` is consumable by planned downstream sections only with explicit gaps preserved.
- `SPEC-007` can consume configuration inputs for authority work only if role labels, role/action mappings, activity context, scope parameters, schedules, sensitivity classifications, and policy values remain inputs to assignment-derived authority rather than grants.
- `SPEC-009` can consume pattern selections, shape roles, and activity bindings while carrying Pattern Registry inventory/schema, projection compatibility, and platform-bundled shape gaps.
- `SPEC-010` can consume configured severities, thresholds, uniqueness values, and bounded detector parameters only if detector logic, flag lifecycle, and resolution semantics remain outside Configuration.
- `SPEC-011` can consume bounded schedules, thresholds, trigger declarations, and server-only constraints only if Trigger / Reactivity owns execution semantics, side effects, timing, and event-store write discipline.
- No `SPEC-006` edits or change-control escalation are required before later acceptance workflow.
