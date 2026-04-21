# Phase 3e: Envelope Type Vocabulary Retrofit

> Retrofit code, tests, fixtures, and prose to match [ADR-002 Addendum](../../adrs/adr-002-addendum-type-vocabulary.md). Four string literals (`conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`) were persisted as envelope `type` values during Phases 1–2, contradicting ADR-4 S3's closed 6-type vocabulary. They are **shape names**, not envelope types. Phase 3e is the narrowest migration that brings the code into conformance.

**Exercises**: ADR-4 S3 (envelope type closure), ADR-2 S6/S8 (identity events), IDR-017 (shape storage), IDR-019 (config package bundled shapes), [ADR-002 Addendum](../../adrs/adr-002-addendum-type-vocabulary.md) (authorship-based type mapping for `conflict_resolved/v1`).

**Primitives touched**: Event Envelope (vocabulary tightening, no field change), Shape Registry (four platform-bundled internal shapes added), Conflict Detector (emission rewrite), Identity Resolver (emission rewrite), Projection Engine (filter predicate rewrite, mobile), Config Packager (bundled-shape registration), Sync pipeline (scoped-pull system-event predicate).

**Not introducing new primitives. Not introducing new wire fields. Not changing ADR decisions.** Phase 3e is execution of the addendum, not a decision surface.

---

## 1. Why Phase 3e Exists

The Phase 3d close-out audit (2026-04-21) walked every code site that referenced the four drift strings. Summary of findings:

- 2 envelope JSON schemas list the 4 strings in the `type` enum. This directly contradicts [ADR-4 S3](../../adrs/adr-004-configuration-boundary.md).
- 4 server Java files emit or filter on those strings as envelope types.
- 3 mobile Dart files do the same — the mobile `ProjectionEngine` in particular has 8 references, making it the largest single change surface.
- 2 shared fixture copies (`contracts/fixtures/projection-equivalence.json` + server mirror) encode the drift.
- 7 doc files carry prose that asserts the drift as if it were correct.

The drift was caught in a safe stage (no production data exists, test count stable at 153 server + 67 mobile). Phase 3e lands before Phase 4 begins so the drift never compounds with role-action enforcement, pattern state machines, or trigger events.

The architectural reconciliation is already recorded in the [ADR-002 Addendum](../../adrs/adr-002-addendum-type-vocabulary.md) — Phase 3e implements it.

---

## 2. What's In Scope

Five narrow deliverables, grouped into three commits. Each deliverable closes against a specific addendum rule.

| # | Item | Closes | Kind |
|---|------|--------|------|
| 3e.1 | Envelope schema tighten — remove `conflict_detected` / `conflict_resolved` / `subjects_merged` / `subject_split` from the `type` enum on both `server/src/main/resources/envelope.schema.json` and `contracts/envelope.schema.json`. The enum becomes exactly 6 values. | ADR-4 S3 | Wire-format tightening |
| 3e.2 | Register four platform-bundled internal shapes in `contracts/shapes/` and via `Shape Registry` at server boot: `conflict_detected.schema.json`, `conflict_resolved.schema.json`, `subjects_merged.schema.json`, `subject_split.schema.json`. Payload schemas match what Phase 1/2 already emit — this is formalization, not new payload design. | Addendum §"Shape Registry Obligation" | Registry completion |
| 3e.3 | Server emission + filter rewrite — `ConflictDetector`, `ConflictResolutionService`, `IdentityService`, `SyncController` emit the correct `type` per the addendum mapping table, and all `isSystemEventType(String type)` helpers rewrite to `isSystemEvent(Event e)` predicates keyed on `shape_ref` prefix. | Addendum §"Consumer Filtering Rule" | Code migration |
| 3e.4 | Mobile emission + filter rewrite — `EventStore` SQL NOT IN predicate rewrites to `shape_ref NOT LIKE '...'`, `ProjectionEngine` `_isSystemEvent` rewrites to `shape_ref` prefix, `SyncService` alias update keys on `shape_ref.startsWith('subjects_merged/')`. | Addendum §"Consumer Filtering Rule" | Code migration |
| 3e.5 | Prose corrections — IDR-009, IDR-015 (SQL snippet), phase-1/2/3/4 specs, ADR-001 pointer paragraph, `contracts/flag-catalog.md` all reference the addendum and use the correct vocabulary. CLAUDE.md codebase map reflects the updated shape registry. | Addendum §"Retrofit Scope (Pointer)" | Docs |

