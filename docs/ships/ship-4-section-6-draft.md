# Ship-4 §6 — Architect's Recommendation (G-8)

> **Type**: architect's recommendation set — **not** the Ship-4 spec.
> **Owner**: user (per H1, the spec author owns §6 lock).
> **Source dispatch**: Ship-3 closeout Wave 4 deferred item G-8 (see [`docs/ships/ship-3-retro.md`](ship-3-retro.md) §13 + session memory `ship-3-closeout-closed.md`).
> **Substrate**: [`docs/reviews/system/adr-005-parity.md`](../reviews/system/adr-005-parity.md) (G-4 parity walk) + the Ship-4 draft spec already opened at [`docs/ships/ship-4.md`](ship-4.md) (mechanical sections pre-filled; §3.2, §6, §6.4 still TODO).
> **Disposition**: each SD below is the architect's recommended answer with cited evidence and alternatives. The user reads, accepts / edits / rejects, then folds the chosen answers into the actual `docs/ships/ship-4.md` §6 at spec lock.
> **Out of bounds for this document**: scope/slice surface decisions (H8 delivery surface, scenarios delivered) — those are user-owned spec §1. Where this draft brushes surface, it restates the architectural shape and yields the surface call to the user.

---

## SD-1 — Topology requirements (walkthrough floor)

**Architect's recommendation**: adopt the **C1-04 minimum + 1 case-handoff requirement** as the Ship-4 walkthrough topology floor. The ship-4.md §6.4 walkthroughs (W-11..W-16 as drafted) currently use 2 actors / 2 villages. Lift to:

- **≥ 3 actors** (case opener; in-scope progressor; coordinator who emits the runtime `assignment_changed` pair; resolver may be one of the first two)
- **≥ 3 villages** (so cross-village reassignment is geographically discriminable, not the trivial "same set" case)
- **≥ 1 cross-village reassignment during the slice** (a coordinator emits an `assignment_ended/v1` + `assignment_created/v1` pair moving an actor from V1 to V2)
- **≥ 1 case-handoff across the reassignment** (a case opened by actor A in V1 receives a `state=progressed` event from actor B after A is reassigned away — the case persists across actor reassignment, B is in V1's currently-active assignment set)

### What each requirement stress-tests (load-bearing invariant cite)

| Requirement | Invariant stressed | Cite |
|---|---|---|
| ≥ 3 actors | [P04 Responsibility Binding](../behavioral_patterns.md) "transfer" variation; rules out two-actor degenerate case | [`docs/reviews/system/domain.md` C1-04](../reviews/system/domain.md) |
| ≥ 3 villages | [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) `activeGeographicScopes` under non-trivial geography; "overlapping historical assignments" path C1-04 names | [C1-04](../reviews/system/domain.md) |
| ≥ 1 cross-village reassignment | [ADR-003 §S2](../adrs/adr-003-authorization-sync.md) runtime-reassignment clause + [FP-018](../flagged-positions.md#fp-018--assignment_endedv1-validated-but-never-consumed-in-scope-reconstruction) `assignment_ended/v1` consumption gate | [ship-4.md §3.1 R2](ship-4.md) |
| ≥ 1 case-handoff across reassignment | [P04](../behavioral_patterns.md) "transfer" + [`access-control-scenario.md`](../access-control-scenario.md) "everything done during that period remains on record" + [S08](../scenarios/08-case-management.md) "Different people may be involved at different points. Responsibility for what happens next may shift from one person to another." | scenario prose + [parity walk](../reviews/system/adr-005-parity.md) §4 finding 4 (R1 case timeline rebuild) |

### Stricter floor — should Ship-4 adopt it?

S08 prose says situations resolve over **"days, weeks, or longer"** with **"different people involved at different points"** and **"responsibility for what happens next may shift from one person to another"**. The most realistic Ship-4 stress would be a case spanning **two reassignments** (open by A, A→B handoff, B→C handoff, C resolves) — exercising both an outbound and an inbound assignment-end on the same case. The C1-04 minimum is *one* reassignment.

**Architect's call**: stick with C1-04 minimum + 1 case-handoff for the **mandatory** floor. Adding a second reassignment exercises FP-018 once more without exercising any new invariant — diminishing returns at the cost of walkthrough length. Recommend an **optional W-13b** that chains a second reassignment for evidence richness, but do not gate the Ship on it. Do not relax C1-04's stated minimums.

### Alternatives considered

- **Stay at 2/2 (current draft)**: rejected — explicitly fails C1-04 by name; Ship-4 is the first Ship that materially exercises P04 and FP-018 and would compound the C1-04 fragility under build pressure.
- **Lift to 4 actors / 4 villages**: rejected — no invariant requires it; cost without signal.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-1.**

---

## SD-2 — Case-as-subject `subject_ref.type` binding

**Architect's recommendation**: **`subject_ref.type = "subject"` of the household** (option (a) in [parity walk §4 finding 1](../reviews/system/adr-005-parity.md#section-4--findings-for-ship-4-spec)). Add an **optional `case_id` (UUID) in payload** for forward-compatibility with multi-case-per-subject; for the Ship-4 slice, when omitted, the case is identified by `(subject_ref.id, activity_ref)` per [ADR-005 §S6 R1](../adrs/adr-005-state-progression.md#s6-pattern-composition-rules) — one subject-level pattern per (subject, activity).

### What `subject_ref.type` accepts today (envelope contract)

[`contracts/envelope.schema.json`](../../contracts/envelope.schema.json) defines `subject_ref.type` as a **closed enum of four values**: `subject`, `actor`, `assignment`, `process`. The schema description annotates `process` as **reserved** with **"no current emission site"** and explicitly cautions: *"Pattern instances in Phase 4 use (subject_ref, activity_ref) or source_event_id per docs/architecture/patterns.md, NOT 'process' refs — do not claim this identity category without a new IDR."*

So:
- The envelope schema does **not** need to change for either option (a) or option (b). The four values are already enumerated.
- Option (b) — `subject_ref.type = "process"` — is **gated by a new IDR** (envelope schema description text + a documented emission site). That is architecture-grade per [ADR-008 §S1](../adrs/adr-008-envelope-reference-fields.md#s1-subject-ref-envelope-field) / charter F-B2 ("never extend the `subject_ref.type` enum without an ADR" — analogically, never *first-emit* a reserved value without a documented decision).

### Why option (a)

1. **The case IS about the household.** [ADR-008 §S1](../adrs/adr-008-envelope-reference-fields.md) defines `subject_ref` as "the entity this event is about." [P04 Responsibility Binding](../behavioral_patterns.md) and [S08 prose](../scenarios/08-case-management.md) anchor the case at the *subject* (household, person, asset) — the case is a thread over events about the household. The household IS the natural subject; the case is a derived thread, not its own kind.
2. **Per-request projection replay (FP-002 (a) precedent) collapses the case timeline cleanly.** The case projection filters events by `(subject_ref.id, shape_ref starts with case/, optionally activity_ref)`. No new identity category needed.
3. **§S6 R1 ambiguity is resolvable in projection.** The parity walk SD-4 ([adr-005-parity.md §5 SD-4](../reviews/system/adr-005-parity.md#section-5--composition-rule-drift-signals)) flags an ambiguity in §S6 R1 — "subject-level" could mean envelope-`subject_ref` or pattern-logical-subject. Choosing (a) with **one pattern per (subject, activity)** picks the latter reading consistently. Multi-case-per-subject is parked behind the optional `case_id`; if/when a future Ship needs concurrent cases, `case_id` becomes payload-required and Rule 1's "subject-level" reads as (subject, activity, case_id). No envelope change ever.
4. **Lowest cost to defer.** Per [parity walk §4 finding 4–5](../reviews/system/adr-005-parity.md#section-4--findings-for-ship-4-spec): zero envelope change, zero schema migration, no new IDR. The `subject_type` CHECK constraint in [`V1__ship1_schema.sql`](../../server/src/main/resources/db/migration/V1__ship1_schema.sql) already accepts `subject`. Option (b) would require a new IDR + an envelope-doc edit + a first-emission walkthrough that proves the new identity category.

### What option (b) would cost

- A new IDR (envelope-doc text edit "process is reserved" → "process is the case-as-aggregate identity"; first-emission documented).
- A pattern-aggregate-as-subject emission walkthrough (case-subject UUID minted at case open; case-subject distinct from household-subject; household-subject reachable via payload back-reference).
- Re-derivation of every existing per-subject query path (admin views, scope checks) to recognise the new subject_type.
- It does *not* require a Flyway migration (the CHECK constraint already admits `process`).

That is in-scope-shaped work, but it is **substantively a sub-Ship of its own** — adding a new identity category alongside Ship-4's state-progression first-exercise bundles two architectural innovations under one tag. **Reject** option (b) for Ship-4. Reserve `process` for the Ship that genuinely needs case-as-aggregate (multi-case-per-household + cross-subject case linking).

### §S exercised + parity status target

- [ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry) (Pattern Registry — first exercise via `case_management` skeleton bound to `case/v1` shape): target `decided-unexercised → exercised-met`.
- [ADR-005 §S6 R1](../adrs/adr-005-state-progression.md#s6-pattern-composition-rules) (one subject-level pattern per activity — first exercise, trivially honoured): target `decided-unexercised → exercised-met`.
- [ADR-008 §S1](../adrs/adr-008-envelope-reference-fields.md#s1-subject-ref-envelope-field) `subject_ref.type = "subject"`: already `exercised-met`; remains so. `process` stays RESERVED.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-2.**

---

## SD-3 — JAR-bundled pattern format

**Architect's recommendation**: **JSON resource**, sibling to JSON shape schemas. File: `server/src/main/resources/patterns/case_management.v1.json`, mirrored byte-identical at `contracts/patterns/case_management.v1.json` under [FP-007](../flagged-positions.md#fp-007--contractserver-resource-shape-drift-not-enforced)'s drift-gate. Schema (informal, locked at retro):

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

The state value is read from `payload.state` of the `case/v1` event (matches ship-4.md §6.1 sub-decision 1).

### Justification

| Criterion | JSON | YAML | DSL | inline-Java |
|---|---|---|---|---|
| Coherence with existing JSON Schema discipline (envelope + shapes) | ✅ same toolchain | ❌ adds YAML parser | ❌ adds parser | ❌ Java-only; deployer surface impossible |
| Evolution profile under [ADR-004 §S10](../adrs/adr-004-configuration-boundary.md#s10-shape-definition-versioning-and-evolution) additive | ✅ JSON Schema regression test path is the same one [FP-016](../flagged-positions.md#fp-016--fixture-event-schema-regression-check-drift-gate-scope-expansion) already protects | partial | partial | ❌ Java diff has no FP-016 analogue |
| Developer ergonomics (review, diff, deployer authorship eventually) | ✅ readable, diffable | ✅ | ❌ requires DSL doc | ❌ |
| Packaging — JAR-bundled today, runtime-loaded later (FP-012 closure) | ✅ same artifact moves out of JAR unchanged | ✅ | partial | ❌ |
| [FP-016](../flagged-positions.md#fp-016--fixture-event-schema-regression-check-drift-gate-scope-expansion) protection against silent non-additive change | ✅ extend drift-gate check 5 to `contracts/patterns/` trivially | requires gate work | requires gate work | not applicable |

### [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) mechanism / instance duality

The **mechanism** (how patterns are expressed — the JSON format above; the platform's evaluator) is platform-fixed PRIMITIVE. The **instances** (specific named patterns: `case_management`, `capture_with_review`, etc. — the fixture file content) are eventually deployer-configured CONFIG. F-C1: the two are different ledger rows (parity walk §4 finding 6 already records this).

Ship-4 lands the mechanism (JSON parser + state-machine evaluator) **and** the first instance (`case_management.v1.json`). Both are JAR-bundled at this Ship per the FP-012 expedient extension (ship-4.md §6.1 sub-decision 2). When [FP-012](../flagged-positions.md#fp-012--deployer-authoring-surface-for-shapestriggerspolicies) closes, the instance file moves out of the JAR; the format is unchanged. This is the cheapest forward-evolution path.

### FP-016 protection requirement

[FP-016](../flagged-positions.md#fp-016--fixture-event-schema-regression-check-drift-gate-scope-expansion) gate item 1 calls for a fixture set under `contracts/fixtures/`. Ship-4 should **add a pattern-fixture corollary**: at least one stored case-event sequence (open → progressed → resolved) replayed through the chosen pattern format must derive the expected terminal state. This is the evidence that future non-additive pattern-format change cannot pass the gate silently. Cost: one fixture file + one assertion. Not an FP — a test artifact pre-built into Ship-4.

### Alternatives considered

- **YAML**: rejected — adds a parser dependency for cosmetic readability gain only.
- **Java-record / enum**: rejected — collapses the mechanism / instance duality (the fixture file becomes Java code; deployer surface is then unreachable without a refactor when FP-012 closes).
- **DSL**: rejected — too early; would need a grammar ADR.

### §S exercised + parity status target

- [ADR-005 §S5](../adrs/adr-005-state-progression.md#s5-pattern-registry): `decided-unexercised → exercised-met` (Pattern Registry mechanism + first instance shipped).
- [ADR-009 §S1](../adrs/adr-009-platform-fixed-vs-deployer-configured.md#s1-duality-rule-charter-invariant) for `pattern` (mechanism PRIMITIVE) + `case_management.v1` (instance CONFIG): already `exercised-met` for the duality rule; the new ledger rows cite §S1 cleanly.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-3.**

---

## SD-4 — ADR-005 §S2 readiness gap

**Architect's recommendation**: **Ship-4 does NOT first-exercise §S2.** The ship-4.md draft sub-decision 6 already declines `transition_violation` emission. §S2 (flagged events excluded from state-machine projection) is therefore not exercised this Ship and remains `decided-unexercised` at Ship-4 close. This is acceptable per R-7 (no `exercised-violated` row blocks tag; `decided-unexercised` rows do not). **No escalation.**

### What §S2 says (verbatim cite)

[ADR-005 §S2](../adrs/adr-005-state-progression.md#s2-flagged-events-excluded-from-state-machine-evaluation): *"Events carrying unresolved flags are excluded from state machine evaluation in the projection engine. They appear in the event timeline but do not change `current_state`."*

### Why Ship-4 does not exercise it

Ship-4 emits no flag whose source is a `case/v1` event:
- No `transition_violation` (sub-decision 6 declines emission).
- No `scope_violation` against `case/v1` triggers because the W-15 walkthrough rejects out-of-scope case-opening at the push entry (no event lands; nothing to flag downstream).
- No `identity_conflict` against `case/v1` — the [`ConflictDetector`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) entry-guard at line 60 reads `shape_ref.startsWith("household_observation/")` only.

So no flagged `case/v1` event ever enters the case-state projector. §S2's exclusion path is not authored. This is the latent-drift risk the parity walk §5 SD-1 names — the projector folds events with no flag-status check, vacuously §S2-clean. Ship-4 inherits the same vacuity and adds one more projector to the count.

### What Ship-4 must do at retro (R-7 deliverable)

The [`docs/reviews/system/adr-005-parity.md`](../reviews/system/adr-005-parity.md) file is the substrate. Ship-4 retro updates it to reflect:

| §S | Pre-Ship-4 status | Post-Ship-4 status |
|---|---|---|
| §S1 | `decided-unexercised` | `decided-unexercised` (mechanism present per draft §2 row; no flag emitted; no transition) |
| §S2 | `decided-unexercised` | `decided-unexercised` (unchanged; latent-drift observation re-recorded) |
| §S3 | `exercised-met` (partial) | `exercised-met` (partial) (no change — no `auto_eligible` emission) |
| **§S4** | `decided-unexercised` | **`exercised-met`** (case-state projection replays per request; W-12 evidence) |
| **§S5** | `decided-unexercised` | **`exercised-met`** (Pattern Registry + `case_management.v1.json` loaded; W-11 evidence) |
| **§S6 R1** | `decided-unexercised` | **`exercised-met`** (one subject-level pattern per activity, trivially) |
| **§S6 R5** | `decided-unexercised` | **`exercised-met`** (single shape-to-pattern binding; deploy-time validator unbuilt per FP-012 — record as partial) |
| §S6 R2/R3/R4 | `decided-unexercised` | `decided-unexercised` (Ship-5/6) |
| §S7 | `exercised-met` (no-propagation) | `exercised-met` (no change) |
| §S8 | `decided-unexercised` | `decided-unexercised` (Ship-7) |
| §S9 | `decided-unexercised` | `decided-unexercised` (Ship-7) |

Ship-4 **cannot tag** with §S4, §S5, or §S6 R1+R5 in any state other than `exercised-met`. Walkthroughs (W-11..W-16 from the draft) provide the evidence for each.

### Is §S2 unprovable in Ship-4? (escalation check)

No. §S2 is **deferred-by-design** for Ship-4 and structurally provable in the Ship that first emits a flag against a state-bearing shape (Ship-5 review or Ship-7 reactive layer). Recording §S2 as latent at Ship-4 close is *not* a violation; recording it as `exercised-met` *would* be (false claim). The latent-drift observation already lives in [parity walk §5 SD-1](../reviews/system/adr-005-parity.md#section-5--composition-rule-drift-signals) — Ship-4 retro re-affirms it; no FP needed unless Ship-4 implementation introduces a new flag-bearing path the orchestrator did not anticipate.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-4.**

---

## SD-5 — FP-014 resolution shape

**Architect's recommendation**: **option (b) — strategy-level position document** at `docs/architecture/pull-class-temporal-anchors.md`. ADR-003 §S is **not modified**; the document disambiguates which time anchors scope evaluation per pull-class without altering the §S2 commitment that "sync scope = access scope." The push path stays untouched.

### Why not (a) full ADR-003-R

ADR-003 §S2 says "sync scope = access scope." The disambiguation Ship-4 needs ("which time anchors the scope eval per pull-class") **does not contradict §S2** — it specifies an under-specified detail. Per [`docs/convergence/supersede-rules.md`](../convergence/supersede-rules.md), supersession is required only when an ADR §S position changes. None changes here. ADR-003-R is heavier than the gap requires.

### Why not (c) defer

The Ship-4 draft §1 commits to delivering the **case-bound pull** (case timeline retrieval — W-12 admin view, W-13 case ownership history, W-16 "waiting too long" query). All three are case-bound pulls — the response set is anchored to the case-subject's persisting identity, not to the requesting actor's request-time scope. Without disambiguation, Ship-4's case timeline either:
- silently uses request-time scope (current `SyncController.pull` behavior at line 114) and excludes events from before the actor's current assignment — **breaks the case-handoff requirement of SD-1** (actor B cannot see A's events prior to reassignment); or
- silently uses event-time scope and inherits the live-sync correctness regression that FP-014's review prose names.

Ship-4 cannot ship correctness against the SD-1 case-handoff topology without disambiguation. **(c) is rejected.**

### What the strategy doc must say

[FP-014 gate item 1](../flagged-positions.md#fp-014--scope-eval-pull-class-temporal-anchor-disambiguation) requires:

| Pull-class | Anchor | Cite |
|---|---|---|
| Live-sync pull (steady-state device pull) | request-time | [`access-control-scenario.md`](../access-control-scenario.md) "Access can be temporary... when the reason for temporary access ends, the expanded access should end too." |
| Historical / audit pull | event-time | same scenario, "every action attributable to a specific person acting in a specific role at a specific time" |
| **Case-bound pull (Ship-4 first exercise)** | **subject-anchored** (the case's persisting `subject_ref.id`; events authorized at event-time per [ADR-003 §S4](../adrs/adr-003-authorization-sync.md) when initially captured remain visible to actors holding the case at pull time) | [S08](../scenarios/08-case-management.md) prose: "Different people may be involved at different points... [the case] remains active and visible." |

Each pull endpoint is named by class. Ship-4 routes the case-bound pull explicitly (e.g., `/admin/cases/{id}` is case-bound; `/api/sync/pull` remains live-sync; no historical-pull endpoint lands this Ship — that's [S04](../scenarios/04-supervisor-review.md) Ship-5).

### Proof no §S is altered

- §S2 ("sync scope = access scope"): untouched. The doc's disambiguation says *which time* anchors scope; it does not redefine what scope IS.
- §S3 (push path uses event-time / `capture.timestamp()`): untouched. [`ConflictDetector.java:67`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) is preserved verbatim — explicitly a Ship-4 non-change.
- §S4 (alias-respects-original-scope): untouched and inherited.
- §S7 (scope-type registry closed at 3): untouched.

The doc lives under `docs/architecture/`, not `docs/adrs/`, signalling it is an under-specification fill-in not an ADR-grade decision. Charter is regenerated; drift gate stays PASS (no ledger row promoted by the doc alone — only the case-bound-pull *implementation* in Ship-4 promotes anything).

### What this changes operationally in Ship-4

- A new test asserts case-bound-pull anchor: actor B opens `/admin/cases/{case_id}` after being reassigned onto the case-subject (W-13 successor); receives the full timeline including events authored by A pre-reassignment.
- W-15 (out-of-scope case opening rejected) continues to use push-path (event-time) anchor — unchanged.
- `/api/sync/pull` (live-sync) continues to use request-time — unchanged.
- The push path and live-sync pull are explicitly listed as "not changed by this disambiguation" in the doc.

### Alternatives considered

- **(a) ADR-003-R**: rejected — heavier than gap, no §S contradiction. Reserve for the Ship that materially supersedes a §S (e.g., when historical-pull lands at Ship-5 with a §S4 alias-handling extension).
- **(c) Defer**: rejected — case-bound pull is structurally required by Ship-4 walkthroughs.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-5.**

---

## SD-6 — FP-018 resolution shape

**Architect's recommendation**: **resolve FP-018 in Ship-4** by extending [`ScopeResolver`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) to consume `assignment_ended/v1` events. This is forced by the ship-4.md draft sub-decision 4 (coordinator-emitted `assignment_ended/v1` + `assignment_created/v1` pair atomically at runtime via [`ServerEmission`](../../server/src/main/java/dev/datarun/ship1/sync/ServerEmission.java)). The pre-set `valid_to` workaround is an append-only violation per [ADR-001 §S1](../adrs/adr-001-offline-data-model.md#s1) and must not leak into Ship-4 walkthroughs.

### Why discrete-event reassignment-end is required by Ship-4

Ship-4 §6.1 sub-decision 4 (orchestrator pre-fill) and SD-1 above commit to runtime reassignment via the discrete `assignment_ended/v1` + `assignment_created/v1` pair. This is the cleanest path:

- Append-only honoured: the original `assignment_created/v1` event is never modified.
- The reassignment is itself an event with a `device_id`/`device_seq` pair — same shape as every other server-emitted event ([Ship-1 W-1 / W-2 / Ship-2 W-3](ship-1.md) precedent).
- [`ScopeResolver.activeAssignments(actorId, at)`](../../server/src/main/java/dev/datarun/ship1/scope/ScopeResolver.java) already replays via event-time-as-of-T; consuming `assignment_ended/v1` is the missing read-side half.

The pre-set `valid_to` shortcut Wave-1 G-7' used for tests at Ship-3 close is **not** allowed in Ship-4 production walkthroughs — using it would (a) require the coordinator to mutate the original event (forbidden) or (b) require the coordinator to know `valid_to` at creation time (defeats the runtime-reassignment scenario S08 names).

### FP-018 gate items 1–3 resolved by Ship-4

[FP-018 gate](../flagged-positions.md#fp-018--assignment_endedv1-validated-but-never-consumed-in-scope-reconstruction):

| Gate | Ship-4 walkthrough |
|---|---|
| 1. `ScopeResolver` reads both `assignment_created/v1` AND `assignment_ended/v1`; composition rule documented | code change in `ScopeResolver.activeAssignments`; Javadoc records the rule |
| 2. Test: V1 active for A pre-T_end; V1 absent for A post-T_end | W-13 (mandatory) + temporal-correctness assertion |
| 3. Offline-late `assignment_ended/v1`: captures with `event.timestamp ∈ (T_end, W)` against V1 fire `scope_violation` | W-13 successor or new W-13c — **recommend adding** |

**Architect's note**: gate item 3 (the offline-late `assignment_ended` case) is the most stress-revealing. The current draft W-11..W-16 does not explicitly cover offline-late assignment-end. **Recommend adding W-13c** (offline late assignment-end ⇒ `scope_violation` on intervening capture) — small extension; high signal; closes FP-018 cleanly.

### Why not defer FP-018

If Ship-4 deferred FP-018 and reverted to the `valid_to` workaround, the walkthroughs would silently rely on an unbuilt path. [FP-017](../flagged-positions.md#fp-017--role_stale-detector-wiring-successor-to-fp-001) gate item 4 explicitly names FP-018 as a prerequisite — deferring here pushes the same dependency into Ship-5 and bundles it with the role-action enforcement Ship-5 already owns. Concentrating the assignment-churn read-side work in Ship-4 is the simpler shape.

### §S exercised + parity status target

- [ADR-003 §S2](../adrs/adr-003-authorization-sync.md#s2) (sync scope = access scope under runtime mutation): **first runtime exercise**. Pre-Ship-4 status: `exercised-met` for bootstrap-only; post-Ship-4 status: `exercised-met` for runtime-reassignment as well. Ship-4 retro updates `docs/reviews/system/adr-003-parity.md` (currently does not exist — Ship-4 retro authors it under R-7).
- The push-path event-time anchor at [`ConflictDetector.java:67`](../../server/src/main/java/dev/datarun/ship1/integrity/ConflictDetector.java) **does NOT change**. SD-5 and SD-6 are orthogonal; both preserve the push-path cleanness.

**User owns this. Accept / edit / reject for Ship-4 §6.SD-6.**

---

## SD-7 — Composite-scenario coverage (H9 declaration)

**Architect's recommendation**: per the search of `docs/scenarios/` (composites are S05, S20, S21 — S22 was renamed to "coordinated-distribution-campaign" in the 2026-04-23 sweep and is no longer a composite for the current Ship-4 slice; verified by `list_dir docs/scenarios/`), the H9 table is:

| Composite | Bullet | Ship-4 classification | Evidence / rationale |
|---|---|---|---|
| [S20 — CHV field operations](../scenarios/20-chv-field-operations.md) | Bullet 1 (encounter individuals + document case details) | `not-applicable-to-this-slice` | S20 bullet 1 is captured, not S08 case-progression. Ship-1 already exercised the capture surface. |
| | Bullet 2 (record outcomes such as diagnosis and treatment) | `not-applicable-to-this-slice` | Ship-7 reactive layer / Ship-5 review territory. |
| | Bullet 3 (track supplies) | `carries-forward-to-Ship-6` | Resource distribution = Ship-6 (S07 / S14). |
| | **Bullet 4 (continuous activities — ongoing engagement, follow-ups, situations developing over time)** | **`exercised-by-Ship-4`** | The case lifecycle IS the continuous activity. Stressed via W-11..W-14 (open → progressed → resolved). First exercise of S20 bullet 4 in any Ship. |
| | **Bullet 5 (history of what was done, when, and by whom)** | **`exercised-by-Ship-4`** | Multi-actor case timeline (per-request projection replay; W-12 + W-13 + the case-handoff requirement in SD-1) renders authorship across actors. Additional stress beyond Ship-3 (which exercised v1+v2 timeline rendering). |
| [S21 — CHV supervisor operations](../scenarios/21-chv-supervisor-operations.md) | Bullet 1 (supervisor meets with a specific volunteer) | `carries-forward-to-Ship-5` | Review pipeline = Ship-5 (S04). Ship-4 has no review primitive. |
| | Bullet 2 (review aspects of work using criteria) | `carries-forward-to-Ship-5` | Same. |
| | Bullet 3 (observe / verify / question) | `carries-forward-to-Ship-5` | Same. |
| | Bullet 4 (document observations) | `carries-forward-to-Ship-5` | Same. |
| | Bullet 5 (identify gaps / inconsistencies) | `carries-forward-to-Ship-5` | Same. |
| | **Implicit: supervisor visibility into team workload** | **`partial — exercised-by-Ship-4`** | Coordinator authority + case-listing surface (which cases are active per actor, queryable from the projection) gives partial team-workload visibility. Ship-4's case-listing admin view (W-12 / W-16) is the load-bearing artifact. Approval / corrective oversight = Ship-5. |
| [S05 — supervision / audit visits](../scenarios/05-supervision-audit-visits.md) | All bullets | `carries-forward-to-Ship-5` | S05 is reviewer-driven; no review pipeline lands until Ship-5. The Ship-4 draft H9 table records "all S05 surface remains 0% covered" — confirmed. |

**None of S05's bullets are exercised by Ship-4.** This is valid per H9 — "None" classifications are explicit, not absent.

### Files searched (H9 transparency)

`list_dir docs/scenarios/` returned 22 scenario files plus `README.md`. Composite scenarios identified: S05, S20, S21. (S22 — `22-coordinated-distribution-campaign-across-grouped-locations.md` — is a *composite* of S07/S09/S14 per its file path, and is Ship-6 territory; recorded as carries-forward to Ship-6 implicitly via the Ship-4 draft §6.5 deferral list and not unpacked here.)

**User owns this. Accept / edit / reject for Ship-4 §6.SD-7.**

---

## Pressure-test summary (H1, H8, H9, F-A2, R-7)

| Rule | Check | Result |
|---|---|---|
| **H1** | Does the recommendation set let the user write §6 without re-discovery? | Yes. Each SD names the §S exercised, the parity transition target, and the cite chain. SD-1 (topology) + SD-6 (FP-018) materially extend the draft walkthrough set; SD-2 / SD-3 lock the shape and pattern artifacts; SD-5 names the strategy doc. SD-4 records the §S2 deferral as not-an-escalation. |
| **H8** | Does any SD touch delivery surface? | SD-1 specifies walkthrough topology — that is acceptance-test substance, not delivery surface. Delivery surface (HTTP simulation vs Flutter) remains user-owned and unchanged from ship-4.md §1 ("scripted multi-actor HTTP simulation against the real server"). No SD crosses into surface decisions. |
| **H9** | SD-7 enumerates composites with file paths? | Yes. S05, S20, S21 enumerated bullet-by-bullet; S22 carry-forward noted. `list_dir docs/scenarios/` searched; 22 files plus README confirmed. |
| **F-A2 / F-A4** | Any flag-emission discrimination on envelope `type`? | Ship-4 emits no new flag categories. Where flag emission appears (only in inherited paths — `ConflictDetector` `scope_violation` on push), discrimination remains on `shape_ref.startsWith("household_observation/")` per current code. No SD authors a `if (type == "...")` branch. SD-3 case-management state machine discriminates on `shape_ref` ("via_shape": "case/v1") never on envelope `type`. Clean. |
| **R-7** | Does each SD name §S + parity status target? | SD-2: §S5 + §S6 R1 → `exercised-met`. SD-3: §S5 + ADR-009 §S1 → `exercised-met`. SD-4: enumerates the §S parity table for retro. SD-5: §S2 / §S3 / §S4 / §S7 of ADR-003 — proven non-altered by the strategy doc; §S2 promoted to `exercised-met` for the runtime-mutation half. SD-6: ADR-003 §S2 runtime-mutation → `exercised-met`. Ship-4 retro deliverables are explicit per SD. |

---

## Items the user should know but that are not SDs

1. **The R-4 sweep referenced in session memory is now feasible.** With SD-1..SD-7 in hand, the R-4 sweep against [`docs/flagged-positions.md`](../flagged-positions.md) for FPs whose `Blocks:` field names Ship-4 reduces to: FP-014 (resolved by SD-5), FP-018 (resolved by SD-6), FP-005 / FP-006 / FP-008 (deferred by ship-4.md draft §5 with rationale; carries forward), FP-015 (deferred — Ship-4 does not touch config publication; carries forward), FP-016 (extended trivially by SD-3 pattern-fixture corollary; not closed but advanced). FP-017 carries to Ship-5 with FP-018 as prerequisite now satisfied at Ship-4 close.
2. **`docs/reviews/system/adr-003-parity.md` does not yet exist.** Ship-4 will be the first Ship to first-exercise an ADR-003 §S beyond its bootstrap case (§S2 runtime-mutation half). Per R-7, Ship-4 retro authors this parity walk file alongside the ADR-005 parity update. Acknowledged as a Ship-4 retro deliverable, not a pre-spec blocker.
3. **No new ADR is recommended.** The ADR-005 first-exercise lands without supersession (parity walk §6 verdict). FP-014's resolution is a strategy doc, not an ADR-R. SD-2 reserves `process` (no IDR needed because no first-emission). The Ship retro may discover otherwise, but that is the point of retros.
