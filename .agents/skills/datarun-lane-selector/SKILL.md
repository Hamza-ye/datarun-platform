---
name: datarun-lane-selector
description: Choose the correct Datarun working lane before starting: PM planning, implementation, validation audit, architecture boundary review, PR review, or steward reconciliation. Use when the task asks what to do next, which agent role to use, or how to route ambiguous work.
---

# Datarun Lane Selector

Use this skill to recommend exactly one working lane before work starts. Do not
perform that lane's work or select a successor NW unless the user explicitly
asks.

## Required Inputs

1. `docs/status.md` Current Routing.
2. The user request and any named NW/task packet, PR, diff, or evidence claim.
3. For "what next", product-route, or current-standing requests:
   `docs/agent-working-surface/platform-next-work-backlog.md` Active Work Index
   and the product planning/spec source named by Current Routing.
4. For explicit validation, review, or architecture questions: the diff/PR,
   validation evidence, or trigger evidence supplied by the request.

## Current-Standing Gate

- Do not select Implementation Agent unless the task or Current Routing names
  one selected NW/task packet ready for execution.
- Do not revive candidate, deferred, future-decision, or blocked product work as
  active implementation.
- Prefer production/pilot readiness work that can become one bounded PR with
  acceptance criteria and executable evidence over process work, unless the user
  explicitly asks for audit/routing/reconciliation or a stop trigger fires.
- If no active implementation slice is selected and the user asks to start
  product work, route to PM Product Planner or stop for owner selection.

## Lane Rules

- PM Product Planner: selecting or shaping product-facing next work, product
  goals, journey slices, owner decisions, or candidate NW routes.
- Implementation Agent: executing one selected NW/task packet.
- Tester / Validation Auditor: mapping diff, PR, CI, or acceptance claims to
  required validation evidence.
- Software Architect Boundary Reviewer: concrete stop trigger involving CDL,
  contracts, stored truth, authority, sync/access, runtime partitioning,
  workflow state, reporting/audit breadth, retention/security, or gap routing.
- Reviewer: fresh-context PR/diff review.
- Steward Session Guide: explicit routing, reconciliation, checkpoint, drift, or
  broad audit work.

## Output

Return:

- current standing used;
- selected lane;
- why;
- why Implementation Agent is or is not allowed;
- primary input files;
- expected output;
- bounded PR or acceptance-evidence shape when product work is being routed;
- stop conditions;
- return trigger to product progress.

## Quick Tests

- Current Routing says no active implementation slice; user asks to start the
  next product work: PM Product Planner or owner-selection stop.
- User names a ready NW/task packet with files and tests: Implementation Agent.
- User asks whether evidence is sufficient or CI is failing: Tester /
  Validation Auditor.
- User provides a PR/diff for fresh review: Reviewer.
- The request would alter contracts, authority, sync/access scope, stored event
  meaning, or another AGENTS stop trigger: Software Architect Boundary Reviewer.
