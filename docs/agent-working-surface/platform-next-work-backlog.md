# Platform Next Work Backlog

Status: active working surface

This backlog orders post-Phase-4 stabilization work. It is not architecture authority; it routes work against the CDL and the Baseline Acceptance Register.

## Priority

| Priority | Meaning |
|---|---|
| P0 | Protects canonical invariants or prevents known drift. |
| P1 | Accepts baseline candidates on critical flows. |
| P2 | Thickens missing scenarios and viability coverage. |
| P3 | Improves product/spec clarity. |
| P4 | Explores deferred platform evolution. |

## Status

| Status | Meaning |
|---|---|
| `ready` | Ready for an implementation or verification agent. |
| `blocked` | Waiting on an explicit dependency or decision. |
| `in_review` | Being handled in the current stabilization slice or awaiting review. |
| `accepted` | Exit condition has been met and evidence is attached elsewhere. |
| `deferred` | Intentionally not active. |
| `future_decision` | Requires successor architecture/platform decision. |
| `superseded` | Replaced by newer work. |

## Backlog

| ID | Title | Type | Priority | Status | Source | Depends on | Exit condition |
|---|---|---|---|---|---|---|---|
| NW-001 | Establish agent working surface | documentation_control | P0 | `accepted` | CDL-000; post-Phase-4 plan | none | README, BAR, backlog, and routing checklist exist. |
| NW-002 | Remove legacy review drafts from active use | documentation_control | P0 | `accepted` | Phase 4 review cleanup | NW-001 | Superseded evidence-pack/backlog/escape-hatch drafts are not active agent inputs. |
| NW-003 | Build baseline acceptance register | baseline_control | P0 | `accepted` | phase specs, contracts, CDL | NW-001 | Core capabilities and deferred surfaces have status, anchors, and exit conditions. |
| NW-004 | Harmonize `events.location_path` IDRs | documentation_control | P0 | `accepted` | IDR-014/015 review finding | NW-003 | IDR-014 and IDR-015 agree that historical event `location_path` is not rewritten except controlled NULL backfill. |
| NW-005 | Verify envelope/type closure | baseline_acceptance | P0 | `accepted` | BAR-001 | NW-003 | Targeted server contract tests pass and evidence is attached to BAR-001. |
| NW-006 | Verify platform payload contract boundary | baseline_acceptance | P0 | `accepted` | BAR-005 | NW-003 | Platform payload contract tests pass and deployer-shape boundary remains intact. |
| NW-007 | Verify flag catalog and resolver routing | baseline_acceptance | P0 | `accepted` | BAR-006 | NW-003 | Targeted flag/resolution tests prove exact resolver equality and resolver payload presence. |
| NW-008 | Verify sync and authority baseline | baseline_acceptance | P1 | `accepted` | BAR-003, BAR-004, BAR-007 | NW-003 | Scope-filtered sync, subject-history backfill, assignment containment, and responsibility-binding tests pass. |
| NW-009 | Verify projection and integrity baseline | baseline_acceptance | P1 | `accepted` | BAR-009, BAR-012, BAR-013, BAR-014 | NW-003 | 2026-06-02: Server-side identity, projection equivalence, pattern state, transition, and uniqueness tests pass; BAR-014 mobile evidence remains routed to NW-010. |
| NW-010 | Verify mobile baseline | baseline_acceptance | P1 | `accepted` | BAR-008, BAR-011, BAR-012, BAR-014 | NW-024 | 2026-06-02: Targeted Flutter tests for config, expression, projection, pattern, selective retention, and mobile advisory authority boundary pass; BAR-008, BAR-011, and BAR-014 are accepted, and BAR-012 has mobile evidence attached. |
| NW-011 | Create viability closure review | viability_review | P1 | `accepted` | viability assessment; post-Phase-4 plan | NW-003 | V1-V6, tensions, and blind spots have closure routes. |
| NW-012 | Add S23 configuration/setup scenario | scenario_thickening | P1 | `accepted` | V2 setup blind spot | NW-003, NW-011 | Scenario covers setup flow without implementation prescription. |
| NW-013 | Add S25 worker onboarding/transfer/exit scenario | scenario_thickening | P1 | `accepted` | access-control role transition pressure | NW-003, NW-011 | Scenario exercises stale authority, handoff, retention, and audit. |
| NW-014 | Add S24 long-running data lifecycle scenario | scenario_thickening | P2 | `accepted` | retention blind spot; CDL-037 | NW-003, NW-011 | Scenario separates live sync, device retention, audit reconstruction, and compliance evolution. |
| NW-015 | Add S26 reporting and aggregate oversight scenario | scenario_thickening | P2 | `accepted` | reporting blind spot | NW-003, NW-011 | Scenario makes freshness, flags, scope, and exclusion semantics visible. |
| NW-016 | Add S27 non-health composite scenario | scenario_thickening | P2 | `accepted` | domain-agnosticism proof | NW-003, NW-011 | Scenario validates logistics/distribution pressure without health vocabulary. |
| NW-017 | Thicken S12 under offline delay | scenario_thickening | P2 | `accepted` | scenario 12 viability risk; CDL-042 | NW-003, NW-011 | Scenario states delayed responses and does not claim a trigger engine is implemented. |
| NW-018 | Write S15 multi-audience views mini-brief | deferred_exploration | P4 | `deferred` | viability assessment | NW-011 | Brief remains problem-space only and does not promote implementation. |
| NW-019 | Write S16 emergency authority override mini-brief | deferred_exploration | P4 | `deferred` | viability assessment; FP-011-sensitive authority pressure | NW-011 | Brief marks emergency override as not current implementation. |
| NW-020 | Write S18 analytics-derived initiation mini-brief | deferred_exploration | P4 | `deferred` | viability assessment | NW-011 | Brief marks analytics initiation as future platform evolution. |
| NW-021 | Revisit S06/entity lifecycle decision | future_decision | P4 | `future_decision` | BAR-105 | product decision | Either keep S06 deferred or promote entity lifecycle through a bounded successor plan. |
| NW-022 | Formalize escape-hatch register | documentation_control | P0 | `accepted` | architecture boundary/primitives; `escape-hatch-register.md` | NW-001 | Active register exists; hatches are modeled as inactive measured triggers, not TODOs or implementation permission. |
| NW-023 | Create scenario-to-baseline pressure map | scenario_review | P1 | `accepted` | `docs/reviews/scenario-baseline-pressure-map-protocol.md`; scenarios index; BAR | NW-003, NW-011 | Scenarios are triaged against BAR/deferred surfaces and 2-4 near-term runtime probes are recommended without promoting deferred work. |
| NW-024 | Fix mobile advisory authority boundary | baseline_fix | P1 | `accepted` | CDL-003, CDL-035, CDL-047; NW-010 stop report | NW-010 stop condition | 2026-06-02: Mobile role/action advisories warn without becoming authoritative rejection of structurally valid capture; focused advisory/config/expression tests pass, targeted search finds no advisory rejection branches, and the NW-010 targeted mobile suite passes. |
| NW-025 | Add S19 offline/stale authority runtime probe | scenario_runtime_probe | P1 | `accepted` | NW-023; S19 pressure-map row; CDL-003, CDL-021, CDL-030, CDL-031, CDL-035, CDL-037 | NW-008, NW-009, NW-010, NW-024 | 2026-06-03: `./mvnw -q -Dtest=SubjectHistoryBackfillIntegrationTest,ScopeFilteredSyncIntegrationTest,SyncControllerIntegrationTest,AuthFlagIntegrationTest,AssignmentContainmentIntegrationTest,ResponsibilityBindingScenarioIntegrationTest test` passed (58 tests). `ResponsibilityBindingScenarioIntegrationTest` now includes the S19 runtime probe: stale structurally valid offline capture is persisted, `temporal_authority_expired` and `role_stale` are emitted without `scope_violation` misclassification, normal pull remains current-scope/watermark-based, and subject-history keeps its own cursor without mutating normal device watermark. |
| NW-026 | Add S00 structured capture runtime probe | scenario_runtime_probe | P1 | `accepted` | NW-023; S00 pressure-map row; CDL-001, CDL-003, CDL-006, CDL-007, CDL-008, CDL-019, CDL-021 | NW-025 | 2026-06-03: `./mvnw -q -Dtest=SyncControllerIntegrationTest,ConflictDetectorIntegrationTest,DomainUniquenessIntegrationTest,EnvelopeVocabularyTest,EnvelopeSchemaParityTest test` passed (33 tests). `SyncControllerIntegrationTest` now includes the S00 runtime probe: valid structured capture persists with the existing envelope, correction is appended as a second event without mutating the original payload, replaying the correction id is idempotent, pull remains server-watermark ordered, and stale same-subject work from another device emits existing `concurrent_state_change` flag mechanics. |
| NW-027 | Verify BAR-002 append-only event store acceptance | baseline_acceptance | P1 | `accepted` | BAR-002; CDL-001, CDL-002, CDL-019, CDL-021; NW-026 runtime evidence | NW-026 | Accepted 2026-06-03: BAR-002 accepted with source inspection showing operational event writes append through `EventRepository.insert` and no production event delete/rewrite path beyond BAR-015-tracked insert-time `location_path` denormalization; `./mvnw -Dtest=SyncControllerIntegrationTest test` passed (9 tests) and `./mvnw -Dtest=MultiDeviceE2ETest,SubjectHistoryBackfillIntegrationTest,ScopeFilteredSyncIntegrationTest test` passed (18 tests). |
| NW-028 | Verify BAR-015 historical location-path immutability | baseline_acceptance | P1 | `accepted` | BAR-015; CDL-031, CDL-033; IDR-014/015 | NW-027 | Accepted 2026-06-03: BAR-015 accepted with source inspection showing no reparent/backfill surface, only insert-time event `location_path` denormalization, and stored-path scope-filtered sync; added `ScopeFilteredSyncIntegrationTest.subjectLocationUpdate_doesNotRewriteHistoricalEventLocationPath`; `./mvnw -Dtest=ScopeFilteredSyncIntegrationTest,AssignmentContainmentIntegrationTest test` passed (28 tests). |
| NW-029 | Add S21 supervisor review runtime probe | scenario_runtime_probe | P2 | `ready` | NW-023; S21 pressure-map row; BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | NW-009, NW-010, NW-024, NW-028 | Scoped supervisor review produces expected `capture_with_review/v1` pattern state, preserves scoped visibility/subject-history boundaries, flags invalid review/state cases, and keeps exact resolver semantics without trigger execution, auto-resolution, resolver reassignment, or production auth claims. |
| NW-030 | Add S27 logistics transfer runtime probe | scenario_runtime_probe | P2 | `ready` | NW-023; S27 pressure-map row; BAR-006, BAR-007, BAR-012, BAR-013, BAR-014 | NW-009, NW-010, NW-024, NW-028 | Non-health logistics handoff probe exercises `transfer_with_acknowledgment/v1`, partial receipt/discrepancy review, scoped sync, projection equivalence, and manual resolution without auto-resolution, new scope mechanisms, or new envelope fields/types. |
