# Boundary View — Altitude Classifications

> What the architecture decides, what it leaves to specification, what it leaves to implementation, and what it explicitly excludes. 29 items classified. Zero items remain "deferred without category."

---

## 1. Classification Framework

Every concern that touches the architecture is classified into exactly one category:

| Category | Meaning | Treatment in this description |
|----------|---------|-------------------------------|
| **Decided** | An ADR committed this position. | Stated in [primitives.md](primitives.md) and [contracts.md](contracts.md). Traced to source. |
| **Specification-grade** | Not decided by ADR, but must be consolidated before implementation. The existing material contains enough to derive the answer. | Derivation stated below. No new decisions — all derived from decided positions. |
| **Implementation-grade** | The architecture intentionally left this open. Multiple valid implementations exist within the decided constraints. | Constraint boundary stated. Implementation choices NOT prescribed. |
| **Outside** | Explicitly not part of this architecture. Named so it doesn't leak in. | Exclusion stated. Interface points declared where relevant. |

---

## 2. Specification-Grade Items

5 items that must be consolidated into the description. Each is derived from decided positions — no new decisions are taken.

### SG-1. Flag Creation Location

**Question**: Are flags created server-only or also on-device?

**Derivation**: Detect-before-act [2-S12] requires flags to gate policy execution. Policies execute server-only [4-S5]. Devices have no flag state for other subjects' events.

**Position**: Flags are created server-side during sync processing. Device-side flag creation is an additive platform evolution — the architecture neither requires nor prohibits it. The detect-before-act guarantee applies at the point where policies execute, which is server-side.

### SG-2. Unified Flag Catalog

**Question**: What is the complete, consistently-named catalog of flag categories?

**Derivation**: ADRs 2, 3, 4, and 5 each introduced flag categories with inconsistent naming. The majority pattern (`snake_case`) established by ADR-002 is authoritative.

