# Project Checkpoint — 2026-04-13

---

## 1. Bearing

- **Phase**: Architecture Exploration — four foundational ADRs complete, entering workflow
- **Momentum**: `ADVANCING` — ADR-004 completed (full exploration + decision) since last checkpoint. Four of five ADRs now decided.
- **Last milestone**: ADR-004 (Configuration Boundary) decided with 14 sub-decisions. Event envelope finalized at 11 fields. P2 ("set up, not built") confirmed. T2 (highest-severity tension) resolved.
- **Horizon**: ADR-005 (State Progression & Workflow) — the last remaining foundational ADR. Determines how work moves through stages, whether state machines are a platform primitive, and how workflow conflict cascades are handled.

The platform's architectural foundation is now substantially resolved. Four ADRs cover: storage (immutable events), identity (four types, accept-and-flag), authorization (assignment-based, sync=scope), and configuration (typed shapes, fixed types, four-layer gradient). The event envelope — the contract that governs every record written across every device — is finalized at 11 fields. No further ADRs are expected to extend it.

**Shift in decision character**: ADRs 1–3 asked "how does the engine work?" ADR-4 asked "where does the engine end and the deployment begin?" ADR-5 asks "how does work move through stages?" — combining workflow semantics with the constraints from all four prior ADRs. The key risk is no longer boundary judgment (ADR-4) but behavioral composition: how do offline-first, append-only event streams express state transitions, multi-actor handoffs, and cascading consequences?

---

## 2. Artifact Map

### **Scenarios (06-scenarios/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| S00 — Basic structured capture | Scenario | SETTLED | Simplicity validation target. ADR-4 validates: 2 artifacts, no code. |
| S01 — Entity-linked capture | Scenario | SETTLED | Core identity stress scenario for ADR-2. |
| S02 — Periodic reporting | Scenario | SETTLED | Phase 1. |
| S03 — User-based assignment | Scenario | SETTLED | Actor identity axis. ADR-3 input. |
| S04 — Supervisor review | Scenario | SETTLED | Phase 1 workflow scenario. Walk-through completed. |
| S05 — Supervision audit visits | Scenario | SETTLED | Hierarchical visibility. ADR-3 input. |
| S06 — Entity registry lifecycle | Scenario | SETTLED | Split/merge/deactivation. ADR-2 + ADR-4 (schema evolution). |
| S07 — Resource distribution | Scenario | SETTLED | Process identity type. Multi-level handoffs. |
| S08 — Case management | Scenario | SETTLED | Long-running case stress. Walk-through completed. ADR-5 key input. |
| S09 — Coordinated campaign | Scenario | SETTLED | Multi-assignment configuration. ADR-4 activity model validated. |
| S10 — Dynamic targeting | Scenario | SETTLED | Phase 1. |
| S11 — Multi-step approval | Scenario | SETTLED | Phase 1. ADR-5 key input. |
| S12 — Event-triggered actions | Scenario | SETTLED | Scoped by ADR-4 (L3 triggers, server-only, depth-2 DAG, 50 per deployment). |
| S13 — Cross-flow linking | Scenario | SETTLED | Phase 1. |
| S14 — Multi-level distribution | Scenario | SETTLED | Phase 1. ADR-3 + ADR-5 input. |
| S15 — Cross-program overlays | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S16 — Emergency rapid response | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S18 — Advanced analytics | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S19 — Offline capture and sync | Scenario | SETTLED | Central conflict/ordering scenario for ADR-2. |
| S20 — CHV field operations | Scenario | SETTLED | ADR-4 Session 2 walk-through completed. |
| S21 — CHV supervisor operations | Scenario | SETTLED | Phase 1. Multi-org/multi-program. |

### **Principles (principles.md)**

| Principle | Status | Last tested | Change since last checkpoint |
|-----------|--------|-------------|------------------------------|
| P1: Offline is the default | Confirmed | ADR-001, ADR-002, ADR-004 | **ADR-004 confirmation added** (L2 on-device, atomic config at sync) |
| P2: Configuration has boundaries | Confirmed | ADR-004 | **NEW — confirmed from Hypothesis** (four-layer gradient, complexity budgets, S00 = 2 artifacts) |
| P3: Records are append-only | Confirmed | ADR-001, ADR-002 | Unchanged |
| P4: Patterns compose | Confirmed | ADR-001, ADR-002, ADR-003, ADR-004 | **ADR-004 confirmation added** (shapes + events + assignments + flags — all composition, no new mechanisms) |
| P5: Conflict is surfaced | Confirmed | ADR-001, ADR-002 | Unchanged |
| P6: Authority is contextual and auditable | Confirmed | ADR-003 | Unchanged |
| P7: Simplest scenario stays simple | Confirmed | ADR-001, ADR-002, ADR-004 | **ADR-004 confirmation added** (S00 config ≈ ODK XLSForm effort) |

