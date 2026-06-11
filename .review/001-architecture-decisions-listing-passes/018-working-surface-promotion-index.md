# 018 - Working Surface Promotion Index

Status: reference/provenance

This note records the Wave 5 promotion from `.review` workbench artifacts into the active working surface.

## Promoted Active Surface

```txt
docs/agent-working-surface/decision-anchor-layer/
```

Promoted files:

* `README.md`
* `architecture-decision-anchors.md`
* `gap-routing-playbook.md`
* `provenance-index.md`

## What Was Not Promoted

The following are intentionally kept out of the working surface:

* context capsules;
* handoff capsules;
* pass chronology;
* candidate inventories;
* correction-patch instructions;
* embedded legacy known-gap detail;
* detailed exploration/source-to-card provenance.

Those remain in `.review` or git history for reference.

## Source Intent

The working surface preserves the durable goals from `009` and `016`:

* future work should route through vocabulary, DEC anchors, boundaries, and closure paths;
* CDL and contracts remain authority;
* accepted BAR/NW/IDR facts are folded as durable inputs, not kept as competing decision surfaces;
* IDR-first work is frozen as provenance unless future routing explicitly selects an IDR-like artifact;
* volatile known gaps are indexed inside the active `gap-routing-playbook.md` to avoid a separate maintenance surface.

Follow-up correction:

* The promoted working surface was reduced to two active files plus provenance to avoid drift.
* Vocabulary guardrails and known-gap rows are folded into `gap-routing-playbook.md`.
