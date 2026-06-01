# Viability Closure Review

Status: active post-Phase-4 review

Authority: the CDL governs architecture. This review classifies viability pressure and routes follow-up work; it does not create architecture decisions or implementation acceptance.

## Purpose

The original viability assessment returned a conditional go. Phase 4 has now completed, so the remaining work is not more broad exploration. The right closure path is to separate what is architecturally closed, what is baseline-candidate implementation, what needs verification, what needs scenario thickening, and what remains deferred or future-decision work.

## Closure Vocabulary

| Status | Meaning |
|---|---|
| `closed_architecturally` | CDL contains the governing decision. |
| `baseline_candidate` | Current docs/code claim enough to verify through the BAR. |
| `needs_code_verification` | Requires targeted code inspection before acceptance. |
| `needs_runtime_verification` | Requires targeted test or scenario execution before acceptance. |
| `needs_scenario_thickening` | Problem-space coverage is too thin for product confidence. |
| `intentionally_deferred` | Not part of the current baseline. |
| `future_decision` | Requires successor platform/architecture decision. |

## Vision Guarantees

| ID | Guarantee | Current closure | Route |
|---|---|---|---|
| V1 | Works without connectivity | `baseline_candidate` | Verify BAR-003, BAR-004, BAR-008, and BAR-014 with targeted sync/mobile tests. |
| V2 | Set up, not built | `needs_scenario_thickening` | Add S23 configuration/setup scenario; verify BAR-010 and BAR-011 before promoting claims. |
| V3 | Trustworthy records | `baseline_candidate` | Verify BAR-001, BAR-002, BAR-005, BAR-006, and BAR-009. |
| V4 | One system, not many | `needs_runtime_verification` | Verify BAR-012 through BAR-014 and run scenario-grade composition tests. |
| V5 | Grows without breaking | `baseline_candidate` | Verify config version coexistence, shape version permanence, projection rebuildability, and pattern package delivery. |
| V6 | Domain-agnostic | `needs_scenario_thickening` | Add S27 logistics/distribution composite to prove non-health pressure. |

## Tensions

| Tension | Current closure | Route |
|---|---|---|
| Offline-first vs real-time reactivity | `closed_architecturally` for delayed server authority; `needs_scenario_thickening` for S12 | Thicken S12 under offline delay; do not claim general trigger execution. |
| Configuration simplicity vs expressive power | `closed_architecturally` by CDL-038/CDL-043/CDL-044 | Use routing checklist for every config feature; verify deploy-time validation. |
| Domain-agnosticism vs domain-specific validation | `closed_architecturally` by mechanism/instance split and shape-declared rules | Add S27 and keep domain validation as configuration/content, not platform semantics. |
| Trustworthy records vs offline correction | `closed_architecturally` by append-only and accept-and-flag | Verify flag lifecycle, identity resolution, and projection exclusion. |
| Scale/local retention vs low-end devices | `baseline_candidate` plus `needs_scenario_thickening` | Verify selective retention and add S24 data lifecycle scenario. |

## Blind Spots

| Blind spot | Current closure | Route |
|---|---|---|
| Configuration/setup experience | `needs_scenario_thickening` | NW-012 / S23. |
| Long-running data lifecycle and retention | `needs_scenario_thickening` | NW-014 / S24; keep CDL-037 distinction between sync and local retention. |
| Worker onboarding, transfer, leave, and exit | `needs_scenario_thickening` | NW-013 / S25. |
| Reporting and aggregate oversight | `needs_scenario_thickening` | NW-015 / S26. |
| Non-health domain proof | `needs_scenario_thickening` | NW-016 / S27. |
| Emergency authority override | `intentionally_deferred` | NW-019 mini-brief only; do not bypass assignment authority. |
| Analytics-derived initiation | `future_decision` | NW-020 mini-brief only; do not treat as ordinary trigger/config behavior. |
| Multi-audience views | `intentionally_deferred` | NW-018 mini-brief only; do not promote S15 into current baseline. |

## Working Conclusion

The conditional-go concerns are not a signal to reopen the whole architecture. They split into three professional work streams:

1. Accept baseline candidates through targeted verification.
2. Thicken the scenarios that expose product and operational blind spots.
3. Keep deferred platform evolution clearly marked until a successor decision selects it.
