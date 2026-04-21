# Walk-Through: S01 — Facility Observation through Phase 3 Config Pipeline

## Purpose

Verify the Phase 3 investigation brief end-to-end against a real scenario (S01: entity-linked capture). Traces a concrete `facility_observation/v1` instance through every config pipeline stage — from deployer authoring through device capture to multi-version projection.

**IDRs fed by this walk-through**: [IDR-017](../decisions/idr-017-shape-storage.md) (shape storage), [IDR-018](../decisions/idr-018-expression-grammar.md) (expression grammar), [IDR-019](../decisions/idr-019-config-package.md) (config package). All three Lock DDs resolved directly from findings here.

## Scenarios Exercised

S01 (entity-linked capture), S04 (supervisor review — comment field), S05 (audit checklists — multi_select reference)

---

## Context

A health program monitors facilities (clinics, health posts) through periodic observation visits. A field worker visits a known facility, observes conditions, and records structured findings linked to that facility. Over time, the program sees a history of observations per facility.

### Actors

- **Program manager** (L0 — activity assembly)
- **Configuration specialist** (L1 — shape authoring, L2 — expression logic)
- **Field worker** (capture)
- **Supervisor** (review)

---

## Stage 1: Deployer Authors a Shape

The configuration specialist creates `facility_observation/v1`:

```json
{
  "name": "facility_observation",
  "version": 1,
  "status": "active",
  "sensitivity": "standard",
  "uniqueness": null,
  "fields": [
    {
      "name": "subject_facility",
      "type": "subject_ref",
      "required": true,
      "description": "The facility being observed"
    },
    {
      "name": "service_availability",
      "type": "select",
      "required": true,
      "options": ["full", "partial", "closed"],
      "description": "Overall service availability at time of visit"
    },
    {
      "name": "staff_present",
      "type": "integer",
      "required": true,
      "validation": { "min": 0, "max": 200 }
    },
    {
      "name": "stockout_items",
      "type": "multi_select",
      "required": false,
      "options": ["vaccines", "antimalarials", "ors", "antibiotics", "gloves", "none"],
      "description": "Items currently out of stock"
    },
    {
      "name": "needs_followup",
      "type": "boolean",
      "required": true
    },
    {
      "name": "followup_notes",
      "type": "narrative",
      "required": false,
      "description": "Details on why follow-up is needed"
    }
  ]
}
```

### Walk-through observations

- **`subject_ref`** type is mandatory for S01 — without it, the capture can't link to a known facility. The form engine must present a subject picker (filtered to facilities in the worker's scope).
- **`select`** with 3 options — straightforward single selection.
- **`integer`** (not decimal) — staff count is whole numbers. L1 validation: `min: 0, max: 200`.
- **`multi_select`** — stockout items where multiple can be selected simultaneously. Distinct from `select`: form renders checkboxes not radio buttons, payload stores array.
- **`boolean`** — binary flag.
- **`narrative`** — free text multiline for follow-up notes. The field invites a show/hide expression (only show when `needs_followup = true`).

**Field count**: 6 fields. Well within 60-field budget.

**Field type coverage exercised**: `subject_ref`, `select`, `integer`, `multi_select`, `boolean`, `narrative`. Missing from this shape but tested elsewhere: `text`, `decimal`, `date`, `location`. The vocabulary handles this scenario.

---

## Stage 2: Deployer Creates an Activity

The program manager assembles activity `facility_monitoring`:

```json
{
  "name": "facility_monitoring",
  "shapes": ["facility_observation/v1"],
  "roles": {
    "field_worker": ["capture"],
    "supervisor": ["capture", "review"]
  },
  "pattern": null,
  "sensitivity": "standard"
}
```

### Walk-through observations

- Activity binds one shape. Multiple shapes per activity is supported but not needed here.
- Role-action mapping: field workers capture, supervisors both capture and review.
- `pattern: null` — no state machine (Phase 4 stub).
- **Key question tested**: Can the same shape (`facility_observation/v1`) be reused in a different activity (e.g., `emergency_assessment`) with different expression logic? If expressions are external to the shape and bound at the activity level → **yes**. If expressions are inline in the shape → both activities get identical show/hide logic. This is the strongest evidence for L1/L2 separation.

---

## Stage 3: Deployer Adds L2 Expressions

The configuration specialist writes three expressions for the `facility_monitoring` activity using `facility_observation/v1`:

### Expression 1: Show/hide using `entity.*`

```json
{
  "shape_ref": "facility_observation/v1",
  "field": "stockout_items",
  "activity_ref": "facility_monitoring",
  "type": "show_condition",
  "condition": {
    "neq": ["entity.facility_type", "community_health_post"]
  }
}
```

