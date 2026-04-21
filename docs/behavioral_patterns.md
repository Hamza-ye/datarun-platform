# Behavioral Pattern Catalog

## 1. Purpose

This document exists as **Phase 1** of a structured methodology for closing the design space of the Datarun platform:

```
Scenarios (done)
    ↓
Behavioral Patterns 
    ↓
Principles    ← initial Hypothesis about in `docs/principles.md`
    ↓
Constraint Decisions 
    ↓
Domain Model
    ↓
Structural Architecture
    ↓
Validation
```

**Patterns are behavioral, not architectural.** Each pattern names a behavior the platform must support — not a construct, not a database table, not a technology choice. Whether "State Progression" becomes a status field, a state machine, or an event log is a constraint decision made later. At this stage, we only name what keeps appearing.

**Patterns are inputs to constraint decisions, not outputs.** The patterns here were extracted by reading all Phase 1 scenarios and identifying what recurs, what varies, and what connects. They are the raw material for the next phase (Principles), not a design proposal.

**This is the first narrowing step.** Scenarios describe the full operational reality. Patterns extract what that reality requires the platform to support. Principles will then articulate the beliefs that guide how we build support for those patterns. Nothing is decided here except what behaviors are real.

---

## 2. Core Behavioral Patterns

Twelve atomic patterns are sufficient to decompose every Phase 1 scenario. "Atomic" means each pattern is not reducible to a combination of the others.

---

### P01 — Structured Recording

**What the behavior is:** Someone captures a predefined set of details about something observed, done, or received. The structure of what to capture is defined before the act of capturing happens.

**Appears in:** S00, S01, S05, S20, S21

**What varies:**
- The subject being recorded (an observation, a visit, a distribution, a case outcome)
- Whether the structure is fixed or configurable
- Whether the record stands alone or is linked to something persistent (see P02)

---

### P02 — Subject Linkage

**What the behavior is:** A record is tied to a recognizable, persistent real-world thing — a person, place, household, or piece of equipment — whose identity survives across multiple interactions over time.

**Appears in:** S01, S06, S08, S13, S20

**What varies:**
- Whether the subject's identity is stable (S01) or can change — split, merge, or be reassigned (S06)
- Whether the link is one-to-one or one-to-many (one subject, many records)
- Whether the subject is known at the time of recording or must be resolved later

---

### P03 — Temporal Rhythm

**What the behavior is:** There is an expectation that something will happen on a predictable rhythm — weekly, monthly, per-campaign. A gap in that rhythm is not simply absence; it is a meaningful signal.

**Appears in:** S02, S05, S06, S09

**What varies:**
- Whether the cadence is fixed (S02: monthly report due) or event-triggered (S05: visit expected after enrollment)
- Whether the rhythm is defined ahead of time or emerges from conditions (see P09: when conditions trigger action, the resulting obligation may carry a rhythmic expectation)
- Whether missing an expected occurrence is tracked automatically or requires someone to notice

---

### P04 — Responsibility Binding

**What the behavior is:** A specific person is accountable for a specific scope of work. This makes it unambiguous who should have done something and whether they did.

**Appears in:** S03, S09, S14, S20, S21

**What varies:**
- Whether responsibility is assigned by role, by geography, or by explicit assignment
- Whether responsibility is fixed for a period or can be delegated or transferred
- Whether the scope is a type of work, a set of subjects, or a time window

---

### P05 — Hierarchical Visibility

**What the behavior is:** Different people see different scopes of information and work based on their position in an organizational structure. Seeing more is not the same as doing more; visibility follows hierarchy, not just role.

**Appears in:** S03, S04, S05, S09, S11, S14, S15, S21

**What varies:**
- Whether visibility is purely read access or includes the ability to act on what is seen
- Whether hierarchy is organizational, geographic, programmatic, or a combination
- Whether a person's scope can be expanded for a specific purpose or time window

---

### P06 — Review and Judgment

**What the behavior is:** Work completed by one person passes through another person's assessment before it is considered final. The reviewer may approve, reject, return, or escalate.

**Appears in:** S04, S05, S11, S21

**What varies:**
- Whether review is single-level (S04: supervisor reviews) or multi-level (S11: sequential approval chain)
- Whether the reviewer is always the same role or depends on context (S21: supervisor assesses work done by CHV)
- Whether rejection returns work to the originator or routes it elsewhere

---

### P07 — Transfer with Acknowledgment

