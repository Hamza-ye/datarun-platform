# 22 — Platform Primitives Inventory

> Pre-specification consolidation pass. Every platform primitive identified across 5 ADRs, 21 exploration sessions, and 2 experiment walk-throughs — reconciled, cataloged, and organized for specification structure decisions.

---

## 1. Purpose

The platform specification document must consolidate 52 sub-decisions across 5 ADRs into a single implementation-grade reference organized by **what gets built**, not by the decision history that shaped it. Before writing that specification, this document:

1. **Catalogs every platform primitive** — a component with a distinct invariant, clear inputs/outputs, and a named place in the architecture
2. **Reconciles the early walkthrough** (`experiments/002-walk-through-scenarios-primitives.md`) with the mature shapes that emerged through ADR-3, 4, and 5
3. **Maps cross-cutting concerns** that span multiple primitives
4. **Proposes a specification structure** — single file or split, and how to group

This document is evaluable on its own. Reading it should give a complete picture of what the platform's building blocks are without re-reading the 5 ADRs.

---

## 2. Sources

| Source | What it contributes |
|--------|-------------------|
| ADR-001 (Offline Data Model) | Event Store, Projection Engine, event envelope foundation (4 fields), escape hatch B→C |
| ADR-002 (Identity & Conflict) | Identity Resolver, Conflict Detector, causal ordering, accept-and-flag, envelope +5 fields |
| ADR-003 (Authorization & Sync) | Scope Resolver, assignment-based access, sync=scope, authority-as-projection, envelope +0 fields |
| ADR-004 (Configuration Boundary) | Shape Registry, Expression Evaluator, Trigger Engine, Deploy-time Validator, Config Packager, four-layer gradient, envelope +2 fields |
| ADR-005 (State Progression) | Pattern Registry, Command Validator (advisory), source-only flagging, auto-resolution, context.* scope, envelope +0 fields |
| Walkthrough (002) | Early primitive candidates (8 total, 2 settled, 6 open/candidate). Written pre-ADR-2. |
| All 21 explorations | Reasoning trails, stress tests, coherence audits — the "why" behind each primitive's shape |

---

## 3. Walkthrough Reconciliation

The walkthrough (`experiments/002-walk-through-scenarios-primitives.md`) was written before ADR-2. It event-stormed S04 and S08 against the ADR-1 primitives and discovered 6 new candidates. Here is what happened to each:

| Walkthrough Primitive | Walkthrough Status | Current Status | What Changed |
|---|---|---|---|
| Event Store | Settled | **Settled — expanded** | Envelope grew from 4 to 11 fields across 5 ADRs. Core invariants unchanged. |
| Projection Engine | Settled (core) / Open (capabilities) | **Settled — major capability expansion** | Gained: alias resolution (ADR-2), authority reconstruction (ADR-3), multi-version handling (ADR-4), state machine evaluation (ADR-5), flag exclusion (ADR-5), source-chain traversal (ADR-5), context.* pre-resolution (ADR-5). |
| Subject Identity | Open (ADR-2) | **→ Identity Resolver (settled)** | Renamed. Expanded from "canonical ID" to 4 typed categories, alias-based merge, DAG acyclicity, lineage graph. ADR-2 S1–S9. |
| Conflict Detector | Candidate (ADR-2) | **Settled — much richer** | Accept-and-flag, detect-before-act, single-writer resolution, 6 flag categories, resolvability classification. ADR-2 S11–S14, ADR-3 S7, ADR-5 S1–S3. |
| Command Validator | Candidate ("gatekeeper") | **Settled — fundamentally reframed** | The walkthrough imagined a gatekeeper that blocks writes. ADR-5 S4 decided: advisory only, never blocking. Events are always written (ADR-2 S14). The validator warns; the Conflict Detector flags. Not a write-path gate — a UX component. |
| Assignment Resolver | Candidate ("who reviews this") | **Split and subsumed** | The "who has access" concern → Scope Resolver (ADR-3). The "who is responsible" concern → Pattern Registry participant roles (ADR-5 S5). No standalone Assignment Resolver exists. |
| Scope Resolver | Open (ADR-3) | **Settled** | Sync scope = access scope. 3 platform-fixed scope types. Server-computed. ADR-3 S1–S2, ADR-4 S7. |
| Shape Registry | Open (ADR-4) | **Settled** | Typed, versioned, deprecation-only evolution, 60-field budget. ADR-4 S1, S10. |
| *(not in walkthrough)* Expression Evaluator | — | **New (ADR-4)** | Operators + field references, zero functions, two contexts (form, trigger). ADR-4 S11, ADR-5 S8. |
| *(not in walkthrough)* Trigger Engine | — | **New (ADR-4/5)** | Server-only, non-recursive DAG max path 2. L3a event-reaction + L3b deadline-check. Auto-resolution as L3b sub-type. ADR-4 S5/S12, ADR-5 S9. |
| *(not in walkthrough)* Deploy-time Validator | — | **New (ADR-4/5)** | Hard complexity budgets, deploy-time enforcement. ADR-4 S13, ADR-5 S6. |
| *(not in walkthrough)* Config Packager | — | **New (ADR-4)** | Atomic configuration delivery, at most 2 versions on-device. ADR-4 S6. |
| *(not in walkthrough)* Pattern Registry | — | **New (ADR-5)** | Platform-fixed workflow skeletons, deployer-selected at L0. 5 composition rules. ADR-5 S5/S6. |

