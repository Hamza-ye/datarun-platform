# NW-139 PC2 Synthetic Web-Admin Session Provisioning

Status: non-authoritative environment-preparation evidence

Date: 2026-06-22

## Result

Synthetic web-admin session/provisioning path status: `READY`.

NW-139 preserved R12, configured an isolated synthetic OIDC provider for the
retained PC2 stack, applied accepted deployment-managed provisioning commands
for a synthetic web-admin principal binding and reviewed config, and validated
the web-admin session path with HTTP probes only. No live browser walkthrough
proof was run.

Exactly one next route is selected: NW-140, a separate PC2 live browser
walkthrough proof route.

## Access

Fixed-IP SSH remained available through the retained host-key alias:

```bash
ssh -o BatchMode=yes -o HostKeyAlias=datarun-app.lab nmcp@192.168.1.213
```

Future browser-proof tunnel must include both the app and synthetic OIDC
provider ports:

```bash
ssh -o HostKeyAlias=datarun-app.lab -N \
  -L 28080:127.0.0.1:28080 \
  -L 28090:172.17.0.1:28090 \
  nmcp@192.168.1.213
```

Future app URL:
`http://127.0.0.1:28080/web-admin/operational`.

The synthetic OIDC authorization endpoint is intentionally addressed through
the operator tunnel as `http://127.0.0.1:28090/auth`. On the app host it is
served by an isolated NW-139 provider bound to Docker host-gateway address
`172.17.0.1:28090`, not a public interface.

## R12 Continuity

R12 continuity was inspected before touching retained PC2 state and again
after NW-139 changes.

Before NW-139 PC2 changes:

- App host `vm-datarun-app` was reachable by fixed IP.
- R12 app container `datarun-reference-server-1` remained up and healthy.
- R12 app image remained
  `localhost:5000/datarun/server@sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`.
- R12 source revision label remained
  `757d6c8d386f760693157c3e1388c877efdf6a0e`.
- R12 ports remained `127.0.0.1:18080->8080/tcp` and
  `127.0.0.1:18081->8081/tcp`.
- R12 readiness and liveness were HTTP 200 with `{"status":"UP"}`.
- R12 `/api/auth/me` without token remained HTTP 401.
- R12 `/web-admin/operational` remained HTTP 404.
- R12 evidence root `/opt/datarun-lab/evidence/NW-067-R12-2026-06-18`
  and runtime config `/opt/datarun-lab/runtime-config-nw076-g2` remained
  present.
- DB1 evidence remained unchanged: event count 8, max sync watermark 9,
  config version 1, four principal bindings total, two active principal
  bindings, Flyway V1-V10, encrypted recovery point
  `20260617-022519F_20260618-131808D`, RPO met, and clean DB secret scan.
- Ops/Keycloak evidence remained unchanged: `datarun-keycloak` and
  Alertmanager stayed up, active/critical backup alerts were empty, R12 token
  cleanup still reported `exists_after_cleanup=false`, and secret scan was
  clean.

After NW-139 PC2 changes:

- R12 app container, image revision/digest, ports, readiness/liveness,
  unauthenticated `/api/auth/me` standing, and `/web-admin/operational` 404
  standing remained unchanged.
- R12 Keycloak containers remained up on the ops host and were not mutated.
- R12 DB1 evidence, monitoring evidence, backup/recovery evidence, runtime
  config, token cleanup standing, and retained evidence roots were not
  changed.

## Retained PC2 State

Retained PC2 stack after NW-139:

- Source path: `/home/nmcp/datarun-platform-pc2-src`.
- Source revision:
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Compose project: `datarun-pc2-nw125`.
- Compose config files now include:
  - `/home/nmcp/datarun-platform-pc2-src/docker-compose.yml`;
  - `/home/nmcp/datarun-platform-pc2-src/nw125-compose.override.yml`;
  - `/home/nmcp/datarun-pc2-nw139/compose.oidc.override.yml`.
- Server image: `datarun-pc2-nw125-server:latest`.
- Server image revision label:
  `75880aafd346d06d4439b037b29d0d193a02f7ec`.
