# Datarun Platform

## What This Is

The implementation repository for the Datarun operations platform — a domain-agnostic operational backbone. Event-sourced, offline-first, append-only architecture.

**Design docs**: `design/docs/` (git submodule → github.com/Hamza-ye/datarun.git)

---

## Current Phase

**Phase 3: Configuration** — in progress (3a complete, 3b/3c remaining)

- Phase 0 (Core Loop): COMPLETE
- Phase 1 (Identity & Integrity): COMPLETE — 64 total tests
- Phase 2 (Authorization & Multi-Actor): COMPLETE — 80 tests before Phase 3
- Phase 3a (Shapes + Config Delivery): COMPLETE — 80 server + 33 mobile tests
- Phase 3b (Expressions + DtV): Not started
- Phase 3c (Config Packager + Full Pipeline): Not started

See [docs/status.md](docs/status.md) for detailed status and quality gate results.

Phase spec: `design/docs/implementation/phases/phase-3.md`
IDRs: IDR-017 (shape storage), IDR-018 (expression grammar), IDR-019 (config package) in `design/docs/decisions/`

---

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Server | Java / Spring Boot | Java 17 (Temurin via sdkman), Spring Boot 3.2.5 |
| Server DB | PostgreSQL | 16.x (JSONB for event payloads) |
| Mobile | Dart / Flutter | Flutter 3.41.7 |
| Device DB | SQLite | sqflite 2.4.2 |
| Admin UI | Thymeleaf (server-rendered) | IDR-006 |
| Contracts | JSON Schema (Draft 2020-12) | networknt json-schema-validator 1.4.0 |
| CI | GitHub Actions | postgres:16-alpine service container |

---

## Build & Test

```bash
# Start test database (host networking — works with VPN)
docker compose -f docker-compose.test.yml up -d

# Run all server tests (80 tests, ~40s)
cd server && ./mvnw test

# Run single test class
./mvnw test -Dtest=ConfigIntegrationTest

# Run mobile tests
cd mobile && flutter test

# Stop test database
docker compose -f docker-compose.test.yml down
```

### Dev Environment Notes

- **VPN + Docker**: Use `network_mode: host` — Windscribe breaks port forwarding.
- **Test DB port**: `localhost:15432` (local), `localhost:5432` (CI).
- **Java**: `sdk use java 17.0.14-tem`
- **Flutter**: `/home/hamza/dev/flutter`, Android SDK at `/home/hamza/Android/Sdk`

---

## Git Workflow

**Branch model**: Single `main` branch. No PRs — solo dev with AI agents.

**Commit cadence**: At sub-phase boundaries, when quality gates pass (all tests green).

**Commit convention**: Conventional commits with scope tags.
```
feat(server): Phase 3a shapes, activities, config endpoint     # server code
feat(mobile): Phase 3a config store, shape rewrite, form engine # mobile code
docs: update CLAUDE.md codebase map + status.md for Phase 3a    # docs-only
```

