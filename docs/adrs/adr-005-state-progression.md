# ADR-005: State Progression and Workflow

> Status: **Decided**
> Date: 2026-04-13
> Exploration: [Reading Guide](../exploration/guide-adr-005.md) · Raw: [19](../exploration/archive/19-adr5-session1-scoping.md) (Session 1), [20](../exploration/archive/20-adr5-session2-stress-test.md) (Session 2), [21](../exploration/archive/21-adr5-session3-part1-structural-coherence.md) (Session 3)

---

## Context

ADR-001 established immutable events as the storage primitive. ADR-002 established accept-and-flag conflict resolution with detect-before-act (S12) — events are never rejected for staleness (S14). ADR-003 established assignment-based access with sync-scope-as-authorization. ADR-004 established typed shapes, a platform-fixed 6-type event vocabulary, a four-layer configuration gradient with server-only triggers, and an expression language with two evaluation contexts. The event envelope was finalized at 11 fields.

ADR-004 explicitly deferred six questions to ADR-005:

1. Whether state machines are a platform primitive (enforced transitions) or a projection pattern (derived state)
2. Whether `status_changed` warrants a 7th structural event type
3. How multi-step, multi-actor workflows compose in an offline-first, append-only model
4. How workflow state interacts with the flag cascade when upstream events are retroactively flagged
5. Whether the `context.*` expression scope is needed for workflow-aware form logic
6. Whether domain conflict resolution can be automated, and how auto-resolution integrates with state machines

ADR-005 answers one question: **How does work move through stages?** ADRs 1–3 answered how the engine works. ADR-4 answered where the engine ends and the deployment begins. ADR-5 asks a behavioral question: given the committed architecture, how do multi-step, multi-actor workflows compose?

The exploration event-stormed four scenarios (S04 supervisor review, S08 case management, S11 multi-step approval, S07/S14 resource distribution), stress-tested all six positions with adversarial scenarios, applied the irreversibility filter, and audited structural coherence against all four prior ADRs. The result: ADR-005 has the **smallest irreversibility surface of all five ADRs** — zero envelope changes, zero type vocabulary changes, and one mild strategy-protecting position (a new flag category). Every other position is projection logic, configuration semantics, or server-side processing — all evolvable without data migration.

This is the first ADR exploration with zero upstream assumptions. All inputs are committed decisions.

---

## Decision

The platform uses **projection-derived state machines**, **platform-fixed workflow patterns**, and **source-only flagging with automated resolution** as its state progression model. Nine sub-decisions follow, classified by permanence: strategy-protecting constraints (S1–S3) guard structural invariants from prior ADRs; initial strategies (S4–S9) document the workflow architecture, all evolvable without affecting stored events.

There are no structural constraints in this ADR. No envelope field is added. No type is added to the vocabulary. The event envelope remains at 11 fields and the type vocabulary at 6 types. This confirms the trend observed from ADR-3 onward: each subsequent ADR touches fewer stored-data surfaces, because the foundational structures from ADRs 1–2 were designed with sufficient extensibility.

### Strategy-Protecting Constraints

These guard structural invariants established by prior ADRs. The invariants they protect cannot change; the implementations can evolve.

#### S1: Transition Violation Flag Category

**The Conflict Detector evaluates incoming events against pattern-defined state machine rules and raises a `transition_violation` flag when an event represents a state transition invalid under the subject's current derived state.**

An event flagged with `transition_violation` is stored (ADR-1 S1, append-only) and accepted (ADR-2 S14, never rejected for staleness). The flag surfaces the anomaly for resolution (P5). This is structurally identical to existing flag categories (`identity_conflict`, `stale_reference`, `scope_violation`, `concurrent_state_change`) — one more evaluation step in the same conflict detection pipeline.

Protects V1 (offline-first): without this, the alternative is rejection-based enforcement — blocking events that violate transition rules. But a CHV who fills out a case interaction form while offline, not knowing the case was closed, would lose work on sync. Transition violations are overwhelmingly caused by offline timing overlaps, not by malicious or careless data entry. Flagging preserves the work; rejection destroys it.

The flag type string `transition_violation` appears in stored `ConflictDetected` events. Adding the category is always safe (new events only; no change to existing flags). Removing it is never needed — existing flags persist unaffected.

#### S2: Flagged Events Excluded from State Machine Evaluation

