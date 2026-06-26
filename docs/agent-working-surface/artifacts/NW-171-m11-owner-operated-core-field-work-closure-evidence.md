# NW-171 M1.1 Owner-Operated Core Field-Work Closure Evidence

Status: complete
Date: 2026-06-27
Type: implementation / validation evidence
Source: NW-171 execution packet and owner handoff

## Scope

NW-171 validates one lifecycle-neutral M1.1 field-check path for the
owner-operated local/on-prem posture. This artifact was created before
continuing into setup/config, assignment, mobile login, capture, sync,
correction, and standing evidence.

No runtime code, contracts, schemas, sync protocol, authority rules, S06
lifecycle, import/replay, retention/security promise, tenant/control-plane
behavior, broad reporting, Keycloak cutover hardening, or production cutover
was changed by this initial probe pass.

## First Environment Probes

Probe time:

```text
cwd: /home/hamza/datarun-platform
branch: product/nw-171-m11-owner-operated-core-field-work
commit: dee5c107f275a8caec01259ddd200abf76ff92a8
local time: 2026-06-27T00:56:13+03:00
```

### DNS And Resolver Paths

Default resolver after owner updated both `auth.nmcpye.org` and
`app.nmcpye.org`:

```bash
resolvectl flush-caches
getent ahostsv4 app.nmcpye.org
getent ahostsv4 auth.nmcpye.org
dig +short app.nmcpye.org A @127.0.0.53
dig +short auth.nmcpye.org A @127.0.0.53
```

Initial observed state after the first auth-only owner update:

```text
app.nmcpye.org -> 192.168.1.213 by libc/stub resolver
auth.nmcpye.org -> 5.255.17.8 by libc/stub resolver
stub DNS app answer: 192.168.1.213
stub DNS auth answer: b9070a9629c9.sn.mynetname.net / 5.255.17.8
```

Resolved observed state after the second owner update:

```text
app.nmcpye.org -> b9070a9629c9.sn.mynetname.net / 5.255.17.8
auth.nmcpye.org -> b9070a9629c9.sn.mynetname.net / 5.255.17.8
```

WireGuard DNS check:

```bash
dig +short app.nmcpye.org A @10.50.0.1
dig +short auth.nmcpye.org A @10.50.0.1
```

Initial observed state after the first auth-only owner update:

```text
app.nmcpye.org -> 192.168.1.213
auth.nmcpye.org -> b9070a9629c9.sn.mynetname.net / 5.255.17.8
```

Public resolver check performed earlier in the same pass showed public DNS for
both names resolving through `b9070a9629c9.sn.mynetname.net` to `5.255.17.8`.

Path classification:

| Probe target | Default path observed | Result |
|---|---|---|
| `app.nmcpye.org` | Public proxy path to `5.255.17.8` after owner update | Usable; Datarun responds. |
| `auth.nmcpye.org` | Public proxy path to `5.255.17.8` | Usable; Keycloak discovery and TLS pass. |
| `app.nmcpye.org` forced to `5.255.17.8` | Public proxy path | Usable; Datarun responds. |

### Datarun Base URL Probe

Initial default path after cache flush, before the second owner update:

```bash
curl -sS --max-time 10 -o /dev/null \
  -w 'post_flush_app_remote=%{remote_ip}:%{remote_port} status=%{http_code} ssl=%{ssl_verify_result}\n' \
  https://app.nmcpye.org/
```

Observed:

```text
post_flush_app_remote=:0 status=000 ssl=0
curl: Failed to connect to app.nmcpye.org port 443
```

Forced public proxy path:

```bash
curl -sS --max-time 10 --resolve app.nmcpye.org:443:5.255.17.8 \
  -o /dev/null \
  -w 'app_forced_public_remote=%{remote_ip}:%{remote_port} status=%{http_code} ssl=%{ssl_verify_result}\n' \
  https://app.nmcpye.org/
```

Observed:

```text
app_forced_public_remote=5.255.17.8:443 status=404 ssl=0
```

Interpretation: `404` at `/` is the current Datarun application response on
the public proxy path and is sufficient to prove the base host reaches Datarun.

