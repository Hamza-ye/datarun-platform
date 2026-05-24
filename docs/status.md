# Platform Status

> Living state tracker. Updated in-place as work progresses.

**Last updated**: 2026-05-24 (Phase 4.5 enabled-binding pattern-state projection landed; FP-005/FP-009 remain open)

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

**Phase 4: Workflow & Policies** — **IN PROGRESS** (Phase 4.5 pattern-state projection landed)

Phase 4.0 (role-action enforcement) was drafted and rolled back. IDR-020 is active as [Pattern State Machine Representation](decisions/idr-020-pattern-state-machine-representation.md), grounded in `docs/architecture/patterns.md` and `docs/exploration/28-pattern-inventory-walkthrough.md`. IDR-021 is active as [Role-Action Enforcement Model](decisions/idr-021-role-action-enforcement-model.md), explicitly excluding FP-005 backfill/audit scope. IDR-022 is active as [Flag Severity + Domain Uniqueness](decisions/idr-022-flag-severity-and-domain-uniqueness.md). IDR-023 is active as [Role-Action Domain Boundary and Assignment Administration](decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md), narrowing activity role-action to activity work actions and keeping assignment lifecycle authority out of `activities[*].roles`. IDR-024 is active as [Multi-axis Assignment Containment](decisions/idr-024-multi-axis-assignment-containment.md), deciding that assignment creation/end containment applies across geographic, subject-list, and activity axes with explicit bootstrap/root semantics. IDR-025 is active as [Pattern Definition Contract and Delivery](decisions/idr-025-pattern-definition-contract-and-delivery.md), deciding that platform pattern definitions are canonical `contracts/patterns` artifacts and are delivered to mobile through the atomic config package. The Phase 4 implementation spec is drafted at [phase-4.md](implementation/phases/phase-4.md). Phase 4.1 role-action work has landed across server-side config validation/package preservation, authoritative `role_stale` action-authority semantics, assignment-administration boundary checks, and mobile advisory role-action gating. Phase 4.2 flag severity has landed across platform defaults, deployment-wide L0 overrides, package delivery, and server/mobile effective severity interpretation. IDR-024 / FP-007 assignment containment hardening has landed across multi-axis create/end authority, explicit initial bootstrap semantics, empty subject/activity rejection, and null-activity work-event authorization. FP-008 assignment command identity binding has landed so `/api/assignments` create/end evaluates authority for the authenticated token actor, not request-body actor IDs; the unauthenticated HTML admin assignment console is documented as development-only and no longer takes command actor IDs from assignment forms. Phase 4.3 domain uniqueness has landed across deploy-time shape validation, config package preservation, server accept-and-flag detection, duplicate-basis exclusion for unresolved flags, accepted-resolution re-inclusion, and mobile advisory duplicate checks. Phase 4.4 pattern registry/binding validation has landed across the platform-bundled binding metadata registry, deploy-time activity binding validation, config package preservation, and mobile raw binding preservation. IDR-025 pattern definition delivery has landed across canonical contract files, server resource loading, config package delivery, and mobile packaged-definition preservation. Phase 4.5 enabled-binding pattern-state projection has landed for `capture_with_review/v1`, `multi_step_approval/v1`, and `transfer_with_acknowledgment/v1` using rebuildable/on-demand server and mobile projection, shared fixtures, universal state fields, and unresolved/accepted/rejected flag exclusion semantics. `ongoing_resolution/v1` remains registered but not enabled until FP-005 closes. Phase 4 remains open: transition detection, FP-005 backfill, FP-009 resolver gating, and scenario-grade responsibility-binding coverage are not complete.

### Carried architectural debt — ADR-007 + Phase 3e retrofit

A Phase 3d close-out audit (2026-04-21) found that Phases 1–2 persisted four string literals (`conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`) as envelope `type` values, contradicting ADR-4 S3's closed 6-type vocabulary. [ADR-007](adrs/adr-007-envelope-type-closure.md) is the current authority: those four strings are internal **shape** names, not envelope types. Phase 3e landed the code retrofit. New code must not key on those strings as envelope types.

