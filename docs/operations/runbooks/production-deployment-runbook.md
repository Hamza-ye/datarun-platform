# Production Deployment Runbook

Status: in_review
Document type: runbook
Owner: Hamza
Source: NW-066 and
`docs/agent-working-surface/prompts/NW-066-write-production-deployment-runbook.md`
Authority: operates within the accepted first reference deployment policy,
NW-063, NW-065, BAR-001 through BAR-015, BAR-104, IDR-027, IDR-028,
IDR-029, and IDR-030
Last reviewed: 2026-06-14
Supersedes: none
Related:
`docs/operations/policies/first-reference-deployment-policy.md`,
`docs/operations/rehearsals/production-deployment-rehearsal-plan.md`,
`deploy/reference/README.md`, `deploy/reference/compose.yaml`,
`deploy/reference/preflight.sh`, and
`deploy/reference/provisioning-inputs.md`

## 1. Scope And Safety Boundary

This runbook operates one immutable Datarun server container on one Linux host,
behind an external TLS reverse proxy, with durable external PostgreSQL 16. It
uses synthetic, non-sensitive data until the separate real-production gates
in the policy are accepted.

The repository supplies the server image, production profile, Compose service,
preflight, health/metrics endpoints, and audited one-shot provisioning. The
deployment must supply and approve exact adapters for DNS/TLS, PostgreSQL
creation and recovery, secret population, OIDC token acquisition, monitoring,
alerts, and evidence storage. A provider-neutral requirement is not proof that
one of those adapters exists.

Do not use `/admin`, `/admin/config`, or `/admin/dev`. Do not mutate application
tables or Flyway history with operator SQL. The SQL in this runbook is
read-only evidence collection.

## 2. Required Execution Record

Create an access-controlled execution record before running commands. Record
the following without secret values:

- change/rehearsal ID, operator, approver, date, `Asia/Aden` window, and target;
- repository commit, full image digest, OCI source, OCI revision, and image
  verification/vulnerability evidence;
- host, public rehearsal DNS name, loopback application/management ports, and
  capacity limits;
- PostgreSQL 16 endpoint owner, database name, least-privilege runtime role,
  TLS trust source, and a secret-safe `psql` service profile name;
- exact approved database create, recovery-point create, freshness check,
  restore/PITR, and backup-retention procedures;
- exact approved DNS, TLS proxy, firewall, certificate validation, renewal,
  and expiry-alert procedures;
- exact approved secret population, access, rotation, and revocation
  procedures;
- OIDC issuer, audience, JWKS URI, test-principal owner, and secret-safe token
  acquisition/revocation procedure;
- log/metric destinations, dashboard, alert rules, recipients, and alert-test
  procedure;
- provisioning input paths, reviews, operator UUID, and evidence identifiers;
- application rollback compatibility statement or an explicit statement that
  only restore or forward fix is allowed;
- evidence root, retention classification, and incident/communication route.

For synthetic rehearsal Hamza may use the accepted one-person exception, but
must record why a second reviewer was unavailable, the reviewed artifacts,
automated results, recovery readiness, and retrospective review.

**Stop:** do not begin NW-067 when any required adapter is absent, generic,
untested, or inaccessible to its named operator.

## 3. Common Shell Setup

Run from the repository root on the application host. Do not enable shell
tracing. Populate these non-secret values from the execution record:

```bash
set -euo pipefail
export DATARUN_IMAGE='registry.example/datarun/server@sha256:<64-hex-digest>'
export DATARUN_IMAGE_SOURCE='https://<approved-source>'
export DATARUN_IMAGE_REVISION='<40-hex-commit>'
export DATARUN_RUNTIME_CONFIG_DIR='/absolute/secret-managed/runtime-config'
export DATARUN_PROVISIONING_DIR='/absolute/reviewed/provisioning'
export DATARUN_TRUST_DIR='/absolute/trust-material'
export DATARUN_APP_HOST_PORT='18080'
export DATARUN_MANAGEMENT_HOST_PORT='18081'
export DATARUN_CPU_LIMIT='1.0'
export DATARUN_MEMORY_LIMIT='1024m'
export DATARUN_PIDS_LIMIT='256'
export DATARUN_STOP_GRACE_PERIOD='45s'
export DATARUN_OPERATOR_ID='<operator-uuid>'
export DATARUN_EVIDENCE_ID='<approved-change-or-rehearsal-id>'
export DATARUN_PUBLIC_BASE_URL='https://<synthetic-rehearsal-host>'
export DATARUN_PGSERVICE='<secret-safe-psql-service-profile>'
export DATARUN_RESTORE_PGSERVICE='<secret-safe-restore-psql-service-profile>'
export DATARUN_EVIDENCE_DIR='/absolute/access-controlled/evidence-directory'
export DATARUN_AUTH_CURL_CONFIG='/absolute/protected/curl-auth-config'
export DATARUN_WRONG_AUDIENCE_CURL_CONFIG='/absolute/protected/wrong-audience-curl-config'
export DATARUN_WRONG_ISSUER_CURL_CONFIG='/absolute/protected/wrong-issuer-curl-config'
export DATARUN_OUT_OF_SCOPE_AUTH_CURL_CONFIG='/absolute/protected/out-of-scope-actor-curl-config'
```

`DATARUN_AUTH_CURL_CONFIG` is mode `0600`, is populated by the approved token
adapter, and contains only the bearer `Authorization` header for `curl`, with
no URL or behavior flags. Never put the token in an environment variable,
command line, ticket, or evidence file. The two negative-test config files
follow the same rule. The PostgreSQL service profile similarly keeps
credentials outside commands.