**Summary**: The walkthrough correctly identified the foundational pair (Event Store, Projection Engine) and spotted two real candidates (Conflict Detector, Scope Resolver). It mis-characterized the Command Validator as a gatekeeper (ADR-5 corrected this), and the Assignment Resolver was split across two other primitives. Five entirely new primitives emerged from ADR-4 and ADR-5 that the walkthrough couldn't have seen.

The walkthrough remains useful as a scenario exercise (the S04 and S08 traces are still valid), but its primitives map is superseded by this inventory.

---

## 4. Primitive Inventory

Eleven platform primitives. Each has a distinct invariant that no other primitive shares. Organized by architectural layer.

### Layer 1: Storage & Projection

#### 4.1. Event Store

The foundational write primitive. All data enters the system as immutable events written here.

| Aspect | Detail |
|--------|--------|
| **Invariant** | All writes append-only. Events never modified or deleted. Sole write path for all state changes. |
| **Structural constraints** | ADR-1 S1 (append-only), S2 (typed immutable events), S3 (client-generated UUIDs), S4 (sync unit = event), S5 (envelope) |
| **Event envelope** | 11 fields: `id`, `type`, `shape_ref`, `[activity_ref]`, `subject_ref`, `actor_ref`, `device_id`, `device_seq`, `sync_watermark`, `timestamp`, `payload` |
| **Inputs** | Events from devices (via sync), events from Trigger Engine (server-side) |
| **Outputs** | Immutable events to Projection Engine, Conflict Detector, sync protocol |
| **Configuration surface** | None — the Event Store is platform infrastructure, not configurable |
| **Escape hatch** | B→C (ADR-1): add application-maintained views alongside the event store if projection complexity exceeds thresholds |

**Key design properties:**
- Idempotent sync (receiving the same event twice is a no-op)
- Order-independent arrival (events carry their own ordering metadata)
- Self-describing events (every event contains its shape_ref, type, and full identification)

#### 4.2. Projection Engine

The foundational read primitive. Computes current state from event streams. Everything the user sees is a projection.

| Aspect | Detail |
|--------|--------|
| **Invariant** | Current state = f(events). Projections are always rebuildable from the event stream. If a projection diverges from events, the events win. |
| **Structural constraints** | ADR-1 S2 (projections derived, never independently written), ADR-2 S6 (alias resolution in projection), ADR-3 S3 (authority-as-projection), ADR-5 S2 (flagged events excluded from state derivation) |
| **Inputs** | Events from Event Store, pattern definitions from Pattern Registry, alias table from Identity Resolver, assignment timeline for authority reconstruction |
| **Outputs** | Per-subject current state, per-event review status, workflow state, indexed cross-subject views, context.* property values, source-chain traversal results |
| **Configuration surface** | L1 projection rules (value-to-value lookup tables), pattern-defined auto-maintained projections |
| **Escape hatch** | B→C (ADR-1): add materialized views. Trigger: rebuild >200ms/subject on low-end device. |

**Capabilities accumulated across ADRs:**

