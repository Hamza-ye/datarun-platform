---
id: idr-013
title: Assignment event payload design (DD-1)
status: active
date: 2026-04-19
phase: 2a
type: decision
touches: [contracts, server/authorization, server/sync]
superseded-by: ~
evolves: ~
commit: ~
tags: [authorization, assignment, payload, dd]
---

# Assignment event payload design (DD-1)

## Context

Phase 2 introduces `assignment_changed` events (ADR-4 S3) to bind actors to scopes. The event type was decided; the specific payloads for `assignment_created/v1` and `assignment_ended/v1` were not. Every other Phase 2 component depends on this shape.

## Decision

### `assignment_created/v1` payload

```json
{
  "target_actor": {"type": "actor", "id": "<uuid>"},
  "role": "<string>",
  "scope": {
    "geographic": "<uuid> | null",
    "subject_list": ["<uuid>", ...] | null,
    "activity": ["<string>", ...] | null
  },
  "valid_from": "<ISO-8601 datetime>",
  "valid_to": "<ISO-8601 datetime> | null"
}
```

### `assignment_ended/v1` payload

```json
{
  "reason": "<string> | null"
}
```

### Key choices

1. **No `assignment_id` in payload.** The assignment's identity is `subject_ref.id` on the envelope (`subject_ref: {type: "assignment", id: <uuid>}`). An `assignment_ended` event targets the same assignment by sharing `subject_ref.id`. No redundant identifier needed.

2. **Single geographic UUID per assignment**, not an array. Non-contiguous areas require separate assignments. This gives each assignment a clean lifecycle — end one, keep the other. Scope across assignments composes with OR.

3. **No `ended_by` in `assignment_ended` payload.** The `actor_ref` on the envelope already records who ended the assignment (the coordinator). No redundant field.

4. **`role` is an opaque string** in Phase 2. Deployer-defined vocabulary validated against configuration in Phase 3. Phase 2 stores and passes through; no role-action logic.

5. **`valid_to: null`** means indefinite — assignment active until explicitly ended.

6. **AND composition within assignment**: all non-null scope dimensions must pass for an event to be in scope. **OR across assignments**: an event is authorized if ANY active assignment passes.

### Sync rule E9

Assignment events targeting the pulling actor must ALWAYS be included in sync, regardless of geographic scope. The device PE needs them to know its own scope. These are identified by `payload->target_actor->id = actor_id`.

### Flag detection ordering

`temporal_authority_expired` must be checked BEFORE `scope_violation`. If an assignment expired and the actor pushed afterward (not knowing about the expiry), the flag is `temporal_authority_expired` (auto_eligible), not `scope_violation` (manual_only). Checking scope first would mis-classify as scope_violation because the expired assignment is no longer active.

### Temporal authority detection

For `valid_to` expiry: the trigger is that the assignment is expired NOW and the actor still pushed. No `timestamp` envelope field is used — `timestamp` is advisory (ADR-2 S3), drives no correctness. The flag is always `auto_eligible` because expiry was pre-declared in the assignment.

## Alternatives Rejected

- **`assignment_id` in payload** — redundant with envelope `subject_ref.id`. Violates DRY. Creates ambiguity about which is authoritative.
- **Array of geographic UUIDs per assignment** — compound geographic scope complicates lifecycle (how to end coverage of one area?). Separate assignments are simpler and compose naturally.
- **Compound assignments (AND across areas)** — wrong composition. Multiple areas for one actor should be OR (authorized for any of them), which separate assignments give for free.

## Consequences

- Every downstream component (Scope Resolver, CD, sync query) uses `subject_ref.id` as assignment identity
- PE reconstructs active assignments: all `assignment_created` events minus those with a corresponding `assignment_ended` sharing `subject_ref.id`, filtered by temporal bounds
- Flag detection pipeline has a fixed ordering: temporal → scope → role

## Traces

- ADR: adr-003 (S1, S3, S7, S9), adr-004 (S3, S7)
- Constraint: C6 (PE→SR), cross-cutting flag catalog
- Files: contracts/shapes/assignment_created.schema.json, contracts/shapes/assignment_ended.schema.json
