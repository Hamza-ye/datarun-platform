> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-005**. For a navigated reading guide, see [guide-adr-005.md](../guide-adr-005.md).
# ADR-005 Session 1: Scoping, Event Storm, and the Pivotal Question

> Phase: 1 of 3 — Scoping & Event Storm
> Date: 2026-04-13
> Input: ADR-001 (storage), ADR-002 (identity + conflict), ADR-003 (authorization + sync), ADR-004 (configuration boundary), S04, S07, S08, S11, S14, S06
> Purpose: Define ADR-5's decision surface, event-storm four key scenarios through the workflow lens, and resolve Q1 — whether state machines are a platform primitive or a projection pattern. Q2 (`status_changed` as 7th type) falls directly out of Q1.

---

## 1. What ADR-5 Must Decide

ADR-5 answers one question: **How does work move through stages?**

ADRs 1–3 answered "how does the engine work?" ADR-4 answered "where does the engine end and the deployment begin?" ADR-5 asks a behavioral question: given an append-only, offline-first event store with typed shapes, platform-fixed event types, server-only triggers, and a four-layer configuration gradient — how do multi-step, multi-actor workflows compose?

S04, S08, S11, and S14 all describe work that progresses through stages with multiple actors. The platform cannot express any non-trivial workflow until ADR-5 decides whether state machines are primitives or patterns, and how multi-actor handoffs compose within the constraints of all four prior ADRs.

### 1.1 The Decision Surface

Six questions queued from ADR-004's "Next Decision" section. Unlike ADR-4's 12 questions, these cluster tightly around one theme: state progression.

| # | Question | Source | Nature | Envelope? |
|---|----------|--------|--------|-----------|
| Q1 | **State machine as platform primitive or projection pattern?** Should the platform enforce transition rules (primitive: invalid transitions are rejected) or derive state from event sequences (pattern: projections compute current state, shapes encode valid next steps)? | ADR-1 deferral, ADR-4 Session 3 Part 1 | Boundary + Technical | Unlikely — state is derived from events already stored |
| Q2 | **`status_changed` as 7th structural event type?** Does a state transition require different platform processing from `capture`? | ADR-4 Session 3 Part 3 carry-forward | Technical | Yes — adds to the closed type vocabulary |
| Q3 | **Multi-step, multi-actor workflow composition** How do workflows with sequential stages (A submits → B reviews → C approves) compose in an offline-first, append-only model? | S08, S11 | Boundary + Technical | No |
| Q4 | **Workflow–flag cascade interaction** What happens when an upstream event is flagged (ADR-2 accept-and-flag) after downstream workflow steps have already fired? | ADR-2 detect-before-act model | Technical | No |
| Q5 | **`context.*` expression scope** Should form-context expressions access pre-resolved actor/workflow context (e.g., case status, assigned facility attributes)? | ADR-4 Session 3 stress test carry-forward | Technical | No |
| Q6 | **Domain conflict resolution automation (Q7b)** Can deployers configure auto-resolution rules for specific flag types? How does auto-resolution interact with state machines? | ADR-4 Session 3 Part 4 deferral | Boundary | No |

### 1.2 Irreversibility Prediction

**The envelope is closed at 11 fields.** ADR-5 is not expected to add fields. The only irreversible-surface candidate is Q2 — adding `status_changed` to the type vocabulary. But ADR-4 S3 established that the type vocabulary is append-only and adding a type is a code change, not a data migration. This makes Q2 a **low-cost irreversible** — easy to add, impossible to remove, but wrong-choice recovery is manageable (if `status_changed` turns out unnecessary, it persists as an unused type; no harm to existing events).

Everything else — state representation, composition model, flag cascade rules, expression scope, auto-resolution — lives in projection logic, server-side processing, pattern definitions, or configuration semantics. These are strategies, not constraints.

**Consequence**: Session 2's stress test should be proportionally lighter than ADR-4's. The irreversibility filter will likely produce zero Tier 1 items (nothing touching the envelope), with Q2 as the sole borderline case.

### 1.3 Upstream Assumptions

All upstream inputs are **committed** — no assumptions or leans:

- ADR-001 **committed**: Immutable events, append-only, client-generated UUIDs, event as sync unit.
- ADR-002 **committed**: Four identity types, accept-and-flag, detect-before-act, single-writer conflict, merge=alias, events never rejected for staleness.
- ADR-003 **committed**: Assignment-based access, sync=scope, authority-as-projection, scope-containment.
- ADR-004 **committed**: 6 structural event types (capture, review, alert, task_created, task_completed, assignment_changed), typed shapes via shape_ref, activity_ref optional, four-layer gradient, server-only triggers (L3a event-reaction depth-2 DAG, L3b deadline-check), expression language (operators + field references, zero functions), complexity budgets (hard, deploy-time enforced).

This is the first ADR exploration with zero upstream assumptions. Everything is committed.

---

## 2. Event Storm: Four Scenarios Through the Workflow Lens

### Method

For each scenario, the event storm asks three questions that previous walk-throughs did not:

1. **What state transitions exist?** — Map the lifecycle phases of the subject.
2. **Can the transitions be expressed using the existing 6 types + shapes + triggers?** — The critical test for Q1.
3. **Where do multi-actor handoffs create tension with offline-first?** — The concurrency question.

The S04 and S08 walk-throughs in `002-walk-through-scenarios-primitives.md` already identified Command Validator and Assignment Resolver as candidate primitives, and surfaced the question of state-as-emergent-property vs. explicit-tracking. This session revisits those scenarios with ADR-4's settled vocabulary in hand, and adds S11 and S07 for the multi-step and multi-actor dimensions.

