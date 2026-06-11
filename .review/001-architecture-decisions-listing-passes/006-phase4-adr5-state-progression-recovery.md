# Phase 4: ADR-005 State Progression + Workflow Recovery

## Context Capsule

- Recovery mode: archaeological reconstruction only; no redesign, no alternatives, no reopening.
- Anchor file: `002-phase0-decision-register.md`.
- Upstream recovered sources:
  - `003-phase1-adr2-identity-conflict-recovery.md`
  - `004-phase2-adr3-auth-sync-recovery.md`
  - `005-phase3-adr4-config-boundary-recovery.md`
- ADR target: ADR-005 S1–S9.
- Source files read:
  - `21-adr5-session3-part1-structural-coherence.md`
  - `20-adr5-session2-stress-test.md`
  - `19-adr5-session1-scoping.md`
- Reading order used: `21 → 20 → 19`.
- Plan adjustment made:
  - Original recovery strategy integrated ADR-005 vocabulary during cross-lineage consolidation.
  - Updated plan inserts this dedicated ADR-005 recovery pass before consolidation.
  - Previous Phase 4 becomes Phase 5: cross-lineage vocabulary consolidation.
  - Previous Phase 5 becomes Phase 6: authoritative architecture map.
- Classification of this plan adjustment:
  - Recovery-method adjustment, not architecture change.
  - Expansion of an explicitly open front: ADR-004 deferred `status_changed`, `context.*`, auto-resolution, and pattern/state progression to ADR-005.
  - Does not violate accepted decisions because all extracted material is checked against ADR-005 S1–S9.
- Settled outputs:
  - State progression model: state machines are projection patterns, not stored state and not rejection mechanisms.
  - `transition_violation` flag category and its interaction with detect-before-act.
  - Flagged-event exclusion from state derivation.
  - Platform-level flag resolvability classification: `auto_eligible` / `manual_only`.
  - Pattern Registry boundary: platform-fixed workflow skeletons selected and parameterized at L0.
  - Five workflow composition rules.
  - Source-only flagging and source-chain traversal.
  - `context.*` expression scope with seven pre-resolved read-only values.
  - Auto-resolution as an L3b subtype using `system:auto_resolution/{policy_id}`.
- Rejected / excluded:
  - State machine enforcement by event rejection.
  - Storing current state in event payload or envelope.
  - Adding `status_changed` as ADR-005 structural type.
  - Adding `pattern_ref` to the event envelope.
  - Deployer-authored state machines.
  - Deployer-defined `context.*` properties.
  - Dynamic cross-subject or cross-event form queries through `context.*`.
  - Propagating flags to every downstream event.
  - Auto-resolution as an unbounded rule engine.
  - Treating initial pattern examples as frozen platform inventory.
- Deferred / open evolution:
  - Exact pattern inventory and skeleton specifications → platform specification / platform evolution.
  - New pattern types → platform evolution, not deployment configuration.
  - Additional `context.*` properties → platform evolution that preserves closed append-only vocabulary.
  - Richer source-chain visualization and flag queue ergonomics → platform evolution.
  - Additional auto-resolution policies → platform evolution within L3b guardrails.
  - Pattern migration mechanics → platform evolution / implementation strategy.
- Terms locked in this pass:
  - `transition_violation`
  - `flagged-event exclusion`
  - `auto_eligible`
  - `manual_only`
  - `flag resolvability`
  - `projection-derived state machine`
  - `Command Validator`
  - `Pattern Registry`
  - `pattern`
  - `participant roles`
  - `state machine skeleton`
  - `parameterization points`
  - `subject-level pattern`
  - `event-level pattern`
  - `composition rules`
  - `source-only flagging`
  - `source-chain traversal`
  - `source_event_ref`
  - `context.*` scope
  - `context.subject_state`
  - `context.subject_pattern`
  - `context.activity_stage`
  - `context.actor.role`
  - `context.actor.scope_name`
  - `context.days_since_last_event`
  - `context.event_count`
  - `auto-resolution`
  - `auto_resolution` source type
  - `system:auto_resolution/{policy_id}`

---

## 0. Phase Plan Adjustment

The original plan treated ADR-005 as already complete and integrated its vocabulary during cross-lineage consolidation. That was safe but asymmetrical: ADR-002, ADR-003, and ADR-004 each received a dedicated recovery file while ADR-005 would only be folded into the consolidated vocabulary.

The revised plan is:

