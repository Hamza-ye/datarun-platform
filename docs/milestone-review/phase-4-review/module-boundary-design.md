# Module Boundary Design

> Status: **Proposed implementation architecture**  
> Authority input: `canonical-decision-ledger.md`  
> Review input: `001-implementation-decision-alignment-review.md`, phase plans, and active IDRs in the project source set.
>
> Avoided Inputs: Inspect code/tests or run scenarios was excluded as inputs for  the process produced this file.

Purpose: define maintainable server/mobile/module boundaries for the current implementation and preserve a clean later split path if operational scale or team topology requires it.

This document is not a replacement for `canonical-decision-ledger.md`. It is an implementation-architecture companion: it translates canonical decisions into module boundaries, dependency rules, package layout, persistence ownership, ports, adapters, and split-readiness guidance.

---

## 1. Design posture

The recommended architecture is a **modular monolith with clean internal boundaries**.

Do not split into services early. The event stream, sync pipeline, authorization model, conflict pipeline, projection behavior, configuration package, and workflow projection are tightly coupled by architectural invariants. Premature distribution would create consistency and operational complexity without enough evidence that a process boundary is needed.

Design the monolith so that each module has:

- explicit public application services;
- explicit ports for cross-module calls;
- private persistence where possible;
- no direct dependency on another module's database tables except through approved read models or ports;
- adapter isolation for HTTP, database, mobile storage, and admin UI;
- shared contracts isolated from domain behavior.

The result should be easy to maintain as one deployable while preserving a credible extraction path later.

---

## 2. Boundary principles

### P1 — Canonical ledger remains the architecture authority

Module design must preserve the canonical ledger. If implementation convenience conflicts with canonical invariants, module design loses.

### P2 — Event stream is the integration core, not a shared mutable database

Operational facts are appended as events. Modules may derive projections, indexes, and caches, but those are rebuildable. No module owns canonical current state outside the event stream.

### P3 — Projections are read models, not ownership boundaries

A module may own a projection table for performance, but that table does not own the business fact. Repair is event replay or event append, not direct projection patching.

### P4 — Cross-module dependencies go through ports

Application services may call ports. They should not import another module's adapter, repository, SQL mapper, controller, or internal domain object directly.

### P5 — Domain modules do not depend on adapters

Domain logic cannot depend on Spring controllers, SQL libraries, HTTP, Flutter widgets, or concrete database implementations. Adapters implement ports at the edges.

### P6 — Shared kernel stays small

Only stable, low-ambiguity concepts belong in shared kernel: envelope value objects, typed references, shape refs, event type enum, flag category enum, causal metadata, and validation primitives. Do not put services, workflows, repositories, or policy logic in shared kernel.

### P7 — Server authority and device advisory behavior are separate

Mobile may validate, warn, hide, cache, project, or advise. Server-side acceptance, authorization, conflict detection, resolver enforcement, trigger execution, and canonical sync decisions remain authoritative unless a future platform decision changes that.

### P8 — Configuration is data, not deployer code

Configuration modules may package shapes, activities, expressions, role maps, severity overrides, uniqueness constraints, and pattern bindings. They must not allow deployer-authored state machines, arbitrary functions, dynamic queries, or custom access logic.

### P9 — Mechanism and instance remain separate

Platform mechanisms are modules/contracts. Deployer instances are configuration data. The module layout must not let deployer-authored instances redefine platform mechanism behavior.

### P10 — Split readiness is secondary to correctness

Every module should have a future extraction seam. Extraction should happen only after real pressure appears: independent scaling, separate team ownership, regulatory isolation, or operational deploy cadence mismatch.

---

## 3. Capability inventory

