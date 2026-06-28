Status: blocked
Document type: rehearsal_record
Owner: Hamza
Source: NW-181
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `deploy/reference/README.md`, `deploy/reference/provisioning-inputs.md`, and `deploy/reference/pilot-packages/field-assets/`
Last reviewed: 2026-06-28
Supersedes: NW-180 skip reason for image-command absence only
Related: `docs/agent-working-surface/platform-next-work-backlog.md`, `docs/status.md`, `deploy/reference/pilot-packages/field-assets/`

# 2026-06-28 NW-181 Local Deployment Image Refresh And Field-Assets Smoke

## Result

Final result: `blocked` for the field-assets smoke; `passed` for the image
refresh and basic deployment-alignment checks.

The intended local/on-prem app deployment now runs the current accepted server
image from `main` revision
`473741b3beff2e18b39bdf88e5fe59d9df0dfc62`. The image includes the
NW-178 `field-assets-seed` one-shot command and the server service was
recreated on that image.

The field-assets package smoke stopped before seed, mobile/API capture/sync, or
reviewer evidence. The intended deployment DB already had NW-171 assignment
events. The documented field-assets setup path requires an exact setup-owner
assignment created by `system:assignment_bootstrap/initial`, but the
`assignment-bootstrap` one-shot command only runs on an empty assignment store
or an exact single matching initial assignment. On this non-empty deployment,
that precondition cannot be satisfied without selecting a new bounded setup
route or using a clean deployment.

No candidate promotion/rejection, lifecycle, duplicate/merge/split, semantic
location/place behavior, registry/import/export, real-data import, broad
rollout, production cutover, Keycloak hardening, schema, contract, sync, or
runtime code work was performed.

## Environment

- app VM: `192.168.1.213`, hostname `vm-datarun-app`
- auth VM: `192.168.1.217`, hostname `vm-datarun-ops-01`
- public app URL: `https://app.nmcpye.org`
- public auth URL: `https://auth.nmcpye.org`
- compose project: `/home/nmcp/datarun-local-app`
- previous image:
  `datarun-local-server:local-keycloak-conditional-web-security`,
  `sha256:b340416e83980355ba9fcd69a49a9209c59658547374dc091780dce656796bbb`,
  revision
  `9c1e6eebb9f81d3648a8fdd27e18b522b61c4366+local-web-security-conditional`
- new image:
  `datarun-local-server:nw181-473741b3`,
  `sha256:2a2962579d14a568b2dfe7cc2f0c4534f08299745154051dbd81ca11c64c8506`,
  revision `473741b3beff2e18b39bdf88e5fe59d9df0dfc62`,
  source `https://github.com/Hamza-ye/datarun-platform`,
  user `10001:10001`
- rollback tag retained:
  `datarun-local-server:pre-nw181-b340416e`
- remote evidence logs:
  `/home/nmcp/datarun-local-app/evidence/NW-181-docker-build.log`,
  `/home/nmcp/datarun-local-app/evidence/NW-181-field-assets-config-publish.log`,
  `/home/nmcp/datarun-local-app/evidence/NW-181-field-assets-complete-config-publish.log`,
  `/home/nmcp/datarun-local-app/evidence/NW-181-field-assets-assignment-bootstrap.log`,
  `/home/nmcp/datarun-local-app/evidence/NW-181-field-assets-seed-precondition.log`,
  and `/home/nmcp/datarun-local-app/evidence/NW-181-server-recreate.log`

## Commands

Local branch was created from current `main` after syncing `origin/main`.

```bash
git fetch --prune origin
git switch main
git pull --ff-only origin main
git switch -c product/nw-181-refresh-local-deployment-image-smoke
git archive --format=tar --output=/tmp/datarun-nw181-473741b3.tar HEAD
scp /tmp/datarun-nw181-473741b3.tar nmcp@192.168.1.213:/home/nmcp/datarun-local-app/build/datarun-nw181-473741b3.tar
```

Remote source unpack and image build:

```bash
ssh -o BatchMode=yes nmcp@192.168.1.213 'set -eu
src=/home/nmcp/datarun-nw181-src-473741b3
mkdir -p "$src"
tar -xf /home/nmcp/datarun-local-app/build/datarun-nw181-473741b3.tar -C "$src"
image=datarun-local-server:nw181-473741b3
revision=473741b3beff2e18b39bdf88e5fe59d9df0dfc62
created=$(date -u +%Y-%m-%dT%H:%M:%SZ)
log=/home/nmcp/datarun-local-app/evidence/NW-181-docker-build.log
docker build -f "$src/server/Dockerfile" -t "$image" \
  --build-arg IMAGE_VERSION=nw181-20260628 \
  --build-arg IMAGE_REVISION="$revision" \
  --build-arg IMAGE_CREATED="$created" "$src" >"$log" 2>&1
docker image inspect "$image" --format \
  "image={{.RepoTags}} id={{.Id}} created={{.Created}} revision={{index .Config.Labels \"org.opencontainers.image.revision\"}} source={{index .Config.Labels \"org.opencontainers.image.source\"}} user={{.Config.User}}"'
```

Image tagging and server recreate:

```bash
ssh -o BatchMode=yes nmcp@192.168.1.213 'set -eu
docker tag datarun-local-server:local-keycloak-conditional-web-security \
  datarun-local-server:pre-nw181-b340416e
docker tag datarun-local-server:nw181-473741b3 \
  datarun-local-server:local-keycloak-conditional-web-security
cd /home/nmcp/datarun-local-app
docker compose -f compose.yaml up -d --no-deps --force-recreate server'
```

Field-assets package inputs were staged into the existing provisioning mount
under field-assets-specific names:

```bash
scp deploy/reference/pilot-packages/field-assets/reviewed-config.json \
  nmcp@192.168.1.213:/home/nmcp/datarun-local-app/build/field-assets-reviewed-config.json
scp deploy/reference/pilot-packages/field-assets/assignment-bootstrap.setup-owner.json \
  nmcp@192.168.1.213:/home/nmcp/datarun-local-app/build/field-assets-assignment-bootstrap.setup-owner.json
scp deploy/reference/pilot-packages/field-assets/seeded-field-assets.synthetic.json \
  nmcp@192.168.1.213:/home/nmcp/datarun-local-app/build/field-assets-seeded-field-assets.synthetic.json
ssh -o BatchMode=yes nmcp@192.168.1.213 'set -eu
sudo install -o 10001 -g 10001 -m 0640 \
  /home/nmcp/datarun-local-app/build/field-assets-reviewed-config.json \
  /home/nmcp/datarun-local-app/provisioning/field-assets-reviewed-config.json
sudo install -o 10001 -g 10001 -m 0640 \
  /home/nmcp/datarun-local-app/build/field-assets-assignment-bootstrap.setup-owner.json \
  /home/nmcp/datarun-local-app/provisioning/field-assets-assignment-bootstrap.setup-owner.json
sudo install -o 10001 -g 10001 -m 0640 \
  /home/nmcp/datarun-local-app/build/field-assets-seeded-field-assets.synthetic.json \
  /home/nmcp/datarun-local-app/provisioning/field-assets-seeded-field-assets.synthetic.json'
```

Standalone package config publish was attempted first and correctly rejected
because the deployment already had current M1.1 shapes:

```bash
ssh -o BatchMode=yes nmcp@192.168.1.213 'cd /home/nmcp/datarun-local-app &&
docker compose -f compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=config-publish \
  --datarun.ops.input=/run/datarun/provisioning/field-assets-reviewed-config.json \
  --datarun.ops.operator-id=17400000-0000-4000-8000-000000000099 \
  --datarun.ops.evidence-id=NW-181:field-assets-config-publish'
```

To preserve the intended deployment DB/config state, a complete reviewed config
input was generated from the live config package version `2`, live
`deployment_config` policy rows, and the field-assets reviewed config. The
generated input was installed as
`/run/datarun/provisioning/nw181-field-assets-complete-config.json`.

