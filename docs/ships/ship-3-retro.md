# Ship-3 Retro — Shape Evolution (S06b)

> **Tag target**: `ship-3`
> **Opened**: 2026-04-26
> **Closed (implementation)**: 2026-04-27
> **Spec**: [`ship-3.md`](ship-3.md)
> **Parent**: [`ship-2-retro.md`](ship-2-retro.md) — `ship-1`, `ship-1b`, `ship-2` tags do not move.
> **Scenarios shipped**: [S06b](../scenarios/06-entity-registry-lifecycle.md#06b-when-the-shape-of-information-changes) (versioning half — additive evolution + deprecation under multi-version load) under [S19](../scenarios/19-offline-capture-and-sync.md). Composite [S20](../scenarios/20-chv-field-operations.md) bullets 1 + 5 stressed under shape evolution.

---

## 1. Walkthroughs that passed

Four new walkthroughs added in [`Ship3WalkthroughAcceptanceTest`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship3WalkthroughAcceptanceTest.java) (sibling of `WalkthroughAcceptanceTest` and `Ship2WalkthroughAcceptanceTest`, Ship-1 / Ship-2 precedent). All four pass against the real HTTP surface; Ship-1 + Ship-1b + Ship-2 walkthroughs untouched and remain green. Suite total: **46/46** (`./mvnw test`, `Tests run: 46, Failures: 0, Errors: 0`); was 33/33 at Ship-2 close.

| W | Method | ADR §S exercised | Commit |
|---|---|---|---|
| W-6 | [`walkthrough_W6_additive_happy_path_with_FP009_closure`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship3WalkthroughAcceptanceTest.java) | ADR-004 §S10 (additive evolution); ADR-001 §S2 (multi-version projection on demand); FP-009 closure (v1-current direction) | `05101ad` |
| W-7 | [`walkthrough_W7_deprecation_field_stored_verbatim_both_versions_render`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship3WalkthroughAcceptanceTest.java) | ADR-004 §S10 (deprecation class); ADR-001 §S1 (immutability — deprecated field stored verbatim); ADR-004 §S6 simulated at the HTTP layer (two devices on different `shape_ref` values) | `05101ad` |
| W-8 | [`walkthrough_W8_unknown_shape_ref_rejected_400_shape_unknown`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship3WalkthroughAcceptanceTest.java) | Ship-1 strict-unknown-`shape_ref` choice preserved under the multi-version registry (both unknown-version and unknown-name cases) | `05101ad` |
| W-10 | [`walkthrough_W10_backward_compat_mixed_version_admin_render`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship3WalkthroughAcceptanceTest.java) | ADR-004 §S10 ("all versions remain valid forever"); R5 / R2 stressed; S20 bullet 5 (history of what was done) under multi-version timeline rendering | `05101ad` |

**Inheritance not regressed.** Ship-1 W-0 / W-1 / W-2 + Ship-1b M1 / M2 + Ship-2 W-3 / W-4 / W-5 all green at Ship-3 close. Verified mechanically: `./mvnw test` runs the full sibling-acceptance set in one pass; failing any prior walkthrough would fail the build.

**W-9 absorbed into a unit test, not a walkthrough.** §S13 60-field budget enforcement runs at registry-load per spec §6.1 sub-decision 5; landed as [`FieldCountBudgetTest`](../../server/src/test/java/dev/datarun/ship1/event/FieldCountBudgetTest.java) (commit `0107da9`). The unit-test-not-walkthrough shape is itself FP-012 trigger evidence (§4 DR-2 below).

---

## 2. §7 retro criteria — evidence

Every numbered criterion from spec §7 mechanically verified against HEAD.

| # | Criterion | Status | Evidence |
|---|---|---|---|
| 1 | Walkthroughs pass via HTTP; Ship-3 walkthroughs (W-6 + the additional ones from §6.4) pass; suite total grows; no prior tests removed | ✅ | `./mvnw test` → `Tests run: 46, Failures: 0, Errors: 0`. Sibling class [`Ship3WalkthroughAcceptanceTest`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship3WalkthroughAcceptanceTest.java) (commit `05101ad`); 33/33 → 46/46 (4 walkthrough tests + `FieldCountBudgetTest` + `ShapePayloadValidatorTest` extensions). |
| 2 | Every Ship-3 commit cites a scenario (`S06b` / `S20` / `S19`) | ✅ | `git log --oneline ship-2..HEAD` — every `(ship-3)`-scoped commit subject carries `S06b` (with `S20` / `S19` co-cites where the slice exercises composite coverage). Two `chore(...)` commits in range without scenario cites (`a62b6b5` skill, `4a4687f` orchestrator H9 rule); permitted exemption per §7.2 / Ship-2 precedent — recorded in §10. |
| 3 | ADR-004 §S10 exercised: (a) v1 events remain valid, (b) v2 events validate against v2, (c) projection routes on `shape_ref` | ✅ | (a) W-10 pre-loads a v1 capture and asserts it remains readable after v2 deploy; (b) W-6 step 2 + W-7 push v2 captures and assert validation success; (c) [`ShapePayloadValidator`](../../server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java) keys on `shape_ref` (HashMap registry); [`SyncController.inScopeForPull`](../../server/src/main/java/dev/datarun/ship1/sync/SyncController.java) discriminates on `shape_ref` prefix; admin `events.html` renders per-row by `shape_ref` string match. Branching on `shape_ref` everywhere — F-A2 honoured. |
| 4 | ADR-004 §S13 budget enforced: a unit test rejects > 60-field shape registration | ✅ | [`FieldCountBudgetTest`](../../server/src/test/java/dev/datarun/ship1/event/FieldCountBudgetTest.java) (commit `0107da9`) registers a synthetic 61-field shape and asserts `ShapePayloadValidator.enforceFieldCountBudget` rejects at registry-load. Counting convention (top-level `properties` only; recursion not flattened) documented in the method javadoc and class comment per spec §7 row 4. |
| 5 | FP-007 drift-gate stays PASS; check 4 byte-identical after any shape edit | ✅ | `bash scripts/check-convergence.sh` exit 0 at HEAD. Last 6 lines: `[2/4] concept-ledger.md settled-by cites` / `[3/4] open forward-refs (>>> OPEN-Q: without resolution)` / `  open: 2, resolved: 4` / `[4/4] contracts/shapes <-> server/src/main/resources/schemas/shapes parity` / `  shape trees byte-identical` / `PASS`. Ship-3 added two new shape files (`household_observation.v1.schema.json` rename + `household_observation.v2.schema.json`); both mutated lock-step in both trees inside commit `0c1949e`. |
| 6 | Inheritance not regressed; no envelope `type` vocabulary edit (F-A1); no `subject_split.schema.json` arity narrowing | ✅ | 46/46 includes all Ship-1 / 1b / 2 tests. `git diff ship-2..HEAD -- contracts/envelope.schema.json server/src/main/resources/envelope.schema.json` → 0 lines (no envelope edit). `git diff ship-2..HEAD -- contracts/shapes/subject_split.schema.json` → 0 lines (no arity edit). |
| 7 | Shape registry storage discipline declared and proved | ✅ | Pattern (a) JAR-bundled fixture chosen per spec §6.1 sub-decision 3. Proof: [`ShapePayloadValidatorTest`](../../server/src/test/java/dev/datarun/ship1/event/ShapePayloadValidatorTest.java) reproducibly loads identical schemas across restarts (classpath registry-load on each fresh validator instantiation). Drift-gate check 4 (FP-007) verifies byte-identity across both trees. Ledger row `shape_registry` (CONFIG-storage) added per §7. R4 closed; persistence-outside-the-JAR re-deferred to FP-012 alongside FP-011 directory split. |
| 8 | No new ADR drafted unless R1–R5 / §3.2 triggered one | ✅ | No ADR drafted. R1–R5 assessment §3 below; §3.2 §4 below. None observed at risk of supersession. |
| 9 | Ledger rows for `shape_registry`, `multi_version_projection`, `field_count_budget`, `household_observation/v2` created with correct ADR-009 §S1 classification | ✅ | Four new STABLE rows in [`docs/convergence/concept-ledger.md`](../convergence/concept-ledger.md): `household_observation/v2` (CONFIG, ADR-009 §S1 + ADR-004 §S10), `shape_registry` (CONFIG-storage, ADR-004 §S1 + §S10), `multi_version_projection` (DERIVED, ADR-001 §S2 + ADR-004 §S10), `field_count_budget` (PRIMITIVE, ADR-004 §S13). Ship-3 close-out summary appended; counts `STABLE 254 → 258`, total `272 → 276`. Commit `767c30f`. |
| 10 | Charter regenerated; drift gate PASS | ✅ | [`docs/charter.md`](../charter.md) §Status regenerated from ledger (T5 commit). `bash scripts/check-convergence.sh` exit 0; checks 1–4 all PASS. |
| 11 | Composite S20 coverage statement: (a) S20 bullet 1 stressed under shape evolution; (b) S20 bullet 5 additionally stressed via mixed-version `/admin/events`; (c) S05 / S21 unchanged | ✅ | §9 below. |
| 12 | Retro note filed | ✅ | This file. |
| 13 | `ship-3` tag applied; `ship-2` / `ship-1b` / `ship-1` unmoved | ✅ | T7 close action; verification in close report. |
| 14 | FP-009 closure asserted (v1-current direction) — `ConflictDetector` unchanged across Ship-3; W-6 fires `identity_conflict` on a v1-current capture whose prior duplicate was recorded under v2 | ✅ | `git diff ship-2..HEAD -- server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java` returns **zero lines** (detector untouched). [`Ship3WalkthroughAcceptanceTest#walkthrough_W6_additive_happy_path_with_FP009_closure`](../../server/src/test/java/dev/datarun/ship1/acceptance/Ship3WalkthroughAcceptanceTest.java) drives the v1-current direction and observes the flag. Identity-key field names (`village_ref`, `household_name`) preserved verbatim in `household_observation/v2` per spec §6.1 sub-decision 2. **Asymmetry note**: the v2-current direction is *not* asserted by W-6 — the detector's entry guard at [`ConflictDetector.java`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) line 35 (`HOUSEHOLD_SHAPE = "household_observation/v1"`) pins activation to v1-current captures. The version-pinning expedient folds into [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) gate (b)/(c) when shape-declared uniqueness lands; it is *not* a new FP. FP-009 RESOLVED 2026-04-27. |

---

## 3. §3.1 risk assessment — R1–R5

| Risk | Observed? | Why / evidence |
|---|---|---|
| **R1** — Multi-version projection routing on `shape_ref` | **Not observed** | `/admin/events` over a mixed v1/v2 stream renders per-row with version-specific fidelity (W-7, W-10). No conflation, no fidelity loss across two reads. Branching on `shape_ref` everywhere ([`SyncController`](../../server/src/main/java/dev/datarun/ship1/sync/SyncController.java) prefix; admin template per-row); F-A2 clean. **No supersession.** |
| **R2** — All-versions-valid-forever under absent v1 schema | **Not observed** | v1 schema retained alongside v2 in the registry; W-10 reads pre-Ship-3 v1 events post-v2-deploy and they parse. [`ShapePayloadValidator`](../../server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java) loads both filename conventions (`<name>.schema.json` for /v1, `<name>.v<N>.schema.json` for /vN); no version is ever dropped. **No supersession.** |
| **R3** — 60-field budget enforcement first exercise | **Closed acceptance criterion** | Spec §7 row 4 promoted R3 from "structural risk" to "must show enforcement." Landed via [`FieldCountBudgetTest`](../../server/src/test/java/dev/datarun/ship1/event/FieldCountBudgetTest.java) at registry-load (commit `0107da9`). Counting convention = top-level `properties` count (recursion not flattened); documented in javadoc + class comment so future Ships do not re-litigate. **Not at risk of supersession**; the §S13 commitment names the limit, not the convention, so the convention choice is implementation-grade (§5 below, Frame 2 applied). |
| **R4** — Shape registry storage discipline | **Closed at retro — pattern (a) JAR-bundled fixture** | Chosen at spec §6.1 sub-decision 3; rebuild proof = [`ShapePayloadValidatorTest`](../../server/src/test/java/dev/datarun/ship1/event/ShapePayloadValidatorTest.java) reproducibly loads identical schemas across restarts. Drift-gate check 4 enforces byte-identity across both shape trees. Persistence-outside-the-JAR re-deferred to FP-012 / FP-011 (Ship that closes the deployer-authoring surface). Ledger row `shape_registry` (CONFIG-storage) created at §7 below. **No new FP** — the open question collapses into FP-011 + FP-012 already-tracked. |
| **R5** — Backward-compat under additive evolution | **Not observed** | W-6 step 1 (v1+v1 baseline) preserves Ship-1's behavior; v1 payload validates against v1 after v2 registration (W-10 implicit). v2's `additionalProperties: false` enforces the version boundary at the schema level (a v2 payload is *rejected* by v1) — version-isolated validation by construction. **No supersession.** |

No ADR §S moved from STABLE to DISPUTED. No ADR drafted, no ADR superseded.

---

## 4. §3.2 domain-realism observations

Spec §3.2 named two carry-into-build observations:

- **DR-1** — operating risk surface = §3.1's R-rows. Honored: R1/R2/R5 not observed, R3 promoted to closed acceptance criterion + landed, R4 closed at retro by pattern (a) selection. Operator-UX failures on mixed-version timeline rendering: not observed in the slice (W-7 + W-10 render coherently; deprecated `visit_notes` and v2-only `head_of_household_phone` surface in their respective rows without conflation).
- **DR-2** — FP-012 trigger evidence. Confirmed during build: walkthrough infeasibility for runtime authoring forced §S13 budget assertion into a unit test rather than HTTP (sub-decision 5). The JAR-bundled fixture is operational but its limits are now concrete: no path exists by which a non-engineering deployer can author a shape, so FP-012's gate (b)/(c) (admin endpoint + Q7a uniqueness declarations) accumulates trigger evidence.

**Ship-3-discovered subtlety — v2-current asymmetry in `ConflictDetector`.** Surfaced by spec-conformance review at retro authorship time. The detector's entry guard (`HOUSEHOLD_SHAPE = "household_observation/v1"`, [`ConflictDetector.java`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) line 35) pins activation to v1-current captures: a v1-current capture whose prior duplicate was recorded under v2 is correctly flagged (W-6); a v2-current capture whose prior duplicate was recorded under v1 (or v2) does not currently re-enter the detector. **This is *not* a new FP** — it is a deeper FP-009-shaped surface that folds into [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) gate (b)/(c) (shape-declared uniqueness moves the detector from version-pinned to shape-driven). Recorded here so a future agent reading W-6 understands the W-6 wording history (spec §6.4 + §7.14 were tightened in close T1 to name the asserted direction). No ADR §S re-decided; no FP register row added.

No new FPs opened from §3.2.

---

## 5. Implementation-grade choices made during build (Frame-2)

Choices the spec deliberately deferred to retro. Each is **explicitly not architectural** — Ship-4 may revisit any of them. Frame 2 applied to each: the architecture commits a property; the implementation chose a convention.

### 5.1 Multi-file split for shape versions (`household_observation.v1.schema.json` + `household_observation.v2.schema.json`)

File-layout choice. [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) commits *"authored as deltas, stored as full snapshots"* — storage-as-snapshots, not file layout. The decision to put each version in its own file (vs. one file holding both) is implementation. [`ShapePayloadValidator`](../../server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java)'s registry-load recognises both conventions (Ship-1 plain `<name>.schema.json` for /v1, Ship-3 versioned `<name>.v<N>.schema.json` for /vN); Ship-4 may consolidate or further split without §S10 supersession.

### 5.2 §S13 field-count = top-level `properties` count (recursion not flattened)

Counting convention. [§S13](../adrs/adr-004-configuration-boundary.md#s13-complexity-budgets) commits the limit ("60 fields per shape"); it does *not* commit the counting convention. The chosen convention is documented in [`ShapePayloadValidator.enforceFieldCountBudget`](../../server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java) javadoc and [`FieldCountBudgetTest`](../../server/src/test/java/dev/datarun/ship1/event/FieldCountBudgetTest.java) class comment so future Ships have a settled cite; if a future shape design pressure-tests the convention (e.g., a deeply nested object that's effectively 80 leaf fields in 5 top-level `properties`), the convention can be revised without §S13 supersession. Implementation, documented.

### 5.3 `shape_unknown` token wording in 400 response

Error-message marker. ADR-004 §S1 commits self-describing-by-`shape_ref` and Ship-1 chose strict-unknown rejection; the *string* used to surface unknown-shape rejections (`shape_unknown:` prefix in [`SyncController`](../../server/src/main/java/dev/datarun/ship1/sync/SyncController.java)'s `validation_failed` pipeline) is an error-message convention. No contract surface depends on the exact token. Implementation.

### 5.4 Pull-side scope filter prefix change in `SyncController.inScopeForPull`

Branching still on `shape_ref` (F-A2 honoured), minimal extension required to make v2 events flow through the same scope filter as v1. The change is from exact `/v1` match to prefix-startsWith on `household_observation/` and `conflict_detected/`. Spec-conformance review verified the change minimal and F-A2-clean. Implementation.

### 5.5 W-6 asymmetric direction

Test design, not architecture. The symmetric (v2-current) version of mixed-version `identity_conflict` cannot fire without modifying [`ConflictDetector`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java)'s entry guard, which would have invalidated FP-009's "detector unchanged across Ship-3" closure constraint. W-6 deliberately asserts only the v1-current/v2-prior direction; spec §6.4 + §7.14 were tightened (T1 commit) to name this. The symmetric direction is FP-012 gate (b)/(c) territory. Implementation.

---

## 6. FPs touched

| FP | Status at close | Action |
|---|---|---|
| **FP-001** — `role_stale` / scope-from-cache | OPEN | No Ship-3 interaction (S06b is shape evolution, not authority). Next re-evaluation at the first role-action enforcement Ship (Ship-5). |
| **FP-002** — `subject_lifecycle` read-discipline | RESOLVED at Ship-2 close | Pattern inherited (no projection cache for shape registry either). |
| **FP-003** — envelope schema parity | RESOLVED at Phase 3e | No action. |
| **FP-004** — `assignment_ref` envelope field | OPEN | No Ship-3 interaction. Carries forward. |
| **FP-005** — corrections surface | OPEN | No Ship-3 interaction. Likely candidate for Ship-4 (case management referencing existing UUIDs). |
| **FP-006** — S7↔S8 attribution under corrective split | OPEN | No Ship-3 interaction (R-4 sweep verdict at spec lock honoured: pure schema versioning introduces no UUID-referenced device flows). Carries forward to Ship-4 / Ship-5 unchanged. |
| **FP-007** — contract↔server-resource shape drift | RESOLVED at Ship-2 close | Drift-gate check 4 stayed PASS across Ship-3's shape edits (commit `0c1949e` mutated both trees in lock-step inside one commit; gate enforced byte-identity at the next run). No regression. |
| **FP-008** — `conflict_detected` lacks `root_cause` trace | OPEN per path (c) | Ship-3 emitted no `conflict_detected/v1` events whose `source_event_id` differs from the trigger event (Ship-1 categories `scope_violation` / `identity_conflict` continue to be self-triggering). Early flag corpus stays trace-clean. Carries to Ship-4. |
| **FP-009** — `ConflictDetector` field-name coupling | **RESOLVED 2026-04-27** | T2 commit `fa03d20`. Gate met by `git diff ship-2..ship-3 -- ConflictDetector.java` empty + W-6 passing. Asymmetry note recorded; v2-current direction folds into FP-012 — no new FP. |
| **FP-010** — cross-version projection composition contract | OPEN | Carries forward. Ship-3's additive-only slice did not exercise breaking changes or cross-version aggregation; per-event routing on `shape_ref` is sufficient at this scale. Trigger remains the first Ship that needs a breaking change OR multi-version aggregation in admin/projection. |
| **FP-011** — `household_observation` directory classification | OPEN (re-deferred at spec) | Ship-3 did not restructure the `contracts/shapes/` directory. Re-deferral honoured; gate remains "lands alongside FP-012." |
| **FP-012** — deployer-authoring surface | OPEN | Trigger evidence accumulated this Ship (DR-2 + the v2-current asymmetry note in §4 above). The build is still deferred; the architecture is not under question. |
| **FP-013** — config-package wire-versioning | OPEN | No real-device config delivery this Ship. Ship-3 simulated §S6 atomicity at the HTTP layer per spec §6.4. Carries forward. |

No new FPs opened. The R-4 sweep at spec lock named all relevant FPs in advance; the v2-current asymmetry surfaced at conformance review is recorded as a Ship-3-discovered subtlety in §4 — explicitly *not* a new FP, folds into FP-012.

---

## 7. Ledger deltas

Four new rows added in [`docs/convergence/concept-ledger.md`](../convergence/concept-ledger.md). Counts: **STABLE 254 → 258**, total **272 → 276**. No pre-existing row changed classification (no history-line mutations elsewhere). Ship-3 close-out summary appended to the round-2 epilogue alongside Ship-2's. Commit `767c30f`.

| Concept | Classification | Settled-by | Status | Notes |
|---|---|---|---|---|
| `household_observation/v2` | CONFIG | ADR-009 §S1, ADR-004 §S10 | STABLE | Deployer-instance shape per ADR-009 §S1 duality; v2 is the next member of the same instance lineage `household_observation` named at Ship-1, not a platform primitive. Additive evolution per §S10 — adds `head_of_household_phone`, deprecates `visit_notes`. Identity-key fields (`village_ref`, `household_name`) preserved verbatim per spec §6.1 sub-decision 2 — closes FP-009. v1 schema retained forever per §S10. |
| `shape_registry` | CONFIG | ADR-004 §S1, ADR-004 §S10 | STABLE | Pattern (a) per spec §6.1 sub-decision 3: JAR-bundled fixture continued one Ship as named expedient; classpath-loaded at boot by `ShapePayloadValidator`; no projection cache. Mirrored at `contracts/shapes/` under FP-007 drift gate. Persistence-outside-the-JAR re-deferred to FP-012 / FP-011. Distinct from the abstract `shape-registry` PRIMITIVE row (Ship-2 `alias-projection` vs `merge-alias-projection` precedent). |
| `multi_version_projection` | DERIVED | ADR-001 §S2, ADR-004 §S10 | STABLE | Per-request projection routing on `shape_ref`; no cache (FP-002 (a) pattern continued). Branching on `shape_ref` everywhere (validation, pull-side scope filter, admin timeline); F-A2 honoured. Distinct from the abstract `multi-version-projection` ALGORITHM row. |
| `field_count_budget` | PRIMITIVE | ADR-004 §S13 | STABLE | Deploy-time validation rule enforced at registry-load by `ShapePayloadValidator.enforceFieldCountBudget`. PRIMITIVE-adjacent: not a runtime concept and not a payload concept — a build-time invariant projected into a server primitive. Counting convention (top-level `properties` only) documented; revisable without §S13 supersession. Distinct from the abstract `field-budget` INVARIANT row. |

History entries: each new row carries a `Ship-3 (2026-04-27): CREATED` line per ledger rule 1.

---

## 8. ADR risk assessment — under-load behaviour

Each ADR §S exercised in spec §2 + each §3.1 R-row evaluated for whether under-load behaviour matched the §S commitment:

| ADR §S | Commitment | Observed under load | Verdict |
|---|---|---|---|
| ADR-004 §S1 | `shape_ref = {name}/v{N}`; registry append-only on `(name, version)`; all versions in registry forever; events self-describing across versions | Both v1 and v2 schemas loaded into the registry simultaneously and neither dropped; `shape_ref` carries discriminating weight (v1 ≠ v2) for the first time | **Matched.** No supersession. |
| ADR-004 §S10 | Versioned via `shape_ref`; authored as deltas, stored as snapshots; additive / deprecation / breaking; default = deprecation-only; all versions valid forever; projection routes on `shape_ref` | Additive + deprecation v2 lands; v1 events still validate post-v2; admin projection routes per row on `shape_ref` | **Matched.** No supersession. |
| ADR-004 §S13 | Hard limits enforced at deploy-time validation; 60 fields per shape | `FieldCountBudgetTest` rejects a 61-field shape at registry-load; counting convention (top-level `properties`) documented | **Matched.** Convention choice is implementation-grade per §5.2 (Frame 2). |
| ADR-001 §S2 | State is a projection of events; B→C escape hatch only if rebuildable + non-authoritative | Multi-version projection on demand; no shape-projection cache; reads replay from event store | **Matched.** No supersession. |
| ADR-007 §S1 | Envelope `type` vocabulary closed at six values | `type=capture` unchanged across v1/v2 captures; no envelope vocabulary edit | **Matched.** F-A1 honoured; F-A2 clean (no integrity logic branches on `type` for shape-version concerns). |
| ADR-008 §S1 | `shape_ref = {name}/v{N}` on the envelope | Mechanism unchanged; v2 exercised the `vN` half | **Matched.** |
| R1 | Multi-version projection routing | Not observed | No supersession. |
| R2 | All-versions-valid-forever | Not observed | No supersession. |
| R3 | §S13 budget enforcement | Closed acceptance criterion landed | No supersession; convention documented. |
| R4 | Shape registry storage discipline | Closed at retro (pattern a) | No supersession; ledger row created. |
| R5 | Additive-evolution backward-compat | Not observed | No supersession; v2's `additionalProperties: false` enforces the version boundary. |

**Ship-3 closes with no ADR R-revision.** No ADR drafted, no ADR superseded, no §S moved from STABLE to DISPUTED.

---

## 9. Composite S20 coverage statement

- **S20 bullet 1 (encounter individuals, document relevant details)** — explicitly stressed under shape evolution. The shape being evolved IS the CHV capture shape; W-6 and W-7 capture under v1 and v2 with both versions holding alongside.
- **S20 bullet 5 (history of what was done, when, and by whom)** — additionally stressed via mixed-version `/admin/events` rendering. W-7 + W-10 demonstrate per-version fidelity across the timeline (v1 row shows the "no phone field" note; v2 row surfaces the deprecated `visit_notes` and v2-only `head_of_household_phone`).
- **S05 / S21** — unchanged at 0% covered. S05 is reviewer-driven (Ship-5); S21 lands with S04.

Carry-back acknowledgement (Ship-1 / Ship-1b / Ship-2 composite coverage was not declared in those specs) was filed once in spec §1; not repeated here.

---

## 10. Cosmetic / hygiene notes

Not fix-obligations; recorded for future Ships and for `git log` readers who might mistake the absence of a scenario cite for a missing one.

- **Two `chore(...)` commits in Ship-3 range without scenario cites**: `a62b6b5` (`chore(skill): allow read-only access to implementation surfaces`) and `4a4687f` (`chore(orchestrator): add H9 composite-scenario coverage rule`). Both are build-tooling / orchestrator hygiene, not S06b work. Permitted exemption per spec §7.2 / Ship-2 precedent (`a54e60b` orchestrator chore in Ship-2 range).
- **Spec wording tightened in T1.** §6.4 W-6 and §7.14 originally read symmetrically ("ConflictDetector continues to flag duplicate-household across mixed v1/v2 events") but the implementation is asymmetric (only v1-current direction). Tightened in close commit `b4111cb` to name the asserted direction and the asymmetry. Recorded so future Ships understand the W-6 wording history; the asymmetry itself folds into FP-012 (see §4 + §6).
- **`Ship3WalkthroughAcceptanceTest` under `dev.datarun.ship1.acceptance` package.** Slice-name (`ship-3`) vs package-name (`ship1`) divergence inherited from Ship-2 (`Ship2WalkthroughAcceptanceTest` was already under `dev.datarun.ship1.acceptance`); locked at Ship-1 by structural choice. Ship-N classes share Ship-1's package by convention; the package name is stable, the class name carries the slice identity.

---

## 11. Hand-off to Ship-4

Recorded here as a starting position; Ship-4 spec authoring will revise.

- **Likely scope**: S04 (corrections — CHV-initiated) + start of S08 (case management referencing existing UUIDs by reference). The first Ship that introduces UUID-referenced device flows is the trigger for FP-006 (S7↔S8 attribution under corrective split) and may be the trigger for FP-008 (`conflict_detected` `root_cause` trace if stale-reference flags first emit).
- **Live R-N risks**: R1 / R2 / R5 not observed at single-shape additive scale; carry forward to a future Ship that exercises breaking changes or cross-version aggregation. R3 closed; R4 closed.
- **FPs becoming candidate for closure in Ship-4**: FP-005 (corrections surface — directly addressed by S04), FP-006 (UUID-referenced flow first arises), FP-008 (stale-reference flag emission). FP-005 close requires schema authoring + walkthrough; the others gate on whether the slice exercises the relevant emission/attribution path.
- **Live deferred surfaces**: deactivation-while-referenced (S06b parked surface from spec §6.5) likely lands in Ship-4 alongside case management. Mobile config delivery (FP-013 + on-device at-most-2-version coexistence) parked until the Ship that revisits Flutter; not Ship-4.
- **Architecture not under question for Ship-4**: ADR-002 (identity merge/split), ADR-007 (type vocabulary), ADR-008 (envelope reference fields), ADR-009 (mechanism vs instance) all remain STABLE. Ship-4 should not draft a new ADR unless its slice surfaces a §S at risk.
- **Discriminator discipline**: branch on `shape_ref` for shape-version concerns; on `actor_ref` for system-vs-human authorship; on `type` only for the closed six-value pipeline answer. F-A1 / F-A2 / F-A3 / F-A4 inherited.

---

## 12. Open questions

None from Ship-3 build. OQ-1 / OQ-2 / OQ-3 / OQ-4 / OQ-5 resolved at §6.1 lock 2026-04-27 (recorded in spec §9). Spec §9 was annotated against the §6.1 lock in commit `d822a6a` during the Ship-3 build phase; T6 of close added no new OQ.
