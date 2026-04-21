# Phase 3 Investigation Brief — Configuration Territory

> Extracted from ADR-004 exploration files (docs 15, 16, 17, 18).
> Purpose: Feed DD-1 through DD-5 investigations with conclusions the explorations established but that aren't obvious from reading the ADR alone.
> Lifecycle: Investigation input → consumed by DD work → resolved into IDRs → sections marked `→ resolved by IDR-0XX`.
> **Status**: All DDs resolved. DD-1 → IDR-017, DD-2 → IDR-018, DD-3 → IDR-019, DD-4 → union projection (approach direction), DD-5 → field-list editor (Leaf). This document is now reference material — do not use `[INVESTIGATE]` tags as open questions.

---

## How to Use This Document

Each section maps to a DD in phase-3.md. When investigating a DD:
1. Read this section first — it establishes the ground you're building on
2. What's marked **[ESTABLISHED]** is exploration-confirmed constraint, not open for re-investigation
3. What's marked **[INVESTIGATE]** is the actual open question the DD must resolve
4. When the DD resolves into an IDR, mark the section here with `→ resolved by IDR-0XX`

---

## Layer Territory (Precise Boundaries)

Before diving into DDs, the four layers have sharper boundaries than the ADR text conveys. The explorations stress-tested each layer's exact territory:

**L0 — Assembly** (no runtime behavior, no expressions, no side effects)
- Activity definitions: name, shapes, role-action mappings, pattern ref, sensitivity
- Flag severity overrides: per-deployment flat key→severity map (exploration 18 Q9)
- Sensitivity classification on activities: `standard`/`elevated`/`restricted` (exploration 18 Q12)
- Scope type composition on assignments: geographic × subject_list × activity (exploration 18 Q10, ADR-4 S7)
- Deployment parameters
- **Owner persona**: program manager

**L1 — Shape** (declarative only — NOT an expression language)
- Field definitions: `{name, type, required, options, validation_params}`
- **[ESTABLISHED]** L1 validation is parameter constraints ONLY: `{min, max, required, options}` — no conditionals, no field references, no boolean operators (exploration 15 Check d)
- Projection rules: pure value-to-value lookup tables, no conditionals, no expressions (position B7)
- Uniqueness constraints: declarative `{scope, period, action}` — no expressions, no trigger logic (exploration 18 Q7a)
- Sensitivity classification on shapes
- **Owner persona**: configuration specialist
- **Key distinction**: L1 declares structure. It never evaluates at runtime. This means L1 validation parameters (`min: 1, max: 50`) are NOT the same thing as L2 expressions (`payload.age > 18`). They look similar but live in different layers with different semantics.

**L2 — Logic** (form-scoped runtime, no persistent side effects)
- Show/hide conditions, computed defaults, conditional warnings
- Expression language: operators + field refs, zero functions, ≤3 predicates
- Form-context references: `payload.*`, `entity.*`, `context.*` (7 pre-resolved properties)
- Expression outputs: booleans (conditions) AND typed values (defaults/set)
- Evaluates on device during form interaction
- **Owner persona**: configuration specialist
- **Key distinction**: L2 creates no persistent records. It affects what the user SEES, not what gets stored.

**L3 — Policy** (system-scoped runtime, creates events — Phase 4 execution)
- Event-reaction triggers (3a, sync-time), deadline-check triggers (3b, async)
- Same expression grammar as L2, different reference scope: `event.*` only
- **[ESTABLISHED]** Trigger context REJECTS any expression containing `entity.*` references (exploration 15 Check d). This is a DtV check.
- payload_map is a SEPARATE mini-DSL, not the expression language: static values + `$source.` field refs only, no operators, no conditions. Max depth: `$source.` + one of {envelope field | `payload.<field>`}
- Evaluates server-only (position B3, ADR-4 S5)
- **Key distinction**: L3 is the ONLY config layer that creates persistent records in the event store.

---

## DD-1 Feed: Shape Storage & Versioning → resolved by IDR-017

