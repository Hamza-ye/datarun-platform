# First Deployment Workshop Stage 4 Software Architecture Packet

Status: workshop-stage-output

Date: 2026-06-12

Role: Software Architect

Authority: none. This packet maps Candidate 1 to existing system boundaries
and successor routes. It does not redo Stage 1 stewardship, authorize
implementation, create architecture authority, or decide product scope.

## 1. Software Architect Role Boundary

Stage 4 maps Candidate 1 to existing system boundaries and successor routes.
Candidate 1 is treated as configured assigned capture, offline/local save,
sync, correction, optional subject link, freshness, and unresolved issue
visibility over accepted kernel constructs.

Stage 4 does not:

- redo Stage 1 source-order/stewardship work;
- authorize implementation;
- create new contracts or authority;
- decide product scope.

## 2. Candidate 1 Technical Boundary Map

| Surface | Candidate 1 boundary |
|---|---|
| Contracts | Reuse existing envelope, sync protocol, flag catalog, deployer shape DSL, config package schema, platform payload schemas, pattern definitions, and shared fixtures. No new envelope fields/types, config sections, report contract, or scope contract. |
| Server | Reuse append-only event store, config publication, sync, assignment authorization, identity alias projection, integrity detection, and resolver services. Compose views from existing projections/events rather than introducing durable workflow state. |
| Mobile | Reuse setup/connect, actor-resolved session, per-actor partitions, local pending events, work list, form capture, sync panel, advisory warnings, and selective retention. Mobile remains advisory for policy/state anomalies. |
| Config | Use deployer shapes, activities, role-action maps, severity overrides, expressions, and platform pattern bindings already delivered by atomic config packages. No config scripts, deployer state machines, custom query namespaces, or scope logic. |
| Sync | Reuse idempotent push, ordered scoped pull, actor-scoped `device_sync_state`, independent subject-history cursor, and current assignment-derived scope. Latest synced view is not live truth. |
| Identity | Use production principal binding for actor identity and existing subject refs/aliases for optional subject-linked capture. Missing-subject capture must remain unlinked/candidate/review-oriented, not canonical lifecycle. |
| Authorization | Reuse assignment-derived access, fixed geography/subject-list/activity/temporal scope axes, and assignment-admin command capability where applicable. No IdP group/claim/JWT `actor_id` authority. |
| Integrity | Reuse accept-and-flag, flag catalog categories, unresolved-flag exclusion, exact designated-resolver resolution, and event traceability. No direct flag mutation, auto-resolution, or resolver reassignment. |
| Admin surfaces | Current web admin/config surfaces are product-surface-partial/development-only. Candidate 1 design can reference setup/config/assignment/review needs, but production admin auth and online binding-admin UI remain successor routes. |

## 3. Accepted Constructs Reused By Candidate 1

- BAR-001, BAR-002, BAR-015, NW-026/S00: closed envelope/type vocabulary,
  append-only record and correction behavior, stable historical event location
  path.
- BAR-003, BAR-004, BAR-007, NW-025/S19: scoped sync, independent subject
  history, assignment-derived access, stale offline accept-and-flag.
- BAR-008, NW-055: mobile selective retain, per-actor local partitions,
  actor-scoped sync state.
- BAR-009: subject aliases and merge/split projection support, without S06
  lifecycle.
- BAR-010, BAR-011, NW-032/S23, NW-057: deploy-time config validation, atomic
  config packages, fixed `context.*` boundary, mobile current/pending config
  promotion.
- BAR-012, BAR-014: platform-owned patterns and server/mobile projection
  equivalence.
- BAR-006, BAR-013, NW-029/S21, NW-033/S26: flags, resolver equality, review
  state participation, traceable unresolved issue inputs.
- BAR-104, NW-037, NW-038, NW-040: OIDC/JWKS server auth through explicit
  principal bindings, not claims or groups.
- NW-050: assignment-admin create/end command capability with same-assignment
  containment.

## 4. Successor Route Map

