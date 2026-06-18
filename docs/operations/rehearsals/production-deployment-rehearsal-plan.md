# Production Deployment Rehearsal Plan

Status: accepted
Document type: rehearsal_plan
Owner: Hamza
Source: NW-066 and
`docs/agent-working-surface/prompts/NW-066-write-production-deployment-runbook.md`
Amended by: NW-067 solo-owner operating-model gate
Authority: operates within the accepted first reference deployment policy,
NW-063, accepted NW-065 tooling, and the production deployment runbook
Last reviewed: 2026-06-14
Supersedes: none
Related:
`docs/operations/runbooks/production-deployment-runbook.md`,
`docs/operations/policies/first-reference-deployment-policy.md`, and
`docs/agent-working-surface/prompts/NW-067-rehearse-production-deployment-recovery.md`

## 1. Purpose And Claim Boundary

NW-067 uses this plan to test the accepted runbook in a clean, isolated
reference environment with synthetic data and identities. The result may show
that the named environment is repeatably deployable and recoverable under the
accepted policy. It cannot approve real data/users, mobile OAuth/OIDC login,
production web administration, NW-054 device security, Kubernetes,
multi-host availability, managed-provider support, or independent human
continuity.

## 2. Runbook And Candidate Under Test

Before scheduling, record:

- runbook repository commit and document status;
- previous accepted and candidate repository commits, image digests, OCI
  source/revisions, compatibility evidence, PostgreSQL 16 service/version,
  host/runtime versions, and provisioning input SHA-256s;
- public synthetic DNS name and external TLS/proxy implementation;
- exact deployment-owned backup/PITR, restore, monitoring/alert, secret,
  DNS/TLS, OIDC token, and evidence adapters;
- operator and approver Hamza, solo-owner risk acknowledgement, incident
  contact, cold-recovery account/session boundary, and `Asia/Aden` exercise
  window;
- source, restored, and upgrade environment identifiers;
- external raw-evidence location and 13-month retention control.

**Scheduling gate:** NW-066 must be accepted, all adapters must be executable,
both the previous accepted and candidate images must be available by digest,
the candidate must pass full preflight, upgrade/rollback compatibility
evidence must be recorded, and the solo cold-recovery session/account boundary
must be prepared. Otherwise a new or restarted production-deployment
rehearsal remains blocked.

## 3. Environment And Data

Use:

- one Linux application host with only loopback app/management listeners;
- external TLS proxy and synthetic DNS name;
- isolated external PostgreSQL 16 source and restore targets;
- external read-only mounted secrets/config/trust material;
- synthetic IdP tenant/principals, actors, assignments, subjects, and events;
- external metrics/log destinations and alert delivery to Hamza;
- no production database copy, personal data, production credential, or
  production DNS name.

Prepare reviewed provisioning files for principal bindings, complete config,
initial assignment, valid push/pull smoke, and deliberate invalid candidates.
Do not commit tokens, passwords, certificates containing private keys, or raw
evidence.

## 4. Roles And Safety

Hamza holds deployment, service, host, DNS/TLS, database, secrets, monitoring,
release, incident, support, data/compliance, evidence, and recovery authority
for this synthetic rehearsal. Record every use of the accepted solo-owner
approval model and its required evidence.

No second operator is required for this solo-owner rehearsal. R12 must use a
fresh privileged session and only indexed operational state. A separate
Hamza-owned account may test privilege separation but must not be represented
as independent human continuity. Shared human credentials remain forbidden.

Abort immediately on:

- missing canonical contract resources or failed full preflight;
- unsafe/partial migration or pressure to edit Flyway history;
- failed restore, RPO over 1 hour, or RTO over 8 hours;
- actor/binding drift, cross-scope exposure, or event mutation/loss;
- secret/token/private data in logs, commands, screenshots, or evidence;
- use of development admin surfaces or undocumented database writes.

Preserve secret-safe evidence and mark the rehearsal failed. Do not weaken
policy, tests, or acceptance criteria.

## 5. Evidence And Timing

Create one external directory per scenario and a sanitized index containing:

- start/end timestamps in UTC and `Asia/Aden`;
- operator, approver/exception, runbook step, exact candidate identity;
- commands or named deployment-owned adapter version used;
- expected and observed result, timing, deviations, and stop decision;
- evidence filenames/checksums without secret values;
- cleanup/revocation result and follow-up NW item.

At disaster declaration, record the backup adapter's latest recoverable
timestamp and verify its age is no more than 1 hour. RPO is the distance from
the declaration to that boundary, not an elapsed clock. Start the RTO clock at
the declaration and stop it only when the restored minimum service passes
readiness, auth, config, and authorized-pull smoke.

## 6. Scenario Matrix

Execute in order unless a recorded dependency requires a different sequence.

