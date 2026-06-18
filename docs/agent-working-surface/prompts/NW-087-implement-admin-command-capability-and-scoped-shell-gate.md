# NW-087 Agent Prompt: Implement Admin Command Capability And Scoped Shell Gate

You are working in `/home/hamza/datarun-platform`.

## Current Standing

NW-086 is accepted: `/web-admin` now provides a separate browser login/session
boundary with OIDC/JWKS validation, server-managed session state, CSRF, logout,
expiry, principal-binding revalidation, secret-safe audit events, and a minimal
protected shell.

The current Thymeleaf `/admin`, `/admin/config`, and `/admin/dev` lanes remain
development-only. Do not productionize those controllers or templates.

## Goal

Implement the first server-side web/config admin command capability policy and
gate the production web-admin shell with it.

Exit target:

```text
An actor with a valid /web-admin session can enter the production web-admin
shell only when server-side deployment policy grants web_admin.access; denied
actors cannot enter the shell, and IdP claims/groups/roles/JWT actor_id remain
non-authority. Config-admin command names are represented and evaluable as
policy, but config authoring/publish workflows remain separate successors.
```

This is implementation work for command policy and shell access only. It is
not config UI productization, not assignment administration UX, not mobile
login, not tenant-aware architecture, not real-production approval, and not a
contract/schema change.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/implementation/module-interfaces.md` sections for authenticated actor,
   web-admin security/session, assignment-admin capability policy, and config
   packaging
5. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-079,
   NW-086, NW-087, NW-093, and NW-099
6. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
7. `docs/specifications/platform/assignment-scope-and-administration.md`
8. Relevant server auth/config/admin source and tests after reading the specs:
   - `server/src/main/java/dev/datarun/server/authorization/`
   - `server/src/main/java/dev/datarun/server/config/`
   - related tests under `server/src/test/java/dev/datarun/server/`

Open contracts only if you believe a process-boundary shape must change. The
expected route is no contract change. Stop before changing contracts unless a
successor route explicitly authorizes it.

## In Scope

Implement the minimal command capability gate:

- platform-owned command names:
  - `web_admin.access`
  - `web_admin.read_scoped`
  - `config_admin.author`
  - `config_admin.validate`
  - `config_admin.readiness_review`
  - `config_admin.approve`
  - `config_admin.publish`
- a deployment-configured server-side policy, likely following the existing
  `AssignmentAdminCapabilityPolicy` / `AssignmentAdminCapabilityService`
  pattern;
- validation for the policy shape and supported command vocabulary;
- command evaluation from the server-resolved `/web-admin` session actor
  context;
- `/web-admin/shell` denial unless `web_admin.access` is granted;
- secret-safe audit/logging for shell access granted/denied;
- focused tests proving denial, grant, claim non-authority, request-body/UI
  spoofing non-authority, and existing bearer API compatibility.

## Out Of Scope

Do not implement:

- config authoring, validation, readiness review, approval, publish UI, or
  config package publication behavior;
- assignment administration web UX;
- flag/conflict review UI;
- online production principal-binding administration APIs or UI;
- mobile OIDC/login, refresh/logout, secure storage, or shared-device login UX;
- tenant/workspace-aware auth, admin context, storage, sync, or config;
- reporting/export, broad audit/history reads, support reads, retention,
  local encryption, decommissioning, sealed recovery, new scopes, or real
  production approval.

## Required Behavior

- A valid `/web-admin` session alone is not enough to enter the shell.
- Shell entry requires `web_admin.access` from server-side deployment policy.
- The command policy must be platform-owned server state, not config-package
  content, not mobile-visible authority, not activity role-action config, and
  not IdP claims.
- IdP groups, roles, resource claims, custom claims, JWT `actor_id`,
  request-body actor IDs, UI-selected actors, and the fixed development admin
  actor are non-authority.
- `web_admin.access` grants no config, assignment, resolver, or data-read
  authority by itself.
- `web_admin.read_scoped` and `config_admin.*` must be represented/evaluable
  for successor handlers, but this slice should not build those handlers.
- Existing bearer-token APIs remain owned by `ActorTokenInterceptor`.
- Current development HTML lanes remain hidden in production.

## Expected Changed Surfaces

Expected implementation homes:

- server auth/config policy classes;
- `/web-admin` session controller/service shell gate;
- focused server tests;
- `docs/implementation/module-interfaces.md` only if the implemented module
  boundary changes.

Do not change `contracts/`, JSON schemas, migrations, BAR, CDL, operations
evidence, mobile code, product specs, or real-production standing.

## Expected Tests

Add or update focused server tests proving:

- unauthenticated browser requests still cannot reach the shell;
- authenticated session without `web_admin.access` cannot enter the shell;
- authenticated session with `web_admin.access` can enter the shell;
- request-body/UI actor spoofing cannot change command actor context;
- IdP claims/groups/roles/JWT `actor_id` do not grant shell access;
- invalid command names or malformed policy are rejected/fail closed;
- `web_admin.access` does not grant config-admin, assignment-admin, resolver,
  or data-read authority;
- existing `/api/**` bearer-auth paths still return their existing missing or
  invalid token behavior and no browser auth challenge;
- production development-surface containment remains active for `/admin`,
  `/admin/config`, `/admin/dev`, and dev token/bootstrap routes.

Run focused Maven tests you add or touch. Run broader auth/config/sync
regression tests if shared security, filter, policy, or controller
infrastructure changes.

## Verification

Required before commit:

```bash
git diff --check
```

Also report:

- exact Maven test command(s) run;
- whether contracts, schemas, migrations, BAR, CDL, operations docs, and
  mobile files had no diff;
- intentionally deferred config workflow/admin UI behavior.

## Stop Conditions

Stop and report before work that:

- uses IdP groups, roles, claims, JWT `actor_id`, request body, UI state, or a
  fixed dev actor as authority;
- implements config authoring/publish workflow or admin UI expansion;
- adds online production principal-binding admin;
- changes sync protocol, config-package schema, shape-format schema, flag
  catalog, event envelope fields, or event `type` vocabulary;
- changes assignment/scope authority, resolver equality, mobile auth/session,
  tenant/workspace context, storage isolation, reporting/export,
  retention/security, or production deployment approval.

## Commit Flow

Use one implementation commit:

```text
feat(auth): add web admin command gate
```

Use the NW trailer:

```text
NW: NW-087
```

Do not mark NW-087 accepted in the implementation commit. After tests pass,
record acceptance separately with a status/backlog commit.
