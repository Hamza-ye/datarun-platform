# Mobile OIDC Login And Token Lifecycle

Status: accepted
Document type: platform_spec
Owner: mobile/security verifier
Source: NW-085 row in `docs/agent-working-surface/platform-next-work-backlog.md` and `docs/agent-working-surface/prompts/NW-085-decide-mobile-oidc-login-and-token-lifecycle.md`
Authority: `docs/specifications/product/product-candidate-1.md`; `docs/specifications/platform/production-auth-principal-binding.md`; `docs/specifications/platform/shared-device-session-and-local-state.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md` for mobile non-goal separation; BAR-104 and BAR-106; implementation boundaries in `docs/implementation/module-interfaces.md`
Last reviewed: 2026-06-19
Supersedes: none
Related: `docs/specifications/platform/production-auth-principal-binding.md`; `docs/specifications/platform/shared-device-session-and-local-state.md`; `docs/specifications/platform/production-web-admin-authentication-and-authority.md`; `docs/specifications/product/product-candidate-1.md`; `contracts/sync-protocol.md`; `docs/agent-working-surface/platform-next-work-backlog.md`; `docs/agent-working-surface/prompts/NW-085-decide-mobile-oidc-login-and-token-lifecycle.md`; `docs/implementation/module-interfaces.md`; `mobile/lib/data/device_identity.dart`; `mobile/lib/data/sync_service.dart`; `mobile/lib/data/event_store.dart`

## Purpose

This specification selects the Product Candidate 1 mobile field-user login and
token lifecycle boundary before mobile implementation work begins.

It accepts a product/platform/security decision for external field-user mobile
login, actor-session alignment, refresh/logout/re-login expectations,
offline behavior, shared-device login boundaries, token-placement limits, and
successor implementation evidence.

It does not implement mobile login, mobile UI, token storage, tests, runtime
code, contracts, schemas, event envelope fields, sync protocol behavior,
tenant-aware internals, operations evidence, retention/security policy, or
real-production approval.

## Decision Summary

The selected Product Candidate 1 mobile login shape is:

```text
mobile system browser or external user-agent OIDC authorization-code flow with PKCE
-> optional OS/provider native-app broker handoff when available
-> provider-issued bearer credential presented to Datarun APIs
-> GET /api/auth/me resolves the credential through explicit principal binding
-> one active server-resolved actor session
-> actor-local mutable partition for mobile work and sync state
```

The provider credential authenticates the provider principal only. Platform
access still requires the accepted explicit active binding:

```text
(issuer, subject) -> actor_id
```

The mobile app must align every login, refresh, same-actor resume, re-login,
and actor-scoped push with the server-resolved `actor_id` returned by
`GET /api/auth/me`. A provider login result, provider account label, IdP group,
role, claim, resource claim, custom claim, JWT `actor_id`, mobile-selected
actor, typed actor id, Organization label, product role, or local session label
is not platform actor, assignment, resolver, admin, or sync authority.

## Selected Login Shape

Product Candidate 1 selects an external user-agent OIDC authorization-code
flow with PKCE for mobile field users.

Accepted behavior:

- mobile initiates login through the device system browser, custom tab,
  authentication session, or equivalent external user-agent boundary;
- the app may allow a provider or operating-system native broker to handle
  authentication when the provider and device support it;
- native broker use is optional and must return through the same accepted
  provider credential and server actor-resolution path;
- the app must not treat successful provider authentication as a writable
  Datarun actor session until `/api/auth/me` returns a server-resolved actor;
- mobile API calls use the resulting bearer credential only through the
  accepted actor-scoped API boundary;
- the temporary bearer setup path may remain for internal development,
  automated tests, and synthetic demonstrations, but it is not the external
  field-user Product Candidate 1 login shape.

The selected shape is intentionally generic. It does not approve a concrete
production provider, provider tenant, realm, client registration, region,
jurisdiction, support model, notification path, compliance review, or real
users/data rollout. Those choices remain NW-093 real-production approval or
operations commitments.

### Rejected Alternatives

