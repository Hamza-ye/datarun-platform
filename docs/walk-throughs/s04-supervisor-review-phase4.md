# Walk-Through: S04 — Supervisor Review through Phase 4

## Purpose

Challenge the Phase 4 model against an accepted Phase 1 Core scenario from [S04: Review and Judgment by Another Person](../scenarios/04-supervisor-review.md). This walk-through checks that Phase 1-4 decisions compose for a common review flow without relying on unresolved `ongoing_resolution` backfill.

**Phase 4 decisions exercised**: [IDR-020](../decisions/idr-020-pattern-state-machine-representation.md) (`capture_with_review` event-level pattern), [IDR-021](../decisions/idr-021-role-action-enforcement-model.md) (role-action `capture`/`review` enforcement), [IDR-022](../decisions/idr-022-flag-severity-and-domain-uniqueness.md) (severity and accept-and-flag ordering).

## Scenarios Exercised

S00 (structured capture), S01 (entity-linked capture), S03 (assignment), S04 (supervisor review), S19 (offline capture and sync constraint)

---

## Context

A health program monitors facilities. A field worker records a facility observation. A supervisor must review the observation before it is treated as final for reporting and downstream workflow decisions.

### Actors

- **Field worker** — captures facility observations.
- **Supervisor** — reviews submitted observations.
- **Program manager** — configures the activity, shapes, roles, and review pattern.

### Objective

Keep field capture offline-capable while preserving a clear review trail: who captured the work, who reviewed it, what decision they made, and which items are still waiting.

---

## Configuration

### Shapes

`facility_observation/v1`:

- `subject_facility`: `subject_ref`, required
- `service_availability`: `select`, required
- `staff_present`: `integer`, required
- `needs_followup`: `boolean`, required
- `notes`: `narrative`, optional

`facility_observation_review/v1`:

- `source_event_ref`: `text`, required
- `decision`: `select`, required, values `accepted` and `returned`
- `review_note`: `narrative`, optional

The review shape references the captured event through payload, not through a new envelope field.

### Activity

```json
{
  "name": "facility_monitoring",
  "shapes": [
    "facility_observation/v1",
    "facility_observation_review/v1"
  ],
  "roles": {
    "field_worker": ["capture"],
    "supervisor": ["review"]
  },
  "pattern": {
    "subject": null,
    "event": [
      {
        "ref": "capture_with_review/v1",
        "composition": "event",
        "shape_roles": {
          "review_decision": ["facility_observation_review/v1"]
        },
        "activation_roles": {
          "on_shapes": ["facility_observation/v1"]
        },
        "participant_roles": {
          "capturer": ["field_worker"],
          "reviewer": ["supervisor"]
        },
        "parameters": {}
      }
    ]
  },
  "sensitivity": "standard"
}
```

### Validation Points

- `field_worker` has `capture` but not `review`.
- `supervisor` has `review` but not `capture` in this activity. A deployment may also grant `capture`, but this walk-through keeps the distinction sharp.
- The pattern is event-level, so it does not consume the activity's subject-level pattern slot.
- `facility_observation/v1` is activation-bound for `capture_with_review`; `facility_observation_review/v1` is transition-bound.
- No deployer-authored state machine appears in config. The deployer maps shapes and roles only.

---

## Normal Flow

### 1. Assignment and Sync

The field worker has an active assignment covering the facility's location and activity `facility_monitoring`. The supervisor has a broader assignment covering the same facility and the same activity.

Normal `/api/sync/pull` is request-time scoped. The worker receives the facility and activity config because their current assignment authorizes that work. The supervisor receives the captured event after it syncs because their current assignment covers the facility.

### 2. Offline Capture

The worker opens the `facility_monitoring` activity, selects the facility, and captures `facility_observation/v1`.

The event envelope uses:

- `type = "capture"`
- `shape_ref = "facility_observation/v1"`
- `activity_ref = "facility_monitoring"`
- `subject_ref = { "type": "subject", "id": facility_id }`

The device may show advisory review status after local projection, but it does not create authoritative flags.

### 3. Server Push

On push, the server:

1. validates envelope and shape structure before persistence;
2. persists the structurally valid event;
3. runs identity/lifecycle checks;
4. runs authorization checks, including role-action:
   - horizon authority: the worker's covering assignment at `min(event.sync_watermark, push.last_pull_watermark)` allowed `capture`;
   - current authority: the worker's current covering assignment still allows `capture`;
5. runs domain uniqueness checks if the shape declares any;
6. runs pattern transition checks.

The `capture_with_review` activation creates an event-level projection instance keyed by the captured event ID and `capture_with_review/v1`. The initial state is `pending_review`.