For sub-phases touching both server and mobile, split into 2-3 commits:
1. `feat(server): ...` — server code + server tests
2. `feat(mobile): ...` — mobile code + mobile tests
3. `docs: ...` — CLAUDE.md codebase map + status.md (can fold into #2 if small)

**Tags**: Lightweight tag at each phase boundary: `phase-3a`, `phase-3b`, etc.

**Before committing** (agent responsibility):
- All tests green (`./mvnw test` and/or `flutter test`)
- CLAUDE.md codebase map updated to reflect new/changed/removed files
- `docs/status.md` updated with current test counts and sub-phase status
- No leftover TODOs, dead imports, or commented-out code

**Agent co-building rules**:
- Agents own the code they write + the codebase map entries for it
- After a subagent finishes, the orchestrator verifies tests and reviews key files before committing
- Never commit with failing tests, even if "only docs changed"
- `config_loader.dart`-style dead code gets deleted, not deprecated

---

## Repository Structure

```
contracts/                         # Language-neutral shared definitions
  envelope.schema.json             # 11-field event envelope (JSON Schema Draft 2020-12)
  sync-protocol.md                 # Push/pull sync contract
  flag-catalog.md                  # 6 flag categories (3 identity + 3 auth)
  fixtures/projection-equivalence.json
  shapes/                          # Assignment event schemas

server/src/main/java/dev/datarun/server/
  event/                           # Event store + envelope validation
  sync/                            # Push/pull pipeline (2-Tx)
  subject/                         # Subject projection + REST
  identity/                        # Server identity, alias cache, merge/split
  integrity/                       # Conflict detection + resolution + sweep job
  authorization/                   # Token auth, assignments, scope resolution, locations
  config/                          # Phase 3: shapes, activities, config packaging
  admin/                           # Thymeleaf admin UI controller

server/src/main/resources/
  db/migration/V1-V5               # Flyway migrations (5 total)
  templates/                       # Thymeleaf HTML (admin, config)
  envelope.schema.json             # Bundled for server-side validation

server/src/test/java/              # 12 test classes, 80 server integration tests
  AbstractIntegrationTest.java     # Base: test actor provisioning, auth helpers

mobile/lib/
  domain/                          # Event, Shape (IDR-017), SubjectSummary models
  data/                            # EventStore, SyncService, ConfigStore, ProjectionEngine
  presentation/                    # AppState, screens (WorkList, SubjectDetail, Form), widgets

design/                            # Git submodule → datarun docs repo
  docs/architecture/               # Primitives, patterns, contracts, boundaries
  docs/adrs/                       # 5 ADRs (all DECIDED)
  docs/decisions/                  # 19 IDRs
  docs/implementation/             # Plan, execution plan, UX model, phase specs
  docs/scenarios/                  # 22 operational scenarios
```

---

## Codebase Map — Server

### event/ — Event Store
| File | Purpose | Key API |
|------|---------|---------|
| `Event.java` | 11-field event record | `record(id, type, shapeRef, activityRef, subjectRef, actorRef, deviceId, deviceSeq, syncWatermark, timestamp, payload)` |
| `EventRepository.java` | Event persistence + scoped queries | `insert(Event)→bool`, `findSince(watermark,limit)`, `findSinceScoped(watermark,limit,actorId,scopePaths)`, `findBySubjectId(UUID)`, `hasNewerEventsFromOtherDevices(...)` |
| `EnvelopeValidator.java` | JSON Schema validation of event envelope | `validate(JsonNode)→List<String>` |

### sync/ — Push/Pull Pipeline
| File | Purpose | Key API |
|------|---------|---------|
| `SyncController.java` | 2-Tx push/pull: Tx1 persists, Tx2 runs conflict detection | `POST /api/sync/push` (no auth), `POST /api/sync/pull` (Bearer token, scope-filtered). Push validates envelope → shape payload → persist → detect conflicts. Pull returns events + config_version. |

### subject/ — Subject Projection
| File | Purpose | Key API |
|------|---------|---------|
| `SubjectProjection.java` | Derive subject summaries from events; alias-aware, flag-excluding | `listSubjects()→List<SubjectSummary>` |
| `SubjectSummary.java` | Subject record: id, latestEventType, latestTimestamp, eventCount, flagCount | Record |
| `SubjectController.java` | REST subject endpoints | `GET /api/subjects`, `GET /api/subjects/{id}/events` (alias chain resolution) |

### identity/ — Identity Management
| File | Purpose | Key API |
|------|---------|---------|
| `ServerIdentity.java` | Stable server device_id + SEQUENCE device_seq | `getDeviceId()`, `nextDeviceSeq()` |
| `AliasCache.java` | In-memory ConcurrentHashMap retired→surviving | `resolve(UUID)→UUID`, `isRetired(UUID)`, `refresh()` |
| `IdentityService.java` | Merge/split with DD-3 row-level locking | `merge(retiredId, survivingId, actorId, reason)→Event`, `split(sourceId, actorId, reason)→Event` |
| `IdentityController.java` | REST | `POST /api/identity/merge`, `POST /api/identity/split` |

### integrity/ — Conflict Detection & Resolution
| File | Purpose | Key API |
|------|---------|---------|
| `ConflictDetector.java` | Per-event identity CD + auth CD; creates flag events | `evaluate(events, lastPullWatermark)→List<Event>`, `evaluateAuth(events, actorId)→List<Event>`, `sweep(trailingWatermark)→List<Event>` |
| `ConflictResolutionService.java` | Resolve flags, create manual identity conflicts | `resolve(flagEventId, resolution, reclassifiedSubjectId, actorId, reason)→Event`, `listUnresolvedFlags()`, `getFlagDetail(UUID)` |
| `ConflictController.java` | REST flag operations | `GET /api/conflicts`, `POST /api/conflicts/{flagId}/resolve`, `POST /api/conflicts/identity` |
| `ConflictSweepJob.java` | 5-min scheduled sweep | `@Scheduled sweep()` |

### authorization/ — Token Auth & Scope
| File | Purpose | Key API |
|------|---------|---------|
| `ActorTokenInterceptor.java` | Bearer token → actor_id on /api/sync/pull | `preHandle(...)` |
| `ActorTokenRepository.java` | Token CRUD (SecureRandom 32-byte hex) | `createToken(actorId)→String`, `resolveToken(token)→UUID`, `revoke(token)` |
| `ScopeResolver.java` | Reconstruct assignments from events; scope containment | `getActiveAssignments(actorId)→List<ActiveAssignment>`, `isInScope(assignments, locationPath, subjectId, activityRef)→bool` |
| `ActiveAssignment.java` | Assignment record with containment tests | `isActive()`, `containsGeographically(path)`, `containsSubject(id)`, `containsActivity(ref)` |
| `AssignmentService.java` | Create/end assignments with S5 scope-containment | `createAssignment(...)→Event`, `endAssignment(id, actorId, reason)→Event` |
| `LocationRepository.java` | Geographic hierarchy with materialized paths (IDR-014) | `findById(UUID)`, `findPathById(UUID)→String`, `insert(id, name, parentId, level)` |
| `SubjectLocationRepository.java` | Subject→location mapping with denormalized path | `findPathBySubjectId(UUID)→String`, `upsert(subjectId, locationId, locationPath)` |
| `WebConfig.java` | Registers token interceptor | Interceptor on `/api/sync/pull` |

### config/ — Phase 3: Configuration
| File | Purpose | Key API |
|------|---------|---------|
| `Shape.java` | Shape record (IDR-017) | `record(name, version, status, sensitivity, schemaJson, createdAt)`, `shapeRef()→"{name}/v{version}"` |
| `ShapeRepository.java` | Shape persistence | `insert(Shape)`, `findByNameAndVersion(name,ver)→Optional`, `findAll()`, `findActive()`, `updateStatus(name,ver,status)` |
| `ShapeService.java` | DtV L1 validation: field budget ≤60, type vocabulary, name format | `createShape(name, sensitivity, schemaJson)→List<String>`, `createVersion(name, sensitivity, schemaJson)→List<String>`, `deprecate(name,ver)`. Constants: `VALID_TYPES` (10 types), `VALID_TYPES_LIST`. Static: `parseShapeRef(ref)→String[]` |
| `Activity.java` | Activity record | `record(name, configJson, status, sensitivity, createdAt)` |
| `ActivityRepository.java` | Activity persistence | `insert(Activity)`, `update(Activity)`, `findByName(name)→Optional`, `findAll()`, `findActive()` |
| `ActivityService.java` | Activity validation: name format, shapes exist | `createActivity(name, sensitivity, configJson)→List<String>`, `updateActivity(...)`, `deprecate(name)` |
| `ConfigPackager.java` | Assembles IDR-019 JSON package (all shapes, active activities, empty expressions/flags) | `publish(publishedBy)→int version`, `getLatest()→Optional<ConfigPackage>`, `getLatestVersion()→int` |
| `ConfigApiController.java` | Config delivery endpoint with ETag | `GET /api/sync/config` (If-None-Match → 304) |
| `ConfigAdminController.java` | Thymeleaf admin: shape CRUD, activity CRUD, publish | Routes under `/admin/config/` |
| `ShapePayloadValidator.java` | Validates event payload against shape fields; unknown shapes pass through | `validate(shapeRef, payload)→List<String>` |

### admin/ — Admin UI
| File | Purpose |
|------|---------|
| `AdminController.java` | Thymeleaf: subject list, detail, flags, assignments | Routes under `/admin/` |

---

## Codebase Map — Mobile

### domain/ — Models
| File | Purpose |
|------|---------|
| `event.dart` | 11-field Event class; `toMap()` (SQLite), `toEnvelope()` (sync), `fromMap()`, `fromServerJson()` |
| `shape.dart` | IDR-017 format. ShapeField(name,type,required,description,displayOrder,group,deprecated,options:List\<String\>), ShapeDefinition(shapeRef,name,version,status,sensitivity,fields). `activeFields` getter filters deprecated + sorts by displayOrder. Factory: `fromConfigJson(shapeRef, json)` |
| `subject_summary.dart` | SubjectSummary(subjectId, subjectType, latestTimestamp, name, captureCount, flagCount) |

### data/ — Data Layer
| File | Purpose | Key API |
|------|---------|---------|
| `event_store.dart` | SQLite: events, aliases, assignments, config. DB version=4 | `insert(Event)`, `insertFromServer(Event)`, `getUnpushed()`, `markPushed(ids)`, `getBySubject(id)`, `getAll()`, `getAllAliases()`, `getActiveAssignments()`, `purgeOutOfScopeEvents(deviceId)`, `getConfigPackage()`, `saveConfigPackage(version, json)` |
| `event_assembler.dart` | Assembles 11-field envelope from form state | `assemble(subjectId, shapeRef, payload)→Future<Event>` |
| `sync_service.dart` | Push→pull pipeline with actor token auth + config download after pull | `sync()→Future<SyncResult>`. Downloads config via GET /api/sync/config with If-None-Match when server reports newer config_version. |
| `config_store.dart` | SQLite-persisted config cache (IDR-019). Parses shapes/activities into memory on init. | `init()`, `applyConfig(json)`, `getShape(ref)→ShapeDefinition?`, `getActivity(name)`, `getActiveActivities()→List<String>`, `getShapesForActivity(name)→List<ShapeDefinition>`, `configVersion→int` |
| `device_identity.dart` | Persists device_id, actor_id, device_seq, actor_token | `init()→Future<DeviceIdentity>`, `nextSeq()→Future<int>`, `get/set actorToken` |
| `projection_engine.dart` | Subject list + detail from events; alias-aware, flag-excluding | `getSubjectList()→Future<List<SubjectSummary>>`, `getSubjectDetail(id)→Future<List<Event>>`, `getFlaggedEventIds()→Future<Set<String>>` |

### presentation/ — UI
| File | Purpose |
|------|---------|
| `app_state.dart` | ChangeNotifier: subjects, assignments, sync status, pending count. Uses ConfigStore (not ConfigLoader). `refresh()`, `sync()` |
| `screens/work_list_screen.dart` | Subject list, sync indicator, FAB. Config-driven shape selection (single→direct, multiple→dialog, none→message) |
| `screens/subject_detail_screen.dart` | Event timeline with flags. Config-driven shape selection for capture action |
| `screens/form_screen.dart` | Shape-driven form via `configStore.getShape(shapeRef)` (sync). Uses `activeFields` for rendering. Dirty tracking, save. |
| `widgets/sync_panel.dart` | Modal: sync trigger, last sync time, device ID |
| `widgets/widget_mapper.dart` | Maps field type → Flutter widget. Types: text, integer, decimal, number, date, select, multi_select (chips), boolean, narrative (multiline), location, subject_ref |

---

## Database Migrations

| Version | File | Tables/Changes |
|---------|------|----------------|
| V1 | `V1__create_events_table.sql` | events (BIGSERIAL sync_watermark) |
| V2 | `V2__server_identity_and_device_sync.sql` | server_identity, server_device_seq SEQUENCE, device_sync_state |
| V3 | `V3__subject_aliases_and_lifecycle.sql` | subject_aliases (CHECK constraint), subject_lifecycle |
| V4 | `V4__authorization_tables.sql` | locations (materialized path), actor_tokens, subject_locations, events.location_path, covering index, assignment expression index |
| V5 | `V5__shapes_and_config.sql` | shapes (PK: name+version), activities (PK: name), expression_rules (UUID PK), config_packages (PK: version), device_sync_state.config_version |

---

## Test Classes — Server (80 tests)

| Class | Tests | Module |
|-------|-------|--------|
| `SyncControllerIntegrationTest` | 8 | Push/pull pipeline, pagination |
| `MultiDeviceE2ETest` | 8 | Multi-device sync, conflict flags |
| `IdentityResolverIntegrationTest` | 6 | Merge/split, alias chains |
| `ConflictDetectorIntegrationTest` | 8 | W_effective, stale_reference detection |
| `ConflictResolutionIntegrationTest` | 6 | Resolve flags, manual identity conflicts |
| `ProjectionEquivalenceTest` | 6 | Server/mobile projection parity (E7) |
| `SubjectControllerIntegrationTest` | 4 | Subject REST, alias-aware queries |
| `AuthFlagIntegrationTest` | 4 | Auth CD: scope_violation, temporal_authority |
| `AdminFlagIntegrationTest` | 4 | Admin UI flag operations |
| `MultiActorScopeIntegrationTest` | 4 | Multi-actor scope containment (S5) |
| `ScopeFilteredSyncIntegrationTest` | 6 | IDR-015 scope-filtered pull |
| `ConfigIntegrationTest` | 16 | Phase 3a: shape/activity CRUD, config endpoint, payload validation |

## Test Classes — Mobile (33 tests)

| File | Tests | Module |
|------|-------|--------|
| `event_test.dart` | — | Event model serialization |
| `form_engine_test.dart` | 5+ | WidgetMapper: text, number, select, boolean, date fields |
| `projection_engine_test.dart` | — | Subject list/detail projection |
| `projection_equivalence_test.dart` | 6 | Server/mobile projection parity (E7) |
| `selective_retain_test.dart` | — | Scope-based event purging |
| `config_store_test.dart` | 7 | Config parse, shape lookup, activity→shapes, persistence round-trip |

---

## Test Infrastructure

- Base class: `AbstractIntegrationTest` — `@SpringBootTest(RANDOM_PORT)`, `@ActiveProfiles("test")`
- Constants: `TEST_ACTOR_ID`, `TEST_TOKEN` (64-char hex)
- `provisionTestToken()` — idempotent: inserts token + root-scope assignment event
- `authHeaders()` — returns Bearer token HttpHeaders
- Pattern: `@BeforeEach` cleans tables (reverse dependency order), calls `provisionTestToken()`

---

## Architecture Reference

Canonical docs in `design/docs/`:

| What | Where |
|------|-------|
| Architecture primitives (11), patterns (4), contracts (21), cross-cutting (8), boundaries (29) | `design/docs/architecture/` |
| ADRs (5, all DECIDED) — offline data model, identity conflict, authorization sync, config boundary, state progression | `design/docs/adrs/` |
| IDRs (19) — implementation decisions | `design/docs/decisions/` |
| Implementation plan + execution plan | `design/docs/implementation/plan.md`, `execution-plan.md` |
| Phase specs | `design/docs/implementation/phases/` |
| UX model | `design/docs/implementation/ux-model.md` |
| 22 operational scenarios | `design/docs/scenarios/` |

---

## Forbidden Patterns

| # | Forbidden | Why |
|---|-----------|-----|
| F1 | Add or modify envelope fields | Envelope finalized at 11 fields. ADR-level decision. |
| F2 | Create new event types | Type vocabulary is platform-fixed, closed, append-only. |
| F3 | Write to events table outside Event Store module | Write-path discipline. |
| F4 | Modify or delete persisted events | Append-only is the foundational invariant. |
| F5 | Skip contract tests | Contract tests are the backbone of system correctness. |
| F6 | Reject events for state staleness | Accept-and-flag — events are never rejected. |
| F11 | Schema changes without migration scripts | Flyway on server, SQLite onUpgrade on device. |

---

## Journal Triggers

Record in `design/docs/implementation/phases/phase-N.md` when:

1. An IG (implementation-grade) decision is made
2. Mid-phase discovery reaches Stage 2+ (spike or escalation)
3. Something is tried and abandoned after >1 hour invested
4. A quality gate passes or fails at a sub-phase or phase boundary
5. An environment or tooling workaround is adopted

If you're an agent and hit one of these triggers, flag it to the user for recording.

---

## Codebase Map Maintenance

The codebase map above is the primary context source for new sessions. **After completing a sub-phase** (tests green, ready to commit), update the codebase map in this file to reflect new, changed, or removed files before committing. This is a 2-minute task — the agent that just built the code is the cheapest writer of the entry.
