# Platform Status

> Living state tracker. Updated in-place as work progresses.

**Last updated**: 2026-04-18

---

## Current Phase

**Phase 1: Identity & Integrity** — Conflict Detector, Identity Resolver, Integration

| Sub-phase | Status | Notes |
|-----------|--------|-------|
| **1a: Conflict Detector** | **Complete** | All 8 quality gates pass. 19 tests green (8 Phase 0 + 3 subject + 8 Phase 1a). |
| **1b: Identity Resolver** | **Complete** | All 8 quality gates pass. 27 tests green (19 prior + 8 Phase 1b). |
| **1c: Mobile + Admin + Integration** | **In progress** | Server-side resolution slice complete (36 tests green). Mobile PE, admin UI, E2E remaining. |

---

## What's Built

- `contracts/` — envelope schema (11 fields, 10 event types, Draft 2020-12), sync protocol (extended: device_id, last_pull_watermark, flags_raised)
- `server/` — Spring Boot app: event store, sync push/pull, subject projection, envelope validation
- `server/identity/` — ServerIdentity (env var + DB fallback, SEQUENCE-backed device_seq), AliasCache (ConcurrentHashMap, loaded at startup), IdentityService (merge/split with DD-3 row-level locking), IdentityController (REST endpoints)
- `server/integrity/` — ConflictDetector (per-event W_effective detection + stale_reference detection), ConflictSweepJob (5-min stateless sweep), ConflictResolutionService (resolve: accepted/rejected/reclassified + manual identity_conflict flags), ConflictController (REST: resolve, list flags, create identity_conflict)
- `server/sync/` — Two-Tx pipeline (TransactionTemplate: Tx1 persist, Tx2 CD + flags)
- `server/subject/` — SubjectProjection with flag exclusion + alias resolution (CTE-based, LEFT JOIN subject_aliases)
- `server/subject/` — SubjectController with alias-aware event retrieval (includes events from all alias chains)
- Migration V2: server_identity table, server_device_seq SEQUENCE, device_sync_state table
- Migration V3: subject_aliases table (with CHECK constraint), subject_lifecycle table
- `docker-compose.yml` — full stack dev setup
- `docker-compose.test.yml` — test DB with host networking
- `.github/workflows/server-ci.yml` — GitHub Actions CI

**Repository**: https://github.com/Hamza-ye/datarun-platform.git

---

## What's Next

1. Continue Phase 1c: Mobile + Admin + Integration
   - Mobile PE extended for aliases + flags
   - Admin flag resolution UI (Thymeleaf)
   - End-to-end multi-device scenario test
   - Projection equivalence test (server PE vs mobile PE)

---

## Blockers

_(None)_

---

## Active Decisions

_(None pending — Phase 1c will follow phase-1.md spec.)_
