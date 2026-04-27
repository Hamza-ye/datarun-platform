# Ship-3 — Shape Evolution (S06b)

> **Status**: spec draft — mechanical sections pre-filled (§1, §2, §3.1, §4, §5, §6.5, §7). User-owned sections (§3.2, §6, §6.4) marked TODO. Hard rule H1: no code begins until this spec is reviewed and all cited ADRs are Decided.
>
> **Opened**: 2026-04-26
> **Tag target**: `ship-3`
> **Parent**: [`ship-2.md`](ship-2.md) (tag `ship-2` does not move; Ship-3 opens against the Ship-2 server).
> **Slice decision**: pure payload-shape versioning. Deactivation-while-referenced surface from S06b prose is parked in §6.5 for a later Ship.

---

## 1. Scenarios delivered

Ship-3 delivers the **schema-versioning half of [S06b](../scenarios/06-entity-registry-lifecycle.md)**: when the shape of expected information changes, work already in progress under the old shape stays valid, new work follows the updated shape, both versions remain readable, and the platform tracks which version each record was created under. Continues under the [S19](../scenarios/19-offline-capture-and-sync.md) offline constraint.

| Scenario | What Ship-3 delivers |
|---|---|
| [S06b](../scenarios/06-entity-registry-lifecycle.md#06b-when-the-shape-of-information-changes) (versioning half) | The **server-side** shape registry holds ≥ 2 versions of one shape simultaneously, append-only on `(name, version)` ([ADR-004 §S1](../adrs/adr-004-configuration-boundary.md#s1-shape-reference-in-envelope): *"All shape versions remain in the registry forever"*; [§S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution): *"All versions remain valid forever"*). v1 events and v2 events coexist in the event store. Validation routes on `shape_ref`. Projection (admin event timeline) handles a mixed-version stream without losing fidelity of either version. |
| [S19](../scenarios/19-offline-capture-and-sync.md) | Two simulated devices on different `shape_ref` values exercise the *server's* acceptance of mixed-version captures (the "work arrives centrally after the rules have already changed" invariant from S06b prose). The on-device side of this — atomic configuration delivery and at-most-2-version coexistence per [ADR-004 §S6](../adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) — is **modelled by simulation, not tested on a real device**. See §6.5. |

### Delivery surface (H8)

Scripted HTTP simulation against the real server (Ship-1 / Ship-2 precedent). Multi-version capture is simulated by two devices on different `shape_ref` values, not by a real Flutter client running an older config bundle (config delivery to a real device is a Ship-1b-shaped concern that lands at the Ship that revisits the mobile surface). Acceptance gate: a `Ship3WalkthroughAcceptanceTest` sibling of the prior Ship suites driving Ship-3 walkthroughs against `TestRestTemplate`.

### Composite-scenario coverage (S05 / S20 / S21)

| Composite | Bullets exercised by Ship-3 | Bullets carried forward |
|---|---|---|
| [S20 — CHV field operations](../scenarios/20-chv-field-operations.md) | **Bullet 1 (encounter individuals, document relevant details)** is directly stressed: the shape being evolved IS the CHV capture shape. Where Ship-1 silently exercised this bullet on a static schema, Ship-3 puts it under the version-evolution constraint. **Bullet 5 (history of what was done, when, and by whom)** continues to be partly delivered through `/admin/events` and is *additionally* stressed because the admin timeline must now render two payload shapes coherently — declared. | Bullets 2 (outcomes / treatment), 3 (supplies), 4 (continuous activities) — none touched. They need new shapes/activities owned by later Ships. |
| [S05 — supervision / audit visits](../scenarios/05-supervision-audit-visits.md) | **None.** S05 is reviewer-driven; no review pipeline lands until Ship-5. | All S05 surface remains 0% covered. |
| [S21 — CHV supervisor operations](../scenarios/21-chv-supervisor-operations.md) | **None.** Supervisor authority surface lands with S04 in Ship-5. | All S21 surface remains 0% covered. |

### Carry-back acknowledgement (no backfill)

Composite-scenario coverage was not declared in Ship-1 or Ship-2 specs. From Ship-3 forward, every spec §1 names composite coverage explicitly. Past Ships are not amended (tags do not move). Recorded once, here:

- Ship-1 W-0 exercised S20 bullet 1 (encounter, document details) without declaration.
- Ship-1's `/admin/events` partly delivered S20 bullet 5 ("history of what was done, when, and by whom") without declaration.
- Ship-2's identity reconciliation is an S20 *operational prerequisite*, not a direct S20 bullet.
- S05 and S21 remain 0% covered; S21 lands with S04 at Ship-5.

### Scenarios deliberately not delivered

- **S06b deactivation-while-referenced surface** — parked in §6.5 for a later Ship (likely Ship-4 case management, where long-running references against subjects in the registry first matter operationally).
- **Bulk re-classification / mass schema changes** (S06b prose third paragraph) — parked in §6.5; out of scope until a Ship explicitly owns it.
- **Breaking changes (field removed or type changed; ADR-004 §S10 "exceptional path")** — parked. Slice exercises additive evolution + deprecation only.
- S08 / S04 / S11 / S07 / S09 / S02 / S10 / S12 — owned by later Ships per [`docs/ships/README.md`](README.md).

---

## 2. ADRs exercised

Ship-1 + Ship-1b + Ship-2 exercised ADR-001, ADR-002 §S1/§S5/§S6–§S14, ADR-003, ADR-004 §S1, ADR-006, ADR-007, ADR-008, ADR-009 §S1/§S2. Ship-3 is the first exercise of ADR-004 §S10 (shape evolution) under live multi-version load.

| ADR §S | What it commits | How Ship-3 exercises it |
|---|---|---|
| [ADR-004 §S1](../adrs/adr-004-configuration-boundary.md#s1-shape-reference-in-envelope) | `shape_ref = {name}/v{N}`. Shape registry is **server-side** and **append-only on (name, version)**: *"All shape versions remain in the registry forever; events are self-describing across versions."* | Ship-1 exercised the `{name}/v{N}` *format* with v1 alone. Ship-3 is the **first time the registry holds more than one version**, putting the append-only-forever half of §S1 under live load. R2 (§3.1) names the failure mode. |
| [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) | Shapes versioned via `shape_ref`. **Authored as deltas, stored as full snapshots.** Three change classes: additive / deprecation / breaking. Default = deprecation-only. **All versions remain valid forever.** Projection routes on `shape_ref`. | First exercise. Ship-3 introduces v2 of one Ship-1 shape (the CHV capture shape). v1 events recorded under Ship-1/2 stay valid; v2 events validate against the v2 schema; the admin projection routes correctly. Whether Ship-3 includes a deprecation is a §6 sub-decision. Breaking changes deferred. Delta-authoring tool not built (§6.5); v2 ships as a snapshot file. |
| [ADR-004 §S13](../adrs/adr-004-configuration-boundary.md#s13-complexity-budgets) | Hard limits enforced at deploy-time validation. **60 fields per shape.** | First exercise of the validation surface (even if Ship-3's v2 is far below 60 fields). Spec commitment: shape-version registration validates the field-count budget; a shape that would exceed 60 fields is rejected at registration time, not silently accepted. |
| [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | State is a projection of events. Escape hatch B→C (cacheable projection) only if rebuildable and never authoritative. | Multi-version stream projected on demand. Ship-3 inherits the Ship-2 / FP-002 (a) pattern: no shape-projection cache. The `/admin/events` rendering replays from events per request, branching on `shape_ref`. |
| [ADR-007 §S1](../adrs/adr-007-envelope-type-closure.md#s1-the-envelope-type-vocabulary-is-closed-at-six-values) | Envelope `type` vocabulary closed at six values. | **Unchanged by Ship-3.** v2 of a `capture` shape stays `type=capture`. Shape evolution does not extend the type vocabulary. Recorded to defend F-A1 against a tempting-but-wrong "let's add a `schema_changed` type" mistake. |
| [ADR-008 §S1](../adrs/adr-008-envelope-reference-fields.md#s1-subject-ref-envelope-field) | `shape_ref` is `{name}/v{N}` on the envelope. | **Unchanged by Ship-3.** The mechanism was already there from Ship-1; Ship-3 is the first time the `vN` half of the contract carries discriminating weight (`v1 ≠ v2`). |

### Inherited invariants (must not regress)

These are not "exercised" by Ship-3 in the supersession sense — they are inherited from prior Ships and the Ship-3 slice must not disturb them:

| Inherited from | Invariant | How Ship-3 stays clean |
|---|---|---|
| Ship-2 / FP-002 (a) | No `subject_lifecycle` cache; projections replay from events on demand. | Shape projection follows the same pattern. No `shape_registry_cache` table; if a shape registry table exists, it's the configuration store, not a projection cache. |
| Ship-2 / contract | `subject_split.schema.json` `successor_ids: array, minItems: 2`. | Ship-3 does not edit `subject_split.schema.json`. Shape evolution operates on a different shape (CHV capture), not on identity shapes. |
| Ship-2 / FP-007 | Drift-gate check 4 (`contracts/shapes` ↔ `server/src/main/resources/schemas/shapes` byte-identical). | Any shape Ship-3 edits is mutated in lock-step in both trees inside the same commit. The gate fails the build if not. |
| Ship-1 / Ship-2 | Server-emitted events use shared `ServerEmission.SERVER_DEVICE_ID = 00000000-…-0001` and `server_device_seq`. | Ship-3 does not introduce a new server emission site (no shape-versioning event is emitted by the server in this slice). If §6 chooses to record shape-version registration as an envelope event, ServerEmission is the only path. |
| Ship-2 | Coordinator authority via projection from `assignment_created/v1` `role="coordinator"`; no parallel surface. | Ship-3 does not add new authority dimensions. Whether shape-registration is a coordinator-only operation is a §6 sub-decision. |

### Out of scope (explicitly not exercised)

- ADR-002 §S6–§S14 — identity not touched.
- ADR-003 — authorization not touched (no new scope dimensions).
- ADR-005 — state progression / review not touched.
- ADR-004 §S11 (expressions), §S12 (triggers), §S14 (deployer-parameterized policies beyond shape) — Ship-7 reactive layer.
- [ADR-004 §S6](../adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) — atomic configuration delivery + on-device at-most-2-version coexistence + "device applies the new configuration only after in-progress work under the previous configuration completes." **Modelled by HTTP simulation, not tested on a real device.** Carries forward to the mobile sub-Ship that revisits the Flutter client for shape-version awareness (see §6.5). The simulation does not violate §S6 (each simulated device pins to one `shape_ref` value at a time), but it does not prove the real-device sequencing either.
- ADR-009 §S3 (pattern registry) — Ship-4 / Ship-7.
- Mobile delivery surface — config delivery to a real device defers to the Ship that revisits Flutter (Ship-1b-shaped sub-Ship for shape-version awareness, owed but not now).

---

## 3. ADRs at risk of supersession

### 3.1 Structural risks

| Risk | ADR position at risk | Observable that would force supersession |
|---|---|---|
| **R1 — Multi-version projection routing on `shape_ref`** | [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution), [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | A read against `/admin/events` over a mixed-version stream produces a payload representation that conflates v1 and v2 fields, or that loses fidelity of one version, or that produces inconsistent rendering across two reads of the same event set. Forces an addendum specifying multi-version projection contract. |
| **R2 — All-versions-valid-forever under absent v1 schema** | [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) | A v1 event becomes unparseable after v2 lands because the runtime drops the v1 schema. §S10 commits "all versions remain valid forever"; if the implementation can only validate the latest version, §S10 is broken. Forces an explicit "shape registry never deletes a registered version" commitment, possibly an addendum. |
| **R3 — 60-field budget enforcement first exercise** | [ADR-004 §S13](../adrs/adr-004-configuration-boundary.md#s13-complexity-budgets) | The budget is treated as advisory rather than enforced (a shape registers with > 60 fields without rejection). §S13 explicitly says "Hard limits enforced at deploy-time validation, not advisory guidelines." Forces either an enforcement landing or an §S13 addendum admitting the gap. |
| **R4 — Shape registry storage discipline** | [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | The shape registry is read as authoritative state without a rebuild proof — i.e., the mistake FP-002 identified for `subject_lifecycle`, repeated on shape-versioning. Either Ship-3 picks the events-on-demand pattern (inherited from FP-002 (a)) or it justifies a registry table as configuration storage (not a projection cache, distinct rationale). The choice and proof is a Ship-3 retro deliverable. **Likely candidate for a new FP if not closed at retro.** |
| **R5 — Backward-compat under additive evolution** | [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) | A v1 payload that was valid under the v1 schema fails validation after v2 registration (because something in the v2 path leaks back). Forces a "version-isolated validation" commitment. |

### 3.2 Domain-realism risks

**Locked 2026-04-27.** Two observations carry into the build:

- **DR-1 — operating risk surface = §3.1's R-rows.** R1 (mixed-version projection routing), R2 (v1-still-validates), R3 (§S13 budget enforcement first exercise), R4 (registry storage discipline), R5 (additive-evolution backward-compat) are the pre-enumerated risks. No further domain-realism risks are speculated pre-build (per the Ship-2 precedent: novel surfaces are higher-signal observed than guessed). Operator-UX failures (mixed-version timeline rendering; "this record was created under v1" downstream-reader confusion) record at retro §4.
- **DR-2 — FP-012 trigger evidence.** Walkthroughs cannot exercise a runtime authoring path because no such path exists in Ship-3 (§6 sub-decision 4 — coordinator-runtime authoring deliberately omitted). The §S13 60-field budget assertion lands as a unit test rather than an HTTP walkthrough (sub-decision 5). **That walkthrough infeasibility is the trigger evidence** for FP-012 (deployer-authoring surface): the JAR-bundled fixture from Ship-1, extended one Ship here, is a named expedient and its limits are now visible as test-shape distortion rather than abstract architectural commentary.

---

## 4. Ledger concepts touched

| Concept | Current | Ship-3 impact |
|---|---|---|
| `shape` [PRIMITIVE] | STABLE | First exercise under multi-version evolution. Status unchanged unless ADR-004 §S10 supersession triggers. |
| `shape_ref` [CONTRACT envelope field] | STABLE | First exercise where the `vN` segment carries discriminating weight (v1 ≠ v2). Status unchanged. |
| `shape_registry` [CLASSIFICATION-TBD] | not previously asserted as a ledger row | Ship-3 chooses storage discipline (events-on-demand inherited from FP-002 (a), or a registered-config table with rebuild proof). Ledger row asserted and classified at retro. |
| `shape_version_registration` [TBD — event vs. config-side] | not previously asserted | Whether registering a new shape version is a configuration-layer write (no envelope event) or a config-event with `type=…` (new envelope vocabulary forbidden by F-A1) is a §6 sub-decision. **The shape vocabulary closure forces the answer toward configuration-layer writes** — but the row classification is a retro deliverable. |
| `multi_version_projection` [DERIVED] | not previously materialised | First materialisation. Pattern matches `SubjectAliasProjector` precedent: per-request replay branching on `shape_ref`. |
| `field_count_budget` [PLATFORM-FIXED constraint] | not previously asserted as a ledger row | First enforcement landing. Row classification: PRIMITIVE-adjacent / CONFIG-validation. Asserted at retro. |
| `household_observation/v2` [PLATFORM-BUNDLED candidate] **OR** [CONFIG candidate per ADR-009] | not previously asserted | **Classification depends on §6 sub-decision**: does the v2 shape ship in `contracts/shapes/` (platform-bundled, like `subjects_merged/v1`) or as deployer-authored configuration (ADR-009 §S1 mechanism vs. instance — `household_observation` is a deployer-instance shape, the deployer chose the name; v2 evolution is also deployer territory). Likely the latter; the spec slice should pick the right answer here, since misclassifying it as platform-bundled would mistakenly elevate a deployer concept. |

Expected classification outcomes: shape registry row created (DERIVED if events-on-demand, CONFIG-storage if registered table); multi-version projection row created (DERIVED); field-count budget row created; `household_observation/v2` classified per ADR-009 §S1 duality.

---

## 5. Flagged positions — consult ([Rule R-4](../flagged-positions.md))

R-4 sweep ran 2026-04-26 against the pure-versioning slice.

| FP | Status | Ship-3 interaction |
|---|---|---|
| [FP-001](../flagged-positions.md#fp-001--role_stale-projection-derived-role-verification) — `role_stale` temporal-divergence test | OPEN | **Does not block.** Ship-3 is shape evolution, not authority. Lands at Ship-5. |
| [FP-002](../flagged-positions.md#fp-002--subject_lifecycle-table-read-discipline-audit) | RESOLVED at Ship-2 close | No action; pattern inherited (no projection cache). |
| [FP-003](../flagged-positions.md) — envelope schema parity | RESOLVED | No action. |
| [FP-004](../flagged-positions.md#fp-004--assignment_ref-as-potential-future-envelope-field) — `assignment_ref` envelope field | OPEN | **Does not block.** Shape evolution does not introduce assignment-targeting events. |
| [FP-005](../flagged-positions.md#fp-005--corrections-surface-is-unassigned-in-the-5-ship-map) — corrections surface | OPEN | **Does not block.** Ship-3 does not deliver S04. Shape evolution and capture corrections are distinct concerns. |
| [FP-006](../flagged-positions.md#fp-006--s7s8-attribution-semantics-in-the-corrective-split-case) — S7↔S8 attribution under corrective split | OPEN | **Does not block under the pure-versioning slice.** R-4 sweep verdict: pure schema versioning introduces no UUID-referenced device flows beyond the Ship-1 fresh-`subject_id`-per-capture pattern. The structural prerequisite for FP-006 (a CHV device captures against an existing-subject UUID, then that UUID is archived by a corrective split, then the offline capture syncs) does not arise. Carries forward unchanged. |
| [FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced) — shape parity | RESOLVED at Ship-2 close | Drift-gate check 4 stays in place. Ship-3 must lock-step any shape edit; the gate enforces it. |
| [FP-008](../flagged-positions.md#fp-008--conflict_detected-payload-lacks-root_cause-trace-metadata) — `conflict_detected` lacks `root_cause` | OPEN | **Does not block under the pure-versioning slice.** R-4 sweep verdict: pure schema versioning emits no new flag categories. Existing `scope_violation` and `identity_conflict` continue to be self-trigger (`source_event_id == trigger`). Trace metadata only becomes load-bearing at the first Ship that emits a flag whose source-of-badness differs from the source event — Ship-4 (case management referencing existing UUIDs) at the latest. Carries forward unchanged. |

**Inherited open RFS items**:

- **RFS-1** (naïve identity heuristic): unchanged. Ship-3 does not touch identity.
- **RFS-2** (village-on-payload): may interact if the `household_observation` v2 spec changes the village field's representation. §6 sub-decision; if v2 touches village, declare; otherwise unchanged.
- **RFS-3** (schema duplication): the FP-007 drift-gate now defends against this for shapes; envelope still single-file via FP-003 parity test.

**New FPs anticipated**:

- Likely one on shape registry storage discipline (R4) if the choice and proof are not airtight at retro — the FP-002-shaped question, one layer over.
- Possibly one on operator UX for mixed-version timelines if §3.2 surfaces it during build.

---

## 6. Slice

**Locked 2026-04-27 — Cleaving A (narrow slice; no new ADR; JAR-bundled fixture continued as a named expedient).** Sub-decisions:

### 6.1 Sub-decisions

1. **Shape evolved.** `household_observation` evolves to **v2**. v1 events from Ship-1 / Ship-2 stay valid forever per [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution). Ship-3 is the first Ship where the registry holds more than one version of any shape.

2. **Change set.** v2 is **additive + deprecation** — one new optional field added, one existing optional field marked deprecated. Default change class CF-1 per [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution): *"the default evolution path is deprecation-only."* Breaking changes remain parked (§6.5).

   **Identity-key fields used by `ConflictDetector` — `village_ref` and `household_name` — MUST remain in v2 unchanged.** This is the **FP-009 closure assertion**: the detector's hard-coded field-name coupling at [`server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) lines 53–56 is permitted to remain unchanged across Ship-3 because v2 preserves these field names. Any future shape change that touches identity-key fields reopens FP-009 and folds into FP-012's gate (b)/(c) — shape-declared uniqueness (Q7a).

3. **Shape registry storage — JAR-bundled fixture continued.** v2 ships as a snapshot file in `server/src/main/resources/schemas/shapes/` (Ship-1 precedent), mirrored byte-identically in `contracts/shapes/` under [FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced)'s drift-gate. **This is a named expedient extended one Ship — it is not the architecture.** The architecture is [ADR-004 §S6](../adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) (atomic configuration delivery) + [§S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) (versioning policy) + [§S13](../adrs/adr-004-configuration-boundary.md#s13-complexity-budgets) (deploy-time validation) + [§S14](../adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies) (deployer-parameterized policies). The **build of the deployer-authoring surface** that those §S call for is **deferred via FP-012** with a hard gate naming the first triggering Ship. The directory-classification consequence (`household_observation` is deployer-CONFIG per [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) but lives alongside platform-bundled shapes) is re-deferred via FP-011, gated to land alongside FP-012.

4. **No coordinator-runtime authoring path in Ship-3.** Collapses into sub-decision 3: registration is a deploy-time event from the platform's perspective. There is no HTTP endpoint a coordinator hits to register v2; v2 ships in the next deploy. The runtime-authoring surface is FP-012 territory.

5. **§S13 60-field budget enforcement = unit test on the validator at registry-load.** Not an HTTP walkthrough. The reason is itself signal: with no runtime-authoring endpoint (sub-decision 4), there is no HTTP surface to assert against. **That infeasibility is the trigger evidence captured by §3.2 DR-2 and FP-012.** Implementation note: if the unit test would require new scaffolding beyond Ship-1's existing validator harness, surface it in retro §3.2 — the test may be parked as a Ship-internal followup rather than block §6.1 lock.

### 6.5 Out of scope (deliberately not built)

- **Deactivation-while-referenced** (S06b prose: "A thing in the registry may be deactivated or removed, but existing records, assignments, and ongoing work still reference it"). Parked. Lands at the Ship that owns S08 case management or earlier, when long-running references first matter operationally. Likely Ship-4.
- **Bulk re-classification / mass shape changes** (S06b prose third paragraph). Parked.
- **Breaking changes** (ADR-004 §S10 "exceptional path" with deployer acknowledgment + server-side migration support). Deferred until a forcing function demands it; until then, deprecation-only is sufficient.
- **Real-device shape-version awareness + atomic config delivery sequencing** (ADR-004 §S6). Ship-3 simulates multi-version capture with two HTTP devices on different `shape_ref` values; it does not deliver a config bundle to a real Flutter client and does not exercise the "finish in-progress work under previous config before applying new config" sequencing. Owed to a Ship-1b-shaped mobile sub-Ship.
- **Delta-authoring tooling** (ADR-004 §S10 first half: "Authored as deltas"). Ship-3 ships v2 as a full snapshot schema file (the §S10 storage half). The deployer-facing delta-authoring surface is configuration tooling, parked per ADR-004 close-out.
- **Shape registry as deployer-authored configuration L1 surface** (deployer YAML / visual builder). Configuration authoring tooling is implementation, parked per ADR-004 close-out.
- **Field-count budget enforcement on Ship-1 historical shapes**. Ship-3 lands the enforcement; existing Ship-1 / Ship-2 shapes are below 60 fields by inspection. No retroactive validation pass.
- **`conflict_detected/v1` `root_cause` field** (FP-008 (c)). Not produced by this slice; carries to the producing Ship.
- **Deployer-authoring surface for shapes/triggers/policies** (REST endpoint, file format, UI). Tracked: [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapesriggerspolicies).
- **Q7a shape-declared uniqueness rules** ([ADR-004 §S14](../adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies)). Tracked: FP-012 gate (b)/(c).
- **Q12 shape/activity-level sensitivity classification** ([ADR-004 §S14](../adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies)). Tracked: FP-012 gate (c).
- **Breaking-change shape evolution path** ([ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) "exceptional path"). Tracked: [FP-010](../flagged-positions.md#fp-010--cross-version-projection-composition-contract).
- **Cross-version projection composition contract** (multi-version aggregation in admin/projection layer beyond per-event routing on `shape_ref`). Tracked: FP-010.
- **Config-package wire-versioning scheme** ([ADR-004 §S6](../adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) commits atomicity but not wire format; file-15 action item never landed). Tracked: [FP-013](../flagged-positions.md#fp-013--config-package-wire-versioning-scheme).
- **`contracts/shapes/` vs platform-bundled directory split** (F-C1 — `household_observation` is deployer-CONFIG per [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) but lives alongside platform primitives like `subjects_merged/v1` / `subject_split/v1`). **Re-deferred** as [FP-011](../flagged-positions.md#fp-011--household_observation-directory-classification-re-deferral); gate = same Ship as FP-012, because resolving the split before deployer-CONFIG shapes are persisted outside the JAR is restructuring without load.

### 6.4 Walkthroughs

**Locked 2026-04-27.** Each walkthrough asserts on `shape_ref` (F-A2 / F-A4) — never on envelope `type` for shape-versioning logic. Ship-2 used W-3 / W-4 / W-5; Ship-3 picks up at W-6.

- **W-6 — Additive happy path (mandatory).** Two devices on `household_observation/v1`; server's shape registry is updated to include v2; both devices then capture under v2. Server validates and persists. **`ConflictDetector` continues to flag duplicate-household across mixed v1/v2 events** — this is the **FP-009 closure assertion**: the detector is unchanged across Ship-3 and fires because the identity-key fields (`village_ref`, `household_name`) are preserved per §6.1 sub-decision 2. Exercises [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) (additive evolution) + [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) (multi-version projection on demand).
- **W-7 — Deprecation (mandatory).** A device on v2 captures with the deprecated field present in payload; server stores the event verbatim (immutability); projection routes correctly on `shape_ref`; a device still on v1 continues to function unchanged. Exercises the at-most-2-version coexistence intent of [ADR-004 §S6](../adrs/adr-004-configuration-boundary.md#s6-atomic-configuration-delivery) / [§S12](../adrs/adr-004-configuration-boundary.md#s12-trigger-architecture-and-limits) — Ship-3 simulates §S6 sequencing via two HTTP devices on different `shape_ref` values; does not test on a real Flutter client.
- **W-8 — Unknown `shape_ref` rejection (mandatory).** A device sends an envelope with `shape_ref` whose version is not in the registry → `400` with `shape_unknown`. Inherits Ship-1's strict-unknown-`shape_ref` choice; defends against silent-version-upgrade drift.
- **W-10 — Backward-compat read (mandatory).** Pre-Ship-3 v1 events from Ship-1 fixtures remain readable after v2 deploy; `/admin/events` renders the mixed-version timeline correctly with payload fidelity intact for each version. Stresses R5 / R2.

**W-9 absorbed into a unit test, not a walkthrough.** §S13 60-field budget enforcement runs at registry-load (per §6.1 sub-decision 5). Recorded here so the absence is intentional, not forgotten — and so the absence is read as FP-012 trigger evidence (§3.2 DR-2).

---

## 7. Retro criteria

The retro must show, mechanically:

1. **Walkthroughs pass via HTTP.** `Ship3WalkthroughAcceptanceTest` extends `WalkthroughAcceptanceTest` / `Ship2WalkthroughAcceptanceTest` precedent; all Ship-1 + Ship-2 walkthroughs remain green; Ship-3 walkthroughs (W-6 + the additional ones from §6.4) pass. Suite total grows; no prior tests removed.
2. **Commits cite scenarios.** Every Ship-3 commit's subject line carries one of `S06b`, `S20`, `S19` (e.g., `feat(ship-3): S06b — register household_observation/v2 schema`). Build-tooling-hygiene commits without scenario cites are flagged in §10 (Ship-2 precedent).
3. **ADR-004 §S10 exercised.** Proof: (a) v1 events from Ship-1 fixture remain valid under the post-Ship-3 validator, (b) v2 events validate under the v2 schema, (c) projection routes on `shape_ref`. Evidence: walkthrough output + a SQL or grep proof point.
4. **ADR-004 §S13 budget enforced.** Proof: a unit test or walkthrough rejects a > 60-field shape registration.
5. **FP-007 drift-gate stays PASS.** `bash scripts/check-convergence.sh` exit 0; check 4 byte-identical after any shape edit. Any Ship-3 shape edit is mutated in lock-step in both trees.
6. **Inheritance not regressed.** Ship-1 W-0 / W-1 / W-2 / Ship-1b M1 / M2 / Ship-2 W-3 / W-4 / W-5 all green at Ship-3 close. No envelope `type` vocabulary edit (F-A1). No `subject_split.schema.json` arity narrowing.
7. **Shape registry storage discipline declared and proved.** §6 sub-decision 3 picked at spec close; retro shows the proof. If pattern (b) events-on-demand: rebuild test. If pattern (a) configuration-layer storage: classpath-load test + drift-gate proof. R4 closed at retro or recorded as a new FP.
8. **No new ADR drafted unless R1–R5 / §3.2 triggered one.** Frame 4 applied to any retro position that *feels* architectural.
9. **Ledger rows updated.** Rows for `shape_registry`, `multi_version_projection`, `field_count_budget`, `household_observation/v2` (with the correct ADR-009 §S1 classification) created. Counts incremented in the close-out summary.
10. **Charter regenerated; drift gate PASS.** [`docs/charter.md`](../charter.md) §Status regenerated from ledger; all four checks PASS.
11. **Composite S20 coverage statement.** Retro records: (a) Ship-3 stressed S20 bullet 1 explicitly under shape evolution; (b) S20 bullet 5 (history of what was done) was additionally stressed via mixed-version `/admin/events` rendering; (c) S05 / S21 unchanged.
12. **Retro note filed.** `ship-3-retro.md` covers walkthroughs, criteria, R1–R5, §3.2 observations, implementation-grade choices, FPs touched, ledger deltas, Ship-4 handoff, OQs, cosmetic notes.
13. **`ship-3` tag applied; `ship-2` / `ship-1b` / `ship-1` unmoved.**
14. **FP-009 closure asserted.** [`ConflictDetector`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) is **unchanged** across Ship-3 (no edits to the `detect(...)` payload field-name reads — `village_ref` and `household_name` remain the literal field names looked up). W-6 passes against mixed v1+v2 events with the detector firing on duplicate-household. If this row fails, FP-009 stays OPEN and reopens the shape-declared-uniqueness work folded into FP-012 gate (b)/(c).

---

## 8. Hand-off to Ship-4

Filled at retro.

---

## 9. Open questions — to resolve before build

To be filled by the user. Likely items emerging from §6 sub-decisions:

- **OQ-1**: Which shape gets v2? (Default recommendation: `household_observation`.)
  RESOLVED at §6.1 lock 2026-04-27 — see §6.1 sub-decision 1 (`household_observation`).
- **OQ-2**: Additive-only or include a deprecation? (Default recommendation: include one deprecation.)
  RESOLVED at §6.1 lock 2026-04-27 — see §6.1 sub-decision 2 (additive + one deprecation; identity-key fields `village_ref` and `household_name` preserved verbatim per FP-009 closure).
- **OQ-3**: Shape registry storage pattern — (a) configuration-layer / (b) events-on-demand. (Default recommendation: (a).)
  RESOLVED at §6.1 lock 2026-04-27 — see §6.1 sub-decision 3 (JAR-bundled fixture continued as a named expedient; events-on-demand projection per FP-002 (a) — no shape-projection cache).
- **OQ-4**: Is shape v2 registered at deploy time only, or via a coordinator-authored runtime path? (Default recommendation: deploy time only.)
  RESOLVED at §6.1 lock 2026-04-27 — see §6.1 sub-decision 4 (deploy-time only; coordinator-runtime authoring deferred to FP-012).
- **OQ-5**: Field-count budget enforcement — walkthrough or unit test? (Decide with OQ-3.)
  RESOLVED at §6.1 lock 2026-04-27 — see §6.1 sub-decision 5 (unit test — FieldCountBudgetTest at registry-load; the walkthrough infeasibility is the FP-012 trigger evidence per §3.2 DR-2).

---

## 10. Skill-gap note (orchestrator)

Composite-scenario coverage declaration in spec §1 is not yet a named rule in the orchestrator skill. Ship-1 and Ship-2 went past it without declaring; Ship-3 declares (this spec, §1 "Composite-scenario coverage" subsection) and carry-back-acknowledges. If Ship-4 spec authoring forgets the declaration without an explicit handoff reminder, the skill is amended at Ship-4 retro per the "two-instance rule" (one instance is an episode; two is a rule).
