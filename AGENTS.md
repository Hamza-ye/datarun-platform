# Datarun Platform - Agent Onboarding

This file exists to reduce wrong assumptions in fresh AI-agent sessions. It is the repo's context router, not a request to read the whole documentation set.

## Start Here

Default implementer packet:

1. This file.
2. The top **Current Routing** section of `docs/status.md`.
3. The relevant section of `docs/implementation/module-interfaces.md` only when touching implementation code or module behavior.
4. The exact `contracts/` files and code paths named by the task or discovered from the touched surface.
5. the agent working surface `docs/agent-working-surface`, especially `docs/agent-working-surface/decision-anchor-layer/README.md` for architecture-sensitive routing.

Open additional docs only when routed:

- `docs/flagged-positions.md` - read the summary table first; open a specific FP section only when its `Blocks` field or topic matches the task, or when adding/updating an FP (no new FPs should be added here, it's gonna be drained and retired with what the baseline decide and route later).
- `docs/agent-working-surface/decision-anchor-layer/` - use for DEC anchors, gap routing, and deciding whether future work belongs in architecture, platform spec, implementation, policy, or product/problem evidence.
- CDL/IDRs/ - open only when `docs/status.md`, the task, a touched contract, or code comments name the decision.
- `docs/architecture/`, phase specs, scenarios, and exploration archives - use for architecture-steward planning, drift investigation, or when a task explicitly depends on that context.
- `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md` (the CDL) the current architecture decision authority

If these sources disagree with each other or with the code, stop and surface the drift before implementing.

### Slicing Canonical Decisions (the new authority replacement of the now retired `adrs/`)

When routed, to prevent loading the entire 2600+ line `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md` into your context window, use the following resources:

- **README Index:** Refer to `docs/architecture/adrs-decisions-canonical-ledger/README.md` for a categorized directory linking direct line numbers to specific decisions.
- **Query Tool:**

```bash
python3 scripts/query_cdl.py --help                  
usage: query_cdl.py [-h] [--id ID] [--tag TAG] [--category CATEGORY] [--search SEARCH] [--format {concise,full,json}] [--list-tags]
                    [--list-categories]

Query and slice the Datarun Canonical Decision Ledger.

options:
  -h, --help            show this help message and exit
  --id ID               Retrieve details for a specific decision ID, or a list of IDs separated by commas/spaces (e.g. CDL-001,CDL-002)
  --tag TAG             Filter decisions by tag(s). Accepts comma- or space-separated lists (e.g. 'sync,identity' or 'sync identity')
  --category CATEGORY   Filter decisions by category (e.g. '3. Canonical event envelope')
  --search SEARCH       Search titles, decisions, constraints, and must-not-happen for text
  --format {concise,full,json}
                        Output format (default: concise for lists, full for ID queries)
  --list-tags           List all tags present in the ledger and exit
  --list-categories     List all categories present in the ledger and exit
```

## Steward And Implementer Split

- Architecture-steward sessions may read broadly, reconcile status, produce bounded implementation prompts, and dispatch working agents. Preserve product-needed lanes, but do not simulate multi-role workshop or gate-review chains as a substitute for product judgment or implementation.
- Implementer sessions should read only the bounded task packet plus the default implementer packet above.
- A task packet should state goal, files to read, authority/guardrails,
  forbidden work, expected tests, commit role/sequence and acceptance boundary,
  and stop-and-report conditions.

## Repository Map

- `docs/` - platform principles, ADRs, IDRs, phase plans, status, quality gates, and flagged positions.
- `docs/specifications/` - accepted product behavior and platform-detail specifications under architecture/contracts.
- `docs/operations/` - operational policies, executable runbooks, and rehearsal plans/records.
- `contracts/` - process-boundary intent: event envelope, sync protocol, flag catalog, platform shape schemas, and shared fixtures.
- `server/` - Spring Boot backend, event store, sync, configuration, identity, authorization, integrity detection, admin surfaces.
- `mobile/` - Flutter client, local event store, projection/advisory behavior, sync client, UI.

## Contracts Guidance

Current contract roles:

- `contracts/envelope.schema.json` defines the 11-field event envelope and closed 6-value envelope `type` vocabulary. The server validates against a bundled copy in `server/src/main/resources/envelope.schema.json`; `EnvelopeSchemaParityTest` requires the two copies to match.
- `contracts/sync-protocol.md` describes push/pull protocol intent and invariants. Verify against current server/mobile code when changing sync behavior.
- `contracts/flag-catalog.md` defines flag categories, default severity, resolvability, detection ordering, and state-exclusion semantics.
- `contracts/shapes/*.schema.json` defines platform-bundled assignment, identity, and integrity payload shapes. Server runtime bundles these contract schemas for platform payload validation. They are not deployer-authored shape registry rows, not deployer-editable, not packaged as deployer `shapes`, and not activity-bindable as form shapes.
- `contracts/shape-format.schema.json` defines the deployer-authored form shape DSL stored in the `shapes` table. It is not a platform payload schema.
- `contracts/config-package.schema.json` defines the server-emitted/mobile-consumed config package wire shape, including the current top-level package keys and tolerated unknown top-level keys for forward compatibility.
- `contracts/pattern-definition.schema.json` and `contracts/patterns/*.json` define platform-owned workflow pattern definitions. Server runtime loads these as the Pattern Registry source of truth, config packages deliver referenced definitions under `pattern_definitions`, and mobile reads the packaged definitions.
- `contracts/fixtures/*.json` are shared equivalence fixtures used by server and mobile tests.

when changing data that crosses server/mobile/process boundaries, treat `contracts/` as the first place to check. Do not assume it is a generated-code source or that every schema is runtime-validated everywhere.

## Architectural Guardrails

- No new envelope fields or envelope `type` values without Architecture level authority.
- Structurally valid state/policy anomalies are accepted and flagged; do not reject them unless the relevant ADR/IDR explicitly says structural validation applies.
- Authority and state are projections from events unless a documented B-to-C escape hatch has been explicitly activated.
- Respect the active phase boundaries in `docs/status.md`. Do not implement work listed as blocked or deferred.
- `docs/flagged-positions.md` is append-only. Use its table as the default lookup surface; add a new FP only for a real deferred architectural position with a specific trigger and gate, and only after confirming no existing FP covers it.

## Build And Test

From the repository root:

```bash
# Start test database
cd /home/hamza/datarun-platform && docker compose -f docker-compose.test.yml up -d test-db

# Run all server tests
(cd /home/hamza/datarun-platform/server && ./mvnw test)

# Run one server test class
(cd /home/hamza/datarun-platform/server && ./mvnw test -Dtest=ConfigIntegrationTest)

# Run all mobile tests
(cd /home/hamza/datarun-platform/mobile && flutter test)
```

## Working Practice

- Prefer existing patterns and helpers over new abstractions.
- Keep changes scoped to the active slice.
- Before creating a new durable documentation output, use
  `docs/documentation-organization.md` to choose its canonical home, metadata,
  index, evidence, and supersession route.
- Update docs/status/checklists only when implementation actually begins or lands.
- Leave unrelated dirty worktree changes alone.

## Commit And Progress Flow

`docs/commit-workflow.md` is the durable commit standard. When a task
authorizes commits:

1. Route selected work separately: `docs(<domain>): route <outcome>`.
2. Land any required decision/spec/policy before implementation.
3. Commit implementation with its tests using `feat`, `fix`, `test`, or
   `refactor`; do not mark the NW accepted in that commit.
4. After verification, record standing separately with
   `docs(status): accept <outcome>` when backlog/BAR/status changes.
5. Create `docs(checkpoint): snapshot <milestone>` only for a real milestone,
   handoff, or long-pause boundary, never for every NW item.

Use `<type>(<scope>): <imperative outcome>` and add `NW: NW-###` in the commit
body/trailer for NW-owned work. Stage explicit owned paths, inspect the staged
diff, keep unrelated worktree changes out, and report the final worktree state.
Review fixes use their truthful behavior type; documentation-only organization
uses `docs(hygiene)` only when semantics and accepted standing are unchanged.
