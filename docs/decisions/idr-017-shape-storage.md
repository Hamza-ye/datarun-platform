---
id: idr-017
title: Shape storage — versioned snapshots with L1/L2 separation
status: active
date: 2026-04-19
phase: 3a
type: decision
reversal-cost: high
touches: [server/config, mobile/data, contracts]
superseded-by: ~
evolves: ~
commit: ~
tags: [configuration, shape, dd, storage, versioning]
---

# Shape Storage — Versioned Snapshots with L1/L2 Separation

## Context

Phase 3 makes the platform domain-agnostic by introducing deployer-authored shapes that define event payloads. ADR-4 S1/S10 establish that shapes are versioned (monotonically), stored as snapshots, immutable once published, and all versions remain valid forever. The investigation brief (§DD-1) identified two critical open questions: (1) the L1/L2 separation — do expressions live inside shapes or externally? (2) the field type vocabulary.

The S01 walk-through confirmed that expressions MUST be external: the same shape used in different activities requires different form logic. Embedding expressions in shapes makes activity-specific logic impossible and causes unnecessary version churn.

## Decision

### Table Schema

```sql
CREATE TABLE shapes (
    name        VARCHAR(100) NOT NULL,
    version     INTEGER NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'active',
    sensitivity VARCHAR(20) NOT NULL DEFAULT 'standard',
    schema_json JSONB NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (name, version),
    CONSTRAINT chk_shape_name CHECK (name ~ '^[a-z][a-z0-9_]*$'),
    CONSTRAINT chk_version_positive CHECK (version > 0),
    CONSTRAINT chk_status CHECK (status IN ('active', 'deprecated')),
    CONSTRAINT chk_sensitivity CHECK (sensitivity IN ('standard', 'elevated', 'restricted'))
);

CREATE INDEX idx_shapes_name ON shapes (name);
CREATE INDEX idx_shapes_status ON shapes (name, status);
```

Each row is a **standalone snapshot** — no delta-application logic. Creating a new version stores the complete field list. The admin UI presents "create new version" as: copy current → edit → save as N+1.

### Shape JSON Document Structure (`schema_json` column)

```json
{
  "fields": [
    {
      "name": "subject_facility",
      "type": "subject_ref",
      "required": true,
      "description": "The facility being observed",
      "display_order": 1,
      "group": null,
      "deprecated": false,
      "options": null,
      "validation": null
    },
    {
      "name": "staff_present",
      "type": "integer",
      "required": true,
      "description": null,
      "display_order": 3,
      "group": "staffing",
      "deprecated": false,
      "options": null,
      "validation": { "min": 0, "max": 200 }
    },
    {
      "name": "stockout_items",
      "type": "multi_select",
      "required": false,
      "description": null,
      "display_order": 4,
      "group": "supply",
      "deprecated": false,
      "options": ["vaccines", "antimalarials", "ors", "antibiotics", "gloves", "none"],
      "validation": null
    }
  ],
  "uniqueness": null,
  "subject_binding": "subject_facility"
}
```

**Field object properties**:

| Property | Type | Required | Purpose |
|----------|------|----------|---------|
| `name` | string | yes | Field identifier. `[a-z][a-z0-9_]*`. Unique within shape. |
| `type` | string | yes | One of the 10-type vocabulary (see below). |
| `required` | boolean | yes | L1 validation: reject if null on submit. |
| `description` | string | no | Human-readable hint for form rendering. |
| `display_order` | integer | no | Form field ordering. Null = append order. |
| `group` | string | no | Visual grouping label for form sectioning. |
| `deprecated` | boolean | no | Default false. Deprecated fields hidden from new form entry but present in historical events. |
| `options` | string[] | conditional | Required for `select` and `multi_select`. Null for all other types. |
| `validation` | object | no | L1 parameter constraints: `{min, max}` for numeric/text-length, `{precision}` for decimal. No expressions. No conditionals. |

**Top-level shape document properties**:

| Property | Type | Purpose |
|----------|------|---------|
| `fields` | array | Ordered list of field objects. Max 60 (DtV-enforced). |
| `uniqueness` | object\|null | Phase 4 stub. Format: `{scope, period, action}`. Null in Phase 3. |
| `subject_binding` | string\|null | Name of the `subject_ref`-typed field that maps to the event envelope's `subject_ref.id`. Null if shape has no subject link. |

