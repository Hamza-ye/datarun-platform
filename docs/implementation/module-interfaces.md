# Module Interface Baseline

> Current as of the 2026-06-13 NW-065 production-runtime implementation work. This file records implemented module boundaries; it is not a roadmap for new subsystems.

## Authority role

This file is the **implemented boundary map** for the current platform codebase. It is subordinate to `canonical-decision-ledger.md`.

Use this file to understand module ownership, inputs, outputs, storage, forbidden responsibilities, and guard tests. Do not use it to introduce new architecture decisions, revise CDL constraints, promote implementation claims to architecture truth, or activate deferred platform-evolution items.

Mechanism/instance rule: module boundaries must preserve the CDL mechanism/instance split. Platform-owned mechanisms remain platform-owned; deployer-authored instances remain configuration-package content.

Claim rule: "implemented" in this file means the boundary is recorded as implemented by the current documentation set. Operational proof still requires source-code inspection, test evidence, or scenario execution. Current acceptance status lives in `docs/agent-working-surface/baseline-acceptance-register.md`.

Routing rule: when a task needs rationale, change classification, gap status, future-work routing, or architecture test intent, use `docs/agent-working-surface/decision-anchor-layer/README.md` as the active stewardship router. Use `docs/agent-working-surface/escape-hatch-register.md` only for measured escape-hatch context. The CDL wins on decisions; contracts win on crossed wire/process boundaries; `docs/status.md`, the Baseline Acceptance Register, and the platform next-work backlog govern current implementation standing and evidence.

Non-overlap rule: this file is not a DEC record, gap register, architecture rationale companion, roadmap, or product-readiness map. Keep it as a thin code-boundary index for implementers who are already touching a module. If a future edit starts explaining why a boundary exists or where new work should route, put that in the decision-anchor layer instead and link back only when needed.

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
- **Outputs**: command-capable active-assignment filtering used by `AssignmentService` before the create/end containment checks described in `docs/specifications/platform/assignment-scope-and-administration.md`.
- **Storage**: `deployment_config` row keyed `assignment_admin_capabilities`; assignment events keep role/scope only and do not store command capability.
- **Forbidden**: `activities[*].roles`, request-body actor IDs, IdP groups/claims/roles, JWT `actor_id`, UI vocabulary, new scope mechanisms, assignment payload or envelope fields, resolver reassignment, auto-resolution, mobile authoritative rejection, or config-package authority for this policy.
- **Guards**: `AssignmentContainmentIntegrationTest`, `ProductionAuthIntegrationTest`, `DeployTimeValidatorTest`, responsibility-binding, scope-filtered sync, subject-history backfill, and conflict-resolution regression tests.

## Web/Config Admin Command Capability Policy

- **Owns**: server-side deployment-configured `admin_command_capabilities` actor-to-command policy for platform-owned `web_admin.access`, `web_admin.read_scoped`, and `config_admin.*` command names.
- **Inputs**: the server-resolved `WebAdminSessionContext.actorId()`, policy JSON from `deployment_config` seeded by reviewed one-shot provisioning, and command constants selected by server-side handlers.
- **Outputs**: command-capability decisions used by the production `/web-admin` shell gate, `/web-admin/config` handlers, and `/web-admin/assignments` page/action entry handlers. Missing, malformed, or unknown-command policy fails closed for command evaluation.
- **Storage**: `deployment_config` row keyed `admin_command_capabilities`; policy maps explicit actor UUIDs to command arrays and is not packaged to mobile clients.
- **Forbidden**: assignment roles, `activities[*].roles`, IdP groups/claims/roles, JWT `actor_id`, request-body actor IDs, UI-selected actors, fixed development admin actors, config-package authority, assignment-admin containment bypass, resolver authority, broad data-read authority, browser editing of `admin_command_capabilities`, `assignment_admin_capabilities`, or principal-binding policy, contracts/schemas/envelopes/migrations/mobile auth changes, or production deployment approval.
- **Guards**: `AdminCommandCapabilityServiceIntegrationTest`, `OneShotProvisioningIntegrationTest`, `WebAdminSessionBoundaryTest`, `WebAdminSecurityFoundationTest`, `ProductionAuthIntegrationTest`, and `ProductionDevelopmentSurfaceFilterTest`.

## Authenticated Actor Resolver

