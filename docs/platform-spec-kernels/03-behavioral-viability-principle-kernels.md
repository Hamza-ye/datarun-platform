# Behavioral Viability And Principle Kernel Staging

Status: Iteration 13 staging split

This temporary staging file holds behavioral-pattern, viability-assessment, and principle kernels. It is not a final atomic document.

## Staged Kernels

## Kernel: Behavioral Pattern Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

The behavioral patterns extracted from scenarios name recurring behaviors the platform must support. They are not platform constructs, database tables, technology choices, implementation mechanisms, or architecture primitives.

Source basis:

- `docs/behavioral_patterns.md` / `## 1. Purpose`
- `docs/scenarios/README.md` / `## What These Scenarios Are Not`

Closure basis:

Settled as an interpretation rule for scenario and behavioral-pattern extraction.

Scope:

Applies to all scenario-derived and behavioral-pattern-derived kernels.

Non-goals:

This kernel does not decide the platform primitive vocabulary or architecture decomposition.

Forbidden interpretations:

- Do not convert a behavioral pattern name directly into a platform primitive.
- Do not treat the behavioral pattern catalog as a database or interface design.
- Do not treat the pattern decomposition as implementation scope.

Open edges:

Technical closure remains to be extracted from viability, exploration, and ADR sources.

Platform specification note:

The platform specification may use behavioral pattern names as requirement categories only where later sources do not require a stricter technical term.

## Kernel: Structured Recording Behavior

Status: Settled
Kind: invariant

Specification statement:

Someone captures a predefined set of details about something observed, done, or received. The structure exists before capture, and the resulting record remains available for later reference.

Source basis:

- `docs/behavioral_patterns.md` / `P01 — Structured Recording`
- Scenario evidence: S00, S01, S05, S20, S21

Closure basis:

Settled as a behavioral requirement. Not closed as a technical recording primitive.

Scope:

Applies to structured capture across standalone records, subject-linked records, visits, field work, and assessments.

Non-goals:

Does not decide form, event, document, schema, or storage representation.

Forbidden interpretations:

- Do not require unrelated workflow machinery for the basic recording behavior.
- Do not treat "record" as a technical storage decision at this stage.

Open edges:

Recording contract and storage model remain open until exploration/ADR closure.

Platform specification note:

Use as the behavioral baseline for any technical recording contract.

## Kernel: Subject Linkage Behavior

Status: Settled
Kind: invariant

Specification statement:

A record may be tied to a recognizable, persistent real-world subject whose identity survives across multiple interactions over time, even when that identity is stable, changing, ambiguous, or resolved later.

Source basis:

- `docs/behavioral_patterns.md` / `P02 — Subject Linkage`
- Scenario evidence: S01, S06, S08, S13, S20

Closure basis:

Settled as a behavioral requirement. Not closed as a subject primitive, reference contract, or identity lifecycle model.

Scope:

Applies to people, places, households, equipment, organizational units, cases, and comparable operational subjects.

Non-goals:

Does not decide identifiers, aliasing, merge/split, registry structure, or reference fields.

Forbidden interpretations:

- Do not assume subject identity is always stable.
- Do not assume subject linkage requires one specific technical identity model.

Open edges:

Identity and reference mechanics remain open until exploration/ADR closure.

Platform specification note:

Use as the behavioral basis for later subject, reference, and identity semantics.

## Kernel: Temporal Rhythm Behavior

Status: Settled
Kind: invariant

Specification statement:

Some work is expected on a predictable rhythm. A missing expected occurrence is meaningful, not merely absence of data.

Source basis:

- `docs/behavioral_patterns.md` / `P03 — Temporal Rhythm`
- Scenario evidence: S02, S05, S06, S09

Closure basis:

Settled as a behavioral requirement. Not closed as a scheduling, deadline, task, or projection mechanism.

Scope:

Applies to recurring reports, planned visits, registry reconfirmation, campaign windows, and follow-up expectations.

Non-goals:

Does not decide calendars, task generation, reminders, timers, or rule evaluation.

Forbidden interpretations:

- Do not treat missing work as indistinguishable from no expected work.
- Do not assume all rhythms are fixed calendar schedules.

Open edges:

Temporal representation and enforcement remain open until later sources close them.

Platform specification note:

Use to justify explicit representation of expected work and gaps once the technical model is closed.

## Kernel: Responsibility Binding Behavior

Status: Settled
Kind: invariant

Specification statement:

A specific person may be accountable for a specific scope of work, making it clear who should have done something and whether they did.

