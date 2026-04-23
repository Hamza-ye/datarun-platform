# Ships — Scenario-Driven Delivery Index

> Each Ship delivers one or more scenarios from [../scenarios/](../scenarios/) end-to-end. Ships replace the prior phase-driven decomposition at Phase 2 closure (2026-04-23). See [charter.md § Rhythm](../charter.md#rhythm) for the per-Ship loop and anti-drift rules.

## Map

| Ship | Cluster | Scenarios | ADR surface | Status |
|---|---|---|---|---|
| **Ship-1** | A+B — offline structured capture under assigned scope | S00, S01, S03 | ADR-001, -003, -006, -007, -008, -009 §S1/§S2 | **spec in progress** |
| Ship-2 | D — long-running subjects + shape evolution | S06, S08, S05 | + ADR-002 §S7–§S11, ADR-004 §S10 | pending |
| Ship-3 | C — judgment / approvals | S04, S11 | + ADR-005 §S1/§S4/§S5/§S6 | pending |
| Ship-4 | E — transfer + campaigns | S07, S09, S14, S22 | + ADR-004 §S2, ADR-008 §S3, ADR-009 §S4 | pending |
| Ship-5 | F — reactive layer (triggers, expressions, auto-resolution) | S02, S10, S12 | + ADR-004 §S11–§S14, ADR-005 §S8/§S9 | pending |

## Out of scope for Ships

- **S19** (offline capture and sync) — a constraint every Ship inherits, not a Ship itself.
- **S13** (cross-flow linking) — lands opportunistically when Ship-4 introduces two coexisting activities.
- **S05, S20, S21** — composite scenarios; acceptance tests for Ships that contain their parts.
- **S15, S16, S18** — Phase-2 per [viability-assessment.md](../viability-assessment.md); deliberately deferred.

## Rules (carried from charter § Rhythm)

1. Ship spec written before any code.
2. Slice end-to-end; no horizontal work.
3. Every commit cites the scenario ID it advances (`feat(ship-N): S0X — …`).
4. Scenario acceptance walkthrough follows scenario prose — it is the acceptance criterion.
5. Retro: ADR supersessions → ledger updates → charter regenerated (never hand-edited) → drift gate re-runs → Ship tag.

Hard rule: no code work begins until the Ship spec is written and all cited ADRs are Decided.
