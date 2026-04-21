# Phase 3: Configuration

> The system becomes domain-agnostic. "A deployer defines a new data shape, publishes it, and field workers capture data with it — no code change."

**Exercises**: ADR-4 S1–S11, S13 (partial: field + predicate budgets only), S14 (partial: sensitivity only). ADR-5 S8 (`context.*` resolution). Contracts C5, C11, C12, C13, C14, C19, C20. IG-2, IG-6, IG-7, IG-8, IG-9, IG-15.

**Primitives built**: Shape Registry (authoring, versioning, deprecation-only evolution), Expression Evaluator (form L2 + trigger L3 contexts), Deploy-time Validator (hard budgets, composition rules), Config Packager (atomic delivery, at-most-2 versions).

**Contracts exercised**: C5 (PE → EE), C11 (ShR → PE), C12 (ShR → EE), C13 (ShR → DtV), C14 (ShR → CP), C19 (DtV → CP), C20 (CP → sync).

---

## 1. What Phase 3 Proves

Phase 3 proves the platform is domain-agnostic — that the data model, forms, validation, and sync are all driven by deployer-authored configuration, not hardcoded structures.

It validates:
- Shapes define event payloads and are versioned (S1, S10)
- Activities group shapes with role-action mappings (S2)
- The four-layer gradient (L0–L3) works in practice — L0 assembly, L1 shapes, L2 form logic (S9)
- Expressions evaluate correctly in form context (`payload.*`, `entity.*`, `context.*`) (S11)
- Expressions evaluate correctly in trigger context (`event.*`) — wired but not executed until Phase 4 (S11)
- `context.*` properties are pre-resolved and static during form fill (C5)
- Complexity budgets reject over-budget configurations at deploy time (S13)
- Configuration packages are delivered atomically; at most 2 versions coexist on device (S6)
- Projection routes on `shape_ref` version — multi-version events project correctly (C11)
- All shape versions remain valid forever — deprecated shapes still render and project (S10)

**What Phase 3 does NOT include**: No Trigger Engine execution (Phase 4). No Pattern Registry (Phase 4). No workflow state machines. No auto-resolution. Expressions are built and tested in both contexts, but L3 triggers don't fire. The `basic_capture/v1` hardcoded shape becomes a deployer-authored shape — that's the proof.

---

## 2. What's Settled (Not Design Questions)

| Point | Source | Implication |
|---|---|---|
| `shape_ref` mandatory, format `{name}/v{version}`, names `[a-z][a-z0-9_]*` | ADR-4 S1 | Envelope field already exists. Shape lookup key is defined. |
| `activity_ref` optional, groups shapes with role-action-pattern mappings | ADR-4 S2 | Activities are the deployer's organizational unit |
| 6-type ADR-4 vocabulary: capture, review, alert, task_created, task_completed, assignment_changed | ADR-4 S3 | Append-only. `review`, `alert`, `task_created`, `task_completed` ship with Phase 4 triggers. Phase 3 uses `capture` + `assignment_changed` (already implemented). Envelope vocabulary = 6 types. The four identity/integrity primitives (`conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`) are platform-bundled **shape names**, not envelope types — see [ADR-002 Addendum](../../adrs/adr-002-addendum-type-vocabulary.md). |
| System actor convention: `system:{source_type}/{source_id}` | ADR-4 S4 | Already implemented in Phase 1 |
| Triggers server-only (both L3a and L3b) | ADR-4 S5 | Device never evaluates triggers |
| Atomic config delivery, at-most-2 versions on device | ADR-4 S6 | Binary: old config until apply, then new config. No partial states. |
| 3 platform-fixed scope types: geographic, subject_list, activity. AND composition. | ADR-4 S7 | No deployer-authored scope logic. Already implemented in Phase 2. |
| No field-level sensitivity. Sensitivity at shape/activity level only. | ADR-4 S8 | 3 levels: standard, elevated, restricted |
| Four-layer gradient: L0 assembly, L1 shape, L2 logic, L3 policy | ADR-4 S9 | Phase 3 builds L0–L2. L3 execution is Phase 4. |
| Shapes: typed, versioned, authored as deltas, stored as snapshots. Deprecation-only evolution. | ADR-4 S10 | All versions valid forever. Breaking changes exceptional. |
| Expression language: operators + field refs, zero functions, two contexts | ADR-4 S11 | Form: `payload.*`, `entity.*`, `context.*`. Trigger: `event.*`. |
| L3a event-reaction + L3b deadline-check. Non-recursive. DAG max path 2. Output ≠ input type. | ADR-4 S12 | Phase 3 builds the expression evaluator that supports both contexts; Phase 4 wires the trigger execution. |
| Complexity budgets: 60 fields/shape, 3 predicates/condition, 5 triggers/type, 50 triggers/deployment, depth 2 | ADR-4 S13 | Hard limits, deploy-time enforced |
| Deployer-parameterized policies (not authored) | ADR-4 S14 | Policy logic is platform-defined patterns. Deployers configure parameters. |
| `context.*` 7 properties: `subject_state`, `subject_pattern`, `activity_stage`, `actor.role`, `actor.scope_name`, `days_since_last_event`, `event_count` | ADR-5 S8 | Platform-fixed. Read-only. Pre-resolved at form-open. |

