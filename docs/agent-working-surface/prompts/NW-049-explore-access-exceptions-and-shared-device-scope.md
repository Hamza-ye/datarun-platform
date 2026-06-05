# NW-049 Agent Prompt: Explore Access Exceptions And Shared-Device Scope

You are working in `/home/hamza/datarun-platform`.

## Goal

Produce a focused exploration and decision-routing artifact for access-exception pressure after NW-050.

Exit target:

```text
Datarun has a clear route for auditor/special access, shared-device actor sessions, grace behavior, subject-scope variants, query/custom scope pressure, and device data-expiry questions, without weakening the accepted assignment-admin, production-auth, sync, or resolver authority boundaries.
```

This is exploration and routing work only. It must not implement runtime behavior, schemas, APIs, UI, mobile flows, migrations, tests, or accepted platform capability changes.

## Steward Recommendation To Start From

Start from this recommended platform direction and reject it only if current authority surfaces or source inspection show a concrete contradiction:

1. Keep NW-050 settled.
   - Assignment create/end command capability is `assignment_admin.create` / `assignment_admin.end`.
   - One active assignment must both grant the command and contain the requested create/end scope.
   - Do not reopen assignment-admin command authority in this slice.
2. Split access-exception pressure into separate families instead of creating one broad "exception" feature:
   - auditor/special read visibility;
   - emergency or special write authority;
   - shared-device actor session switching;
   - grace behavior for stale/offline work;
   - subject-scope variants;
   - query/custom scope pressure;
   - device data expiry or retained local data.
3. Prefer deferral or successor decisions over implementation when a pressure crosses current authority boundaries.
   - New scope mechanisms route through BAR-108 and a successor platform/security decision.
   - Field-level sensitivity, encryption, redaction, export, or retention expansion routes through BAR-106 or a successor security/platform decision.
   - Resolver reassignment and auto-resolution remain separate future decisions.
4. Treat shared devices as authentication/session lifecycle pressure first, not as scope authority.
   - Events must remain actor-bound to the authenticated actor.
   - IdP groups, roles, claims, and JWT `actor_id` claims remain non-authority.
5. Treat grace behavior carefully.
   - Accepted stale/offline work is already handled through accept-and-flag semantics.
   - Do not silently authorize stale work, rewrite normal sync watermarks, or make mobile authoritative rejection the platform rule.
6. Use the NW-047 operational UX companion for product-facing terminology only.
   - UX vocabulary must not become sync, assignment, resolver, or scope authority.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-043, NW-048, NW-049, NW-050, and nearby NW-044 through NW-046 rows.
5. `docs/agent-working-surface/assignment-admin-authority-exploration.md`
   - Use it to inherit the NW-043 split: assignment-admin command capability is now resolved by NW-048/NW-050; access exceptions remain separate.
6. `docs/decisions/idr-029-assignment-admin-command-capability.md`
7. `docs/agent-working-surface/operational-ux-layering-companion.md`
   - Use only as a product/UX vocabulary guardrail; it is not authority.
8. `docs/milestone-review/phase-4-review/architecture-rationale-and-routing-companion.md`
   - Read the authority/use rule, routing workflow, irreversibility filter, and configuration anti-pattern guardrails. Do not treat it as decision authority.
9. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-104, BAR-106, BAR-108, and any adjacent deferred/future-decision rows touched by your recommendation.
10. IDRs:
   - `docs/decisions/idr-021-role-action-enforcement-model.md`
   - `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`
   - `docs/decisions/idr-024-multi-axis-assignment-containment.md`
   - `docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`
   - `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
   - `docs/decisions/idr-028-production-principal-binding-administration.md`
   - `docs/decisions/idr-029-assignment-admin-command-capability.md`
11. `docs/implementation/module-interfaces.md`
   - Read `Scope Resolver`, `Assignment Admin Capability Policy`, `Authenticated Actor Resolver`, `Conflict Detector`, `Config Packager`, `Projection Engine`, and sync-related notes.
12. Scenario and pressure sources:
   - `docs/access-control-scenario.md`
   - `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`
   - `docs/scenarios/19-offline-capture-and-sync.md`
   - `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
   - `docs/scenarios/24-long-running-deployment-data-lifecycle.md`
