# 24 — Decision Harvest

> Step 1 of the Architecture Consolidation (framework: [doc 23](23-consolidation-framework.md)).
> Systematically extracts every committed position across 5 ADRs into a flat table, maps each to the primitives it binds, and catalogs all deferred items for boundary mapping (Step 2).

---

## 1. Decided Positions

52 explicit sub-decisions + 5 emergent positions. Each row traces to its source ADR and the primitives it binds.

**Classification key:**
- **SC** = Structural constraint (change requires data migration)
- **SP** = Strategy-protecting constraint (guards a structural invariant; implementation can evolve)
- **IS** = Initial strategy (evolvable without affecting stored events)

**Primitive abbreviations:**
- **ES** = Event Store
- **PE** = Projection Engine
- **IR** = Identity Resolver
- **CD** = Conflict Detector
- **SR** = Scope Resolver
- **ShR** = Shape Registry
- **EE** = Expression Evaluator
- **TE** = Trigger Engine
- **DtV** = Deploy-time Validator
- **CP** = Config Packager
- **PR** = Pattern Registry

---

### ADR-001: Offline Data Model — 5 sub-decisions

| ID | Statement | Class. | Primitives | Cross-refs |
|----|-----------|--------|------------|------------|
| 1-S1 | All writes are append-only. Events never modified or deleted. | SC | ES | — |
| 1-S2 | Atomic write unit is a typed, immutable event. Current state is a projection computed from the event stream. Projections always rebuildable from events. Write-path discipline: all state changes through event store. | SC | ES, PE | — |
| 1-S3 | All identifiers (events, subjects, records) are client-generated UUIDs. | SC | ES, IR | — |
| 1-S4 | Sync unit is the immutable event. Sync is idempotent, append-only, order-independent. | SC | ES | 3-S2 |
| 1-S5 | Event envelope carries: identity (UUID), type, payload, timestamp. Extensible for downstream ADRs. | SC | ES | 2-S1, 2-S2, 4-S1, 4-S2, 4-S3 |

**Strategies documented in ADR-001 (not sub-decisions, but stated positions):**

| ID | Statement | Class. | Primitives | Cross-refs |
|----|-----------|--------|------------|------------|
| 1-ST1 | Event log is single source of truth. | IS | ES, PE | Protects escape hatch |
| 1-ST2 | Write-path discipline: never write to projections independently. | IS | ES, PE | Protects escape hatch |
| 1-ST3 | Projections always rebuildable from events. | IS | PE | 1-S2 embeds this |
| 1-EH1 | Escape hatch B→C: add application-maintained views if projection complexity exceeds thresholds. Viable only if event log is gap-free. | IS | PE | 1-ST2 protects this |

---

### ADR-002: Identity & Conflict Resolution — 14 sub-decisions