---

### 2.1 S04 — Supervisor Review (State Transitions in Submit-Review Cycle)

**Lifecycle phases**: captured → pending_review → [accepted | returned] → (if returned) corrected → pending_review → ...

**Event sequence using existing types**:

| Step | Actor | Event type | Shape | Subject state after |
|------|-------|-----------|-------|-------------------|
| 1. CHV records observation | CHV | `capture` | `household_observation/v1` | captured (implicit: no review event exists yet) |
| 2. Supervisor reviews, accepts | Supervisor | `review` | `review_decision/v1` (payload: decision=accepted, source_event_ref) | accepted |
| 2b. Supervisor reviews, returns | Supervisor | `review` | `review_decision/v1` (payload: decision=returned, reason) | returned |
| 3. CHV corrects, resubmits | CHV | `capture` | `household_observation/v1` (correction — new event, not edit) | corrected → pending_review |
| 4. Supervisor re-reviews | Supervisor | `review` | `review_decision/v1` | accepted (or returned again) |

**Can existing types express this?** Yes. `capture` + `review` cover every step. The subject's lifecycle state is **projection-derivable**:

```
state(subject) = 
  if no events → does_not_exist
  if last event is capture → pending_review
  if last review.decision == accepted → accepted
  if last review.decision == returned → returned
```

The projection walks events in causal order (device_seq + sync_watermark) and computes current state. No new type needed. No new envelope field needed.

**Where offline creates tension**:

**Scenario A — Concurrent capture and review**: CHV is offline, captures a new observation for the same subject. Meanwhile, supervisor (also offline or on server) reviews the previous observation. Both sync. Result: the subject has a new capture AFTER a review. The projection computes: last event is capture → pending_review. The review for the old capture is part of the history. The new capture needs its own review. This is **correct behavior** — no conflict, no special handling. The projection naturally handles it.

**Scenario B — Supervisor reviews stale data**: Supervisor's device has the capture event. CHV corrects the observation offline (new capture event). Supervisor reviews the old version. Both sync. Now the subject has: capture_v1 → capture_v2 (correction) → review (of v1). The review targeted the wrong version.

This is a **stale-review problem**, not a state machine problem. ADR-2's accept-and-flag model handles it: the review event references `source_event_ref` (the v1 capture). A flag can be raised: "review references a superseded capture." But the events are all valid and stored. The projection shows: latest capture = v2 (unreviewed), review of v1 = accepted. A human decides whether to re-review.

**Finding**: S04's workflow composes cleanly with `capture` + `review`. State is projection-derived. Offline concurrency is handled by event ordering + accept-and-flag. No new primitives needed.

---

### 2.2 S08 — Case Management (Long-Running, Multi-Actor State Progression)

**Lifecycle phases**: opened → active (accumulating interactions) → [referred | transferred | resolved | reopened] → ... → closed

This is the hardest workflow scenario. A case may accumulate 50–100 events over weeks or months. Responsibility shifts between actors. State progression is non-linear (a case can be reopened, re-referred, transferred multiple times).

**Event sequence using existing types**:

| Step | Actor | Event type | Shape | Notes |
|------|-------|-----------|-------|-------|
| 1. Identify problem | CHV | `capture` | `case_opening/v1` | Subject created. payload includes initial assessment. |
| 2. Record interaction | CHV/Nurse | `capture` | `case_interaction/v1` | payload: visit notes, measurements, decisions |
| 3. Request review | CHV | `capture` | `review_request/v1` | payload: reason for referral, urgency |
| 4. Supervisor reviews | Supervisor | `review` | `case_review/v1` | payload: decision (continue, refer, close), notes |
| 5. Transfer responsibility | Supervisor | `assignment_changed` | `case_transfer/v1` | payload: from_actor, to_actor, reason |
| 6. Record resolution | CHV/Nurse | `capture` | `case_resolution/v1` | payload: outcome, resolution_type |
| 7. Close case | Supervisor | `review` | `case_closure_review/v1` | payload: decision=closed, final_assessment |
| 8. Reopen case | Any assigned | `capture` | `case_reopening/v1` | payload: reason, new_assessment |

**Can existing types express this?** Partially — but stress points appear.

**What works**: Every data-recording step maps to `capture`. Reviews map to `review`. Transfers map to `assignment_changed`. The 6-type vocabulary covers the structural processing needs.

**Stress Point 1 — State derivation complexity**:

The case state is derivable from events, but the derivation logic is significantly more complex than S04:

```
state(case) =
  start: does_not_exist
  on case_opening → opened
  on case_interaction when state==opened → active  
  on case_interaction when state==active → active (no change)
  on case_resolution → resolved
  on case_closure_review where decision==closed → closed
  on case_reopening when state in [resolved, closed] → reopened → active
  on case_transfer → active (responsibility shifted, not state)
```

This is a **state machine** — it has named states, explicit transitions, and invalid transitions (you can't close a case that hasn't been resolved; you can't reopen a case that's still active). The question is: who enforces the transition rules?

**Option A — Projection-only (pattern)**: The projection computes current state from events. The shapes encode the valid transitions in their presence/absence. No enforcement — if a CHV submits a `case_interaction` for a closed case, the event is accepted (append-only), and a flag is raised ("interaction submitted for closed case"). A human or trigger handles the flag.

**Option B — Platform-enforced (primitive)**: The platform maintains a state machine definition per workflow pattern. The Command Validator checks: "given current state = closed, is capture(case_interaction) a valid transition?" If not, the write is rejected before the event is created.