Source basis:

- `docs/behavioral_patterns.md` / `P04 — Responsibility Binding`
- Scenario evidence: S03, S09, S14, S20, S21

Closure basis:

Settled as a behavioral requirement. Not closed as an assignment model, access model, or actor-scope contract.

Scope:

Applies to explicit assignments, role-based responsibility, geographic responsibility, subject-set responsibility, work-type responsibility, and time-windowed responsibility.

Non-goals:

Does not decide assignment events, role vocabulary, scope fields, or authorization mechanics.

Forbidden interpretations:

- Do not model accountability without named responsible actors or actor groups where the scenario requires specificity.
- Do not equate responsibility with visibility or authority in every context.

Open edges:

Responsibility, access, and sync mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for later assignment and authority contracts.

## Kernel: Hierarchical Visibility Behavior

Status: Settled
Kind: invariant

Specification statement:

Different people see different scopes of information and work based on organizational, geographic, programmatic, or exceptional visibility relationships. Seeing more is not the same as being authorized to do more.

Source basis:

- `docs/behavioral_patterns.md` / `P05 — Hierarchical Visibility`
- Scenario evidence: S03, S04, S05, S09, S11, S14, S15, S21

Closure basis:

Settled as a behavioral requirement. Not closed as a hierarchy model, scope model, or access-control implementation.

Scope:

Applies to supervision, regional oversight, campaign monitoring, multi-level distribution, approvals, cross-program overlays, and auditor-like exceptions.

Non-goals:

Does not decide containment logic, sync scope, read permissions, write permissions, or exception encoding.

Forbidden interpretations:

- Do not collapse visibility and action authority.
- Do not assume hierarchy has no exceptions.

Open edges:

Visibility and authority mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for separating visibility from authority in later technical contracts.

## Kernel: Review And Judgment Behavior

Status: Settled
Kind: invariant

Specification statement:

Work completed by one person may pass through another person's assessment before it is final. The assessment may approve, reject, return, question, or escalate the work, and the outcome is part of the record.

Source basis:

- `docs/behavioral_patterns.md` / `P06 — Review and Judgment`
- Scenario evidence: S04, S05, S11, S21

Closure basis:

Settled as a behavioral requirement. Not closed as a workflow, state machine, review event, or approval protocol.

Scope:

Applies to supervisor review, audit visits, multi-step approval, and supervisor assessment.

Non-goals:

Does not decide review states, transition rules, reviewer assignment, or finalization mechanics.

Forbidden interpretations:

- Do not lose who reviewed what, when, and with what decision.
- Do not assume every review is single-level or same-role.

Open edges:

Review and state progression mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for auditable review semantics.

## Kernel: Transfer With Acknowledgment Behavior

Status: Settled
Kind: invariant

Specification statement:

Something may move from one party to another, and the receiver confirms receipt, disputes quantity or condition, or notes a discrepancy. The transfer is not behaviorally complete until acknowledgment or discrepancy is recorded.

Source basis:

- `docs/behavioral_patterns.md` / `P07 — Transfer with Acknowledgment`
- Scenario evidence: S07, S14, S20, S22

Closure basis:

Settled as a behavioral requirement. Not closed as inventory, logistics, transaction, or workflow mechanism.

Scope:

Applies to goods, supplies, resources, authority, responsibility, and related handoff behaviors.

Non-goals:

Does not decide inventory accounting, transfer event shape, chain-of-custody model, or discrepancy resolution.

Forbidden interpretations:

- Do not treat send as equivalent to received.
- Do not erase discrepancies from the transfer history.

Open edges:

Transfer representation and reconciliation remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for handoff and acknowledgment contracts.

## Kernel: State Progression Behavior

Status: Settled
Kind: invariant

Specification statement:

Work may move through meaningful stages. Earlier stages constrain later stages, progress is recorded, and current status matters for what can happen next.

Source basis:

- `docs/behavioral_patterns.md` / `P08 — State Progression`
- Scenario evidence: S04, S07, S08, S09, S11, S12, S14, S20, S22

Closure basis:

Settled as a behavioral requirement. Not closed as stored status, state machine, projection, or workflow engine.

Scope:

Applies to review, transfer, case management, campaigns, approvals, event-triggered responses, and multi-level distribution.

Non-goals:

Does not decide whether state is stored, derived, configured, or enforced by a platform mechanism.

Forbidden interpretations:

- Do not assume all progression is linear.
- Do not assume current state can be understood without history where the behavior requires progression.

Open edges:

State representation and transition semantics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for later state and workflow decisions without naming their implementation.

## Kernel: Condition-Triggered Action Behavior

Status: Settled
Kind: conditional-validity

Specification statement:

Observed conditions, thresholds, patterns, gaps, or elapsed expectations may determine that something needs attention. The resulting action may be notification, assignment, escalation, or new work, and previous actions may influence future needs.

Source basis:

- `docs/behavioral_patterns.md` / `P09 — Condition-Triggered Action`
- Scenario evidence: S10, S12, S18

Closure basis:

Settled as a behavioral requirement, with S18 representing deferred extension pressure. Not closed as a trigger engine or rules language.

Scope:

Applies to dynamic targeting, event-triggered responses, escalation, analytics-derived flows, and feedback loops.

Non-goals:

Does not decide trigger timing, real-time semantics, rule expressiveness, notification channels, or automated work creation.

Forbidden interpretations:

- Do not assume all triggered action is real-time.
- Do not infer an unbounded rules engine from the behavior.

Open edges:

Trigger boundaries and mechanism remain open until viability, exploration, and ADR sources close them.

Platform specification note:

Use as behavioral evidence for reactive work while preserving configuration-boundary caution.

## Kernel: Cross-Reference Behavior

Status: Settled
Kind: invariant

Specification statement:

Separate activities may be related because understanding one requires context from another. The connection should be visible without forcing independent activities into one rigid process.

Source basis:

- `docs/behavioral_patterns.md` / `P10 — Cross-Reference`
- Scenario evidence: S08, S13, S18, S22

Closure basis:

Settled as a behavioral requirement. Not closed as reference fields, links, joins, projections, or relationship contracts.

Scope:

Applies to case context, supply-to-campaign context, prior audit findings, analytics-derived relationships, and related but independent flows.

Non-goals:

Does not decide link representation, referential integrity, activity identity, or coupling rules.

Forbidden interpretations:

- Do not force related activities into a single workflow solely because they are connected.
- Do not hide meaningful operational relationships between otherwise independent activities.

Open edges:

Cross-reference representation and interaction rules remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for explicit but non-rigid linking semantics.

## Kernel: Shape Evolution Behavior

Status: Settled
Kind: conditional-validity

Specification statement:

The expected structure of information may change over time. Old records remain valid under the shape that was active when captured, while new work may follow the updated shape.

Source basis:

- `docs/behavioral_patterns.md` / `P11 — Shape Definition and Evolution`
- Scenario evidence: S00, S06

Closure basis:

Settled as a behavioral requirement. Not closed as schema versioning, shape registry, migration, or validation mechanism.

Scope:

Applies to fixed and evolving expected information structures, old/new record coexistence, and records from different periods.

Non-goals:

Does not decide whether shape changes are migrated, versioned, projected, or read through compatibility layers.

Forbidden interpretations:

- Do not invalidate older records because the expected structure changed.
- Do not require all old and new records to share the same current shape.

Open edges:

Shape/version mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for later configuration and record-shape contracts.

## Kernel: Offline-First Work Behavior

Status: Settled
Kind: invariant

Specification statement:

Meaningful work happens without connectivity. Records, decisions, and state progression may be created locally and later reconciled with shared state when connectivity returns.

Source basis:

- `docs/behavioral_patterns.md` / `P12 — Offline-First Work`
- Scenario evidence: S19 and cross-cutting pressure across field scenarios, including S22

Closure basis:

Settled as a behavioral requirement. Not closed as sync protocol, local storage, conflict detection, or reconciliation model.

Scope:

Applies to field capture, decisions, stale local state, conflicting offline work, sync arrival, and cross-user reconciliation.

Non-goals:

Does not decide offline feature subset, conflict policy, data replication strategy, or user-visible sync behavior.

Forbidden interpretations:

- Do not require connectivity for field work.
- Do not assume local and central state are always current with each other.
- Do not equate sync arrival time with work time.

Open edges:

Offline storage, sync, ordering, and reconciliation mechanics remain open until later sources close them.

Platform specification note:

Use as behavioral evidence for offline-first architecture decisions without preselecting those decisions.

## Kernel: Behavioral Composition Without New Patterns

Status: Settled
Kind: conditional-validity

Specification statement:

The Phase 1 scenario set decomposes into the behavioral pattern catalog without requiring new patterns, and composite scenarios decompose into the same atomic behavioral patterns as their constituent scenarios.