```bash
jq -n \
  --slurpfile pkg /tmp/nw181-current-config-v2.json \
  --slurpfile assign /tmp/nw181-assignment-admin-capabilities.json \
  --slurpfile admin /tmp/nw181-admin-command-capabilities.json \
  --slurpfile asset deploy/reference/pilot-packages/field-assets/reviewed-config.json \
  '($pkg[0]) as $pkg | ($asset[0]) as $asset |
   if (($pkg.expressions // {}) | keys | length) != 0 then
     error("live package expressions are non-empty")
   else
     {
       schema_version: 1,
       source: "NW-181:m11-plus-field-assets-smoke",
       shapes: (((($pkg.shapes // {}) | to_entries | map(.value | {
         name, version, status, sensitivity,
         schema_json: {
           fields: (.fields // []),
           subject_binding,
           uniqueness
         }
       })) + ($asset.shapes // [])) | unique_by(.name + "/" + (.version|tostring))),
       activities: (((($pkg.activities // {}) | to_entries | map(.value | {
         name, status, sensitivity,
         config_json: ({shapes: (.shapes // []), roles: (.roles // {})} +
           (if .pattern == null then {} else {pattern} end))
       })) + ($asset.activities // [])) | unique_by(.name)),
       expressions: [],
       flag_severity_overrides: ($pkg.flag_severity_overrides // {}),
       assignment_admin_capabilities:
         ($assign[0] | .roles = ((.roles // {}) *
           ($asset.assignment_admin_capabilities.roles // {}))),
       admin_command_capabilities:
         ($admin[0] | .actors = ((.actors // {}) *
           ($asset.admin_command_capabilities.actors // {})))
     }
   end' > /tmp/nw181-field-assets-complete-config.json
```

Complete config publish:

```bash
ssh -o BatchMode=yes nmcp@192.168.1.213 'cd /home/nmcp/datarun-local-app &&
docker compose -f compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=config-publish \
  --datarun.ops.input=/run/datarun/provisioning/nw181-field-assets-complete-config.json \
  --datarun.ops.operator-id=17400000-0000-4000-8000-000000000099 \
  --datarun.ops.evidence-id=NW-181:field-assets-complete-config-publish'
```

Setup-owner bootstrap and seed precondition probes:

```bash
ssh -o BatchMode=yes nmcp@192.168.1.213 'cd /home/nmcp/datarun-local-app &&
docker compose -f compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=assignment-bootstrap \
  --datarun.ops.input=/run/datarun/provisioning/field-assets-assignment-bootstrap.setup-owner.json \
  --datarun.ops.operator-id=17400000-0000-4000-8000-000000000099 \
  --datarun.ops.evidence-id=NW-181:field-assets-assignment-bootstrap'

ssh -o BatchMode=yes nmcp@192.168.1.213 'cd /home/nmcp/datarun-local-app &&
docker compose -f compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=field-assets-seed \
  --datarun.ops.input=/run/datarun/provisioning/field-assets-seeded-field-assets.synthetic.json \
  --datarun.ops.operator-id=17400000-0000-4000-8000-000000000099 \
  --datarun.ops.evidence-id=NW-181:field-assets-seed-precondition'
```

Public HTTPS and web-admin probes:

```bash
curl -sS -o /tmp/nw181-app-auth-me-missing.json \
  -w 'app_auth_me_missing_http=%{http_code} remote=%{remote_ip} ssl=%{ssl_verify_result}\n' \
  https://app.nmcpye.org/api/auth/me
curl -sS -o /tmp/nw181-app-web-admin-login.txt \
  -w 'app_web_admin_login_http=%{http_code} remote=%{remote_ip} ssl=%{ssl_verify_result} redirect=%{redirect_url}\n' \
  https://app.nmcpye.org/web-admin/login
curl -sS -o /tmp/nw181-auth-oidc.json \
  -w 'auth_oidc_http=%{http_code} remote=%{remote_ip} ssl=%{ssl_verify_result}\n' \
  https://auth.nmcpye.org/realms/datarun-local/.well-known/openid-configuration
```

The web-admin login check used the actual OIDC browser form flow with the
retained `hamza-pilot` credential from the auth VM. The command kept the
password and session cookies in temporary files and did not print them.

## Evidence Summary

