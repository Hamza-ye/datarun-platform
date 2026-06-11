# Decision Anchor Provenance Index

Status: reference index

This file points to the workbench artifacts behind the active decision-anchor layer. It is a reference index, not an active routing surface.

## Workbench Location

```txt
.review/001-architecture-decisions-listing-passes/
```

The `.review` folder preserves extraction chronology, audit findings, correction instructions, and promotion notes. Do not route future implementation work directly from `.review`.

## Source Roles

| Workbench file | Role |
|---|---|
| `001-recovery-strategy.md` | Recovery plan for the original ADR extraction pass. |
| `002-phase0-decision-register.md` | Recovery verification register, now lineage under CDL authority. |
| `003` through `006` | ADR recovery lineage for identity/conflict, auth/sync, config, and workflow/state. |
| `007-phase5-cross-lineage-vocabulary.md` | Recovered vocabulary lineage. |
| `008-authoritative-architecture-map.md` | Recovered architecture map for the original pass, now lineage under CDL authority. |
| `009-decision-anchor-extraction-charter.md` | Extraction method and dependency order. |
| `010-candidate-architecture-decision-inventory.md` | Historical candidate inventory. |
| `011-core-architecture-decision-records.md` | Full draft DEC corpus and Wave 2 overlay source. |
| `012-vocabulary-anchor-map.md` | Full draft vocabulary map and Wave 3 overlay source. Folded into active `gap-routing-playbook.md` as vocabulary guardrails. |
| `013-gap-routing-playbook.md` | Full draft routing playbook and legacy embedded gap examples. Folded into active `gap-routing-playbook.md`. |
| `013-known-gap-routing-register.md` | Workbench source for known-gap rows now folded into active `gap-routing-playbook.md`. |
| `014-architecture-decision-coherence-audit.md` | Original coherence audit. |
| `015-decision-anchor-correction-patch.md` | First correction patch provenance. |
| `016-stewardship-layer-assessment.md` | Adoption assessment and catch-up findings. |
| `017-systematic-catchup-and-alignment-plan.md` | Wave plan for stabilization. |
| `018-working-surface-promotion-index.md` | Promotion checklist for what became active working-surface material. |

## Direct Promotion Rule

Do not move or copy `.review` files into the active decision-anchor layer.
Extract only claims that are compatible with the CDL, contracts, BAR/NW
evidence, and current code standing. Candidate or untracked `.review` material
is audit input only unless a committed provenance index explicitly keeps it.

## Retired Companion

`docs/agent-working-surface/architecture-rationale-and-routing-companion.md` was retired as an active surface after its stable routing roles were folded into `gap-routing-playbook.md`.

Use git history for the full retired companion text. Use this directory for active routing.
