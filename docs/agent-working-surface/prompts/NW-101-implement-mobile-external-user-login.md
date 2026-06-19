# NW-101 Agent Prompt: Implement Mobile External Field-User Login

You are working in `/home/hamza/datarun-platform`.

## Owning Row

NW-101 in `docs/agent-working-surface/platform-next-work-backlog.md`.

This is implementation work only. It is not acceptance, real-production
approval, a retention/security policy, a tenant-aware route, or a contract
change.

## Current Standing

NW-084, NW-071, NW-070, and NW-085 are accepted. Product Candidate 1 now has
durable product language, shared-device local-state behavior, production
principal binding, and mobile OIDC/token lifecycle authority.

NW-093 remains blocked for real users/data, provider, region, jurisdiction,
support, compliance/security, and go/no-go approval. NW-054 remains separate
for retention/security claims beyond NW-085 token-placement limits. NW-094
through NW-098 remain separate for managed control-plane and tenant-aware
runtime/storage/sync work.

## Agent Roles

- Primary: Mobile App Builder.
- Backend Architect: involve only if implementation discovers a necessary
  server API, auth contract, callback, or configuration change. Stop before
  making that change unless the route is explicitly expanded.
- API Tester: involve for verification if real code touches the mobile/server
  callback, bearer auth, `/api/auth/me`, or sync/auth path.

## Goal

Implement Product Candidate 1 mobile external field-user login with:

```text
system browser or external user-agent OIDC authorization-code flow with PKCE
-> optional OS/provider native-app broker handoff when available
-> bearer credential presented to Datarun APIs
-> GET /api/auth/me resolves the server actor
-> one active server-resolved actor session
-> actor-local mutable partition for mobile work and sync state
```

Exit target:

```text
A mobile field user can sign in through the accepted external-user-agent
OIDC + PKCE boundary, resolve the platform actor through /api/auth/me, and
continue sync/capture under the accepted shared-device actor partition rules.
Refresh, logout, re-login, expiry, and actor switching stop or resume work
only through same-actor server resolution, with focused mobile evidence.
```

