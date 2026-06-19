# Product Candidate Handoff Template

Status: active template
Document type: product_handoff_template
Owner: product steward
Authority: template only; creates no product behavior or acceptance by itself

Use this template when a future product candidate needs a PM planning surface.
The accepted product spec remains the source of product behavior. The handoff
routes planning decisions, candidate next-work rows, stop conditions, and PM
ownership questions.

## Product Goal

State the product goal in one paragraph. It must come from an accepted product
spec, accepted audit, or explicitly routed owner decision. Do not add behavior
inside the goal.

## Target Deployment / Customer Archetype

- Customer or organization shape:
- Deployment lane:
- Real-production standing:
- Internal vocabulary that must not leak into product language:
- Approval route if real users or data are involved:

## Primary Users / Jobs

| User | Job | Notes |
|---|---|---|
|  |  |  |

## In-Scope Journeys

- Journey 1:
- Journey 2:
- Journey 3:

## Explicit Non-Goals

- Non-goal:
- Deferred route:
- Do-not-cross boundary:

## Current Standing

| Product slice | Current standing | Accepted NW/source | Remaining route | PM interpretation |
|---|---|---|---|---|
|  |  |  |  |  |

## Scenario-to-Slice Map

| Scenario pressure | User value | Product-candidate journey | Current support | Candidate NW route | Next product decision | Do-not-cross boundary |
|---|---|---|---|---|---|---|
|  |  |  |  |  |  |  |

## Candidate NW Decomposition Routes

These are candidate routes only. Promote at most one bounded row at a time.

| Candidate route | Suggested priority | User value / why now | Input sources | Output expected | Acceptance evidence | Stop condition |
|---|---|---|---|---|---|---|
|  |  |  |  |  |  |  |

## Product-Level Definition Of Done

| Journey | Done when user can | Evidence category | Required guardrail | Detailed validation owner |
|---|---|---|---|---|
|  |  |  |  |  |

## Owner Decisions

- Decision:
- Product owner:
- Deadline or trigger:

## Validation Matrix Link

- Detailed validation matrix:
  [agent validation matrix](../../agent-working-surface/validation-matrix.md).
- Known red checks:
- Checks that belong to a future validation/CI reset:

## Stop Conditions

Stop and route before implementation if the candidate:

- changes product behavior outside the accepted product spec;
- changes architecture authority, contracts, schemas, envelope fields, sync
  protocol, validation/CI policy, or production approval;
- turns candidate PM routes into accepted backlog rows without an NW selection;
- claims real-production readiness without a real-production approval route;
- combines unrelated product, platform, operations, or architecture decisions.

## Future-PC Closure Checklist

- Accepted product spec linked.
- Handoff reviewed by product steward.
- Current standing table matches accepted status/backlog rows.
- Scenario map covers the main pressures and deferred scenarios.
- Candidate routes are candidates only.
- Product-level DoD points to the detailed validation owner.
- Owner decisions are explicit.
- Stop conditions are present.
- Status/backlog updated when the handoff is accepted.
