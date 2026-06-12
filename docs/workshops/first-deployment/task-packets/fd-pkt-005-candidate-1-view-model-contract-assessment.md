# FD-PKT-005 Candidate 1 View-Model/Contract Assessment

Status: prepared task-packet draft

Date: 2026-06-13

Authority: none. This packet assesses Candidate 1 view composition and contract
pressure. It does not authorize implementation, change CDL, BAR, NW,
contracts, schemas, APIs, runtime behavior, event vocabulary, or product scope
by itself.

## 1. Header

Packet ID: FD-PKT-005

Lane: Candidate 1 view-model/contract assessment

Assigned role: Software Architect + steward accountability

Claim status: `needs-routing-check`

Objective: assess whether Candidate 1 can use adapter-level view composition
over existing events, projections, flags, sync metadata, assignments, config,
subject refs, and mobile actor partitions, or whether a routed shared
view-model/contract packet is required before any implementation dispatch.

Authority and source order:

1. CDL/contracts and active status remain binding authority.
2. BAR/NW standing may be referenced only as accepted, deferred, or future
   standing, not expanded into new implementation scope.
3. FD-PKT-001, FD-PKT-002, FD-PKT-003, FD-PKT-004, and FD-PKT-101 are packet
   inputs.
4. Stage 4 architecture output is assessment context, not architecture
   authority.
5. Stage 5 mobile output is mobile feasibility/evidence context, not
   implementation authority.
6. Scenario/user-fit and workshop docs are product evidence context, not
   architecture authority.

Operational/persona labels used:

- Setup owner, field user, supervisor/reviewer, operator/support, shared-device
  user, and auditor are acting contexts only.
- Every downstream artifact using those labels must map them as:

```txt
actor + active assignment + role + scope + time + activity/context
-> available actions and visible data
-> projected operational surface
```

Allowed files/contracts:

- Allowed write: this file only.
- Allowed source context: `AGENTS.md`, `docs/status.md` Current Routing,
  `docs/agent-working-surface/first-deployment-task-packet-router.md`,
  `docs/workshops/first-deployment/README.md`,
  `docs/workshops/first-deployment/stage-4-software-architecture.md`,
  `docs/workshops/first-deployment/stage-5-mobile.md`,
  `docs/workshops/first-deployment/stage-8-task-packet-backlog.md`,
  FD-PKT-001, FD-PKT-002, FD-PKT-003, FD-PKT-004, and FD-PKT-101.
- Optional read-only contract context: existing envelope, sync protocol, flag
  catalog, shape-format, config-package, platform payload shapes, pattern
  definitions, and shared fixtures.
- No contract edits, schema edits, fixture edits, code edits, API edits,
  router/status/backlog/README edits, BAR/NW/CDL edits, workshop control edits,
  runtime behavior edits, test execution, implementation tasks, or
  implementation authorization.

Accepted constructs reused:

- Closed 11-field event envelope and closed 6-value envelope `type`
  vocabulary.
- Append-only capture/correction behavior and historical event traceability.
- Idempotent sync push, actor-scoped pull, normal watermarks, subject-history
  backfill where already authorized, and read APIs where already present.
- Assignment-derived access and fixed accepted scope axes.
- Server-resolved actor identity through explicit principal binding,
  `/api/auth/me`, authenticated actor context, and prior server-resolved mobile
  sessions.
- Deployer-authored shape DSL, activities, role-action maps, context boundary,
  config package delivery, and platform-owned pattern definitions.
- Platform payload shapes for assignments, flags, and current identity alias
  behavior without promoting new lifecycle truth.
- Existing flags, unresolved-flag exclusion, exact designated-resolver
  semantics, and projection participation.
- Mobile local event store, pending push state, sync metadata, timestamps,
  config state, flags/advisories, and per-actor local partitions.

Excluded successor lanes:

- FD-PKT-101 S06/entity lifecycle, maintained known things, discovered-unit
  lifecycle, known-set source/authority, candidate promotion, registry
  stewardship, lifecycle states, duplicate workflow, and merge/split UX.
- FD-PKT-102 and FD-PKT-103 production web admin auth, online binding-admin
  UI/API, mobile OIDC/Keycloak login, token refresh/logout, token expiry,
  secure storage, offline re-auth, and IdP group/claim/JWT `actor_id`
  authority.
