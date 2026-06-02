# NW-027 Agent Prompt: Verify BAR-002 Append-Only Event Store Acceptance

You are working in `/home/hamza/datarun-platform`.

## Goal

Verify and, if supported by evidence, accept BAR-002: append-only event store.

Exit target:

```text
BAR-002 moves to baseline_accepted only if source inspection proves there is no operational event update/delete path and targeted sync tests prove idempotent inserts.
```

NW-026 already gives runtime scenario evidence for append-only correction and idempotent replay. This slice must add the missing code-inspection proof and attach exact evidence to BAR-002.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-026 and NW-027.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-001, BAR-002, BAR-003, BAR-004, BAR-013, and BAR-015.
6. `docs/implementation/module-interfaces.md`
   - Read `Event Store`, `Projection Engine`, and `Sync`-related guard references.
7. `contracts/sync-protocol.md`
8. Use `scripts/query_cdl.py` only for CDL-001, CDL-002, CDL-019, and CDL-021 when checking authority wording.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/event/
server/src/main/java/dev/datarun/server/sync/
server/src/main/java/dev/datarun/server/subject/
server/src/main/java/dev/datarun/server/projection/
server/src/main/resources/db/migration/
server/src/test/java/dev/datarun/server/sync/SyncControllerIntegrationTest.java
server/src/test/java/dev/datarun/server/e2e/MultiDeviceE2ETest.java
server/src/test/java/dev/datarun/server/subject/
```

Adjust exact paths based on current package layout.

## Expected Verification Points

Prove:

1. Operational event writes append new rows; existing operational event identity, envelope fields, payload, actor, subject, type, shape, and device metadata are not patched to model corrections or state changes.
2. There is no production code path that deletes operational events.
3. There is no production code path that updates existing operational event payload/domain meaning.
4. Sync push is idempotent by event id.
5. Corrections/reviews/resolutions/identity changes/assignment changes are represented as additional events, not mutation of prior events.
6. Any `events.location_path` update found during inspection is classified carefully:
   - insert-time denormalization immediately after event insert belongs to BAR-015 scrutiny;
   - it must not be used to claim mutable operational truth;
   - do not silently accept historical location rewrite drift under BAR-002.

## Suggested Inspection

Run targeted searches and inspect every hit:

```bash
rg -n "UPDATE events|DELETE FROM events|deleteFrom\\(\"events\"|deleteBy|update\\(" server/src/main/java server/src/main/resources/db/migration
rg -n "INSERT INTO events|ON CONFLICT|sync_watermark|device_sequence|payload" server/src/main/java/dev/datarun/server/event server/src/main/java/dev/datarun/server/sync
```

The first search does not need zero hits. It needs classified hits:

- allowed non-event-table updates, such as device sync state or config tables;
- event insert/idempotency paths;
- known `events.location_path` denormalization routed to BAR-015;
- any real operational event mutation, which is a stop condition.

## Targeted Tests

Run focused sync/event tests first. Use exact class names present in the repository, likely:

```bash
cd server
./mvnw -Dtest=SyncControllerIntegrationTest test
./mvnw -Dtest=MultiDeviceE2ETest,SubjectHistoryBackfillIntegrationTest,ScopeFilteredSyncIntegrationTest test
```

If these are too broad or class names differ, run the closest sync/idempotency/subject-history tests and report the exact command.

Do not run Flutter. Do not run full Maven unless source inspection or targeted tests reveal shared behavior uncertainty.

## BAR And Backlog Updates

If verification passes:

- Update BAR-002 to `baseline_accepted`.
- Attach exact date, commands, code-inspection summary, and NW-026 runtime evidence reference.
- Mark NW-027 `accepted` with concise evidence.
- Do not mark BAR-015 accepted from BAR-002 inspection unless BAR-015's own exit condition is also fully handled in its own slice.

If verification finds drift:

- Leave BAR-002 as `baseline_candidate`.
- Mark NW-027 `blocked` or leave `in_review` with exact file/path/test evidence.
- Do not code around the drift unless the fix is narrow, clearly inside Event Store/Sync boundaries, and preserves current architecture.

## Forbidden Work

- Do not delete or rewrite existing events.
- Do not add envelope fields or event `type` values.
- Do not change `shape_ref` grammar.
- Do not add durable workflow-state tables.
- Do not rewrite normal sync watermarks.
- Do not implement triggers, auto-resolution, resolver reassignment, S06/entity lifecycle, production auth-provider authority, field-level sensitivity, or new scope mechanisms.
- Do not conflate BAR-002 append-only acceptance with BAR-015 historical location-path immutability.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
docs(status): accept append-only event store baseline
```

The commit may include BAR/backlog updates and a narrow regression test only if inspection exposes a missing but necessary append-only/idempotency guard. It must not include broad production changes or unrelated scenario probes.

## Stop And Report

Stop and report if:

- production code mutates/deletes operational event rows for corrections, reviews, state changes, identity changes, assignments, flags, or workflow behavior;
- accepting BAR-002 would require ignoring a real update/delete path;
- the only way to prove append-only behavior requires changing contracts or envelope semantics;
- location-path behavior contradicts BAR-015/IDR-014/IDR-015 and needs separate handling.
