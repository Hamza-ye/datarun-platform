# NW-042 Agent Prompt: Add S22 Coordinated Distribution Campaign Runtime Probe

You are working in `/home/hamza/datarun-platform`.

## Goal

Add a constrained S22 scenario-grade runtime probe for coordinated work across grouped locations, using the ITN distribution campaign as the concrete pressure example.

Exit target:

```text
Current assignment/reassignment, subject-history handoff, unit-level capture, transfer_with_acknowledgment/v1, flag/resolution, projection, and sync semantics can support a coordinated distribution campaign without adding new platform primitives or leaking campaign/ITN domain terms into core architecture.
```

This is scenario runtime evidence across accepted baseline rows. It is not a campaign product feature, operational UI slice, reporting API, lifecycle implementation, or successor architecture decision.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-041 through NW-047.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-003, BAR-004, BAR-006, BAR-007, BAR-012, BAR-013, and BAR-014.
6. `docs/implementation/module-interfaces.md`
   - Read `Event Store`, `Projection Engine`, `Conflict Detector`, `Scope Resolver`, `Pattern Registry`, `Trigger Engine`, and `Command Validator`.
7. `docs/checkpoints/checkpoint-2026-06-04-gap-baseline-assessment.md`
   - Read the gap table row for S22, `S22 Pressure Map Addendum`, guardrails, and march orders only.
