---
name: datarun-implementation-agent
description: Execute one selected Datarun NW or task packet with product-slice-first scope, bounded changes, context-friendly validation, and explicit evidence.
---

# Datarun Implementation Agent

Use this skill only when the user task or `docs/status.md` Current Routing names
one selected NW/task packet ready for execution.

## Required Inputs

1. User task and selected NW/task packet.
2. `docs/status.md` Current Routing.
3. Active NW row or task packet acceptance criteria, non-goals, tests, and stop
   conditions.
4. Exact touched files and local code/test patterns.
5. Relevant nested `server/AGENTS.md`, `mobile/AGENTS.md`, or
   `contracts/AGENTS.md`.
6. `docs/agent-working-surface/validation-matrix.md`.

## Checklist

1. Confirm the selected NW/task and user/deployment outcome.
2. Inspect only the needed docs, code, tests, contracts, and specs.
3. Plan briefly when the work is multi-file or crosses surfaces.
4. Implement the scoped change.
5. Run the narrowest focused gate that proves the touched behavior.
6. Run the required full gate for the touched surface when acceptance requires
   it.
7. Update status/backlog/BAR only when the task authorizes that state change.
8. Report final worktree state and validation evidence.

## Maven / CI Output

For Maven/server gates, follow root `AGENTS.md` and `server/AGENTS.md`: focused
runs may stream when small; noisy/full or CI-equivalent runs should be captured
to `/tmp` and summarized.

## Surfaced Follow-Up Visibility

Classify each material follow-up as one of:

- current-slice fix;
- selected successor;
- candidate backlog row;
- explicit deferral with trigger;
- rejected or not a risk, with reason.

If status/backlog edits are not authorized, report the items in the PR body or
final handoff under "Surfaced follow-ups needing routing."

Before acceptance, status/backlog must show one of:

- exactly one selected successor;
- no successor selected with reason;
- explicit owner decision pending;
- candidate/deferred rows for material residuals.

## Stop Conditions

Stop and report if the work would change product scope, architecture authority,
contracts, runtime behavior outside the selected slice, CI, BAR, CDL, gap
register, or artifact trace without explicit selection.

## Must Not

- Do not choose the next NW.
- Do not act as steward by default.
- Do not read broad architecture by default.
- Do not change status or backlog unless authorized.
- Do not mark work accepted just because files changed.
- Do not implement future, deferred, or candidate routes without selection.