No state is written into the event.

### 4. Supervisor Review

The supervisor pulls the captured event, sees it as `pending_review`, and submits `facility_observation_review/v1` with:

- `type = "review"`
- `shape_ref = "facility_observation_review/v1"`
- `activity_ref = "facility_monitoring"`
- payload `source_event_ref = captured_event_id`
- payload `decision = "accepted"` or `"returned"`

The server accepts the review if the supervisor's covering assignment permits `review`. The event-level projection moves:

- `pending_review -> accepted` when decision is `accepted`;
- `pending_review -> returned` when decision is `returned`.

The timeline still contains both the capture and review events. Reporting can treat accepted captures as final and returned captures as needing correction.

---

## Failure and Edge Cases

### Unauthorized Review Attempt

A field worker tries to submit `facility_observation_review/v1`.

Expected behavior:

- The event is structurally valid and is persisted.
- Role-action evaluation finds that neither horizon nor current covering authority permits `review`.
- The server emits `role_stale` as `type = "alert"`, `shape_ref = "conflict_detected/v1"`.
- The review event remains visible in the timeline.
- The review event is excluded from authoritative review-state derivation while unresolved, so the captured observation remains `pending_review`.

This verifies IDR-021's narrowing: the flag is about action authority for `review`, not merely about a changed role label.

### Duplicate or Late Review

Two supervisors review the same capture offline. The first synced review moves the event-level state from `pending_review` to `accepted`. The second review syncs later.

Expected behavior:

- The second review is accepted and persisted.
- Pattern transition detection evaluates current state before applying the incoming review.
- No valid transition exists from `accepted` through another `review_decision`.
- The server emits `transition_violation`.
- The second review is excluded from authoritative review-state derivation while unresolved.
- The timeline surfaces the anomaly for resolution instead of silently overwriting the first review.

This is an event-level state conflict, not a subject-level workflow conflict.

### Returned and Corrected Work

If the supervisor returns the observation, the original review cycle reaches `returned`. The worker submits a corrected observation as a new `capture` event. That new capture starts its own `capture_with_review` instance and enters `pending_review`.

Expected behavior:

- The original capture remains in the record with review outcome `returned`.
- The corrected capture has its own review state.
- No new envelope field or `status_changed` event is needed.

### Severity Override

If the deployment sets:

```json
{
  "flag_severity_overrides": {
    "transition_violation": "blocking",
    "role_stale": "blocking"
  }
}
```

the unresolved flagged review events are still excluded from authoritative projection because unresolved flagged events are category-agnostically excluded. Severity affects operational gating and queue priority; it does not change resolvability or projection truth.

### FP-005 Boundary

This walk-through does not require subject-history backfill:

- `capture_with_review` is event-level.
- The review instance is keyed by the captured event.
- A supervisor who is currently assigned receives new in-scope events through normal request-time scoped live pull.

The flow must not be used as precedent for `ongoing_resolution`. Long-running subject state still requires the FP-005 backfill decision before implementation.

Once `ongoing_resolution` is implemented, its events remain reviewable in the same way. The subject-level `ongoing_resolution` binding owns the lifecycle state of the subject, while an event-level `capture_with_review` overlay can own review state for individual opening, interaction, resolution, or reopening events. Reviewability does not remove the FP-005 backfill prerequisite and must not be folded into the subject-level state machine.

---

## Acceptance Gates

- A valid field-worker capture activates `capture_with_review` and projects to `pending_review`.
- A valid supervisor review moves the event-level state to `accepted` or `returned`.
- A worker-authored review is accepted, flagged as `role_stale`, and excluded from review-state derivation.
- A second review after a terminal review outcome is accepted, flagged as `transition_violation`, and excluded from review-state derivation.
- No new envelope field, new envelope type, event-carried `pattern_ref`, or `subject_ref.type = "process"` is used.
- Device role-action and transition checks are advisory only; server detection remains authoritative.
- Normal live sync remains request-time scoped and is not changed into subject-history or audit pull.
- The same review overlay can be applied to events inside a subject-level pattern, including `ongoing_resolution` after FP-005 is closed.

## Verdict

S04 is a good first Phase 4 candidate. It exercises role-action enforcement, event-level pattern projection, transition detection, severity behavior, flag exclusion, and offline accept-and-flag without depending on unresolved `ongoing_resolution` backfill.

Recommended first implementation fixture: `facility_monitoring` with `facility_observation/v1`, `facility_observation_review/v1`, `field_worker`, `supervisor`, and `capture_with_review/v1`.
