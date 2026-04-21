> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-004**. For a navigated reading guide, see [guide-adr-004.md](../guide-adr-004.md).
# ADR-004 Session 3, Part 2: Irreversibility Filter

> Phase: 3.2 of 4 — Irreversibility Filter
> Date: 2026-04-12
> Input: Session 2 positions (14-adr4-session2-scenario-walkthrough.md §9.1), Part 1 structural coherence audit (15-adr4-session3-part1-structural-coherence.md)
> Purpose: Apply the three-test irreversibility filter to every ADR-4 position. Classify each as structural constraint (envelope-touching, irreversible), strategy-protecting constraint (server-side, guards an invariant), or initial strategy (evolvable). Only envelope-touching items get full adversarial treatment in Part 3.

---

## Method

Same methodology proven in ADR-3's course correction (12-adr3-course-correction.md): apply the three-test filter to every position, sort what's permanent from what's evolvable, scope the stress test proportionally.

**The three tests, applied in order:**

| Test | Question | Yes = irreversible | No = probably evolvable |
|------|----------|-------------------|----------------------|
| **Stored state** | If I change this after 1000 devices have been offline for 3 weeks, do I need to transform stored data? | Constraint | Strategy |
| **Contract surface** | How many components must agree on this? (>2 = higher irreversibility) | Constraint | Strategy |
| **Wrong choice recovery** | What does recovery require — data migration, protocol change, or code change? | Migration/protocol = constraint. Code = strategy. | |

---

## Complete Inventory of ADR-4 Positions

Every position formed in Sessions 1, 2, and 3 Part 1, organized by category:

### Category A: Envelope positions (potentially irreversible)

| # | Position | Source |
|---|----------|--------|
| A1 | `shape_ref` as mandatory envelope field, format `"{shape_name}/v{version}"` | Session 2, Q3 |
| A2 | `activity_ref` as optional envelope field (null when shape disambiguates) | Session 2, Q11 |
| A3 | `type` field uses a platform-fixed closed vocabulary of structural event types | Session 2, Q1 |
| A4 | System-generated events use `system:trigger/{trigger_id}` as `actor_ref` | Part 1, Check (f) |

### Category B: Configuration architecture (potentially constraining)

| # | Position | Source |
|---|----------|--------|
| B1 | Four-layer gradient: L0 Assembly → L1 Shape → L2 Logic → L3 Policy | Session 1 |
| B2 | L3 split into 3a (event-reaction, sync) and 3b (deadline-check, async) | Session 2 |
| B3 | All triggers (3a and 3b) are server-only | Part 1, Check (c) |
| B4 | One expression language, two evaluation contexts (form, trigger) | Part 1, Check (d) |
| B5 | Expression language: zero functions, operators + field references only | Part 1, Check (h) |
| B6 | Payload mapping: static values + source field references only, max depth from type system | Part 1, Check (h) |
| B7 | Projection rules: pure value-to-value lookup tables (no conditionals) | Part 1, Check (g) |
| B8 | Trigger dependency graph: DAG, max path length 2 | Part 1, Check (h) |
| B9 | Shapes authored as deltas, stored as full snapshots | Session 2 |
| B10 | Change classification: additive / deprecation / breaking | Session 2 |
| B11 | Config delivered as atomic package at sync, applied after in-progress work | Session 2 |
| B12 | Device holds at most 2 config versions (current + previous) | Session 2 |

### Category C: Platform capability architecture

| # | Position | Source |
|---|----------|--------|
| C1 | Patterns are a platform-fixed closed set, parameterized by deployers | Part 1, Check (e) |
| C2 | Three orthogonal platform capabilities: aggregate projection, target comparison, time windowing | Part 1, Check (i) |
| C3 | Escalation depth: max 2 levels (hard wall) | Session 2 |
| C4 | Campaign progress treated as composition of platform capabilities, not monolithic feature | Part 1, Check (i) |

### Category D: Boundary decisions

| # | Position | Source |
|---|----------|--------|
| D1 | T2 boundary line confirmed at L3 → code | Session 2 |
| D2 | Side effect definition: "creates persistent record in event store" (L2 = form-scoped, L3 = system-scoped) | Part 1, Check (g) |
| D3 | Complexity budgets: max 60 fields/shape, max 3 predicates/condition, max 5 triggers/event type, max 50 triggers/deployment | Session 1.5 |
| D4 | Platform capability elevation principle: cross-subject aggregation = platform, per-subject = configuration | Part 1, Check (i) |

