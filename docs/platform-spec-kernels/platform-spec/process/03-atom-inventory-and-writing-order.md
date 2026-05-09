# Atom Inventory And Writing Order

Status: Initial decomposition plan

This document defines the initial platform-spec decomposition and the professional writing order. It is intentionally ordered to stabilize the load-bearing contracts before workflow, flags, triggers, reporting, or operational surfaces.

Paths in this document are relative to `docs/platform-spec-kernels/platform-spec/`.

For fast lookup, agents should read `START-HERE.md` and `atom-registry.yml` before this inventory. This inventory explains the writing order; the registry gives the current machine-readable status map.

## Decomposition Principles

- Split by ownership boundary, not by ADR number, product surface, role label, or implementation module.
- Put canonical truth and envelope contracts before derived behavior.
- Put authority and configuration boundaries before user-facing workflow assumptions.
- Keep high-risk gaps visible as hold-backs instead of silently closing them.
- Accept narrow atoms first; compose them later through explicit contracts.

## Initial Atom Set

| Order | File | Atom | Primary Boundary | Initial Status | Why Here |
|---:|---|---|---|---|---|
| 1 | `atoms/01-spec-governance.md` | Spec Governance And Source Authority | Cross-cutting process | Draft | Prevents later atoms from using the wrong source authority. |
| 2 | `atoms/02-glossary-and-core-definitions.md` | Glossary And Core Definitions | Cross-cutting definitions | Draft | Stabilizes vocabulary before behavior is drafted. |
| 3 | `atoms/03-event-log-storage.md` | Event Log And Storage | Event Log / Storage | Draft | Establishes canonical truth and write-path discipline. |
| 4 | `atoms/04-event-envelope-schema.md` | Event Envelope And Schema | Event Envelope / Schema | Planned | Freezes the stable envelope contract before references or workflow depend on it. |
| 5 | `atoms/05-references-and-identity-lineage.md` | References And Identity Lineage | Identity / Lineage | Planned | Keeps subject lineage narrow and prevents identity from absorbing authority or workflow. |
| 6 | `atoms/06-configuration-and-parametrization.md` | Configuration And Parameterization | Configuration | Planned | Defines deployer variation without surrendering platform semantics. |
| 7 | `atoms/07-assignment-authority-and-sync.md` | Assignment, Authority, And Sync | Assignment / Authority / Sync | Planned | Establishes access, authority reconstruction, and sync scope before derived behavior. |
| 8 | `atoms/08-local-data-lifecycle.md` | Local Data Lifecycle | Local Data Lifecycle | Planned / Conditional | Needed before sensitive local lifecycle or scope contraction details become implementation work. |
| 9 | `atoms/09-projections-workflow-and-patterns.md` | Projections, Workflow, And Patterns | Projection / Workflow State | Planned | Builds on events, envelope, configuration, and authority. |
| 10 | `atoms/10-conflict-flag-and-resolution.md` | Conflict, Flag, And Resolution | Flag / Resolution | Planned | Keeps detection source facts separate from flag lifecycle and resolution. |
| 11 | `atoms/11-trigger-reactivity.md` | Trigger And Reactivity | Trigger / Reactivity | Planned / Conditional | Must respect detect-before-act and event-store write path. |
| 12 | `atoms/12-reporting-aggregation-and-freshness.md` | Reporting, Aggregation, And Freshness | Reporting / Aggregation | Planned / Conditional | Must remain projection-derived and access-scoped. |
| 90 | `atoms/90-open-decisions.md` | Open Decisions And Hold-backs | Cross-boundary | Draft | Keeps gaps visible and prevents accidental closure. |
| 91 | `atoms/91-rejected-paths.md` | Rejected Paths | Cross-boundary | Draft | Makes review guardrails easy to enforce. |
| 92 | `atoms/92-change-control-log.md` | Change-Control Log | Cross-boundary process | Draft | Records accepted baseline changes, disputes, and formal reopens. |

## Batch Plan

### Batch 0: Planning Scaffold

Files:

- `README.md`
- `atom-registry.yml`
- `process/01-atomization-operating-plan.md`
- `process/02-spec-atom-template.md`
- `process/03-atom-inventory-and-writing-order.md`

Acceptance rule:

- These files define process only. They must not create platform behavior.
- The registry is a lookup layer only; atom files remain canonical.

### Batch 1A: Control Foundation

Files:

- `atoms/01-spec-governance.md`
- `atoms/90-open-decisions.md`
- `atoms/91-rejected-paths.md`
- `atoms/92-change-control-log.md`

Primary risks:

- open gaps being silently closed
- rejected paths returning under new names
- draft material being treated as implementation authority
- change-control triggers being handled only from memory

