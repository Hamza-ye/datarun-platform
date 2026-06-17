Status: partial
Document type: rehearsal_record
Owner: Hamza
Source: NW-067; `docs/agent-working-surface/prompts/NW-067-rehearse-production-deployment-recovery.md`
Authority: operates within `docs/operations/policies/first-reference-deployment-policy.md`, `docs/operations/runbooks/production-deployment-runbook.md`, and `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`
Last reviewed: 2026-06-17
Supersedes: none
Related: `docs/operations/rehearsals/production-deployment-rehearsal-plan.md`; raw evidence at `/opt/datarun-lab/evidence/NW-067-2026-06-17`

# 2026-06-17 Production Deployment Reference Environment Rehearsal

## Result

Final result: `partial`.

R1-R9 produced useful synthetic reference-environment evidence, including clean install, corrected clean migration repeatability, auth/TLS negative checks, provisioning idempotency, sync smoke, measured restore, previous-to-candidate upgrade, and contained failed-start response. NW-067 is not accepted because R10 credential/JWKS rotation, R11 alert delivery, and R12 fresh-session solo cold recovery did not pass. The backup adapter also reported `cipher: none`, so backup encryption policy compliance remains unproven even where restore timing passed.

This record does not approve real production, real data, mobile OAuth/OIDC login UX, independent human continuity, or turnkey monitoring/rotation readiness.

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
| R10 credential, binding, JWKS rotation | Partial / blocked | R4 covers binding rotation evidence only. Database credential and JWKS rotation were not executed because no reviewed two-generation DB secret adapter or Keycloak signing-key rotation adapter was present. |
| R11 alert and incident response | Blocked | No Prometheus/Alertmanager/Grafana or equivalent alert-recipient adapter was found; only Keycloak was running on the ops VM. Missing alert destination blocks pass. |
| R12 solo cold recovery | Not executed | Requires closing this active session and starting a fresh privileged Hamza session using only indexed docs, approved access, execution record, and evidence index. |

## Deviations

- The rehearsal began before all scheduling gates were satisfied. Missing R10/R11/R12 adapters make the final standing `partial`.
- R2 and early R8 SQL used `PGSERVICE` plus `PGDATABASE`; this did not override the service database as expected. Corrected evidence uses explicit `psql "service=... dbname=..."` connections.
- R8 DB2 recovery-point attempt failed because DB2 was a restored target with `archive_mode` disabled. R8 was rerun against DB1 to use the live backup adapter.
- R8 post-upgrade pull first used `last_pull_watermark`; the accepted pull contract uses `since_watermark`. Corrected pull evidence was retained.
- The Keycloak realm import file and generated password files were lab secrets and must not be committed. The import-on-start JSON was removed during cleanup.

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

- NW-075: prove an encrypted backup/PITR adapter before any future pass claim.
- NW-076: add a reviewed two-generation DB credential rotation adapter and evidence procedure.
- NW-077: add a reviewed Keycloak/JWKS rotation adapter that proves new-key acceptance and old-key/token behavior without unreviewed admin changes.
- NW-078: install and exercise a monitoring/alert stack with alert delivery to Hamza.
- Perform R12 from a genuinely fresh privileged session with the accepted
  successor adapters in place.

## Successor Standing Update

As of 2026-06-17, NW-075, NW-076, NW-077, and NW-078 have accepted successor
adapter records. This does not change this record's original result: NW-067
remains partial and not accepted until R12 is executed from a genuinely fresh
privileged Hamza session.
