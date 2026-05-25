# Post Phase 4 Contract Architecture Analysis

Date: 2026-05-25

Scope: contract ownership, loading, validation, packaging, testing, and versioning for
everything under `contracts/`, with specific attention to platform payload shape
schemas and the pre-FP-010 `PlatformShapeBootstrap` approach.

This review drove the post-Phase-4 FP-010 implementation. The implementation
outcome is: platform payload JSON Schemas are loaded directly as server runtime
contracts; platform payload schemas are not deployer shape registry rows;
legacy mirror rows are filtered out of config packages; and
`ongoing_resolution/v1` assignment transfer is owned by platform pattern
definition `platform_shape_roles`.

## 1. Contract Taxonomy

The durable split is:

- Platform-owned contracts are product/runtime law. They should be loaded or
  tested from contract artifacts, not invented by deployer data.
- Deployer-owned configuration is deployment data. It belongs in the config
  database, is admin-editable where the UI allows it, and is packaged to mobile.
- CI-owned fixtures are shared behavioral examples. They are not runtime config.

| Artifact | Owner | Source of truth | Runtime consumers | Store in DB? | Admin-editable? | Package to mobile? | Mobile runtime validation |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `contracts/envelope.schema.json` | Platform | Root contract file | Server `EnvelopeValidator`; mobile `Event` and `EventAssembler` by model/test parity | No | No | No | Prefer model/fixture tests for 11 fields and closed type vocabulary. Do not require JSON Schema validation on every mobile event unless mobile becomes an authoritative event validator. |
| `contracts/sync-protocol.md` | Platform | Root protocol document, backed by integration tests | Server `SyncController`, `SubjectHistoryBackfillService`, `EventRepository`; mobile `sync_service.dart` | No | No | No | Runtime code follows protocol. Mobile should be covered by sync tests and fixtures, not by validating a prose document. |
| `contracts/flag-catalog.md` | Platform | Currently prose in root contract file; should become machine-readable or parity-tested against code mirrors | Server detectors/resolvers and `FlagCatalog`; mobile `flag_severity.dart`; config package only carries overrides | No for the catalog. Yes only for deployment-specific severity overrides. | Catalog no. Overrides yes. | Package overrides, not the platform catalog, unless a later version intentionally packages platform metadata. | Use code defaults plus packaged overrides. Add parity tests or generated data if the catalog remains mirrored in Java/Dart. |
| `contracts/shapes/*.schema.json` platform payload schemas | Platform | Root JSON Schema files | Server platform event emission, sync push validation, event persistence guard; mobile projections/classifiers consume resulting events | No in the deployer `shapes` table. If DB introspection is ever required, use separate immutable system-contract metadata or explicit ownership columns. | No | No as form shapes. Pattern definitions may reference platform shape refs as platform semantics, but the schemas should not be packaged as deployer form shapes. | No full runtime JSON Schema by default. Mobile should fixture-test canonical payloads and keep defensive typed extraction/classification. |
| Deployer shape/config schemas | Platform owns the schema language; deployers own shape instances | Currently IDR-017 plus `ShapeService` and DB rows. There is no root `shape-format.schema.json` or `config-package.schema.json` yet. | Server `ShapeService`, `ShapePayloadValidator`, `ActivityService`, `ConfigPackager`; mobile `ConfigStore`, form rendering, advisory uniqueness | Yes for deployer shape versions | Yes through admin APIs/UI | Yes, all relevant deployer shape versions and config package data | Mobile should parse and validate enough for forms/advisory behavior. Full JSON Schema validation is optional only after a machine-readable config package schema exists. |
| `contracts/pattern-definition.schema.json` and `contracts/patterns/*.json` | Platform | Root schema and pattern definition files | Server `PatternRegistry`, deploy-time validation, `ConfigPackager`; mobile `ConfigStore`, `PatternProjectionEngine` | No | No | Yes, only referenced pattern definitions under config package `pattern_definitions` | Mobile should consume packaged definitions defensively and prove behavior through shared fixtures. Runtime JSON Schema validation is optional. |
| `contracts/fixtures/*.json` | CI/platform shared test data | Root fixture files | Server and mobile tests for expression, subject projection, and pattern projection equivalence | No | No | No, except fixture-local package examples inside tests | Test-only. Mobile should load root fixtures in unit tests and compare behavior with server expectations. |

