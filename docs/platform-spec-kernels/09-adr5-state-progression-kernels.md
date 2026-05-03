# ADR-005 State Progression Kernel Staging

Status: Iteration 34 staging

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
