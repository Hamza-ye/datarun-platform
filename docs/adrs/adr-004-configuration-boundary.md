# ADR-004: Configuration Boundary

> Status: **Decided**
> Date: 2026-04-12
> Exploration: [Reading Guide](../exploration/guide-adr-004.md) · Raw: [13](../exploration/archive/13-adr4-session1-scoping.md) (Session 1), [14](../exploration/archive/14-adr4-session2-scenario-walkthrough.md) (Session 2), [15–18](../exploration/archive/) (Session 3 Parts 1–4)
> **Related addendum (2026-04-21)**: [ADR-002 Addendum — Envelope Type Mapping](adr-002-addendum-type-vocabulary.md) clarifies how identity/integrity events map onto the 6-type envelope vocabulary established by S3. Binding for all code.

---

## Context

ADR-001 established immutable events as the storage primitive and reserved envelope space for schema versioning and activity context. ADR-002 established four typed identity categories and the accept-and-flag conflict model, deferring domain-specific conflict rules to ADR-004. ADR-003 established assignment-based access control and sync-scope-as-authorization, deferring scope type extensibility, flag severity configuration, role-action permissions, and sensitivity classification to ADR-004.

ADR-004 answers the platform's defining question: **where does the platform end and the deployment begin?** Vision commitment V2 promises "set up, not built." Tension T2 — the highest-severity open tension — asks where "configuration" becomes "programming." Twelve sub-questions accumulated across the first three ADRs, spanning event type ownership, data shape definition, schema versioning, the expressiveness ceiling, trigger scoping, conflict rules, permissions, flag severity, scope extensibility, activity correlation, and sensitivity classification.

The exploration studied six comparable platforms (DHIS2, CommCare, ODK, OpenSRP, Salesforce, Odoo), catalogued six anti-patterns that configuration systems fall into (AP-1 through AP-6), walked five scenarios through a configuration lens, audited structural coherence across all solutions, applied the irreversibility filter to 24 candidate positions, and adversarially attacked the three envelope-touching positions. The result: ADR-004 is the broadest decision surface of the four ADRs but has the **smallest irreversibility surface** — two new envelope fields plus a value vocabulary for one existing field. The remaining decisions are configuration architecture: strategies that shape the deployer experience but can evolve without data migration.

---

## Decision

The platform uses **typed data shapes**, **platform-fixed event types**, and **a four-layer configuration gradient with hard complexity limits** as its configuration model. Fourteen sub-decisions follow, classified by permanence: structural constraints (S1–S3) lock what cannot change without data migration; strategy-protecting constraints (S4–S8) guard structural invariants through conventions and server-side enforcement; initial strategies (S9–S14) document the configuration architecture and deployer parameterization, all evolvable without affecting stored events.

### Structural Constraints

#### S1: Shape Reference in Envelope

**Every event carries `shape_ref`: a mandatory field declaring which data shape version the event's payload conforms to.** Format: `{shape_name}/v{version}`. Shape names match `[a-z][a-z0-9_]*`. Version is a monotonically increasing positive integer.

The naming constraint eliminates parse ambiguity (no `/` in names) and ensures predictable identifiers across all deployer-authored names. The shape registry — how shapes are stored, synced, and managed — is server-side and evolvable. New shapes are added without touching existing events. All shape versions remain in the registry forever; events are self-describing across versions.

Resolves Q3 (schema versioning scheme). Constrains Q2 (shape definition — shapes are typed, versioned, referenced from the envelope).

#### S2: Activity Reference in Envelope

**Events may carry `activity_ref`: an optional field identifying which activity instance the event was captured within.** Format: deployer-chosen identifier matching `[a-z][a-z0-9_]*`, or null. References an activity instance (e.g., `measles_campaign_2026`), not a definition template.

The device auto-populates `activity_ref` from the activity UI context at capture time. Human-authored events always have activity context available (the user opens an activity to fill a form), making the field effectively mandatory for human captures without schema enforcement. Trigger outputs inherit `activity_ref` from the source event. Imported historical data uses null — the honest answer when provenance is unknown.

Optionality is the escape hatch: events without `activity_ref` are valid and interpretable (shape alone may suffice). Making the field mandatory would force importers to fabricate provenance.

