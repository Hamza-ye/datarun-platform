# Module Interface Baseline

> Current as of the 2026-06-05 NW-055 shared-device actor-partition checkpoint. This file records implemented module boundaries; it is not a roadmap for new subsystems.

## Authority role

This file is the **implemented boundary map** for the current platform codebase. It is subordinate to `canonical-decision-ledger.md`.

Use this file to understand module ownership, inputs, outputs, storage, forbidden responsibilities, and guard tests. Do not use it to introduce new architecture decisions, revise CDL constraints, promote implementation claims to architecture truth, or activate deferred platform-evolution items.

Mechanism/instance rule: module boundaries must preserve the CDL mechanism/instance split. Platform-owned mechanisms remain platform-owned; deployer-authored instances remain configuration-package content.

Claim rule: "implemented" in this file means the boundary is recorded as implemented by the current documentation set. Operational proof still requires source-code inspection, test evidence, or scenario execution. Current acceptance status lives in `docs/agent-working-surface/baseline-acceptance-register.md`.

Routing rule: when a task needs rationale, change classification, escape-hatch context, or architecture test intent, use `docs/agent-working-surface/architecture-rationale-and-routing-companion.md` as non-authoritative companion context. The CDL wins on decisions; `docs/status.md` and the Baseline Acceptance Register govern current implementation standing.

## Event Store

- **Owns**: append-only event persistence, idempotent event insert, sync watermark assignment, ordered event reads.
- **Inputs**: structurally valid event envelopes plus payload JSON.
- **Outputs**: stored events, ordered event streams, subject/event lookup rows.
- **Storage**: `events` table and `events_sync_watermark_seq`.
- **Forbidden**: operational event update/delete paths, hidden workflow state, resolver reassignment, envelope field/type expansion without CDL authority.
- **Guards**: `EnvelopeVocabularyTest`, `EnvelopeSchemaParityTest`, sync/subject/projection integration tests, platform payload backstop validation for platform-owned shape refs.

## Projection Engine

- **Owns**: rebuildable subject, alias, lifecycle, and workflow-pattern projections from events.
- **Inputs**: ordered events, active activity bindings, packaged platform pattern definitions.
- **Outputs**: subject summaries, subject histories, pattern-state JSON.
- **Storage**: projections are derived; `subject_aliases` is rebuildable alias cache only.
- **Forbidden**: durable workflow-state tables, normal sync watermark rewrites, treating unresolved event-level flags as subject-level state.
- **Guards**: `ProjectionEquivalenceTest`, `PatternStateProjectionTest`, mobile projection tests, shared `contracts/fixtures/*.json`.

## Identity Resolver

- **Owns**: subject merge/split events, canonical alias projection, identity lifecycle projection.
- **Inputs**: merge/split commands and identity conflict resolutions.
- **Outputs**: `subjects_merged/v1`, `subject_split/v1`, alias cache rebuilds.
- **Storage**: events plus rebuildable `subject_aliases`.
- **Forbidden**: split-as-alias, merge without canonical alias preservation, non-event identity state.
- **Guards**: `IdentityResolverIntegrationTest`, subject projection/equivalence tests, platform payload contract tests.

## Conflict Detector

- **Owns**: accept-and-flag detection for role/scope/time/domain/transition anomalies.
- **Inputs**: pushed or server-emitted domain events plus actor scope/history context.
- **Outputs**: `conflict_detected/v1` events with catalog category, resolvability, and designated resolver.
- **Storage**: none beyond emitted events.
- **Forbidden**: rejecting structurally valid policy/state anomalies, auto-resolution, resolver reassignment.
- **Guards**: auth/domain/transition integration tests, `FlagCatalogTest`, platform payload contract tests.

## Scope Resolver

- **Owns**: actor assignment reconstruction and visibility/authority decisions from assignment events.
- **Inputs**: authenticated actor context, assignment event history, subject/location/activity scopes.
- **Outputs**: active assignment facts and scope containment decisions.
- **Storage**: assignment authority is event-derived; dev actor tokens, auth principal bindings, and location tables support lookup only.
- **Forbidden**: group/claim direct authority, JWT `actor_id` direct authority, or identity-provider roles as platform authority unless a later successor decision explicitly promotes that model.
- **Guards**: assignment containment, scope-filtered sync, responsibility-binding, and auth flag tests.

