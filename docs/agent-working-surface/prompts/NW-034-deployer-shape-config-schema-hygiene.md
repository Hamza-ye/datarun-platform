# NW-034 Agent Prompt: Decide Deployer Shape/Config Schema Hygiene

You are working in `/home/hamza/datarun-platform`.

## Goal

Decide and, if the current behavior is consistent enough, add machine-readable contract hygiene for deployer shape format and config package delivery.

Exit target:

```text
The repository either has bounded root contract schemas for the deployer shape format and config package wire shape, with focused parity/validation tests against current server/mobile behavior, or a precise stop report explains why adding them would change semantics or expose unresolved drift.
```

This is a contract-hygiene slice. It must not change runtime semantics, promote a new configuration phase, or make mobile runtime JSON Schema validation mandatory.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-031, NW-032, and NW-034.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-010, BAR-011, BAR-012, and BAR-014.
6. `docs/implementation/module-interfaces.md`
   - Read `Shape Registry`, `Config Packager`, `Pattern Registry`, and `Platform Payload Contracts`.
7. `docs/implementation/phases/post-phase-4-contract-architecture-analysis.md`
   - Read the contract taxonomy, missing contract artifacts, config package, and recommended testing sections.
8. `docs/decisions/idr-017-shape-storage.md`
9. `docs/decisions/idr-019-config-package.md`
10. `docs/decisions/idr-025-pattern-definition-contract-and-delivery.md`
11. `contracts/pattern-definition.schema.json`
12. `contracts/patterns/*.json`
13. Existing `contracts/shapes/*.schema.json` only as platform-payload contrast; do not treat them as deployer shape DSL schemas.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/config/ShapeService.java
server/src/main/java/dev/datarun/server/config/ConfigPackager.java
server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java
server/src/test/java/dev/datarun/server/config/ConfigIntegrationTest.java
server/src/test/java/dev/datarun/server/config/DeployTimeValidatorTest.java
mobile/lib/data/config_store.dart
mobile/lib/domain/shape.dart
mobile/test/config_store_test.dart
```

Adjust exact paths based on current package layout.

## Decision Boundary

First decide whether schemas can describe current accepted behavior without changing it.

Expected good outcome:

- Add `contracts/shape-format.schema.json` for deployer-authored shape DSL bodies, not platform payload schemas.
- Add `contracts/config-package.schema.json` for the server-emitted/mobile-consumed config package wire shape.
- Add focused tests proving representative valid current shape/config examples pass and representative invalid examples fail where the contract is intended to be strict.
- Update only minimal contract guidance docs if needed.

Expected stop outcome:

- If current code/IDR behavior has unresolved ambiguity that a schema would have to decide, stop and report exact mismatches instead of inventing semantics.
- If schema validation would require changing server/mobile runtime behavior, stop and report the required follow-up.

## Schema Guardrails

The schemas must reflect accepted behavior:

- `shape-format.schema.json` describes deployer form shape DSL, including field names/types, required/deprecated/display metadata, select options, subject binding, and uniqueness where current code supports them.
- `config-package.schema.json` describes current package keys: `version`, `published_at`, `shapes`, `activities`, `expressions`, `flag_severity_overrides`, `sensitivity_classifications`, and `pattern_definitions`.
- Config package activity entries must include the current `status` field added by NW-032.
- Platform payload schemas under `contracts/shapes/*.schema.json` remain separate runtime contracts and must not become deployer form shapes.
- Pattern definitions remain governed by `contracts/pattern-definition.schema.json`; config-package schema may reference or structurally admit the packaged `pattern_definitions` section without duplicating the whole pattern-definition contract.
- Unknown top-level config package keys are tolerated by mobile. If the schema is for server-emitted packages, decide whether top-level additional properties are allowed for forward compatibility and document the choice in the test or schema description.

## Expected Tests

Prefer focused contract tests using existing JSON Schema tooling if present.

Server-side examples may include:

```bash
cd server
./mvnw -Dtest=ConfigIntegrationTest,DeployTimeValidatorTest test
```

If you add dedicated contract tests, run them plus the nearest config tests, for example:

```bash
cd server
./mvnw -Dtest=ConfigPackageSchemaContractTest,ShapeFormatSchemaContractTest,ConfigIntegrationTest,DeployTimeValidatorTest test
```

Run mobile config tests only if mobile fixtures/parsing expectations are touched:

```bash
cd mobile
flutter test test/config_store_test.dart
```

Report exact commands. Do not run full Maven/Flutter unless the change broadens beyond contract hygiene.

## BAR And Backlog Updates

If schema hygiene lands:

- Mark NW-034 `accepted`.
- Attach exact date, commands, and concise evidence summary to the NW-034 backlog row.
- Do not change BAR-010 status; BAR-010 is already accepted. This slice adds contract hygiene, not baseline acceptance.

If schema hygiene cannot land safely:

- Leave NW-034 `in_review` or mark it `blocked` with exact mismatch evidence.
- Do not add partial schemas that look authoritative but do not match runtime behavior.

## Forbidden Work

- Do not change envelope fields or event `type` values.
- Do not change config package runtime semantics, deployer shape semantics, pattern definitions, or platform payload schemas.
- Do not package platform payload schemas as deployer `shapes`.
- Do not require mobile runtime JSON Schema validation.
- Do not introduce generated code, new build pipelines, or broad validators unless the repo already has the tooling and the change remains small.
- Do not implement trigger execution, reporting, production auth, new scope mechanisms, entity lifecycle, field-level sensitivity/encryption/redaction, or resolver changes.
- Do not make stale implementation-plan references authoritative; reconcile against current IDRs, BAR evidence, and code.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(contracts): add shape config schema hygiene
```

The commit may include root contract schemas, focused tests, and minimal docs/backlog updates. It must not include runtime behavior changes except tiny test-only fixture plumbing, and it must not include NW-033 reporting work.

## Stop And Report

Stop and report if:

- schema authoring would require changing accepted server/mobile behavior;
- current IDR/code behavior disagrees on required fields, supported field types, activity package structure, pattern delivery, or unknown-key tolerance;
- adding schemas requires a new validator dependency or build path that is larger than this hygiene slice;
- the shape-format schema would be confused with platform payload JSON Schemas;
- config-package schema cannot describe current forward-compatible package behavior without making an architectural decision.