| Capability | Current responsibility | Canonical concerns touched | Permanent surface touched | Split risk |
|---|---|---|---|---|
| Eventing / Event Store | Validate and persist immutable events, assign watermarks, deduplicate. | Event stream authority, envelope, type closure, shape_ref. | Event table, envelope schema, sync protocol. | Very high; keep central. |
| Sync / Replication | Push/pull, device state, scope-filtered delivery, config discovery, subject-history backfill. | Sync scope = access scope, accept-and-flag, causality. | Sync APIs, device sync state, watermark protocol. | High; can split later behind stable protocol. |
| Projection | Derived state, subject views, alias-aware reads, flag exclusion, workflow state, source-chain traversal. | Projections rebuildable, detect-before-act, state-as-projection. | Projection APIs/tables. | Medium; keep as internal read-model layer first. |
| Identity | Merge, split, alias projection, lineage invariants, stale-reference interpretation. | Alias in projection, no historical rewrite, online identity commands. | Platform-bundled identity shapes, alias projection. | Medium. |
| Integrity / Flags | Detection orchestration, flag emission, sweep, resolver routing, single-writer resolution. | Accept-and-flag, detect-before-act, flag lifecycle. | Flag event shapes, resolver metadata, detection pipeline. | High; cross-cutting. |
| Authorization / Assignment / Scope | Assignment lifecycle, scope containment, role-action authority, access set computation. | Authority as projection, scope mechanism, assignment containment. | Assignment shapes, locations, subject_locations, auth APIs. | High; security-sensitive. |
| Configuration | Shape registry, expressions, activities, deploy-time validation, config package. | Bounded configuration, shape versioning, atomic delivery. | Config DB, package JSON, schemas. | Medium; good future split candidate. |
| Workflow / Patterns | Pattern registry, pattern definitions, bindings, state projection, transition evaluation. | Workflow state as projection, platform-owned patterns. | Pattern contracts, pattern_definitions package key. | Medium; good future split candidate after stable ports. |
| Policy / Triggers | L3 event-reaction, L3 deadline checks, auto-resolution policy execution. | Server-only L3, auto-resolution as event append. | Trigger/policy config and emitted events. | Medium/high once scheduled. |
| Admin / Operator UI | Human workflows: config authoring, assignment admin, resolver queues, review surfaces. | UI adapter only; cannot own semantics. | Web controllers/templates/API views. | Low; easiest extraction. |
| Mobile Runtime | Offline capture, local event store, local projections, config storage, advisory validation, sync client. | Offline-first, advisory local checks, local projection. | SQLite schema, mobile config store, Flutter UI. | Separate codebase already; keep protocol-driven. |
| Contracts | Envelope schema, platform shapes, pattern-definition schema, sync protocol, fixtures. | Compatibility surface and shared vocabulary. | Files under `contracts/`. | Not a service; shared artifact. |

---

## 4. Boundary classification

| Boundary | Classification | Rationale |
|---|---|---|
| `contracts` | Shared contract package | Stable schemas/protocols/fixtures consumed by server and mobile. No runtime domain behavior. |
| `shared-kernel` | Small shared kernel | Value objects/enums used across modules. Must stay small. |
| `eventing` | Core kernel / infrastructure-backed domain service | All operational writes enter here. High-stability boundary. |
| `sync` | Application module | Orchestrates transport, scope filtering, config discovery, push pipeline. |
| `projection` | Read-model module | Owns derivation contracts and projection building. Not canonical truth. |
| `identity` | Domain module | Owns identity lifecycle commands and alias projection logic. |
| `integrity` | Domain/application module | Cross-cutting detection and resolution lifecycle. Requires ports to other modules. |
| `authorization` | Domain module | Owns assignments, scope, role-action authority, admin command containment. |
| `configuration` | Domain/application module | Owns deployer-authored configuration surfaces and package delivery. |
| `workflow` | Domain/read-model module | Owns platform pattern interpretation and transition evaluation. |
| `policy` | Application/domain module | Owns server-side L3 triggers and auto-resolution policy execution when implemented. |
| `admin` | UI adapter | Calls application services only. Owns no domain rules. |
| `mobile-runtime` | Client application with local adapters | Mirrors selected modules locally with advisory authority and offline storage. |

---

## 5. Proposed server module layout

Recommended Java/Spring package layout:

```text
server/src/main/java/dev/datarun/server/
  shared/
    kernel/
      EventId
      EventType
      ShapeRef
      TypedRef
      ActorRef
      DeviceId
      DeviceSequence
      SyncWatermark
      FlagCategory
      FlagSeverity
      FlagResolvability
    contracts/
      EnvelopeValidator
      ShapeSchemaValidator
      PatternDefinitionValidator

  modules/
    eventing/
      domain/
      application/
      ports/
      adapters/postgres/
      adapters/contracts/

    sync/
      application/
      ports/
      adapters/http/
      adapters/postgres/

    projection/
      domain/
      application/
      ports/
      adapters/postgres/

    identity/
      domain/
      application/
      ports/
      adapters/postgres/
      adapters/http/

    integrity/
      domain/
      application/
      ports/
      adapters/postgres/
      adapters/http/

    authorization/
      domain/
      application/
      ports/
      adapters/postgres/
      adapters/http/

    configuration/
      domain/
      application/
      ports/
      adapters/postgres/
      adapters/http/
      adapters/contracts/

    workflow/
      domain/
      application/
      ports/
      adapters/contracts/
      adapters/postgres/

    policy/
      domain/
      application/
      ports/
      adapters/scheduler/
      adapters/postgres/

    admin/
      adapters/web/
```

Alternative if current code is smaller: use the same conceptual boundaries but keep fewer physical packages at first. Do not collapse `authorization`, `integrity`, and `workflow` into one package; that is where most future drift risk sits.

---

## 6. Module contracts

### 6.1 `eventing`

**Purpose**  
Own canonical event append, envelope validation, idempotency, and watermark assignment.

**Owns**:

- Event envelope validation.
- Closed six-value event type validation.
- Shape reference syntax validation.
- Event append semantics.
- Idempotent insert by event id.
- Server-assigned sync watermark.
- Event lookup by id/watermark for application services.

**Does not own**:

- Identity merge/split semantics.
- Authorization decisions.
- Conflict category detection.
- Workflow state.
- Config authoring.
- Admin UI.

**Public application services**:

```text
AppendEventsService
AppendServerEventService
LoadEventsService
EventEnvelopeValidationService
```

**Ports**:

```text
EventStore
EventWatermarkAllocator
EventEnvelopeValidator
ServerEventProducerIdentityPort
```

**Private persistence**:

```text
events
server_identity
server_device_seq
```

**Allowed dependencies**:

- `shared-kernel`
- `contracts`

**Forbidden dependencies**:

- `authorization` internals
- `identity` internals
- `workflow` internals
- `configuration` internals
- admin adapters

**Split-readiness note**  
Do not extract early. This module is the consistency center. If extracted later, it becomes the event-ingestion service with a stable append/query API and strict contract tests.

---

### 6.2 `sync`

**Purpose**  
Own push/pull orchestration, device sync state, scope-filtered delivery, config-version discovery, and subject-history backfill transport.

**Owns**:

- Push endpoint orchestration.
- Pull endpoint orchestration.
- Device sync state.
- Push request knowledge horizon handling.
- Normal live pull pagination.
- Config version advertisement.
- Subject-history backfill endpoint orchestration.

**Does not own**:

- Event persistence mechanics.
- Scope containment rules.
- Conflict detection rules.
- Projection semantics.
- Config package construction.

**Public application services**:

```text
PushSyncService
PullSyncService
SubjectHistoryBackfillService
SyncConfigDiscoveryService
```

**Consumes ports**:

```text
EventStore
ConflictDetectionPort
AuthorizationScopePort
ConfigVersionPort
ProjectionReadPort
IdentityAliasPort
```

**Private persistence**:

```text
device_sync_state
actor_tokens if not moved under auth adapter yet
```

**Boundary rules**:

- Normal `/sync/pull` remains request-time scoped.
- Subject-history backfill is a separate endpoint/use case.
- Normal sync must not become audit pull.
- Sync must not deliver unauthorized data and rely on UI hiding.
- Sync may use denormalized infrastructure metadata, but cannot add envelope fields.

**Split-readiness note**  
Can be extracted after the protocol stabilizes, but only if `AuthorizationScopePort`, `EventStore`, and `ConfigVersionPort` are stable. Avoid extracting before authorization and backfill semantics settle.

---

### 6.3 `projection`

**Purpose**  
Own rebuildable read models and projection rules used by server APIs and other modules.

**Owns**:

- Subject projection.
- Event timeline view composition.
- Flag-excluded current state.
- Alias-aware read composition.
- Source-chain traversal.
- Pattern-state projection facade.
- Projection rebuild contracts.

**Does not own**:

- Canonical event truth.
- Command authorization.
- Resolver authority.
- Event mutation.

**Public application services**:

```text
SubjectProjectionService
TimelineProjectionService
ProjectionRebuildService
FlagAwareProjectionService
```

**Consumes ports**:

```text
EventStore
IdentityAliasPort
FlagParticipationPort
PatternStatePort
AuthorizationVisibilityPort
```

**Private persistence**:

Projection tables are allowed but must be marked rebuildable:

```text
subject_projection
flag_projection
pattern_state_projection if introduced later as optimization
```

**Boundary rules**:

- Projection tables are rebuildable.
- Projection code cannot append or mutate canonical events.
- Projection output cannot be used as sole evidence of a decision unless the decision also exists as an event.

**Split-readiness note**  
A read-model service can be extracted later. It should subscribe/replay events, not own writes.

---

### 6.4 `identity`

**Purpose**  
Own subject identity lifecycle: merge, split, alias projection, lineage invariants, and stale-reference interpretation.

**Owns**

- Merge command validation.
- Split command validation.
- Alias projection maintenance.
- Lineage acyclicity checks.
- Active/retired/split lifecycle interpretation.
- Stale-reference signal support for integrity.

**Does not own**:

- General conflict lifecycle.
- Authorization expansion through aliases.
- Assignment scope.
- Domain uniqueness.

**Public application services**:

```text
MergeSubjectsService
SplitSubjectService
IdentityLifecycleProjectionService
AliasLookupService
```

**Consumes ports**:

```text
EventStore
AppendServerEventPort
AuthorizationCommandAuthorityPort
FlagEmitterPort
```

**Private persistence**:

```text
subject_aliases   # rebuildable projection only
```

**Boundary rules**:

- Historical subject references are never rewritten.
- Alias table is never an independent authority source.
- Merge/split commands are online and server-validated.
- Authorization for historical work uses original subject context.

**Split-readiness note**  
Could be extracted after `EventStore` append and alias lookup ports stabilize. Keep alias projection rebuildable so extraction does not create canonical dual ownership.

---

### 6.5 `integrity`

**Purpose**  
Own flag lifecycle, conflict detection orchestration, detector execution, resolver routing, and canonical resolution enforcement.

**Owns**:

- Detector orchestration.
- Flag event creation.
- Deterministic flag identity input tuple.
- Sweep/re-evaluation orchestration.
- Flag resolver routing.
- Single-writer resolution validation.
- Detect-before-act participation semantics.