**Events carrying unresolved flags are excluded from state machine evaluation in the projection engine. They appear in the event timeline but do not change `current_state`. When a flag is resolved as accepted, the projection re-derives state including the event. When a flag is resolved as rejected, the projection remains unchanged.**

Extends ADR-2 S12 (detect-before-act). S12 established: "flagged events do not trigger policy execution until the flag is resolved." S2 extends the same principle: flagged events do not change derived state. The rationale is identical — flagged events have uncertain validity; acting on uncertain data (whether by triggering policies or by advancing a state machine) produces downstream effects that are harder to reverse than the flag itself.

The projection shows: "case is in state X. 1 pending event awaiting flag resolution." The flagged event is visible — not hidden. It is excluded from state DERIVATION, not from the event TIMELINE.

Protects P5 (conflict is surfaced, not silently resolved): the projection reflects validated state, not tentative state. This prevents a scenario where a flagged transition advances the state machine, downstream actors act on the new state, and the flag is later resolved as "rejected" — requiring manual unwinding of all downstream work.

#### S3: Flag Resolvability Classification

**Each flag category carries a platform-defined resolvability classification: `auto_eligible` or `manual_only`. Auto-resolution policies (S9) can target only `auto_eligible` flag types. The classification is platform-level policy, not deployer-configurable.**

Initial classification:

| Flag category | Classification | Rationale |
|---------------|---------------|-----------|
| `transition_violation` | `auto_eligible` | Usually timing overlap between offline actors. Low severity. |
| `stale_reference` | `auto_eligible` | Entity updated after event creation. Self-correcting in most cases. |
| `scope_violation` | `manual_only` | Potential unauthorized access. Must be human-reviewed. |
| `identity_conflict` | `manual_only` | Potential duplicate subjects. Merge decision requires human judgment. |
| `concurrent_state_change` | `manual_only` | Two actors changed the same subject concurrently. Resolution requires domain judgment. |
| `domain_uniqueness_violation` | `manual_only` | Business rule violation. Context-dependent resolution. |

Protects security and data integrity: prevents auto-resolution from silently dismissing security-relevant flags. A deployer cannot make `scope_violation` auto-resolvable. Promoting a flag type from `manual_only` to `auto_eligible` is a platform code change — deliberate, reviewed, deployed.

### Initial Strategies

These document the workflow architecture. Each can evolve without affecting stored events.

#### S4: State Machines as Projection Patterns

**Subject lifecycle state is derived from the event sequence by the projection engine using pattern-defined state machine rules. State is never stored in events. The platform does not enforce transitions — it flags violations.**

The append-only, offline-first constraints from ADR-1 and ADR-2 force this answer. The constraint chain admits no alternative:

1. V1 (offline-first) → P1 (no operation requires connectivity)
2. P3 (append-only) → ADR-1 S1 (all writes append-only)
3. ADR-2 S14 (events never rejected for staleness)
4. Therefore: enforcement-based state machines (which reject events violating transition rules) violate all three.

State is always computable from events. In all four event-stormed scenarios (S04, S08, S11, S07/S14), the current state is unambiguously derivable from the event sequence using shape identity and causal ordering. Storing state in events would be redundant (creates a consistency risk when derived and stored state disagree) and would require a new envelope field (violates the closed-envelope commitment from ADR-4).

**The on-device Command Validator is advisory, not blocking.** On-device, the validator can warn: "this case is resolved — are you sure you want to add an interaction?" The user can override the warning. The event is written regardless. On-server, the same transition rules are evaluated and `transition_violation` flags are created for invalid transitions (S1). The validator improves user experience (fewer unnecessary flags) without compromising the append-only contract.

**Evaluated and rejected: `status_changed` as 7th structural event type.** A state-transition event requires no different platform processing from `capture`. The distinction between state-changing events (a `case_resolution` shape) and state-preserving events (a `case_interaction` shape) is expressed through the shape + the pattern definition, not through the event type. The type tells the platform HOW to process the event structurally; the shape + pattern tells it WHAT the event means in the domain. Adding `status_changed` was evaluated and found to add zero processing-behavior value that existing types do not already provide. The type vocabulary remains at 6. The escape hatch remains: the vocabulary is append-only (ADR-4 S3), so `status_changed` can be added later if a future scenario reveals genuinely different processing behavior.

#### S5: Pattern Registry

**The platform provides a Pattern Registry: a set of platform-fixed workflow skeletons that deployers select and parameterize at Layer 0.** Patterns are a closed vocabulary with the same governance as event types — platform-fixed, deployer-referenced, not deployer-authored. Adding a pattern is a platform evolution, not a deployer action.

