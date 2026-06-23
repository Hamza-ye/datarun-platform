# NW-144 - Create Replacement PC2 Fixture And Run Live Browser Proof

## Goal

Finish the PC2 Single Work-Linked Attention Review proof cleanly after NW-142
found that the original NW-141 fixture had already been consumed.

Create exactly one replacement synthetic, non-sensitive, scoped PC2
work-linked attention fixture inside only the retained isolated PC2 stack, run
the bounded live browser proof through the two-port tunnel, submit exactly one
`Accept` decision as the synthetic web-admin actor, and record before/after
evidence that the unresolved attention item clears.

This is not real data. Do not use real users, real organization data,
production secrets, or NW-093-gated material.

## Owner Approval

NW-144 requires explicit owner approval to create one replacement fixture and
submit one synthetic `Accept` resolution. If that approval is not present in
the NW-144 task request or provided during NW-144, stop before mutation.

## Inputs

- `docs/status.md`
- `docs/agent-working-surface/platform-next-work-backlog.md`
- `docs/agent-working-surface/artifacts/NW-143-consumed-pc2-fixture-reconciliation.md`
- `docs/agent-working-surface/artifacts/NW-142-pc2-live-browser-walkthrough-proof.md`
- `docs/agent-working-surface/artifacts/NW-141-pc2-live-walkthrough-preconditions.md`
- `docs/specifications/product/product-candidate-2-pm-handoff.md`
- `docs/specifications/platform/conflict-flag-resolution-and-attention-query-boundary.md`
- `docs/agent-working-surface/validation-matrix.md`

## Required First Steps

1. Inspect R12 continuity before touching or using PC2 state.
2. Confirm the retained PC2 stack still matches NW-143 infrastructure/auth
   standing.
3. Confirm no unresolved NW-141 fixture flag remains and that a replacement
   fixture is needed.
4. Establish the two-port tunnel:

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
- One replacement synthetic/non-sensitive PC2 fixture inside only the retained
  isolated PC2 stack.
- Use the synthetic NW-139 OIDC path and synthetic web-admin actor.
- Use accepted assignment/sync/provisioning paths where possible.
- Submit exactly one `Accept` decision for the replacement fixture as actor
  `33333333-3333-4333-8333-333333333333`.
- Record before/after HTTP/browser/DB evidence in one artifact.
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

Do not broaden beyond the one replacement-item PC2 Single Work-Linked Attention
Review path. Do not create more than one replacement unresolved attention item.

## Output

Create one artifact:

```text
docs/agent-working-surface/artifacts/NW-144-pc2-replacement-fixture-live-browser-proof.md
```

The artifact must state:

- `PASS`, `PARTIAL`, `NOT_READY`, or `FAIL`;
- exact app URL and tunnel path used;
- R12 continuity before and after;
- replacement fixture IDs and standing before proof;
- PC2 login/session standing;
- PC2 `/web-admin/operational` standing before and after `Accept`;
- browser walkthrough beats observed;
- submitted decision: `Accept`;
- resolver standing and actor match;
- post-submit attention-cleared evidence;
- any friction or blocker;
- retained/cleanup state;
- exactly one next route.

## Validation

Always run:

```bash
cd /home/hamza/datarun-platform
git diff --check
rg "NW-144" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
test -f docs/agent-working-surface/artifacts/NW-144-pc2-replacement-fixture-live-browser-proof.md
grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-144-pc2-replacement-fixture-live-browser-proof.md
```

Runtime automated tests are skipped unless NW-144 changes runtime code, which
is forbidden.
