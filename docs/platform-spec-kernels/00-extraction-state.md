# Platform Specification Kernel Extraction State

Status: Iteration 32 ADR-004 session3 part4 extracted

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
- ADR-001-specific handling: `docs/exploration/archive/02-adr1-offline-data-model.md` is the primary ADR-001 exploration lineage source, and `docs/exploration/archive/04-decision-audit.md` is the primary ADR-001 audit/closure lineage source. `docs/exploration/archive/03-adr1-forward-projection.md` is valid ADR-001 lineage, but each claim must be classified as either an ADR-001 consequence, a candidate ADR-001 selection pressure, or a deferred downstream closure candidate owned by ADR-002 through ADR-005. Do not let forward projection decide downstream ADRs by itself, and do not discard a projected claim merely because it belongs to downstream territory. Later owning ADR sources may promote, abandon, contradict, or keep it conditional.

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
- `06-adr2-identity-conflict-kernels.md`: temporary staging file for ADR-002 identity and conflict lineage kernels.
- `07-adr3-authorization-sync-kernels.md`: temporary staging file for ADR-003 authorization and selective-sync lineage kernels.
- `08-adr4-configuration-boundary-kernels.md`: temporary staging file for ADR-004 configuration-boundary lineage kernels.

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

Current iteration: 32

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
- `docs/exploration/archive/02-adr1-offline-data-model.md`
- `docs/exploration/archive/03-adr1-forward-projection.md`
- `docs/exploration/archive/04-decision-audit.md`
- `docs/exploration/archive/05-adr2-event-storm-identity.md`
- `docs/exploration/archive/07-adr2-phase2-stress-test-results.md`
- `docs/exploration/archive/09-adr2-phase3-classification-results.md`
- `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md`
- `docs/exploration/archive/11-adr3-phase2-stress-test.md`
- `docs/exploration/archive/12-adr3-course-correction.md`
- `docs/adrs/adr-003-authorization-sync.md`
- `docs/exploration/archive/13-adr4-session1-scoping.md`
- `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md`
- `docs/exploration/archive/15-adr4-session3-part1-structural-coherence.md`
- `docs/exploration/archive/16-adr4-session3-part2-irreversibility-filter.md`
- `docs/exploration/archive/17-adr4-session3-part3-adversarial-stress-tests.md`
- `docs/exploration/archive/18-adr4-session3-part4-remaining-q-resolution.md`

Next source:

- `docs/adrs/adr-004-configuration-boundary.md`

Ignored-as-source:

- `docs/exploration/archive/22-system-evolution-map.md`

## Conflict Log

No blocking conflicts recorded yet.

Rest-state merge note:

- Iterations 6 and 7 extracted S00/S01 granular scenario kernels before the compressed behavioral pass. They remain valid as scenario evidence, but later rest-state cleanup may merge or cross-reference them under behavioral kernels such as `Structured Recording Behavior`, `Subject Linkage Behavior`, `Shape Evolution Behavior`, and `Offline-First Work Behavior`.

## Deferred Closure Register

Deferred closure candidates are claims that appeared in an earlier source but belong to a later owning ADR or later closure source. They must survive until the owning sources are processed.

Allowed outcomes when the owning source is processed:

- `promoted`: later source commits or carries the claim forward.
- `abandoned`: later source drops the claim or chooses a different model without carrying it.
- `contradicted`: later source reverses the claim.
- `conditional`: later source keeps the claim valid only under named assumptions.
- `open`: later source explicitly leaves the claim unresolved.

Current deferred candidates:

- `03-forward-projection / ADR-002 identity-conflict`: event/action-log stream-linking, event-level conflict context, action-log view conflict state, and snapshot full-state merge friction. Outcome after `09-adr2-phase3-classification-results.md`: classified into ADR-002 Bucket 1 constraints, ADR-002 Bucket 2 strategies, Bucket 3 deferrals to ADR-4/5, and Bucket 4 accepted risks. Final verification still owned by ADR-002.
- `03-forward-projection / ADR-003 authorization-sync`: event two-tier sync, action-log dual sync paths, snapshot full-snapshot scaling pressure, and stale-access handling consequences. Outcome after ADR-003: promoted into assignment-based access, sync-scope-as-access, authority-as-projection, original-subject authorization, assignment scope containment, online-only resolution, authorization detect-before-act, tiered projection strategy, authorization stale-flag strategy, scope-change data strategy, accepted risks, and explicit ADR-004/ADR-005 deferrals.
- `03-forward-projection / ADR-004 configuration`: event types as platform vocabulary, configurable shapes/assignments/schedules, projection-rule boundary pressure, action-log view-schema surface, and snapshot behavior-in-code ceiling. Owning sources: ADR-004 exploration files and ADR-004.
- `03-forward-projection / ADR-005 workflow`: separation of data and workflow under events, action-log conflict-time reprojection, snapshot fusion of approval action and data, and state-machine projection pressure. Owning sources: ADR-005 exploration files and ADR-005.
- `03-forward-projection / ADR-001 selection`: snapshot structural ceiling, events irreversibility advantage, action-log convergence/dual-write risk, and events projection-infrastructure risk. Outcome after `04-decision-audit.md`: promoted by audit toward typed immutable events/event-log source of truth, with final verification still owned by ADR-001.

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
- `ADR-001 Exploration Boundary` — settled extraction rule from `docs/exploration/archive/02-adr1-offline-data-model.md`; exploration narrows but ADR-001 decides.
- `Offline Data Model Irreversibility` — settled ADR-001 exploration risk classification; storage primitive migration is high-irreversibility.
- `Offline Data Model Subdecision Coupling` — settled ADR-001 exploration structure; record mutability, write granularity, identity generation, sync unit, and conflict semantics are coupled.
- `Append-Only Storage Forced` — conditional ADR-001 exploration conclusion; final closure waits for ADR-001.
- `Storage Unit Option Set` — open ADR-001 exploration question; snapshots vs events vs action log remains for ADR-001.
- `Snapshot Workflow Weakness` — conditional rejected-alternative lineage; final rejection waits for ADR-001.
- `Client-Generated Identity Forced` — conditional ADR-001 exploration conclusion; final closure waits for ADR-001 and ADR-002.
- `Immutable Record Sync Shape` — conditional ADR-001 exploration conclusion; ADR-003 closes scope/auth behavior.
- `Conflict Surfacing Default` — conditional ADR-001 exploration conclusion; later ADRs close policy.
- `Materialized Reads Required For Performance` — conditional event-like-model performance invariant; ADR-001 closes read model obligations.
- `ADR-001 Downstream Constraint Set` — conditional dependency map for ADR-002 through ADR-005.
- `ADR-001 Forward Projection Boundary` — settled extraction rule from `docs/exploration/archive/03-adr1-forward-projection.md`; downstream projections inform ADR-001 but do not decide ADR-002 through ADR-005.
- `Snapshot Structural Ceiling` — candidate rejected-alternative lineage; snapshots undercut configurable workflow and are lossy to migrate from.
- `Events Irreversibility Advantage` — candidate ADR-001 selection pressure; events preserve the safest migration path toward action-log-style views.
- `Action Log Convergence And Dual-Write Risk` — candidate ADR-001 selection pressure; hard cases push action log toward event-style reprojection while dual-write gaps risk traceability/current-state divergence.
- `Events Projection Infrastructure Risk` — conditional ADR-001 risk; events depend on reliable projection across versions, out-of-order arrival, and low-end devices.
- `Downstream Projection Spillover Guard` — settled extraction rule; projected ADR-002 through ADR-005 consequences must remain consequences unless later sources decide them.
- `ADR-001 Decision Audit Boundary` — settled extraction rule from `docs/exploration/archive/04-decision-audit.md`; audit normalizes ADR-001 scope and downstream ownership.
- `ADR-001 Event Storage Audit Closure` — conditional ADR-001 audit closure; typed immutable events, event-log source of truth, rebuildable projections, client UUIDs, immutable-event sync, and extensible envelope pending ADR-001 verification.
- `Write-Path Discipline Requirement` — conditional audit-required invariant; all state changes enter through event store and projections are derived.
- `Projection Rebuild Scope Deferral` — open audit deferral; local rebuild depends on ADR-003 sync scope.
- `ADR-001 Downstream Overreach Audit` — settled audit finding; X1-X6 belong to ADR-003 through ADR-005, not ADR-001.
- `ADR-002 Scope From Audit` — open owning-scope map for identity and conflict resolution.
- `ADR-003 Scope From Audit` — open owning-scope map for authorization, selective sync, projection location, and stale access.
- `ADR-004 Scope From Audit` — open owning-scope map for configuration boundary and activity context.
- `ADR-005 Scope From Audit` — open owning-scope map for state progression and workflow.
- `ADR-001 Normalization Safety Check` — conditional audit conclusion; proceed to ADR-002 only after ADR-001 changes are verified.
- `ADR-002 Event Storm Boundary` — settled extraction rule from `docs/exploration/archive/05-adr2-event-storm-identity.md`; Phase 1 discovers but does not decide.
- `Identity As Load-Bearing Event Reference` — candidate ADR-002 framing; identity makes event references meaningful.
- `Identity Type Taxonomy Candidate` — candidate taxonomy; subject, actor, process, and assignment identities.
- `Subject Identity Lifecycle Candidate` — candidate lifecycle; create, reference, update attributes, deactivate, merge, split, ambiguous.
- `Identity Merge And Split Lineage Candidate` — candidate invariant; lineage preserved, historical events not rewritten.
- `Conflict Taxonomy Candidate` — candidate taxonomy; additive, state, duplicate, stale, content, revoked-authority, cross-lifecycle conflicts.
- `Accept-And-Flag Stale Identity Work Candidate` — candidate interaction rule; stale offline work accepted as factual but flagged.
- `Causal Ordering Mechanism Open` — open ADR-002 mechanism decision; device time alone insufficient.
- `Assignment Identity Axis Candidate` — candidate identity primitive linking actor, scope, responsibility, and temporal authority.
- `Process Identity And Pending Match Candidate` — candidate identity primitive/pattern for shipment-like processes and unknown references.
- `ADR-002 Phase 2 Stress-Test Boundary` — settled extraction rule from `docs/exploration/archive/07-adr2-phase2-stress-test-results.md`; stress-test evidence, not final decision.
- `Accept-And-Flag Stress-Test Survivor` — conditional survivor with required modifications.
- `Single-Writer Conflict Resolution Requirement` — conditional requirement; termination rule for conflict resolution.
- `Structured Flag Root Cause And Batch Resolution Requirement` — conditional requirement; scalable flag grouping and backlog management.
- `Detect Before Act Sync Processing Requirement` — conditional algorithm requirement; conflict detection before downstream policies fire.
- `Alias Table Stress-Test Survivor` — conditional survivor with required modifications.
- `Corrective Split Over Unmerge Requirement` — conditional rejected-alternative lineage for symmetric unmerge.
- `Device Sequence Sync Watermark Survivor` — conditional causal-ordering survivor.
- `Device Time Advisory Requirement` — conditional event-envelope/ordering invariant.
- `Pending Match Bijective Constraint` — conditional invariant for unresolved-reference matching.
- `ADR-002 Phase 3 Classification Boundary` — settled extraction rule from `docs/exploration/archive/09-adr2-phase3-classification-results.md`; classification map, not final ADR.
- `ADR-002 Irreversibility Classification Rule` — settled classification method; stored-event/envelope changes are constraints, code/projection/UI changes are strategies or deferrals.
- `ADR-002 Event Envelope Constraint Set` — conditional Phase 3 closure; device_id, device_sequence, sync_watermark, and typed identity references pending ADR-002 verification.
- `Device Time Advisory Constraint` — conditional Phase 3 closure; device_time is not structural.
- `Merge Alias Projection Constraint` — conditional Phase 3 closure; merge uses alias-in-projection, never physical re-reference.
- `Corrective Split Constraint` — conditional Phase 3 closure; no SubjectsUnmerged event type.
- `Split Frozen-History Acyclicity Constraint` — conditional Phase 3 closure; split archives source and lineage is DAG by construction.
- `SubjectSplit Online-Only Constraint` — conditional Phase 3 closure; split is server-validated and not offline.
- `Conflict Resolution And Detection Constraints` — conditional Phase 3 closure; single-writer resolution, detect-before-act sync processing, raw-reference conflict detection.
- `Accept Stale Events Constraint` — conditional Phase 3 closure; validly structured stale-state events are accepted and flagged, not rejected.
- `ADR-002 Strategy Classification Set` — conditional strategy set; evolvable ADR-002 implementation/read-model choices.
- `ADR-002 Cross-ADR Deferral Set` — open deferral set; ADR-4/5 must close pending match generality, domain conflict rules, and workflow cascades.
- `ADR-002 Accepted Risk Set` — conditional accepted-risk set with revisit triggers.
- `ADR-002 Simplicity Validation` — conditional validation that Bucket 1 constraints do not materially complicate S00.
- `ADR-002 Pre-ADR Reconciliation Checkpoint` — housekeeping reconciliation; verifies what ADR-002 must confirm, keep strategy-bounded, defer, or preserve as accepted risk.
- `ADR-002 Decision Boundary` — settled ADR extraction rule from `docs/adrs/adr-002-identity-conflict.md`.
- `ADR-002 Envelope Contract` — ADR-settled event envelope additions and typed identity references.
- `ADR-002 Device Time Advisory Invariant` — ADR-settled timestamp/ordering invariant.
- `ADR-002 Identity Evolution Contract` — ADR-settled merge/split/corrective split contract.
- `ADR-002 Lineage Validation Contract` — ADR-settled DAG and online-only merge/split contract.
- `ADR-002 Conflict Contract` — ADR-settled single-writer, detect-before-act, raw-reference conflict contract.
- `ADR-002 Stale Event Acceptance Invariant` — ADR-settled accept-and-flag invariant for state staleness.
- `ADR-002 Explicit Deferral Contract` — ADR-settled deferral map to ADR-003, ADR-004, and ADR-005.
- `ADR-002 Accepted Risk Contract` — ADR-settled conditional validity and revisit triggers.
- `ADR-002 Reconciliation Result` — settled finding that ADR-002 confirms Phase 3 Bucket 1, preserves strategies, and defers cross-ADR items without contradiction.
- `ADR-003 Phase 1 Policy Scenario Boundary` — settled extraction rule from `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md`; Phase 1 scenario analysis does not decide ADR-003.
- `ADR-003 Upstream Assumption Compatibility` — settled extraction context; ADR-003 Phase 1 accepts ADR-001/ADR-002 commitments as inputs.
- `Authorization Staleness Accept-And-Flag Candidate` — candidate interaction rule; stale authorization work is accepted as factual and flagged, not rejected.
- `Assignment-Based Authorization Candidate` — candidate primitive; assignment as actor-role-scope-duration/process authorization unit.
- `Authority Context Envelope Candidate` — superseded Phase 1 candidate; course correction replaces envelope authority context with authority-as-projection pending ADR-003 verification.
- `Sync Scope As Offline Authorization Candidate` — candidate interaction rule; server sync scope is primary offline authorization mechanism.
- `Subject-Scoped Sync Candidate` — candidate sync rule; in-scope subject history is not limited by event author.
- `Scope Composition Candidate` — open high-severity question; standing and campaign assignments may compose but final scope algebra is undecided.
- `Temporal Authority Server-Side Enforcement Candidate` — candidate interaction rule; device time is advisory and temporal enforcement occurs on sync.
- `Tiered Projection And Sync Topology Candidate` — candidate conditional topology; field/supervisor/coordinator tiers may need different raw/summary projection placement.
- `Scope Contraction Data Handling Open` — open high-severity question; local data behavior after scope narrowing is undecided.
- `Hierarchical Scope Model Candidate` — candidate scope model; primary axis appears geographic/organizational hierarchy with possible subject-based exceptions.
- `Scope Transition Atomicity Open` — open sync-protocol question; assignment change and new-scope data delivery must avoid inconsistent partial transitions.
- `Shared Device Actor Scope Open` — open device/auth boundary; sync scope is actor-scoped while ADR-002 device identity is hardware-bound.
- `Resolver Designation Candidate` — candidate interaction rule; conflict resolver designation appears assignment-derived.
- `ADR-003 Phase 1 Hot Spot Set` — open ADR-003 risk checklist from Phase 1.
- `Auditor Access Exception Open` — open cross-boundary access exception not stressed by Phase 1.
- `ADR-003 Phase 2 Stress-Test Boundary` — settled extraction rule from `docs/exploration/archive/11-adr3-phase2-stress-test.md`; stress-test evidence does not decide final ADR-003.
- `Assignment Model Structural Extension Set` — conditional Phase 2 survivor; assignment model needs actor-as-subject visibility, lifecycle policy, sync atomicity, tagging, and alias-scope handling.
- `Assignment Creation Scope-Containment Invariant Candidate` — conditional constraint candidate; server validates new assignment scope against creator scope.
- `Role And Capability Enforcement Candidate` — conditional cross-ADR boundary; server validates role-action compatibility and capability restrictions.
- `Sync Scope Expansion And Resumption Strategy` — conditional strategy; priority sync and resumable delivery are needed for first sync/large scope expansion.
- `Scope Contraction Purge Constraint Candidate` — conditional lifecycle rule; sensitive data requires active purge/selective retain rather than retain-hide.
- `Retain-Hide And Indefinite Retain Rejection For Sensitive Data` — conditional rejected-alternative lineage; UI-hide and indefinite local retention are rejected for sensitive scoped data.
- `Authorization Flag Detect-Before-Act Candidate` — conditional algorithm; authorization flags must block downstream policy execution where required.
- `Authorization Flag Coordination Candidate` — conditional algorithm; multi-flag events require bundled or ordered resolution.
- `Conflict Resolution Online-Only Candidate` — conditional invariant; resolver authority should be checked online at resolution time.
- `Sensitive Subject Authorization Exception Open` — open design-constraint collision between accept-and-flag and sensitive-data compliance.
- `Projection Freshness Metadata Candidate` — conditional contract; summaries and projections need visible freshness/consistency metadata.
- `Incremental Projection Update Strategy` — conditional strategy; incremental updates are normal path, full rebuild recovery path.
- `Authority Context Bounded Reference Candidate` — superseded Phase 2 envelope candidate; course correction rejects assignment refs in the ADR-003 envelope path.
- `Authority Context Assertion Semantics Candidate` — conditional audit invariant; authority context records device belief, not server-verified fact.
- `Platform Actor For System Events Candidate` — superseded Phase 2 candidate tied to mandatory authority context; course correction says no platform actor is needed for ADR-003 authority context if authority is projected.
- `Shared Device Per-Actor Session Candidate` — conditional invariant; shared devices need actor partitioning and per-actor sync knowledge or explicit limitation.
- `Auditor Access Structural Additions Open` — open auditor gap; capability, query scope, and data expiry may be structural.
- `Missing Operational Path Set` — open Phase 2 gap set; case reassignment, referral, coordinator transactionality, expired assignments, deactivated actor assessments.
- `ADR-003 Phase 2 Candidate Classification Set` — open handoff register of constraint and strategy candidates awaiting course correction/ADR-003.
- `ADR-003 Course-Correction Boundary` — settled extraction rule from `docs/exploration/archive/12-adr3-course-correction.md`; reconciliation source, not final ADR.
- `Authority-As-Projection Candidate` — conditional course-correction decision; no authority-context envelope field, authority derived from assignment/process timelines.
- `Assignment Sync Ordering Requirement Candidate` — conditional requirement implied by authority-as-projection; assignment events must be available before/with authorized work events.
- `ADR-003 Course-Correction Constraint Promotion Set` — conditional promotion set; scope containment, alias-respects-original-scope, online-only conflict resolution, authorization detect-before-act.
- `ADR-003 Strategy Reclassification Set` — conditional reclassification; priority sync, purge policy, freshness metadata, auto-resolution, shared-device sessions are strategies.
- `ADR-003 Stress-Test Overcall Correction Set` — conditional correction; sensitive subjects, actor-partitioned storage, actor-as-subject visibility, and auditor access are not treated as ADR-003 envelope constraints by this source.
- `ADR-003 Course-Correction Residual Risk` — open pre-ADR checklist; verify sync ordering, strategy carry-forward, and deferred/owned risks in ADR-003.
- `ADR-003 Decision Boundary` — settled ADR extraction rule from `docs/adrs/adr-003-authorization-sync.md`.
- `ADR-003 Assignment Access Contract` — ADR-settled assignment-based authorization contract.
- `ADR-003 Sync Scope Access Invariant` — ADR-settled sync-scope-equals-access-scope invariant.
- `ADR-003 Authority-As-Projection Contract` — ADR-settled no-new-envelope-fields authority reconstruction contract.
- `ADR-003 Alias Original Scope Invariant` — ADR-settled original-subject authorization invariant after merges.
- `ADR-003 Assignment Creation Scope-Containment Contract` — ADR-settled server-side assignment creation validation.
- `ADR-003 Conflict Resolution Online-Only Invariant` — ADR-settled server-validated conflict-resolution invariant.
- `ADR-003 Authorization Detect-Before-Act Contract` — ADR-settled extension of detect-before-act to authorization flags.
- `ADR-003 Tiered Projection Strategy` — ADR-settled initial evolvable projection/sync topology.
- `ADR-003 Authorization Staleness Strategy` — ADR-settled initial stale-authorization flag strategy.
- `ADR-003 Scope Change Data Handling Strategy` — ADR-settled initial selective-retain/local-data policy.
- `ADR-003 Explicit Deferral Contract` — ADR-settled handoff to ADR-004, ADR-005, implementation, and strategy.
- `ADR-003 Accepted Risk Contract` — ADR-settled accepted risks and revisit triggers.
- `ADR-003 Reconciliation Result` — settled result confirming course-correction path and closing ADR-003 lineage.
- `ADR-004 Session 1 Scoping Boundary` — settled extraction rule from `docs/exploration/archive/13-adr4-session1-scoping.md`; Session 1 scopes but does not decide ADR-004.
- `ADR-004 Decision Surface` — open twelve-question ADR-004 boundary surface.
- `ADR-004 Irreversibility Focus Set` — candidate stress-focus map; Q1, Q3, and Q11 potentially touch stored events.
- `Configuration Boundary Anti-Pattern Set` — candidate ADR-004 guardrail set from prior art.
- `Expression And Trigger Ceiling Candidate` — candidate configuration/code boundary for pure expressions and bounded triggers.
- `Schema Evolution First-Class Requirement` — candidate schema versioning/coexistence requirement.
- `Unified Configuration Artifact Pipeline Candidate` — candidate invariant against overlapping configuration authorities.
- `Configuration Gradient Hypothesis` — candidate four-layer configuration model plus parameterize/extend dimension.
- `Configuration Complexity Budget Candidate` — candidate governor-limit approach for configuration complexity.
- `Domain-Agnostic Configuration Vocabulary Guard` — candidate guardrail against domain-specific internal vocabulary.
- `ADR-004 Session 2 Charter` — open handoff to scenario walkthroughs.
- `ADR-004 Session 2 Scenario Walkthrough Boundary` — settled extraction rule from `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md`; scenario evidence, not final ADR.
- `Platform Structural Type Vocabulary Candidate` — conditional Q1 position; platform-fixed structural event types plus deployment-defined shapes.
- `Shape Reference Envelope Candidate` — conditional Q3 position; mandatory `shape_ref` in `{shape_name}/v{version}` format.
- `Optional Activity Reference Envelope Candidate` — conditional Q11 position; optional `activity_ref` when shape alone does not disambiguate.
- `Shape Version Coexistence Rule Candidate` — conditional Q4 position for in-progress work and offline shape-version coexistence.
- `ADR-004 Gradient Validation Result` — conditional validation and revision of four-layer gradient.
- `Bounded Trigger Engine Candidate` — conditional primitive for event-reaction and deadline-check policies.
- `Projection Rule Artifact Candidate` — candidate Layer 1 artifact for cross-shape projection composition.
- `Campaign Progress Platform Capability Candidate` — candidate platform capability, deployer-parameterized but not deployer-built.
- `Role Action Permission Activity Parameter Candidate` — conditional Q8 position; role-action permissions as activity configuration.
- `ADR-004 Session 2 Unstressed Question Set` — open gap set for Q7, Q9, Q10, and Q12.
- `ADR-004 Session 3 Charter` — open handoff to stress testing.
- `ADR-004 Session 3 Part 1 Structural Coherence Boundary` — settled extraction rule from `docs/exploration/archive/15-adr4-session3-part1-structural-coherence.md`; coherence evidence, not final ADR.
- `Configuration Artifact Lifecycle Model Candidate` — conditional two-lifecycle model: event-coupled shapes and config-package artifacts.
- `Configuration Dependency Validation Candidate` — conditional deploy-time validation and acyclic dependency graph.
- `Device Server Evaluation Contract Candidate` — conditional split; L3 triggers revised to server-only, L2 handles device feedback.
- `Single Expression Language Candidate` — conditional one expression language with form/trigger contexts and zero functions.
- `Pattern Framework Structure Candidate` — conditional platform-fixed pattern framework with ADR-005 inventory dependency.
- `ADR-004 Envelope Composition Candidate` — conditional complete/non-redundant envelope with `shape_ref` and optional `activity_ref`.
- `System Actor Reference Candidate` — conditional `system:trigger/{trigger_id}` actor reference for trigger-generated events.
- `Layer Boundary Formalization Candidate` — conditional formal layer-boundary and side-effect definitions.
- `Anti-Pattern Guard Formalization Candidate` — conditional AP-1/AP-2/AP-5 guard formalization.
- `Platform Capability Boundary Candidate` — conditional cross-subject aggregation belongs to platform capabilities.
- `ADR-004 Part 2 Reframe Result` — open handoff to irreversibility filtering with added system actor format question.
- `ADR-004 Session 3 Part 2 Irreversibility Filter Boundary` — settled extraction rule from `docs/exploration/archive/16-adr4-session3-part2-irreversibility-filter.md`; permanence classification, not final ADR closure.
- `ADR-004 Irreversibility Tier Classification Candidate` — conditional tier map separating Tier 1 structural envelope constraints, Tier 2 strategy-protecting constraints, and Tier 3 initial strategies.
- `Shape Reference Structural Constraint Candidate` — conditional Tier 1 candidate; mandatory `shape_ref`, `{shape_name}/v{version}`, constrained shape names, integer versions.
- `Activity Reference Structural Constraint Candidate` — conditional Tier 1 candidate; optional `activity_ref` as deployer-chosen activity instance identifier.
- `Structural Event Type Vocabulary Constraint Candidate` — conditional Tier 1 candidate; platform-fixed append-only structural type vocabulary with six initial types.
- `System Actor Format Strategy-Protecting Constraint Candidate` — conditional Tier 2 candidate; non-null system actor references using `system:{source_type}/{source_id}` convention.
- `Configuration Architecture Strategy Classification Candidate` — conditional classification that most non-envelope configuration architecture choices are initial strategies, while server-only triggers and atomic packages are strategy-protecting constraints.
- `Platform Capability Strategy Classification Candidate` — conditional classification that platform capability inventory and limits are strategy, not stored-event locks.
- `ADR-004 Part 3 Stress Scope Candidate` — open handoff narrowing Part 3 to Tier 1 envelope stress tests with lighter validation for Tier 2/3 items.
- `ADR-004 Session 3 Part 3 Adversarial Stress Boundary` — settled extraction rule from `docs/exploration/archive/17-adr4-session3-part3-adversarial-stress-tests.md`; stress evidence, not final ADR closure.
- `Activity Reference Optionality Stress-Test Survivor` — conditional survivor; optional schema field with normal activity-context auto-population and null for imports/unknown context.
- `Shape Reference Versioning Stress-Test Survivor` — conditional survivor; envelope format survives breaking-schema attack while migration/projection policy remains separate.
- `Structural Event Type Vocabulary Stress-Test Survivor` — conditional survivor; six initial structural types sufficient for ADR-004 scenarios, with new types justified only by different platform processing.
- `Configuration Limit Light-Validation Result` — conditional light validation for 60-field shape budget and trigger DAG max path length two as evolvable validation strategies.
- `Pre-Resolved Context Scope Candidate` — candidate expression-scope refinement; bounded `context.*` scope may avoid arbitrary cross-entity queries but needs later validation.
- `ADR-004 Part 4 Handoff From Stress Tests` — open handoff for Q7 breaking-change policy, `context.*`, possible ADR-005 `status_changed`, and same-shape activity validation warnings.
- `ADR-004 Session 3 Part 4 Remaining Question Boundary` — settled extraction rule from `docs/exploration/archive/18-adr4-session3-part4-remaining-q-resolution.md`; remaining-question synthesis, not final ADR closure.
- `Domain Uniqueness Constraint Candidate` — conditional Q7a position; shape-level declarative uniqueness rules checked optimistically on device and authoritatively on server, producing ADR-002 conflict flags.
- `Domain Conflict Resolution Deferral` — open Q7b deferral to ADR-005 for automated resolution strategies.
- `Flag Severity Configuration Candidate` — conditional Q9 position; per-deployment severity overrides over platform-defined flag types and defaults.
- `Platform-Fixed Composable Scope Types Candidate` — conditional Q10 position; geographic, subject_list, and activity scope dimensions with AND composition and no deployer-defined containment logic.
- `Sensitivity Classification Candidate` — conditional Q12 position; shape/activity-level sensitivity levels affect sync, retention, audit, and display without envelope fields.
- `Schema Evolution Default Strategy Refinement` — conditional B10 refinement; additive/deprecation by default, breaking changes exceptional with server-side validation and deployer confirmation.
- `Deploy-Time Configuration Validation Capability Candidate` — conditional synthesis finding; validates artifacts, budgets, references, trigger DAGs, uniqueness rules, and same-shape activity warnings before deployment.
- `ADR-004 Session 3 Synthesis Result` — conditional pre-ADR synthesis; all twelve ADR-004 questions resolved or explicitly deferred, no envelope additions from Q7/Q9/Q10/Q12.

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