Create the evidence directory using the deployment's approved storage
procedure and verify only its metadata:

```bash
test -d "$DATARUN_EVIDENCE_DIR"
test "$(stat -c '%a' "$DATARUN_AUTH_CURL_CONFIG")" = '600'
test "$(stat -c '%a' "$DATARUN_WRONG_AUDIENCE_CURL_CONFIG")" = '600'
test "$(stat -c '%a' "$DATARUN_WRONG_ISSUER_CURL_CONFIG")" = '600'
test "$(stat -c '%a' "$DATARUN_OUT_OF_SCOPE_AUTH_CURL_CONFIG")" = '600'
command -v docker
command -v curl
command -v psql
command -v diff
command -v sha256sum
command -v jq
command -v grep
command -v date
```

Expected: the evidence directory is restricted, the auth file mode is `600`,
and the required operator clients are available.

**Stop:** secrets visible in process arguments/output, unrestricted evidence,
missing clients, or unavailable owner access. Correct the deployment adapter;
do not weaken the check.

## 4. Release Artifact Review

Prerequisites: approved release and locally available digest-selected image.

Inspect image identity:

```bash
docker image inspect \
  --format '{{.Id}} source={{index .Config.Labels "org.opencontainers.image.source"}} revision={{index .Config.Labels "org.opencontainers.image.revision"}} user={{.Config.User}}' \
  "$DATARUN_IMAGE"
```

Expected: digest/source/revision match approval; user is `10001:10001`;
the candidate is the exact artifact named in the execution record.

Separately execute the required image vulnerability review and
contract-resource verification for this exact digest. Execute signature
verification when the selected release process signs images. Repository CI
and `scripts/verify-server-image.sh` are release evidence inputs, not a
signature or vulnerability scanner.

**Stop:** mutable tag, identity mismatch, missing contract resource, or failed
review. Do not prepare the environment with an unapproved candidate. Replace
the candidate and repeat artifact review.

Retain: approval, image identity, vulnerability result, signature result when
signing is selected, contract-resource verification, and source revision.

## 5. Network, TLS, Database, And Secrets

Prerequisites: approved deployment-specific procedures from Section 2.

1. Execute the approved empty PostgreSQL database and least-privilege runtime
   role creation procedure. Grant only what Flyway and the application require.
2. Execute the approved DNS/TLS proxy and host-firewall procedure. Forward
   public HTTPS only to `127.0.0.1:$DATARUN_APP_HOST_PORT`; never proxy or
   expose the management port.
3. Populate the runtime config tree, provisioning directory, and PostgreSQL
   trust directory through the approved secret/config mechanism. Mounts remain
   read-only to UID/GID `10001:10001`.
4. Run full repository preflight and retain the rendered deployment.
5. Confirm the PostgreSQL major. Validate the public certificate and
   direct-port boundary using the deployment's approved network vantage
   points; the public application request is checked after start in Section 6.

```bash
deploy/reference/preflight.sh | tee "$DATARUN_EVIDENCE_DIR/preflight.txt"
docker compose -f deploy/reference/compose.yaml config \
  > "$DATARUN_EVIDENCE_DIR/compose-rendered.yaml"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command='SELECT current_setting('\''server_version_num'\'')::integer / 10000 AS postgres_major;'
```

Expected: public HTTPS has the approved chain/name; direct public application
and management ports are unreachable; JDBC uses `sslmode=verify-full` and the
mounted root certificate; and PostgreSQL reports major version `16`. Host
loopback liveness is checked after start in Section 6. Full preflight reports
success; rendered ports bind only to `127.0.0.1`; and the service uses the
production profile, read-only mounts/root filesystem, limits, capability drop,
and graceful stop.

**Stop:** plaintext public path, invalid certificate/name, public management
access, shared/default database credentials, writable secret mount, or secret
material in logs/evidence. Also stop on unreadable/missing mounted files,
development defaults, non-loopback listeners, or failed preflight. Revoke
exposed material or correct inputs before continuing.

Retain: sanitized DNS/TLS/firewall checks, database role identifier and grants
review, mounted-file metadata, secret identifiers/versions only, full preflight
output, and rendered Compose with secret values absent.

## 6. First Start And Forward Migration

Prerequisites: clean PostgreSQL database, successful preflight, approved
recovery procedure, and a recovery point no older than the 1-hour RPO when
upgrading an existing database.

Start the server and inspect startup:

```bash
docker compose -f deploy/reference/compose.yaml up -d server
docker compose -f deploy/reference/compose.yaml ps \
  | tee "$DATARUN_EVIDENCE_DIR/compose-ps.txt"
docker compose -f deploy/reference/compose.yaml logs --no-color server \
  > "$DATARUN_EVIDENCE_DIR/server-startup.log"
curl --fail --silent --show-error \
  "http://127.0.0.1:$DATARUN_MANAGEMENT_HOST_PORT/actuator/health/readiness" \
  | tee "$DATARUN_EVIDENCE_DIR/readiness.json"
curl --silent --show-error --output /dev/null \
  --write-out 'public_proxy_http_status=%{http_code}\n' \
  "$DATARUN_PUBLIC_BASE_URL" \
  | tee "$DATARUN_EVIDENCE_DIR/public-proxy-status.txt"
```

Collect read-only Flyway evidence:

```bash
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command='SELECT installed_rank, version, description, type, script, checksum, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;' \
  > "$DATARUN_EVIDENCE_DIR/flyway-schema-history.txt"
```