---

## Irreversibility Filter: Position by Position

### A1: `shape_ref` — mandatory envelope field, format `"{shape_name}/v{version}"`

| Test | Assessment |
|------|-----------|
| **Stored state** | Every event carries `shape_ref`. After 1000 offline devices sync, all their events have this format. Changing the format means: (a) rewriting every event in the store, or (b) maintaining two parsers forever. Both are migration-level costs. **YES — irreversible.** |
| **Contract surface** | Components that parse/use `shape_ref`: event store (indexing), projection engine (field resolution), shape registry (version lookup), sync layer (config dependency), form engine (schema loading), validation engine (field checking), conflict detector (shape-aware comparison). **7 components.** Extremely high coupling. |
| **Wrong choice recovery** | If the format is wrong: data migration (rewrite events) + protocol change (all components update parsing) + device update (all deployed devices must understand new format). **Full migration.** |

**Classification: STRUCTURAL CONSTRAINT — highest irreversibility.**

**What exactly is being locked?**

1. The field name: `shape_ref` (in the envelope)
2. The field presence: mandatory (every event, always)
3. The field format: a string that encodes both shape identity and version

**What remains evolvable despite this lock?**

- The shape CONTENTS (field definitions) can change freely via versioning
- The shape REGISTRY (how shapes are stored, synced, managed) is server-side, evolvable
- New shapes can be added without touching existing events
- The authoring format (YAML, JSON, visual builder) is completely separate

**Format deep-dive: Is `"{shape_name}/v{version}"` the right format?**

The format must satisfy:
1. **Parseable** — any component can extract shape name and version from the string
2. **Collision-free** — no two shapes produce the same `shape_ref`
3. **Human-readable** — inspectable in logs, debug tools, manual database queries
4. **Extensible** — can accommodate future needs without format-breaking changes

Testing the proposed format `"household_observation/v1"`:
- Parse: split on `/v` → name=`household_observation`, version=`1`. **Simple.** But: what if a shape name contains `/v`? E.g., shape `intake/visit` → `shape_ref: "intake/visit/v1"` → ambiguous parse (is it shape=`intake/visit`, version=`1` or shape=`intake`, version=`isit/v1`?).
- Fix: either (a) forbid `/` in shape names (safe, restrictive), or (b) use a different separator.

**Alternative formats considered:**

| Format | Example | Parse safety | Readability | Extensibility |
|--------|---------|-------------|-------------|---------------|
| `{name}/v{N}` | `household_observation/v1` | ⚠️ Ambiguous if name contains `/v` | ✅ Best | ⚠️ Adding namespace requires format change |
| `{name}:v{N}` | `household_observation:v1` | ✅ `:` forbidden in identifiers | ✅ Good | ⚠️ Same limitation |
| `{name}@{N}` | `household_observation@1` | ✅ `@` forbidden in identifiers | ✅ Good | Add namespace: `ns.name@1` |
| `{name}#{N}` | `household_observation#1` | ✅ `#` forbidden in identifiers | ⚠️ `#` has fragment semantics | — |
| structured object | `{"name":"...","version":1}` | ✅ Unambiguous | ❌ Verbose, harder to scan | ✅ Add any field | 

The key question: is future namespace extensibility an irreversibility concern?

**Scenario**: Organization A deploys shapes `patient_encounter/v1`. Later, Organization B deploys on the same server instance with their own `patient_encounter/v1`. Shape name collision.

**Does this happen?** In a multi-tenant server, yes. In a single-deployment server, no (one deployer controls all shape names). 

**Resolution**: If multi-tenancy is a future need, namespacing CAN be added as a prefix to the shape name itself (e.g., `org_a.patient_encounter@1` vs. `org_b.patient_encounter@1`). The separator between name and version is orthogonal to namespacing — namespacing lives in the naming convention, not in the version separator.

**Position: `{name}@{version}` is safer than `{name}/v{version}`.** The `@` separator:
- Cannot appear in identifiers (shape names are alphanumeric + underscore)
- Parses unambiguously: last `@` splits name from version
- Is short and readable
- Does not conflict with URI, file path, or fragment semantics

