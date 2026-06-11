# Decision Anchor Layer

Status: active stewardship routing surface

This directory is the cleaned working-surface version of the `.review/001-architecture-decisions-listing-passes/` decision-anchor workbench.

It exists to make future work traceable without forcing agents to read the extraction chronology.

## Source Order

Use sources in this order:

1. `docs/architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md`
   - Architecture authority.
   - Slice with `docs/architecture/adrs-decisions-canonical-ledger/README.md` or `scripts/query_cdl.py`.
2. `contracts/`
   - Wire, schema, sync, flag, shape, pattern, and shared-fixture contracts.
3. `docs/agent-working-surface/decision-anchor-layer/`
   - Operational stewardship layer: DEC anchors and gap routing.
4. `docs/agent-working-surface/baseline-acceptance-register.md`
   - Accepted implementation standing and evidence.
5. `docs/agent-working-surface/platform-next-work-backlog.md`
   - Accepted and future-decision work routing evidence.
6. `docs/decisions/`
   - Existing IDR provenance and validation inputs.

If this layer conflicts with the CDL, contracts, BAR, NW backlog, or current code evidence, patch this layer or stop and report the drift.

## Surface Goals

This layer carries the durable goals from the extraction charter and the stewardship assessment:

* make future work mechanically traceable from pressure to DEC anchor, vocabulary guardrail, boundary, and closure route;
* prevent product/problem, implementation, operational policy, and platform-spec details from becoming architecture by accident;
* keep CDL authority intact while providing a smaller consumable surface;
* fold durable IDR/BAR/NW outcomes into stable anchors and freeze IDR-first routing as provenance;
* avoid maintaining several active routing companions in parallel.

## Files

| File | Role |
|---|---|
| `architecture-decision-anchors.md` | Active `011` surface: DEC-to-CDL map, accepted extension inputs, and current caveats. |
| `gap-routing-playbook.md` | Active `013` surface: routing rules, vocabulary guardrails, known-gap register, and implementation prompt checklist. |
| `provenance-index.md` | Reference index back to `.review` workbench artifacts and retired companion material. |

## Maintenance Rule

Patch upstream first:

```txt
architecture-decision-anchors.md
-> gap-routing-playbook.md
```

Do not update active routers from raw `.review` artifacts. Use `.review` only as provenance, reference, or audit material.
