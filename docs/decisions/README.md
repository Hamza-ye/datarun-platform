# Implementation Decision Records (IDRs)

Micro-decisions made during implementation. Each file documents one choice with its context, alternatives, and consequences. Separate from [CDL](../architecture/adrs-decisions-canonical-ledger/README.md) (architecture-grade) and [Phase Specs](../implementation/phases/) (scope, deliverables, milestones).

## Durable Behavior Note

Some historically active IDRs have since been extracted into accepted platform
specifications or contracts. For NW-068 through NW-070 behavior, use these
durable targets before IDR prose:

- configuration, expression, and config package behavior:
  [Expression Language Behavior](../specifications/platform/expression-language.md),
  [Configuration Package And Shape Lifecycle](../specifications/platform/configuration-package-and-shapes.md),
  and [sync-protocol.md](../../contracts/sync-protocol.md) for
  `/api/sync/config`;
- assignment, scope, role-action, containment, and assignment-administration
  behavior:
  [Assignment Scope And Administration](../specifications/platform/assignment-scope-and-administration.md);
- production auth, OIDC/JWKS, principal binding, and deployment-managed
  principal-binding provisioning:
  [Production Auth Principal Binding](../specifications/platform/production-auth-principal-binding.md).

IDRs remain implementation provenance, rationale, and validation inputs unless
a current route explicitly says otherwise.

## When to create an IDR

Same triggers as [execution-plan.md §14](../implementation/execution-plan.md#14-implementation-decisions):

1. An IG decision is made
2. Mid-phase discovery reaches Stage 2 or 3
3. Something was tried and abandoned (>1 hour invested)
4. An environment/tooling workaround is adopted
5. Any choice where "why not X?" might be asked later

## When NOT to create an IDR

- Milestones (progress markers) → stay in phase specs
- Architecture-grade decisions → CDL
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
| **CDL** | `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.json` | Architecture constraints (stored data, cross-device contracts) |
| **IDR** | `docs/decisions/` | Implementation choices (code patterns, tooling, conventions) |
| **Product Spec** | `docs/specifications/product/` | Accepted user-visible behavior, language, journeys, and product acceptance |
| **Platform Spec** | `docs/specifications/platform/` | Accepted exact platform behavior within architecture/contracts |
| **Phase Spec** | `docs/implementation/phases/` | Scope, deliverables, quality gates, milestones |
| **Operational Policy** | `docs/operations/policies/` | Deployment-owner choices, responsibilities, controls, and escalation |
| **Runbook** | `docs/operations/runbooks/` | Repeatable executable operational procedure |
| **Rehearsal** | `docs/operations/rehearsals/` | Exercise plan and dated execution evidence |
| **Checkpoint** | `docs/checkpoints/` | Periodic project state snapshots |

One concern = one canonical location. Link related documents instead of
duplicating normative content. See
`docs/documentation-organization.md` for the complete routing and traceability
standard.