**However**: the format difference (`/v` vs. `@`) is cosmetic if shape names are constrained. If shape names are limited to `[a-z][a-z0-9_]*` (lowercase, alphanumeric, underscores only), then `/v` is also unambiguous because `/` cannot appear in the name.

**Commitment: format = `{shape_name}/v{version}` WITH a shape naming constraint: shape names must match `[a-z][a-z0-9_]*` (lowercase start, alphanumeric + underscores only).** The naming constraint eliminates the parse ambiguity. This is simpler than changing the separator, and the naming constraint is independently desirable (predictable identifiers, no encoding issues, grep-friendly).

**Irreversibility summary for A1:**

| Sub-decision | Classification | Confidence |
|-------------|---------------|------------|
| `shape_ref` exists in envelope | Structural constraint | High |
| `shape_ref` is mandatory | Structural constraint | High |
| Format: `{shape_name}/v{version}` | Structural constraint | High — with naming constraint |
| Shape naming rule: `[a-z][a-z0-9_]*` | Strategy-protecting constraint | High — protects parse safety |
| Version is integer (monotonic) | Structural constraint | High — simplest comparison, no semver complexity |

---

### A2: `activity_ref` — optional envelope field

| Test | Assessment |
|------|-----------|
| **Stored state** | Events that carry `activity_ref` store it permanently. Events with `null` don't need migration. Changing the field name or semantics after deployment means: events with the old format coexist with events under the new format. But since the field is optional with null default, ADDING it later is cheap — existing events just have null. **MEDIUM — format matters, but optionality provides an escape hatch.** |
| **Contract surface** | Components that use `activity_ref`: projection engine (grouping), campaign progress (aggregate by activity), reporting (filter by activity). **3 components.** Moderate coupling — significantly less than `shape_ref`'s 7. |
| **Wrong choice recovery** | If the format is wrong: events with the old format persist but are a minority (only campaign events carry it). Recovery = code change to handle both formats. Not a full migration. If we decide the field shouldn't exist at all: null events are unaffected; events with values can be handled by ignoring the field. **Code change, not data migration.** |

**Classification: STRUCTURAL CONSTRAINT — but lower irreversibility than A1.**

The optionality is the key differentiator. `shape_ref` is in EVERY event. `activity_ref` is in SOME events. The blast radius of a format error is proportionally smaller.

**What exactly is being locked?**

1. The field name: `activity_ref`
2. The field presence: optional (null when not needed)
3. The field semantics: "which activity instance this event was captured as part of"

**What remains evolvable?**

- Which events carry it (deployer decision per activity configuration)
- How the server uses it for grouping/reporting (server-side logic)
- Whether it becomes mandatory in future versions (additive change — existing null events remain valid)

**Format deep-dive: What is the value of `activity_ref`?**

Session 2 used activity names: `activity_ref: "measles_campaign_2026"`. But activities are deployer-defined identifiers. What format rules apply?

**Options:**

| Option | Format | Example | Pro | Con |
|--------|--------|---------|-----|-----|
| Free-form string | Deployer's chosen ID | `"measles_campaign_2026"` | Simple, readable | No structure; collision risk across deployments |
| Namespaced string | `{activity_type}.{id}` | `"campaign.measles_2026"` | Type-safe, parseable | Adds parsing complexity; is the prefix useful? |
| Opaque UUID | Server-assigned | `"a3f8b2c1-..."` | Globally unique, no collisions | Unreadable in logs; deployers don't author UUIDs |
| Deployer ID with naming constraint | Same as shape names | `"measles_campaign_2026"` | Consistent with shape naming; `[a-z][a-z0-9_]*` | Slightly restrictive |

**Position: Deployer-chosen identifier with the same naming constraint as shapes: `[a-z][a-z0-9_]*`.** Consistent naming rules across all deployer-defined identifiers (shapes and activities). Readable, parseable, no separator ambiguities. The server assigns no UUID — the deployer's ID IS the reference.

**But: what about activity instances vs. activity definitions?**

A campaign might recur: "measles campaign 2026" and "measles campaign 2027." Are these two activities or two instances of one activity?

| Model | `activity_ref` value | Distinguishes recurrences? |
|-------|---------------------|--------------------------|
| Activity = definition | `"measles_campaign"` | No — all recurrences share one ref |
| Activity = instance | `"measles_campaign_2026"` | Yes — each recurrence is distinct |

