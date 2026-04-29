# 002 — Code Survey

> **Purpose**: Maps the current state of the codebase against the domain areas
> listed in [001-spec-claims.md](./001-spec-claims.md). Describes what is
> actually implemented — no gap analysis, no correctness judgment.
> Generated 2026-04-29.

---

## DOMAIN: Event Envelope & Append-Only Store

**Files**:
- `server/src/main/java/dev/datarun/ship1/event/Event.java`
- `server/src/main/java/dev/datarun/ship1/event/EventRepository.java`
- `server/src/main/java/dev/datarun/ship1/event/EventMapper.java`
- `server/src/main/resources/db/migration/V1__ship1_schema.sql`
- `contracts/envelope.schema.json`

**What exists**: The `Event` Java record carries the full 11-field envelope (id, type, shapeRef, activityRef, subjectType, subjectId, actorId, deviceId, deviceSeq, syncWatermark, timestamp, payload). `EventRepository` is the sole writer to the `events` table using `INSERT ... ON CONFLICT (id) DO NOTHING` — no UPDATE or DELETE paths exist. The DB schema enforces a CHECK constraint on `type` (6-value closed enum) and `subject_type` (4-value closed enum). `sync_watermark` is a `BIGSERIAL` assigned on insert. A `UNIQUE(device_id, device_seq)` constraint enforces causal ordering uniqueness. `EventMapper` translates between the wire-format JSON (nested `subject_ref`/`actor_ref` objects) and the flat Java record.

**Ship it landed in**: Ship-1 (`0a57363`), evolved through Ship-2 (ServerEmission extraction at `8ca6675`) and Ship-3 (multi-version shape routing).

**Notable**: The `events` table has no `received_at` index despite the column existing. `sync_watermark` is `NOT NULL` in the DB (`BIGSERIAL`) but nullable (`Long`) in the Java record — the Java side treats null as "not yet synced" for server-authored events, but the DB always assigns a value on insert. `activity_ref` is nullable in both schema and code but is null in every event observed across all Ships so far.

---

## DOMAIN: Envelope Validation

**Files**:
- `server/src/main/java/dev/datarun/ship1/event/EnvelopeValidator.java`
- `server/src/main/resources/schemas/envelope.schema.json`

**What exists**: `EnvelopeValidator` loads the envelope JSON Schema (Draft 2020-12) from classpath at `@PostConstruct` and validates every inbound push event against it before persistence. Returns a list of human-readable violation messages; empty list means valid. Uses the `networknt/json-schema-validator` library. The schema enforces required fields (id, type, shape_ref, subject_ref, actor_ref, device_id, device_seq, timestamp, payload), closed enum for `type` (6 values) and `subject_ref.type` (4 values), UUID format on id/device_id/subject_ref.id, regex pattern on `shape_ref` (`^[a-z][a-z0-9_]*/v[0-9]+$`), and `additionalProperties: false` at the envelope root.

**Ship it landed in**: Ship-1 (`0a57363`).

**Notable**: The bundled `schemas/envelope.schema.json` is byte-identical to `contracts/envelope.schema.json` (drift-gate enforced per FP-007). `sync_watermark` and `activity_ref` are not in the `required` array — they are optional on inbound push.

---

## DOMAIN: Shape & Payload Validation

**Files**:
- `server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java`
- `server/src/main/resources/schemas/shapes/*.schema.json` (8 files)
- `contracts/shapes/*.schema.json` (8 files, parity-enforced)

**What exists**: `ShapePayloadValidator` loads all shape schemas from `classpath:schemas/shapes/` at boot. It supports two filename conventions: `<name>.schema.json` → `<name>/v1` (Ship-1 single-version) and `<name>.v<N>.schema.json` → `<name>/v<N>` (Ship-3 multi-version). At load time, each schema is checked against the ADR-004 §S13 60-field budget via `validateShapeBudget()` — schemas exceeding 60 top-level properties cause boot failure. At runtime, `validate(shapeRef, payload)` looks up the schema by the event's `shape_ref` string and validates the payload. Unknown `shape_ref` values produce a `shape_unknown:` marker that surfaces as HTTP 400. Eight shapes are registered: `assignment_created/v1`, `assignment_ended/v1`, `conflict_detected/v1`, `conflict_resolved/v1`, `household_observation/v1`, `household_observation/v2`, `subjects_merged/v1`, `subject_split/v1`.

