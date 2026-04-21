# Project Checkpoint — 2026-04-12

---

## 1. Bearing

- **Phase**: Architecture Exploration — three foundational ADRs complete, entering configuration boundary
- **Momentum**: `ADVANCING` — ADR-003 completed (exploration + draft) since last checkpoint. Three of five ADRs now decided or drafted.
- **Last milestone**: ADR-003 (Authorization & Selective Sync) drafted with 10 sub-decisions. Irreversibility filter methodology proven and codified into the exploration framework.
- **Horizon**: ADR-004 (Configuration Paradigm & Boundary) — the highest-risk, most consequential remaining decision. V2 ("set up, not built") lives or dies here.

The platform's infrastructure layer is now architecturally resolved: storage primitive (immutable events), identity model (four typed categories, alias merges, device-sequence ordering), and authorization model (assignment-based, sync=scope, authority-as-projection). ADR-003 confirmed that the project is entering a zone where **fewer decisions touch the event envelope** — most remaining choices are evolvable strategies rather than permanent commitments. The one exception: ADR-004's schema versioning scheme, which will be stored in every event.

**Shift in decision character**: ADRs 1–3 asked "how does the engine work?" ADR-4 asks "where does the engine end and the deployment begin?" This is a product-architecture boundary judgment, not a pure infrastructure decision. The exploration methodology must adapt accordingly.

---

## 2. Artifact Map

### **Scenarios (06-scenarios/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| S00 — Basic structured capture | Scenario | SETTLED | Simplicity validation target across all ADRs. |
| S01 — Entity-linked capture | Scenario | SETTLED | Core identity stress scenario for ADR-2. |
| S02 — Periodic reporting | Scenario | SETTLED | Phase 1. |
| S03 — User-based assignment | Scenario | SETTLED | Actor identity axis. Key input for ADR-3 (S20 group). |
| S04 — Supervisor review | Scenario | SETTLED | Phase 1 workflow scenario. Walk-through completed. |
| S05 — Supervision audit visits | Scenario | SETTLED | Hierarchical visibility. ADR-3 input. |
| S06 — Entity registry lifecycle | Scenario | SETTLED | Split/merge/deactivation stress for ADR-2. |
| S07 — Resource distribution | Scenario | SETTLED | Process identity type. Multi-level handoffs for ADR-3. |
| S08 — Case management | Scenario | SETTLED | Long-running case stress. Walk-through completed. |
| S09 — Coordinated campaign | Scenario | SETTLED | Multi-assignment configuration. ADR-3/4 input. |
| S10 — Dynamic targeting | Scenario | SETTLED | Phase 1. |
| S11 — Multi-step approval | Scenario | SETTLED | Phase 1. ADR-5 key input. |
| S12 — Event-triggered actions | Scenario | SETTLED | Viability Condition 2 mandates strict scoping. ADR-4 key risk. |
| S13 — Cross-flow linking | Scenario | SETTLED | Phase 1. |
| S14 — Multi-level distribution | Scenario | SETTLED | Phase 1. ADR-3 input. |
| S15 — Cross-program overlays | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S16 — Emergency rapid response | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S18 — Advanced analytics | Scenario | DEFERRED | Phase 2. Viability Condition 3. |
| S19 — Offline capture and sync | Scenario | SETTLED | Central conflict/ordering scenario for ADR-2. ADR-3 input. |
| S20 — CHV field operations | Scenario | SETTLED | Role-based access. ADR-3 key input. |
| S21 — CHV supervisor operations | Scenario | SETTLED | Phase 1. Multi-org/multi-program. |

### **Patterns (01-patterns/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| Behavioral patterns catalog | Patterns | SETTLED | 12 recurring primitives identified across 21 scenarios. |

### **Principles (principles.md)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| P1 — Offline is the default | Principle | CONFIRMED | ADR-001, ADR-002, ADR-003. File not updated. |
| P2 — Set up, not built | Principle | HYPOTHESIS | ADR-004 is the test. |
| P3 — Records are append-only | Principle | CONFIRMED | ADR-001, ADR-002. File not updated. |
| P4 — Composition over special mechanisms | Principle | CONFIRMED | ADR-003 (no new structural mechanisms — reuses accept-and-flag, detect-before-act). File not updated. |
| P5 — Conflict is surfaced | Principle | CONFIRMED | ADR-001, ADR-002. File not updated. |
| P6 — Authority is contextual and auditable | Principle | CONFIRMED | ADR-003 (full audit chain: event → actor → assignments → scope → flag). File not updated. |
| P7 — Simplest scenario stays simple | Principle | CONFIRMED | ADR-001, ADR-002, ADR-003 (S00 adds zero auth interactions, zero new envelope fields). File not updated. |

