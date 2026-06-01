# Project Checkpoint — 2026-06-01 (Post-Phase-4 Stabilization)

---

## 1. Bearing

- **Anchor commit**: `99d6d76 docs(reviews): map scenario pressure to baseline`
- **Phase**: Post-Phase-4 stabilization.
- **Momentum**: `ADVANCING` — the project has moved from broad Phase 4 closure claims into BAR-backed verification and scenario-grade probe selection.
- **Last milestone**: NW-023 accepted the scenario-to-baseline pressure map and selected S00, S19, S21, and S27 as the near-term probe set.
- **Horizon**: accept the remaining projection/integrity and mobile baseline candidates, then turn S00/S19 into first scenario-grade runtime probes.

Phase 4 is complete and the project is intentionally not in a new feature phase. The current work is stabilization: keep the authority surface clean, verify baseline candidates with targeted tests, preserve deferred/future-decision boundaries, and only then promote scenario pressure into runtime probes. The working assumption is that CDL remains architecture authority, the Baseline Acceptance Register records current implementation standing, and old review artifacts no longer define active truth.

---

## 2. Standing Baseline

### Accepted Baseline Rows

| BAR | Capability | Evidence anchor |
|---|---|---|
| BAR-001 | Event envelope and type closure | 2026-06-01 targeted contract slice including envelope vocabulary/parity tests. |
| BAR-003 | Push/pull sync core | NW-008 targeted sync/authority tests; pull now scans past filtered rows and composes scope axes within assignment. |
| BAR-004 | Subject-history backfill | NW-008 targeted tests; independent cursor, per-page authorization, alias behavior, no normal watermark mutation. |
| BAR-005 | Platform payload contracts | 2026-06-01 platform payload contract tests. |
| BAR-006 | Flag catalog and resolver routing | NW-007 targeted integrity tests; exact resolver equality and unauthorized-resolution flagging. |
| BAR-007 | Assignment containment and scope-filtered sync | NW-008 targeted tests; multi-axis containment, actor-bound assignment commands, responsibility binding. |

### Remaining Baseline Candidates

| BAR | Capability | Next route |
|---|---|---|
| BAR-002 | Append-only event store | Backend verification: no operational update/delete path, idempotent inserts. |
| BAR-008 | Selective retention | Mobile baseline verification. |
| BAR-009 | Identity merge/split and alias projection | NW-009 projection/integrity verification. |
| BAR-010 | Config package delivery | Config verification; likely after NW-009 or as part of NW-010-adjacent work. |
| BAR-011 | Expression evaluator | Server/mobile evaluator parity verification. |
| BAR-012 | Pattern registry and pattern-state projection | NW-009 / NW-010 split between server and mobile projection. |
| BAR-013 | Transition and domain-uniqueness detection | NW-009 projection/integrity verification. |
| BAR-014 | Mobile/server projection equivalence | NW-009/NW-010 shared fixture verification. |
| BAR-015 | Historical `events.location_path` immutability | Candidate; code search supports insert-time-only update, regression guard still useful. |

### Deferred Or Future-Decision Surfaces

| BAR | Surface | Standing |
|---|---|---|
| BAR-101 | General trigger execution | `deferred`; S12 remains active scenario pressure, CDL-042 constrains future execution to server-side L3. |
| BAR-102 | Auto-resolution execution | `deferred`; do not infer from `auto_eligible` resolvability. |
| BAR-103 | Resolver reassignment | `future_decision`; no reassignment event surface exists. |
| BAR-104 | Production OIDC/JWT/Keycloak authority | `future_decision`; FP-011 remains open. |
| BAR-105 | S06/entity lifecycle | `deferred`; scenarios may pressure it but do not promote it. |
| BAR-106 | Field-level sensitivity/encryption/redaction | `future_decision`; current sensitivity is shape/activity-level. |
| BAR-107 | New envelope fields or event types | `future_decision`; no ordinary implementation may add them. |
| BAR-108 | New scope mechanisms | `future_decision`; scope mechanisms remain platform-fixed. |

### Active Backlog

| NW | Status | Meaning |
|---|---|---|
| NW-001 through NW-008, NW-011 through NW-017, NW-022, NW-023 | `accepted` | Working surface, BAR, cleanup, core verification, viability review, scenario thickening, escape-hatch register, and scenario pressure map are in place. |
| NW-009 | `ready` | Verify projection and integrity baseline. |
| NW-010 | `ready` | Verify mobile baseline. |
| NW-018 through NW-020 | `deferred` | Mini-briefs only; do not promote as implementation. |
| NW-021 | `future_decision` | S06/entity lifecycle decision remains unselected. |

---

## 3. Recent Movement

