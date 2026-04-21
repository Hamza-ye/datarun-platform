> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-005**. For a navigated reading guide, see [guide-adr-005.md](../guide-adr-005.md).
# ADR-005 Session 2: Stress Test, Composition Rules, and Irreversibility Filter

> Phase: 2 of 3 — Stress Test & Integration
> Date: 2026-04-13
> Input: Session 1 positions (19-adr5-session1-scoping.md), ADR-001 through ADR-004 (all committed), S04/S07/S08/S11/S14/S06
> Purpose: Stress-test the four MEDIUM-confidence positions from Session 1 (Q3 composition rules, Q4 flag cascade, Q5 context.* scope, Q6 auto-resolution). Apply irreversibility filter to all positions. Produce final classifications for ADR-005 writing.

---

## 1. Q3 — Pattern Composition Rules

### 1.1 The Question

Can a single subject participate in multiple patterns simultaneously? How do the state machines compose?

Session 1 lean: **independent composition** — each pattern tracks its own state per subject/activity combination.

### 1.2 Concrete Scenario Walk-throughs

**Scenario 1: Case with embedded approval**

A malaria case (case_management pattern) requires a drug regimen change that needs multi-level approval (multi_step_approval pattern). The subject is the patient/case. Two patterns operate on the same subject simultaneously.

Event sequence:

| # | Event | Type | Shape | Pattern affected | Case state | Approval state |
|---|-------|------|-------|-----------------|------------|----------------|
| 1 | Case opened | `capture` | `malaria_case_opening` | case_management | opened | — |
| 2 | First interaction | `capture` | `malaria_follow_up` | case_management | active | — |
| 3 | Drug change requested | `capture` | `drug_change_request` | multi_step_approval | active (unchanged) | submitted → pending_L1 |
| 4 | District officer approves | `review` | `approval_decision` (level=1, approved) | multi_step_approval | active (unchanged) | approved_L1 → pending_L2 |
| 5 | Regional manager approves | `review` | `approval_decision` (level=2, approved) | multi_step_approval | active (unchanged) | final_approved |
| 6 | Drug regimen applied | `capture` | `malaria_follow_up` | case_management | active (unchanged) | — (approval complete) |
| 7 | Case resolved | `capture` | `malaria_case_outcome` | case_management | resolved | — |

**Analysis**: The two state machines run independently. The case stays "active" throughout the approval process. The approval has its own state (pending_L1 → approved_L1 → final_approved). They don't interfere.

**Key question**: How does the projection distinguish which pattern an event belongs to?

Answer: **The shape determines the pattern.** The deployer's activity configuration maps shapes to patterns:

```
activity: malaria_case_tracking
patterns:
  - pattern: case_management
    shapes:
      opening: malaria_case_opening
      interaction: malaria_follow_up
      resolution: malaria_case_outcome
      ...
  - pattern: multi_step_approval
    shapes:
      submission: drug_change_request
      decision: approval_decision
    parameters:
      levels: 2
```

When an event arrives for this subject, the projection engine checks: "which pattern does this shape belong to?" and updates the corresponding state machine. A shape belongs to exactly one pattern within an activity. This is enforced at configuration time (deploy-time validation).

**Constraint discovered**: A shape can belong to only one pattern within a single activity. If two patterns within the same activity both claim the same shape, the deploy-time validator rejects the configuration. This is AP-6 prevention — no overlapping authority over the same event.

**Scenario 2: Distribution chain within a campaign**

A vaccination campaign (campaign pattern from S09) includes supply distribution (transfer_with_acknowledgment pattern from S07). The campaign has stages. Distribution is one stage.

But these are **different subjects**. The campaign tracks vaccination targets (subject = geographic area or individual). The distribution tracks supplies (subject = shipment). They are linked by activity_ref (both events carry `activity_ref: "measles_campaign_2026"`), not by shared subject.

**Finding**: Most cross-pattern scenarios involve **different subjects linked by activity**, not the same subject with multiple patterns. The malaria case + approval scenario is the exception, and it composes cleanly because shapes disambiguate.

**Scenario 3: Entity lifecycle with review**

A facility registry (entity_lifecycle pattern from S06) requires that facility updates be reviewed before taking effect (capture_with_review pattern from S04).

Event sequence:

| # | Event | Type | Shape | Pattern affected | Lifecycle state | Review state |
|---|-------|------|-------|-----------------|----------------|--------------|
| 1 | Facility registered | `capture` | `facility_registration` | entity_lifecycle | registered | — |
| 2 | Details updated | `capture` | `facility_update` | entity_lifecycle + capture_with_review | updated | pending_review |
| 3 | Update reviewed | `review` | `facility_update_review` | capture_with_review | updated (unchanged) | accepted |
| 4 | Facility verified | `capture` | `facility_verification` | entity_lifecycle | verified | — |

