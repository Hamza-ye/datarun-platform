# 25 — Boundary Mapping

> Step 2 of the Architecture Consolidation (framework: [doc 23](23-consolidation-framework.md)).
> Classifies every unresolved concern into exactly one category: decided, specification-grade, implementation-grade, or outside. Resolves the 4 findings from the Decision Harvest ([doc 24](24-decision-harvest.md)).

---

## 1. Classification Categories

| Category | Meaning | Architecture Description treatment |
|----------|---------|-------------------------------------|
| **Decided** | An ADR committed this. | State as-is. Trace to source. |
| **Spec-grade** | Not decided by ADR, but must be consolidated before the Architecture Description is complete. Existing decided material contains enough to derive the answer — no new exploration needed. | Consolidate from decided positions. Flag the derivation. |
| **Impl-grade** | Architecture intentionally left this open. Multiple valid implementations exist within the decided constraints. | State the constraint boundary. Do not fill in. |
| **Outside** | Not part of this architecture. Named so it doesn't leak in. | State the exclusion. Declare interface points if relevant. |

---

## 2. Harvest Findings Resolution

Four findings from doc 24 §7 that require resolution before classifying the deferred items.

### F1. Flag Category Naming Inconsistency

**Finding**: ADR-002/005 use `snake_case` for flag categories (`identity_conflict`, `stale_reference`, `transition_violation`). ADR-003 S9 uses `PascalCaseFlag` (`ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`).

**Classification: Spec-grade.**

The ADRs decided what is flagged and how flags behave. The naming convention is not an architectural constraint — no stored data depends on these strings (flag categories are metadata within `ConflictDetected` event payloads, whose schema is shaped by Shape Registry). However, the Architecture Description must present a unified flag catalog, which requires a consistent naming convention.

**Derivation**: ADR-002 established the pattern first (`identity_conflict`, `stale_reference`, `concurrent_state_change`). ADR-004 and ADR-005 followed the same convention (`domain_uniqueness_violation`, `transition_violation`). ADR-003's `PascalCaseFlag` names were introduced in a strategy section (S9, initial strategy) and are the outliers. The Architecture Description should normalize all flag categories to `snake_case` per the established majority pattern. The three ADR-003 authorization flag types normalize to: `scope_stale`, `role_stale`, `temporal_authority_expired`.

### F2. Reporting/Aggregation Architecturally Unaddressed

**Finding**: No ADR commits a position on reporting or aggregation. ADR-4 S11 says "cross-subject aggregation is a platform capability, not a configuration expression" — a boundary statement, not a decision.

**Classification: The concern requires decomposition. Two sub-concerns have different classifications.**

**F2a. Aggregation interface points — Spec-grade.**

The Architecture Description must declare where aggregation connects to the decided architecture. This is derivable from existing positions:
- Aggregation reads from the Projection Engine's outputs (per-subject state is the input to any cross-subject aggregation).
- Flagged events are excluded from state derivation (5-S2) — which means aggregation based on projection state automatically excludes flagged events. This is a consequence, not a new decision.
- Aggregation does not write to the Event Store — it produces read-only views. This follows from write-path discipline (1-ST2) — aggregation is a projection concern.

The Architecture Description should state: aggregation is a server-side projection capability that consumes per-subject projected state. It inherits the flagged-event-exclusion guarantee from 5-S2. It does not create events. Its configuration surface (what deployers can define as aggregate views) is outside the architecture.

**F2b. Aggregation model, configuration surface, and deployer experience — Outside.**

What aggregation looks like to deployers (which aggregate views exist, how they're defined, how they're queried) is not constrained by any ADR. Multiple valid models exist: pre-defined dashboards, deployer-configured aggregate rules, SQL-based reporting, external BI integration. The architecture constrains the input (per-subject projection state) and the integrity guarantee (flagged exclusion). Everything else is outside.

**Interface point**: The Architecture Description declares that the Projection Engine's per-subject state is the aggregation input surface, and that 5-S2 extends to any downstream consumer including aggregation.

