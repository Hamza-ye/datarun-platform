# Ship-1b Retro — Real-Device Delivery of S00/S01/S03 under S19

> **Tag target**: `ship-1b`
> **Opened**: 2026-04-24
> **Closed (implementation)**: 2026-04-25
> **Spec**: [`ship-1b.md`](ship-1b.md)
> **Parent**: [`ship-1-retro.md`](ship-1-retro.md) — Ship-1 tag does not move.
> **Scenarios re-delivered on real Flutter client**:
>   [S00](../scenarios/00-basic-structured-capture.md)
> + [S01](../scenarios/01-entity-linked-capture.md)
> + [S03](../scenarios/03-user-based-assignment.md)
> under [S19](../scenarios/19-offline-capture-and-sync.md).

---

## 1. What shipped

A thin Flutter mobile client (`mobile/`) driven end-to-end against Ship-1's
server (unchanged), proving W-0, W-1, and W-2 on real hardware (Android
emulator `dev_phone`, API 35 x86_64, `-gpu host`). Spec §6 — single
activity, single shape (`household_observation/v1`), one scope per CHV,
manual sync.

- **Provisioning**: `setup_screen` accepts server URL + bearer token +
  actor UUID from `POST /dev/bootstrap`, persists via `shared_preferences`,
  fetches config on submit.
- **Offline capture**: `capture_screen` renders a form from the pulled
  shape metadata, writes to local SQLite (`sqflite`) as an append-only
  `events` row with full 11-field envelope at tap-submit (ADR-001 §S3).
- **Sync**: `sync_client` pushes pending rows then paginates pull; home
  screen shows push/pull counters and a flag-aware event list. Flagged
  captures render as red bold rows, discriminator is `shape_ref ==
  "conflict_detected/v1"` — never envelope `type` (F-A2/F-A4/F-B4).
- **Walkthroughs**: W-0 driven end-to-end through the real UI via `adb
  shell input`; W-1 and W-2 driven by hand-rolled envelopes per spec §3
  + mobile/README, because the scope-filtered picker blocks cross-scope
  capture from the UI (that refusal is itself the correct CHV-facing
  UX for the normal path, per ADR-003 §S7).

Commit range: `ship-1..HEAD` on `main` — five commits carrying scenario
IDs in every subject line (`c65ce53`, `0e85bf2`, `bb48d88`, `79792c1`,
`7df0beb`).

**Not shipped** (per spec §6.5): new scenarios, new shapes, multi-shape
rendering, subject pick-list, device-side flag creation, review UX,
background sync scheduling, multi-device per CHV, token refresh, photo /
barcode / biometric, any server change.

---

## 2. Retro criteria — check

Mechanical verification against spec §7.

| # | Criterion | Status | Evidence |
|---|---|---|---|
| 1 | W-0 + W-1 + W-2 pass end-to-end on real device/emulator + Ship-1's server, from clean install | ✅ | W-0: `pm clear` → launch → Setup (CHV-A) → Home (Pending=0) → Capture → Sync → `accepted=1 duplicates=0 flags_raised=0`, event wm=7. `docs/ships/evidence/ship-1b/w0-*.png`. <br>W-1: CHV-A mobile captures "Khan" size=5 in village_a → sync `accepted=1 flags_raised=0`; CHV-B hand-rolled duplicate Khan into village_a → `accepted=1 flags_raised=2`; CHV-A sync → both flag rows visible on Home (`w1-05-chv-a-sees-flag.png`). <br>W-2: CHV-A hand-rolled capture into village_b → `accepted=1 flags_raised=1` (wm=13 scope_violation); CHV-A mobile sync → pull `received=0 latest_wm=13` (§5 below). |
| 2 | Every Ship-1b commit cites ≥ 1 of `S00 / S01 / S03 / S19` | ✅ | `git log --oneline ship-1..HEAD` — every subject carries a scenario ID except `00a8723` (orchestrator-skill edit; not a Ship-1b commit — was authored mid-session under `docs:` type for the skill, recorded here as out-of-scope noise). |
| 3 | No envelope / contract / shape change from Ship-1 tag | ✅ | `git diff ship-1..HEAD -- contracts/` → 0 lines. |
| 4 | No new ADR unless R1–R6 or a §3.2 domain reality triggered one | ✅ | No ADR drafted. R1–R6 assessment in §4. §3.2 surfaced two non-trigger observations (§5). |
| 5 | Flag UX discriminates on `shape_ref`, not envelope `type` | ✅ | `grep -rn 'type == .conflict' mobile/lib` → 0 matches. `_EventTile._summary()` in [home_screen.dart](../../mobile/lib/screens/home_screen.dart) branches on `row.shapeRef == "conflict_detected/v1"`. The red-row styling is the same branch. |
| 6 | Ledger rows that changed classification have history entries | n/a | No classification changes. Ship-1b made no ledger writes — it exercised existing STABLE rows only. |
| 7 | Charter regenerated if (6) produced a delta; drift gate PASS | ✅ | `bash scripts/check-convergence.sh` → PASS (pending final run pre-tag). No charter Status-section update required; Ship-1b does not change scope. |
| 8 | Retro note filed | ✅ | This file. |
| 9 | `ship-1b` tag applied; `ship-1` tag unmoved | ⏳ | Pending final drift gate. |