**What the behavior is:** Something — goods, authority, responsibility — moves from one party to another. The receiver confirms receipt, disputes the quantity or condition, or notes a discrepancy. The transfer is not complete until acknowledged.

**Appears in:** S07, S14, S20

**What varies:**
- Whether the transfer is single-hop (S07: sender to receiver) or multi-hop with tracing (S14: central → regional → local)
- Whether discrepancy handling is simple (note and continue) or blocks the next leg
- Whether what is transferred is physical, informational, or both

---

### P08 — State Progression

**What the behavior is:** Work moves through defined stages. Progress from one stage to the next is meaningful and recorded. Earlier stages constrain what is possible in later stages.

**Appears in:** S04, S07, S08, S11, S14

**What varies:**
- Whether progression is linear (S07: sent → received → confirmed) or branching (S08: case may reopen)
- Whether stages can be skipped, reversed, or looped
- Whether transitions require human action, automatic detection, or elapsed time

---

### P09 — Condition-Triggered Action

**What the behavior is:** Observed conditions — a threshold crossed, a pattern detected, a gap in expected activity — determine what needs attention. The trigger is not a scheduled plan but a state that has come to exist.

**Appears in:** S10, S12, S18

**What varies:**
- Whether the triggering condition is a single event (S12: notification sent immediately) or an accumulated pattern (S18: analytics detects trend over time)
- Whether the triggered action is a notification, an assignment, an escalation, or a new workflow
- Whether the feedback from the triggered action refines future detection (S18 creates a feedback loop)

---

### P10 — Cross-Reference

**What the behavior is:** Separate, independent activities are connected because understanding one requires context from another. The connection is not incidental; it changes what should be done or how results should be interpreted.

**Appears in:** S08, S13, S18

**What varies:**
- Whether the connection is explicit (S13: two flows explicitly linked) or discovered after the fact (S18: analytics surfaces the relationship)
- Whether the connected activities share a common subject (same person, same location) or only a temporal or causal relationship
- Whether acting on the connection is required or optional

---

### P11 — Shape Definition and Evolution

**What the behavior is:** The expected structure of information is defined ahead of time — which fields exist, what is required, what values are valid. That structure can change; old records remain valid under the shape that was current when they were captured.

**Appears in:** S00, S06

**What varies:**
- Whether shape changes are additive (new optional fields), breaking (required fields added), or deprecating (fields removed)
- Whether old records under old shapes must be migrated or can coexist with records under new shapes
- Whether shape changes are versioned explicitly or resolved at read time

---

### P12 — Offline-First Work

**What the behavior is:** Meaningful work happens without connectivity — records are created, decisions are made, state progresses. When connectivity returns, locally accumulated work reconciles with the shared state.

**Appears in:** S19 (cross-cutting to all scenarios above)

**What varies:**
- Whether offline work is full-featured or a constrained subset of connected behavior
- Whether reconciliation is automatic (last-write-wins) or requires human resolution of conflicts
- Whether the user is aware of offline/online state or the transition is transparent

---

## 3. Variation Spectra

Each pattern appears with different characteristics across scenarios. The spectrum shows the range of what the platform must handle, from simpler to more complex.

| Pattern | Variation Spectrum | Example Range |
|---------|-------------------|---------------|
| **Review and Judgment** | Single-level → Multi-level → Cross-hierarchy | S04 (supervisor reviews one level) → S11 (sequential approval chain) → S21 (supervisor assesses CHV across hierarchy boundary) |
| **Temporal Rhythm** | Fixed cadence → Event-triggered → Condition-driven | S02 (weekly/monthly report due) → S05 (visit expected after planned schedule) → S10 (obligation arises from observed conditions) |
| **Transfer with Acknowledgment** | Single-hop → Multi-hop with tracing | S07 (sender → receiver, one confirmation) → S14 (central → regional → local, each leg confirmed independently) |
| **Subject Linkage** | Simple identity → Mutable identity → Ambiguous identity | S01 (record tied to known stable thing) → S06 (thing splits, merges, or changes attributes) → S01 edge case (same real-world thing recorded under different identities) |
| **State Progression** | Linear → Branching → Looping | S07 (sent → received → confirmed, no deviation) → S08 (case may branch to multiple resolution paths) → S08 (resolved case can reopen) |
| **Condition-Triggered Action** | Immediate → Escalation → Feedback loop | S12 (single event triggers immediate response) → S12 (no response triggers escalation) → S18 (outcome of response refines future condition detection) |

---

## 4. Scenario Decomposition Table

