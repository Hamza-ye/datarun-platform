# NW-043 Agent Prompt: Explore Assignment-Admin Authority And Access Exceptions

You are working in `/home/hamza/datarun-platform`.

## Goal

Produce a focused exploration artifact for assignment-administration command authority and related access exceptions.

Exit target:

```text
The project has a reconciled, baseline-visible recommendation for how scoped assignment permissions should evolve, without confusing assignment administration with activity role-actions or UI/product vocabulary.
```

This is exploration and routing work only. It must not implement runtime behavior, schemas, APIs, UI, mobile flows, migrations, tests, or accepted platform capability changes.

## Sequence

Preferred march order:

1. Run NW-047 first if any near-term operational UI/product vocabulary work is expected.
2. Then run NW-043 using the NW-047 companion as a guardrail for UX-facing language.
3. Route any resulting implementation/IDR/probe as a successor slice.

If NW-047 has not been accepted yet, NW-043 may still inspect architecture/code and preserve the fresh assignment-admin context, but it must avoid UI-facing vocabulary decisions. If the recommendation depends on user-facing workflow semantics, stop and route NW-047 first.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-041, NW-042, NW-043, and NW-047.
5. If NW-047 is accepted, read:
   - `docs/agent-working-surface/operational-ux-layering-companion.md`
6. `docs/checkpoints/checkpoint-2026-06-04-gap-baseline-assessment.md`
   - Read only assignment administration, access exceptions, authorization staleness, subject scope, shared device, S22, and operational UX guardrail material.
7. IDRs:
   - `docs/decisions/idr-021-role-action-enforcement-model.md`
   - `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`
   - `docs/decisions/idr-024-multi-axis-assignment-containment.md`
   - `docs/decisions/idr-026-conflict-resolver-routing-and-single-writer-resolution.md`
   - `docs/decisions/idr-027-production-auth-principal-actor-binding.md`
   - `docs/decisions/idr-028-production-principal-binding-administration.md`
8. `docs/implementation/module-interfaces.md`
   - Read `Command Validator`, `Scope Resolver`, `Authenticated Actor Resolver`, `Event Store`, `Projection Engine`, `Conflict Detector`, `Sync Controller`, and assignment/authorization module notes.
9. Scenario and pressure sources:
   - `docs/access-control-scenario.md`
   - `docs/scenarios/25-worker-onboarding-transfer-and-exit.md`
   - `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md` only for assignment-admin pressure already probed by NW-042.
10. Exploration archives for provenance only:
   - `docs/exploration/archive/10-adr3-phase1-policy-scenarios.md`
     - Focus: coordinator assignment authority, reassignment while offline, subject-history handoff, scope axes, campaign/time-bound assignment authority, resolver designation, assignment as atomic authorization grant.
   - `docs/exploration/archive/11-adr3-phase2-stress-test.md`
     - Focus: misconfigured out-of-scope assignment, role hierarchy/permission-table pressure, concurrent grants, device-side enforcement bypass, no silent permission elevation/persistence, missing reassignment/referral/offline coordinator paths.
   - `docs/exploration/archive/12-adr3-course-correction.md`
     - Focus: no envelope `authority_context`, authority as projection from assignment timeline, server-side scope containment, conflict resolution online-only, auditor visibility as deferred/evolvable.
   - `docs/exploration/archive/15-adr4-session3-part1-structural-coherence.md`
     - Focus: device/server assignment resolution, server-only scope computation, patterns not providing role/scope bindings, fixed platform scope types.
11. Contracts:
   - `contracts/sync-protocol.md`
   - `contracts/flag-catalog.md`
   - `contracts/shapes/assignment_created.schema.json`
   - `contracts/shapes/assignment_ended.schema.json`
12. Code/test surfaces for inspection only:
   - `server/src/main/java/dev/datarun/server/authorization/AssignmentService.java`
   - `server/src/main/java/dev/datarun/server/authorization/AssignmentController.java`
   - `server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java`
   - `server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java`
   - `server/src/main/java/dev/datarun/server/sync/SyncController.java`
   - `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java`
   - `server/src/main/java/dev/datarun/server/integrity/ConflictResolutionService.java`
   - `server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java`
   - `server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java`
13. Use `scripts/query_cdl.py` only for narrow CDL slices around assignment, scope, accept-and-flag, authority as projection, mechanism/instance split, production auth non-authority, sync visibility, and activity role-action boundaries.

Do not read broad architecture/history docs unless a directly read source routes you there.

## Expected Artifact

Create:

```text
docs/agent-working-surface/artifacts/NW-043-assignment-admin-authority-exploration.md
```

The artifact should include:

