# ADR-005 State Progression Kernel Staging

Status: Iteration 37 staging

This temporary staging file holds ADR-005 state-progression and workflow lineage kernels. It is not a final atomic document.

## Staged Kernels

## Kernel: ADR-005 Session 1 Scoping Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/19-adr5-session1-scoping.md` is ADR-005 Session 1 scoping and event-storm exploration. It defines ADR-005's workflow/state-progression decision surface, event-storms S04, S08, S11, and S07/S14 through the workflow lens, forms high-confidence positions on Q1 and Q2, and proposes a composition model for later stress testing. It does not make final ADR-005 decisions.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / supersession notice
- `docs/exploration/archive/19-adr5-session1-scoping.md` / opening purpose
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 8. Summary of Session 1 Positions`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-005 Session 1.

Non-goals:

Does not decide final ADR-005 workflow semantics, pattern inventory, flag cascade behavior, `context.*`, auto-resolution, or ADR-settled type vocabulary changes.

Forbidden interpretations:

- Do not treat Session 1 directional leans for Q3-Q6 as ADR-settled closure.
- Do not reopen ADR-004's closed envelope or type vocabulary except through the explicit Q2/status-type question.

Open edges:

Session 2 must stress-test composition rules, flag cascade behavior, `context.*`, auto-resolution, and irreversibility classification.

Platform specification note:

Use as ADR-005 scoping and first workflow-lens evidence.

## Kernel: ADR-005 Decision Surface

Status: Open
Kind: open-question

Specification statement:

ADR-005 must decide how work moves through stages across six questions: whether state machines are platform primitives or projection patterns, whether `status_changed` is needed as a seventh structural event type, how multi-step multi-actor workflow composition works, how workflow interacts with flags and downstream cascades, whether form expressions gain pre-resolved `context.*`, and whether domain conflict resolution can be automated.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 1. What ADR-5 Must Decide`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 1.1 The Decision Surface`

Closure basis:

Open ADR-005 decision surface.

Scope:

Applies to state progression, workflow patterns, structural type growth, flag cascade behavior, expression context, and conflict resolution automation.

Non-goals:

Does not decide any of the six questions by itself.

Forbidden interpretations:

- Do not treat workflow as a generic configuration issue already closed by ADR-004; ADR-004 explicitly handed these questions to ADR-005.
- Do not add envelope fields unless a later ADR-005 source explicitly proves they are required.

Open edges:

Q1 and Q2 receive high-confidence Session 1 positions; Q3-Q6 remain for stress testing and ADR-005 verification.

Platform specification note:

Use as ADR-005 scope checklist.

## Kernel: ADR-005 Irreversibility Prediction

Status: Candidate
Kind: conditional-validity-rule

Specification statement:

ADR-005 Session 1 predicts that the event envelope remains closed at eleven fields and that the only possible irreversible surface is adding `status_changed` to the append-only structural type vocabulary. Other ADR-005 concerns live in projection logic, server-side processing, pattern definitions, or configuration semantics and are therefore strategy-level unless later stress testing proves otherwise.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 1.2 Irreversibility Prediction`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 8. Summary of Session 1 Positions`

Closure basis:

Candidate irreversibility prediction pending Session 2 filter and ADR-005 verification.

Scope:

Applies to envelope stability, structural type growth, state representation, pattern configuration, flag cascade rules, `context.*`, and auto-resolution.

Non-goals:

Does not decide that `status_changed` is added.

Forbidden interpretations:

- Do not treat Q2 as a reason to add fields to the event envelope.
- Do not classify workflow configuration as structural merely because workflow is important.

Open edges:

Session 2 must apply the irreversibility filter to all Session 1 positions.

Platform specification note:

Use as ADR-005 stress-depth guidance.

## Kernel: Projection-Derived Workflow State Candidate

Status: Conditional
Kind: algorithm

Specification statement:

ADR-005 Session 1 resolves Q1 directionally: workflow state should be derived as a projection over immutable events and configured pattern definitions, not enforced by rejecting invalid transitions. Events representing stale or invalid transitions are accepted and flagged rather than rejected, preserving ADR-001 append-only storage and ADR-002 accept-and-flag behavior.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / S04 walkthrough
- `docs/exploration/archive/19-adr5-session1-scoping.md` / S08 walkthrough
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 3.3 Resolving Q1: Primitive or Pattern?`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 8. Summary of Session 1 Positions`

Closure basis:

Conditional high-confidence Session 1 position pending Session 2 stress testing and ADR-005 verification.

Scope:

Applies to supervisor review, case management, multi-step approvals, resource distribution, state derivation, transition validation, stale offline work, and invalid transition handling.

Non-goals:

Does not define final pattern syntax or all workflow patterns.

Forbidden interpretations:

- Do not reject offline events solely because they violate current derived workflow state.
- Do not store a redundant state field in the event envelope under this candidate.
- Do not treat state as unknowable merely because it is not stored; it is computed from events and pattern definitions.

Open edges:

Session 2 must stress pattern composition and flag cascade behavior before ADR-005 final closure.

Platform specification note:

Use as the current ADR-005 Q1 candidate.

## Kernel: Status Changed Type Rejection Candidate

Status: Conditional
Kind: rejected-alternative

Specification statement:

ADR-005 Session 1 concludes that `status_changed` is not needed as a seventh structural event type. State-changing versus state-preserving behavior is expressed through shape and pattern definition, while the existing structural event pipeline still validates payload, links subject, updates projections, evaluates triggers, and checks flags. A new type would be justified only by different platform processing behavior, which Session 1 did not find.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 4. Resolving Q2: status_changed as 7th Type`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 8. Summary of Session 1 Positions`

Closure basis:

Conditional rejected-alternative lineage pending Session 2 stress testing and ADR-005 verification.

Scope:

Applies to structural event vocabulary, case state changes, approval decisions, shipment state, and future type-growth criteria.

Non-goals:

Does not forbid future append-only type additions if later scenarios prove a different processing behavior.

Forbidden interpretations:

- Do not add `status_changed` merely to make workflow state explicit.
- Do not encode domain lifecycle labels in event `type` when shape and pattern carry that meaning.

Open edges:

Session 2 and ADR-005 must verify that no stress case requires different platform processing.

Platform specification note:

Use as Q2 rejection lineage unless later ADR-005 sources reverse it.

## Kernel: Pattern Registry Candidate

Status: Conditional
Kind: primitive

Specification statement:

ADR-005 Session 1 identifies a Pattern Registry candidate. The platform provides fixed workflow pattern skeletons such as `capture_with_review`, `case_management`, `multi_step_approval`, and `transfer_with_acknowledgment`. Deployers select and parameterize patterns with shapes, roles, levels, deadlines, and scope; they do not author state machines from scratch. Pattern definitions provide state derivation rules for projections, transition validity rules for flagging, and role-per-step declarations for assignment resolution.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 5.1 Pattern-Provided State Machines`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 5.2 Deployer Parameterization`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 5.3 What This Means for the Primitives Map`

Closure basis:

Conditional new primitive candidate pending Session 2 stress testing and ADR-005 verification.

Scope:

Applies to workflow pattern inventory, state machine skeletons, pattern parameterization, projection engine, conflict detector, assignment resolver, and device-synced configuration packages.

Non-goals:

Does not decide final pattern inventory, pattern syntax, or whether patterns can compose independently or nest.

Forbidden interpretations:

- Do not let deployers author arbitrary workflow engines under this candidate.
- Do not confuse platform-fixed pattern skeletons with domain-specific shape content.

Open edges:

Session 2 must stress pattern composition and whether patterns remain independent per subject/activity combination.

Platform specification note:

Use as the emerging ADR-005 workflow primitive candidate.

## Kernel: Transition Violation Flag Candidate

Status: Conditional
Kind: interaction-rule

Specification statement:

ADR-005 Session 1 introduces `transition_violation` as a new flag category candidate. When an accepted event violates the configured workflow transition rules for the subject's derived state, the event is stored and flagged rather than rejected. This extends ADR-002 accept-and-flag behavior into workflow state progression.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / S08 stress points
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 3.2 The Critical Test`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / primitives map update

Closure basis:

Conditional Session 1 candidate pending stress testing and ADR-005 verification.

Scope:

Applies to invalid workflow transitions, stale offline work, post-resolution interactions, skip-level approvals, receipt-before-dispatch cases, and detect-before-act behavior.

Non-goals:

Does not decide downstream flag cascade propagation or automated resolution.

Forbidden interpretations:

- Do not reject the underlying event merely because a transition violation flag is created.
- Do not treat transition violations as structural type errors.

Open edges:

Session 2 must test source-only flagging versus propagation and auto-resolution behavior.

Platform specification note:

Use as workflow-conflict extension lineage.

## Kernel: Workflow Composition Directional Lean

Status: Open
Kind: open-question

Specification statement:

ADR-005 Session 1 leans toward independent pattern composition: each pattern tracks its own state independently per subject/activity combination, allowing one subject to participate in multiple patterns at the same time. Whether patterns can nest or constrain each other remains open for Session 2.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 6.1 Q3 — Composition Rules`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / Session 2 charter

Closure basis:

Open directional lean pending Session 2 stress testing.

Scope:

Applies to multi-pattern subjects, case management plus approval, nested transfer workflows, pattern scoping, and projection presentation.

Non-goals:

Does not decide final composition semantics.

Forbidden interpretations:

- Do not treat independent composition as settled before stress testing.
- Do not assume nested patterns are allowed.

Open edges:

Session 2 must walk concrete multi-pattern scenarios.

Platform specification note:

Use as Q3 handoff to Session 2.

## Kernel: Workflow Flag Cascade Directional Lean

Status: Open
Kind: open-question

Specification statement:

ADR-005 Session 1 leans toward source-only flagging for downstream workflow cascades. If an upstream event is flagged after downstream events have fired, only the source event receives the flag; downstream projections compute derived-from-flagged status from source-chain traceability rather than multiplying flags across every downstream event.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 6.2 Q4 — Flag Cascade Behavior`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 8. Summary of Session 1 Positions`

Closure basis:

Open directional lean pending Session 2 stress testing.

Scope:

Applies to flagged upstream events, trigger-created tasks, downstream completions, detect-before-act, projection traceability, and flag queues.

Non-goals:

Does not decide final cascade behavior or flag propagation policy.

Forbidden interpretations:

- Do not create downstream flags for every derived event unless later stress testing justifies flag propagation.
- Do not hide downstream dependency on flagged roots; projections must remain traceable.

Open edges:

Session 2 must test whether source-only flagging stalls or obscures workflow risk.

Platform specification note:

Use as Q4 handoff to Session 2.

## Kernel: Context Scope Directional Lean

Status: Open
Kind: open-question

Specification statement:

ADR-005 Session 1 leans toward adding `context.*` as a pre-resolved form-expression scope populated from local projection at form-open time. Candidate properties include subject state, assigned facility attributes, and activity stage. The scope remains read-only and pre-resolved, not a dynamic query facility.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 6.3 Q5 — context.* Expression Scope`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 8. Summary of Session 1 Positions`

Closure basis:

Open directional lean pending Session 2 property specification and ADR-005 verification.

Scope:

Applies to L2 form expressions, workflow-aware form logic, local projections, subject state, actor context, and activity stage.

Non-goals:

Does not allow dynamic joins, arbitrary entity lookup, aggregation, or query execution from expressions.

Forbidden interpretations:

- Do not treat `context.*` as reopening ADR-004's expression language architecture.
- Do not expose arbitrary projection data without a bounded property set.

Open edges:

Session 2 must define available properties and validate on-device pre-resolution.

Platform specification note:

Use as Q5 handoff to Session 2.

## Kernel: Auto-Resolution Directional Lean

Status: Open
Kind: open-question

Specification statement:

ADR-005 Session 1 leans toward treating auto-resolution as an L3b deadline-policy subtype. Auto-resolution rules would watch flag states over time and create explicit resolution events when configured conditions are met, using the same server-side delayed evaluation mechanism as deadline checks.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `### 6.4 Q6 — Auto-Resolution and State Machine Integration`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 8. Summary of Session 1 Positions`

Closure basis:

Open directional lean pending Session 2 mechanism validation and ADR-005 verification.

Scope:

Applies to domain conflict resolution automation, transition violation flags, deadline policy, server-side delayed evaluation, and resolution events.

Non-goals:

Does not decide that deployers can auto-resolve all flag types or bypass ADR-002 single-writer semantics.

Forbidden interpretations:

- Do not silently resolve flags without explicit resolution events.
- Do not run auto-resolution on device under this lean.

Open edges:

Session 2 must validate whether L3b can support auto-resolution without new infrastructure.

Platform specification note:

Use as Q6 handoff to Session 2.

## Kernel: ADR-005 Session 2 Charter

Status: Open
Kind: open-question

Specification statement:

ADR-005 Session 2 must stress-test pattern composition rules, source-only flag cascade behavior, `context.*` scope property specification, auto-resolution as an L3b subtype, and the irreversibility classification of all Session 1 positions. Expected outcome is zero Tier 1 envelope items and mostly strategy-level positions.

Source basis:

- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 7. Session 2 Charter`
- `docs/exploration/archive/19-adr5-session1-scoping.md` / `## 8. Summary of Session 1 Positions`

Closure basis:

Open handoff to ADR-005 Session 2.

Scope:

Applies to next-source processing and ADR-005 final verification.

Non-goals:

Does not perform the Session 2 stress tests.

Forbidden interpretations:

- Do not advance to ADR-005 final closure without stress-testing Q3-Q6.

Open edges:

Next source must stress-test the composition model and classify permanence.

Platform specification note:

Use as the immediate handoff to `docs/exploration/archive/20-adr5-session2-stress-test.md`.

