> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-004**. For a navigated reading guide, see [guide-adr-004.md](../guide-adr-004.md).
# ADR-004 Session 2: Scenario Walk-throughs Through the Configuration Lens

> Phase: 2 of 4 — Scenario Walk-throughs
> Date: 2026-04-12
> Input: Session 1 scoping (13-adr4-session1-scoping.md), gradient hypothesis (revised §6), 5 selected scenarios
> Purpose: Ground the four-layer gradient hypothesis in concrete deployment stories. For each scenario, produce configuration artifact sketches, identify which layer each element belongs to, find where the configuration hits walls, and surface positions on the three envelope questions (Q1, Q3, Q11).

---

## Method

Each scenario is walked through by asking: **"What would a deployer actually author to set up this operational activity?"**

For every configuration element that emerges, two questions are answered:
- **Dimension A**: What expressiveness layer? (L0 Assembly / L1 Shape / L2 Logic / L3 Policy)
- **Dimension B**: Parameterizing existing vocabulary, or extending it?

The walk-through produces:
1. Concrete configuration artifact sketches (what the deployer writes)
2. Layer classification for each element
3. Where the configuration hits a wall (if it does)
4. What feels natural vs. forced

Notation: artifact sketches use a hypothetical declarative format. The format itself is not a decision — the structure and content are what matter. Session 3 will evaluate formats.

---

## Walk-through 1: S00 — Basic Structured Capture

> "Someone needs to record a set of known details about something they've observed."

This is the P7 litmus test. If S00 requires anything beyond Layer 0 + Layer 1, the gradient is wrong.

### What the deployer needs

A deployer wants field workers to record household observations. They need:
1. A description of what to capture (fields, types, validation)
2. Who captures it (role, area)
3. What happens after capture (nothing — capture only)

### Configuration artifacts

**Artifact 1 — Shape definition** (Layer 1, extends vocabulary):

```
shape: household_observation
version: 1
fields:
  - name: head_of_household
    type: text
    required: true

  - name: household_size
    type: number
    required: true
    validation: { min: 1, max: 50 }

  - name: water_source
    type: select
    options: [piped, well, river, rainwater, other]
    required: true

  - name: latrine_type
    type: select
    options: [flush, pit_improved, pit_unimproved, none]
    required: true

  - name: notes
    type: text
    required: false
```

**Artifact 2 — Activity definition** (Layer 0, parameterizes):

```
activity: household_survey
shape: household_observation
pattern: capture_only
assignment:
  role: field_worker
  scope: geographic
```

That's it. Two artifacts. Five fields. One pattern.

### Layer classification

| Element | Layer | Dimension B | Rationale |
|---------|-------|------------|-----------|
| Shape `household_observation` | L1 | Extends | New shape in deployment vocabulary |
| Each field definition | L1 | Extends | New field in shape |
| Field validation `{min: 1, max: 50}` | L1 | Parameterizes | Uses platform's built-in range validator |
| Activity `household_survey` | L0 | Parameterizes | Selects shape, pattern, assignment |
| Pattern `capture_only` | L0 | Parameterizes | Platform-provided pattern |
| Assignment `field_worker / geographic` | L0 | Parameterizes | Uses ADR-3's assignment model |

### Where it hits a wall

Nowhere. S00 is clean L0 + L1.

### P7 assessment

**With a template**: If a generic "observation" template exists, an operations manager selects it, fills in field names and types, assigns a role → Layer 0 only. Time: ~5 minutes.

**From scratch**: Configuration specialist defines the shape (L1), operations manager creates the activity (L0). Time: ~15–20 minutes.

**Overhead from the platform**: Zero concepts the deployer doesn't need. No expressions, no triggers, no workflows, no cross-activity references. The activity definition is 5 lines. The shape is a field list. P7 holds.

### Envelope observations

**Q1** (event type vocabulary): The event this produces would be `type: "capture", shape_ref: "household_observation/v1"`. The structural type (`capture`) is platform-fixed. The domain meaning (`household_observation`) is in the shape reference. This separation feels right — the platform routes on `type`, the deployment interprets via `shape_ref`.

**Q3** (schema versioning): `shape_ref: "household_observation/v1"`. Format: `{shape_name}/v{version}`. Simple, readable, carries both identity and version.

**Q11** (activity reference): Not needed. The shape alone identifies the activity context. No `activity_ref` in the event. P7-compatible — zero extra envelope overhead for the simplest scenario.

---

## Walk-through 2: S06/S06b — Entity Registry Lifecycle + Schema Evolution

> "An organization keeps track of a recognized set of things. The kind of information being collected may change over time."

This stresses Q3 (schema versioning) and Q4 (on-device config coexistence).

### What the deployer needs

A deployer manages a facility registry. They need:
1. A shape describing facility attributes
2. An activity supporting registration, updates, and periodic re-verification
3. A review workflow for changes
4. Later: the ability to add a new field without breaking existing data

### Configuration artifacts

**Artifact 1 — Shape v1** (Layer 1, extends):

```
shape: facility
version: 1
fields:
  - name: facility_name
    type: text
    required: true

  - name: facility_type
    type: select
    options: [health_center, hospital, dispensary, community_unit]
    required: true

  - name: location
    type: geo
    required: true

  - name: status
    type: select
    options: [active, inactive, under_construction]
    required: true

  - name: capacity_beds
    type: number
    required: false
```

**Artifact 2 — Activity** (Layer 0, parameterizes):

```
activity: facility_registry
shape: facility
patterns:
  - register: capture_with_review
  - update: capture_with_review
  - verify: periodic_capture
    schedule: { interval: annual }
assignment:
  capture_role: field_supervisor
  review_role: district_coordinator
  scope: geographic
```

**Artifact 3 — Shape v2** (Layer 1, extends — additive change):

```
shape: facility
version: 2
based_on: facility/v1
added_fields:
  - name: has_solar_panel
    type: boolean
    required: false
    default: null

  - name: catchment_population
    type: number
    required: false
```

### The schema evolution scenario in detail

