# Datarun Platform

## What This Is

The implementation repository for the Datarun operations platform — a domain-agnostic operational backbone. Event-sourced, offline-first, append-only architecture.

**Design docs**: `docs/` (previously a submodule, now tracked in this repo)

**Before drafting an IDR or starting a new phase, read**: [`docs/flagged-positions.md`](docs/flagged-positions.md) — living register of deferred verification items. Rule R-4: items whose `Blocks:` field names your upcoming work must be resolved (or explicitly re-deferred with justification) before proceeding. Silent deferral is forbidden (Rule R-1).

---

## Current Phase

**Phase 3c (Config Packager + Full Pipeline) — COMPLETE with carried debt.**
Phase 4.0 (role-action enforcement) was drafted, rolled back (IDR-020 violated architecture),
and the corresponding Phase 3a quality gate remains OPEN. A Phase 3d close-out is proposed
before any Phase 4 work; see §"Audit — End of Phase 3c (2026-04-21)" below.

- Phase 0 (Core Loop): COMPLETE
- Phase 1 (Identity & Integrity): COMPLETE
- Phase 2 (Authorization & Multi-Actor): COMPLETE
- Phase 3a (Shapes + Config Delivery): COMPLETE — one QG carried as debt (role→action)
- Phase 3b (Expressions + DtV): COMPLETE
- Phase 3c (Config Packager + Full Pipeline): COMPLETE
- **Phase 3d (close-out)**: COMPLETE — 3d.1 activity_ref plumbing, 3d.2 sensitivity surface on device, 3d.3 ContextResolver. 67 mobile tests, 153 server tests.
- **Phase 4**: NOT STARTED — blocked on IDR-020 rewrite + IDR-021 (role-action) + IDR-022 (severity/uniqueness)

**Test counts (actual, 2026-04-21)**: 14 server test classes, 103 `@Test` methods
(parameterized expansions ≈ 153 rows, which is what `status.md` historically reported).
Mobile: ~54 tests across ~7 files. The "157" in older CLAUDE.md revisions was the
rolled-back Phase 4.0 number — ignore it.

See [docs/status.md](docs/status.md) for living status.

Phase specs: `docs/implementation/phases/phase-{0,1,2,3}.md`
IDRs: IDR-001..IDR-019 in `docs/decisions/`. IDR-020, IDR-021, IDR-022 pending (see audit below).

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

# Run all server tests (153 tests, ~30s)
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

## Agent Workflow

See `docs/agent-workflow/` for session tasks and persistent lessons.

### Workflow Orchestration

**1. Plan Node Default**
- Enter plan mode for any non-trivial task (three or more steps, or involving architectural decisions).
- If something goes wrong, stop and re-plan immediately rather than continuing blindly.
- Use plan mode for verification steps, not just implementation.
- Write detailed specifications upfront to reduce ambiguity.

**2. Subagent Strategy**
- Use subagents liberally to keep the main context window clean.
- Offload research, exploration, and parallel analysis to subagents.
- For complex problems, allocate more compute via subagents.
- Assign one task per subagent to ensure focused execution.

**3. Self-Improvement Loop**
- After any correction from the user, update `docs/agent-workflow/lessons.md` with the relevant pattern.
- Create rules for yourself that prevent repeating the same mistake.
- Iterate on these lessons rigorously until the mistake rate declines.
- Review lessons at the start of each session when relevant to the project.

**4. Verification Before Done**
- Never mark a task complete without proving it works.
- Diff behavior between main and your changes when relevant.
- Ask: "Would a staff engineer approve this?"
- Run tests, check logs, and demonstrate correctness.

**5. Demand Elegance (Balanced)**
- For non-trivial changes, pause and ask whether there is a more elegant solution.
- If a fix feels hacky, implement the solution you would choose knowing everything you now know.
- Do not over-engineer simple or obvious fixes.
- Critically evaluate your own work before presenting it.

**6. Autonomous Bug Fixing**
- When given a bug report, fix it without asking for unnecessary guidance.
- Review logs, errors, and failing tests, then resolve them.
- Avoid requiring context switching from the user.
- Fix failing CI tests proactively.

### Task Management