| Commit | Meaning | Evidence |
|---|---|---|
| `99d6d76` | NW-023 accepted the scenario-to-baseline pressure map. | `docs/reviews/scenario-baseline-pressure-map.md`; S00/S19/S21/S27 selected with deferred/future surfaces routed rather than promoted. |
| `50e07dc` | Added the NW-023 handoff prompt. | `docs/agent-working-surface/prompts/NW-023-create-scenario-to-baseline-pressure-map.md`. |
| `c5f0b61` | Added the scenario pressure-map protocol. | `docs/reviews/scenario-baseline-pressure-map-protocol.md`. |
| `828a0ea` | NW-008 accepted sync/authority baseline. | Two targeted Maven commands passed: 37 tests and 33 tests; BAR-003/BAR-004/BAR-007 accepted. |
| `4cbce70` | Added NW-008 handoff prompt. | `docs/agent-working-surface/prompts/NW-008-verify-sync-authority-baseline.md`. |
| `a88aeb9` | Clarified scenario routing. | S12 now explicitly routes trigger execution to BAR-101/CDL-042; S25/S27 wording avoids auth/entity-lifecycle cues. |

Superseded review drafts were removed from active use. Current implementation status belongs in BAR, not legacy evidence packs or Phase 4 review chronology.

---

## 4. Architecture Guardrails

### Source Order

1. CDL is architecture authority.
2. Rationale companion provides non-authoritative routing/test intent.
3. Escape-hatch register routes measured evolution pressure only.
4. Vision/scenarios define problem-space pressure.
5. `contracts/` define implementation-facing boundary intent.
6. Phase files and IDRs are provenance unless routed.
7. BAR records current accepted/candidate/deferred implementation standing.

### Non-Negotiable Guardrails

| Guardrail | Standing |
|---|---|
| No new envelope fields or event `type` values | Preserved. |
| Authority/state are event-derived unless explicit B-to-C hatch fires | Preserved. |
| Normal live sync is not historical/audit pull | Preserved by FP-005/NW-008. |
| Subject-history backfill does not mutate normal live-sync watermarks | Accepted by BAR-004. |
| Assignment authority is not auth-provider/group-derived | Preserved; FP-011 remains future-decision. |
| Deployer config must not become code, scope logic, or state-machine authoring | Preserved; expression/pattern hatches inactive. |
| Historical `events.location_path` is insert-time infrastructure metadata | Candidate BAR-015; NW-008 code search supports no normal rewrite path. |

### Escape Hatches

All six escape hatches are `inactive_until_triggered`: projection materialization, authority snapshot/context, actor-scoped ordering metadata, shape migration tooling, expression L3 extension, and pattern inventory expansion. None has fired. The register is routing context only and does not authorize implementation.

---

## 5. Risk Pulse

### New Or Elevated Risks

| Risk | Severity | Trigger | Mitigation | Backlog need |
|---|---|---|---|---|
| Conservative `has_more` after scoped pull filtering | Low | NW-008 pull now scans past filtered rows; if exactly `limit` authorized rows are found while candidates are exhausted, `has_more` may overstate and cause one extra empty pull. | Add a focused pagination semantics test when touching sync again; not a data leak or baseline blocker. | Optional follow-up if client UX/perf makes it visible. |
| Remaining baseline candidates are broad | Medium | NW-009 spans identity, projection equivalence, pattern state, transition, uniqueness, and conflict behavior. | Keep next prompts tightly sliced; accept BAR rows independently. | Covered by NW-009/NW-010. |
| Production auth drift | Medium | FP-011 remains open and may be tempting during worker-exit or reporting work. | Keep bearer actor-bound model until production OIDC/JWT work is explicitly selected. | Existing BAR-104/FP-011 sufficient. |
| Scenario probes becoming accidental feature phases | Medium | S00/S19/S21/S27 are now selected pressure probes and could attract trigger, lifecycle, auth-provider, or custom-scope work. | Treat the pressure map as routing, not authority; each probe prompt must name forbidden deferred/future surfaces. | Covered by NW-023 map; add probe-specific backlog rows only when drafting runtime probes. |

### Resolved Or De-Risked Items

| Risk | Resolution | When |
|---|---|---|
| Legacy review artifacts treated as current truth | Working-surface registers supersede old evidence/backlog/escape-hatch drafts. | `e51e6c7` |
| Resolver routing ambiguity | Manual identity conflict now preserves first unresolved resolver; exact resolver equality tested. | `ad0ec6d` |
| Scope-filtered pull leaking across subject/activity axes | Live pull now composes geographic, subject-list, and activity axes within the same assignment. | `828a0ea` |
| Subject-history backfill coupling to normal watermark | Targeted tests accepted independent cursor/no normal watermark mutation. | `828a0ea` |
| Unordered scenario pressure | NW-023 triaged S00-S16, S18, and S19-S27 against BAR/deferred surfaces. | `99d6d76` |

---

## 6. Scenario And Product Pressure

Scenarios 23-27 were added to thicken post-Phase-4 blind spots: setup/configuration, long-running data lifecycle, worker transitions, reporting/aggregate oversight, and non-health logistics. They remain problem-space documents and do not promote deferred surfaces by themselves.

Scenario 12 remains active pressure for follow-up actions after observations, but BAR-101 keeps general trigger execution deferred. Current architecture says any future L3 trigger execution is server-only per CDL-042.