### Flagged positions register (living)

[`docs/flagged-positions.md`](flagged-positions.md) — deferred verification items and quiet positions that must not be forgotten. State as of 2026-05-24:

| FP# | Item | Blocks | Severity | Status |
|-----|------|--------|:--------:|--------|
| FP-001 | `role_stale` projection-derived role verification | IDR-021 | A | **RESOLVED** |
| FP-002 | `subject_lifecycle` table read-discipline audit | Phase 4 | B | **RESOLVED** |
| FP-003 | Envelope schema parity test | — | C | **RESOLVED** (EnvelopeSchemaParityTest) |
| FP-004 | `assignment_ref` as potential future envelope field | future assignment-targeting ADR/work | B | **OPEN** |
| FP-005 | Scoped pull temporal anchor and subject-history backfill | Phase 4 `ongoing_resolution` | A | **IN_PROGRESS** (routed; IDR-021 drafting unblocked) |
| FP-006 | `temporal_authority_expired` superseded-assignment false positive | Phase 4 role-action / detection ordering | A | **RESOLVED** |
| FP-007 | Multi-axis assignment containment and null-activity semantics | Phase 4 assignment-administration hardening | A | **RESOLVED** |
| FP-008 | Assignment command actor identity binding | Phase 4.3 entry / assignment command exposure | A | **RESOLVED** |
| FP-009 | Conflict resolver designation and single-writer resolution enforcement | Phase 4.6 `transition_violation`; resolver routing / auto-resolution | A | **OPEN** (not a Phase 4.5 blocker while projection stays flag-free) |
| FP-010 | Platform-bundled payload shape contract parity | platform payload shape changes / production contract-hygiene close-out | C | **OPEN** (not blocking landed Phase 4.5 projection because it consumes existing payload fields) |

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