- **Owns**: resolving bearer credentials to one platform `actor_id` before actor-scoped API logic runs.
- **Inputs**: `Authorization: Bearer <credential>`, auth mode configuration, dev token table, local JWT principal claims, OIDC/JWKS issuer/audience/JWKS configuration, explicit `(issuer, subject) -> actor_id` bindings.
- **Outputs**: authenticated actor context for sync, assignment, config, conflict, and `/api/auth/me` endpoints.
- **Storage**: `actor_tokens` for development mode; `auth_principal_bindings` for production-auth active lookup support; `auth_principal_binding_operations` for append-only deployment-managed provisioning audit/history. Production binding administration is deployment-managed manifest provisioning with auditable operation history per `docs/specifications/platform/production-auth-principal-binding.md`, not an online admin API.
- **Forbidden**: treating JWT groups, roles, resource claims, or JWT `actor_id` claims as assignment, scope, resolver, or admin authority.
- **Guards**: `ProductionAuthIntegrationTest`, `LocalJwtAuthCompatibilityIntegrationTest`, actor-token integration tests, scope-filtered sync and conflict-resolution tests.

## Web Admin Security Foundation

- **Owns**: Spring Security and OAuth2 client framework presence plus the servlet security chain that preserves existing API controller/interceptor behavior while enabling CSRF/session support for the separate production web-admin shell.
- **Inputs**: ordinary HTTP requests after container filters and before MVC handlers.
- **Outputs**: a security filter chain that disables framework form login, basic auth, OAuth2 login, logout, and request cache; ignores CSRF for `/api/**`, current development `/admin/**` lanes, and the OIDC callback; permits requests to continue to the existing owning filters, interceptors, and controllers.
- **Storage**: none.
- **Forbidden**: replacing `ActorTokenInterceptor` as the bearer API actor-resolution owner, making current Thymeleaf `/admin`, `/admin/config`, or `/admin/dev` development consoles production-ready, granting authority from Spring Security principals, IdP claims/groups/roles, JWT `actor_id`, request bodies, or UI state.
- **Guards**: `WebAdminSecurityFoundationTest`, `ProductionAuthIntegrationTest`, `ProductionDevelopmentSurfaceFilterTest`, and existing sync/config/admin regression tests.

## Production Web Admin Session Boundary

- **Owns**: the separate `/web-admin` browser login/session shell for production web-admin entry proof.
- **Inputs**: OIDC authorization-code callback state/code, configured public callback URI, server-side token exchange output validated through `OidcJwksTokenValidator`, server-stored login state and nonce, explicit active `(issuer, subject) -> actor_id` principal bindings, and Spring Security CSRF tokens for protected state-changing web-admin requests.
- **Outputs**: server-managed web-admin session context containing actor id, issuer, subject, auth source, login/last-seen/expiry metadata, and secret-safe session correlation; minimal shell only when `admin_command_capabilities` grants `web_admin.access` to the server-resolved actor; logout/session invalidation; login/session/shell audit events.
- **Storage**: servlet session only for browser web-admin state; principal bindings stay in `auth_principal_bindings`; login/session audit is published as application events and secret-safe logs, not domain events.
- **Forbidden**: using `/web-admin` session id as platform authority by itself, accepting actor ids from browser requests, deriving shell access from assignment roles, IdP claims/groups/roles, JWT `actor_id`, request bodies, UI state, or fixed development actors, granting config-admin authority, assignment authority, resolver authority, broad data-read authority, or online principal-binding administration, productionizing current `/admin`, `/admin/config`, or `/admin/dev` lanes, changing contracts/schemas/envelopes/migrations/mobile auth, or treating IdP claims/groups/roles/JWT `actor_id` as platform authority.
- **Guards**: `WebAdminSessionBoundaryTest`, `AdminCommandCapabilityServiceIntegrationTest`, `ProductionAuthIntegrationTest`, `ProductionDevelopmentSurfaceFilterTest`, `WebAdminSecurityFoundationTest`, and relevant sync/config/admin regression tests.

## Production Web Admin Assignment Workflow

- **Owns**: the server-rendered Spring MVC/Thymeleaf `/web-admin/assignments` create/end responsibility workflow after the web-admin session and `web_admin.access` shell gate.
- **Inputs**: server-managed `WebAdminSessionContext.actorId()`, Spring Security CSRF tokens, browser form submissions for target actor, role, geographic/subject/activity scopes, valid-from/valid-to values, and target assignment ids for end commands.
- **Outputs**: scoped assignment-administration pages showing only active assignments for the server-resolved session actor; append-only assignment create/end commands executed through the existing `AssignmentService` using `assignment_admin.create` / `assignment_admin.end` capability plus same-assignment containment.
- **Storage**: no new authority storage; servlet session supplies actor context but not standalone authority, assignment authority remains event-derived, and assignment events stay append-only through the existing assignment/event-store boundary.
- **Forbidden**: productionizing current `/admin`, `/admin/assignments`, `/admin/config`, or `/admin/dev` lanes, accepting browser/request actor ids as the command actor, treating the session id, UI state, IdP claims/groups/roles, JWT `actor_id`, fixed development actors, or `web_admin.access` as assignment mutation authority, bypassing `assignment_admin_capabilities` or same-assignment containment, browser editing of `assignment_admin_capabilities`, `admin_command_capabilities`, or principal-binding policy, adding new command authority or scope mechanisms, changing contracts/schemas/envelopes/sync protocol/migrations/mobile code or behavior/tenant-aware internals, online binding admin, operations evidence, or real-production approval.
- **Guards**: `WebAdminAssignmentWorkflowIntegrationTest`, `AssignmentContainmentIntegrationTest`, `AdminCommandCapabilityServiceIntegrationTest`, `WebAdminSessionBoundaryTest`, `ProductionAuthIntegrationTest`, `ProductionDevelopmentSurfaceFilterTest`, and `WebAdminSecurityFoundationTest`.

