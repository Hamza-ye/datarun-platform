# NW-051 Agent Prompt: Decide Special Read/Write Access Boundary

You are working in `/home/hamza/datarun-platform`.

## Goal

Decide the bounded route for auditor/special read visibility and emergency/special write pressure identified by NW-049.

Exit target:

```text
Datarun has a decision route for special read and write access that preserves assignment-derived authority, sync/access equality, NW-050 assignment-admin command capability, BAR-104 production auth, and IDR-026 resolver equality.
```

This is decision-routing work only unless the current routing explicitly promotes an IDR. Do not implement runtime behavior.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/artifacts/NW-049-access-exceptions-shared-device-scope-exploration.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-049 through NW-054.
5. `docs/agent-working-surface/artifacts/NW-043-assignment-admin-authority-exploration.md`
6. `docs/decisions/idr-021-role-action-enforcement-model.md`
7. `docs/decisions/idr-024-multi-axis-assignment-containment.md`
8. `docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`
9. `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
10. `docs/decisions/idr-029-assignment-admin-command-capability.md`
11. `docs/implementation/module-interfaces.md` sections `Scope Resolver`, `Assignment Admin Capability Policy`, `Authenticated Actor Resolver`, `Conflict Detector`, and sync-related notes.
12. `contracts/sync-protocol.md`
13. `contracts/flag-catalog.md`
14. Scenario pressure: `docs/access-control-scenario.md`, `docs/scenarios/24-long-running-deployment-data-lifecycle.md`, and `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`.
15. Use `scripts/query_cdl.py` only for CDL-030, CDL-031, CDL-032, CDL-035, CDL-037, CDL-046, and CDL-055.

## Questions To Answer

1. Is current-scope auditor visibility adequately modeled as ordinary assignments with no or limited work actions?
2. If broad audit/history is needed, is it a separate read surface, a platform-owned special-access mechanism, or explicit deferral?
3. Does emergency/special write authority fit ordinary time-bounded assignments plus role-action permissions?
4. What would be unsafe to authorize through assignment-admin command capability?
5. What tests would prove the chosen route without weakening sync/access equality or exact resolver equality?

## Guardrails

- Do not reopen NW-050 assignment-admin command capability.
- Do not add new scope mechanisms, envelope fields, envelope `type` values, `authority_context`, or `assignment_ref`.
- Do not infer authority from IdP groups, roles, claims, JWT `actor_id`, request-body actor IDs, UI labels, or scenario vocabulary.
- Do not implement auditor semantics, emergency override semantics, audit-history APIs, report APIs, resolver reassignment, auto-resolution, or mobile authoritative rejection.
- Keep structurally valid stale/offline work accepted-and-flagged unless a successor decision explicitly changes the model.

## Expected Output

If the slice selects binding platform behavior, route it to the correct durable
surface under `docs/documentation-organization.md`; do not assume an IDR is the
default home.

If the slice only routes, defers, compares paths, or stops for product/security input, create:

```text
docs/agent-working-surface/artifacts/NW-051-special-read-write-access-boundary-routing.md
```

The output should decide one of:

- explicit deferral;
- simple current-scope auditor visibility as ordinary assignment/config posture;
- a successor durable decision/spec route for broad audit/history or emergency/special write access;
- a stop report naming the product/security decision needed before a binding decision can be written.

Do not create a non-authoritative file with "decision" in the filename under `docs/agent-working-surface/`.

If a successor implementation is chosen, add a new bounded backlog row and prompt only after the decision lands. Do not add implementation rows from this slice before the decision is explicit.

## Backlog And Status Updates

If the output lands:

- Mark NW-051 `accepted`.
- Update `docs/status.md` Current Routing with the recommendation.
- Update `docs/agent-working-surface/platform-next-work-backlog.md` with evidence and any successor rows/prompts.

If the route is blocked by a product/security decision:

- Leave NW-051 `future_decision` or mark it `blocked` with the missing decision named.
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
docs(auth): decide special access boundary
```

## Stop And Report

Stop if the route requires new scope mechanisms, envelope/schema changes, IdP group/claim authority, normal sync watermark rewrites, resolver reassignment, auto-resolution, or mobile authoritative rejection before a product/security decision exists.
