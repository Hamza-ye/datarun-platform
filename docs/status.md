# Platform Status

> Living state tracker. Updated in-place as work progresses.

**Last updated**: 2026-04-20

---

## Current Phase

**Phase 4: Workflow & Policies** — **IN PROGRESS** (4.0 complete)

| Sub-phase | Status | Notes |
|-----------|--------|-------|
| **4.0: Role-Action Enforcement** | **Complete** | 157 server + 54 mobile tests. Whitelist model: ConflictDetector checks actor role against activity.roles matrix, flags scope_violation with "Role-action mismatch" reason. 4 quality gate tests (G0.1–G0.4). |
| **4a: Patterns** | Not started | Vertical slice with capture_with_review. IDR-020 DECIDED. |
| **4b: Triggers** | Not started | |
| **4c: CV + E2E** | Not started | |

**Deferred from 4.0 → 4a**: Mobile UI gating (WorkListScreen shape filtering + FormScreen action filtering by actor role). Server enforces via accept-and-flag; mobile filtering is presentation-only, natural fit with 4a's pattern-driven UI rework.

### Previous Phases

| Phase | Status | Tests |
|-------|--------|-------|
| **0: Core Loop** | Complete | Foundation |
| **1: Identity & Integrity** | Complete | 64 total |
| **2: Authorization & Multi-Actor** | Complete | 80 server + 22 mobile |
| **3a: Shapes + Config Delivery** | Complete | 80 server + 33 mobile |
| **3b: Expressions + DtV** | Complete | 148 server + 47 mobile |
| **3c: Config Packager + Full Pipeline** | Complete | 153 server + 54 mobile |
| **4.0: Role-Action Enforcement** | Complete | 157 server + 54 mobile |

---

## What's Built

- `contracts/` — envelope schema (11 fields, 10 event types, Draft 2020-12), sync protocol (extended: device_id, last_pull_watermark, flags_raised), assignment schemas (assignment_created/v1, assignment_ended/v1)
- `server/` — Spring Boot app: event store, sync push/pull, subject projection, envelope validation
- `server/authorization/` — AssignmentService (create/end with S5 scope-containment), ScopeResolver (authority reconstruction from event timeline, 3 scope types), ActorTokenInterceptor (Bearer token auth on pull), ActorTokenRepository (SecureRandom 32-byte hex tokens, revocation), LocationRepository (materialized path hierarchy), SubjectLocationRepository, ActiveAssignment (isActive, containsGeographically/Subject/Activity), WebConfig (interceptor on /api/sync/pull only), REST controllers for assignments/locations/tokens
- `server/identity/` — ServerIdentity (env var + DB fallback, SEQUENCE-backed device_seq), AliasCache (ConcurrentHashMap, loaded at startup), IdentityService (merge/split with DD-3 row-level locking), IdentityController (REST endpoints)
- `server/integrity/` — ConflictDetector (per-event W_effective detection + stale_reference detection + auth CD: scope_violation, temporal_authority_expired, role_stale), ConflictSweepJob (5-min stateless sweep), ConflictResolutionService (resolve: accepted/rejected/reclassified + manual identity_conflict flags), ConflictController (REST: resolve, list flags, create identity_conflict)
- `server/sync/` — Two-Tx pipeline (TransactionTemplate: Tx1 persist, Tx2 identity CD + auth CD + flags). Pull: scope-filtered with post-query activity + subject_list filtering (AND within assignment, OR across assignments)
- `contracts/flag-catalog.md` — 6 flag categories (3 identity + 3 authorization) with detection ordering and resolvability
- `server/subject/` — SubjectProjection with flag exclusion + alias resolution + assignment_changed exclusion (CTE-based, LEFT JOIN subject_aliases)
- `server/subject/` — SubjectController with alias-aware event retrieval (includes events from all alias chains)
- `server/event/` — EventRepository extended: location_path denormalization on insert, findSinceScoped (3-category OR: geo subjects, own assignments, system events), findByType
- Migration V1: events table (BIGSERIAL sync_watermark)
- Migration V2: server_identity table, server_device_seq SEQUENCE, device_sync_state table
- Migration V3: subject_aliases table (with CHECK constraint), subject_lifecycle table
- Migration V4: locations (materialized path), actor_tokens, subject_locations, events.location_path column, covering index (idx_events_scoped_pull), assignment expression index
- Admin UI: subject list, flag list, assignment list, assignment creation form, location hierarchy browser
- `docker-compose.yml` — full stack dev setup
- `docker-compose.test.yml` — test DB with host networking
- `.github/workflows/server-ci.yml` — GitHub Actions CI

**Repository**: https://github.com/Hamza-ye/datarun-platform.git

---

## What's Next

**Phase 4a: Patterns** — vertical slice with `capture_with_review` pattern (IDR-020 DECIDED).

### Test Debt (carried from Phase 3)
- Multi-version PE fixture
- activity_ref auto-population
- Widget-level form tests

### Deferred Items
- Mobile UI gating by role (deferred from 4.0 → 4a)
- Performance benchmark (tracked since Phase 3 boundary)

---

## Blockers

_(None)_

---

## Active Decisions

| Decision | Status | Reference |
|----------|--------|-----------|
| IDR-020: Pattern State Machine Representation | DECIDED | `design/docs/decisions/idr-020-pattern-state-machine-representation.md` |
| DD-2: Lock — needs IDR-021 (state change events) | OPEN | Phase 4 spec |
