# NW-088 Agent Prompt: Productize S23 Config Setup Workflow

You are working in `/home/hamza/datarun-platform`.

## Current Standing

NW-086, NW-087, and NW-100 are accepted.

`/web-admin` now has a production browser login/session boundary, `/web-admin`
shell access is gated by server-side `web_admin.access`, and the reviewed
one-shot provisioning path can seed `admin_command_capabilities` before a
browser setup workflow is used.

The current `/admin/config` controller and templates remain development-only.
They are not production routes, not production authority, and must stay hidden
in production mode.

## Expert Call

Build Candidate 1 web admin as server-rendered Spring MVC/Thymeleaf/HTML first.
Do not introduce Angular, a SPA shell, or a new frontend build system in this
slice.

Use ordinary session-backed pages and CSRF-protected server-side form actions
under `/web-admin/config`. Angular can be reconsidered only in a future route if
the workflow becomes a genuinely rich client application with heavy draft
editing, complex multi-step client state, large interactive surfaces, or a
shared design system requirement.

## Goal

Implement the first production `/web-admin/config` S23 setup workflow over
accepted config behavior.

Exit target:

```text
An authorized web-admin actor can create or update a draft setup candidate,
run server validation, record readiness review, approve the exact validated
candidate hash, and publish one atomic config package. Each action is gated by
the matching config_admin.* command, audited with the server-resolved session
actor, and preserves accepted config-package, expression, assignment, and
web-admin authority boundaries.
```