Important missing contract artifacts:

- There is no machine-readable deployer shape DSL schema under `contracts/`.
  The shape language is enforced by `ShapeService` and described in IDR-017.
- There is no machine-readable config package schema. `ConfigPackager` and
  `ConfigStore` define the effective shape.
- There is no machine-readable sync request/response schema. The protocol is
  prose plus server/mobile tests.
- The flag catalog is prose with code mirrors. That is acceptable short-term
  only if parity tests make drift visible.

## 2. Professional Strategies

### Direct Runtime Loading From Bundled Contract Artifacts

The runtime bundles contract files as resources and validates or interprets them
directly.

Pros:

- One source of truth. Drift is minimized because there is no hand-maintained
  mirror.
- Startup/resource-load failures expose packaging mistakes early.
- Fits platform-owned artifacts that are small, versioned, and not
  deployer-authored: envelope schema, platform payload schemas, and platform
  pattern definitions.
- CI can verify the exact resources the runtime will load.

Cons:

- Each runtime needs compatible parsers and validators.
- Runtime startup failure semantics must be intentional.
- Full JSON Schema validation may be too expensive or unnecessary on mobile.
- Schemas are still only structural. Cross-event semantic rules need code.

Appropriate for Datarun:

- Server runtime loading of `contracts/envelope.schema.json`,
  `contracts/shapes/*.schema.json`, `contracts/pattern-definition.schema.json`,
  and `contracts/patterns/*.json`.
- Mobile test-time use of shared fixtures and optional generated helpers, not
  mandatory runtime JSON Schema validation.

### Generated Code or Models From Contracts

Schemas produce Java/Dart records, validators, enums, or fixture models.

Pros:

- Strong compile-time surfaces for stable contracts.
- Reduces hand-written enum and field-name drift.
- Useful for envelope vocabulary, flag catalog data, platform payload DTOs, and
  pattern definition models once the schema stabilizes.

Cons:

- Generator setup becomes a new build dependency and failure mode.
- JSON Schema features do not always map cleanly to Java/Dart types.
- Generated churn can obscure small contract changes.
- Dynamic deployer shapes still need runtime interpretation.

Appropriate for Datarun:

- A later hygiene slice could generate envelope/type constants and flag catalog
  data.
- It is not required to fix FP-010. Direct server loading is simpler and enough
  for platform payload validation.

### DB-Seeded System Contracts With Immutable Ownership Metadata

Platform contracts are seeded into a database registry, but with explicit system
ownership and immutability.

Pros:

- Common in products that need one registry for query, admin display,
  deployment diffing, or package assembly.
- Allows a database transaction to treat system and deployer config together.
- Can support version activation windows if product requirements demand it.

Cons:

- The database becomes a mirror unless every row records source artifact,
  owner, mutability, package policy, and bindability.
- Admin UIs and deployer validators easily leak system rows into deployer
  workflows.
- Migrations must handle source-controlled contract upgrades carefully.
- Without ownership metadata, it is easy to make platform contracts look like
  editable deployer shapes.

Appropriate for Datarun:

- Not appropriate for the current `shapes` table as-is.
- If a DB system registry is ever needed, it must have explicit metadata such
  as `owner = platform`, `source_kind = json_schema`, `editable = false`,
  `activity_bindable = false`, and `package_kind = platform_metadata`.
- The current platform payload schemas do not need this. They can be loaded
  directly from bundled resources.

### CI-Only Contract Tests With Runtime Mirrors

The runtime has hand-written mirrors, while CI tests compare those mirrors to
contract files or fixtures.

Pros:

- Low runtime cost.
- Useful when a contract is prose or when an old code path cannot yet load the
  source artifact.
- Good transitional safety net while removing mirrors.

Cons:

- Weaker than runtime loading. Drift is caught only when tests run.
- Coverage is often partial because mirrors rarely represent the full schema.
- Does not protect direct server emission paths unless those paths are tested.

Appropriate for Datarun:

