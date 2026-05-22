---
id: idr-021
title: Role-action enforcement model
status: active
date: 2026-05-22
phase: 4-prep
type: decision
reversal-cost: high
touches: [server/config, server/integrity, server/authorization, mobile/data, mobile/domain, contracts]
superseded-by: ~
evolves: ADR-003 S1/S3/S7, ADR-004 S9/S14
commit: ~
tags: [authorization, role-action, configuration, conflict, phase-4]
---

# Role-Action Enforcement Model

## Context

ADR-003 defines access as assignment scope plus role permission, but Phase 2 only implemented scope filtering and conservative authorization flags. ADR-004 places role-action mappings at Layer 0 as deployment configuration, and IDR-019 already carries activity `roles` through the config package. Phase 4.0 was rolled back because it mixed role-action enforcement with pattern state machines; IDR-020 separated pattern state, leaving role-action permission semantics as the remaining IDR-021 decision.

FP-001 is resolved: `role_stale` is now derived from the assignment event timeline at the device knowledge horizon, not from a cache or envelope field. FP-005 is routed out of this IDR: live sync remains request-time scoped; subject-history backfill is separate `ongoing_resolution` work; audit/historical pull is not part of Phase 4 live sync.

## Decision

Role-action permissions are activity-scoped Layer 0 configuration. The canonical config shape is the existing activity `roles` object:

```json
{
  "activities": {
    "facility_monitoring": {
      "roles": {
        "field_worker": ["capture"],
        "supervisor": ["capture", "review"]
      }
    }
  }
}
```

Each key is an assignment role string. Each value is a list of permitted activity work actions. IDR-023 narrows the Phase 4 activity role-action vocabulary to five envelope `type` values: `capture`, `review`, `alert`, `task_created`, and `task_completed`. `assignment_changed` is intentionally excluded because assignment lifecycle commands are authority administration, not activity-scoped work. This IDR does not add envelope fields, event types, assignment payload fields, or a separate capability primitive. Finer action vocabularies, such as shape-role-specific actions, remain a later extension only after Phase 4 proves the coarse structural action model insufficient.

For offline-pushed work events, evaluation is a server-side Conflict Detector pass after temporal and scope checks. For each accepted non-system, non-assignment event, the server reconstructs the actor's assignment roles at the event's effective knowledge horizon using the assignment event timeline, then compares the attempted action against both:

1. **horizon authority** — at least one assignment covering the event at `min(event.sync_watermark, push.last_pull_watermark)` granted a role whose configured action list contained the event's `type`;
2. **current authority** — at least one current assignment covering the event still grants a role whose configured action list contains the event's `type`.

If both checks pass, the role-action check passes. If either check fails, the event is accepted and flagged as `role_stale`. The `role_stale` category therefore remains the Phase 4 authorization-role anomaly category rather than claiming a new flag slot; its payload should distinguish reasons such as `action_not_permitted_at_horizon` and `action_no_longer_permitted`.

The device uses the same config for advisory UX only: hide or warn on unavailable forms/actions where possible, but never treat local role-action checks as authoritative. A rooted device or stale config can bypass local checks; the server pass is the correctness boundary.

`role_stale` narrows from "role label changed" to "role/action authority mismatch affects the attempted action." A role change from `field_worker` to `supervisor` does not create `role_stale` for an old `capture` event if both the horizon role and current role permit `capture`. A role change to a role without `capture` creates `role_stale` because the current role no longer permits the action. An event whose horizon role never permitted `review` also creates `role_stale`, even if the actor was later promoted, because the device did not have that authority when the event was created. The category remains `manual_only`, with detect-before-act inherited from ADR-003 S7 and the unified flag lifecycle.

When an actor has multiple active assignments, permissions compose by OR across covering assignments. Scope dimensions still compose exactly as IDR-013/IDR-015 decide: AND within an assignment, OR across assignments. A role attached to an assignment grants actions only inside that assignment's scope. Multiple roles are therefore allowed, but they do not grant global permission outside their own covering assignments.

Assignment lifecycle commands still emit immutable `assignment_changed` events, but IDR-023 moves those commands out of the `activities[*].roles` model. Phase 4 role-action enforcement must not infer assignment-administration authority from activity role maps, and `role_stale` must not be emitted for `assignment_changed` lifecycle events. Assignment create/end remains an online authority command path governed by ADR-003 S5 scope-containment and existing provisioning/bootstrap behavior until a separate assignment-administration authority decision replaces or hardens it.

