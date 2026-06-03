# Datarun Platform - Agent Onboarding

This file exists to reduce wrong assumptions in fresh AI-agent sessions. It is the repo's context router, not a request to read the whole documentation set.

## Start Here

Default implementer packet:

1. This file.
2. The top **Current Routing** section of `docs/status.md`.
3. The relevant section of `docs/implementation/module-interfaces.md` for the touched module.
4. The exact `contracts/` files and code paths named by the task or discovered from the touched surface.

Open additional docs only when routed:

- `docs/flagged-positions.md` - read the summary table first; open a specific FP section only when its `Blocks` field or topic matches the task, or when adding/updating an FP.
- ADRs, CDL/IDRs - open only when `docs/status.md`, the task, a touched contract, or code comments name the decision.
- `docs/architecture/`, phase specs, scenarios, and exploration archives - use for architecture-steward planning, drift investigation, or when a task explicitly depends on that context.
- `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md` 

If these sources disagree with each other or with the code, stop and surface the drift before implementing.

### Slicing Canonical Decisions (the new authority replacement of the now retired `adrs/`)

When routed, to prevent loading the entire 2600+ line `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md` into your context window, use the following resources:

- **JSON Catalog:** Refer to `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.json` for a structured, low-token representation of all decisions, constraints, and guardrails.
- **README Index:** Refer to `docs/architecture/adrs-decisions-canonical-ledger/README.md` for a categorized directory linking direct line numbers to specific decisions.
- **Query Tool:** Run `python3 scripts/query_cdl.py --tag <tag>`, `--id <CDL-ID>`, or `--search "<term>"` to extract exactly the relevant decisions and constraints for your task.

## Steward And Implementer Split

- Architecture-steward sessions may read broadly, reconcile status, and produce bounded implementation prompts.
- Implementer sessions should read only the bounded task packet plus the default implementer packet above.
- A task packet should state goal, files to read, authority/guardrails, forbidden work, expected tests, commit boundary, and stop-and-report conditions.

## Repository Map

- `docs/` - platform principles, ADRs, IDRs, phase plans, status, quality gates, and flagged positions.
- `contracts/` - process-boundary intent: event envelope, sync protocol, flag catalog, platform shape schemas, and shared fixtures.
- `server/` - Spring Boot backend, event store, sync, configuration, identity, authorization, integrity detection, admin surfaces.
- `mobile/` - Flutter client, local event store, projection/advisory behavior, sync client, UI.

## Contracts Guidance

Treat `contracts/` as the first place to check when changing data that crosses server/mobile/process boundaries. Do not assume it is a generated-code source or that every schema is runtime-validated everywhere.

Current contract roles:

- `contracts/envelope.schema.json` defines the 11-field event envelope and closed 6-value envelope `type` vocabulary. The server validates against a bundled copy in `server/src/main/resources/envelope.schema.json`; `EnvelopeSchemaParityTest` requires the two copies to match.
- `contracts/sync-protocol.md` describes push/pull protocol intent and invariants. Verify against current server/mobile code when changing sync behavior.
- `contracts/flag-catalog.md` defines flag categories, default severity, resolvability, detection ordering, and state-exclusion semantics.
- `contracts/shapes/*.schema.json` defines platform-bundled assignment, identity, and integrity payload shapes. Server runtime bundles these contract schemas for platform payload validation. They are not deployer-authored shape registry rows, not deployer-editable, not packaged as deployer `shapes`, and not activity-bindable as form shapes.
- `contracts/shape-format.schema.json` defines the deployer-authored form shape DSL stored in the `shapes` table. It is not a platform payload schema.
- `contracts/config-package.schema.json` defines the server-emitted/mobile-consumed config package wire shape, including the current top-level package keys and tolerated unknown top-level keys for forward compatibility.
- `contracts/pattern-definition.schema.json` and `contracts/patterns/*.json` define platform-owned workflow pattern definitions. Server runtime loads these as the Pattern Registry source of truth, config packages deliver referenced definitions under `pattern_definitions`, and mobile reads the packaged definitions.
- `contracts/fixtures/*.json` are shared equivalence fixtures used by server and mobile tests.

Update contracts and tests together when touching:

- envelope fields, envelope `type`, `shape_ref` conventions, or identity reference shape;
- sync request/response fields, pagination, watermarks, auth-visible sync behavior, or scope filtering;
- deployer shape DSL fields, field types, uniqueness semantics, or config package wire keys;
- platform shape payloads;
- flag categories, resolver semantics, severity/resolvability, or detection ordering;
- platform pattern refs, state/transition semantics, projection semantics, or pattern definition delivery;
- server/mobile projection or expression equivalence behavior.

If a contract appears stale, do not silently code around it. Either update it in the same slice or report the drift clearly.

## Architectural Guardrails

- No new envelope fields or envelope `type` values without ADR-level authority.
- Structurally valid state/policy anomalies are accepted and flagged; do not reject them unless the relevant ADR/IDR explicitly says structural validation applies.
- Authority and state are projections from events unless a documented B-to-C escape hatch has been explicitly activated.
- Respect the active phase boundaries in `docs/status.md`. Do not implement work listed as blocked or deferred.
- `docs/flagged-positions.md` is append-only. Use its table as the default lookup surface; add a new FP only for a real deferred architectural position with a specific trigger and gate, and only after confirming no existing FP covers it.

## Build And Test

From the repository root:

```bash
# Start test database
docker compose -f docker-compose.test.yml up -d test-db

# Run all server tests
(cd server && ./mvnw test)

# Run one server test class
(cd server && ./mvnw test -Dtest=ConfigIntegrationTest)

# Run all mobile tests
(cd mobile && flutter test)
```

Use targeted tests for the touched surface first, then broaden when shared detector, sync, config, projection, or contract paths change.

## Working Practice

- Prefer existing patterns and helpers over new abstractions.
- Keep changes scoped to the active slice.
- Update docs/status/checklists only when implementation actually begins or lands.
- Leave unrelated dirty worktree changes alone.
- Use conventional commits when committing, with concise scope, for example `feat(integrity): ...`, `fix(sync): ...`, `docs(status): ...`.
