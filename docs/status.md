# Platform Status

> Living state tracker. Updated in-place as work progresses.

**Last updated**: 2026-06-04 (NW-039 principal-binding administration decision accepted)

---

## Current Routing

Use this section as the low-token bootstrap for new sessions.

- Phase 4 is complete; the completion audit is green.
- Post-Phase-4 stabilization now uses `docs/agent-working-surface/README.md` as the active agent working-surface router.
- Current implementation acceptance status belongs in `docs/agent-working-surface/baseline-acceptance-register.md`; do not treat legacy Phase 4 review drafts as active baseline truth.
- Current baseline standing: BAR-001 through BAR-015 are accepted, including BAR-010 config package delivery.
- Scenario runtime evidence now includes NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, and NW-033/S26. Use the backlog rows for exact evidence.
- Contract hygiene now includes root schemas for deployer-authored shape format and the server-emitted/mobile-consumed config package: `contracts/shape-format.schema.json` and `contracts/config-package.schema.json`.
- NW-037 accepted the bounded production-auth foundation: explicit `(issuer, subject) -> actor_id` principal binding, authenticated actor context, `/api/auth/me`, production-mode push actor binding, and mobile actor alignment. Use IDR-027 for the decision boundary and the NW-037 backlog row for evidence.
- NW-038 accepted the server-side OIDC/JWKS auth-provider boundary: configured asymmetric JWT validation by issuer, audience, and JWKS URI behind `AuthenticatedActorResolver`, with explicit `(issuer, subject) -> actor_id` binding as the only actor mapping. Use the NW-038 backlog row for evidence.
- FP-010 is resolved; platform payload schemas are runtime contracts, not deployer shape rows.
- NW-039 accepted IDR-028, selecting deployment-managed principal-binding provisioning as the first production administration path. It defines bootstrap, create, rotate, deactivate, rebind/correction, audit, idempotency/concurrency, and an append-only operation-history boundary with active binding projection/support rows.
- FP-011 remains open until NW-040 implements and tests production principal-binding provisioning; any group/claim authority model remains a separate future decision. NW-037 through NW-039 did not make groups, roles, JWT `actor_id`, or IdP claims direct platform authority.
- NW-040 is the next ready implementation slice: use `docs/agent-working-surface/prompts/NW-040-implement-production-principal-binding-provisioning.md` to implement deployment-managed binding manifest provisioning selected by IDR-028, including audit/idempotency/concurrency tests and continued group/claim/JWT `actor_id` non-authority.
- No baseline acceptance candidate is currently active. Entity lifecycle, trigger/reporting expansion, auto-resolution, resolver reassignment, online production binding-admin APIs, and new scope mechanisms remain future-decision follow-ups.
- Default implementer context is `AGENTS.md`, this section, the relevant section of `docs/implementation/module-interfaces.md`, and the exact contracts/code touched by the task.
- Historical phase detail, active decision text, architecture docs, scenarios, and exploration archives are not default context. Open them only when the task surface, a touched file, or a drift investigation routes you there.

---

## Current Phase And Historical Detail

**Phase 3: Configuration** — **COMPLETE** (including 3d close-out and 3e envelope-type retrofit)

| Sub-phase | Status | Notes |
|-----------|--------|-------|
| **3a: Shapes + Config Delivery** | **Complete** | 80 server + 33 mobile tests. |
| **3b: Expressions + DtV** | **Complete** | 148 server + 47 mobile tests. |
| **3c: Config Packager + Full Pipeline** | **Complete** | 153 server + 54 mobile tests. |
| **3d: Close-out** | **Complete** | 153 server + 67 mobile tests. activity_ref plumbing, sensitivity surface on device, ContextResolver. |
| **3e: Envelope Type Vocabulary Retrofit** | **Complete** | 164 server + 72 mobile tests. Executes [ADR-007](adrs/adr-007-envelope-type-closure.md): envelope `type` closed at 6 values; four identity/integrity facts are platform-bundled shapes. |

**Phase 4: Workflow & Policies** — **COMPLETE** (completion audit green)

