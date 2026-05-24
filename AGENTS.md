# Datarun Platform - Agent Onboarding

This file exists to reduce wrong assumptions in fresh AI-agent sessions. It is an onboarding map, not a replacement for the ADRs, IDRs, phase specs, contracts, or code.

## Start Here

Before writing code or drafting design, read:

1. `docs/status.md` - current phase, landed work, open blockers, active ADRs/IDRs, and recommended next slice.
2. `docs/flagged-positions.md` - deferred gates and blockers. Check existing FPs before adding a new one.
3. The active phase spec and decision files named by `docs/status.md` or by the user prompt.
4. Relevant `contracts/` files when touching any cross-boundary behavior.

If these sources disagree with each other or with the code, stop and surface the drift before implementing.

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
- `contracts/shapes/*.schema.json` documents platform-bundled assignment, identity, and integrity payload shapes. Server runtime uses mirrored platform shape definitions; keep references and mirrors coherent.
- `contracts/pattern-definition.schema.json` and `contracts/patterns/*.json` define platform-owned workflow pattern definitions. Server runtime loads these as the Pattern Registry source of truth, config packages deliver referenced definitions under `pattern_definitions`, and mobile reads the packaged definitions.
- `contracts/fixtures/*.json` are shared equivalence fixtures used by server and mobile tests.

Update contracts and tests together when touching:

- envelope fields, envelope `type`, `shape_ref` conventions, or identity reference shape;
- sync request/response fields, pagination, watermarks, auth-visible sync behavior, or scope filtering;
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
- `docs/flagged-positions.md` is append-only. Add a new FP only for a real deferred architectural position with a specific trigger and gate, and only after confirming no existing FP covers it.

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