**But Option B conflicts with ADR-1 and ADR-2.** Events are never rejected for staleness (ADR-2 S14). The append-only model (ADR-1 S1) means every event is stored. If a CHV is offline with a stale projection showing the case as "active" when it's actually "closed," they may legitimately spend an hour filling in a case interaction form. Rejecting that event on sync violates the core contract: **work done offline is never lost.**

**Resolution Direction**: Option A with enhancement. The projection derives state. Transition violations are **flagged, not rejected**. The flag is: "this event represents a transition that is invalid according to the current state machine definition." The event is stored (immutable, append-only), the projection incorporates it with a violation marker, and a human or auto-resolution policy (Q6) handles the flag.

This preserves:
- ADR-1: append-only, no event rejected
- ADR-2: accept-and-flag, detect-before-act
- ADR-4: shapes define the payload, types define processing behavior
- Offline-first: work done offline is always accepted

**Stress Point 2 — Who defines valid transitions?**

If state is projection-derived and transitions are flagged-not-rejected, the transition rules must live somewhere. Where?

They are **configuration** — defined per shape combination within a pattern. The deployer declares:

```
"When the subject has a case_opening event and no case_resolution event, 
the valid next shapes are: case_interaction, case_transfer, review_request.
If any other shape arrives, flag it."
```

This is a **state machine definition at the shape level** — which shapes are valid after which shapes, given the current derived state. It lives in Layer 1 (shape-level configuration) or as part of the pattern definition (Layer 0).

**Stress Point 3 — Transfer as state-neutral event**:

A `case_transfer` (assignment_changed) shifts responsibility without changing the case's lifecycle state. The case is still "active" — just with a different responsible actor. This is important: **not every event is a state transition**. Some events are state-preserving interactions. The state machine must distinguish between state-changing events ("case opened," "case resolved") and state-preserving events ("interaction recorded," "responsibility transferred").

**Where offline creates tension**:

**Scenario C — Concurrent resolution**: Two actors (nurse and CHV) both offline. Nurse records a `case_resolution`. CHV records a `case_interaction`. Both sync. The case now has: interaction (from CHV, stale view) + resolution (from nurse). Causal order: whichever `device_seq` + `sync_watermark` determines ordering.

If the resolution came first (causally), the interaction is a post-resolution event → flagged as "interaction after resolution." If the interaction came first, the resolution follows a valid path. ADR-2's causal ordering resolves this — no new mechanism needed. The flag ("event arrived after a state-changing event that the actor hadn't seen") is the same accept-and-flag pattern used for identity conflicts.

**Scenario D — Concurrent transfer**: Supervisor A transfers the case to Actor B. Separately, Supervisor A (stale view) or Supervisor C records a review for the case. Both sync. The review may target the wrong assignee.

Again, this is a stale-data flag, not a state machine problem. The review is valid (it happened), the transfer is valid (it was authorized). The projection shows: current assignee = Actor B, last review = by Supervisor A/C. If that review needs Actor B's attention, a trigger or human handles the routing.

**Finding**: S08 confirms that state machines are **projection patterns, not enforcement primitives**. The append-only + offline-first constraints make rejection-based enforcement incompatible. State is derived. Transitions are flagged when invalid. The flagging logic needs state machine definitions — which are deployer-configured workflow descriptions, not platform-hardcoded rules.

---

### 2.3 S11 — Multi-Step Approval (Sequential, Multi-Level Judgment Chain)

**Lifecycle phases**: submitted → level_1_review → [approved_L1 | returned_L1 | rejected_L1] → level_2_review → [approved_L2 | returned_L2 | rejected_L2] → ... → final_approved | final_rejected

This scenario stresses two things S04 and S08 don't: **sequential ordering across multiple actors** and **chain stalling visibility**.

**Event sequence using existing types**:

| Step | Actor | Event type | Shape | Subject state after |
|------|-------|-----------|-------|-------------------|
| 1. Worker submits work | Worker | `capture` | `field_report/v1` | submitted |
| 2. District officer reviews | District officer | `review` | `approval_decision/v1` (payload: level=1, decision=approved) | approved_L1 |
| 3. Regional manager reviews | Regional manager | `review` | `approval_decision/v1` (payload: level=2, decision=approved) | approved_L2 |
| 4. National director reviews | National director | `review` | `approval_decision/v1` (payload: level=3, decision=approved) | final_approved |
| Alt: Any level returns | Reviewer | `review` | `approval_decision/v1` (payload: level=N, decision=returned, reason) | returned_LN |
| Alt: Worker corrects and resubmits | Worker | `capture` | `field_report/v1` (new version) | resubmitted → level 1 |

**Can existing types express this?** Yes — `capture` + `review` cover every step, just as in S04. The shape payload carries the level and decision. The structural event type is the same (`review`); what differs is the domain meaning in the payload.

**State derivation**:

```
state(subject) =
  if last event is capture → submitted (restart from level 1)
  if last review at level N, decision=approved →
    if N == max_level → final_approved
    else → pending_level_(N+1)
  if last review at level N, decision=returned → returned_LN
  if last review at level N, decision=rejected → final_rejected
```

This is a **parameterized state machine** — the number of levels is deployer-configured, not platform-fixed. The projection logic is generic: "walk events, track the highest approved level, compute where in the chain the subject currently sits."

**What's new vs. S04**: The chain length is variable and deployer-defined. The assignment at each level is different (district officer → regional manager → national director). The platform must support: "for level N, the reviewer is the actor assigned with role X in the scope that contains the subject's scope."

