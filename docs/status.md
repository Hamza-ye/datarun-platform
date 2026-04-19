# Platform Status

> Living state tracker. Updated in-place as work progresses.

**Last updated**: 2026-04-20

---

## Current Phase

**Phase 3: Configuration** — **3a COMPLETE, 3b/3c remaining**

| Sub-phase | Status | Notes |
|-----------|--------|-------|
| **3a: Shapes + Config Delivery** | **Complete** | 80 server + 33 mobile tests. Shapes, activities, config endpoint, payload validation, admin UI (server). ConfigStore, shape.dart IDR-017 rewrite, sync config download, form engine, widget_mapper 10 field types (mobile). |
| **3b: Expressions + DtV** | Not started | IDR-018 expression grammar. Java + Dart evaluators in parallel. |
| **3c: Config Packager + Full Pipeline** | Not started | IDR-019 full pipeline E2E. |

### Previous Phases

| Phase | Status | Tests |
|-------|--------|-------|
| **0: Core Loop** | Complete | Foundation |
| **1: Identity & Integrity** | Complete | 64 total |
| **2: Authorization & Multi-Actor** | Complete | 80 server + 22 mobile |
| **3a: Shapes + Config Delivery** | Complete | 80 server + 33 mobile |

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

**Phase 3: Configuration** — Domain-agnosticism. Deployer-authored shapes, expression evaluator, config packaging.

Prerequisites before starting:
- Write `docs/implementation/phases/phase-3.md` (sub-phase breakdown, quality gates, module scope)
- Resolve IG items: IG-2, IG-6, IG-7, IG-8, IG-9, IG-15

Primitives to build:
- Shape Registry (full: authoring, versioning, deprecation-only evolution)
- Expression Evaluator (form L2 + trigger L3 contexts)
- Deploy-time Validator (hard budgets, composition rules)
- Config Packager (atomic delivery, at-most-2 versions)
- Admin UI: config authoring (shapes, activities, expressions)

Contracts exercised: C5, C12, C13, C14, C19, C20. ADR exercised: ADR-4 fully.

---

## Blockers

_(None)_

---

## Active Decisions

_(None pending — Phase 3 spec needs to be written before implementation begins.)_