### Artifact Lifecycle Context

**[ESTABLISHED]** Shapes are the only event-coupled artifact (exploration 15 Check a). Two lifecycle models:

| Model | Artifacts | Versioned? | Referenced from events? |
|-------|-----------|-----------|------------------------|
| **Event-coupled** | Shapes only | Yes, explicitly via `shape_ref` | Yes, mandatory |
| **Config-package** | Activities, logic rules, triggers, projection rules | No individual versioning — package-level version only | No (or ID-only via `activity_ref`) |

DD-1 designs shape storage. DD-3 designs everything-else delivery. They share a DB but have different lifecycle requirements.

### L1/L2 Separation — The Exploration's Strongest Signal

**[INVESTIGATE]** → **Resolved by IDR-017**: Expressions are external. `expression_rules` table keyed by `(activity_ref, shape_ref, field_name, rule_type)`. Walk-through confirmed: same shape in different activities needs different expressions.

Evidence for separation (expressions as standalone L2 artifacts linked by activity + shape_ref):

1. **Multiple activities sharing one shape is a confirmed supported pattern** (exploration 17 Attack 1). Example: `vaccination_record/v1` used by both `routine_immunization` and `measles_campaign`. If expressions are embedded in the shape, they apply identically across all activities — activity-specific form logic becomes impossible.

2. **Version churn**: A show/hide change in L2 would force a new shape version even though the shape itself (L1) didn't change. Shape versions are in the event envelope (structural constraint); unnecessary version bumps waste an irreversible resource.

3. **DtV separation**: Structural validation (L1 — field counts, types, names) and logic validation (L2 — expression references, predicate counts) are conceptually distinct checks. Separating the artifacts makes DtV simpler and error messages clearer.

Evidence against separation:

1. **Simplicity**: One artifact is simpler to author, store, deliver, and reason about than two linked artifacts. Deployers think in "forms," not "shapes + expressions."

2. **Config package complexity**: If expressions are external, the authority relationship between shape fields and expression references must be managed. The config package must wire them at delivery time.

**Design direction**: The shape JSON structure should be designed to work either way. If expressions are inline, the config package's top-level `"expressions"` key is redundant. If expressions are external, that key becomes the authoritative home for L2 artifacts. Resolve before locking DD-3.

### Field Type Vocabulary

**[ESTABLISHED]** Field types are a shape-internal concern from ADR-4 S10 (shape definition) and S9 (L1 typed structure). NOT from S3 (S3 is the 6 structural event types).

**[INVESTIGATE]** → **Resolved by IDR-017**: 10 types: `text`, `integer`, `decimal`, `boolean`, `date`, `select`, `multi_select`, `location`, `subject_ref`, `narrative`. Starting point from exploration + scenario pressure:

| Type | Status | Evidence |
|------|--------|----------|
| `text` | Baseline | S00 |
| `integer` | **[INVESTIGATE]** | S07 (counting distributed items — 50 nets, 12 vials). Distinct from decimal: form input is whole numbers, validation rejects decimals. |
| `decimal` | **[INVESTIGATE]** | S22 (measuring quantities — weight 12.4kg, height 87.2cm). Needs precision handling. |
| `boolean` | Baseline | S00 |
| `date` | Baseline | S00 |
| `select` | Baseline | S00. Single selection from options list. |
| `multi_select` | **[INVESTIGATE]** | S05 (audit checklists — multiple items checked), S20 (symptom lists — multiple symptoms selected). Could be `select` with `multiple: true` flag instead. |
| `location` | Baseline | S03 (assignment scope). Location UUID referencing location hierarchy. |
| `subject_ref` | **Required** | S01 (entity-linked capture), S22 (parent-child subject links). Without this, the second scenario cannot work. |
| `narrative` | **[INVESTIGATE]** | S08 (case notes), S04 (supervisor comments). Could be `text` with `multiline: true` flag. |

