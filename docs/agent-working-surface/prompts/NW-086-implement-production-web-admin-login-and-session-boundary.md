# NW-086 Agent Prompt: Implement Production Web Admin Login And Session Boundary

You are working in `/home/hamza/datarun-platform`.

## Goal

Implement the first production web admin login/session boundary for Product
Candidate 1.

Exit target:

```text
Production-mode web admin has browser OIDC/JWKS login, server-managed session,
CSRF/logout/expiry behavior, principal-binding revalidation, admin actor
context, login/session audit, and a minimal protected shell without exposing
development admin authority.
```

This is implementation work for login/session only. It is not config UI
productization, not admin command policy, not assignment administration, not
mobile login, not tenant-aware architecture, not real-production approval, and
not a contract/schema change.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/implementation/module-interfaces.md` sections for auth, web/admin,
   config/admin, and sync/auth surfaces
5. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-079,
   NW-084, NW-086, NW-087, and NW-093
6. `docs/specifications/product/product-candidate-1.md`
7. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
8. `docs/specifications/platform/production-auth-principal-binding.md`
9. `docs/specifications/platform/assignment-scope-and-administration.md`
10. Relevant server auth/admin source and tests after reading the specs:
    - `server/src/main/java/dev/datarun/server/authorization/`
    - `server/src/main/java/dev/datarun/server/admin/`
    - `server/src/main/java/dev/datarun/server/config/`
    - `server/src/main/java/dev/datarun/server/ops/`
    - related server tests under `server/src/test/java/dev/datarun/server/`

Open contracts only if you believe a process-boundary shape must change. The
expected route is no contract change. Stop before changing contracts unless a
successor route explicitly authorizes it.

## In Scope

Implement the minimal production web admin session boundary:

- browser OIDC/JWKS login initiation and callback;
- server-side validation through the accepted production-auth provider
  boundary;
- explicit active `(issuer, subject) -> actor_id` binding before session
  creation;
- server-managed session cookie with secure attributes suitable for production
  profile;
- server-side session state with actor id, issuer, subject, login time, expiry,
  auth source, and secret-safe correlation metadata;
- principal-binding revalidation before protected admin actions;
- logout and session invalidation;
- session expiry denial before mutation;
- CSRF protection for state-changing admin requests;
- secret-safe login/session audit or operation history;
- a minimal protected web admin shell that proves authenticated entry only;
- preservation of production development-surface containment for dev token,
  bootstrap, and fixed-development-actor surfaces.

## Out Of Scope

Do not implement:

- `web_admin.access`, `web_admin.read_scoped`, or `config_admin.*` command
  policy. That is NW-087.
- config authoring, validation, readiness review, approval, publish UI, or
  config package changes;
- assignment administration web UX;
- conflict/flag review UI;
- online principal-binding administration APIs or UI;
- mobile OIDC/login, refresh/logout, secure storage, or shared-device login UX;
- tenant/workspace-aware auth, admin context, storage, sync, or config;
- reporting/export, broad audit/history reads, support reads, retention,
  local encryption, decommissioning, sealed recovery, new scopes, or real
  production approval.

## Required Behavior

Use the accepted NW-079 platform spec as the behavioral target:

- no browser request may supply an actor id that overrides the server-resolved
  actor;
- invalid, expired, wrong-issuer, wrong-audience, malformed, unsupported,
  unmapped, or deactivated principals fail before session creation or action;
- IdP groups, roles, resource claims, custom claims, JWT `actor_id`,
  request-body actor IDs, UI-selected actors, and the fixed dev admin actor are
  non-authority;
- session id by itself grants no platform authority;
- protected admin actions use the session's server-resolved actor context;
- state-changing admin requests require CSRF protection;
- logout, expiry, and binding deactivation stop action before mutation;
- audit/logging must not retain bearer tokens, provider tokens, session
  cookies, passwords, private keys, or full sensitive request bodies.

The shell can be minimal. It only needs to prove authenticated entry and safe
denial. Command-gated admin screens remain hidden or unavailable until NW-087.

## Expected Changed Surfaces

Expected implementation homes are server auth/session/admin code and focused
server tests. Update implementation-boundary docs only if the implementation
changes a module contract that future agents need to know.

Do not change `contracts/`, JSON schemas, migrations, BAR, CDL, operations
evidence, mobile code, or Product Candidate 1 scope for NW-086 unless a stop
condition routes a separate successor first.

## Expected Tests

Add or update focused server tests proving:

- unauthenticated browser requests cannot reach production admin shell/actions;
- login rejects invalid, expired, wrong-issuer, wrong-audience, malformed, and
  unmapped principals;
- login creates a session only for an explicitly bound active principal;
- protected shell resolves the same actor from server session state;
- request-body or UI actor spoofing cannot change actor context;
- IdP groups/roles/claims and JWT `actor_id` do not grant web admin access;
- logout invalidates the session;
- session expiry denies protected access;
- principal-binding deactivation or rebind invalidates or denies the existing
  session before admin action;
- state-changing requests require CSRF;
- production development-surface containment remains active for dev token and
  bootstrap routes.

Run the focused Maven tests you add or touch. Run the broader relevant server
test subset if shared auth, filter, or controller infrastructure changes.

## Verification

Required before commit:

```bash
git diff --check
```

Also report:

- exact Maven test command(s) run;
- whether contracts, schemas, migrations, BAR, CDL, operations docs, and
  mobile files had no diff;
- any intentionally deferred NW-087 command-gate behavior.

## Stop Conditions

Stop and report before work that:

- uses IdP groups, roles, claims, JWT `actor_id`, request body, UI state, or
  fixed dev actor as authority;
- implements admin command policy or config UI expansion inside NW-086;
- adds online production principal-binding admin;
- changes sync protocol, config-package schema, shape-format schema, flag
  catalog, event envelope fields, or event `type` vocabulary;
- changes assignment/scope authority, resolver equality, mobile auth/session,
  tenant/workspace context, storage isolation, reporting/export,
  retention/security, or production deployment approval.

## Commit Flow

Use one implementation commit:

```text
feat(auth): add production web admin session boundary
```

Use the NW trailer:

```text
NW: NW-086
```

Do not mark NW-086 accepted in the implementation commit. After tests pass,
record acceptance separately with a status/backlog commit.
