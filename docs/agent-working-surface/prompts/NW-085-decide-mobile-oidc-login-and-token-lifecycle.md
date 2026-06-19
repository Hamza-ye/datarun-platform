# NW-085 Agent Prompt: Decide Mobile OIDC/Login And Token Lifecycle

You are working in `/home/hamza/datarun-platform`.

## Product Candidate 1 Reason

NW-071 is accepted, so shared-device session and local-state behavior now has a
durable platform authority at
`docs/specifications/platform/shared-device-session-and-local-state.md`.

Product Candidate 1 pressure now includes external field users logging into the
mobile app. Before implementation agents build mobile login, token refresh,
logout, shared-device login copy, or token lifecycle behavior, Datarun needs a
bounded product/platform/security decision for the mobile authentication shape.

## Goal

Decide the external field-user mobile OIDC/login and token lifecycle boundary
for Product Candidate 1.

Exit target:

```text
Datarun has an accepted product/platform/security decision for mobile
field-user login, actor-session alignment, token lifecycle expectations,
shared-device login boundaries, offline behavior, and pre-implementation
evidence for Product Candidate 1.
```

This is decision/specification work. It is not runtime implementation, not
mobile UI implementation, not mobile token-storage implementation, not a
retention/security policy, not a tenant-aware architecture route, not a
contract/schema change, and not real-production approval.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing and Recommended next move
3. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-085 and
   adjacent NW-071, NW-054, NW-093, NW-094, NW-095, and NW-096
4. `docs/specifications/product/product-candidate-1.md`
5. `docs/specifications/platform/production-auth-principal-binding.md`
6. `docs/specifications/platform/shared-device-session-and-local-state.md`
7. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
   only for mobile-login non-goal context and separation from browser web-admin
   sessions
8. `docs/implementation/module-interfaces.md` sections for Authenticated Actor
   Resolver, Sync Surfaces, and Mobile Actor Session And Local Store
9. Existing mobile auth/session/sync implementation only as context if the
   decision depends on current behavior:
   - `mobile/lib/data/device_identity.dart`
   - `mobile/lib/data/sync_service.dart`
   - `mobile/lib/data/event_store.dart`

Use CDL slices only if the sources above expose a concrete authority conflict
or the selected model would introduce a new authority primitive. Do not read or
rewrite the whole CDL.

## Accepted Authority To Preserve

Use the accepted production principal-binding authority:

```text
(issuer, subject) -> actor_id
```

Required constraints:

- the server resolves the authenticated actor through an explicit active
  principal binding;
- a valid provider credential is necessary but not sufficient for platform
  access;
- `/api/auth/me` returns the server-resolved actor id that mobile must align
  with before actor-scoped work;
- IdP groups, IdP roles, resource claims, custom claims, JWT `actor_id`,
  request-body actor ids, UI-selected actor labels, product roles, and
  mobile-selected actors are not platform actor, assignment, resolver,
  admin, or sync authority.

Use the accepted shared-device/local-state specification:

- exactly one active actor session exists on a shared physical device;
- writable actor sessions require `/api/auth/me` actor resolution or a prior
  server-resolved same-actor session resume;
- actor switching follows drain-or-seal behavior;
- mutable local state, pending work, watermarks, cursors, config state, and
  token/session material are actor-local partitions;
- product copy must use product-safe vocabulary and must not imply erasure,
  encryption, retention duration, no-local-copy, tenant isolation, or real
  production safety unless a successor route accepts that behavior.

## Questions To Answer

1. Which Product Candidate 1 mobile login shape should be selected at the
   product/platform boundary:
   - mobile system browser or external user-agent OIDC authorization flow;
   - provider/native-app broker handoff if available;
   - embedded web view;
   - temporary bearer setup path retained only for internal or synthetic demos;
   - another bounded login shape that still resolves through explicit
     principal binding?
2. Which parts of provider selection belong to this decision, and which remain
   NW-093 real-production approval or operations commitments?
3. What are the minimum refresh, logout, re-login, and session-expiry
   expectations for a mobile actor session before implementation?
4. What token-storage posture can be stated now without overclaiming NW-054
   retention/security behavior? At minimum, decide the boundary for no tokens
   in events, logs, config packages, or cross-actor mutable state, while
   avoiding unaccepted promises about encryption, secure deletion, recovery,
   no-local-retention, retention duration, or sealed-partition recovery.
5. How does mobile align each login, refresh, resume, and push with
   `/api/auth/me` and the accepted server-resolved actor id?
6. What can the app do before first login, while offline before login, after a
   successful login, after token expiry, after logout, and during a shared
   device actor switch?
7. What is the shared-device login UX boundary for sign in, sign out, switch
   actor, same-actor resume, sealed pending work, and product-safe local-state
   language?
8. What evidence must exist before a successor implementation starts, and what
   focused tests or documentation checks should that successor prove?

## Expected Durable Output

