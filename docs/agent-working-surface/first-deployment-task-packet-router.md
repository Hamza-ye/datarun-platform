# First-Deployment Router

Status: first implementation slice accepted

Date: 2026-06-13

## Current Route

The first-deployment workshop is closed. Use:

1. [summary.md](../workshops/first-deployment/summary.md) for the consolidated
   product outcome, lane standing, S06 disposition, and technical boundary.
2. [implementation-task.md](../workshops/first-deployment/implementation-task.md)
   for the completed first implementation task and evidence.

Do not reconstruct the removed workshop chronology from git history and do not
draft more FD-PKT, role, stage, prerequisite, or gate-review documents for this
slice.

## Dispatch Standing

The mobile sync-status task landed in commit `8692607`. It composes
presentation over existing mobile state and fixes the bug where a failed sync
attempt updated `lastSync`.

S06/entity lifecycle remains a visible BAR-105 / NW-021 future-decision lane,
but it does not block this task because the task contains no subject-link,
known-set, candidate, lifecycle, duplicate, merge, or split behavior.

## Source Order

Use `AGENTS.md`, `docs/status.md` Current Routing, the mobile section of
`docs/implementation/module-interfaces.md`, and the exact code/tests named by
the implementation task. Use the decision-anchor layer only if implementation
discovers pressure for new contracts, authority, sync semantics, or durable
state.

## Completion

NW-059 is accepted with 13 focused tests, 114 full mobile tests, and clean
touched-file analysis. Select the next bounded lane from the summary without
recreating workshop or gate-review process.
