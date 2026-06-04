# NW-038 Agent Prompt: Implement OIDC/JWKS Auth Provider Boundary

You are working in `/home/hamza/datarun-platform`.

## Goal

Implement the next production-auth slice after NW-037:

```text
Add a real OIDC/JWKS token-validation mode behind the existing authenticated
actor resolver, while keeping explicit (issuer, subject) -> actor_id binding as
the only platform actor mapping and keeping groups/roles/claims non-authority.
```

This is a server auth-provider boundary slice. It must not implement mobile OIDC
login UX, shared-device actor switching, principal-binding administration, new
scope mechanisms, resolver reassignment, auto-resolution, or event contract
changes.

## Files To Read

Read only this bounded packet first:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-037, NW-038, and NW-039.
4. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-104 only.
5. `docs/flagged-positions.md`
   - Read FP-011 only.
6. `docs/implementation/module-interfaces.md`
   - Read `Scope Resolver` and `Authenticated Actor Resolver`.
7. `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
8. `docs/agent-working-surface/prompts/NW-035-evaluate-production-auth-fp-011-successor-phase.md`
   - Use as historical design context only.
9. `contracts/sync-protocol.md`
   - Read push/pull/subject-history auth sections only.
10. CDL slices by command, not broad file reading:
    - `python3 scripts/query_cdl.py --id CDL-006`
    - `python3 scripts/query_cdl.py --id CDL-018`
    - `python3 scripts/query_cdl.py --id CDL-030`
    - `python3 scripts/query_cdl.py --id CDL-031`
    - `python3 scripts/query_cdl.py --id CDL-032`
    - `python3 scripts/query_cdl.py --id CDL-034`
    - `python3 scripts/query_cdl.py --id CDL-035`
    - `python3 scripts/query_cdl.py --id CDL-055`
11. Current code paths:
    - `server/pom.xml`
    - `server/src/main/resources/application.properties`
    - `server/src/main/java/dev/datarun/server/authorization/AuthProperties.java`
    - `server/src/main/java/dev/datarun/server/authorization/AuthenticatedActorResolver.java`
    - `server/src/main/java/dev/datarun/server/authorization/JwtPrincipalTokenValidator.java`
    - `server/src/main/java/dev/datarun/server/authorization/AuthPrincipalBindingRepository.java`
    - `server/src/main/java/dev/datarun/server/authorization/ActorTokenInterceptor.java`
    - `server/src/main/java/dev/datarun/server/authorization/AuthMeController.java`
    - `server/src/test/java/dev/datarun/server/authorization/ProductionAuthIntegrationTest.java`

Open other files only when these routes name them.

## Authority And Guardrails

- IDR-027 is active: validated authentication principal maps through explicit
  `(issuer, subject) -> actor_id`.
- FP-011 remains open. This slice may satisfy the live provider-validation part
  of FP-011, but it does not close FP-011 or accept BAR-104 by itself because
  operational binding administration/provisioning remains NW-039.
- `actor_ref` remains event authorship only. Do not add account, user, group, or
  IdP fields to the event envelope or platform payload schemas.
- Groups, realm roles, client roles, resource claims, and JWT `actor_id` claims
  must not grant sync visibility, work-action authority, assignment-admin
  authority, or conflict-resolution authority.
- Assignment history, scope axes, role-action config, and IDR-026 resolver rules
  remain the only platform authority sources.

## Expected Implementation Boundary

Implement a new production token-validation path behind
`AuthenticatedActorResolver`, preferably as a new auth mode such as
`oidc-jwks` while preserving existing `dev-token` and NW-037 local `jwt` mode.

Expected behavior:

- Validate asymmetric provider JWTs using configured issuer, audience, and JWKS
  URI.
- Require a verifiable signature and reject unsupported or unsafe algorithms.
- Validate `iss`, `sub`, `aud`, `exp`, and `nbf` according to normal OIDC/JWT
  expectations.
- Resolve only `(iss, sub)` through `auth_principal_bindings`.
- Return an authenticated actor context with a source name that distinguishes
  OIDC/JWKS from dev-token and local HS256 test mode.
- Keep `/api/auth/me`, sync push/pull/subject-history/config, assignments, and
  conflicts using the same resolved actor context.
- Update `application.properties`, IDR-027, module/status/backlog evidence only
  after implementation and tests pass.

Dependency guidance:

- Use a proven JOSE/JWT library rather than hand-rolling JWKS and RSA signature
  verification.
- Keep dependency scope tight. Avoid installing a broad Spring Security filter
  chain unless you can prove it does not bypass or duplicate the existing
  `ActorTokenInterceptor`/assignment-derived authority model.
- Tests must use local fixtures or an in-process JWKS server, not a live
  external Keycloak dependency.

## Expected Tests

Add focused server tests proving:

- Valid OIDC/JWKS token resolves through `auth_principal_bindings`.
- Missing bearer, malformed token, bad signature, unknown `kid`, wrong issuer,
  wrong audience, expired token, not-yet-valid token, and unsupported algorithm
  reject before push persistence.
- Unmapped valid principal rejects as `invalid_token` and persists no pushed
  event.
- Groups/roles/resource claims/JWT `actor_id` remain non-authority for:
  - pull visibility;
  - subject-history backfill;
  - assignment create/end;
  - canonical conflict resolution.
- Existing NW-037 local JWT and dev-token compatibility behavior still passes.

Run at minimum:

```bash
cd server
./mvnw -Dtest=ProductionAuthIntegrationTest test
./mvnw -Dtest=ProductionAuthIntegrationTest,SyncControllerIntegrationTest,ScopeFilteredSyncIntegrationTest,AssignmentContainmentIntegrationTest,ConflictResolutionIntegrationTest,SubjectHistoryBackfillIntegrationTest test
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
- No `user_id`, `account_id`, `group_id`, realm-role, client-role, or
  equivalent IdP fields in event envelopes or platform payloads.
- No group/claim/platform-admin authority.
- No production binding-admin API or UI in this slice.
- No mobile OIDC login UX or refresh-token storage in this slice.
- No shared-device or multi-actor session switching.
- No resolver reassignment, auto-resolution, trigger execution, or new scope
  mechanisms.
- No tests that require a live network identity provider.

## Commit Boundary

One implementation commit is expected if tests pass, for example:

```text
feat(auth): add oidc jwks principal validation
```

Do not combine with binding administration, mobile login UX, or unrelated
baseline cleanup.

## Stop And Report

Stop and report if:

- Product requires IdP group/role/claim membership to grant authority directly.
- A real provider integration cannot be tested without live external services.
- A library choice forces a broad security filter chain that bypasses current
  actor/assignment semantics.
- The implementation seems to require new envelope fields, new event types, a
  new scope mechanism, binding administration, or shared-device session choice.
- Current docs, contracts, and code disagree about whether a token claim may be
  platform authority.
