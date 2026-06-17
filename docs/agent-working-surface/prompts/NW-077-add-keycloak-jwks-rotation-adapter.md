# NW-077 Agent Prompt: Add Keycloak/JWKS Rotation Adapter

You are Worker NW-077 in `/home/hamza/datarun-platform`.

You are not alone in the codebase or lab. Do not revert unrelated repository
changes. Do not mutate Keycloak, Datarun, or secrets until the read-only
precheck is complete, the recovery path is explicit, and Hamza has approved
the rotation window.

## Goal

Close the NW-067 JWKS rotation blocker by adding and exercising a reviewed
synthetic Keycloak signing-key rotation procedure that preserves explicit
principal-to-actor binding authority.

## Current Gap

NW-067 R10 is blocked because Section 15 of the production runbook names the
provider signing-key/JWKS requirement, but no reviewed Keycloak adapter exists.
The server accepts asymmetric OIDC tokens only after issuer, audience, and JWKS
validation, then resolves actor authority from the explicit
`(issuer, subject) -> actor_id` binding table. The missing piece is an
operator-safe Keycloak signing-key procedure that proves new-key acceptance,
expected old-key/token behavior, and unchanged principal binding without
turning IdP groups, roles, claims, or JWT `actor_id` into authority.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/operations/policies/first-reference-deployment-policy.md`
   Section 7
5. `docs/operations/runbooks/production-deployment-runbook.md` Sections 3,
   7, 10, and 15
6. `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
7. `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
8. `docs/agent-working-surface/platform-next-work-backlog.md` NW-077 row
9. `deploy/reference/README.md`
10. `server/src/main/java/dev/datarun/server/authorization/OidcJwksTokenValidator.java`
11. `server/src/main/java/dev/datarun/server/authorization/AuthenticatedActorResolver.java`

## Safest Execution Route

Use a synthetic Keycloak realm/client/user and short-lived synthetic tokens.

1. Capture read-only Keycloak container/admin surface, realm ID, current key
   providers, active signing `kid`, JWKS metadata, Datarun configured issuer,
   audience, and JWKS URI.
2. Confirm the existing bound synthetic principal still resolves through
   `/api/auth/me` before key changes.
3. Add a new generated RSA signing-key provider with higher priority than the
   existing active provider. This should make new tokens use the new `kid`.
4. Acquire a new synthetic token through the existing secret-safe token
   adapter and verify Datarun accepts it.
5. Make the old key passive or disabled according to the selected provider
   policy, then prove old-token behavior. If Datarun accepts the old token
   only because JWKS cache or token-validity overlap is expected, record the
   policy window; otherwise stop.
6. Confirm the active principal-binding rows and `/api/auth/me` actor result
   are unchanged after rotation.

Keycloak's documented rotation model is priority-based: new keys with higher
priority sign new tokens, passive keys may still verify old signatures, and
old keys are removed or disabled after the chosen overlap window. Do not force
an immediate old-token failure unless the provider policy explicitly disables
or removes the old key and the application cache behavior is accounted for.

## Read-Only Remote Precheck

Run these from a network that can reach the lab. Keep outputs secret-safe.

```bash
ssh -o BatchMode=yes nmcp@192.168.1.213 \
  'set -euo pipefail
   container="$(docker ps \
     --filter label=com.docker.compose.project=datarun-reference \
     --filter label=com.docker.compose.service=server \
     --format "{{.Names}}" | head -n1)"
   test -n "$container"
   docker inspect "$container" \
     --format "name={{.Name}} image={{.Image}} status={{.State.Status}}"
   docker inspect "$container" --format "{{json .Mounts}}" |
     jq -r ".[] | [.Source,.Destination,.RW] | @tsv"'
```

On the Keycloak host or container, inspect without changing state:

```bash
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
docker exec "$KEYCLOAK_CONTAINER" /opt/keycloak/bin/kcadm.sh get realms/datarun \
  --fields id,realm,enabled
docker exec "$KEYCLOAK_CONTAINER" /opt/keycloak/bin/kcadm.sh get keys -r datarun \
  --fields kid,type,algorithm,status,providerId,providerPriority
curl --fail --silent --show-error "$DATARUN_OIDC_JWKS_URI" |
  jq '{key_count:(.keys|length), kids:[.keys[].kid]}'
