# Decision Gap Register

Status: Draft from Architecture Baseline v0

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

### Subject-Based Scope And Auditor Access

Classification: Architecture decision gap

Affected baseline:

- Authorization and sync
- Immutable event sync and access-scoped delivery

Why open:

ADR-003/ADR-004 leave subject-based scope, auditor access, sensitive-subject classification, and related visibility exceptions open or policy-owned.

Later-source assessment:

Later ADRs may elaborate access/visibility only if they do not contradict assignment-derived access and sync scope as access scope.

### Shared Device Actor Scope

Classification: Architecture decision gap

Affected baseline:

- Authorization and sync
- Identity and references

Why open:

The baseline separates hardware-bound device identity from actor-scoped access, but shared-device actor scope remains unresolved.

Later-source assessment:

Assess later claims for consistency with original-subject authorization, actor assignment, and immutable event authorship.

## Platform-Spec Detail Gaps

### Exact Pattern Registry Inventory

Classification: Platform-spec detail gap

Affected baseline:

- Projection and workflow

Why open:

ADR-005 settles the Pattern Registry as a platform-owned workflow primitive but leaves exact pattern inventory and skeletons undecided.

Later-source assessment:

Later claims may propose inventory, but must not change projection-derived workflow state or add envelope/event-type structure without formal reopen.

### Formal Pattern Schema Format

Classification: Platform-spec detail gap

Affected baseline:

- Projection and workflow
- Configuration boundary

Why open:

ADR-005 leaves formal pattern schema format outside closure.

Later-source assessment:

Later claims may be deferred spec details unless they alter platform/deployer boundaries.

### Source-Chain Traversal Limits

Classification: Platform-spec detail gap

Affected baseline:

- Workflow flag lineage and auto-resolution boundary

Why open:

ADR-005 closes source-only flagging and source-chain traversal but leaves traversal depth limits undecided.

Later-source assessment:

Claims about traversal limits should be assessed as platform-spec details unless they introduce stored derived flags or alter source-only lineage.

### Bounded Context Expression Details

Classification: Platform-spec detail gap

Affected baseline:

- Bounded context expressions

Why open:

ADR-005 closes the allowed `context.*` surface but leaves caching internals and detailed execution mechanics open.

Later-source assessment:

Later claims must not expand `context.*` beyond form-only seven-value scope without formal reopen.

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

### Configuration Authoring And Deployment UX

Classification: Implementation/tooling gap

Affected baseline:

- Configuration boundary

Why open:

Configuration authoring format, deployment packaging UX, deploy-time validator UX, and breaking-change migration tooling are deferred.

Later-source assessment:

Claims must preserve bounded configuration, atomic configuration packages, and no deployer-authored arbitrary access-control logic.

### Auto-Resolution Authoring And Monitoring

Classification: Implementation/tooling gap

Affected baseline:

- Workflow flag lineage and auto-resolution boundary

Why open:

ADR-005 closes the L3b auto-resolution boundary but leaves authoring UX and monitoring/reporting surface open.

Later-source assessment:

Claims must preserve explicit resolution events and `system:auto_resolution/{policy_id}` attribution.

### Sync Delivery Mechanics

Classification: Implementation/tooling gap

Affected baseline:

- Authorization and sync

Why open:

Sync pagination, priority, bandwidth handling, transport details, and operational delivery mechanics remain deferred.

Later-source assessment:

Claims must preserve immutable event sync, scope filtering, idempotency, append-only behavior, and order independence.

## Operational Policy Gaps

### Retention And Archival

Classification: Operational policy gap

Affected baseline:

- Storage model

Why open:

Retention and archival remain open from earlier viability/blind-spot extraction and are not closed by ADR-001 through ADR-005.

Later-source assessment:

Any policy must preserve immutable event history constraints or explicitly request baseline reconsideration.

### Setup Experience And Onboarding

Classification: Operational policy gap

Affected baseline:

- Configuration boundary
- Authorization and sync

Why open:

Setup experience, onboarding, and role transition details remain open.

Later-source assessment:

Later claims should be treated as UX/policy/tooling unless they change assignment, authority, or configuration boundaries.

### Reporting And Aggregation

Classification: Operational policy gap

Affected baseline:

- Projection and workflow
- Authorization and sync

Why open:

Reporting aggregation and workflow-aware reporting remain open after ADR-005.

Later-source assessment:

Claims must preserve event-log source of truth, projection derivation, and access-scope constraints.

## Later-Source Assessment Gaps

### ADR-006-R Through ADR-009 Assessment

Classification: Later-source assessment gap

Affected baseline:

- Closed flag interactions
- Detect-before-act and accept-and-flag discipline
- Event envelope baseline and stability
- Configuration boundary
- Projection and workflow

Why open:

ADR-006-R through ADR-009 are quarantined until the ADR-001 through ADR-005 baseline and gap register are stable.

Assessment rule:

Each later claim must be classified as consistent elaboration, open-gap closure candidate, deferred implementation/spec detail, new unauthorized claim, conflict with closed baseline, or valid dispute requiring formal reopen.

### General Flag Semantics

Classification: Later-source assessment gap

Affected baseline:

- Closed flag interactions
- Detect-before-act and accept-and-flag discipline

Why open:

ADR-005 closes workflow-specific flag interactions but explicitly does not close general flag semantics.

Assessment rule:

Keep flag category creation, conflict detection timing, source-only cascade, unresolved-flag state derivation, flag resolution/auto-resolution, and general flag semantics separate.

## Non-Gaps

These are closed under the current baseline and must not be reopened without formal change control:

- immutable event log source of truth
- append-only writes
- client-generated UUIDs for offline creation
- immutable event sync
- no stored immutable `authority_context`
- assignment-derived access
- sync scope as access scope
- platform-owned structural event type vocabulary
- deployer-configured shapes and bounded configuration
- no `status_changed` structural type for ADR-005 workflow
- projection-derived workflow state
- source-only workflow flag lineage
- form-only bounded `context.*`