| Capability | Source | Description |
|-----|--------|-------------|
| Per-subject state derivation | ADR-1 | Core projection: subject_id → current state |
| Indexed cross-subject queries | Walkthrough (S04) | "All subjects where status=X and reviewer=Y" |
| State-change timestamps | Walkthrough (S04) | Track *when* each status began, not just current status |
| Alias resolution | ADR-2 S6 | Resolve retired-ID references to surviving ID for reads |
| Authority reconstruction | ADR-3 S3 | Derive authority context from assignment event timeline |
| Multi-version event handling | ADR-4 S1/S10 | Route projection logic based on shape_ref |
| State machine evaluation | ADR-5 S4 | Derive workflow state from events + pattern definitions |
| Flagged event exclusion | ADR-5 S2 | Flagged events visible in timeline but excluded from state derivation |
| Source-chain traversal | ADR-5 S7 | Walk source_event_ref chains to surface upstream flags |
| context.* pre-resolution | ADR-5 S8 | Pre-resolve 7 properties at form-open time from local projection |

The Projection Engine has the largest capability surface of any primitive, spanning all 5 ADRs. Its core invariant ("state = f(events)") is simple; its capability list is what makes specification work complex.

---

### Layer 2: Identity & Integrity

#### 4.3. Identity Resolver

Manages subject identity lifecycle: creation, merge, split, alias resolution, lineage tracking.

| Aspect | Detail |
|--------|--------|
| **Invariant** | 4 typed identity categories (subject, actor, assignment, process). Merge = alias in projection, never rewrite. Lineage graph is a DAG by construction. |
| **Structural constraints** | ADR-2 S1 (causal ordering: device_seq + sync_watermark), S2 (typed identity refs), S5 (device_id hardware-bound), S6 (merge = alias), S7 (no unmerge), S8 (split freezes history), S9 (DAG acyclicity) |
| **Strategy-protecting** | ADR-2 S10 (merge/split online-only) |
| **Inputs** | SubjectsMerged, SubjectSplit events (server-validated) |
| **Outputs** | Alias table (eager transitive closure, single-hop lookup) to Projection Engine. Lineage graph for audit. |
| **Configuration surface** | None — identity model is platform-fixed |
| **Escape hatches** | Actor-sequence counter for cross-device ordering (ADR-2): trigger >10% of conflict flags from same-actor cross-device ordering |

**Identity type taxonomy:**

| Type | Lifecycle | Mergeable | Example |
|------|-----------|-----------|---------|
| Subject | Persistent | Yes | Person, facility, location |
| Actor | Persistent | No | Field worker, supervisor |
| Assignment | Temporal | No | "CHV covering Village X from Jan–Jun" |
| Process | Transient | No | Shipment, review cycle, campaign instance |

#### 4.4. Conflict Detector

The anomaly detection pipeline. Evaluates incoming events against projected state and raises flags. Never rejects events.

| Aspect | Detail |
|--------|--------|
| **Invariant** | Accept-and-flag: events are never rejected for state staleness. Detect-before-act: flagged events don't trigger policies or advance state machines. Single-writer: each flag has exactly one designated resolver. |
| **Structural constraints** | ADR-2 S12 (detect-before-act), S14 (never reject for staleness), S11 (single-writer resolution), S13 (raw references for detection) |
| **Strategy-protecting** | ADR-3 S7 (detect-before-act extends to auth flags), ADR-5 S1 (transition_violation flag category), S2 (flagged events excluded from state derivation), S3 (flag resolvability classification) |
| **Inputs** | Incoming events at sync, projection state, pattern definitions (for transition validation), assignment timeline (for auth validation) |
| **Outputs** | ConflictDetected events (with designated resolver), ConflictResolved events |
| **Configuration surface** | L0: per-flag-type severity (blocking vs. informational). L1: domain uniqueness constraints (shape-declared). L3b: auto-resolution policies. |
| **Escape hatches** | Auto-resolution for state-change flags (ADR-2): trigger >3 flags generated/resolved ratio for 2+ weeks |

**Flag categories:**

| Category | Source | Resolvability | Typical cause |
|----------|--------|---------------|---------------|
| `identity_conflict` | ADR-2 | manual_only | Potential duplicate subjects |
| `stale_reference` | ADR-2 | auto_eligible | Entity updated after event creation |
| `concurrent_state_change` | ADR-2 | manual_only | Two actors changed the same subject concurrently |
| `scope_violation` | ADR-3 | manual_only | Potential unauthorized access |
| `domain_uniqueness_violation` | ADR-4 | manual_only | Business rule violation |
| `transition_violation` | ADR-5 | auto_eligible | Invalid state transition (usually offline timing overlap) |

