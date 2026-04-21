> **⚠️ SUPERSEDED** — This is a raw exploration document archived for reference. Decisions were finalized in **ADR-005**. For a navigated reading guide, see [guide-adr-005.md](../guide-adr-005.md).
# ADR-005 Session 3, Part 1: Structural Coherence Audit

> Phase: 3.1 of 3 — Structural Coherence
> Date: 2026-04-13
> Input: Session 1 positions (19-adr5-session1-scoping.md), Session 2 stress tests (20-adr5-session2-stress-test.md), ADR-001 through ADR-004 (all committed)
> Purpose: Verify that ADR-5's nine positions compose with all four prior ADRs and with each other. If they don't compose, the fix is revision of the position — not proceeding to ADR writing. This audit is the gate.

---

## Method

ADR-4's coherence audit checked whether five scenario solutions composed into one system. ADR-5's coherence audit has a different structure: six stable positions (Q1–Q6, all HIGH confidence) must integrate cleanly with the committed architecture from ADR-1 through ADR-4.

Nine checks, organized in three groups:

**Group A — Does ADR-5 integrate with each prior ADR?**

| Check | Question |
|-------|----------|
| (a) ADR-1 integration | Does state-as-projection compose with the append-only event model? |
| (b) ADR-2 integration | Do transition_violation flags and source-only cascading compose with accept-and-flag and detect-before-act? |
| (c) ADR-3 integration | Does the pattern registry compose with assignment-based access and sync-scope-as-authorization? |
| (d) ADR-4 integration | Do patterns, context.*, and auto-resolution fit cleanly into the four-layer gradient, the 6-type vocabulary, and the expression language? |

**Group B — Do ADR-5's positions compose with each other?**

| Check | Question |
|-------|----------|
| (e) Internal coherence | Do the six positions (Q1–Q6) form a consistent model, or do any two positions contradict? |
| (f) Primitives composition | Do the new/expanded primitives (Pattern Registry, expanded Projection Engine, expanded Conflict Detector, context.* scope) interact cleanly? |

**Group C — Cross-cutting integrity**

| Check | Question |
|-------|----------|
| (g) Envelope integrity | Confirm zero envelope changes. Validate that no position secretly requires a new field. |
| (h) Anti-pattern check | Does any ADR-5 position create a path toward AP-1 through AP-6? |
| (i) Principle alignment | Does each principle hold after ADR-5? |

Each check produces a verdict: **composes**, **composes-with-clarification**, or **does-not-compose**.

---

## Check (a): ADR-1 Integration — Append-Only Event Model

### The question

ADR-1 commits: immutable events, append-only, client-generated UUIDs, event as sync unit, projections always rebuildable. ADR-5 says state is projection-derived and transition violations are flagged, not rejected.

Does projection-derived state create any tension with the event model?

### Analysis

**Tension candidate 1 — Projection rebuild cost for workflow state**

ADR-1 guarantees: projections are rebuildable from events. ADR-5 adds workflow state as a projection concern. The projection engine must now walk a subject's full event history, applying pattern-defined state machine rules, to compute current state.

For S04 (3-state, few events per subject): trivial. For S08 (6+ states, 50–100 events over months): the projection engine replays 50–100 events per subject to derive state. With 10,000 active cases, a full rebuild traverses 500K–1M events.

Is this a problem? **No — it's the same problem ADR-1 already anticipated.** Session 1 of the project identified "projection complexity on low-end Android" as a known risk with mitigation: the B→C escape hatch (add application-maintained views alongside the event store). Workflow state projection is one more computation in the same pipeline, not a qualitatively different concern. The scaling spike (100–500 events per subject) is the right test, but it tests the projection engine generically — workflow state doesn't change the architectural decision.

**Tension candidate 2 — State never stored in events**

ADR-5 position Q1 says state is derived, never stored. This means no event payload contains a `current_state` field that declares the subject's state at event time. Is there a scenario where NOT having the state in the event causes a problem?

Test: A supervisor reviews a case. The review form shows the case's current state (e.g., "active"). The supervisor accepts. The review event's payload contains `decision: accepted`, `source_event_ref: ...`. It does NOT contain `case_state_at_review_time: active`. If someone later replays events and the state machine definition has changed (a new state was added), the derived state at review time might differ from what the supervisor actually saw.

This is a **configuration versioning** concern, not a state storage concern. The supervisor saw the state derived under config version N. If config version N+1 changes the state machine, projection replay under N+1 might compute a different state sequence. But ADR-4 S6 (atomic configuration delivery) ensures that the state machine definition changes atomically, and shape versioning ensures event payloads are interpreted against their declared shape version. The state derivation is deterministic given the config version. If the config changes, the projection changes — that's the intended behavior (updated rules, updated projections).

**Would storing state in events help?** It would create a snapshot — "at event time, the state was X." But it would also create a consistency risk: what if the stored state disagrees with the derived state? Which is authoritative? The event-sourcing principle (ADR-1) says derived state is authoritative. Storing state in events would create a second source of truth. This violates the single-source-of-truth strategy from ADR-1.

**Tension candidate 3 — Event ordering for state derivation**

State derivation depends on causal ordering (device_seq + sync_watermark from ADR-2). If two events arrive out of order, the state derivation depends on the order in which the projection processes them. This is not new — ADR-2 established that causal ordering resolves this, and accept-and-flag handles cases where ordering reveals conflicts. Workflow state derivation uses the same ordering, no special handling needed.

### Verdict

**Check (a): COMPOSES.**

State-as-projection is a natural extension of ADR-1's event-sourcing model. Projection rebuild cost is a known, mitigated risk (B→C escape hatch). State is never redundantly stored in events. Event ordering for state derivation uses ADR-2's established causal ordering. No tension.

