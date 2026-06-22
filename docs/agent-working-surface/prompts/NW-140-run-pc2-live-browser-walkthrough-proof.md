# NW-140 - Run PC2 Live Browser Walkthrough Proof

## Goal

Run a bounded live browser walkthrough proof for PC2 Single Work-Linked
Attention Review against the retained isolated PC2 lab stack prepared by
NW-126 and NW-139.

This is owner-review proof only. It is not production approval, not real-user
use, not R12 proof, not all-PC proof, and not runtime implementation.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-139-pc2-synthetic-web-admin-session-provisioning.md`
- `docs/agent-working-surface/artifacts/NW-126-pc2-synthetic-lab-environment-reconciliation.md`
- `docs/agent-working-surface/artifacts/NW-124-pc2-synthetic-walkthrough-proof.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/validation-matrix.md`

## Required First Steps

1. Inspect R12 continuity before touching or using PC2 state.
2. Confirm the retained PC2 stack still matches NW-139:
   source revision, compose project `datarun-pc2-nw125`, app port
   `127.0.0.1:28080`, synthetic OIDC provider port `172.17.0.1:28090`,
   principal binding, reviewed config, `/web-admin/login`, and authenticated
   `/web-admin/operational`.
3. Establish the two-port tunnel:

```bash
ssh -o HostKeyAlias=datarun-app.lab -N \
  -L 28080:127.0.0.1:28080 \
  -L 28090:172.17.0.1:28090 \
  nmcp@192.168.1.213
```

## Allowed

- Read-only R12 inspection.
- Live browser walkthrough proof against only the isolated PC2 stack.
- Use the synthetic NW-139 OIDC path and synthetic web-admin actor.
- Record proof evidence in one artifact.
- Update status, backlog, and artifact index.
- Add one successor prompt only if exactly one next route is selected.

## Forbidden

No R12 mutation.
No R12 Keycloak mutation or repurposing.
No real users/data/secrets.
No production approval.
No auth bypass or dev-login shortcut.
No runtime app code, Dockerfile/build-tooling, schemas, tests, CI,
product/platform specs, BAR, CDL, gap register, reporting/export, queue/list
review, automation, tenant/control-plane, mobile code, server feature
implementation, or all-PC proof.

Do not broaden the walkthrough beyond PC2 Single Work-Linked Attention Review.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-140-pc2-live-browser-walkthrough-proof.md
```

The artifact must state:

- whether the PC2 live browser walkthrough proof is `PASS`, `PARTIAL`,
  `NOT_READY`, or `FAIL`;
- exact app URL and tunnel path used;
- R12 continuity before and after;
- PC2 login/session standing;
- PC2 `/web-admin/operational` standing;
- walkthrough beats observed for the one-item attention review path;
- any friction or blocker;
- retained/cleanup state;
- exactly one next route.

## Validation

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-140" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-140-pc2-live-browser-walkthrough-proof.md
grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-140-pc2-live-browser-walkthrough-proof.md
```

Runtime automated tests are skipped unless NW-140 changes runtime code, which
is forbidden.