### **ADRs (adrs/)**

| Artifact | Type | Status | Sub-decisions | Change since last checkpoint |
|----------|------|--------|---------------|------------------------------|
| ADR-001 — Offline Data Model | ADR | DECIDED | 5 structural + 3 strategy | Unchanged |
| ADR-002 — Identity & Conflict | ADR | DECIDED | 14 | Unchanged |
| ADR-003 — Authorization & Sync | ADR | DECIDED | 10 | **Promoted from DRAFT** |
| ADR-004 — Configuration Boundary | ADR | DECIDED | 14 (3 structural + 5 strategy-protecting + 6 strategy) | **NEW — completed** |
| ADR-005 — State Progression | ADR | NOT STARTED | — | 6 specific questions queued from ADR-4 |

### **Explorations (07-decision-exploration/)**

| Artifact | Type | Status | Change since last checkpoint |
|----------|------|--------|------------------------------|
| 00 — Exploration Framework | Framework | SETTLED | Unchanged |
| 01 — Architecture Landscape | Exploration | SETTLED | Unchanged |
| 02–04 — ADR-1 explorations | Exploration | SETTLED | Unchanged |
| 05, 07, 09 — ADR-2 explorations | Exploration | SETTLED | Unchanged |
| 10–12 — ADR-3 explorations | Exploration | SETTLED | Unchanged |
| 13 — ADR-4 Session 1 Scoping | Exploration | SETTLED | Unchanged (completed before last checkpoint) |
| 14 — ADR-4 Session 2 Walk-throughs | Exploration | SETTLED | **NEW** |
| 15 — ADR-4 Session 3 Part 1: Structural Coherence | Exploration | SETTLED | **NEW** |
| 16 — ADR-4 Session 3 Part 2: Irreversibility Filter | Exploration | SETTLED | **NEW** |
| 17 — ADR-4 Session 3 Part 3: Adversarial Stress Tests | Exploration | SETTLED | **NEW** |
| 18 — ADR-4 Session 3 Part 4: Remaining Q Resolution | Exploration | SETTLED | **NEW** |

### **Experiments (02-experiments/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| 002 — Scenario/Primitive Walk-through | Experiment | SETTLED | 12 primitives mapped. |
| S00 Event Spike | Experiment | SETTLED | Python spike + observations. All 5 steps passed. |

### **Cross-cutting**

| Artifact | Type | Status | Change since last checkpoint |
|----------|------|--------|------------------------------|
| Access Control Scenario | Cross-cut | SETTLED | Unchanged |
| Constraints | Cross-cut | SETTLED | Unchanged |
| Viability Assessment | Cross-cut | SETTLED | Unchanged |
| Patterns (01-patterns/) | Patterns | SETTLED | 12 behavioral patterns. Unchanged. |

---

## 3. Decision Board

#### ADR-001: Offline Data Model — DECIDED

All sub-decisions settled at HIGH confidence. No changes since last checkpoint.

Escape hatch: B→C (add materialized views alongside event store). **SAFE** — spike passed for 5 events. Scaling spike not yet run.

#### ADR-002: Identity & Conflict — DECIDED

All 14 sub-decisions settled at HIGH confidence. No changes since last checkpoint.

No escape hatches. All decisions are Bucket 1 constraints.

#### ADR-003: Authorization & Sync — DECIDED

**Change**: Promoted from DRAFT to DECIDED since last checkpoint.

All 10 sub-decisions settled. 4 structural constraints, 3 strategy-protecting, 3 initial strategies.

Escape hatch: S3 → add `authority_context` to envelope via ADR-001 S5 if projection-based reconstruction proves slow. Trigger: >50ms/event. **SAFE** — no deployment exists.

#### ADR-004: Configuration Boundary — DECIDED (NEW)

14 sub-decisions committed via four-session exploration (scoping → walk-throughs → structural coherence / irreversibility / stress tests / Q resolution → ADR writing).