| Revised phase | Output file | Scope |
|---|---|---|
| Phase 1 | `003-phase1-adr2-identity-conflict-recovery.md` | ADR-002 identity + conflict |
| Phase 2 | `004-phase2-adr3-auth-sync-recovery.md` | ADR-003 authorization + sync |
| Phase 3 | `005-phase3-adr4-config-boundary-recovery.md` | ADR-004 configuration boundary |
| Phase 4 | `006-phase4-adr5-state-progression-recovery.md` | ADR-005 state progression + workflow |
| Phase 5 | `007-phase5-cross-lineage-vocabulary.md` | Merge vocabulary, primitive taxonomy, interaction model |
| Phase 6 | `008-authoritative-architecture-map.md` | Final architecture reference |

This changes the recovery process, not the settled architecture.

### Why this adjustment is valid

ADR-005 has its own settled sub-decision set in the Phase 0 register. That means it can be recovered through the same ADR-anchored method as ADR-002 through ADR-004.

The ADR-005 exploration lineage is also unusually clean:

- Session 1 defines the decision surface and resolves the core forcing function: state as projection, not enforcement.
- Session 2 applies the stress test and irreversibility filter, raising all questions to high confidence.
- Session 3 verifies structural coherence against ADR-001 through ADR-004 and produces the final primitive map.

The safe reading order is therefore reverse-clean:

```txt
21 → 20 → 19
```

Read the coherence audit first, then the stress-test classifications, then the initial scoping/event-storm evidence.

---

## 1. ADR Checkpoint

This pass is bounded by ADR-005 S1–S9 from the Phase 0 register.

| ADR-005 ID | Recovery use in this pass |
|---|---|
| S1 | Recover `transition_violation` as a flag category and its boundary relative to existing conflict flags. |
| S2 | Recover flagged-event exclusion from state machine evaluation while preserving timeline visibility. |
| S3 | Recover platform-level flag resolvability classification: `auto_eligible` / `manual_only`. |
| S4 | Recover state machines as projection patterns; state never stored; `Command Validator` advisory. |
| S5 | Recover Pattern Registry as platform-fixed workflow skeleton vocabulary selected/parameterized at L0. |
| S6 | Recover the five composition rules for subject-level and event-level patterns. |
| S7 | Recover source-only flagging and source-chain traversal. |
| S8 | Recover `context.*` expression scope and its seven pre-resolved read-only properties. |
| S9 | Recover auto-resolution as L3b subtype, system actor format, and loop-prevention guards. |

Everything below maps to one or more of these entries. Anything that appears only in exploration and does not map to S1–S9 is marked rejected, excluded, deferred, or implementation strategy.

---

## 2. Upstream Boundary from Phases 1–3

ADR-005 sits on top of the full previously settled architecture. It does not redefine storage, identity, authorization, configuration, triggers, or event type semantics.

| Upstream term / rule | How ADR-005 uses it | Boundary |
|---|---|---|
| `event` | Workflow state is derived by replaying events. | ADR-005 does not introduce stored mutable state. |
| append-only event store | Invalid transitions create flags; events are not rejected. | Workflow enforcement cannot violate append-only/offline-first semantics. |
| `device_sequence` / `sync_watermark` | Projection-derived state uses existing causal ordering. | ADR-005 adds no ordering primitive. |
| `accept-and-flag` | Transition violations are surfaced through the flag model. | No state transition is rejected only because current projection says it is invalid. |
| `detect-before-act` | Flagged events do not trigger policies and do not alter derived workflow state. | ADR-005 extends the principle to state derivation. |
| `source_event_ref` | Source-chain traversal follows event causality for downstream lineage. | No flag propagation needed. |
| assignment-based access | Pattern roles resolve through assignments and scope containment. | ADR-005 does not introduce a new authorization model. |
| `sync scope = access scope` | Workflow events sync according to subject/activity access scope. | Pattern state does not create separate sync authority. |
| `shape_ref` | Shape-to-pattern mapping determines which pattern an event participates in. | No `pattern_ref` envelope field. |
| `activity_ref` | Cross-activity workflow linkage uses activity context. | Patterns do not span activities by hidden envelope metadata. |
| six structural `type` values | Existing `capture`/`review`/`task_*` types express state progression. | `status_changed` not added by ADR-005. |
| L0–L3 gradient | Pattern selection is L0; `context.*` is L2 form scope; auto-resolution is L3b. | ADR-005 fits the ADR-004 boundary without changing the gradient. |
| server-only triggers | Auto-resolution uses server-side L3b behavior. | No device-side trigger engine introduced. |