Expected: one healthy container; readiness is `UP`; Flyway rows are successful,
ordered, and match the candidate; startup has no contract-resource, database,
OIDC configuration, principal-binding manifest, or development-surface
failure. Normal server startup applies the reviewed principal-binding manifest
through the accepted audited provisioner. The public proxy completes trusted
TLS and returns an HTTP response without exposing either loopback listener.

**Stop:** failed/partial migration, checksum mismatch, lock timeout, missing
schema history, unhealthy service, or unexpected DDL. Remove traffic. Preserve
logs and database state. Never edit Flyway history or run a down migration.
Choose only authorized restore or tested forward fix.

Retain: timestamps, startup logs, readiness, schema history, and pre-migration
recovery-point identity/freshness.

## 7. OIDC And Explicit Principal Binding

Prerequisites: approved synthetic provider tenant, configured issuer/audience/
JWKS URI, reviewed `principal-bindings.json`, protected token adapter, and
operator/evidence IDs.

Confirm JWKS connectivity without recording key material:

```bash
curl --fail --silent --show-error \
  "$(tr -d '\r\n' < "$DATARUN_RUNTIME_CONFIG_DIR/datarun.auth.oidc.jwks-uri")" \
  --output /dev/null
```

The initial application occurred during normal startup in Section 6. Validate
the exact input and prove idempotency by applying it twice with the exact
one-shot command:

```bash
docker compose -f deploy/reference/compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=principal-bindings \
  --datarun.ops.input=/run/datarun/provisioning/principal-bindings.json \
  --datarun.ops.operator-id="$DATARUN_OPERATOR_ID" \
  --datarun.ops.evidence-id="$DATARUN_EVIDENCE_ID" \
  | tee "$DATARUN_EVIDENCE_DIR/principal-bindings-apply.json"
docker compose -f deploy/reference/compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=principal-bindings \
  --datarun.ops.input=/run/datarun/provisioning/principal-bindings.json \
  --datarun.ops.operator-id="$DATARUN_OPERATOR_ID" \
  --datarun.ops.evidence-id="$DATARUN_EVIDENCE_ID" \
  | tee "$DATARUN_EVIDENCE_DIR/principal-bindings-reapply.json"
```

Collect secret-safe audit evidence:

```bash
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command='SELECT operation_id, manifest_version, manifest_source, manifest_content_hash, applied_at, applied_by, issuer, subject, target_actor_id, desired_active, reason, changed FROM auth_principal_binding_operations ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/principal-binding-operations.txt"
```

Acquire a synthetic test token through the approved secret-safe adapter, then:

```bash
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/auth/me" \
  | tee "$DATARUN_EVIDENCE_DIR/auth-me.json"
test "$(
  curl --config "$DATARUN_WRONG_AUDIENCE_CURL_CONFIG" \
    --silent --show-error --output /dev/null --write-out '%{http_code}' \
    "$DATARUN_PUBLIC_BASE_URL/api/auth/me"
)" = '401'
test "$(
  curl --config "$DATARUN_WRONG_ISSUER_CURL_CONFIG" \
    --silent --show-error --output /dev/null --write-out '%{http_code}' \
    "$DATARUN_PUBLIC_BASE_URL/api/auth/me"
)" = '401'
```

Expected: both commands exit zero with the same `input_sha256`; reapplication
is idempotent; audit rows identify explicit issuer/subject operations; and
`/api/auth/me` returns the explicitly bound actor with OIDC auth source while
wrong-audience and wrong-issuer tokens receive HTTP 401.

**Stop:** wrong issuer/audience, unverifiable JWKS, ambiguous or drifting
operation, actor mismatch, provider group/role/claim authority, or secret
leakage. Revoke the test token and correct the reviewed binding/provider input.

## 8. Reviewed Configuration Publication

Prerequisites: complete reviewed config snapshot in the provisioning mount,
approval, and evidence ID.

```bash
docker compose -f deploy/reference/compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=config-publish \
  --datarun.ops.input=/run/datarun/provisioning/reviewed-config.json \
  --datarun.ops.operator-id="$DATARUN_OPERATOR_ID" \
  --datarun.ops.evidence-id="$DATARUN_EVIDENCE_ID" \
  | tee "$DATARUN_EVIDENCE_DIR/config-publish.json"
docker compose -f deploy/reference/compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=config-publish \
  --datarun.ops.input=/run/datarun/provisioning/reviewed-config.json \
  --datarun.ops.operator-id="$DATARUN_OPERATOR_ID" \
  --datarun.ops.evidence-id="$DATARUN_EVIDENCE_ID" \
  | tee "$DATARUN_EVIDENCE_DIR/config-republish.json"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command='SELECT version, published_at, published_by FROM config_packages ORDER BY version DESC LIMIT 1;' \
  > "$DATARUN_EVIDENCE_DIR/config-package-version.txt"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --dump-header "$DATARUN_EVIDENCE_DIR/config-response-headers.txt" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/config" \
  --output "$DATARUN_EVIDENCE_DIR/config-package.json"
```

Expected: transactional validation succeeds, both command results have the
same `input_sha256` and `config_version`, the second has `published=false`,
one package version is current, and the HTTP response has that package and
ETag.

**Stop:** omitted existing config, validation failure, partial publication,
unexpected new version on exact reapply, or need for a development admin
surface. Correct the complete reviewed input; do not patch tables directly.

## 9. Initial Assignment Bootstrap