---

### Layer 3: Authorization & Sync

#### 4.5. Scope Resolver

Determines what data goes to which device (sync scope) and whether an actor is authorized for a given action (access scope). These are the same computation applied in different contexts.

| Aspect | Detail |
|--------|--------|
| **Invariant** | Sync scope = access scope. A device receives exactly the data its actor is authorized to act on — no more, no less. |
| **Structural constraints** | ADR-3 S1 (assignment-based access: scope-containment test), S2 (sync = access), S4 (alias-respects-original-scope) |
| **Strategy-protecting** | ADR-3 S5 (scope-containment on assignment creation — server-validated), ADR-4 S7 (no deployer-authored scope logic, 3 platform-fixed scope types) |
| **Inputs** | Assignment events (active assignments per actor), scope definitions (geographic hierarchy, subject lists, activities), subject locations |
| **Outputs** | Per-actor sync payload (to devices), authorization decisions (allow/deny per action) |
| **Configuration surface** | L0: scope type composition per role (which dimensions apply). Geographic hierarchy, subject lists, and activity assignments are deployment data. |
| **Escape hatches** | Add authority_context to envelope (ADR-3): trigger >50ms/event for authority reconstruction at scale |

**Scope types (platform-fixed):**

| Scope type | Containment test |
|-----------|-----------------|
| `geographic` | Subject's location within actor's area assignment |
| `subject_list` | Subject's ID in actor's explicit assignment list |
| `activity` | Event's activity_ref in actor's permitted activities |

Assignments may combine multiple scope dimensions. All non-null dimensions must pass (AND composition).

**Subsumes the early walkthrough's "Assignment Resolver"** for access concerns. The "who is responsible" question (workflow routing) is handled by Pattern Registry participant roles (§4.10).

---

### Layer 4: Configuration

#### 4.6. Shape Registry

Defines and manages typed payload schemas for events. The bridge between deployer-authored data structures and the platform's event model.

| Aspect | Detail |
|--------|--------|
| **Invariant** | Every event's payload conforms to a typed, versioned shape. All shape versions remain valid forever. |
| **Structural constraints** | ADR-4 S1 (shape_ref mandatory in envelope, format `{shape_name}/v{version}`) |
| **Inputs** | Shape definitions from deployer (authored as deltas, stored as full snapshots) |
| **Outputs** | Shape schemas to device (via Config Packager), validation rules to Deploy-time Validator, field metadata to Expression Evaluator |
| **Configuration surface** | L1: field definitions (name, type, required/optional, validation). Uniqueness constraints. Projection rules. |
| **Escape hatches** | Revisit deprecation-only evolution (ADR-4): trigger >3 deployments hit field budget from deprecated accumulation |

**Evolution rules:**
- Additive (new optional fields): always safe
- Deprecation (field hidden from new forms, retained in schema): default path
- Breaking (field removed or type changed): exceptional, requires explicit acknowledgment
- 60-field budget per shape bounds cruft accumulation

#### 4.7. Expression Evaluator

Evaluates conditions for form logic (L2) and trigger conditions (L3). One language, two contexts, zero functions.

| Aspect | Detail |
|--------|--------|
| **Invariant** | Operators + field references only. Zero functions. Reads pre-resolved values — not a query engine. |
| **Structural constraints** | None — the language specification is an initial strategy (ADR-4 S11) |
| **Inputs** | Field values from payload, entity projection, context.* properties (form context) or event envelope/payload (trigger context) |
| **Outputs** | Boolean condition results (for show/hide, trigger firing) or computed values (for defaults) |
| **Configuration surface** | L2: form logic (show/hide conditions, computed defaults, conditional warnings). L3: trigger conditions. |
| **Escape hatches** | Revisit L3 expressiveness ceiling (ADR-4): trigger >3 deployments need code for "should be configurable" |

**Reference scopes by context:**

| Context | Available scopes | Where it runs |
|---------|-----------------|---------------|
| Form (L2) | `payload.*`, `entity.*`, `context.*` | On-device |
| Trigger (L3) | `event.*` | Server-only |

