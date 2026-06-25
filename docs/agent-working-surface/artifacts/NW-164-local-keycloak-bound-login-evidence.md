# NW-164 Local Keycloak Bound Login Evidence

Status: accepted implementation evidence
Date: 2026-06-26

## Outcome

NW-164 is complete. Existing lab deployments were preserved, an isolated local
Keycloak issuer was deployed and configured, one fresh Hamza-owned pilot account
was bound to a Datarun actor through explicit principal binding, and both live
web-admin login plus mobile public-client PKCE `/api/auth/me` actor resolution
were proven against the live issuer.

Successor: No successor selected.

## Stable Runtime Names

NW-164 appears only in evidence/change identifiers. Runtime resources use
stable names:

- Keycloak compose project/container: `datarun-local-keycloak`
- Realm: `datarun-local`
- Web client: `datarun-web-admin`
- Mobile client: `datarun-mobile`
- Datarun compose project: `datarun-local-app`
- Pilot user: `hamza-pilot`

## Lab Deployment Standing

Retained deployments were not mutated:

- `datarun-reference-server-1` on `datarun-app.lab` remained healthy at
  `127.0.0.1:18080/18081`.
- Existing `datarun-keycloak` on `keycloak.lab:443` remained up and returned
  OIDC discovery HTTP 200.

New isolated deployments:

- `datarun-local-keycloak` on `192.168.1.217:28443`.
- `datarun-local-app` on `192.168.1.213:28443`, management on
  `127.0.0.1:28481`.

Final health checks:

- Reference app readiness: `{"status":"UP"}`.
- Local app readiness: `{"status":"UP"}`.
- Existing Keycloak discovery: HTTP 200.
- Local Keycloak discovery: HTTP 200.

## Provider And Clients

Issuer:

```text
https://keycloak.lab:28443/realms/datarun-local
```

Safe Keycloak evidence on `keycloak.lab`:

```text
/home/nmcp/datarun-local-keycloak/evidence/NW-164/realm-summary.txt
/home/nmcp/datarun-local-keycloak/evidence/NW-164/kcadm-safe-output.txt
/home/nmcp/datarun-local-keycloak/evidence/NW-164/openid-configuration.json
```

Committed secret-free configuration material:

```text
deploy/reference/local-keycloak/README.md
```

Configured values:

- Realm: `datarun-local`
- Web client: `datarun-web-admin`
- Mobile client: `datarun-mobile`
- Pilot username: `hamza-pilot`
- Pilot subject: `7c346f07-f928-4958-bb0e-3de5360d89ef`
- Web client redirect: `https://datarun-app.lab:28443/web-admin/oidc/callback`
- Mobile redirect: `dev.datarun.mobile://oauth2redirect`
- Token audience mapper: `datarun-server`

The pilot account profile was completed with non-secret profile fields so
Keycloak did not stop first login at profile-update.

## Datarun Configuration

Safe app evidence on `datarun-app.lab`:

```text
/home/nmcp/datarun-local-app/evidence/NW-164/runtime-summary.txt
/home/nmcp/datarun-local-app/evidence/NW-164/config-inputs.sha256
/home/nmcp/datarun-local-app/evidence/NW-164/provisioning-summary.txt
```

Runtime configuration:

- Auth mode: `oidc-jwks`
- Issuer: `https://keycloak.lab:28443/realms/datarun-local`
- Audience: `datarun-server`
- JWKS URI:
  `https://keycloak.lab:28443/realms/datarun-local/protocol/openid-connect/certs`
- Web-admin OIDC endpoints: live Keycloak authorization and token endpoints.
- Web-admin redirect:
  `https://datarun-app.lab:28443/web-admin/oidc/callback`
- Web-admin client id: `datarun-web-admin`
- Principal-binding operator marker: `operator:nw-164-local-keycloak`
- App serving TLS: `https://datarun-app.lab:28443`
- Database: isolated `datarun-local-app` Postgres volume.

Secrets remain in remote secret/config files and are not recorded in repository
or evidence content.

## Principal Binding And Pilot Config

Bound actor:

```text
15000000-0000-4000-8000-000000000001
```

Provisioning output:

```json
{"command":"principal-bindings","status":"succeeded","operator_id":"7cbd4bbc-2998-482f-ba69-29b25dc545e4","evidence_id":"NW-164:local-keycloak-principal-binding","applied_operations":0,"skipped_operations":1,"changed_operations":0,"input_sha256":"58ac432fdee7ffb5a1110127eb9fc3591eb02fd3088cb1c5591b49437f08a1b1"}
{"command":"config-publish","status":"succeeded","operator_id":"7cbd4bbc-2998-482f-ba69-29b25dc545e4","evidence_id":"NW-164:local-keycloak-reviewed-config","config_version":1,"published":true,"changed_authoring_rows":4,"input_sha256":"7863445b640b4943998945899bb205722636a748a75f2b6807f0bfe318f91047"}
{"command":"assignment-bootstrap","status":"succeeded","operator_id":"7cbd4bbc-2998-482f-ba69-29b25dc545e4","evidence_id":"NW-164:local-keycloak-assignment-bootstrap","assignment_event_id":"3e42ed1a-0705-453a-a915-0ccf82b254a3","created":true,"input_sha256":"2deba6abfbabaec080995ab4a7300beb90401616da1402d73b30b92d05ce59fc"}
```

