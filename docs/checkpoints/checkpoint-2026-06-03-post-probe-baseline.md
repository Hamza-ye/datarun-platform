# Project Checkpoint - 2026-06-03 (Post-Probe Baseline)

---

## 1. Bearing

- **Anchor commit**: `a457b62 test(workflow): add logistics transfer scenario probe`
- **Phase**: Post-Phase-4 stabilization.
- **Momentum**: `ADVANCING` - the first scenario-probe set has landed and the accepted baseline is now broad enough to choose the next slice deliberately.
- **Last milestone**: NW-030 accepted the S27 logistics transfer runtime probe after NW-029 accepted S21 supervisor review.
- **Horizon**: accept or block BAR-010 config package delivery, then select the next product/architecture movement from a clean checkpoint.

Phase 4 remains complete and the project is not yet in a new feature phase. The current truth surface is CDL for architecture and the agent working-surface BAR/backlog for implementation standing. The main change since the last checkpoint is that the selected S00/S19/S21/S27 runtime probes have moved from recommendations to accepted evidence. Do not infer new architecture from that confidence: deferred trigger, auth-provider, entity lifecycle, sensitivity, auto-resolution, resolver reassignment, envelope, and scope surfaces remain outside the baseline.

## 2. Standing Snapshot

### Baseline Register

| Standing | Rows | Meaning |
|---|---|---|
| `baseline_accepted` | BAR-001, BAR-002, BAR-003, BAR-004, BAR-005, BAR-006, BAR-007, BAR-008, BAR-009, BAR-011, BAR-012, BAR-013, BAR-014, BAR-015 | Envelope/type closure, append-only event store, sync/backfill, platform payloads, flags/resolver routing, assignment/scope, mobile retention/expression/projection, identity, patterns, transition/domain detection, projection equivalence, and historical `events.location_path` immutability are accepted with attached evidence. |
| `baseline_candidate` | BAR-010 | Config package delivery still needs a focused acceptance pass covering deploy-time validation, atomic package/version behavior, shape version coexistence, pattern definition delivery, and mobile parsing/pending behavior. |
| `deferred` / `future_decision` | BAR-101 through BAR-108 | Trigger execution, auto-resolution, resolver reassignment, production OIDC/JWT authority, S06/entity lifecycle, field-level sensitivity/security, new envelope fields/types, and new scope mechanisms remain unpromoted. |

### Backlog

| Standing | Rows | Meaning |
|---|---|---|
| `accepted` | NW-001 through NW-017, NW-022 through NW-030 | Working surface, BAR, core verification, scenario thickening, escape-hatch register, pressure map, mobile advisory fix, S19/S00 probes, BAR-002/BAR-015 acceptance, and S21/S27 probes are complete. |
| `ready` | NW-031 | Verify BAR-010 config package delivery baseline. |
| `deferred` | NW-018, NW-019, NW-020 | S15, S16, and S18 mini-briefs remain deferred exploration. |
| `future_decision` | NW-021 | S06/entity lifecycle remains an explicit product/platform decision, not assumed baseline. |

### Steward Notes

`docs/implementation/module-interfaces.md` remains useful. It should be treated as an implemented boundary map for implementer routing, not as proof of acceptance; BAR records proof. `docs/status.md`, `docs/README.md`, the pressure map, and the viability closure review were refreshed in this checkpoint pass so old ADR routing and unexecuted-probe wording do not mislead future agents.

## 3. Recent Movement

| Commit | Meaning | Evidence |
|---|---|---|
| `a457b62` | NW-030 accepted S27 logistics transfer runtime probe. | `ResponsibilityBindingScenarioIntegrationTest` covers `transfer_with_acknowledgment/v1`, scoped sync, manual discrepancy review, exact resolver semantics, and out-of-order accept-and-flag behavior. |
| `48c5e14` | NW-029 accepted S21 supervisor review runtime probe. | `ResponsibilityBindingScenarioIntegrationTest` covers scoped supervisor visibility, `capture_with_review/v1`, unresolved/non-designated flagged review exclusion, and exact resolver re-inclusion. |
| `acece96` | NW-028 accepted BAR-015 historical location-path immutability. | Added `ScopeFilteredSyncIntegrationTest.subjectLocationUpdate_doesNotRewriteHistoricalEventLocationPath`; targeted tests passed. |
| `54a552e` | NW-027 accepted BAR-002 append-only event store. | Source inspection plus sync/e2e tests proved no operational event rewrite/delete path beyond BAR-015 insert-time location denormalization. |
| `d4ac128` | NW-026 accepted S00 structured capture runtime probe. | `SyncControllerIntegrationTest` covers append-only correction, idempotent replay, server-watermark pull ordering, and existing concurrency flag mechanics. |
| `c971299` | NW-025 accepted S19 offline/stale authority runtime probe. | Scenario probe covers stale authority flags, current-scope live pull, and subject-history cursor isolation. |
| `d279ba3` | NW-024 fixed the mobile advisory authority boundary. | Mobile advisory checks warn without blocking structurally valid captures. |
| `65e7589`, `0c3ad62`, `cf5d0a7`, `eea9f0f` | Steward routing commits created bounded prompts for the accepted slices above. | Backlog rows and prompt files captured goal, guardrails, tests, commit boundaries, and stop conditions. |

