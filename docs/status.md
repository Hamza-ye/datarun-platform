# Platform Status

> Living state tracker. Updated in-place as work progresses.

**Last updated**: 2026-05-22 (IDR-022 drafted)

---

## Current Phase

**Phase 3: Configuration** — **COMPLETE** (including 3d close-out and 3e envelope-type retrofit)

| Sub-phase | Status | Notes |
|-----------|--------|-------|
| **3a: Shapes + Config Delivery** | **Complete** | 80 server + 33 mobile tests. |
| **3b: Expressions + DtV** | **Complete** | 148 server + 47 mobile tests. |
| **3c: Config Packager + Full Pipeline** | **Complete** | 153 server + 54 mobile tests. |
| **3d: Close-out** | **Complete** | 153 server + 67 mobile tests. activity_ref plumbing, sensitivity surface on device, ContextResolver. |
| **3e: Envelope Type Vocabulary Retrofit** | **Complete** | 164 server + 72 mobile tests. Executes [ADR-007](adrs/adr-007-envelope-type-closure.md): envelope `type` closed at 6 values; four identity/integrity facts are platform-bundled shapes. |

**Phase 4: Workflow & Policies** — **NOT STARTED**

Phase 4.0 (role-action enforcement) was drafted and rolled back. IDR-020 has now been rewritten as [Pattern State Machine Representation](decisions/idr-020-pattern-state-machine-representation.md), grounded in `docs/architecture/patterns.md` and `docs/exploration/28-pattern-inventory-walkthrough.md`. IDR-021 has been drafted as [Role-Action Enforcement Model](decisions/idr-021-role-action-enforcement-model.md), explicitly excluding FP-005 backfill/audit scope. IDR-022 has been drafted as [Flag Severity + Domain Uniqueness](decisions/idr-022-flag-severity-and-domain-uniqueness.md). Phase 4 implementation still needs a Phase 4 spec before code begins.

### Carried architectural debt — ADR-007 + Phase 3e retrofit

A Phase 3d close-out audit (2026-04-21) found that Phases 1–2 persisted four string literals (`conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`) as envelope `type` values, contradicting ADR-4 S3's closed 6-type vocabulary. [ADR-007](adrs/adr-007-envelope-type-closure.md) is the current authority: those four strings are internal **shape** names, not envelope types. Phase 3e landed the code retrofit. New code must not key on those strings as envelope types.

### Flagged positions register (living)

[`docs/flagged-positions.md`](flagged-positions.md) — deferred verification items and quiet positions that must not be forgotten. State as of 2026-05-22:

| FP# | Item | Blocks | Severity | Status |
|-----|------|--------|:--------:|--------|
| FP-001 | `role_stale` projection-derived role verification | IDR-021 | A | **RESOLVED** |
| FP-002 | `subject_lifecycle` table read-discipline audit | Phase 4 | B | **RESOLVED** |
| FP-003 | Envelope schema parity test | — | C | **RESOLVED** (EnvelopeSchemaParityTest) |
| FP-004 | `assignment_ref` as potential future envelope field | future assignment-targeting ADR/work | B | **OPEN** |
| FP-005 | Scoped pull temporal anchor and subject-history backfill | Phase 4 `ongoing_resolution` | A | **IN_PROGRESS** (routed; IDR-021 drafting unblocked) |

**Rule R-4**: before drafting a new IDR or starting a new phase, read the register end-to-end. Items whose `Blocks:` field names the upcoming work must be resolved or explicitly re-deferred.

### Previous Phases

| Phase | Status | Tests |
|-------|--------|-------|
| **0: Core Loop** | Complete | Foundation |
| **1: Identity & Integrity** | Complete | 64 total |
| **2: Authorization & Multi-Actor** | Complete | 80 server + 22 mobile |
| **3a: Shapes + Config Delivery** | Complete | 80 server + 33 mobile |
| **3b: Expressions + DtV** | Complete | 148 server + 47 mobile |
| **3c: Config Packager + Full Pipeline** | Complete | 153 server + 54 mobile |
| **3d: Close-out** | Complete | 153 server + 67 mobile |
| **3e: Envelope Type Vocabulary Retrofit** | Complete | 164 server + 72 mobile |

---

## What's Built

