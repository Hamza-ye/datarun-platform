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
| V2 | Set up, not built | `needs_runtime_verification` | BAR-010 and BAR-011 are accepted; S23 is the next-wave runtime route to prove setup/config composition as a product workflow. |
| V3 | Trustworthy records | `runtime_verified_current_baseline` | BAR-001, BAR-002, BAR-005, BAR-006, and BAR-009 are accepted; NW-026/S00 adds append-only structured-capture runtime evidence. |
| V4 | One system, not many | `runtime_verified_current_baseline` | BAR-012 through BAR-014 are accepted; NW-029/S21 and NW-030/S27 add scenario-grade composition evidence. |
| V5 | Grows without breaking | `runtime_verified_current_baseline` | BAR-010, BAR-012, and BAR-014 are accepted for config package atomicity/version coexistence, pattern delivery, and projection equivalence. |
| V6 | Domain-agnostic | `runtime_verified_current_baseline` | S27 exists and NW-030 accepted a non-health logistics/distribution runtime probe. |

## Tensions

| Tension | Current closure | Route |
|---|---|---|
| Offline-first vs real-time reactivity | `closed_architecturally` for delayed server authority; `intentionally_deferred` for general trigger execution | S12 is thickened; do not claim general trigger execution until BAR-101 successor work selects it. |
| Configuration simplicity vs expressive power | `closed_architecturally` by CDL-038/CDL-043/CDL-044; `runtime_verified_current_baseline` for package delivery | Use routing checklist for every config feature; S23 is the next bounded setup/config runtime probe. |
| Domain-agnosticism vs domain-specific validation | `runtime_verified_current_baseline` for current mechanisms | NW-030 proves non-health transfer pressure; keep domain validation as configuration/content, not platform semantics. |
| Trustworthy records vs offline correction | `runtime_verified_current_baseline` | BAR-002 and NW-026 prove append-only correction/idempotency; flag lifecycle/projection rows are accepted. |
| Scale/local retention vs low-end devices | `runtime_verified_current_baseline` for current retention policy; `future_decision` beyond that | BAR-008 and S24 scenario exist; field-level encryption/redaction/purge remains BAR-106 future work. |

## Blind Spots

| Blind spot | Current closure | Route |
|---|---|---|
| Configuration/setup experience | `needs_runtime_verification` | S23 exists; BAR-010 is accepted, so setup/config can now be exercised as a bounded runtime/product workflow probe. |
| Long-running data lifecycle and retention | `closed_architecturally` for sync/retention split; `future_decision` beyond current policy | S24 exists; keep CDL-037 distinction between sync and local retention and BAR-106 for field-level sensitivity. |
| Worker onboarding, transfer, leave, and exit | `closed_architecturally` for assignment-derived authority pressure | S25 exists; production auth-provider behavior remains BAR-104/FP-011 future work. |
| Reporting and aggregate oversight | `needs_runtime_verification` | S26 exists; next-wave probe candidate after BAR-010 or phase selection. |
| Non-health domain proof | `runtime_verified_current_baseline` | S27 exists and NW-030 accepted the logistics transfer runtime probe. |
| Emergency authority override | `intentionally_deferred` | NW-019 mini-brief only; do not bypass assignment authority. |
| Analytics-derived initiation | `future_decision` | NW-020 mini-brief only; do not treat as ordinary trigger/config behavior. |
| Multi-audience views | `intentionally_deferred` | NW-018 mini-brief only; do not promote S15 into current baseline. |

## Working Conclusion

The conditional-go concerns are not a signal to reopen the whole architecture. As of 2026-06-03, BAR-001 through BAR-015 are accepted and the remaining pressure has moved into deliberate next-wave scenario/runtime choices. Deferred/future-decision rows remain intentionally outside the current baseline.

1. Run S23 setup/config as the first next-wave probe, because it directly exercises the "set up, not built" promise now that BAR-010 is accepted.
2. Keep S26 reporting/aggregate oversight as the next candidate after S23, constrained to current projection, freshness, flag, and scope semantics.
3. Keep deferred platform evolution clearly marked until a successor decision selects it.
