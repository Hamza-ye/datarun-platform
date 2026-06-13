# Implementation Task: Append-Only Correction UX

Status: ready 2026-06-13

## Goal

Let a field user correct a prior capture without editing or replacing history.
A correction must create a new capture for the same subject and exact shape,
while the original event remains visible and unchanged.

## Read

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/implementation/module-interfaces.md` section "Mobile Actor Session And Local Store"
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `docs/reviews/scenario-baseline-pressure-map.md` S00 expected events
- `mobile/lib/presentation/screens/subject_detail_screen.dart`
- `mobile/lib/presentation/screens/form_screen.dart`
- `mobile/lib/data/event_assembler.dart`

## Allowed Files

- `mobile/lib/presentation/screens/form_screen.dart`
- `mobile/lib/presentation/screens/subject_detail_screen.dart`
- `mobile/test/correction_flow_test.dart` (new)
- `mobile/test/capture_handoff_test.dart` only for regression alignment

## Required Behavior

1. A capture event in subject history offers `Add correction` only when its
   exact `shape_ref` remains available in local config.
2. The correction form uses the same subject, shape, and activity refs and
   prefills active fields from the selected event payload.
3. The form clearly explains that saving creates a new record and keeps the
   original in history.
4. Saving without changing a field does not append a duplicate correction.
5. Saving after a change calls the existing `EventAssembler.assemble()` and
   creates a distinct event; it never updates the selected event.
6. On return, subject history and app pending/projection state refresh, both
   events remain visible, and the surviving screen confirms:
   `Correction saved on this device. Original record remains in history.
   Waiting to sync.`
7. Non-capture events and captures whose exact shape is unavailable do not
   offer the correction action.
8. The UI does not claim a durable correction-to-original link or introduce a
   new correction event type, envelope field, or payload metadata.

## Guardrails

Reuse the existing `capture` envelope type, exact `shape_ref`, subject ref,
activity ref, form renderer, event assembler, local event store, projection,
pending count, and advisory role-action checks. Do not mutate events, infer
shape-version compatibility, add correction metadata outside deployer shapes,
edit contracts/server/sync, change authority or flag semantics, or introduce
entity lifecycle.

## Tests

Add widget coverage for:

- eligible capture exposes `Add correction`;
- correction form is prefilled and explains append-only behavior;
- unchanged correction is not saved;
- changed correction appends a distinct event with the same subject/shape/
  activity and leaves the original payload unchanged;
- history refresh shows original and correction;
- unavailable shape and non-capture events expose no correction action.

Run:

```bash
cd mobile
flutter test test/correction_flow_test.dart test/capture_handoff_test.dart
flutter test
dart analyze lib/presentation/screens/form_screen.dart \
  lib/presentation/screens/subject_detail_screen.dart \
  test/correction_flow_test.dart \
  test/capture_handoff_test.dart
```

## Commit Boundary

One implementation commit: `feat(mobile): add append-only correction flow`

Stop and report if the flow requires event mutation, a new envelope field/type,
undeclared payload metadata, shape-version inference, durable correction
linkage, changed sync/authority semantics, or entity-lifecycle truth.
