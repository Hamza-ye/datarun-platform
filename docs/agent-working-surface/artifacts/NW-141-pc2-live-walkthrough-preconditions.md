# NW-141 PC2 Live Walkthrough Preconditions

Status: non-authoritative product-validation / environment-preparation artifact

Date: 2026-06-23

## Classification

`READY`

The retained isolated PC2 lab stack now has exactly one visible, scoped,
synthetic work-linked attention item for the synthetic web-admin actor, and a
browser-capable execution path works through the required two-port tunnel.

Exact app URL used:
`http://127.0.0.1:28080/web-admin/operational`

Exact tunnel path used:

```bash
ssh -o HostKeyAlias=datarun-app.lab -N \
  -L 28080:127.0.0.1:28080 \
  -L 28090:172.17.0.1:28090 \
  nmcp@192.168.1.213
```

## R12 continuity

Before touching PC2 state, R12 was inspected read-only and preserved:

- App host: `vm-datarun-app`.
- R12 app container:
  `datarun-reference-server-1|localhost:5000/datarun/server|Up 17 hours (healthy)|127.0.0.1:18080->8080/tcp, 127.0.0.1:18081->8081/tcp`.
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

After PC2 fixture/browser work, R12 was inspected read-only again and remained
unchanged:

- App host R12 app container stayed healthy on the same image digest, source
  revision, version, and loopback ports.
- R12 readiness remained HTTP 200 with `{"status":"UP"}`.
- R12 `/api/auth/me` without token remained HTTP 401.
- R12 `/web-admin/operational` remained HTTP 404.
- R12 evidence root and runtime config remained present.
- DB1 host `vm-datarun-db-14` retained the R12 evidence root.
- Ops host `vm-datarun-ops-01` showed `datarun-keycloak` and
  `datarun-alertmanager` up and retained the R12 evidence root.

No R12 state, R12 Keycloak state, real users, real data, or production secrets
were mutated or used.

## Retained PC2 Stack Standing

The retained isolated PC2 stack still matched NW-139 before fixture work:

- Source path: `/home/nmcp/datarun-platform-pc2-src`.
- Source revision: `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Compose project: `datarun-pc2-nw125`.
- App port: `127.0.0.1:28080`.
- Synthetic OIDC provider port: `172.17.0.1:28090`.
- Server image revision label:
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Server image version: `nw126-pc2-synthetic`.
- `/api/auth/me` without token: HTTP 401.
- `/web-admin/operational` without session: HTTP 302 to `/web-admin/login`.
- `/web-admin/login`: HTTP 302 to the synthetic OIDC authorization endpoint.
- OIDC discovery on the synthetic provider: HTTP 200.
- Active principal binding:
  issuer `http://127.0.0.1:28090`,
  subject `pc2-synthetic-web-admin`,
  actor `33333333-3333-4333-8333-333333333333`.
- Reviewed config granted exactly `web_admin.access` and
  `web_admin.read_scoped` to that synthetic actor.

## Fixture Standing

The one synthetic PC2 fixture is scoped to the retained isolated PC2 stack only:

- Synthetic context/source: `NW-141:pc2-live-walkthrough-fixture`.
- Synthetic web-admin actor/designated resolver:
  `33333333-3333-4333-8333-333333333333`.
- Synthetic subject:
  `14114114-2141-4141-9141-141141141141`.
- Synthetic activity: `field_visit`.
- Assignment-bootstrap event:
  `2e18d569-47ff-4bca-acd8-4a002f7aa75f`.
- Source work event:
  `14114114-3141-4141-9141-141141141141`, shape `visit/v1`,
  type `capture`, activity `field_visit`.
- Unresolved work-linked attention event:
  `14114114-4141-4141-9141-141141141141`, shape `conflict_detected/v1`,
  category `role_stale`, source event
  `14114114-3141-4141-9141-141141141141`, designated resolver
  `33333333-3333-4333-8333-333333333333`.

Accepted paths used:

- `assignment-bootstrap` one-shot provisioning created the scoped assignment
  with evidence id `NW-141/pc2/assignment-bootstrap`.
- The synthetic OIDC token path and authenticated `/api/sync/push` added the
  synthetic source work and manual attention event.
