# Platform Specification Kernel Extraction State

Status: Iteration 2 in progress

This file is the durable state carrier for extracting platform-specification kernels from the approved source set. It exists so the work can survive context compaction without drifting into unapproved sources, stale memory, ADR-shaped organization, or premature atomization.

## Session Source Boundary

Authoritative source set for this extraction:

- `docs/access-control-scenario.md`
- `docs/constraints.md`
- `docs/README.md`
- `docs/scenarios/`
- `docs/principles.md`
- `docs/viability-assessment.md`
- `docs/behavioral_patterns.md`
- `docs/exploration/archive/`
- `docs/adrs/`

Forbidden for this extraction:

- Any docs outside the source set above.
- Any markdown files directly under `docs/exploration/` that are not inside `docs/exploration/archive/`.
- Architecture, implementation, ships, convergence, reviews, flagged positions, checkpoints, experiments, code, git history, and prior assistant memory.
- `docs/flagged-positions.md`, `docs/charter.md`, and `docs/status.md` are explicitly excluded. They must not be referenced, scanned, inferred from, or treated as existing context for this extraction.
- `docs/exploration/archive/22-system-evolution-map.md` as a source of taxonomy, conclusions, closure, or strategy. It may be acknowledged only as timeline context and must not influence extraction output.

Special ADR handling:

- `docs/adrs/adr-002-addendum-type-vocabulary.md` is not authoritative. It may be treated only as drift-history context where a later authoritative ADR absorbs, supersedes, or restates a point.
- `docs/adrs/adr-006-flag-semantics.md` is superseded by `docs/adrs/adr-006-flag-semantics-R.md`. Use ADR-006-R as the authoritative source for flag semantics. ADR-006 text is usable only where ADR-006-R carries it forward.
- ADR-006 through ADR-009 may contain valid closed points, but their convergence/classification style requires stricter extraction than ADR-001 through ADR-005.

## Output Goal

The immediate output is not a final platform specification and not a lineage archive. The immediate output is a small number of controlled working documents that accumulate atomic platform-specification kernels until the allowed source set reaches rest state.

Rest state means:

- Every candidate kernel has a final status.
- Every settled kernel has an approved source basis.
- Every open item is explicitly open.
- Every rejected alternative is explicitly rejected.
- Every conditional claim names its validity conditions.
- No kernel conflicts with another kernel.
- No kernel depends on forbidden sources.
- Atomic sections can be split into final files without changing meaning.

## Working Documents

- `00-extraction-state.md`: source boundary, scan cursor, closure rules, conflict log, and iteration history.
- `01-kernel-working-draft.md`: atomic kernel sections staged in one file until the full source set is processed.

Final atomic files must not be created until rest state is reached.

## Kernel Definition

A platform-specification kernel is the smallest technical statement that can later be lifted into a Platform Specification Document without depending on ADR narrative structure.

Acceptable kernel kinds:

- primitive
- contract
- invariant
- algorithm
- interaction rule
- configuration boundary
- forbidden interpretation
- open question
- rejected alternative
- conditional validity rule

ADR numbers, exploration filenames, and document headings are source anchors only. They do not define kernel boundaries.

## Closure Rules

A kernel may be marked `Settled` only when one of these is true:

- A decided ADR explicitly commits the statement.
- A decided ADR carries the statement forward from an earlier source.
- An approved ground-truth document states the statement as a platform constraint, confirmed principle, or scenario requirement.
- Exploration evidence converges and no later approved source contradicts it; in that case the closure basis must say `settled-by-evidence`, not `ADR-settled`.

A kernel remains `Candidate` when it appears only as exploration reasoning and has not reached closure.

A kernel becomes `Rejected` only when an approved source rejects it or a later decided ADR rules it out.

A kernel becomes `Conditional` only when the approved source states a dependency on assumptions, constraints, scenario subset, phase boundary, or validity condition.

A kernel becomes `Open` only when approved sources explicitly defer it, flag it as unresolved, or state that it is undecided.

## Anti-Drift Rules