Create the selected durable decision output under the home chosen by
`docs/documentation-organization.md`. The likely durable home is a platform or
product/platform security specification under `docs/specifications/`.

The durable output should include:

- document metadata required by `docs/documentation-organization.md`;
- selected mobile login shape and rejected alternatives;
- provider/browser/native-app boundary at product/platform level only;
- actor resolution and `/api/auth/me` alignment semantics;
- refresh, logout, re-login, and session-expiry expectations;
- token-storage posture at the level accepted for this route, with explicit
  NW-054 exclusions;
- offline behavior before first login, after login, during expiry, after
  logout, and during shared-device switching;
- shared-device login UX boundary and product-safe vocabulary constraints;
- explicit non-goals and deferred surfaces;
- acceptance evidence required before implementation;
- successor implementation prompt criteria and expected guard tests;
- stop conditions that require product/security, retention/security,
  contract, architecture, operations, or real-production escalation.

If the work cannot select a mobile login shape because product/security input
is missing, write a non-binding routing artifact under
`docs/agent-working-surface/artifacts/`, leave NW-085 unaccepted, and name the
missing input. Do not create a pseudo-decision.

## Guardrails

- Do not implement runtime code, mobile UI, mobile token storage, tests,
  migrations, schemas, fixtures, contracts, sync protocol behavior, event
  envelope fields, or event `type` values in this decision slice.
- Do not make IdP groups, roles, resource claims, custom claims, JWT
  `actor_id`, provider profile data, request-body actor ids, UI-selected
  actors, product personas, or Organization labels direct platform authority.
- Do not allow local-only writable actors, offline self-registration, or a
  locally typed actor id to create a writable actor session.
- Do not bypass `/api/auth/me` actor alignment before actor-scoped push.
- Do not reauthor, transfer, cross-sign, or show one actor's pending work as
  another actor's local work.
- Do not promise local encryption, secure deletion, sealed-partition recovery,
  token/session retention duration, no-local-retention, redaction, erasure,
  decommissioning, remote wipe, or sensitivity handling beyond accepted
  actor partitioning and sealed pending-work behavior. Coordinate NW-054 if
  those claims are needed.
- Do not add tenant-aware runtime, tenant/workspace actor mappings, tenant
  selection, workspace-scoped config, tenant sync context, mobile tenant
  partition keys, pooled predicates, or bridge isolation. Coordinate NW-094,
  NW-095, and NW-096 if those enter scope.
- Do not change contracts, sync/auth process boundaries, config-package
  schemas, assignment payloads, envelope fields/types, watermarks, reset
  semantics, or error vocabularies.
- Do not claim real-production approval, real users/data approval, provider,
  region, jurisdiction, notification, support, compliance/security review, or
  operations go/no-go. NW-093 remains blocked until those commitments become
  concrete.
- Do not include reporting/export, conflict UI, resolver reassignment, batch
  conflict handling, auto-resolution, online principal-binding admin, new
  scopes, or production web-admin session implementation.

## Stop And Escalate

Stop and report instead of writing around the issue if the decision needs any
of these:

- retention/security claims such as local encryption, secure deletion,
  retention duration, no-local-retention, erasure, redaction, decommissioning,
  remote wipe, sealed-partition recovery, token/session retention policy, or
  sensitivity treatment;
- tenant-aware partition keys, tenant-aware actor mappings, workspace-scoped
  config, tenant sync context, pooled storage predicates, or SaaS
  control-plane behavior;
- IdP claim, group, role, resource-claim, custom-claim, provider-profile, or
  JWT `actor_id` authority;
- UI-selected actor authority, local-only writable actors, offline
  self-registration, or cross-actor request signing;
- event envelope, sync protocol, config-package, assignment payload, schema,
  fixture, or process-boundary error vocabulary changes;
- real users, real organizational data, concrete production provider/region/
  jurisdiction choices, support commitments, notification paths, or
  compliance/security approval;
- broad audit/history access, reporting/export, conflict UI, resolver
  reassignment, batch resolution, auto-resolution, or emergency/special write
  bypasses.

## Verification

For the decision slice, run:

```bash
git diff --check
```

Also verify:

- the durable decision output is indexed from the nearest specifications
  README if a spec is created;
- the NW-085 backlog row links this prompt and records selected/accepted
  standing only after the decision output and checks are complete;
- `docs/status.md` keeps NW-093 blocked for real-production approval and
  NW-054 as the retention/security route;
- no runtime code, mobile files, tests, migrations, schemas, fixtures,
  contracts, BAR, CDL, operations evidence, old IDR text, or unrelated docs
  changed unless the prompt explicitly allows it.

## Commit Flow

If commits are requested, use separate commits for route, decision/spec, and
status acceptance when applicable. Include:

```text
NW: NW-085
```

Do not mark NW-085 accepted until the durable decision output, verification,
and required review are complete. Do not start mobile implementation until a
successor implementation row is selected after NW-085 acceptance.