1. A direct answer on whether current assignment administration outside activity role-actions leaves a clean model for scoped assignment permissions.
2. A current implemented-model summary:
   - assignment command authority;
   - activity role-action authority;
   - subject/geographic/activity scope;
   - scoped sync inclusion and subject-history handoff;
   - conflict resolver inclusion/exclusion;
   - production principal binding non-authority for groups/claims/request bodies.
3. An archive-to-current reconciliation table:
   - source;
   - archive claim or pressure;
   - current status;
   - remaining gap;
   - route.
4. A risk split across:
   - authorization;
   - projection;
   - offline sync;
   - reassignment;
   - resolver inclusion;
   - access exceptions;
   - UX semantics.
5. A path comparison. At minimum compare:
   - keep current containment-only assignment administration for now;
   - add a separate scoped assignment-admin command capability outside `activities[*].roles`;
   - attach command capabilities to assignment/config policy while preserving axis containment;
   - central/provisioning-only administration with no field assignment-admin UI;
   - route access exceptions separately, such as auditor access, shared device, grace periods, subject-scope variants, and new scope mechanisms.
6. A recommendation:
   - defer;
   - produce an IDR;
   - run a runtime probe;
   - create a bounded implementation prompt;
   - or split into multiple successors.
7. Concrete successor rows/prompts to add only if the recommendation needs them.

Keep the artifact concise enough for future agents to use as a routing source. Prefer tables and short decision bullets over broad essay prose.

## Questions To Answer

Answer these explicitly:

1. Does keeping assignment administration outside activity roles make scoped assignment permissions harder to model?
2. Does the current containment-only model suffice for near-term field responsibility management?
3. If not, what is the minimal command-authority vocabulary or policy shape, and where should it live?
4. What must remain outside the model: `activities[*].roles`, IdP claims/groups, request-body actor IDs, envelope fields, resolver reassignment, and UI vocabulary?
5. Which access-exception concerns are part of assignment-admin authority, and which should remain separate future decisions?
6. What tests or probes would prove the recommended path without overbuilding it?
7. What operational UX terms, if any, must wait for NW-047 guidance?

## Guardrails

- `assignment_changed` remains outside the activity role-action vocabulary.
- Activity role-actions remain only the IDR-023 bounded set: `capture`, `review`, `alert`, `task_created`, and `task_completed`.
- Assignment command authority must not be inferred from IdP groups, JWT actor claims, request-body actors, or UI labels.
- Preserve authority-as-projection from assignment history; do not add envelope `authority_context` or `assignment_ref`.
- Preserve server-side multi-axis assignment containment from IDR-024.
- Preserve accept-and-flag behavior for structurally valid stale/offline work.
- Preserve exact designated-resolver enforcement from IDR-026; do not add resolver reassignment or auto-resolution.
- Preserve normal live-sync watermarks and the separate subject-history backfill surface.
- Do not create new scope mechanisms, auditor role semantics, shared-device semantics, grace-period behavior, reporting API, import/export, or mobile authoritative rejection unless routed as separate successor decisions.
- Do not let UX/product vocabulary become authority for synchronization, conflict detection, assignment command authorization, resolver equality, or projection correctness.

## Forbidden Work

- Do not edit runtime code, tests, schemas, migrations, mobile code, UI, contracts, or config-package fields.
- Do not write a decision artifact, change CDL text, accept BAR rows, or resolve FPs in this slice.
- Do not mark NW-043 accepted unless the exploration artifact and routing updates are complete.
- Do not turn archive material into current authority. Reconcile it against current CDL/IDR/BAR/code and route only the remaining gaps.
- Do not combine the exploration with implementation for the sake of speed.

## Verification

This is documentation/routing work. Run:

```bash
git diff --check
```

If Markdown tables are added, inspect them enough to catch broken row structure.

## Backlog And Status Updates

If the artifact lands:

- Mark NW-043 `accepted`.
- Update `docs/status.md` Current Routing with the recommendation and any successor route.
- Update `docs/agent-working-surface/platform-next-work-backlog.md` with evidence and any successor rows.
- Do not mark NW-047 accepted unless NW-047 itself was completed.

If the exploration concludes a decision is required before implementation:

- Add a bounded successor row for the IDR/probe/implementation handoff.
- Keep that successor separate from NW-043.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
docs(auth): explore assignment admin authority
```

The commit should include only the exploration artifact and tightly related routing updates.

## Stop And Report

Stop and report if:

- current CDL/IDR/BAR/code contradict each other on assignment command authority;
- a clean model appears impossible without adding envelope fields or activity role-action expansion;
- the recommendation depends on operational UX/product vocabulary and NW-047 is not accepted;
- the analysis would need to promote new runtime authority rather than route a successor decision;
- the work cannot separate assignment command authority from access exceptions cleanly.
