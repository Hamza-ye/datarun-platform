# Atomization Readiness From Product

Status: Session 10 product-alignment artifact

This document assesses whether the product-alignment track is ready to feed atomization.

It is a readiness gate, not a backlog, not an implementation plan, and not an architecture decision. It does not reopen the first-slice selection from `09-first-vertical-slice.md`.

## Source Basis

Primary inputs:

- `01-phase-1-scenario-boundary-map.md`
- `02-product-experience-principles.md`
- `03-user-roles-and-operational-contexts.md`
- `04-core-operational-journeys.md`
- `05-information-architecture.md`
- `06-product-vocabulary-alignment.md`
- `07-interaction-state-model.md`
- `08-ux-gap-routing.md`
- `09-first-vertical-slice.md`

Architecture guardrails:

- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../pre-operations/04-accepted-pre-atomization-decisions.md`

## Verdict

Conditional GO:

```text
Proceed with narrow selected-slice atomization.
```

NO-GO:

```text
Do not resume broad platform atomization yet.
```

The product track is ready to feed atomization only for the selected slice:

```text
Assigned offline capture -> sync visibility -> authorized review -> returned correction -> evidence/history -> minimal freshness-aware oversight
```

Where source scenarios call this `supervisor review`, the atomization-safe reading is an authorized review/oversight context. Atomization must not turn field actor, supervisor, coordinator, auditor, reviewer, or administrator labels into fixed platform classes. It should name the operation class and boundary: offline-capable capture, sync-visible review, online/coordination-required setup or resolution, or offline-with-constraints authority.

This is enough to start atomizing product-backed platform surfaces where product behavior, scenario pressure, and baseline boundary ownership agree.

It is not enough to atomize full authorization, full sync delivery, full identity/lineage, full Pattern Registry, full reporting, full setup, full exception handling, transfer/custody, auditor access, or retention/export.

## Control Rule

Atomization may proceed only when each proposed atom has all of the following:

| Requirement | Meaning |
|---|---|
| Product behavior | The atom is needed by the selected slice, not by a speculative future surface |
| Scenario pressure | The atom is grounded in Phase 1 scenario pressure already mapped by Session 1 |
| Boundary owner | The atom names the accepted professional-baseline boundary that owns the behavior |
| Gap routing | The atom names open gaps it touches and states what it does not close |
| Vocabulary layer | The atom keeps core, product translation, and operational-surface terms separate |
| Offline walkthrough | The atom still behaves under saved-local, pending-sync, synced, and narrow sync-problem states |

If any requirement is missing, the item is not ready for atomization. It should be routed back to product clarification, gap register, change control, or later implementation design.

## Ready To Atomize Now

These surfaces are ready only inside the selected slice.

| Surface | Primary Boundary | Ready Scope | Non-Closure Rule |
|---|---|---|---|
| Selected-slice product behavior | Product alignment / later product spec | One assigned offline capture and review loop | Does not define the full product or final UI |
| Capture event/history obligations | Event Log/Storage | Original capture, correction, review decision, and preserved history | Does not create mutable canonical records |
| Accepted envelope use | Event Envelope/Schema | Use the closed envelope for slice events | Does not add `authority_context`, tenant, deployment, group, or user fields |
| Preconfigured activity and information shape use | Configuration + Event Envelope/Schema | Consume one configured activity and one information shape | Does not close setup-builder UX, schema tooling, or Pattern Registry schema |
| Simple assignment-derived work visibility | Assignment/Auth/Sync | One capture context, one review/oversight context, one simple assignment context | Does not close permission tables, shared devices, temporary authority, auditor scope, subject-based scope, or fixed role taxonomy |
| Offline local status language | Event Log/Storage + Assignment/Auth/Sync + product translation | Saved locally, pending sync, synced, and narrow sync problem | Does not define sync transport, bandwidth, pagination, deletion, or sensitive local lifecycle policy |
| Review queue | Projection/Workflow State | Submitted work appears for an authorized review/oversight context after sync visibility | Does not create canonical `WorkItem` storage or fixed `Supervisor` class |
| Approve/return decision path | Projection/Workflow State + Event Log/Storage | Approve or return with reason, then correction/resubmission | Does not define multi-step approval or full Pattern Registry inventory |
| Evidence/history view | Event Log/Storage + product translation | Show original, correction, and decision trail | Does not define export, audit retention, or source-chain traversal closure |
| Minimal freshness-aware oversight | Projection-derived view + Reporting/Aggregation pressure | Narrow counts for assigned, submitted, returned, approved, and stale/pending visibility | Does not close reporting/aggregation |

## Conditional Atomization

These surfaces may be atomized only if their constraint is kept explicit in the atom itself.

| Surface | Constraint | If Constraint Fails |
|---|---|---|
| Stable target context | Display only pre-existing target label/context; no offline target creation, duplicate resolution, merge/split, alias-cycle handling, or subject-based scope | Remove target context from the first slice |
| Minimal review pattern | Specify only the selected approve/return loop; do not generalize into full workflow authoring or full Pattern Registry inventory | Route back to Pattern Registry gaps |
| Narrow sync-problem visibility | Explain local/central visibility mismatch without treating malformed events as normal conflicts | Route to sync delivery mechanics or conflict/flag gap |
| Minimal oversight | Present freshness-aware counts as read-model interpretation | Route broader needs to Reporting/Aggregation |
| Returned work as attention | Treat return as review decision routing, not general flag semantics | Route broader exception behavior to conflict/flag gaps |

Conditional atomization should be labeled as conditional in the atomization output. It should not be promoted to stable platform behavior without a later acceptance pass.

## Not Ready For Atomization

These surfaces remain outside the first atomization pass.

| Surface | Reason |
|---|---|
| Full authorization model | Permission details, subject-based scope, auditor access, shared devices, temporary authority, and revocation reconciliation remain open or deferred |
| Account, IdP, tenant, deployment, or group model | Pre-operations decisions keep these outside the event envelope and outside direct authority shortcuts |
| Full sync engine or delivery mechanics | Product states can be shown; transport guarantees, delivery strategy, pagination, ordering strategy, and scope-contraction mechanics remain outside this slice |
| Full identity/lineage resolution | Merge, split, alias-cycle, duplicate resolution, subject lifecycle, and offline target creation remain outside this slice |
| Full Pattern Registry inventory/schema | The slice may use one narrow review/correction pattern; it does not close inventory, formal schema, or authoring semantics |
| Full reporting/aggregation | Minimal freshness-aware oversight is not reporting closure |
| Setup/configuration authoring UX | The first slice consumes configuration; it does not define builder, deployment, migration, or onboarding tooling |
| Broad exception, flag, and conflict semantics | Returned work and narrow sync problem visibility do not generalize flags/conflicts |
| Transfer/custody/discrepancy | Later slice after the selected capture/review/offline loop proves composition |
| Import/export, retention, archive, and formal audit package | Operational policy and tooling remain outside first-slice atomization |
| Sensitive local data lifecycle | Local status labels do not settle storage duration, deletion, device partitioning, or shared-device behavior |

## Boundary Ownership Check

Atomization should preserve this ownership map:

| Boundary | First-Slice Atomization Use | Guardrail |
|---|---|---|
| Event Log/Storage | Capture, correction, decision, and evidence history obligations | Append-only history remains the source of truth |
| Event Envelope/Schema | Closed envelope use for accepted slice events | No new envelope authority or deployment fields |
| Configuration | One existing activity and information shape | No setup-builder closure |
| Assignment/Auth/Sync | Simple assignment-derived visibility and scoped sync eligibility | Sync scope remains access scope |
| Projection/Workflow State | Review queue, returned correction, current interpretation, and minimal counts | Projection state is derived, not canonical storage truth |
| Flag/Resolution | No general atom; returned work remains review routing | Do not generalize ADR-005 flag semantics |
| Reporting/Aggregation | Minimal freshness-aware read model only | Broader reporting stays routed to the gap register |
| Identity/Lineage | No required atom in the first slice | Optional target context must not imply identity closure |
| Local Data Lifecycle | User-visible local/sync states only | Sensitive lifecycle and scope-contraction policy remain outside scope |

## Atomization Acceptance Tests

Each first-pass atom should answer these questions before it is accepted:

1. Which selected-slice behavior needs this atom?
2. Which scenario pressure does it trace to?
3. Which professional-baseline boundary owns it?
4. Which open gaps does it touch but not close?
5. What vocabulary layer is being used: platform core, product translation, or operational surface?
6. What happens while the field user is offline?
7. What becomes visible only after sync?
8. What remains visible as history after correction or review?
9. What user-facing state is derived rather than stored canonical state?
10. What would trigger change control if added to the atom?

An atom should be rejected or narrowed if it introduces:

- new event envelope fields
- stored immutable `authority_context`
- tenant, deployment, group, IdP, device, or account authority shortcuts
- a canonical `WorkItem` primitive
- mutable canonical records competing with immutable events
- field-level sensitivity as a first-slice dependency
- generalized Pattern Registry closure
- general conflict/flag semantics
- subject-based scope or auditor access
- broad reporting/aggregation
- sync delivery mechanics hidden inside UX states

## Sequencing From Here

Professional sequencing from this point:

1. Close this product-alignment track with `11-alignment-closeout.md`.
2. Confirm the stable input surface for atomization.
3. Start atomization from the selected slice, not from broad platform categories.
4. Atomize only the ready and explicitly conditional surfaces listed in this document.
5. Keep every deferred surface visible in gap routing rather than burying it inside implementation.

This preserves the architecture baseline while giving atomization a concrete product behavior to serve.