| Alternative | Decision |
|---|---|
| Embedded web view login | Rejected for Product Candidate 1. The app must not host provider credential entry inside an embedded web view as the accepted external field-user login boundary. |
| Manual bearer token entry or QR/token paste for external users | Rejected for external field users. It may remain only for internal, test, or synthetic candidate setup paths. |
| Web-admin server-managed browser session reused by mobile | Rejected. `/web-admin` browser sessions and command authority are separate from mobile bearer-bound actor APIs. |
| Mandatory native provider app broker | Rejected as a requirement. Broker handoff is allowed when available, but Product Candidate 1 must not depend on a provider/device-specific broker as the only login shape. |
| Resource-owner password collection by the mobile app | Rejected. Mobile must not collect provider passwords directly as the accepted login shape. |
| IdP group, role, claim, profile, or JWT `actor_id` as actor mapping | Rejected. Actor mapping remains explicit active principal binding only. |
| Offline self-registration or local actor picker | Rejected. No local-only writable actor session may be created. |

## Provider Boundary

This decision accepts only the mobile application boundary for an
OIDC-compatible login flow and the platform alignment that follows it.

Provider responsibilities outside this specification include:

- concrete provider product selection;
- provider tenant, realm, app registration, redirect URI approval, region, and
  jurisdiction;
- provider support, notification, uptime, breach, continuity, and compliance
  commitments;
- exact token lifetime, refresh-token issuance, revocation, and end-session
  support;
- production approval for real users or real organizational data.

Datarun platform behavior inside this specification is limited to:

- accepting a bearer credential on existing actor-scoped API surfaces;
- validating the credential through the accepted production auth provider
  boundary;
- resolving the validated principal through explicit active
  `(issuer, subject) -> actor_id` binding;
- returning the server-resolved actor from `/api/auth/me`;
- requiring mobile to align local actor-session state with that response.

Provider display names, email addresses, profile fields, groups, roles, and
claims may be shown only as non-authority context if a successor product/UI
slice accepts that copy. They must not create actor authority, assignment
authority, resolver authority, admin authority, tenant selection, or sync
scope.

## Actor Resolution And `/api/auth/me` Alignment

Mobile actor state is established by the server, not by the login UI.

Accepted alignment behavior:

- after a provider login result, the app calls `GET /api/auth/me` before any
  actor-scoped local work is activated for that credential;
- `/api/auth/me` returns the server-resolved `actor_id` and diagnostic auth
  source after the accepted bearer resolver validates the credential and
  active principal binding;
- the returned `actor_id` selects the active actor session and actor-local
  mutable partition;
- event assembly uses the active server-resolved actor id for `actor_ref.id`;
- push, pull, config, and subject-history requests use the active session's
  bearer credential;
- before actor-scoped push, mobile must stop rather than push if
  `/api/auth/me` is unauthorized, resolves no actor, or resolves an actor
  different from the active local session;
- a refreshed or re-login credential that resolves to the same actor may
  resume the same actor partition;
- a refreshed or re-login credential that resolves to a different actor is an
  actor switch and must follow drain-or-seal behavior before the new actor can
  see or create actor-scoped local state.

The auth-source label returned by `/api/auth/me` is diagnostic context only.
It cannot override the resolved actor id.

## Refresh, Expiry, Logout, And Re-Login

This specification accepts minimum lifecycle expectations without deciding
provider token lifetime, storage cleanup, revocation, or retention policy.

### Refresh

If the implementation has provider-issued refresh capability, refresh may
renew the bearer credential used for Datarun APIs. After every refresh, mobile
must call `/api/auth/me` and verify that the server-resolved actor still
matches the active local actor session before actor-scoped push or session
continuation.

If refresh fails, is unavailable, is revoked, or resolves to no actor, the app
must stop network actor actions until re-login succeeds. Existing local work
remains in its actor partition and may be pushed only after a credential again
resolves to the same actor.

### Expiry

An expired or rejected credential cannot be used for push, pull, config,
subject-history, assignment, conflict, or `/api/auth/me` success.

When expiry is known while the same server-resolved actor session remains
locally active, the app may continue only the offline behavior that is already
allowed for that actor partition. It must show that sign-in is needed before
sync can complete. It must not create a new writable actor, transfer pending
work, sign another actor's requests, or claim that pending work was accepted
by the server.

