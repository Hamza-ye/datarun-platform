# ADR-002 Addendum: Envelope Type Mapping for Identity and Integrity Events

> Status: **Decided** (Addendum to ADR-002, effective alongside ADR-004 S3)
> Date: 2026-04-21
> Exploration: [archive/18](../exploration/archive/18-envelope-vocabulary.md), [archive/20](../exploration/archive/20-system-event-authorship.md), [archive/21](../exploration/archive/21-6-type-closure.md) §4.2
> Retrofit phase: Phase 3e (see [phase-3e.md](../implementation/phases/phase-3e.md) once specced)

---

## Why This Addendum Exists

**This is a traceability record, not a new decision.** It documents how ADR-002's identity/integrity event vocabulary is reconciled with ADR-004 S3's closed 6-type envelope vocabulary. The reconciliation is forced by the architecture; there is no latitude here. This document exists so that future agents (human or AI) do not re-introduce the drift that was caught in Phase 3d's end-of-phase audit.

### The drift that was caught

During Phases 1 and 2, four string literals were used as envelope `type` values:

- `conflict_detected`
- `conflict_resolved`
- `subjects_merged`
- `subject_split`

These strings were added to `envelope.schema.json`'s `type` enum, persisted in the `events` table, asserted on in integration tests, and keyed on in both server and mobile code (grep hits in 12+ files as of 2026-04-21).

**This contradicted ADR-4 S3**, which locks the envelope type vocabulary to exactly **six** values: `capture`, `review`, `alert`, `task_created`, `task_completed`, `assignment_changed`. Any 7th, 8th, 9th, or 10th envelope type is an architecture-grade change and is forbidden without a new ADR.

The drift was not malicious. Phase 1/2 pre-dated the ADR-4 S3 closure, and ADR-4 was decided without an explicit audit of Phase 1/2 code. This addendum closes that audit.

---

## The Correction (Reading A)

The four strings above are **shape names**, not envelope types. Each is now a **platform-bundled internal shape** with the envelope `type` assigned by semantic authorship.

### Mapping table

| Domain fact | Envelope `type` | `shape_ref` | Authoring actor |
|---|---|---|---|
| Integrity detector raises a flag | `alert` | `conflict_detected/v1` | Detector (server/device system actor) |
| Human reviewer resolves a flag | `review` | `conflict_resolved/v1` | Human actor (any scoped role with authority) |
| Auto-resolution policy resolves a flag | `capture` | `conflict_resolved/v1` | `system:auto_resolution/{policy_id}` |
| Identity merge performed | `capture` | `subjects_merged/v1` | Authoring actor (admin or system) |
| Identity split performed | `capture` | `subject_split/v1` | Authoring actor (admin) |

### Why `alert` for detector output

A `conflict_detected` event is exactly what ADR-4 S3 defines as `alert`: *"system-detected anomaly requiring attention, no human judgment rendered yet."* The type matches the semantic perfectly.

### Why `review` for manual resolution but `capture` for auto-resolution

Per cross-cutting.md §1, `review` is defined as **"judgment on a prior event."** Manual resolution is exactly that: a human weighs evidence on a flagged event and renders a decision. Auto-resolution is not judgment — it is a deterministic output produced when a time window elapses with an enabling event present. That makes it a system-authored data-recording event, which ADR-4 S3 classifies as `capture`.

Archive 21 §4.2 Check (d) is explicit: **auto-resolution events use `type=capture` with the resolution shape.** This addendum carries that forward without modification.

The same `shape_ref` (`conflict_resolved/v1`) is used in both cases because the domain fact is identical — only the authorship differs. Consumers that care about "is this a resolution event?" filter on `shape_ref`, not `type`.

### Why `capture` for merge/split

Merge and split are data-recording lifecycle events: they record what happened to a subject's identity. No judgment is being rendered — they are authored with full authority by admin workflows. `capture` is the right type per ADR-4 S3.

---

## Consumer Filtering Rule (Binding)

**Any code that needs to identify identity/integrity events MUST filter on `shape_ref`, not on `type`.**

Correct:

```dart
// Mobile
bool isSystemEvent(Event e) =>
  e.shapeRef.startsWith('conflict_detected/') ||
  e.shapeRef.startsWith('conflict_resolved/') ||
  e.shapeRef.startsWith('subjects_merged/') ||
  e.shapeRef.startsWith('subject_split/');
```

```java
// Server
private static final Set<String> SYSTEM_SHAPE_PREFIXES = Set.of(
    "conflict_detected/", "conflict_resolved/",
    "subjects_merged/",   "subject_split/");

private boolean isSystemEvent(Event e) {
    return SYSTEM_SHAPE_PREFIXES.stream().anyMatch(e.shapeRef()::startsWith);
}
```

**Forbidden**:

```java
// DO NOT do this — keys on type, which now refers to the 6-value envelope vocabulary
if ("conflict_detected".equals(e.type())) { ... }
```

The reason: `type` no longer uniquely identifies an integrity event. An `alert` event could be a `conflict_detected/v1` flag OR any future alert shape. A `review` event could be a manual `conflict_resolved/v1` OR any future review shape. Discrimination lives in `shape_ref`.

---

## Deterministic Flag ID — Clarified

Phase 1 specifies: *"flag event UUID is derived from `(source_event_id + flag_category)` to enable idempotent sweep re-evaluation."*

With four internal integrity shapes rather than one, the derivation becomes:

```
flag_event_uuid = UUIDv5(
  namespace = DATARUN_FLAG_NS,
  name      = source_event_id + "|" + shape_ref + "|" + flag_category
)
```

`shape_ref` is included so that a future integrity shape (e.g., a secondary anomaly detector emitting a different `shape_ref` with the same `flag_category`) cannot collide with existing `conflict_detected/v1` flag UUIDs. This is backward-compatible: re-deriving against existing Phase 1/2 flags (which all carry `shape_ref = conflict_detected/v1`) produces the same UUID if the hash inputs are ordered identically.

---

## Shape Registry Obligation

The four internal shapes (`conflict_detected/v1`, `conflict_resolved/v1`, `subjects_merged/v1`, `subject_split/v1`) are **platform-bundled**. They are registered at server boot time by the platform itself, not by admin UI, and they cannot be deprecated by operators.

This matches the treatment of `assignment_created/v1` and `assignment_ended/v1` (already platform-bundled per IDR-013 and `contracts/shapes/`). Phase 3e will add schema files for the four internal shapes under `contracts/shapes/` and bundled-shape registration on server startup.

---

## FORBIDDEN PATTERNS — For Future Agents

The drift happened because agents treated the envelope `type` field as an extensible tag. It is not. These rules are binding and must be quoted in any future PR that wants to add a new event kind:

### F-A1: Never add a value to the envelope `type` enum

The envelope type vocabulary is locked at 6 values by ADR-4 S3. If you think you need a 7th type, you are wrong — you need a new `shape_ref`. The envelope type answers "what processing pipeline?" The shape answers "what domain fact?" These are different axes.

**If a spec, ticket, or user request says "add a new event type", translate it to "add a new shape" before writing code.**

### F-A2: Never filter code on `type == "<specific_string>"` for discrimination

Filtering on `type` is only valid for pipeline-level routing (e.g., "route all reviews to the review-processing pipeline"). For domain discrimination ("is this a merge event?"), filter on `shape_ref`.

If a test asserts `e.type == "conflict_detected"` the test is wrong — it must assert `e.type == "alert" && e.shapeRef == "conflict_detected/v1"`.

### F-A3: Never use envelope type to encode authorship

`type` is not a proxy for "who wrote this." Authorship is carried by `actor_ref`. The same shape (e.g., `conflict_resolved/v1`) can be written under different envelope types depending on the authoring actor (human → `review`, system → `capture`). Do not invent a new type to distinguish authorship — discriminate on `actor_ref` (specifically, the `system:*` prefix convention for system actors).

### F-A4: Before writing code that keys on a system event, read this addendum

If you are about to write any of:

- `if (type == "conflict_detected" | "conflict_resolved" | "subjects_merged" | "subject_split")`
- `WHERE type IN ('conflict_detected', ...)`
- `type.startsWith("conflict")` (or any envelope-type-based integrity classification)