Resolved default path after the second owner update:

```bash
resolvectl flush-caches
getent ahostsv4 app.nmcpye.org
dig +short app.nmcpye.org A @127.0.0.53
curl -sS --max-time 10 -o /dev/null \
  -w 'app_default_remote=%{remote_ip}:%{remote_port} status=%{http_code} ssl=%{ssl_verify_result}\n' \
  https://app.nmcpye.org/
```

Observed:

```text
app.nmcpye.org -> b9070a9629c9.sn.mynetname.net / 5.255.17.8
app_default_remote=5.255.17.8:443 status=404 ssl=0
```

### `/api/auth/me` Missing-Token Result

Forced public proxy path:

```bash
curl -sS --max-time 10 --resolve app.nmcpye.org:443:5.255.17.8 \
  -o /dev/null \
  -w 'app_forced_auth_me=%{remote_ip}:%{remote_port} status=%{http_code} ssl=%{ssl_verify_result}\n' \
  https://app.nmcpye.org/api/auth/me
```

Observed:

```text
app_forced_auth_me=5.255.17.8:443 status=401 ssl=0
```

Resolved default path after the second owner update:

```bash
curl -sS --max-time 10 -o /dev/null \
  -w 'app_auth_me_default_remote=%{remote_ip}:%{remote_port} status=%{http_code} ssl=%{ssl_verify_result}\n' \
  https://app.nmcpye.org/api/auth/me
```

Observed:

```text
app_auth_me_default_remote=5.255.17.8:443 status=401 ssl=0
```

Interpretation: the public proxy Datarun API returns the expected missing-token
`401`.

### `/web-admin/login` Redirect And Callback Host Consistency

Forced public proxy path before the owner-side DNS retry:

```bash
curl -k -sS --max-time 10 --resolve app.nmcpye.org:443:5.255.17.8 \
  -o /tmp/nw171-login-body.txt -D - \
  https://app.nmcpye.org/web-admin/login
```

Observed after redacting cookie, state, and nonce values:

```text
HTTP/1.1 302
Location: https://auth.nmcpye.org/realms/datarun-local/protocol/openid-connect/auth?...&client_id=datarun-web-admin&redirect_uri=https://app.nmcpye.org/web-admin/oidc/callback&state=<redacted>&nonce=<redacted>
```

Interpretation: the web-admin login redirect uses `auth.nmcpye.org` for OIDC
authorization and `app.nmcpye.org` for the callback host. The recorded evidence
does not retain cookies, authorization codes, state values, nonce values, or
tokens.

Resolved default path after both owner-side DNS updates:

```bash
curl -sS --max-time 10 -o /dev/null \
  -w 'login_default_remote=%{remote_ip}:%{remote_port} status=%{http_code} ssl=%{ssl_verify_result} redirect=%{redirect_url}\n' \
  https://app.nmcpye.org/web-admin/login
```

Observed after redacting state and nonce values:

```text
login_default_remote=5.255.17.8:443 status=302 ssl=0
redirect=https://auth.nmcpye.org/realms/datarun-local/protocol/openid-connect/auth?...&client_id=datarun-web-admin&redirect_uri=https://app.nmcpye.org/web-admin/oidc/callback&state=<redacted>&nonce=<redacted>
```

### Keycloak Discovery Issuer

Default path after owner updated `auth.nmcpye.org`:

```bash
curl -sS --max-time 10 \
  https://auth.nmcpye.org/realms/datarun-local/.well-known/openid-configuration \
  | jq -r '{issuer,authorization_endpoint,token_endpoint,jwks_uri}'
```

Observed:

```json
{
  "issuer": "https://auth.nmcpye.org/realms/datarun-local",
  "authorization_endpoint": "https://auth.nmcpye.org/realms/datarun-local/protocol/openid-connect/auth",
  "token_endpoint": "https://auth.nmcpye.org/realms/datarun-local/protocol/openid-connect/token",
  "jwks_uri": "https://auth.nmcpye.org/realms/datarun-local/protocol/openid-connect/certs"
}
```

Interpretation: `auth.nmcpye.org` now uses the public proxy path by default and
advertises the expected issuer.