- Do not promote options, alternatives, attacks, hypotheses, or stress-test branches into settled kernels unless the same approved source concludes them or a later decided ADR commits them.
- Rejected alternatives become guardrails, not design options.
- Exploration files provide proof, failure modes, and conditionality. ADRs provide closure when they decide.
- Ledger, charter, or update prose inside ADR-006 through ADR-009 does not create platform-spec content unless backed by decision text.
- Forbidden-pattern sections can produce constraint kernels only where the ADR marks them binding or explicitly forbidden.
- Forward-reference sections can produce open-question kernels only when the question remains open within approved sources.

## Scan Order

Iteration order is source order, not output structure:

1. Ground truth docs in the user-provided order:
   - `docs/access-control-scenario.md`
   - `docs/constraints.md`
   - `docs/README.md`
   - `docs/scenarios/`
   - `docs/principles.md`
   - `docs/viability-assessment.md`
   - `docs/behavioral_patterns.md`
2. `docs/exploration/archive/` in filename order, excluding `22-system-evolution-map.md` as an extraction source.
3. `docs/adrs/` in decision order, with the special handling above.

For every processed source:

1. Update the scan cursor.
2. Extract candidate claims.
3. Classify each claim by kernel kind.
4. Merge or add sections in `01-kernel-working-draft.md`.
5. Run a local conflict check.
6. Record open conflicts or closure changes here before moving on.

## Scan Cursor

Current iteration: 2

Processed sources:

- `docs/access-control-scenario.md`
- `docs/constraints.md`

Next source:

- `docs/README.md`

Ignored-as-source:

- `docs/exploration/archive/22-system-evolution-map.md`

## Conflict Log

No conflicts recorded yet.

## Candidate Kernel Register

- `Contextual Authority` — settled requirement from `docs/access-control-scenario.md`; concrete mechanism unresolved.
- `Access Scope Partitioning` — settled requirement from `docs/access-control-scenario.md`; sync/projection mechanics unresolved.
- `Temporary Access Lifecycle` — settled requirement from `docs/access-control-scenario.md`; grant/revocation protocol unresolved.
- `Role And Responsibility Transition Preservation` — settled requirement from `docs/access-control-scenario.md`; lifecycle representation unresolved.
- `Hierarchical Visibility With Exceptions` — settled requirement from `docs/access-control-scenario.md`; hierarchy/exception mechanism unresolved.
- `Access Rule Evolvability` — settled requirement from `docs/access-control-scenario.md`; configuration boundary unresolved.
- `Offline Access Divergence` — settled requirement from `docs/access-control-scenario.md`; reconciliation behavior unresolved.
- `Authority-Context Attribution` — settled requirement from `docs/access-control-scenario.md`; storage/reconstruction model unresolved.
- `Tiered Operator Contexts` — settled operational constraint from `docs/constraints.md`; persona-specific UI and data contracts unresolved.
- `Offline Primary Field Operations` — settled operational constraint from `docs/constraints.md`; sync and storage mechanics unresolved.
- `Synced-State Oversight` — settled operational constraint from `docs/constraints.md`; freshness representation unresolved.
- `Large Deployment Scale Envelope` — settled operational constraint from `docs/constraints.md`; performance budgets unresolved.
- `Continuous Record Accumulation` — settled operational constraint from `docs/constraints.md`; retention and local lifecycle mechanics unresolved.
- `Concurrent Activities In One Platform` — settled operational constraint from `docs/constraints.md`; activity/configuration mechanics unresolved.
- `Compliance Mechanism Boundary` — settled operational constraint from `docs/constraints.md`; jurisdiction-specific policy content out of scope.
- `Interoperability Compatibility` — settled operational constraint from `docs/constraints.md`; integration implementation deferred.
- `Responsiveness By Work Tier` — settled operational constraint from `docs/constraints.md`; latency budgets are requirement-level only.
- `Configuration Changes Propagate On Sync` — settled operational constraint from `docs/constraints.md`; versioning and conflict mechanics unresolved.

## Iteration History

### Iteration 0

Created the extraction state and working draft structure. No source claims have been promoted.

### Iteration 1

Processed `docs/access-control-scenario.md`. Extracted access-control requirements as settled ground-truth kernels, with concrete mechanisms left open for later approved sources.

### Iteration 2

Processed `docs/constraints.md`. Extracted operational-environment requirements as settled constraints, with concrete data, sync, configuration, and interface mechanisms left open for later approved sources.