This is an **Assignment Resolver concern** — ADR-3's assignment-based access model already provides the primitives (role + scope + assignment). The workflow pattern defines: "at level N, the reviewer role is X." The Assignment Resolver computes: "who holds role X in the scope that contains this subject?"

**Where offline creates tension**:

**Scenario E — Skip-level review**: District officer is offline. Regional manager sees the submission (perhaps through a different sync path or wider scope). Regional manager reviews it as level 2 without level 1 approval.

This is **structurally identical** to the stale-review problem in S04. The level-2 review arrives without a preceding level-1 approval. The projection shows: submitted → approved_L2 (gap at L1). A flag is raised: "level-2 approval without level-1 approval." The events are all stored. A human resolves by either: (a) the district officer retroactively reviews (adding a level-1 event that fills the gap in the projection), or (b) the regional manager's review is authoritative and the gap is accepted.

**Scenario F — Return while already re-reviewed**: Level-1 reviewer returns the submission. Meanwhile (offline), level-2 reviewer (with stale data showing L1 approval) approves it. Both sync. The subject now has: returned_L1 + approved_L2. The causal ordering determines the projection view. If the return is causally first, the L2 approval is flagged ("approval of returned submission"). If the L2 approval is causally first, the return comes after an already-approved L2. Either way: accept-and-flag, human resolves.

**Finding**: S11 confirms projection-derived state with parameterized level count. Multi-level chains are **composed from repeated `review` events with level metadata in the shape payload**. The pattern is generic and the number of levels is deployer-configured. No new event types needed.

---

### 2.4 S07 + S14 — Resource Distribution and Multi-Level Handoffs

**Lifecycle phases**: dispatched → in_transit → [received | partially_received | disputed] → (if multi-level) forwarded → in_transit → received → ...

This scenario introduces **two-party coordination**: a sender and receiver must both contribute events for a single handoff. And in S14, the chain has multiple levels (central → regional → district → site).

**Event sequence using existing types**:

| Step | Actor | Event type | Shape | Subject state after |
|------|-------|-----------|-------|-------------------|
| 1. Central dispatches supplies | Central officer | `capture` | `dispatch_record/v1` (payload: items, quantities, destination) | dispatched |
| 2. Regional confirms receipt | Regional officer | `capture` | `receipt_confirmation/v1` (payload: items_received, discrepancies) | received_at_regional |
| 3. Regional forwards to district | Regional officer | `capture` | `dispatch_record/v1` (payload: items, quantities, destination=district) | dispatched_to_district |
| 4. District confirms receipt | District officer | `capture` | `receipt_confirmation/v1` | received_at_district |
| 5. Discrepancy reported | Any receiver | `capture` | `discrepancy_report/v1` (payload: expected vs actual, explanation) | disputed |
| 6. Discrepancy investigated | Supervisor | `review` | `discrepancy_review/v1` (payload: resolution, adjusted_quantities) | resolved |

**Can existing types express this?** Yes. Every step is either `capture` (recording what happened — dispatch, receipt, discrepancy) or `review` (investigating a discrepancy). No task creation needed. No assignment change (the chain is pre-configured in the campaign/activity definition).

**But there's a new structural element**: The subject identity shifts across levels. The central dispatch creates **one subject** (the shipment). But at regional level, the shipment may be split into multiple sub-shipments for different districts. Each sub-shipment is a new subject linked to the parent.

This is **ADR-2's identity model**: the sub-shipment subject references the parent shipment via `subject_ref` relationships. The projection can trace the full tree: parent shipment → child sub-shipments → per-site receipts.

**State derivation**:

```
state(shipment) =
  if dispatch_record exists, no receipt → in_transit
  if receipt_confirmation exists →
    if discrepancies == none → received
    if discrepancies exist, no review → disputed
    if discrepancy_review exists → resolved
  if dispatch_record exists for child subjects → partially_forwarded / fully_forwarded
```

This is a **tree-structured state machine** — the parent subject's state depends partly on the states of its child subjects. "Fully forwarded" means all child dispatch events exist. "Fully received" means all child receipts exist.

**Where offline creates tension**:

**Scenario G — Dispatch and receipt cross in transit**: Central dispatches (event created offline). Regional receives the physical goods and creates a receipt event (also offline, before the dispatch event syncs). Both sync. The receipt event appears without a corresponding dispatch event. The projection handles this: receipt exists but dispatch doesn't → flagged ("receipt without dispatch"). When the dispatch syncs, the projection updates and the flag resolves.

This is ADR-2's stale-data model again. Accept both events. The projection eventually reconciles.

**Scenario H — Partial receipt at one level, full dispatch at next**: Regional receives 80 out of 100 items. Regional dispatches 80 to district (correct — sending what they received). But the discrepancy hasn't been reviewed yet. The district receives 80, confirms receipt. Meanwhile, the discrepancy investigation finds the 20 missing items and they're sent separately.

No conflict. Each event is valid. The projection tracks: original dispatch (100) → receipt (80) → discrepancy (20 missing) → forward dispatch (80) → district receipt (80) → supplementary dispatch (20) → supplementary receipt (20). All captured as separate events. State is derived from the full event sequence.

**Finding**: S07/S14 confirms projection-derived state. Multi-level distribution is a **tree of subjects** with per-level state. The parent's "aggregate state" (fully distributed, partially distributed) is a projection over child subjects. No new event types needed. The `capture` type handles dispatches, receipts, and discrepancy reports — the domain meaning lives in the shape, not the type.

---

## 3. Cross-Storm Synthesis

