# ADR-006-R Flag Semantics Assessment

Status: Later-source assessment against accepted ADR-001 through ADR-005 baseline

This document assesses `../../adrs/adr-006-flag-semantics-R.md` through the accepted baseline and architecture responsibility map. ADR-006-R is assessment material only. It may supersede `../../adrs/adr-006-flag-semantics.md` inside the ADR-006 revision lineage, but it does not supersede ADR-001 through ADR-005.

## Source Basis

Assessment inputs:

- `02-change-control.md`
- `04-architecture-baseline-v0.md`
- `05-decision-gap-register.md`
- `07-system-boundary-map.md`
- `08-baseline-acceptance-check.md`
- `09-identity-boundary-control.md`
- `../10-adr1-5-rest-state-closure-register.md`
- `../../adrs/adr-006-flag-semantics-R.md`
- `../../adrs/adr-006-flag-semantics.md`, only because ADR-006-R carries S1 through S4 by reference

Not used as authority:

- ADR-006-R references to reviews, flagged positions, architecture, implementation tracking, charter, ledger, or downstream contract documents.
- ADR-007 through ADR-009 claims. Those remain unassessed until their own passes.

## Assessment Scope

ADR-006-R has two different assessment surfaces:

1. Carried flag-semantics claims from ADR-006 S1 through S4.
2. New alias-cycle claim in ADR-006-R S5.

The carried claims mostly address the accepted `General flag semantics` later-source assessment gap.

The S5 claim is narrower and riskier. It touches subject-lineage acyclicity, online-only merge/split validation, accept-and-flag, general flag category creation, conflict lifecycle, and read-side identity semantics over a flagged cycle.

## Responsibility Routing

| Claim Area | Primary Responsibility Area | Affected Areas | Reason |
|---|---|---|---|
| accept-and-flag as invariant | Flag / Resolution | Event Log / Storage; Projection / Workflow State | Elaborates accepted accept-and-flag discipline without changing event-log source of truth. |
| flag as canonical event-stream anomaly surface | Flag / Resolution | Event Envelope / Schema; Event Log / Storage | Attempts to close general event-stream representation for state anomalies. |
| conflict detection as algorithm | Flag / Resolution | Identity / Lineage; Assignment / Authority / Sync; Projection / Workflow State | Keeps detector procedures separate from source facts and downstream effects. |
| flag creation location | Flag / Resolution | Assignment / Authority / Sync; Trigger / Reactivity | Addresses where flags are produced, which is not closed by ADR-001 through ADR-005. |
| alias-cycle predicate | Identity / Lineage | Flag / Resolution; Event Log / Storage | The predicate is about subject-lineage acyclicity; surfacing is through flags. |
| alias-cycle resolution/read behavior | Identity / Lineage | Flag / Resolution; Projection / Workflow State | ADR-006-R explicitly defers read-side and resolution semantics. |

## Claim Classification