Acceptance rule:

- These files may route and protect decisions, but they must not introduce platform behavior.

### Batch 1B: Foundation Behavior

Files:

- `atoms/02-glossary-and-core-definitions.md`
- `atoms/03-event-log-storage.md`
- `atoms/04-event-envelope-schema.md`

Primary risks:

- envelope drift
- type vs shape collapse
- projection state becoming canonical
- source hierarchy confusion

Acceptance rule:

- Later atoms can reference these without re-reading ADR prose.

### Batch 2: Core Boundaries

Files:

- `atoms/05-references-and-identity-lineage.md`
- `atoms/06-configuration-and-parametrization.md`
- `atoms/07-assignment-authority-and-sync.md`

Primary risks:

- identity absorbing actor, assignment, process, workflow, or reporting lifecycles
- deployer configuration becoming arbitrary platform logic
- account, group, tenant, or IdP claims becoming authority sources
- `activity_ref` being overread as authority context

Acceptance rule:

- References, configuration, and authority can evolve independently through named contracts.

### Batch 3: Derived Behavior

Files:

- `atoms/09-projections-workflow-and-patterns.md`
- `atoms/10-conflict-flag-and-resolution.md`
- `atoms/11-trigger-reactivity.md`

Primary risks:

- workflow state becoming stored canonical state
- invalid transitions being rejected instead of accepted and flagged where the baseline says otherwise
- conflict detection and flag lifecycle merging into one broad subsystem
- triggers firing before relevant checks run

Acceptance rule:

- Derived behavior consumes events, configuration, authority, and projections without owning their source facts.

### Batch 4: Operational Surfaces

Files:

- `atoms/08-local-data-lifecycle.md`
- `atoms/12-reporting-aggregation-and-freshness.md`
- later export, retention, deployment, and operations atoms if needed

Primary risks:

- reports becoming authority shortcuts
- freshness being implied as real-time visibility
- retain-and-hide being treated as sufficient sensitive-data handling
- external interoperability reshaping canonical events

Acceptance rule:

- Operational surfaces preserve core invariants and route unresolved policy to open decisions.

### Batch 5: Control Register Maintenance

Files:

- `atoms/90-open-decisions.md`
- `atoms/91-rejected-paths.md`
- `atoms/92-change-control-log.md`

Acceptance rule:

- Every open gap, rejected path, and accepted baseline change continues to have one visible place to land.

## Per-Atom Drafting Order

For each atom:

1. copy the template from `process/02-spec-atom-template.md`
2. assign the primary boundary
3. list source guardrails
4. draft scope and non-scope first
5. draft invariants
6. draft inputs, outputs, and boundary crossings
7. draft allowed extensions
8. draft forbidden couplings
9. route open gaps
10. run boundary review
11. run integration review
12. accept, defer, hold back, reject, or escalate

## First Hold-backs

These must remain visible until explicitly closed:

- cloud multi-tenancy and shared-runtime hosting
- deployment identity in event envelopes
- external identity-provider authority
- group-managed authorization
- shared-device multi-actor sessions
- auditor access and subject-based scope
- temporary authority, revocation, and offline grace-period policy
- general flag semantics beyond accepted workflow cases
- alias-cycle read-side behavior and resolution semantics
- exact Pattern Registry inventory and formal pattern schema
- source-chain traversal depth limits
- sync delivery mechanics, pagination, priority, and bandwidth policy
- local purge/lifecycle rules for sensitive data
- reporting freshness semantics
- retention and archival
- structured import/export contracts

## First Rejected Paths Register

Carry these into `atoms/91-rejected-paths.md` when that file is drafted:

- mutable canonical records plus separate audit log
- snapshot-primary or action-log-primary source of truth
- direct canonical projection patching
- structural ordering by `device_time`
- stored immutable `authority_context`
- deployer-authored arbitrary access-control logic
- field-level sensitivity
- `tenant_id`, `deployment_id`, `user_id`, or `group_id` as event-envelope authority fields
- `status_changed`, `current_state`, or `pattern_ref` as structural envelope additions
- role labels as platform actor subclasses
- queues, work items, review lists, or dashboard items as canonical storage primitives
- last-write-wins for operational conflicts requiring judgment
- invisible automatic merge where judgment is required

## Completion Definition

Atomization is complete enough for implementation planning when:

- foundation and core-boundary atoms are accepted
- derived-behavior atoms have at least draft contracts
- all high-risk gaps are visible in `atoms/90-open-decisions.md`
- rejected paths are consolidated in `atoms/91-rejected-paths.md`
- implementation designs can map to spec atoms without inventing architecture