| Decision | Classification | Status | Confidence |
|----------|---------------|--------|------------|
| S1: shape_ref in envelope | Structural constraint | SETTLED | HIGH |
| S2: activity_ref optional in envelope | Structural constraint | SETTLED | HIGH |
| S3: Platform-fixed type vocabulary (6 types) | Structural constraint | SETTLED | HIGH |
| S4: System actor identity convention | Strategy-protecting | SETTLED | HIGH |
| S5: Triggers execute server-only | Strategy-protecting | SETTLED | HIGH |
| S6: Atomic configuration delivery | Strategy-protecting | SETTLED | HIGH |
| S7: No deployer-authored scope logic | Strategy-protecting | SETTLED | HIGH |
| S8: No field-level sensitivity | Strategy-protecting | SETTLED | HIGH |
| S9: Four-layer gradient (L0–L3) | Initial strategy | SETTLED | HIGH |
| S10: Shape definition & evolution | Initial strategy | SETTLED | HIGH |
| S11: Expression language (operators, no functions) | Initial strategy | SETTLED | MEDIUM |
| S12: Trigger architecture & limits | Initial strategy | SETTLED | HIGH |
| S13: Complexity budgets (hard, enforced) | Initial strategy | SETTLED | MEDIUM |
| S14: Deployer-parameterized policies | Initial strategy | SETTLED | HIGH |

Escape hatches:
- **Expression ceiling**: If >3 deployments in year one require code for capabilities that "feel configurable," revisit L3 expressiveness.
- **Complexity budgets**: Deploy-time rejections consistently appealed → revisit specific budget values.
- **Deprecation-only evolution**: >3 deployments hit field budget from deprecated-field accumulation → revisit evolution policy.
- **Six event types**: ADR-5 may add a 7th (`status_changed`) if state transitions need different processing behavior.

All escape hatches are **SAFE** — no deployment exists. Budget adjustments and vocabulary additions require no data migration.

#### ADR-005: State Progression — NOT STARTED

Six specific questions queued from ADR-004's "Next Decision" section:

| # | Question | Source |
|---|----------|--------|
| 1 | State machine as platform primitive or projection pattern? | ADR-1 deferral |
| 2 | `status_changed` as 7th event type? | ADR-4 Session 3 |
| 3 | Multi-step, multi-actor workflow composition (offline-first) | S08, S11 |
| 4 | Workflow–flag cascade interaction | ADR-2 accept-and-flag model |
| 5 | `context.*` expression scope for workflow-aware form logic | ADR-4 Session 3 stress test |
| 6 | Domain conflict resolution automation (Q7b) | ADR-4 Session 3 Part 4 |

Key inputs: S08 (case management), S11 (multi-step approval), S04 (supervisor review with state transitions), S07 (resource distribution with transfer handoffs).

---

## 4. Open Fronts

Ranked by blocking potential.

### 1. ADR-005: State Progression & Workflow — CRITICAL PATH

**Why it blocks**: Last foundational ADR. S04, S08, S11, S14 all describe work that moves through stages with multiple actors. The platform cannot express any non-trivial workflow until ADR-5 decides whether state machines are primitives or patterns, and how multi-actor handoffs compose within the append-only, offline-first model.

**What changed since last checkpoint**: Fully unblocked. ADR-4 provides the type vocabulary (6 types, growable), trigger architecture (server-only, depth-2 DAG), and expression language that ADR-5 builds on. Six specific questions are queued.

**Unblocked by**: Begin ADR-5 Session 1 (scoping + event storm from S08/S11/S04/S07).

### 2. Projection scaling spike — DE-RISKING

**Why it blocks**: ADR-002 Phase 2 identified projection rebuild cost as a potential concern for subjects with 100–500 events. The S00 spike validated 5 events. Not blocking ADR-5 but informs the B→C escape hatch assessment under realistic load.

**What changed since last checkpoint**: Unchanged. Still not run.

**Unblocked by**: Build and run the spike. Expected artifact: `docs/02-experiments/s01-projection-spike/`

### 3. Platform specification gap — EMERGING

**Why it matters**: Four ADRs are decided, but no document yet maps ADR decisions onto implementation-grade primitives. The primitives table in the walk-through document (002-walk-through-scenarios-primitives.md) is the closest, but the shape registry, trigger engine, deploy-time validator, and expression evaluator are now settled enough to warrant a consolidated specification.

