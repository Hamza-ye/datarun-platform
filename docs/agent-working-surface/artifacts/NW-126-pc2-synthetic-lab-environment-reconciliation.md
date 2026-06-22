# NW-126 PC2 Synthetic Lab Environment Reconciliation

Status: non-authoritative environment-reconciliation evidence

Date: 2026-06-22

## Result

PC2 environment status: `NOT_READY`.

NW-126 unblocked lab access, inspected the R12 reference deployment first,
reconciled retained NW-125 PC2 state, completed the isolated PC2 image/build
from current `main`, and retained a loopback-only synthetic PC2 stack.
However, the environment is not yet suitable for live browser proof because no
synthetic web-admin browser session/provisioning path exists. The current PC2
server has no web-admin OIDC browser-login configuration, and
`GET /web-admin/login` fails closed with `oidc_issuer_not_configured`.

Exactly one next route is selected: NW-139, a bounded PC2 synthetic
web-admin auth/session/provisioning environment route. Do not run PC2 live
browser proof until NW-139 or another explicitly selected route proves a
synthetic web-admin session path.

## Access

- Local DNS for `datarun-app.lab` and `datarun-app.lab.nmcpye` did not resolve
  from this operator environment during NW-126.
- Fixed-IP SSH worked with the retained host key alias:
  `ssh -o BatchMode=yes -o HostKeyAlias=datarun-app.lab nmcp@192.168.1.213`.
- App host hostname: `vm-datarun-app`.
- DB1 fixed-IP SSH worked with
  `ssh -o BatchMode=yes -o HostKeyAlias=datarun-db1.lab nmcp@192.168.1.214`.
- Ops/Keycloak fixed-IP SSH worked with
  `ssh -o BatchMode=yes -o HostKeyAlias=keycloak.lab nmcp@192.168.1.217`.

Future operator tunnel path for the retained PC2 stack:

```bash
ssh -o HostKeyAlias=datarun-app.lab -N -L 28080:127.0.0.1:28080 nmcp@192.168.1.213
```

If lab DNS is working for the operator, the equivalent host form is:

```bash
ssh -N -L 28080:127.0.0.1:28080 nmcp@datarun-app.lab
```

Future app URL after tunneling:
`http://127.0.0.1:28080/web-admin/operational`.

## R12 Reference Deployment

R12 reference deployment inspection was completed before any retained PC2 state
was touched.

Observed app host standing:

- Compose project: `datarun-reference`.
- Container: `datarun-reference-server-1`.
- Image: `localhost:5000/datarun/server@sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`.
- Source revision label: `757d6c8d386f760693157c3e1388c877efdf6a0e`.
- Version label: `nw067-candidate`.
- Ports: `127.0.0.1:18080->8080/tcp` and
  `127.0.0.1:18081->8081/tcp`.
- Health: readiness `{"status":"UP"}` HTTP 200; liveness
  `{"status":"UP"}` HTTP 200.
- Protected endpoint standing: `/api/auth/me` HTTP 401 without a token;
  `/api/sync/config` HTTP 401 without a token; `/api/sync/pull` HTTP 401
  without a token.
- PC2 route standing on R12: `/web-admin/operational` HTTP 404 because the R12
  image predates PC2.

Preserved evidence/config standing:

- `/opt/datarun-lab/evidence/NW-067-R12-2026-06-18` remains the R12 evidence
  root.
- `/opt/datarun-lab/runtime-config-nw076-g2` remains retained and was not
  changed.
- App evidence still includes R12 readiness/liveness, reference server image
  and container evidence, monitoring summaries, protected-smoke evidence, and
  token-cleanup evidence.
- Monitoring evidence remained clean: Prometheus ready, R12 targets up, no
  firing alerts, no critical backup alerts, and final secret scan clean.
- DB1 evidence remained clean: Flyway V1-V10, event count 8, max sync
  watermark 9, config package version 1, four principal bindings total, two
  active principal bindings, encrypted pgBackRest recovery point
  `20260617-022519F_20260618-131808D`, RPO met, and DB secret scan clean.
- Ops evidence remained clean: Alertmanager active and critical backup alert
  summaries empty, R12 protected smoke passed, token cleanup reported
  `exists_after_cleanup=false`, and ops secret scan clean.

Final R12 continuity check after PC2 work:

- `datarun-reference-server-1` remained up and healthy on the same image and
  loopback ports.
- Readiness and liveness remained HTTP 200.
- `/web-admin/operational` remained HTTP 404 on R12.
- `/api/auth/me` without token remained HTTP 401.

No R12 evidence, runtime config, monitoring evidence, DB recovery/backup
evidence, token cleanup standing, Keycloak realm/client/user state, or R12
container state was mutated.

## Retained PC2 State

Initial retained NW-125 state:

- Source path existed: `/home/nmcp/datarun-platform-pc2-src`.
- Source HEAD before continuation:
  `9dee62d8019d13076ce359db3be999d4db916200`.
- Branch: `main`.
- Working tree: clean except untracked `nw125-compose.override.yml`.
- Existing override initially published:
  `127.0.0.1:25432:5432` for DB and `127.0.0.1:28080:8080` for app, but the
  base compose file also published broad `0.0.0.0:5432` and `0.0.0.0:8080`
  listeners until NW-126 corrected the override.
- Compose project `datarun-pc2-nw125`: no retained containers before
  continuation.
- Related PC2 volumes/networks: none before continuation.
- Port `127.0.0.1:28080`: no listener before continuation.
- Initial probe to `127.0.0.1:28080`: connection refused.

NW-126 continuation:

- Fetched and fast-forwarded the isolated PC2 source to current local `main`
  after PR #54 merge:
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Built `datarun-pc2-nw125-server:latest`.
- Build completed successfully. Maven `dependency:go-offline` and
  `./mvnw package -DskipTests -B` both reported `BUILD SUCCESS`.
- Started isolated compose project `datarun-pc2-nw125`.
- Corrected the isolated override to use Compose list override syntax so only
  loopback ports are retained:
  - DB: `127.0.0.1:25432->5432/tcp`.
  - Server: `127.0.0.1:28080->8080/tcp`.

Final retained PC2 state:

- Source path: `/home/nmcp/datarun-platform-pc2-src`.
- Source HEAD: `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Compose project: `datarun-pc2-nw125`.
- Containers:
  - `datarun-pc2-nw125-db-1`, image
    `ghcr.io/vadosware/pg_idkit:0.2.3-pg16.2-alpine3.18-amd64`, healthy,
    `127.0.0.1:25432->5432/tcp`.
  - `datarun-pc2-nw125-server-1`, image
    `datarun-pc2-nw125-server`, up,
    `127.0.0.1:28080->8080/tcp`.
- Image ID:
  `sha256:2264cd5ba83f5def35d33defd3b2b5812f84e6dae10abd9931d80b9ba3469b23`.
- Image labels:
  - `org.opencontainers.image.revision=75880aafd346d06d4439b037b29d0d193a02f7ec`.
  - `org.opencontainers.image.version=nw126-pc2-synthetic`.
  - `org.opencontainers.image.source=https://github.com/Hamza-ye/datarun-platform`.
- Volume retained: `datarun-pc2-nw125_pgdata`.
- Network retained: `datarun-pc2-nw125_default`.

## PC2 Endpoint Standing

Final loopback probes on the retained PC2 stack:

| Endpoint | Result | Standing |
|---|---:|---|
| `/actuator/health/readiness` | HTTP 200, `{"status":"UP"}` | ready |
| `/actuator/health/liveness` | HTTP 200, `{"status":"UP"}` | live |
| `/api/auth/me` without token | HTTP 401, `{"error":"missing_token"}` | protected |
| `/web-admin/operational` without session | HTTP 302 to `/web-admin/login` | route reachable, session required |
| `/web-admin/login` | HTTP 500 | not proof-ready |

`/web-admin/operational` is reachable in the limited sense that current `main`
routes to the web-admin login boundary instead of returning 404. It is not
ready for live browser proof because login initiation fails closed without
OIDC browser-login configuration.

Observed server failure reason for `/web-admin/login`:
`AuthResolutionException: oidc_issuer_not_configured`.

## Synthetic Session And Provisioning Standing

Synthetic web-admin principal/session/provisioning path: `NO`.

Reasons:

- No isolated PC2 web-admin OIDC issuer, authorization endpoint, token
  endpoint, redirect URI, client id/secret, or JWKS configuration was present.
- No PC2-specific principal-binding manifest was applied to the isolated PC2
  database during NW-126.
- No PC2 reviewed config was applied to seed `web_admin.access` or
  `web_admin.read_scoped` for a synthetic web-admin actor.
- R12 Keycloak metadata was inspected read-only, but R12 Keycloak was not
  mutated or repurposed.
- R12 protected-smoke bearer-token evidence was not reused as browser-session
  proof.
- No dev-login shortcut, auth bypass, direct SQL shortcut, or app runtime code
  change was introduced.

## Cleanup And Retention

Retained synthetic state:

- `/home/nmcp/datarun-platform-pc2-src`.
- Compose project `datarun-pc2-nw125`.
- Image `datarun-pc2-nw125-server:latest`.
- Containers `datarun-pc2-nw125-db-1` and
  `datarun-pc2-nw125-server-1`.
- Volume `datarun-pc2-nw125_pgdata`.
- Network `datarun-pc2-nw125_default`.
- Loopback-only ports `127.0.0.1:25432` and `127.0.0.1:28080`.

Cleanup/completion performed:

- Corrected the isolated PC2 compose override to remove broad `0.0.0.0` host
  publishing inherited from the root `docker-compose.yml`.
- Left no direct public PC2 listener on `8080` or `5432`.
- Left R12 untouched.

## Broader Lab Observation

Observation only: because the retained PC2 image is built from current `main`,
it contains the later web-admin surfaces added after PC2, including PC3 and
PC4 routes. The environment may be a useful base for later broader PC1-PC4 lab
proof work after the PC2 auth/session/provisioning gap is resolved. NW-126
does not select all-PC proof and does not claim broader proof readiness.

## Next Route

Selected next route: NW-139 - Prepare isolated PC2 synthetic web-admin
auth/session/provisioning path.

NW-139 should stay inside the isolated `datarun-pc2-nw125` environment and
either:

- configure a synthetic OIDC/browser-login provider path plus accepted
  deployment-managed principal binding and reviewed config provisioning for a
  synthetic web-admin actor; or
- classify the auth/session/provisioning path as still `NOT_READY` with the
  exact blocker.

NW-139 must not run the PC2 live browser walkthrough proof, mutate R12,
repurpose R12 secrets, use real users/data, add auth bypass/dev-login
shortcuts, change app runtime code, change Dockerfile/build tooling, or
broaden into all-PC proof.

## Validation

Runtime automated tests were skipped because NW-126 changed local docs and
isolated lab state only. No runtime code, tests, contracts, schemas,
migrations, CI behavior, validation policy, product/platform specs, BAR, CDL,
gap register, mobile code, server/web-admin implementation, real users/data,
or production standing changed.

Required docs validation is recorded in status/backlog for the NW-126 local
documentation changes.