The important status shift is that S21 and S27 are no longer blocked on projection/mobile evidence. NW-009 and NW-010 are accepted, and the S21/S27 probes themselves have passed.

## 4. Architecture Guardrails

### Source Order

1. CDL is architecture authority.
2. The rationale companion is non-authoritative routing/test-intent context.
3. The escape-hatch register routes measured evolution pressure only.
4. Vision, constraints, scenarios, and access-control docs define problem-space pressure.
5. `contracts/` define implementation-facing boundaries.
6. Phase files and IDRs are provenance unless routed.
7. BAR/backlog record current implementation standing.

### Non-Negotiable Guardrails

| Guardrail | Standing |
|---|---|
| No new envelope fields or event `type` values | Preserved; S00/S19/S21/S27 used existing envelope/type vocabulary. |
| Events remain append-only operational truth | BAR-002 accepted; corrections/reviews/resolutions are additional events. |
| Projections remain derived and rebuildable | BAR-012/BAR-014 accepted; scenario probes use projection-derived state. |
| Valid state/policy anomalies are accepted and flagged | Preserved in S19 stale authority, S21 unauthorized review, and S27 out-of-order receipt. |
| Live sync is current-scope/watermark-based, not audit pull | Preserved; subject-history remains separate. |
| Assignment-derived authority is the current authority model | Preserved; FP-011 remains open only for production auth-provider work. |
| Config stops at bounded L3, not custom code/scope/state-machine authoring | Preserved; BAR-010 still needs acceptance evidence. |

### Escape Hatches

All escape hatches remain inactive. The recent scenario probes did not fire a hatch: they reused current scope axes, platform-owned patterns, event-derived authority/state, and existing flag/resolution mechanics.

## 5. Risk Pulse

### New Or Elevated Risks

| Risk | Severity | Trigger | Mitigation | Backlog need |
|---|---|---|---|---|
| BAR-010 is now the lone baseline candidate | Medium | Most other BAR rows are accepted, so config-package assumptions become the main remaining confidence gap. | NW-031 is ready with server and mobile acceptance points. | Covered by NW-031. |
| Strong probe results could tempt accidental phase selection | Medium | S00/S19/S21/S27 now give broader confidence, but they do not authorize S23/S26, reporting warehouses, trigger execution, or entity lifecycle. | Require an explicit phase/probe selection after BAR-010 or a separate checkpoint. | No new row until selected. |
| S23 setup pressure may hide config-as-code expansion | Medium | BAR-010 and S23 both touch setup/configuration. | Keep BAR-010 to package delivery evidence only; no S23 runtime probe or config language growth in that slice. | Covered by NW-031 stop conditions. |
| Old docs can still look authoritative by chronology | Low | `docs/README.md`, viability review, and pressure map predated the accepted probe set. | This checkpoint pass refreshed stale routing/status language and preserved historical docs as provenance. | No separate row needed unless drift returns. |

### Resolved Or De-Risked Items

| Item | Resolution | Evidence |
|---|---|---|
| Mobile advisory rejection risk | NW-024 fixed advisory behavior to warn, not reject. | Targeted mobile tests passed per working-agent report. |
| S19 core offline promise uncertainty | NW-025 accepted runtime probe. | 58-test surrounding Maven slice passed. |
| S00 trustworthy-record correction path | NW-026 accepted runtime probe. | 33-test surrounding Maven slice passed. |
| BAR-002 append-only proof gap | NW-027 accepted after inspection and tests. | Sync and e2e/backfill/scope tests passed. |
| BAR-015 location-path immutability gap | NW-028 accepted after regression coverage. | Scope/containment tests passed. |
| S21/S27 projection/mobile gating | NW-029 and NW-030 accepted after NW-009/NW-010 evidence. | 38-test and 56-test surrounding Maven slices passed. |

## 6. Scenario And Product Pressure

The first-wave runtime probe set from the pressure map is complete:

| Scenario | Standing | Notes |
|---|---|---|
| S00 | Accepted runtime evidence | Basic structured capture and correction without envelope/state mutation. |
| S19 | Accepted runtime evidence | Offline stale authority and scoped reconciliation. |
| S21 | Accepted runtime evidence | Supervisor review, scoped visibility, projection state, and exact resolver behavior. |
| S27 | Accepted runtime evidence | Non-health logistics transfer and manual discrepancy review. |