- Envelope parity only while a copied server resource remains.
- Fixture mirror parity only while server test resources duplicate root
  fixtures.
- Flag catalog parity until the catalog becomes machine-readable or generated.
- Not sufficient as the final strategy for platform payload shapes.

### Hybrid Strategy

Use direct loading for platform-owned runtime contracts, DB storage for
deployer-authored config, generated helpers only where they remove real drift,
and CI parity tests for the few mirrors that remain.

This is the recommended professional strategy for Datarun because the platform
has both immutable platform contracts and mutable deployer configuration. Trying
to force both into the same registry hides ownership and creates admin/package
leakage.

## 3. Recommended Datarun Strategy

The clean target rule is:

**The deployer `shapes` table stores deployer form shape DSL versions. It is not
the source of truth for platform event payload JSON Schemas.**

### Contract Loading

Load these directly on the server from bundled contract artifacts:

- `contracts/envelope.schema.json` for envelope validation.
- `contracts/shapes/*.schema.json` for exact platform payload shape refs.
- `contracts/pattern-definition.schema.json` for startup validation of bundled
  platform pattern definitions.
- `contracts/patterns/*.json` as the platform pattern registry source of truth.

The current copied `server/src/main/resources/envelope.schema.json` can remain
temporarily with a parity test, but the cleaner target is Maven/Gradle resource
inclusion from the root contract file, matching how patterns are already
bundled.

### Platform Payload Shapes and the DB

Platform payload shapes should not be stored in the deployer `shapes` table.
They are JSON Schemas for platform event payloads, not deployer form shape DSL
definitions.

`PlatformShapeBootstrap` should be retired in the target architecture. If it
remains temporarily, treat it as a compatibility shim only, not as ownership.
Do not use it to claim FP-010 is resolved.

Platform payload contracts should not be visible in `/admin/config/shapes`.
They should not be create/version/deprecate editable. They should not be listed
as deployer form shapes.

Platform payload contracts should not be generally bindable to deployer
activities. A deployer activity binds deployer form shapes and pattern roles,
not arbitrary platform event payload schemas.

### Pattern Definitions and Platform Shape Refs

There is one important exception to "not bindable": platform pattern semantics
may need to observe platform event refs, especially assignment events. The
current pattern projection code treats `assignment_created/v1` and
`assignment_ended/v1` as transfer events only when the activity pattern binding
maps those refs to the `transfer` shape role. Both server
`PatternStateProjection` and mobile `PatternProjectionEngine` depend on that
binding style.

That relationship should be made platform-owned, not deployer-owned. The target
should be one of:

- Preferred: pattern definitions declare intrinsic platform shape roles, for
  example transfer is driven by `assignment_created/v1` and
  `assignment_ended/v1`. Activity bindings then list only deployer shape roles.
- Transitional: activity pattern bindings may include platform refs only for
  roles explicitly marked by the platform pattern definition as platform-owned,
  and validation resolves those refs through the platform payload contract
  registry, not the deployer `shapes` table.

Do not solve this by seeding assignment payload schemas into the deployer shape
registry.

### Config Packages

Config packages should include:

- Deployer form shapes from the `shapes` table, including the versions required
  by the active package contract.
- Activities, expressions, severity overrides, sensitivity classifications, and
  deployer config data.
- Referenced platform pattern definitions under `pattern_definitions`.

Config packages should not include platform payload contracts as form shapes.
Mobile receives platform event payloads through sync, not through form shape
configuration.

### Server Emission and Ingestion

Server-created platform events should be validated before persistence against
the direct-loaded platform payload contracts. A central platform event factory
would be the cleanest emission API, with an `EventRepository` guard as a final
backstop.

Sync push should treat exact platform payload shape refs specially:

- If `shape_ref` is a known platform payload ref, validate payload against the
  direct-loaded platform JSON Schema before persistence.
- If `shape_ref` is a known deployer shape ref, validate against the deployer
  shape DSL.
- Preserve the current forward-compatibility behavior for unknown non-platform
  refs unless a separate ADR/IDR changes the sync contract.

The platform payload schema should enforce all structural requirements that are
already architectural law. For example, IDR-026 and the architecture contract say
new `conflict_detected/v1` flags have a designated resolver; the JSON Schema
should require the field when FP-010 closes.