**Session 2 used instances** (`measles_campaign_2026`). This is correct: the deployer creates a new activity instance for each campaign. The definition (what shapes, what stages, what roles) can be shared as a template, but each deployment is a distinct instance with its own time window, targets, and identity. `activity_ref` points to the INSTANCE, not the template.

This means `activity_ref` values are unique per deployment. No collision possible within a deployment. Cross-deployment collision is handled by multi-tenancy (future concern, same as shapes).

**Irreversibility summary for A2:**

| Sub-decision | Classification | Confidence |
|-------------|---------------|------------|
| `activity_ref` exists in envelope | Structural constraint | High |
| `activity_ref` is optional (null default) | Structural constraint | High — optionality is the escape hatch |
| Format: deployer-chosen identifier, `[a-z][a-z0-9_]*` | Strategy-protecting constraint | High — consistent with shape naming |
| Semantics: references an activity instance, not a definition | Structural constraint | High |

---

### A3: `type` — platform-fixed closed vocabulary of structural event types

| Test | Assessment |
|------|-----------|
| **Stored state** | Every event carries `type`. The VALUES stored are from the platform's vocabulary. If event types are later renamed, events with old type names persist forever. If new types are added, old events don't need migration (new types only appear in new events). If types are removed, existing events with that type must still be parseable. **PARTIALLY irreversible — the vocabulary can grow but not shrink or rename.** |
| **Contract surface** | `type` is used by: event store (routing), projection engine (state computation), trigger engine (matching), sync layer (conflict detection behavior), form engine (UI selection). **5 components.** High coupling. |
| **Wrong choice recovery** | Adding a type: code change (all components learn to handle new type). No data migration. Renaming a type: events carry the old name forever, so both names must be understood. Protocol change. Removing a type: events with that type still exist, projection engine must handle them. Protocol change at minimum. **Addition is cheap. Rename/removal is expensive.** |

**Classification: STRUCTURAL CONSTRAINT — but only the INITIAL vocabulary is irreversible. Growth is cheap.**

**What exactly is being locked?**

1. The field name: `type` (already committed by ADR-1)
2. The field semantics: platform-fixed structural type (processing behavior, not domain meaning)
3. The initial vocabulary: whatever types ship in v1

**What remains evolvable?**

- Adding new structural types (code change, no data migration)
- The processing behavior associated with each type (code, evolvable)
- What shapes are compatible with what types (configuration, evolvable)

**Initial vocabulary assessment:**

Session 2 identified these types across all scenarios:

| Type | Meaning | Required by | Removable later? |
|------|---------|-------------|-----------------|
| `capture` | Human records an observation or action | S00, S06, S09, S20 | No — foundational |
| `review` | Human evaluates another's work | S06, S20 | No — review pattern depends on it |
| `alert` | System or human flags a condition | S12, S20 | No — trigger engine produces these |
| `task_created` | System or human assigns work | S12 | Maybe — could merge with alert? |
| `task_completed` | Human completes assigned work | S12 | Maybe — could be a specialized capture? |
| `escalation` | System raises unresolved issue | S12 | Maybe — could be a specialized alert? |
| `assignment_changed` | Authority changes scope/role | All (implicit) | No — ADR-3 depends on it |

**Minimality check: Can any types be merged without losing structural distinction?**

- `task_created` vs. `alert`: Both are system-generated notifications. But they have different processing semantics — tasks have an expected response (tracked by deadline checks), alerts do not. **Keep separate.**
- `task_completed` vs. `capture`: Both are human-authored data. But `task_completed` references a prior `task_created` event (closing the loop), while `capture` is standalone. The projection engine treats them differently (task completion updates task status). **Keep separate.**
- `escalation` vs. `alert`: Both signal a condition. But escalation carries escalation-level metadata and references a prior trigger chain. Could this be an `alert` with `escalation_level` in the payload? Yes — structurally, escalation IS an alert with additional context. **Merge: `escalation` → `alert` with payload distinguishing severity/level.**

**Revised initial vocabulary (6 types, not 7):**