1. **Plan First**: Use `manage_todo_list` tool for in-session tracking; write cross-session plans to `docs/agent-workflow/todo.md`.
2. **Verify Plan**: Review before starting implementation.
3. **Track Progress**: Mark items complete as you go.
4. **Explain Changes**: Provide a high-level summary at each step.
5. **Document Results**: Add a review section after completion.
6. **Capture Lessons**: Update `docs/agent-workflow/lessons.md` after corrections.

### Core Principles

- **Simplicity First**: Make every change as simple as possible. Minimize code impact.
- **No Laziness**: Identify root causes. Avoid temporary fixes. Apply senior developer standards.
- **Minimal Impact**: Touch only what is necessary. Avoid introducing new bugs.

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
  fixtures/expression-evaluation.json  # 50 shared expression evaluator test cases (E7)
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

server/src/test/java/              # 14 test classes, 103 @Test methods (~153 parameterized rows)
  AbstractIntegrationTest.java     # Base: test actor provisioning, auth helpers

mobile/lib/
  domain/                          # Event, Shape (IDR-017), ExpressionEvaluator, SubjectSummary models
  data/                            # EventStore, SyncService, ConfigStore (+ expressions), ProjectionEngine
  presentation/                    # AppState, screens (WorkList, SubjectDetail, Form), widgets

design/                            # formerly a git submodule — now inlined here
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
| `ConflictDetector.java` | Per-event identity CD + auth CD; creates flag events. **Phase 3c scope only** — no role-action check (rolled back), no transition_violation, no domain_uniqueness_violation | `evaluate(events, lastPullWatermark)→List<Event>` (identity: concurrent_state_change + stale_reference), `evaluateAuth(events, actorId)→List<Event>` (auth: temporal_authority_expired → scope_violation → role_stale, ordered), `sweep(trailingWatermark)→List<Event>` |
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
| `WebConfig.java` | Registers token interceptor | Interceptor on `/api/sync/pull`, `/api/sync/config` |

### config/ — Phase 3: Configuration
| File | Purpose | Key API |
|------|---------|---------|
| `Shape.java` | Shape record (IDR-017) | `record(name, version, status, sensitivity, schemaJson, createdAt)`, `shapeRef()→"{name}/v{version}"` |
| `ShapeRepository.java` | Shape persistence | `insert(Shape)`, `findByNameAndVersion(name,ver)→Optional`, `findAll()`, `findActive()`, `updateStatus(name,ver,status)` |
| `ShapeService.java` | DtV L1 validation: field budget ≤60, type vocabulary, name format | `createShape(name, sensitivity, schemaJson)→List<String>`, `createVersion(name, sensitivity, schemaJson)→List<String>`, `deprecate(name,ver)`. Constants: `VALID_TYPES` (10 types), `VALID_TYPES_LIST`. Static: `parseShapeRef(ref)→String[]` |
| `Activity.java` | Activity record | `record(name, configJson, status, sensitivity, createdAt)` |
| `ActivityRepository.java` | Activity persistence | `insert(Activity)`, `update(Activity)`, `findByName(name)→Optional`, `findAll()`, `findActive()` |
| `ActivityService.java` | Activity validation: name format, shapes exist | `createActivity(name, sensitivity, configJson)→List<String>`, `updateActivity(...)`, `deprecate(name)` |
| `ConfigPackager.java` | Assembles IDR-019 JSON package (all shapes, active activities, expressions grouped by activity.shape, empty flags) | `publish(publishedBy)→int version`, `getLatest()→Optional<ConfigPackage>`, `getLatestVersion()→int` |
| `ConfigApiController.java` | Config delivery endpoint with ETag (quoted + raw) | `GET /api/sync/config` (If-None-Match → 304, Bearer token auth) |
| `ConfigAdminController.java` | Thymeleaf admin: shape CRUD, activity CRUD, expression CRUD, publish with DtV gating | Routes under `/admin/config/` |
| `ShapePayloadValidator.java` | Validates event payload against shape fields; unknown shapes pass through | `validate(shapeRef, payload)→List<String>` |
| `ExpressionEvaluator.java` | Pure-function JSON AST evaluator (IDR-018). 8 comparison + 3 logical + ref | `evaluateCondition(JsonNode, Map)→boolean`, `evaluateValue(JsonNode, Map)→Object` |
| `ExpressionRule.java` | Expression rule record | `record(id, activityRef, shapeRef, fieldName, ruleType, expression, message, createdAt)` |
| `ExpressionRepository.java` | Expression rule persistence (JSONB expression column) | `insert(ExpressionRule)`, `findAll()`, `findByActivityAndShape(activity, shape)`, `delete(UUID)` |
| `DeployTimeValidator.java` | DtV L2: field refs exist, operator-type compat, predicate budget ≤3, no nesting, default type match | `validate(ExpressionRule, Shape)→List<String>` |

