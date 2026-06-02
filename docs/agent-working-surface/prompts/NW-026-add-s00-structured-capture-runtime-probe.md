# NW-026 Agent Prompt: Add S00 Structured Capture Runtime Probe

You are working in `/home/hamza/datarun-platform`.

## Goal

Add a focused S00 scenario-grade runtime probe for “structured capture with offline correction.”

This slice should prove the simplest trustworthy-record baseline: a valid structured capture is stored as an immutable event, a correction/amendment is stored as another event rather than a mutation, duplicate/idempotent push behavior is stable, and any selected duplicate/concurrency anomaly is surfaced through existing flag mechanics.

Exit target:

```text
Structured capture and correction preserve the 11-field envelope, append events instead of mutating existing records, remain idempotent across sync push, and use existing flag categories for configured duplicate/concurrent anomalies without adding contracts or workflow dependencies.
```

This is scenario runtime evidence across existing BAR rows. It is not a new baseline capability and not a successor architecture decision.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-023, NW-025, and NW-026.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-001, BAR-002, BAR-005, BAR-010, and BAR-013.
6. `docs/implementation/module-interfaces.md`
   - Read `Event Store`, `Platform Payload Contracts`, `Conflict Detector`, `Shape Registry`, and `Config Packager`.
7. `docs/reviews/scenario-baseline-pressure-map.md`
   - Read only the S00 row, S00 selected walkthrough, Test And Backlog Recommendations, and Safe Progress Call.
