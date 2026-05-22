---
id: idr-023
title: Role-action domain boundary and assignment administration
status: active
date: 2026-05-22
phase: 4-prep
type: decision
reversal-cost: medium
touches: [server/config, server/integrity, server/authorization, mobile/data, mobile/domain, contracts]
superseded-by: ~
evolves: IDR-021, ADR-003 S5, ADR-004 S9/S14
commit: ~
tags: [authorization, role-action, assignment, configuration, phase-4]
---

# Role-Action Domain Boundary and Assignment Administration

## Context

IDR-021 placed activity role-action mappings in `activities[*].roles` and initially treated the activity action vocabulary as the full six-value envelope `type` vocabulary. The first Phase 4 server role-action slice implemented the offline work-event path cleanly, but the online assignment lifecycle path exposed an ambiguity: assignment events use `type = "assignment_changed"` and usually `activity_ref = null`, while `activities[*].roles` is explicitly activity-scoped Layer 0 configuration.

Activity has always been optional at the envelope/model level. Making assignment administration depend on activity config would quietly turn activity into a universal authorization anchor and would make unrestricted or activity-null assignments impossible to reason about without invented policy. Assignment lifecycle commands are authority administration, not ordinary activity work.

## Decision

Activity role-action mappings apply only to activity-scoped work actions. The Phase 4 activity role-action vocabulary is:

- `capture`;
- `review`;
- `alert`;
- `task_created`;
- `task_completed`.

`assignment_changed` is not a valid action inside `activities[*].roles`. Deploy-time validation must reject activity role maps that include it.

Server role-action detection remains the IDR-021 offline work-event path: structurally valid work events are accepted, then flagged as `role_stale` when horizon or current covering assignment authority does not permit the event's activity action. `role_stale` does not apply to `assignment_changed` lifecycle events. Assignment lifecycle events are still envelope-type-keyed authority facts used by scope and assignment projections.

Assignment lifecycle commands remain online commands that append immutable `assignment_changed` events. They are not offline accept-and-flag work events, and they are not authorized through `activities[*].roles` in Phase 4. Existing ADR-003 S5 scope-containment and existing provisioning/bootstrap behavior remain the current implementation boundary. A future assignment-administration authority model may replace or harden that boundary, but it must be a separate decision and must not make activity mandatory as the root of all authorization.

Pattern projection may still consume `assignment_changed` events where platform-bundled patterns need transfer or responsibility facts. That consumption does not imply that `assignment_changed` belongs in activity role-action config. Pattern binding validation may check activity role-action prerequisites for activity work transitions, but assignment lifecycle transitions need a distinct assignment-administration rule before they become command-authoring gates.

## Alternatives Rejected

- **Keep `assignment_changed` in `activities[*].roles`** — makes optional activity load-bearing for assignment administration and leaves `activity_ref = null` assignment events without a natural permission target.
- **Require every actor-authored assignment to name activities** — narrows the data model to fit the role-action implementation instead of preserving the existing assignment model.
- **Treat `activityList == null` as every configured activity** — brittle; adding an activity would silently change who can administer broad assignments.
- **Reject assignment lifecycle commands through accept-and-flag** — wrong domain. Assignment create/end is an online authority command path, not offline user work.
- **Add a general capability primitive now** — too broad for the current Phase 4 slice. If assignment administration needs richer policy, it should get its own decision and tests.

## Phase 4 Quality Gates

- Activity role validation rejects `assignment_changed`.
- Existing activity work actions continue to validate and package unchanged.
- Existing pushed-work `role_stale` tests for `capture` and `review` remain green.
- Assignment lifecycle create/end behavior is not silently changed by the Phase 4 activity role-action slice.
- Pattern binding validation does not require `activities[*].roles` to authorize `assignment_changed` transitions.

## Consequences

- IDR-021 is amended: activity role-action is the work-action model, not the assignment-administration model.
- Phase 4 can continue role-action, severity, uniqueness, and pattern work without turning activity into a global authorization primitive.
- A later assignment-administration authority decision remains possible. It should explicitly address provisioning/bootstrap, null activity scope, end-assignment target authority, and how administration roles are configured.

## Traces

- IDR: [IDR-021](idr-021-role-action-enforcement-model.md), [IDR-013](idr-013-assignment-payload.md), [IDR-015](idr-015-scope-filtered-sync-query.md), [IDR-020](idr-020-pattern-state-machine-representation.md)
- ADR: [ADR-003 S5](../adrs/adr-003-authorization-sync.md), [ADR-004 S9/S14](../adrs/adr-004-configuration-boundary.md)
- Files: `server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java`, `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java`, `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`