This is the first real Candidate 1 production admin surface. Keep it bounded
and usable. Prefer a small complete vertical workflow over a broad partial
admin console.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/commit-workflow.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md` rows NW-084,
   NW-086, NW-087, NW-088, NW-100, and NW-093
5. `docs/specifications/product/product-candidate-1.md`
6. `docs/specifications/platform/production-web-admin-authentication-and-authority.md`
7. `docs/specifications/platform/configuration-package-and-shapes.md`
8. `docs/specifications/platform/expression-language.md`
9. `docs/implementation/module-interfaces.md` sections for web/config admin
   command capability, web-admin security/session, config packaging, and
   authenticated actor resolver
10. Relevant source/tests after reading the specs:
    - `server/src/main/java/dev/datarun/server/authorization/WebAdminSessionController.java`
    - `server/src/main/java/dev/datarun/server/authorization/AdminCommandCapabilityService.java`
    - `server/src/main/java/dev/datarun/server/config/`
    - `server/src/main/java/dev/datarun/server/ops/provisioning/ReviewedConfigProvisioner.java`
    - `server/src/main/resources/templates/config/`
    - related tests under `server/src/test/java/dev/datarun/server/`

Open contracts only if you believe a process-boundary contract must change.
The expected route is no contract change.

## In Scope

- Add production routes under `/web-admin/config`.
- Gate every route/action with the server-resolved web-admin session actor and
  the exact command capability:
  - `config_admin.author` for draft candidate create/update;
  - `config_admin.validate` for validation;
  - `config_admin.readiness_review` for readiness review;
  - `config_admin.approve` for approval;
  - `config_admin.publish` for package publication.
- Preserve `web_admin.access` as shell entry only; it must not grant config
  commands by itself.
- Provide a small server-rendered UI for a draft setup candidate, validation
  result, readiness review, approval, publish, current package status, and
  audit/status feedback.
- Persist enough candidate and audit state to bind validation, readiness,
  approval, and publish to the exact candidate content hash. Add server-side
  migrations only if needed for this candidate/audit state.
- Publish through existing accepted config package behavior: revalidate before
  publish and emit one monotonic full package snapshot.
- Reuse existing config services and validation rules where possible. Existing
  `/admin/config` templates and controller code may inform implementation, but
  must not become the production route or production authority.
- Update focused tests and module-interface/status/backlog docs after
  implementation.

## Out Of Scope

Do not implement:

- Angular, SPA routing, or a frontend build system;
- productionizing existing `/admin`, `/admin/config`, or `/admin/dev`;
- editing `admin_command_capabilities` or `assignment_admin_capabilities` from
  the browser;
- online principal-binding administration;
- assignment administration UX;
- mobile setup flow, mobile login/session, or mobile storage changes;
- tenant/workspace-aware auth, sync, storage, config, or UI;
- new config-package keys, shape-format schema changes, sync protocol changes,
  envelope fields, or envelope `type` values;
- deployer scripts/functions, trigger execution, dynamic query authority,
  custom workflow state machines, or per-device config package variants;
- reporting/export, broad audit/history reads, conflict queues, resolver
  reassignment, batch resolution, or auto-resolution;
- real-production approval.

## Required Behavior

- A valid web-admin session without the matching command is denied before the
  action runs.
- Browser requests never supply or override the command actor id. Actor context
  comes only from the server-managed web-admin session.
- IdP groups, roles, claims, JWT `actor_id`, request body actor ids, UI state,
  fixed development actors, and assignment roles remain non-authority for
  config-admin commands.
- Draft edits do not publish and are not delivered to devices.
- Validation failures are shown as setup issues and block approval/publish.
- Readiness review records human review against a candidate hash and validation
  result; it is not workflow truth outside this admin candidate.
- Approval binds to the exact validated candidate hash. Changing candidate
  content invalidates prior approval.
- Publish revalidates, confirms approval still matches the candidate hash, and
  publishes one atomic package through accepted `ConfigPackager` semantics.
- Invalid setup, stale approval, missing command capability, and failed publish
  leave no partial config package publication.
- Current development surfaces remain hidden in production.

## Expected Changed Surfaces

Likely implementation homes:

- production web config controller/service under server code;
- server-rendered templates under a `/web-admin` template namespace;
- optional migration for candidate/audit state if existing tables are
  insufficient;
- focused server tests for auth, command gates, candidate state, validation,
  approval, publish, and production dev-surface containment;
- `docs/implementation/module-interfaces.md` only if module boundaries change;
- status/backlog acceptance after verification.

Avoid changes to contracts, JSON schemas, mobile code, BAR, CDL, tenant
architecture, operations rehearsal evidence, or real-production standing.

## Expected Tests

Add or update focused server tests proving:

- unauthenticated users cannot reach `/web-admin/config`;
- actors with `web_admin.access` but without the matching `config_admin.*`
  command are denied for each protected action;
- granted actors can perform the matching action;
- request-body/UI actor spoofing cannot change the command actor;
- draft edits do not publish packages;
- validation failures block readiness/approval/publish;
- approval is tied to the exact candidate hash and becomes stale after edits;
- publish revalidates and creates one atomic package only for the approved
  candidate;
- publish records the server-resolved actor and audit/status evidence;
- current `/admin`, `/admin/config`, and `/admin/dev` remain development-only
  and hidden in production.

Run focused Maven tests for new/touched code. Run broader auth/config/web-admin
regression if shared controller, security, validation, packaging, or migration
code changes.

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
- any intentionally deferred UI polish or structured editor depth.

## Stop Conditions

Stop and report before work that:

- makes config-admin authority come from IdP groups/roles/claims, JWT
  `actor_id`, request bodies, UI state, fixed dev actors, or assignment roles;
- turns `/admin/config` into a production surface;
- lets browser UI edit admin command capability, assignment command capability,
  or principal binding policy;
- requires new process-boundary contracts, config package keys, envelope
  fields/types, sync protocol fields, tenant-aware internals, mobile partition
  changes, or a new authority primitive;
- turns readiness review or approval into real-production approval;
- absorbs assignment admin UX, mobile login/session, reporting/export,
  conflict batch handling, retention/security, or tenant/workspace work.

## Commit Flow

Use this route commit before implementation:

```text
docs(config): route setup workflow productization
```

Use one or more focused implementation commits if needed, starting with:

```text
feat(config): add web admin setup workflow
```

Use the NW trailer:

```text
NW: NW-088
```

Do not mark NW-088 accepted in an implementation commit. After tests pass,
record acceptance separately with a status/backlog commit.
