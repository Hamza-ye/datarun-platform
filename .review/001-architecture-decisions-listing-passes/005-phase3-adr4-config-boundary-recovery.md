# Phase 3: ADR-004 Configuration Boundary Recovery

## Context Capsule

- Recovery mode: archaeological reconstruction only; no redesign, no alternatives, no reopening.
- Anchor file: `002-phase0-decision-register.md`.
- Upstream recovered sources:
  - `003-phase1-adr2-identity-conflict-recovery.md`
  - `004-phase2-adr3-auth-sync-recovery.md`
- ADR target: ADR-004 S1–S14.
- Source files read:
  - `18-adr4-session3-part4-remaining-q-resolution.md`
  - `16-adr4-session3-part2-irreversibility-filter.md`
  - `15-adr4-session3-part1-structural-coherence.md`
  - `17-adr4-session3-part3-adversarial-stress-tests.md`
  - `14-adr4-session2-scenario-walkthrough.md`
- Reading order used: `18 → 16 → 15 → 17 → 14`.
- Supporting source used only for anti-pattern definitions:
  - `13-adr4-session1-scoping.md`
  - Reason: Phase 0 requires recovery of AP-1 through AP-6; later ADR-004 files reference the AP labels and confirm guards, but file 13 contains the compact catalog definitions.
- Settled outputs:
  - Event type vocabulary closure and processing-behavior boundary.
  - `shape_ref` and `activity_ref` envelope semantics.
  - Shape model, shape lifecycle, and relation to events, activities, and patterns.
  - Four-layer configuration gradient and the anti-pattern guards that forced it.
  - Expression language boundary: one language, two contexts, zero functions.
  - Trigger architecture: server-only, 3a/3b split, non-recursive DAG, max path length 2.
  - Atomic configuration delivery and maximum 2-version coexistence.
  - Scope type and sensitivity boundaries carried from ADR-003 deferrals.
  - Complexity budget calibration.
  - Deployer-parameterized policy boundary: flag severity, domain uniqueness, scope composition, sensitivity levels.
- Rejected / excluded:
  - Deployer-authored structural event types.
  - Domain event names as platform `type` values.
  - `pattern_ref` as an event-envelope field.
  - Mandatory `activity_ref` for all events.
  - Self-describing payloads as replacement for `shape_ref`.
  - Deployer-defined access-control logic or deployer-defined scope containment functions.
  - Field-level sensitivity as ADR-004 configuration.
  - Device-side trigger execution as settled behavior.
  - Dynamic cross-entity expression queries.
  - Trigger recursion or unbounded trigger chains.
  - Payload mapping with expressions or computed values.
- Deferred / open evolution:
  - `status_changed` type → ADR-005.
  - `context.*` expression scope → ADR-005.
  - Domain conflict resolution strategies / auto-resolution → ADR-005.
  - Pattern inventory and state progression semantics → ADR-005.
  - Field-level regulatory controls, encryption, erasure, or de-identification → platform evolution beyond ADR-004.
  - Multi-tenant namespace strategy for shape/activity identifiers → platform evolution that does not violate accepted decisions.
  - Raising/lowering complexity budgets → platform evolution that does not violate accepted decisions.
- Terms locked in this pass:
  - `shape_ref`
  - `shape`
  - `shape version`
  - `shape registry`
  - `activity_ref`
  - `activity instance`
  - `capture`
  - `review`
  - `alert`
  - `task_created`
  - `task_completed`
  - `assignment_changed`
  - `system actor`
  - `system:{source_type}/{source_id}`
  - `trigger` source type
  - `server-only triggers`
  - `atomic config delivery`
  - `config version`
  - `geographic`
  - `subject_list`
  - `activity`
  - `sensitivity classification`
  - `L0 Assembly`
  - `L1 Shape`
  - `L2 Logic`
  - `L3 Policy`
  - `four-layer gradient`
  - `L3→code boundary`
  - `shape definition`
  - `shape evolution`
  - `deprecation-only`
  - `breaking change`
  - `expression language`
  - `payload.*`
  - `entity.*`
  - `event.*`
  - `event-reaction trigger (3a)`
  - `deadline-check trigger (3b)`
  - `trigger DAG`
  - `max path length 2`
  - `complexity budgets`
  - `domain_uniqueness_violation`
  - `standard`
  - `elevated`
  - `restricted`

---

## 1. ADR Checkpoint

This pass is bounded by ADR-004 S1–S14 from the Phase 0 register.

| ADR-004 ID | Recovery use in this pass |
|---|---|
| S1 | Recover `shape_ref` as mandatory event-envelope field, its format, and the shape registry boundary. |
| S2 | Recover `activity_ref` as optional event-envelope field and activity-instance identifier. |
| S3 | Recover closed, platform-fixed, append-only event `type` vocabulary and the processing-vs-domain boundary. |
| S4 | Recover system actor identity format and trigger source type. |
| S5 | Recover why all triggers are server-only. |
| S6 | Recover atomic config delivery and max 2-version coexistence. |
| S7 | Recover no deployer-authored access logic and the platform-fixed scope type vocabulary. |
| S8 | Recover no field-level sensitivity and shape/activity sensitivity classification. |
| S9 | Recover the four-layer configuration gradient and side-effect boundary. |
| S10 | Recover shape semantics: typed payload schemas, versioning, delta authoring, snapshot storage, deprecation default. |
| S11 | Recover expression language boundary: one language, two contexts, zero functions, bounded scopes. |
| S12 | Recover trigger architecture: 3a/3b split, non-recursive, max path length 2. |
| S13 | Recover complexity budgets and calibration. |
| S14 | Recover deployer-parameterized policies: flag severity, domain uniqueness, scope composition, sensitivity levels. |

