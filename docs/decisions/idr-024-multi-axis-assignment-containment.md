---
id: idr-024
title: Multi-axis assignment containment
status: active
date: 2026-05-22
phase: 4-prep
type: decision
reversal-cost: high
touches: [server/authorization, server/integrity, server/sync, contracts]
superseded-by: ~
evolves: ADR-003 S1/S5, ADR-004 S7, ADR-009 S2, IDR-013, IDR-023
commit: ~
tags: [authorization, assignment, scope, phase-4]
---

# Multi-axis Assignment Containment

## Context

ADR-003 S5 requires `new_assignment.scope <= creating_actor.assignment.scope`, but the current assignment command path only validates geographic containment. IDR-013 already includes all three platform-fixed scope axes in `assignment_created/v1`: `geographic`, `subject_list`, and `activity`. ADR-004 S7 and ADR-009 S2 make those axes a closed platform primitive with AND composition across non-null dimensions and `null` meaning unrestricted on that axis.

The implementation gap is a real privilege-escalation risk. A creator restricted by subject list or activity can currently create a broader assignment on the ignored axes, and a subject-list-only assignment with `geographic = null` can be misread as root-like when the containment check looks only at geography. IDR-023 correctly keeps assignment lifecycle commands out of `activities[*].roles`, so this hardening belongs in the assignment-administration command path, not in activity role-action enforcement.

## Decision

Assignment creation containment applies across every platform-fixed scope axis: `geographic`, `subject_list`, and `activity`. A new assignment is authorized only when at least one active creator assignment contains the requested new assignment scope on all three axes, or when the command is executed through an explicit bootstrap/root authority path.

Containment is evaluated per covering creator assignment. Implementations must not satisfy one requested axis from one creator assignment and another requested axis from a different creator assignment unless a future decision explicitly introduces delegated union semantics for assignment administration.

Axis containment rules:

- **Geographic**: a requested non-null geographic scope must be within the covering creator assignment's geographic scope. A requested `geographic = null` is broader than any restricted geographic scope and requires the covering creator assignment to also have `geographic = null`, or explicit bootstrap/root authority.
- **Subject list**: a requested non-null subject list is contained when the covering creator assignment has `subject_list = null` or the requested subject IDs are a subset of the covering creator assignment's subject list. A requested `subject_list = null` is unrestricted on the subject-list axis and requires the covering creator assignment to also have `subject_list = null`, or explicit bootstrap/root authority.
- **Activity**: a requested non-null activity list is contained when the covering creator assignment has `activity = null` or the requested activity refs are a subset of the covering creator assignment's activity list. A requested `activity = null` is unrestricted on the activity axis and requires the covering creator assignment to also have `activity = null`, or explicit bootstrap/root authority.

`null` remains the only representation of unrestricted scope on an axis. Empty arrays are not unrestricted. Command validation should reject empty `subject_list` and `activity` arrays, or otherwise treat them as granting no values, but must not interpret them as equivalent to `null`.

Subject-list-only assignments do not imply root or administrator authority. A creator assignment with `geographic = null` and a non-null `subject_list` is unrestricted only on geography; it remains restricted on subjects and cannot create a subject-unrestricted assignment unless explicit bootstrap/root authority is present. The same rule applies to activity-only or mixed-axis assignments.

Bootstrap/root authority must be explicit. A creator having no active assignments is not, by itself, production authority to create arbitrary assignments. The only acceptable bypasses are:

- an initial bootstrap/provisioning path that is explicitly identified by the command surface and restricted to initial deployment or controlled provisioning; or
- a root/platform authority represented outside ordinary scoped assignments by a dedicated system actor, provisioning token, or successor assignment-administration authority model.

If the implementation keeps a temporary "no assignments yet" bootstrap escape hatch, it must be bounded to initial provisioning state, not to the requesting actor's personal absence of assignments. The check must distinguish "the system has no assignments/provisioning root yet" from "this actor currently has no assignments."

### Activity-null Authorization

Activity remains optional in the event envelope. This decision does not make `activity_ref` mandatory.

For ordinary actor-authored work events whose envelope type is one of the activity work actions (`capture`, `review`, `alert`, `task_created`, `task_completed`), an activity-restricted assignment does not authorize `activity_ref = null`. A null-activity work event is authorized only by a covering assignment whose activity axis is unrestricted (`activity = null`) or by an explicitly decided import/baseline/system rule.

Platform/system and identity/assignment events are classified separately from ordinary activity work:

- `assignment_changed` lifecycle events remain online assignment-administration commands and are not authorized through `activities[*].roles`.
- Identity and integrity events such as `subjects_merged`, `subject_split`, `conflict_detected`, and `conflict_resolved` shapes are platform-managed or server-validated paths governed by their own decisions.
- Imported or baseline data with unknown activity provenance may remain `activity_ref = null`, but any authority rule that admits it under activity-restricted actors requires a separate import/baseline decision.

`ActiveAssignment.containsActivity(null)` therefore must not return true for activity-restricted assignments when evaluating ordinary work-event authority or sync containment. The implementation may expose separate helpers for "ordinary work event" versus "platform/system event" classification, but it must not use null activity as a wildcard under a restricted activity list.

### Assignment Ending

Ending an assignment is also assignment administration. It cannot be authorized merely because the request contains an actor ID or because the actor can authenticate.

The actor ending an assignment must have target-assignment authority: authority over the assignment being ended, evaluated against the target assignment's scope and lifecycle, or explicit bootstrap/root authority. A future implementation may define exact end-assignment policy details, but it must at minimum prevent an actor with a narrower or disjoint assignment from ending broader or unrelated assignments.

## Alternatives Rejected

- **Keep geography-only containment** - contradicts ADR-004 S7 and IDR-013, and lets subject-list or activity-restricted creators mint broader assignments on ignored axes.
- **Treat subject-list-only assignments as root-like because geography is null** - collapses AND-composed scope into a single geography axis and turns one unrestricted axis into global authority.
- **Use `activities[*].roles` to authorize assignment administration** - rejected by IDR-023. Assignment lifecycle commands are authority administration, not activity-scoped work.
- **Make activity mandatory to avoid null semantics** - contradicts ADR-004 S2 and IDR-013. Null activity remains valid, but restricted activity assignments do not authorize ordinary null-activity work by default.
- **Let separate creator assignments satisfy separate axes** - creates implicit union/privilege-composition semantics for assignment administration. If that is ever desired, it needs an explicit decision and tests.
- **Treat "creator has no assignments" as general authority** - acceptable only as a tightly bounded initial bootstrap condition, not as a production authorization rule.

## Phase 4 Quality Gates

- Assignment creation rejects a requested `geographic = null` when the covering creator assignment is geographically restricted and no explicit bootstrap/root authority is present.
- Assignment creation rejects a requested `subject_list = null` when the covering creator assignment has a non-null subject list and no explicit bootstrap/root authority is present.
- Assignment creation rejects requested subject IDs outside the covering creator assignment's restricted subject list.
- Assignment creation rejects a requested `activity = null` when the covering creator assignment has a non-null activity list and no explicit bootstrap/root authority is present.
- Assignment creation rejects requested activity refs outside the covering creator assignment's restricted activity list.
- A subject-list-only creator assignment does not create subject-unrestricted or unrelated-subject assignments merely because its geographic axis is null.
- Bootstrap/root tests prove that initial provisioning is explicit and bounded; an arbitrary actor with no active assignments cannot create broad production authority.
- Ordinary work events with `activity_ref = null` are not authorized by activity-restricted assignments.
- Assignment lifecycle commands remain outside `activities[*].roles`; `assignment_changed` stays invalid in activity role-action config.
- Ending an assignment requires target-assignment authority or explicit bootstrap/root authority.

## Consequences

- ADR-003 S5 is clarified for all three scope axes without adding envelope fields, event types, or a new role-action capability.
- The implementation pass must harden `AssignmentService.createAssignment`, `AssignmentService.endAssignment`, and any shared `ActiveAssignment`/sync/activity containment helpers that currently treat null activity or empty lists too broadly.
- Sync and authorization checks must keep ordinary work-event activity semantics distinct from platform/system/identity/assignment events.
- IDR-021 and IDR-023 remain intact: role-action enforcement covers activity work actions only, and assignment administration stays a separate online authority path.

## Traces

- ADR: [ADR-003 S1/S5](../adrs/adr-003-authorization-sync.md), [ADR-004 S2/S7](../adrs/adr-004-configuration-boundary.md), [ADR-009 S2](../adrs/adr-009-platform-fixed-vs-deployer-configured.md)
- IDR: [IDR-013](idr-013-assignment-payload.md), [IDR-021](idr-021-role-action-enforcement-model.md), [IDR-023](idr-023-role-action-domain-boundary-and-assignment-administration.md)
- Register: [FP-007](../flagged-positions.md)
- Files: `contracts/shapes/assignment_created.schema.json`, `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`, `server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java`, `server/src/main/java/dev/datarun/server/sync/SyncController.java`
