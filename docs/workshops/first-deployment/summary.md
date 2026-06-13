# First-Deployment Summary

Status: workshop closed; first implementation slice routed

Date: 2026-06-13

## Product Outcome

The first useful deployment surface is assigned, configured capture that works
offline and explains local save, sync progress, failure, freshness, correction,
and unresolved issues honestly.

Candidate 1 may use existing subject links, but the first implementation slice
does not need subject or entity-lifecycle behavior.

## Product Acceptance

- A field user can complete configured capture while offline.
- The app distinguishes saved on this device, waiting to sync, syncing, synced,
  and failed sync.
- Failed sync preserves local pending work and gives a retry path.
- A successful-sync timestamp is not shown for a failed attempt.
- Latest synced data is not described as live field truth.
- Corrections remain appended records rather than in-place history edits.
- Product copy does not imply canonical registry or entity-lifecycle truth.

## Lane Register

| Lane | Standing | Route |
|---|---|---|
| Candidate 1 capture/offline/sync | Kernel accepted; product surface partial | Start with the mobile sync-status implementation task. |
| S06/entity lifecycle | Deferred from baseline; product need remains visible | BAR-105 / NW-021 successor decision when maintained known things or lifecycle states are required. |
| Production auth/admin/mobile login | Server auth kernel accepted; product surfaces partial | Separate product/platform decision. |
| Retention/security/device lifecycle | Future decision | BAR-106 / NW-054. |
| Reporting/import-export | Future decision | NW-044. |
| Conflict review UX and automation | Single-flag semantics accepted; scaled operations deferred | NW-045 and existing resolver guardrails. |
| Subject/query/custom scope | Future decision | BAR-108 / NW-053. |
| Operations readiness | Operator-deployable with constraints | Separate runbook and rehearsal evidence. |

## Claim Standing

| Claim | Standing |
|---|---|
| Append-only capture and correction kernel | `accepted` |
| Scoped offline sync and pending-event preservation | `accepted` |
| Candidate 1 operational capture over current constructs | `runtime-evidenced` |
| Current mobile setup, work list, capture, and sync UI | `product-surface-partial` |
| Production mobile OIDC/login | `needs-decision` |
| Reporting, retention/security, and broad conflict operations | `needs-decision` |
| Turnkey production readiness | `blocked` pending separate ops/product evidence |

## Technical Boundary

| Surface | First-deployment use |
|---|---|
| Contracts | Reuse the existing envelope, sync protocol, flag catalog, config package, shapes, patterns, and fixtures. |
| Server | Reuse append-only events, assignment authorization, sync, projections, and flags. |
| Mobile | Compose user-visible state from `AppState`, local pending events, `SyncResult`, timestamps, projections, and flags. |
| Config | Reuse current atomic config packages and shape-driven forms. |
| Sync | Reuse bearer-bound push/pull and actor-scoped progress. Do not change protocol or watermarks. |
| Identity | Existing subject refs and aliases remain available; no lifecycle model is added. |
| Authority | Assignment and principal-binding authority remain server-derived. Mobile remains advisory. |
| Integrity | Structurally valid state/policy anomalies remain accept-and-flag. |

## S06 Disposition

The workshop selected parallel S06 discovery rather than silently absorbing
entity lifecycle into Candidate 1. That conclusion is retained, but S06 is not
a prerequisite for the selected sync-status slice because the slice has no
known-set, subject-link, lifecycle, duplicate, merge, or split behavior.

If a later slice needs maintained known things, active/inactive/retired truth,
registry stewardship, candidate promotion, or merge/split UX, stop and route
BAR-105 / NW-021 before implementation.

## Current Dispatch

Implement [implementation-task.md](implementation-task.md). It fixes the
current false-success timestamp behavior and adds tested presentation states
for saved locally, waiting, syncing, synced, and failed sync.

Use `AGENTS.md`, `docs/status.md` Current Routing, the mobile section of
`docs/implementation/module-interfaces.md`, and the exact code/tests named by
the task. No additional workshop or gate document is required.
