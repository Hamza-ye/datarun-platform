# Atom Inventory And Writing Order

Status: Initial decomposition plan

This document defines the initial platform-spec decomposition and the professional writing order. It is intentionally ordered to stabilize the load-bearing contracts before workflow, flags, triggers, reporting, or operational surfaces.

Paths in this document are relative to `docs/platform-spec-kernels/platform-spec/`.

For fast lookup, agents should read `atom-registry.yml` before this inventory. This inventory explains the writing order; the registry gives the current machine-readable status map.

## Decomposition Principles

- Split by ownership boundary, not by ADR number, product surface, role label, or implementation module.
- Put canonical truth and envelope contracts before derived behavior.
- Put authority and configuration boundaries before user-facing workflow assumptions.
- Keep high-risk gaps visible as hold-backs instead of silently closing them.
- Accept narrow atoms first; compose them later through explicit contracts.

## Initial Atom Set

| Order | File | Atom | Primary Boundary | Why Here |
|---:|---|---|---|---|
| 1 | `atoms/01-spec-governance.md` | Spec Governance And Source Authority | Cross-cutting process | Prevents later atoms from using the wrong source authority. |
| 2 | `atoms/02-glossary-and-core-definitions.md` | Glossary And Core Definitions | Cross-cutting definitions | Stabilizes vocabulary before behavior is drafted. |
| 3 | `atoms/03-event-log-storage.md` | Event Log And Storage | Event Log / Storage | Establishes canonical truth and write-path discipline. |
| 4 | `atoms/04-event-envelope-schema.md` | Event Envelope And Schema | Event Envelope / Schema | Freezes the stable envelope contract before references or workflow depend on it. |
| 5 | `atoms/05-references-and-identity-lineage.md` | References And Identity Lineage | Identity / Lineage | Keeps subject lineage narrow and prevents identity from absorbing authority or workflow. |
| 6 | `atoms/06-configuration-and-parametrization.md` | Configuration And Parameterization | Configuration | Defines deployer variation without surrendering platform semantics. |
| 7 | `atoms/07-assignment-authority-and-sync.md` | Assignment, Authority, And Sync | Assignment / Authority / Sync | Establishes access, authority reconstruction, and sync scope before derived behavior. |
| 8 | `atoms/08-local-data-lifecycle.md` | Local Data Lifecycle | Local Data Lifecycle | Needed before sensitive local lifecycle or scope contraction details become implementation work. |
| 9 | `atoms/09-projections-workflow-and-patterns.md` | Projections, Workflow, And Patterns | Projection / Workflow State | Builds on events, envelope, configuration, and authority. |
| 10 | `atoms/10-conflict-flag-and-resolution.md` | Conflict, Flag, And Resolution | Flag / Resolution | Keeps detection source facts separate from flag lifecycle and resolution. |
| 11 | `atoms/11-trigger-reactivity.md` | Trigger And Reactivity | Trigger / Reactivity | Must respect detect-before-act and event-store write path. |
| 12 | `atoms/12-reporting-aggregation-and-freshness.md` | Reporting, Aggregation, And Freshness | Reporting / Aggregation | Must remain projection-derived and access-scoped. |
| 90 | `atoms/90-open-decisions.md` | Open Decisions And Hold-backs | Cross-boundary | Keeps gaps visible and prevents accidental closure. |
| 91 | `atoms/91-rejected-paths.md` | Rejected Paths | Cross-boundary | Makes review guardrails easy to enforce. |
| 92 | `atoms/92-change-control-log.md` | Change-Control Log | Cross-boundary process | Records accepted baseline changes, disputes, and formal reopens. |

## Batch Plan

### Batch 0: Planning Scaffold

Files:

- `README.md`
- `atom-registry.yml`
- `process/01-atomization-operating-plan.md`
- `process/02-spec-atom-template.md`
- `process/03-atom-inventory-and-writing-order.md`
- `process/04-planned-consumer-review-cards.md`

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
- Foundation behavior atoms are accepted as a batch, not promoted one by one.
- Before acceptance, Challenge Review must check rejected paths and change-control triggers.
- Before acceptance, Integration Review must check planned consumers `SPEC-005`, `SPEC-006`, and `SPEC-007`, plus direct registry consumers `SPEC-009` for the projection/workflow risk surface and `SPEC-010` for the conflict/flag risk surface.
- Because these consumers are still planned, use `process/04-planned-consumer-review-cards.md` as the review surface instead of creating skeleton atom files.
- Planned-consumer review cards are superseded when their atoms are drafted; they are not downstream contracts.

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

## Control Registers

Current hold-backs live in `atoms/90-open-decisions.md`.
Current rejected paths live in `atoms/91-rejected-paths.md`.
Do not maintain duplicate hold-back or rejected-path lists in this inventory.

## Completion Definition

Atomization is complete enough for implementation planning when:

- foundation and core-boundary atoms are accepted
- derived-behavior atoms have at least draft contracts
- all high-risk gaps are visible in `atoms/90-open-decisions.md`
- rejected paths are consolidated in `atoms/91-rejected-paths.md`
- implementation designs can map to spec atoms without inventing architecture