---

## 3. Decision Boundary Classification

ADR-005 has no structural event-envelope commitment. It is dominated by strategy-protecting constraints and initial strategies.

### 3.1 Structural constraints

None.

ADR-005 does not add event envelope fields, does not add a structural event type, and does not change the 11-field envelope.

### 3.2 Strategy-protecting constraints

| Item | What it protects | ADR anchor |
|---|---|---|
| `transition_violation` | Invalid workflow transitions are surfaced without rejecting events. | S1 |
| Flagged-event exclusion | Derived state reflects validated state, not uncertain flagged data. | S2 |
| Flag resolvability classification | Prevents deployer-configured auto-resolution of flags that require human judgment. | S3 |

### 3.3 Initial strategies

| Item | Current position | ADR anchor |
|---|---|---|
| Projection-derived state machines | State is computed by projection, never stored. | S4 |
| Advisory `Command Validator` | Warns on-device; server creates transition flags on sync. | S4 |
| Pattern Registry | Platform-fixed skeleton vocabulary selected and parameterized at L0. | S5 |
| Composition rules | Five rules governing subject-level and event-level patterns. | S6 |
| Source-only flagging | Root cause flag only; downstream impact computed in projection. | S7 |
| `context.*` | Seven pre-resolved read-only values in form context. | S8 |
| Auto-resolution | L3b subtype using bounded server-side policy. | S9 |

---

## 4. State Progression Model

### 4.1 Settled rule

State machines are projection patterns.

```txt
current_state = f(event_stream, pattern_definition, config_version, flag_status)
```

State is not stored in events.

State is not a separate mutable record.

State is not an event-envelope field.

State is not authoritative if it disagrees with the replayed event stream.

### 4.2 Why enforcement was rejected

A state machine as an enforcement primitive would reject invalid transitions.

That breaks the upstream contract:

- ADR-001 stores immutable events.
- ADR-002 says events are never rejected for state staleness.
- Offline devices may act on stale local projections.
- The platform must preserve what happened, then surface anomalies.

Therefore, invalid workflow transitions are accepted and flagged.

### 4.3 Projection-derived state lifecycle

| Stage | Semantics | Boundary |
|---|---|---|
| No events | Subject has no workflow state for the activity/pattern. | Projection derives absence. |
| Initial event | A shape mapped to a pattern starts state derivation. | Existing `capture` type is enough. |
| Intermediate events | `capture`, `review`, `task_created`, `task_completed`, or other existing types move the projection through pattern-defined states. | Type is structural processing behavior, not workflow state name. |
| Invalid transition | Event is stored, flagged as `transition_violation`, and excluded from state derivation until resolved. | Flag model, not rejection. |
| Accepted resolution | Projection re-derives including the event. | State can change after resolution. |
| Rejected resolution | Projection continues excluding the event from state derivation. | Event remains visible in timeline. |

### 4.4 Current-state authority

The projection is authoritative because it is rebuildable from events plus config.

Storing `current_state` in an event would create two authorities:

1. the event stream-derived state;
2. the stored state claim.

ADR-005 rejects that split. If state can be derived, it is not stored.

---

## 5. `status_changed` Evaluation

ADR-004 deferred `status_changed` to ADR-005 because a new structural event type is justified only when platform processing behavior differs.

ADR-005 resolves the question:

```txt
status_changed is not added.
```

### 5.1 Reason

Workflow progress can be expressed with the existing structural event types plus shapes and pattern definitions.

Examples:

| Domain action | Structural expression |
|---|---|
| Case opened | `capture` with case-opening shape |
| Case resolved | `capture` with case-outcome shape |
| Approval decision | `review` with approval-decision shape |
| Task created | `task_created` with task shape |
| Task completed | `task_completed` with task shape |

The state change is a projection result, not the structural event type.

### 5.2 Rejected path

| Path | Status | Reason |
|---|---|---|
| `status_changed` as 7th type | `[REJECTED / no action in ADR-005]` | State-changing vs state-preserving behavior is pattern/shape semantics, not distinct platform processing. |

### 5.3 Evolution rule

A new structural type remains possible as platform evolution only if a future scenario proves genuinely different platform processing behavior.

That would be an append-only extension to the type vocabulary, not deployment configuration.

---

## 6. Pattern Registry

### 6.1 Definition

The Pattern Registry is the platform-fixed vocabulary of workflow skeletons.