### Iteration 14

Processed `docs/exploration/archive/02-adr1-offline-data-model.md`, the first ADR-specific exploration source. Extracted ADR-001 lineage around forced append-only storage, client-generated identity, immutable-record sync shape, conflict surfacing, materialized-read pressure, snapshot workflow weakness, and downstream constraints. Kept the central storage-unit choice open because the source is superseded and explicitly says ADR-001 finalizes the decision.

### Iteration 15

Processed `docs/exploration/archive/03-adr1-forward-projection.md` as ADR-001 forward-projection lineage. Extracted downstream consequence pressure without letting it decide ADR-002 through ADR-005. Captured the strong Events selection pressure, snapshot structural ceiling, action-log convergence/dual-write risk, and projection-infrastructure risk. Final ADR-001 closure remains for `docs/exploration/archive/04-decision-audit.md` and ADR-001.

### Iteration 16

Housekeeping-only correction to ADR-001 forward-projection handling. Added a deferred closure register so claims from `03-adr1-forward-projection.md` that may later become real ADR-002 through ADR-005 decisions are preserved until their owning exploration and ADR sources are processed. No source was processed, no cursor advanced, and no kernel status changed.

### Iteration 17

Processed `docs/exploration/archive/04-decision-audit.md` as the ADR-001 audit and normalization source. Extracted conditional ADR-001 event-storage closure, required write-path discipline, projection rebuild-scope deferral, downstream overreach correction, ADR-002 through ADR-005 owning-scope maps, and the audit safety check. Promoted the `03` ADR-001 selection pressure toward typed immutable events at audit level, but kept final platform closure pending ADR-001 extraction.