Phase 4.0 (role-action enforcement) was drafted and rolled back. IDR-020 is active as [Pattern State Machine Representation](decisions/idr-020-pattern-state-machine-representation.md), grounded in `docs/architecture/patterns.md` and `docs/exploration/28-pattern-inventory-walkthrough.md`. IDR-021 is active as [Role-Action Enforcement Model](decisions/idr-021-role-action-enforcement-model.md), explicitly excluding FP-005 backfill/audit scope. IDR-022 is active as [Flag Severity + Domain Uniqueness](decisions/idr-022-flag-severity-and-domain-uniqueness.md). IDR-023 is active as [Role-Action Domain Boundary and Assignment Administration](decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md), narrowing activity role-action to activity work actions and keeping assignment lifecycle authority out of `activities[*].roles`. IDR-024 is active as [Multi-axis Assignment Containment](decisions/idr-024-multi-axis-assignment-containment.md), deciding that assignment creation/end containment applies across geographic, subject-list, and activity axes with explicit bootstrap/root semantics. IDR-025 is active as [Pattern Definition Contract and Delivery](decisions/idr-025-pattern-definition-contract-and-delivery.md), deciding that platform pattern definitions are canonical `contracts/patterns` artifacts and are delivered to mobile through the atomic config package. IDR-026 is active as [Conflict Resolver Routing and Single-Writer Resolution](decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md), and its FP-009 runtime enforcement has landed. IDR-027 is active as [Production Auth Principal-To-Actor Binding](decisions/idr-027-production-auth-principal-actor-binding.md); NW-037 landed its explicit principal-binding foundation and NW-038 landed OIDC/JWKS provider validation without resolving FP-011 or BAR-104. IDR-028 is active as [Production Principal-Binding Administration](decisions/idr-028-production-principal-binding-administration.md); NW-039 selected deployment-managed binding provisioning and unblocked NW-040 implementation while FP-011 and BAR-104 remain open. The Phase 4 implementation spec is at [phase-4.md](implementation/phases/phase-4.md). Phase 4.1 role-action work has landed across server-side config validation/package preservation, authoritative `role_stale` action-authority semantics, assignment-administration boundary checks, and mobile advisory role-action gating. Phase 4.2 flag severity has landed across platform defaults, deployment-wide L0 overrides, package delivery, and server/mobile effective severity interpretation. IDR-024 / FP-007 assignment containment hardening has landed across multi-axis create/end authority, explicit initial bootstrap semantics, empty subject/activity rejection, and null-activity work-event authorization. FP-008 assignment command actor identity binding has landed so `/api/assignments` create/end evaluates authority for the authenticated token actor, not request-body actor IDs; the unauthenticated HTML admin assignment console is documented as development-only and no longer takes command actor IDs from assignment forms. Phase 4.3 domain uniqueness has landed across deploy-time shape validation, config package preservation, server accept-and-flag detection, duplicate-basis exclusion for unresolved flags, accepted-resolution re-inclusion, and mobile advisory duplicate checks. Phase 4.4 pattern registry/binding validation has landed across the platform-bundled binding metadata registry, deploy-time activity binding validation, config package preservation, and mobile raw binding preservation. IDR-025 pattern definition delivery has landed across canonical contract files, server resource loading, config package delivery, and mobile packaged-definition preservation. Phase 4.5 enabled-binding pattern-state projection has landed for `capture_with_review/v1`, `multi_step_approval/v1`, `transfer_with_acknowledgment/v1`, and `ongoing_resolution/v1` using rebuildable/on-demand server and mobile projection, shared fixtures, universal state fields, and unresolved/accepted/rejected flag exclusion semantics. FP-005 subject-history backfill has landed as a separate `/api/sync/subject-history` surface with independent cursor pagination, request-time authorization on every page, merge-alias and split-lineage coverage, activity filtering, and no mutation of normal live-sync watermarks. `ongoing_resolution/v1` projection now consumes that backfill surface for newly assigned long-running subjects. FP-009 runtime resolver enforcement has landed across resolver designation, bearer-bound conflict APIs, canonical resolution checks, and unauthorized-resolution flagging. Phase 4.6 transition detection has landed with IDR-020 transition matching, IDR-026 resolver routing, legal-transition-only accepted re-inclusion, and no auto-resolution. The P04 scenario-grade Responsibility Binding gate has landed with coordinated campaign, overlapping responsibility area, mid-campaign reassignment, scope-filtered sync, and role-action boundary coverage. Post-Phase-4 scenario probes now add accepted runtime evidence for S19 offline stale authority, S00 structured capture, S21 scoped supervisor review, S23 setup/config, S26 reporting/aggregate oversight, and S27 non-health logistics transfer. The completion audit found all applicable Phase 4 gates green; S06/entity lifecycle remains explicitly deferred.