**context.* properties (ADR-5 S8) — 7 platform-fixed, read-only:**
`subject_state`, `subject_pattern`, `activity_stage`, `actor.role`, `actor.scope_name`, `days_since_last_event`, `event_count`

**Constraints:** 3 predicates per condition max (ADR-4 S13).

#### 4.8. Trigger Engine

Server-side reactive processing. Watches for events or non-events and produces response events.

| Aspect | Detail |
|--------|--------|
| **Invariant** | Server-only. Non-recursive. DAG max path 2. A trigger's output type must differ from its input type. |
| **Strategy-protecting** | ADR-4 S5 (server-only execution) |
| **Inputs** | Incoming events (L3a), time-based scans (L3b), flag state (auto-resolution sub-type) |
| **Outputs** | Exactly one output event per trigger firing, written to Event Store with system actor identity |
| **Configuration surface** | L3a: event-reaction triggers (synchronous, fires during sync). L3b: deadline-check triggers + auto-resolution policies (asynchronous). |
| **Escape hatches** | None specific — trigger architecture is evolvable strategy |

**Two mechanisms:**

| Mechanism | Model | When it fires | What it watches for |
|-----------|-------|---------------|---------------------|
| L3a event-reaction | Synchronous | During sync processing | Single-event condition on incoming event |
| L3b deadline-check | Asynchronous | Server scheduled | Non-occurrence: expected event not received within time window |
| L3b auto-resolution | Asynchronous | Server scheduled | Flag + resolution-enabling event on same subject within time window (ADR-5 S9) |

**Limits:** 5 triggers per event type, 50 total per deployment, depth-2 DAG max.

**System actor identity:** `system:{source_type}/{source_id}` (ADR-4 S4). Source types: `trigger`, `auto_resolution`.

**Loop prevention (3 independent guards):**
1. Detect-before-act (ADR-2 S12): trigger outputs go through normal pipeline; if flagged, they don't trigger further policies
2. DAG max path 2 (ADR-4 S12): at most 2 levels of chaining
3. Input/output type separation: output type must differ from input type

#### 4.9. Deploy-time Validator

The configuration acceptance gate. Enforces hard complexity budgets before any configuration reaches a device.

| Aspect | Detail |
|--------|--------|
| **Invariant** | Hard limits enforced at deploy time, not advisory guidelines. The platform rejects configuration packages that exceed any budget. |
| **Inputs** | Complete configuration package (shapes, activities, triggers, patterns, role-action tables) |
| **Outputs** | Accept or reject with specific violations listed |
| **Configuration surface** | None — the validator IS the configuration boundary |

**Budget table:**

| Dimension | Limit | Source |
|-----------|-------|--------|
| Fields per shape | 60 | ADR-4 S13 |
| Predicates per condition | 3 | ADR-4 S13 |
| Triggers per event type | 5 | ADR-4 S13 |
| Triggers per deployment (L3a + L3b + auto-resolution) | 50 | ADR-4 S13 |
| Escalation depth | 2 levels | ADR-4 S13 |

**ADR-5 additions (5 new checks):**
- Shape-to-pattern mapping is unique within an activity (S6 Rule 5)
- One subject-level pattern per activity (S6 Rule 1)
- Pattern role mapping completeness
- Auto-resolution targets only `auto_eligible` flag types (S3)
- Auto-resolution counts within L3b budget (S9)

#### 4.10. Config Packager

The configuration delivery pipeline. Gets configuration from server to device atomically.

| Aspect | Detail |
|--------|--------|
| **Invariant** | Atomic delivery. At most 2 configuration versions coexist on device (current + previous for in-progress work). A device never operates with a shape from version N and a trigger from version N+1. |
| **Strategy-protecting** | ADR-4 S6 |
| **Inputs** | Validated configuration package (passes Deploy-time Validator) |
| **Outputs** | Atomic configuration payload delivered to device at sync |
| **Configuration surface** | None — the packager delivers configuration, it is not configurable |

**Contents of a configuration package:**
- Shape definitions (all versions, for multi-version projection support)
- Activity definitions (role-action mappings, pattern selections, scope compositions)
- Trigger definitions (L3a, L3b, auto-resolution)
- Pattern definitions (state machine skeletons + parameterization)
- Expression rules (L2 show/hide, computed defaults)
- Flag severity overrides (blocking vs. informational per flag type)
- Sensitivity classifications (per shape/activity)

