# Ship-4 §6 — Architect's Recommendation (G-8 redo)

> Supersedes prior draft at commit `2b2ee9b` per orchestrator G-8 re-dispatch — prior draft conformed to a provisional `ship-4.md` instead of defining scope from closeout findings. This draft defines scope.
>
> **Type**: architect's recommendation set — **not** the Ship-4 spec.
> **Owner**: user (per H1, the spec author owns §6 lock).
> **Source dispatch**: Ship-3 closeout Wave 4 deferred item G-8 (re-dispatched 2026-04-28).
> **Substrate (closeout-finding-driven, NOT `ship-4.md`)**:
> [`docs/reviews/system/adr-005-parity.md`](../reviews/system/adr-005-parity.md) (G-4 parity walk),
> [FP-014](../flagged-positions.md#fp-014--scope-eval-pull-class-temporal-anchor-disambiguation) three pull-classes,
> [FP-018](../flagged-positions.md#fp-018--assignment_endedv1-validated-but-never-consumed-in-scope-reconstruction) `assignment_ended/v1` consumption gap,
> [ADR-006-R §S5](../adrs/adr-006-flag-semantics-R.md#s5--alias-cycle-prevention-as-a-push-path-guard-with-cycle_violation-flag) alias-cycle guard now in place,
> [`docs/reviews/system/domain.md` C1-04](../reviews/system/domain.md) topology floor,
> [`access-control-scenario.md`](../access-control-scenario.md) authority semantics,
> [S04](../scenarios/04-supervisor-review.md) and [S08](../scenarios/08-case-management.md) prose,
> [P04 Responsibility Binding](../behavioral_patterns.md).
> **What is `docs/ships/ship-4.md` here?** A pre-closeout scratchpad. Not authoritative. SD-0 may recommend §1 of that file be discarded.
> **Disposition**: the user reads, accepts / edits / rejects each SD, then folds chosen answers into a re-authored `docs/ships/ship-4.md` at spec lock.

---

## §1 SD-0 — Recommended Ship-4 scope

### Options considered

- **(α) S08 only** — case-management as the state-progression slice. ADR-005 §S4/§S5/§S6 R1+R5 first-exercised. FP-018 forced (case spans reassignment). FP-014 case-bound class first-exercised in tests; live-sync confirmed; historical-audit forward-cite by test, complete by doc.
- **(β) S04 + S08** — corrections + review + case-management bundled. All three FP-014 classes substantively tested. Adds ADR-005 §S5 `capture_with_review` pattern, §S6 R2 (event-level patterns), and **ADR-007 §S1 `review` envelope-type first-emission** on top of the §S4/§S5/§S6 R1+R5 case load. Plus FP-005 corrections shape.
- **(γ) S04 only** — corrections + supervisor review without case-management. Live-sync confirmed; case-bound + historical-audit forward-cite. FP-018 not strictly forced (supervisor reviewer is typically stable across the review window). Adds ADR-007 review-type first-emission. Lighter than β.
- **(δ) FP-only** — pull-class doc + FP-018 fix + alias-cycle guard hardening, no scenarios delivered. Charter § Rhythm: "A Ship delivers one or more scenarios" — δ is closeout-shaped, not Ship-shaped.
- **(ε) Other cleaving planes considered**: (i) S08 + audit-pull endpoint without S05 — solution-driven, rejected; (ii) S08 + S04's runtime-reassignment fragment only — subsumed by α (the case-handoff requires the same reassignment surface); (iii) S08 split into two micro-Ships (open/progress vs reassign/handoff) — the case-handoff is the load-bearing invariant of the slice; splitting it removes the SD-1 stress.

### Recommendation: **(α) S08 only**

Reasoning, four-axis derivation independent of the prior draft and of `ship-4.md`:

#### Axis 1 — ADR coherence (which scope lets the closeout findings land cleanly?)

| Closeout finding | Lands cleanest under |
|---|---|
| FP-014 three pull-classes (live-sync, historical-audit, case-bound) | **α**: case-bound has its natural first-exerciser in S08 (case timeline crosses reassignment — the SD-1 case-handoff requirement makes case-bound load-bearing). Historical-audit's natural first-exerciser is **S05** (auditor visit), not S04. S04's reviewer typically operates within live-sync — review windows are short and reviewer scope is co-current with capture scope. Bundling S04 into Ship-4 does NOT add a clean historical-audit walkthrough; it adds a `review`-type first-emission, which is a different axis. |
| FP-018 `assignment_ended/v1` consumption gap | **α** forces it (case-handoff = cross-village reassignment as a discrete event). γ does not force it. β forces it via the S08 half. |
| ADR-005 §S parity (§S4/§S5/§S6 R1+R5 first-exercise) | **α** is the natural first-exerciser. β duplicates onto §S5 (`capture_with_review` pattern + `case_management` pattern in the same Ship). γ first-exercises §S5 + §S6 R2 *without* §S4 — leaves §S4 as the heaviest first-exercise still pending. |
| Alias-cycle guard (now in place) | All four options: defense-in-depth only; no Ship interaction. Recorded as inherited invariant, not an SD. |

α's profile: **one ADR cluster first-exercised** (ADR-005 state-progression half) + **two §S transitions on prior ADRs** (ADR-003 §S2 runtime half; FP-018 closure). γ's profile: review-pattern + corrections-shape + ADR-007 review-type first-emission — **three new architectural surfaces**, no §S4 first-exercise. β bundles α + γ into one Ship.

#### Axis 2 — Closeout-finding velocity (does splitting the strategy doc force a re-open?)

The pull-class strategy doc (SD-5) is a *single architectural artifact* — disambiguation-by-class. Per the orchestrator's hard constraint, the doc is **complete-by-doc, partial-by-test**: all three classes substantively named in §2 of the doc, regardless of which Ship-4 walkthroughs exercise. Splitting case-bound (Ship-4) from historical-audit (Ship-5/S05) does NOT force the doc to be re-opened — the doc names all three at first authoring, and a future Ship simply transitions historical-audit's parity row from `decided-unexercised → exercised-met` without editing the doc's class definitions.

This means: **velocity is preserved under α**. The doc is authored once, completely. Tests fill in over Ships. The "splitting forces re-open" failure mode the orchestrator named applies to *partial doc + forward-cite stub* (which the prior draft incorrectly recommended) — not to *complete doc + partial test*.

#### Axis 3 — Scenario weight (do S04's review path and S08's case path share substrate?)

They do NOT share substrate enough to require bundling:

- S04 reviewer authority is event-scoped (review one capture). S08 case authority is subject-scoped (the case persists; ownership transfers). [P04](../behavioral_patterns.md) and [S08](../scenarios/08-case-management.md) variation "delegated or transferred" is what S08 stresses; S04 stresses "role-plus-context" (reviewer authority distinct from capturer authority). Different P04 variations.
- The load-bearing intersection — *a case under correction* — is an S04+S08+FP-005 three-way composite. That composite is the Ship-5 stress, not Ship-4 territory.
- ADR-005 §S5 patterns: `case_management` (subject-level) and `capture_with_review` (event-level) compose freely per §S6 R2 — they're designed to be exercised independently.

S04 and S08 are **orthogonal P04 variations** with disjoint state spaces (subject-level vs event-level state machines, ADR-005 §S6 R1 vs R2). Splitting is cheap; bundling is not the load-bearing intersection.

#### Axis 4 — Ship-size pressure-test (charter § Rhythm)

| Signal | α (S08) | β (S04+S08) | γ (S04) | δ (FP-only) |
|---|---|---|---|---|
| ADR clusters first-exercised | 1 (ADR-005 state-progression) + 2 §S transitions | 3 (ADR-005 state-progression + ADR-005 review-pattern + ADR-007 review-type half + corrections shape) | 2 (ADR-005 review-pattern + ADR-007 review-type half + corrections shape) | 0 (closeout-shape) |
| Walkthrough screen-fit (mandatory floor) | 6–7 (W-11..W-13c per SD-1 + SD-6) — one screen | 12–14 — two screens | 6–8 — borderline | 0 walkthroughs (FPs only) |
| Build-session context (new shapes / patterns / detectors) | `case/v1` shape + `case_management` pattern + FP-018 read-side | β = α + `review` shape family + `capture_with_review` pattern + corrections shape + first `type=review` emission discipline | `review` family + `capture_with_review` + corrections + ADR-007 review-type first-emission | pull-class doc + FP-018 read-side |
| First-exercise novelty under one tag | High but bounded | **Very high** — three first-exercises stacked | High | None (closeout) |
| **Pressure-test verdict** | **PASS** | **OVER-SIZED** | **NEAR-LIMIT (passes; but value-thin under FP-014 doc constraint)** | **UNDER-SIZED (not a Ship per § Rhythm)** |

β fails the same rule that produced the Ship-2 size-split decision (originally Ship-2 = S06+S06b+S08, split into Ship-2/3/4 because three ADR clusters cannot be retro-tested under one tag). Re-bundling under β would un-do that lesson without new evidence.

γ passes size but loses S08's natural §S4 first-exercise and FP-018 forcing. Under γ, ADR-005 §S4 stays `decided-unexercised` for one more Ship — every additional Ship under that condition deepens the parity-walk debt the closeout just paid down.

#### Recommendation summary (one paragraph)

**Ship-4 = (α) S08 only.** First-exercise ADR-005 §S4/§S5/§S6 R1+R5; first-exercise ADR-003 §S2 runtime-reassignment half; resolve FP-018 in full; author the FP-014 strategy doc covering all three pull-classes substantively, with case-bound first-exercised in tests and live-sync confirmed (historical-audit forward-cite-by-test, **complete-by-doc**). Ship-size verdict: **PASS** at C1-04 minimum + 1 case-handoff floor. Ship-map: **no shift** — Ship-5 (S04 + S11) absorbs review + multi-step approval + corrections (FP-005) as a coherent judgment cluster, which is where ADR-007 review-type first-emission and ADR-006 §S2 review-type discipline naturally land. The provisional `ship-4.md` § 1 scope choice (S08 only) is correct; what its §6 must change vs the prior draft is SD-5 (pull-class doc shape) and SD-6 (FP-018 explicit closure with W-13c offline-late case).

### Recommended Ship-map shift

**None.** The current map (Ship-4 = S08, Ship-5 = S04+S11) is coherent under α. No edit to `docs/ships/README.md` recommended. Recorded for transparency: had β been recommended, the map would shift to Ship-4 = S04+S08, Ship-5 = S11 — α makes that shift unnecessary.

### Does `docs/ships/ship-4.md` § 1 need rewriting?

**Partial yes.** § 1's scope ("state-progression half of S08") is correct under α and survives unchanged. What MUST be rewritten (or replaced when the user re-authors §6 from this draft) at lock:

- §3.1 R1–R5 should add **R6** for case-bound pull-class anchor under SD-5 (the SD-1 case-handoff requirement creates a new structural risk row).
- §5 R-4 sweep table row for FP-014 must change from "**FIRST-LOAD-DECISION-NEEDED** ... §6.5-defer" (pre-closeout framing) to "**RESOLVED IN-SHIP** by SD-5 strategy doc; case-bound class substantively first-exercised."
- §5 R-4 sweep table row for FP-018 must change from "carries forward" to "**RESOLVED IN-SHIP** by SD-6."
- §6.1 sub-decision 4 must reference SD-6's W-13c (offline-late `assignment_ended/v1`) explicitly.
- §6.4 must enumerate W-11..W-13c per SD-1's mandatory floor.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-0.**

---

## §2 SD-1 — Topology requirements (walkthrough floor)

**Architect's recommendation**: adopt **C1-04 minimum + 1 case-handoff requirement** as the Ship-4 mandatory walkthrough topology floor.

- **≥ 3 actors** (case opener; in-scope progressor; coordinator who emits the runtime `assignment_changed` pair)
- **≥ 3 villages** (so cross-village reassignment is geographically discriminable)
- **≥ 1 cross-village reassignment during the slice** (`assignment_ended/v1` + `assignment_created/v1` pair from V1 → V2)
- **≥ 1 case-handoff across the reassignment** (case opened by A in V1; `state=progressed` event from B after A is reassigned; B is in V1's then-active assignment set; **case-bound pull retrieves full timeline by SD-5 anchor**)

### What each requirement stress-tests

| Requirement | Invariant stressed | Cite |
|---|---|---|
| ≥ 3 actors | [P04](../behavioral_patterns.md) "transfer" variation | [C1-04](../reviews/system/domain.md) |
| ≥ 3 villages | [`ScopeResolver.activeGeographicScopes`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) under non-trivial geography | [C1-04](../reviews/system/domain.md) |
| ≥ 1 cross-village reassignment | [ADR-003 §S2](../adrs/adr-003-authorization-sync.md) runtime-reassignment + [FP-018](../flagged-positions.md#fp-018--assignment_endedv1-validated-but-never-consumed-in-scope-reconstruction) consumption | SD-6 below |
| ≥ 1 case-handoff across reassignment | [P04](../behavioral_patterns.md) transfer + [`access-control-scenario.md`](../access-control-scenario.md) "everything done during that period remains on record" + [S08](../scenarios/08-case-management.md) "responsibility for what happens next may shift from one person to another" + **SD-5 case-bound pull anchor** | parity walk §4 finding 4 |

### Stricter floor — adopted?

C1-04 minimum + 1 case-handoff is the **mandatory** floor. Optional **W-13b** (chained second reassignment — A→B→C handoff) recommended for evidence richness, not gated. The orchestrator's SD-6 below adds **W-13c (offline-late `assignment_ended/v1`)** — also mandatory, gated by FP-018's gate-3.

### Alternatives considered

- 2/2 (current `ship-4.md` draft): rejected — explicitly fails C1-04.
- 4/4: rejected — no invariant requires it.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-1.**

---

## §3 SD-2 — Case-as-subject `subject_ref.type` binding

**(In scope under α — case-management is delivered.)**

**Architect's recommendation**: **`subject_ref.type = "subject"` of the household** with an optional payload `case_id` (UUID) for forward-compatibility with multi-case-per-subject; for the Ship-4 slice, when omitted, the case is identified by `(subject_ref.id, activity_ref)` per [ADR-005 §S6 R1](../adrs/adr-005-state-progression.md#s6-pattern-composition-rules).

### What the envelope schema accepts

[`contracts/envelope.schema.json`](../../contracts/envelope.schema.json) closes `subject_ref.type` at four values: `subject`, `actor`, `assignment`, `process`. `process` is RESERVED with explicit "no current emission site" + "do not claim this identity category without a new IDR." The schema needs no change for either option (a) or (b).

### Why option (a) — household-as-subject + payload `case_id`

1. **The case IS about the household.** [ADR-008 §S1](../adrs/adr-008-envelope-reference-fields.md) defines `subject_ref` as "the entity this event is about." [P04](../behavioral_patterns.md) and [S08](../scenarios/08-case-management.md) anchor the case at the *subject* (household, person, asset).
2. **Per-request projection replay collapses the case timeline cleanly.** Filter events by `(subject_ref.id, shape_ref starts with case/, optionally activity_ref)`. No new identity category needed.
3. **§S6 R1 ambiguity resolved consistently.** Parity walk SD-4 flags an ambiguity in §S6 R1 ("subject-level" = envelope-`subject_ref` or pattern-logical-subject). Choosing (a) with one pattern per (subject, activity) picks pattern-logical consistently. Multi-case-per-subject is parked behind optional `case_id`.
4. **Lowest cost to defer.** Zero envelope change, zero schema migration, no new IDR. The `subject_type` CHECK constraint in [`V1__ship1_schema.sql`](../../server/src/main/resources/db/migration/V1__ship1_schema.sql) already accepts `subject`.

### What option (b) (`process`) would cost

A new IDR (envelope-doc text edit "process is reserved" → "process is the case-as-aggregate identity") + a pattern-aggregate-as-subject emission walkthrough + re-derivation of every per-subject query path. **Substantively a sub-Ship of its own** — adding a new identity category alongside Ship-4's state-progression first-exercise bundles two architectural innovations under one tag. **Reject** for Ship-4. Reserve `process` for the Ship that genuinely needs case-as-aggregate (multi-case-per-household + cross-subject case linking).

### §S exercised + parity status target

- ADR-005 §S5 (Pattern Registry — first exercise via `case_management` skeleton bound to `case/v1`): `decided-unexercised → exercised-met`.
- ADR-005 §S6 R1 (one subject-level pattern per activity): `decided-unexercised → exercised-met`.
- ADR-008 §S1 `subject_ref.type = "subject"`: `exercised-met` carries; `process` stays RESERVED.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-2.**

---

## §4 SD-3 — Pattern format (JAR-bundled)

**(In scope under α.)**

**Architect's recommendation**: **JSON resource**, sibling to JSON shape schemas. File: `server/src/main/resources/patterns/case_management.v1.json`, mirrored byte-identical at `contracts/patterns/case_management.v1.json` under FP-007's drift-gate.

```jsonc
{
  "name": "case_management",
  "version": 1,
  "subject_level": true,
  "states": ["open", "progressed", "resolved"],
  "initial_state": "open",
  "transitions": [
    {"from": "open",       "to": "progressed", "via_shape": "case/v1"},
    {"from": "progressed", "to": "progressed", "via_shape": "case/v1"},
    {"from": "open",       "to": "resolved",   "via_shape": "case/v1"},
    {"from": "progressed", "to": "resolved",   "via_shape": "case/v1"}
  ],
  "terminal_states": ["resolved"]
}
```

State value read from `payload.state` of the `case/v1` event.

### Justification (collapsed)

| Criterion | JSON | YAML | DSL | inline-Java |
|---|---|---|---|---|
| Coherence with JSON Schema discipline | ✅ same toolchain | ❌ adds parser | ❌ | ❌ Java-only — deployer surface impossible |
| Evolution profile under [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) | ✅ FP-016 regression-test path applies | partial | partial | ❌ |
| Diff/review ergonomics | ✅ | ✅ | ❌ | ❌ |
| Packaging — JAR-bundled today, runtime-loaded later (FP-012 closure) | ✅ unchanged when moved out | ✅ | partial | ❌ |
| FP-016 protection | ✅ extend drift-gate trivially | requires gate work | requires gate work | n/a |

### ADR-009 §S1 mechanism / instance duality

**Mechanism** (JSON format + evaluator) is platform-fixed PRIMITIVE. **Instances** (`case_management.v1.json`) are eventually deployer-configured CONFIG. F-C1 separation. Ship-4 lands the mechanism + the first instance, both JAR-bundled per the FP-012 expedient extension. When FP-012 closes, the instance file moves out of the JAR; format unchanged.

### FP-016 protection requirement

Add a pattern-fixture corollary to FP-016: at least one stored `case/v1` event sequence (open → progressed → resolved) replayed through the chosen pattern format must derive the expected terminal state. Cost: one fixture file + one assertion. Not a new FP — a Ship-4 test artifact.

### §S exercised + parity status target

- ADR-005 §S5: `decided-unexercised → exercised-met`.
- ADR-009 §S1 for `pattern` PRIMITIVE + `case_management.v1` CONFIG instance: duality rule already `exercised-met`; new ledger rows cite §S1 cleanly.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-3.**

---

## §5 SD-4 — ADR-005 §S transition table (Ship-4 retro deliverable)

**Architect's recommendation**: explicit table, not a single readiness item. Authored at retro into [`docs/reviews/system/adr-005-parity.md`](../reviews/system/adr-005-parity.md) per Rule R-7.

| §S | Pre-Ship-4 status | Post-Ship-4 target | Evidence at retro |
|---|---|---|---|
| §S1 (`transition_violation` flag category) | `decided-unexercised` | `decided-unexercised` | Mechanism present (pattern definition); no flag emission this Ship (sub-decision in §6.1 declines). Ship-5 first-exercise. |
| §S2 (flagged events excluded from state-machine eval) | `decided-unexercised` | `decided-unexercised` | No flag targets `case/v1` this Ship (no `transition_violation` emission; `scope_violation` rejects at push pre-persist; `identity_conflict` entry-guard reads `household_observation/` only). Latent-drift observation per parity §5 SD-1 re-recorded. |
| §S3 (resolvability classification) | `exercised-met` (partial) | `exercised-met` (partial) | No `auto_eligible` emission. Unchanged. |
| **§S4 (state-from-events projection)** | `decided-unexercised` | **`exercised-met`** | Case-state projection replays per request from `case/v1` events; W-12 evidence; no cache (FP-002 (a) precedent). |
| **§S5 (Pattern Registry)** | `decided-unexercised` | **`exercised-met`** | `case_management.v1.json` loaded; W-11 evidence. Deployer-authoring surface deferred (FP-012). |
| **§S6 R1 (one subject-level pattern per activity)** | `decided-unexercised` | **`exercised-met`** | Single binding: `case/v1` ↔ `case_management` ↔ activity. Trivially honored. |
| §S6 R2/R3/R4 | `decided-unexercised` | `decided-unexercised` | Ship-5 (R2/R3 review/approval) / Ship-6 (R4 cross-activity). |
| **§S6 R5 (shape-to-pattern uniqueness)** | `decided-unexercised` | **`exercised-met` (partial)** | Single binding honors the rule trivially. **Deploy-time validator unbuilt** — partial pending FP-012 closure. |
| §S7 (source-only flagging) | `exercised-met` (no-prop half) | `exercised-met` | Unchanged — no derived flag emission. |
| §S8 (`context.*` scope) | `decided-unexercised` | `decided-unexercised` | Ship-7. |
| §S9 (auto-resolution) | `decided-unexercised` | `decided-unexercised` | Ship-7. |

**Ship-4 cannot tag** with §S4, §S5, or §S6 R1 in any state other than `exercised-met`. §S6 R5's `(partial)` qualifier is acceptable per parity §4 finding 6 — fully met when FP-012 closes.

### Is §S2 unprovable in Ship-4? (escalation check)

No. §S2 is structurally provable in the Ship that first emits a flag against a state-bearing shape (Ship-5 review or Ship-7 reactive layer). Recording §S2 as latent at Ship-4 close is *not* a violation; recording it as `exercised-met` *would* be (false claim).

**User owns this. Accept / edit / reject for Ship-4 §6.SD-4.**

---

## §6 SD-5 — FP-014 resolution shape (pull-class strategy doc)

**Architect's recommendation**: **option (b) — strategy-level position document** at `docs/architecture/pull-class-temporal-anchors.md`. **Complete-by-doc, partial-by-test.** All three pull-classes covered substantively in §2 of the doc. Ship-4 walkthroughs exercise case-bound substantively + live-sync corroboration; historical-audit is forward-cite *by test only*. The doc itself cannot be partial — that would be Rule R-1 silent-deferral against FP-014 gate-1 ("disambiguates the temporal anchor by pull-class," not "by Ship-4-tested-class").

### Why this is the corrective vs the prior draft

The prior G-8 draft (`2b2ee9b`) wrote SD-5 as "case-bound substantively, live-sync + historical-audit forward-cite." That conflates *test coverage* with *doc completeness*. FP-014's gate-1 is satisfied by the architectural artifact (the doc), not by walkthroughs. The doc names all three classes as **first-class architectural concepts**, not as forward-pointing stubs. Ship-5+ promotes parity rows class-by-class without re-opening the doc.

### Why not (a) full ADR-003-R

ADR-003 §S2 ("sync scope = access scope") is **not contradicted** by class-disambiguation — the doc specifies *which time anchors which class's scope evaluation*, an under-specified detail. Per [`docs/convergence/supersede-rules.md`](../convergence/supersede-rules.md), supersession is required only when an ADR §S position changes. None changes here. ADR-003-R is heavier than the gap requires.

### Why not (c) defer

Ship-4's case-handoff topology (SD-1) requires case-bound semantics. Without disambiguation, the case timeline either:
- silently uses request-time scope ([`SyncController.pull` line 114](../../server/src/main/java/dev/datarun/ship1/sync/SyncController.java)) and excludes events authored by A pre-reassignment from B's view — **breaks SD-1**; or
- silently uses event-time and inherits the live-sync correctness regression FP-014 names.

**(c) is rejected.**

### Strategy doc — table of contents (what SD-5 commits Ship-4 to author)

`docs/architecture/pull-class-temporal-anchors.md`:

> **§1 Purpose & scope.** What this doc disambiguates; what §S of which ADRs it does NOT alter; relationship to FP-014.
>
> **§2 The three pull-classes** (each substantively, with definition + domain anchor + correctness requirement + test contract):
>
> > **§2.1 Live-sync pull (request-time-anchored).**
> > - Definition: device-online steady-state pull; scope evaluated as `actor.activeAssignments(now)`.
> > - Domain anchor: [`access-control-scenario.md`](../access-control-scenario.md) "Access can be temporary... when the reason for temporary access ends, the expanded access should end too."
> > - Endpoints in scope: [`SyncController.pull`](../../server/src/main/java/dev/datarun/ship1/sync/SyncController.java), [`ConfigController`](../../server/src/main/java/dev/datarun/ship1/config/ConfigController.java).
> > - Correctness requirement: revoked authority disappears from next pull; events authored under prior scope no longer visible to that actor unless reachable via another class.
> > - Test contract: actor reassigned away from V1 → next live-sync pull excludes new V1 events.
> > - Ship-4 status: `exercised-met` (already met since Ship-1; corroborated by W-15 out-of-scope rejection).
> >
> > **§2.2 Historical / audit pull (event-time-anchored).**
> > - Definition: an auditor reconstructs what an actor's scope authorized at event time; scope evaluated as `actor.activeAssignments(event.timestamp)`.
> > - Domain anchor: [`access-control-scenario.md`](../access-control-scenario.md) "every action attributable to a specific person acting in a specific role at a specific time" — audit ⇒ event-time reconstruction.
> > - Endpoints in scope: **none implemented in Ship-4.** Forward-pointed at S05 (auditor) — Ship-X.
> > - Correctness requirement: auditor pulling actor X's activity over [T1, T2] sees X's full event corpus *as authorized at each event's `event.timestamp`*, regardless of reassignments occurring in [T2, now].
> > - Test contract: forward-only — Ship that first implements an audit endpoint exercises this.
> > - Ship-4 status: `decided-unexercised`. Doc is complete; test transitions in a future Ship.
> >
> > **§2.3 Case-bound pull (subject-anchored).**
> > - Definition: an actor holding a case retrieves the case's full timeline; scope evaluated as `case.subject_ref` ∈ actor's case-holding set, not against geographic scope.
> > - Domain anchor: [S08](../scenarios/08-case-management.md) "Different people may be involved at different points... [the case] remains active and visible."
> > - Endpoints in scope: case-timeline projection (`/admin/cases/{id}` and equivalent admin/projection paths). NOT `/api/sync/pull` (that remains live-sync).
> > - Correctness requirement: the actor currently holding the case sees events authored by every prior holder *across* reassignments, including events whose authoring actor's geographic scope no longer covers the case's village.
> > - Test contract: Ship-4 W-13/W-13c successor — case opened by A in V1; A reassigned away; B picks up case in V1; B retrieves full timeline including A's pre-reassignment events.
> > - Ship-4 status: `decided-unexercised → exercised-met` (first exercise).
>
> **§3 Push path is unaffected.** [`ConflictDetector.java:67`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) preserves event-time anchor verbatim. Ship-4 makes no change.
>
> **§4 ADR-003 §S2 reading.** "Sync scope = access scope" reads as: *scope is what authorized the events visible at the appropriate anchor for the pull class*. No §S contradicted. Forward-references: §S2 sentence-2 commitment ("when scope changes (reassignment), the next sync adjusts the payload") is the live-sync class statement; the historical-audit and case-bound classes are under-specifications this doc fills, not contradictions.
>
> **§5 Why a strategy doc, not ADR-003-R.** [`docs/convergence/supersede-rules.md`](../convergence/supersede-rules.md) bar (no §S contradicted). The doc lives under `docs/architecture/`, signaling fill-in not architecture-grade decision.
>
> **§6 Future change shape.** If a class-anchor must change (e.g., case-bound becomes hybrid request-time-with-case-fallback), revise the doc. If a §S claim is contradicted, escalate to ADR-003-R.

### Proof no §S is altered

- §S2 ("sync scope = access scope"): untouched — class-disambiguation specifies *which time*, not *what scope is*.
- §S3 (push path uses event-time per `capture.timestamp()`): untouched — `ConflictDetector` line 67 preserved verbatim.
- §S4 (alias-respects-original-scope): untouched and inherited.
- §S7 (scope-type registry closed at 3): untouched.

### What this changes operationally in Ship-4

- **New test (case-bound first exercise)**: actor B reads `/admin/cases/{id}` after picking up the case from A; receives full timeline including A's pre-reassignment events.
- **W-15 (out-of-scope case-opening rejected)**: continues to use push-path (event-time) anchor — unchanged.
- **`/api/sync/pull` and `/api/sync/config`** (live-sync): continue request-time — unchanged.
- **`/admin/cases/{id}`** (case-bound): subject-anchored — new behavior, codifies what the case timeline already needs to do.

### Alternatives considered

- (a) ADR-003-R: rejected — heavier than gap, no §S contradiction.
- (c) Defer: rejected — case-bound is structurally required by SD-1.
- (d) Partial doc + forward-cite stubs: **rejected — this is what the prior G-8 draft did.** Rule R-1 silent-deferral.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-5.**

---

## §7 SD-6 — FP-018 resolution shape

**Architect's recommendation**: **resolve FP-018 in Ship-4 in full** by extending [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) to consume `assignment_ended/v1` events. Forced by SD-1's case-handoff requirement + the runtime-reassignment commitment.

### Full-or-none discipline

The orchestrator's hard constraint: half-reading the events is worse than none. If Ship-4 includes any discrete-event reassignment-end, FP-018 closes by `ScopeResolver` reading both `assignment_created/v1` AND `assignment_ended/v1` per gate item 1. Pre-set `valid_to` on the original event remains forbidden in Ship-4 production walkthroughs (append-only violation per [ADR-001 §S1](../adrs/adr-001-offline-data-model.md)).

### Gate items 1–3 mapped to Ship-4 walkthroughs

| Gate | Walkthrough |
|---|---|
| 1. `ScopeResolver` reads both `assignment_created/v1` AND `assignment_ended/v1`; composition rule documented in Javadoc | Code change in `ScopeResolver.activeAssignments` and `activeGeographicScopes`; Javadoc records: `assignment_ended/v1` with `target.actor_id = A`, `scope_ref = S`, `at = T_end` removes `(A, S)` from active set for any `at > T_end`. |
| 2. V1 active for A pre-T_end; V1 absent for A post-T_end | **W-13** (mandatory) — case-handoff temporal-correctness assertion. |
| 3. Offline-late `assignment_ended/v1`: captures with `event.timestamp ∈ (T_end, W)` against V1 fire `scope_violation` | **W-13c (NEW, mandatory)** — most stress-revealing case; closes FP-018's gate-3. |

### Why W-13c is mandatory, not optional

Without W-13c, `ScopeResolver` could be implemented to read only the `assignment_created/v1` plus a request-time `valid_to`-derived check that doesn't actually consult `assignment_ended/v1` events arriving offline-late. W-13c is the test that *cannot pass* under the half-implementation. It is therefore the gate-defining walkthrough, not a richness add-on.

### §S exercised + parity status target

- **ADR-003 §S2 (runtime-reassignment half)**: pre-Ship-4 `decided-unexercised`; post-Ship-4 `exercised-met`. Ship-4 retro authors `docs/reviews/system/adr-003-parity.md` (does not yet exist) per Rule R-7.
- **Push-path event-time anchor at [`ConflictDetector.java:67`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java)**: unchanged. SD-5 and SD-6 are orthogonal; both preserve push-path cleanness.

### Why not defer FP-018

[FP-017](../flagged-positions.md#fp-017--role_stale-detector-wiring-successor-to-fp-001) gate-4 names FP-018 as a prerequisite. Deferring here pushes the dependency into Ship-5 and bundles it with role-action enforcement Ship-5 already owns. Concentrating assignment-churn read-side work in Ship-4 is the cleaner shape.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-6.**

---

## §8 SD-7 — Composite coverage (H9) under chosen scope

**Architect's recommendation**: derived under α (S08-only).

| Composite | Bullet | Ship-4 classification | Evidence / rationale |
|---|---|---|---|
| [S20 — CHV field operations](../scenarios/20-chv-field-operations.md) | 1 (encounter individuals + document case details) | `not-applicable-to-this-slice` | S20 bullet 1 is captured (Ship-1). |
| | 2 (record outcomes — diagnosis, treatment) | `not-applicable-to-this-slice` | Ship-7 reactive / Ship-5 review territory. |
| | 3 (track supplies) | `carries-forward-to-Ship-6` | Resource distribution = Ship-6 (S07/S14). |
| | **4 (continuous activities — ongoing engagement, follow-ups, situations developing over time)** | **`exercised-by-Ship-4`** | The case lifecycle IS the continuous activity. W-11..W-14. First exercise of S20 bullet 4 in any Ship. |
| | **5 (history of what was done, when, by whom)** | **`exercised-by-Ship-4`** | Multi-actor case timeline (per-request projection replay; W-12 + W-13 + SD-1 case-handoff). Authorship across actors via case-bound pull (SD-5). |
| [S21 — CHV supervisor operations](../scenarios/21-chv-supervisor-operations.md) | 1–5 | `carries-forward-to-Ship-5` | Review pipeline = Ship-5 (S04). Ship-4 has no review primitive. |
| | Implicit (supervisor visibility into team workload) | **`partial — exercised-by-Ship-4`** | Coordinator authority + case-listing surface (which cases active per actor, queryable from projection). Approval/oversight = Ship-5. |
| [S05 — supervision/audit visits](../scenarios/05-supervision-audit-visits.md) | All bullets | `carries-forward-to-Ship-5+` | Reviewer-driven; no review pipeline lands until Ship-5. **Note**: S05's audit-visit half is the natural FP-014 historical-audit-class first-exerciser — Ship-X (when scheduled). |

S22 (`22-coordinated-distribution-campaign-across-grouped-locations.md`) is composite of S07/S09/S14 → Ship-6. Carry-forward only.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-7.**

---

## §9 NEW (S04 in scope?) — ADR-006 §S2 review-type first-exercise discipline

**Status under SD-0 = α**: **n/a if SD-0 = α (chosen)**. Ship-4 emits zero `type=review` envelopes. ADR-007 §S1's `review` half stays `decided-unexercised`. ADR-006 §S2 review-type discipline lands at the Ship that first emits `type=review` (Ship-5 under the current map).

If the user overrides SD-0 to **β** or **γ** at lock, this SD activates and must declare:

1. First `type=review` emission site (server-side path; system actor format `system:review_router/{policy}` or human reviewer's `actor_ref`).
2. Discrimination discipline: review/capture/alert authorship in `actor_ref`, never `type` (F-A3/F-A4).
3. Catalog row: `review` envelope-type half exits the closed-vocabulary debt at this Ship.
4. Parity transition target: ADR-007 §S1 `review` half `decided-unexercised → exercised-met`.

Recommendation if scope shifts: author this SD then; keep α now.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-9.**

---

## §10 R-7 parity transition table (Ship-4 retro deliverable)

Single-page summary the retro folds into the parity files. Ship-4 cannot tag with any `exercised-violated` row open.

| ADR | §S | Pre-Ship-4 | Post-Ship-4 | Parity file (R-7) |
|---|---|---|---|---|
| ADR-005 | §S1 | `decided-unexercised` | `decided-unexercised` | `adr-005-parity.md` (existing) |
| ADR-005 | §S2 | `decided-unexercised` | `decided-unexercised` (latent observation re-recorded) | `adr-005-parity.md` |
| ADR-005 | §S3 | `exercised-met` (partial) | `exercised-met` (partial) | `adr-005-parity.md` |
| **ADR-005** | **§S4** | `decided-unexercised` | **`exercised-met`** | `adr-005-parity.md` |
| **ADR-005** | **§S5** | `decided-unexercised` | **`exercised-met`** | `adr-005-parity.md` |
| **ADR-005** | **§S6 R1** | `decided-unexercised` | **`exercised-met`** | `adr-005-parity.md` |
| ADR-005 | §S6 R2/R3/R4 | `decided-unexercised` | `decided-unexercised` | `adr-005-parity.md` |
| **ADR-005** | **§S6 R5** | `decided-unexercised` | **`exercised-met` (partial)** | `adr-005-parity.md` |
| ADR-005 | §S7 | `exercised-met` | `exercised-met` | `adr-005-parity.md` |
| ADR-005 | §S8/§S9 | `decided-unexercised` | `decided-unexercised` | `adr-005-parity.md` |
| ADR-003 | §S2 (bootstrap) | `exercised-met` | `exercised-met` | **`adr-003-parity.md` (NEW — Ship-4 retro authors)** |
| **ADR-003** | **§S2 (runtime)** | `decided-unexercised` | **`exercised-met`** | `adr-003-parity.md` (NEW) |
| ADR-003 | §S3 (push-path event-time) | `exercised-met` | `exercised-met` (preserved verbatim under SD-5) | `adr-003-parity.md` (NEW) |
| ADR-003 | §S4 (alias-respects-scope) | `exercised-met` | `exercised-met` | `adr-003-parity.md` (NEW) |
| ADR-003 | §S7 (scope-type registry) | `exercised-met` | `exercised-met` | `adr-003-parity.md` (NEW) |
| ADR-007 | §S1 (`capture` etc.) | `exercised-met` | `exercised-met` | n/a (post-hoc parity walk in retro) |
| ADR-007 | §S1 (`review` half) | `decided-unexercised` | `decided-unexercised` (Ship-5) | n/a |
| ADR-009 | §S1 duality (`pattern` PRIMITIVE / `case_management.v1` CONFIG) | `exercised-met` (rule), new ledger rows | `exercised-met` | existing |

**Forbidden tag-blocking states**: any `exercised-violated`. None projected.

---

## §11 Ship-size pressure-test outcome

**Verdict: PASS** under (α).

| Signal | Reading | Threshold | OK? |
|---|---|---|---|
| ADR clusters first-exercised under one tag | 1 cluster (ADR-005 state-progression half) + 2 §S transitions on prior ADRs (ADR-003 §S2 runtime, FP-018) | ≤ 1 cluster + targeted §S transitions | ✅ |
| Walkthroughs (mandatory floor) | 6–7 (W-11, W-12, W-13, W-13c, W-14, W-15, W-16) | one screen | ✅ |
| New shapes / patterns | `case/v1` shape + `case_management.v1.json` pattern (mechanism + first instance, both JAR-bundled per FP-012 expedient) | bounded | ✅ |
| Detector / projector touches | `ScopeResolver` extension (FP-018); new case-state projector (per-request replay, no cache) | bounded | ✅ |
| FP closures | FP-014 (resolved by SD-5 strategy doc), FP-018 (resolved by SD-6) | concentrated, not bundled with new architecture | ✅ |
| R-7 parity files | adr-005-parity.md (update) + adr-003-parity.md (NEW) | typical retro work | ✅ |
| Build-session context fit | One architectural innovation (state-progression first-exercise) + targeted disambiguation (pull-class doc) | fits | ✅ |

**Optional richness adds (not gating)**: W-13b (chained second reassignment); pattern-fixture corollary fixture under `contracts/fixtures/patterns/` (FP-016 pre-pay).

If at lock the user expands scope to β, this verdict flips to **OVER-SIZED** — the explicit recommendation is to keep α and route review/approval/corrections to Ship-5 as the current map already does.

---

## Items the user should know but that are not SDs

1. **R-4 sweep against [`docs/flagged-positions.md`](../flagged-positions.md)** under α reduces to: FP-014 (closed by SD-5), FP-018 (closed by SD-6), FP-005/006/008/010/011/015 (carry forward), FP-012b/c/d/e (carry forward), FP-016 (advanced via SD-3 fixture corollary, not closed), FP-017 (FP-018 prerequisite now satisfied at Ship-4 close).
2. **`docs/reviews/system/adr-003-parity.md` does not yet exist.** Ship-4 is the first Ship to first-exercise an ADR-003 §S beyond bootstrap (§S2 runtime half). Per R-7, Ship-4 retro authors this parity walk file alongside the ADR-005 parity update. Acknowledged retro deliverable, not pre-spec blocker.
3. **No new ADR is recommended.** ADR-005 first-exercise lands without supersession (parity walk §6 verdict). FP-014's resolution is a strategy doc, not an ADR-R. SD-2 reserves `process` (no IDR — no first-emission). FP-018 fix is a code change, not an ADR change.
4. **Anomalies the prior draft missed (caught now)**:
   - *SD-5 framing*: prior draft conflated test coverage with doc completeness. Corrected: doc complete-by-doc, partial-by-test.
   - *W-13c*: prior draft treated the offline-late `assignment_ended/v1` walkthrough as "recommend adding"; under FP-018's full-or-none discipline, it is **mandatory**, not optional.
   - *§9 (ADR-006 §S2 review-type discipline)*: not previously surfaced; correctly n/a under α but flagged for Ship-5 trigger.
   - *adr-003-parity.md* authoring: prior draft mentioned the file does not exist but did not list it as a Ship-4 retro deliverable explicitly. Now listed in §10.
   - *Ship-map shift*: prior draft offered no recommendation; this draft explicitly says "no shift" under α and what would shift under β (transparency for the lock decision).

**End of architect's recommendation set. User owns each SD.**
