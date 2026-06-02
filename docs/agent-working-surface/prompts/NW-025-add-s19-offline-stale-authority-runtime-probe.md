# NW-025 Agent Prompt: Add S19 Offline/Stale Authority Runtime Probe

You are working in `/home/hamza/datarun-platform`.

## Goal

Add a constrained S19 scenario-grade runtime probe for “offline capture, stale assignment, and scoped reconciliation.”

This slice should prove the current baseline preserves offline work, accepts-and-flags stale authority, and keeps normal sync scoped and watermark-based.

Exit target:

```text
A structurally valid capture created from an actor's stale offline authority is persisted, flagged for the expected authority/staleness anomaly, and does not expand sync, subject-history, retention, scope, or authorization semantics.
```

This is scenario runtime evidence across existing BAR rows. It is not a new baseline capability and not a successor architecture decision.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-023, NW-024, NW-025, and NW-026.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-002, BAR-003, BAR-004, BAR-007, BAR-008, BAR-013, and BAR-014.
6. `docs/implementation/module-interfaces.md`
   - Read `Event Store`, `Conflict Detector`, `Scope Resolver`, and `Projection Engine`.
7. `docs/reviews/scenario-baseline-pressure-map.md`
   - Read only the S19 row, S19 selected walkthrough, Test And Backlog Recommendations, and Safe Progress Call.
