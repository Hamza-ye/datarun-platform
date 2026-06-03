# NW-032 Agent Prompt: Add S23 Setup/Config Runtime Probe

You are working in `/home/hamza/datarun-platform`.

## Goal

Add a constrained S23 scenario-grade runtime probe for "setting up a new operational activity."

Exit target:

```text
A coordinator can configure a bounded new operational activity without custom development; invalid setup fails before publication; valid setup is delivered atomically to devices; old and new shape/config versions remain interpretable; and no deferred platform mechanisms are introduced.
```

This is scenario runtime evidence across accepted baseline rows. It is not a new configuration feature phase and not a successor architecture decision.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-023, NW-031, and NW-032.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-010, BAR-011, BAR-012, and BAR-014.
6. `docs/implementation/module-interfaces.md`
   - Read `Shape Registry`, `Config Packager`, `Pattern Registry`, `Projection Engine`, `Command Validator (Advisory Only)`, and `Trigger Engine`.
7. `docs/reviews/scenario-baseline-pressure-map.md`
   - Read only the S23 row, the safe progress call, and deferred-surface guardrails for S23.
8. `docs/reviews/viability-closure-review.md`
   - Read V2 and the configuration simplicity tension only.
9. `docs/scenarios/23-configure-new-operational-activity.md`
10. `contracts/pattern-definition.schema.json`
11. `contracts/patterns/capture_with_review.v1.json`
12. `contracts/fixtures/expression-evaluation.json`
13. `contracts/fixtures/pattern-state-projection.json`
14. `contracts/sync-protocol.md`
15. Use `scripts/query_cdl.py` only for CDL-038, CDL-039, CDL-041, CDL-043, CDL-044, CDL-047, CDL-051, and CDL-056 if authority wording needs clarification.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/config/
server/src/main/java/dev/datarun/server/sync/SyncController.java
server/src/test/java/dev/datarun/server/config/
mobile/lib/data/config_store.dart
mobile/lib/domain/activity_role_actions.dart
mobile/lib/data/event_store.dart
mobile/test/config_store_test.dart
mobile/test/expression_evaluator_test.dart
mobile/test/activity_role_actions_test.dart
mobile/test/pattern_projection_test.dart
```

Adjust exact paths only if current layout differs. Prefer adding focused tests to existing config/mobile test classes unless a small scenario-specific class is clearer.

## Scenario Boundary

The probe may use:

- deployer-authored form shapes and shape versions;
- a configured activity with role-action maps, expression-backed warning/default/show-condition behavior, sensitivity/severity metadata, and a platform pattern binding;
- the existing atomic config package endpoint and mobile current+pending config model;
- existing platform pattern definitions such as `capture_with_review/v1`;
- existing mobile advisory behavior for role/action and expression warnings.

The probe must not become:

- a trigger execution or policy side-effect test;
- a deployer-authored state-machine test;
- a script/function/loop/dynamic-query feature;
- a new sync protocol, envelope field, or event `type` change;
- a new scope mechanism or production auth-provider test;
- a reporting/dashboard/warehouse feature;
- an S06/entity lifecycle feature.

## Expected Acceptance Points

Prove the smallest coherent set that covers S23:

1. Invalid setup is caught before publication or device dependency. Use existing deploy-time validation boundaries for missing/unknown shape refs, invalid role actions, invalid expressions, invalid pattern refs/bindings, or publish drift as appropriate.
2. A valid new activity setup can be represented with current shape registry, role-action config, bounded expressions, and platform pattern binding.
3. Publishing produces a monotonic atomic package containing the relevant deployer shapes/activity config and only referenced platform pattern definitions.
4. A changed setup creates a later package while preserving old/deprecated shape versions needed for old events and in-progress work.
5. Mobile can hold current plus pending setup, keep the current setup usable before promotion, and interpret the promoted package without losing old refs needed for in-progress work.
6. Mobile preserves bounded warnings/advisories as advisory behavior only; it does not reject structurally valid events for state or policy reasons.
7. No trigger execution, custom code, new expression functions, deployer-authored state machines, new scope mechanisms, production auth claims, entity lifecycle, or contract changes are introduced.

If existing tests already prove an acceptance point, reference them in the backlog evidence. Add focused tests only where S23 composition is unproven.

## Targeted Tests

Run the new focused test first.

Then run the nearest config/mobile slice:

```bash
cd server
./mvnw -Dtest=ConfigIntegrationTest,DeployTimeValidatorTest test

cd mobile
flutter test test/config_store_test.dart test/expression_evaluator_test.dart test/activity_role_actions_test.dart test/pattern_projection_test.dart
```

If the implementation touches shared projection fixtures, pattern definitions, sync behavior, or broader mobile projection, rerun the smallest affected companion tests and report the exact commands.

## BAR And Backlog Updates

If the S23 runtime probe passes:

- Mark NW-032 `accepted`.
- Attach exact date, commands, and concise evidence summary to the NW-032 backlog row.
- Do not mark BAR rows accepted merely because S23 passed; BAR-010, BAR-011, BAR-012, and BAR-014 are already accepted.
- Do not open NW-033/S26 unless the steward/user explicitly selects it next.

If the probe exposes a current baseline bug:

- Leave NW-032 `in_review` or mark it `blocked` with exact file/path/test evidence.
- Add a precise follow-up backlog row only if the fix is outside this prompt's boundary.
- Do not weaken the scenario by silently dropping version coexistence, mobile pending/current behavior, bounded warning behavior, or publish-time failure evidence.

## Forbidden Work

- Do not implement general trigger execution.
- Do not implement auto-resolution, resolver reassignment, or production OIDC/JWT/Keycloak/group/claim authority.
- Do not add envelope fields or event `type` values.
- Do not add new sync protocol fields or change normal sync watermark semantics.
- Do not add deployer-authored state machines, custom scope logic, custom scripts, loops, dynamic queries, or new expression functions.
- Do not package platform payload schemas as deployer `shapes`.
- Do not implement reporting dashboards, analytics storage, S06/entity lifecycle, or field-level sensitivity/encryption/redaction.
- Do not change contracts or shared fixtures unless you first stop and report concrete drift against accepted behavior.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(config): add setup scenario probe
```

The commit may include focused server/mobile tests, a narrow production fix only if needed to satisfy accepted baseline behavior, and NW-032 backlog evidence. It must not include unrelated reporting, contract-schema, production-auth, trigger, lifecycle, or architecture edits.

## Stop And Report

Stop and report if:

- proving S23 requires a new root deployer shape/config schema, new envelope fields/types, new expression functions, custom code, custom scope logic, trigger execution, deployer-authored state machines, or production auth-provider authority;
- mobile and server package behavior disagree in a way that cannot be fixed narrowly inside accepted config/package boundaries;
- old/new version coexistence is contradicted by current code;
- bounded warnings become authoritative event rejection on device;
- platform pattern delivery requires changing platform pattern contracts instead of packaging referenced definitions.
