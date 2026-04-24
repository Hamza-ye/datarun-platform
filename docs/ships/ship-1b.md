# Ship-1b — Real-Device Delivery of S00/S01/S03 under S19

> **Status**: spec ready for build. Slim shape adopted 2026-04-24: §3.2 domain risks are not enumerated pre-build (observed at retro); §6 scope/slice is the thinnest Flutter client against Ship-1's server; §6.4 walkthroughs are Ship-1's W-0/W-1/W-2 re-scripted on real hardware. §3.1 structural risks (R1–R6) remain the ADR-risk surface; they are exercised by running the walkthroughs, not by dedicated walkthrough steps.
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

### 3.2 Domain-realism risks

Not enumerated pre-build. Ship-1b puts the app on real hardware for the first time; which domain realities bite is discovered by running, not by speculation. Categories to watch during build and record in the retro §4 as they surface: **connectivity** (2G/EDGE, intermittent drop), **lifecycle** (OS-initiated app kill, low-memory recycling, reinstall), **input** (script / keyboard for `household_name`). Anything observed that pushes an ADR §S position is promoted to a real risk at retro, not before.

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

## 6. Slice

Thinnest Flutter client that: authenticates with a bearer token, pulls config (activities + shapes + villages in scope), pulls events in scope, renders `household_observation/v1` as a dynamic form, persists captured events locally in an append-only store, and pushes on a manual sync action (plus opportunistic push on network return). One activity, one shape, one scope type, one assignment per CHV — mirrors Ship-1 §6.1 on the device half.

Two instances required (two emulators, or one emulator + one device, or two physical devices). Server is Ship-1's server, unchanged — no server-side work in Ship-1b.

**Starting point**: user-decided between adopting the untracked `mobile/` tree as a skeleton vs. building fresh against Ship-1's server. Not a scope question — an execution question. Record the choice at first commit.

Out of scope: see §6.5.

---

## 6.4 Acceptance walkthroughs

Ship-1b re-delivers Ship-1's three walkthroughs at the device surface. The walkthroughs are not reinvented; they are re-scripted against real hardware. The §3.1 structural risks (R1–R6) are exercised by the act of running these walkthroughs on a real device — retro §4 records which ones surfaced observable trouble.

- **W-0 (happy path, S00).** CHV-A on a real device installs, authenticates, pulls config + scope, captures one in-scope household while offline, syncs on network return. Same assertions as Ship-1 W-0: one `capture` event server-side with `shape_ref=household_observation/v1`, zero `conflict_detected/v1` events, device UI reflects synced state.
- **W-1 (duplicate household, S01).** Two instances. Both offline. Both capture the same household in village-1. CHV-A syncs first, CHV-B second. Same assertion as Ship-1 W-1: server emits `identity_conflict` flag (`conflict_detected/v1`, discriminated on `shape_ref` per F-A2). Both device UIs reflect their own capture's post-sync state.
- **W-2 (out-of-scope capture, S03).** CHV-A attempts a capture in village-2 (not assigned). Syncs. Same assertion as Ship-1 W-2: server emits `scope_violation` flag. Device UI reflects post-sync state.

W-1 and W-2 walkthrough prose requires each device's UI to "reflect post-sync state" for a flagged capture. What that UX actually is — hide, annotate, queue-for-review, silent — is an implementation-grade choice made during build and recorded in retro §3, not pre-specified here. That is where R4 and R5 are observed.

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

1. All three walkthroughs in §6.4 (W-0, W-1, W-2) pass end-to-end on a real device or emulator + Ship-1's server, starting from clean install.
2. Every commit in the Ship-1b range cites at least one scenario ID (`S00`, `S01`, `S03`, or `S19`). `git log --oneline` greppable.
3. No envelope / contract / shape change from Ship-1 tag. `git diff ship-1..HEAD -- contracts/` → empty.
4. No new ADR drafted unless one of R1–R6 (§3.1) or a domain reality surfaced during build triggered one. If one did: ADR merged before tag; charter regenerated; drift gate PASS.
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