### TLS And Certificate Observation

App public proxy path:

```bash
curl -sS --max-time 10 --resolve app.nmcpye.org:443:5.255.17.8 \
  -o /dev/null \
  -w 'app_tls_http=%{http_code} ssl_verify_result=%{ssl_verify_result}\n' \
  https://app.nmcpye.org/

echo | openssl s_client -connect app.nmcpye.org:443 \
  -servername app.nmcpye.org 2>/dev/null \
  | openssl x509 -noout -subject -issuer -dates -fingerprint -sha256 \
      -ext subjectAltName
```

Observed:

```text
app_tls_http=404 ssl_verify_result=0
subject=CN = app.nmcpye.org
issuer=C = US, O = Let's Encrypt, CN = YE1
notBefore=Jun 26 19:16:11 2026 GMT
notAfter=Sep 24 19:16:10 2026 GMT
sha256 Fingerprint=19:4B:32:38:FB:2D:CD:D7:51:3E:8A:61:A3:22:98:B1:D2:F9:4C:60:DA:79:3C:E0:EE:9E:D7:78:23:7D:69:55
Subject Alternative Name: DNS:app.nmcpye.org
```

Auth default path after owner-side retry:

```bash
curl -sS --max-time 10 -o /dev/null \
  -w 'auth_default_remote=%{remote_ip}:%{remote_port} status=%{http_code} ssl=%{ssl_verify_result}\n' \
  https://auth.nmcpye.org/realms/datarun-local/.well-known/openid-configuration

echo | openssl s_client -connect auth.nmcpye.org:443 \
  -servername auth.nmcpye.org 2>/dev/null \
  | openssl x509 -noout -subject -issuer -dates -fingerprint -sha256 \
      -ext subjectAltName
```

Observed:

```text
auth_default_remote=5.255.17.8:443 status=200 ssl=0
subject=CN = auth.nmcpye.org
issuer=C = US, O = Let's Encrypt, CN = YE1
notBefore=Jun 26 19:25:28 2026 GMT
notAfter=Sep 24 19:25:27 2026 GMT
sha256 Fingerprint=3B:99:5F:FC:27:82:60:0D:8A:CE:60:C9:EC:F1:FB:BF:7F:D7:A5:71:06:29:5B:02:72:46:CA:50:E2:84:0E:C5
Subject Alternative Name: DNS:auth.nmcpye.org
```

Earlier LAN-local auth path before the owner-side retry:

```text
auth.nmcpye.org -> 192.168.1.217
TLS certificate subject=CN = keycloak.lab
issuer=CN = Datarun NW-067 Lab Root CA
discovery status=404
```

Interpretation: the auth default path is now corrected to the public proxy.
The app default path is also corrected to the public proxy after the second
owner-side DNS update.

### VM / Topology Summary

Secret-safe topology observations:

```text
Datarun VM: 192.168.1.213, hostname vm-datarun-app
Keycloak VM: 192.168.1.217, hostname vm-datarun-ops-01
Public proxy/DNS target: 5.255.17.8 / b9070a9629c9.sn.mynetname.net
Default resolver in this workstation: systemd-resolved with WireGuard DNS 10.50.0.1
```

Datarun VM container/listener observations:

```text
datarun-local-app-server-1 maps 192.168.1.213:28443->8080/tcp
datarun-local-app-db-1 runs PostgreSQL 16 image
management listener: 127.0.0.1:28481 over HTTP
readiness: HTTP 200 {"status":"UP"}
no listener observed on 192.168.1.213:443
```

Keycloak VM container/listener observations:

```text
datarun-local-keycloak maps 192.168.1.217:28443->8443/tcp
datarun-local-keycloak command includes --hostname=https://auth.nmcpye.org
datarun-keycloak retained container maps 0.0.0.0:443->8443/tcp with keycloak.lab hostname/cert
no active nginx service observed on the Keycloak VM
```

The auth DNS correction made the default `auth.nmcpye.org` path use the public
proxy instead of the retained `datarun-keycloak` container on the Keycloak VM.
The later app DNS correction made the default `app.nmcpye.org` path use the
same public proxy instead of the app VM LAN address.