**Does not own**:

- Assignment containment rules.
- Pattern transition tables.
- Shape uniqueness definitions.
- Identity lifecycle commands.
- Admin UI resolver UX.

**Public application services**:

```text
ConflictDetectionService
FlagEmitterService
FlagResolutionService
ResolverRoutingService
ConflictSweepService
FlagParticipationService
```

**Consumes ports**:

```text
EventStore
AppendServerEventPort
ProjectionReadPort
IdentityLifecyclePort
AuthorizationAuthorityPort
WorkflowTransitionPort
ConfigurationPolicyPort
```

**Private persistence**:

No canonical mutable flag table. Optional queue/index tables must be rebuildable from events.

**Boundary rules**:

- Detection failure cannot roll back event persistence.
- Flags are emitted as events.
- Resolver authority must be checked server-side.
- Request-body actor IDs cannot be authority inputs.
- Auto-eligible does not mean system-owned.
- Manual-only categories cannot become auto-resolvable by deployer config.

**Split-readiness note**  
Do not extract until resolver routing and flag category catalog are stable. This module has many dependencies; ports must be narrow and test-covered before extraction.

---

### 6.6 `authorization`

**Purpose**  
Own assignment lifecycle semantics, scope containment, access-scope computation, role-action authority evaluation, and assignment-administration command boundaries.

**Owns**:

- Assignment payload interpretation.
- Assignment creation/end command authorization.
- Multi-axis scope containment.
- Geographic, subject-list, and activity scope axes.
- Role-action authority evaluation.
- Authenticated actor binding for assignment commands.
- Scope-filter support for sync.
- Resolver candidate eligibility support.

**Does not own**:

- Activity configuration storage.
- Pattern state.
- Conflict lifecycle.
- Event persistence.
- Admin UI forms.

**Public application services**:

```text
AssignmentCommandService
ScopeResolverService
AccessScopeService
RoleActionAuthorityService
AssignmentAuthorityProjectionService
```

**Consumes ports**:

```text
EventStore
AppendServerEventPort
ConfigurationRoleActionPort
IdentityAliasPort
LocationReferencePort
```

**Private persistence**:

```text
locations
subject_locations
assignment_projection if materialized
actor_tokens until production auth replaces it
```

**Boundary rules**:

- Scope axes are platform-fixed.
- AND within one assignment; OR across assignments.
- Creator containment must be satisfied by one covering creator assignment unless a future decision introduces delegated union semantics.
- `assignment_changed` is not activity role-action work.
- Activity-restricted assignments do not authorize ordinary null-activity work events.
- Device-side authority checks are advisory.

**Split-readiness note**  
Could become a separate authorization service, but only after assignment command semantics and resolver eligibility APIs are stable. Keep location reference data ownership explicit.

---

### 6.7 `configuration`

**Purpose**  
Own deployer-authored configuration surfaces and package delivery: shapes, activities, expressions, severity overrides, uniqueness declarations, sensitivity classifications, pattern bindings, validation, and config package assembly.

**Owns**:

- Shape registry.
- Shape version storage.
- Activity definitions.
- Expression rules.
- Deploy-time validation.
- Config package assembly.
- Config package versioning.
- Sensitivity classification surface.
- Flag severity override config.
- Domain uniqueness declarations.
- Pattern binding config.

**Does not own**:

- Runtime conflict detection.
- Runtime authorization enforcement.
- Runtime workflow state.
- Event storage.
- Mobile UI rendering.

**Public application services**:

```text
ShapeRegistryService
ActivityConfigService
ExpressionRuleService
DeployTimeValidationService
ConfigPackageService
ConfigVersionService
```

**Consumes ports**:

```text
PatternDefinitionRegistryPort
ContractSchemaValidatorPort
```

**Private persistence**:

```text
shapes
activities
expression_rules
config_packages
```

**Boundary rules**:

- Published shape versions remain valid forever.
- Expressions are external to shapes.
- Config packages are atomic snapshots.
- Device holds at most current + pending package.
- Deploy-time validation rejects invalid config before packaging.
- Unknown future package keys may be forward-compatible only when they do not change current semantics.

**Split-readiness note**  
Good extraction candidate after schema/config versioning stabilizes. It can become a config service because its cadence differs from event sync.

---

### 6.8 `workflow`

**Purpose**  
Own platform pattern definitions, pattern binding interpretation, projection-derived workflow state, transition evaluation, and transition-violation signal generation.

**Owns**:

- Pattern definition registry.
- Pattern definition contract loading.
- Pattern binding validation semantics.
- Subject-level and event-level pattern identity.
- Pattern state transition evaluator.
- Transition-violation candidate evaluation.
- Command validator rules exposed to mobile as advisory logic.

**Does not own**:

- Deployer-authored transition tables.
- Event envelope fields.
- Role-action permission config.
- Flag persistence.
- Assignment administration.

**Public application services**:

```text
PatternRegistryService
PatternBindingValidationService
PatternStateProjectionService
TransitionEvaluationService
CommandValidationService
```

