# NW-145 - Reconcile Post-PC2 Live Proof Standing And Select Next Route

## Goal

Reconcile the accepted NW-144 PC2 replacement live browser proof into the
control plane, classify the residual isolated-PC2 lab state, and select exactly
one next route.

This is a routing/status reconciliation slice only. Do not mutate runtime state.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-144-pc2-replacement-fixture-live-browser-proof.md`
- `docs/agent-working-surface/artifacts/NW-143-consumed-pc2-fixture-reconciliation.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/validation-matrix.md`

## Required Scope

- Record PC2 proof standing after NW-144.
- Decide whether the residual unassigned PC2 detector flag is:
  - parked lab residue,
  - a narrow cleanup route,
  - or evidence for an existing future conflict route.
- Select exactly one next route for the project control plane.

## Preserve Boundaries

Do not mutate R12.
Do not mutate the retained isolated PC2 stack.
Do not use real users, real organization data, production secrets, or
NW-093-gated material.
Do not approve production.
Do not add auth bypass/dev-login.
Do not change runtime app code, Dockerfile/build tooling, schemas, tests, CI,
product/platform specs, BAR, CDL, gap register, reporting/export,
queue/list review, automation, tenant/control-plane, mobile code, server
feature implementation, or all-PC proof.

## Output

Create:

`docs/agent-working-surface/artifacts/NW-145-post-pc2-live-proof-standing-and-next-route.md`

The artifact must state:

- PC2 live proof standing after NW-144;
- R12 continuity status from NW-144 evidence, without re-mutating R12;
- residual isolated-PC2 lab state;
- whether any blocker remains for PC2 Single Work-Linked Attention Review;
- exactly one next route.

## Validation

Run:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-145" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-145-post-pc2-live-proof-standing-and-next-route.md
grep -n "PC2 live proof standing" docs/agent-working-surface/artifacts/NW-145-post-pc2-live-proof-standing-and-next-route.md
```

Runtime automated tests are skipped unless NW-145 changes runtime code, which
is forbidden.
