# Baseline Validation And Full-Stack Readiness

Status: Assessment artifact; no baseline mutation; no final platform-spec sections

This document records the validation pass over the professional baseline and classifies full-stack readiness gaps before platform-spec platform-spec section drafting. It does not change the accepted ADR-001 through ADR-005 baseline.

## Source Basis

Validated against:

- `../00-extraction-state.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `../professional-baseline/02-change-control.md`
- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../../adrs/adr-001-offline-data-model.md`
- `../../adrs/adr-002-identity-conflict.md`
- `../../adrs/adr-003-authorization-sync.md`
- `../../adrs/adr-004-configuration-boundary.md`
- `../../adrs/adr-005-state-progression.md`
- `../../principles.md`
- `../../constraints.md`
- `../../access-control-scenario.md`

## Phase 1 Result

Validation decision: pass.

The professional baseline is internally consistent enough to proceed to full-stack gap classification and platform-spec section drafting planning. No ADR-001 through ADR-005 decision is contradicted, and no final platform-spec section should be written directly from ADR prose or exploration prose.

### Preserved Closure

- ADR-001 storage closure is preserved: immutable append-only event log, event-store write path, projection-derived views, client-generated identifiers, immutable event sync.
- ADR-002 identity/conflict closure is preserved: typed references, `device_id` / `device_sequence` / `sync_watermark`, advisory `device_time`, alias-in-projection, raw-reference conflict detection, stale-event acceptance, online-only merge/split where required.
- ADR-003 authorization/sync closure is preserved: assignment-derived access, sync scope as access scope, no stored immutable `authority_context`, original-subject authorization, additive scope expansion, selective-retain contraction.
- ADR-004 configuration closure is preserved: `shape_ref`, optional `activity_ref`, six structural event types, server-only triggers where required, atomic configuration packages, no deployer-authored access-control logic, no field-level sensitivity.
- ADR-005 workflow closure is preserved: no new envelope fields, no `status_changed`, no `current_state`, no `pattern_ref`, projection-derived workflow state, source-only workflow flag lineage, bounded form-only `context.*`, L3b auto-resolution boundary.

### Boundary Controls Hold

The identity control is necessary and should remain active during platform-spec section drafting. ADR-002's `subject`, `actor`, `process`, and `assignment` categories are envelope/reference categories, not fixed domain entity classes and not ownership claims. A referent may be a person, place, transfer, campaign, review, another actor's work, or operational process; ownership depends on the mechanism that governs that referent's lifecycle.

The conflict/flag/offline control also holds. Accept-and-flag, detect-before-act, structural validation, flag lifecycle, offline default, workflow state exclusion, and local advisory validation are related controls, not one broad subsystem.

The operational-constraints control holds. `constraints.md` remains operational envelope authority. It does not define storage, sync protocol, access-control implementation, reporting product model, tenancy model, or deployment packaging by itself.

### Non-Blocking Watch Items

- ADR-002 prose still contains examples that can be overread as fixed subject-domain mappings. Platform specification section drafting should cite `../professional-baseline/09-identity-boundary-control.md` and `../professional-baseline/12-adr008-reference-fields-assessment.md` for the implementation-safe reading.
- General flag semantics, alias-cycle behavior, exact Pattern Registry inventory, formal pattern schema, source-chain traversal limits, sync delivery mechanics, reporting aggregation, and local data lifecycle remain explicit gaps or hold-backs.
- ADR-006-R through ADR-009 remain classified assessment material only. Their carry-forward candidates can inform platform-spec section drafting, but they do not supersede ADR-001 through ADR-005.

## Phase 2 Classification Rules

Use these readiness categories before writing platform-spec sections:

- Safely deferrable: can be handled after platform-spec section drafting as product, UX, implementation, operations, or deployment detail without changing closed invariants or boundary ownership.
- Requires early routing: should be assigned to owning boundaries and hold-backs before affected sections are drafted, but does not need an architecture decision yet.
- Requires pre-specification decision: introduces or threatens an invariant, envelope/reference interpretation, authority boundary, sync boundary, tenant boundary, or platform/deployer split that could be accidentally frozen into the first sections.

## Full-Stack Gap Classification