## Kernel: ADR-005 Session 2 Stress-Test Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/20-adr5-session2-stress-test.md` is ADR-005 Session 2 stress-test and irreversibility-filter evidence. It tests Session 1's Q3-Q6 directional leans, raises all six ADR-005 questions to high confidence, classifies ADR-005 permanence, and narrows Session 3 to coherence audit plus ADR writing. It does not make final ADR-005 decisions.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / supersession notice
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / opening purpose
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 7. Session 2 Summary`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-005 Session 2.

Non-goals:

Does not replace ADR-005 final decision text and does not freeze exact workflow pattern inventory.

Forbidden interpretations:

- Do not treat Session 2 `initial strategy` classifications as ADR-settled architecture.
- Do not treat the expected artifact path in Session 2 prose as changing the archive scan cursor when the archived Session 3 file exists under `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md`.

Open edges:

Session 3 must audit structural coherence before ADR-005 final extraction.

Platform specification note:

Use as high-confidence stress-test lineage before ADR-005 closure.

## Kernel: ADR-005 Composition Rule Candidate

Status: Conditional
Kind: interaction-rule

Specification statement:

ADR-005 Session 2 refines workflow composition into five rules: each activity has at most one subject-level pattern for a subject; event-level patterns such as review and approval compose freely; subject-level patterns may embed approval sub-flows scoped to specific submission events; cross-activity linking uses `activity_ref` or payload references rather than shared patterns; and each shape-to-pattern mapping is unique within an activity.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 1.3 Composition Rules -- Resolution`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 1.4 Q3 Resolution`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 7. Session 2 Summary`

Closure basis:

Conditional high-confidence Session 2 position pending Session 3 coherence audit and ADR-005 verification.

Scope:

Applies to subject-level state, event-level state, embedded approval flows, activity boundaries, shape mapping, deploy-time validation, and projection state keys.

Non-goals:

Does not define exact pattern schemas or all initial pattern skeletons.

Forbidden interpretations:

- Do not allow two competing subject-level lifecycle state machines for the same subject within one activity.
- Do not treat cross-activity workflow as one shared pattern spanning activities.
- Do not let two patterns in the same activity claim the same shape.

Open edges:

Session 3 must verify these rules compose with ADR-001 through ADR-004 and with the full ADR-005 position set.

Platform specification note:

Use as the refined Q3 candidate replacing the broader Session 1 independent-composition lean.

## Kernel: Source-Only Flag Cascade Candidate

Status: Conditional
Kind: algorithm

Specification statement:

ADR-005 Session 2 confirms source-only flagging for workflow cascades. When an upstream event is flagged after downstream events already exist, only the root-cause event receives the flag. Downstream events remain unflagged, while projections compute and display their dependency on a flagged source through source-chain traversal.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 2. Q4 -- Flag Cascade Behavior`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 2.4 Edge Case: Double-Retroactive Flagging`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 2.5 Q4 Resolution`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 7. Session 2 Summary`

Closure basis:

Conditional high-confidence Session 2 position pending Session 3 coherence audit and ADR-005 verification.

Scope:

Applies to root-cause flags, downstream trigger-created events, retroactive flag discovery, projection rendering, source-chain traversal, and resolution effects.

Non-goals:

Does not define UI copy or final projection schema.

Forbidden interpretations:

- Do not multiply flags across downstream events merely because they derive from a flagged source.
- Do not hide downstream dependence on flagged roots; the projection must surface source-chain flag state.
- Do not retroactively invalidate trigger outputs that were valid when created.

Open edges:

ADR-005 must verify whether source-chain traversal is carried as projection capability or specification detail.

Platform specification note:

Use as the refined Q4 candidate replacing the Session 1 source-only directional lean.

## Kernel: ADR-005 Pre-Resolved Context Scope Candidate

Status: Conditional
Kind: contract

Specification statement:

ADR-005 Session 2 refines `context.*` into a pre-resolved, read-only, platform-fixed form-expression scope. Initial properties are `context.subject_state`, `context.subject_pattern`, `context.activity_stage`, `context.actor.role`, `context.actor.scope_name`, `context.days_since_last_event`, and `context.event_count`. Values are resolved from local projection, configuration, and assignment data when the form opens and remain static during form fill.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 3. Q5 -- context.* Expression Scope`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 3.3 What Properties Should Be Available?`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 3.6 Q5 Resolution`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 7. Session 2 Summary`

Closure basis:

Conditional high-confidence Session 2 position pending Session 3 coherence audit and ADR-005 verification.

Scope:

Applies to L2 form expressions, form-open context resolution, workflow-aware forms, actor context, local subject projections, and platform-fixed expression namespaces.

Non-goals:

Does not expose dynamic joins, other subjects' state, aggregate values, arbitrary projection fields, payload fields from other events, or trigger-expression context.

Forbidden interpretations:

- Do not let deployers define arbitrary `context.*` properties.
- Do not evaluate `context.*` through live queries during form fill.
- Do not make `context.*` available to server-side trigger expressions under this candidate.

Open edges:

ADR-005 must verify the property list and governance model before final closure.

Platform specification note:

Use as the refined Q5 candidate and as the ADR-005-owned successor to the ADR-004 `context.*` deferral.

## Kernel: Auto-Resolution L3b Candidate

Status: Conditional
Kind: interaction-rule

Specification statement:

ADR-005 Session 2 confirms auto-resolution as an L3b deadline-policy subtype. Auto-resolution policies watch eligible flags and later domain events over a configured time window, then create explicit auditable resolution events or escalate/manual-review outcomes. The mechanism is server-side and reuses deadline-check infrastructure.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 4. Q6 -- Auto-Resolution and State Machine Integration`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 4.2 Scenario Walk-through: Transition Violation Auto-Resolution`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 4.5 Q6 Resolution`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 7. Session 2 Summary`

Closure basis:

Conditional high-confidence Session 2 position pending Session 3 coherence audit and ADR-005 verification.

Scope:

Applies to domain conflict resolution automation, transition violation resolution, server-side delayed evaluation, deadline windows, watched events, resolution events, and escalation/manual-review fallbacks.

Non-goals:

Does not allow device-side auto-resolution or silent flag dismissal.

Forbidden interpretations:

- Do not auto-resolve without creating explicit resolution events.
- Do not bypass detect-before-act or trigger depth limits.
- Do not let deployers auto-resolve flag types that the platform marks manual-only.

Open edges:

ADR-005 must verify how resolution events are represented and how this interacts with final flag semantics.

Platform specification note:

Use as the refined Q6 candidate and ADR-005-owned closure candidate for ADR-004's domain-conflict-resolution automation deferral.

## Kernel: Flag Resolvability Classification Candidate

Status: Conditional
Kind: configuration-boundary

Specification statement:

ADR-005 Session 2 introduces platform-defined flag resolvability classifications. `auto_eligible` flag types may have deployer-configured auto-resolution policies; `manual_only` flag types reject auto-resolution at deploy time. Initial classifications mark `transition_violation` and `stale_reference` as auto-eligible, while `scope_violation`, `identity_conflict`, and `concurrent_state_change` remain manual-only.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 4.4 Adversarial Test: Auto-Resolution Overreach`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 4.5 Q6 Resolution`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 5.1 Classification`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### New artifacts from Session 2`

Closure basis:

Conditional high-confidence Session 2 position pending Session 3 coherence audit and ADR-005 verification.

Scope:

Applies to platform flag vocabulary governance, deploy-time validation of auto-resolution policies, security-relevant flag handling, and initial flag classification.