- Server image version label: `nw126-pc2-synthetic`.
- Containers:
  - `datarun-pc2-nw125-db-1`, healthy, `127.0.0.1:25432->5432/tcp`;
  - `datarun-pc2-nw125-server-1`, up, `127.0.0.1:28080->8080/tcp`.
- Retained PC2 volume: `datarun-pc2-nw125_pgdata`.
- Retained PC2 network: `datarun-pc2-nw125_default`.

Additional isolated NW-139 state retained on the app host:

- Base path: `/home/nmcp/datarun-pc2-nw139`.
- Synthetic OIDC provider script:
  `/home/nmcp/datarun-pc2-nw139/oidc/oidc_provider.py`.
- Synthetic OIDC signing key:
  `/home/nmcp/datarun-pc2-nw139/oidc/private.pem`, mode `600`.
- Provider PID file:
  `/home/nmcp/datarun-pc2-nw139/oidc/provider.pid`.
- Provider bind host:
  `/home/nmcp/datarun-pc2-nw139/pc2-oidc-bind-host.txt` with
  `172.17.0.1`.
- Provisioning inputs:
  `/home/nmcp/datarun-pc2-nw139/provisioning/principal-bindings.json` and
  `/home/nmcp/datarun-pc2-nw139/provisioning/reviewed-config.json`.
- Evidence files:
  `/home/nmcp/datarun-pc2-nw139/evidence/`.

The synthetic client secret and signing key are lab-only synthetic materials,
not real production secrets. Transient session cookies and authorization codes
were removed from retained evidence.

## OIDC And Session Standing

`/web-admin/login` now starts a configured synthetic OIDC flow: `YES`.

Unauthenticated final endpoint standing:

| Endpoint | Result | Standing |
|---|---:|---|
| `/actuator/health/readiness` | HTTP 200 | ready |
| `/api/auth/me` without token | HTTP 401 | protected |
| `/web-admin/operational` without session | HTTP 302 to `/web-admin/login` | protected route reachable |
| `/web-admin/login` | HTTP 302 to `http://127.0.0.1:28090/auth?...` | synthetic OIDC flow starts |

HTTP-only session validation:

- `GET /web-admin/login` returned HTTP 302 to the configured synthetic OIDC
  authorization endpoint with `response_type=code`, `scope=openid`,
  `client_id=datarun-web-admin`, configured redirect URI, state, and nonce.
- Synthetic provider authorization returned HTTP 302 to
  `/web-admin/oidc/callback`.
- `/web-admin/oidc/callback` returned HTTP 303 to `/web-admin/shell`.
- Authenticated `/web-admin/shell` returned HTTP 200.
- Authenticated `/web-admin/operational` returned HTTP 200.

This proves the synthetic session/provisioning path is available for a later
browser walkthrough. It does not claim the PC2 live browser proof has been run.

## Principal Binding

Synthetic principal binding exists: `YES`.

Accepted one-shot provisioning command:

```text
datarun.ops.command=principal-bindings
datarun.ops.input=/run/datarun/provisioning/principal-bindings.json
datarun.ops.operator-id=13913913-9139-4139-9139-139139139139
datarun.ops.evidence-id=NW-139/pc2/principal-bindings
```

Result:

- `status=succeeded`.
- `applied_operations=1`.
- `skipped_operations=0`.
- `changed_operations=1`.
- `input_sha256=1e29f02192c2a13ee4447ecb584f813320b7b4e907beccb9e1199d362df4665b`.

Bound synthetic principal:

- issuer: `http://127.0.0.1:28090`;
- subject: `pc2-synthetic-web-admin`;
- actor: `33333333-3333-4333-8333-333333333333`;
- state: `active`.

The command required `--spring.main.lazy-initialization=true` together with
`--spring.main.web-application-type=none` because current `main` otherwise
eagerly initializes the web-admin security filter-chain bean in non-web
command mode. This is a command invocation adjustment only; no runtime code or
build tooling changed.

