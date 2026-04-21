# 27 — Gap Identification

> Step 4 of the Architecture Consolidation (framework: [doc 23](23-consolidation-framework.md)).
> Cross-checks the decision harvest ([doc 24](24-decision-harvest.md)), boundary map ([doc 25](25-boundary-mapping.md)), and contract table ([doc 26](26-contract-extraction.md)) against behavioral patterns and scenarios. Produces a gap register.

---

## 1. Method

Four sub-steps per the framework (doc 23 §4 Step 4):

| Sub-step | What it checks | Input |
|----------|---------------|-------|
| 4a. Contract gaps | Interacting primitives with no decided-position coverage | Contract table (doc 26 §2), inventory edges (doc 22 §4) |
| 4b. Boundary gaps | Spec-grade items lacking decided anchors | Boundary map (doc 25 §3.1), contract derivations (doc 26 §3) |
| 4c. Coverage gaps | Patterns and scenarios against contracted interactions | 12 patterns (behavioral_patterns.md §2), 21 scenarios (scenarios/), contract table (doc 26 §2) |
| 4d. Outside interfaces | Whether outside items need declared interface points | Boundary map (doc 25 §3.3) |

---

## 2. Sub-step 4a: Contract Gaps

**Question**: Are there interacting primitive pairs with no contract?

Doc 26 §6 verified every input/output edge from the inventory (doc 22 §4) against the contract table. Result: **every edge is covered by at least one contract**. No primitive pair that interacts lacks a traced guarantee.

**Possible hidden edges**: Could two primitives interact through a path not captured in the inventory? Three candidates:

| Candidate edge | Exists? | Why / why not |
|----------------|---------|---------------|
| IR → CD (identity data for conflict detection) | No — indirect via PE | CD uses projected state (C4) which includes alias-resolved identities. IR provides the alias table to PE (C7). IR → CD is mediated by PE, not direct. |
| PR → CP (patterns in config package) | No — indirect via DtV | PR provides composition rules to DtV (C18). DtV gates what reaches CP (C19). Pattern definitions flow through the validated config package, not a direct PR → CP edge. |
| SR → CD (scope data for auth flag evaluation) | No — indirect via PE | CD evaluates authority using projected state (C4), which includes authority context derived by PE from assignment events (C6). SR does not feed CD directly. |

All three candidate hidden edges are mediated by existing contracts. No direct edge is missing.

**4a result: Zero contract gaps.**

---

## 3. Sub-step 4b: Boundary Gaps

**Question**: Are any spec-grade items from the boundary map anchored to decided positions that are too weak to derive the answer?

The 5 spec-grade items (doc 25 §3.1) and their anchoring strength:

| Item | Anchoring positions | Derivation strength | Gap? |
|------|-------------------|---------------------|------|
| SG-1: Flag creation location | 2-S12 (detect-before-act), 4-S5 (server-only triggers) | Strong — server-side follows from policy execution location. Device-side explicitly classified as additive platform evolution. | No |
| SG-2: Unified flag catalog | E5 (emergent), 2-S11, 2-S14, 3-S9, 4-S14, 5-S1, 5-S3 | Strong — every flag type traces to a specific ADR sub-decision. Resolvability derived from ADR text. Naming normalized per established convention. | No |
| SG-3: Sync contract | 1-S4, 2-S1, 2-S12, 3-S2, 4-S6 | Strong — doc 26 §4.1 maps all 8 sync guarantees to specific contracts. Every guarantee traces to a decided position. | No |
| SG-4: Aggregation interface | 1-S2, 1-ST2, 5-S2 | Strong — input surface (projection state), integrity inheritance (flagged-event-exclusion), write boundary (write-path discipline) all trace to decided positions. | No |
| SG-5: Sensitive-subject classification | 4-S8, 3-S2 | Strong — shape/activity sensitivity decided. Subject-level classified as platform evolution requiring new scope-filtering dimension. Extension surface (3-S2) identified. | No |

Additionally, 6 spec-grade contracts from doc 26 §3 were verified for derivation soundness:

| Contract | Derivation from | Sound? |
|----------|----------------|--------|
| C4: PE → CD | 2-S13, 3-S3, 5-S4, 1-S2 | Yes — 4 independent positions each require PE → CD flow |
| C12: ShR → EE | 4-S11, 4-S1 | Yes — field references + typed shapes necessitate schema access |
| C13: ShR → DtV | 4-S13, 4-S1 | Yes — budget enforcement requires shape data |
| C14: ShR → CP | 4-S10, 4-S6 | Yes — "all versions valid forever" + atomic delivery |
| C17: EE → TE | 4-S12, 4-S11 | Yes — "triggers have conditions" + "one expression language" |
| C19: DtV → CP | 4-S13, 4-S6 | Yes — hard enforcement + atomic delivery |

**4b result: Zero boundary gaps.** Every spec-grade item and every spec-grade contract has sufficient decided-position anchoring. No derivation requires assumptions beyond what the ADRs committed.

---

## 4. Sub-step 4c: Coverage Gaps

### 4c.1 Pattern Coverage

For each of the 12 behavioral patterns, identify which contracts serve it. A pattern is covered if the contracted interactions are sufficient to support the behavior described — not if they prescribe how.

| Pattern | Contracts that serve it | Coverage assessment |
|---------|------------------------|---------------------|
| **P01 — Structured Recording** | C1 (ES: immutable, self-describing events), C11 (ShR → PE: typed schemas, versioned) | **Covered.** Events carry shape_ref; shapes define field structure; events are immutable records. |
| **P02 — Subject Linkage** | C1 (ES: subject_ref in envelope), C7 (IR → PE: alias table, merge/split), C2 (ES → PE: subject-grouped projection) | **Covered.** Every event carries typed subject_ref. Identity lifecycle (merge, split, alias) handled by IR. Projection groups by subject. |
| **P03 — Temporal Rhythm** | C9 (CD → TE: detect-before-act for triggers), C21 (TE → ES: trigger output events), C17 (EE → TE: condition evaluation) | **Covered.** L3b deadline-check triggers watch for non-occurrence within time windows. System generates alert/task events when deadlines pass. |
| **P04 — Responsibility Binding** | C10 (SR → sync: scope-filtered by assignment), C6 (PE → SR: authority from assignment projection) | **Covered.** Assignment events bind actors to scope. Scope Resolver computes responsibility from assignment timeline. |
| **P05 — Hierarchical Visibility** | C10 (SR → sync: device gets authorized data), C6 (PE → SR: authority context) | **Covered.** Tiered projection location (3-S8) + scope types (geographic, subject_list, activity) determine what each hierarchy level sees. |
| **P06 — Review and Judgment** | C15 (PR → PE: pattern-defined state machines), C16 (PR → CD: transition rules), C8 (CD → PE: transition_violation flags) | **Covered.** `capture_with_review` and `multi_step_approval` patterns define review workflows. State machines track review state as projections. Invalid transitions flagged. |
| **P07 — Transfer with Acknowledgment** | C15 (PR → PE: `transfer_with_acknowledgment` pattern), C1 (ES: events for send/receive/discrepancy), C8 (CD → PE: flags for anomalies) | **Covered.** Transfer pattern defines send → receive → confirm/dispute states. Multi-hop via activity_ref cross-activity linking (5-S6 Rule 4). |
| **P08 — State Progression** | C15 (PR → PE: state machine skeletons), C16 (PR → CD: valid transitions), C8 (CD → PE: transition_violation flagging), C5 (PE → EE: context.* includes subject_state) | **Covered.** State machines are projection patterns. Transitions validated against pattern definitions. State available in expression context for form logic. |
| **P09 — Condition-Triggered Action** | C17 (EE → TE: condition evaluation), C21 (TE → ES: output event), C9 (CD → TE: detect-before-act gating) | **Covered.** L3a event-reaction for immediate triggers. L3b deadline-check for non-occurrence. DAG max path 2 for escalation chains. Detect-before-act prevents cascade from flagged events. |
| **P10 — Cross-Reference** | C1 (ES: events carry subject_ref, activity_ref), C2 (ES → PE: projection supports indexed cross-subject queries) | **Covered.** Cross-flow linking via shared subject_ref across activities. activity_ref enables cross-activity references (5-S6 Rule 4). Source-chain traversal (5-S7) enables upstream tracing. |
| **P11 — Shape Definition and Evolution** | C11 (ShR → PE: all versions valid forever), C14 (ShR → CP: all versions delivered), C13 (ShR → DtV: budget enforcement) | **Covered.** Shapes are typed, versioned, deprecation-only evolution. Old records valid under old shape. Multi-version projection routing via shape_ref. |
| **P12 — Offline-First Work** | C1 (ES: client-generated UUIDs, self-describing events), C3 (ES: never reject for staleness), C8 (CD → PE: accept-and-flag), C20 (CP → sync: atomic config delivery), C10 (SR → sync: scope-filtered) | **Covered.** Cross-cutting constraint. Events created offline with full envelope. Accept-and-flag handles staleness on sync. Config delivered atomically. Scope filtering determines sync payload. |