### 3.1 What All Four Scenarios Share

| Property | S04 | S08 | S11 | S07/S14 |
|----------|-----|-----|-----|---------|
| State is derivable from events | ✅ | ✅ | ✅ | ✅ |
| State transitions are implicit (shape sequence) | ✅ | ✅ | ✅ | ✅ |
| Multiple actors contribute events | ✅ (CHV + supervisor) | ✅ (CHV + nurse + supervisor) | ✅ (worker + N reviewers) | ✅ (sender + receiver per level) |
| Offline concurrency creates tension | Stale review | Concurrent resolution | Skip-level review | Receipt without dispatch |
| ADR-2 accept-and-flag handles the tension | ✅ | ✅ | ✅ | ✅ |
| Existing 6 types sufficient | ✅ | ✅ | ✅ | ✅ |
| State machine complexity | Low (3 states) | High (6+ states, non-linear) | Medium (N levels × 3 outcomes) | Medium (per-level, tree-structured) |

### 3.2 The Critical Test: Can Workflows Be Expressed Without New Machinery?

The answer is **yes for event recording** and **partially for state enforcement**.

**What works without new machinery**:
- Every workflow action maps to an event with existing types.
- State is always derivable from event sequences using projection logic.
- Multi-actor handoffs are handled by assignment-based scoping (ADR-3) and shape-level role declarations.
- Offline concurrency is handled by ADR-2's accept-and-flag model.
- Delays and overdue items are handled by ADR-4's L3b deadline triggers.

**What needs new machinery**:
- **Transition validation**: Someone must define which shapes are valid in which states. This is configuration, not code — but it's a new configuration artifact type: a **state machine definition** that the projection engine and Command Validator use.
- **Transition flagging**: When an event represents an invalid transition (per the state machine definition), the system must flag it. This extends ADR-2's flagging taxonomy with a new flag category: `transition_violation`.
- **Pattern-level composition**: The deployer doesn't author state machines from scratch. They select a pattern (`capture_with_review`, `case_management`, `multi_step_approval`, `transfer_with_acknowledgment`) and parameterize it. The pattern provides the state machine skeleton. The deployer fills in: which shapes, which roles at each step, how many levels, what deadlines.

### 3.3 Resolving Q1: Primitive or Pattern?

**Q1: Should state machines be a platform primitive (with platform-enforced transition rules) or a projection pattern (derived from event sequences by configuration)?**

The event storms point strongly toward **projection pattern**, not platform primitive. The reasoning:

**1. Enforcement-based state machines conflict with the append-only, offline-first model.**

If the platform enforces transitions (rejects invalid events), offline workers can lose work. A CHV who fills out a case interaction form while offline — not knowing the case was closed — would have their event rejected on sync. This violates ADR-1 (append-only), ADR-2 S14 (events never rejected for staleness), and the core offline-first commitment (V1).

**2. State is always computable from events.**

In all four scenarios, the current state is unambiguously derivable from the event sequence. There is no scenario where you need a "state field" in the event to know the state — the combination of event type + shape + causal ordering determines it. Storing state in events would be redundant (violates DRY) and creates a consistency risk (what if the stored state disagrees with the derived state?).

**3. Transition rules are configuration, not constraints.**

Different deployments will define different valid transitions for the same structural pattern. One deployment's case management might allow reopening; another might not. One deployment's approval chain has 3 levels; another has 5. The transition rules are deployer-configured — they belong in the configuration gradient (L1 or pattern level), not in the platform's core processing pipeline.

**4. Flagging handles violations without losing data.**

Instead of rejecting invalid transitions, the platform accepts the event (preserving the offline-first contract) and flags it ("this event violates the configured transition rules for this subject's current state"). This is exactly ADR-2's accept-and-flag pattern — extended to a new flag category. The infrastructure already exists. The flag can be resolved by a human (supervisor decides the transition is actually valid given context) or by automation (Q6 auto-resolution).

**5. The pattern provides the skeleton; the deployer configures the specifics.**

The platform ships patterns (capture_with_review, case_management, multi_step_approval, transfer_with_acknowledgment). Each pattern defines:
- Participant roles (capturer, reviewer, approver, sender, receiver)
- Which structural event types participate (capture, review, assignment_changed)
- The state machine skeleton (which states, which transitions)
- Which transitions are state-changing vs. state-preserving
- What projections are auto-maintained (current state, assigned actor, pending duration)

The deployer selects a pattern, plugs in shapes and roles, parameterizes the level count or stages, and optionally adds L2 expressions and L3 triggers on top.

### Q1 Resolution: State machines are **projection patterns**.

Classification: **initial strategy** (not structural constraint, not strategy-protecting). Changing this decision is a code change to the projection engine, not a data migration. Events don't carry state — they don't change regardless of how state is computed. The state machine definition is configuration, stored server-side, synced to devices.

Confidence: **HIGH**. The append-only + offline-first constraints force this answer. An enforcement-based alternative would require either: (a) rejecting events (violates ADR-1/ADR-2), or (b) storing state in events (redundant, consistency risk, envelope change). Neither is viable.

---

## 4. Resolving Q2: `status_changed` as 7th Type

**Q2: Does a state transition require different platform processing from `capture`?**

With Q1 resolved as "projection pattern," Q2's framing shifts. The original question assumed that state machines might be platform primitives — in which case `status_changed` would signal "the platform's state machine engine should process this differently." With state as a projection, the question becomes: **is there any processing behavior that a state-transition event needs that `capture` doesn't already provide?**

### Testing against the event storms