- The sync push returned HTTP 200 with `{"accepted":2,"flags_raised":1,"duplicates":0}`.
- The generated detector flag
  `21d8d6b3-e9b3-327e-a4f2-5d19044e9101` was resolved through the accepted
  bearer-bound `/api/conflicts/{flag_id}/resolve` API as the exact designated
  synthetic actor, returning HTTP 200 and resolution event
  `f5c57b88-c285-46c1-b40b-52d8618dc1e5`.

Post-cleanup database standing:

- Unresolved conflict flags: `1`.
- Unresolved flags for fixture source work: `1`.
- The one unresolved flag is the manual synthetic fixture attention event
  `14114114-4141-4141-9141-141141141141`.

## HTTP And Browser Evidence

Concrete HTTP evidence through the two-port tunnel:

- Unauthenticated `/web-admin/operational`: HTTP 302 to
  `http://127.0.0.1:28080/web-admin/login`.
- Synthetic OIDC login/session: HTTP 200 final page with `JSESSIONID`;
  login flow ended at `http://127.0.0.1:28080/web-admin/shell`.
- Authenticated `/web-admin/operational`: HTTP 200.
- Authenticated operational page showed the scoped subject
  `14114114-2141-4141-9141-141141141141`.
- Authenticated operational page showed activity `Field Visit`.
- Authenticated operational page showed `Needs review`.
- Authenticated operational page showed the review link
  `/web-admin/operational/attention`.
- Authenticated attention review page returned HTTP 200.
- Attention review page showed `Source Work`.
- Resolver standing matched the synthetic actor:
  `You are the assigned reviewer for this item.`
- Resolution form was present for the designated resolver, with `Accept` and
  `Reject` controls.

Concrete browser evidence:

- Playwright Chromium was installed in the user cache after approval because
  no local system browser was available.
- Headless Chromium drove the same tunnel path and passed the browser check.
- Browser evidence file: `/tmp/nw141-playwright-evidence.json`.
- Browser screenshot: `/tmp/nw141-attention-review.png`.
- Browser result: `pass: true`.

## Walkthrough Beats Observed

Observed one-item attention review precondition path:

1. Browser login starts at `/web-admin/login` and completes through the
   synthetic NW-139 OIDC provider.
2. Authenticated session lands under `/web-admin` with a server-managed
   session cookie.
3. `/web-admin/operational` shows one scoped source work item for the synthetic
   subject and `Field Visit` activity.
4. The page shows one `Needs review` cue.
5. The `Review` link opens `/web-admin/operational/attention`.
6. The attention review page shows the same source work context.
7. The reviewer standing says the current synthetic actor is assigned.
8. The resolution form is present only for that designated resolver session.

The resolution form was not submitted in NW-141; the fixture remains available
for NW-142.

## Friction Or Blocker

No remaining NW-141 blocker.

Friction handled during NW-141:

- The first local browser standing had only a Firefox snap stub and no usable
  Chromium/Chrome binary.
- Playwright Chromium download was approved and installed to the user cache,
  then browser verification passed.
- The initial sync push generated one extra detector `role_stale` flag because
  the source event was accepted at the device knowledge horizon. That generated
  flag was resolved through the accepted conflict API by the exact designated
  synthetic actor, leaving exactly one unresolved work-linked attention item.

## Retained And Cleanup State

- Retained PC2 compose project: `datarun-pc2-nw125`.
- Retained app port: `127.0.0.1:28080`.
- Retained synthetic OIDC provider port: `172.17.0.1:28090`.
- Retained synthetic fixture source and attention state in the isolated PC2 DB.
- Retained NW-141 remote evidence under `/home/nmcp/datarun-pc2-nw141`.
- Transient browser evidence is in `/tmp`.
- The long-lived manual tunnel used during verification was stopped after the
  browser evidence passed.
- No R12 cleanup was needed because R12 was not mutated.

## Next Route

Exactly one next route is selected:

`NW-142 - Run PC2 live browser walkthrough proof`

## Validation

- `git diff --check` passed.
- `rg "NW-141" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md` passed.
- `test -f docs/agent-working-surface/artifacts/NW-141-pc2-live-walkthrough-preconditions.md` passed.
- `grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-141-pc2-live-walkthrough-preconditions.md` passed.
- Runtime automated tests skipped because NW-141 changed no runtime code, tests,
  contracts, schemas, migrations, CI behavior, validation policy, product or
  platform specs, BAR, CDL, gap register, mobile code, or server/web-admin
  implementation.
