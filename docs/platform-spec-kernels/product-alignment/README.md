# Product Alignment Track

Status: Closed pre-atomization product/UX alignment track

This folder reconnects the accepted platform architecture baseline to product behavior, operational workflows, and user expectations before selected-slice atomization and implementation progression.

It does not redesign the architecture. It does not create platform behavior authority by itself. It translates the approved domain problem space into product-facing artifacts, then routes any pressure back through the professional baseline, gap register, and change-control rules.

## Why This Exists

The project reached a strong architecture baseline before the product interaction layer was equally explicit. That creates a real risk: atomized platform specs could become internally correct but weakly grounded in how users actually experience field work, oversight, configuration, sync, review, conflicts, and reporting.

This track reduces that risk by stabilizing product-facing artifacts before implementation progression.

Session output sections in individual artifacts preserve the handoff logic from the time each session was written. For the current forward path, `11-alignment-closeout.md` is the controlling product-alignment closeout artifact.

## Source Boundary

Domain and product-pressure sources:

- `../../README.md`
- `../../constraints.md`
- `../../access-control-scenario.md`
- `../../behavioral_patterns.md`
- `../../principles.md`
- `../../viability-assessment.md`
- `../../scenarios/README.md`
- `../../scenarios/00-basic-structured-capture.md`
- `../../scenarios/01-entity-linked-capture.md`
- `../../scenarios/02-periodic-reporting.md`
- `../../scenarios/03-user-based-assignment.md`
- `../../scenarios/04-supervisor-review.md`
- `../../scenarios/05-supervision-audit-visits.md`
- `../../scenarios/06-entity-registry-lifecycle.md`
- `../../scenarios/07-resource-distribution.md`
- `../../scenarios/08-case-management.md`
- `../../scenarios/09-coordinated-campaign.md`
- `../../scenarios/10-dynamic-targeting.md`
- `../../scenarios/11-multi-step-approval.md`
- `../../scenarios/12-event-triggered-actions.md`
- `../../scenarios/13-cross-flow-linking.md`
- `../../scenarios/14-multi-level-distribution.md`
- `../../scenarios/19-offline-capture-and-sync.md`
- `../../scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`

Architecture guardrails:

- `../10-adr1-5-rest-state-closure-register.md`
- `../00-extraction-state.md`
- `../professional-baseline/README.md`
- `../professional-baseline/04-architecture-baseline-v0.md`
- `../professional-baseline/05-decision-gap-register.md`
- `../professional-baseline/07-system-boundary-map.md`
- `../professional-baseline/09-identity-boundary-control.md`
- `../professional-baseline/15-conflict-flag-offline-boundary-control.md`
- `../professional-baseline/16-operational-constraints-boundary-control.md`
- `../professional-baseline/17-authorization-visibility-boundary-control.md`
- `../pre-operations/04-accepted-pre-atomization-decisions.md`

## Non-Authority Rule

Product-alignment artifacts may:

- group scenarios by product experience and operational tension
- describe user-facing behavior
- propose product vocabulary
- identify UX gaps
- recommend atomization sequencing

They may not:

- add event envelope fields
- rename architecture mechanisms as if the baseline changed
- close architecture gaps
- turn candidate vocabulary into platform vocabulary without mapping
- treat a UI concept as canonical storage or projection truth
- bypass `../professional-baseline/02-change-control.md`

## Domain-Agnostic Rule

The product model must preserve the ambition from `../../README.md`: the platform should feel like one coherent system, with the same concepts, contracts, and ways of seeing what happened and what is pending across simple reporting, review, case follow-up, and multi-level distribution campaigns.

Domain-specific examples may explain behavior, but the artifact language should prefer domain-agnostic terms unless a scenario is being quoted or summarized.

## Planned Sessions

1. `01-phase-1-scenario-boundary-map.md`: group Phase 1 scenarios by product boundary and tension.
2. `02-product-experience-principles.md`: product-facing principles derived from domain commitments and operational constraints.
3. `03-user-roles-and-operational-contexts.md`: product operating contexts, expectations, and working environments.
4. `04-core-operational-journeys.md`: main user journeys and operational workflows.
5. `05-information-architecture.md`: product navigation and work surfaces.
6. `06-product-vocabulary-alignment.md`: product terms mapped to baseline terms and forbidden interpretations.
7. `07-interaction-state-model.md`: user-visible states, offline/sync states, review states, flag/conflict states, and what remains UI-only.
8. `08-ux-gap-routing.md`: product/UX gaps routed to the existing gap register or proposed as explicit additions.
9. `09-first-vertical-slice.md`: first product-backed slice for atomization and implementation progression.
10. `10-atomization-readiness-from-product.md`: readiness gate for selected-slice atomization.
11. `11-alignment-closeout.md`: close the product-alignment track and state the stable atomization input surface.

## Stable Outputs

- `01-phase-1-scenario-boundary-map.md`: Session 1 boundary and tension map.
- `02-product-experience-principles.md`: Session 2 product experience principles.
- `03-user-roles-and-operational-contexts.md`: Session 3 product role and operating-context model.
- `04-core-operational-journeys.md`: Session 4 reusable operational journey model.
- `05-information-architecture.md`: Session 5 product surface and navigation model.
- `06-product-vocabulary-alignment.md`: Session 6 vocabulary control and baseline-mapping artifact.
- `07-interaction-state-model.md`: Session 7 operational-surface state model.
- `08-ux-gap-routing.md`: Session 8 routing from product pressure to gaps, deferrals, and change-control triggers.
- `09-first-vertical-slice.md`: Session 9 selected first product-backed vertical slice.
- `10-atomization-readiness-from-product.md`: Session 10 atomization readiness gate for the selected slice.
- `11-alignment-closeout.md`: Session 11 closeout and stable atomization input surface.

## Next Step

Begin selected-slice atomization from `09-first-vertical-slice.md`, `10-atomization-readiness-from-product.md`, and `08-ux-gap-routing.md`.

## Working Rule

Do not atomize broad internal platform surfaces from this track. Atomize only after product behavior, scenario pressure, and baseline boundary ownership agree.
