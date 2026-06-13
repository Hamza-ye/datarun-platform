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
| [First reference deployment policy](policies/first-reference-deployment-policy.md) | Operational policy | `accepted` | Hamza | NW-064 |

NW-063 selected the reference deployment class, and NW-064 now supplies its
accepted operating policy. NW-065 tooling may proceed; runbook and rehearsal
successors remain dependent on tested implementation.
