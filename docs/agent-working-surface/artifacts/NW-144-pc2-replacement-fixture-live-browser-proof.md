# NW-144 PC2 Replacement Fixture Live Browser Proof

Status: non-authoritative product-validation / owner-review evidence artifact

Date: 2026-06-23

## Classification

`PASS`

The retained isolated PC2 stack received exactly one replacement synthetic
work-linked attention fixture visible to the synthetic web-admin actor, the
browser walkthrough ran through the required two-port tunnel, and the
owner-approved `Accept` decision was submitted once through the real
server-rendered attention review form.

The replacement fixture attention item cleared after submission. One unrelated
unassigned detector flag appeared against the old consumed NW-141 source work;
it is recorded below as friction and is not a replacement fixture item or a
synthetic-actor review cue.

## App URL And Tunnel

Exact app URL used:
`http://127.0.0.1:28080/web-admin/operational`

Exact tunnel path used:

```bash
ssh -o BatchMode=yes -o HostKeyAlias=datarun-app.lab -N \
  -L 28080:127.0.0.1:28080 \
  -L 28090:172.17.0.1:28090 \
  nmcp@192.168.1.213
```

The tunnel was stopped after browser and DB after-state evidence was captured.

## R12 continuity

Before PC2 mutation, R12 was inspected read-only and preserved:

- App host: `vm-datarun-app`.
- R12 app container:
  `datarun-reference-server-1|localhost:5000/datarun/server|Up 19 hours (healthy)|127.0.0.1:18080->8080/tcp, 127.0.0.1:18081->8081/tcp`.
- R12 image/revision/version:
  `localhost:5000/datarun/server@sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`,
  source revision `757d6c8d386f760693157c3e1388c877efdf6a0e`,
  version `nw067-candidate`.
- R12 readiness on `127.0.0.1:18081`: HTTP 200, `{"status":"UP"}`.
- R12 `/api/auth/me` without token: HTTP 401.
- R12 `/web-admin/operational`: HTTP 404.
- R12 evidence root and runtime config remained present:
  `/opt/datarun-lab/evidence/NW-067-R12-2026-06-18`,
  `/opt/datarun-lab/runtime-config-nw076-g2`.
- DB1 host `vm-datarun-db-14` retained the R12 evidence root.
- Ops host `vm-datarun-ops-01` retained the R12 evidence root; `datarun-keycloak`
  and `datarun-alertmanager` were up.

After NW-144 PC2 fixture/browser work, R12 was inspected read-only again and
remained unchanged:

- R12 app container stayed healthy on the same image digest, source revision,
  version, and loopback ports.
- R12 readiness remained HTTP 200 with `{"status":"UP"}`.
- R12 `/api/auth/me` without token remained HTTP 401.
- R12 `/web-admin/operational` remained HTTP 404.
- R12 evidence root and runtime config remained present.
- DB1 host `vm-datarun-db-14` retained the R12 evidence root.
- Ops host `vm-datarun-ops-01` showed `datarun-keycloak` and
  `datarun-alertmanager` up and retained the R12 evidence root.

No R12 state, R12 Keycloak state, real users, real data, or production secrets
were mutated or used.

## PC2 Standing Before Proof

Retained isolated PC2 stack standing:

- Source path: `/home/nmcp/datarun-platform-pc2-src`.
- Source revision: `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Compose project: `datarun-pc2-nw125`.
- App port: `127.0.0.1:28080`.
- Synthetic OIDC provider port: `172.17.0.1:28090`.
- Server image revision label:
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Server image version: `nw126-pc2-synthetic`.
- PC2 readiness: HTTP 200, `{"status":"UP"}`.
- `/api/auth/me` without token: HTTP 401.
- `/web-admin/operational` without session: HTTP 302 to `/web-admin/login`.
- `/web-admin/login`: HTTP 302 to the synthetic OIDC authorization endpoint.
- OIDC discovery on the synthetic provider: HTTP 200.
- Active principal binding count for the synthetic issuer/subject/actor: `1`.
- Reviewed config granted exactly `web_admin.access` and
  `web_admin.read_scoped` to actor
  `33333333-3333-4333-8333-333333333333`.

The consumed NW-141 fixture remained resolved before NW-144:

- Unresolved conflict flags before replacement push: `0`.
- Unresolved NW-141 fixture flag count: `0`.
- Unresolved flags for the NW-141 source work: `0`.
- Replacement source/flag IDs were absent before push.

## Replacement Fixture

Owner approval was explicit in the NW-144 execution turn. The replacement
fixture used the existing accepted NW-141 assignment scope for the synthetic
actor and scoped subject, because a second one-shot initial assignment
bootstrap correctly failed closed with:

`Bootstrap authority unavailable: existing assignment state differs`

No rows were created by that failed assignment attempt.

Accepted replacement path used:

- Synthetic OIDC token path for actor
  `33333333-3333-4333-8333-333333333333`.
- Authenticated `/api/sync/push` against only the retained isolated PC2 stack.
- Push request `last_pull_watermark`: `6`.
- Push response: HTTP 200 with
  `{"accepted":2,"flags_raised":0,"duplicates":0}`.

Replacement fixture IDs:

- Synthetic context/source:
  `NW-144 synthetic replacement Single Work-Linked Attention Review fixture`.
- Synthetic organization label:
  `Example PC2 Replacement Organization`.
- Synthetic web-admin actor/designated resolver:
  `33333333-3333-4333-8333-333333333333`.
- Scoped synthetic subject:
  `14114114-2141-4141-9141-141141141141`.
- Synthetic activity: `field_visit`.
- Replacement source work event:
  `14414414-3144-4144-9144-144144144144`, sync watermark `7`,
  shape `visit/v1`, type `capture`.
- Replacement unresolved attention event:
  `14414414-4144-4144-9144-144144144144`, sync watermark `8`,
  shape `conflict_detected/v1`, category `role_stale`, source event
  `14414414-3144-4144-9144-144144144144`, designated resolver
  `33333333-3333-4333-8333-333333333333`.

Before browser proof:

- Unresolved flags overall: `1`.
- Unresolved NW-144 replacement flag count: `1`.
- Unresolved flags for the NW-144 replacement source: `1`.

## HTTP And Browser Evidence

Concrete HTTP evidence through the tunnel:

- Unauthenticated `/web-admin/operational`: HTTP 302 to
  `http://127.0.0.1:28080/web-admin/login`.
- `/web-admin/login`: HTTP 302 to `http://127.0.0.1:28090/auth?...`.
- Synthetic OIDC discovery: HTTP 200.

Browser automation:

- Command:
  `NODE_PATH=/home/hamza/.npm/_npx/c70424b36042416e/node_modules npx -y -p @playwright/test@1.46.1 playwright test nw144.spec.js --reporter=line`
- Working directory: `/tmp/nw144-pw`.
- Result: `1 passed (5.7s)`.
- Browser: cached Playwright Chromium
  `/home/hamza/.cache/ms-playwright/chromium-1129/chrome-linux/chrome`.
- Evidence JSON: `/tmp/nw144-playwright-evidence.json`.
- Screenshots:
  `/tmp/nw144-operational-before.png`,
  `/tmp/nw144-attention-before-accept.png`,
  `/tmp/nw144-operational-after-accept.png`.

Browser evidence summary:

- Synthetic OIDC login/session completed and landed at
  `http://127.0.0.1:28080/web-admin/shell`.
- Authenticated `/web-admin/operational` returned to
  `http://127.0.0.1:28080/web-admin/operational`.
- Operational before state showed the scoped subject.
- Operational before state showed `Field Visit`.
- Operational before state had `Needs review` count `1`.
- Operational before state had one review link to
  `/web-admin/operational/attention`.
- Attention review page opened at
  `http://127.0.0.1:28080/web-admin/operational/attention`.