Source basis:

- `docs/behavioral_patterns.md` / `## 4. Scenario Decomposition Table`
- `docs/behavioral_patterns.md` / `## 6. Validation`
- Scenario evidence: S00-S14, S19, S20, S21, with S22 providing an additional composite core scenario in the scenario index.

Closure basis:

Settled as behavioral validation. Not closed as architectural primitive sufficiency.

Scope:

Applies to behavioral decomposition and cross-scenario reuse.

Non-goals:

Does not prove any particular architecture, primitive set, implementation phase, or platform module boundary.

Forbidden interpretations:

- Do not treat "no new behavioral pattern" as "no new technical mechanism."
- Do not treat composite scenario decomposition as evidence that every implementation detail is already closed.

Open edges:

Technical sufficiency and architecture closure remain to be established by viability, exploration, and ADR sources.

Platform specification note:

Use as a domain-level sufficiency claim for behavioral coverage, not as a final technical specification.

## Kernel: Conditional Platform Viability

Status: Settled
Kind: conditional-validity

Specification statement:

The platform approach is viable as a conditional go: the Phase 1 scenario set, offline cross-cut, access-control concern, constraints, and vision form a coherent enough problem space to proceed into architecture exploration, provided specific scope and boundary conditions are respected.

Source basis:

- `docs/viability-assessment.md` / `## Executive Summary`
- `docs/viability-assessment.md` / `### Recommendation: CONDITIONAL GO`

Closure basis:

Settled as a pre-architecture viability verdict. Not settled as final architecture.

Scope:

Applies to the decision to proceed from domain/scenario analysis into architecture exploration.

Non-goals:

Does not decide technical architecture, primitives, implementation sequence, or platform specification content.

Forbidden interpretations:

- Do not treat conditional go as unconditional architecture approval.
- Do not treat the viability assessment as final ADR closure.

Open edges:

The named conditions and risks must be closed or bounded by later exploration and ADRs.

Platform specification note:

The platform specification should inherit the viability conditions as historical constraints only where later sources confirm or refine them.

## Kernel: No Hard Scenario Conflicts

Status: Settled
Kind: conditional-validity

Specification statement:

The approved scenario set contains no hard conflicts that make a unified platform impossible. The observed conflicts are design trade-offs and tensions, not contradictions that invalidate the platform concept.

Source basis:

- `docs/viability-assessment.md` / `## 3. Conflict Check`

Closure basis:

Settled as a viability finding over the scenario and domain-ground-truth set.

Scope:

Applies to scenario coherence and the decision to continue architecture exploration.

Non-goals:

Does not prove any particular technical solution or eliminate implementation risk.

Forbidden interpretations:

- Do not treat absence of hard conflicts as proof that every scenario should drive initial architecture equally.
- Do not ignore documented tensions because no hard conflict was found.

Open edges:

Each tension still requires technical closure or explicit deferral.

Platform specification note:

Use as context for why the platform can be specified as one coherent system, while preserving documented tensions.

## Kernel: Configuration Boundary Collapse Risk

Status: Settled
Kind: configuration-boundary

Specification statement:

The line between "set up" and "built" is the most architecturally significant viability risk. If configuration becomes too expressive, it collapses into a programming language and undermines the platform promise.

Source basis:

- `docs/viability-assessment.md` / `T2: Configuration simplicity vs. expressive power`
- `docs/viability-assessment.md` / `Risk Flags` / `R1`
- `docs/viability-assessment.md` / `Condition 1 — Configuration boundary first`

Closure basis:

Settled as a high-severity viability risk and architecture-entry condition. Not closed as the final configuration model.

Scope:

Applies to shape evolution, dynamic targeting, event-triggered actions, multi-audience views, analytics-derived flows, and any future deployer-authored logic.

Non-goals:

Does not decide the layers, syntax, rule language, admin UI, extension model, or deployer capabilities.

Forbidden interpretations:

- Do not infer unlimited deployer-authored logic from "set up, not built."
- Do not hide software development inside a configuration language.

Open edges:

The concrete configuration boundary remains to be closed by exploration and ADRs.

Platform specification note:

The platform specification must make the configuration boundary explicit and should preserve this risk as the reason the boundary exists.

## Kernel: Offline Reactivity Eventual-Consistency Tension

Status: Settled
Kind: conditional-validity

Specification statement:

Reactive behavior and timely response must be reconciled with offline-first work. In the viability assessment, triggers and oversight reactions are framed as eventually consistent: they can occur when information becomes centrally visible, not necessarily when the field observation happened.

