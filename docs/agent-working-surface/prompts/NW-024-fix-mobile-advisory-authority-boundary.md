# NW-024 Agent Prompt: Fix Mobile Advisory Authority Boundary

You are working in `/home/hamza/datarun-platform`.

## Goal

Fix the mobile advisory authority boundary discovered during NW-010.

Current problem: mobile role/action advisory decisions are used as local rejection paths. This conflicts with the baseline rule that structurally valid state/policy anomalies are accepted and flagged by the server, while mobile command validation remains advisory.

Exit target:

```text
Mobile may warn users about missing/stale role-action authority, but it must not prevent structurally valid capture solely because the local advisory decision is negative.
```

After the fix, unblock NW-010 by rerunning its targeted mobile verification slice. If the full NW-010 acceptance conditions are met, update BAR/NW-010 as described below. If not, land only the advisory-boundary fix and leave precise follow-up routing.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-010 and NW-024.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-008, BAR-011, BAR-012, BAR-014, and related BAR-013.
6. `docs/implementation/module-interfaces.md`
   - Read `Conflict Detector`, `Projection Engine`, `Pattern Registry`, and `Command Validator (Advisory Only)`.
7. `docs/constraints.md`
   - Read connectivity, capture latency, and configuration-change constraints.
8. `docs/decisions/idr-021-role-action-enforcement-model.md`
   - Read the decision, alternatives rejected, and quality gates.
9. Use `scripts/query_cdl.py` only for CDL-003, CDL-035, and CDL-047 when checking authority wording.

Implementation and test files to inspect:

```text
mobile/lib/domain/activity_role_actions.dart
mobile/lib/data/config_store.dart
mobile/lib/presentation/screens/form_screen.dart
mobile/lib/presentation/screens/work_list_screen.dart
mobile/lib/presentation/screens/subject_detail_screen.dart
mobile/test/activity_role_actions_test.dart
mobile/test/config_store_test.dart
mobile/test/event_assembler_test.dart
mobile/test/event_classifiers_test.dart
mobile/test/expression_evaluator_test.dart
mobile/test/projection_engine_test.dart
mobile/test/projection_equivalence_test.dart
mobile/test/pattern_projection_test.dart
mobile/test/selective_retain_test.dart
mobile/test/context_resolver_test.dart
```

## Authority And Guardrails

CDL-level constraints:

```text
CDL-003: structurally valid state-stale events are accepted and flagged.
CDL-035: stale authorization work is accepted and flagged; severity controls downstream policy.
CDL-047: on-device command validation is advisory, not authoritative.
```

Product constraints:

```text
Capture must be immediate and must not depend on connectivity.
Work in progress under an old configuration completes under old rules; new work follows the new configuration.
```

Interpretation for this slice:

- Hard local blocking is valid for structural form/payload requirements.
- Hard local blocking is not valid for role/action/scope/workflow/current-state advisory misses.
- Mobile may show warning text, stale-state indicators, confirmation UI, or lower-priority affordances.
- If the current UI has no alternate path to capture, hiding/filtering an action or disabling Save is authoritative blocking and must be fixed.
- Server-side conflict detection remains the correctness boundary.

## Expected Implementation Boundary

Make the smallest mobile-only change that removes advisory-as-rejection behavior.

Expected fixes likely include:

- Replace `blocked` naming or semantics in `ActivityActionDecision` if it continues to imply local authority.
- Keep warning information available from `ActivityActionAdvisory`.
- Do not disable `FormScreen` Save solely because role/action advisory is negative.
- Do not return from `_save` solely because role/action advisory is negative; refresh and display the warning, then continue normal event assembly if structural form validation passes.
- Do not filter every capture path out of `WorkListScreen` or `SubjectDetailScreen` solely because `decision.allowed` is false. If the UI still de-emphasizes or annotates warnings, preserve at least one clear path to capture.
- Preserve existing structural validation and event assembly behavior.