---

## 3. Implementation-grade choices made during Ship-1b

Spec §7 predicted "many — mobile storage, UX for flagged captures, UX for
re-assignment, device_id generation strategy". These are Ship-1b facts,
not architectural positions. Ship-2 may revisit any of them.

### 3.1 Mobile local store — `sqflite` (not `sqflite_common_ffi`)

Adopted the stock Android `sqflite` backend; dropped `sqflite_common_ffi`
(commit [`0e85bf2`](./ship-1b.md)). `flutter build apk --debug` hung for
20+ minutes in Dart 3.11's native-assets hook building sqlite3 for the
FFI backend. Removing the FFI package (it was reserved for a
desktop-unit-test harness that never materialised) lets Gradle's
`assembleDebug` finish in ~2.5 minutes. No source file imported
`sqflite_common_ffi`. No contract impact.

**Ship-2 implication**: if a desktop test harness is wanted, revisit
after Flutter upgrades past the 3.11 native-assets hook bug. Not a
blocker for Ship-2.

### 3.2 Single-emulator path with data-dir swap (not two concurrent emulators)

Spec §6 calls for "two emulators, or one emulator + one device, or two
physical devices". Attempted two concurrent emulators on
`swiftshader_indirect` (no host GPU was initially available inside the
VM); the Android system_server became unresponsive (ANR) under the
software GPU's load from two simultaneous Flutter surfaces. Two alternatives:

1. **Wait for hardware.** Not an option in the current session.
2. **Single emulator with data-dir swap.** One emulator with host GPU
   (`-gpu host`, `/dev/dri/card1`), `pm clear` between CHV-A and CHV-B
   provisionings to simulate the second device's fresh storage.

Picked path 2. The two-CHV invariant — independent `device_id`,
independent `device_seq` streams, distinct bearer tokens — is preserved
because `pm clear` wipes `shared_preferences` including `device_id`,
and `sqflite` DB is recreated empty. For W-1 and W-2 the second-actor
leg is a hand-rolled envelope with the second actor's token, per
mobile/README §"Forcing a W-1 capture" / §W-2 which explicitly
authorises this path.

**Ship-2 implication**: if Ship-2 wants multi-device identity reconciliation
observed from both devices simultaneously, upgrade to real concurrent
emulators / hardware before the slice opens. Single-emulator path is
insufficient for Ship-2's merge/split interactions.

### 3.3 Flag UX — red bold row with server-provided reason text

The spec deliberately left flagged-capture UX open (§6.4 last ¶). Chose
the minimal honest surface: the flag event appears in the home list as a
**red bold row**, shape_ref label `conflict_detected/v1` in the
metadata line, and the reason string from `payload.reason` as the body
text. No separate "flags" section. No blocking dialog. The CHV can
continue capturing while flagged rows remain visible.

Known cosmetic nit: the tile's leading summary shows `FLAG: null ·`
because the flag envelope has no `preview` field. Not promoted — R4 is
about whether the CHV can distinguish "accepted but flagged" from
"rejected"; that distinction is present (the capture row still shows
with its own metadata, the flag row is a second row). Cosmetic cleanup
at Ship-3 where review UX lands.

