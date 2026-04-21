# Phase 1 — Topological Sort & ADR Queue

> **Input**: [concept-ledger.md](../concept-ledger.md) round 0 (269 canonical concepts,
> 244 PROPOSED, 22 OPEN, 3 DEFERRED) + [disputes-harvest.md](disputes-harvest.md)
> (9 DISPUTED rows enriched with archive-harvest reasoning).
>
> **Output**: the Phase 2 ADR queue (root-first) + triage of the 22 OPEN rows
> into four action buckets.
>
> **Lifespan**: temporary scaffolding. Archived at Phase 4 freeze.

---

## 1. Exit criterion check

Per [protocol.md](../protocol.md) §Phase 1: *"every OPEN concept has a draft
ADR in the queue."* The ledger has 22 rows in OPEN/DISPUTED status (13
intrinsically OPEN + 9 DISPUTED). Not all 22 require an ADR — see §2 triage.

## 2. Triage of the 22 OPEN rows

The 22 rows split into four action buckets. Only **Bucket C** produces ADRs
in the Phase 2 queue.

### Bucket A — OBSOLETE (10 rows)

Named **anti-pattern alerts** and **convergence-protocol process steps** that
are not platform concepts. They entered the ledger because raw-cluster
inventory didn't distinguish "concept in the platform's vocabulary" from
"meta-commentary about how to reason about platforms." Resolution: mark
OBSOLETE in round 1; their cautionary content stays in
`docs/exploration/archive/` (anti-patterns) or `docs/convergence/` (process
steps), but they don't earn rows in the charter.

**Anti-pattern names (7 rows)** — introduced by ADR-004 Session 3 as cautions,
never as platform objects:

- `anti-pattern` (the meta-concept)
- `configuration-specialist-trap`
- `greenspun-rule`
- `inner-platform-effect`
- `overlapping-authority-trap`
- `schema-evolution-trap`
- `trigger-escalation-trap`

**Convergence-process steps (3 rows)** — introduced by raw-B from the
consolidation framework doc, describing *how we sort concepts*, not what the
platform contains:

- `boundary-mapping`
- `decision-harvest`
- `gap-identification`

These are properties of the convergence protocol itself, which is temporary
scaffolding due to be archived at Phase 4 freeze.

### Bucket B — DEFER to Phase 4 (1 row)

- `sensitive-subject-classification` — subject-level sensitivity dimension for
  access control. Real design surface, but belongs with Phase 4 workflow &
  policy work. **Blocking condition**: Phase 4 start. Marked DEFERRED.

### Bucket C — Phase 2 ADR queue (11 rows, 4 ADRs)

9 DISPUTED rows + 2 OPEN rows (`flag-creation-location`,
`projection-rebuild-strategy`) collapse to **4 ADRs** because the harvest
surfaced a cross-cutting duality (see
[disputes-harvest.md §Cross-cutting finding](disputes-harvest.md)). Queue
below in §3.

### Bucket D — punt to IDR, not ADR (1 row)

- `projection-rebuild-strategy` — *"Rebuild projection from scratch vs.
  incremental catch-up on cold start"* is an implementation-grade choice
  (IG-authority), not a new architectural decision. Route to an IDR once the
  flag-lifecycle ADR lands. **Action**: leave OPEN in round 1; redirect to
  IDR track in round 2 after ADR-006 settles flag semantics.

---

## 3. ADR queue — root-first

Dependency order derived by: (a) concept depth in the envelope graph
(envelope fields < referents < aggregates < cross-cutting invariants);
(b) authority-order weighting (root docs > ADRs > archive > architecture,
per 2026-04-22 correction); (c) harvest-surfaced collapses.

### ADR-006 — Flags are invariant, detection is algorithm

- **Resolves**: `accept-and-flag` → INVARIANT, `flag` → INVARIANT,
  `flag-creation-location` → settled as corollary (server side, per
  ConflictDetector — archive confirms).
- **Upstream deps**: none. Foundational. Root of the Phase 2 queue.
- **Source weighting**: root docs `docs/access-control-scenario.md` +
  ADR-002 S14 + [disputes-harvest.md §Group-1](disputes-harvest.md).
- **Expected supersessions**: none. First ADR in the convergence round.
- **Companion sweep**: 15 `FLAG`-classified rows get their `settled-by`
  pointer updated to `ADR-006 §X` in the same commit.

### ADR-007 — Envelope `type` is closed; integrity primitives are shapes

- **Resolves**: `conflict-detected` → CONTRACT (ratifies the existing
  ADR-002 Addendum into main ADR stream).
- **Upstream deps**: ADR-006 (flag semantics must be settled before we
  canonicalize the 4 integrity shapes).
- **Source weighting**: ADR-002 Addendum (the whole document) +
  [disputes-harvest.md §Group-1](disputes-harvest.md).
