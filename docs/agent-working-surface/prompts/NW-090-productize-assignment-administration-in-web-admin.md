# NW-090 Agent Prompt: Productize Assignment Administration In Web Admin

You are working in `/home/hamza/datarun-platform`.

## Current Standing

NW-084, NW-069, NW-070, NW-079, NW-086, NW-087, NW-088, and NW-100 are
accepted.

`/web-admin` has a production browser login/session boundary. `/web-admin`
shell access is gated by server-side `web_admin.access`. `/web-admin/config`
is the accepted first Candidate 1 server-rendered Spring MVC/Thymeleaf pattern.

The current `/admin/assignments` controller and templates remain
development-only. They bind assignment commands to the fixed development admin
actor and must stay hidden in production.

NW-093 remains blocked. This route does not approve real production, real
users, real organizational data, provider/region/jurisdiction selection, or
support commitment.

## Expert Call

Build Candidate 1 assignment administration as server-rendered Spring
MVC/Thymeleaf/HTML under `/web-admin/assignments`.

Do not introduce Angular, a SPA shell, or a new frontend build system in this
slice. Use ordinary session-backed pages and CSRF-protected server-side form
actions, following the `/web-admin/config` pattern where it fits.

## Goal

Implement the first production `/web-admin/assignments` assignment
administration surface for Product Candidate 1.

Exit target:

```text
An authorized web-admin actor can create and end responsibilities from
server-rendered pages under /web-admin/assignments. The page requires a valid
web-admin session plus web_admin.access, and every assignment mutation uses the
existing assignment_admin.create or assignment_admin.end authority path with
same-assignment containment from the accepted assignment spec and code.
```

Keep the slice narrow. Prefer a complete create/end responsibility workflow
over a broad admin console.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-084,
   NW-088, NW-090, NW-093, and the accepted web-admin rows NW-086, NW-087,
   NW-100
5. `docs/specifications/product/product-candidate-1.md`
6. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
7. `docs/specifications/platform/assignment-scope-and-administration.md`
8. `docs/specifications/platform/production-auth-principal-binding.md`
9. `docs/implementation/module-interfaces.md` sections for scope resolver,
   assignment admin capability policy, web/config admin command capability,
   web-admin security/session, and production runtime boundary
10. Relevant source/tests after reading the specs:
    - `server/src/main/java/dev/datarun/server/authorization/WebAdminSessionController.java`
    - `server/src/main/java/dev/datarun/server/authorization/WebAdminSessionService.java`
    - `server/src/main/java/dev/datarun/server/authorization/AdminCommandCapabilityService.java`
    - `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`
    - `server/src/main/java/dev/datarun/server/authorization/AssignmentAdminCapabilityService.java`
    - `server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java`
    - `server/src/main/java/dev/datarun/server/authorization/AssignmentController.java`
    - `server/src/main/java/dev/datarun/server/admin/AdminController.java`
    - `server/src/main/java/dev/datarun/server/config/WebAdminConfigController.java`
    - `server/src/main/resources/templates/web-admin/config.html`
    - existing assignment templates under `server/src/main/resources/templates/`
      as development-only reference
    - related tests under `server/src/test/java/dev/datarun/server/`

Open contracts only if you believe a process-boundary contract must change.
The expected route is no contract change.

## Authority

- The production path is Spring MVC/Thymeleaf/HTML under
  `/web-admin/assignments`.
- Page entry requires a valid server-managed web-admin session plus
  `web_admin.access` for the server-resolved session actor.
- Assignment create must call the existing assignment command path for the
  server-resolved session actor and must rely on existing
  `assignment_admin.create` capability plus same-assignment containment.
- Assignment end must call the existing assignment command path for the
  server-resolved session actor and must rely on existing
  `assignment_admin.end` capability plus same-assignment containment.
- `assignment_admin.create` and `assignment_admin.end` remain owned by the
  existing `assignment_admin_capabilities` role-to-command policy evaluated
  from current active assignments.
- `web_admin.access` is only shell/page entry. It must not grant assignment
  create, assignment end, broad reads, resolver authority, config-admin
  commands, or root authority by itself.
- No new command authority, authority source, scope axis, or containment model
  is allowed in this slice. If implementation appears to need one, stop and
  escalate.

## In Scope

- Add production routes under `/web-admin/assignments`.
- Provide CSRF-protected browser forms for creating assignments and ending
  assignments through the existing `AssignmentService`.
- Use only the server-managed `WebAdminSessionContext.actorId()` as the command
  actor.
- Preserve accepted assignment payload meaning: target actor, role,
  geographic scope, subject-list scope, activity scope, valid-from, and
  valid-to.
- Preserve accepted scope semantics: `null` means unrestricted; empty
  subject/activity lists are invalid; non-null axes compose with AND inside one
  assignment; assignments compose with OR only across visibility/work authority,
  not by combining command capability from one assignment with scope from
  another for a mutation.
- Keep the current `/admin/assignments` development surface dev-only and hidden
  in production.
- Add focused tests and update implementation/status/backlog docs only after
  implementation lands and verification passes.

## Out Of Scope

Do not implement:

- productionizing existing `/admin`, `/admin/assignments`, `/admin/config`, or
  `/admin/dev`;
- Angular, SPA routing, or a frontend build system;
- generic admin root, fixed development actor, bootstrap actor, or no-assignment
  bypass for browser assignment commands;
- IdP group, claim, role, resource claim, JWT `actor_id`, browser-selected
  actor, request-body actor, UI state, or session id as assignment authority;
