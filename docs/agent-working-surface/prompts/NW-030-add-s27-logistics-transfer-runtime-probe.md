# NW-030 Agent Prompt: Add S27 Logistics Transfer Runtime Probe

You are working in `/home/hamza/datarun-platform`.

## Goal

Add a constrained S27 scenario-grade runtime probe for “logistics distribution across multiple handoffs.”

This slice should prove domain-agnostic composition:

```text
Non-health logistics handoff events use existing assignment authority, scoped sync, transfer_with_acknowledgment/v1 projection, manual discrepancy review, and existing flag mechanics without adding health-domain assumptions or new platform mechanisms.
```

This is scenario runtime evidence across accepted BAR rows and the V6 domain-agnostic viability pressure. It is not a new baseline capability and not a successor architecture decision.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-023, NW-025 through NW-030.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-006, BAR-007, BAR-012, BAR-013, and BAR-014.
6. `docs/implementation/module-interfaces.md`
   - Read `Conflict Detector`, `Scope Resolver`, `Projection Engine`, `Pattern Registry`, and `Trigger Engine`.
7. `docs/reviews/scenario-baseline-pressure-map.md`
   - Read only the S27 row, S27 selected walkthrough, Test And Backlog Recommendations, and Safe Progress Call.
8. `docs/reviews/viability-closure-review.md`
   - Read V6 and the domain-agnostic tension only.
9. `docs/scenarios/27-logistics-distribution-composite.md`
10. `contracts/flag-catalog.md`
11. `contracts/pattern-definition.schema.json`
12. `contracts/patterns/transfer_with_acknowledgment.v1.json`
13. `contracts/fixtures/pattern-state-projection.json`
14. `contracts/sync-protocol.md`
15. Use `scripts/query_cdl.py` only for CDL-003, CDL-004, CDL-021, CDL-030, CDL-031, CDL-047, CDL-048, CDL-049, CDL-051, CDL-054, and CDL-055 when checking authority wording.

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

Adjust exact paths based on current package layout. Prefer existing transfer pattern/projection, transition, assignment, and sync helpers.

## Scenario Boundary

The probe may use:

- logistics-named deployer shapes such as dispatch, receipt, discrepancy report, and discrepancy resolution;
- existing `transfer_with_acknowledgment/v1` pattern binding/definition;
- current geographic, subject_list, and activity scope axes;
- manual supervisor/steward discrepancy review;
- existing flag categories such as `transition_violation`, `scope_violation`, `domain_uniqueness_violation`, or `concurrent_state_change`.

The probe must not become:

- a custom custody scope mechanism;
- a new envelope field/type proposal;
- an auto-resolution test;
- a trigger/policy side-effect test;
- a deployer-authored state-machine test;
- a production auth-provider test;
- an S06 inventory/entity lifecycle feature.

## Expected Acceptance Points

Prove the smallest coherent set that covers S27:

1. A sender records a dispatch under existing assignment scope.
2. A receiver records a receipt or partial receipt under existing assignment scope.
3. A discrepancy path remains visible and manually reviewable; no automatic cleanup is introduced.
4. `transfer_with_acknowledgment/v1` projection derives expected state from events and excludes unresolved flagged events where applicable.
5. At least one invalid/out-of-order transfer action is accepted and emits an expected existing flag rather than being rejected for state/policy reasons.
6. Scoped sync shows each actor only the transfer/custody data authorized by existing scope axes.
7. No health-domain naming or assumptions are needed in platform semantics.
8. No new envelope fields/types, new scope mechanisms, trigger execution, auto-resolution, resolver reassignment, entity lifecycle, or field-level sensitivity behavior are introduced.

Prefer one focused server integration test. If projection and sync assertions need separate existing harnesses, keep the split small and explain it.

## Targeted Tests

Run the new focused test first.

Then run the nearest existing targeted server slice that proves the surrounding boundary. Use exact class names present in the repository, likely including:

```bash
cd server
./mvnw -Dtest=PatternStateProjectionTest,TransitionViolationIntegrationTest,ConflictResolutionIntegrationTest,AssignmentContainmentIntegrationTest,ScopeFilteredSyncIntegrationTest test
```

If class names differ, run the closest transfer-pattern, transition, resolution, assignment, and sync tests and report the exact command.

Do not run Flutter or full Maven unless the implementation touches shared fixtures, contracts, broad projection behavior, or mobile parity.

## BAR And Backlog Updates

If the S27 runtime probe passes:

- Mark NW-030 `accepted`.
- Attach exact date, command, and concise evidence summary to the NW-030 backlog row.
- Do not mark BAR rows accepted merely because S27 passed; the relevant BAR rows are already accepted.

If the probe exposes a current baseline bug:

- Leave NW-030 `in_review` or mark it `blocked` with exact file/path/test evidence.
- Add a precise follow-up backlog row only if the fix is outside this prompt's boundary.

## Forbidden Work

- Do not implement auto-resolution of discrepancies.
- Do not implement general trigger execution or policy side effects.
- Do not implement resolver reassignment.
- Do not add new envelope fields or event `type` values.
- Do not add new scope mechanisms or deployer-defined scope logic.
- Do not let deployer config author a custom state machine.
- Do not implement S06 inventory/entity lifecycle.
- Do not introduce production OIDC/JWT/Keycloak/group/claim authority.
- Do not change contracts or shared fixtures unless you first stop and report drift.
- Do not make health-domain concepts platform semantics.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(workflow): add logistics transfer scenario probe
```

The commit may include the focused server test, a narrow production fix only if needed to satisfy accepted baseline behavior, and NW-030 backlog evidence. It must not include unrelated scenario, mobile, production-auth, trigger, auto-resolution, resolver-reassignment, contract, or architecture edits.

## Stop And Report

Stop and report if:

- proving S27 requires custom custody scope, new envelope fields/types, deployer-authored state machines, trigger execution, auto-resolution, or resolver reassignment;
- transfer state requires durable workflow-state storage;
- pattern/projection behavior diverges from accepted BAR-012/BAR-014 evidence;
- discrepancy handling cannot remain manual under current exact-resolver semantics;
- domain-agnostic proof requires adding logistics-specific platform semantics.