Non-goals:

Does not settle the final complete flag taxonomy or ADR-006 flag semantics.

Forbidden interpretations:

- Do not let deployers change a flag type from manual-only to auto-eligible.
- Do not auto-resolve scope, identity, or concurrent-state flags under the initial classification.
- Do not treat resolvability as stored event data under this candidate.

Open edges:

Later ADR-005 and ADR-006 extraction must verify whether this classification is carried forward, refined, or superseded.

Platform specification note:

Use as the auto-resolution guardrail candidate.

## Kernel: ADR-005 Irreversibility Classification Candidate

Status: Conditional
Kind: conditional-validity-rule

Specification statement:

ADR-005 Session 2 classifies ADR-005 as having no Tier 1 envelope-touching positions, one mild Tier 2 strategy-protecting item in the `transition_violation` flag category, and all other state-progression positions as Tier 3 strategies. The event envelope remains unchanged and the structural event type vocabulary remains at six types.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 5. Irreversibility Filter`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 5.1 Classification`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 5.2 Irreversibility Summary`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 7. Session 2 Summary`

Closure basis:

Conditional Session 2 irreversibility classification pending Session 3 coherence audit and ADR-005 verification.

Scope:

Applies to envelope stability, structural type vocabulary, pattern registry, workflow projection strategy, flag cascade behavior, `context.*`, auto-resolution, `transition_violation`, and flag resolvability classification.

Non-goals:

Does not make final ADR closure and does not prove future ADRs cannot add types or flag categories.

Forbidden interpretations:

- Do not add envelope fields from ADR-005 Session 2.
- Do not add `status_changed` from ADR-005 Session 2.
- Do not inflate projection/configuration/server-side strategy decisions into stored-event constraints.

Open edges:

Session 3 must validate structural coherence before ADR-005 final extraction.

Platform specification note:

Use as ADR-005 permanence guidance.

## Kernel: Pattern Inventory Scope Boundary Candidate

Status: Conditional
Kind: configuration-boundary

Specification statement:

ADR-005 Session 2 separates pattern architecture from pattern inventory. ADR-005 owns the Pattern Registry model, composition rules, state-as-projection, and interactions with flags and expressions. Exact initial pattern skeletons and formal pattern schemas belong to platform specification or implementation documentation as initial strategies and examples, not frozen ADR commitments.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 6.1 The Pattern Inventory -- Scope Decision`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### New artifacts from Session 2`

Closure basis:

Conditional Session 2 scope boundary pending Session 3 coherence audit and ADR-005 verification.

Scope:

Applies to Pattern Registry architecture, pattern examples, initial pattern inventory, formal schemas, and rest-state platform-spec consolidation.

Non-goals:

Does not decide which exact pattern skeletons ship initially.

Forbidden interpretations:

- Do not freeze example workflow skeletons as final ADR commitments.
- Do not move the Pattern Registry architectural model out of ADR-005 scope.

Open edges:

ADR-005 must verify this ADR-versus-specification boundary.

Platform specification note:

Use to avoid conflating architectural closure with inventory documentation.

## Kernel: Advisory Command Validator Candidate

Status: Conditional
Kind: interaction-rule

Specification statement:

ADR-005 Session 2 clarifies the Command Validator for workflow transitions: on device it validates against local projection and configured patterns to warn users but does not block event creation; on sync, the server validates the same transition rules and generates `transition_violation` flags where appropriate. The validator is advisory on device and a flag generator on server, never a rejection gate for stale offline work.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 6.2 Command Validator Revisited`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 7. Session 2 Summary`

Closure basis:

Conditional Session 2 clarification pending Session 3 coherence audit and ADR-005 verification.

Scope:

Applies to offline command validation, workflow transition warnings, sync-time validation, server flag generation, and append-only event acceptance.

Non-goals:

Does not define final validator UX or warning override interface.

Forbidden interpretations:

- Do not reject event creation because local projection says a transition is invalid.
- Do not treat the Command Validator as a hard state-machine enforcement primitive.
- Do not skip server-side validation merely because the device warned or did not warn.

Open edges:

ADR-005 must verify the validator role with final workflow state and flag semantics.

Platform specification note:

Use as workflow validation lineage tied to projection-derived state.

## Kernel: ADR-005 Session 3 Charter

Status: Open
Kind: open-question

Specification statement:

ADR-005 Session 3 must perform structural coherence audit across ADR-005 Session 1 and Session 2 positions and ADR-001 through ADR-004, then support final ADR-005 writing. Session 2 narrows Session 3 away from full adversarial stress testing because ADR-005 has zero Tier 1 envelope items and one mild Tier 2 item.

Source basis:

- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `### 5.3 Stress Test Scope`
- `docs/exploration/archive/20-adr5-session2-stress-test.md` / `## 8. Session 3 Charter`

Closure basis:

Open handoff to ADR-005 Session 3 and final ADR-005 verification.

Scope:

Applies to next-source processing, structural coherence checks, and ADR-005 final extraction.

Non-goals:

Does not perform the coherence audit or final ADR extraction.

Forbidden interpretations:

- Do not skip Session 3 coherence merely because Session 2 raised confidence to HIGH.
- Do not treat Session 2 as final ADR-005 closure.

Open edges:

Next source is `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md`.

Platform specification note:

Use as the immediate handoff to ADR-005 Session 3.

## Kernel: ADR-005 Session 3 Coherence Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` is ADR-005 Session 3 structural-coherence audit evidence. It verifies ADR-005 positions against ADR-001 through ADR-004, checks internal consistency, confirms envelope integrity and anti-pattern containment, and passes the gate for ADR-005 writing. It does not replace final ADR-005 decision text.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / supersession notice
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / opening purpose
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Overall Verdict`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Gate Decision`

Closure basis:

Settled as an extraction boundary.

Scope:

Applies to all kernels extracted from ADR-005 Session 3 structural coherence.

Non-goals:

Does not promote ADR-005 exploration positions into ADR-settled contracts before processing `docs/adrs/adr-005-state-progression.md`.

Forbidden interpretations:

- Do not treat the audit primitive map's `Settled` labels as final platform closure before ADR-005 extraction.
- Do not collapse flag category, flag creation, source-chain projection, flag exclusion from state derivation, and flag resolution into one undifferentiated flag mechanism.

Open edges:

ADR-005 must confirm, revise, or reject the coherence audit's carry-forward clarifications and primitive map.

Platform specification note:

Use as the final pre-ADR coherence gate.

## Kernel: ADR-005 Structural Coherence Audit Result

Status: Conditional
Kind: conditional-validity-rule

Specification statement:

ADR-005 Session 3 audits nine checks and finds that all ADR-005 positions compose with ADR-001 through ADR-004 and with each other. State-as-projection composes with append-only events; `transition_violation`, source-only flagging, and detect-before-act compose with ADR-002; Pattern Registry roles compose with assignment-based access; patterns, `context.*`, and auto-resolution fit ADR-004's gradient and trigger architecture; internal dependencies are acyclic; primitives interact without runtime cycles; the event envelope remains unchanged; anti-patterns are contained; and all seven principles hold.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Method`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / checks `(a)` through `(i)`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Overall Verdict`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Gate Decision`

