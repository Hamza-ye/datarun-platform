# Section Inventory And Writing Order

Status: Candidate decomposition input

This document defines the initial platform-spec decomposition and the professional writing order. It is intentionally ordered to stabilize the load-bearing contracts before workflow, flags, triggers, reporting, or operational surfaces.

Paths in this document are relative to `docs/platform-spec-kernels/platform-spec/`.

This inventory is candidate process input. The current section structure comes from `../../professional-baseline/20-platform-spec-outline.md`; `section-registry.yml` is only a manifest for local paths and candidate inputs.

## Decomposition Principles

- Split by ownership boundary, not by ADR number, product surface, role label, or implementation module.
- Put canonical truth and envelope contracts before derived behavior.
- Put authority and configuration boundaries before user-facing workflow assumptions.
- Keep high-risk gaps visible as hold-backs instead of silently closing them.
- Accept narrow sections first; compose them later through explicit contracts.

## Initial Section Set

| Order | File | Section | Primary Boundary | Why Here |
|---:|---|---|---|---|
| 1 | `sections/00-specification-source-authority.md` | Specification Source Authority | professional-baseline source authority | Prevents later sections from using the wrong source authority. |
| 2 | `sections/01-core-definitions-and-boundary-vocabulary.md` | Core Definitions And Boundary Vocabulary | professional-baseline source authority | Stabilizes vocabulary before behavior is drafted. |
| 3 | `sections/02-event-log-and-storage-model.md` | Event Log And Storage Model | Event Log / Storage | Establishes canonical truth and write-path discipline. |
| 4 | `sections/03-event-envelope-schema-and-references.md` | Event Envelope, Schema, And References | Event Envelope / Schema | Freezes the stable envelope contract before references or workflow depend on it. |
| 5 | `sections/04-identity-and-lineage.md` | Identity And Lineage | Identity / Lineage | Keeps subject lineage narrow and prevents identity from absorbing authority or workflow. |
| 6 | `sections/05-assignment-authority-and-sync.md` | Assignment, Authority, And Sync | Assignment / Authority / Sync | Establishes access, authority reconstruction, and sync scope before derived behavior. |
| 7 | `sections/06-configuration-and-parametrization.md` | Configuration And Parameterization | Configuration | Defines deployer variation without surrendering platform semantics. |
| 8 | `sections/07-projection-workflow-and-pattern-registry.md` | Projection, Workflow, And Pattern Registry | Projection / Workflow State | Builds on events, envelope, configuration, and authority. |
| 9 | `sections/08-flags-conflict-surfacing-and-resolution.md` | Flags, Conflict Surfacing, And Resolution | Flag / Resolution | Keeps detection source facts separate from flag lifecycle and resolution. |
| 10 | `sections/09-local-data-lifecycle-and-operational-constraints.md` | Local Data Lifecycle And Operational Constraints | Local Data Lifecycle | Needed before sensitive local lifecycle or scope contraction details become implementation work. |
| 11 | `sections/10-reporting-aggregation-and-freshness.md` | Reporting, Aggregation, And Freshness | Reporting / Aggregation | Must remain projection-derived and access-scoped. |
| 12 | `sections/11-trigger-and-reactivity.md` | Trigger And Reactivity | Configuration | Must respect bounded configuration, detect-before-act, and event-store write path. |
| 13 | `sections/12-import-export-and-external-compatibility.md` | Import, Export, And External Compatibility | Event Envelope / Schema | Preserves external compatibility without making external schemas canonical. |
| 90 | `sections/90-open-decisions-and-gap-register-citations.md` | Open Decisions And Gap Register Citations | professional-baseline source authority | Keeps gaps visible and prevents accidental closure. |
| 91 | `sections/91-rejected-alternatives.md` | Rejected Alternatives | professional-baseline source authority | Makes rejected paths easy to enforce. |
| 92 | `sections/92-change-control-log.md` | Change-Control Log | Cross-boundary process | Records accepted baseline changes, disputes, and formal reopens. |

## Batch Plan

### Batch 0: Planning Scaffold

Files:

- `README.md`
- `section-registry.yml`
- `process/01-platform-spec-section-operating-plan.md`
- `process/02-platform-spec-section-template.md`
- `process/03-section-inventory-and-writing-order.md`
- `process/04-planned-consumer-review-cards.md`