- FD-PKT-104 retention/security/device lifecycle, expiry, decommissioning,
  sealed recovery, local encryption, redaction/no-local-retention, and
  token/session retention policy.
- FD-PKT-105 reporting dashboards/APIs/export/import, broad audit/history,
  aggregate access, and reporting freshness semantics.
- FD-PKT-106 conflict review queues, batch handling, pending-match derivations,
  conflict automation, resolver reassignment, direct flag mutation, and
  auto-resolution.
- FD-PKT-107 subject/query/custom scope, auditor/report filters as authority,
  special read/write bypasses, and new scope mechanisms.
- FD-PKT-108 ops readiness, constrained-deployment release claims, TLS/secrets,
  backup/restore, migration rollback, monitoring, incident response, and
  staging rehearsal.

Commit boundary: docs-only packet creation. Do not edit any other file. Do
not commit.

## 2. Role Boundary

Software Architect + steward accountability may:

- assess whether Candidate 1 view states map to existing sources;
- classify surfaces as adapter composition, existing contract reuse, or
  route-needed;
- identify contract, schema, API, S06, authority, and mobile/local-state
  pressure before implementation dispatch;
- recommend downstream routing gates and evidence requirements.

Software Architect + steward accountability must not:

- implement, dispatch implementation, or authorize implementation;
- create a shared view-model contract, API, schema, event field, event type,
  scope mechanism, durable workflow state, stored current truth table, local
  authority, or S06 lifecycle behavior;
- decide product scope, S06 promotion, production auth, retention/security,
  reporting, conflict automation, custom scope, ops readiness, release
  readiness, or commits;
- treat persona labels as identity categories, authority primitives, fixed
  modules, config namespaces, product-area boundaries, or service boundaries.

Evidence gaps become routing gates or evidence work. They are not reasons to
erase Candidate 1 product need or hide S06.

## 3. Assessment Summary And Recommendation

Recommendation: Candidate 1 should proceed, for planning purposes only, as
adapter/view composition over existing constructs. Do not create a new shared
view-model contract, API, schema, event vocabulary, durable workflow state, or
S06 data model in this packet.

Rationale:

- The required Candidate 1 states can be explained from current sync metadata,
  mobile pending state, existing flags/projections, assignments/config, actor
  partitions, append-only events, and subject refs where available.
- The states are mostly user-facing interpretations of existing facts, not new
  platform facts. They should remain adapter labels unless a later
  implementation packet proves multiple shipped components need one stable
  wire shape.
- Server/API work, if any is later proposed, should compose current events,
  projections, flags, assignments, sync metadata, and config. It must not
  introduce durable workflow state or a new meaning for "current truth."
- Mobile work, if any is later proposed, should remain local UI composition
  over per-actor local state and server-resolved authority. It must not reject
  structurally valid work authoritatively or create actor/scope truth locally.
- S06 remains a hard visible gate. View-model names such as known thing,
  candidate, linked, unlinked, duplicate suspected, inactive, closed, moved,
  verified, merge, or split must not solve lifecycle by naming.

Implementation dispatch remains `no-go`. Later implementation packets may be
drafted only after FD-PKT-002 through FD-PKT-005 are gated and FD-PKT-101
chooses, promotes, splits, or explicitly excludes the S06 dependency.

## 4. Source Input Summary From FD-PKT-002/003/004/101 And Stage 4/5

