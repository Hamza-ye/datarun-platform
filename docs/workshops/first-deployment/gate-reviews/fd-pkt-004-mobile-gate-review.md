# FD-PKT-004 Mobile Gate Review

## Header

| Field | Value |
|---|---|
| Role | Mobile App Builder |
| Date | 2026-06-13 |
| Gate status | Conditional pass for FD-PKT-004 as a mobile/offline gate input; not implementation authorization |
| Files reviewed | `AGENTS.md`; `docs/status.md` Current Routing; `docs/agent-working-surface/first-deployment-task-packet-router.md`; `docs/workshops/first-deployment/README.md`; `docs/workshops/first-deployment/stage-5-mobile.md`; `docs/workshops/first-deployment/task-packets/fd-pkt-003-candidate-1-evidence-plan.md`; `docs/workshops/first-deployment/task-packets/fd-pkt-004-candidate-1-mobile-offline-validation.md`; `docs/workshops/first-deployment/task-packets/fd-pkt-005-candidate-1-view-model-contract-assessment.md`; `docs/workshops/first-deployment/task-packets/fd-pkt-101-s06-entity-lifecycle-discovery.md` |
| Mobile files reviewed | None |
| Tests run | None |

## Mobile Gate Finding For FD-PKT-004

FD-PKT-004 is acceptable as the mobile/offline gate review input before a bounded mobile implementation packet is drafted, with conditions.

The packet stays inside the current first-deployment routing: it treats mobile work as `product-surface-partial`, maps visible mobile labels to existing backing state, keeps mobile warnings advisory, and does not create new authority, contracts, APIs, event vocabulary, S06 lifecycle behavior, retention/security behavior, or production mobile login scope.

Its strongest gate value is the state-to-source discipline. Saved locally, waiting to sync, synced, synced with issue, failed, unauthorized, actor drift, no connection, latest synced, needs review, access changed, correction, and shared-device if claimed are all tied to existing local state, sync metadata, flags/advisories, projections, assignments/config, timestamps, and actor partitions rather than new platform facts.

Residual risk remains high around mobile copy. Words such as known thing, candidate, duplicate suspected, inactive, closed, moved, retired, verified, merge, and split can cause users to infer S06 lifecycle truth. FD-PKT-004 handles that correctly by making S06 a visible gate and by requiring user/SME evidence before any Candidate 1 implementation packet uses those claims.

## Narrow Mobile Slice That May Be Safe To Dispatch If Any

The narrowest safe mobile implementation slice is a mobile-only adapter/copy slice over existing state. It should not be a full Candidate 1 flow, not a new shared view model, and not a contract/API change.

The slice may compose and display a small Candidate 1 status surface from existing mobile sources:

- active server-resolved actor/session display from raw bearer setup plus `/api/auth/me`, only as constrained setup;
- pending, waiting to sync, syncing, synced, synced with issue, failed, unauthorized, actor drift, and no-connection labels from existing local event and sync state;
- saved-on-this-device wording from pending local events in the active actor partition;
- issue/needs-review indicators from existing flags/advisories and projections;
- latest synced/freshness wording from timestamps, pull/sync metadata, and projection metadata;
- append-only correction wording only where the current capture path already appends history;
- shared-device warning/isolation UI only if Candidate 1 explicitly claims shared-device use.

The future packet must name exact mobile files after inspection. From Stage 5, likely surfaces are existing `AppState`, `DeviceIdentity`, `EventStore`, `SyncService`, config store, projection engine, and current screens/widgets. The packet must stop if inspection shows the slice needs a new shared contract, new API response meaning, new local authority, mobile-side hard rejection, secure storage/encryption/retention semantics, or S06 lifecycle state.

## Required Flutter/Test Boundaries To Carry Forward

No Flutter tests were run for this review. A future implementation packet must choose exact commands for the touched surface and keep tests targeted.

Carry these existing test names as candidate evidence inventory, not mandatory commands for every slice:

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

The implementation packet must carry these boundaries where touched:

- setup success/failure against `/api/auth/me`, with no mobile OIDC/Keycloak login claim;
- offline form save creates one pending event with active actor/session data;
- advisory warnings display without blocking structurally valid saves;
- sync status covers success, push failure, unauthorized, actor drift, and no connection while preserving pending work;
- restart, failed sync, auth failure, and retry preserve pending work for the same actor/session;
- work-list/status UI renders pending count, issue count, latest timestamp, sync entry, empty state, and no-config state only from existing backing state;
- subject-linked capture, if touched, appends another event and does not imply lifecycle truth or in-place edit;
- missing-known-thing/candidate capture remains unlinked or review-oriented evidence;
- shared-device tests are required only if shared devices are claimed, and must prove A-to-B isolation, drain-or-seal behavior, pending warning, and safe resume where allowed;
- static/widget copy review must reject lifecycle, production-readiness, retention/security, reporting, hard-rejection, live-truth, and authority overclaims.

## Required Manual Walkthroughs To Carry Forward

Each walkthrough must record acting context, authority mapping, device/network setup, exact vocabulary tested, observed comprehension, defects or risks, route triggers, and pass/revise/route outcome.

Carry forward these manual walkthroughs:

- invalid token during constrained setup;
- valid setup through `/api/auth/me` and active actor/session display;
- offline capture saved locally with pending count;
- app restart before sync with pending work preserved;
- sync retry and transition through waiting/syncing/synced/synced-with-issue;
- failed sync, unauthorized, actor drift, and no connection with pending preservation;
- subject-linked capture against an existing known thing where available;
- missing-known-thing or candidate capture as unlinked/review evidence;
- correction as append-only follow-up record;
- stale/access-changed save and later issue visibility;
- shared-device A-to-B switch only if shared devices are claimed;
- supervisor/latest-synced/freshness interpretation;
- operator/support recovery for invalid token, setup failure, sync failure, access ended, and pending work.

## Mobile Claims That Remain Blocked/Excluded

These remain out of FD-PKT-004 and any narrow mobile implementation packet:

- productized mobile OIDC/Keycloak login, browser/provider login, token refresh/logout, token expiry UX, secure storage, and offline re-auth;
- local encryption, retention/security policy, expiry, device decommissioning, sealed recovery, redaction/no-local-retention, and token/session retention policy;
- mobile authoritative rejection of structurally valid work;
- canonical known-thing registry creation, active/inactive/retired/closed/moved/verified lifecycle truth, discovered-unit lifecycle, candidate promotion, duplicate workflow, registry stewardship, automatic matching, and merge/split UX;
- new envelope fields, event types, schema fields, config package keys, API meanings, scope mechanisms, durable workflow state, or shared view-model contracts;
- reporting dashboards/APIs/export/import, report freshness truth, broad audit/history, aggregate access, conflict review queues, direct flag mutation, resolver reassignment, auto-resolution, custom/query scope, special access, and production readiness.

Shared-device claims are not generally authorized. They are either excluded from the first mobile implementation slice or explicitly evidence-gated with A-to-B isolation, drain-or-seal behavior, pending preservation, no cross-actor visibility, and no retention/security or recovery promise.

## Explicit Implementation Unblock Position

UNBLOCK WITH CONDITIONS

This is a Mobile App Builder gate position for Project Shepherd consolidation only. It does not authorize implementation by itself.

Conditions:

- FD-PKT-004 may be accepted as a mobile/offline gate input only after consolidation keeps Candidate 1 implementation dependent on FD-PKT-002, FD-PKT-003, FD-PKT-005, and FD-PKT-101 gates.
- FD-PKT-101 must choose, promote, split, or explicitly exclude/defer S06 before any Candidate 1 implementation packet can honestly use missing-known-thing, candidate, duplicate, or lifecycle-adjacent language.
- The first mobile implementation packet must be one bounded mobile surface with exact files, exact tests, exact manual walkthrough evidence, excluded successor lanes, forbidden work, stop conditions, and commit boundary.
- The implementation packet must stay adapter/view composition over existing mobile state and server-resolved authority.
- Shared-device behavior must be absent unless explicitly claimed and evidence-gated.

## Stop/Route Conditions

Stop and route through Project Shepherd if:

- Candidate 1 mobile copy or UX needs maintained known things, lifecycle state, discovered-unit stewardship, registry stewardship, duplicate workflow, candidate promotion, or merge/split behavior;
- mobile validation requires production mobile login, token lifecycle, secure storage, offline re-auth, local encryption, retention/security, device decommissioning, sealed recovery, or device-loss recovery promises;
- local warnings become mobile-side hard rejection or local access/scope authority;
- UI-selected actor, JWT `actor_id`, IdP group/role/claim, persona label, module name, or support label becomes actor or scope authority;
- shared-device work requires cross-actor visibility, recovery, retention/security, decommissioning, sealed recovery, local encryption, or token/session retention policy;
- saved locally implies server receipt, approval, global completeness, retention, or recoverability after device loss;
- latest synced implies live field truth, supervisor approval, audit completeness, or report truth;
- needs review implies production review queue, direct flag mutation, hard rejection, resolver reassignment, or auto-resolution;
- correction implies in-place edit, deletion, replacement, cancellation, resolver reassignment, or history rewrite;
- any new contract, schema, API, fixture, event vocabulary, scope mechanism, durable workflow state, shared view-model contract, or mobile local authority is needed;
- shared-device claims are made without explicit evidence boundaries;
- unrelated worktree changes appear during implementation packet drafting.

Route triggers:

- FD-PKT-101/BAR-105 for known-set source, lifecycle, candidate promotion, duplicate workflow, registry stewardship, or merge/split needs.
- FD-PKT-103 for mobile OIDC/Keycloak login, provider login, token lifecycle, secure storage, or offline re-auth.
- FD-PKT-104 for retention/security, local encryption, expiry, decommissioning, sealed recovery, redaction/no-local-retention, or token/session retention.
- FD-PKT-005 or a successor contract packet for shared view-model, API, schema, config package, sync, flag, scope, or durable-state pressure.
- FD-PKT-105 for reporting freshness, dashboards, reports, exports, imports, aggregate access, or broad audit/history.
- FD-PKT-106 for conflict review queues, direct flag mutation, resolver reassignment, batch handling, or auto-resolution.
- FD-PKT-107 for custom/query scope, auditor/report filters as authority, special access, or new scope mechanisms.
- FD-PKT-006/007/108 for ops readiness, runbooks, staging rehearsal, monitoring, incident response, backup/restore, rollback, TLS/secrets, or release-readiness claims.