## DNS Blocker Resolved Before Journey Continuation

Blocked action: Use default `https://app.nmcpye.org` as the Datarun web/API
base URL after resolver cache flush.

Evidence: `getent ahostsv4 app.nmcpye.org` and `dig @127.0.0.53` return
`192.168.1.213`; `curl https://app.nmcpye.org/` fails to connect to port 443;
the app VM has no listener on `192.168.1.213:443`; forced public proxy
`--resolve app.nmcpye.org:443:5.255.17.8` returns Datarun `404` at `/` and
expected missing-token `401` at `/api/auth/me` with `ssl_verify_result=0`.

Smallest missing capability/fact: remove or change the WireGuard DNS override
for `app.nmcpye.org` so the default resolver path matches the public proxy
answer `5.255.17.8`, or provide access to the DNS/proxy host that owns the
override.

Independent work that can continue: auth default-path evidence is now green;
forced-public Datarun probes are green; accepted NW-163 through NW-165 and
platform specs can still be cited while waiting for default app DNS correction.

Owner question, if absolutely required: answered by the second owner-side DNS
update; no remaining owner question for domain routing before journey
continuation.

Resolution evidence:

```text
app.nmcpye.org -> b9070a9629c9.sn.mynetname.net / 5.255.17.8
app_default_remote=5.255.17.8:443 status=404 ssl=0
app_auth_me_default_remote=5.255.17.8:443 status=401 ssl=0
login_default_remote=5.255.17.8:443 status=302 ssl=0 redirect host auth.nmcpye.org callback host app.nmcpye.org
```

## Owner-Operated M1.1 Journey Evidence

All live journey probes below used the corrected default resolver path:

```text
app.nmcpye.org -> 5.255.17.8, public proxy path
auth.nmcpye.org -> 5.255.17.8, public proxy path
```

The LAN DNS caveat remains preserved: a local resolver may route
`auth.nmcpye.org` directly to the Keycloak VM / LAN path in other environments.
This run did not use that LAN-local path after the owner-side DNS correction.

### Setup / Config

Local package files added for the contained field-check proof:

```text
deploy/reference/pilot-packages/field-check/README.md
deploy/reference/pilot-packages/field-check/reviewed-config.json
deploy/reference/pilot-packages/field-check/assignment-create.field-worker.json
```

Package hashes:

```text
reviewed-config.json sha256=114974c2c28f07010482ec6812c39ce18eb047cec9ae97367cccf9f40a1b988d
assignment-create.field-worker.json sha256=e3c7e3a27584245fdcb5a1ac747c8a2c0d4cea8f74c920e3ccc6117913b1359b
```

The reviewed config was installed on the app VM at
`/home/nmcp/datarun-local-app/provisioning/nw171-field-check-reviewed-config.json`
with read-only mode and then published through the existing one-shot command:

```bash
ssh nmcp@192.168.1.213 \
  'cd /home/nmcp/datarun-local-app &&
   docker compose -f compose.yaml run --rm --no-deps server
     --spring.main.web-application-type=none
     --datarun.ops.command=config-publish
     --datarun.ops.input=/run/datarun/provisioning/nw171-field-check-reviewed-config.json
     --datarun.ops.operator-id=<existing NW-164 operator UUID>
     --datarun.ops.evidence-id=NW-171:m11-field-check-config'
```

Observed, secret-safe result:

```json
{
  "command": "config-publish",
  "status": "succeeded",
  "evidence_id": "NW-171:m11-field-check-config",
  "config_version": 2,
  "published": true,
  "changed_authoring_rows": 3,
  "input_sha256": "114974c2c28f07010482ec6812c39ce18eb047cec9ae97367cccf9f40a1b988d"
}
```

Authenticated config probe:

```bash
curl -H 'Authorization: Bearer <redacted>' \
  https://app.nmcpye.org/api/sync/config
```

Observed:

```text
status=200 etag="2"
{"version":2,"has_field_check_shape":true,"has_field_check_activity":true,"field_check_roles":{"field_worker":["capture"]}}
```

### Principal Binding

The live mobile public-client OIDC authorization-code + PKCE exchange used:

```text
authorization endpoint: https://auth.nmcpye.org/realms/datarun-local/protocol/openid-connect/auth
token endpoint: https://auth.nmcpye.org/realms/datarun-local/protocol/openid-connect/token
client_id: datarun-mobile
redirect_uri: dev.datarun.mobile://oauth2redirect
```

No password, cookie, state, nonce, authorization code, token, or refresh token
is stored in this artifact.

Observed, secret-safe result:

```text
auth_form=present
callback_scheme=dev.datarun.mobile
callback_host=oauth2redirect
callback_state_match=yes
code_present=yes
token_http=200
token_issuer=https://auth.nmcpye.org/realms/datarun-local
token_audience=datarun-server,account
token_client=datarun-mobile
token_user=hamza-pilot
auth_me={"actor_id":"15000000-0000-4000-8000-000000000001","auth_source":"oidc-jwks-principal"}
```

### Assignment

Contained assignment request:

```json
{
  "target_actor_id": "15000000-0000-4000-8000-000000000001",
  "role": "field_worker",
  "geographic_scope": null,
  "subject_list": null,
  "activity_list": ["field_check"],
  "valid_from": "2026-06-27T00:00:00Z",
  "valid_to": null
}
```

Live create/list probes:

```bash
curl -X POST https://app.nmcpye.org/api/assignments \
  -H 'Authorization: Bearer <redacted>' \
  -H 'Content-Type: application/json' \
  --data-binary @deploy/reference/pilot-packages/field-check/assignment-create.field-worker.json

curl -H 'Authorization: Bearer <redacted>' \
  https://app.nmcpye.org/api/assignments/actor/15000000-0000-4000-8000-000000000001
```

Observed:

```text
assignment_create={"assignment_id":"dae89213-b727-427b-92aa-9de54ac07ae8","event_id":"584d5444-47a6-4917-bf63-45f56155ab0c","error":null}
assignment_list={"count":2,"field_check":1}
```

### Mobile Login / Scoped Work / Offline Capture / Sync / Correction

An actual Android UI run was attempted, but the host cannot boot the configured
x86_64 AVDs because `/dev/kvm` is unavailable:

```text
emulator -avd dev_phone -no-window -no-snapshot -gpu swiftshader_indirect -no-audio
ERROR | x86_64 emulation currently requires hardware acceleration!
CPU acceleration status: /dev/kvm is not found
```

This blocks only the Android UI/device run in the current workstation
environment. It did not block mobile data-layer validation, live app/auth
domains, or server-side journey continuation.

A one-off Flutter test harness was created and removed in the same working
turn. It exercised mobile `MobileAuthService`, `DeviceIdentity`, actor-local
`EventStore`, `ConfigStore`, `EventAssembler`, and `SyncService` against the
live public domains with the OIDC token obtained above.

Command:

```bash
cd /home/hamza/datarun-platform/mobile
flutter test test/nw171_live_journey_test.dart --reporter=expanded
```

Observed:

```text
mobile_sign_in_actor=15000000-0000-4000-8000-000000000001
mobile_setup_sync pushed=0 pulled=2 config_version=2 local_assignments=2 field_check_assignments=1
mobile_offline_capture event_id=de4d89b3-c9e0-4abc-996a-8d12961ae3b8 subject_id=17100000-0000-4000-8000-000000000001 pending=1
mobile_capture_sync pushed=1 pulled=1 pending=0 watermark=3
mobile_offline_correction event_id=3c50205e-37c6-4cc7-baaf-9967962f0678 subject_id=17100000-0000-4000-8000-000000000001 pending=1
mobile_correction_sync pushed=1 pulled=1 pending=0 watermark=4
00:02 +1: All tests passed!
```

Interpretation: the mobile data layer signed in with the live OIDC-derived
credential, loaded config v2, learned the field-check assignment, assembled one
offline capture, synced it, assembled one lifecycle-neutral corrective
recapture for the same subject, and synced it with no pending local work left.

### Owner / Supervisor Standing, Freshness, And Attention

Web-admin session was opened through the live web OIDC path:

```text
web_login_redirect_host=auth.nmcpye.org
web_callback_host=app.nmcpye.org
web_post_http=302 callback_http=303 shell_http=200 operational_http=200 report_http=200 handoff_http=200 attention_http=200
```

Before the controlled attention cue, the scoped operational report showed the
clean field-check standing:

```text
Freshness state: known_latest_input
Latest visible input: 2026-06-26T22:16:48.306288Z
Clean source work: 2
Excluded unresolved source count: 0
Needs attention: 0
Activity row: Field Check | clean source work 2 | needs attention 0
```

The current accepted attention boundary was then exercised with one contained,
structurally valid `review` event against the field-check shape. The activity
grants `field_worker` only `capture`, so the accepted auth conflict detector
persisted the source event and raised `role_stale`. No new contract, lifecycle,
resolver, or queue behavior was added.

Push result:

```text
attention_push_event_id=735bfd07-e943-4177-b46e-7e2218589d65
attention_push={"accepted":1,"duplicates":0,"flags_raised":1,"error":null,"details":null}
```

Secret-safe DB readback:

```text
3deb450e-3078-36df-94fe-416bf088eaaf|role_stale|actor|system:resolver_unassigned/role_stale
```

Post-flag operational and report standing:

```text
Operational latest synced work:
Field Check Record / Field Check / subject 17100000-0000-4000-8000-000000000001
Received at 2026-06-26T22:18:53.640951Z
Freshness: latest visible synced work received at 2026-06-26T22:18:53.640951Z;
this does not prove all devices are current.
Needs review: One unresolved attention item is attached to this work.

Scoped report:
Freshness state: known_latest_input
Latest visible input: 2026-06-26T22:18:53.824517Z
Clean source work: 2
Excluded unresolved source count: 1
Needs attention: 1
Activity row: Field Check | clean source work 2 | excluded unresolved 1 | needs attention 1
```

Handoff page caveats:

```text
Freshness unknown; this does not prove every device has synced.
Needs attention: Visible work has unresolved attention.
Late synced work may include work captured offline before it became visible here.
Not currently resolvable by this session; use the designated reviewer standing.
```

Attention review page:

```text
Source Work: Field Check Record / Field Check
Reviewer Standing: This item is blocked because no reviewer is currently assigned.
```

This is an accepted attention cue and reviewer-standing observation. It is not
a new reviewer assignment, resolver reassignment, batch queue, or broad
reporting implementation.

### Run / Diagnose Evidence

Run bundle collected after the journey:

```text
current_time=2026-06-27T01:21:01+03:00
git_commit=dee5c107f275a8caec01259ddd200abf76ff92a8
dns_app=5.255.17.8
dns_auth=5.255.17.8
app_base=remote=5.255.17.8:443 status=404 ssl=0
auth_discovery=remote=5.255.17.8:443 status=200 ssl=0
app_vm_health=status=200 body={"status":"UP"}
```

App VM container posture:

```text
datarun-local-app-db-1       running
datarun-local-app-server-1   running   192.168.1.213:28443->8080, 127.0.0.1:28481->8081
```

Field-check event/flag rows:

```text
de4d89b3-c9e0-4abc-996a-8d12961ae3b8|capture|field_check_record/v1|field_check|17100000-0000-4000-8000-000000000001|15000000-0000-4000-8000-000000000001|3|NULL|NULL
3c50205e-37c6-4cc7-baaf-9967962f0678|capture|field_check_record/v1|field_check|17100000-0000-4000-8000-000000000001|15000000-0000-4000-8000-000000000001|4|NULL|NULL
735bfd07-e943-4177-b46e-7e2218589d65|review|field_check_record/v1|field_check|17100000-0000-4000-8000-000000000001|15000000-0000-4000-8000-000000000001|5|NULL|NULL
3deb450e-3078-36df-94fe-416bf088eaaf|alert|conflict_detected/v1|NULL|17100000-0000-4000-8000-000000000001|system:conflict_detector/role_stale|6|role_stale|735bfd07-e943-4177-b46e-7e2218589d65
```

## Acceptance Rows