| Source | Input to this assessment |
|---|---|
| FD-PKT-002 | Candidate 1 promises setup comprehension, assigned work, standalone capture, optional subject-linked capture, missing-known-thing/candidate capture, saved locally, sync states, failed-sync recovery, append-only correction, needs review, latest synced, access ended, shared-device language if claimed, and operator/support recovery. It forbids lifecycle truth, production auth, retention/security, reporting, conflict automation, custom scope, and implementation authorization. |
| FD-PKT-003 | FD-PKT-005 must answer whether Candidate 1 states can be composed from existing events, projections, flags, sync metadata, assignment scope, and config. It must stop on contract/schema/API/S06 model pressure, new authority movement, new durable state, custom scope, production reporting, or lifecycle behavior. |
| FD-PKT-004 | Mobile/offline validation maps labels to raw bearer plus `/api/auth/me`, active actor sessions, actor partitions, local pending events, sync metadata, flags/advisories, timestamps, config, assignment-derived scope, and append-only capture/correction. It asks FD-PKT-005 to decide whether any mobile state needs a shared view-model/API shape. |
| FD-PKT-101 | S06 remains `needs-decision`. Candidate 1 can stay S01-compatible only if subject-linked capture links to existing known things and missing-known-thing capture remains unpromoted review evidence. Any maintained known-set source, lifecycle vocabulary, candidate promotion, registry stewardship, duplicate workflow, or merge/split behavior routes through BAR-105/S06 before implementation. |
| Stage 4 | Architecture context says Candidate 1 should reuse existing contracts, event store, config publication, sync, assignment authorization, identity alias projection, integrity detection, resolver semantics, mobile local state, and projections. UX view models should be adapter compositions unless multiple shipped components need a stable shared shape. |
| Stage 5 | Mobile context says setup/connect, work list, capture, optional subject link, offline save, sync, correction, local flags, timestamps, actor partitions, and shared-device drain-or-seal are feasible as product-surface-partial evidence, not implementation authority or production readiness. |

## 5. Candidate 1 State Composition Matrix

| Candidate 1 state | Existing source to compose from | Classification | Route-needed trigger |
|---|---|---|---|
| Setup/connect | Raw bearer setup, `/api/auth/me`, server-resolved actor session, config package state, setup/config/assignment operational checks. | Existing contract reuse | Route FD-PKT-103/102 if productized mobile login, token lifecycle, online binding-admin UI/API, production admin auth, or IdP claim authority is needed. |
| Assigned work | Active assignment, role-action allowance, scope, time, activity/context, config activities, local projections, sync scope. | Existing contract reuse | Route FD-PKT-107 if visibility requires custom/query scope, UI filters as access authority, auditor/report scope, or special read/write bypass. |
| Standalone capture | Shape-driven form save, append-only capture event, local pending event, envelope payload governed by deployer shape. | Existing contract reuse | Stop if standalone capture needs new envelope fields, event types, mutable rows, in-place edits, hard local rejection, or lifecycle truth. |
| Subject-linked capture | Existing `subject_ref`, subject refs/history where authorized, alias projection where available, latest synced context. | Existing contract reuse | Route FD-PKT-101 if users need known-set source/authority, verification truth, active/inactive state, registry stewardship, duplicate workflow, or merge/split UX. |
| Missing-known-thing/candidate capture | Unlinked or candidate-oriented capture evidence, local pending event, later review/issue visibility where current flags/projections support it. | Adapter composition | Route FD-PKT-101 if the surface needs canonical registry creation, candidate promotion, automatic matching, discovered-unit lifecycle, maintained known-set state, or lifecycle status. |
| Saved locally | Mobile local event store and pending push state for the active actor/session partition. | Adapter composition | Route FD-PKT-104 if copy requires retention guarantee, device-loss recovery, local encryption, decommissioning, sealed recovery, or no-local-retention promise. |
| Waiting to sync | Pending push count, sync queue metadata, local event state, active actor partition. | Adapter composition | Stop if waiting state becomes durable workflow state, server acceptance, approval, or live truth. |
| Syncing | Sync in-progress metadata for push/pull/config fetch, active actor/session, preserved pending state. | Adapter composition | Stop if syncing implies approval, conflict-free completion, global completeness, or reporting truth. |
| Synced | Successful push/pull exchange, server-assigned watermarks, scoped projection refresh, local pull state. | Existing contract reuse | Route only if product needs a new API or contract meaning for approved, complete, reviewed, or globally current. |
| Synced with issue | Existing conflict/flag events, unresolved issue counts, projections, flagged event references. | Existing contract reuse | Route FD-PKT-106 if issue visibility needs direct flag mutation, review queues, resolver reassignment, batch handling, or auto-resolution. |
| Failed to sync | Sync error metadata, preserved pending events, retry/support copy, active actor partition. | Adapter composition | Route FD-PKT-103 for token refresh/logout/login semantics; route ops packets if support recovery exceeds constrained operator-managed wording. |
| Unauthorized/actor drift/no connection | Auth/actor-alignment check before push, `/api/auth/me` refresh where available, network failure state, pending preservation. | Existing contract reuse | Stop if mobile local override, JWT `actor_id`, provider groups/claims, cross-actor access, token lifecycle, or local authority is required. |
| Correction | Append-only follow-up capture/update over same subject/context where available, projections that preserve history. | Existing contract reuse | Route FD-PKT-106 if correction requires in-place edit, deletion, mutable history, conflict automation, resolver reassignment, or auto-resolution. |
| Latest synced/freshness | Last sync timestamp, latest local timestamp, scoped pull watermark, projection metadata, pending limits, unresolved issue context. | Adapter composition | Route FD-PKT-105 if freshness becomes reporting/API/export semantics, audit completeness, supervisor approval, or live field truth. |
| Needs review | Existing flags, unresolved issue counts, designated-resolver rules, projections, review events where authorized. | Existing contract reuse | Route FD-PKT-106 if needs review becomes production queue, hard rejection, direct flag mutation, broad review authority, resolver reassignment, or auto-resolution. |
| Access ended/stale access | Local assignment/config state, stale assignment warnings, server-side accept-and-flag behavior, later flags/projections where applicable. | Existing contract reuse | Route FD-PKT-101 if lifecycle truth is required; route FD-PKT-104 if deletion/security/retention is implied; stop on mobile-side hard rejection. |
| Shared-device if claimed | Exactly one active actor session, actor-local partitions, drain-or-seal switching, read-only immutable config blobs, pending-work warnings. | Adapter composition | Route FD-PKT-104 if recovery, decommissioning, token/session retention, sealed recovery, local encryption, redaction, or no-local-retention is needed. |
| Operator/support recovery | Setup/connect errors, invalid token copy, sync failure copy, assignment/config checks, constrained operator process, support scripts. | Adapter composition | Route FD-PKT-006/007/108 if support claims require runbooks, staging rehearsal, monitoring, incident response, backup/restore, or production readiness. |