- `contracts/` — envelope schema (11 fields, closed 6-type envelope vocabulary per ADR-4 S3 and ADR-007, Draft 2020-12), sync protocol (extended: push `device_id`/`last_pull_watermark`/`flags_raised`, pull bearer-token scope plus `config_version`/`has_more`), shape schemas: assignment_created/v1, assignment_ended/v1, conflict_detected/v1, conflict_resolved/v1, subjects_merged/v1, subject_split/v1 (latter four are platform-bundled internal shapes per ADR-007), pattern definition schema plus canonical platform pattern definitions under `contracts/patterns/`
- `server/` — Spring Boot app: event store, sync push/pull, subject projection, envelope validation
- `server/authorization/` — AssignmentService (create/end with IDR-024 multi-axis scope containment, explicit initial bootstrap path, and target-assignment end authority), ScopeResolver (authority reconstruction from event timeline, 3 scope types), ActorTokenInterceptor (Bearer token auth on sync pull/config and assignment APIs), ActorTokenRepository (SecureRandom 32-byte hex tokens, revocation), LocationRepository (materialized path hierarchy), SubjectLocationRepository, ActiveAssignment (isActive, containsGeographically/Subject/Activity with null activity not authorized by activity-restricted assignments), WebConfig (interceptor on `/api/sync/pull`, `/api/sync/config`, and `/api/assignments/**`), REST controllers for assignments/locations/tokens
- `server/identity/` — ServerIdentity (env var + DB fallback, SEQUENCE-backed device_seq), IdentityLifecycleProjection (event-derived lifecycle), SubjectAliasProjection (rebuildable `subject_aliases` projection), IdentityService (merge/split with subject-scoped advisory locking), IdentityController (REST endpoints)
- `server/integrity/` — ConflictDetector (per-event W_effective detection + stale_reference detection + auth CD: scope_violation, temporal_authority_expired, role_stale), DomainUniquenessDetector (`domain_uniqueness_violation` accept-and-flag for `shapes[*].uniqueness`), FlagCatalog (fixed resolvability + severity defaults), ConflictSweepJob (5-min stateless sweep), ConflictResolutionService (resolve: accepted/rejected/reclassified + manual identity_conflict flags + effective severity surfaces), ConflictController (REST: resolve, list flags, create identity_conflict)
- `server/sync/` — Two-Tx pipeline (TransactionTemplate: Tx1 persist, Tx2 identity CD + auth CD + flags). Pull: scope-filtered with post-query activity + subject_list filtering (AND within assignment, OR across assignments)
- `contracts/flag-catalog.md` — 9 flag-category slots: implemented identity/authorization categories, Phase 4 `domain_uniqueness_violation` and `transition_violation`, plus one reserved slot
- `server/subject/` — SubjectProjection with flag exclusion + alias resolution + assignment_changed exclusion (CTE-based, LEFT JOIN subject_aliases)
- `server/subject/` — SubjectController with alias-aware event retrieval (includes events from all alias chains)
- `server/event/` — EventRepository extended: location_path denormalization on insert, findSinceScoped (3-category OR: geo subjects, own assignments, system events), findByType
- `server/projection/` — rebuildable PatternStateProjection for enabled pattern bindings, deriving state from active activity bindings, contract-backed PatternRegistry definitions, and authoritative flag-resolution state without durable workflow tables
- `mobile/data/` — PatternProjectionEngine counterpart reading ConfigStore activity bindings plus packaged `pattern_definitions`, with shared fixture equivalence for enabled patterns
- Migration V1: events table (BIGSERIAL sync_watermark)
- Migration V2: server_identity table, server_device_seq SEQUENCE, device_sync_state table
- Migration V3: subject_aliases table (with CHECK constraint)
- Migration V4: locations (materialized path), actor_tokens, subject_locations, events.location_path column, covering index (idx_events_scoped_pull), assignment expression index
- Migration V6: deployment_config table for deployment-wide L0 `flag_severity_overrides`
- Admin UI: subject list, flag list, development-only assignment list/creation form, location hierarchy browser
- `docker-compose.yml` — full stack dev setup
- `docker-compose.test.yml` — test DB with host networking
- `.github/workflows/server-ci.yml` — GitHub Actions CI

**Repository**: https://github.com/Hamza-ye/datarun-platform.git

---

## What's Next

**Phase 4: Workflow & Policies** — active implementation. IDR-020, IDR-021, IDR-022, IDR-023, IDR-024, and IDR-025 are active; the Phase 4 implementation spec is drafted, and Phase 4.1 role-action, Phase 4.2 flag severity, and Phase 4.3 domain uniqueness have landed.