Source basis:

- `docs/viability-assessment.md` / `T1: Offline-first vs. real-time reactivity`

Closure basis:

Settled as a viability tension and direction. Not closed as a trigger mechanism or timing contract.

Scope:

Applies to dynamic targeting, event-triggered actions, emergency response pressure, oversight delay, and sync-mediated visibility.

Non-goals:

Does not decide trigger timing, notification channels, sync protocol, or whether later ADRs preserve this exact mechanism.

Forbidden interpretations:

- Do not assume field-recorded events can always trigger immediate central action.
- Do not design reactive behavior as inherently real-time unless later authoritative sources close that.

Open edges:

Reactive semantics and sync/freshness contracts remain to be closed by later sources.

Platform specification note:

The platform specification should treat real-time reactivity as constrained by offline-first operation unless later decisions introduce bounded exceptions.

## Kernel: Domain Mechanism Content Separation

Status: Settled
Kind: configuration-boundary

Specification statement:

Domain-agnosticism is viable if the platform provides mechanisms while deploying organizations provide domain content: what conditions mean, which thresholds matter, what "resolved" means, and which validations apply.

Source basis:

- `docs/viability-assessment.md` / `T3: Domain-agnosticism vs. domain-specific validation`

Closure basis:

Settled as a viability direction. Not closed as a specific configuration, validation, or rule model.

Scope:

Applies to case management, dynamic targeting, event-triggered actions, domain-specific validation, and business rules.

Non-goals:

Does not decide rule expressiveness, validation language, domain content schema, or which rules require code.

Forbidden interpretations:

- Do not hard-code domain content into platform mechanisms.
- Do not claim domain-agnosticism requires the platform to know no domain-specific content at deployment time.

Open edges:

The mechanism/content boundary remains to be closed by later sources.

Platform specification note:

The platform specification should separate platform-owned mechanism from deployer-provided domain content.

## Kernel: Trustworthy Records Offline Correction Tension

Status: Settled
Kind: conditional-validity

Specification statement:

Traceable records and offline correction create a real trade-off: preserving full history and surfacing conflicts adds operational complexity, but silent overwrite or erasure would violate the trustworthy-records promise.

Source basis:

- `docs/viability-assessment.md` / `T4: Trustworthy records vs. offline correction`
- `docs/scenarios/00-basic-structured-capture.md` / `## What makes this hard`

Closure basis:

Settled as a viability tension. Not closed as append-only storage, event sourcing, or conflict-resolution policy.

Scope:

Applies to corrections, offline arrival order, conflicting corrections, trustworthy records, and human judgment pressure.

Non-goals:

Does not decide storage mutability, conflict categories, flagging, or resolution workflow.

Forbidden interpretations:

- Do not silently merge or overwrite where traceability is required.
- Do not minimize the operational complexity introduced by full-history preservation.

Open edges:

Write model and conflict behavior remain to be closed by exploration and ADRs.

Platform specification note:

The platform specification should preserve this tension as the motivation for whatever record-history and conflict semantics are later closed.

## Kernel: Phase 2 Deferral Guardrail

Status: Settled
Kind: conditional-validity

Specification statement:

Scenarios 15, 16, and 18 are compatible with the platform ambition but should remain deferred as initial architecture drivers because they introduce view-composition, crisis-authority, and analytics-infrastructure pressures that can distort the Phase 1 core.

Source basis:

- `docs/viability-assessment.md` / `Executive Summary`
- `docs/viability-assessment.md` / `Condition 3 — Keep Phase 2 deferred`
- `docs/scenarios/README.md` / `## Phasing`

Closure basis:

Settled as a viability guardrail. Not settled as permanent exclusion or final platform scope.

Scope:

Applies to multi-audience views, emergency response, analytics-derived flows, authority override, and analytics-driven initiation.

Non-goals:

Does not decide that deferred scenarios are invalid, out of scope forever, or unsupported by future platform evolution.

Forbidden interpretations:

- Do not let deferred scenarios drive initial core architecture.
- Do not design the Phase 1 core around crisis mode or analytics infrastructure.
- Do not actively block future compatibility unless later sources close that trade-off.

Open edges:

Future support and exact compatibility constraints remain to be closed by later sources.

Platform specification note:

The platform specification should distinguish core constraints from deferred extension pressures.

## Kernel: Setup Experience Blind Spot

Status: Open
Kind: open-question