A pattern provides:

- state machine skeleton;
- valid transition table;
- participant role declarations;
- parameterization points;
- shape-role/stage slots;
- projection specification;
- transition validity rules for the conflict detector.

### 6.2 Boundary

| Owns | Does not own |
|---|---|
| Platform-fixed workflow skeletons. | Deployer-authored state machines. |
| State derivation rules. | Custom deployer transition code. |
| Transition validity rules. | Custom deployer validators that reject events. |
| Role-per-step declarations. | The actual actor assignment timeline. |
| Pattern parameterization slots. | Deployment-specific role names, shape names, or scope assignments. |
| Projection specification for workflow state. | Stored state in events. |

### 6.3 Pattern lifecycle

| Stage | Semantics | Boundary |
|---|---|---|
| Defined by platform | Pattern skeleton is implemented and released as platform vocabulary. | Platform evolution. |
| Selected by deployer | Activity chooses a pattern. | L0 Assembly. |
| Parameterized | Activity maps shapes, roles, deadlines, and levels into pattern slots. | L0 Assembly / L3b parameterization. |
| Delivered | Pattern definitions and activity bindings sync in the atomic config package. | ADR-004 config delivery. |
| Evaluated | Projection Engine derives current state; Conflict Detector checks transition validity. | Runtime projection/server processing. |
| Evolved | Platform changes pattern implementation or adds new patterns. | Platform evolution with projection rebuild. |

### 6.4 Pattern inventory boundary

The pattern architecture is settled.

The exact initial inventory is not frozen as architecture.

Examples such as `capture_with_review`, `case_management`, `multi_step_approval`, and `transfer_with_acknowledgment` are existence proofs and likely initial strategies. Their exact skeleton definitions belong in the platform specification, not this recovery pass.

### 6.5 Deployer parameterization

Deployer activity configuration can select and fill pattern slots:

```txt
activity: malaria_case_tracking
pattern: case_management
shapes:
  opening: malaria_case_opening
  interaction: malaria_follow_up
  resolution: malaria_case_outcome
roles:
  assigned_worker: chv
  supervisor: clinic_supervisor
scope: geographic
deadlines:
  follow_up_interval: 7d
```

This is assembly, not programming.

The deployer does not author the state machine.

---

## 7. Composition Rules

ADR-005 settles five composition rules.

| Rule | Semantics | Enforcement |
|---|---|---|
| 1. One subject-level pattern per activity | An activity binds at most one subject-level lifecycle state machine to a subject. | Deploy-time validation. |
| 2. Event-level patterns compose freely | Review/approval status can attach to individual events without competing with subject lifecycle state. | Projection Engine. |
| 3. Approval sub-flows embed | Multi-step approval is scoped to a submission/source event, not a competing subject lifecycle. | Configuration + projection. |
| 4. Cross-activity linking uses `activity_ref` | Patterns do not secretly span activities; related work is correlated by activity or payload refs. | Existing envelope field and payload refs. |
| 5. Shape-to-pattern mapping is unique within an activity | No two patterns in the same activity claim the same shape. | Deploy-time validation. |

### 7.1 Subject-level pattern

A subject-level pattern derives lifecycle state for a subject within an activity.

Examples:

- case lifecycle;
- facility lifecycle;
- campaign participation lifecycle.

It answers:

```txt
What state is this subject in for this activity?
```

### 7.2 Event-level pattern

An event-level pattern derives state for a specific source event.

Examples:

- review status of one captured observation;
- approval progress for one drug-change request;
- acknowledgment status for one transfer event.

It answers:

```txt
What state is this event/sub-flow in?
```

### 7.3 Why the distinction matters

Without the distinction, a review flow and a case lifecycle appear to compete over the same state space.

ADR-005 resolves the tension:

- subject-level state and event-level state are separate projections;
- they can coexist over the same subject because they answer different questions;
- deploy-time validation prevents two subject-level patterns from claiming the same subject/activity state space.

---

## 8. `transition_violation`

### 8.1 Definition

A `transition_violation` flag marks an event whose implied state transition is invalid under the applicable pattern definition.

Example:

```txt
current_state = closed
incoming event shape = case_interaction
pattern rule = case_interaction is valid only from opened/active/referred/reopened
result = transition_violation
```

### 8.2 Processing semantics