**Analysis**: Here `facility_update` triggers state transitions in TWO patterns — it advances the entity lifecycle (an update occurred) AND initiates a review cycle (the update needs approval).

**Does this break the "one shape, one pattern" constraint?**

No — because the patterns operate on different concerns:
- entity_lifecycle tracks the **subject's lifecycle** (registered → updated → verified → deprecated)
- capture_with_review tracks the **event's review status** (the specific update event is pending_review → accepted/returned)

These are not competing state machines over the same state space. The lifecycle is about the subject over time. The review is about a specific event's approval status. The projection can track both independently because they answer different questions:
- "What lifecycle phase is this facility in?" (entity_lifecycle)
- "Has the latest update been reviewed?" (capture_with_review)

**Refinement**: The composition model needs to distinguish between **subject-level state** (lifecycle, case status) and **event-level state** (review status of a specific event). The `review` type inherently provides event-level state (it references a source event). Case management, entity lifecycle, and multi-step approval provide subject-level state.

### 1.3 Composition Rules — Resolution

**Rule 1: One subject-level pattern per activity.** An activity binds at most one subject-level state machine to a subject. You don't have two competing lifecycle state machines on the same subject within the same activity. This is simple and prevents the hardest composition problem (conflicting state machines).

Example: `malaria_case_tracking` uses `case_management` as the subject-level pattern. It cannot also use `entity_lifecycle` on the same subject — those are different activities on different subjects (cases vs. facilities).

**Rule 2: Event-level patterns compose freely.** Review patterns (capture_with_review) track per-event states. Any event within any subject-level pattern can be reviewed. The review state machine operates on events, not subjects. There's no conflict because the state space is different.

**Rule 3: Subject-level patterns can embed approval sub-flows.** A subject under case_management can have one or more multi_step_approval sub-flows for specific decisions. Each sub-flow is scoped to a specific submission event (the drug_change_request), not to the subject's lifecycle. The approval state machine is effectively event-level (it tracks the approval status of a specific submission), even though it has subject-level implications.

**Implementation**: The projection engine maintains:
- One **subject-level state** per (subject, activity, pattern) tuple
- Multiple **event-level states** per (event_id, pattern) — review status, approval progress
- The subject-level state and event-level states are queried independently

**Rule 4: Cross-activity linking uses activity_ref, not shared patterns.** When two activities interact (campaign + distribution), the link is the `activity_ref` field in the event envelope or a subject-ref cross-reference in the payload. Patterns don't span activities.

**Rule 5: Shape-to-pattern mapping is unique within an activity.** Deploy-time validation ensures no two patterns within the same activity claim the same shape. This prevents AP-6 (overlapping authority).

### 1.4 Q3 Resolution

| Rule | Description | Enforcement |
|------|-------------|-------------|
| One subject-level pattern per activity | No competing lifecycle state machines on the same subject | Deploy-time validation |
| Event-level patterns compose freely | Review/approval states are per-event, not per-subject | Projection engine |
| Approval sub-flows embed within subject patterns | Multi_step_approval scoped to a specific submission event | Configuration (shape mapping) |
| Cross-activity linking via activity_ref | Patterns don't span activities | Structural (envelope field) |
| Shape-to-pattern unique within activity | No overlapping authority | Deploy-time validation |

Classification: **initial strategy** — composition rules are enforced in configuration validation and projection logic. Changing them requires code changes, not data migration. Events carry no pattern information — pattern assignment is configuration-driven.

Confidence: **HIGH**. The three walk-throughs (case+approval, campaign+distribution, lifecycle+review) covered the composition cases that the scenarios produce. The model handles all of them without conflict.

---

## 2. Q4 — Flag Cascade Behavior

### 2.1 The Question

When an upstream event is flagged after downstream workflow steps have already fired, what happens to the downstream events?

Session 1 lean: **Option B — source-only flagging**. Only the root-cause event is flagged. Downstream "contamination" is a computed projection property, not additional flags.

### 2.2 End-to-End Walk-through

**Setup**: S12-style trigger chain.