## Production Web Admin Config Workflow

- **Owns**: the server-rendered Spring MVC/Thymeleaf `/web-admin/config` S23 setup workflow for draft candidate authoring, dry-run validation, readiness review, approval, and publish through accepted config package/provisioning behavior after the web-admin session and `web_admin.access` shell gate.
- **Inputs**: server-managed `WebAdminSessionContext.actorId()`, Spring Security CSRF tokens, browser form submissions, JSON setup candidate content, candidate hashes, server-selected `config_admin.author`, `config_admin.validate`, `config_admin.readiness_review`, `config_admin.approve`, and `config_admin.publish` command constants, existing config validation services, and `ReviewedConfigProvisioner`.
- **Outputs**: draft candidate state that is not delivered to devices; validation/readiness/approval state bound to the exact candidate content hash; stale-hash denial after candidate changes; one atomic published config package through `ReviewedConfigProvisioner`; and monotonic audit/history entries recording the server-resolved actor and workflow action.
- **Storage**: web-admin config candidate and audit/history tables for browser workflow state; accepted config package storage and `deployment_config` rows remain owned by the existing config publication boundary; servlet session state supplies the actor context but not standalone authority.
- **Forbidden**: productionizing current `/admin`, `/admin/config`, or `/admin/dev` lanes, accepting actor ids or command authority from browser requests, UI state, IdP claims/groups/roles, JWT `actor_id`, assignment roles, or fixed development actors, treating `web_admin.access` as config-admin authority, browser editing of `admin_command_capabilities`, `assignment_admin_capabilities`, or principal-binding policy, changing contracts/schemas/envelopes/sync protocol/mobile code or behavior/tenant-aware internals, adding config-package keys or per-device variants, assignment-admin UX, online binding admin, operations evidence, or real-production approval.
- **Guards**: `WebAdminConfigWorkflowIntegrationTest`, `WebAdminSessionBoundaryTest`, `AdminCommandCapabilityServiceIntegrationTest`, `OneShotProvisioningIntegrationTest`, `ConfigIntegrationTest`, `DeployTimeValidatorTest`, `ProductionDevelopmentSurfaceFilterTest`, and `WebAdminSecurityFoundationTest`.

## Production Runtime Boundary

- **Owns**: production-profile startup validation, graceful shutdown settings, separation of the management listener, bounded health/Prometheus exposure, development-surface hiding, and secret-safe structured request logs.
- **Inputs**: the active Spring profile plus deployment-supplied database, OIDC/JWKS, principal-binding operator, listener, and shutdown properties.
- **Outputs**: fail-closed startup errors naming unsafe property keys, liveness/readiness and Prometheus signals, request correlation fields, and `404` responses for development admin/token surfaces in production.
- **Storage**: none; operational configuration remains external to the application artifact.
- **Forbidden**: accepting development auth/default credentials in production, exposing secret/header/query/body values in request logs, granting platform authority, or replacing deployment-owned TLS, host/database monitoring, backup, and alert routing.
- **Guards**: `ProductionRuntimeValidatorTest`, `ProductionDevelopmentSurfaceFilterTest`, `StructuredRequestLoggingFilterTest`, `ObservabilityIntegrationTest`, and `ProductionAuthIntegrationTest`.

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

## One-Shot Provisioning

- **Owns**: non-web, file-driven application of reviewed principal-binding manifests, complete deployment-config snapshots, and the single initial assignment bootstrap.
- **Inputs**: one strict UTF-8 JSON file, a command name, an operator-evidence UUID, and an external evidence identifier.
- **Outputs**: secret-safe JSON with the command result and exact input SHA-256; principal-binding audit rows, validated authoring/config-package rows, or one bootstrap assignment event through the existing owning services.
- **Storage**: only the existing principal-binding audit/projection tables, configuration tables/packages, and append-only event store.
- **Forbidden**: online production admin APIs, direct operator SQL, new authority sources, IdP claim/group authority, general root assignment creation, duplicate package publication for exact reapplication, or rollback semantics.
- **Guards**: `OneShotProvisioningIntegrationTest`, `ProductionAuthIntegrationTest`, `ConfigIntegrationTest`, `DeployTimeValidatorTest`, and `AssignmentContainmentIntegrationTest`.

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