Acceptance rule:

- These files define process only. They must not create platform behavior.
- The registry is a manifest only; accepted source documents and accepted section files remain authoritative.

### Batch 1A: Control Foundation

Files:

- `sections/00-specification-source-authority.md`
- `sections/90-open-decisions-and-gap-register-citations.md`
- `sections/91-rejected-alternatives.md`
- `sections/92-change-control-log.md`

Primary risks:

- open gaps being silently closed
- rejected paths returning under new names
- draft material being treated as implementation authority
- change-control triggers being handled only from memory

Acceptance rule:

- These files may route and protect decisions, but they must not introduce platform behavior.

### Batch 1B: Foundation Behavior

Files:

- `sections/01-core-definitions-and-boundary-vocabulary.md`
- `sections/02-event-log-and-storage-model.md`
- `sections/03-event-envelope-schema-and-references.md`

Primary risks:

- envelope drift
- type vs shape collapse
- projection state becoming canonical
- source hierarchy confusion

Acceptance rule:

- Later sections can reference these without re-reading ADR prose.
- Foundation behavior sections are accepted as a batch, not promoted one by one.
- Before acceptance, Challenge Review must check rejected paths and change-control triggers.
- Before acceptance, Integration Review must check planned consumers `04`, `05`, `06`, `07`, and `08` for identity, authority, configuration, projection/workflow, and flag/conflict risk surfaces.
- Because these consumers are still planned, use `process/04-planned-consumer-review-cards.md` as the review surface instead of creating skeleton section files.
- Planned-consumer review cards are superseded when their sections are drafted; they are not downstream contracts.

### Batch 2: Core Boundaries

Files:

- `sections/05-references-and-identity-lineage.md`
- `sections/06-configuration-and-parametrization.md`
- `sections/07-assignment-authority-and-sync.md`

Primary risks:

- identity absorbing actor, assignment, process, workflow, or reporting lifecycles
- deployer configuration becoming arbitrary platform logic
- account, group, tenant, or IdP claims becoming authority sources
- `activity_ref` being overread as authority context

Acceptance rule:

- References, configuration, and authority can evolve independently through named contracts.

### Batch 3: Derived Behavior

Files:

- `sections/09-projections-workflow-and-patterns.md`
- `sections/10-conflict-flag-and-resolution.md`
- `sections/11-trigger-reactivity.md`

Primary risks:

- workflow state becoming stored canonical state
- invalid transitions being rejected instead of accepted and flagged where the baseline says otherwise
- conflict detection and flag lifecycle merging into one broad subsystem
- triggers firing before relevant checks run

Acceptance rule:

- Derived behavior consumes events, configuration, authority, and projections without owning their source facts.

### Batch 4: Operational Surfaces

Files:

- `sections/08-local-data-lifecycle.md`
- `sections/12-reporting-aggregation-and-freshness.md`
- later export, retention, deployment, and operations sections if needed

Primary risks:

- reports becoming authority shortcuts
- freshness being implied as real-time visibility
- retain-and-hide being treated as sufficient sensitive-data handling
- external interoperability reshaping canonical events

Acceptance rule:

- Operational surfaces preserve core invariants and route unresolved policy to open decisions.

### Batch 5: Control Register Maintenance

Files:

- `sections/90-open-decisions-and-gap-register-citations.md`
- `sections/91-rejected-alternatives.md`
- `sections/92-change-control-log.md`

Acceptance rule:

- Every open gap, rejected path, and accepted baseline change continues to have one visible place to land.

## Per-Section Drafting Order

For each section:

1. copy the template from `process/02-platform-spec-section-template.md`
2. assign the primary boundary
3. list accepted source basis and routed constraints
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

Current hold-backs live in `sections/90-open-decisions-and-gap-register-citations.md`.
Current rejected paths live in `sections/91-rejected-alternatives.md`.
Do not maintain duplicate hold-back or rejected-path lists in this inventory.

## Completion Definition

Platform specification section drafting is complete enough for implementation planning when:

- foundation and core-boundary sections are accepted
- derived-behavior sections have at least draft contracts
- all high-risk gaps are visible in `sections/90-open-decisions-and-gap-register-citations.md`
- rejected paths are consolidated in `sections/91-rejected-alternatives.md`
- implementation designs can map to platform-spec sections without inventing architecture
