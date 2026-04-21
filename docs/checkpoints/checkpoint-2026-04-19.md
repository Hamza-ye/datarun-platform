# Project Checkpoint — 2026-04-19 (Phase 2 Complete)

---

## 1. Bearing

- **Phase**: Phase 2 (Authorization & Multi-Actor) — **COMPLETE**
- **Momentum**: `ADVANCING` — All 3 sub-phases delivered (2a, 2b, 2c). 9 acceptance criteria met. 90 tests (64 server + 26 mobile), 0 failures.
- **Last milestone**: Phase 2c auth flags + sync hardening. Scope_violation, temporal_authority_expired, and role_stale detection live in push pipeline. Selective-retain purge on mobile. Flag catalog documented.
- **Horizon**: Phase 3 (Configuration) — domain-agnosticism. Requires writing `phase-3.md` before implementation starts.

Phase 2 was the largest phase so far. It added a full authorization layer on top of the Phase 0/1 event-sourced core: assignment-based access, geographic hierarchy with materialized paths, scope-filtered sync, multi-actor visibility, actor token authentication, auth conflict detection (3 categories), selective-retain on mobile, and admin UI for assignments/locations/tokens. 16 IDRs now recorded across all phases. Zero regressions against Phase 0/1 tests.

---

## 2. Artifact Map

### Platform Repository (`datarun-platform/`)

| Module | Status | Key artifacts |
|--------|--------|---------------|
| `contracts/` | ACTIVE | Envelope schema (11 fields, 10 event types), sync protocol, assignment schemas, `flag-catalog.md` (6 categories) |
| `server/` | ACTIVE | 64 tests. Event store, sync (two-Tx pipeline), projection (CTE + alias + flag exclusion), validation, admin UI |
| `server/authorization/` | **NEW (Phase 2)** | AssignmentService, ScopeResolver, ActorTokenInterceptor, LocationRepository, SubjectLocationRepository |
| `server/integrity/` | ACTIVE | ConflictDetector (identity CD + auth CD: 6 flag categories), ConflictSweepJob, ConflictResolutionService |
| `server/identity/` | STABLE | ServerIdentity, AliasCache, IdentityService (merge/split) |
| `mobile/` | ACTIVE | 26 tests. Event store (selective-retain purge), sync service, projection engine, form engine |
| `admin/` | ACTIVE | Thymeleaf: subjects, flags, assignments, locations, tokens |
| CI | ACTIVE | GitHub Actions with postgres:16-alpine service container |

### Docs Repository (`datarun/`)

| Area | Status | Notes |
|------|--------|-------|
| `scenarios/` | SETTLED | S00–S14 exercised. S15, S16, S18 deferred. |
| `architecture/` | SETTLED | 6 files: README, primitives (11), patterns (4), contracts (21), cross-cutting (8), boundary (29). |
| `adrs/` | SETTLED | 5 ADRs, all DECIDED. 52 sub-decisions. Unchanged since consolidation. |
| `decisions/` | ACTIVE | 16 IDRs (4 per sub-phase average). All active. |
| `implementation/plan.md` | NEEDS UPDATE | Phase 2 status line still shows "Not started". |
| `implementation/phases/phase-2.md` | ACTIVE | Spec complete, 2a+2b journals written. 2c journal pending. |
| `checkpoints/` | ACTIVE | This is the 6th checkpoint. |

### Test Inventory

| Phase | Server tests | Mobile tests | Total |
|-------|-------------|-------------|-------|
| Phase 0 (Core Loop) | 13 | 14 | 27 |
| Phase 1 (Identity) | 31 | 6 | 37 |
| Phase 2a (Assignment) | 7 | 0 | 7 |
| Phase 2b (Multi-Actor) | 7 | 2 | 9 |
| Phase 2c (Auth Flags) | 6 | 4 | 10 |
| **Total** | **64** | **26** | **90** |

---

## 3. Decision Board

### IDRs by Phase

| Phase | IDRs | Key decisions |
|-------|------|---------------|
| 0 | IDR-001 through IDR-006 | Test infra (docker-compose host networking), pg_idkit, snake_case JSON, networknt validator, GitHub Actions CI, Thymeleaf admin |
| 1 | IDR-007 through IDR-012 | Watermark concurrency, server as event producer, alias table, separate-Tx CD, manual identity conflicts, sqflite in-memory path |
| 2 | IDR-013 through IDR-016 | Assignment payload design, materialized path locations, denormalized location_path sync, actor token table |

