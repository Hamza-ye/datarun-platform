# ADR-006-R: Flag Semantics — Alias-Cycle Prevention as a Push-Path Guard

> Status: **DECIDED**
> Date: 2026-04-27
> Supersedes: [ADR-006](adr-006-flag-semantics.md) (full supersede; §S1–§S4 carried verbatim by reference; one new position §S5)
> Upstream: ADR-001, ADR-002, ADR-002 Addendum, ADR-007, ADR-008
> Downstream: none at decide time
> Trigger: Ship-3 closeout Wave 3. Reviewer findings SC-07 ("Alias cycle detection absent; append-only precludes remediation") and C2-03 ("Alias cycle constructible offline; no prevention in merge guard") in [`docs/reviews/system/architect.md`](../reviews/system/architect.md), routed via [`docs/reviews/system/recovery-plan.md`](../reviews/system/recovery-plan.md) §3 step 5.

---

## Context

ADR-006 settled the canonical split between flag-as-property (INVARIANT, §S1/§S2) and conflict-detection-as-procedure (ALGORITHM, §S3) and named the eight active flag categories with one reserved growth slot. ADR-002 §S6 settled alias semantics for `subjects_merged/v1` (`retired_id → surviving_id`). ADR-002 §S9 stated lineage acyclicity *by construction*, justified by §S10's commitment that merge and split execute only through server-validated transactions.

A subsequent system review surfaced that the by-construction claim has no enforcing mechanism. §S10 routes merge/split through the server but no server-side procedure inspects the proposed alias edge against the existing alias graph for cycle closure. Because the alias graph is a directed graph over subject UUIDs and the platform accepts merge events from the field, two coordinators acting on offline-divergent views of the alias graph can each commit a merge that, taken together, closes a cycle. Once committed, the cycle is permanent — ADR-001 §S1 forbids deletion. Identity resolution that traverses the alias graph becomes ill-defined (canonical-form lookup loops).

The reviewer's proposed remedy was to reject the cycle-closing event at push time. That remedy contradicts ADR-006 §S1 (accept-and-flag): a validly-structured event is never rejected for state-based reasons, including state-of-the-alias-graph reasons. The architectural reconciliation is to keep accept-and-flag whole and surface the anomaly as a flag — extending the catalog with a ninth category, `cycle_violation`. That is the single new position this ADR makes.

ADR-006-R does not change ADR-001 (events containing cycles remain permanent), ADR-007 (the flag rides envelope `type=alert` per §S1), or ADR-008 (system author format unchanged). It also does not retract ADR-002 §S9: the lineage graph remains acyclic *as the platform's commitment*, but the commitment is now surfaced through accept-and-flag rather than carried as an unenforced "by construction" claim.

---

## Decision

### S1 — `accept-and-flag` is an INVARIANT

