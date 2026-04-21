> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-004**. For a navigated reading guide, see [guide-adr-004.md](../guide-adr-004.md).
# ADR-004 Session 3, Part 1: Structural Coherence Audit

> Phase: 3.1 of 4 — Structural Coherence
> Date: 2026-04-12
> Input: Session 2 walk-throughs (14-adr4-session2-scenario-walkthrough.md), 5 scenario solutions (S00, S06, S09, S12, S20)
> Purpose: Verify that the five local solutions from Session 2 compose into one system — one artifact lifecycle, one dependency model, one expression language, one device/server contract, one pattern framework, one envelope. If they don't compose, the fix is revision of the gradient, not re-running scenarios.

---

## Method

Six checks, each asking: "Do the five scenario solutions agree on this structural concern, or did they each invent a local answer?"

| Check | Question |
|-------|----------|
| (a) Artifact lifecycle | Do ALL artifact types share one lifecycle model, or did each scenario invent its own? |
| (b) Dependency graph | Do artifact dependencies form a coherent, acyclic graph with consistent cascade rules? |
| (c) Device/server split | Is the evaluation location (device, server, both) consistent and explicit for every capability? |
| (d) Expression language | Is there one expression language or did each layer invent its own? |
| (e) Pattern framework | Is "pattern" a coherent concept with a clear boundary, or fuzzy and scenario-specific? |
| (f) Envelope composition | Does the full envelope serve all five scenarios without redundancy or gaps? |

Each check produces a verdict: **composes**, **composes-with-revision**, or **does-not-compose**.

---

## Check (a): Artifact Lifecycle — Do All Artifact Types Share One Model?

### Inventory

Session 2 produced six artifact types across five scenarios:

| # | Artifact type | Layer | Examples | Scenarios |
|---|---------------|-------|----------|-----------|
| 1 | Shape definition | L1 | `household_observation/v1`, `facility/v1`, `facility/v2` | S00, S06, S09, S12, S20 |
| 2 | Activity definition | L0 | `household_survey`, `facility_registry`, `measles_campaign_2026` | S00, S06, S09, S20 |
| 3 | Logic rule | L2 | show/hide, computed defaults, conditional warnings | S20 |
| 4 | Trigger | L3 | `stockout_investigation`, `critical_value_alert`, `investigation_overdue` | S12, S20 |
| 5 | Projection rule | L1 | treatment→supply impact mapping | S20 |
| 6 | Campaign definition | L0 | `measles_campaign_2026` (variant of activity) | S09 |

### Lifecycle analysis per type

**Shape definitions** — Session 2 addressed this thoroughly:
- Versioned explicitly: `shape_name/v{N}` in every event via `shape_ref`
- Authored as deltas, stored as full snapshots
- Change classification: additive / deprecation / breaking
- Devices hold at most 2 versions (current + previous for in-progress work)
- All versions remain valid forever (append-only, consistent with P3)

Shapes are **event-coupled** — events permanently reference them. This makes shape versioning irreversible. Well-designed.

**Activity definitions** — Less covered in Session 2. Key questions:
- Are activities referenced from events? **Yes, optionally** — via `activity_ref` in the envelope. But `activity_ref` is an identifier (stable ID), not a version reference. The activity's structural definition (which shape, which pattern, which roles) can change without invalidating events.
- Do activities need versioning? **Not in the same way shapes do.** An activity ID is stable. The definition behind it (roles, scope, patterns) can evolve. Historical events carrying that `activity_ref` remain valid — they point to the activity's identity, not its historical definition.
- What about retirement? If an activity is deactivated, events with that `activity_ref` still exist. Projections can flag them as "from retired activity." No data loss.

Activities are **ID-stable, definition-mutable**. No version-in-envelope needed.

**Logic rules** — Bound to shapes. Not referenced from events at all. Logic rules affect what the user SEES during capture (show/hide fields, computed defaults, warnings), but the stored event payload is the captured data. Changing a logic rule changes future form behavior, not historical events.
- No independent versioning needed.
- Changes take effect at next config sync.
- If a rule references a field that's deprecated in a shape update, that's a dependency validation issue (see Check b).

Logic rules are **config-level, no versioning**.

**Triggers** — Produce events, but are not referenced FROM events. The trigger's output events are self-describing (they carry their own `type`, `shape_ref`, etc.). The trigger itself is not cited in the output event — the output stands on its own in the event store.
- No independent versioning needed.
- Changing a trigger's condition or output changes future evaluations. Historical trigger-produced events are unaffected.
- Deactivating a trigger stops future evaluations but preserves produced events.

Triggers are **config-level, no versioning**.

**Projection rules** — Instructions to the projection engine. Not referenced from events. Changes affect how projections are computed, not what events exist. Since projections are rebuildable from events (ADR-1), changing a projection rule means recomputing affected projections.
- No independent versioning needed.
- Changes trigger projection rebuild (potentially expensive on-device for large event sets — implementation concern, not structural).

Projection rules are **config-level, no versioning**.

**Campaign definitions** — A variant of activity definitions with additional structure (time window, stages, targets). Same lifecycle as activities: ID-stable, definition-mutable, optionally referenced via `activity_ref`.

### The two lifecycle models

A clear structural pattern emerges:

| Lifecycle model | Artifact types | Referenced from events? | Versioning | Change impact |
|----------------|---------------|----------------------|------------|---------------|
| **Event-coupled** | Shapes | Yes (`shape_ref`, mandatory) | Explicit version in every event | Additive safe; breaking requires migration declaration |
| **Config-package** | Activities, logic rules, triggers, projection rules, campaigns | No (or ID-only via optional `activity_ref`) | No individual versioning | Changes affect future behavior only |

**The event-coupled lifecycle is fully specified by Session 2.** Shape versioning, change classification, coexistence rules — all addressed.

**The config-package lifecycle is structurally sound but under-specified.** All config-level artifacts share the same delivery mechanism (atomic package at sync, applied after in-progress work), the same change-impact model (future-only), and the same deprecation model (soft delete, historical references preserved). What's missing is:

1. **Config package versioning** — How does a device know which config version it's running? Answer: a package-level version identifier (e.g., monotonic counter or hash). This is delivery infrastructure, not event architecture.
2. **Config package atomicity** — Can a deployer publish a partial update (just one trigger change) or must the entire config be republished? Answer: the package is atomic (all-or-nothing delivery), but authoring can be granular. The validation step (Check b) catches inconsistencies before deployment.

These are implementation decisions, not structural constraints. They don't affect the event model or the envelope.

### Verdict

**Check (a): COMPOSES.**

All artifact types fall into one of two lifecycle models. The models are structurally compatible — event-coupled artifacts provide permanent references in the event stream, config-package artifacts provide mutable behavior without touching stored events. No scenario invented a lifecycle that conflicts with another.