All 16 IDRs active. No reversals, no superseded decisions.

### Architecture Decision Status

All 5 ADRs unchanged since consolidation (checkpoint 2026-04-14). Phase 2 exercised ADR-3 (Authorization & Sync) fully:

- S1 (assignment-based access) — implemented via AssignmentService + ScopeResolver
- S2 (sync=scope) — implemented via scope-filtered pull with 3-category OR query
- S3 (authority-as-projection) — implemented via event-timeline reconstruction in ScopeResolver
- S4 (alias-respects-original-scope) — implemented via SubjectLocationRepository + alias-aware projection
- S5 (scope-containment) — enforced on assignment creation

---

## 4. Open Fronts

### 1. Phase 3 spec — BLOCKING

Phase 3 (Configuration) cannot start without `phase-3.md`. Requires sub-phase breakdown, quality gates, module scope, and DD slots for 6 IG items (IG-2, IG-6, IG-7, IG-8, IG-9, IG-15).

### 2. Docs repo commit — MINOR

Platform repo has Phase 2c pushed. Docs repo's last commit covers Phase 2b journal only. Phase 2c journal, status updates, and this checkpoint need to be committed.

### 3. plan.md Phase 2 status — MINOR

`docs/implementation/plan.md` Phase 2 status line still shows "Not started" in the phase status table.

---

## 5. Condition Ledger

### 5a. Viability GO Conditions

| # | Condition | Status | Evidence |
|---|-----------|--------|----------|
| 1 | Configuration boundary among first major architecture decisions | **MET** | ADR-004 decided. |
| 2 | S12 requires strict scoping | **MET** | ADR-004 S5, S12 budgets. |
| 3 | Phase 2 scenarios (S15, S16, S18) remain deferred | **MET** | All three deferred. Not exercised by Phase 2 implementation. |

### 5b. Phase 2 Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| 1 | Scope-filtered sync: each actor's pull returns exactly authorized events | **PASS** — tested with 3+ actors at different scope levels (ScopeFilteredSyncIntegrationTest, MultiActorScopeIntegrationTest) |
| 2 | Supervisor visibility | **PASS** — supervisor pull returns subordinate events (MultiActorScopeIntegrationTest) |
| 3 | Assignment lifecycle: create/end, PE reconstructs authority | **PASS** — AssignmentService + ScopeResolver timeline reconstruction |
| 4 | Scope-containment enforcement (S5) | **PASS** — creation rejected when scope exceeds creator's |
| 5 | Authorization flags: 3 categories detected with correct resolvability | **PASS** — AuthFlagIntegrationTest (6 tests) |
| 6 | Selective-retain: mobile purges out-of-scope, retains own | **PASS** — selective_retain_test.dart (4 tests) |
| 7 | No regression: Phase 0+1 tests pass | **PASS** — all 64 server + 26 mobile pass |
| 8 | Actor identification: unauthenticated requests rejected | **PASS** — ActorTokenInterceptor on /api/sync/pull |
| 9 | Performance: supervisor sync < 5s, authority reconstruction < 50ms/event | **PASS** — well under thresholds |

### 5c. ADR Escape Hatch Conditions

| Escape Hatch | Status | Change |
|-------------|--------|--------|
| B→C: materialized views (projection > 200ms) | SAFE | Directionally validated (spike). No change. |
| Auto-resolution for state-change flags | SAFE | No change. |
| Actor-sequence counter (cross-device) | SAFE | No change. |
| Authority_context envelope extension (> 50ms/event) | SAFE | Authority reconstruction well under threshold. |
| L3 expressiveness ceiling | SAFE | Not yet exercised (Phase 3). |
| Complexity budget appeals | SAFE | Not yet exercised (Phase 3). |
| Deprecation accumulation | SAFE | Not yet exercised (Phase 3). |
| Pattern inventory insufficient | SAFE | Not yet exercised (Phase 4). |
| Source-only flagging misses contamination | SAFE | Not yet exercised (Phase 4). |
| Auto-resolution masks issues | SAFE | Not yet exercised (Phase 4). |
| context.* projection bug | SAFE | Not yet exercised (Phase 3). |

---

## 6. Risk Pulse

### Active Risks

