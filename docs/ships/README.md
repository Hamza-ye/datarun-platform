# Ships — Scenario-Driven Delivery Index

> Each Ship delivers a **vertical slice through one or more scenarios** from [../scenarios/](../scenarios/). A Ship does not deliver a scenario "fully" — scenarios are cross-cutting problem narratives. The Ship picks the slice, and §6.5 "deliberately not built" lists what the Ship parks for later Ships or FPs. Ships replace the prior phase-driven decomposition at Phase 2 closure (2026-04-23). See [charter.md § Rhythm](../charter.md#rhythm) for the per-Ship loop and anti-drift rules.

## Map

| Ship | Cluster | Scenarios | ADR surface | Status |
|---|---|---|---|---|
| **Ship-1** | A+B — offline structured capture under assigned scope | S00, S01, S03 | ADR-001, -003, -006, -007, -008, -009 §S1/§S2 | **CLOSED** (tag `ship-1`, 2026-04-24). Delivery surface: scripted two-device HTTP simulation. |
| **Ship-1b** | same scenarios as Ship-1, new delivery surface | S00, S01, S03 under S19 | + ADR-002 §S5, ADR-001 §S1/§S3, ADR-002 §S1 under app lifecycle, ADR-006 §S2 flag UX | **CLOSED** (tag `ship-1b`, 2026-04-25). Real Flutter client + real device/emulator. Parent `ship-1` tag did not move. |
| Ship-2 | identity — registry lifecycle + merge/split | S06 (registry lifecycle + merge/split) | + ADR-002 §S6–§S11, ADR-001 §S2 (projection rebuild via FP-002) | pending |
| Ship-3 | shape evolution | S06b | + ADR-004 §S10, ADR-001 §S2 (version-diverse projection) | pending |
| Ship-4 | case management — long-running situations | S08 | + ADR-003 (assignment churn) + ADR-005 first exercise | pending |
| Ship-5 | C — judgment / approvals | S04, S11 | + ADR-005 §S1/§S4/§S5/§S6 | pending |
| Ship-6 | E — transfer + campaigns | S07, S09, S14, S22 | + ADR-004 §S2, ADR-008 §S3, ADR-009 §S4 | pending |
| Ship-7 | F — reactive layer (triggers, expressions, auto-resolution) | S02, S10, S12 | + ADR-004 §S11–§S14, ADR-005 §S8/§S9 | pending |

## Out of scope for Ships

- **S19** (offline capture and sync) — a constraint every Ship inherits, not a Ship itself.
- **S13** (cross-flow linking) — lands opportunistically when Ship-4 introduces two coexisting activities.
- **S05, S20, S21** — composite scenarios; acceptance tests for Ships that contain their parts.
- **S15, S16, S18** — Phase-2 per [viability-assessment.md](../viability-assessment.md); deliberately deferred.

## Ship-2 size split — RESOLVED 2026-04-25

The original 5-Ship map bundled **S06 registry lifecycle + merge/split**, **S06b shape evolution**, and **S08 case management** into a single Ship-2. By the Ship-size pressure-test in the orchestrator skill, that exceeded a single Ship's envelope (three distinct ADR clusters: identity, shape versioning, assignment churn).

Resolved at Ship-1b close: split applied as the 7-Ship map above. Ship-2 = S06 only (identity / merge/split). Ship-3 = S06b (shape evolution). Ship-4 = S08 (case management). Downstream Ships renumbered.

Rationale: each of the three clusters carries its own ADR surface, its own retro pressure-test envelope, and its own first-time exercise of previously-unstressed positions. Bundling would violate the orchestrator skill's "one Ship = one cluster of ADR positions under stress" rule.

## Unassigned surface

- **Corrections (CHV-initiated amendment of prior captures)** — ADR-001 §S1 commits to it; no scenario currently owns it. Registered as [FP-005](../flagged-positions.md#fp-005). Likely lands in the Ship that delivers S04 (review).

## Rules (carried from charter § Rhythm)

1. Ship spec written before any code.
2. Slice vertically; no horizontal work. The slice advances the chosen scenarios; the rest of each scenario's problem surface is parked in §6.5 or on the FP register.
3. Every commit cites the scenario ID it advances (`feat(ship-N): S0X — …`).
4. Scenario acceptance walkthrough follows scenario prose — it is the acceptance criterion.
5. Retro: ADR supersessions → ledger updates → charter regenerated (never hand-edited) → drift gate re-runs → Ship tag.

Hard rule: no code work begins until the Ship spec is written and all cited ADRs are Decided.