### Mobile Runtime and Tests

Mobile should not need full runtime JSON Schema validation for every event in
this slice. It should:

- Keep envelope assembly/parsing model tests aligned with the 11-field schema.
- Use shared fixtures for subject projection, pattern projection, and
  expression equivalence.
- Add focused tests for platform ref classifiers and typed payload extraction.
- Treat packaged pattern definitions and deployer shapes defensively, ignoring
  unknown top-level config keys as already intended.

If mobile later starts authoring platform payload events directly, generated
payload models or a mobile-side validator should be reconsidered.

## 4. Current-State Gap Analysis

### Baseline Architecture Gaps

`contracts/` is the right source-of-truth location, but only part of it is
loaded directly today.

- `server/src/main/java/dev/datarun/server/event/EnvelopeValidator.java` loads a
  classpath envelope schema. The project still maintains a copied
  `server/src/main/resources/envelope.schema.json`, with
  `EnvelopeSchemaParityTest` and CI copying the root file before server verify.
  This is workable but not as clean as direct resource inclusion from
  `contracts/envelope.schema.json`.
- `server/src/main/java/dev/datarun/server/config/PatternRegistry.java` already
  loads bundled `contracts/patterns/*.json` resources. That is the right
  direction, but runtime startup does not validate loaded pattern definitions
  against `contracts/pattern-definition.schema.json`; tests do.
- `server/src/main/java/dev/datarun/server/config/PlatformShapeBootstrap.java`
  currently mirrors four platform payload shapes into the deployer `shapes`
  table using the deployer shape DSL. It does not load the canonical JSON
  Schemas under `contracts/shapes/`. It also omits `assignment_created/v1` and
  `assignment_ended/v1`.
- `server/src/main/java/dev/datarun/server/config/ShapePayloadValidator.java`
  validates through deployer shape rows. Unknown shape refs pass through. In the
  baseline, exact platform payload refs are not validated against canonical
  `contracts/shapes/*.schema.json`.
- `server/src/main/java/dev/datarun/server/config/ShapeRepository.java` and the
  `shapes` table have no ownership, source kind, editability, package policy, or
  bindability metadata. This makes platform rows indistinguishable from deployer
  form shape rows.
- `server/src/main/java/dev/datarun/server/config/ConfigPackager.java` packages
  all rows returned by `shapeRepository.findAll()`. If platform mirrors live in
  the table, they can leak into mobile as deployer form shapes.
- `server/src/main/java/dev/datarun/server/config/ActivityService.java` and
  deploy-time validation resolve shape refs through the deployer shape
  repository. That model cannot cleanly represent platform pattern refs such as
  assignment transfer events.
- `server/src/main/java/dev/datarun/server/config/ConfigAdminController.java`
  is an admin surface over deployer shapes. Platform payload contracts do not
  belong there.
- `contracts/shapes/conflict_detected.schema.json` does not currently require
  `designated_resolver`, even though IDR-026 and the architecture contract make
  the resolver semantic requirement part of platform flag law. This means the
  contract file itself is still under-specified for FP-010.
- The prose `contracts/flag-catalog.md` is mirrored in server and mobile code
  without a machine-readable catalog or parity gate.
- The sync protocol is prose plus tests. There is no request/response schema or
  explicit CI gate that enumerates protocol fields.
- `.github/workflows/server-ci.yml` runs server Maven verify on server/contract
  changes, but it does not run mobile tests for contract changes and does not
  check the full contract artifact inventory.

### Mobile Gaps

Mobile is mostly in the right posture for this phase: it consumes packages and
fixtures instead of trying to be an authoritative contract validator.

Remaining gaps:

- `mobile/lib/data/config_store.dart` parses deployer shapes and packaged
  pattern definitions without a machine-readable config package schema to test
  against.
- `mobile/lib/data/pattern_projection.dart` now receives platform assignment
  transfer refs through packaged pattern definition `platform_shape_roles`;
  activity bindings remain deployer-shape ownership only.
- `mobile/lib/data/projection_engine.dart` and platform classifiers use
  shape-ref conventions. They need fixture and classifier tests, not necessarily
  runtime JSON Schema validation.