### Carried architectural debt — ADR-007 + Phase 3e retrofit

A Phase 3d close-out audit (2026-04-21) found that Phases 1–2 persisted four string literals (`conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`) as envelope `type` values, contradicting ADR-4 S3's closed 6-type vocabulary. [ADR-007](adrs/adr-007-envelope-type-closure.md) is the current authority: those four strings are internal **shape** names, not envelope types. Phase 3e landed the code retrofit. New code must not key on those strings as envelope types.

### Flagged positions register (living)

[`docs/flagged-positions.md`](flagged-positions.md) — deferred verification items and quiet positions that must not be forgotten. State as of 2026-05-25:

| FP# | Item | Blocks | Severity | Status |
|-----|------|--------|:--------:|--------|
| FP-001 | `role_stale` projection-derived role verification | IDR-021 | A | **RESOLVED** |
| FP-002 | `subject_lifecycle` table read-discipline audit | Phase 4 | B | **RESOLVED** |
| FP-003 | Envelope schema parity test | — | C | **RESOLVED** (EnvelopeSchemaParityTest) |
| FP-004 | `assignment_ref` as potential future envelope field | future assignment-targeting ADR/work | B | **OPEN** |
| FP-005 | Scoped pull temporal anchor and subject-history backfill | Phase 4 `ongoing_resolution` | A | **RESOLVED** |
| FP-006 | `temporal_authority_expired` superseded-assignment false positive | Phase 4 role-action / detection ordering | A | **RESOLVED** |
| FP-007 | Multi-axis assignment containment and null-activity semantics | Phase 4 assignment-administration hardening | A | **RESOLVED** |
| FP-008 | Assignment command actor identity binding | Phase 4.3 entry / assignment command exposure | A | **RESOLVED** |
| FP-009 | Conflict resolver designation and single-writer resolution enforcement | Phase 4.6 `transition_violation`; runtime resolver enforcement / auto-resolution | A | **RESOLVED** |
| FP-010 | Platform-bundled payload shape contract parity | platform payload shape changes / production contract-hygiene close-out | C | **RESOLVED** |
| FP-011 | Authentication principal-to-actor mapping and group non-authority | production Keycloak/OIDC/JWT integration; group/claim authority models | B | **OPEN** (not an FP-009 blocker while conflict APIs stay bearer actor-bound) |

**Rule R-4**: before drafting a new IDR or starting a new phase, grep/read the register for items whose `Blocks:` field names the upcoming work. Blocking items must be resolved or explicitly re-deferred.

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

