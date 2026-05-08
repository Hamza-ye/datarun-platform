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
- `11-adr007-envelope-type-assessment.md`: later-source assessment of ADR-007 envelope-type closure and shape-based fact discrimination against the accepted baseline.
- `12-adr008-reference-fields-assessment.md`: later-source assessment of ADR-008 reference-field contracts and reference-vs-referent classification against the accepted baseline.
- `13-adr009-duality-rule-assessment.md`: later-source assessment of ADR-009 platform-fixed mechanism versus deployer-configured instance classification against the accepted baseline.
- `14-pattern-inventory-walkthrough-assessment.md`: assessment of the historical pattern-inventory walkthrough as candidate material for Pattern Registry inventory and schema gaps.
- `15-conflict-flag-offline-boundary-control.md`: dependency-aware overlay that keeps conflict detection, accept-and-flag, detect-before-act, flag lifecycle, and offline-default behavior from collapsing into one broad boundary during atomization.
- `16-operational-constraints-boundary-control.md`: dependency-aware overlay that preserves `constraints.md` as operational envelope authority without turning it into architecture or implementation authority.
- `17-authorization-visibility-boundary-control.md`: dependency-aware overlay that preserves `access-control-scenario.md` as authorization/visibility pressure without mutating ADR-003 or widening deployer access-control authority.

Related pre-operations material:

- `../pre-operations/README.md`: readiness assessment, accepted pre-atomization decision record, and decision-board entrypoint for reviewing high-impact choices before affected atomization.
- `../product-alignment/README.md`: product/UX alignment track for reconnecting Phase 1 scenarios, workflows, user expectations, and the accepted architecture baseline before broad atomization.

## Current Standing

- ADR-001 through ADR-005 are the current extracted closure baseline.
- `../10-adr1-5-rest-state-closure-register.md` is the compact closure overlay.
- `08-baseline-acceptance-check.md` accepts the baseline as stable enough for boundary validation, targeted later-source assessment, and platform-spec skeleton work.
- `10-adr006r-flag-semantics-assessment.md` classifies ADR-006-R S1 through S4 as mostly compatible general-flag-semantics candidates, while routing ADR-006-R S5 alias-cycle behavior as a formal decision gap before identity/flag atomization if in scope.
- `11-adr007-envelope-type-assessment.md` classifies ADR-007 as mostly consistent envelope-boundary elaboration: `type` remains the six-value processing axis, while domain/integrity facts are discriminated by `shape_ref`.
- `12-adr008-reference-fields-assessment.md` classifies ADR-008 as mostly consistent reference-boundary elaboration: `*_ref` fields are envelope contracts, while referents keep separate lifecycle ownership and classification.
- `13-adr009-duality-rule-assessment.md` classifies ADR-009 as mostly consistent platform/deployer-boundary elaboration: platform-fixed mechanisms and deployer-configured instances must be split during atomization.
- `14-pattern-inventory-walkthrough-assessment.md` treats `28-pattern-inventory-walkthrough.md` as candidate material for pattern inventory/schema, not as authority or final pattern specification.
- `15-conflict-flag-offline-boundary-control.md` keeps the validated principles visible while routing accepted conflict, flag, and offline-default behavior through the stabilized boundaries before platform-spec atomization.
- `16-operational-constraints-boundary-control.md` routes operational constraints such as offline field work, low-end devices, compliance support, interoperability compatibility, responsiveness, and configuration propagation into accepted mechanisms or explicit gaps.
- `17-authorization-visibility-boundary-control.md` routes contextual authority, hierarchy exceptions, temporary authority, role transitions, and offline authorization disagreement into accepted mechanisms or explicit gaps.
- ADR-006-R through ADR-009 remain assessment material. Their claims are classified in `10` through `13`; they are not automatic authority over ADR-001 through ADR-005.
- Pre-operations readiness and decision-board material lives in `../pre-operations/` so assessment/process material and draft briefs do not mix with baseline artifacts.

## Working Rule

Do not continue broad extraction when the next problem is baseline clarity. Use the closure overlay to produce engineering-facing baseline artifacts, route them through system boundaries, then assess later ADRs only against explicit gaps or disputes.

## Current Next Step

Use `../product-alignment/README.md` as the next pre-atomization working surface. It should consume `../pre-operations/04-accepted-pre-atomization-decisions.md`, `15-conflict-flag-offline-boundary-control.md`, `16-operational-constraints-boundary-control.md`, and `17-authorization-visibility-boundary-control.md` as guardrails while stabilizing product behavior before broad atomization resumes. Treat the decision briefs in `../pre-operations/` as stakeholder rationale, not instructions.