- `mobile/assets/shapes/basic_capture_v1.json` is outside `contracts/` and
  appears stale relative to the current config package flow. It should not be
  treated as a contract source.

### Exploratory Diff Review Outcome

The exploratory FP-010 diff improved several real problems:

- `server/pom.xml` starts bundling `contracts/shapes` resources.
- `PlatformPayloadContractValidator` loads the six platform
  payload JSON Schemas from classpath and validates exact shape refs.
- `ShapePayloadValidator` is changed to route exact platform refs through JSON
  Schema validation before falling back to deployer shape validation.
- `EventRepository` is changed to validate platform payloads before insert,
  which catches direct server emission and test insert paths.
- `ShapeService` and `ConfigAdminController` add guards to hide or block
  platform shape mutations.
- New tests exercise schema bundling, representative payload validation, and
  several server emission paths.

Those are useful directions, especially direct loading of platform payload
contracts and emission-path tests.

The exploratory diff still preserved the central architecture problem at review time:

- `PlatformShapeBootstrap` remained and still seeded platform mirrors into the
  deployer `shapes` table.
- `ShapeService` gained hard-coded platform shape names instead of a clean
  platform contract registry boundary.
- The DB rows remained mirror data with no ownership metadata. They were hidden by
  service/controller behavior rather than made impossible by schema design.
- `ConfigPackager` still packaged all shapes from the repository unless it was
  separately filtered, so platform rows can still leak into mobile packages.
- The test additions compared only selected required fields for four DB mirrors,
  not full parity with all six platform JSON Schemas.
- `contracts/shapes/conflict_detected.schema.json` still did not require
  `designated_resolver`, so the semantic contract remains weaker than IDR-026.
- The `ActivityService` change rejected all platform refs in pattern bindings.
  That is too blunt while `ongoing_resolution` projection still relies on
  `assignment_created/v1` and `assignment_ended/v1` being mapped to `transfer`.
- Dirty docs marked FP-010 resolved before the ownership problem was actually
  resolved.

Verdict:

- Keep the direct-loaded platform payload validator idea.
- Keep the emission and ingress validation test direction.
- Replace the DB-mirror/admin-hiding approach with a clear platform payload
  contract boundary.
- FP-010 may be marked resolved only after platform payload contracts are no
  longer represented as editable deployer shape rows, config package leakage is
  tested, and assignment transfer ownership moves into platform pattern
  definitions.

## 5. Testing and Parity Plan

The test suite should prove both artifact integrity and runtime behavior.

| Gate | Belongs in | Purpose |
| --- | --- | --- |
| Contract inventory test | Server unit or small CI script | Enumerate expected files under `contracts/` and fail when new contract artifact classes are added without a consumer/test decision. |
| JSON Schema parse/validity tests | Server unit | Parse `contracts/envelope.schema.json`, `contracts/pattern-definition.schema.json`, and every `contracts/shapes/*.schema.json` with the same validator version the server uses. |
| Pattern definition contract test | Server unit | Validate every `contracts/patterns/*.json` against `contracts/pattern-definition.schema.json`; assert registry loads the same set. |
| Platform payload schema tests | Server unit | Validate representative valid payloads and negative required-field/type cases for all six platform payload refs. Include `designated_resolver` once the schema is tightened. |
| Resource bundling/loading tests | Server unit | Assert server classpath contains envelope, pattern schema, all pattern definitions, and all platform payload schemas. |
| Parity tests for remaining mirrors | Server unit and mobile unit | Keep only where mirrors remain: envelope copy until removed; server fixture mirrors; code flag catalog mirrors until generated/machine-readable. |
| Server emission validation tests | Server integration | Exercise assignment create/end, conflict detection categories, conflict resolution, subject merge, subject split, and transition violation emission. Assert emitted events validate against platform payload schemas. |
| Server ingestion validation tests | Server integration | Push exact platform refs with invalid payloads and expect rejection. Push valid platform payloads and expect acceptance where allowed. Preserve unknown non-platform forward compatibility unless a separate decision changes it. |
| Event persistence backstop tests | Server integration/unit | Direct insert of invalid platform payload should fail if `EventRepository` remains the final guard. If a platform event factory becomes authoritative, test both factory and repository guard. |
| Config package tests | Server integration | Assert packages include deployer form shapes and referenced pattern definitions, but do not include platform payload contracts as form shapes. Assert deprecated deployer shape versions remain included if required by the config contract. |
| Admin boundary tests | Server MVC/integration | Assert platform payload contracts are not listed or mutable in deployer shape admin APIs/UI. |
| Pattern platform-ref tests | Server and mobile unit | Assert assignment transfer semantics work without requiring platform payload schemas to be rows in the deployer shape table. |
| Mobile config tests | Mobile unit | Assert `ConfigStore` parses deployer shapes, pattern definitions, severity overrides, and unknown top-level keys defensively. |
| Mobile fixture tests | Mobile unit | Continue loading root `contracts/fixtures/*.json` for expression, subject projection, and pattern projection equivalence. Add cases for resolver-bearing conflicts and assignment transfer events. |
| Cross-implementation projection equivalence | Server and mobile unit | Keep root fixtures authoritative. Add parity for any server test-resource fixture mirror or remove the mirror. |
| CI contract gates | GitHub Actions | On `contracts/**` changes, run server contract tests and mobile tests. Add a lightweight inventory/resource check before Maven/Flutter tests so missing bundled resources fail clearly. |

