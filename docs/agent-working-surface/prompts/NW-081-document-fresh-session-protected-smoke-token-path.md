# NW-081 Agent Prompt: Document Fresh-Session Protected-Smoke Token Path

You are Worker NW-081 in `/home/hamza/datarun-platform`. You are not alone in
the codebase: do not revert or overwrite others' repo changes. Your
responsibility is fresh-session protected-smoke token path proof for NW-081.
Avoid editing local repo docs unless explicitly asked later; remote
evidence/procedure notes are fine.

## Goal

Add and prove a documented fresh-session, secret-safe bearer-token acquisition
path for the active rotated synthetic principal, then run authorized protected
smoke. Do not mark NW-067 accepted.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
4. `docs/operations/rehearsals/2026-06-17-keycloak-jwks-rotation-adapter.md`
5. `docs/operations/rehearsals/2026-06-17-encrypted-backup-pitr-adapter.md`,
   only for its successful restored protected-smoke/token-handling evidence and
   cleanup constraints
6. `docs/operations/runbooks/production-deployment-runbook.md` Sections 10 and
   16
7. `docs/specifications/platform/production-auth-principal-binding.md`
8. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-067,
   NW-077, NW-081

## Execution Boundary

Remote hosts are the existing lab hosts from the evidence records. Use SSH with
`BatchMode` and `HostKeyAlias` as needed. Keep all retained evidence
secret-safe.

## Tasks

- Identify the active rotated synthetic principal and explicit binding from
  evidence/docs.
- Determine a fresh-session token acquisition path using approved lab/Keycloak
  access. Prefer a documented command/procedure that acquires a short-lived
  token without retaining raw token material in evidence.
- Prove authorized protected smoke against the live reference app:
  `/api/auth/me`, `/api/sync/config`, and authorized pull. Use active rotated
  principal only.
- Preserve evidence under `/opt/datarun-lab/evidence/NW-081-2026-06-18` or a
  timestamped successor if needed: redacted token metadata, HTTP result JSON,
  cleanup proof, and a procedure note sufficient for a future fresh session.
  Do not retain raw bearer tokens, passwords, client secrets, private keys, or
  bearer URLs.
- If no documented fresh-session token path can be created without a real
  product/security decision or unavailable secret, stop and report the exact
  blocker.

## Final Response

Include:

- result `PASS` or `BLOCKED`;
- exact evidence root or roots;
- active subject and actor;
- high-level token path without secrets;
- smoke endpoints/results;
- cleanup state;
- any doc facts the integrator should record.

Do not commit.

## Authorized Continuation

If the retained synthetic rotated-worker password file is stale, Hamza
authorizes, for this synthetic rehearsal only, secret-safe Keycloak admin
inspection and reset of the active `field-worker-rotated` user's password.

Constraints:

- do not retain raw password, admin token, bearer token, client secret, private
  key, or bearer URL in evidence;
- record only redacted metadata, command intent, timestamps, HTTP statuses,
  subject/actor, cleanup proof, and secret-scan output;
- do not change principal binding, actor mapping, groups, roles, claims, or JWT
  actor authority.

After reset/verification, rerun fresh-session token acquisition and protected
`/api/auth/me`, `/api/sync/config`, and authorized pull smoke. If admin access
or reset path is unavailable, report `BLOCKED` with exact reason.
