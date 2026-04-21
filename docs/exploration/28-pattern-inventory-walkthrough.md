# 28: Pattern Inventory Walk-through

> Purpose: Formal specifications for the 4 existence-proof patterns validated in ADR-5 S5. Normalizes the pseudo-YAML sketches from exploration/19 §5.1 into consistent, specification-grade definitions. Resolves the entity_lifecycle disposition (§8).
> Date: 2026-04-14
> Input: ADR-005, exploration/19, exploration/20, architecture/primitives.md, architecture/contracts.md, architecture/cross-cutting.md, scenarios S00/S04/S06/S07/S08/S11/S14

---

## §1 Method and Conventions

### 1.1 Scope

This document takes the 4 pattern sketches from exploration/19 §5.1 — which use inconsistent notation and leave structural questions implicit — and produces normalized specifications. Each pattern gets: states, transitions, roles, projections, parameterization points, and composition type declaration.

The pattern **architecture** is decided in ADR-5 (S4 projection patterns, S5 Pattern Registry, S6 composition rules). This document specifies the **inventory** — which patterns ship and what their skeletons look like. The inventory is implementation scope (initial strategy, evolvable), not architectural constraint.

### 1.2 Transition Notation

Every transition is defined by a tuple:

```
(current_state, event.type, shape_role) → next_state
```

Where:

- `current_state`: the state the subject (or event) is in before this event. `—` means no prior state (activating transition).
- `event.type`: one of the 6 platform-fixed types: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`.
- `shape_role`: the pattern's abstract name for a shape slot (e.g., `opening`, `interaction`, `review_decision`). The deployer maps concrete `shape_ref` values to these roles at L0. Shape roles use `snake_case`.
- `next_state`: the resulting state. `(same)` for state-preserving transitions.

**Transition markers**:

| Marker | Meaning |
|--------|---------|
| `SC*` | State-changing, **activating** — creates a new state instance |
| `SC` | State-changing — advances `current_state` |
| `SP` | State-preserving — event is valid but `current_state` is unchanged |

**Conditional transitions**: when a shape role has branching outcomes (e.g., a review decision with `accepted` vs `returned`), the condition is noted inline as `shape_role {condition}`. Each branch gets its own row.

**No implicit transitions**: the exploration/19 sketches use `implicit: true` for transitions like `dispatched → in_transit` and `submitted → pending_level_1`. This document eliminates implicit transitions. Where the prior sketches marked a transition as implicit, the initial state absorbs it directly (e.g., the initial state is `in_transit`, not `dispatched → in_transit`). Where a state is derived from another value (e.g., `final_approved` when `level == max_levels`), the derivation rule appears in the transition condition.

### 1.3 State Naming

- States use `snake_case`.
- Initial state: marked `[I]` in the state list. Entered by the activating transition (`SC*`).
- Terminal state: marked `[T]`. No outbound transitions defined.
- Quiescent state: a non-terminal state where no regular activity is expected but further transitions are structurally possible (e.g., `closed` in case_management — reopening remains available).

### 1.4 Composition Types

Each pattern declares exactly one composition type. This determines how the Projection Engine tracks state.

| Type | PE state key | Meaning |
|------|-------------|---------|
| **subject-level** | `(subject_ref, activity_ref)` | One state per subject per activity. Tracks the subject's lifecycle within the activity. |
| **event-level** | `(source_event_id)` | One state per originating event. Tracks the processing status of a specific event (review, approval). |

**Composition type implications for PE**:

- **Subject-level**: PE maintains `subject_state_table[subject_ref, activity_ref] → {state, projections...}`. Each incoming event for (subject, activity) is checked against the transition table. Rule 1 applies: at most one subject-level pattern per activity.
- **Event-level**: PE maintains `event_state_table[source_event_id, pattern_id] → {state, projections...}`. Each relevant event creates or updates an entry keyed by the source event it tracks. Rule 2 applies: event-level patterns compose freely.

A pattern with dual composition type (multi_step_approval) declares which type applies in each deployment mode (§5.6).

### 1.5 Shape Binding

Shapes relate to patterns in two ways:

| Binding | What it means | Rule 5 applies? |
|---------|--------------|-----------------|
| **Transition-bound** | Shape is mapped to a state transition in the pattern's state machine. Appears in the transition table. | Yes — unique within activity |
| **Activation-bound** | Shape triggers creation of a new pattern instance but is not itself a transition in the pattern. | No — activation references shapes; it does not compete for them |

Most patterns have only transition-bound shapes (activation = first transition-bound event). `capture_with_review` additionally uses activation-binding in overlay mode: it watches capture events from other patterns' shapes to create review instances.

### 1.6 Projection Types

| Type | Who defines | What it provides |
|------|-----------|-----------------|
| **Auto-maintained** | Pattern definition (platform-fixed) | PE computes automatically for every pattern instance: `current_state`, `pending_since`, `time_in_state` |
| **Pattern-specific** | Pattern definition (platform-fixed) | PE computes per pattern: e.g., `interaction_count` for case_management, `approval_chain` for multi_step_approval |
| **Deployer-configured** | Deployer at L1 | Projection rules (value-to-value lookup tables) for domain-specific aggregations |

Auto-maintained projections are universal across all patterns. Pattern-specific projections are declared in each pattern's specification below.

### 1.7 Parameterization

Deployers parameterize patterns at L0 (Assembly layer). What they fill in:

| Parameter kind | What the deployer provides | Example |
|---------------|--------------------------|---------|
| Shape mappings | Concrete `shape_ref` for each shape role | `opening: malaria_case_opening/v1` |
| Role mappings | Assignment role for each participant role | `assigned_worker: chv` |
| Deadlines | Duration values for L3b deadline triggers | `follow_up_interval: 7d` |
| Numeric | Pattern-specific integers | `levels: 3` (multi_step_approval) |
| Activation | Shapes that trigger event-level instances | `on_shapes: [malaria_follow_up/v1]` |

The deployer does not see or author state machines. Pattern definitions are platform-fixed, synced to devices as part of the atomic configuration package [4-S6, C15, C20].

---

## §2 S00 — No Pattern (P7 Confirmation)

**Scenario**: [S00](../scenarios/00-basic-structured-capture.md) — recording structured information. A person records details about something they've observed. No review, no lifecycle, no approval.

**Walk-through**:

| Step | Actor | Event type | Shape | What happens |
|------|-------|-----------|-------|-------------|
| 1 | CHV | `capture` | `household_observation/v1` | Observation recorded |
| 2 | CHV | `capture` | `household_observation/v1` | Correction (new event, `source_event_ref` links to #1) |
| 3 | — | — | — | Projection shows latest observation per subject |

**Pattern required?** No. The activity configuration:

```
activity: household_monitoring
pattern: none
shapes: [household_observation]
roles: [chv]
```

**PE behavior**: standard per-subject event list. No state derivation. No transition evaluation. No flags beyond existing ADR-2 conflict detection (identity, staleness).

**What this confirms**:

- **P7 (simplest scenario stays simple)**: S00 requires zero pattern selection at L0. Two deployer artifacts: one activity, one shape. No state machine complexity.
- **Pattern Registry is opt-in**: the `pattern: none` configuration is valid. Not every activity needs a workflow pattern.
- **S00 cost**: 2 deployer artifacts (activity + shape), no code, no pattern parameterization. Same as the P2 validation in ADR-4 Session 2.

---

## §3 capture_with_review

### 3.1 Walk-through: S04 — Supervisor Review

**Scenario**: [S04](../scenarios/04-supervisor-review.md) — work done by one person is reviewed by another.

| Step | Actor | Event type | Shape role | Subject state after |
|------|-------|-----------|------------|-------------------|
| 1 | CHV | `capture` | `primary_capture` | Event E1 → `pending_review` |
| 2a | Supervisor | `review` | `review_decision {accepted}` | E1 → `accepted` |
| 2b | Supervisor | `review` | `review_decision {returned}` | E1 → `returned` |
| 3 | CHV | `capture` | `primary_capture` (correction) | New event E2 → `pending_review`. E1 stays `returned`. |
| 4 | Supervisor | `review` | `review_decision {accepted}` | E2 → `accepted` |

**Key observation**: each capture event has its own independent review cycle. The subject can accumulate multiple capture events at different review stages. Resubmission after return creates a NEW event (append-only), linked to the returned event via `source_event_ref` in the payload. The old event's state is frozen at `returned`.

**Offline tension** (from exploration/19 §2.1): concurrent capture and review (CHV captures while supervisor reviews a previous capture) resolves naturally — different events, independent review cycles. Stale review (supervisor reviews an old version while CHV has already corrected) is handled by ADR-2 accept-and-flag: the review references a superseded source event, projection makes this visible.

### 3.2 Pattern Specification

**Composition type**: `event-level`

**States**:

| State | Marker | Description |
|-------|--------|-------------|
| `pending_review` | [I] | Capture event exists, no review event references it yet |
| `accepted` | [T] | Reviewer accepted this event |
| `returned` | [T] | Reviewer returned this event for correction |

Both terminal states are final for the tracked event. Correction creates a new event with its own review cycle.

**Transition table**:

| ID | From | event.type | Shape role | → To | |
|----|------|-----------|------------|------|---|
| T1 | — | `capture` | ∈ `activation_set` | `pending_review` | SC* |
| T2 | `pending_review` | `review` | `review_decision` {accepted} | `accepted` | SC |
| T3 | `pending_review` | `review` | `review_decision` {returned} | `returned` | SC |

T2/T3 branch on the `decision` field in the review_decision shape's payload. The review event's payload carries `source_event_ref` identifying which capture event is being reviewed.

**Roles**:

| Participant role | Transitions | Responsibility |
|-----------------|-------------|----------------|
| `capturer` | T1 | Creates capture events that trigger review cycles |
| `reviewer` | T2, T3 | Reviews capture events and records decisions |

**Projections**:

Auto-maintained (per tracked event):
- `review_status`: current state
- `pending_since`: timestamp of the activating capture event
- `time_in_state`: duration since last state-changing event

Pattern-specific (per subject, aggregated across event-level instances):
- `pending_review_count`: count of events in `pending_review` state for this subject/activity
- `accepted_count`: count in `accepted`
- `returned_count`: count in `returned`
- `latest_review_outcome`: the most recent review decision across all events for this subject

**Parameterization** (deployer fills at L0):

| Parameter | Kind | Required | Description |
|-----------|------|----------|-------------|
| `activation.on_shapes` | Shape list | Yes | Shape_refs that trigger review cycles (e.g., `[household_observation/v1]`) |
| `shapes.review_decision` | Shape mapping | Yes | Shape_ref for review outcome events |
| `roles.capturer` | Role mapping | Yes | Assignment role for capturers |
| `roles.reviewer` | Role mapping | Yes | Assignment role for reviewers |
| `deadlines.review_deadline` | Duration | No | Expected review turnaround (L3b trigger for overdue alerts) |

### 3.3 Composition Type Details

**PE state tracking**: `event_state_table[source_event_id, capture_with_review] → {review_status, pending_since}`

**Activation model**:

- **Standalone mode** (S04 — capture_with_review is the only pattern): `activation.on_shapes` lists shapes that are exclusively transition-bound to this pattern. Both shape roles (`activation_set` and `review_decision`) are owned by capture_with_review for Rule 5 purposes.
- **Overlay mode** (combined with a subject-level pattern): `activation.on_shapes` references shapes owned by the subject-level pattern. capture_with_review observes matching capture events and creates review instances. Only `review_decision` is transition-bound to capture_with_review. The activation shapes are **activation-bound** (§1.5), not transition-bound, so Rule 5 is not violated.

**Embedding rule**: capture_with_review can overlay any activity. It does not consume the subject-level pattern slot (Rule 1). Multiple event-level patterns can coexist (Rule 2).

---

## §4 case_management

### 4.1 Walk-through: S08 — Following Something Over Time

**Scenario**: [S08](../scenarios/08-case-management.md) — a situation requires ongoing attention through multiple interactions until resolution.

| Step | Actor | Event type | Shape role | Subject state after |
|------|-------|-----------|------------|-------------------|
| 1 | CHV | `capture` | `opening` | `opened` |
| 2 | CHV | `capture` | `interaction` | `active` |
| 3 | CHV | `capture` | `interaction` | `active` (SP) |
| 4 | CHV | `capture` | `referral` | `referred` |
| 5 | Nurse | `capture` | `interaction` | `active` |
| 6 | Nurse | `capture` | `resolution` | `resolved` |
| 7 | Supervisor | `review` | `closure_review` {closed} | `closed` |
| 8 | CHV | `capture` | `reopening` | `reopened` |
| 9 | CHV | `capture` | `interaction` | `active` |

**Key observations**:
- State progression is non-linear (reopening creates a cycle).
- Some events are state-preserving (subsequent interactions in `active`).
- Responsibility shifts via `assignment_changed` without affecting lifecycle state.
- The longest scenarios (S08) accumulate 50–100 events over weeks or months.

**Offline tension** (from exploration/19 §2.2): Concurrent resolution (nurse resolves while CHV records interaction offline) and concurrent transfer (supervisor transfers while another supervisor reviews) are both handled by ADR-2 causal ordering + accept-and-flag. Transition violations (e.g., interaction submitted for a closed case by an offline CHV) are flagged via [5-S1], not rejected.

### 4.2 Pattern Specification

**Composition type**: `subject-level`

**States**:

| State | Marker | Description |
|-------|--------|-------------|
| `opened` | [I] | Case created via opening event |
| `active` | | Case receiving interactions |
| `referred` | | Case referred for specialist input |
| `resolved` | | Case outcome recorded, awaiting closure review |
| `closed` | [Q] | Case closure approved. Quiescent — reopening possible if `reopening` role is mapped |
| `reopened` | | Case reactivated, awaiting first follow-up interaction |

No hard terminal state. `closed` is quiescent [Q]: if the deployer maps a shape to the `reopening` role, T8 is available. If unmapped, `closed` is effectively terminal (no outbound transitions exist).

**Transition table**:

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

† T11: any state except `—` (case must exist). Non-lifecycle reviews don't change state.

T7/T7b: branching on payload field `decision` in the closure_review shape. `{closed}` advances to `closed`; `{continue}` (or any other outcome) preserves state.

**Roles**:

| Participant role | Transitions | Responsibility |
|-----------------|-------------|----------------|
| `assigned_worker` | T1–T6, T8–T9 | Creates data capture events |
| `supervisor` | T7/T7b, T10, T11 | Reviews, transfers responsibility, case review |

**Projections**:

Auto-maintained:
- `current_state`: lifecycle state
- `pending_since`: timestamp of most recent state-changing event
- `time_in_state`: duration since last state change

Pattern-specific:
- `current_assignee`: actor from the most recent `assignment_changed` event (or initial assignment at case opening)
- `last_interaction_date`: timestamp of most recent interaction event (regardless of state change)
- `interaction_count`: total interaction events across all states
- `referral_count`: total referral events
- `reopen_count`: total reopening events

**Parameterization** (deployer fills at L0):

| Parameter | Kind | Required | Description |
|-----------|------|----------|-------------|
| `shapes.opening` | Shape mapping | Yes | Shape for case creation |
| `shapes.interaction` | Shape mapping | Yes | Shape for case interactions |
| `shapes.referral` | Shape mapping | No | Shape for referral requests. If unmapped, T4 is unavailable. |
| `shapes.resolution` | Shape mapping | Yes | Shape for case resolution |
| `shapes.closure_review` | Shape mapping | Yes | Shape for closure review |
| `shapes.reopening` | Shape mapping | No | Shape for reopening. If unmapped, `closed` is terminal. |
| `shapes.transfer` | Shape mapping | No | Shape for `assignment_changed` events |
| `shapes.case_review` | Shape mapping | No | Shape for non-lifecycle reviews |
| `roles.assigned_worker` | Role mapping | Yes | Assignment role for case workers |
| `roles.supervisor` | Role mapping | Yes | Assignment role for supervisors |
| `deadlines.follow_up_interval` | Duration | No | Expected interval between interactions (L3b) |
| `deadlines.overdue_threshold` | Duration | No | When to generate overdue alert (L3b) |
| `deadlines.resolution_target` | Duration | No | Target time from opening to resolution (L3b) |

### 4.3 Composition Type Details

**PE state tracking**: `subject_state_table[subject_ref, activity_ref] → {state, current_assignee, last_interaction_date, time_in_state, interaction_count, ...}`

Each incoming event for (subject, activity) is checked against the transition table:
1. Look up current state.
2. Match (current_state, event.type, shape_role) against transition table.
3. If matched as SC: update `current_state`, reset `pending_since` and `time_in_state`.
4. If matched as SP: update pattern-specific projections (e.g., `last_interaction_date`, `interaction_count`) without touching `current_state`.
5. If no match: event is valid in the activity but unknown to the pattern. No state change, no flag. (Shapes from other patterns in the same activity are ignored.)
6. If match found but from-state doesn't match: `transition_violation` flag [5-S1].

**Embedding rule**: case_management consumes the subject-level slot for the activity (Rule 1). Event-level patterns (capture_with_review for interaction review, multi_step_approval for embedded approvals) compose alongside it (Rules 2, 3).

---

## §5 multi_step_approval

### 5.1 Walk-through: S11 — Multiple Levels of Judgment

**Scenario**: [S11](../scenarios/11-multi-step-approval.md) — work progresses through a sequence of reviews, each by a different person.

Example with 3 levels:

| Step | Actor | Event type | Shape role | State after |
|------|-------|-----------|------------|-------------|
| 1 | Worker | `capture` | `submission` | `pending` {level=1} |
| 2 | District officer | `review` | `level_decision` {approved} | `pending` {level=2} |
| 3 | Regional manager | `review` | `level_decision` {approved} | `pending` {level=3} |
| 4 | National director | `review` | `level_decision` {approved} | `final_approved` |
| Alt: Any level returns | Reviewer | `review` | `level_decision` {returned} | `returned` |
| Alt: Worker resubmits | Worker | `capture` | `submission` | `pending` {level=1} (restart) |

**Key observation**: the number of levels is deployer-configured, not platform-fixed. The pattern is parameterized: `levels: N`. The state machine uses a `current_level` attribute rather than N separate named states.

**Offline tension** (from exploration/19 §2.3): Skip-level review (L2 reviewer acts before L1 completes) → transition_violation flag. Return while already re-reviewed (L1 returns while L2 approves offline) → accept-and-flag, causal ordering determines projection.

### 5.2 Pattern Specification

**Composition type**: `subject-level` (primary) or `event-level` (embedded via Rule 3)

**States**:

| State | Marker | Description |
|-------|--------|-------------|
| `pending` | [I] | Awaiting review at `current_level` (1..max). Level starts at 1 on activation. |
| `returned` | | Returned for correction at any level. Resubmission restarts at level 1. |
| `final_approved` | [T] | Approved at all levels |
| `final_rejected` | [T] | Rejected at any level |

The `pending` state carries an attribute `current_level` (integer, 1..`levels`). This is a state attribute, not a separate state — the PE tracks (state, level) as a composite.

**Transition table**:

| ID | From | event.type | Shape role | → To | |
|----|------|-----------|------------|------|---|
| T1 | — | `capture` | `submission` | `pending` {level=1} | SC* |
| T2 | `pending` {level=N, N < levels} | `review` | `level_decision` {approved} | `pending` {level=N+1} | SC |
| T3 | `pending` {level=levels} | `review` | `level_decision` {approved} | `final_approved` | SC |
| T4 | `pending` | `review` | `level_decision` {returned} | `returned` | SC |
| T5 | `pending` | `review` | `level_decision` {rejected} | `final_rejected` | SC |
| T6 | `returned` | `capture` | `submission` | `pending` {level=1} | SC |

T2/T3/T4/T5 branch on the `decision` field in the level_decision shape's payload. T2 vs T3 is distinguished by whether `current_level` has reached the `levels` parameter.

**Level validation**: the level_decision shape's payload contains a `level` field. The pattern validates: the review must be for exactly `current_level`. A review at level 3 when `current_level` is 2 produces a `transition_violation` flag.

**Roles**:

| Participant role | Transitions | Responsibility |
|-----------------|-------------|----------------|
| `submitter` | T1, T6 | Creates submission events |
| `level_N_reviewer` | T2–T5 at level N | Reviews and decides at their assigned level |

Deployer provides N role mappings: one per level (e.g., `level_1_reviewer: district_officer`, `level_2_reviewer: regional_manager`, `level_3_reviewer: national_director`). The Assignment Resolver computes who holds each role in the scope containing the subject.

**Projections**:

Auto-maintained:
- `current_state`: `pending` | `returned` | `final_approved` | `final_rejected`
- `pending_since`: timestamp of most recent state-changing event
- `time_in_state`: duration since last state change

Pattern-specific:
- `current_level`: integer (1..levels when pending, 0 when returned, null when terminal)
- `approval_chain`: ordered list of `{level, actor_ref, decision, timestamp}` for all review events
- `time_at_current_level`: duration at the current level (resets on level advancement)
- `submission_count`: total submissions (including resubmissions)

**Parameterization** (deployer fills at L0):

| Parameter | Kind | Required | Description |
|-----------|------|----------|-------------|
| `levels` | Numeric | Yes | Number of approval levels (integer, min 2) |
| `shapes.submission` | Shape mapping | Yes | Shape for submissions (and resubmissions) |
| `shapes.level_decision` | Shape mapping | Yes | Shape for level decisions |
| `roles.submitter` | Role mapping | Yes | Assignment role for submitters |
| `roles.level_N_reviewer` | Role mapping | Yes (×N) | Assignment role per level. Deployer provides one mapping per configured level. |
| `deadlines.review_deadline` | Duration | No | Expected review turnaround per level (L3b trigger) |

### 5.3 Composition Type Details

**Primary mode** (subject-level):

PE state tracking: `subject_state_table[subject_ref, activity_ref] → {state, current_level, approval_chain, ...}`

The subject IS the work item being approved. This matches S11 (field reports requiring multi-level judgment).

**Embedded mode** (event-level, Rule 3):

PE state tracking: `event_state_table[submission_event_id, multi_step_approval] → {state, current_level, approval_chain, ...}`

The submission event is a capture event within the host subject-level pattern's activity. Example: a `drug_change_request` event within `malaria_case_tracking` (case_management). The submission shape is exclusively owned by multi_step_approval — it is NOT a case_management shape. The host pattern's subject state is unaffected by the approval flow.

**Deployer declares the mode** at L0 by how the pattern is assigned to the activity:

| Mode | Declaration | Rule 1 interaction |
|------|------------|-------------------|
| Primary | Pattern assigned as the activity's subject-level pattern | Consumes the subject-level slot |
| Embedded | Pattern listed alongside a subject-level pattern | Operates as event-level; does not consume the subject-level slot |

The transition table is identical in both modes. Only the PE state key changes.

---

## §6 transfer_with_acknowledgment

### 6.1 Walk-through: S07+S14 — Handoffs and Multi-Level Distribution

**Scenario**: [S07](../scenarios/07-resource-distribution.md) — items move from sender to receiver with confirmation. [S14](../scenarios/14-multi-level-distribution.md) — resources move through a chain of responsibility across multiple levels.

Single-level handoff:

| Step | Actor | Event type | Shape role | Subject state after |
|------|-------|-----------|------------|-------------------|
| 1 | Central officer | `capture` | `dispatch` | `in_transit` |
| 2 | Regional officer | `capture` | `receipt` {no discrepancies} | `received` |
| — | OR | | | |
| 2b | Regional officer | `capture` | `receipt` {discrepancies} | `partial_receipt` |
| 3 | Regional officer | `capture` | `discrepancy_report` | `disputed` |
| 4 | Supervisor | `review` | `discrepancy_resolution` | `resolved` |

**Key observations**:
- Two-party coordination: sender dispatches, receiver confirms. Both contribute events to the same subject (shipment).
- Subject = shipment. State tracks the shipment's transfer lifecycle.
- Multi-level (S14): each level is a separate subject (sub-shipment) with its own pattern instance. Parent-child relationship tracked via payload cross-references.

**Offline tension** (from exploration/19 §2.4): dispatch and receipt crossing in transit (receipt event arrives before dispatch event syncs) → accept-and-flag, projection eventually reconciles. Partial receipt at one level with full dispatch at next → each event is valid, projection tracks the full chain.

### 6.2 Pattern Specification

**Composition type**: `subject-level`

**States**:

| State | Marker | Description |
|-------|--------|-------------|
| `in_transit` | [I] | Dispatch recorded. Items are in transit to receiver. |
| `received` | [T] | Receipt confirmed with no discrepancies |
| `partial_receipt` | | Receipt confirmed but with discrepancies |
| `disputed` | | Discrepancy report filed |
| `resolved` | [T] | Discrepancy reviewed and resolved |

**Transition table**:

| ID | From | event.type | Shape role | → To | |
|----|------|-----------|------------|------|---|
| T1 | — | `capture` | `dispatch` | `in_transit` | SC* |
| T2 | `in_transit` | `capture` | `receipt` {no discrepancies} | `received` | SC |
| T3 | `in_transit` | `capture` | `receipt` {discrepancies} | `partial_receipt` | SC |
| T4 | `in_transit`, `partial_receipt` | `capture` | `discrepancy_report` | `disputed` | SC |
| T5 | `partial_receipt`, `disputed` | `review` | `discrepancy_resolution` | `resolved` | SC |

T2/T3 branch on the `discrepancies` field in the receipt shape's payload. `{no discrepancies}` = discrepancy list empty or flag false. `{discrepancies}` = any discrepancy recorded.

**Roles**:

| Participant role | Transitions | Responsibility |
|-----------------|-------------|----------------|
| `sender` | T1 | Records dispatch |
| `receiver` | T2, T3, T4 | Confirms receipt, reports discrepancies |
| `supervisor` | T5 | Resolves discrepancies |

**Projections**:

Auto-maintained:
- `current_state`: transfer state
- `pending_since`: timestamp of most recent state-changing event
- `time_in_state`: duration since last state change

Pattern-specific:
- `items_dispatched`: quantity extracted from dispatch event payload
- `items_received`: quantity extracted from receipt event payload
- `discrepancy_summary`: computed from dispatch vs receipt quantities
- `time_in_transit`: duration from dispatch (T1) to receipt (T2/T3). Ongoing if still `in_transit`.

**Parameterization** (deployer fills at L0):

| Parameter | Kind | Required | Description |
|-----------|------|----------|-------------|
| `shapes.dispatch` | Shape mapping | Yes | Shape for dispatch records |
| `shapes.receipt` | Shape mapping | Yes | Shape for receipt confirmations |
| `shapes.discrepancy_report` | Shape mapping | No | Shape for discrepancy reports. If unmapped, T4 is unavailable. |
| `shapes.discrepancy_resolution` | Shape mapping | No | Shape for discrepancy reviews. If unmapped, T5 is unavailable. |
| `roles.sender` | Role mapping | Yes | Assignment role for senders |
| `roles.receiver` | Role mapping | Yes | Assignment role for receivers |
| `roles.supervisor` | Role mapping | No | Assignment role for discrepancy resolution. Required if discrepancy shapes are mapped. |
| `deadlines.receipt_deadline` | Duration | No | Expected time for receipt confirmation (L3b) |
| `deadlines.resolution_deadline` | Duration | No | Expected time for discrepancy resolution (L3b) |

### 6.3 Composition Type Details

**PE state tracking**: `subject_state_table[subject_ref, activity_ref] → {state, items_dispatched, items_received, discrepancy_summary, ...}`

**Multi-level distribution** (S14):

Multi-level handoffs are modeled as multiple instances of the same pattern on related subjects:

| Level | Subject | Pattern instance | Example |
|-------|---------|-----------------|---------|
| Central → Regional | Parent shipment | transfer_with_acknowledgment | Central dispatches 1000 doses |
| Regional → District | Child shipment A | transfer_with_acknowledgment | Regional forwards 500 to District A |
| Regional → District | Child shipment B | transfer_with_acknowledgment | Regional forwards 500 to District B |
| District → Site | Grandchild shipment | transfer_with_acknowledgment | District forwards 100 to Site X |

Each child subject references the parent via a `parent_shipment_ref` field in the dispatch shape's payload. The PE can trace the full tree: parent → children → grandchildren.

**Parent aggregate state**: "fully distributed" = all child subjects in `received` or `resolved`. "Partially distributed" = at least one child still in `in_transit`. This is a **deployer-configured L1 projection rule** — the pattern itself handles a single handoff level. Cross-subject aggregations over the parent-child tree are projection concerns, not pattern machinery.

**Embedding rule**: transfer_with_acknowledgment consumes the subject-level slot (Rule 1). capture_with_review can overlay (Rule 2) for review of dispatch or receipt records.

---

## §7 Composition Validation

The 5 composition rules from ADR-5 S6 [C18], validated against the concrete patterns defined above.

### 7.1 Rule 1: One Subject-Level Pattern per Activity

**Rule**: an activity binds at most one subject-level state machine to a subject.

**Subject-level patterns**: case_management, multi_step_approval (primary mode), transfer_with_acknowledgment.

**Event-level patterns**: capture_with_review, multi_step_approval (embedded mode).

| Test case | Subject-level | Event-level | Valid? |
|-----------|--------------|-------------|--------|
| Malaria case tracking | case_management | capture_with_review (overlay) | ✓ — one subject-level |
| Malaria case + drug approval | case_management | capture_with_review + multi_step_approval (embedded) | ✓ — one subject-level, two event-level |
| Field report approval chain | multi_step_approval (primary) | capture_with_review (overlay) | ✓ — one subject-level |
| Supply distribution | transfer_with_acknowledgment | — | ✓ — one subject-level |
| ✗ Case tracking + distribution | case_management + transfer_with_acknowledgment | — | ✗ — two subject-level patterns on same subject |

The last case is rejected at deploy-time [C18]. Case tracking and distribution are separate activities on different subjects (cases vs shipments), linked by `activity_ref` (Rule 4).

### 7.2 Rule 2: Event-Level Patterns Compose Freely

**Rule**: event-level patterns track per-event states. No conflict because the state spaces are disjoint.

| Test case | Subject-level | Event-level | State spaces |
|-----------|--------------|-------------|-------------|
| Case + review + approval | case_management | capture_with_review + multi_step_approval | Case state ∈ {opened, active, ...}. Review state ∈ {pending_review, accepted, returned}. Approval state ∈ {pending, returned, final_*}. All disjoint. |

PE maintains three independent state lookups:
1. `subject_state[subject, activity] → case state` (case_management)
2. `event_state[event_id, capture_with_review] → review state` (per capture event)
3. `event_state[event_id, multi_step_approval] → approval state` (per submission event)

No interference. Validated.

### 7.3 Rule 3: Approval Sub-Flows Embed

**Rule**: a subject under a subject-level pattern can have multi_step_approval sub-flows scoped to specific submission events.

**Concrete test** (from exploration/20 §1.2): malaria case tracking with drug regimen change approval.

| # | Event | Shape owner | Case state | Approval state |
|---|-------|------------|------------|----------------|
| 1 | Case opened (opening) | case_management | `opened` | — |
| 2 | Follow-up (interaction) | case_management | `active` | — |
| 3 | Drug change request (submission) | multi_step_approval | `active` (unchanged) | `pending` {L1} |
| 4 | L1 approves (level_decision) | multi_step_approval | `active` (unchanged) | `pending` {L2} |
| 5 | L2 approves (level_decision) | multi_step_approval | `active` (unchanged) | `final_approved` |
| 6 | Follow-up (interaction) | case_management | `active` (SP) | — |

The `drug_change_request` shape belongs exclusively to multi_step_approval. case_management does not define a transition for it — the event is unknown to case_management and ignored. The case stays `active` throughout. The approval has its own progression tracked in the event_state_table. Validated.

### 7.4 Rule 4: Cross-Activity via activity_ref

**Rule**: patterns don't span activities. Cross-activity linking uses `activity_ref` (envelope field) or payload cross-references.

**Concrete test** (from exploration/20 §1.2): vaccination campaign + supply distribution.

| Activity | Subject type | Pattern | Link |
|----------|-------------|---------|------|
| `measles_campaign_2026` | Geographic area / individual | (campaign-specific — outside initial inventory) | `activity_ref: measles_campaign_2026` |
| `measles_supply_distribution` | Shipment | transfer_with_acknowledgment | `activity_ref: measles_campaign_2026` in dispatch events |

Different subjects, different activities, different patterns, linked by shared `activity_ref`. No pattern spans the boundary. Validated.

### 7.5 Rule 5: Shape-to-Pattern Unique Within Activity

**Rule**: deploy-time validation ensures no two patterns define competing state transitions for the same shape [C18].

**Concrete test**: malaria case tracking with case_management + capture_with_review + embedded multi_step_approval.

| Shape (concrete) | Transition-bound to | Activation-bound to |
|-----------------|--------------------|--------------------|
| `malaria_case_opening/v1` | case_management (opening) | — |
| `malaria_follow_up/v1` | case_management (interaction) | capture_with_review (activation set) |
| `malaria_case_outcome/v1` | case_management (resolution) | — |
| `malaria_closure_review/v1` | case_management (closure_review) | — |
| `follow_up_review/v1` | capture_with_review (review_decision) | — |
| `drug_change_request/v1` | multi_step_approval (submission) | — |
| `approval_decision/v1` | multi_step_approval (level_decision) | — |

Transition-bound column: no shape appears in two patterns. Activation-bound (`malaria_follow_up/v1`) is referenced by capture_with_review for instance creation but is not transition-bound to it — the pattern does not define a state transition triggered by that shape. Rule 5 satisfied.

**Deploy-time validator checks** [C18]:
1. Build the transition-bound shape set for each pattern.
2. Assert: intersection of all transition-bound sets = ∅.
3. Assert: activation-bound shapes exist in the activity's known shape set.
4. Assert: at most one pattern has composition type = subject-level.

---

## §8 S06 Disposition: entity_lifecycle

### 8.1 The Question

ADR-5 validated 4 existence-proof patterns. Exploration/20 §1.2 Scenario 3 walked a facility registry (S06) where entity updates needed review, and mentioned `entity_lifecycle` as a pattern. The question: is entity_lifecycle a 5th pattern, or can S06 be handled by parameterizing case_management?

### 8.2 S06 Walk-through

**Scenario**: [S06](../scenarios/06-entity-registry-lifecycle.md) — maintaining a known set of things. Things are registered, updated, verified, may be deprecated, and may be reactivated.

| Step | Actor | Event type | Shape role | State after |
|------|-------|-----------|------------|-------------|
| 1 | Field worker | `capture` | `registration` | `registered` |
| 2 | Field worker | `capture` | `update` | `active` |
| 3 | Field worker | `capture` | `update` | `active` (SP) |
| 4 | Verifier | `review` | `verification` | `verified` |
| 5 | Field worker | `capture` | `update` | `active` |
| 6 | Verifier | `review` | `verification` | `verified` |
| 7 | Admin | `review` | `deprecation` | `deprecated` |
| 8 | Admin | `capture` | `reactivation` | `active` |

### 8.3 Attempted Mapping to case_management

| entity_lifecycle concept | case_management role | Mapping quality |
|-------------------------|---------------------|-----------------|
| `registered` → `opened` | Opening | Clean ✓ |
| `active` → `active` | Active with interactions | Clean ✓ |
| `update` → `interaction` | State-preserving interaction | Clean ✓ |
| `verification` → `closure_review` {closed} | Resolution + closure | **Poor** — verification is periodic, not a one-time resolution |
| `deprecated` → `closed` | Closed | Structural fit, but `verified` ≠ `resolved` |
| `reactivation` → `reopening` | Reopening | Clean ✓ |
| `referred` → (n/a) | Referral | No entity_lifecycle counterpart — deployer omits the shape |

### 8.4 Structural Differences

Two differences make the mapping fail:

**Difference 1: Updates after verification are normal, not violations.**

In case_management, an `interaction` after `resolved` is a transition violation (T3 is only valid from `active`; no outbound interaction from `resolved`). The CHV is warned, the event is flagged.

In entity_lifecycle, an `update` after `verified` is **expected behavior** — the entity's data changed, so it needs re-verification. The state transitions from `verified` back to `active`. This is not a violation; it's the normal lifecycle cycle.

If mapped to case_management, every post-verification update would produce a `transition_violation` flag. This defeats the purpose — the flags would be false positives, overwhelming the resolution queue with non-issues.

**Difference 2: Verification is cyclical, not terminal.**

case_management's `resolved → closed` is a linear progression toward case completion. The case is expected to reach `closed` and stay there (reopening is exceptional).

entity_lifecycle's `active → verified` is a repeating cycle. Entities are expected to go through verification periodically (every 6–12 months). The dominant lifecycle is `active → verified → active → verified → ...`, indefinitely. Verification is a checkpoint, not a conclusion.

### 8.5 Hypothetical entity_lifecycle State Machine

If defined as a separate pattern:

**Composition type**: `subject-level`

**States**: `registered` [I], `active`, `verified`, `deprecated` [Q]

**Transition table**:

| ID | From | event.type | Shape role | → To | |
|----|------|-----------|------------|------|---|
| T1 | — | `capture` | `registration` | `registered` | SC* |
| T2 | `registered` | `capture` | `update` | `active` | SC |
| T3 | `active` | `capture` | `update` | `active` | SP |
| T4 | `active` | `review` | `verification` | `verified` | SC |
| T5 | `verified` | `capture` | `update` | `active` | SC |
| T6 | `verified` | `review` | `verification` | `verified` | SP |
| T7 | `active`, `verified` | `review` | `deprecation` | `deprecated` | SC |
| T8 | `deprecated` | `capture` | `reactivation` | `active` | SC |

Key structural features:
- T5 (`verified` → `active` on update) is a normal transition, not a violation. This is the fundamental difference from case_management.
- T6 (re-verification while already verified) is state-preserving — refreshes verification date.
- T4 and T5 form the cyclical verification loop: `active → verified → active → verified → ...`
- No `referred` state. No multi-step closure. Simpler than case_management.

### 8.6 Disposition

**entity_lifecycle is a separate pattern.** It cannot be expressed as a parameterization of case_management because the transition semantics differ structurally:

| Behavior | case_management | entity_lifecycle |
|----------|----------------|-----------------|
| Update after verification/resolution | `transition_violation` flag | Normal transition (→ active) |
| Verification/resolution model | Linear progression toward closure | Cyclical checkpoint |
| Expected endpoint | `closed` (quiescent) | No endpoint; entities live indefinitely |
| Transition count | 11 (T1–T11) | 8 (T1–T8) |

**Inventory recommendation**: entity_lifecycle is a valid 5th pattern candidate. The architecture supports it without changes (subject-level, Rule 1 compliant, all constraints satisfied). Whether it ships in the initial inventory is an implementation decision:

- **Include if**: S06-like scenarios (entity registries) appear in early deployments.
- **Defer if**: early deployments focus on case management and reporting (S04/S08). entity_lifecycle can be added as a platform evolution with zero data migration.

The pattern inventory is append-only [5-S5]. Adding entity_lifecycle later carries no architectural cost.

---

## §9 Findings and Inventory Summary

### 9.1 Key Findings

**F1: All 4 patterns use only 3 of 6 event types.**

| Event type | Patterns that use it |
|-----------|---------------------|
| `capture` | All 4 |
| `review` | All 4 |
| `assignment_changed` | case_management (T10, state-preserving) |
| `alert` | None (used by L3b triggers, not pattern transitions) |
| `task_created` | None (used by L3a triggers, not pattern transitions) |
| `task_completed` | None (used by trigger chain completion) |

Patterns define lifecycle and review flows. Triggers (`alert`, `task_created`, `task_completed`) react to lifecycle events but don't participate in state derivation. This confirms ADR-5 S4: patterns are projection concerns, decoupled from trigger machinery.

**F2: Shape binding has two modes, not one.**

Exploration/19's sketches assumed each shape in a pattern is transition-bound. The composition validation (§7.5) revealed that capture_with_review in overlay mode uses activation-binding — it observes shapes from other patterns to create instances, without competing for state transitions. Rule 5 applies only to transition-bound shapes.

This distinction was implicit in the exploration/20 analysis ("patterns operate on different concerns") but not formally declared. §1.5 makes it explicit.

**F3: Parameterization controls pattern scope.**

Optional shape_role mappings serve as feature gates:
- case_management without `shapes.reopening` → `closed` is terminal
- case_management without `shapes.referral` → no referral transitions
- transfer_with_acknowledgment without `shapes.discrepancy_report` → no dispute tracking

The deployer doesn't modify the state machine; they control its surface area through presence/absence of shape mappings.

**F4: Multi-level distribution is pattern composition, not a separate pattern.**

S14 (multi-level distribution) is handled by multiple instances of transfer_with_acknowledgment on a tree of related subjects. No new pattern machinery needed. Parent-child relationships use payload cross-references. Aggregate state ("fully distributed") is a projection concern.

**F5: entity_lifecycle is structurally distinct from case_management.**

The cyclical verification model and "updates don't violate verified state" semantics make entity_lifecycle a separate pattern, not a parameterization. See §8 for full analysis.

**F6: S00 confirms P7 (simplest scenario stays simple).**

No-pattern activities are valid. Pattern selection at L0 is opt-in. S00 requires 2 deployer artifacts, no pattern configuration. The Pattern Registry adds zero overhead to scenarios that don't use it.

### 9.2 Inventory Summary Table

| Pattern | Composition | States | Transitions | Shape roles | Participant roles | Parameterization points |
|---------|-------------|--------|-------------|-------------|------------------|------------------------|
| `capture_with_review` | event-level | 3 (pending_review, accepted, returned) | 3 (T1–T3) | 2 (activation set, review_decision) | 2 (capturer, reviewer) | activation shapes, review shape, roles×2, review deadline |
| `case_management` | subject-level | 6 (opened, active, referred, resolved, closed, reopened) | 11 (T1–T11) | 8 (opening, interaction, referral, resolution, closure_review, reopening, transfer, case_review) | 2 (assigned_worker, supervisor) | shapes×8 (3 optional), roles×2, deadlines×3 |
| `multi_step_approval` | subject-level / event-level | 4 (pending, returned, final_approved, final_rejected) + current_level attribute | 6 (T1–T6) | 2 (submission, level_decision) | 1+N (submitter, level_N_reviewer×N) | levels, shapes×2, roles×(1+N), review deadline |
| `transfer_with_acknowledgment` | subject-level | 5 (in_transit, received, partial_receipt, disputed, resolved) | 5 (T1–T5) | 4 (dispatch, receipt, discrepancy_report, discrepancy_resolution) | 3 (sender, receiver, supervisor) | shapes×4 (2 optional), roles×3, deadlines×2 |

**Candidate pattern (not in initial inventory)**:

| Pattern | Composition | States | Transitions | Disposition |
|---------|-------------|--------|-------------|-------------|
| `entity_lifecycle` | subject-level | 4 (registered, active, verified, deprecated) | 8 (T1–T8) | Separate pattern; defer to platform evolution |

### 9.3 Architecture Consistency Checks

| Check | Result |
|-------|--------|
| All transitions use only the 6 platform-fixed event types | ✓ |
| All patterns use shape_roles, not concrete shape_refs | ✓ |
| Flagged events excluded from state derivation [5-S2] | ✓ — applies uniformly to all patterns |
| Pattern definitions sync via atomic config [4-S6, C20] | ✓ — patterns are config, not events |
| Deploy-time validation enforces Rules 1–5 [C18] | ✓ — validated in §7 with concrete examples |
| Command Validator is advisory [5-S4] | ✓ — warns on-device, never blocks |
| Composition rules accommodate embedding [5-S6 Rule 3] | ✓ — multi_step_approval dual mode validated |
| S00 works without any pattern [P7] | ✓ — validated in §2 |

### 9.4 Contracts Satisfied

| Contract | What this document validates |
|----------|----------------------------|
| C15 (PR → PE) | Each pattern defines: states, transitions, auto-maintained projections, parameterization. PE has everything needed for state derivation. |
| C16 (PR → CD) | Each pattern defines valid transitions. CD can evaluate incoming events against transition tables and raise `transition_violation` flags. |
| C18 (PR → DtV) | Rules 1–5 validated against concrete patterns. Shape uniqueness, composition type constraints, and role mapping completeness are all checkable at deploy time. |

### 9.5 Open Items for Implementation

| Item | What remains | Where it lives |
|------|-------------|---------------|
| Pattern schema format | Exact file/data format for pattern definitions in config packages | Implementation scope |
| PE evaluation algorithm | How PE applies transition tables efficiently (hash lookup on (state, type, shape_role)) | Implementation scope |
| Projection extraction rules | How pattern-specific projections (items_dispatched, approval_chain) extract values from payloads | L1 projection rules or pattern-specific extractors |
| entity_lifecycle timing | When/whether to add as 5th pattern | Platform evolution decision |
| Additional patterns | Future scenarios may require patterns not yet defined (e.g., scheduled_collection, periodic_assessment) | Pattern Registry is append-only [5-S5] |