**Meaning**: Hide the stockout checklist for community health posts (they don't manage stock). Show it for clinics and hospitals.

**Reference resolution**: `entity.facility_type` is a projected attribute of the subject (the facility). PE pre-resolves this at form-open. The evaluator reads a pre-computed value — it doesn't query.

### Expression 2: Computed default using `context.*`

```json
{
  "shape_ref": "facility_observation/v1",
  "field": "needs_followup",
  "activity_ref": "facility_monitoring",
  "type": "default",
  "value": {
    "gt": ["context.days_since_last_event", 90]
  }
}
```

**Meaning**: If no observation has been recorded for this facility in >90 days, default `needs_followup` to `true`.

**Reference resolution**: `context.days_since_last_event` is one of the 7 pre-resolved `context.*` properties (ADR-5 S8). Computed by PE from the event stream for this subject+activity.

**Output type**: This is a value expression producing a boolean (the result of the comparison becomes the default value for a boolean field). The evaluator computes the comparison and outputs a typed value.

### Expression 3: Warning using `payload.*`

```json
{
  "shape_ref": "facility_observation/v1",
  "field": "staff_present",
  "activity_ref": "facility_monitoring",
  "type": "warning",
  "condition": {
    "and": [
      { "eq": ["payload.service_availability", "full"] },
      { "lt": ["payload.staff_present", 3] }
    ]
  },
  "message": "Full service with fewer than 3 staff — please verify"
}
```

**Meaning**: Warn if the worker reports full service availability but very few staff present — likely an error.

**Reference resolution**: Both references are `payload.*` — they refer to other fields in the same form during editing. No pre-resolution needed; evaluated live as the worker fills fields.

**Predicate count**: 2 predicates with `and`. Within the 3-predicate budget.

### Walk-through observations

- **Expression format handles all three reference namespaces**: `entity.*`, `context.*`, `payload.*` ✓
- **Both output types covered**: condition (boolean for show/hide and warning) and value (computed boolean for default) ✓
- **L1/L2 separation signal**: Each expression carries `activity_ref`. This makes activity-specific logic possible. If expressions were inline in the shape, the `activity_ref` field would be meaningless — all activities sharing the shape get identical logic. **This confirms: expressions must be external to shapes, bound by (activity_ref, shape_ref, field).**
- **DtV can validate independently**: L1 checks (field exists, types match) are separate from L2 checks (references resolve, predicate budget). Clean separation.

---

## Stage 4: DtV Validates

DtV runs on the full config before packaging.

### Checks that pass:

| Check | Result |
|-------|--------|
| Shape name format | `facility_observation` matches `[a-z][a-z0-9_]*` ✓ |
| Fields per shape | 6 ≤ 60 ✓ |
| Field types in vocabulary | `subject_ref`, `select`, `integer`, `multi_select`, `boolean`, `narrative` — all valid ✓ |
| No duplicate field names | All unique ✓ |
| Shape version monotonic | 1 (first version) ✓ |
| Expression references valid | `entity.facility_type` (entity attribute), `context.days_since_last_event` (valid context property), `payload.service_availability` (field exists in shape), `payload.staff_present` (field exists in shape) ✓ |
| Predicate count per condition | Max 2 ≤ 3 ✓ |
| Activity→shape reference | `facility_observation/v1` exists ✓ |
| Sensitivity level valid | `standard` ∈ {standard, elevated, restricted} ✓ |
| Dependency cascade | No deprecated fields referenced by active expressions ✓ |

### Invented violation — DtV catches it:

A configuration specialist adds a fourth expression:

```json
{
  "shape_ref": "facility_observation/v1",
  "field": "followup_notes",
  "activity_ref": "facility_monitoring",
  "type": "show_condition",
  "condition": {
    "and": [
      { "eq": ["payload.needs_followup", true] },
      { "neq": ["entity.facility_type", "community_health_post"] },
      { "gt": ["context.event_count", 0] },
      { "eq": ["payload.service_availability", "partial"] }
    ]
  }
}
```

**DtV rejects**: "Expression for field `followup_notes` in `facility_monitoring/facility_observation/v1`: condition has 4 predicates, exceeds maximum of 3."

The deployer simplifies:

```json
{
  "shape_ref": "facility_observation/v1",
  "field": "followup_notes",
  "activity_ref": "facility_monitoring",
  "type": "show_condition",
  "condition": {
    "eq": ["payload.needs_followup", true]
  }
}
```

**DtV passes.** Single predicate. Only show follow-up notes when follow-up is flagged.

### Additional DtV check — trigger context rejection:

If someone mistakenly writes an expression for trigger context referencing `entity.*`:

```json
{
  "context": "trigger",
  "condition": { "eq": ["entity.facility_type", "hospital"] }
}
```

**DtV rejects**: "Trigger-context expression must not reference `entity.*`. Only `event.*` references allowed."

### Walk-through observations

- All Phase 3 DtV checks from §9 are exercised ✓
- **Missing DtV check identified**: `multi_select` payload values must be arrays. When an expression references a `multi_select` field with a scalar comparison operator (`eq`, `gt`), DtV should either reject or require the `in` operator. **But wait** — this is a runtime type mismatch, not a budget/composition violation. The expression grammar uses `in` for set-membership (`{"in": ["vaccines", "payload.stockout_items"]}`). DtV should validate that expressions referencing `multi_select` fields don't use scalar comparisons. **Add to DtV checks: type-compatibility between expression operators and referenced field types.**
- **New DtV check (not in §9)**: Expression `type: "default"` on a `required: true` field — is that valid? Yes: the default pre-fills but the worker can change it. No DtV violation needed.

---

## Stage 5: Config Packager Assembles

After DtV passes, Config Packager builds the atomic package:

```json
{
  "version": 1,
  "published_at": "2026-05-15T08:00:00Z",
  "shapes": {
    "facility_observation/v1": {
      "name": "facility_observation",
      "version": 1,
      "status": "active",
      "sensitivity": "standard",
      "uniqueness": null,
      "fields": [
        { "name": "subject_facility", "type": "subject_ref", "required": true },
        { "name": "service_availability", "type": "select", "required": true, "options": ["full", "partial", "closed"] },
        { "name": "staff_present", "type": "integer", "required": true, "validation": { "min": 0, "max": 200 } },
        { "name": "stockout_items", "type": "multi_select", "required": false, "options": ["vaccines", "antimalarials", "ors", "antibiotics", "gloves", "none"] },
        { "name": "needs_followup", "type": "boolean", "required": true },
        { "name": "followup_notes", "type": "narrative", "required": false }
      ]
    }
  },
  "activities": {
    "facility_monitoring": {
      "name": "facility_monitoring",
      "shapes": ["facility_observation/v1"],
      "roles": {
        "field_worker": ["capture"],
        "supervisor": ["capture", "review"]
      },
      "pattern": null,
      "sensitivity": "standard"
    }
  },
  "expressions": {
    "facility_monitoring/facility_observation/v1": [
      {
        "field": "stockout_items",
        "type": "show_condition",
        "condition": { "neq": ["entity.facility_type", "community_health_post"] }
      },
      {
        "field": "needs_followup",
        "type": "default",
        "value": { "gt": ["context.days_since_last_event", 90] }
      },
      {
        "field": "staff_present",
        "type": "warning",
        "condition": {
          "and": [
            { "eq": ["payload.service_availability", "full"] },
            { "lt": ["payload.staff_present", 3] }
          ]
        },
        "message": "Full service with fewer than 3 staff — please verify"
      },
      {
        "field": "followup_notes",
        "type": "show_condition",
        "condition": { "eq": ["payload.needs_followup", true] }
      }
    ]
  },
  "flag_severity_overrides": {},
  "sensitivity_classifications": {
    "facility_observation/v1": "standard",
    "facility_monitoring": "standard"
  }
}
```

### Walk-through observations

- **Forward-compatibility stubs present**: `pattern: null`, `uniqueness: null`, `flag_severity_overrides: {}` ✓
- **Expressions keyed by `{activity_ref}/{shape_ref}`** — this is the natural binding key when expressions are external to shapes. The key supports the "same shape, different activities, different expressions" pattern.
- **Authority is clear**: shape definitions live in `"shapes"`, expressions live in `"expressions"`. No duplication. No overlapping authority (addresses AP-6).
- **Sensitivity classifications denormalized** into the package for easy device-side lookup.
- **All shape versions included** (currently only v1). When v2 arrives, both appear in `"shapes"`.

---

## Stage 6: Device Receives Config

### Sync flow:

1. Device performs regular sync pull (push events, pull new events).
2. Sync response includes `"config_version": 1` header field.
3. Device compares with its current config version (null — first time).
4. Device calls `GET /api/sync/config` (or receives inline — DD-3 decides).
5. Response: the full config package JSON above.
6. Device stores as `pending` config.
7. No form is currently open → device switches immediately: `pending` → `current`, old `current` discarded.

### If a form were open:

- Config stays as `pending`.
- Worker completes and submits the open form under old config.
- On next form-open, device promotes `pending` → `current`.
- At-most-2 guarantee: `current` + `pending`. Never more.

### Walk-through observations

- **Config version signaling works**: simple monotonic integer comparison. Device only downloads if server version > local version.
- **Transition timing is form-open**: clean boundary. No mid-form config switch.
- **Full package download**: no delta/patch complexity. Package is small (6 fields × ~100 bytes = negligible). Even at 50 shapes with 60 fields each, package is <1MB.

---

## Stage 7: Worker Captures an Event

A field worker visits Facility #A47 (a district hospital).

### Form-open sequence:

1. Worker opens `facility_monitoring` activity from work list.
2. Form engine reads shape `facility_observation/v1` from current config.
3. Form engine evaluates L2 expressions:
   - **Show/hide (`stockout_items`)**: PE resolves `entity.facility_type` = `"district_hospital"` → `neq "community_health_post"` → **true** → field shown.
   - **Default (`needs_followup`)**: PE resolves `context.days_since_last_event` = 120 → `gt 90` → **true** → default value set to `true`.
   - **Show/hide (`followup_notes`)**: `payload.needs_followup` = `true` (from default) → **shown**.

4. Form renders: `subject_facility` (pre-set to A47 since worker navigated from subject), `service_availability`, `staff_present`, `stockout_items` (visible), `needs_followup` (defaulted true), `followup_notes` (visible because default is true).

### Worker fills:

- `service_availability` = `"full"`
- `staff_present` = `2`
- `stockout_items` = `["antimalarials", "ors"]`
- `needs_followup` = leaves as `true` (default)
- `followup_notes` = `"Severe understaffing, stockouts of critical items. Needs urgent support."`

### Live expression re-evaluation:

- Worker entered `service_availability = "full"` and `staff_present = 2`.
- **Warning fires**: `full AND staff_present < 3` → true → warning shown: "Full service with fewer than 3 staff — please verify"
- Worker reads warning, confirms the count is correct, submits anyway (warnings are non-blocking).

### Submission:

5. Validation engine checks payload against shape L1 constraints:
   - `subject_facility`: non-null UUID, exists in subject registry ✓
   - `service_availability`: in options list ✓
   - `staff_present`: integer, 0 ≤ 2 ≤ 200 ✓
   - `stockout_items`: array, all values in options list ✓
   - `needs_followup`: boolean ✓
   - `followup_notes`: string (no max_length constraint) ✓

6. Event stored locally:

```json
{
  "id": "evt_...",
  "type": "capture",
  "shape_ref": "facility_observation/v1",
  "activity_ref": "facility_monitoring",
  "subject_ref": "sub_A47",
  "actor_ref": "actor_fw1",
  "occurred_at": "2026-05-16T09:23:00Z",
  "received_at": null,
  "version": 1,
  "payload": {
    "subject_facility": "sub_A47",
    "service_availability": "full",
    "staff_present": 2,
    "stockout_items": ["antimalarials", "ors"],
    "needs_followup": true,
    "followup_notes": "Severe understaffing, stockouts of critical items. Needs urgent support."
  }
}
```

### Walk-through observations

- **`shape_ref` and `activity_ref` in envelope** — event is self-describing ✓
- **Expression evaluation offline** — all references resolved from local projection. No server call ✓
- **Warning is non-blocking** — L2 affects what user sees, not what gets stored (L2 key distinction) ✓
- **`multi_select` payload is an array** — form engine and validation engine both handle this correctly ✓
- **`subject_ref` field vs envelope `subject_ref`**: The shape field `subject_facility` (type `subject_ref`) and the envelope's `subject_ref` serve different purposes. Envelope `subject_ref` identifies the event's subject for projection/routing. The payload field captures the deployer's semantic relationship. **In practice for S01, they're the same value.** The form engine could auto-populate envelope `subject_ref` from the first `subject_ref`-typed field, or the activity binding defines which field maps to envelope. This is a DD-1 consideration — document it.

---

## Stage 8: Server Receives and Projects

### Sync push:

1. Device pushes event during next sync.
2. Server receives, validates:
   - Envelope structure (11 fields) ✓
   - `shape_ref` exists in Shape Registry ✓
   - Payload validates against `facility_observation/v1` schema ✓
   - Actor authorized for `capture` action on `facility_monitoring` activity ✓ (Phase 2 infrastructure)
   - Scope containment: subject A47 within actor's geographic scope ✓ (Phase 2 infrastructure)
3. Server persists event, sets `received_at`.
4. PE projects: updates subject A47's state with latest observation data.

### Shape evolution — deployer creates v2:

The program decides to track water availability. Configuration specialist creates `facility_observation/v2`:

```json
{
  "name": "facility_observation",
  "version": 2,
  "status": "active",
  "sensitivity": "standard",
  "uniqueness": null,
  "fields": [
    { "name": "subject_facility", "type": "subject_ref", "required": true },
    { "name": "service_availability", "type": "select", "required": true, "options": ["full", "partial", "closed"] },
    { "name": "staff_present", "type": "integer", "required": true, "validation": { "min": 0, "max": 200 } },
    { "name": "stockout_items", "type": "multi_select", "required": false, "options": ["vaccines", "antimalarials", "ors", "antibiotics", "gloves", "none"] },
    { "name": "water_available", "type": "boolean", "required": true },
    { "name": "needs_followup", "type": "boolean", "required": true },
    { "name": "followup_notes", "type": "narrative", "required": false }
  ]
}
```

Changes from v1:
- **Added**: `water_available` (new required field — acceptable because new events use v2)
- **Deprecation-only**: v1 fields are all preserved. No removals. No type changes.

### Multi-version projection:

- **v1 events** (like the one captured above): `water_available` is absent → PE treats as null.
- **v2 events** (new captures): all fields present including `water_available`.
- **Union projection**: PE sees both as the same logical shape. Missing fields = null. Subject A47's projected state:
  - Latest `service_availability`: "full" (from v1 event)
  - Latest `staff_present`: 2 (from v1 event)
  - Latest `water_available`: null (no v2 event yet for this subject)
- After a v2 event for A47: `water_available` gets a value.

### Walk-through observations

- **Deprecation-only evolution works cleanly** — v2 is a strict superset ✓
- **Union projection is sufficient** — no version-routing logic needed. Missing = null ✓
- **Config package v2 includes both shape versions** — device can render historical v1 events and capture new v2 events ✓
- **`shape_ref` in existing events is immutable** — v1 events always say `facility_observation/v1`, correctly ✓

---

## Findings

> All findings below were consumed by IDR-017, IDR-018, and IDR-019. This section is now reference material.

### Brief constraints that hold:

1. **L1/L2 separation**: Confirmed necessary. The `entity.facility_type` show/hide expression makes sense for `facility_monitoring` but might not apply in an `emergency_assessment` activity using the same shape. Expressions must be activity-scoped.
2. **Field type vocabulary**: All 6 fields mapped to distinct types from the brief's vocabulary. `subject_ref`, `integer`, `multi_select`, and `narrative` are all exercised and necessary.
3. **Flat fields**: Adequate. No repeating-group pressure in this scenario.
4. **Expression format**: The brief's operator set handles all three expressions without strain.
5. **DtV checks**: All apply. One addition needed (operator-type compatibility).
6. **Config package structure**: Works. The `{activity_ref}/{shape_ref}` expression key is natural.
7. **Forward-compatibility stubs**: Present and harmless.
8. **Multi-version projection**: Union projection handles v1→v2 cleanly.

### Issues found:

| # | Issue | Severity | Resolution |
|---|-------|----------|------------|
| 1 | **DtV needs operator-type compatibility check** — scalar operators on `multi_select` fields should be rejected (use `in` instead) | Medium | → IDR-018 operator-type compatibility table |
| 2 | **`subject_ref` field ↔ envelope `subject_ref` relationship** — needs explicit rule for how shape-level `subject_ref` fields map to the envelope's `subject_ref` | Medium | → IDR-017 `subject_binding` field in shape document |
| 3 | **Expression default output type must match field type** — `{ "gt": [...] }` produces boolean, assigned to a boolean field. What if someone writes a numeric expression for a boolean field? | Low | → IDR-018 DtV check: default expression output type vs target field type |

### L1/L2 separation verdict:

**Separate.** The walk-through makes this unambiguous:
- Same shape used in different activities needs different show/hide logic.
- Expression change (e.g., tweaking the warning threshold from 3 to 2) should NOT create a new shape version.
- DtV validates shapes and expressions with different rule sets — cleaner as separate artifacts.
- The expression binding key `(activity_ref, shape_ref, field)` is natural and complete.

### Field type vocabulary verdict:

The walk-through confirms these types are necessary beyond the baseline 6:
- **`subject_ref`**: Mandatory for S01. No workaround.
- **`integer`**: Distinct from decimal. Staff count = whole numbers.
- **`multi_select`**: Distinct from `select`. Stockout checklist = multiple selections, stored as array.
- **`narrative`**: Could be `text` with a flag, but the semantic distinction (multiline, no length cap) is meaningful for form rendering and future features (text analysis). Worth the type.

**Resolution**: 10 types total: `text`, `integer`, `decimal`, `boolean`, `date`, `select`, `multi_select`, `location`, `subject_ref`, `narrative`.