**This is not blocking** ADR-5 or any near-term work. It's technical debt that grows as ADRs accumulate: future readers must reconstruct the full picture by reading 4 ADRs, 18 exploration documents, and 12 primitives. Not urgent, but worth noting.

**Unblocked by**: After ADR-5 — consolidate all decided primitives into a single reference.

---

## 5. Condition Ledger

### 5a. Viability GO Conditions

| # | Condition | Status | Evidence | Change |
|---|-----------|--------|----------|--------|
| 1 | Configuration boundary must be among the first major architecture decisions. | **MET** | ADR-004 decided. Four-layer gradient, complexity budgets, explicit L3→code boundary. | **RESOLVED** — was NOT MET |
| 2 | S12 requires strict scoping to prevent unbounded rules engine. | **MET** | ADR-004 S5 (server-only), S12 (depth-2 DAG, 5 triggers/type, 50/deployment), S13 (hard budgets). | **RESOLVED** — was NOT ADDRESSED |
| 3 | Phase 2 scenarios (S15, S16, S18) remain deferred. | MET | All three scenarios marked DEFERRED. No ADR work references them. | Unchanged |

**All three GO conditions are now MET.** The platform viability assessment's conditional recommendation is satisfied.

### 5b. ADR Escape Hatch Conditions

| ADR | Escape Hatch | Activation Condition | Status |
|-----|-------------|---------------------|--------|
| ADR-001 | B→C: add materialized views | Projection complexity unmanageable on low-end Android (<200ms target missed) | SAFE — spike passed for 5 events. Scaling spike not yet run. |
| ADR-002 | Auto-resolution for state-change flags | Flags generated/day ÷ resolved/day > 3 for > 2 consecutive weeks | SAFE — no deployment exists. |
| ADR-002 | Actor-sequence counter for cross-device | > 10% of conflict flags traced to same-actor cross-device ordering | SAFE — no deployment exists. |
| ADR-003 | Add authority_context to envelope | Authority reconstruction > 50ms/event at scale | SAFE — no deployment exists. |
| ADR-004 | Revisit L3 expressiveness ceiling | > 3 deployments need code for "should be configurable" | SAFE — no deployment exists. |
| ADR-004 | Revisit specific complexity budgets | Deploy-time rejections consistently appealed | SAFE — no deployment exists. |
| ADR-004 | Revisit deprecation-only evolution | > 3 deployments hit field budget from deprecated accumulation | SAFE — no deployment exists. |
| ADR-004 | Add 7th event type (status_changed) | ADR-5 identifies processing behavior that differs from `capture` | OPEN — ADR-5 will evaluate this. |

### 5c. ADR-004 Revisit Triggers (NEW)

These are post-deployment monitoring triggers accumulated during ADR-4 exploration. None can activate until deployments exist.

| Trigger | Threshold | What it informs |
|---------|-----------|-----------------|
| Expression ceiling hit rate | > 3 deployments requiring code for "configurable" needs | L3 expressiveness / AP-2 boundary |
| Complexity budget appeals | Consistent deployer pushback on specific budgets | Budget calibration |
| Deprecated-field accumulation | > 3 deployments hitting field budget from cruft | Evolution strategy (S10) |
| Flag volume at scale | Total flags generated per sync cycle | Accept-and-flag scalability |
| Authority reconstruction speed | > 50ms/event for authority chain rehabilitation | ADR-003 S3 escape hatch |
| Supervisor sync time | Total sync duration for supervisor-tier devices | Tiered projection strategy (ADR-003 S8) |

---

## 6. Risk Pulse

| Risk | Severity | Trigger | Mitigation | Change since last checkpoint |
|------|----------|---------|------------|------------------------------|
| Projection complexity on low-end devices | MEDIUM | Screen load > 200ms for 100+ events | B→C escape hatch + scaling spike | **UNCHANGED** — spike still not run |
| Authority reconstruction slow at scale | MEDIUM | > 50ms/event | Per-actor caching or add authority_context to envelope | Unchanged |
| Accept-and-flag backlog overwhelm | MEDIUM | Bulk reconfiguration + offline workers | Batch resolution + auto-resolution | Unchanged |
| Blocking flags delaying legitimate work | LOW | > 5% blocking flags resolved as "valid" | Per-flag-type severity configuration (now decided: ADR-4 S14) | **MITIGATED** — mechanism decided |
| Workflow cascade on flagged events | MEDIUM | ADR-5 exploration | Detect-before-act (ADR-2 S12) + ADR-5 must define cascade behavior | **NEW** |
| State machine complexity vs. projection derivation | MEDIUM | ADR-5 exploration | 6-type vocabulary provides floor; ADR-5 chooses between primitive and pattern | **NEW** |

