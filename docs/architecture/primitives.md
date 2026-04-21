# Structural View — Platform Primitives

> The 11 building blocks of the Datarun platform. Each has a distinct invariant that no other primitive shares. Organized by architectural layer.

---

## 1. Event Store

The foundational write primitive. All data enters the system as immutable events written here.

**Invariant**: All writes are append-only. Events are never modified or deleted. The Event Store is the sole write path for all state changes.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [1-S1] | All writes are append-only. Events never modified or deleted. | Structural |
| [1-S2] | Atomic write unit is a typed, immutable event. Current state is a projection computed from the event stream. Write-path discipline: all state changes through the Event Store. | Structural |
| [1-S3] | All identifiers (events, subjects, records) are client-generated UUIDs. | Structural |
| [1-S4] | Sync unit is the immutable event. Sync is idempotent, append-only, order-independent. | Structural |
| [1-S5] | Event envelope carries 11 fields. Extensible for downstream ADRs. | Structural |
| [E1] | Envelope is finalized at 11 fields. Five ADRs required zero use of the extensibility clause. | Emergent |

**Configuration surface**: None. The Event Store is platform infrastructure, not configurable.

**Escape hatch**: B→C — add application-maintained views alongside the event store if projection complexity exceeds thresholds on low-end devices. Viable only if the event log is gap-free, which is why write-path discipline [1-ST2] is documented as a strategy protecting this option.

**Key properties**:
- **Idempotent sync**: receiving the same event twice is a no-op
- **Order-independent arrival**: events carry their own ordering metadata (`device_seq`, `sync_watermark`)
- **Self-describing events**: every event contains its `shape_ref`, `type`, and full identification

---

## 2. Projection Engine

The foundational read primitive. Computes current state from event streams. Everything the user sees is a projection.

**Invariant**: Current state = f(events). Projections are always rebuildable from the event stream. If a projection diverges from events, the events win.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [1-S2] | Projections derived from events, never independently written. | Structural |
| [2-S6] | Alias resolution in projection: retired IDs map to surviving IDs for reads. | Structural |
| [3-S3] | Authority context is a projection, not an envelope field. Derived from assignment event timeline. | Structural |
| [5-S2] | Flagged events are visible in timeline but excluded from state derivation. | Strategy-protecting |
| [4-S11] | Projection rules are pure value-to-value lookup tables. | Initial strategy |
| [5-S4] | State machines are projection patterns. State derived from events + pattern definitions, never stored in events. | Initial strategy |
| [5-S8] | `context.*` scope: 7 platform-fixed properties pre-resolved on-device from local projection at form-open time. | Initial strategy |

**Capabilities** (accumulated across 5 ADRs):

| Capability | Source |
|-----------|--------|
| Per-subject state derivation | [1-S2] |
| Indexed cross-subject queries | Scenario walk-throughs (S04) |
| State-change timestamps | Scenario walk-throughs (S04) |
| Alias resolution | [2-S6] |
| Authority reconstruction | [3-S3] |
| Multi-version event handling (route on `shape_ref`) | [4-S1], [4-S10] |
| State machine evaluation | [5-S4] |
| Flagged event exclusion | [5-S2] |
| Source-chain traversal | [5-S7] |
| `context.*` pre-resolution | [5-S8] |

**Configuration surface**: L1 projection rules (value-to-value lookup tables), pattern-defined auto-maintained projections.

**Escape hatch**: Add materialized views if rebuild exceeds ~200ms/subject on low-end device.

---

## 3. Identity Resolver

Manages subject identity lifecycle: creation, merge, split, alias resolution, lineage tracking.