**Consumes ports**:

```text
EventStore
ConfigurationPatternBindingPort
FlagParticipationPort
FlagEmitterPort
AuthorizationRolePort
SubjectHistoryBackfillPort
```

**Private persistence**:

Pattern definition files/contracts and optional rebuildable projection tables only:

```text
contracts/patterns/*.json
pattern_state_projection if introduced as optimization
```

**Boundary rules**:

- Workflow state is projection-derived.
- Events never carry `current_state`, `workflow_state`, or `pattern_ref`.
- Deployers bind shapes/roles/parameters; they do not author transition tables.
- Transition violations are accepted and flagged, not rejected.
- Pattern definitions are platform-owned and versioned.

**Split-readiness note**  
Potential future extraction candidate after pattern definitions and projection interfaces stabilize. Until then, keep it in-process to avoid consistency and latency problems.

---

### 6.9 `policy`

**Purpose**  
Own server-side L3 policies: event-reaction triggers, deadline checks, and auto-resolution policies when implemented.

**Owns**:

- Server-only trigger execution.
- Deadline scheduler.
- Auto-resolution policy execution.
- Trigger recursion/depth enforcement.
- Policy output event emission through eventing/integrity ports.

**Does not own**:

- Device L3 execution.
- Deployer-authored code.
- Flag direct mutation.
- Pattern state truth.

**Public application services**:

```text
EventReactionPolicyService
DeadlinePolicyService
AutoResolutionPolicyService
PolicySchedulerService
```

**Consumes ports**:

```text
EventStore
AppendServerEventPort
ConfigurationPolicyPort
FlagResolutionPort
ProjectionReadPort
```

**Private persistence**:

```text
policy_schedule_queue if needed
trigger_execution_log if needed
```

**Boundary rules**:

- L3 policies run server-side only.
- Policy output appends events.
- Auto-resolution emits normal `conflict_resolved/v1` events and cannot mutate flags directly.
- Trigger chains stay bounded.

**Split-readiness note**  
Do not extract before the first production policy set is stable. Scheduler operations may later warrant separation.

---

### 6.10 `admin`

**Purpose**  
Own human-facing operator, configuration, assignment, resolver, and review UI surfaces.

**Owns**:

- HTML/API controllers for admin flows.
- View models.
- Form submissions to application services.
- Human review screens.
- Configuration authoring screens.
- Development-only admin affordances explicitly marked as such.

**Does not own**:

- Authorization semantics.
- Resolver authority.
- Event creation rules.
- Config validation semantics.
- Assignment containment.

**Consumes**:

```text
Identity application services
Integrity application services
Authorization application services
Configuration application services
Projection read services
```

**Boundary rules**:

- Controllers do not write tables directly.
- Controllers do not infer authority from request body IDs.
- Admin UI development shortcuts must not define production semantics.

**Split-readiness note**  
Easiest extraction candidate. It can become a separate web app once APIs are stable.

---

### 6.11 `mobile-runtime`

**Purpose**  
Own offline client behavior: event capture, local event store, local projections, config storage, advisory validation, sync client, form runtime, and mobile UI.

**Owns**:

- SQLite event store adapter.
- Local config package store.
- Local projection engine.
- Form rendering.
- Event assembler.
- Local device sequence and watermark state.
- Advisory validation: form expressions, role-action visibility, uniqueness warnings, command validation.
- Sync client.

**Does not own**:

- Canonical conflict detection.
- Canonical authorization decisions.
- Server trigger execution.
- Resolver enforcement.
- Canonical event rejection for state reasons.

**Suggested mobile package layout**:

```text
mobile/lib/
  shared/
    kernel/
    contracts/
  data/
    event_store/
    config_store/
    sync_client/
    projection_store/
  domain/
    eventing/
    projection/
    configuration/
    authorization_advisory/
    workflow_advisory/
    expression/
  application/
    capture/
    sync/
    form_runtime/
    advisory_validation/
  presentation/
    screens/
    widgets/
```

**Boundary rules**:

- Local checks are advisory unless explicitly server-confirmed.
- Local projections are rebuildable from local events/config.
- Device never creates authoritative server-side flags unless a future platform decision adds device-side flag creation.
- Device never runs L3 triggers as canonical policy.

**Split-readiness note**  
Already separated by deployment boundary. Keep behavior contract-driven through shared schemas and fixtures.

---

### 6.12 `contracts`

**Purpose**  
Own shared schemas, protocol definitions, platform-bundled shapes, pattern definitions, flag catalog, and shared fixtures.

**Owns**:

```text
contracts/envelope.schema.json
contracts/sync-protocol.md
contracts/shapes/*.schema.json
contracts/pattern-definition.schema.json
contracts/patterns/*.json
contracts/flag-catalog.md
shared projection fixtures
```

**Does not own**:

- Runtime code behavior.
- Repository implementations.
- UI rendering.

**Boundary rules**:

- Contract files must be validated in CI.
- Server and mobile must share fixtures for critical cross-runtime equivalence.
- Duplicated schemas must have parity tests or be generated from one source.