The type set is a **Lock** — persisted in shape definitions, parsed by both evaluators and form engines. Types can be added later (JSON field, no DB migration) but require evaluator + form engine + DtV updates on both platforms. Better to start with the right set.

**[ESTABLISHED]** `number` should NOT be a single type — the investigation should resolve whether to split into `integer`/`decimal` or keep `number` with a `precision` parameter.

### Flat Fields — Confirmed Deliberate

**[ESTABLISHED]** Repeating groups fight the 60-field budget, break `payload.*` flat references in expressions, and add cross-platform complexity. The event-sourced architecture's natural composition (multiple shapes per activity, multiple events per subject) handles line-items (S07/S14/S22). This should be documented as a Lock-level decision in DD-1.

### Uniqueness Constraints in Shape Structure

**[ESTABLISHED]** Exploration 18 Q7a placed uniqueness at L1 — a declarative block on the shape definition:

```json
"uniqueness": {
  "scope": ["subject_ref", "activity_ref"],
  "period": "calendar_week",
  "action": "warn"
}
```

No expressions. No trigger logic. Pure declarative constraint. DD-1's shape JSON must include this field (nullable for Phase 3, populated Phase 4). Server-side evaluation at sync time reuses ADR-2's conflict detection infrastructure — violations produce `ConflictDetected` events with `domain_uniqueness_violation` flag type.

### Field-Level Metadata Extensibility

**[ESTABLISHED]** The base field object `{name, type, required}` must be extensible for: `options` (select), `validation` (min/max — L1 parameter constraints), `deprecated` (S06 shape evolution), display hints (`description`, `display_order`, `group` for visual form sectioning). Use an open object structure — don't constrain future field properties with a rigid schema.

### Deprecation Status Tracking

**[ESTABLISHED]** S10 deprecation-only evolution. Shape status must be tracked: `active`/`deprecated`. Deprecated shapes remain in registry forever, hidden from new-form creation, still valid for projection and historical rendering.

---

## DD-2 Feed: Expression Language Grammar → resolved by IDR-018

### Grammar Size

**[ESTABLISHED]** The grammar is trivially small — by design. 3-predicate limit means no recursion, no nesting beyond AND/OR of 3 leaf comparisons. A hand-written evaluator suffices — no parser generator needed (position B5).

### Two Output Types

**[ESTABLISHED]** Exploration 14 (S20 walkthrough): conditions (`when:`) produce booleans. Computed defaults/set-values produce typed values. Example: `{ "set": { "referral": true } }`. DD-2 must design for both condition evaluation (boolean) and value computation (typed).

### The Lock Is the Serialization Format

**[ESTABLISHED]** The evaluator implementations are Lean (behind the format). The JSON AST structure is the Lock — persisted in DB, synced to mobile, evaluated cross-platform. Changing it = data migration + two evaluator rewrites.

**[INVESTIGATE]** → **Resolved by IDR-018**: JSON AST with prefix-operator nodes. 8 comparison + 3 logical. See IDR-018 for full grammar. The concrete AST node types. The exploration established the operator set:
- **Comparison**: `eq`, `neq`, `gt`, `gte`, `lt`, `lte`, `in`, `not_null`
- **Logical**: `and`, `or`, `not` (max 3 predicates per condition)
- **References**: `payload.{field}`, `entity.{field}`, `context.{property}`, `event.{field}` (trigger context only)
- **All references prefixed** — no bare field names

### Context Resolution

**[ESTABLISHED]** `context.*` is pre-resolved, read-only, static during form fill. The expression evaluator reads 7 pre-computed values from PE — it does NOT compute them. `entity.*` attributes (like `entity.age_months`) are also projection-derived. The evaluator is a value-lookup + comparison engine, not a computation engine.

### Trigger Context Scope Restriction

**[ESTABLISHED]** Trigger context accepts `event.*` only — rejects `entity.*`. This is a deploy-time validation check (DtV), not a runtime check. An expression tagged for trigger context containing `entity.*` → DtV rejects before packaging.

### payload_map Is NOT the Expression Language

