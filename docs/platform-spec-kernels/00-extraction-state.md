# Platform Specification Kernel Extraction State

Status: Iteration 13 in progress

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

Ground-truth document handling:

- `docs/*.md` files in the approved ground-truth set and `docs/scenarios/` are domain, vision, ambition, and requirements sources. They predate exploration and ADRs and must not be read as implying a specific architecture or implementation path unless they explicitly state a requirement.
- If an approved ground-truth file contains links or reading-order instructions to forbidden docs, those links and instructions are ignored for extraction purposes.
- `docs/viability-assessment.md` is the first architecture-toned narrowing pass. It evaluates whether the domain ground truth and scenarios describe a buildable platform. It may create viability, tension, risk, blind-spot, and conditional-scope kernels, but it does not close final architecture or implementation decisions.
- Terms such as "primitive" in `docs/viability-assessment.md` are candidate abstraction language from pre-architecture narrowing, not final platform primitive classification.
- `docs/principles.md` sets pre-architecture decision guidance derived from vision, constraints, and behavioral patterns. Its later confirmation annotations can mark principles as validated guidance, but embedded ADR examples must not be used to close detailed platform interfaces before ADR extraction.
- Archive processing begins with `docs/exploration/archive/01-architecture-landscape.md` before `00-exploration-framework.md`. Although numerically second, it was the first architecture-landscape exploration artifact and guided the ADR order and framework shape. It is superseded/raw exploration, so it can create lineage, tradeoff, prior-art, and candidate-decision-order kernels, but not final architecture closure.
- `docs/exploration/archive/00-exploration-framework.md` is methodology only. It describes how agents should explore and write ADRs. Its sample ADR dependency order is optimistic/non-authoritative and must not be treated as a final ordering or as architecture closure.

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
- `01-kernel-working-draft.md`: staging index, kernel template, and rest-state split targets.
- `02-domain-requirement-kernels.md`: temporary staging file for ground-truth, scenario-index, and early scenario requirement kernels.
- `03-behavioral-viability-principle-kernels.md`: temporary staging file for behavioral-pattern, viability-assessment, and principle kernels.
- `04-architecture-lineage-kernels.md`: temporary staging file for architecture-landscape and ADR-lineage kernels.
- `05-methodology-and-extraction-rules.md`: temporary staging file for extraction-methodology kernels.

Final atomic files must not be created until rest state is reached.

The numbered staging files are context-management groups only. They are not final atomic documents and must not define final platform-spec organization.

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

## Probe Handling

Probe reads are allowed only inside the approved source boundary and only to improve extraction sequencing. A probe read does not mark a source as processed, does not promote kernels, and does not change closure.

Current probe notes:

- `docs/scenarios/*.md` were probed after `docs/scenarios/README.md`. The files are small and can be processed file-by-file or in small coherent batches without context pressure.
- `docs/behavioral_patterns.md` was probed before formal processing. It is a behavioral narrowing document, not an architectural source. It should inform scenario extraction by naming recurring behaviors, but it must not be treated as deciding platform primitives, storage models, interfaces, or implementation mechanisms.
- Scenario extraction should use behavioral patterns as a cross-check after each scenario: capture the scenario's domain pressure first, then note which behavioral pattern evidence it supports if the mapping is explicit or later confirmed.
- User-approved scan-order adjustment: `docs/behavioral_patterns.md` is formally processed with the compressed scenario pass before `docs/viability-assessment.md`, because it is a behavioral, pre-architecture narrowing over the scenario set and helps avoid scenario-file overfitting.

## Scan Cursor

Current iteration: 13

Processed sources:

- `docs/access-control-scenario.md`
- `docs/constraints.md`
- `docs/README.md`
- `docs/scenarios/README.md`
- `docs/scenarios/00-basic-structured-capture.md`
- `docs/scenarios/01-entity-linked-capture.md`
- `docs/scenarios/02-periodic-reporting.md`
- `docs/scenarios/03-user-based-assignment.md`
- `docs/scenarios/04-supervisor-review.md`
- `docs/scenarios/05-supervision-audit-visits.md`
- `docs/scenarios/06-entity-registry-lifecycle.md`
- `docs/scenarios/07-resource-distribution.md`
- `docs/scenarios/08-case-management.md`
- `docs/scenarios/09-coordinated-campaign.md`
- `docs/scenarios/10-dynamic-targeting.md`
- `docs/scenarios/11-multi-step-approval.md`
- `docs/scenarios/12-event-triggered-actions.md`
- `docs/scenarios/13-cross-flow-linking.md`
- `docs/scenarios/14-multi-level-distribution.md`
- `docs/scenarios/15-cross-program-overlays.md`
- `docs/scenarios/16-emergency-rapid-response.md`
- `docs/scenarios/18-advanced-analytics-derived-flows.md`
- `docs/scenarios/19-offline-capture-and-sync.md`
- `docs/scenarios/20-chv-field-operations.md`
- `docs/scenarios/21-chv-supervisor-operations.md`
- `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
- `docs/behavioral_patterns.md`
- `docs/viability-assessment.md`
- `docs/principles.md`
- `docs/exploration/archive/01-architecture-landscape.md`
- `docs/exploration/archive/00-exploration-framework.md`

Next source:

- `docs/exploration/archive/02-adr1-offline-data-model.md`

Ignored-as-source:

- `docs/exploration/archive/22-system-evolution-map.md`

## Conflict Log

No blocking conflicts recorded yet.

Rest-state merge note:

- Iterations 6 and 7 extracted S00/S01 granular scenario kernels before the compressed behavioral pass. They remain valid as scenario evidence, but later rest-state cleanup may merge or cross-reference them under behavioral kernels such as `Structured Recording Behavior`, `Subject Linkage Behavior`, `Shape Evolution Behavior`, and `Offline-First Work Behavior`.

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
- `Common Operational Substrate` — settled vision requirement from `docs/README.md`; concrete platform primitives unresolved.
- `Setup Not Built` — settled vision requirement from `docs/README.md`; configuration boundary unresolved.
- `Coherent Single-System Experience` — settled vision requirement from `docs/README.md`; common contracts and concepts unresolved.
- `Operational Adaptability Without Rebuild` — settled vision requirement from `docs/README.md`; evolution mechanisms unresolved.
- `Domain-Agnostic Field Operations` — settled ambition constraint from `docs/README.md`; cross-domain validation remains evidence-driven.
- `Scenario Problem-Space Boundary` — settled extraction rule from `docs/scenarios/README.md`; scenarios do not prescribe constructs or architecture.
- `Operational Complexity Progression` — settled scenario-ordering context from `docs/scenarios/README.md`; not an implementation sequence.
- `Scenario Phase Boundary` — settled requirements-scope classification from `docs/scenarios/README.md`; Phase 2 scenarios are compatible but deferred as drivers.
- `Foundational Scenario Evidence Set` — settled extraction priority from `docs/scenarios/README.md`; foundational scenarios must be read before compositional scenarios.
- `Structured Capture Baseline` — settled scenario requirement from `docs/scenarios/00-basic-structured-capture.md`; storage primitive unresolved.
- `Coexisting Record Shapes` — settled scenario requirement from `docs/scenarios/00-basic-structured-capture.md`; versioning mechanism unresolved.
- `Duplicate Independent Capture Pressure` — settled scenario pressure from `docs/scenarios/00-basic-structured-capture.md`; identity/conflict mechanism unresolved.
- `Traceable Record Correction` — settled scenario requirement from `docs/scenarios/00-basic-structured-capture.md`; correction model unresolved.
- `Subject-Linked Record History` — settled scenario requirement from `docs/scenarios/01-entity-linked-capture.md`; subject identity model unresolved.
- `Duplicate Subject Identity Pressure` — settled scenario pressure from `docs/scenarios/01-entity-linked-capture.md`; duplicate identity handling unresolved.
- `Identity Ambiguity Over Time` — settled scenario pressure from `docs/scenarios/01-entity-linked-capture.md`; identity lifecycle semantics unresolved.
- `Subject History Ordering Under Sync` — settled scenario pressure from `docs/scenarios/01-entity-linked-capture.md`; ordering model unresolved.
- `Behavioral Pattern Boundary` — settled extraction rule from `docs/behavioral_patterns.md`; behavioral patterns are not architecture.
- `Structured Recording Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; technical recording primitive unresolved.
- `Subject Linkage Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; subject identity mechanism unresolved.
- `Temporal Rhythm Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; scheduling mechanism unresolved.
- `Responsibility Binding Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; assignment/access mechanism unresolved.
- `Hierarchical Visibility Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; visibility model unresolved.
- `Review And Judgment Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; workflow/review mechanism unresolved.
- `Transfer With Acknowledgment Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; transfer protocol unresolved.
- `State Progression Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; state model unresolved.
- `Condition-Triggered Action Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; trigger mechanism unresolved.
- `Cross-Reference Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; linking mechanism unresolved.
- `Shape Evolution Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; shape/version mechanism unresolved.
- `Offline-First Work Behavior` — settled behavioral requirement from scenarios and `docs/behavioral_patterns.md`; sync/reconciliation mechanism unresolved.
- `Behavioral Composition Without New Patterns` — settled behavioral validation from `docs/behavioral_patterns.md`; architecture decomposition unresolved.
- `Conditional Platform Viability` — settled viability verdict from `docs/viability-assessment.md`; architecture exploration allowed under conditions.
- `No Hard Scenario Conflicts` — settled viability finding from `docs/viability-assessment.md`; not a proof of final architecture.
- `Configuration Boundary Collapse Risk` — settled high-severity viability risk from `docs/viability-assessment.md`; boundary unresolved.
- `Offline Reactivity Eventual-Consistency Tension` — settled viability tension from `docs/viability-assessment.md`; reactive mechanism unresolved.
- `Domain Mechanism Content Separation` — settled viability direction from `docs/viability-assessment.md`; validation/configuration mechanism unresolved.
- `Trustworthy Records Offline Correction Tension` — settled viability tension from `docs/viability-assessment.md`; write/conflict model unresolved.
- `Phase 2 Deferral Guardrail` — settled viability condition from `docs/viability-assessment.md`; deferred pressures must not drive initial architecture.
- `Setup Experience Blind Spot` — settled viability blind spot from `docs/viability-assessment.md`; setup/admin evidence missing in scenarios.
- `Retention And Archival Blind Spot` — settled viability blind spot from `docs/viability-assessment.md`; data lifecycle unresolved.
- `Onboarding And Role Transition Blind Spot` — settled viability blind spot from `docs/viability-assessment.md`; lifecycle UX unresolved.
- `Reporting Aggregation Blind Spot` — settled viability blind spot from `docs/viability-assessment.md`; aggregation scope unresolved.
- `Domain-Agnosticism Proof Gap` — settled viability blind spot from `docs/viability-assessment.md`; non-health composite validation not yet strong.
- `Low-End Device Scale Risk` — settled viability risk from `docs/viability-assessment.md`; selective sync/local lifecycle unresolved.
- `Principle Validation Lifecycle` — settled process rule from `docs/principles.md`; principles guide decisions and can be confirmed/refined/challenged.
- `Offline Default Principle` — settled validated principle from `docs/principles.md`; detailed sync/storage mechanics unresolved.
- `Bounded Configuration Principle` — settled validated principle from `docs/principles.md`; final configuration boundary unresolved.
- `Append-Only History Principle` — settled validated principle from `docs/principles.md`; detailed write model unresolved until ADR extraction.
- `Composition Over Exceptions Principle` — settled validated principle from `docs/principles.md`; final primitive vocabulary unresolved.
- `Conflict Surfacing Principle` — settled validated principle from `docs/principles.md`; flag/conflict mechanisms unresolved until ADR extraction.
- `Contextual Auditable Authority Principle` — settled validated principle from `docs/principles.md`; authority contract unresolved until ADR extraction.
- `Simplicity Baseline Principle` — settled validated principle from `docs/principles.md`; technical simplicity proof unresolved until ADR extraction.
- `Architecture Landscape Superseded Boundary` — settled extraction rule from `docs/exploration/archive/01-architecture-landscape.md`; raw exploration cannot close final architecture.
- `Constraint Filter Survivors` — settled exploration finding from `docs/exploration/archive/01-architecture-landscape.md`; final mechanisms unresolved.
- `Prior-Art Failure Mode Set` — settled exploration evidence from `docs/exploration/archive/01-architecture-landscape.md`; used as design guardrails.
- `Hybrid Architecture Candidate` — candidate exploration direction from `docs/exploration/archive/01-architecture-landscape.md`; final decision unresolved until ADRs.
- `Offline Data Model Dependency Root` — candidate decision-order finding from `docs/exploration/archive/01-architecture-landscape.md`; later ADR closure required.
- `Configuration Boundary Depends On Upstream Decisions` — candidate decision-order correction from `docs/exploration/archive/01-architecture-landscape.md`; later ADR closure required.
- `Critical Decision Coupling Map` — candidate exploration map from `docs/exploration/archive/01-architecture-landscape.md`; dependency closure required.
- `ADR Exploration Sequence Candidate` — candidate exploration sequence from `docs/exploration/archive/01-architecture-landscape.md`; final ADR bodies close outcomes.
- `Exploration Framework Methodology Boundary` — settled extraction rule from `docs/exploration/archive/00-exploration-framework.md`; framework is process, not architecture.
- `Assumption Discipline` — settled methodology rule from `docs/exploration/archive/00-exploration-framework.md`; assumptions must not become decisions silently.
- `Exploration Narrowing Not Decision` — settled methodology rule from `docs/exploration/archive/00-exploration-framework.md`; directional leans are not final commitments.
- `Flagged Upstream Problem Handling` — settled methodology rule from `docs/exploration/archive/00-exploration-framework.md`; blocking/informational flags preserve conditionality.
- `Irreversibility Filter Method` — settled methodology rule from `docs/exploration/archive/00-exploration-framework.md`; stress depth scales by permanence.
- `Decision Audit Gate Method` — settled methodology rule from `docs/exploration/archive/00-exploration-framework.md`; audit checks assumptions, scope bleed, gaps, and decision placement.
- `ADR Scope Hygiene Method` — settled methodology rule from `docs/exploration/archive/00-exploration-framework.md`; ADRs commit decisions, exploration preserves journey.