```

Do not print admin passwords, client secrets, bearer tokens, private keys, or
full JWK material. Record only key IDs, algorithms, provider IDs, priorities,
statuses, and timestamps.

## Adapter To Create

Create the lab-only adapter outside git unless the procedure becomes reusable
operator documentation:

```text
/opt/datarun-lab/adapters/nw077-keycloak-jwks-rotation.sh
```

The adapter should wrap `kcadm.sh` from the Keycloak container and read admin
credentials from the approved secret mechanism. It must not take admin
passwords, client secrets, tokens, private keys, or generated signing material
in command-line arguments, environment variables, committed files, or evidence
files.

The Keycloak Admin CLI operation for adding a generated signing key should be
the equivalent of:

```bash
realm_id="$(
  docker exec "$KEYCLOAK_CONTAINER" /opt/keycloak/bin/kcadm.sh \
    get realms/datarun --fields id --format csv --noquotes
)"
docker exec "$KEYCLOAK_CONTAINER" /opt/keycloak/bin/kcadm.sh create components \
  -r datarun \
  -s name=nw077-rsa-generated \
  -s providerId=rsa-generated \
  -s providerType=org.keycloak.keys.KeyProvider \
  -s parentId="$realm_id" \
  -s 'config.priority=["101"]' \
  -s 'config.enabled=["true"]' \
  -s 'config.active=["true"]' \
  -s 'config.keySize=["2048"]'
```

Before using priority `101`, inspect current priorities and choose a reviewed
higher value. If the realm uses non-RSA signing or FIPS-specific providers,
stop and route the provider-specific variant instead of improvising.

To stop signing with the old provider while retaining verification overlap,
make the old provider passive:

```bash
docker exec "$KEYCLOAK_CONTAINER" /opt/keycloak/bin/kcadm.sh update \
  "components/$OLD_PROVIDER_ID" -r datarun \
  -s 'config.active=["false"]'
```

To prove old-key rejection after the reviewed overlap, disable the old
provider:

```bash
docker exec "$KEYCLOAK_CONTAINER" /opt/keycloak/bin/kcadm.sh update \
  "components/$OLD_PROVIDER_ID" -r datarun \
  -s 'config.enabled=["false"]'
```

If Datarun continues accepting a token signed by a disabled provider because
the JWKS cache still contains the old key, either wait for the documented cache
boundary or restart the Datarun service and record that restart as part of the
adapter. Do not change issuer, audience, JWKS URI, or binding semantics.

## Commands To Run

Use the accepted runbook environment variables. Keep `set -x` disabled.

```bash
export DATARUN_EVIDENCE_DIR=/opt/datarun-lab/evidence/NW-077-$(date -u +%Y%m%dT%H%M%SZ)
install -d -m 0750 "$DATARUN_EVIDENCE_DIR"

curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/auth/me" \
  | tee "$DATARUN_EVIDENCE_DIR/jwks-before-auth-me.json"
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command='SELECT id, issuer, subject, actor_id, active, created_at, deactivated_at FROM auth_principal_bindings ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/jwks-before-principal-bindings.txt"
```

Run the adapter phases:

```bash
/opt/datarun-lab/adapters/nw077-keycloak-jwks-rotation.sh inspect \
  --realm datarun \
  --evidence-dir "$DATARUN_EVIDENCE_DIR"

/opt/datarun-lab/adapters/nw077-keycloak-jwks-rotation.sh add-active-key \
  --realm datarun \
  --provider-name nw077-rsa-generated \
  --evidence-dir "$DATARUN_EVIDENCE_DIR"

# Refresh the protected curl config through the existing token adapter.
# The token value must remain only in the 0600 curl config.
test "$(stat -c '%a' "$DATARUN_AUTH_CURL_CONFIG")" = '600'

curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/auth/me" \
  | tee "$DATARUN_EVIDENCE_DIR/jwks-new-key-auth-me.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/config" \
  --output "$DATARUN_EVIDENCE_DIR/jwks-new-key-config.json"
curl --config "$DATARUN_AUTH_CURL_CONFIG" --fail --silent --show-error \
  --header 'Content-Type: application/json' \
  --data-binary @"$DATARUN_PROVISIONING_DIR/smoke-pull-request.json" \
  "$DATARUN_PUBLIC_BASE_URL/api/sync/pull" \
  | tee "$DATARUN_EVIDENCE_DIR/jwks-new-key-pull.json"