Specification statement:

The setup/configuration experience is the least-tested core promise in the scenario set. Scenarios describe field operations, not the administrator's experience of setting up an operational activity.

Source basis:

- `docs/viability-assessment.md` / `Vision Guarantee Coverage` / `V2`
- `docs/viability-assessment.md` / `Blind spot 1: The setup/configuration experience`

Closure basis:

Open as a viability blind spot.

Scope:

Applies to administrator setup, activity configuration, setup usability, and the credibility of "set up, not built."

Non-goals:

Does not decide the admin UI, configuration layers, or authoring workflow.

Forbidden interpretations:

- Do not treat setup UX as already scenario-proven.
- Do not let field-operation scenarios alone prove the setup promise.

Open edges:

Setup experience evidence and configuration authoring semantics remain open.

Platform specification note:

The platform specification should avoid overstating setup usability until later sources close the boundary and authoring model.

## Kernel: Retention And Archival Blind Spot

Status: Open
Kind: open-question

Specification statement:

The domain ground truth establishes large, accumulating records, but no scenario directly addresses archival, retention policy, or the operational experience of years of accumulated data.

Source basis:

- `docs/viability-assessment.md` / `Blind spot 2: Data archival and retention`
- `docs/constraints.md` / `## Scale`

Closure basis:

Open as a viability blind spot.

Scope:

Applies to old records, retention policy, archival, compliance, performance, local storage, and long-running deployments.

Non-goals:

Does not decide deletion, summarization, local eviction, central retention, or archive access.

Forbidden interpretations:

- Do not assume data lifecycle is solved by the scenarios.
- Do not ignore accumulated-data pressure on low-end devices.

Open edges:

Retention and archival mechanics remain open.

Platform specification note:

The platform specification should mark data lifecycle as unresolved unless later decisions close it.

## Kernel: Onboarding And Role Transition Blind Spot

Status: Open
Kind: open-question

Specification statement:

User onboarding and role-transition operations are not covered by a dedicated scenario, even though access-control requirements mention promotion, transfer, leave, coverage, and responsibility handoff.

Source basis:

- `docs/viability-assessment.md` / `Blind spot 3: User onboarding and role transitions`
- `docs/access-control-scenario.md`

Closure basis:

Open as a viability blind spot.

Scope:

Applies to onboarding, transfer, leave, role changes, responsibility handoff, and lifecycle events for workers.

Non-goals:

Does not decide actor lifecycle, assignment lifecycle, HR integration, or handoff workflow.

Forbidden interpretations:

- Do not assume role lifecycle UX is scenario-proven.
- Do not erase responsibility continuity when roles change.

Open edges:

Actor and responsibility lifecycle mechanics remain open.

Platform specification note:

The platform specification should avoid overclaiming role lifecycle coverage until later closure.

## Kernel: Reporting Aggregation Blind Spot

Status: Open
Kind: open-question

Specification statement:

The scenarios cover capture, oversight, and coordination, but do not directly cover decision-maker aggregation such as cross-district totals or monthly resolved-case counts. Scenario 15 is related but deferred.

Source basis:

- `docs/viability-assessment.md` / `Blind spot 4: Reporting and aggregation for decision-makers`

Closure basis:

Open as a viability blind spot.

Scope:

Applies to aggregate reporting, decision-maker views, summaries, and cross-scope metrics.

Non-goals:

Does not decide analytics, dashboards, materialized views, reporting schema, or Phase 1 inclusion.

Forbidden interpretations:

- Do not assume detailed aggregation requirements are covered by capture scenarios.
- Do not silently pull deferred multi-audience view complexity into the core.

Open edges:

Aggregation scope and reporting semantics remain open.

Platform specification note:

The platform specification should distinguish operational oversight from analytical aggregation unless later sources close them together.

## Kernel: Domain-Agnosticism Proof Gap

Status: Open
Kind: open-question

Specification statement:

Core scenarios are domain-pure, but composite validation examples are health-oriented. Domain-agnosticism is plausible but not fully proven by a second non-health composite scenario in the ground-truth set.

Source basis:

- `docs/viability-assessment.md` / `Vision Guarantee Coverage` / `V6`
- `docs/viability-assessment.md` / `Risk Flags` / `R4`

Closure basis:

Open as a low-medium viability proof gap.

Scope:

Applies to domain-neutral core terminology, cross-domain validation, and risk of implicit health-domain assumptions.

Non-goals:

Does not reject the platform's domain-agnostic ambition.

