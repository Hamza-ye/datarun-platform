Status: partial
Document type: rehearsal_record
Owner: Hamza
Source: NW-075; `docs/agent-working-surface/prompts/NW-075-prove-encrypted-backup-pitr-adapter.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-17
Supersedes: none
Related: `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`; raw evidence at `/opt/datarun-lab/evidence/NW-075-2026-06-17`

# 2026-06-17 Encrypted Backup/PITR Adapter Evidence

## Result

Final result: `partial`.

NW-075 proved the core backup-encryption gap from the partial NW-067 rehearsal:
DB1 now writes pgBackRest backups to a fresh MinIO bucket with client-side
`aes-256-cbc` encryption, and DB2 restored from that encrypted repository with
matching schema/event/config/binding counts. NW-075 is not accepted yet because
the full restored application smoke was limited to readiness; the protected
OIDC bearer-token adapter used during NW-067 was not available as a reusable
secret-safe procedure, so `/api/auth/me`, `/api/sync/config`, and authorized
pull smoke were not rerun for the NW-075 restore target.

This record does not accept NW-067, prove monitoring or rotation readiness, or
approve real production.

## Environment

Raw evidence root: `/opt/datarun-lab/evidence/NW-075-2026-06-17`.

Source database: `datarun-db1.lab` (`192.168.1.214`). Restore database:
`datarun-db2.lab` (`192.168.1.215`). Storage host: `minio.lab`
(`192.168.1.211`).

Fresh backup bucket: `datarun-nw075-backup`.

## Evidence Summary

| Check | Result | Evidence |
|---|---|---|
| Pre-existing NW-067 backup posture | Failed encryption requirement | NW-067 pgBackRest evidence reported `cipher: none` against `datarun-nw067-backup`. |
| Fresh encrypted repository | Pass | `db1-pgbackrest-config-redacted.txt` records `repo1-s3-bucket=datarun-nw075-backup`, `repo1-cipher-type=aes-256-cbc`, and redacted secret-bearing options. |
| Fresh full backup | Pass | `db1-pgbackrest-info.txt` reports `cipher: aes-256-cbc` and full backup `20260617-022519F`, stopped at `2026-06-17 02:26:07+00`. |
| DB2 clean restore | Pass | DB2 was restored from the encrypted repository and PostgreSQL returned active. |
| Source/restore count comparison | Pass | `source-db-counts.txt` and `restore-db-counts.txt` both show 10 successful Flyway rows, 8 events, max watermark 9, config version 1, and 4 principal bindings. |
| Restored app readiness | Pass | A disposable restore-smoke app container against DB2 returned readiness `UP`; see `restore-readiness.json`. |
| Restored auth/config/pull smoke | Not executed | No reusable protected bearer-token adapter was available after NW-067 cleanup. Do not infer endpoint-level restored-service proof from readiness alone. |

## Deviations

- A first attempt to create a dedicated MinIO user was aborted because
  `mc admin user add` exposed generated credential material in the remote
  process list. The generated material was discarded, temporary secret files
  were removed, and no generated user or policy remained.
- The accepted reusable runbook still describes the desired rotation/backup
  posture generically. This record proves the lab adapter state but does not
  yet update the reusable operator procedure.
- Direct lab DNS for DB1/DB2 was intermittent from the orchestrating session;
  direct IP with `HostKeyAlias` was used for DB operations.
- A safer remote-only token path was attempted, but `nmcp` on the app host was
  not authorized to SSH into `keycloak.lab`; transferring a live bearer token
  through the orchestrating machine was rejected as too risky without explicit
  owner approval.

## Cleanup State

- The temporary local cipher file and temporary evidence staging directory were
  removed.
- The aborted MinIO generated secret files, policy file, generated bucket/user
  residue, and hung `mc` processes were removed before the successful route was
  retried.
- The disposable restore-smoke app container, compose network, env file, and
  runtime config directory were removed after readiness evidence.
- DB1 and DB2 retain the encrypted pgBackRest configuration and the fresh
  `datarun-nw075-backup` repository remains for inspection and future restore
  smoke completion.
- DB2 currently contains the restored synthetic `datarun` database.

## Follow-Up Work

- Add or recover a reusable protected OIDC token acquisition adapter so NW-075
  can complete restored `/api/auth/me`, `/api/sync/config`, and authorized pull
  smoke without exposing bearer tokens. One acceptable route is app-host access
  to a reviewed token helper; another requires explicit owner approval for any
  orchestrator-mediated live-token transfer.
- Re-run the restored app smoke against DB2 using that adapter.
- If the encrypted backup adapter becomes the accepted reusable operator path,
  update `docs/operations/runbooks/production-deployment-runbook.md` Section 12
  with the concrete procedure.