### **ADRs (adrs/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| ADR-001 — Offline Data Model | ADR | DECIDED | 10 sub-decisions. Promoted 2026-04-10. |
| ADR-002 — Identity & Conflict | ADR | DECIDED | 14 sub-decisions. Promoted 2026-04-10. |
| ADR-003 — Authorization & Sync | ADR | DRAFT | 10 sub-decisions. 4 structural constraints, 3 strategy-protecting, 3 initial strategies. Drafted 2026-04-12. |
| ADR-004 — Configuration Paradigm | ADR | NOT STARTED | Viability Condition 1. Highest-risk remaining decision. |
| ADR-005 — State Progression | ADR | NOT STARTED | Depends on ADR-001 through ADR-004. |

### **Explorations (07-decision-exploration/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| 00 — Exploration Framework | Framework | SETTLED | Three-phase methodology + irreversibility filter (added from ADR-3). |
| 01 — Architecture Landscape | Exploration | SETTLED | Dependency map, 5 decision blocks, tension analysis. |
| 02 — ADR-1 Offline Data Model | Exploration | SETTLED | 5 sub-decisions, 3 options evaluated. |
| 03 — ADR-1 Forward Projection | Exploration | SETTLED | Irreversibility gradient established. |
| 04 — Decision Audit | Exploration | SETTLED | 6 issues found, 5 changes applied. |
| 05 — ADR-2 Phase 1 Event Storm | Exploration | SETTLED | 5 scenarios, 4 identity types, 7 conflict types, 13 questions. |
| 07 — ADR-2 Phase 2 Stress Test | Exploration | SETTLED | 19 findings, 3 mechanisms, 8 missing workflow paths. |
| 09 — ADR-2 Phase 3 Classification | Exploration | SETTLED | 15 Bucket-1 constraints, 14 Bucket-2 strategies, 5 Bucket-3 deferrals. |
| 10 — ADR-3 Phase 1 Policy Scenarios | Exploration | SETTLED | 6 scenarios, 13 hot spots, 6 envelope questions. |
| 11 — ADR-3 Phase 2 Stress Test | Exploration | SETTLED | 5 mechanisms tested, ~25 attack paths. |
| 12 — ADR-3 Course Correction | Exploration | SETTLED | Irreversibility filter proven. Stress test reconciled against Option (c). 4 genuine constraints found. |

### **Experiments (02-experiments/)**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| 002 — Scenario/Primitive Walk-through | Experiment | SETTLED | 12 primitives mapped across scenarios. |
| S00 Event Spike | Experiment | SETTLED | Python spike + observations. All 5 steps passed. |

### **Cross-cutting**

| Artifact | Type | Status | Notes |
|----------|------|--------|-------|
| Access Control Scenario | Cross-cut | SETTLED | Domain-pure rewrite. Feeds ADR-003. |
| Constraints | Cross-cut | SETTLED | Operational context and boundaries. |
| Viability Assessment | Cross-cut | SETTLED | GO conditions, phasing, risks. |

---

## 3. Decision Board

#### ADR-001: Offline Data Model — DECIDED

| Decision | Status | Confidence |
|----------|--------|------------|
| S1: Records are append-only | SETTLED | HIGH |
| S2: Atomic unit = typed immutable event | SETTLED | HIGH |
| S3: Client-generated UUIDs | SETTLED | HIGH |
| S4: Sync unit = immutable event | SETTLED | HIGH |
| S5: Conflict detection must surface | SETTLED | HIGH |
| S6: Event log = single source of truth | SETTLED | HIGH |
| S7: Projections rebuildable from events | SETTLED | HIGH |
| S8: Event envelope minimum fields | SETTLED | HIGH |
| Write-path discipline | SETTLED | HIGH |
| Projection rebuild scope | OPEN | MEDIUM — resolves in ADR-003 (S8) |

