# NW-048 Agent Prompt: Decide Assignment-Admin Command Capability Model

You are working in `/home/hamza/datarun-platform`.

## Goal

Produce an IDR for assignment-admin command authority before any production assignment-admin UI/API expansion.

Exit target:

```text
Datarun has a selected assignment-admin command-capability model that keeps assignment create/end outside activity role-actions, preserves IDR-024 containment, and gives a future implementation slice exact server-side authority rules and tests.
```

This is a decision/routing slice. It should create an IDR and routing updates only. Do not implement runtime behavior, schemas, APIs, migrations, UI, mobile flows, or tests in this slice.

## Steward Recommendation To Start From

Start from this recommended platform direction and reject it only if the current authority surfaces or code inspection show a concrete contradiction:

1. Add a platform-owned assignment-admin command capability vocabulary outside `activities[*].roles`.
   - Initial command names to decide: `assignment_admin.create` and `assignment_admin.end`.
   - The IDR may choose different final names, but the vocabulary must stay assignment-admin-specific and must not use envelope `type` values as product permissions.
2. Evaluate command capability server-side for the authenticated actor before IDR-024 scope containment.
   - Capability says whether the actor may attempt assignment administration.
   - Containment still decides whether the requested create/end scope is inside one active covering assignment.
3. Prefer a deployment-configured role-to-command-capability policy keyed by existing assignment role labels, with platform-owned command names and platform-owned containment.
   - This supports field handoff and coordinator delegation without inventing new scope mechanisms.
   - Central/provisioning-only administration should remain an explicit deployment posture or rejected alternative, not the default platform target.
4. Do not add event-envelope fields, `assignment_ref`, or device-authored authority claims.
5. Do not encode command capability in `assignment_created/v1` payload unless the IDR proves audit/time semantics require a platform payload contract change. The default should be assignment role label plus current server-side policy, not new event truth.
6. Keep access exceptions separate. Auditor/special access, shared-device actor switching, grace behavior, subject-scope variants, query/custom scope, and new scope mechanisms route through NW-049 or later successors.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-043, NW-048, NW-049, and nearby accepted auth/scope rows only.
5. `docs/agent-working-surface/assignment-admin-authority-exploration.md`
6. `docs/agent-working-surface/operational-ux-layering-companion.md`
   - Use only as a guardrail for product-facing words; it is not authority.
7. IDRs:
   - `docs/decisions/idr-021-role-action-enforcement-model.md`
   - `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`
   - `docs/decisions/idr-024-multi-axis-assignment-containment.md`
   - `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
   - `docs/decisions/idr-028-production-principal-binding-administration.md`
8. `docs/implementation/module-interfaces.md`
   - Read `Scope Resolver`, `Authenticated Actor Resolver`, `Event Store`, `Config Packager`, and related assignment/authorization notes.
9. Contracts:
   - `contracts/shapes/assignment_created.schema.json`
   - `contracts/shapes/assignment_ended.schema.json`
   - `contracts/config-package.schema.json` only if the selected policy surface touches config delivery.
10. Code/test surfaces for inspection only:

- `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`
- `server/src/main/java/dev/datarun/server/authorization/AssignmentController.java`
- `server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java`
- `server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java`
- `server/src/main/java/dev/datarun/server/config/DeployTimeValidator.java`
- `server/src/main/java/dev/datarun/server/config/ConfigPackager.java`
- `server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java`
- `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`

1. Use `scripts/query_cdl.py` only for narrow slices around assignment authority, sync/access, projected authority, containment, production-auth non-authority, and fixed scope mechanisms:

- CDL-030
- CDL-031
- CDL-032
- CDL-034
- CDL-035
- CDL-055

Do not read broad architecture/history docs or old archives unless a directly read source exposes drift that cannot be resolved from the packet.

## Expected Artifact

Create:

```text
docs/decisions/idr-029-assignment-admin-command-capability.md
```

The IDR should include:

1. Context:
   - current containment-only model;
   - why it remains accepted baseline;
   - why it is too coarse for production assignment-admin expansion.
2. Decision:
   - selected command-capability model;
   - exact command vocabulary;
   - where policy lives;
   - how it is evaluated relative to authentication and containment.
3. Scope:
   - create assignment;
   - end assignment;
   - bootstrap/provisioning/root behavior;
   - compatibility with existing accepted assignment events.
4. Non-authority boundary:
   - no `activities[*].roles`;
   - no IdP group/claim/JWT `actor_id` authority;
   - no request-body actor authority;
   - no UI/product vocabulary authority;
   - no resolver reassignment or auto-resolution;
   - no new scope mechanisms.
5. Alternatives:
   - current containment-only;
   - separate command-capability policy;
   - assignment/config policy keyed by assignment roles;
   - central/provisioning-only;
   - event-payload command capability.
6. Consequences:
   - whether implementation needs config/package contract changes;
   - whether mobile only gets advisory/display data or no change;
   - how existing deployments/tests should remain compatible.
7. Guard tests for a future implementation:
   - actor with covering scope but no admin capability cannot create/end;
   - actor with admin capability can create/end only inside one contained active assignment;
   - out-of-scope create/end still fails even with capability;
   - spoofed request-body actors and IdP groups/claims do not grant command authority;
   - `assignment_changed` remains invalid in `activities[*].roles`;
   - normal sync, subject-history, resolver equality, and S22 handoff behavior remain unchanged.
8. Follow-up route:
   - add a bounded implementation prompt/backlog row only if the IDR selects implementable runtime work;
   - leave NW-049 separate.

## Forbidden Work

- Do not edit runtime code, tests, schemas, migrations, mobile code, UI, or contracts.
- Do not implement the selected model in this slice.
- Do not change CDL text or accept BAR rows.
- Do not expand `activities[*].roles` beyond `capture`, `review`, `alert`, `task_created`, and `task_completed`.
- Do not add envelope fields, envelope `type` values, `authority_context`, or `assignment_ref`.
- Do not infer authority from IdP groups, roles, JWT `actor_id`, request-body actor IDs, or UI/product vocabulary.
- Do not combine access exceptions with assignment-admin command authority.
- Do not add auditor access, shared-device sessions, grace behavior, new scope mechanisms, resolver reassignment, auto-resolution, reporting APIs, import/export, or mobile authoritative rejection.

## Backlog And Status Updates

If the IDR lands:

- Mark NW-048 `accepted`.
- Update `docs/status.md` Current Routing with the selected model and successor route.
- Update `docs/agent-working-surface/platform-next-work-backlog.md` with evidence.
- Add a successor implementation row/prompt only if the IDR selects runtime work.
- Do not mark NW-049 accepted or superseded.

If the IDR concludes implementation is not needed:

- Mark NW-048 accepted with explicit deferral.
- Keep current containment-only assignment administration as baseline.
- Do not add an implementation row.

If the decision exposes a contract or CDL-level gap:

- Keep NW-048 `in_review` or mark it `blocked`.
- Add a bounded successor decision row instead of smuggling the contract/CDL change into this slice.

## Verification

This is documentation/routing work. Run:

```bash
git diff --check
```

If Markdown tables are added, inspect them enough to catch broken row structure.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
docs(auth): decide assignment admin command capability
```

The commit should include only the IDR and tightly related routing updates.

## Stop And Report

Stop and report if:

- current CDL/IDR/BAR/code contradict each other on assignment command authority;
- a clean model appears impossible without adding envelope fields or expanding activity role-actions;
- the recommended command-capability path requires a new scope mechanism;
- the decision cannot separate assignment-admin command authority from access exceptions;
- implementation work becomes necessary before the decision can be written.
