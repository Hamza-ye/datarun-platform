Status: partial
Document type: rehearsal_record
Owner: Hamza
Source: NW-067; `docs/agent-working-surface/prompts/NW-067-rehearse-production-deployment-recovery.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-18
Supersedes: none
Related: `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`; raw evidence at `/opt/datarun-lab/evidence/NW-067-2026-06-17`

# 2026-06-17 Production Deployment Reference Environment Rehearsal

## Result

Final result: `partial`.

R1-R9 produced useful synthetic reference-environment evidence, including clean
install, corrected clean migration repeatability, auth/TLS negative checks,
provisioning idempotency, sync smoke, measured restore, previous-to-candidate
upgrade, and contained failed-start response. The original 2026-06-17 attempt
did not pass R10 credential/JWKS rotation, R11 alert delivery, or R12
fresh-session solo cold recovery, and its backup adapter reported `cipher:
none`.

Accepted successor adapters NW-075, NW-076, NW-077, and NW-078 cover the
backup encryption, DB credential rotation, Keycloak/JWKS rotation, and
monitoring/alert-delivery gaps for the successor path. The 2026-06-18
fresh-session R12 attempt then found stale backup/recovery posture outside the
1-hour RPO and lacked a documented fresh-session bearer-token path for
authorized protected smoke. NW-080 and NW-081 now accept fixes for those two
R12 blockers, but NW-067 is still not accepted until R12 is rerun from a
genuinely fresh privileged Hamza session.

This record does not approve real production, real data, mobile OAuth/OIDC
login UX, independent human continuity, a real production alert recipient, or a
production token-handling procedure.

## Environment

Raw evidence root: `/opt/datarun-lab/evidence/NW-067-2026-06-17`.

Primary app host: `datarun-app.lab` (`192.168.1.213`), public synthetic URL `https://datarun-rehearsal.lab`, app loopback `127.0.0.1:18080`, management loopback `127.0.0.1:18081`.

Source database: `datarun-db1.lab` (`192.168.1.214`). Restore database: `datarun-db2.lab` (`192.168.1.215`). Storage and IdP hosts were `minio.lab` and `keycloak.lab`.

Candidate image: `localhost:5000/datarun/server@sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`, source revision `757d6c8d386f760693157c3e1388c877efdf6a0e`.

Previous image built for R8 from accepted commit `3dc09b5d89c2754e94ed1b538b40363664b0a5cc`: `localhost:5000/datarun/server@sha256:543bd2e1b3f6a62de07caef81d4d4632fdd43e001aa4c4a2897d116703a81f0b`.

## Scenario Results