## 6. Contract/Schema Pressure Matrix

| Surface | Current fit for Candidate 1 | Assessment | Routed contract work trigger |
|---|---|---|---|
| Envelope | Current 11-field envelope and closed `type` vocabulary cover capture, review, alert, task created/completed, and assignment changed. Candidate 1 state labels are not envelope facts. | No change | Any new envelope field, new `type`, new actor/subject identity category, or UI status encoded as envelope truth requires formal architecture routing. |
| Sync protocol | Push/pull, watermarks, actor-scoped pull, subject-history backfill, read APIs, authenticated actor binding, and idempotency support saved/waiting/syncing/synced/failure/freshness composition. | Later implementation inspection | New sync state semantics, altered watermarks, lowered/rewritten pull state, cross-actor pull, broad audit pull, or a new sync endpoint requires routing. |
| Flag catalog | Existing flag categories, unresolved-flag exclusion, severity/resolvability, and exact designated-resolver semantics support issue/needs-review visibility. | No change | New flag category, severity/resolvability meaning, direct mutation, resolver reassignment, review queue semantics, or auto-resolution requires routing. |
| Shape-format | Deployer form DSL supports capture forms and subject-bound fields. Candidate 1 UI states are not deployer shape fields. | No change | UI status, lifecycle state, access logic, scripts, custom traversal, device triggers, or deployer-authored state machines in shape DSL requires routing. |
| Config package | Current package carries shapes, activities, role-action maps, sensitivity, and packaged pattern definitions. Unknown top-level keys are tolerated, but Candidate 1 should not add package sections. | No change | New top-level package key, new role/action semantics, expanded `context.*`, or work-list/view-model contract shipped through config requires routing. |
| Platform payload shapes | Existing assignment, conflict, and identity payload shapes are runtime contracts. Subject merge/split payloads must not become Candidate 1 lifecycle promise. | No change | New platform payload shape or use of existing identity-lifecycle payloads as day-one S06 behavior requires BAR-105/S06 routing. |
| Pattern definitions | Platform-owned pattern definitions and config bindings remain available for accepted workflow behavior. Candidate 1 should not add pattern inventory. | No change | New platform pattern, deployer-authored pattern semantics, action expansion beyond accepted role-actions, or lifecycle transition modeling requires routing. |
| Shared fixtures | Projection, expression, and pattern fixtures remain test evidence sources. This packet does not change fixtures. | No change | Fixture changes are allowed only in later implementation packets with exact touched behavior and tests. |
| APIs/endpoints | Existing `/api/auth/me`, sync push/pull/subject-history, subject read APIs, config publication, assignment/admin surfaces where already accepted can back adapter views. | Later implementation inspection | New endpoint, new response field with shared semantic meaning, new status API, broad audit/report API, or altered authorization meaning requires routing. |
| Mobile local state | Existing local event store, pending push, sync metadata, config store, projections, flags, timestamps, and actor partitions can back UI states. | Later implementation inspection | New local authority, durable local workflow state, hard rejection, cross-actor visibility, secure-storage claim, encryption/retention behavior, or shared contract model requires routing. |
| Server projections | Existing subject, assignment, alias, integrity, and pattern projections can be read/composed as projections from events. | Later implementation inspection | Stored current truth table, durable workflow state, lifecycle status projection, candidate promotion projection, or reporting aggregate truth requires routing. |