## Reviewed Config

Reviewed config grants only needed synthetic web-admin commands: `YES`.

Accepted one-shot provisioning command:

```text
datarun.ops.command=config-publish
datarun.ops.input=/run/datarun/provisioning/reviewed-config.json
datarun.ops.operator-id=13913913-9139-4139-9139-139139139139
datarun.ops.evidence-id=NW-139/pc2/reviewed-config
```

Result:

- `status=succeeded`.
- `config_version=1`.
- `published=true`.
- `changed_authoring_rows=3`.
- `input_sha256=707faff3e1436eb9daa985abff4220bf70612e003597d6082ef7971fedf6bb5a`.

`deployment_config.admin_command_capabilities` contains exactly:

```json
{
  "schema_version": 1,
  "actors": {
    "33333333-3333-4333-8333-333333333333": [
      "web_admin.access",
      "web_admin.read_scoped"
    ]
  }
}
```

No config-admin, assignment-admin, resolver, reporting/export, queue/list,
automation, tenant/control-plane, or all-PC authority was granted.

## Cleanup

Retained intentionally:

- isolated PC2 app/DB containers, network, image, and DB volume;
- isolated synthetic OIDC provider process and files under
  `/home/nmcp/datarun-pc2-nw139`;
- isolated PC2 provisioning input and evidence files;
- isolated PC2 OIDC runtime override
  `/home/nmcp/datarun-pc2-nw139/compose.oidc.override.yml`.

Cleaned:

- transient session cookie evidence;
- raw authorization-code redirect evidence.

Cleanup proof:

- `session-cookie.txt=absent`;
- `auth-headers.txt=absent`;
- `provider-authorize.status=absent`;
- raw authorization-code scan: `no_matches`;
- synthetic client-secret scan in retained evidence: `no_matches`.

## Boundaries Preserved

- No PC2 live browser walkthrough proof was run.
- No R12 container, runtime config, evidence root, monitoring evidence,
  backup/recovery evidence, token-cleanup standing, or Keycloak state was
  mutated.
- No R12 Keycloak realm/client/user state was repurposed.
- No real users, real organizational data, customer data, or real production
  secrets were used.
- No production approval was granted.
- No auth bypass or dev-login shortcut was added.
- No app runtime code, Dockerfile/build tooling, schemas, tests, CI,
  product/platform specs, BAR, CDL, gap register, reporting/export,
  queue/list review, automation, tenant/control-plane, mobile code,
  server feature implementation, or all-PC proof changed.

## Next Route

Selected next route: NW-140 - Run PC2 live browser walkthrough proof.

NW-140 must be separate from NW-139. It should use the retained isolated PC2
stack and the two-port tunnel, run only the PC2 browser walkthrough proof, and
continue to avoid R12 mutation, real users/data, production approval,
runtime/build/schema/test/CI/spec changes, reporting/export, queue/list
expansion, automation, tenant/control-plane work, and all-PC proof.

## Validation

Manual/ops evidence:

- R12 app, DB1, and ops/Keycloak continuity inspected before PC2 changes.
- Retained PC2 stack matched NW-126 before changes.
- Synthetic OIDC provider reached by host and PC2 server container path.
- One-shot provisioning command for principal bindings succeeded.
- One-shot provisioning command for reviewed config succeeded.
- PC2 server recreated with OIDC override and returned readiness HTTP 200.
- `/web-admin/login` returned HTTP 302 to the synthetic OIDC flow.
- HTTP-only callback validation created a web-admin session.
- Authenticated `/web-admin/shell` and `/web-admin/operational` returned
  HTTP 200.
- R12 app, DB1 evidence, and ops/Keycloak standing rechecked after PC2
  changes.

Runtime automated tests were skipped because NW-139 changed local docs and
isolated lab runtime/provisioning state only. No app runtime code, tests,
contracts, schemas, migrations, CI behavior, validation policy,
product/platform specs, BAR, CDL, gap register, mobile code,
server/web-admin implementation, real users/data, or production standing
changed.