- `contracts/` — envelope schema (11 fields, closed 6-type envelope vocabulary per ADR-4 S3 and ADR-007, Draft 2020-12), sync protocol (extended: push `device_id`/`last_pull_watermark`/`flags_raised` plus production-auth actor binding, pull bearer-token scope plus `config_version`/`has_more`, and separate subject-history backfill), deployer `shape-format` schema, server/mobile config-package schema, platform payload shape schemas: assignment_created/v1, assignment_ended/v1, conflict_detected/v1, conflict_resolved/v1, subjects_merged/v1, subject_split/v1 (bundled into server runtime for payload validation; not deployer shape registry rows), pattern definition schema plus canonical platform pattern definitions under `contracts/patterns/`
- `server/` — Spring Boot app: event store, sync push/pull, subject projection, envelope validation
- `server/authorization/` — AssignmentService (create/end with IDR-024 multi-axis scope containment, explicit initial bootstrap path, and target-assignment end authority), ScopeResolver (authority reconstruction from event timeline, 3 scope types), AuthenticatedActorResolver (dev-token or IDR-027 `(issuer, subject) -> actor_id` binding), ActorTokenInterceptor (Bearer credential auth on `/api/auth/me`, sync push/pull/subject-history/config, assignment, and conflict APIs), ActorTokenRepository (SecureRandom 32-byte hex tokens, revocation), AuthPrincipalBindingRepository, JwtPrincipalTokenValidator (local HS256 test/foundation validator behind the resolver boundary), OidcJwksTokenValidator (configured issuer/audience/JWKS asymmetric JWT validation behind the same resolver boundary), LocationRepository (materialized path hierarchy), SubjectLocationRepository, ActiveAssignment (isActive, containsGeographically/Subject/Activity with null activity not authorized by activity-restricted assignments), WebConfig, REST controllers for auth-me/assignments/locations/dev-token admin
- `server/identity/` — ServerIdentity (env var + DB fallback, SEQUENCE-backed device_seq), IdentityLifecycleProjection (event-derived lifecycle), SubjectAliasProjection (rebuildable `subject_aliases` projection), IdentityService (merge/split with subject-scoped advisory locking), IdentityController (REST endpoints)
- `server/integrity/` — ConflictDetector (per-event W_effective detection + stale_reference detection + auth CD: scope_violation, temporal_authority_expired, role_stale), DomainUniquenessDetector (`domain_uniqueness_violation` accept-and-flag for `shapes[*].uniqueness`), TransitionViolationDetector (`transition_violation` accept-and-flag for active pattern bindings), ResolverRoutingService (IDR-026 resolver designation), FlagCatalog (fixed resolvability + severity defaults), ConflictSweepJob (5-min stateless sweep), ConflictResolutionService (canonical single-writer resolve: accepted/rejected/reclassified + unauthorized-resolution flagging + manual identity_conflict flags + effective severity surfaces), ConflictController (bearer-bound REST: resolve, actor-filtered flag list, create identity_conflict)
- `server/sync/` — Two-Tx pipeline (TransactionTemplate: Tx1 persist, Tx2 identity CD + auth CD + domain uniqueness + transition detection + flags). Production-auth push requires bearer-authenticated actor binding and rejects client-authored `system:*`, missing, invalid, or mismatched `actor_ref.id` before persistence. Pull: scope-filtered with post-query activity + subject_list filtering (AND within assignment, OR across assignments). Subject-history backfill: separate bearer-authenticated `/api/sync/subject-history` cursor surface for subject/activity projection repair; it checks current assignment authorization on every page and does not mutate `device_sync_state`.
- `contracts/flag-catalog.md` — 9 flag-category slots: implemented identity/authorization categories, Phase 4 `domain_uniqueness_violation` and `transition_violation`, plus one reserved slot
- `server/subject/` — SubjectProjection with flag exclusion + alias resolution + assignment_changed exclusion (CTE-based, LEFT JOIN subject_aliases)
- `server/subject/` — SubjectController with alias-aware event retrieval (includes events from all alias chains)
- `server/event/` — EventRepository extended: location_path denormalization on insert, findSinceScoped (3-category OR: geo subjects, own assignments, system events), subject-history backfill page query, findByType
- `server/projection/` — rebuildable PatternStateProjection for enabled pattern bindings, deriving state from active activity bindings, contract-backed PatternRegistry definitions, and authoritative flag-resolution state without durable workflow tables
- `mobile/data/` — SyncService bearer push/pull/config, `/api/auth/me` actor refresh, DeviceIdentity server-resolved actor storage, and PatternProjectionEngine counterpart reading ConfigStore activity bindings plus packaged `pattern_definitions`, with shared fixture equivalence for enabled patterns
- Migration V1: events table (BIGSERIAL sync_watermark)
- Migration V2: server_identity table, server_device_seq SEQUENCE, device_sync_state table
- Migration V3: subject_aliases table (with CHECK constraint)
- Migration V4: locations (materialized path), actor_tokens, subject_locations, events.location_path column, covering index (idx_events_scoped_pull), assignment expression index
- Migration V6: deployment_config table for deployment-wide L0 `flag_severity_overrides`
- Migration V8: `auth_principal_bindings` table for IDR-027 production-auth principal-to-actor lookup
- Admin UI: subject list, flag list, development-only assignment list/creation form, location hierarchy browser
- `docker-compose.yml` — full stack dev setup
- `docker-compose.test.yml` — test DB with host networking
- `.github/workflows/server-ci.yml` — GitHub Actions CI