---

## 3. Design Decisions — ALL RESOLVED

5 implementation design decisions. DD-1 through DD-3 are Locks resolved via walk-through + IDRs before building. DD-4 and DD-5 are Lean/Leaf resolved by approach direction.

### DD-1: Shape storage and versioning model — RESOLVED → [idr-017](../../decisions/idr-017-shape-storage.md)

> Investigation input: [phase-3-investigation-brief.md §DD-1](phase-3-investigation-brief.md#dd-1-feed-shape-storage--versioning) · Walk-through: [s01-facility-observation-config-pipeline.md](../../walk-throughs/s01-facility-observation-config-pipeline.md)

**Resolved choices:**

1. **`shapes` table with `(name, version)` PK**, JSONB `schema_json`. Each row is a standalone snapshot. No delta-application logic.
2. **10-type field vocabulary (Lock)**: `text`, `integer`, `decimal`, `boolean`, `date`, `select`, `multi_select`, `location`, `subject_ref`, `narrative`. Stress-tested against S01/S05/S07/S08/S22.
3. **Flat fields (Lock)**: No nested objects, no repeating groups. Line items handled by separate shapes + multiple events. Deliberate architectural choice.
4. **L1/L2 separation**: Expressions are external — stored in `expression_rules` table keyed by `(activity_ref, shape_ref, field_name, rule_type)`. Walk-through confirmed: same shape in different activities needs different expressions. Shape versions track structural change only.
5. **`subject_binding`**: Shape document names which `subject_ref`-typed field maps to envelope `subject_ref.id`.
6. **Field metadata**: `description`, `display_order`, `group`, `deprecated`, `options`, `validation` — all in field object.
7. **Activities table**: `name` PK, `config_json` JSONB. NOT individually versioned — config-package-level versioning.

See finalized schemas in §9.

### DD-2: Expression language grammar and evaluation — RESOLVED → [idr-018](../../decisions/idr-018-expression-grammar.md)

> Investigation input: [phase-3-investigation-brief.md §DD-2](phase-3-investigation-brief.md#dd-2-feed-expression-language-grammar) · Walk-through: [s01-facility-observation-config-pipeline.md](../../walk-throughs/s01-facility-observation-config-pipeline.md) §Stage 3

**Resolved choices:**

1. **JSON AST with prefix-operator nodes (Lock)**. Comparison nodes: `{ "eq": [left, right] }`. Logical nodes: `{ "and": [...] }`. 8 comparison operators (`eq`, `neq`, `gt`, `gte`, `lt`, `lte`, `in`, `not_null`), 3 logical (`and`, `or`, `not`).
2. **3 rule types**: `show_condition` (boolean), `warning` (boolean + message column), `default` (value via comparison or `ref` node).
3. **`ref` value node**: `{ "ref": "context.actor.scope_name" }` — the only way to produce non-boolean defaults. Passthrough value resolution.
4. **Null handling**: null in comparison → false (except `not_null`). Safe default.
5. **Type coercion**: string "3" vs number 3 → coerce, fail → false.
6. **Operator-type compatibility (DtV)**: ordering operators require numeric/date; `in` for set membership; `multi_select` left operand with `eq`/`neq` rejected.
7. **Trigger context restriction (DtV)**: `event.*` only — rejects `entity.*`, `context.*`, `payload.*`.
8. **Cross-platform equivalence**: 30+ shared JSON test fixtures, both evaluators must pass identically.
9. **Max constraints**: 3 predicates per condition, depth 1 (logical wrapping comparisons), zero functions, zero recursion.

See grammar summary and operator table in §9.

### DD-3: Config package format and sync delivery — RESOLVED → [idr-019](../../decisions/idr-019-config-package.md)

> Investigation input: [phase-3-investigation-brief.md §DD-3](phase-3-investigation-brief.md#dd-3-feed-config-package-format) · Walk-through: [s01-facility-observation-config-pipeline.md](../../walk-throughs/s01-facility-observation-config-pipeline.md) §Stage 5–6

**Resolved choices:**

1. **Separate endpoint** `GET /api/sync/config`. Discovery via `config_version` integer in sync pull response. Device compares, downloads if newer. Supports `If-None-Match` for 304.
2. **Monotonic version counter** stored in `config_packages` table. Full snapshot per row (no diffs).
3. **Package JSON keys**: `version`, `published_at`, `shapes` (keyed by `{name}/v{version}`), `activities` (keyed by name), `expressions` (keyed by `{activity_ref}.{shape_ref}` → array), `flag_severity_overrides`, `sensitivity_classifications`.
4. **Device two-slot model**: `current` + `pending`. Pending promoted to current at form-open (not mid-form). At-most-2 guaranteed.
5. **DtV → Packager pipeline**: Deployer edits granularly → clicks Publish → DtV validates full candidate → Packager assembles → stores as new version.
6. **Forward-compatible parsing**: Mobile ignores unknown top-level keys. Phase 4 additions (`triggers`, `projection_rules`) are additive.
7. **Config version tracking**: `device_sync_state.config_version` column tracks what each device has received.

### DD-4: Multi-version projection routing — RESOLVED (approach direction)

**Union projection**: PE treats all versions of a shape as the same logical entity. Missing fields default to null. Walk-through §Stage 8 confirmed: v1 events project alongside v2 events, missing fields = null. No version-routing logic needed. Breaking changes out of scope (IG-7 deferred).

### DD-5: Shape authoring UX — RESOLVED (approach direction)

**Field-list editor**: Table of fields with name, type, required, options. Server-rendered Thymeleaf (IDR-006). 10-type vocabulary with type pickers. "Create new version" copies current → edit → save as N+1. DtV runs on save, violations shown inline. JSON export/import for power users. Leaf decision — no downstream contracts.

---

## 4. Reversibility Triage (retroactive)

Classified at phase start, triaged before first DD resolved. All Lock DDs now have IDRs. See [execution-plan.md §6.1](../execution-plan.md#61-reversibility-triage-required-step) for the framework.

| Decision | Bucket | Why | Implemented as | Evolve-later trigger |
|----------|--------|-----|----------------|---------------------|
| DD-1: Shape storage schema | **Lock** | Table schema + shape JSON format persisted in DB, synced to devices. Changing = Flyway migration + mobile migration + config re-delivery. | IDR-017. `(name, version)` PK, JSONB snapshot. 10-type vocabulary. L1/L2 separated. | — |
| DD-2: Expression serialization format | **Lock** | Persisted in `expression_rules.expression` as JSONB, synced to mobile, evaluated cross-platform. Changing = data migration + two evaluator rewrites. | IDR-018. JSON AST with prefix-operator nodes. 8 comparison + 3 logical operators. `ref` value node. | — |
| DD-3: Config package wire format | **Lock** | Once mobile parses this, changing structure = coordinated server + mobile release. | IDR-019. Separate `/api/sync/config` endpoint. Monotonic version. Two-slot device model. | — |
| DD-4: Multi-version projection routing | **Lean** | Code-only. No schema change. PE already exists. Interface is C11. | Union projection — missing fields = null. No version-routing logic. | >3 versions of same shape AND projection errors in tests |
| DD-5: Shape authoring UX | **Leaf** | Thymeleaf HTML. Zero downstream contracts. Can be rewritten any time. | Field-list table with add/remove rows. | — |
| `context.*` caching (IG-6) | **Lean** | Internal to EE. Contract C5 defines the interface, not the caching strategy. | Compute all 7 properties at form-open. No cache. | Form-open latency >200ms on reference device |
| DtV error display (IG-9) | **Leaf** | Admin UI only. No contract. | Plain list of violations. | — |
| Expression evaluator internals | **Lean** | Code-only, behind the Lock serialization format (IDR-018). Interface is C5 + C12. | Hand-written switch/case on AST node type. No parser generator. | Operator count >15 or nesting depth >2 |
| Config version tracking | **Lean** | Server-internal. Device uses existing sync pull. No protocol change. | `device_sync_state.config_version` column. | — |

**Skill callouts**:
- DD-2 required **cross-platform expertise** (Java + Dart). The serialization format is a Lock — once expressions are persisted, changing the grammar means data migration. Resolved via IDR-018 with 30+ shared test fixtures for cross-platform equivalence.
- DD-1 and DD-3 are standard Lock decisions — careful design resolved via IDR-017 and IDR-019.

**March-forward priority**: DD-1 → DD-2 → DD-3 (sequential dependency). DD-4 and DD-5 can be resolved during implementation. IG-6, IG-9 don't need advance decisions — build the simplest thing.

---

## 5. Sub-Phases

### Phase 3a: Shape Registry + Basic Config Delivery

**Goal**: Shapes are stored, versioned, and delivered to devices. The hardcoded `basic_capture/v1` becomes a deployer-authored shape. S00 still works — P7 floor holds.

**Deliverables**:
- `shapes` table (DD-1 resolved)
- Shape CRUD API + admin UI for shape authoring (DD-5 resolved)
- Shape versioning: create version, deprecate version, list versions
- Config endpoint: `/api/sync/config` returns shape definitions
- Mobile config storage + retrieval during sync
- Mobile form engine reads shape from config instead of hardcoded definition
- PE routes on `shape_ref` version — multi-version events project correctly (DD-4 resolved)
- Activity definition storage (name, shapes, role-action mappings)
- Admin UI: activity management

**Quality gates**:
- [ ] Deployer creates shape `household_visit/v1` with 5 fields in admin UI → shape stored with correct schema
- [ ] Shape version created → device receives it at next sync via config endpoint
- [ ] Mobile form renders fields from config-delivered shape (not hardcoded)
- [ ] Mobile captures event with `shape_ref = household_visit/v1` → server validates payload against shape schema
- [ ] Mobile pushes event with payload that doesn't match `shape_ref` schema → server rejects with 400 (structural invalidity, not accept-and-flag — malformed payloads are not stale state)
- [ ] Shape name `Household-Visit` → DtV rejects ("invalid name format")
- [ ] Shape deprecated → hidden from new form list, existing events still project
- [ ] Create `household_visit/v2` adding optional field → v1 events still project correctly (union projection)
- [ ] Multi-version projection equivalence: server PE and mobile PE produce identical output for events spanning v1 and v2
- [ ] Activity created with shape + role-action mapping → device shows activity in work list
- [ ] Event captured within activity → `activity_ref` auto-populated in envelope (ADR-4 S2)
- [ ] Actor without `capture` action for activity → cannot create capture events for that activity
- [ ] Contract C11: PE routes on shape_ref version, all versions project
- [ ] Contract C14: config package includes all shape versions including deprecated
- [ ] No regression: all Phase 0+1+2 tests pass

### Phase 3b: Expression Evaluator + Deploy-time Validator

**Goal**: Forms become dynamic — show/hide fields, computed defaults, conditional warnings. Configuration is budget-checked at deploy time.

**Deliverables**:
- Expression language grammar (DD-2 resolved)
- Expression evaluator — Java (server) and Dart (mobile) implementations
- `context.*` property resolution from PE (IG-6)
- L2 form logic: show/hide conditions, computed defaults, conditional warnings
- Deploy-time Validator: field count, predicate count, all ADR-4 S13 budgets
- Admin UI: expression builder for shape fields
- Admin UI: validation error display (IG-9)

**Quality gates**:
- [ ] Expression `payload.age > 18` evaluates to true/false correctly on both server and mobile
- [ ] Cross-platform expression equivalence: shared test fixture of 20+ expressions → Java and Dart produce identical boolean results (same rigor as E7 projection equivalence)
- [ ] Expression referencing `context.subject_state` evaluates against PE-resolved value
- [ ] Expression referencing `context.actor.role` evaluates against assignment-derived value
- [ ] Expression referencing `context.event_count` evaluates against computed value
- [ ] `context.subject_pattern` and `context.activity_stage` resolve to null when no pattern configured → expressions handle null gracefully
- [ ] Expression evaluation works offline — `context.*` resolved from local projection, no server call
- [ ] Show/hide condition on field → mobile form hides field when condition is false
- [ ] Computed default expression → mobile form pre-fills field value
- [ ] Shape with 61 fields → DtV rejects with specific violation ("exceeds 60-field budget")
- [ ] Condition with 4 predicates → DtV rejects ("exceeds 3-predicate limit")
- [ ] Expression in trigger context (`event.*`) evaluates correctly on server (wired, not executed as trigger)
- [ ] Contract C5: PE provides pre-resolved `context.*` to EE, values static during form fill
- [ ] Contract C12: EE reads typed field definitions from shape
- [ ] Contract C13: DtV enforces field counts, predicate limits, and structural constraints from shape
- [ ] Contract C19: only validated configs reach the packager
- [ ] No regression

### Phase 3c: Config Packager + Full Pipeline

**Goal**: End-to-end config pipeline works. Deployer authors → validator checks → packager builds → device receives → field worker captures with new shape using dynamic forms.

**Deliverables**:
- Config Packager: assembles validated config into atomic payload
- Full config delivery via sync (DD-3 resolved)
- Device-side config management: active vs pending, transition timing
- At-most-2-versions coexistence on device
- Config version tracking (server knows what version each device has)
- Sensitivity classification on shapes/activities (standard/elevated/restricted)
- Full end-to-end test: author shape → validate → package → sync → capture → project

**Quality gates**:
- [ ] Deployer creates shape + activity + expressions → publishes config → device receives complete package at next sync
- [ ] Device with config v1 receives v2 → completes in-progress v1 form → switches to v2
- [ ] Device never has more than 2 config versions simultaneously
- [ ] Config with DtV violation → publish blocked, deployer sees specific error
- [ ] Sensitivity classification on shape → config package carries classification
- [ ] Flag severity overrides field present in config package (platform defaults; deployer override deferred to Phase 4)
- [ ] Contract C20: config delivered atomically, at-most-2 coexistence verified
- [ ] Full pipeline E2E: admin authors `malaria_followup/v1` → publishes → worker syncs → captures with new shape → server validates → PE projects → visible in admin
- [ ] No regression

---

## 6. Scope by Module

### contracts/

- Shape schemas (JSON Schema for shape definitions — the meta-schema)
- Activity definition schema
- Expression condition schema
- Config package schema

### server/config/ (new)

- ShapeRepository, ShapeService — CRUD, versioning, deprecation
- ActivityRepository, ActivityService — activity management
- ExpressionEvaluator — server-side evaluation (both contexts)
- DeployTimeValidator — budget enforcement, composition rules
- ConfigPackager — package assembly
- ConfigController — REST: `/api/sync/config`, admin CRUD endpoints

### server/event/

- EventRepository extended: payload validation against shape schema on push

### server/subject/

- SubjectProjection extended: multi-version shape routing

### server/resources/templates/

- Shape authoring UI (list, create, version, deprecate)
- Activity management UI
- Expression builder UI
- Config publish UI with DtV feedback

### mobile/data/

- ConfigStore — local config storage, version tracking, active/pending management
- SyncService extended: config check + download on sync cycle

### mobile/domain/

- ExpressionEvaluator — Dart-side evaluation (form context only)

### mobile/presentation/

- FormEngine extended: reads shape from config, evaluates L2 expressions
- WorkList extended: activities from config

---

## 7. Deferred Items

| Item | Reason | When |
|------|--------|------|
| Trigger Engine execution (L3a, L3b) | Phase 4. Expression evaluator supports trigger context but triggers don't fire. | Phase 4 |
| Auto-resolution policies | Phase 4. L3b sub-type. | Phase 4 |
| Pattern Registry | Phase 4. State machines, workflow patterns. | Phase 4 |
| Trigger budgets in DtV | S13 trigger limits (per-type ≤5, per-deployment ≤50, depth ≤2). No triggers to validate until Phase 4. | Phase 4 |
| Domain uniqueness constraints | S14 Q7. Requires server-side evaluation at sync + conflict flag infrastructure. | Phase 4 |
| Flag severity overrides (deployer config) | S14. Config package carries the field. Phase 3 populates platform defaults; deployer override UI ships with Phase 4 admin tooling. | Phase 4 |
| Sensitivity-based sync filtering | S14. Classification is on shapes/activities. Sync-scope filtering (actors need sensitive-access role) requires role-action tables at L0. | Phase 4 |
| Breaking change migration tooling (IG-7) | Deprecation-only is default. Tooling only if escape hatch triggers. | If needed |
| Device sharing / multi-actor config | IG-14. Per-actor config sessions. | Future |
| Pattern composition rules in DtV | ADR-5 S6. DtV checks budget dimensions in Phase 3; composition rules added when Pattern Registry ships in Phase 4. | Phase 4 |
| Deployer-authored projection rules | Projection rules are L1 artifacts (position B7: pure value-to-value lookup tables). Phase 3 PE uses hardcoded projection logic from Phases 0–2. Deployer-authored projection rules require storage, config delivery, and cross-platform evaluation. | Phase 4 |

---

## 8. Acceptance Criteria

1. **Domain-agnostic capture**: A deployer authors a new shape, publishes it, and a field worker captures data with it — zero code changes.
2. **Shape versioning**: Multiple shape versions coexist. Old events project correctly. Deprecated shapes hidden from new forms.
3. **Dynamic forms**: Show/hide conditions, computed defaults, and conditional warnings work on mobile using deployer-authored expressions.
4. **Cross-platform expressions**: Same expression evaluates identically on server (Java) and mobile (Dart).
5. **Budget enforcement**: DtV rejects configurations exceeding any hard limit. Violations are specific and actionable.
6. **Atomic config delivery**: Config package delivered as a unit. Device transitions cleanly. At most 2 versions coexist.
7. **Context properties**: `context.*` values (7 properties) are pre-resolved from PE and available in form expressions.
8. **No regression**: All Phase 0, 1, and 2 tests pass. Identity, integrity, and authorization remain intact.

---

## 9. Technical Specifications

Design details live in the IDRs. This section routes to canonical sources and captures cross-cutting specs not owned by a single IDR.

| Topic | Canonical source | Key sections |
|-------|-----------------|--------------|
| Shape table schema, `schema_json` structure | [IDR-017](../../decisions/idr-017-shape-storage.md) | §Table Schema, §Shape JSON Document Structure |
| Field type vocabulary (10 types — Lock) | [IDR-017](../../decisions/idr-017-shape-storage.md) | §Field Type Vocabulary |
| Flat fields rationale, `subject_binding` | [IDR-017](../../decisions/idr-017-shape-storage.md) | §Flat Fields, §subject_binding |
| L1/L2 separation (expressions external) | [IDR-017](../../decisions/idr-017-shape-storage.md) | §L1/L2 Separation |
| Activity definition, `config_json` | [IDR-017](../../decisions/idr-017-shape-storage.md) | §Activity Definition Storage |
| Expression AST grammar, operator table | [IDR-018](../../decisions/idr-018-expression-grammar.md) | §AST Node Types, §Grammar Summary |
| Rule types (`show_condition`, `warning`, `default`) | [IDR-018](../../decisions/idr-018-expression-grammar.md) | §Expression Rule Document Structure |
| `ref` value nodes, operator-type compatibility | [IDR-018](../../decisions/idr-018-expression-grammar.md) | §Value Expression Nodes, §Operator-Type Compatibility |
| Context properties (7 fixed), trigger context | [IDR-018](../../decisions/idr-018-expression-grammar.md) | §Context Properties, §Trigger Context |
| Cross-platform evaluation contract (30+ fixtures) | [IDR-018](../../decisions/idr-018-expression-grammar.md) | §Cross-Platform Evaluation Contract |
| Config package JSON structure, key layout | [IDR-019](../../decisions/idr-019-config-package.md) | §Config Package JSON Structure, §Package Structure Keys |
| Monotonic version, config endpoint, ETag/304 | [IDR-019](../../decisions/idr-019-config-package.md) | §Package Versioning, §Config Endpoint Design |
| Device two-slot model (`current`/`pending`) | [IDR-019](../../decisions/idr-019-config-package.md) | §Device-Side Config Management |
| DtV → Packager pipeline, forward compatibility | [IDR-019](../../decisions/idr-019-config-package.md) | §DtV → Packager Pipeline, §Forward-Compatibility |

### Deploy-time validation checks (cross-IDR synthesis)

| Check | Budget | Source |
|-------|--------|--------|
| Fields per shape | ≤ 60 | ADR-4 S13 |
| Predicates per condition | ≤ 3 | ADR-4 S13 |
| Shape name format | `[a-z][a-z0-9_]*` (rejected if invalid) | ADR-4 S1 |
| Shape version | Monotonically increasing positive integer | ADR-4 S1 |
| Field type | Must be in platform type vocabulary | ADR-4 S9 (L1) |
| Duplicate field names | Rejected within a shape version | — |
| Expression references | Must reference valid fields/context properties | C12 |
| Trigger-context expression scope | Expressions in trigger context must not reference `entity.*` or `context.*` | ADR-4 S11, IDR-018 |
| Operator-type compatibility | Ordering operators require numeric/date; `in` for set membership; `multi_select` with `eq`/`neq` rejected | IDR-018 |
| Default expression output type | `default` expression output type must match target field type | IDR-018 |
| Dependency cascade: deprecated field | Active expressions referencing a deprecated shape field → rejected | Expl 15 Check (b) |
| Dependency cascade: activity→shape | Activity referencing non-existent shape version → rejected | Expl 15 Check (b) |
| Activity shape references | Must reference existing shape versions | — |
| Sensitivity level | Must be `standard`, `elevated`, or `restricted` | ADR-4 S8 |

Trigger budgets (per-type ≤5, per-deployment ≤50, depth ≤2) deferred to Phase 4 when Trigger Engine ships.
Pattern composition rules (ADR-5 S6) deferred to Phase 4 when Pattern Registry ships.
Domain uniqueness constraints (ADR-4 S14 Q7) deferred to Phase 4 — requires both Pattern Registry and Trigger Engine for server-side enforcement.

---

## 10. Risks

| Risk | Severity | Mitigation |
|------|----------|------------|
| Cross-platform expression evaluation divergence | HIGH | IDR-018: 30+ shared JSON test fixtures. Both evaluators must pass identically. Same pattern as E7. |
| Config package size grows large with many shape versions | LOW | Shapes are small (≤60 fields). Even 50 versions of 10 shapes is <1MB JSON. Monitor but don't optimize prematurely. |
| Multi-version projection edge cases | MEDIUM | Deprecation-only evolution constrains the problem. Test with 3+ versions of same shape in projection suite. |
| `context.*` resolution performance | LOW | 7 properties, computed at form-open. PE already computes subject state. Cache per form session. |
| Shape authoring UX too complex for deployers | MEDIUM | Start with field-list editor, not visual form builder. JSON export for power users. Iterate based on feedback. |

**Risks retired by IDR resolution:**
- ~~Field type vocabulary too narrow~~ → IDR-017: 10 types stress-tested against S01/S05/S07/S08/S22.
- ~~L1/L2 boundary blurring~~ → IDR-017: expressions are external. Walk-through confirmed.
- ~~AP-6 overlapping authority~~ → IDR-017/019: shapes in `"shapes"`, expressions in `"expressions"`. Single authority per artifact.

**Anti-pattern test suite** (from exploration doc 13): DD investigations should verify against these 6 named anti-patterns from the ADR-4 exploration. AP-3 (Configuration Specialist Trap) and AP-6 (Overlapping Authority) are the active concerns for Phase 3:

| AP | Name | Phase 3 relevance |
|----|------|-------------------|
| AP-1 | Inner Platform Effect | Expression language has zero functions, 3-predicate max. Guard holds. |
| AP-2 | Greenspun's Rule | No functions, no loops. Guard holds. |
| AP-3 | Configuration Specialist Trap | **Active.** JSON AST format isn't deployer-friendly. Mitigated by visual condition builder (DD-5). Monitor. |
| AP-4 | Schema Evolution Trap | Deprecation-only + 60-field budget. Guard holds. |
| AP-5 | Trigger Escalation Trap | No triggers in Phase 3. Guard holds by absence. |
| AP-6 | Overlapping Authority | **Resolved.** IDR-017 separated L1/L2. IDR-019 package has single authority per key. |

---

## 11. Journal

(Entries added as work progresses.)