Everything below maps to one or more of these entries. Items that only appear in exploration but do not map to S1–S14 are marked rejected, provisional, deferred, or implementation strategy.

---

## 2. Upstream Boundary from Phases 1 and 2

ADR-004 does not redefine the event store, identity model, conflict model, authorization model, or sync model. It defines the configuration boundary that operates on top of those settled primitives.

| Upstream term / rule | How ADR-004 uses it | Boundary |
|---|---|---|
| `event` | All configured behavior ultimately creates, validates, interprets, or projects immutable events. | ADR-004 cannot introduce mutable state as a configuration escape hatch. |
| `payload` | Shapes define payload schemas. | Payload remains event data, not an embedded schema or program. |
| `subject_ref` | Shape/activity/projection logic may use the subject as the event target. | ADR-004 does not alter typed identity reference semantics. |
| `actor_ref` | Human or system actors author events. | System-generated events must still have auditable actor identity. |
| `accept-and-flag` | Domain uniqueness and flag severity reuse the flag model. | ADR-004 configures some flag rules/severity; it does not reject stale events. |
| `detect-before-act` | Flag severity affects whether flagged events trigger downstream behavior. | ADR-004 does not weaken detect-before-act. |
| `assignment-based access` | Activities parameterize roles/scopes; scope types are finalized here. | Deployers cannot author arbitrary access-control logic. |
| `sync scope = access scope` | Sensitivity and scope types affect sync/retention policies. | ADR-004 configures within platform-fixed boundaries. |
| `authority-as-projection` | Activity/scope/sensitivity config informs projections. | Authority context still stays out of the envelope. |

---

## 3. Decision Boundary Classification

ADR-004 recovered three classes of commitment.

### 3.1 Structural constraints

These touch stored events and therefore cannot be changed without migration or permanent dual semantics.

| Structural item | What is locked | Why it is structural |
|---|---|---|
| `shape_ref` | Mandatory envelope field, format `{shape_name}/v{version}`. | Every event carries it; event store, projection, shape registry, sync, form, validation, and conflict logic depend on it. |
| `activity_ref` | Optional envelope field, deployer-chosen activity instance identifier. | Events that carry it store it permanently; changing semantics would split historical interpretation. |
| `type` | Platform-fixed, closed, append-only, initial 6-value vocabulary. | The `type` value is stored in every event and drives platform processing. |

### 3.2 Strategy-protecting constraints

These do not primarily change stored event schema, but protect structural invariants.

| Strategy-protecting item | Protected invariant |
|---|---|
| `system:{source_type}/{source_id}` actor format | Every system-generated event remains auditable through `actor_ref`. |
| Server-only triggers | Device simplicity, no duplicate trigger emission, no divergent device/server trigger evaluation. |
| Atomic configuration delivery | Devices never run partially inconsistent configuration packages. |
| Max 2 config versions on device | Offline work-in-progress completes under old config while limiting local complexity. |
| No deployer-authored access-control logic | Access integrity remains platform-controlled. |
| Platform-fixed scope types | Scope containment remains auditable and bounded. |
| No field-level sensitivity | Event immutability and payload integrity are preserved. |
| Naming constraints for shape/activity identifiers | `shape_ref` and `activity_ref` stay parse-safe and grep-friendly. |

### 3.3 Initial strategies

These are settled as current architecture but evolvable without event migration.

| Initial strategy | Current position |
|---|---|
| Four-layer gradient | L0 Assembly → L1 Shape → L2 Logic → L3 Policy → Code boundary. |
| Shape authoring/storage | Delta-authored, snapshot-stored, versioned typed payload schemas. |
| Shape evolution | Additive and deprecation by default; breaking changes exceptional. |
| Expression language | One language, two contexts, operators + field references only, zero functions. |
| Trigger model | 3a event reaction, 3b deadline check, non-recursive DAG, max path 2. |
| Complexity budgets | 60 fields/shape, 3 predicates/condition, 5 triggers/event type, 50 triggers/deployment, 2-level escalation. |
| Deployer policy parameters | Flag severity, domain uniqueness, scope composition, sensitivity levels. |

---

## 4. Event Contract Additions from ADR-004

ADR-004 adds no free-form authority or workflow context to the event envelope. It commits three event-envelope semantics: `shape_ref`, `activity_ref`, and the closed `type` vocabulary.

### 4.1 `shape_ref`

Recovered rule:

```txt
shape_ref = "{shape_name}/v{version}"
shape_name = [a-z][a-z0-9_]*
version = monotonic integer
```

Semantics:

- `shape_ref` is mandatory in every event.
- It identifies the payload schema used at capture time.
- It is a reference to the shape registry, not an embedded schema.
- It is stable forever; historical events remain valid under their recorded shape version.
- The event payload must conform to the referenced shape version.

Why it was forced:

- Events captured by offline devices must remain interpretable when configurations evolve.
- Devices may submit older-shape events after newer versions are deployed.
- A mandatory versioned reference avoids self-describing payload bloat and avoids ambiguous replay.
- The shape registry can evolve independently, but event interpretation remains deterministic.

What is locked:

| Locked | Not locked |
|---|---|
| Field name `shape_ref`. | Shape authoring UI or file format. |
| Mandatory presence. | Shape storage implementation. |
| Parseable string format. | Shape registry backend. |
| Shape identity + version carried in event. | Shape contents, because new versions can be added. |

### 4.2 `activity_ref`

Recovered rule:

```txt
activity_ref = null | "{activity_instance_id}"
activity_instance_id = [a-z][a-z0-9_]*
```