**All 12 patterns are covered.** Every pattern decomposes into contracted interactions without requiring capabilities beyond what the contracts guarantee.

### 4c.2 Scenario Coverage

For each of the 21 scenarios, verify that the scenario's pattern decomposition (from behavioral_patterns.md §4) maps to contracted interactions.

#### Phase 1 — Core (S00–S14)

| Scenario | Patterns | Contracted? | Notes |
|----------|----------|-------------|-------|
| S00 — Basic structured capture | P01, P11 | Yes | C1 + C11 + C14. The simplicity baseline. |
| S01 — Entity-linked capture | P01, P02 | Yes | C1 + C7. Subject identity lifecycle fully contracted. |
| S02 — Periodic reporting | P01, P03, P04 | Yes | C1 + C21 (L3b deadline-check for missing reports) + C10 (assignment-based responsibility). |
| S03 — Designated responsibility | P04, P05 | Yes | C10 + C6. Scope resolver + assignment projection. |
| S04 — Supervisor review | P06, P08, P05 | Yes | C15 (`capture_with_review` pattern) + C16 + C8 + C10. |
| S05 — Supervision visits | P01, P03, P06, P02 | Yes | Composition of S00+S02+S04 contracted capabilities. |
| S06 — Registry lifecycle | P02, P11, P08 | Yes | C7 (merge/split) + C11 (schema evolution) + C15 (state progression). |
| S07 — Resource distribution | P07, P08 | Yes | C15 (`transfer_with_acknowledgment` pattern) + C8. |
| S08 — Case management | P02, P08, P10, P04 | Yes | C15 (`case_management` pattern) + C7 (identity) + C1 (cross-refs via subject_ref) + C10 (responsibility). |
| S09 — Coordinated campaign | P03, P04, P05, P08 | Yes | C21 (deadline monitoring) + C10 (assigned scope) + C15 (state progression) + C6 (hierarchical authority). Campaign progress = aggregation of per-subject state (interface §7.1, doc 26). |
| S10 — Dynamic targeting | P09, P02, P04 | Yes | C17 + C21 (condition-triggered action) + C1 (subject linkage) + C10 (assignment). |
| S11 — Multi-step approval | P06, P08, P05 | Yes | C15 (`multi_step_approval` pattern) + C16 + C8 + C10. |
| S12 — Event-triggered actions | P09, P08 | Yes | C17 + C21 (L3a event-reaction, L3b deadline-check). Escalation via DAG max path 2. |
| S13 — Cross-flow linking | P10, P02 | Yes | C1 (shared subject_ref across activities) + C2 (projection supports cross-subject queries). activity_ref for explicit cross-activity references. |
| S14 — Multi-level distribution | P07, P08, P05, P04 | Yes | C15 (transfer pattern) + multi-hop via activity_ref linking (5-S6 Rule 4) + C10 (scope per level). |

#### Cross-Cutting

| Scenario | Patterns | Contracted? | Notes |
|----------|----------|-------------|-------|
| S19 — Offline capture and sync | P12 | Yes | Cross-cutting constraint. C1 + C3 + C8 + C10 + C20. |

#### Composite Real-World

