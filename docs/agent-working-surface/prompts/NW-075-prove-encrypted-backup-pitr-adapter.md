# NW-075 Agent Prompt: Prove Encrypted Backup/PITR Adapter

You are Worker NW-075 in `/home/hamza/datarun-platform`.

You are not alone in the codebase or lab. Do not revert unrelated repository
changes. Do not mutate remote backup, database, or storage state until the
read-only precheck is complete, the recovery path is explicit, and Hamza has
approved the backup exercise window.

## Goal

Close the NW-067 backup-encryption blocker by configuring or selecting a
concrete backup/PITR adapter for the synthetic reference environment and
retaining evidence that backups are encrypted, protected from the application
host boundary, fresh enough for the accepted RPO, and restorable.

## Current Gap

NW-067 R7 proved functional restore timing, but pgBackRest reported `cipher:
none`. Read-only lab inspection after the partial rehearsal found DB1/DB2 using
pgBackRest with an S3 repository on MinIO bucket `datarun-nw067-backup`, TLS CA
verification enabled, and no `repo1-cipher-type` / `repo1-cipher-pass`.

Treat the `datarun-nw067-backup` repository as partial-rehearsal evidence. Do
not retrofit it into a pass claim.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/operations/policies/first-reference-deployment-policy.md`
5. `docs/operations/runbooks/production-deployment-runbook.md` Section 12
6. `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
7. `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
8. `docs/agent-working-surface/platform-next-work-backlog.md` NW-075 row

## Safest Execution Route

Use a fresh encrypted repository boundary.

1. Capture read-only DB1/DB2 pgBackRest config, WAL archive posture, current
   repository `info`, MinIO bucket posture, and app-host absence of backup
   credentials.
2. Create a new MinIO bucket such as `datarun-nw075-backup`. Prefer object lock
   and versioning if MinIO supports them cleanly in the lab.
3. Create a dedicated MinIO user/policy scoped only to the new bucket. Do not
   place backup credentials on the application host.
4. Update DB1 and DB2 pgBackRest config to use the new bucket and client-side
   encryption:

```text
repo1-cipher-type=aes-256-cbc
repo1-cipher-pass=<secret from approved secret mechanism>
```

5. Keep pgBackRest config or a verified included secret fragment owned by
   `root:postgres` and mode `0640`, or stricter.
6. Run `stanza-create`, `check`, and a fresh full backup from DB1.
7. Wait for a recoverable timestamp at or after the frozen source time.
8. Reset DB2 as a clean restore target and execute runbook Section 12 against
   the encrypted repository.
9. Record NW-075 evidence only. Do not mark NW-067 accepted.

## Read-Only Remote Precheck

Run these from a network that can reach the lab. Keep outputs secret-safe.

```bash
ssh -o BatchMode=yes nmcp@datarun-db1.lab \
  'set -euo pipefail
   sudo -n -u postgres pgbackrest version
   sudo -n -u postgres pgbackrest --stanza=datarun info
   sudo -n grep -E "^(archive_mode|archive_command|archive_timeout)" \
     /etc/postgresql/*/main/postgresql.conf
   sudo -n awk "/^[[:space:]]*($|#)/ { next } { print FILENAME \":\" NR \":\" $0 }" \
     /etc/pgbackrest.conf'
```

```bash
ssh -o BatchMode=yes nmcp@datarun-db2.lab \
  'set -euo pipefail
   sudo -n -u postgres pgbackrest version
   sudo -n -u postgres pgbackrest --stanza=datarun info || true
   sudo -n awk "/^[[:space:]]*($|#)/ { next } { print FILENAME \":\" NR \":\" $0 }" \
     /etc/pgbackrest.conf'
```

```bash
ssh -o BatchMode=yes nmcp@minio.lab \
  'set -euo pipefail
   sudo -n sh -c ". /etc/default/minio; \
     export MC_HOST_lab=https://$MINIO_ROOT_USER:$MINIO_ROOT_PASSWORD@localhost:9000; \
     mc --insecure ls lab; \
     mc --insecure version info lab/datarun-nw067-backup || true; \
     mc --insecure retention info lab/datarun-nw067-backup || true; \
     mc --insecure encrypt info lab/datarun-nw067-backup || true"'
```

Do not print MinIO root password, S3 access keys, `repo1-cipher-pass`, or
private certificate material.

## Adapter To Create

Create lab-only adapter material outside git unless the procedure becomes a
reusable operator path:

```text
/opt/datarun-lab/adapters/nw075-encrypted-pgbackrest.sh
/opt/datarun-lab/evidence/NW-075-2026-06-17/
```

The adapter should:

- create the new bucket with object lock/versioning when available;
- create or select a dedicated bucket-scoped MinIO user/policy;
- write redacted pgBackRest config evidence before and after the change;
- install encrypted pgBackRest config on DB1/DB2 through privileged file writes;
- run backup, restore, and smoke checks;
- avoid passing secrets in command-line arguments, shell history, committed
  files, or evidence.

## Commands To Run

Use generated secret material supplied out of band. Keep `set -x` disabled.

```bash
export DATARUN_EVIDENCE_DIR=/opt/datarun-lab/evidence/NW-075-2026-06-17
install -d -m 0750 "$DATARUN_EVIDENCE_DIR"
```

Create the fresh bucket, using the selected MinIO credential path:

```bash
ssh -o BatchMode=yes nmcp@minio.lab \
  'sudo -n sh -c ". /etc/default/minio; \
    export MC_HOST_lab=https://$MINIO_ROOT_USER:$MINIO_ROOT_PASSWORD@localhost:9000; \
    mc --insecure mb --with-lock lab/datarun-nw075-backup; \
    mc --insecure version enable lab/datarun-nw075-backup; \
    mc --insecure version info lab/datarun-nw075-backup"'
```

After installing encrypted pgBackRest config on DB1/DB2:

```bash
ssh -o BatchMode=yes nmcp@datarun-db1.lab \
  'sudo -n -u postgres pgbackrest --stanza=datarun stanza-create'
ssh -o BatchMode=yes nmcp@datarun-db1.lab \
  'sudo -n -u postgres pgbackrest --stanza=datarun check'
ssh -o BatchMode=yes nmcp@datarun-db1.lab \
  'sudo -n -u postgres pgbackrest --stanza=datarun --type=full backup'
ssh -o BatchMode=yes nmcp@datarun-db1.lab \
  'sudo -n -u postgres pgbackrest --stanza=datarun info'
```

Then execute runbook Section 12 freeze, recoverable timestamp, DB2 clean
restore, source/restore comparison, readiness, auth, config, and authorized
pull smoke against the encrypted repository.

## Evidence To Retain

Retain outside git:

- redacted DB1/DB2 pgBackRest config snapshots before and after the change;
- MinIO bucket settings, versioning/object-lock/encryption posture, and access
  policy summary without credentials;
- TLS certificate details proving MinIO identity and CA verification;
- pgBackRest `stanza-create`, `check`, `backup`, and `info` output showing
  encrypted cipher;
- freeze time, latest recoverable timestamp, RPO/RTO measurements;
- source/restore schema, event, config, binding, and assignment comparisons;
- readiness, `/api/auth/me`, config, and authorized-pull smoke outputs;
- cleanup state, failed attempts, and residual risk.

## Repo Docs To Patch

Retain raw secret-safe evidence outside git under the current lab evidence
root, or a successor evidence root if this is a fresh exercise. If the adapter
procedure changes the reusable operator path, update the production runbook in
a separate commit from the dated evidence.

Create a dated rehearsal or adapter evidence record only when a concrete
exercise has been run and evidence exists.

## Acceptance

NW-075 can be accepted only when evidence shows:

- the selected backup/PITR adapter reports encryption at rest, not `cipher:
  none` or an equivalent unencrypted state;
- backup traffic and restore traffic use protected channels or provider
  controls consistent with the accepted policy;
- backup material is outside the application host failure/access boundary;
- a latest recoverable timestamp is available and measured against the
  accepted 1-hour RPO;
- a clean isolated restore passes readiness, auth, config, and authorized-pull
  smoke;
- no secrets, keys, tokens, private certificates, or raw passwords are
  committed.

## Guardrails

- Use only synthetic/non-sensitive data.
- Do not weaken the accepted backup encryption/off-site policy to obtain a
  pass.
- Do not delete or rewrite retained NW-067 raw evidence.
- Do not repair schema history or event data manually.
- Do not mark NW-067 accepted from NW-075 alone.
- Do not place backup credentials on the application host.
- Do not use the old unencrypted `datarun-nw067-backup` repository as pass
  evidence.

## Verification

Run `git diff --check` for any repository changes. Verify the evidence record
does not contain secret values. Verify the reference service remains healthy if
it is kept running after the exercise.

## Commit Flow

Route or runbook corrections use:

```text
docs(ops): route encrypted backup adapter

NW: NW-075
```

Adapter evidence uses:

```text
test(ops): prove encrypted backup adapter

NW: NW-075
```

Accept NW-075 only after the evidence names the adapter, encryption posture,
recoverable timestamp, restore result, cleanup, and residual risk.

## Stop And Report

Stop if pgBackRest still reports `cipher: none`, MinIO TLS verification fails,
secrets would be printed or committed, the app host receives backup
credentials, DB2 is not clean before restore, latest recoverable time is before
the freeze or outside 1-hour RPO, restore diffs schema/events/config/bindings
unexpectedly, readiness/auth/config/pull smoke fails, or the route requires
weakening NW-064 backup policy.
