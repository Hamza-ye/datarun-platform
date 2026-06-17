# NW-069 Agent Prompt: Extract Assignment Scope And Administration Durable Behavior

You are working in `/home/hamza/datarun-platform`.

## Goal

Create the durable platform/spec route for accepted assignment, scope-filtered
access, activity role-action boundary, multi-axis containment, and
assignment-admin command-capability behavior currently scattered across IDRs,
phase notes, BAR evidence, contracts, module boundaries, and implementation
tests.

This is a specification extraction task. It is not an implementation task, not
a behavior-change task, and not an old-document cleanup pass.

## Read

1. `AGENTS.md`
2. `docs/status.md` Current Routing
3. `docs/documentation-organization.md`
4. `docs/commit-workflow.md`
5. `docs/agent-working-surface/platform-next-work-backlog.md` NW-069
6. `docs/agent-working-surface/artifacts/architecture-classification-drift-audit.md`
7. `docs/agent-working-surface/artifacts/idr-durable-surface-routing-audit.md`
8. `docs/agent-working-surface/decision-anchor-layer/gap-routing-playbook.md`
9. `docs/implementation/module-interfaces.md` sections for Event Store,
   Conflict Detector, Scope Resolver, Assignment Admin Capability Policy,
   Authenticated Actor Resolver, Sync Surfaces, Config Packager, and Mobile
   Actor Session And Local Store
10. BAR-003, BAR-006, BAR-007, BAR-010, BAR-011, BAR-013, BAR-014, and BAR-104
    in `docs/agent-working-surface/baseline-acceptance-register.md`
11. `docs/decisions/idr-013-assignment-payload.md`
12. `docs/decisions/idr-014-materialized-path-locations.md`
13. `docs/decisions/idr-015-scope-filtered-sync-query.md`
14. `docs/decisions/idr-021-role-action-enforcement-model.md`
15. `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`
16. `docs/decisions/idr-024-multi-axis-assignment-containment.md`
17. `docs/decisions/idr-029-assignment-admin-command-capability.md`
18. `contracts/shapes/assignment_created.schema.json`
19. `contracts/shapes/assignment_ended.schema.json`
20. `contracts/sync-protocol.md`
21. `contracts/config-package.schema.json`
22. `contracts/flag-catalog.md`
23. Relevant implementation/test evidence:
    - `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`
    - `server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java`
    - `server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java`
    - `server/src/main/java/dev/datarun/server/authorization/AssignmentAdminCapabilityService.java`
    - `server/src/main/java/dev/datarun/server/sync/SyncController.java`
    - `server/src/main/java/dev/datarun/server/event/EventRepository.java`
    - `server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java`
    - `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java`
    - `server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/authorization/ScopeFilteredSyncIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/integrity/AuthFlagIntegrationTest.java`
    - `server/src/test/java/dev/datarun/server/config/DeployTimeValidatorTest.java`

Use CDL slices only if one of the sources above exposes a concrete authority
conflict. Do not read or rewrite the whole CDL.

## Expected Output

Create or update only the durable surfaces needed for this slice:

- platform specification under
  `docs/specifications/platform/assignment-scope-and-administration.md`;
- `docs/specifications/platform/README.md` index entry;
- backlog/status fold-forward only if the NW is fully accepted after
  verification and review.

The platform spec should use the required document metadata from
`docs/documentation-organization.md` and should record accepted behavior for:

- assignment payload meaning and assignment identity;
- active assignment reconstruction and temporal validity;
- scope composition: AND within one assignment, OR across assignments;
- geographic, subject-list, and activity axes, including unrestricted `null`
  semantics and empty-list rejection/non-equivalence;
- write-time event `location_path` scope behavior and non-rewrite boundary;
- scope-filtered sync inclusion categories, including own assignment events and
  subject-history separation;
- activity role-action vocabulary and server/mobile authority split;
- `assignment_changed` exclusion from activity role-action configuration;
- multi-axis create/end containment and explicit bootstrap/root boundaries;
- assignment-admin command capability policy, command names, role-to-command
  semantics, same-assignment command-plus-containment rule, and non-authority
  boundaries;
- non-goals and future routes for new scope mechanisms, broad audit/history
  access, online production admin expansion, IdP group/claim authority,
  resolver reassignment, auto-resolution, mobile authoritative rejection, and
  retention/security behavior.

## Required Decisions Inside The Slice

Decide and state explicitly:

- which parts are owned by existing contracts, especially assignment payload
  schemas, config package shape, sync protocol, and flag catalog;
- which parts are accepted platform behavior that need durable prose because
  contracts alone do not express the behavior;
- which parts remain implementation evidence only, such as query/index
  mechanics or exact Java helper structure;
- whether `contracts/sync-protocol.md` already carries enough sync/access
  process-boundary language for this extraction. Update it only if a narrow
  documentation-only trace correction is required; do not change protocol
  semantics;
- how IDR-013/014/015/021/023/024/029 remain historical implementation
  provenance and inputs after extraction.

## Guardrails

- Do not change runtime code, JSON schemas, fixtures, tests, BAR, CDL, old IDR
  text, or phase files.
- Do not add envelope fields, envelope `type` values, assignment payload fields,
  assignment refs, scope mechanisms, activity action vocabulary, config-package
  known sections, or authority sources.
- Do not turn `activities[*].roles` into assignment-administration authority.
- Do not make assignment-admin command capability an IdP claim, request-body
  actor field, mobile-side authority, resolver authority, conflict-resolution
  authority, or audit/history authority.
- Do not use deployment/product labels as platform authority primitives.
- Do not infer a new production admin UI/API, online binding-admin surface,
  broad auditor role, emergency override, or retention/security behavior.
- Do not weaken containment-before-append or exact sync/access equivalence. If
  extraction appears to require weakening those boundaries, stop and report a
  formal architecture/security route.
- Do not duplicate JSON schema contents in platform specs; link to contracts.
- Stop if accepted behavior appears to change rather than become better
  documented.

## Verification

Run:

```bash
git diff --check
```

Also verify that:

- `docs/documentation-organization.md` and `docs/commit-workflow.md` are not
  changed;
- the new durable spec is indexed from
  `docs/specifications/platform/README.md`;
- links to contracts, BAR/status evidence, IDRs, module boundaries, and test
  evidence are valid by path search;
- no runtime code, schema, fixture, test, old IDR, phase file, BAR, or CDL diff
  exists.

## Commit Flow

Use separate commits for route, durable specification, optional contract
documentation, and status acceptance if commits are requested. Include:

```text
NW: NW-069
```

Do not mark NW-069 accepted until durable outputs and verification are
complete, reviewed, and folded forward.
