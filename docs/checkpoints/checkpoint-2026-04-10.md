# Project Checkpoint — 2026-04-10

---

## 1. Bearing

- **Phase**: Architecture Exploration — ADR-2 complete, ADR-3 next
- **Momentum**: `ADVANCING` — Two foundational ADRs drafted through rigorous three-phase exploration; no stalls or blockers.
- **Last milestone**: ADR-002 (Identity & Conflict Resolution) drafted with 14 sub-decisions, grounded by a Phase 1 event storm, Phase 2 stress test, and Phase 3 classification.
- **Horizon**: ADR-003 (Authorization & Selective Sync) exploration started and scoped — this would unlock the configuration boundary work.

The platform's storage primitive (immutable events) and identity model (four typed categories, alias-based merges, device-sequence + sync-watermark ordering) are now drafted. Both ADRs are in Draft status — committed but revisable. The project has completed the two most irreversible architectural decisions and is entering the layer where more decisions become evolvable strategies rather than permanent commitments. The next ADR (Authorization & Sync) is the last one that touches the event envelope before configuration design begins.

---

## 2. Artifact Map

### **Scenarios (06-scenarios/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| S00 — Basic structured capture | Scenario | SETTLED | Domain-pure. Used as simplicity validation in both ADRs. |
| S01 — Entity-linked capture | Scenario | SETTLED | Domain-pure. Core identity stress scenario for ADR-2. |
| S02 — Multi-source capture | Scenario | SETTLED | Domain-pure. Phase 1. |
| S03 — User-based assignment | Scenario | SETTLED | Domain-pure. Actor identity axis discovered via ADR-2 event storm. |
| S04 — Submitted-reviewed capture | Scenario | SETTLED | Domain-pure. Phase 1 workflow scenario. |
| S05 — Aggregated view | Scenario | SETTLED | Domain-pure. Phase 1. |
| S06 — Entity registry lifecycle | Scenario | SETTLED | Domain-pure. Split/merge/deactivation stress for ADR-2. |
| S06b — Schema evolution while offline | Scenario | SETTLED | Domain-pure. Phase 1. |
| S07 — Resource distribution | Scenario | SETTLED | Domain-pure. Process identity type discovered here. |
| S08 — Case management | Scenario | SETTLED | Domain-pure. Phase 1. Long-running case stress. |
| S09 — Dashboard with drill-down | Scenario | SETTLED | Domain-pure. Phase 1. |
| S10 — Scheduled activities | Scenario | SETTLED | Domain-pure. Phase 1. |
| S11 — Multi-step approval | Scenario | SETTLED | Domain-pure. Phase 1. |
| S12 — Event-triggered actions | Scenario | SETTLED | Domain-pure. Phase 1. Scoping mandated by viability (Condition 2). |
| S13 — Import/export | Scenario | SETTLED | Domain-pure. Phase 1. |
| S14 — Multi-level distribution | Scenario | SETTLED | Domain-pure. Phase 1. |
| S15 — Complex scheduling | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S16 — Rule builder | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S17 — Custom dashboards | Scenario | SETTLED | Domain-pure. Phase 1. |
| S18 — External system sync | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S19 — Offline capture and sync | Scenario | SETTLED | Domain-pure. Central conflict/ordering scenario for ADR-2. |
| S20 — Role-based data access | Scenario | SETTLED | Domain-pure. Phase 1. |
| S21 — Multi-org/multi-program | Scenario | SETTLED | Domain-pure. Phase 1. |

### **Patterns (01-patterns/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| Behavioral patterns catalog | Patterns | SETTLED | 12 recurring primitives identified across 21 scenarios. Domain-pure rewrite completed. |