### Logout

Logout ends the active mobile actor session for interactive use on the device.

Accepted logout behavior:

- stop new capture, advisory work, background push/pull/config, and
  subject-history scheduling for the actor being logged out;
- when pending work exists and the actor still has a valid credential and
  connectivity, the app may offer a sync-before-sign-out path;
- if pending work cannot be drained, the actor partition is sealed under the
  shared-device boundary;
- after logout, no actor-scoped local data is visible or pushable until a
  same-actor resume or new login resolves through `/api/auth/me`;
- the next actor cannot use the previous actor's credential, partition,
  pending work, config state, watermarks, cursors, or local views.

Provider-global logout, token revocation, end-session semantics, storage
cleanup, retention duration, and deletion posture are not accepted here. Route
those questions to NW-054 or NW-093 as appropriate.

### Re-Login

Re-login always re-enters through provider authentication or an accepted
same-actor credential path, then `/api/auth/me`.

Accepted re-login behavior:

- same resolved actor: resume that actor's partition and sync state subject to
  current assignments and server responses;
- different resolved actor: perform actor switching and select the other
  actor's partition only after drain-or-seal completes;
- unbound, invalid, deactivated, or unresolved principal: no writable actor
  session is created;
- changed principal binding never rewrites events already authored with the
  previous resolved actor.

## Token Placement Posture

This route accepts only placement and non-leakage boundaries. It does not
accept local encryption, secure deletion, recovery, revocation, retention
duration, no-local-retention, erasure, redaction, remote wipe, device
decommissioning, sealed-partition recovery, or sensitivity treatment.

Accepted token-placement rules:

- bearer credentials and refresh material must not be written into event
  envelopes, event payloads, assignment payloads, config packages, exported
  setup, operation-history rows, domain events, or sync request bodies;
- API credentials are sent to Datarun APIs only through the bearer
  authorization boundary, not as form fields, JSON fields, query parameters,
  actor labels, or config values;
- mobile logs, error messages, diagnostics, and audit-like traces must not
  include raw bearer credentials, refresh material, authorization codes,
  provider secrets, passwords, session cookies, or private keys;
- token/session material retained by the mobile app belongs to the
  server-resolved actor session and must not be stored in cross-actor mutable
  state or used by another actor partition;
- token/session material must not make one actor's local events, pending work,
  config state, watermarks, cursors, projections, assignments, or advisory
  state visible to another actor.

The exact mobile storage mechanism is an implementation detail for a successor
slice, bounded by the rules above and by NW-054 for retention/security claims.

## Offline Behavior

Mobile offline behavior depends on whether a server-resolved actor session has
ever been established.

| State | Accepted behavior |
|---|---|
| Before first login while online | The app may show Organization-level entry and sign-in affordances. It must not create writable actor-scoped work, fetch actor-scoped config, or assemble actor-authored events until `/api/auth/me` resolves an actor. |
| Before first login while offline | The app may show that sign-in requires connectivity. It must not create a local-only actor, capture writable work, sync, or select a previous actor that was never server-resolved. |
| After successful login | The app may activate the returned actor, select that actor partition, fetch config and assigned work, capture configured activity entries online or offline where local setup and assignment context allow, and sync with the active credential. |
| Offline after prior successful login | The app may continue allowed offline work only in the active server-resolved actor partition using local setup and assignment context already available. It must present sync completion as pending until connectivity and valid same-actor credentials allow sync. |
| Credential expired, revoked, or refresh failed | The app must stop network actor actions and require re-login or refresh. It may keep same-actor offline local state visible only under the active prior server-resolved actor session. Pending work can sync only after a credential resolves to the same actor. |
| After logout | There is no active actor for interactive work. Prior actor data is not visible or pushable to the next user. Pending work remains sealed unless the same actor signs in again or a future accepted recovery path exists. |
| During shared-device switch | The app stops old-actor work, drains when possible, seals when necessary, and activates the new actor only after `/api/auth/me` resolves that actor. No actor-scoped data is visible while the switch is incomplete. |

