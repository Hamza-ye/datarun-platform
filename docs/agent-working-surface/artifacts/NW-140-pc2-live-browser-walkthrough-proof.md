# NW-140 PC2 Live Browser Walkthrough Proof

Status: non-authoritative product-validation / owner-review evidence artifact

Date: 2026-06-23

## Result

PC2 live browser walkthrough proof status: `NOT_READY`.

NW-140 confirmed the retained isolated PC2 stack still matches the NW-139
synthetic OIDC/session/provisioning path and proved authenticated
`/web-admin/operational` is reachable through the two-port tunnel. The
one-item Single Work-Linked Attention Review walkthrough could not be completed
because the retained PC2 state has no visible scoped work and no unresolved
work-linked attention item to open or resolve. The current operator environment
also has no usable browser driver: `firefox` resolves to a snap stub and
Playwright/Puppeteer/Selenium are not installed.

Exactly one next route is selected: NW-141, a bounded PC2 live-walkthrough
precondition route for one synthetic scoped work-linked attention fixture and a
browser-capable execution path.

## App URL And Tunnel Path Used

App URL used:

```text
http://127.0.0.1:28080/web-admin/operational
```

Tunnel path used:

```bash
ssh -o BatchMode=yes -o HostKeyAlias=datarun-app.lab \
  -o ExitOnForwardFailure=yes -N \
  -L 28080:127.0.0.1:28080 \
  -L 28090:172.17.0.1:28090 \
  nmcp@192.168.1.213
```

This is the NW-140 two-port tunnel with `BatchMode` and
`ExitOnForwardFailure` added for bounded non-interactive proof execution.

## R12 continuity

R12 continuity before PC2 access:

- App host `vm-datarun-app` was reachable by fixed IP.
- R12 container `datarun-reference-server-1` remained up and healthy.
- R12 app image digest remained
  `sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`.
- R12 source revision label remained
  `757d6c8d386f760693157c3e1388c877efdf6a0e`.
- R12 version label remained `nw067-candidate`.
- R12 ports remained `127.0.0.1:18080->8080/tcp` and
  `127.0.0.1:18081->8081/tcp`.
- R12 readiness and liveness on management port `18081` returned HTTP 200
  with `{"status":"UP"}`.
- R12 `/api/auth/me` without token returned HTTP 401.
- R12 `/web-admin/operational` returned HTTP 404.
- R12 evidence root `/opt/datarun-lab/evidence/NW-067-R12-2026-06-18`
  and runtime config `/opt/datarun-lab/runtime-config-nw076-g2` remained
  present.
- DB1 host `vm-datarun-db-14` retained the R12 evidence root.
- Ops host `vm-datarun-ops-01` retained the R12 evidence root; Keycloak
  `datarun-keycloak` and Alertmanager `datarun-alertmanager` remained up.

R12 continuity after PC2 access:

- R12 container, image digest, source revision, version label, and loopback
  ports were unchanged.
- R12 readiness and liveness still returned HTTP 200 with `{"status":"UP"}`.
- R12 `/api/auth/me` without token still returned HTTP 401.
- R12 `/web-admin/operational` still returned HTTP 404.
- R12 evidence/config roots remained present on the app host; DB1 and ops
  evidence roots remained present; Keycloak and Alertmanager remained up.

No R12 container, R12 runtime config, R12 Keycloak state, R12 DB state,
monitoring evidence, backup/recovery evidence, or R12 product standing was
intentionally changed.

## Retained PC2 Stack Confirmation

Retained PC2 standing matched NW-139:

- Source path: `/home/nmcp/datarun-platform-pc2-src`.
- Source revision:
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Branch: `main`; working tree had only the expected untracked
  `nw125-compose.override.yml`.
- Compose project: `datarun-pc2-nw125`.
- Containers:
  - `datarun-pc2-nw125-server-1`, up, `127.0.0.1:28080->8080/tcp`;
  - `datarun-pc2-nw125-db-1`, healthy, `127.0.0.1:25432->5432/tcp`.
- Server image ID:
  `sha256:2264cd5ba83f5def35d33defd3b2b5812f84e6dae10abd9931d80b9ba3469b23`.
- Server image revision label:
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Server image version label: `nw126-pc2-synthetic`.
- Synthetic OIDC provider was listening on `172.17.0.1:28090`.
- Synthetic provider PID `38960` was running
  `/home/nmcp/datarun-pc2-nw139/oidc/oidc_provider.py`.
- Provider bind-host file contained `172.17.0.1`.

Principal binding input still contains one active binding:

```text
issuer=http://127.0.0.1:28090
subject=pc2-synthetic-web-admin
actor=33333333-3333-4333-8333-333333333333
```

Reviewed config still grants exactly:

```text
web_admin.access
web_admin.read_scoped
```

No config-admin, assignment-admin, resolver, reporting/export, queue/list,
automation, tenant/control-plane, or all-PC authority was observed in the
reviewed config.

## PC2 Login And Session Standing

Pre-session probes:

- `GET /actuator/health/readiness` through app port `28080`: HTTP 200.
- `GET /api/auth/me` without token: HTTP 401.
- `GET /web-admin/operational` without session: HTTP 302 to
  `http://127.0.0.1:28080/web-admin/login`.
- `GET /web-admin/login`: HTTP 302 to
  `http://127.0.0.1:28090/auth?...`.