- browser editing of `assignment_admin_capabilities`;
- browser editing of `admin_command_capabilities` or principal-binding policy;
- new subject, query, custom, auditor, emergency, tenant, workspace, or
  location-as-tenant scopes;
- contract, schema, sync protocol, assignment payload, event envelope field, or
  event envelope `type` changes;
- tenant-aware runtime internals, tenant/workspace UI, pooled predicates, or
  default-workspace scaffolding;
- online production principal-binding administration;
- reporting, export/import, broad audit/history reads, resolver reassignment,
  batch resolution, or auto-resolution;
- mobile code, mobile setup flow, mobile login/session, mobile storage, or
  shared-device local-state changes;
- operations evidence, deployment runbook/rehearsal changes, provider/region
  choices, support commitments, or real-production approval.

## Required Behavior

- Unauthenticated users are redirected to `/web-admin/login` for page access
  and cannot execute assignment form actions.
- Authenticated web-admin actors without `web_admin.access` are denied before
  page or action work runs.
- Actors with `web_admin.access` but without a current active assignment
  granting `assignment_admin.create` cannot create assignments.
- Actors with `web_admin.access` but without a current active assignment
  granting `assignment_admin.end` cannot end assignments.
- Create and end containment remains enforced by one active assignment that
  both grants the matching command and contains the requested or target
  assignment scope.
- Command capability from one assignment must not be combined with scope from
  another assignment for create or end.
- Browser requests may include target actor and target assignment IDs as
  command inputs, but they must never supply or override the command actor ID.
- Request actor spoofing fields such as `actor_id`, `creator_actor_id`, or
  similar form parameters are ignored or rejected and cannot affect the
  server-resolved session actor.
- Assignment events remain append-only `assignment_changed` events with existing
  shapes and envelope semantics.
- `web_admin.access`, `admin_command_capabilities`, config-admin commands, IdP
  metadata, and UI labels do not grant assignment mutations.
- CSRF protection applies to state-changing browser form actions.
- Current `/admin`, `/admin/assignments`, `/admin/config`, and `/admin/dev`
  remain development-only and hidden in production.

## Expected Changed Surfaces

Likely implementation homes:

- a production web-admin assignment controller/service under server code;
- server-rendered templates under the `web-admin` template namespace;
- a `/web-admin/shell` link only when appropriate, without turning shell access
  into assignment authority;
- focused server tests for session, command gates, same-assignment containment,
  CSRF, actor spoofing, and development-surface containment;
- `docs/implementation/module-interfaces.md` only if module boundaries change;
- status/backlog acceptance after implementation verification.

Avoid changes to contracts, JSON schemas, sync protocol, mobile code, BAR, CDL,
tenant architecture, operations rehearsal evidence, or real-production
standing.

## Expected Tests

Add or update focused server tests proving:

- unauthenticated users are redirected before reaching
  `/web-admin/assignments`;
- authenticated actors without `web_admin.access` are denied;
- actors with `web_admin.access` but without the required assignment command are
  denied for create and end;
- create containment is enforced, including denial when command capability and
  requested scope are split across different active assignments;
- end containment is enforced, including denial when command capability and
  target assignment scope are split across different active assignments;
- request actor spoofing is rejected or ignored and cannot change the command
  actor;
- the current `/admin` assignment surface remains development-only and hidden
  in production;
- CSRF-protected browser forms are required for create and end actions;
- relevant existing assignment/admin regression tests still pass, especially
  `AssignmentContainmentIntegrationTest`,
  `AdminCommandCapabilityServiceIntegrationTest`,
  `WebAdminSessionBoundaryTest`,
  `ProductionAuthIntegrationTest`,
  `ProductionDevelopmentSurfaceFilterTest`, and
  `WebAdminSecurityFoundationTest`.

Run focused Maven tests for new/touched code. Run broader auth/assignment/
web-admin regression if shared controller, security, assignment service,
location lookup, or migration code changes.

## Verification

Required before commit:

```bash
git diff --check
```

Also report:

- exact Maven test command(s) run;
- whether contracts, schemas, sync protocol, mobile files, BAR, CDL,
  tenant/workspace routes, operations evidence, and real-production standing had
  no diff;
- any intentionally deferred UI polish, assignment listing/search depth, or
  structured picker work.

## Stop Conditions

Stop and report before work that:

- combines command capability from one assignment with scope from another
  assignment for create or end;
- needs a new subject, query, custom, auditor, emergency, tenant, workspace, or
  location-as-tenant scope;
- needs admin capability policy editing from the browser UI, including
  `assignment_admin_capabilities`, `admin_command_capabilities`, or principal
  binding policy;
- needs tenant/workspace internals, pooled storage, runtime tenant context, or
  tenant-aware sync/config/assignment behavior;
- weakens accepted assignment containment or turns web-admin shell access into
  assignment root authority;
- makes assignment authority come from IdP groups/roles/claims, JWT `actor_id`,
  request bodies, UI state, browser-selected actors, fixed development actors,
  or assignment role labels without the existing server-side command policy;
- turns `/admin/assignments` into a production surface;
- requires contracts, schemas, sync protocol, envelope, mobile, operations
  evidence, or real-production approval changes.

## Commit Flow

Only commit if the task explicitly authorizes commits.

Use this route commit before implementation if routing is not already landed:

```text
docs(assignments): route web admin assignment administration
```

Use one or more focused implementation commits if needed, starting with:

```text
feat(assignments): add web admin assignment administration
```

Use the NW trailer:

```text
NW: NW-090
```

Do not mark NW-090 accepted in an implementation commit. After tests pass,
record acceptance separately with a status/backlog commit.
