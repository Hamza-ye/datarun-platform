# Local Keycloak Reference Overlay

Status: secret-free NW-164 deployment reference

This overlay records the stable local Keycloak/Datarun configuration proven in
NW-164. It is not a complete compose bundle and intentionally excludes
passwords, client secrets, private keys, cookies, tokens, and authorization
codes.

## Stable Runtime Names

- Keycloak compose project/container: `datarun-local-keycloak`
- Keycloak realm: `datarun-local`
- Web client: `datarun-web-admin`
- Mobile client: `datarun-mobile`
- Datarun compose project: `datarun-local-app`
- Pilot account: `hamza-pilot`

## Lab Endpoints

- Keycloak host: `keycloak.lab` / `192.168.1.217`
- Keycloak HTTPS issuer port: `28443`
- Datarun app host: `datarun-app.lab` / `192.168.1.213`
- Datarun HTTPS app port: `28443`
- Datarun management listener: `127.0.0.1:28481`

The retained reference app and retained lab Keycloak keep their existing ports
and compose projects. This overlay must not mutate those retained deployments.

## OIDC Values

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

`datarun.web-admin.oidc.client-secret` must come from the remote secret file,
not from committed material.

## Principal Binding

```text
issuer=https://keycloak.lab:28443/realms/datarun-local
subject=7c346f07-f928-4958-bb0e-3de5360d89ef
actor_id=15000000-0000-4000-8000-000000000001
state=active
```

The subject is the fresh `hamza-pilot` Keycloak account created for NW-164.
Actor `15000000-0000-4000-8000-000000000001` is the stock-pilot admin actor
from the accepted stock operations pilot package.

## Secret Placement

Remote-only secret files used in the NW-164 proof:

- Keycloak admin password.
- Web client secret.
- Pilot account password.
- Datarun DB password.
- Datarun app TLS private key and keystore password.

Only public certificates, SHA-256 fingerprints, redacted flow summaries, and
successful provisioning result JSON are retained as evidence.

## Evidence

Accepted evidence is recorded at:

```text
docs/agent-working-surface/artifacts/NW-164-local-keycloak-bound-login-evidence.md
```