### Iteration 18

Processed `docs/exploration/archive/05-adr2-event-storm-identity.md` as ADR-002 Phase 1 event discovery. Created `06-adr2-identity-conflict-kernels.md` for ADR-002 staging. Extracted candidate identity taxonomy, subject lifecycle, merge/split lineage, conflict taxonomy, stale-state accept-and-flag pressure, causal-ordering open question, assignment identity, and process/pending-match identity. Did not promote discovered events, commands, or proposed aggregates as final platform primitives.

### Iteration 19

Processed `docs/exploration/archive/07-adr2-phase2-stress-test-results.md` as ADR-002 Phase 2 stress-test evidence. Extracted survivor mechanisms and required modifications: accept-and-flag with single-writer resolution, root-cause metadata, batch resolution, detect-before-act sync processing, alias table with eager closure/acyclicity/corrective split, device sequence plus sync watermark, advisory device time, and pending-match constraints. Kept all as conditional stress-test findings pending Phase 3 classification and ADR-002.

### Iteration 20

Processed `docs/exploration/archive/09-adr2-phase3-classification-results.md` as ADR-002 Phase 3 classification. Extracted Bucket 1 irreversible constraints, Bucket 2 evolvable strategies, Bucket 3 cross-ADR deferrals, Bucket 4 accepted risks, and simplicity validation. Updated the deferred closure register so ADR-002 forward-projection claims are classified but still pending ADR-002 verification. Paused source scanning for assessment before choosing ADR-002 reconciliation or ADR-002 extraction.