- IDR-021 (Role-Action Enforcement Model) is active. It builds on FP-001's role timeline check and keeps FP-005 out of role-action scope: live sync stays request-time scoped, subject-history backfill is separate Phase 4 `ongoing_resolution` work, and audit/historical pull is out of Phase 4 live sync unless a successor decision introduces a separate pull class/API.
- IDR-022 (Flag Severity + Domain Uniqueness) is active. It defines deployment-wide L0 severity overrides through `flag_severity_overrides`, keeps resolvability platform-owned, defines shape-declared `domain_uniqueness_violation`, and keeps FP-005 backfill/audit behavior out of scope.
- IDR-023 (Role-Action Domain Boundary and Assignment Administration) is active. It excludes `assignment_changed` from `activities[*].roles`; assignment create/end remains an online authority path, not an activity role-action work event.
- IDR-024 (Multi-axis Assignment Containment) is active. It closes the decision gap around assignment creation/end containment across `geographic`, `subject_list`, and `activity`, defines explicit bootstrap/root semantics, and states that activity-restricted assignments do not authorize ordinary null-activity work events.
- IDR-025 (Pattern Definition Contract and Delivery) is active. It makes `contracts/patterns/*.json` the canonical platform-owned pattern source, loads those definitions into the server registry, and delivers referenced definitions through the atomic config package for mobile runtime use.
- Phase spec: [docs/implementation/phases/phase-4.md](implementation/phases/phase-4.md) (drafted; tracks landed Phase 4.1 role-action gates, Phase 4.2 severity gates, Phase 4.3 uniqueness gates, Phase 4.4 registry/binding gates, and remaining Phase 4 gates)
- Landed implementation slices: Phase 4.1 role-action enforcement from IDR-021/IDR-023, including authoritative server `role_stale` action-authority semantics and mobile advisory role-action gating; Phase 4.2 flag severity defaults and deployment-wide `flag_severity_overrides` from IDR-022; IDR-024 / FP-007 assignment administration hardening; FP-008 assignment command identity binding; Phase 4.3 domain uniqueness schema, detector, and mobile advisory uniqueness from IDR-022; Phase 4.4 platform pattern registry and activity binding validation from IDR-020; IDR-025 pattern definition contract/package delivery; Phase 4.5 rebuildable pattern-state projection for enabled bindings. These do not implement transition violations, `ongoing_resolution`, resolver routing, conflict-resolution authority enforcement, auto-resolution, normal sync backfill, or durable workflow-state tables.
- Recommended next implementation slice: FP-005 subject-history backfill decision/implementation before any `ongoing_resolution/v1` projection or enablement. `transition_violation` emission remains blocked while FP-009 is open. FP-010 is a separate platform payload shape contract-hygiene follow-up and does not block current projection behavior unless future work changes platform-bundled payload shapes.
- Coverage gap recorded: Phase 4 still needs a scenario-grade P04 Responsibility Binding quality gate for coordinated campaigns with overlapping areas and mid-campaign reassignment. Existing primitive tests do not close that scenario gate, but it is not currently a blocker for Phase 4.4 registry/binding validation.

### Test Debt (carried from Phase 3)
- Multi-version PE fixture
- activity_ref auto-population
- Widget-level form tests

---

## Blockers

FP-005 remains `IN_PROGRESS` and still blocks Phase 4 `ongoing_resolution` implementation until subject-history backfill is specified and tested. FP-009 is `OPEN` and blocks Phase 4.6 `transition_violation` emission, resolver routing, conflict-resolution authority enforcement, auto-resolution, and Phase 4 close-out if resolver routing remains unresolved; it does not block Phase 4.5 pattern-state projection while that slice stays flag-free. FP-010 is `OPEN` as platform payload shape contract hygiene; it does not block Phase 4.5 unless the slice changes platform-bundled payload shapes or claims payload-shape contract parity. The P04 Responsibility Binding scenario-grade test gap is a Phase 4 coverage gate, not a current blocker for pattern-state projection.

---

## Active Decisions

| Decision | Status | Reference |
|----------|--------|-----------|
| IDR-020: Pattern State Machine Representation | **ACTIVE** | [idr-020-pattern-state-machine-representation.md](decisions/idr-020-pattern-state-machine-representation.md) |
| IDR-021: Role-Action Enforcement Model | **ACTIVE** | [idr-021-role-action-enforcement-model.md](decisions/idr-021-role-action-enforcement-model.md) |
| IDR-022: Flag Severity + Domain Uniqueness | **ACTIVE** | [idr-022-flag-severity-and-domain-uniqueness.md](decisions/idr-022-flag-severity-and-domain-uniqueness.md) |
| IDR-023: Role-Action Domain Boundary and Assignment Administration | **ACTIVE** | [idr-023-role-action-domain-boundary-and-assignment-administration.md](decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md) |
| IDR-024: Multi-axis Assignment Containment | **ACTIVE** | [idr-024-multi-axis-assignment-containment.md](decisions/idr-024-multi-axis-assignment-containment.md) |
| IDR-025: Pattern Definition Contract and Delivery | **ACTIVE** | [idr-025-pattern-definition-contract-and-delivery.md](decisions/idr-025-pattern-definition-contract-and-delivery.md) |