| Risk | Severity | Trigger | Mitigation |
|------|----------|---------|------------|
| Authority reconstruction slow at scale | LOW | > 50ms/event | Phase 2 confirms well under threshold. Per-actor caching available if needed. |
| Accept-and-flag backlog overwhelm | LOW | Bulk reconfig + offline workers | Auto-resolution (Phase 4). Batch resolution available now. |
| Phase 3 expression language complexity | MEDIUM | Grammar design leads to unbounded expressiveness | ADR-4 hard budgets (3 predicates, depth-2 DAG, 5 triggers/type). IG-15 deferred to Phase 3. |
| Mobile DB migration complexity | LOW | Schema changes in Phase 3 | SQLite migration path established (version 1→2→3 working). |

### Resolved Since Last Checkpoint

| Risk | Resolution |
|------|-----------|
| Scope-filtered sync performance | Materialized path + covering index = sub-second for 1000 subjects |
| Auth flag detection ordering | Temporal checked before scope (test verifies no misclassification) |
| Selective-retain data loss | Own-device events always retained; purge only non-own, non-system events |

---

## 7. Progress Assessment

### The Arc

```
Phase 0: Core Loop          ████████████████████ COMPLETE ✓  (13 commits)
Phase 1: Identity            ████████████████████ COMPLETE ✓  (64 tests total at exit)
Phase 2: Authorization       ████████████████████ COMPLETE ✓  (90 tests total at exit)
Phase 3: Configuration       ░░░░░░░░░░░░░░░░░░░░ NOT STARTED
Phase 4: Workflow            ░░░░░░░░░░░░░░░░░░░░ NOT STARTED
```

### Phase 2 Delivery Summary

| Sub-phase | Duration | Tests added | Key IDRs |
|-----------|----------|-------------|----------|
| 2a: Assignment + Basic Scope | 1 session | 7 server | IDR-013, 014, 015, 016 |
| 2b: Multi-Actor + Supervisor | 1 session | 7 server + 2 mobile | — |
| 2c: Auth Flags + Hardening | 1 session | 6 server + 4 mobile | — |

Phase 2 was delivered in 3 sessions with 4 design decisions upfront (all resolved before implementation). The decision-first approach (spec + DDs → build) continues to work well. All 4 DDs were resolved before 2a started; 2b and 2c needed no new DDs.

### Cumulative Metrics

| Metric | Phase 0 | Phase 1 | Phase 2 |
|--------|---------|---------|---------|
| Server tests | 13 | 44 | 64 |
| Mobile tests | 14 | 20 | 26 |
| Total tests | 27 | 64 | 90 |
| IDRs | 6 | 6 | 4 |
| ADRs exercised | ADR-1 (partial) | ADR-1, ADR-2 | ADR-3 |
| Platform commits | 5 | 3 | 5 |

### What the Previous Checkpoint's March Orders Produced

The previous checkpoint (2026-04-14) was pre-implementation. Its march order #2 was "implementation planning." Since then:
- `plan.md` and `execution-plan.md` written
- `ux-model.md` written
- Phase 0, 1, and 2 all completed
- 13 platform commits, 90 tests passing, 16 IDRs recorded

---

## 8. March Orders

### 1. Write `phase-3.md`

**Why now**: Phase 3 cannot start without a spec. The pattern is established: phase spec → DDs → build.

**What it involves**: Sub-phase breakdown for Configuration. Map primitives (Shape Registry, Expression Evaluator, Deploy-time Validator, Config Packager) to sub-phases. Define quality gates. Identify DD slots for IG-2, IG-6, IG-7, IG-8, IG-9, IG-15. Define exit criteria.

**Scope**: SINGLE SESSION

### 2. Commit docs repo

**Why now**: Platform repo is pushed through Phase 2c. Docs repo is one phase behind.

**What it involves**: Phase 2c journal in `phase-2.md`, update plan.md status table, commit this checkpoint. Straightforward bookkeeping.

**Scope**: IMMEDIATE (same session as checkpoint)

### 3. Begin Phase 3 implementation

**Why now**: After `phase-3.md` is written, the same decision-first pattern applies. ADR-4 is fully decided. The 6 IG items are identified. The platform's event store, sync pipeline, and admin UI are all extensible.

**Expected first deliverable**: Shape Registry — JSON schema authoring, storage as events, shape versioning with deprecation-only evolution. This is the foundation of domain-agnosticism.

**Scope**: MULTI-SESSION
