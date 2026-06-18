# NW-080 Agent Prompt: Refresh Encrypted Backup Recovery Point

You are Worker NW-080 in `/home/hamza/datarun-platform`. You are not alone in
the codebase: do not revert or overwrite others' repo changes. Your
responsibility is remote lab execution/evidence for NW-080 only; do not edit
local repo docs unless explicitly asked later.

## Goal

Refresh the accepted encrypted backup recovery point and clear the live
`DatarunBackupStale` condition honestly, preserving evidence and cleanup state.
Do not mark NW-067 accepted.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`
4. `docs/operations/rehearsals/2026-06-17-encrypted-backup-pitr-adapter.md`
5. `docs/operations/rehearsals/2026-06-17-monitoring-alert-adapter.md`
6. `docs/operations/runbooks/production-deployment-runbook.md` Sections 11-12
   and 16
7. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-067,
   NW-075, NW-078, NW-080

## Execution Boundary

Remote hosts are the existing lab hosts from the evidence records. Use SSH with
`BatchMode` and `HostKeyAlias` as needed. Keep all retained evidence
secret-safe.

## Tasks

- Inspect current backup/recovery and monitoring state, especially
  `DatarunBackupStale`.
- Create a fresh encrypted pgBackRest recovery point for the active reference
  DB using the accepted NW-075 adapter, not the old unencrypted NW-067
  repository.
- Preserve evidence under `/opt/datarun-lab/evidence/NW-080-2026-06-18` or a
  timestamped successor if needed.
- Prove the latest recovery point is within 1 hour RPO with concrete
  timestamps.
- Update the backup freshness monitoring metric only from real fresh backup
  evidence; do not fabricate a timestamp.
- Capture Prometheus/Alertmanager/webhook evidence showing
  `DatarunBackupStale` resolved and final zero active critical backup alerts.
- Capture cleanup/secret-scan evidence. Do not retain raw secrets, passwords,
  tokens, private keys, or bearer URLs.
- If a fresh encrypted backup cannot be produced without a real policy decision
  or missing credential, stop and report the blocker.

## Final Response

Include:

- result `PASS` or `BLOCKED`;
- exact evidence root or roots;
- commands/hosts used at a high level;
- backup ID, stop time, and RPO age;
- alert resolution status;
- cleanup state;
- any doc facts the integrator should record.

Do not commit.
