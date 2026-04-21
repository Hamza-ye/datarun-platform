# Behavioral View — Inter-Primitive Contracts

> 21 guarantees that define how the platform's primitives interact. Every contract traces to decided architectural positions. No contract is invented. Contracts describe guarantees (invariants), not behavior (how the guarantee is fulfilled).

---

## 1. Contract Table

21 contracts across 11 primitives. Each states what the provider promises, not how it delivers.

**Classification**: **D** = Decided (directly stated in an ADR). **SG** = Specification-grade (derived from decided positions; derivation in §2).

### Event Store → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C1 | ALL | Every persisted event is immutable, carries a complete 11-field envelope, and is self-describing via `type` and `shape_ref`. Events are never modified or deleted. | [1-S1], [1-S5], [4-S1], [4-S3], [E1] | D |
| C2 | PE | The complete event stream for any subject is available for projection derivation. Projections are always rebuildable from the event stream. | [1-S2], [1-ST3] | D |
| C3 | CD | Every incoming event is persisted regardless of state staleness or anomaly. No event is rejected before entering the store. | [2-S14], [1-S1] | D |

### Projection Engine → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C4 | CD | Per-subject projected state — including alias-resolved identities, authority context from assignment timeline, and current workflow state — is available for conflict evaluation. | [1-S2], [2-S13], [3-S3], [5-S4] | SG |
| C5 | EE | Entity projection values and 7 `context.*` properties are pre-resolved and available for expression evaluation. Form context: `payload.*`, `entity.*`, `context.*`. Values are static during form fill. | [4-S11], [5-S8] | D |
| C6 | SR | Authority context — derived from the assignment event timeline as a projection — is available for scope-containment evaluation. No separate authority data store exists. | [3-S3] | D |

### Identity Resolver → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C7 | PE | Alias table provides eager transitive closure with single-hop lookup. Every `retired_id` maps to exactly one `surviving_id`. Lineage graph is acyclic by construction. | [2-S6], [2-S9] | D |

### Conflict Detector → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C8 | PE | Every detected anomaly produces a `ConflictDetected` event identifying the source event, its flag category (from the 9-category unified catalog), and exactly one designated resolver. Flagged events are deterministically identifiable for state exclusion. | [2-S11], [2-S14], [5-S2], [E3], [E5] | D |
| C9 | TE | No event carrying an unresolved flag triggers policy execution. Each flag carries a resolvability classification (`auto_eligible` or `manual_only`), available for auto-resolution policy evaluation. | [2-S12], [3-S7], [5-S3], [5-S9] | D |

### Scope Resolver → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C10 | sync | Sync payload is scope-filtered. Device receives exactly the data its actor is authorized to act on — no more, no less. Scope-containment evaluated against original `subject_ref` (alias-respects-original-scope). | [3-S1], [3-S2], [3-S4] | D |

### Shape Registry → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C11 | PE | Shape schemas for all versions are available. All versions remain valid forever. Projection logic can route on `shape_ref`. | [4-S1], [4-S10] | D |
| C12 | EE | Typed field definitions (names, types, constraints) are available for expression evaluation. Expressions reference fields as defined in shapes. | [4-S11], [4-S1] | SG |
| C13 | DtV | Shape field counts, types, and uniqueness constraints are available for budget enforcement and composition rule validation. | [4-S13], [4-S1] | SG |
| C14 | CP | All shape versions (including deprecated) are available for inclusion in configuration packages. Devices may encounter events from any historical version. | [4-S10], [4-S6] | SG |

### Expression Evaluator → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C17 | TE | Boolean condition evaluation is available for trigger conditions in trigger context (`event.*` scope). Conditions are bounded (3 predicates max). | [4-S11], [4-S13] | SG |

### Trigger Engine → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C21 | ES | Each trigger firing produces exactly one output event with system actor identity (`system:{source_type}/{source_id}`). Output type differs from input type. Event enters the normal pipeline (detection, projection, sync). | [4-S4], [4-S12] | D |

### Deploy-time Validator → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C19 | CP | Only configuration packages that pass all budget checks and composition rules reach the Config Packager. Rejection is binary — accept or reject with specific violations listed. | [4-S13], [5-S6] | SG |

### Config Packager → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C20 | sync | Configuration is delivered atomically at sync. At most 2 configuration versions coexist on-device. A device never operates with a shape from version N and a trigger from version N+1. | [4-S6] | D |

### Pattern Registry → consumers

| # | Consumer | Guarantee | Source | Cl. |
|---|----------|-----------|--------|-----|
| C15 | PE | Pattern-defined state machine skeletons (named states, valid transitions, auto-maintained projections, parameterization) are available for workflow state derivation. | [5-S4], [5-S5] | D |
| C16 | CD | Pattern-defined valid transitions are available for evaluating incoming events and creating `transition_violation` flags. | [5-S1], [5-S5] | D |
| C18 | DtV | 5 pattern composition rules and pattern structural constraints are available for deploy-time enforcement. | [5-S6] | D |

---

## 2. Specification-Grade Derivations

6 contracts are classified as specification-grade. Each is a necessary consequence of decided positions — not an invention. The derivations:

**C4 (PE → CD)**: No ADR says "PE provides state to CD." But [2-S13] requires CD to use raw references with alias resolution in projection, [3-S3] puts authority in projection, [5-S4] puts workflow state in projection, and [1-S2] makes projection the only source of current state. Four independent positions each require a PE → CD flow.

**C12 (ShR → EE)**: [4-S11] specifies expressions reference `payload.*` fields. [4-S1] specifies payloads are validated against `shape_ref`. For the Expression Evaluator to resolve field references, it must access field definitions from the Shape Registry.

**C13 (ShR → DtV)**: [4-S13] establishes 60 fields/shape budget. For the Deploy-time Validator to enforce field counts, it must access shape definitions from the Shape Registry.

**C14 (ShR → CP)**: [4-S10] says all shape versions remain valid forever. [4-S6] says configuration is delivered atomically. For devices to handle multi-version event streams, the Config Packager must include shape definitions from all versions.

**C17 (EE → TE)**: [4-S12] specifies triggers have conditions. [4-S11] specifies one expression language. For the Trigger Engine to evaluate trigger conditions, it must use the Expression Evaluator.

**C19 (DtV → CP)**: [4-S13] establishes deploy-time enforcement. [4-S6] establishes atomic delivery. For the Config Packager to deliver only valid configuration, it must receive only validated packages from the Deploy-time Validator.

---

## 3. Circular Dependencies

Two circular dependency chains exist. Neither is a deadlock — both are sequential pipelines.

### PE ↔ CD

- C4: PE → CD (projected state for detection)
- C8: CD → PE (flagged event identification for state exclusion)

**Resolution**: These operate at different times. PE provides its *existing* state to CD when a new event arrives. CD then produces flag events that update PE's state for *future* derivation cycles. PE derives state from events [C2]; CD's flag events are just more events PE processes subsequently.

### TE → ES → CD → TE

- C21: TE → ES (trigger output events)
- C3: ES → CD (all events available for detection)
- C9: CD → TE (flagged events gated from policy execution)

**Resolution**: Trigger outputs enter the normal pipeline. If a trigger-generated event gets flagged, it does NOT trigger further policies (C9). Reinforced by three guards: detect-before-act [2-S12], DAG max path 2 [4-S12], output type ≠ input type [4-S12].

---

## 4. Dependency Structure

### Provider distribution

| Provider | Contracts out | Consumers |
|----------|:------------:|-----------|
| Event Store | 3 | ALL, PE, CD |
| Projection Engine | 3 | CD, EE, SR |
| Identity Resolver | 1 | PE |
| Conflict Detector | 2 | PE, TE |
| Scope Resolver | 1 | sync |
| Shape Registry | 4 | PE, EE, DtV, CP |
| Expression Evaluator | 1 | TE |
| Trigger Engine | 1 | ES |
| Deploy-time Validator | 1 | CP |
| Config Packager | 1 | sync |
| Pattern Registry | 3 | PE, CD, DtV |

### Architectural observations

**Conflict Detector is the chokepoint.** Highest incoming dependency count: requires projected state from PE (C4), transition rules from PR (C16), and events from ES (C3). Provides guarantees to both PE (C8) and TE (C9). Every anomaly type routes through this single pipeline. This is by design [E3] — not a decomposition signal.

**Projection Engine has the widest consumer surface.** Provides state to CD (C4), EE (C5), and SR (C6), plus the aggregation interface for outside concerns. Any change to how PE derives state ripples through 3 contracts + 1 interface.

**Shape Registry is the configuration hub.** Most outgoing contracts (4). Feeds PE, EE, DtV, and CP. All 4 consumers rely on "all versions valid forever" [4-S10]. This justifies the deploy-time validation gate (C19) — DtV prevents invalid shapes from entering the system.

**Spec-grade contracts cluster around configuration flow.** 5 of 6 spec-grade contracts involve the configuration pipeline (ShR→EE, ShR→DtV, ShR→CP, EE→TE, DtV→CP). The ADRs decided WHAT each primitive constrains; the inter-primitive data flows for configuration delivery were implied but never explicitly committed.

**Two sync-output contracts (C10, C20) and no sync primitive.** Both the Scope Resolver and Config Packager provide guarantees to "sync" — not to another primitive. Sync is not a primitive because it enforces no invariant of its own. The sync contract is fully decomposable into guarantees from ES, SR, CD, and CP — see [cross-cutting.md § Sync Contract](cross-cutting.md#6-sync-contract).

---

## 5. Interface Declarations

Two outside concerns touch the architecture boundary. These are not contracts — no primitive makes a guarantee to an outside concern — but the description declares where outside concerns connect.

### Aggregation Interface

**Outside concern**: Cross-subject aggregation (dashboards, BI, reports).

| Constraint on aggregation | Source |
|--------------------------|--------|
| Input is per-subject projection state | [C2], [C4] |
| Flagged-event-exclusion inherited | [C8], [5-S2] |
| Does not write to Event Store | [1-ST2] |

### Command Validator Interface

**Advisory component** (not a primitive). Depends on two primitives:

| Dependency | From | What it uses |
|-----------|------|-------------|
| Subject's projected state | PE via [C2] | Current workflow state for advisory warnings |
| Pattern definitions | PR via [C15] | Valid transitions for pre-submission checks |

Without the Command Validator, the system works identically — the Conflict Detector catches the same violations as flags.
