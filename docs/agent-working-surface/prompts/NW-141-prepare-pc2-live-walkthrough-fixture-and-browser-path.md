# NW-141 - Prepare PC2 Live Walkthrough Fixture And Browser Path

## Goal

Prepare the retained isolated PC2 lab stack for a later bounded live browser
walkthrough proof of Product Candidate 2 Single Work-Linked Attention Review.

This is precondition/environment work only. It must either prove that one
synthetic scoped work-linked attention item and a browser-capable execution
path are ready, or classify the remaining blocker exactly.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-140-pc2-live-browser-walkthrough-proof.md`
- `docs/agent-working-surface/artifacts/NW-139-pc2-synthetic-web-admin-session-provisioning.md`
- `docs/agent-working-surface/artifacts/NW-126-pc2-synthetic-lab-environment-reconciliation.md`
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
- Bounded preparation inside only the retained isolated PC2 stack.
- Use synthetic/non-sensitive PC2 fixture data only.
- Use the synthetic NW-139 OIDC path and synthetic web-admin actor.
- Establish evidence that one visible scoped source work item has one
  unresolved work-linked attention cue for the synthetic actor.
- Establish evidence that the operator has a usable browser-capable path for a
  later live walkthrough.
- Record precondition evidence in one artifact.
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

Do not broaden the fixture beyond one synthetic work-linked attention item for
PC2 Single Work-Linked Attention Review.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-141-pc2-live-walkthrough-preconditions.md
```

The artifact must state:

- whether PC2 live walkthrough preconditions are `READY`, `PARTIAL`,
  `NOT_READY`, or `FAIL`;
- exact app URL and tunnel path used;
- R12 continuity before and after;
- PC2 login/session standing;
- PC2 `/web-admin/operational` standing;
- one-item fixture standing for source work, attention cue, review page, and
  resolver authority;
- browser-capable execution-path standing;
- any friction or blocker;
- retained/cleanup state;
- exactly one next route.

## Validation

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-141" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-141-pc2-live-walkthrough-preconditions.md
grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-141-pc2-live-walkthrough-preconditions.md
```

Runtime automated tests are skipped unless NW-141 changes runtime code, which
is forbidden.
