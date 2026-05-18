# Decision Gap Register

Status: Accepted gap register from Architecture Baseline v0

This register lists items that remain unresolved after the ADR-001 through ADR-005 baseline. It is not a backlog and not an implementation plan. Its purpose is to prevent later documents from quietly closing gaps without classification and review.

Primary references:

- `04-architecture-baseline-v0.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `02-change-control.md`

## Gap Classification

- Architecture decision gap: needs a formal architecture/platform decision.
- Platform-spec detail gap: needs specification detail, but the architecture boundary is already closed.
- Implementation/tooling gap: needs implementation design, tooling, UX, or operational mechanics.
- Operational policy gap: deployment or organization policy, not core architecture.
- Product validation gap: needs scenario/product validation before it should drive platform evolution.
- Later-source assessment gap: ADR-006-R through ADR-009 may contain relevant claims, but those claims must be assessed through change control.

## Closure Paths

- Formal architecture decision: write a focused decision memo or ADR that explicitly names the baseline item it changes or extends.
- Platform-spec detailing: write platform-spec language under the existing architecture boundary; no new ADR unless the detail changes the boundary.
- Implementation/tooling design: write an engineering design, prototype, or tickets; preserve the baseline constraints.
- Operational policy definition: define product/operations policy; escalate only if policy requires architecture change.
- Product validation: validate with target scenarios or deployments; escalate only if validation shows the baseline primitives are insufficient.
- Later-source assessment: classify later ADR claims through `02-change-control.md`; do not absorb them directly.
- No action until product need appears: keep the gap visible but do not spend engineering time until a concrete need appears.

## Register Control

This file is the canonical register for open decision state. It owns gap classification, primary responsibility ownership, affected responsibility areas, closure path, priority, platform-spec handling, and hold-back reopen triggers.

`07-system-boundary-map.md` defines responsibility routing and cross-boundary contracts. It must not maintain a second open-gap register. If ownership changes, update this register first and then update responsibility language only where the responsibility definition itself changes.

ADR-006-R through ADR-009 remain assessed input, not baseline authority. Their useful material enters platform-spec work through one of three lanes:

- consistent elaboration of a settled baseline rule, used as platform-spec constraint language
- open-gap closure candidate, kept under the owning gap below
- hold-back or formal-reopen candidate, kept visible without becoming normal platform behavior

## Open Gap Ownership Index

This index is the only canonical gap-ownership index in the professional baseline. Detailed rationale remains in the individual gap entries.

| Gap | Primary Responsibility Area | Affected Areas | Platform-Spec Handling |
|---|---|---|---|
| Domain conflict automation outside workflow | Flag / Resolution | Projection / Workflow State; Configuration | Open decision unless the platform spec only states ADR-005 workflow-specific behavior. |
| Subject-based scope and auditor access | Assignment / Authority / Sync | Reporting / Aggregation; Local Data Lifecycle | Open decision if first target deployment needs these access paths. |
| Shared device actor scope | Assignment / Authority / Sync | Identity / Lineage; Event Envelope / Schema | Open decision unless shared devices are explicitly out of initial scope. |
| Temporary authority and offline revocation reconciliation | Assignment / Authority / Sync | Flag / Resolution; Projection / Workflow State; Local Data Lifecycle | Policy/spec detail under existing mechanisms unless new scope, envelope, or projection semantics are needed. |
| Alias-cycle enforcement and resolution semantics | Identity / Lineage | Flag / Resolution; Projection / Workflow State; Event Log / Storage | Open decision before identity or flag sections claim cycle-closing behavior. |
| Authorization visibility and role-action detail surfaces | Assignment / Authority / Sync | Configuration; Reporting / Aggregation; Local Data Lifecycle | Platform-spec or policy detail under existing assignment/scope mechanisms; formal decision for new scope or authority semantics. |
| Operational actor vocabulary and operation-class routing | Assignment / Authority / Sync | Configuration; Projection / Workflow State; Reporting / Aggregation | Spec constraint: product/deployer labels are not platform actor subclasses. |
| Envelope type, shape ref, references, and parametrization boundary | Event Envelope / Schema | Configuration; Projection / Workflow State; Assignment / Authority / Sync | Spec constraint: keep `type`, `shape_ref`, references, mechanisms, instances, and product labels separate. |
| Process reference and process lifecycle semantics | Projection / Workflow State | Event Envelope / Schema; Identity / Lineage; Assignment / Authority / Sync | Platform-spec detail only if process identity, pending-match, or process lifecycle behavior is in scope. |
| Exact Pattern Registry inventory | Projection / Workflow State | Configuration; Flag / Resolution | Platform-spec detail. Do not convert inventory choices into new event-envelope structure. |
| Formal Pattern Registry schema format | Projection / Workflow State | Configuration | Platform-spec detail plus implementation/tooling design. |
| Source-chain traversal limits | Flag / Resolution | Projection / Workflow State | Platform-spec detail under ADR-005 source-only flag lineage. |
| Bounded context expression details | Projection / Workflow State | Configuration | Platform-spec detail under the closed ADR-005 `context.*` surface. |
| Projection performance and caching | Event Log / Storage | Projection / Workflow State | Implementation/tooling design; no architecture decision unless source-of-truth rules change. |
| Low-end device scale and offline performance | Event Log / Storage | Projection / Workflow State; Assignment / Authority / Sync; Local Data Lifecycle | Implementation/tooling design and reference-device validation; formal decision only if canonical event, projection, sync, or lifecycle semantics change. |
| Event schema and versioning tooling | Event Envelope / Schema | Event Log / Storage; Configuration | Implementation/tooling design plus platform-spec versioning obligations. |
| Structured import/export compatibility | Event Envelope / Schema | Event Log / Storage; Configuration; Reporting / Aggregation | Compatibility obligation only; formal decision if external schemas redefine platform records. |
| Configuration authoring and deployment UX | Configuration | Trigger / Reactivity | Implementation/tooling design under bounded configuration rules. |
| Auto-resolution authoring and monitoring | Flag / Resolution | Reporting / Aggregation | Implementation/tooling design; platform-spec detail only for required audit surfaces. |
| Sync delivery mechanics | Assignment / Authority / Sync | Local Data Lifecycle | Implementation/tooling design under immutable, scope-filtered sync. |
| Retention and archival | Local Data Lifecycle | Event Log / Storage; Reporting / Aggregation | Operational policy; formal decision if canonical event history changes. |
| Sensitive data policy and local lifecycle | Local Data Lifecycle | Configuration; Assignment / Authority / Sync; Event Log / Storage | Operational policy and implementation detail under shape/activity sensitivity unless access, envelope, or canonical history changes. |
| Setup experience and onboarding | Configuration | Assignment / Authority / Sync; Local Data Lifecycle | Product/operations and tooling unless onboarding changes authority or configuration semantics. |
| Reporting and aggregation | Reporting / Aggregation | Assignment / Authority / Sync; Projection / Workflow State | Platform-spec detail plus implementation/tooling and policy decisions. |
| Domain-agnostic proof gap | Configuration | Projection / Workflow State; Reporting / Aggregation | Product validation; formal architecture decision only if validation shows new primitives are required. |
| General flag semantics | Flag / Resolution | Identity / Lineage; Assignment / Authority / Sync; Projection / Workflow State; Reporting / Aggregation | Open decision for non-workflow flag lifecycle, identity, category, and resolution behavior. |

## Architecture Decision Gaps

### Domain Conflict Automation Outside Workflow

Classification: Architecture decision gap

Affected baseline:

- Detect-before-act and accept-and-flag discipline
- Configuration boundary
- Workflow flag lineage and auto-resolution boundary

Why open:

ADR-005 closes workflow `transition_violation`, resolvability classification, and L3b auto-resolution. It does not close general domain conflict resolution automation.

Later-source assessment:

ADR-006-R through ADR-009 may contain relevant claims. Classify them as open-gap closure candidates, conflicts, or implementation details before accepting any change.

Closure path:

- Formal architecture decision if general domain conflict automation is needed beyond ADR-005 workflow auto-resolution.
- Later-source assessment for any ADR-006-R through ADR-009 claim that appears to close or redefine this area.

Priority:

- P1 if platform spec must define non-workflow conflict behavior.
- P2 if implementation can start with ADR-005 workflow-only handling and defer general automation.

### Subject-Based Scope And Auditor Access

Classification: Architecture decision gap

Affected baseline:

- Authorization and sync
- Immutable event sync and access-scoped delivery

Why open:

ADR-003/ADR-004 leave subject-based scope, auditor access, sensitive-subject policy beyond shape/activity-level sensitivity classification, and related visibility exceptions open or policy-owned.

Later-source assessment:

Later ADRs may elaborate access/visibility only if they do not contradict assignment-derived access and sync scope as access scope.

Closure path:

- Formal architecture decision if subject-based or auditor access requires new scope semantics.
- Operational policy definition if it can be expressed with existing assignment, role, capability, and sync-scope mechanisms.

Priority:

- P1 if auditor or subject-based scope is in first target deployment.
- P3 if no near-term deployment needs it.

### Shared Device Actor Scope

Classification: Architecture decision gap

Affected baseline:

- Authorization and sync
- Identity and references

Why open:

The baseline separates hardware/app-installation-bound device identity from actor-scoped access, and treats device time as advisory only. Shared-device actor scope remains unresolved because actor partitioning, session boundaries, and authorship/accountability rules are not closed.

Later-source assessment:

Assess later claims for consistency with original-subject authorization, actor assignment, immutable event authorship, `device_sequence` ordering, and `sync_watermark` concurrency detection.

Closure path:

- Formal architecture decision if shared devices are a supported platform mode.
- No action until product need appears if shared devices can be declared unsupported or limited initially.
- Implementation/tooling design if support only needs actor-partitioned local sessions under existing baseline constraints.

Priority:

- P1 if shared devices are required for initial deployments.
- P3 if deployment model assumes one actor session per device.

### Temporary Authority And Offline Revocation Reconciliation

Classification: Operational policy gap with architecture decision trigger

Affected baseline:

- Authorization and sync
- Detect-before-act and accept-and-flag discipline
- Local data lifecycle under scope changes
- Flag / Resolution if late authorization anomalies are surfaced as flags

Why open:

`../../access-control-scenario.md` requires temporary access grants, clean revocation, graceful role/responsibility changes, and offline enforcement using last-known rules. ADR-003 closes assignment-derived access, sync scope as access scope, authority as projection, additive scope expansion, and selective-retain scope contraction. It does not close the exact policy for temporary grant expiry, grace periods after revocation, role handoff windows, or how late work created under stale local authority is surfaced during sync.

Later-source assessment:

Claims may elaborate this gap only if they preserve assignment-derived access, projection-derived authority, immutable event history, original-reference authorization, and scope-filtered sync. Claims that add stored `authority_context`, group/identity-provider direct authority, arbitrary deployer access logic, or field-level sensitivity conflict with the baseline.

Closure path:

- Operational policy definition if temporary authority, revocation, and handoff behavior can be expressed with existing assignment, role, scope, activity/context, and sync-scope mechanisms.
- Platform-spec detailing for required projection inputs, sync-time anomaly surfacing, and local last-known enforcement language.
- Formal architecture decision if the policy requires new scope semantics, new envelope fields, stored authority snapshots, a general authorization-flag invariant, or a different sync/projection rule for stale local authority.

Priority:

- P1 before authorization/sync specification if the first platform spec includes campaigns, emergency cover, temporary grants, or offline revocation behavior.
- P3 if the initial platform spec can explicitly defer temporary-authority workflow and grace-period policy.

### Alias-Cycle Enforcement And Resolution Semantics

Classification: Architecture decision gap

Affected baseline:

- Client-generated identity and lineage preservation
- Detect-before-act and accept-and-flag discipline
- Closed ADR-005 workflow flag interactions and open general flag semantics

Why open:

ADR-001 through ADR-005 close subject-lineage acyclicity and online-only merge/split validation where required. ADR-006-R raises a later-source claim that a cycle-closing alias event should be accepted and surfaced as `cycle_violation`, while read-side behavior over a cyclic graph and cycle-resolution effects remain undecided.

Later-source assessment:

ADR-006-R is a valid dispute against silently treating subject-lineage acyclicity as mechanically enforced. It is not enough, by itself, to change the accepted baseline. Assess related ADR-007 through ADR-009 claims before accepting any event-shape, flag-catalog, or mechanism/configuration classification implied by the cycle-guard proposal.

Closure path:

- Formal architecture decision if alias-cycle behavior is included in the first identity or flag platform-spec sections.
- Platform-spec detailing only after the decision states whether cycle-closing alias events are accepted-and-flagged, rejected as structural invalidity, or excluded from lineage projection until resolution.
- Implementation/tooling design for graph traversal, batch handling, and tests after the architecture decision.

Priority:

- P1 before drafting identity lineage or general flag semantics if alias-cycle behavior is in scope.
- P2 if the first platform spec explicitly defers alias-cycle handling.

## Platform-Spec Detail Gaps

### Operational Actor Vocabulary And Operation-Class Routing

Classification: Platform-spec detail gap

Affected baseline:

- Authorization and sync
- Configuration boundary
- Projection and workflow
- Operational/product vocabulary pressure, not platform-spec authority

Why open:

Ground-truth and product-facing sources use operational labels such as field worker, supervisor, coordinator, administrator, regional lead, auditor, and reviewer. These labels are necessary for scenario pressure and UX design, but they can be overread as permanent platform actor classes or core responsibility types. The accepted baseline already routes authority through actors, assignments, roles, scopes, activities, time, projections, and sync scope. It does not decide a fixed responsibility taxonomy.

Later-source assessment:

Later claims may use operational labels as examples only if they preserve assignment-derived authority, bounded configuration, projection-derived state, and the mechanism/instance split. Claims that add fixed role classes, role-specific envelope fields, direct permission shortcuts, or service boundaries named after operational labels should be classified as unauthorized unless a formal decision changes the baseline.

Closure path:

- Platform-spec detailing for each section to state its operation class: offline-capable, online/coordination-required, or offline-with-constraints.
- Product/UX design can choose labels, default surfaces, and navigation treatments without changing core mechanisms.
- Implementation design may use temporary role labels for seed data or tests, but core interfaces should be named for behaviors, capabilities, or boundary responsibilities rather than persona labels.
- Formal architecture decision only if a future requirement needs fixed platform-owned actor subclasses.

Priority:

- P1 before first-slice specification so review, approval, oversight, setup, and resolution language does not harden into fixed platform classes.

### Authorization Visibility And Role-Action Detail Surfaces

Classification: Platform-spec detail gap with architecture decision triggers

Affected baseline:

- Authorization and sync
- Configuration boundary
- Reporting and aggregation
- Local data lifecycle where visibility changes affect device retention

Why open:

ADR-003 closes assignment-derived access, sync scope as access scope, original-subject authorization, and authority as projection. It leaves role-action permission tables, assessment visibility, cross-level distribution visibility, and exact visibility exceptions as configuration, policy, or implementation/specification detail unless they require new scope or authority semantics.

Later-source assessment:

Later claims may elaborate authorization inputs, role-action table shape, and visibility policy only if they preserve assignment-derived access, bounded configuration, immutable event sync, and projection-derived authority. Claims that add fixed platform role classes, stored `authority_context`, group/identity-provider direct authority, field-level sensitivity, or arbitrary deployer access logic conflict with the baseline unless formal change control reopens it.

Closure path:

- Platform-spec detailing for role-action table shape, operation classes, and authorization inputs under existing assignment/scope mechanisms.
- Operational policy definition for assessment visibility, cross-level visibility, or role-transition policy that does not require new platform semantics.
- Formal architecture decision if a requirement needs new scope types, subject-based scope semantics, auditor paths, direct group authority, stored authority snapshots, or different sync-scope behavior.

Priority:

- P1 if the first platform specification includes assessment visibility, auditor visibility, cross-level distribution visibility, or detailed role-action tables.
- P3 if initial implementation can defer those surfaces while preserving assignment-derived access.

### Envelope Type, Shape Ref, And Parametrization Boundary

Classification: Platform-spec detail gap with assessed source material available

Affected baseline:

- Event envelope
- Configuration boundary
- Projection and workflow
- Operational/product vocabulary pressure, not platform-spec authority

Why open:

ADR-004 correctly closes `shape_ref`, optional `activity_ref`, and the six-value structural event `type` vocabulary. ADR-005 reinforces that closure by adding no envelope fields or type values and by rejecting `status_changed`. Later assessments clarify related distinctions: `type` is not domain fact, reference is not referent, and platform-fixed mechanism is not deployer-configured instance.

The risk is not an unresolved architecture decision. The risk is spec-drafting drift: future specs or implementation decomposition may collapse the axes and turn domain facts, review labels, workflow states, role labels, product queues, or activity labels into new envelope types, platform classes, or service boundaries.

Later-source assessment:

Later claims may elaborate this area only if they preserve the orthogonal model:

- `type` is platform processing behavior.
- `shape_ref` is payload fact schema/version.
- `actor_ref` is authorship.
- `activity_ref` is activity context.
- patterns and projections own workflow behavior.
- assignments, scopes, and sync own authority.
- deployer labels and role bindings remain configuration/product vocabulary.

Claims that add envelope type values for domain facts, role actions, workflow states, identity/integrity facts, product work items, or offline/sync states should be classified as unauthorized unless formal change control reopens the envelope.

This entry is also the register home for ADR-007 and ADR-008 detail candidates that should not become a separate platform-spec authority layer: final reference serialization, active emission sites for typed references, platform-bundled identity/integrity/conflict/resolution shape inventory, and shape/reference discrimination rules. New envelope fields or new typed-reference categories remain architecture-grade changes unless the existing envelope contract is sufficient.

Closure path:

- Use `18-envelope-shape-parametrization-boundary-control.md` as lineage assessment material.
- Use `19-envelope-shape-parametrization-definitions.md` as candidate source material for the `01 Core Definitions And Boundary Vocabulary` platform-spec section.
- Platform-spec sections involving events, review, patterns, roles, work queues, or selected-slice behavior must cite the relevant axis rather than relying on overloaded prose.
- Formal architecture decision only if a future requirement truly needs a new envelope field, new type value, new fixed actor subclass, or new platform-owned mechanism.

Priority:

- P1 before event-envelope, review, Pattern Registry, selected-slice, authorization/sync, or implementation-decomposition specification.

### Process Reference And Process Lifecycle Semantics

Classification: Platform-spec detail gap with architecture decision trigger

Affected baseline:

- Event envelope
- Identity and references
- Projection and workflow
- Authorization and sync where process references affect authority or sync scope

Why open:

ADR-002 names `process` as one typed reference category, and later ADR-008 assessment preserves reference-versus-referent separation. The baseline does not close active emission sites for process references, process identity lifecycle, pending-match behavior, or whether any process lifecycle belongs in identity, workflow projection, or configuration.

Later-source assessment:

Later claims may use process references only as envelope/reference contracts unless a platform-spec section explicitly defines the lifecycle owner. Claims that store process lifecycle as subject-lineage identity state, add new envelope fields, or use process references as authority snapshots require formal change control.

Closure path:

- Platform-spec detailing if a workflow/process section needs process identity, pending-match behavior, or process lifecycle semantics under existing reference contracts.
- Formal architecture decision if the existing typed-reference contract is insufficient or a new envelope field/reference category is required.
- Implementation/tooling design after the lifecycle owner and emission sites are specified.

Priority:

- P1 only if the first platform specification includes process identity, pending-match, or process-scoped workflow behavior.
- P3 if initial specifications can defer active process-reference emission.

### Exact Pattern Registry Inventory

Classification: Platform-spec detail gap

Affected baseline:

- Projection and workflow

Why open:

ADR-005 settles the Pattern Registry as a platform-owned workflow primitive but leaves exact pattern inventory and skeletons undecided.

Later-source assessment:

Later claims may propose inventory, but must not change projection-derived workflow state or add envelope/event-type structure without formal reopen.

Closure path:

- Platform-spec detailing.
- Implementation/tooling design for authoring, packaging, and validation of initial pattern definitions.

Priority:

- P1 for the first platform specification.

### Formal Pattern Schema Format

Classification: Platform-spec detail gap

Affected baseline:

- Projection and workflow
- Configuration boundary

Why open:

ADR-005 leaves formal pattern schema format outside closure.

Later-source assessment:

Later claims may be deferred spec details unless they alter platform/deployer boundaries.

Closure path:

- Platform-spec detailing for the contract shape.
- Implementation/tooling design for concrete serialization, validation, and authoring.

Priority:

- P1 for the first platform specification.

### Source-Chain Traversal Limits

Classification: Platform-spec detail gap

Affected baseline:

- Workflow flag lineage and auto-resolution boundary

Why open:

ADR-005 closes source-only flagging and source-chain traversal but leaves traversal depth limits undecided.

Later-source assessment:

Claims about traversal limits should be assessed as platform-spec details unless they introduce stored derived flags or alter source-only lineage.

Closure path:

- Platform-spec detailing for normative traversal semantics and minimum required behavior.
- Implementation/tooling design for performance limits and rendering.

Priority:

- P1 if the platform spec includes source-only flag lineage.

### Bounded Context Expression Details

Classification: Platform-spec detail gap

Affected baseline:

- Bounded context expressions

Why open:

ADR-005 closes the allowed `context.*` surface but leaves caching internals and detailed execution mechanics open.

Later-source assessment:

Later claims must not expand `context.*` beyond form-only seven-value scope without formal reopen.

Closure path:

- Platform-spec detailing for evaluation timing and value semantics.
- Implementation/tooling design for caching internals.

Priority:

- P2 unless first implementation includes state-aware forms immediately.

## Product Validation Gaps

### Domain-Agnostic Proof Gap

Classification: Product validation gap

Affected baseline:

- Configuration boundary
- Projection and workflow
- Reporting and aggregation

Why open:

Earlier viability work preserved a domain-agnostic proof concern: the accepted primitives should remain capable of supporting multiple deployment domains without turning configuration into an inner platform or forcing domain-specific platform code for ordinary operations. ADR-001 through ADR-005 give strong evidence for the core model, but they do not prove every future domain will fit the same bounded surfaces.

Later-source assessment:

Later claims may provide scenario evidence or identify pressure that the baseline cannot express. They must not silently add platform primitives, new event types, broad reporting models, or arbitrary deployer code. If validation shows the current primitives are insufficient, route the exact failed pressure through formal architecture change control.

Closure path:

- Product validation against representative target deployments and selected vertical slices.
- Platform-spec detailing only for validated capabilities that fit existing architecture boundaries.
- Formal architecture decision only if validation requires new primitives or changes to event, configuration, projection, authority, or flag semantics.

Priority:

- P2 before broad implementation planning across multiple domains.
- P3 if the first implementation is intentionally narrow and the domain limits are documented.

## Implementation / Tooling Gaps

### Projection Performance And Caching

Classification: Implementation/tooling gap

Affected baseline:

- Storage model
- Projection and workflow

Why open:

Projection optimization, caching, rebuild implementation, and low-end device performance strategy remain implementation/specification work.

Later-source assessment:

Later claims should normally be implementation details unless they weaken event-log source-of-truth or projection rebuild discipline.

Closure path:

- Implementation/tooling design.
- No formal architecture decision unless proposed performance strategy weakens source-of-truth or rebuild invariants.

Priority:

- P2 for implementation planning.

### Low-End Device Scale And Offline Performance

Classification: Implementation/tooling gap with architecture decision trigger

Affected baseline:

- Event log source of truth
- Projection and workflow
- Authorization and sync
- Local data lifecycle

Why open:

ADR-001 through ADR-005 accept low-end device and intermittent-connectivity pressure, but they do not close concrete performance budgets, storage thresholds, priority sync strategy, projection rebuild strategy, or local compaction/summarization mechanics.

Later-source assessment:

Later claims may propose implementation strategies only if they preserve immutable event history, append-only sync, projection rebuild discipline, scope-filtered delivery, and local lifecycle constraints. Claims that make projections canonical, mutate events, hide data instead of applying lifecycle policy where stronger handling is required, or require global state for ordinary offline capture need formal architecture reconsideration.

Closure path:

- Implementation/tooling design for projection, caching, sync prioritization, local storage pressure handling, and reference device tests.
- Platform-spec detailing only for minimum performance obligations or compatibility constraints that affect contracts.
- Formal architecture decision if performance strategy changes canonical event, projection, sync, or lifecycle semantics.

Priority:

- P2 before core mobile/offline implementation planning.

### Event Schema And Versioning Tooling

Classification: Implementation/tooling gap

Affected baseline:

- Event envelope
- Storage model
- Configuration boundary

Why open:

Event schema/versioning tooling and projection merge strategy across schema versions are deferred.

Later-source assessment:

Claims must preserve immutable event validity and envelope stability.
They must also preserve `device_time` as advisory display/audit data only, not structural ordering input.

Closure path:

- Implementation/tooling design for migration, schema registry, and projection compatibility.
- Platform-spec detailing for immutable event versioning obligations.

Priority:

- P2 for implementation planning.

### Structured Import Export Compatibility

Classification: Implementation/tooling gap with platform-spec compatibility constraint

Affected baseline:

- Event envelope
- Storage model
- Configuration boundary
- Reporting and aggregation

Why open:

`../../constraints.md` requires the platform to remain capable of structured record exchange with external systems, but it explicitly does not require real-time integration or Phase 1 delivery. ADR-001 through ADR-005 preserve a stable event/envelope/schema direction but do not close import/export contracts, external standards, or integration timing.

Later-source assessment:

Claims must preserve immutable event history, envelope stability, deployer shape/activity boundaries, projection derivation, and access-scope constraints. External exchange requirements must not make an external system schema canonical for platform records.

Closure path:

- Platform-spec detailing for import/export compatibility obligations if included in the platform specification.
- Implementation/tooling design for concrete import/export jobs, mapping, validation, and operational delivery.
- Operational policy definition where external exchange is driven by compliance, reporting, or deployment-specific data-sharing obligations.
- Formal architecture decision only if interoperability requirements alter canonical event, envelope, access, or lifecycle semantics.

Priority:

- P3 unless first deployments require structured external exchange.
- P2 before implementation planning for import/export or external reporting surfaces.

### Configuration Authoring And Deployment UX

Classification: Implementation/tooling gap

Affected baseline:

- Configuration boundary

Why open:

Configuration authoring format, deployment packaging UX, deploy-time validator UX, and breaking-change migration tooling are deferred.

Later-source assessment:

Claims must preserve bounded configuration, atomic configuration packages, and no deployer-authored arbitrary access-control logic.

Closure path:

- Implementation/tooling design for authoring and validator UX.
- Platform-spec detailing only for required package/validation behavior.

Priority:

- P2 for implementation planning.

### Auto-Resolution Authoring And Monitoring

Classification: Implementation/tooling gap

Affected baseline:

- Workflow flag lineage and auto-resolution boundary

Why open:

ADR-005 closes the L3b auto-resolution boundary but leaves authoring UX and monitoring/reporting surface open.

Later-source assessment:

Claims must preserve explicit resolution events and `system:auto_resolution/{policy_id}` attribution.

Closure path:

- Implementation/tooling design for authoring and monitoring.
- Platform-spec detailing for required audit surfaces if needed.

Priority:

- P2 after workflow/flag implementation planning starts.

### Sync Delivery Mechanics

Classification: Implementation/tooling gap

Affected baseline:

- Authorization and sync

Why open:

Sync pagination, priority, bandwidth handling, transport details, and operational delivery mechanics remain deferred.

Later-source assessment:

Claims must preserve immutable event sync, scope filtering, idempotency, append-only behavior, and order independence.
They must also preserve the closed scope-change baseline: scope expansion is additive, and scope contraction uses selective retain unless a later formal decision changes the local lifecycle strategy.

Closure path:

- Implementation/tooling design.
- No architecture decision unless delivery mechanics change sync invariants.

Priority:

- P2 for sync-engine implementation planning.

## Operational Policy Gaps

### Retention And Archival

Classification: Operational policy gap

Affected baseline:

- Storage model

Why open:

Retention and archival remain open from earlier viability/blind-spot extraction and are not closed by ADR-001 through ADR-005.

Later-source assessment:

Any policy must preserve immutable event history constraints or explicitly request baseline reconsideration.
Sensitive-data policy must not treat retain-and-hide as sufficient lifecycle handling after scope contraction.

Closure path:

- Operational policy definition.
- Formal architecture decision only if retention requires deletion, redaction, or mutation of canonical events.

Priority:

- P3 unless legal/compliance requirements are known now.

### Sensitive Data Policy And Local Lifecycle

Classification: Operational policy gap with architecture decision trigger

Affected baseline:

- Authorization and sync
- Configuration boundary
- Event log source of truth
- Local data lifecycle under scope changes

Why open:

ADR-004 closes shape/activity-level sensitivity classification and rejects field-level sensitivity. ADR-003 closes selective retain as the initial scope-contraction strategy, while also preserving that sensitive deployments need stronger local lifecycle handling than retain-and-hide. The baseline does not close sensitive-subject policy, local purge rules, encryption/key handling, retention windows, or compliance-driven device lifecycle behavior.

Later-source assessment:

Later claims may elaborate sensitive-data lifecycle only if they preserve immutable event history, shape/activity-level sensitivity classification, assignment-derived access, sync scope as access scope, and the rejection of field-level sensitivity. Claims that require canonical event deletion/redaction, field-level sensitivity, hidden-but-retained local data as sufficient handling, or new access semantics need formal change control.

Closure path:

- Operational policy definition for sensitive classifications, retention windows, purge policy, device loss, compliance handling, and role-transition obligations.
- Implementation/tooling design for local storage, encryption, purge, audit, and migration behavior.
- Platform-spec detailing only for lifecycle obligations that become normative platform contracts.
- Formal architecture decision if the policy changes canonical event history, access/sync semantics, sensitivity granularity, or scope-contraction behavior.

Priority:

- P1 if the first deployment handles sensitive data that requires stronger local lifecycle treatment.
- P3 if initial deployments can stay within non-sensitive or explicitly bounded sensitivity assumptions.

### Setup Experience And Onboarding

Classification: Operational policy gap

Affected baseline:

- Configuration boundary
- Authorization and sync

Why open:

Setup experience, onboarding, and role transition details remain open.

Later-source assessment:

Later claims should be treated as UX/policy/tooling unless they change assignment, authority, or configuration boundaries.

Closure path:

- Operational policy definition for onboarding/role-transition process.
- Implementation/tooling design for setup flows.
- Formal architecture decision only if setup requires new authority/configuration semantics.

Priority:

- P3 unless initial deployment onboarding is blocked.

### Reporting And Aggregation

Classification: Operational policy gap

Affected baseline:

- Projection and workflow
- Authorization and sync

Why open:

Reporting aggregation and workflow-aware reporting remain open after ADR-005.

Later-source assessment:

Claims must preserve event-log source of truth, projection derivation, and access-scope constraints.

Closure path:

- Platform-spec detailing for aggregate capability boundaries.
- Implementation/tooling design for reporting projections.
- Operational policy definition for decision-maker reporting requirements.

Priority:

- P2 for the first platform specification if reporting capabilities are included.

## Completed Later-Source Assessment

### ADR-006-R Through ADR-009 Assessment

Classification: Completed later-source assessment

Affected baseline:

- Closed flag interactions
- Detect-before-act and accept-and-flag discipline
- Event envelope baseline and stability
- Configuration boundary
- Projection and workflow

Why closed:

ADR-006-R through ADR-009 were assessed against the accepted ADR-001 through ADR-005 baseline, architecture responsibility map, and gap register in `10` through `13`.

Assessment rule preserved:

Each later claim must be classified as consistent elaboration, open-gap closure candidate, deferred implementation/spec detail, new unauthorized claim, conflict with closed baseline, or valid dispute requiring formal reopen.

Closure output:

- `10-adr006r-flag-semantics-assessment.md`
- `11-adr007-envelope-type-assessment.md`
- `12-adr008-reference-fields-assessment.md`
- `13-adr009-duality-rule-assessment.md`

Platform-spec use:

- Use classified carry-forward candidates from `10` through `13` as specification constraints, not new decisions.
- Carry forward the `type` / `shape_ref` / reference / mechanism-instance distinctions as specification constraints because they clarify already-settled ADR-001 through ADR-005 boundaries.
- Carry forward the flag constraint that accept-and-flag applies to validly structured state anomalies, not malformed envelopes or invalid payloads.
- Keep flag lifecycle separate from detector source facts, identity lineage, authorization, workflow projection, and reporting ownership.
- Keep `actor_ref` as authorship, `activity_ref` as activity context, and product/deployer labels as configuration or product vocabulary, not platform actor subclasses.
- Route general flag semantics, alias-cycle behavior, final reference emission, Pattern Registry inventory/schema, subject/auditor scope, resolution-event mapping, and bundled shape inventory through the owning gaps in this register.
- Hold back unified flag catalogs, `cycle_violation` normalization, server-created flag invariants, request-time temporal anchors, and non-workflow resolution mapping until the owning gap states a closure path.
- Do not treat ADR-006-R through ADR-009 as automatic authority over ADR-001 through ADR-005.

Later-source hold-back reopen triggers:

- Unified flag catalog: reopen when a flag specification needs a cross-category catalog with lifecycle, identity, and resolution semantics.
- `cycle_violation`: reopen when an identity/flag decision chooses accept-and-flag, reject, or projection-exclusion behavior for cycle-closing lineage facts.
- Server-created flags as a permanent invariant: reopen when a flag category needs normative detector placement and creation authority.
- Request-time temporal anchor: reopen when a detector specification needs a temporal anchor and can preserve `device_time` as advisory only.
- Non-workflow resolution-event mapping: reopen when a general flag/resolution specification defines category-specific manual or automated resolution events.
- Platform-bundled shape inventory: reopen when an owning behavior section needs a normative bundled shape and payload contract.
- Process reference active emission or process lifecycle: reopen when projection/workflow specification needs process identity, pending-match behavior, or process lifecycle semantics.
- Subject-based scope, auditor access, or new scope types: reopen when a first deployment or authorization section requires those access paths.

## Later-Source Assessment Gaps

### General Flag Semantics

Classification: Later-source assessment gap

Affected baseline:

- Closed flag interactions
- Detect-before-act and accept-and-flag discipline

Why open:

ADR-005 closes workflow-specific flag interactions but explicitly does not close general flag semantics.

Assessment rule:

Keep flag category creation, conflict detection timing, source-only cascade, unresolved-flag state derivation, flag resolution/auto-resolution, and general flag semantics separate.

This gap also owns ADR-006-R and ADR-007 candidates for flag identity, flag creation location, detector placement, resolution-event mapping, and any non-workflow flag catalog. Those candidates must not be normalized through the envelope or workflow sections before this gap states a closure path.

Closure path:

- Use `10-adr006r-flag-semantics-assessment.md` as classified candidate material.
- Platform-spec detailing only for general flag semantics that do not alter closed ADR-001 through ADR-005 flag boundaries.
- Formal architecture decision for claims that alter closed ADR-001 through ADR-005 flag boundaries or close a genuinely open flag-semantics gap.

Priority:

- P1 if flag semantics are needed for the first platform specification.

## Non-Gaps

These are closed under the current baseline and must not be reopened without formal change control:

- immutable event log source of truth
- append-only writes
- client-generated UUIDs for offline creation
- device time as advisory display/audit data only
- structural ordering by `device_sequence` and concurrency detection by `sync_watermark`
- immutable event sync
- no stored immutable `authority_context`
- assignment-derived access
- sync scope as access scope
- scope expansion as additive and scope contraction as selective retain under the ADR-003 initial strategy
- platform-owned structural event type vocabulary
- deployer-configured shapes and bounded configuration
- deployer policy values over platform-owned mechanisms, including flag severity overrides, domain uniqueness constraints, platform-fixed scope composition, and shape/activity-level sensitivity classification
- no `status_changed` structural type for ADR-005 workflow
- no structural ordering by `device_time`
- retain-and-hide is not sufficient sensitive-data handling after scope contraction
- projection-derived workflow state
- source-only workflow flag lineage
- form-only bounded `context.*`