### **Principles (principles.md)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| P1 — Offline is the default | Principle | DRAFT | Confirmed by ADR-001 & ADR-002. |
| P2 — Set up, not built | Principle | DRAFT | Hypothesis. Pending ADR-004 (Configuration Boundary). |
| P3 — Records are append-only | Principle | DRAFT | Confirmed by ADR-001 & ADR-002. |
| P4 — One system, not a toolkit | Principle | DRAFT | Hypothesis. |
| P5 — Conflict is surfaced | Principle | DRAFT | Confirmed by ADR-001 & ADR-002. Not yet updated in principles.md. |
| P6 — Metadata is data | Principle | DRAFT | Hypothesis. |
| P7 — Simplest scenario stays simple | Principle | DRAFT | Confirmed by ADR-001 S00 walk-through & ADR-002 simplicity validation. |

### **ADRs (adrs/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| ADR-001 — Offline Data Model | ADR | DECIDED | 10 sub-decisions. Decision audit completed. Readiness audit passed. Promoted 2026-04-10. |
| ADR-002 — Identity & Conflict | ADR | DECIDED | 14 sub-decisions. Full three-phase exploration complete. Readiness audit passed. Promoted 2026-04-10. |
| ADR-003 — Authorization & Sync | ADR | NOT STARTED | Scope defined by decision audit §3 and ADR-002 consequences. |
| ADR-004 — Configuration Paradigm | ADR | NOT STARTED | Viability Condition 1 mandates this. Most consequential for V2. |
| ADR-005 — State Progression | ADR | NOT STARTED | Depends on ADR-001 through ADR-004. |

### **Explorations (07-decision-exploration/)**

| Artifact | Type | Status | Notes |
| --- | --- | --- | --- |
| 00 — Exploration Framework | Framework | SETTLED | Three-phase methodology (event storm → stress test → classification). |
| 01 — Architecture Landscape | Exploration | SETTLED | Dependency map, 5 decision blocks, tension analysis. |
| 02 — ADR-1 Offline Data Model | Exploration | SETTLED | 5 sub-decisions, 3 options evaluated, scenario-by-scenario analysis. |
| 03 — ADR-1 Forward Projection | Exploration | SETTLED | Options projected through ADRs 2–5. Irreversibility gradient established. |
| 04 — Decision Audit | Exploration | SETTLED | 6 issues found, 5 required changes applied to ADR-001, ADR scope normalization. |
| 05 — ADR-2 Phase 1 Event Storm | Exploration | SETTLED | 5 scenarios, 4 identity types, 7 conflict types, 12 aggregates, 13 questions. |
| 07 — ADR-2 Phase 2 Stress Test | Exploration | SETTLED | 19 findings across 3 mechanisms, 8 missing workflow paths, 12 assumptions. |
| 09 — ADR-2 Phase 3 Classification | Exploration | SETTLED | 15 Bucket-1 constraints, 14 Bucket-2 strategies, 5 Bucket-3 deferrals, 5 Bucket-4 risks. S00 simplicity validated. |

### **Experiments (02-experiments/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| 002 — Scenario/Primitive Walk-through | Experiment | SETTLED | 12 primitives mapped across 21 scenarios. |
| S00 Event Spike | Experiment | SETTLED | Python spike + observations. 5 steps (capture, projection, correction, conflict, replay). All passed. 5 open questions recorded. |

### **Cross-cutting**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| Access Control Scenario | Cross-cut | SETTLED | Domain-pure rewrite. Role × context × resource matrix. Feeds ADR-003. |

---

## 3. Decision Board

#### ADR-001: Offline Data Model

- **Status**: DECIDED — readiness audit passed, promoted 2026-04-10.