| Scenario | What signals a state transition | Current type | Different processing needed? |
|----------|-------------------------------|-------------|----------------------------|
| S04 | Review decision (accept/return) | `review` | No — `review` already triggers review-status projection updates |
| S08 | Case resolution, case closure, reopening | `capture` with lifecycle shapes | See analysis below |
| S11 | Approval at each level | `review` | No — same as S04 |
| S07 | Receipt confirmation, discrepancy | `capture` | No — projection derives transit state |

**The S08 analysis**: When a CHV captures a `case_resolution` event, is the platform processing identical to capturing a `case_interaction` event? Let's compare:

| Processing step | `case_interaction` (state-preserving) | `case_resolution` (state-changing) |
|----------------|--------------------------------------|-----------------------------------|
| Shape validation | ✅ Validate against shape schema | ✅ Same |
| Subject linking | ✅ Link to subject_ref | ✅ Same |
| Projection update | ✅ Add to interaction history | ✅ Add to interaction history **+ update lifecycle state** |
| Trigger evaluation | ✅ L3a triggers for this shape | ✅ L3a triggers for this shape |
| Flag evaluation | ✅ Check for conflicts | ✅ Check for conflicts **+ check transition validity** |

The difference is in projection and flagging — not in the event pipeline. The projection engine knows which shapes are state-changing because the **pattern definition** declares it. The flag evaluator knows which transitions are valid because the **state machine configuration** declares it.

**Both of these are configuration-driven dispatching, not type-driven dispatching.** The platform doesn't need a different `type` to know "this event changes state" — it needs the pattern definition to declare "shape X is a state-changing shape in this pattern." The type tells the platform how to process the event structurally (validate payload, update projections, evaluate triggers). The shape + pattern tells the platform what domain significance the event has.

### Q2 Resolution: `status_changed` is **NOT needed** as a 7th type.

The domain distinction between state-changing and state-preserving events is expressed through:
- The **shape** (a `case_resolution` shape vs. a `case_interaction` shape)
- The **pattern definition** (which shapes trigger state transitions)
- The **projection logic** (how to update the derived state)

None of these require different structural processing behavior in the event pipeline. The 6-type vocabulary remains sufficient.

Classification: **resolved — no change to type vocabulary**.

Confidence: **HIGH**. `status_changed` would only be justified if the event pipeline needed to route differently for state transitions. Since state is projection-derived and transition rules are pattern-configured, the routing happens in configuration, not in the type dispatch.

**Escape hatch preserved**: The type vocabulary is append-only. If a future scenario reveals processing behavior that genuinely differs (not just domain meaning that differs), `status_changed` can still be added. ADR-4 S3 makes addition cheap.

---

## 5. Composition Model: How Patterns Provide State Machine Skeletons

With Q1 and Q2 resolved, the remaining questions (Q3–Q6) concern how workflows **compose**. This section sketches the model that Sessions 2 and 3 will stress-test.

### 5.1 Pattern-Provided State Machines

Each workflow pattern provides a **state machine skeleton**: named states, valid transitions, role assignments per transition, and auto-maintained projections.

**Example: `capture_with_review` pattern skeleton**:

```
pattern: capture_with_review
states: [pending_review, accepted, returned]
initial_trigger: capture event with pattern's primary shape
transitions:
  - from: pending_review
    to: accepted
    shape: review_decision (decision=accepted)
    actor_role: reviewer
  - from: pending_review
    to: returned
    shape: review_decision (decision=returned)
    actor_role: reviewer
  - from: returned
    to: pending_review
    shape: [primary_shape] (resubmission)
    actor_role: capturer
projections:
  - current_state
  - pending_since (timestamp of last state-changing event)
  - review_history (list of all review events)
```

**Example: `case_management` pattern skeleton**:

```
pattern: case_management
states: [opened, active, referred, resolved, closed, reopened]
initial_trigger: capture event with opening shape
transitions:
  - from: opened
    to: active
    shape: case_interaction
    actor_role: assigned_worker
    state_preserving: false  # first interaction = active
  - from: active
    to: active
    shape: case_interaction
    actor_role: assigned_worker
    state_preserving: true  # subsequent interactions don't change state
  - from: active
    to: referred
    shape: review_request
    actor_role: assigned_worker
  - from: [active, referred]
    to: resolved
    shape: case_resolution
    actor_role: assigned_worker
  - from: resolved
    to: closed
    shape: case_closure_review (decision=closed)
    actor_role: supervisor
  - from: [resolved, closed]
    to: reopened
    shape: case_reopening
    actor_role: assigned_worker
  - from: reopened
    to: active
    shape: case_interaction
    actor_role: assigned_worker
  - from: any
    to: (same state)
    shape: assignment_changed
    type: assignment_changed
    state_preserving: true
projections:
  - current_state
  - current_assignee
  - last_interaction_date
  - time_in_current_state
  - interaction_count
```

**Example: `multi_step_approval` pattern skeleton**:

```
pattern: multi_step_approval
parameters:
  levels: integer (min 2, max configurable, default 3)
states: [submitted, pending_level_N, approved_level_N, returned, final_approved, final_rejected]
initial_trigger: capture event with primary shape
transitions:
  - from: submitted
    to: pending_level_1
    implicit: true  # submission = pending level 1
  - from: pending_level_N
    to: approved_level_N (if N < max_level → pending_level_N+1)
    shape: approval_decision (decision=approved, level=N)
    actor_role: level_N_reviewer
  - from: pending_level_N
    to: returned
    shape: approval_decision (decision=returned, level=N)
    actor_role: level_N_reviewer
  - from: pending_level_N
    to: final_rejected
    shape: approval_decision (decision=rejected, level=N)
    actor_role: level_N_reviewer
  - from: returned
    to: submitted (restarts from level 1)
    shape: [primary_shape] (resubmission)
    actor_role: submitter
  - from: approved_level_max
    to: final_approved
    implicit: true  # last level approval = final
projections:
  - current_state
  - current_level
  - approval_chain (list of all decisions with level, actor, timestamp)
  - time_at_current_level
```

