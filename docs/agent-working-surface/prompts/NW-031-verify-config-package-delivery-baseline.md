# NW-031 Agent Prompt: Verify BAR-010 Config Package Delivery Baseline

You are working in `/home/hamza/datarun-platform`.

## Goal

Verify and, if evidence supports it, accept BAR-010: config package delivery.

Exit target:

```text
BAR-010 moves to baseline_accepted only if targeted server/config and mobile evidence proves deploy-time validation, atomic package delivery, shape version coexistence, referenced pattern definition delivery, and safe forward-compatible parsing behavior.
```

This is a verification slice, not a new configuration feature phase.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-010 and nearby config/projection rows BAR-011, BAR-012, and BAR-014.
5. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-010 and NW-031.
6. `docs/implementation/module-interfaces.md`
   - Read `Shape Registry`, `Config Packager`, `Pattern Registry`, and `Command Validator (Advisory Only)`.
7. `contracts/pattern-definition.schema.json`
8. `contracts/patterns/*.json`
9. `contracts/sync-protocol.md`
10. Use `scripts/query_cdl.py` only for CDL-038, CDL-039, CDL-041, CDL-044, and CDL-056 if authority wording needs clarification.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/config/
server/src/main/java/dev/datarun/server/sync/SyncController.java
server/src/test/java/dev/datarun/server/config/ConfigIntegrationTest.java
server/src/test/java/dev/datarun/server/config/DeployTimeValidatorTest.java
mobile/lib/data/config_store.dart
mobile/lib/data/event_store.dart
mobile/test/config_store_test.dart
mobile/test/pattern_projection_test.dart
```

Adjust exact paths only if current layout differs.

## Verification Points

Prove:

1. Deploy-time validation rejects invalid expressions, role actions, pattern refs, pattern bindings, severity overrides, uniqueness declarations, and config-package publish drift.
2. Config packages are atomic snapshots with monotonic versions, not diffs.
3. Devices can hold current plus pending config and promote only at the existing safe transition point.
4. Shape versions coexist in packages; deprecated/old versions remain available for old events and in-progress work.
5. Pattern definitions are platform-owned contract resources and the config package includes only referenced definitions under `pattern_definitions`.
6. Mobile preserves deployer shapes, activities, expressions, role-action maps, severity overrides, sensitivity classifications, pattern bindings, and packaged pattern definitions.
7. Unknown top-level config keys are ignored or preserved safely where current forward-compatible behavior requires it.
8. Unknown event `shape_ref` behavior remains forward-compatible for non-platform shapes; do not confuse this with platform payload contract validation.

## Targeted Tests

Run server config tests first:

```bash
cd server
./mvnw -Dtest=ConfigIntegrationTest,DeployTimeValidatorTest test
```

Run focused mobile config/package tests:

```bash
cd mobile
flutter test test/config_store_test.dart test/pattern_projection_test.dart
```

If a narrow gap is found and fixed in shared package/projection behavior, rerun the smallest affected companion tests. Do not run full Maven or full Flutter unless the change crosses broad config, contract, or projection behavior.

## Expected Work

Start with code inspection and existing tests. Add focused tests only if an acceptance point is unproven but the implementation is correct. Make production changes only for narrow drift inside the existing Config Packager, Deploy-Time Validator, mobile ConfigStore, or package parsing boundaries.

If verification passes:

- Update BAR-010 to `baseline_accepted`.
- Attach exact date, commands, and concise source/test evidence.
- Mark NW-031 `accepted` with the same evidence.

If verification fails:

- Leave BAR-010 as `baseline_candidate`.
- Leave NW-031 `ready` or mark it `blocked` with exact evidence.
- Do not partially accept BAR-010 by relying only on server or only on mobile evidence unless the missing side is explicitly not required by the BAR exit condition.

## Forbidden Work

- Do not add envelope fields or event `type` values.
- Do not turn deployer configuration into code, custom scope logic, or deployer-authored state machines.
- Do not implement trigger execution, auto-resolution, resolver reassignment, S06/entity lifecycle, production OIDC/JWT/group/claim authority, field-level sensitivity/encryption/redaction, or new scope mechanisms.
- Do not package platform payload schemas as deployer `shapes`.
- Do not change platform pattern definitions or shared fixtures unless you first prove they are stale against current accepted behavior and report the impact.
- Do not start S23 or S26 runtime probes in this slice.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
test(config): verify config package delivery baseline
```

The commit may include focused tests, narrow fixes, and BAR/backlog updates. It must not include broad feature work or unrelated scenario probes.

## Stop And Report

Stop and report if:

- BAR-010 acceptance would require a new config package schema, new envelope semantics, trigger execution, custom scope logic, or deployer-authored state machines;
- server and mobile package behavior disagree in a way that cannot be fixed narrowly;
- shape version coexistence or package atomicity is contradicted by current code;
- pattern definition delivery requires changing platform pattern contracts instead of packaging referenced definitions.
