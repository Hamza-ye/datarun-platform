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
| `runtime_verified_current_baseline` | Current BAR/backlog evidence has accepted the relevant baseline rows or scenario probes. |
| `needs_scenario_thickening` | Problem-space coverage is too thin for product confidence. |
| `intentionally_deferred` | Not part of the current baseline. |
| `future_decision` | Requires successor platform/architecture decision. |

## Vision Guarantees

| ID | Guarantee | Current closure | Route |
|---|---|---|---|
| V1 | Works without connectivity | `runtime_verified_current_baseline` | BAR-003, BAR-004, BAR-008, and BAR-014 are accepted; NW-025/S19 adds stale-offline-authority runtime evidence. |
| V2 | Set up, not built | `baseline_candidate` | S23 exists; BAR-011 is accepted; BAR-010 config package delivery still needs focused acceptance before this is a fully accepted baseline claim. |
| V3 | Trustworthy records | `runtime_verified_current_baseline` | BAR-001, BAR-002, BAR-005, BAR-006, and BAR-009 are accepted; NW-026/S00 adds append-only structured-capture runtime evidence. |
| V4 | One system, not many | `runtime_verified_current_baseline` | BAR-012 through BAR-014 are accepted; NW-029/S21 and NW-030/S27 add scenario-grade composition evidence. |
| V5 | Grows without breaking | `baseline_candidate` | Projection rebuildability and pattern delivery evidence exist, but BAR-010 still owns config package atomicity/version coexistence acceptance. |
| V6 | Domain-agnostic | `runtime_verified_current_baseline` | S27 exists and NW-030 accepted a non-health logistics/distribution runtime probe. |

## Tensions

| Tension | Current closure | Route |
|---|---|---|
| Offline-first vs real-time reactivity | `closed_architecturally` for delayed server authority; `intentionally_deferred` for general trigger execution | S12 is thickened; do not claim general trigger execution until BAR-101 successor work selects it. |
| Configuration simplicity vs expressive power | `closed_architecturally` by CDL-038/CDL-043/CDL-044; `baseline_candidate` for package delivery | Use routing checklist for every config feature; verify BAR-010 before broad setup claims. |
| Domain-agnosticism vs domain-specific validation | `runtime_verified_current_baseline` for current mechanisms | NW-030 proves non-health transfer pressure; keep domain validation as configuration/content, not platform semantics. |
| Trustworthy records vs offline correction | `runtime_verified_current_baseline` | BAR-002 and NW-026 prove append-only correction/idempotency; flag lifecycle/projection rows are accepted. |
| Scale/local retention vs low-end devices | `runtime_verified_current_baseline` for current retention policy; `future_decision` beyond that | BAR-008 and S24 scenario exist; field-level encryption/redaction/purge remains BAR-106 future work. |

## Blind Spots

| Blind spot | Current closure | Route |
|---|---|---|
| Configuration/setup experience | `baseline_candidate` | S23 exists; BAR-010 is the remaining acceptance route. |
| Long-running data lifecycle and retention | `closed_architecturally` for sync/retention split; `future_decision` beyond current policy | S24 exists; keep CDL-037 distinction between sync and local retention and BAR-106 for field-level sensitivity. |
| Worker onboarding, transfer, leave, and exit | `closed_architecturally` for assignment-derived authority pressure | S25 exists; production auth-provider behavior remains BAR-104/FP-011 future work. |
| Reporting and aggregate oversight | `needs_runtime_verification` | S26 exists; next-wave probe candidate after BAR-010 or phase selection. |
| Non-health domain proof | `runtime_verified_current_baseline` | S27 exists and NW-030 accepted the logistics transfer runtime probe. |
| Emergency authority override | `intentionally_deferred` | NW-019 mini-brief only; do not bypass assignment authority. |
| Analytics-derived initiation | `future_decision` | NW-020 mini-brief only; do not treat as ordinary trigger/config behavior. |
| Multi-audience views | `intentionally_deferred` | NW-018 mini-brief only; do not promote S15 into current baseline. |

## Working Conclusion

The conditional-go concerns are not a signal to reopen the whole architecture. As of 2026-06-03, most closure pressure has moved from broad concern into BAR/backlog evidence. The remaining current-baseline acceptance item is BAR-010 config package delivery; deferred/future-decision rows remain intentionally outside the current baseline.

1. Accept the remaining baseline candidate, BAR-010, through targeted verification.
2. Select next-wave scenario probes deliberately after the baseline is clean, with S23 and S26 as visible candidates rather than implicit work.
3. Keep deferred platform evolution clearly marked until a successor decision selects it.