Closure basis:

Conditional coherence-audit result pending ADR-005 verification.

Scope:

Applies to ADR-005 integration with prior ADRs, internal consistency, primitive interactions, envelope stability, anti-pattern containment, principle alignment, and readiness for final ADR extraction.

Non-goals:

Does not itself settle ADR-005 sub-decisions.

Forbidden interpretations:

- Do not skip ADR-005 final extraction because the coherence audit passes.
- Do not treat absence of structural contradiction as proof that every implementation detail is decided.

Open edges:

ADR-005 must provide the final decision source.

Platform specification note:

Use as the ADR-005 pre-ADR reconciliation checkpoint.

## Kernel: Flagged Events State-Derivation Clarification

Status: Conditional
Kind: algorithm

Specification statement:

ADR-005 Session 3 clarifies that events carrying unresolved flags are excluded from state machine evaluation in the projection engine. They remain visible in the event timeline but do not change `current_state`. If a flag is resolved as accepted, projections re-derive state including the event; if resolved as rejected or invalid, state derivation continues to exclude it.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Check (b): ADR-2 Integration -- Accept-and-Flag, Detect-Before-Act`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Clarifications to Carry into ADR-005`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Primitives Map -- Final State After ADR-5`

Closure basis:

Conditional coherence-audit clarification pending ADR-005 verification.

Scope:

Applies to unresolved flags, workflow state derivation, projection replay, current-state computation, flag resolution effects, and event timeline visibility.

Non-goals:

Does not decide final ADR-006 flag semantics, severity, queues, lifecycle states, or UI presentation.

Forbidden interpretations:

- Do not hide flagged events from timelines.
- Do not let unresolved flagged events advance workflow state.
- Do not treat this as changing event acceptance; flagged events remain stored.
- Do not conflate exclusion from state derivation with exclusion from all projections or all reports.

Open edges:

ADR-005 must confirm this clarification, and later flag-semantics extraction must preserve or refine its boundary without conflating it with unrelated flag semantics.

Platform specification note:

Use as the careful boundary between accept-and-flag storage, detect-before-act policy execution, and workflow state projection.

## Kernel: Auto-Resolution System Actor Format Candidate

Status: Conditional
Kind: contract

Specification statement:

ADR-005 Session 3 clarifies that auto-resolution events use `actor_ref` format `system:auto_resolution/{policy_id}`. This extends ADR-004's `system:{source_type}/{source_id}` system actor convention by adding `auto_resolution` as a source type alongside `trigger`.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Clarifications to Carry into ADR-005`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Check (i): Principle Alignment`

Closure basis:

Conditional coherence-audit clarification pending ADR-005 verification.

Scope:

Applies to system-authored auto-resolution events, actor attribution, auditability, policy identity, and ADR-004 system actor convention extension.

Non-goals:

Does not define all system actor source types or all flag resolution payload fields.

Forbidden interpretations:

- Do not use a null actor for auto-resolution events.
- Do not hide the auto-resolution policy identity.
- Do not treat auto-resolution as unaudited background mutation.

Open edges:

ADR-005 must verify the actor format and final event representation.

Platform specification note:

Use as ADR-005 lineage for auditable auto-resolution.

## Kernel: ADR-005 Envelope Integrity Confirmation Candidate

Status: Conditional
Kind: invariant

Specification statement:

ADR-005 Session 3 confirms that no ADR-005 position requires a new event envelope field. Workflow state is derived rather than stored; `status_changed` is not added; pattern assignment is derived from `shape_ref`, `activity_ref`, and configuration rather than a `pattern_ref`; source-chain traversal uses payload references; `context.*` is form-open computation; and auto-resolution uses standard events and existing `actor_ref`.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Check (g): Envelope Integrity -- Zero Changes Confirmed`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `### The pattern_ref question`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Overall Verdict`

Closure basis:

Conditional coherence-audit confirmation pending ADR-005 verification.

Scope:

Applies to state storage, structural event type vocabulary, pattern references, source-chain traversal, `context.*`, auto-resolution, and event envelope stability.

Non-goals:

Does not prevent future platform evolution from adding fields through a later approved decision.

Forbidden interpretations:

- Do not add `pattern_ref` to the envelope for ADR-005.
- Do not store `current_state` in the envelope or payload as an authoritative workflow state.
- Do not add `status_changed` from ADR-005 coherence evidence.

Open edges:

ADR-005 must settle whether this confirmation becomes final ADR closure.

Platform specification note:

Use as the strongest ADR-005 envelope-stability lineage.

## Kernel: ADR-005 Configuration Gradient Integration Candidate

Status: Conditional
Kind: configuration-boundary

Specification statement:

ADR-005 Session 3 maps workflow features into ADR-004's configuration gradient without adding layers: pattern skeletons are platform-fixed below L0; pattern selection, shape mapping, and role mapping are L0 assembly; state-aware form logic uses L2 `context.*`; workflow deadlines and auto-resolution are L3b; transition validation is server-side conflict detection; and new configuration checks remain deploy-time validations.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Check (d): ADR-4 Integration -- Four-Layer Gradient, Type Vocabulary, Expression Language`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / deploy-time validation table
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Overall Verdict`

Closure basis:

Conditional coherence-audit integration finding pending ADR-005 verification.

Scope:

Applies to Pattern Registry placement, L0 assembly, L2 form expressions, L3b policies, conflict detection, trigger budgets, and deploy-time validation.

Non-goals:

Does not define exact authoring UI or full pattern schema syntax.

Forbidden interpretations:

- Do not create a new configuration layer for workflow.
- Do not let deployers author platform pattern skeletons.
- Do not make `context.*` available to trigger expressions.
- Do not exclude auto-resolution policies from L3b budget checks.

Open edges:

ADR-005 must verify the final boundary and later rest-state cleanup must reconcile it with ADR-004 configuration kernels.

Platform specification note:

Use as ADR-005-to-ADR-004 boundary evidence.

## Kernel: ADR-005 Primitive Interaction Map Candidate

Status: Conditional
Kind: contract

Specification statement:

ADR-005 Session 3 describes a primitive interaction map in which the Pattern Registry supplies state-machine definitions to the Projection Engine, transition rules to the Conflict Detector, role declarations to the Assignment Resolver, and configuration constraints to deploy-time validation. The Projection Engine supplies pre-resolved `context.*` values to the Expression Evaluator. The Conflict Detector supplies flag status that affects projection state derivation. The L3b Trigger Engine creates resolution events that resolve flags. The audit finds these relationships acyclic and incremental.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Check (f): Primitives Composition -- New and Expanded Primitives`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Primitives Map -- Final State After ADR-5`

Closure basis:

Conditional primitive-composition finding pending ADR-005 verification.

Scope:

Applies to Pattern Registry, Projection Engine, Conflict Detector, Assignment Resolver, Deploy-Time Validator, Expression Evaluator, and Trigger Engine interactions.

Non-goals:

Does not settle final primitive inventory before ADR-005.

Forbidden interpretations:

- Do not introduce runtime feedback loops among pattern, projection, conflict, expression, and trigger primitives.
- Do not add a new device-side engine for ADR-005 workflow.
- Do not move server-side flag generation onto the device under this candidate.

Open edges:

ADR-005 must confirm the primitive map and final primitive statuses.

Platform specification note:

Use as pre-ADR primitive reconciliation evidence.

## Kernel: ADR-005 Anti-Pattern Containment Candidate

Status: Conditional
Kind: conditional-validity-rule

Specification statement:

ADR-005 Session 3 finds ADR-005 anti-pattern risks contained: platform-fixed `context.*` and pattern vocabularies contain AP-1; structural auto-resolution parameters contain AP-2; AP-3 is monitored but the S00 simplicity floor remains; platform-managed pattern evolution contains AP-4; detect-before-act, depth limits, and input/output separation contain AP-5; and definition-versus-enforcement separation plus mutually exclusive manual/auto resolution contain AP-6.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Check (h): Anti-Pattern Check -- AP-1 Through AP-6`
- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Overall Verdict`

Closure basis:

Conditional coherence-audit finding pending ADR-005 verification.

Scope:

Applies to configuration-language boundaries, pattern governance, auto-resolution configuration, schema/pattern evolution, trigger cascade control, and authority separation.

Non-goals:

Does not remove the need to monitor configuration complexity after implementation.

Forbidden interpretations:

- Do not treat auto-resolution as a general rule language.
- Do not treat pattern transition definitions and conflict detection as overlapping authorities; one defines validity, the other evaluates events.
- Do not allow one flag instance to be both auto-resolved and manually resolved.

Open edges:

ADR-005 must verify which anti-pattern guards become final decision text or accepted risk.

Platform specification note:

Use as ADR-005 guardrail evidence.

## Kernel: ADR-005 Writing Gate

Status: Open
Kind: open-question

Specification statement:

After ADR-005 Session 3, the next extraction source is the final ADR-005 decision document. The coherence audit passes, no structural revision is required, and ADR-005 writing may proceed.

Source basis:

- `docs/exploration/archive/21-adr5-session3-part1-structural-coherence.md` / `## Gate Decision`