S25 and S27 were corrected after review to avoid accidental implementation cues: worker exit is phrased as access/responsibility rather than login/auth provider behavior, and logistics uses "real-world things being tracked" rather than implying generic platform entity lifecycle.

NW-023 now gives scenario pressure a bounded routing surface. It selects S00 and S19 as first scenario-grade probe designs, with S21 and S27 prepared in parallel but gated on NW-009/NW-010 evidence. S23 and S26 remain next-wave probes; S06, S15, S16, and S18 stay deferred/future-decision pressure unless successor work explicitly selects them.

---

## 7. Verification Ledger

| Area | Command/evidence | Result |
|---|---|---|
| Envelope and platform payload contracts | `./mvnw -Dtest=EnvelopeVocabularyTest,EnvelopeSchemaParityTest,PlatformPayloadShapeContractTest,PlatformPayloadEmissionContractIntegrationTest,PlatformPayloadBoundaryTest,FlagCatalogTest test` | Passed; BAR-001 and BAR-005 accepted. |
| Flag catalog and resolver routing | `./mvnw -Dtest=FlagCatalogTest,ConflictResolutionIntegrationTest,ConflictDetectorIntegrationTest,AuthFlagIntegrationTest,DomainUniquenessIntegrationTest,TransitionViolationIntegrationTest test` | Passed: 56 tests; BAR-006 accepted. |
| Sync/backfill/scope first group | `./mvnw -Dtest=SubjectHistoryBackfillIntegrationTest,ScopeFilteredSyncIntegrationTest,SyncControllerIntegrationTest,IdentityResolverIntegrationTest test` | Passed: 37 tests; BAR-003/BAR-004/BAR-007 accepted. |
| Sync/authority second group | `mvn -Dtest=MultiDeviceE2ETest,AssignmentContainmentIntegrationTest,ResponsibilityBindingScenarioIntegrationTest,AuthFlagIntegrationTest test` | Passed: 33 tests; BAR-003/BAR-007 accepted. |
| Compile guard | `./mvnw -DskipTests clean test-compile` | Passed after NW-008. |
| Location path rewrite search | Code search after NW-008 | `events.location_path` update path remains insert-time resolution, not normal sync/backfill/reparent. |
| Scenario pressure map review | `git diff --cached --check`; targeted `rg` for trigger/auth/entity/envelope/scope/sensitivity terms | Passed; expected hits route deferred/future work instead of promoting it. |

Full Maven/Flutter suites were not rerun for this checkpoint. The stabilization strategy remains targeted verification first, broader suites when cross-module behavior changes or before a release-like checkpoint.

---

## 8. March Orders

1. **Run NW-009: verify projection and integrity baseline.**
   - Why now: BAR-009, BAR-012, BAR-013, and BAR-014 are the next critical server-side acceptance cluster after sync/authority.
   - Expected artifact: focused tests/fixes plus BAR/backlog updates; likely new or refreshed NW-009 prompt before handoff.
   - Scope: WORK.
   - Stop condition: any fix requires new envelope fields, durable workflow-state authority, auto-resolution, resolver reassignment, or entity lifecycle.

2. **Run NW-010: verify mobile baseline after the server projection slice.**
   - Why now: offline confidence depends on mobile selective retention, expression evaluator, projection engine, pattern projection, and shared fixture equivalence.
   - Expected artifact: targeted Flutter test evidence and BAR updates for BAR-008/BAR-011/BAR-012/BAR-014 where justified.
   - Scope: WORK.
   - Stop condition: mobile behavior diverges from contracts/shared fixtures or requires a new config/scope mechanism.

3. **Verify BAR-002 and BAR-015 as narrow backend guards.**
   - Why now: append-only event store and historical `events.location_path` immutability underpin many accepted rows but remain candidates.
   - Expected artifact: code inspection notes or focused regression tests, BAR updates, and optionally a small backlog item for `has_more` pagination semantics.
   - Scope: SESSION.
   - Stop condition: any operational update/delete or historical location rewrite path is found.

4. **Keep FP-011 and trigger/entity lifecycle surfaces out of incidental work.**
   - Why now: S12/S16/S25/S27 all create tempting product pressure around triggers, emergency authority, auth, and lifecycle.
   - Expected artifact: no code unless a successor decision selects one of those surfaces; prompts should name them as forbidden work.
   - Scope: ongoing guardrail.
   - Stop condition: any ordinary implementation prompt starts using production auth/group claims, general trigger execution, or entity lifecycle as assumed baseline.

5. **Prepare first scenario-probe prompts from NW-023, but sequence them behind the right baseline evidence.**
   - Why now: S00 and S19 are selected as the first scenario-grade probes, while S21/S27 are intentionally gated on projection/mobile evidence.
   - Expected artifact: bounded prompts for S00 and S19 runtime probes, each naming BAR rows, contracts touched, forbidden work, and stop conditions.
   - Scope: SESSION.
   - Stop condition: a probe requires trigger execution, auto-resolution, resolver reassignment, production auth authority, entity lifecycle, field-level sensitivity, new envelope fields/types, or new scope mechanisms.