Each pattern provides:

| Component | What it defines |
|-----------|----------------|
| Participant roles | Which roles interact and in what capacity (capturer, reviewer, approver, sender, receiver) |
| Structural event types | Which of the platform's 6 types participate |
| State machine skeleton | Named states, valid transitions, which shapes are state-changing vs. state-preserving |
| Auto-maintained projections | What the projection engine computes automatically (current_state, pending_since, time_in_state) |
| Parameterization points | What the deployer fills in (which shapes, which roles, how many levels, what deadlines) |

The deployer does not see or author the state machine. They see: "this is a case management activity where CHVs track malaria cases, supervisors review and close, and follow-ups are expected every 7 days." They select a pattern, map shapes to pattern roles, set deadlines, and deploy. This is Layer 0 — assembling from platform-provided components.

Pattern definitions are synced to devices as part of the atomic configuration package (ADR-4 S6). The projection engine uses them to derive state. The conflict detector uses them to evaluate transition validity (S1).

The pattern **inventory** (which specific patterns ship initially and their exact state machine skeletons) is not committed in this ADR. The inventory is implementation scope — the set will grow. The architecture (S4) and composition model (S6) are the architectural decisions. Four patterns were validated as existence proofs during the exploration:

- **capture_with_review**: Submit → review → accept/return → resubmit cycle. Covers S04 (supervisor review).
- **case_management**: Open → interact → refer/transfer → resolve → close → reopen. Long-running, multi-actor. Covers S08.
- **multi_step_approval**: Submit → level-1 review → level-N review → final decision. Parameterized level count. Covers S11.
- **transfer_with_acknowledgment**: Dispatch → receive → discrepancy → resolve. Two-party coordination. Covers S07/S14.

#### S6: Pattern Composition Rules

**Five rules govern how patterns compose within and across activities.** These are enforced at deploy-time validation (ADR-4 S13 model) and in the projection engine.

**Rule 1: One subject-level pattern per activity.** An activity binds at most one subject-level state machine to a subject. No competing lifecycle state machines on the same subject within the same activity. This prevents the hardest composition problem — conflicting state machines — by construction.

**Rule 2: Event-level patterns compose freely.** Review patterns (capture_with_review) track per-event states — the review status of a specific event, not the lifecycle state of the subject. Any event within any subject-level pattern can be reviewed. The review state machine operates on events, not subjects. No conflict because the state spaces are disjoint.

**Rule 3: Approval sub-flows embed within subject patterns.** A subject under case_management can have one or more multi_step_approval sub-flows for specific decisions (e.g., a drug regimen change requiring multi-level approval). Each sub-flow is scoped to a specific submission event — effectively event-level, even though it has subject-level implications.

**Rule 4: Cross-activity linking uses `activity_ref`, not shared patterns.** When two activities interact (a campaign and its supply distribution), the link is the `activity_ref` field in the event envelope or a subject-ref cross-reference in the payload. Patterns do not span activities.

**Rule 5: Shape-to-pattern mapping is unique within an activity.** No two patterns within the same activity claim the same shape. Deploy-time validation rejects configurations where two patterns list the same shape. This is AP-6 prevention — no overlapping authority over the same event.

The projection engine maintains:
- One **subject-level state** per (subject, activity, pattern) tuple
- Multiple **event-level states** per (event_id, pattern) — review status, approval progress
- Subject-level and event-level states are queried independently

#### S7: Source-Only Flagging

**When an upstream event is retroactively flagged, only the root-cause event receives a flag. Downstream events created from flagged sources do not receive additional flags.** Downstream "contamination" is a computed projection property — not additional stored flags.

The projection engine supports **source-chain traversal**: given any event, walk its `source_event_ref` chain (in the payload, already present for review and trigger output events) back to the originating event and surface any flags in that chain. The UI renders: "⚠ This task was created from a flagged source event." When the root flag is resolved, the indicator disappears from all downstream projections.

Why not flag propagation (creating derived flags on downstream events):
- **Scale**: One root cause produces N downstream flags. A busy coordinator with 200 flags gets 200× the resolution work for the same number of decisions. Every downstream flag resolves to "see root cause."
- **Redundancy**: Downstream flags carry no additional decision information. They all say: "this event descends from a flagged source." The projection can compute that directly.
- **Resolution**: Under source-only flagging, resolving one root flag fully resolves the chain (O(1)). Under flag propagation, the resolver must also resolve each derived flag individually (O(N)) — busywork with no decision value.

