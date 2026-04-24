# Ship-1b — Real-Device Delivery of S00/S01/S03 under S19

> **Status**: spec scaffold (no code yet). **Orchestrator disclosure**: §1, §2, §3 (structural), §4, §5, §6.5, §7 pre-filled from cites. **§3 (domain realism), §6 (scope/slice), §6.4 (walkthroughs) are user-owned and blank pending your fill.** Walkthrough brainstorm is raw material, not a proposal. Pressure-test pending once user-owned sections are drafted.
>
> **Opened**: 2026-04-24
> **Tag target**: `ship-1b`
> **Previous**: [`ship-1.md`](ship-1.md) — scenarios proved by scripted two-device HTTP simulation. Ship-1b proves the same scenarios on a real Flutter client + real device or emulator.
> **Parent Ship's tag does not move**: `ship-1` remains at its committed SHA.

---

## 1. Scenarios delivered

Ship-1b re-delivers **S00**, **S01**, **S03** under **S19** — **same scenarios as Ship-1, different delivery surface**. No new scenarios.

| Scenario | What Ship-1b adds beyond Ship-1 |
|---|---|
| [S00](../scenarios/00-basic-structured-capture.md) | Capture occurs on a real Flutter UI driven by `household_observation/v1`, persisted locally while offline, and pushed on sync. |
| [S01](../scenarios/01-entity-linked-capture.md) | Identity duplicate emerges from **two real devices** operating independently, not from one HTTP test client acting as two. |
| [S03](../scenarios/03-user-based-assignment.md) | A CHV installs the app, authenticates, pulls their scope, and sees only their village's events and config. |
| [S19](../scenarios/19-offline-first.md) | **Real** offline-first behaviour: airplane mode, low battery, app restart, reinstall, intermittent connectivity. Ship-1's two-client simulation could not exercise any of these. |

**Why a separate Ship, not a side-quest**: Ship-1 proved the server can serve two simulated devices over `TestRestTemplate`. The *load-bearing scenario of Ship-1* — S19 (offline-first) — is proved by construction (event-based sync protocol) but not by observation. Ship-1b puts the client half of S19 under adversarial load for the first time. That touches ADR-001, ADR-002 §S5, ADR-006, and the device UX for ADR-003 §S7 pull filtering — enough ADR surface that side-quest discipline is insufficient. See the skill's *Side-quest lifecycle* section for the classification test.

**Scenarios deliberately not delivered**: anything not already in Ship-1 (S02, S04, S06, S07, S08, …). No new deployer shapes. No pattern. No merge/split. No triggers.

---

## 2. ADRs exercised

Same ADR surface as Ship-1 §2, but exercised on the **device half** of the system for the first time. Only positions that Ship-1 did not meaningfully put under load are listed here.

| ADR §S | What it commits | How Ship-1b exercises it for the first time |
|---|---|---|
| [ADR-001 §S1–§S5](../adrs/adr-001-offline-data-model.md) | 11-field envelope; append-only; events are the sync unit; offline-first is a property, not a feature | Client-side event store (SQLite or equivalent) is append-only; device never rewrites or deletes an event; every captured form is an `Event` from the moment of tap-submit, not a form-state object that *becomes* an event on sync. |
| [ADR-002 §S1](../adrs/adr-002-identity-conflict.md) | Causal ordering via `(device_id, device_seq)` | `device_seq` is a client-side monotonic counter persisted across app restart, app kill, OS kill, and low-memory recycling. |
| [ADR-002 §S5](../adrs/adr-002-identity-conflict.md) | `device_id` is stable per-device | `device_id` generated on first launch, persisted in device storage, survives app restart. First real exercise — Ship-1 faked this with a random UUID per `TestRestTemplate`. |
| [ADR-003 §S2, §S3](../adrs/adr-003-authorization-sync.md) | Sync = access scope; scope is a projection | Device's view of the world (visible villages, visible subjects, visible events) is strictly the scope-filtered pull response. No speculative UI state pre-populated from config. |
| [ADR-003 §S7](../adrs/adr-003-authorization-sync.md) | Pull is scope-filtered at event time for reads | Device receives only in-scope events on pull; UI has no path to show out-of-scope subjects, even transiently. |
| [ADR-004 §S1](../adrs/adr-004-configuration-boundary.md) | `shape_ref = {name}/v{N}` | Device renders a capture form dynamically from the shape it pulled in config v1. Not a hand-coded form. |
| [ADR-006 §S1, §S2](../adrs/adr-006-flag-semantics.md) | Accept-and-flag; flag-as-canonical-surface | First device UX encounter with a flagged capture: after sync, CHV's own capture surfaces as flagged. The UI must present this without either hiding it (losing honesty) or blocking the CHV (losing work). |
| [ADR-008 §S1, §S2](../adrs/adr-008-envelope-reference-fields.md) | `subject_ref` typed enum; `actor_ref` human UUID or `system:*` | Device writes `subject_ref = {type:"subject", id: <uuid>}`; the bearer token resolves to the CHV's human `actor_ref` on the server side. |