**Ship it landed in**: Ship-1 (`0a57363`); multi-version routing and field-budget guard added Ship-3 (`fa85683`, `0107da9`, `7540fc2`).

**Notable**: `assignment_ended/v1` schema exists and is registered but no code path emits or consumes `assignment_ended` events (FP-018). The field-budget guard is startup-only — no HTTP endpoint enforces it at deploy time (FP-012b). The `validateShapeBudget` method is `public static` and documented as callable from a future `POST /api/shapes` endpoint.

---

## DOMAIN: Sync Protocol (Push / Pull)

**Files**:
- `server/src/main/java/dev/datarun/ship1/sync/SyncController.java`
- `contracts/sync-protocol.md`

**What exists**: `SyncController` exposes `POST /api/sync/push` and `POST /api/sync/pull`. **Push**: accepts a batch of events as a JSON array, validates each (envelope + shape payload) — rejects the entire batch on any validation error. Inserts events one-by-one with dedup. For each newly-inserted `type=capture` event, runs `ConflictDetector.detect()`. Before persistence, runs `CycleGuard.checkBatch()` over alias events and emits `cycle_violation` flags post-persist. Returns `{accepted, duplicates, flags_raised}`. **Pull**: authenticated via `ActorAuthInterceptor`. Reconstructs the caller's scope at-now via `ScopeResolver`, then streams events in ascending `sync_watermark` order with scope filtering. Over-fetches in pages of 500 to fill the requested limit (max 500). Returns `{events[], latest_watermark}`. Scope filtering is inline: assignment events filtered by target_actor, household_observations by village_ref, conflict_detected by subject_id membership. Default-deny for unrecognized shapes.

**Ship it landed in**: Ship-1 (`0a57363`); cycle-guard integration added Ship-3 closeout (`a582e59`).

**Notable**: Push does not authenticate the caller — `ActorAuthInterceptor` is only registered on `/api/sync/**` but push validation does not cross-check `actor_ref` in the event against the bearer token identity. Pull uses at-now scope (not per-event-time), documented as a Ship-1 simplification. The `subjectsInScopes` helper scans all `household_observation/*` events on every pull — no index optimization.

---

## DOMAIN: Authorization & Scope Resolution

**Files**:
- `server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java`
- `server/src/main/java/dev/datarun/ship1/sync/ActorAuthInterceptor.java`
- `server/src/main/java/dev/datarun/ship1/sync/CoordinatorAuthInterceptor.java`
- `server/src/main/java/dev/datarun/ship1/sync/ActorTokenRepository.java`
- `server/src/main/java/dev/datarun/ship1/sync/WebConfig.java`

**What exists**: `ScopeResolver` reconstructs an actor's authorization scope by replaying all `assignment_created/v1` events, filtering by `target_actor.id` and temporal validity (`valid_from`/`valid_to`). It exposes `activeGeographicScopes(actorId, at)` returning village UUIDs and `hasRoleAt(actorId, role, at)` for projection-derived role checks. No cache — full replay every call. `ActorAuthInterceptor` resolves `Authorization: Bearer <token>` headers against `actor_tokens` table; registered on `/api/sync/**`. `CoordinatorAuthInterceptor` does the same plus a `hasRoleAt("coordinator")` check; registered on `/admin/subjects/**`. `WebConfig` wires both interceptors to their respective path patterns. `ActorTokenRepository` is a simple token→actor_id lookup with `ON CONFLICT DO NOTHING` insert.

**Ship it landed in**: Ship-1 (`0a57363`); `hasRoleAt` and `CoordinatorAuthInterceptor` added Ship-2 (`ecf3ece`, `3b92e50`).

**Notable**: `ScopeResolver` reads only `assignment_created/v1` — it does not consume `assignment_ended/v1` events (FP-018). The `valid_to` field on the assignment event is read but no code path ever creates an `assignment_ended` event. No `role_stale` or `temporal_authority_expired` detection exists (OC-1, OC-3). Admin UI paths (`/admin/events`, `/admin/flags`) have no auth interceptor.

---

## DOMAIN: Conflict Detection & Flagging

**Files**:
- `server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java`
- `contracts/flag-catalog.md`
- `contracts/shapes/conflict_detected.schema.json`

