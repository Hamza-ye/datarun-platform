---
name: datarun-truth-freshness
description: Check whether Datarun's charter, concept ledger, ADR supersession state, flagged-position register, contracts, and latest git history agree before Ship work, ADR work, convergence repair, phase/framework planning, or any task that depends on current architecture truth.
---

# Datarun Truth Freshness

## Purpose

Use this skill before planning or executing Datarun work that depends on current architecture truth. The goal is to detect when the mechanical drift gate passes but the charter or ledger is semantically stale relative to newer git commits, ADRs, contracts, or FP resolutions.

## Workflow

1. Run the deterministic report:

   ```bash
   bash skills/codex/datarun-truth-freshness/scripts/truth_freshness.sh
   ```

2. Read the final `VERDICT` first, then inspect the numbered sections above it.

3. If the verdict is `DRIFT-GATE-FAIL`, fix the gate failure before any other work.

4. If the verdict is `MECHANICALLY-CLEAN-BUT-STALE`, do truth repair before Ship/framework work:
   - update ledger rows for new or re-cited concepts;
   - update ADR supersession headers if needed;
   - regenerate or reconcile `docs/charter.md`;
   - update charter FP status summaries;
   - rerun the report.

5. If the verdict is `BLOCKED-BY-DIRTY-WORKTREE`, report the dirty authority files and avoid edits until the user confirms scope.

6. If the verdict is `FRESH`, proceed to the requested Ship, ADR, or planning task.

## Verdict Meanings

- `FRESH`: drift gate passes and no freshness issues were detected.
- `MECHANICALLY-CLEAN-BUT-STALE`: drift gate passes, but git/ADR/FP facts are not reflected in charter or ledger.
- `DRIFT-GATE-FAIL`: `scripts/check-convergence.sh` failed.
- `BLOCKED-BY-DIRTY-WORKTREE`: authority files are dirty, so freshness cannot be trusted without user direction.

## Rules

- Do not treat `docs/charter.md` as fresh just because the drift gate passes.
- Do not start Ship code when the latest ADR or resolved FP is absent from charter status.
- Do not create another long prose audit unless the user asks. Use the script output as the audit surface.
- Preserve Datarun's authority order: charter/ledger/convergence rules -> ADRs -> architecture docs -> implementation docs -> code.