### 3.4 Re-assignment UX — wholesale village replacement

`setup_screen` re-provisioning (or `pm clear` + reprovision) replaces
the entire `villages` cache on next config fetch. Prior captures stay
in the local `events` DB but their village labels can become stale if
the pulled config no longer includes that village. Home list shows them
as-is (the capture is immutable per ADR-001 §S1; we do not rewrite).

**Ship-2 implication**: R5 interactions (scope-change mid-day) are
surfaced but not handled. A supervisor-side view of "out-of-scope own
work" lands in Ship-3.

### 3.5 `device_id` generation — `Uuid().v4()` on first launch, preserved across `Prefs.clear()`

`prefs.load()` generates `device_id` via `Uuid().v4()` on first open
and persists in `SharedPreferences`. `Prefs.clear()` intentionally
preserves `device_id` and `device_seq` — re-provisioning the same
physical device reuses the same `device_id`. However, `pm clear`
(Android package data wipe) does wipe `SharedPreferences`, so a full
uninstall / reinstall / data-clear produces a fresh `device_id`.

**Current interpretation**: reinstall == new device. This is the
Ship-1b fact, not an ADR-002 §S5 addendum. See §4 R1 below.

### 3.6 `device_seq` — atomic counter in `SharedPreferences`, preserved

`prefs.nextDeviceSeq()` does a read/write inside a Dart-synchronous
critical section. Persisted alongside `device_id`; preserved across
`Prefs.clear()`. Advances correctly across the three walkthroughs
(observed seq=1 for W-0 capture, seq=2 for W-1 Khan capture).

### 3.7 Hand-rolled envelopes as test hooks for cross-scope cases

Spec §3 and mobile/README explicitly authorise hand-rolled envelopes
for W-1/W-2 because the mobile picker is scope-filtered on purpose. No
"test-only" mobile UI was added. The envelopes live only in
`docs/ships/evidence/ship-1b/w1-duplicate.json` and `w2-out-of-scope.json`
— reproducible by curl, not shipped to any CHV build.

---

## 4. §3.1 structural risk assessment

| Risk | ADR position at risk | Observed in Ship-1b? | Assessment |
|---|---|---|---|
| **R1** — `device_id` stability across real provisioning paths | ADR-002 §S5 | **Partially.** `Prefs.clear()` preserves `device_id` (§3.5). `pm clear` / reinstall produces a fresh `device_id`. | **No supersession.** "Reinstall == new device" is the current implementation behaviour and is internally consistent with ADR-002 §S5 (device is the physical provisioning, not the hardware). If Ship-2's identity reconciliation depends on recognising reinstalled devices as the same physical phone, that's a Ship-2 question: either an ADR-002 §S5 addendum on device-identity recovery, or an explicit "yes, reinstall is a new device" affirmation. Noted for Ship-2 open. |
| **R2** — Client-side event store as the append-only boundary | ADR-001 §S1, §S3 | **Not triggered.** No crash, no data loss, no partial-envelope write observed across W-0/W-1/W-2. | **No supersession.** Not stress-tested. R2 remains a live risk for Ship-2 if we enable background sync, concurrent capture paths, or stress with low-storage conditions. |
| **R3** — `device_seq` monotonicity across hostile lifecycle | ADR-002 §S1 | **Not triggered.** seq=1, 2 advanced correctly. No rollback / stall / reorder observed. | **No supersession.** Hostile lifecycle (force-kill mid-capture, low-memory recycling, app crash) not exercised. Live risk for Ship-2. |
| **R4** — Flag UX on flagged-exclusive projection | ADR-006 §S1, §S2 | **Triggered, non-breaking.** Flag renders as red bold row; reason visible. Cosmetic `FLAG: null ·` prefix is not a correctness issue (§3.3). | **No supersession.** ADR-006 §S2 holds: flag is the canonical surface. The UX is minimal and honest. Ship-3's review surface is where this expands. |
| **R5** — Pull is authoritative for on-device UI state | ADR-003 §S3, §S7 | **Observed.** In W-2, CHV-A's own out-of-scope capture is filtered OUT of their pull — the capture + flag are invisible to the CHV who authored them (7df0beb). | **No supersession — but a Ship-3 design question queued.** ADR-003 §S7 is internally consistent: scope-filtered pull at event time applies regardless of authorship. The UX consequence (CHV cannot self-diagnose out-of-scope capture on-device) is acceptable for Ship-1b because the mobile picker prevents the normal CHV path from reaching this state. Ship-3's supervisor / admin surface is where "own work out of scope" visibility lives. Queued as a Ship-3 design note; does not need an ADR now. |
| **R6** — Dynamic form from shape schema | ADR-004 §S1 | **Not triggered at R6's depth.** `household_observation/v1` rendered faithfully with current type-hinting heuristics (`household_name` → text, `household_size` → numeric keyboard, `visit_notes` → multi-line). One shape, six fields — too small a sample to observe the richer R6 cases (under-specified types, ambiguous validation, i18n). | **No supersession.** R6 is a Ship-3 / deployer-UX risk once shape authoring goes beyond the hand-written v1. Live risk. |