| Scenario | Result | Evidence |
|---|---|---|
| R1 clean install | Pass | `R1-baseline/` records image identity, preflight, readiness, DB identity/SSL, Flyway history, config version, principal-binding count, and provisioning checksums. |
| R2 migration repeatability | Pass after lab adapter correction | Initial DB2 start failed because `pg_hba.conf` allowed only `datarun`; a narrow `hostssl datarun_r2 datarun_runtime 192.168.1.213/32` rule fixed the lab adapter. Corrected explicit DB evidence is `R2-migration-repeatability/corrected-explicit-datarun-r2-state.txt`: `datarun_r2` on DB2, Flyway V1-V10, two principal bindings, no events. Earlier PGSERVICE/PGDATABASE query outputs were retained as flawed evidence. |
| R3 TLS/provider/secrets | Pass | `R3-auth-tls-secrets/auth-negative-tests.txt` shows valid auth accepted, missing/wrong issuer/wrong audience rejected, and management not public. `secret-log-safety.txt` records no sensitive-pattern hits after removing a truststore-password logging issue. |
| R4 binding apply/reapply/change | Pass with adapter correction | Original reapply was idempotent. Keycloak generated a different subject UUID than the requested import ID, so the first rotated-worker binding failed. Corrected manifest bound the actual provider subject; old worker was rejected and rotated worker resolved to the expected actor. |
| R5 config and assignment | Pass | Reviewed config exact reapply returned version 1 with no new publication. Initial assignment exact reapply returned the existing event. Assignment create/end probes enforced command capability plus containment. |
| R6 device auth/config/sync | Pass with recorded advisory | Initial push before device catch-up raised `role_stale`, recorded as honest stale-authority advisory. After catch-up, clean push accepted one event, raised zero flags, and pull returned the pushed event. |
| R7 backup and isolated restore | Functional restore pass; policy caveat | RPO/RTO evidence: disaster `2026-06-17T00:37:19+00:00`, minimum service restored `2026-06-17T00:40:53+00:00`, `rto_seconds=214`, latest backup stop `2026-06-17T00:37:15+00:00`, `rpo_seconds=4`. First DB2 restore failed on dirty target/timeline and was retained. Clean timeline-selected restore then passed readiness/auth/config/pull. pgBackRest info reported `cipher: none`; encrypted backup compliance remains unproven. |
| R8 previous-to-candidate upgrade | Pass after failed DB2 recovery-point attempt | First previous image build used a short OCI revision label and was retained as failed preparation; rebuilt with full revision. DB2 recovery point failed because restored DB2 had `archive_mode` disabled. R8 was rerun on DB1 `datarun_upgrade`; pgBackRest diff `20260616-233201F_20260617-010141D` stopped after freeze. Candidate upgrade kept Flyway V1-V10, one assignment event, config version 1, two bindings, readiness, auth, config, and corrected pull smoke. |
| R9 failed deployment/migration response | Pass | Invalid image preflight failed on OCI revision mismatch before start. DB-denied first start used `datarun_r9_denied` without a `pg_hba` rule; service stayed not ready, and local DB1 check showed no `public.flyway_schema_history`. Recovery decision selected fix/rerun or leave traffic on previous healthy service; no manual schema repair. |
| R10 credential, binding, JWKS rotation | Original attempt partial; successor adapters accepted | R4 covers binding rotation evidence only in the original NW-067 run. Database credential rotation is now covered by accepted NW-076, and Keycloak/JWKS rotation is now covered by accepted NW-077. |
| R11 alert and incident response | Original attempt blocked; successor adapter accepted | No Prometheus/Alertmanager/Grafana or equivalent alert-recipient adapter was found during the original NW-067 run; only Keycloak was running on the ops VM. Monitoring/alert delivery is now covered by accepted NW-078. |
| R12 solo cold recovery | Executed 2026-06-18; partial / failed | Fresh approved SSH access was restored and the current state was reconstructed from indexed docs plus retained evidence. Live readiness/liveness and monitoring-target checks passed, DB1 read-only state matched the retained baseline shape, and the latest encrypted pgBackRest recovery point was identified. R12 did not pass because Prometheus/Alertmanager showed active critical `DatarunBackupStale` beginning 2026-06-17T16:27:34Z, the latest retained backup-success timestamp was 2026-06-17T15:26:33Z, and no documented fresh-session bearer-token helper or retained token path was available for authorized `/api/auth/me`, `/api/sync/config`, and pull smoke. |

## Deviations

- The rehearsal began before all scheduling gates were satisfied. Missing
  R10/R11/R12 adapters made the original standing `partial`; accepted successor
  adapters now close R10/R11, but R12 still fails.
- R2 and early R8 SQL used `PGSERVICE` plus `PGDATABASE`; this did not override the service database as expected. Corrected evidence uses explicit `psql "service=... dbname=..."` connections.
- R8 DB2 recovery-point attempt failed because DB2 was a restored target with `archive_mode` disabled. R8 was rerun against DB1 to use the live backup adapter.
- R8 post-upgrade pull first used `last_pull_watermark`; the accepted pull contract uses `since_watermark`. Corrected pull evidence was retained.
- The Keycloak realm import file and generated password files were lab secrets and must not be committed. The import-on-start JSON was removed during cleanup.
- The 2026-06-18 R12 fresh-session attempt recovered approved SSH access and
  current state without hidden shell history, but found recovery posture stale:
  the encrypted repository still reported only full backup `20260617-022519F`
  stopped at `2026-06-17 02:26:07+00`, while the monitoring textfile metric
  recorded last backup success `2026-06-17T15:26:33Z` and Alertmanager had an
  active critical `DatarunBackupStale` alert. Protected bearer-token smoke
  could not be rerun because the retained evidence records the expected
  rotated principal but does not provide a documented fresh-session token
  acquisition path or retained token.

