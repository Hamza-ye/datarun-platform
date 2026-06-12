# FD-PKT-004 Candidate 1 Mobile/Offline Validation

Status: prepared task-packet draft

Date: 2026-06-13

Authority: none. This packet defines mobile/offline UX and evidence targets
for Candidate 1. It does not authorize implementation, change CDL, BAR, NW,
contracts, schemas, APIs, runtime behavior, mobile code, event vocabulary, or
product scope by itself.

## 1. Header

Packet ID: FD-PKT-004

Lane: Candidate 1 mobile/offline validation

Assigned role: Mobile App Builder

Claim status: `product-surface-partial`

Objective: define mobile UX/spec evidence for Candidate 1 setup/connect,
work-list state, standalone capture, subject-linked capture,
missing-known-thing or candidate capture, offline save, sync failure,
pending preservation, shared-device switch if claimed, append-only correction,
freshness interpretation, needs-review visibility, and constrained
operator/support recovery without implementing mobile UI or authorizing
Candidate 1 implementation.

Authority and source order:

1. CDL/contracts and active status remain binding authority. Contract and
   status guardrails take precedence over workshop wording.
2. FD-PKT-001, FD-PKT-002, FD-PKT-003, and FD-PKT-101 are packet inputs.
3. Stage 5 mobile output is mobile feasibility/evidence context, not
   implementation authority.
4. Scenario/user-fit and workshop docs are product evidence context, not
   architecture authority.
5. BAR/NW standing may be referenced only as accepted, deferred, or future
   standing, not expanded into new implementation scope.

Operational/persona labels used:

- Setup owner, field user, supervisor/reviewer, operator/support, and shared
  device user are acting contexts only.
- Every evidence artifact that uses an acting-context label must map it as:

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
  `docs/workshops/first-deployment/stage-5-mobile.md`,
  `docs/workshops/first-deployment/stage-8-task-packet-backlog.md`,
  FD-PKT-001, FD-PKT-002, FD-PKT-003, and FD-PKT-101.
- Optional read-only source context: targeted `mobile/` file names or tests
  named by Stage 5 only if needed to avoid guessing a mobile state or test
  name.
- Allowed contract stance: reference existing envelope, sync protocol, flag
  catalog, shape-format, config-package, platform payload shape, pattern
  definition, and shared fixture boundaries only as guardrails. No contract
  edits, new schema rows, new event vocabulary, or implementation authority.

Accepted constructs reused:

- Raw bearer setup plus `/api/auth/me` actor resolution for constrained setup.
- Active actor session and prior server-resolved actor-session resume.
- Actor-local partitions for events, pending push, projections, normal pull
  watermarks, subject-history cursors, token/session material, and active or
  pending config state.
- Pending local events, push/pull/sync metadata, timestamps, config package
  state, assignment-derived access/scope, flags/advisories, and append-only
  capture/correction behavior.
- Existing subject reference/history support where available, without
  lifecycle truth or registry stewardship.
- Mobile advisory warnings that can inform the user but cannot authoritatively
  reject structurally valid work.

Excluded successor lanes:

- FD-PKT-005 view-model/contract assessment, including any stable shared
  view-model, new API meaning, or contract/schema route.
- FD-PKT-101 S06/entity lifecycle, maintained known things, discovered-unit
  lifecycle, registry stewardship, lifecycle words, candidate promotion,
  duplicate stewardship, and merge/split UX.
- FD-PKT-102 and FD-PKT-103 production web admin auth, online binding-admin
  UI/API, mobile OIDC/Keycloak login, token refresh/logout, token expiry,
  secure storage, and IdP group/claim authority.
- FD-PKT-104 retention/security/device lifecycle, expiry, decommissioning,
  sealed recovery, local encryption, redaction/no-local-retention, and
  token/session retention policy.
- FD-PKT-105 reporting dashboards/APIs/export/import, broad audit/history,
  aggregate access, and reporting freshness semantics.
- FD-PKT-106 conflict review queues, batch handling, pending-match
  derivations, conflict automation, resolver reassignment, and
  auto-resolution.
- FD-PKT-107 subject/query/custom scope, auditor/report filters as authority,
  and special read/write bypasses.
- FD-PKT-108 ops readiness and constrained-deployment release claims.

Forbidden work:

- No code edits, contract edits, schema edits, API edits, fixture edits,
  router/status/backlog/README edits, BAR/NW/CDL edits, workshop control edits,
  runtime behavior edits, commits, test execution, implementation tasks, or
  implementation authorization.

Expected evidence:

- Mobile backing-state matrix, mobile evidence matrix, Flutter evidence target
  list, manual mobile walkthrough matrix, shared-device boundary,
  mobile-auth/login boundary, S06 mobile boundary, open product/UX questions,
  expected evidence artifacts, forbidden work, stop conditions, done
  definition, and downstream packet impacts.

Manual walkthroughs:

- Invalid token, valid setup, offline capture, app restart, sync retry, failed
  sync with pending preservation, subject-linked capture,
  missing-known-thing/candidate capture, correction as appended record,
  stale/access-changed save and later server issue visibility, shared-device
  A-to-B switch if claimed, supervisor/latest-synced/freshness
  interpretation, and operator/support recovery.

Commit boundary: docs-only packet creation. Do not edit any other file. Do not
commit.

## 2. Role Boundary

Mobile App Builder may:

- translate FD-PKT-002/003/101 and Stage 5 into mobile/offline validation
  targets;
- name mobile labels, states, backing sources, evidence questions, and stop
  conditions;
- identify existing Flutter test names already cited by Stage 5;
- describe candidate widget/integration target boundaries without commands;
- define manual mobile walkthrough scripts and expected evidence artifacts;
- keep mobile feasibility visible as `product-surface-partial`.

Mobile App Builder must not:

- implement mobile UI, data, sync, auth, storage, test, or view-model code;
- decide product scope, architecture primitives, contract shape, authority,
  S06 promotion, release readiness, or implementation dispatch;
- reject structurally valid events authoritatively on device;
- create actor, scope, lifecycle, registry, review, correction, retention, or
  security authority locally;
- assume mobile OIDC/Keycloak login, token refresh/logout, secure storage,
  local encryption, decommissioning, sealed recovery, or retention policy
  exists;
- turn acting-context labels into identity categories, access rules, modules,
  config namespaces, product-area boundaries, or service boundaries.

Evidence gaps become mobile evidence work or gates. They are not reasons to
erase Candidate 1 product need, and they are not permission to add mobile
implementation scope.

## 3. Mobile Scope Summary

Candidate 1 mobile validation is a product-surface evidence slice over the
accepted kernel. It can validate whether users understand constrained setup,
assigned work, capture, saved-local status, sync status, failed-sync recovery,
correction, latest-synced freshness, needs-review signals, and shared-device
isolation if claimed.

This packet keeps Candidate 1 S01-compatible. Standalone capture and
missing-known-thing capture are unlinked or candidate evidence for review, not
canonical known-thing creation, lifecycle state, or registry truth.

Mobile remains advisory for local warnings. Server-side authority remains
authoritative for actor binding, assignment/access scope, event acceptance,
flags, projections, and sync outcomes.

## 4. Source Input Summary

| Source | Mobile/offline input for FD-PKT-004 |
|---|---|
| FD-PKT-002 | Candidate 1 includes setup comprehension, assigned work, standalone capture, optional subject-linked capture, missing-known-thing/candidate capture, local save, sync states, failed-sync recovery, append-only correction, latest synced, needs review, access-ended/stale-access explanation, shared-device language if claimed, and operator/support recovery. It requires acting-context authority mapping and forbids lifecycle, production auth, retention/security, reporting, conflict automation, custom scope, and implementation authorization. |
| FD-PKT-003 | Mobile evidence must cover raw bearer plus `/api/auth/me`, work-list counts and timestamps, offline save with one pending event and active actor/session data, waiting/syncing/synced/synced-with-issue/failed/unauthorized/actor-drift/no-connection states, restart and retry pending preservation, advisory non-blocking warnings, append-only correction, candidate-only missing-known-thing handling, shared-device isolation if claimed, and freshness comprehension. |
| FD-PKT-101 | S06 remains `needs-decision`. Mobile copy must keep known thing, candidate, unlinked, duplicate suspected, inactive, closed, moved, retired, verified, merged, and split language from becoming lifecycle truth. Missing-known-thing handling must remain unpromoted evidence unless S06 is promoted through BAR-105 routing. |
| Stage 5 mobile | Current Flutter feasibility exists for constrained bearer setup, `/api/auth/me` actor activation, subject-centric work list, shape-driven forms, offline save, sync/pull/push/config metadata, advisory warnings, append-only correction path, local flags, timestamps, actor partitions, and shared-device drain-or-seal behavior. Stage 5 is feasibility/evidence context only and does not authorize implementation. |

## 5. Backing-State Matrix

