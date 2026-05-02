# Methodology And Extraction Rule Kernel Staging

Status: Iteration 13 staging split

This temporary staging file holds extraction-methodology kernels. These kernels guide how later sources are read; they are not platform behavior or architecture closure.

## Staged Kernels

## Kernel: Exploration Framework Methodology Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/00-exploration-framework.md` is an archived methodology document for how ADR explorations should be conducted and written. It is not architecture, not current source of truth, and not specific to any ADR.

Source basis:

- `docs/exploration/archive/00-exploration-framework.md` / archive notice
- `docs/exploration/archive/00-exploration-framework.md` / opening description

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels derived from the exploration framework.

Non-goals:

Does not decide ADR order, architecture content, platform primitives, or implementation mechanism.

Forbidden interpretations:

- Do not treat the example ADR dependency chain as final architecture order.
- Do not treat methodology rules as platform behavior.
- Do not treat the framework as proving that every later exploration followed it literally.

Open edges:

Actual ADR-specific lineage must be extracted from the individual exploration files and ADR bodies.

Platform specification note:

Use this source to preserve exploration discipline, not as platform specification content.

## Kernel: Assumption Discipline

Status: Settled
Kind: interaction-rule

Specification statement:

Downstream exploration must explicitly declare upstream assumptions, distinguish committed decisions from assumed directional leans, and state what changes if an assumption fails.

Source basis:

- `docs/exploration/archive/00-exploration-framework.md` / `## Working Principle`
- `docs/exploration/archive/00-exploration-framework.md` / `### Pass 1: Explore in Dependency Order`

Closure basis:

Settled as exploration methodology.

Scope:

Applies to reading and extracting ADR exploration files.

Non-goals:

Does not decide whether any specific assumption was valid.

Forbidden interpretations:

- Do not promote an upstream assumption into a settled kernel unless later sources validate it.
- Do not erase conditional branches when an exploration depends on a lower-confidence lean.

Open edges:

Assumption validity must be checked in each ADR-specific extraction.

Platform specification note:

Use this as an extraction rule when deciding whether an exploration claim is candidate, conditional, open, or settled.

## Kernel: Exploration Narrowing Not Decision

Status: Settled
Kind: forbidden-interpretation

Specification statement:

Exploration narrows the decision space by eliminating options, identifying survivors, naming pivotal questions, and stating directional leans with confidence. Exploration does not itself commit final ADR decisions.

Source basis:

- `docs/exploration/archive/00-exploration-framework.md` / `Rules for Pass 1`
- `docs/exploration/archive/00-exploration-framework.md` / `Confidence Levels`

Closure basis:

Settled as exploration methodology.

Scope:

Applies to directional leans, options, stress tests, and confidence levels in exploration files.

Non-goals:

Does not decide the final status of any option.

Forbidden interpretations:

- Do not treat a directional lean as an accepted ADR.
- Do not treat an option surviving exploration as selected.
- Do not treat confidence level as final closure.

Open edges:

Final selection or rejection must come from audit conclusions or ADRs.

Platform specification note:

Use to prevent archive prose from drifting into platform specification as settled architecture.

## Kernel: Flagged Upstream Problem Handling

Status: Settled
Kind: interaction-rule

Specification statement:

Explorations can flag upstream problems, misplaced decisions, and unresolved tensions. Blocking flags make the affected lean conditional; informational flags are carried to audit without invalidating the lean.

Source basis:

- `docs/exploration/archive/00-exploration-framework.md` / `Rules for Pass 1`
- `docs/exploration/archive/00-exploration-framework.md` / exploration document `Flags` guidance

Closure basis:

Settled as exploration methodology.

Scope:

Applies to flags found in ADR exploration files.

Non-goals:

Does not decide whether any specific flag was resolved.

Forbidden interpretations:

- Do not silently absorb upstream problems into downstream conclusions.
- Do not ignore blocking conditionality when extracting a kernel.

Open edges:

Flag resolution must be checked in later audit, exploration, or ADR sources.

Platform specification note:

Use to preserve open and conditional status until resolution evidence appears.

## Kernel: Irreversibility Filter Method

Status: Settled
Kind: interaction-rule

Specification statement:

Exploration should classify findings by irreversibility before stress testing: envelope-touching changes require full adversarial stress test; protocol/server-side choices need lighter validation; policy/configuration choices can be documented without inflated permanence.

Source basis:

- `docs/exploration/archive/00-exploration-framework.md` / `### Irreversibility Filter (before stress testing)`

Closure basis:

Settled as exploration methodology.

Scope:

Applies to extracting permanence, stress-test depth, and closure confidence from ADR exploration files.

Non-goals:

Does not decide which findings in later files actually fall into which category.

Forbidden interpretations:

- Do not inflate evolvable strategies into permanent constraints.
- Do not under-stress envelope-touching decisions.
- Do not apply uniform ceremony where the constraint surface is small.

Open edges:

Each ADR exploration must be checked for its actual irreversibility classifications.

Platform specification note:

Use to distinguish structural constraints from strategy-protecting constraints and initial strategies during later rest-state cleanup.

## Kernel: Decision Audit Gate Method

Status: Settled
Kind: interaction-rule

Specification statement:

Before writing ADRs, the exploration process should audit extracted decisions, validate upstream assumptions, detect scope bleed, detect gaps, verify dependency order, and assign each decision to exactly one ADR.

Source basis:

- `docs/exploration/archive/00-exploration-framework.md` / `### Pass 1 -> Pass 2 Gate: Decision Audit`

Closure basis:

Settled as exploration methodology.

Scope:

Applies to reconstructing how exploration moved into ADR decisions.

Non-goals:

Does not prove that the gate was perfectly followed or that all flags were resolved.

Forbidden interpretations:

- Do not place one decision in multiple ADRs without later source support.
- Do not allow scope bleed to become settled architecture.
- Do not proceed from exploration lean to ADR-level closure without audit or ADR evidence.

Open edges:

Actual audit outcomes must be extracted from the relevant exploration/audit and ADR files.

Platform specification note:

Use as the rest-state checklist for cleaning candidate kernels before atomizing final docs.

## Kernel: ADR Scope Hygiene Method

Status: Settled
Kind: interaction-rule

Specification statement:

ADRs should reference committed prior ADRs, include explicit "What This Does NOT Decide" boundaries, describe downstream consequences as constraints rather than decisions, and classify sub-decisions by permanence while leaving detailed rationale in exploration.

Source basis:

- `docs/exploration/archive/00-exploration-framework.md` / `### Pass 2: Write ADRs in Dependency Order`
- `docs/exploration/archive/00-exploration-framework.md` / `## Principles for Scoping`
- `docs/exploration/archive/00-exploration-framework.md` / `### Evolvability Check`

Closure basis:

Settled as ADR-writing methodology.

Scope:

Applies to interpreting ADR bodies and their relationship to exploration files.

Non-goals:

Does not decide any ADR's content or permanence category.

Forbidden interpretations:

- Do not treat ADR consequences as decisions for downstream ADRs.
- Do not ignore "What This Does NOT Decide" when extracting platform-spec kernels.
- Do not mistake exploration rationale for ADR commitment unless the ADR commits it.

Open edges:

Actual ADR content and compliance with this method remain to be extracted from ADR files.

Platform specification note:

Use to ensure final atomic docs preserve scope boundaries and do not overstate ADR decisions.

## Pending Split Targets

Do not create final atomic files yet. Candidate future groups, to be validated after rest state:

- primitives
- contracts
- invariants
- algorithms
- configuration
- interactions
- forbidden-patterns
- open-questions
- rejected-alternatives
- conditional-validity