### F3. Sync Protocol Guarantees Span 4 Primitives

**Finding**: No single primitive owns the sync contract. Guarantees come from ES (1-S4: idempotent, append-only, order-independent), SR (3-S2: scope-filtered), CD (2-S12: detect-before-act ordering), and CP (4-S6: atomic config delivery).

**Classification: Spec-grade.**

The sync contract is not a primitive. It is a set of cross-primitive guarantees that the Architecture Description must present as a coherent whole. Each guarantee traces to a decided position — no invention needed.

**Treatment**: The Architecture Description should include the sync contract as a cross-cutting concern (alongside the event envelope, accept-and-flag, and detect-before-act). The contract states what the sync protocol GUARANTEES — not what it IS (no protocol description, no sequence diagrams, no component).

Sync contract guarantees (each traced to source):

| Guarantee | Source |
|-----------|--------|
| Events are the sync unit. Sync transfers events the receiver hasn't seen. | 1-S4 |
| Sync is idempotent — receiving the same event twice is a no-op. | 1-S4 |
| Sync is append-only — the server never instructs deletion or modification. | 1-S4 |
| Sync is order-independent — events carry their own ordering metadata. | 1-S4, 2-S1 |
| Sync payload is scope-filtered — device receives exactly the data its actor is authorized for. | 3-S2 |
| Conflict detection runs before policy execution on synced events. | 2-S12 |
| Configuration is delivered atomically at sync. At most 2 versions coexist on device. | 4-S6 |
| Merge/split/resolution events sync like any other event, filtered by scope. | 3-S2 (general), 2-S10, 3-S6 (online-only creation) |

### F4. ADR-003 Authorization Flag Types Not in ADR-005 Resolvability Table

**Finding**: ADR-005 S3 classifies 6 flag categories for resolvability but doesn't list the 3 ADR-003 authorization flag types.

**Classification: Spec-grade.**

The Architecture Description must present a complete, unified flag catalog with resolvability classification for every flag type. This is derivable:

- `scope_stale` (was `ScopeStaleFlag`): ADR-003 S9 already specifies watermark-based auto-resolution → **auto_eligible**.
- `role_stale` (was `RoleStaleFlag`): ADR-003 S7 says this can be blocking for capability-restricted actions. Requires human review of whether the role downgrade affects the event → **manual_only**.
- `temporal_authority_expired` (was `TemporalAuthorityExpiredFlag`): ADR-003 S9 calls this informational. Expired temporal authority is typically a timing overlap (assignment ended, worker didn't know). Structurally similar to `stale_reference` → **auto_eligible**.

The unified catalog (9 flag categories total) is presented in §4.

---

## 3. Deferred Items Classification

25 items from the harvest (doc 24 §3.2). Each classified into exactly one category.

### 3.1 Specification-Grade

Items that must be consolidated into the Architecture Description. The existing decided material contains enough to derive the answer.

| # | Item | Constraint boundary (what the architecture decided) | What the Architecture Description must state |
|---|------|-----------------------------------------------------|-----------------------------------------------|
| SG-1 | Flag creation location (server-only vs. device-side) | 2-S12 (detect-before-act) requires flags to gate policy execution. Policies execute server-only (4-S5). Device has no flag state for other subjects. | Flags are created server-side during sync processing. Device-side flag creation is an additive platform evolution, not an architectural constraint — the architecture neither requires nor prohibits it. The detect-before-act guarantee (2-S12) applies at the point where policies execute, which is server-side. |
| SG-2 | Unified flag catalog | E5 (emergent), F1, F4 from harvest | Architecture Description presents the complete 9-category flag catalog with normalized names, resolvability classification, and source ADR. See §4. |
| SG-3 | Sync contract | F3 from harvest | Cross-cutting concern with 8 traced guarantees. See F3 resolution above. |
| SG-4 | Aggregation interface points | F2a from harvest | Aggregation is a server-side projection capability. Input = per-subject projection state. Inherits flagged-event-exclusion (5-S2). Does not write to Event Store. |
| SG-5 | Sensitive-subject classification | 4-S8 decides shape/activity-level sensitivity. Subject-level is open. | Subject-level sensitivity is not architecturally distinguished from shape/activity sensitivity. Events about a sensitive subject are sensitive because they use a sensitive shape or belong to a sensitive activity — the sensitivity attaches to the data structure, not the subject identity. If subject-level sensitivity is needed (a specific person is sensitive regardless of what data is collected about them), it would require a new scope-filtering dimension — a platform evolution, not a configuration change. The Architecture Description declares this as an interface point: the scope-filtering mechanism (3-S2) is the extension surface. |

### 3.2 Implementation-Grade

Items where the architecture decided the constraint boundary and intentionally left the implementation open. Multiple valid approaches exist within the constraints.

| # | Item | Constraint boundary (decided) | Why implementation is open |
|---|------|-------------------------------|---------------------------|
| IG-1 | Projection rebuild strategy (incremental vs. full) | Projections are always rebuildable from events (1-S2). Escape hatch B→C available (1-EH1). | The architecture constrains WHAT (rebuildable from events). Whether rebuild is full, incremental, or materialized is a performance/device strategy. Multiple valid approaches (full replay, snapshot + incremental, CQRS-style materialized views). |
| IG-2 | Projection merge strategy across schema versions | Events are self-describing via `shape_ref` (4-S1). All versions remain valid forever (4-S10). | The architecture constrains WHAT (route on `shape_ref`, all versions valid). How multi-version streams compose into a single per-subject projection is server/device logic. |
| IG-3 | Batch resolution and flag grouping | Single-writer resolution (2-S11). ConflictResolved events individually created. | The architecture constrains WHAT (each resolution is its own event with designated resolver). Grouping in the review UI and batch-creating individual resolution events is a read-model and UX concern. |
| IG-4 | Sync pagination, priority ordering, bandwidth | Sync unit = event (1-S4). Scope-filtered (3-S2). Idempotent (1-S4). | The architecture constrains WHAT syncs and that it's idempotent. Whether sync sends recent events first, paginates, or prioritizes is protocol optimization. |
| IG-5 | Source-chain traversal depth limits | Source-only flagging with source-chain traversal (5-S7). | The architecture decides the capability. Maximum traversal depth is a performance tuning parameter — limited by the DAG max path 2 (4-S12) which bounds trigger-generated chain length at 3 events. Organic chains (human-authored events referencing prior events) have no architectural limit. A practical depth limit is a performance trade-off. |
| IG-6 | `context.*` property caching on-device | 7 properties, pre-resolved at form-open time, static during fill (5-S8). | The architecture decides WHAT properties exist and WHEN they resolve. How they're cached between form opens is device-side optimization. |
| IG-7 | Breaking change migration mechanism | Deprecation-only default. Breaking changes explicit and exceptional (4-S10). | The architecture decides the policy (deprecation-only, breaking requires acknowledgment). The tooling (migration scripts, version comparison, deployer workflow) is implementation. |
| IG-8 | Configuration authoring format | Four-layer gradient (4-S9). Shape semantics (4-S10). Expression constraints (4-S11). Trigger architecture (4-S12). | The architecture decides the semantic model. Whether deployers write YAML, use a visual builder, or use a constrained DSL is tooling. |
| IG-9 | Deploy-time validator UX | Hard budgets (4-S13). Composition rules enforced at deploy-time (5-S6). | The architecture decides WHAT is validated and that rejection is binary (accept or reject with violations). How violations are reported to the deployer is UX. |
| IG-10 | Pattern skeleton formal schemas | Patterns provide: participant roles, structural event types, state machine skeleton, auto-maintained projections, parameterization points (5-S5). | The architecture decides what each pattern CONTAINS. Whether patterns are defined in YAML, JSON, or code is format. |
| IG-11 | Pattern inventory (which patterns ship initially) | Platform-fixed, closed vocabulary (5-S5). 4 existence proofs validated. | The architecture decides governance (platform-fixed, deployer-selects). Which patterns ship is inventory — a platform development decision, not an architectural one. |
| IG-12 | Auto-resolution policy authoring UX | Mechanism decided: watches `auto_eligible` flags + enabling events within time window (5-S9). | The architecture decides the mechanism. How deployers author policies (form builder, YAML, constrained template) is tooling. |
| IG-13 | Auto-resolution specific policies | Mechanism decided (5-S9). `auto_eligible` flag types decided (5-S3). | The architecture decides what CAN be auto-resolved and the resolution mechanism. Which specific policies ship by default is a platform decision, not architecture. |
| IG-14 | Device sharing (multiple actors on one device) | `device_id` is hardware-bound (2-S5). `device_seq` scoped to `device_id` (2-S1). Sync scope per actor (3-S2). | The architecture constrains identity and ordering. Per-actor sync sessions to prevent watermark corruption is a known protocol improvement — implementation-grade because it doesn't change stored events or the sync unit. |
| IG-15 | Flag creation location follow-up | See SG-1 for the architectural boundary. Adding device-side flags is additive, doesn't change event structure. | If device-side flag creation is later added, it produces the same `ConflictDetected` events. The architecture doesn't change — a new location for an existing operation. |

### 3.3 Outside

Items explicitly not part of this architecture. Named so they don't leak in.

| # | Item | Why outside | Interface point (if any) |
|---|------|-------------|--------------------------|
| O-1 | Aggregation model, configuration surface, deployer experience | See F2b. No ADR constrains what aggregation looks like to deployers. Multiple valid models (dashboards, BI, SQL, deployer-configured views). | Input surface: per-subject projection state from Projection Engine. Integrity guarantee: flagged-event-exclusion (5-S2) inherited. Write boundary: aggregation does not create events (1-ST2). |
| O-2 | Data archival/retention | No ADR addresses this. Events are append-only (1-S1) and never deleted, which is the storage invariant. Whether archived events are tiered to cold storage, compressed, or age-limited for device sync is operational policy. | The append-only invariant (1-S1) applies to the Event Store. "Never deleted" is the architectural position. Archival policies that don't delete (tier, compress, exclude from sync after a time boundary) are operational. Archival policies that DO delete violate 1-S1 and would require an architectural amendment. |
| O-3 | Reporting/aggregation model | See O-1. The aggregation model is outside. Reporting that consumes aggregation is even further outside. | Same as O-1. |
| O-4 | Assessment visibility to assessed worker | Sync filter configuration per deployment. No architectural constraint involved — scope resolver (3-S2) determines what syncs, and whether a worker sees assessments about themselves is a scope-filter parameter. | Scope Resolver configuration (L0). No new mechanism needed. |
| O-5 | Subject-based scope for case management | ADR-003 WTNDD explicitly deferred this as "new assignment configuration, not structural change." It's a scope type composition question answered within the existing `subject_list` scope type (4-S7). | Scope Resolver's `subject_list` scope type. Case assignment is an `AssignmentCreated` event with `subject_list` scope containing the case's subject_ref. No new scope type needed. |
| O-6 | Auditor access (cross-hierarchy, read-only) | ADR-003 WTNDD: "New role type + query-based scope — assignment configuration, not structural." An auditor is an actor with a broader scope (potentially full hierarchy) and a restricted action set (read-only). | Scope Resolver configuration (L0): broad scope assignment. Role-action tables (4-S14): read-only action set. No new mechanism needed. |
| O-7 | Grace period for expired temporal authority | Already resolved strategically by 3-S9: watermark-based auto-resolution replaces time-window grace periods. No further classification needed. | N/A — resolved. |
| O-8 | Pending match pattern generality | Accept-always (2-S14) supports pending matches implicitly. Whether pending match becomes a named Pattern Registry pattern is a pattern inventory question (IG-11). | If needed, it's a new pattern in the Pattern Registry — a platform evolution via the existing governance model (5-S5). |

---

## 4. Unified Flag Catalog

Derived from the harvest (doc 24 §2 E5) and findings F1, F4. Consolidates all flag categories across ADRs 2, 3, 4, and 5 with normalized naming and resolvability classification.

| # | Category | Source ADR | Resolvability | Derivation |
|---|----------|-----------|---------------|------------|
| 1 | `identity_conflict` | ADR-2 | manual_only | Potential duplicate subjects. Merge decision requires human judgment. |
| 2 | `stale_reference` | ADR-2 | auto_eligible | Entity updated after event creation. Self-correcting in most cases. |
| 3 | `concurrent_state_change` | ADR-2 | manual_only | Two actors changed same subject concurrently. Domain judgment needed. |
| 4 | `scope_violation` | ADR-3 | manual_only | Potential unauthorized access. Formerly `scope_stale`. Security-critical. |
| 5 | `role_stale` | ADR-3 | manual_only | Actor's role changed. Capability-restricted actions may be affected. |
| 6 | `temporal_authority_expired` | ADR-3 | auto_eligible | Assignment ended, worker didn't know. Timing overlap — analogous to `stale_reference`. |
| 7 | `domain_uniqueness_violation` | ADR-4 | manual_only | Business rule violation. Context-dependent resolution. |
| 8 | `transition_violation` | ADR-5 | auto_eligible | Invalid state transition. Usually offline timing overlap. |
| 9 | *(reserved)* | — | — | Space for future categories. The catalog is append-only per the accept-and-flag pattern. |

**Naming note**: ADR-003's original names (`ScopeStaleFlag`, `RoleStaleFlag`, `TemporalAuthorityExpiredFlag`) are normalized to `snake_case` per the convention established by ADR-002. The `scope_stale` → `scope_violation` rename reflects that ADR-003 S7 treats this as a security concern (blocking by default), aligning it with the severity implied by "violation" over "stale." ADR-003 S9's detailed text confirms: scope changes may represent unauthorized access, not just timing staleness.

**Resolvability note**: ADR-005 S3's table covered categories 1–3, 7–8. Categories 4–6 are derived here from ADR-003 S7 and S9 text. Category 5 (`role_stale`) is classified `manual_only` because ADR-003 S7 specifies it as blocking for capability-restricted actions — auto-resolution could silently accept actions the actor was no longer authorized to perform.

---

## 5. Complete Boundary Table

Every concern from the harvest, findings, and inventory open questions — classified once.

| Concern | Category | Trace | §ref |
|---------|----------|-------|------|
| Flag naming convention | Spec-grade | F1 → SG-2 | §2 F1 |
| Reporting/aggregation interface | Spec-grade | F2a → SG-4 | §2 F2a |
| Reporting/aggregation model | Outside | F2b → O-1 | §2 F2b |
| Sync contract | Spec-grade | F3 → SG-3 | §2 F3 |
| Auth flag resolvability | Spec-grade | F4 → SG-2 | §2 F4 |
| Flag creation location | Spec-grade | SG-1 | §3.1 |
| Unified flag catalog | Spec-grade | SG-2 | §4 |
| Sensitive-subject classification | Spec-grade | SG-5 | §3.1 |
| Projection rebuild strategy | Impl-grade | IG-1 | §3.2 |
| Projection merge across versions | Impl-grade | IG-2 | §3.2 |
| Batch resolution / flag grouping | Impl-grade | IG-3 | §3.2 |
| Sync pagination / priority / bandwidth | Impl-grade | IG-4 | §3.2 |
| Source-chain traversal depth | Impl-grade | IG-5 | §3.2 |
| `context.*` caching on-device | Impl-grade | IG-6 | §3.2 |
| Breaking change migration tooling | Impl-grade | IG-7 | §3.2 |
| Configuration authoring format | Impl-grade | IG-8 | §3.2 |
| Deploy-time validator UX | Impl-grade | IG-9 | §3.2 |
| Pattern skeleton formal schemas | Impl-grade | IG-10 | §3.2 |
| Pattern inventory (which ship) | Impl-grade | IG-11 | §3.2 |
| Auto-resolution authoring UX | Impl-grade | IG-12 | §3.2 |
| Auto-resolution specific policies | Impl-grade | IG-13 | §3.2 |
| Device sharing | Impl-grade | IG-14 | §3.2 |
| Data archival/retention | Outside | O-2 | §3.3 |
| Assessment visibility | Outside | O-4 | §3.3 |
| Subject-based scope (case mgmt) | Outside | O-5 | §3.3 |
| Auditor access | Outside | O-6 | §3.3 |
| Grace period for expired authority | Outside (resolved) | O-7 | §3.3 |
| Pending match pattern generality | Outside | O-8 | §3.3 |
| Expression language grammar/syntax | Impl-grade | 4-S11 constraints decided | Framework §4 |
| Aggregation deployer experience | Outside | O-1, O-3 | §3.3 |

---

## 6. Summary Counts

| Category | Count | What it means for the Architecture Description |
|----------|------:|------------------------------------------------|
| Spec-grade | 5 | Must be consolidated into the description. Derived from decided positions — no new decisions. |
| Impl-grade | 15 | Architecture Description states the constraint boundary for each. Does not fill in the implementation. |
| Outside | 8 | Named and excluded. Interface points declared where relevant. |
| Resolved | 1 | Grace period — already resolved by 3-S9. No further action. |
| **Total** | **29** | 25 harvest items + 4 harvest findings (some findings created new items; count net of overlaps) |

---

## 7. Observations for Step 3

### 7.1 The Architecture Description gains 3 cross-cutting concerns

From the original 5 in the inventory (doc 22 §5), this step adds or refines:

1. **Sync contract** (new — F3 resolution). Not a primitive, not a component. A set of 8 guarantees traced to 4 primitives.
2. **Unified flag catalog** (refined — was spread across 4 ADRs). 9 categories with normalized names and resolvability classification.
3. **Aggregation interface** (new — F2a resolution). Input surface, integrity inheritance, write boundary.

The full cross-cutting concern set for the Architecture Description:
1. Event envelope (11 fields, universal contract)
2. Accept-and-flag lifecycle
3. Detect-before-act pipeline
4. Four-layer configuration gradient
5. Configuration delivery pipeline
6. Sync contract (guarantees only)
7. Unified flag catalog
8. Aggregation interface declaration

### 7.2 No items require exploration

Every spec-grade item is derivable from existing decided positions. No gaps require new exploration sessions. This was the best possible outcome from the boundary mapping — it confirms that the architecture phase is genuinely complete and consolidation can proceed without detours.

### 7.3 Implementation-grade items cluster around tooling/UX

12 of 15 impl-grade items are tooling, UX, or format concerns (authoring format, validator UX, pattern schemas, migration tooling, etc.). The remaining 3 are performance/optimization (projection rebuild, sync pagination, source-chain depth). This clustering confirms the architecture's altitude is correct — it decided the structural WHAT and left the operational HOW open.

---

## 8. Step 2 Completion Checklist

- [x] Every item from harvest §3.2 (25 items) classified into exactly one category
- [x] Every harvest finding (F1–F4) resolved with classification and derivation logic
- [x] Every item from the consolidation framework's §4 Step 2 concern list classified
- [x] Unified flag catalog produced with normalized naming and resolvability (§4)
- [x] Complete boundary table produced (§5) — zero items remain "deferred without category"
- [x] No items require new exploration
- [x] Observations surfaced for Step 3 (§7)

**Step 2 is complete. 5 spec-grade items to consolidate. 15 impl-grade boundaries to state. 8 outside items to declare. 0 explorations needed.**
