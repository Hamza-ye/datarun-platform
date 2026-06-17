Status: accepted
Document type: rehearsal_record
Owner: Hamza
Source: NW-076; `docs/agent-working-surface/prompts/NW-076-add-db-credential-rotation-adapter.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-17
Supersedes: none
Related: `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`; raw evidence at `datarun-app.lab:/opt/datarun-lab/evidence/NW-076-2026-06-17`

# 2026-06-17 DB Credential Rotation Adapter Evidence

## Result

Final result: `accepted`.

NW-076 exercised a two-generation synthetic database credential rotation for
the reference app. The original `datarun_runtime` role remained available as
the recovery credential, while the app was switched from synthetic generation
1 to synthetic generation 2 through separate mounted config-tree directories
and compose env files. Generation 2 is the active runtime credential at the end
of the exercise.

The final state keeps `datarun_runtime_nw076_g2` login-enabled and disables
`datarun_runtime_nw076_g1` login. The g1 `pg_hba.conf` entry was removed,
PostgreSQL was reloaded, and app-to-DB login as g1 failed at the intended
boundary. The app remained healthy on g2 and passed protected `/api/auth/me`,
`/api/sync/config`, and authorized pull smoke.

This record does not accept NW-067, prove monitoring/alert delivery,
fresh-session R12, independent human continuity, or real production.

## Environment

Raw evidence root: `datarun-app.lab:/opt/datarun-lab/evidence/NW-076-2026-06-17`.

App host: `datarun-app.lab` (`192.168.1.213`). Database host:
`datarun-db1.lab` (`192.168.1.214`). Active reference container:
`datarun-reference-server-1`.

Synthetic generation roles:

- `datarun_runtime_nw076_g1`
- `datarun_runtime_nw076_g2`

## Evidence Summary

| Check | Result | Evidence |
|---|---|---|
| Pre-rotation app posture | Pass | `app-mounts-before.txt`, `app-containers-before.txt`, and `readiness-before-db-rotation.json` capture the reference app and read-only config mount posture before the switch. |
| Synthetic role preparation | Pass | `db-roles-after-prepare.txt` shows g1/g2 login roles without superuser, createdb, createrole, replication, or bypassrls; `pg-hba-after-prepare.txt` shows role-scoped `hostssl` entries for the app host. |
| Generation 1 switch | Pass | `readiness-g1.json`, `app-containers-g1.txt`, `g1-auth-me.json`, `g1-config.json`, `g1-pull-request.json`, and `g1-pull.json` record readiness and protected smoke after switching to g1. |
| Generation 2 switch | Pass | `readiness-g2.json`, `app-containers-g2.txt`, `g2-auth-me.json`, `g2-config.json`, `g2-pull-request.json`, and `g2-pull.json` record readiness and protected smoke after switching to g2. |
| Generation 1 revocation | Pass | `db-roles-after-revoke.txt` shows `datarun_runtime_nw076_g1` with `rolcanlogin=false`; `pg-hba-after-revoke.txt` shows the g1 `pg_hba.conf` line removed and the g2 line retained. |
| Old-generation rejection | Pass | `g1-rejection-after-revoke.txt` records app-host `psql` exit status `2` and PostgreSQL rejection for g1 after revocation. |
| Active generation proof | Pass | `readiness-final-g2.json`, `active-env-final.txt`, `app-runtime-residue-final.txt`, and `g2-direct-connection-proof.txt` show the app healthy with `DATARUN_RUNTIME_CONFIG_DIR=/opt/datarun-lab/runtime-config-nw076-g2` and direct DB login as `datarun_runtime_nw076_g2`. |

## Deviations

- The current primary lab credential `datarun_runtime` owns the database and
  public objects. NW-076 did not clone ownership or `CREATE` posture into
  g1/g2. The synthetic generations used narrower grants and still supported
  startup, readiness, auth, config, and pull smoke.
- g1 rejection was proven after both disabling login and removing the g1
  role-scoped `pg_hba.conf` entry. This is the accepted boundary for the
  rehearsal: the old generation cannot connect from the app host.

## Cleanup State

- Local generated password temp files were removed.
- Temporary bearer-token files and curl configs were removed after each smoke.
- The g1 runtime config directory and g1 env file were removed after rejection
  proof.
- The active env file `nw067.env` was backed up and updated to point at
  `/opt/datarun-lab/runtime-config-nw076-g2`.
- DB1 retains `datarun_runtime_nw076_g1` as a disabled evidence role and
  `datarun_runtime_nw076_g2` as the active synthetic runtime role. The original
  `datarun_runtime` role remains available as recovery.

## Follow-Up Work

- If the reference environment is reset before the final NW-067 rerun, ensure
  the active app env still points at the accepted g2 runtime config or rerun
  this adapter.
- Before real production, replace this lab-only password generation/transfer
  path with the selected production secret manager and rotation workflow.
