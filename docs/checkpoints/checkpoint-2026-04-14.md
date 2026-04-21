# Project Checkpoint — 2026-04-14 (post-Architecture Description)

---

## 1. Bearing

- **Phase**: Architecture Description complete — entering implementation
- **Momentum**: `ADVANCING` — Architecture Consolidation completed in full (5 steps, 6 working documents, 5 architecture files). Zero gaps. Zero deferred explorations.
- **Last milestone**: Architecture Description written — 5 files in `docs/architecture/` consolidating 11 primitives, 21 contracts, 8 cross-cutting concerns, 29 boundary classifications, and 61 decided positions into a single implementation-grade reference.
- **Horizon**: Implementation — the architecture is fully described and self-contained. The next work is building it.

The Architecture Consolidation sequence ran Steps 1–5 without interruption: Decision Harvest (doc 24, 61 positions) → Boundary Mapping (doc 25, 29 classifications) → Contract Extraction (doc 26, 21 contracts) → Gap Identification (doc 27, zero gaps) → Architecture Description (5 files in `docs/architecture/`). The critical-path item from the previous four checkpoints — "platform specification document" — is done. The description is organized by architectural view (structural, behavioral, boundary), not by decision history or implementation layer.

---

## 2. Artifact Map

### **Scenarios (scenarios/)**

| Artifact | Status | Notes |
|----------|--------|-------|
| S00–S14 (15 scenarios) | SETTLED | Phase 1 core. All exercised by at least one ADR. |
| S15, S16, S18 | DEFERRED | Phase 2. Viability Condition 3 holds. |
| S19 — Offline capture and sync | SETTLED | Cross-cutting, central to ADR-1/2. |
| S20–S21 — Composite scenarios | SETTLED | CHV field work + supervisor visit. Validation scenarios. |

### **Behavioral Pattern Catalog (behavioral_patterns.md)**

| Artifact | Status | Notes |
|----------|--------|-------|
| Patterns catalog (12 patterns) | SETTLED | P01–P12 extracted from scenarios. Unchanged. |

### **Principles (principles.md)**

| Principle | Status | Last tested |
|-----------|--------|-------------|
| P1: Offline is the default | Confirmed | ADR-001, ADR-002, ADR-005 |
| P2: Configuration has boundaries | Confirmed | ADR-004, ADR-005 |
| P3: Records are append-only | Confirmed | ADR-001, ADR-002, ADR-005 |
| P4: Patterns compose | Confirmed | ADR-001, ADR-002, ADR-003, ADR-005 |
| P5: Conflict is surfaced | Confirmed | ADR-001, ADR-002, ADR-005 |
| P6: Authority is contextual and auditable | Confirmed | ADR-003, ADR-005 |
| P7: Simplest scenario stays simple | Confirmed | ADR-001, ADR-002, ADR-004, ADR-005 |

All 7 confirmed through all 5 ADRs. No longer provisional.

### **ADRs (adrs/)**

| Artifact | Status | Sub-decisions | Change since last checkpoint |
|----------|--------|---------------|------------------------------|
| ADR-001 — Offline Data Model | DECIDED | 5 structural + 3 strategy | Unchanged |
| ADR-002 — Identity & Conflict | DECIDED | 14 | Unchanged |
| ADR-003 — Authorization & Sync | DECIDED | 10 | Unchanged |
| ADR-004 — Configuration Boundary | DECIDED | 14 | Unchanged |
| ADR-005 — State Progression | DECIDED | 9 | Unchanged |

**52 sub-decisions** across 5 ADRs. **12 structural constraints** (irreversible), **11 strategy-protecting constraints**, **29 initial strategies** (evolvable).

### **Architecture Description (architecture/)**

| Artifact | Status | Content |
|----------|--------|---------|
| [README.md](../architecture/README.md) | SETTLED | Vision commitments, confirmed principles, glossary (14 terms), reading guide, traceability notation |
| [primitives.md](../architecture/primitives.md) | SETTLED | 11 primitives — invariants, constraints, config surfaces, escape hatches. Binding summary: 61 positions traced. |
| [patterns.md](../architecture/patterns.md) | **NEW** | 4 workflow pattern specifications: state machines, transitions, roles, projections, parameterization. Inventory summary + consistency checks. entity_lifecycle deferred. |
| [contracts.md](../architecture/contracts.md) | SETTLED | 21 inter-primitive contracts (15 decided, 6 spec-grade). Dependency structure. Circular dependency analysis. Interface declarations. |
| [cross-cutting.md](../architecture/cross-cutting.md) | SETTLED | 8 cross-cutting concerns: event envelope (11 fields), accept-and-flag (8 stages), detect-before-act (3 levels), four-layer gradient, config delivery pipeline, sync contract (8 guarantees), unified flag catalog (9 categories), aggregation interface. |
| [boundary.md](../architecture/boundary.md) | SETTLED | 29 boundary classifications (5 spec-grade, 15 impl-grade, 8 outside, 1 resolved). 6 escape hatches. Complete position trace. Completion verification passed. |

