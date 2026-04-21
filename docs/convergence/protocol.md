# Convergence Protocol

> **Lifespan**: temporary scaffolding. Lives from drafting (2026-04-22) through
> Phase 4 freeze. At freeze, this file moves to `docs/exploration/` as a
> historical artifact and is no longer authoritative.

## Why this exists

Architecture was drafted as a forward-reference graph: early ADRs/exploration
docs assumed downstream decisions would land a certain way. They did not always
land that way. Some early "invariants" turned out to be config knobs; some
"primitives" turned out to be derived; some "open questions" got silently
closed by an unrelated ADR. The result: stale claims in `CLAUDE.md`,
`docs/architecture/*`, and exploration docs that confuse every fresh-context
agent.

This protocol is a **fixpoint loop**, not a linear walk. It runs until the
concept ledger stops changing across a full round.

## Phases

### Phase 0 — Inventory
Walk every doc that contains a named concept (exploration, ADRs, architecture,
phases, scenarios, walk-throughs, checkpoints, experiments, contracts).
For each named concept, emit one row in `concept-ledger.md` with:
  - current best-guess classification
  - the doc that introduced it
  - any open question it carries

No decisions yet. Inventory only.

**Exit criterion**: every named concept in the repo has a row.

### Phase 1 — Topological sort
From the ledger, derive the dependency graph: which concepts must be settled
before others. Produce a draft ADR queue, root-first. The queue is a best-guess
order; supersessions in later rounds will reshuffle it.

**Exit criterion**: every OPEN concept has a draft ADR in the queue.

### Phase 2 — Draft-with-closure (the loop)
For each ADR in the queue:
  1. Read all upstream DECIDED ADRs and the current concept ledger.
  2. Draft the ADR.
  3. For every concept the ADR touches:
       - update the ledger row's classification + history
       - if the new classification differs from the ledger's current value:
         the prior ADR that set that value is **superseded** (full or partial)
         per `supersede-rules.md`
  4. In the **same commit**:
       - land the ADR (`docs/adrs/adr-NNN.md`, status DECIDED)
       - rewrite affected `docs/architecture/*` files
       - annotate exploration docs the ADR closes (per `annotation-conventions.md`)
       - regenerate `charter.md`
       - update `concept-ledger.md`
       - if any prior ADR was superseded, update its header
  5. Run `scripts/check-convergence.sh`. If it fails, fix and re-commit before
     the next ADR.

**Round-end check** (after every ADR in the queue is drafted once):
  - If any ADR was superseded this round → another round is required.
    Re-topo-sort the queue (supersessions can change dependencies) and repeat.
  - If zero supersessions AND zero open forward-refs → CONVERGED. Go to Phase 3.

**Anti-cycling rule**: a single concept may not flip between the same two
classifications more than twice. On the third flip, the next ADR draft must
either (a) commit to one classification with explicit rationale, or (b) declare
the concept DEFERRED with a named blocking condition.

### Phase 3 — Verification walk
Re-walk every annotated exploration doc in original numeric order. Confirm
every `>>> OPEN-Q:` has a matching `>>> CLOSED BY`, `>>> STALE`, or
`>>> RECLASSIFIED BY`. Run `scripts/check-convergence.sh` in strict mode.

**Exit criterion**: drift gate passes; ledger has zero rows in OPEN status.

### Phase 4 — Freeze
- `charter.md` becomes immutable except via new ADR + same-commit regen.
- `convergence/` directory moves to `docs/exploration/99-convergence-archive/`.
- `flagged-positions.md` moves to the same archive — its entries should all be
  closed by ADRs by this point.
- `CLAUDE.md` is stripped: keep only (a) a 10-line pointer to charter.md and
  (b) the codebase map.
- Drift gate moves into CI as a hard build failure.
- Code work resumes.

## What this protocol does NOT permit

- Editing a DECIDED ADR. Wrong ADRs are superseded, never amended.
- Adding an Addendum to an ADR. The current ADR-002 Addendum becomes ADR-002-R
  when convergence reaches it.
- Code changes during Phase 0–3. The protocol is docs-only until freeze.
- Adding new authoritative surfaces. The four permanent surfaces are: ADRs,
  architecture/, charter.md, codebase map. Nothing else.

## Cold-start

Any agent (human or AI) returning to convergence work, especially after a
context reset, reads `cold-start.md` first. It is the five-line contract that
gets us back to where we left off in one read.
