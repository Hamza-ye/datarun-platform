# Project Checkpoint — 2026-04-13 (post-ADR-005)

---

## 1. Bearing

- **Phase**: Architecture complete — entering platform specification
- **Momentum**: `ADVANCING` — ADR-005 decided today. All five foundational ADRs complete. Docs restructured.
- **Last milestone**: ADR-005 (State Progression & Workflow) decided with 9 sub-decisions. Zero envelope changes. Zero type vocabulary changes. All 7 principles confirmed through all 5 ADRs. Documentation restructured (directories renamed, exploration index added, reading guide written).
- **Horizon**: Platform specification document — consolidating all primitives from ADRs 1–5 into a single implementation-grade reference.

The initial architecture sequence is complete. Five ADRs decided over 21 exploration sessions cover: storage (immutable events), identity (four types, accept-and-flag), authorization (assignment-based, sync=scope), configuration (typed shapes, four-layer gradient), and workflow (projection-derived state machines, Pattern Registry, composition rules). The event envelope — the contract governing every record on every device — is finalized at 11 fields, unchanged since ADR-004. The project now transitions from "what are the constraints?" to "what exactly gets built?"

---

## 2. Artifact Map

### **Scenarios (scenarios/)**

| Artifact | Status | Notes |
|----------|--------|-------|
| S00–S14 (15 scenarios) | SETTLED | Phase 1 core. All exercised by at least one ADR. |
| S15, S16, S18 | DEFERRED | Phase 2. Viability Condition 3 holds. |
| S19 — Offline capture and sync | SETTLED | Cross-cutting, central to ADR-1/2. |
| S20–S21 — Composite scenarios | SETTLED | CHV field work + supervisor visit. Validation scenarios. |

### **Behavioral Pattern Catalog (behavioral_patterns.md**

| Artifact | Status | Notes |
|----------|--------|-------|
| Patterns catalog (12 patterns) | SETTLED | P01–P12 extracted from scenarios. Unchanged since creation. |

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
| ADR-003 — Authorization & Sync | DECIDED | 10 (4 structural + 3 strategy-protecting + 3 strategy) | Unchanged |
| ADR-004 — Configuration Boundary | DECIDED | 14 (3 structural + 5 strategy-protecting + 6 strategy) | Unchanged |
| ADR-005 — State Progression | DECIDED | 9 (0 structural + 3 strategy-protecting + 6 strategy) | **NEW — completed** |

**52 sub-decisions** across 5 ADRs. **12 structural constraints** (irreversible), **11 strategy-protecting constraints**, **29 initial strategies** (evolvable).

### **Explorations (exploration/)**

| Artifact | Status | Change since last checkpoint |
|----------|--------|------------------------------|
| 00 — Exploration Framework | SETTLED | Unchanged |
| 01 — Architecture Landscape | SETTLED | Unchanged |
| 02–04 — ADR-1 explorations | SETTLED | Unchanged |
| 05, 07, 09 — ADR-2 explorations | SETTLED | Unchanged |
| 10–12 — ADR-3 explorations | SETTLED | Unchanged |
| 13–18 — ADR-4 explorations (6 files) | SETTLED | Unchanged |
| 19 — ADR-5 Session 1: Scoping & Event Storm | SETTLED | **NEW** |
| 20 — ADR-5 Session 2: Stress Test | SETTLED | **NEW** |
| 21 — ADR-5 Session 3 Part 1: Structural Coherence | SETTLED | **NEW** |
| exploration/README.md — Index | SETTLED | **NEW** — grouped by ADR |

### **Experiments (experiments/)**

| Artifact | Status | Notes |
|----------|--------|-------|
| S00 Event Spike | SETTLED | Python spike + observations. 5-event validation passed. |
| 002 — Scenario/Primitive Walk-through | SETTLED | S04 + S08 walk-throughs. 12 primitives mapped. |
| Projection scaling spike | NOT STARTED | Fourth checkpoint without running. See Open Fronts §2. |

### **Cross-cutting**

| Artifact | Status | Notes |
|----------|--------|-------|
| Access Control Scenario | SETTLED | Unchanged |
| Constraints | SETTLED | Unchanged |
| Viability Assessment | SETTLED | All 3 GO conditions MET |
| Checkpoints | 4 files | This is the fourth |

---

## 3. Decision Board

All five ADRs are decided. No open questions remain within any ADR. This section is now a settled ledger.

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

With all ADRs decided, the blocking dynamics have shifted. No architectural question blocks another. The fronts are now about translating architecture into buildable specifications.

### 1. Platform specification document — CRITICAL PATH