**Invariant**: 4 typed identity categories (subject, actor, assignment, process). Merge = alias in projection, never rewrite. Lineage graph is a DAG by construction.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [2-S1] | Causal ordering via `device_seq` + `sync_watermark`. `(device_id, device_seq)` is globally unique and never reused. | Structural |
| [2-S2] | All identity references carry a type discriminator and UUID: `{type, id}`. Types: subject, actor, process, assignment. | Structural |
| [2-S3] | `device_time` (mapped to `timestamp`) is advisory only. No ordering or correctness depends on it. | Structural |
| [2-S5] | `device_id` identifies a physical device, not a user. New device = new `device_id`. | Structural |
| [2-S6] | Merge = alias in projection. `SubjectsMerged` creates alias mapping: `retired_id → surviving_id`. Eager transitive closure. Single-hop lookup. | Structural |
| [2-S7] | No `SubjectsUnmerged`. Wrong merges corrected via `SubjectSplit`. | Structural |
| [2-S8] | Split freezes history. Source is permanently archived. Historical events remain attributed to source. | Structural |
| [2-S9] | Lineage graph acyclicity enforced by construction. Merge operands must be active. Archived is terminal. | Structural |
| [2-S10] | Merge and split are online-only. Server-validated preconditions. | Strategy-protecting |

**Identity type taxonomy**:

| Type | Lifecycle | Mergeable | Example |
|------|-----------|-----------|---------|
| Subject | Persistent | Yes | Person, facility, location |
| Actor | Persistent | No | Field worker, supervisor |
| Assignment | Temporal | No | "CHV covering Village X from Jan–Jun" |
| Process | Transient | No | Shipment, review cycle, campaign instance |

**Configuration surface**: None. The identity model is platform-fixed.

---

## 4. Conflict Detector

The anomaly detection pipeline. Evaluates incoming events against projected state and raises flags. Never rejects events.

**Invariant**: Accept-and-flag — events are never rejected for state staleness. Detect-before-act — flagged events do not trigger policies or advance state machines. Single-writer — each flag has exactly one designated resolver.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [2-S11] | Single-writer conflict resolution. Every `ConflictDetected` event designates exactly one resolver. | Strategy-protecting |
| [2-S12] | Conflict detection before policy execution. Flagged events do not trigger policies until resolved. | Strategy-protecting |
| [2-S13] | Conflict detection uses raw references (original `subject_id`). Alias resolution occurs only in projection, after detection. | Structural |
| [2-S14] | Events are never rejected for state staleness. Anomalies surfaced as `ConflictDetected` events. | Structural |
| [3-S7] | Detect-before-act extends to ALL flag types including authorization flags. Blocking vs. informational is deployment-configurable. | Strategy-protecting |
| [5-S1] | `transition_violation` flag category: incoming events evaluated against pattern-defined state machine rules. | Strategy-protecting |
| [5-S2] | Flagged events excluded from state machine evaluation. Extends detect-before-act to state derivation. | Strategy-protecting |
| [5-S3] | Flag resolvability classification: `auto_eligible` or `manual_only`. Platform-defined, not deployer-configurable. | Strategy-protecting |
| [5-S7] | Source-only flagging. Only root-cause event receives flag. Downstream contamination is a computed projection property via source-chain traversal. | Initial strategy |
| [E3] | Accept-and-flag is the universal anomaly handling mechanism across identity, authorization, state transitions, and domain rules. | Emergent |
| [E4] | Detect-before-act is the universal ordering guarantee: policy execution, state derivation, authorization. | Emergent |

**Configuration surface**: L0 — per-flag-type severity (blocking vs. informational). L1 — domain uniqueness constraints (shape-declared, server-authoritative). L3b — auto-resolution policies.