Keep the slice narrow. The temporary manual bearer setup path may remain only
for internal development, automated tests, and synthetic demonstrations; it
must not be presented as the external field-user Product Candidate 1 login.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing and What's Next
3. `docs/commit-workflow.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-071,
   NW-085, NW-101, NW-093, NW-054, and NW-094 through NW-098
5. `docs/specifications/product/product-candidate-1.md`
6. `docs/specifications/platform/mobile-oidc-login-and-token-lifecycle.md`
7. `docs/specifications/platform/production-auth-principal-binding.md`
8. `docs/specifications/platform/shared-device-session-and-local-state.md`
9. `docs/implementation/module-interfaces.md` sections for Authenticated
   Actor Resolver, Sync Surfaces, and Mobile Actor Session And Local Store
10. `docs/agent-working-surface/operational-ux-layering-companion.md` only if
    editing login/session/switching labels or navigation; use it for
    vocabulary guardrails, not authority
11. Mobile implementation and tests, capped to the touched surface:
    - `mobile/pubspec.yaml`
    - `mobile/lib/main.dart`
    - `mobile/lib/data/device_identity.dart`
    - `mobile/lib/data/sync_service.dart`
    - `mobile/lib/data/event_store.dart`
    - `mobile/lib/data/event_assembler.dart`
    - `mobile/lib/presentation/app_state.dart`
    - `mobile/lib/presentation/screens/setup_screen.dart`
    - `mobile/lib/presentation/screens/work_list_screen.dart`
    - `mobile/lib/presentation/widgets/sync_panel.dart`
    - `mobile/test/sync_service_test.dart`
    - `mobile/test/event_assembler_test.dart`
    - `mobile/test/work_readiness_test.dart`
    - `mobile/test/sync_panel_test.dart`

Open contracts only if you believe a process-boundary contract must change.
The expected route is no contract change.

Open NW-044 through NW-046 only if implementation pressure starts touching
reporting/export, conflict automation/batch resolution, or generic flag/pattern
traversal reporting. Those topics are forbidden in this row.

## Authority And Guardrails

- Mobile login uses the accepted system browser/external-user-agent
  authorization-code + PKCE boundary. Do not use embedded web view login,
  direct password collection, or manual token paste as the external field-user
  login shape.
- Native/provider broker handoff is optional when available. It must return
  through the same provider credential and `/api/auth/me` actor-resolution
  path.
- A provider credential is necessary but not sufficient. Platform actor
  authority comes only from explicit active `(issuer, subject) -> actor_id`
  binding resolved by the server.
- The app must not activate writable actor-local work until `/api/auth/me`
  returns a server-resolved actor id for the credential.
- Groups, roles, claims, profile data, JWT `actor_id`, request-body actor IDs,
  UI-selected actors, product personas, Organization labels, and local session
  labels are not actor, assignment, resolver, admin, or sync authority.
- Refresh must call `/api/auth/me` and verify the resolved actor still matches
  the active local session before actor-scoped push or session continuation.
- Same-actor re-login may resume the same actor partition. Different-actor
  login is an actor switch and must follow drain-or-seal behavior.
- Logout stops new capture, advisory work, background sync, config fetch, and
  subject-history scheduling for the active actor. Undrained pending work is
  sealed for same-actor resume or a future accepted recovery route.
- Bearer credentials, refresh material, authorization codes, provider secrets,
  passwords, session cookies, and private keys must not appear in event
  envelopes, payloads, config packages, sync JSON bodies, query strings, logs,
  diagnostics, or cross-actor mutable state.
- Token/session storage mechanics may be implemented only inside NW-085's
  placement limits. Do not claim encryption, secure deletion, retention
  duration, no-local-retention, erasure, redaction, remote wipe,
  decommissioning, sealed-partition recovery, or sensitivity treatment.
- Login/session/actor-switching copy and navigation polish is allowed only
  where it makes this flow usable and product-safe. Do not route a broad mobile
  redesign in this row.

## In Scope

- Add a mobile auth handoff abstraction for external user-agent
  authorization-code + PKCE login, testable without a real production
  provider.
- Add the mobile login initiation and callback/completion path needed by that
  abstraction.
- Resolve the provider credential through `/api/auth/me` before activating an
  actor session.
- Store and use token/session material only as actor-local session material
  under the accepted shared-device partition boundary.
- Implement refresh, logout, re-login, expiry handling, and switch-user flows
  to the minimum accepted Product Candidate 1 behavior.
- Keep manual bearer setup available only as internal/test/synthetic setup if
  still needed, with product copy that does not present it as external-user
  login.
- Add narrowly scoped login/session/switching UI vocabulary or navigation
  polish where required for the flow.

## Out Of Scope

Do not implement:

- real-production approval, real users/data approval, provider/region/
  jurisdiction selection, support commitment, notification path, or
  compliance/security approval;
- local encryption, secure deletion, token/session retention duration,
  no-local-retention, erasure, redaction, device decommissioning, remote wipe,
  sealed-partition recovery, administrator recovery/export, or sensitivity
  treatment;
- tenant-aware auth, tenant/workspace actor mappings, tenant selection,
  workspace-scoped config, tenant sync context, mobile tenant partition keys,
  pooled predicates, bridge isolation, or a tenant isolation harness;
- event envelope fields, envelope `type` values, sync protocol changes,
  config-package schema changes, assignment payload changes, fixtures,
  migrations, watermark/reset semantics, or process-boundary error vocabulary
  changes;
- IdP group/claim/role authority, provider-profile authority, UI-selected
  actor authority, local-only writable actors, offline self-registration, or
  cross-actor request signing;
- reauthoring, transferring, cross-signing, or showing one actor's pending
  work as another actor's local work;
- reporting/export, broad audit/history reads, conflict UI, conflict
  automation, batch resolution, resolver reassignment, auto-resolution, online
  principal-binding admin, new scopes, or production web-admin session changes;
- broad mobile redesign outside login/session/actor-switching usability.

## Expected Tests

Add or update focused Flutter/unit/widget tests proving:

- external field-user login goes through an external-user-agent
  authorization-code + PKCE handoff abstraction and does not depend on embedded
  web view login, direct password collection, or manual token paste;
- provider login result does not activate a writable actor session until
  `/api/auth/me` returns a server-resolved actor id;
- unbound, invalid, expired, malformed, unauthorized, wrong-actor, or missing
  actor responses create no writable actor session;
- refresh calls `/api/auth/me`, resumes only for the same actor, and stops
  before actor-scoped push when the credential is unauthorized or resolves to a
  different actor;
- logout stops actor work and prevents the next actor from seeing, pushing, or
  reusing prior-actor local state;
- same-actor re-login resumes the same actor partition without changing event
  authorship;
- different-actor re-login follows drain-or-seal switching before the new
  actor can see or create actor-scoped local state;
- expired credentials block push, pull, config, subject-history, assignment,
  and conflict API calls until same-actor refresh or re-login succeeds;
- sealed prior-actor pending work remains pushable only by the same resolved
  actor;
- bearer credentials, refresh material, authorization codes, and provider
  secrets do not appear in event envelopes, payloads, config packages, sync
  JSON bodies, query strings, cross-actor mutable state, or log-visible error
  messages as feasible in mobile tests;
- login/session/switching copy uses accepted vocabulary such as `Sign in`,
  `Sign out`, `Switch user`, `saved locally`, `waiting to sync`, and `needs
  sign-in to sync` without implying deletion, encryption, tenant isolation, or
  real-production safety.

Expected mobile regression coverage:

```bash
flutter test test/sync_service_test.dart test/event_assembler_test.dart test/work_readiness_test.dart test/sync_panel_test.dart
```

Run new focused auth/login tests. Run full `flutter test` before acceptance
when practical, especially if shared app state, navigation, or session storage
changes.

Run relevant existing server auth/sync tests only if server code or server
configuration is touched. Likely server suites then include
`ProductionAuthIntegrationTest`, `LocalJwtAuthCompatibilityIntegrationTest`,
`SyncControllerIntegrationTest`, and `ProductionDevelopmentSurfaceFilterTest`,
plus any new server tests for touched callback/config behavior.

## Verification

Before any implementation commit:

```bash
git diff --check
```

Also report:

- exact Flutter and, if applicable, Maven commands run;
- whether contracts, schemas, sync protocol, event envelope, fixtures,
  migrations, BAR, CDL, operations evidence, tenant/workspace routes,
  reporting/export routes, and real-production standing had no diff;
- any intentionally deferred provider-specific configuration, richer mobile
  navigation, native broker integration depth, secure-storage posture, or
  broad mobile redesign.

## Acceptance Boundary

NW-101 is not accepted by opening this prompt or by implementing code alone.
Acceptance requires implementation evidence, focused tests, verification, and a
separate status/backlog acceptance update after review.

This row can accept only Product Candidate 1 mobile external field-user login
within the accepted NW-085, NW-071, NW-070, and NW-084 boundaries. It cannot
accept NW-093 real-production approval, NW-054 retention/security behavior,
NW-094 through NW-098 tenant/control-plane behavior, reporting/export,
conflict automation, or contract/schema changes.

## Stop And Report

Stop before implementation if the slice needs:

- a concrete production provider, provider tenant/realm/client, provider
  region, jurisdiction, support commitment, notification path, real users,
  real organizational data, compliance/security review, or go/no-go approval;
- local encryption, secure deletion, token/session retention duration,
  no-local-retention, erasure, redaction, decommissioning, remote wipe,
  sealed-partition recovery, administrator recovery/export, or sensitivity
  treatment;
- tenant-aware actor mappings, tenant/workspace selection, workspace-scoped
  config, tenant sync context, mobile tenant partition keys, pooled predicates,
  bridge isolation, managed-deployment control-plane behavior, or tenant
  isolation tests;
- IdP group, role, claim, resource-claim, custom-claim, provider-profile, JWT
  `actor_id`, UI-selected actor, local session label, or request body authority;
- local-only writable actors, offline self-registration, embedded provider
  credential entry, direct provider password collection, or cross-actor request
  signing;
- event envelope, sync protocol, config-package, assignment payload, schema,
  fixture, migration, watermark/reset, or process-boundary error vocabulary
  changes;
- reporting/export, broad audit/history access, conflict UI, resolver
  reassignment, batch resolution, auto-resolution, online principal-binding
  admin, new scopes, emergency/special write bypasses, or production web-admin
  session changes.

## Commit Flow

Only commit if the task explicitly authorizes commits.

Use the routing commit if this row and prompt are not already landed:

```text
docs(mobile): route external field-user login

NW: NW-101
```

Use one or more implementation/test commits as needed, starting with:

```text
feat(mobile): add external field-user login

NW: NW-101
```

Do not mark NW-101 accepted in an implementation commit. After verification and
review, record acceptance separately with a status/backlog commit such as:

```text
docs(status): accept mobile external field-user login

NW: NW-101
```