**Why it blocks**: 52 sub-decisions across 5 ADRs define the architecture, but no single document maps them onto implementation-grade primitives. A developer reading this today would need to cross-reference 5 ADRs + 21 exploration documents to reconstruct what the event store, projection engine, conflict detector, shape registry, trigger engine, expression evaluator, pattern registry, and deploy-time validator actually are and how they interact. Without this, implementation cannot begin coherently.

**Unblocked by**: Write the document. All inputs exist. This is consolidation, not discovery.

### 2. Projection scaling spike — DE-RISKING

**Why it matters**: The B→C escape hatch (ADR-1) — adding materialized views alongside the event store — remains validated only against 5 events. Case management subjects (S08) may accumulate 50–100 events. ADR-005 added state machine evaluation as a projection capability. The spike should now validate combined projection load: entity state + workflow state + flag checking + source-chain traversal.

**What's changed**: ADR-005 introduced a revisit trigger: "projection rebuild for a single subject exceeds 200ms on the reference low-end device." The threshold is now explicit but untested.

**Unblocked by**: Build and run the spike against realistic event counts and projection complexity.

### 3. Pattern inventory — SPECIFICATION GAP

**Why it matters**: ADR-005 S5 decided that the Pattern Registry is a platform-fixed primitive and validated four existence-proof patterns (capture_with_review, case_management, multi_step_approval, transfer_with_acknowledgment). But the exact state machine skeletons, parameterization surfaces, and auto-maintained projections for each pattern are explicitly deferred to implementation. This is the largest surface area of specification work remaining.

**Unblocked by**: Part of the platform specification document — not a separate effort.

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
| ADR-001 | B→C: add materialized views | Projection rebuild > 200ms/subject on low-end device | **WATCH** — threshold now explicit (ADR-5), spike not run |
| ADR-002 | Auto-resolution for state-change flags | Flags generated/day ÷ resolved/day > 3 for > 2 consecutive weeks | SAFE |
| ADR-002 | Actor-sequence counter for cross-device | > 10% of conflict flags from same-actor cross-device ordering | SAFE |
| ADR-003 | Add authority_context to envelope | Authority reconstruction > 50ms/event at scale | SAFE |
| ADR-004 | Revisit L3 expressiveness ceiling | > 3 deployments need code for "should be configurable" | SAFE |
| ADR-004 | Revisit complexity budgets | Deploy-time rejections consistently appealed | SAFE |
| ADR-004 | Revisit deprecation-only evolution | > 3 deployments hit field budget from deprecated accumulation | SAFE |
| ADR-004 | Add 7th event type (`status_changed`) | Processing behavior differs from `capture` | **CLOSED** — ADR-5 evaluated, rejected. Vocabulary stays at 6. Escape hatch preserved (append-only vocabulary). |
| ADR-005 | Pattern inventory insufficient | > 2 deployments request custom state machines matching no pattern | SAFE |
| ADR-005 | Source-only flagging misses contamination | > 20% downstream-contamination cases missed by reviewers | SAFE |
| ADR-005 | Auto-resolution masks issues | > 5% auto-resolved flags re-opened within 30 days | SAFE |
| ADR-005 | context.* projection bug | Form behavior diverges from expected state-aware logic | SAFE |

---

## 6. Risk Pulse

### Active risks

| Risk | Severity | Trigger | Mitigation |
|------|----------|---------|------------|
| Projection complexity on low-end devices | MEDIUM | Rebuild > 200ms for 100+ events (ADR-5 explicit threshold) | B→C escape hatch. **Spike not run — fourth checkpoint.** |
| Authority reconstruction slow at scale | MEDIUM | > 50ms/event | Per-actor caching or authority_context envelope extension |
| Accept-and-flag backlog overwhelm | LOW | Bulk reconfiguration + offline workers | Auto-resolution (ADR-5 S9) + batch resolution |
| Platform specification gap grows | LOW | Implementation starts without consolidated reference | Write the specification — all inputs exist |

### Resolved or de-risked since last checkpoint

| Risk | Resolution | When |
|------|-----------|------|
| Workflow cascade on flagged events | Resolved: detect-before-act extended to state derivation (ADR-5 S2), source-only flagging (ADR-5 S7) | ADR-005 (2026-04-13) |
| State machine complexity vs. projection derivation | Resolved: Pattern Registry with fixed skeletons (ADR-5 S5), composition rules prevent conflict (ADR-5 S6) | ADR-005 (2026-04-13) |
| `status_changed` forced as 7th type | Resolved: evaluated and rejected. No processing behavior difference from `capture`. Vocabulary remains at 6. | ADR-005 (2026-04-13) |
| Blocking flags delaying legitimate work | Resolved: per-flag-type severity (ADR-4 S14), flag resolvability classification (ADR-5 S3), auto-resolution (ADR-5 S9) | ADR-004/005 |