1. Shape v1 deployed. Devices download it. Captures use `shape_ref: "facility/v1"`.
2. Deployer creates shape v2 (adds two fields). Publishes to server.
3. Devices that sync receive v2. Their next capture uses `shape_ref: "facility/v2"`.
4. Devices still offline continue capturing under v1. Their events arrive later with `shape_ref: "facility/v1"`. **This is valid** — v1 was the active schema on their device at capture time.
5. Projection engine handles both: v1 events have `has_solar_panel: null` (field didn't exist). v2 events have the field populated.
6. Viewing a facility entity shows current state from both versions — v1 fields always present, v2 fields present for events captured after the upgrade.

### What this stresses

**Q3 — How shapes are versioned**: Shape v2 is authored as a delta (`based_on: facility/v1, added_fields: [...]`) but stored in the registry as a full snapshot (complete field list including inherited v1 fields). The event carries only the version reference (`facility/v2`), not the shape definition. The device resolves the reference against its local shape registry.

**Q4 — Config coexistence**:
- **In-progress work**: A field supervisor started filling a facility registration form under v1. Config v2 arrives mid-form. What happens? **The form completes under v1.** The next new form uses v2. No mid-form disruption. The device maintains at most 2 config versions simultaneously (current + previous for in-progress completion).
- **Config delivery timing**: Configuration is delivered as an atomic package during sync (not mid-session). The device applies new config after completing any in-progress work.
- **Backward compatibility**: v1 events remain valid forever. The shape registry keeps all versions. Projections know how to read across versions (v2's field list is a superset of v1's).

**Breaking vs. additive changes**: Adding a field is additive (v1 events get null for the new field). Removing a field is breaking — captured data references it. Renaming is breaking. Type changes are breaking.

The configuration model should classify changes:
- **Additive** (add field, add option value): automatic, no migration needed.
- **Deprecation** (mark field as hidden/retired): field remains in history, stops appearing in new forms.
- **Breaking** (remove field, change type, rename): requires explicit migration declaration and deployer acknowledgment.

### Layer classification

| Element | Layer | Dimension B |
|---------|-------|------------|
| Shape v1 | L1 | Extends |
| Shape v2 (delta) | L1 | Extends |
| Activity with 3 patterns | L0 | Parameterizes |
| Schedule (annual verification) | L0 | Parameterizes |
| Review workflow | L0 | Parameterizes (uses `capture_with_review` pattern) |
| Change classification rules (additive/breaking) | Platform-fixed | N/A — deployer doesn't configure this |

### Where it hits a wall

The distinction between "register," "update," and "verify" actions on the same entity using the same shape. These are different **patterns** applied to the same shape. The activity definition groups them, and the event's structural type (`capture` for registration, `capture` for update, `capture` for verification) doesn't distinguish them.

**Options**:
- A: Different structural event types: `register`, `update`, `verify`. Expands the platform's type vocabulary.
- B: Same type (`capture`), distinguished by a `pattern` field: `pattern: "register"`, `pattern: "verify"`.
- C: Same type, distinguished only by context (first capture of a new subject = registration; capture of existing subject = update; capture during annual cycle = verification).

Option A bloats the structural vocabulary (every deployment pattern becomes a type). Option C is fragile (how does the system know it's an "annual cycle"?). **Option B is the cleanest**: the platform provides a small set of structural types, and the activity definition declares which pattern each one follows. The `pattern` field is metadata on the activity configuration, and the event could carry it as a reference: `type: "capture", pattern_ref: "verify"`.

But wait — does `pattern_ref` need to be in the event envelope? Or can it be derived? If the event carries `activity_ref: "facility_registry"` and the activity configuration says "verification happens on an annual schedule," then a verification event can be identified as: capture + facility shape + facility_registry activity + matches annual schedule timing. That's derivable — no `pattern_ref` needed in the envelope.

**This means Q11 (activity reference) isn't just about campaigns.** Even routine entity management benefits from knowing which activity an event belongs to, because the activity carries pattern context. But for S06, the shape is unique to the activity — `facility` shape only appears in `facility_registry` activity. So `activity_ref` is optional here too (shape alone disambiguates).

**Holding verdict on `pattern_ref`**: wait for S09's pressure.

### What feels natural vs. forced

**Natural**: Shape definition, additive versioning, activity assembly.

**Forced**: Distinguishing register/update/verify within the same shape. The deployer's intent is clear ("these are different actions"), but the configuration model doesn't have a clean home for it beyond the activity-level pattern grouping.

---

## Walk-through 3: S09 — Coordinated Campaign

> "An organization decides to carry out a coordinated effort across many locations within a defined time window."

This is Q11's hardest test. A campaign is a time-bounded, multi-stage, multi-team coordination effort.

### What the deployer needs

A deployer plans a vaccination campaign across 5 districts during a 3-week window. Stages: preparation (verify stocks), execution (vaccinate), follow-up (adverse event reporting). Multiple teams, each assigned to geographic areas. Coordinators oversee progress.

### Configuration artifacts

**Artifact 1 — Shapes** (Layer 1, extends):

```
shape: supply_verification
version: 1
fields:
  - name: vaccine_type
    type: select
    options: [bcg, opv, measles, pentavalent]
    required: true
  - name: doses_available
    type: number
    required: true
  - name: cold_chain_functional
    type: boolean
    required: true

shape: vaccination_record
version: 1
fields:
  - name: individual_ref
    type: entity_ref
    required: true
  - name: vaccine_given
    type: select
    options: [bcg, opv, measles, pentavalent]
    required: true
  - name: dose_number
    type: number
    required: true
  - name: adverse_reaction_observed
    type: boolean
    default: false

shape: adverse_event_report
version: 1
fields:
  - name: individual_ref
    type: entity_ref
    required: true
  - name: reaction_type
    type: select
    options: [fever, rash, swelling, anaphylaxis, other]
    required: true
  - name: severity
    type: select
    options: [mild, moderate, severe]
    required: true
  - name: action_taken
    type: text
```

**Artifact 2 — Campaign definition** (Layer 0, parameterizes):

```
activity: measles_campaign_2026
type: campaign
time_window:
  start: 2026-05-01
  end: 2026-05-21
stages:
  - name: preparation
    shape: supply_verification
    pattern: capture_with_review
    deadline: 2026-05-03

  - name: execution
    shape: vaccination_record
    pattern: capture_only
    deadline: 2026-05-18

  - name: follow_up
    shape: adverse_event_report
    pattern: capture_only
    deadline: 2026-05-21
assignment:
  teams:
    role: vaccination_team
    scope: geographic
  coordinators:
    role: campaign_coordinator
    scope: geographic
```

**Artifact 3 — Logic** (Layer 2, parameterizes):

```
logic:
  shape: vaccination_record
  rules:
    - when: "adverse_reaction_observed == true"
      warn: "Please complete an adverse event report for this individual"
```

### Where the campaign model hits a wall

**Wall 1 — Progress monitoring**:

The coordinator needs to see: "District X has vaccinated 3,200 out of target 5,000 children. 4 days remaining." This requires:
- A **target** per area (configured where? Layer 0 as campaign parameter?)
- A **count** of vaccination_record events per area (aggregate across subjects)
- A **comparison** at a point in time

This is NOT an event-level concern. No single event triggers the progress calculation. It's an **aggregate projection**: count(vaccination_records in scope) / target.

The trigger model (Layer 3: "when event X happens, create event Y") does not handle this. Progress monitoring is a **scheduled aggregate evaluation**, not an event reaction.

**Options**:
- A: Platform provides "campaign progress" as a built-in projection capability. Deployers configure targets. Platform computes coverage. This is a platform feature, not deployer configuration.
- B: Layer 3 gets a new sub-type: "scheduled evaluation" that runs at sync time and compares aggregate values to thresholds. Deployers configure the evaluation.
- C: Progress monitoring is out of configuration scope — it's a reporting/analytics concern handled by the server tier.

**Assessment**: The deployer's need is legitimate and recurring (every campaign has targets and progress). The pattern is generic (count events matching condition in scope, compare to threshold). But the mechanism (aggregate evaluation, targets per scope node) is computationally different from event-triggered policies.

**Position**: This is Option A — platform-provided campaign progress capability, deployer-parameterized (targets, scope granularity, reporting frequency). Adding aggregate-query expressions to Layer 3 would be AP-1 (inner platform effect — you're building a query language). Keeping progress as a named platform capability with configurable parameters keeps the boundary clean.

This means "campaign progress monitoring" is a **platform pattern** at the same level as "capture_with_review" — something the platform provides and the deployer parameterizes, not something the deployer builds from primitives.

**Wall 2 — Activity reference in events (Q11 critical moment)**:

An organization runs routine immunization AND this campaign. Both produce `vaccination_record` events with the same shape. Same field workers. Same geographic scope.

How does anyone — the server, a coordinator, a report — distinguish a campaign vaccination from a routine vaccination?

**Without `activity_ref`**: You must infer from context. The event's timestamp falls within the campaign window? The actor was assigned to the campaign? Both conditions together? This is fragile:
- What if a CHV does a routine vaccination during campaign week? (Same actor, same time window, same shape.)
- What if the campaign extends by a week? (Time window changes; events captured during the extension must retroactively be considered campaign events.)
- What if a CHV assigned to the campaign also has routine duties? (Same actor, both activities.)

**With `activity_ref`**: The event says `activity_ref: "measles_campaign_2026"`. Unambiguous. The campaign event is distinguished at the source, by the person who captured it, at capture time. No inference needed.

**This settles Q11.** The `activity_ref` field is necessary. It is the deployer's declaration: "this event was captured as part of this activity." Without it, campaign analytics, progress monitoring, and cross-activity coordination are built on fragile inference. With it, they're built on explicit declaration.

**Is it optional?** Yes — S00 doesn't need it, S20's activities are shape-distinguished. But when needed, it's an envelope field. The platform must support it. Events that don't carry it have `activity_ref: null` (standalone).

### Layer classification

| Element | Layer | Dimension B |
|---------|-------|------------|
| 3 shapes | L1 | Extends |
| Campaign definition (stages, time window) | L0 | Parameterizes (`campaign` is a platform-provided activity type) |
| Assignment (teams + coordinators) | L0 | Parameterizes |
| Logic (adverse reaction warning) | L2 | Parameterizes |
| Coverage targets per area | L0 | Parameterizes (campaign parameter) |
| Progress monitoring | Platform capability | N/A — deployer parameterizes, doesn't build |
| Progress alerting (behind schedule) | Platform capability (campaign-progress pattern) | Parameterizes (threshold, recipients) |

### What feels natural vs. forced

**Natural**: Shapes for each stage, campaign as a time-bounded activity with stages, assignment of teams and coordinators. The campaign definition reads clearly — a coordinator could understand this artifact.

**Forced**: Progress monitoring. The deployer's intent ("tell me what's falling behind") is simple. The mechanism required (aggregate evaluation, threshold comparison, scheduled alerting) doesn't fit the event-triggered model. Accepting it as a platform capability rather than deployer-built configuration is the right call — but it means the platform must provide it.

**New insight**: Some operational patterns are common enough to be **platform-provided capabilities** rather than deployer-configured compositions. "Campaign with targets and progress monitoring" is one. "Capture with review" is another. These are composable templates, not unique compositions. The gradient hypothesis's Layer 0 already assumes this ("select from pre-built components") — the insight is that the component library must include campaign-level patterns, not just single-activity patterns.

---

## Walk-through 4: S12 — Event-Triggered Actions

> "Certain observations should automatically lead to a response. If the initial response doesn't happen, a broader or more urgent reaction follows."

This is the Viability Condition 2 scenario — the one the viability assessment flagged as highest risk for boundary collapse. AP-5 (Trigger Escalation Trap) was written specifically for this scenario.

### What the deployer needs

Three trigger types emerge from the scenario:

1. **Reactive trigger**: "When a stockout is reported, the supply coordinator should investigate." — Something happened → someone should respond.
2. **Threshold trigger**: "When a critical health value is recorded, the right people should know." — Something happened with a concerning value → alert.
3. **Deadline trigger**: "If the initial response doesn't happen, escalation follows." — Something should have happened but didn't → escalate.

### Configuration artifacts

**Artifact 1 — Shapes for trigger outputs** (Layer 1, extends):

```
shape: investigation_task
version: 1
fields:
  - name: source_event_ref
    type: event_ref
    required: true
  - name: description
    type: text
  - name: priority
    type: select
    options: [normal, urgent]

shape: clinical_alert
version: 1
fields:
  - name: source_event_ref
    type: event_ref
    required: true
  - name: alert_reason
    type: text
  - name: severity
    type: select
    options: [warning, critical]

shape: escalation_notice
version: 1
fields:
  - name: original_task_ref
    type: event_ref
    required: true
  - name: escalation_level
    type: number
  - name: reason
    type: text
```

**Artifact 2 — Reactive trigger** (Layer 3, parameterizes):

```
trigger: stockout_investigation
type: event_reaction
when:
  event_type: capture
  shape: stock_report
  condition: "payload.stock_level == 0"
then:
  create_event:
    type: task_created
    shape: investigation_task
    target_role: supply_coordinator
    scope: same_as_source
    payload_map:
      source_event_ref: "$source.id"
      description: "Stockout reported — investigate supply chain"
      priority: "urgent"
```

**Artifact 3 — Threshold trigger** (Layer 3, parameterizes):

```
trigger: critical_value_alert
type: event_reaction
when:
  event_type: capture
  shape: patient_observation
  condition: "payload.temperature > 39.5 OR payload.oxygen_sat < 90"
then:
  create_event:
    type: alert
    shape: clinical_alert
    target_role: clinic_supervisor
    scope: same_as_source
    payload_map:
      source_event_ref: "$source.id"
      alert_reason: "Critical vital signs recorded"
      severity: "critical"
```

**Artifact 4 — Deadline trigger** (Layer 3, parameterizes):

```
trigger: investigation_overdue
type: deadline_check
watches:
  trigger: stockout_investigation
  expected_response:
    event_type: task_completed
    shape: investigation_task
    matching: "subject_ref == $task.subject_ref"
  deadline: 48h
then:
  create_event:
    type: escalation
    shape: escalation_notice
    target_role: district_supply_manager
    scope: parent_of_source
    payload_map:
      original_task_ref: "$task.id"
      escalation_level: 1
      reason: "Investigation not completed within 48 hours"
```

### Decomposing the trigger types

The walk-through reveals **three fundamentally different trigger mechanisms**:

**Type A — Event reactions** (triggers 1 & 2):
- **When**: A specific event type with specific shape arrives, and a field condition is met.
- **Evaluation**: Synchronous, at event ingestion time. Can run on device OR server.
- **Scope**: Single event → single response. No aggregation, no temporal logic.
- **Complexity**: Low. Condition is a pure predicate on payload fields. Output is a single event with mapped fields.
- **AP-5 check**: Clean. Non-recursive (output event type differs from input). Bounded (one condition, one output). No chain.

**Type B — Deadline checks** (trigger 3):
- **When**: An expected response event doesn't arrive within a time window after a trigger event.
- **Evaluation**: **Asynchronous, server-side only.** Requires a scheduler that periodically checks: "for each task_created event, has a matching task_completed event arrived within 48 hours?" This cannot run on-device — offline devices cannot reliably track deadlines across all relevant events.
- **Scope**: Watches one trigger's output over time. Detects non-occurrence.
- **Complexity**: Medium. Requires matching logic ("response event matches trigger event by subject_ref"), temporal comparison ("48 hours since trigger event"), and scope traversal ("parent_of_source" for escalation).
- **AP-5 check**: The escalation itself (creating an escalation_notice) is bounded. But can an escalation_notice trigger ANOTHER deadline check? That would be recursion.

**Type C — Escalation chains** (extending trigger 3):
The deployer's natural intent: "If district supply manager doesn't respond in 72h either, escalate to national supply coordinator."

This is a second deadline_check watching the output of the first deadline_check. It is trigger recursion — exactly what AP-5 forbids.

**Resolution**: Bounded escalation. The platform provides a maximum escalation depth (proposed: 2 levels). The deployer can configure:
- Level 1: If task not completed in 48h → escalate to district level.
- Level 2: If district doesn't respond in 72h → escalate to regional level.
- Level 3: Not configurable. If needed → code / platform feature request.

This is a **hard wall**, not a soft guideline. The platform enforces it. A deployer who configures a level-3 escalation gets a validation error at deploy time, not a runtime failure.

### Complexity budget check

| Budget parameter | Limit | Trigger 1 | Trigger 2 | Trigger 3 |
|-----------------|-------|-----------|-----------|-----------|
| Predicates per condition | 3 | 1 ✅ | 2 ✅ | 1 ✅ |
| Events created per trigger | 1 | 1 ✅ | 1 ✅ | 1 ✅ |
| Trigger chain depth | 1 (non-recursive) | 0 ✅ | 0 ✅ | Watches another trigger's output ⚠️ |
| Triggers per event type | 5 | — | — | — |
| Max escalation depth | 2 | N/A | N/A | Level 1 ✅ |

Trigger 3's "watches another trigger's output" is not trigger recursion (trigger 3 doesn't fire from trigger 1's execution). It watches for the ABSENCE of a response to trigger 1's output event. This is a deadline check, not a chain. But it must be bounded — hence max escalation depth = 2.

### Where S12 hits the wall

**Wall 1 — Deadline checks are server-side only.** Offline devices cannot evaluate "has event X been responded to within Y hours?" because:
- They don't have all relevant events (other actors' responses may not be synced yet)
- Clock reliability across devices is advisory only (ADR-2 S3)
- The evaluation requires scanning the event timeline for matching response events

**Consequence**: Deadline-based triggers fire on the server, not on devices. The resulting events (escalation_notice, overdue_alert) are synced to target actors at next sync. This means escalation latency = sync interval + deadline. If sync happens once daily and the deadline is 48h, the escalation fires at 48–72h. This is acceptable for the use cases described.

**Wall 2 — Response matching is a judgment call.** "Has the investigation been completed?" requires defining what counts as completion. The deployer specifies `expected_response: {event_type: task_completed, shape: investigation_task, matching: "subject_ref == $task.subject_ref"}`. But what if the investigation is completed by a DIFFERENT event? What if someone visits the site and creates a `supply_delivery` event — does that count as "responded"?

**Resolution**: The deployer explicitly defines what counts as a response. The platform matches literally. If the deployer wants two event types to count as responses, they configure two `expected_response` entries (OR logic). The complexity budget allows this (max 3 predicates).

**Wall 3 — Payload derivation in trigger output.** Triggers 1–3 include `payload_map` sections that derive output payload fields from input event fields. This is field-level mapping, not arbitrary computation. But it's a capability that could grow:
- First: static values (`"description": "Stockout reported"`) ✅
- Then: source field references (`"source_event_ref": "$source.id"`) ✅
- Then: conditional values (`"priority": IF payload.stock_level == 0 THEN "urgent" ELSE "normal"`) ⚠️
- Then: computed values (`"days_overdue": NOW - $task.timestamp / 86400"`) ❌

The slope toward AP-1 is visible. The guard: payload_map supports **static values** and **direct field references** only. No expressions, no conditionals, no computation in the mapping. If the output payload needs computed values, the trigger creates the event with static/referenced values, and a Layer 2 expression on the output shape can compute derived fields.

### Layer classification

| Element | Layer | Dimension B | Notes |
|---------|-------|------------|-------|
| Output shapes (3) | L1 | Extends | New shapes for trigger outputs |
| Event-reaction triggers | L3 | Parameterizes | Uses platform's `event_reaction` type |
| Deadline-check triggers | L3 | Parameterizes | Uses platform's `deadline_check` type |
| Conditions (`payload.stock_level == 0`) | L3 | Parameterizes | Pure predicates on payload fields |
| Payload mapping | L3 | Parameterizes | Static values + field references only |
| Response matching | L3 | Parameterizes | Event type + subject match |
| Escalation depth limit | Platform-fixed | N/A | Hard wall: max 2 levels |
| Deadline evaluation timing | Platform-fixed | N/A | Server-side only |

### Layer 3 sub-type revision

The walk-through confirms that Layer 3 needs two explicit sub-types:

**Layer 3a: Event-reaction policies**
- Trigger: a specific event arrives with matching conditions
- Evaluation: synchronous, at ingestion (can be on-device or server)
- Output: one event of a pre-declared type
- Complexity budget: ≤3 predicates, 1 output event, no chains

**Layer 3b: Deadline-based policies**
- Trigger: expected response event doesn't arrive within configured deadline
- Evaluation: asynchronous, server-side only (runs at sync time or on schedule)
- Output: one event (escalation/alert)
- Complexity budget: ≤3 predicates for response matching, max 2 escalation levels
- Constraint: max deadline granularity = hours (not minutes, not seconds)

Both sub-types produce events through the normal event pipeline. Both are non-recursive. Both are bounded. The key difference is evaluation timing (synchronous vs. scheduled) and evaluation location (device-capable vs. server-only).

### What feels natural vs. forced

**Natural**: Event reactions (Type A). "When X happens with Y condition, create Z" — clean, predictable, bounded. The deployer's intent maps directly to the configuration artifact.

**Forced**: Escalation chains. The deployer's intent ("escalate if no response") is simple. The mechanism required (scheduled non-occurrence detection, response matching, scope hierarchy traversal) is complex. The gap between intent simplicity and mechanism complexity is real. The resolution — platform-provided deadline_check as a named mechanism with bounded escalation — is honest about this gap rather than pretending configuration can express it simply.

---

## Walk-through 5: S20 — CHV Field Operations

> "A community health volunteer works within a community providing basic health services."

This is the end-to-end deployer experience test — multiple activities for one role, cross-activity data relationships, the full configuration stack.

### What the deployer needs

A deployer sets up CHV operations: patient encounters (symptoms, diagnosis, treatment), supply tracking, supervisor review of encounters. These are **continuous daily activities**, not campaign-bound.

### Configuration artifacts

**Artifact 1 — Shapes** (Layer 1, extends):

```
shape: patient_encounter
version: 1
fields:
  - name: individual_ref
    type: entity_ref
    required: true
  - name: symptoms
    type: multi_select
    options: [fever, cough, diarrhea, rash, malaria_signs, pneumonia_signs]
    required: true
  - name: diagnosis
    type: select
    options: [malaria, pneumonia, diarrhea, referral_needed, healthy, other]
    required: true
  - name: treatment_given
    type: multi_select
    options: [act, ors, zinc, amoxicillin, paracetamol, none]
  - name: referral
    type: boolean
    default: false

shape: supply_count
version: 1
fields:
  - name: item
    type: select
    options: [act, ors, zinc, amoxicillin, paracetamol, rdt_kits]
    required: true
  - name: quantity
    type: number
    required: true
  - name: count_type
    type: select
    options: [physical_count, received, expired, lost]
    required: true
```

**Artifact 2 — Logic** (Layer 2, parameterizes):

```
logic:
  shape: patient_encounter
  rules:
    - id: show_treatment
      when: "diagnosis IN ['malaria', 'pneumonia', 'diarrhea']"
      show: [treatment_given]

    - id: auto_referral
      when: "diagnosis == 'referral_needed'"
      set: { referral: true }
      lock: [referral]

    - id: neonatal_warning
      when: "entity.age_months < 2"
      hide: [treatment_given]
      warn: "Neonates require facility-based care — refer immediately"
      set: { referral: true }
```

**Artifact 3 — Activities** (Layer 0, parameterizes):

```
activity: chv_patient_care
shape: patient_encounter
pattern: capture_with_review
review_role: chv_supervisor
assignment:
  role: chv
  scope: geographic

activity: chv_supply_tracking
shape: supply_count
pattern: capture_only
assignment:
  role: chv
  scope: geographic
```

**Artifact 4 — Trigger** (Layer 3, parameterizes — optional):

```
trigger: referral_alert
type: event_reaction
when:
  event_type: capture
  shape: patient_encounter
  condition: "payload.referral == true"
then:
  create_event:
    type: alert
    shape: referral_notification
    target_role: facility_nurse
    scope: same_as_source
    payload_map:
      source_event_ref: "$source.id"
      alert_reason: "Patient referred from community"
```

### Cross-activity data flow: the supply question

The deployer's natural expectation: "When a CHV treats a patient with ACTs, their ACT supply goes down."

**Three options**:

**Option A — Trigger-based automatic deduction**: A Layer 3 trigger watches patient_encounter events, and for each treatment given, automatically creates a supply_count event subtracting the used quantity. This is a trigger with payload derivation: reading `treatment_given` from the encounter, looking up the corresponding supply item, and creating a supply event with `quantity: -1, count_type: "used"`.

Problem: This requires a lookup function in the trigger's payload mapping (`lookup(payload.treatment_given, treatment_supply_map)`). That exceeds the "static values + direct field references only" constraint established in S12's walk-through. Adding lookup opens the door to AP-1.

**Option B — Manual recording**: The CHV records treatment AND separately records supply usage. Two events, two actions. More friction for the CHV, but each event is explicitly human-authored.

Problem: Friction. CHVs are busy. Recording the same information twice (treatment given + supply used) is exactly the kind of duplication that drives users to abandon systems.

**Option C — Projection-based derivation**: The supply projection reads BOTH supply_count events AND patient_encounter events. Supply stock = physical_count + received - used (from encounters) - expired - lost. The projection engine computes "used" by aggregating `treatment_given` fields from encounter events, using a configured mapping table (ACT → 1 dose per treatment).

Assessment: This keeps events honest (human-authored only), avoids trigger complexity, and uses the projection engine's existing capability (derive state from multiple event types). The mapping table (treatment → supply impact) is configuration — Layer 1 level (it defines how shapes relate to each other).

**Position**: Option C. The projection engine handles cross-shape aggregation. The treatment-to-supply mapping is a Layer 1 configuration artifact:

```
projection_rule: treatment_supply_impact
source_shape: patient_encounter
source_field: treatment_given
target_entity: actor_supply  # the CHV's own supply
mapping:
  act: { item: act, quantity: -1 }
  ors: { item: ors, quantity: -1 }
  zinc: { item: zinc, quantity: -1 }
  amoxicillin: { item: amoxicillin, quantity: -1 }
  paracetamol: { item: paracetamol, quantity: -1 }
```

This is a new artifact type — **projection rules** that define how one shape's events feed into another entity's projections. It doesn't create events. It instructs the projection engine how to compose data across shapes.

**Is this Layer 1 or Layer 2?** It's declarative (no expressions, no conditions — just a mapping table). But it defines cross-shape relationships, which is beyond simple field definition. Position: **Layer 1 extension** — it's schema-level declaration of how shapes relate. The deployer declares the mapping; the platform's projection engine applies it.

### Activity reference check (Q11)

Does S20 need `activity_ref`?

The CHV has two activities: `chv_patient_care` and `chv_supply_tracking`. Events use different shapes (`patient_encounter` vs. `supply_count`). The shape unambiguously identifies which activity the event belongs to. No `activity_ref` needed.

But: what if this CHV is ALSO participating in a vaccination campaign (S09) that uses a `vaccination_record` shape? Now the CHV has three activities. The vaccination events need `activity_ref: "measles_campaign_2026"` to distinguish campaign work from routine work. The patient encounter events don't need it (shape distinguishes).

**This confirms the Q11 position from S09**: `activity_ref` is optional. Used when shape alone doesn't disambiguate (campaigns, overlapping programs). Not used when shape is sufficient (routine single-activity work). S00 never uses it. S20's base scenario never uses it. S09 always uses it. S20+S09 combined: campaign events use it, routine events don't.

### Logic (Layer 2) observations

The `neonatal_warning` rule references `entity.age_months` — a property of the referenced individual entity, not a field in the current event's payload. The Layer 2 spec says "pure functions of the current event's payload fields + entity attributes."

This cross-reference works because:
1. The device has the individual entity's projection (synced via ADR-3's scope mechanism).
2. `entity.age_months` is a projection-derived attribute, available locally.
3. The expression is still pure (no side effects, no mutation) — it reads entity state but doesn't change it.

But it expands the data scope from "current event payload only" to "current event payload + referenced entity attributes." This is important for the complexity budget: how many entity attributes can an expression reference? Proposal: expressions can reference attributes of **one** entity (the subject of the event), not arbitrary entities. This prevents expressions from becoming cross-entity queries.

### Layer classification

| Element | Layer | Dimension B |
|---------|-------|------------|
| Shapes (2) | L1 | Extends |
| Logic rules (3 expressions) | L2 | Parameterizes |
| Activities (2) | L0 | Parameterizes |
| Role assignments | L0 | Parameterizes |
| Referral trigger | L3 | Parameterizes |
| Projection rule (treatment→supply impact) | L1 | Extends (new artifact type: cross-shape projection rule) |
| `activity_ref` | Not needed | — |

### Where it hits a wall

**Wall — Cross-shape projection composition**. The supply-tracking derivation from encounters requires the projection engine to compose data across shapes. This is architecturally clean (projections already derive state from events) but introduces a new configuration artifact type: projection rules that declare cross-shape relationships.

This artifact type wasn't in the original gradient hypothesis. It sits at Layer 1 (declarative, no logic, schema-level relationship declaration) but extends the vocabulary in a new direction — not just "what fields does this shape have" but "how does this shape's data feed into other projections."

**Session 3 should validate**: Can all cross-activity data flow be handled through projection rules without creating trigger-based automatic events? If yes, the trigger system stays simple (no payload derivation) and the projection engine absorbs the complexity (which is its job — projections ARE derived state).

### What feels natural vs. forced

**Natural**: Shape definition, skip logic, activities, role assignments. The CHV's daily workflow maps cleanly to two activities. An operations manager could understand and modify these artifacts.

**Forced**: The supply-treatment coupling. The deployer's intent ("supplies should track usage automatically") is clear. The mechanism — projection-based derivation with cross-shape mapping tables — is correct but introduces a new artifact type that didn't exist before. The alternative (triggers) is worse (adds payload computation to the trigger system). The projection approach is honest about what's happening: supply state is DERIVED from encounter data, not independently recorded.

---

## 6. Cross-Scenario Synthesis

### 6.1 Gradient Hypothesis Validation

**What held across all five scenarios:**

| Gradient element | Validated by | Status |
|-----------------|-------------|--------|
| Layer 0 (Assembly) as template selection + parameterization | S00, S06, S09, S20 | **Confirmed** — every scenario's activity definition was Layer 0 |
| Layer 1 (Shape Definition) as declarative schema | S00, S06, S09, S12, S20 | **Confirmed** — field lists with types, validation, options |
| Layer 2 (Logic) as pure expressions on payload + entity attributes | S20 | **Confirmed** — skip logic, computed defaults, warnings |
| Layer 3 (Policy) as bounded triggers | S12 | **Confirmed with revision** — needs 3a (event-reaction) and 3b (deadline-check) sub-types |
| Complexity budgets | All | **Held** — no scenario exceeded field counts, expression depth, or trigger limits |
| AP-6 (unified artifact pipeline) | All | **Not violated** — all artifacts could share a registry and versioning mechanism |
| Two-dimension model (expressiveness × vocabulary scope) | All | **Useful** — distinguished parameterize vs. extend at every layer |

**What needs revision:**

| Finding | Impact on hypothesis |
|--------|---------------------|
| Layer 3 has two sub-types (event-reaction vs. deadline-check) | Split Layer 3 into 3a and 3b with different evaluation models |
| Projection rules (cross-shape composition) are a new Layer 1 artifact type | Layer 1 extends beyond "field definitions" to include "cross-shape relationships" |
| Campaign progress monitoring is a platform capability, not deployer-built | Layer 0's component library must include campaign-level patterns |
| `payload_map` in triggers must be restricted to static values + field references | Add explicit constraint to Layer 3: no expressions in payload derivation |
| Entity attribute access in Layer 2 needs scoping | Add constraint: expressions reference at most one entity (the event's subject) |

### 6.2 Positions on Envelope Questions

#### Q1: Event Type Vocabulary — HYBRID (platform structural types + deployment shapes)

**Evidence**: Every scenario produced events that fall into a small set of structural types:

| Structural type | Meaning | Which scenarios |
|----------------|---------|-----------------|
| `capture` | Human records an observation or action | S00, S06, S09, S20 |
| `review` | Human evaluates another's work | S06, S20 |
| `alert` | System or human flags a condition | S12, S20 |
| `task_created` | System or human assigns work | S12 |
| `task_completed` | Human completes assigned work | S12 |
| `escalation` | System raises unresolved issue | S12 |
| `assignment_changed` | Authority changes scope/role bindings | All (implicit) |

This is a small, stable vocabulary (~10–15 structural types). It covers all five scenarios without scenario-specific types.

The deployment-specific meaning lives in `shape_ref` — what was captured, what was reviewed, what was alerted on. The platform routes on `type` (platform-fixed); the deployment interprets via `shape_ref` (deployment-defined).

**Position**: The `type` field in every event uses a **platform-fixed closed vocabulary**. The platform defines the structural types and their processing semantics (capture events go through conflict detection; reviews trigger status changes in projections; alerts route to target roles). Deployments define shapes that parameterize these types. No deployment can invent a new structural type.

**What this means for Q1 specifically**: Event types are platform-fixed. The deployment's vocabulary extension happens through shapes (Q2), not types (Q1). This is the lower-risk option — it keeps the event processing pipeline platform-controlled while giving deployers full freedom in data definition.

#### Q3: Schema Versioning — SHAPE REFERENCE IN ENVELOPE

**Format**: `shape_ref: "{shape_name}/v{version}"` — e.g., `"facility/v2"`, `"patient_encounter/v1"`

**Stored**: As a mandatory field in every event envelope. This is irreversible.

**Why this format**:
- Short and human-readable (debugging, logs, manual inspection)
- Carries both identity (which shape) and version (which schema) in one field
- Separable by convention (split on `/v`)
- Extensible (could add deployment namespace later: `"dhis2_import.facility/v1"`)

**Registry**: The shape registry maps `shape_name/version` → full field definition. Shapes are authored as deltas (deployer specifies changes from previous version), stored as complete snapshots (device resolves one reference, gets full field list). The registry is synced to devices as part of configuration delivery.

**Coexistence rule**: All shape versions remain valid forever (append-only). Events captured under v1 are never invalidated by v2's existence. Projections read across versions by treating missing fields as null.

#### Q11: Activity Reference — OPTIONAL ENVELOPE FIELD

**Evidence by scenario**:

| Scenario | Needs `activity_ref`? | Why / why not |
|----------|----------------------|---------------|
| S00 | No | Shape alone identifies context |
| S06 | No | Shape unique to activity |
| S09 | **Yes** | Same shape used in campaign AND routine work; must distinguish |
| S12 | No | Triggers don't need activity context |
| S20 | No (base) / Yes (if campaign overlaps) | Shape distinguishes routine activities; campaign overlap needs it |

**Position**: `activity_ref` is an **optional envelope field**. Present when the deployer's activity definition includes an activity identifier AND the shape is shared across activities. Absent when shape alone disambiguates.

**What this means for the envelope**: The event envelope gains two new fields from ADR-4:
- `shape_ref` (mandatory) — which payload schema this event was captured under
- `activity_ref` (optional) — which activity instance this event belongs to

Combined with ADR-1 and ADR-2's envelope fields, the full envelope becomes:

```
id              # ADR-1: UUID, client-generated
type            # ADR-1: platform-fixed structural type (capture, review, alert, ...)
shape_ref       # ADR-4: deployment-defined shape reference ("{name}/v{n}")
activity_ref    # ADR-4: optional activity instance reference
subject_ref     # ADR-2: typed identity reference
actor_ref       # ADR-2: who performed this action
device_id       # ADR-2: hardware-bound device identifier
device_seq      # ADR-2: device-local sequence number
sync_watermark  # ADR-2: server-assigned ordering
timestamp       # ADR-1: device_time (advisory)
payload         # ADR-1: shape-conforming data
```

**P7 check**: For S00, the deployer defines a shape and an activity. The event carries `type: "capture"`, `shape_ref: "household_observation/v1"`, `activity_ref: null`. Zero overhead from the activity reference. P7 holds.

### 6.3 Additional Positions (Q4, Q5, Q6, Q8)

#### Q4: Configuration Delivery — Atomic Package at Sync

Configuration is delivered as a versioned atomic package during sync. The device applies new configuration after completing in-progress work. The device maintains at most 2 config versions simultaneously (current + previous for in-progress completion).

Events reference their shape version, not their config package version. This means config updates don't invalidate in-progress work — the work completes under the shape version it started with, regardless of when the config update arrives.

#### Q5: T2 Boundary Line — Confirmed at Layer 3 → Code

The four-layer gradient survived all five scenarios. No scenario required expressiveness beyond Layer 3's bounded triggers. The boundary line is:

- **Configuration territory** (L0–L3): Shape definition, field logic, bounded triggers, deadline checks.
- **Code territory**: Aggregate analytics, custom projection logic, unbounded workflows, external integrations.

The one gray area — campaign progress monitoring — was resolved by adding it as a platform capability (deployer parameterizes, doesn't build). This is the right pattern for recurring operations needs that are too complex for Layer 3 but too common to require code per deployment.

#### Q6: S12 Scoping — Bounded with Two Sub-types

Event-triggered actions are strictly scoped as follows:
- **3a** (event-reaction): synchronous, single-event condition → single-event output. No chains.
- **3b** (deadline-check): asynchronous server-side, non-occurrence detection → single-event output. Max 2 escalation levels.
- Both: no expressions in payload derivation (static values + field references only).
- Both: non-recursive (output event type ≠ input event type, per AP-5).
- Both: complexity budget applies (≤3 predicates, ≤5 triggers per event type, ≤50 triggers per deployment).

#### Q8: Role-Action Permission Tables — Layer 0 Activity Parameters

Role→action mappings are defined at Layer 0 as part of the activity definition. Each activity declares which roles can perform which actions (capture, review, etc.) on which shapes. The permission table is delivered to devices as part of the configuration package.

This is simple parameterization: `{role: chv, can: [capture], shape: patient_encounter}`. No expression language needed. The Command Validator primitive (partially settled in the primitives map) evaluates these permissions at write time.

### 6.4 New Artifacts Discovered

The walk-throughs surfaced **three artifact types** not in the original gradient hypothesis:

| Artifact | Layer | Purpose | Example |
|----------|-------|---------|---------|
| **Projection rule** | L1 | Declares cross-shape data relationships for the projection engine | Treatment→supply impact mapping |
| **Campaign pattern** | L0 (platform-provided) | Defines multi-stage, time-bounded, target-tracked coordination | `type: campaign` with stages, targets, assignment groups |
| **Deadline policy** | L3b | Defines non-occurrence detection with escalation | `type: deadline_check` with expected response, deadline, escalation target |

### 6.5 What the Walk-throughs Didn't Stress

These questions weren't resolved because no selected scenario exercised them:

| Question | Why not stressed | Recommendation |
|----------|-----------------|----------------|
| Q7 (conflict rules configuration) | No scenario involved custom conflict resolution | Hold for ADR-5 or Session 3 adversarial test |
| Q9 (flag-type severity) | No scenario needed fine-grained flag configuration | Validate in Session 3 with S19 (offline sync) |
| Q10 (scope type extensibility) | All scenarios used geographic scoping | Validate in Session 3 with S08 (case management) or S15 (cross-program) |
| Q12 (sensitive-subject classification) | No scenario involved sensitive data categorization | Defer — evolvable, not blocking |

---

## 7. The Full Configuration Stack (End-to-End Deployer Experience)

Based on all five walk-throughs, here is the complete configuration stack a deployer works with, ordered by typical authoring sequence:

### Step 1: Define shapes (Layer 1 — Configuration Specialist)

For each type of information the deployment captures, define a shape:
- Field name, type, required/optional, validation, display hints
- Version number (starts at 1, increments on change)
- Change classification: additive (new fields) vs. deprecation (retiring fields) vs. breaking (type changes)

**Artifact count**: Typically 3–10 shapes per deployment.

### Step 2: Add logic to shapes (Layer 2 — Configuration Specialist)

For shapes that need conditional behavior:
- Skip logic (show/hide fields based on values)
- Computed defaults (set field value based on other fields or entity attributes)
- Conditional validation (warn or block based on expressions)
- Data scope: current event payload + subject entity attributes (one entity only)

**Artifact count**: 0–5 expressions per shape. S00 has zero. S20 has three.

### Step 3: Define cross-shape relationships (Layer 1 — Configuration Specialist)

If shapes have data dependencies:
- Projection rules declaring how one shape's data feeds into another entity's projections
- Mapping tables for cross-shape derivation

**Artifact count**: 0–3 projection rules per deployment. Most deployments have zero. S20 has one.

### Step 4: Define activities (Layer 0 — Operations Manager)

For each operational workflow:
- Name, shape, pattern (capture_only, capture_with_review, periodic_capture)
- Assignment (role, scope)
- Optional: time window (campaigns), schedule (periodic), stages

**Artifact count**: 1–5 activities per deployment.

### Step 5: Define triggers (Layer 3 — Configuration Specialist or Platform Specialist)

For cross-activity automation:
- Event-reaction triggers (when X happens with condition Y, create Z)
- Deadline triggers (when expected response to X doesn't arrive in time, escalate)

**Artifact count**: 0–10 triggers per deployment. S00 has zero. S12 has three.

### Step 6: Deploy

Configuration is packaged and synced to devices. Devices apply new configuration after completing in-progress work.

### Total deployer effort by scenario

| Scenario | L0 | L1 | L2 | L3 | Total artifacts | Specialist needed? |
|----------|-----|-----|-----|-----|----------------|-------------------|
| S00 | 1 activity | 1 shape | 0 | 0 | 2 | L1 for shape (or template) |
| S06 | 1 activity | 1 shape + 1 version | 0 | 0 | 3 | L1 for shape |
| S09 | 1 campaign | 3 shapes | 1 logic | 0 (progress is platform) | 5 | L1 for shapes |
| S12 | 0 | 3 shapes | 0 | 3 triggers | 6 | L3 for triggers |
| S20 | 2 activities | 2 shapes + 1 projection rule | 3 logic rules | 1 trigger | 9 | L1+L2+L3 |

S00 = 2 artifacts, no specialist required if using templates. S20 = 9 artifacts across all layers. The complexity grows with operational complexity, not with platform complexity. P7 holds.

---

## 8. Primitives Map Update

The walk-throughs refined the primitives map. Three items change:

| Primitive | Status before Session 2 | Status after | Change |
|-----------|------------------------|-------------|--------|
| Shape Registry | Open (ADR-4 Q2/Q3) | **Position emerging** — shapes as versioned declarative schemas, registry synced to devices, `shape_ref` in envelope | Q2/Q3 positions formed |
| Command Validator | Partially settled (auth side from ADR-3) | **Strengthened** — validates role-action permissions from activity definitions at write time | Q8 position formed |
| Assignment Resolver | Partially settled (mechanism from ADR-3) | **Strengthened** — resolves from activity configuration (role, scope, pattern) | L0 activity definitions are the assignment input |

**New candidate primitive**:

| Primitive | Invariant | Status | Source |
|-----------|-----------|--------|--------|
| Trigger Engine | Layer 3 policies are evaluated: 3a at ingestion, 3b on server schedule. Non-recursive, bounded, produces at most 1 event per trigger per source event. | **Candidate** | S12 walk-through |

---

## 9. Session 3 Charter

Session 3 must stress-test the positions formed here. Two activities:

### 9.1 Irreversibility Filter on Envelope Additions

Two new envelope fields proposed: `shape_ref` (mandatory) and `activity_ref` (optional). Apply the three-test irreversibility filter:

| Test | `shape_ref` | `activity_ref` |
|------|------------|----------------|
| **Stored state**: change after 1000 offline devices? | Every event carries it — migration = rewrite all events | Every event with it carries it — but null events don't need migration |
| **Contract surface**: how many components depend on it? | Projection engine, shape registry, conflict detector, sync layer | Projection engine (grouping), campaign progress, reporting |
| **Wrong choice recovery**: what does recovery require? | Data migration + protocol change | If optional with null default: cheap to add later. If mandatory: same as shape_ref |

Preliminary assessment: `shape_ref` is high-irreversibility (must get the format right). `activity_ref` is medium-irreversibility (optional means low migration cost, but format still matters).

### 9.2 Adversarial Stress Test

Five attack vectors for Session 3:

| Attack | Target | What it stresses |
|--------|--------|-----------------|
| "Shape with 200 fields" | L1 complexity budget (max 60) | Does the budget hold? What breaks at 61? |
| "Expression referencing 3 entities" | L2 data scope (single entity) | What if the deployer needs cross-entity expressions? Is single-entity sufficient? |
| "Trigger whose output is watched by another trigger's deadline check" | L3 escalation depth | Is 2 levels sufficient for real-world escalation chains? |
| "Campaign using the same shape as three other activities" | Q11 `activity_ref` | Does optional `activity_ref` handle complex multi-activity deployments? |
| "Schema v3 removes a required field that v1 and v2 had" | Q3 schema versioning | Breaking change handling — does the additive/deprecation/breaking classification hold? |

### 9.3 Unresolved Questions for Session 3

1. **Platform-provided patterns inventory**: Which operational patterns should be platform capabilities vs. deployer-composed? Session 2 identified `campaign_with_progress` and `capture_with_review`. What else? Should `case_management` be one?

2. **Shape format**: The walk-throughs used a hypothetical YAML-like format. The actual authoring format (JSON Schema, custom DSL, spreadsheet, visual builder) matters for the AP-3 (Configuration Specialist Trap) test. How easy is this format for a non-developer?

3. **Projection rule scoping**: Cross-shape projection rules (S20's treatment→supply mapping) need limits. How many cross-shape rules per deployment? Can they chain (shape A feeds shape B's projection, which feeds shape C's)? Chaining would risk emergent complexity.

4. **Structural type vocabulary completeness**: Is ~10–15 structural types sufficient? Walk-throughs used: capture, review, alert, task_created, task_completed, escalation, assignment_changed. What about: correction, transfer, merge_request, flag, close, reopen?

---

## 10. Key Findings Summary

### Confirmed

1. The four-layer gradient (L0 Assembly → L1 Shape → L2 Logic → L3 Policy) holds across all five scenarios.
2. Complexity budgets from Session 1.5 were not exceeded by any scenario.
3. P7 (simplest scenario stays simple) is protected: S00 requires 2 artifacts, zero expressions, zero triggers.
4. The two-dimension model (expressiveness × vocabulary scope) is analytically useful — every configuration element could be classified on both dimensions.
5. AP-6 (unified artifact pipeline) was not violated — all artifacts share the same structural pattern (declarative, versionable, diffable).

### Decided (pending Session 3 stress test)

6. **Q1**: Platform-fixed structural event types + deployment-defined shapes. The `type` field is a closed vocabulary; deployers extend through `shape_ref`.
7. **Q3**: `shape_ref` as mandatory envelope field. Format: `"{shape_name}/v{version}"`. Shapes stored as full snapshots, authored as deltas.
8. **Q11**: `activity_ref` as optional envelope field. Used when shape alone doesn't disambiguate (campaigns, overlapping programs). Null for standalone activities.

### Revised

9. Layer 3 splits into **3a** (event-reaction, synchronous) and **3b** (deadline-check, asynchronous server-side). Both bounded, non-recursive.
10. Layer 1 expands to include **projection rules** (cross-shape data relationships) alongside shape definitions.
11. Layer 0's component library must include **campaign-level patterns** (multi-stage, time-bounded, target-tracked) alongside single-activity patterns.

### Discovered

12. **Campaign progress monitoring** is a platform capability, not deployer-built configuration. Aggregate evaluation at server level, deployer-parameterized targets.
13. **Payload derivation in triggers** must be restricted to static values + direct field references. No expressions, no lookups. Cross-shape data flow goes through projection rules, not triggers.
14. **Escalation depth = 2 levels** is the hard wall for deadline-based triggers. Configurable, not negotiable.