- **Expected supersessions**: ADR-002 becomes ADR-002-R (the addendum is
  folded into the main stream, per protocol §"Adding an Addendum is not
  permitted — it becomes a new ADR when convergence reaches it").
- **Note**: for `conflict-detected` the likely round-1 outcome is direct
  STABLE because the addendum already contains the reasoning — this ADR may
  be a minimal ratification doc.

### ADR-008 — Envelope reference fields are CONTRACT shapes

- **Resolves**: `subject-ref` → CONTRACT, `actor-ref` → CONTRACT,
  `activity-ref` → CONTRACT. The subagent artifact readings (B cluster
  claiming RESERVED) are refuted; the PRIMITIVE reading of `subject-ref` was
  category error (the *referent* is PRIMITIVE, the *reference field* is
  CONTRACT).
- **Upstream deps**: ADR-007 (envelope type closure) — ref fields live in
  the envelope, so envelope vocabulary must be settled first.
- **Source weighting**: `contracts/envelope.schema.json` + ADR-004 §4 +
  [disputes-harvest.md §Group-2](disputes-harvest.md).
- **Expected supersessions**: none (ref fields were always de-facto CONTRACT
  in the schema; this ADR makes it canonical).
- **Size**: probably short. Category-error dissolution + subagent-artifact
  rebuttal.

### ADR-009 — Platform-fixed mechanism vs deployer-configured instance

**This is the cross-cutting ADR surfaced by the harvest.** It defines the
structural shape that recurs across 9 of the 22 OPEN concepts.

- **Resolves**: `scope` → PRIMITIVE (mechanism) + retains CONFIG reading for
  `scope-type`/`scope-composition`; `pattern` → PRIMITIVE (mechanism) +
  retains CONFIG reading for specific patterns; `activity` → CONFIG (instance
  = bundle of shape+pattern+role+scope); + names the duality so every future
  concept that sits on it can cite this ADR instead of re-deriving the split.
- **Upstream deps**: ADR-008 (envelope refs settled — we need `activity-ref`
  as CONTRACT before we can define what `activity` is as an instance of).
- **Source weighting**: `docs/access-control-scenario.md §P6` (root) +
  ADR-004 Session 3 (archive stress-test) +
  [disputes-harvest.md §Group-3](disputes-harvest.md) +
  [disputes-harvest.md §Cross-cutting finding](disputes-harvest.md).
- **Expected supersessions**: ADR-004 partial supersede (its framing of
  `scope` as purely config gets refined — scope-the-mechanism is primitive,
  scope-the-configured-instance is config).
- **Side effect**: unlocks a potential new classification `PRIMITIVE+CONFIG`
  or a notation convention in the ledger schema — TBD in ADR-009 itself.

---

## 4. Synonym-merge list

Called out in the round-0 ledger summary. Phase 2 ADRs resolve these by
picking canonical names; losers become OBSOLETE aliases.

| candidates | likely canonical | handled in |
|---|---|---|
| `role-stale` / `role-staleness` | `role-stale` (shorter, matches flag catalog) | ADR-006 |
| `scope-stale` / `scope-violation` | separate concepts — `scope-violation` is the flag, `scope-stale` describes the state of an assignment drifted out of scope. Verify in Phase 2. | ADR-006 |
| `workflow-pattern` / `pattern` | `pattern` | ADR-009 |

Don't resolve pre-ADR; these merges are outcomes of the ADRs that settle the
parent concepts.

---

## 5. Origin-layer tagging

User's 2026-04-22 authority-order correction requires each concept be
weighted by origin layer when Phase 2 ADR authors consult sources. Layers
(highest authority first):

1. **root-docs** — `docs/access-control-scenario.md`,
   `docs/constraints.md`, `docs/principles.md`, `docs/viability-assessment.md`
2. **adr** — `docs/adrs/adr-00{1..5}.md` + ADR-002 Addendum
3. **archive-harvest** — `docs/exploration/archive/00..21` (stress-tests)
4. **phase-spec** — `docs/implementation/phases/phase-{0..3e}.md`
5. **architecture-consolidation** — `docs/architecture/*` (late-consolidated,
   lowest authority per user correction)

The ledger's `introduced-in` column already encodes the origin doc. For
Phase 2 ADR drafting, treat that column as the primary source tag. A full
per-row tagging pass is **not required** for Phase 1 — the 4 ADRs on the
queue each explicitly cite their weighted sources in §3 above.

---

## 6. Phase 2 round-0 prediction

If the queue drafts cleanly without supersession cascades:

- **Round 1**: 4 ADRs drafted. ADR-002 superseded (→ ADR-002-R), ADR-004
  partially superseded by ADR-009. Round 1 required (1+ supersessions).
- **Round 2**: re-topo-sort. The only likely reshuffle is if ADR-009's
  duality classification forces a new review of PROPOSED rows that sit on the
  same split (candidates: `role`, `shape`, `expression`). If clean, round 2
  is a certification pass only — all PROPOSED → STABLE.
- **Round 3**: verification (Phase 3).

Best-case ADR count: **4**. Realistic: **4–6** if duality review in round 2
surfaces 1–2 more concepts needing explicit ADR treatment (likely `role` or
`shape`).

---

## 7. Ready to close Phase 1

- 22 OPEN rows triaged into 4 buckets.
- 4-ADR queue in dependency order with upstream deps, source weighting, and
  expected supersessions named.
- Synonym-merge list recorded for Phase 2 handling.
- Origin-layer tagging handled via existing `introduced-in` column (no
  separate pass required).

**User checkpoint before Phase 2**: approve the triage (Bucket A
OBSOLETE sweep, Bucket B Phase-4 defer, Bucket D IDR redirect) and the
4-ADR queue order.