8. `contracts/sync-protocol.md`
9. `contracts/flag-catalog.md`
10. Use `scripts/query_cdl.py` only for CDL-003, CDL-021, CDL-030, CDL-031, CDL-035, and CDL-037 when checking authority wording.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/sync/
server/src/main/java/dev/datarun/server/event/
server/src/main/java/dev/datarun/server/authorization/
server/src/main/java/dev/datarun/server/integrity/
server/src/test/java/dev/datarun/server/sync/
server/src/test/java/dev/datarun/server/e2e/
server/src/test/java/dev/datarun/server/auth/
server/src/test/java/dev/datarun/server/authorization/
server/src/test/java/dev/datarun/server/integrity/
```

Adjust exact paths based on current package layout. Prefer existing sync, assignment, and auth-flag integration test patterns over creating a new harness.

## Scenario Boundary

S19 is cross-cutting, but this first runtime probe must stay narrow.

The probe may use:

- a minimal deployer capture shape or existing test shape;
- a minimal activity reference if needed for role/action or scope behavior;
- existing assignment-created and assignment-ended platform payload shapes;
- existing sync push/pull and subject-history surfaces;
- existing flag categories from `contracts/flag-catalog.md`.

The probe must not become:

- a BAR-010 config-package-delivery test;
- a shape-version coexistence test;
- an expression parity test;
- a workflow/pattern-state test;
- a mobile UI test;
- a production authentication test;
- a new retention/security policy test.

If config setup is needed, keep it as fixture setup for the scenario. Do not treat successful config setup as BAR-010 acceptance.

## Expected Acceptance Points

Prove these points with one focused server integration/runtime test, or with the smallest coherent set if existing harnesses force separation:

1. An initial assignment grants the actor authority for the test subject/activity/scope.
2. The actor has an old sync/pull view or stale assignment state.
3. A supervisor/coordinator ends or narrows the assignment while the actor is effectively offline.
4. The actor pushes a structurally valid capture created under stale authority.
5. The server persists the source event; it does not reject the event for state or policy reasons.
6. The server emits the expected `conflict_detected/v1` authorization/staleness flag.
7. Normal sync pull remains actor-scoped and watermark-based.
8. Subject-history, if exercised, uses its own cursor and does not mutate normal sync watermark.
9. No new envelope fields/types, scope types, auth-provider authority, retention policy, trigger behavior, auto-resolution, or resolver reassignment are introduced.

Prefer asserting one clear stale-authority category first. If multiple categories are naturally emitted by current code, assert the relevant set and explain why the combination is expected under `contracts/flag-catalog.md`.

## Authority And Guardrails

CDL-level constraints:

```text
CDL-003: structurally valid state-stale events are accepted and flagged.
CDL-021: sync transfers immutable events idempotently and by scope.
CDL-030: access is assignment-based.
CDL-031: sync scope equals access scope.
CDL-035: stale authorization work is accepted and flagged; severity controls downstream policy.
CDL-037: scope contraction data handling is device retention policy, not canonical event mutation.
```

Interpretation for this slice:

- Server-side structural validation may reject malformed envelopes or invalid payloads.
- Server-side state/policy checks must not reject otherwise valid offline work.
- Assignment-derived authority remains the authority model.
- Subject-history backfill remains a separate cursor surface.
- Runtime scenario evidence can support confidence in accepted baseline rows, but it does not by itself create a new architecture decision.

## Expected Implementation Boundary

Make the smallest server-test change that composes existing behavior.

Expected work likely includes:

- Add one focused integration test near existing sync/assignment/auth-flag tests.
- Reuse existing helper methods for actor tokens, assignments, sync push/pull, and flag lookup.
- Use existing platform payload shape refs for assignment and conflict events.
- Use an existing deployer capture shape/config fixture if available; otherwise create the minimal test fixture inside the test.
- Assert persistence before or alongside flag assertions.
- Assert normal pull scope/watermark behavior with the same actor or a clearly scoped second actor.

Do not change production code unless the runtime probe reveals a real regression against accepted baseline behavior. If production code must change, keep it inside the existing Event Store, Sync, Scope Resolver, or Conflict Detector boundaries and explain the bug.

## Verification

Run the new focused test first.

Then run the nearest existing targeted server slice that proves the surrounding boundary. Use the exact test class names discovered in the repository, likely including some or all of:

```bash
cd server
./mvnw -Dtest=SubjectHistoryBackfillIntegrationTest,ScopeFilteredSyncIntegrationTest,SyncControllerIntegrationTest,AuthFlagIntegrationTest,AssignmentContainmentIntegrationTest test
```

If class names differ, use the closest existing sync, assignment containment, and auth-flag integration tests and report the exact command.

Do not run full Maven or Flutter unless the implementation touches shared fixtures, contracts, broad sync behavior, mobile retention, or cross-platform projection semantics.

## BAR And Backlog Updates

If the S19 runtime probe passes:

- Mark NW-025 `accepted`.
- Attach the exact command, date, and concise evidence summary to the NW-025 backlog row.
- Leave NW-026 `blocked` unless the steward/user explicitly chooses to open S00 next; do not implement S00 in this slice.
- Do not mark BAR rows accepted merely because S19 passed.
- Add evidence to a BAR row only if this slice actually proves that row's stated exit condition. For example, BAR-002 still requires code inspection proving no operational event update/delete path plus sync idempotent insert evidence.

If the probe exposes a current baseline bug:

- Leave NW-025 `in_review` or mark it `blocked` with the exact blocker.
- Add a precise follow-up backlog row only if the fix is outside this prompt's boundary.
- Do not weaken the scenario or silently narrow assertions to make the test pass.

If the probe cannot be built without deferred/future work:

- Mark NW-025 `blocked` with the deferred/future surface that blocks it.
- Do not implement that surface.

## Forbidden Work

- Do not change contracts or shared fixtures unless you first stop and report the drift.
- Do not add envelope fields or event `type` values.
- Do not add durable workflow-state tables.
- Do not rewrite normal sync watermarks or turn live sync into historical pull.
- Do not implement general trigger execution.
- Do not implement auto-resolution.
- Do not implement resolver reassignment.
- Do not implement S06/entity lifecycle.
- Do not introduce production OIDC/JWT/Keycloak/group/claim authority.
- Do not add field-level sensitivity, encryption, redaction, or new retention policy.
- Do not add new scope mechanisms or deployer-defined scope logic.
- Do not treat mobile advisory checks as authoritative rejection paths.
- Do not start S00, S21, or S27 scenario runtime probes in this slice.
- Do not mark BAR-010 accepted from incidental config fixture setup.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(sync): add stale offline authority scenario probe
```

The commit may include the focused server test, a narrow production fix only if needed to satisfy accepted baseline behavior, and NW-025 backlog evidence. It must not include unrelated scenario, mobile, production-auth, trigger, entity-lifecycle, retention-policy, contract, or architecture edits.

## Stop And Report

Stop and report if:

- current code rejects the stale-but-structured event instead of accepting and flagging it;
- the expected flag category is unclear or conflicts with `contracts/flag-catalog.md`;
- proving S19 requires new scope semantics, production auth-provider authority, field-level sensitivity, trigger execution, auto-resolution, or resolver reassignment;
- config setup expands into BAR-010 package-delivery verification;
- subject-history behavior would require changing normal live-sync watermark semantics;
- the test requires changing contracts, shared fixtures, envelope fields/types, or platform payload shapes.