**Interaction with detect-before-act (ADR-2 S12):** Detect-before-act prevents triggers from firing on flagged events AT THE TIME OF DETECTION. For retroactive flags (flag raised after the trigger already fired), the trigger output (event B) exists and was valid when created. The platform does not retroactively invalidate trigger outputs. The flag on event A surfaces the root cause; a human decides whether B's outcome needs revisiting. This is P5 — the platform makes provenance visible, not automatically consequential.

#### S8: Context Expression Scope

**The expression evaluator gains a `context.*` data scope in the form evaluation context, providing 7 pre-resolved, read-only values derived from the local projection and assignment state.** Trigger expressions (server-side) do NOT access `context.*`.

| Property | Source | What it enables |
|----------|--------|----------------|
| `context.subject_state` | Subject-level pattern state from projection | State-aware form sections (show reopening fields only when case is resolved) |
| `context.subject_pattern` | Pattern assigned to this subject's activity | Pattern-aware conditional logic |
| `context.activity_stage` | Current stage of the activity (for campaigns) | Stage-aware form behavior |
| `context.actor.role` | Current actor's role from assignment resolver | Role-aware form logic |
| `context.actor.scope_name` | Actor's assigned scope (facility, district name) | Pre-fill location fields |
| `context.days_since_last_event` | Days since last event on this subject | Overdue warnings |
| `context.event_count` | Total events for this subject | First-visit vs. follow-up logic |

All properties are **pre-resolvable on-device from data already present** — local projection data and assignment resolver state. No new data needs to sync. The device resolves these values once at form-open time; they are static during form fill. From the expression evaluator's perspective, `context.subject_state` is syntactically identical to `entity.facility_name` — a named value in its namespace.

The property vocabulary is **platform-fixed, closed, append-only** — same governance as event types. A deployer cannot define `context.my_field`. Adding a property is a platform code change. This prevents AP-1 (inner platform effect — deployer-extensible expression scopes would be a step toward a query language).

The expression language grammar is unchanged. The evaluator gains one more data scope in the form context, not a new evaluation mode. ADR-4 S11's language specification stands: operators + field references, zero functions.

#### S9: Auto-Resolution as L3b Sub-Type

**Deployers can configure auto-resolution policies — a sub-type of L3b deadline-check triggers — that automatically resolve `auto_eligible` flags when specific conditions are met within a time window.**

An auto-resolution policy declares:

| Parameter | What it specifies |
|-----------|------------------|
| `flag_type` | Which flag category to watch. Must be `auto_eligible` (S3). |
| `condition` | Structural match on the flag (which transition, which shape). Not a general expression — a parameterized structure. |
| `watch_for` | An event shape on the same subject that would resolve the flag. |
| `within` | Time window (duration). |
| `if_watched_event_arrives` | Resolution decision and note (auto-resolve the flag). |
| `if_deadline_expires` | Escalation action (mark for manual review, notify supervisor). |

The mechanism is structurally identical to L3b deadline-check triggers:

| L3b deadline check | Auto-resolution |
|-------------------|-----------------|
| Watches for: response event after trigger event | Watches for: resolution-enabling event after flag |
| Deadline: time window | Deadline: time window |
| If response arrives: cancel deadline | If enabling event arrives: auto-resolve flag |
| If deadline expires: escalate | If deadline expires: escalate or mark for manual review |
| Creates an event (escalation) | Creates an event (flag resolution via `ConflictResolved`) |
| Server-side, asynchronous | Server-side, asynchronous |

No new infrastructure. The trigger engine handles auto-resolution using the same execution model it already implements for deadline checks.

**Auto-resolution events use `actor_ref` format `system:auto_resolution/{policy_id}`.** This extends ADR-4 S4's system actor convention, adding `auto_resolution` as a source_type alongside `trigger`. Every auto-resolution is traceable and auditable — it produces a standard `ConflictResolved` event that a human can inspect and override.

**Loop prevention** relies on three independent guards, all from prior ADRs:
1. Detect-before-act (ADR-2 S12): auto-resolution events go through the normal pipeline; if flagged themselves, they don't trigger further policies.
2. DAG max path 2 (ADR-4 S12): at most 2 levels of trigger/auto-resolution chaining.
3. Input/output separation: auto-resolution watches flags, not trigger outputs. The input space and output space do not overlap.