| Gap | Readiness classification | Primary routing | Reason |
|---|---|---|---|
| Notifications and escalation | Requires early routing | Trigger / Reactivity | ADR-004 already has `alert`, task events, bounded trigger DAGs, and escalation metadata. ADR-005 has auto-resolution escalation. The missing work is delivery surface, recipient routing, notification state, and channel semantics. These must not bypass event-store writes, assignment/sync scope, detect-before-act, or flag lifecycle. |
| Admin and configuration surfaces | Requires early routing | Configuration | Configuration authoring, deploy-time validation, package publishing, setup, onboarding, and role-transition surfaces already exist as gaps. UX details are deferrable, but the routes must preserve atomic config packages, platform-owned mechanisms, no arbitrary access logic, and no envelope changes. |
| Multi-tenant deployment model, cloud plus self-hosted | Requires pre-specification decision | Deployment / Tenancy routing surface, with Event Log / Storage and Configuration as affected boundaries | The baseline assumes deployments but does not define tenant isolation, deployment partitioning, data residency, self-host packaging, or whether deployment context is outside the event envelope. Before platform-spec section drafting, decide the minimum boundary rule: tenant/deployment context must not add envelope fields or redefine event identity unless a formal change-control decision accepts that. |
| Authentication: actor to user, identity, and user-group mapping | Requires pre-specification decision | Assignment / Authority / Sync | The baseline has `actor_ref`, assignment-derived authority, and device identity separation, but it does not define auth accounts, sessions, groups, IdP mapping, or actor provisioning. Before platform-spec drafting authorization/sync, decide the minimum mapping rule: authenticated principals map to actor references; authority still derives from assignments/roles/scopes, not from user groups as arbitrary access programs. |
| Field capture, review, and resolution user surfaces | Requires early routing | Event Envelope / Schema; Projection / Workflow State; Flag / Resolution | UI details can wait, but affected sections must preserve activity-context capture, advisory offline validation, visible flagged timelines, online-only resolution where required, and projection-derived state. |
| Reporting, dashboards, and freshness surfaces | Requires early routing if included in first product slice; otherwise safely deferrable | Reporting / Aggregation | Reporting must preserve projection derivation, access/sync scope, and visible freshness. It must not become canonical operational state or an authority shortcut. |
| Setup, onboarding, role transition | Requires early routing if initial deployment depends on it; otherwise safely deferrable | Configuration; Assignment / Authority / Sync | Already accepted as a gap. Escalate only if onboarding requires new assignment, authority, actor-session, or configuration-package semantics. |
| Audit export and structured interoperability | Safely deferrable unless first deployment has compliance/export obligations | Event Envelope / Schema | `constraints.md` requires structured exchange compatibility, not Phase 1 real-time integration. Any export/import design must keep external schemas derived from internal events/projections, not canonical over them. |
| Retention, archival, sensitive local lifecycle | Safely deferrable unless compliance or self-host requirements are immediate | Local Data Lifecycle | Existing constraint is enough for platform-spec section drafting: retain-and-hide is not sufficient for sensitive deployments, and local lifecycle must not mutate central immutable history. |

`Deployment / Tenancy` above is a routing placeholder for the platform-spec section drafting discussion, not a newly accepted system boundary or implementation module. It exists because the current boundary map does not yet name where hosting model, tenant isolation, deployment packaging, and jurisdictional residency decisions land.

## Priority Before Platform specification section drafting Discussion

1. Make two pre-specification decisions before drafting affected sections:
   - the deployment/tenant context decision, especially whether it stays outside the event envelope
   - the authentication/account-to-actor mapping decision, especially whether groups are configuration inputs rather than authority sources
2. Route notifications/escalation, admin/configuration surfaces, and operational user surfaces as hold-backs in the first platform-spec section drafting plan.
3. Keep reporting, audit/export, retention, and self-host operations visible as deferred or conditional concerns unless the first deployment requires them.

## Platform Specification Section Drafting Constraints

- Do not add `tenant_id`, `user_id`, `group_id`, notification status, or admin workflow state to the event envelope without formal change control.
- Do not let authentication accounts or user groups replace assignment-derived access.
- Do not let notification delivery state become canonical operational truth unless it enters through an accepted event/projection boundary.
- Do not let admin UI convenience mutate configuration, assignments, subjects, or workflow state outside the accepted event/configuration mechanisms.
- Do not treat cloud hosting, self-hosting, or jurisdictional deployment needs as proof of a new platform primitive until the affected boundary is named and the invariant is explicit.