Closure basis:

Open handoff to `docs/adrs/adr-005-state-progression.md`.

Scope:

Applies to immediate next-source processing and final ADR-005 verification.

Non-goals:

Does not itself extract the ADR.

Forbidden interpretations:

- Do not continue to ADR-006 sources before processing ADR-005.
- Do not treat the gate as equivalent to ADR-settled closure.

Open edges:

Next source is `docs/adrs/adr-005-state-progression.md`.

Platform specification note:

Use as the handoff from archive exploration into ADR-005 final decision extraction.

## Kernel: ADR-005 Decision Boundary

Status: Settled
Kind: forbidden-interpretation

Specification statement:

`docs/adrs/adr-005-state-progression.md` is the decided ADR-005 source for state progression and workflow. It closes ADR-004's six deferred state-progression questions by committing projection-derived state machines, platform-fixed workflow patterns, source-only workflow flag cascade behavior, bounded form-context `context.*`, and L3b auto-resolution. ADR-005 does not decide broad ADR-006 flag semantics or implementation inventory details.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / status and context
- `docs/adrs/adr-005-state-progression.md` / `## Decision`
- `docs/adrs/adr-005-state-progression.md` / `## What This Does NOT Decide`

Closure basis:

ADR-settled extraction boundary.

Scope:

Applies to all kernels extracted from ADR-005.

Non-goals:

Does not let later ADR-006 through ADR-009 claims supersede ADR-005 closure.

Forbidden interpretations:

- Do not reopen ADR-005 closed workflow/state-progression decisions from later convergence prose.
- Do not treat ADR-005 as deciding complete flag lifecycle semantics beyond the workflow-specific flag interactions it names.
- Do not treat pattern inventory or formal pattern schemas as committed by ADR-005.

Open edges:

Later extraction must judge ADR-006 through ADR-009 against the ADR-001 through ADR-005 closure baseline.

Platform specification note:

Use as the final ADR-005 closure boundary.

## Kernel: ADR-005 No Structural Change Contract

Status: Settled
Kind: invariant

Specification statement:

ADR-005 adds no event envelope fields and no structural event type. The event envelope remains at eleven fields and the structural type vocabulary remains at six types. ADR-005 rejects adding `status_changed` because workflow state transitions require no distinct platform processing behavior beyond existing event types plus shape and pattern definitions.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `## Decision`
- `docs/adrs/adr-005-state-progression.md` / `#### S4: State Machines as Projection Patterns`
- `docs/adrs/adr-005-state-progression.md` / `### What is now constrained`

Closure basis:

ADR-settled invariant and rejected alternative.

Scope:

Applies to ADR-005 envelope stability, structural type vocabulary, `status_changed`, `current_state`, and pattern references.

Non-goals:

Does not prevent a future approved ADR from adding a new type if a later scenario proves different platform processing behavior.

Forbidden interpretations:

- Do not add `status_changed` for ADR-005 workflow.
- Do not add `current_state` or `pattern_ref` to the event envelope for ADR-005.
- Do not encode domain lifecycle labels in structural event `type`.

Open edges:

Future type additions remain possible only through explicit platform evolution.

Platform specification note:

Use as the ADR-005 confirmation that workflow is carried by projection, shapes, and patterns rather than stored-event structure.

## Kernel: ADR-005 Transition Violation Flag Contract

Status: Settled
Kind: interaction-rule

Specification statement:

The Conflict Detector evaluates incoming events against pattern-defined state machine rules and raises a `transition_violation` flag when an accepted event represents a state transition invalid under the subject's current derived state. The event is stored and accepted; the flag surfaces the anomaly for resolution. `transition_violation` is a strategy-protecting flag category because its type string appears in stored `ConflictDetected` events.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S1: Transition Violation Flag Category`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled strategy-protecting constraint.

Scope:

Applies to workflow transition validation, conflict detection, stale offline work, flag creation, and sync-time processing.

Non-goals:

Does not decide broad flag semantics, flag queues, severity, or UI behavior outside the workflow-transition context.

Forbidden interpretations:

- Do not reject stale/offline events solely because they violate current workflow state.
- Do not treat `transition_violation` as a structural event type.
- Do not run workflow transition flag generation on device; device validation remains advisory under ADR-005.

Open edges:

Later flag-semantics extraction must preserve this workflow-specific category unless a valid source explicitly narrows non-conflicting details.

Platform specification note:

Use as ADR-005's workflow-specific extension to accept-and-flag conflict detection.

## Kernel: ADR-005 Flagged Event State Derivation Contract

Status: Settled
Kind: algorithm

Specification statement:

Events carrying unresolved flags are excluded from state machine evaluation in the projection engine. They remain visible in the event timeline but do not change `current_state`. When a flag is resolved as accepted, the projection re-derives state including the event. When a flag is resolved as rejected, the projection remains unchanged.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S2: Flagged Events Excluded from State Machine Evaluation`
- `docs/adrs/adr-005-state-progression.md` / `### What is now constrained`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled strategy-protecting constraint.

Scope:

Applies to unresolved flags, workflow state projection, current-state computation, event timeline visibility, and flag resolution effects.

