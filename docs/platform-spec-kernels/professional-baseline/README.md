# Professional Baseline Operating Model

This folder defines how to move from extraction evidence toward an engineering-usable platform baseline without drowning in lineage.

It is not an approved source for platform behavior. It is an operating reference for the platform-spec-kernels work.

## Purpose

The extraction work has produced useful evidence, but production engineering teams do not build from extraction notes. They first establish a controlled baseline, identify gaps, and define how later documents can change that baseline.

This folder formalizes that operating model.

## Reference Docs

- `01-baseline-workflow.md`: the order of work from extracted evidence to usable architecture/spec baseline.
- `02-change-control.md`: how post-baseline ADRs or new claims are allowed to affect the baseline.
- `03-artifact-definitions.md`: the expected artifacts and what each one is allowed to contain.
- `04-architecture-baseline-v0.md`: current engineering-facing baseline generated from the ADR-001 through ADR-005 closure overlay.
- `05-decision-gap-register.md`: unresolved post-baseline gaps classified before later ADR assessment or implementation planning.
- `06-baseline-stabilization-plan.md`: ordered stabilization steps for accepting the baseline, assigning closure paths, triaging gaps, and only then assessing later ADRs.
- `07-system-boundary-map.md`: engineering boundary routing map for assigning settled mechanisms, gaps, and later claims to one primary system boundary.
- `08-baseline-acceptance-check.md`: accepted sign-off for ADR-001 through ADR-005 baseline, including priority tiers for accepted gaps.
- `09-identity-boundary-control.md`: dependency-aware overlay that prevents ADR-002's broad identity/conflict document shape from becoming broad implementation coupling.
- `10-adr006r-flag-semantics-assessment.md`: later-source assessment of ADR-006-R flag-semantics and alias-cycle claims against the accepted baseline and validated boundaries.

## Current Standing

- ADR-001 through ADR-005 are the current extracted closure baseline.
- `../10-adr1-5-rest-state-closure-register.md` is the compact closure overlay.
- `08-baseline-acceptance-check.md` accepts the baseline as stable enough for boundary validation, targeted later-source assessment, and platform-spec skeleton work.
- `10-adr006r-flag-semantics-assessment.md` classifies ADR-006-R S1 through S4 as mostly compatible general-flag-semantics candidates, while routing ADR-006-R S5 alias-cycle behavior as a formal decision gap before identity/flag atomization if in scope.
- ADR-006-R through ADR-009 are quarantined assessment material until the baseline is accepted and gap-checked.

## Working Rule

Do not continue broad extraction when the next problem is baseline clarity. Use the closure overlay to produce engineering-facing baseline artifacts, route them through system boundaries, then assess later ADRs only against explicit gaps or disputes.

## Current Next Step

Follow `06-baseline-stabilization-plan.md`: validate `07-system-boundary-map.md` against the accepted baseline, gap priorities, and identity boundary-control overlay, then assess ADR-006-R through ADR-009 only against the stabilized gaps and boundaries.
