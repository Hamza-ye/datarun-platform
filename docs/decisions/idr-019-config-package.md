---
id: idr-019
title: Config package — atomic JSON delivery via dedicated endpoint
status: active
date: 2026-04-19
phase: 3c
type: decision
reversal-cost: high
touches: [server/config, server/sync, mobile/data, contracts]
superseded-by: ~
evolves: ~
commit: ~
tags: [configuration, package, dd, sync, delivery]
---

# Config Package — Atomic JSON Delivery via Dedicated Endpoint

## Context

ADR-4 S6 establishes atomic config delivery with at-most-2 versions on device. The investigation brief (§DD-3) identifies three open questions: (1) package versioning mechanism, (2) endpoint design, and (3) authority for expressions. DD-1 (IDR-017) resolved that expressions are external (L1/L2 separated), making the package's `"expressions"` key the authoritative home for L2 artifacts. DD-2 (IDR-018) defined the AST format for expression content.

## Decision

### Config Package JSON Structure

```json
{
  "version": 12,
  "published_at": "2026-05-15T08:00:00Z",
  "shapes": {
    "facility_observation/v1": {
      "name": "facility_observation",
      "version": 1,
      "status": "active",
      "sensitivity": "standard",
      "uniqueness": null,
      "fields": [
        { "name": "subject_facility", "type": "subject_ref", "required": true, "description": "The facility being observed", "display_order": 1, "group": null, "deprecated": false, "options": null, "validation": null },
        { "name": "service_availability", "type": "select", "required": true, "description": null, "display_order": 2, "group": null, "deprecated": false, "options": ["full", "partial", "closed"], "validation": null },
        { "name": "staff_present", "type": "integer", "required": true, "description": null, "display_order": 3, "group": "staffing", "deprecated": false, "options": null, "validation": { "min": 0, "max": 200 } }
      ],
      "subject_binding": "subject_facility"
    },
    "facility_observation/v2": {
      "name": "facility_observation",
      "version": 2,
      "status": "active",
      "sensitivity": "standard",
      "uniqueness": null,
      "fields": [ "..." ],
      "subject_binding": "subject_facility"
    }
  },
  "activities": {
    "facility_monitoring": {
      "name": "facility_monitoring",
      "shapes": ["facility_observation/v2"],
      "roles": {
        "field_worker": ["capture"],
        "supervisor": ["capture", "review"]
      },
      "pattern": null,
      "sensitivity": "standard"
    }
  },
  "expressions": {
    "facility_monitoring": {
      "facility_observation/v2": [
        {
          "field": "stockout_items",
          "type": "show_condition",
          "expression": { "when": { "neq": ["entity.facility_type", "community_health_post"] } }
        },
        {
          "field": "needs_followup",
          "type": "default",
          "expression": { "value": { "gt": ["context.days_since_last_event", 90] } }
        },
        {
          "field": "staff_present",
          "type": "warning",
          "expression": { "when": { "and": [{ "eq": ["payload.service_availability", "full"] }, { "lt": ["payload.staff_present", 3] }] } },
          "message": "Full service with fewer than 3 staff — please verify"
        }
      ]
    }
  },
  "flag_severity_overrides": {},
  "sensitivity_classifications": {
    "shapes": {
      "facility_observation/v1": "standard",
      "facility_observation/v2": "standard"
    },
    "activities": {
      "facility_monitoring": "standard"
    }
  }
}
```

### Package Structure Keys

| Key | Type | Content | Phase 3 | Phase 4+ |
|-----|------|---------|---------|----------|
| `version` | integer | Monotonic counter, incremented on each publish | ✓ | ✓ |
| `published_at` | ISO 8601 | Server timestamp of package assembly | ✓ | ✓ |
| `shapes` | object | All shape versions (keyed by `{name}/v{version}`), including deprecated | ✓ | ✓ |
| `activities` | object | All active activities (keyed by name) | ✓ | ✓ |
| `expressions` | object | L2 expression rules, keyed by `{activity_ref}.{shape_ref}` | ✓ | ✓ |
| `flag_severity_overrides` | object | Flat `{flag_category: severity}` map. Empty in Phase 3. | stub | populated |
| `sensitivity_classifications` | object | Per-shape and per-activity sensitivity levels | ✓ | ✓ |

### Package Versioning — Monotonic Counter

Config version is a server-managed **monotonic integer** stored in a dedicated table:

```sql
CREATE TABLE config_packages (
    version      INTEGER PRIMARY KEY,
    package_json JSONB NOT NULL,
    published_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_by UUID
);
```

Each publish increments version by 1. The full package is stored as a snapshot (not as a diff from previous). Old package rows are retained for audit but never sent to devices — only the latest version is served.

**Why monotonic counter over content hash**: 
- Simple comparison: `device_version < server_version` → download needed.
- Human-readable: "device is on config v12" is clearer than "device is on config abc4f2e".
- Ordered: you can tell which is newer without parsing.
- Content hash has no ordering guarantee and requires the device to store the full hash.

### Config Endpoint Design — Separate Endpoint

**Endpoint**: `GET /api/sync/config`

**Request**: Bearer token (same auth as sync). No body needed.

**Response** (200):
```json
{
  "version": 12,
  "published_at": "...",
  "shapes": { ... },
  "activities": { ... },
  "expressions": { ... },
  "flag_severity_overrides": { ... },
  "sensitivity_classifications": { ... }
}
```

**Response** (304 Not Modified): If `If-None-Match: 12` header matches current version. Saves bandwidth on no-change syncs.

**Discovery mechanism**: The existing sync pull response (`GET /api/sync/pull`) gains one field:

```json
{
  "events": [...],
  "config_version": 12
}
```

The device compares `config_version` from pull response with its local config version. If different, it calls the config endpoint. This keeps the sync pull lean (no config payload in event sync) while providing version discovery without a separate poll.

**Why separate endpoint over inline delivery**:
- Config payloads are larger than typical event sync responses. Mixing them would make every sync cycle slow even when config hasn't changed.
- Config changes are rare (days/weeks between publishes). Event sync is frequent (minutes/hours). Different cadences → separate channels.
- The device can skip the config download entirely if versions match (304/skip).
- Config delivery can be independently retried on network failure without re-pushing events.

### Expression Key Structure in Package

Expressions are keyed hierarchically: `expressions[activity_ref][shape_ref]` → array of rules.

```
expressions
  └── "facility_monitoring"              (activity_ref)
        └── "facility_observation/v2"    (shape_ref)
              ├── { field, type, expression, message? }
              ├── ...
```

**Why this structure**:
- The device form engine needs expressions for a specific `(activity, shape)` pair at form-open time. This key structure gives O(1) lookup.
- Activity references the "current" shape version in its `shapes` array. Expressions reference the same version. When shape upgrades v1→v2, expressions are written for v2. Old v1 expressions are not included (v1 is for projection only, not new captures).
- If an activity references multiple shapes, each shape has its own expression array under the activity key.

### Device-Side Config Management

#### Storage

Device stores config as two slots:

| Slot | Purpose |
|------|---------|
| `current` | Active config. Forms render from this. May be null on first launch. |
| `pending` | Downloaded but not yet applied. Null when no update waiting. |

Storage format: a single JSON file per slot in the app's private storage (not in SQLite — config is read as a whole, not queried field-by-field). Alternatively, a `config` table with two rows — implementation choice (Lean).

#### Transition Logic

```
On sync pull response:
  if response.config_version > local.current.version (or current is null):
    download config from /api/sync/config
    store as pending
    
On form-open:
  if pending != null:
    current = pending
    pending = null
  render form using current config
```

**Invariants**:
- A form in progress always uses the config that was `current` when it opened. No mid-form switch.
- At most 2 config versions exist on device: `current` + `pending`.
- If device has no `current` (first launch), it must download config before any form can render. The work list shows "Configuration required — sync to download."
- If download fails, device continues with `current` (or stays blocked if null).

#### Version Tracking on Server

The server tracks what config version each device has received:

```sql
ALTER TABLE device_sync_state ADD COLUMN config_version INTEGER DEFAULT 0;
```

Updated when device successfully downloads config (the config endpoint response is a 200). This allows the server to know which devices are outdated — useful for admin visibility ("3 devices still on config v11") but not required for correctness (devices self-discover via pull response).

### DtV → Packager Pipeline

