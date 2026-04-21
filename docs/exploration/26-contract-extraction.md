# 26 — Contract Extraction

> Step 3 of the Architecture Consolidation (framework: [doc 23](23-consolidation-framework.md)).
> Extracts every inter-primitive guarantee from the decision harvest ([doc 24](24-decision-harvest.md)) and boundary map ([doc 25](25-boundary-mapping.md)). Each contract traces to decided positions. No contract is invented.

---

## 1. Method

**Input**: Decision harvest (doc 24 — 61 positions), boundary map (doc 25 — 5 spec-grade items, 8 cross-cutting concerns), primitives inventory (doc 22 — 11 primitives with inputs/outputs).

**Process**: For every pair of primitives that interact (identified from doc 22 §4 inputs/outputs), extract the inter-primitive guarantees. Each contract states what the provider promises as an invariant — not how the guarantee is fulfilled.

**Format per contract**:

| Field | Content |
|-------|---------|
| Provider | The primitive making the guarantee |
| Consumer | The primitive relying on the guarantee |
| Guarantee | What the provider promises, stated as an invariant |
| Requires | What the provider needs from others to fulfill the guarantee |
| Source | Decided position(s) from the harvest that authorize this contract |
| Classification | Decided (directly stated in an ADR) or spec-grade (derived from decided positions) |

**Primitive abbreviations**: ES = Event Store, PE = Projection Engine, IR = Identity Resolver, CD = Conflict Detector, SR = Scope Resolver, ShR = Shape Registry, EE = Expression Evaluator, TE = Trigger Engine, DtV = Deploy-time Validator, CP = Config Packager, PR = Pattern Registry.

---

## 2. Contract Table

21 contracts across 11 primitives. Organized by provider.

### 2.1 Event Store (provider) — 3 contracts

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C1 | ALL | Every persisted event is immutable, carries a complete 11-field envelope, and is self-describing via `type` and `shape_ref`. Events are never modified or deleted. | Nothing (foundational) | 1-S1, 1-S5, 4-S1, 4-S3, E1 | Decided |
| C2 | PE | The complete event stream for any subject is available for projection derivation. Projections are always rebuildable from the event stream. | Nothing (foundational) | 1-S2, 1-ST3 | Decided |
| C3 | CD | Every incoming event is persisted regardless of state staleness or anomaly. No event is rejected before entering the store. | Nothing (foundational) | 2-S14, 1-S1 | Decided |

### 2.2 Projection Engine (provider) — 3 contracts

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C4 | CD | Per-subject projected state — including alias-resolved identities, authority context from assignment timeline, and current workflow state — is available for conflict evaluation. | C2 (events from ES), C7 (aliases from IR), C15 (patterns from PR), C18 (schemas from ShR) | 1-S2, 2-S13, 3-S3, 5-S4 | Spec-grade |
| C5 | EE | Entity projection values and 7 `context.*` properties are pre-resolved and available for expression evaluation. Form context: `payload.*`, `entity.*`, `context.*`. Values are static during form fill. | C2 (events from ES), C15 (patterns from PR) | 4-S11, 5-S8 | Decided |
| C6 | SR | Authority context — derived from the assignment event timeline as a projection — is available for scope-containment evaluation. No separate authority data store exists. | C2 (events from ES) | 3-S3 | Decided |

### 2.3 Identity Resolver (provider) — 1 contract

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C7 | PE | Alias table provides eager transitive closure with single-hop lookup. Every `retired_id` maps to exactly one `surviving_id`. The lineage graph is acyclic by construction. | C1 (ES persists SubjectsMerged / SubjectSplit events) | 2-S6, 2-S9 | Decided |

### 2.4 Conflict Detector (provider) — 2 contracts

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C8 | PE | Every detected anomaly produces a `ConflictDetected` event identifying the source event, its flag category (from the 9-category unified catalog), and exactly one designated resolver. Flagged events are deterministically identifiable for state exclusion. | C4 (projected state from PE), C16 (transition rules from PR) | 2-S11, 2-S14, 5-S2, E3, E5 | Decided |
| C9 | TE | (a) No event carrying an unresolved flag triggers policy execution. (b) Each flag carries a resolvability classification (`auto_eligible` or `manual_only`), available for auto-resolution policy evaluation. | C4 (projected state from PE), C16 (transition rules from PR) | 2-S12, 3-S7, 5-S3, 5-S9 | Decided |