| ID | Scenario and inject | Required procedure | Pass evidence | Failure/cleanup |
|---|---|---|---|---|
| R1 | Clean install on empty PostgreSQL 16 | Runbook Sections 2-7 | Approved digest; full preflight; TLS boundary; successful Flyway history; readiness; explicit binding | Fail on resource/default/migration/health/exposure defect; preserve target for diagnosis |
| R2 | Initial migration repeatability | Rebuild a second empty database and repeat Sections 5-7 | Same successful migration set once; no checksum drift/manual SQL | Fail on partial/checksum/lock/unexpected DDL; destroy only after evidence |
| R3 | TLS, provider, and mounted secrets | Exercise certificate/name, firewall, JWKS, valid token, wrong-audience token, wrong-issuer token, secret readability, and log inspection | HTTPS-only public path; management private; valid token accepted; wrong issuer/audience rejected; no secret leakage | Revoke token/material and remove temporary DNS |
| R4 | Binding manifest apply/reapply/change | Run Section 7 with reviewed active, exact reapply, and reviewed deactivate/rebind operations | Idempotent hashes, append-only audit, expected old/new principal behavior | Fail on ambiguity, partial apply, group/claim authority, or unaudited change |
| R5 | Config and initial assignment | Run Sections 8-9, exact reapply, and contained create/end command probes | One config version for exact input; one initial assignment event; create/end require accepted command capability plus containment | Fail on dev surface, invalid publication, duplicate bootstrap, command/containment drift, or inferred authority |
| R6 | Device bearer auth/config/sync | Run Section 10 with synthetic configured event and assigned actor | Correct `/api/auth/me`; config/ETag; accepted push; authorized monotonic pull; honest pending/freshness | Treat mismatch, cross-scope data, rewrite/loss, or false status as fail |
| R7 | Backup and isolated clean restore | Seed known history, create recovery point, inject source loss, declare disaster, run Section 12 | Recovery age <=1 hour; service restored <=8 hours; schema/events/config/bindings/assignments and smoke consistent | Failed restore or missed objective fails NW-067; retain failed target |
| R8 | Previous-to-candidate upgrade | Start previous accepted image/data, create recovery point, apply candidate through Section 13 | Forward migration; old history interpretable; complete post-upgrade smoke | Route through Section 14; never assume old-image compatibility |
| R9 | Failed deployment and migration response | Prove invalid config/image stops at preflight, then use the approved network adapter to deny database connectivity during a disposable first start before Flyway can run | Failure before traffic; unchanged/consistent schema history; correct evidence/severity; explicit rollback/restore/forward-fix decision; no manual repair | Fail if safe state/authority cannot be identified |
| R10 | Database credential, binding, and JWKS rotation | Run Section 15 with two synthetic generations | New generation works; old rejected at intended boundary; binding/authority stable; timing recorded | Revoke all test material; fail on drift, leakage, or unbounded outage |
| R11 | Alert and incident response | Inject readiness, database/JWKS, backup freshness, certificate, and capacity conditions through approved adapters | Alerts reach owner at accepted thresholds; timeline, triage, evidence, escalation, and recovery decision recorded | Missing/false/unowned alert or leaked telemetry fails scenario |
| R12 | Solo cold recovery | Close the active session, start a fresh privileged session, and use only indexed docs, approved access, execution record, and evidence index | Hamza reconstructs current state from retained evidence, runs smoke, identifies the recovery point, and names stop conditions and recovery authority without hidden session state | Undocumented knowledge, inaccessible approved account, hidden prior-session state, or unclear recovery authority makes the result partial or failed |

## 7. Failure Inject Rules

All injects use disposable synthetic infrastructure and pre-approved adapters.
Do not introduce an unreviewed schema migration merely to force failure.

Approved inject classes:

- invalid or missing non-secret configuration that production validation must
  reject before service;
- unavailable PostgreSQL/JWKS endpoint at the deployment adapter boundary;
- revoked synthetic credential or retired synthetic provider key;
- stopped server/readiness failure;
- backup freshness threshold breach reported by the selected backup monitor;
- certificate-expiry test signal from the selected TLS/monitoring adapter;
- host/database capacity threshold generated through the selected safe load or
  monitoring-test mechanism;
- database connectivity denial before a disposable first-start Flyway attempt,
  using the selected network/failure-inject adapter.

An inject must not alter accepted platform semantics, bypass authority, expose
real data, or require cleanup by deleting/revising application events.

## 8. Acceptance

Overall result is:

- **pass** only when R1-R12 pass, RPO/RTO are met, no stop condition occurs,
  cleanup succeeds, and evidence is complete;
- **partial** when completed scenarios are truthful but one or more required
  scenarios/adapters, including solo cold recovery, are unavailable or
  inconclusive without a safety failure;
- **fail** when any abort condition or scenario failure occurs.

Clean install, restore, upgrade, failure response, rotation, alert delivery,
incident triage, and solo cold recovery are mandatory. Partial execution cannot
strengthen the production-readiness claim.

## 9. Cleanup And Record

After each scenario:

1. Confirm required evidence and checksums exist outside git.
2. Revoke synthetic tokens, credentials, bindings, and temporary access.
3. Remove temporary DNS/TLS/secret material through approved adapters.
4. Stop/destroy disposable source and restore targets only after evidence
   retention is confirmed.
5. Verify no secrets or sensitive data entered repository files.

Create and index:

```text
docs/operations/rehearsals/YYYY-MM-DD-production-deployment-reference-environment.md
```

The record must identify exact versions, actual procedures, observations,
timings, deviations, cleanup, evidence locations, follow-up NW rows, and final
pass/partial/fail result. Reusable runbook corrections land separately from
the dated record when independently reviewable.
