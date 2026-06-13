# Implementation Task: Mobile Sync Status

Status: completed 2026-06-13

## Goal

Make offline and sync state understandable without changing sync semantics.
Users must be able to distinguish saved on this device, waiting to sync,
syncing, synced, and failed sync. A failed attempt must not update the latest
successful-sync timestamp.

## Read

- `AGENTS.md`
- `docs/status.md` Current Routing
- `docs/implementation/module-interfaces.md` section "Mobile Actor Session And Local Store"
- `mobile/lib/presentation/app_state.dart`
- `mobile/lib/presentation/widgets/sync_panel.dart`
- `mobile/lib/data/sync_service.dart`
- `mobile/test/sync_service_test.dart`

## Allowed Files

- `mobile/lib/presentation/app_state.dart`
- `mobile/lib/presentation/widgets/sync_panel.dart`
- `mobile/test/sync_panel_test.dart` (new)
- `mobile/test/sync_service_test.dart` only if a regression assertion is needed

## Required Behavior

1. Add a presentation-only sync status derived from existing `pendingCount`,
   `isSyncing`, `SyncResult`, and the latest successful-sync time. Do not
   persist a new source of truth.
2. Update the successful-sync timestamp only when `SyncResult.error == null`.
3. Before sync, pending work is described as saved on this device and waiting
   to sync.
4. During sync, show a clear syncing state and disable duplicate submission.
5. On success, show sent/received counts and the latest successful-sync time.
6. On failure, show a clear failure/retry message and state that pending work
   remains saved on the device when `pendingCount > 0`.
7. Keep connection, unauthorized, and actor/session drift details available as
   secondary error text without treating mobile as authority.
8. Replace symbolic push/pull copy with plain user-facing language.

## Guardrails

Follow `AGENTS.md` and `docs/status.md`. Do not edit contracts, server code,
sync protocol/storage semantics, actor authority, retention/security, S06
entity lifecycle, or shared-device switching.

## Tests

Add widget/state coverage for:

- never synced with no pending work;
- pending work waiting to sync;
- syncing;
- successful sync and successful timestamp;
- failed sync with pending work preserved in the UI;
- failed sync does not replace the last successful-sync timestamp.

Run:

```bash
cd mobile
flutter test test/sync_panel_test.dart test/sync_service_test.dart
flutter test
```

Manual walkthrough: save one capture offline, open the sync panel, force a
connection failure, confirm the pending record is still described as saved,
then retry successfully and confirm the latest successful-sync time updates.

## Commit Boundary

One commit: `feat(mobile): clarify offline sync status`

Stop and report only if the UI requires a new API/contract, persisted workflow
state, changed sync semantics, or authority beyond existing mobile state.

## Evidence

- Commit: `8692607 feat(mobile): clarify offline sync status`
- Focused: 13 tests passed.
- Full mobile suite: 114 tests passed.
- Touched-file `dart analyze`: no issues.