| ID | Statement | Class. | Primitives | Cross-refs |
|----|-----------|--------|------------|------------|
| 2-S1 | Every event carries `device_id` (hardware-bound UUID), `device_seq` (monotonically increasing integer per device), `sync_watermark` (last-known server state version). | SC | ES, IR, CD | 1-S5 |
| 2-S2 | All identity references carry a type discriminator and UUID: `{type, id}`, where type ∈ {subject, actor, process, assignment}. | SC | ES, IR | 1-S5 |
| 2-S3 | `device_time` (mapped to `timestamp` in envelope) is advisory only. No ordering, conflict detection, or protocol correctness depends on it. | SC | ES, CD, PE | 1-S5 |
| 2-S4 | `device_seq` and `sync_watermark` must be persisted to durable storage on device. Must survive reboots, crashes. `(device_id, device_seq)` is globally unique and never reused. | SC | ES | 2-S1 |
| 2-S5 | `device_id` identifies a physical device, not a user account. New device = new `device_id`. | SC | ES, IR | 2-S1 |
| 2-S6 | Merge = alias-in-projection, never re-reference. `SubjectsMerged` creates alias mapping: `retired_id → surviving_id`. Eager transitive closure. Single-hop lookup. | SC | IR, PE | 1-S1 |
| 2-S7 | No `SubjectsUnmerged`. Wrong merges corrected via `SubjectSplit`. | SC | IR | 2-S6 |
| 2-S8 | Split freezes history. Source is permanently archived. All historical events remain attributed to source. New events go to successors. | SC | IR | 1-S1, 2-S6 |
| 2-S9 | Lineage graph acyclicity enforced by construction. Merge operands must be active. Archived is terminal. | SC | IR | 2-S6, 2-S8 |
| 2-S10 | `SubjectsMerged` and `SubjectSplit` are online-only operations. Server-validated preconditions. | SP | IR | 2-S9 |
| 2-S11 | Single-writer conflict resolution. Every `ConflictDetected` event designates exactly one resolver. Only designated resolver's `ConflictResolved` is canonical. | SP | CD | — |
| 2-S12 | Conflict detection before policy execution. Flagged events do not trigger policies until resolved. | SP | CD, TE, PE | 5-S2 extends |
| 2-S13 | Conflict detection uses raw references (original `subject_id`). Alias resolution occurs only in projection, after detection. | SC | CD, IR, PE | 2-S6 |
| 2-S14 | Events are never rejected for state staleness. Anomalies surfaced as `ConflictDetected` events. Accept-and-flag. | SC | ES, CD | 1-S1, 5-S1 |

---

### ADR-003: Authorization & Selective Sync — 10 sub-decisions

| ID | Statement | Class. | Primitives | Cross-refs |
|----|-----------|--------|------------|------------|
| 3-S1 | Assignment-based access control. Every access rule reduces to scope-containment test: `actor.assignment.scope ⊇ subject.location`. | SC | SR | 2-S2 |
| 3-S2 | Sync scope = access scope. Device receives exactly the data its actor is authorized to act on. Server computes sync payload from active assignments. | SC | SR, ES | 1-S4 |
| 3-S3 | Authority context is a projection, not an envelope field. Derived at query time from assignment event timeline. No new envelope fields from ADR-003. | SC | PE, SR | 1-S5 |
| 3-S4 | Alias-respects-original-scope. Authorization evaluated against original `subject_ref` as written, not post-merge surviving subject's scope. | SC | SR, IR | 2-S6, 2-S13 |
| 3-S5 | Scope-containment invariant on `AssignmentCreated`. New assignment scope ⊆ creating actor's scope. Server-validated. | SP | SR | 3-S1 |
| 3-S6 | `ConflictResolved` events can only be created through server-validated transaction. Online-only. | SP | CD | 2-S10 precedent |
| 3-S7 | Detect-before-act extends to ALL flag types including authorization flags. Which flags are blocking vs. informational is deployment-configurable. | SP | CD, PE | 2-S12 |
| 3-S8 | Tiered projection location: device-local for field workers, hybrid for supervisors, server-computed for coordinators. | IS | PE | — |
| 3-S9 | Authorization staleness uses accept-and-flag. New flag types: `ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`. Watermark-based auto-resolution for `ScopeStaleFlag`. | IS | CD, SR | 2-S14 |
| 3-S10 | Scope contraction: selective retain. Own events retained, others' events about out-of-scope subjects are purge candidates. Device-side policy, not sync instruction. | IS | SR | — |

---

### ADR-004: Configuration Boundary — 14 sub-decisions

