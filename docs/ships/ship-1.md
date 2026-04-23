# Ship-1 — Offline Structured Capture Under Assigned Scope

> **Status**: spec (no code yet)
> **Opened**: 2026-04-24
> **Tag target**: `ship-1`
> **Previous**: — (first Ship post-convergence)

---

## 1. Scenarios delivered

This Ship delivers **S00**, **S01**, **S03** end-to-end on one device + one server, under the **S19** offline constraint.

| Scenario | What the Ship proves |
|---|---|
| [S00 — Recording Structured Information](../scenarios/00-basic-structured-capture.md) | A CHV on a phone captures a record against a known shape, offline, and the server accepts it on sync. |
| [S01 — Recording Information About a Specific Thing](../scenarios/01-entity-linked-capture.md) | The record is tied to an identifiable subject (a household). A second CHV's independent record for the same household produces a detectable duplicate on sync. |
| [S03 — Designated Responsibility](../scenarios/03-user-based-assignment.md) | Each CHV is assigned a village; the server's scope-filtered pull delivers only their assigned subjects and activities. |

**Inherited constraints** (S19, not a deliverable): work proceeds without connectivity; reconciliation happens on sync; `timestamp` is advisory; ordering uses `device_seq` + `sync_watermark`.

**Scenarios deliberately not delivered**: merge/split of duplicate households (S06, Ship-2); supervisor review of captures (S04, Ship-3); periodic reporting rhythm (S02, Ship-5).

---

## 2. ADRs exercised

Every ADR listed is cited with the specific §S whose decision Ship-1 puts under real load. If Ship-1 shipping reveals any of these decisions is wrong, the §S must be superseded per [supersede-rules.md](../convergence/supersede-rules.md).

| ADR §S | What it commits | How Ship-1 exercises it |
|---|---|---|
| [ADR-001 §S1–§S5](../adrs/adr-001-offline-data-model.md) | 11-field envelope; append-only; events are the sync unit | Every captured record is an `Event` with the full envelope; server never modifies; push/pull is event-based. |
| [ADR-002 §S1](../adrs/adr-002-identity-conflict.md) | Causal ordering via `(device_id, device_seq)` + `sync_watermark` | Offline captures from two devices land with non-overlapping `device_seq` space; server orders by `sync_watermark`. |
| [ADR-002 §S3](../adrs/adr-002-identity-conflict.md) | `timestamp` is advisory | No correctness logic keys on `timestamp`. Display only. |
| [ADR-002 §S5](../adrs/adr-002-identity-conflict.md) | `device_id` is stable per-device | Device generates `device_id` on first run; persists in local storage. |
| [ADR-003 §S1–§S7](../adrs/adr-003-authorization-sync.md) | Sync = access scope; scope is a projection; assignments as events | Pull delivers scope-filtered events; scope reconstructed server-side from `assignment_changed` events. |
| [ADR-004 §S1](../adrs/adr-004-configuration-boundary.md) | `shape_ref = {name}/v{N}` | One shape at v1 is shipped to the device as part of the config package. |
| [ADR-004 §S3](../adrs/adr-004-configuration-boundary.md) | Envelope `type` answers *which pipeline* | All Ship-1 captures ride `type=capture`. |
| [ADR-006 §S1](../adrs/adr-006-flag-semantics.md) | Accept-and-flag (property) | Server never rejects a validly-structured event; out-of-scope captures create `scope_violation` flags. |
| [ADR-006 §S2](../adrs/adr-006-flag-semantics.md) | Flag-as-canonical-surface on the event stream | The duplicate-household scenario and the scope-violation scenario both surface as flag events, never as rejections. |
| [ADR-006 §S4](../adrs/adr-006-flag-semantics.md) | Server-side flag creation (default) | Ship-1 creates all flags server-side during sync processing. Device-side creation is not in scope. |
| [ADR-007 §S1](../adrs/adr-007-envelope-type-closure.md) | Envelope `type` closed at 6 values | Only `type=capture` and `type=assignment_changed` are emitted in Ship-1. |
| [ADR-008 §S1](../adrs/adr-008-envelope-reference-fields.md) | `subject_ref` = typed UUID, 4-value enum | Every capture carries `subject_ref = {type: "subject", id: <household-uuid>}`. |
| [ADR-008 §S2](../adrs/adr-008-envelope-reference-fields.md) | `actor_ref` = human UUID or `system:{source_type}/{source_id}` | CHV captures carry human UUID. Server-created flags carry `system:conflict_detector/identity_conflict` (or similar; concrete naming fixed at build time, not here). |
| [ADR-008 §S3](../adrs/adr-008-envelope-reference-fields.md) | `activity_ref` is optional, deployer-chosen | Ship-1 uses exactly one activity (`household_observation`); every capture carries `activity_ref = "household_observation"`. |
| [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md) | Mechanism PRIMITIVE vs. instance CONFIG | `scope` (platform-fixed) ≠ the village-level geographic instances (deployer CONFIG). |
| [ADR-009 §S2](../adrs/adr-009-platform-fixed-vs-deployer-configured.md) | Scope-type registry closed at 3 values | Ship-1 uses `geographic` scope only. |