**Stop.** Re-read the Consumer Filtering Rule above. Use `shape_ref` instead. If you think you have a case that genuinely needs to key on `type`, that is an architecture-grade question — escalate to a new IDR, do not just write the code.

### F-A5: Never split a shape across envelope types without recording the authorship rule

`conflict_resolved/v1` legitimately spans `type=review` and `type=capture` because the authorship rule is written above (manual vs auto). If you introduce another shape that spans types, you MUST record the authorship discriminator in the same table form as the mapping table above, in a new addendum or IDR. Leaving authorship implicit is how the Phase 1/2 drift started.

---

## Why Reading A, Not Reading B

Two readings were on the table during the Phase 3d audit:

- **Reading A (adopted)**: strict 6-type envelope, four internal shapes bundled with the platform, authorship determines type for the `conflict_resolved` shape.
- **Reading B (rejected)**: relax ADR-4 S3 to 10 types by re-admitting `conflict_detected`, `conflict_resolved`, `subjects_merged`, `subject_split` as platform-reserved envelope types.

Reading B was rejected for three reasons:

1. **ADR-4 S3 is a structural constraint, not a strategy.** Relaxing it to "6 operator types + 4 platform types" concedes that the envelope type vocabulary is extensible, which defeats the whole point of a closed vocabulary (predictable pipeline routing, bounded contract surface).

2. **Authorship can't be encoded in type.** Reading B has no clean answer for auto-resolution — is it `conflict_resolved` or `capture`? Either choice breaks a different invariant (append-only type semantics, or authorship clarity). Reading A dissolves the question: shape = what happened, type = what pipeline, actor = who did it.

3. **Archive 21 §4.2 Check (d) already committed to Reading A.** The 6-type closure was explicitly audited against identity/integrity events during ADR-4 ratification; the walk-through concluded with Reading A's mapping. Reading B would require retracting that audit result.

---

## Retrofit Scope (Pointer)

Code and tests across server, mobile, and contracts must be migrated off the 4 drift type strings. Full scope lives in **Phase 3e** (not yet specced as of this addendum's authoring). Summary:

- 2 envelope schemas (server + contracts)
- 4 server Java files (SyncController, IdentityService, ConflictDetector, ConflictResolutionService)
- 3 mobile Dart files (event_store, projection_engine, sync_service)
- 2 shared fixture copies (projection-equivalence.json)
- ~5 test files (server + mobile)
- Prose corrections in IDR-009, IDR-015, phases 1/2/3/4, ADR-001 pointer paragraph, flag-catalog.md

The retrofit is **data-destructive for dev/test DBs** (existing events carry soon-to-be-invalid `type` values); CI and local dev must recreate the docker-compose test DB on the retrofit commit. No production data exists yet — this concern ends when Phase 3e lands.

---

## Traceability

| Subject | Source |
|---|---|
| Envelope type closure (6 values) | ADR-4 S3 |
| Four integrity domain facts | ADR-2 S6, S8, and Phase 1/2 specs |
| Auto-resolution uses `capture` | Archive 21 §4.2 Check (d) |
| `review` = "judgment on a prior event" | docs/architecture/cross-cutting.md §1 |
| Platform-bundled shapes (precedent) | IDR-013, contracts/shapes/assignment_*.schema.json |
| Deterministic flag ID derivation (pre-addendum) | phase-1.md line 161 |
| Grep evidence of drift (2026-04-21) | Phase 3d close-out audit |

---

## Does This Supersede Anything?

- **ADR-002**: not superseded. This addendum does not change any S1–S14 sub-decision. It clarifies how integrity events carry their envelope type, which ADR-002 never specified.
- **ADR-004 S3**: not superseded. The 6-type vocabulary stands. This addendum confirms it.
- **Phase 1 / Phase 2 specs**: prose will be corrected in Phase 3e's docs commit. No architectural change.
- **IDR-015** (scope-filtered sync query): the SQL example in IDR-015 currently keys on `type IN (...)`. Phase 3e rewrites it to key on `shape_ref`, and may require a new index (Flyway V6) if the existing covering index does not help the `shape_ref LIKE` predicate.

---

## Next Step

Phase 3e is specced and executed. After 3e passes, this addendum is closed; Phase 4 proceeds on a clean vocabulary.