---

## Check (b): ADR-2 Integration — Accept-and-Flag, Detect-Before-Act

### The question

ADR-2 commits: accept-and-flag (events never rejected for staleness), detect-before-act (flagged events don't trigger policies), single-writer conflict resolution, merge=alias. ADR-5 adds `transition_violation` as a flag category and establishes source-only cascading for flag chains.

### Analysis

**Tension candidate 1 — transition_violation is a new flag source**

ADR-2 defines the flagging mechanism. The flag categories in ADR-2 and ADR-3 are: identity conflict, stale reference, scope violation, concurrent state change. ADR-5 adds `transition_violation` — an event represents a state transition that the pattern's state machine says is invalid.

Does the conflict detector need to change? Yes, but narrowly. The conflict detector currently evaluates: "does this event create an identity conflict? a scope violation? a concurrent modification?" Adding "does this event violate the pattern's transition rules?" is one more evaluation in the same pipeline. The input is the same (the incoming event + the current projection state). The output is the same (a flag on the event). The mechanism is identical.

**Integration test**: A CHV captures a `case_interaction` for a closed case (offline, didn't know it was closed).

1. Event arrives at server. Conflict detector evaluates.
2. Identity checks: clean (subject exists, actor valid). No identity flag.
3. Scope check: clean (actor is assigned to this subject's scope). No scope flag.
4. Transition check: current state = closed. Pattern says `case_interaction` is valid from `[opened, active, referred, reopened]` but not from `closed`. **Flag: `transition_violation`**.
5. Event is stored (ADR-1, append-only). Flag is attached. Detect-before-act applies: this event does not trigger L3 policies.

This flows cleanly through the existing pipeline. The transition check is one more step, not a structural change to the pipeline.

**Tension candidate 2 — Detect-before-act and workflow state**

ADR-2 S12: "Events flagged by the Conflict Detector do not trigger policy execution until the flag is resolved." ADR-5's transition violations are flags. So:

- A `case_interaction` event flagged with `transition_violation` does not trigger L3a triggers.
- If the pattern says "notify supervisor on every interaction," the notification does NOT fire for this flagged interaction.
- This is **correct behavior** — the interaction may be invalid (the case is closed). Generating a supervisor notification for a potentially invalid interaction would amplify the problem.

But what about the **state machine itself**? When the projection derives state, does it include flagged events or exclude them?

**Option A — Exclude flagged events from state derivation**: The case remains "closed" because the flagged `case_interaction` doesn't count as a valid interaction. The supervisor resolves the flag. If accepted, the projection re-derives including the event: case → "active" (reopened via the accepted interaction).

**Option B — Include flagged events in state derivation**: The case moves to "active" because the interaction event exists (it was accepted by the event store). The flag marks it as questionable, but the projection includes it.

**Resolution**: Option A is correct because it's consistent with detect-before-act. The principle is: flagged events have uncertain validity. The projection should reflect the **validated** state, not the tentative state. If the interaction is accepted (flag resolved), the projection updates. If rejected (the interaction is marked as invalid), the state stays "closed."

But "excluding from state derivation" doesn't mean the event is invisible. The projection shows: "case is closed. 1 pending interaction awaiting flag resolution." The event is visible in the timeline. It doesn't affect current state until the flag is resolved.

**This is consistent with detect-before-act**: just as flagged events don't trigger policies, flagged events don't change derived state. The principle extends naturally.

**Formal statement**: Flagged events are excluded from state machine evaluation in the projection engine. They are visible in the timeline but do not affect `current_state`. When a flag is resolved (accepted), the projection re-derives state including the event. When a flag is resolved (rejected — marked as invalid), the projection remains unchanged.

**Tension candidate 3 — Source-only flagging and ADR-2's flag model**

Session 2 (Q4) established source-only flagging: only the root-cause event is flagged, downstream contamination is a computed projection property via source-chain traversal. Does this compose with ADR-2?

ADR-2 creates flags on individual events. Source-only flagging says: don't create additional flags on downstream events. This is a restriction on flag CREATION, not a modification to existing flag behavior. ADR-2's flag model is: "when the conflict detector finds a problem, it flags that event." Source-only flagging says: "when reporting downstream contamination, use the projection to show the lineage, don't create new flags." These are compatible — ADR-2 says nothing about propagating flags to related events; source-only flagging confirms that flag propagation is not needed.

**Tension candidate 4 — Flag resolution events and ordering**

When a flag is resolved (supervisor accepts or rejects the flagged event), a resolution event is created. This resolution event goes through the normal pipeline: conflict detection, projection update, trigger evaluation. The resolution event changes the flag's status, which changes whether the originally-flagged event participates in state derivation (per tension candidate 2 above).

Does this create an ordering problem? The resolution event has its own device_seq/sync_watermark. It's ordered after the flagged event and after the flag-creation event. The projection replays in order: (1) original event arrives, (2) flag created, (3) event excluded from state, (4) resolution event arrives, (5) projection re-derives with or without the event. This is deterministic and well-ordered. No tension.

### Verdict

**Check (b): COMPOSES — with one clarification.**

`transition_violation` integrates cleanly as one more evaluation step in the conflict detector. Source-only flagging is compatible with ADR-2's flag model (restriction on flag creation, not modification). Flag resolution and re-derivation follow deterministic ordering.

**Clarification**: Flagged events are excluded from state machine evaluation until the flag is resolved. This extends detect-before-act from "flagged events don't trigger policies" to "flagged events don't change derived state." The extension is natural — both say "don't act on uncertain data."

---

## Check (c): ADR-3 Integration — Assignment-Based Access, Sync Scope

### The question

ADR-3 commits: assignment-based access control (scope-containment test), sync scope = access scope (devices get only authorized data), authority-as-projection. ADR-5 adds the Pattern Registry as a new primitive with per-step role declarations that the Assignment Resolver uses.

### Analysis

**Tension candidate 1 — Pattern roles and ADR-3 assignment model**

Patterns declare roles per step: "reviewer at level N is role X." The Assignment Resolver uses ADR-3's assignment model (actor + role + scope) to determine who holds that role. Does the pattern introduce a new kind of assignment?

No. The pattern references existing roles. The deployer maps pattern roles to deployment-specific role names: `reviewer: clinic_supervisor`. The Assignment Resolver computes: "who is assigned as `clinic_supervisor` in the scope containing this subject?" This is exactly ADR-3's scope-containment test — no new mechanism.

**Multi-level approval adds one wrinkle**: different levels have different reviewer roles, and those roles may exist at different scope levels (district officer at district scope, regional manager at regional scope). The Assignment Resolver must find the actor with role X at the scope level that CONTAINS the subject's scope. This is hierarchical scope resolution — already part of ADR-3's model (3–6 organizational hierarchy levels).

Test: Subject is in Facility A (geographic scope). Level 1 reviewer is `district_officer` — resolved at district scope containing Facility A. Level 2 reviewer is `regional_manager` — resolved at regional scope containing the district. ADR-3's scope-containment test walks up the hierarchy to find the assigned actor. The pattern just tells it which role to look for at each level. **Composes.**

**Tension candidate 2 — Sync scope for workflow events**

A case_management subject accumulates events from multiple actors (CHV, nurse, supervisor). All events share the same subject_ref. ADR-3 says: sync scope = access scope. An actor sees events for subjects in their scope. If a case is transferred (assignment_changed), the new assignee's scope must include the subject. The old assignee may lose visibility (selective-retain on scope contraction, ADR-3 S10).

Does the transfer event itself need special sync handling? No — the `assignment_changed` event is a normal event on the subject. It syncs to anyone whose scope includes the subject. The Assignment Resolver processes it and updates the projection: new assignee = Actor B. ADR-3's scope resolver then computes sync scope for Actor B's next sync, including this subject.

**Tension candidate 3 — Pattern configuration in the config package**

The Pattern Registry is a new primitive. Its content (pattern definitions including state machine skeletons) must be synced to devices as part of the configuration package. ADR-3's sync model distinguishes between event data (synced per scope) and configuration (synced to all devices).

Pattern definitions are configuration — they're the same for all devices in a deployment. They sync as part of the atomic configuration package (ADR-4 S6). No scope-based filtering needed. **Composes.**

### Verdict

**Check (c): COMPOSES.**

Pattern roles use ADR-3's existing assignment model — role + scope-containment. Multi-level approval uses hierarchical scope resolution, already in ADR-3. Pattern definitions are deployment-wide configuration, synced atomically. No new sync mechanism needed.

---

## Check (d): ADR-4 Integration — Four-Layer Gradient, Type Vocabulary, Expression Language

This is the largest check. ADR-4 has the most surface area to integrate with.

### The question

ADR-4 commits: 6 structural event types, typed shapes via shape_ref, four-layer gradient (L0–L3), one expression language with two contexts (form, trigger), server-only triggers (L3a/L3b), complexity budgets, deploy-time validation. ADR-5 adds patterns, context.* scope, and auto-resolution. Do they fit?

### Analysis

**4.1 — Where do patterns live in the gradient?**

Patterns are platform-provided state machine skeletons that deployers select and parameterize. In the gradient:

- **Pattern definitions** (the skeletons themselves) are **platform-fixed, below L0**. They're like event types — platform vocabulary that deployers reference but don't create. Adding a pattern is a platform evolution, not a deployer action.
- **Pattern selection and parameterization** (choosing `case_management`, mapping shapes to roles) is **L0 — Assembly**. The deployer assembles an activity from platform components. ADR-4's L0 description says: "assembling from platform-provided components." Patterns fit exactly.
- **Pattern-aware deadlines** (follow_up_interval, overdue_threshold) are **L3b parameters** — configuring existing deadline-check infrastructure with pattern-specific timing.
- **Pattern-aware form logic** (context.subject_state) is **L2 expressions** using the context.* scope.

The gradient absorbs patterns without any layer revision. Each aspect of a pattern maps to an existing layer:

| Pattern aspect | Gradient layer | Mechanism |
|----------------|---------------|-----------|
| Skeleton definition | Below L0 (platform-fixed) | Pattern Registry primitive |
| Activity binding | L0 | Activity definition |
| Shape-to-role mapping | L0 | Activity parameterization |
| Transition validation | Server-side conflict detection | Conflict Detector (expanded) |
| State-aware form logic | L2 | context.* scope in expressions |
| Workflow deadlines | L3b | Deadline-check triggers |
| Auto-resolution policies | L3b | Auto-resolution sub-type |

No new layer. No layer boundary shifted. **Clean.**

**4.2 — Type vocabulary: does ADR-5 need a 7th type?**

Session 1 resolved Q2: `status_changed` is NOT needed. The 6-type vocabulary is sufficient because state transitions are expressed through shapes + patterns, not through platform processing behavior. Session 2 confirmed this at HIGH confidence. The type vocabulary is unchanged.

Verification: walk each ADR-5 workflow event through the 6 types.

| Workflow event | Type | Why not a new type |
|----------------|------|-------------------|
| Case opened | `capture` | Data recording — standard pipeline |
| Case interaction | `capture` | Data recording — standard pipeline |
| Case review | `review` | Source-linking + review-status projection — standard |
| Case transfer | `assignment_changed` | Scope/role modification — sync recomputation — standard |
| Approval decision | `review` | Source-linking + level-tracking in payload — standard |
| Dispatch/receipt | `capture` | Data recording — standard |
| Discrepancy report | `capture` | Data recording — standard |
| Auto-resolution event | System event — `capture` with resolution shape | System-authored, uses system actor identity. No new processing behavior — goes through standard pipeline. |
| Transition violation flag | Not an event type — created by conflict detector | Part of the flag pipeline, not the event type vocabulary |

Every workflow construct maps to existing types. **No 7th type needed.**

**4.3 — Expression language: does context.* break "one language, two contexts"?**

ADR-4 established: one expression language, two evaluation contexts (form context with `payload.*` + `entity.*`, trigger context with `payload.*` only). ADR-5 adds `context.*` as a third data scope in the form context.

This does NOT create a third evaluation context. It adds data to the existing form context:

| Property | Before ADR-5 | After ADR-5 |
|----------|-------------|-------------|
| Form context data scopes | `payload.*`, `entity.*` | `payload.*`, `entity.*`, `context.*` |
| Trigger context data scopes | `payload.*` | `payload.*` (unchanged) |
| Expression grammar | Unchanged | Unchanged |
| Operators | Unchanged | Unchanged |
| Output types | Unchanged | Unchanged |

The expression evaluator gains one more data scope in the form context namespace. From the evaluator's perspective, `context.subject_state` is syntactically identical to `entity.facility_name` — a read-only pre-resolved value. The evaluator doesn't care where the value came from; it sees a named value in its namespace.

**Key constraint preserved**: `context.*` is form-context only. Trigger expressions do NOT access `context.*`. This is correct because triggers evaluate on the server at event-processing time — there is no "form open" moment, no user-facing context. Triggers work with the event payload as submitted.

**Does context.* in form expressions mean forms now depend on projection state?**

Yes, indirectly. The form's behavior (which fields are visible, what defaults appear) depends on `context.subject_state`, which comes from the local projection. But this dependency is:
- **Read-only**: the form reads the projection, never writes to it
- **Pre-resolved**: values are captured once at form-open time, static during form fill
- **One-directional**: projection → form, never form → projection

The form doesn't "query" the projection during expression evaluation. The device resolves context values before the expression evaluator runs — the evaluator sees static values. This preserves the expression language's simplicity: no dynamic lookups, no mid-evaluation queries.

**The 7-property vocabulary**: Session 2 defined 7 initial context properties (`subject_state`, `subject_pattern`, `activity_stage`, `actor.role`, `actor.scope_name`, `days_since_last_event`, `event_count`). These are platform-fixed, closed, append-only — same governance as event types. A deployer cannot define `context.my_field`. Adding a property is a platform change.

This governance model mirrors ADR-4 S3 (type vocabulary): platform-fixed, closed, append-only, each addition is a code change. The consistency is deliberate and prevents AP-1 (inner platform effect — deployer-extensible expression scopes would be a step toward a query language).

**4.4 — Auto-resolution as L3b sub-type: does it fit the trigger architecture?**

ADR-4's trigger architecture:
- L3a: event-reaction. Server-only. DAG max path 2. 5 triggers per type per shape, 50 per deployment.
- L3b: deadline-check. Server-only. Watches for response event within time window. Escalates on timeout.
- Non-recursive (AP-5). Output type ≠ input type. One event output per trigger execution.

Auto-resolution is a sub-type of L3b:
- Watches for a resolution-enabling event (e.g., `case_reopening`) after a flag of a specific type
- On deadline: escalates (same as standard L3b)
- On watched event: creates a resolution event (same pattern as L3b cancellation, but with an explicit output)

Does auto-resolution fit within the trigger limits?

**Budget test**: 5 triggers per type per shape. Auto-resolution policies don't target shapes — they target flag types. Are auto-resolution policies counted in the trigger budget?

They should count in the L3b budget (50 per deployment total), but they use a different targeting mechanism (flag type + condition, not event type + shape). The budget can accommodate them with a shared deployment-wide limit: "total L3b policies (deadline + auto-resolution) ≤ 50 per deployment."

**DAG depth test**: Auto-resolution fires when a flag exists AND a watched event arrives. The resolution event is depth 1. Can the resolution event trigger another L3a trigger? Yes, up to depth 2 (DAG max path 2). Could that L3a trigger's output be auto-resolved? No — auto-resolution watches for flags, and L3a outputs don't inherently have flags (they'd only be flagged if the conflict detector finds a problem, which is a different flow).

Session 2 already validated loop prevention through three independent guards:
1. Detect-before-act (auto-resolution events don't trigger policies if flagged)
2. Depth-2 DAG (at most 2 levels of trigger chaining)
3. Input/output separation (auto-resolution watches flags, not trigger outputs)

**The trigger architecture absorbs auto-resolution without structural change.**

**4.5 — Deploy-time validation: what new validations does ADR-5 require?**

ADR-4 established deploy-time validation as the single enforcement point for configuration consistency. ADR-5 adds configuration concepts that need validation:

| Validation | What it checks | When it fails |
|------------|---------------|---------------|
| Shape-to-pattern uniqueness | No two patterns within the same activity claim the same shape | Two patterns list the same shape |
| One subject-level pattern per activity | An activity binds at most one subject-level state machine | Activity lists two subject-level patterns (e.g., case_management + entity_lifecycle) |
| Pattern role mapping completeness | Every role declared in the pattern has a mapping in the activity | Activity uses case_management but doesn't map `supervisor` role |
| Auto-resolution flag type eligibility | Auto-resolution policies can only target `auto_eligible` flag types | Policy targets `scope_violation` (manual_only) |
| Auto-resolution within L3b budget | Total L3b policies (deadline + auto-resolution) ≤ 50 | Budget exceeded |

These are all **deploy-time validations** — consistent with ADR-4's model. No runtime validation added. The deploy-time validator gains five new checks, but the enforcement model is unchanged.

### Verdict

**Check (d): COMPOSES.**

Patterns fit cleanly into the gradient (platform-fixed below L0, parameterized at L0, extended at L2/L3b). The type vocabulary is unchanged at 6. The expression language gains one data scope in the form context — no grammar change, no structural change. Auto-resolution fits as a L3b sub-type within existing trigger limits. Deploy-time validation gains five new checks, model unchanged.

---

## Check (e): Internal Coherence — Do Q1–Q6 Compose?

### The question

Do the six ADR-5 positions form a consistent model? Could any two positions contradict?

### Pairwise interaction matrix

| | Q1 (state-as-projection) | Q2 (no 7th type) | Q3 (composition) | Q4 (source-only flags) | Q5 (context.*) | Q6 (auto-resolution) |
|---|---|---|---|---|---|---|
| **Q1** | — | Q2 follows from Q1 | Q3 depends on Q1 | Q4 uses Q1's model | Q5 reads Q1's output | Q6 resolves Q1's flags |
| **Q2** | | — | Independent | Independent | Independent | Independent |
| **Q3** | | | — | Q4 applies per-pattern | Q5.subject_state depends on Q3 | Auto-resolution per-pattern |
| **Q4** | | | | — | Independent | Q6 resolves only root flag |
| **Q5** | | | | | — | Independent |
| **Q6** | | | | | | — |

### Interaction analysis

**Q1 → Q2**: Q2 (no `status_changed` type) follows directly from Q1 (state-as-projection). If state is derived, there's no distinct processing behavior for state transitions, so no new type is needed. These are **reinforcing, not conflicting**.

**Q1 → Q3**: Composition rules (Q3) specify HOW projections handle multi-pattern subjects. Rule 1 (one subject-level pattern per activity) ensures the projection engine computes exactly one `current_state` per (subject, activity) tuple. Without this constraint, the projection would need to merge competing state machines — which Q1's model doesn't address. **Q3 is a necessary complement to Q1.**

**Q1 → Q4**: Source-only flagging (Q4) works BECAUSE state is projection-derived (Q1). If state were stored in events, a flagged event would contain a state value that might be wrong, creating a more complex problem. With derived state, the projection simply excludes flagged events from state derivation (per Check (b) clarification). **Q4 is enabled by Q1.**

**Q1 → Q5**: `context.subject_state` reads the projection output from Q1's state machine evaluation. The value comes from the projection engine after it processes the pattern's state machine. This is a **consumer relationship** — Q5 consumes Q1's output. No circular dependency (Q5 doesn't feed back into state derivation).

**Q1 → Q6**: Auto-resolution (Q6) resolves `transition_violation` flags, which are generated by Q1's state-as-projection model. When a flag is resolved, the projection re-derives state (per Check (b)). Auto-resolution is a **lifecycle companion** to Q1's flagging — it handles the flags Q1 produces.

**Q3 → Q5**: `context.subject_state` reflects the state from the subject-level pattern in the current activity. Q3 says one subject-level pattern per activity. So `context.subject_state` is unambiguous — there's exactly one state machine providing it. If Q3 allowed multiple subject-level patterns, `context.subject_state` would be ambiguous (which pattern's state?). **Q3 prevents an ambiguity in Q5.**

**Q4 → Q6**: Auto-resolution creates a resolution event for a root-cause flag. Under source-only flagging, this resolves the single flag. No downstream flags to chase. **Q4 simplifies Q6** — auto-resolution only needs to handle one flag per root cause.

### Verdict

**Check (e): COMPOSES.**

All six positions form a coherent directed graph. Q1 (state-as-projection) is the foundation. Q2 follows from it. Q3 bounds it. Q4, Q5, Q6 each interact with Q1 in well-defined, non-conflicting ways. No contradictions. No circular dependencies.

---

## Check (f): Primitives Composition — New and Expanded Primitives

### The question

ADR-5 introduces one new primitive (Pattern Registry) and expands three existing ones (Projection Engine, Conflict Detector, Expression Evaluator). Do they interact cleanly?

### Primitive interaction map

```
┌─────────────────────────────────────────────────────────────┐
│                 PRIMITIVE INTERACTION MAP                     │
│                                                             │
│  Pattern Registry ──────► Projection Engine                  │
│    (provides state          (evaluates state machines,      │
│     machine definitions)     derives current_state)         │
│                                                             │
│  Pattern Registry ──────► Conflict Detector                  │
│    (provides transition      (evaluates transition_violation │
│     validity rules)          flags)                          │
│                                                             │
│  Pattern Registry ──────► Assignment Resolver                │
│    (provides role-per-step   (resolves which actor holds     │
│     declarations)            each pattern role)              │
│                                                             │
│  Pattern Registry ──────► Deploy-Time Validator              │
│    (provides composition     (validates configuration        │
│     rules & constraints)     against pattern rules)          │
│                                                             │
│  Projection Engine ─────► Expression Evaluator               │
│    (provides context.*       (pre-resolves context values    │
│     values at form-open)     for form expressions)           │
│                                                             │
│  Conflict Detector ─────► Projection Engine                  │
│    (flag status determines   (flagged events excluded from   │
│     state derivation)        state machine evaluation)       │
│                                                             │
│  Trigger Engine (L3b) ──► Conflict Detector                  │
│    (auto-resolution          (creates resolution events      │
│     policies)                that resolve flags)             │
└─────────────────────────────────────────────────────────────┘
```

### Cycle check

Is there a cycle in the primitive interaction graph?

- Pattern Registry → Projection Engine → Expression Evaluator: no return edge. Expression evaluator consumes context values, doesn't feed back into projections or pattern definitions.
- Pattern Registry → Conflict Detector → Projection Engine: the Conflict Detector flags events, the Projection Engine excludes flagged events from state derivation. The Projection Engine doesn't feed back into the Conflict Detector. One-directional chain.
- Trigger Engine (L3b auto-resolution) → Conflict Detector: auto-resolution creates resolution events. These events go through the normal pipeline (Conflict Detector evaluates them). But auto-resolution watches FLAGS, not trigger outputs. The pipeline is: flag exists → auto-resolution fires → resolution event → pipeline → Conflict Detector evaluates the resolution event (likely clean, no flag) → done. No cycle because the resolution event is a normal event processed once, not a recursive trigger for more auto-resolution.

**No cycles.** All interactions are acyclic. The Pattern Registry is a read-only data source — nothing feeds back into it at runtime.

### Resource contention

Do expanded primitives create resource contention on low-end devices?

The device runs four engines (from ADR-4 coherence audit): form engine, validation engine, event store, projection engine. ADR-5 adds:

| New capability | Which engine | Device impact |
|----------------|-------------|---------------|
| State machine evaluation | Projection Engine | One more computation per subject projection rebuild. Cost: O(events × transitions) per subject. |
| context.* pre-resolution | Projection Engine + Form Engine | One read from projection cache at form-open. Cost: negligible (7 cached values). |
| Transition warning | Validation Engine (advisory) | One lookup per event submission: "is this shape valid in current state?" Cost: O(1) state machine lookup. |

No new engine on device. No new data to sync (pattern definitions are part of the existing config package). The projection engine does more work per rebuild, but incrementally (state machine evaluation is a linear pass over events, piggybacks on the same replay the projection already does).

The conflict detector expansion (transition_violation evaluation) is **server-only** — the device validation engine only advises ("this might violate transition rules"), it doesn't generate flags. Flag generation happens at event ingestion on the server. No device-side conflict detection change.

### Verdict

**Check (f): COMPOSES.**

New and expanded primitives interact through well-defined, acyclic relationships. The Pattern Registry is a read-only data source consumed by four other primitives. No primitive cycles. No new device-side engines. Device impact is incremental additions to existing engines.

---

## Check (g): Envelope Integrity — Zero Changes Confirmed

### The question

Session 1 predicted and Session 2 confirmed: ADR-5 has no envelope-touching positions. Verify this rigorously — could any position secretly require a new field?

### Field-by-field verification

For each ADR-5 position, test: "does this require information to be carried IN the event envelope that isn't already there?"

| Position | Envelope needs | Verdict |
|----------|---------------|---------|
| Q1: State-as-projection | State is derived, not stored. No state field in envelope. | ✅ No envelope change |
| Q2: No 7th type | Type vocabulary unchanged. | ✅ No envelope change |
| Q3: Composition rules | Pattern assignment is configuration, not per-event. No `pattern_ref` in envelope — the shape determines the pattern (via config). | ✅ No envelope change |
| Q4: Source-only flagging | Flags are attached to events by the conflict detector (server-side metadata). Source-chain traversal uses `source_event_ref` IN THE PAYLOAD (already present for review and trigger events). | ✅ No envelope change |
| Q5: context.* | Context values are pre-resolved at form-open time. Not stored in events. | ✅ No envelope change |
| Q6: Auto-resolution | Resolution events are standard events (capture with resolution shape). System actor identity uses existing `actor_ref` format. | ✅ No envelope change |

### The pattern_ref question

Could a `pattern_ref` field be useful? It would declare which pattern a specific event belongs to, similar to how `shape_ref` declares which shape.

**Why it's NOT needed**: The pattern is determined by the shape-to-pattern mapping in the activity configuration. Given `shape_ref` + `activity_ref`, the system can compute which pattern the event belongs to. Adding `pattern_ref` to the envelope would be redundant — derivable from existing fields plus configuration.

**Why redundancy would be harmful**: If `pattern_ref` in the envelope disagrees with the shape-to-pattern mapping in configuration (e.g., because the configuration was updated), which is authoritative? The configuration or the envelope? This is the same consistency risk identified in Check (a) for stored state — don't store derived values in events.

### Verdict

**Check (g): COMPOSES.**

Zero envelope changes confirmed. All ADR-5 positions derive their behavior from configuration, projection logic, and server-side processing — none require per-event metadata beyond the existing 11 fields.

The envelope remains at 11 fields across 5 ADRs. This is the strongest possible validation of the envelope design: a complete workflow and state progression system — the most behaviorally complex ADR yet — adds zero fields to the event record.

---

## Check (h): Anti-Pattern Check — AP-1 Through AP-6

### The question

Does any ADR-5 position create a path toward any of the six anti-patterns?

### AP-1: Inner Platform Effect (config becomes a programming language)

**Risk vector**: context.* scope could grow unboundedly, giving form expressions increasingly powerful query capabilities, eventually approaching a query language.

**Guard**: context.* properties are platform-fixed, closed, append-only. 7 initial properties. A deployer cannot define custom context properties. Adding a property is a platform code change. This is the same governance model as event types (AP-1-proof by ADR-4 S3's precedent).

**Risk vector**: Pattern state machines could become increasingly complex — deployers might demand custom states, conditional transitions, compound conditions on transitions.

**Guard**: Patterns are platform-fixed. Deployers select and parameterize, they don't author state machines. A deployer who needs a state machine the platform doesn't offer requests a platform evolution — or uses the code boundary (L3→code). The pattern inventory is a closed vocabulary with the same governance as event types.

**Verdict: AP-1 contained.** Both risk vectors are blocked by the platform-fixed vocabulary pattern.

### AP-2: Greenspun's Tenth Rule (config reimplements half a language)

**Risk vector**: Auto-resolution policies combine conditions (flag_type + transition + watched event + deadline). Could this become an ad-hoc rule language?

**Guard**: Auto-resolution conditions are structural: flag_type (from a closed vocabulary), transition details (from the pattern definition), watched event shape (from the shape registry), deadline (a duration value). No expressions, no boolean logic, no operators. The deployer declares what to watch for and what to do — they don't write rules.

**Verdict: AP-2 contained.** Auto-resolution is a parameterized configuration structure, not a rule language.

### AP-3: Configuration Specialist Trap (expertise changes form)

**Risk vector**: Pattern selection + shape mapping + role binding + deadline configuration + auto-resolution policies = a potentially complex configuration surface.

**Assessment**: This is a real concern. ADR-5 adds configuration complexity. However:
- Pattern selection is a dropdown (closed list).
- Shape-to-role mapping is a table (pattern role → deployment role).
- Deadlines are duration values.
- Auto-resolution policies are rare (only for auto_eligible flag types).

The deployer's cognitive load for a simple scenario (S00) is unchanged — zero patterns (capture_only is implicit), one shape, one activity. For S08 (case management), the deployer selects a pattern, maps shapes to roles, and sets deadlines. The complexity scales with the scenario, not with the platform.

**Guard**: ADR-4's S00 test (P7 validation) holds — the simplest scenario still requires 2 configuration artifacts. ADR-5 adds zero artifacts to S00.

**Verdict: AP-3 monitored, not triggered.** The configuration surface grows with scenario complexity (appropriate) rather than with platform complexity (would be AP-3). The S00 floor is preserved.

### AP-4: Schema Evolution Trap (schemaless data quality collapse)

**Risk vector**: Pattern state machine changes (adding a state, removing a transition) could orphan projections or create inconsistent derived state.

**Guard**: Pattern definitions are platform-fixed — they change through platform evolution, not deployer configuration. Platform developers handle migration (recompute projections, handle new/removed states). Since projections are rebuildable (ADR-1), adding a state to a pattern is: update definition → recompute projections. No orphaned data.

**Verdict: AP-4 contained.** Pattern evolution is platform-managed, projection-safe.

### AP-5: Trigger Escalation Trap (cascading triggers)

**Risk vector**: Auto-resolution (L3b sub-type) creates resolution events. Could these events trigger L3a triggers, which create more events, which get flagged, which trigger more auto-resolution?

**Guard**: Three independent guards (validated in Session 2):
1. DAG max path 2 — at most 2 levels of event chaining
2. Detect-before-act — flagged events don't trigger policies
3. Input/output separation — auto-resolution watches flags, not trigger outputs

Even in the worst case (auto-resolution event → L3a trigger → new event → flagged → auto-resolution eligible), the depth-2 limit stops the chain at step 2. The third step (new auto-resolution) would require depth 3 — blocked.

**Verdict: AP-5 contained.** Existing guards from ADR-4 prevent cascade. ADR-5 doesn't weaken them.

### AP-6: Overlapping Authority Trap (multiple mechanisms for same concern)

**Risk vector**: State machine transitions are defined in patterns (platform-fixed) AND validated by the conflict detector (transition_violation flags). Are these two mechanisms for the same concern?

**Analysis**: No — they serve different functions:
- Pattern definition declares what transitions are VALID (a truth table).
- Conflict detector evaluates whether a specific event VIOLATES those rules (an enforcement mechanism).

This is the same split as shapes (declare field types) + validation engine (enforce type constraints). One defines, one enforces. Not overlapping — complementary.

**Risk vector 2**: Auto-resolution and manual flag resolution — are these two mechanisms for the same concern?

**Analysis**: Yes, intentionally. But they're not overlapping — they're mutually exclusive per flag instance. A flag is auto-resolved only if an auto-resolution policy exists AND the conditions are met. Otherwise, it's manual. A single flag is never both auto-resolved and manually resolved. The deployer chooses which flag types get auto-resolution. No conflict.

**Verdict: AP-6 contained.** No overlapping authority. Definition/enforcement split is complementary, not duplicative. Auto-resolution and manual resolution are mutually exclusive per flag instance.

---

## Check (i): Principle Alignment

### The question

Do all seven principles hold after ADR-5?

| Principle | Pre-ADR-5 status | ADR-5 test | Post-ADR-5 status |
|-----------|------------------|------------|-------------------|
| P1: Offline is the default | Confirmed (ADR-1,2) | State derived from local projection. context.* pre-resolved locally. Advisory validation on-device. No workflow step requires connectivity. | **Confirmed** |
| P2: Configuration has boundaries | Confirmed (ADR-4) | Patterns are platform-fixed (deployers don't author state machines). context.* is platform-fixed (deployers don't define properties). Auto-resolution targets platform-classified flag types only. | **Confirmed** |
| P3: Records are append-only | Confirmed (ADR-1,2) | Invalid transitions are flagged, not rejected. Events always stored. Derived state is never written to event payloads. | **Confirmed** |
| P4: Patterns compose | Confirmed (ADR-1,2,3) | 5 composition rules proven across 3 multi-pattern scenarios. Subject-level + event-level state machines compose independently. | **Confirmed — strengthened** |
| P5: Conflict is surfaced | Confirmed (ADR-1,2) | transition_violation is a new flag category. Source-only flagging with source-chain traversal. Auto-resolution is explicit and auditable (produces events). | **Confirmed — extended** |
| P6: Authority is contextual/auditable | Confirmed (ADR-3) | Pattern role declarations are explicit. Auto-resolution events carry system actor identity (`system:auto_resolution/{policy_id}`). Every resolution is traceable. | **Confirmed** |
| P7: Simplest scenario stays simple | Confirmed (ADR-1,2,4) | S00 uses `capture_only` — no state machine, no workflow, no flags, no composition. Zero overhead. S00 still requires exactly 2 configuration artifacts. | **Confirmed** |

### Verdict

**Check (i): COMPOSES.**

All seven principles hold. P4 and P5 are strengthened by ADR-5 (composition rules formalized, flag model extended).

---

## Overall Verdict

| Check | Verdict | Key Finding |
|-------|---------|-------------|
| (a) ADR-1 integration | **Composes** | State-as-projection is a natural extension of the event-sourcing model. No stored-state tension. |
| (b) ADR-2 integration | **Composes — one clarification** | Flagged events excluded from state machine evaluation (extends detect-before-act). |
| (c) ADR-3 integration | **Composes** | Pattern roles use existing assignment model. Pattern definitions are deployment-wide config. |
| (d) ADR-4 integration | **Composes** | Patterns fit gradient (below L0 + L0 parameterization). Expression language gains scope, not grammar. Auto-resolution fits L3b. Type vocabulary unchanged. |
| (e) Internal coherence | **Composes** | All six positions form an acyclic dependency graph. Q1 is foundation; Q2–Q6 are reinforcing. |
| (f) Primitives composition | **Composes** | New/expanded primitives interact acyclically. No new device engines. Incremental additions only. |
| (g) Envelope integrity | **Composes** | Zero envelope changes. 11 fields across 5 ADRs. Strongest possible envelope stability validation. |
| (h) Anti-pattern check | **All contained** | AP-1/AP-2 blocked by platform-fixed vocabularies. AP-3 monitored. AP-4/AP-5/AP-6 structurally prevented. |
| (i) Principle alignment | **All confirmed** | P4 and P5 strengthened. P7 floor preserved. |

**All ADR-5 positions compose with all four prior ADRs and with each other.** No structural revision needed. No position requires modification.

---

## Clarifications to Carry into ADR-005

Two items discovered during the coherence audit that should be stated explicitly in the ADR:

### Clarification 1: Flagged events and state derivation

**Statement**: Flagged events are excluded from state machine evaluation in the projection engine. They are visible in the event timeline but do not change `current_state`. When a flag is resolved as accepted, the projection re-derives state including the event. This extends detect-before-act from "flagged events don't trigger policies" to "flagged events don't change derived state."

This is a natural extension of ADR-2 S12, not a new constraint. It belongs in the ADR as a stated consequence, not as a separate decision.

### Clarification 2: Auto-resolution system actor format

**Statement**: Auto-resolution events use actor_ref format `system:auto_resolution/{policy_id}`. This extends ADR-4's system actor convention (`system:{source_type}/{source_id}`), adding `auto_resolution` as a source_type alongside `trigger`.

---

## Primitives Map — Final State After ADR-5

| Primitive | Invariant | Status | ADR source |
|-----------|-----------|--------|-----------|
| Event Store | Events immutable, IDs unique, sole write path | **Settled** | ADR-1 |
| Projection Engine | Current state = f(events), rebuildable. Evaluates pattern state machines. Derives workflow state. Excludes flagged events from state evaluation. Source-chain traversal for flag lineage. | **Settled** | ADR-1 + ADR-5 |
| Subject Identity | Four typed categories, merge=alias, split=freeze | **Settled** | ADR-2 |
| Conflict Detector | Accept-and-flag, detect-before-act, single-writer. Evaluates transition_violation. 5 flag categories (identity_conflict, stale_reference, scope_violation, concurrent_state_change, transition_violation). | **Settled** | ADR-2 + ADR-5 |
| Command Validator | Advisory on-device (warns on transition violations, doesn't block). Flag-generator on-server. | **Settled** | ADR-1 + ADR-3 + ADR-5 |
| Assignment Resolver | Current responsibility computable on-device. Resolves per-level/per-stage assignments from pattern role declarations. | **Settled** | ADR-3 + ADR-5 |
| Scope Resolver | Sync scope = access scope, server-computed | **Settled** | ADR-3 |
| Shape Registry | Typed, versioned shapes via shape_ref. Deployer-authored, deploy-time validated. | **Settled** | ADR-4 |
| Trigger Engine | L3a event-reaction (server, depth-2 DAG), L3b deadline-check (server, async). L3b includes auto-resolution sub-type. | **Settled** | ADR-4 + ADR-5 |
| Expression Evaluator | Operators + field references, zero functions. Form context: payload.* + entity.* + context.* (7 properties). Trigger context: payload.* only. | **Settled** | ADR-4 + ADR-5 |
| Pattern Registry | Platform-fixed workflow skeletons. Provides state machine definitions, transition validity rules, role-per-step declarations, auto-maintained projection specifications. Deployer selects and parameterizes at L0. | **Settled** | ADR-5 |

---

## Gate Decision

All nine checks pass. No structural revision needed. No position requires modification. Two clarifications carry forward for explicit statement in the ADR.

**The coherence audit passes. ADR-005 writing may proceed.**