Escape hatch: B→C (add materialized views alongside event store). Safe — spike validated, scaling spike not yet run.

#### ADR-002: Identity & Conflict — DECIDED

| Decision | Status | Confidence |
|----------|--------|------------|
| S1: Causal ordering (device_seq + watermark) | SETTLED | HIGH |
| S2: Typed identity references (4 categories) | SETTLED | HIGH |
| S3: device_time is advisory | SETTLED | HIGH |
| S4: Sequence/watermark persistence | SETTLED | HIGH |
| S5: device_id is hardware-bound | SETTLED | HIGH |
| S6: Merge = alias in projection | SETTLED | HIGH |
| S7: No unmerge | SETTLED | HIGH |
| S8: Split freezes history | SETTLED | HIGH |
| S9: Lineage graph acyclicity | SETTLED | HIGH |
| S10: Merge/split are online-only | SETTLED | HIGH |
| S11: Single-writer conflict resolution | SETTLED | HIGH |
| S12: Detect before act | SETTLED | HIGH |
| S13: Detection uses raw references | SETTLED | HIGH |
| S14: Events never rejected for staleness | SETTLED | HIGH |

No escape hatches. All decisions are Bucket 1 constraints.

#### ADR-003: Authorization & Sync — DRAFT

| Decision | Status | Confidence | Classification |
|----------|--------|------------|----------------|
| S1: Assignment-based access control | SETTLED | HIGH | Structural constraint |
| S2: Sync scope = access scope | SETTLED | HIGH | Structural constraint |
| S3: No new envelope fields (authority-as-projection) | SETTLED | HIGH | Structural constraint |
| S4: Alias-respects-original-scope | SETTLED | HIGH | Structural constraint |
| S5: Scope-containment on assignment creation | SETTLED | HIGH | Strategy-protecting |
| S6: Conflict resolution is online-only | SETTLED | HIGH | Strategy-protecting |
| S7: Detect-before-act extends to auth flags | SETTLED | HIGH | Strategy-protecting |
| S8: Tiered projection location | SETTLED | MEDIUM | Initial strategy |
| S9: Authorization staleness handling | SETTLED | MEDIUM | Initial strategy |
| S10: Selective-retain on scope change | SETTLED | MEDIUM | Initial strategy |

Escape hatch: S3 → add `authority_context` to envelope via ADR-001 S5 if projection-based reconstruction proves slow. Trigger: >50ms/event.

#### ADR-004: Configuration Paradigm — NOT STARTED

Known sub-decisions (accumulated from ADR-1/2/3 deferrals):

| Question | Source | Nature |
|----------|--------|--------|
| Event type vocabulary ownership (platform vs. deployment) | ADR-1 deferral | Boundary |
| Schema/shape definition & versioning (A/B/C) | ADR-1 envelope gap | Technical — touches envelope |
| Configuration versioning & on-device coexistence | Offline constraint | Technical |
| T2 boundary line (where config stops, code begins) | Core tension | Judgment |
| S12 scoping (event-triggered actions) | Viability Condition 2 | Boundary + Risk |
| Domain-specific conflict rule configuration | ADR-2 deferral | Boundary |
| Role-action permission table definition & delivery | ADR-3 S1 deferral | Boundary |
| Per-flag-type severity configuration (blocking/informational) | ADR-3 S7 deferral | Boundary |
| Scope type extensibility (geographic, subject-based, query-based) | ADR-3 deferral | Boundary |
| Assignment configuration for new role types | ADR-3 deferral | Boundary |
| Sensitive-subject classification mechanism | ADR-3 deferral | Boundary |
| Activity model & correlation metadata | ADR-1 exploration | Technical |

**Key observation**: Half of these are boundary judgment calls, not technical mechanism choices. The event-storm methodology alone won't carry ADR-4.

#### ADR-005: State Progression — NOT STARTED

Blocked by ADR-004. Known sub-decisions: state machines, data vs. workflow events, multi-step composition, offline workflow conflicts, downstream cascade.

---

## 4. Open Fronts

Ranked by blocking potential.

### 1. Configuration boundary (T2 tension) — CRITICAL PATH

