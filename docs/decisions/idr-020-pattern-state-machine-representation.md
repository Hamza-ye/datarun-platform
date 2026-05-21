---
id: idr-020
title: Pattern state machine representation
status: active
date: 2026-05-21
phase: 4-prep
type: decision
reversal-cost: high
touches: [server/config, server/integrity, server/projection, mobile/data, mobile/domain, contracts]
superseded-by: ~
evolves: ADR-005 S4-S6
commit: ~
tags: [workflow, pattern-registry, projection, conflict, phase-4]
---

# Pattern State Machine Representation

## Context

Phase 4.0 was rolled back because its first draft mixed role-action enforcement with pattern state machines and drifted from the already-decided ADR-005 model. ADR-005 requires state to be projection-derived, pattern definitions to be platform-fixed, transitions to be flagged rather than rejected, and deployers to parameterize rather than author state machines. Scenario 06 adds one important guardrail: `entity_lifecycle` cannot be silently approximated with `ongoing_resolution`, because updates after verification are normal registry behavior, not violations.

## Decision

Pattern definitions are platform-bundled state-machine specs. Deployer config binds an activity to those specs; it does not define states, transitions, or transition effects.

The config package keeps the existing `activities[name].pattern` slot from IDR-019, but the value becomes a pattern-binding set object:

```json
{
  "subject": {
    "ref": "ongoing_resolution/v1",
    "composition": "subject",
    "shape_roles": {
      "opening": ["malaria_episode_opening/v1"],
      "interaction": ["malaria_follow_up/v1", "malaria_follow_up/v2"],
      "resolution": ["malaria_episode_outcome/v1"],
      "closure_review": ["malaria_closure_review/v1"]
    },
    "participant_roles": {
      "assigned_worker": ["chv"],
      "supervisor": ["supervisor"]
    },
    "parameters": {
      "follow_up_interval": "P7D"
    }
  },
  "event": [
    {
      "ref": "capture_with_review/v1",
      "composition": "event",
      "shape_roles": {
        "review_decision": ["follow_up_review/v1"]
      },
      "activation_roles": {
        "on_shapes": ["malaria_follow_up/v1", "malaria_follow_up/v2"]
      },
      "participant_roles": {
        "capturer": ["chv"],
        "reviewer": ["supervisor"]
      },
      "parameters": {}
    }
  ]
}
```

`subject` is nullable and may contain at most one subject-level binding. `event` is an array of event-level bindings. Each binding's `ref` names a platform-bundled pattern definition. The `/vN` suffix versions the definition, not the envelope or event payload. Pattern definitions use the transition notation already fixed in `docs/architecture/patterns.md`: `(current_state, event.type, shape_role) -> next_state`, with `SC*`, `SC`, and `SP` transition effects.

`shape_roles` values are arrays, not single strings. A role can include multiple shape versions so old offline work under `facility/v1` and new work under `facility/v2` can both remain projectable under the same role. New capture can be restricted by the activity's current `shapes` list; projection must still recognize deprecated shape versions that remain in the role binding.

`composition` is copied from the pattern definition and validated for consistency. Subject-level state identity is derived from `(subject_ref, activity_ref, binding.ref)`. Event-level state identity is derived from `(source_event_id, binding.ref)`. The `binding.ref` value is the platform pattern definition reference inside the activity config; it is not an event envelope field, payload field, or stored event authority. For a concrete event, the applicable binding is resolved from `activity_ref`, `shape_ref`, and the activity's pattern binding set. Phase 4 must not use `subject_ref.type = "process"` for pattern instances; `process` remains reserved under ADR-008.

Runtime state is derived from events plus the pattern binding. Events never store `current_state`, and no envelope field is added. The first Phase 4 implementation should derive state on demand or through rebuildable in-process structures. A durable workflow-state projection table remains an ADR-001 B->C optimization escape hatch only after measured read cost justifies it.