Resolves Q11 (activity model and correlation metadata).

#### S3: Structural Event Type Vocabulary

**The `type` field uses a platform-fixed, closed, append-only vocabulary of structural event types.** Types represent platform processing behavior, not domain meaning. Domain meaning lives in shapes.

Initial vocabulary (6 types):

| Type | Processing semantics |
|------|---------------------|
| `capture` | Human-authored data record → shape validation → projection pipeline |
| `review` | Human evaluation of another event → source linking → review-status projection |
| `alert` | Condition notification → target-role routing (escalation metadata in payload when applicable) |
| `task_created` | Work assignment → deadline tracking → response watch |
| `task_completed` | Task response → source linking → deadline cancellation |
| `assignment_changed` | Scope/role modification → sync scope recomputation (ADR-3) |

A new type is justified only when it requires different platform processing behavior. If the only difference is domain meaning, it is `capture` with a domain-specific shape. New types may be added (code change, no data migration). No type may be removed or renamed once events carry it.

Resolves Q1 (event type vocabulary ownership).

**With S1 and S2, the event envelope reaches its final form for the initial platform version — 11 fields committed across four ADRs:**

| Field | Source | Presence |
|-------|--------|----------|
| `id` | ADR-1 | Mandatory |
| `type` | ADR-1 + ADR-4 (S3) | Mandatory |
| `shape_ref` | ADR-4 (S1) | Mandatory |
| `activity_ref` | ADR-4 (S2) | Optional |
| `subject_ref` | ADR-2 | Mandatory |
| `actor_ref` | ADR-2 | Mandatory |
| `device_id` | ADR-2 | Mandatory |
| `device_seq` | ADR-2 | Mandatory |
| `sync_watermark` | ADR-2 | Mandatory (null until synced) |
| `timestamp` | ADR-1 | Mandatory |
| `payload` | ADR-1 | Mandatory |

### Strategy-Protecting Constraints

These guard structural invariants through conventions and server-side enforcement. The invariants they protect cannot change; the implementations can evolve.

#### S4: System Actor Identity Convention

**System-generated events use `actor_ref` format `system:{source_type}/{source_id}`.** `source_type` starts with `trigger` and grows as new system sources emerge (scheduler, import, migration). `source_id` is the configuration identifier of the specific trigger or job.

Protects V3 (trustworthy records): every event — human or system — has a non-null, traceable author. The convention is a value-format pattern within the existing `actor_ref` field, not a schema change. Evolvable via code change.

#### S5: All Triggers Execute Server-Only

**Both trigger types — event-reaction (L3a) and deadline-check (L3b) — evaluate and fire exclusively on the server.** Devices receive trigger outputs as normal events at the next sync.

Protects device simplicity: trigger evaluation requires cross-subject data and conflict-flag awareness that devices may not have. Relaxing this (adding device-side triggers) would require shipping the trigger engine, expression evaluator, and conflict-flag state to every device — significant complexity with no data migration, but a deliberate platform evolution.

#### S6: Atomic Configuration Delivery

**Configuration is delivered to devices as an atomic package at sync time. The device applies the new configuration only after in-progress work under the previous configuration completes. At most two configuration versions coexist on-device: current and previous (for finishing in-progress work).**

Protects against partial configuration causing inconsistent device behavior. A device never operates with a shape from config version N and a trigger from config version N+1.

Resolves Q4 (configuration versioning and on-device coexistence).

#### S7: No Deployer-Authored Access Control Logic

**Scope types are platform-fixed. Deployers select and compose scope types for each assignment but cannot define custom scope types or custom containment logic.** Access control evaluation — the scope-containment test from ADR-3 — runs platform code for a fixed set of scope types.

Protects access control integrity: bugs in custom scope logic are data-leak vulnerabilities, not data-quality issues. The platform owns the security-critical path.

Three initial scope types:

| Scope type | Containment test |
|-----------|-----------------|
| `geographic` | Subject's location within actor's area assignment (hierarchy containment) |
| `subject_list` | Subject's ID in actor's explicit assignment list |
| `activity` | Event's `activity_ref` in actor's permitted activities |

