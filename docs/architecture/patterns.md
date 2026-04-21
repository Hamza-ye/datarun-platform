# Pattern Specifications

> Formal specifications for the platform's workflow patterns. Each pattern was validated through scenario walk-throughs in [exploration/28](../exploration/28-pattern-inventory-walkthrough.md) and traced to ADR-005 S5 (Pattern Registry).

---

## 1. Reading Guide

Each pattern specification follows a fixed structure:

| Section | What it contains |
|---------|-----------------|
| Composition type | `subject-level` or `event-level` — determines PE state key |
| States | Named states with initial [I], terminal [T], and quiescent [Q] markers |
| Transition table | `(current_state, event.type, shape_role) → next_state` with SC/SP markers |
| Roles | Participant roles mapped to transitions |
| Projections | Auto-maintained + pattern-specific projections |
| Parameterization | What the deployer fills at L0 |

### Conventions

**Transition markers**: `SC*` = state-changing, activating (creates instance). `SC` = state-changing. `SP` = state-preserving.

**Shape roles**: abstract names for shape slots (e.g., `opening`, `interaction`). Deployers map concrete `shape_ref` values to roles at L0. Shape roles use `snake_case`.

**Composition types**:

| Type | PE state key | Rule |
|------|-------------|------|
| `subject-level` | `(subject_ref, activity_ref)` | Rule 1: at most one per activity |
| `event-level` | `(source_event_id)` | Rule 2: compose freely |

**Shape binding modes**: Shapes relate to patterns as **transition-bound** (mapped to a state transition; Rule 5 applies) or **activation-bound** (triggers instance creation without competing for transitions; Rule 5 does not apply).

**Auto-maintained projections** (universal across all patterns): `current_state`, `pending_since`, `time_in_state`.

**Parameterization**: optional shape role mappings serve as feature gates — unmapped shapes disable their transitions. The deployer controls pattern scope through presence/absence, not by modifying the state machine.

### Traceability