- Attention review page showed `Source Work`.
- Resolver standing matched:
  `You are the assigned reviewer for this item.`
- Resolution form was present for the exact designated resolver session.
- `Accept` button count: `1`.
- `Reject` button count: `1`.
- Attention token hidden input count: `1`.

Submitted decision:

- Decision: `Accept`.
- Reason: `NW-144 owner-approved synthetic Accept`.
- Browser clicked the real form button:
  `button[name="resolution"][value="accepted"]`.

Browser after state:

- Redirected to `http://127.0.0.1:28080/web-admin/operational`.
- Success message appeared:
  `Review recorded. The item is resolved.`
- `Needs review` count: `0`.
- Review link count: `0`.

## Post-Submit DB Evidence

Canonical accepted resolution event:

- Resolution event:
  `cf0e6b52-06cb-4134-a548-6f1c29cc6956`.
- Sync watermark: `10`.
- Shape: `conflict_resolved/v1`.
- Actor: `33333333-3333-4333-8333-333333333333`.
- Resolution: `accepted`.
- Flag event:
  `14414414-4144-4144-9144-144144144144`.
- Source event:
  `14414414-3144-4144-9144-144144144144`.
- Reason: `NW-144 owner-approved synthetic Accept`.

After browser proof:

- Unresolved NW-144 replacement flag count: `0`.
- Unresolved flags for the NW-144 replacement source: `0`.
- Authenticated operational page showed zero synthetic-actor `Needs review`
  cues.

## Friction Or Blocker

Friction recorded, non-blocking for the NW-144 replacement proof:

- The first attempt to create a new assignment via one-shot
  `assignment-bootstrap` failed closed because the PC2 stack already had
  assignment state. No rows were created by that attempt.
- The accepted replacement path therefore reused the existing accepted
  synthetic assignment scope and created only one replacement unresolved
  synthetic-actor attention item.
- After the replacement proof, one unrelated unresolved detector flag existed:
  `3ebd24d2-24a6-3732-904d-247ba4f9fad4`, sync watermark `9`,
  category `concurrent_state_change`, source event
  `14114114-3141-4141-9141-141141141141`, designated resolver
  `system:resolver_unassigned/concurrent_state_change`.
- That flag is against the old consumed NW-141 source work, not the NW-144
  replacement source, and it did not appear as a synthetic-actor
  `/web-admin/operational` `Needs review` cue.
- It was not cleared in NW-144 because the designated resolver is not the
  synthetic web-admin actor and NW-144 forbids resolver bypasses, direct DB
  mutation, and runtime implementation changes.

No browser blocker remains for the replacement PC2 proof.

## Retained And Cleanup State

- Retained PC2 compose project: `datarun-pc2-nw125`.
- Retained app port: `127.0.0.1:28080`.
- Retained synthetic OIDC provider port: `172.17.0.1:28090`.
- Retained NW-144 remote evidence under `/home/nmcp/datarun-pc2-nw144`.
- Retained NW-144 replacement source, flag, and accepted-resolution events in
  the isolated PC2 DB.
- Retained unrelated unassigned detector flag
  `3ebd24d2-24a6-3732-904d-247ba4f9fad4` for successor reconciliation.
- The local two-port tunnel was stopped after evidence capture.
- Transient browser evidence remains in `/tmp`.
- No R12 cleanup was needed because R12 was not mutated.

## Next Route

Exactly one next route is selected:

`NW-145 - Reconcile post-PC2 live proof standing and select next route`

## Validation

- `git diff --check` passed.
- `rg "NW-144" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md` passed.
- `test -f docs/agent-working-surface/artifacts/NW-144-pc2-replacement-fixture-live-browser-proof.md` passed.
- `grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-144-pc2-replacement-fixture-live-browser-proof.md` passed.
- Runtime automated tests skipped because NW-144 changed no local runtime code,
  tests, contracts, schemas, migrations, CI behavior, validation policy,
  product or platform specs, BAR, CDL, gap register, mobile code, or
  server/web-admin implementation.