### **Explorations (exploration/)**

| Artifact | Status | Change since last checkpoint |
|----------|--------|------------------------------|
| 00–21 — Framework + ADR explorations | SETTLED | Unchanged |
| 22 — Platform Primitives Inventory | SETTLED | Unchanged (input to consolidation) |
| 23 — Consolidation Framework | SETTLED | Unchanged (methodology) |
| 24 — Decision Harvest | SETTLED | 61 positions harvested |
| 25 — Boundary Mapping | SETTLED | 29 classifications |
| 26 — Contract Extraction | SETTLED | 21 contracts |
| 27 — Gap Identification | SETTLED | zero gaps confirmed |
| 28 — Pattern Inventory Walk-through | **NEW** | 4 patterns formally specified. S00 no-pattern validated. entity_lifecycle deferred. |
| exploration/README.md | SETTLED | **UPDATED** — pattern inventory section added |

### **Experiments (experiments/)**

| Artifact | Status | Notes |
|----------|--------|-------|
| S00 Event Spike | SETTLED | Python spike + observations. 5-event validation passed. |
| 002 — Scenario/Primitive Walk-through | SETTLED | S04 + S08 walk-throughs. 12 primitives mapped. |
| Projection scaling spike | **DONE** | O(n) linear scaling confirmed. P99 3.0ms Python @ 200 events. JVM projected 0.1–0.3ms. See [s01-projection-spike/](../experiments/s01-projection-spike/). |

### **Cross-cutting**

| Artifact | Status | Notes |
|----------|--------|-------|
| Access Control Scenario | SETTLED | Unchanged |
| Constraints | SETTLED | Unchanged |
| Viability Assessment | SETTLED | All 3 GO conditions MET |
| Checkpoints | 5 files | This is the fifth |

---

## 3. Decision Board

All five ADRs are decided. No open questions remain within any ADR. The Architecture Description consolidates all decisions into a single reference. This section is a settled ledger.

### Architecture at a glance

| Layer | What's decided | Key ADR |
|-------|---------------|---------|
| **Storage** | Immutable events, append-only, client-generated UUIDs. Event envelope: 11 fields. | ADR-001 |
| **Identity** | 4 typed categories (subject, actor, assignment, process). Merge=alias, no unmerge, detect-before-act. Accept-and-flag — events never rejected. | ADR-002 |
| **Authorization** | Assignment-based access, sync=scope. Authority-as-projection. Alias-respects-original-scope. | ADR-003 |
| **Configuration** | Typed shapes, platform-fixed 6-type vocabulary. Four-layer gradient (L0–L3). Hard complexity budgets. Triggers server-only, DAG depth 2. | ADR-004 |
| **Workflow** | Projection-derived state machines. Pattern Registry. 5 composition rules. Source-only flagging. `context.*` scope (7 properties). Auto-resolution as L3b sub-type. | ADR-005 |

### Structural invariants (cannot change without data migration)

| Invariant | Source |
|-----------|--------|
| All writes append-only | ADR-1 S1 |
| Typed immutable events as storage primitive | ADR-1 S2 |
| Client-generated UUIDs | ADR-1 S3 |
| Sync unit = immutable event | ADR-1 S4 |
| Event envelope: 11 fields (id, type, shape_ref, [activity_ref], subject_ref, actor_ref, device_id, device_seq, sync_watermark, timestamp, payload) | ADR-1 S5 + ADR-2 + ADR-4 |
| Causal ordering: device_seq + sync_watermark | ADR-2 S1 |
| 4 typed identity categories | ADR-2 S2 |
| Merge = alias in projection, never rewrite | ADR-2 S6 |
| Assignment-based access, sync=scope | ADR-3 S1/S2 |
| Authority-as-projection, no envelope extension | ADR-3 S3 |
| shape_ref mandatory, activity_ref optional | ADR-4 S1/S2 |
| Platform-fixed type vocabulary (6 types, append-only) | ADR-4 S3 |

---

## 4. Open Fronts

The critical-path blocker from the last four checkpoints (platform specification / Architecture Description) is resolved. What remains is de-risking and implementation preparation.

### ~~1. Projection scaling spike~~ — RESOLVED

**Resolved**: 2026-04-14. Spike completed in `docs/experiments/s01-projection-spike/`. Results: O(n) linear scaling confirmed across 1–200 events. P99 3.0ms in Python at 200 events. JVM projected 0.1–0.3ms. B→C escape hatch directionally validated — pure-projection architecture viable at realistic scale.

