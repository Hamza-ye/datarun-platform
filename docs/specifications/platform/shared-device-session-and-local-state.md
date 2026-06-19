# Shared-Device Session And Local State

Status: accepted
Document type: platform_spec
Owner: mobile/sync verifier
Source: NW-071 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-071-extract-shared-device-session-and-local-state-durable-behavior.md`
Authority: BAR-104 and BAR-106; `contracts/sync-protocol.md`; `contracts/envelope.schema.json`; `contracts/config-package.schema.json`; IDR-030 as historical decision input; NW-052/NW-055 implementation evidence; implementation boundaries in `docs/implementation/module-interfaces.md`
Last reviewed: 2026-06-19
Supersedes: none
Related: `docs/specifications/product/product-candidate-1.md`; `docs/specifications/platform/production-auth-principal-binding.md`; `docs/specifications/platform/assignment-scope-and-administration.md`; `docs/specifications/platform/configuration-package-and-shapes.md`; `contracts/sync-protocol.md`; `contracts/envelope.schema.json`; `contracts/config-package.schema.json`; `docs/agent-working-surface/baseline-acceptance-register.md`; `docs/agent-working-surface/platform-next-work-backlog.md`; `docs/agent-working-surface/artifacts/NW-049-access-exceptions-shared-device-scope-exploration.md`; `docs/decisions/idr-030-shared-device-session-lifecycle.md`; `docs/agent-working-surface/prompts/NW-052-decide-shared-device-session-lifecycle.md`; `docs/agent-working-surface/prompts/NW-055-implement-shared-device-actor-partitions.md`; `docs/implementation/module-interfaces.md`; `mobile/lib/data/device_identity.dart`; `mobile/lib/data/sync_service.dart`; `mobile/lib/data/event_store.dart`; `server/src/main/resources/db/migration/V10__actor_scoped_device_sync_state.sql`; `mobile/test/sync_service_test.dart`; `mobile/test/event_assembler_test.dart`; `mobile/test/config_store_test.dart`; `mobile/test/projection_engine_test.dart`; `mobile/test/pattern_projection_test.dart`; `mobile/test/selective_retain_test.dart`; `server/src/test/java/dev/datarun/server/sync/SyncControllerIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/ProductionAuthIntegrationTest.java`; `server/src/test/java/dev/datarun/server/config/ConfigIntegrationTest.java`; `server/src/test/java/dev/datarun/server/sync/SubjectHistoryBackfillIntegrationTest.java`; `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`

## Purpose

This specification records accepted platform behavior for shared physical
devices, actor sessions, actor-local mutable state, actor-scoped sync
bookkeeping, sealed pending work, and local-state vocabulary.

It extracts durable behavior from IDR-030, NW-052, NW-055, BAR evidence,
contracts, product Candidate 1 pressure, module boundaries, and implementation
tests. It does not change runtime behavior, mobile login, token lifecycle,
contracts, schemas, event envelope fields, sync protocol behavior, tenant
internals, retention/security policy, operations evidence, or real-production
standing.

## Contract And Trace Decision

This platform spec owns the accepted local/session behavior that is not durable
enough when left only in IDR prose, module-boundary notes, product terms, or
implementation evidence. The behavior crosses authentication, sync, mobile
local storage, config package delivery, and product copy, while still operating
inside existing architecture and contract boundaries.

The existing contracts remain authoritative for process and wire shapes:

| Surface | Contract-owned content |
|---|---|
| `contracts/envelope.schema.json` | Event envelope fields, closed envelope `type` vocabulary, and event author/device fields. |
| `contracts/sync-protocol.md` | Push, pull, config, and subject-history endpoint shape, bearer authentication, watermarks, cursors, error vocabulary, actor-binding errors, config discovery, and subject-history live-sync isolation. |
| `contracts/config-package.schema.json` | Server-emitted/mobile-consumed config package body shape and unknown top-level key tolerance. |

This spec owns the prose behavior those contracts do not express: exactly one
active actor on a shared physical device, drain-or-seal switching, actor-local
mutable partitions, shared immutable config blob limits, sealed pending-work
boundaries, actor-scoped sync bookkeeping interpretation, and product-safe
local-state vocabulary.

Implementation details such as exact SQLite table names, key names, Dart or
Java helper layout, migration mechanics, file paths, and SQL details are
evidence only unless this specification names the behavior as accepted.

IDR-030, NW-052, and NW-055 remain historical implementation provenance and
evidence. After NW-071 acceptance, use this specification, the contracts above,
BAR-104/BAR-106 standing, and the current module boundaries as the durable
target for shared-device session and local-state behavior. Do not treat old IDR
prose as a parallel active specification.

## Session Boundary

A shared physical device may hold data for more than one actor only under a
single-active-actor session model.

Accepted behavior:

- exactly one actor session is active on the physical device at a time;
- actor-scoped capture, advisory checks, event assembly, push, pull, config
  promotion, subject-history backfill, and local views use only the active
  actor partition;
- a writable actor session requires a server-resolved actor context from
  `GET /api/auth/me` or a previously established actor session whose actor id
  was resolved by the server;
- a UI-selected person, locally typed actor id, product role, Organization
  label, IdP group, IdP role, resource claim, custom claim, or JWT `actor_id`
  cannot create a writable actor session;
- no actor-scoped local data is visible or pushable while a switch is
  incomplete.

This is actor-session behavior, not a scope mechanism. Assignment-derived
access, role-action authority, resolver equality, and conflict/flag behavior
remain governed by their accepted platform specifications and contracts.

## Actor Refresh And Resume

The active local session must align with server actor resolution.

Accepted refresh and resume paths:

- a new or refreshed session calls `GET /api/auth/me` with the bearer
  credential and stores the returned actor id as the session actor;
- a prior actor session may be resumed only when it was originally established
  through server resolution and its available credential still resolves to the
  same actor;
- before push, the client must stop rather than push if the credential is
  unauthorized, no actor id is resolved, or the server-resolved actor differs
  from the active local session;
- push, pull, config, and subject-history requests use only the active
  session's credential.

The auth-source label returned by `/api/auth/me` is diagnostic context. It is
not authority and cannot override the resolved actor id.

## Drain-Or-Seal Switching

Switching from actor A to actor B must complete A's active work boundary before
B can see or create actor-scoped local state.

Accepted switch sequence:

1. Stop A capture, advisory evaluation, config promotion, normal pull,
   subject-history backfill, and push scheduling.
2. Drain A's pending push with A's credential when connectivity and A's still
   valid session allow it.
3. If A cannot be drained, seal A's actor partition.
4. Acquire B's credential and resolve B through `/api/auth/me`, or resume a
   previously established B session whose actor id came from server resolution.
5. Select or create B's actor-local partition before any B-authored event,
   normal pull, subject-history page, config promotion, advisory check, or
   local actor view occurs.

An interrupted switch may recover to A active, B active, or no active actor. It
must not recover to B with A's actor-scoped local data visible, pushable, or
used for advisory/projection state.

## Actor-Local Mutable Partitions

Mutable local state is actor-local. A shared mutable store with UI filtering is
not an accepted model.

Each actor partition owns:

- local event rows;
- pending-push queues;
- local projections and advisory state;
- local assignments and assignment-derived views;
- aliases and subject/projection support state derived from the actor's local
  event set;
- normal pull watermarks;
- `last_pull_watermark` used for push concurrency detection;
- subject-history cursors;
- active and pending config package version state and promotion metadata;
- token/session material retained for that actor session.

The physical device id and device-local sequence may remain shared device
provenance for event envelopes. They do not make one actor's mutable local
state visible to another actor and do not make sync progress device-global for
actor-scoped behavior.

Purging a partition on every switch may be a future deployment/security
posture, but it is not the current platform minimum and must not be assumed by
product copy. The current accepted minimum is actor partitioning plus safe
sealing of pending work.

## Immutable Shared Config Blobs

Published config package blobs may be cached outside an actor partition only
when they are immutable, read-only package content and contain no actor-scoped
data.

Actor-local state still owns:

- which config version is active for that actor;
- which config version is pending for that actor;
- config promotion metadata;
- device-reported config-version observation for sync/config bookkeeping.

Actor B must not inherit actor A's active/pending config state, promotion
state, or sync/config observation merely because both actors can read the same
immutable package blob.

## Actor-Scoped Sync Bookkeeping

Server sync bookkeeping for normal pull/config observation is keyed by the
physical `device_id` and authenticated actor id. It is operational
bookkeeping, not authority.

Accepted behavior:

- normal pull remains bearer-authenticated, actor-scoped, and evaluated against
  the actor's current assignments at request time;
- when pull includes `device_id`, server-side pull/config observation is
  recorded for `(device_id, authenticated actor_id)`;
- actor A's high normal pull watermark must not cause actor B to skip
  authorized data;
- actor B's pull/config activity must not lower, rewrite, or corrupt actor A's
  normal pull watermark;
- `last_pull_watermark` sent on push comes from the active actor partition;
- subject-history cursors are actor-local client state, while the
  subject-history endpoint remains an independent subject/activity-bound
  backfill surface that does not mutate normal pull bookkeeping.

This specification does not add sync reset semantics, audit/history pull, or
normal live-sync watermark rewrites.

## Sealed Pending Work

Human-authored pending work belongs to the actor partition where it was
created.

Accepted behavior:

- pending events keep their original `actor_ref`;
- pending events can be pushed only with a credential that resolves to the
  same actor as the event `actor_ref.id`;
- pending events must not be reauthored, rewritten, transferred into another
  actor's queue, shown in another actor's local work list, or signed with
  another actor's request credential;
- actor B's background sync, config, and advisory work must not process actor
  A's sealed partition;
- if A's credential is unavailable, expired, revoked, or no longer resolves to
  A, A's pending work remains sealed until a same-actor resume or a separately
  decided recovery path exists.

Recovery/export of abandoned sealed partitions, token expiry handling while
events remain pending, and administrator access to another actor's local
pending work are retention/security questions for NW-054 or a successor
decision. They are not accepted by this specification.

## Product-Safe Local-State Vocabulary

Product and UI copy may describe the current behavior with bounded terms that
do not create retention, encryption, deletion, or security promises.

| Term | Safe meaning | Must not imply |
|---|---|---|
| `saved locally` | Captured in the active actor partition and not necessarily accepted by the server yet. | Server persistence, cross-device visibility, retention duration, encryption, or backup. |
| `waiting to sync` | The active actor partition has pending work that has not completed push/pull sync. | Data loss, server rejection, or background sync for another actor. |
| `active actor session` | The one server-resolved actor session currently allowed to assemble, view, and sync actor-scoped local state. | UI-selected authority, IdP claim authority, tenant/workspace selection, or multi-actor activity. |
| `actor partition` | Actor-local mutable events, pending work, projections, advisory state, config state, watermarks, cursors, and token/session material. | Separate tenant storage, long-term retention guarantee, encryption boundary, or legal data compartment. |
| `sealed pending work` | Prior-actor pending work hidden from other actors and pushable only by the same resolved actor or a future accepted recovery path. | Erasure, secure deletion, archival, administrator recovery, or cross-actor transfer. |
| `shared setup package` | Immutable read-only config package content that may be cached once and used by multiple actor partitions. | Actor-specific setup authority, per-actor package variant, or shared mutable config state. |

Avoid language such as "securely erased", "encrypted", "retained for X days",
"recoverable by admin", "no local copy", "tenant-isolated", or "safe for real
production" unless a successor NW accepts that behavior.

## Non-Goals

This specification does not authorize:

- mobile OIDC/login, refresh/logout, secure storage, provider selection,
  shared-device login UX, or token lifecycle decisions;
- token/session retention policy beyond current same-actor partitioning and
  sealed pending-work boundaries;
- local expiry, device decommissioning, remote wipe, no-local-retention views,
  local encryption, erasure, redaction, sensitivity handling, or sealed
  partition recovery;
- tenant-aware auth, tenant/workspace local partition keys, tenant sync
  context, workspace-scoped config, pooled storage predicates, bridge
  isolation, or SaaS control-plane behavior;
- event envelope fields or envelope `type` values;
- sync protocol shape, watermark reset semantics, broad audit/history pull,
  subject-history expansion, config package shape, or assignment payload
  changes;
- new geographic, subject, activity, query, custom, auditor, or emergency
  scope mechanisms;
- UI-selected actor authority, local-only writable offline actors, IdP
  group/role/claim authority, or JWT `actor_id` authority;
- mobile authoritative rejection of structurally valid state/policy anomalies;
- real-production approval, real users/data approval, provider/region
  selection, jurisdiction/data classification, support commitment, or
  compliance/security review.

## Escalation Triggers

Route a successor before work depends on any of the following:

- NW-085: mobile provider login, refresh/logout, secure storage, shared-device
  login UX, external field-user login readiness, or retained token lifecycle.
- NW-054: expiry, device decommissioning, sealed-partition recovery, local
  encryption, token/session retention, no-local-retention, erasure, redaction,
  sensitivity treatment, administrator recovery/export of sealed work, or other
  retention/security claims.
- NW-094 through NW-096: managed-deployment control-plane behavior,
  tenant-aware identity/membership, tenant-aware data isolation, tenant sync
  context, workspace-scoped config, or mobile tenant partition keys.
- Contract/architecture route: envelope field/type changes, sync protocol
  changes, config package schema changes, assignment payload changes, new
  watermarks/reset semantics, new scope mechanisms, or changes to
  process-boundary error vocabulary.
- NW-093: real-production approval, real users/data, provider/region,
  jurisdiction, support commitment, compliance/security review, or operational
  go/no-go.

Stop rather than implement around the issue if a slice needs IdP claims or UI
selection as actor authority, local-only writable offline actors, cross-actor
request signing, server event deletion/mutation, normal sync watermark
rewrites, or broad audit/history access.

## Acceptance Evidence

NW-052 accepted IDR-030 as the shared-device session lifecycle decision input.
NW-055 implemented the accepted actor-partition behavior and recorded focused
mobile/server evidence:

- `flutter test` passed with 107 tests;
- `./mvnw -Dtest=SyncControllerIntegrationTest,ProductionAuthIntegrationTest,ConfigIntegrationTest,SubjectHistoryBackfillIntegrationTest,ResponsibilityBindingScenarioIntegrationTest test` passed with 70 tests;
- full `./mvnw test` passed with 321 tests.

BAR-104 remains the accepted production auth/principal-binding baseline:
provider credentials authenticate only through explicit active principal
bindings, and groups, roles, resource claims, and JWT `actor_id` are not direct
platform authority.

BAR-106 remains a future-decision route for retention/security behavior beyond
current actor partitioning and sealed pending-work boundaries.

NW-071 acceptance requires this platform specification to be indexed from
`docs/specifications/platform/README.md`, documentation-only verification to
pass, and active status/backlog surfaces to route future shared-device,
mobile-login, local-state, tenant-lane, and product-copy work through this spec
instead of IDR-030 prose.