### 2.5 Scope Resolver (provider) — 1 contract

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C10 | sync | Sync payload is scope-filtered. Device receives exactly the data its actor is authorized to act on — no more, no less. Scope-containment evaluated against original `subject_ref` (alias-respects-original-scope). | C6 (authority context from PE) | 3-S1, 3-S2, 3-S4 | Decided |

### 2.6 Shape Registry (provider) — 4 contracts

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C11 | PE | Shape schemas for all versions are available. All versions remain valid forever. Projection logic can route on `shape_ref`. | C19 (shapes pass DtV) | 4-S1, 4-S10 | Decided |
| C12 | EE | Typed field definitions (names, types, constraints) are available for expression evaluation. Expressions reference fields as defined in shapes. | C19 (shapes pass DtV) | 4-S11 + 4-S1 | Spec-grade |
| C13 | DtV | Shape field counts, types, and uniqueness constraints are available for budget enforcement and composition rule validation. | Nothing | 4-S13 + 4-S1 | Spec-grade |
| C14 | CP | All shape versions (including deprecated) are available for inclusion in configuration packages. Devices may encounter events from any historical version. | Nothing | 4-S10, 4-S6 | Spec-grade |

### 2.7 Expression Evaluator (provider) — 1 contract

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C17 | TE | Boolean condition evaluation is available for trigger conditions in trigger context (`event.*` scope). Conditions are bounded (3 predicates max). | C12 (field metadata from ShR), C5 (projection values from PE for form context) | 4-S11, 4-S13 | Spec-grade |

### 2.8 Trigger Engine (provider) — 1 contract

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C21 | ES | Each trigger firing produces exactly one output event with system actor identity (`system:{source_type}/{source_id}`). Output type differs from input type. Event enters the normal pipeline (detection, projection, sync). | C17 (condition evaluation from EE), C3 (ES accepts all events) | 4-S4, 4-S12 | Decided |

### 2.9 Deploy-time Validator (provider) — 1 contract

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C19 | CP | Only configuration packages that pass all budget checks and composition rules reach the Config Packager. Rejection is binary — accept or reject with specific violations listed. | C13 (shapes from ShR), C16 (composition rules from PR) | 4-S13, 5-S6 | Spec-grade |

### 2.10 Config Packager (provider) — 1 contract

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C20 | sync | Configuration is delivered atomically at sync. At most 2 configuration versions coexist on-device. A device never operates with a shape from version N and a trigger from version N+1. | C19 (validated config from DtV), C14 (all shape versions from ShR) | 4-S6 | Decided |

### 2.11 Pattern Registry (provider) — 3 contracts

| # | Consumer | Guarantee | Requires | Source | Class. |
|---|----------|-----------|----------|--------|--------|
| C15 | PE | Pattern-defined state machine skeletons (named states, valid transitions, auto-maintained projections, parameterization) are available for workflow state derivation. | Nothing (PR is platform-fixed; deployer parameterization is external input) | 5-S4, 5-S5 | Decided |
| C16 | CD | Pattern-defined valid transitions are available for evaluating incoming events and creating `transition_violation` flags. | Nothing (PR is platform-fixed) | 5-S1, 5-S5 | Decided |
| C18 | DtV | 5 pattern composition rules and pattern structural constraints are available for deploy-time enforcement. | Nothing (rules are platform-fixed) | 5-S6 | Decided |

---

## 3. Spec-Grade Contract Derivations

6 contracts are classified as specification-grade — their guarantees are necessary consequences of decided positions but are not directly stated in any ADR. Each derivation shows which positions combine to produce the guarantee.

### C4: PE → CD (Projected State for Detection)

**Why it's spec-grade**: No ADR sub-decision explicitly says "the Projection Engine provides its state to the Conflict Detector." The requirement is a logical consequence of four independent decided positions:

1. **2-S13**: Conflict detection uses raw references; alias resolution occurs only in projection, after detection. → CD consumes projection output.
2. **3-S3**: Authority context is a projection, not an envelope field. → CD's auth checks require projection-derived data.
3. **5-S4**: State machines are projection patterns. → CD's transition validation requires projection-derived workflow state.
4. **1-S2**: Current state is a projection computed from events. → The only source of "current state" is the Projection Engine.

