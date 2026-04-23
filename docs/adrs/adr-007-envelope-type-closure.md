# ADR-007: Envelope Type Closure and Integrity Shape Canonicalization

> Status: **Decided**
> Date: 2026-04-23
> Convergence round: 1 (Phase 2 ADR #2)
> Upstream: ADR-001, ADR-002, ADR-004 §S3, ADR-006 §S2
> Absorbs: ADR-002 Addendum (2026-04-21) — content lifted, document superseded
> Exploration: archive/18, archive/20, archive/21 §4.2 · [Phase 0.5 harvest — Group 1](../convergence/inventory/disputes-harvest.md)

---

## Context

The envelope `type` field defines which processing pipeline handles an event. ADR-004 §S3 closed the type vocabulary at six values: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`. That closure is a structural invariant — extending it is an architecture-grade change, forbidden without a new ADR.

During Phases 1 and 2 of implementation, four additional strings were used as envelope `type` values: `conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split`. They leaked into the envelope schema, the events table, tests, and client code across ~12 files. This contradicted ADR-004 §S3.

The drift was caught in the Phase 3d audit and corrected under a document titled *"ADR-002 Addendum: Envelope Type Mapping for Identity and Integrity Events"* (2026-04-21). The correction was right. The document form was wrong.

**The Addendum form creates two authoritative documents for one decision area** — exactly the failure mode the convergence protocol was written to eliminate (see [supersede-rules.md §"Why no Addenda"](../convergence/supersede-rules.md)). Future agents reading ADR-002 would see a pointer to the Addendum; agents reading only the Addendum would miss ADR-002's context; agents reading neither would re-introduce the drift. The Phase 0 inventory DISPUTED the concept `conflict-detected` precisely because prose across the repo still carried the pre-Addendum reading.

This ADR canonicalizes the correction as a standalone, self-contained decision. The Addendum is absorbed. The content below is authoritative; the Addendum file is retained only as an archival artifact with a top-of-file pointer to this ADR.

---

## Decision

### S1: The envelope `type` vocabulary is closed at six values

Allowed values: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`. No seventh, eighth, or Nth value is admissible. Extension is an architecture-grade change.

**What `type` answers**: which processing pipeline handles this event. It does not answer "what domain fact does this event record" (that is `shape_ref`) and does not answer "who authored this event" (that is `actor_ref`). These three axes are orthogonal and must stay orthogonal.

This re-states ADR-004 §S3 as the canonical closure statement. ADR-004 §S3 remains the first-decision cite; this ADR is the convergence-era canonical cite.

### S2: Identity and integrity domain facts are expressed as platform-bundled shapes

Four integrity/identity domain facts have `shape_ref` values, **not** `type` values:

| Domain fact | Envelope `type` | `shape_ref` | Authoring actor |
|---|---|---|---|
| Integrity detector raises a flag | `alert` | `conflict_detected/v1` | Detector (server or device system actor) |
| Human reviewer resolves a flag | `review` | `conflict_resolved/v1` | Human actor (any scoped role with resolver authority) |
| Auto-resolution policy resolves a flag | `capture` | `conflict_resolved/v1` | `system:auto_resolution/{policy_id}` |
| Identity merge performed | `capture` | `subjects_merged/v1` | Authoring actor (admin or system) |
| Identity split performed | `capture` | `subject_split/v1` | Authoring actor (admin) |

**Why `alert` for detector output.** An integrity-detection event is a system-identified anomaly requiring attention with no human judgment rendered yet. That is the precise definition of `alert` per ADR-004 §S3.

**Why `review` for manual resolution but `capture` for auto-resolution.** `review` is defined as "judgment on a prior event." Manual resolution fits exactly: a human weighs evidence on a flagged event and renders a decision. Auto-resolution is not judgment — it is a deterministic output produced when a timing or state window elapses. That makes it a system-authored data-recording event, which is `capture`. The same `shape_ref` (`conflict_resolved/v1`) carries both cases because the **domain fact** is identical; only the authorship differs. Consumers that care about "is this a resolution event?" filter on `shape_ref`, not `type` (see §S4).

**Why `capture` for merge/split.** Merge and split record what happened to a subject's identity. No judgment is being rendered; they are authored with full authority by admin workflows. `capture` is correct per ADR-004 §S3.

This reading is consistent with ADR-006 §S2: on the event stream, integrity anomalies take flag form via the `alert` + `conflict_detected/v1` pair; no parallel anomaly-record surface exists on the stream. ADR-007 closes the *how* (which envelope type, which shape); ADR-006 §S2 holds the *what* (that a flag event is the canonical representation at all).

### S3: Consumer filtering rule (binding)

**Any code that needs to identify identity or integrity events MUST filter on `shape_ref`, not on `type`.**

The `type` field is only valid for pipeline-level routing ("route all reviews to the review-processing pipeline"). For domain discrimination ("is this a merge event?"), filter on `shape_ref`.

The reason: `type` no longer uniquely identifies an integrity event. An `alert` could be a `conflict_detected/v1` flag **or** any future alert shape. A `review` could be a manual `conflict_resolved/v1` **or** any future review shape. Discrimination lives in `shape_ref`.

### S4: Deterministic flag identity includes `shape_ref`

The flag-event UUID derivation:

```
flag_event_uuid = UUIDv5(
  namespace = DATARUN_FLAG_NS,
  name      = source_event_id + "|" + shape_ref + "|" + flag_category
)
```

`shape_ref` participates in the hash so that a future integrity shape emitting the same `flag_category` against the same source event cannot collide with the existing `conflict_detected/v1` flag UUIDs. The derivation is backward-compatible with Phase 1/2 data (which all carry `shape_ref = conflict_detected/v1`) when hash inputs are ordered identically.

### S5: Platform-bundled shape registry obligation

The four integrity/identity shapes (`conflict_detected/v1`, `conflict_resolved/v1`, `subjects_merged/v1`, `subject_split/v1`) are **platform-bundled**. They are registered by the platform itself at server boot; they are not authored through the admin UI; they cannot be deprecated by operators.

Platform-bundled is a classification of the shape registry, parallel to `assignment_created/v1` and `assignment_ended/v1` (already platform-bundled per IDR-013). Shape schemas live under `contracts/shapes/`; boot-time registration is a server obligation.

---

## Forbidden patterns (binding — quote in any PR introducing a new event kind)

**F-A1: Never add a value to the envelope `type` enum.** If a spec or user request says "add a new event type," translate it to "add a new shape" before writing anything. `type` answers *which pipeline*; `shape_ref` answers *what fact*. These are different axes.

**F-A2: Never filter code on `type == "<specific_string>"` for domain discrimination.** Filtering on `type` is only valid for pipeline routing. For "is this a merge event?", filter on `shape_ref`. A test asserting `e.type == "conflict_detected"` is wrong — it must assert `e.type == "alert" && e.shapeRef == "conflict_detected/v1"`.

**F-A3: Never use envelope `type` to encode authorship.** `type` is not a proxy for "who wrote this." Authorship is in `actor_ref`. The same shape (e.g., `conflict_resolved/v1`) can ride different types depending on authoring actor (human → `review`, system → `capture`). Discriminate on `actor_ref` (specifically, the `system:*` prefix convention for system actors).

**F-A4: Before writing code that keys on a system event, re-read this ADR.** If you are about to write anything like `if (type in ["conflict_detected", "conflict_resolved", "subjects_merged", "subject_split"])` or the SQL equivalent, stop. Use `shape_ref`. If you think you have a case that genuinely requires keying on `type`, that is an architecture-grade question — it escalates to a new ADR, not an IDR.

**F-A5: Never split a shape across envelope types without recording the authorship rule.** `conflict_resolved/v1` legitimately spans `type=review` and `type=capture` because the authorship rule is stated in §S2. If you introduce another shape that spans types, the authorship discriminator **must** be written down in the same table form. Leaving authorship implicit is how the Phase 1/2 drift started.

---

## Rejected alternatives

**Alt-1: relax ADR-004 §S3 to 10 types** by re-admitting `conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split` as platform-reserved envelope types. Rejected on three grounds:

1. ADR-004 §S3 is a structural constraint. Relaxing it concedes that the type vocabulary is extensible, which defeats the purpose of a closed vocabulary (predictable pipeline routing, bounded contract surface).
2. No clean answer for auto-resolution authorship — is it `conflict_resolved` or `capture`? Either choice breaks a different invariant.
3. Archive 21 §4.2 Check (d) already committed to the current reading during ADR-004 ratification. Reversing that would require retracting an audited result.

**Alt-2: keep the Addendum form.** Rejected. The Addendum pattern is the specific failure mode the convergence protocol exists to eliminate. The correction content is durable; the document form is not.

**Alt-3: make `conflict-detected` a PRIMITIVE row in the ledger.** Rejected. A primitive is a load-bearing object the platform is built on. `conflict_detected` is a shape — a contract-shaped expression of an integrity fact. It rides on the Event primitive. The Phase 0 PRIMITIVE reading was an un-migrated prose layer from pre-Addendum phase specs.

---

## Consequences

### Supersessions

- **ADR-002 Addendum** — superseded by this ADR. Content absorbed. File retained for archival; header updated with `Superseded-By: ADR-007 (content absorbed)`; top-of-file annotation `>>> ABSORBED INTO ADR-007: this entire document's content is now in ADR-007 §S1–§S5 and the forbidden-patterns section; consult ADR-007 as canonical.`
- **ADR-002 main** — not superseded. Its decisions S1–S14 stand. Its Addendum pointer line in the header is now a one-hop redirect: readers follow the link, land in the Addendum, see the ABSORBED INTO annotation, and arrive at ADR-007.
- **ADR-004 §S3** — not superseded. Re-stated and made canonical for the convergence era.

### Ledger updates (round 1)

| concept | was | becomes | settled-by | status |
|---|---|---|---|---|
| `conflict-detected` | DISPUTED | CONTRACT | ADR-007 §S2 | PROPOSED |
| `conflict-resolved` | CONTRACT | CONTRACT (re-cited) | ADR-007 §S2 | PROPOSED |
| `subjects-merged` | CONTRACT | CONTRACT (re-cited) | ADR-007 §S2 | PROPOSED |
| `subject-split` | CONTRACT | CONTRACT (re-cited) | ADR-007 §S2 | PROPOSED |
| `type-vocabulary` | INVARIANT | INVARIANT (re-cited) | ADR-007 §S1 | PROPOSED |

### Charter updates

- Invariants gain one row: *envelope type vocabulary is closed at six values* ([ADR-007 §S1](adrs/adr-007-envelope-type-closure.md)).
- Contracts table gains four rows: `conflict_detected/v1`, `conflict_resolved/v1`, `subjects_merged/v1`, `subject_split/v1` (platform-bundled shapes, ADR-007 §S2).

### Forward reference

ADR-008 (next in queue) canonicalizes envelope **reference** fields (`subject_ref`, `actor_ref`, `activity_ref`) as CONTRACT. It cites this ADR's §S3 for the `actor_ref`-is-authorship-authority principle.

---

## Traceability

| Subject | Source |
|---|---|
| Envelope type closure (6 values) | ADR-004 §S3 (first decision); ADR-007 §S1 (canonical) |
| Four integrity domain facts | ADR-002 S6, S8, Phase 1/2 specs |
| Auto-resolution uses `capture` | Archive 21 §4.2 Check (d) |
| `review` = "judgment on a prior event" | `docs/architecture/cross-cutting.md` §1 |
| Platform-bundled shape treatment | IDR-013 (assignment shapes); ADR-007 §S5 generalizes |
| Correction provenance | ADR-002 Addendum (2026-04-21) — absorbed by this ADR |
