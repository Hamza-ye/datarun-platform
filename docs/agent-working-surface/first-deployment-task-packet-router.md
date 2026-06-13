# First-Deployment Router

Status: implementation dispatch active

Date: 2026-06-13

## Current Route

The first-deployment workshop is closed. Use:

1. [summary.md](../workshops/first-deployment/summary.md) for the consolidated
   product outcome, lane standing, S06 disposition, and technical boundary.
2. [implementation-task.md](../workshops/first-deployment/implementation-task.md)
   for the only active implementation task.

Do not reconstruct the removed workshop chronology from git history and do not
draft more FD-PKT, role, stage, prerequisite, or gate-review documents for this
slice.

## Dispatch Standing

The mobile sync-status task is ready for implementation. It is presentation
composition over existing mobile state and fixes a current bug where a failed
sync attempt updates `lastSync`.

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

The slice is complete when the implementation task's focused and full Flutter
tests pass and the code lands in its stated commit boundary. Update status and
NW evidence after implementation, not by creating another review packet.
