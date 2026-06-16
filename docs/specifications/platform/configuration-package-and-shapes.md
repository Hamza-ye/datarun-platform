# Configuration Package And Shape Lifecycle

Status: accepted
Document type: platform_spec
Owner: config verifier
Source: NW-068 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-068-extract-configuration-durable-behavior.md`
Authority: CDL-038, CDL-039, CDL-041, CDL-044, and CDL-056; BAR-010; `contracts/shape-format.schema.json`; `contracts/config-package.schema.json`; `contracts/sync-protocol.md`; IDR-017 and IDR-019 as historical decision inputs
Last reviewed: 2026-06-16
Supersedes: none
Related: `docs/specifications/platform/expression-language.md`; `contracts/shape-format.schema.json`; `contracts/config-package.schema.json`; `contracts/sync-protocol.md`; `docs/decisions/idr-017-shape-storage.md`; `docs/decisions/idr-019-config-package.md`; `server/src/main/java/dev/datarun/server/config/ConfigPackager.java`; `server/src/main/java/dev/datarun/server/config/ConfigApiController.java`; `server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java`; `server/src/test/java/dev/datarun/server/config/ConfigIntegrationTest.java`; `server/src/test/java/dev/datarun/server/config/DeployTimeValidatorTest.java`; `mobile/lib/data/config_store.dart`; `mobile/test/config_store_test.dart`

## Purpose

This specification records accepted platform behavior for deployer-authored
form shapes, activity configuration, config publication, atomic package
delivery, and mobile config promotion.

It complements the contract files. It does not duplicate their JSON schemas,
change package shape, or create a new decision surface.

## Contract And Protocol Decision

`/api/sync/config`, bearer authentication, `ETag`, and `If-None-Match`
semantics belong in `contracts/sync-protocol.md`.

Reason: the endpoint is a process-boundary API under `/api/sync`, and pull
responses already advertise `config_version` for discovery. Keeping endpoint
auth and cache-validation semantics in the sync protocol prevents a parallel
contract note from splitting one sync/config delivery flow across two process
contracts.

The package body shape itself remains owned by
`contracts/config-package.schema.json`. The deployer-authored form shape DSL
remains owned by `contracts/shape-format.schema.json`.

## Shape Lifecycle

Deployer-authored form shapes are versioned by `shape_ref` in the format
`{name}/v{version}`. Names and versions are bounded by the shape-format and
config-package schemas.

Accepted behavior:

- each shape version is a complete snapshot, not a runtime delta;
- shape version structure is immutable once published;
- creating a new version stores a complete new snapshot;
- deprecated shape versions remain valid for old events and projection;
- deprecated versions are hidden from new capture selection;
- published packages include all deployer shape versions needed for historical
  rendering/projection, including deprecated versions;
- structural shape changes create shape versions, while expression-only changes
  do not;
- `subject_binding`, when present, names the required `subject_ref` field that
  populates the event envelope subject reference.

Shape fields are flat. Nested objects, repeating groups, and arrays of objects
are not part of the deployer shape format. The only array-valued form field type
is the bounded `multi_select` type described by the schemas.

Platform payload schemas under `contracts/shapes/` are not deployer-authored
form shapes. They are not deployer-editable shape rows, not delivered under the
config package `shapes` section, and not activity-bindable as form shapes.

## L1/L2 Separation

Form shapes define L1 structure. Expression rules define L2 form behavior.

Accepted behavior:

- expressions are external to shape definitions;
- expressions are keyed by activity plus shape reference in the package;
- the same shape version may have different expression rules in different
  activities;
- expression changes do not create new shape versions;
- shape definitions do not carry embedded expression authority.

The expression-language semantics are defined in
`docs/specifications/platform/expression-language.md`.

## Activities And Role Maps

Activities are package-level configuration artifacts. They are not individually
versioned as separate durable records. A published package captures the current
activity definitions at publish time.

The config package carries active activities, their referenced shape refs,
activity work-action role maps, sensitivity classification, and accepted
pattern-binding structure. Exact package fields are owned by
`contracts/config-package.schema.json`.

Activity role-action maps are activity work configuration. They do not define
assignment administration, custom scope logic, resolver authority, or trigger
execution.

## Publish Pipeline

Publication is an all-or-nothing validated pipeline:

1. Deployer-authored shapes, activities, expression rules, and supported
   configuration declarations are edited as granular source configuration.
2. Deploy-time validation evaluates the full candidate.
3. If validation fails, publish is blocked and no package version is created.
4. If validation passes, the packager assembles and stores one full package
   snapshot with a new monotonic version.

Required validation includes shape field budgets, field references, activity
shape references, activity work actions, expression grammar/type checks,
unknown `context.*` rejection, supported pattern bindings, severity overrides,
uniqueness declarations, sensitivity values, and dependency checks.

Structurally valid runtime state or policy anomalies are still handled by the
accepted event/flag model. This publish pipeline only controls whether
configuration can be published.

## Config Package

The config package is the server-emitted, mobile-consumed atomic configuration
snapshot. Its body shape is governed by `contracts/config-package.schema.json`.

Accepted behavior:

- package `version` is a server-managed monotonic integer;
- every published package is a full snapshot, not a diff;
- mobile must receive and apply a package atomically;
- known package sections are bounded by the schema;
- unknown top-level package keys are tolerated for forward compatibility;
- the `shapes` section contains deployer form shapes only;
- package preservation includes activities, role-action maps, expressions,
  severity overrides, sensitivity classifications, uniqueness declarations,
  pattern bindings, and referenced platform pattern definitions.

Unknown top-level key tolerance is not permission for deployer-authored
execution. New behavior still needs a routed platform or contract change before
mobile or server code relies on that key.

## Delivery And Discovery

Sync pull responses advertise the latest published config version as
`config_version`. Devices compare that value with their local current config
version and fetch `/api/sync/config` when they need the latest package.

The config endpoint:

- requires the same bearer authentication boundary as sync;
- returns the latest published full package on `200 OK`;
- returns an `ETag` derived from the package version;
- accepts quoted or unquoted `If-None-Match` version values;
- returns `304 Not Modified` with no package body when the client already has
  the latest version.

Device-reported config versions in pull requests are observability inputs for
sync/config progress tracking. They are not the source of configuration truth
and do not make per-device config variants.

## Mobile Promotion

Mobile config state uses a two-slot model:

| Slot | Role |
|---|---|
| `current` | Active config used to render forms and evaluate activity configuration. |
| `pending` | Downloaded package waiting for a safe promotion point. |

Accepted promotion behavior:

- a form in progress uses the config that was current when the form opened;
- a pending package is promoted to current at form-open, not mid-form;
- the device retains at most current plus pending config versions;
- if a download fails and current exists, the device continues with current;
- if no current config exists, configured capture cannot render until config is
  obtained.

Promotion behavior is local application behavior over the atomic package. It
does not change event truth, envelope fields, sync watermarks, or server
authorization.

## Acceptance Evidence

BAR-010 is the baseline evidence for this specification. It records deploy-time
validation, config endpoint auth/ETag behavior, monotonic atomic full snapshots,
shape version coexistence including deprecated versions, referenced-only
`pattern_definitions`, preservation of uniqueness/severity/sensitivity,
pull-response `config_version` discovery, device-reported config-version
tracking, mobile current/pending promotion, and unknown top-level package-key
tolerance.

NW-034 adds contract-hygiene evidence for
`contracts/shape-format.schema.json` and `contracts/config-package.schema.json`.
The implementation evidence remains in the referenced server and mobile tests;
this specification is the durable behavior target, not a replacement for those
tests.

## Classification Of IDR-017 And IDR-019 Details

| Detail | Durable classification |
|---|---|
| Shape refs, versioned full snapshots, deprecation-only evolution, all versions remaining valid, flat fields, subject binding, L1/L2 separation | Accepted platform behavior. |
| Deployer shape DSL field structure and type vocabulary | Existing contract authority in `contracts/shape-format.schema.json` and `contracts/config-package.schema.json`. |
| Config package top-level keys, known section structure, platform payload shape exclusion from package `shapes`, and unknown top-level key tolerance | Existing contract authority in `contracts/config-package.schema.json`. |
| Pull `config_version`, `/api/sync/config`, bearer auth, ETag, and `If-None-Match` behavior | Existing process-boundary authority in `contracts/sync-protocol.md` after NW-068. |
| Table names, columns, indexes, admin UI forms, and exact controller/store class layout | Implementation evidence only. |
| JSON-file versus SQLite storage for mobile config blobs | Implementation evidence only; the accepted behavior is two-slot atomic promotion. |
| IDR-019 package-key examples that omit later accepted `pattern_definitions` | Old-doc trace only; current contract authority is `contracts/config-package.schema.json`. |
| IDR-019 statement that a config endpoint `200` response updates device sync state | Old-doc trace only; accepted behavior records device-reported config versions through pull observability, not endpoint-body truth. |
| IDR-017 Phase 3 wording that treats uniqueness as only a stub | Old-doc trace only; current contracts preserve accepted uniqueness declarations, while full flag/detection behavior belongs to the integrity/flag surfaces. |

## Non-Goals

This spec does not authorize:

- deployer-authored code, functions, triggers, dynamic queries, joins, or
  recursion;
- deployer-authored custom scope or containment logic;
- per-device or per-actor config package variants;
- platform payload schemas as deployer form shapes;
- envelope field or envelope type changes;
- field-level sensitivity, encryption, redaction, retention, or export
  behavior beyond package preservation of current shape/activity sensitivity;
- mobile authoritative rejection of structurally valid events that the server
  accepts and flags.

## Escalation Triggers

Route a successor platform, contract, or architecture decision before:

- changing config package wire shape or known top-level keys;
- changing unknown-key tolerance;
- adding a deployer shape field type;
- changing shape ref format, version lifecycle, or deprecation semantics;
- adding nested/repeating form structures;
- allowing expressions inside shape definitions;
- introducing per-device config variants;
- changing config discovery, auth, ETag, or promotion semantics;
- treating config as authority for sync scope, resolver routing, assignment
  containment, trigger execution, or event mutation.