### Out of scope (explicitly not exercised)

These are called out to prevent scope creep and to make the retro's job unambiguous:

- ADR-002 §S6–§S11 (merge/split mechanism). **Ship-2 territory.** Ship-1 *detects* duplicates (raises `identity_conflict` flag); Ship-1 does **not** resolve them.
- ADR-004 §S4–§S14 (triggers, expressions, deploy-time validation beyond shape/scope compatibility). **Ship-5 territory.**
- ADR-005 in full (patterns, state progression). **Ship-3/4 territory.** Ship-1 has no pattern.
- ADR-006 §S3 (flag resolvability / auto-resolution). **Ship-3 or later.** Ship-1 creates flags; does not resolve them. The admin UI lists unresolved flags; that is the closure for Ship-1.

---

## 3. ADRs at risk of supersession

The whole point of Ship-1 is to put these decisions under adversarial load for the first time. Each risk names the specific position that would break, and the observable that would break it.

| Risk | ADR position at risk | Observable that would force supersession |
|---|---|---|
| **R1 — Scope-filtered pull performance** | [ADR-003 §S2, §S3](../adrs/adr-003-authorization-sync.md) | Scope reconstruction per pull exceeds a bound that makes real field sync infeasible (target: < 1s for a village with 500 subjects + 2000 events over a year on a realistic server). Trigger: a new ADR explicitly committing to a scope cache with invariants, OR `assignment_ref` becoming a real envelope field ([FP-004](../flagged-positions.md#fp-004)). |
| **R2 — `timestamp` really is advisory** | [ADR-002 §S3](../adrs/adr-002-identity-conflict.md) | A real operational need surfaces where the *display order* of captures diverges from the *causal order* in a way that confuses CHVs or supervisors. If so, ADR-002 §S3 must either be re-affirmed with UX mitigation, or amended. |
| **R3 — `device_id` stability** | [ADR-002 §S5](../adrs/adr-002-identity-conflict.md) | Device factory-reset or reinstall in the field produces a new `device_id` for the same physical phone, with no recovery path. If Ship-1 reveals this is common, an ADR addendum on device identity recovery is required (not today — only if observed). |
| **R4 — `subject_ref` sufficient for households** | [ADR-008 §S1](../adrs/adr-008-envelope-reference-fields.md) | A household record needs to point at another household (parent, split-off, adjacent) in a way the 4-value enum cannot express. This is a Ship-4 expected risk (S22), not a Ship-1 expected one, but called out because households are the Ship-1 subject type and it is better to discover earlier than later. |
| **R5 — Accept-and-flag under S19** | [ADR-006 §S1, §S4](../adrs/adr-006-flag-semantics.md) | A field situation surfaces where raising a flag (e.g., `scope_violation`) is worse than rejecting — the flag becomes ambient noise the CHV cannot act on. ADR-006 would need a revisit on flag emission location/suppression. |

---

## 4. Ledger concepts touched

Ship-1 touches the following ledger rows. Most are already STABLE; a few may move.

| Concept | Current | Ship-1 impact |
|---|---|---|
| `event` [PRIMITIVE] | STABLE | Exercised end-to-end; should remain STABLE. |
| `subject` [PRIMITIVE] | STABLE | Exercised at the household level; should remain STABLE. |
| `actor` [INVARIANT] | STABLE | CHV human actors + server system actors both used. |
| `device` [CONTRACT / PRIMITIVE — verify] | STABLE | First real-device exercise; any sloppiness surfaces here. |
| `scope` [PRIMITIVE] | STABLE (ADR-009 §S2) | Geographic scope used; closure-at-3-values exercised by using only one type. |
| `assignment` [INVARIANT] | STABLE | Assignment events created; scope reconstructed from them. |
| `assignment_created/v1` [CONTRACT] | STABLE | Shape emitted; `assignment_changed` envelope type carries it. |
| `scope_violation` [FLAG] | STABLE (ADR-006 §S2) | First real-world emission path exercised. |
| `identity_conflict` [FLAG] | STABLE (ADR-006 §S2) | Duplicate household detection emits this flag; **Ship-1 does not resolve it** — Ship-2 does. |
| `accept-and-flag` [INVARIANT] | STABLE | Exercised for both out-of-scope capture and duplicate subject. |
| `subject_ref` [CONTRACT] | STABLE | First real exercise of the 4-value enum. |
| `actor_ref` [CONTRACT] | STABLE | Human UUID and `system:*` both exercised. |
| `activity_ref` [CONTRACT] | STABLE | One activity (`household_observation`) used. |
| `household_observation/v1` | **NEW** — first deployer shape | Added as deployer CONFIG; registered in config package; `shape_ref` used by all captures. |
| `village` scope type | existing under `scope` PRIMITIVE | First real geographic scope instance. |

Expected status changes at retro: none anticipated. If any STABLE row becomes unstable during Ship-1 (a field observation contradicts its classification), that row moves to DISPUTED and a retro ADR is drafted.

---

## 5. Flagged positions — consult ([Rule R-4](../flagged-positions.md))

| FP | Status | Ship-1 interaction |
|---|---|---|
| [FP-001](../flagged-positions.md) — `role_stale` projection-derived role verification | OPEN | **Not blocking.** FP-001's gate references existing `server/src/main/java/.../ConflictDetector.java`, which is being discarded. Ship-1 must implement scope reconstruction from `assignment_changed` events by construction — a cache-based shortcut is a build-time red flag. The replacement gate for Ship-1: a test exists where an actor's scope is reconstructed at event time by replaying assignment events, not read from any cache. That test IS the Ship-1 scope-filtered pull acceptance test. FP-001 is re-targeted against the new code at Ship-1 retro. |
| [FP-002](../flagged-positions.md) — `subject_lifecycle` read-discipline audit | OPEN | **Not applicable to Ship-1.** FP-002 is about merge/split lifecycle state — Ship-2 territory. Ship-1 does not write to any subject lifecycle table. Re-assessed at Ship-2 start. |
| [FP-003](../flagged-positions.md) — envelope schema parity test | RESOLVED | No action. |
| [FP-004](../flagged-positions.md) — `assignment_ref` as potential future envelope field | OPEN | **Not blocking Ship-1.** No Ship-1 emission site requires referencing an assignment distinctly from its subject. Re-evaluated at Ship-4. |

**New FPs opened by this spec**: none anticipated. Any risk surfaced during Ship-1 build that cannot be addressed in-scope gets a new FP before commit.

---

## 6. Slice — the thinnest vertical

Ship-1 is one device + one server + one activity + one shape + one assignment + one sync cycle, with two induced adversarial scenarios.

### 6.1 Deployer config (L0 + L1)

- **One activity**: `household_observation` (plain, no pattern, no triggers, no expressions).
- **One shape**: `household_observation/v1` with six fields:
  - `household_name` (text, required)
  - `head_of_household_name` (text, required)
  - `household_size` (integer, required, ≥ 1)
  - `latitude` (decimal, optional)
  - `longitude` (decimal, optional)
  - `visit_notes` (narrative, optional)
- **One scope type**: `geographic`.
- **Geographic tree**: 1 district → 2 villages (deployer seed). Subject locations are villages.
- **Two actors**: CHV-A assigned to village-1; CHV-B assigned to village-2. Assignments emitted as `type=assignment_changed` events with `assignment_created/v1` shape.
- **Config package v1**: assembled server-side, delivered to both devices at first sync.

### 6.2 Devices

- Two physical devices (or emulators) each bound to one CHV actor.
- Each device:
  - Generates a stable `device_id` on first launch.
  - Persists `device_seq` as a monotonic counter.
  - Downloads config package v1 on first sync.
  - Offers a capture form driven by the shape.
  - Stores events locally until push.

### 6.3 Server

- Receives pushed events over the sync protocol.
- For each event: validates envelope → validates payload against shape → persists → runs integrity detection (identity duplicate, scope violation) → emits flag events for detected anomalies.
- Serves scope-filtered pull: for actor X, returns events where scope reconstruction shows the event is within X's assignments at event time.
- Admin UI: lists all events, lists unresolved flags.

### 6.4 Adversarial walkthroughs (the acceptance criteria)

Two induced scenarios, scripted. If either walkthrough fails, Ship-1 is not done.

**W-1: Duplicate household across devices.**
1. Both devices are offline.
2. CHV-A registers "Khan household" (village-1) on device-A with 5 members.
3. CHV-B registers "Khan household" (village-1, same GPS) on device-B with 6 members — unaware of CHV-A's record.
4. Both devices come online. Device-A syncs first; device-B syncs second.
5. **Assert**: server holds two `capture` events, both `household_observation/v1`. Server emits one `identity_conflict` flag (shape `conflict_detected/v1`, `type=alert`, `actor_ref=system:conflict_detector/identity_conflict`), pointing at both events.
6. **Assert**: admin UI shows the flag in its unresolved list. Ship-1 does not resolve the flag; Ship-2 does.
7. **Assert**: both capture events are visible in their respective devices' pulls. Projection (subject list) shows both events excluded from the canonical household-view pending resolution (flag-excluding projection per ADR-006 §S1).

**W-2: Out-of-scope capture.**
1. Device-A (CHV-A, village-1 only) is offline.
2. CHV-A, by UI mis-selection or data corruption, captures a household record with `subject_ref` pointing at a household in village-2.
3. Device-A syncs.
4. **Assert**: server accepts the event (accept-and-flag — no rejection). Server emits one `scope_violation` flag.
5. **Assert**: admin UI shows the `scope_violation` flag.
6. **Assert**: CHV-A's next pull does **not** return the village-2 subject's event timeline — scope filtering still holds on the read side.

**W-0 (happy path, implicit)**: a normal capture by CHV-A in village-1 completes end-to-end with no flags. Implied; not separately listed.

### 6.5 What is deliberately not built

- No pattern support. No review surface. No transition_violation flag.
- No triggers. No `context.*`. No expression evaluator.
- No merge/split resolution. Identity flags stay open.
- No shape evolution (no v2). One shape, one version.
- No deployer admin surface for shape authoring — shapes are bootstrapped in code for Ship-1; Ship-3 or later introduces a deployer-facing shape CRUD.
- No role-action enforcement (former IDR-020 scope).
- No flag severity / domain uniqueness policy surface.
- No field-level sensitivity classification.
- No aggregation surface (dashboards, cross-subject reports).
- **No rebuild from existing code.** The existing `server/` and `mobile/` are treated as historical. Ship-1 is a fresh implementation against the charter. Code from the existing tree may be consulted for patterns but not copied without audit against ADRs.

---

## 7. Retro criteria

Ship-1 is done when all of the following hold. Each is mechanically verifiable.

1. Walkthroughs **W-1** and **W-2** pass end-to-end on two real (or emulated) devices + one server, starting from a clean build.
2. Every commit in the Ship-1 range cites at least one scenario ID (`S00`, `S01`, or `S03`). `git log --oneline` greppable for `S0[013]`.
3. The envelope schema in `contracts/envelope.schema.json` is unchanged from HEAD at Ship-1 open.
4. No new ADR drafted *during* Ship-1 unless one of the risks (R1–R5 §3) triggered. If one did: the ADR is merged before the Ship tag; the charter is regenerated; drift gate passes.
5. All five integrity/flag events observed in walkthroughs are correctly discriminated on `shape_ref`, never on envelope `type` (F-A2, F-A4, F-B4). A grep of the new code for `type == "conflict_detected"` or similar returns nothing.
6. Ledger: any row that changed classification during Ship-1 has a history entry citing the Ship-1 observation and, if applicable, the superseding ADR.
7. Charter: regenerated after retro. Any new invariant/contract/forbidden pattern added is cite-backed. Drift gate PASS.
8. Retro note filed at `docs/ships/ship-1-retro.md` with: scenarios shipped, ADRs confirmed, ADRs superseded (if any), FPs resolved / opened, ledger deltas, hand-off context for Ship-2.

---

## 8. Hand-off to Ship-2

At Ship-1 retro close, this section documents what Ship-2 inherits.

*(Populated at retro.)*

Expected handoffs:
- One or two open `identity_conflict` flags from W-1 — Ship-2 resolves them via `subjects_merged/v1`.
- The household subject as the first long-running subject type — Ship-2 extends to merge/split + shape evolution.
- The scope-filtered pull implementation, proven under real assignment churn — Ship-2 extends it with subject-level scope and shape-version routing.