**Budget**: Auto-resolution policies count toward the L3b deployment-wide limit. Total L3b policies (deadline-check + auto-resolution) ≤ 50 per deployment.

---

## What This Does NOT Decide

| Concern | Belongs to | Why deferred |
|---------|-----------|--------------|
| Pattern inventory (which patterns ship initially, exact state machine skeletons) | Implementation / Platform specification | The architecture is decided (S4, S5, S6). Which patterns ship is inventory, not architecture. The set will grow. |
| Pattern skeleton formal schemas (YAML/JSON format for state machine definitions) | Implementation | The semantic model is decided. The authoring surface is tooling. |
| Projection rebuild strategy for workflow state (incremental vs. full) | Implementation | S4 constrains what is computed. How it is computed efficiently is server-side/device-side optimization. |
| Source-chain traversal depth limits | Implementation | S7 defines the capability. Maximum traversal depth is a performance tuning parameter. |
| `context.*` property caching on-device | Implementation | S8 defines what is available. How properties are cached between form opens is device architecture. |
| Auto-resolution policy authoring UX | Implementation | S9 defines the mechanism. How deployers author policies is tooling. |
| Workflow-aware reporting and aggregation | Platform specification | How workflow state feeds into aggregate reporting (completion rates, average resolution time) is a platform capability, not an architectural decision. |
| Platform specification document | Post-ADR | Consolidating all primitives from ADR-1 through ADR-5 into a single implementation-grade reference. |

---

## Consequences

### What is now constrained

- **Event envelope**: Unchanged. 11 fields across five ADRs. The envelope's extensibility clause (ADR-1 S5) remains available but five ADRs have required zero use of it — the original 4-field minimum plus 7 additions across ADRs 2 and 4 have proven sufficient for the platform's full behavioral range, from basic capture through multi-step, multi-actor workflows.

- **Type vocabulary**: Unchanged at 6 types. `status_changed` was evaluated and rejected; the escape hatch (append-only vocabulary, ADR-4 S3) is preserved.

- **Conflict detection pipeline**: Gains one evaluation step (transition violation). The pipeline structure is unchanged — same input (incoming event + projection state), same output (flag on event), same timing (at sync processing, before policy execution).

- **Projection engine**: Now includes state machine evaluation as a projection capability. The engine derives workflow state from events + pattern definitions. Flagged events are excluded from state derivation (S2). Source-chain traversal surfaces upstream flags (S7). These are capability additions to the existing engine, not structural changes.

- **Expression evaluator**: Gains one data scope (`context.*` with 7 properties) in the form evaluation context. The grammar, operators, and evaluation model are unchanged.

- **Trigger engine**: Gains auto-resolution as a L3b sub-type. Same execution model, same limits, same loop-prevention guards. The engine distinguishes deadline-check triggers from auto-resolution policies by their input (event vs. flag) — not by their processing pipeline.

- **Configuration validation**: Gains five new deploy-time checks: shape-to-pattern uniqueness, one subject-level pattern per activity, pattern role mapping completeness, auto-resolution flag type eligibility, auto-resolution within L3b budget.

- **Platform implementation**: Must deliver: a Pattern Registry (storing named workflow definitions), state machine evaluation in the projection engine, `transition_violation` detection in the conflict pipeline, `context.*` pre-resolution in the form engine, and auto-resolution infrastructure in the L3b trigger engine. All are additions to existing primitives — no new engines on-device.

### Risks accepted

- **Projection rebuild cost scales with workflow complexity.** Case management subjects (S08) may accumulate 50–100 events over months. State derivation replays the full sequence per subject. Mitigation: the B→C escape hatch (ADR-1) — add application-maintained views alongside the event store. Revisit trigger: projection rebuild for a single subject exceeds 200ms on the reference low-end device.

- **Pattern inventory may prove insufficient for real deployments.** Four patterns were validated as existence proofs. Real deployments may need patterns not yet defined (e.g., scheduled inspection cycles, multi-party negotiation). Adding patterns is a platform code change, not a data migration — but it requires platform developer time. Revisit trigger: >2 deployments in the first year request custom state machines that correspond to no existing pattern.

- **Source-only flagging loses explicit downstream contamination markers.** Under source-only flagging, a user viewing a downstream event (a task created from a flagged source) relies on the projection's source-chain traversal to surface the upstream flag. If the projection UI fails to render the traversal result, the contamination is invisible. Mitigation: source-chain traversal is a projection capability, not optional UI behavior — it must be rendered. Revisit trigger: user research finds >20% of downstream-contamination cases are missed by reviewers.