**Why it blocks**: Viability Condition 1. V2 ("set up, not built") is the platform's central promise to deploying organizations. ADR-004 must draw the line between what deployers configure and what requires platform development. Every scenario's practical usability depends on where this line falls.

**Unblocked by**: Begin ADR-4 Session 1 (scoping + prior art). The four-session plan:
1. Scoping & prior art (DHIS2/CommCare/ODK configuration boundary analysis)
2. Scenario walk-throughs through configuration lens (S00, S06b, S09, S12, S20)
3. Boundary drawing + irreversibility filter + S12 stress test
4. ADR writing + S00 simplicity validation

**ADR home**: ADR-004

### 2. S12 (event-triggered actions) scoping — HIGH RISK

**Why it blocks**: Viability Condition 2 mandates strict scoping. Without clear limits, S12 becomes an unbounded rules engine that collapses the configuration boundary. This is entangled with ADR-004's core question.

**Unblocked by**: ADR-4 Session 3 must stress-test S12 specifically.

**ADR home**: ADR-004

### 3. Schema versioning scheme — ENVELOPE DECISION

**Why it blocks**: ADR-001 acknowledges every event should carry a shape version, but the versioning *scheme* (envelope tag vs. self-describing payload vs. registry reference) is deferred. This is one of ADR-4's few genuinely irreversible decisions — it touches stored events.

**Unblocked by**: ADR-4 exploration must evaluate options A/B/C.

**ADR home**: ADR-004

### 4. Workflow model (state machines) — BLOCKED

**Why it blocks**: ADR-005 cannot start until ADR-004 decides the configuration boundary. S04, S08, S11, S14 all depend on how workflows are defined.

**Unblocked by**: Complete ADR-004 → then explore ADR-005.

**ADR home**: ADR-005

### 5. ADR-003 readiness audit — OPEN

**Why it blocks**: ADR-003 is Draft but has not been through a formal readiness audit. ADR-001 and ADR-002 both had readiness audits before promotion to DECIDED. Should be done before ADR-4 builds on ADR-3.

**Unblocked by**: Run readiness audit on ADR-003.

**ADR home**: ADR-003

### 6. Projection scaling spike — DE-RISKING

**Why it blocks**: ADR-002 Phase 2 identified projection rebuild cost as a potential concern for subjects with 100–500 events. The S00 spike validated 5 events (trivially fast). Not blocking ADR-4 but informs the B→C escape hatch assessment.

**Unblocked by**: Build and run the spike.

**ADR home**: Strategy (not a constraint)

### 7. Principles.md not updated

**Why it matters**: P1/P3/P5/P7 confirmed by ADR-1/2; P4/P6 confirmed by ADR-3. The file still shows all as "Hypothesis." Low urgency but growing drift.

**Unblocked by**: Update principles.md statuses.

---

## 5. Condition Ledger

### 5a. Viability GO Conditions

| # | Condition | Status | Evidence |
|---|-----------|--------|----------|
| 1 | Configuration boundary must be the first major architecture decision. | NOT MET | ADR-004 has not started. ADR-001/002/003 were completed first by dependency order. The ordering is correct — the condition is being approached via the dependency chain. ADR-004 is now unblocked. |
| 2 | Scenario 12 requires strict scoping to prevent unbounded rules engine. | NOT ADDRESSED | No exploration specific to S12 scoping. Will be addressed in ADR-4 Session 3. |
| 3 | Phase 2 scenarios (S15, S16, S18) remain deferred. | MET | All three scenarios marked DEFERRED. No ADR work references them. |

### 5b. ADR Escape Hatch Conditions

| ADR | Escape Hatch | Activation Condition | Status |
|-----|-------------|---------------------|--------|
| ADR-001 | B→C: add materialized views | Projection complexity unmanageable on low-end Android (<200ms target missed) | SAFE — spike passed for 5 events. Scaling spike not yet run. |
| ADR-002 | Auto-resolution for state-change flags | Flags generated/day ÷ resolved/day > 3 for > 2 consecutive weeks | SAFE — no deployment exists. |
| ADR-002 | Actor-sequence counter for cross-device | > 10% of conflict flags traced to same-actor cross-device ordering | SAFE — no deployment exists. |
| ADR-003 | Add authority_context to envelope | Authority reconstruction > 50ms/event at scale | SAFE — no deployment exists. Retreat path is cheap (ADR-001 S5). |

