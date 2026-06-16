# NW-053 Agent Prompt: Decide Subject/Query/Custom Scope Boundary

You are working in `/home/hamza/datarun-platform`.

## Goal

Decide whether current `subject_list` scope is sufficient for the identified pressure, or whether a BAR-108 platform-owned scope mechanism should be promoted.

Exit target:

```text
Datarun has a clear route for subject-scope variants and query/custom scope pressure without deployer-defined containment logic, config-as-code, normal sync leaks, or assignment-admin containment drift.
```

This is decision-routing work only unless current routing explicitly promotes an IDR. Do not implement runtime behavior.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/artifacts/NW-049-access-exceptions-shared-device-scope-exploration.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-049 through NW-054.
5. `docs/decisions/idr-024-multi-axis-assignment-containment.md`
6. `docs/decisions/idr-029-assignment-admin-command-capability.md`
7. `docs/agent-working-surface/architecture-rationale-and-routing-companion.md` sections on routing, irreversibility, configuration anti-patterns, and scope routing.
8. `docs/implementation/module-interfaces.md` sections `Scope Resolver`, `Assignment Admin Capability Policy`, `Config Packager`, and sync-related notes.
9. `contracts/shapes/assignment_created.schema.json`
10. `contracts/sync-protocol.md`
11. Scenario pressure: `docs/access-control-scenario.md`, `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`, and `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`.
12. Use `scripts/query_cdl.py` only for CDL-030, CDL-031, CDL-032, and CDL-055.

## Questions To Answer

1. Which concrete pressure cannot be represented by explicit `subject_list`, geography, activity, and temporal bounds?
2. Is the pressure ergonomic/product complexity, or a real platform scope mechanism gap?
3. Would query/custom scope require deployer-authored containment logic or config-as-code?
4. If a new platform-owned mechanism is needed, what minimal semantics and assignment containment tests must exist?
5. What implementation rows should remain blocked until the decision lands?

## Guardrails

- Do not add a new scope mechanism in this slice.
- Do not add deployer-defined scope scripts, queries, loops, custom containment, or config-as-code.
- Do not alter assignment payload schemas, envelope fields, envelope `type` values, or config-package fields.
- Do not weaken sync scope equals access scope.
- Keep assignment-admin command-plus-containment from NW-050 intact.

## Expected Output

If the slice selects binding platform behavior, route it to the correct durable
surface under `docs/documentation-organization.md`; do not assume an IDR is the
default home.

If the slice only routes, defers, compares paths, or stops for product/platform input, create:

```text
docs/agent-working-surface/artifacts/NW-053-subject-query-custom-scope-boundary-routing.md
```

The output should decide one of:

- explicit deferral with rationale that `subject_list` remains sufficient;
- a successor durable decision/spec route for a bounded BAR-108 platform-owned scope mechanism route;
- a stop report naming the product/platform decision needed before routing can continue.

Do not create a non-authoritative file with "decision" in the filename under `docs/agent-working-surface/`.

Do not add implementation rows until the decision lands.

## Backlog And Status Updates

If the output lands:

- Mark NW-053 `accepted`.
- Update `docs/status.md` Current Routing with the recommendation.
- Update `docs/agent-working-surface/platform-next-work-backlog.md` with evidence and any successor rows/prompts.

If the route is blocked by a product/platform decision:

- Leave NW-053 `future_decision` or mark it `blocked` with the missing decision named.
- Do not invent an implementation path.

## Verification

Run:

```bash
git diff --check
```

Inspect any Markdown tables you add.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
docs(auth): decide subject scope boundary
```

## Stop And Report

Stop if the route requires deployer-authored scope logic, normal sync data leaks, envelope/schema changes, or assignment-admin containment changes before a platform decision exists.