## 7. Server/API Read-Model Assessment

Candidate 1 can be composed from existing server-side sources without new
durable workflow state or new API meaning if downstream implementation keeps
these limits:

- Setup and configuration surfaces read current config packages, shapes,
  activities, role-action maps, assignment setup, and publication state as
  existing configuration facts.
- Assigned work reads active assignment-derived access, fixed accepted scope
  axes, activity/context, and existing projections. It does not create a new
  work-item lifecycle.
- Saved/waiting/syncing/synced/failure states remain sync metadata or mobile
  local pending state. Server read models should not store a workflow status
  table for them.
- Subject-linked capture reads existing subject refs/history and alias
  projection where authorized. It does not claim known-set source, verification,
  lifecycle state, or registry stewardship.
- Needs review and synced-with-issue read existing flags, unresolved issue
  counts, flagged event references, resolver rules, and projections. They do
  not become a production queue or flag-mutation API.
- Latest synced/freshness uses sync/projection timestamps, watermarks, and
  scoped pull context as latest-known information, not live truth, audit
  completeness, report freshness, or approval.
- Actor and access context comes from authenticated actor resolution and active
  assignments. IdP groups, roles, provider claims, JWT `actor_id`, UI labels,
  or custom query filters do not become authority.

A routed read-model/contract packet is required before implementation if any
future server/API proposal needs:

- a stable shared JSON shape consumed by multiple shipped surfaces as contract
  truth;
- new API response fields that encode Candidate 1 statuses as platform facts;
- new durable workflow state, stored current truth, lifecycle state, or
  candidate promotion state;
- new scope, query-scope, auditor/report scope, or special access behavior;
- production reporting/export/import, broad audit/history, or aggregate
  freshness semantics;
- conflict review queues, direct flag mutation, resolver reassignment, or
  auto-resolution;
- S06 known-set source, maintained registry behavior, duplicate workflow,
  merge/split behavior, or lifecycle vocabulary as API truth.

## 8. Mobile Adapter Assessment

Candidate 1 mobile can remain local UI composition if downstream work maps
each visible state to existing local and server-derived sources:

- setup/connect from raw bearer entry, `/api/auth/me`, active actor session,
  and config/assignment visibility;
- current-user display from the active server-resolved actor and active local
  actor partition;
- work list from assignment/config visibility, local projections, pending
  count, flag count, latest timestamp, sync entry point, and empty/no-config
  state;
- saved locally and waiting to sync from pending local events in the active
  actor partition;
- syncing, synced, failed, unauthorized, actor drift, and no connection from
  sync metadata and preserved pending state;
- synced with issue and needs review from flags/advisories and projections;
- latest synced/freshness from timestamps, watermarks, and projection metadata;
- correction from append-only follow-up capture, not in-place edit;
- subject-linked and missing-known-thing flows from existing subject refs or
  unlinked/candidate evidence language;
- shared-device warnings from one active actor session, drain-or-seal behavior,
  and actor-local partitions if shared devices are claimed.

Mobile route-needed triggers:

- a mobile view needs a stable cross-process contract rather than local adapter
  mapping;
- local UI must authoritatively reject structurally valid work or enforce new
  access/scope rules;
- mobile copy requires productized OIDC/Keycloak login, refresh/logout, token
  expiry UX, secure storage, offline re-auth, local encryption, retention,
  decommissioning, or sealed recovery;
- shared-device behavior needs recovery/decommissioning guarantees, retention
  policy, or cross-actor visibility;
- missing-known-thing/candidate capture must create registry truth,
  lifecycle state, candidate promotion, duplicate workflow, or merge/split UX;
- latest synced, saved locally, failed sync, needs review, correction, or
  access changed implies live truth, server receipt, approval, rejection, data
  loss, deletion, retention/security, or history rewrite.

## 9. S06 Boundary In View Models

S06 must stay visible and unsolved by view-model naming, API fields, lifecycle
states, or mobile/server composition.

Rules for Candidate 1 view models:

- `known thing` is product copy under validation. It is not a contract field,
  identity category, source-of-truth model, or maintained registry promise.
- `linked` means a record refers to an existing subject/known thing where the
  current system already supports that reference. It does not mean verified,
  active, current, canonical, or lifecycle-approved.
- `unlinked`, `candidate`, and `missing known thing` mean preserved capture
  evidence needing matching/review. They do not mean registry creation,
  candidate promotion, discovered-unit lifecycle, or automatic matching.
- `duplicate suspected` may be review-oriented wording backed by current flags
  or evidence. It does not mean duplicate workflow, auto-merge, merge/split UX,
  resolver reassignment, or auto-resolution.
- Lifecycle words such as active, inactive, retired, closed, moved, verified,
  merged, split, current, and canonical must not appear as Candidate 1 platform
  truth unless FD-PKT-101 promotes and routes S06.
- The maintained known-set source remains an FD-PKT-101 question. It must not
  be inferred from a view-model field, subject picker, work-list grouping, or
  mobile/server composition.
- Existing subject merge/split payload shapes and alias behavior do not
  authorize a new first-deployment registry lifecycle product promise.

Any implementation packet that proposes fields such as `lifecycle_status`,
`known_thing_status`, `candidate_status`, `registry_state`,
`verification_state`, `duplicate_state`, `merge_state`, or `split_state` must
stop and route through FD-PKT-101/BAR-105 before implementation.

## 10. Authority And Access Boundary

Persona and UX labels do not become actor/scope authority.

Allowed authority model:

- Actor identity comes from explicit production principal binding and server
  resolution, or from a prior server-resolved mobile actor session under the
  accepted shared-device boundary.
- Mobile setup can validate constrained raw bearer plus `/api/auth/me`, but
  that is not production mobile OIDC/Keycloak login.
- Access and visibility derive from active assignments, role-action allowance,
  fixed accepted scope axes, time, activity/context, and server-side policy.
- Review/resolution authority follows exact designated-resolver semantics.
- Assignment create/end authority, when relevant, remains the platform-owned
  assignment-admin command capability with same-assignment containment.

Forbidden authority movement:

- no IdP group, IdP role, provider claim, JWT `actor_id`, UI-selected actor,
  persona label, screen name, module name, or product area becomes actor or
  scope authority;
- no custom/query scope, report filter, auditor filter, admin convenience
  filter, or mobile local filter becomes access authority;
- no mobile-side hard rejection of structurally valid work;
- no local stored truth table for current assignments, lifecycle, review state,
  or workflow state beyond existing projections and sync/local metadata;
- no support/operator label implies broad data access, sealed-partition
  recovery, production admin authority, or retention/security guarantee.

## 11. Recommendation By Surface

