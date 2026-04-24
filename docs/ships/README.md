# Ships — Scenario-Driven Delivery Index

> Each Ship delivers one or more scenarios from [../scenarios/](../scenarios/) end-to-end. Ships replace the prior phase-driven decomposition at Phase 2 closure (2026-04-23). See [charter.md § Rhythm](../charter.md#rhythm) for the per-Ship loop and anti-drift rules.

## Map

| Ship | Cluster | Scenarios | ADR surface | Status |
|---|---|---|---|---|
| **Ship-1** | A+B — offline structured capture under assigned scope | S00, S01, S03 | ADR-001, -003, -006, -007, -008, -009 §S1/§S2 | **CLOSED** (tag `ship-1`, 2026-04-24). Delivery surface: scripted two-device HTTP simulation. |
| **Ship-1b** | same scenarios as Ship-1, new delivery surface | S00, S01, S03 under S19 | + ADR-002 §S5, ADR-001 §S1/§S3, ADR-002 §S1 under app lifecycle, ADR-006 §S2 flag UX | **spec in progress**. Real Flutter client + real device/emulator. Parent `ship-1` tag does not move. |
| Ship-2 | D — long-running subjects + shape evolution | S06, S08, S05 | + ADR-002 §S7–§S11, ADR-004 §S10 | pending |
| Ship-3 | C — judgment / approvals | S04, S11 | + ADR-005 §S1/§S4/§S5/§S6 | pending |
| Ship-4 | E — transfer + campaigns | S07, S09, S14, S22 | + ADR-004 §S2, ADR-008 §S3, ADR-009 §S4 | pending |
| Ship-5 | F — reactive layer (triggers, expressions, auto-resolution) | S02, S10, S12 | + ADR-004 §S11–§S14, ADR-005 §S8/§S9 | pending |

## Out of scope for Ships

- **S19** (offline capture and sync) — a constraint every Ship inherits, not a Ship itself.
- **S13** (cross-flow linking) — lands opportunistically when Ship-4 introduces two coexisting activities.
- **S05, S20, S21** — composite scenarios; acceptance tests for Ships that contain their parts.
- **S15, S16, S18** — Phase-2 per [viability-assessment.md](../viability-assessment.md); deliberately deferred.

## Open question — Ship-2 size (raised 2026-04-24)

Ship-2 as currently mapped bundles **S06 (registry lifecycle + merge/split)**, **S06b (shape evolution, embedded in S06 file)**, and **S08 (case management — long-running situations with responsibility shifts)**. That is three distinct ADR clusters: merge/split (ADR-002 §S6–§S11), shape evolution (ADR-004 §S10), and assignment churn + state progression under a long-running subject (ADR-003 + ADR-005 first-time exercise). By the Ship-size pressure-test in the orchestrator skill, this exceeds a single Ship's envelope.

**Proposed split (not applied; user decides at Ship-2 open):**

| Ship | Scenarios | Primary cluster |
|---|---|---|
| Ship-2 | S06 registry lifecycle + merge/split | identity (ADR-002 §S6–§S11), projection rebuild (ADR-001 §S2 activation via FP-002) |
| Ship-3 | S06b shape evolution | shape versioning (ADR-004 §S10), version-diverse projection (ADR-001 §S2) |
| Ship-4 | S08 case management | assignment churn + state progression (ADR-003 + ADR-005 first exercise) |
| Ship-5 | S04 + S11 judgment / approvals | review / approval (ADR-005 §S1, §S4–§S6) |
| Ship-6 | S07 + S09 + S14 + S22 transfer + campaigns | transfer + multi-level (ADR-008 §S3, ADR-009 §S4) |
| Ship-7 | S02 + S10 + S12 reactive layer | triggers / expressions / auto-resolution (ADR-004 §S11–§S14, ADR-005 §S8–§S9) |

Resolve this split at Ship-2 spec-open time. If rejected, the rejection must be recorded with a named reason; silent reversion to the 5-Ship bundling violates the skill's Ship-size pressure-test.

## Unassigned surface

- **Corrections (CHV-initiated amendment of prior captures)** — ADR-001 §S1 commits to it; no scenario currently owns it. Registered as [FP-005](../flagged-positions.md#fp-005). Likely lands in the Ship that delivers S04 (review).

## Rules (carried from charter § Rhythm)

1. Ship spec written before any code.
2. Slice end-to-end; no horizontal work.
3. Every commit cites the scenario ID it advances (`feat(ship-N): S0X — …`).
4. Scenario acceptance walkthrough follows scenario prose — it is the acceptance criterion.
5. Retro: ADR supersessions → ledger updates → charter regenerated (never hand-edited) → drift gate re-runs → Ship tag.

Hard rule: no code work begins until the Ship spec is written and all cited ADRs are Decided.