Prerequisites: no prior assignment state in the clean environment, explicitly
bound target actor, a reviewed bounded synthetic subject/activity scope,
reviewed initial role, and accepted config containing the required
assignment-admin capability policy. The rehearsal binding manifest also
contains a second synthetic actor that initially has no assignment. The
create/end probe below gives it ended assignment history for the out-of-scope
sync test; principal binding itself grants no platform authority.

```bash
docker compose -f deploy/reference/compose.yaml run --rm --no-deps server \
  --spring.main.web-application-type=none \
  --datarun.ops.command=assignment-bootstrap \
  --datarun.ops.input=/run/datarun/provisioning/initial-assignment.json \
  --datarun.ops.operator-id="$DATARUN_OPERATOR_ID" \
  --datarun.ops.evidence-id="$DATARUN_EVIDENCE_ID" \
  | tee "$DATARUN_EVIDENCE_DIR/assignment-bootstrap.json"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command="SELECT id, actor_ref->>'id' AS actor_id, payload->'target_actor'->>'id' AS target_actor_id, payload->>'role' AS role, sync_watermark, timestamp FROM events WHERE type = 'assignment_changed' AND shape_ref = 'assignment_created/v1' ORDER BY sync_watermark;" \
  > "$DATARUN_EVIDENCE_DIR/assignment-events.txt"
```

Reapply the same bootstrap command and confirm it returns the same event ID.
Expected: exactly the reviewed initial event exists and exact reapplication is
idempotent. Subsequent assignment administration remains subject to accepted
command capability plus containment; bootstrap does not create general root
authority.

Prove the configured actor can create and end one contained synthetic
assignment for the second bound actor using reviewed request files. The
created scope is within the bootstrap actor's scope; ending it leaves the
second actor with assignment history but no active assignment for Section 10:

```bash
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_PROVISIONING_DIR/assignment-command-create.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/assignments" \
  | tee "$DATARUN_EVIDENCE_DIR/assignment-command-create.json"
export DATARUN_TEST_ASSIGNMENT_ID='<assignment_id returned above>'
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_PROVISIONING_DIR/assignment-command-end.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/assignments/$DATARUN_TEST_ASSIGNMENT_ID/end" \
  | tee "$DATARUN_EVIDENCE_DIR/assignment-command-end.json"
test "$(
  curl --config "$DATARUN_AUTH_CURL_CONFIG" \
    --silent --show-error --output /dev/null --write-out '%{http_code}' \
    --header 'Content-Type: application/json' \
    --data-binary @"$DATARUN_PROVISIONING_DIR/assignment-command-out-of-scope.json" \
    "$DATARUN_PUBLIC_BASE_URL/api/assignments"
)" = '400'
```

Expected: create and end each append one event and succeed only through one
active assignment that grants the relevant command and contains the target
scope; the reviewed out-of-scope request is rejected. Keep the returned
assignment ID in the execution record; do not parse it through an unreviewed
helper.

**Stop:** prior assignment state, drift, duplicate event, actor/scope mismatch,
missing command capability, containment failure for the intended test,
unexpected success outside the reviewed scope, or inferred authority from
deployment/IdP access. Preserve evidence and route the correction; do not
delete or rewrite events.

## 10. Device Auth, Config, Push, And Pull Smoke

Prerequisites: synthetic assigned actor, protected bearer token, approved test
device UUID, reviewed valid push request generated by the configured client or
fixture, and a pull request for that same actor/device. Also prepare a second
valid event for a different synthetic subject, authored by the explicitly
bound second actor whose contained assignment was ended in Section 9. The
assigned actor's bounded scope excludes that second event's subject. This is a
technical bearer-token smoke, not mobile OAuth/OIDC login acceptance.