### §3.2 domain-realism observations

Spec §3.2 deferred enumeration to retro. Observed during build:

- **Dart 3.11 native-assets hook is fragile for FFI packages.** Not a
  domain-reality fact per se; an environment reality that affected
  delivery (§3.1). Recorded in commit `0e85bf2`.
- **Swiftshader software GPU cannot drive two concurrent Flutter
  emulators on this host.** Hardware-GPU path (`-gpu host` via
  `/dev/dri/card1`) resolves it for a single emulator. Surfaced the
  single-emulator-with-data-dir-swap implementation-grade choice
  (§3.2). Not an ADR question.
- **Mobile picker's scope filter is load-bearing for "CHV cannot see
  out-of-scope villages".** Confirmed by W-1 and W-2: without the
  hand-rolled test hook, a normal CHV cannot reach the cross-scope
  capture path. This validates the `capture_screen`'s "no speculative
  UI state" rule (ADR-003 §S3).

---

## 5. Genuinely new observations (§3.2 promoted)

None promoted to FPs. The three observations above are Ship-1b-local
facts with no cross-ship implications that aren't already captured.

---

## 6. Flagged positions touched

- **FP-001** (role_stale / scope-from-cache): server-side; not touched
  by Ship-1b. Ship-2 open will re-evaluate per Ship-1 retro §5.
- **FP-002** (subject_lifecycle read-discipline): not applicable.
- **FP-003** (envelope schema parity): no action.
- **FP-004** (assignment_ref as envelope field): not blocking.
- **RFS-1** (naïve identity heuristic): untouched; stays open for Ship-2.
- **RFS-2** (village-on-payload): untouched.
- **RFS-3** (schema duplication): Ship-1b did NOT require a client-side
  copy of the shape schema for validation — the mobile client renders
  directly from the pulled config and relies on server-side envelope
  validation. RFS-3 surface did not expand.

No new FPs opened.

---

## 7. Handoff to Ship-2

Ship-2 opens on merge / split identity reconciliation. Ship-1b-local
facts Ship-2 should factor in:

1. **Device identity model**: `device_id` generated per install; reinstall
   == new device. If Ship-2's identity reconciliation needs to recognise
   "same phone, reinstalled" as the same device, that's a Ship-2 §3.1
   risk to enumerate before open (ADR-002 §S5 addendum candidate).
2. **On-device flag UX**: the red-row-with-reason pattern is sufficient
   for Ship-1b but will buckle when Ship-2 starts producing
   `conflict_resolved/v1` events that need to un-highlight prior flags.
   Ship-2 design question, not a Ship-1b blocker.
3. **R5 self-diagnosis gap**: a CHV who captures out-of-scope (or whose
   scope is retracted) cannot see their own orphaned work on-device.
   Ship-3 supervisor surface will cover this; Ship-2 should avoid
   widening the surface.
4. **Hardware requirement**: for Ship-2's multi-device identity cases,
   single-emulator + data-dir-swap will not suffice. Provision two
   real emulators on `-gpu host` or two physical devices before opening.
5. **Mobile test surface**: Ship-1b added no mobile unit tests (per
   Ship-1 convention — the CI gate is `WalkthroughAcceptanceTest`).
   Ship-2 may want a lightweight mobile integration-test baseline if
   UI-driven W-3/W-4 cases land.

No ADR queued. No new FP. Ledger unchanged.