Every Phase 1 scenario decomposes into a combination of the patterns above. This is the validation test: if a scenario cannot be decomposed, either the catalog is incomplete or the scenario is genuinely unique.

| Scenario | Patterns |
|----------|----------|
| S00 — Basic structured capture | Structured Recording (P01), Shape Definition and Evolution (P11) |
| S01 — Entity-linked capture | Structured Recording (P01), Subject Linkage (P02) |
| S02 — Periodic reporting | Structured Recording (P01), Temporal Rhythm (P03), Responsibility Binding (P04) |
| S03 — Designated responsibility | Responsibility Binding (P04), Hierarchical Visibility (P05) |
| S04 — Supervisor review | Review and Judgment (P06), State Progression (P08), Hierarchical Visibility (P05) |
| S05 — Supervision visits | Structured Recording (P01), Temporal Rhythm (P03), Review and Judgment (P06), Subject Linkage (P02) |
| S06 — Registry lifecycle | Subject Linkage (P02), Shape Definition and Evolution (P11), State Progression (P08) |
| S07 — Resource distribution | Transfer with Acknowledgment (P07), State Progression (P08) |
| S08 — Case management | Subject Linkage (P02), State Progression (P08), Cross-Reference (P10), Responsibility Binding (P04) |
| S09 — Coordinated campaign | Temporal Rhythm (P03), Responsibility Binding (P04), Hierarchical Visibility (P05), State Progression (P08) |
| S10 — Dynamic targeting | Condition-Triggered Action (P09), Subject Linkage (P02), Responsibility Binding (P04) |
| S11 — Multi-step approval | Review and Judgment (P06), State Progression (P08), Hierarchical Visibility (P05) |
| S12 — Event-triggered actions | Condition-Triggered Action (P09), State Progression (P08) |
| S13 — Cross-flow linking | Cross-Reference (P10), Subject Linkage (P02) |
| S14 — Multi-level distribution | Transfer with Acknowledgment (P07), State Progression (P08), Hierarchical Visibility (P05), Responsibility Binding (P04) |
| S19 — Offline capture and sync | Offline-First Work (P12) — applies as a cross-cutting constraint to all patterns above |
| S20 — CHV field operations | Structured Recording (P01), Subject Linkage (P02), Transfer with Acknowledgment (P07), State Progression (P08), Responsibility Binding (P04) |
| S21 — Supervisor operations | Review and Judgment (P06), Structured Recording (P01), Subject Linkage (P02), Hierarchical Visibility (P05) |

---

## 5. Connection Map

Patterns do not operate in isolation. In many scenarios, the output of one pattern becomes the input of another.

- **Recording → Obligation**: Captured information creates an expectation for future work. A record entered via Structured Recording (P01) tied to a subject via Subject Linkage (P02) establishes a Temporal Rhythm (P03) expectation for follow-up. (S00/S01 → S02 → S04)

- **Recording → Targeting**: Past observations change what currently needs attention. Records captured via Structured Recording (P01) feed into Condition-Triggered Action (P09), which identifies which subjects (P02) require a response. (S00/S01 → S10 → S08)

- **Handoff → Handoff → Tracing**: Each confirmed receipt in Transfer with Acknowledgment (P07) advances State Progression (P08) and becomes the input to the next leg of a multi-hop transfer. (S07 → S14)

- **Review → Escalation**: A Review and Judgment (P06) outcome — or the absence of one within a Temporal Rhythm (P03) window — triggers a Condition-Triggered Action (P09) that may invoke additional review levels via State Progression (P08). (S04 → S12 → S11)

- **Observation → Cross-Reference → New Effort**: Structured Recording (P01) by a CHV creates a record that Cross-Reference (P10) connects to prior supply or case history, which Condition-Triggered Action (P09) uses to detect a pattern that initiates a new Coordinated effort (via Temporal Rhythm P03 + Responsibility Binding P04). (S20 → S13 → S18 → S09)

---

## 6. Validation

> Every Phase 1 scenario (00–14, 19, 20, 21) should decompose cleanly into a combination of these patterns. If a scenario doesn't decompose, either the pattern catalog is incomplete (add a pattern) or the scenario is doing something genuinely unique (note it explicitly).

All Phase 1 scenarios pass this test based on the decomposition table in Section 4. No scenario required a new pattern. No scenario was left undecomposed. Composite scenarios (S20, S21) decompose into the same atomic patterns as their constituent scenarios, confirming the catalog is sufficient.