| Mobile label or state | Existing backing source to validate | Evidence question | Must not imply |
|---|---|---|---|
| Setup/connect | Raw bearer setup plus `/api/auth/me` response. | Does the user understand this as constrained setup that activates a server-resolved actor session? | Productized mobile OIDC/Keycloak login, token refresh/logout, IdP claim authority, or production auth readiness. |
| Current user / active session | Active actor session resolved by server. | Is the visible user/session tied to server actor binding and the active local actor partition? | UI-selected actor authority, JWT `actor_id` authority, groups/roles as direct platform authority, or multi-actor mixed state. |
| Assigned work / work list | Assignment/config state, active assignment role/action/scope/time, and activity/context. | Can users explain why work appears without persona labels becoming authority? | UI-only permission, custom/query scope, IdP group authority, or fixed product module boundaries. |
| Empty or no-config state | Config package and assignment visibility state. | Does the user know whether setup/config/assignment is missing without seeing architecture vocabulary? | Production admin UX, config internals, or automatic support access. |
| Standalone capture | Shape-driven form save producing an append-only pending event. | Does standalone capture feel usable without implying a subject registry action? | New envelope fields, event types, mutable rows, in-place edits, or S06 lifecycle. |
| Subject-linked capture | Existing subject reference/history support where available. | Does the user understand this as a record linked to an existing known thing? | Active/inactive/retired truth, verification truth, registry stewardship, or merge/split behavior. |
| Missing-known-thing / candidate capture | Unlinked pending event or review-oriented candidate evidence. | Does the user understand work can continue while matching/review remains unresolved? | Canonical registry creation, candidate promotion, automatic matching, discovered-unit lifecycle, or lifecycle state. |
| Saved locally | Pending events in the local event store for the active actor/session. | Does the user understand the work is preserved on this device and not yet server-visible? | Server receipt, approval, review, retention guarantee, recovery after device loss, or conflict-free status. |
| Waiting to sync / pending count | Pending push count and sync queue metadata. | Does the pending count explain queued work without changing authority? | Server acceptance, live truth, or durable workflow state. |
| Syncing | Push/pull/config fetch metadata and in-progress sync state. | Can users distinguish in-progress transfer from accepted or reviewed work? | Approval, conflict-free completion, or reporting completeness. |
| Synced | Successful push/pull metadata and scoped projection refresh. | Does synced mean server exchange completed for this user/scope? | Supervisor approval, global completeness, complete report truth, or live field reality. |
| Synced with issue / needs review | Flags/advisories, flagged events, projections, and unresolved issue counts. | Does the user see issue visibility as review-needed, not hard rejection? | Auto-resolution, resolver reassignment, direct flag mutation, batch queue, or broad review authority. |
| Failed to sync | Sync error metadata with preserved pending events. | Does the user know pending work remains local and can be retried or escalated? | Data loss, server rejection, token refresh/logout, mobile login, sealed recovery, or guaranteed support recovery. |
| Unauthorized / actor drift | Actor refresh or auth/actor-alignment check before push. | Does the user understand push stopped and pending work remains preserved? | Local override, mobile-side authority, token lifecycle, or cross-actor data access. |
| No connection | Network/sync failure state with pending data unchanged. | Does offline/no-connection copy preserve confidence without overclaiming? | Server receipt, live visibility, or device-loss recovery. |
| Latest synced / freshness | Last sync timestamp, latest local timestamp, projection metadata, and scoped pull metadata. | Can supervisors distinguish latest known here from live field truth? | Live field reality, full reporting truth, audit completeness, or approval. |
| Correction / update | Append-only follow-up capture over same subject/context where available. | Does correction read as an added record/update with history preserved? | In-place edit, erasure, cancellation, resolver reassignment, or auto-resolution. |
| Stale/access changed | Local assignment/config state plus later server flags/projections where applicable. | Does the user understand local warnings are advisory and valid saved work may later be issue-visible? | Mobile hard rejection, deletion, security erasure, decommissioning, or lifecycle truth. |
| Shared-device switch | One active actor session, actor partitions, drain-or-seal switch behavior, and read-only shared immutable config blobs. | Can users understand A-to-B isolation and pending-work warning if shared devices are claimed? | Cross-actor visibility, retention/security guarantee, sealed recovery, local encryption, or device decommissioning. |

## 6. Candidate 1 Mobile Evidence Matrix

