# NW-033 Agent Prompt: Add S26 Reporting/Aggregate Runtime Probe

You are working in `/home/hamza/datarun-platform`.

## Goal

Add a constrained S26 scenario-grade runtime probe for operational reporting and aggregate oversight.

Exit target:

```text
Current projection, sync, and flag semantics can support operational aggregate oversight: report inputs carry freshness, unresolved-flag treatment is visible, scoped views do not leak out-of-scope records, and drill-back traceability remains event-based, without adding a reporting warehouse or new analytics subsystem.
```

This is scenario runtime evidence across accepted baseline rows. It is not a reporting product phase and not a successor architecture decision.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-023, NW-031, NW-032, and NW-033.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-006, BAR-007, BAR-012, BAR-013, and BAR-014.
6. `docs/implementation/module-interfaces.md`
   - Read `Projection Engine`, `Conflict Detector`, `Scope Resolver`, `Event Store`, and `Trigger Engine`.
7. `docs/reviews/scenario-baseline-pressure-map.md`
   - Read only the S26 row, deferred-surface guardrails for S26, Test And Backlog Recommendations, and Safe Progress Call.
8. `docs/reviews/viability-closure-review.md`
   - Read the reporting/aggregate oversight blind spot only.
9. `docs/scenarios/26-operational-reporting-and-aggregate-oversight.md`
10. `contracts/flag-catalog.md`
11. `contracts/sync-protocol.md`
12. `contracts/fixtures/pattern-state-projection.json`
13. Use `scripts/query_cdl.py` only for CDL-003, CDL-004, CDL-021, CDL-030, CDL-031, CDL-047, CDL-051, CDL-054, and CDL-055 if authority wording needs clarification.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/subject/
server/src/main/java/dev/datarun/server/event/
server/src/main/java/dev/datarun/server/sync/
server/src/main/java/dev/datarun/server/authorization/
server/src/main/java/dev/datarun/server/integrity/
server/src/main/java/dev/datarun/server/projection/
server/src/test/java/dev/datarun/server/authorization/
server/src/test/java/dev/datarun/server/integrity/
server/src/test/java/dev/datarun/server/projection/
server/src/test/java/dev/datarun/server/e2e/
```

Adjust exact paths based on current package layout. Prefer existing scenario/integration-test helpers.

## Scenario Boundary

The probe may use:

- existing `SubjectProjection` summaries, event timelines, pattern-state projection, conflict/flag APIs, sync pull, and subject-history/backfill surfaces;
- test-local aggregation over existing projection/sync outputs to prove reporting semantics;
- existing assignment scope axes for scoped visibility;
- existing unresolved/accepted/rejected flag exclusion semantics;
- existing event timestamps, sync watermarks, pull response fields, and summary timestamps as freshness inputs.

The probe must not become:

- a reporting warehouse, analytics store, dashboard, or new report API;
- a trigger, scheduled reporting job, or automatic missing-work detector;
- an auto-resolution or resolver-reassignment slice;
- a production auth-provider or multi-audience view feature;
- a new scope mechanism, event type, envelope field, or durable workflow-state table.

If a user-facing reporting endpoint appears necessary, stop and report. This slice should prove current platform semantics and gaps, not design a reporting product.

## Expected Acceptance Points

Prove the smallest coherent set that covers S26:

1. Aggregate inputs expose freshness: event timestamps, latest projected timestamps, sync watermarks, or pull metadata let a report distinguish stale data from live truth.
2. Unresolved flagged source events are excluded from projected state/aggregate counts where current projection semantics require exclusion.
3. Unresolved flag counts or conflict-list evidence remain visible so questionable records are not silently hidden.
4. Exact designated-resolver acceptance re-includes the source event in projection/aggregate semantics; non-accepted or unresolved flags do not.
5. Scoped views use existing assignment/sync scope and do not leak out-of-scope records to a viewer. Do not claim `/api/subjects` is a production scoped-reporting endpoint unless the current code already enforces that.
6. Drill-back traceability remains event-based: a high-level subject/flag/report count can be traced back to source events and flag/resolution events.
7. No reporting warehouse, analytics storage, trigger execution, auto-resolution, resolver reassignment, production auth claims, new scope mechanisms, or contract changes are introduced.

Prefer one focused server integration test. If current APIs force separate projection/flag/sync tests, keep the split small and explain it in the backlog evidence.

## Targeted Tests

Run the new focused test first.

Then run the nearest surrounding server slice:

```bash
cd server
./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest,PatternStateProjectionTest,ConflictResolutionIntegrationTest,TransitionViolationIntegrationTest,ScopeFilteredSyncIntegrationTest test
```

If class names differ, run the closest projection, flag resolution, transition, and scope-filtered sync tests and report the exact command. Do not run Flutter or full Maven unless the implementation touches shared fixtures, mobile projection, contracts, or broad sync behavior.

## BAR And Backlog Updates

If the S26 runtime probe passes:

- Mark NW-033 `accepted`.
- Attach exact date, commands, and concise evidence summary to the NW-033 backlog row.
- Do not mark BAR rows accepted merely because S26 passed; the relevant BAR rows are already accepted.
- Do not start NW-034 in this slice.

If the probe exposes a current baseline bug:

- Leave NW-033 `in_review` or mark it `blocked` with exact file/path/test evidence.
- Add a precise follow-up backlog row only if the fix is outside this prompt's boundary.
- Do not weaken the scenario by silently dropping freshness, unresolved-flag visibility, scoped visibility, or drill-back evidence.

## Forbidden Work

- Do not implement a reporting warehouse, analytics storage model, dashboard, or new report API.
- Do not implement scheduled reporting, expected-period gap detection, or trigger execution.
- Do not implement auto-resolution or resolver reassignment.
- Do not add envelope fields, event `type` values, sync protocol fields, durable workflow-state tables, or new scope mechanisms.
- Do not introduce production OIDC/JWT/Keycloak/group/claim authority or S15 multi-audience views.
- Do not implement field-level sensitivity/encryption/redaction or S06/entity lifecycle.
- Do not change contracts or shared fixtures unless you first stop and report concrete drift against accepted behavior.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(reporting): add aggregate oversight scenario probe
```

The commit may include focused server tests, a narrow production fix only if needed to satisfy accepted baseline behavior, and NW-033 backlog evidence. It must not include unrelated contract-schema, production-auth, reporting-product, trigger, lifecycle, mobile, or architecture edits.

## Stop And Report

Stop and report if:

- proving S26 requires a new report endpoint, reporting warehouse, analytics store, trigger engine, new scope mechanism, or production auth-provider authority;
- scoped aggregate visibility cannot be shown without changing public API authorization;
- unresolved-flag treatment contradicts accepted BAR-006/BAR-012/BAR-013/BAR-014 evidence;
- traceability requires a new envelope reference field or durable workflow/report table;
- freshness cannot be represented from current event/projection/sync metadata.