### ~~2. Pattern inventory~~ — RESOLVED

**Resolved**: 2026-04-14. Four patterns formally specified with full state machines, transitions, roles, projections, and parameterization in [architecture/patterns.md](../architecture/patterns.md). Walk-through analysis in [exploration/28](../exploration/28-pattern-inventory-walkthrough.md). entity_lifecycle identified as separate 5th pattern, deferred to platform evolution. S00 no-pattern validated.

### ~~3. Checkpoint prompt adaptation~~ — RESOLVED

**Resolved**: 2026-04-14. Updated `.github/prompts/checkpoint.prompt.md` — phase context, input list, artifact categories, open fronts examples, and arc template all reflect post-architecture/implementation phase. Removed references to `docs/specifications/` directory and ADR exploration phase front types.

---

## 5. Condition Ledger

### 5a. Viability GO Conditions

| # | Condition | Status | Evidence |
|---|-----------|--------|----------|
| 1 | Configuration boundary must be among the first major architecture decisions. | **MET** | ADR-004 decided. Four-layer gradient, complexity budgets, explicit L3→code boundary. |
| 2 | S12 requires strict scoping to prevent unbounded rules engine. | **MET** | ADR-004 S5 (server-only), S12 (depth-2 DAG, 5 triggers/type, 50/deployment). |
| 3 | Phase 2 scenarios (S15, S16, S18) remain deferred. | **MET** | All three scenarios marked DEFERRED. No ADR references them. |

All three GO conditions MET since checkpoint 2026-04-13. No change.

### 5b. ADR Escape Hatch Conditions

| ADR | Escape Hatch | Activation Condition | Status |
|-----|-------------|---------------------|--------|
| ADR-001 | B→C: add materialized views | Projection rebuild > 200ms/subject on low-end device | **DIRECTIONALLY VALIDATED** — spike confirms O(n) linear, P99 3.0ms Python @ 200 events. JVM projected well under threshold. |
| ADR-002 | Auto-resolution for state-change flags | Flags generated/day ÷ resolved/day > 3 for > 2 consecutive weeks | SAFE |
| ADR-002 | Actor-sequence counter for cross-device | > 10% of conflict flags from same-actor cross-device ordering | SAFE |
| ADR-003 | Add authority_context to envelope | Authority reconstruction > 50ms/event at scale | SAFE |
| ADR-004 | Revisit L3 expressiveness ceiling | > 3 deployments need code for "should be configurable" | SAFE |
| ADR-004 | Revisit complexity budgets | Deploy-time rejections consistently appealed | SAFE |
| ADR-004 | Revisit deprecation-only evolution | > 3 deployments hit field budget from deprecated accumulation | SAFE |
| ADR-004 | Add 7th event type (`status_changed`) | Processing behavior differs from `capture` | **CLOSED** — ADR-5 evaluated, rejected. Vocabulary stays at 6. |
| ADR-005 | Pattern inventory insufficient | > 2 deployments request custom state machines matching no pattern | SAFE |
| ADR-005 | Source-only flagging misses contamination | > 20% downstream-contamination cases missed by reviewers | SAFE |
| ADR-005 | Auto-resolution masks issues | > 5% auto-resolved flags re-opened within 30 days | SAFE |
| ADR-005 | context.* projection bug | Form behavior diverges from expected state-aware logic | SAFE |

---

## 6. Risk Pulse

### Active risks

| Risk | Severity | Trigger | Mitigation |
|------|----------|---------|------------|
| ~~Projection complexity on low-end devices~~ | ~~MEDIUM~~ | ~~Rebuild > 200ms for 100+ events~~ | **RESOLVED** — spike confirms O(n) linear, P99 3.0ms Python @ 200 events. Pure-projection viable. |
| Authority reconstruction slow at scale | MEDIUM | > 50ms/event | Per-actor caching or authority_context envelope extension |
| Accept-and-flag backlog overwhelm | LOW | Bulk reconfiguration + offline workers | Auto-resolution (ADR-5 S9) + batch resolution |

### Resolved since last checkpoint

| Risk | Resolution | When |
|------|-----------|------|
| Platform specification gap grows | **RESOLVED**: Architecture Description written — 6 files in `docs/architecture/`, self-contained, all 61 positions traced, all 21 contracts stated, all 29 boundaries declared. | 2026-04-14 |
| Projection complexity on low-end devices | **RESOLVED**: Spike confirms O(n) linear scaling, P99 3.0ms Python @ 200 events. JVM projected 0.1–0.3ms. Pure-projection architecture viable at realistic scale. | 2026-04-14 |
| Pattern inventory specification gap | **RESOLVED**: 4 patterns formally specified in architecture/patterns.md. entity_lifecycle deferred. | 2026-04-14 |

