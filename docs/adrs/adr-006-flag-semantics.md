# ADR-006: Flag Semantics — Invariant Property, Algorithmic Procedure

> Status: **Decided**
> Date: 2026-04-22
> Convergence round: 1 (Phase 2 ADR #1)
> Upstream: ADR-001, ADR-002, ADR-002 Addendum
> Downstream: ADR-007 (will cite this for the integrity-shape invariant)
> Exploration: [Phase 0.5 harvest — Group 1](../convergence/inventory/disputes-harvest.md) · Raw: archive/05, archive/07, archive/09

---

## Context

Phase 0 inventory found three DISPUTED classifications on flag-related concepts:

- `accept-and-flag` — 3 sources read ALGORITHM, 1 read INVARIANT
- `flag` — sources split across DERIVED / INVARIANT / PRIMITIVE
- `flag-creation-location` — OPEN; server-vs-device question

The Phase 0.5 archive harvest (see [disputes-harvest.md §Group-1](../convergence/inventory/disputes-harvest.md)) found the dispute is not about what the platform does — it is about which facet of one underlying thing each classification captures. The platform never rejects events for state anomalies. That is a property; it does not change. The machinery that inspects events and raises flags is a procedure; it can evolve without changing the property. The disputes collapse once the two are separated.

ADR-002 S14 committed the property. ADR-002 Addendum §DD-3 made the flag identity deterministic. Phase 3e code wired four platform-bundled shapes into the integrity pipeline. What remained undocumented was the canonical split: **property vs. procedure**, and where each sits in the concept vocabulary. This ADR makes that split explicit.

---

## Decision

The platform distinguishes **three** load-bearing concepts around flags. Each gets a single canonical classification.

### S1: `accept-and-flag` is an INVARIANT

**A validly-structured event is never rejected for state-based reasons. State anomalies surface as flag events appended alongside the accepted event, never as rejections or modifications.**

This is a property of the write path. It holds for every event the platform accepts — identity-stale references, out-of-scope captures, role-stale actors, concurrent edits, and any future state-anomaly class all resolve to *accept + flag*, never to *reject*.

**Scope boundary.** The invariant is state-based, not structural. Structurally-malformed envelopes (missing required fields, wrong types, invalid UUIDs) are rejected at the envelope validator — that is the S9 contract-validation layer, not this invariant. ADR-001 drew this line; this ADR ratifies it.

**Consequence.** The detect-before-act guarantee (ADR-002 S12) is downstream of this invariant: because flagged events are accepted, some mechanism must withhold them from policy evaluation until resolved. That mechanism is algorithmic (§S3 below).

### S2: `flag` is an INVARIANT (as a class)

**Flags are the canonical representation of state anomalies on the event stream. Every state anomaly that the platform surfaces as a first-class record on the event stream does so as a flag event — no parallel anomaly-record surface exists or is permitted on the event stream.**

This ADR defines representation and emission. It does not override §S1 (accept-and-flag) and does not govern non-event-stream surfaces such as telemetry, metrics, or operational logs. §S1 governs *whether* anomalies enter the stream; §S2 governs *the shape they take when they do*.

The instances of the class are DERIVED (produced by the Conflict Detector, not authored by actors — see §S3). Individual flag categories — `scope_violation`, `temporal_authority_expired`, `role_stale`, `concurrent_state_change`, `stale_reference`, `domain_uniqueness_violation`, `transition_violation` — are CONFIG rows in the flag catalog. But the *class itself* — the commitment that *when anomalies are surfaced on the event stream, they take flag form, and no competing record-shape for the same purpose exists* — is INVARIANT.

**Why not PRIMITIVE.** A primitive is a load-bearing object the platform is built on (Event, Subject, Actor, Device). A flag is a kind of event, not a new primitive. It rides on the Event primitive.

**Why not DERIVED.** DERIVED is right for individual flag instances (computed from observed anomalies). It is wrong for the class, because the class itself is a promise about coverage, not a computed result.

### S3: `conflict-detection` is the ALGORITHM

**The procedure that inspects events against projected state and emits flags is algorithmic. It evolves without changing the invariants in S1 and S2.**

The server-side Conflict Detector (ADR-002 S13, implemented in `server/src/main/java/dev/datarun/server/integrity/ConflictDetector.java`) is the current implementation. Its shape is not protocol; it is mechanism. It can be extended (new detectors), relocated (device-side pre-flagging, see §S4), or re-implemented without violating S1 or S2.

The algorithm has named parts — evaluate-per-event, sweep, single-writer resolution — but these are ALGORITHM rows, not INVARIANT rows.

### S4: `flag-creation-location` — server-side by default, additively evolvable

**Flags are created server-side during sync processing. Device-side flag creation is not prohibited by the architecture; it is an additive evolution.**

Archive 07 §A1 raised the location question. Archive 09 B2 locked server-side as the initial answer because (a) the server has the full projected state needed for cross-device conflict detection and (b) the detect-before-act guarantee applies where policies execute, which is server-side. The architectural note `boundary.md §SG-1` already records this position; this ADR canonicalizes it.

This is not a knob for deployers. It is a platform implementation choice with an open door for additive evolution.

---

## Consequences

### Charter updates

- **Invariants** gain two rows: *accept-and-flag* (ADR-006 §S1) and *flags are the canonical event-stream representation of state anomalies* (ADR-006 §S2).
- **Primitives** gain no rows (flag is not a primitive — correction if any such row existed).
- **Cross-cutting rules** already cite accept-and-flag; cites get repointed to `ADR-006 §S1` (previously `[2-S14]`, which stays as the first-decision cite but is now dominated by this ADR's formal statement).

### Ledger updates (round 1)

| concept | was | becomes | settled-by | status |
|---|---|---|---|---|
| `accept-and-flag` | DISPUTED | INVARIANT | ADR-006 §S1 | PROPOSED |
| `flag` | DISPUTED | INVARIANT | ADR-006 §S2 | PROPOSED |
| `flag-creation-location` | OPEN | INVARIANT | ADR-006 §S4 | PROPOSED |
| `conflict-detection` | ALGORITHM | ALGORITHM (unchanged) | ADR-006 §S3 | PROPOSED |
| 7 flag-catalog rows (scope_violation, temporal_authority_expired, role_stale, concurrent_state_change, stale_reference, domain_uniqueness_violation, transition_violation) | FLAG | FLAG (unchanged) | ADR-006 §S2 | PROPOSED |

All rows go to PROPOSED, not STABLE. Per ledger rule 3, STABLE requires a full round in which neither the row nor any upstream it depends on changed. Round 1 is still in progress.

### Synonym merges

- `role-stale` and `role-staleness` — canonical: `role-stale`. `role-staleness` row will be marked OBSOLETE (duplicate) in the same commit.
- `scope-stale` and `scope-violation` — **kept distinct**. `scope-violation` is the flag category (in the catalog); `scope-stale` is the state predicate (an assignment has drifted out of scope). They are different concepts; the archive inventory conflated them.

### Supersessions

**None.** ADR-002 S14 made the first statement of this invariant and is not being superseded — it is being formally ratified and re-cited. The new cite `ADR-006 §S1` names the canonical form; `ADR-002 S14` remains accurate for the structural commitment in the identity model. Per [supersede-rules.md §"When supersession is optional"](../convergence/supersede-rules.md), no supersede is required when a new ADR refines orthogonal aspects without changing any decided position.

### Rejected alternatives

**Alt-1: classify `accept-and-flag` as ALGORITHM.** Rejected because the algorithm (Conflict Detector) is a current implementation; the invariant it enforces is the load-bearing claim. Future implementations will also uphold the invariant — that is the whole point of calling it an invariant.

**Alt-2: classify `flag` as PRIMITIVE.** Rejected per §S2 rationale — flag is a kind of event, not a new primitive.

**Alt-3: classify `flag` as DERIVED.** Rejected per §S2 rationale — DERIVED applies to instances, not to the class's coverage commitment.

**Alt-4: leave `flag-creation-location` as OPEN for a future IDR.** Rejected because the architecture note `boundary.md §SG-1` already committed the position. Leaving it open in the ledger creates drift exactly of the kind the convergence protocol exists to eliminate.

**Alt-5: phrase §S2 as "flags are the *only* surface for state anomalies."** Rejected as too strong. The earlier form made flags the single surface for anomalies globally, which would pre-commit against legitimate non-event-stream surfaces (metrics, telemetry, audit logs) and would leak into §S1's territory (the accept-vs-reject decision). The canonical wording scopes §S2 to event-stream representation only: when anomalies enter the event stream, flags are the shape; what happens off-stream is not §S2's concern. §S1 remains the sole authority on whether anomalies enter.

---

## Forward reference

ADR-007 (next in queue) will cite §S2 when canonicalizing the four platform-bundled integrity shapes (`conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`). The event-stream canonicality in §S2 is what makes those four shapes the *closed set* of integrity event names on the stream.