| Step | Behavior |
|---|---|
| Event arrives | Event is stored. |
| Pattern state is evaluated | Conflict Detector checks transition validity. |
| Invalid transition found | `transition_violation` flag is created/attached. |
| Detect-before-act applies | The flagged event does not trigger L3 policies. |
| State derivation applies | The flagged event is excluded from state machine evaluation. |
| Timeline remains complete | The flagged event is visible as historical/audit data. |

### 8.3 Boundary

`transition_violation` extends the flag model. It does not extend the event envelope.

It is strategy-protecting because the flag category appears in stored flag events, but it is additive and does not require migration of existing events.

---

## 9. Flagged-Event Exclusion

### 9.1 Settled rule

Flagged events are excluded from state machine evaluation in the projection engine.

They remain visible in the event timeline.

They do not change `current_state` while unresolved.

When the flag is resolved as accepted, the projection re-derives state including the event.

When the flag is resolved as rejected or invalid, the projection remains derived without that event.

### 9.2 Why this follows from detect-before-act

ADR-002 established that flagged events do not trigger policies until resolved.

ADR-005 extends the same uncertainty rule to state derivation:

```txt
Do not act on uncertain data.
Do not derive current workflow state from uncertain data.
```

This prevents the projection from showing a tentative state as authoritative.

### 9.3 Visible but not derivational

Timeline visibility and state derivation are separate.

| Concern | Includes flagged event? | Reason |
|---|---:|---|
| Event timeline / audit | Yes | It happened and must remain traceable. |
| Current state derivation | No, until accepted | Current state should reflect validated event sequence. |
| Policy trigger execution | No, until accepted | Detect-before-act prevents amplification. |

---

## 10. Flag Resolvability Classification

### 10.1 Definition

Every flag category has a platform-level resolvability classification:

```txt
flag_resolvability ∈ { auto_eligible, manual_only }
```

This classification is platform-level, not deployer-configurable.

### 10.2 Semantics

| Classification | Meaning |
|---|---|
| `auto_eligible` | A flag may be resolved automatically if a bounded platform policy applies and its conditions are met. |
| `manual_only` | A human resolver is required; auto-resolution policy cannot target this flag type. |

### 10.3 Boundary

Deployer configuration may parameterize allowed auto-resolution policies for `auto_eligible` categories.

Deployer configuration may not reclassify a `manual_only` flag as auto-resolvable.

This prevents the configuration layer from bypassing human judgment for identity, authority, or conflicting-state cases that require contextual review.

### 10.4 Recovered initial classification

| Flag category | Resolvability |
|---|---|
| `transition_violation` | `auto_eligible` |
| `stale_reference` | `auto_eligible` |
| `identity_conflict` | `manual_only` |
| `concurrent_state_change` | `manual_only` |
| `scope_violation` | `manual_only` |
| `domain_uniqueness_violation` | `manual_only` |

ADR-003-specific informational/blocking defaults remain severity policy; resolvability is a separate classification.

---

## 11. Source-Only Flagging

### 11.1 Settled rule

Only the root-cause event is flagged.

Downstream events are not given derived flags.

Downstream impact is computed by projection through source-chain traversal.

### 11.2 Why propagation was rejected

Flag propagation multiplies one root problem into many queue items.

Example:

```txt
A = flagged source event
B = task_created from A
C = task_completed from B
D = escalation from B
```

Propagation would create flags on A, B, C, and D.

But resolving A is the actual decision. B/C/D are consequences. Additional flags create workload without adding decision value.

### 11.3 Source-chain traversal

Projection can render downstream events with an upstream-warning state:

```txt
D → source_event_ref B → source_event_ref A → A has unresolved flag
```

The warning is computed.

It is not a stored flag on D.

### 11.4 Boundary

| Owns | Does not own |
|---|---|
| Trace `source_event_ref` chains. | Create derived flags for every downstream event. |
| Surface upstream flag status in projections. | Retroactively invalidate downstream events automatically. |
| Clear computed warnings when root flag resolves. | Require resolver to manually resolve every downstream effect. |

---

## 12. `context.*` Expression Scope

### 12.1 Settled rule

ADR-005 adds a bounded form-context expression scope:

```txt
context.*
```

It is:

- pre-resolved when the form opens;
- read-only;
- platform-fixed;
- closed and append-only;
- available in form context;
- not dynamic query access;
- not stored in events.

### 12.2 Seven locked properties