**Split-readiness note**  
This is a shared artifact package, not a service.

---

## 7. Dependency graph

Allowed high-level dependency direction:

```text
contracts / shared-kernel
        ↓
domain modules
        ↓
application services
        ↓
adapters: HTTP, PostgreSQL, SQLite, scheduler, admin UI, mobile UI
```

Runtime orchestration uses ports to invert dependencies where needed.

### Approved cross-module dependency pattern

```text
sync.application
  calls AuthorizationScopePort
  calls EventStore
  calls ConflictDetectionPort
  calls ConfigVersionPort

integrity.application
  calls AuthorizationAuthorityPort
  calls IdentityLifecyclePort
  calls WorkflowTransitionPort
  calls ProjectionReadPort

workflow.application
  calls ConfigurationPatternBindingPort
  calls FlagEmitterPort
  calls SubjectHistoryBackfillPort
```

### Forbidden dependency pattern

```text
sync imports authorization.adapters.postgres.AssignmentRepository
integrity imports workflow.adapters.postgres.PatternStateTable
admin writes events table directly
mobile treats advisory validator result as canonical authority
configuration imports sync controller
workflow writes conflict flags directly with SQL
```

---

## 8. Ports and adapters

Use explicit interfaces for cross-boundary collaboration.

### Core ports

```text
EventStore
AppendEventPort
AppendServerEventPort
EventEnvelopeValidator
EventWatermarkAllocator
```

### Projection ports

```text
ProjectionReadPort
FlagParticipationPort
SourceChainTraversalPort
ProjectionRebuildPort
```

### Identity ports

```text
IdentityAliasPort
IdentityLifecyclePort
SubjectLineagePort
```

### Integrity ports

```text
FlagEmitterPort
FlagResolutionPort
ResolverRoutingPort
ConflictDetectionPort
```

### Authorization ports

```text
AuthorizationScopePort
AssignmentAuthorityPort
RoleActionAuthorityPort
ResolverEligibilityPort
LocationReferencePort
```

### Configuration ports

```text
ShapeRegistryPort
ActivityConfigPort
ExpressionRulePort
ConfigPackagePort
FlagSeverityConfigPort
DomainUniquenessConfigPort
PatternBindingConfigPort
```

### Workflow ports

```text
PatternDefinitionRegistryPort
PatternStatePort
TransitionEvaluationPort
CommandAdvisoryPort
```

### Policy ports

```text
PolicySchedulePort
AutoResolutionPolicyPort
DeadlineEvaluationPort
```

Ports should use stable DTOs/value objects from shared kernel, not database rows or UI view models.

---

## 9. Database ownership rules

### Rule DB-1 — The `events` table is special

The event table is the canonical operational log. It is owned by `eventing`, but many modules need read access. Reads must be mediated by query ports or sanctioned read-only repositories.

Writes go through `AppendEventPort` or `AppendServerEventPort` only.

### Rule DB-2 — Projection tables are owned by the module that rebuilds them

Examples:

| Table | Owning module | Canonical? |
|---|---|---|
| `subject_aliases` | `identity` | No; rebuildable projection. |
| `subject_projection` | `projection` | No; rebuildable projection. |
| `assignment_projection` | `authorization` | No; rebuildable projection. |
| `pattern_state_projection` | `workflow`, if added | No; rebuildable projection. |
| flag queue/index tables | `integrity`, if added | No; rebuildable projection/index. |

### Rule DB-3 — Configuration tables are owned by `configuration`

```text
shapes
activities
expression_rules
config_packages
```

Other modules use `Configuration*Port` interfaces.

### Rule DB-4 — Authorization reference tables are owned by `authorization`

```text
locations
subject_locations
actor_tokens until replaced
```

`events.location_path`, if present, is infrastructure metadata written by event persistence/sync authorization collaboration. It is not an envelope field and must not become a mutable reinterpretation of historical authorization.

### Rule DB-5 — No cross-module foreign-key sprawl by default

Prefer stable IDs and ports over direct relational coupling across modules. Use database constraints inside a module. Cross-module referential validation belongs in application services or deploy-time validation unless correctness demands otherwise.

### Rule DB-6 — Table names should reveal projection status

Projection tables should be named or documented as projections to prevent agents and developers from treating them as source-of-truth tables.

---

## 10. Application flow ownership

### 10.1 Push sync flow

Owner: `sync.application.PushSyncService`

Flow:

```text
HTTP adapter receives push
  → Sync authenticates device/actor context
  → Eventing validates and persists structurally valid events in Tx1
  → Integrity runs detector orchestration in Tx2
  → Integrity appends deterministic flag events if anomalies found
  → Sync returns accepted/duplicates/flags_raised
```

Boundary constraints:

- Eventing does not call detectors.
- Detector failure does not roll back event persistence.
- Sync orchestration does not own detector rules.
- Flag event creation goes through eventing append.

### 10.2 Pull sync flow

Owner: `sync.application.PullSyncService`

