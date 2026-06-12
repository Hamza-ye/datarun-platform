# Scenarios user-fit

These packets are product/problem evidence, not architecture authority. They
were generated from a narrow packet and then reviewed against the current
working surface. Use them to understand user fit, product vocabulary, and
validation risk before drafting platform-spec work.

Input files consumed by the scenario user-fit passes:

- Foundational scenarios: [S00](../00-basic-structured-capture.md), [S01](../01-entity-linked-capture.md), [S06 and S06b](../06-entity-registry-lifecycle.md), and [S19 offline capture](../19-offline-capture-and-sync.md).
- Architecture authority: [Canonical Decision Ledger](../../architecture/adrs-decisions-canonical-ledger/canonical-decision-ledger.md), sliced through the CDL README or `scripts/query_cdl.py`.
- Problem definition files: [platform vision and ambition](../../README.md), [constraints](../../constraints.md), and [access control scenarios](../../access-control-scenario.md).
- Active routing surface: [decision anchor README](../../agent-working-surface/decision-anchor-layer/README.md), [architecture decision anchors](../../agent-working-surface/decision-anchor-layer/architecture-decision-anchors.md), and [gap routing playbook](../../agent-working-surface/decision-anchor-layer/gap-routing-playbook.md).
- Accepted standing and product readiness overlays: [baseline acceptance register](../../agent-working-surface/baseline-acceptance-register.md), [platform next-work backlog](../../agent-working-surface/platform-next-work-backlog.md), and [NW-056 product standing map](../../agent-working-surface/artifacts/NW-056-product-standing-and-production-readiness-map.md).
- Product vocabulary guardrail: [operational UX layering companion](../../agent-working-surface/operational-ux-layering-companion.md).
- Routing-only evolution context: [escape hatch register](../../agent-working-surface/escape-hatch-register.md).

Older review artifacts such as `012-vocabulary-anchor-map.md`,
`008-authoritative-architecture-map.md`, `architecture-decision-records.md`,
`architecture-vocabulary-anchor-map.md`, and `known-gaps-register.md` are
provenance only unless the active decision-anchor layer explicitly carries
their claim forward.

The [synthesis](foundational-product-fit-readiness-and-validation-matrix.md)
lists the high-risk product-fit surfaces. Vocabulary and gap files define how
to keep product terms from becoming architecture. The escape hatch register
adds one more guard: measured triggers can route evolution, but they do not
authorize implementation.

Operational/persona labels in these packets, including coordinator/setup owner,
field user, supervisor/reviewer, operator/admin, support role, and auditor,
describe a person acting in a context. They are not actor identity categories,
authority primitives, fixed UI modules, product-area names, config namespaces,
or implementation boundaries. Translate authority as:

```txt
actor + active assignment + role + scope + time + activity/context
-> available actions and visible data
-> projected operational surface
```

Human-facing review and handoff guidance is captured in
[scenario-user-fit-packets-standing-review-and-playbook.md](../../reviews/scenario-user-fit-packets-standing-review-and-playbook.md).
That file is for steward/operator use; it is not default agent routing unless a
task packet explicitly cites it.

Current standing overlay:

- Accepted scenario runtime evidence now includes S00, S19, S21, S22, S23, S26,
  and S27. Treat those as feasibility evidence for current constructs, not as
  proof that these packets are product-validated workflows.
- S06/entity lifecycle is deferred from the current baseline, not from early
  deployment planning. Subject-linked capture and bounded subject history are
  usable foundations, while full registry lifecycle, discovered-unit lifecycle,
  merge/split UX, and operational lifecycle states should be kept in the
  near-future product-deployment lane and need explicit successor routing
  before implementation.
- Reporting, import/export, broad audit/history access, custom/query scope,
  retention/security, mobile OIDC login, production admin auth, batch conflict
  handling, triggers, auto-resolution, resolver reassignment, and IdP
  group/claim authority remain visible routed lanes that need decision,
  planning, evidence, and change control before implementation.