| Property | Meaning | Source |
|---|---|---|
| `context.subject_state` | Current subject-level workflow state. | Projection Engine / Pattern Registry. |
| `context.subject_pattern` | Current pattern identity for the subject/activity context. | Activity config / Pattern Registry. |
| `context.activity_stage` | Current stage of activity such as campaign stage. | Activity config + projection. |
| `context.actor.role` | Actor's current role. | Assignment Resolver. |
| `context.actor.scope_name` | Actor's current scope label/name. | Assignment Resolver / Scope Resolver. |
| `context.days_since_last_event` | Days since last event for the subject. | Local projection. |
| `context.event_count` | Count of events for the subject. | Local projection. |

### 12.3 What `context.*` is not

| Excluded use | Reason |
|---|---|
| Querying other subjects. | Cross-subject query would turn forms into a query engine. |
| Aggregating across subjects. | Reporting/projection concern, not form expression. |
| Accessing arbitrary projection internals. | Couples configuration to implementation. |
| Reading payloads from arbitrary prior events. | Cross-event query; use explicit source refs or projections. |
| Deployer-defined context properties. | Violates platform-fixed vocabulary boundary. |

### 12.4 Relation to ADR-004 expression language

ADR-004 expression language remains operators + field references, zero functions.

ADR-005 adds one more data scope, not new grammar.

```txt
ADR-004 scopes: payload.*, entity.*, event.*
ADR-005 added form scope: context.*
```

Trigger expressions do not get open form context. Auto-resolution and server-side trigger processing use server-known event/flag inputs within L3b boundaries.

---

## 13. Command Validator

### 13.1 Definition

The `Command Validator` is advisory on-device and authoritative only as a server-side flag generator during sync/ingestion.

### 13.2 On-device behavior

On-device validation may warn:

```txt
This case is closed. Adding an interaction may create a transition violation.
```

The actor can still proceed.

The event is written locally.

This preserves offline-first and append-only guarantees.

### 13.3 Server behavior

On sync, the server evaluates the event against the current pattern projection.

If invalid, the server creates/attaches `transition_violation`.

The event is accepted, not rejected.

### 13.4 Boundary

| Owns | Does not own |
|---|---|
| User warning before writing a likely invalid transition. | Blocking event creation offline. |
| Server transition flag generation. | Rejection as workflow enforcement. |
| Pattern-aware validation. | Deployer-authored validator code. |

---

## 14. Auto-Resolution

### 14.1 Definition

Auto-resolution is an L3b subtype.

It watches eligible flags and later conditions, then emits a standard resolution event under a system actor:

```txt
actor_ref = system:auto_resolution/{policy_id}
```

### 14.2 Boundary

| Owns | Does not own |
|---|---|
| Resolving `auto_eligible` flags under bounded platform policies. | Reclassifying manual flags as auto-resolvable. |
| Server-side delayed evaluation similar to deadline-check triggers. | Device-side resolution. |
| Auditable resolution event creation. | Silent mutation of flag status. |
| Platform-defined loop guards. | Arbitrary deployer-authored rule engine. |

### 14.3 Loop-prevention guards

ADR-005 preserves ADR-004 trigger safety with three independent guards:

1. `detect-before-act`: flagged events do not trigger policies.
2. Trigger DAG max path length 2.
3. Input/output separation: auto-resolution watches flags, not trigger outputs.

### 14.4 Relationship to manual resolution

Auto-resolution and manual resolution are mutually exclusive per flag instance at the point of resolution.

A flag is auto-resolved only if:

- the flag type is `auto_eligible`;
- a platform-supported auto-resolution policy applies;
- all policy conditions are met;
- loop guards are satisfied.

Otherwise the flag remains manual.

---

## 15. Envelope Integrity

ADR-005 adds zero event envelope fields.

The final envelope remains:

```txt
id
type
shape_ref
activity_ref
subject_ref
actor_ref
device_id
device_seq
sync_watermark
timestamp
payload
```

### 15.1 Explicit non-fields

| Non-field | Why not in envelope |
|---|---|
| `current_state` | Derived from events and pattern definition. |
| `pattern_ref` | Derivable from `shape_ref` + `activity_ref` + activity config. |
| `context.*` values | Pre-resolved at form open; not event facts. |
| `resolvability` | Platform classification of flag category, not event metadata. |
| `transition_validity` | Computed by Conflict Detector, expressed as flag if violated. |

### 15.2 Type vocabulary integrity

ADR-005 does not add `status_changed`.

The structural type vocabulary remains:

```txt
capture
review
alert
task_created
task_completed
assignment_changed
```

### 15.3 Pattern reference rejection