**Repository**: https://github.com/Hamza-ye/datarun-platform.git

---

## What's Next

**Post-Phase-4 stabilization** — active. Phase 4 is complete; the active status surfaces are the working-surface BAR/backlog, not old review chronology. BAR-001 through BAR-015 are accepted, including BAR-010 config package delivery. The scenario runtime probe set selected and routed from NW-023 has landed: NW-025/S19, NW-026/S00, NW-029/S21, NW-030/S27, NW-032/S23, and NW-033/S26. NW-034 added contract hygiene for deployer shape format and config-package schemas. NW-037 added the IDR-027 production-auth principal binding foundation without resolving FP-011 or accepting BAR-104. NW-038 added server-side OIDC/JWKS provider validation without resolving FP-011 or accepting BAR-104. NW-039 added IDR-028, selecting deployment-managed binding provisioning for production principal-binding administration. NW-040 is now ready to implement that provisioning/audit path using `docs/agent-working-surface/prompts/NW-040-implement-production-principal-binding-provisioning.md`; FP-011 and BAR-104 remain open until that runtime evidence lands.

- IDR-021 (Role-Action Enforcement Model) is active. It builds on FP-001's role timeline check and keeps FP-005 out of role-action scope: live sync stays request-time scoped, subject-history backfill is separate Phase 4 `ongoing_resolution` work, and audit/historical pull is out of Phase 4 live sync unless a successor decision introduces a separate pull class/API.
- IDR-022 (Flag Severity + Domain Uniqueness) is active. It defines deployment-wide L0 severity overrides through `flag_severity_overrides`, keeps resolvability platform-owned, defines shape-declared `domain_uniqueness_violation`, and keeps FP-005 backfill/audit behavior out of scope.
- IDR-023 (Role-Action Domain Boundary and Assignment Administration) is active. It excludes `assignment_changed` from `activities[*].roles`; assignment create/end remains an online authority path, not an activity role-action work event.
- IDR-024 (Multi-axis Assignment Containment) is active. It closes the decision gap around assignment creation/end containment across `geographic`, `subject_list`, and `activity`, defines explicit bootstrap/root semantics, and states that activity-restricted assignments do not authorize ordinary null-activity work events.
- IDR-025 (Pattern Definition Contract and Delivery) is active. It makes `contracts/patterns/*.json` the canonical platform-owned pattern source, loads those definitions into the server registry, and delivers referenced definitions through the atomic config package for mobile runtime use.
- IDR-026 (Conflict Resolver Routing and Single-Writer Resolution) is active. It defines resolver routing for all active/imminent flag categories, canonical resolution semantics, production conflict API actor binding, and defers resolver reassignment and auto-resolution mechanics.
- IDR-027 (Production Auth Principal-To-Actor Binding) is active. It defines explicit principal binding, forbids group/claim/JWT `actor_id` direct authority, and now includes the NW-038 OIDC/JWKS validation mode.
- IDR-028 (Production Principal-Binding Administration) is active. It selects deployment-managed manifest provisioning for production binding administration, defers online admin APIs, and keeps binding provisioning implementation as the NW-040 FP-011/BAR-104 gate.
- Phase spec: [docs/implementation/phases/phase-4.md](implementation/phases/phase-4.md) (reconciled; tracks landed Phase 4.1 role-action gates, Phase 4.2 severity gates, Phase 4.3 uniqueness gates, Phase 4.4 registry/binding gates, Phase 4.6 transition gates, and P04 scenario-grade Responsibility Binding coverage)
- Landed implementation slices: Phase 4.1 role-action enforcement from IDR-021/IDR-023, including authoritative server `role_stale` action-authority semantics and mobile advisory role-action gating; Phase 4.2 flag severity defaults and deployment-wide `flag_severity_overrides` from IDR-022; IDR-024 / FP-007 assignment administration hardening; FP-008 assignment command identity binding; Phase 4.3 domain uniqueness schema, detector, and mobile advisory uniqueness from IDR-022; Phase 4.4 platform pattern registry and activity binding validation from IDR-020; IDR-025 pattern definition contract/package delivery; Phase 4.5 rebuildable pattern-state projection for enabled bindings including `ongoing_resolution/v1`; FP-005 subject-history backfill; FP-009 runtime resolver designation and single-writer resolution enforcement; Phase 4.6 transition detection; P04 scenario-grade Responsibility Binding coverage; FP-010 platform payload shape contract parity; NW-037/IDR-027 production-auth principal binding foundation; and NW-038 OIDC/JWKS auth-provider validation. These do not implement auto-resolution, resolver reassignment, normal sync backfill, durable workflow-state tables, binding provisioning runtime, mobile OIDC login UX, or group/claim authority.
- Recommended next slice: NW-040 production principal-binding provisioning implementation using `docs/agent-working-surface/prompts/NW-040-implement-production-principal-binding-provisioning.md`. Implement the IDR-028 deployment-managed manifest path with audit history, active binding projection/support rows, idempotency/concurrency controls, and tests for continued group/claim/JWT `actor_id` non-authority. Do not add online production binding-admin APIs, new scope mechanisms, resolver reassignment, auto-resolution, or envelope changes.
- Coverage gap closed: P04 now has scenario-grade coverage for reassignment plus S19 stale offline authority, S00 structured capture, S21 scoped supervisor review, S23 setup/config, S26 reporting/aggregate oversight, and S27 logistics transfer.