### admin/ — Admin UI
| File | Purpose |
|------|---------|
| `AdminController.java` | Thymeleaf: subject list, detail, flags, assignments. Routes under `/admin/` |
| `DevBootstrapController.java` | Dev-only helpers: seed test actor token + root-scope assignment |

---

## Codebase Map — Mobile

### domain/ — Models
| File | Purpose |
|------|---------|
| `event.dart` | 11-field Event class; `toMap()` (SQLite), `toEnvelope()` (sync), `fromMap()`, `fromServerJson()` |
| `shape.dart` | IDR-017 format. ShapeField(name,type,required,description,displayOrder,group,deprecated,options:List\<String\>), ShapeDefinition(shapeRef,name,version,status,sensitivity,fields). `activeFields` getter filters deprecated + sorts by displayOrder. Factory: `fromConfigJson(shapeRef, json)` |
| `subject_summary.dart` | SubjectSummary(subjectId, subjectType, latestTimestamp, name, captureCount, flagCount) |
| `expression_evaluator.dart` | Pure-function JSON AST evaluator (IDR-018 Dart port). Identical results to Java via shared fixtures. | `evaluateCondition(Map, Map)→bool`, `evaluateValue(Map, Map)→dynamic` |

### data/ — Data Layer
| File | Purpose | Key API |
|------|---------|---------|
| `event_store.dart` | SQLite: events, aliases, assignments, config (current + pending). DB version=5 | `insert(Event)`, `insertFromServer(Event)`, `getUnpushed()`, `markPushed(ids)`, `getBySubject(id)`, `getAll()`, `getAllAliases()`, `getActiveAssignments()`, `purgeOutOfScopeEvents(deviceId)`, `getConfigPackage()`, `saveConfigPackage(version, json)`, `getPendingConfigPackage()`, `savePendingConfigPackage(version, json)`, `deletePendingConfigPackage()` |
| `event_assembler.dart` | Assembles 11-field envelope from form state | `assemble({subjectId, shapeRef, payload, activityRef?})→Future<Event>`. activityRef auto-populated from dispatching screen (Phase 3d.1). |
| `sync_service.dart` | Push→pull pipeline with actor token auth + config download after pull | `sync()→Future<SyncResult>`. Downloads config via GET /api/sync/config with If-None-Match when server reports newer config_version. Sends config_version in pull request for server-side tracking. |
| `config_store.dart` | SQLite-persisted config cache (IDR-019). Two-slot model: current + pending. Parses shapes/activities/expressions/sensitivity_classifications into memory on init. | `init()`, `applyConfig(json)`, `promotePending()`, `hasPending`, `getShape(ref)→ShapeDefinition?`, `getActivity(name)`, `getActiveActivities()→List<String>`, `getShapesForActivity(name)→List<ShapeDefinition>`, `getExpressionsForField(activity,shape,field)`, `getShowCondition(...)`, `getDefaultExpression(...)`, `getWarningExpression(...)`, `getShapeSensitivity(ref)→String`, `getActivitySensitivity(name)→String`, `configVersion→int` |
| `device_identity.dart` | Persists device_id, actor_id, device_seq, actor_token | `init()→Future<DeviceIdentity>`, `nextSeq()→Future<int>`, `get/set actorToken` |
| `context_resolver.dart` | Phase 3d.3: resolves `context.*` properties at form-open (ADR-5 S8 / IDR-018 rule 4). Returns actor.role, actor.scope_name, event_count, days_since_last_event from local state; subject_state/subject_pattern/activity_stage as null (Phase 4 placeholders). | `resolve({subjectId, activityRef, now})→Future<Map<String,dynamic>>` |
| `projection_engine.dart` | Subject list + detail from events; alias-aware, flag-excluding | `getSubjectList()→Future<List<SubjectSummary>>`, `getSubjectDetail(id)→Future<List<Event>>`, `getFlaggedEventIds()→Future<Set<String>>` |