`pattern_ref` is rejected as an envelope field because it would store a derived configuration relationship.

If `pattern_ref` disagrees with the activity's shape-to-pattern mapping, the system would have two authorities.

ADR-005 keeps the authority in configuration and projection.

---

## 16. Primitive Changes After ADR-005

ADR-005 introduces one new primitive and expands several existing primitives.

| Primitive | ADR-005 effect | Boundary |
|---|---|---|
| Pattern Registry | New primitive. | Provides platform-fixed workflow skeletons and transition rules. |
| Projection Engine | Expanded. | Derives workflow state; excludes flagged events; performs source-chain traversal. |
| Conflict Detector | Expanded. | Evaluates `transition_violation`. |
| Command Validator | Clarified. | Advisory on-device, flag-generator on-server. |
| Assignment Resolver | Expanded. | Resolves pattern participant roles using ADR-003 assignments. |
| Trigger Engine | Expanded. | L3b includes auto-resolution subtype. |
| Expression Evaluator | Expanded. | Form context gains `context.*`. |
| Deploy-Time Validator | Expanded. | Validates pattern composition, role mapping, shape-to-pattern uniqueness, auto-resolution eligibility, and L3b budgets. |

### 16.1 Primitive interaction model

```txt
Pattern Registry
  → Projection Engine
      derives workflow current_state
      excludes unresolved flagged events
      supports source-chain traversal

Pattern Registry
  → Conflict Detector
      supplies transition validity rules
      produces transition_violation flags

Pattern Registry
  → Assignment Resolver
      supplies participant role declarations
      resolves actors through assignment scope containment

Projection Engine
  → Expression Evaluator
      pre-resolves context.* values at form-open

Trigger Engine L3b
  → Auto-resolution
      resolves auto_eligible flags via system actor
```

No runtime primitive writes back into the Pattern Registry.

No new device-side engine is introduced.

---

## 17. Anti-Pattern Guard Recovery

ADR-005 was checked against the ADR-004 anti-pattern catalog.

| Anti-pattern | ADR-005 risk | Guard |
|---|---|---|
| AP-1 Inner Platform Effect | `context.*` or patterns become open programming surfaces. | Both are platform-fixed closed vocabularies. |
| AP-2 Greenspun Drift | Auto-resolution becomes a rules language. | Auto-resolution is structural parameterization, not expressions/functions. |
| AP-3 Configuration Specialist Trap | Pattern selection + shape/role/deadline mapping increases setup complexity. | S00 remains unchanged; complexity scales only with scenario complexity. |
| AP-4 Schema Evolution Trap | Pattern changes orphan state. | Patterns are platform-managed; projections are rebuildable. |
| AP-5 Trigger Escalation Trap | Auto-resolution loops with triggers. | Detect-before-act, max path 2, input/output separation. |
| AP-6 Overlapping Authority Trap | Patterns and Conflict Detector both handle transitions. | Pattern defines valid transitions; Conflict Detector evaluates violations. Definition and enforcement are separate. |

---

## 18. Rejected / Excluded Paths

| Path | Status | Reason |
|---|---|---|
| State machine as enforcement primitive | `[REJECTED]` | Enforcement implies rejection; rejection violates offline-first append-only accept-and-flag contract. |
| Storing `current_state` in events | `[REJECTED]` | Creates second source of truth and breaks projection authority. |
| `status_changed` type | `[REJECTED / no action]` | Existing structural types plus shapes/patterns express state progression. |
| `pattern_ref` envelope field | `[REJECTED]` | Redundant and creates config/envelope authority conflict. |
| Deployer-authored state machines | `[REJECTED]` | Crosses configuration boundary into platform evolution/code. |
| Deployer-defined `context.*` properties | `[REJECTED]` | Would turn form expressions into an unbounded platform/query surface. |
| Dynamic `context.*` queries | `[REJECTED]` | Violates zero-function bounded expression model. |
| Flag propagation to downstream events | `[REJECTED]` | Creates flag multiplication and resolver workload without decision value. |
| Auto-resolution for `manual_only` flags | `[REJECTED]` | Bypasses platform-level human-judgment classification. |
| Exact initial pattern skeletons as settled ADR vocabulary | `[EXCLUDED]` | Pattern architecture is settled; inventory/specification belongs to platform spec and can evolve. |

---

## 19. Deferred / Open Evolution