---

## 7. Progress Assessment

### The arc

```
ADR-1 (Storage)          ████████████████████ DECIDED ✓
ADR-2 (Identity)         ████████████████████ DECIDED ✓
ADR-3 (Authorization)    ████████████████████ DECIDED ✓
ADR-4 (Configuration)    ████████████████████ DECIDED ✓
ADR-5 (Workflow)         ████████████████████ DECIDED ✓
Consolidation Step 1     ████████████████████ COMPLETE ✓  (Decision Harvest)
Consolidation Step 2     ████████████████████ COMPLETE ✓  (Boundary Mapping)
Consolidation Step 3     ████████████████████ COMPLETE ✓  (Contract Extraction)
Consolidation Step 4     ████████████████████ COMPLETE ✓  (Gap Identification)
Consolidation Step 5     ████████████████████ COMPLETE ✓  (Architecture Description)
```

**Architecture Consolidation is complete.** The platform's architecture is fully described in a single, self-contained reference. Every decided position is traced. Every contract is stated. Every boundary is declared. The project transitions from "what gets built?" to "build it."

### What the last checkpoint's march orders produced

| March Order | Result |
|-------------|--------|
| 1. Platform specification document | **DONE** — Architecture Consolidation ran all 5 steps. Produced 4 working documents (docs 24–27) and 5 architecture files in `docs/architecture/`. Zero gaps. All completion tests passed. |
| 2. Projection scaling spike | **DONE** — spike completed same day as checkpoint. O(n) linear, P99 3.0ms Python. See [s01-projection-spike/](../experiments/s01-projection-spike/). |
| 3. Checkpoint prompt adaptation | **DONE** — updated `.github/prompts/checkpoint.prompt.md` for implementation phase. |

The critical-path item was completed comprehensively — not as a single specification document, but through a rigorous 5-step consolidation methodology that harvested decisions, mapped boundaries, extracted contracts, verified zero gaps, and produced a view-organized architecture description. The result is stronger than a single spec file would have been.

### Architecture Consolidation sequence (today)

| Step | Document | Key output |
|------|----------|------------|
| Step 1: Decision Harvest | [doc 24](../exploration/24-decision-harvest.md) | 61 positions (52 explicit + 4 strategies + 5 emergent) |
| Step 2: Boundary Mapping | [doc 25](../exploration/25-boundary-mapping.md) | 29 classifications (5 spec, 15 impl, 8 outside, 1 resolved) |
| Step 3: Contract Extraction | [doc 26](../exploration/26-contract-extraction.md) | 21 contracts (15 decided, 6 spec-grade). Zero gaps. |
| Step 4: Gap Identification | [doc 27](../exploration/27-gap-identification.md) | Zero gaps across all sub-steps. Ready for Step 5. |
| Step 5: Architecture Description | [docs/architecture/](../architecture/) | 5 files. Structural + behavioral + boundary views. Self-contained. |

### Event envelope evolution (closed)

| Checkpoint | Envelope fields | Source |
|------------|----------------|--------|
| ADR-1 | 4: id, type, payload, timestamp | Minimum viable |
| ADR-2 | 9: + subject_ref, actor_ref, device_id, device_seq, sync_watermark | Identity + ordering |
| ADR-3 | 9: (no change) | Authority-as-projection |
| ADR-4 | 11: + shape_ref, activity_ref | Configuration + activity |
| ADR-5 | 11: (no change) | State is a projection |

Envelope has been stable for 3 ADRs and the full consolidation sequence. Closed.

---

## 8. March Orders

### ~~1. Projection scaling spike~~ — DONE

Completed same day as checkpoint. See [s01-projection-spike/](../experiments/s01-projection-spike/).

### ~~1b. Pattern inventory~~ — DONE

Completed same day as checkpoint. See [architecture/patterns.md](../architecture/patterns.md) and [exploration/28](../exploration/28-pattern-inventory-walkthrough.md).

### 2. Implementation planning

**Why now**: The Architecture Description is complete and self-contained. A developer (or team) can read `docs/architecture/` and understand what to build. The next step is deciding *how* to build it: technology choices, module boundaries, build order, and what the first deliverable looks like.

**What it involves**: Technology selection (language, storage, sync protocol). Module decomposition mapped to primitives. Build order (what ships first — likely Event Store + Projection Engine + Shape Registry for S00). Spike candidates for spec-grade items (SG-1 through SG-5).

**Expected artifact**: Implementation plan document or ADR-level decision on technology stack.

**Scope**: WORK (multi-session)

### ~~3. Checkpoint prompt adaptation~~ — DONE

Completed 2026-04-14. Updated `.github/prompts/checkpoint.prompt.md` for post-architecture phase.
