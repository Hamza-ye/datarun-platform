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
- `04-architecture-baseline-v0.md`: current engineering-facing baseline generated from the ADR-001 through ADR-005 closure register.
- `05-decision-gap-register.md`: unresolved post-baseline gaps classified before later ADR assessment or implementation planning.
- `06-baseline-stabilization-plan.md`: ordered stabilization steps for accepting the baseline, assigning closure paths, triaging gaps, and only then assessing later ADRs.
- `07-system-boundary-map.md`: architecture responsibility map for assigning settled mechanisms, gaps, and later claims to one primary responsibility area.
- `08-baseline-acceptance-check.md`: accepted sign-off for ADR-001 through ADR-005 baseline, including priority tiers for accepted gaps.
- `09-identity-boundary-control.md`: assessed identity-routing input that has been routed into the gap register and responsibility map.
- `10-adr006r-flag-semantics-assessment.md`: later-source assessment of ADR-006-R flag-semantics and alias-cycle claims against the accepted baseline and responsibility routing.
- `11-adr007-envelope-type-assessment.md`: later-source assessment of ADR-007 envelope-type closure and shape-based fact discrimination against the accepted baseline.
- `12-adr008-reference-fields-assessment.md`: later-source assessment of ADR-008 reference-field contracts and reference-vs-referent classification against the accepted baseline.
- `13-adr009-duality-rule-assessment.md`: later-source assessment of ADR-009 platform-fixed mechanism versus deployer-configured instance classification against the accepted baseline.
- `14-pattern-inventory-walkthrough-assessment.md`: assessment of the historical pattern-inventory walkthrough as candidate material for Pattern Registry inventory and schema gaps.
- `15-conflict-flag-offline-boundary-control.md`: assessed routing input for conflict detection, accept-and-flag, detect-before-act, flag lifecycle, and offline-default behavior.
- `16-operational-constraints-boundary-control.md`: assessed routing input for operational constraints that must not become architecture or implementation authority by themselves.
- `17-authorization-visibility-boundary-control.md`: assessed routing input for authorization/visibility pressure that must preserve ADR-003 and bounded configuration.
- `18-envelope-shape-parametrization-boundary-control.md`: focused ADR-004 lineage assessment for envelope `type`, `shape_ref`, platform-fixed structure, and deployer parametrization.
- `19-envelope-shape-parametrization-definitions.md`: candidate definition source material for future baseline or gap assessment.

Related context material:

- `../pre-operations/README.md`: readiness assessment, accepted pre-specification decision record, and decision-board context for high-impact choices.
- `../product-alignment/README.md`: product/UX alignment track for reconnecting Phase 1 scenarios, workflows, user expectations, and the accepted architecture baseline.

## Current Standing

- ADR-001 through ADR-005 are the current extracted closure baseline.
- `../10-adr1-5-rest-state-closure-register.md` is the compact closure register.
- `08-baseline-acceptance-check.md` accepts the baseline as stable enough for responsibility validation and targeted later-source assessment.
- `10-adr006r-flag-semantics-assessment.md` classifies ADR-006-R S1 through S4 as mostly compatible general-flag-semantics candidates, while routing ADR-006-R S5 alias-cycle behavior as a formal decision gap before identity/flag specification if in scope.
- `11-adr007-envelope-type-assessment.md` classifies ADR-007 as mostly consistent envelope-boundary elaboration: `type` remains the six-value processing axis, while domain/integrity facts are discriminated by `shape_ref`.
- `12-adr008-reference-fields-assessment.md` classifies ADR-008 as mostly consistent reference-boundary elaboration: `*_ref` fields are envelope contracts, while referents keep separate lifecycle ownership and classification.
- `13-adr009-duality-rule-assessment.md` classifies ADR-009 as mostly consistent platform/deployer-boundary elaboration: platform-fixed mechanisms and deployer-configured instances must stay split in any future controlled specification or implementation assessment.
- `14-pattern-inventory-walkthrough-assessment.md` treats `28-pattern-inventory-walkthrough.md` as candidate material for pattern inventory/schema, not as authority or final pattern specification.
- `15-conflict-flag-offline-boundary-control.md` records routed findings for accepted conflict, flag, and offline-default behavior; durable gap and responsibility routing belongs in `05` and `07`.
- `16-operational-constraints-boundary-control.md` records routed findings for operational constraints such as offline field work, low-end devices, compliance support, interoperability compatibility, responsiveness, and configuration propagation.
- `17-authorization-visibility-boundary-control.md` records routed findings for contextual authority, hierarchy exceptions, temporary authority, role transitions, and offline authorization disagreement.
- `18-envelope-shape-parametrization-boundary-control.md` confirms ADR-004 closed the core line correctly and records the axis split: `type` is processing behavior, `shape_ref` is fact schema/version, `activity_ref` is context, review is layered, and deployer labels do not become platform classes.
- `19-envelope-shape-parametrization-definitions.md` is candidate source material for event-envelope, shape, pattern, reference, projection, and parametrization terms if a gap or change-control assessment needs it.
- ADR-006-R through ADR-009 remain assessment material. Their claims are classified in `10` through `13`; carry-forward constraints, open gaps, and hold-backs are governed by `05-decision-gap-register.md`. They are not automatic authority over ADR-001 through ADR-005.
- Pre-operations readiness and decision-board material lives in `../pre-operations/` so assessment/process material and draft briefs do not mix with baseline artifacts.

## Working Rule

Do not continue broad extraction when the next problem is baseline clarity. Use the closure register to produce engineering-facing baseline artifacts, route them through architecture responsibility areas, then assess later ADRs only against explicit gaps or disputes.

## Current Hold

The `../platform-spec/` workspace is frozen. Do not continue section drafting, registry maintenance, planned-consumer review, or dashboard generation from that workspace.

Use `05-decision-gap-register.md` as the only canonical open-gap register. Assessed material from `09` through `19` remains context unless its durable result is already represented in `04`, `05`, or `07`.

No platform-spec outline artifact is retained. Any future outline-like synthesis must be regenerated from `04`, `05`, and `07` under the current baseline rules.

Product-alignment material was created later and should be assessed separately before it affects this baseline closeout path.
