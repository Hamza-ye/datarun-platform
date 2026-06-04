# NW-050 Agent Prompt: Implement Assignment-Admin Command Capability

You are working in `/home/hamza/datarun-platform`.

## Goal

Implement the assignment-admin command capability model selected by IDR-029:

```text
Server-side assignment create/end authorization requires the authenticated actor
to hold the relevant platform-owned assignment-admin command capability through
one active assignment role, and that same active assignment must contain the
requested create/end target scope under IDR-024.
```

This is a server authorization/config slice. It must not implement auditor or
special access, shared-device sessions, grace behavior, subject-scope variants,
query/custom scopes, resolver reassignment, auto-resolution, online production
binding-admin APIs, UI, mobile authoritative rejection, or event contract
changes.

## Files To Read

Read only this bounded packet first:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-043, NW-048, NW-049, and NW-050.
4. `docs/decisions/idr-021-role-action-enforcement-model.md`
5. `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`
6. `docs/decisions/idr-024-multi-axis-assignment-containment.md`
7. `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
8. `docs/decisions/idr-028-production-principal-binding-administration.md`
9. `docs/decisions/idr-029-assignment-admin-command-capability.md`
10. `docs/implementation/module-interfaces.md`
    - Read `Scope Resolver`, `Authenticated Actor Resolver`, `Event Store`,
      `Config Packager`, and related assignment/authorization notes.
11. Contracts:
    - `contracts/shapes/assignment_created.schema.json`
    - `contracts/shapes/assignment_ended.schema.json`
    - `contracts/config-package.schema.json` only if capability policy is
      packaged to mobile/admin clients.
12. Current code/test surfaces:
    - `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`
    - `server/src/main/java/dev/datarun/server/authorization/AssignmentController.java`
    - `server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java`
    - `server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java`
    - `server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java`
    - `server/src/main/java/dev/datarun/server/config/ConfigPackager.java`
    - `server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/authorization/ProductionAuthIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`
    - config package contract tests only if package shape changes.
13. CDL slices by command, not broad file reading:
    - `python3 scripts/query_cdl.py --id CDL-030`
    - `python3 scripts/query_cdl.py --id CDL-031`
    - `python3 scripts/query_cdl.py --id CDL-032`
    - `python3 scripts/query_cdl.py --id CDL-034`
    - `python3 scripts/query_cdl.py --id CDL-035`
    - `python3 scripts/query_cdl.py --id CDL-055`

Open other files only when these sources route you there.

## Authority And Guardrails

- IDR-029 is active and selects the exact commands
  `assignment_admin.create` and `assignment_admin.end`.
- Command capability is outside `activities[*].roles`; activity role maps remain
  limited to `capture`, `review`, `alert`, `task_created`, and
  `task_completed`.
- Server authorization must use the authenticated actor from
  `AuthenticatedActorResolver`, not request-body actor fields.
- For ordinary actor commands, one active assignment must both grant the
  relevant command through its role label and contain the requested target
  scope. Do not combine command capability from one assignment with scope from a
  different assignment.
- IDR-024 containment remains platform-owned across `geographic`,
  `subject_list`, and `activity`; do not add scope types or deployer-defined
  containment logic.
- Groups, roles, resource claims, custom claims, and JWT `actor_id` claims are
  not command authority.
- Initial bootstrap/provisioning remains explicit and separate from ordinary
  actor command authority.

## Expected Implementation Boundary

Implement a dedicated deployment-configured server policy surface named
`assignment_admin_capabilities` with role-to-command semantics:

```json
{
  "schema_version": 1,
  "roles": {
    "coordinator": ["assignment_admin.create", "assignment_admin.end"],
    "handoff_lead": ["assignment_admin.create"]
  }
}
```

Expected behavior:

- Validate the policy before it can authorize commands.
- Reject unknown command names, non-object role maps, blank role names, and
  non-array command lists.
- Treat absent role entries and absent command names as deny.
- Keep command names platform-owned constants.
- Keep the policy separate from activity config and `activities[*].roles`.
- Evaluate `assignment_admin.create` before create containment and
  `assignment_admin.end` before end containment.
- Run IDR-024 containment only across command-capable active assignments.
- Preserve explicit initial bootstrap behavior.
- Preserve current request-body spoofing protections.
- Preserve platform payload schemas for `assignment_created/v1` and
  `assignment_ended/v1`.

Implementation surface guidance:

- Prefer a small authorization/config service for policy lookup and validation.
- Reuse existing deployment-config patterns where they fit; do not introduce a
  broad configuration subsystem for this slice.
- If policy is delivered to mobile or admin clients for advisory display, add a
  known top-level config package section and update
  `contracts/config-package.schema.json` plus contract tests.
- If policy remains server-only, do not touch mobile or config package
  contracts.

## Expected Tests

Add focused server tests proving:

- actor with covering scope but no `assignment_admin.create` cannot create;
- actor with covering scope but no `assignment_admin.end` cannot end;
- actor with `assignment_admin.create` can create only inside one active
  command-capable covering assignment;
- actor with `assignment_admin.end` can end only target assignments inside one
  active command-capable covering assignment;
- command capability from one assignment cannot combine with scope from another
  assignment;
- out-of-scope create/end fails even with command capability;
- create-only capability does not grant end capability, and end-only capability
  does not grant create capability;
- spoofed request-body actor fields do not grant command authority;
- IdP groups, roles, resource claims, custom claims, and JWT `actor_id` do not
  grant assignment-admin command authority;
- `assignment_changed` remains invalid in `activities[*].roles`;
- explicit initial bootstrap remains bounded and cannot be reached by spoofing;
- normal sync, subject-history, resolver equality, and S22 handoff behavior
  remain unchanged.

Run at minimum:

```bash
cd server
./mvnw -Dtest=AssignmentContainmentIntegrationTest test
./mvnw -Dtest=ProductionAuthIntegrationTest,AssignmentContainmentIntegrationTest test
./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest,ScopeFilteredSyncIntegrationTest,SubjectHistoryBackfillIntegrationTest,ConflictResolutionIntegrationTest test
```

If config package contracts change, also run:

```bash
cd server
./mvnw -Dtest=ConfigPackageSchemaContractTest,ConfigIntegrationTest,DeployTimeValidatorTest test
```

Run full server tests after a coherent green slice. Run mobile tests only if
mobile code changes.

## Forbidden Work

- No envelope fields, envelope `type` values, `authority_context`, or
  `assignment_ref`.
- No assignment payload command-capability fields.
- No expansion of `activities[*].roles` beyond the five IDR-023 work actions.
- No IdP group/claim/JWT `actor_id` authority.
- No request-body actor authority.
- No auditor/special access, shared-device actor switching, grace behavior,
  subject-scope variants, query/custom scopes, or new scope mechanisms.
- No resolver reassignment, auto-resolution, trigger execution, reporting API,
  import/export, production binding-admin API/UI, or mobile authoritative
  rejection.

## Documentation Updates

After implementation and passing tests, update:

- `docs/status.md` Current Routing and What's Next.
- `docs/agent-working-surface/platform-next-work-backlog.md` NW-050 evidence.
- `docs/implementation/module-interfaces.md` if module ownership, inputs,
  outputs, storage, or guard tests change.
- `contracts/config-package.schema.json` only if policy is packaged to clients.

Do not mark NW-049 accepted or superseded.

## Commit Boundary

One implementation commit is expected if tests pass, for example:

```text
feat(auth): add assignment admin command capabilities
```

## Stop And Report

Stop and report if:

- the implementation appears to require new envelope fields, assignment payload
  fields, or event `type` values;
- command capability cannot be kept separate from `activities[*].roles`;
- clean enforcement appears impossible without a new scope mechanism;
- product requires IdP group/role/claim membership or request-body actor IDs as
  command authority;
- implementation pressure combines NW-049 access exceptions with assignment
  create/end command authority.
