# NW-168 - Establish Next Product Goal And Representative Journey Portfolio

Status: ready prompt
Document type: execution_packet
Owner: PM Product Planner
Authority: routing/planning input only; creates no accepted product behavior, platform authority, implementation scope, real-production approval, or architecture decision by itself

## Goal

Establish the next active Product Goal and a small representative journey portfolio that will feed the next consolidation pass.

This NW is not the consolidation pass itself. It selects the evidence set and Product Goal basis for the next work. It must prevent both failure modes:

- jumping directly from technical proof to implementation without a product journey frame;
- creating a broad conceptual model before selecting the journey evidence it must serve.

Expected durable output:

`docs/specifications/product/product-goal-and-representative-journeys.md`

Expected successor, if this NW succeeds:

`NW-169 - Consolidate cross-scenario product conceptual model and interaction grammar`

## Read First

Required:

- `AGENTS.md`
- `docs/status.md`
- `docs/documentation-organization.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/product-journey-and-slice-sequencing.md`
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
- `docs/agent-working-surface/baseline-acceptance-register.md`
- `docs/agent-working-surface/validation-matrix.md`
- `docs/specifications/product/README.md`
- `docs/scenarios/README.md`
- `docs/viability-assessment.md`
- `docs/behavioral_patterns.md`
- `docs/reviews/scenario-baseline-pressure-map.md`
- `docs/reviews/viability-closure-review.md`
- `docs/workshops/first-deployment/summary.md`
- `docs/scenarios/scenario-user-fit-packets/`

Read individual scenario files only as needed to justify the selected portfolio. Do not read the whole repository by default.

## Authority And Evidence Rules

- CDL remains architecture authority.
- Contracts remain implementation-facing interface authority.
- Accepted specs and BAR rows are accepted standing only where they explicitly apply.
- Scenario files, user-fit packets, viability reviews, workshops, runtime tests, and prior PRs are evidence.
- Code and tests prove current behavior; they do not define product semantics by themselves.
- Product labels, domain examples, fixture names, and legacy form names must not become platform vocabulary.
- Gap and future-decision rows are routable when triggered, not forbidden by default.
- `docs/specifications/product/product-goal-and-representative-journeys.md` is a derived planning surface, not accepted product behavior.

## Product Framing

Datarun must be treated as one coherent configurable system:

- same product concepts;
- same interaction grammar;
- reusable behavioral compositions;
- deployment-specific terminology and configuration;
- progressively delivered usable journeys.

Scenarios provide product evidence and composition pressure. They must not become separate domain products or silently redefine platform mechanisms.

The selected portfolio must test whether this product frame can contain simple, composite, and materially different scenarios without creating separate domain products.

## Work

Create:

`docs/specifications/product/product-goal-and-representative-journeys.md`

Use this header:

```text
Status: active planning surface
Document type: product_planning
Owner: product steward
Source: NW-168
Authority: derived planning surface only; creates no accepted product behavior, platform authority, implementation scope, real-production approval, or architecture decision by itself
Last reviewed: 2026-06-26
Supersedes: none
Related: docs/agent-working-surface/product-journey-and-slice-sequencing.md; docs/scenarios/README.md; docs/viability-assessment.md; docs/behavioral_patterns.md
```

The document must include these sections.

## 1. Product Goal

State one active Product Goal.

The Product Goal must include:

* target users and responsibilities;
* user/deployment outcome;
* why this goal matters now;
* measurable success signals;
* representative scenario families;
* explicit exclusions that protect focus without treating future work as prohibited.

Do not define architecture, runtime implementation, product candidate scope, or accepted product behavior in this goal.

## 2. Representative Journey Portfolio

Select a small representative journey portfolio:

1. One simple journey.
2. One composite journey.
3. One materially different journey that tests domain neutrality.

For each journey, record:

* journey name;
* scenario/source files;
* user/responsibility;
* initiating condition;
* user intent;
* desired user-success view;
* end-to-end outline;
* online/offline touchpoints;
* attention, review, correction, handoff, or failure moments if present;
* current evidence status: `PROVEN`, `PARTIAL`, `NOT_RUN`, or `FAILED`;
* existing evidence from accepted NWs, tests, workshops, specs, or reviews;
* unresolved assumptions;
* why this journey belongs in the portfolio.