13. Contracts:
   - `contracts/sync-protocol.md`
   - `contracts/flag-catalog.md`
   - `contracts/shapes/assignment_created.schema.json`
   - `contracts/shapes/assignment_ended.schema.json`
   - `contracts/config-package.schema.json` only if your recommendation touches config delivery.
14. Code/test surfaces for inspection only:
   - `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`
   - `server/src/main/java/dev/datarun/server/authorization/AssignmentAdminCapabilityService.java`
   - `server/src/main/java/dev/datarun/server/authorization/AssignmentController.java`
   - `server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java`
   - `server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java`
   - `server/src/main/java/dev/datarun/server/authorization/AuthenticatedActorResolver.java`
   - `server/src/main/java/dev/datarun/server/authorization/PrincipalBindingManifestProvisioner.java`
   - `server/src/main/java/dev/datarun/server/authorization/PrincipalBindingProvisioningRunner.java`
   - `server/src/main/java/dev/datarun/server/authorization/AuthPrincipalBindingRepository.java`
   - `server/src/main/java/dev/datarun/server/sync/SyncController.java`
   - `server/src/main/java/dev/datarun/server/integrity/ConflictResolutionService.java`
   - `server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java`
   - `server/src/test/java/dev/datarun/server/authorization/ProductionAuthIntegrationTest.java`
   - `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`
15. Mobile inspection only if shared-device/session pressure cannot be assessed from server docs:
   - `mobile/lib/data/device_identity.dart`
   - `mobile/lib/data/sync_service.dart`
   - related auth/session tests discovered from those files.
16. Use `scripts/query_cdl.py` only for narrow slices around assignment authority, projected authority, fixed scope mechanisms, sync/access visibility, production-auth non-authority, retention/sensitivity, and mechanism/instance split. Start with:
   - CDL-030
   - CDL-031
   - CDL-032
   - CDL-035
   - CDL-037
   - CDL-046
   - CDL-055

Do not read broad architecture/history docs or old archives unless a directly read source exposes drift that cannot be resolved from this packet.

## Expected Artifact

Create:

```text
docs/agent-working-surface/access-exceptions-shared-device-scope-exploration.md
```

The artifact should include:

1. Executive recommendation:
   - what should stay deferred;
   - what needs a successor IDR/platform decision;
   - what may be implemented inside existing boundaries, if anything;
   - what must not be implemented.
2. Current baseline summary after NW-050:
   - production principal-to-actor binding;
   - assignment-admin command capability;
   - fixed assignment scope mechanisms;
   - live sync and subject-history split;
   - accept-and-flag stale/offline behavior;
   - exact designated-resolver equality.
3. Pressure taxonomy table with at least:
   - auditor/special read access;
   - emergency/special write access;
   - shared-device actor sessions;
   - grace-period behavior;
   - subject-scope variants;
   - query/custom scope pressure;
   - device data expiry or retained local data;
   - IdP group/claim authority requests.
4. For each pressure, record:
   - domain need;
   - current support;
   - authority/sync/projection risk;
   - whether it touches BAR-104, BAR-106, BAR-108, resolver reassignment, auto-resolution, or envelope/schema authority;
   - recommended route.
5. Path comparison:
   - keep current model and defer;
   - model read-only auditor visibility as normal assignments;
   - create a platform-owned special-access mechanism;
   - introduce shared-device session switching without changing scope authority;
   - add grace UX/advisory only;
   - promote new subject/query/custom scope;
   - handle device data expiry as retention/security rather than live-sync authority.
6. Successor routing:
   - add one or more bounded successor backlog rows/prompts only if the artifact identifies a concrete next decision or implementation slice;
   - otherwise record explicit deferral and stop.

Keep the artifact concise enough for future agents to use as a routing source. Prefer tables and direct recommendation bullets over broad essay prose.

## Questions To Answer

Answer these explicitly:

1. Which access-exception pressures are production blockers, and which are product evolution?
2. Can auditor/special read visibility be modeled with existing assignments, or does it require a new platform-owned mechanism?
3. Does any access exception require write authority, and if so why is assignment-admin command capability insufficient?
4. Is shared-device support an authentication/session problem, a device-retention problem, a scope problem, or a combination that must be split?
5. Should grace behavior change authority, or remain accept-and-flag plus advisory UX?
6. Are subject-scope variants sufficient under existing `subject_list`, or is there real pressure for a new scope mechanism?
7. Do query/custom scopes cross the mechanism/instance boundary or create config-as-code risk?
8. How should device data expiry and retained local data route relative to BAR-106, live sync, and subject-history?
9. What tests or probes would prove each recommended successor without overbuilding it?

## Guardrails

- Keep assignment administration outside `activities[*].roles`.
- Keep activity role-actions limited to `capture`, `review`, `alert`, `task_created`, and `task_completed`.
- Preserve NW-050 assignment-admin command capability; do not reopen it.
- Preserve authenticated actor authority from explicit principal binding or dev token lookup.
- Do not infer authority from IdP groups, roles, claims, JWT `actor_id`, request-body actor IDs, UI labels, or scenario vocabulary.
- Preserve authority as projection from assignment history unless a successor decision explicitly changes the model.
- Preserve server-side multi-axis containment from IDR-024.
- Preserve accept-and-flag behavior for structurally valid stale/offline work.
- Preserve exact designated-resolver enforcement from IDR-026; do not add resolver reassignment or auto-resolution.
- Preserve normal live-sync watermarks and the separate subject-history backfill surface.
- Do not add envelope fields, envelope `type` values, `authority_context`, or `assignment_ref`.
- Do not add new scope mechanisms, emergency override semantics, auditor semantics, shared-device semantics, grace-period behavior, report APIs, import/export, or mobile authoritative rejection inside this slice.
- Do not let UX/product vocabulary become authority for synchronization, conflict detection, assignment command authorization, resolver equality, projection correctness, or retention.

## Forbidden Work

- Do not edit runtime code, tests, schemas, migrations, mobile code, UI, contracts, or config-package fields.
- Do not implement access exceptions.
- Do not write an IDR unless the current routing explicitly requires it; prefer successor IDR rows/prompts from this broad exploration.
- Do not change CDL text, accept BAR rows, or resolve FPs.
- Do not mark NW-049 accepted unless the exploration artifact and routing updates are complete.
- Do not combine NW-049 with NW-044 reporting/import-export, NW-045 conflict automation/batch resolution, or NW-046 flag cascade/pattern traversal reporting.
- Do not turn archived examples, scenario terms, or UX vocabulary into current authority.

## Verification

This is documentation/routing work. Run:

```bash
git diff --check
```

If Markdown tables are added, inspect them enough to catch broken row structure.

## Backlog And Status Updates

If the artifact lands:

- Mark NW-049 `accepted`.
- Update `docs/status.md` Current Routing with the recommendation and any successor route.
- Update `docs/agent-working-surface/platform-next-work-backlog.md` with evidence and any successor rows/prompts.
- Do not mark successor rows accepted.

If the exploration concludes a successor decision is required before implementation:

- Add a bounded successor IDR/platform-decision row and prompt.
- Keep implementation rows blocked or absent until the successor decision lands.

If the exploration concludes explicit deferral is best:

- Mark NW-049 `accepted` with the deferral rationale.
- Leave BAR-106/BAR-108 and related future-decision rows active.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
docs(auth): explore access exception boundaries
```

The commit should include only the exploration artifact and tightly related routing updates.

## Stop And Report

Stop and report if:

- current CDL/IDR/BAR/code contradict each other on scope, sync, production-auth, or assignment-admin authority;
- a clean route appears impossible without adding envelope fields or expanding activity role-actions;
- the recommendation would promote IdP group/claim authority;
- the recommendation would require new scope mechanisms but cannot be split into a successor decision;
- shared-device pressure cannot be separated from authority changes;
- implementation work becomes necessary before the exploration can be written.
