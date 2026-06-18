# NW-079 Agent Prompt: Decide Production Web Admin Authentication And Admin Authority

You are working in `/home/hamza/datarun-platform`.

## Goal

Produce the bounded decision/spec brief for production web admin
authentication and admin-authority behavior before any production web admin UI
implementation.

Exit target:

```text
Datarun has an accepted route for production web admin authentication,
browser/session actor resolution, admin command authority, audit semantics,
development-console containment, and deferred admin surfaces.
```

This is decision/specification work. It is not UI implementation, not an
online principal-binding admin API, not a production deployment rehearsal, and
not a runtime code-change task.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md` row NW-079 and
   the Post-NW-068 trigger map
6. `docs/agent-working-surface/artifacts/product-admin-surface-forward-plan.md`
7. `docs/agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md`
8. `docs/agent-working-surface/operational-ux-layering-companion.md`
9. `docs/agent-working-surface/decision-anchor-layer/README.md`
10. `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
11. `docs/specifications/platform/production-auth-principal-binding.md`
12. `docs/specifications/platform/assignment-scope-and-administration.md`
13. `docs/specifications/platform/configuration-package-and-shapes.md`
14. `docs/specifications/platform/expression-language.md`
15. `contracts/sync-protocol.md`
16. `contracts/config-package.schema.json`
17. `contracts/shape-format.schema.json`
18. `contracts/flag-catalog.md`
19. `docs/implementation/module-interfaces.md` sections for Authenticated
    Actor Resolver, Assignment Admin Capability Policy, Config Packager,
    Conflict Resolution, Production Runtime Boundary, and any web/admin
    module notes
20. Current web/admin and auth implementation evidence:
    - `server/src/main/java/dev/datarun/server/authorization/WebConfig.java`
    - `server/src/main/java/dev/datarun/server/admin/AdminController.java`
    - `server/src/main/java/dev/datarun/server/config/ConfigAdminController.java`
    - `server/src/main/java/dev/datarun/server/admin/DevBootstrapController.java`
    - `server/src/main/java/dev/datarun/server/ops/ProductionDevelopmentSurfaceFilter.java`
    - `server/src/main/java/dev/datarun/server/authorization/AuthenticatedActorResolver.java`
    - `server/src/main/java/dev/datarun/server/authorization/ActorTokenInterceptor.java`
    - `server/src/main/java/dev/datarun/server/authorization/AuthMeController.java`
    - `server/src/main/java/dev/datarun/server/authorization/AssignmentController.java`
    - `server/src/main/java/dev/datarun/server/integrity/ConflictController.java`
    - `server/src/test/java/dev/datarun/server/admin/AdminFlagIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/config/ConfigIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/ops/ProductionDevelopmentSurfaceFilterTest.java`

Use CDL slices only if the sources above expose a concrete authority conflict
or the selected model would introduce new structural authority. Do not read or
rewrite the whole CDL.

## Questions To Answer

1. Which production admin personas or operating contexts are in scope first:
   setup owner, assignment coordinator, reviewer/resolver, support operator,
   deployment owner, or read-only observer?
2. Which admin tasks are in scope first: admin shell access, config edit,
   validation, publish, assignment create/end, location/subject inspection,
   flag inspection/resolution, or development bootstrap containment?
3. Which browser/session authentication mode should be selected for first
   production web admin:
   - bearer token only;
   - OIDC/JWKS login with server-managed session cookie;
   - reverse-proxy authenticated identity forwarded to the app;
   - development admin remains dev-only and production web admin is deferred?
4. How does the chosen mode resolve the admin request to the accepted platform
   actor context without using IdP claims as platform authority?
5. Which admin actions are governed by accepted assignment-admin command
   authority, which require explicit web-admin/config-admin command authority,
   and which remain deployment-managed only?
6. What audit semantics are required for login/session, config changes,
   config publish, assignment commands, flag resolution, and development
   surface containment?
7. Which surfaces remain deferred: online principal-binding administration,
   mobile OIDC login, reporting/export, retention/security, broad audit reads,
   conflict batch handling, resolver reassignment, auto-resolution, new scope
   mechanisms, and production deployment approval?

## Expected Durable Output

Create the selected durable output under `docs/documentation-organization.md`.
The expected first durable home is:

```text
docs/specifications/platform/production-web-admin-authentication-and-authority.md
```

Also update:

- `docs/specifications/platform/README.md` when a platform spec is created;
- `docs/agent-working-surface/platform-next-work-backlog.md` NW-079 row;
- `docs/status.md` Current Routing only after the decision/spec is ready for
  acceptance or accepted, as appropriate.

The durable output must include:

- document metadata required by `docs/documentation-organization.md`;
- admin persona/context and task inventory;
- selected authentication/session mode and rejected alternatives;
- server-resolved admin actor context semantics;
- command authority model for each selected admin task;
- audit semantics and required event/operation-history posture;
- development-console containment and production-mode behavior;
- explicit non-goals and deferred surfaces;
- acceptance criteria and successor implementation rows;
- stop conditions that require architecture, contracts, operations, or
  product decision escalation.

If the work cannot select a mode because product/security input is missing,
write a non-binding routing artifact under
`docs/agent-working-surface/artifacts/` and leave NW-079 blocked with the
missing input named. Do not create a pseudo-decision.

## Guardrails

- Do not implement web UI, controllers, filters, sessions, migrations, tests,
  schemas, or contracts in this slice unless a narrow documentation-only
  reference correction is explicitly required.
- Do not mark `/admin` or `/admin/config` production-ready until the decision
  is accepted and a successor implementation verifies the selected model.
- Do not use IdP groups, roles, resource claims, custom claims, JWT `actor_id`,
  request-body actor IDs, UI-selected actors, or the fixed development admin
  actor as platform/admin authority.
- Do not add online production principal-binding admin APIs or UI. Existing
  accepted principal-binding administration remains deployment-managed manifest
  provisioning unless a separate route selects online binding administration.
- Do not weaken accepted assignment-admin command-plus-containment behavior.
- Do not add new scope mechanisms, resolver reassignment, auto-resolution,
  batch conflict resolution, reporting/export APIs, broad audit/history reads,
  entity lifecycle, trigger execution, envelope fields, or event `type` values.
- Do not move owner process, secret handling, production deployment approval,
  backup/restore, monitoring, or rehearsal proof from operations docs into
  platform authority.
- Stop if the selected model needs a new authority primitive, new process
  contract, changed auth semantics, changed sync/access behavior, or direct
  provider-claim authority.

## Verification

Run:

```bash
git diff --check
```

Also verify:

- any new durable spec is indexed from `docs/specifications/platform/README.md`;
- no runtime code, tests, schemas, fixtures, migrations, contracts, BAR, CDL,
  old IDR text, operations policy/runbook/rehearsal record, or unrelated docs
  changed unless explicitly justified;
- links to accepted specs, contracts, implementation evidence, and NW rows are
  valid by path search;
- `docs/documentation-organization.md` and `docs/commit-workflow.md` remain
  unchanged.

## Commit Flow

If commits are requested, use separate commits:

```text
docs(product): route production admin auth
docs(platform): define production admin auth
docs(status): accept production admin auth
```

Use the NW trailer on commits owned by this route:

```text
NW: NW-079
```

Do not mark NW-079 accepted until the durable output, verification, and
required review are complete. Do not start web UI implementation until a
successor row is selected after NW-079 acceptance.

## Stop And Report

Stop if the brief tries to solve mobile login, online principal-binding
administration, reporting/export, retention/security, conflict batch handling,
new scopes, or deployment approval in the same slice.