1. Deployer edits shapes, activities, or expressions in admin UI (granular authoring).
2. Deployer clicks "Publish Config".
3. **DtV runs** on the full candidate package:
   - All L1 checks (field counts, types, names, validation params)
   - All L2 checks (expression references, predicate counts, type compatibility)
   - Dependency cascade (expression→shape field references, activity→shape references)
   - Cross-references (activity's `shapes` array points to existing shape versions)
4. If DtV passes → **Config Packager assembles**:
   - Fetches all shape versions from `shapes` table
   - Fetches all active activities from `activities` table
   - Fetches all expression_rules grouped by activity+shape
   - Assembles the JSON structure above
   - Stores in `config_packages` with incremented version
5. If DtV fails → publish blocked, specific violations returned to admin UI.

### Forward-Compatibility for Phase 4

The package structure accommodates Phase 4 additions without schema migration:

| Phase 4 artifact | How it fits |
|------------------|-------------|
| Triggers (L3a, L3b) | New top-level key `"triggers": {}` — devices already parse unknown keys as ignored in Phase 3. Or: add to package schema when Phase 4 ships (the package JSON schema itself is Lean — only the structural keys are Lock). |
| Pattern Registry | `activities[name].pattern` goes from `null` to a pattern reference string. |
| Uniqueness constraints | `shapes[ref].uniqueness` goes from `null` to `{scope, period, action}`. |
| Flag severity overrides | `flag_severity_overrides` goes from `{}` to populated. |
| Projection rules | New top-level key `"projection_rules": {}`. |

**Device tolerance**: The mobile config parser must ignore unknown top-level keys (forward-compatible parsing). When Phase 4 adds `"triggers"`, devices running Phase 3 code ignore it safely. Devices updated to Phase 4 code read it.

### What the Package Does NOT Include

- **Event data** — events sync separately via `/api/sync/pull`.
- **Location hierarchy** — already on device from Phase 2 sync.
- **Actor assignments** — already synced as assignment_changed events.
- **Subject registry** — subjects are built from event projections.

The config package is purely **configuration metadata** — it tells the device how to render forms, evaluate logic, and validate input. It contains no operational data.

## Alternatives Rejected

- **Config inline in sync pull response** — Makes every sync cycle carry the full config payload even when unchanged. Config changes are rare; event sync is frequent. Wasteful.
- **Content-hash versioning** — No natural ordering. Devices can't determine "is mine outdated?" without a server roundtrip that returns both hashes. Monotonic integer allows simple `<` comparison.
- **Delta/patch delivery** — Config packages are small (<1MB even at scale). Delta logic adds complexity for negligible savings. Full snapshots are simpler and eliminate state-reconstruction bugs.
- **SQLite config storage on device** — Config is read as a whole at form-open, not queried per-field. A JSON file (or single DB blob) is simpler. No query benefits from normalization.
- **Expressions embedded in shapes within the package** — Would create dual authority (shapes have expressions AND `"expressions"` key has them). AP-6 violation. IDR-017 settled this: expressions are external, package `"expressions"` key is authoritative.
- **Per-device config variants** — Out of scope. All devices in a deployment receive the same config. Actor-specific behavior is handled by role-action mappings and scope filtering, not config variants.

## Consequences

- The config package wire format is a **Lock** — once mobile parses this structure, changing it requires coordinated server + mobile release.
- The separate endpoint means sync and config delivery are independently evolvable. Adding config features doesn't affect the event sync protocol.
- The `config_version` field in sync pull responses is a lightweight addition — one integer field. No protocol break.
- Forward-compatible parsing (ignore unknown keys) means Phase 4 can add top-level package keys without forcing a mobile release. Only keys that the mobile needs to READ require a coordinated release.
- Full-snapshot storage means the server can always serve the latest package without assembly — it's pre-built at publish time. Serving is a single DB read + 200 response.
- At-most-2 coexistence is enforced by the device's `current`/`pending` slot model. No code path can create a third slot.

## Traces

- ADR: ADR-4 S6 (atomic delivery, at-most-2), S10 (all versions available)
- Constraint: C14 (all shape versions in package), C19 (DtV gates packager), C20 (atomic delivery, at-most-2)
- Depends on: IDR-017 (shape + expression_rules tables), IDR-018 (expression AST format)
- Walk-through: [s01-facility-observation-config-pipeline.md](../walk-throughs/s01-facility-observation-config-pipeline.md) §Stage 5, §Stage 6
- Files: `server/src/main/java/dev/datarun/server/config/ConfigPackager.java`, `server/src/main/java/dev/datarun/server/config/ConfigController.java`, `mobile/lib/data/config_store.dart`