**[ESTABLISHED]** payload_map is a separate, intentionally minimal DSL for trigger output construction (Phase 4). Two constructs only: static values and `$source.` field references. DD-2 should design the expression AST only; payload_map is a Phase 4 concern with its own simpler format.

### Survival Across L1/L2 Separation

**[INVESTIGATE]** → **Resolved by IDR-018 + IDR-017**: Expressions are external to shapes. Format supports standalone artifacts referencing shape + field via `expression_rules` table key. If expressions are external to shapes (DD-1 outcome), the serialization format must support standalone expression artifacts referencing a shape + field. Design for the external case — inline becomes trivial. The format should include a `shape_ref` + `field_name` binding, not assume embedding.

---

## DD-3 Feed: Config Package Format → resolved by IDR-019

### Package Versioning

**[INVESTIGATE]** → **Resolved by IDR-019**: Monotonic integer counter. `config_packages` table stores version + full snapshot. Device compares `config_version` from sync pull response. Exploration 15 flagged this explicitly as under-specified: "How does a device know which config version it's running?" Options: monotonic counter or content hash. Both are valid. DD-3 picks.

### Forward-Compatibility Stubs

**[ESTABLISHED]** These fields must exist in the package structure now (nullable/empty) to avoid schema migration when Phase 4 populates them:
- `pattern: null` on activities
- `uniqueness: null` on shapes
- `flag_severity_overrides: {}` on package
- `sensitivity_classifications: {}` on package

### Authority Resolution for Expressions

**[INVESTIGATE]** → **Resolved by IDR-019**: Expressions are external (IDR-017). Package `"expressions"` key is authoritative home for L2 artifacts, keyed by `{activity_ref}.{shape_ref}`. Depends on DD-1's L1/L2 separation outcome. If expressions are separated, the package's top-level `"expressions"` key is the authoritative home for L2 artifacts. If inline, that key is either redundant or a denormalized index. DD-3 must wait for DD-1.

### Atomicity

**[ESTABLISHED]** Granular authoring, atomic delivery. Deployer edits individual artifacts. Published package is always complete. DtV validates the whole package as a unit. Device receives one document, stores it, switches when ready.

### Device Transition Timing

**[ESTABLISHED]** "New config applied after in-progress work under previous config completes" = switch at form-open, not mid-form. Device stores `{current, pending}`. Pending becomes current when no form is open. Old current is discarded.

### Config Endpoint

**[INVESTIGATE]** → **Resolved by IDR-019**: Separate endpoint `GET /api/sync/config`. Discovery via `config_version` integer in sync pull response. Separate endpoint vs. piggyback on sync pull. The exploration flagged both as viable. A version number in the sync pull response suffices to trigger a config download. DD-3 picks.

---

## DD-4 Feed: Multi-Version Projection Routing

### Deprecation-Only Makes This Simple

**[ESTABLISHED]** Deprecation-only evolution (S10) means v2 is always a superset of v1. Missing fields in older events default to null. No field type changes or removals to handle (breaking changes are exceptional, out of Phase 3 scope).

**[INVESTIGATE]** → **Resolved (approach direction)**: Union projection. Deprecation-only makes version-routing unnecessary. Missing fields = null. Whether PE needs version-routing logic (handle each version differently) or can use union projection (treat all versions of a shape as the same logical entity, missing fields = null). The exploration evidence favors union projection given the deprecation-only constraint.

---

## DD-5 Feed: Shape Authoring UX

This is a Leaf decision — no downstream contracts, no exploration constraints beyond what's already in ADR-4. Phase-3.md's current approach direction (field-list editor, Thymeleaf) is sufficient. No investigation brief needed.

---

## Cross-DD Constraints

### Dependency Cascade Validation

**[ESTABLISHED]** Exploration 15 Check (b): all dependency violations caught at deploy-time, never runtime. The dependency graph:

```
Shape ◄── Logic Rule    (field references)
Shape ◄── Activity      (shape by name)
Shape ◄── Trigger       (condition field refs + output shape)
Shape ◄── Projection    (source shape + field refs)
```

