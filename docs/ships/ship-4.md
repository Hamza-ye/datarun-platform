# Ship-4 — Case Management (S08)

> **Status**: spec DRAFT — mechanical sections pre-filled (§1, §2, §3.1, §4, §5, §6.5, §7, §9). User-owned sections (§3.2, §6, §6.4) marked TODO. Hard rule H1: no code begins until this spec is reviewed and all cited ADRs are Decided.
>
> **Opened**: 2026-04-27 (orchestrator draft pre-fill)
> **Tag target**: `ship-4`
> **Parent**: [`ship-3.md`](ship-3.md) (tag `ship-3` does not move; Ship-4 opens against the Ship-3 server).
> **Slice intent (orchestrator recommendation, user-confirmable in §6)**: pure case-progression + assignment-churn under existing identity + shape stack. Single activity. Corrections deferred to Ship-5; cross-flow linking deferred to Ship-6; auto-resolution / "waiting too long" alerts deferred to Ship-7.

---

## 1. Scenarios delivered

Ship-4 delivers the **state-progression half of [S08](../scenarios/08-case-management.md)**: situations identified, tracked across multiple interactions over time, with shifting responsibility between actors, until they reach a conclusion. State is derived from the event sequence ([ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns)); responsibility shifts via runtime `assignment_changed` events ([ADR-003 §S2](../adrs/adr-003-authorization-sync.md)). Continues under the [S19](../scenarios/19-offline-capture-and-sync.md) offline constraint.

| Scenario | What Ship-4 delivers |
|---|---|
| [S08](../scenarios/08-case-management.md) (state-progression half) | A `case/v1` shape (deployer-CONFIG per [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant)) carrying lifecycle events as `type=capture` envelopes with `state` in payload (open / progressed / resolved). Per-subject case timeline projected on demand from events; no cache. Reassignment supported via runtime `assignment_changed` events. Out-of-scope-village case-opening rejected. "Waiting too long" surfaces as a read-only query against the projection — not a flag. |
| [S19](../scenarios/19-offline-capture-and-sync.md) | Multi-actor case progression (opener / progressor / coordinator-reassigner / resolver) simulated as distinct bearer tokens hitting the real server. Offline capture of case events under assigned scope; server-side scope check uses `subject_ref` event-time scope ([ADR-003 §S4](../adrs/adr-003-authorization-sync.md)) — Ship-1 W-2 precedent extended from `household_observation/v1` to `case/v1`. |

### Delivery surface (H8)

Scripted multi-actor HTTP simulation against the real server (Ship-1 / Ship-2 / Ship-3 precedent). Distinct bearer tokens stand in for distinct devices/actors; no Flutter client is exercised this Ship. The mobile delivery surface for case-tracking — long-running per-subject timeline on a real device under intermittent connectivity — defers to a Ship-4b-shaped sub-Ship if/when mobile case-tracking becomes load-bearing (same shape as the Ship-1b precedent for Ship-1).

### Composite-scenario coverage (H9)

| Composite | Bullets exercised by Ship-4 | Bullets carried forward |
|---|---|---|
| [S20 — CHV field operations](../scenarios/20-chv-field-operations.md) | **Bullet 4 (continuous activities — ongoing engagement, follow-ups, situations developing over time)** explicitly stressed for the first time. The case lifecycle IS the continuous activity. **Bullet 5 (history of what was done, when, and by whom)** additionally stressed via the per-case event timeline (per-request projection replay, multi-actor authorship visible). | Bullets 1, 2, 3 — none touched. |
| [S21 — CHV supervisor operations](../scenarios/21-chv-supervisor-operations.md) | **Partial.** Supervisor visibility into team workload exercised by the case-listing surface (which cases are active per actor, queryable from the projection). Approval / corrective oversight remains 0% — Ship-5. | All other S21 surface remains 0% covered. |
| [S05 — supervision / audit visits](../scenarios/05-supervision-audit-visits.md) | **None.** S05 is reviewer-driven; no review pipeline lands until Ship-5. | All S05 surface remains 0% covered. |

### Carry-back acknowledgement

Not needed. Ship-3 §1 already filed the historical composite-coverage carry-back for Ship-1 / Ship-1b / Ship-2. Ship-4 declares fresh per the H9 rule.

### Scenarios deliberately not delivered