### Iteration 21

Housekeeping-only ADR-002 reconciliation pass. Added a pre-ADR checklist to `06-adr2-identity-conflict-kernels.md` covering Phase 3 Bucket 1 verification, Bucket 2 strategy boundaries, Bucket 3 cross-ADR deferrals, accepted risks, and user risk-note handling. No source was processed and no kernel status was promoted. The recommended next action is ADR-002 extraction before continuing archive source scan.

### Iteration 22

Processed `docs/adrs/adr-002-identity-conflict.md` against the ADR-002 reconciliation checkpoint. Confirmed ADR-002 carries the Phase 3 Bucket 1 constraints, keeps Bucket 2 items as strategies, explicitly defers Bucket 3 items to ADR-003/004/005, and preserves accepted risks with revisit triggers. Added ADR-settled kernels for envelope contract, device-time advisory semantics, identity evolution, lineage validation, conflict contract, stale-event acceptance, explicit deferrals, accepted risks, and reconciliation result. Did not use `docs/adrs/adr-002-addendum-type-vocabulary.md` as authority in this pass.

### Iteration 23

Processed `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md` as ADR-003 Phase 1 scenario-driven policy analysis. Created `07-adr3-authorization-sync-kernels.md` for ADR-003 staging. Extracted assignment-based authorization, authority context, sync scope as offline authorization, subject-scoped sync, temporal authority, tiered projection/sync topology, resolver designation, and the Phase 1 open hot spots. Kept all architecture claims candidate/open except the source boundary and upstream-assumption compatibility context.