| Mobile surface | Evidence to produce | Pass criterion | Gate or route |
|---|---|---|---|
| Setup/connect | Validation script for invalid token, valid raw bearer setup, `/api/auth/me` actor activation, and setup failure copy. | User understands constrained setup and active actor session without production-login expectations. | Route FD-PKT-103 if provider login, refresh/logout, token expiry, secure storage, or offline re-auth is needed. |
| Work list | Evidence for assigned work, pending count, flag count, latest timestamp, sync entry point, empty state, and no-config state. | User understands visible work as assignment/config backed and can distinguish pending, issue, and freshness indicators. | Route FD-PKT-005 if a stable shared view-model/API shape is required; route FD-PKT-107 if custom/query scope is required. |
| Standalone capture | Evidence that a basic form entry can be saved as local pending work without subject link. | User understands a standalone record without inferring registry lifecycle or platform primitive changes. | Stop if new event vocabulary, hard local rejection, mutable row expectations, or lifecycle language is required. |
| Subject-linked capture | Evidence that a record can be linked to an existing known thing where available. | User understands linked record as a reference to an existing thing, not verified lifecycle truth. | Route FD-PKT-101 if known-set source, verification, active/inactive state, subject-history expectation, duplicate stewardship, or merge/split is required. |
| Missing-known-thing / candidate capture | Evidence that work can continue as unlinked/candidate evidence for review. | User does not treat candidate capture as registry creation, automatic matching, or canonical lifecycle state. | This is an S06 gate; stop if candidate-only handling is not understandable or acceptable. |
| Offline save | Evidence that saving offline creates preserved pending work with active actor/session data and saved-on-this-device copy. | User understands work is local and not yet server-visible. | Route FD-PKT-104 if device-loss recovery, retention, local encryption, or decommissioning guarantees are needed. |
| Sync states | Evidence for waiting to sync, syncing, synced, synced with issue, and latest synced states. | User distinguishes transfer state, issue state, and freshness state without approval/live-truth overclaim. | Route FD-PKT-005 if labels cannot be composed from existing events, flags, sync metadata, assignment, and config. |
| Failed sync | Evidence that failed sync preserves pending work and supports retry/support language. | User does not infer data loss, server rejection, token refresh, or guaranteed recovery. | Route FD-PKT-006/007/108 for ops/support readiness if support path exceeds constrained wording. |
| Unauthorized / actor drift / no connection | Evidence that push stops before unsafe send, pending work remains preserved, and recovery copy is honest. | User understands pending preservation and does not expect local override or token lifecycle behavior. | Route FD-PKT-103 for mobile auth/login; stop if mobile needs to become actor/scope authority. |
| Correction | Evidence that correction is appended as another record/update and original history remains traceable. | User distinguishes correction from in-place edit, deletion, replacement, cancellation, approval, or conflict resolution. | Route FD-PKT-106 if correction needs conflict automation, resolver reassignment, auto-resolution, or batch review. |
| Supervisor/latest-synced/freshness interpretation | Evidence that supervisors read latest synced, latest timestamp, unresolved issue signals, and pending limits correctly. | Supervisor/reviewer distinguishes latest known here from live field truth, approval, and reporting completeness. | Route FD-PKT-105 if dashboard/API/report freshness semantics are needed. |
| Needs-review visibility | Evidence that issue/attention/duplicate-suspected indicators are visible and limited. | User understands view-only versus allowed action and that issues remain unresolved until authorized server-side handling. | Route FD-PKT-106 if review queues, direct flag mutation, resolver reassignment, or auto-resolution are required. |
| Access-ended/stale access | Evidence that access-changed warnings remain advisory and later server issue visibility is understandable. | User understands valid local saved work may be preserved and later marked for review, not deleted or rejected locally. | Route FD-PKT-101 if lifecycle truth is required; route FD-PKT-104 for retention/security/device lifecycle claims. |
| Shared-device if claimed | Evidence for A-to-B switch, pending-work warning, drain-or-seal behavior, partition isolation, and safe resume. | Actor B cannot see A data; users understand active actor, pending state, isolation, and no retention/security promise. | Required only if shared devices are claimed; route FD-PKT-104 for recovery/decommissioning/retention/security. |
| Operator/support recovery | Evidence for invalid token, setup/connect problem, sync failure, access ended, and pending work explanation. | Support path is realistic for constrained operator-managed use and does not imply production readiness. | Route FD-PKT-006/007/108 for ops runbooks, staging rehearsal, monitoring, incident response, and constrained deployment evidence. |

## 7. Flutter Evidence Targets

This docs packet does not run tests and does not prescribe exact commands.
Future implementation packets must choose commands for their touched surface.

Existing useful Flutter targets named by Stage 5:

- `sync_service_test`
- `event_assembler_test`
- `activity_role_actions_test`
- `selective_retain_test`
- `projection_equivalence_test`
- `projection_engine_test`
- `form_engine_test`
- `config_store_test`
- `context_resolver_test`
- `expression_evaluator_test`

Candidate widget/integration target descriptions by boundary:

| Boundary | Candidate target description | Gate |
|---|---|---|
| Setup/connect | Setup success and setup failure against `/api/auth/me`, including invalid token and active actor display. | No production mobile OIDC/Keycloak login claim. |
| Offline save | Form offline save creates one pending event with active actor/session data and saved-on-this-device state. | No server-received, retention, or device-recovery implication. |
| Advisory warning | Advisory warning displays for local risk but does not block save. | No mobile-side hard rejection or local authority. |
| Sync panel | Success, push failure, unauthorized, actor drift, and no-connection states preserve pending data. | No token refresh/logout or local override claim. |
| Pending preservation | Restart, failed sync, auth failure, and retry preserve pending work for the active actor/session. | No cross-actor visibility or sealed-recovery promise. |
| Work list | Pending count, flag count, latest timestamp, sync entry point, empty state, and no-config state render from existing backing state. | Stop if a new shared contract/API is required before FD-PKT-005. |
| Subject-linked capture | Subject detail capture appends another event and preserves history. | No lifecycle truth, verification truth, or in-place edit. |
| Missing-known-thing/candidate capture | Standalone or missing capture remains unlinked/candidate and review-oriented. | Stop if candidate capture creates registry truth or promotion behavior. |
| Shared-device if claimed | Actor A drains or seals, actor B is isolated, and actor A can resume safely where allowed. | No retention/security, decommissioning, or local encryption claim. |
| Claim wording | Static/widget copy review for banned lifecycle, production-readiness, retention/security, reporting, and authority wording. | Stop if banned wording is needed to make the mobile flow honest. |

## 8. Manual Mobile Walkthrough Matrix

Each walkthrough must record acting context, authority mapping, device/network
setup, script, exact vocabulary tested, observed comprehension, defects or
risks, S06/successor-lane triggers, and pass/revise/route outcome.

| Walkthrough | Script focus | Evidence to capture | Pass criterion | Gate or route |
|---|---|---|---|---|
| Invalid token | Enter invalid raw bearer credential during setup. | Error copy, pending-state preservation if any, support instruction. | User understands setup failed and does not expect refresh/logout or provider login. | Route FD-PKT-103 if product needs mobile login or token lifecycle. |
| Valid setup | Enter valid constrained credential and activate actor via `/api/auth/me`. | Active actor/session display and setup success language. | User understands which actor is active and why work appears later. | Stop if UI selection, JWT `actor_id`, groups, or roles become authority. |
| Offline capture | Open assigned work, complete a form offline, save locally. | Saved-on-this-device state, pending count, active actor/session association. | User understands server has not received the work yet. | Route FD-PKT-104 if offline confidence requires retention/security or device-loss guarantees. |
| App restart | Restart app after offline save before sync. | Pending work still visible for the same actor/session. | User sees work preserved locally without cross-actor exposure. | Stop on data loss or cross-partition visibility. |
| Sync retry | Return online and retry sync. | Waiting/syncing/synced or synced-with-issue transitions. | User distinguishes transfer, server exchange, and issue visibility. | Route FD-PKT-005 if state needs a new shared view-model/API shape. |
| Failed sync with pending preservation | Trigger push failure, unauthorized, actor drift, or no connection. | Failed state, preserved pending work, retry/support language. | User does not infer data loss, server rejection, approval, or token refresh behavior. | Route FD-PKT-103 for auth lifecycle; route ops packets if support path exceeds constrained wording. |
| Subject-linked capture | Select existing known thing where available, capture against it, sync later. | Confirmation details, linked-record copy, latest-synced context. | User understands link to existing thing without lifecycle truth. | Route FD-PKT-101 if known-set source, verification, active/inactive state, duplicate stewardship, or merge/split is required. |
| Missing-known-thing/candidate capture | Cannot find thing, continue as unlinked/candidate record. | Candidate/unlinked wording, review-needed copy, later issue visibility. | User does not infer registry creation, verification, automatic matching, or lifecycle state. | S06 gate; stop if candidate-only handling fails validation. |
| Correction as appended record | Add correction/update after a prior capture. | Correction language, history-preserving explanation, latest-synced result. | User understands correction adds another record/update and does not erase history. | Route FD-PKT-106 if mutable history, conflict automation, resolver reassignment, or auto-resolution is needed. |
| Stale/access-changed save and later server issue visibility | Save while local access/config is stale, then sync and inspect issue visibility where server flags apply. | Advisory warning, saved-local behavior, later needs-review or issue signal. | User understands local warning did not authoritatively reject and server remains authoritative. | Route FD-PKT-101 for lifecycle/state needs or FD-PKT-104 for retention/security claims. |
| Shared-device A-to-B switch if claimed | Actor A has pending work, switch to actor B, inspect isolation, then resume A where allowed. | Pending warning, drain-or-seal behavior, B isolation, A safe resume or sealed state. | Users understand exactly one active actor session and no cross-actor data access. | Required only if shared devices are claimed; route FD-PKT-104 for recovery/decommissioning/retention/security. |
| Supervisor/latest-synced/freshness interpretation | Supervisor/reviewer reads latest synced, latest timestamp, pending limits, and unresolved issue signals. | Comprehension notes for freshness, issue status, and view-only/resolution limits. | User distinguishes latest known here from live field reality, approval, and reporting truth. | Route FD-PKT-105 for reporting freshness or FD-PKT-106 for review queues/automation. |
| Operator/support recovery | Operator/support explains invalid token, setup/connect problem, sync failure, access ended, and pending work. | Support script, risk list, escalation route, constrained-deployment wording. | Support path is realistic and avoids production auth, broad data access, retention, sealed recovery, or turnkey readiness promises. | Route FD-PKT-006/007/108 for ops readiness and staging evidence. |