8. `docs/reviews/scenario-baseline-pressure-map-protocol.md`
9. `docs/scenarios/22-coordinated-distribution-campaign-across-grouped-locations.md`
10. `docs/walk-throughs/itn-distribution-campaign.md`
11. `contracts/flag-catalog.md`
12. `contracts/sync-protocol.md`
13. `contracts/pattern-definition.schema.json`
14. `contracts/patterns/transfer_with_acknowledgment.v1.json`
15. `contracts/fixtures/pattern-state-projection.json`
16. Use `scripts/query_cdl.py` only for CDL-003, CDL-004, CDL-021, CDL-030, CDL-031, CDL-047, CDL-049, CDL-051, CDL-054, CDL-055, and CDL-056 if authority wording needs clarification.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/authorization/
server/src/main/java/dev/datarun/server/event/
server/src/main/java/dev/datarun/server/integrity/
server/src/main/java/dev/datarun/server/projection/
server/src/main/java/dev/datarun/server/sync/
server/src/main/java/dev/datarun/server/subject/
server/src/test/java/dev/datarun/server/authorization/
server/src/test/java/dev/datarun/server/e2e/
server/src/test/java/dev/datarun/server/integrity/
server/src/test/java/dev/datarun/server/projection/
```

Adjust exact paths based on current package layout. Prefer existing scenario/integration-test helpers from `ResponsibilityBindingScenarioIntegrationTest` and the nearby assignment, sync, conflict, transition, and projection tests.

## Scenario Boundary

The probe may use:

- a parent location/village subject and child unit/household subjects;
- one newly discovered unit represented as capture/linkage data only, not full entity lifecycle;
- coordinator/supervisor assignment create/end/reassignment using current assignment authority;
- overlapping field-team work, including offline/stale work that syncs after reassignment;
- deployer-named campaign forms such as unit visit, unit distribution, location status, supply dispatch, receipt, or return;
- existing `transfer_with_acknowledgment/v1` pattern semantics for supply movement;
- existing flag categories such as `domain_uniqueness_violation`, `role_stale`, `temporal_authority_expired`, `transition_violation`, or current equivalents already implemented for the same accepted baseline behavior;
- exact designated-resolver review semantics;
- test-local read-side progress aggregation over existing event/projection/sync outputs.

Translate domain words into current platform constructs:

| Domain term | Use in this probe | Guardrail |
|---|---|---|
| Campaign | Deployment/configured activity set plus assignments and a test-local reporting view. | Not a new event type, scope mechanism, or product module. |
| Village/location progress | Derived aggregation over unit work plus optional human-authored status capture. | Do not store canonical mutable workflow status. |
| Household/unit | Existing Subject or subject-linked capture payload. | Do not implement full create/update/deactivate lifecycle. |
| ITN stock handoff | `transfer_with_acknowledgment/v1` over deployer-named shapes. | No custom custody scope. |
| Completion/follow-up | Human-authored event plus read-side aggregation. | No automatic follow-up trigger. |
| Duplicate visit/distribution | Existing domain uniqueness or conflict flag mechanics. | Do not reject structurally valid offline work. |

## Expected Acceptance Points

Prove the smallest coherent set that covers S22:

1. A coordinator/supervisor creates, ends, or reassigns location/unit work using existing assignment administration. `assignment_changed` remains assignment administration and must not be added to `activities[*].roles`.
2. Two field actors can work overlapping grouped locations/units, including stale/offline work after reassignment. Structurally valid work is persisted and flagged when authority or timing is stale.
3. At least one discovered unit is represented by existing capture/linkage behavior only. Do not add a registry lifecycle feature.
4. Unit visit/distribution events are ordinary `capture` work events under deployer shapes and existing scope/role rules.
5. A supply dispatch/receipt/return path uses `transfer_with_acknowledgment/v1`; an invalid or out-of-order transfer action is accepted and flagged with existing transition semantics.
6. Duplicate unit visit or duplicate distribution pressure emits an existing conflict/domain flag and remains manually reviewable.
7. Unresolved flagged source events are excluded from the relevant projection/aggregate where current projection semantics require exclusion.
8. Exact designated-resolver acceptance re-includes the source event; unresolved, rejected, or non-designated resolution does not.
9. Scoped sync includes in-scope campaign/unit/transfer data and excludes out-of-scope records using current assignment and sync scope axes.
10. Subject-history backfill can provide reassignment/handoff context without mutating normal live-sync watermarks.
11. A test-local progress aggregate can be derived from current event/projection/sync metadata. It may show freshness and unresolved-flag counts, but it must not add a reporting warehouse, dashboard, or new report API.

Prefer one focused server integration test. If current helpers make a split necessary, keep the split small and explain it in the backlog evidence.

## Targeted Tests

Run the new focused test first. If you add it to `ResponsibilityBindingScenarioIntegrationTest`, use a focused method command similar to:

```bash
cd server
./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest#s22CoordinatedDistributionCampaignProbeUsesExistingConstructs test
```

Then run the nearest surrounding server slice:

```bash
cd server
./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest,PatternStateProjectionTest,TransitionViolationIntegrationTest,ConflictResolutionIntegrationTest,AssignmentContainmentIntegrationTest,ScopeFilteredSyncIntegrationTest,SubjectHistoryBackfillIntegrationTest test
```

If class names differ, run the closest scenario, transfer-pattern, transition, resolution, assignment, scope-filtered sync, and subject-history tests and report the exact command.

Do not run Flutter or full Maven unless the implementation touches shared fixtures, contracts, mobile projection/sync behavior, or broad runtime behavior.

## BAR And Backlog Updates

If the S22 runtime probe passes:

- Mark NW-042 `accepted`.
- Attach exact date, commands, and concise evidence summary to the NW-042 backlog row.
- Do not mark new BAR rows accepted. The relevant BAR rows are already accepted.
- Do not start NW-043, NW-044, NW-045, NW-046, or NW-047 in this slice.

If the probe exposes a current baseline bug:

- Leave NW-042 `in_review` or mark it `blocked` with exact file/path/test evidence.
- Add a precise follow-up backlog row only if the fix is outside this prompt's boundary.
- Do not weaken the scenario by silently dropping reassignment, duplicate/offline conflict, transfer, subject-history, scoped sync, resolver, or progress/freshness evidence.

## Forbidden Work

- Do not implement operational UI/UX, product vocabulary, dashboards, reports, or view-model contracts.
- Do not implement NW-047.
- Do not implement entity lifecycle, household registry create/update/deactivate flows, or S06.
- Do not implement trigger execution, automatic follow-up, scheduled work creation, or expected-period gap detection.
- Do not implement auto-resolution, resolver reassignment, batch resolution, or pending-match queues.
- Do not add new scope mechanisms, campaign/custody scope, deployer-defined scope logic, auditor/shared-device behavior, or IdP group/claim authority.
- Do not add envelope fields, event `type` values, shape_ref grammar changes, sync protocol fields, or durable workflow-state tables.
- Do not add a reporting warehouse, analytics store, dashboard, report API, import/export contract, or structured ingest pipeline.
- Do not expand the activity role-action vocabulary; the activity role actions remain `capture`, `review`, `alert`, `task_created`, and `task_completed`.
- Do not make ITN, campaign, village, stock, household, or completion terms platform semantics.
- Do not change contracts or shared fixtures unless you first stop and report concrete drift against accepted behavior.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(workflow): add coordinated campaign scenario probe
```

The commit may include the focused server test, a narrow production fix only if needed to satisfy accepted baseline behavior, and NW-042 backlog evidence. It must not include unrelated contract-schema, mobile, operational-UX, production-auth, reporting-product, trigger, lifecycle, scope, resolver-reassignment, auto-resolution, or architecture edits.

## Stop And Report

Stop and report if:

- proving S22 requires entity lifecycle, trigger execution, automatic follow-up, resolver reassignment, auto-resolution, batch resolution, new scope mechanisms, new envelope fields/types, or a reporting product surface;
- reassignment/handoff continuity cannot be shown with current subject-history or sync semantics;
- duplicate/offline work cannot remain accept-and-flag;
- transfer semantics cannot use `transfer_with_acknowledgment/v1`;
- progress aggregation requires canonical mutable status or a new report API;
- domain terms need to become core platform vocabulary to make the probe pass;
- the probe cannot be implemented without changing current contracts/shared fixtures.
