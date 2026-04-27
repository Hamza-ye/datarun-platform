# Cycle-Guard Contract — Push-Path Alias-Cycle Detection

> **Authority**: [ADR-006-R §S5](../adrs/adr-006-flag-semantics-R.md). This document is the implementation specification step (b) of Ship-3 closeout Wave 3 follows verbatim. It is downstream of the ADR and may be revised; the §S5 commitments may not.
>
> **Tracking**: [FP-019](../flagged-positions.md#fp-019--alias-cycle-guard-implementation-tracking) closes when the implementation lands and the two specified tests pass.
>
> **Read-once**: this document is self-contained. Read it, implement against it, write the two tests. Do not re-debate the design — that is ADR-006-R's job.

---

## 1. Why this guard exists

`subjects_merged/v1` and `subject_split/v1` are platform-bundled identity-lifecycle shapes ([ADR-007 §S2](../adrs/adr-007-envelope-type-closure.md)) that, taken together, build a directed graph over subject UUIDs. Two coordinators acting on offline-divergent views of that graph can each commit an alias edge that, taken jointly, closes a cycle. Once the cycle-closing event is persisted, [ADR-001 §S1](../adrs/adr-001-offline-data-model.md) (append-only) forbids retraction; identity resolution that traverses the cycle is undefined.

[ADR-002 §S9](../adrs/adr-002-identity-conflict.md) committed lineage acyclicity *by construction* but the construction-time enforcement was unimplemented. [ADR-006-R §S5](../adrs/adr-006-flag-semantics-R.md) commits the platform to surfacing cycle-closure as a flag (`flag_category = "cycle_violation"`), preserving accept-and-flag ([ADR-006-R §S1](../adrs/adr-006-flag-semantics-R.md)). This document specifies the procedure.

Reviewer source: [`docs/reviews/system/architect.md`](../reviews/system/architect.md) SC-07 + C2-03. Reviewer recommended reject-at-push; ADR-006-R reconciles to accept-and-flag. The guard accepts; the flag surfaces.

---

## 2. Where the guard runs

The guard lives on the push pipeline in [`SyncController.push`](../../server/src/main/java/dev/datarun/ship1/sync/SyncController.java). Insertion point:

1. **Phase 1** (existing) — per-event envelope validation + shape payload validation. On any error, the entire batch is rejected (existing behavior; do not change).
2. **NEW — Phase 1.5: cycle-guard pass.** Iterate `toInsert` in array order. For each event whose `shape_ref.startsWith("subjects_merged/")` or `shape_ref.startsWith("subject_split/")`, run the cycle predicate (§4 below) against the union graph (§3). Record the verdict per index; do **not** persist anything yet.
3. **Phase 2** (existing) — `EventRepository.insert(e)` for each event in `toInsert`. **Unchanged**: every event lands. The triggering alias event is inserted regardless of the cycle verdict.
4. **NEW — Phase 2.5: cycle flag emission.** For each cycle-positive verdict from Phase 1.5, emit one `conflict_detected/v1` flag event via `EventRepository.insert(...)` with the payload specified in §5. Emit *after* the triggering event has itself been inserted (so `payload.source_event_id` references a row that exists).
5. **Phase 2** (existing) — non-alias `type=capture` events still go through `ConflictDetector.detect(persisted)` for `scope_violation` / `identity_conflict` per existing logic. Non-alias events traverse unchanged.

The guard is a new component (suggested name: `CycleGuard` in `dev.datarun.ship1.integrity`), wired into `SyncController` alongside `ConflictDetector`. It is **not** added to `ConflictDetector` itself: `ConflictDetector` is post-persist per-event; `CycleGuard` is pre-persist batch-serial. The asymmetry is essential (see §3.2) and conflating them would dilute both.

---

## 3. What the guard reads

### 3.1 The persisted alias graph

Two queries against `EventRepository`:

- `eventRepository.findByShapeRefPrefix("subjects_merged/")`
- `eventRepository.findByShapeRefPrefix("subject_split/")`

The existing `findByShapeRefPrefix` helper is sufficient — no new repository method is required. Iterate the returned events; project each into directed edges per §3.3.

### 3.2 In-flight already-accepted edges from earlier in the batch

For event at index `N` in the push batch, the guard considers alias edges contributed by events `0..N-1` of the same batch — but only those whose own cycle verdict was negative *or positive* (both count: per [ADR-006-R §S1](../adrs/adr-006-flag-semantics-R.md), even flagged events are accepted, so a flagged-but-cycle-closing event still contributes its edge to the graph visible to event `N`).

This is the meaningful asymmetry vs. `ConflictDetector`: a strict pre-persist guard that read only the persisted graph would miss the case where two cycle-closing edges arrive in the same push, and a strict post-persist guard would have nondeterministic ordering between the two events of a single batch. Batch-serial is the deterministic minimum.

### 3.3 Edge projection

Each alias-introducing event projects to one or more directed edges over subject UUIDs:

- **`subjects_merged/v1`**: one edge `payload.retired_id → payload.surviving_id`. Per [ADR-002 §S6](../adrs/adr-002-identity-conflict.md) the retired_id aliases to the surviving_id; the directed alias edge points from retired to surviving.
- **`subject_split/v1`**: one edge per successor — for each `succ ∈ payload.successor_ids`, edge `payload.source_id → succ`. Per [ADR-002 §S8](../adrs/adr-002-identity-conflict.md), the source is archived and successors take over; the directed lineage edge points from source to each successor. The schema enforces `minItems: 2` for `successor_ids`, so split events always contribute at least 2 edges.

**Field-name confirmation** (from `contracts/shapes/`):

| shape | source field | target field(s) | edge(s) emitted |
|---|---|---|---|
| `subjects_merged/v1` | `retired_id` | `surviving_id` | `retired_id → surviving_id` |
| `subject_split/v1` | `source_id` | `successor_ids[]` | `source_id → successor_ids[i]` for each i |

The field names above are used verbatim from the schema files; do not paraphrase from memory.

---

## 4. The cycle predicate

### 4.1 Definition

A cycle would form *if and only if* the new edge `from → to` is added to a graph in which `to` already has a forward path back to `from`.

Equivalently: starting from `to` in the union graph (persisted ∪ in-flight-earlier-in-batch), follow forward edges. If `from` is reachable, the new edge would close a cycle.

For a `subjects_merged/v1` event: `from = retired_id`, `to = surviving_id`.
For a `subject_split/v1` event: the event contributes multiple edges; the cycle predicate is checked **per emitted edge** with `from = source_id`, `to = successor_ids[i]` for each `i`. The event is cycle-positive if **any** of its emitted edges would close a cycle. Record the first cycle path found; do not enumerate further (one flag per triggering event is sufficient — match the `ConflictDetector#identity_conflict` pattern of "one flag per new capture").

### 4.2 Algorithm

DFS from `to`, target `from`, over the union graph. Visited-set avoids infinite loops on graphs that already contain a cycle (defense in depth — should not occur, but the guard must not hang if it does).

```
function detectCycle(unionGraph, from, to):
    if from == to:
        return [from, to]                # self-loop is a cycle
    stack = [(to, [to])]
    visited = {to}
    while stack not empty:
        (node, pathSoFar) = stack.pop()
        for next in unionGraph.outgoing(node):
            if next == from:
                return pathSoFar + [from, to]    # canonical close (see §4.3)
            if next not in visited:
                visited.add(next)
                stack.push((next, pathSoFar + [next]))
    return null    # no cycle
```

### 4.3 Canonical cycle-path form

`payload.cycle_path` is a JSON array of subject UUIDs (string-formatted). The canonical form is:

**`[to, intermediate_nodes..., from, to]`** — the traversal starts at the new edge's `to`, follows forward edges through any intermediate nodes, reaches `from`, and closes with `to` to mark the loop.

Worked example: persisted graph `A→B`. New event `subjects_merged/v1` with `retired_id=B, surviving_id=A`. `from=B, to=A`. DFS from `A`: outgoing → `B`. `B == from`, so cycle confirmed. `pathSoFar = [A]`. Return `[A] + [B, A] = [A, B, A]`.

Self-loop case (`from == to`, e.g., a merge with `retired_id == surviving_id` — schema does not forbid this): return `[from, to]` as a degenerate cycle path. Step (b) MAY also reject this earlier as a payload validation issue, but that is outside the cycle-guard's concern.

---

## 5. Flag emission

When the cycle predicate returns positive for an alias-introducing event, after that event itself has been inserted (Phase 2), emit one `conflict_detected/v1` flag event via `EventRepository.insert` with the following construction. There is **no public flag-emission helper available** at this writing — `ConflictDetector#buildFlag` is private. Step (b) MAY either (a) lift `buildFlag` to a package-private helper for reuse, or (b) construct the flag inline in `CycleGuard` mirroring `ConflictDetector#buildFlag`. Either is acceptable; prefer (a) for consistency.

**Envelope fields**:

| field | value |
|---|---|
| `id` | fresh `UUID.randomUUID()` |
| `type` | `"alert"` ([ADR-007 §S1](../adrs/adr-007-envelope-type-closure.md)) |
| `shape_ref` | `"conflict_detected/v1"` (the integrity-flag shape, F-A2/F-A4 discipline — discriminate on shape_ref, never on type) |
| `activity_ref` | `null` |
| `subject_type` / `subject_id` | mirror the triggering event's `subject_type` / `subject_id` (existing `ConflictDetector#buildFlag` precedent) |
| `actor_id` (envelope `actor_ref` serialized) | `"system:cycle_guard/cycle_violation"` ([ADR-008 §S2](../adrs/adr-008-envelope-reference-fields.md) `system:{component}/{id}` format; component = `cycle_guard`, id = `cycle_violation`) |
| `device_id` | server-reserved `00000000-0000-0000-0000-000000000001` via `ServerEmission#serverDeviceId()` (existing precedent) |
| `device_seq` | next from `server_device_seq` PostgreSQL sequence via `ServerEmission#nextServerDeviceSeq()` (existing precedent) |
| `sync_watermark` | unset on insert; assigned by BIGSERIAL (existing precedent) |
| `timestamp` | `OffsetDateTime.now()` — **request-time** anchor per [ADR-006-R §S5.4](../adrs/adr-006-flag-semantics-R.md). NOT the triggering event's `timestamp`. |

**Payload**:

```json
{
  "source_event_id": "<triggering event id>",
  "flag_category": "cycle_violation",
  "resolvability": "manual_only",
  "designated_resolver": {
    "type": "actor",
    "id": "system:cycle_guard/cycle_violation"
  },
  "reason": "alias edge would close a cycle in the subject identity graph",
  "cycle_path": ["<uuid>", "...", "<uuid>"]
}
```

`cycle_path` MUST be the canonical form from §4.3 — starts at `to`, ends at `to`, traverses through `from`. The array length is at least 2 (self-loop) and unbounded above.

The `conflict_detected/v1` shape schema declares `additionalProperties: true`, so `cycle_path` is permitted as a payload extension without schema change.

---

## 6. Accept-and-flag is preserved

The triggering alias event is **always** inserted, regardless of cycle verdict. The flag is a separate event landing alongside it. Both are persisted. The push response carries:

- `accepted` — count of newly-inserted non-flag events from `toInsert` (unchanged).
- `duplicates` — count of `ON CONFLICT (id) DO NOTHING` no-ops (unchanged).
- `flags_raised` — count of all flag events emitted, including the new `cycle_violation` flags. Increment the existing counter; do not introduce a separate count.

No protocol field is added or removed. No HTTP response code changes.

---

## 7. Test contract (mandatory for step (b))

Two tests. Both are integration tests at the `Ship3WalkthroughAcceptanceTest` / `WalkthroughAcceptanceTest` pattern level, driving the real HTTP push surface via `TestRestTemplate`. Both MUST pass for FP-019 to close.

### 7.1 Test A — `cycleGuard_singleEventPush_persistedGraphCloses`

**Setup**: Use the existing dev-bootstrap (or equivalent fixture) to seed actors with assignments. Let `A` and `B` be two distinct subject UUIDs (test-generated).

**Step 1**: Push a batch containing a single `subjects_merged/v1` event with `retired_id = A, surviving_id = B` (the persisted edge `A → B`). Assert: 200 OK, `accepted = 1`, `flags_raised = 0`.

**Step 2**: In a *separate* push batch, push a single `subjects_merged/v1` event with `retired_id = B, surviving_id = A` (the new edge `B → A`, which would close `A → B → A`). Assert:

- HTTP 200 OK.
- `accepted = 1` (the alias event is accepted; F-D1 forbids rejection).
- `flags_raised = 1`.
- A `conflict_detected/v1` event exists in the store with:
  - `payload.flag_category = "cycle_violation"`,
  - `payload.source_event_id = <id of the step-2 event>`,
  - `payload.cycle_path = [A, B, A]` (canonical form per §4.3 — `to, ..., from, to` where `to=A, from=B`),
  - envelope `type = "alert"`,
  - envelope `actor_id` (i.e., `actor_ref` serialized) = `"system:cycle_guard/cycle_violation"`.

**Forcing property**: this test fails under any implementation that rejects the cycle-closing event, that omits the flag, that anchors flag `timestamp` to event-time rather than request-time, or that emits a non-canonical `cycle_path`.

### 7.2 Test B — `cycleGuard_twoEventBatch_inFlightCloses`

**Setup**: as Test A, with no prior alias events persisted.

**Step**: Push a single batch containing two events in array order:

1. `subjects_merged/v1` with `retired_id = A, surviving_id = B` (introduces edge `A → B`).
2. `subjects_merged/v1` with `retired_id = B, surviving_id = A` (would close cycle through edge 1).

Assert:

- HTTP 200 OK.
- `accepted = 2`.
- `flags_raised = 1`.
- One `conflict_detected/v1` event exists with `payload.flag_category = "cycle_violation"`, `payload.source_event_id = <id of event 2>` (the cycle-closing edge), `payload.cycle_path = [A, B, A]`.
- Event 1 has **no** flag attributed to it (it did not close the cycle; on its own it is a clean merge).

**Forcing property**: this test fails under any implementation that does not feed in-flight earlier-in-batch edges into the union graph for later events. A naive pre-persist guard that reads only the persisted graph passes Test A but fails Test B; a naive post-persist guard with no ordering discipline is nondeterministic on Test B. Only batch-serial detection per §3.2 reliably passes both.

### 7.3 Tests NOT required at step (b)

The following are out of scope; they may be exercised by future Ships and are explicitly `decided-unexercised` per [ADR-006-R §S5](../adrs/adr-006-flag-semantics-R.md):

- Cycle introduced via `subject_split/v1` (split source → successors with one successor later merging back to source). The graph model handles it; the test is omitted to keep step (b) bounded.
- Cycles longer than 2 nodes (`A → B → C → A`). Algorithm is graph-general; canonical-form correctness is demonstrated by Test A.
- Resolution of a `cycle_violation` flag via `conflict_resolved/v1`. Resolution semantics are deferred per §S5.3.

---

## 8. Anchor record

The cycle predicate's temporal anchor is **request-time** (`OffsetDateTime.now()` at flag emission), not event-time (`triggering_event.timestamp`). This is locked by [ADR-006-R §S5.4](../adrs/adr-006-flag-semantics-R.md).

Contrast with `scope_violation`: that detector evaluates the actor's scope as of the *event's* timestamp ([ADR-003 §S3](../adrs/adr-003-authorization-sync.md) projection-time anchor; see also [`ScopeViolationTemporalDivergenceTest`](../../server/src/test/java/dev/datarun/ship1/acceptance/ScopeViolationTemporalDivergenceTest.java)). The contrast is deliberate and recorded — alias edges are present-tense graph mutations applied to a present-tense graph; scope violations are field-time observations evaluated under contemporaneous authority. Different anchors, different domain semantics. Do not unify them.

---

## 9. Summary checklist for step (b)

- [ ] `CycleGuard` component created in `dev.datarun.ship1.integrity`.
- [ ] Wired into `SyncController.push` between Phase 1 (validation) and Phase 2 (persistence).
- [ ] Edge projection per §3.3 from `subjects_merged/v1` and `subject_split/v1`.
- [ ] Union graph: persisted (`findByShapeRefPrefix`) ∪ in-flight (earlier batch indices).
- [ ] DFS / BFS cycle predicate per §4.2.
- [ ] Cycle-path canonical form per §4.3 (`[to, ..., from, to]`).
- [ ] Flag construction per §5: `type=alert`, `shape_ref=conflict_detected/v1`, `actor_ref=system:cycle_guard/cycle_violation`, request-time `timestamp`, `payload.flag_category=cycle_violation`, `payload.cycle_path` populated.
- [ ] Triggering event still inserted; flag emitted alongside.
- [ ] `flags_raised` counter increments existing field in push response.
- [ ] Test A (single-event-push, persisted-graph-closes) lands and passes.
- [ ] Test B (two-event-batch, in-flight-closes) lands and passes.
- [ ] FP-019 resolution log updated with both commit SHAs.
- [ ] Drift gate (`scripts/check-convergence.sh`) PASS.