- **[S04](../scenarios/04-supervisor-review.md) / [S11](../scenarios/11-multi-step-approval.md)** — review and approval. Ship-5.
- **[S07](../scenarios/07-resource-distribution.md) / [S09](../scenarios/09-campaign-execution.md) / [S14](../scenarios/14-supply-receipt.md) / [S22](../scenarios/22-deployer-onboarding.md)** — transfer / campaigns / deployer onboarding. Ship-6.
- **[S02](../scenarios/02-reactive-followups.md) / [S10](../scenarios/10-deadline-checks.md) / [S12](../scenarios/12-trigger-driven-actions.md)** — reactive layer / triggers. Ship-7.
- **[S13 cross-flow linking](../scenarios/13-cross-flow-linking.md)** — [`docs/ships/README.md`](README.md) notes S13 lands "opportunistically when Ship-4 introduces two coexisting activities." Ship-4 keeps a **single activity**. Re-deferred to Ship-6 (transfer / distribution introduces the second activity naturally). Bundling cross-flow linking would push Ship-4 into multi-cluster territory.
- **[FP-005](../flagged-positions.md#fp-005--corrections-surface-is-unassigned-in-the-5-ship-map) corrections** (CHV-initiated amendment of prior captures) — separate ADR cluster from state-progression; lands at Ship-5 (review naturally pulls it in).
- **Auto-resolution / "waiting too long" alerts** — Ship-7 reactive layer ([ADR-005 §S9](../adrs/adr-005-state-progression.md#s9-auto-resolution-as-l3b-sub-type) auto-resolution). Without triggers, "waiting too long" is a query against case-state-as-of-now, computed in the projection on demand. Acceptable for Ship-4.
- **Mobile delivery surface for case timelines** — Ship-4b-shaped sub-Ship deferred (Ship-1b precedent).

---

## 2. ADRs exercised

Ship-1 + Ship-1b + Ship-2 + Ship-3 exercised ADR-001, ADR-002 §S1/§S5/§S6–§S14, ADR-003 §S1/§S3/§S4, ADR-004 §S1/§S10/§S13, ADR-006, ADR-007, ADR-008, ADR-009 §S1/§S2. Ship-4 is the **first exercise of [ADR-005](../adrs/adr-005-state-progression.md)** (state-progression / projection-derived state machines) and the **first exercise of [ADR-003 §S2](../adrs/adr-003-authorization-sync.md)'s runtime-reassignment clause** (assignments mutating after deployment, not just at bootstrap).

| ADR §S | What it commits | How Ship-4 exercises it |
|---|---|---|
| [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns) | *"Subject lifecycle state is derived from the event sequence by the projection engine using pattern-defined state machine rules. State is never stored in events. The platform does not enforce transitions — it flags violations."* | First exercise. Ship-4 lands the projection layer that derives `current_state` for a `case/v1` subject by replaying its event sequence on demand. State (`open` / `progressed` / `resolved`) lives in payload, not in envelope. The Command Validator on-device is advisory; on the server, transitions are evaluated against a pattern definition (§6 sub-decision) but Ship-4's slice does **not** emit `transition_violation` flags (sub-decision 6) — the flag-emission half of §S1/§S2 is parked for Ship-5/Ship-7 where the workflow surfaces force it. |
| [ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry) | Pattern Registry — platform-fixed workflow skeletons; `case_management` pattern is one of four named existence-proof patterns. *"Patterns are a closed vocabulary with the same governance as event types — platform-fixed, deployer-referenced, not deployer-authored."* | First exercise. Ship-4 ships a minimal `case_management` pattern definition (states + transitions) as a JAR-bundled fixture (consistent with Ship-3's expedient pattern for shape registry — see [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies)). The deployer-authored pattern surface is parked. Whether the Ship-4 pattern definition is a separate artifact or inlined into the case shape is a §6 sub-decision. |
| [ADR-005 §S6](../adrs/adr-005-state-progression.md#s6-pattern-composition-rules) | Five composition rules. **Rule 1**: one subject-level pattern per activity. **Rule 5**: shape-to-pattern uniqueness within an activity (deploy-time validation). | First exercise of Rule 1 + Rule 5. Ship-4's single activity binds exactly one subject-level pattern (`case_management`) to subjects-as-cases; no other pattern competes. Rule 5 is honoured trivially (one shape, one pattern). Rules 2/3/4 not stressed (no event-level patterns, no sub-flows, no cross-activity linking). |
| [ADR-005 §S1](../adrs/adr-005-state-progression.md#s1-transition-violation-flag-category) | `transition_violation` flag category; events flagged but stored. | **Mechanism present, not exercised in this slice.** Ship-4 sub-decision 6 declines to emit `transition_violation` flags from the case projection in this slice (the projection identifies invalid transitions but the slice does not author the corresponding flag-emission code path). Recorded so the §S commitment is not regressed: invalid case transitions are accepted-and-stored ([ADR-001 §S1](../adrs/adr-001-offline-data-model.md), [ADR-002 §S14](../adrs/adr-002-identity-conflict.md)) and remain visible in the timeline; flag-emission surface defers to the Ship that needs it. |
| [ADR-003 §S2](../adrs/adr-003-authorization-sync.md) | *"When scope changes (reassignment), the next sync adjusts the payload."* Sync scope = access scope; reassignment is the runtime mutation of that scope. | **First exercise at runtime** (Ship-1 / Ship-2 / Ship-3 only emitted `assignment_created/v1` at bootstrap, never under live operation). Ship-4 emits coordinator-authored `assignment_changed` events (`assignment_created/v1` + `assignment_ended/v1` pair) at runtime via [`ServerEmission`](../../server/src/main/java/dev/datarun/ship1/sync/ServerEmission.java) (the only path — Ship-2 precedent). [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java)'s event-replay-as-of-T discipline must continue to compute the active assignment correctly across pre-reassignment and post-reassignment query times — this is the R2 closure (§3.1). |
| [ADR-003 §S4](../adrs/adr-003-authorization-sync.md) | Alias-respects-original-scope: events authorized against the `subject_ref` as written, not the post-merge survivor. | **Inherited.** Ship-4's case-opening scope check uses the event's `subject_ref` (or the subject-via-case-link) at event time, not current state. Same discipline as Ship-1 W-2 / Ship-2 W-3, applied to a new shape. |
| [ADR-001 §S1](../adrs/adr-001-offline-data-model.md) | Append-only immutability. *"Corrections, reviews, status changes, and amendments produce new records that reference earlier ones."* | Case lifecycle is the cleanest exercise of this clause to date: case progression IS a sequence of new records referencing the same subject. No envelope field is mutated; no payload is rewritten. State is what the events say it is. |
| [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | State is a projection of events; B→C escape hatch only if rebuildable + non-authoritative. | Case-state projection follows the [FP-002 (a)](../flagged-positions.md#fp-002--subject_lifecycle-table-read-discipline-audit) pattern carried from Ship-2 / Ship-3: per-request replay, no projection cache. R1 (§3.1) names the failure mode. |
| [ADR-007 §S1](../adrs/adr-007-envelope-type-closure.md#s1-the-envelope-type-vocabulary-is-closed-at-six-values) | Envelope `type` vocabulary closed at six values. | **Unchanged by Ship-4.** Case lifecycle uses `type=capture` events with state in payload. NO new envelope type. Recorded to defend F-A1 against a tempting-but-wrong "let's add `case_opened` / `case_resolved` types" mistake — R3 (§3.1) names the failure mode. The §S4 commitment in ADR-005 explicitly records this evaluation: *"`status_changed` was evaluated and rejected; the escape hatch (append-only vocabulary, ADR-4 S3) is preserved."* |
| [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) | Platform-fixed vs deployer-configured duality. Mechanism vs instance. | `case` is a PRIMITIVE (mechanism, platform-fixed). `case/v1` is CONFIG (deployer-instance — the deployer chose to model "case management" against their domain). Same duality as `household_observation/v1` from Ship-1. |
| [ADR-008 §S1](../adrs/adr-008-envelope-reference-fields.md#s1-subject-ref-envelope-field) | `subject_ref` envelope field. | **Inherited.** Cases anchor to subjects via the envelope `subject_ref` (the household / person / asset the case is about). No new envelope ref field. [FP-004](../flagged-positions.md#fp-004--assignment_ref-as-potential-future-envelope-field) (potential `assignment_ref` envelope field) is **not triggered** by Ship-4: case → actor linkage lives in payload (`assigned_to_actor_id`), not on the envelope. |

### Inherited invariants (must not regress)

| Inherited from | Invariant | How Ship-4 stays clean |
|---|---|---|
| Ship-2 / [FP-002](../flagged-positions.md#fp-002--subject_lifecycle-table-read-discipline-audit) (a) | No `subject_lifecycle` cache; projections replay from events on demand. | Case-state projection follows the same pattern. No `case_state_cache` table. |
| Ship-2 / contract | `subject_split.schema.json` `successor_ids: array, minItems: 2`. | Ship-4 does not edit `subject_split.schema.json`. |
| Ship-2 / [FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced) | Drift-gate check 4 (`contracts/shapes` ↔ `server/src/main/resources/schemas/shapes` byte-identical). | Any shape Ship-4 edits or adds (e.g., `case/v1`) is mutated in lock-step in both trees inside the same commit. |
| Ship-1 / Ship-2 / Ship-3 | Server-emitted events use shared [`ServerEmission`](../../server/src/main/java/dev/datarun/ship1/sync/ServerEmission.java) (`SERVER_DEVICE_ID = 00000000-…-0001` and `server_device_seq`). | Coordinator-emitted runtime `assignment_changed` events go through `ServerEmission` (no parallel emission path). |
| Ship-2 | Coordinator authority via projection from `assignment_created/v1` `role="coordinator"`. | Ship-4 inherits. Case-opening / reassignment authority is a §6 sub-decision (recommendation: any in-scope actor can open; coordinator-only for reassignment, matching Ship-2's coordinator surface). |
| Ship-3 | `ConflictDetector` unchanged ([FP-009](../flagged-positions.md#fp-009--conflictdetector-field-name-coupling) closed). | Ship-4 must not reopen FP-009. The detector reads `village_ref` and `household_name` from `household_observation/v1` only; `case/v1` events do not enter the detector's identity-conflict path. |
| Ship-3 | Multi-version registry ([`ShapePayloadValidator`](../../server/src/main/java/dev/datarun/ship1/event/ShapePayloadValidator.java) loads both filename conventions). | Ship-4 inherits the validator. If `case/v1` is the only Ship-4 shape, the multi-version path is exercised at registry-load only, not actively. |
| Ship-3 | Branch on `shape_ref` for shape-version concerns; on `actor_ref` for system-vs-human authorship; on envelope `type` only for the closed six-value pipeline answer (F-A1 / F-A2 / F-A3 / F-A4). | Ship-4 honours. Case-projection logic discriminates on `shape_ref = case/v1`, never on envelope `type`. |

### Out of scope (explicitly not exercised)

- **ADR-002 §S6–§S14** — identity already exercised; Ship-4 does not extend identity. No merge / split work.
- **ADR-004 §S10** — shape evolution already exercised at Ship-3; Ship-4 does NOT introduce a v2 of any shape. `case/v1` is a new shape, not an evolution.
- **ADR-004 §S11 / §S12 / §S14** — expressions / triggers / deployer-parameterized policies — Ship-7 reactive layer.
- **ADR-005 §S2** — flagged events excluded from state derivation. **Mechanism not exercised** because Ship-4 does not emit `transition_violation` flags (sub-decision 6); §S2's "flagged event still appears in timeline" property is structurally honoured (events are stored regardless), but the projection's flag-aware exclusion path is not authored this Ship.
- **ADR-005 §S3** — flag resolvability classification — no flags emitted by the case path.
- **ADR-005 §S7** — source-only flagging — no upstream flags propagate in this slice.
- **ADR-005 §S8** — `context.*` expression scope — no expressions in Ship-4 (Ship-7).
- **ADR-005 §S9** — auto-resolution as L3b sub-type — Ship-7.
- **ADR-009 §S2 / §S3 / §S4** — pattern registry persistence beyond JAR-bundled fixture, deployer-onboarding surface — defer to [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies).
- **Mobile delivery surface** (Ship-4b-shaped sub-Ship deferred).
- **Cross-flow linking** ([S13](../scenarios/13-cross-flow-linking.md)) — re-deferred to Ship-6.

---

## 3. ADRs at risk of supersession

### 3.1 Structural risks

| Risk | ADR position at risk | Observable that would force supersession |
|---|---|---|
| **R1 — Case-state projection rebuild discipline** | [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns) + [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | The case timeline is rendered from a cache rather than per-request event replay — repeating the [FP-002](../flagged-positions.md#fp-002--subject_lifecycle-table-read-discipline-audit) mistake one layer over (lifecycle cache for cases). Forces either rebuild proof at retro or a new FP. |
| **R2 — Assignment-churn temporal correctness** | [ADR-003 §S2](../adrs/adr-003-authorization-sync.md) | A case reassigned to actor B at time T2, then queried "who owned this case at T1" returns B (read-current instead of read-as-of-T1) — violating event-replay-as-of-T temporal correctness. The [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) pattern (event-replay-as-of) must extend cleanly to assignment-churn under cases. Forces `ScopeResolver`-shaped temporal-correctness assertion at retro. |
| **R3 — Case shape vs envelope type discipline (F-A1 / F-A2 defense)** | [ADR-007 §S1](../adrs/adr-007-envelope-type-closure.md#s1-the-envelope-type-vocabulary-is-closed-at-six-values) + charter F-A1 | Tempting-but-wrong "let's add `case_opened` envelope type" appears in the build and is rejected. Case lifecycle stays in payload state, NOT in envelope type. ADR-005 §S4 explicitly anticipated and rejected this option for `status_changed`; Ship-4 is its first concrete-build defense. Pre-stated to surface the mistake before it lands. |
| **R4 — "Waiting too long" query staleness** | [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) | "Waiting too long" is computed from cached state rather than per-request event replay → stale or inconsistent results. The "waiting too long" query is the closest Ship-4 surface to a derived analytical view; without discipline it becomes a quiet projection cache. Rebuild proof or new FP at retro. |
| **R5 — Subject-deactivation interaction with active cases (S06b parked surface re-enters)** | [ADR-002](../adrs/adr-002-identity-conflict.md) (S6b deactivation surface) + [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns) | A subject is deactivated (the S06b deferred surface, parked at Ship-3 §6.5) while an active case references it. What happens to the case? — undefined today. Ship-4 may surface this as an observation; if no walkthrough exercises it explicitly, record in §3.2 / retro §4 as either deferred-with-FP or out-of-scope-confirmed. Likely an FP at retro if not pre-decided in §6. |

### 3.2 Domain-realism risks

> **TODO at spec lock by user.** Locked at §6 commitment. Two observations to consider pre-build (recommend recording them at retro instead unless predictable now):
> - **DR-1 candidate**: case timeline renders multi-shape if `case/v1` events coexist with `household_observation/v1+v2` capture events linked to the same subject. Operator-UX risk surfaces only at admin rendering — does the per-subject view show a unified timeline (case events + capture events on the same subject) or two parallel timelines? §6 sub-decision territory.
> - **DR-2 candidate**: "waiting too long" threshold is a deployer policy ([ADR-004 §S14](../adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies) territory) — Ship-4 hard-codes a threshold or queries dynamically. The choice is itself a §6 sub-decision; if hard-coded, log a candidate FP for ADR-004 §S14 deployer-parameterized policies eventually.
>
> Both are pre-build speculation. Ship-3 precedent: novel surfaces are higher-signal observed than guessed.

---

## 4. Ledger concepts touched

| Concept | Proposed classification | Status after Ship-4 | Settled-by |
|---|---|---|---|
| `case` | PRIMITIVE (mechanism) | STABLE | [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns), [ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry), [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) |
| `case/v1` | CONFIG (deployer-instance shape) | STABLE | [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant), [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) |
| `case_state_progression` | DERIVED (projection from events) | STABLE | [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns), [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) |
| `assignment_churn` | observed-pattern row (whether a ledger row asserts depends on the §6 sub-decision shape) | retro-asserted | [ADR-003 §S2](../adrs/adr-003-authorization-sync.md), [ADR-002](../adrs/adr-002-identity-conflict.md) (Assignment as event timeline) |
| `case_ownership_history` | DERIVED | STABLE | [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns), [ADR-003 §S2](../adrs/adr-003-authorization-sync.md) |
| `case_management` (pattern) | PRIMITIVE / Pattern Registry entry | retro-asserted | [ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry) |

Note: classifications are RECOMMENDED; retro confirms.

---

## 5. Flagged positions — consult ([Rule R-4](../flagged-positions.md))

R-4 sweep ran 2026-04-27 (draft pre-fill) against the proposed pure-state-progression + assignment-churn slice.

| FP | Status | Ship-4 interaction (verdict) |
|---|---|---|
| [FP-001](../flagged-positions.md#fp-001--role_stale-projection-derived-role-verification) — `role_stale` temporal-divergence test | OPEN | **FIRST-LOAD-DECISION-NEEDED.** Cases route work between actors over time; first time role-changes-mid-case become observable. **Decision needed at §6**: Ship-4 explicitly stresses role-temporal-divergence (would close the FP-001 outstanding gate piece), OR §6.5-defers it. **Recommend §6.5-defer** to Ship-5 (review actor changes naturally pull it in). Confirm at lock. |
| [FP-002](../flagged-positions.md#fp-002--subject_lifecycle-table-read-discipline-audit) | RESOLVED at Ship-2 | Pattern (a) inherited; case-state projection follows the same no-cache discipline (R1). |
| [FP-003](../flagged-positions.md#fp-003--envelope-schema-parity-test-meta-drift-protection) | RESOLVED | No action. |
| [FP-004](../flagged-positions.md#fp-004--assignment_ref-as-potential-future-envelope-field) — `assignment_ref` envelope field | OPEN | **RECOMMEND-DEFER.** Cases reference actors-as-assignees; an `assignment_ref` envelope field would clarify the linkage. Recommend §6.5-defer (don't extend the envelope mid-Ship; case → actor linkage lives in payload, same pattern as `subject_ref` already does for case → subject). |
| [FP-005](../flagged-positions.md#fp-005--corrections-surface-is-unassigned-in-the-5-ship-map) — corrections surface | OPEN | **DOES-NOT-BLOCK** the state-progression slice. §6.5-deferred to Ship-5 (review naturally pulls it in). |
| [FP-006](../flagged-positions.md#fp-006--s7s8-attribution-semantics-in-the-corrective-split-case) — S7↔S8 attribution under corrective split | OPEN | **FIRST-LOAD-DECISION-NEEDED.** Cases reference subject UUIDs; if a case's subject undergoes a corrective split mid-case, FP-006 prerequisites materialize for the first time. **Decision needed at §6**: Ship-4 explicitly commits walkthroughs do NOT exercise corrective-split-against-cased-subject (defers), OR §6.4 includes a walkthrough that surfaces this. **Recommend defer** (single-cluster discipline; identity churn alongside state progression bundles two ADR clusters under stress simultaneously). |
| [FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced) | RESOLVED at Ship-2 | Drift-gate check 4 stays in place. Any shape edit Ship-4 makes (e.g., adding `case/v1`) lock-steps both trees in the same commit. |
| [FP-008](../flagged-positions.md#fp-008--conflict_detected-payload-lacks-root_cause-trace-metadata) — `conflict_detected` lacks `root_cause` | OPEN per path (c) | **FIRST-LOAD-DECISION-NEEDED.** S08 is the first scenario where a flag's "source of badness" can differ from the source event (case-state-stale flag fires on event N because of event M). **Decision needed at §6**: Ship-4 surfaces this and closes FP-008, OR Ship-4 does not emit case-state-stale flags. **Recommend the latter** — fits the no-triggers slice (sub-decision 6); "waiting too long" is read-only query, not flag emission. FP-008 carries to Ship-7 cleanly. |
| [FP-009](../flagged-positions.md#fp-009--conflictdetector-field-name-coupling) | RESOLVED at Ship-3 | Does not block. Ship-4 must not reopen — `case/v1` events do not enter the detector's identity-conflict path. |
| [FP-010](../flagged-positions.md#fp-010--cross-version-projection-composition-contract) — cross-version projection composition | OPEN | **DOES-NOT-BLOCK.** Ship-4 introduces no v2 of any shape; carries forward. |
| [FP-011](../flagged-positions.md#fp-011--household_observation-directory-classification-re-deferral) — `household_observation` directory classification | OPEN (re-deferred) | **DOES-NOT-BLOCK.** Gates with FP-012; carries forward. |
| [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) — deployer-authoring surface | OPEN | **FIRST-LOAD-DECISION-NEEDED.** Ship-4 needs `case/v1` shape (and a `case_management` pattern definition per [ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry)) — fits "first Ship requiring a non-fixture shape" trigger condition. **Decision needed at §6**: ship `case/v1` + pattern as JAR-bundled fixtures (extend the expedient one more Ship), OR open Ship-4 + FP-012 closure as a sub-Ship pair. **Recommend extend the expedient.** S08 is heavy enough as state-progression first-exercise without simultaneously building the deployer-authoring surface. Ship-4 §6.1 sub-decision documents the expedient; FP-012 trigger now points at Ship-5 or first deployer-onboarding Ship, whichever first. |
| [FP-013](../flagged-positions.md#fp-013--config-package-wire-versioning-scheme) — config-package wire-versioning | OPEN | **DOES-NOT-BLOCK.** Gates with FP-012; carries forward. |

**Inherited open RFS items** (Ship-3 precedent format):

- **RFS-1** (naïve identity heuristic): unchanged. Ship-4 does not touch identity.
- **RFS-2** (village-on-payload): unchanged. Ship-4 does not touch the village representation.
- **RFS-3** (schema duplication): the FP-007 drift-gate continues to defend.

**New FPs anticipated**:

- Likely one on case-state staleness operational UX if §3.2 DR-1 / DR-2 surface during build (multi-shape timeline rendering OR hard-coded "waiting too long" threshold).
- Possibly one on subject-deactivation-while-cased (R5) if not pre-decided in §6 and observed during build.
- Possibly one on `case_management` pattern persistence-outside-the-JAR (folds into FP-012's gate, but may surface as its own row if Ship-4 sub-decision 2 records a pattern-specific expedient distinct from the shape-registry expedient).

---

## 6. Slice

**TODO at spec lock by user.** Provided below is the orchestrator's recommended starting position; user confirms / overrides at §6.1 lock.

### 6.1 Sub-decisions (orchestrator recommendation — user to confirm)

1. **New shape: `case/v1`.** Deployer-CONFIG per [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) (same duality as `household_observation/v1` from Ship-1). Recommended top-level payload fields:
   - `state` (enum: `open` / `progressed` / `resolved` — required)
   - `state_reason` (string, optional — free-text annotation on transition)
   - `assigned_to_actor_id` (UUID, optional — defaults to opener at open-state; reassignment via runtime `assignment_changed` event, not via overwriting this field on subsequent case events)
   - **NOT** `case_id` separately (cases are identified by the envelope `subject_ref` of the case-subject; one case per case-subject in this slice — confirm at lock if multiple cases per subject is needed, in which case `case_id` becomes payload-required)
   - **NOT** `subject_id` in payload (envelope `subject_ref` already carries it — F-A4 / no payload duplication)

   Confirm field set at lock.

2. **Shape registry storage: JAR-bundled fixture continued ([FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) expedient extended one more Ship).** `case/v1` ships as a snapshot file in `server/src/main/resources/schemas/shapes/case.v1.schema.json` (Ship-3 precedent), mirrored byte-identically in `contracts/shapes/` under [FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced)'s drift-gate. **Pattern definition** for `case_management` ([ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry)) ships either as an inline server-side artifact (Java enum / record describing states + transitions) or as a separate JSON resource bundled alongside the shape — confirm at lock. **This is a named expedient extended one Ship.** FP-012 trigger now points at Ship-5 or first deployer-onboarding Ship, whichever first.

3. **Case lifecycle = `type=capture` events with payload state.** F-A1 defense. Three logical state values in payload — `open` / `progressed` / `resolved`. **NO new envelope type.** State transitions evaluated at the projection layer (against the pattern definition from sub-decision 2), not at the envelope layer. ADR-005 §S4's *"`status_changed` was evaluated and rejected"* applies directly — Ship-4 is its first build-time honoring.

4. **Assignment churn at runtime.** First non-bootstrap `assignment_changed` events. Coordinator-emitted `assignment_changed` event at runtime, going through [`ServerEmission`](../../server/src/main/java/dev/datarun/ship1/sync/ServerEmission.java) (same path as `ConflictDetector` flag emission — Ship-2 precedent). Mechanism: a coordinator-authenticated HTTP endpoint accepts a reassignment request, the server emits the `assignment_ended/v1` (closing the prior assignment) + `assignment_created/v1` (opening the new one) pair atomically, both events use the server `device_id` and the next two `server_device_seq` values. [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java)'s replay logic handles them identically to bootstrap-emitted assignment events.

5. **No `assignment_ref` envelope field** ([FP-004](../flagged-positions.md#fp-004--assignment_ref-as-potential-future-envelope-field) defer). Case → actor linkage in payload (`assigned_to_actor_id` from sub-decision 1).

6. **No flag emission for case staleness.** "Waiting too long" is a read-only query against case-state-as-of-now, computed in the projection on demand. Defers [FP-008](../flagged-positions.md#fp-008--conflict_detected-payload-lacks-root_cause-trace-metadata) cleanly to Ship-7 (when triggers + auto-resolution land per [ADR-005 §S9](../adrs/adr-005-state-progression.md#s9-auto-resolution-as-l3b-sub-type)). Threshold hard-coded for Ship-4 (recommend 7 days as a CHV-realistic default for follow-up cadence) — note as deployer-parameterized concern in retro candidate FP for [ADR-004 §S14](../adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies). Confirm threshold value at lock.

7. **Authority for case-opening / reassignment.** Recommend: any actor with an active assignment whose scope contains the case's subject can open a case (CHV opens cases on households in their assigned villages — out-of-scope opening is rejected per [ADR-003 §S1](../adrs/adr-003-authorization-sync.md), Ship-1 W-2 precedent extended to `case/v1`). **Reassignment requires coordinator** (matches Ship-2 coordinator authority surface; coordinator-only HTTP endpoint per sub-decision 4). Confirm at lock.

### 6.5 Out of scope (deliberately not built)

- **CHV-initiated corrections** ([FP-005](../flagged-positions.md#fp-005--corrections-surface-is-unassigned-in-the-5-ship-map)) — Ship-5 (review pulls it in naturally).
- **Cross-flow linking** ([S13](../scenarios/13-cross-flow-linking.md)) — Ship-6 (transfer / distribution introduces the second activity naturally; bundling here pushes Ship-4 multi-cluster).
- **Auto-resolution / "waiting too long" alerts** — Ship-7 reactive layer ([ADR-005 §S9](../adrs/adr-005-state-progression.md#s9-auto-resolution-as-l3b-sub-type)).
- **`transition_violation` flag emission** ([ADR-005 §S1](../adrs/adr-005-state-progression.md#s1-transition-violation-flag-category)) — sub-decision 6 declines emission; mechanism remains for a future Ship that needs it. Carries forward as observation.
- **Source-only flagging exercise** ([ADR-005 §S7](../adrs/adr-005-state-progression.md#s7-source-only-flagging)) — no upstream flags propagate this Ship.
- **`context.*` expression scope** ([ADR-005 §S8](../adrs/adr-005-state-progression.md#s8-context-expression-scope)) — Ship-7.
- **Multiple cases per subject** — Ship-4 single-case-per-subject (sub-decision 1, NOT `case_id`); revisit if a future scenario needs concurrent cases.
- **Subject-deactivation-while-cased** (R5 surface; S06b parked surface from Ship-3 §6.5 carries forward) — defer to a future Ship unless §6 explicitly pulls it in.
- **Mobile delivery surface for case timelines** — Ship-4b-shaped sub-Ship deferred (Ship-1b precedent).
- **Deployer-authoring surface for shapes / patterns** ([FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies)) — JAR-bundled fixture extended one more Ship.
- **Pattern Registry persistence outside JAR** ([ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry)) — gates with FP-012.
- **Pattern composition rules 2 / 3 / 4** ([ADR-005 §S6](../adrs/adr-005-state-progression.md#s6-pattern-composition-rules)) — event-level patterns, sub-flows, cross-activity linking — Ship-5/6.
- **`role_stale` temporal-divergence test** ([FP-001](../flagged-positions.md#fp-001--role_stale-projection-derived-role-verification) gate piece) — defer to Ship-5 unless §6 explicitly pulls it in.
- **Corrective-split-against-cased-subject path** ([FP-006](../flagged-positions.md#fp-006--s7s8-attribution-semantics-in-the-corrective-split-case)) — defer; single-cluster discipline.

### 6.4 Walkthroughs

**TODO at spec lock by user.** Provided below is the orchestrator's recommended starting position. Ship-3 used W-6 / W-7 / W-8 / W-10; Ship-4 picks up at W-11.

Each walkthrough asserts on `shape_ref` (F-A2) — never on envelope `type` for case-progression logic. Distinct bearer tokens stand in for distinct actors (case opener, progressor, coordinator, resolver).

- **W-11 — Case open + initial assignment (mandatory).** A CHV in their assigned village opens a case against an existing household subject (`household_observation/v1` capture done prior in setup). The opening event is `type=capture`, `shape_ref=case/v1`, payload `state=open`, `assigned_to_actor_id` defaults to the opener. Server validates against `case/v1` schema and persists. `/admin/cases` (new view) lists the case with `state=open` and the correct assignee. Exercises [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns) (state derived from event sequence) + [ADR-003 §S1](../adrs/adr-003-authorization-sync.md) (scope check on opener).
- **W-12 — Case progression (mandatory).** Same actor (or another actor in the same scope) submits a follow-up `case/v1` event with `state=progressed`. Both events visible in the case timeline; `/admin/cases/{id}` renders the sequence. **Per-request projection replay; no cache** — R1 closure assertion (a SQL or log-based proof point that the projection is replayed fresh on each `/admin/cases/{id}` request, not read from a `case_state_cache` table — verifies [FP-002](../flagged-positions.md#fp-002--subject_lifecycle-table-read-discipline-audit) (a) pattern carries to cases).
- **W-13 — Reassignment (mandatory).** Coordinator emits an `assignment_changed` event at runtime (sub-decision 4) reassigning the case-subject's assignment from CHV-A to CHV-B. **R2 closure assertion**: query [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java)'s active-assignment-as-of-T at T-before-reassign and T-after-reassign — must return CHV-A and CHV-B respectively. The case-ownership-history projection (§4 ledger row) reflects the change. Exercises [ADR-003 §S2](../adrs/adr-003-authorization-sync.md) (runtime reassignment).
- **W-14 — Case resolved (mandatory).** Final `case/v1` event with `state=resolved`. Case appears in resolved-list (or filters out of active-list at `/admin/cases?state=open`). Per-request projection — same R1 discipline as W-12. Exercises [ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns) (projection-derived terminal state).
- **W-15 — Out-of-scope case opening rejected (mandatory).** A CHV tries to open a case against a household-subject in a village outside their assigned scope → rejected (Ship-1 W-2 precedent on `household_observation/v1` capture extends naturally to `case/v1` capture). Server returns 400/403 with a `scope_violation`-shaped error; no `case/v1` event lands. Exercises [ADR-003 §S1](../adrs/adr-003-authorization-sync.md) on the new shape.
- **W-16 — "Waiting too long" query (mandatory).** Two cases pre-loaded: one with `state=progressed` within threshold (e.g., last event 2 days ago), one stale (e.g., last event 10 days ago). Query `/admin/cases?stale=true` returns only the stale one; threshold hard-coded per sub-decision 6. **NO flag emitted** — verified by asserting no `conflict_detected/v1` event lands during the query. Exercises [ADR-001 §S2](../adrs/adr-001-offline-data-model.md#s2-write-granularity) (R4 closure — query computes per request from events, not from a staleness cache).

---

## 7. Retro criteria

The retro must show, mechanically:

1. **Walkthroughs pass via HTTP.** `Ship4WalkthroughAcceptanceTest` extends prior precedent (`WalkthroughAcceptanceTest` / `Ship2WalkthroughAcceptanceTest` / `Ship3WalkthroughAcceptanceTest`); all Ship-1 + Ship-1b + Ship-2 + Ship-3 walkthroughs remain green; Ship-4 W-11..W-16 pass. Suite total grows; no prior tests removed.
2. **Commits cite scenarios.** Every Ship-4 commit's subject line carries one of `S08`, `S19`, `S20` (e.g., `feat(ship-4): S08 — case_management pattern definition`). Build-tooling-hygiene `chore(...)` commits without scenario cites are flagged in §10 (Ship-2 / Ship-3 precedent).
3. **[ADR-005 §S4](../adrs/adr-005-state-progression.md#s4-state-machines-as-projection-patterns) exercised.** Proof: case timeline replays from events per request; per-event state visible in the timeline; current state derived (never stored on envelope, never cached). Evidence: walkthrough output + a SQL or grep proof point.
4. **[ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry) exercised.** Proof: `case_management` pattern definition is loaded from a platform-fixed source (JAR-bundled fixture) and used by the projection engine to derive state. The deployer did not author the pattern; the platform shipped it.
5. **[ADR-005 §S6](../adrs/adr-005-state-progression.md#s6-pattern-composition-rules) Rule 1 + Rule 5 honoured.** Proof: only one subject-level pattern bound to case-subjects in this activity; no shape-to-pattern conflict. Trivially satisfied since Ship-4 ships one pattern; recorded so future Ships do not regress.
6. **[ADR-003 §S2](../adrs/adr-003-authorization-sync.md) assignment-churn-at-runtime exercised.** Proof: `assignment_changed` event accepted at runtime via `ServerEmission`; [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) computes active-assignment-as-of-T correctly across pre/post-reassignment query times (W-13 + a SQL/grep proof point).
7. **[FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced) drift-gate stays PASS.** `bash scripts/check-convergence.sh` exit 0; check 4 byte-identical after `case/v1` shape additions. Both trees (`contracts/shapes/case.v1.schema.json` + `server/src/main/resources/schemas/shapes/case.v1.schema.json`) mutated lock-step in the same commit.
8. **Inheritance not regressed.** Ship-1 W-0 / W-1 / W-2 + Ship-1b M1 / M2 + Ship-2 W-3 / W-4 / W-5 + Ship-3 W-6 / W-7 / W-8 / W-10 all green at Ship-4 close. No envelope `type` vocabulary edit (F-A1, R3 closure). No `subject_split.schema.json` arity narrowing.
9. **R1 closure**: case-state projection has rebuild proof (no cache, per-request replay verified by W-12 evidence) OR new FP recorded.
10. **R2 closure**: temporal-correctness of assignment-churn proven (W-13 + ScopeResolver-as-of-T evidence) OR new FP recorded.
11. **R3 closure**: no envelope `type` vocabulary edit; no `if (event.type() == "case_opened")`-style code; `git diff ship-3..HEAD -- contracts/envelope.schema.json server/src/main/resources/envelope.schema.json` returns zero lines.
12. **R4 closure**: "waiting too long" query is per-request (W-16 evidence) OR new FP recorded.
13. **R5 observation**: subject-deactivation-while-cased explicitly observed during build OR explicitly deferred at retro with new FP / re-defer record.
14. **No new ADR drafted unless R1–R5 / §3.2 triggered one.** Frame 4 applied to any retro position that *feels* architectural.
15. **Ledger rows updated** (§4 rows: `case`, `case/v1`, `case_state_progression`, `assignment_churn` if asserted, `case_ownership_history`, `case_management` pattern). Counts incremented in the close-out summary.
16. **Charter regenerated; drift gate PASS.** [`docs/charter.md`](../charter.md) §Status regenerated from ledger; all four checks PASS.
17. **Composite S20 bullet 4 + S21 partial coverage statement filed.** Retro records: (a) S20 bullet 4 (continuous activities) explicitly stressed for the first time; (b) S20 bullet 5 (history) additionally stressed via case timeline; (c) S21 partial — supervisor visibility into team workload via case-listing; (d) S05 unchanged.
18. **Retro note filed.** `ship-4-retro.md` covers walkthroughs, criteria, R1–R5, §3.2 observations, implementation-grade choices, FPs touched, ledger deltas, Ship-5 handoff, OQs, cosmetic notes.
19. **`ship-4` tag applied; `ship-3` / `ship-2` / `ship-1b` / `ship-1` unmoved.**

---

## 8. Hand-off to Ship-5

Filled at retro.

---

## 9. Open questions — to resolve before build

To be filled by the user. Default recommendations from §6.1 below; user confirms / overrides at §6 lock.

- **OQ-1**: Confirm `case/v1` payload field set (sub-decision 1). Default: `state` + `state_reason` + `assigned_to_actor_id`; **drop** redundant `subject_id` from payload (envelope `subject_ref` already carries it); **drop** redundant `case_id` (cases identified by case-subject UUID, single case per subject in this slice).
- **OQ-2**: Confirm FP-012 expedient extension for `case/v1` shape + `case_management` pattern (sub-decision 2). Default: extend (JAR-bundled fixture). Confirm whether the pattern definition is a separate JSON resource or an inline server-side Java artifact.
- **OQ-3**: Confirm assignment-churn surface (sub-decision 4). Default: coordinator-emitted `assignment_changed` event at runtime via [`ServerEmission`](../../server/src/main/java/dev/datarun/ship1/sync/ServerEmission.java); coordinator-authenticated HTTP endpoint accepts the reassignment request and the server emits the `assignment_ended/v1` + `assignment_created/v1` pair atomically.
- **OQ-4**: Confirm authority for case-opening / reassignment (sub-decision 7). Default: any actor whose active assignment scope contains the case-subject can open; reassignment = coordinator only.
- **OQ-5**: Confirm "waiting too long" threshold strategy (sub-decision 6). Default: hard-coded 7 days; document in retro candidate FP for [ADR-004 §S14](../adrs/adr-004-configuration-boundary.md#s14-deployer-parameterized-policies) deployer-parameterized policies eventually. Confirm threshold value.
- **OQ-6**: Confirm walkthrough W-13 covers temporal-correctness assertion explicitly (R2 closure path). Default: yes — assert ScopeResolver's active-assignment-as-of-T at T-before-reassign returns the prior assignee, at T-after-reassign returns the new assignee.
- **OQ-7** (from R5): Subject-deactivation-while-cased — pre-decide in §6 OR observe-and-record at retro? Default: observe-and-record (no pre-decision; defer the surface to a future Ship that owns deactivation).
- **OQ-8** (from §3.2 DR-1): Multi-shape timeline rendering at `/admin/cases/{id}` — does the case view show only `case/v1` events or also linked `household_observation/v1+v2` events on the same subject? Default: case-only (single-shape timeline); subject-wide unified timeline lives at `/admin/events?subject_id=…` already.

---

## 10. Skill-gap note (orchestrator)

None at draft. Surface at retro.
