Status: accepted
Document type: rehearsal_record
Owner: Hamza
Source: NW-077; `docs/agent-working-surface/prompts/NW-077-add-keycloak-jwks-rotation-adapter.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-17
Supersedes: none
Related: `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`; raw evidence at `keycloak.lab:/opt/datarun-lab/evidence/NW-077-2026-06-17`

# 2026-06-17 Keycloak/JWKS Rotation Adapter Evidence

## Result

Final result: `accepted`.

NW-077 exercised a synthetic Keycloak signing-key rotation for the
`datarun-lab` realm without changing issuer, audience, JWKS URI, or platform
principal-binding authority. The original RS256 provider
`490c6b1a-876d-4335-8c74-bb7c6cbab7d5` at priority `100` was left enabled but
made passive after adding a new higher-priority `rsa-generated` provider named
`nw077-rsa-generated-20260617T034047Z` at priority `110`.

New tokens signed by the new key were accepted by the reference app, and an old
token signed before the rotation remained accepted during the passive-key
overlap window. This matches Keycloak's expected rotation posture: new active
keys sign new tokens, while passive enabled keys may remain in JWKS for old
token verification until the reviewed overlap window ends.

This record does not accept NW-067, prove database-credential rotation,
monitoring/alert delivery, fresh-session R12, independent human continuity, or
real production.

## Environment

Raw evidence root: `keycloak.lab:/opt/datarun-lab/evidence/NW-077-2026-06-17`.

Keycloak host: `keycloak.lab` (`192.168.1.217`). Reference app public URL:
`https://datarun-rehearsal.lab`.

Realm: `datarun-lab`. Issuer:
`https://keycloak.lab/realms/datarun-lab`. Expected audience:
`datarun-server`. Smoke principal: `field-worker-rotated`, subject
`53b46770-c03a-4f3f-b25c-3321c1e5af15`, actor
`22222222-2222-4222-8222-222222222222`.

## Evidence Summary

| Check | Result | Evidence |
|---|---|---|
| Key/provider precheck | Pass | `keys-before.tsv` and `key-components-before.tsv` record the active RS256 provider, priorities, key IDs, and algorithms without private key material. |
| Pre-rotation auth smoke | Pass | `token-before-add-metadata.json` records redacted token metadata; `auth-before-add.json` returned HTTP 200. |
| New signing key added | Pass | `add-active-key-result.txt`, `keys-after-add.tsv`, and `key-components-after-add.tsv` record the new generated RSA provider at priority `110`. |
| New-key token acceptance | Pass | `token-after-add-metadata.json` records the new token header/claims without token or signature material; `auth-new-key-before-passive.json` returned HTTP 200. |
| Old provider passive overlap | Pass | `retire-old-provider-passive-result.txt`, `keys-after-passive.tsv`, and `key-components-after-passive.tsv` show the old provider left enabled and made passive. |
| Old-token behavior | Pass | `auth-old-token-after-passive.json` returned HTTP 200, proving expected old-token acceptance during the overlap window. |
| New-token behavior after passive step | Pass | `auth-new-token-after-passive.json` returned HTTP 200. |
| Principal binding authority | Pass | All successful `/api/auth/me` smoke resolved through the same explicit binding to actor `22222222-2222-4222-8222-222222222222`; `principal-bindings-after-rotation.txt` and `principal-binding-operations-after-rotation.txt` show the active rotated subject mapping and no binding operations after the earlier NW-067 R4 principal rotation. No IdP group, role, resource claim, or JWT `actor_id` was used as platform authority. |

## Deviations

- The adapter selected passive old-key overlap rather than immediate old-token
  rejection. This is intentional and matches the safe Keycloak rotation model
  for an overlap window.
- Orchestrator SSH to the lab briefly timed out after the rotation. After lab
  reachability returned, the DB1 principal-binding and operation-history TSV
  snapshots were copied into the NW-077 evidence directory and checksums were
  refreshed.

## Cleanup State

- No bearer tokens, admin tokens, client secrets, private signing keys, or raw
  passwords were retained in evidence.
- The new RSA signing provider remains active at priority `110`.
- The old RS256 provider remains enabled but passive for the overlap window.
- The main reference app remained healthy during the rotation smoke.

## Follow-Up Work

- Before real production, choose and document the production IdP old-key
  overlap/removal window. This lab record proves passive overlap, not an
  immediate disable/remove policy.