| Scenario | Patterns | Contracted? | Notes |
|----------|----------|-------------|-------|
| S20 — CHV field operations | P01, P02, P07, P08, P04 | Yes | Composition: structured capture + subject linkage + transfers + state progression + assignment. All patterns individually contracted. |
| S21 — Supervisor operations | P06, P01, P02, P05 | Yes | Composition: review + recording + subject linkage + hierarchical visibility. All patterns individually contracted. |

#### Phase 2 — Extensions

| Scenario | Patterns | Contracted? | Notes |
|----------|----------|-------------|-------|
| S15 — Cross-program overlays | P05, P01, P10 | Yes | Multiple views of same data = aggregation concern (outside, O-1). Source events and projections fully contracted. Interface declared (doc 26 §7.1). |
| S16 — Emergency rapid response | P04, P09, P05, P08 | Yes | Rapid scope reassignment = new AssignmentCreated events (C10). Bypassing normal procedures = new activity with broader scope (no architectural change). Expedited communication = L3a triggers (C21). |
| S18 — Analytics-derived flows | P09, P10, P02 | Yes | Pattern detection = aggregation concern (outside, O-1). Resulting new work = events created through existing trigger/manual paths. Traceability via source_event_ref in payload. Feedback loop = new events feeding future projections (C2). |

**All 21 scenarios are covered.** Every scenario decomposes into pattern combinations that map to contracted interactions.

### 4c.3 Coverage Observations

Three scenarios deserve explicit notes because they touch the architecture boundary:

**S09 (Coordinated campaign)**: Campaign progress monitoring requires cross-subject aggregation ("how many of 500 target households have been visited?"). This is served by the aggregation interface (doc 26 §7.1) — reading from per-subject projection state, inheriting flagged-event-exclusion. The aggregation *model* is outside (O-1), but the architectural inputs and integrity guarantees are contracted. No gap.

**S15 (Cross-program overlays)**: Multiple stakeholder views of the same data is an aggregation/reporting concern. Classified as outside (O-1). The architecture's contribution is: per-subject projection state as input surface, trustworthy records (V3) for traceability back to source. No contract gap — the architecture provides the right input surface; how views are composed is outside.

**S18 (Analytics-derived flows)**: Pattern detection from historical data is analytics, classified as outside (O-1). The architecture's contribution is: (a) events are the input to any analytics system via the projection surface, (b) new work initiated from analytics findings enters the platform as normal events, (c) traceability is maintained via source references in payload. No contract gap.

---

## 5. Sub-step 4d: Outside-Boundary Interfaces

For each concern classified as "outside" (doc 25 §3.3), determine whether the architecture must declare an interface to it or whether it's truly external.

| # | Outside concern | Interface needed? | Interface declaration | Status |
|---|----------------|-------------------|----------------------|--------|
| O-1 | Aggregation model / deployer experience | Yes | Declared in doc 26 §7.1: input = PE per-subject state, integrity = flagged-event-exclusion (5-S2), write boundary = no event creation (1-ST2). | Complete |
| O-2 | Data archival/retention | Yes (minimal) | The append-only invariant (1-S1) is the boundary: the architecture never deletes events. Operational policies that tier/compress without deleting are outside. Policies that delete would require an architectural amendment. | Complete — boundary already stated in doc 25 O-2 |
| O-3 | Reporting | No (subsumed) | Reporting consumes aggregation output. The architecture's interface is O-1's aggregation interface. Reporting has no direct touch point with architecture primitives. | Complete — subsumed by O-1 |
| O-4 | Assessment visibility | No | Scope Resolver configuration (L0 parameter). No new mechanism — whether a worker sees assessments about themselves is a scope-filter parameter. | Complete — doc 25 O-4 |
| O-5 | Subject-based scope (case mgmt) | No | Existing `subject_list` scope type (4-S7). Case assignment is an AssignmentCreated event. No new scope type needed. | Complete — doc 25 O-5 |
| O-6 | Auditor access | No | Existing mechanisms: broad scope assignment + read-only action set via role-action tables (4-S14). | Complete — doc 25 O-6 |
| O-7 | Grace period | N/A | Resolved by 3-S9 (watermark-based auto-resolution). | Complete — not outside, resolved |
| O-8 | Pending match pattern | No | If needed, a new pattern in the Pattern Registry — platform evolution via existing governance (5-S5). Accept-always (2-S14) supports it implicitly now. | Complete — doc 25 O-8 |

