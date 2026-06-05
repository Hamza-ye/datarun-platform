# NW-052 Agent Prompt: Decide Shared-Device Session Lifecycle

You are working in `/home/hamza/datarun-platform`.

## Goal

Decide the platform route for shared physical devices without changing assignment scope authority.

Exit target:

```text
Datarun has a clear shared-device session and local data boundary: event authorship remains bound to the authenticated actor, session switching cannot leak prior actor data, and sync/access semantics remain assignment-derived.
```

This is decision-routing work only unless current routing explicitly promotes an IDR. Do not implement runtime behavior.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/access-exceptions-shared-device-scope-exploration.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-049 through NW-054.
5. `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
6. `docs/decisions/idr-028-production-principal-binding-administration.md`
7. `docs/implementation/module-interfaces.md` sections `Authenticated Actor Resolver`, `Scope Resolver`, `Event Store`, and sync-related notes.
8. `contracts/sync-protocol.md`
9. Scenario pressure: `docs/access-control-scenario.md`, `docs/scenarios/19-offline-capture-and-sync.md`, `docs/scenarios/24-long-running-deployment-data-lifecycle.md`, and `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`.
10. Inspect only if needed: `mobile/lib/data/device_identity.dart`, `mobile/lib/data/sync_service.dart`, and `mobile/lib/data/event_store.dart`.
11. Use `scripts/query_cdl.py` only for CDL-030, CDL-031, CDL-032, CDL-037, and CDL-055.

## Questions To Answer

1. What is the minimum safe session switch model for a shared physical device?
2. Should local storage be per actor, purged on switch, or otherwise partitioned?
3. How should unpushed events, actor tokens, device watermarks, and config state behave across actor switches?
4. Which parts are auth/session lifecycle versus retention/security?
5. What tests prove prior actor data and authorship cannot leak to the next actor?

## Guardrails

- Do not treat shared-device support as a new scope mechanism.
- Do not allow one local store to alternate actors without a decided partition/purge model.
- Do not allow events to be authored by anything except the currently authenticated actor.
- Do not promote IdP groups, claims, roles, or JWT `actor_id` as authority.
- Do not rewrite normal live-sync watermarks or make mobile authoritative rejection.
- Do not implement mobile login UX or runtime changes in this decision slice.

## Expected Output

Either:

- a decision artifact or IDR, if current routing explicitly requires one; or
- a stop report naming the product/security choice needed before a decision can be written.

If a successor implementation is selected, add a bounded implementation row and prompt only after the decision lands.

## Verification

Run:

```bash
git diff --check
```

Inspect any Markdown tables you add.

## Stop And Report

Stop if shared-device pressure cannot be separated from authority changes, requires group/claim authority, requires normal sync watermark rewrites, or needs runtime implementation before the session/retention decision is made.