Assignments may combine multiple scope dimensions. All non-null dimensions must pass (AND composition). A null dimension means unrestricted on that axis. New scope types are platform evolution (code change, no data migration).

Resolves Q10 (scope type extensibility).

#### S8: No Field-Level Sensitivity Classification

**Sensitivity classification applies to shapes and activities, not individual fields within a shape. An event referencing a sensitive shape is wholly sensitive.**

Protects event immutability (ADR-1 S1): field-level redaction or selective purge within an immutable event violates append-only. If field-level handling is later needed (e.g., regulatory mandate), the path is event-level payload encryption with key-based access control — a major platform feature, not a configuration concern.

Partially resolves Q12 (the classification boundary; full parameterization in S14).

### Initial Strategies

These document the configuration architecture and deployer parameterization. Each can evolve without affecting stored events.

#### S9: Four-Layer Configuration Gradient

**The configuration-to-code boundary is a graduated four-layer architecture. Each layer adds expressiveness and a clear category of side effects:**

| Layer | Name | What deployers author | Side effects | Who |
|-------|------|----------------------|--------------|-----|
| L0 | Assembly | Activities, role-action mappings, flag severity, sensitivity levels, deployment parameters | None — structural wiring | Program manager |
| L1 | Shape | Field definitions (name, type, required/optional, validation), projection rules, uniqueness constraints | None — schema and data mapping | Configuration specialist |
| L2 | Logic | Show/hide conditions, computed defaults, conditional warnings | Form-scoped only — no persistent records created | Configuration specialist |
| L3 | Policy | Event-reaction triggers (3a), deadline-check triggers (3b) | System-scoped — creates persistent events in the event store | Configuration specialist |

**The boundary between L3 and code is explicit:** anything requiring logic beyond L3's constraints — custom integrations, bulk data processing, novel event processing, cross-deployment coordination — requires platform evolution. This boundary is discovered by reading documentation ("Layer 3 can do X but not Y"), not by hitting invisible walls.

Every prior art platform studied hits a coverage ceiling around 60–70% for non-trivial deployments. ADR-004 is honest about this: configuration handles the operational core; code handles the exceptional. The gradient ensures that deployers know exactly where they are and where the ceiling is.

The side-effect boundary between layers is the structural principle: L0/L1 declare structure (no runtime behavior), L2 affects form presentation (runtime, no persistence), L3 creates events (runtime, persistent). A capability's layer is determined by its side effects, not its complexity.

Resolves Q5 (T2 boundary line) and Q6 (S12 scoping — triggers are L3, bounded by S12 and S13).

#### S10: Shape Definition, Versioning, and Evolution

**Shapes are typed payload schemas, versioned explicitly via `shape_ref` (S1).**

- Authored as deltas (deployer specifies changes from the previous version). Stored as full snapshots (the registry holds the complete field set for each version).
- Change classification: **additive** (new optional fields), **deprecation** (field hidden from new forms, retained in schema), **breaking** (field removed or type changed).
- **The default evolution path is deprecation-only.** Deprecated fields appear as null in new-version events; old-version events retain their values. Breaking changes are exceptional: they require explicit deployer acknowledgment and server-side migration support.
- All versions remain valid forever (consistent with P3). The projection engine handles multi-version event streams by routing on `shape_ref`.

This means shapes grow but never shrink. Schema cruft is bounded by the 60-field complexity budget (S13): deployers who need radical restructuring create a new shape and activity rather than contorting an old shape through accumulated deprecations.

Resolves Q2 (data shape definition) and Q4 (partially — schema evolution within configuration versioning).

#### S11: Expression Language and Logic Rules

**One expression language serves all evaluation contexts. Two contexts exist: form (L2 logic rules on-device) and trigger (L3 conditions on-server). The language supports operators and field references only — zero functions.**

Form-context reference scopes:
- `payload.*` — current event's fields
- `entity.*` — subject entity's projected attributes (one entity, the event's subject)

Trigger-context reference scopes:
- `event.*` — the triggering event's envelope and payload fields

No cross-entity references. No dynamic queries. No aggregation. The expression evaluator reads pre-resolved values — it is not a query engine.

**Payload mapping** for trigger outputs: static values and source event field references only. No expressions in output payloads.

