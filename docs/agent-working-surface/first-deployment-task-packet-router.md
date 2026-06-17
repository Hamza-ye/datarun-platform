# First-Deployment Router

Status: closed routing/provenance surface

Date: 2026-06-13

## Current Route

The first-deployment workshop is closed. Use:

1. [summary.md](../workshops/first-deployment/summary.md) for the consolidated
   product outcome, lane standing, S06 disposition, and technical boundary.
2. [capture-handoff-task.md](../workshops/first-deployment/capture-handoff-task.md)
   for the completed second implementation task and evidence.
3. [implementation-task.md](../workshops/first-deployment/implementation-task.md)
   for the completed first implementation task and evidence.
4. [work-readiness-task.md](../workshops/first-deployment/work-readiness-task.md)
   for the completed third implementation task and evidence.
5. [correction-ux-task.md](../workshops/first-deployment/correction-ux-task.md)
   for the completed fourth implementation task and evidence.

Do not reconstruct the removed workshop chronology from git history and do not
draft more FD-PKT, role, stage, prerequisite, or gate-review documents for this
slice.

## Acceptance Standing

NW-062 landed in commit `9e8670a`. It reuses the accepted S00
same-subject/exact-shape/activity capture semantics, prefills active fields from
the selected capture, requires an effective payload change, and appends a new
event without mutating or replacing the original.

S06/entity lifecycle remains a visible BAR-105 / NW-021 future-decision lane,
but it did not block NW-060, NW-061, or NW-062. Those slices add no known-set,
candidate, lifecycle, duplicate, merge, split, or subject-link authority.

## Closed-Slice Source Order

Use `AGENTS.md`, `docs/status.md` Current Routing, the mobile section of
`docs/implementation/module-interfaces.md`, and the exact code/tests named by
the implementation task. Use the decision-anchor layer only if implementation
discovers pressure for new contracts, authority, sync semantics, or durable
state.

## Completion

NW-059 is accepted with 13 focused tests, 114 full mobile tests, and clean
touched-file analysis. NW-060 is accepted with 12 focused tests, 119 full
mobile tests, and clean touched-file analysis. NW-061 is accepted with 19
focused tests, 126 full mobile tests, and clean touched-file analysis. NW-062
is accepted with 10 focused tests, 131 full mobile tests, and clean
touched-file analysis. No first-deployment implementation task is currently
active; select the next bounded route from the summary lane register.
