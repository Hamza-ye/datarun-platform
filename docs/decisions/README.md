# Implementation Decision Records (IDRs)

Micro-decisions made during implementation. Each file documents one choice with its context, alternatives, and consequences. Separate from [ADRs](../adrs/) (architecture-grade) and [Phase Specs](../implementation/phases/) (scope, deliverables, milestones).

## When to create an IDR

Same triggers as [execution-plan.md §14](../implementation/execution-plan.md#14-implementation-decisions):

1. An IG decision is made
2. Mid-phase discovery reaches Stage 2 or 3
3. Something was tried and abandoned (>1 hour invested)
4. An environment/tooling workaround is adopted
5. Any choice where "why not X?" might be asked later

## When NOT to create an IDR

- Milestones (progress markers) → stay in phase specs
- Architecture-grade decisions → ADRs
- Obvious, uncontroversial choices with no rejected alternatives

## How to find relevant IDRs

- Browse [INDEX.md](INDEX.md) for tables by phase, component, and tag
- Grep frontmatter: `grep -l 'touches:.*server/sync' docs/decisions/idr-*.md`
- Check before modifying code that implements a non-obvious pattern

## Template

Copy [TEMPLATE.md](TEMPLATE.md). Fill in the frontmatter and sections. Aim for 20–40 lines.

### Frontmatter field: `reversal-cost`

Added to support the [Reversibility Triage](../implementation/execution-plan.md#61-reversibility-triage-required-step) framework. Values:

- `high` — **Lock** decisions: reversal requires data migration, protocol change, or coordinated client+server rework
- `low` — **Lean** decisions: reversal is code-only (no data migration, no protocol change, no contract change)
- `~` — not classified (Leaf decisions don't get IDRs)

## Documentation map

| Type | Location | Contains |
|------|----------|----------|
| **ADR** | `docs/adrs/` | Architecture constraints (stored data, cross-device contracts) |
| **IDR** | `docs/decisions/` | Implementation choices (code patterns, tooling, conventions) |
| **Phase Spec** | `docs/implementation/phases/` | Scope, deliverables, quality gates, milestones |
| **Checkpoint** | `docs/checkpoints/` | Periodic project state snapshots |

One concern = one location. No duplication between these.
