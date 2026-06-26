# Local Keycloak Reference Overlay

Status: secret-free NW-164 deployment reference

This overlay records the stable local Keycloak/Datarun configuration proven in
NW-164. It includes executable reference material and intentionally excludes
passwords, client secrets, private keys, cookies, tokens, authorization codes,
and generated provider subjects.

## Runtime Names

- Keycloak compose project/container: `datarun-local-keycloak`
- Keycloak realm: `datarun-local`
- Web client: `datarun-web-admin`
- Mobile client: `datarun-mobile`
- Datarun compose project: `datarun-local-app`
- Pilot account username: `hamza-pilot`

## Service Topology

`compose.yaml` reproduces the NW-164 Keycloak service:

- Image: `quay.io/keycloak/keycloak:26.6.3`
- Pinned digest:
  `sha256:5fdbf2dbb5897cc34e82de49d13e23db011f9925089dbc555fc095f2c8bc1dac`
- Container JVM evidence: Keycloak `26.6.3`, OpenJDK `21.0.11`
- Compose service: one `keycloak` container in project
  `datarun-local-keycloak`
- Database topology: Keycloak `start-dev` embedded database stored in the
  `keycloak-data` Docker volume; no external database service
- TLS binding: `192.168.1.217:28443` mapped to container `8443`
- Certificate/key mount placeholders:
  `${DATARUN_LOCAL_KEYCLOAK_CERT_DIR}/server.crt` and
  `${DATARUN_LOCAL_KEYCLOAK_CERT_DIR}/server.key`

The retained reference app and retained lab Keycloak keep their existing ports
and compose projects. This overlay must not mutate those retained deployments.

## Lab Endpoints

- Keycloak host: `keycloak.lab` / `192.168.1.217`
- Keycloak HTTPS issuer port: `28443`
- Datarun app host: `datarun-app.lab` / `192.168.1.213`
- Datarun HTTPS app port: `28443`
- Datarun management listener: `127.0.0.1:28481`

## Realm And Clients

Realm:

```text
realm=datarun-local
issuer=https://keycloak.lab:28443/realms/datarun-local
```

Web-admin client:

```text
client_id=datarun-web-admin
client_type=confidential
standard_flow_enabled=true
implicit_flow_enabled=false
direct_access_grants_enabled=false
service_accounts_enabled=false
redirect_uri=https://datarun-app.lab:28443/web-admin/oidc/callback
web_origin=https://datarun-app.lab:28443
client_secret_file=<remote secret file>
```

Mobile client:

```text
client_id=datarun-mobile
client_type=public
standard_flow_enabled=true
implicit_flow_enabled=false
direct_access_grants_enabled=false
service_accounts_enabled=false
pkce_code_challenge_method=S256
redirect_uri=dev.datarun.mobile://oauth2redirect
```

Audience mapper on both clients:

```text
name=datarun-server-audience
protocol_mapper=oidc-audience-mapper
included_custom_audience=datarun-server
id_token_claim=true
access_token_claim=true
```

The web-admin login path uses the confidential browser client. The mobile path
uses authorization-code + PKCE through the public client.

## Datarun OIDC Values

```text
datarun.auth.mode=oidc-jwks
datarun.auth.oidc.issuer=https://keycloak.lab:28443/realms/datarun-local
datarun.auth.oidc.audience=datarun-server
datarun.auth.oidc.jwks-uri=https://keycloak.lab:28443/realms/datarun-local/protocol/openid-connect/certs
datarun.web-admin.oidc.authorization-uri=https://keycloak.lab:28443/realms/datarun-local/protocol/openid-connect/auth
datarun.web-admin.oidc.token-uri=https://keycloak.lab:28443/realms/datarun-local/protocol/openid-connect/token
datarun.web-admin.oidc.redirect-uri=https://datarun-app.lab:28443/web-admin/oidc/callback
datarun.web-admin.oidc.client-id=datarun-web-admin
```

`datarun.web-admin.oidc.client-secret` must come from a remote secret file, not
from committed material.

## Provisioning Inputs

`provision-realm.sh` creates or updates the realm, both clients, audience
mapper, and the pilot account. It retrieves the created user's live Keycloak
subject and writes a Datarun principal-binding manifest. Do not hardcode the
generated provider subject as reusable configuration.

Required secret/value placeholders:

```text
KEYCLOAK_ADMIN_PASSWORD_FILE=<remote file>
PILOT_PASSWORD_FILE=<remote file>
WEB_CLIENT_SECRET_FILE=<remote output file>
PRINCIPAL_BINDING_OUT=<remote output file>
DATARUN_BINDING_ACTOR_ID=<proof fixture actor UUID>
```

The generated principal-binding manifest is the input to Datarun's accepted
one-shot `principal-bindings` command. Its `subject` value is per-user runtime
evidence, not reusable configuration.

## Proof Fixture Boundary

NW-164 bound `hamza-pilot` to actor
`15000000-0000-4000-8000-000000000001` and published the stock-pilot config
only as a proof fixture. That fixture does not select stock vocabulary, a
production actor, stock operations product scope, or future pilot product
direction.

## Validation Commands

Secret-free checks:

```bash
cd /home/hamza/datarun-platform
docker compose -f deploy/reference/local-keycloak/compose.yaml config

curl -kfsS \
  https://keycloak.lab:28443/realms/datarun-local/.well-known/openid-configuration \
  >/tmp/datarun-local-keycloak-openid-configuration.json

bash -n deploy/reference/local-keycloak/provision-realm.sh
```

Live setup/provisioning shape:

```bash
export KC_BOOTSTRAP_ADMIN_PASSWORD="$(tr -d '\r\n' < "$KEYCLOAK_ADMIN_PASSWORD_FILE")"
export DATARUN_LOCAL_KEYCLOAK_CERT_DIR=/opt/datarun-lab/keycloak/conf
docker compose -f deploy/reference/local-keycloak/compose.yaml up -d

KEYCLOAK_ADMIN_PASSWORD_FILE=<remote file> \
PILOT_PASSWORD_FILE=<remote file> \
WEB_CLIENT_SECRET_FILE=<remote output file> \
PRINCIPAL_BINDING_OUT=<remote output file> \
DATARUN_BINDING_ACTOR_ID=<proof fixture actor UUID> \
deploy/reference/local-keycloak/provision-realm.sh
```

Datarun validation then applies `PRINCIPAL_BINDING_OUT` through the accepted
one-shot provisioning command and proves:

- Keycloak OIDC discovery returns HTTP 200;
- Datarun readiness is `UP`;
- `/web-admin/login` completes through the live issuer and session-probe
  resolves the bound actor;
- authorization-code + PKCE for `datarun-mobile` exchanges a token that live
  `/api/auth/me` resolves through `oidc-jwks-principal`.

The actual NW-164 observed subject and live proof are retained only in:

```text
docs/agent-working-surface/artifacts/NW-164-local-keycloak-bound-login-evidence.md
```
