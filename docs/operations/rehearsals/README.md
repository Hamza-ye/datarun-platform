# Operations Rehearsals

Status: active rehearsal index

This directory stores exercise plans and dated execution records that test
runbooks, recovery claims, upgrades, incident handling, and operational
continuity. For the accepted initial solo-owner model, continuity is exercised
through a fresh-session cold-recovery scenario; it does not prove independent
human handoff.

Reusable procedure belongs in a runbook. One-time observed results belong in a
rehearsal record.

## Naming

- Plan: `<procedure>-rehearsal-plan.md`
- Record: `YYYY-MM-DD-<procedure>-<environment>.md`

## Required Plan Content

- required metadata from
  [documentation-organization.md](../../documentation-organization.md);
- runbook and version/commit under test;
- environment and data assumptions;
- scenarios and failure injects;
- success and abort criteria;
- evidence capture and cleanup plan.

## Required Record Content

- required metadata with `Document type: rehearsal_record`;
- date, environment, and commit/artifact versions;
- operator role or participants;
- procedure and commands actually used;
- observed results and timings;
- failures, deviations, and retained evidence;
- runbook corrections and follow-up NW rows;
- final pass, partial, or fail result.

## Index

| Rehearsal | Type | Status/result | Runbook | Source NW | Date |
|---|---|---|---|---|---|
| [Production deployment rehearsal plan](production-deployment-rehearsal-plan.md) | Plan | `accepted` | [Production deployment](../runbooks/production-deployment-runbook.md) | NW-066; NW-067 amendment | Not executed |
| [2026-06-17 production deployment reference environment](2026-06-17-production-deployment-reference-environment.md) | Record | `partial` | [Production deployment](../runbooks/production-deployment-runbook.md) | NW-067 | 2026-06-17 |
| [2026-06-17 encrypted backup/PITR adapter](2026-06-17-encrypted-backup-pitr-adapter.md) | Record | `accepted` | [Production deployment](../runbooks/production-deployment-runbook.md) | NW-075 | 2026-06-17 |
| [2026-06-17 DB credential rotation adapter](2026-06-17-db-credential-rotation-adapter.md) | Record | `accepted` | [Production deployment](../runbooks/production-deployment-runbook.md) | NW-076 | 2026-06-17 |
| [2026-06-17 Keycloak/JWKS rotation adapter](2026-06-17-keycloak-jwks-rotation-adapter.md) | Record | `accepted` | [Production deployment](../runbooks/production-deployment-runbook.md) | NW-077 | 2026-06-17 |
| [2026-06-17 monitoring alert adapter](2026-06-17-monitoring-alert-adapter.md) | Record | `accepted` | [Production deployment](../runbooks/production-deployment-runbook.md) | NW-078 | 2026-06-17 |
| [2026-06-18 encrypted backup recovery-point refresh](2026-06-18-encrypted-backup-recovery-point-refresh.md) | Record | `accepted` | [Production deployment](../runbooks/production-deployment-runbook.md) | NW-080 | 2026-06-18 |
| [2026-06-18 fresh-session protected-smoke token path](2026-06-18-fresh-session-protected-smoke-token-path.md) | Record | `accepted` | [Production deployment](../runbooks/production-deployment-runbook.md) | NW-081 | 2026-06-18 |