---

## 7. Progress Assessment

### The arc

```
ADR-1 (Storage)       ████████████████████ DECIDED ✓
ADR-2 (Identity)      ████████████████████ DECIDED ✓
ADR-3 (Authorization) ████████████████████ DECIDED ✓
ADR-4 (Configuration) ████████████████████ DECIDED ✓
ADR-5 (Workflow)      ████████████████████ DECIDED ✓
```

**Architecture exploration is complete.** 52 sub-decisions across 5 ADRs. 12 structural constraints. 21 exploration documents. 7 principles confirmed. All 3 viability conditions met. Event envelope closed at 11 fields.

### What the last checkpoint's march orders produced

| March Order | Result |
|-------------|--------|
| 1. Begin ADR-005 Session 1 | **DONE** — Session 1 + Session 2 + Session 3 all completed. ADR-005 DECIDED. |
| 2. Projection scaling spike | **NOT DONE** — fourth consecutive checkpoint. |
| 3. Decide pattern inventory scope | **DONE** — ADR-005 S5 explicitly deferred inventory to implementation. |

ADR-5 was the critical path item and was completed in full — including three exploration sessions, a structural coherence audit, and the ADR itself. The scaling spike remains the longest-standing deferred item.

### Event envelope evolution (closed)

| Checkpoint | Envelope fields | Source |
|------------|----------------|--------|
| ADR-1 | 4: id, type, payload, timestamp | Minimum viable |
| ADR-2 | 9: + subject_ref, actor_ref, device_id, device_seq, sync_watermark | Identity + ordering |
| ADR-3 | 9: (no change) | Authority-as-projection |
| ADR-4 | 11: + shape_ref, activity_ref | Configuration + activity |
| ADR-5 | 11: (no change) | State is a projection |

### Documentation restructure (today)

| Old name | New name | Rationale |
| --- | --- | --- |
| `06-scenarios/` | `scenarios/` | Drop misleading numbered prefixes |
| `07-decision-exploration/` | `exploration/` | Shorter, no gaps |
| `01-patterns/README.md` | `behavioral_patterns.md` | The only file in folder `README.md` moved to `docs/`, and folder `01-patterns` removed |
| `02-experiments/` | `experiments/` | Consistent with above |
| `04-risks/` | *(deleted)* | Was empty |
| *(none)* | `exploration/README.md` | Index of all 21 exploration documents grouped by ADR |

README updated with reading guide and accurate status. CLAUDE.md updated. All cross-references in ADRs and exploration files updated. Checkpoint files left untouched (historical snapshots).

---

## 8. March Orders

### 1. Platform specification document

**Why now**: This is the sole critical path item. All architecture is decided. Implementation cannot begin coherently without a single document that maps ADR decisions onto concrete primitives, their interfaces, invariants, and interactions. Every session of delay increases the risk of misinterpretation as the architecture exists across 5 separate ADRs.

**What it consolidates**: Event store, projection engine, identity resolver, conflict detector, scope resolver, shape registry, trigger engine, expression evaluator, pattern registry, deploy-time validator, config packager. For each: what it does, what invariants it maintains, what ADR decisions constrain it, how it interacts with other primitives.

**Expected artifact**: `docs/platform-specification.md` (or `docs/specification/` if it warrants splitting)

**Scope**: WORK (multi-session)

### 2. Projection scaling spike

**Why now**: Fourth consecutive checkpoint. ADR-005 added explicit threshold (200ms/subject on low-end device) and new projection responsibilities (state machine evaluation, source-chain traversal). The spike scope has grown — it should now validate combined load, not just entity projection. Running this alongside or immediately after the specification draft would ground the specification in measured performance.

**Expected artifact**: `docs/experiments/s01-projection-spike/`

**Scope**: SESSION

### 3. Checkpoint prompt adaptation

**Why now**: The `/checkpoint` prompt references old directory names (`06-scenarios/`, `07-decision-exploration/`, etc.) and is structured for an active ADR exploration phase (Decision Board tracking open questions, Open Fronts ranked by ADR blocking potential). With all ADRs decided, the prompt should be updated to reflect the post-architecture phase: specification tracking, implementation readiness, and integration concerns.

**Expected artifact**: Updated `.github/prompts/checkpoint.prompt.md`

**Scope**: SPIKE
