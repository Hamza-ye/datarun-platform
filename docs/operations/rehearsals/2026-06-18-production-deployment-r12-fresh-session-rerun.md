Status: accepted
Document type: rehearsal_record
Owner: Hamza
Source: NW-067; `docs/agent-working-surface/prompts/NW-067-rehearse-production-deployment-recovery.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/operations/rehearsals/2026-06-17-production-deployment-reference-environment.md`, `docs/operations/rehearsals/2026-06-18-encrypted-backup-recovery-point-refresh.md`, and `docs/operations/rehearsals/2026-06-18-fresh-session-protected-smoke-token-path.md`; raw evidence at `datarun-app.lab:/opt/datarun-lab/evidence/NW-067-R12-2026-06-18`, `datarun-db1.lab:/opt/datarun-lab/evidence/NW-067-R12-2026-06-18`, and `keycloak.lab:/opt/datarun-lab/evidence/NW-067-R12-2026-06-18`

# 2026-06-18 Production Deployment R12 Fresh-Session Rerun Evidence

## Result

Final result: `accepted` for R12.

A fresh-context worker session was dispatched without inheriting the prior
thread context. It reconstructed the current synthetic reference environment
from indexed docs and retained evidence, created R12 evidence on the app, DB1,
and ops/Keycloak hosts, and reached the required health, monitoring, recovery,
and protected-smoke checks. The worker did not return a final message before
shutdown, so this record is based on direct inspection of the retained evidence
files rather than a worker final-response claim.

R12 now passes for the synthetic reference environment. This record does not
approve real production, real data/users, independent human continuity, mobile
OAuth/OIDC login UX, or a real production token-handling procedure.

## Environment

Raw evidence roots:

- `datarun-app.lab:/opt/datarun-lab/evidence/NW-067-R12-2026-06-18/app`
- `datarun-db1.lab:/opt/datarun-lab/evidence/NW-067-R12-2026-06-18/db1`
- `keycloak.lab:/opt/datarun-lab/evidence/NW-067-R12-2026-06-18/ops`

Reconstructed current state:

- app host: `datarun-app.lab`
- source DB host: `datarun-db1.lab`
- ops/Keycloak host: `keycloak.lab`
- candidate image digest:
  `sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`
- active runtime config: `/opt/datarun-lab/runtime-config-nw076-g2`
- active rotated principal: subject
  `53b46770-c03a-4f3f-b25c-3321c1e5af15`, actor
  `22222222-2222-4222-8222-222222222222`

## Evidence Summary

| Check | Result | Evidence |
|---|---|---|
| Access and reconstruction | Pass | `reference-server-image.txt`, `active-env-safe.txt`, `runtime-config-directories.txt`, `principal-bindings.tsv`, and accepted NW-075 through NW-081 records identify the current hosts, image digest, runtime config, and active rotated principal without prior shell history. |
| Synthetic inject state | Pass | `synthetic-inject-active-grep.txt` is empty. No temporary inject target required cleanup during the rerun. |
| App health | Pass | `readiness-loopback.json` and `liveness-loopback.json` both show `UP`. |
| Unauthenticated protected endpoints | Pass | Ops-vantage public checks returned `401 ssl_verify_result=0` for `/api/auth/me`, `/api/sync/config`, and a POST to `/api/sync/pull`. |
| Monitoring and alerts | Pass | `prometheus-ready-final-r12.txt` reports ready; `prometheus-targets-summary-final-r12.tsv` shows readiness, JWKS, database, certificate, app, and node targets `up`; `prometheus-firing-alerts-summary-final-r12.json`, `prometheus-critical-backup-alerts-final-r12.json`, `alertmanager-active-alerts-summary-final-r12.json`, and `alertmanager-critical-backup-alerts-final-r12.json` are empty arrays. |
| DB1 read-only state | Pass | `db1-state-summary.tsv` records 8 events, max sync watermark 9, one config package at version 1, four principal bindings total, and two active principal bindings. `flyway-history.tsv` records Flyway V1-V10. |
| Recovery point and RPO | Pass | `recovery-point-current.txt` records pgBackRest `cipher=aes-256-cbc`, backup `20260617-022519F_20260618-131808D`, latest recoverable time `2026-06-18T13:18:14Z`, observation time `2026-06-18T13:51:15Z`, `recovery_point_age_seconds=1981`, and `rpo_met=true`. |
| Protected smoke | Pass | `protected-smoke-summary-r12.json` records `/api/auth/me`, `/api/sync/config`, and `/api/sync/pull` all HTTP 200; actor and token subject match the active rotated principal; config version is 1; pull returned `has_more=false` and zero events for the fresh request. |
| Token cleanup | Pass | `token-cleanup-proof-r12.json` records removal of `/dev/shm/nw067-r12-token`; retained token metadata is redacted and has `raw_bearer_material_retained=false`. |
| Secret hygiene | Pass | `secret-scan-final-r12.txt` on app, DB1, and ops reports no retained secret-pattern matches. |

## Recovery Authority And Stop Conditions

Stop and escalate on loss of approved access, hidden or undocumented state
requirements, unclear recovery state or authority, active severity condition,
secret leakage, event mutation/loss, unsafe migration, RPO/RTO failure,
cross-scope exposure, or protected-smoke failure.

Rollback requires proven old-image/current-schema compatibility. Restore
requires Hamza's disaster/restore decision and an accepted loss boundary.
Forward fix requires preserving DB history and deploying a newly tested
immutable image through full preflight.

## Deviations

- The fresh worker session created retained evidence but did not return a
  final response before shutdown. This record therefore cites the evidence
  files directly.
- Public unauthenticated protected-endpoint status files from the app-host
  vantage recorded `000`, while the ops-vantage public checks returned the
  expected `401` with TLS verification success. The R12 acceptance uses the
  ops-vantage public evidence plus app-host loopback readiness/liveness.
- No remote `r12-summary.json` was retained. This dated record is the summary
  of the retained R12 evidence.

## Cleanup State

- No synthetic alert inject target remained active.
- Temporary token material under `/dev/shm/nw067-r12-token` was removed.
- Evidence roots on app, DB1, and ops/Keycloak hosts were retained for the
  rehearsal evidence period.

## Follow-Up Work

- No NW-067 R12 blocker remains after this rerun.
- Real production remains outside this claim and still requires its separate
  provider, region, jurisdiction/data-classification, communication/login, and
  organizational review gates.