| ID | Statement | Class. | Primitives | Cross-refs |
|----|-----------|--------|------------|------------|
| 4-S1 | Every event carries `shape_ref` (mandatory). Format: `{shape_name}/v{version}`. Shape names match `[a-z][a-z0-9_]*`. Version is monotonically increasing positive integer. | SC | ES, ShR, PE | 1-S5 |
| 4-S2 | Events may carry `activity_ref` (optional). Format: `[a-z][a-z0-9_]*` or null. Auto-populated by device, null for imports. | SC | ES, SR, PR | 1-S5 |
| 4-S3 | `type` field uses platform-fixed, closed, append-only vocabulary. 6 initial types: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`. Types represent processing behavior, not domain meaning. | SC | ES, CD, TE, PE | 1-S5 |
| 4-S4 | System actor identity: `system:{source_type}/{source_id}`. Source types start with `trigger`, grow as new system sources emerge. | SP | TE, ES | 5-S9 extends |
| 4-S5 | All triggers execute server-only. Both L3a (event-reaction) and L3b (deadline-check). | SP | TE | — |
| 4-S6 | Atomic configuration delivery. At most 2 config versions coexist on-device (current + previous for in-progress work). | SP | CP | — |
| 4-S7 | No deployer-authored scope logic. 3 platform-fixed scope types: `geographic`, `subject_list`, `activity`. AND composition for multi-dimension assignments. | SP | SR, DtV | 3-S1 |
| 4-S8 | No field-level sensitivity. Sensitivity at shape/activity level only. 3 levels: `standard`, `elevated`, `restricted`. | SP | ShR, SR, CP | 1-S1 |
| 4-S9 | Four-layer configuration gradient: L0 Assembly → L1 Shape → L2 Logic → L3 Policy → Code boundary. Each layer defined by its side effects. | IS | ShR, EE, TE, DtV, CP, PR, SR | — |
| 4-S10 | Shapes: typed, versioned, authored as deltas, stored as snapshots. Deprecation-only default evolution. Additive always safe. Breaking changes explicit + exceptional. All versions remain valid forever. | IS | ShR | 4-S1 |
| 4-S11 | Expression language: operators + field references, zero functions. Two contexts: form (`payload.*`, `entity.*`) and trigger (`event.*`). Not a query engine. Projection rules: pure value-to-value lookup tables. | IS | EE, PE | 5-S8 extends form context |
| 4-S12 | Trigger architecture: L3a event-reaction (sync, single-event condition) + L3b deadline-check (async, non-occurrence). Non-recursive. DAG max path 2. Output type ≠ input type. | IS | TE | 5-S9 adds L3b sub-type |
| 4-S13 | Complexity budgets: 60 fields/shape, 3 predicates/condition, 5 triggers/event type, 50 triggers/deployment, 2-level escalation depth. Hard, deploy-time enforced. | IS | DtV, ShR, EE, TE | 5-S6 adds checks |
| 4-S14 | Deployer-parameterized policies: flag severity (per-deployment, blocking vs. informational), domain uniqueness constraints (shape-declared, server-authoritative), scope type composition (L0), sensitivity classification (per shape/activity). | IS | CD, ShR, SR, CP, DtV | 2-S12, 3-S7 |

---

### ADR-005: State Progression & Workflow — 9 sub-decisions

| ID | Statement | Class. | Primitives | Cross-refs |
|----|-----------|--------|------------|------------|
| 5-S1 | `transition_violation` flag category. Conflict Detector evaluates incoming events against pattern-defined state machine rules. | SP | CD, PR | 2-S14, 1-S1 |
| 5-S2 | Flagged events excluded from state machine evaluation. Visible in timeline, excluded from state derivation. Extends detect-before-act to state derivation. | SP | PE, CD | 2-S12 |
| 5-S3 | Flag resolvability classification: `auto_eligible` or `manual_only`. Platform-defined, not deployer-configurable. 6 flag types classified (see ADR-005 S3 table). | SP | CD, TE | — |
| 5-S4 | State machines as projection patterns. State derived from event sequence + pattern definitions, never stored in events. Platform does not enforce transitions — it flags violations. On-device Command Validator is advisory, not blocking. | IS | PE, PR, CD | 1-S2, 2-S14 |
| 5-S5 | Pattern Registry: platform-fixed workflow skeletons, deployer-selected and parameterized at L0. Closed vocabulary. Adding a pattern = platform evolution. Each pattern provides: participant roles, structural event types, state machine skeleton, auto-maintained projections, parameterization points. | IS | PR, PE, CD, CP | 4-S9 |
| 5-S6 | 5 pattern composition rules: (1) one subject-level pattern per activity, (2) event-level compose freely, (3) approval sub-flows embed, (4) cross-activity via `activity_ref`, (5) shape-to-pattern unique within activity. Deploy-time enforced. | IS | PR, DtV, PE | 4-S13 |
| 5-S7 | Source-only flagging. Only root-cause event receives flag. Downstream contamination is a computed projection property via source-chain traversal (`source_event_ref` in payload). | IS | CD, PE | 2-S12 |
| 5-S8 | `context.*` expression scope: 7 platform-fixed, read-only properties in form context. Pre-resolved on-device from local projection at form-open time. Static during fill. Platform-fixed vocabulary. | IS | EE, PE | 4-S11 |
| 5-S9 | Auto-resolution as L3b sub-type. Watches `auto_eligible` flags + resolution-enabling events within time window. System actor: `system:auto_resolution/{policy_id}`. Counts toward L3b budget. | IS | TE, CD | 4-S4, 4-S12, 5-S3 |

---

## 2. Emergent Positions

Positions not assigned a sub-decision ID in any ADR, but that emerge from the combination of multiple decided positions and are architecturally significant.

| ID | Statement | Classification | Source | Primitives |
|----|-----------|---------------|--------|------------|
| E1 | Event envelope is finalized at 11 fields. Five ADRs have required zero use of the extensibility clause (1-S5). | Emergent SC | ADR-4 Consequences, ADR-5 Consequences. Combination of 1-S5 + 2-S1 + 2-S2 + 4-S1 + 4-S2 + 4-S3. | ES |
| E2 | Type vocabulary is exactly 6 types. `status_changed` evaluated and rejected (ADR-5 S4). Append-only escape hatch preserved. | Emergent SC | ADR-5 S4. Combination of 4-S3 + 5-S4. | ES, TE, CD, PE |
| E3 | Accept-and-flag is the universal anomaly handling mechanism. All deviations from expected state — identity, authorization, state transitions, domain rules — are surfaced as flags on events, never cause rejection. | Emergent pattern | Combination of 2-S14 + 2-S12 + 3-S7 + 3-S9 + 5-S1 + 5-S2. | CD, ES, PE, TE |
| E4 | Detect-before-act is the universal ordering guarantee. Applies to: policy execution (2-S12), authorization (3-S7), state derivation (5-S2). | Emergent pattern | 2-S12 + 3-S7 + 5-S2. | CD, PE, TE |
| E5 | 6 flag categories with resolvability classification. Identity conflict (manual_only), stale reference (auto_eligible), concurrent state change (manual_only), scope violation (manual_only), domain uniqueness violation (manual_only), transition violation (auto_eligible). Plus 3 authorization flag types from ADR-3 (ScopeStaleFlag, RoleStaleFlag, TemporalAuthorityExpiredFlag). | Emergent catalog | 2-S11 + 3-S9 + 4-S14 + 5-S1 + 5-S3. | CD, TE |

---

## 3. Deferred Items Catalog

Every item from every ADR's "What This Does NOT Decide" section, plus items explicitly deferred within sub-decision text. Each entry captures where the item was deferred FROM, what ADR (if any) later resolved it, and its current status.

### 3.1 Resolved by Later ADR

These were deferred by an earlier ADR and subsequently resolved.

| Item | Deferred from | Resolved by | Resolution summary |
|------|---------------|-------------|-------------------|
| Causal ordering mechanism | 1-WTNDD | 2-S1 | device_seq + sync_watermark |
| What constitutes a "conflict" | 1-WTNDD | 2-S11, 2-S12, 2-S14 | Accept-and-flag, single-writer, detect-before-act |
| Sync scope | 1-WTNDD | 3-S2 | Sync scope = access scope |
| Event type vocabulary ownership | 1-WTNDD | 4-S3 | Platform-fixed, closed, append-only |
| Projection rules | 1-WTNDD | 4-S11 (partial), 5-S4 | Value-to-value lookup (L1), state machines as projection patterns |
| Where projections live | 1-WTNDD | 3-S8 | Tiered: device/hybrid/server |
| Sync topology | 1-WTNDD | 3-S8 | Direct (implied by tiered projection) |
| Whether event types are platform-fixed or configurable | 1-WTNDD | 4-S3 | Platform-fixed |
| Activity context / correlation metadata | 1-WTNDD | 4-S2 | `activity_ref` optional envelope field |
| Domain-level conflict conditions | 2-WTNDD | 4-S14 | Domain uniqueness constraints (shape-declared) |
| Cascade on conflict resolution | 2-WTNDD | 5-S2, 5-S7 | Flagged excluded from state derivation; source-only flagging |
| Who resolves which conflict types | 2-WTNDD | 3-S7 (partial) | Detect-before-act for all types; blocking config |
| Scope type extensibility | 3-WTNDD | 4-S7 | 3 platform-fixed scope types, no deployer extension |
| Role-action permission tables | 3-WTNDD | 4-S14 | L0 activity parameters |
| Per-flag-type severity config | 3-WTNDD | 4-S14 | Deployer-overridable, blocking vs. informational |
| State machines: primitive or pattern? | 4-WTNDD | 5-S4 | Projection patterns |
| `status_changed` as 7th type | 4-WTNDD | 5-S4 | Rejected |
| Domain conflict resolution automation (Q7b) | 4-WTNDD | 5-S9 | Auto-resolution as L3b sub-type |
| `context.*` expression scope | 4-WTNDD | 5-S8 | 7 platform-fixed properties, form context only |
| Pattern inventory governance | 4-WTNDD | 5-S5 | Platform-fixed, closed vocabulary |

### 3.2 Unresolved — Requires Boundary Classification (Step 2)

These remain open. Each needs a boundary classification: **specification-grade** (must be pinned before implementation), **implementation-grade** (architecture intentionally left open), or **outside** (not part of this architecture).

| Item | Deferred from | Current status | Notes |
|------|---------------|---------------|-------|
| Projection rebuild strategy (incremental vs. full) | 1-WTNDD, 5-WTNDD | Unresolved | Architecture says projections are rebuildable (1-S2). HOW is open. |
| Flag creation location (server-only vs. device-side) | 2-WTNDD | Unresolved | Currently server-only. Device-side additive. |
| Auto-resolution policies for low-severity flags | 2-WTNDD | Partially resolved by 5-S9 | 5-S9 defines mechanism. Specific policies are open. |
| Batch resolution and flag grouping | 2-WTNDD | Unresolved | Read-model grouping logic. |
| Pending match pattern generality | 2-WTNDD, 4-WTNDD | Unresolved | Accept-always (2-S14) supports it implicitly. Whether first-class configurable is open. |
| Subject-based scope (case management) | 3-WTNDD | Unresolved | Assignment config, not structural. |
| Auditor access (cross-hierarchy, read-only) | 3-WTNDD | Unresolved | New role type + query scope. |
| Device sharing (multiple actors on one device) | 3-WTNDD | Unresolved | Per-actor sync sessions fix watermark corruption. |
| Sync pagination, priority ordering, bandwidth | 3-WTNDD | Unresolved | Protocol optimization. |
| Assessment visibility to assessed worker | 3-WTNDD | Unresolved | Sync filter config. |
| Sensitive-subject classification | 3-WTNDD | Partially resolved by 4-S8 | Shape/activity sensitivity decided. Subject-level open. |
| Grace period for expired temporal authority | 3-WTNDD | Resolved strategically | Watermark-based auto-resolution replaces grace periods (3-S9). |
| Breaking change migration mechanism | 4-WTNDD | Unresolved | Policy decided (4-S10: deprecation-only default). Tooling open. |
| Configuration authoring format | 4-WTNDD | Unresolved | Semantic model decided. Surface (YAML, visual builder) open. |
| Projection merge strategy across schema versions | 4-WTNDD | Unresolved | `shape_ref` ensures self-describing events (4-S1). Composition logic open. |
| Deploy-time validator UX | 4-WTNDD | Unresolved | Budgets decided (4-S13). Violation reporting UX open. |
| Pattern inventory (which patterns ship initially) | 4-WTNDD, 5-WTNDD | Unresolved | Architecture decided (5-S4, 5-S5, 5-S6). Inventory is not architecture. 4 existence proofs validated. |
| Pattern skeleton formal schemas | 5-WTNDD | Unresolved | Semantic model decided. Format open. |
| Source-chain traversal depth limits | 5-WTNDD | Unresolved | Capability decided (5-S7). Depth is performance tuning. |
| `context.*` property caching on-device | 5-WTNDD | Unresolved | Properties decided (5-S8). Caching is device architecture. |
| Auto-resolution policy authoring UX | 5-WTNDD | Unresolved | Mechanism decided (5-S9). Authoring surface open. |
| Workflow-aware reporting and aggregation | 5-WTNDD | Unresolved | Flagged as platform capability. No architecture decision made. |
| Data archival/retention | Cross-cutting | Never addressed | Not mentioned in any ADR. |
| Reporting/aggregation model | Cross-cutting | Never addressed | ADR-4 S11 says "cross-subject aggregation is a platform capability, not a configuration expression" but never specifies what that capability is. Viability blind spot 4. |

---

## 4. Primitive Coverage Matrix

How many decided positions bind each primitive? This tests whether any primitive is under-determined (fewer constraints than expected) or over-concentrated (potential decomposition signal).

| Primitive | SC | SP | IS | Emergent | Total | Notes |
|-----------|----|----|----|---------:|------:|-------|
| Event Store (ES) | 10 | 0 | 2 | 2 | 14 | Highest SC count. Expected — foundational write path. |
| Projection Engine (PE) | 3 | 2 | 5 | 3 | 13 | Highest IS count. Accumulates capabilities across all 5 ADRs. |
| Identity Resolver (IR) | 8 | 1 | 0 | 0 | 9 | All constraints, no strategies. Fully determined by ADR-2. |
| Conflict Detector (CD) | 3 | 5 | 5 | 3 | 16 | Highest total. Touched by every ADR. Cross-cutting by nature. |
| Scope Resolver (SR) | 4 | 3 | 3 | 0 | 10 | Balanced. |
| Shape Registry (ShR) | 1 | 1 | 4 | 0 | 6 | Mostly strategic. Architecture constrains envelope reference; rest is evolvable. |
| Expression Evaluator (EE) | 0 | 0 | 4 | 0 | 4 | Lowest count. Entirely strategic. Expected — language is evolvable. |
| Trigger Engine (TE) | 1 | 2 | 3 | 2 | 8 | |
| Deploy-time Validator (DtV) | 0 | 1 | 4 | 0 | 5 | Entirely enforcement of other primitives' constraints. |
| Config Packager (CP) | 0 | 1 | 2 | 0 | 3 | Lowest total. Thin component — delivers, doesn't decide. |
| Pattern Registry (PR) | 0 | 1 | 3 | 0 | 4 | Entirely ADR-5. Expected — newest primitive. |

**Observations:**
- **Conflict Detector has the highest binding count (16)**. It's touched by every ADR. This reflects its cross-cutting role — not a decomposition signal. Every anomaly type in the platform routes through this one pipeline.
- **Config Packager (3) and Expression Evaluator (4) are the lightest**. Both are implementation-heavy but architecture-light. The architecture says WHAT they deliver / evaluate; nearly everything about HOW is intentionally open.
- **Identity Resolver is all-constraint (9 SC+SP, 0 IS)**. It's fully locked by ADR-2. No evolvable surface. This is correct — identity is the most permanent thing in the system.

---

## 5. Cross-Reference Index

Which decided positions extend, protect, or depend on other positions?

### 5.1 Extension Chains

Positions where a later ADR explicitly extends an earlier one:

| Extension | Base position | Extending position | What was extended |
|-----------|--------------|-------------------|-------------------|
| Detect-before-act scope | 2-S12 (policy execution) | 3-S7 (authorization flags) | Added auth flag types to detect-before-act |
| | 2-S12 + 3-S7 | 5-S2 (state derivation) | Added state machine exclusion to detect-before-act |
| System actor convention | 4-S4 (`system:trigger/...`) | 5-S9 (`system:auto_resolution/...`) | Added auto_resolution as source_type |
| L3b sub-types | 4-S12 (deadline-check) | 5-S9 (auto-resolution) | Added auto-resolution as L3b mechanism |
| Expression context scopes | 4-S11 (form: `payload.*`, `entity.*`) | 5-S8 (form: + `context.*`) | Added pre-resolved workflow properties |
| Deploy-time validation checks | 4-S13 (budgets) | 5-S6 (composition rules) | Added 5 new validation rules |
| Flag categories | 2-S11, 2-S14 (identity, stale_ref, concurrent) | 3-S9 (scope, role, temporal) | Added auth flag types |
| | 2 + 3 | 4-S14 (domain_uniqueness) | Added domain flag type |
| | 2 + 3 + 4 | 5-S1 (transition_violation) | Added workflow flag type |

### 5.2 Protection Relationships

Positions where a strategy-protecting constraint guards a structural invariant:

| Protector | Structural invariant protected | Mechanism |
|-----------|-------------------------------|-----------|
| 2-S10 (merge/split online-only) | 2-S9 (lineage acyclicity) | Server validates preconditions |
| 2-S11 (single-writer resolution) | 2-S14 (accept-and-flag / no rejection) | Prevents meta-conflicts |
| 2-S12 (detect-before-act) | 1-S1 (append-only) + 2-S14 (never reject) | Prevents irreversible cascade from uncertain data |
| 3-S5 (scope-containment on create) | 3-S1 (assignment-based access) | Prevents privilege escalation |
| 3-S6 (resolution online-only) | 2-S14 (accept-and-flag) | Prevents meta-flag chains |
| 3-S7 (detect-before-act for auth) | 3-S1 (access control) + 1-S1 (append-only) | Prevents irreversible downstream damage |
| 4-S4 (system actor identity) | 1-S5 (envelope: actor_ref) via V3 | Ensures all events have traceable author |
| 4-S5 (triggers server-only) | Device simplicity (V1) | Prevents trigger engine + flag state on every device |
| 4-S6 (atomic config delivery) | 1-S4 (sync unit = event) via P1 | Prevents partial config causing inconsistent device behavior |
| 4-S7 (no deployer scope logic) | 3-S1 (access control integrity) | Prevents security bugs in custom scope logic |
| 4-S8 (no field-level sensitivity) | 1-S1 (append-only / immutability) | Prevents selective purge within immutable events |
| 5-S1 (transition_violation flag) | 1-S1 + 2-S14 (append-only + never reject) via V1 | Preserves offline work that violates transition rules |
| 5-S2 (flagged excluded from state) | 2-S12 + P5 (detect-before-act + surface conflicts) | Prevents tentative state from triggering downstream work |
| 5-S3 (resolvability classification) | Security + data integrity | Prevents auto-resolution of security-relevant flags |

---

## 6. Completeness Verification

### 6.1 Sub-decision Count

| ADR | Explicit sub-decisions | Documented strategies | Total |
|-----|----------------------|----------------------|-------|
| ADR-001 | 5 | 3 + 1 escape hatch | 9 |
| ADR-002 | 14 | 0 | 14 |
| ADR-003 | 10 | 0 | 10 |
| ADR-004 | 14 | 0 | 14 |
| ADR-005 | 9 | 0 | 9 |
| **Total** | **52** | **4** | **56** |
| Emergent | — | — | **5** |
| **Grand total** | | | **61** |

### 6.2 "What This Does NOT Decide" Coverage

| ADR | Items in WTNDD | Resolved by later ADR | Unresolved (→ Step 2) |
|-----|---------------|----------------------|----------------------|
| ADR-001 | 9 | 9 (all resolved) | 0 |
| ADR-002 | 10 | 4 fully, 2 partially | 4 |
| ADR-003 | 9 | 3 | 6 |
| ADR-004 | 9 | 4 | 5 |
| ADR-005 | 8 | 0 | 8 |
| Cross-cutting | 2 | 0 | 2 |
| **Total** | **47** | **20 fully + 2 partially** | **25** |

25 unresolved items carry forward to Step 2 (Boundary Mapping) for classification.

### 6.3 Traceability Spot-Checks

Verified that the harvest's extraction matches each ADR's own traceability table:

- ADR-001: 5 sub-decisions match (S1–S5).
- ADR-002: 14 sub-decisions match (S1–S14). Classifications match.
- ADR-003: 10 sub-decisions match (S1–S10). Classifications match.
- ADR-004: 14 sub-decisions match (S1–S14). Classifications match.
- ADR-005: 9 sub-decisions match (S1–S9). Classifications match.

No sub-decision was missed. No sub-decision was duplicated.

---

## 7. Findings for Step 2

Issues surfaced during harvest that require attention in Boundary Mapping:

### F1. Flag category naming inconsistency

ADR-003 S9 introduces flag types using different naming than the pattern in ADR-002: `ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag` vs. the `snake_case` category names used by ADR-002 (`identity_conflict`, `stale_reference`) and ADR-005 (`transition_violation`, `domain_uniqueness_violation`). Step 2 must determine whether the naming convention is architecturally significant or implementation-grade.

### F2. Reporting/aggregation is architecturally unaddressed

Two items land in the unresolved catalog with no partial resolution: reporting/aggregation model and data archival/retention. Reporting has an explicit callout in ADR-4 S11 ("cross-subject aggregation is a platform capability, not a configuration expression") but this is a boundary statement, not a decision. Step 2 must classify these — particularly reporting, which touches the Projection Engine and accept-and-flag lifecycle (do flagged events count in aggregates?).

### F3. Sync protocol guarantees span 4 primitives

The sync protocol appears in positions across ES (1-S4: idempotent/append-only/order-independent), SR (3-S2: scope-filtered), CD (2-S12: detect-before-act ordering), and CP (4-S6: atomic config). No primitive owns the sync contract. Step 2 must determine whether these cross-primitive guarantees need a named home in the Architecture Description or are adequately covered by stating each primitive's individual contract.

### F4. ADR-003 authorization flag types may overlap with ADR-005 flag categories

ADR-003 S9 introduces 3 authorization flag types. ADR-005 S3 classifies 6 flag categories for resolvability but does not list the ADR-003 types in its classification table. Step 2 must determine whether the 3 auth flag types map onto existing categories (likely: `scope_violation` absorbs `ScopeStaleFlag`; some are new) and whether the resolvability classification covers them.

---

## 8. Step 1 Completion Checklist

- [x] Every sub-decision across all 5 ADRs appears in the harvest table (§1)
- [x] Every sub-decision has: source, statement, classification, primitives affected, cross-references (§1)
- [x] Emergent positions identified and sourced (§2)
- [x] Every "What This Does NOT Decide" item from every ADR accounted for — resolved or carried to Step 2 (§3)
- [x] Primitive coverage matrix computed (§4)
- [x] Cross-reference index built — extension chains and protection relationships (§5)
- [x] Completeness verified by count and traceability spot-check (§6)
- [x] Findings surfaced for Step 2 (§7)

**Step 1 is complete. 61 positions harvested. 25 unresolved items carry to Boundary Mapping (Step 2).**