Each position independently requires a PE → CD data flow. Together they define the scope of what PE must provide: alias-resolved identities, authority context, and workflow state.

### C12: ShR → EE (Field Metadata)

**Why it's spec-grade**: 4-S11 specifies that expressions reference `payload.*` fields. 4-S1 specifies that payloads are validated against `shape_ref`. For the Expression Evaluator to resolve field references, it must access field definitions from the Shape Registry. No ADR directly states this flow, but it's a necessary consequence of field references + typed shapes.

### C13: ShR → DtV (Shape Constraints)

**Why it's spec-grade**: 4-S13 establishes hard complexity budgets (60 fields/shape). For the Deploy-time Validator to enforce field counts, it must access shape definitions from the Shape Registry. The ADR establishes the budget and the enforcement point but does not explicitly name the data flow between them.

### C14: ShR → CP (All Versions for Delivery)

**Why it's spec-grade**: 4-S10 says all shape versions remain valid forever. 4-S6 says configuration is delivered atomically. For devices to handle multi-version event streams, the Config Packager must include shape definitions. The Architecture Description must make explicit that shape schemas flow from ShR through the delivery pipeline, including historical versions for multi-version projection support.

### C17: EE → TE (Condition Evaluation)

**Why it's spec-grade**: 4-S12 specifies that triggers have conditions (L3a: single-event condition; L3b: time-window condition). 4-S11 specifies one expression language with two contexts, one of which is trigger context (`event.*`). For the Trigger Engine to evaluate trigger conditions, it must use the Expression Evaluator. No ADR directly assigns this relationship, but it's a necessary consequence of "one language" + "triggers have conditions."

### C19: DtV → CP (Validated Configuration Only)

**Why it's spec-grade**: 4-S13 establishes hard, deploy-time enforcement of complexity budgets. 4-S6 establishes atomic configuration delivery. For the Config Packager to deliver only valid configuration, it must receive only validated packages from the Deploy-time Validator. No ADR directly states this flow. The Architecture Description must make explicit that DtV is an acceptance gate before CP.

---

## 4. Cross-Cutting Contract Sets

Three cross-cutting concerns (identified in doc 25 §7) are not individual contracts but coherent sets of contracts that span multiple primitives. The Architecture Description must present each as a unified guarantee traced to the underlying contracts.

### 4.1 Sync Contract

8 guarantees (from doc 25 F3 resolution), mapped to the contracts that provide them:

| # | Sync guarantee | Providing contract(s) | Source |
|---|---------------|----------------------|--------|
| SY-1 | Events are the sync unit. Sync transfers events the receiver hasn't seen. | C1 (ES → ALL: self-describing events) | 1-S4 |
| SY-2 | Sync is idempotent — receiving the same event twice is a no-op. | C1 (ES → ALL: immutable, unique IDs) | 1-S4 |
| SY-3 | Sync is append-only — the server never instructs deletion or modification. | C1 (ES → ALL: never modified or deleted) | 1-S4 |
| SY-4 | Sync is order-independent — events carry their own ordering metadata. | C1 (ES → ALL: self-describing via device_seq, sync_watermark) | 1-S4, 2-S1 |
| SY-5 | Sync payload is scope-filtered — device receives exactly authorized data. | C10 (SR → sync) | 3-S2 |
| SY-6 | Conflict detection runs before policy execution on synced events. | C9 (CD → TE: detect-before-act) | 2-S12 |
| SY-7 | Configuration is delivered atomically at sync. At most 2 versions coexist. | C20 (CP → sync) | 4-S6 |
| SY-8 | Merge/split/resolution events sync like any other event, filtered by scope. | C10 (SR → sync: scope-filtered), C1 (ES → ALL: all events are events) | 3-S2, 2-S10, 3-S6 |

Every sync guarantee is accounted for by an existing contract. The sync contract is a **derived cross-cutting concern**, not an additional contract or hidden subsystem.

### 4.2 Accept-and-Flag Pipeline

The full anomaly detection lifecycle touches 4 primitives via 5 contracts:

| Pipeline stage | Contract | Provider → Consumer |
|---------------|----------|---------------------|
| Event persisted regardless of anomaly | C3 | ES → CD |
| Projected state available for detection | C4 | PE → CD |
| Anomaly identified and flagged | C8 | CD → PE |
| Flagged events excluded from state derivation | C8 | CD → PE |
| Flagged events gated from policy execution | C9 | CD → TE |
| Auto-resolution watches eligible flags | C9 | CD → TE |
| Resolution event persisted | C21 | TE → ES |
| Post-resolution: state re-derived including event | C2 | ES → PE |

The pipeline is a closed loop: ES → CD (detect) → PE (exclude) → TE (auto-resolve) → ES (persist resolution) → PE (re-derive). Each stage is a contract guarantee, not a behavioral prescription.

### 4.3 Detect-Before-Act

Three levels of the ordering guarantee (from doc 22 §5.3), mapped to contracts:

| Level | What it prevents | Contract |
|-------|-----------------|----------|
| Policy execution | Flagged events don't trigger L3a/L3b | C9 (CD → TE) |
| State derivation | Flagged events don't advance state machines | C8 (CD → PE) |
| Authorization | Auth-flagged events don't trigger downstream work | C9 (CD → TE: applies to all flag types per 3-S7) |

All three levels are served by two contracts (C8 and C9). The Conflict Detector is the sole provider of the detect-before-act guarantee.

---

## 5. Circular Dependencies

Two circular dependency chains exist in the contract graph. Neither is a deadlock — both are sequential pipelines.

### 5.1 PE ↔ CD

- C4: PE → CD (projected state for detection)
- C8: CD → PE (flagged event identification for state exclusion)

**Why this isn't circular**: These contracts operate at different times. PE provides its *existing* projected state to CD when a new event arrives. CD then produces flag events that update PE's state for *future* projections. There is no contract that says "PE's state requires CD's simultaneous output" — PE derives state from events (C2), and CD's flag events are just more events that PE processes in a subsequent derivation cycle.

### 5.2 TE → ES → CD → TE

- C21: TE → ES (trigger output events)
- C3: ES → CD (all events available for detection)
- C9: CD → TE (flagged events gated from policy execution)

**Why this isn't circular**: Trigger outputs enter the normal pipeline. If a trigger-generated event gets flagged, it does NOT trigger further policies (C9). This is reinforced by three independent guards:
1. Detect-before-act (2-S12): flagged events don't trigger policies
2. DAG max path 2 (4-S12): at most 2 levels of trigger chaining
3. Output type ≠ input type (4-S12): a trigger cannot chain with itself

---

## 6. Edge Coverage Verification

Every input/output edge from doc 22 §4 must be covered by at least one contract.

### 6.1 Input edges (what each primitive receives)

| Primitive | Input (from doc 22) | Covering contract |
|-----------|---------------------|-------------------|
| ES | Events from devices (via sync) | External input (not inter-primitive) |
| ES | Events from Trigger Engine | C21 (TE → ES) |
| PE | Events from Event Store | C2 (ES → PE) |
| PE | Pattern definitions from Pattern Registry | C15 (PR → PE) |
| PE | Alias table from Identity Resolver | C7 (IR → PE) |
| PE | Assignment timeline | C2 (ES → PE: derived from assignment events) |
| PE | Shape schemas | C11 (ShR → PE) |
| IR | SubjectsMerged, SubjectSplit events | C1 (ES → ALL) |
| CD | Incoming events at sync | C3 (ES → CD) |
| CD | Projection state | C4 (PE → CD) |
| CD | Pattern definitions (transition validation) | C16 (PR → CD) |
| CD | Assignment timeline (auth validation) | C4 (PE → CD: includes authority context) |
| SR | Assignment events, scope definitions | C6 (PE → SR: authority from assignment projection) |
| SR | Subject locations | External data (deployment-specific) |
| ShR | Shape definitions from deployer | External input (not inter-primitive) |
| EE | Field values from payload, entity projection | C5 (PE → EE) |
| EE | `context.*` properties | C5 (PE → EE) |
| EE | Field metadata from shapes | C12 (ShR → EE) |
| TE | Incoming events (L3a) | C3 (ES → CD: events available) + C9 (CD → TE: only unflagged reach TE) |
| TE | Time-based scans (L3b) | Platform scheduling (not inter-primitive) |
| TE | Flag state (auto-resolution) | C9 (CD → TE) |
| TE | Trigger conditions | C17 (EE → TE) |
| DtV | Complete configuration package | C13 (ShR → DtV) + C18 (PR → DtV) |
| CP | Validated configuration package | C19 (DtV → CP) |
| CP | Shape versions for delivery | C14 (ShR → CP) |
| PR | Pattern definitions (platform-authored) | External (platform development) |
| PR | Deployer parameterization | External input (not inter-primitive) |