### Iteration 24

Processed `docs/exploration/archive/11-adr3-phase2-stress-test.md` as ADR-003 Phase 2 adversarial evidence. Extracted stress-tested constraint candidates, strategy candidates, unsafe/rejected policy options, and open operational gaps. Preserved Phase 2 `RESOLVED` claims as conditional stress-test closure rather than ADR-settled closure. Updated the ADR-003 deferred register so final closure remains with `12-adr3-course-correction.md` and ADR-003.

### Iteration 25

Processed `docs/exploration/archive/12-adr3-course-correction.md` as ADR-003 irreversibility filtering and stress-test reconciliation. Superseded Phase 1/2 authority-context envelope candidates in favor of authority-as-projection, promoted four Phase 2 findings as constraint candidates, reclassified several Phase 2 findings as strategies or non-ADR-003 structural concerns, and recorded residual risks for final ADR-003 verification. Set next source to `docs/adrs/adr-003-authorization-sync.md` rather than ADR-004 archive scanning so ADR-003 can close or correct the reconciliation.

### Iteration 26

Processed `docs/adrs/adr-003-authorization-sync.md`. Confirmed ADR-003 commits the course-correction path: no new envelope fields, authority-as-projection, assignment-based access, sync scope as access scope, original-subject authorization after merge, assignment scope containment, online-only conflict resolution, and detect-before-act for authorization flags. Captured tiered projection, authorization staleness, and scope-change data handling as ADR-settled initial strategies. Recorded ADR-004/ADR-005 deferrals and accepted risks with revisit triggers. Next source returns to archive scan at `docs/exploration/archive/13-adr4-session1-scoping.md`.

