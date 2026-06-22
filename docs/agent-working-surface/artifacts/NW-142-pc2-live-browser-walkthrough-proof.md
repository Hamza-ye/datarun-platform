# NW-142 PC2 Live Browser Walkthrough Proof

Status: non-authoritative product-validation / owner-review evidence artifact

Date: 2026-06-23

## Classification

`NOT_READY`

NW-142 stopped before the live browser proof because the retained NW-141 fixture
was no longer intact at preflight: the single synthetic unresolved attention
item had already been resolved before NW-142 could record before/after browser
state or submit the owner-approved `Accept` decision.

No replacement fixture was created. No browser proof was run. No NW-142
resolution was submitted by the agent.

Exact app URL:
`http://127.0.0.1:28080/web-admin/operational`

Tunnel path:

```bash
ssh -o HostKeyAlias=datarun-app.lab -N \
  -L 28080:127.0.0.1:28080 \
  -L 28090:172.17.0.1:28090 \
  nmcp@192.168.1.213
```

The tunnel was not established for the proof run because execution stopped
during the required PC2 fixture-integrity preflight.

## R12 continuity

Before touching PC2 state, R12 was inspected read-only and preserved:

- App host: `vm-datarun-app`.
- R12 app container:
  `datarun-reference-server-1|localhost:5000/datarun/server|Up 18 hours (healthy)|127.0.0.1:18080->8080/tcp, 127.0.0.1:18081->8081/tcp`.
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

After the stopped NW-142 preflight, R12 was inspected read-only again and
remained unchanged:

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

## PC2 Preflight Standing

The retained isolated PC2 stack still matched NW-141 in infrastructure and auth
standing:

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
- Active principal binding count for the synthetic issuer/subject/actor: `1`.
- Reviewed config granted exactly `web_admin.access` and
  `web_admin.read_scoped` to actor
  `33333333-3333-4333-8333-333333333333`.

PC2 login/session standing for NW-142:

- Synthetic OIDC login/session was not re-exercised in a browser during NW-142
  because the fixture-integrity check stopped the proof before tunnel/browser
  execution.
- The preflight confirmed the login route and OIDC provider were reachable.

PC2 `/web-admin/operational` standing for NW-142:

- Unauthenticated `/web-admin/operational` redirected to login as expected.
- Authenticated operational standing was not re-exercised in a browser during
  NW-142 because the unresolved fixture item was already cleared.

## Before/After Fixture State

Expected NW-141 fixture:

- Source work event:
  `14114114-3141-4141-9141-141141141141`, shape `visit/v1`,
  type `capture`, activity `field_visit`, subject
  `14114114-2141-4141-9141-141141141141`.
- Work-linked attention flag:
  `14114114-4141-4141-9141-141141141141`, shape `conflict_detected/v1`,
  category `role_stale`, source event
  `14114114-3141-4141-9141-141141141141`, designated resolver
  `33333333-3333-4333-8333-333333333333`.
- NW-141 cleanup resolution for generated detector flag:
  `f5c57b88-c285-46c1-b40b-52d8618dc1e5`, accepted, flag event
  `21d8d6b3-e9b3-327e-a4f2-5d19044e9101`.

Actual NW-142 preflight fixture state:

- Unresolved conflict flags: `0`.
- Unresolved NW-141 fixture flag count: `0`.
- Unresolved flags for the NW-141 source work: `0`.
- Additional accepted resolution event already existed:
  `bcafd46a-d439-4548-b51d-bc8ec9dddb09`.
- That event resolved fixture flag
  `14114114-4141-4141-9141-141141141141`.
- Resolution actor:
  `33333333-3333-4333-8333-333333333333`.
- Resolution value: `accepted`.
- Resolution reason: `ok, manual`.

After NW-142 stopped:

- The fixture remained cleared.
- No new fixture was created.
- No additional resolution was submitted by the agent.

## Walkthrough Beats Observed

NW-142 did not run the live browser walkthrough beats because the proof
requires a before-state unresolved attention item, and that item was already
resolved at preflight.

Not run in NW-142:

1. Browser login/session proof.
2. Authenticated operational page proof.
3. `Needs review` cue proof.
4. Attention review page proof.
5. Owner-approved `Accept` submission by the agent.
6. Post-submit browser proof that the cue cleared.

The DB preflight did confirm the accepted-resolution after-state, but it did
not provide the required bounded before/after browser proof for NW-142.

## Browser Evidence

No NW-142 browser evidence was produced because execution stopped before tunnel
and browser startup.

The prior NW-141 browser evidence remains precondition evidence only; it is not
counted as the NW-142 proof submission.

## Friction Or Blocker

The blocker is narrow and specific:

```text
The only retained NW-141 fixture was consumed by an accepted manual resolution
before NW-142 could record the bounded before/after live browser proof.
```

Per the owner instruction, the agent stopped instead of creating another
fixture or improvising a proof from a consumed item.

## Retained And Cleanup State

- Retained PC2 compose project: `datarun-pc2-nw125`.
- Retained app port: `127.0.0.1:28080`.
- Retained synthetic OIDC provider port: `172.17.0.1:28090`.
- Retained synthetic source work and resolution history in the isolated PC2 DB.
- The NW-141 unresolved attention item is no longer unresolved.
- No R12 cleanup was needed because R12 was not mutated.
- No NW-142 browser tunnel remained running.

## Next Route

Exactly one next route is selected:

`NW-143 - Reconcile consumed PC2 fixture and select proof retry path`

## Validation

- `git diff --check` passed.
- `rg "NW-142" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md` passed.
- `test -f docs/agent-working-surface/artifacts/NW-142-pc2-live-browser-walkthrough-proof.md` passed.
- `grep -n "R12 continuity" docs/agent-working-surface/artifacts/NW-142-pc2-live-browser-walkthrough-proof.md` passed.
- Runtime automated tests skipped because NW-142 changed no runtime code, tests,
  contracts, schemas, migrations, CI behavior, validation policy, product or
  platform specs, BAR, CDL, gap register, mobile code, or server/web-admin
  implementation.