## Iteration History

### Iteration 0

Created the extraction state and working draft structure. No source claims have been promoted.

### Iteration 1

Processed `docs/access-control-scenario.md`. Extracted access-control requirements as settled ground-truth kernels, with concrete mechanisms left open for later approved sources.

### Iteration 2

Processed `docs/constraints.md`. Extracted operational-environment requirements as settled constraints, with concrete data, sync, configuration, and interface mechanisms left open for later approved sources.

### Iteration 3

Processed `docs/README.md`. Extracted vision and ambition kernels only. Ignored current-authority and documentation-reading-guide content because it points to excluded docs and is not domain/requirements content for this extraction.

### Iteration 4

Processed `docs/scenarios/README.md` as the index for the scenario directory. Extracted scenario-directory guardrails and scan plan only. Scenario files will be processed individually in filename order, starting with `docs/scenarios/00-basic-structured-capture.md`, so scenario evidence does not get compressed into one oversized pass.

### Iteration 5

Probe-only pass over `docs/scenarios/*.md` and `docs/behavioral_patterns.md`. No kernels were promoted and no source was marked processed. The result is a persisted scenario extraction strategy: process scenarios from domain pressure first, use behavioral patterns only as a non-architectural cross-check, and keep formal behavioral-pattern extraction for its later scan turn.

