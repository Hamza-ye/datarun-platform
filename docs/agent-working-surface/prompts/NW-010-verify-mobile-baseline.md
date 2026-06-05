# NW-010 Agent Prompt: Verify Mobile Baseline

You are working in `/home/hamza/datarun-platform`.

## Goal

Verify the mobile baseline across BAR-008, BAR-011, BAR-012 mobile corroboration, and BAR-014 mobile/shared-fixture equivalence.

Acceptance targets:

```text
BAR-008: mobile selective retention proves own events are retained and out-of-scope other-device events are purged according to current policy.
BAR-011: mobile expression evaluator proves shared fixture parity and bounded evaluator behavior.
BAR-012: mobile pattern projection proves packaged pattern definitions are preserved and the mobile projection matches current accepted pattern-state semantics.
BAR-014: mobile/server projection equivalence proves shared fixtures pass on mobile now that server/shared-fixture evidence is attached from NW-009.
```

BAR-009, BAR-012, and BAR-013 were accepted by NW-009 on 2026-06-02 for the server projection/integrity side. This slice should not reopen server acceptance unless mobile evidence exposes a real divergence.

Move a BAR row to `baseline_accepted` only when fresh mobile test evidence supports that specific row. BAR-014 can move to `baseline_accepted` only if the mobile shared-fixture tests pass and remain aligned with the server evidence already attached by NW-009.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/architecture-rationale-and-routing-companion.md`
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-008, BAR-011, BAR-012, BAR-014, and related rows BAR-010 and BAR-013.
6. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-009 and NW-010 only.
7. `docs/implementation/module-interfaces.md`
   - Read `Projection Engine`, `Pattern Registry`, `Command Validator (Advisory Only)`, and contract guard references.
8. `docs/reviews/scenario-baseline-pressure-map.md`
   - Read only the safe progress call and rows for S19, S21, and S27 so you understand why scenario probes wait.
9. `contracts/fixtures/expression-evaluation.json`
10. `contracts/fixtures/projection-equivalence.json`
11. `contracts/fixtures/pattern-state-projection.json`
12. `contracts/pattern-definition.schema.json`
13. `contracts/patterns/*.json`
14. `contracts/sync-protocol.md`
15. `docs/decisions/idr-018-expression-grammar.md`
16. `docs/decisions/idr-019-config-package.md`
17. `docs/decisions/idr-020-pattern-state-machine-representation.md`
18. `docs/decisions/idr-025-pattern-definition-contract-and-delivery.md`
19. Use `scripts/query_cdl.py` only for CDL-002, CDL-037, CDL-043, CDL-046, CDL-047 through CDL-052 when an authority point needs clarification.

Implementation and test files to inspect:

```text
mobile/lib/data/config_store.dart
mobile/lib/data/context_resolver.dart
mobile/lib/data/event_assembler.dart
mobile/lib/data/event_store.dart
mobile/lib/data/pattern_projection.dart
mobile/lib/data/projection_engine.dart
mobile/lib/data/sync_service.dart
mobile/lib/domain/event.dart
mobile/lib/domain/expression_evaluator.dart
mobile/lib/domain/pattern_state.dart
mobile/test/config_store_test.dart
mobile/test/context_resolver_test.dart
mobile/test/event_assembler_test.dart
mobile/test/event_classifiers_test.dart
mobile/test/expression_evaluator_test.dart
mobile/test/projection_engine_test.dart
mobile/test/projection_equivalence_test.dart
mobile/test/pattern_projection_test.dart
mobile/test/selective_retain_test.dart
```

## Verification Scope

Prove these points by existing tests, focused new tests, or code inspection plus targeted test evidence:

1. Mobile selective retention keeps own-device events even when they are now out of scope.
2. Mobile selective retention purges out-of-scope other-device events according to current assignment-derived subject scope.
3. Mobile selective retention does not purge system/integrity events that current policy keeps.
4. Mobile expression evaluator passes all shared fixture cases in `contracts/fixtures/expression-evaluation.json`.
5. Mobile expression evaluator remains bounded: no arbitrary code, no hidden functions, no deployer scripts, no trigger execution.
6. Mobile config store preserves expressions, activity role actions, severity overrides, sensitivity labels, activity pattern bindings, and packaged pattern definitions needed by projection/advisory behavior.
7. Mobile config store preserves packaged `pattern_definitions` and exposes referenced definitions to `PatternProjectionEngine`.
8. Mobile projection engine applies alias rebuilding and unresolved-flag exclusion consistently with `contracts/fixtures/projection-equivalence.json`.
9. Mobile pattern projection matches `contracts/fixtures/pattern-state-projection.json`, including packaged definitions, all bundled active pattern refs covered by the fixture, unresolved-flag exclusion, accepted re-inclusion, and rejected exclusion.
10. Mobile event assembly/classification still emits/parses the current 11-field envelope vocabulary and does not invent new event `type` values or envelope fields.
11. Mobile advisory behavior remains advisory only. It may warn locally but must not become authoritative rejection of structurally valid events.
12. BAR-014 only becomes accepted if mobile shared-fixture evidence now pairs with NW-009 server/shared-fixture evidence.

## Expected Work

Start with code inspection and existing targeted Flutter tests. If coverage is missing but behavior is correct, add narrowly focused tests in existing mobile test classes. If behavior is wrong, make a minimal mobile-side fix only if it stays inside current mobile data/config/projection/advisory boundaries.

If verification passes:

- Update each accepted BAR row in `docs/agent-working-surface/baseline-acceptance-register.md` with exact command, date, and evidence summary.
- Mark BAR-008 `baseline_accepted` only if selective retention evidence meets its exit condition.
- Mark BAR-011 `baseline_accepted` only if shared expression fixture and bounded evaluator evidence meets its exit condition.
- Add mobile evidence to BAR-012 if mobile pattern projection/config preservation passes. BAR-012 is already `baseline_accepted`; do not downgrade unless you find a real contradiction.
- Mark BAR-014 `baseline_accepted` only if mobile projection and pattern shared-fixture tests pass and remain aligned with NW-009 server evidence.
- Mark NW-010 `accepted` only if BAR-008 and BAR-011 are accepted, BAR-014 is accepted, and BAR-012 mobile evidence is attached or explicitly shown not to change its already accepted standing.

If verification fails:

- Leave affected BAR rows as `baseline_candidate`.
- Leave NW-010 `ready` or add a precise follow-up backlog row with the failing behavior, file/test anchor, and exit condition.
- Do not hide partial success by accepting BAR-014 before mobile fixture parity is proven.

## Targeted Tests

Run from the mobile directory:

```bash
cd mobile
flutter test test/config_store_test.dart test/context_resolver_test.dart test/event_assembler_test.dart test/event_classifiers_test.dart test/expression_evaluator_test.dart test/projection_engine_test.dart test/projection_equivalence_test.dart test/pattern_projection_test.dart test/selective_retain_test.dart
```

If this is too broad to diagnose failures, split into:

```bash
cd mobile
flutter test test/config_store_test.dart test/expression_evaluator_test.dart
flutter test test/projection_engine_test.dart test/projection_equivalence_test.dart test/pattern_projection_test.dart
flutter test test/selective_retain_test.dart test/event_assembler_test.dart test/event_classifiers_test.dart test/context_resolver_test.dart
```

Do not run full Flutter or Maven suites unless the fix crosses shared fixtures, contracts, or broad mobile behavior. If shared fixtures change, rerun the matching server fixture tests or stop and report the NW-009 impact instead of silently moving on.

## Guardrails

- Do not change the event envelope or event `type` vocabulary.
- Do not edit server code.
- Do not edit contracts or shared fixtures unless you first prove they are stale against current server/mobile behavior and report the NW-009 impact.
- Do not implement trigger execution.
- Do not implement auto-resolution.
- Do not implement resolver reassignment.
- Do not implement S06/entity lifecycle.
- Do not introduce production OIDC/JWT/group/claim authority.
- Do not add field-level sensitivity, encryption, or redaction behavior; current retention/sensitivity behavior is not BAR-106.
- Do not add new scope mechanisms or deployer-defined scope logic.
- Do not make mobile advisory checks authoritative rejection paths.
- Do not start S00/S19/S21/S27 scenario runtime probes in this slice.
- Do not mark BAR-010 accepted from mobile-only evidence; config package delivery still needs its own server/config acceptance path unless already verified elsewhere.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(mobile): verify mobile baseline
```

The commit may include focused mobile tests, narrow mobile fixes, and BAR/backlog updates. It must not include unrelated scenario, server, auth-provider, trigger, entity-lifecycle, contract, or architecture edits.

## Stop And Report

Stop and report if:

- mobile behavior diverges from shared fixtures and the fix would require changing contracts or server projection semantics;
- accepting BAR-014 would require ignoring mobile/server projection differences;
- selective retention needs field-level sensitivity/encryption/redaction or new scope mechanisms;
- expression evaluator support would require arbitrary code, hidden functions, deployer scripts, or trigger execution;
- mobile advisory behavior is being used to reject structurally valid events that server would accept and flag;
- the needed fix crosses into production auth, auto-resolution, resolver reassignment, entity lifecycle, new envelope fields/types, or new scope mechanisms.