---

### Layer 5: Workflow

#### 4.11. Pattern Registry

Platform-fixed workflow definitions that deployers select and parameterize. The bridge between "what workflow patterns exist" and "how this deployment uses them."

| Aspect | Detail |
|--------|--------|
| **Invariant** | Patterns are platform-fixed, closed vocabulary. Adding a pattern is a platform evolution, not a deployer action. Deployers select and parameterize at L0. |
| **Structural constraints** | None — Pattern Registry is an initial strategy (ADR-5 S5) |
| **Inputs** | Pattern definitions (platform-authored), deployer parameterization (which shapes, which roles, what deadlines) |
| **Outputs** | State machine definitions to Projection Engine, transition rules to Conflict Detector, participant role definitions for workflow routing |
| **Configuration surface** | L0: pattern selection, shape-to-role mapping, deadline values, approval level count |
| **Escape hatches** | Pattern inventory insufficient (ADR-5): trigger >2 deployments request custom state machines matching no pattern |

**What each pattern provides:**

| Component | Description |
|-----------|-------------|
| Participant roles | Which roles interact (capturer, reviewer, approver, sender, receiver) |
| Structural event types | Which of the 6 platform types participate |
| State machine skeleton | Named states, valid transitions, which shapes are state-changing vs. state-preserving |
| Auto-maintained projections | What the Projection Engine computes automatically (current_state, pending_since, time_in_state) |
| Parameterization points | What the deployer fills in |

**Composition rules (5, enforced at deploy-time):**

| Rule | What it prevents |
|------|-----------------|
| One subject-level pattern per activity | Conflicting lifecycle state machines on same subject |
| Event-level patterns compose freely | Review patterns track per-event states (disjoint state space) |
| Approval sub-flows embed within subject patterns | Approval is scoped to specific submission events |
| Cross-activity linking uses activity_ref | Patterns never span activities |
| Shape-to-pattern mapping unique within activity | No overlapping authority over same event (AP-6) |

**Existence-proof patterns (validated, not committed):**
1. `capture_with_review` — Submit → review → accept/return → resubmit (S04)
2. `case_management` — Open → interact → refer/transfer → resolve → close → reopen (S08)
3. `multi_step_approval` — Submit → level-1 → level-N → decision (S11)
4. `transfer_with_acknowledgment` — Dispatch → receive → discrepancy → resolve (S07/S14)

---

### Advisory Component (not a primitive)

#### Command Validator

On-device advisory component that warns users about potentially invalid actions. **Not a write-path gate** — the walkthrough's "gatekeeper" concept was rejected by ADR-5 S4.

| Aspect | Detail |
|--------|--------|
| **Role** | UX improvement: reduces unnecessary flags by warning users before they submit invalid transitions |
| **Inputs** | Proposed action, subject's projected state (from Projection Engine), pattern definitions (from Pattern Registry) |
| **Outputs** | Warnings (e.g., "this case is resolved — are you sure?"). User can override. |
| **Why not a primitive** | Enforces no invariant of its own. Its work is always redundant to the Conflict Detector + Projection Engine. Without it, the system still works correctly (just with more flags). |

---

## 5. Cross-Cutting Concerns

Five concerns span multiple primitives. The specification must describe these as end-to-end flows, not just per-primitive capabilities.

### 5.1. Event Envelope — The Universal Contract

The 11-field envelope is the interface between every primitive. Every component reads it; only the Event Store writes it.

