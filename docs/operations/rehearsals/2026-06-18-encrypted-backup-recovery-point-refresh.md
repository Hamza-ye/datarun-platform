Status: accepted
Document type: rehearsal_record
Owner: Hamza
Source: NW-080; `docs/agent-working-surface/prompts/NW-080-refresh-encrypted-backup-recovery-point.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`, `docs/operations/rehearsals/2026-06-17-encrypted-backup-pitr-adapter.md`, and `docs/operations/rehearsals/2026-06-17-monitoring-alert-adapter.md`; raw evidence at `datarun-db1.lab:/opt/datarun-lab/evidence/NW-080-2026-06-18`, `datarun-app.lab:/opt/datarun-lab/evidence/NW-080-2026-06-18`, and `keycloak.lab:/opt/datarun-lab/evidence/NW-080-2026-06-18`

# 2026-06-18 Encrypted Backup Recovery-Point Refresh Evidence

## Result

Final result: `accepted`.

NW-080 cleared the failed R12 stale-backup blocker by creating a fresh encrypted
pgBackRest diff backup for the active reference DB using the accepted NW-075
repository. The new recovery point was
`20260617-022519F_20260618-131808D`, stopped at
`2026-06-18T13:18:14Z`, and had measured recovery-point age `66` seconds at
observation time.

The live backup freshness metric was updated only from that real backup stop
timestamp. Prometheus then showed `backup_age_seconds=245.7869999408722`,
zero active firing backup alerts, and zero active firing critical alerts.
Alertmanager delivered a resolved `DatarunBackupStale` webhook notification and
finished with no active alerts.

This record does not accept NW-067 or prove fresh-session R12 by itself.

## Environment

Raw evidence roots:

- `datarun-db1.lab:/opt/datarun-lab/evidence/NW-080-2026-06-18`
- `datarun-app.lab:/opt/datarun-lab/evidence/NW-080-2026-06-18`
- `keycloak.lab:/opt/datarun-lab/evidence/NW-080-2026-06-18`

Active backup repository: `datarun-nw075-backup`.

## Evidence Summary

| Check | Result | Evidence |
|---|---|---|
| Precondition | Pass | `prometheus-alerts-before.json` and `alertmanager-alerts-before.json` show active critical `DatarunBackupStale`; `prometheus-backup-age-before.json` records backup age about 78,680 seconds. |
| Encrypted diff backup | Pass | `db1-pgbackrest-backup-diff.log` records a completed diff backup; `db1-pgbackrest-info-after.txt` and `.json` show `cipher: aes-256-cbc` and backup `20260617-022519F_20260618-131808D`. |
| RPO proof | Pass | `rpo-measurement.txt` records latest recoverable time `2026-06-18T13:18:14Z`, observed time `2026-06-18T13:19:20Z`, age `66`, and `rpo_met=true`. |
| Source DB stability | Pass | `source-db-counts-before-backup.txt` and `source-db-counts-after-backup.txt` both show 8 events and max watermark 9. |
| Monitoring metric provenance | Pass | `backup-metric-update-provenance.txt` records the metric update source backup ID, stop timestamp, stop epoch, and `source_cipher=aes-256-cbc`; `datarun-backup-prom-final.prom` contains the updated timestamp and encryption proof. |
| Alert resolution | Pass | `prometheus-alerts-final.json`, `prometheus-active-critical-backup-alerts-final.json`, `prometheus-backup-alerts-final-summary.txt`, `alertmanager-alerts-final.json`, and `alertmanager-final-summary.txt` show no active backup or critical alerts. `webhook-datarun-backup-stale-resolved-final.jsonl` records the resolved webhook notification. |
| Evidence hygiene | Pass | `secret-scan-final-db1.txt`, `secret-scan-final-app.txt`, and `secret-scan-final-ops.txt` report no unredacted secret patterns. |

## Deviations

- A transient pending `DatarunJwksDown` appeared in one Prometheus poll during
  the wait window and cleared before the final evidence. It was not related to
  the backup freshness blocker.
- Historical webhook sink logs still contain earlier NW-078 permission errors,
  but the NW-080 resolved webhook event was captured successfully.

## Cleanup State

- No temporary backup processes remained; `pgbackrest-processes-after.txt`
  records the post-run process state.
- The monitoring textfile metric remains updated to the real encrypted backup
  stop timestamp.
- The accepted monitoring stack and synthetic webhook sink remain available for
  the next R12 attempt.

## Follow-Up Work

- NW-067 still requires a genuinely fresh privileged Hamza R12 rerun after
  NW-080 and NW-081 are both accepted.
