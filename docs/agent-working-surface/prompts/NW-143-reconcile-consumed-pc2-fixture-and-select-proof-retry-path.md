# NW-143 - Reconcile Consumed PC2 Fixture And Select Proof Retry Path

## Goal

Resolve the narrow blocker left by NW-142: the retained NW-141 PC2 fixture was
already consumed by an accepted manual resolution before the bounded NW-142 live
browser proof could record before/after browser evidence.

This is a reconciliation and retry-selection route only. It must not silently
create a replacement fixture or treat the consumed fixture as a completed proof
without explicit owner evidence.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-142-pc2-live-browser-walkthrough-proof.md`
- `docs/agent-working-surface/artifacts/NW-141-pc2-live-walkthrough-preconditions.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/validation-matrix.md`

## Required First Steps

1. Inspect R12 continuity before touching or using PC2 state.
2. Confirm the retained PC2 stack still matches NW-142 infrastructure/auth
   standing.
3. Confirm the consumed fixture state:
   - source work event `14114114-3141-4141-9141-141141141141`;
   - fixture flag `14114114-4141-4141-9141-141141141141`;
   - accepted resolution event `bcafd46a-d439-4548-b51d-bc8ec9dddb09`;
   - no unresolved NW-141 fixture flag remains.

## Allowed

- Read-only R12 inspection.
- Read-only PC2 fixture/state inspection.
- Owner-facing reconciliation of whether the already-submitted manual
  resolution can be accepted as informal/manual evidence only.
- Select exactly one bounded retry or parking route.
- Add a replacement synthetic fixture only if Hamza explicitly approves that
  replacement in the NW-143 task request or during NW-143, and the artifact
  records the approval, before/after state, and why the consumed fixture cannot
  be reused.
- Record one artifact.
- Update status, backlog, and artifact index.
- Add one successor prompt only if exactly one next route is selected.

## Forbidden

No R12 mutation.
No R12 Keycloak mutation or repurposing.
No real users/data/secrets.
No production approval.
No auth bypass or dev-login shortcut.
No runtime app code, Dockerfile/build-tooling, schemas, tests, CI,
product/platform specs, BAR, CDL, gap register, reporting/export,
queue/list review, automation, tenant/control-plane, mobile code, server
feature implementation, or all-PC proof.

Do not create another fixture by default. Do not submit another resolution
decision unless a replacement fixture is explicitly approved and prepared under
NW-143.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-143-consumed-pc2-fixture-reconciliation.md
```

The artifact must state:

- `READY`, `PARKED`, `NOT_READY`, or `FAIL`;
- R12 continuity before and after;
- consumed fixture standing;
- whether any owner-supplied manual browser evidence exists for the already
  submitted resolution;
- whether a replacement fixture was explicitly approved or not;
- the selected next route, exactly one;
- retained/cleanup state;
- validation evidence.

## Validation

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-143" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-143-consumed-pc2-fixture-reconciliation.md
grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-143-consumed-pc2-fixture-reconciliation.md
```

Runtime automated tests are skipped unless NW-143 changes runtime code, which
is forbidden.
