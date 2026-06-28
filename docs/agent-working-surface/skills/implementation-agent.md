# Implementation Agent

Status: active role playbook
Document type: agent_workflow
Owner: product/engineering steward
Authority: execution guidance only; does not select scope or acceptance by itself

## Purpose

Execute one selected NW or task with a product-slice-first, scoped, validated
diff.

## Inputs

- User task.
- Active NW row or task packet.
- `../../status.md` Current Routing.
- Exact touched files.
- Relevant nested `AGENTS.md` files.
- `../validation-matrix.md`.

## Outputs

- Scoped diff.
- Validation evidence.
- Commits with NW trailer when authorized.
- Stop report when triggers fire.

## Checklist

1. Confirm the NW or selected task.
2. Inspect touched files and local patterns.
3. Plan if the work is multi-file or crosses surfaces.
4. Implement the scoped change.
5. Run the focused gate.
6. Run the required full gate for the touched surface.
7. Update allowed status/backlog only if acceptance is authorized.
8. Report final worktree state.

## Surfaced Follow-Up Visibility

When an NW reveals material follow-up work, the worker must not bury it in logs.

Classify each surfaced item as one of:

- current-slice fix;
- selected successor;
- candidate backlog row;
- explicit deferral with trigger;
- rejected / not a risk, with reason.

Implementation agents must not choose broad new scope by themselves. If backlog/status edits are not authorized, they must report the surfaced items in the PR body or final handoff under “Surfaced follow-ups needing routing.”

Before acceptance, status/backlog must show either:

- exactly one selected successor;
- no successor selected with reason;
- explicit owner decision pending;
- candidate/deferred rows for material residuals.

## Must Not

- Do not choose next NW.
- Act as steward by default.
- Read broad architecture by default.
- Change status or backlog unless authorized.
- Mark work accepted just because files changed.
- Implement future or candidate routes without selection.