```bash
export DATARUN_SMOKE_EVENT_ID="$(
  jq -er '.events[0].id' "$DATARUN_PROVISIONING_DIR/smoke-push-request.json"
)"
export DATARUN_OUT_OF_SCOPE_EVENT_ID="$(
  jq -er '.events[0].id' \
    "$DATARUN_PROVISIONING_DIR/smoke-out-of-scope-push-request.json"
)"
export DATARUN_POST_END_WATERMARK="$(
  psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
    --no-align --tuples-only \
    --command='SELECT COALESCE(MAX(sync_watermark), 0) FROM events;'
)"
jq --argjson watermark "$DATARUN_POST_END_WATERMARK" \
  '.last_pull_watermark = $watermark' \
  "$DATARUN_PROVISIONING_DIR/smoke-out-of-scope-push-request.json" \
  > "$DATARUN_EVIDENCE_DIR/smoke-out-of-scope-push-request.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/auth/me" \
  | tee "$DATARUN_EVIDENCE_DIR/device-auth-me.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/config" \
  --output "$DATARUN_EVIDENCE_DIR/device-config.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_PROVISIONING_DIR/smoke-push-request.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/push" \
  | tee "$DATARUN_EVIDENCE_DIR/device-push.json"
jq -e '.accepted == 1 and .duplicates == 0' \
  "$DATARUN_EVIDENCE_DIR/device-push.json"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --set=event_id="$DATARUN_SMOKE_EVENT_ID" \
  --no-align --tuples-only --field-separator='|' \
  --command="SELECT id, type, shape_ref, activity_ref, subject_ref::text, actor_ref::text, device_id, device_seq, timestamp, payload::text FROM events WHERE id = :'event_id'::uuid;" \
  > "$DATARUN_EVIDENCE_DIR/smoke-event-before-duplicate.txt"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_PROVISIONING_DIR/smoke-push-request.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/push" \
  | tee "$DATARUN_EVIDENCE_DIR/device-push-duplicate.json"
jq -e '.accepted == 0 and .duplicates == 1' \
  "$DATARUN_EVIDENCE_DIR/device-push-duplicate.json"
curl --config "$DATARUN_OUT_OF_SCOPE_AUTH_CURL_CONFIG" \
  --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary \
  @"$DATARUN_EVIDENCE_DIR/smoke-out-of-scope-push-request.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/push" \
  | tee "$DATARUN_EVIDENCE_DIR/out-of-scope-actor-push.json"
jq -e '.accepted == 1 and .flags_raised >= 1' \
  "$DATARUN_EVIDENCE_DIR/out-of-scope-actor-push.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_PROVISIONING_DIR/smoke-pull-request.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/pull" \
  | tee "$DATARUN_EVIDENCE_DIR/device-pull.json"
jq -e --arg included "$DATARUN_SMOKE_EVENT_ID" \
  --arg excluded "$DATARUN_OUT_OF_SCOPE_EVENT_ID" \
  '([.events[].id] | index($included)) != null and
   ([.events[].id] | index($excluded)) == null' \
  "$DATARUN_EVIDENCE_DIR/device-pull.json"
export DATARUN_FIRST_PULL_WATERMARK="$(
  jq -er '.latest_watermark' "$DATARUN_EVIDENCE_DIR/device-pull.json"
)"
jq --argjson watermark "$DATARUN_FIRST_PULL_WATERMARK" \
  '.since_watermark = $watermark' \
  "$DATARUN_PROVISIONING_DIR/smoke-pull-request.json" \
  > "$DATARUN_EVIDENCE_DIR/smoke-pull-next.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_EVIDENCE_DIR/smoke-pull-next.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/pull" \
  | tee "$DATARUN_EVIDENCE_DIR/device-pull-next.json"
jq -e --argjson previous "$DATARUN_FIRST_PULL_WATERMARK" \
  '.latest_watermark >= $previous and
   ([.events[].sync_watermark] | all(. > $previous))' \
  "$DATARUN_EVIDENCE_DIR/device-pull-next.json"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --set=event_id="$DATARUN_SMOKE_EVENT_ID" \
  --no-align --tuples-only --field-separator='|' \
  --command="SELECT id, type, shape_ref, activity_ref, subject_ref::text, actor_ref::text, device_id, device_seq, timestamp, payload::text FROM events WHERE id = :'event_id'::uuid;" \
  > "$DATARUN_EVIDENCE_DIR/smoke-event-after-sync.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/smoke-event-before-duplicate.txt" \
  "$DATARUN_EVIDENCE_DIR/smoke-event-after-sync.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/smoke-event-diff.txt"
```

Also execute the accepted mobile/client pending-to-synced presentation check
against the same synthetic event and record observations without token or
personal data.

Expected: server-resolved actor equals the explicit binding; config version is
available; first push accepts once and exact duplicate push creates no event;
the unassigned second actor's structurally valid event is accepted-and-flagged
but excluded from the assigned actor's pull; subsequent pull watermarks never
regress; the stored smoke event is byte-for-byte unchanged across duplicate
push and pulls; pending/freshness UI reflects the actual result.

**Stop:** actor mismatch, cross-scope data, watermark regression/rewrite,
event loss/mutation, false synced/fresh status, or token leakage. Treat
authority drift or cross-scope exposure as severity 1.

## 11. Health, Logs, Metrics, Alerts, And Capacity

Prerequisites: configured external collectors, dashboards, alert rules,
recipients, and approved alert-test adapter.

```bash
curl --fail --silent --show-error \
  "http://127.0.0.1:$DATARUN_MANAGEMENT_HOST_PORT/actuator/health/liveness" \
  | tee "$DATARUN_EVIDENCE_DIR/liveness.json"
curl --fail --silent --show-error \
  "http://127.0.0.1:$DATARUN_MANAGEMENT_HOST_PORT/actuator/health/readiness" \
  | tee "$DATARUN_EVIDENCE_DIR/readiness-current.json"
curl --fail --silent --show-error \
  "http://127.0.0.1:$DATARUN_MANAGEMENT_HOST_PORT/actuator/prometheus" \
  > "$DATARUN_EVIDENCE_DIR/prometheus.txt"
docker compose -f deploy/reference/compose.yaml logs --no-color --since 10m server \
  > "$DATARUN_EVIDENCE_DIR/server-recent.log"
```

Execute the approved alert test for readiness, database connectivity, host/DB
capacity, backup freshness, certificate expiry, and OIDC/JWKS failure. Verify
70%/15-minute warnings, 85%/10-minute escalation, readiness down for 5
minutes, RPO freshness, and certificate 30/14/7-day handling as applicable.

Expected: structured secret-safe logs, bounded health, Prometheus scrape,
dashboard data, and alerts delivered to Hamza with action/runbook links.

**Stop:** false healthy state, missing required signal, undelivered alert,
unowned route, or secret/token content in telemetry. A missing destination or
recipient blocks rehearsal pass.

## 12. Backup And Clean Restore

Prerequisites: approved concrete backup/PITR adapter meeting continuous
PITR-or-equivalent, daily, 35-day daily/12-month monthly, encrypted, protected
off-site requirements; known synthetic dataset; isolated empty restore target;
and Hamza's recorded restore authorization.