Offline behavior does not change server authority. Structurally valid but
unauthorized state/policy anomalies remain accepted-and-flagged only after the
server accepts a properly authenticated, actor-bound push.

## Shared-Device Login UX Boundary

Product and UI copy for mobile login must fit the accepted shared-device
session model without implying retention/security behavior that has not been
accepted.

Accepted UX boundaries:

- `Sign in` starts provider authentication and then server actor resolution;
- `Sign out` ends the active actor session for interactive use and makes the
  actor partition unavailable to other users;
- `Switch user` or `Switch account` means drain-or-seal the current actor and
  resolve the next actor through `/api/auth/me`;
- `Resume` may be offered only for a previously server-resolved actor session
  and still requires same-actor credential validation before sync;
- actor names, account labels, or provider display information may help the
  user recognize an account but must not create authority;
- sealed pending work may be described as work waiting for the same user to
  sign in and sync;
- the app may ask a user to sync before sign-out or switch when pending work
  exists.

Safe vocabulary includes `signed in`, `signed out`, `switch user`, `resume`,
`saved locally`, `waiting to sync`, `needs sign-in to sync`, `sync before
switching`, `same user must sign in to sync`, `active user`, `active actor
session`, `actor partition`, and `sealed pending work` when used with the
meanings in the shared-device specification.

Product and UI copy must not say or imply:

- `securely erased`, `encrypted`, `retained for X days`, `no local copy`,
  `deleted from this device`, `recoverable by admin`, `remote wipe`, or
  equivalent retention/security promises;
- `tenant-isolated`, `workspace-isolated`, tenant switching, or multi-tenant
  authority;
- `production approved`, `safe for real production`, provider-approved,
  region-approved, compliance-approved, or support-backed;
- that one user can submit, transfer, recover, or view another user's pending
  work through login or switch UI.

## Implementation Successor Criteria

Before mobile implementation begins, the successor row or prompt should name:

- this specification as the selected behavior target;
- the accepted production auth principal-binding specification for
  `(issuer, subject) -> actor_id`;
- the accepted shared-device local-state specification for single-active-actor
  sessions, actor-local partitions, drain-or-seal switching, and safe
  vocabulary;
- the Product Candidate 1 product specification for one-Organization language
  and mobile field-work states;
- the exact mobile auth/session/sync files allowed for implementation;
- the focused mobile and server tests expected for acceptance;
- the stop conditions for NW-054, NW-093, NW-094, NW-095, NW-096, contracts,
  schemas, sync protocol behavior, and architecture authority.

Implementation may choose libraries, redirect URI details, token model
wrappers, and storage mechanics only within this specification and without
making retention/security or real-production claims.

## Expected Guard Tests

A successor implementation should prove at least:

- mobile login uses an external user-agent OIDC authorization-code plus PKCE
  boundary and does not use embedded web view or direct password collection;
- a provider login result does not activate a writable session until
  `/api/auth/me` returns a server-resolved actor;
- unbound, invalid, expired, wrong-issuer, wrong-audience, malformed, or
  otherwise rejected credentials create no writable actor session;
- IdP groups, roles, claims, profile data, and JWT `actor_id` do not grant
  actor, assignment, resolver, admin, or sync authority;
- refresh calls `/api/auth/me` and stops before actor-scoped push when the
  credential is unauthorized or resolves to a different actor;
- same-actor re-login resumes the same actor partition without changing
  event authorship;
- different-actor re-login follows drain-or-seal switching before the new
  actor can see or create actor-scoped local state;
- logout stops new actor work, prevents the next actor from seeing prior
  actor data, and seals undrained pending work;
- offline-before-first-login cannot create local-only writable actors or
  actor-authored events;
- offline-after-login keeps work in the active actor partition and does not
  claim server acceptance before sync;
- expired credentials block push, pull, config, subject-history, assignment,
  and conflict API calls until same-actor refresh or re-login succeeds;
- event assembly uses the active server-resolved actor id;
- bearer credentials and refresh material do not appear in event envelopes,
  payloads, config packages, sync JSON bodies, logs, or cross-actor mutable
  state;