| Surface | Recommendation | Gate |
|---|---|---|
| Product copy | Continue validation using Candidate 1 vocabulary as user-facing copy only. Keep lifecycle and production-readiness words out unless routed. | Stop if users cannot understand candidate/unlinked evidence without S06 behavior. |
| Mobile UI adapter | Use adapter-level composition over existing local event store, pending state, sync metadata, flags, timestamps, config, assignments, and actor partitions. | Route if mobile needs a stable shared contract, local authority, production login, retention/security, or S06 lifecycle behavior. |
| Server/API composition | Compose from existing events, projections, flags, assignments, config, sync metadata, and subject refs where already supported. | Route if a new endpoint, shared wire shape, response field meaning, durable state, or authority change is needed. |
| Shared view model | Do not create a shared view-model contract now. Prefer local adapters plus evidence checklists. | Route only if later implementation inspection proves multiple shipped components need one stable cross-process shape. |
| Contracts | No envelope, sync, flag, shape-format, config-package, platform payload, pattern, or fixture changes are recommended by this packet. | Route before implementation on any contract/schema/fixture pressure. |
| S06 | Keep FD-PKT-101 as a required decision gate before Candidate 1 implementation dispatch. | Stop if known-set source, lifecycle words, candidate promotion, duplicate workflow, or merge/split behavior becomes necessary. |
| Implementation dispatch | Remains `no-go`. | Later implementation packets must be bounded by one surface, exact files/contracts, targeted tests, manual evidence, forbidden work, stop conditions, and commit boundary. |

## 12. Expected Evidence Before Implementation

Before any Candidate 1 implementation packet is dispatched:

- FD-PKT-002, FD-PKT-003, FD-PKT-004, FD-PKT-005, and FD-PKT-101 must be
  reviewed/gated according to the active router and Stage 8 packet gate.
- Product/SME validation must prove Candidate 1 vocabulary is understandable
  without turning candidate/unlinked/missing-known-thing capture into registry
  lifecycle truth.
- A state-to-source checklist must map every Candidate 1 visible state to
  existing backing sources and mark adapter composition versus existing
  contract reuse.
- Mobile validation must cover setup/connect, assigned work, standalone
  capture, subject-linked capture, missing-known-thing/candidate capture,
  saved locally, waiting/syncing/synced/synced-with-issue/failed, unauthorized,
  actor drift, no connection, correction, freshness, needs review, access
  ended, shared-device if claimed, and operator/support recovery.
- Contract pressure review must confirm no envelope, sync, flag, shape,
  config package, platform payload, pattern, fixture, API, scope, authority, or
  durable-state change is required.
- FD-PKT-101 must choose, promote, split, or explicitly exclude/defer S06 for
  Candidate 1 implementation honesty.
- Future implementation packets must name exact files/contracts, accepted
  constructs reused, targeted tests, manual walkthrough evidence, excluded
  successor lanes, forbidden work, stop conditions, and commit boundary.

## 13. Forbidden Work

- Do not edit code, contracts, schemas, APIs, fixtures, router/status/backlog
  files, README files, BAR/NW, CDL, workshop control files, server files,
  mobile files, or runtime behavior.
- Do not run server tests, mobile tests, Flutter tests, Maven tests, scenario
  probes, or implementation validation commands for this docs packet.
- Do not commit.
- Do not implement or authorize Candidate 1, S06, mobile UI, server read
  models, APIs, contracts, shared view models, local storage changes, sync
  changes, auth changes, tests, or release work.
- Do not create a new shared view-model contract, new API meaning, new
  contract/schema field, new event type, envelope field, fixture change, scope
  mechanism, local authority, durable workflow state, stored current truth
  table, or mobile-side hard rejection.
- Do not add or imply canonical entity lifecycle, maintained known-set source,
  active/inactive/retired truth, discovered-unit lifecycle, registry
  stewardship, duplicate workflow, candidate promotion, lifecycle behavior,
  merge/split UX, automatic matching, or verification policy.
- Do not add or imply production auth/admin/mobile OIDC login, browser/provider
  login, token refresh/logout, token expiry, secure storage, online
  binding-admin UI/API, IdP group/claim/JWT `actor_id` authority, local
  encryption, retention/security, expiry, device decommissioning,
  sealed-partition recovery, reporting/export/import, broad audit/history,
  aggregate access divergence, custom/query scope, conflict automation,
  resolver reassignment, auto-resolution, or production readiness.
- Do not make latest synced live truth, needs review a production review queue,
  saved locally server receipt, failed sync data loss, correction history
  rewrite, or access ended deletion/security erasure.
- Do not make persona labels identity categories, authority primitives, fixed
  product modules, config namespaces, product-area boundaries, access rules,
  or implementation service boundaries.