## 9. Shared-Device Boundary

Shared-device evidence is required only if Candidate 1 claims shared-device
use. If claimed, validation must preserve the accepted boundary:

- exactly one active actor session;
- actor identity from `/api/auth/me` or prior server-resolved actor-session
  resume;
- actor-local partitions for events, pending push, projections, normal pull
  watermarks, subject-history cursors, token/session material, and active or
  pending config state;
- drain-or-seal switching behavior;
- no actor B access to actor A local data;
- immutable config blobs may be shared only as read-only non-actor data;
- pending-work warnings explain preservation and isolation without promising
  retention, expiry, decommissioning, sealed recovery, local encryption, or
  support recovery.

Stop if shared-device copy or evidence requires device decommissioning,
sealed-partition recovery, token/session retention policy, local encryption,
no-local-retention, redaction, or broader retention/security behavior.

## 10. Mobile Auth/Login Boundary

FD-PKT-004 may validate constrained setup only: raw bearer credential entry
plus `/api/auth/me` actor resolution, or prior server-resolved actor-session
resume where accepted by existing mobile state.

This packet must not claim or design:

- productized mobile OIDC/Keycloak login;
- browser/provider login;
- token refresh/logout;
- token expiry UX;
- secure storage;
- offline re-auth behavior;
- IdP groups, roles, provider claims, or JWT `actor_id` as actor/scope
  authority;
- online binding-admin UI/API;
- production web admin authentication.

Route FD-PKT-103 if mobile validation cannot be honest without any of those
capabilities.

## 11. S06 Boundary In Mobile

Mobile copy and validation must keep S06 visible and unsolved.

Allowed Candidate 1 mobile language:

- linked record;
- unlinked record;
- known thing as a product term under validation;
- candidate for review;
- missing known thing;
- needs review;
- duplicate suspected as review-oriented language only;
- latest synced;
- correction/update as append-only.

Forbidden as mobile truth unless FD-PKT-101 promotes and routes S06:

- canonical known-thing registry creation;
- active/inactive/retired/closed/moved/verified lifecycle state;
- discovered-unit lifecycle;
- candidate promotion;
- registry stewardship workflow;
- duplicate handling workflow;
- merge/split UX;
- automatic matching;
- mobile-side lifecycle rejection.

The mobile S06 gate passes only if users understand missing-known-thing and
candidate capture as preserved review evidence, not canonical registry truth.
If users cannot understand that boundary, Candidate 1 implementation remains
blocked until FD-PKT-101 promotes, splits, or explicitly excludes S06 with
risk signoff.

## 12. Product/UX Questions Feeding FD-PKT-002/FD-PKT-101/FD-PKT-005

Questions feeding FD-PKT-002 product/spec and UX validation:

- Which exact mobile labels should be validated for saved locally, waiting to
  sync, syncing, synced, synced with issue, failed to sync, access changed,
  needs review, latest synced, linked record, unlinked record, and correction?
- What minimum active-user/session banner is needed for constrained bearer
  setup and shared-device confidence?
- What work-list fields are necessary for first use: assignment label, pending
  count, issue count, latest timestamp, sync entry point, empty/no-config
  state, or other domain-specific cues?
- What support wording gives confidence after failed sync without implying
  server receipt, token refresh, device recovery, or production readiness?

Questions feeding FD-PKT-101 S06 discovery:

- Do field users accept missing-known-thing capture as candidate evidence, or
  do they need maintained known things before first implementation?
- Which words around candidate, duplicate, inactive, closed, moved, retired,
  verified, merge, and split are unavoidable in mobile flows?
- What subject lookup/confirmation details are needed to prevent wrong-link
  capture without implying registry verification or lifecycle state?
- What stale/offline known-set cases cause users to expect lifecycle behavior
  rather than advisory review visibility?

