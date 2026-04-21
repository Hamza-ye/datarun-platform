# Cold-Start Contract

> **Lifespan**: permanent. The five-read contract for any agent (human or AI)
> entering the repo, especially after context reset.

## The five reads — in order, no exceptions

1. **`docs/convergence/protocol.md`** — what convergence phase are we in?
2. **`docs/convergence/concept-ledger.md`** — what's classified, what isn't?
3. **`docs/charter.md`** — what's currently decided?
4. **`CLAUDE.md`** — codebase map section ONLY (where code lives).
5. **`scripts/check-convergence.sh`** — run it. If it fails, fix the drift
   before doing anything else.

## What you do NOT read upfront

- Individual ADRs — read on-demand when the charter or ledger cites them.
- `docs/architecture/*` — read on-demand. Authoritative for current state but
  the charter distills what matters.
- Exploration docs / scenarios / walk-throughs / checkpoints / experiments /
  phase specs / IDRs / status.md / lessons.md / flagged-positions.md — all
  reference material. Read only when a specific question requires them.

## Why this contract exists

Without it, every fresh session re-reads CLAUDE.md and inherits whatever stale
prose it has accumulated. The protocol forbids stale prose in the charter
(drift gate enforces). So routing cold-start through charter.md, not CLAUDE.md
prose, eliminates the failure mode.

## What you do after the five reads

If `scripts/check-convergence.sh` passed:
  - Phase 0–3: continue convergence work per `protocol.md`.
  - Phase 4 (frozen): read the active phase spec named in `charter.md` and
    proceed with code work.

If it failed:
  - Fix the drift before any other work. The script's output names exactly
    which file/cite is wrong. No exceptions, no "I'll fix it later." The
    drift gate is the single mechanism that prevents the repo state we are
    currently working to escape.