---

## 3. What's Explicitly Out of Scope

- **New detection logic.** Phase 3e changes how existing flags are emitted and filtered; it does not add new flag categories or detectors.
- **Severity tiers.** Still Phase 4 / IDR-022.
- **`domain_uniqueness_violation`.** Still IDR-022.
- **Pattern state machines.** Still IDR-020 rewrite.
- **Role-action enforcement.** Still IDR-021.
- **Authorship-based discrimination in the Admin UI.** Once Phase 3e lands, the admin can distinguish manual vs. auto resolution by actor prefix; UI surface for this is Phase 4 scope.
- **New Flyway migration for index strategy on `shape_ref`.** Deferred unless IDR-015's rewritten SQL demonstrates regression (see §5 Risks).

---

## 4. Design Decisions

Phase 3e's architecture is already decided in the addendum. Only two thin Lean decisions remain.

### DD-1 (Lean): Where are the four internal shape schemas stored and loaded?

**Context**: The addendum requires the four internal shapes to be platform-bundled, matching the precedent set by `assignment_created/v1` and `assignment_ended/v1` (IDR-013). Those live under `contracts/shapes/` and are loaded by the server at boot.

**Resolved**: Four new schema files under `contracts/shapes/`:

- `conflict_detected.schema.json` — payload: `source_event_id` (UUID), `flag_category` (string, from the 9-category enum), `detector` (string). Optional: `context` (object) for per-category details.
- `conflict_resolved.schema.json` — payload: `flag_event_id` (UUID), `resolution` (enum: `accepted` / `rejected` / `reclassified`), `reclassified_subject_id` (UUID, nullable — required iff `resolution == "reclassified"`), `reason` (string, optional).
- `subjects_merged.schema.json` — payload: `retired_subject_id` (UUID), `surviving_subject_id` (UUID), `reason` (string, optional).
- `subject_split.schema.json` — payload: `source_subject_id` (UUID), `reason` (string).

**Auto-resolution future-compatibility (R4)**: `conflict_resolved/v1`'s `resolution` enum stays at the three current values. Phase 4 auto-resolution will emit `resolution = "accepted"` with `actor_ref.id = "system:auto_resolution/{policy_id}"`, so the shape does **not** need a v2 to accommodate auto-resolution — discrimination lives in `actor_ref`, not in payload. The schema description must state this explicitly so a Phase 4 agent does not speculatively bump the version.

Server bootstraps register these via `ShapeService` on `ApplicationReadyEvent`, idempotent (skip if already present at the same version). They are read-only — admin UI does not surface them for CRUD (the existing `ShapeService.createShape` path applies; a `status` column value of `active` with a `platform_bundled` flag on the row is **not** being added — we discriminate by name convention only, matching how assignments work today).

**Reversibility**: Lean. Schema files are additive; server bootstrap is idempotent. Wire format is unchanged from what Phase 1/2 already emits.

### DD-2 (Lean): Split `_isSystemEvent` on mobile into named predicates (R3)

**Context**: Today, mobile `ProjectionEngine._isSystemEvent` is one helper used by at least three call-sites that want **different** subsets of "not a user event":

| Consumer | Wants to exclude |
|---|---|
| Subject-list projection | integrity flags + resolutions + merge/split + `assignment_changed` |
| Timeline build | resolutions + `assignment_changed` (flags stay visible in timeline) |
| Flagged-ID set build | only `conflict_detected/*` (by shape_ref) |