Suggested placement:

- Server unit tests under `server/src/test/java/dev/datarun/server/contracts/`
  for artifact parsing, bundling, registry loading, and parity.
- Server integration tests under existing config/sync/integrity packages for
  emission, ingestion, admin, and config package behavior.
- Mobile unit tests under `mobile/test/` using root `contracts/fixtures/*.json`.
- CI wiring in `.github/workflows/server-ci.yml` or a renamed broader contract
  workflow that runs both server and mobile checks for contract changes.

## 6. Actionable Migration Plan

Keep this as contract hygiene only: no new envelope fields, no new envelope
types, no Phase 5 product work, no Keycloak/OIDC work, and no behavior expansion
beyond correcting contract ownership, loading, validation, packaging, and tests.

### Commit 1: Document the Target

Commit message:

`docs(contracts): define post-phase-4 contract ownership strategy`

Touched files:

- `docs/implementation/phases/post-phase-4-contract-architecture-analysis.md`

Tests:

- None required for docs-only.

### Commit 2: Add Contract Inventory and Resource Gates

Commit message:

`test(contracts): inventory bundled contract artifacts`

Touched files:

- `server/src/test/java/dev/datarun/server/contracts/*`
- Possibly `.github/workflows/server-ci.yml` if adding an early inventory step.

Targeted tests:

- `cd server && ./mvnw -Dtest=*Contract* test`

Intent:

- Prove the server can see all contract files it is supposed to load.
- Keep existing envelope parity until the copied resource is removed.
- Add parity for any server fixture mirrors that remain.

### Commit 3: Introduce Direct Platform Payload Contract Loading

Commit message:

`feat(contracts): load platform payload schemas directly`

Touched files:

- `server/pom.xml`
- New server contract registry/validator class under `server/src/main/java/dev/datarun/server/config/` or a clearer `contracts` package.
- New tests under `server/src/test/java/dev/datarun/server/contracts/`

Targeted tests:

- `cd server && ./mvnw -Dtest=PlatformPayloadShapeContractTest test`

Intent:

- Load `contracts/shapes/*.schema.json` as classpath resources.
- Validate representative payloads.
- Do not yet depend on DB mirror rows.

### Commit 4: Tighten Platform Payload Schemas to Architectural Law

Commit message:

`fix(contracts): tighten platform payload shape requirements`

Touched files:

- `contracts/shapes/conflict_detected.schema.json`
- Possibly `contracts/shapes/conflict_resolved.schema.json`
- Emission tests and fixtures that need resolver-bearing payloads.

Targeted tests:

- `cd server && ./mvnw -Dtest=PlatformPayloadShapeContractTest,PlatformPayloadEmissionContractIntegrationTest test`
- `cd mobile && flutter test test/projection_equivalence_test.dart test/pattern_projection_test.dart`

Intent:

- Make structural schemas reflect existing ADR/IDR requirements.
- In particular, require `designated_resolver` for new
  `conflict_detected/v1` payloads when FP-010 closes.

### Commit 5: Wire Server Ingestion and Emission Validation

Commit message:

`fix(events): validate platform payload contracts on ingress and emission`

Touched files:

- `server/src/main/java/dev/datarun/server/config/ShapePayloadValidator.java`
- `server/src/main/java/dev/datarun/server/sync/SyncController.java` if needed
- `server/src/main/java/dev/datarun/server/event/EventRepository.java` or a new
  platform event factory
- Server integration tests for sync push and emission paths

Targeted tests:

- `cd server && ./mvnw -Dtest=PlatformPayloadEmissionContractIntegrationTest,ConfigIntegrationTest test`
- Add a focused sync push invalid-platform-payload test.

Intent:

- Exact platform refs validate against platform JSON Schemas.
- Deployer refs validate against deployer shape DSL.
- Unknown non-platform refs keep current forward-compatibility behavior unless
  a separate decision changes it.

### Commit 6: Remove Platform Payload Mirrors From the Deployer Shape Registry

Commit message:

`refactor(config): separate platform payload contracts from deployer shapes`

Touched files:

- `server/src/main/java/dev/datarun/server/config/PlatformShapeBootstrap.java`
- `server/src/main/java/dev/datarun/server/config/ShapeService.java`
- `server/src/main/java/dev/datarun/server/config/ConfigAdminController.java`
- `server/src/main/java/dev/datarun/server/config/ConfigPackager.java`
- `server/src/test/java/dev/datarun/server/config/*`
- `server/src/test/java/dev/datarun/server/contracts/*`

Targeted tests:

- `cd server && ./mvnw -Dtest=PlatformPayloadBoundaryTest,ConfigIntegrationTest,DeployTimeValidatorTest test`

Intent:

- Stop seeding platform payload contracts into `shapes`.
- Assert config packages contain deployer form shapes only.
- Assert admin shape APIs do not expose platform payload contracts.
- If existing deployed databases matter, add an explicit migration or cleanup
  step. Do not rely on silent service-layer hiding.

### Commit 7: Correct Pattern Handling of Platform Assignment Refs

Commit message:

`fix(patterns): make platform assignment roles pattern-owned`

Touched files:

- `contracts/pattern-definition.schema.json`
- `contracts/patterns/ongoing_resolution.v1.json`
- `server/src/main/java/dev/datarun/server/config/ActivityService.java`
- `server/src/main/java/dev/datarun/server/projection/PatternStateProjection.java`
- `mobile/lib/data/pattern_projection.dart`
- Shared pattern projection fixtures and tests

Targeted tests:

- `cd server && ./mvnw -Dtest=PatternDefinitionContractTest,PatternStateProjectionTest,DeployTimeValidatorTest test`
- `cd mobile && flutter test test/pattern_projection_test.dart`

Intent:

- Remove the need for assignment payload schemas to exist as deployer shape
  rows.
- Keep assignment transfer semantics as platform pattern behavior.
- Allow deployers to bind deployer shapes to pattern roles, not arbitrary
  platform payload schemas.

### Commit 8: Add Mobile and CI Contract Gates

Commit message:

`ci(contracts): run server and mobile contract checks`

Touched files:

- `.github/workflows/server-ci.yml` or a new contract workflow
- Mobile tests under `mobile/test/`
- Optional contract check script

Targeted tests:

- `cd server && ./mvnw test`
- `cd mobile && flutter test`

Intent:

- Contract changes must run both server and mobile contract consumers.
- CI should fail clearly if a contract artifact is added without bundling,
  validation, or fixture coverage.

## Concise Recommendation

Do direct-load platform-owned contracts, keep deployer shape versions in the DB,
validate exact platform payload refs on server ingress and emission, and make CI
prove server/mobile consumers stay aligned.

Do not store platform payload JSON Schemas as editable deployer `shapes` rows,
do not reintroduce `PlatformShapeBootstrap` as the owner of platform contracts,
and keep FP-010 resolved only while the DB-mirror boundary remains removed or
explicitly replaced by immutable system-contract metadata.