| Decision | Status | Confidence | Blocking |
| --- | --- | --- | --- |
| S1: Records are append-only | SETTLED | HIGH | Nothing — forced by V3/P3. |
| S2: Atomic unit = typed immutable event | SETTLED | HIGH | ADR-002 (complete), ADR-003, ADR-004, ADR-005. |
| S3: Client-generated UUIDs | SETTLED | HIGH | Nothing — forced by V1/P1. |
| S4: Sync unit = immutable event | SETTLED | HIGH | ADR-003. |
| S5: Conflict detection must surface | SETTLED | HIGH | ADR-002 (complete). |
| S6: Event log = single source of truth | SETTLED | HIGH | All downstream. |
| S7: Projections rebuildable from events | SETTLED | HIGH | ADR-003 (projection location). |
| S8: Event envelope minimum fields | DRAFT | HIGH | ADR-002 added more fields. |
| Write-path discipline (M1 from audit) | SETTLED | HIGH | already added to ADR-001 per audit change 1. |
| Projection rebuild scope (M2 from audit) | OPEN | MEDIUM | ADR-003 resolves. |

- **Escape hatches**: B→C (event-sourcing to action-log with maintained views). Activation: if projection complexity proves unmanageable on low-end Android. Requires gap-free event log (enforced by write-path discipline rule).

#### ADR-002: Identity Model and Conflict Resolution

- **Status**: DECIDED — readiness audit passed, promoted 2026-04-10.

| Decision | Status | Confidence | Blocking |
|----------|--------|------------|----------|
| S1: Causal ordering (device_seq + watermark) | SETTLED | HIGH | ADR-003 (sync protocol). |
| S2: Typed identity references | SETTLED | HIGH | ADR-003, ADR-004. |
| S3: device_time is advisory | SETTLED | HIGH | Nothing. |
| S4: Sequence/watermark persistence | SETTLED | HIGH | Implementation. |
| S5: device_id is hardware-bound | SETTLED | HIGH | Implementation. |
| S6: Merge = alias in projection | SETTLED | HIGH | ADR-003 (sync scope for merge events). |
| S7: No SubjectsUnmerged | SETTLED | HIGH | Nothing. |
| S8: Split freezes history | SETTLED | HIGH | Nothing. |
| S9: Lineage graph acyclicity | SETTLED | HIGH | Nothing. |
| S10: Merge/split are online-only | SETTLED | HIGH | ADR-003. |
| S11: Single-writer conflict resolution | SETTLED | HIGH | ADR-003 (who is designated resolver). |
| S12: Detect before act (sync order) | SETTLED | HIGH | ADR-005 (cascade handling). |
| S13: Detection uses raw references | SETTLED | HIGH | Nothing. |
| S14: Events never rejected for staleness | SETTLED | HIGH | Nothing. |

- **Escape hatches**: None documented — ADR-002 decisions are treated as constraints (Bucket 1), not strategies.
- **Open questions**:
  - Strategies (Bucket 2) documented in Phase 3 are initial positions, not commitments — e.g., flag creation location (server-first), batch resolution, auto-resolution policies.
  - Deferred items to ADR-4/5: domain-specific conflict rules, downstream cascade, pending match generality.

#### ADR-003: Authorization and Selective Sync

- **Status**: NOT STARTED

- **Known sub-decisions** (from decision audit §3 and ADR-002 consequences):
  - Authorization model representation (role + scope + context)
  - On-device enforcement against projected state
  - Sync scope — what data flows to which device
  - Sync topology — one-tier vs. two-tier (full events vs. summaries)
  - Where projections live (device, server, or both)
  - Projection rebuild scope (ADR-001 M2 resolves here)
  - Stale access rule handling
  - Who can be designated conflict resolver (ADR-002 S11)
  - Who can perform merge/split operations (ADR-002 S10)

- **Key input scenarios**: S03 (assignment enforcement offline), S07 (multi-level handoffs), S05/S09/S21 (hierarchical visibility), S20 (role-based access).

#### ADR-004: Configuration Paradigm and Boundary

- **Status**: NOT STARTED

- **Known sub-decisions** (from decision audit §3):
  - Event type vocabulary ownership — platform vs. deployment configurable
  - Shape/schema definition mechanics
  - Configuration change propagation to devices
  - Configuration versioning and coexistence
  - The T2 boundary — where configuration stops and code begins
  - Domain-specific conflict rule configuration (deferred from ADR-002)