| Field | Type | Source ADR | Presence | Primary consumers |
|-------|------|-----------|----------|-------------------|
| `id` | UUID | ADR-1 | Mandatory | All (global unique reference) |
| `type` | enum(6) | ADR-1/4 | Mandatory | Trigger Engine (processing behavior), Conflict Detector |
| `shape_ref` | string | ADR-4 | Mandatory | Shape Registry (validation), Projection Engine (multi-version routing) |
| `activity_ref` | string | ADR-4 | Optional | Scope Resolver (activity-based filtering), Pattern Registry (pattern binding) |
| `subject_ref` | {type, id} | ADR-2 | Mandatory | Projection Engine (subject grouping), Identity Resolver (alias), Scope Resolver (authorization) |
| `actor_ref` | {type, id} | ADR-2 | Mandatory | Scope Resolver (authorization), audit trail |
| `device_id` | UUID | ADR-2 | Mandatory | Identity Resolver (device provenance), sync protocol |
| `device_seq` | integer | ADR-2 | Mandatory | Causal ordering (intra-device), conflict detection |
| `sync_watermark` | version | ADR-2 | Mandatory | Causal ordering (cross-device concurrency), auto-resolution |
| `timestamp` | datetime | ADR-1 | Mandatory | Advisory (display/audit only — ADR-2 S3) |
| `payload` | object | ADR-1 | Mandatory | Shape-specific data (validated against shape_ref) |

### 5.2. Accept-and-Flag Lifecycle

The full journey from event creation through anomaly detection to resolution. Touches: Event Store → Conflict Detector → Projection Engine → Trigger Engine (auto-resolution) → Event Store (resolution event).

```
Device creates event
    ↓
Event syncs to server
    ↓
Conflict Detector evaluates (BEFORE any policy execution)
    ├── No anomaly → event flows to Projection Engine and Trigger Engine normally
    └── Anomaly detected → ConflictDetected event created
         ↓
         Flag has designated resolver (ADR-2 S11)
         Flagged event visible in timeline but excluded from state derivation (ADR-5 S2)
         Flagged event does not trigger policies (ADR-2 S12)
         ↓
         ├── auto_eligible flag type:
         │    └── Auto-resolution policy watches for enabling event within time window (ADR-5 S9)
         │         ├── Enabling event arrives → ConflictResolved (system:auto_resolution/{id})
         │         └── Deadline expires → escalate or mark for manual review
         │
         └── manual_only flag type:
              └── Designated resolver reviews and creates ConflictResolved event (online-only, ADR-3 S6)
                   ↓
                   ├── Accepted → projection re-derives state including the event
                   └── Rejected → projection remains unchanged
```

### 5.3. Detect-Before-Act Pipeline

The ordering guarantee that prevents downstream cascade from uncertain data. Enforced at three levels:

| Level | What it prevents | Source |
|-------|-----------------|--------|
| Policy execution | Flagged events don't trigger L3a/L3b triggers | ADR-2 S12 |
| State derivation | Flagged events don't advance state machines | ADR-5 S2 |
| Authorization flags | Auth-flagged events don't trigger downstream work; blocking vs. informational configurable | ADR-3 S7 |

### 5.4. Four-Layer Configuration Gradient

How deployer-authored configuration maps to platform primitives:

| Layer | Name | Side effects | Primitives involved |
|-------|------|-------------|---------------------|
| L0 | Assembly | None — structural wiring | Pattern Registry (pattern selection), Scope Resolver (scope composition), Config Packager (flag severity, sensitivity) |
| L1 | Shape | None — schema and data mapping | Shape Registry (field definitions), Projection Engine (projection rules) |
| L2 | Logic | Form-scoped only — no persistent records | Expression Evaluator (show/hide, defaults, warnings) |
| L3 | Policy | System-scoped — creates persistent events | Trigger Engine (L3a event-reaction, L3b deadline-check, auto-resolution) |
| Code | Platform evolution | Unbounded | All primitives — new types, patterns, scope types, context.* properties |

**Boundary principle:** A capability's layer is determined by its side effects, not its complexity.

### 5.5. Configuration Delivery Pipeline

End-to-end flow from deployer authoring to device operation:

```
Deployer authors configuration (shapes, activities, triggers, patterns, rules)
    ↓
Deploy-time Validator checks all budgets and composition rules
    ├── Reject → violations reported, deployer revises
    └── Accept ↓
Config Packager creates atomic configuration package
    ↓
Device receives package at next sync
    ↓
Device applies new config after completing in-progress work under old config
    (at most 2 versions coexist: current + previous)
```

---

## 6. Specification Structure

### The question

Should the platform specification be one file or a split directory? If split, how?

### Size estimate

Each primitive needs ~50–80 lines (description, invariants, interfaces, configuration surface, interactions, escape hatches). 11 primitives × ~65 lines = ~715 lines. Cross-cutting concerns add ~200 lines. Overview, glossary, and reading guide add ~100 lines. **Estimated total: ~1,000 lines.**