- **Auto-resolution may mask legitimate issues in `auto_eligible` categories.** Transition violations are "usually" timing overlaps — but not always. An auto-resolved violation that was actually a data quality problem goes unreviewed. Mitigation: auto-resolution events are standard `ConflictResolved` events — auditable, reviewable, searchable. Periodic audit of auto-resolutions can surface systematic misclassification. Revisit trigger: >5% of auto-resolved flags are manually re-opened within 30 days.

- **`context.*` scope creates a form-to-projection dependency.** If the projection engine has a bug that computes wrong state, forms display wrong conditional logic (hiding fields that should be visible or vice versa). Mitigation: context values are pre-resolved at form-open time and static during fill — the dependency is a one-time read, not a live query. The submitted event's payload is the ground truth, not the form state. Revisit trigger: bug reports where form behavior diverged from expected state-aware logic.

### Principles confirmed

- **P1 (Offline is the default)**: State is derived from the local projection. `context.*` is pre-resolved from local data. The Command Validator is advisory — never blocks offline capture. No workflow step requires connectivity. Even transition violations are handled gracefully: work done offline is always accepted, always stored, always syncable.

- **P2 (Configuration has boundaries)**: Patterns are platform-fixed — deployers select and parameterize, they do not author state machines. `context.*` is platform-fixed — deployers reference properties, they do not define them. Auto-resolution targets platform-classified flag types only. The configuration boundary from ADR-4 is preserved and extended: deployers work within the vocabulary, platform developers extend it.

- **P3 (Records are append-only)**: Invalid transitions are flagged, never rejected. Events are always stored. Derived state is never written to event payloads — it exists only in projections, which are rebuildable. Auto-resolution produces standard `ConflictResolved` events — auditable records, not silent corrections.

- **P4 (Patterns compose; exceptions don't)**: Five composition rules formalize how patterns interact. Subject-level and event-level state machines compose independently. Cross-activity linking uses `activity_ref`. No pattern spans more than one activity. The composition model was validated against three multi-pattern scenarios: case management + approval, campaign + distribution, entity lifecycle + review.

- **P5 (Conflict is surfaced, not silently resolved)**: Transition violations are a new flag category — surfaced, not silently swallowed. Source-only flagging preserves provenance visibility through source-chain traversal. Auto-resolution is explicit: it produces traceable events with system actor identity. No conflict is ever silently dismissed.

- **P6 (Authority is contextual and auditable)**: Pattern role declarations are explicit in the activity configuration. Auto-resolution events carry `system:auto_resolution/{policy_id}` — fully traceable. Every state transition, every flag, every resolution is attributable.

- **P7 (Simplest scenario stays simple)**: S00 (basic structured capture) is unchanged. No patterns, no state machines, no composition rules, no flags, no auto-resolution. The implicit pattern is `capture_only` — zero overhead. S00 still requires exactly 2 configuration artifacts (one shape, one activity). The entire workflow apparatus from ADR-5 exists only when needed and adds zero weight to scenarios that don't use it.

---

## Traceability

| Sub-decision | Classification | Key forcing inputs |
|---|---|---|
| S1 (transition_violation flag) | Strategy-protecting | ADR-1 S1, ADR-2 S14, V1, P5, Session 1 Q1 |
| S2 (flagged events excluded from state derivation) | Strategy-protecting | ADR-2 S12, P5, Session 3 coherence Check (b) |
| S3 (flag resolvability classification) | Strategy-protecting | P5, Session 2 Q6 adversarial test |
| S4 (state machines as projection patterns) | Initial strategy | ADR-1 S1, ADR-2 S14, V1/P1/P3, Session 1 Q1/Q2 |
| S5 (Pattern Registry) | Initial strategy | Session 1 §5, ADR-4 S3/S9, Session 2 §6.1 |
| S6 (pattern composition rules) | Initial strategy | Session 2 Q3 walk-throughs, AP-6, Session 3 Check (e) |
| S7 (source-only flagging) | Initial strategy | ADR-2 S12, Session 2 Q4, Session 3 Check (b) |
| S8 (context.* expression scope) | Initial strategy | ADR-4 S11, AP-1, Session 2 Q5, Session 3 Check (d) |
| S9 (auto-resolution as L3b sub-type) | Initial strategy | ADR-4 S12, ADR-2 S12, Session 2 Q6, Session 3 Check (h) |
