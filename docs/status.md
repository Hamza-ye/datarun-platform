# Platform Status

> Living state tracker. Updated in-place as work progresses.

**Last updated**: 2026-04-19

---

## Current Phase

**Phase 2: Authorization & Multi-Actor** — Assignment Model, Scope Resolver, Scope-Filtered Sync

| Sub-phase | Status | Notes |
|-----------|--------|-------|
| **2a: Assignment Model + Basic Scope** | **Complete** | All 9 quality gates pass. 51 tests green (44 prior + 7 Phase 2a). |
| **2b: Multi-Actor Scope + Supervisor** | **Not started** | Next up: activity/subject_list scope, supervisor visibility, mobile sync with tokens, multi-actor E2E. |
| **2c: Auth Flags + Hardening** | **Not started** | scope_violation, role_stale, temporal_authority_expired flags. |

---

## What's Built

- `contracts/` — envelope schema (11 fields, 10 event types, Draft 2020-12), sync protocol (extended: device_id, last_pull_watermark, flags_raised), assignment schemas (assignment_created/v1, assignment_ended/v1)
- `server/` — Spring Boot app: event store, sync push/pull, subject projection, envelope validation
- `server/authorization/` — AssignmentService (create/end with S5 scope-containment), ScopeResolver (authority reconstruction from event timeline, 3 scope types), ActorTokenInterceptor (Bearer token auth on pull), ActorTokenRepository (SecureRandom 32-byte hex tokens, revocation), LocationRepository (materialized path hierarchy), SubjectLocationRepository, ActiveAssignment (isActive, containsGeographically/Subject/Activity), WebConfig (interceptor on /api/sync/pull only), REST controllers for assignments/locations/tokens
- `server/identity/` — ServerIdentity (env var + DB fallback, SEQUENCE-backed device_seq), AliasCache (ConcurrentHashMap, loaded at startup), IdentityService (merge/split with DD-3 row-level locking), IdentityController (REST endpoints)
- `server/integrity/` — ConflictDetector (per-event W_effective detection + stale_reference detection), ConflictSweepJob (5-min stateless sweep), ConflictResolutionService (resolve: accepted/rejected/reclassified + manual identity_conflict flags), ConflictController (REST: resolve, list flags, create identity_conflict)
- `server/sync/` — Two-Tx pipeline (TransactionTemplate: Tx1 persist, Tx2 CD + flags). Pull: scope-filtered (scoped query for geo-limited actors, unscoped fallback for unrestricted-geo actors)
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

1. Phase 2b: Multi-Actor Scope + Supervisor Visibility
   - `activity` and `subject_list` scope types with AND composition
   - Supervisor scope via geographic hierarchy containment
   - Multi-actor E2E scenario (3 actors, hierarchical visibility)
   - Mobile sync with actor token (Bearer header)
   - Performance validation of scope-filtered query under load

---

## Blockers

_(None)_

---

## Active Decisions

_(None pending — Phase 2b will follow phase-2.md spec.)_