**What exists**: `ConflictDetector` runs post-persist on every `type=capture` event whose `shape_ref` starts with `household_observation/`. It checks two categories: (1) **scope_violation** — the event's `village_ref` is not in the capturing actor's active geographic scope at event timestamp; (2) **identity_conflict** — a prior `household_observation/*` event exists with the same normalized `household_name` in the same `village_ref` but a different `subject_id`. Flags are emitted as `type=alert`, `shape_ref=conflict_detected/v1` events with `actor_id=system:conflict_detector/<category>`. Flag payloads contain `source_event_id`, `flag_category`, `resolvability` (always `manual_only`), `reason`, and `designated_resolver`. Identity-conflict flags include `related_event_ids` and `related_subject_ids` arrays. Flag UUIDs are `UUID.randomUUID()` — not UUIDv5 deterministic derivation as stated in CLAIM-17.

**Ship it landed in**: Ship-1 (`0a57363`); identity_conflict detection upgraded to cross-version matching at Ship-3 closeout (`dab7e6a`).

**Notable**: The flag catalog document (`contracts/flag-catalog.md`) lists 9 categories but only 3 have detection code (`scope_violation`, `identity_conflict`, `cycle_violation`). Categories 1 (`concurrent_state_change`), 2 (`stale_reference`), 5 (`temporal_authority_expired`), 6 (`role_stale`), 7 (`domain_uniqueness_violation`), 8 (`transition_violation`) have no detector implementation. The flag-catalog.md file contains a duplicate section — the catalog content appears twice with slightly different wording (lines 1-41 and 42-77). Flag resolution is not implemented — no `conflict_resolved` events are emitted by any code path (the schema exists but is unused except in contract fixtures).

---

## DOMAIN: Alias Cycle Guard

**Files**:
- `server/src/main/java/dev/datarun/ship1/integrity/CycleGuard.java`

**What exists**: `CycleGuard` is a pre-persist batch-serial cycle detection guard for alias events (`subjects_merged/*`, `subject_split/*`). It builds a directed graph from all persisted merge/split events plus earlier-in-batch accepted edges, then runs DFS from each new alias edge to detect if adding it would close a cycle. Returns a map of `{event_id → cycle_path}` for cycle-positive events. Accept-and-flag is preserved: the alias event is still inserted by the caller; `emitCycleFlag()` creates a `conflict_detected/v1` event with `flag_category=cycle_violation`, `resolvability=manual_only`, `actor_id=system:cycle_guard/cycle_violation`, and includes the `cycle_path` array in the payload. Merge edges are `retired_id → surviving_id`; split edges are `source_id → successor_id` (one per successor).

**Ship it landed in**: Ship-3 closeout (`a582e59`, `0b14607`).

**Notable**: The flag construction in `CycleGuard` is inline (not shared with `ConflictDetector.buildFlag`) — documented as intentional to avoid blast radius from parameterizing the actor-ref prefix. Self-loops are detected as a special case.

---

## DOMAIN: Subject Identity (Merge / Split / Alias Projection)

**Files**:
- `server/src/main/java/dev/datarun/ship1/admin/AdminSubjectsController.java`
- `server/src/main/java/dev/datarun/ship1/admin/SubjectAliasProjector.java`
- `server/src/main/java/dev/datarun/ship1/admin/SubjectLifecycleProjector.java`
- `contracts/shapes/subjects_merged.schema.json`
- `contracts/shapes/subject_split.schema.json`

**What exists**: `AdminSubjectsController` exposes coordinator-only endpoints: `POST /admin/subjects/merge`, `POST /admin/subjects/split`, and `GET /admin/subjects/{id}/canonical`. Merge requires `surviving_id` and `retired_id` (must differ, neither archived). Split requires `source_id` and `successor_ids` (≥2, unique, not equal to source, source not archived). Both emit server-authored events with `type=capture`, coordinator's UUID as `actor_ref`, `ServerEmission.SERVER_DEVICE_ID` as `device_id`, and sequence from `server_device_seq` Postgres sequence. Payloads are validated against shape schemas before insertion. `SubjectAliasProjector` builds an eager-transitive-closure alias map from `subjects_merged/v1` events — no cache, no table, full replay per call. `canonicalId(id)` resolves retired→surviving chains. `aliasChainLength(id)` counts merge hops. `SubjectLifecycleProjector` determines if a subject is "archived" (appears as `retired_id` in a merge or `source_id` in a split) — also no cache.

**Ship it landed in**: Ship-2 (`dce4130`, `17461d9`); canonical endpoint and alias projection also Ship-2.