### Test Debt (carried from Phase 3)
- Multi-version PE fixture
- activity_ref auto-population
- Widget-level form tests

---

## Blockers

FP-005 is `RESOLVED`; `ongoing_resolution/v1` projection now uses the distinct subject-history backfill surface for newly assigned long-running subjects. FP-009 is `RESOLVED`; Phase 4.6 `transition_violation` emission has landed and uses IDR-026 resolver routing, but auto-resolution and resolver reassignment remain deferred successor work. FP-010 is `RESOLVED`; server runtime now bundles and enforces the six platform payload shape schemas from `contracts/shapes/`, and platform payload schemas are not deployer shape registry rows or packaged as deployer `shapes`. FP-011 is `OPEN` for production binding provisioning implementation and any group/claim authority model. NW-037/IDR-027 added the explicit principal-binding foundation, NW-038 added OIDC/JWKS provider validation, and NW-039/IDR-028 selected deployment-managed binding administration, but BAR-104 remains unaccepted until NW-040 runtime evidence lands. The P04/S00/S19/S21/S23/S26/S27 scenario-grade probe gaps selected for stabilization are closed. BAR-010 is accepted; no baseline acceptance candidate is currently active.

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
| IDR-026: Conflict Resolver Routing and Single-Writer Resolution | **ACTIVE** | [idr-026-conflict-resolver-routing-and-single-writer-resolution.md](decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md) |
| IDR-027: Production Auth Principal-To-Actor Binding | **ACTIVE** | [idr-027-production-auth-principal-actor-binding.md](decisions/idr-027-production-auth-principal-actor-binding.md) |
| IDR-028: Production Principal-Binding Administration | **ACTIVE** | [idr-028-production-principal-binding-administration.md](decisions/idr-028-production-principal-binding-administration.md) |