## Assignment Admin Capability Policy

- **Owns**: server-side deployment-configured `assignment_admin_capabilities` role-to-command policy for platform-owned `assignment_admin.create` and `assignment_admin.end`.
- **Inputs**: current active assignment role labels, validated policy JSON from `deployment_config`, and the authenticated actor already resolved for the assignment command path.
- **Outputs**: command-capable active-assignment filtering used by `AssignmentService` before IDR-024 create/end containment checks.
- **Storage**: `deployment_config` row keyed `assignment_admin_capabilities`; assignment events keep role/scope only and do not store command capability.
- **Forbidden**: `activities[*].roles`, request-body actor IDs, IdP groups/claims/roles, JWT `actor_id`, UI vocabulary, new scope mechanisms, assignment payload or envelope fields, resolver reassignment, auto-resolution, mobile authoritative rejection, or config-package authority for this policy.
- **Guards**: `AssignmentContainmentIntegrationTest`, `ProductionAuthIntegrationTest`, `DeployTimeValidatorTest`, responsibility-binding, scope-filtered sync, subject-history backfill, and conflict-resolution regression tests.

## Authenticated Actor Resolver

- **Owns**: resolving bearer credentials to one platform `actor_id` before actor-scoped API logic runs.
- **Inputs**: `Authorization: Bearer <credential>`, auth mode configuration, dev token table, local JWT principal claims, OIDC/JWKS issuer/audience/JWKS configuration, explicit `(issuer, subject) -> actor_id` bindings.
- **Outputs**: authenticated actor context for sync, assignment, config, conflict, and `/api/auth/me` endpoints.
- **Storage**: `actor_tokens` for development mode; `auth_principal_bindings` for production-auth active lookup support; `auth_principal_binding_operations` for append-only deployment-managed provisioning audit/history. Production binding administration is deployment-managed manifest provisioning with auditable operation history per IDR-028, not an online admin API.
- **Forbidden**: treating JWT groups, roles, resource claims, or JWT `actor_id` claims as assignment, scope, resolver, or admin authority.
- **Guards**: `ProductionAuthIntegrationTest`, `LocalJwtAuthCompatibilityIntegrationTest`, actor-token integration tests, scope-filtered sync and conflict-resolution tests.

## Sync Surfaces

- **Owns**: push/pull exchange, bearer-bound actor authorship checks, scope-filtered pull, actor-scoped pull/config bookkeeping, and subject-history backfill.
- **Inputs**: structurally valid event envelopes, authenticated actor context, `device_id`, client `last_pull_watermark`, pull `since_watermark`, config version hints, and subject-history page requests.
- **Outputs**: persisted accepted events, conflict/domain/transition flag events, scope-filtered event pages, config version discovery, and subject-history pages.
- **Storage**: events append through Event Store; `device_sync_state` is operational bookkeeping keyed by `(device_id, actor_id)` and stores normal pull/config observation only.
- **Forbidden**: normal live-sync watermark rewrites, using `device_sync_state` as authority, broad audit/history pull, new scope mechanisms, mobile-authored authority, or changing envelope fields/types.
- **Guards**: `SyncControllerIntegrationTest`, `ScopeFilteredSyncIntegrationTest`, `SubjectHistoryBackfillIntegrationTest`, `ProductionAuthIntegrationTest`, mobile sync tests.

## Shape Registry

- **Owns**: deployer-authored form shape DSL versions in the `shapes` table.
- **Inputs**: deployer shape create/version/deprecate commands.
- **Outputs**: deployer shape definitions for config packages, form rendering, payload structural validation.
- **Storage**: `shapes` table.
- **Forbidden**: treating platform payload JSON Schemas as deployer-editable shape rows.
- **Guards**: `ConfigIntegrationTest`, deploy-time validator tests, `ShapeFormatSchemaContractTest`, `PlatformPayloadBoundaryTest`.

## Platform Payload Contracts

