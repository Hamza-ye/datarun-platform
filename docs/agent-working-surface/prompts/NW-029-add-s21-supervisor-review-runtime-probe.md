# NW-029 Agent Prompt: Add S21 Supervisor Review Runtime Probe

You are working in `/home/hamza/datarun-platform`.

## Goal

Add a constrained S21 scenario-grade runtime probe for “supervisor visit and scoped review.”

This slice should prove a real-world supervisor review composition without promoting deferred behavior:

```text
A field worker creates scoped work; a supervisor with current assignment scope reviews or assesses it through existing review/pattern mechanics; valid review state is projection-derived, invalid review/state cases are accepted-and-flagged, and scoped visibility/subject-history boundaries remain intact.
```

This is scenario runtime evidence across accepted BAR rows. It is not a new baseline capability and not a successor architecture decision.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-023, NW-025 through NW-029.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-006, BAR-007, BAR-012, BAR-013, and BAR-014.
6. `docs/implementation/module-interfaces.md`
   - Read `Conflict Detector`, `Scope Resolver`, `Projection Engine`, `Pattern Registry`, `Trigger Engine`, and `Command Validator (Advisory Only)`.
7. `docs/reviews/scenario-baseline-pressure-map.md`
   - Read only the S21 row, S21 selected walkthrough, Test And Backlog Recommendations, and Safe Progress Call.
8. `docs/scenarios/21-chv-supervisor-operations.md`
9. `contracts/flag-catalog.md`
10. `contracts/pattern-definition.schema.json`
11. `contracts/patterns/capture_with_review.v1.json`
12. `contracts/fixtures/pattern-state-projection.json`
13. `contracts/sync-protocol.md`
14. Use `scripts/query_cdl.py` only for CDL-003, CDL-004, CDL-021, CDL-030, CDL-031, CDL-047, CDL-048, CDL-049, CDL-051, and CDL-054 when checking authority wording.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/authorization/
server/src/main/java/dev/datarun/server/integrity/
server/src/main/java/dev/datarun/server/projection/
server/src/main/java/dev/datarun/server/sync/
server/src/test/java/dev/datarun/server/authorization/
server/src/test/java/dev/datarun/server/integrity/
server/src/test/java/dev/datarun/server/projection/
server/src/test/java/dev/datarun/server/e2e/
```

Adjust exact paths based on current package layout. Prefer existing responsibility-binding, transition, projection, and sync test helpers.

## Scenario Boundary

The probe may use:

- a field-worker assignment scoped to a subject/location/activity;
- a supervisor/reviewer assignment scoped to the same work;
- a minimal deployer capture shape and review/assessment shape or existing test shapes;
- existing `capture_with_review/v1` pattern binding/definition;
- existing conflict flag categories such as `transition_violation`, `scope_violation`, `role_stale`, or `concurrent_state_change`;
- subject-history only if the scenario needs newly assigned supervisor history repair.

The probe must not become:

- a general trigger/late-reminder test;
- an auto-resolution test;
- a resolver-reassignment test;
- a production OIDC/JWT/group/claim authority test;
- an S25 worker transfer/exit probe;
- a mobile UI/advisory test;
- a config-package acceptance test.

## Expected Acceptance Points

Prove the smallest coherent set that covers S21:

1. A field worker creates scoped work under current assignment authority.
2. A supervisor can see/retrieve only scoped source work needed for review.
3. A supervisor review/assessment event is accepted and participates in `capture_with_review/v1` projection-derived state.
4. At least one invalid review/state case is accepted and emits an expected existing flag rather than being rejected for state/policy reasons.
5. Unresolved flagged source/review events are excluded from relevant projection state, and accepted/rejected resolution semantics remain those already implemented.
6. If subject-history is exercised, it uses its own cursor and does not mutate normal live-sync watermark.
7. Exact designated-resolver semantics are preserved; no resolver reassignment or auto-resolution is introduced.
8. No new envelope fields/types, scope mechanisms, trigger behavior, production auth authority, entity lifecycle, or field-level sensitivity behavior are introduced.

Prefer one focused server integration test. If the existing test architecture naturally separates projection and sync assertions, keep the split small and explain it.

## Targeted Tests

Run the new focused test first.

Then run the nearest existing targeted server slice that proves the surrounding boundary. Use exact class names present in the repository, likely including:

```bash
cd server
./mvnw -Dtest=ResponsibilityBindingScenarioIntegrationTest,PatternStateProjectionTest,TransitionViolationIntegrationTest,ConflictResolutionIntegrationTest,ScopeFilteredSyncIntegrationTest test
```

If class names differ, run the closest responsibility-binding/review, pattern-state, transition, resolution, and sync tests and report the exact command.

Do not run Flutter or full Maven unless the implementation touches shared fixtures, contracts, broad projection behavior, or mobile parity.

## BAR And Backlog Updates

If the S21 runtime probe passes:

- Mark NW-029 `accepted`.
- Attach exact date, command, and concise evidence summary to the NW-029 backlog row.
- Do not mark BAR rows accepted merely because S21 passed; the relevant BAR rows are already accepted.

If the probe exposes a current baseline bug:

- Leave NW-029 `in_review` or mark it `blocked` with exact file/path/test evidence.
- Add a precise follow-up backlog row only if the fix is outside this prompt's boundary.

## Forbidden Work

- Do not implement general trigger execution, late reminders, or escalation.
- Do not implement auto-resolution.
- Do not implement resolver reassignment.
- Do not introduce production OIDC/JWT/Keycloak/group/claim authority.
- Do not implement S25 worker transfer/exit.
- Do not add envelope fields, event types, or scope mechanisms.
- Do not add durable workflow-state tables.
- Do not change contracts or shared fixtures unless you first stop and report drift.
- Do not make mobile advisory behavior authoritative.
- Do not start S27 in this slice.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(workflow): add supervisor review scenario probe
```

The commit may include the focused server test, a narrow production fix only if needed to satisfy accepted baseline behavior, and NW-029 backlog evidence. It must not include unrelated scenario, mobile, production-auth, trigger, auto-resolution, resolver-reassignment, contract, or architecture edits.

## Stop And Report

Stop and report if:

- proving S21 requires trigger execution, auto-resolution, resolver reassignment, production auth claims, new scope mechanisms, or new envelope fields/types;
- review state requires durable workflow-state storage;
- pattern/projection behavior diverges from accepted BAR-012/BAR-014 evidence;
- resolver semantics conflict with BAR-006 or `contracts/flag-catalog.md`;
- subject-history behavior would require changing normal live-sync watermark semantics.