## Cleanup State

Primary reference service remains running for follow-up inspection. Disposable
compose projects `datarun-r2`, `datarun-restore-check`, and `datarun-r8-db1`
were stopped after evidence capture.

Cleanup completed after evidence inspection on 2026-06-17:

- removed disposable DB1 databases `datarun_upgrade` and `datarun_r9_denied`;
- removed disposable DB2 databases `datarun_r2` and the failed-path
  `datarun_upgrade` residue;
- removed narrow disposable `pg_hba.conf` lines for those databases and
  reloaded PostgreSQL on DB1/DB2;
- removed temporary runtime config/env directories under
  `/opt/datarun-lab/runtime-config-*` and `/opt/datarun-lab/deploy/nw067-r*.env`;
- removed the Keycloak import-on-start JSON;
- retained `/opt/datarun-lab/evidence/NW-067-2026-06-17` for the retention
  period.

## Follow-Up Work

- Retain accepted successor adapters NW-075, NW-076, NW-077, and NW-078 as the
  backup encryption, DB credential rotation, Keycloak/JWKS rotation, and
  monitoring/alert-delivery evidence for the successor path.
- Rerun R12 from a genuinely fresh privileged Hamza session with accepted
  successor adapters NW-075 through NW-081 in place.

## Successor Standing Update

As of 2026-06-17, NW-075, NW-076, NW-077, and NW-078 have accepted successor
adapter records. This does not change this record's original result: NW-067
remains partial and not accepted until R12 is executed from a genuinely fresh
privileged Hamza session.

On 2026-06-18, a fresh-session R12 attempt reconstructed the current reference
state from the indexed docs and retained evidence only: app host
`datarun-app.lab`, DB1 `datarun-db1.lab`, ops/Keycloak host `keycloak.lab`,
candidate image digest
`sha256:5f246547e1292092b133540a86efe230fd9f9bacc1f73bccc83e8644e4fb82e2`,
runtime config `/opt/datarun-lab/runtime-config-nw076-g2`, Prometheus and
Alertmanager running, Keycloak new RSA provider active with old key passive,
and active rotated principal subject `53b46770-c03a-4f3f-b25c-3321c1e5af15`
bound to actor `22222222-2222-4222-8222-222222222222`. Live app readiness and
liveness were `UP`; unauthenticated protected endpoints returned `401`;
Prometheus was ready; all configured monitoring targets were `up`;
Alertmanager was ready; DB1 had 10 successful Flyway rows, 8 events with max
watermark 9, config version 1, and 2 active principal bindings. The attempt
did not pass because the latest recovery point was outside the 1-hour RPO and
authorized protected smoke lacked a documented fresh-session token path.

Later on 2026-06-18, NW-080 refreshed the accepted encrypted backup recovery
point with pgBackRest diff backup `20260617-022519F_20260618-131808D`,
latest recoverable time `2026-06-18T13:18:14Z`, RPO age `66` seconds, and
resolved `DatarunBackupStale`. NW-081 then proved a fresh-session protected
smoke token path for the active rotated synthetic principal and reran
`/api/auth/me`, `/api/sync/config`, and `/api/sync/pull` with HTTP 200 while
retaining no raw token material. These successor records clear the two R12
blockers found by the failed 2026-06-18 attempt, but the R12 scenario itself
still must be rerun from a genuinely fresh privileged Hamza session before
NW-067 can be accepted.