Semantics:

- `activity_ref` is optional.
- It references an activity instance, not an activity template.
- It is auto-populated when a human capture happens inside an activity context.
- It is null when the event legitimately lacks activity provenance, such as imported historical data or loose records.
- It disambiguates events when the same shape appears in multiple activities.

Why optional survived:

- Mandatory `activity_ref` would fabricate provenance for imports or events with no activity context.
- Null is the honest value when activity context is unknown.
- For normal human-authored events, the device usually knows the current activity and stamps it automatically.
- Shape-only identification remains sufficient for simple cases.

Boundary:

| In boundary | Out of boundary |
|---|---|
| Correlates events to activity instances. | Does not encode pattern identity. |
| Supports campaign/program grouping. | Does not carry authority context. |
| Disambiguates same-shape multi-activity events. | Does not replace `shape_ref`. |

### 4.3 Event `type` vocabulary

Recovered rule:

```txt
type ∈ {
  capture,
  review,
  alert,
  task_created,
  task_completed,
  assignment_changed
}
```

Semantics:

- The `type` field is platform-fixed.
- The vocabulary is closed to deployers.
- The vocabulary is append-only for platform evolution.
- Types describe platform processing behavior, not domain meaning.
- Domain meaning lives in `shape_ref`, `activity_ref`, payload, and activity configuration.

Processing behavior boundary:

| Type | Processing role |
|---|---|
| `capture` | Human or system records structured data; shape validation and projection apply. |
| `review` | Review/assessment of a source event; source-event linking and review status apply. |
| `alert` | System or authorized actor creates a notification/attention event. |
| `task_created` | Work item is created; deadline and completion tracking may apply. |
| `task_completed` | Work item is resolved; deadline tracking can close. |
| `assignment_changed` | Authority/scope changes; sync scope recomputation applies. |

Why six types survived:

- Candidate deployer event names such as `case_opened`, `case_resolved`, `feedback`, `referral_accepted`, and `stock_received` are domain meanings, not structural processing behaviors.
- Most map to `capture` with different shapes.
- Transfers that change authority map to `assignment_changed`.
- Data-bearing transfer notes remain separate `capture` events, because authority change and data capture are two different facts.

Rule for future platform evolution:

> A new structural event type is justified only when it requires different platform processing behavior. If the difference is only domain meaning, use `capture` with a shape and activity context.

`status_changed` is not accepted in ADR-004. It is explicitly deferred to ADR-005 because it depends on whether state progression is platform-enforced or projection-derived.

### 4.4 System actor identity

Recovered rule:

```txt
actor_ref = "system:{source_type}/{source_id}"
```

For trigger-generated events:

```txt
actor_ref = "system:trigger/{trigger_id}"
```

Semantics:

- System-generated events still need authorship.
- Authorship remains in `actor_ref`; no new envelope field is introduced.
- The source type identifies the platform mechanism that emitted the event.
- The source ID points to the configured trigger/policy/source.

Boundary:

| In boundary | Out of boundary |
|---|---|
| Auditable source identity for system events. | Does not make triggers device-side. |
| Reuses `actor_ref`. | Does not create `system_ref` envelope field. |
| Supports trigger provenance. | Does not encode full trigger configuration in event. |

---

## 5. Shape Model

### 5.1 Definition

A `shape` is a typed payload schema referenced by events through `shape_ref`.

It defines:

- field names;
- field types;
- required/optional status;
- simple validation constraints;
- display hints and form-level structure where applicable;
- shape-local domain uniqueness declarations where configured.

It does not define:

- structural event type;
- actor authority;
- sync scope;
- trigger execution semantics;
- workflow state machine semantics;
- arbitrary code.

### 5.2 Shape lifecycle

| Stage | Semantics | Boundary |
|---|---|---|
| Authored as delta | A new version may be declared relative to a prior version. | Authoring convenience only. |
| Stored as snapshot | Registry stores a full shape definition per version. | Projection does not need to chase deltas at runtime. |
| Referenced by event | Event stores only `shape_ref`. | Event does not embed schema. |
| Additive evolution | Add optional fields/options. | Old events remain valid; projections supply null/default where needed. |
| Deprecation | Field remains in history but stops appearing in new forms. | Default evolution path. |
| Breaking change | Remove/rename/type-change. | Exceptional; requires explicit migration declaration and deployer acknowledgment. |
| Historical read | Projection reads event under its referenced shape version. | Shape versions remain valid forever. |

### 5.3 Shape relation to events

The event owns the fact that something happened. The shape owns the structure of the event payload.

```txt
event.type      = platform processing class
shape_ref       = payload schema and domain semantics
payload         = concrete values conforming to shape
```

This separation prevents domain vocabulary from leaking into structural event types.

Example:

| Domain action | Event `type` | `shape_ref` |
|---|---|---|
| Case opened | `capture` | `case_intake/v1` |
| Facility update | `capture` | `facility_update/v2` |
| Household feedback | `capture` | `service_feedback/v1` |
| Supervisor acceptance | `review` | `review_decision/v1` |

### 5.4 Shape relation to activities

Activities assemble shapes into deployer-visible work.

```txt
activity = shape(s) + pattern(s) + roles + scope + policy parameters
```

Shapes can be reused across activities. When they are reused, `activity_ref` disambiguates the activity context.

Boundary:

| Shape owns | Activity owns |
|---|---|
| Payload schema. | Operational context. |
| Field validation. | Role/scope assignment parameters. |
| Shape-local uniqueness. | Pattern selection and activity time window. |
| Schema version. | Activity instance identity. |

### 5.5 Shape relation to patterns