**Position**: 9 flag categories with normalized names and resolvability classification. Full catalog in [cross-cutting.md § Unified Flag Catalog](cross-cutting.md#7-unified-flag-catalog).

### SG-3. Sync Contract

**Question**: How are the sync guarantees — spread across 4 primitives — presented as a coherent whole?

**Derivation**: Each guarantee traces to a decided position and an existing contract. No primitive owns the sync contract; it decomposes into guarantees from ES, SR, CD, and CP.

**Position**: 8 sync guarantees presented as a cross-cutting concern. Full detail in [cross-cutting.md § Sync Contract](cross-cutting.md#6-sync-contract).

### SG-4. Aggregation Interface

**Question**: Where does aggregation connect to the decided architecture?

**Derivation**: Aggregation reads from Projection Engine outputs (per-subject state) [C2]. Flagged events are excluded from state derivation [5-S2], so aggregation based on projection state automatically inherits this exclusion. Aggregation does not write to the Event Store [1-ST2].

**Position**: Aggregation is a server-side projection capability. Input = per-subject projection state. Inherits flagged-event-exclusion. Does not create events. Its configuration surface is outside the architecture. Full detail in [cross-cutting.md § Aggregation Interface](cross-cutting.md#8-aggregation-interface).

### SG-5. Sensitive-Subject Classification

**Question**: Is subject-level sensitivity architecturally distinct from shape/activity sensitivity?

**Derivation**: [4-S8] decides shape/activity-level sensitivity (3 levels: standard, elevated, restricted). Subject-level sensitivity is not architecturally distinguished — events about a sensitive subject are sensitive because they use a sensitive shape or belong to a sensitive activity.

**Position**: If subject-level sensitivity is needed (a specific person is sensitive regardless of what data is collected), it would require a new scope-filtering dimension — a platform evolution [3-S2], not a configuration change. The scope-filtering mechanism is the extension surface.

---

## 3. Implementation-Grade Items

15 items where the architecture decided the constraint boundary and intentionally left the implementation open. Multiple valid approaches exist within the constraints.

| # | Item | Constraint boundary (decided) | Why implementation is open |
|---|------|-------------------------------|---------------------------|
| IG-1 | Projection rebuild strategy | Projections are rebuildable from events [1-S2]. Escape hatch B→C available. | Full replay, incremental, or materialized views — all valid. Performance trade-off. |
| IG-2 | Projection merge across schema versions | Events are self-describing via `shape_ref` [4-S1]. All versions valid forever [4-S10]. | How multi-version streams compose into a single per-subject projection is device/server logic. |
| IG-3 | Batch resolution and flag grouping | Each resolution is its own event with designated resolver [2-S11]. | Grouping in review UI and batch-creating individual resolution events is read-model and UX. |
| IG-4 | Sync pagination, priority, bandwidth | Sync unit = event [1-S4]. Scope-filtered [3-S2]. Idempotent [1-S4]. | Whether sync sends recent events first, paginates, or prioritizes is protocol optimization. |
| IG-5 | Source-chain traversal depth | Source-only flagging with traversal [5-S7]. Trigger chains bounded by DAG max path 2 [4-S12]. | Organic chains have no architectural limit. Practical depth limit is a performance trade-off. |
| IG-6 | `context.*` property caching | 7 properties, pre-resolved at form-open [5-S8]. Static during fill. | How values are cached between form opens is device-side optimization. |
| IG-7 | Breaking change migration tooling | Deprecation-only default. Breaking changes explicit and exceptional [4-S10]. | Migration scripts, version comparison, deployer workflow — all tooling. |
| IG-8 | Configuration authoring format | Four-layer gradient [4-S9]. Shape semantics [4-S10]. Expression constraints [4-S11]. | Whether deployers write YAML, use a visual builder, or use a constrained DSL is tooling. |
| IG-9 | Deploy-time validator UX | Hard budgets [4-S13]. Binary accept/reject. | How violations are reported to the deployer is UX. |
| IG-10 | Pattern skeleton formal schemas | Patterns provide: roles, structural types, state machine skeleton, auto-maintained projections, parameterization [5-S5]. | Whether patterns are defined in YAML, JSON, or code is format. |
| IG-11 | Pattern inventory (which ship) | Platform-fixed, closed vocabulary [5-S5]. 4 existence proofs validated. | Which patterns ship is a platform development decision, not architecture. |
| IG-12 | Auto-resolution policy authoring UX | Mechanism decided: watches `auto_eligible` flags + enabling events [5-S9]. | How deployers author policies is tooling. |
| IG-13 | Auto-resolution specific policies | `auto_eligible` flag types decided [5-S3]. Resolution mechanism decided [5-S9]. | Which specific policies ship by default is platform decision, not architecture. |
| IG-14 | Device sharing (multiple actors) | `device_id` is hardware-bound [2-S5]. `device_seq` scoped to `device_id` [2-S1]. Sync scope per actor [3-S2]. | Per-actor sync sessions to prevent watermark corruption is a protocol improvement. Does not change stored events. |
| IG-15 | Expression language grammar/syntax | Operators + field references, zero functions [4-S11]. 3-predicate limit [4-S13]. | Grammar definition is implementation. The architecture decided the ceiling, not the syntax. |

---

## 4. Outside Items

8 items explicitly not part of this architecture. Named so they don't leak in.

| # | Item | Why outside | Interface point |
|---|------|------------|-----------------|
| O-1 | Aggregation model and deployer experience | No ADR constrains what aggregation looks like to deployers. Multiple valid models (dashboards, BI, deployer-configured views). | Input: per-subject projection state. Integrity: flagged-event-exclusion [5-S2]. Write boundary: no event creation [1-ST2]. |
| O-2 | Data archival/retention | No ADR addresses this. Append-only [1-S1] means events are never deleted — that's the boundary. | Archival policies that tier/compress without deleting are operational. Policies that delete violate [1-S1] and would require an architectural amendment. |
| O-3 | Reporting | Reporting consumes aggregation output. No direct touch point with architecture primitives. | Subsumed by O-1's aggregation interface. |
| O-4 | Assessment visibility to assessed worker | Scope Resolver configuration (L0 parameter). Whether a worker sees assessments about themselves is a scope-filter parameter. | No new mechanism — existing scope configuration. |
| O-5 | Subject-based scope for case management | Existing `subject_list` scope type [4-S7]. Case assignment is an `AssignmentCreated` event with `subject_list` scope. | No new scope type — existing mechanism. |
| O-6 | Auditor access (cross-hierarchy, read-only) | Broad scope assignment + read-only action set via role-action tables [4-S14]. | No new mechanism — existing assignment-based access. |
| O-7 | Grace period for expired temporal authority | Resolved by watermark-based auto-resolution [3-S9]. Not outside — resolved. | N/A |
| O-8 | Pending match pattern generality | Accept-always [2-S14] supports pending matches implicitly. If first-class support needed, a new Pattern Registry pattern — platform evolution via existing governance [5-S5]. | Extension surface: Pattern Registry. |

---

## 5. Decided Positions — Complete Trace

61 decided positions (52 explicit sub-decisions + 4 documented strategies + 5 emergent). Every position appears in this Architecture Description:

### By ADR

| ADR | Positions | Where they appear |
|-----|:---------:|-------------------|
| ADR-001 | 5 sub-decisions + 3 strategies + 1 escape hatch | [primitives.md §1–2](primitives.md#1-event-store), [cross-cutting.md §1](cross-cutting.md#1-event-envelope) |
| ADR-002 | 14 sub-decisions | [primitives.md §3–4](primitives.md#3-identity-resolver), [cross-cutting.md §2–3](cross-cutting.md#2-accept-and-flag-lifecycle) |
| ADR-003 | 10 sub-decisions | [primitives.md §5](primitives.md#5-scope-resolver), [cross-cutting.md §6–7](cross-cutting.md#6-sync-contract) |
| ADR-004 | 14 sub-decisions | [primitives.md §6–10](primitives.md#6-shape-registry), [cross-cutting.md §4–5](cross-cutting.md#4-four-layer-configuration-gradient) |
| ADR-005 | 9 sub-decisions | [primitives.md §4, §11](primitives.md#11-pattern-registry), [cross-cutting.md §7](cross-cutting.md#7-unified-flag-catalog) |
| Emergent | 5 positions | [E1]–[E5] in primitives and cross-cutting sections |

### By classification

| Classification | Count | What it means |
|---------------|:-----:|---------------|
| Structural constraint | 22 | Change requires data migration across offline devices |
| Strategy-protecting | 14 | Guards a structural invariant; implementation can evolve |
| Initial strategy | 16 | Evolvable without affecting stored events |
| Documented strategy | 4 | ADR-001 strategies protecting the escape hatch |
| Emergent | 5 | Arise from the combination of multiple decided positions |
| **Total** | **61** | |

---

## 6. Escape Hatches

Escape hatches are pre-planned evolution paths — architectural decisions that include explicit conditions for revision. They are NOT deferred decisions; they are decided positions with stated triggers.

| Primitive | Escape hatch | Trigger | What changes |
|-----------|-------------|---------|--------------|
| Projection Engine | B→C: add application-maintained views alongside event store | Rebuild >200ms/subject on low-end device | Adds materialized views. Viable only if event log is gap-free [1-ST2]. |
| Scope Resolver | Add `authority_context` to envelope | Authority reconstruction >50ms/event at scale | Adds an envelope field. First use of the extensibility clause [1-S5]. |
| Identity Resolver | Actor-sequence counter for cross-device ordering | >10% of conflict flags from same-actor cross-device ordering | Add per-actor sequence within existing envelope. |
| Shape Registry | Revisit deprecation-only evolution | >3 deployments hit field budget from deprecated accumulation | May require a migration mechanism for deprecated field cleanup. |
| Expression Evaluator | Revisit L3 expressiveness ceiling | >3 deployments need code for "should be configurable" | May add limited function vocabulary within existing budget constraints. |
| Pattern Registry | Expand pattern inventory | >2 deployments request custom state machines matching no pattern | Adds new platform-fixed patterns. Does not change pattern governance model. |

---

## 7. Completion Verification

Per the consolidation framework (doc 23 §4 Step 5):

- [x] Every decided position from the harvest appears in the description, traced to its ADR (§5: 61 positions)
- [x] Every contract from Step 3 appears, stated as a guarantee (contracts.md: 21 contracts)
- [x] Every boundary classification from Step 2 appears, declared (§2–§4: 5 SG + 15 IG + 8 outside + 1 resolved = 29)
- [x] Every behavioral pattern and scenario decomposes into contracted interactions (verified in doc 27: 12/12 patterns, 21/21 scenarios)
- [x] Zero unresolved gaps remain (doc 27 §6: empty register)
- [x] The description is self-contained — evaluable without reading ADRs