Next-wave pressure remains visible but unselected. S23 setup/configuration is the closest product pressure to BAR-010, but BAR-010 should be verified as baseline package delivery before any S23 scenario runtime probe. S26 reporting/aggregate oversight is a plausible next probe after projection confidence, but it must not introduce a separate analytics/reporting storage model by implication. S12 remains active scenario pressure while BAR-101 keeps general trigger execution deferred. S06/entity lifecycle, S15 multi-audience views, S16 emergency authority, and S18 analytics-derived initiation remain deferred/future-decision pressure.

## 7. Verification Ledger

| Area | Command/evidence | Result |
|---|---|---|
| NW-025 S19 focused probe | `./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest test` | Passed: 2 tests. |
| NW-025 S19 surrounding slice | `./mvnw -q -Dtest=SubjectHistoryBackfillIntegrationTest,ScopeFilteredSyncIntegrationTest,SyncControllerIntegrationTest,AuthFlagIntegrationTest,AssignmentContainmentIntegrationTest,ResponsibilityBindingScenarioIntegrationTest test` | Passed: 58 tests. |
| NW-026 S00 focused probe | `./mvnw -Dtest=SyncControllerIntegrationTest test` | Passed: 9 tests. |
| NW-026 S00 surrounding slice | `./mvnw -q -Dtest=SyncControllerIntegrationTest,ConflictDetectorIntegrationTest,DomainUniquenessIntegrationTest,EnvelopeVocabularyTest,EnvelopeSchemaParityTest test` | Passed: 33 tests. |
| NW-027 BAR-002 append-only | Source inspection plus `./mvnw -Dtest=SyncControllerIntegrationTest test` and `./mvnw -Dtest=MultiDeviceE2ETest,SubjectHistoryBackfillIntegrationTest,ScopeFilteredSyncIntegrationTest test` | Passed: 9 tests and 18 tests after DB access was run outside the sandbox. |
| NW-028 BAR-015 location path | `./mvnw -Dtest=ScopeFilteredSyncIntegrationTest,AssignmentContainmentIntegrationTest test` | Passed: 28 tests. |
| NW-029 S21 focused probe | `./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest test` | Passed: 3 tests. |
| NW-029 S21 surrounding slice | `./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest,PatternStateProjectionTest,TransitionViolationIntegrationTest,ConflictResolutionIntegrationTest,ScopeFilteredSyncIntegrationTest test` | Passed: 38 tests. |
| NW-030 S27 focused probe | `./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest test` | Passed: 4 tests. |
| NW-030 S27 surrounding slice | `./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest,PatternStateProjectionTest,TransitionViolationIntegrationTest,ConflictResolutionIntegrationTest,AssignmentContainmentIntegrationTest,ScopeFilteredSyncIntegrationTest test` | Passed: 56 tests. |

This checkpoint pass did not rerun the Maven/Flutter suites because it changed documentation/routing only. `git diff --check` was run after the edits.

## 8. March Orders

1. **Run NW-031: verify BAR-010 config package delivery baseline.**
   - Why now: BAR-010 is the only remaining BAR-001 through BAR-015 candidate, and it underpins S23/setup confidence.
   - Expected artifact: targeted server/mobile evidence, BAR-010 update, NW-031 backlog update, and a single focused commit.
   - Scope: WORK.
   - Stop condition: acceptance would require a new config package schema, trigger execution, custom scope logic, deployer-authored state machines, or changing envelope/pattern contracts.

2. **After BAR-010, make an explicit next-slice choice instead of drifting into one.**
   - Why now: the platform is strong enough to move, but product pressure splits between setup (S23), reporting (S26), and broader phase selection.
   - Expected artifact: steward recommendation or bounded prompt for one selected slice.
   - Scope: SESSION.
   - Stop condition: the proposed slice depends on BAR-101 through BAR-108 deferred/future surfaces without a successor decision.

3. **If choosing a next runtime probe, prefer S23 or S26 with tight boundaries.**
   - Why now: S23 validates "set up, not built" after BAR-010; S26 validates aggregate oversight after projection confidence.
   - Expected artifact: one prompt, not both, with files/tests/forbidden work named.
   - Scope: SESSION.
   - Stop condition: S23 expands the config language or S26 introduces a reporting warehouse/analytics subsystem by implication.

4. **Keep FP-011 and BAR-101 through BAR-108 out of incidental work.**
   - Why now: stronger baseline confidence increases the chance that deferred product pressure looks easy to slip in.
   - Expected artifact: no code; prompts and reviews continue naming these as forbidden unless explicitly selected.
   - Scope: ongoing guardrail.
   - Stop condition: any ordinary task starts treating production auth claims, trigger execution, auto-resolution, resolver reassignment, entity lifecycle, field-level security, new envelope fields/types, or new scope mechanisms as current baseline.

5. **Retain `module-interfaces.md` as an implementer router, but do not let it replace BAR.**
   - Why now: it remains useful for bounded coder context, while BAR is now the evidence surface.
   - Expected artifact: future prompts cite only the relevant module section plus BAR rows.
   - Scope: ongoing hygiene.
   - Stop condition: module-boundary prose is used to mark capability accepted without source/test/scenario evidence.
