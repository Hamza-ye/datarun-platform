# NW-008 Agent Prompt: Verify Sync And Authority Baseline

You are working in `/home/hamza/datarun-platform`.

## Goal

Verify the sync and authority baseline across BAR-003, BAR-004, and BAR-007.

Acceptance targets:

```text
BAR-003: push/pull sync proves idempotent push, ordered pull, pagination/watermark behavior, and scoped delivery.
BAR-004: subject-history backfill proves independent cursoring, per-page authorization, alias behavior, and no normal watermark mutation.
BAR-007: assignment containment and scope-filtered sync prove creator containment, actor-bound assignment commands, three-axis scope filtering, and reassignment behavior.
```

Move a BAR row to `baseline_accepted` only when fresh code inspection or targeted runtime evidence supports that specific row. Partial acceptance is allowed; do not mark all three rows accepted just because one test group passes.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-003, BAR-004, BAR-007, and BAR-015.
6. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-008 only.
7. `docs/implementation/module-interfaces.md`
   - Read `server/event`, `server/sync`, `server/authorization`, and `server/integrity` boundaries.
8. `contracts/sync-protocol.md`
9. `contracts/shapes/assignment_created.schema.json`
10. `contracts/shapes/assignment_ended.schema.json`
11. `docs/decisions/idr-014-materialized-path-locations.md`
12. `docs/decisions/idr-015-scope-filtered-sync-query.md`
13. `docs/decisions/idr-021-role-action-enforcement-model.md`
14. `docs/decisions/idr-023-role-action-domain-boundary-and-assignment-administration.md`
15. `docs/decisions/idr-024-multi-axis-assignment-containment.md`
16. `docs/flagged-positions.md`
    - Read summary table first; open FP-005, FP-006, FP-007, and FP-008 only if needed.
17. Use `scripts/query_cdl.py` only for CDL-010, CDL-021, CDL-030, CDL-031, CDL-034, CDL-037, and CDL-055.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/event/EventRepository.java
server/src/main/java/dev/datarun/server/sync/SyncController.java
server/src/main/java/dev/datarun/server/sync/SubjectHistoryBackfillService.java
server/src/main/java/dev/datarun/server/authorization/ActiveAssignment.java
server/src/main/java/dev/datarun/server/authorization/ScopeResolver.java
server/src/main/java/dev/datarun/server/authorization/AssignmentService.java
server/src/main/java/dev/datarun/server/authorization/AssignmentController.java
server/src/main/java/dev/datarun/server/authorization/SubjectLocationRepository.java
server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java
server/src/test/java/dev/datarun/server/sync/SyncControllerIntegrationTest.java
server/src/test/java/dev/datarun/server/e2e/MultiDeviceE2ETest.java
server/src/test/java/dev/datarun/server/sync/SubjectHistoryBackfillIntegrationTest.java
server/src/test/java/dev/datarun/server/authorization/ScopeFilteredSyncIntegrationTest.java
server/src/test/java/dev/datarun/server/authorization/AssignmentContainmentIntegrationTest.java
server/src/test/java/dev/datarun/server/authorization/ResponsibilityBindingScenarioIntegrationTest.java
server/src/test/java/dev/datarun/server/integrity/AuthFlagIntegrationTest.java
```

## Verification Scope

Prove these points by existing tests, focused new tests, or code inspection plus targeted test evidence:

1. Push is idempotent and does not duplicate client events.
2. Pull returns ordered events after the requested watermark and preserves pagination behavior.
3. Live pull is request-time scoped; it does not become historical/audit pull.
4. Normal live-sync watermarks are not rewritten by subject-history backfill.
5. Subject-history backfill uses an independent cursor and checks authorization on every page.
6. Subject-history backfill covers merge aliases and split lineage where current implementation claims it.
7. Scope filtering composes AND within one assignment and OR across assignments.
8. Scope filtering enforces geographic, subject-list, and activity axes, including null-axis semantics.
9. Assignment create/end commands bind authority to the authenticated actor, not request-body actor IDs.
10. Assignment creation/end containment is enforced across geographic, subject-list, and activity axes.
11. Reassignment expands/contracts sync scope as expected and preserves stale offline work through accept-and-flag behavior.
12. Historical `events.location_path` remains write-time infrastructure metadata and is not rewritten by reparenting or normal sync/backfill paths.

## Expected Work

Start with code inspection and existing targeted tests. If coverage is missing but behavior is correct, add narrowly focused tests in the existing test classes. If behavior is wrong, make a minimal fix only if it stays inside current sync/authorization authority. Stop and report if the fix requires new scope mechanisms, new envelope fields, production auth authority, sync protocol redesign, or historical event rewriting.

If verification passes:

- Update each accepted BAR row in `docs/agent-working-surface/baseline-acceptance-register.md` with exact command, date, and evidence summary.
- Mark NW-008 `accepted` only if BAR-003, BAR-004, and BAR-007 are all accepted.
- If only some rows are accepted, leave NW-008 `ready` or split follow-up rows with precise remaining evidence gaps.

If verification fails:

- Leave affected BAR rows as `baseline_candidate`.
- Add precise backlog follow-up rows with failing behavior, file/test anchor, and exit condition.

## Targeted Tests

Start the test database:

```bash
docker compose -f docker-compose.test.yml up -d test-db
```

Run the sync/authority slice:

```bash
cd server
./mvnw -Dtest=SyncControllerIntegrationTest,MultiDeviceE2ETest,SubjectHistoryBackfillIntegrationTest,ScopeFilteredSyncIntegrationTest,AssignmentContainmentIntegrationTest,ResponsibilityBindingScenarioIntegrationTest,AuthFlagIntegrationTest test
```

If this is too broad to diagnose failures, split the same classes into sync, backfill, and authorization groups. Do not run full Maven or Flutter suites unless the fix crosses into shared projection/config/mobile behavior.

## Guardrails

- Do not change the event envelope or event `type` vocabulary.
- Do not turn live sync into audit or historical pull.
- Do not mutate normal live-sync watermarks from subject-history backfill.
- Do not rewrite historical `events.location_path` after insert, except a controlled NULL backfill explicitly routed through BAR-015.
- Do not introduce production OIDC/JWT/group/claim authority.
- Do not add custom/deployer-defined scope logic or new scope mechanisms.
- Do not reject structurally valid stale/offline work that current architecture says to accept and flag.
- Do not implement resolver reassignment, auto-resolution, trigger execution, or entity lifecycle.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(sync): verify sync authority baseline
```

The commit may include focused server tests, narrow sync/authorization fixes, and BAR/backlog updates. It must not include unrelated scenario, mobile, config, or architecture edits.

## Stop And Report

Stop and report if:

- contracts, IDRs/CDL, and runtime behavior disagree;
- accepting one BAR row would require silently weakening another;
- subject-history backfill is coupled to normal live-sync watermark mutation;
- assignment containment depends on request-body actor identity;
- scope filtering leaks data by ignoring subject-list or activity axes;
- the needed fix crosses into a successor decision surface.