1. Stop all synthetic writers and wait for in-flight requests to finish.
   Capture the complete frozen source state and exact mounted
   `principal-bindings.json` hash. Record the database clock after the freeze:

```bash
export DATARUN_FREEZE_COMPLETED_AT="$(
  psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
    --no-align --tuples-only \
    --command='SELECT clock_timestamp();'
)"
sha256sum "$DATARUN_PROVISIONING_DIR/principal-bindings.json" \
  > "$DATARUN_EVIDENCE_DIR/frozen-principal-bindings.sha256"
```

2. Execute the approved recovery-point procedure after the freeze. Wait until
   the backup adapter reports a latest recoverable timestamp at or after
   `DATARUN_FREEZE_COMPLETED_AT`.
3. Record the controlled disaster declaration and set the selected target to
   that verified latest recoverable timestamp:

```bash
export DATARUN_DISASTER_DECLARED_AT='<ISO-8601 UTC declaration time>'
export DATARUN_RECOVERY_TARGET_UTC='<ISO-8601 UTC latest recoverable time>'
```

4. Confirm the recovery target is not before the completed freeze and the
   declared time minus latest recoverable time is no more than the 1-hour RPO.
   This age is the RPO measurement; RPO is not an elapsed timer.
5. Start the RTO timer at the recorded disaster declaration, inject the
   approved source-loss condition, and execute the approved restore/PITR
   procedure into the isolated target.
6. Before starting an application against the restored target, compare schema,
   complete event ledger, config, binding audit, and assignment evidence to the
   captured frozen state.
7. Point a fresh copy of the reference deployment at the restored target, run
   full preflight, and start the same image digest with the exact frozen
   principal-binding manifest.
8. Verify readiness, `/api/auth/me`, config retrieval, and an authorized pull.
   Do not rerun config publication, assignment bootstrap, assignment commands,
   or push fixtures against the restored database. Confirm normal startup did
   not append or change principal-binding operations.
9. Stop the RTO timer only after those minimum-service checks pass.

Capture and compare with read-only queries:

```bash
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --set=disaster_declared_at="$DATARUN_DISASTER_DECLARED_AT" \
  --set=recovery_target_utc="$DATARUN_RECOVERY_TARGET_UTC" \
  --set=freeze_completed_at="$DATARUN_FREEZE_COMPLETED_AT" \
  --no-align --tuples-only \
  --command="SELECT :'freeze_completed_at'::timestamptz AS freeze_completed_at, :'disaster_declared_at'::timestamptz AS disaster_declared_at, :'recovery_target_utc'::timestamptz AS latest_recoverable_at, EXTRACT(EPOCH FROM (:'disaster_declared_at'::timestamptz - :'recovery_target_utc'::timestamptz))::bigint AS recovery_point_age_seconds, :'recovery_target_utc'::timestamptz >= :'freeze_completed_at'::timestamptz AND (:'disaster_declared_at'::timestamptz - :'recovery_target_utc'::timestamptz) BETWEEN interval '0 seconds' AND interval '1 hour' AS rpo_met;" \
  | tee "$DATARUN_EVIDENCE_DIR/rpo-measurement.txt"
grep -Eq '\|t$' "$DATARUN_EVIDENCE_DIR/rpo-measurement.txt"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command='SELECT COUNT(*) AS frozen_event_count, COALESCE(MAX(sync_watermark), 0) AS frozen_max_watermark FROM events;' \
  > "$DATARUN_EVIDENCE_DIR/source-frozen-boundary.txt"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT version, package_json::text, published_at, published_by FROM config_packages ORDER BY version;' \
  > "$DATARUN_EVIDENCE_DIR/source-config-packages.txt"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT id, operation_id, operation_hash, manifest_version, manifest_source, manifest_content_hash, applied_at, applied_by, issuer, subject, target_actor_id, desired_active, reason, previous_active_binding_id, previous_actor_id, resulting_binding_id, changed FROM auth_principal_binding_operations ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/source-binding-operations.txt"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT id, issuer, subject, actor_id, active, created_at, deactivated_at FROM auth_principal_bindings ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/source-principal-bindings.txt"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT installed_rank, version, description, type, script, checksum, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;' \
  > "$DATARUN_EVIDENCE_DIR/source-flyway-schema-history.txt"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT id, type, shape_ref, activity_ref, subject_ref::text, actor_ref::text, device_id, device_seq, sync_watermark, timestamp, payload::text, received_at FROM events ORDER BY sync_watermark;' \
  > "$DATARUN_EVIDENCE_DIR/source-frozen-event-ledger.txt"
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command='SELECT clock_timestamp() AS observed_at, COUNT(*) AS event_count, COALESCE(MAX(sync_watermark), 0) AS max_watermark FROM events;'
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT version, package_json::text, published_at, published_by FROM config_packages ORDER BY version;' \
  > "$DATARUN_EVIDENCE_DIR/restore-config-packages.txt"
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT id, operation_id, operation_hash, manifest_version, manifest_source, manifest_content_hash, applied_at, applied_by, issuer, subject, target_actor_id, desired_active, reason, previous_active_binding_id, previous_actor_id, resulting_binding_id, changed FROM auth_principal_binding_operations ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/restore-binding-operations.txt"
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT id, issuer, subject, actor_id, active, created_at, deactivated_at FROM auth_principal_bindings ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/restore-principal-bindings.txt"
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT installed_rank, version, description, type, script, checksum, installed_on, success FROM flyway_schema_history ORDER BY installed_rank;' \
  > "$DATARUN_EVIDENCE_DIR/restore-flyway-schema-history.txt"
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT id, type, shape_ref, activity_ref, subject_ref::text, actor_ref::text, device_id, device_seq, sync_watermark, timestamp, payload::text, received_at FROM events ORDER BY sync_watermark;' \
  > "$DATARUN_EVIDENCE_DIR/restore-event-ledger.txt"
sha256sum \
  "$DATARUN_EVIDENCE_DIR/source-frozen-event-ledger.txt" \
  "$DATARUN_EVIDENCE_DIR/restore-event-ledger.txt" \
  > "$DATARUN_EVIDENCE_DIR/event-ledger-sha256.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/source-frozen-event-ledger.txt" \
  "$DATARUN_EVIDENCE_DIR/restore-event-ledger.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/event-ledger-diff.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/source-config-packages.txt" \
  "$DATARUN_EVIDENCE_DIR/restore-config-packages.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/config-packages-diff.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/source-binding-operations.txt" \
  "$DATARUN_EVIDENCE_DIR/restore-binding-operations.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/binding-operations-diff.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/source-principal-bindings.txt" \
  "$DATARUN_EVIDENCE_DIR/restore-principal-bindings.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/principal-bindings-diff.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/source-flyway-schema-history.txt" \
  "$DATARUN_EVIDENCE_DIR/restore-flyway-schema-history.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/flyway-schema-history-diff.txt"
```