Flow:

```text
HTTP adapter receives pull
  → Sync resolves actor/session
  → Authorization computes current access scope
  → Sync queries scoped event window
  → Sync returns events and config_version
```

Boundary constraints:

- Scope computation belongs to authorization.
- Sync does not implement custom containment rules.
- Normal pull is not subject-history backfill or audit pull.

### 10.3 Subject-history backfill flow

Owner: `sync.application.SubjectHistoryBackfillService`

Flow:

```text
request subject-history page
  → Authorization checks request-time access
  → Identity supplies alias/split handling support
  → Sync returns cursor-paginated historical events needed for subject-level projections
```

Boundary constraints:

- Separate from normal live sync.
- Does not lower normal sync watermark.
- Not a general audit API.

### 10.4 Assignment command flow

Owner: `authorization.application.AssignmentCommandService`

Flow:

```text
authenticated actor context
  → validate target assignment command
  → check multi-axis containment / bootstrap/root path
  → append assignment_changed event through eventing
  → projection rebuild/update happens downstream
```

Boundary constraints:

- Request-body actor IDs are not authority.
- `assignment_changed` is not authorized through `activities[*].roles`.
- Ending assignment requires target-assignment authority or explicit root/bootstrap path.

### 10.5 Config publish flow

Owner: `configuration.application.ConfigPackageService`

Flow:

```text
deployer edits config
  → DeployTimeValidationService validates candidate package
  → ConfigPackageService stores full package snapshot
  → Sync advertises new config_version
  → Device downloads package via config endpoint
```

Boundary constraints:

- No partial package publication.
- No bypass around deploy-time validation.
- Config does not define platform mechanisms.

### 10.6 Pattern transition evaluation flow

Owner: `workflow.application.TransitionEvaluationService`, orchestrated by `integrity.application.ConflictDetectionService`

Flow:

```text
accepted source event exists
  → Integrity asks WorkflowTransitionPort whether transition is valid
  → Workflow derives current pattern state from events + binding
  → If invalid, Integrity emits transition_violation flag
```

Boundary constraints:

- Workflow does not reject the event.
- Workflow does not write flags directly.
- Workflow state is projection-derived.

### 10.7 Flag resolution flow

Owner: `integrity.application.FlagResolutionService`

Flow:

```text
resolver request
  → bind authenticated actor
  → load flag and designated_resolver
  → reject non-designated resolver
  → append conflict_resolved/v1 event
  → projections re-derive participation
```

Boundary constraints:

- Request body does not grant resolver identity.
- `resolver_unassigned` cannot be impersonated.
- Resolution is online and server-validated.

---

## 11. Enforcement rules

Use package tests, static analysis, or ArchUnit-style rules.

### Package dependency rules

```text
1. ..modules..domain.. must not depend on ..adapters..
2. ..modules..domain.. must not depend on Spring MVC, JDBC templates, HTTP, or UI packages.
3. ..modules..application.. may depend on same-module domain and ports.
4. ..modules..application.. must not depend on another module's adapters.
5. ..adapters.. may depend on application services and ports.
6. ..admin..adapters.web.. may call application services, not repositories.
7. ..sync.. must not depend on authorization SQL repositories directly.
8. ..integrity.. must not depend on workflow SQL/projection tables directly.
9. ..configuration.. must not depend on sync controllers.
10. shared-kernel must not depend on modules.
```

### Event append rules

```text
1. Only Eventing adapters write the events table.
2. Server-generated events use AppendServerEventPort.
3. Human/admin commands use application services; controllers never write events directly.
4. No module mutates existing events.
```

### Projection rules

```text
1. Projection tables must be rebuildable.
2. Projection rebuild must not require hidden mutable state.
3. Projection divergence is fixed by rebuild or compensating event, not direct canonical patch.
```

### Config rules

```text
1. Config package publication requires deploy-time validation.
2. Pattern definitions are platform-owned contracts.
3. Activity config binds patterns; it does not define transition tables.
4. Expressions stay within bounded grammar.
```

### Mobile rules

```text
1. Mobile advisory checks must be named advisory in code.
2. Mobile must not create canonical server flags unless a future decision allows device-side flag creation.
3. Mobile must not run L3 triggers as canonical policy.
4. Mobile local projections must be rebuildable from local events/config.
```

---

## 12. Split-readiness matrix