**Example: `transfer_with_acknowledgment` pattern skeleton**:

```
pattern: transfer_with_acknowledgment
states: [dispatched, in_transit, received, partially_received, disputed, resolved]
initial_trigger: capture event with dispatch shape
transitions:
  - from: dispatched
    to: in_transit
    implicit: true  # dispatch = in transit
  - from: in_transit
    to: received
    shape: receipt_confirmation (discrepancies=none)
    actor_role: receiver
  - from: in_transit
    to: partially_received
    shape: receipt_confirmation (discrepancies=partial)
    actor_role: receiver
  - from: in_transit
    to: disputed
    shape: discrepancy_report
    actor_role: receiver
  - from: [partially_received, disputed]
    to: resolved
    shape: discrepancy_review
    actor_role: supervisor
projections:
  - current_state
  - items_dispatched
  - items_received
  - discrepancy_summary
  - time_in_transit
```

### 5.2 Deployer Parameterization

The deployer doesn't author state machines — they select a pattern and fill in the blanks:

```
activity: malaria_case_tracking
pattern: case_management
shapes:
  opening: malaria_case_opening
  interaction: malaria_follow_up
  review_request: malaria_referral
  resolution: malaria_case_outcome
  closure_review: malaria_closure_review
  reopening: malaria_reopening
roles:
  assigned_worker: chv
  supervisor: clinic_supervisor
scope: geographic
deadlines:
  follow_up_interval: 7d
  overdue_threshold: 14d
  resolution_target: 90d
```

This is Layer 0 — assembling from platform-provided components. The deployer does not see the state machine. They see: "this is a case management activity where CHVs track malaria cases, supervisors review and close, and follow-ups are expected every 7 days."

### 5.3 What This Means for the Primitives Map

The primitives map after ADR-5 Session 1:

| Primitive | Invariant | Status | Change from previous |
|-----------|-----------|--------|---------------------|
| Event Store | Events immutable, IDs unique, sole write path | **Settled** (ADR-1) | Unchanged |
| Projection Engine | Current state = f(events), rebuildable | **Settled** — now includes state machine evaluation as a projection capability | **Expanded**: derives workflow state from events + pattern definition |
| Subject Identity | Four typed categories, merge=alias, split=freeze | **Settled** (ADR-2) | Unchanged |
| Conflict Detector | Accept-and-flag, detect-before-act, single-writer | **Settled** (ADR-2) | **Expanded**: new flag category `transition_violation` for invalid state transitions |
| Command Validator | No event written that violates lifecycle rules / authorization | **Clarified**: validates against projection + pattern on-device; flags violations but does not reject | **Role clarified**: validator warns, does not block (offline-first) |
| Assignment Resolver | Current responsibility always computable on-device | **Settled** (ADR-3) | **Expanded**: resolves per-level/per-stage assignments from pattern role declarations |
| Scope Resolver | Sync scope = access scope, server-computed | **Settled** (ADR-3) | Unchanged |
| Shape Registry | Defines payload schemas per event type, versioned | **Settled** (ADR-4) | Unchanged |
| Trigger Engine | L3a event-reaction (sync), L3b deadline-check (async), bounded | **Settled** (ADR-4) | Unchanged |
| Expression Evaluator | Operators + field references, zero functions, two contexts | **Settled** (ADR-4) | **Pending Q5**: may gain `context.*` scope |
| Pattern Registry | Defines workflow skeletons per pattern type, platform-fixed | **NEW** — platform provides; deployer selects and parameterizes | **Emerged from ADR-5 event storms** |

