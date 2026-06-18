Status: accepted
Document type: rehearsal_record
Owner: Hamza
Source: NW-081; `docs/agent-working-surface/prompts/NW-081-document-fresh-session-protected-smoke-token-path.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, `docs/specifications/platform/production-auth-principal-binding.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`, `docs/operations/rehearsals/2026-06-17-keycloak-jwks-rotation-adapter.md`, and `docs/operations/rehearsals/2026-06-17-encrypted-backup-pitr-adapter.md`; raw evidence at `keycloak.lab:/opt/datarun-lab/evidence/NW-081-2026-06-18`

# 2026-06-18 Fresh-Session Protected-Smoke Token Path Evidence

## Result

Final result: `accepted`.

NW-081 documented and proved a fresh-session, secret-safe token acquisition
path for the active rotated synthetic principal. The path uses approved SSH
access to `keycloak.lab`, the existing root-owned synthetic password file, and
Keycloak direct grant for client `datarun-cli`. The critical detail is to strip
CR/LF into a temporary file before direct grant; passing the newline-bearing
password file directly returns `invalid_grant`.

The proof acquired a short-lived bearer token without retaining raw token
material, preserved only redacted JWT metadata, and successfully ran protected
smoke against the live reference app:

- `/api/auth/me`: HTTP 200, actor
  `22222222-2222-4222-8222-222222222222`, `auth_source=oidc-jwks-principal`
- `/api/sync/config`: HTTP 200, config version 1
- `/api/sync/pull`: HTTP 200, config version 1, `has_more=false`, event count
  0

This record does not accept NW-067 or create real-production token-handling
policy.

## Environment

Raw evidence root: `keycloak.lab:/opt/datarun-lab/evidence/NW-081-2026-06-18`.

Active synthetic principal:

- username: `field-worker-rotated`
- issuer: `https://keycloak.lab/realms/datarun-lab`
- subject: `53b46770-c03a-4f3f-b25c-3321c1e5af15`
- explicit actor binding: `22222222-2222-4222-8222-222222222222`

## Evidence Summary

| Check | Result | Evidence |
|---|---|---|
| Principal binding | Pass | `active-principal-binding.json` records the expected rotated subject and actor binding. |
| Initial failure mode | Pass | `token-acquisition-debug-redacted.json` records the failed `invalid_grant` path without token retention. |
| Proven token acquisition | Pass | `token-acquisition-retained-password-check.json`, `token-acquisition-redacted.json`, and `token-metadata-redacted.json` show HTTP 200 direct grant, expected subject, RS256 token metadata, and `ttl_seconds_at_capture=300`, without retaining raw token material. |
| Auth smoke | Pass | `auth-me-result.json` records HTTP 200, actor match, and `auth_source=oidc-jwks-principal`. |
| Config smoke | Pass | `sync-config-result.json` records HTTP 200 and config version 1. |
| Pull smoke | Pass | `sync-pull-request.json` and `sync-pull-result.json` record authorized pull returning HTTP 200, config version 1, `has_more=false`, and zero events for the fresh request. |
| Procedure note | Pass | `procedure-note.md` records the fresh-session path and explicitly forbids retaining raw bearer tokens, refresh tokens, passwords, client secrets, private keys, or token-bearing URLs. |
| Cleanup and secret hygiene | Pass | `token-cleanup-proof.txt` records temporary `/dev/shm` token directory removal; `secret-scan-output.txt` reports no compact JWT, token JSON keys, bearer header, or private key material in retained evidence. |

## Deviations

- The retained rotated-worker password file was not stale, and no Keycloak
  password reset was performed. The earlier blocker was caused by sending the
  retained file's trailing newline as part of the direct-grant password.
- This path is accepted only for the synthetic rehearsal principal and does not
  add IdP claim authority, online principal-binding administration, group/role
  authority, or a retained-token workaround.

## Cleanup State

- Temporary token material under `/dev/shm/nw081-token.jhjeZz` was removed.
- No raw bearer token, refresh token, password, admin token, client secret,
  private key, or bearer URL was retained in evidence.

## Follow-Up Work

- NW-067 still requires a genuinely fresh privileged Hamza R12 rerun after
  NW-080 and NW-081 are both accepted.