| ADR-006-R Claim | Classification | Assessment |
|---|---|---|
| S1: validly structured events are not rejected for state-based reasons; anomalies are accepted and flagged | Consistent elaboration, with bounded open-gap closure value | Consistent with the accepted detect-before-act and accept-and-flag discipline. It must remain bounded to state-based anomalies and must not weaken structural envelope/payload validation. |
| S2: flags are the canonical event-stream representation of state anomalies | Open-gap closure candidate | This addresses the `General flag semantics` gap. It is compatible with ADR-005 workflow-specific flags if it does not absorb ADR-005 source-only workflow lineage or auto-resolution semantics without classification. |
| S3: conflict detection is algorithmic and can evolve | Consistent elaboration | Compatible with the responsibility map: detector procedures may consume facts from identity, authorization, or workflow, but those source areas do not own flag lifecycle. |
| S4: flags are server-created by default; device-side creation is additively evolvable | Open-gap closure candidate plus deferred implementation/spec detail | Current baseline does not close flag creation location. Server-side default is plausible under detect-before-act, but should not be promoted to an invariant without formal gap closure. Device-side flagging remains future implementation/spec detail unless a later decision requires it. |
| S5: alias-cycle closure is surfaced as `cycle_violation` while the cycle-closing event is still accepted | Valid dispute requiring formal decision before adoption | This exposes a real tension with the accepted baseline statement that subject lineage remains acyclic and merge/split are online-only where validation is required. The need to detect alias cycles is consistent with the baseline; accepting a cycle-closing lineage event while read-side behavior is undefined is not safe to absorb silently. |
| S5.1: cycle detection runs on the push path after structural validation and before persistence | Open-gap closure candidate if S5 dispute is resolved | The placement is compatible with structural validation versus state-anomaly separation, but exact push-path mechanics are not closed by ADR-001 through ADR-005. |
| S5.2: batch-serial detection within one push request | Deferred implementation/spec detail | This is deterministic procedure detail. It may be a good implementation rule, but current baseline does not define push request array-order semantics. |
| S5.3: `cycle_violation` is manual-only and cycle resolution is deferred | Open-gap closure candidate plus explicit unresolved gap | Manual-only fits the risk profile, but resolution semantics are explicitly undefined. Do not treat cycle handling as settled until resolution/read behavior is decided or clearly deferred. |
| S5.4: request-time anchor for alias-cycle detection | Open-gap closure candidate requiring decision | Compatible with `device_time` being advisory, but it introduces a detector temporal-anchor rule not present in ADR-001 through ADR-005. It should be decided as part of alias-cycle semantics, not generalized to other detectors. |
| S5.5: add `cycle_violation` to flag catalog | Open-gap closure candidate | This is a proposed general flag category. It should not be added to the accepted baseline until general flag semantics and alias-cycle handling are closed. |
| S5 deferred read-side and resolution semantics | Architecture decision gap | ADR-006-R itself says read-side semantics over cyclic graphs and cycle resolution are undecided. These block a safe identity/flag spec section if alias-cycle behavior is included. |
| S5 fork-cycle case marked decided-unexercised | Deferred implementation/spec detail | Treat as unproven corollary, not as accepted baseline behavior. It depends on the eventual graph model and tests. |

## Accepted Carry-Forward Candidates

The following ADR-006-R material is safe to carry forward as candidate platform-spec language after review:

- Accept-and-flag is a state-anomaly invariant, not a detector implementation.
- Flag lifecycle belongs to Flag / Resolution, not Identity / Lineage, Assignment / Authority / Sync, or Projection / Workflow State.
- Conflict detection is an algorithmic pipeline that consumes source facts from other boundaries.
- Structural validation remains outside accept-and-flag; malformed envelopes or payloads can still be rejected.

These candidates do not require changing the ADR-001 through ADR-005 baseline.

## Items Not Safe To Absorb Yet

- A closed unified flag catalog.
- `cycle_violation` as an accepted baseline flag category.
- Server-side flag creation as a permanent invariant.
- Request-time temporal anchoring as a general detector rule.
- Acceptance of a cycle-closing alias event while claiming lineage acyclicity remains closed.
- Read-side identity semantics over a flagged cycle.
- Cycle resolution effects.

These require either formal gap closure or explicit deferral in the first platform-spec outline.

## Baseline Impact

No ADR-001 through ADR-005 baseline item should be changed by this assessment alone.

Required gap/routing updates:

- Add an explicit architecture decision gap for alias-cycle enforcement, read-side behavior, and resolution semantics.
- Route that gap primarily to Identity / Lineage, with Flag / Resolution, Projection / Workflow State, and Event Log / Storage as affected areas.

## Completed Follow-Up

The alias-cycle gap and routing were recorded, and ADR-007 through ADR-009 were assessed in `11` through `13`.

During platform-spec drafting, use this assessment only for the accepted carry-forward candidates and explicit hold-backs listed above.
