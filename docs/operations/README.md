# Operations

Status: active operational documentation index

This directory contains durable operational choices, repeatable procedures,
and executed evidence. Use
[documentation-organization.md](../documentation-organization.md) for naming,
metadata, lifecycle, traceability, and supersession rules.

| Area | Role |
|---|---|
| [policies/](policies/README.md) | Deployment-owner choices, responsibilities, constraints, and escalation. |
| [runbooks/](runbooks/README.md) | Repeatable executable operating procedures. |
| [rehearsals/](rehearsals/README.md) | Exercise plans and dated execution evidence. |

Operational documents are subordinate to CDL, contracts, accepted platform
specifications, and implemented behavior. A runbook must describe what the
system actually supports; it cannot create missing runtime capability by
documentation.

## Active Index

| Document | Type | Status | Owner | Source |
|---|---|---|---|---|
| [First reference deployment policy](policies/first-reference-deployment-policy.md) | Operational policy | `accepted` | Hamza | NW-064; NW-067 amendment |
| [Production deployment runbook](runbooks/production-deployment-runbook.md) | Runbook | `accepted` | Hamza | NW-066; NW-067 amendment |
| [Production deployment rehearsal plan](rehearsals/production-deployment-rehearsal-plan.md) | Rehearsal plan | `accepted` | Hamza | NW-066; NW-067 amendment |

NW-063 selected the reference deployment class, NW-064 supplies its accepted
operating policy, NW-065 supplies tested reference tooling, and NW-066 supplies
the accepted runbook and reusable rehearsal plan. The 2026-06-17 NW-067 attempt
is recorded as partial: R1-R9 produced useful reference-environment evidence,
and successor adapters NW-075 through NW-078 now cover backup encryption,
credential/JWKS rotation, and alert delivery. The 2026-06-18 fresh-session R12
attempt failed on stale backup/RPO posture and a missing documented
fresh-session bearer-token path; NW-080 and NW-081 accepted fixes for those
blockers, but R12 still must be rerun before any NW-067 pass claim.
Independent human continuity remains explicitly unproven.
