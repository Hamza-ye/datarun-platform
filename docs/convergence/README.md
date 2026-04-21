# Convergence Scaffolding

Temporary directory housing the convergence protocol, concept ledger, and
supporting rules. Active from 2026-04-22 through Phase 4 freeze. At freeze,
this directory moves to `docs/exploration/99-convergence-archive/`.

## Files

| file | lifespan | purpose |
|---|---|---|
| `protocol.md` | temporary | the fixpoint loop convergence runs |
| `concept-ledger.md` | temporary (archived) | inventory of every named concept |
| `supersede-rules.md` | permanent (moves to `docs/adrs/`) | how to retire ADRs |
| `annotation-conventions.md` | permanent | how to annotate archival docs |
| `cold-start.md` | permanent | the five-read contract for fresh agents |

## Sibling files (outside this directory)

| file | role |
|---|---|
| `docs/charter.md` | the distilled current decided state |
| `scripts/check-convergence.sh` | drift gate (pre-commit + CI) |

## Read order for fresh agents

See `cold-start.md`. In short: protocol → ledger → charter → CLAUDE.md
codebase map → run drift gate.
