# Datarun Mobile — Ship-1 Emulator Demo

Flutter companion app for the Ship-1 server. Thinnest possible CHV device:
offline-first local capture of `household_observation/v1`, shape-driven form,
scope-filtered village picker, watermarked pull + idempotent push.

Built as the Ship-1 demo artifact (see `docs/ships/ship-1-retro.md` §7 hand-off).
Walk W-1 and W-2 by hand; anything that surfaces is Ship-1 debt that should
close before Ship-2, not defer into it.

## What it does

- **Setup screen** — paste `server_url`, `actor_id`, bearer token (from
  `POST /dev/bootstrap`); persistent `device_id` auto-generated on first launch.
- **Home** — unified event feed (pending / synced / remote), pending-count
  badge, one-tap Sync.
- **Capture** — shape-driven form for `household_observation/v1` with a
  village-picker sourced from `/api/sync/config`. Saves locally as `pending`
  (no network required).
- **Sync** — fetches config → pushes pending → pulls new events. Uses the
  watermark persisted across launches. Idempotent (re-push is a no-op).

## Wire discipline

Every capture is a full 11-field envelope (`contracts/envelope.schema.json`)
built client-side. `device_seq` is a per-install monotonic counter; `device_id`
is persisted across launches per ADR-002 §S5. No code branches on envelope
`type` for integrity flags — flags arrive as `shape_ref=conflict_detected/v1`
and render with a red badge.

## Run the demo (two Android emulators)

Prereqs: Ship-1 server + test DB running.

```bash
# Terminal 1: DB + server
cd /home/hamza/datarun-platform
docker compose -f docker-compose.test.yml up -d
cd server && ./mvnw spring-boot:run
```

```bash
# Terminal 2: seed two CHVs + assignments (returns tokens)
curl -sX POST http://localhost:8080/dev/bootstrap | jq .
# Keep: chv_a_actor_id, chv_a_token, chv_b_actor_id, chv_b_token,
#       village_a, village_b
```

```bash
# Terminal 3: boot two emulators
flutter emulators --launch <avd_1>
flutter emulators --launch <avd_2>
flutter devices   # confirm two emulator IDs

cd /home/hamza/datarun-platform/mobile

# Install on emulator 1 → provision as CHV-A
flutter run -d <emulator-1-id>
#   Server URL: http://10.0.2.2:8080
#   Actor ID:   <chv_a_actor_id>
#   Token:      <chv_a_token>

# Install on emulator 2 → provision as CHV-B
flutter run -d <emulator-2-id>
#   Server URL: http://10.0.2.2:8080
#   Actor ID:   <chv_b_actor_id>
#   Token:      <chv_b_token>
```

Admin surfaces during the demo:

- `http://localhost:8080/admin/events` — full timeline
- `http://localhost:8080/admin/flags` — unresolved flags

### W-0 — happy path

1. CHV-A device: tap **New capture** → pick Village-1 → household "Khan"
   → size 5 → Save.
2. Tap **Sync**. Expected: `push: accepted=1 duplicates=0 flags_raised=0`.
3. Pending count → 0. Row turns green (synced).
4. `/admin/events` shows the capture. `/admin/flags` is empty.

### W-1 — duplicate household across devices

Goal: two CHVs, working offline, both record "Khan" in the same village.
Server flags on second push.

1. Airplane mode ON for both emulators.
2. CHV-A captures in Village-1: "Khan household" / size 5 / Save.
3. CHV-B captures in Village-1: "Khan household" / size 6 / Save.
   (CHV-B's picker only shows Village-2 since config is scope-filtered;
   see "Forcing a W-1 capture" below to cross scopes.)
4. Airplane mode OFF on device A → Sync → `flags_raised=0`.
5. Airplane mode OFF on device B → Sync. Expected: `flags_raised=2`
   (`identity_conflict` + `scope_violation`, retro §3.8).
6. `/admin/flags` shows both flags; a red row appears in the in-app event
   list after the next Sync on each device.

**Forcing a W-1 capture** (CHV-B writing into CHV-A's village). The mobile
picker is scope-filtered on purpose — a CHV UI should not offer villages
they're not assigned to. To reproduce the duplicate-across-scope behaviour:

- Re-run `/dev/bootstrap` with CHV-B also assigned to Village-1 (adjust
  bootstrap seed), or
- Send a hand-rolled envelope with CHV-B's token to `/api/sync/push`
  with `village_ref = village_a`. `WalkthroughAcceptanceTest.walkthrough_W1_duplicate_household`
  is exactly this flow in code.

### W-2 — out-of-scope capture

Same picker constraint. To drive W-2 from the UI: bootstrap a variant where
CHV-A is assigned to Village-1 and Village-2, capture in Village-2, then
post-facto remove the Village-2 assignment by posting an `assignment_ended/v1`.
Otherwise W-2 is a server-side integrity property already covered by
`WalkthroughAcceptanceTest.walkthrough_W2_out_of_scope_capture`; the mobile
UI's refusal to offer unassigned villages is the correct CHV-facing behaviour.

## Deliberate Ship-1 shortcuts

- **Tokens come from `/dev/bootstrap`** — no real provisioning flow.
- **Capture screen refuses on empty local village cache** — first-launch needs
  one successful Sync.
- **No partial-batch retry** — if server 400s a push, pending rows stay pending.
- **Manual Sync only** — no background sync, no connectivity listener.
- **New subject per capture** — identity reconciliation is Ship-2's job.
- **No mobile unit tests** — CI gate is `WalkthroughAcceptanceTest`; this app
  is the demo artifact, not a second test surface.

## Layout

```
mobile/lib/
  main.dart                     # boot: load prefs + open DB
  app.dart                      # MaterialApp + provisioning gate
  data/
    prefs.dart                  # server/token/device_id/device_seq/watermark
    db.dart                     # sqflite: events + villages
  net/
    sync_client.dart            # /api/sync/{config,push,pull}
  screens/
    setup_screen.dart
    home_screen.dart
    capture_screen.dart
```
# datarun_mobile

A new Flutter project.

## Getting Started

This project is a starting point for a Flutter application.

A few resources to get you started if this is your first Flutter project:

- [Learn Flutter](https://docs.flutter.dev/get-started/learn-flutter)
- [Write your first Flutter app](https://docs.flutter.dev/get-started/codelab)
- [Flutter learning resources](https://docs.flutter.dev/reference/learning-resources)

For help getting started with Flutter development, view the
[online documentation](https://docs.flutter.dev/), which offers tutorials,
samples, guidance on mobile development, and a full API reference.