### Iteration 27

Processed `docs/exploration/archive/13-adr4-session1-scoping.md` as ADR-004 Session 1 scoping and prior-art analysis. Created `08-adr4-configuration-boundary-kernels.md`. Extracted the twelve-question ADR-004 decision surface, the Q1/Q3/Q11 irreversibility focus set, six configuration anti-patterns, inherited constraint stack, candidate four-layer configuration gradient, complexity-budget hypothesis, unified artifact pipeline guard, domain-agnostic vocabulary guard, and Session 2 charter. Kept all design claims candidate/open except the source boundary.

### Iteration 28

Processed `docs/exploration/archive/14-adr4-session2-scenario-walkthrough.md` as ADR-004 Session 2 scenario evidence. Extracted conditional positions for Q1 platform-fixed structural event types plus deployment-defined shapes, Q3 mandatory `shape_ref`, Q11 optional `activity_ref`, Q4 shape-version coexistence, Q8 role-action permissions as activity parameters, revised gradient validation, bounded Trigger Engine, Projection Rule artifact, campaign progress as platform capability, unstressed residual questions, and Session 3 stress-test charter. Kept Session 2 `Decided` claims conditional because the source says they are pending Session 3 stress test.

### Iteration 29

Processed `docs/exploration/archive/15-adr4-session3-part1-structural-coherence.md` as ADR-004 Session 3 Part 1 structural-coherence audit. Extracted conditional findings for artifact lifecycle, dependency validation, device/server evaluation split, expression language, pattern framework, envelope composition, system actor references, layer boundaries, anti-pattern guards, and platform capability boundary. Revised the Trigger Engine candidate so both 3a and 3b triggers are server-only, with L2 handling immediate device feedback. Updated campaign progress from one capability into aggregate projection, target comparison, and time windowing. Next source is Part 2 irreversibility filtering.