### L1/L2 Separation: Expressions Are External

Expressions (L2) are **NOT** stored in the shape definition (L1). They are separate artifacts stored in their own table, keyed by `(activity_ref, shape_ref, field_name)`:

```sql
CREATE TABLE expression_rules (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    activity_ref VARCHAR(255) NOT NULL,
    shape_ref    VARCHAR(255) NOT NULL,
    field_name   VARCHAR(100) NOT NULL,
    rule_type    VARCHAR(20) NOT NULL,
    expression   JSONB NOT NULL,
    message      TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_rule_type CHECK (rule_type IN ('show_condition', 'default', 'warning')),
    CONSTRAINT uq_expression_rule UNIQUE (activity_ref, shape_ref, field_name, rule_type)
);

CREATE INDEX idx_expr_activity_shape ON expression_rules (activity_ref, shape_ref);
```

**Rationale** (established by walk-through):
1. Same shape in different activities needs different expressions.
2. Expression changes don't create new shape versions — shapes version only on structural change.
3. DtV validates L1 and L2 with separate rule sets — cleaner as separate artifacts.
4. Config package authority is unambiguous: shapes live in `"shapes"`, expressions in `"expressions"`. No AP-6 (Overlapping Authority).

**Consequence**: A shape version bump means a field was added, removed (deprecated), or a field's type/options/validation changed. Logic-only changes (show/hide threshold, warning message) modify expression_rules without touching the shape.

### Activity Definition Storage

```sql
CREATE TABLE activities (
    name        VARCHAR(100) PRIMARY KEY,
    config_json JSONB NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'active',
    sensitivity VARCHAR(20) NOT NULL DEFAULT 'standard',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_activity_name CHECK (name ~ '^[a-z][a-z0-9_]*$'),
    CONSTRAINT chk_activity_status CHECK (status IN ('active', 'deprecated')),
    CONSTRAINT chk_activity_sensitivity CHECK (sensitivity IN ('standard', 'elevated', 'restricted'))
);
```

Activity `config_json`:
```json
{
  "shapes": ["facility_observation/v1"],
  "roles": {
    "field_worker": ["capture"],
    "supervisor": ["capture", "review"]
  },
  "pattern": null
}
```

Activities are NOT versioned individually — they are config-package artifacts (package-level versioning per ADR-4 S6). Editing an activity updates the row; publishing a new config package captures the current state.

### Field Type Vocabulary (10 Types — Lock)

| Type | Payload representation | L1 validation params | Form widget |
|------|----------------------|---------------------|-------------|
| `text` | string | `{min_length, max_length}` | Single-line input |
| `integer` | number (no decimal) | `{min, max}` | Numeric input, step=1 |
| `decimal` | number | `{min, max, precision}` | Numeric input |
| `boolean` | true/false | — | Toggle/checkbox |
| `date` | ISO 8601 string | `{min, max}` (date bounds) | Date picker |
| `select` | string (one of options) | — (options list is the constraint) | Dropdown/radio |
| `multi_select` | string[] (subset of options) | — (options list is the constraint) | Checkboxes |
| `location` | UUID string | — (must exist in locations table) | Location picker |
| `subject_ref` | UUID string | — (must exist in subject registry) | Subject picker |
| `narrative` | string | `{max_length}` (optional, no default cap) | Multiline textarea |

**Integer vs Decimal**: Split (not unified `number`). Rationale: form input behavior differs (integer rejects decimal input at the widget level), validation differs (integer checks `value % 1 === 0`), and the distinction is meaningful to deployers (counting items vs measuring quantities).

**`multi_select` vs `select` with flag**: Separate types (not `select` with `multiple: true`). Rationale: payload representation differs fundamentally (string vs string[]), expression operators differ (`eq` vs `in`), form widget differs (radio vs checkboxes). A flag would hide a type-level difference behind a parameter.

**`narrative` vs `text` with flag**: Separate type. Rationale: form rendering is distinctly different (single-line vs multiline), default length constraints differ (text typically capped, narrative intentionally uncapped), and future platform features (text search, case notes) may treat them differently.