Collapsing these under one name is exactly the confusion that produced the type-vocabulary drift at a different layer. Phase 3e splits the helper while the code is already being touched.

**Resolved**: Introduce four named predicates in `ProjectionEngine`:

- `isIntegrityFlag(Event e)` — `shape_ref.startsWith('conflict_detected/')`
- `isIntegrityResolution(Event e)` — `shape_ref.startsWith('conflict_resolved/')`
- `isIdentityLifecycle(Event e)` — `shape_ref.startsWith('subjects_merged/') || shape_ref.startsWith('subject_split/')`
- `isAssignmentEvent(Event e)` — `type == 'assignment_changed'` (envelope-type keyed, correctly)

Each call-site picks the combination it needs. The old `_isSystemEvent` helper is removed. Same discipline is applied server-side in `SyncController` and `ConflictDetector` (their `isSystemEventType` helpers become named predicates on the event).

**Reversibility**: Lean. Internal refactor. No wire-format impact.

### DD-3 (Lean): Deterministic flag ID derivation change

**Context**: Phase 1 derives the `conflict_detected` flag UUID from `(source_event_id + flag_category)`. The addendum clarifies this should be `(source_event_id + shape_ref + flag_category)` so future integrity shapes can't collide.

**Resolved**: Update the derivation in `ConflictDetector` to include `shape_ref` as the second hash input. Existing Phase 1/2 flags all carry `shape_ref = conflict_detected/v1` — re-deriving against those produces a **different** UUID than before (because the input changed).

**Implication**: The existing dev/test DB is destroyed at the start of 3e work. This is acceptable — no production data exists, and the `docker compose -f docker-compose.test.yml down -v && up -d` cycle is already routine. The retrofit commit's CI run will be the first clean build of the new derivation.

**Test fixture constraint (R2)**: Tests must not assert on specific flag UUID values — the derivation input changed. Assertions MUST use existence + `(type, shape_ref, flag_category)` tuple checks, not hardcoded UUIDs. Any fixture that embeds a pre-3e flag UUID is rewritten or switched to "match any UUID" semantics.

**Reversibility**: Lean. Internal to `ConflictDetector`. Deterministic — re-derivable. No wire-format change; the UUIDs are opaque to consumers.

**Why not use `UUIDv5` with a platform namespace?** Considered and rejected for this phase — the existing derivation uses `UUID.nameUUIDFromBytes` with a stable salt, which is functionally equivalent for our purposes. Switching to formal v5 is an IG decision for a later phase if we ever expose flag IDs in a public API.

---

## 5. Deliverables

### Commit 1: `feat(server): envelope type vocabulary migration`

**Files modified**:

- `server/src/main/resources/envelope.schema.json` — remove the 4 strings from the `type` enum; final enum has 6 values.
- `contracts/envelope.schema.json` — same change; the two files stay in sync (pre-existing duplication smell, tracked separately as IG cleanup).
- `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java`:
  - Line 346: `type = "alert"`, `shape_ref = "conflict_detected/v1"` on emission.
  - Lines 487–491: `isSystemEventType(String type)` → `isSystemEvent(Event e)`; predicate becomes `shape_ref.startsWith()` over the four prefixes.
  - Deterministic flag ID derivation updated per DD-3.
- `server/src/main/java/dev/datarun/server/integrity/ConflictResolutionService.java`:
  - Line 72 + 166: guard on `flagEvent.shapeRef().startsWith("conflict_detected/")` (not `type`).
  - Line 237: manual resolution emission — `type = "review"`, `shape_ref = "conflict_resolved/v1"`.
  - Line 272: manual-identity-conflict emission — `type = "alert"`, `shape_ref = "conflict_detected/v1"`, payload `flag_category = "identity_conflict"`.
  - (Auto-resolution is Phase 4 territory; no 3e emission site for `type = capture, shape = conflict_resolved/v1`. The addendum documents the mapping so Phase 4 lands it correctly.)