- **Viability mandate**: Condition 1 requires this to be the first major architecture decision after identity/sync foundations are in place.

#### ADR-005: State Progression and Workflow

- **Status**: NOT STARTED

- **Known sub-decisions** (from decision audit §3):
  - State machines: projection-derived, explicit primitives, or both
  - Data vs. workflow event separation
  - Projection rules for state computation
  - Multi-step, multi-actor workflow composition (S11, S14)
  - Offline workflow conflict handling
  - Downstream cascade from conflict resolution (deferred from ADR-002)
  - Pending match mechanics (deferred from ADR-002)

---

## 4. Open Fronts

1. **ADR-003 exploration has not started**
   Why it blocks: ADR-003 determines sync scope, projection location, and authorization model — all required before ADR-004 can meaningfully explore the configuration boundary.
   Unblocked by: Begin the `/ade` session for ADR-003, storming S03, S05, S07, S20, and S21.
   ADR home: ADR-003

2. **Configuration boundary (T2 tension) is unresolved**
   Why it blocks: Viability Condition 1. The central promise of the platform (V2: "set up, not built") depends on drawing the right line between what deployers configure and what requires development.
   Unblocked by: ADR-003 must be at least in exploration before ADR-004 can start. Then: `/ade` for ADR-004.
   ADR home: ADR-004

3. **Projection engine scaling (high-event subjects)**
   Why it blocks: ADR-002 Phase 2 (B1/B5) identified that merge-triggered projection rebuilds on low-end devices could exceed time budgets. The S00 spike tested 5 events; real-world subjects may have hundreds.
   Unblocked by: Spike experiment — projection rebuild for a subject with 100–500 events on simulated low-end hardware.
   ADR home: Strategy (not an ADR constraint — projection strategy is Bucket 2)

4. **S12 (event-triggered actions) scoping**
   Why it blocks: Viability Condition 2 mandates strict scoping to prevent S12 from becoming an unbounded rules engine. No exploration has been conducted.
   Unblocked by: ADR-004 exploration, which must draw the line for S12.
   ADR home: ADR-004

5. **Workflow model (state machines from projections vs. explicit primitives)**
   Why it blocks: ADR-005 cannot start until ADR-004 decides the configuration boundary. S04, S08, S11, S14 all depend on the workflow model.
   Unblocked by: Complete ADR-003 → ADR-004 → then explore ADR-005.
   ADR home: ADR-005

6. **Batch flag resolution operational model**
   Why it blocks: Phase 2 (A3/A6) identified that accept-and-flag collapses without batch resolution at deployment scale. This is a Bucket 2 strategy — no ADR constraint needed — but the read-model design must be considered during ADR-003 (sync, projection).
   Unblocked by: ADR-003 exploration, which must account for flag read-model requirements.
   ADR home: ADR-003 / Strategy

---

## 5. Condition Ledger

### 5a. Viability GO Conditions

| # | Condition | Status | Evidence |
|---|-----------|--------|----------|
| 1 | Configuration boundary must be the first major architecture decision. | NOT MET | ADR-004 has not started. ADR-001 and ADR-002 are completed first by dependency order (ADR-004 depends on both). The ordering is correct — the condition is being approached via the dependency chain, not ignored. |
| 2 | Scenario 12 (event-triggered actions) requires strict scoping to prevent unbounded rules engine. | NOT ADDRESSED | S12 is Phase 1 but no exploration specific to its scoping constraint has been conducted. Will be addressed during ADR-004 exploration. |
| 3 | Phase 2 scenarios (S15, S16, S18) remain deferred to protect Phase 1 core architecture. | MET | All three scenarios are marked as deferred. No exploration or ADR work has referenced them. |

### 5b. ADR Escape Hatch Conditions

