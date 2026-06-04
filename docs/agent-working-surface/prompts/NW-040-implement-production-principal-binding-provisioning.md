# NW-040 Agent Prompt: Implement Production Principal-Binding Provisioning

You are working in `/home/hamza/datarun-platform`.

## Goal

Implement the production principal-binding administration path selected by
IDR-028:

```text
Add deployment-managed manifest provisioning for explicit
(issuer, subject) -> actor_id bindings, with audited append-only operation
history, deterministic/idempotent application, and active binding lookup kept as
authentication support state only.
```

This is a server provisioning slice. It must not implement an online production
admin API/UI, mobile OIDC login UX, IdP group/claim authority, assignment
administration, resolver reassignment, auto-resolution, trigger execution, new
scope mechanisms, or event contract changes.

## Files To Read

Read only this bounded packet first:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-037 through NW-040.
4. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-104 only.
5. `docs/flagged-positions.md`
   - Read FP-011 only.
6. `docs/implementation/module-interfaces.md`
   - Read `Authenticated Actor Resolver`, `Scope Resolver`, and any admin/auth
     boundary section.
7. `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
8. `docs/decisions/idr-028-production-principal-binding-administration.md`
9. Current code/schema:
   - `server/src/main/resources/application.properties`
   - `server/src/main/resources/db/migration/V8__auth_principal_bindings.sql`
   - `server/src/main/java/dev/datarun/server/authorization/AuthProperties.java`
   - `server/src/main/java/dev/datarun/server/authorization/AuthenticatedActorResolver.java`
   - `server/src/main/java/dev/datarun/server/authorization/AuthPrincipalBindingRepository.java`
   - `server/src/main/java/dev/datarun/server/authorization/ActorTokenController.java`
   - `server/src/main/java/dev/datarun/server/authorization/ActorTokenInterceptor.java`
   - `server/src/test/java/dev/datarun/server/authorization/ProductionAuthIntegrationTest.java`
   - `server/src/test/java/dev/datarun/server/authorization/LocalJwtAuthCompatibilityIntegrationTest.java`
10. CDL slices by command, not broad file reading:
    - `python3 scripts/query_cdl.py --id CDL-018`
    - `python3 scripts/query_cdl.py --id CDL-030`
    - `python3 scripts/query_cdl.py --id CDL-031`
    - `python3 scripts/query_cdl.py --id CDL-032`
    - `python3 scripts/query_cdl.py --id CDL-034`
    - `python3 scripts/query_cdl.py --id CDL-035`
    - `python3 scripts/query_cdl.py --id CDL-055`

Open other files only when these routes name them.

## Authority And Guardrails

- IDR-027 is active: a validated authentication principal maps through explicit
  `(issuer, subject) -> actor_id` binding.
- IDR-028 is active: the first production administration path is
  deployment-managed binding provisioning with append-only operation history and
  active lookup projection/support rows.
- Bindings are authentication lookup/support state. They are not assignment
  scope, work-action authority, assignment-admin authority, conflict resolver
  designation, deployment admin authority, or event authority beyond selecting
  the single authenticated actor for existing actor-bound endpoints.
- Assignment history, scope axes, role-action config, and IDR-026 resolver rules
  remain the only platform authority sources.
- JWT `actor_id`, groups, realm roles, client roles, resource claims, and custom
  IdP claims remain non-authority.
- `/api/actors/**` remains development-token administration only and must stay
  disabled outside `dev-token` mode.

## Expected Implementation Boundary

Implement a deployment-managed manifest provisioning mechanism for
`auth_principal_bindings`.

Expected behavior:

- Define a deterministic, reviewable manifest format. JSON is acceptable if it
  fits existing server conventions; do not introduce a broad config system for
  this slice.
- Add server configuration for the manifest source and applied-by/system
  identity if needed.
- Add schema support for audited append-only binding operations. The current
  `auth_principal_bindings` table may remain the active lookup projection or
  support row table, but production administration authority must be the audited
  operation history plus active projection, not unaudited SQL updates.
- Validate the full manifest before application. Reject duplicate desired
  active mappings for the same `(issuer, subject)`, malformed actor UUIDs,
  missing operation id/version, missing reason, missing issuer/subject, and
  ambiguous operations.
- Apply each manifest transactionally and serialize concurrent applications
  using a database lock, advisory lock, or equivalent repository-level guard.
- Support create, rotate, deactivate, explicit rebind/correction, and bootstrap
  semantics from IDR-028.
- Preserve idempotency: reapplying the same manifest must not duplicate
  effective operations or change the active state.
- Retain operation audit metadata required by IDR-028, including issuer,
  subject, target actor, desired state, operation id or manifest version,
  manifest source identity, content hash, applied timestamp, applied-by
  identity, reason, previous active binding id, and previous actor when
  applicable.
- Keep `AuthenticatedActorResolver` resolving only active explicit
  `(issuer, subject)` bindings for both `jwt` and `oidc-jwks` modes.
- Keep test setup helpers from becoming the production authority path. If tests
  need direct binding setup, name it clearly or route them through the
  provisioning runner where the test is proving production behavior.

Implementation surface guidance:

- Prefer a small authorization package service/repository pair over spreading
  manifest logic through controllers or tests.
- A command-line runner, startup-disabled service method, or explicit
  deployment-run component is acceptable if it stays server-side and testable.
- Do not add an HTTP admin endpoint unless a new decision defines production
  binding-admin authority first.

## Expected Tests

Add focused server tests proving:

- Bootstrap applies a manifest and lets a valid provider principal authenticate
  only through the explicit active binding.
- Create, rotate, deactivate, and explicit rebind/correction produce the active
  lookup behavior defined by IDR-028.
- Reapplying the same manifest is idempotent and does not duplicate effective
  audit operations.
- Invalid manifests reject before partial application.
- Concurrent/conflicting application is serialized or rejected without leaving
  inconsistent active bindings.
- Audit metadata is retained, including previous binding/actor details when a
  rebind or deactivation changes active state.
- `AuthenticatedActorResolver` still rejects unmapped or inactive principals.
- JWT `actor_id`, groups, roles, resource claims, and custom IdP claims do not
  grant pull visibility, subject-history visibility, assignment create/end,
  conflict resolution, or binding administration authority.
- `/api/actors/**` remains development-token administration only.
- Existing local JWT and OIDC/JWKS compatibility tests still pass.

Run at minimum:

```bash
cd server
./mvnw -Dtest=ProductionAuthIntegrationTest,LocalJwtAuthCompatibilityIntegrationTest test
./mvnw -Dtest=ProductionAuthIntegrationTest,LocalJwtAuthCompatibilityIntegrationTest,SyncControllerIntegrationTest,ScopeFilteredSyncIntegrationTest,AssignmentContainmentIntegrationTest,ConflictResolutionIntegrationTest,SubjectHistoryBackfillIntegrationTest test
./mvnw test
```

Run mobile tests only if you touch mobile code. If mobile is touched, run:

```bash
cd mobile
flutter test test/event_assembler_test.dart test/sync_service_test.dart
flutter test
```

## Forbidden Work

- No envelope fields or event `type` changes.
- No `user_id`, `account_id`, `group_id`, realm-role, client-role, resource
  claim, or equivalent IdP fields in event envelopes or platform payloads.
- No direct group/claim/platform-admin authority.
- No production binding-admin API or UI in this slice.
- No mobile OIDC login UX or refresh-token storage.
- No shared-device or multi-actor session switching.
- No assignment-admin model changes, resolver reassignment, auto-resolution,
  trigger execution, or new scope mechanisms.
- No hard deletes or historical event rewrites for binding correction.
- No tests that require a live network identity provider.

## Documentation Updates

After implementation and passing tests, update:

- `docs/status.md` Current Routing and What's Next.
- `docs/agent-working-surface/platform-next-work-backlog.md` NW-040 evidence.
- `docs/implementation/module-interfaces.md` if module boundaries or owned
  storage changed.
- `docs/flagged-positions.md` FP-011 only if the NW-040 runtime gate genuinely
  resolves the remaining production-auth concern. If any group/claim authority
  question remains, route that as a successor decision instead of silently
  closing the FP.
- `docs/agent-working-surface/baseline-acceptance-register.md` BAR-104 only if
  FP-011 is resolved and full runtime evidence supports acceptance.

## Commit Boundary

One implementation commit is expected if tests pass, for example:

```text
feat(auth): add principal binding provisioning
```

Do not combine with mobile login UX, online admin APIs, group/claim authority,
or unrelated baseline cleanup.

## Stop And Report

Stop and report if:

- Product requires IdP group/role/claim membership to grant authority directly.
- The implementation pressure points toward an online production admin API.
- Correcting a wrong binding appears to require rewriting events or changing
  historical `actor_ref`.
- The work appears to require new envelope fields, new event types, a new scope
  mechanism, resolver reassignment, auto-resolution, or mobile actor switching.
- Current docs, contracts, and code disagree about whether a token claim may be
  platform authority.