Transition evaluation is accept-and-flag. A structurally valid event is stored even when no transition matches the current derived state. The conflict detector emits a `transition_violation` as `type = alert`, `shape_ref = conflict_detected/v1`, with `flag_category = transition_violation`. Events with unresolved flags are excluded from state derivation and remain visible in the timeline, matching ADR-005 S2 and the existing projection discipline.

`ongoing_resolution` is the active platform name for the behavior called `case_management` in ADR-005 exploration examples: following a subject over multiple interactions until resolution, with optional referral, transfer, closure review, and reopening. The platform name is deliberately domain-neutral; deployer shape names can still use domain terms such as `malaria_episode_opening/v1`.

`entity_lifecycle` remains a separate pattern candidate. Any Phase 4 scope that claims Scenario 06 support must implement it or explicitly defer S06. It must not map S06 to `ongoing_resolution`.

## Alternatives Rejected

- **Store state in events** - violates ADR-005 S4 and reopens the rejected `status_changed` path. State belongs to projection, not payload or envelope.
- **Let deployers author transition tables** - turns L0 assembly into an inner platform and violates ADR-005 S5. Deployers bind shapes, roles, and parameters only.
- **Represent pattern bindings as a string only** - cannot express shape-role mapping, role mapping, event-level overlays, feature gates, or multi-version shape support.
- **Use `process` identities for workflow instances now** - ADR-008 reserves `process`; Phase 4 pattern instances already have sufficient keys without activating a new identity class.
- **Treat `entity_lifecycle` as `ongoing_resolution`** - would create false-positive `transition_violation` flags for normal S06 updates after verification.
- **Create a durable workflow-state table first** - premature storage authority. The B->C escape hatch stays available if benchmarks prove replay cost is material.

## Phase 4 Quality Gates

- S06 registry gate: with `entity_lifecycle`, `verified` + `update` projects to `active` and produces no `transition_violation`.
- Ongoing-resolution contrast gate: with `ongoing_resolution`, `closed` or `resolved` + ordinary `interaction` is accepted, flagged with `transition_violation`, and excluded from state until resolution.
- Shape-evolution gate: a deprecated-but-known shape version bound to the same `shape_role` remains valid for projection when offline work arrives after a newer shape version is active.
- Composition gate: deploy-time validation rejects two subject-level patterns in one activity, duplicate transition-bound shape ownership, missing required shape roles, and missing required participant roles.
- Flag exclusion gate: unresolved flagged events do not advance `current_state`; resolving the flag as accepted re-derives state including the event.
- Vocabulary gate: Phase 4 introduces no new envelope `type`; transition flags use `type=alert`, `shape_ref=conflict_detected/v1`.
- Identity gate: pattern state identity uses `(subject_ref, activity_ref, binding.ref)` or `(source_event_id, binding.ref)`, never an event-carried `pattern_ref` and never `subject_ref.type = "process"`.

## Consequences

- IDR-020 no longer blocks Phase 4 planning; IDR-021 and IDR-022 remain separate blockers for role-action enforcement and flag severity/domain uniqueness.
- The Pattern Registry implementation can start as a small platform-bundled registry plus a generic transition matcher.
- The mobile and server projection engines must share fixtures for at least the S06 and ongoing-resolution contrast gates before Phase 4 is declared complete.
- If an early deployment needs registry lifecycle behavior, Phase 4 must promote `entity_lifecycle` into the implemented inventory instead of relying on `ongoing_resolution`.

## Traces

- ADR: [ADR-005 S1-S6](../adrs/adr-005-state-progression.md), [ADR-007](../adrs/adr-007-envelope-type-closure.md), [ADR-008](../adrs/adr-008-reference-field-canonicalization.md)
- Constraint: `docs/architecture/patterns.md` section 1, section 4, section 7; `docs/exploration/28-pattern-inventory-walkthrough.md` section 8
- Scenario: [S06 Entity Registry Lifecycle](../scenarios/06-entity-registry-lifecycle.md)
- Files: `docs/architecture/patterns.md`, `docs/implementation/phases/phase-3d.md`, `docs/status.md`