ADR-004 recovers the structure, not the final pattern inventory.

Settled in ADR-004:

- Patterns are platform-provided skeletons selected and parameterized by deployers.
- Activities reference patterns by name.
- Shapes are mapped into pattern roles or stages by activity configuration.
- Deployers do not author new pattern engines in configuration.

Deferred to ADR-005:

- Final pattern registry.
- State progression model.
- Subject-level vs event-level pattern composition rules.
- Whether state transitions require a `status_changed` type.

---

## 6. Activity Model

### 6.1 Definition

An activity is a deployer-configured operational unit. It assembles shapes, roles, scopes, patterns, schedules, targets, and policies into a runnable workflow.

Examples:

- `household_survey`
- `facility_registry`
- `measles_campaign_2026`
- `malaria_case_tracking`

An activity instance is what `activity_ref` points to.

### 6.2 Activity lifecycle

| Stage | Semantics | Boundary |
|---|---|---|
| Defined | Deployer creates an activity from shapes, roles, scopes, and patterns. | L0 Assembly. |
| Activated | Included in a config package. | Delivered atomically. |
| Used for capture | Device presents activity UI; events get `activity_ref` where context exists. | Device auto-populates. |
| Mutated | Definition can evolve for future behavior. | Historical events remain valid. |
| Retired | Activity stops being offered for new work. | Historical `activity_ref` stays meaningful. |

### 6.3 Activity vs activity template

`activity_ref` references an activity instance, not a reusable template.

Example:

| Concept | Identifier | Used by event? |
|---|---|---|
| Campaign template | `measles_campaign_template` | No. |
| 2026 campaign instance | `measles_campaign_2026` | Yes. |
| 2027 campaign instance | `measles_campaign_2027` | Yes. |

This preserves campaign-specific time windows, targets, reporting, and audit meaning.

---

## 7. Four-Layer Configuration Gradient

ADR-004's core boundary is the gradient from simple assembly to code.

| Layer | Name | Who uses it | What it controls | Expressiveness ceiling |
|---|---|---|---|---|
| L0 | Assembly | Operations manager | Activities, pattern selection, roles, scopes, schedules, policy parameters. | Parameterize existing platform vocabulary. |
| L1 | Shape | Configuration specialist | Payload schemas, field types, simple validations, shape-local uniqueness. | Declarative schema; no cross-field programming. |
| L2 | Logic | Configuration specialist | Form behavior, warnings, conditional validation. | Pure expressions over bounded scopes; no side effects. |
| L3 | Policy | Platform specialist / advanced config | Server-side event reactions, deadlines, domain policies. | Bounded triggers; no recursion; one side effect. |
| Beyond L3 | Code | Developer/platform evolution | New platform behavior or new primitives. | Real software development. |

### 7.1 Side-effect boundary

Recovered definition:

> A side effect is behavior that creates a persistent record in the event store.

Implications:

| Layer | Side effects allowed? | Reason |
|---|---:|---|
| L0 | No direct side effects. | It assembles artifacts. |
| L1 | No. | Shapes define payload structure. |
| L2 | No. | Form logic may warn, hide, default, validate; it does not create new events. |
| L3 | Yes, bounded. | Triggers may create one predefined event per source event / deadline condition. |
| Code | Yes. | Platform evolution can add new behavior. |

### 7.2 Anti-pattern catalog and guards

ADR-004's gradient exists to prevent six failure modes.

| Anti-pattern | Meaning | ADR-004 guard |
|---|---|---|
| AP-1 Inner Platform Effect | Configuration becomes a worse programming language. | No loops, no side-effectful L2, no expressions in payload maps, bounded L3. |
| AP-2 Greenspun Drift | Incremental functions turn config into half a language. | Zero functions in ADR-004 expressions; use platform evolution instead. |
| AP-3 Configuration Specialist Trap | Setup is technically no-code but practically specialist-only. | L0 templates for operations managers; L1/L2 for specialists; visible boundaries. |
| AP-4 Schema Evolution Trap | Initial setup works, later changes break history/offline devices. | `shape_ref`, versioned shapes, deprecation default, atomic config delivery. |
| AP-5 Trigger Escalation Trap | Triggers become an unbounded workflow engine. | Server-only triggers, non-recursive DAG, max path length 2, one output event. |
| AP-6 Overlapping Authority Trap | Multiple automation/config mechanisms compete over the same event. | One expression mechanism, one trigger mechanism, unified artifact pipeline. |

### 7.3 Layer transition signals

The boundary must be visible to deployers.

| Transition | Signal |
|---|---|
| L0 → L1 | “You are creating a new data structure.” |
| L1 → L2 | “You are writing rules; test them.” |
| L2 → L3 | “This affects multiple activities or creates records; review carefully.” |
| L3 → Code | “This needs platform development.” |

---

## 8. Expression Language Boundary

### 8.1 Recovered rule

ADR-004 settles one expression language with two evaluation contexts.

```txt
expression = operators + field references
functions = none
loops = none
user-defined abstractions = none
side effects = none
```

Allowed scopes in ADR-004:

| Scope | Meaning | Context |
|---|---|---|
| `payload.*` | Current event's in-progress or source payload fields. | L2 form and L3 trigger. |
| `entity.*` | Projected attributes of the event subject/entity. | L2 form and L3 trigger where available. |
| `event.*` | Envelope/source-event fields available to trigger evaluation. | L3 trigger. |

Deferred:

| Scope | Status |
|---|---|
| `context.*` | Deferred to ADR-005; emerged as plausible pre-resolved actor/workflow context. |

### 8.2 Why one language

