# First-Deployment Router

Status: second implementation slice accepted

Date: 2026-06-13

## Current Route

The first-deployment workshop is closed. Use:

1. [summary.md](../workshops/first-deployment/summary.md) for the consolidated
   product outcome, lane standing, S06 disposition, and technical boundary.
2. [capture-handoff-task.md](../workshops/first-deployment/capture-handoff-task.md)
   for the completed second implementation task and evidence.
3. [implementation-task.md](../workshops/first-deployment/implementation-task.md)
   for the completed first implementation task and evidence.

Do not reconstruct the removed workshop chronology from git history and do not
draft more FD-PKT, role, stage, prerequisite, or gate-review documents for this
slice.

## Dispatch Standing

NW-060 landed in commit `5dad1c9`. Successful form saves now return to the
surviving screen, refresh existing projection/pending state, confirm local-save
status, and keep pending work visible from the work list.

S06/entity lifecycle remains a visible BAR-105 / NW-021 future-decision lane,
but it does not block this task. NW-060 reuses the existing subject-linked
capture path without adding known-set, candidate, lifecycle, duplicate, merge,
split, or subject-link authority.

## Source Order

Use `AGENTS.md`, `docs/status.md` Current Routing, the mobile section of
`docs/implementation/module-interfaces.md`, and the exact code/tests named by
the implementation task. Use the decision-anchor layer only if implementation
discovers pressure for new contracts, authority, sync semantics, or durable
state.

## Completion

NW-059 is accepted with 13 focused tests, 114 full mobile tests, and clean
touched-file analysis. NW-060 is accepted with 12 focused tests, 119 full
mobile tests, and clean touched-file analysis. Select the next bounded product
slice from the summary without recreating workshop or gate-review process.