---

## 6. Risk Pulse

| Risk | Severity | Trigger | Mitigation | Change since last checkpoint |
|------|----------|---------|------------|------------------------------|
| Configuration boundary drawn too wide | HIGH | ADR-004 exploration | Viability Condition 1 + T2 tension + prior art analysis | **UNCHANGED** — not yet addressed |
| S12 becoming unbounded rules engine | HIGH | ADR-004 exploration | Viability Condition 2 + stress test in Session 3 | **UNCHANGED** — not yet addressed |
| Projection complexity on low-end devices | MEDIUM | Screen load > 200ms for 100+ events | B→C escape hatch + scaling spike | **UNCHANGED** — spike not yet run |
| Authority reconstruction slow at scale | MEDIUM | > 50ms/event | Per-actor caching or add authority_context to envelope | **NEW** — introduced by ADR-003 S3 |
| Accept-and-flag backlog overwhelm | MEDIUM | Bulk reconfiguration + offline workers | Batch resolution + auto-resolution | **UNCHANGED** |
| Blocking flags delaying legitimate work | LOW | > 5% blocking flags resolved as "valid" | Per-flag-type severity configuration (ADR-4) | **NEW** — introduced by ADR-003 S7 |

**Resolved since last checkpoint**:

| Risk | Resolution | When |
|------|-----------|------|
| Authorization model ambiguity | Resolved: assignment-based access, sync=scope, authority-as-projection | ADR-003 (2026-04-12) |
| Sync scope computation unknown | Resolved: scope-containment test, server-computed per actor | ADR-003 (2026-04-12) |
| Who resolves conflicts (authorization) | Resolved: single-writer (ADR-002 S11) + online-only (ADR-003 S6) | ADR-003 (2026-04-12) |
| Privilege escalation via assignment manipulation | Resolved: scope-containment invariant on AssignmentCreated (ADR-003 S5) | ADR-003 (2026-04-12) |

---

## 7. Methodology Note: ADR-4 Requires Adaptation

The three-phase exploration framework (event storm → stress test → classification) served ADRs 1–3 well because they were infrastructure decisions with clear irreversibility boundaries (envelope fields, stored state, identity references).

ADR-4 is different:
- **Half its questions are boundary judgments**, not technical mechanism choices.
- **The irreversibility surface is narrow** — likely only schema versioning touches the envelope.
- **The risk is judgment error**, not mechanism failure: draw the boundary too tight (everything is code) and V2 fails; draw it too wide (everything is configuration) and you get an inner-platform.
- **Prior art matters more** than in previous ADRs — DHIS2, CommCare, and ODK have all drawn this line and the lessons are informative.

The adapted approach for ADR-4:
1. **Session 1**: Scoping + prior art (where did similar platforms draw the line, what broke)
2. **Session 2**: Scenario walk-throughs through the configuration lens (what does a deployer configure?)
3. **Session 3**: Boundary drawing + irreversibility filter + S12 stress test
4. **Session 4**: ADR writing + S00 simplicity validation + readiness audit

This maintains the project's methodological rigor while adapting the discovery method to the decision's character.

---

## 8. March Orders

1. **Run ADR-003 readiness audit**
   Why now: ADR-003 is Draft. ADR-004 builds on it. A quick audit ensures the foundation is sound before proceeding.
   Scope: HALF-SESSION

2. **Begin ADR-004 Session 1: Scoping & Prior Art**
   Why now: ADR-004 is the critical path. All three upstream ADRs are complete. Viability Condition 1 mandates this.
   Expected artifacts: `docs/07-decision-exploration/13-adr4-session1-scoping.md`
   Scope: SESSION

3. **Update principles.md**
   Why now: Growing drift — 6 of 7 principles confirmed but file unchanged. Low effort, high hygiene value.
   Scope: QUICK

4. **Projection scaling spike (optional, de-risking)**
   Why now: Informs B→C escape hatch assessment. Not blocking ADR-4 but reduces medium-severity risk.
   Expected artifacts: `docs/02-experiments/s01-projection-spike/`
   Scope: HALF-SESSION