One language prevents AP-6. L2 form conditions and L3 trigger conditions share syntax. What differs is context and output behavior.

| Context | Input data | Output |
|---|---|---|
| L2 form context | In-progress `payload.*`, `entity.*`. | UI behavior, warning, validation result, defaulting. |
| L3 trigger context | Synced source event, `event.*`, `payload.*`, projected `entity.*`. | Boolean decision to create one configured event. |

### 8.3 Why zero functions

The final ADR-004 position is stricter than early exploration. The expression language has operators and field references only; it does not start with math/date/string functions.

Reasoning:

- Built-in functions are the slope into AP-2.
- Date math, string manipulation, aggregation, joins, and cross-event queries require platform semantics and offline consistency guarantees.
- If a need is common, it should become a platform-provided value or capability, not an ad hoc expression function.
- Computed or derived attributes belong in projections or platform capabilities, then are read as fields.

### 8.4 Payload mapping is not the expression language

Trigger output payload mapping is deliberately simpler than expressions.

Allowed:

- static values;
- direct source field references.

Not allowed:

- expressions;
- conditionals;
- functions;
- lookups;
- computed values.

Boundary reason:

> Payload mapping must not become a hidden programming surface. The trigger condition decides whether a trigger fires; the payload map only copies known values into a predefined output shape.

---

## 9. Trigger Architecture

### 9.1 Recovered model

ADR-004 settles two L3 trigger subtypes.

| Trigger subtype | Name | Timing | Output | Location |
|---|---|---|---|---|
| 3a | Event-reaction trigger | On event ingestion/sync. | At most one configured event per source event. | Server only. |
| 3b | Deadline-check trigger | Scheduled/asynchronous check after a prior event. | Escalation/alert/task event. | Server only. |

### 9.2 Why server-only

Session 2 initially allowed 3a triggers to run on device or server. The coherence audit revised this to server-only.

Reasoning:

| Problem with device-side triggers | Why it matters |
|---|---|
| Device engine complexity | Devices would need a trigger engine in addition to form, validation, event store, and projection. |
| Duplicate emission | Device and server could both emit the same alert/task. |
| Divergent evaluation | Device may evaluate against stale or partial projection while server has broader state. |
| Offline uncertainty | Deadlines and cross-actor visibility cannot be authoritative on device. |

Recovered split:

| Need | Mechanism | Location |
|---|---|---|
| Immediate user feedback | L2 form logic | Device. |
| System-generated events | L3 triggers | Server. |
| Deadline escalation | L3b scheduler | Server. |

### 9.3 Non-recursion and DAG depth

Recovered rule:

```txt
trigger graph = DAG
max path length = 2
trigger output may be watched only within the bounded DAG
no cycles
no unbounded chain
```

Example legal path:

```txt
stockout event
  → trigger A creates investigation_task
  → trigger B watches investigation_task deadline and creates regional_alert
  → trigger C watches regional_alert deadline and creates national_escalation
```

A fourth trigger watching `national_escalation` exceeds the max path length.

### 9.4 Why max path length 2

Reasoning:

- It covers realistic escalation ladders: immediate → delayed → higher-level escalation.
- Deeper chains produce week-late automation noise.
- Longer chains become hard to inspect visually.
- Cycle detection and deploy-time validation stay simple.
- Anything beyond the in-system escalation ladder is operational/human response or platform code.

---

## 10. Configuration Delivery and Version Coexistence

### 10.1 Atomic configuration delivery

Recovered rule:

> Configuration is delivered as an atomic package during sync.

Semantics:

- The server validates a whole config package before deployment.
- Devices apply config as a coherent package, not as partial artifact updates.
- In-progress work completes under the version it started with.
- New work uses the newest applied config version.

### 10.2 Max 2 versions on device

Recovered rule:

```txt
device config versions = current + previous_for_in_progress
```

Reasoning:

- Offline devices need old config long enough to complete in-progress work.
- Unlimited coexistence would grow device complexity and validation burden.
- Two versions solve the documented offline transition case without turning devices into config archives.

### 10.3 Artifact lifecycle split

ADR-004 recovers two lifecycle models.

| Lifecycle model | Artifact types | Event reference | Versioning |
|---|---|---|---|
| Event-coupled | Shapes | Mandatory `shape_ref`. | Explicit version in every event. |
| Config-package | Activities, logic rules, triggers, projection rules, campaign/activity definitions | None or optional ID-only `activity_ref`. | Package-level delivery, not per-artifact event version. |

### 10.4 Deploy-time validation

Deploy-time validation is the guardrail that prevents runtime configuration inconsistency.

It checks:

- missing shape references;
- deprecated fields used by logic or triggers;
- invalid trigger DAGs;
- same-shape multi-activity warnings requiring `activity_ref`;
- complexity budgets;
- invalid scope composition;
- invalid sensitivity placement;
- dependency graph acyclicity.

Boundary:

> Invalid configuration packages fail before deployment. Runtime devices should not discover broken configuration by executing it.

---

## 11. Scope Types and Access-Control Boundary

ADR-003 deferred final scope vocabulary. ADR-004 settles it.

### 11.1 Scope types

Recovered fixed scope types:

| Scope type | Semantics |
|---|---|
| `geographic` | Actor assignment covers subjects in a geographic hierarchy/area. |
| `subject_list` | Actor assignment covers an explicit set/list of subjects. |
| `activity` | Actor assignment covers an activity context or campaign/program instance. |

### 11.2 Scope composition

Recovered rule:

- Scope types are platform-fixed.
- Deployers compose scope parameters; they do not author containment logic.
- Multiple scope dimensions compose through platform-defined containment semantics.
- The documented final position is composable `AND` across scope dimensions where multiple dimensions apply.