| Module | Extract later? | Extraction priority | Primary split contract | Main blockers |
|---|---:|---:|---|---|
| `admin` | Yes | 1 | HTTP/application APIs | Production auth model, stable admin APIs. |
| `configuration` | Yes | 2 | Config package API + schema contracts | Package versioning and validation APIs must be stable. |
| `workflow` | Maybe | 3 | Pattern definition contracts + transition evaluation API | Needs stable pattern state projection and performance data. |
| `policy` | Maybe | 4 | Trigger/auto-resolution API + scheduler contract | Not enough policy runtime evidence yet. |
| `projection` | Maybe | 5 | Event replay stream + read model APIs | Projection consistency/rebuild strategy must mature. |
| `integrity` | Maybe | 6 | Detector API + flag event contract | Cross-module dependencies and resolver routing must stabilize. |
| `authorization` | Maybe | 7 | Scope/authority API | Security-sensitive; hard to split before production auth. |
| `sync` | Maybe | 8 | Sync protocol | Depends on eventing/auth/config/projection boundaries. |
| `identity` | Maybe | 9 | Identity command + alias API | Alias projection must remain rebuildable. |
| `eventing` | Last, if ever | 10 | Append/query event API | Central consistency kernel; splitting adds large cost. |
| `contracts` | Already separable | n/a | Files/package | Not a service. |
| `mobile-runtime` | Already separate | n/a | Sync/config/contracts | Protocol and fixture parity. |

Recommended extraction order if forced:

```text
1. Admin UI
2. Configuration service
3. Workflow/pattern engine
4. Policy scheduler
5. Projection/read-model service
6. Integrity detector service
7. Authorization service
8. Sync service
9. Identity service
10. Eventing service last, if ever
```

Do not extract `eventing` first. Most invariants integrate through the event stream.

---

## 13. Migration plan from current structure

### Stage 1 — Name the boundaries without moving much code

- Create package roots for modules.
- Add README or `package-info.java` boundary descriptions.
- Move only low-risk value objects to `shared-kernel`.
- Keep behavior unchanged.

### Stage 2 — Introduce ports for cross-module calls

- Add `EventStore`, `AppendServerEventPort`, `AuthorizationScopePort`, `FlagEmitterPort`, `IdentityAliasPort`, `PatternStatePort`, and `ConfigPackagePort`.
- Replace direct repository calls across package boundaries with ports.
- Keep adapters backed by current repositories.

### Stage 3 — Move application services into module packages

- Move sync orchestration to `sync.application`.
- Move conflict detection orchestration to `integrity.application`.
- Move assignment command logic to `authorization.application`.
- Move config publish flow to `configuration.application`.
- Move pattern evaluation to `workflow.application`.

### Stage 4 — Make persistence ownership explicit

- Mark projection tables as rebuildable in code comments/migrations.
- Restrict direct table writes to owning adapters.
- Add tests that controllers cannot write repositories directly.

### Stage 5 — Add architecture tests

- Add package dependency tests.
- Add schema parity tests.
- Add event append tests.
- Add mobile/server contract fixture tests.

### Stage 6 — Consider extraction only after pressure appears

Evidence required before extraction:

```text
- module has stable ports;
- module has independent scaling/deployment pressure;
- database ownership is clean;
- contract tests cover server/mobile behavior;
- operational failure modes are known;
- extraction does not weaken canonical invariants.
```

---

## 14. Known cleanup items that affect boundaries

From the implementation alignment review:

### K1 — Flag category table completion

Implementation uses `role_stale` and `temporal_authority_expired` as active categories. Boundary impact: `integrity`, `authorization`, and `configuration` must share a complete flag category catalog through `shared-kernel`/`contracts`, not duplicate local enums.

### K2 — Deterministic flag ID wording

The architecture should require a deterministic input tuple `(source_event_id, shape_ref, flag_category)` and stable namespace/salt. Boundary impact: deterministic flag identity belongs in `integrity` with a stable shared value object; do not let detector modules invent category-specific ID schemes.

### K3 — `events.location_path` historical immutability

Boundary impact: `authorization` may compute location path and `sync` may use it, but historical event metadata must not become a mutable authorization reinterpretation surface. Treat it as write-time infrastructure metadata.

### K4 — Resolver routing enforcement

Boundary impact: `integrity` owns canonical resolver enforcement. `admin` supplies UI and authenticated actor context only; it cannot decide resolver authority.

---

## 15. Future-agent review checklist

Before accepting a new implementation decision, ask:

```text
1. Which module owns this responsibility?
2. Is this a domain rule, application orchestration, adapter concern, or contract change?
3. Does it add or imply a new envelope field?
4. Does it add or imply a new event type?
5. Does it treat a projection as canonical truth?
6. Does it reject a structurally valid state-stale event instead of flagging?
7. Does it let deployers define platform mechanisms?
8. Does it make mobile advisory behavior authoritative?
9. Does it bypass assignment/scope authority through request-body actor IDs?
10. Does it require another module's private table or adapter?
11. Does it make normal live sync into audit/backfill behavior?
12. Does it preserve server/mobile contract parity?
13. Is the future split path clearer or worse after this decision?
```

If any answer violates canonical constraints, the implementation decision should be revised before coding.

---

## 16. Final recommendation

Adopt the modular-monolith structure now, enforce it with package rules and ports, and defer service extraction.

The immediate implementation task is not to split services. It is to prevent the current codebase from accumulating hidden coupling around four high-risk seams:

```text
sync ↔ authorization
integrity ↔ authorization/workflow/configuration
configuration ↔ workflow
admin ↔ domain command authority
```

Create explicit ports at those seams first. Physical extraction can remain a later operational decision.