- `GET /.well-known/openid-configuration` through provider port `28090`:
  HTTP 200.

Authenticated tunneled session standing:

- Initial `GET http://127.0.0.1:28080/web-admin/operational` redirected
  through `/web-admin/login`, the synthetic OIDC `/auth` endpoint, and
  `/web-admin/oidc/callback`.
- The callback returned HTTP 303 to `/web-admin/shell`.
- The final shell response was HTTP 200.
- The session received `JSESSIONID`.
- Authenticated `GET /web-admin/operational` returned HTTP 200.

This confirms the NW-139 synthetic web-admin login/session path remains
available. It does not prove the one-item attention review journey.

## PC2 `/web-admin/operational` Standing

Authenticated `/web-admin/operational` returned HTTP 200 with page title
`Datarun Operational View`.

Observed headings:

- `Operational View`
- `Latest Synced Work`
- `Freshness`

Observed links:

- `Operational Responsibility Handoff` to `/web-admin/operational/handoff`
- `Scoped Operational Report Snapshot` to `/web-admin/operational/report`
- `Admin Shell` to `/web-admin/shell`

Observed body standing:

```text
No scoped synced work is visible yet.
No scoped synced work is visible yet.
```

Observed forms: none.

Observed attention/review standing:

- `Needs review`: absent.
- `Attention`: absent.
- attention/review links: absent.
- resolution form: absent.
- `Accept` / `Reject` actions: absent.

## Walkthrough Beats Observed

| Beat | Standing | Evidence |
|---|---|---|
| Establish the two-port tunnel for app and synthetic OIDC provider. | PASS | Local tunneled readiness returned HTTP 200 and OIDC discovery returned HTTP 200. |
| Start at the exact app URL. | PASS | `GET /web-admin/operational` through the tunnel started the web-admin/OIDC flow. |
| Use the synthetic NW-139 OIDC path and synthetic web-admin actor. | PASS | Flow used issuer `http://127.0.0.1:28090` and session actor `33333333-3333-4333-8333-333333333333`. |
| Reach an authenticated web-admin session. | PASS | Final shell response HTTP 200 with `JSESSIONID`. |
| Reach authenticated `/web-admin/operational`. | PASS | Authenticated operational response HTTP 200. |
| See one visible scoped source work item. | NOT_READY | Page says no scoped synced work is visible yet. |
| See one work-linked `Needs review` attention cue. | NOT_READY | No `Needs review` text, attention link, or attention form was present. |
| Open one attention review page from the cue. | NOT_READY | No attention link existed to open. |
| Inspect source-work and attention context. | NOT_READY | No review page existed for the retained PC2 state. |
| Make one manual accept/reject decision as the designated reviewer. | NOT_READY | No resolution form or action existed for the retained PC2 state. |
| Confirm the item clears from unresolved attention after decision. | NOT_READY | No decision was possible. |

## Friction And Blocker

Primary blocker: the retained isolated PC2 stack has the NW-139
login/session/provisioning path, but it does not contain one visible scoped
work item with one unresolved work-linked attention cue for the synthetic web
admin actor.

Secondary proof friction: the current operator environment does not have a
usable browser automation driver. `firefox` is present only as a snap stub that
prints `Command '/usr/bin/firefox' requires the firefox snap to be installed`;
Playwright, Puppeteer, Selenium, and Playwright Core are not installed. The
authenticated proof therefore used HTTP session traversal and page inspection,
not a completed real browser click-through.

## Retained And Cleanup State

Retained:

- isolated PC2 app/DB containers, network, image, and DB volume;
- isolated NW-139 synthetic OIDC provider process and files;
- isolated PC2 provisioning input and evidence files;
- R12 reference deployment state.

Cleaned/closed:

- the NW-140 two-port SSH tunnel commands exited after probes;
- no raw authorization code, session cookie, or screenshot evidence was added
  to repository files;
- local `/tmp` browser/profile scratch was not retained as proof evidence.

Not cleaned by mutation:

- synthetic web-admin sessions created during HTTP probes were not deleted from
  the PC2 app because deleting server-side session state would be an extra
  environment mutation outside the proof. They are transient PC2 session state,
  not R12 state.

## Selected Next Route

Selected next route:

```text
NW-141 - Prepare PC2 live walkthrough fixture and browser path
```

NW-141 should stay inside the retained isolated PC2 lab stack and either prove
one synthetic scoped work-linked attention fixture plus a browser-capable
execution path, or classify the proof preconditions as still not ready with
the exact blocker.

NW-141 must not mutate R12, repurpose R12 Keycloak, use real users/data,
approve production, add auth bypass/dev-login, change runtime app code,
Dockerfile/build tooling, schemas, tests, CI, product/platform specs, BAR,
CDL, gap register, reporting/export, queue/list review, automation,
tenant/control-plane, mobile code, server feature implementation, or all-PC
proof.

## Validation Category

Docs-only product-validation / owner-review evidence plus bounded manual/ops
lab inspection.

Runtime automated tests are skipped because NW-140 changes only working-surface
evidence, status/backlog trace, artifact indexing, and one successor prompt.
It changes no runtime code, tests, contracts, schemas, migrations, CI behavior,
validation policy, product/platform behavior, BAR, CDL, gap-register
classifications, mobile code, server code, or web-admin implementation.