### Flat Fields — Deliberate Lock

Shape fields are flat — no nested objects, no repeating groups, no arrays-of-objects. This is a deliberate architectural choice, not a simplification to be revisited:

- Repeating groups fight the 60-field budget (a group of 5 fields × 10 repetitions = 50 fields consumed).
- Repeating groups break `payload.*` flat references in expressions.
- The event-sourced architecture handles line-items naturally: deployer creates separate shapes per item type, multiple events per subject.
- Cross-platform evaluation complexity doubles with nesting.

`multi_select` is the sole array type in payloads — it's a closed array (values from options list), not a structural repetition.

### subject_binding — Envelope Mapping

The shape document's `subject_binding` field names which `subject_ref`-typed field maps to the event envelope's `subject_ref.id`. This resolves the S01 ambiguity:

- When a form is opened from a subject view, the bound field is pre-populated.
- On submission, the form engine copies the bound field's value into envelope `subject_ref.id`.
- If `subject_binding` is null, envelope `subject_ref` must be set by the activity context (e.g., navigating to a subject first).

DtV check: if `subject_binding` is specified, the named field must exist, be of type `subject_ref`, and be `required: true`.

### Version Lifecycle

1. **Create v1**: Deployer authors initial shape. Stored as row `(name, 1, 'active', ...)`.
2. **Create v2**: Admin UI copies v1 fields, deployer edits, saves as `(name, 2, 'active', ...)`. v1 remains active.
3. **Deprecate v1**: Status changes to `deprecated`. Hidden from new form creation. Events with `shape_ref: "name/v1"` still project correctly.
4. **All versions forever**: No deletion. `SELECT * FROM shapes WHERE name = ?` returns full history. Config package includes all.

### shape_ref Format

`{name}/v{version}` — e.g., `facility_observation/v1`. Matches existing `events.shape_ref` column (VARCHAR 255). Lookup: parse into `(name, version)` for PK query.

## Alternatives Rejected

- **Expressions inline in shape JSON** — Prevents activity-specific logic. Causes version churn for logic changes. Creates AP-6 authority overlap with config package `"expressions"` key. Walk-through confirmed this is unworkable.
- **Delta-based storage (store diffs, apply to produce snapshots)** — Adds application logic complexity for negligible storage savings. 60 fields × ~200 bytes = 12KB per version. Not worth the complexity.
- **Single `number` type with precision parameter** — Hides a meaningful distinction (integer vs decimal) behind a parameter. Form widgets, validation logic, and deployer mental models differ. Split is clearer.
- **`select` with `multiple: true` flag** — Payload type changes from string to string[]. This is a type-level difference, not a parameter. Separate types make the contract explicit.
- **File-based shape storage** — No queryability. Can't enforce constraints. Can't index for lookups. PostgreSQL JSONB gives both storage and queryability.

## Consequences

- Shape JSON format is a **Lock** — persisted in DB, synced to devices, parsed by both evaluators and form engines. Changes require Flyway migration + mobile migration + config re-delivery.
- Expression rules are decoupled from shape versions — logic can evolve independently of structure.
- Activities reference shapes by `{name}/v{version}` in their `shapes` array. Activity update (pointing to v2) doesn't require shape table changes.
- DD-2 designs the `expression` JSONB column format (AST nodes). DD-3 designs how shapes + expression_rules + activities are assembled into the config package.
- DtV validates shapes (L1) and expression_rules (L2) as separate passes with distinct checks.
- The 10-type vocabulary is a Lock — adding a type later is possible (JSONB, no migration) but requires evaluator + form engine + DtV updates on both platforms.

## Traces

- ADR: ADR-4 S1 (shape_ref format), S9 (typed fields), S10 (versioning, deprecation-only), S13 (budgets)
- Constraint: C11, C12, C13, C14
- Walk-through: [s01-facility-observation-config-pipeline.md](../walk-throughs/s01-facility-observation-config-pipeline.md)
- Files: `server/src/main/resources/db/migration/V5__shapes_and_config.sql`, `server/src/main/java/dev/datarun/server/config/`