Add focused regressions proving the boundary. Prefer existing test files. If there is no practical widget-test harness for the screens, combine domain/config tests with code inspection and targeted search evidence for the removed blocking patterns.

## Verification

Run the focused advisory tests first:

```bash
cd mobile
flutter test test/activity_role_actions_test.dart test/config_store_test.dart
```

Then run the NW-010 targeted suite:

```bash
cd mobile
flutter test test/config_store_test.dart test/context_resolver_test.dart test/event_assembler_test.dart test/event_classifiers_test.dart test/expression_evaluator_test.dart test/projection_engine_test.dart test/projection_equivalence_test.dart test/pattern_projection_test.dart test/selective_retain_test.dart
```

Run targeted code search from the repo root and report the result:

```bash
rg -n "_captureAllowed|ActivityActionDecision\\.blocked|!_captureAllowed|!decision\\.allowed|decision\\.allowed\\) \\{|continue;" mobile/lib/domain/activity_role_actions.dart mobile/lib/presentation/screens/form_screen.dart mobile/lib/presentation/screens/work_list_screen.dart mobile/lib/presentation/screens/subject_detail_screen.dart
```

The search does not need to return zero lines if a term remains for non-authoritative warning display, but any remaining local rejection path must be explained or fixed.

Do not run Maven or full Flutter unless the fix touches shared fixtures, contracts, broad mobile store behavior, or cross-platform projection semantics.

## BAR And Backlog Updates

If the advisory-boundary fix and the full NW-010 targeted suite pass:

- Mark NW-024 `accepted`.
- Mark NW-010 `accepted` only if BAR-008, BAR-011, and BAR-014 meet their evidence requirements and BAR-012 mobile evidence is attached without reopening its accepted status.
- Move BAR-008 to `baseline_accepted` only with selective retention evidence.
- Move BAR-011 to `baseline_accepted` only with expression evaluator fixture/boundedness evidence.
- Attach mobile evidence to BAR-012 if pattern projection/config preservation passes; do not downgrade unless there is a real contradiction.
- Move BAR-014 to `baseline_accepted` only if mobile shared-fixture tests pass and remain aligned with the NW-009 server evidence already attached.

If the advisory-boundary fix passes but NW-010 acceptance is still incomplete:

- Mark NW-024 `accepted`.
- Change NW-010 back to `ready` with the exact remaining evidence gap.
- Leave BAR rows unchanged except for evidence that is fully proven.

If the fix cannot be made within this boundary:

- Leave NW-024 `ready` or mark it `blocked` with the blocker.
- Leave NW-010 `blocked`.
- Do not move BAR rows to `baseline_accepted`.

## Forbidden Work

- Do not change server code.
- Do not change contracts or shared fixtures unless you first stop and report the drift.
- Do not change the event envelope or event `type` vocabulary.
- Do not implement trigger execution, auto-resolution, resolver reassignment, S06/entity lifecycle, production OIDC/JWT/group/claim authority, new scope mechanisms, or field-level sensitivity/encryption/redaction.
- Do not treat config package delivery as BAR-010 acceptance in this slice.
- Do not add scenario runtime probes.
- Do not weaken structural form/payload validation.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
fix(mobile): keep advisory checks non-authoritative
```

The commit may include mobile code, focused mobile tests, BAR/backlog updates, and no unrelated docs or architecture changes.

## Stop And Report

Stop and report if:

- preserving capture requires changing server accept-and-flag semantics;
- mobile cannot preserve a capture path without adding new envelope fields/types or new scope mechanisms;
- screen behavior cannot be regression-tested or inspected enough to prove advisory checks are no longer authoritative;
- the fix would require changing contracts/shared fixtures or reopening NW-009 server evidence;
- the implementation surface expands into production auth, triggers, auto-resolution, resolver reassignment, entity lifecycle, or field-level sensitivity.