The principal-binding command reported `skipped_operations=1` because the live
server startup runner had already applied the same explicit manifest from
`/run/datarun/provisioning/principal-bindings.json`. The one-shot command then
proved idempotent operation against the same input.

## Web Login Proof

Safe evidence:

```text
/home/nmcp/datarun-local-app/evidence/NW-164/web-login-proof.json
```

Observed live flow:

1. `GET https://datarun-app.lab:28443/web-admin/login` returned 302 to
   `keycloak.lab:28443`.
2. Keycloak login form returned 200.
3. Pilot credential login returned 302 to
   `https://datarun-app.lab:28443/web-admin/oidc/callback` with query keys
   `code`, `iss`, `session_state`, and `state`; values were not retained.
4. Datarun callback returned 303 to `/web-admin/shell`.
5. `/web-admin/shell` returned 200 and contained the bound actor plus
   `oidc-jwks-principal`.
6. CSRF-protected `/web-admin/session/probe` returned:

```json
{
  "actor_id": "15000000-0000-4000-8000-000000000001",
  "auth_source": "oidc-jwks-principal",
  "session_correlation_id_present": true
}
```

## Mobile/Public Client Proof

Safe evidence:

```text
/home/nmcp/datarun-local-app/evidence/NW-164/mobile-auth-me-proof.json
```

Observed live flow:

1. Public client `datarun-mobile` authorization-code + PKCE login reached the
   Keycloak login form.
2. Pilot credential login returned 302 to
   `dev.datarun.mobile://oauth2redirect` with query keys `code`, `iss`,
   `session_state`, and `state`; values were not retained.
3. Token exchange returned 200. The retained token claims were redacted to:

```json
{
  "iss": "https://keycloak.lab:28443/realms/datarun-local",
  "sub": "7c346f07-f928-4958-bb0e-3de5360d89ef",
  "aud": ["datarun-server", "account"],
  "azp": "datarun-mobile",
  "typ": "Bearer",
  "scope": "openid email profile"
}
```

4. Live `/api/auth/me` with the access token returned:

```json
{
  "actor_id": "15000000-0000-4000-8000-000000000001",
  "auth_source": "oidc-jwks-principal"
}
```

This proves the mobile/client path as far as current mobile support requires:
the external-user-agent public-client PKCE path and live `/api/auth/me`
resolution. No on-device UI runtime was needed for this NW proof.

## Runtime Fix Applied

The existing server image exposed a concrete runtime gap during live execution:
one-shot provisioning with `--spring.main.web-application-type=none` failed
because `WebAdminSecurityFoundationConfig` was loaded outside a servlet web
application and required an unavailable `HttpSecurity` bean.

Fix:

- [server/src/main/java/dev/datarun/server/authorization/WebAdminSecurityFoundationConfig.java](/home/hamza/datarun-platform/server/src/main/java/dev/datarun/server/authorization/WebAdminSecurityFoundationConfig.java)
  now has `@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)`.

Normal web startup remains servlet-only and unchanged. Non-web one-shot
provisioning now starts, as proven by the successful live provisioning commands
above.

The isolated app deployment uses derived image:

```text
datarun-local-server:local-keycloak-conditional-web-security
```

Safe image evidence:

```text
/home/nmcp/datarun-local-app/evidence/NW-164/patched-server-image.txt
```

## Secret Safety

Retained evidence contains no cookies, authorization headers, token values,
authorization codes, client secret values, or passwords.

Checks:

- App evidence scan for `access_token`, `refresh_token`, `id_token`,
  `Authorization`, `Set-Cookie`, `code=`, `client_secret`, `password`,
  `DATARUN_ADMIN_SESSION`, and `Bearer` returned no matches.
- Temporary pilot password file `/tmp/nw164-hamza-pilot-password` was removed.
- Keycloak evidence scan hits were limited to public OIDC discovery metadata
  vocabulary such as supported `id_token` response types and
  `client_secret_*` auth methods, not secret values.

## Validation

Live validation:

- Local Keycloak discovery HTTP 200 at
  `https://keycloak.lab:28443/realms/datarun-local/.well-known/openid-configuration`.
- Local app readiness `{"status":"UP"}` at
  `http://127.0.0.1:28481/actuator/health/readiness`.
- Existing reference app readiness `{"status":"UP"}` at
  `http://127.0.0.1:18081/actuator/health/readiness`.
- Existing retained Keycloak discovery HTTP 200 at `https://keycloak.lab`.
- One-shot provisioning commands returned `exit=0`.
- Web-admin login and session probe returned the expected bound actor and
  `oidc-jwks-principal`.
- Mobile public-client PKCE and `/api/auth/me` returned the expected bound actor
  and `oidc-jwks-principal`.

Local validation:

- `./mvnw clean package -DskipTests` from `server/`: passed.
- `./mvnw -Dtest=OneShotProvisioningIntegrationTest,WebAdminSessionBoundaryTest test`
  was attempted and did not validate in this local environment: the provisioning
  integration context could not connect to its test database, and the web-admin
  boundary tests hit Mockito inline self-attach failure. The live lab proof above
  is the acceptance evidence for this slice.
- `git diff --check`: passed.