**Resolved since last checkpoint**:

| Risk | Resolution | When |
|------|-----------|------|
| Configuration boundary drawn too wide | Resolved: four-layer gradient, hard complexity budgets, explicit L3→code boundary, 6 anti-patterns as guardrails | ADR-004 (2026-04-13) |
| S12 becoming unbounded rules engine | Resolved: L3 triggers server-only, depth-2 DAG, 5 triggers/event type, 50 per deployment | ADR-004 (2026-04-13) |
| T2 tension (config vs. expressiveness) | Resolved: graduated boundary with visible ceiling | ADR-004 (2026-04-13) |

---

## 7. Progress Assessment

### The arc so far

```
ADR-1 (Storage)       ████████████████████ DECIDED ✓
ADR-2 (Identity)      ████████████████████ DECIDED ✓
ADR-3 (Authorization) ████████████████████ DECIDED ✓
ADR-4 (Configuration) ████████████████████ DECIDED ✓
ADR-5 (Workflow)      ░░░░░░░░░░░░░░░░░░░░ NOT STARTED
```

**Architecture exploration is 80% complete by ADR count and substantially more by irreversibility surface.** ADR-5's decisions are expected to have a smaller irreversibility footprint than ADR-4 — most workflow choices are projection patterns and server-side processing, not stored-event changes. The envelope is finalized. The trend observed from ADR-3 onward — each subsequent ADR touches fewer irreversible surfaces — is expected to continue.

### What the last checkpoint's march orders produced

| March Order | Result |
|-------------|--------|
| 1. Run ADR-003 readiness audit | **DONE** — ADR-003 promoted to DECIDED |
| 2. Begin ADR-004 Session 1 | **DONE** — all 4 sessions completed, ADR-004 DECIDED |
| 3. Update principles.md | **DONE** — all 7 principles confirmed, file updated |
| 4. Projection scaling spike (optional) | **NOT DONE** — deprioritized behind ADR-4 completion |

ADR-4 completion was the critical path item. Three of four march orders were executed. The optional spike was correctly deprioritized.

### Event envelope evolution (closed)

| Checkpoint | Envelope fields | Source |
|------------|----------------|--------|
| ADR-1 | 4: id, type, payload, timestamp | Minimum viable |
| ADR-2 | 9: + subject_ref, actor_ref, device_id, device_seq, sync_watermark | Identity + ordering |
| ADR-3 | 9: (no change — authority-as-projection) | Authorization is derived |
| ADR-4 | 11: + shape_ref, activity_ref | Configuration + activity context |

The envelope is finalized. Future ADRs should not add fields.

---

## 8. March Orders

### 1. Begin ADR-005 Session 1: Scoping & Event Storm

**Why now**: ADR-5 is the sole critical path item. All four upstream ADRs are decided. Six specific questions are queued from ADR-4.

**Approach**: Event storm S08 (case management), S11 (multi-step approval), S04 (supervisor review), S07 (resource distribution) through the workflow lens. Identify: what state transitions look like in event streams, where multi-actor handoffs create tension with offline-first, how cascading consequences interact with detect-before-act.

**Expected artifacts**: `docs/07-decision-exploration/19-adr5-session1-scoping.md`

**Scope**: SESSION

### 2. Projection scaling spike (de-risking)

**Why now**: Third consecutive checkpoint with this item unrun. While not blocking, the B→C escape hatch assessment remains anecdotal (5 events in the spike). Running this alongside ADR-5 Session 1 would close the last medium-severity risk that preceded ADR-4.

**Expected artifacts**: `docs/02-experiments/s01-projection-spike/`

**Scope**: HALF-SESSION

### 3. Decide whether pattern inventory is ADR-5 scope or post-ADR

**Why now**: ADR-4 Session 3 Part 1 noted that the pattern inventory (which platform-provided behavioral patterns ship initially) was intentionally deferred. ADR-5 will work with workflow patterns — the right moment to decide whether the broader pattern inventory is ADR-5 scope, a separate document, or deferred to implementation.

**Scope**: QUICK — decision only, no document