8. `contracts/envelope.schema.json`
9. `contracts/sync-protocol.md`
10. `contracts/flag-catalog.md`
11. Use `scripts/query_cdl.py` only for CDL-001, CDL-003, CDL-006, CDL-007, CDL-008, CDL-019, and CDL-021 when checking authority wording.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/event/
server/src/main/java/dev/datarun/server/sync/
server/src/main/java/dev/datarun/server/integrity/
server/src/main/java/dev/datarun/server/config/
server/src/test/java/dev/datarun/server/sync/
server/src/test/java/dev/datarun/server/event/
server/src/test/java/dev/datarun/server/integrity/
server/src/test/java/dev/datarun/server/e2e/
```

Adjust exact paths based on current package layout. Prefer existing sync/event/integrity integration test patterns over creating a new harness.

## Scenario Boundary

S00 is the simplest baseline probe. Keep it that way.

The probe may use:

- a minimal deployer capture shape or existing test shape;
- an optional activity reference if an existing test fixture already uses one;
- two capture events on the same subject to model original capture plus correction/amendment;
- an existing detector path for duplicate/concurrent behavior if it can be exercised without broad setup;
- existing sync push/pull behavior.

The probe must not become:

- a BAR-010 config-package-delivery acceptance test;
- a shape-version migration/coexistence test unless existing fixture setup already proves it incidentally;
- a workflow/pattern-state test;
- a subject lifecycle or registry-maintenance test;
- a mobile UI or retention test;
- a production authentication test.

If config setup is needed, keep it as fixture setup for the scenario. Do not treat successful config setup as BAR-010 acceptance.

## Expected Acceptance Points

Prove these points with one focused server integration/runtime test, or with the smallest coherent set if existing harnesses force separation:

1. A structurally valid `capture` event with a deployer shape is accepted and persisted.
2. A correction/amendment is represented by a second event, not by updating the original event.
3. The original event remains present and unchanged after the correction/amendment.
4. Re-pushing the same event id is idempotent and does not create a duplicate stored event.
5. Pull returns events ordered by server sync watermark.
6. If the probe exercises duplicate/concurrent anomaly behavior, the server emits the expected `conflict_detected/v1` flag using an existing flag category.
7. Invalid structure, if checked in this probe, fails before persistence as structural validation, not as state/policy rejection.
8. No new envelope fields/types, shape-ref grammar, workflow state, entity lifecycle, trigger behavior, auto-resolution, resolver reassignment, or scope semantics are introduced.

For the anomaly assertion, prefer the lowest-overhead existing detector path. `concurrent_state_change` is acceptable if the current test harness can model stale last-pull state. `domain_uniqueness_violation` is acceptable only if existing deployer-shape uniqueness setup is already straightforward. Do not add new detector semantics for this probe.

## Authority And Guardrails

CDL-level constraints:

```text
CDL-001: operational truth lives in the immutable event stream.
CDL-003: valid state-stale events are accepted and flagged.
CDL-006: the canonical event envelope has eleven fields.
CDL-007: envelope type is a closed six-value vocabulary.
CDL-008: shape_ref identifies payload schema and domain fact.
CDL-019: atomic write unit is a typed immutable event.
CDL-021: sync transfers immutable events idempotently and by scope.
```

Interpretation for this slice:

- Corrections append; they do not patch existing operational events.
- Domain meaning belongs in `shape_ref` and payload, not a new event `type`.
- State/policy anomalies are accepted and flagged after structural validation.
- Runtime scenario evidence can support confidence in accepted baseline rows, but it does not by itself create a new architecture decision.

## Expected Implementation Boundary

Make the smallest server-test change that composes existing behavior.

Expected work likely includes:

- Add one focused integration test near existing sync/event/integrity tests.
- Reuse existing helper methods for sync push/pull and event lookup.
- Use an existing deployer test shape/config fixture if available; otherwise create the minimal test fixture inside the test.
- Assert event count and event payload/envelope fields before and after correction.
- Assert idempotent push by event id.
- Add one anomaly assertion only if it can be exercised through existing detector behavior without broad setup.

Do not change production code unless the runtime probe reveals a real regression against accepted baseline behavior. If production code must change, keep it inside the existing Event Store, Sync, or Conflict Detector boundaries and explain the bug.

## Verification

Run the new focused test first.

Then run the nearest existing targeted server slice that proves the surrounding boundary. Use the exact test class names discovered in the repository, likely including some or all of:

```bash
cd server
./mvnw -Dtest=SyncControllerIntegrationTest,ConflictDetectorIntegrationTest,DomainUniquenessIntegrationTest,EnvelopeVocabularyTest,EnvelopeSchemaParityTest test
```

If class names differ, use the closest existing sync, event/envelope, and integrity integration tests and report the exact command.

Do not run full Maven or Flutter unless the implementation touches shared fixtures, contracts, broad sync behavior, mobile retention, or cross-platform projection semantics.

## BAR And Backlog Updates

If the S00 runtime probe passes:

- Mark NW-026 `accepted`.
- Attach the exact command, date, and concise evidence summary to the NW-026 backlog row.
- Do not mark BAR rows accepted merely because S00 passed.
- Add evidence to a BAR row only if this slice actually proves that row's stated exit condition. For example, BAR-002 still requires code inspection proving no operational event update/delete path plus sync idempotent insert evidence, and BAR-010 still requires deploy-time validation, package atomicity, version coexistence, pattern delivery, and unknown-key behavior.

If the probe exposes a current baseline bug:

- Leave NW-026 `in_review` or mark it `blocked` with the exact blocker.
- Add a precise follow-up backlog row only if the fix is outside this prompt's boundary.
- Do not weaken the scenario or silently narrow assertions to make the test pass.

If the probe cannot be built without deferred/future work:

- Mark NW-026 `blocked` with the deferred/future surface that blocks it.
- Do not implement that surface.

## Forbidden Work

- Do not change contracts or shared fixtures unless you first stop and report the drift.
- Do not add envelope fields or event `type` values.
- Do not change `shape_ref` grammar.
- Do not mutate existing operational events to model correction.
- Do not add durable workflow-state tables.
- Do not implement general trigger execution.
- Do not implement auto-resolution.
- Do not implement resolver reassignment.
- Do not implement S06/entity lifecycle.
- Do not introduce production OIDC/JWT/Keycloak/group/claim authority.
- Do not add field-level sensitivity, encryption, redaction, or new retention policy.
- Do not add new scope mechanisms or deployer-defined scope logic.
- Do not start S19, S21, or S27 scenario runtime probes in this slice.
- Do not mark BAR-010 accepted from incidental config fixture setup.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(sync): add structured capture scenario probe
```

The commit may include the focused server test, a narrow production fix only if needed to satisfy accepted baseline behavior, and NW-026 backlog evidence. It must not include unrelated scenario, mobile, production-auth, trigger, entity-lifecycle, retention-policy, contract, or architecture edits.

## Stop And Report

Stop and report if:

- current code mutates or deletes the original event to model correction;
- idempotent push cannot be proven without changing event identity semantics;
- the expected anomaly category is unclear or conflicts with `contracts/flag-catalog.md`;
- proving S00 requires entity lifecycle, workflow state, trigger execution, auto-resolution, resolver reassignment, production auth-provider authority, or new scope semantics;
- config setup expands into BAR-010 package-delivery verification;
- the test requires changing contracts, shared fixtures, envelope fields/types, shape-ref grammar, or platform payload shapes.