```

Then apply the reviewed old-key policy and prove behavior:

```bash
/opt/datarun-lab/adapters/nw077-keycloak-jwks-rotation.sh retire-old-key \
  --realm datarun \
  --old-provider-id "$OLD_PROVIDER_ID" \
  --mode passive-or-disabled \
  --evidence-dir "$DATARUN_EVIDENCE_DIR"

/opt/datarun-lab/adapters/nw077-keycloak-jwks-rotation.sh prove-old-token-behavior \
  --old-curl-config "$DATARUN_OLD_KEY_CURL_CONFIG" \
  --expected accepted-during-overlap-or-401-after-disable \
  --evidence-dir "$DATARUN_EVIDENCE_DIR"
```

Finally confirm binding stability:

```bash
psql "service=$DATARUN_PGSERVICE" -X --set ON_ERROR_STOP=1 \
  --command='SELECT id, issuer, subject, actor_id, active, created_at, deactivated_at FROM auth_principal_bindings ORDER BY id;' \
  > "$DATARUN_EVIDENCE_DIR/jwks-after-principal-bindings.txt"
diff -u \
  "$DATARUN_EVIDENCE_DIR/jwks-before-principal-bindings.txt" \
  "$DATARUN_EVIDENCE_DIR/jwks-after-principal-bindings.txt" \
  | tee "$DATARUN_EVIDENCE_DIR/jwks-principal-bindings-diff.txt"
```

The diff must be empty unless the exercise deliberately includes a separately
reviewed principal-binding operation. NW-077 by itself should not require one.

## Evidence To Retain

Retain outside git:

- Keycloak image/version, container identity, realm ID, and admin-surface path;
- current and new key IDs, algorithms, provider IDs, priority, active/enabled
  status, and JWKS key-count metadata, but no private/public JWK body;
- token metadata hashes or decoded header/claims with signature and token
  values redacted;
- `/api/auth/me`, `/api/sync/config`, and authorized pull smoke with the
  new-key token;
- old-key passive/disabled action and old-token expected behavior proof;
- before/after principal-binding rows and empty diff;
- outage/cache timing, cleanup state, and any residual risk.

## Repo Docs To Patch

Patch only docs unless a separate implementation task is approved.

- Update `docs/operations/runbooks/production-deployment-runbook.md` Section
  15 only if this creates a reusable operator procedure beyond the existing
  generic text.
- Create a dated record under `docs/operations/rehearsals/` only after an
  actual exercise exists and summarize secret-safe evidence paths.
- Update `docs/agent-working-surface/platform-next-work-backlog.md`,
  `docs/status.md`, and `docs/operations/README.md` only when NW-077 standing
  changes.
- NW-070 durable auth-spec extraction is not required unless this work changes
  accepted auth behavior.

## Acceptance

NW-077 can be accepted only when evidence shows:

- a new synthetic provider signing key is added and activated;
- a token signed by the new key is accepted by the Datarun service;
- old-key or old-token behavior is tested and matches the provider policy;
- explicit `(issuer, subject) -> actor_id` binding remains the only platform
  actor authority;
- groups, roles, resource claims, and JWT `actor_id` remain non-authority;
- no signing key, token, password, or private certificate is committed.

## Guardrails

- Do not use IdP groups, roles, claims, or JWT `actor_id` as platform
  authority.
- Do not leave import-on-start or generated provider secrets exposed after the
  exercise.
- Do not weaken issuer, audience, JWKS URI, or principal-binding validation.
- Do not create online binding-admin APIs, mobile OIDC login UX, or new actor
  authority sources.
- NW-070 durable auth-spec extraction is not required unless this work changes
  accepted auth behavior; stop and route if it would.

## Verification

Run `git diff --check` for repository changes. Verify valid new-key token
behavior, expected old-key/token behavior, `/api/auth/me`, config, and sync
smoke. Verify evidence contains only redacted token metadata or hashes.

## Commit Flow

Runbook or routing changes use:

```text
docs(ops): route jwks rotation

NW: NW-077
```

Executed evidence uses:

```text
test(ops): prove jwks rotation

NW: NW-077
```

## Stop And Report

Stop if the provider cannot rotate keys through a reviewed adapter, Keycloak
admin access is unavailable or unreviewed, actor binding drifts, old signing
material remains active unexpectedly, Datarun accepts or rejects old tokens in
a way that contradicts the selected provider/cache policy, evidence would
expose tokens or keys, or the only path requires changing accepted auth
authority.