### presentation/ — UI
| File | Purpose |
|------|---------|
| `app_state.dart` | ChangeNotifier: subjects, assignments, sync status, pending count. Holds ConfigStore + ContextResolver. `refresh()`, `sync()` |
| `screens/work_list_screen.dart` | Subject list, sync indicator, FAB. Config-driven shape selection (single→direct, multiple→dialog, none→message). Threads `activityRef` into FormScreen (Phase 3d.1). |
| `screens/subject_detail_screen.dart` | Event timeline with flags. Config-driven shape selection for capture action; threads `activityRef` into FormScreen (Phase 3d.1). |
| `screens/form_screen.dart` | Shape-driven form with expression evaluation. Show/hide (show_condition), computed defaults, conditional warnings. Pre-resolves `context.*` via ContextResolver at form-open; merges into expression values map. Optional activityRef param (Phase 3d.1/3d.3). |
| `widgets/sync_panel.dart` | Modal: sync trigger, last sync time, device ID |
| `widgets/widget_mapper.dart` | Maps field type → Flutter widget. Types: text, integer, decimal, number, date, select, multi_select (chips), boolean, narrative (multiline), location, subject_ref. Optional warningMessage param for amber warning display. |

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

## Test Classes — Server (14 classes, 104 @Test methods; ~153 rows after parameterization)

| Class | @Test | Module |
|-------|:-----:|--------|
| `ConfigIntegrationTest` | 24 | Phase 3a/3b/3c: shape/activity/expression CRUD, config endpoint, payload validation, DtV publish gating, auth on config, config version tracking, full pipeline E2E |
| `DeployTimeValidatorTest` | 15 | DtV L2: field refs, operator-type compat, predicate budget, nesting, default type match |
| `ConflictResolutionIntegrationTest` | 9 | Resolve flags, manual identity conflicts |
| `SyncControllerIntegrationTest` | 8 | Push/pull pipeline, pagination |
| `IdentityResolverIntegrationTest` | 8 | Merge/split, alias chains |
| `ConflictDetectorIntegrationTest` | 8 | W_effective, stale_reference detection |
| `ScopeFilteredSyncIntegrationTest` | 7 | IDR-015 scope-filtered pull |
| `MultiActorScopeIntegrationTest` | 7 | Multi-actor scope containment (S5) |
| `AuthFlagIntegrationTest` | 6 | Auth CD: scope_violation, temporal_authority_expired, role_stale (NO role_action_mismatch — rolled back) |
| `AdminFlagIntegrationTest` | 6 | Admin UI flag operations |
| `SubjectControllerIntegrationTest` | 3 | Subject REST, alias-aware queries |
| `ProjectionEquivalenceTest` | 1 | Server/mobile projection parity (E7) |
| `MultiDeviceE2ETest` | 1 | Multi-device sync E2E |
| `ExpressionEvaluatorTest` | 1 | Parameterized @MethodSource over 50 shared fixtures (E7) — all IDR-018 operators, null handling, type coercion, logical ops |

**Parameterized note**: `ExpressionEvaluatorTest` is a single `@Test`/`@ParameterizedTest` that expands to 50 runs via [fixtures/expression-evaluation.json](contracts/fixtures/expression-evaluation.json). That is why `./mvnw test` reports ~153 total runs vs. 104 `@Test` methods. The "157" number in older CLAUDE.md revisions came from Phase 4.0 before rollback — ignore.

## Test Classes — Mobile (9 files, 67 tests)

| File | Tests | Module |
|------|:-----:|--------|
| `config_store_test.dart` | 23 | Config parse, shape lookup, activity→shapes, persistence round-trip, expression storage/retrieval, two-slot model (pending/current/promotion/restart), sensitivity classifications (Phase 3d.2) |
| `projection_engine_test.dart` | 11 | Subject list/detail projection |
| `expression_evaluator_test.dart` | 9 | Shared fixture-driven (E7, 50 cases) + individual operator unit tests |
| `form_engine_test.dart` | 8 | WidgetMapper: text, number, select, boolean, date fields |
| `context_resolver_test.dart` | 6 | Phase 3d.3: context.* pre-resolution — actor.role, actor.scope_name, event_count, days_since_last_event, Phase 4 null placeholders |
| `selective_retain_test.dart` | 4 | Scope-based event purging |
| `event_assembler_test.dart` | 3 | Phase 3d.1: activity_ref auto-population in assembled events |
| `event_test.dart` | 2 | Event model serialization |
| `projection_equivalence_test.dart` | 1 | Server/mobile projection parity (E7) |