| Type | Processing semantics |
|------|---------------------|
| `capture` | Human-authored data record. Enters shape validation + projection pipeline. |
| `review` | Human evaluation of another event. References source event. Updates review status in projection. |
| `alert` | Condition notification (system or human). May carry escalation metadata in payload. Routes to target role. |
| `task_created` | Work assignment. Tracked for response. Deadline checks watch for corresponding `task_completed`. |
| `task_completed` | Response to a task. References the task. Closes the tracking loop. |
| `assignment_changed` | Scope/role modification. Feeds ADR-3's assignment resolver. |

Can we go smaller? `review` could theoretically be a `capture` with a `source_event_ref` in the payload. But the processing semantics genuinely differ: `review` triggers review-status projection updates, `capture` does not. The platform routes on type, not payload inspection. **6 types is the minimum for current scenarios.**

**Growth path:** When ADR-5 (state progression) adds new event types (`case_opened`, `case_resolved`, `transfer`, etc.), they're added to the vocabulary. No migration. The commitment is: the initial 6 exist forever. New ones can be added. None can be removed once events carry them.

**Irreversibility summary for A3:**

| Sub-decision | Classification | Confidence |
|-------------|---------------|------------|
| `type` is a platform-fixed closed vocabulary | Structural constraint | High |
| Initial vocabulary is append-only (types can be added, never removed) | Structural constraint | High |
| Initial vocabulary: 6 types (capture, review, alert, task_created, task_completed, assignment_changed) | Structural constraint | Medium — ADR-5 may add types, but these 6 are stable |
| `escalation` merged into `alert` (payload-distinguished) | Strategy | Medium — could be revisited before ADR writing |

---

### A4: System actor identity — `system:trigger/{trigger_id}` as `actor_ref`

| Test | Assessment |
|------|-----------|
| **Stored state** | Trigger-generated events carry this as `actor_ref`. All such events are permanent. If the format changes, old events have old format, new events have new format. The `actor_ref` field already exists (ADR-2). The question is: what VALUE does it take for system-generated events? **YES — the format is stored in events.** |
| **Contract surface** | Components that parse `actor_ref`: projection engine (attribution), audit log (who-did-what), sync layer (scope resolution). But: these components already handle `actor_ref`. The system-actor format is a new VALUE PATTERN in an existing field, not a new field. **3 components, but parsing is a VALUE check, not a structural change.** |
| **Wrong choice recovery** | If the format is wrong: events with the old format persist. New format applies going forward. The projection engine needs to understand both patterns. **Code change — no data migration.** The old events aren't invalid; they just use a different notation for system actors. |

**Classification: STRATEGY-PROTECTING CONSTRAINT — not fully irreversible.**

The `actor_ref` field itself is committed (ADR-2). The VALUE format for system actors is less irreversible because:
- Only trigger-generated events carry it (a minority of all events)
- The existing field's semantics ("who created this event") don't change
- Both human and system actor_ref values can coexist without structural conflict
- Code change (parsing update) handles format evolution

**However**, establishing a convention EARLY prevents fragmentation. If triggers use `system:trigger/X` and later scheduled jobs use `background:job/Y`, the lack of a unified system-actor format creates parsing complexity.

**Position: Establish a system actor format convention, classify it as a strategy-protecting constraint (not structural).**

**Format convention:**

```
system:{source_type}/{source_id}
```

Where `source_type` is one of a fixed set: `trigger`, `scheduler`, `import`, `migration`. The set can grow. `source_id` is the configuration identifier of the specific trigger/job/pipeline.

This is documented as a convention that all system-generated events follow. It's not in the envelope schema (the envelope says `actor_ref: string`). It's a value-format convention — important, but evolvable with a code change.

**Irreversibility summary for A4:**

| Sub-decision | Classification | Confidence |
|-------------|---------------|------------|
| System-generated events have a non-null `actor_ref` | Structural constraint | High — follows V3 (trustworthy records) |
| Value format: `system:{source_type}/{source_id}` | Strategy-protecting constraint | Medium — convention, not schema |
| `source_type` vocabulary: starts with `trigger` | Initial strategy | High — grows as new system sources emerge |

---

## Category B–D: Bulk Classification

Everything outside Category A (the envelope) is filtered quickly. The test: does it touch stored events?

### Category B: Configuration architecture