Example:

```txt
assignment scope = geographic(district_a) AND activity(measles_campaign_2026)
```

The actor is authorized for campaign work only inside the assigned geography.

### 11.3 Why deployer-defined scope logic was rejected

Rejected path:

```txt
custom_scope_rule:
  actor.region == subject.region OR actor.program == subject.program
```

Reasoning:

- It becomes deployer-authored access-control code.
- Access-control bugs become data exposure, not merely bad workflow.
- It violates ADR-003's assignment-based access and scope-containment model.
- It creates AP-1/AP-2 pressure in the most security-sensitive part of configuration.

ADR-004 S7 therefore locks a platform-fixed scope vocabulary.

---

## 12. Sensitivity Boundary

ADR-003 deferred sensitive-subject/data classification. ADR-004 resolves it as shape/activity-level classification.

### 12.1 Recovered sensitivity levels

| Level | Semantics |
|---|---|
| `standard` | Normal operational data. |
| `elevated` | Higher sync/retention/audit care. |
| `restricted` | Strongest platform-handled sync/retention/audit controls. |

### 12.2 Placement

Allowed:

- shape-level sensitivity;
- activity-level sensitivity.

Rejected:

- field-level sensitivity as deployer configuration.

Reasoning:

- Events are immutable payload units.
- Field-level sensitivity would require partial payload redaction/encryption/filtering semantics.
- That creates multiple interpretations of one stored event payload.
- It risks breaking append-only traceability and projection determinism.

Boundary:

> Sensitivity classification identifies which event classes require stricter treatment. It does not implement regulatory erasure, de-identification, field-level encryption, or field-level redaction.

Those are platform evolution fronts, not ADR-004 configuration.

---

## 13. Deployer-Parameterized Policies

ADR-004 S14 settles a small set of policy parameters deployers may choose within platform-fixed mechanisms.

### 13.1 Flag severity

Recovered rule:

- Platform defines flag categories and possible severities.
- Deployer can override severity defaults at deployment level.
- Initial severity configuration is a simple parameter map, not an expression system.

Example policy surface:

```txt
flag_severity:
  scope_stale: informational
  role_stale: blocking
  temporal_expired: informational
  concurrent_state: blocking
  stale_reference: informational
  duplicate_identity: blocking
  cross_lifecycle: informational
  domain_uniqueness: warn
```

Boundary:

| In boundary | Out of boundary |
|---|---|
| Selecting severity from fixed vocabulary. | Writing custom flag handling logic. |
| Deployment-level defaults. | Arbitrary per-event condition severity rules. |
| Interaction with detect-before-act. | Replacing detect-before-act. |

### 13.2 Domain uniqueness

Recovered rule:

- Domain uniqueness is a shape-declared constraint.
- It is checked optimistically on device where possible.
- It is authoritative on server at sync.
- Violations use the existing flag/conflict infrastructure.
- Flag category: `domain_uniqueness_violation`.

Example:

```txt
uniqueness:
  shape: household_visit
  scope: [subject_ref, activity_ref]
  period: calendar_week
  action: warn
```

Boundary:

| Domain uniqueness is | Domain uniqueness is not |
|---|---|
| Cross-event validation rule. | General conflict-resolution automation. |
| Server-authoritative at sync. | Guaranteed complete on offline device. |
| A source of flags. | A reason to reject immutable events. |

Domain conflict resolution strategies remain deferred to ADR-005.

### 13.3 Scope composition

Recovered rule:

- Deployers compose platform-fixed scope types.
- They parameterize `geographic`, `subject_list`, and `activity` scopes.
- They do not define new scope types or containment evaluators.

### 13.4 Sensitivity levels

Recovered rule:

- Deployers choose `standard`, `elevated`, or `restricted` at shape/activity level.
- Platform behavior interprets those levels for sync, retention, and audit.
- Field-level sensitivity is outside ADR-004.

---

## 14. Complexity Budgets

Complexity budgets are deploy-time validation rules. They are initial strategy, not event schema.

| Budget | Settled initial value | Reason |
|---|---:|---|
| Fields per shape | 60 | Above real-world observed large forms while forcing oversized forms to split. |
| Predicates per condition | 3 | Keeps expressions human-readable and testable. |
| Triggers per event type | 5 | Prevents trigger fan-out. |
| Total triggers per deployment | 50 | Forces prioritization and keeps validation tractable. |
| Escalation levels | 2 | Covers realistic immediate → regional/national escalation ladder. |
| Config versions on device | 2 | Current plus prior for in-progress work. |

### 14.1 Field budget calibration

Reasoning recovered from stress testing:

- Comprehensive household registration was around 45 fields.
- Medical intake forms were around 30–40 fields.
- Quarterly stock reports were around 15–20 fields.
- A 60-field cap is above practical high-end forms while preserving mobile usability.

If a legitimate form exceeds 60 fields, the intended modeling move is split into multiple shapes/events under one activity.

Example:

```txt
assessment_demographics/v1
assessment_clinical/v1
assessment_environment/v1
assessment_supplies/v1
```

Same subject. Multiple captures. One activity orchestration.

### 14.2 Trigger depth calibration

A max path length of 2 allows:

```txt
source event → task_created → alert/escalation
```

or:

```txt
source event → investigation_task → regional_alert → national_escalation
```

depending on whether path length is counted as edges. The settled constraint is the documented ADR-004 budget: 2-level escalation / max path length 2.

The recovered meaning is bounded escalation, not arbitrary workflow automation.

### 14.3 Evolvability

Budgets can change by platform evolution because they are validation rules, not event semantics.