**Notable**: No synthetic transitive edges are written — transitive closure is read-time only. Duplicate-retired handling uses earliest-watermark-wins. Split successor_ids minimum is 2 (hardcoded). `ConflictDetector` is alias-blind by design — it never calls `SubjectAliasProjector`.

---

## DOMAIN: Server-Authored Event Infrastructure

**Files**:
- `server/src/main/java/dev/datarun/ship1/event/ServerEmission.java`
- `server/src/main/resources/db/migration/V1__ship1_schema.sql` (sequence `server_device_seq`)

**What exists**: `ServerEmission` provides a single reserved server device UUID (`00000000-0000-0000-0000-000000000001`) and a `synchronized` method `nextServerDeviceSeq()` that draws from the Postgres sequence `server_device_seq`. Used by `ConflictDetector`, `CycleGuard`, and `AdminSubjectsController` for all server-emitted events. The sequence survives server restart.

**Ship it landed in**: Ship-1 (`0a57363`); extracted as shared component Ship-2 (`8ca6675`).

**Notable**: The `DevBootstrapController` uses a separate device UUID (`00000000-...0000ff`) for seed assignment events — it does not use `ServerEmission`. Bootstrap sequence numbers are derived by scanning existing events rather than using the Postgres sequence.

---

## DOMAIN: Configuration Delivery

**Files**:
- `server/src/main/java/dev/datarun/ship1/config/ConfigController.java`
- `server/src/main/java/dev/datarun/ship1/config/VillageRepository.java`
- `server/src/main/resources/db/migration/V1__ship1_schema.sql` (`villages` table)