When a shape field is deprecated, DtV must verify no active expression or projection rule references it. When a shape is retired, DtV must verify no active activity references it. The package is rejected with specific broken-reference messages.

**[INVESTIGATE]** → **Deferred to Phase 4 (added to phase-3.md §7)**. How DtV traverses the dependency graph. This is a Phase 3 implementation concern — the dependency types relevant to Phase 3 are shape→expression and activity→shape. Trigger and projection dependencies arrive in Phase 4.

### Deployer Tooling for Dependency Visibility

**[ESTABLISHED]** Exploration 15 Check (b) — AP-3 mitigation: "When a deployer changes a shape, the authoring tool should surface which logic rules, triggers, and projection rules reference fields being modified." This is tooling, not architecture, but important for the admin UI to implement.

### Four Device Engines — Config Must Satisfy Exactly These

**[ESTABLISHED]** Exploration 15 Check (c) revised the device contract:

| Engine | Input from config |
|--------|------------------|
| Form engine | Shape definition + L2 logic rules |
| Validation engine | Shape definition (L1 constraints) |
| Event store | (no config input — stores validated events) |
| Projection engine | Projection rules + shape field definitions |

The config package must satisfy these four engines and no more. No trigger engine on device. No scope resolver on device (beyond locally cached assignments).

### Projection Rules — Phase 3 or Phase 4?

**[INVESTIGATE]** → **Deferred to Phase 4 (added to phase-3.md §7)**. Projection rules are L1 artifacts (exploration position B7: pure value-to-value lookup tables). Phase 3 builds L0-L2. But deployer-authored projection rules may be more complex than the hardcoded PE logic built in Phases 0-2. If Phase 3, DD-1 must include projection rule storage and DD-3 must include them in the config package. If Phase 4, add to deferred items.

---

## Conclusions from Explorations Not Explicit in ADR-004

These are positions the explorations arrived at that Phase 3 should treat as established ground:

| # | Conclusion | Source | Impact |
|---|-----------|--------|--------|
| 1 | L1 validation is declarative parameters, NOT expressions | Expl 15 Check (d) | DD-1: shape `validation` field is `{min, max}` params |
| 2 | payload_map is a separate DSL, not the expression language | Expl 15 Check (d) | DD-2: don't design payload_map into the expression AST |
| 3 | Projection rules are pure lookup tables, no conditionals | Position B7 | DD-1: projection rules are `{source_value → output}` maps |
| 4 | Device runs exactly 4 engines | Expl 15 Check (c) | DD-3: config package scope |
| 5 | Multiple activities sharing one shape is supported | Expl 17 Attack 1 | DD-1: strongest evidence for L1/L2 separation |
| 6 | `entity.*` attributes are projection-derived, not expression functions | Expl 15 Check (d) | DD-2: evaluator reads pre-computed values |
| 7 | Dependency violations caught at deploy-time only | Expl 15 Check (b) | DD-1/DD-3: DtV validates entire dependency graph |
| 8 | Flag severity is per-deployment, not per-activity | Expl 18 Q9 | DD-3: config package carries flat map |
| 9 | Uniqueness is at L1 (shape), not L3 (trigger) | Expl 18 Q7a | DD-1: shape structure includes `uniqueness` field |
| 10 | Sensitivity is config metadata, no new envelope fields | Expl 18 Q12 | DD-3: sensitivity rides config, not events |

---

## What to Leave to Design

The explorations deliberately left these for implementation to resolve — they're design art, not constraints:

1. **Shape JSON internal structure** — exact nesting, property ordering, optional-vs-required
2. **Expression AST node types** — the concrete JSON structure for operators and operands
3. **Config endpoint design** — separate endpoint vs. piggyback on sync pull
4. **Admin UX** — field-list editor, visual condition builder, DtV error display
5. **Delta→snapshot authoring** — how the admin UI presents "create new version"
6. **Config version identifier** — monotonic counter vs. content hash
