# NW-054 Agent Prompt: Decide Device Data Expiry And Retained Local Data Boundary

You are working in `/home/hamza/datarun-platform`.

## Goal

Decide the route for device data expiry and retained local data after scope contraction, worker exit, shared-device handoff, or sensitivity-driven retention pressure.

Exit target:

```text
Datarun has a security/platform route for local expiry and retained data that preserves canonical server event history, normal sync watermarks, subject-history isolation, and assignment-derived authority.
```

This is decision-routing work only unless current routing explicitly promotes an IDR. Do not implement runtime behavior.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/access-exceptions-shared-device-scope-exploration.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-049 through NW-054.
5. `docs/agent-working-surface/baseline-acceptance-register.md` rows BAR-008 and BAR-106.
6. `docs/implementation/module-interfaces.md` sections `Event Store`, `Projection Engine`, `Scope Resolver`, and sync-related notes.
7. `contracts/sync-protocol.md`
8. Scenario pressure: `docs/scenarios/24-long-running-deployment-data-lifecycle.md` and `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`.
9. Inspect only if needed: `mobile/lib/data/event_store.dart`, `mobile/lib/data/sync_service.dart`, and `mobile/test/selective_retain_test.dart`.
10. Use `scripts/query_cdl.py` only for CDL-031, CDL-037, CDL-046, and CDL-055.

## Questions To Answer

1. What data must remain for provenance, local authority projection, and safe retry after scope contraction?
2. What data may or must be purged from a device after scope contraction, actor switch, or expiry?
3. Does the pressure require field-level sensitivity, encryption, redaction, export, or compliance behavior under BAR-106?
4. How should expiry interact with normal live-sync watermarks and subject-history backfill?
5. What crash-safety and regression tests are required before implementation?

## Guardrails

- Do not delete, rewrite, or expire canonical server events.
- Do not mutate normal live-sync watermarks to simulate expiry.
- Do not turn subject-history into arbitrary audit pull.
- Do not claim UI hiding is sufficient for sensitive local data.
- Do not add field-level sensitivity, encryption, redaction, or export behavior without a BAR-106 security/platform decision.
- Do not implement mobile purge changes in this decision slice.

## Expected Output

Create:

```text
docs/agent-working-surface/device-data-expiry-retention-boundary-decision.md
```

The artifact should decide one of:

- explicit deferral under BAR-106 with rationale;
- a successor IDR/security-platform decision for a bounded retention/security implementation route;
- a stop report naming the security/platform decision needed before routing can continue.

Do not add implementation rows until the decision lands.

## Backlog And Status Updates

If the artifact lands:

- Mark NW-054 `accepted`.
- Update `docs/status.md` Current Routing with the recommendation.
- Update `docs/agent-working-surface/platform-next-work-backlog.md` with evidence and any successor rows/prompts.

If the route is blocked by a security/platform decision:

- Leave NW-054 `future_decision` or mark it `blocked` with the missing decision named.
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
docs(retention): decide device data expiry boundary
```

## Stop And Report

Stop if the route requires server event deletion, live-sync watermark rewrites, field-level sensitivity/encryption/redaction, or shared-device local-store behavior before a security/platform decision exists.