---

## Test Infrastructure

- Base class: `AbstractIntegrationTest` — `@SpringBootTest(RANDOM_PORT)`, `@ActiveProfiles("test")`
- Constants: `TEST_ACTOR_ID`, `TEST_TOKEN` (64-char hex)
- `provisionTestToken()` — idempotent: inserts token + root-scope assignment event
- `authHeaders()` — returns Bearer token HttpHeaders
- Pattern: `@BeforeEach` cleans tables (reverse dependency order), calls `provisionTestToken()`

---

## Architecture Reference

Canonical docs in `docs/`:

| What | Where |
|------|-------|
| Architecture primitives (11), patterns (4), contracts (21), cross-cutting (8), boundaries (29) | `docs/architecture/` |
| ADRs (5, all DECIDED) — offline data model, identity conflict, authorization sync, config boundary, state progression | `docs/adrs/` |
| IDRs (19) — implementation decisions | `docs/decisions/` |
| Implementation plan + execution plan | `docs/implementation/plan.md`, `execution-plan.md` |
| Phase specs | `docs/implementation/phases/` |
| UX model | `docs/implementation/ux-model.md` |
| 22 operational scenarios | `docs/scenarios/` |

---

## Forbidden Patterns

| # | Forbidden | Why |
| --- | --- | --- |
| F1 | Add or modify envelope fields | Envelope finalized at 11 fields. ADR-level decision. |
| F2 | Create new envelope `type` values | Envelope type vocabulary is locked at **6 values** by ADR-4 S3: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`. If you think you need a 7th, you need a new **shape**, not a new type. See [ADR-002 Addendum](docs/adrs/adr-002-addendum-type-vocabulary.md). |
| F2a | Filter code on `type == "conflict_detected"` / `conflict_resolved` / `subjects_merged` / `subject_split` | These are **shape names**, not envelope types (per ADR-002 Addendum, 2026-04-21). Filter on `shape_ref` prefix instead. Any code or test that keys integrity-event discrimination off `type` is wrong. |
| F2b | Encode authorship in envelope `type` | `type` answers "what pipeline"; `actor_ref` answers "who authored". `conflict_resolved/v1` spans `type=review` (human) and `type=capture` (`system:auto_resolution/*`) — discriminate on actor, never add a new type. |
| F3 | Write to events table outside Event Store module | Write-path discipline. |
| F4 | Modify or delete persisted events | Append-only is the foundational invariant. |
| F5 | Skip contract tests | Contract tests are the backbone of system correctness. |
| F6 | Reject events for state staleness | Accept-and-flag — events are never rejected. |
| F11 | Schema changes without migration scripts | Flyway on server, SQLite onUpgrade on device. |
| F12 | Change production semantics to fix a test | Tests adapt to production logic, never the reverse. If a test fails after a correct production change, fix the test fixture. |
| F13 | Skip writing a test for the exact scenario that caused a bug | If a bug was caused by scenario X, there must be a test that fails without the fix and passes with it. |
| F14 | Treat test scaffolding as a design input | Production code must be designed from domain rules, not from what makes tests pass. |
| F15 | Silent deferral | If you observe a position that is "correct today but could drift under future work," you MUST add an entry to [`docs/flagged-positions.md`](docs/flagged-positions.md) before closing the phase. Trusting memory is the failure mode that produced the Phase 1/2 envelope-type drift. See Rule R-1 in the register. |

---

## Journal Triggers

Record in `docs/implementation/phases/phase-N.md` when:

1. An IG (implementation-grade) decision is made
2. Mid-phase discovery reaches Stage 2+ (spike or escalation)
3. Something is tried and abandoned after >1 hour invested
4. A quality gate passes or fails at a sub-phase or phase boundary
5. An environment or tooling workaround is adopted

If you're an agent and hit one of these triggers, flag it to the user for recording.

---

## Codebase Map Maintenance

The codebase map above is the primary context source for new sessions. **After completing a sub-phase** (tests green, ready to commit), update the codebase map in this file to reflect new, changed, or removed files before committing. This is a 2-minute task — the agent that just built the code is the cheapest writer of the entry.