**4d result: 2 outside concerns require interface declarations (O-1 aggregation, O-2 archival). Both are already declared.** The remaining 6 either have no direct architecture touch point or are subsumed by existing mechanisms.

---

## 6. Gap Register

The consolidated register. Per the framework: "Every gap has a response. No response is 'decide later.'"

| # | Gap | Type | Evidence | Response |
|---|-----|------|----------|----------|
| — | — | — | — | — |

**The register is empty.** No contract gaps, boundary gaps, coverage gaps, or interface gaps were identified.

---

## 7. Why Zero Gaps

An empty gap register warrants justification. Three factors explain it:

**1. The architecture was stress-tested before consolidation.**
Each ADR went through a three-phase exploration (event storm → stress test → coherence audit) before being committed. ADR-4 and ADR-5 had multi-part sessions with adversarial attacks and irreversibility filters. Problems were surfaced and resolved during exploration, not during consolidation.

**2. The boundary mapping resolved all findings proactively.**
Doc 25 resolved all 4 harvest findings (flag naming, reporting decomposition, sync contract, auth flag types) before contract extraction began. The spec-grade items were derived and validated during Step 2, not left for Step 4 to discover.

**3. The contract extraction achieved full edge coverage.**
Doc 26 verified every inventory edge before this step. The coverage check (4c) confirmed that every pattern and scenario decomposes into contracted interactions. No scenario required capabilities beyond what the contracts guarantee.

The consolidation framework anticipated 0–2 focused exploration sessions from Step 4. Zero sessions are needed. The architecture phase is confirmed complete.

---

## 8. Readiness Assessment for Step 5

The Architecture Description (Step 5) requires:

| Prerequisite | Status |
|-------------|--------|
| Every decided position harvested and traceable | Done (doc 24: 61 positions) |
| Every concern classified with boundary | Done (doc 25: 29 concerns, zero unclassified) |
| Every inter-primitive interaction contracted | Done (doc 26: 21 contracts, zero edge gaps) |
| Every pattern and scenario covered | Done (this document: 12/12 patterns, 21/21 scenarios) |
| Zero unresolved gaps | Done (§6: empty register) |
| No focused explorations needed | Confirmed (§7) |

**All prerequisites met. Step 5 (Architecture Description) may proceed.**

### Architecture Description inputs summary

| Input | Document | Key content |
|-------|----------|-------------|
| Structural view | Doc 22 (inventory) | 11 primitives with invariants, inputs/outputs, config surfaces |
| Decided positions | Doc 24 (harvest) | 61 positions with classifications and primitive bindings |
| Boundaries | Doc 25 (boundary map) | 5 spec-grade derivations, 15 impl-grade boundaries, 8 outside declarations |
| Behavioral view | Doc 26 (contracts) | 21 contracts, 3 cross-cutting sets, 2 interface declarations |
| Coverage proof | Doc 27 (this document) | 12 patterns and 21 scenarios verified against contracts |
| Cross-cutting concerns | Docs 25 + 26 | 8 concerns: envelope, accept-and-flag, detect-before-act, gradient, delivery pipeline, sync contract, flag catalog, aggregation interface |

---

## 9. Step 4 Completion Checklist

- [x] 4a: Every interacting primitive pair checked for contract coverage — zero gaps (§2)
- [x] 4b: Every spec-grade item and contract checked for anchoring strength — zero gaps (§3)
- [x] 4c: All 12 behavioral patterns checked against contracts — all covered (§4 4c.1)
- [x] 4c: All 21 scenarios checked against contracted pattern interactions — all covered (§4 4c.2)
- [x] 4d: All 8 outside concerns checked for interface declarations — all complete (§5)
- [x] Gap register complete — empty (§6)
- [x] Empty register justified (§7)
- [x] Readiness for Step 5 assessed — all prerequisites met (§8)

**Step 4 is complete. Zero gaps. Zero explorations needed. Ready for Step 5 (Architecture Description).**
