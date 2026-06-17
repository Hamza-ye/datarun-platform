Status: accepted
Document type: rehearsal_record
Owner: Hamza
Source: NW-075; `docs/agent-working-surface/prompts/NW-075-prove-encrypted-backup-pitr-adapter.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-17
Supersedes: none
Related: `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`; raw evidence at `/opt/datarun-lab/evidence/NW-075-2026-06-17`

# 2026-06-17 Encrypted Backup/PITR Adapter Evidence

## Result

Final result: `accepted`.

NW-075 proved the core backup-encryption gap from the partial NW-067 rehearsal:
DB1 now writes pgBackRest backups to a fresh MinIO bucket with client-side
`aes-256-cbc` encryption, and DB2 restored from that encrypted repository with
matching schema/event/config/binding counts. A disposable restored app against
DB2 passed readiness, `/api/auth/me`, `/api/sync/config`, and authorized pull
smoke with the active rotated synthetic worker principal. The protected token
path used Hamza-approved orchestrator-mediated transfer, cleaned local and
app-host bearer-token files immediately after the attempt, and did not retain
the token in evidence.

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
| Restored auth/config/pull smoke | Pass after principal correction | `restore-auth-smoke-approved-token-result.txt` records the first approved transfer and HTTP 401 from `/api/auth/me`; DB1/DB2 binding inspection then showed the original `field-worker` subject inactive and the rotated worker subject active. `restore-auth-me-rotated-worker.json`, `restore-config-rotated-worker.json`, `restore-pull-request-rotated-worker.json`, and `restore-pull-rotated-worker.json` record successful restored protected-endpoint smoke with actor `22222222-2222-4222-8222-222222222222`. |

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
- Hamza then approved an orchestrator-mediated synthetic bearer-token transfer
  for this rehearsal environment. Token acquisition through Keycloak direct
  grant was available, but the first restored app attempt used the original
  `field-worker` principal, whose subject had been rotated out of active
  principal binding. That token correctly returned HTTP 401. The successful
  follow-up used `field-worker-rotated`, whose token contains
  `aud=datarun-server`, issuer `https://keycloak.lab/realms/datarun-lab`, and
  active subject `53b46770-c03a-4f3f-b25c-3321c1e5af15`.

## Cleanup State

- The temporary local cipher file and temporary evidence staging directory were
  removed.
- The aborted MinIO generated secret files, policy file, generated bucket/user
  residue, and hung `mc` processes were removed before the successful route was
  retried.
- The disposable restore-smoke app container, compose network, env file, and
  runtime config directory were removed after readiness and protected-endpoint
  smoke evidence.
- Temporary bearer-token files on the orchestrator and app host were removed by
  the approved-transfer cleanup path; cleanup was verified again after the
  restored-auth attempt.
- DB1 and DB2 retain the encrypted pgBackRest configuration and the fresh
  `datarun-nw075-backup` repository remains for inspection.
- DB2 currently contains the restored synthetic `datarun` database.

## Follow-Up Work

- If the encrypted backup adapter becomes the accepted reusable operator path,
  update `docs/operations/runbooks/production-deployment-runbook.md` Section 12
  with the concrete procedure.
- Keep the active synthetic worker-token helper aligned with the rotated
  principal-binding state; the original `field-worker` principal is expected
  rejection evidence, not the current worker smoke principal.