Questions feeding FD-PKT-005 view-model/contract assessment:

- Can mobile compose saved locally, waiting to sync, synced, failed, issue,
  correction, latest synced, and access-ended states from existing local
  events, projections, flags, sync metadata, assignment scope, and config?
- Which Candidate 1 state labels are purely local UI composition, and which
  would require a stable shared view-model shape across server/mobile?
- Can missing-known-thing/candidate capture be represented without a new S06
  data model, lifecycle state shape, candidate promotion workflow, or registry
  contract?
- Can needs-review and latest-synced/freshness remain existing
  flags/projections/sync metadata without a production review queue or
  reporting freshness contract?

## 13. Expected Evidence Artifacts

FD-PKT-004 evidence work should produce:

- mobile backing-state checklist mapping each label to existing state and
  authority source;
- mobile copy/glossary review for saved locally, waiting to sync, synced,
  synced with issue, failed to sync, latest synced, needs review, correction,
  linked, unlinked, candidate, and access changed;
- Flutter target inventory using Stage 5 test names and candidate
  widget/integration boundaries, with no exact commands in this packet;
- manual walkthrough scripts and completed notes for the matrix in section 8;
- screenshots or screen recordings only as evidence artifacts if later
  validation work creates them, not as implementation requirements here;
- pending-preservation evidence notes for offline save, restart, failed sync,
  unauthorized/actor drift, retry, and shared-device switch if claimed;
- S06-sensitive mobile-copy review showing candidate/unlinked/missing-known
  language does not imply lifecycle truth;
- support recovery script for invalid token, setup/connect failure, sync
  failure, access ended, and pending work;
- unresolved risk list with owner, route, and pass/revise/stop outcome;
- downstream handoff notes for FD-PKT-002, FD-PKT-005, FD-PKT-101, and ops or
  successor packets where applicable.

## 14. Forbidden Work

- Do not edit code, contracts, schemas, APIs, fixtures, router/status/backlog
  files, README files, BAR/NW, CDL, workshop control files, mobile files, or
  runtime behavior.
- Do not run server tests, mobile tests, Flutter tests, Maven tests, scenario
  probes, or implementation validation commands for this docs packet.
- Do not commit.
- Do not implement or authorize mobile UI, view-models, sync behavior, auth
  behavior, local storage behavior, tests, Candidate 1, or S06.
- Do not create product scope or release readiness.
- Do not use mobile evidence gaps to erase Candidate 1 product need.
- Do not add or imply envelope fields, new event types, scope mechanisms,
  durable workflow state, new contracts, new shared view-models, contract
  schema fields, fixture changes, deployer-authored access logic,
  deployer-authored state machines, scripts, custom traversals,
  device-side triggers, expanded `context.*` refs, expression-function
  vocabulary, or new pattern inventory.
- Do not add or imply canonical entity lifecycle, active/inactive/retired
  truth, discovered-unit lifecycle, registry stewardship, duplicate handling
  workflow, merge/split UX, movement/closure/retirement behavior,
  verification policy, candidate promotion, or S06 lifecycle behavior.
- Do not add or imply production auth/admin/mobile login, browser/provider
  login, token refresh/logout, token expiry, secure storage, online
  binding-admin UI/API, IdP group/claim/JWT `actor_id` authority, local
  encryption, retention/security, expiry, device decommissioning,
  sealed-partition recovery, reporting/export/import, broad audit/history,
  aggregate access divergence, custom/query scope, conflict automation,
  resolver reassignment, auto-resolution, or production readiness.
- Do not make mobile warnings authoritative rejection.
- Do not make latest synced live truth.
- Do not make needs review a production review queue or hard rejection.
- Do not make missing-known-thing/candidate capture canonical registry truth.
- Do not make persona labels identity categories, authority primitives, fixed
  product modules, config namespaces, product-area boundaries, access rules,
  or implementation service boundaries.
- Do not route work back under a retired first-deployment review path. Current
  workshop home is `docs/workshops/first-deployment/`.

## 15. Stop And Report Conditions

Stop and report if:

- active status, routed packet inputs, Stage 5, or workshop docs conflict in a
  way that changes this packet boundary;
- drafting this packet requires editing any file other than this one;
- mobile validation requires code, contract, schema, API, fixture, router,
  status, backlog, README, BAR/NW, CDL, workshop control, or runtime behavior
  changes before implementation routing;
- Candidate 1 cannot honestly remain S01-compatible without maintained known
  things, lifecycle state, discovered-unit stewardship, registry stewardship,
  duplicate stewardship, merge/split UX, lifecycle words, or candidate
  promotion;
