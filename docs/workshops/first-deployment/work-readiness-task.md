# Implementation Task: Mobile Work Readiness

Status: completed 2026-06-13

## Goal

Make the state after device connection understandable. A field user must know
whether to download work, wait for sync, retry a failure, ask for assignment or
configuration, or begin capture.

## Read

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/implementation/module-interfaces.md` section "Mobile Actor Session And Local Store"
- `docs/agent-working-surface/operational-ux-layering-companion.md`
- `mobile/lib/presentation/app_state.dart`
- `mobile/lib/presentation/screens/work_list_screen.dart`
- `mobile/lib/data/config_store.dart`

## Allowed Files

- `mobile/lib/presentation/screens/work_list_screen.dart`
- `mobile/lib/presentation/app_state.dart` only for a presentation-derived
  helper with no new persisted state
- `mobile/test/work_readiness_test.dart` (new)
- `mobile/test/capture_handoff_test.dart` only for regression alignment

## Required Behavior

1. Before any local config is available, show `Get your work` with an action
   that runs the existing sync flow.
2. While that sync runs, show `Getting your work` and prevent duplicate
   submission.
3. If the attempt fails, show `Couldn't get work`, preserve secondary error
   text, and provide `Try Again`.
4. If sync succeeds without a usable config/capture form, explain that work
   setup is unavailable and provide a sync-again action.
5. If usable capture forms exist but no current assignment is available,
   explain that no assigned work is available and provide a sync-again action.
6. Do not block configured capture solely because the local assignment set is
   empty. Existing mobile advisory/server accept-and-flag behavior remains.
7. When assignment and capture form configuration are both available, show a
   clear ready-to-capture empty state.
8. Existing captured records and pending-local status remain visible in every
   readiness state.

## Guardrails

Reuse `AppState.sync()`, `lastSyncResult`, `isSyncing`,
`activeAssignments`, `ConfigStore.configVersion`, active activities, and
configured shapes. Add no persisted readiness state. Do not edit setup/login,
contracts, server code, sync protocol/storage, authority, shared-device
sessions, retention, reporting, or S06 entity lifecycle. Mobile remains
advisory.

## Tests

Add widget/state coverage for:

- first-use get-work state and action;
- sync-in-progress state with disabled duplicate action;
- failed first sync and retry;
- successful sync without config/forms;
- configured forms without assignment;
- assignment plus forms ready to capture;
- existing records remain visible while readiness needs attention;
- configured capture remains reachable without assignment.

Run:

```bash
cd mobile
flutter test test/work_readiness_test.dart test/capture_handoff_test.dart \
  test/sync_panel_test.dart
flutter test
dart analyze lib/presentation/screens/work_list_screen.dart \
  lib/presentation/app_state.dart \
  test/work_readiness_test.dart \
  test/capture_handoff_test.dart
```

## Commit Boundary

One implementation commit: `feat(mobile): explain work readiness`

Stop and report if the UI requires a new API/contract, persisted readiness
state, changed sync semantics, mobile authority, login/token lifecycle, or
entity-lifecycle truth.

## Evidence

- Commit: `1b4ca81 feat(mobile): explain work readiness`
- Focused work-readiness, capture-handoff, and sync-panel suite: 19 tests
  passed.
- Full mobile suite: 126 tests passed.
- Touched-file `dart analyze`: no issues.
