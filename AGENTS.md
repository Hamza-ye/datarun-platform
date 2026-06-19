# Datarun Platform - Agent Instructions

Purpose: keep sessions focused on the active product/task slice, protect
accepted boundaries, and leave verifiable evidence. This is a router, not a
request to read the whole docs tree.

## Default Startup Packet
1. Read the user task and identify the active NW/product slice if named.
2. Read `docs/status.md` Current Routing.
3. Read the active NW row or task packet when one is named.
4. Read only the product spec, code, tests, contracts, or docs needed for the
   task or touched files.
5. For user-visible work, anchor to the accepted product spec or PM handoff and
   explicit non-goals before architecture routing.
6. If no active slice is selected and implementation is implied, stop and ask
   for the selected NW/product slice before coding.

Default role is implementer. Use steward routing only when the task explicitly
asks for audit, routing, reconciliation, checkpoint, architecture, or gap work,
or when a stop trigger fires.

## Task Packet Contract
A good task packet states: goal, files to read, accepted boundaries, forbidden
work, expected tests, commit/acceptance boundary, and stop conditions.

## Product-first Rule
For ordinary implementation, describe the user/deployment outcome first, then
the implementation surface. Do not turn product labels into authority,
identity, scope, contract, or storage primitives.

## Architecture / Gap Routing Triggers
Stop and route through the gap playbook before implementation if the change
would affect envelope fields/types, stored event meaning, sync/access scope,
authority sources, durable workflow state, resolver truth, deployer config
power, retention/security promises, reporting/audit breadth, tenant/runtime
partitioning, contract semantics, or any blocked/deferred row in `docs/status.md`.

Use `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
only when a trigger fires or the task is steward/routing work. Use CDL/IDRs only
when routed by status, task, code comments, contracts, or gap classification.

## Repository Map
- `docs/status.md` - current routing and standing.
- `docs/specifications/` - accepted product/platform behavior.
- `docs/agent-working-surface/` - steward routing, backlog, BAR, and prompts.
- `contracts/` - process/wire contracts and shared fixtures.
- `server/` - Spring Boot backend.
- `mobile/` - Flutter client.
- `docs/operations/` - policies, runbooks, and rehearsal evidence.

## Validation Evidence
Use the narrowest relevant focused test first, then the required full gate for
the touched surface. Report exact command, cwd, result, count/duration when
available, and skipped-gate rationale.

Use `docs/agent-working-surface/validation-matrix.md` for touched-surface gates
and evidence format.

- Server: see `server/AGENTS.md`.
- Mobile: see `mobile/AGENTS.md`.
- Contracts: see `contracts/AGENTS.md`.
- Docs-only: run `git diff --check`.

`flutter analyze` is not a hard gate until known issues are fixed or baselined.
Android compile is required for native/platform/auth/plugin changes.

## Working Rules
Prefer existing patterns. Keep changes scoped. Leave unrelated dirty work alone.
Update status/backlog/BAR only when the task authorizes that state change.
For durable docs, use `docs/documentation-organization.md`.

## Commit Trace
Commit only when authorized. Follow `docs/commit-workflow.md`. For NW-owned
work include `NW: NW-###` and validation evidence. Do not mark work accepted
just because files changed.

## Steward Guidance
Broad reading, CDL slicing, gap classification, dispatch packets, and
architecture reconciliation live in
`docs/agent-working-surface/steward-session-guide.md`.
