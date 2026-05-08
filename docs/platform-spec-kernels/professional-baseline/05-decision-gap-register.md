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
- Later-source assessment gap: ADR-006-R through ADR-009 may contain relevant claims, but those claims must be assessed through change control.

## Closure Paths

- Formal architecture decision: write a focused decision memo or ADR that explicitly names the baseline item it changes or extends.
- Platform-spec detailing: write platform-spec language under the existing architecture boundary; no new ADR unless the detail changes the boundary.
- Implementation/tooling design: write an engineering design, prototype, or tickets; preserve the baseline constraints.
- Operational policy definition: define product/operations policy; escalate only if policy requires architecture change.
- Later-source assessment: classify later ADR claims through `02-change-control.md`; do not absorb them directly.
- No action until product need appears: keep the gap visible but do not spend engineering time until a concrete need appears.

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

- P1 before authorization/sync atomization if the first platform spec includes campaigns, emergency cover, temporary grants, or offline revocation behavior.
- P3 if initial atomization can explicitly defer temporary-authority workflow and grace-period policy.

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

- P1 before atomizing identity lineage or general flag semantics if alias-cycle behavior is in scope.
- P2 if the first platform spec explicitly defers alias-cycle handling.

## Platform-Spec Detail Gaps

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

- P1 for platform specification skeleton.

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

- P1 for platform specification skeleton.

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

- Platform-spec detailing for import/export compatibility obligations if included in the spec skeleton.
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

- P2 for platform spec skeleton if reporting capabilities are included.

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

ADR-006-R through ADR-009 were assessed against the accepted ADR-001 through ADR-005 baseline, validated boundary map, and gap register in `10` through `13`.

Assessment rule preserved:

Each later claim must be classified as consistent elaboration, open-gap closure candidate, deferred implementation/spec detail, new unauthorized claim, conflict with closed baseline, or valid dispute requiring formal reopen.

Closure output:

- `10-adr006r-flag-semantics-assessment.md`
- `11-adr007-envelope-type-assessment.md`
- `12-adr008-reference-fields-assessment.md`
- `13-adr009-duality-rule-assessment.md`

Atomization use:

- Use the classified carry-forward candidates and hold-backs from `10` through `13`.
- Do not treat ADR-006-R through ADR-009 as automatic authority over ADR-001 through ADR-005.

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

Closure path:

- Use `10-adr006r-flag-semantics-assessment.md` as classified candidate material.
- Platform-spec detailing only for general flag semantics that do not alter closed ADR-001 through ADR-005 flag boundaries.
- Formal architecture decision for claims that alter closed ADR-001 through ADR-005 flag boundaries or close a genuinely open flag-semantics gap.

Priority:

- P1 if flag semantics are needed for the platform spec skeleton.

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