**What exists**: `ConfigController` exposes `GET /api/sync/config` (authenticated via `ActorAuthInterceptor`). Returns a JSON package containing: `version: 1`, a single activity (`household_observation`), a single shape (`household_observation/v1` with its full JSON Schema), and scope-filtered villages (only villages matching the actor's active geographic assignments). `VillageRepository` wraps a simple `villages` table (id, district_name, name) with upsert and filtered queries.

**Ship it landed in**: Ship-1 (`0a57363`).

**Notable**: Config delivery always returns `household_observation/v1` — it does not include v2 even though the shape is registered. No atomic config delivery mechanism (CLAIM-8 unexercised). No versioning beyond the hardcoded `"version": 1`. The endpoint loads the schema from classpath directly (`loadShapeJson`) rather than using `ShapePayloadValidator`'s registry.

---

## DOMAIN: Admin UI

**Files**:
- `server/src/main/java/dev/datarun/ship1/admin/AdminController.java`
- `server/src/main/resources/templates/admin/events.html`
- `server/src/main/resources/templates/admin/flags.html`

**What exists**: `AdminController` is a Thymeleaf MVC controller with three routes: `/admin/` (redirects to `/admin/events`), `/admin/events` (renders all events in watermark order), `/admin/flags` (renders all `conflict_detected/*` events). The events view shows the full event stream; the flags view lists unresolved flags. No resolution actions are implemented — viewing only.

**Ship it landed in**: Ship-1 (`0a57363`); events view updated Ship-3 to render mixed v1/v2 stream (`22c9013`).

**Notable**: No authentication on admin UI routes. No flag resolution pipeline. "Unresolved flags" is defined as all `conflict_detected/*` events since no resolution mechanism exists to filter them.

---

## DOMAIN: Dev Bootstrap

**Files**:
- `server/src/main/java/dev/datarun/ship1/admin/DevBootstrapController.java`

**What exists**: `POST /dev/bootstrap` seeds the development environment: creates 2 villages (district "Mirpur"), 3 actors (2 CHVs + 1 coordinator) with bearer tokens, and emits `assignment_created/v1` events binding each CHV to one village and the coordinator to an all-null-scope assignment with `role=coordinator`. Uses a separate dev device UUID. Returns all created IDs and tokens in the response.

**Ship it landed in**: Ship-1 (`0a57363`); coordinator seeding added Ship-2 (`da661d7`).

**Notable**: Not idempotent by default (generates random UUIDs each call). The Javadoc mentions a `seed=fixed` parameter for idempotency but no implementation of that parameter exists in the code.

---

## DOMAIN: State Machines / Projection Patterns

**Files**: None in `server/`.

**What exists**: No state machine, pattern registry, or projection engine implementation exists in the server codebase. The concepts are specified in ADR-005 and referenced by CLAIM-10, CLAIM-11, and UI-1, but no Java code implements lifecycle state derivation, pattern definitions, or the `capture_with_review` pattern.

**Ship it landed in**: N/A — not yet implemented.

**Notable**: The `contracts/fixtures/projection-equivalence.json` file defines a cross-platform projection equivalence fixture (7 events including capture, flag, merge, and resolution) with expected output, but no Java projection engine consumes it.

---

## DOMAIN: Expression Language

**Files**: None in `server/`.

**What exists**: No expression evaluator exists in the server codebase. The `contracts/fixtures/expression-evaluation.json` file defines 43 cross-platform evaluation cases covering operators (eq, neq, gt, gte, lt, lte, in, not_null, and, or, not, ref) with value resolution across namespaces (payload.*, context.*, entity.*, event.*), type coercion rules, and null-safety semantics. No Java implementation consumes these fixtures.

**Ship it landed in**: N/A — not yet implemented.

**Notable**: The fixture is comprehensive and implementation-ready. It references `context.*` properties (CLAIM-13) but no resolution mechanism exists.

---

## DOMAIN: Trigger Engine

**Files**: None in `server/`.

**What exists**: No trigger engine (L3a event-reaction or L3b deadline-check) exists in the server codebase.

**Ship it landed in**: N/A — not yet implemented.

**Notable**: CLAIM-7 (triggers execute server-only) and CLAIM-14 (auto-resolution as L3b) are both decided-unexercised.

---

## DOMAIN: Flag Resolution

**Files**:
- `contracts/shapes/conflict_resolved.schema.json` (schema only)

**What exists**: The `conflict_resolved/v1` shape schema exists in contracts and is registered by `ShapePayloadValidator` at boot. However, no code path emits `conflict_resolved` events. The admin flags view (`/admin/flags`) is read-only. The `contracts/fixtures/projection-equivalence.json` contains a `type=review`, `shape_ref=conflict_resolved/v1` resolution event as a test fixture, but no server endpoint produces one. The flag catalog documents three resolution options (accepted, rejected, reclassified) but none are implemented.

**Ship it landed in**: Schema registered Ship-1; no resolution code in any Ship.

**Notable**: The projection-equivalence fixture treats resolution as already specified (uses `type=review` for manual resolution per the catalog), which provides a contract for future implementation.

---

## DOMAIN: Assignment Lifecycle (assignment_ended)

**Files**:
- `contracts/shapes/assignment_ended.schema.json` (schema only)

**What exists**: The `assignment_ended/v1` shape schema exists and is registered by `ShapePayloadValidator`. No code path emits `assignment_ended` events. `ScopeResolver` reads only `assignment_created/v1` events and uses the `valid_to` field on those events for temporal filtering — it does not look for `assignment_ended` events.

**Ship it landed in**: Schema registered Ship-1; no consumption code in any Ship.

**Notable**: This is the documented EXERCISED-VIOLATED claim (CLAIM-18 / FP-018). The append-only invariant says ending an assignment requires a discrete event, but `ScopeResolver` uses `valid_to` from the creation event instead.

---

## Summary File Inventory

| Package | Production Files | Test Files |
|---|---|---|
| `event` | Event, EventMapper, EventRepository, EnvelopeValidator, ShapePayloadValidator, ServerEmission | FieldCountBudgetTest, ShapePayloadValidatorTest |
| `sync` | SyncController, ActorAuthInterceptor, CoordinatorAuthInterceptor, ActorTokenRepository, WebConfig | CoordinatorAuthInterceptorTest |
| `scope` | ScopeResolver | ScopeResolverRoleTest, ScopeViolationTemporalDivergenceTest |
| `integrity` | ConflictDetector, CycleGuard | AliasCycleGuardTest |
| `admin` | AdminController, AdminSubjectsController, DevBootstrapController, SubjectAliasProjector, SubjectLifecycleProjector | AdminSubjectsControllerTest, DevBootstrapCoordinatorTest, SubjectAliasCanonicalEndpointTest, SubjectAliasProjectorTest |
| `config` | ConfigController, VillageRepository | — |
| `(root)` | DatarunApplication | WalkthroughAcceptanceTest, Ship2WalkthroughAcceptanceTest, Ship3WalkthroughAcceptanceTest, AdrInvariantTest |

**Total**: 22 production Java files, 14 test Java files, 1 SQL migration, 8 shape schemas, 1 envelope schema, 2 contract fixtures.