- `server/src/main/java/dev/datarun/server/identity/IdentityService.java`:
  - Line 201: merge emission — `type = "capture"`, `shape_ref = "subjects_merged/v1"`.
  - Line 232: split emission — `type = "capture"`, `shape_ref = "subject_split/v1"`.
- `server/src/main/java/dev/datarun/server/sync/SyncController.java`:
  - Lines 210, 257–259: `isSystemEventType(String type)` → `isSystemEvent(Event e)`; rewrite with shape prefix.
- `server/src/main/java/dev/datarun/server/config/` — bootstrap registration of four bundled shapes on `ApplicationReadyEvent`. One new method on `ShapeService` (or a new `PlatformShapeBootstrap` component); idempotent.
- Four new files under `contracts/shapes/`.
- `server/src/test/resources/fixtures/projection-equivalence.json` — rewrite the three test events to use new `type` + `shape_ref` pair.
- `server/src/test/java/.../ConflictDetectorIntegrationTest.java` — lines 156/166/252: assertions update.
- `server/src/test/java/.../ConflictResolutionIntegrationTest.java` — lines 120/146/154: assertions update.
- Flyway: no new migration. The `type` column already accepts any string; the enum lived only in the JSON schema validator. The DB is recreated per DD-3 regardless.

**Contract test additions**:

- **Envelope schema test**: assert that an event with `type = "conflict_detected"` is **rejected** by the envelope validator post-3e (it's no longer in the enum).
- **Shape registry test**: assert that `ShapeService.findAll()` returns the 4 platform-bundled shapes after boot without any admin action.
- **Emission mapping test** (one per mapping row in the addendum table): detector flag → `type=alert, shape_ref=conflict_detected/v1`; manual resolution → `type=review, shape_ref=conflict_resolved/v1`; merge → `type=capture, shape_ref=subjects_merged/v1`; split → `type=capture, shape_ref=subject_split/v1`.

### Commit 2: `feat(mobile): envelope type vocabulary migration`

**Files modified**:

- `mobile/lib/data/event_store.dart`:
  - Lines 306–307: SQL `type NOT IN (...)` → `shape_ref NOT LIKE 'conflict_detected/%' AND shape_ref NOT LIKE 'conflict_resolved/%' AND shape_ref NOT LIKE 'subjects_merged/%' AND shape_ref NOT LIKE 'subject_split/%' AND type != 'assignment_changed'`.
  - Note: `assignment_changed` stays filtered on `type` because it genuinely IS an envelope type, not a shape prefix. The predicate is now hybrid: shape-ref for integrity events, type for assignment events. Acceptable and correct — they answer different questions.
- **SQLite schema check (R1)**: Before the SQL rewrite lands, verify that `mobile/lib/data/event_store.dart`'s `CREATE TABLE events` statement does not carry a CHECK constraint on the `type` column (it currently does not, per pre-3e audit, but the check is part of DD-2 execution). If a CHECK exists, it is removed in the same migration.
- `mobile/lib/data/projection_engine.dart` — 8 sites:
  - Lines 17–46: the flagged-ID build and resolution filter rewrite to `shape_ref.startsWith('conflict_detected/')` and `shape_ref.startsWith('conflict_resolved/')`.
  - Lines 122–130: subject-detail filters same rewrite.
  - Lines 149–152: `_isSystemEvent` becomes a `shape_ref` prefix check over the four integrity prefixes; does **not** include `assignment_changed` (assignments are envelope-type-discriminated; they're already excluded earlier in the pipeline by a separate filter).
- `mobile/lib/data/sync_service.dart`:
  - Lines 100–101: `if (event.type == 'subjects_merged')` → `if (event.shapeRef.startsWith('subjects_merged/'))`.
- `mobile/test/projection_engine_test.dart` — lines 54/75/96: rewrite test event construction to use new `type` + `shape_ref` pair.
- `mobile/test/selective_retain_test.dart` — line 127: same.
- `contracts/fixtures/projection-equivalence.json` — three events rewritten; this is the cross-platform fixture read by both mobile and server parity tests.

**Contract test additions**:

- **Parity test** (already exists): the updated fixture keeps server + mobile in sync. If either side's parser rejects the new shape, the test fails — catching any residual drift.

### Commit 3: `docs: envelope type vocabulary migration`

**Files modified**:

- `docs/decisions/idr-009-alias-table.md` — line 23 prose: replace *"insert `subjects_merged` event"* with *"insert event with `type=capture`, `shape_ref=subjects_merged/v1`"*.
- `docs/decisions/idr-015-scope-filtered-sync-query.md` — line 65 SQL snippet: rewrite to `shape_ref LIKE 'conflict_detected/%' OR ...`. Add a note on the index consideration (see §5 Risks).
- `docs/implementation/phases/phase-1.md` — lines 137, 161, 214, 231, 234, 248, 253, 286: prose corrections. Line 161 (deterministic flag ID) updated per DD-3.
- `docs/implementation/phases/phase-2.md` — line 520 prose correction.
- `docs/implementation/phases/phase-3.md` — line 39 table: remove "Total platform vocabulary = 10 types" and replace with "Envelope vocabulary = 6 types. Four identity/integrity shapes are platform-bundled (see ADR-002 Addendum)."
- `docs/implementation/phases/phase-4.md` — line 275 prose correction; confirms auto-resolution emits `type=capture, shape_ref=conflict_resolved/v1`.
- `docs/adrs/adr-001-offline-data-model.md` — line 92 prose: keep the conceptual reference (*"a `subjects_merged` event"* is OK as shorthand) but add an ADR-002 Addendum footnote pointer.
- `contracts/flag-catalog.md` — line 3 prose: *"Each flag is persisted as an event with `type=alert` and `shape_ref=conflict_detected/v1`; `flag_category` lives in the payload."* Line 26 similar.
- `CLAUDE.md` — Codebase Map additions:
  - `contracts/shapes/` entries for the 4 new internal shape schemas.
  - `config/PlatformShapeBootstrap.java` (or wherever registration lands) entry.
  - Server test class test counts updated.
  - Mobile test class counts updated.
  - Repository structure line updated.
  - Add a one-line pointer to `docs/flagged-positions.md`.

#### Additional docs-commit scope folded in (audit findings)

Five cleanups caught during the phase-3e review pass (audit of Phase 0/1/2/3 drift against architecture). Same risk profile as 3e.5 — correct prose to match the architecture — so they land in the same commit rather than a follow-up.

- **A1 — `flag-catalog.md` resolution prose (binding fix)**: The current file claims `reclassify = change the flag category`. Code and ADR-2 S11 disagree — `reclassified` means **the captured event has been reclassified to a different subject**, requires a `reclassified_subject_id`, and is only valid for `identity_conflict` flags. Replace the §Resolution prose with:

  > **accepted** — the flagged event is accepted into state derivation (flag cleared).
  > **rejected** — the flagged event is permanently excluded from state derivation (flag cleared, event stays in the store).
  > **reclassified** — the flagged event has been reclassified to a different subject; requires a target `reclassified_subject_id`. Only valid for `identity_conflict` flags.

- **A2 — `flag-catalog.md` truncated to 6 categories**: `docs/architecture/boundary.md` SG-2 and `cross-cutting.md` §7 define **9 categories**. The catalog file lists 6. Extend to 9 with the missing rows explicitly marked:

  | 7 | `domain_uniqueness_violation` | Shape-declared uniqueness CD | `manual_only` | TBD | **Deferred — Phase 4 / IDR-022** |
  | 8 | `transition_violation` | Pattern state-machine CD | `auto_eligible` | TBD | **Deferred — Phase 4 / IDR-020** |
  | 9 | reserved | reserved | reserved | reserved | **Reserved — growth slot; do not claim without ADR amendment** |

  This prevents a future agent from treating the catalog as closed-at-6 and inventing a workaround when IDR-022 or IDR-020 lands.

- **B1 — `subject_ref.type` enum `process` is aspirational**: Both envelope schema files list `process` in the enum, but no code emits or consumes it. Add description text to `subject_ref` in both schemas:

  > Note: `process` is reserved for future workflow-instance refs (see ADR-2 S2). No current emission site. Pattern instances in Phase 4 use `(subject_ref, activity_ref)` or `source_event_id` per `patterns.md`, NOT `process` refs — do not claim this identity category without a new IDR.

- **B2 — `actor_ref` system-actor convention under-documented**: Add description text to `actor_ref` in both schemas:

  > System actors use `id` prefixed with `system:{component}/{identifier}` (e.g., `system:auto_resolution/late_entry_accept`). The `type` enum stays `['actor']` — authorship discrimination is by `id` prefix, never by adding a new type value. See ADR-002 Addendum F-A3.

- **B4 — Two envelope schemas can diverge silently**: `contracts/envelope.schema.json` and `server/src/main/resources/envelope.schema.json` are maintained as separate files. Add a server-side contract test (`EnvelopeSchemaParityTest`) that reads both files and asserts byte-for-byte equality (modulo trailing newline). The test fails the build if they diverge. IG — no IDR needed. Cheap insurance against a repeat of the type-vocabulary drift.

---

## 6. Quality Gates

All must pass. Phase 3e is not complete until every item is green.

- [ ] **3e.1**: `envelope.schema.json` (both copies) contains exactly the 6 ADR-4 S3 values in `type.enum`. A contract test asserts an event with any of the 4 drift strings as `type` is **rejected** by the validator.
- [ ] **3e.2**: Server boot loads four platform-bundled shape definitions; `ShapeService.findAll()` returns them without admin action; Shape Registry contract test passes.
- [ ] **3e.3**: All server emission sites produce events whose `(type, shape_ref)` pair matches the addendum mapping table. Contract test per mapping row.
- [ ] **3e.4**: All mobile filter sites discriminate on `shape_ref` (for integrity events) or `type` (for assignment events) appropriately. `ProjectionEngine` parity test against the updated shared fixture passes.
- [ ] **3e.5**: Prose audit — `grep -rn "conflict_detected\|conflict_resolved\|subjects_merged\|subject_split" docs/ contracts/` returns only (a) the addendum itself, (b) `flag-catalog.md` in the corrected form, (c) shape-schema file paths, (d) historical journal entries that are explicitly marked as pre-3e. No active specs or IDRs assert the drift mapping.
- [ ] **CD idempotency preserved**: Sweep-then-sweep produces no duplicate flags under the new derivation (DD-3). Existing `ConflictDetectorIntegrationTest` asserts this — update fixtures, not semantics.
- [ ] **Projection equivalence preserved**: Updated shared fixture + existing parity test prove server and mobile projections produce identical subject lists and event timelines.
- [ ] **No regression**: all Phase 0/1/2/3a/3b/3c/3d tests still pass. Test count expectation: 153 server + 67 mobile *minimum* (plus new contract tests from §5; realistic landing point ≈ 160 server + 69 mobile).
- [ ] **Dev/test DB recreated** at the start of Phase 3e work; CI passes with the fresh schema (no pre-3e events lingering).

---

## 7. Reversibility Triage

| Decision | Bucket | Why |
|----------|--------|-----|
| DD-1 envelope enum tighten | **Execution of ADR-4 S3** | Already a Lock decision at the ADR level. Phase 3e does not re-decide; it aligns code. |
| DD-2 named predicates (mobile + server) | **Lean** | Internal refactor. Same behavior, more readable key. |
| DD-3 flag ID derivation | **Lean** | Internal to detector. No external consumers; deterministic re-derivable. |
| 3e.1–3e.5 code + docs migration | **Lean** | Execution of the addendum. |

No Locks introduced by Phase 3e. By design — this is retrofit, not new architecture.

---

## 8. Risks & Mitigations

| Risk | Likelihood | Mitigation |
|------|:---:|------|
| IDR-015's scope-filtered sync query gets slower because the `type IN (...)` path used a covering index and `shape_ref LIKE` doesn't. | Medium | Run `EXPLAIN ANALYZE` on the rewritten query against a seeded test DB (≥10k events) as part of 3e.5 verification. If regression >20%, add Flyway V6 with an index on `shape_ref` prefix or a partial index for system events. Tracked as an IG decision during 3e execution, not pre-committed. |
| A non-obvious code site keys on the drift strings and is missed by the grep. | Low | The initial grep (CLAUDE.md 2026-04-21 audit) found 12 files; Phase 3e verifies by running the same grep at the end of Commit 3 and asserting zero remaining matches outside documented exceptions. |
| `ProjectionEngine` mobile rewrite introduces a subtle projection divergence from server. | Medium | The shared parity fixture test is the safety net. Update the fixture once, run both sides, any divergence fails both tests symmetrically. |
| Fresh dev DB required per DD-3 surprises a developer mid-PR. | Low | Add a one-line note to the PR/commit message and to CLAUDE.md's "Build & Test" section: *"Phase 3e requires `docker compose down -v` before first test run."* |
| Auto-resolution emission sites come in Phase 4 and accidentally use `type=review`. | Low | Addendum F-A5 is explicit; the mapping table will be directly cited in Phase 4 spec when auto-resolution lands. No preemptive 3e code needed — Phase 3e leaves no auto-resolution emission site in the codebase. |

---

## 9. Open Questions Before Implementation

None. The addendum locks every judgment call (authorship → type mapping, shape naming, consumer filter rule). Phase 3e is execution.

If an implementing agent believes it has found an open question, that agent must:

1. Re-read the [ADR-002 Addendum](../../adrs/adr-002-addendum-type-vocabulary.md) end-to-end.
2. Re-read §F-A1 through §F-A5 in that addendum (forbidden patterns).
3. If the question genuinely remains, escalate — do not choose. This is exactly the failure mode that produced the Phase 1/2 drift.

---

## 10. Flagged Positions Carried Past 3e

The 3e review pass surfaced two items that are **verification** work, not retrofit — they do not belong in 3e's code commits but must not be forgotten. They are recorded in the living register: [`docs/flagged-positions.md`](../../flagged-positions.md). They block IDR-021 drafting.

| FP# | Item | Trigger | Outcome gate |
|-----|------|---------|-------------|
| FP-001 | `role_stale` projection-derived role verification | Pre-IDR-021 | Confirm role-as-of-event is reconstructed from assignment event timeline, not a cache or envelope field. Add integration test that fails if role is cached. See [flagged-positions.md](../../flagged-positions.md) |
| FP-002 | `subject_lifecycle` table read-discipline audit | Pre-Phase-4 | Confirm every read is defensive (rebuildable) or there is a rebuild procedure from events. Add comment in V3 migration file. See [flagged-positions.md](../../flagged-positions.md) |

**Rule for future agents**: Before drafting IDR-021, read `docs/flagged-positions.md` end-to-end. Items in `OPEN` status must be resolved (or explicitly re-deferred with justification) before the IDR is written. Do **not** silently skip them. That is how Phase 1/2 type-vocabulary drift got in.

---

## 11. Journal

Entries recorded here as 3e progresses. Empty at spec authoring time.

- **2026-04-21**: Phase 3e specced and reviewed. ADR-002 Addendum committed as `d2b4cbb`. Review pass applied corrections R1–R5 and folded audit findings A1/A2/B1/B2/B4 into 3e.5 scope. Two verification-only items (FP-001 `role_stale`, FP-002 `subject_lifecycle`) recorded in `docs/flagged-positions.md` as gating IDR-021 / Phase 4.
- **2026-04-21**: Commit 1 landed as `e35263e` — server envelope type migration. Envelope enum reduced 10→6 in both schema files. Four platform-bundled shape schemas added under `contracts/shapes/`. `PlatformShapeBootstrap` registers them on `ApplicationReadyEvent` (idempotent). All server emission sites (`ConflictDetector`, `ConflictResolutionService`, `IdentityService`) now emit the architecturally correct `(type, shape_ref)` tuples. F-A3 system-actor convention enforced (`system:{component}/{identifier}`). DD-3 deterministic flag ID derivation updated to include `shape_ref`. All filter/discrimination sites (`SyncController`, `EventRepository`, `SubjectProjection`) rewritten to key on `shape_ref`. Test assertions conform to architecture (not legacy behavior): `IdentityResolverIntegrationTest` counts domain captures by `shape_ref = 'basic_capture/v1'`, not by envelope type; `ConfigIntegrationTest` `@BeforeEach` preserves the four platform-bundled shape names (they are platform contract, not deployer-managed). New contract tests: `EnvelopeVocabularyTest` (parameterized rejection/acceptance), `PlatformShapeBootstrapTest`, system-actor convention assertion in `ConflictDetectorIntegrationTest`. Server test count 153 → 163 (all green).
- **2026-04-21**: Commit 2 landed as `6a774be` — mobile envelope type migration. `ProjectionEngine` `_isSystemEventType` string switch replaced with four top-level named predicates per DD-2 (`isIntegrityFlag`, `isIntegrityResolution`, `isIdentityLifecycle`, `isAssignmentEvent`). `event_store.dart` `purgeOutOfScopeEvents` SQL migrated from `type NOT IN (...)` to four `shape_ref NOT LIKE` clauses plus a single `type != 'assignment_changed'` check — hybrid predicate is correct (type is the pipeline question, `shape_ref` is the payload question). `sync_service.dart` alias-merge processing keys on `shapeRef.startsWith('subjects_merged/')`. Test fixtures (`projection_engine_test.dart`, `selective_retain_test.dart`) rewritten with architecturally correct (type, shape_ref, F-A3 actor_ref) tuples. New DD-2 contract test `event_classifiers_test.dart` (5 tests): positive/negative coverage per predicate, version independence (vN), mutual exclusivity on canonical events. Mobile test count 67 → 72 (all green).
- **2026-04-21**: Commit 3 landed — docs + folded audit fixes. A1: `contracts/flag-catalog.md` resolution prose corrected (`reclassified` is subject-reclassification, only valid for `identity_conflict` flags). A2: catalog extended from 6 to 9 categories matching SG-2 / cross-cutting.md §7 (categories 7/8 deferred to Phase 4; category 9 reserved as growth slot). B1: `process` marked reserved in `subject_ref.description` in both envelope schemas. B2: `system:{component}/{identifier}` convention documented in `actor_ref.description`. B4: `EnvelopeSchemaParityTest` lands in `server/src/test/java/.../contracts/`, asserting byte-parity between `contracts/envelope.schema.json` and the server-bundled copy — closes FP-003. Prose corrections landed in `phase-1.md` (line 161 DD-3 update; line 317 addendum pointer), `phase-3.md` (line 39: 10 types → 6 envelope types + 4 shapes), `phase-4.md` (line 275: auto-resolution discrimination by actor prefix), `idr-009.md` (merge step), `idr-015.md` (SQL rewrite). CLAUDE.md codebase map updated: `PlatformShapeBootstrap` under `config/`, four new shape schemas under `contracts/shapes/`, server test table extended to 16 classes / 164 rows, mobile test table extended to 10 files / 72 tests. `docs/status.md` marks Phase 3e COMPLETE. `docs/flagged-positions.md` FP-003 → RESOLVED. Final test counts: **164 server / 72 mobile, all green**. Phase 3e close-out complete.