| Item | Classification | Why it remains outside Phase 4 lock |
|---|---|---|
| Exact Pattern Registry inventory | Platform evolution that does not violate accepted decisions. | ADR-005 locks the registry model, not every pattern shipped forever. |
| New platform patterns | Platform evolution. | Deployers select patterns; they do not author new engines. |
| Pattern migration mechanics | Implementation / platform evolution. | Projections are rebuildable; migration tooling is not event structure. |
| Additional `context.*` properties | Platform evolution that does not violate accepted decisions. | Must remain platform-fixed, pre-resolved, read-only, append-only. |
| Richer workflow visualization | Platform evolution. | Uses existing projections and source-chain traversal. |
| Additional auto-resolution policy templates | Platform evolution within L3b. | Must target `auto_eligible` flags and respect loop guards. |
| Batch/manual resolution UX | Platform evolution that does not violate accepted decisions. | Flag storage and resolvability semantics remain unchanged. |
| Pattern analytics/reporting | Platform evolution. | Derived from projections; not stored in events. |

---

## 20. Terms Locked by ADR-005

| Term | Definition | ADR anchor |
|---|---|---|
| `transition_violation` | Flag category for an event whose implied transition is invalid under the applicable pattern. | S1 |
| `flagged-event exclusion` | Rule that unresolved flagged events are visible but excluded from state machine evaluation. | S2 |
| `auto_eligible` | Flag category may be automatically resolved under bounded platform policy. | S3 |
| `manual_only` | Flag category requires human resolution and cannot be targeted by auto-resolution. | S3 |
| `flag resolvability` | Platform-level classification of flag categories into `auto_eligible` / `manual_only`. | S3 |
| `projection-derived state machine` | Workflow state machine whose state is computed from events and pattern definitions rather than stored or enforced by rejection. | S4 |
| `Command Validator` | Advisory on-device validator and server-side flag-generation boundary for transition validity. | S4 |
| `Pattern Registry` | Platform-fixed registry of workflow skeletons selected and parameterized by deployers. | S5 |
| `pattern` | A platform-defined workflow skeleton with states, transitions, role declarations, and parameterization points. | S5 |
| `participant roles` | Roles declared by a pattern for steps such as submitter, reviewer, receiver, supervisor. | S5 |
| `state machine skeleton` | Platform-defined state/transition structure of a pattern. | S5 |
| `parameterization points` | Slots filled by deployment configuration: shapes, roles, deadlines, levels, scopes. | S5 |
| `subject-level pattern` | Pattern that derives state for a subject in an activity. | S6 |
| `event-level pattern` | Pattern that derives state for a source event or sub-flow. | S6 |
| `composition rules` | Five ADR-005 rules governing how subject-level, event-level, approval, cross-activity, and shape-to-pattern mappings compose. | S6 |
| `source-only flagging` | Rule that only the root-cause event receives a flag. | S7 |
| `source-chain traversal` | Projection capability that walks `source_event_ref` lineage and surfaces upstream flags. | S7 |
| `source_event_ref` | Event reference used to link review/trigger/downstream events to their source. | S7 |
| `context.*` scope | Form-expression scope containing pre-resolved read-only contextual values. | S8 |
| `context.subject_state` | Current projected subject state. | S8 |
| `context.subject_pattern` | Pattern identity in the current subject/activity context. | S8 |
| `context.activity_stage` | Current activity/campaign stage. | S8 |
| `context.actor.role` | Actor role resolved from assignment. | S8 |
| `context.actor.scope_name` | Actor scope label/name resolved from assignment/scope. | S8 |
| `context.days_since_last_event` | Days elapsed since last event in local subject projection. | S8 |
| `context.event_count` | Count of events in local subject projection. | S8 |
| `auto-resolution` | L3b subtype that resolves eligible flags under bounded server-side policy. | S9 |
| `auto_resolution` source type | System actor source type for auto-resolution events. | S9 |
| `system:auto_resolution/{policy_id}` | Actor reference format used by auto-resolution. | S9 |

---

## 21. Output Summary

ADR-005 recovers the workflow/state progression layer without changing the event contract.

The core architectural closure is:

```txt
Workflow state is projection-derived.
Patterns define valid transitions.
The Conflict Detector flags invalid transitions.
Flagged events are visible but excluded from derived state.
Patterns are platform-fixed and selected/parameterized at L0.
Auto-resolution is bounded L3b behavior.
No envelope fields are added.
No structural event type is added.
```

This completes independent recovery for ADR-002 through ADR-005.

Next revised phase: `007-phase5-cross-lineage-vocabulary.md`.