| Route | Stage 4 placement |
|---|---|
| S06/entity lifecycle | BAR-105/NW-021/NW-036 successor lane. Candidate 1 may link to known subjects as an S01-compatible path, but must not create active/inactive/retired lifecycle truth or discovered-unit lifecycle unless FD-PKT-001 moves S06 before Candidate 1 implementation planning. |
| Admin/mobile auth | BAR-104 kernel accepted, but production web admin auth, mobile OIDC/Keycloak login, token lifecycle, and online binding-admin API/UI need routed decisions. |
| Retention/security | BAR-106/NW-054. Local expiry, decommissioning, sealed-partition recovery, encryption, redaction/no-local-retention, and token/session retention are not Candidate 1 kernel behavior. |
| Reporting/import-export | NW-044. Candidate 1 may show freshness/unresolved issue inputs, but no reporting warehouse/API/export/import or broad audit/history surface. |
| Conflict review UX | Single-flag review language can be designed over current resolver semantics. Batch queues, pending-match derivations, automation, resolver reassignment, and auto-resolution route through NW-045/BAR-102/BAR-103. |
| Subject/query/custom scope | NW-053/BAR-108. UI filters, report filters, auditor views, or admin convenience must not become access authority or query/custom scope. |
| Ops readiness | NW-056 candidate ops route. Production wording needs TLS/secrets/backup/restore/Flyway rollback/monitoring/auth-manifest/config/assignment runbooks and rehearsal evidence. |

## 5. Technical Dependency And Sequencing Notes

1. Candidate 1 should first define a current-kernel product/spec slice: setup,
   assigned work, capture, local save, sync, correction, optional subject link,
   freshness, unresolved issue visibility.
   FD-PKT-001 must confirm whether that slice stays S01-compatible or whether
   S06/BAR-105 needs to move earlier.
2. UX view models should be adapter-level compositions over existing events,
   projections, flags, sync metadata, and assignment scope. If multiple shipped
   components need a new shared shape, route contract work before
   implementation.
3. Production admin auth should precede productionizing admin/config/review
   surfaces.
4. Mobile OIDC/token lifecycle should precede any claim that mobile login is
   production-ready; current raw bearer connect remains a constrained bridge.
5. Retention/security must precede exit/decommission/no-local-retention
   promises.
6. NW-044 must precede durable reporting/dashboard/API/export/import
   implementation.
7. NW-053 must precede any scope behavior beyond current fixed axes.
8. QA should preserve evidence class separation: accepted kernel,
   scenario-runtime-evidenced path, product-surface-partial UI, and
   operator-deployable-with-constraints ops.

## 6. Contract / Schema / Authority Drift Risks

- Product terms such as work item, progress, pending review, blocked, route,
  known thing, or latest synced view becoming envelope fields, event types,
  durable state, scope mechanisms, or flag categories.
- Missing-subject capture becoming canonical entity lifecycle by accident.
- Freshness UI implying live truth instead of latest synced/projected view.
- Admin UX relying on fixed dev actor, request-body actor, IdP
  roles/groups/claims, or JWT `actor_id`.
- Mobile warnings becoming local authoritative rejection.
- Config UX introducing scripts, custom traversals, loops, query authority, or
  deployer-authored state machines.
- Review UX mutating flags directly or bypassing exact designated-resolver
  semantics.
- Reporting/audit UX creating unapproved broad history reads, export/import
  contracts, or query/custom scope.
- Ops language treating development compose/admin screens as production
  hardening.

## 7. Questions For Later Roles

UX Architect:

- Which Candidate 1 states must be visible on the first screen: saved locally,
  waiting to sync, synced, failed, synced with issue, access ended, latest
  synced?
- How should missing-subject/unlinked-known-thing be worded so it does not
  imply lifecycle creation?
- What minimum freshness and unresolved-issue display avoids live-truth
  overclaim?

Mobile App Builder:

- Which current local states can support the UX vocabulary without new storage
  authority?
- What actor-switch and pending-work warnings are feasible over IDR-030
  partitions?
- Which mobile flows still depend on mobile OIDC/token lifecycle routing?

Reality Checker:

- Which Candidate 1 claims are accepted, runtime-evidenced,
  product-surface-partial, or needs-decision?
- Where does the workshop language risk implying turnkey production readiness?

Test Results Analyzer:

- Which existing BAR/NW tests cover Candidate 1 directly, and where are manual
  walkthroughs still required?
- What evidence gates are needed for admin auth, mobile login,
  retention/security, reporting, and ops before production claims?

## 8. Advice To Workshop Lead

Future task packets should name:

- one lane;
- one role owner;
- current claim status;
- exact source files/contracts;
- accepted constructs reused;
- successor routes excluded;
- expected evidence class.

Each packet should include the UX term plus its architecture backing,
especially for known thing, assigned work, needs review, latest synced view,
correction, and access ended.

Implementation packets should stay narrow. Do not combine Candidate 1 capture
UX with admin auth, mobile OIDC, retention/security, reporting/export, new
scope, or conflict automation.

For any code task, make explicit whether the work is product design,
view-model composition, contract work, platform decision, or implementation,
and include targeted Maven/Flutter/manual evidence expectations.