1. CHV captures a stock report (event A) — `capture`, shape `stock_report`, payload: `stock_level = 0`.
2. L3a trigger fires: creates a task for the supply coordinator (event B) — `task_created`, shape `investigation_task`.
3. Supply coordinator completes the investigation (event C) — `task_completed`, shape `investigation_task`.
4. **Two weeks later**: Event A is flagged. The CHV's scope assignment was stale — they were reassigned to a different area before capturing event A, but their device hadn't synced the reassignment. ADR-3 scope-containment violation detected. Flag: `scope_violation` on event A.

**Question**: What happens to events B and C?

**Option A analysis (flag propagation):**
- Event B receives a `derived_from_flagged` flag. Event C receives a `derived_from_flagged` flag.
- The flag queue now has 3 entries for one root cause.
- The resolver (supervisor) opens the queue. Sees 3 flags. Must investigate A, then decide about B, then decide about C. But B and C are causally determined by A — if A is valid, B and C are valid. If A is invalid, B and C are automatically questionable.
- The resolver resolves A ("accept — CHV was in the area anyway"). Then must also resolve B and C individually. But there's nothing to decide for B and C — they're consequences of A's resolution.
- **Result**: 3× the resolution work for 1× the decision. At scale (a busy coordinator with 200 flags), this is untenable.

**Option B analysis (source-only flagging):**
- Only event A is flagged. Events B and C carry no flags.
- The projection for the subject shows: "event A: flagged (scope_violation)." When rendering event B, the projection can compute: "this event was triggered by event A (via source_event_ref in B's payload), and event A is flagged."
- The UI can show: "⚠ This task was created from a flagged source event."
- The resolver opens the queue. Sees 1 flag for event A. Resolves it. Done. B and C's "derived from flagged" indicator disappears in the projection because A is no longer flagged.
- **Result**: 1 resolution action for 1 root cause. Downstream visibility is preserved through computed projection properties. No flag multiplication.

### 2.3 Adversarial Test: Unresolved Flag for Weeks

**Attack scenario**: Event A is flagged. The resolver doesn't act for 3 weeks. During those 3 weeks, the workflow continues:

- Event D: escalation_notice (L3b deadline trigger — investigation wasn't done)
- Event E: district manager creates a new task
- Event F: someone completes the new task

Under **source-only flagging**:
- Events D, E, F are created normally.
- But wait — **ADR-2 S12 (detect-before-act)**: "Events flagged by the Conflict Detector do not trigger policy execution until the flag is resolved."

This means event A's flag should have **prevented** the L3a trigger from firing in the first place. Event B should never have been created. The detect-before-act guarantee handles the cascade at the source.

**But what if the flag was raised AFTER event B was created?** This is the specific scenario described: event A was clean when the trigger fired (B was created), but was flagged later (when the scope reassignment synced).

In this case, event B exists and is valid at the time of creation. The flag on A is a retroactive discovery. Options:

1. **B is valid as-created.** The trigger fired correctly given the information available. The flag on A means the trigger-input was questionable, not that the trigger-output was malformed. B is a valid `task_created` event that a supply coordinator acted on. Flagging B retroactively serves no purpose — the investigation either happened or it didn't.

2. **B is questionable.** The investigation was triggered by potentially invalid data. The coordinator's work (C) may have been unnecessary.

**Resolution**: Both are correct descriptions of reality. The question is what the platform should DO about it. And the answer is: **nothing automatic**. The flag on A surfaces the root cause. The projection shows B's lineage to A. A human decides whether B's outcome (the completed investigation) needs revisiting. No automatic cascade.

This is exactly P5 (Conflict is surfaced, not silently resolved). The platform's job is to make the provenance chain visible, not to automatically propagate consequences. Automatic propagation would create a flag explosion that overwhelms the resolution queue and adds no decision value — every downstream flag resolves to "see root cause."

### 2.4 Edge Case: Double-Retroactive Flagging

**Attack**: Event A is clean. Trigger creates B. B is clean. B's deadline trigger creates D (escalation). Then A is flagged. Now B, C (if completed), and D all descend from a flagged source.

Under source-only flagging: only A is flagged. The projection for the subject shows a lineage chain where the root has a flag. Each downstream event's rendered view can include a notice: "upstream source flagged." But flags don't multiply.

**Does this create a silent problem?** Only if someone looking at event D (the escalation notice) has no way to know that its root cause (A) is flagged. The projection must be able to trace: D → B → A, and show that A carries a flag.

**Requirement for projections**: The projection engine must support **source-chain traversal** — given an event, walk its `source_event_ref` chain back to the originating event, and surface any flags on that chain. This is a projection capability, not a new flag type.

### 2.5 Q4 Resolution

**Position: Source-only flagging.** Confirmed at HIGH confidence.

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| Flag scope | Root-cause event only | One decision per root cause. No flag multiplication. |
| Downstream visibility | Computed projection property via source-chain traversal | Projection traces `source_event_ref` chain, surfaces upstream flags |
| Retroactive flagging | No automatic cascade. Flag on A doesn't create flags on B, C, D. | Events B/C/D were valid when created. Human decides if they need revisiting. |
| Interaction with S12 | Detect-before-act prevents trigger firing on flagged events *at the time of detection* | Retroactive flags (discovered after trigger already fired) don't retroactively invalidate trigger outputs |
| Resolution effect | Resolving root flag clears the "upstream flagged" indicator on all downstream projections | One resolution action fully resolves the chain |

**New projection capability**: Source-chain traversal with flag surfacing. Not a new primitive — an extension of the projection engine's existing event-linking capability.

Classification: **initial strategy** — flag behavior is server-side and projection logic. Changing it requires code changes, not data migration. Events carry their own flags independently; the cascade decision affects how projections render, not what's stored.

Confidence: **HIGH**. The adversarial tests (3-week delay, double-retroactive, escalation chain) all resolve cleanly. Flag multiplication (Option A) fails the practical test at scale. Source-only flagging (Option B) handles every scenario with O(1) resolution work per root cause.

---

## 3. Q5 — `context.*` Expression Scope

### 3.1 The Question

Should form-level expressions access pre-resolved contextual facts about the actor and the workflow state?

Session 1 lean: **Add `context.*` as a pre-resolved scope**.

### 3.2 Form-Authoring Scenario Walk-through

**Scenario**: A deployer configures a malaria case follow-up form. The form needs to:

1. **Show/hide a "reopening reason" section** — visible only when the case is in "resolved" or "closed" state (because the follow-up is effectively a reopening).
2. **Default the "facility" field** — to the actor's currently assigned facility.
3. **Show a warning** — if this is the first interaction in more than 14 days.

**Without `context.*`**: The form has no access to the subject's current state or the actor's assignment. The deployer must either:
- Add a hidden field that the device pre-populates from the projection before opening the form (device-specific implementation detail leaked into configuration)
- Use a trigger to reject/flag forms that don't match the expected state (after the fact, not during capture)
- Give up on the feature

**With `context.*`**: The form expressions reference pre-resolved values:

```yaml
shape: malaria_follow_up
fields:
  - name: reopening_reason
    type: text
    visible_when: "context.subject_state in ['resolved', 'closed']"
    required_when: "context.subject_state in ['resolved', 'closed']"

  - name: facility
    type: text
    default: "context.actor.facility_name"

  - name: days_since_last_interaction
    type: number
    computed: true
    read_only: true

rules:
  - when: "context.days_since_last_interaction > 14"
    warn: "It has been more than 14 days since the last interaction"
```

### 3.3 What Properties Should Be Available?

The scope must be **bounded** — an open-ended "access anything about the subject" scope would be AP-1 (inner platform effect). The properties must be **pre-resolvable** on-device from the local projection, not requiring dynamic queries.

**Proposed `context.*` properties:**

| Property | Source | Pre-resolvable? | Why useful |
|----------|--------|-----------------|-----------|
| `context.subject_state` | Subject-level pattern state from projection | ✅ Local projection | State-aware form sections |
| `context.subject_pattern` | Which pattern the subject participates in | ✅ Configuration | Pattern-aware forms |
| `context.activity_stage` | Current stage of the activity (for campaigns) | ✅ Configuration + projection | Stage-aware forms |
| `context.actor.role` | Current actor's role | ✅ Assignment resolver | Role-aware form logic |
| `context.actor.scope_name` | Current actor's assigned scope (e.g., facility name, district name) | ✅ Assignment resolver | Pre-fill location fields |
| `context.days_since_last_event` | Days since last event on this subject | ✅ Local projection (timestamp diff) | Overdue warnings |
| `context.event_count` | Total events for this subject | ✅ Local projection (count) | Conditional logic for first vs. subsequent interactions |

**What is NOT in `context.*`:**
- Other subjects' states (that's a cross-subject query — forbidden in form expressions)
- Aggregate values across multiple subjects (that's a reporting concern, not a form concern)
- Arbitrary projection fields (that would make the form coupled to projection internals)
- Payload fields from other events (that's a cross-event query — use `source_event_ref` in shapes instead)

### 3.4 Feasibility on Low-End Devices

All proposed properties are **derivable from data already on the device**:

- `subject_state` — computed from the subject's event stream (already projected locally)
- `actor.role`, `actor.scope_name` — available from the assignment resolver (already on-device per ADR-3)
- `days_since_last_event` — timestamp of the last event in the local projection vs. current time
- `event_count` — count of events in the local subject projection

No new data needs to sync. No new computation model needed. The device resolves these values when opening the form, not during expression evaluation. The expression evaluator sees them as static values — same as `payload.*` references.

### 3.5 AP-1 Check

Is `context.*` a step toward the inner platform effect?

**Test**: Does this scope add conditional logic, loops, functions, or dynamic queries? No. It adds **7 pre-resolved read-only values** to the expression evaluator's namespace. The execution model doesn't change — the evaluator still evaluates `field_a > field_b` type expressions. `context.subject_state` is just another value in scope, like `payload.temperature`.

**Test**: Can the scope grow unboundedly? Not if the property list is **platform-fixed**. Deployers reference `context.subject_state` — they cannot define `context.my_custom_field`. The list is a closed vocabulary, like event types.

**Guard**: `context.*` properties are platform-defined, closed, append-only — same governance as event types. Adding a new context property is a platform change, not a configuration change. This prevents deployer-driven scope creep.

### 3.6 Q5 Resolution

**Position: Add `context.*` as a pre-resolved, platform-fixed scope.**

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| Scope | 7 initial properties (subject_state, subject_pattern, activity_stage, actor.role, actor.scope_name, days_since_last_event, event_count) | All pre-resolvable from local data. All useful in form logic. |
| Governance | Platform-fixed, closed vocabulary. Adding properties is a platform change. | Prevents AP-1. Same governance as event types. |
| Resolution timing | Pre-resolved when form opens, before expression evaluation. Static during form fill. | No dynamic queries. No mid-form updates. |
| Architecture impact | Expression evaluator gains one more data scope. No structural change. | `context.*` is identical to `payload.*` from the evaluator's perspective — just a different value source. |
| Offline behavior | All properties derived from local data. No connectivity needed. | Fully offline-compatible. |

**Interaction with ADR-4's expression language**: ADR-4 defined two expression contexts (form context, trigger context). `context.*` is available in **form context only**. Trigger expressions evaluate on the server at event-processing time — they don't have a "form open" moment. Triggers access event payload fields directly.

Classification: **initial strategy** — the property list is evolvable (adding a property is a code change, not a data migration). The expression evaluator's architecture doesn't change. Events don't carry context values — they're computed at form-open time.

Confidence: **HIGH** (post-walk-through). The form-authoring scenario demonstrates clear utility. The AP-1 guard (platform-fixed vocabulary) is effective. Pre-resolution feasibility is confirmed for low-end devices.

---

## 4. Q6 — Auto-Resolution and State Machine Integration

### 4.1 The Question

Can deployers configure rules for automatically resolving certain flag types? How does auto-resolution interact with state machines?

Session 1 lean: **Auto-resolution is a L3b sub-type.**

### 4.2 Scenario Walk-through: Transition Violation Auto-Resolution

**Setup**: A case_management activity. The case is in "resolved" state. A CHV was offline and submitted a `case_interaction` event (they didn't know the case was resolved). The event is accepted (ADR-2 S14) and flagged with `transition_violation` ("interaction submitted for resolved case").

**Option 1 — Manual resolution only**: A supervisor reviews the flag, sees the interaction, and decides: "the CHV visited the patient and recorded useful observations; the case should be reopened." Supervisor creates a `case_reopening` event. The flag is resolved as "timing overlap — valid, case reopened."

**Option 2 — Auto-resolution**: The deployer configures a policy:

```yaml
auto_resolution:
  flag_type: transition_violation
  condition:
    transition: "case_interaction into resolved"
  watch_for:
    event_shape: case_reopening
    on_same_subject: true
    within: 72h
  resolution:
    if_watched_event_arrives:
      decision: "timing_overlap_valid"
      note: "Case reopened within 72h of violation — interaction valid"
    if_deadline_expires:
      decision: "needs_manual_review"
      escalate_to: supervisor
```

**Analysis**: This auto-resolution policy is structurally identical to a L3b deadline check:

| L3b deadline check | Auto-resolution |
|-------------------|-----------------|
| Watches for: response event after trigger event | Watches for: resolution-enabling event after flag |
| Deadline: time window | Deadline: time window |
| If response arrives: cancel deadline | If enabling event arrives: auto-resolve flag |
| If deadline expires: escalate | If deadline expires: escalate or mark for manual review |
| Server-side, asynchronous | Server-side, asynchronous |
| Creates an event (escalation) | Creates an event (flag resolution) |

The mechanisms are the same. The difference is the input (a flag vs. a trigger output) and the output (a resolution event vs. an escalation event).

### 4.3 Adversarial Test: Auto-Resolution Loops

**Attack**: Can auto-resolution create a loop?

Setup: Flag A is auto-resolved, creating resolution event R. Could event R trigger a new flag, which triggers a new auto-resolution, which creates a new event, etc.?

**Analysis**: No, because of two existing guards:

1. **Detect-before-act (ADR-2 S12)**: The auto-resolution event (a `ConflictResolved` type, which is likely a system-authored `capture` event with a resolution shape) is processed through the normal pipeline. If it triggers any flags itself, those flags are detected before any further policies fire.

2. **Trigger depth limit (ADR-4)**: DAG max path 2. The auto-resolution is depth 1 (flag → resolution event). If the resolution event triggers another policy, that's depth 2 (maximum). A third level is blocked.

3. **Auto-resolution watches flags, not events**: The auto-resolution policy fires when a specific flag type exists AND a watched event arrives. Resolution events are not the type of events that auto-resolution policies watch for (they watch for domain events like `case_reopening`, not for system events like flag resolution). The input space and output space don't overlap.

**Result**: No loop. The existing guards (detect-before-act, depth-2 DAG, input/output type separation) prevent recursive auto-resolution by construction.

### 4.4 Adversarial Test: Auto-Resolution Overreach

**Attack**: A deployer configures auto-resolution for a high-severity flag (e.g., `scope_violation` — the actor was outside their authorized area). Should the platform allow this?

**Analysis**: This is a policy question, not an architectural one. Some flag types are suitable for auto-resolution (transition violations due to timing overlaps). Others are not (scope violations that indicate unauthorized access).

**Guard**: Flag types should carry a **resolvability classification**:

| Classification | Meaning | Auto-resolution? |
|----------------|---------|------------------|
| `auto_eligible` | Low-severity, likely caused by offline timing. Auto-resolution safe. | Deployer can configure auto-resolution policies |
| `manual_only` | Medium/high-severity. Requires human judgment. | Auto-resolution policies rejected at deploy-time |

The classification is **platform-defined, per flag type** — consistent with the pattern of platform-fixed vocabularies. The initial classification:

| Flag type | Classification | Rationale |
|-----------|---------------|-----------|
| `transition_violation` | `auto_eligible` | Usually timing overlap between offline actors. Low severity. |
| `stale_reference` | `auto_eligible` | Entity was updated; event used old version. Self-correcting in most cases. |
| `scope_violation` | `manual_only` | Potential unauthorized access. Must be reviewed. |
| `identity_conflict` | `manual_only` | Potential duplicate subjects. Merge decision requires human judgment. |
| `concurrent_state_change` | `manual_only` | Two actors changed the same subject concurrently. Resolution requires domain judgment. |

This classification is a platform-level policy, not deployer-configurable. A deployer cannot make a `scope_violation` auto-resolvable. This prevents auto-resolution from silently dismissing security-relevant flags.

### 4.5 Q6 Resolution

**Position: Auto-resolution as L3b sub-type with resolvability classification.**

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| Mechanism | L3b deadline check sub-type. Same infrastructure: watch for event, check deadline, create event. | Reuses existing trigger architecture. No new primitives. |
| Scope | Auto-eligible flag types only. Platform classifies each flag type. | Prevents auto-resolution of security-relevant flags. |
| Configuration | Deployer configures: flag type + condition + watched event + deadline + resolution decision | Same deployer-facing model as deadline triggers. |
| Output | Creates a `ConflictResolved` event with the auto-resolution decision | Standard event. Auditable. Reversible (a human can override). |
| Loop prevention | Detect-before-act + depth-2 DAG + input/output type separation | Three independent guards. No new mechanism needed. |
| Initial auto_eligible types | `transition_violation`, `stale_reference` | Low-severity, timing-related flags. |
| Escape hatch | Deployers can set auto-resolution policies to `disabled` per deployment | Opt-out is trivial. |

Classification: **initial strategy** — auto-resolution policies are server-side logic. The flag classification is platform-defined but can evolve (promoting a flag type from manual_only to auto_eligible is a code change, not a data migration). Events carry flags the same way regardless of how they're resolved.

Confidence: **HIGH** (post-adversarial test). The mechanism reuses L3b infrastructure. The loop-prevention guards exist by construction. The resolvability classification prevents overreach.

---

## 5. Irreversibility Filter

Applying the three-tier filter to all ADR-5 positions.

### 5.1 Classification

**Test**: "If we commit to this and later discover it's wrong, can we change it without migrating stored events or breaking deployed devices?"

| # | Position | Stored state? | Contract surface? | Wrong-choice recovery? | Classification |
|---|----------|--------------|-------------------|----------------------|----------------|
| Q1 | State machines as projection patterns | No — events carry no state field. State is derived in projection logic. | Projection engine + pattern registry (2 components). | Code change to projection engine. No data migration. | **Initial strategy** |
| Q2 | No `status_changed` type | No change to stored events. | N/A — no change. | If needed later, append to type vocabulary (cheap, ADR-4 S3). | **Resolved — no action** |
| Q3 | Composition rules (one subject-level pattern per activity, event-level compose freely) | No — composition is configuration + projection logic. | Configuration validator + projection engine (2 components). | Code change to validator and projection. No data migration. | **Initial strategy** |
| Q4 | Source-only flagging | No — events carry their own flags. Cascade behavior is projection rendering. | Projection engine only. | Code change to projection rendering. No data migration. | **Initial strategy** |
| Q5 | `context.*` scope (7 platform-fixed properties) | No — context values are computed at form-open time. Not stored in events. | Expression evaluator + form renderer (2 components). | Code change to add/remove properties. No data migration. | **Initial strategy** |
| Q6 | Auto-resolution as L3b sub-type | No — auto-resolution produces standard ConflictResolved events. The mechanism is server-side logic. | Trigger engine (1 component). | Code change to trigger engine. No data migration. | **Initial strategy** |
| New | Pattern Registry as primitive | No — pattern definitions are configuration, synced to devices. Not stored in events. | Projection engine + configuration pipeline (2 components). | Adding/modifying patterns is a platform code change. No data migration. | **Initial strategy** |
| New | `transition_violation` flag category | Partly — flag events are stored. Adding a new flag category doesn't change existing flags. | Conflict detector + projection engine (2 components). | Adding flag categories is additive. Removing is never needed (old flags persist). | **Strategy-protecting** |
| New | Flag resolvability classification | No — classification is platform logic, not stored state. | Auto-resolution engine (1 component). | Code change to reclassify. No data migration. | **Initial strategy** |

### 5.2 Irreversibility Summary

**Tier 1 (envelope-touching): NONE.**

No ADR-5 position touches the event envelope. The envelope remains at 11 fields. The type vocabulary remains at 6. This confirms the prediction from Session 1: ADR-5 has a zero irreversibility surface for stored data.

**Tier 2 (strategy-protecting): ONE.**

`transition_violation` as a new flag category. Flag events are stored, so adding a new category that generates flags is a strategy-protecting decision — the flag type string appears in stored `ConflictDetected` events. However:
- Adding flag categories is always safe (new events, no change to existing)
- The flag type string is in the payload, not the envelope
- Removing a flag category just means no new flags of that type are created; existing flags persist unaffected

This is the mildest possible Tier 2 item.

**Tier 3 (strategies): ALL OTHERS.**

Every other position is projection logic, configuration semantics, or server-side processing — all evolvable without data migration.

### 5.3 Stress Test Scope

Given zero Tier 1 items and one mild Tier 2 item, **Session 3 does not need a full adversarial stress test**. The appropriate Session 3 scope:

1. **Light validation** of the Tier 2 item (transition_violation flag category): confirm the flag type string is stable and the category is well-defined.
2. **Structural coherence audit**: confirm that all Session 1 + Session 2 positions compose with each other and with ADR-1 through ADR-4.
3. **ADR writing**: With zero irreversible and one mild strategy-protecting position, Session 3 can proceed directly to writing ADR-005.

---

## 6. Cross-Cutting Observations

### 6.1 The Pattern Inventory — Scope Decision

Session 1 identified four workflow-bearing patterns. Session 2 validated their composition. The question from the checkpoint: is the pattern inventory ADR-5 scope or post-ADR?

**Answer**: The pattern **architecture** is ADR-5 scope (pattern registry as primitive, composition rules, state-as-projection). The pattern **inventory** (which specific patterns ship initially and their exact state machine skeletons) is post-ADR documentation. The reasoning:

1. The ADR decides the model — how patterns work, how they compose, how they interact with flags and expressions. This is the architectural decision.
2. The specific patterns (capture_with_review, case_management, multi_step_approval, transfer_with_acknowledgment) are **initial strategies** — the set will grow. Freezing the exact skeleton definitions in the ADR conflates architecture with implementation inventory.
3. The pattern skeletons from Session 1 (§5.1) serve as existence proofs — they demonstrate the model works. They should be referenced in the ADR as examples, not committed as specifications.

The consolidated pattern registry (with formal schemas, parameterization points, projection definitions) belongs in the platform specification document — the same artifact that consolidates all primitives from ADR-1 through ADR-5.

### 6.2 Command Validator Revisited

Session 1 clarified the Command Validator: it validates against projection + pattern on-device, but **flags violations rather than rejecting events**. Session 2 confirms this:

- On-device: the Command Validator can warn ("this case is resolved — are you sure you want to add an interaction?"). The CHV can override the warning. The event is written regardless.
- On-sync: the server validates the same transition rules. If the on-device override was appropriate (case was reopened concurrently), no flag. If not, `transition_violation` flag is created.
- The Command Validator is an **advisory validator on-device** and a **flag generator on-server**. It never blocks event creation.

This is consistent with ADR-2 S14 ("events never rejected for staleness") and V1 (offline-first). The validator improves the user experience (fewer unnecessary flags) without compromising the append-only contract.

### 6.3 Principles Confirmed

| Principle | How ADR-5 confirms it |
|-----------|-----------------------|
| P1 (Offline is the default) | State is projection-derived from local data. context.* is pre-resolved locally. No workflow step requires connectivity. |
| P3 (Records are append-only) | Invalid transitions are flagged, not rejected. Events always stored. State never written — always derived. |
| P4 (Patterns compose) | Composition rules proven across 3 multi-pattern scenarios. Subject-level + event-level state machines compose independently. |
| P5 (Conflict is surfaced) | Transition violations surfaced as flags. Source-only flagging with projection-based lineage tracing. Auto-resolution explicit and auditable. |
| P7 (Simplest scenario stays simple) | S00 uses `capture_only` pattern — no state machine, no workflow, no flags. Zero overhead for the simple case. |

---

## 7. Session 2 Summary

| # | Question | Session 1 Position | Session 2 Result | Final Confidence | Classification |
|---|----------|-------------------|------------------|------------------|----------------|
| Q1 | State machine: primitive or pattern? | Projection pattern | **Confirmed** — no change | HIGH | Initial strategy |
| Q2 | `status_changed` as 7th type? | Not needed | **Confirmed** — no change | HIGH | Resolved (no change) |
| Q3 | Composition rules | Independent composition | **5 rules defined**: one subject-level per activity, event-level compose freely, approval sub-flows embed, cross-activity via activity_ref, shape-to-pattern unique | HIGH | Initial strategy |
| Q4 | Flag cascade | Source-only flagging | **Confirmed** + source-chain traversal as projection capability | HIGH | Initial strategy |
| Q5 | `context.*` scope | Add as pre-resolved scope | **7 properties defined**, platform-fixed vocabulary, form context only | HIGH | Initial strategy |
| Q6 | Auto-resolution | L3b sub-type | **Confirmed** + resolvability classification (auto_eligible vs. manual_only), initial flag type classifications | HIGH | Initial strategy |

**All six questions now at HIGH confidence.**

**Irreversibility surface: zero Tier 1, one mild Tier 2.** ADR-5 is the least irreversible of all five ADRs. This confirms the trend observed from ADR-3 onward: each subsequent ADR touches fewer stored-data surfaces.

### New artifacts from Session 2

| Artifact | What | Where it lives |
|----------|------|---------------|
| 5 composition rules | How patterns compose within and across activities | ADR-005 (initial strategy section) |
| Source-chain traversal | Projection capability for flag lineage | Projection engine specification |
| 7 `context.*` properties | Pre-resolved form context values | ADR-005 (initial strategy section) |
| Flag resolvability classification | auto_eligible vs. manual_only per flag type | ADR-005 (strategy-protecting section) |
| Initial flag classifications | transition_violation→auto, stale_reference→auto, scope/identity/concurrent→manual | ADR-005 (initial strategy section) |

---

## 8. Session 3 Charter

With all six questions at HIGH confidence and zero irreversibility surface, Session 3's scope is narrowed:

1. **Structural coherence audit**: Verify all ADR-5 positions compose with ADR-1 through ADR-4. Check for contradictions, gaps, or overlooked interactions.
2. **Write ADR-005**: All positions are stable. The ADR can be written.

**Expected artifacts**:
- `docs/exploration/21-adr5-session3-coherence-and-adr.md` (short — coherence checks only)
- `docs/adrs/adr-005-state-progression.md` (the ADR)