| Check | Result | Evidence |
|---|---|---|
| Current image built | Pass | Docker build succeeded; image `sha256:2a2962579d14a568b2dfe7cc2f0c4534f08299745154051dbd81ca11c64c8506`, revision `473741b3beff2e18b39bdf88e5fe59d9df0dfc62`, user `10001:10001`. |
| Rollback preserved | Pass | Previous image retained as `datarun-local-server:pre-nw181-b340416e`. |
| Server service refreshed | Pass | `docker compose up -d --no-deps --force-recreate server` completed; readiness returned `{"status":"UP"}`; container health `healthy`. |
| Public app protected endpoint | Pass | `https://app.nmcpye.org/api/auth/me` returned HTTP `401`, remote `134.35.225.57`, `ssl=0`. |
| Public web-admin login redirect | Pass | `/web-admin/login` returned HTTP `302` to `https://auth.nmcpye.org/realms/datarun-local/...` with callback `https://app.nmcpye.org/web-admin/oidc/callback`. |
| Public Keycloak discovery | Pass | OIDC discovery returned HTTP `200`, issuer `https://auth.nmcpye.org/realms/datarun-local`, `ssl=0`. |
| Web-admin OIDC login | Pass | Browser-style form login posted through Keycloak and landed on `https://app.nmcpye.org/web-admin/shell` with HTTP `200`. Direct password grant was not used because `datarun-mobile` rejects direct access grants. |
| Web-admin standing pages | Pass | Authenticated `/web-admin/operational` and `/web-admin/operational/report` both returned HTTP `200`. |
| One-shot app/DB/Flyway path | Pass | One-shot commands reached app startup, DB connectivity, Flyway validation of 11 migrations, and schema version 11 with no migration required. |
| Standalone field-assets config | Expected reject | `config-publish` rejected the standalone field-assets config with `reviewed config omits existing shapes: [field_check_record/v1, stocktake_line/v1]`. |
| Complete config publish | Pass | Generated complete config published version `3`, `published=true`, `changed_authoring_rows=4`, input SHA-256 `021670bc23b386e9dce52278cae04d68d624c21e0f444ef57c8379f3f4ff192d`. |
| `assignment-bootstrap` package step | Blocked | Failed with `Bootstrap authority unavailable: existing assignment state differs`; live DB already had one initial NW-171 pilot admin assignment and one NW-171 field-worker assignment. |
| `field-assets-seed` command presence | Pass for command presence, blocked for application | The command was recognized by the new image and failed on the package precondition: `field asset setup-owner bootstrap assignment is required before field-assets-seed`. It no longer fails as `unsupported provisioning command`. |
| Field-assets seed mutation | Not run | DB counters after the failed seed precondition: `field_asset_seed_events=0`, `field_asset_setup_actor_assignments=0`. |
| Mobile/API capture/sync | Not run | The selected package did not apply, so capture/sync would not prove field-assets runtime alignment. |
| Field-assets reviewer evidence | Not run | No field-assets seed/capture event existed to review. Web-admin pages were reachable, but no field-assets reviewer-standing result is claimed. |

## Stop Reason

The smoke cannot complete on the current intended deployment DB because the
field-assets package requires an exact setup-owner bootstrap assignment that
the accepted bootstrap command cannot create after any assignment events already
exist.

Current live assignment state at the stop:

```text
15000000-0000-4000-8000-000000000001|pilot_admin|null|null|null|system:assignment_bootstrap/initial|1
15000000-0000-4000-8000-000000000001|field_worker|null|null|["field_check"]|15000000-0000-4000-8000-000000000001|1
```

This is not a Flyway/schema mismatch and not command absence. It is an
operational package precondition mismatch between the clean-field-assets setup
path and the already-used NW-171 deployment DB.

## State Left Behind

- The running app server image was updated to revision
  `473741b3beff2e18b39bdf88e5fe59d9df0dfc62`.
- Config package version `3` is published and includes the existing field-check
  and stock-operation entries plus `asset_check/v1` and
  `field_asset_inspection`.
- No field-assets setup-owner assignment, package geography,
  `subject_locations`, seed events, mobile capture, reviewer evidence, or
  out-of-scope denial evidence was created.
- The three field-assets package JSON inputs and the generated complete config
  input remain in the app VM provisioning mount as deployment evidence inputs.

## Follow-Up Work

No successor is selected here.

If field-assets live smoke remains the desired route, select one of these
bounded options explicitly:

- clean-deployment field-assets smoke, using a fresh local/on-prem DB where the
  accepted `assignment-bootstrap` precondition is true; or
- a bounded setup-owner assignment route for non-empty deployments, without
  broad assignment-admin, lifecycle, registry/import/export, candidate
  disposition, contract/schema/sync, or Keycloak hardening work.

Do not continue by adding candidate promotion/rejection, lifecycle,
duplicate/merge/split, semantic location/place behavior, registry/import/export,
real-data import, broad rollout, production cutover, Keycloak hardening, or
schema/contract/runtime changes by drift.