### 6.2 Output edges (what each primitive provides)

| Primitive | Output (from doc 22) | Covering contract |
|-----------|----------------------|-------------------|
| ES | Immutable events to PE | C2 (ES → PE) |
| ES | Immutable events to CD | C3 (ES → CD) |
| ES | Events for sync delivery | C1 (ES → ALL) + C10 (SR → sync: scope-filtered) |
| PE | Per-subject current state | C4 (PE → CD), C5 (PE → EE), C6 (PE → SR) |
| PE | Workflow state | C4 (PE → CD) |
| PE | `context.*` property values | C5 (PE → EE) |
| PE | Source-chain traversal results | C4 (PE → CD: projected state includes traversal) |
| IR | Alias table to PE | C7 (IR → PE) |
| IR | Lineage graph for audit | C7 (IR → PE: acyclicity guarantee covers lineage integrity) |
| CD | ConflictDetected events | C8 (CD → PE) |
| CD | ConflictResolved events | C8 (CD → PE: changes flag state, re-included in derivation) |
| SR | Per-actor sync payload | C10 (SR → sync) |
| SR | Authorization decisions | C10 (SR → sync: scope = access) |
| ShR | Shape schemas to device (via CP) | C14 (ShR → CP) |
| ShR | Validation rules to DtV | C13 (ShR → DtV) |
| ShR | Field metadata to EE | C12 (ShR → EE) |
| EE | Boolean condition results | C17 (EE → TE) |
| EE | Computed values (defaults) | C5 (PE → EE: input) → device-side output (not inter-primitive) |
| TE | Output events to ES | C21 (TE → ES) |
| DtV | Accept/reject decision | C19 (DtV → CP) |
| CP | Atomic config payload to device | C20 (CP → sync) |
| PR | State machine definitions to PE | C15 (PR → PE) |
| PR | Transition rules to CD | C16 (PR → CD) |
| PR | Composition rules to DtV | C18 (PR → DtV) |

**Result**: Every input/output edge from the inventory is covered by at least one contract. External inputs (deployer configuration, device events, subject locations, platform scheduling) are correctly excluded — they are not inter-primitive contracts.

---

## 7. Interface Declarations

Two outside concerns touch the architecture boundary through declared interface points (from doc 25 §3.3). These are not contracts — no primitive makes a guarantee to an outside concern — but the Architecture Description must declare where outside concerns connect.

### 7.1 Aggregation Interface

**Outside concern**: Cross-subject aggregation (dashboards, BI, reports). Classified as outside (doc 25 O-1).

**Interface point**: The Projection Engine's per-subject projected state is the input surface. Three constraints inherited from the architecture:

| Constraint | Source | What it means for aggregation |
|-----------|--------|-------------------------------|
| Input is per-subject projection state | C2 (ES → PE), C4 (PE → CD) | Aggregation reads from projections, not from events directly |
| Flagged-event-exclusion inherited | C8 (CD → PE: 5-S2) | Aggregation based on projection state automatically excludes flagged events |
| Does not write to Event Store | 1-ST2 (write-path discipline) | Aggregation is a read-only downstream consumer |

### 7.2 Command Validator Interface

**Advisory component** (not a primitive — doc 22). Depends on two primitives but enforces no invariant of its own.

| Dependency | From primitive | What it uses |
|-----------|---------------|--------------|
| Subject's projected state | PE (via C2) | Current workflow state for advisory warnings |
| Pattern definitions | PR (via C15) | Valid transitions for pre-submission checks |