The **Pattern Registry** is the new primitive. It:
- Stores named workflow patterns with state machine skeletons
- Is platform-fixed (deployers select, don't create — same as event types)
- Provides state derivation rules that the projection engine executes
- Provides transition validity rules that the conflict detector extends
- Provides role-per-step declarations that the assignment resolver uses
- Is synced to devices as part of the configuration package

---

## 6. Open Questions for Session 2

### 6.1 Q3 — Composition Rules

How do activities that use different patterns interact? Specifically:
- Can a single subject participate in multiple patterns simultaneously? (A case that is both under case_management and has a pending multi_step_approval for a specific decision within it.)
- If yes, do the state machines compose (states from both patterns are tracked independently) or conflict (one pattern's state constrains the other)?
- Can patterns be nested? (A case_management case contains a transfer_with_acknowledgment step as one of its interactions.)

Directional lean: **independent composition** — each pattern tracks its own state independently per subject/activity combination. A subject can be "active" in case_management and "pending_level_2" in a multi_step_approval — these are separate state machines on the same subject, scoped by pattern + activity.

### 6.2 Q4 — Flag Cascade Behavior

When an upstream event is flagged after downstream workflow steps have fired, what happens?

Concrete scenario: CHV captures an observation (event A). Trigger fires, creating a task (event B). Supervisor completes the task (event C). Then event A is flagged as a conflict (identity merge, stale scope, whatever). Events B and C were causally downstream of A.

Options:
- **A: Flag propagation** — Events B and C automatically receive a "derived_from_flagged" flag. The flag cascade is visible. Human resolves the root cause (A), then explicitly resolves or acknowledges the downstream flags.
- **B: Source-only flagging** — Only event A is flagged. The projection shows "derived from flagged source" as a computed property. No new flags on B and C.
- **C: Selective propagation** — Only state-changing downstream events receive derived flags. State-preserving events (interactions, receipts) don't.

Directional lean: **B (source-only flagging)** — cleaner, avoids flag multiplication. The projection can always trace from any event back to its source chain and check for flags. Adding flags to downstream events creates N flags from one problem, overwhelming the flag queue. The detect-before-act rule (ADR-2 S12) already prevents triggers from firing on flagged events — so the cascade is bounded at the point where the flag is detected.

### 6.3 Q5 — `context.*` Expression Scope

The ADR-4 stress test surfaced this: should form expressions access pre-resolved contextual facts about the actor and the workflow state?

Example: A form includes a field that should default to the case's current state, or show/hide sections based on whether the case is "active" or "resolved." Without `context.*`, the form has no access to the subject's projected state.

Options:
- **A: Add `context.*` scope** — Form expressions can reference `context.subject_state`, `context.assigned_facility.type`, `context.activity.stage`. Pre-resolved at form-open time from the local projection. No dynamic queries.
- **B: No `context.*`** — Forms are payload-only. If the deployer needs state-aware logic, they add a read-only field that the device pre-populates from the projection before opening the form. This is a device capability, not an expression language feature.

Directional lean: **A (add `context.*`)** — it's a pre-resolved scope, not a dynamic query. The expression evaluator's architecture doesn't change. The scope is populated from the local projection at form-open time. It provides genuinely useful capability (state-aware form logic) without architectural cost. The key question is: what specific properties are available in `context.*`?

### 6.4 Q6 — Auto-Resolution and State Machine Integration

Can deployers configure rules for automatically resolving certain flag types? How does auto-resolution interact with state machines?

Example: A `transition_violation` flag on a `case_interaction` event (interaction submitted for a closed case) could be auto-resolved with policy: "if the case is reopened within 72 hours, resolve the flag as 'timing overlap — valid.' If not reopened within 72 hours, resolve as 'invalid interaction — needs supervisor review.'"

This touches the trigger architecture. Auto-resolution rules are evaluations that watch flag states over time — structurally similar to L3b deadline checks. They could be a sub-type of deadline policies.

Directional lean: **auto-resolution is a L3b sub-type** — same mechanism (watch for event, check deadline, create resolution event), different purpose (flag resolution instead of escalation). The deployer configures: "for flag type X, if condition Y is met within Z hours, auto-resolve with decision D."

---

## 7. Session 2 Charter

**Objective**: Stress-test the composition model. Specifically:

1. **Pattern composition rules** (Q3): Walk concrete multi-pattern scenarios. Can a case subject have an active case_management AND a pending approval? How does the projection present this?

2. **Flag cascade behavior** (Q4): Walk the "flagged upstream" scenario end-to-end. Validate that source-only flagging is sufficient. Adversarial test: what if the root-cause flag isn't resolved for weeks — does the rest of the workflow stall?

3. **`context.*` scope specification** (Q5): Define what properties are available. Walk a form-authoring scenario that uses `context.subject_state` and `context.activity.stage`. Validate that pre-resolution is feasible on-device.

4. **Auto-resolution specification** (Q6): Define the mechanism. Walk a "transition_violation auto-resolved by reopening" scenario. Validate that L3b mechanisms support this without new infrastructure.

5. **Irreversibility filter**: Apply to all Session 1 positions. Expected outcome: zero Tier 1 items (no envelope changes), possibly 1–2 Tier 2 items (strategy-protecting), remainder Tier 3 (strategies).

**Expected artifact**: `docs/exploration/20-adr5-session2-stress-test.md`

---

## 8. Summary of Session 1 Positions

| # | Question | Position | Confidence | Classification |
|---|----------|----------|------------|----------------|
| Q1 | State machine: primitive or pattern? | **Projection pattern** — state derived from events, transition violations flagged not rejected | HIGH | Initial strategy |
| Q2 | `status_changed` as 7th type? | **Not needed** — state-changing vs. state-preserving is a pattern/shape distinction, not a type distinction | HIGH | Resolved (no change) |
| Q3 | Multi-step workflow composition | **Pattern-provided state machine skeletons** — deployer selects pattern, parameterizes with shapes/roles/levels | HIGH (structure), MEDIUM (composition rules) | Initial strategy |
| Q4 | Flag cascade interaction | **Source-only flagging** (directional lean) — needs stress test | MEDIUM | Strategy |
| Q5 | `context.*` expression scope | **Add as pre-resolved scope** (directional lean) — needs property specification | MEDIUM | Strategy |
| Q6 | Auto-resolution | **L3b sub-type** (directional lean) — needs mechanism validation | MEDIUM | Strategy |

**New primitives emerged**: Pattern Registry (platform-fixed workflow skeletons), `transition_violation` flag category.

**Envelope status**: Unchanged at 11 fields. Type vocabulary unchanged at 6 types. All positions are strategies or strategy-protecting at most.

**Key finding**: The append-only, offline-first constraints from ADR-1 and ADR-2 settle Q1 and Q2 with high confidence. State machines cannot be enforcement primitives because enforcement means rejection, and rejection violates the core contract. This is the cleanest forcing function in the project — the constraint chain (V1 → P1/P3 → ADR-1 S1/S14 → state-as-projection) admits no alternative.