**Flag catalog**: 9 categories across 4 ADRs. Full catalog in [cross-cutting.md § Unified Flag Catalog](cross-cutting.md#7-unified-flag-catalog).

---

## 5. Scope Resolver

Determines what data goes to which device (sync scope) and whether an actor is authorized for a given action (access scope). These are the same computation in different contexts.

**Invariant**: Sync scope = access scope. A device receives exactly the data its actor is authorized to act on — no more, no less.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [3-S1] | Assignment-based access control. Every access rule reduces to scope-containment test: `actor.assignment.scope ⊇ subject.location`. | Structural |
| [3-S2] | Sync scope = access scope. Device receives exactly the data its actor is authorized for. Server computes sync payload from active assignments. | Structural |
| [3-S4] | Alias-respects-original-scope. Authorization evaluated against original `subject_ref` as written, not post-merge surviving subject's scope. | Structural |
| [3-S5] | Scope-containment invariant on `AssignmentCreated`. New assignment scope ⊆ creating actor's scope. Server-validated. | Strategy-protecting |
| [4-S7] | No deployer-authored scope logic. 3 platform-fixed scope types. AND composition for multi-dimension assignments. | Strategy-protecting |

**Scope types** (platform-fixed):

| Scope type | Containment test |
|-----------|-----------------|
| `geographic` | Subject's location within actor's area assignment |
| `subject_list` | Subject's ID in actor's explicit assignment list |
| `activity` | Event's `activity_ref` in actor's permitted activities |

Assignments may combine multiple scope dimensions. All non-null dimensions must pass (AND composition).

**Configuration surface**: L0 — scope type composition per role.

**Escape hatch**: Add `authority_context` to envelope if authority reconstruction exceeds ~50ms/event at scale [3-S3].

---

## 6. Shape Registry

Defines and manages typed payload schemas for events. The bridge between deployer-authored data structures and the platform's event model.

**Invariant**: Every event's payload conforms to a typed, versioned shape. All shape versions remain valid forever.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [4-S1] | Every event carries `shape_ref` (mandatory). Format: `{shape_name}/v{version}`. Names match `[a-z][a-z0-9_]*`. Version is monotonically increasing positive integer. | Structural |
| [4-S10] | Shapes: typed, versioned, authored as deltas, stored as snapshots. Deprecation-only default evolution. All versions remain valid forever. | Initial strategy |
| [4-S8] | No field-level sensitivity. Sensitivity at shape/activity level only: `standard`, `elevated`, `restricted`. | Strategy-protecting |

**Evolution rules**:
- **Additive** (new optional fields): always safe
- **Deprecation** (field hidden from new forms, retained in schema): default path
- **Breaking** (field removed or type changed): exceptional, requires explicit acknowledgment
- 60-field budget per shape bounds cruft accumulation [4-S13]

**Configuration surface**: L1 — field definitions (name, type, required/optional, validation), uniqueness constraints, projection rules.

**Escape hatch**: Revisit deprecation-only default if >3 deployments hit field budget from deprecated accumulation [4-S10].

---

## 7. Expression Evaluator

Evaluates conditions for form logic (L2) and trigger conditions (L3). One language, two contexts, zero functions.

**Invariant**: Operators + field references only. Zero functions. Reads pre-resolved values — not a query engine.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [4-S11] | One expression language with two contexts. Form context: `payload.*`, `entity.*`, `context.*`. Trigger context: `event.*`. | Initial strategy |
| [4-S13] | 3 predicates per condition maximum. | Initial strategy |
| [5-S8] | `context.*` properties (7 platform-fixed): `subject_state`, `subject_pattern`, `activity_stage`, `actor.role`, `actor.scope_name`, `days_since_last_event`, `event_count`. Read-only, pre-resolved at form-open time, static during fill. | Initial strategy |

**Configuration surface**: L2 — form logic (show/hide conditions, computed defaults, conditional warnings). L3 — trigger conditions.

**Escape hatch**: Revisit L3 expressiveness ceiling if >3 deployments need code for "should be configurable" [4-S11].

---

## 8. Trigger Engine

Server-side reactive processing. Watches for events or non-events and produces response events.

**Invariant**: Server-only. Non-recursive. DAG max path 2. A trigger's output type must differ from its input type.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [4-S4] | System actor identity: `system:{source_type}/{source_id}`. | Strategy-protecting |
| [4-S5] | All triggers execute server-only. Both L3a and L3b. | Strategy-protecting |
| [4-S12] | L3a event-reaction (sync, single-event condition) + L3b deadline-check (async, non-occurrence). Non-recursive. DAG max path 2. Output type ≠ input type. | Initial strategy |
| [5-S9] | Auto-resolution as L3b sub-type. Watches `auto_eligible` flags + resolution-enabling events within time window. System actor: `system:auto_resolution/{policy_id}`. | Initial strategy |

**Two mechanisms**:

| Mechanism | Model | When it fires | What it watches for |
|-----------|-------|---------------|---------------------|
| L3a event-reaction | Synchronous | During sync processing | Single-event condition on incoming event |
| L3b deadline-check | Asynchronous | Server scheduled | Non-occurrence: expected event not received within time window |
| L3b auto-resolution | Asynchronous | Server scheduled | Flag + resolution-enabling event on same subject within time window |

**Loop prevention** (3 independent guards):
1. Detect-before-act [2-S12]: trigger outputs go through normal pipeline; if flagged, they do not trigger further policies
2. DAG max path 2 [4-S12]: at most 2 levels of chaining
3. Input/output type separation [4-S12]: output type must differ from input type

**Limits**: 5 triggers per event type, 50 total per deployment [4-S13]. Counts include auto-resolution policies [5-S9].

**Configuration surface**: L3a — event-reaction triggers. L3b — deadline-check triggers + auto-resolution policies.

---

## 9. Deploy-time Validator

The configuration acceptance gate. Enforces hard complexity budgets before any configuration reaches a device.

**Invariant**: Hard limits enforced at deploy time, not advisory guidelines. The platform rejects configuration packages that exceed any budget.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [4-S13] | Complexity budgets: hard, deploy-time enforced. | Initial strategy |
| [5-S6] | 5 pattern composition rules enforced at deploy time. | Initial strategy |

**Budget table**:

| Dimension | Limit | Source |
|-----------|-------|--------|
| Fields per shape | 60 | [4-S13] |
| Predicates per condition | 3 | [4-S13] |
| Triggers per event type | 5 | [4-S13] |
| Triggers per deployment (L3a + L3b + auto-resolution) | 50 | [4-S13] |
| Escalation depth | 2 levels | [4-S13] |

**ADR-005 composition checks** (5 additional rules):
1. Shape-to-pattern mapping unique within activity [5-S6 Rule 5]
2. One subject-level pattern per activity [5-S6 Rule 1]
3. Pattern role mapping completeness [5-S6]
4. Auto-resolution targets only `auto_eligible` flag types [5-S3]
5. Auto-resolution counts within L3b budget [5-S9]

**Configuration surface**: None. The validator IS the configuration boundary.

---

## 10. Config Packager

The configuration delivery pipeline endpoint. Gets validated configuration from server to device atomically.

**Invariant**: Atomic delivery. At most 2 configuration versions coexist on device (current + previous for in-progress work). A device never operates with a shape from version N and a trigger from version N+1.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [4-S6] | Atomic configuration delivery. At most 2 config versions coexist on-device. | Strategy-protecting |

**Contents of a configuration package**:
- Shape definitions (all versions, for multi-version projection support)
- Activity definitions (role-action mappings, pattern selections, scope compositions)
- Trigger definitions (L3a, L3b, auto-resolution)
- Pattern definitions (state machine skeletons + parameterization)
- Expression rules (L2 show/hide, computed defaults)
- Flag severity overrides (blocking vs. informational per flag type)
- Sensitivity classifications (per shape/activity)

**Configuration surface**: None. The packager delivers configuration, it is not configurable.

---

## 11. Pattern Registry

Platform-fixed workflow definitions that deployers select and parameterize. The bridge between "what workflow patterns exist" and "how this deployment uses them."

**Invariant**: Patterns are platform-fixed, closed vocabulary. Adding a pattern is a platform evolution, not a deployer action. Deployers select and parameterize at L0.

**Constraints**:

| ID | Constraint | Classification |
|----|-----------|----------------|
| [5-S4] | State machines are projection patterns. Platform does not enforce transitions — it flags violations. | Initial strategy |
| [5-S5] | Pattern Registry: platform-fixed skeletons, deployer-selected at L0. Closed vocabulary. Each pattern provides: participant roles, structural event types, state machine skeleton, auto-maintained projections, parameterization points. | Initial strategy |
| [5-S6] | 5 composition rules. Deploy-time enforced. | Initial strategy |
| [E2] | Type vocabulary is exactly 6 types. `status_changed` evaluated and rejected. | Emergent |

**Composition rules**:

| Rule | What it prevents |
|------|-----------------|
| One subject-level pattern per activity | Conflicting lifecycle state machines on same subject |
| Event-level patterns compose freely | Review patterns track per-event states (disjoint state space) |
| Approval sub-flows embed within subject patterns | Approval scoped to specific submission events |
| Cross-activity linking uses `activity_ref` | Patterns never span activities |
| Shape-to-pattern mapping unique within activity | No overlapping authority over same event (AP-6) |

**Configuration surface**: L0 — pattern selection, shape-to-role mapping, deadline values, approval level count.

**Escape hatch**: Pattern inventory insufficient if >2 deployments request custom state machines matching no existing pattern [5-S5].

**Pattern specifications**: 4 patterns formally specified with full state machines, transitions, roles, projections, and parameterization points. See [patterns.md](patterns.md) for the concluded specifications. Exploration walk-throughs: [exploration/28](../exploration/28-pattern-inventory-walkthrough.md).

---

## 12. Command Validator (Advisory Component)

On-device advisory component that warns users about potentially invalid actions. **Not a primitive** — it enforces no invariant of its own.

**Role**: UX improvement. Reduces unnecessary flags by warning users before they submit invalid transitions.

**Depends on**: Projection Engine (current workflow state) and Pattern Registry (valid transitions).

**Why not a primitive**: Its work is always redundant to the Conflict Detector + Projection Engine. Without it, the system works identically — just with more flags to resolve.

---

## 13. Primitive Binding Summary

How many decided positions bind each primitive. Tests whether any primitive is under-determined or over-concentrated.

| Primitive | Structural | Strategy-protecting | Initial strategy | Emergent | Total |
|-----------|:----------:|:-------------------:|:----------------:|:--------:|:-----:|
| Event Store | 10 | 0 | 2 | 2 | 14 |
| Projection Engine | 3 | 2 | 5 | 3 | 13 |
| Identity Resolver | 8 | 1 | 0 | 0 | 9 |
| Conflict Detector | 3 | 5 | 5 | 3 | 16 |
| Scope Resolver | 4 | 3 | 3 | 0 | 10 |
| Shape Registry | 1 | 1 | 4 | 0 | 6 |
| Expression Evaluator | 0 | 0 | 4 | 0 | 4 |
| Trigger Engine | 1 | 2 | 3 | 2 | 8 |
| Deploy-time Validator | 0 | 1 | 4 | 0 | 5 |
| Config Packager | 0 | 1 | 2 | 0 | 3 |
| Pattern Registry | 0 | 1 | 3 | 0 | 4 |

**Observations**:
- **Conflict Detector (16)** has the highest binding count — touched by every ADR. Reflects its cross-cutting role, not a decomposition signal.
- **Identity Resolver (9 SC+SP, 0 IS)** is fully locked. No evolvable surface. Correct — identity is the most permanent thing in the system.
- **Config Packager (3) and Expression Evaluator (4)** are the lightest. Both are implementation-heavy but architecture-light — the architecture says WHAT they deliver/evaluate; nearly everything about HOW is open.