Prefer coverage that includes:

* S00 simplicity or equivalent simple structured capture;
* S01/S19-style field/mobile/responsibility/freshness pressure;
* a composite supervision/review/handoff journey such as S20/S21/S22/S27 where supported by evidence;
* at least one non-health or domain-neutral pressure case.

Do not force those exact scenarios if the repository evidence shows a better portfolio. Explain the choice.

## 3. Portfolio Coverage Map

Add a compact table:

```text
Journey
Scenario/source coverage
Why selected
Behavioral patterns involved
Existing evidence
Main stress tested
Status
```

The map should make clear why this portfolio is enough input for NW-169.

## 4. What NW-169 Must Consolidate

Explain what NW-169 should use this portfolio to produce:

* cross-scenario product conceptual model;
* interaction grammar;
* scenario coverage matrix;
* product-level Definition of Done;
* mismatch classification using the NW-167 sequencing matrix.

NW-168 must not create or accept that full conceptual model.

## 5. Explicit Non-Selections

Record that NW-168 does not select:

* runtime implementation;
* product candidate handoff;
* full conceptual model acceptance;
* UI/component model acceptance;
* architecture/platform decision;
* contract/schema/sync change;
* BAR/CDL/gap-register change unless a direct contradiction is found;
* real-production approval;
* domain-specific product vocabulary acceptance;
* legacy data import, account import, submitted-record replay, or production cutover.

## 6. Deferrals And Wake-Up Conditions

For every important deferred concern, use this format:

```text
Capability:
Related user and journey step:
Why it matters:
Current evidence:
Why it is not required now:
Consequence of deferral:
Dependency or trigger:
Expected reconsideration point:
```

Do not use `future decision`, `not production`, or `out of scope` without reason, consequence, and wake-up condition.

## 7. Recommended Successor

If the Product Goal and portfolio are coherent, select exactly one successor:

`NW-169 - Consolidate cross-scenario product conceptual model and interaction grammar`

NW-169 should be planning/consolidation only. It should not implement runtime behavior.

If NW-168 cannot select a coherent portfolio, stop and explain the smallest missing owner decision or evidence gap. Do not select implementation as fallback.

## Required Updates

Update:

* `docs/specifications/product/README.md`
* `docs/status.md`
* `docs/agent-working-surface/platform-next-work-backlog.md`

Status/backlog must say NW-168 is planning/evidence selection only and must not select implementation directly.

Add or update the NW-169 row only if NW-168 succeeds.

Do not update BAR, CDL, contracts, gap register, validation policy, runtime code, server code, mobile code, or operations policy unless a direct contradiction is found. If a contradiction is found, stop and report instead of patching those surfaces inside NW-168.

## Validation

Run:

```bash
git diff --check
test -f docs/specifications/product/product-goal-and-representative-journeys.md
rg "Product Goal|representative journey|NW-169|PROVEN|PARTIAL|NOT_RUN|FAILED" docs/specifications/product/product-goal-and-representative-journeys.md
rg "Product Goal And Representative Journeys" docs/specifications/product/README.md
rg "NW-168|NW-169|product-goal-and-representative-journeys" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
```

Runtime tests are not required because this is product-planning/routing only.

## Stop Conditions

Stop and report if the work appears to require:

* accepting a new platform mechanism;
* changing CDL, BAR, contracts, schemas, sync protocol, stored event meaning, authority, or scope;
* treating a scenario label, legacy form, fixture name, or test helper as product authority;
* selecting implementation before the Product Goal and portfolio are explicit;
* accepting a domain-specific product vocabulary as the shared Datarun vocabulary;
* adding another broad planning artifact without a successor route;
* moving scenario/proof evidence into product specs as accepted behavior without explicit source and caveat.

## PR Body Requirements

When drafting or updating the PR body, state:

* durable output path;
* selected Product Goal;
* selected representative journeys;
* successor selected or reason no successor was selected;
* validation commands and results;
* runtime tests skipped because no runtime code changed;
* no BAR/CDL/contracts/gap-register/runtime/product-behavior acceptance changed.
