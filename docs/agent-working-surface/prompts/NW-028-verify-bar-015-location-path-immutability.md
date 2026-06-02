# NW-028 Agent Prompt: Verify BAR-015 Historical Location-Path Immutability

You are working in `/home/hamza/datarun-platform`.

## Goal

Verify and, if supported by evidence, accept BAR-015: historical `events.location_path` immutability.

Exit target:

```text
BAR-015 moves to baseline_accepted only if code inspection and targeted evidence prove historical event location_path is not rewritten after insert, except for an explicitly controlled NULL backfill route if one exists.
```

This is a baseline-verification slice, not a location feature slice.

## Files To Read

Read only this packet by default:

1. `AGENTS.md`
2. `docs/status.md` Current Routing section only.
3. `docs/agent-working-surface/README.md`
4. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Read NW-027 and NW-028.
5. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Read BAR-003, BAR-007, and BAR-015.
6. `docs/implementation/module-interfaces.md`
   - Read `Event Store` and `Scope Resolver`.
7. `docs/decisions/idr-014-materialized-path-locations.md`
8. `docs/decisions/idr-015-scope-filtered-sync-query.md`
9. `contracts/sync-protocol.md`
10. Use `scripts/query_cdl.py` only for CDL-031 and CDL-033 when checking authority wording.

Implementation and test files to inspect:

```text
server/src/main/java/dev/datarun/server/event/
server/src/main/java/dev/datarun/server/authorization/
server/src/main/java/dev/datarun/server/location/
server/src/main/java/dev/datarun/server/sync/
server/src/main/resources/db/migration/
server/src/test/java/dev/datarun/server/authorization/
server/src/test/java/dev/datarun/server/sync/
```

Adjust exact paths based on current package layout.

## Expected Verification Points

Prove:

1. `events.location_path` is set only as event denormalization tied to event insert or a documented controlled NULL backfill.
2. Reparenting or editing a subject/location, if such a command exists, does not rewrite historical event `location_path`.
3. Scope-filtered sync uses the stored historical event path for the event as written, not a latest subject location lookup that changes historical visibility.
4. There is no broad cascade update from current location hierarchy changes into historical event rows.
5. If no location reparent/backfill surface exists, record that fact explicitly as part of acceptance evidence.

## Suggested Inspection

Run targeted searches and inspect every hit:

```bash
rg -n "location_path|subject_locations|locations|reparent|backfill|UPDATE events" server/src/main/java server/src/main/resources/db/migration server/src/test/java
```

Classify hits as:

- event insert-time denormalization;
- current subject/location mapping updates;
- sync filtering reads;
- tests;
- absent or present reparent/backfill surfaces;
- any historical event-path rewrite, which is a stop condition unless explicitly documented as controlled NULL backfill.

## Targeted Tests

If current code has a reparent or controlled backfill surface, add or run a focused regression proving historical event `location_path` stays stable after that operation.

If current code has no such mutation surface, targeted code inspection plus existing scope-filtered sync tests may be sufficient. Run the closest existing tests, likely:

```bash
cd server
./mvnw -Dtest=ScopeFilteredSyncIntegrationTest,AssignmentContainmentIntegrationTest test
```

Add a regression only if there is an actual behavior surface to guard. Do not invent location reparenting to test this row.

## BAR And Backlog Updates

If verification passes:

- Update BAR-015 to `baseline_accepted`.
- Attach exact date, commands, and code-inspection summary.
- Mark NW-028 `accepted` with concise evidence.

If verification finds drift:

- Leave BAR-015 as `baseline_candidate`.
- Mark NW-028 `blocked` or leave `in_review` with exact file/path/test evidence.
- Do not normalize the drift by changing docs only.

## Forbidden Work

- Do not implement location reparenting.
- Do not implement controlled backfill unless it already exists and only needs a guard.
- Do not rewrite historical event paths as a convenience fix.
- Do not add envelope fields, event types, or scope mechanisms.
- Do not change normal sync watermark semantics.
- Do not implement triggers, auto-resolution, resolver reassignment, S06/entity lifecycle, production auth-provider authority, or field-level sensitivity.

## Commit Boundary

Use one commit if the slice lands cleanly:

```text
docs(status): accept location path immutability baseline
```

The commit may include BAR/backlog updates and a focused regression test if an existing mutation surface needs a guard. It must not include new location features or broad sync rewrites.

## Stop And Report

Stop and report if:

- production code rewrites historical `events.location_path` after event insert outside a documented controlled NULL backfill;
- IDR-014, IDR-015, BAR-015, and code disagree;
- proving the row would require implementing a new location lifecycle/reparenting feature;
- accepting BAR-015 would require weakening sync scope semantics or CDL-033 original-scope authorization semantics.