Non-goals:

Does not decide all ADR-006 flag lifecycle semantics, severity behavior, queues, or general reporting rules.

Forbidden interpretations:

- Do not hide flagged events from timelines.
- Do not let unresolved flagged events advance workflow state.
- Do not conflate exclusion from state derivation with event rejection, deletion, or invisibility.
- Do not infer that all non-workflow projections must exclude flagged events unless a source explicitly says so.

Open edges:

ADR-006-R may refine broader flag lifecycle semantics only if it does not contradict this ADR-005 workflow-state rule.

Platform specification note:

Use as the core boundary separating accept-and-store, timeline visibility, policy execution, and workflow state derivation.

## Kernel: ADR-005 Flag Resolvability Classification Contract

Status: Settled
Kind: configuration-boundary

Specification statement:

Each flag category carries a platform-defined resolvability classification: `auto_eligible` or `manual_only`. Auto-resolution policies can target only `auto_eligible` flag types, and deployers cannot change a flag type's classification. Initial classifications are: `transition_violation` and `stale_reference` are `auto_eligible`; `scope_violation`, `identity_conflict`, `concurrent_state_change`, and `domain_uniqueness_violation` are `manual_only`.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S3: Flag Resolvability Classification`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled strategy-protecting constraint.

Scope:

Applies to auto-resolution eligibility, deployer policy boundaries, and initial flag category classification.

Non-goals:

Does not define all future flag categories or complete ADR-006 flag semantics.

Forbidden interpretations:

- Do not let deployers make `manual_only` flag categories auto-resolvable.
- Do not silently auto-resolve scope, identity, concurrent-state, or domain-uniqueness flags under ADR-005.
- Do not treat resolvability classification as deployer-authored configuration.

Open edges:

Future platform code changes may promote or add classifications through explicit platform evolution; ADR-006-R must be checked against this baseline.

Platform specification note:

Use as ADR-005's guardrail preventing auto-resolution from swallowing security- or judgment-heavy flags.

## Kernel: ADR-005 Projection-Derived State Machine Contract

Status: Settled
Kind: algorithm

Specification statement:

Subject lifecycle state is derived from the event sequence by the Projection Engine using pattern-defined state machine rules. State is never stored in events, and the platform does not enforce workflow transitions by rejecting events; it flags violations. The on-device Command Validator is advisory and may warn users, while server-side sync processing evaluates the same transition rules and creates `transition_violation` flags when needed.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S4: State Machines as Projection Patterns`
- `docs/adrs/adr-005-state-progression.md` / `### What is now constrained`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled initial strategy.

Scope:

Applies to workflow state derivation, command validation, stale offline work, event acceptance, shape/pattern meaning, and projection rebuildability.

Non-goals:

Does not define projection rebuild optimization strategy or final validator UX.

Forbidden interpretations:

- Do not make connectivity required for workflow progression.
- Do not reject offline work because the current server projection would mark the transition invalid.
- Do not store authoritative workflow state in event payloads or envelope fields.
- Do not treat advisory device warnings as flag creation.

Open edges:

Projection rebuild strategy and validator user experience remain implementation-owned.

Platform specification note:

Use as ADR-005's core state progression model.

## Kernel: ADR-005 Pattern Registry Contract

Status: Settled
Kind: primitive

Specification statement:

The platform provides a Pattern Registry: a closed vocabulary of platform-fixed workflow skeletons that deployers select and parameterize at Layer 0. Patterns define participant roles, participating structural event types, state machine skeletons, auto-maintained projections, and deployer parameterization points. Pattern definitions sync to devices as part of ADR-004 atomic configuration packages, and are consumed by the Projection Engine and Conflict Detector.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S5: Pattern Registry`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled initial strategy.

Scope:

Applies to workflow skeleton governance, deployer pattern selection, role mapping, state derivation, transition validity, and configuration package contents.

Non-goals:

Does not commit exact initial pattern inventory, exact state machine skeletons, or formal YAML/JSON schema syntax.

Forbidden interpretations:

- Do not let deployers author arbitrary state machines.
- Do not freeze example patterns as the complete platform inventory.
- Do not treat pattern addition as deployment configuration; it is platform evolution.

Open edges:

Pattern inventory and formal schemas remain platform specification or implementation work.

Platform specification note:

Use as the ADR-005 workflow primitive; final rest-state cleanup should separate architecture from inventory.

## Kernel: ADR-005 Pattern Composition Contract

Status: Settled
Kind: interaction-rule

Specification statement:

Five rules govern pattern composition. One activity binds at most one subject-level state machine to a subject. Event-level patterns compose freely. Approval sub-flows may embed within subject patterns when scoped to a specific submission event. Cross-activity linking uses `activity_ref` or payload cross-references, not shared patterns. Shape-to-pattern mapping is unique within an activity. The Projection Engine maintains one subject-level state per `(subject, activity, pattern)` tuple and multiple event-level states per `(event_id, pattern)`.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S6: Pattern Composition Rules`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled initial strategy.

Scope:

Applies to subject-level state, event-level state, embedded approvals, cross-activity links, deploy-time validation, and projection state keys.

Non-goals:

Does not define every pattern-specific transition table.

Forbidden interpretations:

- Do not allow competing subject-level lifecycle state machines in one activity.
- Do not let one shape be claimed by multiple patterns in the same activity.
- Do not make one pattern span multiple activities.

Open edges:

Pattern-specific details remain inventory/specification work.

Platform specification note:

Use as ADR-005's composition contract and AP-6 guardrail.

## Kernel: ADR-005 Source-Only Flagging Contract

Status: Settled
Kind: algorithm

Specification statement:

When an upstream event is retroactively flagged, only the root-cause event receives a flag. Downstream events created from flagged sources do not receive additional stored flags. Downstream dependency on a flagged source is computed by projection through source-chain traversal over existing payload references such as `source_event_ref`. Resolving the root flag removes the downstream flagged-source indicator from projections.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S7: Source-Only Flagging`
- `docs/adrs/adr-005-state-progression.md` / accepted risk on downstream contamination markers
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled initial strategy.

Scope:

Applies to retroactive flags, downstream trigger/review events, projection lineage traversal, root-cause resolution, and workflow provenance display.

Non-goals:

Does not define complete UI treatment or broad ADR-006 flag queue semantics.

Forbidden interpretations:

- Do not create derived stored flags on every downstream event merely because a source event is flagged.
- Do not hide downstream dependency on flagged roots; source-chain traversal is a required projection capability.
- Do not retroactively invalidate trigger outputs that were valid when created.

Open edges:

Source-chain traversal depth limits and rendering details remain implementation-owned.

Platform specification note:

Use as ADR-005's flag-cascade boundary: root flag is stored; downstream contamination is projected.

## Kernel: ADR-005 Context Expression Scope Contract

Status: Settled
Kind: contract

Specification statement:

The Expression Evaluator gains a form-context-only `context.*` data scope with seven pre-resolved read-only values: `context.subject_state`, `context.subject_pattern`, `context.activity_stage`, `context.actor.role`, `context.actor.scope_name`, `context.days_since_last_event`, and `context.event_count`. Values are resolved on device from local projection and assignment state at form-open time and remain static during form fill. Trigger expressions do not access `context.*`.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S8: Context Expression Scope`
- `docs/adrs/adr-005-state-progression.md` / `### What is now constrained`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled initial strategy.

