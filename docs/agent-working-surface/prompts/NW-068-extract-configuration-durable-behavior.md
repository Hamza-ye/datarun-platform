# NW-068 Agent Prompt: Extract Configuration Durable Behavior

You are working in `/home/hamza/datarun-platform`.

## Goal

Create the durable platform/contract route for accepted configuration,
expression, and config-package behavior that is currently scattered across
IDR-017, IDR-018, IDR-019, BAR evidence, contracts, fixtures, and implementation
notes.

This is a specification extraction task, not an implementation task and not an
old-document cleanup pass.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md` NW-068
6. `docs/agent-working-surface/artifacts/architecture-classification-drift-audit.md`
7. `docs/agent-working-surface/artifacts/idr-durable-surface-routing-audit.md`
8. `docs/implementation/phases/phase-3.md`
9. `docs/implementation/phases/phase-3e.md`
10. `docs/decisions/idr-017-shape-storage.md`
11. `docs/decisions/idr-018-expression-grammar.md`
12. `docs/decisions/idr-019-config-package.md`
13. BAR-010 and BAR-011 in
    `docs/agent-working-surface/baseline-acceptance-register.md`
14. `contracts/shape-format.schema.json`
15. `contracts/config-package.schema.json`
16. `contracts/fixtures/expression-evaluation.json`
17. `contracts/sync-protocol.md`

## Expected Output

Create or update only the durable surfaces needed for this slice:

- platform specification for expression language behavior;
- platform specification for configuration package and shape lifecycle
  behavior;
- contract/protocol update only if needed for config discovery, endpoint auth,
  ETag / `If-None-Match`, or expression schema traceability.

Use semantic filenames under `docs/specifications/platform/`. Include required
document metadata from `docs/documentation-organization.md`.

## Required Decisions Inside The Slice

Decide and state explicitly:

- whether `contracts/expression.schema.json` should be added now, or whether
  the durable expression surface is the platform spec plus
  `contracts/fixtures/expression-evaluation.json` and
  `contracts/config-package.schema.json`;
- whether `/api/sync/config`, auth, ETag, and `If-None-Match` semantics belong
  in `contracts/sync-protocol.md` or in a separate process-boundary contract
  note;
- which IDR-017/018/019 details are accepted platform behavior, existing
  contract authority, implementation evidence, or old-doc trace only.

## Guardrails

- Do not change runtime code, schemas, fixtures, tests, BAR, CDL, or old IDR
  text unless the slice explicitly routes a contract trace fix and the diff is
  documentation-only.
- Do not duplicate JSON schema contents in platform specs; link to contracts.
- Do not create a new durable IDR-like surface.
- Do not promote deployer-authored code, triggers, dynamic queries, functions,
  recursion, joins, custom scope logic, or device-side execution.
- Do not change envelope fields, envelope `type` values, shape refs, config
  package wire shape, expression grammar semantics, or mobile promotion
  behavior.
- Stop if accepted behavior appears to change rather than become better
  documented.

## Verification

Run:

```bash
git diff --check
```

Also verify that:

- `docs/documentation-organization.md` and `docs/commit-workflow.md` are not
  changed;
- new durable specs are indexed from `docs/specifications/platform/README.md`;
- links to contracts and BAR/status evidence are valid by path search.

## Commit Flow

Use separate commits for route, durable specification, optional contract
documentation, and status acceptance if standing changes. Include:

```text
NW: NW-068
```

Do not mark NW-068 accepted until durable outputs and verification are complete.