## 14. Stop And Report Conditions

Stop and report if:

- active status, routed packet inputs, Stage 4, Stage 5, contracts, or workshop
  docs conflict in a way that changes this packet boundary;
- drafting this packet requires editing any file other than this one;
- Candidate 1 requires a new contract, schema, API, fixture, event vocabulary,
  scope mechanism, authority rule, shared view-model contract, durable
  workflow state, stored current truth, or runtime behavior before routing;
- Candidate 1 cannot honestly remain S01-compatible without maintained known
  things, lifecycle state, discovered-unit stewardship, registry stewardship,
  duplicate stewardship, merge/split UX, lifecycle words, or candidate
  promotion;
- S06 is hidden as vague later work without owner, route, evidence need, and
  decision point;
- a user-facing term becomes an event field, event type, scope mechanism, flag
  category, schema/contract field, durable workflow state, authority rule,
  lifecycle truth, or shared API meaning;
- persona labels harden into identity categories, access rules, fixed modules,
  config namespaces, product-area boundaries, or implementation service
  boundaries;
- IdP groups, provider claims, JWT `actor_id`, UI selection, mobile local
  state, custom/query scope, report filters, or support labels become
  authority;
- production auth/admin/mobile login, retention/security, reporting/export,
  import, conflict automation, resolver reassignment, auto-resolution,
  custom/query scope, ops readiness, or production readiness enters Candidate
  1;
- unrelated worktree changes appear; leave them alone and report them.

## 15. Done Definition

FD-PKT-005 is done when:

1. This file exists at
   `docs/workshops/first-deployment/task-packets/fd-pkt-005-candidate-1-view-model-contract-assessment.md`.
2. No other file is edited.
3. Required sections 1 through 16 are present.
4. The packet recommends adapter/view composition by default and names
   route-needed triggers for shared view-model, contract, schema, API, S06,
   authority, mobile, and server read-model pressure.
5. Candidate 1 state composition and contract/schema pressure matrices are
   explicit.
6. Server/API and mobile adapter assessments preserve existing authority,
   projection, sync, flag, assignment, config, and actor-partition boundaries.
7. S06 remains visible with owner, route, evidence need, and decision point.
8. Persona/operational labels remain acting contexts only.
9. The file contains no retired first-deployment review path reference.
10. `git diff --check` passes.
11. `git status --short` shows only this new packet file.
12. No server/mobile tests are run.

## 16. Downstream Packet Impacts

| Downstream packet or lane | Impact |
|---|---|
| FD-PKT-002 | Use this assessment to keep product copy as user-facing validation language, not platform vocabulary or contract truth. |
| FD-PKT-003 | Treat the matrices as evidence gates for contract fit, state-to-source mapping, claim wording, and route-needed triggers. |
| FD-PKT-004 | Keep mobile labels as adapter composition over existing local state, sync metadata, flags, assignments, config, timestamps, and actor partitions. |
| FD-PKT-101 | Use S06 route triggers from this packet as implementation stop gates for known-set source, candidate promotion, lifecycle vocabulary, duplicate workflow, and merge/split behavior. |
| FD-PKT-006/007/108 | Carry operator/support recovery, setup/connect process, sync failure support, staging rehearsal, runbook, monitoring, incident response, and constrained-deployment evidence. Do not make FD-PKT-005 a production-readiness packet. |
| FD-PKT-102/103 | Route production web admin auth, mobile OIDC/Keycloak login, token lifecycle, secure storage, online binding-admin UI/API, and IdP claim/group authority here if needed. |
| FD-PKT-104 | Route retention/security/device lifecycle, local encryption, expiry, decommissioning, sealed recovery, redaction/no-local-retention, and token/session retention policy here if needed. |
| FD-PKT-105/106/107 | Route reporting freshness, reports/export/import, broad audit/history, conflict review queues, resolver reassignment, auto-resolution, subject/query/custom scope, special access, and auditor/report filters if Candidate 1 needs them. |
| Later Candidate 1 implementation packets | Must receive one bounded implementation surface only. They must not use this packet as implementation authorization, and they must include exact files/contracts, accepted constructs reused, targeted tests, manual evidence, excluded successor lanes, forbidden work, stop conditions, and commit boundary. |
