---
name: datarun-pm-product-planner
description: Shape Datarun product goals, Product Candidate handoffs, owner decisions, and candidate NW routes into one product-first next-work recommendation. Use for PM/product planning, not implementation.
---

# Datarun PM Product Planner

Use this skill to shape one product-first next-work recommendation. It creates
planning output only; it does not accept scope, change runtime behavior, or
start implementation.

## Required Inputs

1. `docs/status.md` Current Routing.
2. `docs/agent-working-surface/platform-next-work-backlog.md` Active Work Index
   and relevant accepted rows.
3. `docs/agent-working-surface/product-journey-and-slice-sequencing.md` when
   shaping product goals, representative journeys, candidate boundaries, or
   ordered slice plans.
4. `docs/specifications/product/product-goal-and-representative-journeys.md`
   as the durable planning source for the active Product Goal and journey
   portfolio after NW-168.
5. `docs/specifications/product/product-model-consolidation-and-slice-backlog.md`
   when current standing involves the ordered M1-M5 product backlog.
6. Product Candidate handoff, accepted product spec, or owner decision named by
   status or the task.
7. `docs/agent-working-surface/validation-matrix.md` for evidence category only.

## Planning Rules

- Start from user/deployment outcome and current standing, not screens, APIs,
  tables, fixtures, tests, or historical artifact tone.
- Prefer production/pilot readiness slices that can become one bounded PR with
  acceptance criteria and executable evidence.
- Do not jump from a completed technical proof to another implementation slice
  without citing the active Product Goal, representative journey, and accepted
  non-goals.
- Treat scenarios, walkthroughs, runtime probes, and tests as evidence, not
  product authority.
- Preserve candidate, deferred, future-decision, and blocked standing unless
  the request supplies the trigger or owner decision that changes it.
- If the current process looks unhealthy, recover with proven basics: reduce
  WIP, shrink scope, restore green CI/trunk health, close stale work, and
  document only actionable facts.

## Output

Return:

- user/deployment value;
- current standing used;
- why now;
- input sources;
- one recommended next NW or explicit park/owner-decision request;
- output artifact/code expected;
- acceptance criteria;
- validation category and executable evidence shape;
- stop conditions;
- open owner decisions;
- return trigger to implementation.

## Must Not

- Do not modify runtime code.
- Do not change architecture, contracts, platform specs, validation policy, BAR,
  CDL, gap register, or artifact trace.
- Do not mark candidate routes accepted.
- Do not approve real production, real users/data, imports, or cutover.
- Do not bypass the validation matrix.
- Do not act as steward unless routing or reconciliation is explicitly selected.