Scope:

Applies to L2 form expressions, workflow-aware forms, local projection reads, actor assignment context, and platform-fixed expression namespace governance.

Non-goals:

Does not define on-device caching internals or make context available to trigger expressions.

Forbidden interpretations:

- Do not let deployers define arbitrary `context.*` properties.
- Do not use `context.*` for dynamic joins, aggregate queries, or arbitrary projection access.
- Do not add expression functions or grammar changes for ADR-005.
- Do not make `context.*` live-update during form fill.

Open edges:

Property caching and exact rendering are implementation-owned.

Platform specification note:

Use as ADR-005's closure of ADR-004's `context.*` deferral.

## Kernel: ADR-005 Auto-Resolution L3b Contract

Status: Settled
Kind: interaction-rule

Specification statement:

Deployers can configure auto-resolution policies as an L3b deadline-check subtype for `auto_eligible` flags. A policy watches a flag type and structural condition, waits for a same-subject resolution-enabling event within a time window, and then creates an explicit `ConflictResolved` event or escalates/manual-review outcome when the deadline expires. Auto-resolution uses the same server-side trigger execution model and loop-prevention guards as L3b deadline checks.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S9: Auto-Resolution as L3b Sub-Type`
- `docs/adrs/adr-005-state-progression.md` / `### What is now constrained`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled initial strategy.

Scope:

Applies to auto-eligible flag resolution, watched event conditions, time windows, explicit resolution events, L3b trigger budgets, and loop prevention.

Non-goals:

Does not allow device-side auto-resolution, silent mutation, or general rule-language policies.

Forbidden interpretations:

- Do not auto-resolve `manual_only` flags.
- Do not resolve flags without explicit auditable resolution events.
- Do not bypass detect-before-act, DAG max path two, or input/output separation.
- Do not exclude auto-resolution policies from L3b deployment-wide budget.

Open edges:

Auto-resolution authoring UX remains implementation-owned.

Platform specification note:

Use as ADR-005's closure of domain conflict resolution automation within a bounded workflow/flag mechanism.

## Kernel: ADR-005 Auto-Resolution Actor Contract

Status: Settled
Kind: contract

Specification statement:

Auto-resolution events use `actor_ref` format `system:auto_resolution/{policy_id}`. This extends ADR-004's system actor convention by adding `auto_resolution` as a system source type. Every auto-resolution remains attributable to the policy that produced it.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `#### S9: Auto-Resolution as L3b Sub-Type`
- `docs/adrs/adr-005-state-progression.md` / principles confirmed

Closure basis:

ADR-settled contract.

Scope:

Applies to system-authored auto-resolution events, actor attribution, policy identity, and auditability.

Non-goals:

Does not define all possible system actor source types.

Forbidden interpretations:

- Do not use null or anonymous actors for auto-resolution.
- Do not hide policy identity when recording auto-resolution.
- Do not treat auto-resolution as unaudited background correction.

Open edges:

Detailed `ConflictResolved` payload schema remains implementation/specification work unless closed elsewhere.

Platform specification note:

Use as ADR-005's auditable-authority contract for auto-resolution.

## Kernel: ADR-005 Explicit Non-Decisions

Status: Open
Kind: open-question

Specification statement:

ADR-005 explicitly leaves several concerns outside ADR closure: exact pattern inventory and skeletons; formal pattern schema format; workflow projection rebuild optimization; source-chain traversal depth limits; `context.*` caching internals; auto-resolution authoring UX; workflow-aware reporting and aggregation; and the consolidated platform specification document.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `## What This Does NOT Decide`

Closure basis:

Open explicit deferral set.

Scope:

Applies to implementation, platform specification, reporting capability, authoring UX, and performance tuning work after ADR-005.

Non-goals:

Does not reopen ADR-005's decided architecture and interaction contracts.

Forbidden interpretations:

- Do not infer exact pattern inventory from ADR-005 examples.
- Do not treat performance strategy or authoring format as decided by ADR-005.
- Do not use deferred implementation details to weaken settled ADR-005 contracts.

Open edges:

Deferred items must be handled by platform specification, implementation, or later valid decisions without contradicting ADR-005.

Platform specification note:

Use as ADR-005's boundary for what remains open after final decision.

## Kernel: ADR-005 Accepted Risk Set

Status: Settled
Kind: conditional-validity-rule

Specification statement:

ADR-005 accepts five risks with revisit triggers: workflow projection rebuild cost, insufficient pattern inventory, missed downstream contamination under source-only flagging, auto-resolution masking legitimate issues, and form-to-projection dependency from `context.*`. Mitigations include ADR-001 B-to-C maintained views, platform evolution for new patterns, required source-chain traversal rendering, auditable `ConflictResolved` events, and pre-resolved static form context.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `### Risks accepted`

Closure basis:

ADR-settled accepted-risk set.

Scope:

Applies to projection performance, pattern coverage, source-only flagging UX, auto-resolution audit, and context-dependent form behavior.

Non-goals:

Does not convert accepted risks into unresolved architecture decisions.

Forbidden interpretations:

- Do not ignore source-chain traversal rendering; ADR-005 treats it as mitigation for source-only flagging.
- Do not treat auto-resolution as safe from audit.
- Do not treat insufficient pattern inventory as permission for deployer-authored state machines.

Open edges:

Revisit triggers may drive future platform evolution if thresholds are met.

Platform specification note:

Use to preserve ADR-005's known risk posture.

## Kernel: ADR-005 Reconciliation Result

Status: Settled
Kind: invariant

Specification statement:

ADR-005 carries forward the Session 1 through Session 3 lineage without contradiction and settles the ADR-005 decision surface. Q1 closes as projection-derived state machines; Q2 closes with no `status_changed`; Q3 closes with five composition rules; Q4 closes as source-only flagging with source-chain traversal; Q5 closes as bounded form-only `context.*`; Q6 closes as L3b auto-resolution gated by flag resolvability. ADR-005 leaves implementation and platform-specification details explicitly open.

Source basis:

- `docs/adrs/adr-005-state-progression.md` / `## Decision`
- `docs/adrs/adr-005-state-progression.md` / `## What This Does NOT Decide`
- `docs/adrs/adr-005-state-progression.md` / `## Traceability`

Closure basis:

ADR-settled reconciliation result.

Scope:

Applies to ADR-005 final extraction, closure of ADR-004 deferrals, and transition to later ADR judgment.

Non-goals:

Does not validate ADR-006 through ADR-009 claims.

Forbidden interpretations:

- Do not allow later ADR-006 through ADR-009 convergence prose to silently supersede this ADR-005 closure.
- Do not merge workflow-specific flag contracts into broad flag semantics without source-backed compatibility.

Open edges:

Next extraction should move to the next source in the approved scan order while judging later ADRs against the ADR-001 through ADR-005 closure baseline.

Platform specification note:

Use as the closure marker for ADR-005 before evaluating later ADRs.