Carried verbatim from [ADR-006 §S1](adr-006-flag-semantics.md#s1-accept-and-flag-is-an-invariant). No change.

### S2 — `flag` is an INVARIANT (as a class)

Carried verbatim from [ADR-006 §S2](adr-006-flag-semantics.md#s2-flag-is-an-invariant-as-a-class). The catalog grows by one row at this ADR (see §S5 below); the §S2 commitment that flags are the canonical event-stream representation of state anomalies is unchanged.

### S3 — `conflict-detection` is the ALGORITHM

Carried verbatim from [ADR-006 §S3](adr-006-flag-semantics.md#s3-conflict-detection-is-the-algorithm). The cycle-detection procedure introduced in §S5 is one further realization of the algorithm class — extension is its own normal mode (§S3 second paragraph).

### S4 — `flag-creation-location` — server-side by default, additively evolvable

Carried verbatim from [ADR-006 §S4](adr-006-flag-semantics.md#s4-flag-creation-location--server-side-by-default-additively-evolvable). The cycle-detection procedure is server-side per §S4; this is consistent because the cycle predicate requires the full alias graph, which is server-side state.

### S5 — Alias-cycle prevention as a push-path guard with `cycle_violation` flag

**The platform commits to surfacing alias-cycle closure as a flag event with `payload.flag_category = "cycle_violation"`. The detection runs on the push path, before persistence, against the union of (a) the persisted alias graph and (b) earlier-in-batch alias-introducing events from the same push request. The cycle-closing event is still accepted (§S1 accept-and-flag is preserved); the flag is emitted alongside it.**

Five sub-commitments:

#### S5.1 Push-path placement

Cycle detection runs on the push pipeline after envelope and shape-payload validation succeed and before the cycle-introducing event is persisted to the event store. The guard inspects only events whose `shape_ref` introduces an alias edge — currently `subjects_merged/v1` and `subject_split/v1` (the platform-bundled identity-lifecycle shapes per [ADR-007 §S2](adr-007-envelope-type-closure.md)). Non-alias events traverse the push path unchanged.

The guard operates on a domain abstraction — a directed graph over subject identifiers — built by replaying alias-introducing events. The procedural realization of this graph (in-memory build per request, materialized projection, or otherwise) is ALGORITHM territory (§S3) and may evolve.

#### S5.2 Batch-serial semantics

Within a single push request, cycle-introducing events are processed in array order. The graph state visible to event N includes all alias edges from events 0..N-1 of the same batch that have already been accepted by the validation phase. This is the meaningful difference from the existing post-persist per-event detection pattern: the cycle predicate is sensitive to in-flight unpersisted edges, because two cycle-closing edges can arrive in the same push and neither is visible to the other under a strict pre-persist or strict post-persist model alone.

Batch-serial detection is the deterministic minimum that does not depend on push-ordering between batches and does not require cross-request coordination.

#### S5.3 Manual-resolution-only

The catalog row added by this ADR is `manual_only` per §S2 conventions. The platform does not auto-resolve cycles. A cycle is corrected only by a human operator emitting a `conflict_resolved/v1` event (out of scope at this ADR's decide time). Until resolved, identity resolution that would traverse the cycle is undefined; readers may surface a cycle-present marker but this ADR does not commit to read-side semantics.

#### S5.4 Request-time anchor

Cycle detection runs against the alias graph as of *request time*, not as of the cycle-introducing event's `event.timestamp`. Rationale: alias edges are administrative claims about identity, not field-time observations of the world. There is no "this household *was* the same as that household as of T_C" semantic — alias edges are present-tense graph mutations applied to a present-tense graph. The relevant graph for cycle reachability is the platform's current alias graph, not the alias graph as it stood when the field event was authored.

This anchor is intentionally different from the event-time anchor used for `scope_violation` (per [ADR-003 §S3](adr-003-authorization-sync.md), reinforced by [FP-014](../flagged-positions.md)). The contrast is principled: pull-class temporal anchoring is about projecting authority as it stood when work was performed (so late-arriving events are evaluated under their contemporaneous authority); alias-cycle is about *current graph reachability* (so the question is "would this edge close a cycle in today's graph?"). Recording the anchor distinction here forecloses the ambiguity that would otherwise re-surface every time a future detector is added.

#### S5.5 Catalog row

The flag catalog (`contracts/flag-catalog.md`) grows by one row at this ADR:

| # | Category | Raised by | Resolvability | Designated Resolver | Phase |
|---|----------|-----------|---------------|---------------------|-------|
| 9 | `cycle_violation` | Cycle Guard CD | `manual_only` | system | 3 (closeout) |

The previous "reserved — growth slot" row is consumed by this addition. Future categories require a new ADR amendment.

#### What §S5 does not decide

The following are explicitly deferred:

1. **Read-side semantics over a graph containing a flagged cycle.** A future Ship that exercises identity resolution on a cyclic graph must commit a position. Until then, behavior is undefined and reads MAY surface a cycle-present marker without commitment.
2. **Cycle resolution mechanism.** A `conflict_resolved/v1` event authored against a `cycle_violation` flag has no canonical effect at this ADR's decide time. The resolution path is a future Ship's concern.
3. **Forking-cycle case via split-then-merge.** A `subject_split/v1` followed by a `subjects_merged/v1` between the source and a successor (or among successors) can construct a cycle through the split edges. The graph model is general — split edges are directed graph edges and the detection algorithm is graph-general — so this case is *caught by construction* by the same procedure. It is recorded here as `decided-unexercised`: the canonical exercise is the merge-only cycle (A→B→A); split-driven cycles are a corollary the detector handles without special casing. A future Ship that observes a real fork-cycle case may upgrade this to `exercised-met`.

---

## Consequences

### Charter updates

- **Cross-cutting rules — Flag catalog table** grows from 8 active rows + 1 reserved to 9 active rows. Cite update: row 9 cited as `ADR-006-R §S5`.
- **Invariants** unchanged. §S1 (accept-and-flag) and §S2 (flag-as-canonical-anomaly-surface) are carried verbatim.

### Ledger updates

| concept | was | becomes | settled-by | status |
|---|---|---|---|---|
| `cycle_violation` (flag category) | — (new) | FLAG | ADR-006-R §S5 | DEFERRED until Wave 3 step (b) lands the guard and tests; promotes to STABLE on `exercised-met` per Rule R-7 |
| `accept-and-flag` | INVARIANT | INVARIANT (re-cited) | ADR-006-R §S1 | STABLE (carried) |
| `flag` | INVARIANT | INVARIANT (re-cited) | ADR-006-R §S2 | STABLE (carried) |
| `flag-creation-location` | INVARIANT | INVARIANT (re-cited) | ADR-006-R §S4 | STABLE (carried) |
| `conflict-detection` | ALGORITHM | ALGORITHM (re-cited) | ADR-006-R §S3 | STABLE (carried) |

The PM in Wave 4 of the closeout regenerates ledger and charter; this ADR is the authority cited.

### Supersessions

- ADR-006 is fully superseded by this ADR. §S1–§S4 carry verbatim by reference; §S5 is new. ADR-006's body is preserved in repo per supersede-rules; cites should move to ADR-006-R for §S1–§S5.
- ADR-002 §S9 is *not* superseded. The acyclicity commitment stands. What this ADR adds is the surfacing mechanism (flag) for the case where the construction-time invariant is observed to be violable; ADR-002 §S9's claim that the lineage graph is acyclic is the platform's intent, and the cycle-guard flag is how the platform reports failure to uphold that intent rather than silently storing a corrupt graph.

### Rejected alternatives

**Alt-1: reject the cycle-closing event at push time.** This was the reviewer's literal recommendation. Rejected because it contradicts ADR-006 §S1: the structural validity of the event (envelope + payload schema) is intact; the anomaly is a state predicate (graph reachability). State-based rejection at push is the failure mode §S1 was authored to prevent. Accept-and-flag is the same answer here as for every other state predicate the platform surfaces.

**Alt-2: detect at projection-read-time only (cycle-detection in the alias projector).** Rejected because cycles must be visible at the same moment they are introduced — the operational signal a coordinator needs is "the merge you just submitted closed a cycle," not "the next reader will discover it." Read-side cycle detection (visited-set in the projector) remains valuable as a defense-in-depth measure but is downstream of this guard, not a replacement for it. Whether the projector also adds a visited-set is an implementation choice and is not constrained by this ADR.

**Alt-3: event-time anchor for the cycle predicate.** Rejected per §S5.4 rationale. Alias edges are administrative present-tense graph mutations; the question "did this edge close a cycle?" is a question about today's graph, not the graph as of when the event was authored. The contrast with `scope_violation`'s event-time anchor is intentional and recorded.

**Alt-4: extend the existing per-event post-persist detection pattern to handle cycles.** Rejected because the in-batch case (two cycle-closing edges in the same push) cannot be deterministically detected post-persist without ordering guarantees the protocol does not commit to. Pre-persist batch-serial is the minimum that gives a deterministic predicate.

**Alt-5: defer to a future Ship.** Rejected because the irreversibility profile is asymmetric: a stored cycle is permanent (ADR-001 §S1) but a guard is additive code with no schema impact. The cost of landing the guard now is bounded; the cost of discovering a stored cycle later is unbounded. Deferral would be silent acceptance of a known structural risk and would violate Rule R-1 if not flagged — and once flagged, the flag itself blocks the same Ships that landing the guard would unblock.

### What is forbidden by this ADR

- **F-D1.** Reject a cycle-closing alias event at push. Forbidden by §S1. The event is accepted; a `cycle_violation` flag is emitted alongside.
- **F-D2.** Branch flag-emission code on envelope `type` to identify a cycle violation. Forbidden by F-A2 / F-A4. The discriminator is `payload.flag_category = "cycle_violation"` riding `shape_ref = "conflict_detected/v1"` with `type = "alert"`.
- **F-D3.** Use the cycle-introducing event's `event.timestamp` as the cycle-detection anchor. Forbidden by §S5.4. The anchor is request-time.
- **F-D4.** Auto-resolve a `cycle_violation` flag. Forbidden by §S5.3. Resolvability is `manual_only`.

---

## Forward reference

The push-path guard's procedural specification (where the guard runs in code, what queries it issues, the cycle-path canonical form, the test contract for step (b)) lives in [`docs/architecture/cycle-guard-contract.md`](../architecture/cycle-guard-contract.md). The architecture document is downstream of this ADR; ADR-006-R is the authority, the contract document is the implementation specification.

The implementation tracking is recorded as [FP-019](../flagged-positions.md#fp-019--alias-cycle-guard-implementation-tracking) and closes inside Ship-3 closeout Wave 3 step (b).