**Projection rules** (L1): pure value-to-value lookup tables. No conditionals, no expressions. A projection rule maps one field value to another through a static table. Cross-subject aggregation (e.g., "count of visits in district") is a platform capability, not a configuration expression.

#### S12: Trigger Architecture and Limits

**Layer 3 splits into two mechanisms with distinct execution models:**

- **3a — Event-reaction:** synchronous, server-side. A single-event condition evaluates against an incoming event. If true, produces exactly one output event. Fires during sync processing.
- **3b — Deadline-check:** asynchronous, server-side. Detects non-occurrence (expected event not received within a time window). Produces exactly one output event.

**Both mechanisms are non-recursive:** a trigger's output event type must differ from its input event type. This prevents trigger cascades (AP-5). The trigger dependency graph is a directed acyclic graph with a **maximum path length of 2** — at most 3 trigger nodes in any chain (e.g., event → investigation task → overdue alert → regional escalation). Deeper chains hit diminishing returns: automated responses to week-old situations are noise, not signal. The realistic escalation ladder (immediate → delayed → national) fits within 2.

#### S13: Complexity Budgets

**Hard limits enforced at deploy-time validation, not advisory guidelines.** The platform rejects configuration packages that exceed any budget. This is the primary defense against AP-1 (inner platform effect) and AP-3 (configuration specialist trap).

| Dimension | Limit | Rationale |
|-----------|-------|-----------|
| Fields per shape | 60 | Exceeds real-world maxima (~45 for the largest known field forms). Forces split into sub-forms for larger needs. |
| Predicates per condition | 3 | Keeps expressions human-readable and debuggable. |
| Triggers per event type | 5 | Bounds per-event processing cost. |
| Triggers per deployment | 50 | Bounds total trigger graph complexity to inspectable size. |
| Escalation depth | 2 levels | Matches realistic organizational escalation (S12). |

All budgets are calibrated from scenario walk-throughs and prior art analysis. They are deploy-time validation rules — adjustable via platform update without data migration or stored-event changes.

#### S14: Deployer-Parameterized Policies

Four policy areas where the platform defines the vocabulary and defaults; the deployer selects values.

**Flag severity** (Q9): Per-deployment override of platform defaults. The platform defines flag types (from ADR-2 structural conflicts and ADR-3 authorization flags) and two severity levels: blocking (event held from policy execution until resolved) and informational (event flows, flag visible in audit trail). Deployer overrides defaults per flag type. Per-activity severity is a growth path, not an initial commitment.

**Domain uniqueness constraints** (Q7): Shapes declare uniqueness rules — scope dimensions, time period, and violation action (warn or block). Example: "one `household_visit` per subject per activity per calendar week." Evaluated optimistically on-device against locally known events, authoritatively on the server at sync. Violations produce ADR-2 conflict flags with a `domain_uniqueness_violation` type, reusing the existing detection and resolution infrastructure. Domain-specific conflict _resolution_ strategies (auto-resolve based on role seniority, etc.) are deferred to ADR-5.

**Scope type composition** (Q10): Deployers compose assignments from three platform-fixed scope types (S7). This is L0 parameterization: selecting which scope dimensions apply to each role's assignments and what values they take.

**Sensitivity classification** (Q12): Shapes and/or activities are marked with a sensitivity level: `standard` (default), `elevated`, or `restricted`. Sensitivity affects sync scope filtering (actors need explicit sensitive-access role), device retention policy (sensitive data is purge-eligible on scope contraction per ADR-3 S10), and audit level. No new envelope fields — sensitivity is a property of the shape/activity configuration, evaluated server-side during sync scope computation.

Resolves Q7 (partially — Q7a resolved, Q7b deferred), Q8 (role-action tables as L0 activity parameters), Q9, Q10, Q12.

---

## What This Does NOT Decide