User-visible outcome: One owner-operated M1.1 field-check path is now proven
through the live public app/auth domains: setup/config, principal binding,
assignment, mobile data-layer sign-in, scoped work, offline capture, sync,
corrective recapture, scoped operational standing, freshness caveat, attention
cue, and run/diagnose evidence.

Security/authorization checked: Missing-token `/api/auth/me` returns `401`;
OIDC discovery issuer is `https://auth.nmcpye.org/realms/datarun-local`; the
mobile public-client PKCE token resolves through `/api/auth/me` to actor
`15000000-0000-4000-8000-000000000001` with
`auth_source=oidc-jwks-principal`; assignment and sync APIs were called through
the same bearer boundary.

Offline/sync checked: Mobile data-layer evidence assembled one offline
field-check capture and one corrective recapture, synced both, and ended with
`pending=0`.

Freshness checked: Web operational and scoped report pages show
`known_latest_input`, latest visible input timestamps, clean source work count,
and the explicit caveat that this does not prove all devices are current.

Review/attention checked: A contained accepted `role_stale` attention cue was
raised from a structurally valid field-check `review` event; operational,
report, handoff, and attention pages show the unresolved attention and reviewer
standing without adding resolver reassignment or queue behavior.

Operations/support checked: DNS, TLS, VM/topology, readiness, container
posture, app/auth path classification, event rows, and flag rows are recorded
above without secrets.

Validation evidence:

```text
cwd=/home/hamza/datarun-platform
git diff --check
result: passed

rg "NW-171|M1.1|owner-operated local/on-prem core field-work closure" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/agent-working-surface/prompts/NW-171-implement-m11-owner-operated-core-field-work-closure.md
result: passed

rg "app\\.nmcpye\\.org|auth\\.nmcpye\\.org|public proxy|local-DNS|LAN" docs/status.md docs/agent-working-surface/platform-next-work-backlog.md docs/agent-working-surface/prompts/NW-171-implement-m11-owner-operated-core-field-work-closure.md
result: passed

test -f docs/agent-working-surface/artifacts/NW-171-m11-owner-operated-core-field-work-closure-evidence.md
rg "User-visible outcome:|Security/authorization checked:|Operations/support checked:|Accepted evidence preserved:" docs/agent-working-surface/artifacts/NW-171-m11-owner-operated-core-field-work-closure-evidence.md
result: passed

rg "Blocked action:|Evidence:|Smallest missing capability/fact:|Independent work that can continue:|Owner question, if absolutely required:" docs/agent-working-surface/artifacts/NW-171-m11-owner-operated-core-field-work-closure-evidence.md docs/status.md docs/agent-working-surface/platform-next-work-backlog.md
result: passed

jq package and assignment structural checks for deploy/reference/pilot-packages/field-check/*.json
result: passed

cd /home/hamza/datarun-platform/mobile
flutter test test/stocktake_capture_smoke_test.dart --reporter=expanded
result: passed, 1 test, 0 failures

cd /home/hamza/datarun-platform/mobile
flutter test test/nw171_live_journey_test.dart --reporter=expanded
result: passed, 1 test, 0 failures
note: one-off live-network test file was removed after the run.

cd /home/hamza/datarun-platform/server
./mvnw -Dtest=StockOperationsPilotPackageIntegrationTest test
initial result: failed before assertions because localhost:15432 test DB was not running.

cd /home/hamza/datarun-platform
docker compose -f docker-compose.test.yml up -d test-db
result: passed, test DB started.

cd /home/hamza/datarun-platform/server
./mvnw -Dtest=StockOperationsPilotPackageIntegrationTest test
result: passed, 1 test, 0 failures, 0 errors, 0 skipped.
```

Explicit deferrals and routes: S06 lifecycle, Keycloak cutover hardening,
import/replay, retention/security promises, tenant/control-plane work, broad
reporting/export/import, and production cutover remain out of NW-171 scope.

Accepted evidence preserved: NW-163, NW-164, NW-165, and the accepted platform
specs named by the NW-171 packet remain preserved as evidence anchors.

Stop conditions: No forbidden NW-171 implementation has been started. The
temporary default Datarun base URL DNS/path mismatch was resolved before
journey continuation.