- `contracts/` — envelope schema (11 fields, closed 6-type envelope vocabulary per ADR-4 S3 and ADR-007, Draft 2020-12), sync protocol (extended: device_id, last_pull_watermark, flags_raised), shape schemas: assignment_created/v1, assignment_ended/v1, conflict_detected/v1, conflict_resolved/v1, subjects_merged/v1, subject_split/v1 (latter four are platform-bundled internal shapes per ADR-007)
- `server/` — Spring Boot app: event store, sync push/pull, subject projection, envelope validation
- `server/authorization/` — AssignmentService (create/end with S5 scope-containment), ScopeResolver (authority reconstruction from event timeline, 3 scope types), ActorTokenInterceptor (Bearer token auth on pull), ActorTokenRepository (SecureRandom 32-byte hex tokens, revocation), LocationRepository (materialized path hierarchy), SubjectLocationRepository, ActiveAssignment (isActive, containsGeographically/Subject/Activity), WebConfig (interceptor on /api/sync/pull only), REST controllers for assignments/locations/tokens
- `server/identity/` — ServerIdentity (env var + DB fallback, SEQUENCE-backed device_seq), IdentityLifecycleProjection (event-derived lifecycle), SubjectAliasProjection (rebuildable `subject_aliases` projection), IdentityService (merge/split with subject-scoped advisory locking), IdentityController (REST endpoints)
- `server/integrity/` — ConflictDetector (per-event W_effective detection + stale_reference detection + auth CD: scope_violation, temporal_authority_expired, role_stale), ConflictSweepJob (5-min stateless sweep), ConflictResolutionService (resolve: accepted/rejected/reclassified + manual identity_conflict flags), ConflictController (REST: resolve, list flags, create identity_conflict)
- `server/sync/` — Two-Tx pipeline (TransactionTemplate: Tx1 persist, Tx2 identity CD + auth CD + flags). Pull: scope-filtered with post-query activity + subject_list filtering (AND within assignment, OR across assignments)
- `contracts/flag-catalog.md` — 9 flag-category slots: implemented identity/authorization categories, Phase 4 `domain_uniqueness_violation` and `transition_violation`, plus one reserved slot
- `server/subject/` — SubjectProjection with flag exclusion + alias resolution + assignment_changed exclusion (CTE-based, LEFT JOIN subject_aliases)
- `server/subject/` — SubjectController with alias-aware event retrieval (includes events from all alias chains)
- `server/event/` — EventRepository extended: location_path denormalization on insert, findSinceScoped (3-category OR: geo subjects, own assignments, system events), findByType
- Migration V1: events table (BIGSERIAL sync_watermark)
- Migration V2: server_identity table, server_device_seq SEQUENCE, device_sync_state table
- Migration V3: subject_aliases table (with CHECK constraint)
- Migration V4: locations (materialized path), actor_tokens, subject_locations, events.location_path column, covering index (idx_events_scoped_pull), assignment expression index
- Admin UI: subject list, flag list, assignment list, assignment creation form, location hierarchy browser
- `docker-compose.yml` — full stack dev setup
- `docker-compose.test.yml` — test DB with host networking
- `.github/workflows/server-ci.yml` — GitHub Actions CI

**Repository**: https://github.com/Hamza-ye/datarun-platform.git

---

## What's Next

**Phase 4: Workflow & Policies** — next phase. IDR-020, IDR-021, and IDR-022 are drafted/active; remaining prep is the Phase 4 spec.

- IDR-021 (Role-Action Enforcement Model) is drafted. It builds on FP-001's role timeline check and keeps FP-005 out of role-action scope: live sync stays request-time scoped, subject-history backfill is separate Phase 4 `ongoing_resolution` work, and audit/historical pull is out of Phase 4 live sync unless a successor decision introduces a separate pull class/API.
- IDR-022 (Flag Severity + Domain Uniqueness) is drafted. It defines deployment-wide L0 severity overrides through `flag_severity_overrides`, keeps resolvability platform-owned, defines shape-declared `domain_uniqueness_violation`, and keeps FP-005 backfill/audit behavior out of scope.
- Phase spec: `docs/implementation/phases/phase-4.md` (once written)

### Test Debt (carried from Phase 3)
- Multi-version PE fixture
- activity_ref auto-population
- Widget-level form tests

---

## Blockers

_(None)_

---

## Active Decisions

| Decision | Status | Reference |
|----------|--------|-----------|
| IDR-020: Pattern State Machine Representation | **ACTIVE** | [idr-020-pattern-state-machine-representation.md](decisions/idr-020-pattern-state-machine-representation.md) |
| IDR-021: Role-Action Enforcement Model | **ACTIVE** | [idr-021-role-action-enforcement-model.md](decisions/idr-021-role-action-enforcement-model.md) |
| IDR-022: Flag Severity + Domain Uniqueness | **ACTIVE** | [idr-022-flag-severity-and-domain-uniqueness.md](decisions/idr-022-flag-severity-and-domain-uniqueness.md) |