A single 1,000-line document is evaluable but heavy. Split is warranted.

### Recommended structure: `docs/specifications/`

```
docs/specifications/
  README.md                          # Overview, glossary, reading guide, primitive index
  01-storage-and-projection.md       # Event Store, Projection Engine
  02-identity-and-integrity.md       # Identity Resolver, Conflict Detector
  03-authorization-and-sync.md       # Scope Resolver
  04-configuration.md                # Shape Registry, Expression Evaluator, Trigger Engine,
                                     #   Deploy-time Validator, Config Packager
  05-workflow.md                     # Pattern Registry, Command Validator (advisory)
  06-cross-cutting.md                # Event envelope, accept-and-flag lifecycle,
                                     #   detect-before-act, gradient, delivery pipeline
```

### Why this split

**By architectural layer, not by ADR.** The specification serves implementers ("what do I build?"), not decision archaeologists ("what was decided when?"). The layers match natural implementation boundaries — a developer working on identity resolution shouldn't need to cross-reference a configuration document.

**01 and 02 will be the densest.** The Projection Engine alone has 10 capabilities spanning all 5 ADRs — its specification section will be substantial. The Conflict Detector has 6 flag categories and the full accept-and-flag lifecycle. These two files will likely be 150–200 lines each.

**04 groups 5 primitives.** This seems dense, but these primitives form a tight cluster: the Shape Registry defines what the Expression Evaluator references, the Trigger Engine uses the Expression Evaluator for conditions, the Deploy-time Validator validates them all, and the Config Packager delivers the result. They're one subsystem.

**06 exists because cross-cutting concerns don't fit cleanly in any primitive's section.** The event envelope is consumed by all 11 primitives. The accept-and-flag lifecycle touches 4 primitives. These need their own home.

### Alternative considered: single file

A single `platform-specification.md` would be simpler to navigate but harder to review section-by-section. Given that this is a documentation-only project with session-based review, split files let us write and evaluate one layer per session without losing context.

---

## 7. Open Questions for Specification

Things the specification must address that ADRs explicitly deferred to "implementation" or "platform specification":

| Question | Source | Which spec section |
|----------|--------|--------------------|
| Pattern inventory: which patterns ship initially, exact state machine skeletons | ADR-5 "What This Does NOT Decide" | 05-workflow |
| Pattern skeleton formal schemas (YAML/JSON format for state machine definitions) | ADR-5 deferred | 05-workflow |
| Projection rebuild strategy (incremental vs. full) | ADR-1/5 deferred | 01-storage-and-projection |
| Source-chain traversal depth limits | ADR-5 S7 deferred | 01-storage-and-projection |
| Sync pagination, priority ordering, bandwidth optimization | ADR-3 deferred | 03-authorization-and-sync |
| Configuration authoring format (YAML, visual builder, etc.) | ADR-4 deferred | 04-configuration |
| Projection merge strategy across schema versions | ADR-4 deferred | 01-storage-and-projection |
| Deploy-time validator UX (how violations are reported) | ADR-4 deferred | 04-configuration |
| `context.*` property caching on-device | ADR-5 S8 deferred | 01-storage-and-projection |
| Workflow-aware reporting and aggregation | ADR-5 deferred | 05-workflow or separate |
| Data archival/retention | Cross-cutting (unaddressed) | 06-cross-cutting |
| Reporting/aggregation model | Viability blind spot 4 | 06-cross-cutting or separate |

Not all of these need answers in the first specification pass. The specification should flag which are implementation decisions (can be deferred further) versus which are specification-grade (needed before implementation starts).

---

## 8. Recommendation

1. **Create `docs/specifications/` with the 6-file structure proposed in §6.**
2. **Write in layer order**: 01 (storage/projection) → 02 (identity/integrity) → 03 (auth/sync) → 04 (configuration) → 05 (workflow) → 06 (cross-cutting). This follows the same dependency order as the ADR sequence — each layer builds on the previous.
3. **Use this inventory as the input.** Each primitive's card (§4) is the blueprint for its specification section. The specification expands each card into implementation-grade detail: exact interfaces, interaction protocols, error cases, and the ADR constraints that bind each decision.
4. **Start immediately.** All inputs exist. This is consolidation, not discovery.