### Out of scope (explicitly not exercised)

- Multi-shape rendering (only `household_observation/v1`).
- Subject pick-list from existing household subjects (Ship-2 territory once merge/split lands).
- Device-side flag creation (ADR-006 §S4: server-side only in Ship-1; unchanged here).
- Review / resolution UX (Ship-3).
- Background sync scheduling beyond "manual sync button + opportunistic on-network-return".
- Multi-device per CHV.
- Token refresh, rotation, expiry (inherits Ship-1's §3.1 implementation-grade choice).

---

## 3. ADRs at risk of supersession

### 3.1 Structural risks (orchestrator-drafted from §S positions)

| Risk | ADR position at risk | Observable that would force supersession |
|---|---|---|
| **R1 — `device_id` stability across real provisioning paths** | [ADR-002 §S5](../adrs/adr-002-identity-conflict.md) | App reinstall / factory reset / backup-restore produces a new `device_id` for the same physical phone, and subsequent captures appear to the server as coming from a new device. If Ship-1b reveals this is frequent or silent, ADR-002 §S5 needs an addendum on device identity recovery or an explicit "yes, reinstall is a new device" position. |
| **R2 — Client-side event store as the append-only boundary** | [ADR-001 §S1, §S3](../adrs/adr-001-offline-data-model.md) | App crashes mid-capture, low-storage pressure, OS force-kill, or a corrupted SQLite page produces **lost events**, **partial events written without envelope completeness**, or **duplicate events on retry**. Any of these forces either a Ship-local fix or an ADR on client-side durability guarantees. |
| **R3 — `device_seq` monotonicity across hostile lifecycle events** | [ADR-002 §S1](../adrs/adr-002-identity-conflict.md) | `device_seq` rolls back (app reinstall restores a pre-backup seq), stalls (lost counter persistence), or reorders (concurrent capture paths) — the causal ordering contract breaks. Forces either an ADR on client counter recovery or a server-side tolerance position. |
| **R4 — Flag UX on a flagged-exclusive projection** | [ADR-006 §S1, §S2](../adrs/adr-006-flag-semantics.md) | The CHV cannot distinguish "my capture was accepted but flagged as a duplicate" from "my capture was rejected" without server-side flag visibility on the device. If the only honest UX is one the device cannot render offline-first, ADR-006 §S2 needs revisiting. |
| **R5 — Pull is authoritative for on-device UI state** | [ADR-003 §S3, §S7](../adrs/adr-003-authorization-sync.md) | CHV re-assigned mid-day: previously-captured events for village-1 disappear from the pull on next sync. The UX either hides them (the projection is honest but the CHV's own work "vanishes") or keeps a client-side shadow (violates ADR-003 §S3 — authority context is a projection, not a device cache). Forces either an ADR on CHV-facing authorship view vs. scope view, or an implementation-grade decision recorded at retro. |
| **R6 — Dynamic form from shape schema** | [ADR-004 §S1](../adrs/adr-004-configuration-boundary.md) | A real shape produces a form that cannot be rendered faithfully (field types under-specified, validation ambiguous, i18n unaccounted). Forces either a shape-grammar addendum on form-renderability or a Ship-3 deployer-UI position. |

### 3.2 Domain-realism risks — **user-owned, blank**

*Orchestrator cannot judge field realism. Fill in risks you would see from CHV context that the §S positions don't cover. Examples of the kind of thing that belongs here (not proposals):*

- Low-end Android hardware, 2G / EDGE connectivity, GPS permission churn, app-background-killer behaviour on Chinese Android OEMs, language / script input for `household_name`, photo capture expectations (Ship-1 has none — would CHVs expect one?), CHV battery anxiety driving incomplete captures, supervisors sharing a device, …

Pick the 2–4 that matter. For each: specific observable condition under Ship-1b that would surface it.

---

## 4. Ledger concepts touched

| Concept | Current | Ship-1b impact |
|---|---|---|
| `event` [PRIMITIVE] | STABLE | First real client-side persistence. Any drift here forces an ADR. |
| `device` [CONTRACT / PRIMITIVE] | STABLE | First real device provisioning. R1 + R3 directly stress this row. |
| `actor` [INVARIANT] | STABLE | Real bearer-token-based auth on a real device. |
| `scope` [PRIMITIVE] | STABLE | Exercised on the read side for the first time on a real client. |
| `accept-and-flag` [INVARIANT] | STABLE | First exercise of flag-on-device UX. |
| `household_observation/v1` | deployer CONFIG instance (Ship-1) | First render as a dynamic form on a real client. |

Expected classification changes: none. If R1–R6 trigger, a ledger row may move to DISPUTED and an ADR is drafted.

---

## 5. Flagged positions — consult ([Rule R-4](../flagged-positions.md))

| FP | Status | Ship-1b interaction |
|---|---|---|
| [FP-001](../flagged-positions.md) — `role_stale` / scope-from-cache | OPEN, retargeted at `ScopeResolver` at Ship-1 retro | **Not blocking.** Server side is unchanged by Ship-1b. Re-evaluated at Ship-2 open per Ship-1 retro §5. |
| [FP-002](../flagged-positions.md) — `subject_lifecycle` read-discipline | OPEN | **Not applicable.** Ship-1b does not touch subject_lifecycle tables. |
| [FP-003](../flagged-positions.md) — envelope schema parity | RESOLVED | No action. |
| [FP-004](../flagged-positions.md) — `assignment_ref` as envelope field | OPEN | **Not blocking.** |

**Inherited Ship-1 RFS items**:

- **RFS-1** (naïve identity heuristic): server-side, not touched by Ship-1b. Stays open for Ship-2.
- **RFS-2** (village-on-payload): Ship-1b renders the shape as-is; does not change the payload shape. Stays open.
- **RFS-3** (schema duplication): **if** the Ship-1b client needs a copy of the shape schema for client-side validation, RFS-3 surface expands. Orchestrator flag: address at close if it did.

**New FPs expected**: none anticipated. Any R1–R6 observation that cannot be resolved in-scope gets a new FP before commit.

---

## 6. Slice — **user-owned, blank**

*Orchestrator pressure-test question: what is the thinnest real-device implementation that makes the walkthroughs pass?*

Candidates the user should decide among (not proposals):

- **Adopt existing `mobile/` tree** (built compile-clean during mobile-1 exploration; currently untracked). If adopted, Ship-1b is a discipline-and-testing Ship, not a build-from-scratch Ship.
- **Build fresh.** Treat `mobile/` as reference material, like Ship-1 treated the prior server. The charter conformance cost is lower but the build cost is higher.
- **Hybrid.** Adopt the skeleton; rewrite capture / sync / storage paths against Ship-1's server surface.

Required elements regardless of the above choice:

- One activity, one shape (`household_observation/v1`), one scope type, one assignment per CHV — same as Ship-1 §6.1.
- Real device or emulator capable of offline-mode toggling.
- Two instances (two emulators, or one emulator + one device, or two physical devices).
- Server is Ship-1's server, unchanged. No server-side work in Ship-1b except fixing the disclosures of Ship-1 retro if they surface as blockers (RFS-3 minimally).

Fill §6.1–§6.3 with the slice you choose. Orchestrator will pressure-test "is this the thinnest?" once filled.

---

## 6.4 Adversarial walkthroughs — **user selects from brainstorm below**

<!-- ORCHESTRATOR BRAINSTORM — WALKTHROUGH CANDIDATES
These are candidates for W-0 through W-6 — select, modify, combine, or
discard. Orchestrator has not validated field realism (§3.2 is yours).

W-0 (HAPPY PATH — NOT OPTIONAL):
  C0. CHV-A installs app, enters bearer token, pulls config, pulls scope,
      captures one in-scope household (offline), taps sync while online,
      server receives one capture event with correct envelope, no flags.
      → assertion: one `capture` event in server store with
      `shape_ref=household_observation/v1`, zero `conflict_detected/v1`
      events, device UI shows the capture as synced.

R1 (ADR-002 §S5 — device_id stability):
  C1. CHV-A installs, captures 3 households, syncs. Uninstall. Reinstall.
      Capture 1 more household, sync.
      → assertion: server receives the 4th event with a DIFFERENT
      `device_id` than the first 3. No crash. No duplicate. This is the
      observation that proves §S5 is either fine ("new install = new
      device, accepted") or needs an addendum.
  C2. CHV-A installs, captures, clears app data via OS settings, captures,
      syncs.
      → same assertion.

R2 (ADR-001 §S1, §S3 — client event store durability):
  C3. CHV-A captures 3 households offline. Force-kill app from OS. Relaunch.
      Sync.
      → assertion: all 3 events persist and push. No partial envelopes
      (every event has all 11 fields). No duplicates on retry.
  C4. CHV-A captures offline, toggles airplane mode off → on → off during
      sync push. Relaunch app. Sync.
      → assertion: idempotent push. Server-side event count is correct.
      Client-side "synced" state is correct (no ghosts, no orphans).

R3 (ADR-002 §S1 — device_seq monotonicity):
  C5. CHV-A captures 5 households across 3 app launches (2, 2, 1), offline
      throughout. Sync once at end.
      → assertion: all 5 events have strictly increasing device_seq, no
      gaps forced by app lifecycle, no rollback after any launch.

R4 (ADR-006 §S1, §S2 — flag UX on device):
  C6. W-1-style duplicate (both devices offline, both capture "Khan
      household" in village-1). CHV-A syncs first. CHV-B syncs second.
      Both pull after sync.
      → assertion: server emits `identity_conflict` flag. Both devices see
      their own capture on the device. What does each device's UI SHOW for
      the flagged capture? This is the observation that exercises R4 —
      the acceptable answers are your call, the important part is that
      the walkthrough pins down what "honest flag UX offline-first" looks
      like. The retro records whichever answer you choose as
      implementation-grade.
  C7. CHV-A captures out-of-scope (village-2) household, syncs.
      → assertion: server emits `scope_violation` flag. Device shows ???
      (same open question as C6).

R5 (ADR-003 §S3, §S7 — pull as authoritative):
  C8. CHV-A captures 3 in-village-1 households, syncs. Admin re-assigns
      CHV-A to village-2 (via `assignment_changed` event). CHV-A pulls.
      → assertion: CHV-A's pull no longer contains the village-1 events.
      What does the UI show for the 3 captures the CHV MADE and which
      are still on-device? This is the observation that proves R5.

R6 (ADR-004 §S1 — dynamic form):
  C9. CHV-A captures a household. UI is rendered dynamically from
      `household_observation/v1`. Required fields enforce validation
      client-side.
      → assertion: this is really a W-0 sub-check, not a distinct
      walkthrough. Fold into W-0 if no separate concern.

User: pick 4–6 of these (or write new ones). W-0 is mandatory. At least
one walkthrough per ADR risk you keep live in §3.1. Mark unused candidates
as "considered, not shipped" for the Ship-1b retro record.
-->

---

## 6.5 What is deliberately not built

- No new scenarios (same three as Ship-1 + S19).
- No new shapes. No shape authoring UI. No multi-shape rendering.
- No subject pick-list (merge/split is Ship-2).
- No device-side flag creation.
- No review UX.
- No background sync scheduler beyond "manual sync + on-network-return opportunistic".
- No multi-device-per-CHV.
- No token refresh / rotation / expiry.
- No photo capture. No barcode. No biometric.
- No server changes except a possible RFS-3 cleanup if adopted as Ship-1b prep.

---

## 7. Retro criteria

Ship-1b is done when all of the following hold.

1. All walkthroughs selected in §6.4 pass end-to-end on a real device or emulator + Ship-1's server, starting from clean install.
2. Every commit in the Ship-1b range cites at least one scenario ID (`S00`, `S01`, `S03`, or `S19`). `git log --oneline` greppable.
3. No envelope / contract / shape change from Ship-1 tag. `git diff ship-1..HEAD -- contracts/` → empty.
4. No new ADR drafted unless one of R1–R6 (§3.1) or a user-drafted domain risk (§3.2) triggered. If one did: ADR merged before tag; charter regenerated; drift gate PASS.
5. All integrity/flag UX discriminated on `shape_ref`, never on envelope `type` (F-A2, F-A4, F-B4). Grep the client code for `type == "conflict_detected"` or `type == "capture"` as a UI discriminator → zero matches.
6. Ledger: any row that changed classification has a history entry citing the Ship-1b observation.
7. Charter: regenerated if (6) produced a delta. Drift gate PASS.
8. Retro note filed at `docs/ships/ship-1b-retro.md` covering: walkthroughs passed, ADR risks assessed (R1–R6 + §3.2), implementation-grade choices made (expected: many — mobile storage, UX for flagged captures, UX for re-assignment, `device_id` generation strategy), FPs touched, ledger deltas, handoff to Ship-2.
9. `ship-1b` tag applied. Parent `ship-1` tag does **not** move.

---

## 8. Hand-off to Ship-2

*(Populated at retro.)*

Expected handoffs:

- First real-device observations for Ship-2 to inherit about `device_id` stability, client event store durability, and scope-change UX.
- Any RFS items promoted to platform FPs.
- Ship-2's spec §3 should pre-include any R1–R6 observation that survived Ship-1b untriggered but remains live at larger fixture sizes.