| Concern | Belongs to | Why deferred |
|---------|-----------|--------------|
| State machines / state progression model | ADR-5 | Whether state machines are a platform primitive or a projection pattern depends on workflow analysis not yet performed |
| `status_changed` as a 7th event type | ADR-5 | Contingent on whether state transitions require different platform processing from `capture`. Current vocabulary handles all ADR-4 scenarios |
| Domain conflict resolution automation (Q7b) | ADR-5 | Auto-resolution strategies require state machine integration |
| `context.*` expression scope (actor's operational context) | ADR-5 | Emerged from a stress test, depends on state-progression model. The expression evaluator's architecture doesn't change — `context.*` would be a pre-resolved scope addition |
| Breaking change migration mechanism | Implementation | The policy is decided (S10: deprecation-only default, breaking changes explicit). The migration tooling is implementation |
| Configuration authoring format | Implementation | Whether deployers write YAML, use a visual builder, or both is tooling. The semantic model (layers, shapes, triggers) is decided; the surface is not |
| Projection merge strategy across schema versions | Implementation | `shape_ref` ensures events are self-describing (S1). How the projection engine composes multi-version streams is server-side logic |
| Deploy-time validator UX | Implementation | The budgets are decided (S13). How violations are surfaced to deployers is tooling |
| Pattern inventory | ADR-5 | Patterns are platform-fixed and parameterized (Session 3 Part 1). Which patterns ship initially is an ADR-5 / implementation concern |

---

## Consequences

### What is now constrained

- **Event envelope**: Finalized at 11 fields across four ADRs. `shape_ref` (S1) and `activity_ref` (S2) are new. `type` (S3) gains a defined vocabulary. No further ADRs are expected to add envelope fields — the envelope's extensibility clause (ADR-1 S5) remains available but should be treated as exceptional.

- **ADR-5 (Workflow)**: Workflow operates within the type vocabulary (S3) — new types require justification by different processing behavior. State data is captured via shapes (S10). Workflow automation runs server-side (S5). Conflict resolution automation (Q7b) and the `context.*` expression scope are ADR-5's to resolve. The trigger architecture (S12) provides the reactive infrastructure; ADR-5 determines what workflow patterns compose on top of it.

- **Platform implementation**: Must deliver: a shape registry (versioned, snapshot-based), an expression evaluator (operators + field references, zero functions), a trigger engine (3a sync + 3b async, DAG-validated), a deploy-time validator (budget enforcement), and atomic configuration packaging for device sync.

- **Deployer experience**: The four-layer gradient (S9) is the deployer's mental model. Deployers work with activities and shapes in domain terms — structural event types (`capture`, `review`) are platform internals. The deployer tooling translates domain language ("when a CHV opens a case, they fill the intake form") into platform constructs (activity definition with shape reference and role-action mapping).

### Risks accepted

- **Configuration expertise shifts form (AP-3).** The gradient mitigates but does not eliminate the need for trained configurators at L1–L3. Mitigation: S00 requires zero L2/L3 configuration — the floor is genuinely low. The ceiling is where expertise matters.

- **Expression ceiling at ~60–70% of real needs.** Every prior art platform hits this. ADR-004 is honest: the L3→code boundary is explicit, not hidden. Deployments that exceed L3 know exactly where and why they need platform evolution. Revisit trigger: if >3 deployments in the first year require code changes for capabilities that "feel like they should be configurable."

- **Complexity budgets may need tuning.** All budgets (S13) are initial values calibrated from scenario walk-throughs and prior art. Real deployments may reveal that 60 fields is too generous or too restrictive, or that 50 triggers is insufficient for complex programs. Revisit trigger: deploy-time rejections that deployers consistently appeal as unreasonable.

- **Deprecation-only schema evolution accumulates cruft.** Shapes grow but never shrink. The 60-field budget bounds the accumulation. When a shape approaches its budget with mostly-deprecated fields, the deployer creates a new shape. Revisit trigger: >3 deployments hit the field budget due to deprecated-field accumulation rather than genuine field count.

- **Six event types may prove insufficient.** The append-only vocabulary commitment makes addition cheap. However, premature addition creates permanent parsing obligations across all components. Revisit trigger: ADR-5 analysis identifies a processing behavior that cannot be cleanly expressed by any existing type.

### Principles confirmed

- **P2 (Set up, not built)**: S00 — basic structured capture — requires exactly 2 configuration artifacts: one shape definition (`household_observation/v1`, ~8 fields) and one activity definition (`household_survey`, role-action mapping for CHV). Both are L0/L1 — no logic rules, no triggers, no code. A program manager with training can configure S00. V2 is validated.

- **P7 (Simplest scenario stays simple)**: S00's configuration footprint under ADR-004 is comparable to an ODK XLSForm: define fields, define who can fill them, deploy. The four-layer gradient, trigger architecture, and complexity budgets add zero overhead to the simple case — they exist only when needed.

- **P1 (Offline is the default)**: L2 expressions evaluate on-device using locally available data (`payload.*`, `entity.*`). Configuration reaches devices as an atomic package at sync (S6). No configuration operation requires connectivity at the point of field work.

- **P4 (Composition over invention)**: No new structural mechanisms. Shapes compose with the event store (ADR-1). Activities compose with assignments (ADR-3). Triggers compose with the accept-and-flag model (ADR-2). Domain uniqueness reuses conflict detection infrastructure. Sensitivity composes with sync scope filtering. The configuration layer wires existing primitives — it does not invent new ones.

---

## Traceability

| Sub-decision | Classification | Key forcing inputs |
|---|---|---|
| S1 (shape_ref) | Structural constraint | ADR-1 S5, Session 2 Q3, Session 3 Attack 2 |
| S2 (activity_ref) | Structural constraint | Session 2 Q11, Session 3 Attack 1 |
| S3 (type vocabulary) | Structural constraint | ADR-1 S2, Session 2 Q1, Session 3 Attack 3 |
| S4 (system actor convention) | Strategy-protecting | V3, Session 3 Part 1 Check (f) |
| S5 (triggers server-only) | Strategy-protecting | Session 3 Part 1 Check (c), device complexity |
| S6 (atomic config delivery) | Strategy-protecting | P1, Session 2 Q4 |
| S7 (no deployer scope logic) | Strategy-protecting | ADR-3 S1/S2, Session 3 Part 4 Q10 |
| S8 (no field-level sensitivity) | Strategy-protecting | ADR-1 S1, Session 3 Part 4 Q12 |
| S9 (four-layer gradient) | Initial strategy | T2, V2, prior art (CommCare layers, Salesforce governor limits, DHIS2 metadata) |
| S10 (shape definition & evolution) | Initial strategy | AP-4, Session 2 S06 walk-through, Session 3 Attack 2 |
| S11 (expression language) | Initial strategy | AP-1/AP-2, Session 3 Part 1 Check (d), Session 3 V2 |
| S12 (trigger architecture) | Initial strategy | AP-5, Session 2 S12 walk-through, Session 3 V3 |
| S13 (complexity budgets) | Initial strategy | AP-1/AP-3, Salesforce governor limits, Session 3 V1 |
| S14 (deployer policies) | Initial strategy | ADR-2 S12/S14, ADR-3 S7/S10, Session 3 Part 4 Q7/Q9/Q10/Q12 |

---

## Next Decision

**ADR-5: State Progression and Workflow.**

ADR-004 established that event types are platform-fixed with a 6-type vocabulary (growable), data shapes are typed and versioned, triggers are server-only with bounded depth, and the configuration gradient tops out at Layer 3. ADR-5 must now decide:

1. Whether state machines are a platform primitive (with platform-enforced transition rules) or a projection pattern (derived from event sequences by configuration)
2. Whether `status_changed` warrants a 7th structural event type, based on whether state transitions require different platform processing from `capture`
3. How multi-step, multi-actor workflow composition works within the offline-first, append-only model
4. How workflow state interacts with the accept-and-flag conflict model (cascade behavior when upstream events are flagged after downstream workflow steps have fired)
5. Whether the `context.*` expression scope (an actor's operational context, pre-resolved at form-open time) is needed for workflow-aware form logic
6. Whether domain-specific conflict resolution can be automated (Q7b), and how auto-resolution integrates with state machines

Inputs: ADR-001 (storage model), ADR-002 (identity and conflict model), ADR-003 (authorization), ADR-004 (this decision — configuration boundary, type vocabulary, trigger architecture), S08 "what makes this hard" (multi-step resolution across actors), S11 "what makes this hard" (multi-step approvals), P5 (Conflict is surfaced, not silently resolved).