**Action item** (strategy, not constraint): Specify config-package delivery lifecycle in Session 4 or ADR writing. This includes package versioning and atomicity. It's implementable without structural decisions.

---

## Check (b): Dependency Graph — Acyclic? Consistent Cascade Rules?

### Dependency map

Every artifact-to-artifact dependency discovered across all five scenarios:

```
┌──────────────────────────────────────────────────────────┐
│                   DEPENDENCY GRAPH                        │
│                                                          │
│  Shape ◄── Logic Rule       (references shape fields)    │
│  Shape ◄── Activity         (references shape by name)   │
│  Shape ◄── Trigger          (condition refs shape fields, │
│  │                           output refs output shape)   │
│  Shape ◄── Projection Rule  (source_shape, field refs)   │
│                                                          │
│  Pattern ◄── Activity       (references pattern by name) │
│  (platform-fixed, can't be removed by deployers)         │
│                                                          │
│  Trigger ◄── Trigger        (deadline_check watches      │
│  (bounded: max depth 2,      another trigger's output)   │
│   output type ≠ input type)                              │
│                                                          │
│  Activity ◄── Campaign      (campaign is a specialized   │
│  (structural variant)         activity type)              │
└──────────────────────────────────────────────────────────┘
```

### Cycle analysis

Can dependencies form cycles?

- **Shape → Logic Rule → ?**: Logic rules don't create other artifacts. No outbound edges from logic rules. **Dead end.**
- **Shape → Trigger → Trigger (deadline)**: Deadline-check watches another trigger's output. The deadline-check's OWN output is an event (escalation), not a trigger. Bounded to depth 2. Output event type ≠ input event type (AP-5). **Chain, not cycle. Bounded.**
- **Shape → Projection Rule → ?**: Projection rules map events to projections (read models). They don't create events or other artifacts. **Dead end.**
- **Activity → Shape → Activity**: Could an activity reference a shape that somehow references back to the activity? No — shapes define field structures, they don't reference activities. **No cycle.**

**Dependencies are acyclic by construction.** No feedback loops are possible in the current model.

### Cascade rules

What happens when an upstream artifact changes?

| Change | Downstream impact | Rule |
|--------|-------------------|------|
| Shape field deprecated | Logic rules referencing that field become invalid | **Fail validation at deploy time.** Deployer must update rules before publishing shape change. |
| Shape field deprecated | Trigger conditions referencing that field become invalid | **Fail validation at deploy time.** Same principle. |
| Shape field deprecated | Projection rules referencing that field become invalid | **Fail validation at deploy time.** Same principle. |
| Shape retired (all versions deprecated) | Activities referencing that shape become invalid | **Fail validation at deploy time.** Activities must reference active shapes. |
| Trigger removed | Deadline-check watching that trigger's output becomes invalid | **Fail validation at deploy time.** Remove dependent deadline-checks first. |
| Pattern removed (platform evolution) | Activities referencing that pattern break | **Platform responsibility.** Patterns are platform-fixed; removal is a platform migration, not a deployer action. |

**The cascade rule is uniform:** all dependency violations are caught at **deploy-time validation**, never at runtime. The configuration package is validated as a whole before deployment. An inconsistent package is rejected with specific error messages identifying the broken references.

### Deployer tooling implication

When a deployer changes a shape, the authoring tool should surface: "These logic rules, triggers, and projection rules reference fields you're modifying." This is **AP-3 mitigation** (Configuration Specialist Trap) — making dependencies visible prevents accidental breakage. This is tooling, not architecture, but it's important enough to note.

### Verdict

**Check (b): COMPOSES.**

Dependencies are acyclic. Cascade rules follow one principle (validate at deploy time). No runtime dependency failures are possible if deploy-time validation is enforced. The dependency structure is simple enough to visualize in a deployer tool.

---

## Check (c): Device vs. Server Evaluation — One Consistent Contract?

### The split

Session 2 distributed evaluation across device and server for each capability. But the distribution was stated per-scenario, not as a system-wide contract. This check consolidates.

| Capability | Device | Server | Both | Rationale |
|------------|--------|--------|------|-----------|
| Event creation | ✅ | ✅ | Both | Core offline-first — events created locally, also creatable server-side by triggers |
| Shape validation (field types, required) | ✅ | ✅ | Both | Device validates at capture; server re-validates on receipt (defense in depth) |
| Logic rules (L2) | ✅ | — | Device only | Real-time form behavior requires local execution |
| Projection (per-subject) | ✅ | ✅ | Both | Device for local views; server for complete dataset |
| Projection (cross-subject indexes) | Limited | ✅ | Mostly server | Device has limited scope (synced subjects only); server has all |
| Trigger 3a (event-reaction) | — | ✅ | **Server only** | See analysis below |
| Trigger 3b (deadline-check) | — | ✅ | Server only | Requires complete event timeline + reliable clock |
| Campaign progress | — | ✅ | Server only | Aggregate across all actors/subjects |
| Assignment resolution | ✅ | ✅ | Both | Device resolves locally from synced config; server is source of truth |
| Scope computation | — | ✅ | Server only | Determines what syncs to each device |
| Config delivery | — | ✅ | Server only | Server packages, device receives |
| Conflict detection | — | ✅ | Server only | Requires seeing all events (ADR-2) |
| Merge/split | — | ✅ | Server only | Online-only (ADR-2 S10) |

### The 3a trigger location question — a revision from Session 2

Session 2 stated that 3a triggers (event-reactions) "can run on device OR server." The structural coherence check reveals this introduces three problems:

**Problem 1 — Device trigger engine complexity.** If devices evaluate triggers, they need: a trigger evaluation engine, trigger configuration storage, and the ability to create system-generated events locally. This is a new engine on top of the existing device capabilities (form engine, projection engine, validation engine). The device footprint grows.

**Problem 2 — Deduplication.** If the device fires a trigger and creates an alert event, and the server independently evaluates the same trigger when the source event syncs, two identical alerts are created. Preventing duplicates requires a deduplication protocol (trigger source markers, server-side duplicate detection), adding complexity to both sides.

**Problem 3 — Divergent evaluation.** The device might evaluate a trigger condition differently from the server because it has stale entity data (limited sync scope). One side fires, the other doesn't. Which is authoritative? Managing this divergence creates a consistency problem that doesn't need to exist.

**The alternative:** L2 logic rules ALREADY provide immediate on-device feedback. The S12 critical-value scenario — where a CHV records a dangerous temperature and needs to know immediately — is handled by L2:

```
logic:
  shape: patient_observation
  rules:
    - when: "payload.temperature > 39.5"
      warn: "Critical temperature — refer patient immediately"
      set: { referral: true }
```

This fires instantly, on device, at capture time. The CHV sees the warning. The patient gets referred. No trigger engine needed on device.

The L3a trigger (`critical_value_alert`) creates a system-level alert for the **supervisor** — a coordination/oversight function that is naturally async (the supervisor isn't standing next to the CHV). Server-side evaluation with sync-interval latency is appropriate.

**Revised position:** L3a triggers evaluate **server-only**. L2 logic rules handle **device-side immediate feedback**. The functional split:

| Need | Mechanism | Where |
|------|-----------|-------|
| Immediate user feedback (warnings, auto-fill, show/hide) | L2 logic rules | Device |
| System-generated events (alerts, tasks, escalations) | L3 triggers | Server |

This is cleaner than Session 2's "can run on device OR server" because:
- It eliminates the deduplication problem entirely
- It keeps the device engine simple (form + projection + validation — no trigger evaluation)
- It gives each layer a clear, non-overlapping responsibility
- The deployer knows exactly where each expression evaluates

**Consequence for latency:** Trigger-generated events (alerts, tasks) arrive at target actors after sync. For deployments syncing hourly, alerts fire within 1 hour. For daily sync, within 24 hours. For the use cases Session 2 explored, this is acceptable — trigger outputs are coordination/oversight, not emergency response.

### The device engine contract

After this revision, the device runs exactly four engines:

| Engine | Input | Output | Runs when |
|--------|-------|--------|-----------|
| **Form engine** | Shape definition + L2 logic rules | User interface, field behavior | User opens a form |
| **Validation engine** | Shape definition + user input | Accept/reject capture | User submits a form |
| **Event store** | Validated event | Persisted event | After validation passes |
| **Projection engine** | Events + projection rules | Per-subject state, cross-subject indexes (scoped) | After event write, on demand |

The server runs the same four plus three more:

| Engine | Input | Output | Runs when |
|--------|-------|--------|-----------|
| **Trigger engine** | Synced events + trigger definitions | New events (alerts, tasks, escalations) | At event ingestion |
| **Deadline scheduler** | Trigger definitions + event timeline | Escalation events | On schedule (periodic check) |
| **Scope resolver** | Assignment config + actor hierarchy | Per-device sync scope | At sync request |

### Verdict

**Check (c): COMPOSES — with one revision.**

The device/server split follows a clean contract: forms and immediate feedback on device, event generation and coordination on server, projections and validation on both. The revision is: **3a triggers are server-only** (not "device OR server" as Session 2 stated). L2 logic rules cover the device-side immediate feedback need. This simplifies the device, eliminates deduplication complexity, and gives each layer a non-overlapping evaluation location.

---

## Check (d): Expression Language — One or Many?

### Where expressions appear

Session 2's walk-throughs used expressions in three places:

| Location | Example | Scenario |
|----------|---------|----------|
| L1 shape validation | `validation: { min: 1, max: 50 }` | S00 |
| L2 logic rules | `"diagnosis IN ['malaria', 'pneumonia', 'diarrhea']"` | S20 |
| L2 logic rules (entity access) | `"entity.age_months < 2"` | S20 |
| L3a trigger conditions | `"payload.stock_level == 0"` | S12 |
| L3a trigger conditions | `"payload.temperature > 39.5 OR payload.oxygen_sat < 90"` | S12 |

### Decomposition

**L1 validation** is NOT an expression language. It's declarative parameter constraints: `{min, max, required, options}`. These are built-in validators parameterized by values — no conditional logic, no field references, no boolean operators. L1 stays declarative. Leave it alone.

**L2 logic rules** and **L3a trigger conditions** share the same grammar:

| Feature | L2 | L3 |
|---------|----|----|
| Comparison operators | `==`, `!=`, `<`, `>`, `<=`, `>=` | Same |
| Membership | `IN` | Same |
| Logical connectives | `AND`, `OR`, `NOT` | Same |
| Operand types | string, number, boolean, null, lists | Same |
| Field references | `payload.field_name`, `entity.attribute_name` | `payload.field_name` only |
| Output type | Boolean (for conditions) + value (for `set:`) | Boolean only |
| Functions | None | None |

The grammar is identical. The differences are in **evaluation context**:

| Property | L2 context | L3 context |
|----------|-----------|-----------|
| Data scope | `{payload, entity}` — current event payload + subject entity projection | `{payload}` — source event payload only |
| Output | Boolean (for `when:`) or value (for `set:`) | Boolean only |
| Execution location | Device | Server |
| When | During form interaction | At event ingestion |

### Position: One language, two evaluation contexts

The expression language specification defines:

**Operators:** `==`, `!=`, `<`, `>`, `<=`, `>=`, `IN`, `AND`, `OR`, `NOT`

**Operands:**
- Field references: `payload.<field_name>`, `entity.<attribute_name>`
- Literals: string (`"text"`), number (`42`, `39.5`), boolean (`true`, `false`), null, list (`['a', 'b']`)

**No functions.** Not initially. Adding functions is the slope toward AP-2 (Greenspun's Rule — "any sufficiently complicated config system reimplements half a programming language"). If deployers need date math, string manipulation, or aggregation, that's code territory (beyond L3).

**Evaluation contexts restrict the language:**

| Context | Allowed references | Allowed output | Used by |
|---------|-------------------|----------------|---------|
| `form` | `payload.*`, `entity.*` | boolean, value | L2 logic rules |
| `trigger` | `payload.*` | boolean only | L3a/3b trigger conditions |

**Benefits of one language:**
- One evaluator implementation (parameterized by context)
- One deployer learning curve — deployers who write L2 expressions can read L3 conditions
- One validation/test system
- AP-6 compliance (exactly one expression mechanism, not two competing ones)

### Notation consistency

Session 2 used inconsistent field reference syntax: L2 examples used bare field names (`diagnosis`), L3 used prefixed names (`payload.stock_level`). For one language, syntax should be uniform:

- **All field references are prefixed:** `payload.diagnosis`, `entity.age_months`, `payload.stock_level`
- The prefix makes the data scope explicit — you can see at a glance whether an expression references payload fields only or also entity attributes
- The `trigger` evaluation context rejects any expression containing `entity.*` references

This is a minor notation change from Session 2's walk-throughs, not a structural revision.

### What about L3 payload_map?

Trigger output payload mapping (`payload_map`) uses a different notation: `"$source.id"`, `"Stockout reported"`. This is NOT the expression language — it's a field-mapping DSL with two constructs:

1. **Static values:** `"Stockout reported"`, `"urgent"`, `1`
2. **Source field references:** `"$source.id"`, `"$source.payload.temperature"`

No operators, no conditions, no boolean logic. This is deliberately simpler than the expression language. Session 2 explicitly prohibited expressions in payload_map to prevent AP-1 (inner platform effect). The `payload_map` notation is a separate, intentionally minimal DSL — not part of the expression language.

### Verdict

**Check (d): COMPOSES.**

One expression language with two evaluation contexts. L1 remains declarative (no expressions). L2 and L3 share identical syntax; contexts differ in data scope and output type. Payload mapping is a separate, minimal DSL (static values + field references only). AP-6 is respected: one expression mechanism, one mapping mechanism.

---

## Check (e): Pattern Framework — Coherent Concept or Fuzzy?

### What Session 2 called "patterns"

Four patterns were referenced across five scenarios:

| Pattern name | What it provides | Used by |
|-------------|-----------------|---------|
| `capture_only` | Single capture event, no downstream | S00 |
| `capture_with_review` | Capture → review → approve/reject/return | S06, S20 |
| `periodic_capture` | Capture on schedule (interval-based) | S06 |
| `campaign` | Multi-stage, time-bounded, target-tracked coordination | S09 |

### What IS a pattern?

Synthesizing across all scenarios, a pattern provides:

1. **Participant roles** — which roles interact and in what capacity (capturer, reviewer, coordinator)
2. **Structural event types involved** — which of the platform's closed event types participate (capture, review, task_created, etc.)
3. **Valid sequences** — what order events should appear in (capture before review)
4. **Auto-generated projections** — what views the platform maintains automatically (review status, completion counts, campaign progress)
5. **Parameterization points** — what the deployer configures (which shape, which roles, which scope, time window, targets)

A pattern does NOT provide:
- The shape (deployer-defined, L1)
- The specific role/scope bindings (deployer-configured, L0)
- Logic rules (deployer-added, L2)
- Triggers (deployer-added, L3)

### The boundary: pattern vs. deployer composition

**Platform provides:** Patterns as a closed set. Fixed structural event types, fixed sequences, fixed auto-projections.

**Deployer uses:** Patterns by name. Parameterizes them. Combines them in activities.

**Platform evolves:** New patterns are added by platform developers. Deployers cannot invent new patterns — they request them.

This boundary is the same structural split as event types: platform-fixed vocabulary, deployer-parameterized usage. It's consistent.

### Composition model

How do patterns combine? Two modes emerged from Session 2:

**Mode 1 — Activity-level composition:** An activity uses multiple patterns simultaneously. S06 uses `capture_with_review` (for registration/updates) AND `periodic_capture` (for annual verification). The patterns are independently satisfied — review doesn't depend on periodicity.

**Mode 2 — Stage-level composition:** A campaign defines stages, each with its own pattern. S09's preparation stage uses `capture_with_review`, execution stage uses `capture_only`. Patterns apply per-stage, not per-campaign.

Both modes are LIST-based (patterns listed in the activity/stage definition), not NESTED (no pattern-within-pattern hierarchy). This prevents combinatorial complexity.

### Pattern inventory completeness

Session 2 identified 4 patterns. Mapping the full scenario set reveals candidates not yet named:

| Candidate pattern | Scenarios | What's new vs. existing |
|-------------------|-----------|------------------------|
| `transfer_with_acknowledgment` | S07, S14 | Two-party coordination: transfer → acknowledge → discrepancy |
| `case_management` | S08 | Long-running subject: open → interact → transfer → resolve |
| `multi_step_approval` | S11 | Sequential approval chain across multiple roles/levels |
| `entity_lifecycle` | S06 | Register → update → verify → deprecate |

**Are these patterns or compositions of existing ones?**

- `transfer_with_acknowledgment`: IS a new pattern — involves two actors and a handoff with acknowledgment. None of the current patterns cover two-party coordination at the event level.
- `case_management`: IS a new pattern — long-running, multi-actor, state-tracked subject with transfers and resolution. Different from `capture_with_review` because the subject accumulates events over weeks/months.
- `multi_step_approval`: EXTENDS `capture_with_review` — same structure (submit → review) but with sequential stages and level-based routing.
- `entity_lifecycle`: COMPOSES `capture_only` (registration) + `periodic_capture` (verification) + state tracking (active/inactive/deprecated). The lifecycle state machine is the new element.

### The intentional gap

These candidate patterns all involve **state progression** — subjects moving through lifecycle phases with different rules in each phase. This is ADR-5's decision surface (State Progression and Workflow). Session 3 should NOT finalize the pattern inventory because:

1. State machine representation is an ADR-5 question (projection-derived? explicit primitives? both?)
2. Workflow composition rules are an ADR-5 question
3. Transfer acknowledgment semantics need ADR-5's state model to formalize

**What Session 3 CAN validate:** the pattern STRUCTURE is sound — named, platform-fixed, parameterizable, composable at the activity/stage level. The inventory will grow with ADR-5.

### Verdict

**Check (e): COMPOSES — with an intentional gap.**

The pattern structure is coherent: patterns are a closed platform vocabulary, parameterized by deployers, composed at the activity/stage level (list-based, not nested). The pattern inventory is incomplete — 4 patterns named, 3-4 candidates identified but dependent on ADR-5's state progression decisions. This is the right gap: the structure is decided (ADR-4), the contents grow (ADR-5). No scenario broke the structure.

---

## Check (f): Envelope Composition — Complete, Redundant, or Gapped?

### The proposed envelope

After Session 2, the full event envelope:

```
id              # ADR-1: UUID, client-generated
type            # ADR-1: platform-fixed structural type (capture, review, alert, ...)
shape_ref       # ADR-4: "{shape_name}/v{version}" — mandatory
activity_ref    # ADR-4: activity instance reference — optional (null when shape disambiguates)
subject_ref     # ADR-2: typed identity reference
actor_ref       # ADR-2: who performed this action
device_id       # ADR-2: hardware-bound device identifier
device_seq      # ADR-2: device-local sequence number
sync_watermark  # ADR-2: server-assigned ordering (null until synced)
timestamp       # ADR-1: device_time (advisory)
payload         # ADR-1: shape-conforming data
```

11 fields. Each added by a specific ADR.

### Completeness test — every scenario event

| Scenario event | type | shape_ref | activity_ref | subject_ref | actor_ref | All fields present? |
|----------------|------|-----------|-------------|-------------|-----------|-------------------|
| S00: household observation | `capture` | `household_observation/v1` | null | household_123 | chv_456 | ✅ |
| S06: facility registration | `capture` | `facility/v1` | null | facility_789 | supervisor_101 | ✅ |
| S06: facility v2 update | `capture` | `facility/v2` | null | facility_789 | supervisor_101 | ✅ |
| S09: vaccination (campaign) | `capture` | `vaccination_record/v1` | `measles_campaign_2026` | individual_202 | team_303 | ✅ |
| S09: supply verification | `capture` | `supply_verification/v1` | `measles_campaign_2026` | facility_789 | team_303 | ✅ |
| S12: stockout alert (trigger output) | `alert` | `investigation_task/v1` | null | facility_789 | **???** | ⚠️ See below |
| S12: task created (trigger output) | `task_created` | `investigation_task/v1` | null | facility_789 | **???** | ⚠️ See below |
| S12: escalation (trigger output) | `escalation` | `escalation_notice/v1` | null | facility_789 | **???** | ⚠️ See below |
| S20: patient encounter | `capture` | `patient_encounter/v1` | null | individual_404 | chv_505 | ✅ |
| S20: supply count | `capture` | `supply_count/v1` | null | chv_505_supply | chv_505 | ✅ |
| S20: review of encounter | `review` | `patient_encounter/v1` | null | individual_404 | supervisor_606 | ✅ |

### Issue found: System-generated events need an actor identity

Trigger outputs (alerts, tasks, escalations) are created by the system, not by a human. The `actor_ref` field needs a value. Options:

| Option | Value | Tradeoff |
|--------|-------|----------|
| A | `null` | Breaks V3 (trustworthy records: "who did what") — every event should have an attribution |
| B | `"system"` | Special-case actor. Simple but unspecific. |
| C | `"system:trigger/{trigger_id}"` | Traces back to the specific trigger configuration that produced this event. Full auditability. |

**Position: Option C.** The `actor_ref` for trigger-generated events is `system:trigger/{trigger_id}`. This:
- Satisfies V3: every event records who/what created it
- Enables audit: "this alert was created by the stockout_investigation trigger"
- Fits ADR-2's identity model: the actor type already accommodates non-human actors (the system is an actor with a stable identity)
- Is forward-compatible: other system-generated events (e.g., from scheduled jobs, import pipelines) can use `system:{source_type}/{source_id}`

### Issue check: target routing for trigger outputs

Trigger definitions include `target_role: supply_coordinator` — who should receive the alert. Does the envelope need a `target_ref` field?

**No.** Routing is a projection/assignment concern, not an envelope concern:
- The alert event has a `subject_ref` (the subject that triggered the alert)
- ADR-3's scope resolver determines who has visibility of that subject
- The `target_role` from the trigger configuration is stored in the alert's **payload** (self-describing)
- The projection engine routes: "show alerts for subjects in my scope where payload.target_role matches my role"

No new envelope field needed. The event is self-describing through its payload.

### Issue check: source event reference

Trigger outputs carry `source_event_ref: "$source.id"` — a reference to the event that triggered them. Should this be an envelope field?

**No.** Not every event has a source event. This is specific to trigger outputs and belongs in the payload. The event-to-event relationship is discoverable via payload, not via envelope.

### Redundancy check

Every field serves a distinct purpose:

| Field | Purpose | Redundant with? |
|-------|---------|----------------|
| `id` | Unique identifier | Nothing |
| `type` | Processing pipeline routing (platform-fixed) | Not redundant with `shape_ref` — type is structural, shape is domain |
| `shape_ref` | Payload schema (deployment-defined) | Not redundant with `type` — shape is domain content, type is structural behavior |
| `activity_ref` | Activity context (optional) | Not derivable from other fields (S09 proved this) |
| `subject_ref` | What entity this event is about | Nothing |
| `actor_ref` | Who/what created this event | Nothing |
| `device_id` | Where it was created | Not redundant with `actor_ref` — same actor, different devices |
| `device_seq` | Causal ordering within device | Not redundant with `sync_watermark` — different ordering dimensions |
| `sync_watermark` | Global ordering (server-assigned) | Not redundant with `device_seq` — different scope |
| `timestamp` | When, advisory | Not redundant with `device_seq` — seq is order, timestamp is human-readable time |
| `payload` | Domain data | Nothing |

No redundancy. No field duplicates another's function.

### Size estimate

Back-of-envelope for event envelope overhead (string encoding):

| Field | Typical size |
|-------|-------------|
| id (UUID) | 36 bytes |
| type | ~10 bytes |
| shape_ref | ~25 bytes |
| activity_ref | 0–30 bytes (null or string) |
| subject_ref | ~36 bytes |
| actor_ref | ~36 bytes |
| device_id | ~36 bytes |
| device_seq | ~4 bytes |
| sync_watermark | 0–8 bytes |
| timestamp | ~24 bytes |
| **Total envelope** | **~215–245 bytes** |

For 100,000 events: ~24 MB envelope overhead. On 8GB low-end Android, well within budget. With binary encoding (CBOR/MessagePack) or SQLite column storage, significantly smaller.

### Verdict

**Check (f): COMPOSES.**

The envelope is complete (every scenario event representable), non-redundant (each field serves a distinct purpose), and properly sized (no low-end device concerns). One clarification resolved: system-generated events use `system:trigger/{trigger_id}` as `actor_ref`. No new envelope fields needed beyond Session 2's proposal.

---

## Overall Verdict

| Check | Verdict | Findings |
|-------|---------|----------|
| (a) Artifact lifecycle | **Composes** | Two clean lifecycle models: event-coupled (shapes) and config-package (everything else). No conflicts. |
| (b) Dependency graph | **Composes** | Acyclic. One cascade rule: validate at deploy time. No runtime dependency failures. |
| (c) Device/server split | **Composes — with revision** | 3a triggers revised to server-only (not "device OR server"). L2 logic rules cover device-side immediate feedback. Cleaner separation, simpler device engine. |
| (d) Expression language | **Composes** | One language, two evaluation contexts. L1 declarative, L2 form-context, L3 trigger-context. AP-6 compliant. |
| (e) Pattern framework | **Composes — with intentional gap** | Pattern structure is sound. Inventory incomplete (ADR-5 dependency). Structure decided here, contents grow later. |
| (f) Envelope composition | **Composes** | 11 fields, no redundancy, no gaps. System actor identity clarified. No new fields needed. |

**The five scenario solutions compose into one system.** No structural incoherence found. No gradient revision needed.

---

## Revisions and Clarifications for Part 2

Three items carry forward as input to Part 2 (Irreversibility Filter):

### Revision 1: 3a triggers are server-only

Session 2 stated event-reaction triggers "can run on device OR server." The coherence check determines: **server-only**. L2 logic rules handle device-side immediate feedback. This is a clarification that simplifies the device contract without losing functionality.

**Impact on Part 2:** This revision REDUCES the irreversibility surface — no trigger evaluation engine on device means fewer components depend on trigger configuration format. The irreversibility filter for triggers becomes simpler.

### Clarification 1: System actor identity for trigger outputs

`actor_ref` for trigger-generated events: `system:trigger/{trigger_id}`. Fits ADR-2's identity model. No new envelope fields.

**Impact on Part 2:** The `actor_ref` format for system actors is an envelope FORMAT question — relevant to the irreversibility filter on system event generation.

### Clarification 2: One expression language, context-parameterized

L2 and L3 share one expression grammar. Differences are in evaluation context (data scope, output type, execution location). Notation is uniform: `payload.field_name`, `entity.attribute_name`.

**Impact on Part 2:** The expression language format is NOT in the envelope (expressions are in configuration, not in events). Therefore it's evolvable — strategy, not constraint. Part 2 can note this and move on.

---

## Primitives Map Update

The coherence check refines the device-side architecture:

| Primitive | Session 2 status | After Part 1 | Change |
|-----------|-----------------|-------------|--------|
| Trigger Engine | Candidate — 3a device OR server, 3b server | **Revised** — 3a AND 3b server-only | Device has no trigger engine; server has unified trigger engine |
| Projection Engine | Settled (core) / Open (capabilities) | **Unchanged** — handles indexed views, state-change timestamps, cross-shape composition | No change |
| Shape Registry | Position emerging | **Unchanged** — shapes versioned, registry synced to devices | No change |
| Command Validator | Partially settled | **Unchanged** — validates at write time on device | No change |
| Expression Evaluator | Not named | **New** — single evaluator, context-parameterized (form vs. trigger) | Named as a shared component |

---

## Addendum: Layer Integrity and Anti-Pattern Boundary Checks

The six composition checks above verify that the five solutions share one system. This addendum runs a second pass: do the LAYERS maintain clean boundaries, and are the anti-pattern guards formally defensible?

---

### Check (g): Layer Boundary Integrity — Do the Layers Leak?

#### L0 → L1 boundary: Can a deployer assemble at L0 without knowing L1 internals?

**Test:** Does the S09 campaign's `stages` definition require knowing shape internals?

Looking at the campaign definition:
```
stages:
  - name: preparation
    shape: supply_verification   # ← reference by name
    pattern: capture_with_review
    deadline: 2026-05-03
```

The deployer references shapes by name, not by field structure. They don't need to know that `supply_verification` has a `cold_chain_functional` boolean. They only need to know the shape EXISTS and is appropriate for the stage. **Clean.**

**Test:** Does `review_role: chv_supervisor` in an activity definition leak shape structure?

No. `review_role` is a pattern parameter — it declares who reviews, not what they review. The review pattern applies to ANY shape. A deployer can assign a reviewer without knowing the shape's fields. **Clean.**

**Test:** Does the assignment `scope: geographic` leak anything?

No. Scope types are platform-fixed (ADR-3). The deployer selects one; they don't construct it from shape details. **Clean.**

**Verdict: L0 → L1 boundary holds.** L0 references L1 artifacts by name, never by internal structure.

#### L1 → L2 boundary: Do projection rules leak into L2 territory?

**Test:** The treatment→supply projection rule from S20:
```
projection_rule: treatment_supply_impact
source_shape: patient_encounter
source_field: treatment_given
target_entity: actor_supply
mapping:
  act: { item: act, quantity: -1 }
  ors: { item: ors, quantity: -1 }
```

This is a static lookup table: value → fixed output. No conditions, no operators, no boolean logic. It's structurally identical to a field's `options` list — a fixed enumeration of legal values and their meanings.

**The boundary test:** If a deployer needs "IF treatment_given == ACT AND dose > 2, THEN quantity: -2" — is that still a projection rule (L1) or has it crossed into L2?

**Answer: That's L2.** The moment a mapping table acquires conditionals (`AND`, `>`, `IF/THEN`), it's using the expression language, which is L2. The projection rule artifact type lives at L1 only when it's a **pure mapping table**: input value → fixed output. The test is mechanical:

- Does the mapping reference only ONE source field? → L1
- Does the output depend on a single value lookup (no combinations)? → L1
- Does any entry involve operators, comparisons, or multi-field conditions? → must be expressed as L2 logic feeding into the projection, not as a "smart" projection rule

**Formal guard:** Projection rules (L1) support only `source_value → {static_output}` mappings. Any conditional, multi-field, or computed derivation is L2 logic that writes an intermediate value which the projection then reads. This keeps projection rules as dead-simple lookup tables — data declarations, not logic.

**Verdict: L1 → L2 boundary holds — with a formal constraint.** Projection rules are value-to-value lookup tables. The instant they need conditions, the logic moves to L2.

#### L2 → L3 boundary: Is `set: {referral: true}` a side effect?

This is the sharpest concern. S20's logic rule:
```
- id: auto_referral
  when: "diagnosis == 'referral_needed'"
  set: { referral: true }
  lock: [referral]
```

`set:` modifies a field in the **event being authored** — the form the user is currently filling. Is this a side effect?

**The distinction that resolves this:**

| Property | L2 "set:" | L3 "create_event:" |
|----------|-----------|-------------------|
| **What is affected** | The current event being authored | A NEW event in the event store |
| **When it happens** | During form interaction, before submission | After event ingestion |
| **Scope of effect** | The user's own form — they can see and override (unless `lock:`) | System-wide — creates a persistent record affecting other actors |
| **Reversibility** | User can change the field (unless locked), or abandon the form entirely | Event is written; undoing requires a correction event |
| **Who knows** | Only the user filling the form | All actors who sync that subject's events |

**Formal definition of "side effect" in this system:**

> **A side effect is an action that creates, modifies, or deletes a persistent record in the event store.** L2's `set:` does not create a persistent record — it modifies form state before the user submits. The user remains the author. The submitted event is attributed to the user, not to the rule. L3's `create_event:` produces a new event attributed to the trigger. That's a side effect.

L2 is **form-scoped**: it shapes what the user authors, but the user authors it. L3 is **system-scoped**: it creates records the user didn't author.

**Verdict: L2 → L3 boundary holds — with a formal "side effect" definition.** L2 affects form state (pre-submission, user-attributed). L3 affects system state (post-ingestion, system-attributed). The boundary is authorship.

#### L3a → L3b boundary: Can a deployer misconfigure one as the other?

With Part 1's revision (both server-only), the difference is:
- **L3a (event-reaction):** "WHEN event arrives matching condition → create event." Requires: a source event type + condition.
- **L3b (deadline-check):** "WHEN expected response doesn't arrive within deadline → create event." Requires: a watched trigger, an expected response definition, a time threshold.

These have **structurally different configuration schemas**:
```
# L3a — requires 'when:' block
trigger: stockout_investigation
type: event_reaction
when: { event_type: capture, shape: stock_report, condition: "..." }
then: { create_event: ... }

# L3b — requires 'watches:' block + 'deadline:'
trigger: investigation_overdue
type: deadline_check
watches: { trigger: stockout_investigation, expected_response: {...} }
deadline: 48h
then: { create_event: ... }
```

A deployer who declares `type: event_reaction` must provide `when:`. A deployer who declares `type: deadline_check` must provide `watches:` + `deadline:`. **Misconfiguration is caught at validation** — the schemas are non-overlapping. You can't accidentally write a deadline-check as an event-reaction because the required fields differ.

**Verdict: L3a → L3b boundary holds.** The sub-types have structurally distinct configuration schemas. Misconfiguration fails validation.

---

### Check (h): Anti-Pattern Guard Formalization

#### AP-1 — Payload mapping depth limit

Session 2's payload_map uses:
```
payload_map:
  source_event_ref: "$source.id"
  alert_reason: "Stockout reported"
  priority: "urgent"
```

`$source.id` is a field reference. Is `$source.payload.temperature` also allowed? What about `$source.payload.nested.deep.value`?

**Formal limit:** Payload mapping references support exactly **two path segments**:
- `$source.id` — envelope field (id, type, subject_ref, actor_ref, timestamp, shape_ref)
- `$source.payload.<field_name>` — one payload field

Maximum depth: `$source.` + one of { envelope field | `payload.<field>` }. No deeper traversal. No `$source.payload.treatment_given.dose` — payload fields are flat (shapes define flat field lists, not nested objects). The shape definition's type system enforces this: fields have scalar types (text, number, boolean, select, geo, entity_ref, event_ref). None of these are objects with sub-fields.

**The guard is structural, not just a rule:** Because shapes define flat fields (no nested object type exists in the type system), there IS no `payload.field.subfield` to reference. The depth limit isn't a convention — it's a consequence of the shape type system having no nesting. If the shape type system ever adds an "object" field type, the payload_map depth limit must be revisited.

#### AP-2 — Expression language function ceiling

Part 1 stated "no functions." But S20 uses `entity.age_months` — where does age computation happen?

**Resolution:** `age_months` is a **projection-derived attribute**, not an expression-language function. The projection engine computes `age_months` from a stored `date_of_birth` field when projecting the entity. The L2 expression reads the result as a pre-computed field: `entity.age_months`. The expression evaluator does no date arithmetic — it reads a number and compares it.

**Formal position:** The expression language has **zero functions**. It has:
- Comparison operators: `==`, `!=`, `<`, `>`, `<=`, `>=`
- Membership: `IN`
- Boolean connectives: `AND`, `OR`, `NOT`
- Operands: field references (`payload.*`, `entity.*`), literals (string, number, boolean, null, list)

That's it. No `date_diff()`, no `concat()`, no `length()`, no `round()`. If a deployer needs a computed value, it's either:
1. A **projection-derived attribute** (the projection engine computes it from events and exposes it as a field on the entity), or
2. **Out of scope** — requires platform evolution

**The principled ceiling is architectural, not numerical:** The REASON for zero functions is this — every function added to the expression language is a function that must execute identically on every device, across OS versions, across sync states, forever. Functions create a cross-device compatibility surface. Keeping the expression language to operators-only means the evaluator is trivially portable. Computed values live in the projection engine, which is tested and versioned centrally.

**What about the projection engine's built-in computations?** The projection engine IS allowed computed attributes (age from date_of_birth, days_since_last_visit from event timestamps, count_of_events matching a filter). These are platform capabilities, not deployer expressions. The deployer doesn't write `age(dob)` — the platform provides `age_months` as a computed attribute type that the deployer activates by declaring a field as `type: date_of_birth` (or whatever typed computation the platform supports). The inventory of computed attribute types grows with the platform, not with configuration.

#### AP-5 — Chain vs. recursion: formal definition

The concern: can deadline-check A create event X, and deadline-check B watch for the absence of a response to event X, creating event Y? Is that a chain or recursion?

**Formal model:** The trigger graph is a directed graph where:
- **Nodes** = trigger definitions
- **Edges** = "watches output of" (deadline-check B watches trigger A's output events)

**Recursion** = a cycle in this graph (trigger A → trigger B → trigger A). Forbidden.

**Chain** = an acyclic path (trigger A → trigger B → trigger C). Allowed up to a maximum path length.

**The rule:**
1. The trigger dependency graph must be a **DAG** (no cycles). Validated at deploy time.
2. Maximum path length in the DAG = **2 edges** (3 nodes). Meaning: trigger A fires → trigger B watches A's output → trigger C watches B's output → **stop**. A fourth trigger watching C's output fails validation.
3. Only `deadline_check` triggers can have incoming edges (they're the only type that "watches" another trigger's output). `event_reaction` triggers don't watch other triggers — they watch raw events.

**Applying this to the user's example:**
- Trigger A (event_reaction): stockout reported → create investigation_task
- Trigger B (deadline_check): if investigation_task not responded to in 48h → create regional_alert
- Trigger C (deadline_check): if regional_alert not acknowledged in 72h → create national_escalation

This is a path of length 2: A → B → C. Three nodes. Legal under the rule. The graph is acyclic (no node's output feeds back to an earlier node). The structural types differ at each step (task_created → alert → escalation), satisfying AP-5's output-type-≠-input-type constraint.

**A deployer who tries to add trigger D watching C's output** gets a deploy-time validation error: "Trigger chain exceeds maximum depth of 2. national_escalation is already at the end of a 2-deep chain."

---

### Check (i): Platform Capability Elevation — Is There a Principled Boundary?

#### The implied principle from Session 2

Session 2 elevated "campaign progress monitoring" to a platform capability. The implied (but unstated) principle: **platform capabilities handle cross-subject aggregation; configuration handles per-event and per-subject logic.**

#### Systematic test

| Mechanism | Cross-subject aggregation? | Correctly classified? |
|-----------|--------------------------|----------------------|
| Campaign progress monitoring | Yes — count events across subjects in scope | Platform capability ✓ |
| Deadline check (L3b) | No — watches one trigger's output stream | Configuration (L3) ✓ |
| Supply deduction (projection rule) | No — derives from one event's fields | Configuration (L1) ✓ |
| Coverage reporting (% of population) | Yes — aggregate across all subjects | **Should be platform capability** |
| Case overdue detection | **Partially** — see analysis | Depends on framing |
| Stockout early warning (aggregate) | Yes — aggregate supply across actors | **Should be platform capability** |

#### The "case overdue detection" test

Case overdue detection bifurcates:
- **Per-case overdue** ("this case hasn't had an interaction in 14 days"): This is per-subject. The projection engine computes `days_since_last_interaction` for each case. An L2 expression or a view filter can surface it. **Configuration territory** — no aggregation needed.
- **Cross-case overdue reporting** ("how many cases in this district are overdue?"): This is cross-subject aggregation. Same category as campaign progress. **Platform capability territory.**

This distinction matters: the deployer uses L2/projection for the per-case alert, and a platform reporting capability for the district-level dashboard. These are NOT the same mechanism applied at different scales — they're structurally different (per-subject projection vs. cross-subject aggregation).

#### Is "campaign progress" a conflated capability?

The concern: is "campaign progress monitoring" actually three separable capabilities (aggregate counting, threshold comparison, time-windowed reporting) conflated because they co-occur in campaigns?

**Yes, it IS three separable capabilities.** And conflating them would trap deployers who need one without the others:

| Capability | What it does | Needed by |
|------------|-------------|-----------|
| **Aggregate counting** | Count events matching criteria across subjects in a scope | Campaigns, coverage reporting, stock aggregation, case load metrics |
| **Threshold comparison** | Compare an aggregate to a configured target | Campaign targets, alert thresholds, capacity monitoring |
| **Time-windowed grouping** | Group aggregates by time period | Campaign phases, periodic reporting (S02), trend analysis |

**Revised position:** "Campaign progress monitoring" is NOT a single platform capability. It's the **composition** of three platform capabilities that are independently useful:

1. **Aggregate projection** — platform capability that counts/sums events matching criteria across subjects within a scope. Deployer parameterizes: which event shape, which field, which aggregation (count/sum), which scope level. This is the foundation.

2. **Target comparison** — platform capability that compares an aggregate projection value against a configured target. Deployer parameterizes: target value, comparison type (≥, ≤, =), scope level, alert on breach.

3. **Time windowing** — platform capability that scopes aggregate projections to a time period. Deployer parameterizes: start/end dates (campaigns) or rolling window (monthly reporting).

A campaign COMPOSES all three. Monthly reporting (S02) uses #1 + #3. A stock threshold alert uses #1 + #2. They're orthogonal.

**AP-6 check:** Does this create overlapping authority? No — each capability does one thing. They compose at the activity/campaign definition level (L0 parameterization), not at the execution level. A deployer doesn't choose between three systems for counting — they have one (aggregate projection) and optionally add threshold or time-window parameters.

#### Where do these platform capabilities live in the gradient?

They are NOT configurable at L1–L3. They are **L0 parameterization of platform-provided capabilities** — same structural position as patterns:

| Platform capability | L0 parameterization | Example |
|------------|-------------|-----------|
| Aggregate projection | Shape, field, aggregation type, scope | "Count vaccination_records per district" |
| Target comparison | Aggregate ref, target value, comparison | "Compare vaccination count to target 5000" |
| Time windowing | Aggregate ref, start/end or rolling period | "During May 1–21, 2026" |
| Pattern (capture_with_review) | Shape, roles, scope | "Patient encounters reviewed by supervisor" |

All are platform capabilities, parameterized at L0, not built by deployers from L1–L3 primitives.

---

### Concept Count Audit

How many new concepts does a deployer need to learn to use the system?

**Deployer-facing concepts:**

| Category | Concepts | Count |
|----------|----------|-------|
| Artifact types | Shape, activity, logic rule, trigger, projection rule | 5 |
| Layers the deployer works in | L0 (assemble), L1 (define shapes), L2 (add logic), L3 (add triggers) | 4 |
| Expression constructs | 6 operators + 3 connectives + field references | ~10 |
| Patterns (current) | capture_only, capture_with_review, periodic_capture, campaign | 4 |
| Platform capabilities | Aggregate projection, target comparison, time windowing | 3 |
| Field types | text, number, boolean, select, multi_select, geo, entity_ref, event_ref, date | ~9 |

**Total deployer-facing concepts: ~35.** For comparison, DHIS2 has 50+ metadata types. CommCare has ~40 form/case concepts. This is within range for a system of this scope.

**Composability test:** Can these concepts be combined without knowing each other's internals?
- Shapes compose with activities (by name reference) ✓
- Logic rules compose with shapes (by field reference) ✓
- Triggers compose with shapes (by field reference) and with other triggers (by output watching, bounded) ✓
- Projection rules compose with shapes (by field/value mapping) ✓
- Platform capabilities compose with activities (by L0 parameterization) ✓

No concept requires understanding another concept's internal structure to use it. **Minimal and composable.**

---

## Updated Overall Verdict

| Check | Verdict | Findings |
|-------|---------|----------|
| (a) Artifact lifecycle | **Composes** | Two lifecycle models: event-coupled and config-package |
| (b) Dependency graph | **Composes** | Acyclic, one cascade rule |
| (c) Device/server split | **Composes — with revision** | 3a triggers server-only |
| (d) Expression language | **Composes** | One language, two contexts, zero functions |
| (e) Pattern framework | **Composes — intentional gap** | Structure sound, inventory grows with ADR-5 |
| (f) Envelope composition | **Composes** | 11 fields, no redundancy, no gaps |
| (g) Layer boundaries | **Composes — with formal definitions** | All 4 boundaries hold. "Side effect" defined. Projection rule conditionality bounded. |
| (h) Anti-pattern guards | **Composes — with formalization** | AP-1: depth from type system. AP-2: zero functions, computed attrs in projection. AP-5: DAG + max path 2. |
| (i) Platform capability boundary | **Composes — with decomposition** | Campaign progress decomposed into 3 orthogonal capabilities. Elevation principle: cross-subject aggregation = platform. |

**Nine checks, all compose.** Three required formalization (layer boundary definitions, anti-pattern rules, capability decomposition). One required structural revision (3a server-only). No gradient revision needed.

---

## Part 2 Reframe Assessment

**Is the Part 2 charter still valid?** Yes — no reframe needed.

Part 2's purpose (irreversibility filter on `shape_ref` format, `activity_ref` optionality, structural type vocabulary) is unchanged. The Part 1 findings simplify Part 2's work:
- The 3a server-only revision reduces the contract surface for trigger-related envelope fields (fewer components depend on them)
- The "one expression language" finding confirms the expression format is NOT an irreversibility concern (it's configuration, not envelope)
- The system actor identity (`system:trigger/{trigger_id}`) adds one minor format question to Part 2's scope
- The campaign progress decomposition into 3 platform capabilities is NOT an irreversibility concern (platform capabilities are code, not stored state)
- The formal anti-pattern boundaries are NOT irreversibility concerns (they're enforcement rules, not data formats)

**Recommendation:** Proceed to Part 2 with the Session 2 charter plus the system actor format question. No structural reframe.