After the comparison, run:

```bash
sha256sum "$DATARUN_PROVISIONING_DIR/principal-bindings.json" \
  > "$DATARUN_EVIDENCE_DIR/restore-principal-bindings.sha256"
diff -u \
  "$DATARUN_EVIDENCE_DIR/frozen-principal-bindings.sha256" \
  "$DATARUN_EVIDENCE_DIR/restore-principal-bindings.sha256" \
  | tee "$DATARUN_EVIDENCE_DIR/principal-bindings-input-diff.txt"
deploy/reference/preflight.sh \
  | tee "$DATARUN_EVIDENCE_DIR/restore-preflight.txt"
docker compose -f deploy/reference/compose.yaml up -d server
curl --fail --silent --show-error \
  "http://127.0.0.1:$DATARUN_MANAGEMENT_HOST_PORT/actuator/health/readiness" \
  | tee "$DATARUN_EVIDENCE_DIR/restore-readiness.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/auth/me" \
  | tee "$DATARUN_EVIDENCE_DIR/restore-auth-me.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/config" \
  --output "$DATARUN_EVIDENCE_DIR/restore-config.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_PROVISIONING_DIR/smoke-pull-request.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/pull" \
  | tee "$DATARUN_EVIDENCE_DIR/restore-pull.json"
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT id, operation_id, operation_hash, manifest_version, manifest_source, manifest_content_hash, applied_at, applied_by, issuer, subject, target_actor_id, desired_active, reason, previous_active_binding_id, previous_actor_id, resulting_binding_id, changed FROM auth_principal_binding_operations ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/restore-binding-operations-after-start.txt"
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --no-align --tuples-only --field-separator='|' \
  --command='SELECT id, issuer, subject, actor_id, active, created_at, deactivated_at FROM auth_principal_bindings ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/restore-principal-bindings-after-start.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/restore-binding-operations.txt" \
  "$DATARUN_EVIDENCE_DIR/restore-binding-operations-after-start.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/restore-startup-binding-diff.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/restore-principal-bindings.txt" \
  "$DATARUN_EVIDENCE_DIR/restore-principal-bindings-after-start.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/restore-startup-active-binding-diff.txt"
export DATARUN_MINIMUM_SERVICE_RESTORED_AT="$(
  date --utc +%Y-%m-%dT%H:%M:%SZ
)"
psql "service=$DATARUN_RESTORE_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --set=disaster_declared_at="$DATARUN_DISASTER_DECLARED_AT" \
  --set=minimum_service_restored_at="$DATARUN_MINIMUM_SERVICE_RESTORED_AT" \
  --no-align --tuples-only \
  --command="SELECT :'disaster_declared_at'::timestamptz AS disaster_declared_at, :'minimum_service_restored_at'::timestamptz AS minimum_service_restored_at, EXTRACT(EPOCH FROM (:'minimum_service_restored_at'::timestamptz - :'disaster_declared_at'::timestamptz))::bigint AS rto_seconds, (:'minimum_service_restored_at'::timestamptz - :'disaster_declared_at'::timestamptz) BETWEEN interval '0 seconds' AND interval '8 hours' AS rto_met;" \
  | tee "$DATARUN_EVIDENCE_DIR/rto-measurement.txt"
grep -Eq '\|t$' "$DATARUN_EVIDENCE_DIR/rto-measurement.txt"
```

Expected: `rpo_met` is true, the complete frozen source event ledger matches
the restored ledger exactly, the manifest hash is unchanged, startup appends
no binding operation, minimum usable service returns within 8 hours, and
restored health/auth/config/authorized-pull checks pass without one-shot
provisioning or event append.

**Stop and fail rehearsal:** failed restore, exceeded RPO/RTO, invalid Flyway
history, missing event/config/binding/assignment evidence, authority drift, or
manual repair requirement. Preserve the failed target and evidence; do not
promote it.

## 13. Upgrade And Post-Upgrade Verification

Prerequisites: previous accepted image/database, candidate approval and
preflight, recovery point within RPO, explicit schema compatibility review,
and maintenance authorization.

1. Record current image, schema history, health, smoke results, and recovery
   point.