Classification:

| Change | Classification |
|---|---|
| Raise 60 fields to 80 | Platform evolution that does not violate accepted decisions. |
| Reduce 50 triggers to 30 | Platform evolution with migration/compatibility concerns for existing config, but no event migration. |
| Allow trigger depth 3 | Work on an open/underexplored front; must re-run AP-5 checks. |

---

## 15. Primitive Boundaries Recovered from ADR-004

### 15.1 Event Contract

| Owns | Does not own |
|---|---|
| `type`, `shape_ref`, optional `activity_ref`, `payload` interpretation hooks. | Shape contents, activity definitions, trigger logic, workflow states. |

### 15.2 Shape Registry

| Owns | Does not own |
|---|---|
| Versioned shape definitions; full snapshots; deprecation/breaking metadata. | Event storage, activity scheduling, authorization. |

### 15.3 Activity Registry

| Owns | Does not own |
|---|---|
| Activity instance identifiers, role/scope parameters, pattern selection, schedules, targets. | Event payload schemas, structural event types, access evaluator implementation. |

### 15.4 Config Package Manager

| Owns | Does not own |
|---|---|
| Atomic package delivery, config versioning, device coexistence limit. | Authoring UI, event migration, arbitrary runtime patching. |

### 15.5 Deploy-Time Config Validator

| Owns | Does not own |
|---|---|
| Dependency validation, identifier rules, budget checks, trigger DAG validation, warnings. | Runtime conflict resolution, human approval workflow, deployer intent. |

### 15.6 Expression Evaluator

| Owns | Does not own |
|---|---|
| Boolean/simple expressions over bounded scopes. | Queries, joins, loops, functions, mutation, event creation. |

### 15.7 Form Engine

| Owns | Does not own |
|---|---|
| Shape rendering, L2 logic, local validation, immediate warnings. | System-generated events, server-triggered workflows. |

### 15.8 Trigger Engine

| Owns | Does not own |
|---|---|
| Server-side 3a event-reaction triggers, one output event. | Device-side immediate feedback, recursion, arbitrary payload computation. |

### 15.9 Deadline Scheduler

| Owns | Does not own |
|---|---|
| Server-side 3b deadline checks and bounded escalations. | Replacing workflow engine or state machine semantics. |

### 15.10 Scope Type Registry

| Owns | Does not own |
|---|---|
| Platform-fixed `geographic`, `subject_list`, `activity` containment semantics. | Deployer-authored access-control code. |

### 15.11 Sensitivity Classifier

| Owns | Does not own |
|---|---|
| Shape/activity-level `standard` / `elevated` / `restricted` labels. | Field-level redaction, erasure, encryption, or compliance mechanism. |

---

## 16. Rejected / Excluded Paths

| Path | Status | Reason |
|---|---|---|
| Deployer-defined structural event types | `[REJECTED]` | Event `type` drives platform processing and is stored in every event. |
| Domain event names as `type` values | `[REJECTED]` | Domain meaning belongs in shapes/activities, not structural type. |
| `case_opened`, `case_resolved`, `stock_received` as platform types | `[REJECTED]` | These map to `capture` with different shapes unless processing differs. |
| `status_changed` in ADR-004 | `[DEFERRED → ADR-005]` | Depends on state progression model. |
| Mandatory `activity_ref` | `[REJECTED]` | Fabricates provenance for imports/no-context events. |
| Removing `activity_ref` entirely | `[REJECTED]` | Same-shape multi-activity cases become ambiguous. |
| `pattern_ref` envelope field | `[REJECTED / not locked]` | Pattern can be derived from activity configuration; ADR-005 owns pattern semantics. |
| Embedded/self-describing payload schema | `[REJECTED]` | Bloats events and undermines registry/version discipline. |
| Deployer-authored access-control logic | `[REJECTED]` | Security-sensitive AP-1/AP-2 failure; violates ADR-003 model. |
| Deployer-defined scope containment functions | `[REJECTED]` | Scope containment must remain platform-fixed and auditable. |
| Field-level sensitivity as configuration | `[REJECTED]` | Creates partial-event semantics and threatens immutability. |
| Device-side trigger execution | `[REJECTED]` | Creates duplicate emission, divergence, and device engine complexity. |
| Trigger recursion / unbounded chains | `[REJECTED]` | AP-5 trigger escalation trap. |
| Cross-entity dynamic expression queries | `[REJECTED]` | Turns expression evaluator into a query engine. |
| Functions in ADR-004 expression language | `[REJECTED / initial lock]` | Prevents AP-2 drift. |
| Expressions in payload mapping | `[REJECTED]` | Payload mapping must stay static/ref-copy only. |
| Inline code in configuration | `[REJECTED]` | Crosses L3→code boundary without platform development discipline. |

---

## 17. Deferred / Open Evolution

| Item | Classification | Why it remains outside Phase 3 lock |
|---|---|---|
| `status_changed` structural type | Expansion of explicitly open front → ADR-005. | Only needed if state transitions require distinct platform processing. |
| `context.*` expression scope | Expansion of explicitly open front → ADR-005. | Depends on workflow/state context; must be pre-resolved, not dynamic query. |
| Domain conflict resolution strategies | Expansion of explicitly open front → ADR-005. | Auto-resolution interacts with state machines and workflow validity. |
| Auto-resolution policies | Expansion of explicitly open front → ADR-005. | ADR-004 configures uniqueness/severity; resolution automation is workflow-level. |
| Pattern inventory | Expansion of explicitly open front → ADR-005. | ADR-004 locks structure; ADR-005 locks state progression and pattern registry. |
| Field-level encryption/redaction/erasure | Platform evolution that does not violate accepted decisions if added carefully. | Sensitivity classification can identify data, but mechanism is not ADR-004 config. |
| Multi-tenant shape/activity namespace | Platform evolution that does not violate accepted decisions. | Naming constraints allow future prefixing; no current ADR lock. |
| Raising/lowering complexity budgets | Platform evolution that does not violate accepted decisions. | Budgets are deploy-time validation rules. |
| Richer deploy-time dependency tooling | Platform evolution that does not violate accepted decisions. | Tooling improves visibility; artifacts unchanged. |
| Visual authoring formats | Implementation strategy. | Visual/text tools must emit the same underlying artifacts. |

