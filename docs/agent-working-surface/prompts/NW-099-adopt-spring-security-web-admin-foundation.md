# NW-099 Agent Prompt: Adopt Spring Security Web Admin Foundation

You are working in `/home/hamza/datarun-platform`.

## Goal

Adopt the Spring Security foundation needed for production web-admin work
without implementing the web-admin login/session product slice yet.

Exit target:

```text
The server can include Spring Security and OAuth2 client support without
regressing existing bearer-token APIs, development-surface containment,
production runtime validation, or current tests.
```

This is a framework-foundation implementation slice. It exists because a
2026-06-18 NW-086 attempt to add Spring Security/OAuth2 plus full web-admin
login/session behavior in one patch was stopped as too broad. No code from
that attempt was accepted.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/implementation/module-interfaces.md` auth, web/admin, ops, and sync
   auth sections as needed
5. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-079,
   NW-084, NW-086, NW-087, NW-093, and NW-099
6. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
7. `docs/specifications/platform/production-auth-principal-binding.md`
8. `docs/specifications/product/product-candidate-1.md`
9. Relevant server auth/admin/ops source and tests

Open contracts only if you believe a process-boundary shape must change. The
expected route is no contract change.

## In Scope

Implement only the non-user-facing Spring Security foundation:

- add Spring Security and OAuth2 client dependencies needed by the future
  browser-admin route;
- configure security chains so existing non-browser API behavior continues to
  be owned by `ActorTokenInterceptor` and current controllers;
- preserve `/api/auth/me`, `/api/sync/**`, `/api/assignments/**`,
  `/api/conflicts/**`, health, metrics, and development-mode admin behavior;
- preserve production 404 containment for development admin/token/bootstrap
  surfaces;
- add or update tests proving existing bearer-token API auth still works and
  production development-surface containment still works with Spring Security
  on the classpath;
- document the implemented module boundary only if future implementers need a
  thin code-boundary note.

## Out Of Scope

Do not implement:

- production web-admin OIDC browser login or callback;
- server-managed admin session shell, CSRF/logout/session expiry/session audit;
- `web_admin.access`, `web_admin.read_scoped`, or `config_admin.*` command
  policy;
- config UI, assignment UI, conflict/flag UI, online principal-binding admin,
  mobile login, tenant/workspace-aware behavior, reporting/export,
  retention/security, operations evidence, or real-production approval;
- contract/schema/envelope changes or migrations.

## Required Behavior

- IdP groups, roles, resource claims, custom claims, JWT `actor_id`, request
  bodies, UI state, and the fixed development admin actor remain non-authority.
- Spring Security must not become a second actor-resolution path for existing
  bearer APIs.
- Existing production OIDC/JWKS bearer validation and explicit
  `(issuer, subject) -> actor_id` binding behavior must remain intact.
- Existing development admin and token surfaces must remain development-only.

## Expected Tests

Run focused server tests proving:

- existing production bearer auth still resolves bound OIDC/JWKS principals;
- unmapped/invalid bearer principals still fail as before;
- development token admin remains disabled outside dev-token mode;
- production development-surface filter still hides old `/admin`, `/admin/dev`,
  `/admin/config`, and `/api/actors` surfaces;
- Spring Security does not require form login, basic auth, or CSRF for existing
  bearer JSON APIs.

Run:

```bash
git diff --check
```

Report the exact Maven command(s) run.

## Stop Conditions

Stop before work that:

- implements user-visible web-admin login/session behavior;
- exposes `/admin/session/**` as a working product surface;
- adds command policy or config/admin UX;
- changes contracts, schemas, event envelope fields, event types, sync
  protocol, assignment/scope authority, tenant/workspace context, mobile auth,
  operations evidence, BAR, or CDL;
- requires a product/security choice not already selected by NW-079, NW-084,
  and this NW-099 row.

## Commit Flow

Use one implementation commit:

```text
feat(auth): add web admin security foundation
```

Use the NW trailer:

```text
NW: NW-099
```

Do not mark NW-099 accepted in the implementation commit. After tests pass,
record acceptance separately with a status/backlog commit.