- S06 is hidden as vague later work without owner, evidence, route, and
  decision point;
- mobile copy turns candidate, linked, unlinked, known thing,
  missing-known-thing, duplicate suspected, latest synced, needs review,
  correction, or access changed into an event field, event type, scope
  mechanism, flag category, schema/contract field, durable workflow state,
  authority rule, shared API meaning, or lifecycle truth;
- local warning behavior becomes mobile-side hard rejection or local authority;
- latest synced, saved locally, failed sync, needs review, correction, or
  access changed implies live truth, server receipt, approval, rejection, data
  loss, deletion, retention/security, or history rewrite;
- mobile setup requires production login, token refresh/logout, secure
  storage, offline re-auth, local encryption, sealed recovery, or
  decommissioning;
- shared-device validation requires recovery, retention/security, local
  encryption, decommissioning, or cross-actor visibility;
- production-readiness, production auth/admin/mobile login,
  retention/security, reporting/export/import, conflict automation, resolver
  reassignment, auto-resolution, custom/query scope, or ops readiness enters
  Candidate 1 mobile validation;
- acting-context labels harden into identity categories, fixed modules, access
  rules, config namespaces, product-area boundaries, or implementation service
  boundaries;
- unrelated worktree changes appear; leave them alone and report them.

## 16. Done Definition

FD-PKT-004 is done when:

1. This file exists at
   `docs/workshops/first-deployment/task-packets/fd-pkt-004-candidate-1-mobile-offline-validation.md`.
2. No other file is edited.
3. Required sections 1 through 17 are present.
4. The packet defines mobile/offline UX/spec evidence only and does not
   implement or authorize mobile UI.
5. Candidate 1 remains S01-compatible and S06 remains visible with owner,
   route, evidence need, and decision point.
6. Mobile labels/states are mapped to existing backing sources and forbidden
   implications.
7. Candidate 1 mobile evidence, Flutter target descriptions, and manual
   walkthroughs cover setup/connect, work list, standalone capture,
   subject-linked capture, missing-known-thing/candidate capture, offline
   save, sync states, failed sync, unauthorized/actor drift/no connection,
   correction, freshness, needs-review, access-ended/stale access,
   shared-device if claimed, and operator/support recovery.
8. Shared-device, auth/login, S06, product/UX question, artifact, forbidden
   work, stop-condition, and downstream-impact sections are explicit.
9. The file contains no retired first-deployment review path reference.
10. `git diff --check` passes.
11. No server/mobile tests are run.

## 17. Downstream Packet Impacts

| Downstream packet or lane | Impact |
|---|---|
| FD-PKT-002 | Use mobile evidence questions and wording gates to tighten Candidate 1 product/spec validation for setup, assigned work, saved locally, sync failure, correction, latest synced, needs review, access changed, and candidate-only language. |
| FD-PKT-003 | Treat this packet as the mobile/offline evidence expansion for setup/connect, offline save, failed sync, pending preservation, correction, freshness, access-ended/stale access, and shared-device if claimed. |
| FD-PKT-005 | Assess whether mobile states can stay adapter/view composition over existing events, projections, flags, sync metadata, assignment scope, config, and actor partitions. Stop on shared view-model, contract, schema, API, S06 model, lifecycle state, custom scope, reporting, or authority pressure. |
| FD-PKT-101 | Use mobile candidate/unlinked/missing-known-thing and stale/offline evidence as S06 gate input. Candidate 1 implementation remains blocked if users need lifecycle, registry stewardship, candidate promotion, duplicate workflow, or merge/split behavior. |
| FD-PKT-006/007/108 | Carry operator/support recovery, constrained setup, auth manifest, assignment bootstrap, config publish, sync failure, access-ended support, staging rehearsal, and ops-readiness evidence. Do not make FD-PKT-004 a production-readiness packet. |
| FD-PKT-103 | Route productized mobile OIDC/Keycloak login, provider login, token refresh/logout, token expiry, secure storage, shared-device switch UX beyond current boundary, and offline re-auth behavior here. |
| FD-PKT-104 | Route retention/security/device lifecycle, local encryption, expiry, decommissioning, sealed recovery, no-local-retention, redaction, and token/session retention policy here. |
| FD-PKT-105/106/107 | Route reporting freshness, report/export/import, conflict review queues, resolver reassignment, auto-resolution, subject/query/custom scope, special access, and broad audit/history if mobile validation needs them. |
| Later Candidate 1 implementation packets | Must receive one bounded mobile surface only, with exact files/contracts, accepted constructs reused, targeted tests, manual evidence, excluded successor lanes, forbidden work, stop conditions, and commit boundary. This packet is not implementation dispatch. |