Forbidden interpretations:

- Do not bake health-domain assumptions into platform core because composite examples are health-oriented.
- Do not treat absence of a second-domain composite as a hard contradiction.

Open edges:

Additional non-health validation remains useful for confidence.

Platform specification note:

The platform specification should keep core terms domain-neutral and cite domain-specific examples only as examples.

## Kernel: Low-End Device Scale Risk

Status: Settled
Kind: conditional-validity

Specification statement:

Scale on low-end devices is a medium viability risk: millions of records, low-end Android phones, offline operation, and years of accumulated data can exceed local storage or query performance if not addressed explicitly.

Source basis:

- `docs/viability-assessment.md` / `Risk Flags` / `R5`
- `docs/constraints.md` / `## Scale`

Closure basis:

Settled as a viability risk. Not closed as a selective-sync, local lifecycle, archive, or performance design.

Scope:

Applies to local storage, query performance, selective data availability, accumulated records, and field-device constraints.

Non-goals:

Does not decide local database design, sync filtering, archive strategy, or performance budgets.

Forbidden interpretations:

- Do not assume every device can hold the full deployment dataset.
- Do not ignore local lifecycle just because central records can accumulate.

Open edges:

Selective sync, local lifecycle, and performance design remain to be closed by later sources.

Platform specification note:

The platform specification should include low-end device and scale constraints wherever it defines local data availability.

## Kernel: Principle Validation Lifecycle

Status: Settled
Kind: interaction-rule

Specification statement:

Principles begin as hypotheses derived from vision, constraints, and behavioral patterns. Constraint decisions test them; a principle can be confirmed, refined, challenged, revised, or retired before later decisions proceed.

Source basis:

- `docs/principles.md` / opening section
- `docs/principles.md` / `## How Principles Get Tested`

Closure basis:

Settled as the principle lifecycle described by the approved source.

Scope:

Applies to the relationship between pre-architecture principles and later constraint decisions.

Non-goals:

Does not decide any specific architecture mechanism.

Forbidden interpretations:

- Do not treat initial principles as untested final architecture.
- Do not ignore principle challenges; the source requires revision before proceeding.

Open edges:

Detailed decision closure remains with exploration and ADR sources.

Platform specification note:

The platform specification should use principles as decision rationale and guardrails, not as substitutes for detailed contracts.

## Kernel: Offline Default Principle

Status: Settled
Kind: invariant

Specification statement:

Offline operation is the default. Every behavior that works online must work offline unless later approved sources explicitly narrow it. Connectivity is for synchronization, not for primary operation.

Source basis:

- `docs/principles.md` / `P1: Offline is the default, not the exception`

Closure basis:

Settled as a validated working principle in this source. Detailed mechanisms remain outside this source.

Scope:

Applies to platform behavior under field-level disconnection and intermittent connectivity.

Non-goals:

Does not decide local storage, sync unit, conflict detection, or which coordinator-only operations may require reliable connectivity.

Forbidden interpretations:

- Do not require connectivity for primary field operation.
- Do not model offline support as an optional exception path.

Open edges:

Offline mechanisms remain to be extracted from exploration and ADRs.

Platform specification note:

Use as the governing principle for offline-first platform contracts.

## Kernel: Bounded Configuration Principle

Status: Settled
Kind: configuration-boundary

Specification statement:

"Set up, not built" does not mean infinitely configurable. Configuration should combine and parameterize platform-provided capabilities within explicit boundaries; needs outside those boundaries require platform evolution or are out of scope.

Source basis:

- `docs/principles.md` / `P2: Configuration has boundaries`

Closure basis:

Settled as a validated working principle. Final configuration layers and boundary details remain to be extracted from later sources.

Scope:

Applies to deployer setup, activity configuration, rules, policies, pattern selection, and platform evolution.

Non-goals:

Does not decide configuration syntax, layer count, expression language, policy model, or extension mechanism.

Forbidden interpretations:

- Do not turn configuration into a general-purpose programming language.
- Do not create hidden workarounds for needs outside the configured boundary.

Open edges:

Concrete configuration boundary remains to be closed by exploration and ADRs.

Platform specification note:

The platform specification should make deployer configurability explicit and bounded.

## Kernel: Append-Only History Principle

Status: Settled
Kind: invariant

Specification statement:

Records are append-only and history is preserved. Corrections append rather than replace, and the full history of who did what, when, under what shape, and under what authority remains recoverable.

Source basis:

- `docs/principles.md` / `P3: Records are append-only; history is sacred`

Closure basis:

Settled as a validated working principle. Detailed write model remains to be extracted from ADRs.

Scope:

Applies to corrections, amendments, provenance, schema evolution, authority context, and record history.

Non-goals:

Does not decide event sourcing, envelope shape, storage deletion policy, projection behavior, or correction event contracts.

Forbidden interpretations:

- Do not overwrite or delete history for convenience.
- Do not replace corrections in a way that loses original provenance.

Open edges:

The technical write model and retention/delete semantics remain to be extracted from later sources.

Platform specification note:

Use as the governing principle for record-history and correction semantics.

## Kernel: Composition Over Exceptions Principle

Status: Settled
Kind: invariant

Specification statement:

Real-world complexity should be handled by composing a small set of behavioral patterns rather than adding one-off exceptions. Platform growth should happen through composition and controlled evolution, not by modifying existing behavior for every special case.

Source basis:

- `docs/principles.md` / `P4: Patterns compose; the platform evolves through composition, not modification`

Closure basis:

Settled as a validated working principle. Final platform primitive or mechanism vocabulary remains to be extracted from ADRs.

Scope:

Applies to scenario support, behavioral pattern reuse, platform growth, and exception pressure.

Non-goals:

Does not decide the final primitive list, module boundaries, implementation components, or extension process.

Forbidden interpretations:

- Do not add one-off behavior for every scenario variation.
- Do not treat composition as permission to blur behavioral categories into architecture prematurely.

Open edges:

Technical composition rules remain to be extracted from later sources.

Platform specification note:

Use as a guardrail against special-case platform design.

## Kernel: Conflict Surfacing Principle

Status: Settled
Kind: invariant

Specification statement:

Conflict should be surfaced rather than silently resolved. Automatic resolution is acceptable only when the resolution is provably correct and reversible.

Source basis:

- `docs/principles.md` / `P5: Conflict is surfaced, not silently resolved`

Closure basis:

Settled as a validated working principle. Detailed conflict and flag semantics remain to be extracted from ADRs.

Scope:

Applies to offline edits, concurrent decisions, schema mismatches, identity ambiguity, correction conflicts, and other independent-work conflicts.

Non-goals:

Does not decide conflict categories, flag shapes, resolution workflow, or auto-resolution criteria.

Forbidden interpretations:

- Do not silently overwrite ambiguous independent work.
- Do not auto-resolve unless correctness and reversibility are established by later decisions.

Open edges:

Conflict detection, flagging, and resolution mechanisms remain to be extracted from later sources.

Platform specification note:

Use as the governing principle for anomaly and reconciliation behavior.

## Kernel: Contextual Auditable Authority Principle

Status: Settled
Kind: invariant

Specification statement:

Authority is contextual and auditable. What someone can see and do depends on who they are, what role they hold, what scope they operate in, and when they act. Every action must remain attributable to its authority context.

Source basis:

- `docs/principles.md` / `P6: Authority is always contextual and auditable`

Closure basis:

Settled as a validated working principle. Detailed authority model remains to be extracted from ADRs.

Scope:

Applies to access control, visibility, offline enforcement, role changes, review authority, and audit reconstruction.

Non-goals:

Does not decide assignments, sync scope, actor references, scope fields, or authority projection.

Forbidden interpretations:

- Do not model authority as a global role only.
- Do not lose the context under which an action was performed.

Open edges:

Authority representation and enforcement remain to be extracted from later sources.

Platform specification note:

Use as the governing principle for access-control and attribution contracts.

## Kernel: Simplicity Baseline Principle

Status: Settled
Kind: invariant

Specification statement:

The simplest structured-capture scenario must remain simple. Complexity is opt-in; platform machinery required by richer workflows must not burden scenarios that do not need it.

Source basis:

- `docs/principles.md` / `P7: The simplest scenario stays simple`

Closure basis:

Settled as a validated working principle. Detailed simplicity proof remains to be extracted from ADRs.

Scope:

Applies to S00 and any platform mechanism that could impose unnecessary ceremony on basic capture.

Non-goals:

Does not decide which artifacts or fields are required for S00.

Forbidden interpretations:

- Do not make users understand concepts irrelevant to their simple capture task.
- Do not make advanced workflow, identity, review, trigger, or state machinery mandatory for simple capture.

Open edges:

The concrete minimal contract for S00 remains to be extracted from later sources.

Platform specification note:

Use as a regression check against overcomplicated platform specifications.