2. Set `DATARUN_IMAGE`, source, and revision to the approved candidate.
3. Run Section 4 and the full `deploy/reference/preflight.sh` check from
   Section 5, then apply the candidate with:

```bash
docker compose -f deploy/reference/compose.yaml up -d server
docker compose -f deploy/reference/compose.yaml ps
docker compose -f deploy/reference/compose.yaml logs --no-color --since 10m server \
  > "$DATARUN_EVIDENCE_DIR/upgrade-startup.log"
```

4. Rerun migration evidence, health, auth, config, push/pull, telemetry, and
   capacity checks.

Expected: only forward Flyway migrations occur; old events/config remain
interpretable; the current paths work; outage stays within the approved
maintenance decision.

**Stop:** failed migration/startup, contract drift, failed smoke, or unknown
old-image compatibility. Remove traffic and use Section 14.

## 14. Failure, Rollback, Restore, Or Forward Fix

On failed startup or migration:

```bash
docker compose -f deploy/reference/compose.yaml ps
docker compose -f deploy/reference/compose.yaml logs --no-color server \
  > "$DATARUN_EVIDENCE_DIR/failure-server.log"
```

Record the last successful stage, schema history, traffic state, recovery-point
identity, and incident severity. Then choose exactly one path:

- **Application rollback:** permitted only when retained tests or an explicit
  compatibility statement prove the older image can safely use the current
  schema/data. Set the approved old digest/source/revision, rerun preflight,
  deploy, and repeat post-deploy smoke.
- **Database restore:** Hamza records disaster/restore authority and accepted
  loss boundary, then executes Section 12's deployment-owned restore procedure.
- **Forward fix:** preserve the database and deploy a newly tested immutable
  image through the full release/preflight path.

Flyway is forward-only. There is no supported down migration. Never edit
schema history or describe an unproven old-image start as rollback.

## 15. Credential, Binding, And JWKS Rotation

Use synthetic credentials and the approved rotation adapters.

- Database credential: create the replacement with least privilege, populate
  the mounted secret through the approved mechanism, restart the service,
  verify readiness/smoke, then revoke the old credential and prove rejection.
- Principal binding: apply reviewed deactivate/rebind operations with the
  Section 7 one-shot command, reapply for idempotency, inspect audit, then
  verify old/new principal behavior.
- Provider signing key/JWKS: add/activate the new provider key, verify a
  new-key token, retire the old key per provider policy, verify expected old
  token/key behavior, and confirm explicit actor binding is unchanged.
- Other application secret/certificate: use the selected secret/TLS adapter,
  verify the new value, revoke/retire the old value, and test expiry alerts.

Expected: new material works, old material fails at the intended time, outage
meets policy, and no claim becomes platform authority.

**Stop:** loss of access without recovery path, old credential unexpectedly
valid, actor/binding drift, unbounded outage, or leaked value. Invoke incident
and emergency revocation procedures.

## 16. Incident, Escalation, And Handoff

Declare severity using the policy. Authority drift, cross-scope exposure,
secret leakage, event mutation, or unsafe migration is severity 1 regardless
of apparent availability.

1. Record declaration time, commander, affected target, symptoms, and current
   traffic state.
2. Preserve secret-safe logs, metrics, image/schema/recovery identity, and the
   timeline.
3. Contain unsafe access or traffic using the approved network/secret adapter.
4. Select rollback, restore, or forward fix through Section 14 authority.
5. Execute the approved communication route at the policy cadence.
6. Hand the indexed policy, this runbook, execution record, evidence index,
   current state, next decision, and stop conditions to the next authorized
   operator.

For NW-067, a second authorized operator must independently locate state,
execute smoke checks, find evidence, and explain stop/escalation conditions.
The one-person approval exception does not prove operator handoff by itself.

**Stop:** no reachable owner, shared credential required, undocumented
knowledge required, or unclear recovery authority. Record the scenario as
partial/failed.

## 17. Shutdown, Cleanup, Evidence, And Follow-Up

For a disposable rehearsal target:

```bash
docker compose -f deploy/reference/compose.yaml stop server
docker compose -f deploy/reference/compose.yaml ps
docker compose -f deploy/reference/compose.yaml down
```

Then execute approved adapters to revoke test tokens/credentials/access,
remove temporary DNS/TLS and secret material, expire temporary operator
access, and destroy synthetic source/restore environments only after evidence
retention is confirmed.

Retain approvals, digests, sanitized commands/output, timings, schema/config/
binding/assignment evidence, recovery decisions, alert delivery, incident
timeline, handoff result, exceptions, and cleanup for 13 months. Keep raw
evidence access-controlled and out of git. Commit only a sanitized dated
rehearsal record.

Route every defect or missing adapter to a separate bounded NW item. A partial
or failed rehearsal must not strengthen production-readiness claims.

## 18. Recovery Decision Summary

| Condition | Allowed response |
|---|---|
| App failure, schema unchanged or explicitly backward compatible | Proven application rollback or forward fix |
| Migration applied; old image compatibility unknown | Forward fix or authorized database restore |
| Database corruption or accepted disaster recovery | Authorized restore/PITR with measured loss and RTO |
| Failed migration with no safe tested recovery | Stop traffic, preserve evidence, restore or forward fix |
| Secret or authority compromise | Contain/revoke, severity-1 response, then tested recovery |

This runbook does not approve real production, Kubernetes, a managed provider,
production web administration, mobile OAuth/OIDC login, or NW-054 device
security behavior.
