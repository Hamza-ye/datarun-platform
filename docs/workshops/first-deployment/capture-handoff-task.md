# Implementation Task: Offline Capture Handoff

Status: completed 2026-06-13

## Goal

Make a successful offline capture legible after the form closes. The surviving
screen must confirm that the record is saved on this device and waiting to
sync, and the work list must keep pending local work visible.

## Read

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/implementation/module-interfaces.md` section "Mobile Actor Session And Local Store"
- `mobile/lib/presentation/screens/form_screen.dart`
- `mobile/lib/presentation/screens/work_list_screen.dart`
- `mobile/lib/presentation/screens/subject_detail_screen.dart`
- `mobile/lib/presentation/app_state.dart`

## Allowed Files

- `mobile/lib/presentation/screens/form_screen.dart`
- `mobile/lib/presentation/screens/work_list_screen.dart`
- `mobile/lib/presentation/screens/subject_detail_screen.dart`
- `mobile/test/capture_handoff_test.dart` (new)

## Required Behavior

1. A successful form save returns an explicit saved result to its caller.
2. Both capture entry points refresh local projections and pending count after
   that result.
3. The surviving screen confirms: `Saved on this device. Waiting to sync.`
4. The work list shows pending local record count with the same saved/waiting
   vocabulary and opens the existing sync panel from that status.
5. Leaving a form without saving must not show a success confirmation.
6. Empty work-list copy must describe captured work without implying canonical
   subject-registry or entity-lifecycle truth.

## Guardrails

Do not edit contracts, server code, event assembly/storage, sync protocol or
watermarks, assignment/principal authority, shared-device behavior, retention,
or S06 entity lifecycle. Mobile remains advisory. Reuse `AppState.refresh()`,
`pendingCount`, and the existing sync panel; add no persisted state.

## Tests

Add widget coverage for:

- pending local work visible from the work list;
- pending status opens the existing sync panel;
- successful new capture refreshes and confirms on the work list;
- ordinary form dismissal does not confirm success;
- successful subject-linked capture confirms on subject detail;
- neutral empty work-list copy.

Run:

```bash
cd mobile
flutter test test/capture_handoff_test.dart test/sync_panel_test.dart
flutter test
dart analyze lib/presentation/screens/form_screen.dart \
  lib/presentation/screens/work_list_screen.dart \
  lib/presentation/screens/subject_detail_screen.dart \
  test/capture_handoff_test.dart
```

## Commit Boundary

One implementation commit: `feat(mobile): surface offline capture handoff`

Stop and report if the behavior requires a new API/contract, persisted workflow
state, changed sync semantics, authority beyond existing mobile state, or
entity-lifecycle truth.

## Evidence

- Commit: `5dad1c9 feat(mobile): surface offline capture handoff`
- Focused capture-handoff and sync-panel suite: 12 tests passed.
- Full mobile suite: 119 tests passed.
- Touched-file `dart analyze`: no issues.