Without the Command Validator, the system works identically — the Conflict Detector catches the same violations as flags. The Command Validator reduces flag volume by warning users before submission.

---

## 8. Summary Statistics

| Metric | Count |
|--------|------:|
| Total contracts | 21 |
| Decided (directly traceable to ADR sub-decisions) | 15 |
| Specification-grade (derived from decided positions) | 6 |
| Primitives as providers | 11 (every primitive provides at least 1 contract) |
| Cross-cutting contract sets | 3 (sync contract, accept-and-flag, detect-before-act) |
| Circular dependency chains | 2 (both resolved: sequential pipelines, not deadlocks) |
| Contracts per provider (min/max) | 1 / 4 (ShR has 4; IR, SR, EE, TE, DtV, CP each have 1) |
| Inventory edges covered | All (see §6) |
| Contracts lacking source trace | 0 |

### Provider distribution

| Provider | Outgoing contracts | Consumer(s) |
|----------|-------------------:|-------------|
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

---

## 9. Observations for Step 4

### 9.1 The Conflict Detector is the architectural chokepoint

CD has the highest incoming dependency count: it requires projected state from PE (C4), transition rules from PR (C16), and events from ES (C3). It provides guarantees to both PE (C8) and TE (C9). Every flag type, every detection rule, and every resolution path routes through this single primitive. This is by design (E3: accept-and-flag as universal mechanism) — not a decomposition signal.

### 9.2 The Projection Engine has the widest consumer surface

PE provides state to CD (C4), EE (C5), and SR (C6). It also provides the input surface for the outside aggregation concern (§7.1). Any change to how PE derives state ripples through 3 contracts + 1 interface. The detect-before-act extension (5-S2) to state derivation means PE's exclusion logic is load-bearing for both CD's contract (C8) and TE's contract (C9).

### 9.3 Shape Registry is the configuration hub

ShR has the most outgoing contracts (4). It feeds PE (schemas), EE (field metadata), DtV (budget data), and CP (delivery packages). All 4 consumers rely on ShR honoring the "all versions valid forever" guarantee (4-S10). A ShR failure propagates to all configuration-adjacent primitives. This justifies the deploy-time validation gate (C19) — the DtV prevents invalid shapes from entering the system.

### 9.4 Spec-grade contracts cluster around configuration flow

5 of 6 spec-grade contracts involve the configuration pipeline (ShR → EE, ShR → DtV, ShR → CP, EE → TE, DtV → CP). The remaining one (PE → CD) is the detection state flow. This clustering is expected: the ADRs decided WHAT each primitive constrains; the inter-primitive data flows for configuration delivery were implied but never explicitly committed. The Architecture Description must make these flows explicit.

### 9.5 Two sync-output contracts (C10, C20) and no sync primitive

Both the Scope Resolver (C10: scope-filtered payload) and Config Packager (C20: atomic config delivery) provide guarantees to "sync" — not to another primitive. The sync mechanism is not a primitive because it enforces no invariant of its own. The sync contract (§4.1) is fully decomposable into guarantees from ES, SR, CD, and CP. This validates doc 25 F3's classification: sync is a cross-cutting concern, not a primitive or hidden subsystem.

### 9.6 Zero contract gaps identified

Every inventory edge is covered. Every contract has a source trace. No pair of interacting primitives lacks a contract. Step 4 should verify this against behavioral patterns and scenarios, but no structural gaps are visible from the contract extraction alone.

---

## 10. Step 3 Completion Checklist

- [x] Every input/output edge from doc 22 §4 covered by at least one contract (§6)
- [x] Every contract has a source trace to decided positions from the harvest (§2)
- [x] No contract describes behavior — all state guarantees as invariants (§2)
- [x] Contracts are directional (provider → consumer) (§2)
- [x] Spec-grade contracts have explicit derivation logic (§3)
- [x] Cross-cutting concerns mapped to underlying contracts (§4)
- [x] Circular dependencies identified and justified (§5)
- [x] Interface declarations for outside concerns stated (§7)
- [x] Observations surfaced for Step 4 (§9)

**Step 3 is complete. 21 contracts extracted (15 decided, 6 spec-grade). Every inventory edge covered. Zero source-trace gaps. Ready for Step 4 (Gap Identification).**
