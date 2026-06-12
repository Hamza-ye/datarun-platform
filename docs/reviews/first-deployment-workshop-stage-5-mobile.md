# First Deployment Workshop Stage 5 Mobile Packet

Status: workshop-stage-output

Date: 2026-06-12

Role: Mobile App Builder

Authority: none. This packet assesses mobile feasibility and evidence needs.
It does not authorize implementation, create platform authority, decide product
scope, or replace steward/software-architecture routing.

## 1. Mobile Role Boundary

Stage 5 is a mobile feasibility and evidence packet only.

Mobile can:

- assess current Flutter feasibility for Candidate 1;
- name productization gaps;
- propose Flutter and manual evidence targets;
- identify future task-packet caveats.

Mobile must not:

- reject structurally valid events authoritatively;
- create actor or scope authority locally;
- assume OIDC/token lifecycle exists;
- invent retention, security, decommissioning, or sealed-partition recovery
  behavior.

## 2. Candidate 1 Mobile Flow Feasibility

| Flow | Feasibility | Boundary |
|---|---|---|
| Setup/connect | Feasible today as constrained raw bearer setup. `SetupScreen` verifies `/api/auth/me` and activates the returned actor session. | `product-surface-partial`, not production mobile login. |
| Work list | Feasible as a subject-centric list with active assignment roles, capture count, flag count, latest timestamp, pending-sync count, and sync entry point. | Product IA still needs assigned-work language. |
| Capture | Feasible through shape-driven forms, config promotion, defaults/show conditions, field warnings, and activity-action advisory warnings. | Mobile warnings remain advisory. |
| Optional subject link | Feasible when capturing from subject detail. Standalone capture currently creates a new subject UUID. | Candidate 1 copy/spec must keep standalone capture as unlinked/candidate capture, not S06 lifecycle truth unless FD-PKT-001 moves S06/BAR-105 before implementation planning. |
| Offline save | Feasible. Form save writes a local pending event and tells the user it will sync later. | Label as saved locally, not submitted or server received. |
| Sync | Feasible through actor refresh, push, pull, config fetch, watermarks, pending counts, and error messages. | UX needs clearer waiting/syncing/synced/failed/synced-with-issue states. |
| Correction | Feasible only as another append-only capture path over the same subject. | Dedicated correction UX is not productized and must not imply in-place editing. |
| Review/freshness | Partial. Mobile can show local flags, flagged events, last sync, and latest timestamps. | No production review queue or live truth view. |

## 3. Offline / Sync / Shared-Device Risk Map

| Risk | Mobile reality | Workshop handling |
|---|---|---|
| Offline trust | Local pending events survive failed sync. | UX must say saved on this device and provide retry language. |
| Stale access | Mobile may warn from local assignment/config state. | Server remains responsible for accepting and flagging valid stale work; mobile warnings stay advisory. |
| Actor partition | Current code supports actor-local tokens, watermarks, subject-history cursors, and databases. | Future UI must recreate app state/store for the active actor and never read another actor partition. |
| Shared-device switch | Current `switchActorSession` drains current pending work when possible; otherwise prior work remains sealed. | UX can warn; recovery/decommissioning remains NW-054/BAR-106. |
| Retention | Selective retain is not a security/expiry/decommissioning promise. | Do not describe it as local data deletion policy. |
| Auth failure | Unauthorized or actor-drift checks stop before push and preserve pending work. | UX needs recovery paths without claiming token refresh exists. |
| Freshness | Last sync and latest local timestamp are not live field truth or supervisor approval. | Pair timestamps with latest-synced language and unresolved issue context. |

## 4. Mobile Auth/Login Productization Boundary

Current mobile auth is raw bearer credential entry plus `/api/auth/me` actor
resolution. That is enough for constrained operator-managed use, but not a
productized mobile OIDC/Keycloak login.

Future mobile auth needs a routed decision for:

- browser/provider login;
- refresh;
- logout;
- token expiry;
- secure storage;
- shared-device switch UX;
- offline re-auth behavior;
- error recovery.

Mobile must continue taking actor identity only from `/api/auth/me` or a prior
server-resolved session. UI selection, JWT `actor_id`, provider claims, groups,
or roles must not become actor/scope authority.

## 5. Flutter Test Targets And Manual Walkthroughs

Existing useful Flutter targets:

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

Candidate widget/integration targets:

- Setup success/failure against `/api/auth/me`.
- Form offline save creates one pending event with active actor/session data.
- Advisory warning displays but does not block save.
- Sync panel reports success, push failure, unauthorized, and no-connection
  while preserving pending data.
- Subject detail capture appends another event rather than editing history.
- Shared-device switch drains or seals A, isolates B, then resumes A safely.
- Work list renders pending count, flag count, latest timestamp, and
  empty/no-config states.

Manual walkthroughs:

- invalid token, valid setup, offline capture, app restart, sync retry;
- failed sync with pending work preserved;
- subject-linked capture and missing-known-thing capture;
- correction as appended record;
- stale/access-changed save and later server issue visibility;
- shared-device A-to-B switch with pending work;
- supervisor/latest-synced/freshness interpretation.

## 6. Mobile Implementation Caveats For Future Task Packets

Keep mobile tasks narrow and file-scoped around existing Flutter patterns:

- `AppState`
- `DeviceIdentity`
- `EventStore`
- `SyncService`
- config store
- projection engine
- current screens/widgets

Every task packet should name the UX term and backing state:

- saved locally from pending events;
- waiting to sync from unpushed count;
- synced from push success;
- freshness from sync/projection metadata;
- issue from flags/advisories.

Do not add envelope fields, event types, local permission rules, new scope
filters, durable workflow state, or mobile-side hard rejection. Do not fold
mobile polish together with OIDC login, retention/security, reporting,
conflict automation, or S06 lifecycle.

## 7. Questions For Later Roles

UX Architect:

- What exact labels should replace subject, flag, actor credential, and
  capture in Candidate 1?
- Should standalone capture be shown as unlinked record, missing known thing,
  or another domain term?
- What minimum current-user banner and switch warning is required for shared
  devices?

Software Architect:

- Which view-model shape can expose saved/waiting/synced/failed/issue/freshness
  without creating a new contract?
- Should subject-history appear in Candidate 1 mobile, or stay out of the
  first mobile slice?
- How should mobile surface server flags after sync without implying a review
  queue?

Test Results Analyzer:

- Which existing Flutter tests count as Candidate 1 evidence versus only kernel
  regression evidence?
- Which manual walkthroughs are required before any product-readiness claim?
- What device/OS/offline matrix is enough for first-deployment evidence?

## 8. Advice To Workshop Lead

Treat Candidate 1 mobile as feasible over the accepted kernel, but still
`product-surface-partial`. The next artifact should be a bounded mobile
UX/spec slice, not implementation.

Keep mobile polish separate from mobile OIDC, retention/security, reporting,
admin auth, S06 lifecycle, and conflict automation. Future mobile task packets
must include backing state, excluded successor lanes, Flutter tests, and manual
walkthrough evidence.