- **Owns**: runtime validation of platform-owned payload JSON Schemas under `contracts/shapes/*.schema.json`.
- **Inputs**: exact platform shape refs: assignment, conflict, merge, and split payloads.
- **Outputs**: validation violations or event-write acceptance.
- **Storage**: none; schemas are source-controlled resources bundled into the server artifact.
- **Forbidden**: deployer editing, activity binding as form shapes, DB mirror ownership.
- **Guards**: `PlatformPayloadShapeContractTest`, `PlatformPayloadEmissionContractIntegrationTest`, `PlatformPayloadBoundaryTest`.

## Config Packager

- **Owns**: atomic deployment config packages.
- **Inputs**: deployer shapes, active activities, expression rules, severity overrides, referenced pattern refs.
- **Outputs**: config package JSON with deployer shapes, activities, expressions, severity overrides, and referenced platform pattern definitions.
- **Storage**: `config_packages` and `deployment_config`.
- **Forbidden**: packaging platform payload schemas as deployer `shapes`, mutating sync watermarks.
- **Guards**: `ConfigIntegrationTest`, `DeployTimeValidatorTest`, `ConfigPackageSchemaContractTest`, platform payload boundary tests. BAR-010 is accepted for config package delivery.

## Mobile Actor Session And Local Store

- **Owns**: one active actor session on device, server-resolved actor id/token storage, per-actor local mutable partitions, event assembly authorship, mobile sync request credentials, and advisory/projection data derived from the active actor partition.
- **Inputs**: `/api/auth/me` actor context, bearer credentials, active actor selection/resume, locally assembled events, pulled events, and config packages.
- **Outputs**: actor-authored local events, actor-scoped pending-push batches, local subject/projection/advisory views, active/pending config state, and actor-scoped normal sync progress.
- **Storage**: shared device id and device-global sequence; actor-local token, watermark, and subject-history cursor keys; per-actor SQLite databases containing events, pending push, local assignments, aliases, projections derived from those rows, and current/pending config package state.
- **Forbidden**: writable offline sessions for unknown actors, UI-selected actor authority, cross-actor request signing, shared mutable local event/config state, reauthoring pending events, mobile authoritative rejection, expiry/decommission/recovery policy beyond safe sealing, or IdP claim/group authority.
- **Guards**: `sync_service_test.dart`, `event_assembler_test.dart`, `config_store_test.dart`, `projection_engine_test.dart`, `pattern_projection_test.dart`, `selective_retain_test.dart`, full mobile `flutter test`.

## Pattern Registry

- **Owns**: platform workflow pattern definitions from `contracts/patterns/*.json`.
- **Inputs**: source-controlled pattern definition resources.
- **Outputs**: pattern metadata for deploy-time validation, config package delivery, and projection.
- **Storage**: none; registry is loaded from bundled resources.
- **Forbidden**: deployer-authored state machines, durable workflow state, platform assignment transfer as deployer shape ownership.
- **Guards**: `PatternDefinitionContractTest`, pattern projection/equivalence tests.

## Trigger Engine

- **Owns**: no active runtime trigger behavior. This boundary is reserved for future server-side trigger semantics only when scheduled by a successor platform decision and implementation slice.
- **Inputs**: not active in current implementation.
- **Outputs**: not active in current implementation.
- **Storage**: none currently.
- **Forbidden**: introducing trigger side effects outside Event Store without CDL authority and a Baseline Acceptance Register update.
- **Guards**: no active runtime tests beyond config package empty-section preservation.

## Command Validator (Advisory Only)

**Role**: On-device advisory component that warns users about potentially invalid actions. Reduces unnecessary flags by warning users before they submit invalid transitions. Its work is always redundant to the Conflict Detector + Projection Engine. Without it, the system works identically — just with more flags to resolve.

- **Owns**: UX warnings before users submit potentially invalid transitions.
- **Depends on**: Projection Engine (current workflow state) and Pattern Registry (valid transitions).
- **Outputs**: validation warning.
- **Storage**: none.
- **Forbidden**: using command validation to reject structurally valid state/policy anomalies that should be accepted and flagged.
