# Ship-1 — Project Shepherd Review

## Intended Slice

- S00 basic structured capture (household_observation/v1 shape)
- S01 entity-linked capture (subject_ref: household)
- S03 user-based assignment (CHV actor bound to village via assignment_created/v1)
- S19 offline constraint: accept-offline, detect-on-sync, flag-don't-reject
- Bootstrap: two CHV actors, two villages, two assignment events; tokens issued; admin UI renders events + flags

## Actual Delivered Slice

- S00 + S01 + S03 delivered as intended via W-0 (happy path)
- Scope violation detection via W-2 (CHV-B captures in CHV-A village → `scope_violation` flag)
- Identity conflict detection via W-1 (duplicate household name, same village, different subject_id → `identity_conflict` flag)
- Event-replay scope reconstruction (`ScopeResolver`) — no cache; confirmed by retro §3.2
- Conflict detection emits `conflict_detected/v1` with `type=alert` and `system:conflict_detector/<category>` actor_ref
- `/api/sync/config` delivers scope-filtered villages, activities, shapes
- Thymeleaf admin: `/admin/events` + `/admin/flags`

## Alignment Check

| Item | Result | Note |
|---|---|---|
| Scenario coverage | ✔ | S00, S01, S03 all exercised under S19 |
| Accept-and-flag invariant | ✔ | No rejection for state; flags emitted post-persist |
| Scope resolver event-replay | ✔ | No cache by design; retro confirms |
| Flag shape discrimination | ✔ | Flags identified by shape_ref, not type |
| system:actor format | ✔ | ADR-008 §S2 met |
| FP-001 gate (temporal divergence test) | ✖ | Gate part 2 not authored; W-2 covers correctness, not temporal divergence |
| Scope eval asymmetry formalized | ✖ | Push uses event-time scope; pull uses request-time; noted in retro §3.3 but no FP raised |

## Drift Signals

- Scope evaluation asymmetry (push=event-time, pull=request-time) observed and documented in retro §3.3 but never promoted to a Flagged Position. This is a behavioral gap that compounds as more scope-sensitive scenarios land.
- FP-001 temporal divergence test deferred for the first time; sets a precedent for deferral-without-gate-enforcement.

## Missing Elements

- Integration test that proves `ScopeResolver` cannot pass under a cache-based implementation (FP-001 gate part 2)
- Formal FP for scope evaluation asymmetry

## Verdict

**Aligned**

Slice delivered cleanly. Domain coverage matches intent. Two missing items are documented (FP-001 partial, asymmetry noted) but neither blocks the slice's core claims. No structural drift.
