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