---

## 18. Terms Locked by ADR-004

| Term | Definition | ADR anchor |
|---|---|---|
| `shape_ref` | Mandatory event-envelope reference to a shape and version, format `{shape_name}/v{version}`. | S1 |
| `shape` | Typed payload schema for event data. | S1/S10 |
| `shape version` | Monotonic version of a shape used to interpret historical payloads. | S1/S10 |
| `shape registry` | Registry of versioned shape definitions used by devices, validation, and projection. | S1/S10 |
| `activity_ref` | Optional event-envelope reference to an activity instance. | S2 |
| `activity instance` | Deployer-configured operational unit such as a campaign occurrence or routine program instance. | S2 |
| `capture` | Structural event type for recording structured data. | S3 |
| `review` | Structural event type for review/assessment of a source event or work item. | S3 |
| `alert` | Structural event type for attention/notification. | S3 |
| `task_created` | Structural event type for creating a task/work item. | S3 |
| `task_completed` | Structural event type for completing a task/work item. | S3 |
| `assignment_changed` | Structural event type for authority/scope assignment changes. | S3 |
| `system actor` | Non-human actor identity used for system-generated events. | S4 |
| `system:{source_type}/{source_id}` | Actor reference format for system-generated events. | S4 |
| `trigger` source type | Source type used when trigger engine emits an event. | S4 |
| `server-only triggers` | Rule that all L3 triggers run on server, not device. | S5 |
| `atomic config delivery` | Config package delivered and applied as a coherent unit. | S6 |
| `config version` | Version of an atomic config package held by device/server. | S6 |
| `geographic` | Platform-fixed scope type for geography containment. | S7 |
| `subject_list` | Platform-fixed scope type for explicit subject-list containment. | S7 |
| `activity` | Platform-fixed scope type for activity-instance containment. | S7 |
| `sensitivity classification` | Shape/activity-level label driving sync/retention/audit treatment. | S8/S14 |
| `L0 Assembly` | Configuration layer for assembling activities from existing platform vocabulary. | S9 |
| `L1 Shape` | Configuration layer for defining typed payload schemas. | S9/S10 |
| `L2 Logic` | Configuration layer for pure form/validation logic without side effects. | S9/S11 |
| `L3 Policy` | Configuration layer for bounded server-side policy/triggers. | S9/S12 |
| `four-layer gradient` | L0→L1→L2→L3 configuration model with code boundary. | S9 |
| `L3→code boundary` | Boundary where general-purpose logic becomes platform development, not configuration. | S9 |
| `shape definition` | Declarative schema artifact defining fields/types/constraints. | S10 |
| `shape evolution` | Versioned change model for shapes. | S10 |
| `deprecation-only` | Default safe schema evolution path: retire from future use while keeping historical interpretation. | S10 |
| `breaking change` | Exceptional shape change requiring explicit migration/acknowledgment. | S10 |
| `expression language` | Bounded operators + field-reference syntax shared by L2 and L3. | S11 |
| `payload.*` | Expression scope for current/source payload fields. | S11 |
| `entity.*` | Expression scope for projected subject/entity attributes. | S11 |
| `event.*` | Expression scope for source event envelope fields in triggers. | S11 |
| `event-reaction trigger (3a)` | Server-side trigger reacting to event ingestion/sync. | S12 |
| `deadline-check trigger (3b)` | Server-side scheduled trigger checking unmet deadlines. | S12 |
| `trigger DAG` | Directed acyclic graph of bounded trigger dependencies. | S12 |
| `max path length 2` | Trigger-chain bound preventing unbounded escalation. | S12/S13 |
| `complexity budgets` | Deploy-time limits for shapes, expressions, triggers, and escalation depth. | S13 |
| `domain_uniqueness_violation` | Flag category emitted when deployer-configured uniqueness is violated. | S14 |
| `standard` | Baseline sensitivity level. | S14 |
| `elevated` | Higher sensitivity level affecting platform behavior. | S14 |
| `restricted` | Highest initial sensitivity level affecting sync/retention/audit. | S14 |

---

## 19. Output Summary

ADR-004 recovers the platform/deployment boundary.

Structural locks:

- every event has `shape_ref`;
- some events may have `activity_ref`;
- every event has a platform-fixed structural `type`;
- domain meaning does not enter `type`;
- new structural types are platform evolution only when processing behavior differs.

Strategy-protecting locks:

- system-generated events are auditable via `actor_ref`;
- triggers are server-only;
- config delivery is atomic;
- scope types are platform-fixed;
- sensitivity is shape/activity-level, not field-level.

Initial strategies:

- four-layer gradient;
- versioned shape model;
- one expression language with zero functions;
- bounded server trigger system;
- deploy-time complexity budgets;
- deployer-parameterized policy surfaces.

This pass completes the independent ADR-004 lineage recovery. Phase 4 can now consolidate vocabulary across Phases 1–3 and integrate ADR-005 vocabulary from the decision register.