Config package delivery remains atomic. Devices receive role-action mappings through `activities[name].roles` alongside shapes, expressions, and pattern bindings. Deploy-time validation must reject unknown action names, empty role names, empty action lists, activity roles that reference no assignment role vocabulary if such vocabulary is introduced, and pattern participant-role mappings whose mapped roles lack the structural event actions required by their transitions.

## Alternatives Rejected

- **Role-action hard-coded in platform code** — violates ADR-004's L0 configuration boundary and would require code changes for ordinary deployment policy differences.
- **Device-only enforcement** — bypassable on rooted devices and stale under offline operation. It is useful UX, not an authority boundary.
- **Reject unauthorized actions at sync** — violates accept-and-flag. A structurally valid event is accepted, flagged, and held from downstream effects until resolution.
- **Add capability fields to assignments or events now** — unnecessary stored-data expansion for the Phase 4 activity work model; ADR-003's `authority_context` escape hatch remains unused.
- **Treat any role-label change as `role_stale`** — too noisy once role-action tables exist. The relevant question is whether the actor's role at the event's knowledge horizon permitted the attempted action.
- **Authorize `assignment_changed` through activity role maps** — rejected by IDR-023. It makes optional activity globally load-bearing and leaves activity-null assignment lifecycle events without a coherent permission target.
- **Make audit/read-only access a special sync path here** — FP-005 routes audit/historical pull out of IDR-021. Read-only auditor roles can be represented as roles with no write actions, but historical reconstruction needs a separate pull class/API if implemented.

## Phase 4 Quality Gates

- Config packaging gate: an activity's `roles` map is delivered unchanged in the config package and mobile parser preserves it.
- Device advisory gate: a role lacking `review` cannot open or submit review UI under current config, but this remains advisory and does not replace server detection.
- Server detection gate: an actor whose assignment role does not permit an event's `type` can still push the event; the server accepts it and emits `role_stale`.
- Capability-narrowing gate: role label changes do not create `role_stale` when horizon and current roles both permit the event action; they do create `role_stale` when the action is not permitted at the horizon or is no longer permitted currently.
- Timeline gate: role-action evaluation uses assignment events at `min(event.sync_watermark, push.last_pull_watermark)`, matching FP-001's projection-derived authority semantics.
- Composition gate: with two assignments, a role grants an action only for events inside that assignment's scope; permissions OR across covering assignments and do not leak across scopes.
- Assignment boundary gate: `assignment_changed` is rejected in `activities[*].roles`, and assignment create/end behavior is not silently changed by the activity role-action slice.
- Boundary gate: no Phase 4 role-action work changes `/api/sync/pull` into a subject-history or audit/historical pull.

## Consequences

- IDR-021 is green for Phase 4 planning: it depends on FP-001 and explicitly excludes FP-005's remaining backfill/audit questions.
- `role_stale` implementation must be revised from "any role change" to "attempted action no longer permitted by the role-action table at the relevant authority horizon."
- Server and mobile must share role-action fixtures so advisory device behavior and authoritative server detection interpret config identically.
- IDR-023 remains separate: it narrows this model to activity work actions and leaves assignment-administration authority for a later dedicated decision if needed.
- IDR-022 remains separate: it decides flag severity authoring and `domain_uniqueness_violation`, not the role-action permission model.

## Traces

- ADR: [ADR-003 S1/S3/S5/S7](../adrs/adr-003-authorization-sync.md), [ADR-004 S7/S9/S14](../adrs/adr-004-configuration-boundary.md), [ADR-006 S1-S4](../adrs/adr-006-flag-semantics.md)
- IDR: [IDR-013](idr-013-assignment-payload.md), [IDR-015](idr-015-scope-filtered-sync-query.md), [IDR-019](idr-019-config-package.md), [IDR-020](idr-020-pattern-state-machine-representation.md), [IDR-023](idr-023-role-action-domain-boundary-and-assignment-administration.md)
- Constraint: [access-control-scenario.md](../access-control-scenario.md), [cross-cutting.md §2-3](../architecture/cross-cutting.md), [flagged-positions.md FP-001/FP-005](../flagged-positions.md)
- Files: `server/src/main/java/dev/datarun/server/config/ConfigPackager.java`, `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java`, `contracts/flag-catalog.md`