| # | Position | Touches stored events? | Classification | Rationale |
|---|----------|----------------------|---------------|-----------|
| B1 | Four-layer gradient | No — describes config structure, not event structure | **Initial strategy** | Could restructure layers without migrating events |
| B2 | L3 split into 3a/3b | No — trigger evaluation is server-side | **Initial strategy** | Server code change |
| B3 | Triggers server-only | No — affects what runs where, not what's stored | **Strategy-protecting constraint** | Protects device simplicity; changing it adds device complexity but doesn't touch events |
| B4 | One expression language, two contexts | No — expressions are in config, not events | **Initial strategy** | Language can evolve with config format |
| B5 | Zero functions in expressions | No — expression evaluation is runtime | **Initial strategy** | Functions could be added later via config format change |
| B6 | Payload mapping restrictions | No — mapping is trigger config, not event format | **Initial strategy** | Could relax restrictions via config format change |
| B7 | Projection rules = lookup tables | No — projection rules are config | **Initial strategy** | Could add conditional rules later |
| B8 | Trigger DAG, max path 2 | No — deployment validation rule | **Initial strategy** | Could increase depth limit later |
| B9 | Shapes as deltas/snapshots | No — shape storage is server-side registry | **Initial strategy** | Registry format can evolve |
| B10 | Change classification (additive/deprecation/breaking) | No — deployer workflow, not event structure | **Initial strategy** | Classification rules can evolve |
| B11 | Config as atomic package at sync | No — sync protocol, not stored state | **Strategy-protecting constraint** | Protects against partial config on device |
| B12 | Device holds max 2 config versions | No — device-side policy | **Initial strategy** | Could change to 3 or 1 |

**Zero items in Category B touch stored events.** All are evolvable. Some are strategy-protecting constraints (B3, B11) because they guard structural invariants, but their implementations can change without data migration.

### Category C: Platform capability architecture

| # | Position | Touches stored events? | Classification |
|---|----------|----------------------|---------------|
| C1 | Patterns are platform-fixed | No — platform code, not stored state | **Initial strategy** |
| C2 | Three platform capabilities (aggregate, threshold, windowing) | No — server-side computation | **Initial strategy** |
| C3 | Max 2 escalation levels | No — validation rule | **Initial strategy** |
| C4 | Campaign progress as composition | No — server architecture | **Initial strategy** |

**Zero items in Category C touch stored events.**

### Category D: Boundary decisions

| # | Position | Touches stored events? | Classification |
|---|----------|----------------------|---------------|
| D1 | T2 boundary at L3 → code | No — defines what's configurable | **Initial strategy** |
| D2 | Side effect definition | No — categorization framework | **Initial strategy** |
| D3 | Complexity budgets | No — deployer validation limits | **Initial strategy** |
| D4 | Platform capability elevation principle | No — design principle | **Initial strategy** |

**Zero items in Category D touch stored events.**

---

## Summary: ADR-4's Irreversibility Surface

| Category | Items that touch stored events | Items that don't |
|----------|-------------------------------|-----------------|
| A (Envelope) | **4** (A1, A2, A3, A4) | 0 |
| B (Config architecture) | 0 | 12 |
| C (Platform capabilities) | 0 | 4 |
| D (Boundaries) | 0 | 4 |
| **Total** | **4** | **20** |

**ADR-4 has 4 envelope-level decisions out of 24 total positions.** This is proportional to previous ADRs:
- ADR-2 had ~8 envelope decisions (high)
- ADR-3 had ~1 envelope decision (low — ultimately zero, authority_context resolved as "no new fields")
- ADR-4 has 4 (moderate)

### Irreversibility Tier Classification

**Tier 1 — Structural constraints (irreversible, full stress test in Part 3):**

| Decision | What's locked | Format |
|----------|--------------|--------|
| A1: `shape_ref` | Mandatory envelope field. Format: `{shape_name}/v{version}`. Shape names: `[a-z][a-z0-9_]*`. Version: integer. | `household_observation/v1` |
| A2: `activity_ref` | Optional envelope field (null default). Deployer-chosen identifier, `[a-z][a-z0-9_]*`. References activity instance. | `measles_campaign_2026` or null |
| A3: `type` vocabulary | Platform-fixed, closed, append-only. Initial 6: capture, review, alert, task_created, task_completed, assignment_changed. | `capture` |

**Tier 2 — Strategy-protecting constraints (guards an invariant, implementation evolvable):**