| Source | What it provides |
|--------|-----------------|
| [ADR-005](../adrs/adr-005-state-progression.md) S4–S6 | Pattern architecture: projection-derived state, Pattern Registry, composition rules |
| [primitives.md §11](primitives.md#11-pattern-registry) | Pattern Registry primitive: invariant, constraints, composition rules, config surface |
| [contracts.md](contracts.md) C15, C16, C18 | Pattern Registry contracts → PE, CD, DtV |
| [exploration/28](../exploration/28-pattern-inventory-walkthrough.md) | Detailed walk-throughs, composition validation, entity_lifecycle disposition |

---

## 2. No-Pattern Activities (P7)

Not every activity requires a workflow pattern. Activities configured with `pattern: none` have no state derivation, no transition evaluation, and no pattern-specific flags. The PE applies standard per-subject event lists only.

**S00 validation**: basic structured capture requires 2 deployer artifacts (activity + shape), no pattern parameterization, zero overhead. Confirmed in [exploration/28 §2](../exploration/28-pattern-inventory-walkthrough.md#2-s00--no-pattern-p7-confirmation).

---

## 3. capture_with_review

Review cycle for individual capture events. Each capture event tracked independently.

**Composition type**: `event-level`

### States

| State | Marker | Description |
|-------|--------|-------------|
| `pending_review` | [I] | Capture event exists; no review references it yet |
| `accepted` | [T] | Reviewer accepted |
| `returned` | [T] | Reviewer returned for correction |

Correction after return creates a new event with its own review cycle. The original stays `returned`.

### Transitions

| ID | From | event.type | Shape role | → To | |
|----|------|-----------|------------|------|---|
| T1 | — | `capture` | ∈ `activation_set` | `pending_review` | SC* |
| T2 | `pending_review` | `review` | `review_decision` {accepted} | `accepted` | SC |
| T3 | `pending_review` | `review` | `review_decision` {returned} | `returned` | SC |

T2/T3 branch on `decision` field in review_decision payload. Review event carries `source_event_ref` identifying the tracked capture event.

### Roles

| Role | Transitions |
|------|-------------|
| `capturer` | T1 |
| `reviewer` | T2, T3 |

### Pattern-Specific Projections

Per subject (aggregated across event-level instances):
- `pending_review_count`, `accepted_count`, `returned_count`
- `latest_review_outcome`

### Parameterization

| Parameter | Kind | Required |
|-----------|------|----------|
| `activation.on_shapes` | Shape list | Yes |
| `shapes.review_decision` | Shape mapping | Yes |
| `roles.capturer` | Role mapping | Yes |
| `roles.reviewer` | Role mapping | Yes |
| `deadlines.review_deadline` | Duration | No |

### Composition

- **Standalone mode**: `activation.on_shapes` contains shapes exclusively transition-bound to this pattern.
- **Overlay mode**: `activation.on_shapes` references shapes owned by a subject-level pattern. Only `review_decision` is transition-bound to capture_with_review; activation shapes are activation-bound (Rule 5 not violated). Does not consume the subject-level slot (Rule 1).

---

## 4. case_management

Long-running subject lifecycle with multiple interactions, referrals, transfers, and a review-gated closure.

**Composition type**: `subject-level`

### States

| State | Marker | Description |
|-------|--------|-------------|
| `opened` | [I] | Case created |
| `active` | | Receiving interactions |
| `referred` | | Referred for specialist input |
| `resolved` | | Outcome recorded, awaiting closure review |
| `closed` | [Q] | Closure approved. Reopening available if `reopening` role is mapped. |
| `reopened` | | Reactivated, awaiting first follow-up |

### Transitions

| ID | From | event.type | Shape role | → To | |
|----|------|-----------|------------|------|---|
| T1 | — | `capture` | `opening` | `opened` | SC* |
| T2 | `opened` | `capture` | `interaction` | `active` | SC |
| T3 | `active` | `capture` | `interaction` | `active` | SP |
| T4 | `active` | `capture` | `referral` | `referred` | SC |
| T5 | `referred` | `capture` | `interaction` | `active` | SC |
| T6 | `active`, `referred` | `capture` | `resolution` | `resolved` | SC |
| T7 | `resolved` | `review` | `closure_review` {closed} | `closed` | SC |
| T7b | `resolved` | `review` | `closure_review` {continue} | `resolved` | SP |
| T8 | `resolved`, `closed` | `capture` | `reopening` | `reopened` | SC |
| T9 | `reopened` | `capture` | `interaction` | `active` | SC |
| T10 | any | `assignment_changed` | `transfer` | (same) | SP |
| T11 | any† | `review` | `case_review` | (same) | SP |

† T11: case must exist (no from = `—`).

### Roles

| Role | Transitions |
|------|-------------|
| `assigned_worker` | T1–T6, T8–T9 |
| `supervisor` | T7/T7b, T10, T11 |

### Pattern-Specific Projections

- `current_assignee` — from most recent `assignment_changed` or initial assignment
- `last_interaction_date`
- `interaction_count`, `referral_count`, `reopen_count`

### Parameterization

| Parameter | Kind | Required |
|-----------|------|----------|
| `shapes.opening` | Shape mapping | Yes |
| `shapes.interaction` | Shape mapping | Yes |
| `shapes.referral` | Shape mapping | No |
| `shapes.resolution` | Shape mapping | Yes |
| `shapes.closure_review` | Shape mapping | Yes |
| `shapes.reopening` | Shape mapping | No |
| `shapes.transfer` | Shape mapping | No |
| `shapes.case_review` | Shape mapping | No |
| `roles.assigned_worker` | Role mapping | Yes |
| `roles.supervisor` | Role mapping | Yes |
| `deadlines.follow_up_interval` | Duration | No |
| `deadlines.overdue_threshold` | Duration | No |
| `deadlines.resolution_target` | Duration | No |

Optional shape mappings act as feature gates: no `reopening` shape → `closed` is terminal; no `referral` shape → T4 unavailable.

---

## 5. multi_step_approval

Parameterized sequential approval chain with deployer-configured level count.

**Composition type**: `subject-level` (primary) or `event-level` (embedded via Rule 3)

### States

| State | Marker | Description |
|-------|--------|-------------|
| `pending` | [I] | Awaiting review at `current_level` (1..levels) |
| `returned` | | Returned for correction; resubmission restarts at level 1 |
| `final_approved` | [T] | Approved at all levels |
| `final_rejected` | [T] | Rejected at any level |

`pending` carries a `current_level` attribute (integer, 1..`levels`).

### Transitions

| ID | From | event.type | Shape role | → To | |
|----|------|-----------|------------|------|---|
| T1 | — | `capture` | `submission` | `pending` {level=1} | SC* |
| T2 | `pending` {N < levels} | `review` | `level_decision` {approved} | `pending` {level=N+1} | SC |
| T3 | `pending` {N = levels} | `review` | `level_decision` {approved} | `final_approved` | SC |
| T4 | `pending` | `review` | `level_decision` {returned} | `returned` | SC |
| T5 | `pending` | `review` | `level_decision` {rejected} | `final_rejected` | SC |
| T6 | `returned` | `capture` | `submission` | `pending` {level=1} | SC |

Level validation: the review must be for exactly `current_level`; otherwise `transition_violation`.

### Roles

| Role | Transitions |
|------|-------------|
| `submitter` | T1, T6 |
| `level_N_reviewer` (×N) | T2–T5 at level N |

### Pattern-Specific Projections

- `current_level` — integer (1..levels when pending, null when terminal)
- `approval_chain` — ordered list of `{level, actor_ref, decision, timestamp}`
- `time_at_current_level`
- `submission_count`

### Parameterization

| Parameter | Kind | Required |
|-----------|------|----------|
| `levels` | Numeric (min 2) | Yes |
| `shapes.submission` | Shape mapping | Yes |
| `shapes.level_decision` | Shape mapping | Yes |
| `roles.submitter` | Role mapping | Yes |
| `roles.level_N_reviewer` | Role mapping (×N) | Yes |
| `deadlines.review_deadline` | Duration | No |

### Dual Composition Mode

| Mode | PE state key | Declaration | Rule 1 |
|------|-------------|------------|--------|
| Primary | `(subject_ref, activity_ref)` | Assigned as activity's subject-level pattern | Consumes slot |
| Embedded | `(submission_event_id)` | Listed alongside a subject-level pattern | Does not consume slot |

Transition table is identical in both modes; only the PE state key changes.

---

## 6. transfer_with_acknowledgment

Two-party handoff coordination: sender dispatches, receiver confirms, discrepancies tracked and resolved.

**Composition type**: `subject-level`

### States

| State | Marker | Description |
|-------|--------|-------------|
| `in_transit` | [I] | Dispatch recorded |
| `received` | [T] | Receipt confirmed, no discrepancies |
| `partial_receipt` | | Receipt confirmed with discrepancies |
| `disputed` | | Discrepancy report filed |
| `resolved` | [T] | Discrepancy reviewed and resolved |

### Transitions

| ID | From | event.type | Shape role | → To | |
|----|------|-----------|------------|------|---|
| T1 | — | `capture` | `dispatch` | `in_transit` | SC* |
| T2 | `in_transit` | `capture` | `receipt` {no discrepancies} | `received` | SC |
| T3 | `in_transit` | `capture` | `receipt` {discrepancies} | `partial_receipt` | SC |
| T4 | `in_transit`, `partial_receipt` | `capture` | `discrepancy_report` | `disputed` | SC |
| T5 | `partial_receipt`, `disputed` | `review` | `discrepancy_resolution` | `resolved` | SC |

T2/T3 branch on `discrepancies` field in receipt payload.

### Roles

| Role | Transitions |
|------|-------------|
| `sender` | T1 |
| `receiver` | T2, T3, T4 |
| `supervisor` | T5 |

### Pattern-Specific Projections

- `items_dispatched`, `items_received`
- `discrepancy_summary` — computed from dispatch vs receipt
- `time_in_transit` — duration from T1 to T2/T3

### Parameterization

| Parameter | Kind | Required |
|-----------|------|----------|
| `shapes.dispatch` | Shape mapping | Yes |
| `shapes.receipt` | Shape mapping | Yes |
| `shapes.discrepancy_report` | Shape mapping | No |
| `shapes.discrepancy_resolution` | Shape mapping | No |
| `roles.sender` | Role mapping | Yes |
| `roles.receiver` | Role mapping | Yes |
| `roles.supervisor` | Role mapping | No (required if discrepancy shapes mapped) |
| `deadlines.receipt_deadline` | Duration | No |
| `deadlines.resolution_deadline` | Duration | No |

### Multi-Level Distribution

S14 (multi-level distribution) is handled by multiple instances on related subjects — not by pattern machinery:

| Level | Subject | Pattern instance |
|-------|---------|-----------------|
| Central → Regional | Parent shipment | transfer_with_acknowledgment |
| Regional → District | Child shipment(s) | transfer_with_acknowledgment |

Child subjects reference parents via `parent_shipment_ref` in the dispatch payload. Aggregate state ("fully distributed") is a deployer-configured L1 projection rule over the parent-child tree.

---

## 7. entity_lifecycle (Candidate — Deferred)

Entity registry lifecycle with cyclical verification. **Not in the initial inventory** — deferred to platform evolution per [exploration/28 §8](../exploration/28-pattern-inventory-walkthrough.md#8-s06-disposition-entity_lifecycle).

**Why separate from case_management**: Updates after verification are normal transitions (→ `active`), not violations. Verification is cyclical (repeated every period), not a linear progression toward closure. Mapping to case_management would produce false-positive `transition_violation` flags on every post-verification update.

**States**: `registered` [I], `active`, `verified`, `deprecated` [Q]

**Transitions**: 8 (T1–T8). Key structural feature: T5 (`verified` → `active` on update) is a normal transition — the cyclical `active → verified → active → ...` loop.

**Inventory recommendation**: Include when S06-like scenarios (entity registries) appear in early deployments. Zero architectural cost to add later — the Pattern Registry is append-only [5-S5].

Full specification: [exploration/28 §8](../exploration/28-pattern-inventory-walkthrough.md#8-s06-disposition-entity_lifecycle).

---

## 8. Inventory Summary

| Pattern | Comp. | States | Trans. | Shape roles | Participant roles | Event types used |
|---------|-------|--------|--------|-------------|-------------------|-----------------|
| `capture_with_review` | event | 3 | 3 | 2 | 2 | capture, review |
| `case_management` | subject | 6 | 11 | 8 (3 opt.) | 2 | capture, review, assignment_changed |
| `multi_step_approval` | subj/event | 4 + level | 6 | 2 | 1+N | capture, review |
| `transfer_with_acknowledgment` | subject | 5 | 5 | 4 (2 opt.) | 3 | capture, review |

**Observations**:
- All 4 patterns use only 3 of 6 event types (`capture`, `review`, `assignment_changed`). Triggers (`alert`, `task_created`, `task_completed`) react to lifecycle events but don't participate in state derivation.
- All transitions use shape roles, not concrete shape_refs. Concrete mapping is deployer's L0 responsibility.
- Parameterization through optional shape mappings serves as the primary feature-gating mechanism.

### Architecture Consistency

| Check | Result |
|-------|--------|
| All transitions use 6 platform-fixed event types | ✓ |
| Flagged events excluded from state derivation [5-S2] | ✓ |
| Pattern definitions sync via atomic config [4-S6, C20] | ✓ |
| Deploy-time validation enforces Rules 1–5 [C18] | ✓ |
| Command Validator advisory, never blocking [5-S4] | ✓ |
| S00 works without any pattern [P7] | ✓ |

### Contracts Satisfied

| Contract | Validated |
|----------|-----------|
| C15 (PR → PE) | States, transitions, projections, parameterization defined per pattern |
| C16 (PR → CD) | Valid transitions defined — CD can evaluate and flag violations |
| C18 (PR → DtV) | Rules 1–5 validated with concrete multi-pattern compositions |