| ADR | Escape Hatch | Activation Condition | Status |
|-----|-------------|---------------------|--------|
| ADR-001 | B→C: add application-maintained views alongside event store | Projection complexity proves unmanageable on low-end Android (< 200ms screen load target missed) | SAFE — S00 spike showed 5-event projection is trivial. No real-world data yet. Projection scaling spike proposed but not run. |
| ADR-002 | Auto-resolution for state-change flags | Flags generated/day ÷ flags resolved/day > 3 for > 2 consecutive weeks | SAFE — no deployment exists yet. |
| ADR-002 | Actor-sequence counter for cross-device | > 10% of total conflict flags traced to same-actor cross-device ordering | SAFE — no deployment exists yet. |

---

## 6. Risk Pulse

**Initial risk register** (first checkpoint).

| Risk | Severity | Trigger | Mitigation |
|------|----------|---------|------------|
| Projection complexity on low-end devices | MEDIUM | Screen load > 200ms for subjects with 100+ events | B→C escape hatch. Projection scaling spike not yet run. |
| Accept-and-flag backlog overwhelm | MEDIUM | Real deployment with bulk reconfiguration + offline workers | Batch resolution (Bucket 2 strategy), auto-resolution policies. Both designed but not implemented. |
| Configuration boundary drawn too wide | HIGH | ADR-004 exploration | Viability Condition 1 + T2 tension analysis. Not yet started. |
| Meta-conflict (competing resolutions) | LOW | Mitigated by ADR-002 S11 (single-writer resolution). If single-writer enforcement fails in implementation, risk resurfaces. | Structural constraint in ADR-002. |
| Post-split attribution at scale | LOW | Large split operations + many offline workers | Accepted risk in ADR-002. Frozen-history constraint (S8) is the trade-off. |

**Resolved or de-risked**:

| Risk | Resolution | When |
|------|-----------|------|
| Unmerge structural unsoundness | Eliminated: SubjectsUnmerged replaced by corrective split (ADR-002 S7) | ADR-002 Phase 2/3 (2026-04-10) |
| Cyclic lineage graph | Mitigated: Acyclicity by construction (ADR-002 S9) | ADR-002 Phase 2/3 (2026-04-10) |
| Causal ordering mechanism unknown | Resolved: device-sequence + sync-watermark chosen (ADR-002 S1) | ADR-002 Phase 1–3 (2026-04-10) |
| Identity model ambiguity | Resolved: Four typed categories with common envelope protocol (ADR-002 S2) | ADR-002 Phase 1 (2026-04-10) |

---

## 7. March Orders

1. **Begin ADR-003 exploration (Authorization & Selective Sync)**
   Why now: ADR-003 is the next in the dependency chain (ADR-001 → ADR-002 → ADR-003 → ADR-004 → ADR-005). It blocks ADR-004, which is the viability-mandated configuration boundary decision. The event storm methodology is proven. Input scenarios are identified (S03, S05, S07, S20, S21).
   Expected artifact: `docs/07-decision-exploration/10-adr3-phase1-event-storm.md`
   Estimated scope: WORK (multi-session)

2. **Run projection scaling spike (100–500 events per subject)**
   Why now: Phase 2 identified projection rebuild cost as a potential concern (B1/B5). The S00 spike validated 5 events — trivially fast. A spike with realistic event volumes would de-risk the B→C escape hatch assessment and inform ADR-003's projection location decision.
   Expected artifact: `docs/02-experiments/s01-projection-spike/` — code + observations.
   Estimated scope: SESSION (half-day)

3. **Formal decision gate: Pass 1 → Pass 2 readiness audit for ADR-001 and ADR-002**
   Why now: Both ADRs are Draft. Before proceeding too far into ADR-003+, verify that ADR-001 and ADR-002 are internally consistent, have no dependency contradictions, and can be promoted toward Final. This prevents building on a foundation that later needs revision.
   Expected artifact: A short audit note or annotation on both ADRs — either promoting to DECIDED (Final) or listing specific revision requirements.
   Estimated scope: SESSION (half-day)