| Decision | What it protects |
|----------|-----------------|
| A4: System actor format `system:{type}/{id}` | V3 auditability for system-generated events |
| B3: Triggers server-only | Device simplicity |
| B11: Config as atomic package | Device consistency |
| A1 naming constraint: `[a-z][a-z0-9_]*` | Parse safety of `shape_ref` format |

**Tier 3 — Initial strategies (evolvable without migration):**

Everything in Categories B (except B3, B11), C, and D — 18 positions total.

---

## Part 3 Stress Test Scope

Based on the irreversibility filter, Part 3's adversarial stress tests should be scoped proportionally:

**Full adversarial treatment (Tier 1):**

| Attack vector (from Session 2 §9.2) | Target | Why it's Tier 1 |
|--------------------------------------|--------|----------------|
| "Campaign using same shape as three other activities" | A2: `activity_ref` optionality | Tests whether optional is sufficient or mandatory is needed |
| "Schema v3 removes a required field that v1 and v2 had" | A1: `shape_ref` versioning | Tests breaking-change handling under the committed format |
| New: "Event with type not in the vocabulary" | A3: type vocabulary completeness | Tests whether 6 types are sufficient |

**Light validation (Tier 2/3):**

| Attack vector | Target | Why it's lower priority |
|---------------|--------|----------------------|
| "Shape with 200 fields" | D3: Complexity budgets | Budget is a validation rule (Tier 3), not stored state |
| "Expression referencing 3 entities" | B5: Expression scope | Expression language is config (Tier 3) |
| "Trigger chain depth 3" | B8: DAG max path | Validation rule (Tier 3) |

**Reframing suggestion for Part 3:** Session 2's original 5 attack vectors included 3 that target Tier 3 positions (complexity budgets, expression scope, trigger depth). These DON'T need full adversarial stress tests — they're validation rules that can be changed with a code update. Part 3 should spend its rigor on the 3 Tier 1 attacks plus any new envelope-specific attacks the filter surfaced. The Tier 3 attacks can be addressed briefly (confirm the budget is reasonable, document the reasoning, move on).

---

## Full Envelope After ADR-4 Irreversibility Filter

With all commitments from ADR-1, ADR-2, ADR-3, and ADR-4:

```
id              # ADR-1: UUID, client-generated. Structural constraint.
type            # ADR-1 + ADR-4: platform-fixed structural type. Structural constraint.
                # Values: capture | review | alert | task_created | task_completed | assignment_changed
shape_ref       # ADR-4: "{shape_name}/v{version}". Mandatory. Structural constraint.
                # Shape names: [a-z][a-z0-9_]*. Version: integer.
activity_ref    # ADR-4: deployer-chosen identifier or null. Optional. Structural constraint.
                # Format: [a-z][a-z0-9_]* (same naming rule as shapes).
subject_ref     # ADR-2: typed identity reference. Structural constraint.
actor_ref       # ADR-2: who/what created this event. Structural constraint.
                # System events: system:{source_type}/{source_id} (strategy-protecting convention).
device_id       # ADR-2: hardware-bound device identifier. Structural constraint.
device_seq      # ADR-2: device-local sequence number. Structural constraint.
sync_watermark  # ADR-2: server-assigned ordering (null until synced). Structural constraint.
timestamp       # ADR-1: device_time (advisory). Structural constraint.
payload         # ADR-1: shape-conforming data. Structural constraint.
```

**11 fields, same as Session 2.** No new fields added. Two fields gain ADR-4-specific format commitments (`shape_ref`, `activity_ref`). One field gain a value convention (`actor_ref` for system actors). One field gains a value vocabulary (`type`).

---

## Part 3 Reframe Assessment

**Is Part 3's charter changed by Part 2?** Yes — scope is NARROWED.

Part 2 found that only 3 of Session 2's 5 attack vectors target irreversible positions. The other 2 target evolvable strategies. Part 3 should:

1. **Full adversarial stress on:** `activity_ref` optionality (multi-activity collision), `shape_ref` versioning (breaking changes), and type vocabulary completeness.
2. **Light validation on:** complexity budgets, expression scope, trigger depth — confirm reasoning, don't over-attack.
3. **New attack suggested by Part 2:** system actor identity collision — what if two triggers have similar IDs? (Low risk — trigger IDs are deployer-unique within a deployment, same as shape names.)

The overall reduction: Part 3 is a focused 3-attack stress test, not a 5-attack broad sweep. This should make it faster and sharper.