### Iteration 30

Processed `docs/exploration/archive/16-adr4-session3-part2-irreversibility-filter.md` as ADR-004 Session 3 Part 2 irreversibility classification. Extracted the Tier 1/Tier 2/Tier 3 permanence map, with `shape_ref`, optional `activity_ref`, and structural `type` vocabulary as conditional Tier 1 envelope constraints. Reclassified system actor format, server-only triggers, atomic config packages, and deployer identifier naming rules as strategy-protecting constraints, while most configuration architecture and platform capability positions remain initial strategies. Narrowed the next-source stress scope to Tier 1 envelope attacks plus lighter validation for Tier 2/3 guardrails.

### Iteration 31

Processed `docs/exploration/archive/17-adr4-session3-part3-adversarial-stress-tests.md` as ADR-004 Session 3 Part 3 adversarial evidence. Confirmed the Tier 1 envelope candidates survive without field changes: optional `activity_ref` remains correct with device auto-population when activity context exists, mandatory `shape_ref` preserves event interpretability while projection/migration policy remains separate, and the six-type structural vocabulary covers ADR-004 scenarios with future append-only growth reserved for distinct platform processing. Captured light validation for 60-field shape budgets and trigger DAG depth two, plus the candidate `context.*` expression scope and Part 4 handoff items.

### Iteration 32

Processed `docs/exploration/archive/18-adr4-session3-part4-remaining-q-resolution.md` as ADR-004 Session 3 Part 4 remaining-question resolution and synthesis. Extracted conditional Q7a domain uniqueness constraints, Q9 flag severity configuration, Q10 platform-fixed composable scope types, Q12 shape/activity-level sensitivity classification, and the refined deprecation-first schema-evolution strategy. Preserved Q7b conflict-resolution automation, `context.*`, and possible `status_changed` as ADR-005 deferrals or candidates. Recorded the Session 3 synthesis that all twelve ADR-004 questions are resolved or explicitly deferred and that Q7/Q9/Q10/Q12 add no envelope fields. Next source is ADR-004 itself for closure verification before ADR-005 archive processing.