- current contracts, schemas, envelope fields/types, sync watermarks, config
  package shapes, assignment payloads, BAR, CDL, operations evidence, and
  tenant-aware internals remain unchanged unless a separate route accepts a
  change.

Expected regression suites include mobile auth/session/sync tests around
`device_identity`, `sync_service`, and `event_store`, plus existing server
production-auth, sync, and shared-device related integration tests where the
successor touches those boundaries.

## Acceptance Evidence For This Decision

NW-085 acceptance is documentation-only evidence:

- this accepted platform specification is the durable decision/spec output;
- it is indexed from `docs/specifications/platform/README.md`;
- it composes accepted Product Candidate 1, production principal-binding, and
  shared-device/local-state specifications;
- it keeps NW-093 blocked for real-production approval and NW-054 as the
  retention/security route;
- it creates no runtime implementation, mobile UI, tests, contracts, schemas,
  operations evidence, BAR, CDL, old IDR text, or phase-file changes.

Implementation evidence is intentionally deferred to a successor row.

## Non-Goals

This specification does not authorize:

- runtime code, mobile UI, mobile token-storage implementation, tests,
  migrations, schemas, fixtures, contracts, sync protocol changes, event
  envelope fields, or event `type` values;
- IdP groups, roles, resource claims, custom claims, provider profile data, or
  JWT `actor_id` as platform authority;
- UI-selected actor authority, local-only writable actors, offline
  self-registration, local typed actor ids, or cross-actor request signing;
- bypassing `/api/auth/me` before actor-scoped push;
- reauthoring, transferring, cross-signing, or showing one actor's pending
  work as another actor's local work;
- local encryption, secure deletion, sealed-partition recovery, token/session
  retention duration, no-local-retention, redaction, erasure,
  decommissioning, remote wipe, or sensitivity handling;
- tenant-aware auth, tenant/workspace actor mappings, tenant selection,
  workspace-scoped config, tenant sync context, mobile tenant partition keys,
  pooled predicates, or bridge isolation;
- contract, config-package, assignment-payload, sync, watermark, reset, or
  process-boundary error-vocabulary changes;
- real-production approval, real users/data approval, provider/region/
  jurisdiction selection, support commitment, notification path, or
  compliance/security approval;
- reporting/export, conflict UI, resolver reassignment, batch conflict
  handling, auto-resolution, online principal-binding admin, new scopes, or
  production web-admin session behavior.

## Escalation Triggers

Route a successor before work depends on any of the following:

| Pressure | Route |
|---|---|
| Mobile login shape change, embedded web view request, native-broker-only requirement, or direct password collection | Product/security decision before implementation |
| Local encryption, secure deletion, retention duration, no-local-retention, erasure, redaction, device decommissioning, remote wipe, sealed-partition recovery, administrator recovery/export, token/session retention policy, or sensitivity treatment | NW-054 |
| Concrete provider, provider tenant/realm/client, region, jurisdiction, real users/data, support, notification, compliance/security review, or go/no-go approval | NW-093 |
| Managed-deployment control plane, per-deployment provider orchestration, SaaS lifecycle ownership, or fleet support boundary | NW-094 |
| Tenant-aware identity, membership, actor mapping, tenant/workspace selection, or principal across multiple tenants/workspaces | NW-095 |
| Tenant-aware storage, mobile partition keys, sync/config context, pooled predicates, bridge isolation, or isolation test harness | NW-096 |
| New actor authority primitive, IdP claim/group/role authority, UI-selected actor authority, local-only actor authority, new scope mechanism, or emergency write bypass | Architecture gap route before implementation |
| Event envelope, sync protocol, config-package schema, assignment payload, fixture, process-boundary error vocabulary, watermark, or reset-semantics change | Contract/architecture route before implementation |
| Reporting/export, broad audit/history reads, conflict UI, resolver reassignment, batch resolution, or auto-resolution | Owning product/platform route before implementation |

Stop rather than write around the issue if an implementation slice needs any
authority, contract, retention/security, tenant, operations, or real-production
behavior outside this accepted boundary.
