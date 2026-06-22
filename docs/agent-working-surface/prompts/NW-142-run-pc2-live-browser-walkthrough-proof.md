# NW-142 - Run PC2 Live Browser Walkthrough Proof

## Goal

Run the bounded live browser walkthrough proof for Product Candidate 2 Single
Work-Linked Attention Review against only the retained isolated PC2 lab stack.

NW-141 prepared the required preconditions: exactly one synthetic scoped
work-linked attention item is visible and reviewable for the synthetic
web-admin actor, and a browser-capable path works through the two-port tunnel.

This is not real data. Do not use real users, real organization data,
production secrets, or NW-093-gated material.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-141-pc2-live-walkthrough-preconditions.md`
- `docs/agent-working-surface/artifacts/NW-140-pc2-live-browser-walkthrough-proof.md`
- `docs/agent-working-surface/artifacts/NW-139-pc2-synthetic-web-admin-session-provisioning.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/validation-matrix.md`

## Required First Steps

1. Inspect R12 continuity before touching or using PC2 state.
2. Confirm the retained PC2 stack still matches NW-141:
   source revision, compose project `datarun-pc2-nw125`, app port
   `127.0.0.1:28080`, synthetic OIDC provider port `172.17.0.1:28090`,
   principal binding, reviewed config, `/web-admin/login`, authenticated
   `/web-admin/operational`, one visible scoped source work item, one
   `Needs review` cue, reachable attention review page, and designated-resolver
   form standing.
3. Establish the two-port tunnel:

```bash
ssh -o HostKeyAlias=datarun-app.lab -N \
  -L 28080:127.0.0.1:28080 \
  -L 28090:172.17.0.1:28090 \
  nmcp@192.168.1.213
```

## App URL

`http://127.0.0.1:28080/web-admin/operational`

## Allowed

- Read-only R12 inspection.
- Live browser walkthrough proof against only the retained isolated PC2 stack.
- Use the synthetic NW-139 OIDC path and synthetic web-admin actor.
- Use the synthetic NW-141 scoped work-linked attention fixture.
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
product/platform specs, BAR, CDL, gap register, reporting/export,
queue/list review, automation, tenant/control-plane, mobile code, server
feature implementation, or all-PC proof.

Do not broaden beyond the one-item PC2 Single Work-Linked Attention Review
path. Do not submit the resolution form unless Hamza explicitly approves that
mutation during NW-142 and the artifact records the before/after fixture state.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-142-pc2-live-browser-walkthrough-proof.md
```

The artifact must state:

- `PASS`, `PARTIAL`, `NOT_READY`, or `FAIL`;
- exact app URL and tunnel path used;
- R12 continuity before and after;
- PC2 login/session standing;
- PC2 `/web-admin/operational` standing;
- one-item attention-review walkthrough beats observed;
- browser evidence path and result;
- whether the resolution form was only observed or submitted;
- any friction or blocker;
- retained/cleanup state;
- exactly one next route.

## Validation

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-142" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-142-pc2-live-browser-walkthrough-proof.md
grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-142-pc2-live-browser-walkthrough-proof.md
```

Runtime automated tests are skipped unless NW-142 changes runtime code, which
is forbidden.