### Iteration 6

Processed `docs/scenarios/00-basic-structured-capture.md`. Extracted S00 as the structured-capture simplicity baseline and captured its domain pressures around shape coexistence, duplicate independent capture, and traceable correction. No storage, event, schema, sync, or conflict mechanism was promoted.

### Iteration 7

Processed `docs/scenarios/01-entity-linked-capture.md`. Extracted subject-linked capture requirements and identity pressures around duplicate identities, changing or ambiguous identity, and out-of-order offline subject history. No identity model, merge/split mechanism, aliasing model, or ordering protocol was promoted.

### Iteration 8

Processed the remaining scenario files (`docs/scenarios/02-*` through `22-*`, excluding nonexistent sequence numbers) together with `docs/behavioral_patterns.md` as a compressed behavioral-domain pass. This pass deliberately avoids architecture boundaries, implementation scope, and later ADR assumptions. It extracts the common domain behaviors and composition evidence only. Earlier S00/S01 granular kernels remain as evidence seeds and may be merged or cross-referenced during rest-state cleanup.

### Iteration 9

Processed `docs/viability-assessment.md` as the first architecture-toned narrowing pass. Extracted conditional viability, tensions, risks, guardrails, and blind spots. Did not treat candidate primitive language or mitigation suggestions as final architecture; later exploration and ADR sources must close or correct those points.

### Iteration 10

Processed `docs/principles.md` as validated pre-architecture decision guidance. Extracted the seven principles and their testing lifecycle. Did not use embedded ADR confirmation examples to close detailed interfaces or mechanisms; those remain for ADR extraction.

### Iteration 11

Processed `docs/exploration/archive/01-architecture-landscape.md` before `00-exploration-framework.md`, per user correction. Extracted constraint-filter survivors, prior-art lessons, viable architecture families, hybrid candidate direction, and decision-coupling/ADR-order lineage. The document is superseded raw exploration; no final architecture closure was promoted.

### Iteration 12

Processed `docs/exploration/archive/00-exploration-framework.md` as methodology only. Extracted assumption discipline, narrowing-vs-decision separation, flag handling, irreversibility filtering, decision audit, and ADR scope hygiene. Did not treat its sample ADR dependency order as authoritative.

### Iteration 13

Housekeeping-only split for context control. Replaced the oversized single working draft with a staging index plus four temporary staging files: domain requirement kernels, behavioral/viability/principle kernels, architecture-lineage kernels, and methodology/extraction-rule kernels. No source was processed, no cursor advanced, and no kernel status changed.
